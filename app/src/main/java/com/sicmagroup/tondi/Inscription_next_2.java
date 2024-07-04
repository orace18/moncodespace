package com.sicmagroup.tondi;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.ContextWrapper;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.system.ErrnoException;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
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
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.snackbar.Snackbar;
import com.pixplicity.easyprefs.library.Prefs;
import com.tapadoo.alerter.Alerter;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import static android.Manifest.permission.CAMERA;
import static com.sicmagroup.tondi.Accueil.CGU_FON_KEY;
import static com.sicmagroup.tondi.Accueil.CGU_FR_KEY;
import static com.sicmagroup.tondi.Connexion.ACCESS_RETURNf_KEY;
import static com.sicmagroup.tondi.Connexion.ID_UTILISATEUR_KEY;
import static com.sicmagroup.tondi.Connexion.NUMERO_COMPTE_KEY;
import static com.sicmagroup.tondi.Connexion.PHOTO_CNI_KEY;
import static com.sicmagroup.tondi.utils.Constantes.SERVEUR;

public class Inscription_next_2 extends AppCompatActivity {
    public static final  int CROPPING_CODE =2;
    int id_utilisateur;
    private String upload_URL = SERVEUR+"/api/v1/utilisateurs/inscrire_next_2";
    JSONObject jsonObject;
    RequestQueue rQueue;
    ImageView photo_identite;
    ProgressDialog progressDialog;
    private ArrayList<String> permissionsToRequest;
    private Uri mCropImageUri;

