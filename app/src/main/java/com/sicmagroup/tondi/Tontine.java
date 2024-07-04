package com.sicmagroup.tondi;

import android.content.Context;
import android.util.Log;

import com.google.gson.Gson;
import com.orm.SugarRecord;
import com.orm.query.Condition;
import com.orm.query.Select;
import com.pixplicity.easyprefs.library.Prefs;
import com.sicmagroup.tondi.Enum.TontineEnum;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import static com.sicmagroup.tondi.Accueil.CARTE_NAV_KEY;
import static com.sicmagroup.tondi.Connexion.ID_UTILISATEUR_KEY;

public class Tontine extends SugarRecord<Tontine> {
    private String idUtilisateur;
    private String periode;
    private Boolean prelevementAuto;
    private String idSim;
    private int mise;
    private String statut;
    private Long creation;
    private Long maj;
    private Long continuer;
    private String dateDeblocage;
    private String carnet;
    private String idServer;
    private String denomination;

    public String getDateDeblocage() {
        return dateDeblocage;
    }

    public void setDateDeblocage(String dateDeblocage) {
        this.dateDeblocage = dateDeblocage;
    }


    public String getDenomination() {
        return denomination;
    }

    public void setDenomination(String denomination) {
        this.denomination = denomination;
    }


    public Tontine() {

    }

    public String getId_server()
    {
        return idServer;
    }

    public void setId_server(String id_server)
    {
        this.idServer = id_server;
    }

    public String getPeriode() {
        return periode;
    }

    public void setPeriode(String periode) {
        this.periode = periode;
    }


    public String getId_utilisateur() {
        return idUtilisateur;
    }

    public void setId_utilisateur(String id_utilisateur) {
        this.idUtilisateur = id_utilisateur;
    }

    public int getMise() {
        return mise;
    }

    public void setMise(int mise) {
        this.mise = mise;
    }


    public Boolean getPrelevement_auto() {
        return prelevementAuto;
    }

    public void setPrelevement_auto(Boolean prelevement_auto) {
        this.prelevementAuto = prelevement_auto;
    }

    public String getStatut() {
        return statut;
    }

    public int getFinished(String carnet) {
        List<Tontine> tontines = Select.from(Tontine.class)
                .where(Condition.prop("id_utilisateur").eq(Prefs.getString(ID_UTILISATEUR_KEY,null)),
                        Condition.prop("statut").eq(TontineEnum.COLLECTED.toString()),
                        Condition.prop("periode").eq(this.getPeriode()),
                        Condition.prop("mise").eq(this.getMise()),
                        Condition.prop("carnet").eq(carnet))
                .orderBy("id desc")
                .list();

        return tontines.size();
    }
    public int getMontant() {
        int montant = 0;
        String id_utilisateur = Prefs.getString(ID_UTILISATEUR_KEY,null);
        Utilisateur u = Utilisateur.find(Utilisateur.class, "id_utilisateur = ?", Prefs.getString(ID_UTILISATEUR_KEY,null)).get(0);

        if(id_utilisateur != null) {
            List<Versement> versements = Select.from(Versement.class)
                    .where(Condition.prop("tontine").eq(this.getId()))
                    .where(Condition.prop("fractionne").eq("false"))
                    .where(Condition.prop("utilisateur").eq(u.getId()))
                    .list();
            if (versements != null) {
                for (Versement v : versements) {
                    montant += Integer.parseInt(v.getMontant());
                    // si fractionné, 1
                /*if (v.getFractionne().equals("1")){
                    montant += Integer.parseInt(v.getMontant());
                }
                // si pas fractionné, 0
                if (v.getFractionne().equals("0")){
                    montant += Integer.parseInt(v.getMontant());
                }*/
                }

            }
            return montant;
        } else {
            return -1;
        }
    }
    public int getMontantACotiser() {
        return this.getMise()*372;
    }

