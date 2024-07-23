package com.sicmagroup.tondi;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.IntentSender;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.android.volley.toolbox.JsonObjectRequest;
import com.google.android.gms.auth.api.phone.SmsRetriever;
import com.google.android.gms.auth.api.phone.SmsRetrieverClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.widget.Button;
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
//import com.crashlytics.android.Crashlytics;
import com.google.android.play.core.appupdate.AppUpdateInfo;
import com.google.android.play.core.appupdate.AppUpdateManager;
import com.google.android.play.core.appupdate.AppUpdateManagerFactory;
import com.google.android.play.core.install.model.AppUpdateType;
import com.google.android.play.core.install.model.UpdateAvailability;
import com.google.android.play.core.tasks.OnSuccessListener;
import com.google.firebase.crashlytics.FirebaseCrashlytics;

import com.google.firebase.messaging.FirebaseMessaging;
import com.orm.SugarRecord;
import com.orm.query.Condition;
import com.orm.query.Select;
import com.pixplicity.easyprefs.library.Prefs;
import com.romellfudi.permission.PermissionService;
import com.sicmagroup.formmaster.model.FormElementTextPhone;
import com.sicmagroup.formmaster.model.FormElementZero;
import com.sicmagroup.tondi.Enum.OperationTypeEnum;
import com.sicmagroup.tondi.utils.Constantes;
import com.sicmagroup.ussdlibra.USSDController;
import com.sicmagroup.ussdlibra.USSDService;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.sicmagroup.formmaster.FormBuilder;
import com.sicmagroup.formmaster.model.BaseFormElement;
import com.sicmagroup.formmaster.model.FormElementTextPassword;

//import io.fabric.sdk.android.Fabric;

import static com.sicmagroup.tondi.Accueil.MOOV_DATA_SHARING;
import static com.sicmagroup.tondi.utils.Constantes.CODE_MARCHAND_KEY;
import static com.sicmagroup.tondi.utils.Constantes.REFRESH_TOKEN;
import static com.sicmagroup.tondi.utils.Constantes.SERVEUR;
import static com.sicmagroup.tondi.utils.Constantes.STATUT_UTILISATEUR;
import static com.sicmagroup.tondi.utils.Constantes.TOKEN;

public class Connexion extends AppCompatActivity {
    private static final String TAG = "service_";
    TextView textView;
    private RecyclerView mRecyclerView;
    private FormBuilder mFormBuilder;
    private static final int TAG_TEL = 21;
    private static final int TAG_PASS = 22;

    String url_login = SERVEUR+"/api/v1/utilisateurs/login";

    String user_existe = SERVEUR + "/api/v1/utilisateurs/connecter";
    String user_credential_check = SERVEUR + "/api/v1/utilisateurs/check_password";
    String medias_url = SERVEUR + "/medias/";
    Utilitaire utilitaire;
    ProgressDialog progressDialog;

    private HashMap<String, HashSet<String>> map;
    private USSDController ussdApi;

    static String url_sms = "http://oceanicsms.com/api/http/sendmsg.php";
    static String numeroExpediteurSms = "TONDi";
    static String apiSmsCode = "7293";
    static String utilisateurSms = "sicmaetassocies";
    static String url_get_code_otp = SERVEUR + "/api/v1/codesotp/generer_code";
    static String url_verify_code_otp = SERVEUR + "/api/v1/codesotp/verifier_code";
    static String url_disable_code_otp = SERVEUR + "/api/v1/codesotp/disable_codeotp";
    static String url_generate_otp_insc = SERVEUR + "/api/v1/codesotp/generer_codeotp_insc";
    static String url_verify_code_otp_insc = SERVEUR + "/api/v1/codesotp/verifier_codeotp_insc";

//    static String url_get_nom_prenom_from_qos = SERVEUR + "/api/v1/user/get_customer_from_qos";
    public static String url_save_plainte = SERVEUR + "/api/v1/plaintes/save_plainte";
    static String url_desactiver_account = SERVEUR + "/api/v1/utilisateurs/desactivate_customer";
    static String url_get_code_otp_verif = SERVEUR + "/api/v1/codesotp/generer_codeotp_access";

    static String url_get_code_otp_verif_access = SERVEUR + "/api/v1/codesotp/verifier_codeotp_access";

    public static String ID_UTILISATEUR_KEY = "id_utilisateur";
    public static String NOM_KEY = "nom";
    public static String PRENOMS_KEY = "prenoms";
    static String PIN_KEY = "pin";
    static String PASS_KEY = "pass";
    static String PHOTO_KEY = "photo";
    static String PHOTO_CNI_KEY = "cni_photo";
    public static String NUMERO_COMPTE_KEY = "numero_compte";
    static String CONNECTER_KEY = "connecter_le";
    public static String TEL_KEY = "telephone";
    static String SEXE_KEY = "sexe";
    static String ACCESS_RETURNf_KEY = "accessibity_return";
    static String ACCESS_BOOL = "accessibity_bool";
//    static String ACCESS_NOUVELLE_TONTINE = "nouvelle_tontine_access";
    static String SPLASH_LOADING = "splash_loading";
    //static final String ID_TONTINE_USSD = "id_tontine";
    //static final String MONTANT_VERSE = "mt_verse";
    static String MTN_MECOTI = "62023231";
    static String MTN_TEST = "62023231";
    //static String MTN_MECOTI = "22996665166";
    //static String MTN_TEST = "96665166";
    static String MOOV_TEST = "95816309";
    static String FIREBASE_TOKEN = "firebase_token";
    static String CODE_OTP_RECU = "codeOtp_recu";
    //nombre de fois que les 3 tentatives de validation de pin échoue
    static String NMBR_PWD_FAILED = "nmbr_pwd_failed";
    // nombre d'echec de validation de pin : à 3 on bloque les champs et tous les bouttons pendant 30sec
    static String NMBR_PWD_TENTATIVE_FAILED = "nmbr_pwd_tentative_failed";

    static String TOKEN_OPERATION_ENCOURS = "token_op_encours";
    static String NUMERO_MARCHAND_OP_ENCOURS = "num_marchand_op_encours";
    static String TEL_CLIENT_OP_ENCOURS = "tel_client_op_encours";
    static String MONTANT_OP_ENCOURS = "montant_op_encours";
    static String ID_TONTINE_OP_ENCOURS = "id_tontine_op_encours";

    static String NOM_INSC_ENCOURS = "nom_insc_encours";
    static String TEL_INSC_ENCOURS = "tel_insc_encours";
    static String PRENOM_INSC_ENCOURS = "prenom_insc_encours";
    static String MDP_INSC_ENCOURS = "mdp_insc_encours";
    static String SEXE_INSC_ENCOURS = "sexe_insc_encours";


    /**
     * A trois état :
     * inconnu : Aucun message du 124 encore reçu
     * oui : Le numero est bien dans le telephone
     * non : Le nnumero n'est pas dans le telephone
     * */
    static String NUMERO_VERIFYED = "inconnu";
    static String numero_saisit_insc = "";


    FormElementZero element0 = FormElementZero.createInstance().setTag(99);

    FormElementTextPhone element2 = FormElementTextPhone.createInstance().setTag(TAG_TEL).setTitle("Téléphone").setHint("Votre numéro MOOV").setRequired(true);

    FormElementTextPassword element1 = FormElementTextPassword.createInstance().setTag(TAG_PASS).setTitle("Mot de passe").setHint("Mot de passe").setRequired(true);

    TextView mdp_forget;

    private AppUpdateManager appUpdateManager;
    private static final int RC_APP_UPDATE = 100;
    private Boolean isFromCarteTontine = false;
    private int idTontine = 0;
    private  Sim sim_par_defaut_general = new Sim();