    private final static int ALL_PERMISSIONS_RESULT = 107;
    private ArrayList<String> permissionsRejected = new ArrayList<>();
    private ArrayList<String> permissions = new ArrayList<>();
    Utilitaire utilitaire;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inscription_next_2);

        new Prefs.Builder()
                .setContext(this)
                .setMode(ContextWrapper.MODE_PRIVATE)
                .setPrefsName(getPackageName())
                .setUseDefaultSharedPreference(true)
                .build();
        Prefs.putString(ACCESS_RETURNf_KEY,"com.sicmagroup.tondi.Inscription_next_2");
        utilitaire = new Utilitaire(Inscription_next_2.this);
        if (!utilitaire.isConnected()){
            this.finish();
        }

        Prefs.putInt(CGU_FR_KEY, 0);
        Prefs.putInt(CGU_FON_KEY, 0);
        permissions.add(CAMERA);
        permissionsToRequest = findUnAskedPermissions(permissions);
        //get the permissions we have asked for before but are not granted..
        //we will store this in a global list to access later.


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {


            if (permissionsToRequest.size() > 0)
                requestPermissions(permissionsToRequest.toArray(new String[permissionsToRequest.size()]), ALL_PERMISSIONS_RESULT);
        }

        id_utilisateur = Integer.parseInt(getIntent().getStringExtra("id_utilisateur"));
        photo_identite = findViewById(R.id.photo_cni);
        Button btn_inscription_next = findViewById(R.id.btn_inscription_next_2);
        Button btn_terminer = findViewById(R.id.btn_inscription_terminer_2);
        btn_terminer.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("ResourceAsColor")
            @Override
            public void onClick(View v) {
                if (photo_identite.getDrawable().getConstantState() != getResources().getDrawable( R.drawable.ic_camera).getConstantState())
                {
                    Bitmap bm=((BitmapDrawable)photo_identite.getDrawable()).getBitmap();
                    uploadImage(bm);
                }
                else
                {
                    Alerter.create(Inscription_next_2.this)
                            .setTitle("Vous devez soumettre la photo de votre CNI")
                            .setIcon(R.drawable.ic_warning)
                            .setTitleAppearance(R.style.TextAppearance_AppCompat_Large)
                            .setIconColorFilter(R.color.colorPrimaryDark)
                            //.setText("Vous pouvez maintenant vous connecter.")
                            .setBackgroundColorRes(R.color.colorWhite) // or setBackgroundColorInt(Color.CYAN)
                            .show();
                }


            }
        });
    }

    private void uploadImage(final Bitmap bitmap){

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
        String encodedImage = Base64.encodeToString(byteArrayOutputStream.toByteArray(), Base64.DEFAULT);


        try {
            jsonObject = new JSONObject();
            String imgname = String.valueOf(Calendar.getInstance().getTimeInMillis());
            jsonObject.put("id_utilisateur", id_utilisateur);
            jsonObject.put("name", imgname);
            //  Log.e("Image name", etxtUpload.getText().toString().trim());
            jsonObject.put("image", encodedImage);

            // jsonObject.put("aa", "aa");
        } catch (JSONException e) {
            //Log.e("JSONObject Here", e.toString());
        }
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, upload_URL, jsonObject,
                new Response.Listener<JSONObject>() {
                    @SuppressLint("ResourceAsColor")
                    @Override
                    public void onResponse(JSONObject result) {
                        progressDialog.dismiss();
                        Log.e("Response", result.toString());
                        try {
                            //JSONObject result = new JSONObject(response);
                            //Log.d("My App", obj.toString());
                            if (result.getBoolean("success")){
                                JSONObject user = result.getJSONObject("data");
                                // enregistrer la photo en local
                                //List<Utilisateur> utilisateur = Utilisateur.find(Utilisateur.class, "id = ?", "25");
                                final Utilisateur utilisateur = Utilisateur.find(Utilisateur.class, "id_utilisateur = ?", Prefs.getString(ID_UTILISATEUR_KEY,null)).get(0);
                                utilisateur.setcni_photo(user.getString("photo_cni"));
                                //Log.d("utilisateur_id","utilisateur:"+utilisateur.getId());
                                @SuppressLint("SimpleDateFormat") DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

                                Date maj = (Date)formatter.parse(user.getString("maj"));
                                long output_maj=maj.getTime()/1000L;
                                String str_maj=Long.toString(output_maj);
                                long timestamp_maj = Long.parseLong(str_maj) * 1000;
                                utilisateur.setMaj(timestamp_maj);
                                utilisateur.setNumero_compte(result.getString("numero_compte"));
                                utilisateur.save();
                                // enregistrer la photo en local
                                utilitaire.saveToInternalStorage(bitmap,user.getString("photo_cni"));
                                //URL url = new URL(user.getString("photo_url"));
                                //utilitaire.saveImageUrl(url,user.getString("photo_identite"));
                                //Log.d("utilisateur_id","utilisateur:"+user.getString("photo_url"));
                                Prefs.putString(PHOTO_CNI_KEY, user.getString("photo_cni"));
                                Prefs.putString(NUMERO_COMPTE_KEY, result.getString("numero_compte"));


                                Intent i = new Intent(Inscription_next_2.this, Message_changer_pin.class);
                                i.putExtra("msg_desc","Votre inscription s'est bien déroulée!");
                                i.putExtra("class","com.sicmagroup.tondi.Dashboard");
                                startActivity(i);

                            }else{
                                Alerter.create(Inscription_next_2.this)
                                        .setTitle(result.getString("message"))
                                        .setIcon(R.drawable.ic_warning)
                                        .setTitleAppearance(R.style.TextAppearance_AppCompat_Large)
                                        .setIconColorFilter(R.color.colorPrimaryDark)
                                        //.setText("Vous pouvez maintenant vous connecter.")
                                        .setBackgroundColorRes(R.color.colorWhite) // or setBackgroundColorInt(Color.CYAN)
                                        .show();
                            }
                        } catch (Throwable t) {
                            //Log.d("Inscript_next", String.valueOf(t.getCause()));
                            //Log.e("My App", "Could not parse malformed JSON: \"" + result.toString() + "\"");
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                progressDialog.dismiss();
                ConstraintLayout mainLayout =  findViewById(R.id.layout_inscription_next_2);

                String message;
                if (volleyError instanceof NetworkError || volleyError instanceof AuthFailureError || volleyError instanceof TimeoutError) {
                    //Toast.makeText(Inscription_next.this, "error:"+volleyError.getMessage(), Toast.LENGTH_SHORT).show();
                    message = "Aucune connexion Internet!";
                    Snackbar snackbar = Snackbar
                            .make(mainLayout, message, Snackbar.LENGTH_INDEFINITE)
                            .setAction("REESSAYER", new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    uploadImage(bitmap);
                                }
                            });
                    snackbar.getView().setBackgroundColor(ContextCompat.getColor(Inscription_next_2.this, R.color.colorGray));
                    // Changing message text color
                    snackbar.setActionTextColor(getResources().getColor(R.color.colorPrimaryDark));
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
                                    uploadImage(bitmap);
                                }
                            });
                    snackbar.getView().setBackgroundColor(ContextCompat.getColor(Inscription_next_2.this, R.color.colorGray));
                    // Changing message text color
                    snackbar.setActionTextColor(getResources().getColor(R.color.colorPrimaryDark));
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
                                    uploadImage(bitmap);
                                }
                            });
                    snackbar.getView().setBackgroundColor(ContextCompat.getColor(Inscription_next_2.this, R.color.colorGray));
                    // Changing message text color
                    snackbar.setActionTextColor(getResources().getColor(R.color.colorPrimaryDark));
                    // Changing action button text color
                    View sbView = snackbar.getView();
                    TextView textView = (TextView) sbView.findViewById(com.google.android.material.R.id.snackbar_text);
                    textView.setTextColor(Color.WHITE);
                    snackbar.show();
                }

            }
        });

        rQueue = Volley.newRequestQueue(Inscription_next_2.this);
        jsonObjectRequest.setRetryPolicy(new DefaultRetryPolicy(
                5000,
                -1,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        rQueue.add(jsonObjectRequest);
        progressDialog = new ProgressDialog(Inscription_next_2.this);
        progressDialog.setCancelable(false);
        progressDialog.setMessage("Veuillez patienter SVP! Finalisation de l'inscription en cours...");
        progressDialog.show();

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


    private boolean hasPermission(String permission) {
        if (canMakeSmores()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                return (checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED);
            }
        }
        return true;
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

    /**
     * On load image button click, start pick  image chooser activity.
     */
    public void onLoadImageClick(View view) {
        startActivityForResult(getPickImageChooserIntent(), 200);
    }



    @Override
    protected void onActivityResult(int  requestCode, int resultCode, Intent data) {
        // image from camera
        super.onActivityResult(requestCode, resultCode, data);
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
                Intent intent = new Intent(Inscription_next_2.this, CropActivity.class);
                //Toast.makeText(getApplicationContext(),imageUri.toString(),Toast.LENGTH_LONG).show();
                intent.putExtra("imageUri", imageUri.toString());
                startActivityForResult(intent, CROPPING_CODE);// Activity is started with requestCode 2
            }
        }

        // cropping image
        if (resultCode == CROPPING_CODE) {
            //
            byte[] byteArray = data.getByteArrayExtra("photo_profil");
            Bitmap bmp = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);
            //ImageView photo = findViewById(R.id.photo_identite);
            photo_identite.setImageBitmap(bmp);
            Button btn_terminer = findViewById(R.id.btn_inscription_terminer_2);
            btn_terminer.setVisibility(View.VISIBLE);
            //Toast.makeText(getApplicationContext(),data.getStringExtra("photo_profil"),Toast.LENGTH_LONG).show();

        }
    }

    /*@Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        if (mCropImageUri != null && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            mCropImageView.setImageUriAsync(mCropImageUri);
        } else {
            Toast.makeText(this, "Required permissions are not granted", Toast.LENGTH_LONG).show();
        }
    }*/

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
        if (getImage != null) {
            outputFileUri = Uri.fromFile(new  File(getImage.getPath(), "pickImageResult.jpeg"));
        }
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
        final AlertDialog.Builder builder = new AlertDialog.Builder(Inscription_next_2.this);
        builder.setTitle("Annuler la photo de la CNI");
        builder.setMessage("Êtes vous sûr de vouloir annuler la photo votre CNI ?");
        builder.setPositiveButton("Oui", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Toast.makeText(Inscription_next_2.this, "Finalisez votre inscription svp!", Toast.LENGTH_SHORT).show();
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