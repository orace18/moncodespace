package com.sicmagroup.tondi;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.CountDownTimer;
import android.provider.Settings;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageButton;
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
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.pixplicity.easyprefs.library.Prefs;
import com.romellfudi.permission.PermissionService;
import com.sicmagroup.tondi.ui.plaintes.PlainteAdapter;
import com.tapadoo.alerter.Alerter;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static com.sicmagroup.tondi.Connexion.ID_UTILISATEUR_KEY;
import static com.sicmagroup.tondi.Connexion.TEL_KEY;
import static com.sicmagroup.tondi.Connexion.url_save_plainte;
import static com.sicmagroup.tondi.utils.Constantes.accessToken;

public class MesPlaintes extends AppCompatActivity {

    private static MediaRecorder mediaRecorder;
    private static MediaPlayer mediaPlayer;

    private static String audioFilePath;

    private boolean isRecording = false;
    ImageButton closeButton;
    ImageButton playButton;
    ImageButton recordButton;
    ImageButton sendButton;
    ProgressBar progressBar;
    TextView timing;
    CountDownTimer timer = null;
    long timerLevel = 0;
    long time_of_audio;
    RecyclerView plainte_recyclerView;
    ArrayList<Plainte> plainteArrayList;
    PlainteAdapter plainteAdapter;
    File mypath;
    ProgressDialog progressDialog;
    JSONObject jsonObject;
    RequestQueue rQueue;
    Button sort_btn;
    Boolean sorted = false;

    int serverResponseCode = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mes_plaintes);
        //Toolbar toolbar = findViewById(R.id.toolbar);
        //setSupportActionBar(toolbar);

       // FloatingActionButton fab = findViewById(R.id.fab);
        /*fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });*/

//        audioFilePath =
//                Environment.getExternalStorageDirectory().getAbsolutePath()
//                        + "/myaudio.3gp";
        time_of_audio = 0;
        sort_btn = (Button) findViewById(R.id.sort_plainte);
        plainte_recyclerView = (RecyclerView) findViewById(R.id.plainte_recyclerView);
        plainteArrayList = new ArrayList<>();
        plainteArrayList = (ArrayList<Plainte>) Plainte.listAll(Plainte.class);
//        Collections.sort(plainteArrayList);
//        Collections.reverse(plainteArrayList);
        plainteAdapter = new PlainteAdapter(this, plainteArrayList);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        plainte_recyclerView.setLayoutManager(layoutManager);
        plainte_recyclerView.setItemAnimator(new DefaultItemAnimator());
        plainte_recyclerView.setAdapter(plainteAdapter);
//        plainte_recyclerView.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                plainteAdapter.notifyDataSetChanged();
//            }
//        });

        progressDialog = new ProgressDialog(MesPlaintes.this);

        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, 1);

        new PermissionService(this).request(
                new String[]{Manifest.permission.RECORD_AUDIO},
                callback);

        //button retour
        Button back_to = findViewById(R.id.back_to);
        back_to.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MesPlaintes.this.finish();
                startActivity(new Intent(MesPlaintes.this, Dashboard.class));
            }
        });

        sort_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!sorted)
                {
                    sorted = true;

                    Collections.reverse(plainteArrayList);
                    Toast.makeText(MesPlaintes.this, "Trier du plus récent au plus ancien", Toast.LENGTH_SHORT).show();
                    Log.d("trie", "!sorted");
                    sort_btn.setText("A-Z");
                }
                else {
                    sorted = false;
                    Collections.sort(plainteArrayList);
                    Toast.makeText(MesPlaintes.this, "Trier du plus ancien au plus récent", Toast.LENGTH_SHORT).show();
                    Log.d("trie", "sorted");
                    sort_btn.setText("Z-A");
                }

                plainteAdapter.notifyDataSetChanged();
            }
        });

        //button deconnexion
//        Button btn_deconnexion = (Button) findViewById(R.id.btn_deconnexion);
//        btn_deconnexion.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                alertView("Déconnexion","Êtes vous sûr de vouloir vous déconnecter?");
//            }
//        });

//        Button btn_accueil = findViewById(R.id.btn_accueil);
//        btn_accueil.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                MesPlaintes.this.finish();
//                startActivity(new Intent(MesPlaintes.this, Dashboard.class));
//            }
//        });

