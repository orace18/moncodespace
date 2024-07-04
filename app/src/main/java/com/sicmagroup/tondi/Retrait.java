package com.sicmagroup.tondi;

import com.orm.SugarRecord;

import java.util.Calendar;

public class Retrait extends SugarRecord<Retrait> {


    public Tontine getTontine() {
        return tontine;
    }

    public void setTontine(Tontine tontine) {
        this.tontine = tontine;
    }

    public Long getCreation() {
        return creation;
    }

    public void setCreation(Long creation) {
        this.creation = creation;
    }

    public String getStatut() {
        return statut;
    }

    public void setStatut(String statut) {
        this.statut = statut;
    }

    private Tontine tontine;

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    private Utilisateur utilisateur;
    private String token;
    private String statut;

    public String getMontant() {
        return montant;
    }

    public void setMontant(String montant) {
        this.montant = montant;
    }

    public String getIdMarchand() {
        return idMarchand;
    }

    public void setIdMarchand(String idMarchand) {
        this.idMarchand = idMarchand;
    }

    private String montant;
    private String idMarchand="0";
    private Long creation;
    private Long maj;


    public Long getMaj() {
        return maj;
    }

    public void setMaj(Long maj) {
        this.maj = maj;
    }


    public Utilisateur getUtilisateur() {
        return utilisateur;
    }

    public void setUtilisateur(Utilisateur utilisateur) {
        this.utilisateur = utilisateur;
    }

    public int getExpire() {
        Long diff = Calendar.getInstance().getTime().getTime()-this.getCreation();
        int remaining_seconds = (int) (86400000 - diff);
        return remaining_seconds;
    }
}