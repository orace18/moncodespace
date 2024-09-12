package com.sicmagroup.tondi;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import androidx.annotation.NonNull;

import com.android.volley.toolbox.JsonObjectRequest;
import com.google.android.material.snackbar.Snackbar;

import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
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
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;
import com.orm.SugarRecord;
import com.orm.query.Condition;
import com.orm.query.Select;
import com.pixplicity.easyprefs.library.Prefs;
import com.romellfudi.permission.PermissionService;
import com.sicmagroup.formmaster.model.FormElementPickerDate;
import com.sicmagroup.formmaster.model.FormElementTextSingleLine;
import com.sicmagroup.tondi.Enum.PeriodiciteEnum;
import com.sicmagroup.tondi.Enum.TontineEnum;
import com.sicmagroup.tondi.utils.Constantes;
import com.sicmagroup.ussdlibra.USSDController;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.sicmagroup.formmaster.FormBuilder;
import com.sicmagroup.formmaster.listener.OnFormElementValueChangedListener;
import com.sicmagroup.formmaster.model.BaseFormElement;
import com.sicmagroup.formmaster.model.FormElementPickerSingle;
import com.sicmagroup.formmaster.model.FormElementSwitch;
import com.sicmagroup.formmaster.model.FormElementTextNumber;

import static com.sicmagroup.tondi.Accueil.COTIS_INFO__KEY;
import static com.sicmagroup.tondi.Accueil.DUALSIM_INFO_KEY;
import static com.sicmagroup.tondi.Connexion.ACCESS_BOOL;
import static com.sicmagroup.tondi.utils.Constantes.ACCESS_NOUVELLE_TONTINE;
import static com.sicmagroup.tondi.Connexion.ACCESS_RETURNf_KEY;
import static com.sicmagroup.tondi.Connexion.ID_UTILISATEUR_KEY;
import static com.sicmagroup.tondi.Connexion.MOOV_TEST;
import static com.sicmagroup.tondi.Connexion.MTN_MECOTI;
import static com.sicmagroup.tondi.Connexion.MTN_TEST;
import static com.sicmagroup.tondi.Connexion.TEL_KEY;
import static com.sicmagroup.tondi.utils.Constantes.REFRESH_TOKEN;
import static com.sicmagroup.tondi.utils.Constantes.SERVEUR;
import static com.sicmagroup.tondi.utils.Constantes.TOKEN;
import static com.sicmagroup.tondi.utils.Constantes.accessToken;
import static com.sicmagroup.tondi.utils.Constantes.url_refresh_token;

public class NouvelleTontine extends AppCompatActivity  {
    TextView textView;
    private RecyclerView mRecyclerView;
    private FormBuilder mFormBuilder;
    private RecyclerView dateRecyclerView;
    private FormBuilder dateFormBuilder;
    private static final int TAG_DENOMINATION = 20;
    private static final int TAG_PERIODE = 21;
    private static final int TAG_MISE = 22;
    private static final int TAG_MODE_COTISATION = 23;
    private static final int TAG_DATE = 25;
    private static final int TAG_SWITCH_BLOQUE = 26;
    private USSDController ussdApi;
    String url_souscrire = SERVEUR+"/api/v1/tontines/souscrire";
    String url_afficher_sims = SERVEUR+"/api/v1/sims/afficher";
    String url_payer = SERVEUR+"/api/v1/versements/payer";
    String url_try = SERVEUR+"/api/v1/versemets/verifier_transaction";
    String url_verifier_statut = SERVEUR+"/api/v1/versements/verifier_transaction";
    String url_get_statut = SERVEUR+"/api/v1/versements/get_transaction_statut";
    static String TRANS_STR = "trans_str";
    ProgressDialog progressDialog;
    private HashMap<String, HashSet<String>> map;
    int id_tontine;
    int mise;
    int nb_vers_defaut = 0;
    int ussd_level =0;
    Utilitaire utilitaire;
    Long heure_transaction_global;
    boolean isNewTontine = false;
    FormElementPickerDate element5;
    FormElementSwitch element6;
    List<BaseFormElement> formItems;
    List<BaseFormElement> formDateItem;
    private final String TAG = "NouvelleTontine";

    @Override
    protected void onPause() {
        super.onPause();
        Log.d("onPause","back");
        //Prefs.putBoolean(ACCESS_BOOL,false);
        //Prefs.putString(ACCESS_RETURN_KEY,"");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d("onResume","fore");
        Prefs.putBoolean(ACCESS_BOOL,true);
        Prefs.putString(ACCESS_RETURNf_KEY,"com.sicmagroup.tondi.NouvelleTontine");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d("onStop","back");
        Prefs.putBoolean(ACCESS_BOOL,false);
        Prefs.putString(ACCESS_RETURNf_KEY,"");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d("onDestroy","back");
        Prefs.putBoolean(ACCESS_BOOL,false);
        Prefs.putString(ACCESS_RETURNf_KEY,"");
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nouvelle_tontine_2);
        new Prefs.Builder()
                .setContext(this)
                .setMode(ContextWrapper.MODE_PRIVATE)
                .setPrefsName(getPackageName())
                .setUseDefaultSharedPreference(true)
                .build();
        setupForm();
        element5 = FormElementPickerDate.createInstance().setTag(TAG_DATE).setHint("Date de déblocage").setRequired(true);
        dateRecyclerView = (RecyclerView) findViewById(R.id.recyclerView_date);
        dateFormBuilder = new FormBuilder(NouvelleTontine.this, dateRecyclerView);



        // Toast.makeText(this, "transref: "+Prefs.getString(TRANS_STR,null), Toast.LENGTH_SHORT).show();
        // classe de retour après activation des accessibiltés
        Prefs.putString(ACCESS_RETURNf_KEY,"com.sicmagroup.tondi.NouvelleTontine");
        Prefs.putBoolean(ACCESS_BOOL,true);
        Prefs.putBoolean(ACCESS_NOUVELLE_TONTINE,true);

        map = new HashMap<>();
        map.put("KEY_LOGIN", new HashSet<>(Arrays.asList("espere", "waiting", "loading", "esperando")));
        map.put("KEY_ERROR", new HashSet<>(Arrays.asList("problema", "problem", "error", "null")));
//        new PermissionService(this).request(
//                new String[]{/*Manifest.permission.CALL_PHONE,*/ Manifest.permission.READ_PHONE_STATE},
//                callback);

        utilitaire = new Utilitaire(this);

        Log.d("nouvelle_tontine_return", "access_bool:"+Prefs.getBoolean(ACCESS_BOOL, false));
        //ViewFlow viewFlow = findViewById(R.id.viewflow);
        //DiffAdapter myAdapter = new DiffAdapter(this);
        //viewFlow.setAdapter(myAdapter);


        //SmartTabLayout viewPagerTab = (SmartTabLayout) findViewById(R.id.viewpagertab);
        //viewPagerTab.setViewPager(viewPager);

//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//            requestPermissions(new String[]{CALL_PHONE}, 1);
//        }

        if(getIntent().hasExtra("first_versement"))
        {
            ImageView img = findViewById(R.id.alert_icon);
            img.setVisibility(View.VISIBLE);
            TextView first_versment_text = findViewById(R.id.first_versment_text);
            first_versment_text.setText("Première tontine pour finaliser l'inscription");
            first_versment_text.setVisibility(View.VISIBLE);
        }
        else
        {
            TextView first_versment_text = findViewById(R.id.first_versment_text);
            ImageView img = findViewById(R.id.alert_icon);
            img.setVisibility(View.GONE);
            first_versment_text.setVisibility(View.GONE);

        }
    }
    @Override
    public void onBackPressed() {
        if(!getIntent().hasExtra("first_versement"))
        {
            Dialog dialog = new Dialog(NouvelleTontine.this);
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.setCancelable(true);
            dialog.setContentView(R.layout.dialog_attention);

            TextView titre = (TextView) dialog.findViewById(R.id.deco_title);
            titre.setText("Attention");
            TextView message_deco = (TextView) dialog.findViewById(R.id.deco_message);
            message_deco.setText("Êtes-vous sur d'annuler la création de cette tontine?");

            Button oui = (Button) dialog.findViewById(R.id.btn_oui);
            oui.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialog.dismiss();
                    NouvelleTontine.this.finish();
//                                    startActivity(new Intent(NouvelleTontine.this, MesTontines.class));
                }
            });

            Button non = (Button) dialog.findViewById(R.id.btn_non);
            non.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialog.cancel();
                }
            });

            dialog.show();

        }
        else
        {
            Dialog dialog = new Dialog(NouvelleTontine.this);
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.setCancelable(true);
            dialog.setContentView(R.layout.dialog_attention);

            TextView titre = (TextView) dialog.findViewById(R.id.deco_title);
            titre.setText("Attention");
            TextView message_deco = (TextView) dialog.findViewById(R.id.deco_message);
            message_deco.setText("Êtes-vous sur de vouloir quitter l'application");

            Button oui = (Button) dialog.findViewById(R.id.btn_oui);
            oui.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialog.cancel();
                    NouvelleTontine.this.finishAffinity();
//                                    startActivity(new Intent(NouvelleTontine.this, MesTontines.class));
                }
            });

            Button non = (Button) dialog.findViewById(R.id.btn_non);
            non.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialog.cancel();
                }
            });

            dialog.show();

//            Toast.makeText(this, "Vous devez finalisez votre inscription avant de continuer.", Toast.LENGTH_SHORT).show();
        }

    }

    public class ViewDialog {

        void showDialog(Activity activity){
            final Dialog dialog = new Dialog(activity);
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);

            dialog.setCancelable(false);
            dialog.setContentView(R.layout.cotisation_auto_infos);

            CheckBox check_cotis_i = dialog.findViewById(R.id.check_cotis_i);
            check_cotis_i.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    // si coché
                    if (isChecked){
                        // mettre à jour la préférence cotis_infos_key
                        Prefs.putInt(COTIS_INFO__KEY,1);
                    }
                    // sinon
                    else{
                        // mettre à jour la préférence cotis_infos_key
                        Prefs.putInt(COTIS_INFO__KEY,0);
                    }
                }
            });
            Button dialogButton = (Button) dialog.findViewById(R.id.btn_oui);
            dialogButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialog.dismiss();
                }
            });
            //Toast.makeText(getApplicationContext(),"e",Toast.LENGTH_LONG).show();

            dialog.show();

        }
    }

    public class ViewOk {

        void showDialog(Activity activity, String desc){

            final Dialog dialog = new Dialog(activity);
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.setCancelable(false);
            dialog.setContentView(R.layout.message_ok);
            TextView t = dialog.findViewById(R.id.msg_desc);
            ImageView i = dialog.findViewById(R.id.msg_check);
            t.setText(desc);
            Button dialogButton = dialog.findViewById(R.id.btn_oui);
            i.setColorFilter(activity.getResources().getColor(R.color.fbutton_color_green_sea));

            dialogButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {


                    dialog.dismiss();

                }
            });
            //Toast.makeText(getApplicationContext(),"e",Toast.LENGTH_LONG).show();

            dialog.show();

        }
    }

    private void setupForm() {
        List<Tontine> last_tontine;
        int num_carnet = 1;
        last_tontine = Tontine.findWithQuery(Tontine.class, " SELECT * FROM Tontine where id_utilisateur="+Prefs.getString(ID_UTILISATEUR_KEY,null)+ " ORDER BY CAST(carnet AS DECIMAL(5,2))DESC ");
        if(last_tontine.size()>0){
            num_carnet=Integer.parseInt(last_tontine.get(0).getCarnet())+1;
//                    Log.e("derniere_tontine",""+(pos)+"//"+last_tontine.get(0).getCarnet());

        }
        mRecyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        mFormBuilder = new FormBuilder(this, mRecyclerView, new OnFormElementValueChangedListener() {
            @Override
            public void onValueChanged(BaseFormElement baseFormElement) {
                if (baseFormElement.getTag() == TAG_SWITCH_BLOQUE){
                    if(baseFormElement.getValue().equals("Oui")){
                        Dialog dialog_recapitulatif = new Dialog(NouvelleTontine.this);
                        dialog_recapitulatif.requestWindowFeature(Window.FEATURE_NO_TITLE);
                        dialog_recapitulatif.setCancelable(true);
                        dialog_recapitulatif.setContentView(R.layout.dialog_recapitlatif);

                        TextView titre = (TextView) dialog_recapitulatif.findViewById(R.id.deco_title);
                        titre.setText("Information");
                        TextView message_deco = (TextView) dialog_recapitulatif.findViewById(R.id.deco_message);

                        message_deco.setText("Une tontine bloquée ne peut-être encaissée qu'après la date de déblocage ou depuis une agence de COMUBA.");
                        ImageView imageView = (ImageView) dialog_recapitulatif.findViewById(R.id.imageView);
                        imageView.setImageResource(R.drawable.ic_info);
                        Button oui = (Button) dialog_recapitulatif.findViewById(R.id.btn_oui);
                        oui.setText("Ok");
                        oui.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                formDateItem = new ArrayList<>();
                                formDateItem.add(element5);
                                dateFormBuilder.addFormElements(formDateItem);
                                dateRecyclerView.setVisibility(View.VISIBLE);
                                dialog_recapitulatif.cancel();
//                                dialog_recapitulatif.cancel();
//                                formItems.add(element5);
//                                mRecyclerView.notify();
                            }
                        });

                        Button non = (Button) dialog_recapitulatif.findViewById(R.id.btn_non);
                        non.setVisibility(View.GONE);
                        non.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                baseFormElement.setValue("Non");
                                element6.setValue("Non");
                                dialog_recapitulatif.cancel();
                            }
                        });

                        dialog_recapitulatif.show();
                    } else {
                        dateRecyclerView.setVisibility(View.GONE);
                    }
                }
                //Toast.makeText(NouvelleTontine.this, baseFormElement.getValue(), Toast.LENGTH_LONG).show();
                if (baseFormElement.getTag()==TAG_MODE_COTISATION){
                    if (baseFormElement.getValue().equals("Oui")){

                        // si préférence ne pas afficher cette fenetre n'est pas defini
                        if (Prefs.getInt(COTIS_INFO__KEY,0)!=1){
                            // afficher la fenetre d'infos
                            NouvelleTontine.ViewDialog alert = new NouvelleTontine.ViewDialog();
                            alert.showDialog(NouvelleTontine.this);
                        }

                    }
                }
            }

        }
        );

        // single item picker input
        List<String> type_mise = new ArrayList<>();
        type_mise.add("Journalière");
        type_mise.add("Hebdomadaire");
        type_mise.add("Mensuelle");
        FormElementPickerSingle element1 = FormElementPickerSingle.createInstance().setTag(TAG_PERIODE).setTitle("Cliquer pour choisir la période des cotisations/mises").setHint("Périodicité").setOptions(type_mise).setPickerTitle("Choisir une période").setRequired(true);;
        FormElementTextNumber element2 = FormElementTextNumber.createInstance().setTag(TAG_MISE).setTitle("Montant de la cotisation/mise").setHint("Mise").setRequired(true);
// switch input
        final FormElementSwitch element3 = FormElementSwitch.createInstance().setTag(TAG_MODE_COTISATION).setTitle("Cotisations automatiques").setSwitchTexts("Oui", "Non");
        FormElementTextSingleLine element4 = FormElementTextSingleLine.createInstance().setTag(TAG_DENOMINATION).setTitle("Nom du carnet").setValue("Ma tontine "+num_carnet).setRequired(false);
        element6 = FormElementSwitch.createInstance().setTag(TAG_SWITCH_BLOQUE).setHint("Tontine bloquée").setTitle("Tontine bloquée ?").setSwitchTexts("Oui", "Non").setRequired(false);
        /*password.setInputType(InputType.TYPE_CLASS_TEXT |
                InputType.TYPE_TEXT_VARIATION_PASSWORD);*/

        formItems = new ArrayList<>();
        formItems.add(element4);
        formItems.add(element1);
        formItems.add(element2);
        formItems.add(element6);
//        formItems.add(element3);

        mFormBuilder.addFormElements(formItems);
        // mFormBuilder.refreshView();

        Button back_to = findViewById(R.id.back_to);
        back_to.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!getIntent().hasExtra("first_versement"))
                {

                    Dialog dialog = new Dialog(NouvelleTontine.this);
                    dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                    dialog.setCancelable(true);
                    dialog.setContentView(R.layout.dialog_attention);

                    TextView titre = (TextView) dialog.findViewById(R.id.deco_title);
                    titre.setText("Attention");
                    TextView message_deco = (TextView) dialog.findViewById(R.id.deco_message);
                    message_deco.setText("Êtes-vous sur d'annuler la création de cette tontine?");

                    Button oui = (Button) dialog.findViewById(R.id.btn_oui);
                    oui.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            dialog.dismiss();
                            NouvelleTontine.this.finish();
//                                    startActivity(new Intent(NouvelleTontine.this, MesTontines.class));
                        }
                    });

                    Button non = (Button) dialog.findViewById(R.id.btn_non);
                    non.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            dialog.cancel();
                        }
                    });

                    dialog.show();

                }
                else
                {
                    Dialog dialog = new Dialog(NouvelleTontine.this);
                    dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                    dialog.setCancelable(true);
                    dialog.setContentView(R.layout.dialog_attention);


                    TextView titre = (TextView) dialog.findViewById(R.id.deco_title);
                    titre.setText("Attention");
                    TextView message_deco = (TextView) dialog.findViewById(R.id.deco_message);
                    message_deco.setText("Êtes-vous sur de vouloir quitter l'application");

                    Button oui = (Button) dialog.findViewById(R.id.btn_oui);
                    oui.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            dialog.cancel();
                            NouvelleTontine.this.finishAffinity();
//                                    startActivity(new Intent(NouvelleTontine.this, MesTontines.class));
                        }
                    });

                    Button non = (Button) dialog.findViewById(R.id.btn_non);
                    non.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            dialog.cancel();
                        }
                    });

                    dialog.show();

