package com.sicmagroup.tondi;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.android.volley.toolbox.JsonObjectRequest;
import com.google.android.gms.auth.api.phone.SmsRetriever;
import com.google.android.gms.auth.api.phone.SmsRetrieverClient;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
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

import com.mikhaellopez.circularprogressbar.CircularProgressBar;
import com.pixplicity.easyprefs.library.Prefs;
import com.romellfudi.permission.PermissionService;
import com.sicmagroup.formmaster.model.FormElementPickerSexe;
import com.sicmagroup.formmaster.model.FormElementZero;
import com.sicmagroup.tondi.utils.Constantes;
import com.sicmagroup.tondi.Enum.OperationTypeEnum;
import com.sicmagroup.ussdlibra.USSDController;
import com.sicmagroup.ussdlibra.USSDService;
import com.theartofdev.edmodo.cropper.CropImageView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


import com.sicmagroup.formmaster.FormBuilder;
import com.sicmagroup.formmaster.listener.OnFormElementValueChangedListener;
import com.sicmagroup.formmaster.model.FormElementTextPhone;
import com.sicmagroup.formmaster.model.FormElementTextSingleLine;
import com.sicmagroup.formmaster.model.BaseFormElement;
import com.sicmagroup.formmaster.model.FormElementTextPassword;

import static com.sicmagroup.tondi.Accueil.CGU_FON_KEY;
import static com.sicmagroup.tondi.Accueil.CGU_FR_KEY;
import static com.sicmagroup.tondi.Accueil.MOOV_DATA_SHARING;
import static com.sicmagroup.tondi.Connexion.ACCESS_BOOL;
import static com.sicmagroup.tondi.Connexion.ACCESS_RETURNf_KEY;
import static com.sicmagroup.tondi.Connexion.CONNECTER_KEY;
import static com.sicmagroup.tondi.Connexion.ID_UTILISATEUR_KEY;
import static com.sicmagroup.tondi.Connexion.NOM_KEY;
import static com.sicmagroup.tondi.Connexion.NUMERO_VERIFYED;
import static com.sicmagroup.tondi.Connexion.PASS_KEY;
import static com.sicmagroup.tondi.Connexion.PIN_KEY;
import static com.sicmagroup.tondi.Connexion.PRENOMS_KEY;
import static com.sicmagroup.tondi.Connexion.TEL_KEY;
import static com.sicmagroup.tondi.Connexion.url_generate_otp_insc;
import static com.sicmagroup.tondi.utils.Constantes.SERVEUR;
import static com.sicmagroup.tondi.utils.Constantes.TOKEN;
import static com.sicmagroup.tondi.utils.Constantes.USER_CF_MDP_EXTRAT_KEY;
import static com.sicmagroup.tondi.utils.Constantes.USER_MDP_EXTRAT_KEY;
import static com.sicmagroup.tondi.utils.Constantes.USER_NOM_EXTRAT_KEY;
import static com.sicmagroup.tondi.utils.Constantes.USER_PRENOMS_EXTRAT_KEY;
import static com.sicmagroup.tondi.utils.Constantes.USER_TEL_EXTRAT_KEY;
import static com.sicmagroup.tondi.utils.Constantes.accessToken;


public class Inscription extends AppCompatActivity {
    private static final String TAG = "service_O";
    private static final String LOG_TAG = "inscription";
    private CropImageView mCropImageView;
    private Uri mCropImageUri;
    private RecyclerView mRecyclerView;
    private FormBuilder mFormBuilder;
    public static final int CROPPING_CODE = 2;
    private static final int TAG_NOM = 11;
    private static final int TAG_PRENOMS = 12;
    private static final int TAG_TEL = 13;
    private static final int TAG_PASS = 14;
    private static final int TAG_cPASS = 15;
    private static final int TAG_SEXE = 16;

    String url_inscrire = SERVEUR + "/api/v1/utilisateurs/inscrire";

    String cgu_fr_url = SERVEUR + "/medias/cgu_audio_fr.m4a";
    String cgu_fon_url = SERVEUR + "/medias/cgu_test.mp3";
    String cgu_test_url = SERVEUR + "/medias/cgu_test_new.mp3";
    MediaPlayer mp_cgu_fon;
    MediaPlayer mp_cgu_fr;
    Utilitaire utilitaire;
    ProgressDialog progressDialog;
    CircularProgressBar circularProgressBar;
    CircularProgressBar circularProgressBar1;

    private Handler mHandler = new Handler();
    private int pStatus = 0;
    private HashMap<String, HashSet<String>> map;
    private USSDController ussdApi;
    boolean resumed = true;
    Runnable mUpdateUI = null;
    JSONObject jsonObject;
    RequestQueue rQueue;
    List<String> type_sexe = new ArrayList<>();


