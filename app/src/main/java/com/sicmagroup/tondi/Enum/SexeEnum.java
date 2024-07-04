package com.sicmagroup.tondi.Enum;

public enum SexeEnum {
    MALE(0, "MALE"),
    FEMALE(1, "FEMALE"),
    OTHER(2, "OTHER");

    private int i;
    private String value;

    SexeEnum(int i, String value){
        this.i = i;
        this.value = value;
    }

    @Override
    public String toString() {
        return this.value;
    }
}
