package teaonly.projects.palmapi;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

public class InterActivity extends Activity {

    private static final int DEFAULT_ROI = 64;              // Size of ROI in pixel
    private static final int SCAN_FIRST = 1;
    private static final int SCAN_SECOND = 2;
    private static final int READ_BLOCK_SIZE = 255;

    private Bitmap palmPrintP1;
    private Bitmap[] palmPrintP2 = new Bitmap[8];
    private Bitmap originalPrintP2;
    private ImageView imgP1, imgP2, imgP21, imgP22, imgP23, imgP24, imgP25, imgP26, imgP27, imgP28;
    private TextView txtP1, txtP2, txtSize;
    private Button btnP1, btnP2, btnCompare,btnLogP1, btnLogP2, btnSubmit;
    private String binaryP1, binaryP2;
    StringBuilder emailString = new StringBuilder();
    private int roiSize = DEFAULT_ROI;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.inter);

        imgP1 = (ImageView) findViewById(R.id.imgP1);
        imgP2 = (ImageView) findViewById(R.id.imgP2);
        imgP21 = (ImageView) findViewById(R.id.imgP21);
        imgP22 = (ImageView) findViewById(R.id.imgP22);
        imgP23 = (ImageView) findViewById(R.id.imgP23);
        imgP24 = (ImageView) findViewById(R.id.imgP24);
        imgP25 = (ImageView) findViewById(R.id.imgP25);
        imgP26 = (ImageView) findViewById(R.id.imgP26);
        imgP27 = (ImageView) findViewById(R.id.imgP27);
        imgP28 = (ImageView) findViewById(R.id.imgP28);
        btnP1 = (Button) findViewById(R.id.btnP1);
        btnLogP1 = (Button) findViewById(R.id.btnLogP1);
        btnP2 = (Button) findViewById(R.id.btnP2);
        btnLogP2 = (Button) findViewById(R.id.btnLogP2);
        btnCompare = (Button) findViewById(R.id.btnCompare);
        btnSubmit = (Button) findViewById(R.id.btnSubmit);
        txtP1 = (TextView) findViewById(R.id.txtP1);
        txtP2 = (TextView) findViewById(R.id.txtP2);
        txtSize = (TextView) findViewById(R.id.txtSize);

        emailString.append("Default ROI Size: 64px");
        emailString.append("\n");

        btnP1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent P1Intent = new Intent(getApplicationContext(), MainActivity.class);
                startActivityForResult(P1Intent, SCAN_FIRST);
            }
        });

        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                roiSize = Integer.parseInt(txtSize.getText().toString());
                emailString.append("Size of Size Changed: ");
                emailString.append(String.valueOf(roiSize));
                emailString.append("px");
                emailString.append("\n\n");
                Toast.makeText(getApplicationContext(), "Successfully changed crop size", Toast.LENGTH_LONG).show();
            }
        });

//        btnLogP1.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Toast.makeText(getApplicationContext(), "Binary: " + readFromFile("P1.txt").length(), Toast.LENGTH_LONG).show();
//            }
//        });

        btnP2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent P2Intent = new Intent(getApplicationContext(), MainActivity.class);
                startActivityForResult(P2Intent, SCAN_SECOND);
            }
        });

