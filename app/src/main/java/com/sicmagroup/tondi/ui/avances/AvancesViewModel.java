package com.sicmagroup.tondi.ui.avances;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class AvancesViewModel extends ViewModel {

    private final MutableLiveData<String> mText;

    public AvancesViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("This is dashboard fragment");
    }

    public LiveData<String> getText() {
        return mText;
    }
}