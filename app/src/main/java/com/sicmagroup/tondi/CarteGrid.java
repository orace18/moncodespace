package com.sicmagroup.tondi;

import android.content.Context;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.sicmagroup.tondi.Enum.PeriodiciteEnum;

import java.util.ArrayList;
import java.util.List;

public class CarteGrid extends BaseAdapter {

    private Context mContext;
    private final List<Versement> gridViewString;
    int nb_versements;
    String periode;
    private ArrayList card;
    private ArrayList isVersementOk;
    private LayoutInflater layoutInflater;


    public CarteGrid(Context context, List<Versement> gridViewString, int nb_versements, String periode) {
        mContext = context;
        this.gridViewString = gridViewString;
        this.nb_versements = nb_versements;
        this.periode = periode;
        this.card = new ArrayList();
        this.isVersementOk = new ArrayList();
        Log.e("nbV", periode+"");
        layoutInflater = LayoutInflater.from(context);
        if (this.periode.equals(PeriodiciteEnum.MENSUELLE.toString()))
        {
            for(int l=0; l<12; l++)
            {
                this.card.add(l, (l+1));
                if ((l+1) <= nb_versements)
                    this.isVersementOk.add((l+1));
            }
        }
        else if(this.periode.equals(PeriodiciteEnum.HEBDOMADAIRE.toString()))
        {
            for(int l=0; l<52; l++)
            {
                this.card.add(l, (l+1));
                if ((l+1) <= nb_versements)
                    this.isVersementOk.add((l+1));
            }
        }
        else if(this.periode.equals(PeriodiciteEnum.JOURNALIERE.toString()))
        {
            for(int l=0; l<31; l++)
            {
                this.card.add(l, (l+1));
                if ((l+1) <= nb_versements)
                    this.isVersementOk.add((l+1));

            }
        }
    }

    @Override
    public int getCount() {
        return gridViewString.size();
    }

    @Override
    public Object getItem(int i) {
        return null;
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }
    public void setDisabled(ImageView imageView) {

        final ColorMatrix grayscaleMatrix = new ColorMatrix();

        grayscaleMatrix.setSaturation(0);

        final ColorMatrixColorFilter filter = new ColorMatrixColorFilter(grayscaleMatrix);
        imageView.setColorFilter(filter);
    }
    @Override
    public View getView(int i, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        if (convertView == null) {
            convertView = layoutInflater.inflate(R.layout.carte_item, null);
            viewHolder = new ViewHolder();
            viewHolder.textViewAndroidHolder = (TextView) convertView.findViewById(R.id.num_versement);
            viewHolder.imageviewHolder = convertView.findViewById(R.id.versement_ok);
            convertView.setTag(viewHolder);
        }
        else
        {
            viewHolder = (ViewHolder)convertView.getTag();
        }
       try {
            if (this.isVersementOk.contains((int)this.card.get(i)))
            {
                viewHolder.imageviewHolder.setVisibility(View.VISIBLE);
            }
            else{
                viewHolder.imageviewHolder.setVisibility(View.GONE);
            }
        }catch (java.lang.IndexOutOfBoundsException e){

        }



        viewHolder.textViewAndroidHolder.setText(""+this.card.get(i));
        return convertView;
       /* View gridViewAndroid;
        LayoutInflater inflater = (LayoutInflater) mContext
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        if (convertView == null) {

            gridViewAndroid = new View(mContext);
            gridViewAndroid = inflater.inflate(R.layout.carte_item, null);
            TextView textViewAndroid = (TextView) gridViewAndroid.findViewById(R.id.num_versement);
            //Toast.makeText(mContext,"ok"+i,Toast.LENGTH_LONG).show();
            Log.e("test_grid_position", String.valueOf(gridViewString.size()));
           // textViewAndroid.setText(""+i+1);
            if (i< nb_versements){
                // si versement par ussd
                /*if (gridViewString.get(i).getUssd()==1){
                    //
                    //Toast.makeText(mContext,"ok"+gridViewString.get(i).getUssd(),Toast.LENGTH_LONG).show();
                    if ((gridViewString.get(i).getStatut_paiement().equals("") || gridViewString.get(i).getStatut_paiement().equals("null"))){
                        ImageView imageview = gridViewAndroid.findViewById(R.id.versement_pending);
                        imageview.setVisibility(View.VISIBLE);
                    }else{
                        ImageView imageview = gridViewAndroid.findViewById(R.id.versement_ok);
                        imageview.setVisibility(View.VISIBLE);
                    }

                }else{

                    ImageView imageview = gridViewAndroid.findViewById(R.id.versement_ok);
                    //imageview.setColorFilter(R.color.colorBlack);
                    //setDisabled(imageview);
                    imageview.setVisibility(View.VISIBLE);
                }*/
                /*ImageView imageview = gridViewAndroid.findViewById(R.id.versement_ok);
                //imageview.setColorFilter(R.color.colorBlack);
                //setDisabled(imageview);
                imageview.setVisibility(View.VISIBLE);


            }
            textViewAndroid.setText(""+card.get(i));

        } else {
            gridViewAndroid = (View) convertView;
        }*/

       // return gridViewAndroid;
    }

    static class ViewHolder {
        TextView textViewAndroidHolder;
        ImageView imageviewHolder;
    }
}