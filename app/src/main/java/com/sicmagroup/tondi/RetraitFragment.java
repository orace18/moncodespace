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
import android.text.format.DateFormat;
import android.util.Log;
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
import com.sicmagroup.tondi.Enum.RetraitEnum;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import static com.sicmagroup.tondi.Connexion.ID_UTILISATEUR_KEY;
import static com.sicmagroup.tondi.utils.Constantes.SERVEUR;

public class RetraitFragment extends Fragment {
    /*private static final int TAG_MODE = 11;
    RecyclerView mRecyclerView;
    FormBuilder frm_retrait = null;*/
    private RadioGroup radioGroup;
    private RadioButton radioButton;

  private RecyclerView recyclerView;
  private RetraitAdapter adapter;
  private List<Retrait> retraitsList;
  String url_afficher = SERVEUR+"/api/v1/retraits/afficher";
  @Override
  public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
      @Nullable Bundle savedInstanceState) {

    return inflater.inflate(R.layout.fragment_retrait, container, false);
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

      retraitsList = new ArrayList<>();
    adapter = new RetraitAdapter(this.getActivity(), retraitsList);

    RecyclerView.LayoutManager mLayoutManager = new GridLayoutManager(this.getActivity(), 1);
    recyclerView.setLayoutManager(mLayoutManager);
    recyclerView.addItemDecoration(new GridSpacingItemDecoration(1, dpToPx(10), true));
    recyclerView.setItemAnimator(new DefaultItemAnimator());
    recyclerView.setAdapter(adapter);
    /*recyclerView.addOnItemTouchListener(new RecyclerView.OnItemTouchListener() {
        @Override
        public boolean onInterceptTouchEvent(@NonNull RecyclerView recyclerView, @NonNull MotionEvent motionEvent) {

            Toast.makeText(getActivity(), "hfvhre"+recyclerView.ge, Toast.LENGTH_SHORT).show();
            return false;
        }

        @Override
        public void onTouchEvent(@NonNull RecyclerView recyclerView, @NonNull MotionEvent motionEvent) {

        }

        @Override
        public void onRequestDisallowInterceptTouchEvent(boolean b) {

        }
    });*/

      /*recyclerView.setOnClickListener(new View.OnClickListener() {
          @Override
          public void onClick(View v) {
              Toast.makeText(getActivity(), "hfvhre", Toast.LENGTH_SHORT).show();
              //Intent intent = new Intent(getActivity(),Encaisser.class);
              //intent.putExtra("id_tontine", String.valueOf(v.getTag()));
              //startActivity(intent);
          }
      });*/


    // si fragment en cours
    if (position==1){
        afficherRetraits(RetraitEnum.IN_PROGRESS.name());
    }
    // si fragment terminee
//    if (position==2){
//        afficherTontine("valide");
//
//    }



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
  private void afficherRetraits(final String statut) {
      Utilisateur u = Utilisateur.find(Utilisateur.class, "id_utilisateur = ?", Prefs.getString(ID_UTILISATEUR_KEY,null)).get(0);

      List<Retrait> retraits = Select.from(Retrait.class)
              .where(Condition.prop("utilisateur").eq(String.valueOf(u.getId())),
                      Condition.prop("statut").eq(statut), Condition.prop("token").notEq("null"))

//              .whereOr(Condition.prop("utilisateur").eq(Prefs.getString(ID_UTILISATEUR_KEY,null)),
//                      Condition.prop("statut").eq("en attente"))
              .orderBy("creation desc")
              .list();

      if (retraits.size()>0){
          String creation;
          String periode;
          int mise;
          int id;
          String montant;
          String versements;
          for (int i = 0; i < retraits.size(); i++) {
//              if (!retraits.get(i).getStatut().equals("valide"))
//              {
                  Retrait row = retraits.get(i);
                  Calendar cal = Calendar.getInstance(Locale.FRENCH);
                  cal.setTimeInMillis(row.getCreation());
                  String date_c = DateFormat.format("yyyy-MM-dd HH:mm:ss", cal).toString();
                  creation = date_c;
                  periode = row.getTontine().getPeriode();
                  mise = row.getTontine().getMise();

                  List<Versement> versements1 = Select.from(Versement.class)
                          .where(Condition.prop("tontine").eq(row.getId()))
                          .list();

                  //montant = row.getMontant();
                  //Tontine t = SugarRecord.findById(Tontine.class, retraits.get(i).getTontine());
                  Retrait retrait_row = new Retrait();
                  retrait_row.setCreation(row.getCreation());
//                  retrait_row.setMaj(row.getCreation());
                  retrait_row.setMaj(row.getMaj());
                  retrait_row.setToken(retraits.get(i).getToken());
                  retrait_row.setTontine(retraits.get(i).getTontine());
                  retrait_row.setId(row.getId());
                  Log.e("test_montant", row.getMontant());
                    retrait_row.setMontant(row.getMontant());
                  //tontine_row.setMontant(montant);
                  if (retrait_row.getExpire()>0){
                      retraitsList.add(retrait_row);
                  }

//              }

          }
          //Log.d("Retraits__","i:"+retraitsList.get(0).getToken()+"mise:"+retraitsList.get(0).getTontine().getMise());
          //Toast.makeText(getActivity(), "i:"+retraitsList.get(0).getId(), Toast.LENGTH_SHORT).show();
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
