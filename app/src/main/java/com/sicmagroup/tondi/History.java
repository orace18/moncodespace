package com.sicmagroup.tondi;

import com.orm.SugarRecord;

public class History extends SugarRecord<History> {
    private String titre;
    private  String contenu;
    private Long creation;
    private int etat;
    private int numero;
    private Utilisateur user;
    private String idServer;

    public String getTitre() {
        return titre;
    }
    public String getContenu(){ return contenu; }
    public Long getCreation(){ return creation; }
    public int getEtat() { return etat; }
    public int getNumero(){return numero;}
    public void setNumero(int numero){this.numero = numero;}

    public void setTitre(String titre) {
        this.titre = titre;
    }
    public void setContenu(String contenu) { this.contenu = contenu; }
    public void setCreation(Long creation) { this.creation = creation; }
    public void setEtat(int etat) { this.etat = etat; }
    public String getId_server()
    {
        return idServer;
    }

    public void setId_server(String id_server)
    {
        this.idServer = id_server;
    }

    public Utilisateur getUser() {
        return user;
    }

    public void setUser(Utilisateur user) {
        this.user = user;
    }
}
