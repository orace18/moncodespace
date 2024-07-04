package com.sicmagroup.tondi;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.orm.query.Condition;
import com.orm.query.Select;
import com.pixplicity.easyprefs.library.Prefs;
import com.sicmagroup.tondi.utils.Constantes;

import java.util.List;

import static com.sicmagroup.tondi.Connexion.ID_UTILISATEUR_KEY;
import static com.sicmagroup.tondi.utils.Constantes.USER_CF_MDP_EXTRAT_KEY;
import static com.sicmagroup.tondi.utils.Constantes.USER_MDP_EXTRAT_KEY;
import static com.sicmagroup.tondi.utils.Constantes.USER_NOM_EXTRAT_KEY;
import static com.sicmagroup.tondi.utils.Constantes.USER_PRENOMS_EXTRAT_KEY;
import static com.sicmagroup.tondi.utils.Constantes.USER_TEL_EXTRAT_KEY;

public class Message_non extends AppCompatActivity {
    int mmi =0;
    String nom;
    String prenoms;
    String tel;
    String mdp;
    String cnf_mdp;
    Button btn_fermer_cgu;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // remove title
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.message_non);

        Intent getIntent = getIntent();
        String msg_desc = getIntent.getStringExtra("msg_desc");
        final int id_tontine = getIntent.getIntExtra("id_tontine",0);
        final int id_retrait = getIntent.getIntExtra("id_retrait",0);
        String str_class = "";
        str_class = getIntent.getStringExtra("class");
        if (getIntent.getExtras().containsKey("mmi")){
            if (getIntent.getStringExtra("mmi").equals("1")){
                mmi=1;
            }
        }

        //Toast.makeText(getApplicationContext(),"id:"+id_retrait,Toast.LENGTH_LONG).show();
        TextView msg_desc_text = findViewById(R.id.msg_desc);
        msg_desc_text.setText(msg_desc);
        //justify(cgu_text);
        ImageView i = findViewById(R.id.msg_check);
        i.setColorFilter(getResources().getColor(R.color.colorPrimaryDark));
        btn_fermer_cgu = findViewById(R.id.btn_oui);
        final String finalStr_class = str_class;
        btn_fermer_cgu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                Message_non.this.finish();
                if (id_tontine!=0){
                    Log.e("id_tontine_err", String.valueOf(id_tontine));
                    carte(id_tontine);

                }
                else if(id_retrait!=0){
                    Intent intent = new Intent(Message_non.this,Encaisser.class);
                    intent.putExtra("id_retrait",id_retrait);
                    startActivity(intent);
                    Message_non.this.finish();
                }

                else if(finalStr_class!=null){
                    Intent i = new Intent();
                    if (mmi==1){ //inscription
                        nom = getIntent().getStringExtra(USER_NOM_EXTRAT_KEY);
                        prenoms = getIntent().getStringExtra(USER_PRENOMS_EXTRAT_KEY);
                        tel = getIntent().getStringExtra(USER_TEL_EXTRAT_KEY);
                        mdp = getIntent().getStringExtra(USER_MDP_EXTRAT_KEY);
                        cnf_mdp = getIntent().getStringExtra(USER_CF_MDP_EXTRAT_KEY);
                        i.putExtra(USER_NOM_EXTRAT_KEY,nom);
                        i.putExtra(USER_PRENOMS_EXTRAT_KEY,prenoms);
                        i.putExtra(USER_TEL_EXTRAT_KEY,tel);
                        i.putExtra(USER_MDP_EXTRAT_KEY,mdp);
                        i.putExtra(USER_CF_MDP_EXTRAT_KEY,cnf_mdp);
                        i.setClassName(getApplicationContext(), finalStr_class);
                        startActivity(i);
                        Message_non.this.finish();
                    }
                    else if(finalStr_class.equals("com.sicmagroup.tondi.NouvelleTontine")){
                        List<Tontine> tontines;
                        tontines = Select.from(Tontine.class)
                                .where(Condition.prop("id_utilisateur").eq(Prefs.getString(ID_UTILISATEUR_KEY,null)))
                                .list();
                        if(tontines.size() == 0)
                            i.putExtra("first_versement", true);
                        i.setClassName(getApplicationContext(), finalStr_class);
                        startActivity(i);
                        Message_non.this.finish();
                    } else if(finalStr_class.equals("com.sicmagroup.tondi.Connexion")){
                        i.setClassName(getApplicationContext(), finalStr_class);
                        startActivity(i);
                        Message_non.this.finish();
                    } else {
                        Intent intent = new Intent(getApplicationContext(), Home.class);
                        switch (finalStr_class){
                            case "com.sicmagroup.tondi.MesTontines":

                                intent.putExtra(Constantes.BOTTOM_NAV_DESTINATION, Constantes.DESTINATION_TONTINES);
                                startActivity(intent);
                                Message_non.this.finish();
                                break;
                            case "com.sicmagroup.tondi.MesRetraits":
                                intent.putExtra(Constantes.BOTTOM_NAV_DESTINATION, Constantes.DESTINATION_RETRAITS);
                                startActivity(intent);
                                Message_non.this.finish();
                                break;
                            default:
                                startActivity(intent);
                                Message_non.this.finish();
                                break;
                        }
                    }

                }
                else {
                    startActivity(new Intent(getApplicationContext(), Home.class));
                    Message_non.this.finish();
                }
                /*try {
                    startActivity(new Intent(Message_ok.this, (Class<?>) Class.forName(str_class).newInstance()));
                } catch (IllegalAccessException | InstantiationException | ClassNotFoundException e) {
                    e.printStackTrace();
                }*/
            }
        });

    }

    private void carte(int id_tontine){
        //mContext.startActivity(new Intent(mContext,Carte.class));
        //Toast.makeText(mContext,"id_tontineeee="+Integer.parseInt(String.valueOf(id_tontine)),Toast.LENGTH_LONG).show();
        Intent intent = new Intent(new Intent(Message_non.this,CarteMain.class));
        intent.putExtra("id_tontine",id_tontine);
        Message_non.this.finish();
        startActivity(intent);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        btn_fermer_cgu.performClick();
    }
}
