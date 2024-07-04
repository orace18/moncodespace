package com.sicmagroup.tondi;

import android.content.ContextWrapper;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.ogaclejapan.smarttablayout.SmartTabLayout;
import com.ogaclejapan.smarttablayout.utils.v4.FragmentPagerItemAdapter;
import com.ogaclejapan.smarttablayout.utils.v4.FragmentPagerItems;
import com.pixplicity.easyprefs.library.Prefs;

import static com.sicmagroup.tondi.Connexion.ID_UTILISATEUR_KEY;

public class MesRetraits extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_liste_retraits_2);
        new Prefs.Builder()
                .setContext(this)
                .setMode(ContextWrapper.MODE_PRIVATE)
                .setPrefsName(getPackageName())
                .setUseDefaultSharedPreference(true)
                .build();
       ;
       Bundle bundle =  new Bundle();
       bundle.putInt("pos",1);
        Bundle bundle1 =  new Bundle();
        bundle1.putInt("pos",3);
        FragmentPagerItemAdapter adapter = new FragmentPagerItemAdapter(
                getSupportFragmentManager(), FragmentPagerItems.with(this)
                .add("Tontines terminées", TontineFragment.class, bundle)
                .add("Codes retraits", RetraitFragment.class)
                .add("Déjà encaissées", TontineFragment.class,bundle1)
                .create());

        WrapContentHeightViewPager viewPager = (WrapContentHeightViewPager) findViewById(R.id.viewpager);
        viewPager.setAdapter(adapter);

        SmartTabLayout viewPagerTab = (SmartTabLayout) findViewById(R.id.viewpagertab);
        viewPagerTab.setViewPager(viewPager);

//        Button btn_deconnexion = (Button)findViewById(R.id.btn_deconnexion);
//        btn_deconnexion.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                // TODO Auto-generated method stub
//                alertView("Déconnexion","Êtes vous sûr de vouloir vous déconnecter?");
//
//            }
//        });
//
//        Button btn_accueil = findViewById(R.id.btn_accueil);
//        btn_accueil.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                MesRetraits.this.finish();
//                startActivity(new Intent(MesRetraits.this, Dashboard.class));
//            }
//        });
//
//        Button btn_about = findViewById(R.id.btn_about);
//        btn_about.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                MesRetraits.this.finish();
//                startActivity(new Intent(MesRetraits.this, About_us.class));
//            }
//        });

        FloatingActionButton btn_cmt_retirer = (FloatingActionButton) findViewById(R.id.btn_cmt_retirer);
        btn_cmt_retirer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                //MesRetraits.this.finish();
                startActivity(new Intent(MesRetraits.this,Ccm.class));
            }
        });
        Button back_to = findViewById(R.id.back_to);
        back_to.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                MesRetraits.this.finish();
                startActivity(new Intent(MesRetraits.this,Dashboard.class));
            }
        });

    }

    @Override
    public void onBackPressed() {
        // your code.
        MesRetraits.this.finish();
        startActivity(new Intent(MesRetraits.this,Dashboard.class));
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
                        MesRetraits.this.finish();
                        startActivity(new Intent(MesRetraits.this,Connexion.class));
                    }
                }).show();
    }
}
