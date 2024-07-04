package com.sicmagroup.tondi.Enum;

public enum RetraitEnum {
    IN_PROGRESS(0, "En cours"),
    VALIDATE(1, "valide"),
    UNVALIDATE(2, "non valide");

    private int position;
    private String value;

    RetraitEnum(int position, String value){
        this.position = position;
        this.value = value;
    }

    @Override
    public String toString() {
        // TODO Auto-generated method stub
        return this.value;
    }
}
