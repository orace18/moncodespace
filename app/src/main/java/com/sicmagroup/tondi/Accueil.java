package com.sicmagroup.tondi;

import static com.sicmagroup.tondi.Connexion.ID_UTILISATEUR_KEY;
import static com.sicmagroup.tondi.Connexion.PHOTO_CNI_KEY;
import static com.sicmagroup.tondi.Connexion.PHOTO_KEY;
import static com.sicmagroup.tondi.Connexion.TEL_KEY;
import static com.sicmagroup.tondi.utils.Constantes.STATUT_UTILISATEUR;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.Dialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.IntentSender;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.work.Constraints;
import androidx.work.NetworkType;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import com.google.android.material.snackbar.Snackbar;
import com.google.android.play.core.appupdate.AppUpdateInfo;
import com.google.android.play.core.appupdate.AppUpdateManager;
import com.google.android.play.core.appupdate.AppUpdateManagerFactory;
import com.google.android.play.core.install.InstallState;
import com.google.android.play.core.install.InstallStateUpdatedListener;
import com.google.android.play.core.install.model.AppUpdateType;
import com.google.android.play.core.install.model.InstallStatus;
import com.google.android.play.core.install.model.UpdateAvailability;
import com.google.android.play.core.tasks.OnSuccessListener;
import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.orm.query.Condition;
import com.orm.query.Select;
import com.pixplicity.easyprefs.library.Prefs;
import com.romellfudi.permission.PermissionService;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.TimeUnit;

//import io.fabric.sdk.android.Fabric;

public class Accueil extends AppCompatActivity {
    private static final int RC_APP_UPDATE = 100;
    private static final String TAG = "Accueil";
    static String FIRST_LAUNCH_KEY = "first_launch";
    static String COTIS_INFO__KEY = "cotis_auto";
    static String DUALSIM_INFO_KEY = "dualsim";
    static String CGU_FR_KEY = "cgu_fr";
    static String CGU_FON_KEY = "cgu_fon";
    static String CARTE_NAV_KEY = "carte_nav";
    static String PAYEMENT_IS_CHECKED_KEY = "payment_is_ckecked";
    static String MDP_ERROR_TRYING = "mdp_error_try";
    static String MOOV_DATA_SHARING = "moov_data_sharing";
    static int UPDATE_REQUEST_CODE = 101;
    TextView textView;
    Button btn_inscrire;
    Button btn_connecter;
    Boolean accept_sharing_data_withMoov;
    private HashMap<String, HashSet<String>> map;
    private WorkManager workManager;
    private Constraints constraints;
    private PeriodicWorkRequest periodicWorkRequestAutoVer, periodicWorkRequestSynch, periodicWorkRequestUpdateDb;
    private AppUpdateManager appUpdateManager;
    private PermissionService.Callback callback = new PermissionService.Callback() {
        @Override
        public void onRefuse(ArrayList<String> RefusePermissions) {
            Toast.makeText(Accueil.this,
                    getString(R.string.refuse_permissions),
                    Toast.LENGTH_SHORT).show();
            Accueil.this.finish();
        }

        @Override
        public void onFinally() {
            // pass
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (!Settings.canDrawOverlays(Accueil.this)) {
                    Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                            Uri.parse("package:" + Accueil.this.getPackageName()));
                    startActivity(intent);
                }
            }
        }
    };
    private InstallStateUpdatedListener installStateUpdatedListener = new InstallStateUpdatedListener() {
        @Override
        public void onStateUpdate(InstallState state) {
            if (state.installStatus() == InstallStatus.DOWNLOADED) {
                showCompletedUpdate();
            }
        }
    };


    // Setup a recurring alarm every half hour
