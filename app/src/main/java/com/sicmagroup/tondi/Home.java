package com.sicmagroup.tondi;

import static com.sicmagroup.tondi.CodeOtpVerification.progressDialog;
import static com.sicmagroup.tondi.Connexion.ID_UTILISATEUR_KEY;
import static com.sicmagroup.tondi.Connexion.PHOTO_CNI_KEY;
import static com.sicmagroup.tondi.Connexion.PHOTO_KEY;
import static com.sicmagroup.tondi.Connexion.TEL_KEY;
import static com.sicmagroup.tondi.utils.Constantes.REFRESH_TOKEN;
import static com.sicmagroup.tondi.utils.Constantes.SERVEUR;
import static com.sicmagroup.tondi.utils.Constantes.STATUT_UTILISATEUR;
import static com.sicmagroup.tondi.utils.Constantes.TOKEN;
import static com.sicmagroup.tondi.utils.Constantes.accessToken;
import static com.sicmagroup.tondi.utils.Constantes.url_refresh_token;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageButton;
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
import com.google.android.material.bottomnavigation.BottomNavigationView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.content.ContextCompat;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.snackbar.Snackbar;
import com.orm.query.Condition;
import com.orm.query.Select;
import com.pixplicity.easyprefs.library.Prefs;
import com.sicmagroup.tondi.databinding.ActivityHomeBinding;
import com.sicmagroup.tondi.utils.Constantes;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Home extends AppCompatActivity {

    private com.sicmagroup.tondi.databinding.ActivityHomeBinding binding;
    ImageButton fab_deco;
    ImageView  compte_drawer;
    Utilitaire utilitaire;
    private RecyclerView recyclerViewHistory;
    private HistoryAdapter historyAdapter;
    private TextView ifNoHistory;
    private List<History> historyList;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityHomeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        new Prefs.Builder()
                .setContext(this)
                .setMode(ContextWrapper.MODE_PRIVATE)
                .setPrefsName(getPackageName())
                .setUseDefaultSharedPreference(true)
                .build();


        Log.e("La vue est crée", "Oui ! Home");
        recyclerViewHistory = findViewById(R.id.recycler_view_history);
        ifNoHistory = (TextView) findViewById(R.id.ifNoActivityMsg);

        if (recyclerViewHistory != null) {
            recyclerViewHistory.setLayoutManager(new LinearLayoutManager(this));
            // Set your adapter here as well
        } else {
            Log.e("HomeActivity", "RecyclerView is null. Please check the layout ID.");
        }


       // recyclerViewHistory.setLayoutManager(new LinearLayoutManager(this));

        historyList = new ArrayList<>();
        historyAdapter = new HistoryAdapter(this, historyList);
        recyclerViewHistory.setAdapter(historyAdapter);
        afficherDixHistorique();


          //rediriger vers inscription_next si il manque la photo ou une autre information
//        Log.e("home", String.valueOf(Prefs.getString(PHOTO_KEY, null) == "null"));
//
//        Log.e("home", String.valueOf(Prefs.getString(PHOTO_KEY, null)));
        if(Prefs.getString(PHOTO_KEY, null).equals("null") || Prefs.getString(PHOTO_CNI_KEY, "").isEmpty()){
            Intent inscription_next = new Intent(Home.this, Inscription_next.class);
            if(Prefs.contains(ID_UTILISATEUR_KEY))
                inscription_next.putExtra("id_utilisateur", Prefs.getString(ID_UTILISATEUR_KEY, ""));
            Home.this.finish();
            startActivity(inscription_next);
            Log.e("Home", "in");
        } else {
            Log.e("Home", "out");
            List<Tontine> tontines;
            tontines = Select.from(Tontine.class)
                    .where(Condition.prop("id_utilisateur").eq(Prefs.getString(ID_UTILISATEUR_KEY,null)))
                    .list();
            Log.e("Home", "out size = "+tontines.size());
//            tontines = (List<Tontine>) Tontine.findAll(Tontine.class);
//            Log.e("Home", "out size = "+tontines.size());
            if(tontines.size() == 0)
            {
                Home.this.finish();
                Intent intent=new Intent(Home.this,NouvelleTontine.class);
                intent.putExtra("first_versement", true);
                startActivity(intent);
            }
        }

        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);
        fab_deco = (ImageButton) findViewById(R.id.fab_deco);
        compte_drawer = (ImageView) findViewById(R.id.user_avatar);
        utilitaire = new Utilitaire(this);

        String medias_url =   SERVEUR + "/medias/";
        ImageView user_avatar = findViewById(R.id.user_avatar);
        Picasso.get().load(medias_url+ Prefs.getString(PHOTO_KEY,null)+".png").transform(new Dashboard.CircleTransform()).into(user_avatar);

        ContextWrapper cw = new ContextWrapper(this);
        File directory = cw.getDir("tontine_photos", Context.MODE_PRIVATE);
        String photo_identite = Prefs.getString(PHOTO_KEY, "");
        // Create imageDir
        File mypath = new File(directory, "user_avatar" + ".png");
        //File mypath = new File(directory, photo_identite + ".png");
        Picasso.get().load(mypath).transform(new Dashboard.CircleTransform()).into(user_avatar);
        BottomNavigationView navView = findViewById(R.id.nav_view);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_home, R.id.navigation_retraits, R.id.navigation_tontines, R.id.navigation_avances, R.id.navigation_plaintes)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_home);
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(binding.navView, navController);
        Intent intent = getIntent();
        if(intent.hasExtra(Constantes.BOTTOM_NAV_DESTINATION) && intent.getStringExtra(Constantes.BOTTOM_NAV_DESTINATION) != null){
            String destination = intent.getStringExtra(Constantes.BOTTOM_NAV_DESTINATION);
            switch (destination){
                case Constantes.DESTINATION_TONTINES:
                    navView.setSelectedItemId(R.id.navigation_tontines);
                    break;
                case Constantes.DESTINATION_RETRAITS:
                    navView.setSelectedItemId(R.id.navigation_retraits);
                    break;
                case Constantes.DESTINATION_AVANCES:
                    navView.setSelectedItemId(R.id.navigation_avances);
                    break;
                case Constantes.DESTINATION_PLAINTES:
                    navView.setSelectedItemId(R.id.navigation_plaintes);
                    break;
                case Constantes.DESTINATION_ACCUEIL:
                    navView.setSelectedItemId(R.id.navigation_home);
                    afficherDixHistorique();

                    break;
                default:
                    navView.setSelectedItemId(R.id.navigation_home);
                    afficherDixHistorique();
                    break;
            }
        }

        fab_deco.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // TODO Auto-generated method stub
                Log.e("Click","First");
                alertView("Déconnexion", "Êtes vous sûr de vouloir vous déconnecter?");

            }
        });
        compte_drawer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(Home.this, CompteDrawerActivity.class));
                overridePendingTransition(R.anim.anim_slide_in_right, R.anim.anim_slide_out_right);
            }
        });

        ImageButton refresh_db_btn = (ImageButton) findViewById(R.id.refresh_db_btn);
        refresh_db_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Snackbar.make(binding.getRoot() , "Syncronisation en cours",  Snackbar.LENGTH_LONG).show();
                utilitaire.refreshDatabse();

            }
        });



    }

    private void afficherDixHistorique(){

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
                        Log.d("medias", "10001");
                        Log.e("Response", response.toString());

                        try {
                            // Vérifier si la réponse est valide
                            if (response.getInt("responseCode") == 0) {
                                final JSONArray activity = response.getJSONArray("body");

                                // Liste temporaire pour stocker les activités
                                List<History> liste_activity = new ArrayList<>();

                                // Limiter à 10 éléments maximum
                                int maxItems = 10;
                                for (int i = 0; i < activity.length() && i < maxItems; i++) {
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

                                    liste_activity.add(row);
                                }

                                // Mettre à jour la liste d'activités dans le RecyclerView
                                if (liste_activity.isEmpty()) {
                                    historyAdapter.notifyDataSetChanged();
                                    ifNoHistory.setVisibility(View.VISIBLE);
                                    recyclerViewHistory.setVisibility(View.INVISIBLE);
                                } else {
                                    historyList.clear(); // Efface l'ancienne liste d'historiques
                                    historyList.addAll(liste_activity); // Ajout des 10 dernières activités
                                    historyAdapter.notifyDataSetChanged(); // Notifie l'adaptateur des changements
                                    ifNoHistory.setVisibility(View.INVISIBLE);
                                    recyclerViewHistory.setVisibility(View.VISIBLE);
                                }

                                // Masque la boîte de dialogue de progression
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
//                        progressDialog.dismiss();
                        // Gérer les erreurs de la requête
                        Log.d("Error.Inscription", String.valueOf(volleyError.getMessage()));
                        CoordinatorLayout mainLayout = findViewById(R.id.layout_history);

                        String message;
                        if (volleyError instanceof NetworkError || volleyError instanceof AuthFailureError || volleyError instanceof TimeoutError) {
                            message = "Aucune connexion Internet!";
                            //showSnackbar(mainLayout, message);
                        } else if (volleyError instanceof ServerError) {
                            message = "Impossible de contacter le serveur!";
                            showSnackbar(mainLayout, message);
                        } else if (volleyError instanceof ParseError) {
                            message = "Une erreur est survenue!";
                            showSnackbar(mainLayout, message);
                        }
                    }
                }
        ) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                headers.put("Content-Type", "application/json");
                headers.put("Authorization", "Bearer " + accessToken); // Ajouter le token ici
                return headers;
            }
        };

        // Configurer la politique de nouvelle tentative de la requête
        jsonRequest.setRetryPolicy(new DefaultRetryPolicy(
                50000,
                -1,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        // Ajouter la requête à la file de requêtes
        queue.add(jsonRequest);
//
//        // Initialiser la boîte de dialogue de progression et l'afficher
//        progressDialog = new ProgressDialog(Home.this);
//        progressDialog.setCancelable(false);
//        progressDialog.setMessage("Veuillez patienter SVP! Récupération de vos activités ...");
//        progressDialog.show();
    }

    // Fonction utilitaire pour afficher un Snackbar
    private void showSnackbar(CoordinatorLayout mainLayout, String message) {
        Snackbar snackbar = Snackbar
                .make(mainLayout, message, Snackbar.LENGTH_INDEFINITE)
                .setAction("REESSAYER", new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        refreshAccessToken( Home.this, new Home.TokenRefreshListener() {
                            @Override
                            public void onTokenRefreshed(boolean success) {
                                if (success) {
                                    afficherDixHistorique();

                                }
                            }
                        });
                    }
                });
        snackbar.getView().setBackgroundColor(ContextCompat.getColor(Home.this, R.color.colorGray));
        snackbar.setActionTextColor(getResources().getColor(R.color.colorPrimary));
        View sbView = snackbar.getView();
        TextView textView = sbView.findViewById(com.google.android.material.R.id.snackbar_text);
        textView.setTextColor(Color.WHITE);
        snackbar.show();


    }


    private void alertView( String title ,String message ) {
        Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(true);
        dialog.setContentView(R.layout.dialog_deconnexion);

        TextView titre = (TextView) dialog.findViewById(R.id.deco_title);
        titre.setText(title);
        TextView message_deco = (TextView) dialog.findViewById(R.id.deco_message);
        message_deco.setText(message);

        Button oui = (Button) dialog.findViewById(R.id.btn_oui);
        oui.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                Prefs.putString(ID_UTILISATEUR_KEY,null);
                startActivity(new Intent(Home.this,Connexion.class));
                Home.this.finish();
            }
        });

        Button non = (Button) dialog.findViewById(R.id.btn_non);
        non.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        dialog.show();

    }


    @Override
    protected void onDestroy() {
        super.onDestroy();

    }

    private void refreshAccessToken(Context context, Home.TokenRefreshListener listener) {
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