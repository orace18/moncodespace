package com.sicmagroup.tondi;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;
import com.orm.SugarRecord;
import com.orm.query.Condition;
import com.orm.query.Select;
import com.pixplicity.easyprefs.library.Prefs;
import com.sicmagroup.tondi.utils.Constantes;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.sicmagroup.tondi.Accueil.MOOV_DATA_SHARING;
import static com.sicmagroup.tondi.Connexion.CONNECTER_KEY;
import static com.sicmagroup.tondi.Connexion.FIREBASE_TOKEN;
import static com.sicmagroup.tondi.Connexion.ID_UTILISATEUR_KEY;
import static com.sicmagroup.tondi.Connexion.NOM_KEY;
import static com.sicmagroup.tondi.Connexion.NUMERO_COMPTE_KEY;
import static com.sicmagroup.tondi.Connexion.PASS_KEY;
import static com.sicmagroup.tondi.Connexion.PHOTO_CNI_KEY;
import static com.sicmagroup.tondi.Connexion.PHOTO_KEY;
import static com.sicmagroup.tondi.Connexion.PIN_KEY;
import static com.sicmagroup.tondi.Connexion.PRENOMS_KEY;
import static com.sicmagroup.tondi.Connexion.SEXE_KEY;
import static com.sicmagroup.tondi.utils.Constantes.BOTTOM_NAV_DESTINATION;
import static com.sicmagroup.tondi.utils.Constantes.CODE_MARCHAND_KEY;
import static com.sicmagroup.tondi.utils.Constantes.REFRESH_TOKEN;
import static com.sicmagroup.tondi.utils.Constantes.SERVEUR;
import static com.sicmagroup.tondi.utils.Constantes.STATUT_UTILISATEUR;
import static com.sicmagroup.tondi.Connexion.TEL_KEY;
import static com.sicmagroup.tondi.utils.Constantes.TOKEN;
import static com.sicmagroup.tondi.utils.Constantes.accessToken;
import static com.sicmagroup.tondi.utils.Constantes.url_refresh_token;

public class Utilitaire {
    String medias_url = SERVEUR + "/medias/";
    String url_synchro = SERVEUR + "/api/v1/synchronisation/enregistrer";
    String url_update_databases = SERVEUR+"/api/v1/version/print_update";
    String url_refresh_databse = SERVEUR+"/api/v1/version/all_data_user";
    String url_update_data_update_table = SERVEUR+"/api/v1/version/validate_data_update";
    String url_get_statut_utilisateur = SERVEUR + "/api/v1/synchronisation/retournerStatut";
    private Context context;
    ProgressDialog progressDialog;

    public Utilitaire(Context context) {
        this.context=context;

        new Prefs.Builder()
                .setContext(this.context)
                .setMode(ContextWrapper.MODE_PRIVATE)
                .setPrefsName(this.context.getPackageName())
                .setUseDefaultSharedPreference(true)
                .build();
    }



    public boolean isConnected(){
        boolean connected = false;
        ConnectivityManager connectivityManager = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if(connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.CONNECTED ||
                connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() == NetworkInfo.State.CONNECTED) {
            //we are connected to a network
            connected = true;
        }
        return connected;
    }

    public void saveImageUrl(URL url, String name) throws IOException {
        Log.d("getExternalInfo", name);
        try  {
            //The sdcard directory e.g. '/sdcard' can be used directly, or
            //more safely abstracted with getExternalStorageDirectory()
            //Log.d("getExternalInfo", "jojo");
            InputStream input = url.openStream();
            File storagePath = Environment.getExternalStorageDirectory();
            ContextWrapper cw = new ContextWrapper(context);
            File directory = cw.getDir("tontine_photos", Context.MODE_PRIVATE);
            // Create imageDir
            File mypath=new File(directory,name+".png");

            try {
                OutputStream output = new FileOutputStream(mypath);
                byte[] buffer = new byte[20];
                int bytesRead = 0;
                while ((bytesRead = input.read(buffer, 0, buffer.length)) >= 0) {
                    output.write(buffer, 0, bytesRead);
                }
            }catch (IOException e) {
                e.printStackTrace();
                Log.d("getExternalInfo", "exeption1"+e.getMessage());

            }
        } catch (IOException e) {
            e.printStackTrace();
            Log.d("getExternalInfo", "exeption2"+e.getMessage());

        }

    }

