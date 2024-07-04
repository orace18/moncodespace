package com.sicmagroup.tondi;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.android.volley.NoConnectionError;
import com.google.android.gms.auth.api.phone.SmsRetriever;
import com.google.android.gms.auth.api.phone.SmsRetrieverClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.text.format.DateFormat;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
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
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.gson.Gson;
import com.orm.SugarRecord;
import com.orm.query.Condition;
import com.orm.query.Select;
import com.pixplicity.easyprefs.library.Prefs;
import com.romellfudi.permission.PermissionService;
import com.sicmagroup.formmaster.FormBuilder;
import com.sicmagroup.formmaster.model.BaseFormElement;
import com.sicmagroup.formmaster.model.FormElementTextNumber;
import com.sicmagroup.formmaster.model.FormElementTextPassword;
import com.sicmagroup.tondi.Enum.OperationTypeEnum;
import com.sicmagroup.tondi.Enum.PeriodiciteEnum;
import com.sicmagroup.tondi.Enum.RetraitEnum;
import com.sicmagroup.tondi.Enum.TontineEnum;
import com.sicmagroup.tondi.utils.Constantes;
import com.sicmagroup.ussdlibra.USSDController;
import com.tapadoo.alerter.Alerter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.sicmagroup.tondi.CarteMain.url_init_retrait_espece;
import static com.sicmagroup.tondi.CarteMain.url_payer;
import static com.sicmagroup.tondi.CarteMain.url_retry_init_retrait_espece;
import static com.sicmagroup.tondi.Connexion.ACCESS_BOOL;
import static com.sicmagroup.tondi.Accueil.CARTE_NAV_KEY;
import static com.sicmagroup.tondi.Accueil.DUALSIM_INFO_KEY;
import static com.sicmagroup.tondi.Connexion.ACCESS_RETURNf_KEY;
import static com.sicmagroup.tondi.Connexion.FIREBASE_TOKEN;
import static com.sicmagroup.tondi.Connexion.ID_UTILISATEUR_KEY;
import static com.sicmagroup.tondi.Connexion.MOOV_TEST;
import static com.sicmagroup.tondi.Connexion.MTN_MECOTI;
import static com.sicmagroup.tondi.Connexion.MTN_TEST;
import static com.sicmagroup.tondi.Connexion.NMBR_PWD_FAILED;
import static com.sicmagroup.tondi.Connexion.NMBR_PWD_TENTATIVE_FAILED;
import static com.sicmagroup.tondi.Connexion.PASS_KEY;
import static com.sicmagroup.tondi.utils.Constantes.SERVEUR;
import static com.sicmagroup.tondi.utils.Constantes.STATUT_UTILISATEUR;
import static com.sicmagroup.tondi.Connexion.TEL_KEY;
import static com.sicmagroup.tondi.Connexion.url_desactiver_account;
import static com.sicmagroup.tondi.Connexion.url_get_code_otp;
import static com.sicmagroup.tondi.utils.Constantes.accessToken;

public class Carte extends AppCompatActivity {
    int onStartCount = 0;
    int nb_vers_defaut = 0;
    GridView versements_grid;
    private RadioGroup radioGroup;
    private RadioButton radioButton;
    ProgressDialog progressDialog;
    List<Versement> versement_list =new ArrayList<Versement>();
    String url_afficher_prev = SERVEUR+"/api/v1/tontines/carte_precedente";
    String url_afficher_next = SERVEUR+"/api/v1/tontines/carte_suivante";


    String url_derniere_tontine = SERVEUR+"/api/v1/tontines/derniere_tontine";
    String url_premiere_tontine = SERVEUR+"/api/v1/tontines/premiere_tontine";
    String url_verser = SERVEUR+"/api/v1/versements/verser";
    String url_retrait_mmo = SERVEUR+"/api/v1/versements/retrait_mmo";
    String url_verifier_statut = SERVEUR+"/api/v1/versements/verifier_transaction";

    //url api pour obtenir id de retrait
    String url_get_id_retrait_by_id_tontine = SERVEUR+"/api/v1/retraits/getIdRetraitByTontine";

    String url_terminer_tontine = SERVEUR+"/api/v1/tontines/terminer";


    int id_tontine;
    int mise;
    String nav = null;
    int ussd_level=0;
    Button btn_terminer;
    FormBuilder frm_pin_acces = null;
    private static final int TAG_PIN = 11;
    Long heure_transaction_global;

    Button btn_nouveau_versement;
    Button btn_code_retrait;
    Button btn_encaisser;

    ImageView lockedIcon;
    LinearLayout linearLayoutDeblocageDetail;
    TextView dateDeblocageTextView;



    private static final int TAG_MONTANT = 11;
    RecyclerView mRecyclerView;
    FormBuilder frm_versement = null;
    private HashMap<String, HashSet<String>> map;
    private USSDController ussdApi;
    Tontine tontine_main;
    private String TAG = "carte";

    @Override
    protected void onPause() {
        super.onPause();
        Log.d("onPause","back");
        //Prefs.putBoolean(ACCESS_BOOL,false);
        //Prefs.putString(ACCESS_RETURN_KEY,"");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d("onResume","fore");
        Prefs.putBoolean(ACCESS_BOOL,true);
        Prefs.putString(ACCESS_RETURNf_KEY,"com.sicmagroup.tondi.Carte");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d("onStop","back");
        Prefs.putBoolean(ACCESS_BOOL,false);
        Prefs.putString(ACCESS_RETURNf_KEY,"");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d("onDestroy","back");
        Prefs.putBoolean(ACCESS_BOOL,false);
        Prefs.putString(ACCESS_RETURNf_KEY,"");
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent getIntent = getIntent();
        id_tontine = getIntent.getIntExtra("id_tontine",0);
        tontine_main = SugarRecord.findById(Tontine.class, (long) id_tontine);
        nav = getIntent.getStringExtra("nav");
        onStartCount = 1;

        //Toast.makeText(getApplicationContext(),"jernfbj://"+id_tontine,Toast.LENGTH_LONG).show();
        if (savedInstanceState == null) // 1st time
        {
            if (nav.equals("prev")){
                this.overridePendingTransition(R.anim.anim_slide_in_right,
                        R.anim.anim_slide_out_right);
            }else{
                this.overridePendingTransition(R.anim.anim_slide_in_left,
                        R.anim.anim_slide_out_left);
            }

        } else // already created so reverse animation
        {
            onStartCount = 2;
        }


        //Toast.makeText(getApplicationContext(),+
        // "id_tontine_intent"+":"+id_tontine,Toast.LENGTH_LONG).show();
        // remove title
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

//        setContentView(R.layout.activity_carte);
        setContentView(R.layout.activity_carte_3);

        new Prefs.Builder()
                .setContext(this)
                .setMode(ContextWrapper.MODE_PRIVATE)
                .setPrefsName(getPackageName())
                .setUseDefaultSharedPreference(true)
                .build();

        // classe de retour après activation des accessibiltés
        Prefs.putString(ACCESS_RETURNf_KEY,"com.sicmagroup.tondi.Carte");
        Prefs.putBoolean(ACCESS_BOOL,true);
        map = new HashMap<>();
        map.put("KEY_LOGIN", new HashSet<>(Arrays.asList("espere", "waiting", "loading", "esperando")));
        map.put("KEY_ERROR", new HashSet<>(Arrays.asList("problema", "problem", "error", "null")));
//        new PermissionService(this).request(
//                new String[]{/*Manifest.permission.CALL_PHONE, Manifest.permission.READ_PHONE_STATE,*/ Manifest.permission.READ_SMS, Manifest.permission.RECEIVE_SMS},
//                callback);

        //ViewFlow viewFlow = findViewById(R.id.viewflow);
        //DiffAdapter myAdapter = new DiffAdapter(this);
        //viewFlow.setAdapter(myAdapter);


        //SmartTabLayout viewPagerTab = (SmartTabLayout) findViewById(R.id.viewpagertab);
        //viewPagerTab.setViewPager(viewPager);
        btn_terminer = findViewById(R.id.btn_terminer);
        btn_nouveau_versement = findViewById(R.id.btn_nouveau_versement);
        lockedIcon = findViewById(R.id.isBlocked);
        linearLayoutDeblocageDetail = findViewById(R.id.layout_deblocage_details);
        dateDeblocageTextView = findViewById(R.id.deblocage);

        afficherCarte();
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//            requestPermissions(new String[]{CALL_PHONE}, 1);
//        }

        Button back_to = findViewById(R.id.back_to);
        back_to.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                Intent getIntent = getIntent();
                final boolean isNewTontine = getIntent.getBooleanExtra("isNewTontine",false);
                Log.e("isNewTontine3",""+isNewTontine);
                if (isNewTontine){
                    Intent i = new Intent(Carte.this,Home.class);
                    i.putExtra(Constantes.BOTTOM_NAV_DESTINATION, Constantes.DESTINATION_TONTINES);
                    startActivity(i);
                }else{
                    Carte.this.finish();
                }
//                Intent i = new Intent(Carte.this, Home.class);
//                i.putExtra(Constantes.BOTTOM_NAV_DESTINATION, Constantes.DESTINATION_TONTINES);
//                i.putExtra("tab_name",tontine_main.getStatut());
//                startActivity(i);
            }
        });


        btn_encaisser = findViewById(R.id.btn_encaisser);
        //btn_encaisser.setVisibility(View.INVISIBLE);
        //Button btn_terminer = findViewById(R.id.btn_terminer);
        if (tontine_main.getPeriode().equals(PeriodiciteEnum.JOURNALIERE.toString())){
            btn_terminer.setText("Arrêter mois en cours");
        }
        btn_terminer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                if(tontine_main.getMontCumuleNow(id_tontine,tontine_main.getStatut()) >= 1000)
//                {
                    // TODO Auto-generated method stub
                    alertView("Terminer la tontine","Etes-vous sûr de vouloir arrêter cette tontine ?",id_tontine);
//                }
//                else
//                {
//                    Toast.makeText(Carte.this, "Vous devez avoir au minimum 1000 F de cotisation avant d'effectuer un retrait", Toast.LENGTH_LONG).show();
//                }

                FirebaseMessaging.getInstance().getToken().addOnCompleteListener(new OnCompleteListener<String>() {
                    @Override
                    public void onComplete(@NonNull Task<String> task) {
                        if(!task.isSuccessful())
                        {
                            Log.e("testFirebase", "Fetching FCM registration token failed", task.getException());
                            return;
                        }

                        // Get new FCM registration token
                        String token = task.getResult();

                        //Save du token dans la bdd local
                        Utilisateur user = new Utilisateur();
                        user = new Utilisateur().getUser(Prefs.getString(TEL_KEY, ""));
                        Prefs.putString(FIREBASE_TOKEN, token);
                        user.setFirebaseToken(token);
                        user.save();
//                        SendFirebaseToken(token, user.getNumero());
                        // Log and toast
                        @SuppressLint({"StringFormatInvalid", "LocalSuppress"}) String msg = getString(R.string.msg_token_fmt, token);
                        Log.e("testFirebase", msg);
                        //Toast.makeText(Dashboard.this, msg, Toast.LENGTH_SHORT).show();


                    }
                });
            }
        });
        btn_encaisser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseMessaging.getInstance().getToken().addOnCompleteListener(new OnCompleteListener<String>() {
                    @Override
                    public void onComplete(@NonNull Task<String> task) {
                        if(!task.isSuccessful())
                        {
                            Log.e("testFirebase", "Fetching FCM registration token failed", task.getException());
                            return;
                        }

                        // Get new FCM registration token
                        String token = task.getResult();

                        //Save du token dans la bdd local
                        Utilisateur user = new Utilisateur();
                        user = new Utilisateur().getUser(Prefs.getString(TEL_KEY, ""));
                        Prefs.putString(FIREBASE_TOKEN, token);
                        user.setFirebaseToken(token);
                        user.save();
//                        SendFirebaseToken(token, user.getNumero());
                        // Log and toast
                        @SuppressLint({"StringFormatInvalid", "LocalSuppress"}) String msg = getString(R.string.msg_token_fmt, token);
                        Log.e("testFirebase", msg);
                        //Toast.makeText(Dashboard.this, msg, Toast.LENGTH_SHORT).show();


                    }
                });
                // TODO Auto-generated method stub
                Carte.ViewDialogPin alertPin = new Carte.ViewDialogPin();
                alertPin.showDialog(Carte.this);

            }
        });
        btn_nouveau_versement.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                heure_transaction_global = System.currentTimeMillis() / 1000L;
                Carte.ViewDialog alert = new Carte.ViewDialog();
                alert.showDialog(Carte.this);
            }
        });

        btn_code_retrait = findViewById(R.id.btn_voir_code);
        btn_code_retrait.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

