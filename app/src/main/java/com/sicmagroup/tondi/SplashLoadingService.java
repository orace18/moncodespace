package com.sicmagroup.tondi;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.view.Gravity;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.pixplicity.easyprefs.library.Prefs;
import com.sicmagroup.ussdlibra.GifImageView;

import static com.sicmagroup.tondi.Connexion.ACCESS_BOOL;
import static com.sicmagroup.tondi.Connexion.SPLASH_LOADING;

/**
 * SplashLoadingService for Android splashing dialog
 *
 * @author Romell Dominguez
 * @version 1.1.d 23/02/2017
 * @since 1.1.d
 */
public class SplashLoadingService extends Service {

    private LinearLayout layout;
    private WindowManager wm;
    public static final String CHANNEL_ID = "ForegroundServiceChannel";

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @SuppressLint("ResourceAsColor")
    public int onStartCommand(Intent intent, int flags, int startId) {

        wm = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        Point size = new Point();
        wm.getDefaultDisplay().getSize(size);
        int LAYOUT_FLAG;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            LAYOUT_FLAG = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        } else {
            LAYOUT_FLAG = WindowManager.LayoutParams.TYPE_PHONE;
        }

        int padding_in_dp = 100;
        final float scale = getResources().getDisplayMetrics().density;
        int padding_in_px = (int) (padding_in_dp * scale + 0.5f);

        layout = new LinearLayout(this);
        layout.setBackgroundColor(getResources().getColor(com.sicmagroup.ussdlibra.R.color.white) );

        layout.setOrientation(LinearLayout.VERTICAL);

        WindowManager.LayoutParams params =
                new WindowManager.LayoutParams
                        (WindowManager.LayoutParams.MATCH_PARENT,
                                WindowManager.LayoutParams.MATCH_PARENT,
                                LAYOUT_FLAG
                                , WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE |
                                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
                                PixelFormat.RGB_565);

        ImageView imageView = new ImageView(this);
        imageView.setImageResource(com.sicmagroup.ussdlibra.R.drawable.tondi);
        imageView.setPaddingRelative(150,padding_in_px,150,0);
        LinearLayout.LayoutParams params_ll = new LinearLayout
                .LayoutParams(LinearLayout.MarginLayoutParams.MATCH_PARENT, 50);
        params_ll.gravity = Gravity.CENTER;
        params_ll.weight = 1;
        params_ll.topMargin=0;

        RelativeLayout relativeLayout = new RelativeLayout(this);
        RelativeLayout.LayoutParams rp = new RelativeLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);
        rp.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);
        relativeLayout.addView(imageView,rp);
        layout.addView(relativeLayout, params_ll);
        LinearLayout.LayoutParams params_l2 = new LinearLayout
                .LayoutParams(LinearLayout.MarginLayoutParams.MATCH_PARENT, 0);
        params_l2.gravity = Gravity.CENTER|Gravity.TOP;
        params_l2.weight = 1;
        params_l2.bottomMargin=0;
        TextView textView = new TextView(this);
        textView.setTextSize(16
        ); // set your text size = 20sp
        textView.setGravity(Gravity.CENTER);

        //textView.setPadding(0, 200, 0, 200);
        textView.setText(intent.getStringExtra("texte"));
        textView.setPaddingRelative(150,0,150,0);
        layout.addView(textView,params_l2);

        /**/GifImageView gifImageView = new GifImageView(this);
        gifImageView.setGifImageResource(com.sicmagroup.ussdlibra.R.drawable.loading);
        gifImageView.setPaddingRelative(0,padding_in_px,0,padding_in_px);

        relativeLayout = new RelativeLayout(this);
        rp = new RelativeLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);
        rp.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);
        relativeLayout.addView(gifImageView,rp);

        layout.addView(relativeLayout,params_ll);


        new Prefs.Builder()
                .setContext(SplashLoadingService.this)
                .setMode(ContextWrapper.MODE_PRIVATE)
                .setPrefsName(getPackageName())
                .setUseDefaultSharedPreference(true)
                .build();

        Prefs.putBoolean(ACCESS_BOOL,true);
        Prefs.putBoolean(SPLASH_LOADING,true);

        wm.addView(layout,params);
        return START_STICKY;
    }


    @Override
    public void onCreate() {
        super.onCreate();
        //Activity a = (Activity) getApplicationContext();
        //a.setContentView(R.layout.activity_connexion);

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopSelf();

        Prefs.putBoolean(SPLASH_LOADING,false);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (layout != null) {
                    wm.removeView(layout);
                    layout = null;
                }
            }
        },500);
    }
}