//        Button btn_about = findViewById(R.id.btn_about);
//        btn_about.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                MesPlaintes.this.finish();
//                startActivity(new Intent(MesPlaintes.this, About_us.class));
//            }
//        });

        FloatingActionButton btn_nouvelle_plainte = (FloatingActionButton)  findViewById(R.id.btn_nouvelle_plainte);
        btn_nouvelle_plainte.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ViewDialog alert = new ViewDialog();
                alert.showDialog(MesPlaintes.this);
            }
        });
    }

    public class ViewDialog {
        void showDialog(Activity activity){
            Dialog dialog = new Dialog(activity);
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.setCancelable(true);
            dialog.setContentView(R.layout.dialog_new_plainte);

            ContextWrapper cw = new ContextWrapper(MesPlaintes.this);
            // path to /data/data/yourapp/app_data/imageDir
            File directory = cw.getDir("tontine_plaintes", Context.MODE_PRIVATE);
            time_of_audio = System.currentTimeMillis() / 1000L;
            Utilisateur u = new Utilisateur().getUser(Prefs.getString(TEL_KEY, ""));
            mypath=new File(directory,time_of_audio+"_"+ u.getId_utilisateur()+".mp3");
            audioFilePath = mypath.getPath();

            dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialog) {
                    dialog.dismiss();
                    if(timer != null)
                    {
                        timer.cancel();
                    }

                    timing.setText("0:00");
                    //supprimer le fichier

                    mypath.delete();

                    if(mediaRecorder != null)
                    {
                        mediaRecorder.stop();
                        mediaRecorder.release();
                        mediaRecorder = null;
                    }

                }
            });
            recordButton = (ImageButton) dialog.findViewById(R.id.btn_record);
            playButton = (ImageButton) dialog.findViewById(R.id.btn_play);
            closeButton = (ImageButton) dialog.findViewById(R.id.btn_close);
            sendButton = (ImageButton) dialog.findViewById(R.id.btn_send);
            progressBar = (ProgressBar) dialog.findViewById(R.id.progressBar);
            timing = (TextView) dialog.findViewById(R.id.timing);
            progressBar.setMax(120);
            playButton.setVisibility(View.GONE);



            if(!hasMicrophone())
            {
                playButton.setEnabled(false);
                recordButton.setEnabled(false);
                sendButton.setEnabled(false);
            }
            else
            {
                playButton.setEnabled(false);
                sendButton.setEnabled(false);
            }

            closeButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialog.dismiss();
                    if(timer != null)
                        timer.cancel();
                    timing.setText("0:00");
                    //supprimer le fichier
                    mypath.delete();
                    if(mediaRecorder != null)
                    {
                        mediaRecorder.stop();
                        mediaRecorder.release();
                        mediaRecorder = null;
                    }


                }
            });

            sendButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    progressDialog.setCancelable(false);
                    progressDialog.setMessage("Envoie de la plainte en cours...");
                    progressDialog.show();
                    stopAudio();
                    Plainte plainte = new Plainte();
                    Utilisateur u = new Utilisateur().getUser(Prefs.getString(TEL_KEY, ""));
                    plainte.setAuteur(u.getId_utilisateur());

                    Date currentTime = Calendar.getInstance().getTime();
                    long output_creation=currentTime.getTime()/1000L;
                    String str_creation=Long.toString(output_creation);
                    long timestamp_creation = Long.parseLong(str_creation) * 1000;


                    plainte.setCreation(timestamp_creation);
                    plainte.setDuration(timerLevel);
                    if(timer != null)
                        timer.cancel();
                    plainte.setFile_name(time_of_audio+"_"+u.getId_utilisateur()+".mp3");

                    ContextWrapper cw = new ContextWrapper(MesPlaintes.this);
                    File directory = cw.getDir("tontine_plaintes", Context.MODE_PRIVATE);
                    File mypath=new File(directory,audioFilePath);
                    plainte.setSended(true);
                    plainte.save();
                    //Afficher sa dans le recyclerView
                    plainteArrayList.add(plainte);
                    plainteAdapter.notifyDataSetChanged();

                    //Envoyer les détailles de la plainte et le fichier au serveur
                    uploadPlainte(plainte, accessToken);


                    dialog.dismiss();
                }
            });



            dialog.show();



        }
    }

    public void recordAudio(View view) throws IOException
    {
        isRecording = true;
        playButton.setEnabled(false);
        recordButton.setEnabled(false);
        sendButton.setEnabled(true);

        try {
            mediaRecorder = new MediaRecorder();
            mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
            mediaRecorder.setOutputFile(audioFilePath);
            mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
            mediaRecorder.prepare();
        } catch (Exception e) {
            e.printStackTrace();
        }

        mediaRecorder.start();
        timer = new CountDownTimer(120000, 1000) {

            public void onTick(long millisUntilFinished) {
                if(millisUntilFinished / 1000 >= 60 && millisUntilFinished / 1000 < 120)
                    timing.setText("1:" + (millisUntilFinished / 1000 - 60) );
                else if(millisUntilFinished / 1000 == 120)
                    timing.setText("2:" + (millisUntilFinished / 1000 - 120) );
                else
                    timing.setText("0:" + millisUntilFinished / 1000 );
                timerLevel = millisUntilFinished / 1000;
                progressBar.setProgress(120 - (int)millisUntilFinished / 1000);
            }

            public void onFinish() {
                stopAudio();
            }

        }.start();


    }

    public void stopAudio ()
    {

        playButton.setEnabled(true);

        if (isRecording)
        {

            recordButton.setEnabled(false);
            mediaRecorder.stop();
            mediaRecorder.release();
            mediaRecorder = null;
            isRecording = false;

        } else {
            mediaPlayer.release();
            mediaPlayer = null;
            recordButton.setEnabled(true);
        }


    }

