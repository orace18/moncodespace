package com.sicmagroup.tondi.Enum;

public enum OperationTypeEnum {
    INSCRIPTION(0, "is_insc"),
    ACCES(1, "is_access"),
    WITHDRAW_FROM_MERCHANT(2, "is_withdraw_from_merchant"),
    WITHDRAW_FROM_CUSTOMER(3, "is_withdraw_from_customer");

    private int position;
    private String value;

    OperationTypeEnum(int i, String value){
        this.position = i;
        this.value = value;
    }

    @Override
    public String toString() {
        return this.value;
    }
}