//        btnLogP2.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Toast.makeText(getApplicationContext(), "Binary: " + readFromFile("P1.txt").length(), Toast.LENGTH_LONG).show();
//            }
//        });

        btnCompare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Convert to byte[]
                // Foreach byte in byte[], convert to binary
                // XOR binary append to string
                long startTime, endTime, totalDuration, d;
                totalDuration = 0;
                byte[] byteArrayP1 = bmpToByteArray(palmPrintP1);
                byte[] byteArrayP2 = bmpToByteArray(palmPrintP2[0]);
                byte[] byteArrayP21 = bmpToByteArray(palmPrintP2[1]);
                byte[] byteArrayP22 = bmpToByteArray(palmPrintP2[2]);
                byte[] byteArrayP23 = bmpToByteArray(palmPrintP2[3]);
                byte[] byteArrayP24 = bmpToByteArray(palmPrintP2[4]);
                byte[] byteArrayP25 = bmpToByteArray(palmPrintP2[5]);
                byte[] byteArrayP26 = bmpToByteArray(palmPrintP2[6]);
                byte[] byteArrayP27 = bmpToByteArray(palmPrintP2[7]);

                startTime = System.nanoTime();
                compareImg(byteArrayP1, byteArrayP2, 0);
                endTime = System.nanoTime();
                d = (endTime - startTime) / 1000000;
                emailString.append("Time cost of P1 vs P2[0]: ");
                emailString.append(String.valueOf(d));
                emailString.append("ms");
                emailString.append("\n\n");
                totalDuration += d;

                startTime = System.nanoTime();
                compareImg(byteArrayP1, byteArrayP21, 1);
                endTime = System.nanoTime();
                d = (endTime - startTime) / 1000000;
                emailString.append("Time cost of P1 vs P2[1]: ");
                emailString.append(String.valueOf(d));
                emailString.append("ms");
                emailString.append("\n\n");
                totalDuration += d;

                startTime = System.nanoTime();
                compareImg(byteArrayP1, byteArrayP22, 2);
                endTime = System.nanoTime();
                d = (endTime - startTime) / 1000000;
                emailString.append("Time cost of P1 vs P2[2]: ");
                emailString.append(String.valueOf(d));
                emailString.append("ms");
                emailString.append("\n\n");
                totalDuration += d;

                startTime = System.nanoTime();
                compareImg(byteArrayP1, byteArrayP23, 3);
                endTime = System.nanoTime();
                d = (endTime - startTime) / 1000000;
                emailString.append("Time cost of P1 vs P2[1]: ");
                emailString.append(String.valueOf(d));
                emailString.append("ms");
                emailString.append("\n\n");
                totalDuration += d;

                startTime = System.nanoTime();
                compareImg(byteArrayP1, byteArrayP24, 4);
                endTime = System.nanoTime();
                d = (endTime - startTime) / 1000000;
                emailString.append("Time cost of P1 vs P2[4]: ");
                emailString.append(String.valueOf(d));
                emailString.append("ms");
                emailString.append("\n\n");
                totalDuration += d;

                startTime = System.nanoTime();
                compareImg(byteArrayP1, byteArrayP25, 5);
                endTime = System.nanoTime();
                d = (endTime - startTime) / 1000000;
                emailString.append("Time cost of P1 vs P2[5]: ");
                emailString.append(String.valueOf(d));
                emailString.append("ms");
                emailString.append("\n\n");
                totalDuration += d;

                startTime = System.nanoTime();
                compareImg(byteArrayP1, byteArrayP26, 6);
                endTime = System.nanoTime();
                d = (endTime - startTime) / 1000000;
                emailString.append("Time cost of P1 vs P2[6]: ");
                emailString.append(String.valueOf(d));
                emailString.append("ms");
                emailString.append("\n\n");
                totalDuration += d;

                startTime = System.nanoTime();
                compareImg(byteArrayP1, byteArrayP27, 7);
                endTime = System.nanoTime();
                d = (endTime - startTime) / 1000000;
                emailString.append("Time cost of P1 vs P2[7]: ");
                emailString.append(String.valueOf(d));
                emailString.append("ms");
                emailString.append("\n\n");
                totalDuration += d;

                emailString.append("Total duration for 8 comparisons: ");
                emailString.append(String.valueOf(totalDuration));
                emailString.append("ms");
                emailString.append("\n");
                emailString.append("Average duration per comparison: ");
                emailString.append(String.valueOf(totalDuration/8));
                emailString.append("ms");

                Intent summaryIntent = new Intent(getApplicationContext(), SummaryActivity.class);
                summaryIntent.putExtra("emailString", emailString.toString());
                startActivity(summaryIntent);
            }
        });
    }

    protected void compareImg(byte[] byteArrayP1, byte[] byteArrayP2, int k) {

        int i = 0;
        int intBinary;
        double differencePercentage;
        int offCounter = 0;                                                                     // Counts the number of different bits. 1 XOR 0 and 0 XOR 1
        StringBuilder sb = new StringBuilder();
        String binaryStrP1, binaryStrP2, binaryResult;

        for (byte b : byteArrayP1) {
            binaryStrP1 = Integer.toBinaryString(b & 0xFF).replace(' ', '0');
            binaryStrP2 = Integer.toBinaryString(byteArrayP2[i++] & 0xFF).replace(' ', '0');

            intBinary = Integer.parseInt(binaryStrP1) ^ Integer.parseInt(binaryStrP2);
            binaryResult = Integer.toBinaryString(intBinary);
            sb.append(binaryResult);
        }

        binaryResult = sb.toString();
        for (char c : binaryResult.toCharArray()) {
            if (Integer.parseInt(String.valueOf(c)) == 1) {
                offCounter++;
            }
        }
        differencePercentage = (double) offCounter / binaryResult.length() * 100;

        emailString.append("P1 vs P2[");
        emailString.append(String.valueOf(k));
        emailString.append("]: \t");
        emailString.append(String.valueOf(100 - differencePercentage));
        emailString.append("%");
        emailString.append("\n");
    }

    /**
     * Method to crop center of image and extract ROI
     * @param bmp
     * @return
     */
    protected Bitmap centerCrop(Bitmap bmp, int width, int height) {
        return Bitmap.createBitmap(bmp, width, height, roiSize, roiSize);
    }
