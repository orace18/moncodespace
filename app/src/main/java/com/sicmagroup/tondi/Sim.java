package com.sicmagroup.tondi;

import com.orm.SugarRecord;

public class Sim extends SugarRecord<Sim> {

    private String reseau;
    private String numero;
    private Long creation;
    private Long maj;
    private  Utilisateur utilisateur;
    private String idSimServer;

    public Sim() {

    }

    public String getId_sim_server()
    {
        return this.idSimServer;
    }

    public void setId_sim_server(String idserver)
    {
        this.idSimServer = idserver;
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


    public String getNumero() {
        return numero;
    }

    public void setNumero(String numero) {
        this.numero = numero;
        //this.reseau= OperatorName(numero);
    }

    /*public String OperatorName(String numero) {
        String operator_name = "";
        String[] mtn_prefix_list =  getResources().getStringArray(R.array.mtn_prefix_list);
        String[] moov_prefix_list = getResources().getStringArray(R.array.moov_prefix_list);
        if (Arrays.asList(mtn_prefix_list).contains(""+numero.charAt(0)+numero.charAt(1))) {
            operator_name = "MTN";
        }
        if (Arrays.asList(moov_prefix_list).contains(""+numero.charAt(0)+numero.charAt(1))) {
            operator_name = "MOOV";
        }
        return operator_name ;
    }*/


    public String getReseau() {
        return reseau;
    }

    public void setReseau(String reseau) {
        this.reseau = reseau;
    }

    public Utilisateur getUtilisateur() {
        return utilisateur;
    }

    public void setUtilisateur(Utilisateur utilisateur) {
        this.utilisateur = utilisateur;
    }
}