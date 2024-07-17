package com.sicmagroup.tondi;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import androidx.annotation.NonNull;

import com.android.volley.AuthFailureError;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.pixplicity.easyprefs.library.Prefs;
import com.romellfudi.permission.PermissionService;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import androidmads.library.qrgenearator.QRGEncoder;
import com.sicmagroup.formmaster.FormBuilder;
import com.sicmagroup.formmaster.model.BaseFormElement;
import com.sicmagroup.formmaster.model.FormElementTextNumber;

import static com.sicmagroup.tondi.Connexion.ID_UTILISATEUR_KEY;
import static com.sicmagroup.tondi.utils.Constantes.SERVEUR;
import static com.sicmagroup.tondi.utils.Constantes.accessToken;

public class Compte_Marchand extends AppCompatActivity {
    GridView versements_grid;
    List<Versement> versement_list =new ArrayList<Versement>();
    //String url_afficher = "http://"+SERVEUR+"/api/v1/tontines/afficher_carte";
    String url_afficher_token = SERVEUR+"/api/v1/retraits/afficher_token";
    int id_tontine;
    int mise;

    private static final int TAG_MONTANT = 11;
    RecyclerView mRecyclerView;
    FormBuilder frm_versement = null;
    String TAG = "GenerateQRCode";
    EditText edtValue;
    TextView mText;
    ImageView qrImage;
    Button start, save;
    String inputValue;
    String savePath = Environment.getExternalStorageDirectory().getPath() + "/QRCode/";
    Bitmap bitmap;
    QRGEncoder qrgEncoder;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Toast.makeText(getApplicationContext(),"id_tontine="+id_tontine,Toast.LENGTH_LONG).show();
        // remove title
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_compte_marchand);

        FloatingActionButton btn_validation = findViewById(R.id.btn_nouvelle_validation);
        btn_validation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                startActivity(new Intent(Compte_Marchand.this,Encaisser_Marchand.class));

            }
        });

        ImageView back_to = (ImageView)findViewById(R.id.back_to);
        back_to.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                Compte_Marchand.this.finish();
                startActivity(new Intent(Compte_Marchand.this,Dashboard.class));
            }
        });

        Button btn_deconnexion = findViewById(R.id.btn_deconnexion);
        btn_deconnexion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                alertView("Déconnexion", "Êtes vous sûr de vouloir vous déconnecter?");

            }
        });

    }

    private void alertView( String title ,String message ) {
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle( title )
                .setIcon(R.drawable.ic_warning)
                .setMessage(message)
                .setNegativeButton("Non", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialoginterface, int i) {
                        dialoginterface.cancel();
                    }})
                .setPositiveButton("Oui", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialoginterface, int i) {
                        Prefs.putString(ID_UTILISATEUR_KEY,null);
                        Compte_Marchand.this.finish();
                        startActivity(new Intent(Compte_Marchand.this,Connexion.class));
                    }
                }).show();
    }

    void setupFrmVersement(Dialog d, Context c){
        mRecyclerView =  d.findViewById(R.id.form_versement);
        frm_versement = new FormBuilder(c, mRecyclerView);

        FormElementTextNumber element6 = FormElementTextNumber.createInstance().setTag(TAG_MONTANT).setTitle("").setRequired(true);
        List<BaseFormElement> formItems = new ArrayList<>();
        formItems.add(element6);

        frm_versement.addFormElements(formItems);
    }
    private void afficher_token(final int id_tontine) {
        RequestQueue queue = Volley.newRequestQueue(Compte_Marchand.this);
        StringRequest postRequest = new StringRequest(Request.Method.POST, url_afficher_token,
                new Response.Listener<String>()
                {
                    @SuppressLint("ResourceAsColor")
                    @Override
                    public void onResponse(String response) {
                        // response
                        Log.d("Response", response);
                        //Toast.makeText(getApplicationContext(),String.valueOf(Integer.parseInt(montant)-(Integer.parseInt(montant)%mise)),Toast.LENGTH_LONG).show();

                        try {

                            JSONObject result = new JSONObject(response);
                            JSONArray array = result.getJSONArray("data");

                            //Log.d("My App", obj.toString());
                            if (result.getBoolean("success")){
                                // Mettre à jour la préférence id tontine
                                //Prefs.putString(ID_TONTINE_USSD, String.valueOf(id_tontine));
                                // Mettre à jour la préférence id tontine
                                //Prefs.putString(MONTANT_VERSE, String.valueOf(Integer.parseInt(montant)-(Integer.parseInt(montant)%mise)));
                                //alertView("souscription ok","ok");
                                TextView token = findViewById(R.id.token);
                                token.setText(array.getJSONObject(0).getString("token"));

                            }else{
                                /*Alerter.create(Carte.this)
                                        .setTitle(result.getString("message"))
                                        .setIcon(R.drawable.ic_warning)
                                        .setTitleAppearance(R.style.TextAppearance_AppCompat_Large)
                                        .setIconColorFilter(R.color.colorPrimaryDark)
                                        //.setText("Vous pouvez maintenant vous connecter.")
                                        .setBackgroundColorRes(R.color.colorWhite) // or setBackgroundColorInt(Color.CYAN)
                                        .show();*/
                            }


                        } catch (Throwable t) {
                            //Log.e("My App", "Could not parse malformed JSON: \"" + response + "\"");
                        }
                    }
                },
                new Response.ErrorListener()
                {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // error
                        //Log.d("Error.NouvelleTontine", error.getMessage());
                    }
                }
        ) {
            @Override
            protected Map<String, String> getParams()
            {

                Map<String, String>  params = new HashMap<String, String>();
                params.put("idTontine", String.valueOf(id_tontine));

                return params;
            }
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                headers.put("Content-Type", "application/json");
                headers.put("Authorization", "Bearer " + accessToken); // Ajoute le token ici
                return headers;
            }
        };
        queue.add(postRequest);
    }



    private PermissionService.Callback callback = new PermissionService.Callback() {
        @Override
        public void onRefuse(ArrayList<String> RefusePermissions) {
            Toast.makeText(Compte_Marchand.this,
                    getString(R.string.refuse_permissions),
                    Toast.LENGTH_SHORT).show();
            Compte_Marchand.this.finish();
        }

        @Override
        public void onFinally() {
            // pass
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (!Settings.canDrawOverlays(Compte_Marchand.this)) {
                    Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                            Uri.parse("package:" + Compte_Marchand.this.getPackageName()));
                    startActivity(intent);
                }
            }
        }
    };

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        callback.handler(permissions, grantResults);
    }
}
