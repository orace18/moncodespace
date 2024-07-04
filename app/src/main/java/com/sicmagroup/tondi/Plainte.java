package com.sicmagroup.tondi;

import com.orm.SugarRecord;

public class Plainte extends SugarRecord<Plainte> implements Comparable {

    private String fileName;
    private boolean sended;
    private long duration;
    private String idUtilisateur;
    private Long creation;

    public Plainte() {
    }

    public String getFile_name() {
        return fileName;
    }

    public void setFile_name(String file_name) {
        this.fileName = file_name;
    }

    public boolean getSended() {
        return sended;
    }

    public void setSended(boolean sended) {
        this.sended = sended;
    }

    public Long getDuration() {
        return duration;
    }

    public void setDuration(Long duration) {
        this.duration = duration;
    }

    public String getAuteur() {
        return idUtilisateur;
    }

    public void setAuteur(String auteur) {
        this.idUtilisateur = auteur;
    }

    public Long getCreation() {
        return creation;
    }

    public void setCreation(Long creation) {
        this.creation = creation;
    }

//    public ArrayList<Plainte> sortByDate(ArrayList<Plainte> plainteArrayList)
//    {
//        Plainte p, q = null;
//
//        for (int i = 0; i < plainteArrayList.size(); i++) {
//            p = plainteArrayList.get(i);
//            q = if plainteArrayList.
//        }
//    }

    @Override
    public int compareTo(Object p) {
        return (int) (this.getCreation() - ((Plainte) p).getCreation());
//        return (int) (((Plainte) p).getCreation() - this.getCreation());
    }

}