    public int getMontCumule(String statut) {
        statut = this.getStatut();
        Utilisateur u = Utilisateur.find(Utilisateur.class, "id_utilisateur = ?", Prefs.getString(ID_UTILISATEUR_KEY,null)).get(0);

        int montant = 0;
        List<Tontine> tontines;
        if (statut.equals(TontineEnum.IN_PROGRESS.toString())){
            // identifier tontines à cumuler
            tontines = Select.from(Tontine.class)
                    //.where(Condition.prop("periode").eq(this.getPeriode()))
                    //.where(Condition.prop("mise").eq(this.getMise()))
                    .where(Condition.prop("carnet").eq(this.getCarnet()))
                    .where(Condition.prop("continuer").eq(this.getContinuer()))
                    .where(Condition.prop("mise").eq(this.getMise()))
                    .where(Condition.prop("periode").eq(this.getPeriode()))
                    .where(Condition.prop("statut").notEq("encaissee"))
                    .where(Condition.prop("id_utilisateur").eq(Prefs.getString(ID_UTILISATEUR_KEY,null)))
                    //.groupBy("continuer")
                    .list();
        }else{
            // identifier tontines à cumuler
            tontines = Select.from(Tontine.class)
                    //.where(Condition.prop("periode").eq(this.getPeriode()))
                    //.where(Condition.prop("mise").eq(this.getMise()))
                    .where(Condition.prop("carnet").eq(this.getCarnet()))
                    .where(Condition.prop("continuer").eq(this.getContinuer()))
                    .where(Condition.prop("mise").eq(this.getMise()))
                    .where(Condition.prop("periode").eq(this.getPeriode()))
                    .where(Condition.prop("statut").eq(statut))
                    .where(Condition.prop("id_utilisateur").eq(Prefs.getString(ID_UTILISATEUR_KEY,null)))
                    //.groupBy("continuer")
                    .list();
        }

        if (tontines!=null){
            for (Tontine t:tontines){
                List<Versement> versements = Select.from(Versement.class)
                        .where(Condition.prop("tontine").eq(t.getId()))
                        //.where(Condition.prop("tontine").eq(this.getMise()))
                        .where(Condition.prop("fractionne").eq("false"))
                        .where(Condition.prop("utilisateur").eq(u.getId()))
                        .list();
                if (versements!=null){
                    for (Versement v:versements){
                        montant += Integer.parseInt(v.getMontant());
                    }

                }
            }

        }

        return montant;
    }

    public int getMontCumuleEnc() {
        int montant = 0;
        Utilisateur u = Utilisateur.find(Utilisateur.class, "id_utilisateur = ?", Prefs.getString(ID_UTILISATEUR_KEY,null)).get(0);

        // identifier tontines à cumuler
        List<Tontine> tontines = Select.from(Tontine.class)
                //.where(Condition.prop("periode").eq(this.getPeriode()))
                //.where(Condition.prop("mise").eq(this.getMise()))
                .where(Condition.prop("carnet").eq(this.getCarnet()))
                .where(Condition.prop("continuer").eq(this.getContinuer()))
                .where(Condition.prop("mise").eq(this.getMise()))
                .where(Condition.prop("periode").eq(this.getPeriode()))
                .where(Condition.prop("statut").eq(TontineEnum.COMPLETED.toString()))
                .where(Condition.prop("id_utilisateur").eq(Prefs.getString(ID_UTILISATEUR_KEY,null)))
                .list();
        if (tontines!=null){
            for (Tontine t:tontines){
                //Log.d("tontineeee_pp",t.getPeriode());
                List<Versement> versements = Select.from(Versement.class)
                        .where(Condition.prop("tontine").eq(t.getId()))
                        //.where(Condition.prop("tontine").eq(this.getMise()))
                        .where(Condition.prop("fractionne").eq("false"))
                        .where(Condition.prop("utilisateur").eq(u.getId()))
                        .list();
                if (versements!=null){
                    for (Versement v:versements){
                        montant += Integer.parseInt(v.getMontant());
                    }

                }
            }

        }

        return montant;
    }

