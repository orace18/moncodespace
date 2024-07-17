package com.sicmagroup.tondi;
import static com.sicmagroup.tondi.Connexion.ID_UTILISATEUR_KEY;
import static com.sicmagroup.tondi.Connexion.TEL_KEY;
import static com.sicmagroup.tondi.Connexion.url_save_plainte;
import static com.sicmagroup.tondi.utils.Constantes.REFRESH_TOKEN;
import static com.sicmagroup.tondi.utils.Constantes.TOKEN;
import static com.sicmagroup.tondi.utils.Constantes.url_refresh_token;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

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
import com.sicmagroup.tondi.ui.plaintes.PlaintesViewModel;
import com.sicmagroup.tondi.utils.Constantes;
import com.tapadoo.alerter.Alerter;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;

public class DialogFragment extends androidx.fragment.app.DialogFragment {
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
    File mypath;
    ProgressDialog progressDialog;
    RequestQueue rQueue;
    Button sort_btn;
    Boolean sorted = false;
    private static MediaRecorder mediaRecorder;
    private static MediaPlayer mediaPlayer;
    JSONObject jsonObject;
    private static String audioFilePath;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        progressDialog = new ProgressDialog(getContext());

        View view = inflater.inflate(R.layout.dialog_new_plainte,container,false);
        ContextWrapper cw = new ContextWrapper(getContext());
        // path to /data/data/yourapp/app_data/imageDir
        File directory = cw.getDir("tontine_plaintes", Context.MODE_PRIVATE);
        time_of_audio = System.currentTimeMillis() / 1000L;
        Utilisateur u = new Utilisateur().getUser(Prefs.getString(TEL_KEY, ""));
        mypath=new File(directory,time_of_audio+"_"+ u.getId_utilisateur()+".mp3");
        audioFilePath = mypath.getPath();

        recordButton = (ImageButton) view.findViewById(R.id.btn_record);
        playButton = (ImageButton) view.findViewById(R.id.btn_play);
        closeButton = (ImageButton) view.findViewById(R.id.btn_close);
        sendButton = (ImageButton) view.findViewById(R.id.btn_send);
        timing = (TextView) view.findViewById(R.id.timing);

        progressBar = (ProgressBar) view.findViewById(R.id.progressBar);

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

        playButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                playButton.setEnabled(false);
                recordButton.setEnabled(false);

