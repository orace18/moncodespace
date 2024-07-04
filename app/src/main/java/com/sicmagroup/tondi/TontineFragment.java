package com.sicmagroup.tondi;

import android.annotation.SuppressLint;
import android.content.ContextWrapper;
import android.content.res.Resources;
import android.graphics.Rect;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.ogaclejapan.smarttablayout.utils.v4.FragmentPagerItem;
import com.orm.SugarRecord;
import com.orm.query.Condition;
import com.orm.query.Select;
import com.pixplicity.easyprefs.library.Prefs;
import com.sicmagroup.tondi.Enum.TontineEnum;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import static com.sicmagroup.tondi.Connexion.ID_UTILISATEUR_KEY;
import static com.sicmagroup.tondi.utils.Constantes.SERVEUR;

public class TontineFragment extends Fragment {
    /*private static final int TAG_MODE = 11;
    RecyclerView mRecyclerView;
    FormBuilder frm_retrait = null;*/
    private RadioGroup radioGroup;
    private RadioButton radioButton;
    private  ImageView no_tontine;
    private RecyclerView recyclerView;
    private TontineAdapter adapter;
    private List<Tontine> tontineList;
   // private TextView ifNoTontineMsg;
    String url_afficher = SERVEUR+"/api/v1/tontines/afficher";
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_tontine, container, false);
    }


    @SuppressLint("RestrictedApi")
    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        final Bundle bdl = getArguments();
        Log.e("test", "cccccccc");
        int position = FragmentPagerItem.getPosition(getArguments());
