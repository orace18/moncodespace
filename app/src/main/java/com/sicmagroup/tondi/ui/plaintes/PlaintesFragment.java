package com.sicmagroup.tondi.ui.plaintes;

import static com.sicmagroup.tondi.Connexion.ID_UTILISATEUR_KEY;
import static com.sicmagroup.tondi.Connexion.TEL_KEY;
import static com.sicmagroup.tondi.Connexion.url_save_plainte;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.provider.Settings;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.romellfudi.permission.PermissionService;
import com.sicmagroup.tondi.Connexion;
import com.sicmagroup.tondi.Dashboard;
import com.sicmagroup.tondi.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkError;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.ServerError;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.pixplicity.easyprefs.library.Prefs;
import com.sicmagroup.tondi.Home;
import com.sicmagroup.tondi.MesPlaintes;
import com.sicmagroup.tondi.Plainte;
import com.sicmagroup.tondi.R;
import com.sicmagroup.tondi.Tontine;
import com.sicmagroup.tondi.Utilisateur;
import com.sicmagroup.tondi.Utilitaire;
import com.sicmagroup.tondi.databinding.FragmentHomeBinding;
import com.sicmagroup.tondi.databinding.FragmentPlaintesBinding;
import com.sicmagroup.tondi.ui.home.TontinesAdapter;
import com.tapadoo.alerter.Alerter;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class PlaintesFragment extends Fragment  {
    private static MediaRecorder mediaRecorder;
    private static MediaPlayer mediaPlayer;

    RecyclerView plainte_recyclerView;
    ArrayList<Plainte> plainteArrayList;
    PlainteAdapter plainteAdapter;
    File mypath;
    ProgressDialog progressDialog;
    JSONObject jsonObject;
    RequestQueue rQueue;
    Button sort_btn;
    Boolean sorted = false;
    public List<Plainte> list;

    private FragmentPlaintesBinding binding;
    private RecyclerView recyclerView;
    private PlainteAdapter plaintesAdapter;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        PlaintesViewModel plaintesViewModel =
                new ViewModelProvider(this).get(PlaintesViewModel.class);

        binding = FragmentPlaintesBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.RECORD_AUDIO}, 1);

        new PermissionService(getActivity()).request(
                new String[]{Manifest.permission.RECORD_AUDIO},
                callback);


        list =  plaintesViewModel.getPlaintes().getValue();

        plaintesAdapter = new PlainteAdapter(root.getContext(), list);
        recyclerView = (RecyclerView)  binding.plainteRecyclerView;
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(root.getContext(), LinearLayoutManager.VERTICAL,false);
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(plaintesAdapter);
        plaintesAdapter.notifyDataSetChanged();

        DialogFragment dialogFragment=new DialogFragment();

        FloatingActionButton btn_nouvelle_plainte = (FloatingActionButton)  binding.btnNouvellePlainte;
        btn_nouvelle_plainte.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialogFragment.show(getChildFragmentManager()  ,"My  Fragment");
            }
        });

        Utilitaire utilitaire = new Utilitaire(getContext());
        //swip response
        SwipeRefreshLayout swipeRefreshLayout = binding.swiperefresh;
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                utilitaire.refreshDatabse(swipeRefreshLayout);
            }
        });

        //dialogFragment.show(getChildFragmentManager()  ,"My  Fragment");
        return root;
    }

    PermissionService.Callback callback = new PermissionService.Callback() {
        public void onRefuse(ArrayList<String> RefusePermissions) {
            Toast.makeText(getContext(),
                    "L'application a besoin d'utiliser le micro de votre téléphone afin de pouvoir enrégistrer vos plaintes. Vous seul(e) pouvez déclencher ou non un enrégistrement.",
                    Toast.LENGTH_LONG).show();
            Intent intent = new Intent(getContext(), Home.class);
            startActivity(intent);
//            MesPlaintes.this.finish();
        }

        @Override
        public void onFinally() {
            // pass
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (!Settings.canDrawOverlays(getContext())) {
                    Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                            Uri.parse("package:" + getActivity().getPackageName()));
                    startActivity(intent);
                }
            }
        }
    };


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        callback.handler(permissions, grantResults);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}