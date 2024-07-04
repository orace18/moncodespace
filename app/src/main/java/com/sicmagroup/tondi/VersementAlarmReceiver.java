package com.sicmagroup.tondi;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class  VersementAlarmReceiver extends BroadcastReceiver {
    public static final int REQUEST_CODE = 12345;
    public static final String ACTION = "com.codepath.example.servicesdemo.alarm";

    // Triggered by the Alarm periodically (starts the service to run task)
    @Override
    public void onReceive(Context context, Intent intent) {

        Utilitaire utilitaire = new Utilitaire(context);
        // si internet, appeler synchroniser_en_ligne
       if (utilitaire.isConnected()){
            utilitaire.synchroniser_en_ligne();
            Log.e("statut_test", "dedans?");
        }

//        Intent i = new Intent(context, AutoVersement.class);
//        i.putExtra("foo", "bar");
//        context.startService(i);


    }
}
