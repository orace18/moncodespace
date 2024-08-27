package com.sicmagroup.tondi;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.sicmagroup.tondi.utils.Constantes;

public class Message_ok extends AppCompatActivity {
    Button btn_fermer_cgu;
    @SuppressLint("LongLogTag")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // remove title
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.message_ok);

        Intent getIntent = getIntent();
        String msg_desc = getIntent.getStringExtra("msg_desc");
        final Long id_tontine = getIntent.getLongExtra("id_tontine",0);
        final Long id_retrait = getIntent.getLongExtra("id_retrait",0);
        Log.e("message_ok","id_retrait"+id_retrait);

        //ajout
        final boolean isNewTontine = getIntent.getBooleanExtra("isNewTontine",false);
        Log.e("isNewTontine2_message_ok",""+isNewTontine);
        //ajout fin

        String str_class = "";
        str_class = getIntent.getStringExtra("class");
        //Toast.makeText(getApplicationContext(),"id:"+id_retrait,Toast.LENGTH_LONG).show();
        TextView msg_desc_text = findViewById(R.id.msg_desc);
        msg_desc_text.setText(msg_desc);
        //justify(cgu_text);
        ImageView i = findViewById(R.id.msg_check);
        i.setColorFilter(getResources().getColor(R.color.fbutton_color_green_sea));
        btn_fermer_cgu = findViewById(R.id.btn_oui);
        final String finalStr_class = str_class;
        btn_fermer_cgu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                Message_ok.this.finish();
                if (id_tontine!=0){
                    carte((long) id_tontine,(boolean) isNewTontine);
                }
                if(id_retrait!=0){
                    Intent intent = new Intent(Message_ok.this,Encaisser.class);
                    intent.putExtra("id_retrait",id_retrait);
                    startActivity(intent);
                }

                if(finalStr_class!=null){
                    Intent intent = new Intent(getApplicationContext(), Home.class);
                    switch (finalStr_class){
                        case "com.sicmagroup.tondi.MesTontines":
                            intent.putExtra(Constantes.BOTTOM_NAV_DESTINATION, Constantes.DESTINATION_TONTINES);
                            startActivity(intent);
                            break;
                        case "com.sicmagroup.tondi.MesRetraits":
                            intent.putExtra(Constantes.BOTTOM_NAV_DESTINATION, Constantes.DESTINATION_RETRAITS);
                            startActivity(intent);
                            break;
                        default:
                            startActivity(intent);
                            break;
                    }
//                    Intent i = new Intent();
//                    i.setClassName(getApplicationContext(), finalStr_class);
//                    startActivity(i);
                }

                /*try {
                    startActivity(new Intent(Message_ok.this, (Class<?>) Class.forName(str_class).newInstance()));
                } catch (IllegalAccessException | InstantiationException | ClassNotFoundException e) {
                    e.printStackTrace();
                }*/
            }
        });



    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
//        Message_ok.this.finish();
//        startActivity(new Intent(Message_ok.this, MesTontines.class));
        btn_fermer_cgu.performClick();
    }

    private void carte(Long id_tontine, boolean isNewTontine){
        //mContext.startActivity(new Intent(mContext,Carte.class));
        //Toast.makeText(mContext,"id_tontineeee="+Integer.parseInt(String.valueOf(id_tontine)),Toast.LENGTH_LONG).show();
        Log.d("id_tontineeee",""+Integer.parseInt(String.valueOf(id_tontine)));
        Intent intent = new Intent(new Intent(Message_ok.this,CarteMain.class));
        intent.putExtra("id_tontine",Integer.parseInt(String.valueOf(id_tontine)));
        intent.putExtra("isNewTontine",isNewTontine);
        Message_ok.this.startActivity(intent);
    }

}
