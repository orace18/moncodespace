package com.sicmagroup.formmaster.model;

import java.util.ArrayList;
import java.util.List;

public class FormElementPickerSexe extends BaseFormElement {
    private String pickerTitle; // custom title for picker
    private List<String> mOptions; // list of options for single and multi picker
    private List<String> mOptionsSelected; // list of selected options for single and multi picker

    public FormElementPickerSexe() {
    }

    public static FormElementPickerSexe createInstance() {
        FormElementPickerSexe formElementPickerSexe = new FormElementPickerSexe();
        formElementPickerSexe.setType(BaseFormElement.TYPE_PICKER_SEXE);
        formElementPickerSexe.setPickerTitle("Choose your sexe");
        return formElementPickerSexe;
    }

    public FormElementPickerSexe setTag(int mTag) {
        return (FormElementPickerSexe)  super.setTag(mTag);
    }

    public FormElementPickerSexe setType(int mType) {
        return (FormElementPickerSexe)  super.setType(mType);
    }

    public FormElementPickerSexe setTitle(String mTitle) {
        return (FormElementPickerSexe)  super.setTitle(mTitle);
    }

    public FormElementPickerSexe setValue(String mValue) {
        return (FormElementPickerSexe)  super.setValue(mValue);
    }

    public FormElementPickerSexe setHint(String mHint) {
        return (FormElementPickerSexe)  super.setHint(mHint);
    }

    public FormElementPickerSexe setRequired(boolean required) {
        return (FormElementPickerSexe)  super.setRequired(required);
    }

    // custom setters
    public FormElementPickerSexe setOptions(List<String> mOptions) {
        this.mOptions = mOptions;
        return this;
    }

    public FormElementPickerSexe setOptionsSelected(List<String> mOptionsSelected) {
        this.mOptionsSelected = mOptionsSelected;
        return this;
    }

    public FormElementPickerSexe setPickerTitle(String title) {
        this.pickerTitle = title;
        return this;
    }

    // custom getters
    public List<String> getOptions() {
        return (this.mOptions == null) ? new ArrayList<String>() : this.mOptions;
    }

    public List<String> getOptionsSelected() {
        return (this.mOptionsSelected == null) ? new ArrayList<String>() : this.mOptionsSelected;
    }

    public String getPickerTitle() {
        return this.pickerTitle;
    }

}
