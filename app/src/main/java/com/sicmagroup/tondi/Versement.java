package com.sicmagroup.tondi;

import com.orm.SugarRecord;

public class Versement extends SugarRecord<Versement> implements Comparable {
    private Tontine tontine;
    private Boolean statutPaiement;
    private String fractionne;
    private String idVersement;
    private String idVersServ;
    private  Utilisateur utilisateur;

    public String getMontant() {
        return montant;
    }

    public void setMontant(String montant) {
        this.montant = montant;
    }


    private String montant;
    private Long creation;
    private Long maj;

    public void setStatut_paiement(Boolean statut_paiement) {
        this.statutPaiement = statut_paiement;
    }

    public void setUssd(int ussd) {
        this.ussd = ussd;
    }

    int ussd;

    public Versement() {
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



    public Boolean getStatut_paiement() {
        return statutPaiement;
    }

    public int getUssd() {
        return  ussd;
    }

    public Tontine getTontine() {
        return tontine;
    }

    public void setTontine(Tontine tontine) {
        this.tontine = tontine;
    }

    public String getFractionne() {
        return fractionne;
    }

    public void setFractionne(String fractionne) {
        this.fractionne = fractionne;
    }

    public String getIdVersement() {
        return idVersement;
    }

    public void setIdVersement(String idVersement) {
        this.idVersement = idVersement;
    }

    public Utilisateur getUtilisateur() {
        return utilisateur;
    }

    public void setUtilisateur(Utilisateur utilisateur) {
        this.utilisateur = utilisateur;
    }

    public String getIdVersServ() {
        return idVersServ;
    }

    public void setIdVersServ(String idVersServ) {
        this.idVersServ = idVersServ;
    }

    @Override
    public int compareTo(Object v) {
        return (int) (this.getCreation() - ((Versement) v).getCreation());
//        return (int) (((Plainte) p).getCreation() - this.getCreation());
    }

}