    public int getMontCumuleEncMarchandRetry() {
        int montant = 0;
        // identifier tontines à cumuler
        Utilisateur u = Utilisateur.find(Utilisateur.class, "id_utilisateur = ?", Prefs.getString(ID_UTILISATEUR_KEY,null)).get(0);

        List<Tontine> tontines = Select.from(Tontine.class)
                //.where(Condition.prop("periode").eq(this.getPeriode()))
                //.where(Condition.prop("mise").eq(this.getMise()))
                .where(Condition.prop("carnet").eq(this.getCarnet()))
                .where(Condition.prop("continuer").eq(this.getContinuer()))
                .where(Condition.prop("mise").eq(this.getMise()))
                .where(Condition.prop("periode").eq(this.getPeriode()))
                .where(Condition.prop("statut").eq(TontineEnum.WAITING.toString()))
                .where(Condition.prop("id_utilisateur").eq(Prefs.getString(ID_UTILISATEUR_KEY,null)))
                .list();
        if (tontines!=null){
            for (Tontine t:tontines){
                //Log.d("tontineeee_pp",t.getPeriode());
                List<Versement> versements = Select.from(Versement.class)
                        .where(Condition.prop("tontine").eq(t.getId()))
                        //.where(Condition.prop("tontine").eq(this.getMise()))
                        .where(Condition.prop("fractionne").eq("false"))
                        .where(Condition.prop("utilisateur").eq(u.getId()))
                        .list();
                if (versements!=null){
                    for (Versement v:versements){
                        montant += Integer.parseInt(v.getMontant());
                    }

                }
            }

        }

        return montant;
    }

    public int getMontCumuleNow(long id_tontine, String statut) {
        int montant = 0;
        Tontine t = SugarRecord.findById(Tontine.class,id_tontine);
        String selon_statut="";
        if (!statut.equals(TontineEnum.IN_PROGRESS.toString())){
            selon_statut=" and statut='"+statut+"' ";
        }else{
            selon_statut=" and statut!='encaissee' ";
        }
        // identifier tontines à cumuler
        List<Tontine> tontines = Tontine.findWithQuery(Tontine.class, "Select * from Tontine where id <= "+String.valueOf(id_tontine)+selon_statut+" and periode='"+t.getPeriode()+"' and mise="+t.getMise()+" and carnet='"+t.getCarnet()+"' and id_utilisateur="+Prefs.getString(ID_UTILISATEUR_KEY,null)+" and continuer= "+t.getContinuer()+" order by id desc");
        Utilisateur u = Utilisateur.find(Utilisateur.class, "id_utilisateur = ?", Prefs.getString(ID_UTILISATEUR_KEY,null)).get(0);
        if (tontines!=null){
            for (Tontine to:tontines){
                List<Versement> versements = Select.from(Versement.class)
                        .where(Condition.prop("tontine").eq(to.getId()))
                        //.where(Condition.prop("tontine").eq(this.getMise()))
                        .where(Condition.prop("fractionne").eq("false"))
                        .where(Condition.prop("utilisateur").eq(u.getId()))
                        .list();
                if (versements!=null){
                    for (Versement v:versements){
                        montant += Integer.parseInt(v.getMontant());
                    }

                }
            }

        }

        return montant;
    }

    public List<Tontine> getIdForMontCumuleNow(long id_tontine, String statut) {
        int montant = 0;
        Tontine t = SugarRecord.findById(Tontine.class,id_tontine);
        String selon_statut="";
        if (!statut.equals(TontineEnum.IN_PROGRESS.toString())){
            selon_statut=" and statut='"+statut+"' ";
        }else{
            selon_statut=" and statut!='encaissee' ";
        }
        // identifier tontines à cumuler
        List<Tontine> tontines = Tontine.findWithQuery(Tontine.class, "Select * from Tontine where (id <= "+String.valueOf(id_tontine)+" or id > "+String.valueOf(id_tontine)+" ) "+selon_statut+" and periode='"+t.getPeriode()+"' and mise="+t.getMise()+" and carnet='"+t.getCarnet()+"' and id_utilisateur="+Prefs.getString(ID_UTILISATEUR_KEY,null)+" and continuer= "+t.getContinuer()+" order by id desc");
        return tontines;
    }

