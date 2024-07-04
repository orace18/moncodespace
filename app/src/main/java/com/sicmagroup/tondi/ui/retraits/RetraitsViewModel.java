package com.sicmagroup.tondi.ui.retraits;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class RetraitsViewModel extends ViewModel {

    private final MutableLiveData<String> mText;

    public RetraitsViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("This is dashboard fragment");
    }

    public LiveData<String> getText() {
        return mText;
    }
}