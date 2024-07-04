package com.sicmagroup.tondi;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.text.format.DateFormat;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;


import com.android.volley.AuthFailureError;
import com.android.volley.NetworkError;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.ServerError;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.play.core.appupdate.AppUpdateInfo;
import com.google.android.play.core.appupdate.AppUpdateManager;
import com.google.android.play.core.appupdate.AppUpdateManagerFactory;
import com.google.android.play.core.install.model.AppUpdateType;
import com.google.android.play.core.install.model.UpdateAvailability;
import com.google.android.play.core.tasks.OnSuccessListener;
import com.orm.query.Condition;
import com.orm.query.Select;
import com.romellfudi.permission.PermissionService;
import com.sicmagroup.formmaster.model.FormElementTextPassword;
import com.sicmagroup.ussdlibra.USSDController;
import com.squareup.picasso.Transformation;

import com.pixplicity.easyprefs.library.Prefs;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.sicmagroup.formmaster.FormBuilder;
import com.sicmagroup.formmaster.model.BaseFormElement;

import org.json.JSONException;
import org.json.JSONObject;

import static com.sicmagroup.tondi.Connexion.ACCESS_BOOL;
import static com.sicmagroup.tondi.Connexion.ACCESS_RETURNf_KEY;
import static com.sicmagroup.tondi.Connexion.CONNECTER_KEY;
import static com.sicmagroup.tondi.Connexion.ID_UTILISATEUR_KEY;
import static com.sicmagroup.tondi.Connexion.NMBR_PWD_FAILED;
import static com.sicmagroup.tondi.Connexion.NMBR_PWD_TENTATIVE_FAILED;
import static com.sicmagroup.tondi.Connexion.NOM_KEY;
import static com.sicmagroup.tondi.Connexion.PASS_KEY;
import static com.sicmagroup.tondi.Connexion.PHOTO_KEY;
import static com.sicmagroup.tondi.Connexion.PRENOMS_KEY;
import static com.sicmagroup.tondi.Connexion.SPLASH_LOADING;
import static com.sicmagroup.tondi.utils.Constantes.SERVEUR;
import static com.sicmagroup.tondi.utils.Constantes.STATUT_UTILISATEUR;
import static com.sicmagroup.tondi.Connexion.TEL_KEY;
import static com.sicmagroup.tondi.Connexion.url_desactiver_account;
import static com.sicmagroup.tondi.utils.Constantes.accessToken;


public class Dashboard extends AppCompatActivity  {
    TextView solde;
    private HashMap<String, HashSet<String>> map;
    private USSDController ussdApi;
    Intent svc ;

    GridView services_grid;

    String[] services_string = {
            "Mes Tontines", "Mon Historique", "Mes Retraits", "Mes Avances", "Mes Plaintes", "Mon Compte"

    };
    /*int[] gridViewImageId = {
            R.drawable.ic_piggy_bank, R.drawable.ic_accounting, R.drawable.ic_coins, R.drawable.ic_gear,R.drawable.ic_chat,R.drawable.ic_house

    };*/
    int[] gridViewImageId = {
            R.drawable.ic_mes_tontines,
            R.drawable.ic_mes_versements,
            R.drawable.ic_mes_retraits_xml_1,
            R.drawable.ic_loan_xml_1,
            R.drawable.ic_message,
            R.drawable.ic_gears

    };


    private static final int TAG_PIN = 11;
    RecyclerView mRecyclerView;
    FormBuilder frm_pin_acces = null;
    Intent smsServiceIntent;
    Context mcontext;
    String medias_url =  SERVEUR + "/medias/";
    Utilitaire utilitaire;
    FloatingActionButton fab_deco;
    FloatingActionButton fab_info;
    FloatingActionButton fab3;
    Boolean isFABOpen = false;
    ImageButton solde_btn;

    private AppUpdateManager appUpdateManager;
    private static final int RC_APP_UPDATE = 100;

