package com.sicmagroup.tondi;

import android.annotation.SuppressLint;
import android.app.NotificationManager;
import android.content.Context;
import android.content.ContextWrapper;
import android.util.Log;

import androidx.core.app.NotificationCompat;

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
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.orm.SugarRecord;
import com.orm.query.Condition;
import com.orm.query.Select;
import com.pixplicity.easyprefs.library.Prefs;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.sicmagroup.tondi.Connexion.ID_UTILISATEUR_KEY;
import static com.sicmagroup.tondi.utils.Constantes.SERVEUR;
import static com.sicmagroup.tondi.utils.Constantes.accessToken;

public class FirebaseService extends FirebaseMessagingService  {

    public static String TAG = "Firebase Test";
    public Context context;

    public FirebaseService() {

    }


    @Override
    public void onNewToken(String token)
    {
        new Prefs.Builder()
                .setContext(this)
                .setMode(ContextWrapper.MODE_PRIVATE)
                .setPrefsName(this.getPackageName())
                .setUseDefaultSharedPreference(true)
                .build();
        Log.e("testFirebase", "refresh token : "+token);
        if(Prefs.contains(ID_UTILISATEUR_KEY))
        {
            Log.e("testFirebase", "refresh token : "+Prefs.getString(ID_UTILISATEUR_KEY, "bb"));
            SendFirebaseToken(token, Prefs.getString(ID_UTILISATEUR_KEY, ""));
        }
        //Save du token dans la bdd local
        /*SharedPreferences c = this.getSharedPreferences(TEL_KEY, Context.MODE_PRIVATE);
        Utilisateur user = new Utilisateur().getUser(c.getString(TEL_KEY, ""));
        user.setFirebaseToken(token);
        user.save();

            SendFirebaseToken(token, user.getId_utilisateur());*/
    }

    public void SendFirebaseToken(final String token, final String numero)
    {
        RequestQueue queue = Volley.newRequestQueue(this);
        String url_update_firebase_token = SERVEUR+"/api/v1/utilisateurs/update_firebase_token";
        StringRequest postRequest = new StringRequest(Request.Method.POST, url_update_firebase_token,
                new Response.Listener<String>()
                {
                    @SuppressLint("ResourceAsColor")
                    @Override
                    public void onResponse(String response) {
                        //progressDialog.dismiss();
                        // response
                        Log.e("ResponseTagkk", response);
                      /*  if (!response.equals("Erreur"))
                        {*/
                        try {

                            JSONObject result = new JSONObject(response);
                            //final JSONArray array = result.getJSONArray("data");
                            //Log.d("My App", obj.toString());
                            if (result.getBoolean("success") ){
                                Log.i("testFirebase", "Success");
                            }else{
                                Log.i("testFirebase", "Failed");
                            }

                        } catch (Throwable t) {
                            Log.d("errorFirebase",t.getMessage());
                        }
                    }
                },
                new Response.ErrorListener()
                {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {
//                        Log.e("ResponseTag", "Erreur");
//                        String message;
//                        if (volleyError instanceof NetworkError || volleyError instanceof AuthFailureError || volleyError instanceof TimeoutError) {
//                            SendFirebaseToken(token, numero);
//
//                        } else if (volleyError instanceof ServerError) {
//                            SendFirebaseToken(token, numero);
//                            //snackbar.show();
//                        }  else if (volleyError instanceof ParseError) {
//                            SendFirebaseToken(token, numero);
//                        }
                    }
                }
        ) {
            @Override
            protected Map<String, String> getParams()
            {
                Map<String, String>  params = new HashMap<String, String>();
                params.put("customerNumber", numero);
                params.put("firebase_token", token);
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
                5000,
                -1,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        queue.add(postRequest);
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage)
    {
        // TODO(developer): Handle FCM messages here.
        // Not getting messages here? See why this may be: https://goo.gl/39bRNJ
        Log.i(TAG, "From: " + remoteMessage.getFrom());

        // Check if message contains a data payload.
        if (remoteMessage.getData().size() > 0) {
            Log.e(TAG, "Message data payload: " + remoteMessage.getData());
            JSONObject reader = new JSONObject(remoteMessage.getData());
            Retrait r = new Retrait();
            try {
                List<Retrait> liste_retrait = Select.from(Retrait.class)
                        .where(Condition.prop("token").eq(reader.getString("token")))
                        .list();
                r = liste_retrait.get(0);
                r.setStatut("valide");
                r.save();
                Utilisateur u = Utilisateur.find(Utilisateur.class, "id_utilisateur = ?", Prefs.getString(ID_UTILISATEUR_KEY,null)).get(0);
                Date currentTime = Calendar.getInstance().getTime();
                long output_maj = currentTime.getTime() / 1000L;
                String str_maj = Long.toString(output_maj);
                long timestamp_maj = Long.parseLong(str_maj) * 1000;

                List<Tontine> tontines = r.getTontine().getIdForMontCumuleNow(r.getTontine().getId(), r.getTontine().getStatut());
                for (Tontine to:tontines) {
                    to.setStatut("encaissee");
                    to.setMaj(timestamp_maj);
                    to.save();
                }
//                    try {
//
//                        //
//                        Synchronisation new_sync2 = new Synchronisation();
//                        JSONObject jsonObject2 = new JSONObject();
//                        jsonObject2.put("a","update#tontines");
//                        jsonObject2.put("n",u.getNumero());
//                        Log.e("soldevv", String.valueOf(u.getSolde()));
//                        jsonObject2.put("s", u.getSolde());
//                        String t_json = gson.toJson(to);
//                        jsonObject2.put("d",t_json);
//                        new_sync2.setMaj(timestamp_maj);
//                        new_sync2.setStatut(0);
//                        new_sync2.setDonnees(jsonObject2.toString());
//                        new_sync2.save();
//
//                    } catch (JSONException e) {
//                        e.printStackTrace();
//                    }
//                }

            } catch (JSONException e) {
                e.printStackTrace();
            }

            if (/* Check if data needs to be processed by long running job */ true) {
                // For long-running tasks (10 seconds or more) use WorkManager.
                Log.e(TAG, "if state");
                //
            } else {
                // Handle message within 10 seconds
                Log.e(TAG, "else state");
            }

        }

        // Check if message contains a notification payload.
        if (remoteMessage.getNotification() != null) {
            Log.e(TAG, "Message Notification Body: " + remoteMessage.getNotification().getBody());
            NotificationCompat.Builder mBuilder =   new NotificationCompat.Builder(FirebaseService.this)
                    .setSmallIcon(R.drawable.icon_logo) // notification icon
                    .setContentTitle(remoteMessage.getNotification().getTitle()) // title for notification
                    .setContentText( remoteMessage.getNotification().getBody())
                    .setAutoCancel(true); // clear notification after click
            NotificationManager mNotificationManager =
                    (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
            mNotificationManager.notify(Integer.parseInt(String.valueOf(1)), mBuilder.build());
        }

        // Also if you intend on generating your own notifications as a result of a received FCM
        // message, here is where that should be initiated. See sendNotification method below.



    }
    /*@Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }*/
}
