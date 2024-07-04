package com.sicmagroup.formmaster.model;

/**
 * Created by Riddhi - Rudra on 28-Jul-17.
 */

public class FormElementZero extends BaseFormElement {
    String mValue;
    public FormElementZero() {
    }

    public static FormElementZero createInstance() {
        FormElementZero FormElementZero = new FormElementZero();
        FormElementZero.setType(BaseFormElement.TYPE_ZERO);
        return FormElementZero;
    }

    public FormElementZero setTag(int mTag) {
        return (FormElementZero)  super.setTag(mTag);
    }

    public FormElementZero setType(int mType) {
        return (FormElementZero)  super.setType(mType);
    }

    public FormElementZero setTitle(String mTitle) {
        return (FormElementZero)  super.setTitle(mTitle);
    }

    public FormElementZero setValue(String mValue) {
        this.mValue=mValue;
        return (FormElementZero)  super.setValue(mValue);
    }


    public FormElementZero setHint(String mHint) {
        return (FormElementZero)  super.setHint(mHint);
    }

    public FormElementZero setRequired(boolean required) {
        return (FormElementZero)  super.setRequired(required);
    }
    
}