    @Override
    protected void onPause() {
        super.onPause();
        Log.d("onPause","back");
        //Prefs.putBoolean(ACCESS_BOOL,false);
        //stopService(svc);
        //Affichage du message d'alerte en cas de compte temporairement désactivé
        LinearLayout alertLayout = (LinearLayout) findViewById(R.id.layout_alert);

//        if (Prefs.contains(STATUT_UTILISATEUR)) {
//            Log.e("statut", Prefs.getString(STATUT_UTILISATEUR, null));
//            switch (Prefs.getString(STATUT_UTILISATEUR, null)) {
//                case "desactive temp":
//                    alertLayout.setVisibility(View.VISIBLE);
//                    TextView alert = (TextView) findViewById(R.id.alerte);
//                    alert.setText(R.string.alerte_desac_temp);
//                    break;
//                case "desactive":
//                    alertLayout.setVisibility(View.GONE);
//                    Prefs.putString(ID_UTILISATEUR_KEY, null);
//                    Dashboard.this.finish();
//                    startActivity(new Intent(Dashboard.this, Connexion.class));
//                    break;
//                case "active":
//                    alertLayout.setVisibility(View.GONE);
//                    break;
//            }
//        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d("onResume","fore");
        Prefs.putBoolean(ACCESS_BOOL,true);
        //Affichage du message d'alerte en cas de compte temporairement désactivé
        TextView alert = (TextView) findViewById(R.id.alerte);
        if (Prefs.contains(STATUT_UTILISATEUR)) {
            Log.d("dashboard:userstatut", Prefs.getString(STATUT_UTILISATEUR, null));
            switch (Prefs.getString(STATUT_UTILISATEUR, null)) {
                case "desactive temp":
                    alert.setVisibility(View.VISIBLE);
                    alert.setText(R.string.alerte_desac_temp);
                    break;
                case "desactive":
                    alert.setVisibility(View.GONE);
                    Prefs.putString(ID_UTILISATEUR_KEY, null);
                    startActivity(new Intent(Dashboard.this, Connexion.class));
                    Dashboard.this.finish();
                    break;
                case "active":
                    alert.setVisibility(View.GONE);
                    break;
            }
        }
        appUpdateManager.getAppUpdateInfo().addOnSuccessListener(new OnSuccessListener<AppUpdateInfo>() {
            @Override
            public void onSuccess(AppUpdateInfo result) {
                if (result.updateAvailability() == UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS){
                    try {
                        appUpdateManager.startUpdateFlowForResult(result, AppUpdateType.IMMEDIATE, Dashboard.this, RC_APP_UPDATE);
                    } catch (IntentSender.SendIntentException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d("onStop","back");
        Prefs.putBoolean(ACCESS_BOOL,false);
        //stopService(svc);


    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d("onDestroy","back");
        Prefs.putBoolean(ACCESS_BOOL,false);
        //stopService(svc);
//        Prefs.putString(ID_UTILISATEUR_KEY,null);
        unregisterReceiver(receiver);
    }

    private NetworkChangeReceiver receiver;
    @SuppressLint("ResourceAsColor")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashbord);
        //setContentView(R.layout.activity_tableau_bord);
        new Prefs.Builder()
                .setContext(this)
                .setMode(ContextWrapper.MODE_PRIVATE)
                .setPrefsName(getPackageName())
                .setUseDefaultSharedPreference(true)
                .build();
        utilitaire = new Utilitaire(Dashboard.this);
        // classe de retour après activation des accessibiltés
        Prefs.putString(ACCESS_RETURNf_KEY,"com.sicmagroup.tondi.Dashboard");
        svc = new Intent(Dashboard.this, SplashLoadingService.class);
        Prefs.putBoolean(ACCESS_BOOL,true);
        map = new HashMap<>();
        map.put("KEY_LOGIN", new HashSet<>(Arrays.asList("waiting", "loading")));
        map.put("KEY_ERROR", new HashSet<>(Arrays.asList("problem", "error")));
        /**
         * Upodate automaticaly and IMMEDIAT
         */

        appUpdateManager = AppUpdateManagerFactory.create(this);
        appUpdateManager.getAppUpdateInfo().addOnSuccessListener(new OnSuccessListener<AppUpdateInfo>() {
            @Override
            public void onSuccess(AppUpdateInfo result) {
                if (result.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE){
                    try {
                        appUpdateManager.startUpdateFlowForResult(result, AppUpdateType.IMMEDIATE, Dashboard.this, RC_APP_UPDATE);
                    } catch (IntentSender.SendIntentException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        //Si l'utilisateur n'a fait aucun tontine le diriger auto sur nouvelle tontine
        String id_utilisateur = Prefs.getString(ID_UTILISATEUR_KEY,null);
        if(id_utilisateur == null){
            Intent redirectToConnexion = new Intent(Dashboard.this, Connexion.class);
            startActivity(redirectToConnexion);
            Dashboard.this.finish();

        } else {
            List<Tontine> tontines;
            tontines = Select.from(Tontine.class)
                    .where(Condition.prop("id_utilisateur").eq(id_utilisateur))
                    .list();
//        Log.d("id_utilisateur", Prefs.getString(ID_UTILISATEUR_KEY, null));
//        Log.d("size", String.valueOf(tontines.size()));
            if(tontines.size() == 0)
            {
                Intent intent=new Intent(Dashboard.this,NouvelleTontine.class);
                intent.putExtra("first_versement", true);
                startActivity(intent);
                Dashboard.this.finish();
            }

        }


//        new PermissionService(this).request(
//                new String[]{/*Manifest.permission.CALL_PHONE,*/ /*Manifest.permission.READ_PHONE_STATE,*/ Manifest.permission.READ_SMS, Manifest.permission.RECEIVE_SMS},
//                callback);

        IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        receiver = new NetworkChangeReceiver();
        registerReceiver(receiver, filter);
        solde_btn = (ImageButton) findViewById(R.id.imageButton_solde);
        solde_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //Dialog pour le pin
                final Dialog dialog = new Dialog(Dashboard.this);
                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                dialog.setCancelable(true);
                dialog.setContentView(R.layout.pin_access_2);
                setupFrmPin(dialog,dialog.getContext());
                TextView msg_try_mdp = dialog.findViewById(R.id.msg_pin_trying);
                if(Prefs.contains(NMBR_PWD_TENTATIVE_FAILED))
                {
                    if(Prefs.getInt(NMBR_PWD_TENTATIVE_FAILED, 0) == 0)
                    {
                        msg_try_mdp.setVisibility(View.GONE);
                    }
                    else
                    {
                        int tentative_restant =  3 - Prefs.getInt(NMBR_PWD_TENTATIVE_FAILED, 0);
                        msg_try_mdp.setVisibility(View.VISIBLE);
                        msg_try_mdp.setText("Il vous reste "+String.valueOf(tentative_restant)+" tentative(s)");
                    }

                }
                Button dialogButton = (Button) dialog.findViewById(R.id.btn_continue);
                dialogButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                        BaseFormElement pin_ = frm_pin_acces.getFormElement(TAG_PIN);
                        String pin_value = pin_.getValue();
                        // verifier sim
                        //verifier_sim(Prefs.getString(TEL_KEY,""),pin_value,intent);

                        Connexion.AeSimpleSHA1 AeSimpleSHA1 = new Connexion.AeSimpleSHA1();
                        String pin_crypt;
                        try {
                            pin_value = AeSimpleSHA1.md5(pin_value);
                            pin_value = AeSimpleSHA1.SHA1(pin_value);

                        } catch (NoSuchAlgorithmException e) {
                            e.printStackTrace();
                        } catch (UnsupportedEncodingException e) {
                            e.printStackTrace();
                        }
                        if (pin_value.equals(Prefs.getString(PASS_KEY,"")))
                        {
                            if(Prefs.contains(NMBR_PWD_TENTATIVE_FAILED))
                            {
                                Prefs.putInt(NMBR_PWD_TENTATIVE_FAILED, 0);
                            }
                            //Show solde

                            Dialog dialog_solde = new Dialog(Dashboard.this);
                            dialog_solde.requestWindowFeature(Window.FEATURE_NO_TITLE);
                            dialog_solde.setCancelable(true);
                            dialog_solde.setContentView(R.layout.dialog_solde);

                            TextView solde_text = dialog_solde.findViewById(R.id.solde_value);
                            Utilisateur u = new Utilisateur().getUser(Prefs.getString(TEL_KEY, null));
                            solde_text.setText(String.valueOf(new DecimalFormat("##.##").format(u.getSolde()))+ " F FCFA");

                            TextView numero_compte = dialog_solde.findViewById(R.id.numero_compte_value);
                            numero_compte.setText(u.getNumero_compte());

                            Button fermer_btn = (Button) dialog_solde.findViewById(R.id.btn_oui);
                            fermer_btn.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    dialog_solde.dismiss();
                                }
                            });

                            dialog_solde.show();

                        }
                        else
                        {
                            if (Prefs.contains(NMBR_PWD_TENTATIVE_FAILED)) {
                                int tentative = Prefs.getInt(NMBR_PWD_TENTATIVE_FAILED, 0);
                                Prefs.putInt(NMBR_PWD_TENTATIVE_FAILED, tentative + 1);
                            } else {
                                Prefs.putInt(NMBR_PWD_TENTATIVE_FAILED, 1);
                            }

                            String msg = "Votre mot de passe est erroné! Veuillez réessayer SVP!";
                            Intent i = new Intent(Dashboard.this, Message_non.class);
                            i.putExtra("msg_desc", msg);
                            i.putExtra("class", "com.sicmagroup.tondi.Dashboard");
                            startActivity(i);
                            dialog.dismiss();
                        }
                    }
                });
                //Toast.makeText(getApplicationContext(),"e",Toast.LENGTH_LONG).show();
                dialog.show();

            }
        });


