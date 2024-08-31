package com.sicmagroup.tondi;

import android.Manifest;
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
import android.graphics.BitmapFactory;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Message;
import android.os.Parcelable;
import android.provider.MediaStore;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.util.Base64;
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
import com.android.volley.toolbox.JsonObjectRequest;
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
import com.squareup.picasso.Transformation;
import com.tapadoo.alerter.Alerter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
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
import java.security.NoSuchAlgorithmException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import com.sicmagroup.formmaster.FormBuilder;
import com.sicmagroup.formmaster.model.BaseFormElement;

import static com.sicmagroup.tondi.Connexion.ID_UTILISATEUR_KEY;
import static com.sicmagroup.tondi.Connexion.NOM_KEY;
import static com.sicmagroup.tondi.Connexion.PASS_KEY;
import static com.sicmagroup.tondi.Connexion.PHOTO_CNI_KEY;
import static com.sicmagroup.tondi.Connexion.PHOTO_KEY;
import static com.sicmagroup.tondi.Connexion.PRENOMS_KEY;
import static com.sicmagroup.formmaster.model.BaseFormElement.TYPE_EDITTEXT_PASSWORD;
import static com.sicmagroup.tondi.Connexion.UUID_KEY;
import static com.sicmagroup.tondi.utils.Constantes.CODE_MARCHAND_KEY;
import static com.sicmagroup.tondi.utils.Constantes.REFRESH_TOKEN;
import static com.sicmagroup.tondi.utils.Constantes.SERVEUR;
import static com.sicmagroup.tondi.utils.Constantes.STATUT_UTILISATEUR;
import static com.sicmagroup.tondi.Connexion.TEL_KEY;
import static com.sicmagroup.tondi.utils.Constantes.TOKEN;
import static com.sicmagroup.tondi.utils.Constantes.accessToken;
import static com.sicmagroup.tondi.utils.Constantes.url_refresh_token;

import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.entity.StringEntity;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;


public class MonCompte extends AppCompatActivity {

    private static final int TAG_OLD_PASS = 41;

    private static final int TAG_PASS = 42;
    private static final int TAG_CF_PASS = 43;
    private static final int TAG_PIN = 44;
    private static final int TAG_TEL = 45;
    private static final int TAG_CODE_MERCHANT = 46;
    private static final int GALLERY_REQUEST_CODE = 200;
    private static final int CAMERA_REQUEST_CODE = 200;
    private RecyclerView form_pass;
    private RecyclerView form_sim;
    private FormBuilder frm_pass_builder;
    private FormBuilder frm_pin_builder;
    private FormBuilder frm_sim_builder;
    private FormBuilder frm_merchant_builder;
    ProgressDialog progressDialog;
    Utilitaire utilitaire1;




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

    private static final int ALL_PERMISSIONS_RESULT = 107;
    private ArrayList<String> permissionsToRequest;
    private ArrayList<String> permissionsRejected = new ArrayList<>();
    private ArrayList<String> permissions = new ArrayList<>();
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


    @SuppressLint("MissingInflatedId")
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