    public double getMontCommisNow(long id_tontine, String statut) {
        return (getMontCumuleNow(id_tontine, statut)*3.226)/100.00;
    }
    public double getMontEncaisseNow(long id_tontine, String statut) {
        return  (getMontCumuleNow(id_tontine,statut)-getMontCommisNow(id_tontine,statut));
    }

    public double getMontCommis() {

        return (getMontCumule(this.getStatut())*3.226)/100.00;
    }
    public double getMontEncaisse() {
        return  (getMontCumuleEnc()-getMontCommis());
    }

    public double getMontEncaisseMarchandRetry() {
        return  (getMontCumuleEncMarchandRetry()-getMontCommis());
    }

    public void setStatut(String statut) {
        this.statut = statut;
    }

    public Long getCreation() {
        return creation;
    }

    public void setCreation(Long creation) {
        this.creation = creation;
    }

    public Long getMaj() {
        return maj;
    }

    public void setMaj(Long maj) {
        this.maj = maj;
    }


    public Long getFin() {
        Utilisateur u = Utilisateur.find(Utilisateur.class, "id_utilisateur = ?", Prefs.getString(ID_UTILISATEUR_KEY,null)).get(0);

        Long l = null;
        if (this.getStatut().equals(TontineEnum.COMPLETED.toString())){
            List<Versement> list_versements = Select.from(Versement.class)
                    .where(Condition.prop("tontine").eq(this.getId()))
                    .where(Condition.prop("fractionne").eq("false"))
                    .where(Condition.prop("utilisateur").eq(u.getId()))
                    .orderBy("creation desc")
                    .list();
            if (list_versements.size()>0)
                l = list_versements.get(0).getCreation();
        }
        return l;
    }
    public String getIdSim() {
        return idSim;
    }

    public void setIdSim(String idSim) {
        this.idSim = idSim;
    }

    public int getVersements() {
        String id_utilisateur = Prefs.getString(ID_UTILISATEUR_KEY,null);
        Utilisateur u = Utilisateur.find(Utilisateur.class, "id_utilisateur = ?", Prefs.getString(ID_UTILISATEUR_KEY,null)).get(0);

        if(id_utilisateur != null) {
            int versements = 0;
            List<Versement> list_versements = Select.from(Versement.class)
                    .where(Condition.prop("tontine").eq(this.getId()))
                    .where(Condition.prop("fractionne").eq("false"))
                    .where(Condition.prop("utilisateur").eq(u.getId()))
                    .list();
            if (list_versements != null) {
                for (Versement v : list_versements) {
                    versements += Integer.parseInt(v.getMontant()) / v.getTontine().getMise();
                }
            }
            return versements;
        } else {
            return -1;
        }
    }
    public Long getPrecedent(Long id,String statut) {
        /*List<Tontine> tontine = Select.from(Tontine.class)
                .where(Condition.prop("id").eq(id-1))
                .list();*/
        List<Tontine> tontines;
        Tontine t = SugarRecord.findById(Tontine.class,id);
        //tontines = Tontine.findWithQuery(Tontine.class, "Select * from Tontine where id < "+String.valueOf(id)+" and periode='"+t.getPeriode()+"' and mise="+t.getMise()+"");
        if (Prefs.getString(CARTE_NAV_KEY,"").equals(TontineEnum.IN_PROGRESS.toString())){
            tontines = Tontine.findWithQuery(Tontine.class, "Select * from Tontine where id < "+String.valueOf(id)+" and statut!='encaissee' and periode='"+t.getPeriode()+"' and mise="+t.getMise()+" and carnet='"+t.getCarnet()+"' and id_utilisateur="+Prefs.getString(ID_UTILISATEUR_KEY,null)+" and continuer= "+t.getContinuer()+" order by id desc");
        }else{
            tontines = Tontine.findWithQuery(Tontine.class, "Select * from Tontine where id < "+String.valueOf(id)+" and statut='"+statut+"' and periode='"+t.getPeriode()+"' and mise="+t.getMise()+" and carnet='"+t.getCarnet()+"' and id_utilisateur="+Prefs.getString(ID_UTILISATEUR_KEY,null)+" and continuer= "+t.getContinuer()+" order by id desc");
        }
        //List<Tontine> tontines = Tontine.findWithQuery(Tontine.class, "Select * from Tontine where id < "+String.valueOf(id)+" and statut='"+statut+"'");
        if(tontines.size()>0){
            return tontines.get(0).getId();
        }
        return id;

    }

