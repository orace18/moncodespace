package com.sicmagroup.tondi.ui.plaintes;

import static com.sicmagroup.tondi.Connexion.ID_UTILISATEUR_KEY;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.orm.query.Condition;
import com.orm.query.Select;
import com.pixplicity.easyprefs.library.Prefs;
import com.sicmagroup.tondi.Plainte;

import java.util.List;

public class PlaintesViewModel extends ViewModel {

    private final MutableLiveData<String> mText;
    private  final  MutableLiveData <List <Plainte> > mPlaintes;

    public PlaintesViewModel() {
        this.mPlaintes =  new MutableLiveData<>();
        mText = new MutableLiveData<>();
        mText.setValue("This is plaintes fragment");
    }

    public LiveData<String> getText() {
        return mText;
    }

    public  LiveData <List<Plainte>> getPlaintes(){

        mPlaintes.setValue( Plainte.listAll(Plainte.class));

//        Log.e("plaintes", Prefs.getString(ID_UTILISATEUR_KEY,null));

        return mPlaintes;
    }
}