    public static Bitmap getBitmapFromURL(String src) {
        try {
            URL url = new URL(src);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoInput(true);
            connection.connect();
            InputStream input = connection.getInputStream();
            return BitmapFactory.decodeStream(input);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }



    public void saveToInternalStorage(Bitmap bitmapImage, String name){
        ContextWrapper cw = new ContextWrapper(context);
        // path to /data/data/yourapp/app_data/imageDir
        File directory = cw.getDir("tontine_photos", Context.MODE_PRIVATE);
        // Create imageDir
        File mypath=new File(directory,name+".png");
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(mypath);
            // Use the compress method on the BitMap object to write image to the OutputStream
            bitmapImage.compress(Bitmap.CompressFormat.PNG, 100, fos);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                fos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        //return directory.getAbsolutePath();
    }

    public void loadImageFromStorage(String name, View v)
    {

        try {
            ContextWrapper cw = new ContextWrapper(context);
            // path to /data/data/yourapp/app_data/imageDir
            File directory = cw.getDir("tontine_photos", Context.MODE_PRIVATE);
            // Create imageDir
            File f=new File(directory,name+".png");
            //File f=new File(path, name+".JPG");
            Bitmap b = BitmapFactory.decodeStream(new FileInputStream(f));
            ImageView img=v.findViewById(R.id.user_avatar);
            img.setImageBitmap(b);
        }
        catch (FileNotFoundException e)
        {
            e.printStackTrace();
        }

    }

    public String getOperatorByNumber(String number) {
        String operator_name = "";
        String[] mtn_prefix_list = context.getResources().getStringArray(R.array.mtn_prefix_list);
        String[] moov_prefix_list = context.getResources().getStringArray(R.array.moov_prefix_list);
        if (Arrays.asList(mtn_prefix_list).contains(""+number.charAt(0)+number.charAt(1))) {
            operator_name = "MTN";
        }
        if (Arrays.asList(moov_prefix_list).contains(""+number.charAt(0)+number.charAt(1))) {
            operator_name = "MOOV";
        }
        return operator_name;
    }

    //envoie les données vers le serveur
    public void synchroniser_en_ligne() {
        // récupérer les lignes de synchronisation dont le statut est égal à 0
        List<Synchronisation> synchronisations = Select.from(Synchronisation.class)
                .where(Condition.prop("statut").eq(0))
                .list();
        //Log.e("Response__PPP", "1");
        // parcourir les lignes à synchroniser
        //send data to server
        for (Synchronisation synchronisation:synchronisations)
        {
            //  envoyer en  ligne
            envoyer_en_ligne(synchronisation);
        }

        //get value from server
        if (Prefs.contains(ID_UTILISATEUR_KEY) && Prefs.getString(ID_UTILISATEUR_KEY, null) != null) {
            recevoir_statut_utilisateur();
        }

        //method to update databse. used by jobschelduler
        getUpdateDatabases();
    }


    class DownloadTask extends AsyncTask<URL,Void,Bitmap> {
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

                /*
                    BufferedInputStream
                        A BufferedInputStream adds functionality to another input stream-namely,
                        the ability to buffer the input and to support the mark and reset methods.
                */
                /*
                    BufferedInputStream(InputStream in)
                        Creates a BufferedInputStream and saves its argument,
                        the input stream in, for later use.
                */
                // Initialize a new BufferedInputStream from InputStream
                BufferedInputStream bufferedInputStream = new BufferedInputStream(inputStream);

                /*
                    decodeStream
                        Bitmap decodeStream (InputStream is)
                            Decode an input stream into a bitmap. If the input stream is null, or
                            cannot be used to decode a bitmap, the function returns null. The stream's
                            position will be where ever it was after the encoded data was read.

                        Parameters
                            is InputStream : The input stream that holds the raw data
                                              to be decoded into a bitmap.
                        Returns
                            Bitmap : The decoded bitmap, or null if the image data could not be decoded.
                */
                // Convert BufferedInputStream to Bitmap object
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
                saveToInternalStorage(result,Prefs.getString(PHOTO_KEY, null));

            }
        }
    }

    class DownloadTaskCNI extends AsyncTask<URL,Void,Bitmap> {
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

                /*
                    BufferedInputStream
                        A BufferedInputStream adds functionality to another input stream-namely,
                        the ability to buffer the input and to support the mark and reset methods.
                */
                /*
                    BufferedInputStream(InputStream in)
                        Creates a BufferedInputStream and saves its argument,
                        the input stream in, for later use.
                */
                // Initialize a new BufferedInputStream from InputStream
                BufferedInputStream bufferedInputStream = new BufferedInputStream(inputStream);

                /*
                    decodeStream
                        Bitmap decodeStream (InputStream is)
                            Decode an input stream into a bitmap. If the input stream is null, or
                            cannot be used to decode a bitmap, the function returns null. The stream's
                            position will be where ever it was after the encoded data was read.

                        Parameters
                            is InputStream : The input stream that holds the raw data
                                              to be decoded into a bitmap.
                        Returns
                            Bitmap : The decoded bitmap, or null if the image data could not be decoded.
                */
                // Convert BufferedInputStream to Bitmap object
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
                saveToInternalStorage(result,Prefs.getString(PHOTO_CNI_KEY, null));

            }
        }
    }
    //Methode pour récupérer le statut de l'utilisateur
    public void recevoir_statut_utilisateur()
    {
        RequestQueue queue = Volley.newRequestQueue(context);
       // Log.e("statut_test", String.valueOf(Prefs.getString(STATUT_UTILISATEUR, null)));
        String token = Prefs.getString(TOKEN, "");
        Log.e("Le token est:", token);

        StringRequest postRequest = new StringRequest(Request.Method.POST, url_get_statut_utilisateur,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.d("stautFuncB",  response);

                        try {
                            JSONObject result = new JSONObject(response);

                            if (result.getBoolean("success"))
                            {
                                Prefs.putString(STATUT_UTILISATEUR, result.getString("data"));
                                Utilisateur user = new Utilisateur().getUser( Prefs.getString(TEL_KEY, null));
                                user.setStatut(result.getString("data"));
                                //Toast.makeText(context, user.getStatut(), Toast.LENGTH_LONG).show();
                                user.save();
//                                Log.e("stat success",  result.getString("data"));
                            }
                            else
                            {
//                                Log.e("stautFunc",  String.valueOf(result.getBoolean("success")));
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
//                            Log.e("stautFunc",  "catch state");

                        }

                    }
                },
                new Response.ErrorListener()
                {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        if (error.networkResponse != null && error.networkResponse.statusCode == 401) {
                            refreshAccessToken(new TokenRefreshListener() {
                                @Override
                                public void onTokenRefreshed(boolean success) {
                                    if (success) {
                                        recevoir_statut_utilisateur();
                                    }
                                }
                            });
                        } else {
                            Log.e("Error.Synchronisation", String.valueOf(error.getMessage()));
                        }
                    }
                }
        ) {
            @Override
            protected Map<String, String> getParams()
            {
                Gson gson = new Gson();
                Map<String, String>  params = new HashMap<String, String>();
                params.put("donnees", Prefs.getString(TEL_KEY, null));
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

    public void envoyer_en_ligne(final Synchronisation synchronisation) {
        //  envoyer en  ligne
        RequestQueue queue = Volley.newRequestQueue(context);

        StringRequest postRequest = new StringRequest(Request.Method.POST, url_synchro,
                new Response.Listener<String>()
                {
                    @SuppressLint("ResourceAsColor")
                    @Override
                    public void onResponse(String response) {
                        //progressDialog.dismiss();
                        // response
                        Log.d("Response__PPP", response);
                        synchronisation.setStatut(1);
                        synchronisation.save();

                    }
                },
                new Response.ErrorListener()
                {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        //progressDialog.dismiss();
                        // error
                        if (error.networkResponse != null && error.networkResponse.statusCode == 401) {
                            refreshAccessToken(new TokenRefreshListener() {
                                @Override
                                public void onTokenRefreshed(boolean success) {
                                    if (success) {
                                        envoyer_en_ligne(synchronisation);
                                    }
                                }
                            });
                        } else {
                            Log.e("Error.Synchronisation", String.valueOf(error.getMessage()));
                        }
                    }
                }
        ) {
            @Override
            protected Map<String, String> getParams()
            {
                Gson gson = new Gson();

                Map<String, String>  params = new HashMap<String, String>();
                params.put("donnees", synchronisation.getDonnees());

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

        //initialize the progress dialog and show it
        progressDialog = new ProgressDialog(context);
        progressDialog.setCancelable(true);
        progressDialog.setMessage("Veuillez patienter SVP! synchronisation en cours...");
        //progressDialog.show();

    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public static void scheduleJob(Context context){
        ComponentName serviceComponent = new ComponentName(context, TondiJobService.class);
        JobInfo.Builder builder= new JobInfo.Builder(123, serviceComponent);
        builder.setPersisted(true);
        builder.setPeriodic(15 * 60 * 1000);


        JobScheduler jobScheduler = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            jobScheduler = context.getSystemService(JobScheduler.class);
            int resultCode = jobScheduler.schedule(builder.build());
            if(resultCode == JobScheduler.RESULT_SUCCESS)
                Log.d("jobScheduler", "Job scheduled");
            else
                Log.d("jobScheduler", "Job schedulling failed");

        }




    }

    public void getUpdateDatabases() {
        sendUpdateDatabasesRequest();
    }

    private void sendUpdateDatabasesRequest() {
        RequestQueue queue = Volley.newRequestQueue(context);
        Log.e("updateDatabse", "job is working");

        JSONObject params = new JSONObject();
        try {
            params.put("customerNumber", Prefs.getString(TEL_KEY, null));
            Log.e("La requete est:", params.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        JsonObjectRequest postRequest = new JsonObjectRequest(Request.Method.POST, Constantes.URL_FREQ_UPDATE_DB, params,
                new Response.Listener<JSONObject>() {
                    @SuppressLint({"ResourceAsColor", "LongLogTag"})
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.e("onResponse: ", response.toString());
                        if (Prefs.getString(NUMERO_COMPTE_KEY, null) != null) {
                            try {
                                JSONObject result = response;
                                if (result.getInt("responseCode") == 0) {
                                    // Récupérer l'id de la table data_update
                                    Log.e("La reponse est sendUpdateDatabasesRequest", result.toString());
                                    int id_data_update = result.getJSONObject("body").getInt("data_update_id");
                                                // Mettre à jour les informations de l'utilisateur
                                    Utilisateur nouvel_utilisateur = null;
                                    if (result.getJSONObject("body").getBoolean("users")) {
                                        JSONObject user = result.getJSONObject("body").getJSONObject("users");
                                        Long id = Long.valueOf(0);
                                        @SuppressLint("SimpleDateFormat") DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                                        Date creation = (Date) formatter.parse(user.getString("createdAt"));
                                        long output_creation = creation.getTime() / 1000L;
                                        String str_creation = Long.toString(output_creation);
                                        long timestamp_creation = Long.parseLong(str_creation) * 1000;
                                        Date maj = (Date) formatter.parse(user.getString("updatedAt"));
                                        long output_maj = maj.getTime() / 1000L;
                                        String str_maj = Long.toString(output_maj);
                                        long timestamp_maj = Long.parseLong(str_maj) * 1000;
                                        // enregistrer en local
                                        List<Utilisateur> userH = SugarRecord.find(Utilisateur.class, "numero = ?", user.getString("numero"));
                                        if (userH.size() != 0) {
                                            nouvel_utilisateur = userH.get(0);
                                            nouvel_utilisateur.setId_utilisateur(user.getString("id"));
                                            nouvel_utilisateur.setNumero(user.getString("numero"));
                                            nouvel_utilisateur.setNom(user.getString("firstName"));
                                            nouvel_utilisateur.setPrenoms(user.getString("lastName"));
                                            nouvel_utilisateur.setPhoto_identite(user.getString("profilePicture"));
                                            nouvel_utilisateur.setcni_photo(user.getString("cniPicture"));
                                            nouvel_utilisateur.setMdp(user.getString("password"));
                                            nouvel_utilisateur.setStatut(user.getString("statut"));
                                            nouvel_utilisateur.setNumero_compte(user.getString("accountNumber"));
                                            if(result.isNull("referent"))
                                                nouvel_utilisateur.setCodeMarchand("");
                                            else
                                                nouvel_utilisateur.setCodeMarchand(result.getString("referent"));
                                            nouvel_utilisateur.setMaj(timestamp_maj);
                                            nouvel_utilisateur.save();
                                            id = Long.parseLong(nouvel_utilisateur.getId_utilisateur());
                                        }
                                        //Log.d("nouvel_utilisateur_id", "id:"+String.valueOf(id));
                                        if (id != null) {
                                            // Mettre à jour la préférence id utilisateur
                                            Prefs.putString(ID_UTILISATEUR_KEY, String.valueOf(id));
                                            // Mettre à jour la préférence nom
                                            Prefs.putString(NOM_KEY, user.getString("firstName"));
                                            // Mettre à jour la préférence prenoms
                                            Prefs.putString(PRENOMS_KEY, user.getString("lastName"));
                                            // Mettre à jour la préférence pin d'accès
                                            // Prefs.putString(PIN_KEY, user.getString("pin_acces"));
                                            // Mettre à jour la préférence pin d'accès
                                            Prefs.putString(PASS_KEY, user.getString("password"));
                                            Prefs.putBoolean(MOOV_DATA_SHARING, true);
                                            // Mettre à jour la préférence statut
                                            Prefs.putString(STATUT_UTILISATEUR, user.getString("statut"));
                                            //Mettre à jour la preference photo de la CNI
                                            Prefs.putString(PHOTO_CNI_KEY, user.getString("cniPicture"));
                                            //Mettre à jour la preference firebase token
//                                    Prefs.putString(FIREBASE_TOKEN, user.getString("firebase_token"));
                                            //Mettre à jour la preference sexe
                                            Prefs.putString(SEXE_KEY, user.getString("sexe"));
                                            //Mettre à jour la préference numero compte
                                            Prefs.putString(NUMERO_COMPTE_KEY, user.getString("accountNumber"));
                                            //Mettre à Jour le code marchand
                                            if(user.isNull("referent"))
                                                Prefs.putString(CODE_MARCHAND_KEY, "");
                                            else
                                                Prefs.putString(CODE_MARCHAND_KEY, user.getString("referent"));
                                            // Mettre à jour la préférence pin d'accès
                                            Date currentTime = Calendar.getInstance().getTime();
                                            long output_current = currentTime.getTime() / 1000L;
                                            String str_current = Long.toString(output_current);
                                            long timestamp_current = Long.parseLong(str_current) * 1000;
                                            Prefs.putString(CONNECTER_KEY, String.valueOf(timestamp_current));
                                            // Mettre à jour la préférence pin d'accès
                                            Prefs.putString(PHOTO_KEY, user.getString("profilePicture"));

                                            // recuperation de la photo ditentie
                                            // recuperer l'url de l'image
                                            /**/
                                            /**/URL url_photo  = new URL(Constantes.URL_MEDIA_PP + Prefs.getString(PHOTO_KEY, null) + ".png");
//                                          String src = Constantes.URL_MEDIA + Prefs.getString(PHOTO_KEY, null) + ".JPG";
                                            URL url_photo_cni = new URL(Constantes.URL_MEDIA_CNI + Prefs.getString(PHOTO_CNI_KEY, null) + ".png");

                                            //Bitmap bitmap_photo =Utilitaire.getBitmapFromURL(src);

                                            Prefs.putString(TEL_KEY, String.valueOf(user.getString("numero")));

                                            new DownloadTask().execute(url_photo);
                                            new DownloadTaskCNI().execute(url_photo_cni);
                                            // enrgeistrer les tontines en local
                                            //e("user_data", result.get("user_data")+"");
                                        }

                                    }


                                    JSONArray tontines = result.getJSONObject("body").getJSONArray("tontines");
                                    // Verifier si api a envoyé des informations à mettre a jour concernant la table Tontines
                                    if (tontines.length() > 0) {
                                        Log.d("tontines_trouve", "oui:" + tontines.length());
                                        List<Tontine> existeTontines = null;
                                        List<Versement> existeVersement = null;
                                        List<Retrait> existeRetrait = null;

                                        for (int i = 0; i < tontines.length(); i++) {
                                            Tontine nouvelle_tontine = new Tontine();

                                            existeTontines = SugarRecord.find(Tontine.class, "id_server = ?", tontines.getJSONObject(i).getString("id"));
                                            if (existeTontines.size() == 0) {
                                                //nouvelle_tontine.setId(Long.valueOf(tontines.getJSONObject(i).getString("id")));
                                                nouvelle_tontine.setId_utilisateur(Prefs.getString(ID_UTILISATEUR_KEY, null));
                                                nouvelle_tontine.setPeriode(tontines.getJSONObject(i).getString("periode"));
                                                nouvelle_tontine.setMise(Integer.parseInt(tontines.getJSONObject(i).getString("mise")));
                                                nouvelle_tontine.setPrelevement_auto(tontines.getJSONObject(i).getBoolean("isAutoPayment"));
//                                                nouvelle_tontine.setIdSim(String.valueOf(sim_par_defaut.getId()));
                                                nouvelle_tontine.setCarnet(tontines.getJSONObject(i).getString("carnet"));
                                                nouvelle_tontine.setContinuer(Long.valueOf(tontines.getJSONObject(i).getString("carnet")));
                                                nouvelle_tontine.setStatut(tontines.getJSONObject(i).getString("state"));
                                                nouvelle_tontine.setId_server(tontines.getJSONObject(i).getString("id"));
                                                if (!tontines.getJSONObject(i).isNull("unBlockDate")) {
                                                    nouvelle_tontine.setDateDeblocage(tontines.getJSONObject(i).getString("unBlockDate"));
                                                }
                                                if (tontines.getJSONObject(i).getString("denomination") == "")
                                                    nouvelle_tontine.setDenomination("Ma tontine " + nouvelle_tontine.getCarnet());
                                                else
                                                    nouvelle_tontine.setDenomination(tontines.getJSONObject(i).getString("denomination"));
                                                // maj des dates
                                                @SuppressLint("SimpleDateFormat") DateFormat formatter_1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                                                Date creation_1 = formatter_1.parse(tontines.getJSONObject(i).getString("createdAt"));
                                                long output_creation_1 = creation_1.getTime() / 1000L;
                                                String str_creation_1 = Long.toString(output_creation_1);
                                                long timestamp_creation_1 = Long.parseLong(str_creation_1) * 1000;
                                                nouvelle_tontine.setCreation(timestamp_creation_1);
                                                Date creation_2 = formatter_1.parse(tontines.getJSONObject(i).getString("updatedAt"));
                                                long output_creation_2 = creation_2.getTime() / 1000L;
                                                String str_creation_2 = Long.toString(output_creation_2);
                                                long timestamp_maj_1 = Long.parseLong(str_creation_2) * 1000;
                                                nouvelle_tontine.setMaj(timestamp_maj_1);
                                                Log.e("connexion", "5connexion trying");

                                                nouvelle_tontine.save();
                                            } else {
                                                nouvelle_tontine = existeTontines.get(0);
                                                nouvelle_tontine.setContinuer(Long.valueOf(tontines.getJSONObject(i).getString("carnet")));
                                                nouvelle_tontine.setStatut(tontines.getJSONObject(i).getString("state"));
                                                @SuppressLint("SimpleDateFormat") DateFormat formatter_1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                                                Date creation_2 = formatter_1.parse(tontines.getJSONObject(i).getString("updatedAt"));
                                                long output_creation_2 = creation_2.getTime() / 1000L;
                                                String str_creation_2 = Long.toString(output_creation_2);
                                                long timestamp_maj_1 = Long.parseLong(str_creation_2) * 1000;
                                                nouvelle_tontine.setMaj(timestamp_maj_1);
                                                Log.e("connexion", "5connexion trying");

                                                nouvelle_tontine.save();
                                            }

                                            // enrgeistrer les versements en local

                                            JSONArray versements = tontines.getJSONObject(i).getJSONArray("versements");
                                            Log.d("versement", versements.toString());
                                            if (versements.length() > 0) {
                                                Versement versement = new Versement();
                                                Log.d("versements_trouve", "oui:" + versements.length() + " carnet: " + nouvelle_tontine.getCarnet());
                                                for (int j = 0; j < versements.length(); j++) {

                                                    existeVersement = SugarRecord.find(Versement.class, "id_vers_serv = ?", versements.getJSONObject(j).getString("id"));

                                                    if (existeVersement.size() == 0) {
                                                        Log.d("versements_trouve", "for:" + j + " carnet: " + nouvelle_tontine.getCarnet());
                                                        versement.setMontant(versements.getJSONObject(j).getString("amount"));
                                                        versement.setStatut_paiement(versements.getJSONObject(j).getBoolean("isValide"));
                                                        versement.setFractionne(versements.getJSONObject(j).getString("isFractioned"));
                                                        versement.setIdVersement(versements.getJSONObject(j).getString("idVersement"));
                                                        versement.setIdVersServ(versements.getJSONObject(j).getString("id"));
                                                        Log.d("versements_trouve", "idServeur1:" + versements.getJSONObject(j).getString("id"));
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
                                                        Log.d("versements_trouve", "idServeur2-1 : " + versement.getIdVersServ());
                                                    }
                                                    Log.d("versements_trouve", "idServeur2-2 : " + versement.getIdVersServ());
                                                    if (versements.getJSONObject(j).getString("isFractioned").equals("false")) {
                                                        List<Versement> versementList = Select.from(Versement.class)
                                                                .where(Condition.prop("id_vers_serv").eq(versements.getJSONObject(j).getString("idVersement")))
                                                                .orderBy("id desc")
                                                                .list();
                                                        //Versement v = SugarRecord.findById(Versement.class, Long.valueOf(versements.getJSONObject(j).getString("id_versement")));
                                                        if (versementList.size() > 0) {
                                                            versement.setIdVersement(String.valueOf(versementList.get(0).getId()));
                                                            versement.save();
                                                        }

                                                    }
                                                }
                                            }


                                            Log.e("connexion", tontines.getJSONObject(i).get("withdraw") + "");
                                            // enrgeistrer les retraits  en local
                                            if(tontines.getJSONObject(i).has("withdraw") && !tontines.getJSONObject(i).isNull("withdraw")) {
                                                JSONObject retraits = tontines.getJSONObject(i).getJSONObject("withdraw");

                                                existeRetrait = SugarRecord.find(Retrait.class, "token = ?", retraits.getString("token"));
                                                Retrait retrait = new Retrait();
                                                if (existeRetrait.size() == 0) {

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
                                                } else {
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
                                    JSONArray histories = result.getJSONObject("body").getJSONArray("histories");
                                    // Verifier si api a envoyé des informations à mettre a jour concernant la table Histories
                                    if (histories.length() > 0) {
                                        Log.d("histories_trouve", "oui:" + histories.length());
                                        List<History> existeHistories = null;
                                        List<Versement> existeVersement = null;
                                        List<Retrait> existeRetrait = null;

                                        for (int i = 0; i < histories.length(); i++) {
                                            History nouvelle_history = new History();

                                            existeHistories = SugarRecord.find(History.class, "id_server = ?", histories.getJSONObject(i).getString("id"));
                                            if (existeHistories.size() == 0) {
                                                nouvelle_history.setContenu(histories.getJSONObject(i).getString("content"));
                                                nouvelle_history.setTitre(histories.getJSONObject(i).getString("title"));
                                                nouvelle_history.setEtat(histories.getJSONObject(i).getBoolean("state") ? 1 : 0);
                                                nouvelle_history.setUser(nouvel_utilisateur);
//                                               nouvelle_history.setNumero(Integer.parseInt(histories.getJSONObject(i).getString("numero_client")));
                                                nouvelle_history.setId_server(histories.getJSONObject(i).getString("id"));

                                                @SuppressLint("SimpleDateFormat")
                                                DateFormat formatter_1 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
                                                Date creation_1 = formatter_1.parse(histories.getJSONObject(i).getString("createdAt"));
                                                long output_creation_1 = creation_1.getTime() / 1000L;
                                                String str_creation_1 = Long.toString(output_creation_1);
                                                long timestamp_creation_1 = Long.parseLong(str_creation_1) * 1000;
                                                nouvelle_history.setCreation(timestamp_creation_1);
                                                nouvelle_history.save();
                                            }
                                        }
                                    }
                                    // Envoyer une requête pour mettre à jour l'état de la table update_data
                                    sendValidateUpdateRequest(id_data_update);
                                } else {
                                    if (result.has("body")) {
                                        String msg = result.getString("body");
                                        Log.e("Mauvais message: ", msg);
                                    } else {
                                        String msg = "Vos identifiants sont incorrects. Veuillez réessayer SVP! ";
                                        Log.e("Mauvais Message: ", msg);
                                    }
                                }
                            } catch (Throwable t) {
                                Log.d("errornscription", String.valueOf(t.getCause()));
                                Log.e("Update Data", t.getMessage());
                            }
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        if (error.networkResponse != null && error.networkResponse.statusCode == 401) {
                            refreshAccessToken(new TokenRefreshListener() {
                                @Override
                                public void onTokenRefreshed(boolean success) {
                                    if (success) {
                                        sendUpdateDatabasesRequest();
                                    }
                                }
                            });
                        } else {
                            Log.e("Error.Synchronisation", String.valueOf(error.getMessage()));
                        }
                    }
                }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                headers.put("Content-Type", "application/json");
                headers.put("Authorization", "Bearer " + Prefs.getString(TOKEN, ""));
                return headers;
            }
        };

        postRequest.setRetryPolicy(new DefaultRetryPolicy(
                30000,
                -1,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        queue.add(postRequest);
    }

    private void sendValidateUpdateRequest(int id_data_update) {
        RequestQueue queue = Volley.newRequestQueue(context);

        JSONObject params = new JSONObject();
        try {
            params.put("customerNumber", Prefs.getString(TEL_KEY, null));
            params.put("id", id_data_update);
            params.put("state", "true");
            Log.e("Le body de la requete", params.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        JsonObjectRequest postRequest = new JsonObjectRequest(Request.Method.POST, Constantes.URL_VALIDATE_FREQ_UPDATE_DB, params,
                new Response.Listener<JSONObject>() {
                    @SuppressLint("ResourceAsColor")
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.e("onResponse: ", response.toString());
                        try {
                            if (response.getBoolean("success")) {
                                Log.d("UpdateJob", "success de mise à jour de la table des mise à jour");
                            } else {
                                Log.d("UpdateJob", "echec de mise à jour de la table des mise à jour");
                            }
                        } catch (JSONException e) {
                            Log.e("UpdateJob", "echec de la mise à Jour de l'etat de la mise à jour");
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        if (error.networkResponse != null && error.networkResponse.statusCode == 401) {
                            refreshAccessToken(new TokenRefreshListener() {
                                @Override
                                public void onTokenRefreshed(boolean success) {
                                    if (success) {
                                        sendUpdateDatabasesRequest();
                                    }
                                }
                            });
                        } else {
                            Log.e("Error.Synchronisation", String.valueOf(error.getMessage()));
                        }
                    }
                }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                headers.put("Content-Type", "application/json");
                headers.put("Authorization", "Bearer " + accessToken);
                return headers;
            }
        };

        postRequest.setRetryPolicy(new DefaultRetryPolicy(
                30000,
                -1,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        queue.add(postRequest);
    }

    private void refreshAccessToken(TokenRefreshListener listener) {
        RequestQueue queue = Volley.newRequestQueue(context);

        JSONObject params = new JSONObject();
        try {
            params.put("refreshToken", Prefs.getString(REFRESH_TOKEN, ""));
            Log.e("Le body de la requete:", params.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        JsonObjectRequest refreshRequest = new JsonObjectRequest(Request.Method.POST, url_refresh_token, params,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
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
                });

        queue.add(refreshRequest);
    }

    private interface TokenRefreshListener {
        void onTokenRefreshed(boolean success);
    }



//    public void getUpdateDatabases() {
//        RequestQueue queue = Volley.newRequestQueue(context);
//        Log.e("updateDatabse", "job is working");
//        StringRequest postRequest = new StringRequest(Request.Method.POST, Constantes.URL_FREQ_UPDATE_DB,
//                new Response.Listener<String>()
//                {
//                    @SuppressLint("ResourceAsColor")
//                    @Override
//                    public void onResponse(String response) {
//                        Log.e("onResponse: ", response);
//                       if (Prefs.getString(NUMERO_COMPTE_KEY,null) != null) {
//                           try {
//
//                               JSONObject result = new JSONObject(response);
//                               if (result.getInt("responseCode") == 0) {
//                                   //recuperer l'id de la table data_update
//                                   int id_data_update = result.getJSONObject("body").getInt("data_update_id");
//                                   // Verifier si api a envoyé des informations à mettre a jour concernant la table User
//                                   Utilisateur nouvel_utilisateur = null;
//                                   if (result.getJSONObject("body").getBoolean("users")) {
//                                       JSONObject user = result.getJSONObject("body").getJSONObject("users");
//                                       Long id = Long.valueOf(0);
//                                       @SuppressLint("SimpleDateFormat") DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
//                                       Date creation = (Date) formatter.parse(user.getString("createdAt"));
//                                       long output_creation = creation.getTime() / 1000L;
//                                       String str_creation = Long.toString(output_creation);
//                                       long timestamp_creation = Long.parseLong(str_creation) * 1000;
//                                       Date maj = (Date) formatter.parse(user.getString("updatedAt"));
//                                       long output_maj = maj.getTime() / 1000L;
//                                       String str_maj = Long.toString(output_maj);
//                                       long timestamp_maj = Long.parseLong(str_maj) * 1000;
//                                       // enregistrer en local
//                                       List<Utilisateur> userH = SugarRecord.find(Utilisateur.class, "numero = ?", user.getString("numero"));
//                                       if (userH.size() != 0) {
//                                           nouvel_utilisateur = userH.get(0);
//                                           nouvel_utilisateur.setId_utilisateur(user.getString("id"));
//                                           nouvel_utilisateur.setNumero(user.getString("numero"));
//                                           nouvel_utilisateur.setNom(user.getString("firstName"));
//                                           nouvel_utilisateur.setPrenoms(user.getString("lastName"));
//                                           nouvel_utilisateur.setPhoto_identite(user.getString("profilePicture"));
//                                           nouvel_utilisateur.setcni_photo(user.getString("cniPicture"));
//                                           nouvel_utilisateur.setMdp(user.getString("password"));
//                                           nouvel_utilisateur.setStatut(user.getString("statut"));
//                                           nouvel_utilisateur.setNumero_compte(user.getString("accountNumber"));
//                                           if(result.isNull("referent"))
//                                               nouvel_utilisateur.setCodeMarchand("");
//                                           else
//                                               nouvel_utilisateur.setCodeMarchand(result.getString("referent"));
//                                           nouvel_utilisateur.setMaj(timestamp_maj);
//                                           nouvel_utilisateur.save();
//                                           id = Long.parseLong(nouvel_utilisateur.getId_utilisateur());
//                                       }
//                                       //Log.d("nouvel_utilisateur_id", "id:"+String.valueOf(id));
//                                       if (id != null) {
//                                           // Mettre à jour la préférence id utilisateur
//                                           Prefs.putString(ID_UTILISATEUR_KEY, String.valueOf(id));
//                                           // Mettre à jour la préférence nom
//                                           Prefs.putString(NOM_KEY, user.getString("firstName"));
//                                           // Mettre à jour la préférence prenoms
//                                           Prefs.putString(PRENOMS_KEY, user.getString("lastName"));
//                                           // Mettre à jour la préférence pin d'accès
//                                           // Prefs.putString(PIN_KEY, user.getString("pin_acces"));
//                                           // Mettre à jour la préférence pin d'accès
//                                           Prefs.putString(PASS_KEY, user.getString("password"));
//                                           Prefs.putBoolean(MOOV_DATA_SHARING, true);
//                                           // Mettre à jour la préférence statut
//                                           Prefs.putString(STATUT_UTILISATEUR, user.getString("statut"));
//                                           //Mettre à jour la preference photo de la CNI
//                                           Prefs.putString(PHOTO_CNI_KEY, user.getString("cniPicture"));
//                                           //Mettre à jour la preference firebase token
////                                    Prefs.putString(FIREBASE_TOKEN, user.getString("firebase_token"));
//                                           //Mettre à jour la preference sexe
//                                           Prefs.putString(SEXE_KEY, user.getString("sexe"));
//                                           //Mettre à jour la préference numero compte
//                                           Prefs.putString(NUMERO_COMPTE_KEY, user.getString("accountNumber"));
//                                           //Mettre à Jour le code marchand
//                                           if(user.isNull("referent"))
//                                               Prefs.putString(CODE_MARCHAND_KEY, "");
//                                           else
//                                               Prefs.putString(CODE_MARCHAND_KEY, user.getString("referent"));
//                                           // Mettre à jour la préférence pin d'accès
//                                           Date currentTime = Calendar.getInstance().getTime();
//                                           long output_current = currentTime.getTime() / 1000L;
//                                           String str_current = Long.toString(output_current);
//                                           long timestamp_current = Long.parseLong(str_current) * 1000;
//                                           Prefs.putString(CONNECTER_KEY, String.valueOf(timestamp_current));
//                                           // Mettre à jour la préférence pin d'accès
//                                           Prefs.putString(PHOTO_KEY, user.getString("profilePicture"));
//
//                                           // recuperation de la photo ditentie
//                                           // recuperer l'url de l'image
//                                           /**/
//                                           /**/URL url_photo  = new URL(Constantes.URL_MEDIA_PP + Prefs.getString(PHOTO_KEY, null) + ".png");
////                                          String src = Constantes.URL_MEDIA + Prefs.getString(PHOTO_KEY, null) + ".JPG";
//                                           URL url_photo_cni = new URL(Constantes.URL_MEDIA_CNI + Prefs.getString(PHOTO_CNI_KEY, null) + ".png");
//
//                                           //Bitmap bitmap_photo =Utilitaire.getBitmapFromURL(src);
//
//                                           Prefs.putString(TEL_KEY, String.valueOf(user.getString("numero")));
//
//                                           new DownloadTask().execute(url_photo);
//                                           new DownloadTaskCNI().execute(url_photo_cni);
//                                           // enrgeistrer les tontines en local
//                                           //e("user_data", result.get("user_data")+"");
//                                       }
//
//                                   }
//
//
//                                   JSONArray tontines = result.getJSONObject("body").getJSONArray("tontines");
//                                   // Verifier si api a envoyé des informations à mettre a jour concernant la table Tontines
//                                   if (tontines.length() > 0) {
//                                       Log.d("tontines_trouve", "oui:" + tontines.length());
//                                       List<Tontine> existeTontines = null;
//                                       List<Versement> existeVersement = null;
//                                       List<Retrait> existeRetrait = null;
//
//                                       for (int i = 0; i < tontines.length(); i++) {
//                                           Tontine nouvelle_tontine = new Tontine();
//
//                                           existeTontines = SugarRecord.find(Tontine.class, "id_server = ?", tontines.getJSONObject(i).getString("id"));
//                                           if (existeTontines.size() == 0) {
//                                               //nouvelle_tontine.setId(Long.valueOf(tontines.getJSONObject(i).getString("id")));
//                                               nouvelle_tontine.setId_utilisateur(Prefs.getString(ID_UTILISATEUR_KEY, null));
//                                               nouvelle_tontine.setPeriode(tontines.getJSONObject(i).getString("periode"));
//                                               nouvelle_tontine.setMise(Integer.parseInt(tontines.getJSONObject(i).getString("mise")));
//                                               nouvelle_tontine.setPrelevement_auto(tontines.getJSONObject(i).getBoolean("isAutoPayment"));
////                                                nouvelle_tontine.setIdSim(String.valueOf(sim_par_defaut.getId()));
//                                               nouvelle_tontine.setCarnet(tontines.getJSONObject(i).getString("carnet"));
//                                               nouvelle_tontine.setContinuer(Long.valueOf(tontines.getJSONObject(i).getString("carnet")));
//                                               nouvelle_tontine.setStatut(tontines.getJSONObject(i).getString("state"));
//                                               nouvelle_tontine.setId_server(tontines.getJSONObject(i).getString("id"));
//                                               if (!tontines.getJSONObject(i).isNull("unBlockDate")) {
//                                                   nouvelle_tontine.setDateDeblocage(tontines.getJSONObject(i).getString("unBlockDate"));
//                                               }
//                                               if (tontines.getJSONObject(i).getString("denomination") == "")
//                                                   nouvelle_tontine.setDenomination("Ma tontine " + nouvelle_tontine.getCarnet());
//                                               else
//                                                   nouvelle_tontine.setDenomination(tontines.getJSONObject(i).getString("denomination"));
//                                               // maj des dates
//                                               @SuppressLint("SimpleDateFormat") DateFormat formatter_1 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
//                                               Date creation_1 = formatter_1.parse(tontines.getJSONObject(i).getString("createdAt"));
//                                               long output_creation_1 = creation_1.getTime() / 1000L;
//                                               String str_creation_1 = Long.toString(output_creation_1);
//                                               long timestamp_creation_1 = Long.parseLong(str_creation_1) * 1000;
//                                               nouvelle_tontine.setCreation(timestamp_creation_1);
//                                               Date creation_2 = formatter_1.parse(tontines.getJSONObject(i).getString("updatedAt"));
//                                               long output_creation_2 = creation_2.getTime() / 1000L;
//                                               String str_creation_2 = Long.toString(output_creation_2);
//                                               long timestamp_maj_1 = Long.parseLong(str_creation_2) * 1000;
//                                               nouvelle_tontine.setMaj(timestamp_maj_1);
//                                               Log.e("connexion", "5connexion trying");
//
//                                               nouvelle_tontine.save();
//                                           } else {
//                                               nouvelle_tontine = existeTontines.get(0);
//                                               nouvelle_tontine.setContinuer(Long.valueOf(tontines.getJSONObject(i).getString("carnet")));
//                                               nouvelle_tontine.setStatut(tontines.getJSONObject(i).getString("state"));
//                                               @SuppressLint("SimpleDateFormat") DateFormat formatter_1 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
//                                               Date creation_2 = formatter_1.parse(tontines.getJSONObject(i).getString("updatedAt"));
//                                               long output_creation_2 = creation_2.getTime() / 1000L;
//                                               String str_creation_2 = Long.toString(output_creation_2);
//                                               long timestamp_maj_1 = Long.parseLong(str_creation_2) * 1000;
//                                               nouvelle_tontine.setMaj(timestamp_maj_1);
//                                               Log.e("connexion", "5connexion trying");
//
//                                               nouvelle_tontine.save();
//                                           }
//
//                                           // enrgeistrer les versements en local
//
//                                           JSONArray versements = tontines.getJSONObject(i).getJSONArray("versements");
//                                           Log.d("versement", versements.toString());
//                                           if (versements.length() > 0) {
//                                               Versement versement = new Versement();
//                                               Log.d("versements_trouve", "oui:" + versements.length() + " carnet: " + nouvelle_tontine.getCarnet());
//                                               for (int j = 0; j < versements.length(); j++) {
//
//                                                   existeVersement = SugarRecord.find(Versement.class, "id_vers_serv = ?", versements.getJSONObject(j).getString("id"));
//
//                                                   if (existeVersement.size() == 0) {
//                                                       Log.d("versements_trouve", "for:" + j + " carnet: " + nouvelle_tontine.getCarnet());
//                                                       versement.setMontant(versements.getJSONObject(j).getString("amount"));
//                                                       versement.setStatut_paiement(versements.getJSONObject(j).getBoolean("isValide"));
//                                                       versement.setFractionne(versements.getJSONObject(j).getString("isFractioned"));
//                                                       versement.setIdVersement(versements.getJSONObject(j).getString("idVersement"));
//                                                       versement.setIdVersServ(versements.getJSONObject(j).getString("id"));
//                                                       Log.d("versements_trouve", "idServeur1:" + versements.getJSONObject(j).getString("id"));
//                                                       versement.setTontine(nouvelle_tontine);
//
//                                                       // maj des dates
//                                                       @SuppressLint("SimpleDateFormat") DateFormat formatter_11 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
//                                                       Date creation_11 = formatter_11.parse(versements.getJSONObject(j).getString("createdAt"));
//                                                       long output_creation_11 = creation_11.getTime() / 1000L;
//                                                       String str_creation_11 = Long.toString(output_creation_11);
//                                                       long timestamp_creation_11 = Long.parseLong(str_creation_11) * 1000;
//                                                       versement.setCreation(timestamp_creation_11);
//                                                       Date creation_21 = formatter_11.parse(versements.getJSONObject(j).getString("updatedAt"));
//                                                       long output_creation_21 = creation_21.getTime() / 1000L;
//                                                       String str_creation_21 = Long.toString(output_creation_21);
//                                                       long timestamp_maj_11 = Long.parseLong(str_creation_21) * 1000;
//                                                       versement.setMaj(timestamp_maj_11);
//                                                       Utilisateur u = SugarRecord.find(Utilisateur.class, "id_utilisateur = ? ", Prefs.getString(ID_UTILISATEUR_KEY, "")).get(0);
//                                                       versement.setUtilisateur(u);
//                                                       versement.save();
//                                                   } else {
//                                                       versement = existeVersement.get(0);
//                                                       Log.d("versements_trouve", "idServeur2-1 : " + versement.getIdVersServ());
//                                                   }
//                                                   Log.d("versements_trouve", "idServeur2-2 : " + versement.getIdVersServ());
//                                                   if (versements.getJSONObject(j).getString("isFractioned").equals("false")) {
//                                                       List<Versement> versementList = Select.from(Versement.class)
//                                                               .where(Condition.prop("id_vers_serv").eq(versements.getJSONObject(j).getString("idVersement")))
//                                                               .orderBy("id desc")
//                                                               .list();
//                                                       //Versement v = SugarRecord.findById(Versement.class, Long.valueOf(versements.getJSONObject(j).getString("id_versement")));
//                                                       if (versementList.size() > 0) {
//                                                           versement.setIdVersement(String.valueOf(versementList.get(0).getId()));
//                                                           versement.save();
//                                                       }
//
//                                                   }
//                                               }
//                                           }
//
//
//                                           Log.e("connexion", tontines.getJSONObject(i).get("withdraw") + "");
//                                           // enrgeistrer les retraits  en local
//                                           if(tontines.getJSONObject(i).has("withdraw") && !tontines.getJSONObject(i).isNull("withdraw")) {
//                                               JSONObject retraits = tontines.getJSONObject(i).getJSONObject("withdraw");
//
//                                               existeRetrait = SugarRecord.find(Retrait.class, "token = ?", retraits.getString("token"));
//                                               Retrait retrait = new Retrait();
//                                               if (existeRetrait.size() == 0) {
//
//                                                   retrait.setTontine(nouvelle_tontine);
//                                                   retrait.setToken(retraits.getString("token"));
//                                                   retrait.setStatut(retraits.getString("state"));
////                                                        retrait.setIdMarchand(retraits.getJSONObject(k).getString("id_marchand"));
//                                                   retrait.setMontant(retraits.getString("amount"));
//
//                                                   // maj des dates
//                                                   @SuppressLint("SimpleDateFormat") DateFormat formatter_111 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
//                                                   Date creation_111 = formatter_111.parse(retraits.getString("createdAt"));
//                                                   long output_creation_111 = creation_111.getTime() / 1000L;
//                                                   String str_creation_111 = Long.toString(output_creation_111);
//                                                   long timestamp_creation_111 = Long.parseLong(str_creation_111) * 1000;
//                                                   retrait.setCreation(timestamp_creation_111);
//                                                   Date creation_211 = formatter_111.parse(retraits.getString("updatedAt"));
//                                                   long output_creation_211 = creation_211.getTime() / 1000L;
//                                                   String str_creation_211 = Long.toString(output_creation_211);
//                                                   long timestamp_maj_111 = Long.parseLong(str_creation_211) * 1000;
//                                                   retrait.setMaj(timestamp_maj_111);
//                                                   Utilisateur u = SugarRecord.find(Utilisateur.class, "id_utilisateur = ? ", Prefs.getString(ID_UTILISATEUR_KEY, "")).get(0);
//                                                   retrait.setUtilisateur(u);
//                                                   retrait.save();
//                                               } else {
//                                                   retrait = existeRetrait.get(0);
//                                                   retrait.setStatut(retraits.getString("state"));
//                                                   @SuppressLint("SimpleDateFormat") DateFormat formatter_111 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
//                                                   Date creation_211 = formatter_111.parse(retraits.getString("updatedAt"));
//                                                   long output_creation_211 = creation_211.getTime() / 1000L;
//                                                   String str_creation_211 = Long.toString(output_creation_211);
//                                                   long timestamp_maj_111 = Long.parseLong(str_creation_211) * 1000;
//                                                   retrait.setMaj(timestamp_maj_111);
//                                                   retrait.save();
//                                               }
//                                           }
//                                       }
//                                   }
//
//                                   // enregistrer les historiques
//                                   JSONArray histories = result.getJSONObject("body").getJSONArray("histories");
//                                   // Verifier si api a envoyé des informations à mettre a jour concernant la table Histories
//                                   if (histories.length() > 0) {
//                                       Log.d("histories_trouve", "oui:" + histories.length());
//                                       List<History> existeHistories = null;
//                                       List<Versement> existeVersement = null;
//                                       List<Retrait> existeRetrait = null;
//
//                                       for (int i = 0; i < histories.length(); i++) {
//                                           History nouvelle_history = new History();
//
//                                           existeHistories = SugarRecord.find(History.class, "id_server = ?", histories.getJSONObject(i).getString("id"));
//                                           if (existeHistories.size() == 0) {
//                                               nouvelle_history.setContenu(histories.getJSONObject(i).getString("content"));
//                                               nouvelle_history.setTitre(histories.getJSONObject(i).getString("title"));
//                                               nouvelle_history.setEtat(histories.getJSONObject(i).getBoolean("state") ? 1 : 0);
//                                               nouvelle_history.setUser(nouvel_utilisateur);
////                                               nouvelle_history.setNumero(Integer.parseInt(histories.getJSONObject(i).getString("numero_client")));
//                                               nouvelle_history.setId_server(histories.getJSONObject(i).getString("id"));
//
//                                               @SuppressLint("SimpleDateFormat")
//                                               DateFormat formatter_1 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
//                                               Date creation_1 = formatter_1.parse(histories.getJSONObject(i).getString("createdAt"));
//                                               long output_creation_1 = creation_1.getTime() / 1000L;
//                                               String str_creation_1 = Long.toString(output_creation_1);
//                                               long timestamp_creation_1 = Long.parseLong(str_creation_1) * 1000;
//                                               nouvelle_history.setCreation(timestamp_creation_1);
//                                               nouvelle_history.save();
//                                           }
//                                       }
//                                   }
//
//                                   //send request to set update_data state to 1
//                                   RequestQueue queue2 = Volley.newRequestQueue(context);
//                                   StringRequest postRequest2 = new StringRequest(Request.Method.POST, Constantes.URL_VALIDATE_FREQ_UPDATE_DB,
//                                           new Response.Listener<String>() {
//                                               @SuppressLint("ResourceAsColor")
//                                               @Override
//                                               public void onResponse(String response) {
//                                                   Log.e("onResponse: ", response);
//                                                   if (Prefs.getString(NUMERO_COMPTE_KEY, null) != null) {
//                                                       try {
//                                                           JSONObject result = new JSONObject(response);
//                                                           if (result.getBoolean("success")) {
//                                                               Log.d("UpdateJob", "success de mise à jour de la table des mise à jour");
//                                                           }  else {
//                                                               Log.d("UpdateJob", "echec de mise à jour de la table des mise à jour");
//                                                           }
//                                                       } catch (Throwable t) {
//                                                            Log.e("UpdateJob", "echec de la mise à Jour de l'etat de la mise à jour");
//                                                       }
//                                                   }
//                                               }
//                                           },
//                                           new Response.ErrorListener() {
//                                               @Override
//                                               public void onErrorResponse(VolleyError error) {
//                                                   Log.e("Error.Synchronisation", String.valueOf(error.getMessage()));
//                                               }
//                                           }
//                                   ) {
//                                       @Override
//                                       protected Map<String, String> getParams() {
//                                           Map<String, String> params = new HashMap<String, String>();
//                                           params.put("customerNumber", Prefs.getString(TEL_KEY, null));
//                                           params.put("etat", "1");
//                                           params.put("id_data_update", id_data_update+"");
//                                           return params;
//                                       }
//                                       @Override
//                                       public Map<String, String> getHeaders() throws AuthFailureError {
//                                           Map<String, String> headers = new HashMap<>();
//                                           headers.put("Content-Type", "application/json");
//                                           headers.put("Authorization", "Bearer " + accessToken); // Ajoute le token ici
//                                           return headers;
//                                       }
//                                   };
//                                   postRequest2.setRetryPolicy(new DefaultRetryPolicy(
//                                           30000,
//                                           -1,
//                                           DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
//                                   queue2.add(postRequest2);
//
//                               } else {
//                                   if (result.has("body")) {
//                                       String msg = result.getString("body");
//                                       Log.e("Mauvais message: ", msg);
//                                   } else {
//                                       String msg = "Vos identifiants sont incorrects. Veuillez réessayer SVP! ";
//                                       Log.e("Mauvais Message: ", msg);
//                                   }
//                               }
//                           } catch (Throwable t) {
//                               Log.d("errornscription", String.valueOf(t.getCause()));
//
//                               Log.e("Update Data", t.getMessage());
//                           }
//                       }
//                    }
//                },
//                new Response.ErrorListener()
//                {
//                    @Override
//                    public void onErrorResponse(VolleyError error) {
//                        Log.e("Error.Synchronisation", String.valueOf(error.getMessage()));
//                    }
//                }
//        ) {
//            @Override
//            protected Map<String, String> getParams()
//            {
//                Map<String, String>  params = new HashMap<String, String>();
//                params.put("customerNumber",Prefs.getString(TEL_KEY,null));
//                return params;
//            }
//            @Override
//            public Map<String, String> getHeaders() throws AuthFailureError {
//                Map<String, String> headers = new HashMap<>();
//                headers.put("Content-Type", "application/json");
//                headers.put("Authorization", "Bearer " + accessToken); // Ajoute le token ici
//                return headers;
//            }
//        };
//        postRequest.setRetryPolicy(new DefaultRetryPolicy(
//                30000,
//                -1,
//                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
//        queue.add(postRequest);
//    }

    //refresh from a swiptRefreshLayout
    public void refreshDatabse( SwipeRefreshLayout swipeRefreshLayout){
        RequestQueue queue = Volley.newRequestQueue(context);
        Log.e("updateDatabse", "swip to reffesh is working");
        StringRequest postRequest = new StringRequest(Request.Method.POST, Constantes.URL_ALL_DATA,
                new Response.Listener<String>()
                {
                    @SuppressLint("ResourceAsColor")
                    @Override
                    public void onResponse(String response) {
                        Log.e("onResponse: ", response);
                        try {
                            JSONObject result = new JSONObject(response);
                            if (result.getInt("responseCode") == 0) {
                                JSONObject user = result.getJSONObject("body").getJSONObject("data");
                                Long id = Long.valueOf(0);
                                Utilisateur nouvel_utilisateur = null;
                                @SuppressLint("SimpleDateFormat") DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                                Date creation = (Date) formatter.parse(user.getString("createdAt"));
                                long output_creation = creation.getTime() / 1000L;
                                String str_creation = Long.toString(output_creation);
                                long timestamp_creation = Long.parseLong(str_creation) * 1000;
                                Date maj = (Date) formatter.parse(user.getString("updatedAt"));
                                long output_maj = maj.getTime() / 1000L;
                                String str_maj = Long.toString(output_maj);
                                long timestamp_maj = Long.parseLong(str_maj) * 1000;
                                // enregistrer en local
                                List<Utilisateur> userH = SugarRecord.find(Utilisateur.class, "numero = ?", user.getString("numero"));
                                if (userH.size() != 0) {
                                    nouvel_utilisateur = userH.get(0);
                                    nouvel_utilisateur.setId_utilisateur(user.getString("id"));
                                    nouvel_utilisateur.setNumero(user.getString("numero"));
                                    nouvel_utilisateur.setNom(user.getString("firstName"));
                                    nouvel_utilisateur.setPrenoms(user.getString("lastName"));
                                    nouvel_utilisateur.setPhoto_identite(user.getString("profilePicture"));
                                    nouvel_utilisateur.setcni_photo(user.getString("cniPicture"));
                                    nouvel_utilisateur.setMdp(user.getString("password"));
                                    nouvel_utilisateur.setStatut(user.getString("statut"));
                                    nouvel_utilisateur.setNumero_compte(user.getString("accountNumber"));
                                    if(result.isNull("referent"))
                                        nouvel_utilisateur.setCodeMarchand("");
                                    else
                                        nouvel_utilisateur.setCodeMarchand(result.getString("referent"));
                                    nouvel_utilisateur.setMaj(timestamp_maj);
                                    nouvel_utilisateur.save();
                                    id = Long.parseLong(nouvel_utilisateur.getId_utilisateur());
                                }
                                //Log.d("nouvel_utilisateur_id", "id:"+String.valueOf(id));
                                if (id != null) {


                                    // Mettre à jour la préférence id utilisateur
                                    Prefs.putString(ID_UTILISATEUR_KEY, String.valueOf(id));
                                    // Mettre à jour la préférence nom
                                    Prefs.putString(NOM_KEY, user.getString("firstName"));
                                    // Mettre à jour la préférence prenoms
                                    Prefs.putString(PRENOMS_KEY, user.getString("lastName"));
                                    // Mettre à jour la préférence pin d'accès
//                                    Prefs.putString(PIN_KEY, user.getString("pin_acces"));
                                    // Mettre à jour la préférence pin d'accès
                                    Prefs.putString(PASS_KEY, user.getString("password"));
                                    Prefs.putBoolean(MOOV_DATA_SHARING, true);
                                    // Mettre à jour la préférence statut
                                    Prefs.putString(STATUT_UTILISATEUR, user.getString("statut"));
                                    //Mettre à jour la preference photo de la CNI
                                    Prefs.putString(PHOTO_CNI_KEY, user.getString("cniPicture"));
                                    //Mettre à jour la preference firebase token
//                                    Prefs.putString(FIREBASE_TOKEN, user.getString("firebase_token"));
                                    //Mettre à jour la preference sexe
                                    Prefs.putString(SEXE_KEY, user.getString("sexe"));
                                    //Mettre à jour la préference numero compte
                                    Prefs.putString(NUMERO_COMPTE_KEY, user.getString("accountNumber"));
                                    //Mettre à Jour le code marchand
                                    if(user.isNull("referent"))
                                        Prefs.putString(CODE_MARCHAND_KEY, "");
                                    else
                                        Prefs.putString(CODE_MARCHAND_KEY, user.getString("referent"));
                                    // Mettre à jour la préférence pin d'accès
                                    Date currentTime = Calendar.getInstance().getTime();
                                    long output_current = currentTime.getTime() / 1000L;
                                    String str_current = Long.toString(output_current);
                                    long timestamp_current = Long.parseLong(str_current) * 1000;
                                    Prefs.putString(CONNECTER_KEY, String.valueOf(timestamp_current));
                                    // Mettre à jour la préférence pin d'accès
                                    Prefs.putString(PHOTO_KEY, user.getString("profilePicture"));
                                    URL url_photo  = new URL(Constantes.URL_MEDIA_PP + Prefs.getString(PHOTO_KEY, null) + ".png");
//                                    String src = Constantes.URL_MEDIA + Prefs.getString(PHOTO_KEY, null) + ".JPG";
                                    URL url_photo_cni = new URL(Constantes.URL_MEDIA_CNI + Prefs.getString(PHOTO_CNI_KEY, null) + ".png");

                                    //Bitmap bitmap_photo =Utilitaire.getBitmapFromURL(src);

                                    Prefs.putString(TEL_KEY, String.valueOf(user.getString("numero")));

                                    new DownloadTask().execute(url_photo);
                                    new DownloadTaskCNI().execute(url_photo_cni);
                                    // enrgeistrer les tontines en local
//                                          Log.e("user_data", result.get("user_data")+"");
                                }

                                JSONArray tontines = user.getJSONArray("tontines");
                                // Verifier si api a envoyé des informations à mettre a jour concernant la table Tontines
                                if (tontines.length() > 0) {
                                    Log.d("tontines_trouve", "oui:" + tontines.length());
                                    List<Tontine> existeTontines = null;
                                    List<Versement> existeVersement = null;
                                    List<Retrait> existeRetrait = null;

                                    for (int i = 0; i < tontines.length(); i++) {
                                        Tontine nouvelle_tontine = new Tontine();

                                        existeTontines = SugarRecord.find(Tontine.class, "id_server = ?", tontines.getJSONObject(i).getString("id"));
                                        if (existeTontines.size() == 0) {
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
                                JSONArray histories = result.getJSONObject("body").getJSONArray("histories");
                                // Verifier si api a envoyé des informations à mettre a jour concernant la table Histories
                                if (histories.length() > 0) {
                                    Log.d("histories_trouve", "oui:" + histories.length());
                                    List<History> existeHistories = null;
                                    List<Versement> existeVersement = null;
                                    List<Retrait> existeRetrait = null;

                                    for (int i = 0; i < histories.length(); i++) {
                                        History nouvelle_history = new History();

                                        existeHistories = SugarRecord.find(History.class, "id_server = ?", histories.getJSONObject(i).getString("id"));
                                        if (existeHistories.size() == 0) {
                                            nouvelle_history.setContenu(histories.getJSONObject(i).getString("content"));
                                            nouvelle_history.setTitre(histories.getJSONObject(i).getString("title"));
                                            nouvelle_history.setEtat(histories.getJSONObject(i).getBoolean("state") ? 1 : 0);
                                            nouvelle_history.setUser(nouvel_utilisateur);
//                                            nouvelle_history.setNumero(Integer.parseInt(histories.getJSONObject(i).getString("numero_client")));
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

                                swipeRefreshLayout.setRefreshing(false);
                                Toast.makeText(context, "Rafraichissement effecuté", Toast.LENGTH_SHORT).show();
                            } else {
                                if (result.has("body")) {
                                    String msg = result.getString("body");
                                    Log.e("Mauvais message: ", msg);
                                } else {
                                    String msg = "Vos identifiants sont incorrects. Veuillez réessayer SVP! ";
                                    Log.e("Mauvais Message: ", msg);
                                }
                            }
                        } catch (Throwable t) {
                            Log.d("errornscription", String.valueOf(t.getCause()));

                            Log.e("Update Data", t.getMessage());
                        }
                    }
                },
                new Response.ErrorListener()
                {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        if (error.networkResponse != null && error.networkResponse.statusCode == 401) {
                            refreshAccessToken(new TokenRefreshListener() {
                                @Override
                                public void onTokenRefreshed(boolean success) {
                                    if (success) {
                                        refreshDatabse(new SwipeRefreshLayout(context));
                                    }
                                }
                            });
                        } else {
                            Log.e("Error.Synchronisation", String.valueOf(error.getMessage()));
                        }
                    }
                }
        ) {
            @Override
            protected Map<String, String> getParams()
            {
                Map<String, String>  params = new HashMap<String, String>();
                params.put("customerNumber",Prefs.getString(TEL_KEY,null));
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

    }

    //refresh all app
    public void refreshDatabse(){
        RequestQueue queue = Volley.newRequestQueue(context);
        Log.e("updateDatabse", "swip to reffesh is working");
        StringRequest postRequest = new StringRequest(Request.Method.POST, Constantes.URL_ALL_DATA,
                new Response.Listener<String>()
                {
                    @SuppressLint("ResourceAsColor")
                    @Override
                    public void onResponse(String response) {
                        Log.e("onResponse: ", response);
                        try {
                            JSONObject result = new JSONObject(response);
                            if (result.getInt("responseCode") == 0) {
                                JSONObject user = result.getJSONObject("body").getJSONObject("data");
                                Long id = Long.valueOf(0);
                                Utilisateur nouvel_utilisateur = null;
                                @SuppressLint("SimpleDateFormat") DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                                Date creation = (Date) formatter.parse(user.getString("createdAt"));
                                long output_creation = creation.getTime() / 1000L;
                                String str_creation = Long.toString(output_creation);
                                long timestamp_creation = Long.parseLong(str_creation) * 1000;
                                Date maj = (Date) formatter.parse(user.getString("updatedAt"));
                                long output_maj = maj.getTime() / 1000L;
                                String str_maj = Long.toString(output_maj);
                                long timestamp_maj = Long.parseLong(str_maj) * 1000;
                                // enregistrer en local
                                List<Utilisateur> userH = SugarRecord.find(Utilisateur.class, "numero = ?", user.getString("numero"));
                                if (userH.size() != 0) {
                                    nouvel_utilisateur = userH.get(0);
                                    nouvel_utilisateur.setId_utilisateur(user.getString("id"));
                                    nouvel_utilisateur.setNumero(user.getString("numero"));
                                    nouvel_utilisateur.setNom(user.getString("firstName"));
                                    nouvel_utilisateur.setPrenoms(user.getString("lastName"));
                                    nouvel_utilisateur.setPhoto_identite(user.getString("profilePicture"));
                                    nouvel_utilisateur.setcni_photo(user.getString("cniPicture"));
                                    nouvel_utilisateur.setMdp(user.getString("password"));
                                    nouvel_utilisateur.setStatut(user.getString("statut"));
                                    nouvel_utilisateur.setNumero_compte(user.getString("accountNumber"));
                                    if(result.isNull("referent"))
                                        nouvel_utilisateur.setCodeMarchand("");
                                    else
                                        nouvel_utilisateur.setCodeMarchand(result.getString("referent"));
                                    nouvel_utilisateur.setMaj(timestamp_maj);
                                    nouvel_utilisateur.save();
                                    id = Long.parseLong(nouvel_utilisateur.getId_utilisateur());
                                }
                                //Log.d("nouvel_utilisateur_id", "id:"+String.valueOf(id));
                                if (id != null) {

                                    // Mettre à jour la préférence id utilisateur
                                    Prefs.putString(ID_UTILISATEUR_KEY, String.valueOf(id));
                                    // Mettre à jour la préférence nom
                                    Prefs.putString(NOM_KEY, user.getString("firstName"));
                                    // Mettre à jour la préférence prenoms
                                    Prefs.putString(PRENOMS_KEY, user.getString("lastName"));
                                    // Mettre à jour la préférence pin d'accès
//                                    Prefs.putString(PIN_KEY, user.getString("pin_acces"));
                                    // Mettre à jour la préférence pin d'accès
                                    Prefs.putString(PASS_KEY, user.getString("password"));
                                    Prefs.putBoolean(MOOV_DATA_SHARING, true);
                                    // Mettre à jour la préférence statut
                                    Prefs.putString(STATUT_UTILISATEUR, user.getString("statut"));
                                    //Mettre à jour la preference photo de la CNI
                                    Prefs.putString(PHOTO_CNI_KEY, user.getString("cniPicture"));
                                    //Mettre à jour la preference firebase token
//                                    Prefs.putString(FIREBASE_TOKEN, user.getString("firebase_token"));
                                    //Mettre à jour la preference sexe
                                    Prefs.putString(SEXE_KEY, user.getString("sexe"));
                                    //Mettre à jour la préference numero compte
                                    Prefs.putString(NUMERO_COMPTE_KEY, user.getString("accountNumber"));
                                    //Mettre à Jour le code marchand
                                    if(user.isNull("referent"))
                                        Prefs.putString(CODE_MARCHAND_KEY, "");
                                    else
                                        Prefs.putString(CODE_MARCHAND_KEY, user.getString("referent"));
                                    // Mettre à jour la préférence pin d'accès
                                    Date currentTime = Calendar.getInstance().getTime();
                                    long output_current = currentTime.getTime() / 1000L;
                                    String str_current = Long.toString(output_current);
                                    long timestamp_current = Long.parseLong(str_current) * 1000;
                                    Prefs.putString(CONNECTER_KEY, String.valueOf(timestamp_current));
                                    // Mettre à jour la préférence pin d'accès
                                    Prefs.putString(PHOTO_KEY, user.getString("profilePicture"));

                                    // recuperation de la photo ditentie
                                    // recuperer l'url de l'image
                                    /**/
                                    /**/URL url_photo  = new URL(Constantes.URL_MEDIA_PP + Prefs.getString(PHOTO_KEY, null) + ".png");
//                                    String src = Constantes.URL_MEDIA + Prefs.getString(PHOTO_KEY, null) + ".JPG";
                                    URL url_photo_cni = new URL(Constantes.URL_MEDIA_CNI + Prefs.getString(PHOTO_CNI_KEY, null) + ".png");

                                    //Bitmap bitmap_photo =Utilitaire.getBitmapFromURL(src);

                                    Prefs.putString(TEL_KEY, String.valueOf(user.getString("numero")));

                                    new DownloadTask().execute(url_photo);
                                    new DownloadTaskCNI().execute(url_photo_cni);
                                    // enrgeistrer les tontines en local
//                                          Log.e("user_data", result.get("user_data")+"");
                                }

                                JSONArray tontines = user.getJSONArray("tontines");
                                // Verifier si api a envoyé des informations à mettre a jour concernant la table Tontines
                                if (tontines.length() > 0) {
                                    Log.d("tontines_trouve", "oui:" + tontines.length());
                                    List<Tontine> existeTontines = null;
                                    List<Versement> existeVersement = null;
                                    List<Retrait> existeRetrait = null;

                                    for (int i = 0; i < tontines.length(); i++) {
                                        Tontine nouvelle_tontine = new Tontine();

                                        existeTontines = SugarRecord.find(Tontine.class, "id_server = ?", tontines.getJSONObject(i).getString("id"));
                                        if (existeTontines.size() == 0) {
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
                                JSONArray histories = result.getJSONObject("body").getJSONArray("histories");
                                // Verifier si api a envoyé des informations à mettre a jour concernant la table Histories
                                if (histories.length() > 0) {
                                    Log.d("histories_trouve", "oui:" + histories.length());
                                    List<History> existeHistories = null;
                                    List<Versement> existeVersement = null;
                                    List<Retrait> existeRetrait = null;

                                    for (int i = 0; i < histories.length(); i++) {
                                        History nouvelle_history = new History();

                                        existeHistories = SugarRecord.find(History.class, "id_server = ?", histories.getJSONObject(i).getString("id"));
                                        if (existeHistories.size() == 0) {
                                            nouvelle_history.setContenu(histories.getJSONObject(i).getString("content"));
                                            nouvelle_history.setTitre(histories.getJSONObject(i).getString("title"));
                                            nouvelle_history.setEtat(histories.getJSONObject(i).getBoolean("state") ? 1 : 0);
                                            nouvelle_history.setUser(nouvel_utilisateur);
//                                            nouvelle_history.setNumero(Integer.parseInt(histories.getJSONObject(i).getString("numero_client")));
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

                                Toast.makeText(context, "Rafraichissement effecuté", Toast.LENGTH_SHORT).show();
                            } else {
                                if (result.has("body")) {
                                    String msg = result.getString("body");
                                    Log.e("Mauvais message: ", msg);
                                } else {
                                    String msg = "Vos identifiants sont incorrects. Veuillez réessayer SVP! ";
                                    Log.e("Mauvais Message: ", msg);
                                }
                            }
                        } catch (Throwable t) {
                            Log.d("errornscription", String.valueOf(t.getCause()));

                            Log.e("Update Data", t.getMessage());
                        }
                    }
                },
                new Response.ErrorListener()
                {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        if (error.networkResponse != null && error.networkResponse.statusCode == 401) {
                            refreshAccessToken(new TokenRefreshListener() {
                                @Override
                                public void onTokenRefreshed(boolean success) {
                                    if (success) {
                                        refreshDatabse();
                                    }
                                }
                            });
                        } else {
                            Log.e("Error.Synchronisation", String.valueOf(error.getMessage()));
                        }
                    }
                }
        ) {
            @Override
            protected Map<String, String> getParams()
            {
                Map<String, String>  params = new HashMap<String, String>();
                params.put("customerNumber",Prefs.getString(TEL_KEY,null));
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

    }

}