package com.sicmagroup.tondi;


import android.annotation.SuppressLint;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.DialogInterface;
import android.content.Intent;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.pixplicity.easyprefs.library.Prefs;
import com.tapadoo.alerter.Alerter;
import com.tapadoo.alerter.OnHideAlertListener;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.sicmagroup.tondi.utils.Constantes.SERVEUR;
import static com.sicmagroup.tondi.utils.Constantes.STATUT_UTILISATEUR;
import static com.sicmagroup.tondi.utils.Constantes.accessToken;

/**
 * Created by Ravi Tamada on 18/05/16.
 */
public class PaiementAdapter extends RecyclerView.Adapter<PaiementAdapter.MyViewHolder> {

    private Context mContext;
    private List<Sim> paiementList;
    String url_deletesim = SERVEUR+"/api/v1/sims/retirer";

    public class MyViewHolder extends RecyclerView.ViewHolder  implements View.OnClickListener {
        public TextView reseau, id;
        public Button supprimer;

        public MyViewHolder(View view) {
            super(view);

            new Prefs.Builder()
                    .setContext(mContext)
                    .setMode(ContextWrapper.MODE_PRIVATE)
                    .setPrefsName(mContext.getPackageName())
                    .setUseDefaultSharedPreference(true)
                    .build();

            id = (TextView) view.findViewById(R.id.id_paiement);
            reseau = (TextView) view.findViewById(R.id.reseau);
            supprimer = view.findViewById(R.id.btn_del_paiement);

            view.setOnClickListener(this);
            supprimer.setOnClickListener(this);

            //Si l'utilisateur est désactivé de façon temporaire
            if (Prefs.contains(STATUT_UTILISATEUR))
            {
                switch (Prefs.getString(STATUT_UTILISATEUR, null)) {
                    case "desactive temp":
                        supprimer.setEnabled(false);
                        break;
                    case "active":
                        supprimer.setEnabled(true);
                        break;
                }
            }

        }


        @Override
        public void onClick(View v) {
            if (v.getId() == supprimer.getId()) {
                alertView("Retrait de SIM","Êtes vous sûr de vouloir retirer cette sim?",paiementList.get(getAdapterPosition()).getId());
            }

        }
    }


    public PaiementAdapter(Context mContext, List<Sim> paiementList) {
        this.mContext = mContext;
        this.paiementList = paiementList;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.paiement_row, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, int position) {
        Sim paiement = paiementList.get(position);
        holder.reseau.setText(paiement.getReseau());
        holder.id.setText(" ( "+paiement.getNumero()+" ) ");

        // loading album cover using Glide library
        //Glide.with(mContext).load(album.getThumbnail()).into(holder.thumbnail);


    }



    @Override
    public int getItemCount() {
        return paiementList.size();
    }

    private void alertView(String title , String message, final Long id_sim ) {
        AlertDialog.Builder dialog = new AlertDialog.Builder(mContext);
        dialog.setTitle( title )
                .setIcon(R.drawable.ic_warning)
                .setMessage(message)
                .setNegativeButton("Non", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialoginterface, int i) {
                        dialoginterface.cancel();
                    }})
                .setPositiveButton("Oui", new DialogInterface.OnClickListener() {
                    @SuppressLint("ResourceAsColor")
                    public void onClick(DialogInterface dialoginterface, int i) {

                        deleteSim(id_sim);

                    }
                }).show();
    }

    private void deleteSim(final Long id_sim) {
        Sim sim = Sim.findById(Sim.class, id_sim);
        sim.delete();

        RequestQueue queue = Volley.newRequestQueue(mContext);

        StringRequest postRequest = new StringRequest(Request.Method.POST, url_deletesim,
                new Response.Listener<String>()
                {
                    @SuppressLint("ResourceAsColor")
                    @Override
                    public void onResponse(String response) {
                        // response
                        Log.d("Response", response);
                        try {

                            JSONObject result = new JSONObject(response);
                            //Log.d("My App", obj.toString());
                            if (result.getBoolean("success")){
                                Alerter.create((MonCompte) mContext)
                                        .setTitle(result.getString("message"))
                                        .setIcon(R.drawable.ic_check)
                                        //.setTitleAppearance(R.style.TextAppearance_AppCompat_Large)
                                        //.setIconColorFilter(R.color.colorPrimaryDark)
                                        //.setText("Vous pouvez maintenant vous connecter.")
                                        .setBackgroundColorRes(R.color.colorPrimaryDark) // or setBackgroundColorInt(Color.CYAN)
                                        .setOnHideListener(new OnHideAlertListener() {
                                            @Override
                                            public void onHide() {
                                                ((MonCompte) mContext).finish();
                                                mContext.startActivity(new Intent(mContext,MonCompte.class));
                                            }
                                        })
                                        .show();
                            }else{
                                Alerter.create((MonCompte) mContext)
                                        .setTitle(result.getString("message"))
                                        .setIcon(R.drawable.ic_warning)
                                        .setTitleAppearance(R.style.TextAppearance_AppCompat_Large)
                                        .setIconColorFilter(R.color.colorPrimaryDark)
                                        //.setText("Vous pouvez maintenant vous connecter.")
                                        .setBackgroundColorRes(R.color.colorWhite) // or setBackgroundColorInt(Color.CYAN)
                                        .show();
                            }


                        } catch (Throwable t) {
                            //Log.e("My App", "Could not parse malformed JSON: \"" + response + "\"");
                        }

                    }
                },
                new Response.ErrorListener()
                {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // error
                        //Log.d("Error.NouvelleTontine", error.getMessage());
                    }
                }
        ) {
            @Override
            protected Map<String, String> getParams()
            {

                Map<String, String>  params = new HashMap<String, String>();

                params.put("id_sim", String.valueOf(id_sim));

                return params;
            }
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                headers.put("Content-Type", "application/json");
                headers.put("Authorization", "Bearer " + accessToken); // Ajoute le token ici
                return headers;
            }
        };
        queue.add(postRequest);
    }
}