                mediaPlayer = new MediaPlayer();
                try {
                    mediaPlayer.setDataSource(audioFilePath);
                    mediaPlayer.prepare();
                    mediaPlayer.start();
                } catch (IOException e) {
                    e.printStackTrace();
                }


            }
        });

        recordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
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
                    mediaRecorder.start();
                } catch (Exception e) {
                    e.printStackTrace();
                }


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
        });

        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
                if(timer != null)
                    timer.cancel();
                timing.setText("0:00");
                //supprimer le fichier
                mypath.delete();
                if(mediaRecorder != null)
                {
                    try {
                        mediaRecorder.stop();
                        mediaRecorder.release();
                        mediaRecorder = null;
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
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

                ContextWrapper cw = new ContextWrapper(getContext());
                File directory = cw.getDir("tontine_plaintes", Context.MODE_PRIVATE);
                File mypath=new File(directory,audioFilePath);
                plainte.setSended(true);
                plainte.save();
               Intent intent = new Intent(getContext(),Home.class);
               intent.putExtra(Constantes.BOTTOM_NAV_DESTINATION,Constantes.DESTINATION_PLAINTES);
               startActivity(intent);
                //Afficher sa dans le recyclerView
//                plainteArrayList.add(plainte);
//                plainteAdapter.notifyDataSetChanged();

                //Envoyer les détailles de la plainte et le fichier au serveur
                uploadPlainte(plainte);


                dismiss();
            }
        });
        return view;
    }

    @Override
    public void onCancel(@NonNull DialogInterface dialog) {
        super.onCancel(dialog);
        if(timer != null)
        {
            timer.cancel();

        }
        timing.setText("0:00");
        progressBar.setProgress(0);
        //supprimer le fichier

        mypath.delete();

        if(mediaRecorder != null)
        {
            try {
                mediaRecorder.stop();
                mediaRecorder.release();
                mediaRecorder = null;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    protected boolean hasMicrophone() {
        PackageManager pmanager = this.getActivity().getPackageManager();
        return pmanager.hasSystemFeature(
                PackageManager.FEATURE_MICROPHONE);
    }

//    public void recordAudio(View view) throws IOException
//    {
//        isRecording = true;
//        playButton.setEnabled(false);
//        recordButton.setEnabled(false);
//        sendButton.setEnabled(true);
//
//        try {
//            mediaRecorder = new MediaRecorder();
//            mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
//            mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
//            mediaRecorder.setOutputFile(audioFilePath);
//            mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
//            mediaRecorder.prepare();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//
//        mediaRecorder.start();
//        timer = new CountDownTimer(120000, 1000) {
//
//            public void onTick(long millisUntilFinished) {
//                if(millisUntilFinished / 1000 >= 60 && millisUntilFinished / 1000 < 120)
//                    timing.setText("1:" + (millisUntilFinished / 1000 - 60) );
//                else if(millisUntilFinished / 1000 == 120)
//                    timing.setText("2:" + (millisUntilFinished / 1000 - 120) );
//                else
//                    timing.setText("0:" + millisUntilFinished / 1000 );
//                timerLevel = millisUntilFinished / 1000;
//                progressBar.setProgress(120 - (int)millisUntilFinished / 1000);
//            }
//
//            public void onFinish() {
//                stopAudio();
//            }
//
//        }.start();
//
//
//    }

    public void stopAudio ()
    {
        playButton.setEnabled(true);
        if (isRecording)
        {

            recordButton.setEnabled(false);
            try {
                mediaRecorder.stop();
                mediaRecorder.release();
                mediaRecorder = null;
                isRecording = false;
            } catch (Exception e) {
                e.printStackTrace();
            }

        } else {
            try {
                mediaPlayer.release();
            } catch (Exception e) {
                e.printStackTrace();
            }
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

    public void uploadPlainte(Plainte plainte) {
        ContextWrapper cw = new ContextWrapper(getContext());
        File directory = cw.getDir("tontine_plaintes", Context.MODE_PRIVATE);
        File sourceFile = new File(directory, plainte.getFile_name());
        if (!sourceFile.isFile()) {
            progressDialog.dismiss();
            Log.e("uploadFile", "Source File not exist :" + sourceFile.getAbsolutePath());
            // Afficher un msg pour dire que le fichier n'existe pas
        } else {
            OkHttpClient okHttpClient = new OkHttpClient();

            // Créez l'objet JSON
            JSONObject jsonObject = new JSONObject();
            try {
                JSONObject dataObject = new JSONObject();
                dataObject.put("audio", sourceFile.getName());
                dataObject.put("duration", plainte.getDuration());

                jsonObject.put("data", dataObject);
                jsonObject.put("audio", sourceFile.getName());
            } catch (JSONException e) {
                e.printStackTrace();
                return;
            }

            // Créez le RequestBody multipart/form-data
            RequestBody body = new MultipartBody.Builder().setType(MultipartBody.FORM)
                    .addFormDataPart("file", sourceFile.getName(), RequestBody.create(MediaType.parse("multipart/form-data"), sourceFile))
                    .addFormDataPart("data", jsonObject.toString())
                    .build();

            FutureTask<Boolean> task = new FutureTask<>(() -> {
                try {
                    okhttp3.Response response = okHttpClient.newCall(
                            new okhttp3.Request.Builder()
                                    .url(Constantes.URL_PLAINTE_NEW)
                                    .addHeader("Authorization", "Bearer " + Prefs.getString(TOKEN, ""))
                                    .post(body)
                                    .build()
                    ).execute();

                    if (response.isSuccessful()) {
                        ResponseBody responseBody = response.body();
                        if (responseBody != null) {
                            String responseString = responseBody.string();
                            JSONObject responseObject = new JSONObject(responseString);
                            Log.e("La réponse de envoie de plainte", responseObject.get("responseCode").toString());

                            int responseCode = responseObject.getInt("responseCode");
                            if (responseCode == 0) {
                                progressDialog.dismiss();
                                return true;
                            } else {
                                return false;
                            }
                        } else {
                            return false;
                        }
                    } else if (response.code() == 401) {
                        refreshAccessToken(getContext(), refreshed -> {
                            if (refreshed) {
                                uploadPlainte(plainte);
                            } else {
                                progressDialog.dismiss();
                                Toast.makeText(getContext(), "Token non actualisé, plainte non envoyée", Toast.LENGTH_SHORT).show();
                            }
                        });
                        return false;
                    } else {
                        return false;
                    }
                } catch (IOException e) {
                    return false;
                }
            });

            try {
                new Thread(task).start();
                task.get();
            } catch (ExecutionException | InterruptedException e) {
                Toast.makeText(getContext(), "Plainte non-envoyée, une erreur est survenue", Toast.LENGTH_SHORT).show();
                Log.e("task", "intask else");
                progressDialog.dismiss();
                e.printStackTrace();
            }
        }
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
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            Log.e("La réponse du refresh token est:", response.toString());
                            String newAccessToken = response.getString("token");
                            String newRefreshToken = response.getString("refreshToken");
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

}

