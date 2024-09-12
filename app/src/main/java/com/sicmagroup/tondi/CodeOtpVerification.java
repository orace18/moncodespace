package com.sicmagroup.tondi;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.telephony.SmsMessage;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
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
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.auth.api.phone.SmsRetriever;
import com.google.android.gms.auth.api.phone.SmsRetrieverClient;
import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.gson.Gson;
import com.orm.SugarRecord;
import com.pixplicity.easyprefs.library.Prefs;
import com.sicmagroup.formmaster.model.BaseFormElement;
import com.sicmagroup.tondi.utils.Constantes;
import com.sicmagroup.tondi.Enum.OperationTypeEnum;
import com.sicmagroup.tondi.Enum.SexeEnum;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


import static com.sicmagroup.tondi.Connexion.CODE_OTP_RECU;
import static com.sicmagroup.tondi.Connexion.CONNECTER_KEY;
import static com.sicmagroup.tondi.Connexion.ID_UTILISATEUR_KEY;
import static com.sicmagroup.tondi.Connexion.NOM_KEY;
import static com.sicmagroup.tondi.Connexion.PASS_KEY;
import static com.sicmagroup.tondi.Connexion.PIN_KEY;
import static com.sicmagroup.tondi.Connexion.PRENOMS_KEY;
import static com.sicmagroup.tondi.Connexion.SEXE_KEY;
import static com.sicmagroup.tondi.Connexion.TEL_KEY;
import static com.sicmagroup.tondi.Connexion.UUID_KEY;
import static com.sicmagroup.tondi.Connexion.url_disable_code_otp;

import static com.sicmagroup.tondi.utils.Constantes.REFRESH_TOKEN;
import static com.sicmagroup.tondi.utils.Constantes.SERVEUR;
import static com.sicmagroup.tondi.utils.Constantes.TOKEN;
import static com.sicmagroup.tondi.utils.Constantes.accessToken;

public class CodeOtpVerification extends AppCompatActivity {

    TextView textView_helpMessage;
    Button button_annulerCodeOtp;
    static Button button_validerCodeOtp;
    static EditText editText_codeOtp;
    int id_tontine;
    public static ProgressDialog progressDialog;
    String montant_t;