    @Override
    protected void onPause() {
        super.onPause();
        Log.d("onPause","back");
        //Prefs.putBoolean(ACCESS_BOOL,false);

    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d("onResume","fore");
        Prefs.putBoolean(ACCESS_BOOL,true);
        appUpdateManager.getAppUpdateInfo().addOnSuccessListener(new OnSuccessListener<AppUpdateInfo>() {
            @Override
            public void onSuccess(AppUpdateInfo result) {
                if (result.updateAvailability() == UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS){
                    try {
                        appUpdateManager.startUpdateFlowForResult(result, AppUpdateType.IMMEDIATE, Connexion.this, RC_APP_UPDATE);
                    } catch (IntentSender.SendIntentException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
//        if (!isAccessibilitySettingsOn(getApplicationContext())) {
//
//            android.app.AlertDialog.Builder alertDialogBuilder = new android.app.AlertDialog.Builder(this);
//            alertDialogBuilder.setTitle("Permission d'accessibilité");
//            ApplicationInfo applicationInfo = getApplicationInfo();
//            int stringId = applicationInfo.labelRes;
//            String name = applicationInfo.labelRes == 0 ?
//                    applicationInfo.nonLocalizedLabel.toString() : getString(stringId);
//            alertDialogBuilder
//                    .setMessage("Tondi a besoin d'exécuter les codes USSD pour les paiements de vos mises en mode déconnecté de l'internet.");
//            alertDialogBuilder.setCancelable(false);
//            alertDialogBuilder.setNeutralButton("Continuer pour activer", new DialogInterface.OnClickListener() {
//                public void onClick(DialogInterface dialog, int id) {
//                    Connexion.this.finish();
//                    Intent intent = new Intent();
//                    intent.setClassName("com.android.settings",
//                            "com.android.settings.Settings");
//                    intent.setAction(Intent.ACTION_MAIN);
//                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK
//                            | Intent.FLAG_ACTIVITY_CLEAR_TASK
//                            | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
//                    intent.putExtra(PreferenceActivity.EXTRA_SHOW_FRAGMENT,
//                            "com.android.settings.accessibility.AccessibilitySettings");
//                    intent.putExtra(PreferenceActivity.EXTRA_SHOW_FRAGMENT_ARGUMENTS,
//                            "com.android.settings.accessibility.AccessibilitySettings");
//                    startActivityForResult(intent, 1);
//                    startActivityForResult(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS), 1);
//                }
//            });
//            android.app.AlertDialog alertDialog = alertDialogBuilder.create();
//            if (alertDialog != null) {
//                alertDialog.show();
//            }
//            //startActivity(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS));
//        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d("onStop","back");
        Prefs.putBoolean(ACCESS_BOOL,false);
        //Toast.makeText(Connexion.this,"Home buton pressed",Toast.LENGTH_LONG).show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d("onDestroy","back");
        Prefs.putBoolean(ACCESS_BOOL,false);

    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        Fabric.with(this, new Crashlytics());
        FirebaseCrashlytics crashlytics = FirebaseCrashlytics.getInstance();
        crashlytics.sendUnsentReports();
        utilitaire= new Utilitaire(this);
        new Prefs.Builder()
                .setContext(this)
                .setMode(ContextWrapper.MODE_PRIVATE)
                .setPrefsName(getPackageName())
                .setUseDefaultSharedPreference(true)
                .build();
        if (Prefs.getString(ID_UTILISATEUR_KEY,null) != null){
            startActivity(new Intent(Connexion.this, Home.class));
        }
        // classe de retour après activation des accessibiltés
        Prefs.putString(ACCESS_RETURNf_KEY,"com.sicmagroup.tondi.Connexion");
        Prefs.putBoolean(ACCESS_BOOL,true);
        setContentView(R.layout.activity_connexion);
        map = new HashMap<>();
        map.put("KEY_LOGIN", new HashSet<>(Arrays.asList("waiting", "loading")));
        map.put("KEY_ERROR", new HashSet<>(Arrays.asList("problem", "error")));
//        new PermissionService(this).request(
//                new String[]{/*Manifest.permission.CALL_PHONE,*/ /*Manifest.permission.READ_PHONE_STATE,*/ Manifest.permission.READ_SMS , Manifest.permission.RECEIVE_SMS},
//                callback);
        Log.d("DEKSP", "dekpe");
        /**
         * Upodate automaticaly and IMMEDIAT
         */

        appUpdateManager = AppUpdateManagerFactory.create(this);
        appUpdateManager.getAppUpdateInfo().addOnSuccessListener(new OnSuccessListener<AppUpdateInfo>() {
            @Override
            public void onSuccess(AppUpdateInfo result) {
                if (result.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE
                        && result.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE)){
                    try {
                        appUpdateManager.startUpdateFlowForResult(result, AppUpdateType.IMMEDIATE, Connexion.this, RC_APP_UPDATE);
                    } catch (IntentSender.SendIntentException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

//        if (!isAccessibilitySettingsOn(getApplicationContext())) {
//
//            android.app.AlertDialog.Builder alertDialogBuilder = new android.app.AlertDialog.Builder(this);
//            alertDialogBuilder.setTitle("Permission d'accessibilité");
//            ApplicationInfo applicationInfo = getApplicationInfo();
//            int stringId = applicationInfo.labelRes;
//            String name = applicationInfo.labelRes == 0 ?
//                    applicationInfo.nonLocalizedLabel.toString() : getString(stringId);
//            alertDialogBuilder
//                    .setMessage("Tondi a besoin d'exécuter les codes USSD pour les paiements de vos mises en mode déconnecté de l'internet.");
//            alertDialogBuilder.setCancelable(false);
//            alertDialogBuilder.setNeutralButton("Continuer pour activer", new DialogInterface.OnClickListener() {
//                public void onClick(DialogInterface dialog, int id) {
//                    Connexion.this.finish();
//                    Intent intent = new Intent();
//                    intent.setClassName("com.android.settings",
//                            "com.android.settings.Settings");
//                    intent.setAction(Intent.ACTION_MAIN);
//                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK
//                            | Intent.FLAG_ACTIVITY_CLEAR_TASK
//                            | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
//                    intent.putExtra(PreferenceActivity.EXTRA_SHOW_FRAGMENT,
//                            "com.android.settings.accessibility.AccessibilitySettings");
//                    intent.putExtra(PreferenceActivity.EXTRA_SHOW_FRAGMENT_ARGUMENTS,
//                            "com.android.settings.accessibility.AccessibilitySettings");
//                    startActivityForResult(intent, 1);
//                    startActivityForResult(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS), 1);
//                }
//            });
//            android.app.AlertDialog alertDialog = alertDialogBuilder.create();
//            if (alertDialog != null) {
//                alertDialog.show();
//            }
//            //startActivity(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS));
//        }
        if (getIntent().getExtras()!=null )
        {
            Intent activityIntent = getIntent();
            mRecyclerView = (RecyclerView) findViewById(R.id.recyclerView);
//            SpacingItemDecorator itemDecorator = new SpacingItemDecorator(40);
//            mRecyclerView.addItemDecoration(itemDecorator);
            mFormBuilder = new FormBuilder(this, mRecyclerView);
            List<BaseFormElement> formItems = new ArrayList<>();
            formItems.add(element0);
            formItems.add(element2);
            formItems.add(element1);
            //element0.setValue("");
            element1.setValue(activityIntent.getStringExtra("mdp"));
            element2.setValue(activityIntent.getStringExtra("tel"));

            mFormBuilder.addFormElements(formItems);
            if(activityIntent.hasExtra("id_tontine")){
                isFromCarteTontine = true;
                idTontine = activityIntent.getIntExtra("id_tontine", 0);
            }
        }else{
            setupForm();
        }

        TextView lien_inscription = (TextView)findViewById(R.id.lien_inscription);
        lien_inscription.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                //Connexion.this.finish();
                startActivity(new Intent(Connexion.this,Inscription.class));
                /*String phoneNumber="*124#".trim();;

                ussdApi = USSDController.getInstance(Connexion.this);
                //result.setText("");
                ussdApi.callUSSDInvoke(phoneNumber, map, new USSDController.CallbackInvoke() {
                    @Override
                    public void responseInvoke(String message) {
                        Log.d("APPUSSpp_ENcours", message);
                    }

                    @SuppressLint("ResourceAsColor")
                    @Override
                    public void over(String message) {
                        Log.d("APPUSSpp", message);
                        // traiter message quand fini OK
                        startActivity(new Intent(Connexion.this,Inscription.class));
                    }
                });*/
                //throw new RuntimeException("This is a crash");

            }
        });

        Button btn_connexion = (Button)findViewById(R.id.btn_connexion);
        btn_connexion.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("ResourceAsColor")
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                if (!mFormBuilder.isValidForm()){
                    if (getIntent().getExtras()!=null){

                        Toast.makeText(Connexion.this, "t:"+element1.getValue()+"//"+"t:"+element2.getValue(), Toast.LENGTH_LONG).show();
                    }else{
                        String msg = "> Veuillez remplir les champs SVP! ";
                        alertView("Erreurs dans le formulaire",msg);
                    }
                }else{
                    BaseFormElement tel = mFormBuilder.getFormElement(TAG_TEL);
                    String tel_value = tel.getValue();
                    Utilitaire utilitaire = new Utilitaire(Connexion.this);
                    auth();
                }
            }
        });
        //startService(new Intent(this, AutoVersement.class));
        final ConstraintLayout mainLayout =  findViewById(R.id.layout_connexion);
        mdp_forget = (TextView) findViewById(R.id.mdp_forget);
        mdp_forget.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Snackbar.make(mainLayout, "Contactez le XX XX XX XX puis suivez les instructions.", Snackbar.LENGTH_LONG).show();
            }
        });

    }

    private void setupForm() {
        mRecyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        mFormBuilder = new FormBuilder(this, mRecyclerView);
        List<BaseFormElement> formItems = new ArrayList<>();
        formItems.add(element0);
        formItems.add(element2);
        formItems.add(element1);

        mFormBuilder.addFormElements(formItems);
    }