    public void terminer(Context context) {
        setStatut(TontineEnum.COMPLETED.toString());
        // maj des dates
        Date currentTime = Calendar.getInstance().getTime();
        long output_maj=currentTime.getTime()/1000L;
        String str_maj=Long.toString(output_maj);
        long timestamp_maj = Long.parseLong(str_maj) * 1000;
        setMaj(timestamp_maj);
        save();


        Gson gson = new Gson();
        Utilisateur u = Utilisateur.find(Utilisateur.class, "id_utilisateur = ?", Prefs.getString(ID_UTILISATEUR_KEY,null)).get(0);
        double solde = u.getSolde();


        try {

            //
            Synchronisation new_sync2 = new Synchronisation();
            JSONObject jsonObject2 = new JSONObject();
            jsonObject2.put("a","update#tontines");
            jsonObject2.put("n",u.getNumero());
            jsonObject2.put("s", solde);
            String t_json = gson.toJson(this);

            jsonObject2.put("d",t_json);
            new_sync2.setMaj(timestamp_maj);
            new_sync2.setStatut(0);
            new_sync2.setDonnees(jsonObject2.toString());
            new_sync2.save();

        } catch (JSONException e) {
            e.printStackTrace();
        }

        Utilitaire utilitaire = new Utilitaire(context);
        // si internet, appeler synchroniser_en_ligne
        if (utilitaire.isConnected()){
            utilitaire.synchroniser_en_ligne();
        }
    }

    public Long getSuivant(Long id,String statut) {
        List<Tontine> tontines;
        Tontine t = SugarRecord.findById(Tontine.class,id);
        //tontines= Tontine.findWithQuery(Tontine.class, "Select * from Tontine where id > "+String.valueOf(id)+" and periode='"+t.getPeriode()+"' and mise="+t.getMise()+"");
        if (Prefs.getString(CARTE_NAV_KEY,"").equals(TontineEnum.IN_PROGRESS.toString())){
            tontines= Tontine.findWithQuery(Tontine.class, "Select * from Tontine where id > "+String.valueOf(id)+" and statut!='encaissee' and periode='"+t.getPeriode()+"' and mise="+t.getMise()+" and continuer="+t.getContinuer()+" and carnet='"+t.getCarnet()+"' and id_utilisateur="+Prefs.getString(ID_UTILISATEUR_KEY,null)+"");
        }else{
            tontines =Tontine.findWithQuery(Tontine.class, "Select * from Tontine where id > "+String.valueOf(id)+" and statut='"+statut+"' and periode='"+t.getPeriode()+"' and mise="+t.getMise()+" and continuer="+t.getContinuer()+" and carnet='"+t.getCarnet()+"' and id_utilisateur="+Prefs.getString(ID_UTILISATEUR_KEY,null)+"");
        }
        //List<Tontine> tontines = Tontine.findWithQuery(Tontine.class, "Select * from Tontine where id > "+String.valueOf(id)+" and statut='"+statut+"'");
        /*List<Tontine> tontine = Select.from(Tontine.class)
                .where(Condition.prop("id").eq(id+1))
                .list();*/
        if(tontines.size()>0) {
            return tontines.get(0).getId();
        }
        return id;
    }

