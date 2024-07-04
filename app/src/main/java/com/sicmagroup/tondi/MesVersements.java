package com.sicmagroup.tondi;

import android.content.ContextWrapper;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.orm.query.Condition;
import com.orm.query.Select;
import com.pixplicity.easyprefs.library.Prefs;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import com.sicmagroup.formmaster.FormBuilder;

import static com.sicmagroup.tondi.Connexion.ID_UTILISATEUR_KEY;
import static com.sicmagroup.tondi.utils.Constantes.SERVEUR;

public class MesVersements extends AppCompatActivity {
    TextView textView;
    private RecyclerView recyclerView;
    private FormBuilder mFormBuilder;
    private static final int TAG_PERIODE = 21;
    private static final int TAG_MISE = 22;
    private static final int TAG_MODE_COTISATION = 23;
    String url_afficher = SERVEUR+"/api/v1/versements/afficher";
    private ArrayList<Versement> versementList;
    private VersementAdapter adapter;
    private TextView ifNoHistory;
    Button sort_btn;
    Boolean sorted = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_versements_2);
        new Prefs.Builder()
                .setContext(this)
                .setMode(ContextWrapper.MODE_PRIVATE)
                .setPrefsName(getPackageName())
                .setUseDefaultSharedPreference(true)
                .build();

        recyclerView = (RecyclerView) findViewById(R.id.recycler_view_versements);
        ifNoHistory = (TextView) findViewById(R.id.ifNoHistoryMsg);
        sort_btn = (Button) findViewById(R.id.sort_versement);

        versementList = new ArrayList<>();
        adapter = new VersementAdapter(this, versementList);
        RecyclerView.LayoutManager mLayoutManager = new GridLayoutManager(this, 1);
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(adapter);
        afficherVersements();

        Button back_to = findViewById(R.id.back_to);
        back_to.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                MesVersements.this.finish();
                startActivity(new Intent(MesVersements.this,Dashboard.class));
            }
        });

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
//                MesVersements.this.finish();
//                startActivity(new Intent(MesVersements.this, Dashboard.class));
//            }
//        });

//        Button btn_about = findViewById(R.id.btn_about);
//        btn_about.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                MesVersements.this.finish();
//                startActivity(new Intent(MesVersements.this, About_us.class));
//            }
//        });

        sort_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!sorted)
                {
                    sorted = true;

                    Collections.reverse(versementList);
                    Toast.makeText(MesVersements.this, "Trier du plus récent au plus ancien", Toast.LENGTH_SHORT).show();
                    Log.d("trie", "!sorted");
                    sort_btn.setText("A-Z");
                }
                else {
                    sorted = false;
                    Collections.sort(versementList);
                    Toast.makeText(MesVersements.this, "Trier du plus ancien au plus récent", Toast.LENGTH_SHORT).show();
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
        MesVersements.this.finish();
        startActivity(new Intent(MesVersements.this,Dashboard.class));
    }

    private void afficherVersements() {

        Date currentTime = Calendar.getInstance().getTime();
        long output_creation=currentTime.getTime()/1000L;
        String str_creation=Long.toString(output_creation);
        long timestamp_creation = Long.parseLong(str_creation) * 1000;


        Utilisateur u = Utilisateur.find(Utilisateur.class, "id_utilisateur = ?", Prefs.getString(ID_UTILISATEUR_KEY,null)).get(0);
        List<Versement> liste_versements = Select.from(Versement.class)
                .where(Condition.prop("utilisateur").eq(u.getId())
                        //Condition.prop("creation").gt(),
                        //Condition.prop("creation").lt(timestamp_creation)
                        )
                .limit("40")
                .list();
        if (liste_versements!=null){

            Long creation;
            Tontine tontine;
            int mise;
            String montant;
            String versements;

            for (int i = 0; i < liste_versements.size(); i++) {
                Versement row = liste_versements.get(i);
                creation = row.getCreation();
                tontine = row.getTontine();
                montant = row.getMontant();

                Versement b = new Versement();
                b.setCreation(creation);
                b.setTontine(tontine);
                b.setMontant(montant);
                versementList.add(b);
            }

            adapter.notifyDataSetChanged();
            ifNoHistory.setVisibility(View.INVISIBLE);
            recyclerView.setVisibility(View.VISIBLE);
        }
        else
        {
            recyclerView.setVisibility(View.INVISIBLE);
            ifNoHistory.setVisibility(View.VISIBLE);
        }
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
                        MesVersements.this.finish();
                        startActivity(new Intent(MesVersements.this,Connexion.class));
                    }
                }).show();
    }
}