    @SuppressLint("ResourceAsColor")
    private void auth() {
        BaseFormElement tel = mFormBuilder.getFormElement(TAG_TEL);
        BaseFormElement mdp = mFormBuilder.getFormElement(TAG_PASS);
        String tel_value = tel.getValue();
        String mdp_value = mdp.getValue();
        Connexion.AeSimpleSHA1 AeSimpleSHA1 = new Connexion.AeSimpleSHA1();

        try {
            mdp_value = AeSimpleSHA1.md5(mdp_value);
            mdp_value = AeSimpleSHA1.SHA1(mdp_value);

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        Utilisateur utilisateur = new Utilisateur();

        auth_en_ligne();
    }


    private void alertView( String title ,String message ) {
        Dialog dialog_alert = new Dialog(Connexion.this);
        dialog_alert.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog_alert.setCancelable(true);
        dialog_alert.setContentView(R.layout.dialog_attention);

        TextView titre = (TextView) dialog_alert.findViewById(R.id.deco_title);
        titre.setText(title);
        TextView message_deco = (TextView) dialog_alert.findViewById(R.id.deco_message);
        message_deco.setText(message);



        Button oui = (Button) dialog_alert.findViewById(R.id.btn_oui);
        oui.setText("OK");
        oui.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog_alert.cancel();
            }
        });

        Button non = (Button) dialog_alert.findViewById(R.id.btn_non);
        non.setVisibility(View.GONE);
        dialog_alert.show();

    }

    public static boolean isTimeAutomatic(Context c) {
        return Settings.Global.getInt(c.getContentResolver(), Settings.Global.AUTO_TIME, 0) == 1;
    }

    public static class AeSimpleSHA1 {
        private String convertToHex(byte[] data) {
            StringBuilder buf = new StringBuilder();
            for (byte b : data) {
                int halfbyte = (b >>> 4) & 0x0F;
                int two_halfs = 0;
                do {
                    buf.append((0 <= halfbyte) && (halfbyte <= 9) ? (char) ('0' + halfbyte) : (char) ('a' + (halfbyte - 10)));
                    halfbyte = b & 0x0F;
                } while (two_halfs++ < 1);
            }
            return buf.toString();
        }

        public  String SHA1(String text) throws NoSuchAlgorithmException, UnsupportedEncodingException {
            MessageDigest md = MessageDigest.getInstance("SHA-1");
            byte[] textBytes = text.getBytes("iso-8859-1");
            md.update(textBytes, 0, textBytes.length);
            //md.update(text.getBytes("UTF-8"));
            byte[] sha1hash = md.digest();
            return convertToHex(sha1hash);
        }

        public String md5(String string) {
            byte[] hash;

            try {
                hash = MessageDigest.getInstance("MD5").digest(string.getBytes("UTF-8"));
            } catch (NoSuchAlgorithmException e) {
                throw new RuntimeException("Huh, MD5 should be supported?", e);
            } catch (UnsupportedEncodingException e) {
                throw new RuntimeException("Huh, UTF-8 should be supported?", e);
            }

            StringBuilder hex = new StringBuilder(hash.length * 2);

            for (byte b : hash) {
                int i = (b & 0xFF);
                if (i < 0x10) hex.append('0');
                hex.append(Integer.toHexString(i));
            }

            return hex.toString();
        }
    }

    @SuppressLint("LongLogTag")
    private void auth_en_ligne() {
        RequestQueue queue = Volley.newRequestQueue(this);
        Log.e("C'est dans la fonction auth en ligne", "Eh oui");

        JSONObject jsonBody = new JSONObject();


        BaseFormElement tel = mFormBuilder.getFormElement(TAG_TEL);
        BaseFormElement mdp = mFormBuilder.getFormElement(TAG_PASS);

        String tel_value = tel.getValue();
        String mdp_value = mdp.getValue();

        try {

            jsonBody.put("numero", tel_value);
            jsonBody.put("password", mdp_value);

            Log.e("Le body de la connexion", jsonBody.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        JsonObjectRequest postRequest = new JsonObjectRequest(Request.Method.POST, Constantes.URL_LOGIN, jsonBody,
                new Response.Listener<JSONObject>() {
                    @SuppressLint({"ResourceAsColor", "LongLogTag", "SuspiciousIndentation"})
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d("Response", response.toString());
                        Connexion.AeSimpleSHA1 AeSimpleSHA1 = new Connexion.AeSimpleSHA1();
                        try {
                            JSONObject result = response;
                            Log.e("Le résultat est de auth en ligne :", result.toString());
                            //int responseCode = result.getInt("responseCode");
                            if (result != null) {
                                final JSONObject user = result.getJSONObject("userDTO");
                                Long id = Long.valueOf(0);
                                Utilisateur nouvel_utilisateur = null;
                                @SuppressLint("SimpleDateFormat") DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                                Date creation = formatter.parse(user.getString("createdAt"));
                                long output_creation = creation.getTime() / 1000L;
                                String str_creation = Long.toString(output_creation);
                                long timestamp_creation = Long.parseLong(str_creation) * 1000;
                                Date maj = formatter.parse(user.getString("updatedAt"));
                                long output_maj = maj.getTime() / 1000L;
                                String str_maj = Long.toString(output_maj);
                                long timestamp_maj = Long.parseLong(str_maj) * 1000;
                                String mot_de_passe = mdp_value;
                                mot_de_passe = AeSimpleSHA1.md5(mot_de_passe);
                                mot_de_passe = AeSimpleSHA1.SHA1(mot_de_passe);
                                // enregistrer en local
                                List<Utilisateur> userH = SugarRecord.find(Utilisateur.class, "numero = ?", tel_value);
                                if (userH.size() == 0) {
                                    nouvel_utilisateur = new Utilisateur();
                                    nouvel_utilisateur.setId_utilisateur(user.getString("id"));
                                    nouvel_utilisateur.setNumero(tel_value);
//                                    if (user.getString("firstName").isEmpty() || user.has("firstName")) {
//                                        nouvel_utilisateur.setNom(user.getString("firstName"));
//                                    }else{
//                                        nouvel_utilisateur.setNom("");
//                                    }
//                                    if (user.getString("lastName").isEmpty() || user.has("lastName")) {
//                                        nouvel_utilisateur.setPrenoms(user.getString("lastName"));
//                                    }else{
//                                        nouvel_utilisateur.setPrenoms("");
//                                    }
//                                    if (user.getString("profilePicture").isEmpty() || user.has("profilePicture")) {
//                                        nouvel_utilisateur.setPhoto_identite(user.getString("profilePicture"));
//                                    }else{
//                                        nouvel_utilisateur.setPhoto_identite("");
//                                    }
//                                    if (user.getString("cniPicture").isEmpty() || user.has("cniPicture")) {
//                                        nouvel_utilisateur.setcni_photo(user.getString("cniPicture"));
//                                    }else{
//                                        nouvel_utilisateur.setcni_photo("");
//                                    }

//                                    nouvel_utilisateur.setPhoto_identite(user.getString("profilePicture"));
//                                    nouvel_utilisateur.setcni_photo(user.getString("cniPicture"));
                                    nouvel_utilisateur.setMdp(mot_de_passe);
                                    nouvel_utilisateur.setStatut("true");
                                    nouvel_utilisateur.setNumero_compte(user.getString("accountNumber"));
                                    if (user.isNull("referent"))
                                        nouvel_utilisateur.setCodeMarchand("");
                                    else
                                        nouvel_utilisateur.setCodeMarchand(user.getString("referent"));

                                    nouvel_utilisateur.setCreation(timestamp_creation);
                                    nouvel_utilisateur.setId_utilisateur(user.getString("id"));
                                    nouvel_utilisateur.setMaj(timestamp_maj);
                                    nouvel_utilisateur.setConnecter_le(timestamp_maj);
                                    nouvel_utilisateur.save();
                                    id = Long.parseLong(nouvel_utilisateur.getId_utilisateur());
                                } else {
                                    Log.e("C'est dans le else","Size == 0 là");
                                    nouvel_utilisateur = userH.get(0);
                                    nouvel_utilisateur.setId_utilisateur(user.getString("id"));
//                                    nouvel_utilisateur.setNumero(tel_value);
//                                    nouvel_utilisateur.setNom(user.getString("firstName"));
//                                    nouvel_utilisateur.setPrenoms(user.getString("lastName"));
//                                    nouvel_utilisateur.setPhoto_identite(user.getString("profilePicture"));
//                                    nouvel_utilisateur.setcni_photo(user.getString("cniPicture"));
//                                    nouvel_utilisateur.setMdp(mot_de_passe);
                                    nouvel_utilisateur.setNumero(tel_value);
//                                    if (user.getString("firstName").isEmpty() || user.has("firstName")) {
//                                        nouvel_utilisateur.setNom(user.getString("firstName"));
//                                    }else{
//                                        nouvel_utilisateur.setNom("");
//                                    }
//                                    if (user.getString("lastName").isEmpty() || user.has("lastName")) {
//                                        nouvel_utilisateur.setPrenoms(user.getString("lastName"));
//                                    }else{
//                                        nouvel_utilisateur.setPrenoms("");
//                                    }
//                                    if (user.getString("profilePicture").isEmpty() || user.has("profilePicture")) {
//                                        nouvel_utilisateur.setPhoto_identite(user.getString("profilePicture"));
//                                    }else{
//                                        nouvel_utilisateur.setPhoto_identite("");
//                                    }
//                                    if (user.getString("cniPicture").isEmpty() || user.has("cniPicture")) {
//                                        nouvel_utilisateur.setcni_photo(user.getString("cniPicture"));
//                                    }else{
//                                        nouvel_utilisateur.setcni_photo("");
//                                    }
//
//                                    nouvel_utilisateur.setStatut(user.getString("statut"));
//                                    nouvel_utilisateur.setNumero_compte(user.getString("accountNumber"));
                                    if (result.isNull("referent"))
                                        nouvel_utilisateur.setCodeMarchand("");
                                    else
                                        nouvel_utilisateur.setCodeMarchand(result.getString("referent"));
                                    nouvel_utilisateur.setMaj(timestamp_maj);
                                    nouvel_utilisateur.save();
                                    id = Long.parseLong(nouvel_utilisateur.getId_utilisateur());
                                }

                                if (id != null) {
                                    Prefs.putString(ID_UTILISATEUR_KEY, String.valueOf(id));
                                    Prefs.putString(NOM_KEY, "");
                                    Prefs.putString(PRENOMS_KEY, "");
                                    Prefs.putString(PASS_KEY, mot_de_passe);
                                    Prefs.putBoolean(MOOV_DATA_SHARING, true);
                                    Prefs.putString(STATUT_UTILISATEUR, "true");
                                    Prefs.putString(PHOTO_CNI_KEY, user.getString("cniPicture"));
                                    Prefs.putString(SEXE_KEY, "");
                                    Prefs.putString(NUMERO_COMPTE_KEY, user.getString("accountNumber"));
                                    Prefs.putString(TOKEN, result.getString("accessToken"));
                                    Prefs.putString(REFRESH_TOKEN, result.getString("refreshToken"));
                                    if (user.isNull("referent"))
                                        Prefs.putString(CODE_MARCHAND_KEY, "");
                                    else
                                        Prefs.putString(CODE_MARCHAND_KEY, user.getString("referent"));

                                    Date currentTime = Calendar.getInstance().getTime();
                                    long output_current = currentTime.getTime() / 1000L;
                                    String str_current = Long.toString(output_current);
                                    long timestamp_current = Long.parseLong(str_current) * 1000;
                                    Prefs.putString(CONNECTER_KEY, String.valueOf(timestamp_current));
                                    Prefs.putString(PHOTO_KEY, user.getString("profilePicture"));

                                    URL url_photo = new URL(Constantes.URL_MEDIA_PP + user.getString("profilePicture"));
                                    URL url_photo_cni = new URL(Constantes.URL_MEDIA_CNI + Prefs.getString(PHOTO_CNI_KEY, null));
                                    Prefs.putString(TEL_KEY, String.valueOf(tel_value));

                                    // Traiter les tontines et historiques
                                    // ...
                                    JSONArray tontines = user.getJSONArray("tontines");
                                    if(tontines.length()>0){
                                        Log.d("tontines_trouve","oui:"+tontines.length());
                                        List<Tontine> existeTontines = null;
                                        List<Versement> existeVersement = null;
                                        List<Retrait> existeRetrait = null;

                                        for (int i=0;i<tontines.length();i++){
                                            Tontine nouvelle_tontine = new Tontine();

                                            existeTontines = SugarRecord.find(Tontine.class, "id_server = ?", tontines.getJSONObject(i).getString("id"));
                                            if(existeTontines.size() == 0){
                                                //nouvelle_tontine.setId(Long.valueOf(tontines.getJSONObject(i).getString("id")));
                                                nouvelle_tontine.setId_utilisateur(Prefs.getString(ID_UTILISATEUR_KEY,null));
                                                nouvelle_tontine.setPeriode(tontines.getJSONObject(i).getString("periode"));
                                                nouvelle_tontine.setMise(Integer.parseInt(tontines.getJSONObject(i).getString("mise")));
                                                nouvelle_tontine.setPrelevement_auto(tontines.getJSONObject(i).getBoolean("isAutoPayment"));
//                                                nouvelle_tontine.setIdSim(String.valueOf(sim_par_defaut.getId()));
                                                nouvelle_tontine.setCarnet(tontines.getJSONObject(i).getString("carnet"));
                                                nouvelle_tontine.setContinuer(Long.valueOf(tontines.getJSONObject(i).getString("carnet")));
                                                nouvelle_tontine.setStatut(tontines.getJSONObject(i).getString("state"));
                                                nouvelle_tontine.setId_server(tontines.getJSONObject(i).getString("id"));
                                                if(!tontines.getJSONObject(i).isNull("unBlockDate")){
                                                    nouvelle_tontine.setDateDeblocage(tontines.getJSONObject(i).getString("unBlockDate"));
                                                }
                                                if(tontines.getJSONObject(i).getString("denomination") == "")
                                                    nouvelle_tontine.setDenomination("Ma tontine "+nouvelle_tontine.getCarnet());
                                                else
                                                    nouvelle_tontine.setDenomination(tontines.getJSONObject(i).getString("denomination"));
                                                // maj des dates
                                                @SuppressLint("SimpleDateFormat") DateFormat formatter_1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                                                Date creation_1 = formatter_1.parse(tontines.getJSONObject(i).getString("createdAt"));
                                                long output_creation_1=creation_1.getTime()/1000L;
                                                String str_creation_1=Long.toString(output_creation_1);
                                                long timestamp_creation_1 = Long.parseLong(str_creation_1) * 1000;
                                                nouvelle_tontine.setCreation(timestamp_creation_1);
                                                Date creation_2 = formatter_1.parse(tontines.getJSONObject(i).getString("updatedAt"));
                                                long output_creation_2=creation_2.getTime()/1000L;
                                                String str_creation_2=Long.toString(output_creation_2);
                                                long timestamp_maj_1 = Long.parseLong(str_creation_2) * 1000;
                                                nouvelle_tontine.setMaj(timestamp_maj_1);
                                                Log.e("connexion", "5connexion trying");

                                                nouvelle_tontine.save();
                                            }
                                            else {
                                                nouvelle_tontine = existeTontines.get(0);
                                                nouvelle_tontine.setContinuer(Long.valueOf(tontines.getJSONObject(i).getString("carnet")));
                                                nouvelle_tontine.setStatut(tontines.getJSONObject(i).getString("state"));
                                                @SuppressLint("SimpleDateFormat") DateFormat formatter_1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                                                Date creation_2 = formatter_1.parse(tontines.getJSONObject(i).getString("updatedAt"));
                                                long output_creation_2=creation_2.getTime()/1000L;
                                                String str_creation_2=Long.toString(output_creation_2);
                                                long timestamp_maj_1 = Long.parseLong(str_creation_2) * 1000;
                                                nouvelle_tontine.setMaj(timestamp_maj_1);
                                                Log.e("connexion", "5connexion trying");

                                                nouvelle_tontine.save();
                                            }

                                            // enrgeistrer les versements en local

                                            JSONArray versements = tontines.getJSONObject(i).getJSONArray("payments");
                                            Log.d("versements_trouve","oui:"+versements.length());
                                            Log.d("versement", versements.toString());
                                            if(versements.length()>0) {

                                                Log.d("versements_trouve","oui:"+versements.length()+" carnet: "+nouvelle_tontine.getCarnet());
                                                for (int j=0;j<versements.length();j++){
                                                    Versement versement = new Versement();
                                                    existeVersement = SugarRecord.find(Versement.class, "id_vers_serv = ?", versements.getJSONObject(j).getString("id"));

                                                    if(existeVersement.size() == 0) {
                                                        Log.d("versements_trouve","for:"+j+" carnet: "+nouvelle_tontine.getCarnet());
                                                        versement.setMontant(versements.getJSONObject(j).getString("amount"));
                                                        versement.setStatut_paiement(versements.getJSONObject(j).getBoolean("isValide"));
                                                        versement.setFractionne(versements.getJSONObject(j).getString("isFractioned"));
                                                        versement.setIdVersement(versements.getJSONObject(j).getString("idVersement"));
                                                        versement.setIdVersServ(versements.getJSONObject(j).getString("id"));
                                                        Log.d("versements_trouve","idServeur1:"+versements.getJSONObject(j).getString("id"));
                                                        versement.setTontine(nouvelle_tontine);

                                                        // maj des dates
                                                        @SuppressLint("SimpleDateFormat") DateFormat formatter_11 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                                                        Date creation_11 = formatter_11.parse(versements.getJSONObject(j).getString("createdAt"));
                                                        long output_creation_11 = creation_11.getTime() / 1000L;
                                                        String str_creation_11 = Long.toString(output_creation_11);
                                                        long timestamp_creation_11 = Long.parseLong(str_creation_11) * 1000;
                                                        versement.setCreation(timestamp_creation_11);
                                                        Date creation_21 = formatter_11.parse(versements.getJSONObject(j).getString("updatedAt"));
                                                        long output_creation_21 = creation_21.getTime() / 1000L;
                                                        String str_creation_21 = Long.toString(output_creation_21);
                                                        long timestamp_maj_11 = Long.parseLong(str_creation_21) * 1000;
                                                        versement.setMaj(timestamp_maj_11);
                                                        Utilisateur u = SugarRecord.find(Utilisateur.class, "id_utilisateur = ? ", Prefs.getString(ID_UTILISATEUR_KEY, "")).get(0);
                                                        versement.setUtilisateur(u);
                                                        versement.save();
                                                    } else {
                                                        versement = existeVersement.get(0);
                                                        Log.d("versements_trouve","idServeur2-1 : "+versement.getIdVersServ());
                                                    }
                                                    Log.d("versements_trouve","idServeur2-2 : "+versement.getIdVersServ());
                                                    if (versements.getJSONObject(j).getString("isFractioned").equals("false")){
                                                        List<Versement> versementList = Select.from(Versement.class)
                                                                .where(Condition.prop("id_vers_serv").eq(versements.getJSONObject(j).getString("idVersement")))
                                                                .orderBy("id desc")
                                                                .list();
                                                        //Versement v = SugarRecord.findById(Versement.class, Long.valueOf(versements.getJSONObject(j).getString("id_versement")));
                                                        if(versementList.size() >0){
                                                            versement.setIdVersement(String.valueOf(versementList.get(0).getId()));
                                                            versement.save();
                                                        }

                                                    }
                                                }
                                            }


                                            Log.e("connexion", "7connexion trying");
                                            Log.e("connexion", tontines.getJSONObject(i).get("withdraw")+"");
                                            // enrgeistrer les versements en local
                                            if(tontines.getJSONObject(i).has("withdraw") && !tontines.getJSONObject(i).isNull("withdraw"))
                                            {
                                                JSONObject retraits = tontines.getJSONObject(i).getJSONObject("withdraw");

                                                existeRetrait = SugarRecord.find(Retrait.class, "token = ?", retraits.getString("token"));
                                                Retrait retrait = new Retrait();
                                                if(existeRetrait.size() == 0) {

                                                    retrait.setTontine(nouvelle_tontine);
                                                    retrait.setToken(retraits.getString("token"));
                                                    retrait.setStatut(retraits.getString("state"));
//                                                        retrait.setIdMarchand(retraits.getJSONObject(k).getString("id_marchand"));
                                                    retrait.setMontant(retraits.getString("amount"));

                                                    // maj des dates
                                                    @SuppressLint("SimpleDateFormat") DateFormat formatter_111 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                                                    Date creation_111 = formatter_111.parse(retraits.getString("createdAt"));
                                                    long output_creation_111 = creation_111.getTime() / 1000L;
                                                    String str_creation_111 = Long.toString(output_creation_111);
                                                    long timestamp_creation_111 = Long.parseLong(str_creation_111) * 1000;
                                                    retrait.setCreation(timestamp_creation_111);
                                                    Date creation_211 = formatter_111.parse(retraits.getString("updatedAt"));
                                                    long output_creation_211 = creation_211.getTime() / 1000L;
                                                    String str_creation_211 = Long.toString(output_creation_211);
                                                    long timestamp_maj_111 = Long.parseLong(str_creation_211) * 1000;
                                                    retrait.setMaj(timestamp_maj_111);
                                                    Utilisateur u = SugarRecord.find(Utilisateur.class, "id_utilisateur = ? ", Prefs.getString(ID_UTILISATEUR_KEY, "")).get(0);
                                                    retrait.setUtilisateur(u);
                                                    retrait.save();
                                                }
                                                else {
                                                    retrait = existeRetrait.get(0);
                                                    retrait.setStatut(retraits.getString("state"));
                                                    @SuppressLint("SimpleDateFormat") DateFormat formatter_111 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                                                    Date creation_211 = formatter_111.parse(retraits.getString("updatedAt"));
                                                    long output_creation_211 = creation_211.getTime() / 1000L;
                                                    String str_creation_211 = Long.toString(output_creation_211);
                                                    long timestamp_maj_111 = Long.parseLong(str_creation_211) * 1000;
                                                    retrait.setMaj(timestamp_maj_111);
                                                    retrait.save();
                                                }
                                            }
                                        }
                                    }
                                    // enregistrer les historiques
                                    if(user.has("histories") && !user.isNull("histories")){
                                        JSONArray histories = user.getJSONArray("histories");
                                        Log.e("blablabl",histories.length()+"");
                                        if(histories.length()>0) {
                                            Log.d("histories_trouve", "oui:" + histories.length());
                                            List<History> existeHistories = null;
                                            List<Versement> existeVersement = null;
                                            List<Retrait> existeRetrait = null;

                                            for (int i = 0; i < histories.length(); i++) {
                                                History nouvelle_history = new History();

                                                existeHistories = SugarRecord.find(History.class, "id_server = ?", histories.getJSONObject(i).getString("id"));
                                                if (existeHistories.size() == 0) {
                                                    //nouvelle_histories.setId(Long.valueOf(tontines.getJSONObject(i).getString("id")));
                                                    nouvelle_history.setContenu(histories.getJSONObject(i).getString("content"));
                                                    nouvelle_history.setTitre(histories.getJSONObject(i).getString("title"));
                                                    nouvelle_history.setEtat(histories.getJSONObject(i).getBoolean("state") ? 1 : 0);
                                                    nouvelle_history.setUser(nouvel_utilisateur);
//                                                nouvelle_history.setNumero(Integer.parseInt(histories.getJSONObject(i).getString("numero_client")));
                                                    nouvelle_history.setId_server(histories.getJSONObject(i).getString("id"));

                                                    @SuppressLint("SimpleDateFormat")
                                                    DateFormat formatter_1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                                                    Date creation_1 = formatter_1.parse(histories.getJSONObject(i).getString("createdAt"));
                                                    long output_creation_1 = creation_1.getTime() / 1000L;
                                                    String str_creation_1 = Long.toString(output_creation_1);
                                                    long timestamp_creation_1 = Long.parseLong(str_creation_1) * 1000;
                                                    nouvelle_history.setCreation(timestamp_creation_1);
                                                    nouvelle_history.save();
                                                }
                                            }
                                        }
                                    }

                                    new DownloadTask().execute(url_photo);
                                    new DownloadTaskCNI().execute(url_photo_cni);
                                    progressDialog.dismiss();

                                    if (isFromCarteTontine) {
                                        Log.e("Redirection", "Vers CarteMain");
                                        Connexion.this.finish();
                                        Intent carteMainIntent = new Intent(Connexion.this, CarteMain.class);
                                        carteMainIntent.putExtra("id_tontine", idTontine);
                                        startActivity(carteMainIntent);
                                    } else {
                                        isFromCarteTontine = false;
//                                        if (user.getString("firstConnexion").equals("0") && user.getBoolean("addByMerchant")) {
//                                            Log.e("test1", "1success");
//                                            BaseFormElement tel = mFormBuilder.getFormElement(TAG_TEL);
//                                            String tel_value = tel.getValue();
//                                            askCodeOtpVerif(tel.getValue());
//                                        } else {
                                            //Log.e("firstConnexion", user.getString("firstConnexion"));
                                            //Log.e("addByMerchant", user.getString("addByMerchant"));
                                            Log.e("test1", "2echec");
                                            Log.e("Redirection", "Vers Home");
                                            startActivity(new Intent(Connexion.this, Home.class));
                                            Connexion.this.finish();
                                       // }
                                    }
                                    startActivity(new Intent(Connexion.this, Home.class));
                                    Connexion.this.finish();
                                }
                            } else {
                                progressDialog.dismiss();
                                String msg = result.getString("body");
                                Intent i = new Intent(Connexion.this, Message_non.class);
                                i.putExtra("msg_desc", msg);
                                i.putExtra("class", "com.sicmagroup.tondi.Connexion");
                                startActivity(i);
                            }
                        } catch (Throwable t) {
                            Log.d("errornscription", String.valueOf(t.getCause()));
                            Log.e("My_App", String.valueOf(response));
                            Log.e("Connexion", t.getMessage());
                        }

                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {
                        progressDialog.dismiss();
                        Log.d("Error.Inscription", String.valueOf(volleyError.getMessage()));
                        ConstraintLayout mainLayout = findViewById(R.id.layout_connexion);

                        String message;
                        if (volleyError instanceof NetworkError || volleyError instanceof AuthFailureError) {
                            message = "Aucune connexion Internet!";
                        } else if (volleyError instanceof TimeoutError) {
                            message = "Erreur de temporisation!";
                        } else if (volleyError instanceof ServerError) {
                            message = "Impossible de contacter le serveur!";
                        } else if (volleyError instanceof ParseError) {
                            message = "Une erreur est survenue!";
                        } else {
                            message = "Erreur inconnue!";
                        }

                        Snackbar snackbar = Snackbar
                                .make(mainLayout, message, Snackbar.LENGTH_INDEFINITE)
                                .setAction("REESSAYER", new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        auth_en_ligne();
                                    }
                                });
                        snackbar.getView().setBackgroundColor(ContextCompat.getColor(Connexion.this, R.color.colorGray));
                        snackbar.setActionTextColor(getResources().getColor(R.color.colorPrimary));
                        View sbView = snackbar.getView();
                        TextView textView = sbView.findViewById(com.google.android.material.R.id.snackbar_text);
                        textView.setTextColor(Color.WHITE);
                        snackbar.show();
                    }
                }
        );

        postRequest.setRetryPolicy(new DefaultRetryPolicy(
                50000,
                -1,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        queue.add(postRequest);

        progressDialog = new ProgressDialog(Connexion.this);
        progressDialog.setCancelable(false);
        progressDialog.setMessage("Veuillez patienter SVP! La récupération de votre compte est en cours ...");
        progressDialog.show();
    }


    public void askCodeOtpVerif(String tel){
        RequestQueue queue = Volley.newRequestQueue(Connexion.this);
        StringRequest postRequest = new StringRequest(Request.Method.POST, Constantes.URL_GENERATE_OTP,
                new Response.Listener<String>() {
                    @SuppressLint({"ResourceAsColor", "LongLogTag"})
                    @Override
                    public void onResponse(String response) {
                        Log.d("ResponseTagMain", response);
                        // if (!response.equals("Erreur")) {
                        try {

                            JSONObject result = new JSONObject(response);
                            Log.e("La reponse de la connexion", result.toString());
                            if (result.getInt("responseCode") == 0) {
                                progressDialog.dismiss();
                                Intent codeOtpVer = new Intent(Connexion.this, CodeOtpVerification.class);
                                codeOtpVer.putExtra("caller_activity", "connexion");
                                codeOtpVer.putExtra("tel", tel);
                                startActivity(codeOtpVer);
                                SmsRetrieverClient client = SmsRetriever.getClient(Connexion.this);
                                Task<Void> task = client.startSmsRetriever();
                                task.addOnSuccessListener(new com.google.android.gms.tasks.OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {


                                    }
                                });
                                task.addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Prefs.putString(ID_UTILISATEUR_KEY, null);
                                        Intent i = new Intent(Connexion.this, Message_non.class);
                                        i.putExtra("msg_desc", "Erreur time out. Veuillez réessayer, Merci.");
                                        i.putExtra("class", "com.sicmagroup.tondi.Connexion");
                                        startActivity(i);
                                    }
                                });
//                                Connexion.this.finish();
                            } else {
                                Log.e("Connexion", "error pour le compte 1");
//                                progressDialog.dismiss();
                                Connexion.this.finish();
                                Prefs.putString(ID_UTILISATEUR_KEY, null);
                                Intent i = new Intent(Connexion.this, Message_non.class);
                                i.putExtra("msg_desc", result.getString("body"));
                                i.putExtra("class", "com.sicmagroup.tondi.Connexion");

                                startActivity(i);
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
//                        progressDialog.dismiss();
                        ConstraintLayout mainLayout =  findViewById(R.id.layout_connexion);
                        // error
                        Log.e("Error.connexion", String.valueOf(error.getMessage()));
                        String message = "Une erreur est survenue! Veuillez réessayer svp1.";
                        Snackbar snackbar = Snackbar
                                .make(mainLayout, message, Snackbar.LENGTH_INDEFINITE)
                                .setAction("OK", new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {

                                    }
                                });
                        snackbar.getView().setBackgroundColor(ContextCompat.getColor(Connexion.this, R.color.colorGray));
                        // Changing message text color
                        snackbar.setActionTextColor(getResources().getColor(R.color.colorPrimaryDark));
                        // Changing action button text color
                        View sbView = snackbar.getView();
                        TextView textView = (TextView) sbView.findViewById(com.google.android.material.R.id.snackbar_text);
                        textView.setTextColor(Color.WHITE);
                        snackbar.show();
                    }
                }
        ) {
            @Override
            protected Map<String, String> getParams() {
                //Date currentDate = new Date();
                Map<String, String> params = new HashMap<String, String>();
                params.put("number", tel);
                params.put("type_operation", OperationTypeEnum.ACCES.toString());
                // params.put("verification_date", android.text.format.DateFormat.format("yyyy-MM-dd H:i:m", currentDate.getTime()).toString());
                return params;
            }
        };

        postRequest.setRetryPolicy(new DefaultRetryPolicy(
                25000,
                -1,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        queue.add(postRequest);
//
        progressDialog = new ProgressDialog(Connexion.this);
        progressDialog.setCancelable(false);
        progressDialog.setMessage("Veuillez patienter SVP! \n Vérification du numero en cours.");
        progressDialog.show();
    }


    private void auth_credential_online(Utilisateur user)
    {
        RequestQueue queue = Volley.newRequestQueue(Connexion.this);
        StringRequest postRequest = new StringRequest(Request.Method.POST, user_credential_check,
                new Response.Listener<String>()
                {
                    @SuppressLint("ResourceAsColor")
                    @Override
                    public void onResponse(String response) {
                        Log.d("ResponseTagMain", response);
                        // if (!response.equals("Erreur")) {
                        try {

                            JSONObject result = new JSONObject(response);
                            if(result.getBoolean("success"))
                            {
                                final JSONObject user_servData = result.getJSONObject("data");
                                progressDialog.dismiss();

                                final JSONObject resultat = result.getJSONObject("data");
                                Prefs.putString(PIN_KEY, user_servData.getString("pin_acces"));
                                // Mettre à jour la préférence pin d'accès
                                Prefs.putString(PASS_KEY, user_servData.getString("mdp"));

                                // maj des dates
                                Date currentTime = Calendar.getInstance().getTime();
                                long output_creation = currentTime.getTime() / 1000L;
                                String str_creation = Long.toString(output_creation);
                                long output_maj = currentTime.getTime() / 1000L;
                                String str_maj = Long.toString(output_maj);
                                long timestamp_maj = Long.parseLong(str_maj) * 1000;
                                user.setConnecter_le(timestamp_maj);

                                Prefs.putString(ID_UTILISATEUR_KEY, String.valueOf(user.getId()));

                                Prefs.putString(NOM_KEY, user.getNom());

                                Prefs.putString(PRENOMS_KEY, user.getPrenoms());

                                Prefs.putString(PHOTO_KEY, user_servData.getString("photo_identite"));

                                Prefs.putString(CONNECTER_KEY, String.valueOf(user.getConnecter_le()));

                                Prefs.putString(TEL_KEY, user_servData.getString("numero"));
                                Prefs.putBoolean(MOOV_DATA_SHARING, true);

                                Prefs.putString(STATUT_UTILISATEUR, user.getStatut());

                                Prefs.putString(PHOTO_CNI_KEY, user_servData.getString("photo_cni"));

                                Prefs.putString(FIREBASE_TOKEN, user.getFirebaseToken());

                                Prefs.putString(SEXE_KEY, user_servData.getString("sexe"));

                                //Mettre à jour la préference numero de compte
                                Prefs.putString(NUMERO_COMPTE_KEY, result.getString("numero_compte"));
                                user.setNumero_compte(result.getString("numero_compte"));
                                user.save();

                                URL url_photo  = new URL(medias_url + Prefs.getString(PHOTO_KEY, null) + ".JPG");
                                new DownloadTask().execute(url_photo);
                                URL url_photo_cni = new URL(medias_url + Prefs.getString(PHOTO_CNI_KEY, null) + ".JPG");
                                new DownloadTaskCNI().execute(url_photo_cni);
                                Log.e("photo",  Prefs.getString(PHOTO_CNI_KEY, null));

                                Connexion.this.finish();
//                                startActivity(new Intent(Connexion.this, Dashboard.class));
                                Log.e("Homeadvance","home3");
                                startActivity(new Intent(Connexion.this, Home.class));

                            }
                            else
                            {
                                progressDialog.dismiss();
                                Connexion.this.finish();
                                String msg="Vos identifiants sont incorrects. Veuillez réessayer SVP!";
                                Intent i = new Intent(Connexion.this, Message_non.class);
                                i.putExtra("msg_desc",msg);
                                i.putExtra("class","com.sicmagroup.tondi.Connexion");
                                startActivity(i);
                            }

                        } catch (JSONException | MalformedURLException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener()
                {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {
                        progressDialog.dismiss();
                        Log.e("ResponseTagMain", String.valueOf(volleyError.getMessage()));
                        Log.e("Stack", "Error StackTrace: \t" + volleyError.getStackTrace());
                        // error
                        //Log.d("Error.Inscription", String.valueOf(error.getMessage()));
                        ConstraintLayout mainLayout =  findViewById(R.id.layout_connexion);

                        // volleyError.getMessage() == null
                        String message;
                        if (volleyError instanceof NetworkError || volleyError instanceof AuthFailureError || volleyError instanceof TimeoutError) {//Toast.makeText(Inscription.this, "error:"+volleyError.getMessage(), Toast.LENGTH_SHORT).show();

                            message = "Aucune connexion Internet! Patientez et réessayez.";
                            final Snackbar snackbar = Snackbar
                                    .make(mainLayout, message, Snackbar.LENGTH_INDEFINITE)
                                    .setAction("Reessayer", new View.OnClickListener() {
                                        @Override
                                        public void onClick(View view) {
                                            auth_credential_online(user);
                                        }
                                    });
                            snackbar.getView().setBackgroundColor(ContextCompat.getColor(Connexion.this, R.color.colorGray));
                            // Changing message text color
                            snackbar.setActionTextColor(getResources().getColor(R.color.colorPrimary));
                            // Changing action button text color
                            View sbView = snackbar.getView();
                            TextView textView = (TextView) sbView.findViewById(com.google.android.material.R.id.snackbar_text);
                            textView.setTextColor(Color.WHITE);
                            snackbar.show();

                        } else if (volleyError instanceof ServerError) {
                            message = "Impossible de contacter le serveur! Patientez et réessayez.";
                            Snackbar snackbar = Snackbar
                                    .make(mainLayout, message, Snackbar.LENGTH_INDEFINITE)
                                    .setAction("REESSAYER", new View.OnClickListener() {
                                        @Override
                                        public void onClick(View view) {
                                            auth_credential_online(user);
                                        }
                                    });
                            snackbar.getView().setBackgroundColor(ContextCompat.getColor(Connexion.this, R.color.colorGray));
                            // Changing message text color
                            snackbar.setActionTextColor(getResources().getColor(R.color.colorPrimary));
                            // Changing action button text color
                            View sbView = snackbar.getView();
                            TextView textView = (TextView) sbView.findViewById(com.google.android.material.R.id.snackbar_text);
                            textView.setTextColor(Color.WHITE);
                            snackbar.show();
                        }  else if (volleyError instanceof ParseError) {
                            //message = "Parsing error! Please try again later";
                            message = "Une erreur est survenue! Patientez et réessayez.";
                            Snackbar snackbar = Snackbar
                                    .make(mainLayout, message, Snackbar.LENGTH_INDEFINITE)
                                    .setAction("REESSAYER", new View.OnClickListener() {
                                        @Override
                                        public void onClick(View view) {
                                            auth_credential_online(user);
                                        }
                                    });
                            snackbar.getView().setBackgroundColor(ContextCompat.getColor(Connexion.this, R.color.colorGray));
                            // Changing message text color
                            snackbar.setActionTextColor(getResources().getColor(R.color.colorPrimary));
                            // Changing action button text color
                            View sbView = snackbar.getView();
                            TextView textView = (TextView) sbView.findViewById(com.google.android.material.R.id.snackbar_text);
                            textView.setTextColor(Color.WHITE);
                            snackbar.show();
                        }
                    }
                }
        ) {
            @Override
            protected Map<String, String> getParams() {

                BaseFormElement tel = mFormBuilder.getFormElement(TAG_TEL);
                BaseFormElement mdp = mFormBuilder.getFormElement(TAG_PASS);

                String tel_value = tel.getValue();
                String mdp_value = mdp.getValue();

                Map<String, String>  params = new HashMap<String, String>();

                params.put("numero", tel_value);
                params.put("mdp", mdp_value);
                return params;
            }
        };

        queue.add(postRequest);


        progressDialog = new ProgressDialog(Connexion.this);
        progressDialog.setCancelable(false);
        progressDialog.setMessage("Veuillez patienter SVP!Connexion en cours...");
        progressDialog.show();
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
                        Log.d("ResponseTagkk", response);
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
        };
        postRequest.setRetryPolicy(new DefaultRetryPolicy(
                5000,
                -1,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        queue.add(postRequest);
    }

    public static void saveImage(String imageUrl, String destinationFile) throws IOException {

        URL url = new URL(imageUrl);
        InputStream is = url.openStream();
        OutputStream os = new FileOutputStream(destinationFile);

        byte[] b = new byte[2048];
        int length;

        while ((length = is.read(b)) != -1) {
            os.write(b, 0, length);
        }

        is.close();
        os.close();
    }

/*
    private class DownloadTask extends AsyncTask<URL,Void,Bitmap> {
        // Before the tasks execution
        protected void onPreExecute(){
            // Display the progress dialog on async task start

        }

        // Do the task in background/non UI thread
        protected Bitmap doInBackground(URL...urls){
            URL url = urls[0];
            HttpURLConnection connection = null;

            try{
                // Initialize a new http url connection
                connection = (HttpURLConnection) url.openConnection();

                // Connect the http url connection
                connection.connect();

                // Get the input stream from http url connection
                InputStream inputStream = connection.getInputStream();

                BufferedInputStream bufferedInputStream = new BufferedInputStream(inputStream);
                Bitmap bmp = BitmapFactory.decodeStream(bufferedInputStream);

                // Return the downloaded bitmap
                return bmp;

            }catch(IOException e){
                e.printStackTrace();
            }finally{
                // Disconnect the http url connection
                connection.disconnect();
            }
            return null;
        }

        // When all async task done
        protected void onPostExecute(Bitmap result){
            // Hide the progress dialog
            if(result!=null){
                // Save bitmap to internal storage
                utilitaire.saveToInternalStorage(result,Prefs.getString(PHOTO_KEY, null));
            }else {
                // Notify user that an error occurred while downloading image
                Toast.makeText(getApplicationContext(),"Erreur, photo de profile introuvable",Toast.LENGTH_LONG).show();
            }
        }
    }*/


    private class DownloadTask extends AsyncTask<URL, Void, Bitmap> {
        private URL url;

        // Before the tasks execution
        protected void onPreExecute() {
            // Display the progress dialog on async task start
        }

        // Do the task in background/non UI thread
        protected Bitmap doInBackground(URL... urls) {
            url = urls[0];
            return downloadImage(url);
        }

        // Download the image from the given URL
        private Bitmap downloadImage(URL url) {
            HttpURLConnection connection = null;

            try {
                // Initialize a new http url connection
                connection = (HttpURLConnection) url.openConnection();

                // Connect the http url connection
                connection.connect();

                // Check if the response code is 401
                int responseCode = connection.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_UNAUTHORIZED) {
                    // Refresh the token and retry the download
                    if (refreshToken()) {
                        return downloadImage(url); // Retry the download after refreshing the token
                    } else {
                        return null; // If token refresh fails, return null
                    }
                }

                // Get the input stream from http url connection
                InputStream inputStream = connection.getInputStream();

                BufferedInputStream bufferedInputStream = new BufferedInputStream(inputStream);
                Bitmap bmp = BitmapFactory.decodeStream(bufferedInputStream);

                // Return the downloaded bitmap
                return bmp;

            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (connection != null) {
                    // Disconnect the http url connection
                    connection.disconnect();
                }
            }
            return null;
        }

        // Refresh the token
        private boolean refreshToken() {
            // Implement your token refresh logic here
            // Return true if token refresh is successful, false otherwise
            // You might use a synchronous HTTP request here for simplicity
            // For example, using HttpURLConnection or OkHttpClient to refresh the token
            // Here is a simple example (you need to adapt it to your logic):
            try {
                URL url = new URL(Constantes.url_refresh_token);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("POST");
                connection.setDoOutput(true);
                connection.setRequestProperty("Content-Type", "application/json");

                JSONObject params = new JSONObject();
                params.put("refreshToken", Prefs.getString(REFRESH_TOKEN, ""));

                OutputStream os = connection.getOutputStream();
                os.write(params.toString().getBytes("UTF-8"));
                os.close();

                int responseCode = connection.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    InputStream inputStream = connection.getInputStream();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                    StringBuilder result = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        result.append(line);
                    }
                    reader.close();

                    JSONObject jsonResponse = new JSONObject(result.toString());
                    String newAccessToken = jsonResponse.getString("token");
                    String newRefreshToken = jsonResponse.getString("refreshToken");

                    Prefs.putString(TOKEN, newAccessToken);
                    Prefs.putString(REFRESH_TOKEN, newRefreshToken);
                    return true;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return false;
        }

        // When all async task done
        protected void onPostExecute(Bitmap result) {
            // Hide the progress dialog
            if (result != null) {
                // Save bitmap to internal storage
                utilitaire.saveToInternalStorage(result, Prefs.getString(PHOTO_KEY, null));
            } else {
                // Notify user that an error occurred while downloading image
                Toast.makeText(getApplicationContext(), "Erreur, photo de profil introuvable", Toast.LENGTH_LONG).show();
            }
        }
    }

/*
    private class DownloadTaskCNI extends AsyncTask<URL,Void,Bitmap> {
        // Before the tasks execution
        protected void onPreExecute(){
            // Display the progress dialog on async task start

        }

        // Do the task in background/non UI thread
        protected Bitmap doInBackground(URL...urls){
            URL url = urls[0];
            HttpURLConnection connection = null;

            try{
                // Initialize a new http url connection
                connection = (HttpURLConnection) url.openConnection();

                // Connect the http url connection
                connection.connect();

                // Get the input stream from http url connection
                InputStream inputStream = connection.getInputStream();

                BufferedInputStream bufferedInputStream = new BufferedInputStream(inputStream);

                Bitmap bmp = BitmapFactory.decodeStream(bufferedInputStream);

                // Return the downloaded bitmap
                return bmp;

            }catch(IOException e){
                e.printStackTrace();
            }finally{
                // Disconnect the http url connection
                connection.disconnect();
            }
            return null;
        }

        // When all async task done
        protected void onPostExecute(Bitmap result){
            // Hide the progress dialog

            if(result!=null){
                // Save bitmap to internal storage
                utilitaire.saveToInternalStorage(result,Prefs.getString(PHOTO_CNI_KEY, null));

            }else {
                // Notify user that an error occurred while downloading image
                Toast.makeText(getApplicationContext(),"Une erreur est survenue",Toast.LENGTH_LONG).show();
            }
        }
    }
*/

    private class DownloadTaskCNI extends AsyncTask<URL, Void, Bitmap> {
        private URL url;

        // Before the tasks execution
        protected void onPreExecute() {
            // Display the progress dialog on async task start
        }

        // Do the task in background/non UI thread
        protected Bitmap doInBackground(URL... urls) {
            url = urls[0];
            return downloadImage(url);
        }

        // Download the image from the given URL
        private Bitmap downloadImage(URL url) {
            HttpURLConnection connection = null;

            try {
                // Initialize a new http url connection
                connection = (HttpURLConnection) url.openConnection();

                // Connect the http url connection
                connection.connect();

                // Check if the response code is 401
                int responseCode = connection.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_UNAUTHORIZED) {
                    // Refresh the token and retry the download
                    if (refreshToken()) {
                        return downloadImage(url); // Retry the download after refreshing the token
                    } else {
                        return null; // If token refresh fails, return null
                    }
                }

                // Get the input stream from http url connection
                InputStream inputStream = connection.getInputStream();

                BufferedInputStream bufferedInputStream = new BufferedInputStream(inputStream);
                Bitmap bmp = BitmapFactory.decodeStream(bufferedInputStream);

                // Return the downloaded bitmap
                return bmp;

            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (connection != null) {
                    // Disconnect the http url connection
                    connection.disconnect();
                }
            }
            return null;
        }

        // Refresh the token
        private boolean refreshToken() {
            // Implement your token refresh logic here
            // Return true if token refresh is successful, false otherwise
            // You might use a synchronous HTTP request here for simplicity
            // For example, using HttpURLConnection or OkHttpClient to refresh the token
            // Here is a simple example (you need to adapt it to your logic):
            try {
                URL url = new URL(Constantes.url_refresh_token);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("POST");
                connection.setDoOutput(true);
                connection.setRequestProperty("Content-Type", "application/json");

                JSONObject params = new JSONObject();
                params.put("refreshToken", Prefs.getString(REFRESH_TOKEN, ""));

                OutputStream os = connection.getOutputStream();
                os.write(params.toString().getBytes("UTF-8"));
                os.close();

                int responseCode = connection.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    InputStream inputStream = connection.getInputStream();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                    StringBuilder result = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        result.append(line);
                    }
                    reader.close();

                    JSONObject jsonResponse = new JSONObject(result.toString());
                    String newAccessToken = jsonResponse.getString("token");
                    String newRefreshToken = jsonResponse.getString("refreshToken");

                    Prefs.putString(TOKEN, newAccessToken);
                    Prefs.putString(REFRESH_TOKEN, newRefreshToken);
                    return true;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return false;
        }

        // When all async task done
        protected void onPostExecute(Bitmap result) {
            // Hide the progress dialog
            if (result != null) {
                // Save bitmap to internal storage
                utilitaire.saveToInternalStorage(result, Prefs.getString(PHOTO_CNI_KEY, null));
            } else {
                // Notify user that an error occurred while downloading image
                Toast.makeText(getApplicationContext(), "Une erreur est survenue", Toast.LENGTH_LONG).show();
            }
        }
    }

    private PermissionService.Callback callback = new PermissionService.Callback() {
        @Override
        public void onRefuse(ArrayList<String> RefusePermissions) {
            Toast.makeText(Connexion.this,
                    getString(R.string.refuse_permissions),
                    Toast.LENGTH_SHORT).show();
            Connexion.this.finish();
        }

        @Override
        public void onFinally() {
            // pass
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (!Settings.canDrawOverlays(Connexion.this)) {
                    Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                            Uri.parse("package:" + Connexion.this.getPackageName()));
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

    private void verifier_sim(final String numero){
        //Toast.makeText(getApplicationContext(),"auth_en_ligne"+""+numero,Toast.LENGTH_LONG).show();
        final String reseau = utilitaire.getOperatorByNumber(numero);
        String phoneNumber = null;
        if (reseau.equals("MTN")){
            phoneNumber = "*136*8#".trim();
        }else{
            phoneNumber = ("*111*2*1#").trim();
        }


        ussdApi = USSDController.getInstance(Connexion.this);
        final Intent svc = new Intent(Connexion.this, SplashLoadingService.class);
        svc.putExtra("texte","La vérification de votre mot de passe est en cours...");
        //startService(svc);
        //result.setText("");
        ussdApi.callUSSDInvoke(phoneNumber, map, new USSDController.CallbackInvoke() {
            @Override
            public void responseInvoke(String message) {
                Log.d("APPUSSpp11", message);
            }

            @SuppressLint("ResourceAsColor")
            @Override
            public void over(String message) {
                //stopService(svc);
                Log.d("APPUSSpp", message);
                Pattern pattern_msg_ok;
                Pattern pattern_msg_ok1=null;
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
                    pattern_msg_ok1 = Pattern.compile("ProblèmedeconnexionoucodeIHMnonvalide");
                    pattern_msg_ok2 = Pattern.compile("MMInonvalide");
                    pattern_msg_ok3 = Pattern.compile("Invalidservicecode");
                    pattern_msg_ok4 = Pattern.compile("229"+"\\d");
                }

                Matcher matcher = pattern_msg_ok.matcher(message.replaceAll("\\s",""));

                assert pattern_msg_ok1 != null;
                assert pattern_msg_ok2 != null;
                assert pattern_msg_ok3 != null;
                Matcher matcher_connexion = pattern_msg_ok1.matcher(message.replaceAll("\\s",""));
                Matcher matcher_connexion_2 = pattern_msg_ok2.matcher(message.replaceAll("\\s",""));
                Matcher matcher_connexion_3 = pattern_msg_ok3.matcher(message.replaceAll("\\s",""));
                Matcher matcher_sim_non_4 = pattern_msg_ok4.matcher(message.replaceAll("\\s",""));
                // if our pattern matches the string, we can try to extract our groups
                if (matcher.find() ) {
                    // sim verifie
                    auth();
                }else{
                    // si probleme connexion
                    if (matcher_sim_non_4.find()){
                        String msg="La SIM "+numero+" n'a pas pu être vérifié! Veuillez insérer votre SIM ("+numero+") puis réessayer SVP!";
                        Intent i = new Intent(Connexion.this, Message_non.class);
                        i.putExtra("mmi","1");
                        i.putExtra("tel",element2.getValue());
                        i.putExtra("mdp",element1.getValue());
                        i.putExtra("msg_desc",msg);
                        i.putExtra("class","com.sicmagroup.tondi.Connexion");
                        startActivity(i);
                    }
                    else if(matcher_connexion.find()){
                        // afficher Message_non
                        //Connexion.this.finish();
                        String msg="Problème de connexion ou IHM non valide. Veuillez réessayer SVP!";
                        Intent i = new Intent(Connexion.this, Message_non.class);
                        i.putExtra("mmi","1");
                        i.putExtra("tel",element2.getValue());
                        i.putExtra("mdp",element1.getValue());
                        i.putExtra("msg_desc",msg);
                        i.putExtra("class","com.sicmagroup.tondi.Connexion");
                        startActivity(i);
                    }else if(matcher_connexion_2.find() || matcher_connexion_3.find()){

                        String msg="Problème de connexion ou MMI non valide. Veuillez réessayer SVP!";
                        Intent i = new Intent(Connexion.this, Message_non.class);
                        i.putExtra("mmi","1");
                        i.putExtra("tel",element2.getValue());
                        i.putExtra("mdp",element1.getValue());
                        i.putExtra("msg_desc",msg);
                        i.putExtra("class","com.sicmagroup.tondi.Connexion");
                        startActivity(i);
                    }else{

                        String msg="Tondi rencontre des difficultés réseau. Veuillez réessayer plustard SVP!";
                        Intent i = new Intent(Connexion.this, Message_non.class);
                        i.putExtra("mmi","1");
                        i.putExtra("tel",element2.getValue());
                        i.putExtra("mdp",element1.getValue());
                        i.putExtra("msg_desc",msg);
                        i.putExtra("class","com.sicmagroup.tondi.Connexion");
                        startActivity(i);
                    }

                }

            }
        });
    }

    @Override
    public void onBackPressed() {

        Dialog dialog_alert = new Dialog(Connexion.this);
        dialog_alert.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog_alert.setCancelable(true);
        dialog_alert.setContentView(R.layout.dialog_attention);

        TextView titre = (TextView) dialog_alert.findViewById(R.id.deco_title);
        titre.setText("Quitter l'application");
        TextView message_deco = (TextView) dialog_alert.findViewById(R.id.deco_message);
        message_deco.setText("Êtes vous sûr de vouloir quitter Tondi ?");

        Button oui = (Button) dialog_alert.findViewById(R.id.btn_oui);
        oui.setText("Oui");
        oui.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog_alert.cancel();
                Connexion.this.finishAffinity();
            }
        });

        Button non = (Button) dialog_alert.findViewById(R.id.btn_non);
        non.setText("Non");
        non.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog_alert.cancel();

            }
        });

        dialog_alert.show();

    }


    // To check if service is enabled
    private boolean isAccessibilitySettingsOn(Context mContext) {
        int accessibilityEnabled = 0;
        final String service = getPackageName() + "/" + USSDService.class.getCanonicalName();
        try {
            accessibilityEnabled = Settings.Secure.getInt(
                    mContext.getApplicationContext().getContentResolver(),
                    android.provider.Settings.Secure.ACCESSIBILITY_ENABLED);
            Log.v(TAG, "accessibilityEnabled = " + accessibilityEnabled);
        } catch (Settings.SettingNotFoundException e) {
            Log.e(TAG, "Error finding setting, default accessibility to not found: "
                    + e.getMessage());
        }
        TextUtils.SimpleStringSplitter mStringColonSplitter = new TextUtils.SimpleStringSplitter(':');

        if (accessibilityEnabled == 1) {
            Log.v(TAG, "***ACCESSIBILITY IS ENABLED*** -----------------");
            String settingValue = Settings.Secure.getString(
                    mContext.getApplicationContext().getContentResolver(),
                    Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES);
            if (settingValue != null) {
                mStringColonSplitter.setString(settingValue);
                while (mStringColonSplitter.hasNext()) {
                    String accessibilityService = mStringColonSplitter.next();

                    Log.v(TAG, "-------------- > accessibilityService :: " + accessibilityService + " " + service);
                    if (accessibilityService.equalsIgnoreCase(service)) {
                        Log.v(TAG, "We've found the correct setting - accessibility is switched on!");
                        return true;
                    }
                }
            }
        } else {
            Log.v(TAG, "***ACCESSIBILITY IS DISABLED***");
        }

        return false;
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
    protected void onUserLeaveHint() {
        final Intent svc = new Intent(Connexion.this, SplashLoadingService.class);
        //stopService(svc);
        super.onUserLeaveHint();
    }
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode==KeyEvent.KEYCODE_HOME){
            Log.e("home key pressed", "****");

        }
        return super.onKeyDown(keyCode, event);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_APP_UPDATE && resultCode != RESULT_OK) {
            Toast.makeText(this, "Annuler", Toast.LENGTH_SHORT).show();
        }
    }

}
