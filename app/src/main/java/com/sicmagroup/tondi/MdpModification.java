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
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Message;
import android.os.Parcelable;
import android.provider.MediaStore;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.content.ContextCompat;
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
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
import com.google.gson.Gson;
import com.orm.SugarRecord;
import com.orm.query.Condition;
import com.orm.query.Select;
import com.pixplicity.easyprefs.library.Prefs;
import com.sicmagroup.formmaster.model.FormElementTextPassword;
import com.sicmagroup.formmaster.model.FormElementTextPhone;
import com.sicmagroup.formmaster.model.FormElementTextSingleLine;
import com.squareup.picasso.Picasso;
import com.tapadoo.alerter.Alerter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
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
import static com.sicmagroup.tondi.Connexion.PHOTO_KEY;
import static com.sicmagroup.tondi.Connexion.PRENOMS_KEY;
import static com.sicmagroup.formmaster.model.BaseFormElement.TYPE_EDITTEXT_PASSWORD;
import static com.sicmagroup.tondi.Connexion.url_generate_otp_insc;
import static com.sicmagroup.tondi.utils.Constantes.CODE_MARCHAND_KEY;
import static com.sicmagroup.tondi.utils.Constantes.SERVEUR;
import static com.sicmagroup.tondi.utils.Constantes.STATUT_UTILISATEUR;
import static com.sicmagroup.tondi.Connexion.TEL_KEY;
import static com.sicmagroup.tondi.utils.Constantes.accessToken;

public class MdpModification extends AppCompatActivity {
    public static ProgressDialog progressDialog;
    LinearLayout mainLayout;

    private static final int TAG_PASS = 42;
    private static final int TAG_CF_PASS = 43;
    private static final int TAG_PIN = 44;
    private static final int TAG_TEL = 45;

    private FormBuilder frm_sim_builder;
    private FormBuilder frm_pin_builder;

    private RecyclerView form_pass;
    private FormBuilder frm_pass_builder;

    String url_modifier_pass = SERVEUR + "/api/v1/marchands/update_mdp";

