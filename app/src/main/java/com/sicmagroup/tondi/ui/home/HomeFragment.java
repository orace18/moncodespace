package com.sicmagroup.tondi.ui.home;


import static android.content.Context.MODE_PRIVATE;
import static com.sicmagroup.tondi.Connexion.ID_UTILISATEUR_KEY;
import static com.sicmagroup.tondi.Connexion.NOM_KEY;
import static com.sicmagroup.tondi.Connexion.NUMERO_COMPTE_KEY;
import static com.sicmagroup.tondi.Connexion.PRENOMS_KEY;
import static com.sicmagroup.tondi.utils.Constantes.STATUT_UTILISATEUR;

import android.app.Activity;
import android.app.Dialog;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DefaultItemAnimator;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.pixplicity.easyprefs.library.Prefs;
import com.sicmagroup.tondi.Connexion;
import com.sicmagroup.tondi.Dashboard;
import com.sicmagroup.tondi.History;

import com.sicmagroup.tondi.HistoryActivity;
import com.sicmagroup.tondi.Home;
import com.sicmagroup.tondi.R;
import com.sicmagroup.tondi.Tontine;
import com.sicmagroup.tondi.TontineFragment;
import com.sicmagroup.tondi.databinding.FragmentHomeBinding;
import com.sicmagroup.tondi.utils.Constantes;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HomeFragment extends Fragment {
    private FragmentHomeBinding binding;
    private RecyclerView recyclerView;
    private  RecyclerView recyclerViewHistory;
    private TontinesAdapter tontinesAdapter;
    private HistoryAdapter historyAdapter;
    private List <History>  historyList;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        HomeViewModel homeViewModel =
                new ViewModelProvider(this).get(HomeViewModel.class);
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        new Prefs.Builder()
                .setContext(getContext())
                .setMode(MODE_PRIVATE)
                .setPrefsName(getActivity().getPackageName())
                .setUseDefaultSharedPreference(true)
                .build();
        TextView nameUser = (TextView) binding.userName;
        TextView compteUser = (TextView) binding.compteUser;

        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("MyPrefs", MODE_PRIVATE);
        String nom = sharedPreferences.getString("nom", "Moi");
        String prenoms = sharedPreferences.getString("prenoms", "Même");
        Log.e("Le nom est :", nom);
        Log.e("Le prenom est :", prenoms);

        nameUser.setText(Prefs.getString(NOM_KEY, nom) + " " + Prefs.getString(PRENOMS_KEY, prenoms));
        compteUser.setText("Compte N°:" + Prefs.getString(NUMERO_COMPTE_KEY, "000000000000"));


        View root = binding.getRoot();
        List<Tontine> list =  homeViewModel.getTontines().getValue();
        tontinesAdapter = new TontinesAdapter(root.getContext(), list);
        recyclerView = (RecyclerView)  binding.recyclerViewHome;
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(root.getContext(), LinearLayoutManager.HORIZONTAL,false);
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(tontinesAdapter);
        tontinesAdapter.notifyDataSetChanged();

        historyList = homeViewModel.getHistories().getValue();
        historyAdapter = new HistoryAdapter(root.getContext(), historyList);
        recyclerViewHistory = (RecyclerView) binding.recyclerViewHistory;
        LinearLayoutManager mLayoutManagerHistory = new LinearLayoutManager(root.getContext(), LinearLayoutManager.VERTICAL,false);
        recyclerViewHistory.setLayoutManager(mLayoutManagerHistory);
        recyclerViewHistory.setItemAnimator(new DefaultItemAnimator());
        recyclerViewHistory.setAdapter(historyAdapter);
        historyAdapter.notifyDataSetChanged();
        Button btn_afficher_tout = (Button) binding.btnAfficherTout;


        btn_afficher_tout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ConstraintLayout homeLayout = (ConstraintLayout) getActivity().findViewById(R.id.container);
                BottomNavigationView bottomNavigationView = (BottomNavigationView) homeLayout.findViewById(R.id.nav_view);
                bottomNavigationView.setSelectedItemId(R.id.navigation_tontines);
            }
        });
        TextView voir_plus_history =(TextView) binding.voirPlusHistory;
        voir_plus_history.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getContext(), HistoryActivity.class);
                startActivity(intent);
            }
        });

        requireActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(), new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                Log.e("TAG", "handleOnBackPressed: onBack pressed");
                Dialog dialog = new Dialog(getContext());
                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                dialog.setCancelable(true);
                dialog.setContentView(R.layout.dialog_deconnexion);

                ImageView imageView = (ImageView) dialog.findViewById(R.id.imageView);
                imageView.setImageResource(R.drawable.outline_logout_white_48);

                TextView titre = (TextView) dialog.findViewById(R.id.deco_title);
                titre.setText("Quitter l'application");
                TextView message_deco = (TextView) dialog.findViewById(R.id.deco_message);
                message_deco.setText("Êtes vous sûr de vouloir fermer l'application ?");

                Button oui = (Button) dialog.findViewById(R.id.btn_oui);
                oui.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Prefs.putString(ID_UTILISATEUR_KEY,null);
                        dialog.dismiss();
                        getActivity().finishAffinity();
//                Intent startMain = new Intent(Intent.ACTION_MAIN);
//                startMain.addCategory(Intent.CATEGORY_HOME);
//                startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                startActivity(startMain);
                    }
                });

                Button non = (Button) dialog.findViewById(R.id.btn_non);
                non.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                    }
                });

                dialog.show();
            }
        });
//        final TextView textView = binding.textHome;
//        homeViewModel.getText().observe(getViewLifecycleOwner(), textView::setText);

        TextView alert = (TextView) binding.alerte;
        if (Prefs.contains(STATUT_UTILISATEUR)){
            Log.e("statut", Prefs.getString(STATUT_UTILISATEUR, null));
            switch (Prefs.getString(STATUT_UTILISATEUR, null)) {
                case "desactive temp":
                    alert.setVisibility(View.VISIBLE);
                    alert.setText(R.string.alerte_desac_temp);
                    break;
                case "desactive":
                    alert.setVisibility(View.VISIBLE);
                    Prefs.putString(ID_UTILISATEUR_KEY, null);
                    startActivity(new Intent(getContext(), Connexion.class));
                    ((Activity) getContext()).finish();
                    break;
                case "active":
                    alert.setVisibility(View.GONE);
                    break;
            }
        }

        return root;

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }


}