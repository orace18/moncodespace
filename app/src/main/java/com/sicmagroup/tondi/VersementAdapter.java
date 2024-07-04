package com.sicmagroup.tondi;


import android.content.Context;
import androidx.appcompat.widget.PopupMenu;
import androidx.recyclerview.widget.RecyclerView;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Calendar;
import java.util.List;
import java.util.Locale;

/**
 * Created by Ravi Tamada on 18/05/16.
 */
public class VersementAdapter extends RecyclerView.Adapter<VersementAdapter.MyViewHolder> {

    private Context mContext;
    private List<Versement> versementList;

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView creation, titre, montant;
        public ImageView thumbnail, overflow;

        public MyViewHolder(View view) {
            super(view);
            creation = (TextView) view.findViewById(R.id.creation);
            titre = (TextView) view.findViewById(R.id.titre);
            montant = (TextView) view.findViewById(R.id.montant_verse);
        }
    }


    public VersementAdapter(Context mContext, List<Versement> versementList) {
        this.mContext = mContext;
        this.versementList = versementList;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.versement_row, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, int position) {
        Versement versement = versementList.get(position);
        Calendar cal = Calendar.getInstance(Locale.FRENCH);
        cal.setTimeInMillis(versement.getCreation());
        String date = DateFormat.format("yyyy-MM-dd à HH:mm", cal).toString();
        holder.creation.setText("Effectué le "+date);
        holder.titre.setText("Tontine "+versement.getTontine().getPeriode()+" de "+versement.getTontine().getMise()+" F");
        holder.montant.setText(versement.getMontant()+" F");

        // loading album cover using Glide library
        //Glide.with(mContext).load(album.getThumbnail()).into(holder.thumbnail);


    }

    /**
     * Showing popup menu when tapping on 3 dots
     */
    private void showPopupMenu(View view) {
        // inflate menu
        PopupMenu popup = new PopupMenu(mContext, view);
        MenuInflater inflater = popup.getMenuInflater();
        inflater.inflate(R.menu.tontine, popup.getMenu());
        popup.setOnMenuItemClickListener(new MyMenuItemClickListener());
        popup.show();
    }



    /**
     * Click listener for popup menu items
     */
    class MyMenuItemClickListener implements PopupMenu.OnMenuItemClickListener {

        public MyMenuItemClickListener() {
        }

        @Override
        public boolean onMenuItemClick(MenuItem menuItem) {
            switch (menuItem.getItemId()) {
                case R.id.action_carte_tontine:
                    Toast.makeText(mContext, "Add to favourite", Toast.LENGTH_SHORT).show();
                    return true;
                case R.id.action_terminer_tontine:
                    Toast.makeText(mContext, "Play next", Toast.LENGTH_SHORT).show();
                    return true;
                default:
            }
            return false;
        }
    }

    @Override
    public int getItemCount() {
        return versementList.size();
    }
}