//    public void playAudio (View view) throws IOException
//    {
//        playButton.setEnabled(false);
//        recordButton.setEnabled(false);
//
//        mediaPlayer = new MediaPlayer();
//        mediaPlayer.setDataSource(audioFilePath);
//        mediaPlayer.prepare();
//        mediaPlayer.start();
//    }


    public void uploadPlainte(Plainte plainte, String token) {
        String fileName = plainte.getFile_name();

        HttpURLConnection conn = null;
        DataOutputStream dos = null;
        String lineEnd = "\r\n";
        String twoHyphens = "--";
        String boundary = "*****";
        int bytesRead, bytesAvailable, bufferSize;
        byte[] buffer;
        int maxBufferSize = 1 * 1024 * 1024;
        ContextWrapper cw = new ContextWrapper(MesPlaintes.this);
        File directory = cw.getDir("tontine_plaintes", Context.MODE_PRIVATE);
        File sourceFile = new File(directory, plainte.getFile_name());
        String encodedAudio = null;

        if (!sourceFile.isFile()) {
            progressDialog.dismiss();
            Log.e("uploadFile", "Source File not exist :" + audioFilePath);
            // Afficher un msg pour dire que le fichier n'existe pas
            return;
        }

        try {
            FileInputStream fis = new FileInputStream(sourceFile.getPath());
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            byte[] b = new byte[1024];
            for (int readNum; (readNum = fis.read(b)) != -1; ) {
                bos.write(b, 0, readNum);
            }

            encodedAudio = Base64.encodeToString(bos.toByteArray(), Base64.DEFAULT);
        } catch (Exception e) {
            Log.d("mylog", e.toString());
        }

        try {
            JSONObject jsonObject = new JSONObject();
            String imgname = String.valueOf(Calendar.getInstance().getTimeInMillis());
            jsonObject.put("id", plainte.getAuteur());
            jsonObject.put("duration", plainte.getDuration());
            jsonObject.put("audio", encodedAudio);
            jsonObject.put("filename", plainte.getFile_name());
        } catch (JSONException e) {
            Log.e("JSONObject Here", e.toString());
        }

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, url_save_plainte, jsonObject,
                new Response.Listener<JSONObject>() {
                    @SuppressLint("ResourceAsColor")
                    @Override
                    public void onResponse(JSONObject result) {
                        progressDialog.dismiss();
                        Log.e("Response", result.toString());
                        try {
                            if (result.getBoolean("success")) {
                                JSONObject resultat = result.getJSONObject("data");
                                progressDialog.dismiss();
                                Toast.makeText(MesPlaintes.this, "Plainte bien envoyée", Toast.LENGTH_SHORT).show();
                            } else {
                                progressDialog.dismiss();
                                Alerter.create(MesPlaintes.this)
                                        .setTitle(result.getString("message"))
                                        .setIcon(R.drawable.ic_warning)
                                        .setTitleAppearance(R.style.TextAppearance_AppCompat_Large)
                                        .setIconColorFilter(R.color.colorPrimaryDark)
                                        .setBackgroundColorRes(R.color.colorWhite)
                                        .show();
                            }
                        } catch (Throwable t) {
                            Log.e("My App", "Could not parse malformed JSON: \"" + result.toString() + "\"");
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                progressDialog.dismiss();
                androidx.coordinatorlayout.widget.CoordinatorLayout mainLayout = findViewById(R.id.layout_plainte);
                String message;

                if (volleyError instanceof NetworkError || volleyError instanceof AuthFailureError || volleyError instanceof TimeoutError) {
                    message = "Aucune connexion Internet!";
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
                                uploadPlainte(plainte, token);
                            }
                        });
                snackbar.getView().setBackgroundColor(ContextCompat.getColor(MesPlaintes.this, R.color.colorGray));
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
                headers.put("Authorization", "Bearer " + token);
                return headers;
            }
        };

        rQueue = Volley.newRequestQueue(MesPlaintes.this);
        jsonObjectRequest.setRetryPolicy(new DefaultRetryPolicy(
                15000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        rQueue.add(jsonObjectRequest);
    }



//    public void sendRecord(View view)
//    {
//        stopAudio();
//        timer.cancel();
//        Plainte plainte = new Plainte();
//        Utilisateur u = new Utilisateur().getUser(Prefs.getString(TEL_KEY, ""));
//        plainte.setAuteur(u);
//        plainte.setCreation(System.currentTimeMillis() / 1000L);
//        plainte.setDuration(timerLevel);
//        plainte.setFile_name(time_of_audio+"_"+u.getId_utilisateur()+".3gp");
//
//        ContextWrapper cw = new ContextWrapper(MesPlaintes.this);
//        File directory = cw.getDir("tontine_plaintes", Context.MODE_PRIVATE);
//        File mypath=new File(directory,audioFilePath);
//        plainte.setSize(mypath.getTotalSpace());
//        plainte.save();
//
//    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        MesPlaintes.this.finish();
        startActivity(new Intent(MesPlaintes.this, Dashboard.class));
        if(timer != null)
            timer.cancel();
        if (timing != null)
            timing.setText("0:00");
        //supprimer le fichier
        if(mypath != null)
            mypath.delete();
        if(mediaRecorder != null )
        {
            mediaRecorder.stop();
            mediaRecorder.release();
            mediaRecorder = null;
        }

    }

    protected boolean hasMicrophone() {
        PackageManager pmanager = this.getPackageManager();
        return pmanager.hasSystemFeature(
                PackageManager.FEATURE_MICROPHONE);
    }

    private void alertView( String title ,String message ) {
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle( title )
                .setIcon(R.drawable.ic_warning)
                .setMessage(message)
                .setNegativeButton("Non", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialoginterface, int i) {
                        dialoginterface.cancel();
                    }})
                .setPositiveButton("Oui", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialoginterface, int i) {
                        Prefs.putString(ID_UTILISATEUR_KEY,null);
                        MesPlaintes.this.finish();
                        startActivity(new Intent(MesPlaintes.this,Connexion.class));
                    }
                }).show();
    }

    private PermissionService.Callback callback = new PermissionService.Callback() {
        @Override
        public void onRefuse(ArrayList<String> RefusePermissions) {
            Toast.makeText(MesPlaintes.this,
                    "L'application a besoin d'utiliser le micro de votre téléphone afin de pouvoir enrégistrer vos plaintes. Vous seul(e) pouvez déclencher ou non un enrégistrement.",
                    Toast.LENGTH_LONG).show();
            MesPlaintes.this.finish();
        }

        @Override
        public void onFinally() {
            // pass
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (!Settings.canDrawOverlays(MesPlaintes.this)) {
                    Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                            Uri.parse("package:" + MesPlaintes.this.getPackageName()));
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
}