        //readSims();
        // mettre à jour le profil du tableau de bord
        String nom = Prefs.getString(NOM_KEY, "");
        String prenoms = Prefs.getString(PRENOMS_KEY, "");
        Long connecter_le = Long.valueOf(Prefs.getString(CONNECTER_KEY, ""));
        TextView user_name = (TextView) findViewById(R.id.user_name);
        user_name.setSelected(true);
        user_name.setText(nom + " " + prenoms);

        TextView user_last_connect = (TextView) findViewById(R.id.user_last_connect);
        Calendar cal = Calendar.getInstance(Locale.FRENCH);
        cal.setTimeInMillis(connecter_le);
        String date = DateFormat.format("dd/MM/yyyy à HH:mm", cal).toString();
        user_last_connect.setText("Connecté le " + date);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab_info = (FloatingActionButton) findViewById(R.id.fab_info);
        fab_deco = (FloatingActionButton) findViewById(R.id.fab_deco);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!isFABOpen){
                    showFABMenu();
                }else{
                    closeFABMenu();
                }
            }
        });

        //Toast.makeText(getApplicationContext(),Prefs.getString(PHOTO_KEY,null).toString(),Toast.LENGTH_LONG).show();
        ImageView user_avatar = findViewById(R.id.user_avatar);
        Picasso.get().load(medias_url+Prefs.getString(PHOTO_KEY,null)+".JPG").transform(new Dashboard.CircleTransform()).into(user_avatar);

        ContextWrapper cw = new ContextWrapper(this);
        File directory = cw.getDir("tontine_photos", Context.MODE_PRIVATE);
        String photo_identite = Prefs.getString(PHOTO_KEY, "");
        // Create imageDir
        File mypath = new File(directory, photo_identite + ".JPG");
        Picasso.get().load(mypath).transform(new CircleTransform()).into(user_avatar);



//        Picasso.get().load(medias_url+Prefs.getString(PHOTO_KEY,null)+".JPG").transform(new CircleTransform()).into(user_avatar);
//        utilitaire.loadImageFromStorage(photo_identite,getWindow().getDecorView());
//        Log.d("isFilePresent","isFilePresent:"+isFilePresent());
//        Toast.makeText(getApplicationContext(),"photo_identite:"+isFilePresent(),Toast.LENGTH_LONG).show();

//        final Button btn_deconnexion = findViewById(R.id.btn_deconnexion);
        fab_deco.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                alertView("Déconnexion", "Êtes vous sûr de vouloir vous déconnecter?");

            }
        });
//
//        Button btn_accueil = findViewById(R.id.btn_accueil);
//        btn_accueil.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//
//            }
//        });

//       btn_accueil.setTextColor(R.color.colorPrimary);

//        Button btn_about = findViewById(R.id.btn_about);
        fab_info.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                startActivity(new Intent(Dashboard.this, About_us.class));
                Dashboard.this.finish();
            }
        });