        permissions.add(Manifest.permission.CAMERA);
        permissions.add(Manifest.permission.READ_EXTERNAL_STORAGE);
        permissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);

        permissionsToRequest = findUnAskedPermissions(permissions);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (permissionsToRequest.size() > 0) {
                requestPermissions(permissionsToRequest.toArray(new String[permissionsToRequest.size()]), ALL_PERMISSIONS_RESULT);
            }
        }


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


        String nom = Prefs.getString(NOM_KEY, "");
        String prenoms = Prefs.getString(PRENOMS_KEY, "");
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
       ImageView user_avatar = (ImageView) findViewById(R.id.avatar);

       /* String medias_url =   SERVEUR + "/medias/";
        ImageView user_avatar = findViewById(R.id.avatar);
        Picasso.get().load(medias_url+ Prefs.getString(PHOTO_KEY,null)).transform(new Dashboard.CircleTransform()).into(user_avatar);
*/
        ContextWrapper cw = new ContextWrapper(this);
        File directory = cw.getDir("tontine_photos", Context.MODE_PRIVATE);
        String photo_identite = Prefs.getString(PHOTO_KEY, "");
        File mypath = new File(directory, "user_avatar" + ".png");
        Picasso.get().load(mypath).transform(new Dashboard.CircleTransform()).into(user_avatar);


        Log.e("Profil",mypath.toString());



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



        try {
           URL url_photo  = new URL(Constantes.URL_MEDIA_PP + photo_identite);
            new MonCompte.DownloadTask().execute(url_photo);

        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }

        String photo_cni = Prefs.getString(PHOTO_CNI_KEY, "");
        File mypath_cni = new File(directory, photo_cni + ".png");
        ImageView photo_cni_view = (ImageView) findViewById(R.id.photo_cni_cmpte);
        Picasso.get().load(mypath_cni).into(photo_cni_view);
        Log.e("photo cni", photo_cni);
       /* Picasso.get()
                .load(mypath_cni)
                .resize(90, 90)
                .into(photo_cni_view);*/

            user_avatar.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showPictureDialog();
                }
            });
       // loadImageFromStorage("user_avatar", user_avatar);


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

        if(Prefs.getString(CODE_MARCHAND_KEY, "").equals("null") || Prefs.getString(CODE_MARCHAND_KEY, "").equals("")){
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
//            Picasso.get().load(medias_url+Prefs.getString(PHOTO_KEY,null)).transform(new Dashboard.CircleTransform()).into(user_avatar);
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

    private ArrayList<String> findUnAskedPermissions(ArrayList<String> wanted) {
        ArrayList<String> result = new ArrayList<>();
        for (String perm : wanted) {
            if (!hasPermission(perm)) {
                result.add(perm);
            }
        }
        return result;
    }

    private boolean hasPermission(String permission) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED;
        }
        return true;
    }

    @TargetApi(Build.VERSION_CODES.M)
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case ALL_PERMISSIONS_RESULT:
                for (String perms : permissionsToRequest) {
                    if (!hasPermission(perms)) {
                        permissionsRejected.add(perms);
                    }
                }

                if (permissionsRejected.size() > 0) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        if (shouldShowRequestPermissionRationale(permissionsRejected.get(0))) {
                            showMessageOKCancel("Ces permissions sont nécessaires pour l'application. Veuillez les accorder.",
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                                requestPermissions(permissionsRejected.toArray(new String[permissionsRejected.size()]), ALL_PERMISSIONS_RESULT);
                                            }
                                        }
                                    });
                        }
                    }
                }
                break;
        }
    }

    private void showMessageOKCancel(String message, DialogInterface.OnClickListener okListener) {
        new AlertDialog.Builder(this)
                .setMessage(message)
                .setPositiveButton("OK", okListener)
                .setNegativeButton("Annuler", null)
                .create()
                .show();
    }


    private void showPictureDialog() {
        AlertDialog.Builder pictureDialog = new AlertDialog.Builder(this);
        pictureDialog.setTitle("Choisissez une action");
        String[] pictureDialogItems = {
                "Sélectionner depuis la galerie",
                "Prendre une photo"};
        pictureDialog.setItems(pictureDialogItems,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case 0:
                                choosePhotoFromGallery();
                                break;
                            case 1:
                                takePhotoFromCamera();
                                break;
                        }
                    }
                });
        pictureDialog.show();
    }

    public void choosePhotoFromGallery() {
        Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(galleryIntent, GALLERY_REQUEST_CODE);

    }


    private void checkCameraPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, CAMERA_REQUEST_CODE);
        } else {
            takePhotoFromCamera();
        }
    }



    private void takePhotoFromCamera() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(intent, CAMERA_REQUEST_CODE);
        }
    }



    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            if (requestCode == GALLERY_REQUEST_CODE) {
                if (data != null && data.getData() != null) {
                    Uri contentURI = data.getData();
                    try {
                        Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), contentURI);
                        String path = saveImage(bitmap);
                        Log.e("Image", bitmap.toString());
                        String imageName = "user_avatar";
                        Bitmap circularBitmap = getCircularBitmap(bitmap);
                        ImageView user_avatar = (ImageView) findViewById(R.id.avatar);

                        user_avatar.setImageBitmap(circularBitmap);
                        Log.e("Photo profil uploaded",bitmap.toString());                        Toast.makeText(MonCompte.this, "Image Saved!", Toast.LENGTH_SHORT).show();
                        uploadImageToServer(bitmap,path);
                        String imgname = String.valueOf(Calendar.getInstance().getTimeInMillis());
                    } catch (IOException e) {
                        e.printStackTrace();
                        Toast.makeText(MonCompte.this, "Failed to load image!", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(MonCompte.this, "Failed to get image from gallery!", Toast.LENGTH_SHORT).show();
                }
            }
            if (requestCode == CAMERA_REQUEST_CODE) {
                if (data != null && data.getExtras() != null) {
                    Bitmap thumbnail = (Bitmap) data.getExtras().get("data");
                    if (thumbnail != null) {
                        Log.d("Photo prise", thumbnail.toString());
                        Bitmap circularBitmap = getCircularBitmap(thumbnail);
                        ImageView user_avatar = (ImageView) findViewById(R.id.avatar);

                        user_avatar.setImageBitmap(circularBitmap);
                        Log.e("Photo profil uploaded",thumbnail.toString());
                        String path = saveImage(thumbnail);
                        Log.e("Photo profil uploaded",path);
                        Toast.makeText(MonCompte.this, "Image Saved!", Toast.LENGTH_SHORT).show();
                        uploadImageToServer(thumbnail, path);

                    } else {
                        Toast.makeText(MonCompte.this, "Failed to capture image!", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(MonCompte.this, "Failed to capture image!", Toast.LENGTH_SHORT).show();
                }
            }
        } else {
            Toast.makeText(MonCompte.this, "Action canceled or failed!", Toast.LENGTH_SHORT).show();
        }
    }

    public String encodeToBase64(Bitmap image) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        image.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
        byte[] byteArray = byteArrayOutputStream.toByteArray();
        return Base64.encodeToString(byteArray, Base64.DEFAULT);
    }


    public String saveImage(Bitmap myBitmap) {
        ContextWrapper cw = new ContextWrapper(getApplicationContext());
        File directory = cw.getDir("tontine_photos", Context.MODE_PRIVATE);
        File mypath = new File(directory, "user_avatar.png");

        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(mypath);
            myBitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                fos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return mypath.getAbsolutePath();
    }


    private Bitmap getCircularBitmap(Bitmap bitmap) {
        int width = Math.min(bitmap.getWidth(), bitmap.getHeight());

        Bitmap outputBitmap = Bitmap.createBitmap(width, width, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(outputBitmap);

        final Paint paint = new Paint();
        final Rect rect = new Rect(0, 0, width, width);
        final RectF rectF = new RectF(rect);

        float radius = width / 2.0f;

        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(Color.BLACK);
        canvas.drawCircle(radius, radius, radius, paint);

        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap, rect, rect, paint);

        return outputBitmap;
    }



    private Bitmap resizeImage(Bitmap originalImage, int width, int height) {
        return Bitmap.createScaledBitmap(originalImage, width, height, false);
    }

    private String convertImageToBase64(String imagePath) {
        Bitmap bitmap = BitmapFactory.decodeFile(imagePath);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
        byte[] byteArray = byteArrayOutputStream.toByteArray();
        return Base64.encodeToString(byteArray, Base64.DEFAULT);
    }

   /* private void uploadImageToServer(String imagePath) {
        // Convertir l'image en base64
        String base64Image = convertImageToBase64(imagePath);

        // Créer le JSON pour le corps de la requête
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("pp_file", base64Image);
        } catch (JSONException e) {
            e.printStackTrace();
            Toast.makeText(MonCompte.this, "Failed to create JSON body", Toast.LENGTH_SHORT).show();
            return;
        }

        // Créer un client OkHttp
        OkHttpClient client = new OkHttpClient();

        // Créer le corps de la requête
        RequestBody body = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), jsonObject.toString());

        // Remplacer cette variable par le token d'autorisation réel
        String authToken = accessToken;

        // Construire la requête POST avec le header d'autorisation
        okhttp3.Request request = new okhttp3.Request.Builder()
                .url(Constantes.UPDATE_PROFIL_IMAGE +"/"+ Prefs.getString(UUID_KEY, ""))
                .addHeader("Authorization", "Bearer " + authToken)
                .post(body)
                .build();

        // Envoyer la requête de manière asynchrone
        client.newCall(request).enqueue(new Callback() {
            @SuppressLint("LongLogTag")
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e("Erreur lors de l'envoie de l'image", e.getMessage());
                // Gérer les erreurs
                runOnUiThread(() -> Toast.makeText(MonCompte.this, "Failed to upload image", Toast.LENGTH_SHORT).show());
            }


            @Override
            public void onResponse(Call call, okhttp3.Response response) throws IOException {
                Log.e("La reponse du seveur", response.toString());
                // Gérer la réponse du serveur
                if (response.isSuccessful()) {
                    runOnUiThread(() -> Toast.makeText(MonCompte.this, "Image uploaded successfully", Toast.LENGTH_SHORT).show());
                    Log.e("image path", imagePath);
                    Prefs.putString(PHOTO_KEY, imagePath);
                } else {
                    runOnUiThread(() -> Toast.makeText(MonCompte.this, "Failed to upload image, server error", Toast.LENGTH_SHORT).show());
                }
            }
        });
    }*/


