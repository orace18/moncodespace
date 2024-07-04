package com.sicmagroup.tondi;

import com.orm.SugarRecord;

public class Synchronisation extends SugarRecord<Synchronisation> {

    private Long id;
    private String donnees;
    private int statut;

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public void setId(Long id) {
        this.id = id;
    }

    public String getDonnees() {
        return donnees;
    }

    public void setDonnees(String donnees) {
        this.donnees = donnees;
    }

    public int getStatut() {
        return statut;
    }

    public void setStatut(int statut) {
        this.statut = statut;
    }

    public Long getMaj() {
        return maj;
    }

    public void setMaj(Long maj) {
        this.maj = maj;
    }

    private Long maj;

}