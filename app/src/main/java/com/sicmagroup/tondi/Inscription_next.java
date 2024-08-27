package com.sicmagroup.tondi;

import android.Manifest;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.provider.MediaStore;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import android.system.ErrnoException;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkError;
import com.android.volley.ParseError;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.ServerError;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.firebase.messaging.FirebaseMessaging;
import com.pixplicity.easyprefs.library.Prefs;
import com.sicmagroup.tondi.utils.Constantes;
import com.tapadoo.alerter.Alerter;
import com.theartofdev.edmodo.cropper.CropImageView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;

import com.sicmagroup.formmaster.FormBuilder;

import static android.Manifest.permission.CAMERA;
import static com.sicmagroup.tondi.Accueil.CGU_FON_KEY;
import static com.sicmagroup.tondi.Accueil.CGU_FR_KEY;
import static com.sicmagroup.tondi.Connexion.ACCESS_RETURNf_KEY;
import static com.sicmagroup.tondi.Connexion.FIREBASE_TOKEN;
import static com.sicmagroup.tondi.Connexion.ID_UTILISATEUR_KEY;
import static com.sicmagroup.tondi.Connexion.NOM_KEY;
import static com.sicmagroup.tondi.Connexion.NUMERO_COMPTE_KEY;
import static com.sicmagroup.tondi.Connexion.PHOTO_CNI_KEY;
import static com.sicmagroup.tondi.Connexion.PHOTO_KEY;
import static com.sicmagroup.tondi.Connexion.PRENOMS_KEY;
import static com.sicmagroup.tondi.utils.Constantes.CODE_MARCHAND_KEY;
import static com.sicmagroup.tondi.utils.Constantes.SERVEUR;
import static com.sicmagroup.tondi.utils.Constantes.TOKEN;
import static com.sicmagroup.tondi.utils.Constantes.accessToken;

import cz.msebera.android.httpclient.entity.mime.content.ByteArrayBody;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;


public class Inscription_next extends AppCompatActivity {
    private CropImageView mCropImageView;
    private Uri mCropImageUri;
    private Uri mCropImageUri2;
    private RecyclerView mRecyclerView;
    private FormBuilder mFormBuilder;
    public static final  int CROPPING_CODE =2;
    public static final  int CROPPING_CODE_CNI = 3;
    private static final int TAG_NOM = 11;
    private static final int TAG_PRENOMS = 12;
    private static final int TAG_TEL = 13;
    private static final int TAG_PASS = 14;
    private static final int TAG_cPASS = 15;
    String url_inscrire_next = SERVEUR+"/api/v1/utilisateurs/inscrire_next";
    MediaPlayer mp_cgu_fon;
    MediaPlayer mp_cgu_fr;
    int id_utilisateur;
    private final int GALLERY = 1;
    private String upload_URL = SERVEUR+"/api/v1/utilisateurs/inscrire_next";
    JSONObject jsonObject;
    RequestQueue rQueue;
    ImageView photo_identite;
    ImageView photo_cni;
    ProgressDialog progressDialog;
    private ArrayList<String> permissionsToRequest;

