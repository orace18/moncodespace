package com.sicmagroup.tondi;

import android.annotation.SuppressLint;
import android.app.job.JobParameters;
import android.app.job.JobScheduler;
import android.app.job.JobService;
import android.content.ContextWrapper;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import androidx.annotation.RequiresApi;

import com.pixplicity.easyprefs.library.Prefs;

import static com.sicmagroup.tondi.Connexion.TEL_KEY;

@SuppressLint("SpecifyJobSchedulerIdRange")
@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class TondiJobService extends JobService {
    @Override
    public boolean onStartJob(JobParameters params) {
        new Prefs.Builder()
                .setContext(this)
                .setMode(ContextWrapper.MODE_PRIVATE)
                .setPrefsName(getPackageName())
                .setUseDefaultSharedPreference(true)
                .build();
        if(Prefs.contains(TEL_KEY)){
            //service for calendar and notification
            Intent service = new Intent(getApplicationContext(), AutoVersement.class);
            getApplicationContext().startService(service);
//            Utilitaire.scheduleJob(getApplicationContext());
            Log.d("jobScheduler", "Job started");
            Utilitaire utilitaire = new Utilitaire(getApplicationContext());
            // si internet, appeler synchroniser_en_ligne
            if (utilitaire.isConnected()){
                try {
                    utilitaire.synchroniser_en_ligne();
                    Log.d("synchronisation", "in it!");
                }catch (Exception e) {
                    Log.e("Job error", e.getMessage());
                }
            }

        }

        return false;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        Log.d("jobScheduler", "Job ended");

        return false;
    }
}