    public double getNumeroCarnet(Long id) {
        List<Tontine> tontines;
        Tontine t = SugarRecord.findById(Tontine.class,id);

        /*tontines = Tontine.findWithQuery(Tontine.class, "Select * from Tontine where id < "+String.valueOf(id)+
                " and id_utilisateur="+Prefs.getString(ID_UTILISATEUR_KEY,null)+" and continuer="+t.getContinuer());*/
        tontines = Tontine.findWithQuery(Tontine.class, "Select * from Tontine where  id_utilisateur="+Prefs.getString(ID_UTILISATEUR_KEY,null)+" and mise="+t.getMise()+" and periode='"+t.getPeriode()+"'");
        // déterminer le montant total à cotiser
        double montant_cotiser=0.00;
        // déterminer le montant cotiser
        double montant_a_cotiser=0.00;
        // numero de la tontine
        int no_tontine =0;
        for (Tontine to:tontines) {
            montant_cotiser += to.getMontant();
            montant_a_cotiser += to.getMontantACotiser();
            if (to.getId().equals(id)){
                no_tontine= tontines.indexOf(to)+1;
            }
        }
        Log.d("Carnet_Mise", "no_tontine:"+no_tontine+"//id:"+id+"//taille:"+tontines.size());//size:"+tontines.size()+"//cotiser"+montant_cotiser+"//a_cotiser"+montant_a_cotiser);
        //
        if (no_tontine!=0){
            return Math.ceil((no_tontine/12.00));
        }
        //Log.d("Carnet_Mise", "fb:"+montant_a_cotiser);//size:"+tontines.size()+"//cotiser"+montant_cotiser+"//a_cotiser"+montant_a_cotiser);
        //Log.d("Carnet_Montant_cotiser", "fb:"+montant_cotiser);//size:"+tontines.size()+"//cotiser"+montant_cotiser+"//a_cotiser"+montant_a_cotiser);
        //Log.d("Carnet", "fb:"+tontines.size());//size:"+tontines.size()+"//cotiser"+montant_cotiser+"//a_cotiser"+montant_a_cotiser);
        // numeroCarnet = (12*montant_cotiser)/montant_à_cotiser
        /*if ((int) ((12*montant_cotiser)/montant_a_cotiser)<1){
            return  1;
        }else{
            return (int) ((12*montant_cotiser)/montant_a_cotiser);
        }*/

        return -1;
    }

    public int getPositionOfNewCarte()
    {
        List<Tontine> tontines;
        tontines = Tontine.findWithQuery(Tontine.class, "Select * from Tontine where  id_utilisateur="+Prefs.getString(ID_UTILISATEUR_KEY,null)+" and mise="+this.getMise()+" and periode='"+this.getPeriode()+"'"+" and carnet='"+this.getCarnet()+"'");
        return tontines.size();
    }

    public int getPositionCarteNew(Long id) {
        List<Tontine> tontines;
        Tontine t = SugarRecord.findById(Tontine.class,id);
        tontines = Tontine.findWithQuery(Tontine.class, "Select * from Tontine where  id_utilisateur="+Prefs.getString(ID_UTILISATEUR_KEY,null)+" and mise="+t.getMise()+" and periode='"+t.getPeriode()+"'"+" and carnet='"+t.getCarnet()+"'");
        // déterminer le montant total à cotiser
        double montant_cotiser=0.00;
        // déterminer le montant cotiser
        double montant_a_cotiser=0.00;
        // numero de la tontine
        int no_tontine =0;
        for (Tontine to:tontines) {
            montant_cotiser += to.getMontant();
            montant_a_cotiser += to.getMontantACotiser();
            if (to.getId().equals(id)){
                no_tontine= tontines.indexOf(to);
            }
        }
        Log.d("Carnet_Mise", "no_tontine:"+no_tontine+"//id:"+id+"//taille:"+tontines.size());//size:"+tontines.size()+"//cotiser"+montant_cotiser+"//a_cotiser"+montant_a_cotiser);
        //
//        return tontines.size();
        return  no_tontine+1;
        //Log.d("Carnet_Mise", "fb:"+montant_a_cotiser);//size:"+tontines.size()+"//cotiser"+montant_cotiser+"//a_cotiser"+montant_a_cotiser);
        //Log.d("Carnet_Montant_cotiser", "fb:"+montant_cotiser);//size:"+tontines.size()+"//cotiser"+montant_cotiser+"//a_cotiser"+montant_a_cotiser);
        //Log.d("Carnet", "fb:"+tontines.size());//size:"+tontines.size()+"//cotiser"+montant_cotiser+"//a_cotiser"+montant_a_cotiser);
        // numeroCarnet = (12*montant_cotiser)/montant_à_cotiser
        /*if ((int) ((12*montant_cotiser)/montant_a_cotiser)<1){
            return  1;
        }else{
            return (int) ((12*montant_cotiser)/montant_a_cotiser);
        }*/

    }

