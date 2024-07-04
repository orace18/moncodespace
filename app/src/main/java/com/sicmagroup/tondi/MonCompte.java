package com.sicmagroup.tondi;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Message;
import android.os.Parcelable;
import android.provider.MediaStore;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.recyclerview.widget.RecyclerView;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
import com.google.gson.Gson;
//import com.google.mlkit.vision.common.InputImage;
//import com.google.mlkit.vision.text.Text;
//import com.google.mlkit.vision.text.TextRecognition;
//import com.google.mlkit.vision.text.TextRecognizer;
//import com.google.mlkit.vision.text.TextRecognizerOptions;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.orm.SugarRecord;
import com.orm.query.Condition;
import com.orm.query.Select;
import com.pixplicity.easyprefs.library.Prefs;
import com.sicmagroup.formmaster.model.FormElementTextPassword;
import com.sicmagroup.formmaster.model.FormElementTextPhone;
import com.sicmagroup.formmaster.model.FormElementTextSingleLine;
import com.sicmagroup.tondi.Enum.TontineEnum;
import com.sicmagroup.tondi.utils.Constantes;
import com.squareup.picasso.Picasso;
import com.tapadoo.alerter.Alerter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.sicmagroup.formmaster.FormBuilder;
import com.sicmagroup.formmaster.model.BaseFormElement;

import static com.sicmagroup.tondi.Connexion.ID_UTILISATEUR_KEY;
import static com.sicmagroup.tondi.Connexion.NOM_KEY;
import static com.sicmagroup.tondi.Connexion.PASS_KEY;
import static com.sicmagroup.tondi.Connexion.PHOTO_CNI_KEY;
import static com.sicmagroup.tondi.Connexion.PHOTO_KEY;
import static com.sicmagroup.tondi.Connexion.PRENOMS_KEY;
import static com.sicmagroup.formmaster.model.BaseFormElement.TYPE_EDITTEXT_PASSWORD;
import static com.sicmagroup.tondi.utils.Constantes.CODE_MARCHAND_KEY;
import static com.sicmagroup.tondi.utils.Constantes.SERVEUR;
import static com.sicmagroup.tondi.utils.Constantes.STATUT_UTILISATEUR;
import static com.sicmagroup.tondi.Connexion.TEL_KEY;
import static com.sicmagroup.tondi.utils.Constantes.accessToken;

import cz.msebera.android.httpclient.Header;

public class MonCompte extends AppCompatActivity {

    private static final int TAG_OLD_PASS = 41;
    private static final int TAG_PASS = 42;
    private static final int TAG_CF_PASS = 43;
    private static final int TAG_PIN = 44;
    private static final int TAG_TEL = 45;
    private static final int TAG_CODE_MERCHANT = 46;
    private RecyclerView form_pass;
    private RecyclerView form_sim;
    private FormBuilder frm_pass_builder;
    private FormBuilder frm_pin_builder;
    private FormBuilder frm_sim_builder;
    private FormBuilder frm_merchant_builder;
    ProgressDialog progressDialog;




    String url_afficher = SERVEUR + "/api/v1/sims/afficher";
    String url_modifier_pin = SERVEUR + "/api/v1/utilisateurs/modifier_pin_acces";
    String url_modifier_pass = SERVEUR + "/api/v1/utilisateurs/modifier_mot_de_passe";
    String url_savesim = SERVEUR + "/api/v1/sims/nouvelle";
    String url_link_merchant = SERVEUR + "/api/v1/marchands/lier_marchand";
    String url_get_linked_merchant = SERVEUR + "/api/v1/marchands/get_merchant_details";
    private ArrayList<Sim> paiementList;
    private PaiementAdapter adapter;
    private RecyclerView recyclerView;

    Bitmap myBitmap;
    Uri picUri;
    Utilitaire utilitaire;

    private ArrayList<String> permissionsToRequest;
    private ArrayList<String> permissionsRejected = new ArrayList<>();
    private ArrayList<String> permissions = new ArrayList<>();

    private final static int ALL_PERMISSIONS_RESULT = 107;
    String medias_url = SERVEUR + "/medias/";

    public static boolean isMultiSimEnabled = false;
    public static List<SubscriptionInfo> subInfoList;
    public static ArrayList<String> numbers;
    private SubscriptionManager subscriptionManager;
    static final Integer PHONESTATS = 0x1;
    private final String TAG = "SIMINFO";
    private ConstraintLayout expandeSecurite;
    private ImageButton expandedSecuriteBtn;
    private LinearLayout securiteFormLayout;
    private LinearLayout merchantFormLayout;
    private boolean isSecuriteExpanded = false;
    private boolean isMerchantDetailsExpanded = false;
    private ConstraintLayout expandeMerchantBox;
    private ImageButton expandedMerchantBtn;
    private LinearLayout MerchantFormLayout;
    private RecyclerView merchandForm;
    private TextView cardMerchantTitle;
    private Button btn_pmerchant;
    private ProgressBar progressBar;
    private LinearLayout linearLayoutDetails;
    private static AsyncHttpClient client = new AsyncHttpClient();
    private TextView name_merchant;
    private TextView tel_merchant;
    private TextView code_merchant;

    TextView cni_details;

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_compte_2);
        new Prefs.Builder()
                .setContext(this)
                .setMode(ContextWrapper.MODE_PRIVATE)
                .setPrefsName(getPackageName())
                .setUseDefaultSharedPreference(true)
                .build();
        utilitaire = new Utilitaire(this);
        setupForm();
