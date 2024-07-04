package com.sicmagroup.tondi;

import android.content.Context;
import android.util.AttributeSet;
import android.util.DisplayMetrics;

import androidx.viewpager.widget.ViewPager;

public class WrapContentHeightViewPager extends ViewPager {

    public WrapContentHeightViewPager(Context context) {
        super(context);

    }

    public WrapContentHeightViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
//        ProgressDialog progressDialog = new ProgressDialog(this.getContext());
//        progressDialog.setCancelable(false);
//        progressDialog.setMessage("Chargement en cours...");
//        progressDialog.show();
//        int height = 0;
//        if(getChildCount() <= 10){
//            height = 720;
//            Log.e("childCount", ""+getChildCount());
//        }
//        else{
//            for(int i = 0; i < getChildCount(); i++) {
//                View child = getChildAt(i);
//                child.measure(widthMeasureSpec, MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));
//                int h = child.getMeasuredHeight();
//                if(h > height) height = h;
//            }
//
//        }
        DisplayMetrics displayMetrics = getContext().getResources().getDisplayMetrics();
//        int pxWidth = displayMetrics.widthPixels;
//        float dpWidth = pxWidth / displayMetrics.density;
        int pxHeight = displayMetrics.heightPixels;
//        float dpHeight = pxHeight / displayMetrics.density;

//        Log.e("childCount", ""+pxHeight);
        heightMeasureSpec = MeasureSpec.makeMeasureSpec(pxHeight, MeasureSpec.EXACTLY);
//        progressDialog.dismiss();
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

}
