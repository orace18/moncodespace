package com.sicmagroup.tondi.Enum;

public enum UserStatut {
    ACTIVE(0, "ACTIVE"),
    DESACTIVE(1, "DESACTIVE"),
    DESACTIVE_TEMP(2, "DESACTIVE_TEMP");

    private int posititon;
    private String value;

    UserStatut(int i, String value){
        this.posititon = i;
        this.value = value;
    }


    @Override
    public String toString() {
        // TODO Auto-generated method stub
        return this.value;
    }
}