//    protected Bitmap centerCrop(Bitmap bmp) {
//        return Bitmap.createBitmap(bmp, bmp.getWidth()/2, bmp.getHeight()/2, 64, 64);
//    }

    /**
     * Method to convert bitmap images to bytearray
     * @param bmp
     * @return
     */
    protected byte[] bmpToByteArray(Bitmap bmp) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.PNG, 50, stream);
        byte[] byteArray = stream.toByteArray();

        return byteArray;
    }

    /**
     *
     * @param requestCode an int constant assigned to each startActivityForResult. It is used to identify which activity is returning the result
     * @param resultCode used to identify if the activity returned a successful result.
     * @param intent used to retrieve extras(data) from intent
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (resultCode == RESULT_OK) {
            if (requestCode == SCAN_FIRST) {
                byte[] byteArray = intent.getByteArrayExtra("palmImg");
                long scanTime = intent.getLongExtra("time", 0);
                palmPrintP1 = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);
                palmPrintP1 = centerCrop(palmPrintP1, palmPrintP1.getWidth() / 2, palmPrintP1.getHeight() / 2);
                imgP1.setImageBitmap(palmPrintP1);
//                binaryP1 = byteArrayToBinary(byteArray);
//                btnLogP1.setVisibility(View.VISIBLE);
                emailString.append("P1 Scan Time: ");
                emailString.append(String.valueOf(scanTime));
                emailString.append("ms");
                emailString.append("\n\n");
//                writeToFile("P1.txt", binaryP1);
            } else if (requestCode == SCAN_SECOND) {
                byte[] byteArray = intent.getByteArrayExtra("palmImg");
                long scanTime = intent.getLongExtra("time", 0);
                originalPrintP2 = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);
//                binaryP2 = byteArrayToBinary(byteArray);
//                btnLogP2.setVisibility(View.VISIBLE);
//                writeToFile("P2.txt", binaryP2);
                // Original
                palmPrintP2[0] = centerCrop(originalPrintP2, originalPrintP2.getWidth() / 2, originalPrintP2.getHeight() / 2);
                imgP2.setImageBitmap(palmPrintP2[0]);
                // X-1, Y-1
                palmPrintP2[1] = centerCrop(originalPrintP2, (originalPrintP2.getWidth() / 2) - 1, (originalPrintP2.getHeight() / 2) - 1);
                imgP21.setImageBitmap(palmPrintP2[1]);
                // X, Y-1
                palmPrintP2[2] = centerCrop(originalPrintP2, (originalPrintP2.getWidth() / 2), (originalPrintP2.getHeight() / 2) - 1);
                imgP22.setImageBitmap(palmPrintP2[2]);
                // X+1, Y-1
                palmPrintP2[3] = centerCrop(originalPrintP2, (originalPrintP2.getWidth() / 2) + 1, (originalPrintP2.getHeight() / 2) - 1);
                imgP23.setImageBitmap(palmPrintP2[3]);
                // X-1, Y
                palmPrintP2[4] = centerCrop(originalPrintP2, (originalPrintP2.getWidth() / 2) - 1, (originalPrintP2.getHeight() / 2));
                imgP24.setImageBitmap(palmPrintP2[4]);
                // X+1, Y
                palmPrintP2[5] = centerCrop(originalPrintP2, (originalPrintP2.getWidth() / 2) + 1, (originalPrintP2.getHeight() / 2));
                imgP25.setImageBitmap(palmPrintP2[5]);
                // X-1, Y+1
                palmPrintP2[6] = centerCrop(originalPrintP2, (originalPrintP2.getWidth() / 2) - 1, (originalPrintP2.getHeight() / 2) + 1);
                imgP26.setImageBitmap(palmPrintP2[6]);
                // X, Y+1
                palmPrintP2[7] = centerCrop(originalPrintP2, (originalPrintP2.getWidth() / 2), (originalPrintP2.getHeight() / 2) + 1);
                imgP27.setImageBitmap(palmPrintP2[7]);
                // X+1, Y+1
//                palmPrintP2[8] = centerCrop(originalPrintP2, (originalPrintP2.getWidth()/2) + 1, (originalPrintP2.getHeight()/2) + 1);
//                imgP28.setImageBitmap(palmPrintP2[8]);
                emailString.append("P2 Scan Time: ");
                emailString.append(String.valueOf(scanTime));
                emailString.append("ms");
                emailString.append("\n\n");
            }
        }
    }

    /**
     * Write binary string to a file.
     * @param filename Name for the output file
     * @param binaryString Binary output for the given image (palmprint)
     */
