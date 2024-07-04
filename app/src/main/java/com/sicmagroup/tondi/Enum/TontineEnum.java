package com.sicmagroup.tondi.Enum;

public enum TontineEnum {
    IN_PROGRESS(0, "IN_PROGRESS"), // en cours
    COMPLETED(1, "COMPLETED"), // terminée
    WAITING(2, "WAITING"), // en attente
    COLLECTED(3, "COLLECTED"); // encaissée
//    BLOQUEE(4, "bloquee");

    private String stringValue;
    private int intValue;

    TontineEnum(int i, String s) {
        stringValue = s;
        intValue = i;
    }

    @Override
    public String toString() {
        return stringValue;
    }
}
