package com.sicmagroup.tondi;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;

import com.pixplicity.easyprefs.library.Prefs;

import static com.sicmagroup.tondi.Accueil.CGU_FR_KEY;

public class CGU extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // remove title
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_cgu);
        Intent intent = getIntent();
        String origine = "";
        if(intent.hasExtra("origine"))
            origine = intent.getStringExtra("origine");
        //TextView cgu_text = (TextView)findViewById(R.id.cgu_text);
        //justify(cgu_text);
        ImageView btn_fermer_cgu = findViewById(R.id.btn_fermer_cgu);
        btn_fermer_cgu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                CGU.this.finish();
            }
        });
        Button btn_cgu_lu = findViewById(R.id.btn_cgu_lu);
        btn_cgu_lu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub

                Prefs.putInt(CGU_FR_KEY,1);
                CGU.this.finish();
            }
        });


        if (!origine.equals(""))
        {
            btn_cgu_lu.setVisibility(View.GONE);
        }
        else
            btn_cgu_lu.setVisibility(View.VISIBLE);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent i = new Intent(CGU.this,Home.class);
        startActivity(i);
    }
}
