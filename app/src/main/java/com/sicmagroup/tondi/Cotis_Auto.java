package com.sicmagroup.tondi;

import com.orm.SugarRecord;

public class Cotis_Auto extends SugarRecord<Cotis_Auto> {

    private Long creation;
    private Long maj;
    private  Tontine tontine;
    private  Utilisateur utilisateur;

    public Tontine getTontine() {
        return tontine;
    }

    public void setTontine(Tontine tontine) {
        this.tontine = tontine;
    }



    public Cotis_Auto() {

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

    void setMaj(Long maj) {
        this.maj = maj;
    }



    public Utilisateur getUtilisateur() {
        return utilisateur;
    }

    public void setUtilisateur(Utilisateur utilisateur) {
        this.utilisateur = utilisateur;
    }
}