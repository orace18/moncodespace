package com.sicmagroup.tondi;

import android.annotation.SuppressLint;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.orm.query.Condition;
import com.orm.query.Select;
import com.pixplicity.easyprefs.library.Prefs;
import com.sicmagroup.tondi.Enum.PeriodiciteEnum;
import com.sicmagroup.tondi.Enum.TontineEnum;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import static com.sicmagroup.tondi.Connexion.ID_UTILISATEUR_KEY;
import static com.sicmagroup.tondi.Connexion.TEL_KEY;

public class AutoVersementWorker extends Worker {

    String numeroRecepteur = "";
    Context c;

    public AutoVersementWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
        new Prefs.Builder()
                .setContext(context)
                .setMode(ContextWrapper.MODE_PRIVATE)
                .setPrefsName(context.getPackageName())
                .setUseDefaultSharedPreference(true)
                .build();
        c = context;

    }

    @NonNull
    @Override
    public Result doWork() {

        try{
            autoVersementFunc(c);
            return Result.success();
        }
        catch (Exception e){
            Log.i("workerError", e.getMessage());
            return Result.failure();

        }
    }

    public void autoVersementFunc(Context context){
        Calendar cal0 = Calendar.getInstance(TimeZone.getTimeZone("GMT+1:00"));
        Date currentLocalTime0 = cal0.getTime();
        DateFormat date0 = new SimpleDateFormat("HH:mm");
//                              you can get seconds by adding  "...:ss" to it
        date0.setTimeZone(TimeZone.getTimeZone("GMT+1:00"));

        //numero Recepteur du sms de rappel
        numeroRecepteur = "229"+ Prefs.getString(TEL_KEY, "");
        //numeroRecepteur = "22996797324";

        Utilisateur utilisateurs = new Utilisateur();

        Log.d("AutoVersement", "in it!");
        if (utilisateurs.getAll().size()>0 && Prefs.getString(ID_UTILISATEUR_KEY,null)!=null){
            //Log.i("TondiJobService", "ok 1");
            // cotisations journalières
            Utilisateur u = Utilisateur.find(Utilisateur.class, "id_utilisateur = ?", Prefs.getString(ID_UTILISATEUR_KEY,null)).get(0);

            List<Cotis_Auto> cotis_auto_jour = Select.from(Cotis_Auto.class)
                    .where(Condition.prop("utilisateur").eq(u.getId()))
                    .list();
            Log.i("TondiJobService", String.valueOf(cotis_auto_jour.size()));
            // s'il y a des tontines journalières à prélèvement automatique
            if (cotis_auto_jour.size()>0){
                //Log.d("Versement_oto", String.valueOf(ct_ajour.getMaj()));

                Log.i("TondiJobService", "ok 3");
                //Log.i("TondiJobService", "auto oui");
                // vérifier si la tontine est en cours
                for (final Cotis_Auto ct_ajour:cotis_auto_jour) {
                    List<Tontine> tontine = Select.from(Tontine.class)
                            .where(Condition.prop("id").eq(ct_ajour.getTontine().getId()))
                            .where(Condition.prop("statut").eq(TontineEnum.IN_PROGRESS.toString()))
                            .list();

                    Log.d("autoVersWorker", String.valueOf(tontine.size()));
                    if ( tontine.size()>0){
                        // vérifier si nouvelle journée depuis dernière maj cotis_auto
                        // récupérer derniere date maj cotis_auto
                        Long maj = ct_ajour.getTontine().getMaj();
                        // convertir la datetime maj cotis_auto en date
                        //String d = getDate(maj,"dd/MM/yyyy");

                        Calendar cal = Calendar.getInstance(Locale.FRENCH);
                        cal.setTimeInMillis(maj);
                        if (ct_ajour.getTontine().getPeriode().equals(PeriodiciteEnum.JOURNALIERE)){
                            cal.add(Calendar.DAY_OF_WEEK, 1);
                        }
                        if (ct_ajour.getTontine().getPeriode().equals(PeriodiciteEnum.HEBDOMADAIRE)){
                            cal.add(Calendar.WEEK_OF_MONTH, 1);
                        }
                        if (ct_ajour.getTontine().getPeriode().equals(PeriodiciteEnum.MENSUELLE)){
                            cal.add(Calendar.MONTH, 1);
                        }
                        String date_next_day = android.text.format.DateFormat.format("dd/MM/yyyy", cal).toString();
                        //Log.e("next_date", date_next_day);
                        // convert date to calendar
                        Date currentDate = new Date();
                        Calendar c = Calendar.getInstance();
                        c.setTime(currentDate);
                        Calendar cal1 = Calendar.getInstance(Locale.FRENCH);
                        cal1.setTimeInMillis(c.getTimeInMillis());
                        String date_now = android.text.format.DateFormat.format("dd/MM/yyyy", cal1).toString();
                        // récupérer la date courante en milliseconds



                        Log.d("autoworker", "date_now_"+date_now+"_next_"+date_next_day);
                        // si nouvelle journée/semaine/ou mois depuis dernière maj cotis_auto
                        //if(date_now.equals(date_next_day)){
//                        if(date_now.equals(date_next_day)){
                        if(cal1.after(cal) || cal1.compareTo(cal) > 0){
                            //Log.i("TondiJobService", "01");
                            //Log.d("Versement_oto1", date_next_day+"");
                            // si auncun versement lors du rapper 1 ou 2 ou 3
                            //if ( ct_ajour.getRappelUn()!=1 && ct_ajour.getRappelDeux()!=1 && ct_ajour.getRappelTrois()!=1 ){
                            // récupérer le time actuelm
                            Intent intent_1 = new Intent(context, CarteMain.class);
                            intent_1.putExtra("id_tontine",Integer.parseInt(String.valueOf(ct_ajour.getTontine().getId()))
                            );

                            Calendar cal2 = Calendar.getInstance(TimeZone.getTimeZone("GMT+1:00"));
                            Date currentLocalTime = cal2.getTime();
                            DateFormat date = new SimpleDateFormat("HH:mm");
                            // you can get second
                            // s by adding  "...:ss" to it
                            date.setTimeZone(TimeZone.getTimeZone("GMT+1:00"));

                            String localTime = date.format(currentLocalTime);
                            int minimumMise = 1;

                            //verifiez si un versement a déjà été fait à la date_next_day, avec id_tontine de la table versmeent
                            //egal a id dans la table tontine
                            List<Versement> versements = Select.from(Versement.class)
                                    .list();
                            Boolean dejaPayer = false;
                            for (Versement v:versements) {
                                if(v.getTontine().getId() == tontine.get(0).getId() &&
                                        android.text.format.DateFormat.format("dd/MM/yyyy", v.getCreation()).toString().equals(android.text.format.DateFormat.format("dd/MM/yyyy", cal.getTimeInMillis()).toString()))
                                    dejaPayer = true;
                            }

                            Log.d("dejaPayer", String.valueOf(dejaPayer));
                            if(!dejaPayer)
                            {
                                // si time egal 08:30 ? 12:00 | 12:01 | 12:02
                                if ( (localTime.equals("12:00") || localTime.equals("12:01") || localTime.equals("12:02")) && ct_ajour.getTontine().getMise() >= minimumMise){

                                    //Log.i("TondiJobService", "03");
                                    Intent intentAction = new Intent(context,VersementReceiver.class);
                                    intentAction.setAction(context.getPackageName()+".AutoVersement");
                                    //This is optional if you have more than one buttons and want to differentiate between two
                                    intentAction.putExtra("action","verser");
                                    intentAction.putExtra("id_tontine",Integer.parseInt(String.valueOf(ct_ajour.getTontine().getId())));
                                    intentAction.putExtra("mise",Integer.parseInt(String.valueOf(ct_ajour.getTontine().getMise())));
                                    PendingIntent pendingIntent = PendingIntent.getActivity(context, 1, intent_1, PendingIntent.FLAG_UPDATE_CURRENT);
                                    PendingIntent pendingIntent2 = PendingIntent.getActivity(context, 3, intentAction, PendingIntent.FLAG_UPDATE_CURRENT);
                                    try {
                                        // Perform the operation associated with our pendingIntent
                                        pendingIntent.send();
                                        pendingIntent2.send();
                                    } catch (PendingIntent.CanceledException e) {
                                        e.printStackTrace();
                                    }

                                    NotificationCompat.Builder mBuilder =   new NotificationCompat.Builder(context)
                                            .setSmallIcon(R.drawable.icon_logo) // notification icon
                                            .setContentTitle("Cotisations Automatiques") // title for notification
                                            .setContentText("Cliquez pour confirmer la cotisation "+ct_ajour.getTontine().getPeriode()+" de "+ct_ajour.getTontine().getMise()+"F ce jour")
                                            .addAction(android.R.drawable.ic_menu_compass, "Voir la Carte", pendingIntent)
                                            .addAction(android.R.drawable.ic_menu_directions, "Confirmer la cotisation", pendingIntent2)
                                            .setAutoCancel(true); // clear notification after click
                                    @SuppressLint("WrongConstant") PendingIntent pi = PendingIntent.getActivity(context,0,intent_1, Intent.FLAG_ACTIVITY_NEW_TASK | PendingIntent.FLAG_MUTABLE);
                                    mBuilder.setContentIntent(pi);
                                    NotificationManager mNotificationManager =
                                            (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                                    mNotificationManager.notify(Integer.parseInt(String.valueOf(ct_ajour.getId())), mBuilder.build());


                                }

                                if ( (localTime.equals("15:00") || localTime.equals("15:01") || localTime.equals("15:02")) && ct_ajour.getTontine().getMise() >= minimumMise ){
                                    Intent intentAction = new Intent(context,VersementReceiver.class);
                                    //This is optional if you have more than one buttons and want to differentiate between two
                                    intentAction.putExtra("action","verser");
                                    intentAction.putExtra("id_tontine",Integer.parseInt(String.valueOf(ct_ajour.getTontine().getId())));
                                    intentAction.putExtra("mise",Integer.parseInt(String.valueOf(ct_ajour.getTontine().getMise())));
                                    PendingIntent pendingIntent = PendingIntent.getActivity(context, 1, intent_1, PendingIntent.FLAG_UPDATE_CURRENT);
                                    PendingIntent pendingIntent2 = PendingIntent.getActivity(context, 3, intentAction, PendingIntent.FLAG_UPDATE_CURRENT);
                                    try {
                                        // Perform the operation associated with our pendingIntent
                                        pendingIntent.send();
                                        pendingIntent2.send();
                                    } catch (PendingIntent.CanceledException e) {
                                        e.printStackTrace();
                                    }

                                    NotificationCompat.Builder mBuilder =   new NotificationCompat.Builder(context)
                                            .setSmallIcon(R.drawable.icon_logo) // notification icon
                                            .setContentTitle("Cotisations Automatiques") // title for notification
                                            .setContentText("Cliquez pour confirmer la cotisation du jour")
                                            .addAction(android.R.drawable.ic_menu_compass, "Voir la Carte", pendingIntent)
                                            .addAction(android.R.drawable.ic_menu_directions, "Confirmer la cotisation", pendingIntent2)
                                            .setAutoCancel(true); // clear notification after click
                                    @SuppressLint("WrongConstant") PendingIntent pi = PendingIntent.getActivity(context,0,intent_1, Intent.FLAG_ACTIVITY_NEW_TASK | PendingIntent.FLAG_MUTABLE);
                                    mBuilder.setContentIntent(pi);
                                    NotificationManager mNotificationManager =
                                            (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                                    mNotificationManager.notify(Integer.parseInt(String.valueOf(ct_ajour.getId())), mBuilder.build());

                                }

                                if (localTime.equals("17:30") && ct_ajour.getTontine().getMise() >= minimumMise){
                                    Intent intentAction = new Intent(context,VersementReceiver.class);
                                    //This is optional if you have more than one buttons and want to differentiate between two
                                    intentAction.putExtra("action","verser");
                                    intentAction.putExtra("id_tontine",Integer.parseInt(String.valueOf(ct_ajour.getTontine().getId())));
                                    intentAction.putExtra("mise",Integer.parseInt(String.valueOf(ct_ajour.getTontine().getMise())));
                                    PendingIntent pendingIntent = PendingIntent.getActivity(context, 1, intent_1, PendingIntent.FLAG_UPDATE_CURRENT);
                                    PendingIntent pendingIntent2 = PendingIntent.getActivity(context, 3, intentAction, PendingIntent.FLAG_UPDATE_CURRENT);
                                    try {
                                        // Perform the operation associated with our pendingIntent
                                        pendingIntent.send();
                                        pendingIntent2.send();
                                    } catch (PendingIntent.CanceledException e) {
                                        e.printStackTrace();
                                    }

                                    NotificationCompat.Builder mBuilder =   new NotificationCompat.Builder(context)
                                            .setSmallIcon(R.drawable.icon_logo) // notification icon
                                            .setContentTitle("Cotisations Automatiques") // title for notification
                                            .setContentText("Cliquez pour confirmer la cotisation du jour")
                                            .addAction(android.R.drawable.ic_menu_compass, "Voir la Carte", pendingIntent)
                                            .addAction(android.R.drawable.ic_menu_directions, "Confirmer la cotisation", pendingIntent2)
                                            .setAutoCancel(true); // clear notification after click
                                    @SuppressLint("WrongConstant") PendingIntent pi = PendingIntent.getActivity(context,0,intent_1, Intent.FLAG_ACTIVITY_NEW_TASK | PendingIntent.FLAG_MUTABLE);
                                    mBuilder.setContentIntent(pi);
                                    NotificationManager mNotificationManager =
                                            (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                                    mNotificationManager.notify(Integer.parseInt(String.valueOf(ct_ajour.getId())), mBuilder.build());

                                    Date currentT = Calendar.getInstance().getTime();
                                    long output_maj=currentT.getTime()/1000L;
                                    String str_maj=Long.toString(output_maj);
                                    long timestamp_maj = Long.parseLong(str_maj) * 1000;
                                    ct_ajour.setMaj(timestamp_maj);
                                    ct_ajour.save();

                                }
                                // si time superieur a 17:30
                                if (checktimings("17:30", localTime)){
                                    Date currentT = Calendar.getInstance().getTime();
                                    long output_maj=currentT.getTime()/1000L;
                                    String str_maj=Long.toString(output_maj);
                                    long timestamp_maj = Long.parseLong(str_maj) * 1000;
                                    ct_ajour.setMaj(timestamp_maj);
                                    ct_ajour.save();
                                }
                            }


                            //}
                            //Log.d("Versement_oto", String.valueOf(ct_ajour.getTontine().getMise()));
                            //Log.d("Versement_oto2", "dd:"+checktimings("17:30", "17:29")+"");

                        }
                    }
                }


                //
            }

            else
            {
                List<Tontine> tontines_automatiques = Select.from(Tontine.class)
                        .where(Condition.prop("id_utilisateur").eq(Long.valueOf(Prefs.getString(ID_UTILISATEUR_KEY,null))))
                        .where(Condition.prop("statut").eq(TontineEnum.IN_PROGRESS.toString()))
                        .where(Condition.prop("prelevement_auto").eq(1))
                        .list();
                if (tontines_automatiques.size()>0)
                {
                    Log.d("AutoVersement", "Size auto tontine"+tontines_automatiques.size());
                    for (Tontine t:tontines_automatiques)
                    {

                        //obtention de la date de dernier versement
                        List<Versement> versements_personnaliser = Select.from(Versement.class)
                                .where(Condition.prop("tontine").eq(t.getId()))
                                .where(Condition.prop("fractionne").eq(false))
                                .orderBy("creation desc")
                                .list();
                        Log.d("AutoVersement", "Size versement"+versements_personnaliser.size());
                        // vérifier si nouvelle journée depuis dernière maj cotis_auto
                        // récupérer derniere date maj cotis_auto
                        Long maj = t.getCreation();
                        if (versements_personnaliser.size()>0)
                            maj = versements_personnaliser.get(0).getCreation();

                        // convertir la datetime maj cotis_auto en date
                        //String d = getDate(maj,"dd/MM/yyyy");

                        Calendar cal = Calendar.getInstance(Locale.FRENCH);
                        cal.setTimeInMillis(maj);
                        if (t.getPeriode().equals(PeriodiciteEnum.JOURNALIERE)){
                            cal.add(Calendar.DAY_OF_WEEK, 1);
                        }
                        if (t.getPeriode().equals(PeriodiciteEnum.HEBDOMADAIRE)){
                            cal.add(Calendar.WEEK_OF_MONTH, 1);
                        }
                        if (t.getPeriode().equals(PeriodiciteEnum.MENSUELLE)){
                            cal.add(Calendar.MONTH, 1);
                        }
                        String date_next_day = android.text.format.DateFormat.format("yyyy-MM-dd", cal).toString();
                        //Log.e("next_date", date_next_day);
                        // convert date to calendar
                        Date currentDate = new Date();
                        Calendar c = Calendar.getInstance();
                        c.setTime(currentDate);
                        Calendar cal1 = Calendar.getInstance(Locale.FRENCH);
                        cal1.setTimeInMillis(c.getTimeInMillis());
                        String date_now = android.text.format.DateFormat.format("yyyy-MM-dd", cal1).toString();
                        // récupérer la date courante en milliseconds



                        Log.d("AutoVersement", "date_now_"+date_now+"_next_"+date_next_day+" _ "+t.getPeriode());
                        // si nouvelle journée/semaine/ou mois depuis dernière maj cotis_auto
                        //if(date_now.equals(date_next_day)){
                        if(cal1.after(cal) || cal1.compareTo(cal) > 0){


                            Intent intent_1 = new Intent(context, CarteMain.class);
                            intent_1.putExtra("id_tontine",Integer.parseInt(String.valueOf(t.getId()))
                            );

                            Calendar cal2 = Calendar.getInstance(TimeZone.getTimeZone("GMT+1:00"));
                            Date currentLocalTime = cal2.getTime();
                            DateFormat date = new SimpleDateFormat("HH:mm");
                            // you can get second
                            // s by adding  "...:ss" to it
                            date.setTimeZone(TimeZone.getTimeZone("GMT+1:00"));

                            String localTime = date.format(currentLocalTime);
                            int minimumMise = 2;

                            //verifiez si un versement a déjà été fait à la date_next_day, avec id_tontine de la table versmeent
                            //egal a id dans la table tontine
                            List<Versement> versements = Select.from(Versement.class)
                                    .list();
                            Boolean dejaPayer = false;
                            for (Versement v:versements) {
                                if(v.getTontine().getId() == t.getId() &&
                                        android.text.format.DateFormat.format("yyyy-MM-dd", v.getCreation()).toString().equals(android.text.format.DateFormat.format("yyyy-MM-dd", cal.getTimeInMillis()).toString()))
                                    dejaPayer = true;
                            }

                            Log.d("dejaPayer_"+t.getPeriode()+"_"+t.getMise(), String.valueOf(dejaPayer));
                            if(dejaPayer == false)
                            {
                                // si time egal 08:30 ? 12:00 | 12:01 | 12:02
                                if ( (localTime.equals("12:00") || localTime.equals("12:01") || localTime.equals("12:02"))  && t.getMise() >= minimumMise){

                                    //Log.i("TondiJobService", "03");
                                    Intent intentAction = new Intent(context,VersementReceiver.class);
                                    intentAction.setAction(context.getPackageName()+".AutoVersement");
                                    //This is optional if you have more than one buttons and want to differentiate between two
                                    intentAction.putExtra("action","verser");
                                    intentAction.putExtra("id_tontine",Integer.parseInt(String.valueOf(t.getId())));
                                    intentAction.putExtra("mise",Integer.parseInt(String.valueOf(t.getMise())));
                                    PendingIntent pendingIntent = PendingIntent.getActivity(context, 1, intent_1, PendingIntent.FLAG_UPDATE_CURRENT);
                                    PendingIntent pendingIntent2 = PendingIntent.getActivity(context, 3, intentAction, PendingIntent.FLAG_UPDATE_CURRENT);
                                    try {
                                        // Perform the operation associated with our pendingIntent
                                        pendingIntent.send();
                                        pendingIntent2.send();
                                    } catch (PendingIntent.CanceledException e) {
                                        e.printStackTrace();
                                    }

                                    NotificationCompat.Builder mBuilder =   new NotificationCompat.Builder(context)
                                            .setSmallIcon(R.drawable.icon_logo) // notification icon
                                            .setContentTitle("Cotisations Automatiques") // title for notification
                                            .setContentText("Cliquez pour confirmer la cotisation "+t.getPeriode()+" de "+t.getMise()+"F ce jour")
                                            .addAction(android.R.drawable.ic_menu_compass, "Voir la Carte", pendingIntent)
                                            .addAction(android.R.drawable.ic_menu_directions, "Confirmer la cotisation", pendingIntent2)
                                            .setAutoCancel(true); // clear notification after click
                                    @SuppressLint("WrongConstant") PendingIntent pi = PendingIntent.getActivity(context,0,intent_1, Intent.FLAG_ACTIVITY_NEW_TASK | PendingIntent.FLAG_MUTABLE);
                                    mBuilder.setContentIntent(pi);
                                    NotificationManager mNotificationManager =
                                            (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                                    mNotificationManager.notify(Integer.parseInt(String.valueOf(t.getId())), mBuilder.build());

                                }


                                if ( (localTime.equals("15:00") || localTime.equals("15:01") || localTime.equals("15:02") || localTime.equals("16:02")) && t.getMise() >= minimumMise ){
                                    Intent intentAction = new Intent(context,VersementReceiver.class);
                                    //This is optional if you have more than one buttons and want to differentiate between two
                                    intentAction.putExtra("action","verser");
                                    intentAction.putExtra("id_tontine",Integer.parseInt(String.valueOf(t.getId())));
                                    intentAction.putExtra("mise",Integer.parseInt(String.valueOf(t.getMise())));
                                    PendingIntent pendingIntent = PendingIntent.getActivity(context, 1, intent_1, PendingIntent.FLAG_UPDATE_CURRENT);
                                    PendingIntent pendingIntent2 = PendingIntent.getActivity(context, 3, intentAction, PendingIntent.FLAG_UPDATE_CURRENT);
                                    try {
                                        // Perform the operation associated with our pendingIntent
                                        pendingIntent.send();
                                        pendingIntent2.send();
                                    } catch (PendingIntent.CanceledException e) {
                                        e.printStackTrace();
                                    }

                                    NotificationCompat.Builder mBuilder =   new NotificationCompat.Builder(context)
                                            .setSmallIcon(R.drawable.icon_logo) // notification icon
                                            .setContentTitle("Cotisations Automatiques") // title for notification
                                            .setContentText("Cliquez pour confirmer la cotisation du jour")
                                            .addAction(android.R.drawable.ic_menu_compass, "Voir la Carte", pendingIntent)
                                            .addAction(android.R.drawable.ic_menu_directions, "Confirmer la cotisation", pendingIntent2)
                                            .setAutoCancel(true); // clear notification after click
                                    @SuppressLint("WrongConstant") PendingIntent pi = PendingIntent.getActivity(context,0,intent_1, Intent.FLAG_ACTIVITY_NEW_TASK | PendingIntent.FLAG_MUTABLE);
                                    mBuilder.setContentIntent(pi);
                                    NotificationManager mNotificationManager =
                                            (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                                    mNotificationManager.notify(Integer.parseInt(String.valueOf(t.getId())), mBuilder.build());
                                }
                                if (localTime.equals("17:30") && t.getMise() >= minimumMise){
                                    Intent intentAction = new Intent(context,VersementReceiver.class);
                                    //This is optional if you have more than one buttons and want to differentiate between two
                                    intentAction.putExtra("action","verser");
                                    intentAction.putExtra("id_tontine",Integer.parseInt(String.valueOf(t.getId())));
                                    intentAction.putExtra("mise",Integer.parseInt(String.valueOf(t.getMise())));
                                    PendingIntent pendingIntent = PendingIntent.getActivity(context, 1, intent_1, PendingIntent.FLAG_UPDATE_CURRENT);
                                    PendingIntent pendingIntent2 = PendingIntent.getActivity(context, 3, intentAction, PendingIntent.FLAG_UPDATE_CURRENT);
                                    try {
                                        // Perform the operation associated with our pendingIntent
                                        pendingIntent.send();
                                        pendingIntent2.send();
                                    } catch (PendingIntent.CanceledException e) {
                                        e.printStackTrace();
                                    }

                                    NotificationCompat.Builder mBuilder =   new NotificationCompat.Builder(context)
                                            .setSmallIcon(R.drawable.icon_logo) // notification icon
                                            .setContentTitle("Cotisations Automatiques") // title for notification
                                            .setContentText("Cliquez pour confirmer la cotisation du jour")
                                            .addAction(android.R.drawable.ic_menu_compass, "Voir la Carte", pendingIntent)
                                            .addAction(android.R.drawable.ic_menu_directions, "Confirmer la cotisation", pendingIntent2)
                                            .setAutoCancel(true); // clear notification after click
                                    @SuppressLint("WrongConstant") PendingIntent pi = PendingIntent.getActivity(context,0,intent_1, Intent.FLAG_ACTIVITY_NEW_TASK | PendingIntent.FLAG_MUTABLE);
                                    mBuilder.setContentIntent(pi);
                                    NotificationManager mNotificationManager =
                                            (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                                    mNotificationManager.notify(Integer.parseInt(String.valueOf(t.getId())), mBuilder.build());

                                    Date currentT = Calendar.getInstance().getTime();
                                    long output_maj=currentT.getTime()/1000L;
                                    String str_maj=Long.toString(output_maj);
                                    long timestamp_maj = Long.parseLong(str_maj) * 1000;
                                   /* t.setMaj(timestamp_maj);
                                    t.save();*/



                                }
                                // si time superieur a 17:30
                                if (checktimings("17:30", localTime)){
                                    Date currentT = Calendar.getInstance().getTime();
                                    long output_maj=currentT.getTime()/1000L;
                                    String str_maj=Long.toString(output_maj);
                                    long timestamp_maj = Long.parseLong(str_maj) * 1000;
                                    /*t.setMaj(timestamp_maj);
                                    t.save();*/
                                }
                            }


                            //}
                            //Log.d("Versement_oto", String.valueOf(ct_ajour.getTontine().getMise()));
                            //Log.d("Versement_oto2", "dd:"+checktimings("17:30", "17:29")+"");

                        }
                    }

                }

            }

            // Release the wake lock provided by the WakefulBroadcastReceiver.
//            WakefulBroadcastReceiver.completeWakefulIntent(context.getI);
            // JobInfo.Builder job = new JobInfo.Builder(12345, intent.getComponent());


        }


    }

    private boolean checktimings(String time, String endtime) {

        String pattern = "HH:mm";
        SimpleDateFormat sdf = new SimpleDateFormat(pattern);

        try {
            Date date1 = sdf.parse(time);
            Date date2 = sdf.parse(endtime);

            if(date1.before(date2)) {
                return true;
            } else {

                return false;
            }
        } catch (ParseException e){
            e.printStackTrace();
        }
        return false;
    }

}
