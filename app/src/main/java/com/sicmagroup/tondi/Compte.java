package com.sicmagroup.tondi;

public class Compte {
    private int id;
    private int id_tontine;
    private String periode;
    private int prelevement_auto;
    private int mise;
    private String statut;

    public String getMontant() {
        return montant;
    }

    public void setMontant(String montant) {
        this.montant = montant;
    }


    private String montant;
    private String creation;
    private String maj;

    public Compte() {
    }

    public String getPeriode() {
        return periode;
    }

    public void setPeriode(String periode) {
        this.periode = periode;
    }

    public int getId_tontine() {
        return id_tontine;
    }

    public void setId_tontine(int id_tontine) {
        this.id = id_tontine;
    }


    public int getMise() {
        return mise;
    }

    public void setMise(int mise) {
        this.mise = mise;
    }


    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getPrelevement_auto() {
        return prelevement_auto;
    }

    public void setPrelevement_auto(int prelevement_auto) {
        this.prelevement_auto = prelevement_auto;
    }

    public String getStatut() {
        return statut;
    }

    public void setStatut(String statut) {
        this.statut = statut;
    }

    public String getCreation() {
        return creation;
    }

    public void setCreation(String creation) {
        this.creation = creation;
    }

    public String getMaj() {
        return maj;
    }

    public void setMaj(String maj) {
        this.maj = maj;
    }




}