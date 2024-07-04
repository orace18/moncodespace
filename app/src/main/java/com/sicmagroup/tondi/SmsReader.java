package com.sicmagroup.tondi;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import androidx.annotation.Nullable;

public class SmsReader extends Service {
//    CodeOtpVerification.OtpReceiver smsReceiver = new CodeOtpVerification.OtpReceiver();

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        //registerReceiver(smsReceiver, new IntentFilter("android.provider.Telephony.SMS_RECEIVED"));
        return START_STICKY;
    }


}