//    public void scheduleAlarm() {
//        // Construct an intent that will execute the AlarmReceiver
//        Intent intent = new Intent(getApplicationContext(), VersementAlarmReceiver.class);
//        // Create a PendingIntent to be triggered when the alarm goes off
//        final PendingIntent pIntent = PendingIntent.getBroadcast(this, VersementAlarmReceiver.REQUEST_CODE,
//                intent, PendingIntent.FLAG_UPDATE_CURRENT);
//        // Setup periodic alarm every every half hour from this point onwards
//        long firstMillis = System.currentTimeMillis(); // alarm is set right away
//
//        // Set the alarm to start at approximately 22:00 p.m.
//
//
//        AlarmManager alarm = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);
//        // First parameter is the type: ELAPSED_REALTIME, ELAPSED_REALTIME_WAKEUP, RTC_WAKEUP
//        // Interval can be INTERVAL_FIFTEEN_MINUTES, INTERVAL_HALF_HOUR, INTERVAL_HOUR, INTERVAL_DAY
//        alarm.setInexactRepeating(AlarmManager.RTC_WAKEUP, firstMillis,
//                /*60000*/3000, pIntent);
//
////        ComponentName receiver = new ComponentName(this, VersementAlarmReceiver.class);
////        PackageManager pm = this.getPackageManager();
//
////        pm.setComponentEnabledSetting(receiver,
////                PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
////                PackageManager.DONT_KILL_APP);
//
//
//    }

    @Override
    protected void onResume() {
        super.onResume();
        appUpdateManager.getAppUpdateInfo().addOnSuccessListener(new OnSuccessListener<AppUpdateInfo>() {
            @Override
            public void onSuccess(AppUpdateInfo result) {
                if (result.updateAvailability() == UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS) {
                    try {
                        appUpdateManager.startUpdateFlowForResult(result, AppUpdateType.IMMEDIATE, Accueil.this, RC_APP_UPDATE);
                    } catch (IntentSender.SendIntentException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    @SuppressLint("RestrictedApi")
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        Fabric.with(this, new Crashlytics());
        FirebaseCrashlytics crashlytics = FirebaseCrashlytics.getInstance();
        crashlytics.sendUnsentReports();
        // Inialiser la class des users preferences
        new Prefs.Builder()
                .setContext(this)
                .setMode(ContextWrapper.MODE_PRIVATE)
                .setPrefsName(getPackageName())
                .setUseDefaultSharedPreference(true)
                .build();
        /**
         * Upodate automaticaly and IMMEDIAT
         */

        appUpdateManager = AppUpdateManagerFactory.create(this);
        appUpdateManager.getAppUpdateInfo().addOnSuccessListener(new OnSuccessListener<AppUpdateInfo>() {
            @Override
            public void onSuccess(AppUpdateInfo result) {
                if (result.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE) {
                    try {
                        appUpdateManager.startUpdateFlowForResult(result, AppUpdateType.IMMEDIATE, Accueil.this, RC_APP_UPDATE);
                    } catch (IntentSender.SendIntentException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        AppSignatureHelper appSignatureHelper = new AppSignatureHelper(this);
        Log.e(TAG, appSignatureHelper.getAppSignatures() + "");
        Toast.makeText(this, appSignatureHelper.getAppSignatures() + "", Toast.LENGTH_SHORT).show();

        //Si l'utilisateur n'a fait aucun tontine le diriger auto sur nouvelle tontine
//        Prefs.putString(ID_UTILISATEUR_KEY,null);
        Log.e(TAG, String.valueOf(Prefs.getBoolean(FIRST_LAUNCH_KEY, false)));
        Log.e(TAG, String.valueOf(Prefs.getString(TEL_KEY, "empty").isEmpty()));
        Log.e(TAG, String.valueOf(Prefs.getString(PHOTO_KEY, "empty").isEmpty()));
        Log.e(TAG, String.valueOf(Prefs.getString(PHOTO_CNI_KEY, "empty").isEmpty()));
        Log.e(TAG, String.valueOf(Prefs.getString(ID_UTILISATEUR_KEY, "empty")));


        if (Prefs.getBoolean(FIRST_LAUNCH_KEY, false)) {
            workManager = WorkManager.getInstance(this);
            workManager.cancelAllWork();

            constraints = new Constraints();
            constraints.setRequiredNetworkType(NetworkType.CONNECTED);

            periodicWorkRequestAutoVer = new PeriodicWorkRequest.Builder(AutoVersementWorker.class, 1, TimeUnit.HOURS)
                    .setConstraints(constraints)
                    .build();
            periodicWorkRequestSynch = new PeriodicWorkRequest.Builder(SynchronisationWorker.class, 15, TimeUnit.MINUTES)
                    .setConstraints(constraints)
                    .build();
            periodicWorkRequestUpdateDb = new PeriodicWorkRequest.Builder(UpdateDatabases.class, 15, TimeUnit.MINUTES)
                    .setConstraints(constraints)
                    .build();
            List<PeriodicWorkRequest> listPeriodic = new ArrayList<>();
            listPeriodic.add(periodicWorkRequestUpdateDb);
            workManager.enqueue(listPeriodic);
            //Inscription non achevée
            if (Prefs.getString(PHOTO_KEY, null) == null && Prefs.getString(PHOTO_CNI_KEY, null) == null && Prefs.contains(TEL_KEY)) // partie 1 uniquement effectuée
            {
                Utilisateur nouveau = new Utilisateur().getUser(Prefs.getString(TEL_KEY, ""));
                if (nouveau != null) {
                    Log.e("user", Prefs.getString(TEL_KEY, ""));
                    Log.e("user", nouveau.getId_utilisateur());
                    Intent inscription_next_1 = new Intent(Accueil.this, Inscription_next.class);
                    inscription_next_1.putExtra("id_utilisateur", nouveau.getId_utilisateur());
                    startActivity(inscription_next_1);
                }
            }
//            else if(!Prefs.contains(PHOTO_CNI_KEY) && Prefs.contains(PHOTO_KEY) && Prefs.contains(TEL_KEY)) // partie 1 et 2 uniquement effectuée
//            {
//                Utilisateur nouveau = new Utilisateur().getUser(Prefs.getString(TEL_KEY, ""));
//                if(nouveau != null)
//                {
//                    Intent inscription_next_2 = new Intent(Accueil.this, Inscription_next_2.class);
//                    inscription_next_2.putExtra("id_utilisateur", nouveau.getId_utilisateur());
//                    startActivity(inscription_next_2);
//                }
//            }
            Log.e(TAG, Prefs.getString(PHOTO_KEY, null) + " humm");
            if (Prefs.getString(ID_UTILISATEUR_KEY, null) != null && Prefs.getString(PHOTO_CNI_KEY, null) != null) {
                List<Tontine> tontines;
                tontines = Select.from(Tontine.class)
                        .where(Condition.prop("id_utilisateur").eq(Prefs.getString(ID_UTILISATEUR_KEY, null)))
                        .list();
                if (tontines.size() == 0) {
                    Accueil.this.finish();
                    Intent intent = new Intent(Accueil.this, NouvelleTontine.class);
                    intent.putExtra("first_versement", true);
                    startActivity(intent);
                } else {

                    if (Prefs.contains(STATUT_UTILISATEUR)) {
                        switch (Prefs.getString(STATUT_UTILISATEUR, null)) {
                            case "desactive":
                                Prefs.putString(ID_UTILISATEUR_KEY, null);
                                this.finish();
                                startActivity(new Intent(Accueil.this, Connexion.class));
                                break;

                        }
                    }
                    startActivity(new Intent(Accueil.this, Home.class));
                }
            } else {
//                Log.e(TAG+"2", String.valueOf(Prefs.getString(PHOTO_KEY,null).equals("null")));
//                Log.e(TAG+"2", String.valueOf(Prefs.getString(PHOTO_CNI_KEY,null) == null));
//                Log.e(TAG+"2", String.valueOf(Prefs.getString(TEL_KEY,"empty")));

                if (Prefs.getString(PHOTO_KEY, null) == null && Prefs.getString(PHOTO_CNI_KEY, null) == null && Prefs.contains(TEL_KEY)) // partie 1 uniquement effectuée
                {
                    Utilisateur nouveau = new Utilisateur().getUser(Prefs.getString(TEL_KEY, ""));
                    if (nouveau != null) {
                        Log.e("user", Prefs.getString(TEL_KEY, ""));
                        Log.e("user", nouveau.getId_utilisateur());
                        Intent inscription_next_1 = new Intent(Accueil.this, Inscription_next.class);
                        inscription_next_1.putExtra("id_utilisateur", nouveau.getId_utilisateur());
                        startActivity(inscription_next_1);
                    }
                } else if (Prefs.contains(PHOTO_KEY) && Prefs.contains(PHOTO_CNI_KEY) && Prefs.contains(TEL_KEY)) {
                    startActivity(new Intent(Accueil.this, Connexion.class));
                }

            }
        }


        setContentView(R.layout.activity_accueil);

//        scheduleAlarm();
        map = new HashMap<>();
        map.put("KEY_LOGIN", new HashSet<>(Arrays.asList("waiting", "loading")));
        map.put("KEY_ERROR", new HashSet<>(Arrays.asList("problem", "error")));
//        new PermissionService(this).request(
//                new String[]{/*Manifest.permission.CALL_PHONE,*/ /*Manifest.permission.READ_PHONE_STATE,*/ Manifest.permission.READ_SMS, Manifest.permission.RECEIVE_SMS},
//                callback);
        /*textView = findViewById(R.id.tv_appname);

        TextPaint paint = textView.getPaint();
        float width = paint.measureText(textView.getText().toString());

        Shader textShader = new LinearGradient(0, 90, width, textView.getTextSize(),
                new int[]{
                        Color.parseColor("#FC7100"),
                        Color.parseColor("#F93D00"),
                }, null, Shader.TileMode.CLAMP);*/

        //textView.getPaint().setShader(textShader);

//        accept_sharing_data_withMoov = false;
        // Mettre à jour la préférence first_launch
        Prefs.putBoolean(FIRST_LAUNCH_KEY, true);
        // Mettre préférences infos_cotis
        Prefs.putInt(COTIS_INFO__KEY, 0);
        Prefs.putInt(DUALSIM_INFO_KEY, 0);
        Prefs.putInt(CGU_FR_KEY, 0);
        Prefs.putInt(CGU_FON_KEY, 0);
        Prefs.putString(CARTE_NAV_KEY, "");
        Prefs.putBoolean(PAYEMENT_IS_CHECKED_KEY, false);
        Prefs.putBoolean(MOOV_DATA_SHARING, false);


        btn_inscrire = findViewById(R.id.btn_inscrire);
        btn_inscrire.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                if(accept_sharing_data_withMoov)
//                {
                finish();
                startActivity(new Intent(Accueil.this, Inscription.class));
//                }
//                else
//                {
//                    alertView("Demande d'autorisation", "Autoriser l’application à vérifier votre identité auprès de votre opérateur mobile ?");
//                    alertView("Demande d'autorisation", "Autoriser l’opérateur mobile à partager vos informations d’identité avec l’application ?");
//                    Toast.makeText(Accueil.this, "TONDi a besoin de cette autorisation pour vous facilitez votre inscription sur l'application TONDi", Toast.LENGTH_SHORT).show();
//                }

            }
        });

        btn_connecter = findViewById(R.id.btn_connecter);
        btn_connecter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                finish();
                startActivity(new Intent(Accueil.this, Connexion.class));
            }
        });


        TextView lien_cgu = (TextView) findViewById(R.id.lien_cgu);
        lien_cgu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                startActivity(new Intent(Accueil.this, CGU.class));
            }
        });
//        Utilitaire.scheduleJob(this);


    }

    private void alertView(String title, String message) {

        Dialog dialog = new Dialog(Accueil.this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(false);
        dialog.setContentView(R.layout.dialog_attention_encaissement_marchand);

        ImageView imageView = (ImageView) dialog.findViewById(R.id.imageView);
        imageView.setImageResource(R.drawable.ic_info);

        TextView titre = (TextView) dialog.findViewById(R.id.deco_title);
        titre.setText(title);
        TextView message_deco = (TextView) dialog.findViewById(R.id.deco_message);
        message_deco.setText(message);


        Button oui = (Button) dialog.findViewById(R.id.btn_oui);
        oui.setText("Oui");
        oui.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.cancel();
                accept_sharing_data_withMoov = true;
                Prefs.putBoolean(MOOV_DATA_SHARING, true);
            }
        });

        Button non = (Button) dialog.findViewById(R.id.btn_non);
        non.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(Accueil.this, "TONDi a besoin de cette autorisation pour vous facilitez votre inscription sur l'application TONDi", Toast.LENGTH_SHORT).show();
                accept_sharing_data_withMoov = false;
                Prefs.putBoolean(MOOV_DATA_SHARING, false);
                dialog.cancel();

            }
        });

        dialog.show();


