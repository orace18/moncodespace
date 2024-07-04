package com.sicmagroup.tondi;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import com.orm.query.Condition;
import com.orm.query.Select;
import com.pixplicity.easyprefs.library.Prefs;

import com.sicmagroup.tondi.Enum.TontineEnum;
import com.sicmagroup.ussdlibra.USSDController;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.sicmagroup.tondi.Connexion.ID_UTILISATEUR_KEY;
import static com.sicmagroup.tondi.Connexion.MTN_TEST;
import static com.sicmagroup.tondi.Connexion.utilisateurSms;

public class VersementReceiver extends BroadcastReceiver {

    private HashMap<String, HashSet<String>> map;
    private USSDController ussdApi;
    int id_tontine;
    int mise;
    int nb_vers_defaut = 0;
    Context mcontext;
    @Override
    public void onReceive(Context context, Intent intent) {
        mcontext =context;

        Toast.makeText(context,"recieved",Toast.LENGTH_SHORT).show();
        id_tontine = intent.getIntExtra("id_tontine",0);
        mise = intent.getIntExtra("mise",0);
        String action=intent.getStringExtra("action");

        // si cotisation auto
        if(action.equals("verser")){
            pay_via_ussd(String.valueOf(mise));
        }


        //This is used to close the notification tray
        Intent it = new Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
//        context.sendBroadcast(it);
    }

