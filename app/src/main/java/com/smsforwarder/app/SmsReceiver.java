package com.smsforwarder.app;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.provider.Telephony;
import android.telephony.SmsMessage;
import androidx.work.Constraints;
import androidx.work.Data;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

public class SmsReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (Telephony.Sms.Intents.SMS_RECEIVED_ACTION.equals(intent.getAction())) {
            SmsMessage[] messages = Telephony.Sms.Intents.getMessagesFromIntent(intent);
            if (messages == null || messages.length == 0) return;

            String sender = "Unknown";
            StringBuilder fullBody = new StringBuilder();
            for (SmsMessage msg : messages) {
                if (msg.getOriginatingAddress() != null) {
                    sender = msg.getOriginatingAddress();
                }
                if (msg.getMessageBody() != null) {
                    fullBody.append(msg.getMessageBody());
                }
            }

            String payload = sender + "|" + fullBody.toString();

            Data inputData = new Data.Builder()
                    .putString("payload", payload)
                    .build();

            Constraints constraints = new Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build();

            OneTimeWorkRequest workRequest = new OneTimeWorkRequest.Builder(SendWorker.class)
                    .setInputData(inputData)
                    .setConstraints(constraints)
                    .build();

            WorkManager.getInstance(context).enqueue(workRequest);
        }
    }
}