//                FirebaseMessaging.getInstance().getToken().addOnCompleteListener(new OnCompleteListener<String>() {
//                    @Override
//                    public void onComplete(@NonNull Task<String> task) {
//                        if(!task.isSuccessful())
//                        {
//                            Log.e("testFirebase", "Fetching FCM registration token failed", task.getException());
//                            return;
//                        }
//
//                        // Get new FCM registration token
//                        String token = task.getResult();
//
//                        //Save du token dans la bdd local
//                        Utilisateur user = new Utilisateur();
//                        user = new Utilisateur().getUser(Prefs.getString(TEL_KEY, ""));
//                        Prefs.putString(FIREBASE_TOKEN, token);
//                        user.setFirebaseToken(token);
//                        user.save();
////                        SendFirebaseToken(token, user.getNumero());
//                        // Log and toast
//                        @SuppressLint({"StringFormatInvalid", "LocalSuppress"}) String msg = getString(R.string.msg_token_fmt, token);
//                        Log.e("testFirebase", msg);
//                        //Toast.makeText(Dashboard.this, msg, Toast.LENGTH_SHORT).show();
//                    }
//                });
                ConstraintLayout mainLayout =  findViewById(R.id.layout_cartemain);
                String id_tontines_concernes = null;
                List<Tontine> tontines_concernes = Tontine.find(Tontine.class, "carnet = ? AND periode = ? AND mise = ? AND statut = ?", new String[]{tontine_main.getCarnet(), tontine_main.getPeriode(), String.valueOf(tontine_main.getMise()), TontineEnum.WAITING.toString()}, null, "id desc", null);
                if(tontines_concernes.size() > 0)
                {
                    //Log.e("nmbre_id_concerne", String.valueOf(tontines_concernes.toString()));
                    for (Tontine tontine:tontines_concernes)
                    {
                        if (id_tontines_concernes == null)
                            id_tontines_concernes = String.valueOf(tontine.getId());
                        else
                            id_tontines_concernes =id_tontines_concernes + " , "+ String.valueOf(tontine.getId())  ;
                    }
                }
                List<Retrait> retraitList = SugarRecord.find(Retrait.class, "tontine IN ("+id_tontines_concernes+") AND statut = ?", new String[]{RetraitEnum.IN_PROGRESS.toString()}, null, "creation desc", null);
                if (retraitList.isEmpty())
                {
                    if(tontine_main.getStatut().equals(TontineEnum.WAITING.toString()))
                    {
                        Snackbar snackbar = Snackbar
                                .make(mainLayout, "Erreur, code de retrait introuvable. Contacter votre IMF.", Snackbar.LENGTH_INDEFINITE)
                                .setAction("Nouveau", new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        requete_retry_retrait_2(id_tontine);
                                    }
                                });
                        snackbar.show();
                    }
                }
                else
                {
                    if(tontine_main.getStatut().equals(TontineEnum.WAITING.toString())) {
                        //parcourir la liste pour voir si il y a des codes existants expiré.
                        //Si le dernier code générer est expiré afficher le snackbar du block if
                        //Sinon afficher le code.
                        Retrait r = retraitList.get(0);

                        // trouver le temps restant
                        Long diff = Calendar.getInstance().getTime().getTime() - r.getCreation();
                        int remaining_seconds = (int) (86400000 - diff);//((-Calendar.getInstance().getTime().getTime())/1000);//GetDifference();//86400000;
                        if (remaining_seconds <= 0) {
                            r.setStatut("expire");
                            r.save();
                            Snackbar snackbar = Snackbar
                                    .make(mainLayout, "Erreur, code de retrait expiré.", Snackbar.LENGTH_INDEFINITE)
                                    .setAction("Nouveau", new View.OnClickListener() {
                                        @Override
                                        public void onClick(View view) {
                                            requete_retry_retrait_2(id_tontine);
                                        }
                                    });
                            snackbar.show();
                        } else {
                            Intent j = new Intent(Carte.this, Encaisser.class);
                            //  int g = Math.toIntExact(r.getId());
                            long g =  r.getId();
                            j.putExtra("id_retrait", g);
                            Log.e("test", String.valueOf(r.getId()));
                            Carte.this.finish();
                            // maj des dates
                            startActivity(j);
                        }
                    }
                }

            }
        });

        if (tontine_main.getStatut().equals(TontineEnum.IN_PROGRESS.toString()) ){
            btn_encaisser.setVisibility(View.GONE);
            btn_code_retrait.setVisibility(View.GONE);
        }
        else if(tontine_main.getStatut().equals(TontineEnum.COLLECTED.toString()) ){
            btn_encaisser.setVisibility(View.GONE);
            btn_nouveau_versement.setVisibility(View.GONE);
            btn_code_retrait.setVisibility(View.GONE);
        }
        else if (tontine_main.getStatut().equals(TontineEnum.COMPLETED.toString())){
            btn_terminer.setVisibility(View.GONE);
            btn_nouveau_versement.setVisibility(View.GONE);
            btn_code_retrait.setVisibility(View.GONE);
            Log.e(TAG, tontine_main.getDateDeblocage()+" deb");
            if(tontine_main.getDateDeblocage() != null){
                SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
                try {

                    Date date_deblocage = format.parse(tontine_main.getDateDeblocage());
                    Date now = Calendar.getInstance().getTime();
                    Log.e(TAG, date_deblocage.before(now)+"");
                    if(date_deblocage.before(now)){
                        btn_encaisser.setVisibility(View.VISIBLE);
                    } else {
                        btn_encaisser.setVisibility(View.GONE);
                    }
                } catch(ParseException e){
                    e.printStackTrace();
                }
            }
        }
        //hide du boutton encaisser quand la tontine a le statut "en attente"
        else if (tontine_main.getStatut().equals(TontineEnum.WAITING.toString())){
            btn_terminer.setVisibility(View.GONE);
            btn_nouveau_versement.setVisibility(View.GONE);
            btn_encaisser.setVisibility(View.GONE);
            btn_code_retrait.setVisibility(View.VISIBLE);
        }

        if(tontine_main.getDateDeblocage() != null){
            lockedIcon.setVisibility(View.VISIBLE);
            linearLayoutDeblocageDetail.setVisibility(View.VISIBLE);
            dateDeblocageTextView.setText(tontine_main.getDateDeblocage());
            btn_terminer.setVisibility(View.GONE);
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
            try {
                Date date_deblocage = format.parse(tontine_main.getDateDeblocage());
                Date now = Calendar.getInstance().getTime();
                Log.e(TAG, date_deblocage.before(now)+"");
                if(date_deblocage.before(now)){
                    lockedIcon.setImageResource(R.drawable.lock_open_48px);
                } else {
                    lockedIcon.setImageResource(R.drawable.lock_48px);
                }
            } catch(ParseException e){
                e.printStackTrace();
            }
        } else {
            lockedIcon.setVisibility(View.GONE);
            linearLayoutDeblocageDetail.setVisibility(View.GONE);
        }

        Log.e("statut", tontine_main.getStatut());

        Button btn_next = findViewById(R.id.btn_next);
        btn_next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                suiv_tontine(id_tontine);
            }
        });

        Button btn_prev = findViewById(R.id.btn_prev);
        btn_prev.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                prec_tontine(id_tontine);
            }
        });

        versements_grid.setOnTouchListener(new OnSwipeTouchListener(Carte.this) {

            @Override
            public void onClick() {
                super.onClick();
                // your on click here
                //Toast.makeText(getApplicationContext(),"hjh",Toast.LENGTH_LONG).show();
                RelativeLayout nav = findViewById(R.id.btn_navs);
                if (nav.getVisibility() == View.VISIBLE){
                    nav.setVisibility(View.INVISIBLE);
                }
                else {
                    nav.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onSwipeLeft() {
                super.onSwipeLeft();
                suiv_tontine(id_tontine);
            }

            @Override
            public void onSwipeRight() {
                super.onSwipeRight();
                prec_tontine(id_tontine);
            }
        });


        Animation aniSlide = AnimationUtils.loadAnimation(getApplicationContext(),R.anim.anim_slide_in_left);
        this.getWindow().getDecorView().startAnimation(aniSlide);
        this.getWindow().getDecorView().setOnTouchListener(new OnSwipeTouchListener(Carte.this) {

            @Override
            public void onClick() {
                super.onClick();
                RelativeLayout nav = findViewById(R.id.btn_navs);
                if (nav.getVisibility() == View.VISIBLE){
                    nav.setVisibility(View.INVISIBLE);
                }
                else {
                    nav.setVisibility(View.VISIBLE);
                }
            }


            @Override
            public void onSwipeLeft() {
                super.onSwipeLeft();
                suiv_tontine(id_tontine);
            }

            @Override
            public void onSwipeRight() {
                super.onSwipeRight();
                prec_tontine(id_tontine);
            }
        });

        //Si l'utilisateur est désactivé de façon temporaire
        if (Prefs.contains(STATUT_UTILISATEUR))
        {
            switch (Prefs.getString(STATUT_UTILISATEUR, null)) {
                case "desactive temp":
                    btn_terminer.setEnabled(false);
                    //btn_nouveau_versement.setEnabled(false);
                    btn_encaisser.setEnabled(false);
                    btn_code_retrait.setEnabled(false);
                    break;
                case "desactive":
                    btn_terminer.setEnabled(false);
                    //btn_nouveau_versement.setEnabled(false);
                    btn_encaisser.setEnabled(false);
                    btn_code_retrait.setEnabled(false);
                    Toast.makeText(this, "Compte désactivé", Toast.LENGTH_SHORT).show();
                    Prefs.putString(ID_UTILISATEUR_KEY, null);
                    startActivity(new Intent(Carte.this, Connexion.class));
                    this.finish();
                case "active":
                    btn_terminer.setEnabled(true);
                    btn_nouveau_versement.setEnabled(true);
                    btn_encaisser.setEnabled(true);
                    btn_code_retrait.setEnabled(true);
                    break;
            }
        }

        if(Prefs.contains(NMBR_PWD_TENTATIVE_FAILED)) {
            int tentative = Prefs.getInt(NMBR_PWD_TENTATIVE_FAILED, 0);
            if (tentative >= 3) {
                btn_terminer.setEnabled(false);
                btn_code_retrait.setEnabled(false);
                btn_encaisser.setEnabled(false);
                btn_nouveau_versement.setEnabled(false);
                ScheduledThreadPoolExecutor exec = new ScheduledThreadPoolExecutor(1);
                exec.schedule(new Runnable() {

                    @Override
                    public void run() {

                        Carte.this.runOnUiThread(new Runnable() {

                            @Override
                            public void run() {
                                btn_terminer.setEnabled(true);
                                btn_code_retrait.setEnabled(true);
                                btn_encaisser.setEnabled(true);
                                btn_nouveau_versement.setEnabled(true);
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
//        Utilitaire utilitaire = new Utilitaire(this);
//        //swip response
//
        SwipeRefreshLayout swipeRefreshLayout = findViewById(R.id.swiperefresh);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                Utilitaire utilitaire = new Utilitaire(Carte.this);
                utilitaire.refreshDatabse(swipeRefreshLayout);
            }

        });

    }
    private void requete_retrait_2(final int id_tontine)
    {
        final Tontine tontine = SugarRecord.findById(Tontine.class, (long)id_tontine);
        RequestQueue queue = Volley.newRequestQueue(Carte.this);
        StringRequest postRequest = new StringRequest(Request.Method.POST, url_init_retrait_espece,
                new Response.Listener<String>()
                {
                    @SuppressLint("ResourceAsColor")
                    @Override
                    public void onResponse(String response) {
                        //progressDialog.dismiss();
                        // response
                        Log.e("ResponseTagP", response);
                        try {

                            JSONObject result = new JSONObject(response);
                            //final JSONArray array = result.getJSONArray("data");
                            Log.e("test", String.valueOf(result.getBoolean("success")));
                            Long id_retrait = Long.valueOf(0);
                            if (result.getBoolean("success") && result.has("resultat")) {
                                // maj des dates
                                Date currentTime = Calendar.getInstance().getTime();
                                long output_creation=currentTime.getTime()/1000L;
                                String str_creation=Long.toString(output_creation);
                                long timestamp_creation = Long.parseLong(str_creation) * 1000;
                                long output_maj=currentTime.getTime()/1000L;
                                String str_maj=Long.toString(output_maj);
                                long timestamp_maj = Long.parseLong(str_maj) * 1000;
                                progressDialog.dismiss();
                                JSONArray resultat = result.getJSONArray("resultat");
                                String[] actionGroup = {};
                                String action = "";
                                String object = "";
                                String token = "";
                                for (int i = 0; i < resultat.length(); i++) {

                                    JSONObject content = new JSONObject(resultat.get(i).toString());
                                    actionGroup = content.getString("action").split("#");
                                    action = actionGroup[0];
                                    object = actionGroup[1];
                                    JSONObject data = new JSONObject(content.getJSONObject("data").toString());
                                    Utilisateur u = SugarRecord.find(Utilisateur.class, "id_utilisateur = ? ", Prefs.getString(ID_UTILISATEUR_KEY, "")).get(0);

                                    if ("add".equals(action)) {
                                        if("retraits".equals(object))
                                        {
                                            Retrait retrait = new Retrait();
                                            retrait.setToken(data.getString("token"));
                                            retrait.setCreation(timestamp_creation);
                                            retrait.setMaj(timestamp_maj);
                                            retrait.setMontant(data.getString("montant"));
                                            retrait.setStatut(data.getString("statut"));
                                            List<Tontine> t = SugarRecord.find(Tontine.class, "id_server = ?", data.getString("id_tontine"));
                                            if(t.size()>0)
                                                retrait.setTontine(t.get(0));
                                            retrait.setUtilisateur(u);
                                            retrait.save();
                                            id_retrait = retrait.getId();
                                            token = String.valueOf(data.getLong("token"));
                                        }
                                    }
                                    else if("update".equals(action)){
                                        if (object.equals("tontines")) {
                                            List<Tontine> old = Tontine.find(Tontine.class, "id_server = ?", content.getString("id"));
                                            Log.e("old_t_size", String.valueOf(old.size()));
                                            if (old.size() > 0) {
                                                if(data.has("statut")) {
                                                    old.get(0).setStatut(data.getString("statut"));
                                                    Log.e("old_t_stat", data.getString("statut"));
                                                }
                                                old.get(0).setMaj(timestamp_maj);
                                                old.get(0).save();
                                            }
                                        }
                                    }
                                }
                                if(id_retrait != null)
                                {
                                    String msg="Le code de retrait portant le token "+ token +" est désormais actif pour 24H. Passé ce délai il sera inactif, vous pourrez toujours en généré autant de fois que vous voulez.";
                                    Intent i = new Intent(Carte.this, Message_ok.class);
                                    i.putExtra("msg_desc",msg);
                                    i.putExtra("id_retrait",id_retrait);
                                    startActivity(i);
                                }
                                else
                                {
                                    String msg="Une erreur est survenue!";
                                    Intent i = new Intent(Carte.this, Message_non.class);
                                    i.putExtra("msg_desc",msg);
                                    i.putExtra("id_tontine",id_tontine);
                                    i.putExtra("class","com.sicmagroup.tondi.MesTontines");
                                    startActivity(i);
                                }


                            }
                            else{
                                progressDialog.dismiss();
                                String msg=result.getString("message");
                                Intent i = new Intent(Carte.this, Message_non.class);
                                i.putExtra("msg_desc",msg);
                                i.putExtra("id_tontine",id_tontine);
                                i.putExtra("class","com.sicmagroup.tondi.MesTontines");
                                startActivity(i);
                            }


                        } catch (Throwable t) {
                            Log.d("errornscription",t.getMessage());
                            //Log.e("My App", "Could not parse malformed JSON: \"" + response + "\"");
                        }
                    }
                },
                new Response.ErrorListener()
                {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {
                        progressDialog.dismiss();
                        // error
                        //Log.d("Error.Inscription", String.valueOf(error.getMessage()));
                        ConstraintLayout mainLayout =  findViewById(R.id.layout_cartemain);

                        String message;
                        if (volleyError instanceof NetworkError || volleyError instanceof AuthFailureError || volleyError instanceof TimeoutError) {
                            //Toast.makeText(Inscription.this, "error:"+volleyError.getMessage(), Toast.LENGTH_SHORT).show();
                            //Log.d("VolleyError_Test",volleyError.getMessage());
                            message = "Aucune connexion Internet! Patientez et réessayez.";

                            Dialog dialog = new Dialog(Carte.this);
                            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                            dialog.setCancelable(false);
                            dialog.setContentView(R.layout.dialog_attention);

                            TextView titre = (TextView) dialog.findViewById(R.id.deco_title);
                            titre.setText("Attention");
                            TextView message_deco = (TextView) dialog.findViewById(R.id.deco_message);
                            message_deco.setText(message);

                            Button oui = (Button) dialog.findViewById(R.id.btn_oui);
                            oui.setText("Réessayer");

                            oui.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    requete_retrait_2(id_tontine);
                                }
                            });

                            Button non = (Button) dialog.findViewById(R.id.btn_non);
                            non.setVisibility(View.GONE);
                            dialog.show();

//                            final AlertDialog.Builder dialog_recap = new AlertDialog.Builder(CarteMain.this);
//                            dialog_recap.setTitle( "ATTENTION" )
//                                    .setIcon(R.drawable.ic_warning)
//                                    .setMessage(message)
//                                    .setCancelable(false)
//                                    .setPositiveButton("REESSAYER", new DialogInterface.OnClickListener() {
//                                        public void onClick(DialogInterface dialoginterface, int i) {
//                                            requete_retrait_2(id_tontine);
//                                        }
//                                    }).show();

                        } else if (volleyError instanceof ServerError) {
                            message = "Impossible de contacter le serveur! Patientez et réessayez.";
                            Dialog dialog = new Dialog(Carte.this);
                            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                            dialog.setCancelable(false);
                            dialog.setContentView(R.layout.dialog_attention);

                            TextView titre = (TextView) dialog.findViewById(R.id.deco_title);
                            titre.setText("Attention");
                            TextView message_deco = (TextView) dialog.findViewById(R.id.deco_message);
                            message_deco.setText(message);

                            Button oui = (Button) dialog.findViewById(R.id.btn_oui);
                            oui.setText("Réessayer");

                            oui.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    requete_retrait_2(id_tontine);
                                }
                            });

                            Button non = (Button) dialog.findViewById(R.id.btn_non);
                            non.setVisibility(View.GONE);
                            dialog.show();

//                            final AlertDialog.Builder dialog_recap = new AlertDialog.Builder(CarteMain.this);
//                            dialog_recap.setTitle( "ATTENTION" )
//                                    .setIcon(R.drawable.ic_warning)
//                                    .setMessage(message)
//                                    .setCancelable(false)
//                                    .setPositiveButton("REESSAYER", new DialogInterface.OnClickListener() {
//                                        public void onClick(DialogInterface dialoginterface, int i) {
//                                            requete_retrait_2(id_tontine);
//                                        }
//                                    }).show();
                        }  else if (volleyError instanceof ParseError) {
                            message = "Une erreur est survenue! Patientez et réessayez.";

                            Dialog dialog = new Dialog(Carte.this);
                            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                            dialog.setCancelable(false);
                            dialog.setContentView(R.layout.dialog_attention);

                            TextView titre = (TextView) dialog.findViewById(R.id.deco_title);
                            titre.setText("Attention");
                            TextView message_deco = (TextView) dialog.findViewById(R.id.deco_message);
                            message_deco.setText(message);

                            Button oui = (Button) dialog.findViewById(R.id.btn_oui);
                            oui.setText("Réessayer");

                            oui.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    requete_retrait_2(id_tontine);
                                }
                            });

                            Button non = (Button) dialog.findViewById(R.id.btn_non);
                            non.setVisibility(View.GONE);
                            dialog.show();

//                            final AlertDialog.Builder dialog_recap = new AlertDialog.Builder(CarteMain.this);
//                            dialog_recap.setTitle( "ATTENTION" )
//                                    .setIcon(R.drawable.ic_warning)
//                                    .setMessage(message)
//                                    .setCancelable(false)
//                                    .setPositiveButton("REESSAYER", new DialogInterface.OnClickListener() {
//                                        public void onClick(DialogInterface dialoginterface, int i) {
//                                            requete_retrait_2(id_tontine);
//                                        }
//                                    }).show();
                        }
                    }
                }
        ) {
            @Override
            protected Map<String, String> getParams()
            {
                Map<String, String>  params = new HashMap<String, String>();
                params.put("numero", Prefs.getString(TEL_KEY, ""));
                params.put("id_tontine_server", String.valueOf(tontine.getId_server()));
                params.put("montant", String.valueOf(tontine_main.getMontEncaisse()));
                params.put("periode", String.valueOf(tontine.getPeriode()));
                params.put("mise", String.valueOf(tontine_main.getMise()));
                params.put("carnet", String.valueOf(tontine_main.getCarnet()));
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

        postRequest.setRetryPolicy(new DefaultRetryPolicy(
                25000,
                -1,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        queue.add(postRequest);

        progressDialog = new ProgressDialog(Carte.this);
        progressDialog.setCancelable(false);
        progressDialog.setMessage("Veuillez patienter SVP! \nInitialisation de la requête de retrait en espèce...");
        progressDialog.show();


    }

    private void requete_retry_retrait_2(final int id_tontine)
    {
        final Tontine tontine = SugarRecord.findById(Tontine.class, (long)id_tontine);
        RequestQueue queue = Volley.newRequestQueue(Carte.this);
        StringRequest postRequest = new StringRequest(Request.Method.POST, Constantes.URL_RETRY_WITHDRAW,
                new Response.Listener<String>()
                {
                    @SuppressLint("ResourceAsColor")
                    @Override
                    public void onResponse(String response) {
                        //progressDialog.dismiss();
                        // response
                        Log.e("ResponseTagP", response);
                        try {

                            JSONObject result = new JSONObject(response);
                            //final JSONArray array = result.getJSONArray("data");
                            Log.e("test", String.valueOf(result.getInt("responseCode")));
                            Long id_retrait = Long.valueOf(0);
                            if (result.getInt("responseCode") == 0) {
                                // maj des dates
                                Date currentTime = Calendar.getInstance().getTime();
                                long output_creation=currentTime.getTime()/1000L;
                                String str_creation=Long.toString(output_creation);
                                long timestamp_creation = Long.parseLong(str_creation) * 1000;
                                long output_maj=currentTime.getTime()/1000L;
                                String str_maj=Long.toString(output_maj);
                                long timestamp_maj = Long.parseLong(str_maj) * 1000;
                                progressDialog.dismiss();
                                JSONArray resultat = result.getJSONArray("body");
                                String[] actionGroup = {};
                                String action = "";
                                String object = "";
                                String token = "";
                                for (int i = 0; i < resultat.length(); i++) {

                                    JSONObject content = new JSONObject(resultat.get(i).toString());
                                    actionGroup = content.getString("action").split("#");
                                    action = actionGroup[0];
                                    object = actionGroup[1];
                                    JSONObject data = new JSONObject(content.getJSONObject("data").toString());
                                    Utilisateur u = SugarRecord.find(Utilisateur.class, "id_utilisateur = ? ", Prefs.getString(ID_UTILISATEUR_KEY, "")).get(0);

                                    if ("add".equals(action)) {
                                        if("retraits".equals(object))
                                        {
                                            Retrait retrait = new Retrait();
                                            retrait.setToken(data.getString("token"));
                                            retrait.setCreation(timestamp_creation);
                                            retrait.setMaj(timestamp_maj);
                                            retrait.setMontant(data.getString("amount"));
                                            retrait.setStatut(data.getString("state"));
                                            List<Tontine> t = SugarRecord.find(Tontine.class, "id_server = ?", tontine_main.getId_server());
                                            if(t.size()>0)
                                                retrait.setTontine(t.get(0));
                                            retrait.setUtilisateur(u);
                                            retrait.save();
                                            id_retrait = retrait.getId();
                                            token = data.getString("token");
                                        }
                                    }
                                    else if("update".equals(action)){
                                        if (object.equals("tontines")) {
                                            List<Tontine> old = Tontine.find(Tontine.class, "id_server = ?", data.getString("id"));
                                            Log.e("old_t_size", String.valueOf(old.size()));
                                            if (old.size() > 0) {
                                                if(data.has("state")) {
                                                    old.get(0).setStatut(data.getString("state"));
                                                    Log.e("old_t_stat", data.getString("state"));
                                                }
                                                old.get(0).setMaj(timestamp_maj);
                                                old.get(0).save();
                                            }
                                        }
                                    }
                                }
                                if(id_retrait != null)
                                {
                                    String msg="Le code de retrait portant le token "+ token +" est désormais actif pour 24H. Passé ce délai il sera inactif, vous pourrez toujours en généré autant de fois que vous voulez.";
                                    Intent i = new Intent(Carte.this, Message_ok.class);
                                    i.putExtra("msg_desc",msg);
                                    i.putExtra("id_retrait",id_retrait);
                                    startActivity(i);
                                }
                                else
                                {
                                    String msg="Une erreur est survenue!";
                                    Intent i = new Intent(Carte.this, Message_non.class);
                                    i.putExtra("msg_desc",msg);
                                    i.putExtra("id_tontine",id_tontine);
                                    i.putExtra("class","com.sicmagroup.tondi.MesTontines");
                                    startActivity(i);
                                }


                            }
                            else{
                                progressDialog.dismiss();
                                String msg=result.getString("body");
                                Intent i = new Intent(Carte.this, Message_non.class);
                                i.putExtra("msg_desc",msg);
                                i.putExtra("id_tontine",id_tontine);
                                i.putExtra("class","com.sicmagroup.tondi.MesTontines");
                                startActivity(i);
                            }


                        } catch (Throwable t) {
                            Log.d("errornscription",t.getMessage());
                            //Log.e("My App", "Could not parse malformed JSON: \"" + response + "\"");
                        }
                    }
                },
                new Response.ErrorListener()
                {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {
                        progressDialog.dismiss();
                        // error
                        //Log.d("Error.Inscription", String.valueOf(error.getMessage()));
                        ConstraintLayout mainLayout =  findViewById(R.id.layout_cartemain);

                        String message;
                        if (volleyError instanceof NetworkError || volleyError instanceof AuthFailureError || volleyError instanceof TimeoutError) {
                            //Toast.makeText(Inscription.this, "error:"+volleyError.getMessage(), Toast.LENGTH_SHORT).show();
                            //Log.d("VolleyError_Test",volleyError.getMessage());
                            message = "Aucune connexion Internet! Patientez et réessayez.";

                            Dialog dialog = new Dialog(Carte.this);
                            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                            dialog.setCancelable(false);
                            dialog.setContentView(R.layout.dialog_attention);

                            TextView titre = (TextView) dialog.findViewById(R.id.deco_title);
                            titre.setText("Attention");
                            TextView message_deco = (TextView) dialog.findViewById(R.id.deco_message);
                            message_deco.setText(message);

                            Button oui = (Button) dialog.findViewById(R.id.btn_oui);
                            oui.setText("Réessayer");

                            oui.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    requete_retrait_2(id_tontine);
                                }
                            });

                            Button non = (Button) dialog.findViewById(R.id.btn_non);
                            non.setVisibility(View.GONE);
                            dialog.show();

//                            final AlertDialog.Builder dialog_recap = new AlertDialog.Builder(CarteMain.this);
//                            dialog_recap.setTitle( "ATTENTION" )
//                                    .setIcon(R.drawable.ic_warning)
//                                    .setMessage(message)
//                                    .setCancelable(false)
//                                    .setPositiveButton("REESSAYER", new DialogInterface.OnClickListener() {
//                                        public void onClick(DialogInterface dialoginterface, int i) {
//                                            requete_retrait_2(id_tontine);
//                                        }
//                                    }).show();

                        } else if (volleyError instanceof ServerError) {
                            message = "Impossible de contacter le serveur! Patientez et réessayez.";
                            Dialog dialog = new Dialog(Carte.this);
                            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                            dialog.setCancelable(false);
                            dialog.setContentView(R.layout.dialog_attention);

                            TextView titre = (TextView) dialog.findViewById(R.id.deco_title);
                            titre.setText("Attention");
                            TextView message_deco = (TextView) dialog.findViewById(R.id.deco_message);
                            message_deco.setText(message);

                            Button oui = (Button) dialog.findViewById(R.id.btn_oui);
                            oui.setText("Réessayer");

                            oui.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    requete_retrait_2(id_tontine);
                                }
                            });

                            Button non = (Button) dialog.findViewById(R.id.btn_non);
                            non.setVisibility(View.GONE);
                            dialog.show();

//                            final AlertDialog.Builder dialog_recap = new AlertDialog.Builder(CarteMain.this);
//                            dialog_recap.setTitle( "ATTENTION" )
//                                    .setIcon(R.drawable.ic_warning)
//                                    .setMessage(message)
//                                    .setCancelable(false)
//                                    .setPositiveButton("REESSAYER", new DialogInterface.OnClickListener() {
//                                        public void onClick(DialogInterface dialoginterface, int i) {
//                                            requete_retrait_2(id_tontine);
//                                        }
//                                    }).show();
                        }  else if (volleyError instanceof ParseError) {
                            message = "Une erreur est survenue! Patientez et réessayez.";

                            Dialog dialog = new Dialog(Carte.this);
                            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                            dialog.setCancelable(false);
                            dialog.setContentView(R.layout.dialog_attention);

                            TextView titre = (TextView) dialog.findViewById(R.id.deco_title);
                            titre.setText("Attention");
                            TextView message_deco = (TextView) dialog.findViewById(R.id.deco_message);
                            message_deco.setText(message);

                            Button oui = (Button) dialog.findViewById(R.id.btn_oui);
                            oui.setText("Réessayer");

                            oui.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    requete_retrait_2(id_tontine);
                                }
                            });

                            Button non = (Button) dialog.findViewById(R.id.btn_non);
                            non.setVisibility(View.GONE);
                            dialog.show();