    private final static int ALL_PERMISSIONS_RESULT = 107;
    private ArrayList<String> permissionsRejected = new ArrayList<>();
    private ArrayList<String> permissions = new ArrayList<>();
    Utilitaire utilitaire;
    String CURRENT_UUID;
    String ACCESS_TOKEN;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inscription_next);

        new Prefs.Builder()
                .setContext(this)
                .setMode(ContextWrapper.MODE_PRIVATE)
                .setPrefsName(getPackageName())
                .setUseDefaultSharedPreference(true)
                .build();
        Prefs.putString(ACCESS_RETURNf_KEY, "com.sicmagroup.tondi.Inscription_next");

        utilitaire = new Utilitaire(Inscription_next.this);
        if (!utilitaire.isConnected()) {
            this.finish();
        }

        Prefs.putInt(CGU_FR_KEY, 0);
        Prefs.putInt(CGU_FON_KEY, 0);
        permissions.add(CAMERA);
        permissionsToRequest = findUnAskedPermissions(permissions);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (permissionsToRequest.size() > 0) {
                requestPermissions(permissionsToRequest.toArray(new String[permissionsToRequest.size()]), ALL_PERMISSIONS_RESULT);
            }
        }

        // Vérifiez si l'extra "id_utilisateur" est présent
        Intent intent = getIntent();
        if (intent.hasExtra("id_utilisateur")) {
            int id_utilisateur = intent.getIntExtra("id_utilisateur", -1);
            CURRENT_UUID = intent.getStringExtra("user_uuid");
            ACCESS_TOKEN = intent.getStringExtra("accessToken");
            if (id_utilisateur != -1) {
                Log.e("id_utilisateur", String.valueOf(id_utilisateur));
            } else {
                Log.e("IntentError", "L'extra 'id_utilisateur' est manquant ou invalide.");
            }
        } else {
            Log.e("IntentError", "L'extra 'id_utilisateur' n'est pas passé à l'activité Inscription_next.");
        }

        photo_identite = findViewById(R.id.photo_identite);
        photo_cni = findViewById(R.id.photo_cni);
        Button btn_terminer = findViewById(R.id.btn_inscription_terminer);
        btn_terminer.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("ResourceAsColor")
            @Override
            public void onClick(View v) {
                if (photo_identite.getDrawable().getConstantState() == getResources().getDrawable(R.drawable.avatar_camera1).getConstantState()) {
                    Alerter.create(Inscription_next.this)
                            .setTitle("Vous devez soumettre une photo d'identité")
                            .setIcon(R.drawable.ic_warning)
                            .setTitleAppearance(R.style.TextAppearance_AppCompat_Large)
                            .setIconColorFilter(R.color.colorPrimaryDark)
                            .setBackgroundColorRes(R.color.colorWhite)
                            .show();
                } else if (photo_cni.getDrawable().getConstantState() == getResources().getDrawable(R.drawable.cni).getConstantState()) {
                    Alerter.create(Inscription_next.this)
                            .setTitle("Vous devez soumettre la photo de votre carte d'identité")
                            .setIcon(R.drawable.ic_warning)
                            .setTitleAppearance(R.style.TextAppearance_AppCompat_Large)
                            .setIconColorFilter(R.color.colorPrimaryDark)
                            .setBackgroundColorRes(R.color.colorWhite)
                            .show();
                } else {
                    Bitmap bm = ((BitmapDrawable) photo_identite.getDrawable()).getBitmap();
                    Bitmap bm2 = ((BitmapDrawable) photo_cni.getDrawable()).getBitmap();
                    EditText cm = findViewById(R.id.code_marchand);
                    String cmValue = cm.getText().toString();
                    uploadImage(bm, bm2, cmValue);
                }
            }
        });
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


    @SuppressLint("LongLogTag")
    private void uploadImage(final Bitmap bitmap, final Bitmap bitmap2, String cmValue) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        ByteArrayOutputStream byteArrayOutputStream2 = new ByteArrayOutputStream();

        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
        bitmap2.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream2);

        String encodedImage = Base64.encodeToString(byteArrayOutputStream.toByteArray(), Base64.DEFAULT);
        String encodedImage2 = Base64.encodeToString(byteArrayOutputStream2.toByteArray(), Base64.DEFAULT);

        OkHttpClient okHttpClient = new OkHttpClient();
        String imgname = String.valueOf(Calendar.getInstance().getTimeInMillis());
        String imgname2 = String.valueOf(Calendar.getInstance().getTimeInMillis());
        progressDialog = new ProgressDialog(Inscription_next.this);
        progressDialog.setCancelable(false);
        progressDialog.setMessage("Veuillez patienter SVP! Sauvegarde des photos en cours...");
        progressDialog.show();
        Prefs.putString(TOKEN, ACCESS_TOKEN);
        // enregistrer la photo en local
        utilitaire.saveToInternalStorage(bitmap, imgname);
        utilitaire.saveToInternalStorage(bitmap2, imgname2);

        ContextWrapper cw = new ContextWrapper(Inscription_next.this);
        File directory = cw.getDir("tontine_photos", Context.MODE_PRIVATE);
        File mypath1 = new File(directory, imgname + ".png");
        File mypath2 = new File(directory, imgname2 + ".png");

        RequestBody body;
        Log.e("cmValue", cmValue + "==");
        if (cmValue == null || cmValue.equals("null")) {
            body = new MultipartBody.Builder().setType(MultipartBody.FORM)
                    .addFormDataPart("pp_file", mypath1.getName(), RequestBody.create(MediaType.parse("multipart/form-data"), mypath1))
                    .addFormDataPart("cni_file", mypath2.getName(), RequestBody.create(MediaType.parse("multipart/form-data"), mypath2))
                    .build();
            Log.e("La valeur du body", body.toString());
        } else {
            body = new MultipartBody.Builder().setType(MultipartBody.FORM)
                    .addFormDataPart("pp_file", mypath1.getName(), RequestBody.create(MediaType.parse("multipart/form-data"), mypath1))
                    .addFormDataPart("cni_file", mypath2.getName(), RequestBody.create(MediaType.parse("multipart/form-data"), mypath2))
                    .addFormDataPart("referent", cmValue)
                    .build();
            Log.e("La valeur du body dans le else", body.toString());
        }

        FutureTask<Boolean> refreshDataBaseTask = new FutureTask<>(() -> {
            try {
                utilitaire.refreshDatabse();
            } finally {
                Log.e("refreshDb", "can't refresh bdd");
            }
            return true;
        });

        FutureTask<Boolean> task = new FutureTask<>(() -> {
            try {
                Log.e("task", "intask");
                Log.e("La valeur du UUID", CURRENT_UUID);
                Log.e("Le token est:", accessToken);
                ResponseBody responseBody = okHttpClient.newCall(
                        new Request.Builder().url(Constantes.URL_INSCRIPTION_NEXT + CURRENT_UUID).put(body).build()
                ).execute().body();

                if (responseBody != null) {
                    Log.e("task", "intask IN IF");
                    Log.e("La reponse envoie de fichier", responseBody.toString());
                    String responseString = responseBody.string();
                    JSONObject response = new JSONObject(responseString);

                    if (response.has("responseCode")) {
                        int responseCode = response.getInt("responseCode");
                        Log.e("La response ", response.toString());
                        if (responseCode == 0) {
                            try {
                                new Thread(refreshDataBaseTask).start();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            JSONObject user = response.getJSONObject("body");
                            final Utilisateur utilisateur = Utilisateur.find(Utilisateur.class, "id_utilisateur = ?", Prefs.getString(ID_UTILISATEUR_KEY, null)).get(0);
                            Log.e("My App4", user.toString());

                            SharedPreferences sharedPreferences = getSharedPreferences("MyPrefs", MODE_PRIVATE);
                            String nom = sharedPreferences.getString("nom", "");
                            String prenoms = sharedPreferences.getString("prenoms", "");

                            // Get values from JSON with default values if keys are missing
                            String profilePicture = user.optString("profilePicture", utilisateur.getPhoto_identite());
                            String cniPicture = user.optString("cniPicture", utilisateur.getcni_photo());
                            String accountNumber = user.optString("accountNumber", utilisateur.getNumero_compte());
                            String referent = user.optString("referent", "");
                            String firstName = user.optString("firstName", Prefs.getString(PRENOMS_KEY,""));
                            String lastName = user.optString("lastName", Prefs.getString(NOM_KEY, ""));
                            String updatedAtStr = user.optString("updatedAt", "1970-01-01 00:00:00");

                            utilisateur.setPhoto_identite(profilePicture);
                            utilisateur.setcni_photo(cniPicture);
                            Log.e("My App2", profilePicture);
                            @SuppressLint("SimpleDateFormat") DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                            Date maj = (Date) formatter.parse(updatedAtStr);
                            long output_maj = maj.getTime() / 1000L;
                            String str_maj = Long.toString(output_maj);
                            long timestamp_maj = Long.parseLong(str_maj) * 1000;
                            utilisateur.setMaj(timestamp_maj);
                            utilisateur.setNumero_compte(accountNumber);
                            utilisateur.setNom(lastName);
                            utilisateur.setPrenoms(firstName);
                            utilisateur.save();
                            utilitaire.saveToInternalStorage(bitmap, profilePicture);
                            utilitaire.saveToInternalStorage(bitmap2, cniPicture);
                            Prefs.putString(PHOTO_KEY, profilePicture);
                            Prefs.putString(FIREBASE_TOKEN, "");
                            Prefs.putString(PHOTO_CNI_KEY, cniPicture);
                            Prefs.putString(NUMERO_COMPTE_KEY, accountNumber);
                            Log.e("Le numero de compte est:", accountNumber);
                            Log.e("inscrire_next", "onResponse: " + user);
                            Prefs.putString(CODE_MARCHAND_KEY, referent);
                            JSONArray tontines = user.optJSONArray("tontines");
                            if (tontines == null) {
                                tontines = new JSONArray();
                            }
                            // Traitez le tableau tontines ici
                            progressDialog.dismiss();
                            Inscription_next.this.finish();
                            Intent i = new Intent(Inscription_next.this, Message_ok.class);
                            i.putExtra("msg_desc", "Votre inscription s'est bien déroulée!");
                            i.putExtra("class", "com.sicmagroup.tondi.NouvelleTontine");
                            startActivity(i);
                            return response.getInt("responseCode") == 0;
                        } else {
                            showError(response.getString("body"));
                            Log.e("task", "intask else");
                            progressDialog.dismiss();
                            return false;
                        }
                    } else {
                        showError("La réponse du serveur est invalide");
                        Log.e("task", "responseCode manquant");
                        progressDialog.dismiss();
                        return false;
                    }
                } else {
                    showError("Une erreur est survenue: ResponseBody est null");
                    Log.e("task", "responseBody est null");
                    progressDialog.dismiss();
                    return false;
                }
            } catch (IOException e) {
                showError("Une erreur IO est survenue: " + e.getMessage());
                Log.e("task", "IOException: " + e.getMessage());
               // e.printStackTrace();
                progressDialog.dismiss();
                return false;
            } catch (JSONException e) {
                showError("Une erreur JSON est survenue: " + e.getMessage());
                Log.e("task", "JSONException: " + e.getMessage());
              //  e.printStackTrace();
                progressDialog.dismiss();
                return false;
            } catch (ParseException e) {
                showError("Une erreur de parsing est survenue: " + e.getMessage());
                Log.e("task", "ParseException: " + e.getMessage());
               // e.printStackTrace();
                progressDialog.dismiss();
                return false;
            }
        });

        try {
            new Thread(task).start();
            task.get();
        } catch (ExecutionException e) {
            showError("Une erreur ExecutionException est survenue: " + e.getMessage());
            Log.e("task", "ExecutionException: " + e.getMessage());
            e.printStackTrace();
            progressDialog.dismiss();
        } catch (InterruptedException e) {
            showError("Une erreur InterruptedException est survenue: " + e.getMessage());
            Log.e("task", "InterruptedException: " + e.getMessage());
            e.printStackTrace();
            progressDialog.dismiss();
        }
    }


    private void showError(String message) {
        Alerter.create(Inscription_next.this)
                .setTitle(message)
                .setIcon(R.drawable.ic_warning)
                .setTitleAppearance(R.style.TextAppearance_AppCompat_Large)
                .setBackgroundColorRes(R.color.colorWhite)
                .show();
    }


    private void alertView( String title ,String message ) {
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle( title )
                .setIcon(R.drawable.ic_warning)
                .setMessage(message)

                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialoginterface, int i) {
                    }
                }).show();
    }

    /**
     * On load image button click, start pick  image chooser activity.
     */
    public void onLoadImageClick(View view) {
        startActivityForResult(getPickImageChooserIntent(), 200);
    }

    public void onLoadImageClick2(View view) {
        startActivityForResult(getPickImageChooserIntent(), 150);
    }

    @Override
    protected void onActivityResult(int  requestCode, int resultCode, Intent data) {
        // image from camera
        super.onActivityResult(requestCode, resultCode, data);
        Log.e("requestcode", requestCode+"");
        if (requestCode == 200 || requestCode == 2){
            if (resultCode == Activity.RESULT_OK) {
                Uri imageUri = getPickImageResultUri(data);

                // For API >= 23 we need to check specifically that we have permissions to read external storage,
                // but we don't know if we need to for the URI so the simplest is to try open the stream and see if we get error.
                boolean requirePermissions = false;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
                        checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED &&
                        isUriRequiresPermissions(imageUri)) {
                    // request permissions and handle the result in onRequestPermissionsResult()
                    requirePermissions = true;
                    mCropImageUri = imageUri;
                    requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 0);
                }


                if (!requirePermissions) {
                    //mCropImageView.setImageUriAsync(imageUri);
                    Intent intent = new Intent(Inscription_next.this, CropActivity.class);
                    //Toast.makeText(getApplicationContext(),imageUri.toString(),Toast.LENGTH_LONG).show();
                    intent.putExtra("imageUri", imageUri.toString());
                    startActivityForResult(intent, CROPPING_CODE);// Activity is started with requestCode 2
                }
            }
            // cropping image
            if (resultCode == CROPPING_CODE) {
                byte[] byteArray = data.getByteArrayExtra("photo_profil");
                mCropImageUri = data.getParcelableExtra("imageUriProfile");
                Bitmap bmp = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);
                //ImageView photo = findViewById(R.id.photo_identite);
                photo_identite.setImageBitmap(bmp);
                Button btn_terminer = findViewById(R.id.btn_inscription_terminer);
                btn_terminer.setVisibility(View.VISIBLE);
