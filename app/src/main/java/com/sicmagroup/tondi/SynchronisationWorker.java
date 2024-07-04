package com.sicmagroup.tondi;

import android.content.Context;
import android.content.ContextWrapper;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.pixplicity.easyprefs.library.Prefs;


public class SynchronisationWorker extends Worker {

    private Context context;
    public SynchronisationWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
        this.context = context;
        new Prefs.Builder()
                .setContext(context)
                .setMode(ContextWrapper.MODE_PRIVATE)
                .setPrefsName(context.getPackageName())
                .setUseDefaultSharedPreference(true)
                .build();
    }

    @NonNull
    @Override
    public Result doWork() {

        try{
            Utilitaire utilitaire = new Utilitaire(this.context);
            utilitaire.synchroniser_en_ligne();

            return Result.success();
        }catch (Exception e){
            Log.e("synchWorker", e.getMessage());
            return Result.failure();
        }
    }
}
