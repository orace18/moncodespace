package com.sicmagroup.tondi.ui.tontines;

import static com.sicmagroup.tondi.Connexion.ID_UTILISATEUR_KEY;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.orm.query.Condition;
import com.orm.query.Select;
import com.pixplicity.easyprefs.library.Prefs;
import com.sicmagroup.tondi.Tontine;

import java.util.List;

public class TontinesViewModel extends ViewModel {

    private final MutableLiveData<Integer> mTotalTontine;
    public TontinesViewModel() {
        mTotalTontine = new MutableLiveData<>();
    }


    public LiveData<Integer> getTontineSize(){
        List<Tontine> liste_tontines_user = Select.from(Tontine.class)
                .where(Condition.prop("id_utilisateur").eq(Prefs.getString(ID_UTILISATEUR_KEY,"")))
                .list();
        mTotalTontine.setValue(liste_tontines_user.size());
        return mTotalTontine;
    }
}