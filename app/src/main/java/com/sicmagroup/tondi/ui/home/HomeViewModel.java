package com.sicmagroup.tondi.ui.home;

import static com.sicmagroup.tondi.Connexion.ID_UTILISATEUR_KEY;
import static com.sicmagroup.tondi.Connexion.TEL_KEY;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.graphics.Color;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.orm.SugarRecord;
import com.orm.query.Condition;
import com.orm.query.Select;
import com.pixplicity.easyprefs.library.Prefs;
import com.sicmagroup.tondi.Connexion;
import com.sicmagroup.tondi.Enum.TontineEnum;
import com.sicmagroup.tondi.History;
import com.sicmagroup.tondi.HistoryActivity;
import com.sicmagroup.tondi.R;
import com.sicmagroup.tondi.Tontine;
import com.sicmagroup.tondi.Utilisateur;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Calendar;
import java.util.Locale;

public class HomeViewModel extends ViewModel {

    private final MutableLiveData<String> mText;
    private  final  MutableLiveData <List <Tontine> > mTontines;
    private final  MutableLiveData <List <History> > mHistories;


    public HomeViewModel() {
        mHistories = new MutableLiveData<>() ;
        mText = new MutableLiveData<>();
        mTontines = new MutableLiveData<>();
        mText.setValue("This is home fragment");

    }

    public LiveData<String> getText() {
        return mText;
    }

    public  LiveData <List<Tontine>> getTontines(){
        List<Tontine> tontineList = Select.from(Tontine.class)
                .where(Condition.prop("id_utilisateur").eq(Prefs.getString(ID_UTILISATEUR_KEY,null)),
                        Condition.prop("statut").eq(TontineEnum.IN_PROGRESS.toString()))
                .groupBy("continuer,mise,periode, carnet")
                .orderBy("id desc")
                .list();
        List<Tontine> tontineListFinal = new ArrayList<Tontine>();
        if (tontineList!=null) {

            for (int i = 0; i < tontineList.size(); i++) {
                Tontine row = tontineList.get(i);
                Calendar cal = Calendar.getInstance(Locale.FRENCH);
                cal.setTimeInMillis(row.getCreation());
                Tontine tontine = SugarRecord.findById(Tontine.class, row.getId());
                tontineListFinal.add(tontine);
            }
        }

        mTontines.setValue(tontineListFinal);
//        Log.e("test", mTontines.getValue().get(0).getCarnet());
        return  mTontines;
    }
    public LiveData <List <History> > getHistories(){
        List<Utilisateur> userH = SugarRecord.find(Utilisateur.class, "numero = ?", Prefs.getString(TEL_KEY,null));
        if(userH.size() > 0){
            mHistories.setValue( Select.from(History.class)
                    .orderBy("id desc")
                    .limit("15")
                    .list());
        }


        Log.e("histories", mHistories.getValue().size()+" = value ");
        return  mHistories;

    }

}