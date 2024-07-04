package com.sicmagroup.tondi;

import static com.sicmagroup.tondi.Connexion.ID_UTILISATEUR_KEY;
import static com.sicmagroup.tondi.Connexion.PHOTO_CNI_KEY;
import static com.sicmagroup.tondi.Connexion.PHOTO_KEY;
import static com.sicmagroup.tondi.utils.Constantes.SERVEUR;
import static com.sicmagroup.tondi.utils.Constantes.STATUT_UTILISATEUR;

import android.app.Dialog;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;


import com.google.android.material.bottomnavigation.BottomNavigationView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.snackbar.Snackbar;
import com.orm.query.Condition;
import com.orm.query.Select;
import com.pixplicity.easyprefs.library.Prefs;
import com.sicmagroup.tondi.databinding.ActivityHomeBinding;
import com.sicmagroup.tondi.utils.Constantes;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.List;

public class Home extends AppCompatActivity {

    private com.sicmagroup.tondi.databinding.ActivityHomeBinding binding;
    ImageButton fab_deco;
    ImageView  compte_drawer;
    Utilitaire utilitaire;
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
        File mypath = new File(directory, photo_identite + ".png");
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
                    break;
                default:
                    navView.setSelectedItemId(R.id.navigation_home);
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
}