//        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_SMS}, 1);
//        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECEIVE_SMS}, 1);

        smsServiceIntent = new Intent(Dashboard.this, SmsReader.class);
        //this.startService(smsServiceIntent);

        //Toast.makeText(Dashboard.this,"eer: "+"edez",Toast.LENGTH_LONG).show();

        CustomGrid adapterViewAndroid = new CustomGrid(this, services_string, gridViewImageId);
        services_grid = findViewById(R.id.services_list);
        services_grid.setAdapter(adapterViewAndroid);
        services_grid.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int i, long id) {
                // clique sur mes tontines
                if (i == 0) {
                    ViewDialog alert = new ViewDialog();
                    alert.showDialog(Dashboard.this, new Intent(Dashboard.this, MesTontines.class));
                    //startActivity(new Intent(Dashboard.this, MesTontines.class));
                }
                // clique sur mes versements
                if (i == 1) {

                    ViewDialog alert = new ViewDialog();
                    alert.showDialog(Dashboard.this, new Intent(Dashboard.this, HistoryActivity.class));
                }
                // clique sur mes retraits
                if (i == 2) {
                    ViewDialog alert = new ViewDialog();
                    alert.showDialog(Dashboard.this, new Intent(Dashboard.this, MesRetraits.class));
                }

                //Clique sur avances sur tontine
                if(i == 3)
                {
                    alert_error("SERVICE INDISPONIBLE A DISTANCE", "Merci de vous rapprocher de COMUBA pour plus d'information.");
                }
                //clique sur mes plaintes
                if(i == 4)
                {
//                    alert_error("FONCTIONNALITE INDISPONIBLE", "Cette fonctionnalité sera disponible dans les prochaines version de l'application");
                    ViewDialog alert = new ViewDialog();
                    alert.showDialog(Dashboard.this, new Intent(Dashboard.this, MesPlaintes.class));
                }
                // clique sur compte & sécurité
                if (i == 5) {
                    //startActivity(new Intent(Dashboard.this, MonCompte.class));
                    //Toast.makeText(getApplicationContext(),"ee"+Prefs.getString(PIN_KEY,""),Toast.LENGTH_LONG).show();
                    ViewDialog alert = new ViewDialog();
                    alert.showDialog(Dashboard.this, new Intent(Dashboard.this, MonCompte.class));
                    // si pin définit
                    /*if (!Prefs.getString("pin","").equals("")){
                        // afficher écran de sécurité PIN requis
                        ViewDialog alert = new ViewDialog();
                        alert.showDialog(Dashboard.this,new Intent(Dashboard.this, MonCompte.class));

                    }*/
                }

                // clique sur operation
                /*if (i==5){
                    ViewDialog alert = new ViewDialog();
                    //alert.showDialog(Dashboard.this,new Intent(Dashboard.this, Compte_Marchand.class));
                }*/
            }
        });

        //Affichage du message d'alerte en cas de compte temporairement désactivé
        TextView alert = (TextView) findViewById(R.id.alerte);
        if (Prefs.contains(STATUT_UTILISATEUR)){
            Log.e("statut", Prefs.getString(STATUT_UTILISATEUR, null));
            switch (Prefs.getString(STATUT_UTILISATEUR, null)) {
                case "desactive temp":
                    alert.setVisibility(View.VISIBLE);

                    alert.setText(R.string.alerte_desac_temp);
                    break;
                case "desactive":
                    alert.setVisibility(View.GONE);
                    Prefs.putString(ID_UTILISATEUR_KEY, null);
                    startActivity(new Intent(Dashboard.this, Connexion.class));
                    Dashboard.this.finish();
                    break;
                case "active":
                    alert.setVisibility(View.GONE);
                    break;
            }
        }