//                            final AlertDialog.Builder dialog_recap = new AlertDialog.Builder(CarteMain.this);
//                            dialog_recap.setTitle( "ATTENTION" )
//                                    .setIcon(R.drawable.ic_warning)
//                                    .setMessage(message)
//                                    .setCancelable(false)
//                                    .setPositiveButton("REESSAYER", new DialogInterface.OnClickListener() {
//                                        public void onClick(DialogInterface dialoginterface, int i) {
//                                            requete_retrait_2(id_tontine);
//                                        }
//                                    }).show();
                        }
                    }
                }
        ) {
            @Override
            protected Map<String, String> getParams()
            {
                Map<String, String>  params = new HashMap<String, String>();
                params.put("customerNumber", Prefs.getString(TEL_KEY, ""));
                params.put("idTontine", String.valueOf(tontine.getId_server()));

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

        postRequest.setRetryPolicy(new DefaultRetryPolicy(
                25000,
                -1,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        queue.add(postRequest);

        progressDialog = new ProgressDialog(Carte.this);
        progressDialog.setCancelable(false);
        progressDialog.setMessage("Veuillez patienter SVP! \nInitialisation de la requête de retrait en espèce...");
        progressDialog.show();


    }
    //Fonction pour desactiver le compte de utilisateur
    public void desactiver_account()
    {
        RequestQueue queue = Volley.newRequestQueue(Carte.this);
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
                                Intent i = new Intent(Carte.this, Message_non.class);
                                i.putExtra("msg_desc",msg);
                                i.putExtra("class","com.sicmagroup.tondi.Connexion");
                                Carte.this.finish();
                                startActivity(i);

                            }
                            else
                            {
                                Prefs.putString(ID_UTILISATEUR_KEY,null);
                                Prefs.putInt(NMBR_PWD_FAILED, 0);
                                String msg="Vos identifiants sont incorrects. Veuillez réessayer SVP!";
                                Intent i = new Intent(Carte.this, Message_non.class);
                                i.putExtra("msg_desc",msg);
                                i.putExtra("class","com.sicmagroup.tondi.Connexion");
                                Carte.this.finish();
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
                params.put("customerNumber", Prefs.getString(TEL_KEY, null));
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
    public void SendFirebaseToken(final String token, final String numero)
    {
        RequestQueue queue = Volley.newRequestQueue(this);
        String url_update_firebase_token = SERVEUR+"/api/v1/utilisateurs/update_firebase_token";
        StringRequest postRequest = new StringRequest(Request.Method.POST, url_update_firebase_token,
                new Response.Listener<String>()
                {
                    @SuppressLint("ResourceAsColor")
                    @Override
                    public void onResponse(String response) {
                        //progressDialog.dismiss();
                        // response
                        Log.e("ResponseTagkk", response);
                      /*  if (!response.equals("Erreur"))
                        {*/
                        try {

                            JSONObject result = new JSONObject(response);
                            //final JSONArray array = result.getJSONArray("data");
                            //Log.d("My App", obj.toString());
                            if (result.getBoolean("success") ){
                                Log.e("testFirebase", "Success");
                            }else{
                                Log.e("testFirebase", "Failed");
                            }

                        } catch (Throwable t) {
                            Log.d("errorFirebase",t.getMessage());
                        }
                    }
                },
                new Response.ErrorListener()
                {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {
                        Log.e("ResponseTag", "Erreur");
                        String message;
//                        if (volleyError instanceof NetworkError || volleyError instanceof AuthFailureError || volleyError instanceof TimeoutError) {
//                            SendFirebaseToken(token, numero);
//
//                        } else if (volleyError instanceof ServerError) {
//                            SendFirebaseToken(token, numero);
//                            //snackbar.show();
//                        }  else if (volleyError instanceof ParseError) {
//                            SendFirebaseToken(token, numero);
//                        }
                    }
                }
        ) {
            @Override
            protected Map<String, String> getParams()
            {
                Map<String, String>  params = new HashMap<String, String>();
                params.put("numero", numero);
                params.put("firebase_token", token);
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
        postRequest.setRetryPolicy(new DefaultRetryPolicy(
                10000,
                -1,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        queue.add(postRequest);
    }


    void setupFrmPin(Dialog d, Context c){
        mRecyclerView =  d.findViewById(R.id.form_pin_access);
        frm_pin_acces = new FormBuilder(c, mRecyclerView);
        FormElementTextPassword element6 = FormElementTextPassword.createInstance().setTag(TAG_PIN).setTitle("").setRequired(true);
        List<BaseFormElement> formItems = new ArrayList<>();
        formItems.add(element6);

        frm_pin_acces.addFormElements(formItems);
    }
    private boolean pin_verifier(String pin) {
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
           return true;
        }else{
          return false;

            /*Intent i = new Intent(Carte.this, Message_non.class);
            i.putExtra("msg_desc", msg);
            i.putExtra("class","com.sicmagroup.tondi.Dashboard");
            startActivity(i);*/
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

    public class ViewDialogPin {
        void showDialog(Activity activity){
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
                    int tentative_restant = 3 - Prefs.getInt(NMBR_PWD_TENTATIVE_FAILED, 0);
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
                    //verifier sim
                    //verifier_sim(Prefs.getString(TEL_KEY,""),pin_value,intent);
                    if(pin_verifier(pin_value))
                    {
                        if(Prefs.contains(NMBR_PWD_TENTATIVE_FAILED))
                        {
                            Prefs.putInt(NMBR_PWD_TENTATIVE_FAILED, 0);
                        }
                        Carte.ViewEncaisser alert = new Carte.ViewEncaisser();
                        //alert.showDialog(mContext,new Intent(mContext, MesTontines.class));
                        alert.showDialog(Carte.this);
                    }
                    else
                    {
                        if(Prefs.contains(NMBR_PWD_TENTATIVE_FAILED))
                        {
                            int tentative = Prefs.getInt(NMBR_PWD_TENTATIVE_FAILED, 0);
                            tentative = tentative + 1;
                            Prefs.putInt(NMBR_PWD_TENTATIVE_FAILED, tentative);
                            if (tentative >= 3) {
                                btn_terminer.setEnabled(false);
                                btn_code_retrait.setEnabled(false);
                                btn_encaisser.setEnabled(false);
                                btn_nouveau_versement.setEnabled(false);
                                ScheduledThreadPoolExecutor exec = new ScheduledThreadPoolExecutor(1);
                                exec.schedule(new Runnable() {

                                    @Override
                                    public void run() {

                                        Carte.this.runOnUiThread(new Runnable() {

                                            @Override
                                            public void run() {
                                                btn_terminer.setEnabled(true);
                                                btn_code_retrait.setEnabled(true);
                                                btn_encaisser.setEnabled(true);
                                                btn_nouveau_versement.setEnabled(true);
                                                Prefs.putInt(NMBR_PWD_TENTATIVE_FAILED, 0);
                                            }
                                        });
                                    }
                                }, 24, TimeUnit.HOURS);
                            }
                        }
                        else
                        {
                            Prefs.putInt(NMBR_PWD_TENTATIVE_FAILED, 1);
                        }
                        Toast.makeText(Carte.this, "Mot de passe erroné", Toast.LENGTH_SHORT).show();
                    }
                    dialog.dismiss();

                }
            });
            //Toast.makeText(getApplicationContext(),"e",Toast.LENGTH_LONG).show();
            dialog.show();
        }
    }


    @Override
    public void onBackPressed() {
        // your code.
        Intent getIntent = getIntent();
        final boolean isNewTontine = getIntent.getBooleanExtra("isNewTontine",false);
        Log.e("isNewTontine3",""+isNewTontine);
        if (isNewTontine){
            Intent i = new Intent(Carte.this,Home.class);
            i.putExtra(Constantes.BOTTOM_NAV_DESTINATION, Constantes.DESTINATION_TONTINES);
            startActivity(i);
        }else{
            Carte.this.finish();
        }
//        Intent i = new Intent(Carte.this,Home.class);
//        i.putExtra(Constantes.BOTTOM_NAV_DESTINATION, Constantes.DESTINATION_TONTINES);
//        i.putExtra("tab_name",tontine_main.getStatut());
//        startActivity(i);
    }

    private void pay_via_ussd(final String montant_value, final String reseau){
        String phoneNumber;
        if (reseau.equals("MTN")){
            phoneNumber = "*400#".trim();
        }else{
            phoneNumber = ("*155*1*1*1*229"+MOOV_TEST+"*229"+MOOV_TEST+"*"+montant_value+"#").trim();
        }

        ussdApi = USSDController.getInstance(Carte.this);

        final Intent svc = new Intent(Carte.this, SplashLoadingService.class);
        svc.putExtra("texte","Le paiement de votre cotisation pour la tontine est en cours...");
        //startService(svc);

        ussdApi.callUSSDInvoke(phoneNumber, map, new USSDController.CallbackInvoke() {
            @Override
            public void responseInvoke(String message) {
                Prefs.putBoolean(ACCESS_BOOL, true);
                ussd_level++;
                Log.d("APPEE1", message);
                Log.d("APP_MENU_LEVEL0", String.valueOf((ussd_level)));
                // premiere reponse: repondre 1, envoi d'argent
                if (ussd_level == 1) {
                    ussdApi.send("1", new USSDController.CallbackMessage() {
                        @Override
                        public void responseMessage(String message) {
                            ussd_level++;

                            Log.d("APPEE2", message);
                            Log.d("APP_MENU_LEVEL", String.valueOf((ussd_level)));
                            // deuxieme reponse: repondre 1, Abonne MM
                            if (ussd_level == 2) {
                                ussdApi.send("1", new USSDController.CallbackMessage() {
                                    @Override
                                    public void responseMessage(String message) {
                                        ussd_level++;
                                        Log.d("APPEE", message);
                                        Log.d("APP_MENU_LEVEL", String.valueOf(ussd_level));
                                        // troisième reponse: repondre par numero marchand tondi

                                    }
                                });
                            }
                        }
                    });
                }

                if (ussd_level == 3) {
                    ussdApi.send(MTN_MECOTI, new USSDController.CallbackMessage() {
                        @Override
                        public void responseMessage(String message) {
                            ussd_level++;
                            Log.d("APP", message);
                            // quatrième reponse: repondre par numero marchand tondi pour confirmer
                            if (ussd_level == 4) {
                                ussdApi.send(MTN_MECOTI, new USSDController.CallbackMessage() {
                                    @Override
                                    public void responseMessage(String message) {
                                        ussd_level++;
                                        Log.d("APP", message);
                                        Log.d("APP_MENU_LEVEL", String.valueOf(ussd_level));
                                    }
                                });
                            }
                        }
                    });
                }

                if (ussd_level == 5) {
                    ussdApi.send(montant_value, new USSDController.CallbackMessage() {
                        @Override
                        public void responseMessage(String message) {
                            ussd_level++;
                            Log.d("APP", message);
                            // quatrième reponse: repondre par numero marchand tondi pour confirmer
                            if (ussd_level == 6) {
                                ussdApi.send("paiement tondi", new USSDController.CallbackMessage() {
                                    @Override
                                    public void responseMessage(String message) {
                                        //ussd_level++;

                                        //ussd_level=0;
                                        Log.d("APP", message);
                                        Log.d("APP_MENU_LEVEL", String.valueOf(ussd_level));
                                    }
                                });
                            }
                        }
                    });
                }

                if (ussd_level == 7) {
                    //stopService(svc);
                    ussd_level = 0;
                }

            }

            @Override
            public void over(String message) {
                Log.d("APPUSSpp", message);
                Log.d("APPUSSpp_info", "Transferteffectuepour" + montant_value);
                // traiter message quand fini OK
                String message_ok = "Transfert effectue pour  " + montant_value + " FCFA a " + MTN_TEST;

                Pattern pattern_msg_ok;
                Pattern pattern_msg_ok1;

                if (reseau.equals("MTN")) {
                    pattern_msg_ok = Pattern.compile("^(Transferteffectuepour" + montant_value + ")FCFA.+" + MTN_TEST);
                    pattern_msg_ok1 = Pattern.compile("^(Transferteffectuepour" + montant_value + ")FCFA.+" + "229" + MTN_TEST);
                } else {
                    pattern_msg_ok = Pattern.compile("^(Vousavezenvoye" + montant_value + ".00)FCFAa.+" + MOOV_TEST);
                    pattern_msg_ok1 = Pattern.compile("^(Vousavezenvoye" + montant_value + ".00)FCFAa.+" + "229" + MOOV_TEST);
                }


                Matcher matcher = pattern_msg_ok.matcher(message.replaceAll("\\s", ""));
                Matcher matcher1 = pattern_msg_ok1.matcher(message.replaceAll("\\s", ""));

                // if our pattern matches the string, we can try to extract our groups
                if (matcher.find() || matcher1.find()) {
                    verser(montant_value);
                } else {
                    /*Alerter.create(Carte.this)
                            .setTitle(message)
                            .setIcon(R.drawable.ic_warning)
                            .setTitleAppearance(R.style.TextAppearance_AppCompat_Large)
                            .setIconColorFilter(R.color.colorPrimaryDark)
                            //.setText("Vous pouvez maintenant vous connecter.")
                            .setBackgroundColorRes(R.color.colorWhite) // or setBackgroundColorInt(Color.CYAN)
                            .show();*/
                    Intent i = new Intent(Carte.this, Message_non.class);
                    i.putExtra("msg_desc",message);
                    i.putExtra("class","com.sicmagroup.tondi.Carte");
                    startActivity(i);
                    // si préférence ne pas afficher cette fenetre n'est pas defini
                    if (Prefs.getInt(DUALSIM_INFO_KEY, 0) != 1) {
                        // afficher la fenetre d'infos
                        Carte.ViewInfos alert = new Carte.ViewInfos();
                        alert.showDialog(Carte.this);
                    }

                }
            }
        });
    }
    public class ViewInfos {

        void showDialog(Activity activity){
            final Dialog dialog = new Dialog(activity);
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);

            dialog.setCancelable(false);
            dialog.setContentView(R.layout.sim1_info);
            CheckBox check_sims_i = dialog.findViewById(R.id.check_sims_i);
            check_sims_i.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    // si coché
                    if (isChecked){
                        // mettre à jour la préférence dualsim_infos_key
                        Prefs.putInt(DUALSIM_INFO_KEY,1);
                    }
                    // sinon
                    else{
                        // mettre à jour la préférence dualsim_infos_key
                        Prefs.putInt(DUALSIM_INFO_KEY,0);
                    }
                }
            });

            Button dialogButton = (Button) dialog.findViewById(R.id.btn_oui);
            dialogButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialog.dismiss();

                }
            });
            //Toast.makeText(getApplicationContext(),"e",Toast.LENGTH_LONG).show();

            dialog.show();

        }
    }
    private void afficherCarte() {
        Tontine tontine = Tontine.findById(Tontine.class, (long) id_tontine);
        if (tontine!=null){
            TextView carte_titre = findViewById(R.id.carte_titre);
            TextView carte_montant = findViewById(R.id.montant);
            TextView carte_montant_cumule = findViewById(R.id.montant_cumule);
            TextView carte_commission = findViewById(R.id.commission);
            TextView carte_montant_a_encaisser = findViewById(R.id.montant_a_encaisser);
            TextView carte_versements = findViewById(R.id.versements);
            Calendar cal = Calendar.getInstance(Locale.FRENCH);
            TextView carte_mise = findViewById(R.id.mise);
            cal.setTimeInMillis(tontine.getCreation());
            String creation = DateFormat.format("dd/MM/yyyy HH:mm:ss", cal).toString();

            int total_versements = tontine.getVersements();
            if(total_versements < 0){
                total_versements = 0;
            }
            @SuppressLint("DefaultLocale") String versements = String.format("%02d", total_versements);
            TextView carte_creation = findViewById(R.id.creation);
            TextView carte_fin = findViewById(R.id.fin);
            if (tontine_main.getStatut().equals(TontineEnum.IN_PROGRESS.toString())){
                carte_fin.setText("-");
            }else{
                Calendar cal1 = Calendar.getInstance(Locale.FRENCH);
                if (tontine.getFin()!=null){
                    cal1.setTimeInMillis(tontine.getFin());
                }
                String fin = DateFormat.format("dd/MM/yyyy", cal1).toString();
                //carte_fin.setText(fin);
                if (tontine.getFin()!=null){
                    carte_fin.setText(fin);
                }else{
                    carte_fin.setText(creation);
                }

                btn_terminer.setVisibility(View.GONE);
            }

            TextView num_carte = findViewById(R.id.num_carte);
            carte_mise.setText(tontine_main.getMise()+ " F");
            if (tontine_main.getPeriode().equals(PeriodiciteEnum.JOURNALIERE.toString())){
//                carte_titre.setText("Carnet N°"+ tontine_main.getCarnet()+" de la tontine journaliere de mise "+tontine.getMise()+" F");
                carte_titre.setText(tontine_main.getDenomination() +" Mise : "+tontine.getMise() +" F");

                num_carte.setText("Carte "+tontine_main.getPositionCarteNew(tontine_main.getId())+ " sur 12");
            }else{
//                carte_titre.setText("Carnet N°"+tontine_main.getCarnet()+" de la tontine "+tontine.getPeriode()
//                        +" de mise "+tontine.getMise()+" F");
                carte_titre.setText(tontine_main.getDenomination());

               /* carte_titre.setText("Carte N°"+tontine_main.getNumero_plus(tontine_main.getId(),Prefs.getString(CARTE_NAV_KEY,""))+" de la tontine "+tontine.getPeriode()
                        +" de mise "+tontine.getMise()+" F");*/
                num_carte.setVisibility(View.INVISIBLE);
                //num_carte.setText("Carte "+tontine_main.getNumero(tontine_main.getId(),Prefs.getString(CARTE_NAV_KEY,""))+ " sur "+tontine_main.getNumeroTotal(tontine_main.getId(),Prefs.getString(CARTE_NAV_KEY,"")));
            }
            carte_creation.setText(creation);

            //num_carte.setText("Carte "+tontine_main.getNumero(tontine_main.getId(),Prefs.getString(CARTE_NAV_KEY,""))+ " sur "+tontine_main.getNumeroTotal(tontine_main.getId(),Prefs.getString(CARTE_NAV_KEY,"")));
            if (tontine.getPeriode().equals(PeriodiciteEnum.JOURNALIERE.toString())){
                carte_versements.setText(versements+" sur 31");
                nb_vers_defaut = 31;
            }
            if (tontine.getPeriode().equals(PeriodiciteEnum.HEBDOMADAIRE.toString())){
                carte_versements.setText(versements+" sur 52");
                nb_vers_defaut=52;
            }
            if (tontine.getPeriode().equals(PeriodiciteEnum.MENSUELLE.toString())){
                carte_versements.setText(versements+" sur 12");
                nb_vers_defaut=12;
            }
            carte_montant_cumule.setText(tontine.getMontCumuleNow(id_tontine,tontine_main.getStatut())+" F");
            carte_commission.setText(new DecimalFormat("##.##").format(tontine.getMontCommisNow(id_tontine,tontine_main.getStatut()))+" F");
            carte_montant_a_encaisser.setText(new DecimalFormat("##.##").format(tontine.getMontEncaisseNow(id_tontine,tontine_main.getStatut()))+" F");
            carte_montant.setText(tontine.getMontant()+" F");
            mise=tontine.getMise();

            //Toast.makeText(getApplicationContext(),"id_tontine_"+nav+":"+id_tontine,Toast.LENGTH_LONG).show();

            for (int i=0; i< nb_vers_defaut;i++){
                Versement v = new Versement();
                Tontine t = Tontine.findById(Tontine.class, (long) id_tontine);
                v.setTontine(t);
                //v.setUssd(liste_versements.getJSONObject(0).getInt("ussd"));
                //v.setStatutPaiment(liste_versements.getJSONObject(0).getString("statut_paiement"));

                                    /*if (i <nb_versements[0]){
                                        if (liste_versements.getJSONObject(i)!=null){
                                            v.setUssd(liste_versements.getJSONObject(i).getInt("ussd"));
                                            v.setStatutPaiment(liste_versements.getJSONObject(i).getString("statut_paiement"));
                                        }else{
                                            v.setStatutPaiment("null");
                                        }
                                    }else{
                                        v.setStatutPaiment("null");
                                    }*/

                versement_list.add(v);
            }
            if (total_versements == 0 || total_versements < 0)
            {
                btn_terminer.setEnabled(false);
                btn_terminer.setClickable(false);
                btn_terminer.setActivated(false);
                btn_terminer.setVisibility(View.GONE);
            }
           /* if (tontine.getVersements() == 31 && tontine.getPositionCarte(tontine.getId()) == 12 && tontine.getPeriode().equals(PeriodiciteEnum.JOURNALIERE.toString()))
            {
                btn_nouveau_versement.setVisibility(View.GONE);
            }*/
            Log.d("versement_list", String.valueOf(versement_list.size()));
            CarteGrid adapterViewAndroid = new CarteGrid(Carte.this, versement_list, total_versements, tontine.getPeriode() );
            versements_grid=findViewById(R.id.carte_tontine);
            if (tontine.getPeriode().equals(PeriodiciteEnum.HEBDOMADAIRE.toString()))
                versements_grid.setColumnWidth(120);
            versements_grid.setAdapter(adapterViewAndroid);
        }


    }
