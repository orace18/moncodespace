package com.sicmagroup.tondi;

import static com.sicmagroup.tondi.Connexion.NOM_KEY;
import static com.sicmagroup.tondi.Connexion.PHOTO_KEY;
import static com.sicmagroup.tondi.Connexion.PRENOMS_KEY;
import static com.sicmagroup.tondi.Connexion.TEL_KEY;
import static com.sicmagroup.tondi.utils.Constantes.SERVEUR;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.pixplicity.easyprefs.library.Prefs;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.text.DecimalFormat;

public class CompteDrawerActivity extends AppCompatActivity {
    ImageView close_drawer ;
  TextView user_name;
    TextView solde_text;
    ConstraintLayout btn_share;
    ConstraintLayout mon_compte;
    ConstraintLayout a_propos;
    ConstraintLayout cgu;
    //    Utilitaire util = new Utilitaire(this);
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_compte_drawer);

        close_drawer = (ImageView) findViewById(R.id.close_drawer);
        user_name = (TextView) findViewById(R.id.user_name);
        user_name.setText(Prefs.getString(NOM_KEY,"User")+" "+Prefs.getString(PRENOMS_KEY,"User")+"");
        solde_text =(TextView) findViewById(R.id.solde_drawer);
        Utilisateur u = new Utilisateur().getUser(Prefs.getString(TEL_KEY, null));
        solde_text.setText(String.valueOf(new DecimalFormat("##.##").format(u.getSolde()))+ " F CFA");
        close_drawer.setOnClickListener(new  View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CompteDrawerActivity.this.finish();

                overridePendingTransition(R.anim.anim_slide_in_left, R.anim.anim_slide_out_left);
            }
        });
        // Partager l'application
        btn_share =(ConstraintLayout) findViewById(R.id.share_apk);
        btn_share.setEnabled(true);
        btn_share.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent =new Intent();
                intent.setAction(Intent.ACTION_SEND);
                intent.putExtra(android.content.Intent.EXTRA_SUBJECT, "Android Studio Pro");

                intent.putExtra(Intent.EXTRA_TEXT,"Hey, moi aussi j'utilise cette application pour faire mes tontines et gérer mon épargne. Télécharge l'application COMUBA TONTINE DIGITALE et fait ta tontine. Clique sur ce lien pour aller dans playStore https://play.google.com/store/apps/details?id=com.sicmagroup.tondi");
                intent.setType("text/plain");
                startActivity(intent);
            }
        });
        //Mon compte
        mon_compte = (ConstraintLayout) findViewById(R.id.mon_compte);

        mon_compte.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(CompteDrawerActivity.this,MonCompte.class));
            }
        });
        // A Propos

        a_propos = (ConstraintLayout) findViewById(R.id.apropos);
        a_propos.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(CompteDrawerActivity.this,About_us.class));
            }
        });

        //CGU
        cgu = (ConstraintLayout) findViewById(R.id.cgu);
        cgu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(CompteDrawerActivity.this,CGU.class);
                intent.putExtra("origine", "compteDrawer");
                startActivity(intent);
            }
        });
        String medias_url =  SERVEUR + "/medias/";
        ImageView user_avatar = findViewById(R.id.user_avatar);
//        Log.e( "onCreate: ",Prefs.getString(PHOTO_KEY,null)+".JPG" );
//        Picasso.get().load(medias_url+ Prefs.getString(PHOTO_KEY,null)+".JPG").transform(new Dashboard.CircleTransform()).into(user_avatar);
////        util.loadImageFromStorage(Prefs.getString(PHOTO_KEY,null),getCurrentFocus());

        ContextWrapper cw = new ContextWrapper(this);
        File directory = cw.getDir("tontine_photos", Context.MODE_PRIVATE);
        String photo_identite = Prefs.getString(PHOTO_KEY, "");
        // Create imageDir
        File mypath = new File(directory, photo_identite + ".png");
        Picasso.get().load(mypath).transform(new Dashboard.CircleTransform()).into(user_avatar);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.anim_slide_in_left, R.anim.anim_slide_out_left);
    }
}