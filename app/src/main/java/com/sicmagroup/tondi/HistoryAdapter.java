package com.sicmagroup.tondi;

import android.annotation.SuppressLint;
import android.content.Context;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.MyViewHolder> {
    public LinearLayout header;
    @Override
    public HistoryAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.history_row, parent, false);

        return new HistoryAdapter.MyViewHolder(itemView);
    }
    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView creation, titre, contenu,heure;
        public ImageView icone;

        public MyViewHolder(View view) {
            super(view);

            header = (LinearLayout) view.findViewById(R.id.history_header);
            icone = (ImageView)view.findViewById(R.id.history_icone);
            creation = (TextView) view.findViewById(R.id.history_date);
            titre = (TextView) view.findViewById(R.id.history_title);
            contenu = (TextView) view.findViewById(R.id.history_description);
            heure = (TextView) view.findViewById(R.id.history_time);

        }
    }
    private Context mContext;
    public HistoryAdapter(Context mContext, List<History> historyList) {
        this.mContext = mContext;
        this.historyList = historyList;

    }

    private List<History> historyList;
    @Override
    public void onBindViewHolder(final HistoryAdapter.MyViewHolder holder, int position) {

        History history = historyList.get(position);

        Calendar cal = Calendar.getInstance(Locale.FRENCH);
        cal.setTimeInMillis(history.getCreation());
        String date = DateFormat.format("dd/MM/yyyy", cal).toString();
        String heure = DateFormat.format("HH:mm", cal).toString();

        Date today = new Date();

        Calendar calnow = Calendar.getInstance(Locale.FRENCH);
        calnow.setTimeInMillis(today.getTime());
        String datenow = DateFormat.format("dd/MM/yyyy", calnow).toString();

        if (position!=0){
            History historyprev = historyList.get(position-1);
            Calendar calc = Calendar.getInstance(Locale.FRENCH);
            calc.setTimeInMillis(historyprev.getCreation());
            String datecmp = DateFormat.format("dd/MM/yyyy", calc).toString();

            if (datecmp.equals(date)){
                header.setVisibility(View.GONE);
            }
        }
        int jour = cal.get(Calendar.DAY_OF_MONTH);
        int aujour= calnow.get(Calendar.DAY_OF_MONTH);

        int compare = aujour-jour;
        switch (compare){
            case 0:
                holder.creation.setText("Aujourd'hui");
                break;
            case 1:
                holder.creation.setText("Hier");
                break;
            case 3:
                holder.creation.setText("Avant-hier");
                break;
            case 7:
                holder.creation.setText("Il y a une semaine");
                break;
            case 30:
                holder.creation.setText("Il y a 1 mois");
                break;
            case 90:
                holder.creation.setText("Il y a 3 mois");
                break;
            default:
                holder.creation.setText(date);
        }

        holder.titre.setText(history.getTitre());
        holder.contenu.setText(history.getContenu());
        holder.heure.setText(heure);
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
            default:

                break;
        }




        // Charge le logo du club dans l'ImageView
       // Glide
                //.with(holder.icone.getContext())
               // .load(history.)
               // .into(holder.logo);
        // loading album cover using Glide library
        //Glide.with(mContext).load(album.getThumbnail()).into(holder.thumbnail);


    }

    @Override
    public int getItemCount() {
        return historyList.size();
    }



}
