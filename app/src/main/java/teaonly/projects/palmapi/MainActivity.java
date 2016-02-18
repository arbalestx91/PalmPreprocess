package teaonly.projects.palmapi;

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.Thread;
import java.nio.ByteBuffer;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.hardware.Camera;
import android.hardware.Camera.PreviewCallback;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.MotionEvent;
import android.view.Window;
import android.view.WindowManager;
import android.view.SurfaceView;
import android.widget.Button;
import android.widget.Toast;

public class MainActivity extends Activity implements View.OnTouchListener, CameraView.CameraReadyCallback, OverlayView.UpdateDoneCallback {
    private CameraView cameraView_;
    private OverlayView overlayView_;
    private ProgressDialog processDialog = null;
    private Button btnSubmit;
    private AppState state_ = AppState.INITED;
    private byte[] rawFrame_ = null;
    private byte[] labelFrame_ = null;
    private Bitmap resultBMP_;
    private Bitmap palmPrint = null;
    private long startTime, endTime, duration;
    private boolean labelProcessing_ = false;
    private LabelThread labelThread_ = null;
    private ProcThread procThread_ = null;
    private PreviewCallback previewCb_ = new PreviewCallback() {
        public void onPreviewFrame(byte[] frame, Camera c) {
            if (state_ != AppState.LABELING)
                return;

            if (labelProcessing_)
                return;

            labelProcessing_ = true;
            int wid = cameraView_.PreviewWidth();
            int hei = cameraView_.PreviewHeight();

            ByteBuffer bbuffer = ByteBuffer.wrap(frame);
            bbuffer.get(rawFrame_, 0, wid * hei + wid * hei / 2);

            waitCompleteLastLabeling();
            labelThread_ = new LabelThread();
            labelThread_.start();
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        NativeAPI.LoadLibraries();

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        Window win = getWindow();
        win.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        win.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.main);

        SurfaceView cameraSurface = (SurfaceView) findViewById(R.id.surface_camera);
        cameraView_ = new CameraView(cameraSurface, 640, 480, 640, 480);
        cameraView_.setCameraReadyCallback(this);

        btnSubmit = (Button) findViewById(R.id.btnSubmit);
        overlayView_ = (OverlayView) findViewById(R.id.surface_overlay);
        overlayView_.setOnTouchListener(this);
        overlayView_.setUpdateDoneCallback(this);

        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Scale the image
                palmPrint = Bitmap.createScaledBitmap(resultBMP_, (int) (resultBMP_.getWidth() * 0.5), (int) (resultBMP_.getHeight() * 0.5), true);
//                palmPrint = Bitmap.createBitmap(palmPrint, palmPrint.getWidth()/2, palmPrint.getHeight()/2 - palmPrint.getWidth()/2, 64, 64);
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                palmPrint.compress(Bitmap.CompressFormat.PNG, 50, stream);
                byte[] byteArray = stream.toByteArray();

                Intent returnIntent = new Intent();
                returnIntent.putExtra("palmImg", byteArray);
                returnIntent.putExtra("time", duration);
                setResult(RESULT_OK, returnIntent);
                finish();
            }
        });
    }

    @Override
    public void onCameraReady() {
        int wid = cameraView_.PreviewWidth();
        int hei = cameraView_.PreviewHeight();
        rawFrame_ = new byte[wid * hei + wid * hei / 2];
        labelFrame_ = new byte[wid * hei / 2];
        resultBMP_ = Bitmap.createBitmap(overlayView_.getWidth(), overlayView_.getHeight(), Bitmap.Config.ARGB_8888);

        NativeAPI.nativePrepare(wid, hei, 2);

        state_ = AppState.LABELING;
        cameraView_.SetPreview(previewCb_);
    }

    @Override
    public void onUpdateDone() {
        labelProcessing_ = false;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public boolean onTouch(View v, MotionEvent evt) {
        if (state_ == AppState.LABELING) {
            startTime = System.nanoTime();
            cameraView_.SetPreview(null);

            processDialog = ProgressDialog.show(this, "", "Processing...", true);
            processDialog.show();

            waitCompleteLastLabeling();
            labelProcessing_ = false;
            state_ = AppState.PROCESSING;
            cameraView_.StopPreview();
            procThread_ = new ProcThread();
            procThread_.start();
            endTime = System.nanoTime();
            duration = (endTime - startTime) / 1000000;
        } else if (state_ == AppState.DISPLAY_SHOW) {
            state_ = AppState.LABELING;
            cameraView_.SetPreview(previewCb_);
            cameraView_.StartPreview();
        }
        return false;
    }

    private void waitCompleteLastLabeling() {
        if (labelThread_ == null)
            return;

        if (labelThread_.isAlive()) {
            try {
                labelThread_.join();
                labelThread_ = null;
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private enum AppState {
        INITED, LABELING, PROCESSING, DISPLAY_SHOW,
    }

    private class LabelThread extends Thread {
        private int width = cameraView_.PreviewWidth();
        private int height = cameraView_.PreviewHeight();

        @Override
        public void run() {
            resultBMP_.eraseColor(Color.TRANSPARENT);
            NativeAPI.nativeLabelPalm(rawFrame_, labelFrame_, resultBMP_);
            overlayView_.DrawResult(resultBMP_);
        }
    }

    private class ProcThread extends Thread {

        @Override
        public void run() {
            resultBMP_.eraseColor(Color.TRANSPARENT);
            NativeAPI.nativeEnhencePalm(labelFrame_, rawFrame_, resultBMP_);
            overlayView_.DrawResult(resultBMP_);
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                public void run() {
                    if (processDialog != null) {
                        processDialog.dismiss();
                        processDialog = null;
                        state_ = AppState.DISPLAY_SHOW;
                    }
                }
            });
        }
    }

}
