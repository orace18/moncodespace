package com.sicmagroup.tondi;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class CustomGrid extends BaseAdapter {

    private Context mContext;
    private final String[] gridViewString;
    private final int[] gridViewImageId;

    public CustomGrid(Context context, String[] gridViewString, int[] gridViewImageId) {
        mContext = context;
        this.gridViewImageId = gridViewImageId;
        this.gridViewString = gridViewString;
    }

    @Override
    public int getCount() {
        return gridViewString.length;
    }

    @Override
    public Object getItem(int i) {
        return null;
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(int i, View convertView, ViewGroup parent) {
        View gridViewAndroid;
        LayoutInflater inflater = (LayoutInflater) mContext
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        //gridViewAndroid=null;
        if (convertView == null) {

            gridViewAndroid = new View(mContext);
            gridViewAndroid = inflater.inflate(R.layout.service_item, null);
            TextView textViewAndroid = (TextView) gridViewAndroid.findViewById(R.id.service_title);
            ImageView imageViewAndroid = (ImageView) gridViewAndroid.findViewById(R.id.service_img);
            textViewAndroid.setText(gridViewString[i]);
            imageViewAndroid.setImageResource(gridViewImageId[i]);
        } else {
            gridViewAndroid = (View) convertView;
        }

        return gridViewAndroid;
    }
}