    String numero;
    String montant;
    Tontine tontine_main;
    String url_retrait_mmo = SERVEUR+"/api/v1/versements/retrait_mmo";
    String url_inscrire = SERVEUR+"/api/v1/utilisateurs/inscrire";
    LinearLayout mainLayout;
    String caller_activity;
    String token;
    String numero_marchand;
    String codeOtpRecu;
    String nom;
    String prenoms;
    String mdp;
    String sexe;
    final String callable_activity_inscription = "inscription";
    final String callable_activity_carte = "carteMain";
    final String callable_activity_smsReceiver = "smsReceiver";
    final String callable_activity_connexion = "connexion";
    Utilitaire utilitaire;
    boolean billetCodeRetraitEspece = false;
    String url_valider = SERVEUR+"/api/v1/versements/valider_ussd";
    private static final int SMS_CONSENT_REQUEST = 2;  // Set to an unused request code
    private static final int REQUEST_CODE = 101;
    private OtpReceiver otpReceiver;
    TextView renvoyerCodeOtp;
    TextView timerBeforeResendCode;
    CountDownTimer timer;
    private static final String CHANNEL_ID = "106";
    private static final int NOTIF_CHANNEL = 107;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_code_otp_verification);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECEIVE_SMS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECEIVE_SMS}, REQUEST_CODE);
        }


        new Prefs.Builder()
                .setContext(this)
                .setMode(ContextWrapper.MODE_PRIVATE)
                .setPrefsName(getPackageName())
                .setUseDefaultSharedPreference(true)
                .build();

        final Intent intent = getIntent();
        utilitaire = new Utilitaire(this);
        caller_activity = intent.getStringExtra("caller_activity");

        textView_helpMessage =  findViewById(R.id.help_msg_saisit_codeotp);
        button_annulerCodeOtp = findViewById(R.id.annuler_code_otp);
        button_validerCodeOtp = findViewById(R.id.valider_code_otp);
        editText_codeOtp = findViewById(R.id.code_otp_saisit);
        mainLayout =  findViewById(R.id.layout_code_otp);
        codeOtpRecu = Prefs.getString(CODE_OTP_RECU, "");


        SmsRetrieverClient client = SmsRetriever.getClient(this);
        Task<Void> task = client.startSmsUserConsent(null); // null pour obtenir les SMS de n'importe quel expéditeur

        task.addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                // API prête à être utilisée
                Log.d("OTP", "Demande de consentement envoyée avec succès.");
            }
        });

        task.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                // Échec de la demande
                Log.e("OTP", "Échec de la demande de consentement.");
            }
        });


        if(caller_activity.equals(callable_activity_carte))
        {
            id_tontine = intent.getIntExtra("id_tontine", 0);
            numero = intent.getStringExtra("numero");
            montant = intent.getStringExtra("montant");
            tontine_main = SugarRecord.findById(Tontine.class, (long) id_tontine);
            codeOtpRecu = intent.getStringExtra("code_otp");
            textView_helpMessage.append(Prefs.getString(TEL_KEY, "").substring(0, 4)+"...");
        }
        else if(caller_activity.equals(callable_activity_smsReceiver))
        {
            numero = Prefs.getString(TEL_KEY, "");
            token = intent.getStringExtra("token");
            numero_marchand = intent.getStringExtra("numero_marchand");
            textView_helpMessage.append(Prefs.getString(TEL_KEY, "").substring(0, 4)+"...");
            codeOtpRecu = intent.getStringExtra("code_otp");

            editText_codeOtp.setText(codeOtpRecu);
            button_validerCodeOtp.performClick();

        }
        else if(caller_activity.equals(callable_activity_inscription))
        {
            numero = intent.getStringExtra("numero");
            nom = intent.getStringExtra("nom");
            prenoms = intent.getStringExtra("prenoms");
            mdp = intent.getStringExtra("mdp");
            sexe = intent.getStringExtra("sexe");

            textView_helpMessage.append(numero.substring(0, 4)+"...");
            codeOtpRecu = intent.getStringExtra("code_otp");
//            editText_codeOtp.setText(codeOtpRecu);
//            button_validerCodeOtp.performClick();

//            Prefs.remove(TEL_INSC_ENCOURS);
//            Prefs.remove(NOM_INSC_ENCOURS);
//            Prefs.remove(PRENOM_INSC_ENCOURS);
//            Prefs.remove(MDP_INSC_ENCOURS);
//            Prefs.remove(SEXE_INSC_ENCOURS);

        }
        else if(caller_activity.equals(callable_activity_connexion)){
            numero = intent.getStringExtra("tel");
        }
        renvoyerCodeOtp = (TextView) findViewById(R.id.resend_otp);
        timerBeforeResendCode = (TextView) findViewById(R.id.timer_fornewOtp);
        renvoyerCodeOtp.setEnabled(false);

       timer = new CountDownTimer(120000, 1000) {
           @Override
           public void onTick(long l) {
                timerBeforeResendCode.setText("dans "+ l/1000);
                //Log.e("timer", ""+l/1000);
           }

           @Override
           public void onFinish() {
               timerBeforeResendCode.setText("maintenant");
               renvoyerCodeOtp.setClickable(true);
               renvoyerCodeOtp.setEnabled(true);
           }
       }.start();

       renvoyerCodeOtp.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View view) {
               if(caller_activity.equals(callable_activity_inscription))
               {
                   RequestQueue queue = Volley.newRequestQueue(CodeOtpVerification.this);
                   StringRequest postRequest = new StringRequest(Request.Method.POST, Constantes.URL_GENERATE_OTP,
                           new Response.Listener<String>() {
                               @SuppressLint("ResourceAsColor")
                               @Override
                               public void onResponse(String response) {
                                   Log.d("InscResonse", response.toString());
                                   // if (!response.equals("Erreur")) {
                                   try {

                                       JSONObject result = new JSONObject(response);
                                       int responseCode = result.getInt("responseCode");
                                       if (responseCode == 0) {
                                           Toast.makeText(CodeOtpVerification.this, "Code envoyé par sms", Toast.LENGTH_SHORT).show();
                                           progressDialog.dismiss();
                                           timer = new CountDownTimer(120000, 1000) {
                                               @Override
                                               public void onTick(long l) {
                                                   timerBeforeResendCode.setText("dans "+ l/1000);
                                               }

                                               @Override
                                               public void onFinish() {
                                                   timerBeforeResendCode.setText("maintenant");
                                                   renvoyerCodeOtp.setClickable(true);
                                                   renvoyerCodeOtp.setEnabled(true);
                                               }
                                           }.start();
                                           renvoyerCodeOtp.setClickable(false);
                                           renvoyerCodeOtp.setEnabled(false);


                                       } else {
                                           progressDialog.dismiss();
                                           Toast.makeText(CodeOtpVerification.this, "Erreur survenue", Toast.LENGTH_SHORT).show();
                                           timer = new CountDownTimer(120000, 1000) {
                                               @Override
                                               public void onTick(long l) {
                                                   timerBeforeResendCode.setText("dans "+ l/1000);
                                               }

                                               @Override
                                               public void onFinish() {
                                                   timerBeforeResendCode.setText("maintenant");
                                                   renvoyerCodeOtp.setClickable(true);
                                                   renvoyerCodeOtp.setEnabled(true);
                                               }
                                           }.start();
                                           renvoyerCodeOtp.setClickable(false);
                                           renvoyerCodeOtp.setEnabled(false);

                                       }

                                   } catch (JSONException e) {
                                       e.printStackTrace();
                                   }
                               }
                           },
                           new Response.ErrorListener() {
                               @Override
                               public void onErrorResponse(VolleyError error) {
                                   progressDialog.dismiss();
                                   // error
                                   //Log.e("Error.inscription", String.valueOf(error.getMessage()));
                                   boolean tokenRefreshed = false;
                                   if (error instanceof AuthFailureError && !tokenRefreshed) {
                                       // Rafraîchir le token et réessayer
                                       refreshAccessToken(CodeOtpVerification.this, new CodeOtpVerification.TokenRefreshListener() {
                                           @Override
                                           public void onTokenRefreshed(boolean success) {
                                               if (success) {

                                               }
                                           }
                                       });
                                   }else {
                                       String message = "Une erreur est survenue! Veuillez réessayer svp.";
                                       Snackbar snackbar = Snackbar
                                               .make(mainLayout, message, Snackbar.LENGTH_INDEFINITE)
                                               .setAction("OK", new View.OnClickListener() {
                                                   @Override
                                                   public void onClick(View view) {

                                                   }
                                               });
                                       snackbar.getView().setBackgroundColor(ContextCompat.getColor(CodeOtpVerification.this, R.color.colorGray));
                                       // Changing message text color
                                       snackbar.setActionTextColor(getResources().getColor(R.color.colorPrimaryDark));
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
                           //Date currentDate = new Date();
                           Map<String, String> params = new HashMap<String, String>();
                           params.put("number", numero);
                           params.put("merchantNumber","");
                           params.put("token", accessToken);
                           params.put("type_operation", OperationTypeEnum.INSCRIPTION.toString());
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
                           50000,
                           -1,
                           DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
                   queue.add(postRequest);

                   progressDialog = new ProgressDialog(CodeOtpVerification.this);
                   progressDialog.setCancelable(false);
                   progressDialog.setMessage("Veuillez patienter SVP! \n Vérification du numero.");
                   progressDialog.show();
               }
               else if(caller_activity.equals(callable_activity_carte))
               {
                   RequestQueue queue = Volley.newRequestQueue(CodeOtpVerification.this);
                   StringRequest postRequest = new StringRequest(Request.Method.POST, Constantes.URL_GENERATE_OTP,
                           new Response.Listener<String>()
                           {
                               @SuppressLint("ResourceAsColor")
                               @Override
                               public void onResponse(String response) {
                                   Log.d("ResCarRenvC", response);
                                   // if (!response.equals("Erreur")) {
                                   try {

                                       JSONObject result = new JSONObject(response);
                                       if(result.getInt("responseCode") == 0 )
                                       {
                                           Toast.makeText(CodeOtpVerification.this, "Code envoyé par sms", Toast.LENGTH_SHORT).show();
                                           progressDialog.dismiss();
                                           timer = new CountDownTimer(120000, 1000) {
                                               @Override
                                               public void onTick(long l) {
                                                   timerBeforeResendCode.setText("dans "+ l/1000);
                                               }

                                               @Override
                                               public void onFinish() {
                                                   timerBeforeResendCode.setText("maintenant");
                                                   renvoyerCodeOtp.setClickable(true);
                                                   renvoyerCodeOtp.setEnabled(true);
                                               }
                                           }.start();
                                           renvoyerCodeOtp.setClickable(false);
                                           renvoyerCodeOtp.setEnabled(false);

                                       }
                                       else
                                       {
                                           progressDialog.dismiss();
                                           Toast.makeText(CodeOtpVerification.this, "Erreur survenue", Toast.LENGTH_SHORT).show();
                                           timer = new CountDownTimer(120000, 1000) {
                                               @Override
                                               public void onTick(long l) {
                                                   timerBeforeResendCode.setText("dans "+ l/1000);
                                               }

                                               @Override
                                               public void onFinish() {
                                                   timerBeforeResendCode.setText("maintenant");
                                                   renvoyerCodeOtp.setClickable(true);
                                                   renvoyerCodeOtp.setEnabled(true);
                                               }
                                           }.start();
                                           renvoyerCodeOtp.setClickable(false);
                                           renvoyerCodeOtp.setEnabled(false);
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
                                   progressDialog.dismiss();
                                   Log.e("ErrCarteRevC", String.valueOf(volleyError.getMessage()));
                                   Log.e("Stack", "Error StackTrace: \t" + volleyError.getStackTrace());
                                   // error
                                   //Log.d("Error.Inscription", String.valueOf(error.getMessage()));

                                   // volleyError.getMessage() == null
                                   String message;
                                   if (volleyError instanceof NetworkError || volleyError instanceof AuthFailureError || volleyError instanceof TimeoutError) {
                                       //Toast.makeText(Inscription.this, "error:"+volleyError.getMessage(), Toast.LENGTH_SHORT).show();
                                       //Log.d("VolleyError_Test",volleyError.getMessage());
                                       message = "Aucune connexion Internet! Patientez et réessayez.";
                                       final Snackbar snackbar = Snackbar
                                               .make(mainLayout, message, Snackbar.LENGTH_INDEFINITE)
                                               .setAction("Ok", new View.OnClickListener() {
                                                   @Override
                                                   public void onClick(View view) {
                                                       refreshAccessToken(CodeOtpVerification.this, new CodeOtpVerification.TokenRefreshListener() {
                                                           @Override
                                                           public void onTokenRefreshed(boolean success) {
                                                               if (success) {
                                                                   Log.e("Token mise à jour", "On peut réessayer");
                                                               }
                                                           }
                                                       });
                                                   }
                                               });
                                       snackbar.getView().setBackgroundColor(ContextCompat.getColor(CodeOtpVerification.this, R.color.colorGray));
                                       // Changing message text color
                                       snackbar.setActionTextColor(getResources().getColor(R.color.colorPrimaryDark));
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
                                                       Log.e("Le code d'erreur", volleyError.getCause().toString());
                                                       retrait_mmo(numero,montant);
                                                   }
                                               });
                                       snackbar.getView().setBackgroundColor(ContextCompat.getColor(CodeOtpVerification.this, R.color.colorGray));
                                       // Changing message text color
                                       snackbar.setActionTextColor(getResources().getColor(R.color.colorPrimaryDark));
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
                                                       //retrait_mmo(numero,montant);
                                                   }
                                               });
                                       snackbar.getView().setBackgroundColor(ContextCompat.getColor(CodeOtpVerification.this, R.color.colorGray));
                                       // Changing message text color
                                       snackbar.setActionTextColor(getResources().getColor(R.color.colorPrimaryDark));
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

                           Map<String, String> params = new HashMap<String, String>();
                           params.put("number", numero);
                           params.put("type_operation", OperationTypeEnum.WITHDRAW_FROM_CUSTOMER.toString());
//                           params.put("id_tontine", String.valueOf(id_tontine));
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
                           50000,
                           -1,
                           DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
                   queue.add(postRequest);


                   progressDialog = new ProgressDialog(CodeOtpVerification.this);
                   progressDialog.setCancelable(false);
                   progressDialog.setMessage("Veuillez patienter SVP! \nTraitement de la requête en cours.");
                   progressDialog.show();
               }
               else if(caller_activity.equals(callable_activity_connexion)){
                   RequestQueue queue = Volley.newRequestQueue(CodeOtpVerification.this);
                   StringRequest postRequest = new StringRequest(Request.Method.POST, Constantes.URL_GENERATE_OTP,
                           new Response.Listener<String>() {
                               @SuppressLint("ResourceAsColor")
                               @Override
                               public void onResponse(String response) {
                                   Log.d("ResponseTagMain", response);
                                   // if (!response.equals("Erreur")) {
                                   try {

                                       JSONObject result = new JSONObject(response);
                                       if (result.getInt("responseCode") == 0) {
                                           progressDialog.dismiss();
                                           Toast.makeText(CodeOtpVerification.this, "Code envoyé par sms", Toast.LENGTH_SHORT).show();
                                           progressDialog.dismiss();
                                           timer = new CountDownTimer(120000, 1000) {
                                               @Override
                                               public void onTick(long l) {
                                                   timerBeforeResendCode.setText("dans "+ l/1000);
                                               }

                                               @Override
                                               public void onFinish() {
                                                   timerBeforeResendCode.setText("maintenant");
                                                   renvoyerCodeOtp.setClickable(true);
                                                   renvoyerCodeOtp.setEnabled(true);
                                               }
                                           }.start();
                                           renvoyerCodeOtp.setClickable(false);
                                           renvoyerCodeOtp.setEnabled(false);


                                       } else {
                                           progressDialog.dismiss();
                                           Toast.makeText(CodeOtpVerification.this, "Erreur survenue", Toast.LENGTH_SHORT).show();
                                           timer = new CountDownTimer(120000, 1000) {
                                               @Override
                                               public void onTick(long l) {
                                                   timerBeforeResendCode.setText("dans "+ l/1000);
                                               }

                                               @Override
                                               public void onFinish() {
                                                   timerBeforeResendCode.setText("maintenant");
                                                   renvoyerCodeOtp.setClickable(true);
                                                   renvoyerCodeOtp.setEnabled(true);
                                               }
                                           }.start();
                                           renvoyerCodeOtp.setClickable(false);
                                           renvoyerCodeOtp.setEnabled(false);
                                       }

                                   } catch (JSONException e) {
                                       e.printStackTrace();
                                   }
                               }
                           },
                           new Response.ErrorListener() {
                               @Override
                               public void onErrorResponse(VolleyError error) {
                                   progressDialog.dismiss();
                                   // error
                                   Log.e("Error.CodeOtp", String.valueOf(error.getMessage()));
                                   String message = "Une erreur est survenue! Veuillez réessayer svp1.";
                                   Snackbar snackbar = Snackbar
                                           .make(mainLayout, message, Snackbar.LENGTH_INDEFINITE)
                                           .setAction("OK", new View.OnClickListener() {
                                               @Override
                                               public void onClick(View view) {

                                               }
                                           });
                                   snackbar.getView().setBackgroundColor(ContextCompat.getColor(CodeOtpVerification.this, R.color.colorGray));
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
                           params.put("number", numero);
                           params.put("type_operation", OperationTypeEnum.ACCES.toString());
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
                           50000,
                           -1,
                           DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
                   queue.add(postRequest);

                   progressDialog = new ProgressDialog(CodeOtpVerification.this);
                   progressDialog.setCancelable(false);
                   progressDialog.setMessage("Veuillez patienter SVP! \n Renvoie du code OTP.");
                   progressDialog.show();
               }
           }
       });




        button_annulerCodeOtp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Dialog dialog_alert = new Dialog(CodeOtpVerification.this);
                dialog_alert.requestWindowFeature(Window.FEATURE_NO_TITLE);
                dialog_alert.setCancelable(true);
                dialog_alert.setContentView(R.layout.dialog_attention);

                TextView titre = (TextView) dialog_alert.findViewById(R.id.deco_title);
                titre.setText("Attention");
                TextView message_deco = (TextView) dialog_alert.findViewById(R.id.deco_message);
                message_deco.setText("Êtes-vous sur d'annuler cette opération");

                Button oui = (Button) dialog_alert.findViewById(R.id.btn_oui);
                oui.setText("Oui");
                oui.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog_alert.dismiss();

                        if(caller_activity.equals(callable_activity_carte)) {
                            // CodeOtpVerification.this.finish();
                            Intent intent = new Intent(CodeOtpVerification.this, CarteMain.class);
                            intent.putExtra("id_tontine", id_tontine);
                            startActivity(intent);
                        }
                        else if(caller_activity.equals(callable_activity_smsReceiver))
                        {
                            //Dans ce cas il faut envoyer un msg au marchand pour annuler l'opération. et donc fermer son dialogProgress
                            CodeOtpVerification.this.finish();
                            Intent intent = new Intent(CodeOtpVerification.this, MesTontines.class);
                            startActivity(intent);
                        }
                        else if(caller_activity.equals(callable_activity_inscription))
                        {
                            //Envoyer une requete vers le serveur pour annuler le code otp
                            //annuler_code_otp(codeOtpRecu,  numero);
                            CodeOtpVerification.this.finish();
                            Intent intent = new Intent(CodeOtpVerification.this, Inscription.class);
                            intent.putExtra("nom", nom);
                            intent.putExtra("prenoms", prenoms);
                            intent.putExtra("tel", numero);
                            intent.putExtra("mdp", mdp);
                            intent.putExtra("cnf_mdp", mdp);
                            startActivity(intent);
                        } else if(caller_activity.equals(callable_activity_connexion)){
                            Prefs.putString(ID_UTILISATEUR_KEY, null);
                            CodeOtpVerification.this.finish();
                            Intent intent = new Intent(CodeOtpVerification.this, Connexion.class);
                            startActivity(intent);
                        }
                    }
                });

                Button non = (Button) dialog_alert.findViewById(R.id.btn_non);
                non.setText("Non");
                non.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        dialog_alert.dismiss();
                    }
                });

                dialog_alert.show();

