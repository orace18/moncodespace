package com.sicmagroup.tondi;

import android.annotation.SuppressLint;
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
import com.orm.query.Condition;
import com.orm.query.Select;
import com.pixplicity.easyprefs.library.Prefs;

import java.util.List;

import static com.sicmagroup.tondi.utils.Constantes.ACCESS_NOUVELLE_TONTINE;
import static com.sicmagroup.tondi.Connexion.ID_UTILISATEUR_KEY;
import static com.sicmagroup.tondi.utils.Constantes.STATUT_UTILISATEUR;

public class MesTontines extends AppCompatActivity {

    @SuppressLint("SuspiciousIndentation")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_liste_tontines_2);

        new Prefs.Builder()
                .setContext(this)
                .setMode(ContextWrapper.MODE_PRIVATE)
                .setPrefsName(getPackageName())
                .setUseDefaultSharedPreference(true)
                .build();

        FragmentPagerItemAdapter adapter = new FragmentPagerItemAdapter(
                getSupportFragmentManager(), FragmentPagerItems.with(this)
                .add("En cours", TontineFragment.class)
                .add("Terminées", TontineFragment.class)
                .add("Marchand", TontineFragment.class)
                .add("Encaissées", TontineFragment.class)
                .create());

        WrapContentHeightViewPager viewPager = (WrapContentHeightViewPager) findViewById(R.id.viewpager);
        viewPager.setAdapter(adapter);

        SmartTabLayout viewPagerTab = (SmartTabLayout) findViewById(R.id.viewpagertab);
        viewPagerTab.setViewPager(viewPager);

        Intent getIntent = getIntent();
        String tab_name = getIntent.getStringExtra("tab_name");
//        if (tab_name!=null){
//            //Toast.makeText(getApplicationContext(),"s:"+tab_name,Toast.LENGTH_LONG).show();
//            if (tab_name.equals("terminee")){
//                viewPager.setCurrentItem(1);
//                //viewPagerTab.getTabAt(1).setSelected(true);
//            }
//
//            if (tab_name.equals("encaissee")){
//                viewPager.setCurrentItem(3);
//                //viewPagerTab.getTabAt(2).setSelected(true);
//            }
//            if (tab_name.equals("en attente")){
//                viewPager.setCurrentItem(2);
//            }
//        }

//
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
        Button back_to = findViewById(R.id.back_to);
        back_to.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                MesTontines.this.finish();
                startActivity(new Intent(MesTontines.this,Dashboard.class));
            }
        });
//
//        Button btn_accueil = findViewById(R.id.btn_accueil);
//        btn_accueil.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                MesTontines.this.finish();
//                startActivity(new Intent(MesTontines.this, Dashboard.class));
//            }
//        });

        FloatingActionButton btn_nouvelle_tontine = (FloatingActionButton)  findViewById(R.id.btn_nouvelle_tontine);

        List<Tontine> liste_tontines_user = Select.from(Tontine.class)
                .where(Condition.prop("id_utilisateur").eq(Prefs.getString(ID_UTILISATEUR_KEY,"")))
                .list();
        if (liste_tontines_user.size()==0){

            // afficher directement la page de nouvelle tontine

            if (!Prefs.getBoolean(ACCESS_NOUVELLE_TONTINE,true))
            startActivity(new Intent(MesTontines.this,NouvelleTontine.class));

        }
        btn_nouvelle_tontine.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent i = new Intent(MesTontines.this,NouvelleTontine.class);

                startActivity(i);
            }
        });

        //Si l'utilisateur est désactivé de façon temporaire
        if (Prefs.contains(STATUT_UTILISATEUR))
        {
            switch (Prefs.getString(STATUT_UTILISATEUR, null)) {
                case "desactive temp":
                    btn_nouvelle_tontine.setEnabled(false);
                    break;
                case "active":
                    btn_nouvelle_tontine.setEnabled(true);
                    break;
            }
        }

//        Button btn_about = findViewById(R.id.btn_about);
//        btn_about.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                MesTontines.this.finish();
//                startActivity(new Intent(MesTontines.this, About_us.class));
//            }
//        });


    }

    @Override
    public void onBackPressed() {
        // your code.
        MesTontines.this.finish();
        startActivity(new Intent(MesTontines.this,Dashboard.class));
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
                        MesTontines.this.finish();
                        startActivity(new Intent(MesTontines.this,Connexion.class));
                    }
                }).show();
    }
}