    public int getPositionCarte(Long id) {
        List<Tontine> tontines;
        Tontine t = SugarRecord.findById(Tontine.class,id);
        tontines = Tontine.findWithQuery(Tontine.class, "Select * from Tontine where  id_utilisateur="+Prefs.getString(ID_UTILISATEUR_KEY,null)+" and mise="+t.getMise()+" and periode='"+t.getPeriode()+"'"+" and carnet='"+t.getCarnet()+"'");
        // déterminer le montant total à cotiser
        double montant_cotiser=0.00;
        // déterminer le montant cotiser
        double montant_a_cotiser=0.00;
        // numero de la tontine
        int no_tontine =0;
        for (Tontine to:tontines) {
            montant_cotiser += to.getMontant();
            montant_a_cotiser += to.getMontantACotiser();
            if (to.getId().equals(id)){
                no_tontine= tontines.indexOf(to);
            }
        }
        Log.d("Carnet_Mise", "no_tontine:"+no_tontine+"//id:"+id+"//taille:"+tontines.size());//size:"+tontines.size()+"//cotiser"+montant_cotiser+"//a_cotiser"+montant_a_cotiser);
        //
        return  no_tontine;
        //Log.d("Carnet_Mise", "fb:"+montant_a_cotiser);//size:"+tontines.size()+"//cotiser"+montant_cotiser+"//a_cotiser"+montant_a_cotiser);
        //Log.d("Carnet_Montant_cotiser", "fb:"+montant_cotiser);//size:"+tontines.size()+"//cotiser"+montant_cotiser+"//a_cotiser"+montant_a_cotiser);
        //Log.d("Carnet", "fb:"+tontines.size());//size:"+tontines.size()+"//cotiser"+montant_cotiser+"//a_cotiser"+montant_a_cotiser);
        // numeroCarnet = (12*montant_cotiser)/montant_à_cotiser
        /*if ((int) ((12*montant_cotiser)/montant_a_cotiser)<1){
            return  1;
        }else{
            return (int) ((12*montant_cotiser)/montant_a_cotiser);
        }*/

    }

    public int getNumero(Long id, String statut) {
        List<Tontine> tontines;
        Tontine t = SugarRecord.findById(Tontine.class,id);
        if (statut.equals(TontineEnum.IN_PROGRESS.toString())){
            tontines= Tontine.findWithQuery(Tontine.class, "Select * from Tontine where statut!='encaissee' and periode='"+t.getPeriode()+"' and mise="+t.getMise()+" and carnet='"+t.getCarnet()+"' and continuer="+t.getContinuer()+" and id_utilisateur="+Prefs.getString(ID_UTILISATEUR_KEY,null)+"");
        }else{
            tontines= Tontine.findWithQuery(Tontine.class, "Select * from Tontine where statut='"+statut+"' and periode='"+t.getPeriode()+"' and mise="+t.getMise()+" and carnet='"+t.getCarnet()+"' and continuer="+t.getContinuer()+" and id_utilisateur="+Prefs.getString(ID_UTILISATEUR_KEY,null)+"");
        }
        for (Tontine to:tontines) {
            if (to.getId().equals(t.getId())){
                return tontines.indexOf(to)+1;
            }
        }

        return -1;
    }

