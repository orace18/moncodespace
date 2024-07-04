package com.sicmagroup.tondi;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.gson.Gson;
import com.orm.query.Condition;
import com.orm.query.Select;
import com.pixplicity.easyprefs.library.Prefs;
import com.sicmagroup.formmaster.FormBuilder;
import com.sicmagroup.formmaster.model.BaseFormElement;
import com.sicmagroup.formmaster.model.FormElementTextPassword;
import com.sicmagroup.tondi.utils.Constantes;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import static com.sicmagroup.tondi.Connexion.ID_UTILISATEUR_KEY;
import static com.sicmagroup.tondi.Connexion.PIN_KEY;

public class Message_changer_pin extends AppCompatActivity {

    private FormBuilder frm_pin_builder;
    private RecyclerView form_pin;
    private RecyclerView recyclerView;
    private static final int TAG_OLD_PASS = 41;
    private static final int TAG_PASS = 42;
    private static final int TAG_PIN = 44;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // remove title
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.message_changer_pin);

        Intent getIntent = getIntent();
        String msg_desc = getIntent.getStringExtra("msg_desc");
        String str_class = "";
        str_class = getIntent.getStringExtra("class");
        //Toast.makeText(getApplicationContext(),"id:"+id_retrait,Toast.LENGTH_LONG).show();
        TextView msg_desc_text = findViewById(R.id.msg_desc);
        msg_desc_text.setText(msg_desc);
        //justify(cgu_text);
        ImageView i = findViewById(R.id.msg_check);
        i.setColorFilter(getResources().getColor(R.color.fbutton_color_green_sea));
        Button btn_fermer = findViewById(R.id.btn_oui);
        final String finalStr_class = str_class;
        btn_fermer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                Message_changer_pin.this.finish();

                if(finalStr_class!=null){
                    List<Tontine> tontines;
                    tontines = Select.from(Tontine.class)
                            .where(Condition.prop("id_utilisateur").eq(Prefs.getString(ID_UTILISATEUR_KEY,null)))
                            .list();
                    if(tontines.size() == 0)
                    {
                        Message_changer_pin.this.finish();
                        Intent intent=new Intent(Message_changer_pin.this,NouvelleTontine.class);
                        intent.putExtra("first_versement", true);
                        startActivity(intent);
                    } else {
                        Intent i = new Intent();
                        i.setClassName(getApplicationContext(), finalStr_class);
                        i.putExtra("tst","jeorhief");
                        startActivity(i);
                    }

                }

            }
        });

        /*Button btn_changer_pin = findViewById(R.id.btn_changer);
        btn_changer_pin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                //Message_changer_pin.this.finish();
                //Intent i = new Intent(Message_changer_pin.this,MonCompte.class);
                //startActivity(i);
                ViewDialog alert = new ViewDialog();
                alert.showDialog(Message_changer_pin.this);
            }
        });*/



    }

    private void carte(Long id_tontine){
        //mContext.startActivity(new Intent(mContext,Carte.class));
        //Toast.makeText(mContext,"id_tontineeee="+Integer.parseInt(String.valueOf(id_tontine)),Toast.LENGTH_LONG).show();
        Intent intent = new Intent(new Intent(Message_changer_pin.this,CarteMain.class));
        intent.putExtra("id_tontine",Integer.parseInt(String.valueOf(id_tontine)));
        Message_changer_pin.this.startActivity(intent);
    }
    @Override
    public void onBackPressed() {
        Message_changer_pin.this.finishAffinity();
    }

    void setupFrmPin(Dialog d, Context c){
        form_pin =  d.findViewById(R.id.form_changer_pin);
        frm_pin_builder = new FormBuilder(c, form_pin);
        final FormElementTextPassword element4 = FormElementTextPassword.createInstance().setTag(TAG_OLD_PASS).setTitle("Ancien Pin").setHint("Renseigner").setRequired(true);
        final FormElementTextPassword element5 = FormElementTextPassword.createInstance().setTag(TAG_PASS).setTitle("Nouveau Pin").setHint("Renseigner").setRequired(true);
        final FormElementTextPassword element6 = FormElementTextPassword.createInstance().setTag(TAG_PIN).setTitle("Confirmer Pin").setHint("Renseigner").setRequired(true);
        List<BaseFormElement> formItems1 = new ArrayList<>();
        formItems1.add(element4);
        formItems1.add(element5);
        formItems1.add(element6);
        frm_pin_builder.addFormElements(formItems1);
    }

    public class ViewDialog {
        void showDialog(Activity activity){
            final Dialog dialog = new Dialog(activity);
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.setCancelable(true);
            dialog.setContentView(R.layout.changer_pin);
            //setupFrmPin(dialog,dialog.getContext());

            form_pin =  dialog.findViewById(R.id.form_changer_pin);
            frm_pin_builder = new FormBuilder(dialog.getContext(), form_pin);
            final FormElementTextPassword element4 = FormElementTextPassword.createInstance().setTag(TAG_OLD_PASS).setTitle("Ancien Pin").setHint("Renseigner").setRequired(true);
            final FormElementTextPassword element5 = FormElementTextPassword.createInstance().setTag(TAG_PASS).setTitle("Nouveau Pin").setHint("Renseigner").setRequired(true);
            final FormElementTextPassword element6 = FormElementTextPassword.createInstance().setTag(TAG_PIN).setTitle("Confirmer Pin").setHint("Renseigner").setRequired(true);
            List<BaseFormElement> formItems1 = new ArrayList<>();
            formItems1.add(element4);
            formItems1.add(element5);
            formItems1.add(element6);
            frm_pin_builder.addFormElements(formItems1);

            Button dialogButton = dialog.findViewById(R.id.btn_valider_changement);
            dialogButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //BaseFormElement pin = frm_pin_acces.getFormElement(TAG_PIN);
                    //String pin_value = pin.getValue();
                    if (!frm_pin_builder.isValidForm()) {
                        String msg = "Vous devez remplir tous les champs.";
                        alertView_simple("Erreurs dans le formulaire", msg);
                    } else {
                        boolean flag = false;
                        String msg = "";
                        String old_pass = Prefs.getString(PIN_KEY, null);
                        Connexion.AeSimpleSHA1 AeSimpleSHA1 = new Connexion.AeSimpleSHA1();
                        String ins_old_pass = element4.getValue();
                    /*try {
                        ins_old_pass = AeSimpleSHA1.md5(ins_old_pass);
                        ins_old_pass = AeSimpleSHA1.SHA1(ins_old_pass);
                        //Log.d("ins_old_pass",ins_old_pass);
                    } catch (NoSuchAlgorithmException e) {
                        e.printStackTrace();
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }*/
                        if (!ins_old_pass.equals(old_pass)) {
                            msg = msg + "> L'Ancien code PIN est incorrect. Veuillez réessayer SVP! \n";
                            flag = true;
                        }


                        if (!element5.getValue().equals(element6.getValue())) {
                            msg = msg + "> Le Nouveau Pin et Confirmer Pin ne correspondent pas. \n";
                            flag = true;
                        }

                        if (flag) {
                            alertView_simple("Erreurs dans le formulaire", msg);
                        } else {
                            dialog.dismiss();
                            savePin();
                            //alertView("Changement du PIN d'accès", "Êtes vous sûr de vouloir appliquer ces modifications?", "pin");
                        }

                    }
                    //savePin();


                }
            });
            //Toast.makeText(getApplicationContext(),"e",Toast.LENGTH_LONG).show();

            dialog.show();

        }

        private void alertView_simple( String title ,String message ) {
            AlertDialog.Builder dialog = new AlertDialog.Builder(Message_changer_pin.this);
            dialog.setTitle( title )
                    .setIcon(R.drawable.ic_warning)
                    .setMessage(message)
                    .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialoginterface, int i) {
                        }
                    }).show();
        }

        @SuppressLint("ResourceAsColor")
        private void savePin() {
            BaseFormElement pin = frm_pin_builder.getFormElement(TAG_PIN);
            final String pin_value = pin.getValue();

            Utilisateur utilisateur_modifie = Utilisateur.find(Utilisateur.class, "id_utilisateur = ?", Prefs.getString(ID_UTILISATEUR_KEY,null)).get(0);
            utilisateur_modifie.setPin_acces(pin_value);
            utilisateur_modifie.save();
            Long id_user = utilisateur_modifie.getId();
            if (id_user!=null){
                Prefs.putString("pin",pin_value);
                // enregistrer dans synchroniser
                Date currentTime = Calendar.getInstance().getTime();
                long output_maj=currentTime.getTime()/1000L;
                String str_maj=Long.toString(output_maj);
                long timestamp_maj = Long.parseLong(str_maj) * 1000;
                try {
                    Synchronisation new_sync = new Synchronisation();
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("a","pin#compte");
                    jsonObject.put("n",utilisateur_modifie.getNumero());
                    Gson gson = new Gson();
                    String pin_json = gson.toJson(utilisateur_modifie);
                    jsonObject.put("d",pin_json);
                    new_sync.setMaj(timestamp_maj);
                    new_sync.setStatut(0);
                    new_sync.setDonnees(jsonObject.toString());
                    new_sync.save();


                } catch (JSONException e) {
                    e.printStackTrace();
                }
                Utilitaire utilitaire = new Utilitaire(Message_changer_pin.this);
                if (utilitaire.isConnected()){
                    utilitaire.synchroniser_en_ligne();
                }
                String msg="Votre PIN d'Accès a été correctement modifié ";
                Intent i = new Intent(Message_changer_pin.this, Message_ok.class);
                i.putExtra("msg_desc",msg);
                i.putExtra("class","com.sicmagroup.tondi.Dashboard");
                startActivity(i);
            }else{
                String msg="Un problème est survenu lors de la modification. Veuillez réessayer plus tard dans le module Mon Compte ";
                Intent i = new Intent(Message_changer_pin.this, Message_non.class);
                i.putExtra("msg_desc",msg);
                i.putExtra("class","com.sicmagroup.tondi.Dashboard");
                startActivity(i);

            }

        }
    }

}