//        recyclerView = (RecyclerView) findViewById(R.id.recycler_paiement);
//        paiementList = new ArrayList<>();
//        adapter = new PaiementAdapter(this, paiementList);
//        RecyclerView.LayoutManager mLayoutManager = new GridLayoutManager(this, 1);
//        recyclerView.setLayoutManager(mLayoutManager);
//        recyclerView.setItemAnimator(new DefaultItemAnimator());
//        recyclerView.setAdapter(adapter);
//        afficherOptionPaiement();
        //askForPermission(Manifest.permission.READ_PHONE_STATE, PHONESTATS);
        //getClientPhoneNumber();
        SharedPreferences sharedPreferences = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        String lastname = sharedPreferences.getString("nom", "");
        String firstname = sharedPreferences.getString("prenoms", "");


        String nom = Prefs.getString(NOM_KEY, lastname);
        String prenoms = Prefs.getString(PRENOMS_KEY, firstname);
        TextView user_name = (TextView) findViewById(R.id.username);
        user_name.setText(nom + " " + prenoms);
        user_name.setSelected(true);
        TextView solde_compte = findViewById(R.id.solde_compte);
        expandeSecurite = (ConstraintLayout) findViewById(R.id.expand_securite);
        expandedSecuriteBtn = (ImageButton) findViewById(R.id.expanded_btn);
        securiteFormLayout = (LinearLayout) findViewById(R.id.securite_form_layout);
        merchantFormLayout = (LinearLayout) findViewById(R.id.merchant_form_layout);
        cardMerchantTitle = (TextView) findViewById(R.id.title_card_marchand);
        btn_pmerchant = (Button) findViewById(R.id.btn_change_merchant);
        progressBar = (ProgressBar) findViewById(R.id.progress_circ);
        linearLayoutDetails = (LinearLayout) findViewById(R.id.merchant_details);
        name_merchant = (TextView) findViewById(R.id.merchant_name);
        tel_merchant = (TextView) findViewById(R.id.merchant_tel);
        code_merchant = (TextView) findViewById(R.id.merchant_code);
        expandedMerchantBtn = (ImageButton) findViewById(R.id.expanded_merchant_btn);
        expandeMerchantBox = (ConstraintLayout) findViewById(R.id.expand_merchant);
        merchandForm = (RecyclerView) findViewById(R.id.form_merchant);

        double solde = 0.00;
        // liste tontines encours et terminees
        List<Tontine>  list_tontines =Select.from(Tontine.class)
                .where(Condition.prop("id_utilisateur").eq(Long.valueOf(Prefs.getString(ID_UTILISATEUR_KEY,null))))
                .where(Condition.prop("statut").eq(TontineEnum.IN_PROGRESS.toString()))
                .whereOr(Condition.prop("statut").eq(TontineEnum.COMPLETED.toString()))
                .list();
        if(list_tontines.size()>0){
            for (Tontine tontine:list_tontines){
                solde=solde+tontine.getMontant();
            }
        }

        Utilisateur u = new Utilisateur().getUser(Prefs.getString(TEL_KEY, null));
        solde_compte.setText("Votre solde est : "+new DecimalFormat("##.##").format(u.getSolde())+" F CFA");

        ImageView user_avatar = findViewById(R.id.avatar);

        ContextWrapper cw = new ContextWrapper(this);
        File directory = cw.getDir("tontine_photos", Context.MODE_PRIVATE);
        String photo_identite = Prefs.getString(PHOTO_KEY, "");
        // Create imageDir
        File mypath = new File(directory, photo_identite + ".png");
        Picasso.get().load(mypath).transform(new Dashboard.CircleTransform()).into(user_avatar);


        ImageView photo_cni_view = findViewById(R.id.photo_cni_cmpte);

        String photo_cni = Prefs.getString(PHOTO_CNI_KEY, "");
        File mypath_cni = new File(directory, photo_cni + ".png");
        Picasso.get().load(mypath_cni).into(photo_cni_view);
        Log.e("photo", photo_cni);
        if(!isSecuriteExpanded){
            expandedSecuriteBtn.setRotationX(0.0F);
        } else {
            expandedSecuriteBtn.setRotationX(180.0F);
        }

        if(!isMerchantDetailsExpanded){
            expandedMerchantBtn.setRotationX(0.0F);
        } else {
            expandedMerchantBtn.setRotationX(180.0F);
        }
        expandedSecuriteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isSecuriteExpanded){
//                    expandedSecuriteBtn.setRotationX(0.0F);
                    RotateAnimation rotateAnimation = new RotateAnimation(180, 0, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
                    rotateAnimation.setFillAfter(true);
                    rotateAnimation.setDuration(200);
                    expandedSecuriteBtn.setAnimation(rotateAnimation);
//                    securiteFormLayout.setVisibility(View.VISIBLE);
                    slideUp(securiteFormLayout);
                } else {
//                    expandedSecuriteBtn.setRotationX(180.0F);
                    RotateAnimation rotateAnimation = new RotateAnimation(0.0F, 180.0F, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
                    rotateAnimation.setDuration(200);
                    rotateAnimation.setFillAfter(true);
                    expandedSecuriteBtn.setAnimation(rotateAnimation);
//                    securiteFormLayout.setVisibility(View.GONE);
                    slideDown(securiteFormLayout);
                }
                isSecuriteExpanded = !isSecuriteExpanded;
            }
        });

        expandedMerchantBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isMerchantDetailsExpanded){
//                    expandedMerchantBtn.setRotationX(0.0F);
                    RotateAnimation rotateAnimation = new RotateAnimation(180.0F, 0.0F, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
                    rotateAnimation.setFillAfter(true);
                    rotateAnimation.setDuration(200);
                    expandedMerchantBtn.setAnimation(rotateAnimation);
                    slideUp(merchantFormLayout);
                } else {
//                    expandedMerchantBtn.setRotationX(180.0F);
                    RotateAnimation rotateAnimation = new RotateAnimation(0.0F, 180.0F, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
                    rotateAnimation.setDuration(200);
                    rotateAnimation.setFillAfter(true);
                    expandedMerchantBtn.setAnimation(rotateAnimation);
                    slideDown(merchantFormLayout);
                }
                isMerchantDetailsExpanded = !isMerchantDetailsExpanded;
            }
        });

        //constraint layout on click
        expandeSecurite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.e("isSecurityExpan", isSecuriteExpanded+"");
                if(isSecuriteExpanded){
//                    expandedSecuriteBtn.setRotationX(0.0F);
                    RotateAnimation rotateAnimation = new RotateAnimation(180.0F, 0.0F, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
                    rotateAnimation.setFillAfter(true);
                    rotateAnimation.setDuration(200);
                    expandedSecuriteBtn.setAnimation(rotateAnimation);
//                    securiteFormLayout.setVisibility(View.VISIBLE);
                    slideUp(securiteFormLayout);
                } else {
//                    expandedSecuriteBtn.setRotationX(180.0F);
                    RotateAnimation rotateAnimation = new RotateAnimation(0.0F, 180.0F, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
                    rotateAnimation.setDuration(200);
                    rotateAnimation.setFillAfter(true);
                    expandedSecuriteBtn.setAnimation(rotateAnimation);
//                    securiteFormLayout.setVisibility(View.GONE);
                    slideDown(securiteFormLayout);
                }
                isSecuriteExpanded = !isSecuriteExpanded;

            }
        });
        expandeMerchantBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isMerchantDetailsExpanded){
//                    expandedMerchantBtn.setRotationX(0.0F);
                    RotateAnimation rotateAnimation = new RotateAnimation(180.0F, 0.0F, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
                    rotateAnimation.setFillAfter(true);
                    rotateAnimation.setDuration(200);
                    expandedMerchantBtn.setAnimation(rotateAnimation);
                    slideUp(merchantFormLayout);
                } else {
//                    expandedMerchantBtn.setRotationX(180.0F);
                    RotateAnimation rotateAnimation = new RotateAnimation(0.0F, 180.0F, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
                    rotateAnimation.setDuration(200);
                    rotateAnimation.setFillAfter(true);
                    expandedMerchantBtn.setAnimation(rotateAnimation);
                    slideDown(merchantFormLayout);
                }
                isMerchantDetailsExpanded = !isMerchantDetailsExpanded;
            }
        });

        if(Prefs.getString(CODE_MARCHAND_KEY, "").equals("")){
            cardMerchantTitle.setText("Lier un marchand");
            //afficher le formulaire
            setupFormMerchand();
            merchandForm.setVisibility(View.VISIBLE);
            btn_pmerchant.setVisibility(View.VISIBLE);
            progressBar.setVisibility(View.GONE);
            linearLayoutDetails.setVisibility(View.GONE);
        } else {
            cardMerchantTitle.setText("Détails");
            //recupérer les détails du marchand
            merchandForm.setVisibility(View.GONE);
            btn_pmerchant.setVisibility(View.GONE);
            showMerchantDetails();

        }