//
        Log.e("id_utilisateur", Prefs.getString("id_utilisateur", ""));

        if(Prefs.contains(NMBR_PWD_TENTATIVE_FAILED))
        {

            int tentative = Prefs.getInt(NMBR_PWD_TENTATIVE_FAILED, 0);
            if(tentative >= 3)
            {
                fab_deco.setEnabled(false);
                fab_info.setEnabled(false);
                solde_btn.setEnabled(false);
                services_grid.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        Toast.makeText(Dashboard.this, "Patienter 24h avant de réessayer", Toast.LENGTH_SHORT).show();

                    }
                });

                ScheduledThreadPoolExecutor exec = new ScheduledThreadPoolExecutor(1);
                exec.schedule(new Runnable() {
                    @Override
                    public void run() {
                        Dashboard.this.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                fab_deco.setEnabled(true);
                                fab_info.setEnabled(true);
                                solde_btn.setEnabled(true);
                                services_grid.setOnItemClickListener(new AdapterView.OnItemClickListener()
                                {
                                    @Override
                                    public void onItemClick(AdapterView<?> parent, View view,int i, long id) {
                                        // clique sur mes tontines
                                        if (i == 0) {
                                            ViewDialog alert = new ViewDialog();
                                            alert.showDialog(Dashboard.this, new Intent(Dashboard.this, MesTontines.class));
                                            //startActivity(new Intent(Dashboard.this, MesTontines.class));
                                        }
                                        // clique sur mes versements
                                        if (i == 1) {

                                            ViewDialog alert = new ViewDialog();
                                            alert.showDialog(Dashboard.this, new Intent(Dashboard.this, MesVersements.class));
                                        }
                                        // clique sur mes retraits
                                        if (i == 2) {
                                            ViewDialog alert = new ViewDialog();
                                            alert.showDialog(Dashboard.this, new Intent(Dashboard.this, MesRetraits.class));
                                        }

                                        //Clique sur avances sur tontine
                                        if(i == 3)
                                        {
                                            alert_error("SERVICE INDISPONIBLE A DISTANCE", "Merci de vous rapprocher de COMUBA pour plus d'information.");
                                        }
                                        //clique sur mes plaintes
                                        if(i == 4)
                                        {
//                                            alert_error("FONCTIONNALITE INDISPONIBLE", "Cette fonctionnalité sera disponible dans les prochaines version de l'application");
                                            ViewDialog alert = new ViewDialog();
                                            alert.showDialog(Dashboard.this, new Intent(Dashboard.this, MesPlaintes.class));
                                        }
                                        // clique sur compte & sécurité
                                        if (i == 5) {
                                            //startActivity(new Intent(Dashboard.this, MonCompte.class));
                                            //Toast.makeText(getApplicationContext(),"ee"+Prefs.getString(PIN_KEY,""),Toast.LENGTH_LONG).show();
                                            ViewDialog alert = new ViewDialog();
                                            alert.showDialog(Dashboard.this, new Intent(Dashboard.this, MonCompte.class));
                                            // si pin définit
                        /*if (!Prefs.getString("pin","").equals("")){
                            // afficher écran de sécurité PIN requis
                            ViewDialog alert = new ViewDialog();
                            alert.showDialog(Dashboard.this,new Intent(Dashboard.this, MonCompte.class));

                        }*/
                                        }

                                        // clique sur operation
                    /*if (i==5){
                        ViewDialog alert = new ViewDialog();
                        //alert.showDialog(Dashboard.this,new Intent(Dashboard.this, Compte_Marchand.class));
                    }*/
                                    }
                                });
                                Prefs.putInt(NMBR_PWD_TENTATIVE_FAILED, 0);

                            }
                        });
                    }
                }, 24, TimeUnit.HOURS);

                if(Prefs.contains(NMBR_PWD_FAILED))
                {
                    Prefs.putInt(NMBR_PWD_FAILED, Prefs.getInt(NMBR_PWD_FAILED, 0) + 1);
                    if(Prefs.getInt(NMBR_PWD_FAILED, 0) >= 2)
                    {
                        //Désactiver le compte de l'utilisateur
                        desactiver_account();
                    }


                }
                else
                    Prefs.putInt(NMBR_PWD_FAILED, 1);


            }
        }


    }



    private void alert_error( String title ,String message ) {
        Dialog dialog = new Dialog(Dashboard.this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(true);
        dialog.setContentView(R.layout.dialog_service_indispo);

        TextView titre = (TextView) dialog.findViewById(R.id.deco_title);
        titre.setText(title);
        TextView message_deco = (TextView) dialog.findViewById(R.id.deco_message);
        message_deco.setText(message);

        Button oui = (Button) dialog.findViewById(R.id.btn_oui);
        oui.setText("Ok");
        oui.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.cancel();
            }
        });

        Button non = (Button) dialog.findViewById(R.id.btn_non);
        non.setVisibility(View.GONE);
//        non.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                dialog.cancel();
//            }
//        });

        dialog.show();
