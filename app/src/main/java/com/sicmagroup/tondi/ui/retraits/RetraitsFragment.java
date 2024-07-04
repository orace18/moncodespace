package com.sicmagroup.tondi.ui.retraits;

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
import com.sicmagroup.tondi.Ccm;
import com.sicmagroup.tondi.MesRetraits;
import com.sicmagroup.tondi.R;
import com.sicmagroup.tondi.RetraitFragment;
import com.sicmagroup.tondi.TontineFragment;
import com.sicmagroup.tondi.Utilitaire;
import com.sicmagroup.tondi.WrapContentHeightViewPager;
import com.sicmagroup.tondi.databinding.FragmentRetraitsBinding;

public class RetraitsFragment extends Fragment {

    private FragmentRetraitsBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        RetraitsViewModel retraitsViewModel =
                new ViewModelProvider(this).get(RetraitsViewModel.class);

        binding = FragmentRetraitsBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        Bundle bundle =  new Bundle();
        bundle.putInt("pos",1);
        Bundle bundle1 =  new Bundle();
        bundle1.putInt("pos",3);
        FragmentPagerItemAdapter adapter = new FragmentPagerItemAdapter(
                getChildFragmentManager(), FragmentPagerItems.with(getContext())
                .add("Tontines terminées", TontineFragment.class, bundle)
                .add("Codes retraits", RetraitFragment.class)
                .add("Déjà encaissées", TontineFragment.class,bundle1)
                .create());

        WrapContentHeightViewPager viewPager = (WrapContentHeightViewPager) binding.viewpager;
        viewPager.setAdapter(adapter);

        SmartTabLayout viewPagerTab = (SmartTabLayout) binding.viewpagertab;
        viewPagerTab.setViewPager(viewPager);
        Utilitaire utilitaire = new Utilitaire(getContext());


        FloatingActionButton btn_cmt_retirer = (FloatingActionButton) binding.btnCmtRetirer;
        btn_cmt_retirer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                //MesRetraits.this.finish();
                startActivity(new Intent(getContext(), Ccm.class));
            }
        });
//        //swip response
//        SwipeRefreshLayout swipeRefreshLayout = binding.swiperefresh;
//        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
//            @Override
//            public void onRefresh() {
//                utilitaire.refreshDatabse(swipeRefreshLayout);
//            }
//        });

//        final TextView textView = binding.textDashboard;
//        retraitsViewModel.getText().observe(getViewLifecycleOwner(), textView::setText);
        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}