//                Toast.makeText(getApplicationContext(),data.getStringExtra("photo_profil"),Toast.LENGTH_LONG).show();
            }

        } else  if (requestCode == 150 || requestCode == 3){
            if (resultCode == Activity.RESULT_OK) {
                Uri imageUri = getPickImageResultUri(data);

                // For API >= 23 we need to check specifically that we have permissions to read external storage,
                // but we don't know if we need to for the URI so the simplest is to try open the stream and see if we get error.
                boolean requirePermissions = false;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
                        checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED &&
                        isUriRequiresPermissions(imageUri)) {
                    // request permissions and handle the result in onRequestPermissionsResult()
                    requirePermissions = true;
                    mCropImageUri = imageUri;
                    requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 0);
                }


                if (!requirePermissions) {
                    //mCropImageView.setImageUriAsync(imageUri);
                    Intent intent = new Intent(Inscription_next.this, CropActivityCni.class);
                    //Toast.makeText(getApplicationContext(),imageUri.toString(),Toast.LENGTH_LONG).show();
                    intent.putExtra("imageUri", imageUri.toString());
                    startActivityForResult(intent, CROPPING_CODE_CNI);// Activity is started with requestCode 2
                }
            }
            // cropping image
            if (resultCode == CROPPING_CODE_CNI) {
                byte[] byteArray = data.getByteArrayExtra("photo_cni");
                mCropImageUri2 = data.getParcelableExtra("imageUriCni");
                Bitmap bmp = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);
                //ImageView photo = findViewById(R.id.photo_identite);
                photo_cni.setImageBitmap(bmp);
                Button btn_terminer = findViewById(R.id.btn_inscription_terminer);
                btn_terminer.setVisibility(View.VISIBLE);
