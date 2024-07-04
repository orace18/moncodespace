package com.sicmagroup.tondi;

import android.annotation.SuppressLint;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Rect;
import android.os.Bundle;
import androidx.annotation.Nullable;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.text.format.DateFormat;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.ogaclejapan.smarttablayout.utils.v4.FragmentPagerItem;
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

public class CarteFragment extends Fragment {
    /*private static final int TAG_MODE = 11;
    RecyclerView mRecyclerView;
    FormBuilder frm_retrait = null;*/
    private RadioGroup radioGroup;
    private RadioButton radioButton;

  private RecyclerView recyclerView;
  private TontineAdapter adapter;
  private List<Tontine> tontineList;
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
    int position = FragmentPagerItem.getPosition(getArguments());
    //TextView title = (TextView) view.findViewById(R.id.item_title);
    //title.setText(String.valueOf(position));
      new Prefs.Builder()
              .setContext(getActivity())
              .setMode(ContextWrapper.MODE_PRIVATE)
              .setPrefsName(getActivity().getPackageName())
              .setUseDefaultSharedPreference(true)
              .build();
    recyclerView = (RecyclerView) view.findViewById(R.id.recycler_view);

    tontineList = new ArrayList<>();
    adapter = new TontineAdapter(this.getActivity(), tontineList, position);

    RecyclerView.LayoutManager mLayoutManager = new GridLayoutManager(this.getActivity(), 1);
    recyclerView.setLayoutManager(mLayoutManager);
    recyclerView.addItemDecoration(new GridSpacingItemDecoration(1, dpToPx(10), true));
    recyclerView.setItemAnimator(new DefaultItemAnimator());
    recyclerView.setAdapter(adapter);

    FloatingActionButton btn_nouvelle_tontine = (FloatingActionButton) view.findViewById(R.id.btn_nouvelle_tontine);
    // si fragment en cours
    if (position==0){
        btn_nouvelle_tontine.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(CarteFragment.this.getActivity(),NouvelleTontine.class));
            }
        });

    }else{
        btn_nouvelle_tontine.setVisibility(View.INVISIBLE);
    }



    // si fragment en cours
    if (position==0){
        afficherTontine(TontineEnum.IN_PROGRESS.toString());
    }
    // si fragment terminee
    if (position==1){
        afficherTontine(TontineEnum.COMPLETED.toString());

    }

      //si fragment en attente
      if(position==2){
          afficherTontine(TontineEnum.WAITING.toString());
      }

    // si fragment encaissee
    if (position==3){
        afficherTontine(TontineEnum.COLLECTED.toString());
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
  @SuppressLint("DefaultLocale")
  private void afficherTontine(final String statut) {
      List<Tontine> tontines = Select.from(Tontine.class)
              .where(Condition.prop("id_utilisateur").eq(Prefs.getString(ID_UTILISATEUR_KEY,null)),
                      Condition.prop("statut").eq(statut))
              .list();

      if (tontines!=null){

          String creation;
          String periode;
          int mise;
          int id;
          String montant;
          String versements;
          for (int i = 0; i < tontines.size(); i++) {
              Tontine row = tontines.get(i);
              Calendar cal = Calendar.getInstance(Locale.FRENCH);
              cal.setTimeInMillis(row.getCreation());
              String date_c = DateFormat.format("yyyy-MM-dd HH:mm:ss", cal).toString();
              creation = date_c;
              periode = row.getPeriode();
              mise = row.getMise();

              List<Versement> versements1 = Select.from(Versement.class)
                      .where(Condition.prop("id_tontine").eq(row.getId()))
                      .list();

              versements = String.format("%010d", Integer.parseInt(String.valueOf(versements1.size())));
              //montant = row.getMontant();

              Tontine tontine_row = new Tontine();
              tontine_row.setCreation(row.getCreation());
              tontine_row.setMaj(row.getCreation());
              tontine_row.setPeriode(periode);
              tontine_row.setMise(mise);
              //tontine_row.setMontant(montant);
              tontineList.add(tontine_row);
          }

          adapter.notifyDataSetChanged();
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