//        Log.e("position", position+"");

        if (bdl!=null &&  bdl.containsKey("pos")){
            try
            {
                int pos = bdl.getInt("pos");
                position = pos;
            }
            catch(final Exception e)
            {
                // Do nothing
            }
        }

        //int pos = getIntent.getIntExtra("pos",0);


        //TextView title = (TextView) view.findViewById(R.id.item_title);
        //title.setText(String.valueOf(position));
        new Prefs.Builder()
                .setContext(getActivity())
                .setMode(ContextWrapper.MODE_PRIVATE)
                .setPrefsName(getActivity().getPackageName())
                .setUseDefaultSharedPreference(true)
                .build();

       // ifNoTontineMsg = (TextView)  view.findViewById(R.id.ifNoHistoryMsg);
        recyclerView = (RecyclerView) view.findViewById(R.id.recycler_view);

        tontineList = new ArrayList<>();
        adapter = new TontineAdapter(this.getActivity(), tontineList, position);

        RecyclerView.LayoutManager mLayoutManager = new GridLayoutManager(this.getActivity(), 1);
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.addItemDecoration(new GridSpacingItemDecoration(1, dpToPx(10), true));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(adapter);
        no_tontine = (ImageView) view.findViewById(R.id.imageView_notontine);


    /*Button btn_nouvelle_tontine =  view.findViewById(R.id.btn_nouvelle_tontine);
    // si fragment en cours
    if (position==0){
        btn_nouvelle_tontine.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(TontineFragment.this.getActivity(),NouvelleTontine.class));
            }
        });

    }else{
        btn_nouvelle_tontine.setVisibility(View.INVISIBLE);
    }*/


        Log.d("position", String.valueOf(position));
        // si fragment en cours
        if (position==0){
            afficherTontine(TontineEnum.IN_PROGRESS);
        }
        // si fragment terminee
        else if (position==1){
            afficherTontine(TontineEnum.COMPLETED);

        }
        // si fragment en attente
        else if (position==2){
            afficherTontine(TontineEnum.WAITING);
        }

        // si fragment encaissee
        else if (position==3){
            afficherTontine(TontineEnum.COLLECTED);
        }

    }



   /* void setupFrmPin(Dialog d, Context c){
        mRecyclerView =  d.findViewById(R.id.form_retrait);
        frm_retrait = new FormBuilder(c, mRecyclerView);

        FormElementTextNumber element6 = FormElementTextNumber.createInstance().setTag(TAG_MODE).setTitle("").setRequired(true);
        List<BaseFormElement> formItems = new ArrayList<>();
        formItems.add(element6);

        frm_retrait.addFormElements(formItems);
    }

    public class ViewDialog {

        void showDialog(Activity activity, final Intent intent){
            final Dialog dialog = new Dialog(activity);
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.setCancelable(true);
            dialog.setContentView(R.layout.pin_access);
            setupFrmPin(dialog,dialog.getContext());


            Button dialogButton = (Button) dialog.findViewById(R.id.btn_continue);
            dialogButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    BaseFormElement pin = frm_retrait.getFormElement(TAG_MODE);

                    String pin_value = pin.getValue();
                    if (pin_value.equals(Prefs.getString(PIN_KEY,""))){
                        startActivity(intent);
                    }
                    dialog.dismiss();

                }
            });
            //Toast.makeText(getApplicationContext(),"e",Toast.LENGTH_LONG).show();

            dialog.show();

        }
    }*/

    /**
     * Adding few albums for testing
     */
    private void afficherTontine(final TontineEnum statut) {
        List<Tontine> tontines;
        if(statut.toString().equals(TontineEnum.IN_PROGRESS.toString())){
            tontines = Select.from(Tontine.class)
                    .where(Condition.prop("id_utilisateur").eq(Prefs.getString(ID_UTILISATEUR_KEY,null)),
                            Condition.prop("statut").eq(statut.toString()))
                    .groupBy("continuer,mise,periode, carnet")
                    .orderBy("id desc")
                    .list();
        } else {
            tontines = Select.from(Tontine.class)
                    .where(Condition.prop("id_utilisateur").eq(Prefs.getString(ID_UTILISATEUR_KEY,null)),
                            Condition.prop("statut").eq(statut.toString()))
                    .groupBy("continuer,mise,periode, carnet")
                    .orderBy("id desc")
                    .list();
        }
        if (tontines!=null){

            for (int i = 0; i < tontines.size(); i++) {
                Tontine row = tontines.get(i);
                Calendar cal = Calendar.getInstance(Locale.FRENCH);
                cal.setTimeInMillis(row.getCreation());

                Tontine tontine = SugarRecord.findById(Tontine.class,row.getId());
                if(statut.equals(TontineEnum.COMPLETED.toString())){

                    tontineList.add(tontine);
                }else{
                    tontineList.add(tontine);
                }
            }

            adapter.notifyDataSetChanged();
            //no_tontine.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        }
    }

    /**
     * RecyclerView item decoration - give equal margin around grid item
     */
    public class GridSpacingItemDecoration extends RecyclerView.ItemDecoration {

        private int spanCount;
        private int spacing;
        private boolean includeEdge;

        public GridSpacingItemDecoration(int spanCount, int spacing, boolean includeEdge) {
            this.spanCount = spanCount;
            this.spacing = spacing;
            this.includeEdge = includeEdge;
        }

        @Override
        public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
            int position = parent.getChildAdapterPosition(view); // item position
            int column = position % spanCount; // item column

            if (includeEdge) {
                outRect.left = spacing - column * spacing / spanCount; // spacing - column * ((1f / spanCount) * spacing)
                outRect.right = (column + 1) * spacing / spanCount; // (column + 1) * ((1f / spanCount) * spacing)

                if (position < spanCount) { // top edge
                    outRect.top = spacing;
                }
                outRect.bottom = spacing; // item bottom
            } else {
                outRect.left = column * spacing / spanCount; // column * ((1f / spanCount) * spacing)
                outRect.right = spacing - (column + 1) * spacing / spanCount; // spacing - (column + 1) * ((1f /    spanCount) * spacing)
                if (position >= spanCount) {
                    outRect.top = spacing; // item top
                }
            }
        }
    }

    /**
     * Converting dp to pixel
     */
    private int dpToPx(int dp) {
        Resources r = getResources();
        return Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, r.getDisplayMetrics()));
    }

}