//    private void afficherCarte(String navi) {
//        String url = url_afficher_prev;
//        if (navi.equals("next")){
//            url = url_afficher_next;
//        }
//
//        //params.put("id_utilisateur", Prefs.getString(ID_UTILISATEUR_KEY,null));
//
//        Tontine tontine = Tontine.findById(Tontine.class, (long) id_tontine);
//        if (tontine!=null){
//
//            List<Versement> liste_versements = Select.from(Versement.class)
//                    .where(Condition.prop("id_tontine").eq(id_tontine))
//                    .list();
//            TextView carte_titre = findViewById(R.id.carte_titre);
//            TextView carte_montant = findViewById(R.id.montant);
//            TextView carte_versements = findViewById(R.id.versements);
//            Calendar cal = Calendar.getInstance(Locale.FRENCH);
//            cal.setTimeInMillis(tontine.getCreation());
//            String creation = DateFormat.format("dd/MM/yyyy HH:mm:ss", cal).toString();
//
//            TextView carte_creation = findViewById(R.id.creation);
//            carte_titre.setText("Carte de la tontine "+tontine.getPeriode()
//                    +" de mise "+tontine.getMise()+" F");
//            carte_creation.setText(creation);
//            String versements = String.format("%010d", Integer.parseInt(String.valueOf(liste_versements.size())));
//            carte_versements.setText(versements+"/31");
//            carte_montant.setText("");
//            mise=tontine.getMise();
//
//            //Toast.makeText(getApplicationContext(),"id_tontine_"+nav+":"+id_tontine,Toast.LENGTH_LONG).show();
//
//            for (int i=0; i<= 30;i++){
//                Versement v = new Versement();
//                Tontine t = Tontine.findById(Tontine.class, (long) id_tontine);
//                v.setTontine(t);
//                //v.setUssd(liste_versements.getJSONObject(0).getInt("ussd"));
//                //v.setStatutPaiment(liste_versements.getJSONObject(0).getString("statut_paiement"));
//
//                                    /*if (i <nb_versements[0]){
//                                        if (liste_versements.getJSONObject(i)!=null){
//                                            v.setUssd(liste_versements.getJSONObject(i).getInt("ussd"));
//                                            v.setStatutPaiment(liste_versements.getJSONObject(i).getString("statut_paiement"));
//                                        }else{
//                                            v.setStatutPaiment("null");
//                                        }
//                                    }else{
//                                        v.setStatutPaiment("null");
//                                    }*/
//
//                versement_list.add(v);
//            }
//            if (tontine.getVersements() == 0 || )
//            {
//
//                btn_terminer.setEnabled(false);
//            }
//            if (tontine.getStatut().equals("terminee"))
//            {
//                btn_nouveau_versement.setVisibility(View.GONE);
//
//            }
//
//                                /*for (int i=0; i<= 30;i++){
//                                    Versement v = new Versement();
//                                    v.setId_tontine(array.getJSONObject(0).getInt("id_tontine"));
//                                    v.setUssd(liste_versements.getJSONObject(0).getInt("ussd"));
//                                    if (liste_versements.getJSONObject(i)!=null){
//                                        v.setStatutPaiment(liste_versements.getJSONObject(0).getString("statut_paiement"));
//                                    }else{
//                                        v.setStatutPaiment("null");
//                                    }
//                                    versement_list.add(v);
//                                }*/
//
//                                /*Versement v = new Versement();
//                                v.setId_tontine(liste_versements.getJSONObject(0).getInt("id_tontine"));
//                                v.setCreation("errdtfytg");
//                                v.setStatutPaiment(liste_versements.getJSONObject(0).getString("statut_paiement"));*/
//            //versement_list=new String[31];
//        }
//
//
//    }

    private void suiv_tontine(final int id_tontine) {
        Tontine tontine = new Tontine();
        Long id_suiv = tontine.getSuivant((long) id_tontine,tontine_main.getStatut());
        if(id_tontine==Integer.parseInt(String.valueOf(id_suiv))){
            Toast.makeText(getApplicationContext(),"Il n'y plus de tontine après celle-ci",Toast.LENGTH_LONG).show();
        }else{
            Intent getIntent = getIntent();
            final boolean isNewTontine = getIntent.getBooleanExtra("isNewTontine",false);
            Carte.this.finish();
            Intent intent = new Intent(Carte.this,Carte.class);
            intent.putExtra("id_tontine",Integer.parseInt(String.valueOf(id_suiv)));
            intent.putExtra("nav","next");
            intent.putExtra("isNewTontine", isNewTontine);
            startActivity(intent);
        }

    }
    private void prec_tontine(final int id_tontine) {
        Tontine tontine = new Tontine();
        Long id_prec = tontine.getPrecedent((long) id_tontine,tontine_main.getStatut());
        if(id_tontine==Integer.parseInt(String.valueOf(id_prec))){
            Toast.makeText(getApplicationContext(),"Il n'y plus de tontine avant celle-ci",Toast.LENGTH_LONG).show();
        }else{
            Intent getIntent = getIntent();
            final boolean isNewTontine = getIntent.getBooleanExtra("isNewTontine",false);
            Carte.this.finish();
            Intent intent = new Intent(Carte.this,Carte.class);
            intent.putExtra("id_tontine",Integer.parseInt(String.valueOf(id_prec)));
            intent.putExtra("nav","prev");
            intent.putExtra("isNewTontine", isNewTontine);
            startActivity(intent);
        }

    }

    @SuppressLint("ResourceAsColor")
    private void verser(final String montant) {
        Versement versement = new Versement();
        Tontine t = Tontine.findById(Tontine.class, (long) id_tontine);
        Tontine temp = t;
        Tontine tn = new Tontine();
        // besoin de fractionner
        // recuperer le nombre de versements effectues
        int nb_vers_eff = t.getMontant()/t.getMise();
        // recuperer le nombre de versements restants
        int nb_vers_rest = nb_vers_defaut-nb_vers_eff;
        // recuperer le nombre de versements à éffectuer
        int nb_vers_a_eff = Integer.parseInt(montant)/t.getMise();
        Long id_vers = null;
        Long id_tont = t.getId();
        double solde = 0.00;

        // si le nombre nb_vers_a_eff>nb_vers_rest
        if (nb_vers_a_eff>nb_vers_rest){
            Log.e("ICI? if", String.valueOf(nb_vers_a_eff));
            // verser la fraction principale
            Versement v = new Versement();
            v.setFractionne("1");
            v.setIdVersement("0");
            v.setTontine(t);
            v.setMontant(montant);
            // maj des dates
            Date currentTime = Calendar.getInstance().getTime();
            long output_creation=currentTime.getTime()/1000L;
            String str_creation=Long.toString(output_creation);
            long timestamp_creation = Long.parseLong(str_creation) * 1000;
            long output_maj=currentTime.getTime()/1000L;
            String str_maj=Long.toString(output_maj);
            long timestamp_maj = Long.parseLong(str_maj) * 1000;
            v.setCreation(timestamp_creation);
            v.setMaj(timestamp_maj);
            Utilisateur u = Utilisateur.find(Utilisateur.class, "id_utilisateur = ?", Prefs.getString(ID_UTILISATEUR_KEY,null)).get(0);
            v.setUtilisateur(u);
            v.save();
            long id_versement = v.getId();

            // verser le rest de la tontine actuelle
            Versement v1 = new Versement();
            v1.setFractionne("0");
            v1.setIdVersement(String.valueOf(id_versement));
            v1.setTontine(t);
            v1.setMontant(String.valueOf(nb_vers_rest*t.getMise()));
            // maj des dates
            v1.setCreation(timestamp_creation);
            v1.setMaj(timestamp_maj);
            v1.setUtilisateur(u);
            v1.save();

            // Terminer la tontine
            t.setStatut(TontineEnum.COMPLETED.toString());
            t.setMaj(timestamp_maj);
            t.save();

            // enregistrer dans synchroniser
            if(v.getId()!=null){
                List<Tontine>  list_tontines =Select.from(Tontine.class)
                        .where(Condition.prop("id_utilisateur").eq(Long.valueOf(Prefs.getString(ID_UTILISATEUR_KEY,null))))
                        .where(Condition.prop("statut").eq(TontineEnum.IN_PROGRESS.toString()))
                        .whereOr(Condition.prop("statut").eq(TontineEnum.COMPLETED.toString()))
                        .whereOr(Condition.prop("statut").eq(TontineEnum.WAITING.toString()))
                        .list();
                if(list_tontines.size()>0){
                    for (Tontine tontine:list_tontines){
                        solde=solde+tontine.getMontant();
                    }

                }
                // sync versement
                try {
                    Synchronisation new_sync = new Synchronisation();
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("a","add#versements");
                    jsonObject.put("n",u.getNumero());
                    jsonObject.put("s",solde);
                    Log.e("solde1", String.valueOf(solde));
                    Gson gson = new Gson();
                    String v_json = gson.toJson(v);
                    jsonObject.put("d",v_json);
                    new_sync.setMaj(timestamp_maj);
                    new_sync.setStatut(0);
                    new_sync.setDonnees(jsonObject.toString());
                    new_sync.save();

                    Synchronisation new_sync1 = new Synchronisation();
                    JSONObject jsonObject1 = new JSONObject();
                    jsonObject1.put("a","add#versements");
                    jsonObject1.put("n",u.getNumero());
                    jsonObject.put("s",solde);
                    Log.e("solde2", String.valueOf(solde));
                    String v1_json = gson.toJson(v1);
                    jsonObject1.put("d",v1_json);
                    new_sync1.setMaj(timestamp_maj);
                    new_sync1.setStatut(0);
                    new_sync1.setDonnees(jsonObject1.toString());
                    new_sync1.save();

                    //
                    Synchronisation new_sync2 = new Synchronisation();
                    JSONObject jsonObject2 = new JSONObject();
                    jsonObject2.put("a","update#tontines");
                    jsonObject2.put("n",u.getNumero());
                    String t_json = gson.toJson(t);
                    jsonObject2.put("d",t_json);
                    new_sync2.setMaj(timestamp_maj);
                    new_sync2.setStatut(0);
                    new_sync2.setDonnees(jsonObject2.toString());
                    new_sync2.save();

                    solde = 0.00;

                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
            nb_vers_rest = nb_vers_a_eff-nb_vers_rest;
            nb_vers_a_eff = nb_vers_rest;
            if (nb_vers_rest>nb_vers_defaut){
                nb_vers_rest = nb_vers_defaut;
            }
            // tant que le nombre nb_vers_a_eff>nb_vers_rest
            while (nb_vers_a_eff>nb_vers_rest){
                // créer une nouvelle tontine
                tn = new Tontine();
                tn.setMise(t.getMise());
                tn.setIdSim(t.getIdSim());
                tn.setId_utilisateur(t.getId_utilisateur());
                tn.setPrelevement_auto(t.getPrelevement_auto());
                tn.setPeriode(t.getPeriode());
                if(nb_vers_rest>=nb_vers_defaut){
                    tn.setStatut(TontineEnum.COMPLETED.toString());
                }else{
                    tn.setStatut(TontineEnum.IN_PROGRESS.toString());
                }


                tn.setCreation(timestamp_creation);
                tn.setMaj(timestamp_maj);
                tn.setContinuer(t.getContinuer());
                tn.setCarnet(t.getCarnet());
                tn.save();
                int no_carnet =Integer.parseInt(t.getCarnet());
                // si tontine journalière
                if(t.getPeriode().equals("Journalière")){
                    List<Tontine> derniere_tontine;
                    derniere_tontine = Tontine.findWithQuery(Tontine.class, "Select * from Tontine where  id_utilisateur="+Prefs.getString(ID_UTILISATEUR_KEY,null)+" and mise="+t.getMise()+" and periode='"+t.getPeriode()+"' and carnet='"+t.getCarnet()+"' order by id desc ");
                    if(derniere_tontine.size()>0){
                        // recuperer la position de la carte
                        int pos = derniere_tontine.get(0).getPositionCarte(derniere_tontine.get(0).getId());
                        if(pos==12){
                            t = tn;
                            /**
                             * LeDOC
                             */
                            List<Tontine> last_tontine;
                            last_tontine = Tontine.findWithQuery(Tontine.class, " SELECT * FROM Tontine where id_utilisateur="+Prefs.getString(ID_UTILISATEUR_KEY,null)+ " ORDER BY CAST(carnet AS DECIMAL(5,2))DESC ");
                            if(last_tontine.size()>0){

                                no_carnet=Integer.parseInt(last_tontine.get(0).getCarnet())+1;
                                Log.e("derniere_tontine",""+(pos)+"//"+derniere_tontine.get(0).getCarnet());

                            }
                            //no_carnet=Integer.parseInt(derniere_tontine.get(0).getCarnet())+1;
                            //tn.setContinuer(tn.getId());
                            //tn.setContinuer(tn.getId());
                        }

                        Log.d("derniere_tontine",""+(pos)+"//"+derniere_tontine.get(0).getCarnet());
                    }
                }
                else
                {
                    Log.e("Error", "1");

                    List<Tontine> last_tontine;
                    last_tontine = Tontine.findWithQuery(Tontine.class, " SELECT * FROM Tontine where id_utilisateur="+Prefs.getString(ID_UTILISATEUR_KEY,null)+ " ORDER BY CAST(carnet AS DECIMAL(5,2))DESC ");
                    if(last_tontine.size()>0){
                        no_carnet=Integer.parseInt(last_tontine.get(0).getCarnet())+1;
                    }
                }
                tn.setCarnet(String.valueOf(no_carnet));
                tn.save();

                // verser le reste de la tontine actuelle
                Versement v2 = new Versement();
                v2.setFractionne("0");
                v2.setIdVersement(String.valueOf(id_versement));
                v2.setTontine(tn);
                v2.setMontant(String.valueOf(nb_vers_rest*tn.getMise()));
                // maj des dates
                v2.setCreation(timestamp_creation);
                v2.setMaj(timestamp_maj);

                v2.setUtilisateur(u);
                v2.save();

                if(tn.getId()!=null){
                    List<Tontine>  list_tontines =Select.from(Tontine.class)
                            .where(Condition.prop("id_utilisateur").eq(Long.valueOf(Prefs.getString(ID_UTILISATEUR_KEY,null))))
                            .where(Condition.prop("statut").eq(TontineEnum.IN_PROGRESS.toString()))
                            .whereOr(Condition.prop("statut").eq(TontineEnum.COMPLETED.toString()))
                            .whereOr(Condition.prop("statut").eq(TontineEnum.WAITING.toString()))
                            .list();
                    if(list_tontines.size()>0){
                        for (Tontine tontine:list_tontines){
                            solde=solde+tontine.getMontant();
                        }
                    }
                    try {
                        Synchronisation new_sync = new Synchronisation();
                        JSONObject jsonObject = new JSONObject();
                        jsonObject.put("a","add#tontines");
                        jsonObject.put("n",u.getNumero());
                        jsonObject.put("s",solde);
                        Log.e("solde3", String.valueOf(solde));
                        Gson gson = new Gson();
                        String tn_json = gson.toJson(tn);
                        jsonObject.put("d",tn_json);
                        new_sync.setMaj(timestamp_maj);
                        new_sync.setStatut(0);
                        new_sync.setDonnees(jsonObject.toString());
                        new_sync.save();

                        Synchronisation new_sync1 = new Synchronisation();
                        JSONObject jsonObject1 = new JSONObject();
                        jsonObject1.put("a","add#versements");
                        jsonObject1.put("n",u.getNumero());
                        jsonObject1.put("s",solde);
                        Log.e("solde3", String.valueOf(solde));
                        String v2_json = gson.toJson(v2);
                        jsonObject1.put("d",v2_json);
                        new_sync1.setMaj(timestamp_maj);
                        new_sync1.setStatut(0);
                        new_sync1.setDonnees(jsonObject1.toString());
                        new_sync1.save();

                        solde = 0.00;
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                }
                //v.setMontant(String.valueOf(nb_vers_rest*t.getMise()));
                //versement.save();
                // mettre à jour nb_vers_a_eff
                /*nb_vers_rest = nb_vers_defaut-(tn.getMontant()/tn.getMise());
                nb_vers_a_eff = nb_vers_a_eff-nb_vers_rest;*/
                nb_vers_rest = nb_vers_a_eff-nb_vers_rest;
                nb_vers_a_eff = nb_vers_rest;
                if (nb_vers_rest>nb_vers_defaut){
                    nb_vers_rest = nb_vers_defaut;
                }
            }
            //t = temp;
            // créer une nouvelle tontine
            tn = new Tontine();
            tn.setMise(t.getMise());
            tn.setIdSim(t.getIdSim());
            tn.setId_utilisateur(t.getId_utilisateur());
            tn.setPeriode(t.getPeriode());
            tn.setPrelevement_auto(t.getPrelevement_auto());
            if(nb_vers_rest>=nb_vers_defaut){
                tn.setStatut(TontineEnum.COMPLETED.toString());
            }else{
                tn.setStatut(TontineEnum.IN_PROGRESS.toString());
            }
            //tn.setStatut("en cours");
            tn.setCreation(timestamp_creation);
            tn.setMaj(timestamp_maj);
            tn.setContinuer(t.getContinuer());
            tn.setCarnet(t.getCarnet());
            tn.save();
            int no_carnet =Integer.parseInt(t.getCarnet());
            // si tontine journalière
            if(t.getPeriode().equals("Journalière")){
                List<Tontine> derniere_tontine;
                derniere_tontine = Tontine.findWithQuery(Tontine.class, "Select * from Tontine where  id_utilisateur="+Prefs.getString(ID_UTILISATEUR_KEY,null)+" and mise="+t.getMise()+" and periode='"+t.getPeriode()+"' and carnet='"+t.getCarnet()+"'  order by id desc ");
                if(derniere_tontine.size()>0){
                    // recuperer la position de la carte
                    int pos = derniere_tontine.get(0).getPositionCarte(derniere_tontine.get(0).getId());
                    if(pos==12){
                       // t = tn;
                        List<Tontine> last_tontine;
                        last_tontine = Tontine.findWithQuery(Tontine.class, " SELECT * FROM Tontine where id_utilisateur="+Prefs.getString(ID_UTILISATEUR_KEY,null)+ " ORDER BY CAST(carnet AS DECIMAL(5,2))DESC ");
                        if(last_tontine.size()>0){
                            no_carnet=Integer.parseInt(last_tontine.get(0).getCarnet())+1;
                        }
                       //no_carnet=Integer.parseInt(derniere_tontine.get(0).getCarnet())+1;
                        //tn.setContinuer(tn.getId());
                    }
                    Log.d("derniere_tontineo",""+(pos));
                }
            }
            else
            {
                Log.e("Error", "2");

                List<Tontine> last_tontine;
                last_tontine = Tontine.findWithQuery(Tontine.class, " SELECT * FROM Tontine where id_utilisateur="+Prefs.getString(ID_UTILISATEUR_KEY,null)+ " ORDER BY CAST(carnet AS DECIMAL(5,2))DESC ");
                    if(last_tontine.size()>0){
                    no_carnet=Integer.parseInt(last_tontine.get(0).getCarnet())+1;
                    Log.e("Error", String.valueOf(no_carnet));
                }
            }
            tn.setCarnet(String.valueOf(no_carnet));
            tn.save();


            // verser le rest de la tontine actuelle
            Versement v2 = new Versement();
            v2.setFractionne("0");
            v2.setIdVersement(String.valueOf(id_versement));
            v2.setTontine(tn);
            v2.setMontant(String.valueOf(nb_vers_rest*tn.getMise()));
            v2.setUtilisateur(u);
            // maj des dates
            v2.setCreation(timestamp_creation);
            v2.setMaj(timestamp_maj);
            v2.save();
            // si prelevement auto
            if (tn.getPrelevement_auto()){

                List<Cotis_Auto>  cotis_auto_jour =Select.from(Cotis_Auto.class)
                        .where(Condition.prop("utilisateur").eq(u.getId()))
                        .where(Condition.prop("tontine").eq(tn.getId()))
                        .list();
                Date currentT = Calendar.getInstance().getTime();
                long output_maj1=currentT.getTime()/1000L;
                String str_maj1=Long.toString(output_maj1);
                long timestamp_maj1 = Long.parseLong(str_maj1) * 1000;
                if(cotis_auto_jour.size()>0){
                    cotis_auto_jour.get(0).setMaj(timestamp_maj1);
                    cotis_auto_jour.get(0).save();
                }

            }
            if(tn.getId()!=null){
                List<Tontine>  list_tontines =Select.from(Tontine.class)
                        .where(Condition.prop("id_utilisateur").eq(Long.valueOf(Prefs.getString(ID_UTILISATEUR_KEY,null))))
                        .where(Condition.prop("statut").eq(TontineEnum.IN_PROGRESS.toString()))
                        .whereOr(Condition.prop("statut").eq(TontineEnum.COMPLETED.toString()))
                        .whereOr(Condition.prop("statut").eq(TontineEnum.WAITING.toString()))
                        .list();
                if(list_tontines.size()>0){
                    for (Tontine tontine:list_tontines){
                        solde=solde+tontine.getMontant();
                    }
                    Log.e("solde", String.valueOf(solde));
                }
                try {
                    Synchronisation new_sync = new Synchronisation();
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("a","add#tontines");
                    jsonObject.put("n",u.getNumero());
                    jsonObject.put("s",solde);
                    Log.e("solde4", String.valueOf(solde));
                    Gson gson = new Gson();
                    String tn_json = gson.toJson(tn);
                    jsonObject.put("d",tn_json);
                    new_sync.setMaj(timestamp_maj);
                    new_sync.setStatut(0);
                    new_sync.setDonnees(jsonObject.toString());
                    new_sync.save();

                    Synchronisation new_sync1 = new Synchronisation();
                    JSONObject jsonObject1 = new JSONObject();
                    jsonObject1.put("a","add#versements");
                    jsonObject1.put("n",u.getNumero());
                    jsonObject1.put("s",solde);
                    Log.e("solde5", String.valueOf(solde));
                    String v2_json = gson.toJson(v2);
                    jsonObject1.put("d",v2_json);
                    new_sync1.setMaj(timestamp_maj);
                    new_sync1.setStatut(0);
                    new_sync1.setDonnees(jsonObject1.toString());
                    new_sync1.save();

                    solde = 0.00;
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }

            id_vers=id_versement;
            id_tont = tn.getId();
            //t = temp;

            // afficher la carte de la nouvelle tontine
            /*CarteMain.this.finish();
            Intent intent = new Intent(CarteMain.this,CarteMain.class);
            intent.putExtra("id_tontine",Integer.parseInt(String.valueOf(tn.getId())));
            startActivity(intent);*/
        }
        else{
            Log.e("ICI? else", String.valueOf(nb_vers_a_eff));
            // verser le a_effectue de la tontine actuelle
            //Toast.makeText(getApplicationContext(),"t:"+String.valueOf(nb_vers_a_eff*t.getMise()),Toast.LENGTH_LONG).show();
            Versement v = new Versement();
            v.setFractionne("0");
            v.setIdVersement("0");
            v.setTontine(t);
            v.setMontant(String.valueOf(nb_vers_a_eff*t.getMise()));

            // maj des dates
            Date currentTime = Calendar.getInstance().getTime();
            long output_creation=currentTime.getTime()/1000L;
            String str_creation=Long.toString(output_creation);
            long timestamp_creation = Long.parseLong(str_creation) * 1000;
            long output_maj=currentTime.getTime()/1000L;
            String str_maj=Long.toString(output_maj);
            long timestamp_maj = Long.parseLong(str_maj) * 1000;
            v.setCreation(timestamp_creation);
            v.setMaj(timestamp_maj);
            /*if (nb_vers_a_eff==nb_vers_rest){
                // Terminer la tontine
                t.setStatut("terminee");
                t.setMaj(timestamp_maj);
                t.save();
            }*/

            Utilisateur u = Utilisateur.find(Utilisateur.class, "id_utilisateur = ?", Prefs.getString(ID_UTILISATEUR_KEY,null)).get(0);
            v.setUtilisateur(u);
            v.save();
            if(v.getId()!=null){
                List<Tontine>  list_tontines =Select.from(Tontine.class)
                        .where(Condition.prop("id_utilisateur").eq(Long.valueOf(Prefs.getString(ID_UTILISATEUR_KEY,null))))
                        .where(Condition.prop("statut").eq(TontineEnum.IN_PROGRESS.toString()))
                        .whereOr(Condition.prop("statut").eq(TontineEnum.COMPLETED.toString()))
                        .whereOr(Condition.prop("statut").eq(TontineEnum.WAITING.toString()))
                        .list();
                if(list_tontines.size()>0){
                    for (Tontine tontine:list_tontines){
                        solde=solde+tontine.getMontant();
                    }
                    Log.e("solde", String.valueOf(solde));
                }
                try {
                    Synchronisation new_sync = new Synchronisation();
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("a","add#versements");
                    jsonObject.put("n",u.getNumero());
                    jsonObject.put("s",solde);
                    Log.e("solde6", String.valueOf(solde));
                    Gson gson = new Gson();
                    String v_json = gson.toJson(v);
                    jsonObject.put("d",v_json);
                    new_sync.setMaj(timestamp_maj);
                    new_sync.setStatut(0);
                    new_sync.setDonnees(jsonObject.toString());
                    new_sync.save();

                    solde = 0.00;

                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }

            // si fin exacte calendrier
            if(nb_vers_a_eff==nb_vers_rest){
                // Terminer la tontine
                t.setStatut(TontineEnum.COMPLETED.toString());
                t.setMaj(timestamp_maj);
                t.save();
                // céer une nouvelle tontine sans versement
                tn = new Tontine();
                tn.setMise(t.getMise());
                tn.setIdSim(t.getIdSim());
                tn.setId_utilisateur(t.getId_utilisateur());
                tn.setPeriode(t.getPeriode());
                tn.setPrelevement_auto(t.getPrelevement_auto());
                tn.setStatut("en cours");

                tn.setCreation(timestamp_creation);
                tn.setMaj(timestamp_maj);
                tn.setContinuer(t.getContinuer());
                tn.setCarnet(t.getCarnet());
                tn.save();
                int no_carnet =Integer.parseInt(t.getCarnet());
                // si tontine journalière
                if(t.getPeriode().equals("Journalière")){
                    List<Tontine> derniere_tontine;
                    derniere_tontine = Tontine.findWithQuery(Tontine.class, "Select * from Tontine where  id_utilisateur="+Prefs.getString(ID_UTILISATEUR_KEY,null)+" and mise="+t.getMise()+" and periode='"+t.getPeriode()+"' and carnet='"+t.getCarnet()+"' order by id desc ");
                    if(derniere_tontine.size()>0){
                        // recuperer la position de la carte
                        int pos = derniere_tontine.get(0).getPositionCarte(derniere_tontine.get(0).getId());
                        if(pos==12){
                            Log.e("position", String.valueOf(pos));
                           // t = tn;

                            List<Tontine> last_tontine;
                            last_tontine = Tontine.findWithQuery(Tontine.class, " SELECT * FROM Tontine where id_utilisateur="+Prefs.getString(ID_UTILISATEUR_KEY,null)+ " ORDER BY CAST(carnet AS DECIMAL(5,2))DESC ");
                            if(last_tontine.size()>0){
                                no_carnet=Integer.parseInt(last_tontine.get(0).getCarnet())+1;
                            }
                            //no_carnet=Integer.parseInt(derniere_tontine.get(0).getCarnet())+1;
                            //tn.setContinuer(tn.getId());
                        }
                        Log.d("derniere_tontine",""+(pos)+"//"+derniere_tontine.get(0).getCarnet());
                    }
                }
                else
                {
                    Log.e("Error", "3");

                    List<Tontine> last_tontine;
                    last_tontine = Tontine.findWithQuery(Tontine.class, " SELECT * FROM Tontine where id_utilisateur="+Prefs.getString(ID_UTILISATEUR_KEY,null)+ " ORDER BY CAST(carnet AS DECIMAL(5,2))DESC ");
                    if(last_tontine.size()>0){
                        no_carnet=Integer.parseInt(last_tontine.get(0).getCarnet())+1;
                    }
                }
                tn.setCarnet(String.valueOf(no_carnet));
                tn.save();
                id_tont = tn.getId();

                if(t.getId()!=null){
                    List<Tontine>  list_tontines =Select.from(Tontine.class)
                            .where(Condition.prop("id_utilisateur").eq(Long.valueOf(Prefs.getString(ID_UTILISATEUR_KEY,null))))
                            .where(Condition.prop("statut").eq(TontineEnum.IN_PROGRESS.toString()))
                            .whereOr(Condition.prop("statut").eq(TontineEnum.COMPLETED.toString()))
                            .whereOr(Condition.prop("statut").eq(TontineEnum.WAITING.toString()))
                            .list();
                    if(list_tontines.size()>0){
                        for (Tontine tontine:list_tontines){
                            solde=solde+tontine.getMontant();
                        }
                    }
                    try {
                        Synchronisation new_sync = new Synchronisation();
                        JSONObject jsonObject = new JSONObject();
                        jsonObject.put("a","update#tontines");
                        jsonObject.put("n",u.getNumero());
                        jsonObject.put("s",solde);
                        Log.e("solde7", String.valueOf(solde));
                        Gson gson = new Gson();
                        String t_json = gson.toJson(t);
                        jsonObject.put("d",t_json);
                        new_sync.setMaj(timestamp_maj);
                        new_sync.setStatut(0);
                        new_sync.setDonnees(jsonObject.toString());
                        new_sync.save();

                        Synchronisation new_sync1 = new Synchronisation();
                        JSONObject jsonObject1 = new JSONObject();
                        jsonObject1.put("a","add#tontines");
                        jsonObject1.put("n",u.getNumero());
                        jsonObject1.put("s",solde);
                        Log.e("solde8", String.valueOf(solde));
                        String tn_json = gson.toJson(tn);
                        jsonObject1.put("d",tn_json);
                        new_sync1.setMaj(timestamp_maj);
                        new_sync1.setStatut(0);
                        new_sync1.setDonnees(jsonObject1.toString());
                        new_sync1.save();

                        solde = 0.00;
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                }

                // si prelevement auto
                Utilisateur utilisateur = Utilisateur.find(Utilisateur.class, "id_utilisateur = ?", Prefs.getString(ID_UTILISATEUR_KEY,null)).get(0);

                if (tn.getPrelevement_auto()){
                    List<Cotis_Auto>  cotis_auto_jour =Select.from(Cotis_Auto.class)
                            .where(Condition.prop("utilisateur").eq(utilisateur.getId()))
                            .where(Condition.prop("tontine").eq(tn.getId()))
                            .list();
                    Date currentT = Calendar.getInstance().getTime();
                    long output_maj1=currentT.getTime()/1000L;
                    String str_maj1=Long.toString(output_maj1);
                    long timestamp_maj1 = Long.parseLong(str_maj1) * 1000;
                    if (cotis_auto_jour.size()>0){
                        cotis_auto_jour.get(0).setMaj(timestamp_maj1);
                        cotis_auto_jour.get(0).save();
                    }

                }
            }else{
                // si prelevement auto
//                Utilisateur utilisa = Utilisateur.find(Utilisateur.class, "id_utilisateur = ?", Prefs.getString(ID_UTILISATEUR_KEY,null)).get(0);

                if (t.getPrelevement_auto()){
                    List<Cotis_Auto>  cotis_auto_jour =Select.from(Cotis_Auto.class)
                            .where(Condition.prop("utilisateur").eq(u.getId()))
                            .where(Condition.prop("tontine").eq(t.getId()))
                            .list();
                    Date currentT = Calendar.getInstance().getTime();
                    long output_maj1=currentT.getTime()/1000L;
                    String str_maj1=Long.toString(output_maj1);
                    long timestamp_maj1 = Long.parseLong(str_maj1) * 1000;
                    if (cotis_auto_jour.size()>0){
                        cotis_auto_jour.get(0).setMaj(timestamp_maj1);
                        cotis_auto_jour.get(0).save();
                    }
                }
            }

            id_vers = v.getId();
        }

        if (id_vers!=null){
            Utilitaire utilitaire = new Utilitaire(Carte.this);
            // si internet, appeler synchroniser_en_ligne
            if (utilitaire.isConnected()){
                utilitaire.synchroniser_en_ligne();
            }
            Tontine tontine = SugarRecord.findById(Tontine.class,id_tont);
            int num_carte;
            Tontine t_now;
            if (tn.getId()!=null){
                t_now=tn;
            }else{
                t_now=t;
            }

            if (tontine_main.getPeriode().equals(PeriodiciteEnum.JOURNALIERE.toString())){
                num_carte=t_now.getNumero(t_now.getId(),Prefs.getString(CARTE_NAV_KEY,""));
            }else{
                num_carte=t_now.getNumero_plus(t_now.getId(),Prefs.getString(CARTE_NAV_KEY,""));
                //num_carte.setText("Carte "+tontine_main.getNumero(tontine_main.getId(),Prefs.getString(CARTE_NAV_KEY,""))+ " sur "+tontine_main.getNumeroTotal(tontine_main.getId(),Prefs.getString(CARTE_NAV_KEY,"")));
            }
            String msg="Le montant total cumulé des cotisations du carnet N° "+t_now.getCarnet()+
                    " est de "+t_now.getMontCumule(tontine.getStatut())+" F et le montant de la carte N°"+num_carte+" en cours est de "+t_now.getMontant()+"F ";
            Intent i = new Intent(Carte.this, Message_ok.class);
            i.putExtra("msg_desc",msg);
            i.putExtra("id_tontine",id_tont);
            startActivity(i);

        }else{
            Alerter.create(Carte.this)
                    .setTitle("Une erreur est survenue lors du versement. Veuillez réessayer SVP!")
                    .setIcon(R.drawable.ic_warning)
                    .setTitleAppearance(R.style.TextAppearance_AppCompat_Large)
                    .setIconColorFilter(R.color.colorPrimaryDark)
                    .setBackgroundColorRes(R.color.colorWhite) // or setBackgroundColorInt(Color.CYAN)
                    .show();
        }

    }



    void setupFrmVersement(Dialog d, Context c){
        mRecyclerView =  d.findViewById(R.id.form_versement);
        frm_versement = new FormBuilder(c, mRecyclerView);

        FormElementTextNumber element6 = FormElementTextNumber.createInstance().setTag(TAG_MONTANT).setTitle("").setRequired(true);
        List<BaseFormElement> formItems = new ArrayList<>();
        formItems.add(element6);


        element6.setValue(String.valueOf(mise));

        frm_versement.addFormElements(formItems);
    }
    public class ViewDialog {

        void showDialog(Activity activity){
            final Dialog dialog = new Dialog(activity);
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.setCancelable(true);
            dialog.setContentView(R.layout.dialog_nouveau_versement);
            setupFrmVersement(dialog,dialog.getContext());


            Button dialogButton = dialog.findViewById(R.id.btn_valider_versement);
            dialogButton.setOnClickListener(new View.OnClickListener() {
                @SuppressLint("ResourceAsColor")
                @Override
                public void onClick(View v) {
                    //Toast.makeText(getApplicationContext(), "hjgfhyerf", Toast.LENGTH_SHORT).show();
                    BaseFormElement montant = frm_versement.getFormElement(TAG_MONTANT);
                    String montant_value = montant.getValue();
                    if(montant_value.contains(" ")|| montant_value.isEmpty())
                    {
                        Toast t = Toast.makeText(getApplicationContext(),"Mauvais montant! \nVeuillez taper un montant entre "+mise +"FCFA et 100 000FCFA",Toast.LENGTH_LONG);
                        t.setGravity(Gravity.CENTER|Gravity.CENTER, 0,0);
                        t.show();
                    }
                    else {
                        montant_value = String.valueOf(Integer.parseInt(montant_value) - (Integer.parseInt(montant_value) % mise));
                        int total_a_cocher = 0;
                        int total_a_cotiser = 0;
                        int total_restant = 0;
                        if(tontine_main.getPeriode().equals(PeriodiciteEnum.JOURNALIERE.toString())){
                            total_a_cocher = 31*12;
                            total_a_cotiser = mise * total_a_cocher;
                            total_restant = total_a_cotiser - tontine_main.getMontCumule(tontine_main.getStatut());
                        } else if(tontine_main.getPeriode().equals(PeriodiciteEnum.HEBDOMADAIRE.toString())){
                            total_a_cocher = 52;
                            total_a_cotiser = mise * total_a_cocher;
                            total_restant = total_a_cotiser - tontine_main.getMontCumule(tontine_main.getStatut());
                        } else if(tontine_main.getPeriode().equals(PeriodiciteEnum.MENSUELLE.toString())){
                            total_a_cocher = 12;
                            total_a_cotiser = mise * total_a_cocher;
                            total_restant = total_a_cotiser - tontine_main.getMontCumule(tontine_main.getStatut());
                        }
                        Log.e(TAG, "onClick: "+total_restant + " cum "+tontine_main.getMontCumule(tontine_main.getStatut())+ " a co"+ total_a_cotiser);
                        if (Integer.parseInt(montant_value) < mise) {
                            Toast.makeText(getApplicationContext(), "Le montant saisi est inférieur à la mise de " + mise + " F", Toast.LENGTH_LONG).show();
                        } else if(Integer.parseInt(montant_value) > total_restant){
                            Toast t = Toast.makeText(getApplicationContext(),"Le montant saisi dépasse les capacités de votre carnet",Toast.LENGTH_LONG);
                            t.setGravity(Gravity.CENTER, 0,0);
                            t.show();
                        } else {
                            Log.e("id_sim", tontine_main.getIdSim());
                            alertView("Nouveau Versement", "Confirmez-vous ce versement de " + montant_value + " F", montant_value);
                                dialog.dismiss();

                        }

                    }
                }
            });
            //Toast.makeText(getApplicationContext(),"e",Toast.LENGTH_LONG).show();

            dialog.show();

        }
    }
    public class ViewEncaisser {

        void showDialog(Context activity){
            final Dialog dialog = new Dialog(activity);
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.setCancelable(true);
            dialog.setContentView(R.layout.dialog_retrait);

            //final String numero = SugarRecord.findById(Sim.class, Long.valueOf(tontine_main.getIdSim())).getNumero();
            final String numero = Prefs.getString(TEL_KEY, "");
            Button dialogButton = (Button) dialog.findViewById(R.id.btn_continue_retrait);
            dialogButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    radioGroup = (RadioGroup) dialog.findViewById(R.id.radio_retrait);

                    // get selected radio button from radioGroup
                    int selectedId = radioGroup.getCheckedRadioButtonId();


                    // find the radiobutton by returned id
                    radioButton = (RadioButton) dialog.findViewById(selectedId);

                    //Toast.makeText(mContext,
                    //      radioButton.getText(), Toast.LENGTH_SHORT).show();
                    /*BaseFormElement pin = frm_pin_acces.getFormElement(TAG_PIN);

                    String pin_value = pin.getValue();
                    if (pin_value.equals(Prefs.getString(PIN_KEY,""))){
                        startActivity(intent);
                    }*/
                    Tontine tontine = Tontine.findById(Tontine.class, (long) id_tontine);

                    if (radioButton.getTag().equals("1")){
                        confirmer_retrait("ATTENTION","Confirmez-vous le virement du montant à encaisser "+tontine_main.getMontEncaisse()
                                        +" ?",1
                                , numero, String.valueOf( tontine_main.getMontEncaisse()));
                        //Log.e("testMntantCumul1", String.valueOf(tontine.getMontant()));

                    }
                    if (radioButton.getTag().equals("2")){
                        confirmer_retrait("ATTENTION ","Vous êtes obligé d\'encaisser le montant cumulé de toutes les cartes terminées du carnet N°"+tontine_main.getCarnet()+" qui est de "+
                                tontine_main.getMontEncaisse()+
                                " F \n\nConfirmez-vous ce retrait?",2, numero,String.valueOf(tontine_main.getMontEncaisse()));
                    }


                    dialog.dismiss();

                }
            });
            //Toast.makeText(getApplicationContext(),"e",Toast.LENGTH_LONG).show();

            dialog.show();

        }
    }

    private void confirmer_retrait(String title, String message, final int mode, final String numero, final String montant) {

        Dialog dialog = new Dialog(Carte.this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(true);
        dialog.setContentView(R.layout.dialog_attention);

        TextView titre = (TextView) dialog.findViewById(R.id.deco_title);
        titre.setText(title);
        TextView message_deco = (TextView) dialog.findViewById(R.id.deco_message);
        message_deco.setText(message);

        ImageView imageView = (ImageView) dialog.findViewById(R.id.imageView);
        imageView.setImageResource(R.drawable.ic_info);

        Button oui = (Button) dialog.findViewById(R.id.btn_oui);
        oui.setText("Oui");
        oui.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.cancel();
                if (mode==1){
                    Log.e("testMntantCumul2", String.valueOf(montant));
                    //retrait_mmo(numero, montant);

                    //il faut envoyer une requete au server pour obtention du code de retrait par sms

                    SmsRetrieverClient client = SmsRetriever.getClient(Carte.this);
                    Task<Void> task = client.startSmsRetriever();
                    RequestQueue queue = Volley.newRequestQueue(Carte.this);

                    task.addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {

                            Intent intent = new Intent(Carte.this, CodeOtpVerification.class);
                            intent.putExtra("id_tontine", Integer.valueOf(tontine_main.getId_server()));
                            intent.putExtra("numero", numero);
                            intent.putExtra("montant", montant);
                            intent.putExtra("caller_activity", "carteMain");
                            startActivity(intent);
                        }
                    });
                    task.addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Intent intent = new Intent(Carte.this, Message_non.class);
                            intent.putExtra("msg_desc", "Une erreur interne est survenue. Si cela persiste contactez le support technique");
                            intent.putExtra("id_tontine", id_tontine);
                            startActivity(intent);
                        }
                    });

                    StringRequest postRequest = new StringRequest(Request.Method.POST, Constantes.URL_GENERATE_OTP,
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
                                            progressDialog.dismiss();

                                        }
                                        else
                                        {
                                            progressDialog.dismiss();
                                            Intent intent = new Intent(Carte.this, Message_non.class);
                                            intent.putExtra("msg_desc", result.getString("body"));
                                            intent.putExtra("id_tontine", id_tontine);
                                            startActivity(intent);
                                        }

                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                }
                            },
                            new Response.ErrorListener()
                            {
                                @Override
                                public void onErrorResponse(VolleyError error) {
                                    //progressDialog.dismiss();
                                    // error
                                    Log.e("Error.receive.smsrappel", String.valueOf(error.getMessage()));
                                }
                            }
                    ) {
                        @Override
                        protected Map<String, String> getParams() {

                            Map<String, String> params = new HashMap<String, String>();
                            params.put("number", Prefs.getString(TEL_KEY, ""));
                            params.put("type_operation", OperationTypeEnum.WITHDRAW_FROM_CUSTOMER.toString());
                            return params;
                        }
                    };
                    postRequest.setRetryPolicy(new DefaultRetryPolicy(
                            50000,
                            -1,
                            DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
                    queue.add(postRequest);



                    progressDialog = new ProgressDialog(Carte.this);
                    progressDialog.setCancelable(false);
                    progressDialog.setMessage("Veuillez patienter SVP! \nTraitement de la requête en cours.");
                    progressDialog.show();

                    //terminer(id_tontine);
                    //retrait_mmo(numero, montant);
                    //Toast.makeText(getApplicationContext(),"Ok rf",Toast.LENGTH_LONG).show();
                            /*Tontine tontine = SugarRecord.findById(Tontine.class, (long)id_tontine);
                            String msg="Le montant total de "+tontine.getMontEncaisse()+" F a été correctement transféré sur votre compte mobile money.  ";
                            Intent j = new Intent(Carte.this, Message_ok.class);
                            j.putExtra("msg_desc",msg);
                            startActivity(j);*/
                }

                if (mode==2){
                    //Toast.makeText(mContext, "id:"+id_tontine, Toast.LENGTH_SHORT).show();
                    requete_retrait_2(id_tontine);

                }

            }
        });

        Button non = (Button) dialog.findViewById(R.id.btn_non);
        non.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.cancel();
            }
        });

        dialog.show();


