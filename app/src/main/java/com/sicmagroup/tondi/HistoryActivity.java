package com.sicmagroup.tondi;

import static com.sicmagroup.tondi.Connexion.ID_UTILISATEUR_KEY;
import static com.sicmagroup.tondi.Connexion.TEL_KEY;
import static com.sicmagroup.tondi.utils.Constantes.REFRESH_TOKEN;
import static com.sicmagroup.tondi.utils.Constantes.SERVEUR;
import static com.sicmagroup.tondi.utils.Constantes.TOKEN;
import static com.sicmagroup.tondi.utils.Constantes.accessToken;
import static com.sicmagroup.tondi.utils.Constantes.url_refresh_token;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
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
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.snackbar.Snackbar;
import com.orm.query.Condition;
import com.orm.query.Select;
import com.pixplicity.easyprefs.library.Prefs;
import com.sicmagroup.formmaster.FormBuilder;
import com.sicmagroup.formmaster.model.BaseFormElement;
import com.sicmagroup.tondi.utils.Constantes;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HistoryActivity extends AppCompatActivity {
    String url_history = SERVEUR + "/api/v1/utilisateurs/print_historique";
    ProgressDialog progressDialog;
    private RecyclerView recyclerView;
    private ArrayList<History> historyList;
    private HistoryAdapter adapter;
    private TextView ifNoHistory;
    Button sort_btn;
    Boolean sorted = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);
        new Prefs.Builder()
                .setContext(this)
                .setMode(ContextWrapper.MODE_PRIVATE)
                .setPrefsName(getPackageName())
                .setUseDefaultSharedPreference(true)
                .build();

        recyclerView = (RecyclerView) findViewById(R.id.recycler_view_history_all);
        ifNoHistory = (TextView) findViewById(R.id.ifNoActivityMsg);
        sort_btn = (Button) findViewById(R.id.sort_history);

        historyList = new ArrayList<>();
        adapter = new HistoryAdapter(this, historyList);
        RecyclerView.LayoutManager mLayoutManager = new GridLayoutManager(this, 1);
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(adapter);
        afficherActivity();

        Button back_to = findViewById(R.id.back_to);
        back_to.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                HistoryActivity.this.finish();
                startActivity(new Intent(HistoryActivity.this,Home.class));
            }
        });

        sort_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!sorted)
                {
                    sorted = true;

                    Collections.reverse(historyList);
                    Toast.makeText(HistoryActivity.this, "Trier du plus récent au plus ancien", Toast.LENGTH_SHORT).show();
                    Log.d("trie", "!sorted");
                    sort_btn.setText("A-Z");
                }
                else {
                    sorted = false;
                    //Collections.sort(historyList);
                    Toast.makeText(HistoryActivity.this, "Trier du plus ancien au plus récent", Toast.LENGTH_SHORT).show();
                    Log.d("trie", "sorted");
                    sort_btn.setText("Z-A");
                }

                adapter.notifyDataSetChanged();

            }
        });

    }

    @Override
    public void onBackPressed() {
        // your code.
        HistoryActivity.this.finish();

        startActivity(new Intent(HistoryActivity.this,Home.class));
    }

    private void afficherActivity() {

        RequestQueue queue = Volley.newRequestQueue(this);

        JSONObject jsonBody = new JSONObject();
        try {
            jsonBody.put("customerNumber", Prefs.getString(TEL_KEY, ""));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        JsonObjectRequest jsonRequest = new JsonObjectRequest(Request.Method.POST, Constantes.URL_HISTORIES, jsonBody,
                new Response.Listener<JSONObject>() {
                    @SuppressLint("ResourceAsColor")
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d("medias_url_cy", "11111");
                        Log.e("Response", response.toString());

                        try {
                            if (response.getInt("responseCode") == 0) {
                                final JSONArray activity = response.getJSONArray("body");

                                List<History> liste_activity = new ArrayList<>();

                                for (int i = 0; i < activity.length(); i++) {
                                    History row = new History();

                                    @SuppressLint("SimpleDateFormat") DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                                    Date creation = (Date)formatter.parse(activity.getJSONObject(i).getString("createdAt"));
                                    long output_creation = creation.getTime() / 1000L;
                                    String str_creation = Long.toString(output_creation);
                                    long timestamp_creation = Long.parseLong(str_creation) * 1000;
                                    byte[] titleByte = activity.getJSONObject(i).getString("title").getBytes(StandardCharsets.UTF_8);
                                    byte[] contentByte = activity.getJSONObject(i).getString("content").getBytes(StandardCharsets.UTF_8);
                                    row.setTitre(new String(titleByte, StandardCharsets.UTF_8));
                                    row.setContenu(new String(contentByte, StandardCharsets.UTF_8));
                                    row.setCreation(timestamp_creation);
                                    row.setEtat(activity.getJSONObject(i).getBoolean("state") ? 1 : 0);

                                    historyList.add(row);
                                }
                                if (historyList.isEmpty()) {
                                    adapter.notifyDataSetChanged();
                                    ifNoHistory.setVisibility(View.VISIBLE);
                                    recyclerView.setVisibility(View.INVISIBLE);
                                } else {
                                    adapter.notifyDataSetChanged();
                                    ifNoHistory.setVisibility(View.INVISIBLE);
                                    recyclerView.setVisibility(View.VISIBLE);
                                }

                                progressDialog.dismiss();
                            }
                        } catch (Throwable t) {
                            Log.d("errornscription", String.valueOf(t.getCause()));
                            Log.e("My_App", response.toString());
                            Log.e("Connexion", t.getMessage());
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {
                        progressDialog.dismiss();
                        // error
                        Log.d("Error.Inscription", String.valueOf(volleyError.getMessage()));
                        CoordinatorLayout mainLayout = findViewById(R.id.layout_history);

                        String message;
                        if (volleyError instanceof NetworkError || volleyError instanceof AuthFailureError || volleyError instanceof TimeoutError) {
                            message = "Aucune connexion Internet!";
                            Snackbar snackbar = Snackbar
                                    .make(mainLayout, message, Snackbar.LENGTH_INDEFINITE)
                                    .setAction("REESSAYER", new View.OnClickListener() {
                                        @Override
                                        public void onClick(View view) {
                                            refreshAccessToken( HistoryActivity.this, new HistoryActivity.TokenRefreshListener() {
                                                @Override
                                                public void onTokenRefreshed(boolean success) {
                                                    if (success) {
                                                        afficherActivity();
                                                    }
                                                }
                                            });

                                        }
                                    });
                            snackbar.getView().setBackgroundColor(ContextCompat.getColor(HistoryActivity.this, R.color.colorGray));
                            snackbar.setActionTextColor(getResources().getColor(R.color.colorPrimary));
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
                                            afficherActivity();
                                        }
                                    });
                            snackbar.getView().setBackgroundColor(ContextCompat.getColor(HistoryActivity.this, R.color.colorGray));
                            snackbar.setActionTextColor(getResources().getColor(R.color.colorPrimary));
                            View sbView = snackbar.getView();
                            TextView textView = (TextView) sbView.findViewById(com.google.android.material.R.id.snackbar_text);
                            textView.setTextColor(Color.WHITE);
                            snackbar.show();
                        } else if (volleyError instanceof ParseError) {
                            message = "Une erreur est survenue!";
                            Snackbar snackbar = Snackbar
                                    .make(mainLayout, message, Snackbar.LENGTH_INDEFINITE)
                                    .setAction("REESSAYER", new View.OnClickListener() {
                                        @Override
                                        public void onClick(View view) {
                                            afficherActivity();
                                        }
                                    });
                            snackbar.getView().setBackgroundColor(ContextCompat.getColor(HistoryActivity.this, R.color.colorGray));
                            snackbar.setActionTextColor(getResources().getColor(R.color.colorPrimary));
                            View sbView = snackbar.getView();
                            TextView textView = (TextView) sbView.findViewById(com.google.android.material.R.id.snackbar_text);
                            textView.setTextColor(Color.WHITE);
                            snackbar.show();
                        }
                    }
                }
        ) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                accessToken = Prefs.getString(TOKEN,"");
                headers.put("Content-Type", "application/json");
                headers.put("Authorization", "Bearer " + accessToken); // Ajoute le token ici
                return headers;
            }
        };

        jsonRequest.setRetryPolicy(new DefaultRetryPolicy(
                50000,
                -1,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        queue.add(jsonRequest);

        //initialize the progress dialog and show it
        progressDialog = new ProgressDialog(HistoryActivity.this);
        progressDialog.setCancelable(false);
        progressDialog.setMessage("Veuillez patienter SVP! Récupération de vos activités ...");
        progressDialog.show();
    }

    private void refreshAccessToken(Context context, HistoryActivity.TokenRefreshListener listener) {
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
                            accessToken = newAccessToken;
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


}