//    protected void writeToFile(String filename, String binaryString) {
//        try {
//            File file = new File(getFilesDir() + "/text/", filename);
//            if (file.exists()) {
//                file.delete();
//            }
//            file.createNewFile();
//            FileOutputStream fos = new FileOutputStream(file);
//            OutputStreamWriter osw = new OutputStreamWriter(fos);
//
//            osw.write(binaryString);
//            osw.close();
//            fos.close();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
//
//    protected String readFromFile(String filename) {
//        String ret = "";
//        File file = new File(getFilesDir() + "/text/" + filename);
//
//        try {
//            if (file.exists()) {
//                BufferedReader bufferedReader = new BufferedReader(new FileReader(file));
//                String receiveString = "";
//                StringBuilder stringBuilder = new StringBuilder();
//
//                while ((receiveString = bufferedReader.readLine()) != null) {
//                    stringBuilder.append(receiveString);
//                }
//                bufferedReader.close();
//                ret = stringBuilder.toString();
//            }
//        } catch (FileNotFoundException e) {
//            e.printStackTrace();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        return ret;
//    }

    /**
     * Convert the bytearray output of the bitmap image into binary string
     * @param byteArray The array of bytes representing the given image (palmprint)
     * @return String of "1" & "0", binary
     */
//    private String byteArrayToBinary (byte[] byteArray) {
//        byte[] byteArray;
//        String resultBinary = "";
//        StringBuilder sb = new StringBuilder();
//        ByteArrayOutputStream stream;
//        stream = new ByteArrayOutputStream();
//        bmp.compress(Bitmap.CompressFormat.PNG, 100, stream);
//        byteArray = stream.toByteArray();
//        for (byte b : byteArray) {
//            resultBinary = String.format("%s", Integer.toBinaryString(b & 0xFF)).replace(' ', '0');
//            sb.append(resultBinary);
//        }
//        Toast.makeText(getApplicationContext(), "total length = " + sb.length(), Toast.LENGTH_LONG).show();
//        return sb.toString();
//    }
}
