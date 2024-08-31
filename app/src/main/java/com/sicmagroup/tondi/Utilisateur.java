package com.sicmagroup.tondi;

import com.orm.SugarRecord;
import com.orm.query.Condition;
import com.orm.query.Select;
import com.pixplicity.easyprefs.library.Prefs;
import com.sicmagroup.tondi.Enum.TontineEnum;


import java.util.List;

import static com.sicmagroup.tondi.Connexion.ID_UTILISATEUR_KEY;


public class Utilisateur  extends SugarRecord<Utilisateur> {

    private String idUtilisateur;
    private String nom;
    private String prenoms;
    private String numero;
    private String photoIdentite;
    private String mdp;
    private String statut;
    private String pinAcces;
    private Long connecterLe;
    private Long creation;
    private Long maj;
    //token firebase
    private String firebaseToken;
    private String cniPhoto;
    private String numeroCompte;
    private String codeMarchand;
    private String token;
    private String uuid;

    public String getToken() {
        return token;
    }
    public void setToken(String token) {
        this.token = token;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getNumero_compte() {
        return numeroCompte;
    }

    public void setNumero_compte(String numero_compte) {
        this.numeroCompte = numero_compte;
    }

    public Utilisateur() {

    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getPrenoms() {
        return prenoms;
    }

    public void setPrenoms(String prenoms) {
        this.prenoms = prenoms;
    }

    public String getPhoto_identite() {
        return photoIdentite;
    }

    public void setPhoto_identite(String photo_identite) {
        this.photoIdentite = photo_identite;
    }

    public String getcni_photo() {
        return cniPhoto;
    }

    public void setcni_photo(String photo_cni) {
        this.cniPhoto = photo_cni;
    }

    public String getMdp() {
        return mdp;
    }

    public void setMdp(String mdp) {
        this.mdp = mdp;
    }

    public String getStatut() {
        return statut;
    }

    public void setStatut(String statut) {
        this.statut = statut;
    }

    public String getPin_acces() {
        return pinAcces;
    }

    public void setPin_acces(String pin_acces) {
        this.pinAcces = pin_acces;
    }

    public Long getConnecter_le() {
        return connecterLe;
    }

    public void setConnecter_le(Long connecter_le) {
        this.connecterLe = connecter_le;
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

    public String getFirebaseToken()
    {
        return this.firebaseToken;
    }
    public void setFirebaseToken(String token)
    {
        this.firebaseToken = token;
    }


    public String getNumero() {
        return numero;
    }

    public void setNumero(String numero) {
        this.numero = numero;
    }

    public int auth(String num, String mdp) {
        List<Utilisateur> user = Select.from(Utilisateur.class)
                .where(Condition.prop("numero").eq(num),
                        Condition.prop("mdp").eq(mdp))
                .list();
        return  user.size();
    }

    public Utilisateur getUser(String num) {
        List<Utilisateur> user = Select.from(Utilisateur.class)
                .where(Condition.prop("numero").eq(num))
                .list();
        return  user.get(0);
    }
    public List<Utilisateur> getAll() {
        List<Utilisateur> user = Select.from(Utilisateur.class)
                .list();
        return  user;
    }


    public String getId_utilisateur() {
        return idUtilisateur;
    }

    public void setId_utilisateur(String id_utilisateur) {
        this.idUtilisateur = id_utilisateur;
    }

    //getUser solde
    public double getSolde_hold()
    {
        double solde = 0.00;
        //Obtention du solde
        List<Tontine>  list_tontines =Select.from(Tontine.class)
                .where(Condition.prop("id_utilisateur").eq(Long.valueOf(Prefs.getString(ID_UTILISATEUR_KEY,""))))
                .where(Condition.prop("statut").eq(TontineEnum.IN_PROGRESS.toString()))
                .whereOr(Condition.prop("statut").eq(TontineEnum.COMPLETED.toString()))
                .whereOr(Condition.prop("statut").eq(TontineEnum.WAITING.toString()))
                .list();
        if(list_tontines.size()>0){
            for (Tontine tontine:list_tontines){
                solde=solde+tontine.getMontant();
            }
        }

        return solde;
    }

    public double getSolde()
    {
        double solde = 0.00;
        //Obtention du solde
        List<Tontine>  list_tontines =Select.from(Tontine.class)
                .where(Condition.prop("id_utilisateur").eq(Long.valueOf(Prefs.getString(ID_UTILISATEUR_KEY,""))))
                .where(Condition.prop("statut").eq(TontineEnum.IN_PROGRESS.toString()))
                .whereOr(Condition.prop("statut").eq(TontineEnum.COMPLETED.toString()))
                .whereOr(Condition.prop("statut").eq(TontineEnum.WAITING.toString()))
                .list();
        if(list_tontines.size()>0){
            for (Tontine tontine:list_tontines){
                solde=solde+(tontine.getMontant() - (tontine.getMontant()*3.226) / 100);
            }
        }

        return solde;
    }


    public String getCodeMarchand() {
        return codeMarchand;
    }

    public void setCodeMarchand(String codeMarchand) {
        this.codeMarchand = codeMarchand;
    }
}