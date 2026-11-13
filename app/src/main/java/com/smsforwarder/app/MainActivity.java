package com.smsforwarder.app;

import android.Manifest;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.work.Constraints;
import androidx.work.Data;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(50, 60, 50, 50);

        TextView title = new TextView(this);
        title.setText("n8n Webhook URL:");
        title.setTextSize(18);
        layout.addView(title);

        EditText urlInput = new EditText(this);
        urlInput.setHint("https://your-n8n-url/webhook/...");
        SharedPreferences prefs = getSharedPreferences("SMSPrefs", MODE_PRIVATE);
        urlInput.setText(prefs.getString("webhook_url", ""));
        layout.addView(urlInput);

        Button saveBtn = new Button(this);
        saveBtn.setText("Save Webhook");
        saveBtn.setOnClickListener(v -> {
            String url = urlInput.getText().toString().trim();
            prefs.edit().putString("webhook_url", url).apply();
            Toast.makeText(this, "Webhook URL Saved!", Toast.LENGTH_SHORT).show();
        });
        layout.addView(saveBtn);

        Button testBtn = new Button(this);
        testBtn.setText("Send Test Ping to n8n");
        testBtn.setOnClickListener(v -> {
            Data data = new Data.Builder().putString("payload", "TEST|Test ping from custom Android app").build();
            Constraints constraints = new Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build();
            OneTimeWorkRequest req = new OneTimeWorkRequest.Builder(SendWorker.class).setInputData(data).setConstraints(constraints).build();
            WorkManager.getInstance(this).enqueue(req);
            Toast.makeText(this, "Test sent to queue!", Toast.LENGTH_SHORT).show();
        });
        layout.addView(testBtn);

        TextView status = new TextView(this);
        status.setText("\nStatus: Active 24/7 in background.\nRemember to set Battery to Unrestricted in Phone Settings.");
        status.setTextSize(14);
        layout.addView(status);

        setContentView(layout);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECEIVE_SMS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECEIVE_SMS, Manifest.permission.READ_SMS}, 101);
        }
    }
}