//        AlertDialog.Builder dialog = new AlertDialog.Builder(Carte.this);
//        dialog.setTitle( title )
//                .setIcon(R.drawable.ic_warning)
//                .setMessage(message)
//                .setNegativeButton("Non", new DialogInterface.OnClickListener() {
//                    public void onClick(DialogInterface dialoginterface, int i) {
//                        dialoginterface.cancel();
//                    }})
//                .setPositiveButton("Oui", new DialogInterface.OnClickListener() {
//                    public void onClick(DialogInterface dialoginterface, int i) {
//                        if (mode==1){
//                            Log.e("testMntantCumul2", String.valueOf(montant));
//                            //retrait_mmo(numero, montant);
//
//                            //il faut envoyer une requete au server pour obtention du code de retrait par sms
//                            RequestQueue queue = Volley.newRequestQueue(Carte.this);
//
//                            StringRequest postRequest = new StringRequest(Request.Method.POST, url_get_code_otp,
//                                    new Response.Listener<String>()
//                                    {
//                                        @SuppressLint("ResourceAsColor")
//                                        @Override
//                                        public void onResponse(String response) {
//                                            Log.e("ResponseTagMain", response);
//                                            // if (!response.equals("Erreur")) {
//                                            try {
//
//                                                JSONObject result = new JSONObject(response);
//                                                if(result.getBoolean("success") && result.getString("curl_response").contains("ID"))
//                                                {
//                                                    progressDialog.dismiss();
//                                                    Intent intent = new Intent(Carte.this, CodeOtpVerification.class);
//                                                    intent.putExtra("id_tontine", id_tontine);
//                                                    intent.putExtra("numero", numero);
//                                                    intent.putExtra("montant", montant);
//                                                    intent.putExtra("caller_activity", "carte");
//                                                    startActivity(intent);
//                                                    //rediriger vers une activité qui demande a l'user de saisir le code OTP reçu
//                                                    // les informations necessaires à la methode retrait_mmo doivent
//                                                    // être envoyé à cette nouvelle activité ainsi que les données necessaires à l'activité de success
//
//                                                }
//
//                                            } catch (JSONException e) {
//                                                e.printStackTrace();
//                                            }
//                                        }
//                                    },
//                                    new Response.ErrorListener()
//                                    {
//                                        @Override
//                                        public void onErrorResponse(VolleyError error) {
//                                            //progressDialog.dismiss();
//                                            // error
//                                            Log.e("Error.receive.smsrappel", String.valueOf(error.getMessage()));
//                                        }
//                                    }
//                            ) {
//                                @Override
//                                protected Map<String, String> getParams() {
//
//                                    Map<String, String> params = new HashMap<String, String>();
//                                    params.put("numero_client", Prefs.getString(TEL_KEY, ""));
//                                    params.put("id_tontine", String.valueOf(id_tontine));
//                                    return params;
//                                }
//                            };
//
//                            queue.add(postRequest);
//
//                            progressDialog = new ProgressDialog(Carte.this);
//                            progressDialog.setCancelable(false);
//                            progressDialog.setMessage("Veuillez patienter SVP! \n Traitement de la requête en cours.");
//                            progressDialog.show();
//
//                            //terminer(id_tontine);
//                            //retrait_mmo(numero, montant);
//                            //Toast.makeText(getApplicationContext(),"Ok rf",Toast.LENGTH_LONG).show();
//                            /*Tontine tontine = SugarRecord.findById(Tontine.class, (long)id_tontine);
//                            String msg="Le montant total de "+tontine.getMontEncaisse()+" F a été correctement transféré sur votre compte mobile money.  ";
//                            Intent j = new Intent(Carte.this, Message_ok.class);
//                            j.putExtra("msg_desc",msg);
//                            startActivity(j);*/
//                        }
//
//                        if (mode==2){
//                            //Toast.makeText(mContext, "id:"+id_tontine, Toast.LENGTH_SHORT).show();
//                            requete_retrait(id_tontine);
//
//                        }
//
//                    }
//                }).show();
    }

    private void retrait_mmo(final String numero, final String montant ) {
        RequestQueue queue = Volley.newRequestQueue(this);

        StringRequest postRequest = new StringRequest(Request.Method.POST, url_retrait_mmo,
                new Response.Listener<String>()
                {
                    @SuppressLint("ResourceAsColor")
                    @Override
                    public void onResponse(String response) {
                        //progressDialog.dismiss();
                        // response
                        Log.e("ResponseTag", response);
                      /*  if (!response.equals("Erreur"))
                        {*/
                            try {

                                JSONObject result = new JSONObject(response);
                                //final JSONArray array = result.getJSONArray("data");
                                //Log.d("My App", obj.toString());
                                if (result.getBoolean("success") ){
                                    progressDialog.dismiss();

                                    Tontine tontine = SugarRecord.findById(Tontine.class, (long)id_tontine);
                                    String msg="Le montant de "+tontine.getMontEncaisse()+" a été correctement transféré sur votre compte mobile money";

                                    //j.putExtra("id_tontine",Integer.parseInt(String.valueOf(id_tontine)));

                                    //passage a l'etat encaissee des tonines precedentes en cas de retraits cumule de cartes
                                    // maj des dates
                                    Date currentTime = Calendar.getInstance().getTime();
                                    long output_maj=currentTime.getTime()/1000L;
                                    String str_maj=Long.toString(output_maj);
                                    long timestamp_maj = Long.parseLong(str_maj) * 1000;
                                    Gson gson = new Gson();
                                    Utilisateur u = Utilisateur.find(Utilisateur.class, "id_utilisateur = ?", Prefs.getString(ID_UTILISATEUR_KEY,null)).get(0);

                                    List<Tontine> tontines = tontine.getIdForMontCumuleNow(id_tontine, tontine_main.getStatut());
                                    for (Tontine to:tontines)
                                    {
                                        to.setStatut("encaissee");
                                        to.save();

                                        try {

                                            //
                                            Synchronisation new_sync2 = new Synchronisation();
                                            JSONObject jsonObject2 = new JSONObject();
                                            jsonObject2.put("a","update#tontines");
                                            jsonObject2.put("n",u.getNumero());
                                            Log.e("soldevv", String.valueOf(u.getSolde()));
                                            jsonObject2.put("s", u.getSolde());
                                            String t_json = gson.toJson(to);
                                            jsonObject2.put("d",t_json);
                                            new_sync2.setMaj(timestamp_maj);
                                            new_sync2.setStatut(0);
                                            new_sync2.setDonnees(jsonObject2.toString());
                                            new_sync2.save();

                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                   /* tontine.setStatut("encaissee");
                                    tontine.save();*/

                                   Log.e("encaissementTestW", result.toString());

                                /* try {

                                    //
                                    Synchronisation new_sync2 = new Synchronisation();
                                    JSONObject jsonObject2 = new JSONObject();
                                    jsonObject2.put("a","update#tontines");
                                    jsonObject2.put("n",u.getNumero());
                                    String t_json = gson.toJson(tontine);
                                    jsonObject2.put("d",t_json);
                                    new_sync2.setMaj(timestamp_maj);
                                    new_sync2.setStatut(0);
                                    new_sync2.setDonnees(jsonObject2.toString());
                                    new_sync2.save();

                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }*/


                                    Utilitaire utilitaire = new Utilitaire(Carte.this);
                                    // si internet, appeler synchroniser_en_ligne
                                    if (utilitaire.isConnected()){
                                        utilitaire.synchroniser_en_ligne();
                                    }



                                    Intent j = new Intent(Carte.this, Message_ok.class);
                                    j.putExtra("msg_desc",msg);
                                    j.putExtra("class","com.sicmagroup.tondi.MesTontines");

                                    startActivity(j);
                                /*boolean flag = true;
                                if(){

                                }*/

                                    //Toast.makeText(getApplicationContext(),"zzz",Toast.LENGTH_LONG).show();
                                /*Alerter.create(Inscription.this)
                                        .setTitle(result.getString("message"))
                                        .setIcon(R.drawable.ic_check)
                                        //.setTitleAppearance(R.style.TextAppearance_AppCompat_Large)
                                        //.setIconColorFilter(R.color.colorPrimaryDark)
                                        //.setText("Vous pouvez maintenant vous connecter.")
                                        .setBackgroundColorRes(R.color.colorPrimaryDark) // or setBackgroundColorInt(Color.CYAN)
                                        .setOnHideListener(new OnHideAlertListener() {
                                            @Override
                                            public void onHide() {

                                            }
                                        })
                                        .show();*/
                                    //Intent i = new Intent(NouvelleTontine.this, Message_ok.class);
                                    //i.putExtra("msg_desc",result.getString("message"));
                                    ////i.putExtra("id_tontine",Integer.parseInt(String.valueOf(id)));
                                    //i.putExtra("class","com.sicmagroup.tondi.MesTontines");
                                    //startActivity(i);

                                }else{
                                    Log.e("encaissementTestErr", result.toString());

                                    progressDialog.dismiss();
                                /*Alerter.create(Inscription.this)
                                        .setTitle(result.getString("message"))
                                        .setIcon(R.drawable.ic_warning)
                                        .setTitleAppearance(R.style.TextAppearance_AppCompat_Large)
                                        .setIconColorFilter(R.color.colorPrimaryDark)
                                        //.setText("Vous pouvez maintenant vous connecter.")
                                        .setBackgroundColorRes(R.color.colorWhite) // or setBackgroundColorInt(Color.CYAN)
                                        .show();*/
                                    String msg=result.getString("message");
                                    Intent i = new Intent(Carte.this, Message_non.class);
                                    i.putExtra("msg_desc",msg);
                                    i.putExtra("class","com.sicmagroup.tondi.Carte");
                                    startActivity(i);
                                }


                            } catch (Throwable t) {
                                Log.d("errornscription",t.getMessage());
                                //Log.e("My App", "Could not parse malformed JSON: \"" + response + "\"");
                            }

                       /* }
                        else
                            Toast.makeText(Carte.this, "Erreur lors du versement", Toast.LENGTH_LONG).show();*/

                    }
                },
                new Response.ErrorListener()
                {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {
                        Log.e("ResponseTag", "Erreur");
                        progressDialog.dismiss();
                        // error
                        //Log.d("Error.Inscription", String.valueOf(error.getMessage()));
                        ConstraintLayout mainLayout =  findViewById(R.id.layout_cartemain);
                        // volleyError.getMessage() == null
                        if (String.valueOf(volleyError.getMessage()).equals("null"))
                        {
                            try {


                                progressDialog.dismiss();
                                Tontine tontine = SugarRecord.findById(Tontine.class, (long) id_tontine);
                                Log.d("tontinegetPeriode", tontine.getPeriode());
                                String msg = "Le montant de " + tontine.getMontEncaisse() + " a été correctement transféré sur votre compte mobile money";

                                Log.e("testTontine", tontine_main.getStatut());
                                //passage a l'etat encaissee des tonines precedentes en cas de retraits cumule de cartes
                                List<Tontine> tontines = tontine.getIdForMontCumuleNow(id_tontine, tontine_main.getStatut());
                                // maj des dates
                                Date currentTime = Calendar.getInstance().getTime();
                                long output_maj = currentTime.getTime() / 1000L;
                                String str_maj = Long.toString(output_maj);
                                long timestamp_maj = Long.parseLong(str_maj) * 1000;
                                Gson gson = new Gson();
                                Utilisateur u = SugarRecord.find(Utilisateur.class, "id_utilisateur = ? ", Prefs.getString(ID_UTILISATEUR_KEY, "")).get(0);

                                for (Tontine to:tontines)
                                {
                                    to.setStatut("encaissee");
                                    to.save();
                                    try {

                                        //
                                        Synchronisation new_sync2 = new Synchronisation();
                                        JSONObject jsonObject2 = new JSONObject();
                                        jsonObject2.put("a","update#tontines");
                                        jsonObject2.put("n",u.getNumero());
                                        Log.e("soldevv", String.valueOf(u.getSolde()));
                                        jsonObject2.put("s", u.getSolde());
                                        String t_json = gson.toJson(to);
                                        jsonObject2.put("d",t_json);
                                        new_sync2.setMaj(timestamp_maj);
                                        new_sync2.setStatut(0);
                                        new_sync2.setDonnees(jsonObject2.toString());
                                        new_sync2.save();

                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                }



                                Utilitaire utilitaire = new Utilitaire(Carte.this);
                                // si internet, appeler synchroniser_en_ligne
                                if (utilitaire.isConnected()) {
                                    utilitaire.synchroniser_en_ligne();
                                }



                                Intent j = new Intent(Carte.this, Message_ok.class);
                                j.putExtra("class", "com.sicmagroup.tondi.MesTontines");
                                //j.putExtra("id_tontine",Integer.parseInt(String.valueOf(id_tontine)));
                                j.putExtra("msg_desc", msg);

                                startActivity(j);

                            } catch (Throwable t) {
                                Log.d("errornscription", t.getMessage());
                                //Log.e("My App", "Could not parse malformed JSON: \"" + response + "\"");
                            }

                        }
                        String message;
                        if (volleyError instanceof NetworkError || volleyError instanceof AuthFailureError || volleyError instanceof TimeoutError) {
                            //Toast.makeText(Inscription.this, "error:"+volleyError.getMessage(), Toast.LENGTH_SHORT).show();
                            //Log.d("VolleyError_Test",volleyError.getMessage());
                            message = "Aucune connexion Internet!";
                            Snackbar snackbar = Snackbar
                                    .make(mainLayout, message, Snackbar.LENGTH_INDEFINITE)
                                    .setAction("REESSAYER", new View.OnClickListener() {
                                        @Override
                                        public void onClick(View view) {
                                            retrait_mmo(numero,montant);
                                        }
                                    });
                            snackbar.getView().setBackgroundColor(ContextCompat.getColor(Carte.this, R.color.colorGray));
                            // Changing message text color
                            snackbar.setActionTextColor(getResources().getColor(R.color.colorPrimaryDark));
                            // Changing action button text color
                            View sbView = snackbar.getView();
                            TextView textView = (TextView) sbView.findViewById( com.google.android.material.R.id.snackbar_text);
                            textView.setTextColor(Color.WHITE);
                            //snackbar.show();

                        } else if (volleyError instanceof ServerError) {
                            message = "Impossible de contacter le serveur!";
                            Snackbar snackbar = Snackbar
                                    .make(mainLayout, message, Snackbar.LENGTH_INDEFINITE)
                                    .setAction("REESSAYER", new View.OnClickListener() {
                                        @Override
                                        public void onClick(View view) {
                                            retrait_mmo(numero,montant);
                                        }
                                    });
                            snackbar.getView().setBackgroundColor(ContextCompat.getColor(Carte.this, R.color.colorGray));
                            // Changing message text color
                            snackbar.setActionTextColor(getResources().getColor(R.color.colorPrimaryDark));
                            // Changing action button text color
                            View sbView = snackbar.getView();
                            TextView textView = (TextView) sbView.findViewById(com.google.android.material.R.id.snackbar_text);
                            textView.setTextColor(Color.WHITE);
                            //snackbar.show();
                        }  else if (volleyError instanceof ParseError) {
                            //message = "Parsing error! Please try again later";
                            message = "Une erreur est survenue!";
                            Snackbar snackbar = Snackbar
                                    .make(mainLayout, message, Snackbar.LENGTH_INDEFINITE)
                                    .setAction("REESSAYER", new View.OnClickListener() {
                                        @Override
                                        public void onClick(View view) {
                                            retrait_mmo(numero,montant);
                                        }
                                    });
                            snackbar.getView().setBackgroundColor(ContextCompat.getColor(Carte.this, R.color.colorGray));
                            // Changing message text color
                            snackbar.setActionTextColor(getResources().getColor(R.color.colorPrimaryDark));
                            // Changing action button text color
                            View sbView = snackbar.getView();
                            TextView textView = (TextView) sbView.findViewById(com.google.android.material.R.id.snackbar_text);
                            textView.setTextColor(Color.WHITE);
                            //snackbar.show();
                        }
                    }
                }
        ) {
            @Override
            protected Map<String, String> getParams()
            {


                Map<String, String>  params = new HashMap<String, String>();
                params.put("numero", numero);
                params.put("montant", montant);
                Log.e("paramPost", montant);
                return params;
            }
        };
        /*postRequest.setRetryPolicy(new DefaultRetryPolicy(
                0,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));*/
        //DefaultRetryPolicy  retryPolicy = new DefaultRetryPolicy(0, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        //postRequest.setRetryPolicy(new DefaultRetryPolicy(0, -1, 0));
        //postRequest.setRetryPolicy(retryPolicy);
        postRequest.setRetryPolicy(new DefaultRetryPolicy(
                10000,
                -1,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        queue.add(postRequest);

        //initialize the progress dialog and show it
        progressDialog = new ProgressDialog(Carte.this);
        progressDialog.setCancelable(false);
        progressDialog.setMessage("Veuillez patienter SVP! \n Le virement sur votre compte mobile money est en cours...");
        progressDialog.show();
    }


    private static final String ALLOWED_CHARACTERS ="0123456789";
    private static String getRandomString(final int sizeOfRandomString)
    {
        final Random random=new Random();
        final StringBuilder sb=new StringBuilder(sizeOfRandomString);
        for(int i=0;i<sizeOfRandomString;++i)
            sb.append(ALLOWED_CHARACTERS.charAt(random.nextInt(ALLOWED_CHARACTERS.length())));
        return sb.toString();
    }

//    private void requete_retrait(final int id_tontine) {
//        // enregistrer le retrait
//        //MySecureRandom mostSecureRandom = new MySecureRandom();
//        final Retrait retrait = new Retrait();
//        Tontine tontine = SugarRecord.findById(Tontine.class, (long)id_tontine);
//        List<Tontine> tontines = tontine.getIdForMontCumuleNow(id_tontine, tontine_main.getStatut());
//
//        //passage de la tontine au statut "en attente"
//        tontine.setStatut("en attente");
//
//        retrait.setTontine(tontine);
//        retrait.setToken(Long.valueOf(getRandomString(12)));
//        retrait.setStatut("en cours");
//        retrait.setMontant(String.valueOf(tontine_main.getMontEncaisse()));
//        Utilisateur utilisateur = SugarRecord.findById(Utilisateur.class, Long.valueOf(Prefs.getString(ID_UTILISATEUR_KEY,null)));
//        retrait.setUtilisateur(utilisateur);
//        // maj des dates
//        Date currentTime = Calendar.getInstance().getTime();
//        long output_creation=currentTime.getTime()/1000L;
//        String str_creation=Long.toString(output_creation);
//        long timestamp_creation = Long.parseLong(str_creation) * 1000;
//        long output_maj=currentTime.getTime()/1000L;
//        String str_maj=Long.toString(output_maj);
//        long timestamp_maj = Long.parseLong(str_maj) * 1000;
//        retrait.setCreation(timestamp_creation);
//        retrait.setMaj(timestamp_maj);
//        retrait.save();
//        Gson gson = new Gson();
//
//        //sauvegarde de la modification du statut de l'ensemble des tontines concernées
//        for (Tontine to:tontines)
//        {
//            to.setStatut("en attente");
//            to.save();
//            try {
//
//                //
//                Synchronisation new_sync2 = new Synchronisation();
//                JSONObject jsonObject2 = new JSONObject();
//                jsonObject2.put("a","update#tontines");
//                jsonObject2.put("n",utilisateur.getNumero());
//                Log.e("soldevv", String.valueOf(utilisateur.getSolde()));
//                jsonObject2.put("s", utilisateur.getSolde());
//                String t_json = gson.toJson(to);
//                jsonObject2.put("d",t_json);
//                new_sync2.setMaj(timestamp_maj);
//                new_sync2.setStatut(0);
//                new_sync2.setDonnees(jsonObject2.toString());
//                new_sync2.save();
//
//            } catch (JSONException e) {
//                e.printStackTrace();
//            }
//        }
//        tontine.save();
//
//        if (retrait.getId()!=null){
//            // enregistrer dans synchroniser
//            Gson gson1 = new Gson();
//            Utilisateur u = Utilisateur.find(Utilisateur.class, "id_utilisateur = ?", Prefs.getString(ID_UTILISATEUR_KEY,null)).get(0);
//            try {
//
//                /*Synchronisation new_sync2 = new Synchronisation();
//                JSONObject jsonObject2 = new JSONObject();
//                jsonObject2.put("a","update#tontines");
//                jsonObject2.put("n",u.getNumero());
//                jsonObject2.put("s", u.getSolde());
//                String t_json = gson1.toJson(tontine);
//                jsonObject2.put("d",t_json);
//                new_sync2.setMaj(timestamp_maj);
//                new_sync2.setStatut(0);
//                new_sync2.setDonnees(jsonObject2.toString());
//                new_sync2.save();*/
//
//                Synchronisation new_sync = new Synchronisation();
//                JSONObject jsonObject = new JSONObject();
//                jsonObject.put("a","add#retraits");
//                jsonObject.put("n",utilisateur.getNumero());
//
//                String retrait_json = gson1.toJson(retrait);
//                jsonObject.put("d",retrait_json);
//                new_sync.setMaj(timestamp_maj);
//                new_sync.setStatut(0);
//                new_sync.setDonnees(jsonObject.toString());
//                new_sync.save();
//
//            } catch (JSONException e) {
//                e.printStackTrace();
//            }
//            Utilitaire utilitaire = new Utilitaire(Carte.this);
//            if (utilitaire.isConnected()){
//                utilitaire.synchroniser_en_ligne();
//            }
//            String msg="Le code de retrait portant le token "+retrait.getToken()+" est désormais actif pour 24H. Passé ce délai il sera inactif, vous pourrez toujours en généré autant de fois que vous voulez.";
//            Intent i = new Intent(Carte.this, Message_ok.class);
//            i.putExtra("msg_desc",msg);
//            i.putExtra("id_retrait",Integer.parseInt(String.valueOf(retrait.getId())));
//            startActivity(i);
//        }
//    }

    private void alertView(String title , String message, final int id_tontine ) {

        Dialog dialog = new Dialog(Carte.this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(true);
        dialog.setContentView(R.layout.dialog_attention);

        TextView titre = (TextView) dialog.findViewById(R.id.deco_title);
        titre.setText(title);
        TextView message_deco = (TextView) dialog.findViewById(R.id.deco_message);
        message_deco.setText(message);

        Button oui = (Button) dialog.findViewById(R.id.btn_oui);
        oui.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                terminer_tontine(tontine_main.getId_server());
            }
        });

        Button non = (Button) dialog.findViewById(R.id.btn_non);
        non.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.cancel();
            }
        });

        dialog.show();

//        AlertDialog.Builder dialog = new AlertDialog.Builder(Carte.this);
//        dialog.setTitle( title )
//                .setIcon(R.drawable.ic_warning)
//                .setMessage(message)
//                .setNegativeButton("Non", new DialogInterface.OnClickListener() {
//                    public void onClick(DialogInterface dialoginterface, int i) {
//                        dialoginterface.cancel();
//                    }})
//                .setPositiveButton("Oui", new DialogInterface.OnClickListener() {
//                    public void onClick(DialogInterface dialoginterface, int i) {
//
//                        terminer_tontine(tontine_main.getId_server());
//
//
////                        int num_carte;
////                        if (tontine_main.getPeriode().equals(PeriodiciteEnum.JOURNALIERE.toString())){
////                            num_carte=tontine_main.getNumero(tontine_main.getId(),Prefs.getString(CARTE_NAV_KEY,""));
////                        }else{
////                            num_carte=tontine_main.getNumero_plus(tontine_main.getId(),Prefs.getString(CARTE_NAV_KEY,""));
////                            //num_carte.setText("Carte "+tontine_main.getNumero(tontine_main.getId(),Prefs.getString(CARTE_NAV_KEY,""))+ " sur "+tontine_main.getNumeroTotal(tontine_main.getId(),Prefs.getString(CARTE_NAV_KEY,"")));
////                        }
////
////                        if (num_carte!=12) {
////                            Tontine tn = new Tontine();
////                            tn.setMise(tontine_main.getMise());
////                            tn.setIdSim(tontine_main.getIdSim());
////                            tn.setId_utilisateur(tontine_main.getId_utilisateur());
////                            tn.setPrelevement_auto(tontine_main.getPrelevement_auto());
////                            tn.setPeriode(tontine_main.getPeriode());
////                            tn.setStatut("en cours");
////                            // maj des dates
////                            Date currentTime = Calendar.getInstance().getTime();
////                            long output_creation=currentTime.getTime()/1000L;
////                            String str_creation=Long.toString(output_creation);
////                            long timestamp_creation = Long.parseLong(str_creation) * 1000;
////                            long output_maj=currentTime.getTime()/1000L;
////                            String str_maj=Long.toString(output_maj);
////                            long timestamp_maj = Long.parseLong(str_maj) * 1000;
////
////                            tn.setCreation(timestamp_creation);
////                            tn.setMaj(timestamp_maj);
////                            tn.setContinuer(tontine_main.getContinuer());
////                            tn.setCarnet(tontine_main.getCarnet());
////                            tn.save();
////
////                            Utilisateur u = Utilisateur.find(Utilisateur.class, "id_utilisateur = ?", Prefs.getString(ID_UTILISATEUR_KEY,null)).get(0);
////
////                            try {
////                                Gson gson = new Gson();
////                                Synchronisation new_sync2 = new Synchronisation();
////                                JSONObject jsonObject2 = new JSONObject();
////                                jsonObject2.put("a","add#tontines");
////                                jsonObject2.put("n",u.getNumero());
////                                jsonObject2.put("s", u.getSolde());
////                                String t_json = gson.toJson(tn);
////                                jsonObject2.put("d",t_json);
////                                new_sync2.setMaj(timestamp_maj);
////                                new_sync2.setStatut(0);
////                                new_sync2.setDonnees(jsonObject2.toString());
////                                new_sync2.save();
////                            } catch (JSONException e) {
////                                e.printStackTrace();
////                            }
////                        }
////                        terminer(id_tontine);
//
//                    }
//                }).show();
    }

    private void terminer_tontine(final String id_tontine_server)
    {
        RequestQueue queue = Volley.newRequestQueue(Carte.this);
        StringRequest postRequest = new StringRequest(Request.Method.POST, Constantes.URL_COMPLETED_TONTINE,
                new Response.Listener<String>()
                {
                    @SuppressLint("ResourceAsColor")
                    @Override
                    public void onResponse(String response) {
                        //progressDialog.dismiss();
                        // response
                        Log.d("ResponseTagP", response);
                        Long id_tont = Long.valueOf(0);
                        try {

                            JSONObject result = new JSONObject(response);
                            //final JSONArray array = result.getJSONArray("data");
                            Log.d("success_op?", String.valueOf(result.getBoolean("success")));

                            if (result.getInt("responseCode") == 0){
                                // maj des dates
                                Date currentTime = Calendar.getInstance().getTime();
                                long output_creation=currentTime.getTime()/1000L;
                                String str_creation=Long.toString(output_creation);
                                long timestamp_creation = Long.parseLong(str_creation) * 1000;
                                long output_maj=currentTime.getTime()/1000L;
                                String str_maj=Long.toString(output_maj);
                                long timestamp_maj = Long.parseLong(str_maj) * 1000;

                                progressDialog.dismiss();

                                JSONArray resultat = result.getJSONArray("body");
                                String[] actionGroup = {};
                                String action = "";
                                String object = "";
                                for (int i = 0; i < resultat.length(); i++)
                                {
                                    JSONObject content = new JSONObject(resultat.get(i).toString());
                                    actionGroup =  content.getString("action").split("#");
                                    action = actionGroup[0];
                                    object = actionGroup[1];
                                    JSONObject data = new JSONObject(content.getJSONObject("data").toString());
                                    Utilisateur u = Utilisateur.find(Utilisateur.class, "id_utilisateur = ?", Prefs.getString(ID_UTILISATEUR_KEY,null)).get(0);
                                    if ("add".equals(action)) {
                                        if (object.equals("tontines")) {
                                            Tontine nouvelle_tontine = new Tontine();
                                            nouvelle_tontine.setId_server(data.getString("id"));
                                            Log.i("new_t_id", String.valueOf(nouvelle_tontine.getId_server()));

                                            nouvelle_tontine.setId_utilisateur(Prefs.getString(ID_UTILISATEUR_KEY,null));
                                            Log.i("new_t_idUser", String.valueOf(nouvelle_tontine.getId_utilisateur()));

                                            nouvelle_tontine.setPeriode(data.getString("periode"));
                                            Log.i("new_t_periode", String.valueOf(nouvelle_tontine.getPeriode()));

                                            nouvelle_tontine.setMise(data.getInt("mise"));
                                            Log.i("new_t_idMise", String.valueOf(nouvelle_tontine.getMise()));

                                            nouvelle_tontine.setPrelevement_auto(data.getBoolean("isAutoPayment"));
//                                            Log.i("new_t_idAuto", String.valueOf(nouvelle_tontine.getPrelevement_auto()));

//                                            nouvelle_tontine.setIdSim(data.getString("id_sim"));
//                                            Log.i("new_t_idSim", String.valueOf(nouvelle_tontine.getIdSim()));

                                            if(!data.isNull("unBlockDate")){
                                                nouvelle_tontine.setDateDeblocage(data.getString("unBlockDate"));
                                            }


                                            nouvelle_tontine.setCarnet(String.valueOf(data.getString("carnet")));
                                            Log.i("new_t_Carnet", String.valueOf(nouvelle_tontine.getCarnet()));

                                            nouvelle_tontine.setStatut(data.getString("state"));
                                            Log.i("new_t_Statut", String.valueOf(nouvelle_tontine.getStatut()));

                                            nouvelle_tontine.setCreation(timestamp_creation);
                                            Log.i("new_t_crea", String.valueOf(nouvelle_tontine.getCreation()));

                                            nouvelle_tontine.setMaj(timestamp_maj);
                                            Log.i("new_t_Maj", String.valueOf(nouvelle_tontine.getMaj()));

                                            nouvelle_tontine.setContinuer(data.getLong("carnet"));

                                            nouvelle_tontine.save();
                                            id_tont = nouvelle_tontine.getId();
//                                                Tontine old = Tontine.findById(Tontine.class, id_tontine);
//                                                Log.i("old_t", String.valueOf(old.getId()));
                                            if (nouvelle_tontine.getPrelevement_auto()) {
                                                // ajouter cotisations automatiques
                                                Cotis_Auto cotis_auto = new Cotis_Auto();
                                                cotis_auto.setTontine(nouvelle_tontine);
                                                Utilisateur utilisateur = SugarRecord.findById(Utilisateur.class, Long.valueOf(Prefs.getString(ID_UTILISATEUR_KEY, null)));
                                                cotis_auto.setUtilisateur(utilisateur);
                                                // maj des dates
                                                cotis_auto.setCreation(timestamp_creation);
                                                cotis_auto.setMaj(timestamp_creation);
                                                cotis_auto.save();
                                            }
                                        }
                                    }
                                    else if("update".equals(action))
                                    {
                                        if (object.equals("tontines")) {
                                            List<Tontine> old = Tontine.find(Tontine.class, "id_server = ?", data.getString("id"));
                                            Log.e("old_t_size", String.valueOf(old.size()));
                                            if (old.size() > 0) {
                                                if(data.has("statut")) {
                                                    old.get(0).setStatut(data.getString("statut"));
                                                    Log.e("old_t_stat", data.getString("statut"));
                                                }
                                                if(data.has("carnet")) {
                                                    old.get(0).setCarnet(data.getString("carnet"));
                                                    Log.e("old_t_carn", data.getString("carnet"));
                                                }
                                                old.get(0).setMaj(timestamp_maj);
                                                old.get(0).save();
                                                id_tont = old.get(0).getId();
                                            }
                                        }
                                    }
                                }

                                Tontine tontine = SugarRecord.findById(Tontine.class, (long) id_tontine);
                                tontine.terminer(Carte.this);
                                String msg = "";
                                if (tontine.getPeriode().equals(PeriodiciteEnum.JOURNALIERE.toString())){
                                    msg="Votre tontine "+tontine.getPeriode().toLowerCase()+" du mois en cours a été arrêtée au montant cumulé total de "+tontine.getMontant()+" F. Vous pouvez encaisser le montant de "+tontine.getMontEncaisse()+" F" ;
                                }else{
                                    msg="Votre tontine "+tontine.getPeriode().toLowerCase()+" a été arrêtée au montant cumulé total de "+tontine.getMontant()+" F. Vous pouvez encaisser le montant de "+tontine.getMontEncaisse()+" F" ;
                                }

                                //synchronisation apres terminer


                                Intent i = new Intent(Carte.this, Message_ok.class);
                                i.putExtra("msg_desc",msg);
                                i.putExtra("id_tontine", tontine_main.getId());
                                i.putExtra("isNewTontine", true);
                                i.putExtra("class","com.sicmagroup.tondi.MesTontines");
                                startActivity(i);

                            }else{
                                progressDialog.dismiss();
                                String msg=result.getString("body");
                                Intent i = new Intent(Carte.this, Message_non.class);
                                i.putExtra("msg_desc",msg);
                                i.putExtra("id_tontine",tontine_main.getId());
                                i.putExtra("class","com.sicmagroup.tondi.MesTontines");
                                startActivity(i);
                            }


                        } catch (Throwable t) {
                            Log.d("errornscription",t.getMessage());
                            //Log.e("My App", "Could not parse malformed JSON: \"" + response + "\"");
                        }
                    }
                },
                new Response.ErrorListener()
                {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {
                        progressDialog.dismiss();
                        // error
                        //Log.d("Error.Inscription", String.valueOf(error.getMessage()));
                        ConstraintLayout mainLayout =  findViewById(R.id.layout_tontine);

                        String message;
                        if (volleyError instanceof NetworkError || volleyError instanceof AuthFailureError || volleyError instanceof TimeoutError) {
                            //Toast.makeText(Inscription.this, "error:"+volleyError.getMessage(), Toast.LENGTH_SHORT).show();
                            //Log.d("VolleyError_Test",volleyError.getMessage());
                            message = "Aucune connexion Internet! Patientez et réessayez.";
                            Dialog dialog = new Dialog(Carte.this);
                            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                            dialog.setCancelable(false);
                            dialog.setContentView(R.layout.dialog_attention);

                            TextView titre = (TextView) dialog.findViewById(R.id.deco_title);
                            titre.setText("Attention");
                            TextView message_deco = (TextView) dialog.findViewById(R.id.deco_message);
                            message_deco.setText(message);

                            Button oui = (Button) dialog.findViewById(R.id.btn_oui);
                            oui.setText("Réessayer");

                            oui.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    terminer_tontine(tontine_main.getId_server());
                                }
                            });

                            Button non = (Button) dialog.findViewById(R.id.btn_non);
                            non.setVisibility(View.GONE);
                            dialog.show();

//                            final AlertDialog.Builder dialog_recap = new AlertDialog.Builder(Carte.this);
//                            dialog_recap.setTitle( "ATTENTION" )
//                                    .setIcon(R.drawable.ic_warning)
//                                    .setMessage(message)
//                                    .setCancelable(false)
//                                    .setPositiveButton("REESSAYER", new DialogInterface.OnClickListener() {
//                                        public void onClick(DialogInterface dialoginterface, int i) {
//                                            terminer_tontine(tontine_main.getId_server());
//                                        }
//                                    }).show();
                        } else if (volleyError instanceof ServerError) {
                            message = "Impossible de contacter le serveur! Patientez et réessayez";
                            Dialog dialog = new Dialog(Carte.this);
                            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                            dialog.setCancelable(false);
                            dialog.setContentView(R.layout.dialog_attention);

                            TextView titre = (TextView) dialog.findViewById(R.id.deco_title);
                            titre.setText("Attention");
                            TextView message_deco = (TextView) dialog.findViewById(R.id.deco_message);
                            message_deco.setText(message);

                            Button oui = (Button) dialog.findViewById(R.id.btn_oui);
                            oui.setText("Réessayer");

                            oui.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    terminer_tontine(tontine_main.getId_server());
                                }
                            });

                            Button non = (Button) dialog.findViewById(R.id.btn_non);
                            non.setVisibility(View.GONE);
                            dialog.show();