    private void pay_via_ussd(final String montant_value){
        String phoneNumber = "*400#".trim();
        ussdApi = USSDController.getInstance(mcontext);
        //result.setText("");
        ussdApi.callUSSDInvoke(phoneNumber, map, new USSDController.CallbackInvoke() {
            @Override
            public void responseInvoke(String message) {
                Log.d("APPUSSD", message);

                //result.append("\n-\n" + message);
                // first option list - select option 1
                            /*ussdApi.send("1", new USSDController.CallbackMessage() {
                                @Override
                                public void responseMessage(String message) {
                                    Log.d("APPUSSD", message);
                                    //result.append("\n-\n" + message);
                                    // second option list - select option 1
                                    ussdApi.send("1", new USSDController.CallbackMessage() {
                                        @Override
                                        public void responseMessage(String message) {
                                            Log.d("APPUSSD", message);
                                            // saisir numero
                                            ussdApi.send(MTN_TEST, new USSDController.CallbackMessage() {
                                                @Override
                                                public void responseMessage(String message) {
                                                    Log.d("APPUSSD", message);
                                                    // confirmer numero
                                                    ussdApi.send(MTN_TEST, new USSDController.CallbackMessage() {
                                                        @Override
                                                        public void responseMessage(String message) {
                                                            Log.d("APPUSSD", message);
                                                            // montant
                                                            ussdApi.send("100", new USSDController.CallbackMessage() {
                                                                @Override
                                                                public void responseMessage(String message) {
                                                                    Log.d("APPUSSD", message);
                                                                    // motif
                                                                    ussdApi.send("Versement Tontine Digitale", new USSDController.CallbackMessage() {
                                                                        @Override
                                                                        public void responseMessage(String message) {
                                                                            Log.d("APPUSSD", message);
                                                                            // confirmer avec pin
                                                                        }
                                                                    });
                                                                }
                                                            });
                                                        }
                                                    });
                                                }

                                            });
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
                String message_ok = "Transfert effectue pour  "+montant_value+" FCFA a "+MTN_TEST;

                Pattern pattern_msg_ok = Pattern.compile("^(Transferteffectuepour"+montant_value+")FCFA.+"+MTN_TEST);
                Matcher matcher = pattern_msg_ok.matcher(message.replaceAll("\\s",""));

                // if our pattern matches the string, we can try to extract our groups
                if (matcher.find()) {
                    verser(montant_value);
                }else{
                    /*Alerter.create(CarteMain.this)
                            .setTitle(message)
                            .setIcon(R.drawable.ic_warning)
                            .setTitleAppearance(R.style.TextAppearance_AppCompat_Large)
                            .setIconColorFilter(R.color.colorPrimaryDark)
                            //.setText("Vous pouvez maintenant vous connecter.")
                            .setBackgroundColorRes(R.color.colorWhite) // or setBackgroundColorInt(Color.CYAN)
                            .show();*/
                }

                /*if(message.equals(message_ok)){

                }*/

            }
        });
    }

    private void verser(final String montant) {
        Versement versement = new Versement();
        Tontine t = Tontine.findById(Tontine.class, (long) id_tontine);
        // besoin de fractionner
        // recuperer le nombre de versements effectues
        double nb_vers_eff = t.getMontant()/t.getMise();
        // recuperer le nombre de versements restants
        double nb_vers_rest = nb_vers_defaut-nb_vers_eff;
        // recuperer le nombre de versements à éffectuer
        double nb_vers_a_eff = Integer.parseInt(montant)/t.getMise();
        Long id_vers = null;
        // si le nombre nb_vers_a_eff>nb_vers_rest
        if (nb_vers_a_eff>nb_vers_rest){
            // verser la fraction principale
            Versement v = new Versement();
            v.setFractionne("1");
            v.setIdVersement("0");
            v.setTontine(t);
            v.setMontant(montant);
            // maj des dates
            Date currentTime = Calendar.getInstance().getTime();
            long output_creation=currentTime.getTime()/1000L;
            String str_creation=Long.toString(output_creation);
            long timestamp_creation = Long.parseLong(str_creation) * 1000;
            long output_maj=currentTime.getTime()/1000L;
            String str_maj=Long.toString(output_maj);
            long timestamp_maj = Long.parseLong(str_maj) * 1000;
            v.setCreation(timestamp_creation);
            v.setMaj(timestamp_maj);
            v.save();
            long id_versement = v.getId();
            // verser le rest de la tontine actuelle
            Versement v1 = new Versement();
            v1.setFractionne("0");
            v1.setIdVersement(String.valueOf(id_versement));
            v1.setTontine(t);
            v1.setMontant(String.valueOf(nb_vers_rest*t.getMise()));
            // maj des dates
            v1.setCreation(timestamp_creation);
            v1.setMaj(timestamp_maj);
            v1.save();
            // Terminer la tontine
            t.setStatut(TontineEnum.COMPLETED.toString());
            t.setMaj(timestamp_maj);
            t.save();
            nb_vers_rest = nb_vers_a_eff-nb_vers_rest;
            nb_vers_a_eff = nb_vers_rest;
            if (nb_vers_rest>nb_vers_defaut){
                nb_vers_rest = nb_vers_defaut;
            }
            Tontine tn = new Tontine();
            // tant que le nombre nb_vers_rest est >=0 et nb_vers_a_eff>nb_vers_rest
            while (nb_vers_a_eff>nb_vers_rest){

                // créer une nouvelle tontine
                tn = new Tontine();
                tn.setMise(t.getMise());
                tn.setIdSim(t.getIdSim());
                tn.setId_utilisateur(t.getId_utilisateur());
                tn.setPeriode(t.getPeriode());
                tn.setStatut(TontineEnum.IN_PROGRESS.toString());
                tn.setCreation(timestamp_creation);
                tn.setMaj(timestamp_maj);
                tn.save();

                // verser le rest de la tontine actuelle
                Versement v2 = new Versement();
                v2.setFractionne("0");
                v2.setIdVersement(String.valueOf(id_versement));
                v2.setTontine(tn);
                v2.setMontant(String.valueOf(nb_vers_rest*tn.getMise()));
                // maj des dates
                v2.setCreation(timestamp_creation);
                v2.setMaj(timestamp_maj);
                v2.save();

                //v.setMontant(String.valueOf(nb_vers_rest*t.getMise()));
                //versement.save();
                // mettre à jour nb_vers_a_eff
                nb_vers_rest = nb_vers_defaut-(tn.getMontant()/tn.getMise());
                nb_vers_a_eff = nb_vers_a_eff-nb_vers_rest;
            }

            // créer une nouvelle tontine
            tn = new Tontine();
            tn.setMise(t.getMise());
            tn.setIdSim(t.getIdSim());
            tn.setId_utilisateur(t.getId_utilisateur());
            tn.setPeriode(t.getPeriode());
            tn.setStatut(TontineEnum.IN_PROGRESS.toString());
            tn.setCreation(timestamp_creation);
            tn.setMaj(timestamp_maj);
            tn.save();

            // verser le rest de la tontine actuelle
            Versement v2 = new Versement();
            v2.setFractionne("0");
            v2.setIdVersement(String.valueOf(id_versement));
            v2.setTontine(tn);
            v2.setMontant(String.valueOf(nb_vers_rest*tn.getMise()));
            // maj des dates
            v2.setCreation(timestamp_creation);
            v2.setMaj(timestamp_maj);
            v2.save();
            // si prelevement auto
            if (t.getPrelevement_auto()){
                Utilisateur u = Utilisateur.find(Utilisateur.class, "id_utilisateur = ?", Prefs.getString(ID_UTILISATEUR_KEY,null)).get(0);

                List<Cotis_Auto> cotis_auto_jour =Select.from(Cotis_Auto.class)
                        .where(Condition.prop("utilisateur").eq(u.getId() ))
                        .where(Condition.prop("tontine").eq(t.getId()))
                        .list();
                Date currentT = Calendar.getInstance().getTime();
                long output_maj1=currentT.getTime()/1000L;
                String str_maj1=Long.toString(output_maj1);
                long timestamp_maj1 = Long.parseLong(str_maj1) * 1000;
                cotis_auto_jour.get(0).setMaj(timestamp_maj1);
                cotis_auto_jour.get(0).save();
            }
            id_vers=id_versement;
        }else{
            // verser le a_effectue de la tontine actuelle
            //Toast.makeText(getApplicationContext(),"t:"+String.valueOf(nb_vers_a_eff*t.getMise()),Toast.LENGTH_LONG).show();
            Versement v = new Versement();

            v.setFractionne("0");
            v.setIdVersement("0");
            v.setTontine(t);
            v.setMontant(String.valueOf(nb_vers_a_eff*t.getMise()));
            // maj des dates
            Date currentTime = Calendar.getInstance().getTime();
            long output_creation=currentTime.getTime()/1000L;
            String str_creation=Long.toString(output_creation);
            long timestamp_creation = Long.parseLong(str_creation) * 1000;
            long output_maj=currentTime.getTime()/1000L;
            String str_maj=Long.toString(output_maj);
            long timestamp_maj = Long.parseLong(str_maj) * 1000;
            v.setCreation(timestamp_creation);
            v.setMaj(timestamp_maj);
            if (nb_vers_a_eff==nb_vers_rest){
                // Terminer la tontine
                t.setStatut(TontineEnum.COMPLETED.toString());
                t.setMaj(timestamp_maj);
                t.save();
            }
            v.save();
            // si prelevement auto
            if (t.getPrelevement_auto()){
                Utilisateur u = Utilisateur.find(Utilisateur.class, "id_utilisateur = ?", Prefs.getString(ID_UTILISATEUR_KEY,null)).get(0);

                List<Cotis_Auto>  cotis_auto_jour =Select.from(Cotis_Auto.class)
                        .where(Condition.prop("utilisateur").eq(u.getId()))
                        .where(Condition.prop("tontine").eq(t.getId()))
                        .list();
                Date currentT = Calendar.getInstance().getTime();
                long output_maj1=currentT.getTime()/1000L;
                String str_maj1=Long.toString(output_maj1);
                long timestamp_maj1 = Long.parseLong(str_maj1) * 1000;
                cotis_auto_jour.get(0).setMaj(timestamp_maj1);
                cotis_auto_jour.get(0).save();
            }
            id_vers = v.getId();
        }



        /*List<Versement> versements = Select.from(Versement.class)
                .where(Condition.prop("tontine").eq(id_tontine))
                .where(Condition.prop("fractionne").eq(false))
                .list();



        versement.setFractionne("0");
        versement.setIdVersement("0");
        versement.setTontine(t);
        versement.setMontant(montant);
        versement.save();*/

        if (id_vers!=null){
            // Mettre à jour la préférence id tontine
            /*Alerter.create(AutoVersement.this)
                    //.setTitle(result.getString("message"))
                    .setTitle(montant+" F versé")
                    .setIcon(R.drawable.ic_check)
                    //.setTitleAppearance(R.style.TextAppearance_AppCompat_Large)
                    //.setIconColorFilter(R.color.colorPrimaryDark)
                    //.setText("Vous pouvez maintenant vous connecter.")
                    .setBackgroundColorRes(R.color.colorPrimaryDark) // or setBackgroundColorInt(Color.CYAN)
                    .setOnHideListener(new OnHideAlertListener() {
                        @Override
                        public void onHide() {
                            CarteMain.this.finish();
                            Intent intent = new Intent(CarteMain.this,CarteMain.class);
                            intent.putExtra("id_tontine",id_tontine);
                            startActivity(intent);
                        }
                    })
                    .show();*/
        }else{
            /*Alerter.create(CarteMain.this)
                    .setTitle("Une erreur est survenue lors du versement. Veuillez réessayer SVP!")
                    .setIcon(R.drawable.ic_warning)
                    .setTitleAppearance(R.style.TextAppearance_AppCompat_Large)
                    .setIconColorFilter(R.color.colorPrimaryDark)
                    //.setText("Vous pouvez maintenant vous connecter.")
                    .setBackgroundColorRes(R.color.colorWhite) // or setBackgroundColorInt(Color.CYAN)
                    .show();*/
        }


    }

    public void performAction1(){

    }

    public void performAction2(){

    }

}