//        final AlertDialog.Builder dialog = new AlertDialog.Builder(this);
//        dialog.setTitle( title )
//                .setIcon(R.drawable.ic_warning)
//                .setMessage(message)
//                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
//                    public void onClick(DialogInterface dialoginterface, int i) {
//
//                    }
//                }).show();
    }

    private void showFABMenu(){
        isFABOpen=true;
        fab_info.animate().translationY(-getResources().getDimension(R.dimen.standard_55));
        fab_deco.animate().translationY(-getResources().getDimension(R.dimen.standard_105));
//        fab3.animate().translationY(-getResources().getDimension(R.dimen.standard_155));
    }

    private void closeFABMenu(){
        isFABOpen=false;
        fab_deco.animate().translationY(0);
        fab_info.animate().translationY(0);
//        fab3.animate().translationY(0);
    }

    private void readSims() {

        //  LogUtils.logW("Network");
        TelInfo telephonyInfo = TelInfo.getInstance(Dashboard.this);//getMActContext());
        //System.out.println("telche" + Arrays.asList(telephonyInfo.scitems).get(0));

        /*final int listView1 = R.id.listView;
        final ListView lv = (ListView) findViewById(listView1);

        //DualSIM_adapter ad = new DualSIM_adapter(this, R.layout.simci, new ArrayList<Sci>(Arrays.asList(telephonyInfo.scitems)));
        DualSIM_adapter ad = new DualSIM_adapter(this, telephonyInfo.scitemsArr);
        lv.setAdapter(ad);*/
    }

    public boolean isFilePresent() {
        //String path = Dashboard.this.getFilesDir().getAbsolutePath() + "/" + fileName;
        ContextWrapper cw = new ContextWrapper(this);
        File directory = cw.getDir("tontine_photos", Context.MODE_PRIVATE);
        String photo_identite = Prefs.getString(PHOTO_KEY, "");
        // Create imageDir
        File mypath = new File(directory, photo_identite + ".JPG");
        //File file = new File(path);
        return mypath.exists();
    }



    void setupFrmPin(Dialog d, Context c){
        mRecyclerView =  d.findViewById(R.id.form_pin_access);
        frm_pin_acces = new FormBuilder(c, mRecyclerView);
        FormElementTextPassword element6 = FormElementTextPassword.createInstance().setTag(TAG_PIN).setTitle("").setRequired(true);
        List<BaseFormElement> formItems = new ArrayList<>();
        formItems.add(element6);

        frm_pin_acces.addFormElements(formItems);
    }

    public class ViewDialog {
        void showDialog(Activity activity, final Intent intent){
            final Dialog dialog = new Dialog(activity);
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.setCancelable(true);
            dialog.setContentView(R.layout.pin_access_2);
            setupFrmPin(dialog,dialog.getContext());
            TextView msg_try_mdp = dialog.findViewById(R.id.msg_pin_trying);
            if(Prefs.contains(NMBR_PWD_TENTATIVE_FAILED))
            {
                if(Prefs.getInt(NMBR_PWD_TENTATIVE_FAILED, 0) == 0)
                {
                    msg_try_mdp.setVisibility(View.GONE);
                }
                else
                {
                    int tentative_restant =  3 - Prefs.getInt(NMBR_PWD_TENTATIVE_FAILED, 0);
                    msg_try_mdp.setVisibility(View.VISIBLE);
                    msg_try_mdp.setText("Il vous reste "+String.valueOf(tentative_restant)+" tentative(s)");
                }

            }
            Button dialogButton = (Button) dialog.findViewById(R.id.btn_continue);
            dialogButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    BaseFormElement pin = frm_pin_acces.getFormElement(TAG_PIN);
                    String pin_value = pin.getValue();
                    // verifier sim
                    //verifier_sim(Prefs.getString(TEL_KEY,""),pin_value,intent);

                    pin_verifier(intent,pin_value);
                    dialog.dismiss();

                }
            });
            //Toast.makeText(getApplicationContext(),"e",Toast.LENGTH_LONG).show();
            dialog.show();
        }
    }

    private void alertView( String title ,String message ) {
        Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(true);
        dialog.setContentView(R.layout.dialog_deconnexion);

        TextView titre = (TextView) dialog.findViewById(R.id.deco_title);
        titre.setText(title);
        TextView message_deco = (TextView) dialog.findViewById(R.id.deco_message);
        message_deco.setText(message);

        Button oui = (Button) dialog.findViewById(R.id.btn_oui);
        oui.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                Prefs.putString(ID_UTILISATEUR_KEY,null);
                startActivity(new Intent(Dashboard.this,Connexion.class));
                Dashboard.this.finish();
            }
        });

        Button non = (Button) dialog.findViewById(R.id.btn_non);
        non.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        dialog.show();



//        dialog.setTitle( title )
//                .setIcon(R.drawable.ic_warning)
//                .setMessage(message)
//                .setNegativeButton("Non", new DialogInterface.OnClickListener() {
//                    public void onClick(DialogInterface dialoginterface, int i) {
//                        dialoginterface.cancel();
//                    }})
//                .setPositiveButton("Oui", new DialogInterface.OnClickListener() {
//                    public void onClick(DialogInterface dialoginterface, int i) {
//                        Prefs.putString(ID_UTILISATEUR_KEY,null);
//                        Dashboard.this.finish();
//                        startActivity(new Intent(Dashboard.this,Connexion.class));
//                    }
//                }).show();
    }

    public static class CircleTransform implements Transformation {
        @Override
        public Bitmap transform(Bitmap source) {
            int size = Math.min(source.getWidth(), source.getHeight());

            int x = (source.getWidth() - size) / 2;
            int y = (source.getHeight() - size) / 2;

            Bitmap squaredBitmap = Bitmap.createBitmap(source, x, y, size, size);
            if (squaredBitmap != source) {
                source.recycle();
            }

            Bitmap bitmap = Bitmap.createBitmap(size, size, source.getConfig());

            Canvas canvas = new Canvas(bitmap);
            Paint paint = new Paint();
            BitmapShader shader = new BitmapShader(squaredBitmap,
                    BitmapShader.TileMode.CLAMP, BitmapShader.TileMode.CLAMP);
            paint.setShader(shader);
            paint.setAntiAlias(true);

            float r = size / 2f;
            canvas.drawCircle(r, r, r, paint);

            squaredBitmap.recycle();
            return bitmap;
        }

        @Override
        public String key() {
            return "circle";
        }
    }

    @Override
        public void onBackPressed() {
        // your code.
        //stopService(svc);

        Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(true);
        dialog.setContentView(R.layout.dialog_deconnexion);

        ImageView imageView = (ImageView) dialog.findViewById(R.id.imageView);
        imageView.setImageResource(R.drawable.outline_logout_white_48);

        TextView titre = (TextView) dialog.findViewById(R.id.deco_title);
        titre.setText("Quitter l'application");
        TextView message_deco = (TextView) dialog.findViewById(R.id.deco_message);
        message_deco.setText("Êtes vous sûr de vouloir fermer l'application ?");

        Button oui = (Button) dialog.findViewById(R.id.btn_oui);
        oui.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Prefs.putString(ID_UTILISATEUR_KEY,null);
                dialog.dismiss();
                Dashboard.this.finishAffinity();
//                Intent startMain = new Intent(Intent.ACTION_MAIN);
//                startMain.addCategory(Intent.CATEGORY_HOME);
//                startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                startActivity(startMain);
            }
        });

        Button non = (Button) dialog.findViewById(R.id.btn_non);
        non.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        dialog.show();