//        if(utilitaire.isConnected())
//        {
//            Picasso.get().load(medias_url+Prefs.getString(PHOTO_KEY,null)+".JPG").transform(new Dashboard.CircleTransform()).into(user_avatar);
//        }

        //Picasso.get().load(medias_url + Prefs.getString(PHOTO_KEY, null) + ".JPG").transform(new Dashboard.CircleTransform()).into(user_avatar);

        Button back_to =  findViewById(R.id.back_to);
        back_to.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                MonCompte.this.finish();
                startActivity(new Intent(MonCompte.this, Home.class));
            }
        });
//
//        Button btn_deconnexion = (Button) findViewById(R.id.btn_deconnexion);
//        btn_deconnexion.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                // TODO Auto-generated method stub
//                alertView("Déconnexion", "Êtes vous sûr de vouloir vous déconnecter?", null);
//
//            }
//        });
//
//        Button btn_accueil = findViewById(R.id.btn_accueil);
//        btn_accueil.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                MonCompte.this.finish();
//                startActivity(new Intent(MonCompte.this, Dashboard.class));
//            }
//        });
//
//        Button btn_about = findViewById(R.id.btn_about);
//        btn_about.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                MonCompte.this.finish();
//                startActivity(new Intent(MonCompte.this, About_us.class));
//            }
//        });

        /*Button edit_avatar = (Button)findViewById(R.id.edit_avatar);
        edit_avatar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                startActivityForResult(getPickImageChooserIntent(), 200);

            }
        });

        permissions.add(CAMERA);
        permissionsToRequest = findUnAskedPermissions(permissions);
        //get the permissions we have asked for before but are not granted..
        //we will store this in a global list to access later.


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {


            if (permissionsToRequest.size() > 0)
                requestPermissions(permissionsToRequest.toArray(new String[permissionsToRequest.size()]), ALL_PERMISSIONS_RESULT);
        }*/

        // clique sur nouvelle sim
//        Button btn_nouvelle_sim = (Button) findViewById(R.id.btn_add_paiement);
//        btn_nouvelle_sim.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                // TODO Auto-generated method stub
//                MonCompte.ViewDialog alert = new MonCompte.ViewDialog();
//                alert.showDialog(MonCompte.this);
//            }
//        });

        //Si l'utilisateur est désactivé de façon temporaire
//        if (Prefs.contains(STATUT_UTILISATEUR))
//        {
//            switch (Prefs.getString(STATUT_UTILISATEUR, null)) {
//                case "desactive temp":
//                    btn_nouvelle_sim.setEnabled(false);
//                    break;
//                case "active":
//                    btn_nouvelle_sim.setEnabled(true);
//                    break;
//            }
//        }

//        //les cgu
//        TextView txt_cgu = (TextView)findViewById(R.id.txt_cgu);
//        txt_cgu.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                // TODO Auto-generated method stub
//                startActivity(new Intent(MonCompte.this,CGU.class));
//            }
//        });

        //OCR MODULE