//            Toast.makeText(this, "Vous devez finalisez votre inscription avant de continuer.", Toast.LENGTH_SHORT).show();
                }

                // TODO Auto-generated method stub

               /* NouvelleTontine.this.finish();
                startActivity(new Intent(NouvelleTontine.this,MesTontines.class));*/
            }
        });

        Button btn_enregistrer_tontine = (Button)findViewById(R.id.btn_enregistrer_tontine);
        btn_enregistrer_tontine.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                //NouvelleTontine.this.finish();
                //startActivity(new Intent(NouvelleTontine.this,MesTontines.class));
                Boolean form1IsValid = false;
                Boolean form2IsValid = false;
                if (!mFormBuilder.isValidForm()){
                    form1IsValid = false;
                } else {
                    form1IsValid = true;
                }
                if(dateRecyclerView.getVisibility() == View.VISIBLE){
                    if(!dateFormBuilder.isValidForm()){
                        form2IsValid = false;
                    } else {
                        BaseFormElement date_picked = dateFormBuilder.getFormElement(TAG_DATE);
                        String date_choosed = date_picked.getValue();
                        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
                        try {
                            Date date_deblocage = format.parse(date_choosed);
                            Date now = Calendar.getInstance().getTime();
                            Log.e(TAG, date_deblocage.before(now)+"");
                            if(date_deblocage.before(now)){
                                form2IsValid = false;
                            } else {
                                form2IsValid = true;
                            }
                        } catch(ParseException e){
                            e.printStackTrace();
                        }

                    }
                } else {
                    form2IsValid = true;
                }
                Log.e("nouvelleT", form2IsValid+" gg");
                if (form2IsValid && form1IsValid){


                    //Log.d("ekf,erf", String.valueOf(mFormBuilder.getFormElement(TAG_MODE_COTISATION).getValue()));
                    //Toast.makeText(getApplicationContext(),String.valueOf(mFormBuilder.getFormElement(TAG_MODE_COTISATION).getValue()),LENGTH_LONG).show();
                    heure_transaction_global = System.currentTimeMillis() / 1000L;
                    //alertView("souscription en cours","ok");
//                    afficher_sims();
                    //souscrire();
                    final Dialog dialog = new Dialog(NouvelleTontine.this);
                    souscrire(dialog);

                }else{
                    String msg = "Vous devez remplir tous les champs";
                    if (dateRecyclerView.getVisibility() == View.VISIBLE)
                        msg = msg + " et choisir une date de déblocage valide.";
                    else
                        msg = msg + ".";
                    alert_error("Erreurs dans le formulaire",msg);
                }
            }
        });
    }

    @SuppressLint("ResourceAsColor")
    private void souscrire(Dialog dialog) {
        Log.e("user", " in souscrire method");
        //Toast.makeText(getApplicationContext(),tag,Toast.LENGTH_LONG).show();
//        String reseau = SugarRecord.findById(Sim.class, Long.valueOf(tag)).getReseau();
        BaseFormElement periode = mFormBuilder.getFormElement(TAG_PERIODE);
        BaseFormElement mise = mFormBuilder.getFormElement(TAG_MISE);
//        BaseFormElement prelevement_auto = mFormBuilder.getFormElement(TAG_MODE_COTISATION);
        BaseFormElement denomination = mFormBuilder.getFormElement(TAG_DENOMINATION);

        final String periode_value = periode.getValue();
        final String mise_value = mise.getValue();
//        final String prelevement_auto_value = prelevement_auto.getValue();
        final String denomination_value = denomination.getValue();

        int prelevement_auto_int = 0;
//        String prelevement_auto_recap = "Manuel";
//        if (prelevement_auto_value.equals("Oui")){
//            prelevement_auto_int=1;
//            prelevement_auto_recap = "Automatique";
//        }

        //Vérifier si la mise est un Entier et si elle est positive strictement
        try {
            Integer.parseInt(mise_value);
            if(Integer.parseInt(mise_value) < 0 )
                alert_error("Erreur dans le formulaire", "Veuillez taper un montant entre 500 FCFA et 100000FCFA");
            else {
                final Dialog diag = dialog;

                final int finalPrelevement_auto_int = 0;

                Dialog dialog_recapitulatif = new Dialog(NouvelleTontine.this);
                dialog_recapitulatif.requestWindowFeature(Window.FEATURE_NO_TITLE);
                dialog_recapitulatif.setCancelable(true);
                dialog_recapitulatif.setContentView(R.layout.dialog_recapitlatif);


                TextView titre = (TextView) dialog_recapitulatif.findViewById(R.id.deco_title);
                titre.setText("Résumé");
                TextView message_deco = (TextView) dialog_recapitulatif.findViewById(R.id.deco_message);
                String message_recap;
                if(dateRecyclerView.getVisibility() == View.VISIBLE && dateFormBuilder.isValidForm()){
                    BaseFormElement dateDeblocageFormElmnt = dateFormBuilder.getFormElement(TAG_DATE);
                    final String dateDablocage = dateDeblocageFormElmnt.getValue();
                    message_recap = "Tontine "+periode_value+ "\nVous allez souscrire pour  "+
                            mise_value+ " FCFA.\nTontine bloquée jusqu'au "+dateDablocage+" \nVoulez-vous continuer ?";

                } else {
                    message_recap = "Tontine "+periode_value+ "\nVous allez souscrire pour  "+
                            mise_value+ " FCFA.\nVoulez-vous continuer ?";
                }
//                message_deco.setText("Tontine "+periode_value+ "\nVous allez souscrire pour  "+
//                        mise_value+ " FCFA en prélèvement "+ prelevement_auto_recap+".\nVoulez-vous continer ?");
                message_deco.setText(message_recap);
                ImageView imageView = (ImageView) dialog_recapitulatif.findViewById(R.id.imageView);
                imageView.setImageResource(R.drawable.ic_info);
                Button oui = (Button) dialog_recapitulatif.findViewById(R.id.btn_oui);
                oui.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog_recapitulatif.cancel();
                        Utilitaire utilitaire = new Utilitaire(NouvelleTontine.this);
                        if(utilitaire.isConnected()) {
                            if (dateRecyclerView.getVisibility() == View.VISIBLE && dateFormBuilder.isValidForm()){
                                BaseFormElement dateDeblocageFormElmnt = dateFormBuilder.getFormElement(TAG_DATE);
                                final String dateDablocage = dateDeblocageFormElmnt.getValue();
                                payViaInternetTontineBloque(diag, periode_value, finalPrelevement_auto_int, mise_value, denomination_value, dateDablocage);
                            } else {
                                pay_via_internet(diag, periode_value, finalPrelevement_auto_int, mise_value, denomination_value);
                            }
                        }

                    }
                });

                Button non = (Button) dialog_recapitulatif.findViewById(R.id.btn_non);
                non.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        dialog_recapitulatif.cancel();
                    }
                });

                dialog_recapitulatif.show();

            }
        }
        catch (NumberFormatException e){
            alert_error("Erreur dans le formulaire", "Veuillez taper un montant entre 1 FCFA et 100 000FCFA");
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
                        NouvelleTontine.this.finish();
                        startActivity(new Intent(NouvelleTontine.this,Connexion.class));
                    }
                }).show();
    }

    private void alert_error( String title ,String message ) {

        Dialog dialog = new Dialog(NouvelleTontine.this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(true);
        dialog.setContentView(R.layout.dialog_attention);

        TextView titre = (TextView) dialog.findViewById(R.id.deco_title);
        titre.setText(title);
        TextView message_deco = (TextView) dialog.findViewById(R.id.deco_message);
        message_deco.setText(message);

        Button oui = (Button) dialog.findViewById(R.id.btn_oui);
        oui.setText("OK");
        oui.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        Button non = (Button) dialog.findViewById(R.id.btn_non);
        non.setVisibility(View.GONE);

        dialog.show();

    }


    private void afficher_sims() {
        List<Utilisateur> utilisateurL = Utilisateur.find(Utilisateur.class, "id_utilisateur = ?", Prefs.getString(ID_UTILISATEUR_KEY,null));
        Log.e("user", utilisateurL.size()+" size ");
        if(utilisateurL.size() > 0){
            Utilisateur utilisateur = utilisateurL.get(0);
            List<Sim> sims = Sim.find(Sim.class, "utilisateur = ?", String.valueOf(utilisateur.getId()));
            Log.e("user", utilisateur.getId_utilisateur()+" id du guy ");
            NouvelleTontine.ViewSims alert = new NouvelleTontine.ViewSims(sims);
            alert.showDialog(NouvelleTontine.this);
        } else {
            final AlertDialog.Builder builder = new AlertDialog.Builder(NouvelleTontine.this);
            builder.setTitle("Erreur");
            builder.setMessage("Une erreur est surevenue, reconnectez-vous");
            builder.setNeutralButton("Ok", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
//                Toast.makeText(Inscription_next.this, "Finalisez votre inscription svp!", Toast.LENGTH_SHORT).show();
                    finishAffinity();
                    Prefs.putString(ID_UTILISATEUR_KEY, null);
                }
            });
            AlertDialog dialog = builder.create();
            dialog.show();
        }

    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }

    public class ViewSims {
        List<Sim> array;
        public ViewSims(List<Sim> array) {
            this.array=array;
        }

        @SuppressLint({"SetTextI18n", "RestrictedApi"})
        void showDialog(Activity activity){
            Log.e("use","id du guy on est dans showdialog");
            final Dialog dialog = new Dialog(activity);
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.setCancelable(true);
            dialog.setContentView(R.layout.sims_paiement);

            // lister les sims
            final RadioGroup sims_radio =  dialog.findViewById(R.id.sims_list);


            if (array!=null && array.size()!=0){
                for (int i =0;i<array.size();i++){
                    // Initialize a new RadioButton
                    RadioButton rb_flash = new RadioButton(getApplicationContext());
                    // Set a Text for new RadioButton
                    rb_flash.setTag(array.get(i).getId());
                    rb_flash.setText(array.get(i).getReseau()+" - "+array.get(i).getNumero());
                    //rb_flash.setText("flash");
                    // Set the text color of Radio Button
                    /*if (i==0){
                        rb_flash.setChecked(true);
                    }*/
                    rb_flash.setTextColor(Color.BLACK);
                    sims_radio.addView(rb_flash);
                }


            }

            Button btn_selectionner_sim = dialog.findViewById(R.id.btn_selectionner_sim);

            btn_selectionner_sim.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {

                    // get selected radio button from radioGroup
                    int selectedId = sims_radio.getCheckedRadioButtonId();
                    //Toast.makeText(getApplicationContext(),"selectedId:"+selectedId,Toast.LENGTH_LONG).show();

                    if(selectedId!=-1){
                        // find the radiobutton by returned id
                        RadioButton radio = dialog.findViewById(selectedId);
                        final Sim sim = Sim.findById(Sim.class, Long.valueOf(radio.getTag().toString()));
                        //Toast.makeText(getApplicationContext(),"id:"+ radio.getTag(),Toast.LENGTH_LONG).show();
//                        if(sim.getNumero().equals(Prefs.getString(TEL_KEY, "")))
////                            souscrire(radio.getTag().toString(),dialog);
//                        else
//                            Toast.makeText(getApplicationContext(),"Choisissez la bonne sim",Toast.LENGTH_LONG).show();
                    }else {
                        Toast.makeText(getApplicationContext(),"Veuillez sélectionner une Carte SIM",Toast.LENGTH_LONG).show();
                    }
                }

            });



        }
    }

    //Payer une tontine normale via internet
    private void pay_via_internet(final Dialog dialog, final String periode, final int prelevement_auto, final String mise_val, final String denomination_value){
        final BaseFormElement mise = mFormBuilder.getFormElement(TAG_MISE);
//        final Sim sim = Sim.findById(Sim.class, Long.valueOf(id_sim));
        final String montant_value = mise.getValue();
        final Intent svc = new Intent(NouvelleTontine.this, SplashLoadingService.class);
        svc.putExtra("texte","Le paiement pour la souscription à votre tontine est en cours...");
//        ussdApi = USSDController.getInstance(NouvelleTontine.this);
        payer(Prefs.getString(TEL_KEY, ""), montant_value, dialog, heure_transaction_global, periode, prelevement_auto, mise_val, denomination_value);

    }

    private void refreshAccessToken(Context context, NouvelleTontine.TokenRefreshListener listener) {
        RequestQueue queue = Volley.newRequestQueue(context);
        JSONObject params = new JSONObject();
        try {
            params.put("refreshToken", Prefs.getString(REFRESH_TOKEN, ""));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        JsonObjectRequest refreshRequest = new JsonObjectRequest(Request.Method.POST, url_refresh_token, params,
                new Response.Listener<JSONObject>() {
                    @SuppressLint("LongLogTag")
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            Log.e("La réponse du refresh token est:",response.toString());
                            String newAccessToken = response.getString("token");
                            String newRefreshToken = response.getString("refreshToken");
                            Prefs.putString(TOKEN, newAccessToken);
                            Prefs.putString(REFRESH_TOKEN, newRefreshToken);
                            Log.d("RefreshToken", "New Token: " + newAccessToken); // Log the new token
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
                });

        queue.add(refreshRequest);
    }

    //payer une tontine normale via internet  : appel a l'api
    private void payer(final String numero, final String montant, final Dialog dialog, final long heure_transaction, final String periode, final int prelevement_auto, final String mise_val, final String denomination_value) {
        RequestQueue queue = Volley.newRequestQueue(this);
        final String[] resultat = {""};
        Log.e("ResponseTagP", "testPayer");

        StringRequest postRequest = new StringRequest(Request.Method.POST, Constantes.URL_NEW_TONTINE,
                new Response.Listener<String>(){
                    @SuppressLint({"ResourceAsColor", "LongLogTag"})
                    @Override
                    public void onResponse(String response) {
                        Log.e("ResponseTagP", response);
                        if (response == null) {
                            Log.e("Le cas où la reponse est null:", response.toString());
                            handleServerError();
                        } else {
                            //handleResponse(response);
  //                          Log.e("Le cas où la reponse n'est pas null:", response.toString());

                            Log.e("La reponse:", resultat[0]);

                            resultat[0] = response;
                            try {

                                JSONObject result = new JSONObject(response);
                                //final JSONArray array = result.getJSONArray("data");
//                                Log.e("test", String.valueOf(result.getBoolean("success")));
                                Log.e("La reponse du serveur", result.toString());
                                int responseCode = result.getInt("responseCode");
                                if (responseCode == 0) {
                                    Date currentTime = Calendar.getInstance().getTime();
                                    long output_creation = currentTime.getTime() / 1000L;
                                    String str_creation = Long.toString(output_creation);
                                    long timestamp_creation = Long.parseLong(str_creation) * 1000;
                                    long output_maj = currentTime.getTime() / 1000L;
                                    String str_maj = Long.toString(output_maj);
                                    long timestamp_maj = Long.parseLong(str_maj) * 1000;

                                    Utilisateur u = Utilisateur.find(Utilisateur.class, "id_utilisateur = ?", Prefs.getString(ID_UTILISATEUR_KEY, null)).get(0);
                                    Log.e("test U", u.getId_utilisateur());
                                    progressDialog.dismiss();
                                    Long id_tontine = Long.valueOf(0);
                                    if (result.has("body")) {
                                        JSONObject tontineJson = result.getJSONObject("body");
                                        JSONArray resultat = tontineJson.getJSONArray("payments");
                                        Tontine nouvelle_tontine = new Tontine();
                                        nouvelle_tontine.setId_server(tontineJson.getString("id"));
//                                        Log.i("new_t_id", String.valueOf(nouvelle_tontine.getId_server()));

                                        nouvelle_tontine.setId_utilisateur(Prefs.getString(ID_UTILISATEUR_KEY, null));
//                                        Log.i("new_t_idUser", String.valueOf(nouvelle_tontine.getId_utilisateur()));

                                        nouvelle_tontine.setDenomination(tontineJson.getString("denomination"));
//                                        Log.i("new_t_deno", String.valueOf(nouvelle_tontine.getDenomination()));

                                        nouvelle_tontine.setPeriode(tontineJson.getString("periode"));
//                                        Log.i("new_t_periode", String.valueOf(nouvelle_tontine.getPeriode()));

                                        nouvelle_tontine.setMise(tontineJson.getInt("mise"));
//                                        Log.i("new_t_idMise", String.valueOf(nouvelle_tontine.getMise()));

                                        nouvelle_tontine.setPrelevement_auto(tontineJson.getBoolean("isAutoPayment"));
//                                        Log.i("new_t_idAuto", String.valueOf(nouvelle_tontine.getPrelevement_auto()));

//                                                    nouvelle_tontine.setIdSim(id_sim);
//                                        Log.i("new_t_idSim", String.valueOf(nouvelle_tontine.getIdSim()));

                                        nouvelle_tontine.setCarnet(String.valueOf(tontineJson.getString("carnet")));
//                                        Log.i("new_t_Carnet", String.valueOf(nouvelle_tontine.getCarnet()));

                                        nouvelle_tontine.setStatut(tontineJson.getString("state"));
//                                        Log.i("new_t_Statut", String.valueOf(nouvelle_tontine.getStatut()));

                                        nouvelle_tontine.setCreation(timestamp_creation);
//                                        Log.i("new_t_crea", String.valueOf(nouvelle_tontine.getCreation()));

                                        nouvelle_tontine.setMaj(timestamp_maj);
//                                        Log.i("new_t_Maj", String.valueOf(nouvelle_tontine.getMaj()));

                                        nouvelle_tontine.setContinuer(tontineJson.getLong("carnet"));

                                        nouvelle_tontine.save();
                                        id_tontine = nouvelle_tontine.getId();
                                        Tontine old = Tontine.findById(Tontine.class, id_tontine);
                                        Log.i("old_t", String.valueOf(old.getId()));


                                        if (nouvelle_tontine.getPrelevement_auto()) {
                                            // ajouter cotisations automatiques
                                            Cotis_Auto cotis_auto = new Cotis_Auto();
                                            cotis_auto.setTontine(nouvelle_tontine);
                                            cotis_auto.setUtilisateur(u);
                                            // maj des dates
                                            cotis_auto.setCreation(timestamp_creation);
                                            cotis_auto.setMaj(timestamp_creation);
                                            cotis_auto.save();
                                        }

                                        for (int i = 0; i < resultat.length(); i++) {
                                            JSONObject data = new JSONObject(resultat.get(i).toString());

                                            Versement n_versement = new Versement();
                                            n_versement.setFractionne(data.getString("isFractioned"));
//                                            Log.i("new_V_frac", String.valueOf(data.getString("fractionne")));

                                            n_versement.setIdVersement(data.getString("idVersement"));
//                                            Log.i("new_V_idV", String.valueOf(data.getString("id_versement")));

                                            n_versement.setMontant(data.getString("amount"));
//                                            Log.i("new_V_Montan", String.valueOf(data.getString("montant")));
                                            n_versement.setIdVersServ(data.getString("id"));
                                            n_versement.setCreation(timestamp_creation);
                                            n_versement.setStatut_paiement(data.getBoolean("isValide"));
                                            n_versement.setMaj(timestamp_maj);
                                            n_versement.setUtilisateur(u);
//                                            Log.e("new_V_id_user", n_versement.getUtilisateur().getId_utilisateur());
                                            Tontine cible = Tontine.findById(Tontine.class, id_tontine);
                                            n_versement.setTontine(cible);
                                            n_versement.save();
                                        }

                                    }
                                    if (id_tontine != 0) {

                                        Log.i("id_tontine", String.valueOf(id_tontine));
                                        //alertView("souscription ok","ok");
                                        NouvelleTontine.ViewOk alert = new NouvelleTontine.ViewOk();
                                        int nb_tontine = 0;
                                        int nb_tontine_encours = 0;
                                        int nb_tontine_terminees = 0;
                                        int nb_tontine_encaissees = 0;
                                        List<Tontine> liste_tontines = Select.from(Tontine.class)
                                                .where(Condition.prop("id_utilisateur").eq(Prefs.getString(ID_UTILISATEUR_KEY, null)))
                                                .groupBy("carnet")
                                                .list();
                                        nb_tontine = liste_tontines.size();
                                        List<Tontine> liste_tontines_encours = Select.from(Tontine.class)
                                                .where(Condition.prop("id_utilisateur").eq(Prefs.getString(ID_UTILISATEUR_KEY, null)))
                                                .where(Condition.prop("statut").eq(TontineEnum.IN_PROGRESS.toString()))
                                                .list();
                                        nb_tontine_encours = liste_tontines_encours.size();
                                        List<Tontine> liste_tontines_terminees = Select.from(Tontine.class)
                                                .where(Condition.prop("id_utilisateur").eq(Prefs.getString(ID_UTILISATEUR_KEY, null)))
                                                .where(Condition.prop("statut").eq(TontineEnum.COMPLETED.toString()))
                                                .list();
                                        nb_tontine_terminees = liste_tontines_terminees.size();
                                        List<Tontine> liste_tontines_encaissees = Select.from(Tontine.class)
                                                .where(Condition.prop("id_utilisateur").eq(Prefs.getString(ID_UTILISATEUR_KEY, null)))
                                                .where(Condition.prop("statut").eq(TontineEnum.COLLECTED.toString()))
                                                .list();
                                        nb_tontine_encaissees = liste_tontines_encaissees.size();
                                        String to_tontine_user = nb_tontine + " tontine";
                                        String to_tontine_encours = nb_tontine_encours + " tontine";
                                        String to_tontine_terminees = nb_tontine_terminees + " carte";
                                        String to_tontine_encaissees = nb_tontine_encaissees + " carte";
                                        if (nb_tontine > 1) {
                                            to_tontine_user = nb_tontine + " tontines";
                                        }
                                        if (nb_tontine_encours > 1) {
                                            to_tontine_encours = nb_tontine_encours + " tontines";
                                        }
                                        if (nb_tontine_terminees > 1) {
                                            to_tontine_terminees = nb_tontine_terminees + " cartes";
                                        }
                                        if (nb_tontine_encaissees > 1) {
                                            to_tontine_encaissees = nb_tontine_encaissees + " cartes";
                                        }
                                        //ussdApi.destroyInstance();
                                        //Log.d("monde","zz:"+id);

                                        //ajout
                                        isNewTontine = true;
                                        //ajout fin

                                        String msg = "Votre tontine a été correctement enregistrée. Vous avez maintenant au total " + to_tontine_user
                                                + " dont :\n\n • " + to_tontine_encours + " en cours; \n • " + to_tontine_terminees + " terminé(e)s et \n • " + to_tontine_encaissees + " encaissé(e)s";
                                        Intent i = new Intent(NouvelleTontine.this, Message_ok.class);
                                        i.putExtra("msg_desc", msg);
                                        //i.putExtra("id_tontine",Integer.parseInt(String.valueOf(id)));
                                        i.putExtra("id_tontine", id_tontine);
                                        i.putExtra("isNewTontine", isNewTontine);
//                                        i.putExtra("class","com.sicmagroup.tondi.CarteMain");
                                        startActivity(i);

                                        dialog.dismiss();
                                    } else {

                                        String msg = "Une erreur s'est produite. Veuillez réessayer SVP!";
                                        Intent i = new Intent(NouvelleTontine.this, Message_ok.class);
                                        i.putExtra("msg_desc", msg);
                                        i.putExtra("class", "com.sicmagroup.tondi.NouvelleTontine");
                                        startActivity(i);
                                    }

                                } else {
                                    progressDialog.dismiss();

                                    String msg = result.getString("body");
                                    Intent i = new Intent(NouvelleTontine.this, Message_non.class);
                                    i.putExtra("msg_desc", msg);
                                    i.putExtra("class", "com.sicmagroup.tondi.NouvelleTontine");
                                    startActivity(i);
                                }

                            } catch (JSONException e) {
                                throw new RuntimeException(e);
                            }
                        }
                    }
                },
                new Response.ErrorListener() {
                    private boolean tokenRefreshed = false;

                    @Override
                    public void onErrorResponse(VolleyError volleyError) {
                        progressDialog.dismiss();
                        if (volleyError instanceof AuthFailureError && !tokenRefreshed) {
                            // Rafraîchir le token et réessayer
                            refreshAccessToken(NouvelleTontine.this, new TokenRefreshListener() {
                                @Override
                                public void onTokenRefreshed(boolean success) {
                                    if (success) {
                                        tokenRefreshed = true;
                                        payer(numero, montant, dialog, heure_transaction, periode, prelevement_auto, mise_val, denomination_value);
                                    } else {
                                        handleAuthError();
                                    }
                                }
                            });
                        } else {
                            handleError(volleyError, numero, montant, dialog, heure_transaction, periode, prelevement_auto, mise_val, denomination_value);
                        }
                    }
                }) {

            @Override
            protected Map<String, String> getParams() {
                return null; // This method is not used when sending JSON body
            }

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                headers.put("Content-Type", "application/json");
                headers.put("Authorization", "Bearer " + Prefs.getString(TOKEN, "")); // Ajoute le token ici
                return headers;
            }
            @Override
            public byte[] getBody() {
                PeriodiciteEnum periodiciteEnum = PeriodiciteEnum.JOURNALIERE;
                switch (periode) {
                    case "Journalière":
                        periodiciteEnum = PeriodiciteEnum.JOURNALIERE;
                        break;
                    case "Mensuelle":
                        periodiciteEnum = PeriodiciteEnum.MENSUELLE;
                        break;
                    case "Hebdomadaire":
                        periodiciteEnum = PeriodiciteEnum.HEBDOMADAIRE;
                        break;
                }

                JSONObject jsonParams = new JSONObject();
                try {
                    jsonParams.put("denomination", denomination_value);
                    jsonParams.put("periode", periodiciteEnum.name());
                    jsonParams.put("mise", Integer.parseInt(mise_val));
                    jsonParams.put("customerNumber", numero);
                    jsonParams.put("autoPayment", "true");

                    Log.e("Le body", jsonParams.toString());
                    return jsonParams.toString().getBytes();
                } catch (JSONException e) {
                    e.printStackTrace();
                    return null;
                }
            }

            @Override
            public String getBodyContentType() {
                return "application/json; charset=utf-8";
            }
        };

        postRequest.setRetryPolicy(new DefaultRetryPolicy(
                80000,
                -1,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        queue.add(postRequest);

        progressDialog = new ProgressDialog(NouvelleTontine.this);
        progressDialog.setCancelable(false);
        progressDialog.setMessage("Veuillez patienter SVP! \n La souscription à la tontine est en cours...");
        progressDialog.show();
    }

    private void handleServerError() {
        progressDialog.dismiss();
        String msg = "Erreur, le serveur ne réponds pas. Patientez un instant puis réessayer svp.";
        Intent i = new Intent(NouvelleTontine.this, Message_non.class);
        i.putExtra("msg_desc", msg);
        i.putExtra("class", "com.sicmagroup.tondi.NouvelleTontine");
        startActivity(i);
    }

    private void handleAuthError() {
        String msg = "Erreur de rafraîchissement du token. Veuillez réessayer.";
        Intent i = new Intent(NouvelleTontine.this, Message_non.class);
        i.putExtra("msg_desc", msg);
        i.putExtra("class", "com.sicmagroup.tondi.NouvelleTontine");
        startActivity(i);
    }

    private void handleError(VolleyError volleyError, final String numero, final String montant, final Dialog dialog, final long heure_transaction, final String periode, final int prelevement_auto, final String mise_val, final String denomination_value) {
        String message;
        CoordinatorLayout mainLayout = findViewById(R.id.layout_tontine);
        if (volleyError instanceof NetworkError) {
            message = "Aucune connexion Internet!";
            showErrorSnackbar(mainLayout, message, numero, montant, dialog, heure_transaction, periode, prelevement_auto, mise_val, denomination_value);
        } else if (volleyError instanceof TimeoutError) {
            message = "Erreur de temporisation !";
            showErrorSnackbar(mainLayout, message, numero, montant, dialog, heure_transaction, periode, prelevement_auto, mise_val, denomination_value);
        } else if (volleyError instanceof ServerError) {
            message = "Impossible de contacter le serveur!";
            showErrorSnackbar(mainLayout, message, numero, montant, dialog, heure_transaction, periode, prelevement_auto, mise_val, denomination_value);
        } else if (volleyError instanceof ParseError) {
            message = "Une erreur est survenue! Contactez le service client.";
            showErrorSnackbar(mainLayout, message, numero, montant, dialog, heure_transaction, periode, prelevement_auto, mise_val, denomination_value);
        } else {
            Log.e("Error.Inscription", volleyError.getMessage());
        }
    }


    private void showErrorSnackbar(CoordinatorLayout layout, String message, final String numero, final String montant, final Dialog dialog, final long heure_transaction, final String periode, final int prelevement_auto, final String mise_val, final String denomination_value) {
        Snackbar snackbar = Snackbar.make(layout, message, Snackbar.LENGTH_INDEFINITE)
                .setAction("REESSAYER", new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        payer(numero, montant, dialog, heure_transaction, periode, prelevement_auto, mise_val, denomination_value);
                    }
                });
        snackbar.getView().setBackgroundColor(ContextCompat.getColor(NouvelleTontine.this, R.color.colorGray));
        snackbar.setActionTextColor(getResources().getColor(R.color.colorPrimaryDark));
        TextView textView = snackbar.getView().findViewById(com.google.android.material.R.id.snackbar_text);
        textView.setTextColor(Color.WHITE);
        snackbar.show();
    }

    private interface TokenRefreshListener {
        void onTokenRefreshed(boolean success);
    }

    //payer une tontine bloquée via internet : appel a la method de payment
    private void payViaInternetTontineBloque(final Dialog dialog, final String periode, final int prelevement_auto, final String mise_val, final String denomination_value, final String dateDeblocageValue){
        final BaseFormElement mise = mFormBuilder.getFormElement(TAG_MISE);
//        final Sim sim = Sim.findById(Sim.class, Long.valueOf(id_sim));
        final String montant_value = mise.getValue();
        String tel = Prefs.getString(TEL_KEY, "");
        Log.e("Le tel key est:", tel);
        payerTontineBloque(Prefs.getString(TEL_KEY, ""), montant_value, dialog, heure_transaction_global, periode, prelevement_auto, mise_val, denomination_value, dateDeblocageValue);
    }
    //payer une tontine bloquée : appel a l'API
   /* private void payerTontineBloque(final String numero, final String montant, final Dialog dialog, final long heure_transaction, final String periode, final int prelevement_auto, final String mise_val, final String denomination_value, final String dateDeblocageValue ) {
        RequestQueue queue = Volley.newRequestQueue(this);
        final String[] resultat = {""};
        Log.d("ResponseTagP", "testPayer");
        Log.e("Le token est:", accessToken);
        StringRequest postRequest = new StringRequest(Request.Method.POST, Constantes.URL_NEW_TONTINE
                ,
                new Response.Listener<String>(){
                    @SuppressLint("ResourceAsColor")
                    @Override
                    public void onResponse(String response) {
                        //progressDialog.dismiss();
                        // response
                        Log.d("ResponseTagP", response);
                        if(response == null)
                        {
                            progressDialog.dismiss();
                            String msg="Erreur, le serveur ne réponds pas. Patientez un instant puis réessayer svp.";
                            Intent i = new Intent(NouvelleTontine.this, Message_non.class);
                            i.putExtra("msg_desc", msg);
                            i.putExtra("class","com.sicmagroup.tondi.NouvelleTontine");
                            startActivity(i);
                        }
                        else {
                            resultat[0] = response;
                            try {

                                JSONObject result = new JSONObject(response);
                                //final JSONArray array = result.getJSONArray("data");
//                                Log.d("test", String.valueOf(result.getBoolean("success")));
                                int responseCode = result.getInt("responseCode");
                                if (responseCode == 0) {
                                    Date currentTime = Calendar.getInstance().getTime();
                                    long output_creation=currentTime.getTime()/1000L;
                                    String str_creation=Long.toString(output_creation);
                                    long timestamp_creation = Long.parseLong(str_creation) * 1000;
                                    long output_maj=currentTime.getTime()/1000L;
                                    String str_maj=Long.toString(output_maj);
                                    long timestamp_maj = Long.parseLong(str_maj) * 1000;
                                    Utilisateur u = Utilisateur.find(Utilisateur.class, "id_utilisateur = ?", Prefs.getString(ID_UTILISATEUR_KEY,null)).get(0);

                                    progressDialog.dismiss();
                                    Long id_tontine = Long.valueOf(0);
                                    if (result.has("body"))
                                    {
                                        JSONObject tontineJson = result.getJSONObject("body");
                                        JSONArray versements = tontineJson.getJSONArray("payments");
                                        Tontine nouvelle_tontine = new Tontine();
                                        nouvelle_tontine.setId_server(tontineJson.getString("id"));
//                                        Log.i("new_t_id", String.valueOf(nouvelle_tontine.getId_server()));

                                        nouvelle_tontine.setId_utilisateur(Prefs.getString(ID_UTILISATEUR_KEY,null));
//                                        Log.i("new_t_idUser", String.valueOf(nouvelle_tontine.getId_utilisateur()));

                                        nouvelle_tontine.setDenomination(tontineJson.getString("denomination"));
//                                        Log.i("new_t_deno", String.valueOf(nouvelle_tontine.getDenomination()));

                                        nouvelle_tontine.setPeriode(tontineJson.getString("periode"));
//                                        Log.i("new_t_periode", String.valueOf(nouvelle_tontine.getPeriode()));

                                        nouvelle_tontine.setMise(tontineJson.getInt("mise"));
//                                        Log.i("new_t_idMise", String.valueOf(nouvelle_tontine.getMise()));

                                        nouvelle_tontine.setPrelevement_auto(tontineJson.getBoolean("isAutoPayment"));
//                                        Log.i("new_t_idAuto", String.valueOf(nouvelle_tontine.getPrelevement_auto()));

//                                                    nouvelle_tontine.setIdSim(id_sim);
//                                                    Log.i("new_t_idSim", String.valueOf(nouvelle_tontine.getIdSim()));

                                        nouvelle_tontine.setCarnet(String.valueOf(tontineJson.getString("carnet")));
//                                        Log.i("new_t_Carnet", String.valueOf(nouvelle_tontine.getCarnet()));

                                        nouvelle_tontine.setStatut(tontineJson.getString("statut"));
//                                        Log.i("new_t_Statut", String.valueOf(nouvelle_tontine.getStatut()));

                                        nouvelle_tontine.setDateDeblocage(tontineJson.getString("date_deblocage"));

                                        nouvelle_tontine.setCreation(timestamp_creation);
//                                        Log.i("new_t_crea", String.valueOf(nouvelle_tontine.getCreation()));

                                        nouvelle_tontine.setMaj(timestamp_maj);
//                                        Log.i("new_t_Maj", String.valueOf(nouvelle_tontine.getMaj()));

                                        nouvelle_tontine.setContinuer(tontineJson.getLong("carnet"));

                                        nouvelle_tontine.save();
                                        id_tontine = nouvelle_tontine.getId();
                                        Tontine old = Tontine.findById(Tontine.class, id_tontine);
                                        Log.i("old_t", String.valueOf(old.getId()));


                                        if (nouvelle_tontine.getPrelevement_auto()) {
                                            // ajouter cotisations automatiques
                                            Cotis_Auto cotis_auto = new Cotis_Auto();
                                            cotis_auto.setTontine(nouvelle_tontine);
                                            cotis_auto.setUtilisateur(u);
                                            // maj des dates
                                            cotis_auto.setCreation(timestamp_creation);
                                            cotis_auto.setMaj(timestamp_creation);
                                            cotis_auto.save();
                                        }

                                        for (int i = 0; i < versements.length(); i++)
                                        {
                                            JSONObject data = new JSONObject(versements.get(i).toString());

                                            Versement n_versement = new Versement();
                                            n_versement.setFractionne(data.getString("isFractioned"));
                                            n_versement.setIdVersement(data.getString("idVersement"));
                                            n_versement.setMontant(data.getString("amount"));
                                            n_versement.setIdVersServ(data.getString("id"));
                                            n_versement.setCreation(timestamp_creation);
                                            n_versement.setMaj(timestamp_maj);
                                            n_versement.setUtilisateur(u);
                                            Tontine cible = Tontine.findById(Tontine.class, id_tontine);
                                            n_versement.setTontine(cible);
                                            n_versement.save();
                                        }
                                    }
                                    if (id_tontine!=0){
                                        Log.i("id_tontine", String.valueOf(id_tontine));
                                        //alertView("souscription ok","ok");
                                        NouvelleTontine.ViewOk alert = new NouvelleTontine.ViewOk();
                                        int nb_tontine = 0;
                                        int nb_tontine_encours = 0;
                                        int nb_tontine_terminees = 0;
                                        int nb_tontine_encaissees = 0;
                                        List<Tontine> liste_tontines = Select.from(Tontine.class)
                                                .where(Condition.prop("id_utilisateur").eq(Prefs.getString(ID_UTILISATEUR_KEY,null)) )
                                                .groupBy("carnet")
                                                .list();
                                        nb_tontine = liste_tontines.size();
                                        List<Tontine> liste_tontines_encours = Select.from(Tontine.class)
                                                .where(Condition.prop("id_utilisateur").eq(Prefs.getString(ID_UTILISATEUR_KEY,null)) )
                                                .where(Condition.prop("statut").eq(TontineEnum.IN_PROGRESS.toString()) )
                                                .list();
                                        nb_tontine_encours= liste_tontines_encours.size();
                                        List<Tontine> liste_tontines_terminees = Select.from(Tontine.class)
                                                .where(Condition.prop("id_utilisateur").eq(Prefs.getString(ID_UTILISATEUR_KEY,null)) )
                                                .where(Condition.prop("statut").eq(TontineEnum.COMPLETED.toString()) )
                                                .list();
                                        nb_tontine_terminees= liste_tontines_terminees.size();
                                        List<Tontine> liste_tontines_encaissees = Select.from(Tontine.class)
                                                .where(Condition.prop("id_utilisateur").eq(Prefs.getString(ID_UTILISATEUR_KEY,null)) )
                                                .where(Condition.prop("statut").eq(TontineEnum.COLLECTED.toString()) )
                                                .list();

                                        nb_tontine_encaissees= liste_tontines_encaissees.size();
                                        String to_tontine_user = nb_tontine+" tontine";
                                        String to_tontine_encours = nb_tontine_encours+" tontine";
                                        String to_tontine_terminees = nb_tontine_terminees+" carte";
                                        String to_tontine_encaissees = nb_tontine_encaissees+" carte";
                                        if (nb_tontine>1){
                                            to_tontine_user =nb_tontine+" tontines";
                                        }
                                        if (nb_tontine_encours>1){
                                            to_tontine_encours =nb_tontine_encours+" tontines";
                                        }
                                        if (nb_tontine_terminees>1){
                                            to_tontine_terminees =nb_tontine_terminees+" cartes";
                                        }
                                        if (nb_tontine_encaissees>1){
                                            to_tontine_encaissees =nb_tontine_encaissees+" cartes";
                                        }

                                        //ussdApi.destroyInstance();
                                        //Log.d("monde","zz:"+id);

                                        //ajout
                                        isNewTontine = true;
                                        //ajout fin

                                        String msg="Votre tontiner a été correctement enregistrée. Vous avez maintenant au total "+to_tontine_user
                                                +" dont :\n\n • "+to_tontine_encours+" en cours; \n • "+to_tontine_terminees+" terminé(e)s; \n • "+to_tontine_encaissees+" encaissé(e)s \n";
                                        Intent i = new Intent(NouvelleTontine.this, Message_ok.class);
                                        i.putExtra("msg_desc",msg);
                                        //i.putExtra("id_tontine",Integer.parseInt(String.valueOf(id)));
                                        i.putExtra("id_tontine",id_tontine);
                                        i.putExtra("isNewTontine",isNewTontine);
//                                        i.putExtra("class","com.sicmagroup.tondi.CarteMain");
                                        startActivity(i);

                                        dialog.dismiss();
                                    }else{

                                        String msg="Une erreur s'est produite. Veuillez réessayer SVP!";
                                        Intent i = new Intent(NouvelleTontine.this, Message_ok.class);
                                        i.putExtra("msg_desc",msg);
                                        i.putExtra("class","com.sicmagroup.tondi.NouvelleTontine");
                                        startActivity(i);
                                    }

                                } else {
                                    progressDialog.dismiss();

                                    String msg = result.getString("body");
                                    Intent i = new Intent(NouvelleTontine.this, Message_non.class);
                                    i.putExtra("msg_desc", msg);
                                    i.putExtra("class", "com.sicmagroup.tondi.NouvelleTontine");
                                    startActivity(i);
                                }


                            } catch (Throwable t) {
                                Log.d("errornscription", t.getMessage());
                            }
                        }
                    }
                },
                new Response.ErrorListener()
                {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {
                        progressDialog.dismiss();
                        // error
                        Log.e("Error.Inscription", String.valueOf(volleyError.getMessage()));
                        CoordinatorLayout mainLayout = (CoordinatorLayout)  findViewById(R.id.layout_tontine);

                        String message;
                        if (volleyError instanceof NetworkError) {

                            if (volleyError.networkResponse != null && volleyError.networkResponse.statusCode == 401) {
                                refreshTokenAndRetry(numero, montant, dialog, heure_transaction, periode, prelevement_auto, mise_val, denomination_value, dateDeblocageValue);
                            } else {
                                progressDialog.dismiss();
                                // other error handling code...
                            }

                        }
                        else if (volleyError instanceof AuthFailureError) {
                            //Toast.makeText(Inscription.this, "error:"+volleyError.getMessage(), Toast.LENGTH_SHORT).show();
                            //Log.d("VolleyError_Test",volleyError.getMessage());
                            message = "Erreur de temporisation !1";
                            Snackbar snackbar = Snackbar
                                    .make(mainLayout, message, Snackbar.LENGTH_INDEFINITE)
                                    .setAction("REESSAYER", new View.OnClickListener() {
                                        @Override
                                        public void onClick(View view) {
                                            refreshTokenAndRetry(numero, montant, dialog, heure_transaction, periode, prelevement_auto, mise_val, denomination_value, dateDeblocageValue);
                                           // payer(numero,montant,dialog, heure_transaction_global, periode, prelevement_auto, mise_val, denomination_value);
                                        }
                                    });
                            snackbar.getView().setBackgroundColor(ContextCompat.getColor(NouvelleTontine.this, R.color.colorGray));
                            // Changing message text color
                            snackbar.setActionTextColor(getResources().getColor(R.color.colorPrimaryDark));
                            // Changing action button text color
                            View sbView = snackbar.getView();
                            TextView textView = (TextView) sbView.findViewById(com.google.android.material.R.id.snackbar_text);
                            textView.setTextColor(Color.WHITE);
                            snackbar.show();

                        }else if (volleyError instanceof TimeoutError) {
                            //Toast.makeText(Inscription.this, "error:"+volleyError.getMessage(), Toast.LENGTH_SHORT).show();
                            //Log.d("VolleyError_Test",volleyError.getMessage());
                            message = "Erreur de temporisation !";
                            Snackbar snackbar = Snackbar
                                    .make(mainLayout, message, Snackbar.LENGTH_INDEFINITE)
                                    .setAction("REESSAYER", new View.OnClickListener() {
                                        @Override
                                        public void onClick(View view) {
                                            payer(numero,montant,dialog, heure_transaction_global, periode, prelevement_auto, mise_val, denomination_value);
                                        }
                                    });
                            snackbar.getView().setBackgroundColor(ContextCompat.getColor(NouvelleTontine.this, R.color.colorGray));
                            // Changing message text color
                            snackbar.setActionTextColor(getResources().getColor(R.color.colorPrimaryDark));
                            // Changing action button text color
                            View sbView = snackbar.getView();
                            TextView textView = (TextView) sbView.findViewById(com.google.android.material.R.id.snackbar_text);
                            textView.setTextColor(Color.WHITE);
                            snackbar.show();

                        }else if (volleyError instanceof ServerError) {
                            message = "Impossible de contacter le serveur!";
                            Snackbar snackbar = Snackbar
                                    .make(mainLayout, message, Snackbar.LENGTH_INDEFINITE)
                                    .setAction("REESSAYER", new View.OnClickListener() {
                                        @Override
                                        public void onClick(View view) {
                                            payer(numero,montant,dialog, heure_transaction_global, periode, prelevement_auto, mise_val, denomination_value);
                                        }
                                    });
                            snackbar.getView().setBackgroundColor(ContextCompat.getColor(NouvelleTontine.this, R.color.colorGray));
                            // Changing message text color
                            snackbar.setActionTextColor(getResources().getColor(R.color.colorPrimaryDark));
                            // Changing action button text color
                            View sbView = snackbar.getView();
                            TextView textView = (TextView) sbView.findViewById(com.google.android.material.R.id.snackbar_text);
                            textView.setTextColor(Color.WHITE);
                            snackbar.show();
                        }  else if (volleyError instanceof ParseError) {
                            //message = "Parsing error! Please try again later";
                            message = "Une erreur est survenue! Contactez le service client.";
                            Snackbar snackbar = Snackbar
                                    .make(mainLayout, message, Snackbar.LENGTH_INDEFINITE)
                                    .setAction("REESSAYER", new View.OnClickListener() {
                                        @Override
                                        public void onClick(View view) {
                                            payer(numero,montant,dialog,heure_transaction_global, periode, prelevement_auto, mise_val, denomination_value);
                                        }
                                    });
                            snackbar.getView().setBackgroundColor(ContextCompat.getColor(NouvelleTontine.this, R.color.colorGray));
                            // Changing message text color
                            snackbar.setActionTextColor(getResources().getColor(R.color.colorPrimaryDark));
                            // Changing action button text color
                            View sbView = snackbar.getView();
                            TextView textView = (TextView) sbView.findViewById(com.google.android.material.R.id.snackbar_text);
                            textView.setTextColor(Color.WHITE);
                            snackbar.show();
                        }
                    }
                }
        ) {
            @SuppressLint("LongLogTag")
            @Override
            protected Map<String, String> getParams()
            {
                PeriodiciteEnum periodiciteEnum = PeriodiciteEnum.JOURNALIERE;
                switch (periode){
                    case "Journalière":
                        periodiciteEnum = PeriodiciteEnum.JOURNALIERE;
                        break;
                    case "Mensuelle":
                        periodiciteEnum = PeriodiciteEnum.MENSUELLE;
                        break;
                    case "Hebdomadaire":
                        periodiciteEnum = PeriodiciteEnum.HEBDOMADAIRE;
                        break;
                }
                Map<String, String>  params = new HashMap<String, String>();
                params.put("customerNumber", numero);
                params.put("denomination", denomination_value);
                params.put("periode", periodiciteEnum.name());
                params.put("mise", mise_val);
                params.put("autoPayment", "true");
                params.put("dateDeblocage", dateDeblocageValue);
                Log.e("denonciation", denomination_value);
                Log.e("Le body de Tontine bloqué", params.toString());

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
                80000,
                -1,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        queue.add(postRequest);

        //initialize the progress dialog and show it
        progressDialog = new ProgressDialog(NouvelleTontine.this);
        progressDialog.setCancelable(false);
        progressDialog.setMessage("Veuillez patienter SVP! \n La souscription à la tontine est en cours...");
        progressDialog.show();
    }*/

    @SuppressLint("LongLogTag")
    private void payerTontineBloque(final String numero, final String montant, final Dialog dialog, final long heure_transaction, final String periode, final int prelevement_auto, final String mise_val, final String denomination_value, final String dateDeblocageValue ) {
        RequestQueue queue = Volley.newRequestQueue(this);
        final String[] resultat = {""};
        Log.d("ResponseTagP", "testPayer");
        Log.e("Le token est:", accessToken);

        JSONObject params = new JSONObject();
        try {
            PeriodiciteEnum periodiciteEnum = PeriodiciteEnum.JOURNALIERE;
            switch (periode){
                case "Journalière":
                    periodiciteEnum = PeriodiciteEnum.JOURNALIERE;
                    break;
                case "Mensuelle":
                    periodiciteEnum = PeriodiciteEnum.MENSUELLE;
                    break;
                case "Hebdomadaire":
                    periodiciteEnum = PeriodiciteEnum.HEBDOMADAIRE;
                    break;
            }
            params.put("customerNumber", numero);
            params.put("denomination", denomination_value);
            params.put("periode", periodiciteEnum.name());
            params.put("mise", mise_val);
            params.put("autoPayment", prelevement_auto == 1);
            params.put("dateDeblocage", dateDeblocageValue);
            Log.e("Le body de tontine bloqué", params.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        JsonObjectRequest postRequest = new JsonObjectRequest(Request.Method.POST, Constantes.URL_NEW_TONTINE, params,
                new Response.Listener<JSONObject>() {
                    @SuppressLint("ResourceAsColor")
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d("ResponseTagP", response.toString());
                        progressDialog.dismiss();
                        try {
                            int responseCode = response.getInt("responseCode");
                            if (responseCode == 0) {
                                Date currentTime = Calendar.getInstance().getTime();
                                long timestamp_creation = currentTime.getTime();
                                long timestamp_maj = currentTime.getTime();
                                Utilisateur u = Utilisateur.find(Utilisateur.class, "id_utilisateur = ?", Prefs.getString(ID_UTILISATEUR_KEY, null)).get(0);

                              /*  JSONObject tontineJson = response.getJSONObject("body");
                                JSONArray versements = tontineJson.getJSONArray("payments");
                                Tontine nouvelle_tontine = new Tontine();
                                nouvelle_tontine.setId_server(tontineJson.getString("id"));
                                nouvelle_tontine.setId_utilisateur(Prefs.getString(ID_UTILISATEUR_KEY, null));
                                nouvelle_tontine.setDenomination(tontineJson.getString("denomination"));
                                nouvelle_tontine.setPeriode(tontineJson.getString("periode"));
                                nouvelle_tontine.setMise(tontineJson.getInt("mise"));
                                nouvelle_tontine.setPrelevement_auto(tontineJson.getBoolean("isAutoPayment"));
                                nouvelle_tontine.setCarnet(String.valueOf(tontineJson.getString("carnet")));
                                nouvelle_tontine.setStatut(tontineJson.getString("isValide"));
                                nouvelle_tontine.setDateDeblocage(tontineJson.getString("unBlockDate"));
                                nouvelle_tontine.setCreation(timestamp_creation);
                                nouvelle_tontine.setMaj(timestamp_maj);
                                nouvelle_tontine.setContinuer(tontineJson.getLong("carnet"));
                                nouvelle_tontine.save();*/
                                JSONObject tontineJson = response.getJSONObject("body");
                                JSONArray versements = tontineJson.getJSONArray("payments");

                                Tontine nouvelle_tontine = new Tontine();
                                nouvelle_tontine.setId_server(tontineJson.getString("id"));
                                nouvelle_tontine.setId_utilisateur(Prefs.getString(ID_UTILISATEUR_KEY, null));
                                nouvelle_tontine.setDenomination(tontineJson.getString("denomination"));
                                nouvelle_tontine.setPeriode(tontineJson.getString("periode"));
                                nouvelle_tontine.setMise(tontineJson.getInt("mise"));
                                nouvelle_tontine.setPrelevement_auto(tontineJson.getBoolean("isAutoPayment"));
                                nouvelle_tontine.setCarnet(String.valueOf(tontineJson.getString("carnet")));
                                nouvelle_tontine.setStatut(tontineJson.getString("state"));

                                // Gestion de la date de déblocage

                                try {
                                    String unBlockDateString = tontineJson.getString("unBlockDate");
                                    // Supposons que la date soit au format "YYYY-MM-DD"
                                    String[] dateParts = unBlockDateString.split("-");
                                    if (dateParts.length == 3) {
                                        @SuppressLint("DefaultLocale") String unBlockDate = Integer.parseInt(dateParts[0]) + "-" +
                                                String.format("%02d", Integer.parseInt(dateParts[1])) + "-" +
                                                String.format("%02d", Integer.parseInt(dateParts[2]));
                                        nouvelle_tontine.setDateDeblocage(unBlockDate);
                                    }
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }

                                /*JSONArray unBlockDateArray = tontineJson.getJSONArray("unBlockDate");
                                String unBlockDate = unBlockDateArray.getInt(0) + "-" +
                                        String.format("%02d", unBlockDateArray.getInt(1)) + "-" +
                                        String.format("%02d", unBlockDateArray.getInt(2));
                                nouvelle_tontine.setDateDeblocage(unBlockDate);
*/
                                // Timestamps de création et de mise à jour

                                nouvelle_tontine.setCreation(timestamp_creation);
                                nouvelle_tontine.setMaj(timestamp_maj);

                                // Valeur continue
                                nouvelle_tontine.setContinuer(tontineJson.getLong("carnet"));
                                nouvelle_tontine.save();


                                Long id_tontine = nouvelle_tontine.getId();

                                if (nouvelle_tontine.getPrelevement_auto()) {
                                    Cotis_Auto cotis_auto = new Cotis_Auto();
                                    cotis_auto.setTontine(nouvelle_tontine);
                                    cotis_auto.setUtilisateur(u);
                                    cotis_auto.setCreation(timestamp_creation);
                                    cotis_auto.setMaj(timestamp_creation);
                                    cotis_auto.save();
                                }

                                for (int i = 0; i < versements.length(); i++) {
                                    JSONObject data = versements.getJSONObject(i);
                                    Versement n_versement = new Versement();
                                    n_versement.setFractionne(data.getString("isFractioned"));
                                    n_versement.setIdVersement(data.getString("idVersement"));
                                    n_versement.setMontant(data.getString("amount"));
                                    n_versement.setIdVersServ(data.getString("id"));
                                    n_versement.setCreation(timestamp_creation);
                                    n_versement.setMaj(timestamp_maj);
                                    n_versement.setUtilisateur(u);
                                    n_versement.setTontine(nouvelle_tontine);
                                    n_versement.save();
                                }

                                // Rest of your code to handle the response and start new activity
                                if (id_tontine!=0){
                                    Log.i("id_tontine", String.valueOf(id_tontine));
                                    //alertView("souscription ok","ok");
                                    NouvelleTontine.ViewOk alert = new NouvelleTontine.ViewOk();
                                    int nb_tontine = 0;
                                    int nb_tontine_encours = 0;
                                    int nb_tontine_terminees = 0;
                                    int nb_tontine_encaissees = 0;
                                    List<Tontine> liste_tontines = Select.from(Tontine.class)
                                            .where(Condition.prop("id_utilisateur").eq(Prefs.getString(ID_UTILISATEUR_KEY,null)) )
                                            .groupBy("carnet")
                                            .list();
                                    nb_tontine = liste_tontines.size();
                                    List<Tontine> liste_tontines_encours = Select.from(Tontine.class)
                                            .where(Condition.prop("id_utilisateur").eq(Prefs.getString(ID_UTILISATEUR_KEY,null)) )
                                            .where(Condition.prop("statut").eq(TontineEnum.IN_PROGRESS.toString()) )
                                            .list();
                                    nb_tontine_encours= liste_tontines_encours.size();
                                    List<Tontine> liste_tontines_terminees = Select.from(Tontine.class)
                                            .where(Condition.prop("id_utilisateur").eq(Prefs.getString(ID_UTILISATEUR_KEY,null)) )
                                            .where(Condition.prop("statut").eq(TontineEnum.COMPLETED.toString()) )
                                            .list();
                                    nb_tontine_terminees= liste_tontines_terminees.size();
                                    List<Tontine> liste_tontines_encaissees = Select.from(Tontine.class)
                                            .where(Condition.prop("id_utilisateur").eq(Prefs.getString(ID_UTILISATEUR_KEY,null)) )
                                            .where(Condition.prop("statut").eq(TontineEnum.COLLECTED.toString()) )
                                            .list();

                                    nb_tontine_encaissees= liste_tontines_encaissees.size();
                                    String to_tontine_user = nb_tontine+" tontine";
                                    String to_tontine_encours = nb_tontine_encours+" tontine";
                                    String to_tontine_terminees = nb_tontine_terminees+" carte";
                                    String to_tontine_encaissees = nb_tontine_encaissees+" carte";
                                    if (nb_tontine>1){
                                        to_tontine_user =nb_tontine+" tontines";
                                    }
                                    if (nb_tontine_encours>1){
                                        to_tontine_encours =nb_tontine_encours+" tontines";
                                    }
                                    if (nb_tontine_terminees>1){
                                        to_tontine_terminees =nb_tontine_terminees+" cartes";
                                    }
                                    if (nb_tontine_encaissees>1){
                                        to_tontine_encaissees =nb_tontine_encaissees+" cartes";
                                    }

                                    //ussdApi.destroyInstance();
                                    //Log.d("monde","zz:"+id);

                                    //ajout
                                    isNewTontine = true;
                                    //ajout fin

                                    String msg="Votre tontine a été correctement enregistrée. Vous avez maintenant au total "+to_tontine_user
                                            +" dont :\n\n • "+to_tontine_encours+" en cours; \n • "+to_tontine_terminees+" terminé(e)s; \n • "+to_tontine_encaissees+" encaissé(e)s \n";
                                    Intent i = new Intent(NouvelleTontine.this, Message_ok.class);
                                    i.putExtra("msg_desc",msg);
                                    //i.putExtra("id_tontine",Integer.parseInt(String.valueOf(id)));
                                    i.putExtra("id_tontine",id_tontine);
                                    i.putExtra("isNewTontine",isNewTontine);
//                                        i.putExtra("class","com.sicmagroup.tondi.CarteMain");
                                    startActivity(i);

                                    dialog.dismiss();
                                }else{

                                    String msg="Une erreur s'est produite. Veuillez réessayer SVP!";
                                    Intent i = new Intent(NouvelleTontine.this, Message_ok.class);
                                    i.putExtra("msg_desc",msg);
                                    i.putExtra("class","com.sicmagroup.tondi.NouvelleTontine");
                                    startActivity(i);
                                }


                            } else {
                                progressDialog.dismiss();
                                String msg = response.getString("body");
                                Intent i = new Intent(NouvelleTontine.this, Message_non.class);
                                i.putExtra("msg_desc", msg);
                                i.putExtra("class", "com.sicmagroup.tondi.NouvelleTontine");
                                startActivity(i);
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {
                        progressDialog.dismiss();
                        Log.e("Error.Inscription", String.valueOf(volleyError.getMessage()));
                        CoordinatorLayout mainLayout = (CoordinatorLayout) findViewById(R.id.layout_tontine);
                        String message = "Une erreur est survenue!";
                        if (volleyError instanceof NetworkError || volleyError instanceof AuthFailureError) {
                            refreshTokenAndRetry(numero, montant, dialog, heure_transaction, periode, prelevement_auto, mise_val, denomination_value, dateDeblocageValue);
                        } else {
                            Snackbar snackbar = Snackbar.make(mainLayout, message, Snackbar.LENGTH_INDEFINITE)
                                    .setAction("REESSAYER", new View.OnClickListener() {
                                        @Override
                                        public void onClick(View view) {
                                            payerTontineBloque(numero, montant, dialog, heure_transaction, periode, prelevement_auto, mise_val, denomination_value, dateDeblocageValue);
                                        }
                                    });
                            snackbar.getView().setBackgroundColor(ContextCompat.getColor(NouvelleTontine.this, R.color.colorGray));
                            snackbar.setActionTextColor(getResources().getColor(R.color.colorPrimaryDark));
                            View sbView = snackbar.getView();
                            TextView textView = sbView.findViewById(com.google.android.material.R.id.snackbar_text);
                            textView.setTextColor(Color.WHITE);
                            snackbar.show();
                        }
                    }
                }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                headers.put("Content-Type", "application/json");
                headers.put("Authorization", "Bearer " + accessToken);
                return headers;
            }
        };

        postRequest.setRetryPolicy(new DefaultRetryPolicy(
                80000,
                -1,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        queue.add(postRequest);

        progressDialog = new ProgressDialog(NouvelleTontine.this);
        progressDialog.setCancelable(false);
        progressDialog.setMessage("Veuillez patienter SVP! \n La souscription à la tontine est en cours...");
        progressDialog.show();
    }

    @SuppressLint("LongLogTag")
    private void refreshTokenAndRetry(final String numero, final String montant, final Dialog dialog, final long heure_transaction, final String periode, final int prelevement_auto, final String mise_val, final String denomination_value, final String dateDeblocageValue) {
        String refreshToken = Prefs.getString(REFRESH_TOKEN, "");
        JSONObject params = new JSONObject();
        try {
            params.put("refreshToken", refreshToken);
            Log.e("Le body du refresh Token", params.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        JsonObjectRequest refreshTokenRequest = new JsonObjectRequest(
                Request.Method.POST,
                Constantes.url_refresh_token,
                params,
                new Response.Listener<JSONObject>() {
                    @SuppressLint("LongLogTag")
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            Log.e("LA réponse du refresh token", response.toString());

                            String newAccessToken = response.getString("token");
                            String newRefreshToken = response.getString("refreshToken");
                            Prefs.putString(TOKEN, newAccessToken);
                            Prefs.putString(REFRESH_TOKEN, newRefreshToken);
                            accessToken = newAccessToken;
                            payerTontineBloque(numero, montant, dialog, heure_transaction, periode, prelevement_auto, mise_val, denomination_value, dateDeblocageValue);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {
                        progressDialog.dismiss();
                        // handle the error for refreshing token
                        Log.e("Error.RefreshToken", String.valueOf(volleyError.getMessage()));
                        // Show error message to the user
                    }
                }
        );

        RequestQueue queue = Volley.newRequestQueue(this);
        queue.add(refreshTokenRequest);
    }

    private void check_transaction_statut(final String numero, final long heure_transaction, final Dialog dialog, final String id_sim, final String periode, final int prelevement_auto, final String mise_val)
    {
        if(progressDialog.isShowing()){
            progressDialog.dismiss();
        }
        //Prefs.putBoolean(PAYEMENT_IS_CHECKED_KEY, true);
        RequestQueue queue = Volley.newRequestQueue(NouvelleTontine.this);
        StringRequest postRequest = new StringRequest(Request.Method.POST, url_get_statut,
                new Response.Listener<String>()
                {
                    @SuppressLint("ResourceAsColor")
                    @Override
                    public void onResponse(String response) {
                        //progressDialog.dismiss();
                        // response
                        Log.d("ResponseTagP", response);
                        Long id_tontine = Long.valueOf(0);
                        try {

                            JSONObject result = new JSONObject(response);
                            //final JSONArray array = result.getJSONArray("data");
                            Log.d("success_op?", String.valueOf(result.getBoolean("success")));


                            if (result.getBoolean("success")){
                                // maj des dates
                                Date currentTime = Calendar.getInstance().getTime();
                                long output_creation=currentTime.getTime()/1000L;
                                String str_creation=Long.toString(output_creation);
                                long timestamp_creation = Long.parseLong(str_creation) * 1000;
                                long output_maj=currentTime.getTime()/1000L;
                                String str_maj=Long.toString(output_maj);
                                long timestamp_maj = Long.parseLong(str_maj) * 1000;
                                Utilisateur u = Utilisateur.find(Utilisateur.class, "id_utilisateur = ?", Prefs.getString(ID_UTILISATEUR_KEY,null)).get(0);

                                progressDialog.dismiss();

                                if (result.has("resultat"))
                                {
                                    JSONArray resultat = result.getJSONArray("resultat");
                                    String[] actionGroup = {};
                                    String action = "";
                                    String object = "";
                                    for (int i = 0; i < resultat.length(); i++)
                                    {

                                        JSONObject content = new JSONObject(resultat.get(i).toString());
                                        actionGroup =  content.getString("action").split("#");
                                        action = actionGroup[0];
                                        object = actionGroup[1];
                                        JSONObject data = new JSONObject(content.getJSONObject("data").toString());
                                        if ("add".equals(action)) {
                                            if (object.equals("tontines")) {
                                                Tontine nouvelle_tontine = new Tontine();
                                                nouvelle_tontine.setId_server(data.getString("id"));
                                                Log.i("new_t_id", String.valueOf(nouvelle_tontine.getId_server()));

                                                nouvelle_tontine.setId_utilisateur(Prefs.getString(ID_UTILISATEUR_KEY,null));
                                                Log.i("new_t_idUser", String.valueOf(nouvelle_tontine.getId_utilisateur()));

                                                nouvelle_tontine.setDenomination(data.getString("denomination"));
                                                Log.i("new_t_deno", String.valueOf(nouvelle_tontine.getDenomination()));

                                                nouvelle_tontine.setPeriode(data.getString("periode"));
                                                Log.i("new_t_periode", String.valueOf(nouvelle_tontine.getPeriode()));

                                                nouvelle_tontine.setMise(data.getInt("mise"));
                                                Log.i("new_t_idMise", String.valueOf(nouvelle_tontine.getMise()));

                                                nouvelle_tontine.setPrelevement_auto(data.getBoolean("isAutoPayment"));
//                                                Log.i("new_t_idAuto", String.valueOf(nouvelle_tontine.getPrelevement_auto()));

                                                nouvelle_tontine.setIdSim(id_sim);
                                                Log.i("new_t_idSim", String.valueOf(nouvelle_tontine.getIdSim()));

                                                nouvelle_tontine.setCarnet(String.valueOf(data.getString("carnet")));
                                                Log.i("new_t_Carnet", String.valueOf(nouvelle_tontine.getCarnet()));

                                                nouvelle_tontine.setStatut(data.getString("statut"));
                                                Log.i("new_t_Statut", String.valueOf(nouvelle_tontine.getStatut()));

                                                nouvelle_tontine.setCreation(timestamp_creation);
                                                Log.i("new_t_crea", String.valueOf(nouvelle_tontine.getCreation()));

                                                nouvelle_tontine.setMaj(timestamp_maj);
                                                Log.i("new_t_Maj", String.valueOf(nouvelle_tontine.getMaj()));

                                                nouvelle_tontine.setContinuer(data.getLong("carnet"));

                                                nouvelle_tontine.save();
                                                id_tontine = nouvelle_tontine.getId();
                                                Tontine old = Tontine.findById(Tontine.class, id_tontine);
                                                Log.i("old_t", String.valueOf(old.getId()));


                                                if (nouvelle_tontine.getPrelevement_auto() ) {
                                                    // ajouter cotisations automatiques
                                                    Cotis_Auto cotis_auto = new Cotis_Auto();
                                                    cotis_auto.setTontine(nouvelle_tontine);
                                                    cotis_auto.setUtilisateur(u);
                                                    // maj des dates
                                                    cotis_auto.setCreation(timestamp_creation);
                                                    cotis_auto.setMaj(timestamp_creation);
                                                    cotis_auto.save();
                                                }

                                            } else if (object.equals("versements")) {
                                                Versement n_versement = new Versement();
                                                n_versement.setFractionne(data.getString("fractionne"));
                                                Log.i("new_V_frac", String.valueOf(data.getString("fractionne")));

                                                n_versement.setIdVersement(data.getString("id_versement"));
                                                Log.i("new_V_idV", String.valueOf(data.getString("id_versement")));

                                                n_versement.setMontant(data.getString("montant"));
                                                Log.i("new_V_Montan", String.valueOf(data.getString("montant")));

                                                n_versement.setCreation(timestamp_creation);
                                                n_versement.setMaj(timestamp_maj);
                                                n_versement.setUtilisateur(u);
                                                Tontine cible = Tontine.findById(Tontine.class, id_tontine);
                                                n_versement.setTontine(cible);
                                                n_versement.save();
                                            }
                                        }
                                    }
                                }
                                if (id_tontine!=0){
                                    Log.i("id_tontine", String.valueOf(id_tontine));
                                    //alertView("souscription ok","ok");
                                    NouvelleTontine.ViewOk alert = new NouvelleTontine.ViewOk();
                                    int nb_tontine = 0;
                                    int nb_tontine_encours = 0;
                                    int nb_tontine_terminees = 0;
                                    int nb_tontine_encaissees = 0;
                                    List<Tontine> liste_tontines = Select.from(Tontine.class)
                                            .where(Condition.prop("id_utilisateur").eq(Prefs.getString(ID_UTILISATEUR_KEY,null)) )
                                            .groupBy("carnet")
                                            .list();
                                    nb_tontine = liste_tontines.size();
                                    List<Tontine> liste_tontines_encours = Select.from(Tontine.class)
                                            .where(Condition.prop("id_utilisateur").eq(Prefs.getString(ID_UTILISATEUR_KEY,null)) )
                                            .where(Condition.prop("statut").eq(TontineEnum.IN_PROGRESS.toString()) )
                                            .list();
                                    nb_tontine_encours= liste_tontines_encours.size();
                                    List<Tontine> liste_tontines_terminees = Select.from(Tontine.class)
                                            .where(Condition.prop("id_utilisateur").eq(Prefs.getString(ID_UTILISATEUR_KEY,null)) )
                                            .where(Condition.prop("statut").eq(TontineEnum.COMPLETED.toString()) )
                                            .list();
                                    nb_tontine_terminees= liste_tontines_terminees.size();
                                    List<Tontine> liste_tontines_encaissees = Select.from(Tontine.class)
                                            .where(Condition.prop("id_utilisateur").eq(Prefs.getString(ID_UTILISATEUR_KEY,null)) )
                                            .where(Condition.prop("statut").eq(TontineEnum.COLLECTED.toString()) )
                                            .list();
                                    nb_tontine_encaissees= liste_tontines_encaissees.size();
                                    String to_tontine_user = nb_tontine+" tontine";
                                    String to_tontine_encours = nb_tontine_encours+" tontine";
                                    String to_tontine_terminees = nb_tontine_terminees+" carte";
                                    String to_tontine_encaissees = nb_tontine_encaissees+" carte";
                                    if (nb_tontine>1){
                                        to_tontine_user =nb_tontine+" tontines";
                                    }
                                    if (nb_tontine_encours>1){
                                        to_tontine_encours =nb_tontine_encours+" tontines";
                                    }
                                    if (nb_tontine_terminees>1){
                                        to_tontine_terminees =nb_tontine_terminees+" cartes";
                                    }
                                    if (nb_tontine_encaissees>1){
                                        to_tontine_encaissees =nb_tontine_encaissees+" cartes";
                                    }
                                    //ussdApi.destroyInstance();
                                    //Log.d("monde","zz:"+id);
                                    String msg="Votre tontine a été correctement enregistrée. Vous avez maintenant au total "+to_tontine_user
                                            +" dont :\n\n • "+to_tontine_encours+" en cours; \n • "+to_tontine_terminees+" terminé(e)s et \n • "+to_tontine_encaissees+" encaissé(e)s";
                                    Intent i = new Intent(NouvelleTontine.this, Message_ok.class);
                                    i.putExtra("msg_desc",msg);
                                    //i.putExtra("id_tontine",Integer.parseInt(String.valueOf(id)));
                                    i.putExtra("id_tontine",id_tontine);
                                    i.putExtra("class","com.sicmagroup.tondi.MesTontines");
                                    startActivity(i);

                                    dialog.dismiss();
                                }else{

                                    String msg="Une erreur s'est produite. Veuillez réessayer SVP!";
                                    Intent i = new Intent(NouvelleTontine.this, Message_ok.class);
                                    i.putExtra("msg_desc",msg);
                                    i.putExtra("class","com.sicmagroup.tondi.NouvelleTontine");
                                    startActivity(i);
                                }


                            }else{
                                progressDialog.dismiss();
                                String msg=result.getString("message");
                                Intent i = new Intent(NouvelleTontine.this, Message_non.class);
                                i.putExtra("msg_desc",msg);
                                i.putExtra("class","com.sicmagroup.tondi.NouvelleTontine");
                                NouvelleTontine.this.finish();
                                startActivity(i);
                            }


                        } catch (Throwable t) {
                            Log.d("errornscription",t.getMessage());
                            //Log.e("My App", "Could not parse malformed JSON: \"" + response + "\"");
                        }
                    }
                },
                new Response.ErrorListener()
                {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {
                        progressDialog.dismiss();
                        // error
                        //Log.d("Error.Inscription", String.valueOf(error.getMessage()));
//                        ConstraintLayout mainLayout =  findViewById(R.id.layout_tontine);

                        String message;
                        if (volleyError instanceof NetworkError || volleyError instanceof AuthFailureError|| volleyError instanceof TimeoutError) {
                            //Toast.makeText(Inscription.this, "error:"+volleyError.getMessage(), Toast.LENGTH_SHORT).show();
                            //Log.d("VolleyError_Test",volleyError.getMessage());
                            message = "Aucune connexion Internet. Vérifiez votre connection internet et réessayez.";

                            Dialog dialog = new Dialog(NouvelleTontine.this);
                            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                            dialog.setCancelable(false);
                            dialog.setContentView(R.layout.dialog_attention);

                            TextView titre = (TextView) dialog.findViewById(R.id.deco_title);
                            titre.setText("Attention");
                            TextView message_deco = (TextView) dialog.findViewById(R.id.deco_message);
                            message_deco.setText(message);

                            Button oui = (Button) dialog.findViewById(R.id.btn_oui);
                            oui.setText("Réessayer");

                            oui.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    check_transaction_statut(numero,heure_transaction_global, dialog,id_sim, periode, prelevement_auto, mise_val);

                                }
                            });

                            Button non = (Button) dialog.findViewById(R.id.btn_non);
                            non.setVisibility(View.GONE);
                            dialog.show();


                        } else if (volleyError instanceof ServerError) {
                            if(volleyError.networkResponse.statusCode == 404)
                            {
//                                message = "Procéder à la vérification de votre paiement";
//                                Dialog dialog = new Dialog(NouvelleTontine.this);
//                                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
//                                dialog.setCancelable(false);
//                                dialog.setContentView(R.layout.dialog_attention);
//
//                                TextView titre = (TextView) dialog.findViewById(R.id.deco_title);
//                                titre.setText("Attention");
//                                TextView message_deco = (TextView) dialog.findViewById(R.id.deco_message);
//                                message_deco.setText(message);
//
//                                Button oui = (Button) dialog.findViewById(R.id.btn_oui);
//
//                                oui.setText("Vérifier");
//                                oui.setOnClickListener(new View.OnClickListener() {
//                                    @Override
//                                    public void onClick(View v) {
                                        check_transaction_statut(numero,heure_transaction_global, dialog,id_sim, periode, prelevement_auto, mise_val);

//                                    }
//                                });

//                                Button non = (Button) dialog.findViewById(R.id.btn_non);
//                                non.setVisibility(View.GONE);
//                                dialog.show();
                            }
                            else {
                                message = "Impossible de contacter le serveur. Patientez un peu et réessayez.";
                                Dialog dialog = new Dialog(NouvelleTontine.this);
                                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                                dialog.setCancelable(false);
                                dialog.setContentView(R.layout.dialog_attention);

                                TextView titre = (TextView) dialog.findViewById(R.id.deco_title);
                                titre.setText("Attention");
                                TextView message_deco = (TextView) dialog.findViewById(R.id.deco_message);
                                message_deco.setText(message);

                                Button oui = (Button) dialog.findViewById(R.id.btn_oui);

                                oui.setText("Réessayer");
                                oui.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        check_transaction_statut(numero,heure_transaction_global, dialog,id_sim, periode, prelevement_auto, mise_val);

                                    }
                                });

                                Button non = (Button) dialog.findViewById(R.id.btn_non);
                                non.setVisibility(View.GONE);
                                dialog.show();
                            }

                        }  else if (volleyError instanceof ParseError) {
                            //message = "Parsing error! Please try again later";
                            message = "Une erreur est survenue! Patientez et réessayez.";
                            Dialog dialog = new Dialog(NouvelleTontine.this);
                            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                            dialog.setCancelable(false);
                            dialog.setContentView(R.layout.dialog_attention);

                            TextView titre = (TextView) dialog.findViewById(R.id.deco_title);
                            titre.setText("Attention");
                            TextView message_deco = (TextView) dialog.findViewById(R.id.deco_message);
                            message_deco.setText(message);

                            Button oui = (Button) dialog.findViewById(R.id.btn_oui);
                            oui.setText("Réessayer");

                            oui.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    check_transaction_statut(numero,heure_transaction_global, dialog,id_sim, periode, prelevement_auto, mise_val);

                                }
                            });

                            Button non = (Button) dialog.findViewById(R.id.btn_non);
                            non.setVisibility(View.GONE);
                            dialog.show();
                        }
                    }
                }
        ) {
            @Override
            protected Map<String, String> getParams()
            {
                PeriodiciteEnum periodiciteEnum = PeriodiciteEnum.JOURNALIERE;
                switch (periode){
                    case "Journalière":
                        periodiciteEnum = PeriodiciteEnum.JOURNALIERE;
                        break;
                    case "Mensuelle":
                        periodiciteEnum = PeriodiciteEnum.MENSUELLE;
                        break;
                    case "Hebdomadaire":
                        periodiciteEnum = PeriodiciteEnum.HEBDOMADAIRE;
                        break;
                }
                Map<String, String>  params = new HashMap<String, String>();
                params.put("numero", numero);
                params.put("heure_transaction", String.valueOf(heure_transaction));
                params.put("origine", "nouvelle tontine");
                params.put("periode", periodiciteEnum.name());
                params.put("mise", mise_val);
                params.put("id_sim", id_sim);
                params.put("prelevement_auto", String.valueOf(prelevement_auto));
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
                20000,
                -1,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        queue.add(postRequest);

        progressDialog = new ProgressDialog(NouvelleTontine.this);
        progressDialog.setCancelable(false);
        progressDialog.setMessage("Veuillez patienter SVP! \nVérification de la transaction en cours...");
        progressDialog.show();

    }


    private void pay_via_ussd(final Dialog dialog, final String id_sim, final String reseau){
        String phoneNumber;
        final BaseFormElement mise = mFormBuilder.getFormElement(TAG_MISE);
        if (reseau.equals("MTN")){
            phoneNumber = "*400#".trim();
        }else{
            phoneNumber = "*155#";//1*1*1*229"+MOOV_TEST+"*229"+MOOV_TEST+"*"+mise.getVagklue()+"#".trim();
        }
        final String montant_value = mise.getValue();

        ussdApi = USSDController.getInstance(NouvelleTontine.this);
        //result.setText("");
        final Intent svc = new Intent(NouvelleTontine.this, SplashLoadingService.class);
        svc.putExtra("texte","Le paiement pour la souscription à votre tontine est en cours...");
        //startService(svc);

        ussdApi.callUSSDInvoke(phoneNumber, map, new USSDController.CallbackInvoke() {
            @Override
            public void responseInvoke(String message) {
                Prefs.putBoolean(ACCESS_BOOL,true);
                ussd_level++;
                Log.d("APPEE1", message);
                Log.d("APP_MENU_LEVEL0", String.valueOf((ussd_level)));
                // premiere reponse: repondre 1, envoi d'argent
                if (ussd_level==1){
                    ussdApi.send("1", new USSDController.CallbackMessage() {
                        @Override
                        public void responseMessage(String message) {
                            ussd_level++;

                            Log.d("APPEE2", message);
                            Log.d("APP_MENU_LEVEL", String.valueOf((ussd_level)));
                            // deuxieme reponse: repondre 1, Abonne MM
                            if (ussd_level==2){
                                ussdApi.send("1", new USSDController.CallbackMessage() {
                                    @Override
                                    public void responseMessage(String message) {
                                        ussd_level++;
                                        Log.d("APPEE", message);
                                        Log.d("APP_MENU_LEVEL", String.valueOf(ussd_level));
                                        // troisième reponse: repondre par numero marchand tondi

                                    }
                                });
                            }
                        }
                    });
                }

                if (ussd_level==3){
                    ussdApi.send(MTN_MECOTI , new USSDController.CallbackMessage() {
                        @Override
                        public void responseMessage(String message) {
                            ussd_level++;
                            Log.d("APP", message);
                            // quatrième reponse: repondre par numero marchand tondi pour confirmer
                            if (ussd_level==4){
                                ussdApi.send(MTN_MECOTI , new USSDController.CallbackMessage() {
                                    @Override
                                    public void responseMessage(String message) {
                                        ussd_level++;
                                        Log.d("APP", message);
                                        Log.d("APP_MENU_LEVEL", String.valueOf(ussd_level));
                                    }
                                });
                            }
                        }
                    });
                }

                if (ussd_level==5){
                    ussdApi.send(montant_value, new USSDController.CallbackMessage() {
                        @Override
                        public void responseMessage(String message) {
                            ussd_level++;
                            Log.d("APP", message);
                            // quatrième reponse: repondre par numero marchand tondi pour confirmer
                            if (ussd_level==6){
                                ussdApi.send("paiement tondi" , new USSDController.CallbackMessage() {
                                    @Override
                                    public void responseMessage(String message) {
                                        //ussd_level++;

                                        //ussd_level=0;
                                        Log.d("APP", message);
                                        Log.d("APP_MENU_LEVEL", String.valueOf(ussd_level));
                                    }
                                });
                            }
                        }
                    });
                }

                if (ussd_level==7){
                    //stopService(svc);
                    ussd_level=0;
                }



                // first option list - select option 1
                        /*ussdController.send("1", new USSDController.CallbackMessage() {
                            @Override
                            public void responseMessage(String message) {
                                Log.d("APP", message);
                                result.append("\n-\n" + message);
                                // second option list - select option 1
                                ussdController.send("1", new USSDController.CallbackMessage() {
                                    @Override
                                    public void responseMessage(String message) {
                                        Log.d("APP", message);
                                        result.append("\n-\n" + message);
                                    }
                                });
                            }
                        });*/
            }



            @SuppressLint("ResourceAsColor")
            @Override
            public void over(String message) {
                Log.d("APPUSSpp", message);
                // traiter message quand fini OK
                Pattern pattern_msg_ok;
                Pattern pattern_msg_ok1;
                if (reseau.equals("MTN")){
                    pattern_msg_ok = Pattern.compile("^(Transferteffectuepour"+montant_value+")FCFA.+"+MTN_TEST);
                    pattern_msg_ok1 = Pattern.compile("^(Transferteffectuepour"+montant_value+")FCFA.+"+"229"+MTN_TEST);
                }else{
                    pattern_msg_ok = Pattern.compile("^(Vousavezenvoye"+montant_value+".00)FCFA.+"+MOOV_TEST);
                    pattern_msg_ok1 = Pattern.compile("^(Vousavezenvoye"+montant_value+".00)FCFA.+"+"229"+MOOV_TEST);
                }

                Matcher matcher = pattern_msg_ok.matcher(message.replaceAll("\\s",""));
                Matcher matcher1 = pattern_msg_ok1.matcher(message.replaceAll("\\s",""));

                // if our pattern matches the string, we can try to extract our groups
                if (matcher.find() || matcher1.find()) {
                    try {
                        verser(dialog,id_sim);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }else{
                    /*Alerter.create(NouvelleTontine.this)
                            .setTitle(message)
                            .setIcon(R.drawable.ic_warning)
                            .setTitleAppearance(R.style.TextAppearance_AppCompat_Large)
                            .setIconColorFilter(R.color.colorPrimaryDark)
                            //.setText("Vous pouvez maintenant vous connecter.")
                            .setBackgroundColorRes(R.color.colorWhite) // or setBackgroundColorInt(Color.CYAN)
                            .show();*/
                    Intent i = new Intent(NouvelleTontine.this, Message_non.class);
                    i.putExtra("msg_desc",message);
                    i.putExtra("class","com.sicmagroup.tondi.NouvelleTontine");
                    startActivity(i);
                    // si préférence ne pas afficher cette fenetre n'est pas defini
                    /*if (Prefs.getInt(DUALSIM_INFO_KEY,0)!=1){
                        // afficher la fenetre d'infos
                        NouvelleTontine.ViewInfos alert = new NouvelleTontine.ViewInfos();
                        alert.showDialog(NouvelleTontine.this);
                    }*/

                }


            }
        });
    }

    @SuppressLint("ResourceAsColor")
    private void verser(final Dialog dialog,String id_sim) throws JSONException {
        BaseFormElement periode = mFormBuilder.getFormElement(TAG_PERIODE);
        BaseFormElement mise = mFormBuilder.getFormElement(TAG_MISE);
        BaseFormElement prelevement_auto = mFormBuilder.getFormElement(TAG_MODE_COTISATION);

        String periode_value = periode.getValue();
        String mise_value = mise.getValue();
        String prelevement_auto_value = prelevement_auto.getValue();

        Boolean prelevement_auto_int = false;
        if (prelevement_auto_value.equals("Oui")){
            prelevement_auto_int=true;
        }
        int no_carnet=1;
        /*
        * leDOC
        * */
        //obtentiton du plus grand numero de carnet tontine journalière
        List<Tontine> derniere_tontine;
       // if(periode_value.equals("Journalière")){
            //List<Tontine> derniere_tontine;
            derniere_tontine = Tontine.findWithQuery(Tontine.class, " SELECT * FROM Tontine where id_utilisateur="+Prefs.getString(ID_UTILISATEUR_KEY,null)+ " ORDER BY CAST(carnet AS DECIMAL(5,2))DESC ");
            if(derniere_tontine.size()>0){
                no_carnet=Integer.parseInt(derniere_tontine.get(0).getCarnet())+1;
            }
        //}
        // si tontine journalière
        /*if(periode_value.equals("Journalière")){
            //List<Tontine> derniere_tontine;
            derniere_tontine = Tontine.findWithQuery(Tontine.class, "Select * from Tontine where id_utilisateur="+Prefs.getString(ID_UTILISATEUR_KEY,null)+" and mise="+mise_value+" and periode='"+periode_value+"' order by id desc ");
            if(derniere_tontine.size()>0){
                no_carnet=Integer.parseInt(derniere_tontine.get(0).getCarnet())+1;
            }
        }*/


        Tontine nouvelle_tontine = new Tontine();
        nouvelle_tontine.setId_utilisateur(Prefs.getString(ID_UTILISATEUR_KEY,null));
        nouvelle_tontine.setPeriode(periode_value);
        nouvelle_tontine.setMise(Integer.parseInt(mise_value));
        nouvelle_tontine.setPrelevement_auto(prelevement_auto_int);
        nouvelle_tontine.setIdSim(id_sim);
        nouvelle_tontine.setCarnet(String.valueOf(no_carnet));
        nouvelle_tontine.setStatut(TontineEnum.IN_PROGRESS.toString());
        // maj des dates
        Date currentTime = Calendar.getInstance().getTime();
        long output_creation=currentTime.getTime()/1000L;
        String str_creation=Long.toString(output_creation);
        long timestamp_creation = Long.parseLong(str_creation) * 1000;
        long output_maj=currentTime.getTime()/1000L;
        String str_maj=Long.toString(output_maj);
        long timestamp_maj = Long.parseLong(str_maj) * 1000;
        nouvelle_tontine.setCreation(timestamp_creation);
        nouvelle_tontine.setMaj(timestamp_maj);


        nouvelle_tontine.save();
        Long id = nouvelle_tontine.getId();
        nouvelle_tontine.setContinuer((long) no_carnet);
        nouvelle_tontine.save();

        // enregistrer le versement
        Versement versement = new Versement();
        versement.setMontant(mise_value);
        versement.setFractionne("0");
        versement.setIdVersement("0");
        versement.setTontine(nouvelle_tontine);

        // maj des dates
        versement.setCreation(timestamp_creation);
        versement.setMaj(timestamp_maj);

        Utilisateur u = Utilisateur.find(Utilisateur.class, "id_utilisateur = ?", Prefs.getString(ID_UTILISATEUR_KEY,null)).get(0);
        versement.setUtilisateur(u);
        versement.save();

        // enregistrer dans synchroniser
        if(id!=null){
            // liste tontines encours et terminees
            double solde = 0.00;
            List<Tontine>  list_tontines =Select.from(Tontine.class)
                    .where(Condition.prop("id_utilisateur").eq(Long.valueOf(Prefs.getString(ID_UTILISATEUR_KEY,null))))
                    .where(Condition.prop("statut").eq(TontineEnum.IN_PROGRESS.toString()))
                    .whereOr(Condition.prop("statut").eq(TontineEnum.COMPLETED.toString()))
                    .list();
            if(list_tontines.size()>0){
                for (Tontine tontine:list_tontines){
                    solde=solde+tontine.getMontant();
                }
            }

            // sync tontine
            Synchronisation new_sync = new Synchronisation();
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("a","add#tontines");
            jsonObject.put("n",u.getNumero());
            jsonObject.put("s",solde);
            Gson gson = new Gson();
            String nouvelle_tontine_json = gson.toJson(nouvelle_tontine);
            jsonObject.put("d",nouvelle_tontine_json);
            new_sync.setMaj(timestamp_maj);
            new_sync.setStatut(0);
            new_sync.setDonnees(jsonObject.toString());
            new_sync.save();


            // sync versement
            Synchronisation new_sync_v = new Synchronisation();
            JSONObject versement_obj = new JSONObject();
            versement_obj.put("a","add#versements");
            versement_obj.put("n",u.getNumero());
            versement_obj.put("s",solde);
            String versement_json = gson.toJson(versement);
            versement_obj.put("d",versement_json);
            new_sync_v.setMaj(timestamp_maj);
            new_sync_v.setStatut(0);
            new_sync_v.setDonnees(versement_obj.toString());
            new_sync_v.save();

            Utilitaire utilitaire = new Utilitaire(NouvelleTontine.this);
            // si internet, appeler synchroniser_en_ligne
            if (utilitaire.isConnected()){
                utilitaire.synchroniser_en_ligne();
            }
        }

        if (nouvelle_tontine.getPrelevement_auto()){
            // ajouter cotisations automatiques
            Cotis_Auto cotis_auto = new Cotis_Auto();
            cotis_auto.setTontine(nouvelle_tontine);
            Utilisateur utilisateur = SugarRecord.findById(Utilisateur.class, Long.valueOf(Prefs.getString(ID_UTILISATEUR_KEY,null)));
            cotis_auto.setUtilisateur(utilisateur);
            // maj des dates
            cotis_auto.setCreation(timestamp_creation);
            cotis_auto.setMaj(timestamp_maj);
            cotis_auto.save();
        }

        if (id!=null){
            //alertView("souscription ok","ok");
            NouvelleTontine.ViewOk alert = new NouvelleTontine.ViewOk();
            int nb_tontine = 0;
            int nb_tontine_encours = 0;
            int nb_tontine_terminees = 0;
            int nb_tontine_encaissees = 0;
            List<Tontine> liste_tontines = Select.from(Tontine.class)
                    .where(Condition.prop("id_utilisateur").eq(Prefs.getString(ID_UTILISATEUR_KEY,null)) )
                    .list();
            nb_tontine = liste_tontines.size();
            List<Tontine> liste_tontines_encours = Select.from(Tontine.class)
                    .where(Condition.prop("id_utilisateur").eq(Prefs.getString(ID_UTILISATEUR_KEY,null)) )
                    .where(Condition.prop("statut").eq(TontineEnum.IN_PROGRESS.toString()) )
                    .list();
            nb_tontine_encours= liste_tontines_encours.size();
            List<Tontine> liste_tontines_terminees = Select.from(Tontine.class)
                    .where(Condition.prop("id_utilisateur").eq(Prefs.getString(ID_UTILISATEUR_KEY,null)) )
                    .where(Condition.prop("statut").eq(TontineEnum.COMPLETED.toString()) )
                    .list();
            nb_tontine_terminees= liste_tontines_terminees.size();
            List<Tontine> liste_tontines_encaissees = Select.from(Tontine.class)
                    .where(Condition.prop("id_utilisateur").eq(Prefs.getString(ID_UTILISATEUR_KEY,null)) )
                    .where(Condition.prop("statut").eq(TontineEnum.COLLECTED.toString()) )
                    .list();
            nb_tontine_encaissees= liste_tontines_encaissees.size();
            String to_tontine_user = nb_tontine+" tontine";
            String to_tontine_encours = nb_tontine_encours+" tontine";
            String to_tontine_terminees = nb_tontine_terminees+" tontine";
            String to_tontine_encaissees = nb_tontine_encaissees+" tontine";
            if (nb_tontine>1){
                to_tontine_user =nb_tontine+" tontines";
            }
            if (nb_tontine_encours>1){
                to_tontine_encours =nb_tontine_encours+" tontines";
            }
            if (nb_tontine_terminees>1){
                to_tontine_terminees =nb_tontine_terminees+" tontines";
            }
            if (nb_tontine_encaissees>1){
                to_tontine_encaissees =nb_tontine_encaissees+" tontines";
            }
            //ussdApi.destroyInstance();
            //Log.d("monde","zz:"+id);
            String msg="Votre tontine a été correctement enregistrée. Vous avez maintenant au total "+to_tontine_user
                    +" dont :\n\n • "+to_tontine_encours+" en cours; \n • "+to_tontine_terminees+" terminé(e)s et \n • "+to_tontine_encaissees+" encaissé(e)s";
            Intent i = new Intent(NouvelleTontine.this, Message_ok.class);
            i.putExtra("msg_desc",msg);
            //i.putExtra("id_tontine",Integer.parseInt(String.valueOf(id)));
            i.putExtra("id_tontine",Integer.parseInt(String.valueOf(id)));
            //i.putExtra("class","com.sicmagroup.tondi.CarteMain");
            startActivity(i);

            dialog.dismiss();
        }else{

            String msg="Une erreur s'est produite. Veuillez réessayer SVP!";
            Intent i = new Intent(NouvelleTontine.this, Message_ok.class);
            i.putExtra("msg_desc",msg);
            i.putExtra("class","com.sicmagroup.tondi.NouvelleTontine");
            startActivity(i);
        }

    }

    private void retry_payer(final String numero, final String montant, final Dialog dialog, final String id_sim, final long heure_transaction, final String periode, final int prelevement_auto, final String mise_val, final String denomination_value ){
        RequestQueue queue = Volley.newRequestQueue(this);
        final String[] resultat = {""};
        Log.e("ResponseTagP", "testPayer");
        StringRequest postRequest = new StringRequest(Request.Method.POST, url_try,
                new Response.Listener<String>(){
                    @SuppressLint("ResourceAsColor")
                    @Override
                    public void onResponse(String response) {
                        //progressDialog.dismiss();
                        // response
                        Log.e("ResponseTagP", response);
                        if(response == null)
                        {
                            progressDialog.dismiss();
                            String msg="Erreur, le serveur ne réponds pas. Patientez un instant puis réessayer svp.";
                            Intent i = new Intent(NouvelleTontine.this, Message_non.class);
                            i.putExtra("msg_desc", msg);
                            i.putExtra("class","com.sicmagroup.tondi.NouvelleTontine");
                            startActivity(i);
                        }
                        else {
                            resultat[0] = response;
                            try {

                                JSONObject result = new JSONObject(response);
                                //final JSONArray array = result.getJSONArray("data");
                                Log.e("test", String.valueOf(result.getBoolean("success")));

                                if (result.getBoolean("success")) {
                                    Date currentTime = Calendar.getInstance().getTime();
                                    long output_creation=currentTime.getTime()/1000L;
                                    String str_creation=Long.toString(output_creation);
                                    long timestamp_creation = Long.parseLong(str_creation) * 1000;
                                    long output_maj=currentTime.getTime()/1000L;
                                    String str_maj=Long.toString(output_maj);
                                    long timestamp_maj = Long.parseLong(str_maj) * 1000;
                                    Utilisateur u = Utilisateur.find(Utilisateur.class, "id_utilisateur = ?", Prefs.getString(ID_UTILISATEUR_KEY,null)).get(0);

                                    progressDialog.dismiss();
                                    Long id_tontine = Long.valueOf(0);
                                    if (result.has("resultat"))
                                    {
                                        JSONArray resultat = result.getJSONArray("resultat");
                                        String[] actionGroup = {};
                                        String action = "";
                                        String object = "";
                                        for (int i = 0; i < resultat.length(); i++)
                                        {

                                            JSONObject content = new JSONObject(resultat.get(i).toString());
                                            actionGroup =  content.getString("action").split("#");
                                            action = actionGroup[0];
                                            object = actionGroup[1];
                                            JSONObject data = new JSONObject(content.getJSONObject("data").toString());
                                            if ("add".equals(action)) {
                                                if (object.equals("tontines")) {
                                                    Tontine nouvelle_tontine = new Tontine();
                                                    nouvelle_tontine.setId_server(data.getString("id"));
                                                    Log.i("new_t_id", String.valueOf(nouvelle_tontine.getId_server()));

                                                    nouvelle_tontine.setId_utilisateur(Prefs.getString(ID_UTILISATEUR_KEY,null));
                                                    Log.i("new_t_idUser", String.valueOf(nouvelle_tontine.getId_utilisateur()));

                                                    nouvelle_tontine.setDenomination(data.getString("denomination"));
                                                    Log.i("new_t_deno", String.valueOf(nouvelle_tontine.getDenomination()));

                                                    nouvelle_tontine.setPeriode(data.getString("periode"));
                                                    Log.i("new_t_periode", String.valueOf(nouvelle_tontine.getPeriode()));

                                                    nouvelle_tontine.setMise(data.getInt("mise"));
                                                    Log.i("new_t_idMise", String.valueOf(nouvelle_tontine.getMise()));

                                                    nouvelle_tontine.setPrelevement_auto(data.getBoolean("prelevement_auto"));
//                                                    Log.i("new_t_idAuto", String.valueOf(nouvelle_tontine.getPrelevement_auto()));

                                                    nouvelle_tontine.setIdSim(id_sim);
                                                    Log.i("new_t_idSim", String.valueOf(nouvelle_tontine.getIdSim()));

                                                    nouvelle_tontine.setCarnet(String.valueOf(data.getString("carnet")));
                                                    Log.i("new_t_Carnet", String.valueOf(nouvelle_tontine.getCarnet()));

                                                    nouvelle_tontine.setStatut(data.getString("statut"));
                                                    Log.i("new_t_Statut", String.valueOf(nouvelle_tontine.getStatut()));

                                                    nouvelle_tontine.setCreation(timestamp_creation);
                                                    Log.i("new_t_crea", String.valueOf(nouvelle_tontine.getCreation()));

                                                    nouvelle_tontine.setMaj(timestamp_maj);
                                                    Log.i("new_t_Maj", String.valueOf(nouvelle_tontine.getMaj()));

                                                    nouvelle_tontine.setContinuer(data.getLong("carnet"));

                                                    nouvelle_tontine.save();
                                                    id_tontine = nouvelle_tontine.getId();
                                                    Tontine old = Tontine.findById(Tontine.class, id_tontine);
                                                    Log.i("old_t", String.valueOf(old.getId()));


                                                    if (nouvelle_tontine.getPrelevement_auto()) {
                                                        // ajouter cotisations automatiques
                                                        Cotis_Auto cotis_auto = new Cotis_Auto();
                                                        cotis_auto.setTontine(nouvelle_tontine);
                                                        cotis_auto.setUtilisateur(u);
                                                        // maj des dates
                                                        cotis_auto.setCreation(timestamp_creation);
                                                        cotis_auto.setMaj(timestamp_creation);
                                                        cotis_auto.save();
                                                    }

                                                } else if (object.equals("versements")) {
                                                    Versement n_versement = new Versement();
                                                    n_versement.setFractionne(data.getString("fractionne"));
                                                    Log.i("new_V_frac", String.valueOf(data.getString("fractionne")));

                                                    n_versement.setIdVersement(data.getString("id_versement"));
                                                    Log.i("new_V_idV", String.valueOf(data.getString("id_versement")));

                                                    n_versement.setMontant(data.getString("montant"));
                                                    Log.i("new_V_Montan", String.valueOf(data.getString("montant")));

                                                    n_versement.setCreation(timestamp_creation);
                                                    n_versement.setMaj(timestamp_maj);
                                                    n_versement.setUtilisateur(u);
                                                    Tontine cible = Tontine.findById(Tontine.class, id_tontine);
                                                    n_versement.setTontine(cible);
                                                    n_versement.save();
                                                }
                                            }
                                        }
                                    }
                                    if (id_tontine!=0){
                                        Log.i("id_tontine", String.valueOf(id_tontine));
                                        //alertView("souscription ok","ok");
                                        NouvelleTontine.ViewOk alert = new NouvelleTontine.ViewOk();
                                        int nb_tontine = 0;
                                        int nb_tontine_encours = 0;
                                        int nb_tontine_terminees = 0;
                                        int nb_tontine_encaissees = 0;
                                        List<Tontine> liste_tontines = Select.from(Tontine.class)
                                                .where(Condition.prop("id_utilisateur").eq(Prefs.getString(ID_UTILISATEUR_KEY,null)) )
                                                .groupBy("carnet")
                                                .list();
                                        nb_tontine = liste_tontines.size();
                                        List<Tontine> liste_tontines_encours = Select.from(Tontine.class)
                                                .where(Condition.prop("id_utilisateur").eq(Prefs.getString(ID_UTILISATEUR_KEY,null)) )
                                                .where(Condition.prop("statut").eq(TontineEnum.IN_PROGRESS.toString()) )
                                                .list();
                                        nb_tontine_encours= liste_tontines_encours.size();
                                        List<Tontine> liste_tontines_terminees = Select.from(Tontine.class)
                                                .where(Condition.prop("id_utilisateur").eq(Prefs.getString(ID_UTILISATEUR_KEY,null)) )
                                                .where(Condition.prop("statut").eq(TontineEnum.COMPLETED.toString()) )
                                                .list();
                                        nb_tontine_terminees= liste_tontines_terminees.size();
                                        List<Tontine> liste_tontines_encaissees = Select.from(Tontine.class)
                                                .where(Condition.prop("id_utilisateur").eq(Prefs.getString(ID_UTILISATEUR_KEY,null)) )
                                                .where(Condition.prop("statut").eq(TontineEnum.COLLECTED.toString()) )
                                                .list();
                                        nb_tontine_encaissees= liste_tontines_encaissees.size();
                                        String to_tontine_user = nb_tontine+" tontine";
                                        String to_tontine_encours = nb_tontine_encours+" tontine";
                                        String to_tontine_terminees = nb_tontine_terminees+" carte";
                                        String to_tontine_encaissees = nb_tontine_encaissees+" carte";
                                        if (nb_tontine>1){
                                            to_tontine_user =nb_tontine+" tontines";
                                        }
                                        if (nb_tontine_encours>1){
                                            to_tontine_encours =nb_tontine_encours+" tontines";
                                        }
                                        if (nb_tontine_terminees>1){
                                            to_tontine_terminees =nb_tontine_terminees+" cartes";
                                        }
                                        if (nb_tontine_encaissees>1){
                                            to_tontine_encaissees =nb_tontine_encaissees+" cartes";
                                        }
                                        //ussdApi.destroyInstance();
                                        //Log.d("monde","zz:"+id);
                                        String msg="Votre tontine a été correctement enregistrée. Vous avez maintenant au total "+to_tontine_user
                                                +" dont :\n\n • "+to_tontine_encours+" en cours; \n • "+to_tontine_terminees+" terminé(e)s et \n • "+to_tontine_encaissees+" encaissé(e)s";
                                        Intent i = new Intent(NouvelleTontine.this, Message_ok.class);
                                        i.putExtra("msg_desc",msg);
                                        //i.putExtra("id_tontine",Integer.parseInt(String.valueOf(id)));
                                        i.putExtra("id_tontine",id_tontine);
                                        i.putExtra("class","com.sicmagroup.tondi.MesTontines");
                                        startActivity(i);

                                        dialog.dismiss();
                                    }else{

                                        String msg="Une erreur s'est produite. Veuillez réessayer SVP!";
                                        Intent i = new Intent(NouvelleTontine.this, Message_ok.class);
                                        i.putExtra("msg_desc",msg);
                                        i.putExtra("class","com.sicmagroup.tondi.NouvelleTontine");
                                        startActivity(i);
                                    }

                                } else {
                                    progressDialog.dismiss();

                                    String msg = result.getString("message");
                                    Intent i = new Intent(NouvelleTontine.this, Message_non.class);
                                    i.putExtra("msg_desc", msg);
                                    i.putExtra("class", "com.sicmagroup.tondi.NouvelleTontine");
                                    startActivity(i);
                                }


                            } catch (Throwable t) {
                                Log.d("errornscription", t.getMessage());
                            }
                        }
                    }
                },
                new Response.ErrorListener()
                {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {
                        progressDialog.dismiss();
                        // error
                        //Log.d("Error.Inscription", String.valueOf(error.getMessage()));
                        CoordinatorLayout mainLayout = (CoordinatorLayout)  findViewById(R.id.layout_tontine);

                        String message;
                        if (volleyError instanceof NetworkError) {
                            //Toast.makeText(Inscription.this, "error:"+volleyError.getMessage(), Toast.LENGTH_SHORT).show();
                            //Log.d("VolleyError_Test",volleyError.getMessage());
                            message = "Aucune connexion Internet!";
                            Snackbar snackbar = Snackbar
                                    .make(mainLayout, message, Snackbar.LENGTH_INDEFINITE)
                                    .setAction("REESSAYER", new View.OnClickListener() {
                                        @Override
                                        public void onClick(View view) {
                                            retry_payer(numero,montant,dialog,id_sim, heure_transaction_global, periode, prelevement_auto, mise_val, denomination_value);
                                        }
                                    });
                            snackbar.getView().setBackgroundColor(ContextCompat.getColor(NouvelleTontine.this, R.color.colorGray));
                            // Changing message text color
                            snackbar.setActionTextColor(getResources().getColor(R.color.colorPrimaryDark));
                            // Changing action button text color
                            View sbView = snackbar.getView();
                            TextView textView = (TextView) sbView.findViewById(com.google.android.material.R.id.snackbar_text);
                            textView.setTextColor(Color.WHITE);
                            snackbar.show();

                        }
                        if (volleyError instanceof AuthFailureError || volleyError instanceof TimeoutError) {
                            //Toast.makeText(Inscription.this, "error:"+volleyError.getMessage(), Toast.LENGTH_SHORT).show();
                            //Log.d("VolleyError_Test",volleyError.getMessage());
                            message = "Erreur de temporisation !";
                            Snackbar snackbar = Snackbar
                                    .make(mainLayout, message, Snackbar.LENGTH_INDEFINITE)
                                    .setAction("REESSAYER", new View.OnClickListener() {
                                        @Override
                                        public void onClick(View view) {
                                            retry_payer(numero,montant,dialog,id_sim, heure_transaction_global, periode, prelevement_auto, mise_val, denomination_value);
                                        }
                                    });
                            snackbar.getView().setBackgroundColor(ContextCompat.getColor(NouvelleTontine.this, R.color.colorGray));
                            // Changing message text color
                            snackbar.setActionTextColor(getResources().getColor(R.color.colorPrimaryDark));
                            // Changing action button text color
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
                                            retry_payer(numero,montant,dialog,id_sim, heure_transaction_global, periode, prelevement_auto, mise_val, denomination_value);
                                        }
                                    });
                            snackbar.getView().setBackgroundColor(ContextCompat.getColor(NouvelleTontine.this, R.color.colorGray));
                            // Changing message text color
                            snackbar.setActionTextColor(getResources().getColor(R.color.colorPrimaryDark));
                            // Changing action button text color
                            View sbView = snackbar.getView();
                            TextView textView = (TextView) sbView.findViewById(com.google.android.material.R.id.snackbar_text);
                            textView.setTextColor(Color.WHITE);
                            snackbar.show();
                        }  else if (volleyError instanceof ParseError) {
                            //message = "Parsing error! Please try again later";
                            message = "Une erreur est survenue! Contactez le service client.";
                            Snackbar snackbar = Snackbar
                                    .make(mainLayout, message, Snackbar.LENGTH_INDEFINITE)
                                    .setAction("REESSAYER", new View.OnClickListener() {
                                        @Override
                                        public void onClick(View view) {
                                            retry_payer(numero,montant,dialog,id_sim,heure_transaction_global, periode, prelevement_auto, mise_val, denomination_value);
                                        }
                                    });
                            snackbar.getView().setBackgroundColor(ContextCompat.getColor(NouvelleTontine.this, R.color.colorGray));
                            // Changing message text color
                            snackbar.setActionTextColor(getResources().getColor(R.color.colorPrimaryDark));
                            // Changing action button text color
                            View sbView = snackbar.getView();
                            TextView textView = (TextView) sbView.findViewById(com.google.android.material.R.id.snackbar_text);
                            textView.setTextColor(Color.WHITE);
                            snackbar.show();
                        }
                    }
                }
        ) {
            @Override
            protected Map<String, String> getParams()
            {
                Map<String, String>  params = new HashMap<String, String>();
                params.put("numero", numero);
                params.put("montant", montant);
                params.put("heure_transaction", String.valueOf(heure_transaction));
                params.put("denomination", denomination_value);
                params.put("origine", "nouvelle tontine");
                params.put("periode", periode);
                params.put("mise", mise_val);
                params.put("id_sim", id_sim);
                params.put("prelevement_auto", String.valueOf(prelevement_auto));
                Log.e("denonciation", denomination_value);
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
                80000,
                -1,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        queue.add(postRequest);

        //initialize the progress dialog and show it
        progressDialog = new ProgressDialog(NouvelleTontine.this);
        progressDialog.setCancelable(false);
        progressDialog.setMessage("Veuillez patienter SVP! \n La souscription à la tontine est en cours...");
        progressDialog.show();
    }



    private void verifier_statut(final Dialog dialog, final String id_sim  ) {
        RequestQueue queue = Volley.newRequestQueue(this);

        StringRequest postRequest = new StringRequest(Request.Method.POST, url_verifier_statut,
                new Response.Listener<String>()
                {
                    @SuppressLint("ResourceAsColor")
                    @Override
                    public void onResponse(String response) {
                        //progressDialog.dismiss();
                        // response
                        Log.d("ResponseTag", response);
                        Toast.makeText(NouvelleTontine.this, "Versé", Toast.LENGTH_SHORT).show();

                        try {

                            JSONObject result = new JSONObject(response);
                            //final JSONArray array = result.getJSONArray("data");
                            //Log.d("My App", obj.toString());
                            if (result.getBoolean("success")){
                                Prefs.putString(TRANS_STR,"");
                                verser(dialog, id_sim);
                                progressDialog.dismiss();
                                /*boolean flag = true;
                                if(){

                                }*/

                                //Toast.makeText(getApplicationContext(),"zzz",Toast.LENGTH_LONG).show();
                                /*Alerter.create(Inscription.this)
                                        .setTitle(result.getString("message"))
                                        .setIcon(R.drawable.ic_check)
                                        //.setTitleAppearance(R.style.TextAppearance_AppCompat_Large)
                                        //.setIconColorFilter(R.color.colorPrimaryDark)
                                        //.setText("Vous pouvez maintenant vous connecter.")
                                        .setBackgroundColorRes(R.color.colorPrimaryDark) // or setBackgroundColorInt(Color.CYAN)
                                        .setOnHideListener(new OnHideAlertListener() {
                                            @Override
                                            public void onHide() {

                                            }
                                        })
                                        .show();*/
                                /*Intent i = new Intent(NouvelleTontine.this, Message_ok.class);
                                i.putExtra("msg_desc",result.getString("message"));
                                ////i.putExtra("id_tontine",Integer.parseInt(String.valueOf(id)));
                                i.putExtra("class","com.sicmagroup.tondi.MesTontines");
                                startActivity(i);*/

                            }else{
                                progressDialog.dismiss();
                                /*Alerter.create(Inscription.this)
                                        .setTitle(result.getString("message"))
                                        .setIcon(R.drawable.ic_warning)
                                        .setTitleAppearance(R.style.TextAppearance_AppCompat_Large)
                                        .setIconColorFilter(R.color.colorPrimaryDark)
                                        //.setText("Vous pouvez maintenant vous connecter.")
                                        .setBackgroundColorRes(R.color.colorWhite) // or setBackgroundColorInt(Color.CYAN)
                                        .show();*/
                                String msg=result.getString("message");
                                Intent i = new Intent(NouvelleTontine.this, Message_non.class);
                                i.putExtra("msg_desc",msg);
                                i.putExtra("class","com.sicmagroup.tondi.NouvelleTontine");
                                startActivity(i);
                            }


                        } catch (Throwable t) {
                            Log.d("errornscription",t.getMessage());
                            //Log.e("My App", "Could not parse malformed JSON: \"" + response + "\"");
                        }

                    }
                },
                new Response.ErrorListener()
                {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {
                        progressDialog.dismiss();
                        // error
                        //Log.d("Error.Inscription", String.valueOf(error.getMessage()));
                        CoordinatorLayout mainLayout =  findViewById(R.id.layout_tontine);

                        String message;
                        if (volleyError instanceof NetworkError || volleyError instanceof AuthFailureError || volleyError instanceof TimeoutError) {
                            //Toast.makeText(Inscription.this, "error:"+volleyError.getMessage(), Toast.LENGTH_SHORT).show();
                            //Log.d("VolleyError_Test",volleyError.getMessage());
                            message = "Aucune connexion Internet!";
                            Snackbar snackbar = Snackbar
                                    .make(mainLayout, message, Snackbar.LENGTH_INDEFINITE)
                                    .setAction("REESSAYER", new View.OnClickListener() {
                                        @Override
                                        public void onClick(View view) {
                                            verifier_statut(dialog,id_sim);
                                        }
                                    });
                            snackbar.getView().setBackgroundColor(ContextCompat.getColor(NouvelleTontine.this, R.color.colorGray));
                            // Changing message text color
                            snackbar.setActionTextColor(getResources().getColor(R.color.colorPrimaryDark));
                            // Changing action button text color
                            View sbView = snackbar.getView();
                            TextView textView = (TextView) sbView.findViewById(com.google.android.material.R.id.snackbar_text);
                            textView.setTextColor(Color.WHITE);
                            //snackbar.show();

                        } else if (volleyError instanceof ServerError) {
                            message = "Impossible de contacter le serveur!";
                            Snackbar snackbar = Snackbar
                                    .make(mainLayout, message, Snackbar.LENGTH_INDEFINITE)
                                    .setAction("REESSAYER", new View.OnClickListener() {
                                        @Override
                                        public void onClick(View view) {
                                            verifier_statut(dialog,id_sim);
                                        }
                                    });
                            snackbar.getView().setBackgroundColor(ContextCompat.getColor(NouvelleTontine.this, R.color.colorGray));
                            // Changing message text color
                            snackbar.setActionTextColor(getResources().getColor(R.color.colorPrimaryDark));
                            // Changing action button text color
                            View sbView = snackbar.getView();
                            TextView textView = (TextView) sbView.findViewById(com.google.android.material.R.id.snackbar_text);
                            textView.setTextColor(Color.WHITE);
                            //snackbar.show();
                        }  else if (volleyError instanceof ParseError) {
                            //message = "Parsing error! Please try again later";
                            message = "Une erreur est survenue!";
                            Snackbar snackbar = Snackbar
                                    .make(mainLayout, message, Snackbar.LENGTH_INDEFINITE)
                                    .setAction("REESSAYER", new View.OnClickListener() {
                                        @Override
                                        public void onClick(View view) {
                                            verifier_statut(dialog,id_sim);
                                        }
                                    });
                            snackbar.getView().setBackgroundColor(ContextCompat.getColor(NouvelleTontine.this, R.color.colorGray));
                            // Changing message text color
                            snackbar.setActionTextColor(getResources().getColor(R.color.colorPrimaryDark));
                            // Changing action button text color
                            View sbView = snackbar.getView();
                            TextView textView = (TextView) sbView.findViewById(com.google.android.material.R.id.snackbar_text);
                            textView.setTextColor(Color.WHITE);
                            //snackbar.show();
                        }
                    }
                }
        ) {
            @Override
            protected Map<String, String> getParams()
            {


                Map<String, String>  params = new HashMap<String, String>();
                params.put("transref", Prefs.getString(TRANS_STR, null));

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
        /*postRequest.setRetryPolicy(new DefaultRetryPolicy(
                0,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));*/
        //DefaultRetryPolicy  retryPolicy = new DefaultRetryPolicy(0, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        //postRequest.setRetryPolicy(new DefaultRetryPolicy(0, -1, 0));
        //postRequest.setRetryPolicy(retryPolicy);
        postRequest.setRetryPolicy(new DefaultRetryPolicy(
                10000,
                -1,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        queue.add(postRequest);

        //initialize the progress dialog and show it
        progressDialog = new ProgressDialog(NouvelleTontine.this);
        progressDialog.setCancelable(false);
        progressDialog.setMessage("Veuillez patienter SVP! \n La validation de votre paiement est en cours...");
        progressDialog.show();
    }
    public class ViewInfos {

        void showDialog(Activity activity){
            final Dialog dialog = new Dialog(activity);
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);

            dialog.setCancelable(false);
            dialog.setContentView(R.layout.sim1_info);
            CheckBox check_sims_i = dialog.findViewById(R.id.check_sims_i);
            check_sims_i.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    // si coché
                    if (isChecked){
                        // mettre à jour la préférence dualsim_infos_key
                        Prefs.putInt(DUALSIM_INFO_KEY,1);
                    }
                    // sinon
                    else{
                        // mettre à jour la préférence dualsim_infos_key
                        Prefs.putInt(DUALSIM_INFO_KEY,0);
                    }
                }
            });

            Button dialogButton = (Button) dialog.findViewById(R.id.btn_oui);
            dialogButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialog.dismiss();
                }
            });
            //Toast.makeText(getApplicationContext(),"e",Toast.LENGTH_LONG).show();

            dialog.show();

        }
    }

    private PermissionService.Callback callback = new PermissionService.Callback() {
        @Override
        public void onRefuse(ArrayList<String> RefusePermissions) {
            Toast.makeText(NouvelleTontine.this,
                    getString(R.string.refuse_permissions),
                    Toast.LENGTH_SHORT).show();
            NouvelleTontine.this.finish();
        }

        @Override
        public void onFinally() {
            // pass
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (!Settings.canDrawOverlays(NouvelleTontine.this)) {
                    Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                            Uri.parse("package:" + NouvelleTontine.this.getPackageName()));
                    startActivity(intent);
                }
            }
        }
    };

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        callback.handler(permissions, grantResults);
    }

    //OFFLINE MODE
    public void nouvelle_tontine_ussd(final String periode, final int pauto, final String carnet, final String mise){
        // ## variables
        // periode
        ///final String periode = "Journalière";
        // prelevement auto
       /// final int pauto = 0;
        // carnet
        ///final String carnet = "1";
        // sim
        final String sim = Prefs.getString(TEL_KEY, "");
        // mise
        ///final String mise = "100";
        final int[] ussd_level = {0};

        ussdApi = USSDController.getInstance(NouvelleTontine.this);
        String phoneNumber = "*855*5*7*5*4*1*1#";
        ussdApi = USSDController.getInstance(NouvelleTontine.this);
        //startService(svc);
        //result.setText("");
        ussdApi.callUSSDInvoke(phoneNumber, map, new USSDController.CallbackInvoke() {
            @Override
            public void responseInvoke(String message) {
                Prefs.putBoolean(ACCESS_BOOL, true);
                ussd_level[0]++;
                Log.d("APPEE1", message);
                Log.d("APP_MENU_LEVEL0", String.valueOf((ussd_level[0])));
                // premiere reponse: repondre 1, envoi d'argent
                if (ussd_level[0] == 1) {
                    String periode_str = "1";
                    if(periode.equals(PeriodiciteEnum.HEBDOMADAIRE)){
                        periode_str = "2";
                    }

                    if(periode.equals(PeriodiciteEnum.MENSUELLE)){
                        periode_str = "3";
                    }

                    ussdApi.send(periode_str, new USSDController.CallbackMessage() {
                        @Override
                        public void responseMessage(String message) {
                            ussd_level[0]++;

                            Log.d("APPEE2", message);
                            Log.d("APP_MENU_LEVEL", String.valueOf((ussd_level[0])));
                            String auto_str = "2";
                            if(pauto==1){
                                auto_str = "1";
                            }
                            // deuxieme reponse: repondre 1, Abonne MM
                            if (ussd_level[0] == 2) {
                                ussdApi.send(auto_str, new USSDController.CallbackMessage() {
                                    @Override
                                    public void responseMessage(String message) {
                                        ussd_level[0]++;
                                        Log.d("APPEE", message);
                                        Log.d("APP_MENU_LEVEL", String.valueOf(ussd_level[0]));
                                        // troisième reponse: repondre par numero marchand tondi

                                    }
                                });
                            }
                        }
                    });
                }

                if (ussd_level[0] == 3) {
                    ussdApi.send("1", new USSDController.CallbackMessage() {
                        @Override
                        public void responseMessage(String message) {
                            ussd_level[0]++;
                            Log.d("APP", message);
                            // quatrième reponse: repondre par numero marchand tondi pour confirmer
                            if (ussd_level[0] == 4) {
                                // prompt saisir carnet
                                ussdApi.send("1", new USSDController.CallbackMessage() {
                                    @Override
                                    public void responseMessage(String message) {
                                        ussd_level[0]++;
                                        Log.d("APP", message);
                                        Log.d("APP_MENU_LEVEL4", String.valueOf(ussd_level[0]));
                                    }
                                });
                            }
                        }
                    });
                }


                if (ussd_level[0] == 5) {
                    // saisir carnet
                    ussdApi.send(carnet, new USSDController.CallbackMessage() {
                        @Override
                        public void responseMessage(String message) {
                            ussd_level[0]++;
                            Log.d("APP", message);
                            // quatrième reponse: repondre par numero marchand tondi pour confirmer
                            if (ussd_level[0] == 6) {
                                // prompt saisir sim
                                ussdApi.send("1", new USSDController.CallbackMessage() {
                                    @Override
                                    public void responseMessage(String message) {
                                        ussd_level[0]++;
                                        Log.d("APP", message);
                                        Log.d("APP_MENU_LEVEL4", String.valueOf(ussd_level[0]));
                                    }
                                });
                            }
                        }
                    });
                }

                if (ussd_level[0] == 7) {
                    // saisir sim
                    ussdApi.send(sim, new USSDController.CallbackMessage() {
                        @Override
                        public void responseMessage(String message) {
                            ussd_level[0]++;
                            Log.d("APP", message);
                            // quatrième reponse: repondre par numero marchand tondi pour confirmer
                            if (ussd_level[0] == 8) {
                                // prompt saisir mise
                                ussdApi.send("1", new USSDController.CallbackMessage() {
                                    @Override
                                    public void responseMessage(String message) {
                                        ussd_level[0]++;
                                        Log.d("APP", message);
                                        Log.d("APP_MENU_LEVEL4", String.valueOf(ussd_level[0]));
                                    }
                                });
                            }
                        }
                    });
                }

                if (ussd_level[0] == 9) {
                    // saisir mise
                    ussdApi.send(mise, new USSDController.CallbackMessage() {
                        @Override
                        public void responseMessage(String message) {
                            ussd_level[0]++;
                            Log.d("APP", message);
                            // quatrième reponse: repondre par numero marchand tondi pour confirmer
                            if (ussd_level[0] == 10) {
                                // prompt saisir mise
                                ussdApi.send("1", new USSDController.CallbackMessage() {
                                    @Override
                                    public void responseMessage(String message) {
                                        ussd_level[0]++;
                                        Log.d("APP", message);
                                        Log.d("APP_MENU_LEVEL4", String.valueOf(ussd_level[0]));
                                    }
                                });
                            }
                        }
                    });
                }



            }

            @Override
            public void over(String message) {
                Log.d("APPUSSpp", message);

                // traiter message quand fini OK


                Pattern pattern_msg_ok;

                pattern_msg_ok = Pattern.compile("^(Donneesrecues"+ ").+" );


                Matcher matcher = pattern_msg_ok.matcher(message.replaceAll("\\s", ""));

                // if our pattern matches the string, we can try to extract our groups
                if (matcher.find() ) {
                    Intent i = new Intent(NouvelleTontine.this, Message_ok.class);
                    String message_o="";
                    i.putExtra("msg_desc",message_o);
                    i.putExtra("class","com.sicmagroup.tondi.CarteMain");
                    startActivity(i);
                } else {
                    Intent i = new Intent(NouvelleTontine.this, Message_non.class);
                    i.putExtra("msg_desc",message);
                    i.putExtra("class","com.sicmagroup.tondi.CarteMain");
                    startActivity(i);
                    // si préférence ne pas afficher cette fenetre n'est pas defini
                    if (Prefs.getInt(DUALSIM_INFO_KEY, 0) != 1) {
                        // afficher la fenetre d'infos
                        NouvelleTontine.ViewInfos alert = new NouvelleTontine.ViewInfos();
                        alert.showDialog(NouvelleTontine.this);
                    }

                }
            }
        });
    }

}
