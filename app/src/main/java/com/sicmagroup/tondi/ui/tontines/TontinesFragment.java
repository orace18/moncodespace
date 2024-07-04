package com.sicmagroup.tondi.ui.tontines;

import static com.sicmagroup.tondi.Connexion.ID_UTILISATEUR_KEY;
import static com.sicmagroup.tondi.utils.Constantes.STATUT_UTILISATEUR;
import static com.sicmagroup.tondi.utils.Constantes.ACCESS_NOUVELLE_TONTINE;
import static com.sicmagroup.tondi.utils.Constantes.TAB_TITLE_ENCAISSEE;
import static com.sicmagroup.tondi.utils.Constantes.TAB_TITLE_EN_ATTENTE;
import static com.sicmagroup.tondi.utils.Constantes.TAB_TITLE_EN_COURS;
import static com.sicmagroup.tondi.utils.Constantes.TAB_TITLE_TERMINEE;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.ogaclejapan.smarttablayout.SmartTabLayout;
import com.ogaclejapan.smarttablayout.utils.v4.FragmentPagerItemAdapter;
import com.ogaclejapan.smarttablayout.utils.v4.FragmentPagerItems;
import com.orm.query.Condition;
import com.orm.query.Select;
import com.pixplicity.easyprefs.library.Prefs;
import com.sicmagroup.tondi.MesTontines;
import com.sicmagroup.tondi.NouvelleTontine;
import com.sicmagroup.tondi.R;
import com.sicmagroup.tondi.Tontine;
import com.sicmagroup.tondi.TontineFragment;
import com.sicmagroup.tondi.Utilitaire;
import com.sicmagroup.tondi.WrapContentHeightViewPager;
import com.sicmagroup.tondi.databinding.FragmentTontinesBinding;

import java.util.List;

public class TontinesFragment extends Fragment {

    private FragmentTontinesBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        TontinesViewModel tontinesViewModel =
                new ViewModelProvider(this).get(TontinesViewModel.class);

        binding = FragmentTontinesBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        Utilitaire utilitaire = new Utilitaire(getContext());



        FragmentPagerItemAdapter adapter = new FragmentPagerItemAdapter(
                getChildFragmentManager(), FragmentPagerItems.with(root.getContext())
                .add(TAB_TITLE_EN_COURS, TontineFragment.class)
                .add(TAB_TITLE_TERMINEE, TontineFragment.class)
                .add(TAB_TITLE_EN_ATTENTE, TontineFragment.class)
                .add(TAB_TITLE_ENCAISSEE, TontineFragment.class)
                .create());




        WrapContentHeightViewPager viewPager = (WrapContentHeightViewPager) binding.viewpager;
        viewPager.setAdapter(adapter);

        SmartTabLayout viewPagerTab = (SmartTabLayout) binding.viewpagertab;
        viewPagerTab.setViewPager(viewPager);

        FloatingActionButton btn_nouvelle_tontine = (FloatingActionButton)  binding.btnNouvelleTontine;


        if (tontinesViewModel.getTontineSize().getValue().intValue() == 0){
            // afficher directement la page de nouvelle tontine
            if (!Prefs.getBoolean(ACCESS_NOUVELLE_TONTINE,true))
                startActivity(new Intent(root.getContext(), NouvelleTontine.class));
        }
        btn_nouvelle_tontine.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(root.getContext(),NouvelleTontine.class);
                startActivity(i);
            }
        });
        //Si l'utilisateur est désactivé de façon temporaire
        if (Prefs.contains(STATUT_UTILISATEUR))
        {
            switch (Prefs.getString(STATUT_UTILISATEUR, null)) {
                case "desactive temp":
                    btn_nouvelle_tontine.setEnabled(false);
                    break;
                case "active":
                    btn_nouvelle_tontine.setEnabled(true);
                    break;
            }
        }

        //swip response
//        SwipeRefreshLayout swipeRefreshLayout = binding.swiperefresh;
//        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
//            @Override
//            public void onRefresh() {
//                utilitaire.refreshDatabse(swipeRefreshLayout);
//            }
//        });


//        final TextView textView = binding.textTontines;
//        tontinesViewModel.getText().observe(getViewLifecycleOwner(), textView::setText);
        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}