//                AlertDialog.Builder dialog = new AlertDialog.Builder(CodeOtpVerification.this);
//                dialog.setTitle( "Attention" )
//                        .setIcon(R.drawable.ic_warning)
//                        .setMessage("Êtes-vous sur d'annuler l'opération de retrait?")
//                        .setNegativeButton("Non", new DialogInterface.OnClickListener() {
//                            public void onClick(DialogInterface dialoginterface, int i) {
//                                dialoginterface.cancel();
//                            }})
//                        .setPositiveButton("Oui", new DialogInterface.OnClickListener() {
//                            public void onClick(DialogInterface dialoginterface, int i) {
//                                if(caller_activity.equals(callable_activity_carte)) {
//                                   // CodeOtpVerification.this.finish();
//                                    Intent intent = new Intent(CodeOtpVerification.this, CarteMain.class);
//                                    intent.putExtra("id_tontine", id_tontine);
//                                    startActivity(intent);
//                                }
//                                else if(caller_activity.equals(callable_activity_smsReceiver))
//                                {
//                                    //Dans ce cas il faut envoyer un msg au marchand pour annuler l'opération. et donc fermer son dialogProgress
//                                    CodeOtpVerification.this.finish();
//                                    Intent intent = new Intent(CodeOtpVerification.this, MesTontines.class);
//                                    startActivity(intent);
//                                }
//                                else if(caller_activity.equals(callable_activity_inscription))
//                                {
//                                    //Envoyer une requete vers le serveur pour annuler le code otp
//                                    //annuler_code_otp(codeOtpRecu,  numero);
//                                    CodeOtpVerification.this.finish();
//                                    Intent intent = new Intent(CodeOtpVerification.this, Inscription.class);
//                                    startActivity(intent);
//                                }
//                            }
//                        }).show();
            }
        });

        button_validerCodeOtp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String codeSaisit = String.valueOf(editText_codeOtp.getText());
                if(!codeSaisit.equals(""))
                {
                    if (codeSaisit.length() != 6)
                    {
                        Snackbar snackbar = Snackbar
                                .make(mainLayout, "Erreur, code invalide", Snackbar.LENGTH_INDEFINITE)
                                .setAction("OK", new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {

                                    }
                                });
                        snackbar.show();
                    }
                    else {
                        if(caller_activity.equals(callable_activity_carte)) {RequestQueue queue = Volley.newRequestQueue(CodeOtpVerification.this);

                            JSONObject jsonBody = new JSONObject();
                            try {
                                jsonBody.put("code", codeSaisit);
                                jsonBody.put("type_operation", OperationTypeEnum.WITHDRAW_FROM_CUSTOMER.toString());
                                jsonBody.put("numberClient", Prefs.getString(TEL_KEY, ""));
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }

                            JsonObjectRequest jsonRequest = new JsonObjectRequest(
                                    Request.Method.POST,
                                    Constantes.URL_VERIFY_OTP,
                                    jsonBody,
                                    new Response.Listener<JSONObject>() {
                                        @Override
                                        public void onResponse(JSONObject response) {
                                            Log.e("ResVerifCodCar", response.toString());
                                            try {
                                                int responseCode = response.getInt("responseCode");
                                                String bodyString = response.getString("body");
                                                Log.e("jsonObjt", response.toString());
                                                if (responseCode == 0) {
                                                    progressDialog.dismiss();
                                                    retrait_mmo(numero, montant);
                                                } else {
                                                    progressDialog.dismiss();
                                                    CodeOtpVerification.this.finish();
                                                    Intent intent = new Intent(CodeOtpVerification.this, Message_non.class);
                                                    intent.putExtra("msg_desc", bodyString);
                                                    intent.putExtra("id_tontine", id_tontine);
                                                    startActivity(intent);
                                                }
                                            } catch (JSONException e) {
                                                e.printStackTrace();
                                                Log.e("erreurCodOtp", e.getMessage());
                                            }
                                        }
                                    },
                                    new Response.ErrorListener() {
                                        @Override
                                        public void onErrorResponse(VolleyError error) {
                                            progressDialog.dismiss();
                                            String message = "Une erreur est survenue! Veuillez réessayer svp.";
                                            Snackbar snackbar = Snackbar
                                                    .make(mainLayout, message, Snackbar.LENGTH_INDEFINITE)
                                                    .setAction("OK", new View.OnClickListener() {
                                                        @Override
                                                        public void onClick(View view) {
                                                            refreshAccessToken(CodeOtpVerification.this, new TokenRefreshListener() {
                                                                @Override
                                                                public void onTokenRefreshed(boolean success) {
                                                                    if (success) {
                                                                       // queue.add(jsonRequest);
                                                                    }
                                                                }
                                                            });
                                                        }
                                                    });
                                            snackbar.getView().setBackgroundColor(ContextCompat.getColor(CodeOtpVerification.this, R.color.colorGray));
                                            snackbar.setActionTextColor(getResources().getColor(R.color.colorPrimary));
                                            View sbView = snackbar.getView();
                                            TextView textView = sbView.findViewById(com.google.android.material.R.id.snackbar_text);
                                            textView.setTextColor(Color.WHITE);
                                            snackbar.show();
                                        }
                                    }) {
                                @Override
                                public Map<String, String> getHeaders() throws AuthFailureError {
                                    Map<String, String> headers = new HashMap<>();
                                    headers.put("Content-Type", "application/json");
                                    headers.put("Authorization", "Bearer " + accessToken); // Ajoute le token ici
                                    return headers;
                                }
                            };

                            jsonRequest.setRetryPolicy(new DefaultRetryPolicy(
                                    50000,
                                    -1,
                                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
                            queue.add(jsonRequest);

                            progressDialog = new ProgressDialog(CodeOtpVerification.this);
                            progressDialog.setCancelable(false);
                            progressDialog.setMessage("Veuillez patienter SVP! \n Vérification du code.");
                            progressDialog.show();

//                            progressDialog.show();
                        }
                        else if(caller_activity.equals(callable_activity_smsReceiver))
                        {
                            RequestQueue queue = Volley.newRequestQueue(CodeOtpVerification.this);
                            StringRequest postRequest = new StringRequest(Request.Method.POST, Constantes.URL_VERIFY_OTP,
                                    new Response.Listener<String>() {
                                        @SuppressLint("ResourceAsColor")
                                        @Override
                                        public void onResponse(String response) {
                                            Log.e("ResSmsReceiver", response);
                                            // if (!response.equals("Erreur")) {
                                            try {

                                                JSONObject result = new JSONObject(response);
                                                if (result.getBoolean("success")) {
                                                    progressDialog.dismiss();
                                                   Log.e("retrait_espece_otp", "En attente du message de confirmation du marchand");
                                                   Toast.makeText(CodeOtpVerification.this, "En attente de confirmation du marchand", Toast.LENGTH_SHORT).show();
                                                    button_validerCodeOtp.setEnabled(false);
                                                    button_annulerCodeOtp.setEnabled(false);
                                                    billetCodeRetraitEspece = true;
                                                } else {
                                                    progressDialog.dismiss();
                                                    Intent i = new Intent(CodeOtpVerification.this, Message_non.class);
                                                    i.putExtra("msg_desc", result.getString("data"));
                                                    i.putExtra("class", "com.sicmagroup.tondi.MesTontines");
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
                                            progressDialog.dismiss();
                                            // error
                                            Log.e("Error.receive.smsrappel", String.valueOf(error.getMessage()));
                                            String message = "Erreur veuillez remprendre l'operation.";
                                            Snackbar snackbar = Snackbar
                                                    .make(mainLayout, message, Snackbar.LENGTH_INDEFINITE)
                                                    .setAction("REESSAYER", new View.OnClickListener() {
                                                        @Override
                                                        public void onClick(View view) {
                                                            refreshAccessToken(CodeOtpVerification.this, new CodeOtpVerification.TokenRefreshListener() {
                                                                @Override
                                                                public void onTokenRefreshed(boolean success) {
                                                                    if (success) {

                                                                    }
                                                                }
                                                            });
                                                        }
                                                    });
                                            snackbar.getView().setBackgroundColor(ContextCompat.getColor(CodeOtpVerification.this, R.color.colorGray));
                                            // Changing message text color
                                            snackbar.setActionTextColor(getResources().getColor(R.color.colorPrimary));
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
                                    params.put("code", codeSaisit);
                                    params.put("token", token);
                                    params.put("numberMerchant", numero_marchand);
                                    params.put("numberClient", numero);
                                    params.put("type_operation", OperationTypeEnum.WITHDRAW_FROM_CUSTOMER.toString());
                                    // params.put("verification_date", android.text.format.DateFormat.format("yyyy-MM-dd H:i:m", currentDate.getTime()).toString());
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

                            progressDialog = new ProgressDialog(CodeOtpVerification.this);
                            progressDialog.setCancelable(false);
                            progressDialog.setMessage("Veuillez patienter SVP! \n Vérification du code.");
                            progressDialog.show();
                        }
                        else if(caller_activity.equals(callable_activity_inscription)) {
                            RequestQueue queue = Volley.newRequestQueue(CodeOtpVerification.this);

                            JSONObject jsonBody = new JSONObject();
                            try {
                                jsonBody.put("code", codeSaisit);
                                jsonBody.put("numberClient", numero);
                                jsonBody.put(Constantes.TYPE_OP_KEY, OperationTypeEnum.INSCRIPTION.toString());
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }

                            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                                    Request.Method.POST,
                                    Constantes.URL_VERIFY_OTP,
                                    jsonBody,
                                    new Response.Listener<JSONObject>() {
                                        @SuppressLint("ResourceAsColor")
                                        @Override
                                        public void onResponse(JSONObject response) {
                                            Log.e("ResInscr", response.toString());
                                            try {
                                                int responseCode = response.getInt("responseCode");
                                                String bodyString = response.getString("body");
                                                Log.e("jsonObjt", response.toString());
                                                if (responseCode == 0) {
                                                    progressDialog.dismiss();
                                                    Log.d("inscription", "validation du code otp pour poursuivre insc");
                                                    inscrire();
                                                } else {
                                                    progressDialog.dismiss();
                                                    Intent i = new Intent(CodeOtpVerification.this, Message_non.class);
                                                    i.putExtra("msg_desc", bodyString);
                                                    i.putExtra("class", "com.sicmagroup.tondi.Inscription");
                                                    i.putExtra("mmi", "1");
                                                    startActivity(i);
                                                }
                                            } catch (JSONException e) {
                                                e.printStackTrace();
                                                Log.e("erreurCodOtp", e.getMessage());
                                            }
                                        }
                                    },
                                    new Response.ErrorListener() {
                                        @Override
                                        public void onErrorResponse(VolleyError error) {
                                            progressDialog.dismiss();
                                            String message = "Une erreur est survenue! Veuillez réessayer svp.";
                                            Snackbar snackbar = Snackbar
                                                    .make(mainLayout, message, Snackbar.LENGTH_INDEFINITE)
                                                    .setAction("OK", new View.OnClickListener() {
                                                        @Override
                                                        public void onClick(View view) {
                                                        }
                                                    });
                                            snackbar.getView().setBackgroundColor(ContextCompat.getColor(CodeOtpVerification.this, R.color.colorGray));
                                            snackbar.setActionTextColor(getResources().getColor(R.color.colorPrimary));
                                            View sbView = snackbar.getView();
                                            TextView textView = sbView.findViewById(com.google.android.material.R.id.snackbar_text);
                                            textView.setTextColor(Color.WHITE);
                                            snackbar.show();
                                        }
                                    }) {
                                @Override
                                public Map<String, String> getHeaders() throws AuthFailureError {
                                    Map<String, String> headers = new HashMap<>();
                                    headers.put("Content-Type", "application/json");
                                    headers.put("Authorization", "Bearer " + accessToken); // Ajoute le token ici
                                    return headers;
                                }
                            };

                            jsonObjectRequest.setRetryPolicy(new DefaultRetryPolicy(
                                    25000,
                                    -1,
                                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
                            queue.add(jsonObjectRequest);

                            progressDialog = new ProgressDialog(CodeOtpVerification.this);
                            progressDialog.setCancelable(false);
                            progressDialog.setMessage("Veuillez patienter SVP! \n Vérification du code.");
                            progressDialog.show();

                        }else if(caller_activity.equals(callable_activity_connexion)){
                            RequestQueue queue = Volley.newRequestQueue(CodeOtpVerification.this);
                            StringRequest postRequest = new StringRequest(Request.Method.POST, Constantes.URL_VERIFY_OTP,
                                    new Response.Listener<String>() {
                                        @SuppressLint("ResourceAsColor")
                                        @Override
                                        public void onResponse(String response) {
                                            Log.e("ResInscr", response);
                                            // if (!response.equals("Erreur")) {
                                            try {

                                                JSONObject result = new JSONObject(response);
                                                Log.e("jsonObjt", result.toString());
                                                if (result.getBoolean("success")) {
                                                    CodeOtpVerification.this.finish();
                                                    progressDialog.dismiss();
                                                    Log.d("code otp", "validation du code otp pour poursuivre insc");
                                                    startActivity(new Intent(CodeOtpVerification.this,MdpModification.class));

                                                } else {
                                                    progressDialog.dismiss();
                                                    //CodeOtpVerification.this.finish();
                                                    Intent i = new Intent(CodeOtpVerification.this, Message_non.class);
                                                    i.putExtra("msg_desc", result.getString("message"));
                                                    i.putExtra("class", "com.sicmagroup.tondi.Inscription");
                                                    i.putExtra("mmi","1");
                                                    startActivity(i);
                                                }
                                            } catch (JSONException e) {
                                                e.printStackTrace();
                                                Log.e("erreurCodOtp", e.getMessage());
                                            }
                                        }
                                    },
                                    new Response.ErrorListener() {
                                        @Override
                                        public void onErrorResponse(VolleyError error) {
                                            progressDialog.dismiss();
                                            // error
                                            String message = "Une erreur est survenue! Veuillez réessayer svp.";
                                            Snackbar snackbar = Snackbar
                                                    .make(mainLayout, message, Snackbar.LENGTH_INDEFINITE)
                                                    .setAction("OK", new View.OnClickListener() {
                                                        @Override
                                                        public void onClick(View view) {
                                                            refreshAccessToken(CodeOtpVerification.this, new CodeOtpVerification.TokenRefreshListener() {
                                                                @Override
                                                                public void onTokenRefreshed(boolean success) {
                                                                    if (success) {

                                                                    }
                                                                }
                                                            });
                                                        }
                                                    });
                                            snackbar.getView().setBackgroundColor(ContextCompat.getColor(CodeOtpVerification.this, R.color.colorGray));
                                            // Changing message text color
                                            snackbar.setActionTextColor(getResources().getColor(R.color.colorPrimary));
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
                                    params.put("code", codeSaisit);
                                    params.put("numberClient", numero);
                                    params.put("type_operation", OperationTypeEnum.ACCES.toString());
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

                            progressDialog = new ProgressDialog(CodeOtpVerification.this);
                            progressDialog.setCancelable(false);
                            progressDialog.setMessage("Veuillez patienter SVP! \n Vérification du code en cours.");
                            progressDialog.show();
                        }
                    }
                }
                else
                {
                    Snackbar snackbar = Snackbar
                            .make(mainLayout, "Erreur, remplissez le champ.", Snackbar.LENGTH_INDEFINITE)
                            .setAction("OK", new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    refreshAccessToken(CodeOtpVerification.this, new CodeOtpVerification.TokenRefreshListener() {
                                        @Override
                                        public void onTokenRefreshed(boolean success) {
                                            if (success) {

                                            }
                                        }
                                    });
                                }
                            });
                    snackbar.show();
                }
            }
        });

        editText_codeOtp.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(count == 6)
                {
                    button_validerCodeOtp.performClick();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

      /*  if (!codeOtpRecu.equals(""))
        {
            editText_codeOtp.setText(codeOtpRecu);
            button_validerCodeOtp.performClick();
        }*/
    }

    @Override
    public void onBackPressed() {
        AlertDialog.Builder dialog = new AlertDialog.Builder(CodeOtpVerification.this);
        dialog.setTitle( "Attention" )
                .setIcon(R.drawable.ic_warning)
                .setMessage("Êtes-vous sur d'annuler l'opération en cours?")
                .setNegativeButton("Non", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialoginterface, int i) {
                        dialoginterface.cancel();
                    }})
                .setPositiveButton("Oui", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialoginterface, int i) {
                        if(caller_activity.equals(callable_activity_carte)) {
                           // CodeOtpVerification.this.finish();
                            Intent intent = new Intent(CodeOtpVerification.this, CarteMain.class);
                            intent.putExtra("id_tontine", id_tontine);
                            startActivity(intent);
                        }
                        else if(caller_activity.equals(callable_activity_smsReceiver))
                        {
                            if(!billetCodeRetraitEspece)
                            {
                                //CodeOtpVerification.this.finish();
                                Intent intent = new Intent(CodeOtpVerification.this, MesTontines.class);
                                startActivity(intent);
                            }
                            else
                            {
                                Toast.makeText(CodeOtpVerification.this, "Attention, patientez la fin de l'opération", Toast.LENGTH_SHORT).show();
                            }
                            
                        }
                        else if(caller_activity.equals(callable_activity_inscription))
                        {
                            //Envoyer une requete vers le serveur pour annuler le code otp
                            //annuler_code_otp(codeOtpRecu,  numero);
                           // CodeOtpVerification.this.finish();
                            CodeOtpVerification.this.finish();
                            Intent intent = new Intent(CodeOtpVerification.this, Inscription.class);
                            intent.putExtra("nom", nom);
                            intent.putExtra("prenoms", prenoms);
                            intent.putExtra("tel", numero);
                            intent.putExtra("mdp", mdp);
                            intent.putExtra("cnf_mdp", mdp);
                            startActivity(intent);
                        }
                        else if(caller_activity.equals(callable_activity_connexion)){
                            CodeOtpVerification.this.finish();
                            Intent intent = new Intent(CodeOtpVerification.this, Connexion.class);
                            Prefs.putString(ID_UTILISATEUR_KEY, null);
                            startActivity(intent);
                        }
                    }
                }).show();


    }

    public void annuler_code_otp(final String codeOtp, final String numero)
    {
        RequestQueue queue = Volley.newRequestQueue(CodeOtpVerification.this);
        StringRequest postRequest = new StringRequest(Request.Method.POST, url_disable_code_otp,
                new Response.Listener<String>() {
                    @SuppressLint("ResourceAsColor")
                    @Override
                    public void onResponse(String response) {
                        Log.e("ResponseAnnuler", response);
                        // if (!response.equals("Erreur")) {
                        try {

                            JSONObject result = new JSONObject(response);
                            if (result.getBoolean("success")) {
                                progressDialog.dismiss();
                                if(caller_activity.equals(callable_activity_inscription))
                                {
                                    CodeOtpVerification.this.finish();
                                    Intent intent = new Intent(CodeOtpVerification.this, Message_ok.class);
                                    intent.putExtra("msg_desc", "Inscription annulée");
                                    intent.putExtra("class","com.sicmagroup.tondi.Inscription");

                                    startActivity(intent);
                                }
                                else if(caller_activity.equals(callable_activity_carte))
                                {
                                    CodeOtpVerification.this.finish();
                                    Intent intent = new Intent(CodeOtpVerification.this, Message_ok.class);
                                    intent.putExtra("id_tontine", (int) id_tontine);
                                    intent.putExtra("msg_desc", "Opération annulée");
                                    startActivity(intent);
                                }
                                else if(caller_activity.equals(callable_activity_smsReceiver))
                                {
                                    CodeOtpVerification.this.finish();
                                    Intent intent = new Intent(CodeOtpVerification.this, Message_ok.class);
                                    intent.putExtra("msg_desc", "Opération annulée");
                                    intent.putExtra("class","com.sicmagroup.tondi.MesTontines");
                                    startActivity(intent);
                                }

                            } else {
                                progressDialog.dismiss();
                                if(caller_activity.equals(callable_activity_inscription))
                                {
                                    CodeOtpVerification.this.finish();
                                    Intent i = new Intent(CodeOtpVerification.this, Message_non.class);
                                    i.putExtra("msg_desc", "Opération échouée");
                                    i.putExtra("mmi","1");
                                    i.putExtra("class","com.sicmagroup.tondi.Inscription");
                                    startActivity(i);
                                }
                                else if(caller_activity.equals(callable_activity_carte))
                                {
                                    CodeOtpVerification.this.finish();
                                    Intent i = new Intent(CodeOtpVerification.this, Message_non.class);
                                    i.putExtra("msg_desc", "Opération échouée");
                                    i.putExtra("class","com.sicmagroup.tondi.MesTontines");
                                    startActivity(i);
                                }
                                else if(caller_activity.equals(callable_activity_smsReceiver))
                                {
                                    CodeOtpVerification.this.finish();
                                    Intent i = new Intent(CodeOtpVerification.this, Message_non.class);
                                    i.putExtra("msg_desc", "Opération échouée");
                                    i.putExtra("class","com.sicmagroup.tondi.MesTontines");
                                    startActivity(i);
                                }

                            }


                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        progressDialog.dismiss();
                        // error
                        //Log.e("Error.receive.codeotp", String.valueOf(error.getMessage()));
                        String message = "Une erreur est survenue! Veuillez réessayer svp.";
                        Snackbar snackbar = Snackbar
                                .make(mainLayout, message, Snackbar.LENGTH_INDEFINITE)
                                .setAction("OK", new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {

                                    }
                                });
                        snackbar.getView().setBackgroundColor(ContextCompat.getColor(CodeOtpVerification.this, R.color.colorGray));
                        // Changing message text color
                        snackbar.setActionTextColor(getResources().getColor(R.color.colorPrimary));
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
                params.put("code_otp", codeOtp);
                params.put("numero", numero);
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

        progressDialog = new ProgressDialog(CodeOtpVerification.this);
        progressDialog.setCancelable(false);
        progressDialog.setMessage("Veuillez patienter SVP! \n Annulation du code.");
        progressDialog.show();
    }

    private void refreshAccessToken(Context context, TokenRefreshListener listener) {
        RequestQueue queue = Volley.newRequestQueue(context);
        JSONObject params = new JSONObject();
        try {
            params.put("refreshToken", Prefs.getString(REFRESH_TOKEN, ""));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        JsonObjectRequest refreshRequest = new JsonObjectRequest(Request.Method.POST, Constantes.url_refresh_token, params,
                new Response.Listener<JSONObject>() {
                    @SuppressLint("LongLogTag")
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.e("C'est dans le refresh token", "Oui");
                        try {
                            Log.e("La réponse du refresh token est:", response.toString());
                            String newAccessToken = response.getString("token");
                            String newRefreshToken = response.getString("refreshToken");
                            accessToken = newAccessToken;
                            Prefs.putString(TOKEN, newAccessToken);
                            Prefs.putString(REFRESH_TOKEN, newRefreshToken);
                            Log.d("RefreshToken", "New Token: " + newAccessToken);
                            listener.onTokenRefreshed(true);
                        } catch (JSONException e) {
                            e.printStackTrace();
                            listener.onTokenRefreshed(false);
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {
                        Log.e("RefreshToken", "Error: " + volleyError.getMessage());
                        listener.onTokenRefreshed(false);
                    }
                });

        queue.add(refreshRequest);
    }

    public interface TokenRefreshListener {
        void onTokenRefreshed(boolean refreshed);
    }

    private void inscrire() {

        RequestQueue queue = Volley.newRequestQueue(this);
        JSONObject jsonObject;
        jsonObject = new JSONObject();
        try {
            SexeEnum sexeEnum;

            jsonObject.put("firstName", Prefs.getString(PRENOMS_KEY,""));
            jsonObject.put("lastName", Prefs.getString(NOM_KEY,""));
            jsonObject.put("numero", numero);
            jsonObject.put("password", mdp);
            if(sexe.equals("Homme")){
                jsonObject.put("sexe", SexeEnum.MALE);
            } else if(sexe.equals("Femme")){
                jsonObject.put("sexe", SexeEnum.FEMALE);
            } else {
                jsonObject.put("sexe", SexeEnum.OTHER);
            }
Log.e("Le body de la connexion",jsonObject.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, Constantes.URL_INSCRIPTION, jsonObject,
                new Response.Listener<JSONObject>()
                {
                    @SuppressLint("ResourceAsColor")
                    @Override
                    public void onResponse(JSONObject response) {
                        //progressDialog.dismiss();
                        // response
                        Log.e("Response", response.toString());

                        try {
                            int id = 0;
                            String customNumber = "";
                            Connexion.AeSimpleSHA1 AeSimpleSHA1 = new Connexion.AeSimpleSHA1();


                            JSONObject result = response;
                            int responseCode = result.getInt("responseCode");
                            String bodyString = result.getString("body");
                            //final JSONArray array = result.getJSONArray("data");
                            //Log.d("My App", obj.toString());
                            if (responseCode == 0){
                               // JSONObject body = new JSONObject(bodyString);
                                // enregistrer en local
                               // Utilisateur nouvel_utilisateur = new Utilisateur();

                                JSONObject body = response.getJSONObject("body");
                                JSONObject user = body.getJSONObject("userDTO");
                                id = Integer.parseInt(user.getString("id"));
                                customNumber = body.getString("username");
                                String user_uuid = user.getString("uuid");
                                String token = body.getString("accessToken");
                                String refreshToken = body.getString("refreshToken");
                                SharedPreferences sharedPreferences = getSharedPreferences("MyPrefs", MODE_PRIVATE);
                                //String nom = sharedPreferences.getString("nom", "");
                                //String prenoms = sharedPreferences.getString("prenoms", "");

                                Log.e("Le customNum: ", customNumber);
                                Log.e("L'id est : ", String.valueOf(id));
                                Log.e("Le token est:", token);
                                //BaseFormElement mdp = mFormBuilder.getFormElement(TAG_PASS);
                                String mot_de_passe = mdp;
                                mot_de_passe = AeSimpleSHA1.md5(mot_de_passe);
                                mot_de_passe = AeSimpleSHA1.SHA1(mot_de_passe);
                                Log.e("Le mot de passe hashé:", mot_de_passe);

                                Utilisateur nouvel_utilisateur = new Utilisateur();
                                nouvel_utilisateur.setId_utilisateur(String.valueOf(id));
                                nouvel_utilisateur.setNumero(customNumber);
                                nouvel_utilisateur.setNom(Prefs.getString(NOM_KEY,""));
                                nouvel_utilisateur.setPrenoms(Prefs.getString(PRENOMS_KEY,""));
                                nouvel_utilisateur.setPin_acces(mot_de_passe);
                                nouvel_utilisateur.setMdp(mot_de_passe);

                                DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                                long timestamp_creation = formatter.parse(user.getString("createdAt")).getTime();
                                long timestamp_maj = formatter.parse(user.getString("updatedAt")).getTime();

                                nouvel_utilisateur.setCreation(timestamp_creation);
                                nouvel_utilisateur.setMaj(timestamp_maj);
                                nouvel_utilisateur.setConnecter_le(timestamp_maj);
                                nouvel_utilisateur.save();

//                                nouvel_utilisateur.setId_utilisateur(body.getString("id"));
//                                nouvel_utilisateur.setNumero(body.getString("numero"));
//                                nouvel_utilisateur.setNom(body.getString("firstName"));
//                                nouvel_utilisateur.setPrenoms(body.getString("lastName"));
////                                nouvel_utilisateur.setPin_acces(user.getString("pin_acces"));
//                                nouvel_utilisateur.setMdp(body.getString("password"));
//                                // maj des dates
//                                @SuppressLint("SimpleDateFormat") DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
//                                Date creation = (Date)formatter.parse(body.getString("createdAt"));
//                                long output_creation=creation.getTime()/1000L;
//                                String str_creation=Long.toString(output_creation);
//                                long timestamp_creation = Long.parseLong(str_creation) * 1000;
//                                Date maj = (Date)formatter.parse(body.getString("updatedAt"));
//                                long output_maj=maj.getTime()/1000L;
//                                String str_maj=Long.toString(output_maj);
//                                long timestamp_maj = Long.parseLong(str_maj) * 1000;
//                                nouvel_utilisateur.setCreation(timestamp_creation);
//                                nouvel_utilisateur.setId_utilisateur(body.getString("id"));
//                                nouvel_utilisateur.setMaj(timestamp_maj);
//                                nouvel_utilisateur.setConnecter_le(timestamp_maj);
//
//                                nouvel_utilisateur.save();

                                Long iD = Long.parseLong(nouvel_utilisateur.getId_utilisateur());
                                //Log.d("nouvel_utilisateur_id", "id:"+String.valueOf(id));
                                if (iD!=null){

//                                    // Mettre à jour la préférence id utilisateur
//                                    Prefs.putString(ID_UTILISATEUR_KEY, String.valueOf(id));
//                                    // Mettre à jour la préférence nom
//                                    Prefs.putString(NOM_KEY, body.getString("firstName"));
//                                    // Mettre à jour la préférence prenoms
//                                    Prefs.putString(PRENOMS_KEY, body.getString("lastName"));
//                                    // Mettre à jour la préférence pin d'accès
////                                    Prefs.putString(PIN_KEY, user.getString("pin_acces"));
//                                    // Mettre à jour la préférence pin d'accès
//                                    Prefs.putString(PASS_KEY, body.getString("password"));
//                                    // Mettre à jour la préférence pin d'accès
////                                    Prefs.putString(CONNECTER_KEY, String.valueOf(timestamp_creation));
//                                    Prefs.putString(TEL_KEY, String.valueOf(body.getString("numero")));
//                                    //Mettre la valeur du sexe
//                                    Prefs.putString(SEXE_KEY, body.getString("sexe"));
////                                    final JSONObject array = result.getJSONObject("data");
//                                    //Inscription.this.finish();

                                    Prefs.putString(TOKEN, token);
                                    Prefs.putString(REFRESH_TOKEN, refreshToken);
                                    Prefs.putString(ID_UTILISATEUR_KEY, user.getString("id"));
                                   // Prefs.putString(NOM_KEY, user.optString("firstName", prenoms));
                                   // Prefs.putString(PRENOMS_KEY, user.optString("lastName", nom));
                                    Prefs.putString(PIN_KEY, mot_de_passe);
                                    Prefs.putString(PASS_KEY, mot_de_passe);
                                    Prefs.putString(CONNECTER_KEY, String.valueOf(timestamp_creation));
                                    Prefs.putString(TEL_KEY, customNumber);
                                    Prefs.putString(UUID_KEY, user_uuid);


                                    Intent inscription_next_intent = new Intent(CodeOtpVerification.this,Inscription_next.class);
                                    inscription_next_intent.putExtra("id_utilisateur", id);
                                    inscription_next_intent.putExtra("user_uuid", user_uuid);
                                    inscription_next_intent.putExtra("accessToken", token);
//                                    try {
//                                        //inscription_next_intent.putExtra("id_utilisateur", body.getString("id"));
//                                        inscription_next_intent.putExtra("id_utilisateur", id);
//                                    } catch (JSONException e) {
//                                        e.printStackTrace();
//                                    }

                                    startActivity(inscription_next_intent);

                                }

                            }else{
                                progressDialog.dismiss();

                                String msg=bodyString;
                                Intent i = new Intent(CodeOtpVerification.this, Message_non.class);
                                i.putExtra("msg_desc",msg);
                                i.putExtra("class","com.sicmagroup.tondi.Inscription");
                                i.putExtra("mmi","1");
                                startActivity(i);
                            }


                        } catch (Throwable t) {
                            Log.d("errornscription",t.getMessage());
                        }


                    }
                },
                new Response.ErrorListener()
                {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {
                        progressDialog.dismiss();


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
                                            refreshAccessToken(CodeOtpVerification.this, new CodeOtpVerification.TokenRefreshListener() {
                                                @Override
                                                public void onTokenRefreshed(boolean success) {
                                                    if (success) {
                                                        inscrire();
                                                    }
                                                }
                                            });

                                        }
                                    });
                            snackbar.getView().setBackgroundColor(ContextCompat.getColor(CodeOtpVerification.this, R.color.colorGray));
                            // Changing message text color
                            snackbar.setActionTextColor(getResources().getColor(R.color.colorPrimary));
                            // Changing action button text color
                            View sbView = snackbar.getView();
                            TextView textView = (TextView) sbView.findViewById(com.google.android.material.R.id.snackbar_text);
                            textView.setTextColor(Color.WHITE);
                            snackbar.show();

                        } else if (volleyError instanceof ServerError) {
                            message = "Impossible de contacter le serveur!";
                            Snackbar snackbar = Snackbar
                                    .make(mainLayout, message, Snackbar.LENGTH_INDEFINITE)
                                    .setAction("REESSAYER", new View.OnClickListener() {
                                        @Override
                                        public void onClick(View view) {
                                            inscrire();
                                        }
                                    });
                            snackbar.getView().setBackgroundColor(ContextCompat.getColor(CodeOtpVerification.this, R.color.colorGray));
                            // Changing message text color
                            snackbar.setActionTextColor(getResources().getColor(R.color.colorPrimary));
                            // Changing action button text color
                            View sbView = snackbar.getView();
                            TextView textView = (TextView) sbView.findViewById(com.google.android.material.R.id.snackbar_text);
                            textView.setTextColor(Color.WHITE);
                            snackbar.show();
                        }  else if (volleyError instanceof ParseError) {
                            //message = "Parsing error! Please try again later";
                            message = "Une erreur est survenue!";
                            Snackbar snackbar = Snackbar
                                    .make(mainLayout, message, Snackbar.LENGTH_INDEFINITE)
                                    .setAction("REESSAYER", new View.OnClickListener() {
                                        @Override
                                        public void onClick(View view) {
                                            inscrire();
                                        }
                                    });
                            snackbar.getView().setBackgroundColor(ContextCompat.getColor(CodeOtpVerification.this, R.color.colorGray));
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
        );
        /*postRequest.setRetryPolicy(new DefaultRetryPolicy(
                0,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));*/
        //DefaultRetryPolicy  retryPolicy = new DefaultRetryPolicy(0, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        //postRequest.setRetryPolicy(new DefaultRetryPolicy(0, -1, 0));
        //postRequest.setRetryPolicy(retryPolicy);
        jsonObjectRequest.setRetryPolicy(new DefaultRetryPolicy(
                25000,
                -1,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        queue.add(jsonObjectRequest);

        //initialize the progress dialog and show it
        progressDialog = new ProgressDialog(CodeOtpVerification.this);
        progressDialog.setCancelable(false);
        progressDialog.setMessage("Veuillez patienter SVP! \nL'inscription de vos informations est en cours...");
        progressDialog.show();
    }

    /*private void retrait_mmo(final String numero, final String montant) {
        final Tontine t = SugarRecord.findById(Tontine.class, (long) id_tontine);
        RequestQueue queue = Volley.newRequestQueue(this);

        StringRequest postRequest = new StringRequest(Request.Method.POST, Constantes.URL_COLLECTED_MOMO_TONTINE,
                new Response.Listener<String>()
                {
                    @SuppressLint("ResourceAsColor")
                    @Override
                    public void onResponse(String response) {
                        //progressDialog.dismiss();
                        // response
                        Log.e("ResponseRetraitMomo", response.toString());
                        // if (!response.equals("Erreur")) {
                        try {

                            JSONObject result = new JSONObject(response);
                            //final JSONArray array = result.getJSONArray("data");
                            Log.e("My App", result.toString());
                            if (result.getInt("responseCode") == 0) {
                                progressDialog.dismiss();
                                // maj des dates
                                Long id_tont = Long.valueOf(0);
                                Date currentTime = Calendar.getInstance().getTime();
                                long output_creation=currentTime.getTime()/1000L;
                                String str_creation=Long.toString(output_creation);
                                long timestamp_creation = Long.parseLong(str_creation) * 1000;
                                long output_maj=currentTime.getTime()/1000L;
                                String str_maj=Long.toString(output_maj);
                                long timestamp_maj = Long.parseLong(str_maj) * 1000;

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
                                    if ("update".equals(action)) {
                                        if (object.equals("tontines")) {
                                            List<Tontine> old = Tontine.find(Tontine.class, "id_server = ?", data.getLong("id")+"");
                                            if (old.size() > 0) {
                                                if(data.has("state")) {
                                                    old.get(0).setStatut(data.getString("state"));
                                                }
                                                if(data.has("carnet")) {
                                                    old.get(0).setCarnet(data.getString("carnet"));
                                                }
                                                old.get(0).setMaj(timestamp_maj);
                                                old.get(0).save();
                                                id_tont = old.get(0).getId();
                                            }

                                        }
                                    }
                                }
                                if(id_tont != 0)
                                {
                                    CodeOtpVerification.this.finish();
                                    String msg = "Votre tontine a correctement été transféré sur votre compte Moov Money";
//                                    String msg = result.getString("message");
                                    Intent j = new Intent(CodeOtpVerification.this, Message_ok.class);
                                    j.putExtra("class", "com.sicmagroup.tondi.MesTontines");
                                    //j.putExtra("id_tontine",Integer.parseInt(String.valueOf(id_tontine)));
                                    j.putExtra("msg_desc", msg);
                                    startActivity(j);
                                }
                                else
                                {
                                    CodeOtpVerification.this.finish();
                                    String msg = "Désolé, une erreur est survenue. Contactez le service client.";
                                    Intent i = new Intent(CodeOtpVerification.this, Message_non.class);
                                    i.putExtra("msg_desc", msg);
                                    i.putExtra("class", "com.sicmagroup.tondi.MesTontines");
                                    startActivity(i);
                                }

                            }
                            else {
                                progressDialog.dismiss();
                                String msg = result.getString("body");
                                Intent i = new Intent(CodeOtpVerification.this, Message_non.class);
                                i.putExtra("msg_desc", msg);
                                i.putExtra("class", "com.sicmagroup.tondi.NouvelleTontine");
                                startActivity(i);
                            }


                        } catch (Throwable t) {
                            Log.d("errornscription", t.getMessage());
                            //Log.e("My App", "Could not parse malformed JSON: \"" + response + "\"");
                        }
                        }
                        else
                            Toast.makeText(CarteMain.this, "Erreur lors du versement", Toast.LENGTH_LONG).show();

                    }
                },
                new Response.ErrorListener()
                {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {
                        progressDialog.dismiss();
                        Log.e("ErrRetraitMomo", String.valueOf(volleyError.getMessage()));
                        Log.e("Stack", "Error StackTrace: \t" + volleyError.getStackTrace());

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

                                Utilitaire utilitaire = new Utilitaire(CodeOtpVerification.this);
                                // si internet, appeler synchroniser_en_ligne
                                if (utilitaire.isConnected()) {
                                    utilitaire.synchroniser_en_ligne();
                                }

                                Intent j = new Intent(CodeOtpVerification.this, Message_ok.class);
                                j.putExtra("class", "com.sicmagroup.tondi.MesTontines");
                                //j.putExtra("id_tontine",Integer.parseInt(String.valueOf(id_tontine)));
                                j.putExtra("msg_desc", msg);

                                startActivity(j);

                            } catch (Throwable t) {
                                Log.e("errornscription", t.getMessage());
                                //Log.e("My App", "Could not parse malformed JSON: \"" + response + "\"");
                            }

                        }
                        String message;
                        if (volleyError instanceof NetworkError || volleyError instanceof AuthFailureError || volleyError instanceof TimeoutError) {
                            //Toast.makeText(Inscription.this, "error:"+volleyError.getMessage(), Toast.LENGTH_SHORT).show();
                            //Log.d("VolleyError_Test",volleyError.getMessage());
                            progressDialog.dismiss();
                            message = "Aucune connexion Internet!";
                            Snackbar snackbar = Snackbar
                                    .make(mainLayout, message, Snackbar.LENGTH_INDEFINITE)
                                    .setAction("REESSAYER", new View.OnClickListener() {
                                        @Override
                                        public void onClick(View view) {

                                            refreshAccessToken(CodeOtpVerification.this, new CodeOtpVerification.TokenRefreshListener() {
                                                @Override
                                                public void onTokenRefreshed(boolean success) {
                                                    if (success) {
                                                        retrait_mmo(numero,montant);
                                                    }
                                                }
                                            });
                                        }
                                    });
                            snackbar.getView().setBackgroundColor(ContextCompat.getColor(CodeOtpVerification.this, R.color.colorGray));
                            // Changing message text color
                            snackbar.setActionTextColor(getResources().getColor(R.color.colorPrimary));
                            // Changing action button text color
                            View sbView = snackbar.getView();
                            TextView textView = (TextView) sbView.findViewById(com.google.android.material.R.id.snackbar_text);
                            textView.setTextColor(Color.WHITE);
                            snackbar.show();

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
                            snackbar.getView().setBackgroundColor(ContextCompat.getColor(CodeOtpVerification.this, R.color.colorGray));
                            // Changing message text color
                            snackbar.setActionTextColor(getResources().getColor(R.color.colorPrimary));
                            // Changing action button text color
                            View sbView = snackbar.getView();
                            TextView textView = (TextView) sbView.findViewById(com.google.android.material.R.id.snackbar_text);
                            textView.setTextColor(Color.WHITE);
                            snackbar.show();
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
                            snackbar.getView().setBackgroundColor(ContextCompat.getColor(CodeOtpVerification.this, R.color.colorGray));
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
            protected Map<String, String> getParams()
            {


                Map<String, String>  params = new HashMap<String, String>();
                params.put("customerNumber", numero);
//              params.put("montant", montant);
                params.put("idTontine", String.valueOf(id_tontine));
//                params.put("periode", t.getPeriode());
//                params.put("carnet", t.getCarnet());
//                params.put("mise", String.valueOf(t.getMise()));
//                Log.e("paramPost", montant);
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
                50000,
                -1,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        queue.add(postRequest);

        //initialize the progress dialog and show it
        progressDialog = new ProgressDialog(CodeOtpVerification.this);
        progressDialog.setCancelable(false);
        progressDialog.setMessage("Veuillez patienter SVP! \n Le virement sur votre compte mobile money est en cours...");
        progressDialog.show();
    }*/

    private void retrait_mmo(final String numero, final String montant) {
        final Tontine t = SugarRecord.findById(Tontine.class, (long) id_tontine);
        RequestQueue queue = Volley.newRequestQueue(this);

        JSONObject jsonBody = new JSONObject();
        try {
            jsonBody.put("customerNumber", numero);
            jsonBody.put("idTontine", String.valueOf(id_tontine));
            // Ajoutez d'autres paramètres si nécessaire
        } catch (JSONException e) {
            e.printStackTrace();
        }

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                Request.Method.POST,
                Constantes.URL_COLLECTED_MOMO_TONTINE,
                jsonBody,
                new Response.Listener<JSONObject>() {
                    @SuppressLint("ResourceAsColor")
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.e("ResponseRetraitMomo", response.toString());
                        try {
                            if (response.getInt("responseCode") == 0) {
                                progressDialog.dismiss();
                                JSONObject result = response;

                                    // maj des dates
                                    Long id_tont = Long.valueOf(0);
                                    Date currentTime = Calendar.getInstance().getTime();
                                    long output_creation=currentTime.getTime()/1000L;
                                    String str_creation=Long.toString(output_creation);
                                    long timestamp_creation = Long.parseLong(str_creation) * 1000;
                                    long output_maj=currentTime.getTime()/1000L;
                                    String str_maj=Long.toString(output_maj);
                                    long timestamp_maj = Long.parseLong(str_maj) * 1000;

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
                                        if ("update".equals(action)) {
                                            if (object.equals("tontines")) {
                                                List<Tontine> old = Tontine.find(Tontine.class, "id_server = ?", data.getLong("id")+"");
                                                if (old.size() > 0) {
                                                    if(data.has("state")) {
                                                        old.get(0).setStatut(data.getString("state"));
                                                    }
                                                    if(data.has("carnet")) {
                                                        old.get(0).setCarnet(data.getString("carnet"));
                                                    }
                                                    old.get(0).setMaj(timestamp_maj);
                                                    old.get(0).save();
                                                    id_tont = old.get(0).getId();
                                                }

                                            }
                                        }
                                    }
                                    if(id_tont != 0)
                                    {
                                        CodeOtpVerification.this.finish();
                                        String msg = "Votre tontine a correctement été transféré sur votre compte Moov Money";
//                                    String msg = result.getString("message");
                                        Intent j = new Intent(CodeOtpVerification.this, Message_ok.class);
                                        j.putExtra("class", "com.sicmagroup.tondi.MesTontines");
                                        //j.putExtra("id_tontine",Integer.parseInt(String.valueOf(id_tontine)));
                                        j.putExtra("msg_desc", msg);
                                        startActivity(j);
                                    }
                                    else
                                    {
                                        CodeOtpVerification.this.finish();
                                        String msg = "Désolé, une erreur est survenue. Contactez le service client.";
                                        Intent i = new Intent(CodeOtpVerification.this, Message_non.class);
                                        i.putExtra("msg_desc", msg);
                                        i.putExtra("class", "com.sicmagroup.tondi.MesTontines");
                                        startActivity(i);
                                    }


                            } else {
                                progressDialog.dismiss();
                                String msg = response.getString("body");
                                Intent i = new Intent(CodeOtpVerification.this, Message_non.class);
                                i.putExtra("msg_desc", msg);
                                i.putExtra("class", "com.sicmagroup.tondi.NouvelleTontine");
                                startActivity(i);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {

                        progressDialog.dismiss();
                        Log.e("ErrRetraitMomo", String.valueOf(volleyError.getMessage()));
                        Log.e("Stack", "Error StackTrace: \t" + volleyError.getStackTrace());

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

                                Utilitaire utilitaire = new Utilitaire(CodeOtpVerification.this);
                                // si internet, appeler synchroniser_en_ligne
                                if (utilitaire.isConnected()) {
                                    utilitaire.synchroniser_en_ligne();
                                }

                                Intent j = new Intent(CodeOtpVerification.this, Message_ok.class);
                                j.putExtra("class", "com.sicmagroup.tondi.MesTontines");
                                //j.putExtra("id_tontine",Integer.parseInt(String.valueOf(id_tontine)));
                                j.putExtra("msg_desc", msg);

                                startActivity(j);

                            } catch (Throwable t) {
                                Log.e("errornscription", t.getMessage());
                                //Log.e("My App", "Could not parse malformed JSON: \"" + response + "\"");
                            }

                        }
                        String message;
                        if (volleyError instanceof NetworkError || volleyError instanceof AuthFailureError || volleyError instanceof TimeoutError) {
                            //Toast.makeText(Inscription.this, "error:"+volleyError.getMessage(), Toast.LENGTH_SHORT).show();
                            //Log.d("VolleyError_Test",volleyError.getMessage());
                            progressDialog.dismiss();
                            message = "Aucune connexion Internet!";
                            Snackbar snackbar = Snackbar
                                    .make(mainLayout, message, Snackbar.LENGTH_INDEFINITE)
                                    .setAction("REESSAYER", new View.OnClickListener() {
                                        @Override
                                        public void onClick(View view) {

                                            refreshAccessToken(CodeOtpVerification.this, new CodeOtpVerification.TokenRefreshListener() {
                                                @Override
                                                public void onTokenRefreshed(boolean success) {
                                                    if (success) {
                                                        retrait_mmo(numero,montant);
                                                    }
                                                }
                                            });
                                        }
                                    });
                            snackbar.getView().setBackgroundColor(ContextCompat.getColor(CodeOtpVerification.this, R.color.colorGray));
                            // Changing message text color
                            snackbar.setActionTextColor(getResources().getColor(R.color.colorPrimary));
                            // Changing action button text color
                            View sbView = snackbar.getView();
                            TextView textView = (TextView) sbView.findViewById(com.google.android.material.R.id.snackbar_text);
                            textView.setTextColor(Color.WHITE);
                            snackbar.show();

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
                            snackbar.getView().setBackgroundColor(ContextCompat.getColor(CodeOtpVerification.this, R.color.colorGray));
                            // Changing message text color
                            snackbar.setActionTextColor(getResources().getColor(R.color.colorPrimary));
                            // Changing action button text color
                            View sbView = snackbar.getView();
                            TextView textView = (TextView) sbView.findViewById(com.google.android.material.R.id.snackbar_text);
                            textView.setTextColor(Color.WHITE);
                            snackbar.show();
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
                            snackbar.getView().setBackgroundColor(ContextCompat.getColor(CodeOtpVerification.this, R.color.colorGray));
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
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                headers.put("Content-Type", "application/json");
                headers.put("Authorization", "Bearer " + accessToken); // Ajoute le token ici
                return headers;
            }
        };

        jsonObjectRequest.setRetryPolicy(new DefaultRetryPolicy(
                50000,
                -1,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        queue.add(jsonObjectRequest);

        // Initialiser et afficher le ProgressDialog
        progressDialog = new ProgressDialog(CodeOtpVerification.this);
        progressDialog.setCancelable(false);
        progressDialog.setMessage("Veuillez patienter SVP! \n Le virement sur votre compte mobile money est en cours...");
        progressDialog.show();
    }


// ici la class est static sinon elle ne marchera pas avec une erreur de zero argument
    public static class OtpReceiver extends BroadcastReceiver {

        // Constructeur vide requis pour ne pas causer d'erreur
        public OtpReceiver() {

            //super();
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals("android.provider.Telephony.SMS_RECEIVED")) {
                // Récupérer le SMS en accédant à la messagerie
                Bundle bundle = intent.getExtras();
                if (bundle != null) {
                    Object[] pdus = (Object[]) bundle.get("pdus");
                    if (pdus != null) {
                        for (Object pdu : pdus) {
                            SmsMessage smsMessage = SmsMessage.createFromPdu((byte[]) pdu);
                            String messageBody = smsMessage.getMessageBody();
                            // Extraire le code OTP du message
                            String otpCode = extractOtpFromMessage(messageBody);
                            // Mise à jour du editText de otp là
                            Toast.makeText(context, "Code OTP reçu: " + otpCode, Toast.LENGTH_SHORT).show();
                            editText_codeOtp.setText(otpCode);
                        }
                    }
                }
            }
        }

        private String extractOtpFromMessage(String message) {
            // Méthode pour extraire le code OTP du message SMS

            Pattern otpPattern = Pattern.compile("\\d{4,6}"); // Extrait un code OTP de 4 à 6 chiffres
            Matcher matcher = otpPattern.matcher(message);
            if (matcher.find()) {
                return matcher.group(0);
            }
            return null;
        }



        /*@Override
        public void onReceive(Context context, Intent intent) {

            Log.e("sms", "recu");
            if (SmsRetriever.SMS_RETRIEVED_ACTION.equals(intent.getAction())){
                Bundle extras = intent.getExtras();
                Status status = (Status) extras.get(SmsRetriever.EXTRA_STATUS);

                switch (status.getStatusCode()){
                    case CommonStatusCodes.SUCCESS:
                        Intent consentIntent = extras.getParcelable(SmsRetriever.EXTRA_CONSENT_INTENT);
                        String message = (String) extras.get(SmsRetriever.EXTRA_SMS_MESSAGE);
//                        if(message.contains("E0dcFHj5c")){
//
//                        }
//                        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
                        //inscription
                        if(message.contains("<#>Votre code de verification est : ")&& !message.contains("Rappel")){

                            String codeOtpRecu1 = message.substring(36, 42);
//                            Toast.makeText(context, codeOtpRecu1, Toast.LENGTH_SHORT).show();
                            if(editText_codeOtp != null) {
                                editText_codeOtp.setText(codeOtpRecu1.trim());
                                button_validerCodeOtp.performClick();
                            }
                        }
                        //retrait sur mobile money
                        else if (message.contains("<#>Votre code de validation de retrait tontine est : ")&& !message.contains("Rappel")){
                            String codeOtpRecu1 = message.substring(53, 59);
                            Log.e("code_top", codeOtpRecu1);
                            if(editText_codeOtp != null) {
                                editText_codeOtp.setText(codeOtpRecu1);
                                button_validerCodeOtp.performClick();
                            }
                        }
                        else if(message.contains("<#>Numero Marchand : ") && message.contains(" , Votre code de validation de retrait COMUBA Tontine Digitale est ")&& !message.contains("Rappel")){
                            //retrait chez marchand à use

                            Intent intent1 = new Intent(context, CodeOtpVerification.class);
                            String code_msg = "5YG8nL+qD3V";
                            String code_msg_prod = "GCGjS9oU30Z";
                            intent1.putExtra("caller_activity", "smsReceiver");
                            String[] parts = message.split(",");

                            intent1.putExtra("numero_marchand", parts[0].replace("<#>Numero Marchand : ", "").trim());
                            Log.e("num_marchand", parts[0].replace("<#>Numero Marchand : ", ""));

                            intent1.putExtra("token", parts[1].replace("Token de retrait : ", "").trim());
                            Log.e("token", parts[1].replace("Token de retrait : ", ""));
                            String code_otp_part1 = parts[2].replace("Votre code de validation de retrait COMUBA Tontine Digitale est ", "").trim();
                            String code_otp2 = code_otp_part1.replace("\n\n"+code_msg, "").trim();
                            intent1.putExtra("code_otp", code_otp2);
                            Log.d("code_otp", code_otp2);

                            Toast.makeText(context, "Code OTP reçu! Consulter vos messages", Toast.LENGTH_LONG).show();


                        }
                        else if(message.contains("Merci de faire confiance a TONDi") && !message.contains("Rappel"))
                        {

                        }
                        break;
                    case CommonStatusCodes.TIMEOUT:
                        if(context.getClass().getName().equals("Encaisser")){
                            Dialog dialog_alert = new Dialog(context);
                            dialog_alert.requestWindowFeature(Window.FEATURE_NO_TITLE);
                            dialog_alert.setCancelable(true);
                            dialog_alert.setContentView(R.layout.dialog_attention);

                            TextView titre = (TextView) dialog_alert.findViewById(R.id.deco_title);
                            titre.setText("Time out!");
                            TextView message_deco = (TextView) dialog_alert.findViewById(R.id.deco_message);
                            message_deco.setText("Aucune opération effectuée depuit 5min. Quittez puis réessayer svp, Merci.");

                            Button oui = (Button) dialog_alert.findViewById(R.id.btn_oui);
                            oui.setText("Compris");
                            oui.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    dialog_alert.cancel();
                                    context.startActivity(new Intent(context, Dashboard.class));
                                }
                            });

                            Button non = (Button) dialog_alert.findViewById(R.id.btn_non);
                            non.setVisibility(View.GONE);

                            dialog_alert.show();
                        }
                        else{

                        }
                        break;
                }
            }
            else
                Toast.makeText(context, "Un bug est survenu. Réessayez svp", Toast.LENGTH_SHORT).show();

        }*/
    }
    private static SmsMessage getIncomingMessage(Object object, Bundle bundle) {
        SmsMessage smsMessage;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            String format = bundle.getString("format");
            smsMessage = SmsMessage.createFromPdu((byte[]) object, format);
        } else {
            smsMessage = SmsMessage.createFromPdu((byte[]) object);
        }

        return smsMessage;
    }

    private void valider() {
        RequestQueue queue = Volley.newRequestQueue(CodeOtpVerification.this);
        StringRequest postRequest = new StringRequest(Request.Method.POST, url_valider,
                new Response.Listener<String>()
                {
                    @SuppressLint("ResourceAsColor")
                    @Override
                    public void onResponse(String response) {
                        // response
                        Log.e("Response", response);

                        try {

                            JSONObject result = new JSONObject(response);

                            //Log.d("My App", obj.toString());
                            if (result.getBoolean("success")){
                                //alertView("souscription ok","ok");
                                //Prefs.putString(ID_TONTINE_USSD, "");
                                // Mettre à jour la préférence id tontine
                                //Prefs.putString(MONTANT_VERSE, "");
                            }else{

                            }


                        } catch (Throwable t) {
                            //Log.e("My App", "Could not parse malformed JSON: \"" + response + "\"");
                        }


                    }
                },
                new Response.ErrorListener()
                {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // error
                        //Log.d("Error.NouvelleTontine", error.getMessage());
                        if (error instanceof NetworkError || error instanceof AuthFailureError || error instanceof TimeoutError) {
                            //Toast.makeText(Inscription.this, "error:"+volleyError.getMessage(), Toast.LENGTH_SHORT).show();
                            //Log.d("VolleyError_Test",volleyError.getMessage());
                            String message;
                            progressDialog.dismiss();
                            message = "Aucune connexion Internet!";
                            Snackbar snackbar = Snackbar
                                    .make(mainLayout, message, Snackbar.LENGTH_INDEFINITE)
                                    .setAction("REESSAYER", new View.OnClickListener() {
                                        @Override
                                        public void onClick(View view) {

                                            refreshAccessToken(CodeOtpVerification.this, new CodeOtpVerification.TokenRefreshListener() {
                                                @Override
                                                public void onTokenRefreshed(boolean success) {
                                                    if (success) {
                                                        valider();
                                                    }
                                                }
                                            });
                                        }
                                    });
                            snackbar.getView().setBackgroundColor(ContextCompat.getColor(CodeOtpVerification.this, R.color.colorGray));
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
            protected Map<String, String> getParams()
            {

                Map<String, String>  params = new HashMap<String, String>();
                //params.put("id_tontine", Prefs.getString(ID_TONTINE_USSD,null));
                //params.put("id_tontine", periode_value);
                params.put("montant", montant_t);

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
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission accordée
                registerOtpReceiver();
            } else {
                // Permission refusée
                Toast.makeText(this, "Permission de recevoir des SMS refusée.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void registerOtpReceiver() {
        otpReceiver = new OtpReceiver();
        IntentFilter filter = new IntentFilter("android.provider.Telephony.SMS_RECEIVED");
        registerReceiver(otpReceiver, filter);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (otpReceiver != null) {
            unregisterReceiver(otpReceiver);
        }
    }

   /* private BroadcastReceiver smsBroadcastReceiver = new BroadcastReceiver() {
        @SuppressLint("UnsafeIntentLaunch")
        @Override
        public void onReceive(Context context, Intent intent) {
            if (SmsRetriever.SMS_RETRIEVED_ACTION.equals(intent.getAction())) {
                Bundle extras = intent.getExtras();
                assert extras != null;
                Status status = (Status) extras.get(SmsRetriever.EXTRA_STATUS);

                switch (Objects.requireNonNull(status).getStatusCode()) {
                    case CommonStatusCodes.SUCCESS:
                        // Obtenir l'intention de consentement utilisateur
                        @SuppressLint("UnsafeIntentLaunch") Intent consentIntent = extras.getParcelable(SmsRetriever.EXTRA_CONSENT_INTENT);
                        try {
                            startActivityForResult(consentIntent, SMS_CONSENT_REQUEST);
                        } catch (ActivityNotFoundException e) {
                            Log.e("OTP", "Impossible de démarrer l'intention de consentement.");
                        }
                        break;
                    case CommonStatusCodes.TIMEOUT:
                        Log.e("OTP", "Le temps d'attente du SMS a expiré.");
                        break;
                }
            }
        }
    };

    @Override
    protected void onStart() {
        super.onStart();
        IntentFilter intentFilter = new IntentFilter(SmsRetriever.SMS_RETRIEVED_ACTION);
        registerReceiver(smsBroadcastReceiver, intentFilter);
    }

    @Override
    protected void onStop() {
        super.onStop();
        unregisterReceiver(smsBroadcastReceiver);
    }*/


}