//                            final AlertDialog.Builder dialog_recap = new AlertDialog.Builder(Carte.this);
//                            dialog_recap.setTitle( "ATTENTION" )
//                                    .setIcon(R.drawable.ic_warning)
//                                    .setMessage(message)
//                                    .setCancelable(false)
//                                    .setPositiveButton("REESSAYER", new DialogInterface.OnClickListener() {
//                                        public void onClick(DialogInterface dialoginterface, int i) {
//                                            terminer_tontine(tontine_main.getId_server());
//                                        }
//                                    }).show();
//
                        }  else if (volleyError instanceof ParseError) {
                            //message = "Parsing error! Please try again later";
                            message = "Une erreur est survenue! Patientez et réessayez.";
                            Dialog dialog = new Dialog(Carte.this);
                            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                            dialog.setCancelable(false);
                            dialog.setContentView(R.layout.dialog_attention);

                            TextView titre = (TextView) dialog.findViewById(R.id.deco_title);
                            titre.setText("Attention");
                            TextView message_deco = (TextView) dialog.findViewById(R.id.deco_message);
                            message_deco.setText(message);

                            Button oui = (Button) dialog.findViewById(R.id.btn_oui);
                            oui.setText("Réessayer");

                            oui.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    terminer_tontine(tontine_main.getId_server());
                                }
                            });

                            Button non = (Button) dialog.findViewById(R.id.btn_non);
                            non.setVisibility(View.GONE);
                            dialog.show();