    public int getNumero_old(Long id, String statut) {
        List<Tontine> tontines;
        Tontine t = SugarRecord.findById(Tontine.class,id);
        if (statut.equals(TontineEnum.IN_PROGRESS.toString())){
            tontines= Tontine.findWithQuery(Tontine.class, "Select * from Tontine where statut!='encaissee' and periode='"+t.getPeriode()+"' and mise="+t.getMise()+" and carnet='"+t.getCarnet()+"' and continuer="+t.getContinuer()+" and id_utilisateur="+Prefs.getString(ID_UTILISATEUR_KEY,null)+"");
        }else{
            tontines= Tontine.findWithQuery(Tontine.class, "Select * from Tontine where statut='"+statut+"' and periode='"+t.getPeriode()+"' and mise="+t.getMise()+" and carnet='"+t.getCarnet()+"' and continuer="+t.getContinuer()+" and id_utilisateur="+Prefs.getString(ID_UTILISATEUR_KEY,null)+"");
        }
        for (Tontine to:tontines) {
            if (to.getId().equals(t.getId())){
                return tontines.indexOf(to)+1;
            }
        }

        return -1;
    }

    public int getNumero_plus(Long id, String statut) {
        List<Tontine> tontines;
        Tontine t = SugarRecord.findById(Tontine.class,id);
        if (statut.equals(TontineEnum.IN_PROGRESS.toString())){
            tontines= Tontine.findWithQuery(Tontine.class, "Select * from Tontine where statut!='encaissee' and periode='"+t.getPeriode()+"' and mise="+t.getMise()+" and carnet='"+t.getCarnet()+"'  and id_utilisateur="+Prefs.getString(ID_UTILISATEUR_KEY,null)+"");
        }else{
            tontines= Tontine.findWithQuery(Tontine.class, "Select * from Tontine where statut='"+statut+"' and periode='"+t.getPeriode()+"' and mise="+t.getMise()+" and carnet='"+t.getCarnet()+"' and id_utilisateur="+Prefs.getString(ID_UTILISATEUR_KEY,null)+"");
        }
        for (Tontine to:tontines) {
            if (to.getId().equals(t.getId())){
                return tontines.indexOf(to)+1;
            }
        }

        return -1;
    }
    public int getNumeroTotal(Long id, String statut) {
        List<Tontine> tontines;
        Tontine t = SugarRecord.findById(Tontine.class,id);
        if (statut.equals(TontineEnum.IN_PROGRESS.toString())){
            tontines= Tontine.findWithQuery(Tontine.class, "Select * from Tontine where statut!='encaissee' and periode='"+t.getPeriode()+"' and mise="+t.getMise()+" and carnet='"+t.getCarnet()+"' and continuer="+t.getContinuer()+" and id_utilisateur="+Prefs.getString(ID_UTILISATEUR_KEY,null)+"");
        }else{
            tontines= Tontine.findWithQuery(Tontine.class, "Select * from Tontine where statut='"+statut+"' and periode='"+t.getPeriode()+"' and mise="+t.getMise()+" and carnet='"+t.getCarnet()+"' and continuer="+t.getContinuer()+" and id_utilisateur="+Prefs.getString(ID_UTILISATEUR_KEY,null)+"");
        }
        return tontines.size();

    }

    public Long getContinuer() {
        return continuer;
    }

    public void setContinuer(Long continuer) {
        this.continuer = continuer;
    }

    public String getCarnet() {
        return carnet;
    }

    public void setCarnet(String carnet) {
        this.carnet = carnet;
    }

    public Tontine getByIdServer(int idServer){
        List result = SugarRecord.find(Tontine.class, "id_server = ?", String.valueOf(idServer));
        return result.size() > 0 ? (Tontine) result.get(0) : null;
    }
}