    Bitmap myBitmap;
    Uri picUri;
    Utilitaire utilitaire;

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mdp_modification);
        new Prefs.Builder()
                .setContext(this)
                .setMode(ContextWrapper.MODE_PRIVATE)
                .setPrefsName(getPackageName())
                .setUseDefaultSharedPreference(true)
                .build();
        utilitaire = new Utilitaire(this);
        setupForm();
    }

    @Override
    public void onBackPressed() {
        // your code.
        Prefs.putString(ID_UTILISATEUR_KEY,null);
        MdpModification.this.finish();
        Intent i = new Intent(MdpModification.this, Connexion.class);
        startActivity(i);
    }

    private void setupForm() {

        form_pass = (RecyclerView) findViewById(R.id.form_pass);
        frm_pass_builder = new FormBuilder(this, form_pass);

        final FormElementTextPassword element2 = FormElementTextPassword.createInstance().setTag(TAG_PASS).setTitle("Nouveau").setHint("Nouveau mot de passe").setRequired(true);
        final FormElementTextPassword element3 = FormElementTextPassword.createInstance().setTag(TAG_CF_PASS).setTitle("Confirmer").setHint("Confimer nouveau mot de passe").setRequired(true).setType(TYPE_EDITTEXT_PASSWORD);
        /*password.setInputType(InputType.TYPE_CLASS_TEXT |
                InputType.TYPE_TEXT_VARIATION_PASSWORD);*/

        List<BaseFormElement> formItems = new ArrayList<>();
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

                    if (element2.getValue().length() < 6 || element3.getValue().length() < 6) {
                        msg = msg + "> Le Nouveau mot de passe doit être composé au minimum de 6 caractères. \n";
                        flag = true;
                    }

                    if (!element2.getValue().equals(element3.getValue())) {
                        msg = msg + "> Le Nouveau mot de passe et Confirmer ne correspondent pas. \n";
                        flag = true;
                    }
                    if (element2.getValue().equals("000000")) {
                        msg = msg + "> Le Nouveau mot de passe doit être différent de 000000. \n";
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
                            MdpModification.this.finish();
                            startActivity(new Intent(MdpModification.this,Connexion.class));
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

        Utilisateur utilisateur_modifie = Utilisateur.find(Utilisateur.class, "id_utilisateur = ?", Prefs.getString(ID_UTILISATEUR_KEY,null)).get(0);
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
            Utilitaire utilitaire = new Utilitaire(MdpModification.this);
            if (utilitaire.isConnected()){
                utilitaire.synchroniser_en_ligne();
            }
            String msg="Votre PIN d'Accès a été correctement modifié ";
            Intent i = new Intent(MdpModification.this, Message_ok.class);
            i.putExtra("msg_desc",msg);
            i.putExtra("class","com.sicmagroup.tondi.Home");
            startActivity(i);
        }else{
            String msg="Un problème est survenu lors de la modification. Veuillez réessayer SVP! ";
            Intent i = new Intent(MdpModification.this, Message_non.class);
            i.putExtra("msg_desc",msg);
            i.putExtra("class","com.sicmagroup.tondi.Home");
            startActivity(i);
        }

    }

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
        //enregistrer dans la BD via l'API
        modif_mdp(pass_value);
        //end enregistrement
        Utilisateur utilisateur_modifie = Utilisateur.find(Utilisateur.class, "id_utilisateur = ?", Prefs.getString(ID_UTILISATEUR_KEY,null)).get(0);
        utilisateur_modifie.setMdp(pass_value);
        utilisateur_modifie.save();
        Long id_user = utilisateur_modifie.getId();
        if (id_user!=null){
            Prefs.putString(PASS_KEY, pass_value);
            // enregistrer dans synchroniser
            Date currentTime = Calendar.getInstance().getTime();
            long output_maj=currentTime.getTime()/1000L;
            String str_maj=Long.toString(output_maj);
            long timestamp_maj = Long.parseLong(str_maj) * 1000;
            try {
                Synchronisation new_sync = new Synchronisation();
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("a","pass#compte");
                jsonObject.put("n",utilisateur_modifie.getNumero());
                Gson gson = new Gson();
                String user_json = gson.toJson(utilisateur_modifie);
                jsonObject.put("d",user_json);
                new_sync.setMaj(timestamp_maj);
                new_sync.setStatut(0);
                new_sync.setDonnees(jsonObject.toString());
                new_sync.save();


            } catch (JSONException e) {
                e.printStackTrace();
            }
            Utilitaire utilitaire = new Utilitaire(MdpModification.this);
            if (utilitaire.isConnected()){
                utilitaire.synchroniser_en_ligne();
            }

        }else{
            MdpModification.this.finish();
            String msg="Votre Mot de passe a été correctement modifié ";
            Intent i = new Intent(MdpModification.this, Message_ok.class);
            i.putExtra("msg_desc",msg);
            i.putExtra("class","com.sicmagroup.tondi.Home");
            startActivity(i);

        }


    }


    private void modif_mdp(String mdp) {
        RequestQueue queue = Volley.newRequestQueue(MdpModification.this);
        StringRequest postRequest = new StringRequest(Request.Method.POST, url_modifier_pass,
                new Response.Listener<String>() {
                    @SuppressLint("ResourceAsColor")
                    @Override
                    public void onResponse(String response) {
                        Log.e("InscResonse", response.toString());
                        // if (!response.equals("Erreur")) {
                        try {

                            JSONObject result = new JSONObject(response);
                            if (result.getBoolean("success")) {

                                progressDialog.dismiss();
                                MdpModification.this.finish();
                                String msg="Votre Mot de passe a été correctement modifié ";
                                Intent i = new Intent(MdpModification.this, Message_ok.class);
                                i.putExtra("msg_desc",msg);
                                i.putExtra("class","com.sicmagroup.tondi.Home");
                                startActivity(i);

                            } else {
                                progressDialog.dismiss();
                                Toast.makeText(MdpModification.this, "Echec de la modification", Toast.LENGTH_SHORT).show();
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
                        Log.e("Error.modification", String.valueOf(error.getMessage()));
                        String message = "Une erreur est survenue! Veuillez réessayer svp.";
                        Toast.makeText(MdpModification.this, message, Toast.LENGTH_SHORT).show();
                    }
                }
        ) {
            @Override
            protected Map<String, String> getParams() {
                //Date currentDate = new Date();
                Map<String, String> params = new HashMap<String, String>();
                params.put("numero", Prefs.getString(TEL_KEY,""));
                params.put("holdPassword", Prefs.getString(PASS_KEY,""));
                params.put("newPassword",mdp );
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
                15000,
                -1,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        queue.add(postRequest);

        progressDialog = new ProgressDialog(MdpModification.this);
        progressDialog.setCancelable(false);
        progressDialog.setMessage("Veuillez patienter SVP! \n Modification de votre mot de passe en cours.");
        progressDialog.show();
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
            Utilitaire utilitaire = new Utilitaire(MdpModification.this);
            if (utilitaire.isConnected()){
                utilitaire.synchroniser_en_ligne();
            }

            String msg="Votre SIM a été ajouté avec succès.";
            Intent i = new Intent(MdpModification.this, Message_ok.class);
            i.putExtra("msg_desc",msg);
            i.putExtra("class","com.sicmagroup.tondi.Home");
            startActivity(i);

        }else{
            Alerter.create(MdpModification.this)
                    .setTitle("Une erreur est survenue lors de l'ajout. Veuillez réessayer SVP!")
                    .setIcon(R.drawable.ic_warning)
                    .setTitleAppearance(R.style.TextAppearance_AppCompat_Large)
                    .setIconColorFilter(R.color.colorPrimaryDark)
                    //.setText("Vous pouvez maintenant vous connecter.")
                    .setBackgroundColorRes(R.color.colorWhite) // or setBackgroundColorInt(Color.CYAN)
                    .show();
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


    /**
     * On load image button click, start pick  image chooser activity.
     */
    public void onLoadImageClick(View view) {
        startActivityForResult(getPickImageChooserIntent(), 200);
    }






}
