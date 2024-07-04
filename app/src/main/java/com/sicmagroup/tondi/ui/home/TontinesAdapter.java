package com.sicmagroup.tondi.ui.home;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.sicmagroup.tondi.CarteMain;
import com.sicmagroup.tondi.R;
import com.sicmagroup.tondi.Tontine;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class TontinesAdapter extends   RecyclerView.Adapter<TontinesAdapter.MyViewHolder>{

    private  Context mContext;
    private List<Tontine> listTontines;
    private Tontine tontine;
    public TontinesAdapter(Context mContext, List<Tontine> listTontines ){
        this.mContext = mContext;
        this.listTontines = listTontines;

    }
    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.carnet_design_row, parent, false);
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        tontine = listTontines.get(position);
        holder.type_carnet.setText("Carnet "+ tontine.getPeriode());
        holder.mise.setText("Mise: "+tontine.getMise()+" FCFA");
//        holder.total.setText("Total: "+ String.valueOf(tontine.getMontCumule(tontine.getStatut()) - tontine.getMontCumule(tontine.getStatut()) * 0.03)+" FCFA");
        holder.total.setText("Total: "+ String.valueOf(tontine.getMontCumule(tontine.getStatut()))+" FCFA");

        Calendar cal = Calendar.getInstance(Locale.FRENCH);
        cal.setTimeInMillis(tontine.getCreation());
        String date = DateFormat.format("yyyy-MM-dd HH:mm", cal).toString();
        holder.created_at.setText(date);
        holder.name.setText(tontine.getDenomination());
        if(tontine.getDateDeblocage() != null){
            holder.lockedIcon.setVisibility(View.VISIBLE);
            Log.e("tDateDebloc", tontine.getDateDeblocage()+" id "+tontine.getId_server());
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
            try {
                Date date_deblocage = format.parse(tontine.getDateDeblocage());
                Date now = Calendar.getInstance().getTime();
                if(date_deblocage.before(now)){
                    holder.lockedIcon.setImageResource(R.drawable.lock_open_48px);
                } else {
                    holder.lockedIcon.setImageResource(R.drawable.lock_48px);
                }
            } catch(ParseException e){
                e.printStackTrace();
            }
        } else {
            holder.lockedIcon.setVisibility(View.GONE);
        }

    }

    @Override
    public int getItemCount() {
        return listTontines.size();
    }

    public  class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private final TextView type_carnet;
        private final TextView created_at;
        private final TextView name;
        private final TextView mise;
        private final TextView total;
        private final ImageView lockedIcon;
        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            type_carnet = (TextView) itemView.findViewById(R.id.card_view_type);
            created_at = (TextView) itemView.findViewById(R.id.created_at);
            name = (TextView) itemView.findViewById(R.id.denomination);
            mise = (TextView) itemView.findViewById(R.id.mise);
            total = (TextView) itemView.findViewById(R.id.total);
            lockedIcon = itemView.findViewById(R.id.isBlocked);
            itemView.setOnClickListener(this);
        }


        @Override
        public void onClick(View v) {
            Log.e( "onClick: ", "gfgfgf");
            carte(listTontines.get(getAdapterPosition()).getId());
        }
    }
    private void carte(Long id_tontine){
        Intent intent = new Intent(new Intent(mContext, CarteMain.class));
        intent.putExtra("id_tontine",Integer.parseInt(String.valueOf(id_tontine)));
        mContext.startActivity(intent);
    }

}
