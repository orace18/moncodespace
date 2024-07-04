package com.sicmagroup.tondi;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class About_us extends AppCompatActivity {

    @SuppressLint("ResourceAsColor")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about_us_2);

        Button btn_cgu = findViewById(R.id.btn_cgu);
        btn_cgu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(About_us.this,CGU.class);
                i.putExtra("origine", "about_us");
                startActivity(i);
            }
        });

        Button btn_share = findViewById(R.id.btn_share);
        btn_share.setEnabled(true);
        btn_share.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent =new Intent();
                intent.setAction(Intent.ACTION_SEND);
                intent.putExtra(android.content.Intent.EXTRA_SUBJECT, "Android Studio Pro");

                intent.putExtra(Intent.EXTRA_TEXT,"Hey, moi aussi j'utilise cette application pour faire mes tontines et gérer mon épargne. Télécharge l'application COMUBA TONTINE DIGITALE et fait ta tontine. Clique sur ce lien pour aller dans playStore https://play.google.com/store/apps/details?id=com.sicmagroup.tondi");
                intent.setType("text/plain");
                startActivity(intent);
            }
        });

        Button back_to = findViewById(R.id.back_to);
        back_to.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                About_us.this.finish();
                startActivity(new Intent(About_us.this, Home.class));
            }
            });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent i = new Intent(About_us.this,Home.class);
        startActivity(i);
    }
}