//        cni_details = (TextView) findViewById(R.id.cni_details);
//        InputImage image;
//        try {
//            image = InputImage.fromFilePath(this, Uri.fromFile(mypath_cni));
//            cni_details.setVisibility(View.VISIBLE);
//            TextRecognizer recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS);
//            Task<Text> result = recognizer.process(image)
//                    .addOnSuccessListener(new OnSuccessListener<Text>() {
//                        @Override
//                        public void onSuccess(@NonNull Text text) {
//                            String resultText = text.getText();
//                            String f = "";
//                            for (Text.TextBlock block : text.getTextBlocks()) {
//                                String blockText = block.getText();
//                                Point[] blockCornerPoints = block.getCornerPoints();
//                                Rect blockFrame = block.getBoundingBox();
//                                for (Text.Line line : block.getLines()) {
//                                    String lineText = line.getText();
//                                    Point[] lineCornerPoints = line.getCornerPoints();
//                                    Rect lineFrame = line.getBoundingBox();
//                                    for (Text.Element element : line.getElements()) {
//                                        String elementText = element.getText();
//                                        Point[] elementCornerPoints = element.getCornerPoints();
//                                        Rect elementFrame = element.getBoundingBox();
//                                        f += elementText;
//                                    }
//                                }
//                            }
//                            cni_details.setText(f);
//                        }
//                    })
//                    .addOnFailureListener(new OnFailureListener() {
//                        @Override
//                        public void onFailure(@NonNull Exception e) {
//                            cni_details.setText("Error");
//                        }
//                    });
//        }
//        catch (IOException e)
//        {
//            e.printStackTrace();
//        }
    }

    //show merchant_details
    public void showMerchantDetails() {
        RequestParams params = new RequestParams();
        params.put("codeReferent", Prefs.getString(CODE_MARCHAND_KEY, ""));

        AsyncHttpClient client = new AsyncHttpClient();
        // String authorizationToken = Prefs.getString(AUTHORIZATION_KEY, "");

        // Ajouter l'autorisation dans le header
        client.addHeader("Authorization", "Bearer " + accessToken);

        client.post(Constantes.URL_GET_MERCHANT_DETAILS, params, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                super.onSuccess(statusCode, headers, response);
                try {
                    if (response.getInt("responseCode") == 0) {
                        JSONObject body = response.getJSONObject("body");
                        name_merchant.setText(body.getString("nom") + " " + body.getString("prenoms"));
                        code_merchant.setText(body.getString("code_marchand"));
                        tel_merchant.setText(body.getString("numero"));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                Log.i("ErreurAsync", "We got a jsonObject");
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, String responseString) {
                super.onSuccess(statusCode, headers, responseString);
                Log.i("ErreurAsync", "We got a responseString");
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONArray response) {
                super.onSuccess(statusCode, headers, response);
                Log.i("ErreurAsync", "We got an array");
            }

            @Override
            public void onStart() {
                super.onStart();
                progressBar.setVisibility(View.VISIBLE);
                linearLayoutDetails.setVisibility(View.GONE);
            }

            @Override
            public void onFinish() {
                super.onFinish();
                progressBar.setVisibility(View.GONE);
                linearLayoutDetails.setVisibility(View.VISIBLE);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                super.onFailure(statusCode, headers, throwable, errorResponse);
                Log.e("ErreurAsync", errorResponse != null ? errorResponse.toString() : "Error response is null");
            }

//            @Override
//            public void onFailure(int statusCode, Header[] headers, Throwable throwable, String errorResponse) {
//                super.onFailure(statusCode, headers, errorResponse, throwable);
//                Log.e("ErreurAsync", errorResponse != null ? errorResponse : "Error response is null");
//            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONArray errorResponse) {
                super.onFailure(statusCode, headers, throwable, errorResponse);
                Log.e("ErreurAsync", errorResponse != null ? errorResponse.toString() : "Error response is null");
            }
        });
    }
    // slide the view from below itself to the current position
    public void slideUp(View view){
        view.setVisibility(View.VISIBLE);
        TranslateAnimation animate = new TranslateAnimation(
                0,                 // fromXDelta
                0,                 // toXDelta
                -view.getHeight(),  // fromYDelta
                0);                // toYDelta
        animate.setDuration(500);
        animate.setFillAfter(true);
        view.startAnimation(animate);
    }

    // slide the view from its current position to below itself
    public void slideDown(View view){
        TranslateAnimation animate = new TranslateAnimation(
                0,                 // fromXDelta
                0,                 // toXDelta
                0,                 // fromYDelta
                -view.getHeight()); // toYDelta
        animate.setDuration(500);
        animate.setFillAfter(true);
        view.startAnimation(animate);
        view.setVisibility(View.GONE);
    }
    @Override
    public void onBackPressed() {
        // your code.
        MonCompte.this.finish();
        startActivity(new Intent(MonCompte.this, Home.class));
    }


    private void afficherOptionPaiement() {
        Utilisateur utilisateur = Utilisateur.find(Utilisateur.class, "id_utilisateur = ?", Prefs.getString(ID_UTILISATEUR_KEY,null)).get(0);
        List<Sim> sims = Sim.find(Sim.class, "utilisateur = ?", String.valueOf(utilisateur.getId()));
        String reseau;
        String numero;

        for (int i = 0; i < sims.size(); i++) {
            reseau = sims.get(i).getReseau();
            numero = sims.get(i).getNumero();
            Sim b = new Sim();
            b.setReseau(reseau);
            b.setNumero(numero);
            paiementList.add(b);
        }

        adapter.notifyDataSetChanged();

    }

    private void setupForm() {

        form_pass = (RecyclerView) findViewById(R.id.form_pass);
        frm_pass_builder = new FormBuilder(this, form_pass);

        final FormElementTextPassword element1 = FormElementTextPassword.createInstance().setTag(TAG_OLD_PASS).setTitle("Ancien").setHint("Ancien mot de passe").setRequired(true);
        final FormElementTextPassword element2 = FormElementTextPassword.createInstance().setTag(TAG_PASS).setTitle("Nouveau").setHint("Nouveau mot de passe").setRequired(true);
        final FormElementTextPassword element3 = FormElementTextPassword.createInstance().setTag(TAG_CF_PASS).setTitle("Confirmer").setHint("Confimer nouveau mot de passe").setRequired(true).setType(TYPE_EDITTEXT_PASSWORD);
        /*password.setInputType(InputType.TYPE_CLASS_TEXT |
                InputType.TYPE_TEXT_VARIATION_PASSWORD);*/

        List<BaseFormElement> formItems = new ArrayList<>();
        formItems.add(element1);
        formItems.add(element2);
        formItems.add(element3);
        frm_pass_builder.addFormElements(formItems);

        // clique sur enregistrer mon mot de passe
        Button btn_pass = findViewById(R.id.btn_change_pass);
        btn_pass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                if (!frm_pass_builder.isValidForm()) {
                    String msg = "Vous devez remplir tous les champs.";
                    alertView_simple("Erreurs dans le formulaire", msg);
                } else {
                    boolean flag = false;
                    String msg = "";
                    String old_pass = Prefs.getString(PASS_KEY, null);
                    Connexion.AeSimpleSHA1 AeSimpleSHA1 = new Connexion.AeSimpleSHA1();
                    String ins_old_pass = element1.getValue();
                    try {
                        ins_old_pass = AeSimpleSHA1.md5(ins_old_pass);
                        ins_old_pass = AeSimpleSHA1.SHA1(ins_old_pass);
                        //Log.d("ins_old_pass",ins_old_pass);
                    } catch (NoSuchAlgorithmException e) {
                        e.printStackTrace();
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                    if (!ins_old_pass.equals(old_pass)) {
                        msg = msg + "> L'Ancien mot de passe est incorrect. Veuillez réessayer SVP! \n";
                        flag = true;
                    }
                    if (element2.getValue().length() < 6 || element3.getValue().length() < 6) {
                        msg = msg + "> Le Nouveau mot de passe doit être composé au minimum de 6 caractères. \n";
                        flag = true;
                    }

                    if (!element2.getValue().equals(element3.getValue())) {
                        msg = msg + "> Le Nouveau mot de passe et Confirmer ne correspondent pas. \n";
                        flag = true;
                    }

                    if (flag) {
                        alertView_simple("Erreurs dans le formulaire", msg);
                    } else {
                        savePass();
                    }

                }


            }
        });

        //Si l'utilisateur est désactivé de façon temporaire
        if (Prefs.contains(STATUT_UTILISATEUR))
        {
            switch (Prefs.getString(STATUT_UTILISATEUR, null)) {
                case "desactive temp":
                    btn_pass.setEnabled(false);
                    break;
                case "active":
                    btn_pass.setEnabled(true);
                    break;
            }
        }

    }

    private void setupFormMerchand() {

        frm_merchant_builder = new FormBuilder(this, merchandForm);

        final FormElementTextSingleLine element1 = FormElementTextSingleLine.createInstance().setTag(TAG_CODE_MERCHANT).setTitle("Code marchand").setValue("COM#").setRequired(true);

        List<BaseFormElement> formItems = new ArrayList<>();
        formItems.add(element1);

        frm_merchant_builder.addFormElements(formItems);

        // clique sur enregistrer mon mot de passe
//        Button btn_pmerchant = findViewById(R.id.btn_change_merchant);
        btn_pmerchant.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                if (!frm_merchant_builder.isValidForm()) {
                    String msg = "Vous devez remplir ce champs.";
                    alertView_simple("Erreurs dans le formulaire", msg);
                } else {
                    boolean flag = false;
                    String msg = "";

                    if (element1.getValue().length() <= 4) {
                        msg = msg + "Code marchand incorrect \n";
                        flag = true;
                    }

                    if (flag) {
                        alertView_simple("Erreurs dans le formulaire", msg);
                    } else {
                       //envoie de la requête
                        linkMerchant(element1.getValue());
                    }

                }


            }
        });

        //Si l'utilisateur est désactivé de façon temporaire
        if (Prefs.contains(STATUT_UTILISATEUR))
        {
            switch (Prefs.getString(STATUT_UTILISATEUR, null)) {
                case "desactive temp":
                    btn_pmerchant.setEnabled(false);
                    break;
                case "desactive":
                    btn_pmerchant.setEnabled(false);
                    break;
                case "active":
                    btn_pmerchant.setEnabled(true);
                    break;
            }
        }

    }

    private void linkMerchant(String codeMarchand){
        RequestQueue queue = Volley.newRequestQueue(MonCompte.this);
        StringRequest postRequest = new StringRequest(Request.Method.POST, Constantes.URL_LINK_MERCHANT_TO_USER, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.d("onResponse: ", response);
                try {
                    JSONObject result = new JSONObject(response);
                        if (result.getInt("responseCode") == 0) {
                            Prefs.putString(CODE_MARCHAND_KEY, codeMarchand);
                            Intent i = new Intent(MonCompte.this, Message_ok.class);
                            i.putExtra("class", "com.sicmagroup.tondi.Home");
                            i.putExtra("msg_desc", "Affectation du marchand à votre compte réussie");
                            startActivity(i);
                        } else {
                            CoordinatorLayout mainLayout = (CoordinatorLayout) findViewById(R.id.mon_compte_layout);
                            Snackbar.make(mainLayout, "Code de marchand invalide!", Snackbar.LENGTH_SHORT);
                        }
                } catch (Throwable t) {
                    Log.d("link_merchand", String.valueOf(t.getCause()));
                    CoordinatorLayout mainLayout = (CoordinatorLayout) findViewById(R.id.mon_compte_layout);
                    Snackbar.make(mainLayout, "Une erreur est survenue. Réessayer plus tard, merci.", Snackbar.LENGTH_SHORT);
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                CoordinatorLayout mainLayout = (CoordinatorLayout) findViewById(R.id.mon_compte_layout);
                Snackbar.make(mainLayout, "Une erreur est survenue. Réessayez svp!", Snackbar.LENGTH_SHORT);
            }
        }){
            @Nullable
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("code_referent", codeMarchand);
                params.put("customer_number", Prefs.getString(TEL_KEY, ""));
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

    private void alertView( String title ,String message, String action ) {
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        final String finalAction = action;
        dialog.setTitle( title )
                .setIcon(R.drawable.ic_warning)
                .setMessage(message)
                .setNegativeButton("Non", new DialogInterface.OnClickListener() {
                  public void onClick(DialogInterface dialoginterface, int i) {
                      dialoginterface.cancel();
                      }})
                .setPositiveButton("Oui", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialoginterface, int i) {
                        if (finalAction!=null){
                            // si pin
                            if (finalAction.equals("pin")){
                                savePin();
                            }
                            // si sim
                            if (finalAction.equals("sim")){
                                saveSim();
                            }

                        }else{
                            Prefs.putString(ID_UTILISATEUR_KEY,null);
                            MonCompte.this.finish();
                            startActivity(new Intent(MonCompte.this,Connexion.class));
                        }

                    }
                }).show();
    }

    private void alertView_simple( String title ,String message ) {
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle( title )
                .setIcon(R.drawable.ic_warning)
                .setMessage(message)
//     .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
//      public void onClick(DialogInterface dialoginterface, int i) {
//          dialoginterface.cancel();
//          }})
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialoginterface, int i) {
                    }
                }).show();
    }

    @SuppressLint("ResourceAsColor")
    private void savePin() {
        BaseFormElement pin = frm_pin_builder.getFormElement(TAG_PIN);
        final String pin_value = pin.getValue();

        Utilisateur utilisateur_modifie = Utilisateur.find(Utilisateur.class, "id_utilisateur = ?", Prefs.getString(ID_UTILISATEUR_KEY,null)).get(0);// Utilisateur.findById(Utilisateur.class, Long.valueOf(Prefs.getString(ID_UTILISATEUR_KEY,null)));
        utilisateur_modifie.setPin_acces(pin_value);
        utilisateur_modifie.save();
        Long id_user = utilisateur_modifie.getId();
        if (id_user!=null){
            Prefs.putString("pin",pin_value);

            // enregistrer dans synchroniser
            Date currentTime = Calendar.getInstance().getTime();
            long output_maj=currentTime.getTime()/1000L;
            String str_maj=Long.toString(output_maj);
            long timestamp_maj = Long.parseLong(str_maj) * 1000;
            try {
                Synchronisation new_sync = new Synchronisation();
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("a","pin#compte");
                jsonObject.put("n",utilisateur_modifie.getNumero());
                Gson gson = new Gson();
                String pin_json = gson.toJson(utilisateur_modifie);
                jsonObject.put("d",pin_json);
                new_sync.setMaj(timestamp_maj);
                new_sync.setStatut(0);
                new_sync.setDonnees(jsonObject.toString());
                new_sync.save();


            } catch (JSONException e) {
                e.printStackTrace();
            }
            Utilitaire utilitaire = new Utilitaire(MonCompte.this);
            if (utilitaire.isConnected()){
                utilitaire.synchroniser_en_ligne();
            }
            String msg="Votre PIN d'Accès a été correctement modifié ";
            Intent i = new Intent(MonCompte.this, Message_ok.class);
            i.putExtra("msg_desc",msg);
            i.putExtra("class","com.sicmagroup.tondi.MonCompte");
            startActivity(i);
        }else{
            String msg="Un problème est survenu lors de la modification. Veuillez réessayer SVP! ";
            Intent i = new Intent(MonCompte.this, Message_non.class);
            i.putExtra("msg_desc",msg);
            i.putExtra("class","com.sicmagroup.tondi.MonCompte");
            startActivity(i);
            /*Alerter.create(MonCompte.this)
                    .setTitle("Un problème est survenu lors de la modification. Veuillez réessayer SVP!")
                    .setIcon(R.drawable.ic_warning)
                    .setTitleAppearance(R.style.TextAppearance_AppCompat_Large)
                    .setIconColorFilter(R.color.colorPrimaryDark)
                    //.setText("Vous pouvez maintenant vous connecter.")
                    .setBackgroundColorRes(R.color.colorWhite) // or setBackgroundColorInt(Color.CYAN)
                    .show();*/
        }

    }
    /*private void savePin() {
        BaseFormElement pin = frm_pin_builder.getFormElement(TAG_PIN);
        final String pin_value = pin.getValue();
        RequestQueue queue = Volley.newRequestQueue(this);
        StringRequest postRequest = new StringRequest(Request.Method.POST, url_modifier_pin,
                new Response.Listener<String>()
                {
                    @SuppressLint("ResourceAsColor")
                    @Override
                    public void onResponse(String response) {
                        // response
                        Log.d("Response", response);
                        try {

                            JSONObject result = new JSONObject(response);
                            //Log.d("My App", obj.toString());
                            if (result.getBoolean("success")){
                                Prefs.putString("pin",pin_value);
                                Alerter.create(MonCompte.this)
                                        .setTitle(result.getString("message"))
                                        .setIcon(R.drawable.ic_check)
                                        //.setTitleAppearance(R.style.TextAppearance_AppCompat_Large)
                                        //.setIconColorFilter(R.color.colorPrimaryDark)
                                        //.setText("Vous pouvez maintenant vous connecter.")
                                        .setBackgroundColorRes(R.color.colorPrimaryDark) // or setBackgroundColorInt(Color.CYAN)
                                        .show();
                            }else{
                                Alerter.create(MonCompte.this)
                                        .setTitle(result.getString("message"))
                                        .setIcon(R.drawable.ic_warning)
                                        .setTitleAppearance(R.style.TextAppearance_AppCompat_Large)
                                        .setIconColorFilter(R.color.colorPrimaryDark)
                                        //.setText("Vous pouvez maintenant vous connecter.")
                                        .setBackgroundColorRes(R.color.colorWhite) // or setBackgroundColorInt(Color.CYAN)
                                        .show();
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
                    }
                }
        ) {
            @Override
            protected Map<String, String> getParams()
            {

                Map<String, String>  params = new HashMap<String, String>();

                params.put("pin", pin_value);
                params.put("id_utilisateur", Prefs.getString(ID_UTILISATEUR_KEY,null));

                return params;
            }
        };
        queue.add(postRequest);
    }*/

    @SuppressLint("ResourceAsColor")
    private void savePass() {
        BaseFormElement pass = frm_pass_builder.getFormElement(TAG_PASS);
        String pass_value = pass.getValue();
        Connexion.AeSimpleSHA1 AeSimpleSHA1 = new Connexion.AeSimpleSHA1();

        try {
            pass_value =  AeSimpleSHA1.md5(pass_value);
            pass_value = AeSimpleSHA1.SHA1(pass_value);
            //Log.d("ins_old_pass",ins_old_pass);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        Utilisateur utilisateur_modifie = Utilisateur.find(Utilisateur.class, "id_utilisateur = ?", Prefs.getString(ID_UTILISATEUR_KEY,null)).get(0); //Utilisateur.findById(Utilisateur.class, Long.valueOf(Prefs.getString(ID_UTILISATEUR_KEY,null)));
        utilisateur_modifie.setMdp(pass_value);
        utilisateur_modifie.save();
        Long id_user = utilisateur_modifie.getId();
        if (id_user!=null){
            Prefs.putString(PASS_KEY, pass_value);
            RequestQueue queue = Volley.newRequestQueue(this);
            Log.e("updateDatabse", "swip to reffesh is working");
            StringRequest postRequest = new StringRequest(Request.Method.POST, Constantes.URL_CHANGE_PWD,
                    new Response.Listener<String>()
                    {
                        @SuppressLint("ResourceAsColor")
                        @Override
                        public void onResponse(String response) {
                            Log.e("onResponse: ", response);
                            try {
                                JSONObject result = new JSONObject(response);
                                if (result.getInt("responseCode") == 0) {
                                    String msg="Votre Mot de passe a été correctement modifié ";
                                    Intent i = new Intent(MonCompte.this, Message_ok.class);
                                    i.putExtra("msg_desc",msg);
                                    i.putExtra("class","com.sicmagroup.tondi.MonCompte");
                                    progressDialog.dismiss();
                                    startActivity(i);
                                } else {
                                    String msg=result.getString("body");
                                    Intent i = new Intent(MonCompte.this, Message_non.class);
                                    i.putExtra("msg_desc",msg);
                                    i.putExtra("class","com.sicmagroup.tondi.MonCompte");
                                    progressDialog.dismiss();
                                    startActivity(i);
                                }
                        } catch(Throwable t) {
                            Toast.makeText(MonCompte.this, "Une erreur est survenue", Toast.LENGTH_SHORT).show();
                            progressDialog.dismiss();
                            Log.d("errornscription", String.valueOf(t.getCause()));
                            Log.e("Update Data", t.getMessage());
                        }
                    }
        },
        new Response.ErrorListener()
        {
            @Override
            public void onErrorResponse(VolleyError error) {
                progressDialog.dismiss();
                Toast.makeText(MonCompte.this, "Une erreur est survenue", Toast.LENGTH_SHORT).show();
                Log.e("Error.Synchronisation", String.valueOf(error.getMessage()));
            }
        }
        ) {
            @Override
            protected Map<String, String> getParams()
            {
                BaseFormElement oldpass = frm_pass_builder.getFormElement(TAG_OLD_PASS);
                BaseFormElement pass = frm_pass_builder.getFormElement(TAG_PASS);
                Map<String, String>  params = new HashMap<String, String>();
                params.put("numero",Prefs.getString(TEL_KEY,null));
                params.put("hold_password", oldpass.getValue());
                params.put("new_password", pass.getValue());
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
                35000,
                -1,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        queue.add(postRequest);

            progressDialog = new ProgressDialog(this);
            progressDialog.setCancelable(false);
            progressDialog.setMessage("Mise à jour de votre mot de passe");
            progressDialog.show();


        }else{

            String msg="Une erreur est survenue.";
            Intent i = new Intent(MonCompte.this, Message_non.class);
            i.putExtra("msg_desc",msg);
            i.putExtra("class","com.sicmagroup.tondi.MonCompte");
            startActivity(i);
        }


    }

    @SuppressLint("ResourceAsColor")
    private void saveSim() {
        BaseFormElement numero = frm_sim_builder.getFormElement(TAG_TEL);
        final String numero_value = numero.getValue();
        RequestQueue queue = Volley.newRequestQueue(this);
        Sim nouvelle_sim = new Sim();
        nouvelle_sim.setNumero(numero_value);
        nouvelle_sim.setReseau(utilitaire.getOperatorByNumber(numero_value));
        Utilisateur utilisateur = new Utilisateur();
        utilisateur.setId(Long.valueOf(Prefs.getString(ID_UTILISATEUR_KEY,null)));
        nouvelle_sim.setUtilisateur(utilisateur);
        // maj des dates
        Date currentTime = Calendar.getInstance().getTime();
        long output_creation=currentTime.getTime()/1000L;
        String str_creation=Long.toString(output_creation);
        long timestamp_creation = Long.parseLong(str_creation) * 1000;
        long output_maj=currentTime.getTime()/1000L;
        String str_maj=Long.toString(output_maj);
        long timestamp_maj = Long.parseLong(str_maj) * 1000;
        nouvelle_sim.setCreation(timestamp_creation);
        nouvelle_sim.setMaj(timestamp_maj);
        nouvelle_sim.save();
        Long id_user = nouvelle_sim.getId();
        if (id_user!=null){

            // enregistrer dans synchroniser
            try {
                Utilisateur u = Utilisateur.find(Utilisateur.class, "id_utilisateur = ?", Prefs.getString(ID_UTILISATEUR_KEY,null)).get(0);

                Synchronisation new_sync = new Synchronisation();
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("a","sim#compte");
                jsonObject.put("n",u.getNumero());
                Gson gson = new Gson();
                String sim_json = gson.toJson(nouvelle_sim);
                jsonObject.put("d",sim_json);
                new_sync.setMaj(timestamp_maj);
                new_sync.setStatut(0);
                new_sync.setDonnees(jsonObject.toString());
                new_sync.save();


            } catch (JSONException e) {
                e.printStackTrace();
            }
            Utilitaire utilitaire = new Utilitaire(MonCompte.this);
            if (utilitaire.isConnected()){
                utilitaire.synchroniser_en_ligne();
            }

            String msg="Votre SIM a été ajouté avec succès.";
            Intent i = new Intent(MonCompte.this, Message_ok.class);
            i.putExtra("msg_desc",msg);
            i.putExtra("class","com.sicmagroup.tondi.MonCompte");
            startActivity(i);

            }else{
                Alerter.create(MonCompte.this)
                        .setTitle("Une erreur est survenue lors de l'ajout. Veuillez réessayer SVP!")
                        .setIcon(R.drawable.ic_warning)
                        .setTitleAppearance(R.style.TextAppearance_AppCompat_Large)
                        .setIconColorFilter(R.color.colorPrimaryDark)
                        //.setText("Vous pouvez maintenant vous connecter.")
                        .setBackgroundColorRes(R.color.colorWhite) // or setBackgroundColorInt(Color.CYAN)
                        .show();
            }



    }

    public String OperatorName(String numero) {
        String operator_name = "";
        String[] mtn_prefix_list =  getResources().getStringArray(R.array.mtn_prefix_list);
        String[] moov_prefix_list = getResources().getStringArray(R.array.moov_prefix_list);
        if (Arrays.asList(mtn_prefix_list).contains(""+numero.charAt(0)+numero.charAt(1))) {
            operator_name = "MTN";
        }
        if (Arrays.asList(moov_prefix_list).contains(""+numero.charAt(0)+numero.charAt(1))) {
            operator_name = "MOOV";
        }
        return operator_name ;
    }

    private void show_error( String title ,String message ) {
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle( title )
                .setIcon(R.drawable.ic_warning)
                .setMessage(message)
//     .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
//      public void onClick(DialogInterface dialoginterface, int i) {
//          dialoginterface.cancel();
//          }})
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialoginterface, int i) {
                    }
                }).show();
    }

    void setupSim(Dialog d, Context c){
        form_sim = d.findViewById(R.id.form_sim);
        frm_sim_builder = new FormBuilder(this, form_sim);

        final FormElementTextPhone element5 = FormElementTextPhone.createInstance().setTag(TAG_TEL).setTitle("N° de la SIM").setHint("Renseigner").setRequired(true);


        List<BaseFormElement> formItems2 = new ArrayList<>();
        formItems2.add(element5);
        frm_sim_builder.addFormElements(formItems2);
    }

    public class ViewDialog {

        void showDialog(Activity activity){
            final Dialog dialog = new Dialog(activity);
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.setCancelable(true);
            dialog.setContentView(R.layout.nouvelle_sim);
            setupSim(dialog,dialog.getContext());


            Button dialogButton = (Button) dialog.findViewById(R.id.btn_savesim);
            dialogButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    alertView("Nouvelle SIM","Confirmez-vous l'enregistrement de cette sim? ","sim");
                    dialog.dismiss();

                }
            });
            dialog.show();

        }
    }






    /**
     * Create a chooser intent to select the source to get image from.<br/>
     * The source can be camera's (ACTION_IMAGE_CAPTURE) or gallery's (ACTION_GET_CONTENT).<br/>
     * All possible sources are added to the intent chooser.
     */
    public Intent getPickImageChooserIntent() {

        // Determine Uri of camera image to save.
        Uri outputFileUri = getCaptureImageOutputUri();

        List<Intent> allIntents = new ArrayList<>();
        PackageManager packageManager = getPackageManager();

        // collect all camera intents
        Intent captureIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
        List<ResolveInfo> listCam = packageManager.queryIntentActivities(captureIntent, 0);
        for (ResolveInfo res : listCam) {
            Intent intent = new Intent(captureIntent);
            intent.setComponent(new ComponentName(res.activityInfo.packageName, res.activityInfo.name));
            intent.setPackage(res.activityInfo.packageName);
            if (outputFileUri != null) {
                intent.putExtra(MediaStore.EXTRA_OUTPUT, outputFileUri);
            }
            allIntents.add(intent);
        }

        // collect all gallery intents
        Intent galleryIntent = new Intent(Intent.ACTION_GET_CONTENT);
        galleryIntent.setType("image/*");
        List<ResolveInfo> listGallery = packageManager.queryIntentActivities(galleryIntent, 0);
        for (ResolveInfo res : listGallery) {
            Intent intent = new Intent(galleryIntent);
            intent.setComponent(new ComponentName(res.activityInfo.packageName, res.activityInfo.name));
            intent.setPackage(res.activityInfo.packageName);
            allIntents.add(intent);
        }

        // the main intent is the last in the list (fucking android) so pickup the useless one
        Intent mainIntent = allIntents.get(allIntents.size() - 1);
        for (Intent intent : allIntents) {
            if (intent.getComponent().getClassName().equals("com.android.documentsui.DocumentsActivity")) {
                mainIntent = intent;
                break;
            }
        }
        allIntents.remove(mainIntent);

        // Create a chooser from the main intent
        Intent chooserIntent = Intent.createChooser(mainIntent, "Select source");

        // Add all other intents
        chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, allIntents.toArray(new Parcelable[allIntents.size()]));

        return chooserIntent;
    }


    /**
     * Get URI to image received from capture by camera.
     */
    private Uri getCaptureImageOutputUri() {
        Uri outputFileUri = null;
        File getImage = getExternalCacheDir();
        if (getImage != null) {
            outputFileUri = Uri.fromFile(new File(getImage.getPath(), "profile.png"));
        }
        return outputFileUri;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);
        Bitmap bitmap;
        if (resultCode == Activity.RESULT_OK) {

            ImageView imageView = (ImageView) findViewById(R.id.avatar);

            if (getPickImageResultUri(data) != null) {
                picUri = getPickImageResultUri(data);

                try {
                    myBitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), picUri);
                    myBitmap = rotateImageIfRequired(myBitmap, picUri);
                    myBitmap = getResizedBitmap(myBitmap, 500);

                    /*CircleImageView croppedImageView = (CircleImageView) findViewById(R.id.img_profile);
                    croppedImageView.setImageBitmap(myBitmap);*/
                    imageView.setImageBitmap(myBitmap);

                } catch (IOException e) {
                    e.printStackTrace();
                }


            } else {


                bitmap = (Bitmap) data.getExtras().get("data");

                myBitmap = bitmap;
                /*CircleImageView croppedImageView = (CircleImageView) findViewById(R.id.img_profile);
                if (croppedImageView != null) {
                    croppedImageView.setImageBitmap(myBitmap);
                }*/

                imageView.setImageBitmap(myBitmap);

            }

        }

    }

    private static Bitmap rotateImageIfRequired(Bitmap img, Uri selectedImage) throws IOException {

        ExifInterface ei = new ExifInterface(selectedImage.getPath());
        int orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);

        switch (orientation) {
            case ExifInterface.ORIENTATION_ROTATE_90:
                return rotateImage(img, 90);
            case ExifInterface.ORIENTATION_ROTATE_180:
                return rotateImage(img, 180);
            case ExifInterface.ORIENTATION_ROTATE_270:
                return rotateImage(img, 270);
            default:
                return img;
        }
    }

    private static Bitmap rotateImage(Bitmap img, int degree) {
        Matrix matrix = new Matrix();
        matrix.postRotate(degree);
        Bitmap rotatedImg = Bitmap.createBitmap(img, 0, 0, img.getWidth(), img.getHeight(), matrix, true);
        img.recycle();
        return rotatedImg;
    }

    public Bitmap getResizedBitmap(Bitmap image, int maxSize) {
        int width = image.getWidth();
        int height = image.getHeight();

        float bitmapRatio = (float) width / (float) height;
        if (bitmapRatio > 0) {
            width = maxSize;
            height = (int) (width / bitmapRatio);
        } else {
            height = maxSize;
            width = (int) (height * bitmapRatio);
        }
        return Bitmap.createScaledBitmap(image, width, height, true);
    }


    /**
     * Get the URI of the selected image from {@link #getPickImageChooserIntent()}.<br/>
     * Will return the correct URI for camera and gallery image.
     *
     * @param data the returned data of the activity result
     */
    public Uri getPickImageResultUri(Intent data) {
        boolean isCamera = true;
        if (data != null) {
            String action = data.getAction();
            isCamera = action != null && action.equals(MediaStore.ACTION_IMAGE_CAPTURE);
        }


        return isCamera ? getCaptureImageOutputUri() : data.getData();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        // save file url in bundle as it will be null on scren orientation
        // changes
        outState.putParcelable("pic_uri", picUri);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        // get the file url
        picUri = savedInstanceState.getParcelable("pic_uri");
    }

    private ArrayList<String> findUnAskedPermissions(ArrayList<String> wanted) {
        ArrayList<String> result = new ArrayList<String>();

        for (String perm : wanted) {
            if (!hasPermission(perm)) {
                result.add(perm);
            }
        }

        return result;
    }

    private boolean hasPermission(String permission) {
        if (canMakeSmores()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                return (checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED);
            }
        }
        return true;
    }

    private void showMessageOKCancel(String message, DialogInterface.OnClickListener okListener) {
        new AlertDialog.Builder(this)
                .setMessage(message)
                .setPositiveButton("OK", okListener)
                .setNegativeButton("Cancel", null)
                .create()
                .show();
    }

    private boolean canMakeSmores() {
        return (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1);
    }

    @TargetApi(Build.VERSION_CODES.M)
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {

            case ALL_PERMISSIONS_RESULT:
                for (String perms : permissionsToRequest) {
                    if (hasPermission(perms)) {

                    } else {

                        permissionsRejected.add(perms);
                    }
                }

                if (permissionsRejected.size() > 0) {


                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        if (shouldShowRequestPermissionRationale(permissionsRejected.get(0))) {
                            showMessageOKCancel("These permissions are mandatory for the application. Please allow access.",
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

                                                //Log.d("API123", "permisionrejected " + permissionsRejected.size());

                                                requestPermissions(permissionsRejected.toArray(new String[permissionsRejected.size()]), ALL_PERMISSIONS_RESULT);
                                            }
                                        }
                                    });
                            return;
                        }
                    }

                }

                break;
        }

    }



    /**
     * On load image button click, start pick  image chooser activity.
     */
    public void onLoadImageClick(View view) {
        startActivityForResult(getPickImageChooserIntent(), 200);
    }






}
