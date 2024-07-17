package com.sicmagroup.tondi;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import androidx.legacy.content.WakefulBroadcastReceiver;

public class BootBroadcastReceiver extends WakefulBroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        // Launch the specified service when this message is received
        Intent startServiceIntent = new Intent(context, AutoVersement.class);

        // Create a PendingIntent with FLAG_IMMUTABLE
        PendingIntent pendingIntent = PendingIntent.getService(
                context,
                0,
                startServiceIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        try {
            startWakefulService(context, startServiceIntent);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }
    }
}
