package com.sicmagroup.tondi.ui.home;

import android.content.Context;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.sicmagroup.tondi.History;
import com.sicmagroup.tondi.R;
import com.sicmagroup.tondi.Tontine;

import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class HistoryAdapter extends   RecyclerView.Adapter<HistoryAdapter.MyViewHolder>{

    private  Context mContext;
    private List<History> listHistory;
    private History history;
    public HistoryAdapter(Context mContext, List<History> listHistory ){
        this.mContext = mContext;
        this.listHistory = listHistory;

    }
    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.history_row_home, parent, false);
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        history = listHistory.get(position);

        holder.titre.setText(history.getTitre());
        holder.contenu.setText(history.getContenu());
        //Changer l'icon
        String titleActivity = history.getTitre();

        switch (titleActivity){
            case "Plainte":
                holder.icone.setImageResource(R.drawable.angry_plaint);
                break;
            case "Paramètres":
                holder.icone.setImageResource(R.drawable.setting_user);
                break;
            case "Connexion":
                if (history.getEtat()==1){
                    holder.icone.setImageResource(R.drawable.connexion_success);
                }else{
                    holder.icone.setImageResource(R.drawable.connexion_failed);
                }

                break;
            case "Nouvelle Tontine":
                holder.icone.setImageResource(R.drawable.icon_mecoti_w);
                break;
            case "Tontine Terminée":
                holder.icone.setImageResource(R.drawable.no_tontine);
                break;
            case "Retrait Momo":
                holder.icone.setImageResource(R.drawable.momo_versement);
                break;
            case "Retrait Espèces":
                holder.icone.setImageResource(R.drawable.retrait_espece);
                break;
            case "Versement":
                holder.icone.setImageResource(R.drawable.momo_versement);
                break;
        }
        Calendar cal = Calendar.getInstance(Locale.FRENCH);
        cal.setTimeInMillis(history.getCreation());
        String heure = DateFormat.format("HH:mm", cal).toString();
        holder.heure.setText(heure);
    }

    @Override
    public int getItemCount() {
        return listHistory.size();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {
        private final ImageView icone;

        private final TextView titre;
        private final TextView contenu;
        private final TextView heure;
        public MyViewHolder(@NonNull View view) {
            super(view);

            icone = (ImageView)view.findViewById(R.id.history_icone);
            titre = (TextView) view.findViewById(R.id.history_title);
            contenu = (TextView) view.findViewById(R.id.history_description);
            heure = (TextView) view.findViewById(R.id.history_time);

        }
    }
}