    FormElementZero element0 = FormElementZero.createInstance().setTag(99);
    FormElementTextSingleLine element1 = FormElementTextSingleLine.createInstance().setTag(TAG_NOM).setTitle("Nom").setHint("Renseigner").setRequired(true);
    FormElementTextSingleLine element2 = FormElementTextSingleLine.createInstance().setTag(TAG_PRENOMS).setTitle("Prénoms").setHint("Renseigner").setRequired(true);
    final FormElementTextPhone element3 = FormElementTextPhone.createInstance().setTag(TAG_TEL).setTitle("Téléphone").setHint("Entrez un numero MOOV").setRequired(true);
    final FormElementTextPassword element4 = FormElementTextPassword.createInstance().setTag(TAG_PASS).setTitle("Mot de passe").setHint("Mot de passe 6 caractères au moins").setRequired(true);
    final FormElementTextPassword element5 = FormElementTextPassword.createInstance().setTag(TAG_cPASS).setTitle("Confirmer").setHint("Confirmer le mot de passe").
            setRequired(true);
    final FormElementPickerSexe element6 = FormElementPickerSexe.createInstance().setTag(TAG_SEXE).setHint("Sexe").setOptions(type_sexe).setPickerTitle("Sexe").setRequired(true);
    // single item picker input


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inscription);

        // Initialisation des préférences
        new Prefs.Builder()
                .setContext(this)
                .setMode(ContextWrapper.MODE_PRIVATE)
                .setPrefsName(getPackageName())
                .setUseDefaultSharedPreference(true)
                .build();

        Prefs.putString(ACCESS_RETURNf_KEY, "com.sicmagroup.tondi.Inscription");
        Prefs.putBoolean(ACCESS_BOOL, true);
        Prefs.putString(NUMERO_VERIFYED, "inconnu");

        utilitaire = new Utilitaire(this);
        if (!utilitaire.isConnected()) {
            this.finish();
        }
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        // Initialiser les préférences CGU
        Prefs.putInt(CGU_FON_KEY, 0);
        Prefs.putInt(CGU_FR_KEY, 0);

        // Récupérer les valeurs de l'intent
        element1.setValue(getIntent().getStringExtra("nom"));
        element2.setValue(getIntent().getStringExtra("prenoms"));
        element3.setValue(getIntent().getStringExtra("tel"));
        element4.setValue(getIntent().getStringExtra("mdp"));
        element5.setValue(getIntent().getStringExtra("cnf_mdp"));

        // Initialiser la map
        map = new HashMap<>();
        map.put("KEY_LOGIN", new HashSet<>(Arrays.asList("waiting", "loading")));
        map.put("KEY_ERROR", new HashSet<>(Arrays.asList("problem", "error")));

        // Initialiser les composants UI
        setupForm();
        circularProgressBar = findViewById(R.id.circular_progress);
        circularProgressBar.setColor(ContextCompat.getColor(this, R.color.colorPrimaryDark2));

        // Initialiser les media players
        initMediaPlayer();

        // Récupérer les boutons
        final Button b = findViewById(R.id.cgu_fon);
        final Button b1 = findViewById(R.id.cgu_fr);
        b.setVisibility(View.INVISIBLE);
        b1.setVisibility(View.INVISIBLE);

        // Préparer et gérer le media player pour mp_cgu_fon
        prepareMediaPlayer(mp_cgu_fon, cgu_test_url, b, circularProgressBar, R.drawable.bg_ccm);
        // Préparer et gérer le media player pour mp_cgu_fr
        prepareMediaPlayer(mp_cgu_fr, cgu_fr_url, b1, circularProgressBar1, R.drawable.bg_ccm);
    }

    private void initMediaPlayer() {
        mp_cgu_fon = new MediaPlayer();
        mp_cgu_fon.setAudioStreamType(AudioManager.STREAM_MUSIC);

        mp_cgu_fr = new MediaPlayer();
        mp_cgu_fr.setAudioStreamType(AudioManager.STREAM_MUSIC);
    }

    private void prepareMediaPlayer(MediaPlayer mediaPlayer, String dataSource, final Button button, final CircularProgressBar progressBar, int progressBarDrawable) {
        try {
            mediaPlayer.setDataSource(dataSource);
        } catch (IOException e) {
            e.printStackTrace();
        }

        mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(final MediaPlayer player) {
                button.setVisibility(View.VISIBLE);
                button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        handleMediaPlayerClick(player, button, progressBar, progressBarDrawable);
                    }
                });
            }
        });

        mediaPlayer.prepareAsync(); // might take long! (for buffering, etc)
    }

    private void handleMediaPlayerClick(final MediaPlayer player, final Button button, final CircularProgressBar progressBar, int progressBarDrawable) {
        int animationDuration = player.getDuration();
        int actual_percent = (player.getCurrentPosition() * 100) / animationDuration;

        try {
            if (player.isPlaying()) {
                player.pause();
                button.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_play_arrow, 0, 0, 0);
                button.setCompoundDrawablePadding(8);
                progressBar.setBackground(null);
                progressBar.setProgressWithAnimation(actual_percent, player.getCurrentPosition());
            } else {
                Toast.makeText(getApplicationContext(), "La lecture des termes et conditions est en cours...", Toast.LENGTH_SHORT).show();
                player.start();
                progressBar.setBackground(getResources().getDrawable(progressBarDrawable));
                button.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_pause, 0, 0, 0);
                button.setCompoundDrawablePadding(8);
                progressBar.setBackgroundColor(ContextCompat.getColor(this, R.color.colorGrey));
                progressBar.setProgressBarWidth(getResources().getDimension(R.dimen.progressBarWidth));
                progressBar.setBackgroundProgressBarWidth(getResources().getDimension(R.dimen.backgroundProgressBarWidth));
                progressBar.setProgressWithAnimation(100, (animationDuration - player.getCurrentPosition()));

                player.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    @Override
                    public void onCompletion(MediaPlayer mp) {
                        Toast.makeText(getApplicationContext(), "La lecture des termes et conditions est terminée", Toast.LENGTH_LONG).show();
                        Prefs.putInt(CGU_FON_KEY, 1);
                        progressBar.setProgressWithAnimation(0);
                        button.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_play_arrow, 0, 0, 0);
                        button.setCompoundDrawablePadding(8);
                        progressBar.setBackground(null);
                    }
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d("onStop", "back");
        Prefs.putBoolean(ACCESS_BOOL, false);

        handleMediaPlayerPause(mp_cgu_fon, findViewById(R.id.cgu_fon), circularProgressBar);
        handleMediaPlayerPause(mp_cgu_fr, findViewById(R.id.cgu_fr), circularProgressBar1);
    }

    private void handleMediaPlayerPause(MediaPlayer mediaPlayer, Button button, CircularProgressBar progressBar) {
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
            int animationDuration = mediaPlayer.getDuration();
            int actual_percent = (mediaPlayer.getCurrentPosition() * 100) / animationDuration;
            button.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_play_arrow, 0, 0, 0);
            button.setCompoundDrawablePadding(8);
            progressBar.setProgressWithAnimation(actual_percent, mediaPlayer.getCurrentPosition());
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d("onDestroy", "back");
        Prefs.putBoolean(ACCESS_BOOL, false);

        if (mp_cgu_fon.isPlaying()) {
            mp_cgu_fon.pause();
        }

        if (mp_cgu_fr.isPlaying()) {
            mp_cgu_fr.pause();
        }
    }

    private void setupForm() {

        // Initialisation du RecyclerView et du FormBuilder
        mRecyclerView = findViewById(R.id.recyclerView);
        mFormBuilder = new FormBuilder(this, mRecyclerView, new OnFormElementValueChangedListener() {
            @Override
            public void onValueChanged(BaseFormElement formElement) {
                // Actions à effectuer lorsque la valeur d'un élément du formulaire change
            }
        });

        // Ajout des types de sexe
        type_sexe.add("Homme");
        type_sexe.add("Femme");
        type_sexe.add("Autre");

        // Ajout des éléments du formulaire
        List<BaseFormElement> formItems = new ArrayList<>();
        formItems.add(element0);
        formItems.add(element6);
        formItems.add(element3);
        formItems.add(element4);
        formItems.add(element5);

        mFormBuilder.addFormElements(formItems);

        // Configuration du lien de connexion
        TextView lien_connexion = findViewById(R.id.lien_connexion);
        lien_connexion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
                startActivity(new Intent(Inscription.this, Connexion.class));
            }
        });

        // Configuration du bouton d'inscription
        Button btn_inscription = findViewById(R.id.btn_inscription);
        btn_inscription.setOnClickListener(new View.OnClickListener() {
            String nom, prenoms = "";

            @SuppressLint("ResourceAsColor")
            @Override
            public void onClick(View v) {
                if (!mFormBuilder.isValidForm()) {
                    alertView("ATTENTION", "Vous devez remplir tous les champs.");
                } else {
                    String msg = "";
                    boolean flag = false;

                    if (element3.getValue().length() != 8) {
                        msg += "> Le numéro de téléphone doit être composé de 8 caractères.\n";
                        flag = true;
                    }
                    if (element5.getValue().length() < 6 || element4.getValue().length() < 6) {
                        msg += "> Le Mot de passe doit être composé au minimum de 6 caractères.\n";
                        flag = true;
                    }
                    if (!element5.getValue().equals(element4.getValue())) {
                        msg += "> Le champ Mot de passe et Confirmer ne correspondent pas.\n";
                        flag = true;
                    }
//                    if (Prefs.getInt(CGU_FON_KEY, 0) != 1 && Prefs.getInt(CGU_FR_KEY, 0) != 1) {
//                        msg += "> Vous devez lire ou écouter les termes et conditions.\n";
//                        flag = true;
//                    }

                    CheckBox check_cgu = findViewById(R.id.check_cgu);
                    if (!check_cgu.isChecked()) {
                        msg += "> Vous devez accepter les CGU.\n";
                        flag = true;
                    }

                    if (flag) {
                        alertView("Erreurs dans le formulaire", msg);
                    } else {
                        final String tel_value = mFormBuilder.getFormElement(TAG_TEL).getValue();
                        final String mdp_value = mFormBuilder.getFormElement(TAG_PASS).getValue();
                        final String sexe_value = mFormBuilder.getFormElement(TAG_SEXE).getValue();
                        Log.e("Le num : ", tel_value);
                        Log.e("Le mdp : ", mdp_value);

                        if (!tel_value.isEmpty() && tel_value != null && !mdp_value.isEmpty() && mdp_value != null) {
                            obtenirNomPrenomsEtVerifier(tel_value, mdp_value, sexe_value);
                        } else {
                           //
                        }
                    }
                }
            }
        });
    }

    private void obtenirNomPrenomsEtVerifier(final String tel_value, final String mdp_value, final String sexe_value) {
        RequestQueue queue = Volley.newRequestQueue(Inscription.this);

        // Création de l'objet JSON avec les paramètres
        JSONObject params = new JSONObject();
        try {
            params.put("customerNumber", tel_value);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        JsonObjectRequest postRequest = new JsonObjectRequest(Request.Method.POST, Constantes.URL_GET_CUSTOMER_INFO_FROM_QOS, params,
                new Response.Listener<JSONObject>() {
                    @SuppressLint("ResourceAsColor")
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            int responseCode = response.getInt("responseCode");
                            String bodyString = response.getString("body");
                            if (responseCode == 0) {
                                JSONObject body = new JSONObject(bodyString);
                                Log.e("Le body nom et prenom", body.toString());
                                final String nom_value = body.getString("firstName");
                                final String prenoms_value = body.getString("surName");

                                // Enregistrement dans SharedPreferences
                                SharedPreferences sharedPreferences = getSharedPreferences("MyPrefs", MODE_PRIVATE);
                                SharedPreferences.Editor editor = sharedPreferences.edit();
                                editor.putString("nom", nom_value);
                                editor.putString("prenoms", prenoms_value);
                                editor.apply();
                                Log.e("nom", nom_value);
                                Log.e("prenom", prenoms_value);

                                inscrire();
                                //envoyerOtp(tel_value, mdp_value, sexe_value, nom_value, prenoms_value);
                            } else {
                                afficherMessageEtTerminer(bodyString);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        afficherErreurSnackbar();
                    }
                });

        postRequest.setRetryPolicy(new DefaultRetryPolicy(
                25000,
                -1,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        queue.add(postRequest);
        afficherProgressDialog("Vérification du numéro et obtention de votre nom et prénom(s).");
    }


    private void envoyerOtp(final String tel_value, final String mdp_value, final String sexe_value, final String nom_value, final String prenoms_value) {
        RequestQueue queue = Volley.newRequestQueue(Inscription.this);
        StringRequest postRequest = new StringRequest(Request.Method.POST, Constantes.URL_GENERATE_OTP,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject result = new JSONObject(response);
                            int responseCode = result.getInt("responseCode");
                            if (responseCode == 0) {
                                demarrerSmsRetriever(tel_value, mdp_value, sexe_value, nom_value, prenoms_value);
                            } else {
                                afficherMessageEtTerminer(result.getString("body"));
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        afficherErreurSnackbar();
                    }
                }
        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("number", tel_value);
                params.put(Constantes.TYPE_OP_KEY, OperationTypeEnum.INSCRIPTION.toString());
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
        afficherProgressDialog("Vérification du numero en cours.");
    }

    private void demarrerSmsRetriever(final String tel_value, final String mdp_value, final String sexe_value, final String nom_value, final String prenoms_value) {
        SmsRetrieverClient client = SmsRetriever.getClient(Inscription.this);
        Task<Void> task = client.startSmsRetriever();
        task.addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Intent codeOtpVer = new Intent(Inscription.this, CodeOtpVerification.class);
                codeOtpVer.putExtra("numero", tel_value);
                codeOtpVer.putExtra("nom", nom_value);
                codeOtpVer.putExtra("prenoms", prenoms_value);
                codeOtpVer.putExtra("mdp", mdp_value);
                codeOtpVer.putExtra("sexe", sexe_value);
                codeOtpVer.putExtra("caller_activity", "inscription");
                startActivity(codeOtpVer);
            }
        });
        task.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                afficherMessageNon("Erreur time out. Veuillez réessayer, Merci.");
            }
        });
    }

    private void afficherMessageEtTerminer(String message) {
        progressDialog.dismiss();
        Inscription.this.finish();
        Intent i = new Intent(Inscription.this, Message_non.class);
        i.putExtra("msg_desc", message);
        i.putExtra("class", "com.sicmagroup.tondi.Inscription");
        startActivity(i);
    }

    private void afficherErreurSnackbar() {
        progressDialog.dismiss();
        ConstraintLayout mainLayout = findViewById(R.id.layout_inscription);
        Snackbar snackbar = Snackbar
                .make(mainLayout, "Une erreur est survenue! Veuillez réessayer svp.", Snackbar.LENGTH_INDEFINITE)
                .setAction("OK", new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                    }
                });
        snackbar.getView().setBackgroundColor(ContextCompat.getColor(Inscription.this, R.color.colorGray));
        snackbar.setActionTextColor(getResources().getColor(R.color.colorPrimaryDark));
        View sbView = snackbar.getView();
        TextView textView = sbView.findViewById(com.google.android.material.R.id.snackbar_text);
        textView.setTextColor(Color.WHITE);
        snackbar.show();
    }

    private void afficherProgressDialog(String message) {
        progressDialog = new ProgressDialog(Inscription.this);
        progressDialog.setCancelable(false);
        progressDialog.setMessage(message);
        progressDialog.show();
    }

    private void alertView(String title, String message) {
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle(title)
                .setMessage(message)
                .setCancelable(false)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialoginterface, int i) {
                        dialoginterface.dismiss();
                    }
                }).show();
    }

    private void afficherMessageNon(String message) {
        Intent i = new Intent(Inscription.this, Message_non.class);
        i.putExtra("msg_desc", message);
        i.putExtra("class", "com.sicmagroup.tondi.Inscription");
        startActivity(i);
    }

    private void postDelayedWrapped(final int counter, int delay, final String nom, final String prenoms, final String numero, final String mdp) {
        if (counter <= 0) {
            handlePostRequest(nom, prenoms, numero, mdp);
        } else {
            mUpdateUI = new Runnable() {
                public void run() {
                    postDelayedWrapped(counter - 1, 1000, nom, prenoms, numero, mdp);
                }
            };
            mHandler.postDelayed(mUpdateUI, delay);
        }
    }

    private void handlePostRequest(final String nom, final String prenoms, final String numero, final String mdp) {
        progressDialog.dismiss();

        if (Prefs.getString(NUMERO_VERIFYED, "inconnu").equals("oui")) {
            sendPostRequest(nom, prenoms, numero, mdp);
        } else {
            String errorMessage = Prefs.getString(NUMERO_VERIFYED, "inconnu").equals("non") ?
                    "Erreur! Veuillez insérer la sim dans le téléphone. Ou réessayer" :
                    "Erreur! Veuillez réessayer";
            Toast.makeText(Inscription.this, errorMessage, Toast.LENGTH_SHORT).show();
        }
    }

    private void sendPostRequest(final String nom, final String prenoms, final String numero, final String mdp) {
        RequestQueue queue = Volley.newRequestQueue(Inscription.this);
        StringRequest postRequest = new StringRequest(Request.Method.POST, url_generate_otp_insc,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        handlePostResponse(response, nom, prenoms, numero, mdp);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        handleError(error);
                    }
                }
        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("numero_client", numero);
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
        showProgressDialog("Veuillez patienter SVP! \n Vérification du numero.");
    }

    private void handlePostResponse(String response, String nom, String prenoms, String numero, String mdp) {
        try {
            JSONObject result = new JSONObject(response);
            if (result.getBoolean("success") && result.getString("curl_response").contains("ID")) {
                navigateToCodeOtpVerification(nom, prenoms, numero, mdp);
            } else {
                navigateToMessageNon(result.getString("message"));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void handleError(VolleyError error) {
        progressDialog.dismiss();
        ConstraintLayout mainLayout = findViewById(R.id.layout_inscription);
        Log.e("Error.inscription", error.getMessage());

        Snackbar snackbar = Snackbar
                .make(mainLayout, "Une erreur est survenue! Veuillez réessayer svp.", Snackbar.LENGTH_INDEFINITE)
                .setAction("OK", new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                    }
                });
        snackbar.getView().setBackgroundColor(ContextCompat.getColor(Inscription.this, R.color.colorGray));
        snackbar.setActionTextColor(getResources().getColor(R.color.colorPrimaryDark));
        TextView textView = snackbar.getView().findViewById(com.google.android.material.R.id.snackbar_text);
        textView.setTextColor(Color.WHITE);
        snackbar.show();
    }

    private void navigateToCodeOtpVerification(String nom, String prenoms, String numero, String mdp) {
        progressDialog.dismiss();
        Intent codeOtpVer = new Intent(Inscription.this, CodeOtpVerification.class);
        codeOtpVer.putExtra("numero", numero);
        codeOtpVer.putExtra("nom", nom);
        codeOtpVer.putExtra("prenoms", prenoms);
        codeOtpVer.putExtra("mdp", mdp);
        codeOtpVer.putExtra("caller_activity", "inscription");
        Inscription.this.finish();
        startActivity(codeOtpVer);
    }

    private void navigateToMessageNon(String message) {
        progressDialog.dismiss();
        Inscription.this.finish();
        Intent i = new Intent(Inscription.this, Message_non.class);
        i.putExtra("msg_desc", message);
        i.putExtra("class", "com.sicmagroup.tondi.Inscription");
        startActivity(i);
    }

    private void showProgressDialog(String message) {
        progressDialog = new ProgressDialog(Inscription.this);
        progressDialog.setCancelable(false);
        progressDialog.setMessage(message);
        progressDialog.show();
    }

    private boolean verifySim(final String numero) {
        final String reseau = utilitaire.getOperatorByNumber(numero);
        final boolean[] state = {false};
        String phoneNumber = reseau.equals("MTN") ? "*136*8#" : "*124#";

        ussdApi = USSDController.getInstance(Inscription.this);
        ussdApi.callUSSDInvoke(phoneNumber, map, new USSDController.CallbackInvoke() {
            @Override
            public void responseInvoke(String message) {
                Log.e("APPUSSpp11", message);
            }

            @Override
            public void over(String message) {
                state[0] = Prefs.getString(NUMERO_VERIFYED, "inconnu").equals("oui");
            }
        });

        return state[0];
    }

    private void verifierSimPrime(final String numero) {
        final String reseau = utilitaire.getOperatorByNumber(numero);
        String phoneNumber = reseau.equals("MTN") ? "*136*8#" : "*124#";

        ussdApi = USSDController.getInstance(Inscription.this);
        ussdApi.callUSSDInvoke(phoneNumber, map, new USSDController.CallbackInvoke() {
            @Override
            public void responseInvoke(String message) {
                Log.e("APPUSSpp11", message);
            }

            @Override
            public void over(String message) {
                handleUssdResponse(message, numero, reseau);
            }
        });
    }

    private void handleUssdResponse(String message, String numero, String reseau) {
        Pattern pattern_msg_ok;
        Pattern pattern_msg_ok1 = null;
        Pattern pattern_msg_ok2;
        Pattern pattern_msg_ok3;
        Pattern pattern_msg_ok4;

        if (reseau.equals("MTN")) {
            pattern_msg_ok = Pattern.compile("229" + numero);
            pattern_msg_ok1 = Pattern.compile("ProblèmedeconnexionoucodeIHMnonvalide");
            pattern_msg_ok2 = Pattern.compile("MMInonvalide");
            pattern_msg_ok3 = Pattern.compile("Invalidservicecode");
            pattern_msg_ok4 = Pattern.compile("229" + "\\d");
        } else {
            pattern_msg_ok = Pattern.compile("229" + numero);
            pattern_msg_ok2 = Pattern.compile("MMInonvalide");
            pattern_msg_ok3 = Pattern.compile("Invalidservicecode");
            pattern_msg_ok4 = Pattern.compile("229" + "\\d");
        }

        Matcher matcher = pattern_msg_ok.matcher(message.replaceAll("\\s", ""));
        Matcher matcher_connexion_2 = pattern_msg_ok2.matcher(message.replaceAll("\\s", ""));
        Matcher matcher_connexion_3 = pattern_msg_ok3.matcher(message.replaceAll("\\s", ""));
        Matcher matcher_sim_non_4 = pattern_msg_ok4.matcher(message.replaceAll("\\s", ""));
        Matcher matcher_connexion = pattern_msg_ok1.matcher(message.replaceAll("\\s", ""));

        if (matcher.find()) {
            inscrire();
        } else {
            handleUssdError(matcher_sim_non_4, matcher_connexion, matcher_connexion_2, matcher_connexion_3, numero);
        }
    }

    private void handleUssdError(Matcher matcher_sim_non_4, Matcher matcher_connexion, Matcher matcher_connexion_2, Matcher matcher_connexion_3, String numero) {
        if (matcher_sim_non_4.find()) {
            showErrorMessage("La SIM " + numero + " n'a pas pu être vérifié! Veuillez insérer votre SIM (" + numero + ") puis réessayer SVP!");
        } else if (matcher_connexion.find() || matcher_connexion_2.find() || matcher_connexion_3.find()) {
            showErrorMessage("Problème de connexion ou IHM non valide. Veuillez réessayer SVP!");
        } else {
            showErrorMessage("Tondi rencontre des difficultés réseau. Veuillez réessayer plus tard SVP!");
        }
    }

    private void showErrorMessage(String message) {
        Inscription.this.finish();
        Intent i = new Intent(Inscription.this, Message_non.class);
        i.putExtra("mmi", "1");
        i.putExtra("msg_desc", message);
        i.putExtra("class", "com.sicmagroup.tondi.Inscription");
        startActivity(i);
    }

    private void verifier_sim(final String numero) {
        final String reseau = utilitaire.getOperatorByNumber(numero);
        String phoneNumber;

        if (reseau.equals("MTN")) {
            phoneNumber = "*136*8#";
        } else {
            phoneNumber = "*111*2*1#";
        }

        ussdApi = USSDController.getInstance(Inscription.this);
        ussdApi.callUSSDInvoke(phoneNumber, map, new USSDController.CallbackInvoke() {
            @Override
            public void responseInvoke(String message) {
                Log.d("APPUSSpp11", message);
                handleUSSDResponse(message, reseau, numero);
            }

            @Override
            public void over(String message) {
                Log.d("APPUSSpp", message);
                handleUSSDEnd(message, reseau, numero);
            }
        });
    }

    private void handleUSSDResponse(String message, String reseau, String numero) {
        String trimmedMessage = message.replaceAll("\\s", "");

        Pattern pattern_msg_ok = Pattern.compile("229" + numero);
        Pattern pattern_msg_ok1 = Pattern.compile("ProblèmedeconnexionoucodeIHMnonvalide");
        Pattern pattern_msg_ok2 = Pattern.compile("MMInonvalide");
        Pattern pattern_msg_ok3 = Pattern.compile("Invalidservicecode");
        Pattern pattern_msg_ok4 = Pattern.compile("229\\d");

        Matcher matcher = pattern_msg_ok.matcher(trimmedMessage);
        Matcher matcher_connexion_2 = pattern_msg_ok2.matcher(trimmedMessage);
        Matcher matcher_connexion_3 = pattern_msg_ok3.matcher(trimmedMessage);
        Matcher matcher_sim_non_4 = pattern_msg_ok4.matcher(trimmedMessage);

        if (matcher.find()) {
            inscrire();
        } else {
            if (matcher_sim_non_4.find()) {
                showErrorMessage(numero, "La SIM n'a pas pu être vérifiée. Insérez votre SIM et réessayez.");
            } else if (pattern_msg_ok1.matcher(trimmedMessage).find()) {
                showErrorMessage(numero, "Problème de connexion ou code IHM non valide. Veuillez réessayer.");
            } else if (matcher_connexion_2.find() || matcher_connexion_3.find()) {
                showErrorMessage(numero, "Problème de connexion ou MMI non valide. Veuillez réessayer.");
            } else {
                showErrorMessage(numero, "Tondi rencontre des difficultés réseau. Veuillez réessayer plus tard.");
            }
        }
    }

    private void handleUSSDEnd(String message, String reseau, String numero) {
        Log.d("APPUSSpp12", "drtdrq");
    }


    private void inscrire() {
        RequestQueue queue = Volley.newRequestQueue(this);

        // Créer les données JSON à envoyer
        BaseFormElement tel = mFormBuilder.getFormElement(TAG_TEL);
        BaseFormElement mdp = mFormBuilder.getFormElement(TAG_PASS);

        JSONObject postData = new JSONObject();
        try {
            postData.put("numero", tel.getValue());
            postData.put("password", mdp.getValue());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        // Log the data to be sent
        Log.d("PostData", postData.toString());

        // Créer la requête JsonObjectRequest
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                Request.Method.POST, url_inscrire, postData,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d("Response pour tester", response.toString());


                        handleInscriptionResponse(response);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        progressDialog.dismiss();
                        handleVolleyError(error);
                    }
                }
        );

        jsonObjectRequest.setRetryPolicy(new DefaultRetryPolicy(
                5000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
        ));

        // Ajouter la requête à la RequestQueue
        queue.add(jsonObjectRequest);

        progressDialog = new ProgressDialog(Inscription.this);
        progressDialog.setCancelable(false);
        progressDialog.setMessage("Veuillez patienter. Inscription en cours...");
        progressDialog.show();
    }

    int id = 0;
    String customNumber = "";

    private void handleInscriptionResponse(JSONObject response) {
        try {
            if (response.getInt("responseCode") == 0) {
                JSONObject body = response.getJSONObject("body");
                JSONObject user = body.getJSONObject("userDTO");
                id = Integer.parseInt(user.getString("id"));
                customNumber = body.getString("username");
                String user_uuid = user.getString("uuid");
                String token = body.getString("accessToken");
                SharedPreferences sharedPreferences = getSharedPreferences("MyPrefs", MODE_PRIVATE);
                String nom = sharedPreferences.getString("nom", "");
                String prenoms = sharedPreferences.getString("prenoms", "");

                Log.e("Le customNum: ", customNumber);
                Log.e("L'id est : ", String.valueOf(id));
                Log.e("Le token est:", token);

                Utilisateur nouvel_utilisateur = new Utilisateur();
                nouvel_utilisateur.setId_utilisateur(String.valueOf(id));
                nouvel_utilisateur.setNumero(customNumber);
                nouvel_utilisateur.setNom(user.optString("firstName", prenoms));
                nouvel_utilisateur.setPrenoms(user.optString("lastName", nom));
//                nouvel_utilisateur.setPin_acces(user.getString("password"));
//                nouvel_utilisateur.setMdp(user.getString("password"));

                DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                long timestamp_creation = formatter.parse(user.getString("createdAt")).getTime();
                long timestamp_maj = formatter.parse(user.getString("updatedAt")).getTime();

                nouvel_utilisateur.setCreation(timestamp_creation);
                nouvel_utilisateur.setMaj(timestamp_maj);
                nouvel_utilisateur.setConnecter_le(timestamp_maj);
                nouvel_utilisateur.save();

                Sim sim_par_defaut = new Sim();
                sim_par_defaut.setNumero(customNumber);
                sim_par_defaut.setUtilisateur(nouvel_utilisateur);
                sim_par_defaut.setReseau(utilitaire.getOperatorByNumber(customNumber));
                sim_par_defaut.setCreation(timestamp_maj);
                sim_par_defaut.setMaj(timestamp_maj);
                sim_par_defaut.save();

                Prefs.putString(TOKEN, token);
                Prefs.putString(ID_UTILISATEUR_KEY, user.getString("id"));
                Prefs.putString(NOM_KEY, user.optString("firstName", prenoms));
                Prefs.putString(PRENOMS_KEY, user.optString("lastName", nom));
                Prefs.putString(PIN_KEY, "");
                Prefs.putString(PASS_KEY, ""); //cc69caef-172f-4f90-9e92-d54f761eef83
                Prefs.putString(CONNECTER_KEY, String.valueOf(timestamp_creation));
                Prefs.putString(TEL_KEY, customNumber);

                Intent inscription_next_intent = new Intent(Inscription.this, Inscription_next.class);
                inscription_next_intent.putExtra("id_utilisateur", id);
                inscription_next_intent.putExtra("user_uuid", user_uuid);
                inscription_next_intent.putExtra("accessToken", token);
                startActivity(inscription_next_intent);
            } else {
                handleError(response.getString("message"));
            }
        } catch (JSONException e) {
            Log.e("ParsingError", e.getMessage());
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }


    private void handleError(String message) {
        showErrorMessage("", message);
    }

    private void handleVolleyError(VolleyError volleyError) {
        ConstraintLayout mainLayout = findViewById(R.id.layout_inscription);
        String errorMessage;

        if (volleyError instanceof NetworkError || volleyError instanceof AuthFailureError || volleyError instanceof TimeoutError) {
            errorMessage = "Aucune connexion Internet !";
        } else if (volleyError instanceof ServerError) {
            errorMessage = "Impossible de contacter le serveur !";
        } else if (volleyError instanceof ParseError) {
            errorMessage = "Une erreur est survenue !";
        } else {
            errorMessage = "Une erreur inconnue est survenue !";
        }

        Snackbar snackbar = Snackbar.make(mainLayout, errorMessage, Snackbar.LENGTH_INDEFINITE)
                .setAction("RÉESSAYER", new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        inscrire();
                    }
                });

        snackbar.getView().setBackgroundColor(ContextCompat.getColor(Inscription.this, R.color.colorGray));
        snackbar.setActionTextColor(getResources().getColor(R.color.colorPrimaryDark));
        TextView textView = snackbar.getView().findViewById(com.google.android.material.R.id.snackbar_text);
        textView.setTextColor(Color.WHITE);
        snackbar.show();
    }

    private void showErrorMessage(String numero, String message) {
        Inscription.this.finish();

        Intent i = new Intent(Inscription.this, Message_non.class);
        i.putExtra("mmi", "1");
        i.putExtra("nom", element1.getValue());
        i.putExtra("prenoms", element2.getValue());
        i.putExtra("tel", element3.getValue());
        i.putExtra("mdp", element4.getValue());
        i.putExtra("cnf_mdp", element5.getValue());
        i.putExtra("msg_desc", message);
        i.putExtra("class", "com.sicmagroup.tondi.Inscription");
        startActivity(i);
    }

    @Override
    public void onBackPressed() {
        showExitDialog();
    }

    private void showExitDialog() {
        Dialog dialog_alert = new Dialog(Inscription.this);
        dialog_alert.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog_alert.setCancelable(true);
        dialog_alert.setContentView(R.layout.dialog_attention);

        TextView titre = dialog_alert.findViewById(R.id.deco_title);
        titre.setText("Quitter l'application");
        TextView message_deco = dialog_alert.findViewById(R.id.deco_message);
        message_deco.setText("Êtes-vous sûr de vouloir quitter Tondi ?");

        Button oui = dialog_alert.findViewById(R.id.btn_oui);
        oui.setText("Oui");
        oui.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog_alert.cancel();
                Inscription.this.finish();
            }
        });

        Button non = dialog_alert.findViewById(R.id.btn_non);
        non.setText("Non");
        non.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog_alert.cancel();
            }
        });

        dialog_alert.show();
    }
}