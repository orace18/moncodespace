package com.sicmagroup.tondi.Enum;

public enum PeriodiciteEnum {
    JOURNALIERE("JOURNALIERE", 0),
    HEBDOMADAIRE("HEBDOMADAIRE", 1),
    MENSUELLE("MENSUELLE", 2);

    private String value;
    private int position;
    PeriodiciteEnum(String v, int i) {
        value = v;
        position = i;
    }

    @Override
    public String toString() {
        return value;
    }
}