//                            final AlertDialog.Builder dialog_recap = new AlertDialog.Builder(Carte.this);
//                            dialog_recap.setTitle( "ATTENTION" )
//                                    .setIcon(R.drawable.ic_warning)
//                                    .setMessage(message)
//                                    .setCancelable(false)
//                                    .setPositiveButton("REESSAYER", new DialogInterface.OnClickListener() {
//                                        public void onClick(DialogInterface dialoginterface, int i) {
//                                            terminer_tontine(tontine_main.getId_server());
//                                        }
//                                    }).show();
                        }
                    }
                }
        ) {
            @Override
            protected Map<String, String> getParams()
            {
                Map<String, String>  params = new HashMap<String, String>();
                params.put("customerNumber", Prefs.getString(TEL_KEY, ""));
                params.put("idTontine", id_tontine_server);
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

        postRequest.setRetryPolicy(new DefaultRetryPolicy(
                8000,
                -1,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        queue.add(postRequest);

        progressDialog = new ProgressDialog(Carte.this);
        progressDialog.setCancelable(false);
        progressDialog.setMessage("Veuillez patienter SVP! \nL'arrêt de votre carte est en cours...");
        progressDialog.show();
    }

    private void terminer(final int id_tontine) {
        Tontine tontine = SugarRecord.findById(Tontine.class, (long) id_tontine);
        tontine.terminer(Carte.this);
        String msg = "";

        if (tontine.getPeriode().equals(PeriodiciteEnum.JOURNALIERE.toString())){
            msg="Votre tontine "+tontine.getPeriode().toLowerCase()+" du mois en cours a été arrêtée au montant cumulé total de "+tontine.getMontant()+" F. Vous pouvez encaisser le montant de "+tontine.getMontEncaisse()+" F" ;
        }else{
            msg="Votre tontine "+tontine.getPeriode().toLowerCase()+" a été arrêtée au montant cumulé total de "+tontine.getMontant()+" F. Vous pouvez encaisser le montant de "+tontine.getMontEncaisse()+" F" ;
        }

        Intent i = new Intent(Carte.this, Message_ok.class);
        i.putExtra("msg_desc",msg);
        i.putExtra("class","com.sicmagroup.tondi.MesTontines");
        startActivity(i);
    }

    private void alertView(String title , String message, final String montant_value) {

        Dialog dialog = new Dialog(Carte.this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(true);
        dialog.setContentView(R.layout.dialog_attention);

        TextView titre = (TextView) dialog.findViewById(R.id.deco_title);
        titre.setText(title);
        TextView message_deco = (TextView) dialog.findViewById(R.id.deco_message);
        message_deco.setText(message);

        Button oui = (Button) dialog.findViewById(R.id.btn_oui);
        oui.setText("Oui");
        oui.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Utilitaire utilitaire = new Utilitaire(Carte.this);
                if(utilitaire.isConnected())
                {
                    dialog.dismiss();
                    Tontine t = Tontine.findById(Tontine.class, (long) id_tontine);
                    int nbre_versemnt_defaut = 31;
                    if(t.getPeriode().equals(PeriodiciteEnum.MENSUELLE))
                        nbre_versemnt_defaut = 12;
                    else if(t.getPeriode().equals(PeriodiciteEnum.HEBDOMADAIRE))
                        nbre_versemnt_defaut = 52;
                    pay_via_internet(montant_value, t.getId_server(), nbre_versemnt_defaut, t.getMontant());
                }
                else{
                    Toast.makeText(Carte.this, "Mode hors connexion indisponible", Toast.LENGTH_LONG);
                }
//                pay_via_ussd(montant_value, reseau);
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


//        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
//        dialog.setTitle( title )
//                .setIcon(R.drawable.ic_warning)
//                .setMessage(message)
//                .setNegativeButton("Non", new DialogInterface.OnClickListener() {
//                    public void onClick(DialogInterface dialoginterface, int i) {
//                        dialoginterface.cancel();
//                    }})
//                .setPositiveButton("Oui", new DialogInterface.OnClickListener() {
//                    @SuppressLint("ResourceAsColor")
//                    public void onClick(DialogInterface dialoginterface, int i) {
//                        //Log.d("pref_data", "ed");//return;;
//                        //Toast.makeText(getApplicationContext(),Prefs.getString(ID_TONTINE_USSD,""),Toast.LENGTH_LONG).show();;
//
//                        //String message_ok = "Transfert effectue pour  "+montant_value+" FCFA a "+MTN_TEST;
//                        //Log.d("MESSAGEOK",message_ok);
//                        //Log.d("MESSAGEOK_Trim",message_ok.replaceAll("\\s",""));
//                        pay_via_ussd(montant_value, reseau);
//                        //verser(montant_value);
//                        /*if (Prefs.getString(ID_TONTINE_USSD,"").equals("")){
//                            verser(montant_value);
//                            dailNumber("400");
//                        }else{
//                            Alerter.create(Carte.this)
//                                    .setTitle("Veuillez patienter la validation en cours.")
//                                    .setIcon(R.drawable.ic_warning)
//                                    .setTitleAppearance(R.style.TextAppearance_AppCompat_Large)
//                                    .setIconColorFilter(R.color.colorPrimaryDark)
//                                    //.setText("Vous pouvez maintenant vous connecter.")
//                                    .setBackgroundColorRes(R.color.colorWhite) // or setBackgroundColorInt(Color.CYAN)
//                                    .show();
//
//                        }*/
//
//                    }
//                }).show();
    }

    private void pay_via_internet(final String montant, final String id_server, final int nbre_versemnt_defaut, final int montCumule){
        heure_transaction_global = System.currentTimeMillis() / 1000L;
        payer(Prefs.getString(TEL_KEY, ""), montant, heure_transaction_global, id_server, nbre_versemnt_defaut, montCumule);

    }

    private void payer(final String numero, final String montant, final long heure_transaction, final String id_server, final int nbre_versemnt_defaut, final int montCumule) {
        RequestQueue queue = Volley.newRequestQueue(this);
        StringRequest postRequest = new StringRequest(Request.Method.POST, Constantes.URL_PAY_TONTINE,
                new Response.Listener<String>() {
                    @SuppressLint("ResourceAsColor")
                    @Override
                    public void onResponse(String response) {
                        Log.e("ResponseTagP", response);
                        if (response.isEmpty()) {
                            progressDialog.dismiss();
                            String msg = "Erreur, le serveur ne répond pas. Patientez un instant puis réessayez, s'il vous plaît.";
                            Intent i = new Intent(Carte.this, Message_non.class);
                            i.putExtra("msg_desc", msg);
                            i.putExtra("id_tontine", id_tontine);
                            i.putExtra("class", "com.sicmagroup.tondi.Carte");
                            startActivity(i);
                        } else {
                            try {
                                JSONObject result = new JSONObject(response);
                                Log.e("La reponse du body:", result.toString());
                                if (result.getInt("responseCode") == 0) {
                                    progressDialog.dismiss();
                                    try {
                                        JSONArray resultat = result.getJSONArray("body");
                                        Long id_final = verser_2(resultat);

                                        if (id_final != null) {
                                            Tontine tontine = SugarRecord.findById(Tontine.class, id_final);

                                            String msg = "Le montant total cumulé des cotisations du carnet N° " + tontine.getCarnet() +
                                                    " est de " + tontine.getMontCumule(tontine.getStatut()) + " F";
                                            Intent i = new Intent(Carte.this, Message_ok.class);
                                            i.putExtra("msg_desc", msg);
                                            i.putExtra("id_tontine", id_final);
                                            i.putExtra("isNewTontine", true);
                                            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                            startActivity(i);
                                        }

                                    } catch (Throwable t) {
                                        Log.d("errorInscription", t.getMessage());
                                    }

                                } else {
                                    progressDialog.dismiss();
                                    String msg = result.getString("body");
                                    Intent i = new Intent(Carte.this, Message_non.class);
                                    i.putExtra("msg_desc", msg);
                                    i.putExtra("id_tontine", id_tontine);
                                    i.putExtra("class", "com.sicmagroup.tondi.Carte");
                                    startActivity(i);
                                }

                            } catch (Throwable t) {
                                Log.d("errorInscription", t.getMessage());
                            }
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {
                        progressDialog.dismiss();
                        ConstraintLayout mainLayout = findViewById(R.id.layout_cartemain);

                        String message;
                        if (volleyError instanceof NetworkError || volleyError instanceof NoConnectionError) {
                            message = "Aucune connexion Internet!";
                        } else if (volleyError instanceof ServerError) {
                            message = "Erreur du serveur!";
                        } else if (volleyError instanceof AuthFailureError) {
                            message = "Erreur d'authentification!";
                        } else if (volleyError instanceof ParseError) {
                            message = "Erreur de parsing des données!";
                        } else if (volleyError instanceof TimeoutError) {
                            message = "Le délai d'attente est écoulé!";
                        } else {
                            message = "Une erreur inconnue est survenue!";
                        }

                        Log.e("VolleyError", volleyError.toString());

                        Snackbar snackbar = Snackbar
                                .make(mainLayout, message, Snackbar.LENGTH_INDEFINITE)
                                .setAction("REESSAYER", new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        payer(numero, montant, heure_transaction, id_server, nbre_versemnt_defaut, montCumule);
                                    }
                                });
                        snackbar.getView().setBackgroundColor(ContextCompat.getColor(Carte.this, R.color.colorGray));
                        snackbar.setActionTextColor(getResources().getColor(R.color.colorPrimaryDark));
                        View sbView = snackbar.getView();
                        TextView textView = sbView.findViewById(com.google.android.material.R.id.snackbar_text);
                        textView.setTextColor(Color.WHITE);
                        snackbar.show();
                    }
                }
        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("customerNumber", numero);
                params.put("amount", montant);
               // params.put("heure_transaction", String.valueOf(heure_transaction));
                params.put("idTontineServeur", String.valueOf(id_server));
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

        postRequest.setRetryPolicy(new DefaultRetryPolicy(
                80000,
                -1,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        queue.add(postRequest);

        progressDialog = new ProgressDialog(Carte.this);
        progressDialog.setCancelable(false);
        progressDialog.setMessage("Veuillez patienter SVP! \n Le versement de la mise est en cours...");
        progressDialog.show();
    }

    private void dailNumber(String code) {
        String ussdCode = "*" + code + Uri.encode("#");
//        if (ContextCompat.checkSelfPermission(getApplicationContext(), CALL_PHONE) == PackageManager.PERMISSION_GRANTED) {
//            startActivity(new Intent("android.intent.action.CALL", Uri.parse("tel:" + ussdCode)));
//        } else {
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
////                requestPermissions(new String[]{CALL_PHONE}, 1);
//            }
//        }

    }

    private Long verser_2(JSONArray resultat) throws JSONException {
        Date currentTime = Calendar.getInstance().getTime();
        long output_creation = currentTime.getTime() / 1000L;
        String str_creation = Long.toString(output_creation);
        long timestamp_creation = Long.parseLong(str_creation) * 1000;
        long output_maj = currentTime.getTime() / 1000L;
        String str_maj = Long.toString(output_maj);
        long timestamp_maj = Long.parseLong(str_maj) * 1000;

        Long id_tont = Long.valueOf(0);
        Long id_vers = Long.valueOf(0);
        boolean billet_id_vers = false;

        String[] actionGroup = {};
        String action = "";
        String object = "";

        Utilisateur u = SugarRecord.find(Utilisateur.class, "id_utilisateur = ? ", Prefs.getString(ID_UTILISATEUR_KEY, "")).get(0);

        for (int i = 0; i < resultat.length(); i++) {
            JSONObject content = new JSONObject(resultat.get(i).toString());
            try {
                actionGroup = content.getString("action").split("#");
            } catch (JSONException e) {
                e.printStackTrace();
            }
            action = actionGroup[0];
            object = actionGroup[1];
            JSONObject data = new JSONObject(content.getJSONObject("data").toString());

            if ("add".equals(action)) {
                Log.i("action_after", String.valueOf(action));
                if (object.equals("tontines")) {
//                    Log.i("obj_after", String.valueOf(object));
                    Tontine nouvelle_tontine = new Tontine();
                    nouvelle_tontine.setId_server(data.getString("id"));
//                    Log.i("new_t_id", String.valueOf(nouvelle_tontine.getId_server()));

                    nouvelle_tontine.setId_utilisateur(Prefs.getString(ID_UTILISATEUR_KEY, null));
//                    Log.i("new_t_idUser", String.valueOf(nouvelle_tontine.getId_utilisateur()));
                    nouvelle_tontine.setDenomination(tontine_main.getDenomination());
                    nouvelle_tontine.setPeriode(data.getString("periode"));
//                    Log.i("new_t_periode", String.valueOf(nouvelle_tontine.getPeriode()));

                    nouvelle_tontine.setMise(data.getInt("mise"));
//                    Log.i("new_t_idMise", String.valueOf(nouvelle_tontine.getMise()));

                    nouvelle_tontine.setPrelevement_auto(data.getBoolean("isAutoPayment"));
//                    Log.i("new_t_idAuto", String.valueOf(nouvelle_tontine.getPrelevement_auto()));

//                    nouvelle_tontine.setIdSim(data.getString("id_sim"));
//                    Log.i("new_t_idSim", String.valueOf(nouvelle_tontine.getIdSim()));

                    nouvelle_tontine.setCarnet(String.valueOf(data.getString("carnet")));
//                    Log.i("new_t_Carnet", String.valueOf(nouvelle_tontine.getCarnet()));

                    nouvelle_tontine.setStatut(data.getString("state"));
//                    Log.i("new_t_Statut", String.valueOf(nouvelle_tontine.getStatut()));

                    if(!data.isNull("unBlockDate")){
                        nouvelle_tontine.setDateDeblocage(data.getString("unBlockDate"));
                    }


                    nouvelle_tontine.setCreation(timestamp_creation);
//                    Log.i("new_t_crea", String.valueOf(nouvelle_tontine.getCreation()));

                    nouvelle_tontine.setMaj(timestamp_maj);
//                    Log.i("new_t_Maj", String.valueOf(nouvelle_tontine.getMaj()));

                    nouvelle_tontine.setContinuer(data.getLong("carnet"));

                    nouvelle_tontine.save();
                    id_tont = nouvelle_tontine.getId();

                    if (nouvelle_tontine.getPrelevement_auto()) {
                        // ajouter cotisations automatiques
                        Cotis_Auto cotis_auto = new Cotis_Auto();
                        cotis_auto.setTontine(nouvelle_tontine);
                        cotis_auto.setUtilisateur(u);
                        // maj des dates
                        cotis_auto.setCreation(timestamp_creation);
                        cotis_auto.setMaj(timestamp_creation);
                        cotis_auto.save();
                    }

                } else if (object.equals("versements")) {
//                    Log.i("versmt", "vermtn");
                    Versement n_versement = new Versement();
                    n_versement.setFractionne(data.getString("isFractioned"));
//                    Log.e("new_V_frac", String.valueOf(data.getString("fractionne")));

                    if (id_vers == 0)
                        n_versement.setIdVersement(data.getString("idVersement"));
                    else
                        n_versement.setIdVersement(String.valueOf(id_vers));

                    n_versement.setMontant(data.getString("amount"));
//                    Log.e("new_V_Montan", String.valueOf(data.getString("montant")));
                    Log.d("versement id", data.getString("id")) ;
                    n_versement.setIdVersServ(data.getString("id"));
                    n_versement.setCreation(timestamp_creation);
                    n_versement.setMaj(timestamp_maj);
                    n_versement.setUtilisateur(u);
//
                    List<Tontine> cible = Tontine.find(Tontine.class, "id_server = ?", id_tont == 0 ? tontine_main.getId_server() :  id_tont+"");
                    if (cible.size() > 0) {
                        n_versement.setTontine(cible.get(0));
//                        Log.e("cible_id_server", String.valueOf(cible.get(0).getId_server()));
                        id_tont = cible.get(0).getId();
                    }
                    n_versement.save();
                    if (!billet_id_vers) {
                        id_vers = n_versement.getId();
                        billet_id_vers = true;
                    }

//                    Log.e("cible_size", String.valueOf(cible.size()));

                }
            } else if (action.equals("update")) {
//                Log.i("update", String.valueOf(action));
                if (object.equals("tontines")) {
                    List<Tontine> old = Tontine.find(Tontine.class, "id_server = ?", content.getString("id"));
//                    Log.e("old_t_size", String.valueOf(old.size()));
                    if (old.size() > 0) {
                        if(data.has("state")) {
                            old.get(0).setStatut(data.getString("state"));
//                            Log.e("old_t_stat", data.getString("statut"));
                        }
                        if(data.has("carnet")) {
                            old.get(0).setCarnet(data.getString("carnet"));
                            old.get(0).setContinuer(data.getLong("carnet"));
//                            Log.e("old_t_carn", data.getString("carnet"));
                        }
                        old.get(0).setMaj(timestamp_maj);
                        old.get(0).save();
                        id_tont = old.get(0).getId();
                    }
                }
            }
        }
//        finish();
//        Intent intent = getIntent();
//        startActivity(intent);

        return id_tont;
    }



    private PermissionService.Callback callback = new PermissionService.Callback() {
        @Override
        public void onRefuse(ArrayList<String> RefusePermissions) {
            Toast.makeText(Carte.this,
                    getString(R.string.refuse_permissions),
                    Toast.LENGTH_SHORT).show();
            Carte.this.finish();
        }

        @Override
        public void onFinally() {
            // pass
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (!Settings.canDrawOverlays(Carte.this)) {
                    Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                            Uri.parse("package:" + Carte.this.getPackageName()));
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

    @Override
    protected void onStart() {
        // TODO Auto-generated method stub
        super.onStart();
        if (onStartCount > 1) {
            /*if (nav.equals("prev")){
                this.overridePendingTransition(R.anim.anim_slide_in_right,
                        R.anim.anim_slide_out_right);
            }else{
                this.overridePendingTransition(R.anim.anim_slide_in_left,
                        R.anim.anim_slide_out_left);
            }*/
            if (nav.equals("prev")){
                this.overridePendingTransition(R.anim.anim_slide_in_right,
                        R.anim.anim_slide_out_right);
            }else{
                this.overridePendingTransition(R.anim.anim_slide_in_left,
                        R.anim.anim_slide_out_left);
            }

        } else if (onStartCount == 1) {
            onStartCount++;
        }

    }
}