//        final AlertDialog.Builder builder = new AlertDialog.Builder(Dashboard.this);
//        builder.setTitle();
//        builder.setMessage();
//        builder.setPositiveButton("Oui", new DialogInterface.OnClickListener() {
//            @Override
//            public void onClick(DialogInterface dialogInterface, int i) {
//                /*Prefs.putString(ID_UTILISATEUR_KEY,null);
//                Dashboard.this.finish();
//                startActivity(new Intent(Dashboard.this,Connexion.class));*/
//                Intent startMain = new Intent(Intent.ACTION_MAIN);
//                startMain.addCategory(Intent.CATEGORY_HOME);
//                startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                startActivity(startMain);
//            }
//        });
//        builder.setNegativeButton("Non", new DialogInterface.OnClickListener() {
//            @Override
//            public void onClick(DialogInterface dialogInterface, int i){
//                dialogInterface.dismiss();
//
//            }
//        });
//
//        AlertDialog dialog = builder.create();
//        dialog.show();
    }

    private PermissionService.Callback callback = new PermissionService.Callback() {
        @Override
        public void onRefuse(ArrayList<String> RefusePermissions) {
            Toast.makeText(Dashboard.this,
                    getString(R.string.refuse_permissions),
                    Toast.LENGTH_SHORT).show();
            Dashboard.this.finish();
        }

        @Override
        public void onFinally() {
            // pass
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (!Settings.canDrawOverlays(Dashboard.this)) {
                    Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                            Uri.parse("package:" + Dashboard.this.getPackageName()));
                    startActivity(intent);
                }
            }
        }
    };

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        callback.handler(permissions, grantResults);
    }

    private void verifier_sim(final String numero, final String pin, final Intent intent){
        final String reseau = utilitaire.getOperatorByNumber(numero);
        String phoneNumber = null;
        if (reseau.equals("MTN")){
            phoneNumber = "*136*8#".trim();
        }else{
            //phoneNumber = ("*155*1*1*1*229"+MOOV_TEST+"*229"+MOOV_TEST+"*"+montant_value+"#").trim();
        }


        ussdApi = USSDController.getInstance(Dashboard.this);
        svc.putExtra("texte","La vérification de votre code PIN d'accès est en cours...");
        //startService(svc);
        //result.setText("");
        ussdApi.callUSSDInvoke(phoneNumber, map, new USSDController.CallbackInvoke() {
            @Override
            public void responseInvoke(String message) {

            }

            @SuppressLint("ResourceAsColor")
            @Override
            public void over(String message) {
                //stopService(svc);
                Log.d("APPUSSpp", message);

                Pattern pattern_msg_ok;
                Pattern pattern_msg_ok1 = null;
                Pattern pattern_msg_ok2 = null;
                Pattern pattern_msg_ok3 = null;
                Pattern pattern_msg_ok4 = null;
                if (reseau.equals("MTN")){
                    pattern_msg_ok = Pattern.compile("229"+numero);
                    pattern_msg_ok1 = Pattern.compile("ProblèmedeconnexionoucodeIHMnonvalide");
                    pattern_msg_ok2 = Pattern.compile("MMInonvalide");
                    pattern_msg_ok3 = Pattern.compile("Invalidservicecode");
                    pattern_msg_ok4 = Pattern.compile("229"+"\\d");
                }else{
                    pattern_msg_ok = Pattern.compile("229"+numero);
                    pattern_msg_ok4 = Pattern.compile("229"+"\\d");
                }




                Matcher matcher = pattern_msg_ok.matcher(message.replaceAll("\\s",""));
                assert pattern_msg_ok1 != null;
                Matcher matcher_connexion = pattern_msg_ok1.matcher(message.replaceAll("\\s",""));
                Matcher matcher_connexion_2 = pattern_msg_ok2.matcher(message.replaceAll("\\s",""));
                Matcher matcher_connexion_3 = pattern_msg_ok3.matcher(message.replaceAll("\\s",""));
                Matcher matcher_sim_non_4 = pattern_msg_ok4.matcher(message.replaceAll("\\s",""));

                // if our pattern matches the string, we can try to extract our groups
                if (matcher.find() ) {
                    // sim verifie
                    pin_verifier(intent,pin);
                }else{
                    // si probleme connexion
                    if (matcher_sim_non_4.find()){
                        String msg="La SIM "+numero+" n'a pas pu être vérifié! Veuillez insérer votre SIM ("+numero+") puis réessayer SVP!";
                        Intent i = new Intent(Dashboard.this, Message_non.class);
                        i.putExtra("msg_desc",msg);
                        i.putExtra("class","com.sicmagroup.tondi.Dashboard");
                        startActivity(i);
                    }
                    else if(matcher_connexion.find()){
                        // afficher Message_non
                        //Dashboard.this.finish();
                        String msg="Problème de connexion ou IHM non valide. Veuillez réessayer SVP!";
                        Intent i = new Intent(Dashboard.this, Message_non.class);
                        i.putExtra("msg_desc",msg);
                        i.putExtra("class","com.sicmagroup.tondi.Dashboard");
                        startActivity(i);
                    }else if(matcher_connexion_2.find()  || matcher_connexion_3.find()){
                        // afficher Message_non
                        //Dashboard.this.finish();
                        String msg="Problème de connexion ou MMI non valide. Veuillez réessayer SVP!";
                        Intent i = new Intent(Dashboard.this, Message_non.class);
                        i.putExtra("msg_desc",msg);
                        i.putExtra("class","com.sicmagroup.tondi.Dashboard");
                        startActivity(i);
                    }
                    else{
                        // si non verifiéee
                        // afficher Message_non
                        //Dashboard.this.finish();
                        String msg="La SIM "+numero+" n'a pas pu être vérifié! Veuillez insérer votre SIM ("+numero+") puis réessayer SVP!";
                        Intent i = new Intent(Dashboard.this, Message_non.class);
                        i.putExtra("msg_desc",msg);
                        i.putExtra("class","com.sicmagroup.tondi.Dashboard");
                        startActivity(i);
                    }

                }
            }
        });

    }

    private void pin_verifier(Intent intent, String pin) {
        Connexion.AeSimpleSHA1 AeSimpleSHA1 = new Connexion.AeSimpleSHA1();

        try {
            pin = AeSimpleSHA1.md5(pin);
            pin = AeSimpleSHA1.SHA1(pin);

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        if (pin.equals(Prefs.getString(PASS_KEY,""))){
            if(Prefs.contains(NMBR_PWD_TENTATIVE_FAILED))
            {
                Prefs.putInt(NMBR_PWD_TENTATIVE_FAILED, 0);
            }
            startActivity(intent);
        }else{
            if(Prefs.contains(NMBR_PWD_TENTATIVE_FAILED))
            {
                int tentative = Prefs.getInt(NMBR_PWD_TENTATIVE_FAILED, 0);
                Prefs.putInt(NMBR_PWD_TENTATIVE_FAILED, tentative + 1);
            }
            else
            {
                Prefs.putInt(NMBR_PWD_TENTATIVE_FAILED, 1);
            }

            String msg="Votre mot de passe est erroné! Veuillez réessayer SVP!";
            Intent i = new Intent(Dashboard.this, Message_non.class);
            i.putExtra("msg_desc",msg);
            i.putExtra("class","com.sicmagroup.tondi.Dashboard");
            startActivity(i);
                        /*Alerter.create(Dashboard.this)
                        .setTitle("Votre PIN d'Accès est erroné! Veuillez réessayer SVP! ")
                                .setIcon(R.drawable.ic_check)
                                //.setTitleAppearance(R.style.TextAppearance_AppCompat_Large)
                                //.setIconColorFilter(R.color.colorPrimaryDark)
                                //.setText("Vous pouvez maintenant vous connecter.")
                                .setBackgroundColorRes(R.color.colorPrimaryDark) // or setBackgroundColorInt(Color.CYAN)
                                .show();*/
        }
    }




    public class NetworkChangeReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(final Context context, final Intent intent) {

            //Log.v("internet_si_statut", "Receieved notification about network status");
            if (utilitaire.isConnected()){
                utilitaire.synchroniser_en_ligne();
            }

        }


    }

    @Override
    protected void onUserLeaveHint() {
        //Toast.makeText(Connexion.this,"Home buton pressed",Toast.LENGTH_LONG).show();

        final Intent svc = new Intent(Dashboard.this, SplashLoadingService.class);
        if (Prefs.getBoolean(SPLASH_LOADING,false)){
            //stopService(svc);
        }

        super.onUserLeaveHint();
    }

    //Fonction pour désactiver le compte de l'utilisateur
    public void desactiver_account()
    {
        RequestQueue queue = Volley.newRequestQueue(Dashboard.this);
        StringRequest postRequest = new StringRequest(Request.Method.POST, url_desactiver_account,
                new Response.Listener<String>()
                {
                    @SuppressLint("ResourceAsColor")
                    @Override
                    public void onResponse(String response) {
                        Log.e("ResponseTagMain", response);
                        // if (!response.equals("Erreur")) {
                        try {

                            JSONObject result = new JSONObject(response);
                            if(result.getBoolean("success"))
                            {
                                Prefs.putString(ID_UTILISATEUR_KEY,null);
                                Prefs.putInt(NMBR_PWD_FAILED, 0);
                                String msg="Vos identifiants sont incorrects. Votre compte a été désactivé, Merci de contacter le xxxxxxxx et suivez les instructions de réactivation de compte, Merci.";
                                Intent i = new Intent(Dashboard.this, Message_non.class);
                                i.putExtra("msg_desc",msg);
                                i.putExtra("class","com.sicmagroup.tondi.Connexion");
                                Dashboard.this.finish();
                                startActivity(i);

                            }
                            else
                            {
                                Prefs.putString(ID_UTILISATEUR_KEY,null);
                                Prefs.putInt(NMBR_PWD_FAILED, 0);
                                String msg="Vos identifiants sont incorrects. Veuillez réessayer SVP!";
                                Intent i = new Intent(Dashboard.this, Message_non.class);
                                i.putExtra("msg_desc",msg);
                                i.putExtra("class","com.sicmagroup.tondi.Connexion");
                                Dashboard.this.finish();
                                startActivity(i);
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener()
                {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {
                        Log.e("ResponseTagMain", String.valueOf(volleyError.getMessage()));
                        Log.e("Stack", "Error StackTrace: \t" + volleyError.getStackTrace());
                        String message;
                        if (volleyError instanceof NetworkError || volleyError instanceof AuthFailureError || volleyError instanceof TimeoutError) {
                            desactiver_account();

                        } else if (volleyError instanceof ServerError) {
                           desactiver_account();
                        }  else if (volleyError instanceof ParseError) {
                           desactiver_account();
                        }
                    }
                }
        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String>  params = new HashMap<String, String>();
                params.put("numero", Prefs.getString(TEL_KEY, null));
                return params;
            }
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                headers.put("Content-Type", "application/json");
                headers.put("Authorization", "Bearer " + accessToken); // Ajoute le token ici
                return headers;
            }
        };

        queue.add(postRequest);

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_APP_UPDATE && resultCode != RESULT_OK) {
            Toast.makeText(this, "Annuler", Toast.LENGTH_SHORT).show();
        }
    }
}