//        AlertDialog.Builder dialog = new AlertDialog.Builder(Accueil.this);
//        dialog.setTitle( title )
//                .setIcon(R.drawable.ic_info_menu)
//                .setMessage(message)
//                .setCancelable(false)
//                .setNegativeButton("Non", new DialogInterface.OnClickListener() {
//                    public void onClick(DialogInterface dialoginterface, int i) {
//                        Toast.makeText(Accueil.this, "TONDi a besoin de cette autorisation pour vous facilitez votre inscription sur l'application TONDi", Toast.LENGTH_SHORT).show();
//                        accept_sharing_data_withMoov = false;
//                        Prefs.putBoolean(MOOV_DATA_SHARING, false);
//                        dialoginterface.cancel();
//                    }})
//                .setPositiveButton("Oui", new DialogInterface.OnClickListener() {
//                    public void onClick(DialogInterface dialoginterface, int i) {
//                        accept_sharing_data_withMoov = true;
//                        Prefs.putBoolean(MOOV_DATA_SHARING, true);
//                        dialoginterface.cancel();
//                    }
//                }).show();
    }

    public void cancelAlarm() {
        Intent intent = new Intent(getApplicationContext(), VersementAlarmReceiver.class);
        final PendingIntent pIntent = PendingIntent.getBroadcast(
                this,
                VersementAlarmReceiver.REQUEST_CODE,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
        AlarmManager alarm = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);
        alarm.cancel(pIntent);
    }


    protected void onStop() {
//         if(appUpdateManager != null)
//             appUpdateManager.unregisterListener(installStateUpdatedListener);
        super.onStop();
    }

    private void showCompletedUpdate() {
        Snackbar snackbar = Snackbar.make(findViewById(android.R.id.content), "Nouvelle version de l'application prête !!",
                Snackbar.LENGTH_INDEFINITE);
        snackbar.setAction("Installer", new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                appUpdateManager.completeUpdate();
            }
        });
        snackbar.show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        callback.handler(permissions, grantResults);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_APP_UPDATE && resultCode != RESULT_OK) {
            Toast.makeText(this, "Annuler", Toast.LENGTH_SHORT).show();
        }
    }

}