//    private void uploadImageToServer(String imagePath) {
//        File file = new File(imagePath);
//
//        // Créer le RequestBody pour le fichier image
//        RequestBody fileBody = RequestBody.create(MediaType.parse("image/jpeg"), file);
//
//        // Créer le MultipartBody pour la requête
//        MultipartBody requestBody = new MultipartBody.Builder()
//                .setType(MultipartBody.FORM)
//                .addFormDataPart("pp_file", file.getName(), fileBody)
//                .build();
//
//        // Remplacer cette variable par le token d'autorisation réel
//        String authToken = accessToken;
//
//        // Construire la requête POST avec le header d'autorisation
//        okhttp3.Request request = new okhttp3.Request.Builder()
//                .url(Constantes.UPDATE_PROFIL_IMAGE + "/" + Prefs.getString(UUID_KEY, ""))
//                .addHeader("Authorization", "Bearer " + authToken)
//                .post(requestBody)
//                .build();
//
//        // Configurer le client OkHttp avec des timeouts et la reconnexion automatique
//        OkHttpClient client = new OkHttpClient.Builder()
//                .connectTimeout(30, TimeUnit.SECONDS)
//                .writeTimeout(30, TimeUnit.SECONDS)
//                .readTimeout(30, TimeUnit.SECONDS)
//                .retryOnConnectionFailure(true)
//                .build();
//
//        // Envoyer la requête de manière asynchrone
//        client.newCall(request).enqueue(new Callback() {
//            @Override
//            public void onFailure(Call call, IOException e) {
//                Log.e("Erreur lors de l'envoie de l'image", "Message: " + e.getMessage(), e);
//                runOnUiThread(() -> Toast.makeText(MonCompte.this, "Failed to upload image: " + e.getMessage(), Toast.LENGTH_SHORT).show());
//            }
//
//            @Override
//            public void onResponse(Call call, okhttp3.Response response) throws IOException {
//                Log.e("La réponse du serveur", response.toString());
//                if (response.isSuccessful()) {
//                    runOnUiThread(() -> Toast.makeText(MonCompte.this, "Image uploaded successfully", Toast.LENGTH_SHORT).show());
//                    Log.e("image path", imagePath);
//                    Prefs.putString(PHOTO_KEY, imagePath);
//                } else {
//                    runOnUiThread(() -> Toast.makeText(MonCompte.this, "Failed to upload image, server error", Toast.LENGTH_SHORT).show());
//                }
//            }
//        });
//    }



    private void uploadImageToServer(final Bitmap bitmap, String imagePath) {
        File file = new File(imagePath);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
        String imgname = "user_avatar";
        utilitaire.saveToInternalStorage(bitmap, imgname);

        // Créer le RequestBody pour le fichier image
        RequestBody fileBody = RequestBody.create(MediaType.parse("image/jpeg"), file);

        // Créer le MultipartBody pour la requête
        MultipartBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("pp_file", file.getName(), fileBody)
                .build();

        // Remplacer cette variable par le token d'autorisation réel
        String authToken = accessToken;

        // Construire la requête POST avec le header d'autorisation
        okhttp3.Request request = new okhttp3.Request.Builder()
                .url(Constantes.UPDATE_PROFIL_IMAGE + "/" + Prefs.getString(UUID_KEY, ""))
                .addHeader("Authorization", "Bearer " + authToken)
                .post(requestBody)
                .build();

        // Configurer le client OkHttp avec des timeouts et la reconnexion automatique
        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .retryOnConnectionFailure(true)
                .build();

        // Envoyer la requête de manière asynchrone
        client.newCall(request).enqueue(new Callback() {
            @SuppressLint("LongLogTag")
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e("Erreur lors de l'envoie de l'image", "Message: " + e.getMessage(), e);
                runOnUiThread(() -> Toast.makeText(MonCompte.this, "Failed to upload image: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(Call call, okhttp3.Response response) throws IOException {
                Log.e("La réponse du serveur", response.toString());
                if (response.isSuccessful()) {
                    // Récupérer le corps de la réponse sous forme de chaîne
                    String responseString = response.body().string();
                    Log.e("Analyse de l'image", responseString);

                    try {
                        // Créer un JSONObject à partir du corps de la réponse
                        JSONObject resp = new JSONObject(responseString);
                        if (resp.has("responseCode")) {
                            int responseCode = resp.getInt("responseCode");
                            Log.e("La réponse ", resp.toString());
                            if (responseCode == 0) {
                                String img = resp.getString("body");
                                final Utilisateur utilisateur = Utilisateur.find(Utilisateur.class, "id_utilisateur = ?", Prefs.getString(ID_UTILISATEUR_KEY, null)).get(0);

                                utilisateur.setPhoto_identite(img);
                                utilisateur.save();
                                Log.e("La valeur du body", img);
                                runOnUiThread(() -> Toast.makeText(MonCompte.this, "Image envoyée au serveur ", Toast.LENGTH_SHORT).show());
                                Prefs.putString(PHOTO_KEY, img);
                            }
                        }
                    } catch (JSONException e) {
                        throw new RuntimeException(e);
                    }
                   String msg = "Votre photo a été correctement modifiée";
                    Intent i = new Intent(MonCompte.this, Message_ok.class);
                    i.putExtra("msg_desc", msg);
                    i.putExtra("class", "com.sicmagroup.tondi.MonCompte");
                    startActivity(i);
                } else if (response.code() == 401) {
                    refreshAccessToken(MonCompte.this, success -> {
                        if (success) {
                            uploadImageToServer(bitmap,imagePath);
                        } else {
                            runOnUiThread(() -> Toast.makeText(MonCompte.this, "Failed to refresh token, please log in again.", Toast.LENGTH_SHORT).show());
                        }
                    });
                } else {
                    String msg = "Echec lors de la mise à jour de la photo";
                    Intent i = new Intent(MonCompte.this, Message_non.class);
                    i.putExtra("msg_desc", msg);
                    i.putExtra("class", "com.sicmagroup.tondi.MonCompte");
                    startActivity(i);
                }
            }

        });
    }










    //show merchant_details
//    public void showMerchantDetails() {
//        RequestParams params = new RequestParams();
//        params.put("codeReferent", Prefs.getString(CODE_MARCHAND_KEY, ""));
//
//        AsyncHttpClient client = new AsyncHttpClient();
//        // String authorizationToken = Prefs.getString(AUTHORIZATION_KEY, "");
//
//        // Ajouter l'autorisation dans le header
//        client.addHeader("Authorization", "Bearer " + accessToken);
//
//        client.post(Constantes.URL_GET_MERCHANT_DETAILS, params, new JsonHttpResponseHandler() {
//            @Override
//            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
//                super.onSuccess(statusCode, headers, response);
//                try {
//                    if (response.getInt("responseCode") == 0) {
//                        JSONObject body = response.getJSONObject("body");
//                        name_merchant.setText(body.getString("nom") + " " + body.getString("prenoms"));
//                        code_merchant.setText(body.getString("code_marchand"));
//                        tel_merchant.setText(body.getString("numero"));
//                    }
//                } catch (JSONException e) {
//                    e.printStackTrace();
//                }
//                Log.i("ErreurAsync", "We got a jsonObject");
//            }
//
//            @Override
//            public void onSuccess(int statusCode, Header[] headers, String responseString) {
//                super.onSuccess(statusCode, headers, responseString);
//                Log.i("ErreurAsync", "We got a responseString");
//            }
//
//            @Override
//            public void onSuccess(int statusCode, Header[] headers, JSONArray response) {
//                super.onSuccess(statusCode, headers, response);
//                Log.i("ErreurAsync", "We got an array");
//            }
//
//            @Override
//            public void onStart() {
//                super.onStart();
//                progressBar.setVisibility(View.VISIBLE);
//                linearLayoutDetails.setVisibility(View.GONE);
//            }
//
//            @Override
//            public void onFinish() {
//                super.onFinish();
//                progressBar.setVisibility(View.GONE);
//                linearLayoutDetails.setVisibility(View.VISIBLE);
//            }
//
//            @Override
//            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
//                super.onFailure(statusCode, headers, throwable, errorResponse);
//                Log.e("ErreurAsync", errorResponse != null ? errorResponse.toString() : "Error response is null");
//            }
//
////            @Override
////            public void onFailure(int statusCode, Header[] headers, Throwable throwable, String errorResponse) {
////                super.onFailure(statusCode, headers, errorResponse, throwable);
////                Log.e("ErreurAsync", errorResponse != null ? errorResponse : "Error response is null");
////            }
//
//            @Override
//            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONArray errorResponse) {
//                super.onFailure(statusCode, headers, throwable, errorResponse);
//                Log.e("ErreurAsync", errorResponse != null ? errorResponse.toString() : "Error response is null");
//            }
//        });
//    }

    public void showMerchantDetails() {
        JSONObject jsonParams = new JSONObject();
        try {
            // Ajouter les paramètres dans l'objet JSON
            jsonParams.put("codeReferent", Prefs.getString(CODE_MARCHAND_KEY, ""));
            Log.e("Le code Référant:", Prefs.getString(CODE_MARCHAND_KEY,"").toString());
            Log.e("Le body marchand:", jsonParams.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        // Convertir JSONObject en StringEntity
        StringEntity entity = new StringEntity(jsonParams.toString(), "UTF-8");

        AsyncHttpClient client = new AsyncHttpClient();

        // Ajouter l'autorisation dans le header
        client.addHeader("Authorization", "Bearer " + accessToken);

        // Ajouter le Content-Type pour JSON
        client.addHeader("Content-Type", "application/json");

        client.post(null, Constantes.URL_GET_MERCHANT_DETAILS, entity, "application/json", new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                super.onSuccess(statusCode, headers, response);
                try {
                    Log.e("La reponse marchande:",response.toString());
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
//            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONArray errorResponse) {
//                super.onFailure(statusCode, headers, throwable, errorResponse);
//                Log.e("ErreurAsync", errorResponse != null ? errorResponse.toString() : "Error response is null");
//            }
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
            @SuppressLint("LongLogTag")
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
                    Log.e("L'ancien mot de passe est:",old_pass);
                    Connexion.AeSimpleSHA1 AeSimpleSHA1 = new Connexion.AeSimpleSHA1();
                    String ins_old_pass = element1.getValue();

                    try {
                        Log.e("L'Ancien pass avant chifrement:", ins_old_pass);
                        ins_old_pass = AeSimpleSHA1.md5(ins_old_pass);
                        ins_old_pass = AeSimpleSHA1.SHA1(ins_old_pass);
                        Log.d("ins_old_pass",ins_old_pass);
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

    private void linkMerchant(String codeMarchand) {
        RequestQueue queue = Volley.newRequestQueue(MonCompte.this);

        // Créez un objet JSON pour les paramètres
        JSONObject jsonParams = new JSONObject();
        try {
            jsonParams.put("codeReferent", codeMarchand);
            jsonParams.put("customerNumber", Prefs.getString(TEL_KEY, ""));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        JsonObjectRequest postRequest = new JsonObjectRequest(Request.Method.POST, Constantes.URL_LINK_MERCHANT_TO_USER, jsonParams,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d("onResponse: ", response.toString());
                        try {
                            if (response.getInt("responseCode") == 0) {
                                Prefs.putString(CODE_MARCHAND_KEY, codeMarchand);
                                Intent i = new Intent(MonCompte.this, Message_ok.class);
                                i.putExtra("class", "com.sicmagroup.tondi.Home");
                                i.putExtra("msg_desc", "Affectation du marchand à votre compte réussie");
                                startActivity(i);
                            } else {
                                CoordinatorLayout mainLayout = findViewById(R.id.mon_compte_layout);
                                Snackbar.make(mainLayout, "Code de marchand invalide!", Snackbar.LENGTH_SHORT).show();
                            }
                        } catch (Throwable t) {
                            Log.d("link_merchand", String.valueOf(t.getCause()));
                            CoordinatorLayout mainLayout = findViewById(R.id.mon_compte_layout);
                            Snackbar.make(mainLayout, "Une erreur est survenue. Réessayer plus tard, merci.", Snackbar.LENGTH_SHORT).show();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        CoordinatorLayout mainLayout = findViewById(R.id.mon_compte_layout);
                        Snackbar.make(mainLayout, "Une erreur est survenue. Réessayez svp!", Snackbar.LENGTH_SHORT).show();
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

    /*@SuppressLint("ResourceAsColor")
    private void savePass() {
        BaseFormElement pass = frm_pass_builder.getFormElement(TAG_PASS);
        String pass_value = pass.getValue();
        Connexion.AeSimpleSHA1 AeSimpleSHA1 = new Connexion.AeSimpleSHA1();

        try {
            pass_value =  AeSimpleSHA1.md5(pass_value);
            pass_value = AeSimpleSHA1.SHA1(pass_value);
        } catch (NoSuchAlgorithmException | UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        Utilisateur utilisateur_modifie = Utilisateur.find(Utilisateur.class, "id_utilisateur = ?", Prefs.getString(ID_UTILISATEUR_KEY, null)).get(0);
        utilisateur_modifie.setMdp(pass_value);
        utilisateur_modifie.save();

        Long id_user = utilisateur_modifie.getId();
        if (id_user != null) {
            Prefs.putString(PASS_KEY, pass_value);
            RequestQueue queue = Volley.newRequestQueue(this);

            // Créer le corps JSON de la requête
            JSONObject jsonBody = new JSONObject();
            try {
                BaseFormElement oldpass = frm_pass_builder.getFormElement(TAG_OLD_PASS);
                jsonBody.put("numero", Prefs.getString(TEL_KEY, null));
                jsonBody.put("holdPassword", oldpass.getValue());
                jsonBody.put("newPassword", pass.getValue());
                Log.e("Le changement de password", jsonBody.toString());
            } catch (JSONException e) {
                e.printStackTrace();
            }

            // Utiliser JsonObjectRequest pour envoyer les données JSON
            JsonObjectRequest jsonRequest = new JsonObjectRequest(Request.Method.POST, Constantes.URL_CHANGE_PWD, jsonBody,
                    new Response.Listener<JSONObject>() {
                        @SuppressLint("ResourceAsColor")
                        @Override
                        public void onResponse(JSONObject response) {
                            Log.e("onResponse: ", response.toString());
                            try {
                                if (response.getInt("responseCode") == 0) {
                                    String msg = "Votre Mot de passe a été correctement modifié";
                                    Intent i = new Intent(MonCompte.this, Message_ok.class);
                                    i.putExtra("msg_desc", msg);
                                    i.putExtra("class", "com.sicmagroup.tondi.MonCompte");
                                    progressDialog.dismiss();
                                    startActivity(i);
                                } else {
                                    String msg = response.getString("body");
                                    Intent i = new Intent(MonCompte.this, Message_non.class);
                                    i.putExtra("msg_desc", msg);
                                    i.putExtra("class", "com.sicmagroup.tondi.MonCompte");
                                    progressDialog.dismiss();
                                    startActivity(i);
                                }
                            } catch (JSONException t) {
                                Toast.makeText(MonCompte.this, "Une erreur est survenue", Toast.LENGTH_SHORT).show();
                                progressDialog.dismiss();
                                Log.d("errornscription", t.getMessage());
                                Log.e("Update Data", t.getMessage());
                            }
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            progressDialog.dismiss();
                            Toast.makeText(MonCompte.this, "Une erreur est survenue", Toast.LENGTH_SHORT).show();
                            Log.e("Error.Synchronisation", String.valueOf(error.getMessage()));
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
                    35000,
                    -1,
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
            queue.add(jsonRequest);

            progressDialog = new ProgressDialog(this);
            progressDialog.setCancelable(false);
            progressDialog.setMessage("Mise à jour de votre mot de passe");
            progressDialog.show();

        } else {
            String msg = "Une erreur est survenue.";
            Intent i = new Intent(MonCompte.this, Message_non.class);
            i.putExtra("msg_desc", msg);
            i.putExtra("class", "com.sicmagroup.tondi.MonCompte");
            startActivity(i);
        }
    }*/

    @SuppressLint("LongLogTag")
    private void savePass() {
        BaseFormElement pass = frm_pass_builder.getFormElement(TAG_PASS);
        String pass_value = pass.getValue();
        Connexion.AeSimpleSHA1 AeSimpleSHA1 = new Connexion.AeSimpleSHA1();

        try {
            pass_value =  AeSimpleSHA1.md5(pass_value);
            pass_value = AeSimpleSHA1.SHA1(pass_value);
        } catch (NoSuchAlgorithmException | UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        Utilisateur utilisateur_modifie = Utilisateur.find(Utilisateur.class, "id_utilisateur = ?", Prefs.getString(ID_UTILISATEUR_KEY, null)).get(0);
        utilisateur_modifie.setMdp(pass_value);
        utilisateur_modifie.save();

        Long id_user = utilisateur_modifie.getId();
        if (id_user != null) {
            Prefs.putString(PASS_KEY, pass_value);
            RequestQueue queue = Volley.newRequestQueue(this);

            JSONObject jsonBody = new JSONObject();
            try {
                BaseFormElement oldpass = frm_pass_builder.getFormElement(TAG_OLD_PASS);
                jsonBody.put("numero", Prefs.getString(TEL_KEY, null));
                jsonBody.put("holdPassword", oldpass.getValue());
                jsonBody.put("newPassword", pass.getValue());
                Log.e("Le changement de password", jsonBody.toString());
            } catch (JSONException e) {
                e.printStackTrace();
            }

            JsonObjectRequest jsonRequest = new JsonObjectRequest(Request.Method.POST, Constantes.URL_CHANGE_PWD, jsonBody,
                    new Response.Listener<JSONObject>() {
                        @SuppressLint("ResourceAsColor")
                        @Override
                        public void onResponse(JSONObject response) {
                            Log.e("onResponse: ", response.toString());
                            try {
                                if (response.getInt("responseCode") == 0) {
                                    String msg = "Votre Mot de passe a été correctement modifié";
                                    Intent i = new Intent(MonCompte.this, Message_ok.class);
                                    i.putExtra("msg_desc", msg);
                                    i.putExtra("class", "com.sicmagroup.tondi.MonCompte");
                                    progressDialog.dismiss();
                                    startActivity(i);
                                } else {
                                    String msg = response.getString("body");
                                    Intent i = new Intent(MonCompte.this, Message_non.class);
                                    i.putExtra("msg_desc", msg);
                                    i.putExtra("class", "com.sicmagroup.tondi.MonCompte");
                                    progressDialog.dismiss();
                                    startActivity(i);
                                }
                            } catch (JSONException t) {
                                Toast.makeText(MonCompte.this, "Une erreur est survenue", Toast.LENGTH_SHORT).show();
                                progressDialog.dismiss();
                                Log.d("errornscription", t.getMessage());
                                Log.e("Update Data", t.getMessage());
                            }
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            progressDialog.dismiss();
                            if (error.networkResponse != null && error.networkResponse.statusCode == 401) {
                                // Le token a expiré, donc on le rafraîchit et on réessaie la requête
                                refreshAccessToken(MonCompte.this, new TokenRefreshListener() {
                                    @Override
                                    public void onTokenRefreshed(boolean success) {
                                        if (success) {
                                            // Réessayer la requête avec le nouveau token
                                            savePass();
                                        } else {
                                            Toast.makeText(MonCompte.this, "Impossible de rafraîchir le token", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });
                            } else {
                                Toast.makeText(MonCompte.this, "Une erreur est survenue", Toast.LENGTH_SHORT).show();
                                Log.e("Error.Synchronisation", String.valueOf(error.getMessage()));
                            }
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
                    35000,
                    -1,
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
            queue.add(jsonRequest);

            progressDialog = new ProgressDialog(this);
            progressDialog.setCancelable(false);
            progressDialog.setMessage("Mise à jour de votre mot de passe");
            progressDialog.show();

        } else {
            String msg = "Une erreur est survenue.";
            Intent i = new Intent(MonCompte.this, Message_non.class);
            i.putExtra("msg_desc", msg);
            i.putExtra("class", "com.sicmagroup.tondi.MonCompte");
            startActivity(i);
        }
    }


    private void refreshAccessToken(Context context, MonCompte.TokenRefreshListener listener) {
        RequestQueue queue = Volley.newRequestQueue(context);
        JSONObject params = new JSONObject();
        try {
            String refreshToken = Prefs.getString(REFRESH_TOKEN, "");
            Log.e("Mon refresh token", refreshToken);
            params.put("refreshToken", refreshToken);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        JsonObjectRequest refreshRequest = new JsonObjectRequest(
                Request.Method.POST,
                url_refresh_token,
                params,
                new Response.Listener<JSONObject>() {
                    @SuppressLint("LongLogTag")
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            Log.e("La réponse du refresh token", response.toString());
                            String newAccessToken = response.getString("token");
                            String newRefreshToken = response.getString("refreshToken");
                            Prefs.putString(TOKEN, newAccessToken);
                            Prefs.putString(REFRESH_TOKEN, newRefreshToken);
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
                }
        );

        queue.add(refreshRequest);
    }

    private interface TokenRefreshListener {
        void onTokenRefreshed(boolean success);
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

//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//
//        super.onActivityResult(requestCode, resultCode, data);
//        Bitmap bitmap;
//        if (resultCode == Activity.RESULT_OK) {
//
//            ImageView imageView = (ImageView) findViewById(R.id.avatar);
//
//            if (getPickImageResultUri(data) != null) {
//                picUri = getPickImageResultUri(data);
//
//                try {
//                    myBitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), picUri);
//                    myBitmap = rotateImageIfRequired(myBitmap, picUri);
//                    myBitmap = getResizedBitmap(myBitmap, 500);
//
//                    /*CircleImageView croppedImageView = (CircleImageView) findViewById(R.id.img_profile);
//                    croppedImageView.setImageBitmap(myBitmap);*/
//                    imageView.setImageBitmap(myBitmap);
//
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//
//
//            } else {
//
//
//                bitmap = (Bitmap) data.getExtras().get("data");
//
//                myBitmap = bitmap;
//                /*CircleImageView croppedImageView = (CircleImageView) findViewById(R.id.img_profile);
//                if (croppedImageView != null) {
//                    croppedImageView.setImageBitmap(myBitmap);
//                }*/
//
//                imageView.setImageBitmap(myBitmap);
//
//            }
//
//        }
//
//    }

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

    /*private ArrayList<String> findUnAskedPermissions(ArrayList<String> wanted) {
        ArrayList<String> result = new ArrayList<String>();

        for (String perm : wanted) {
            if (!hasPermission(perm)) {
                result.add(perm);
            }
        }

        return result;
    }*/

    /*private boolean hasPermission(String permission) {
        if (canMakeSmores()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                return (checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED);
            }
        }
        return true;
    }*/

    /*private void showMessageOKCancel(String message, DialogInterface.OnClickListener okListener) {
        new AlertDialog.Builder(this)
                .setMessage(message)
                .setPositiveButton("OK", okListener)
                .setNegativeButton("Cancel", null)
                .create()
                .show();
    }*/

    private boolean canMakeSmores() {
        return (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1);
    }

    /*@TargetApi(Build.VERSION_CODES.M)
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

    }*/

    /**
     * On load image button click, start pick  image chooser activity.
     */
    public void onLoadImageClick(View view) {
        startActivityForResult(getPickImageChooserIntent(), 200);
    }

}