//                Toast.makeText(getApplicationContext(),data.getStringExtra("photo_profil"),Toast.LENGTH_LONG).show();
            }

        }

        //}
    }


    /**
     * Create a chooser intent to select the  source to get image from.<br/>
     * The source can be camera's  (ACTION_IMAGE_CAPTURE) or gallery's (ACTION_GET_CONTENT).<br/>
     * All possible sources are added to the  intent chooser.
     */
    public Intent getPickImageChooserIntent() {

// Determine Uri of camera image to  save.
        Uri outputFileUri =  getCaptureImageOutputUri();

        List<Intent> allIntents = new  ArrayList<>();
        PackageManager packageManager =  getPackageManager();

// collect all camera intents
        Intent captureIntent = new  Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        List<ResolveInfo> listCam =  packageManager.queryIntentActivities(captureIntent, 0);
        for (ResolveInfo res : listCam) {
            Intent intent = new  Intent(captureIntent);
            intent.setComponent(new ComponentName(res.activityInfo.packageName, res.activityInfo.name));
            intent.setPackage(res.activityInfo.packageName);
            if (outputFileUri != null) {
                intent.putExtra(MediaStore.EXTRA_OUTPUT, outputFileUri);
            }
            allIntents.add(intent);
        }

// collect all gallery intents
        Intent galleryIntent = new  Intent(Intent.ACTION_GET_CONTENT);
        galleryIntent.setType("image/*");
        List<ResolveInfo> listGallery =  packageManager.queryIntentActivities(galleryIntent, 0);
        for (ResolveInfo res : listGallery) {
            Intent intent = new  Intent(galleryIntent);
            intent.setComponent(new  ComponentName(res.activityInfo.packageName, res.activityInfo.name));
            intent.setPackage(res.activityInfo.packageName);
            allIntents.add(intent);
        }

// the main intent is the last in the  list (fucking android) so pickup the useless one
        Intent mainIntent =  allIntents.get(allIntents.size() - 1);
        for (Intent intent : allIntents) {
            if  (intent.getComponent().getClassName().equals("com.android.documentsui.DocumentsActivity"))  {
                mainIntent = intent;
                break;
            }
        }
        allIntents.remove(mainIntent);

// Create a chooser from the main  intent
        Intent chooserIntent =  Intent.createChooser(mainIntent, "Sélectionner une source");

// Add all other intents
        chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS,  allIntents.toArray(new Parcelable[allIntents.size()]));

        return chooserIntent;
    }

    /**
     * Get URI to image received from capture  by camera.
     */
    private Uri getCaptureImageOutputUri() {
        Uri outputFileUri = null;
        File getImage = getExternalCacheDir();
        Log.e("tesu", getImage.getPath());
        if (getImage != null) {
            outputFileUri = Uri.fromFile(new  File(getImage.getPath(), "pickImageResult.jpeg"));
        }
        Log.e("tesu2", outputFileUri.getPath());

        return outputFileUri;
    }

    /**
     * Get the URI of the selected image from  {@link #getPickImageChooserIntent()}.<br/>
     * Will return the correct URI for camera  and gallery image.
     *
     * @param data the returned data of the  activity result
     */
    public Uri getPickImageResultUri(Intent  data) {
        boolean isCamera = true;
        if (data != null && data.getData() != null) {
            String action = data.getAction();
            isCamera = action != null  && action.equals(MediaStore.ACTION_IMAGE_CAPTURE);
        }
        return isCamera ?  getCaptureImageOutputUri() : data.getData();
    }

    /**
     * Test if we can open the given Android URI to test if permission required error is thrown.<br>
     */
    public boolean isUriRequiresPermissions(Uri uri) {
        try {
            ContentResolver resolver = getContentResolver();
            InputStream stream = resolver.openInputStream(uri);
            stream.close();
            return false;
        } catch (FileNotFoundException e) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                if (e.getCause() instanceof ErrnoException) {
                    return true;
                }
            }
        } catch (Exception e) {
        }
        return false;
    }

    @Override
    public void onBackPressed() {
        // your code.
        final AlertDialog.Builder builder = new AlertDialog.Builder(Inscription_next.this);
        builder.setTitle("Annuler la photo");
        builder.setMessage("Êtes vous sûr de vouloir annuler la photo ?");
        builder.setPositiveButton("Oui", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
//                Toast.makeText(Inscription_next.this, "Finalisez votre inscription svp!", Toast.LENGTH_SHORT).show();
                finishAffinity();
            }
        });
        builder.setNegativeButton("Non", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i){
                dialogInterface.dismiss();
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

}
