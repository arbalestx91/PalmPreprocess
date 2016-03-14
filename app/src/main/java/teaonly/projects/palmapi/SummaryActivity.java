package teaonly.projects.palmapi;

import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class SummaryActivity extends AppCompatActivity {

    private TextView txtSummary = (TextView) findViewById(R.id.txtSummary);
    private Button btnEmail = (Button) findViewById(R.id.btnEmail);

    private String emailString = getIntent().getStringExtra("emailString");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_summary);
        txtSummary.setText(emailString);

        btnEmail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendEmail(emailString.toString());
            }
        });
    }

    protected void sendEmail(String string) {
        Intent emailIntent = new Intent(Intent.ACTION_SENDTO);
        emailIntent.setData(Uri.parse("mailto:" + "fyp@arbalestx.com"));
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, "FYP");
        emailIntent.putExtra(Intent.EXTRA_TEXT, string);

        try {
            startActivity(Intent.createChooser(emailIntent, "Send email using..."));
        } catch (android.content.ActivityNotFoundException ex) {
            Toast.makeText(getApplicationContext(), "No email clients installed.", Toast.LENGTH_SHORT).show();
        }
    }
}
