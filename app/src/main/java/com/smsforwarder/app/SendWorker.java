package com.smsforwarder.app;

import android.content.Context;
import android.content.SharedPreferences;
import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class SendWorker extends Worker {

    public SendWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
    }

    @NonNull
    @Override
    public Result doWork() {
        String payload = getInputData().getString("payload");
        if (payload == null || payload.isEmpty()) {
            return Result.failure();
        }

        SharedPreferences prefs = getApplicationContext().getSharedPreferences("SMSPrefs", Context.MODE_PRIVATE);
        String webhookUrl = prefs.getString("webhook_url", "").trim();

        if (webhookUrl.isEmpty()) {
            return Result.failure();
        }

        try {
            URL url = new URL(webhookUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "text/plain; charset=utf-8");
            conn.setConnectTimeout(15000);
            conn.setReadTimeout(15000);
            conn.setDoOutput(true);

            try (OutputStream os = conn.getOutputStream()) {
                byte[] input = payload.getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }

            int responseCode = conn.getResponseCode();
            conn.disconnect();

            if (responseCode >= 200 && responseCode < 300) {
                return Result.success();
            } else {
                return Result.retry();
            }
        } catch (Exception e) {
            return Result.retry();
        }
    }
}
