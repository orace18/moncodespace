package com.sicmagroup.tondi;


import static com.sicmagroup.tondi.Connexion.ID_UTILISATEUR_KEY;
import static com.sicmagroup.tondi.Connexion.NMBR_PWD_FAILED;
import static com.sicmagroup.tondi.Connexion.NMBR_PWD_TENTATIVE_FAILED;
import static com.sicmagroup.tondi.Connexion.PASS_KEY;
import static com.sicmagroup.tondi.Connexion.TEL_KEY;
import static com.sicmagroup.tondi.Connexion.url_desactiver_account;
import static com.sicmagroup.tondi.utils.Constantes.REFRESH_TOKEN;
import static com.sicmagroup.tondi.utils.Constantes.SERVEUR;
import static com.sicmagroup.tondi.utils.Constantes.TOKEN;
import static com.sicmagroup.tondi.utils.Constantes.url_refresh_token;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkError;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.ServerError;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.pixplicity.easyprefs.library.Prefs;
import com.sicmagroup.formmaster.FormBuilder;
import com.sicmagroup.formmaster.model.BaseFormElement;
import com.sicmagroup.formmaster.model.FormElementTextPassword;
import com.sicmagroup.tondi.utils.Constantes;
import com.tapadoo.alerter.Alerter;
import com.tapadoo.alerter.OnHideAlertListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Created by Ravi Tamada on 18/05/16.
 */
public class RetraitAdapter extends RecyclerView.Adapter<RetraitAdapter.MyViewHolder> {

    private Context mContext;
    private List<Retrait> retraitsList;

    String url_terminer = SERVEUR+"/api/v1/tontines/terminer";
    String url_init_retrait = SERVEUR+"/api/v1/withdraw/init";
    int id_tontine;
    private RadioGroup radioGroup;
    private RadioButton radioButton;
    RecyclerView mRecyclerView;
    FormBuilder frm_pin_acces = null;
    private static final int TAG_PIN = 11;

    public String accessToken = Prefs.getString(TOKEN,"");


    public class MyViewHolder extends RecyclerView.ViewHolder  implements View.OnClickListener {
        public TextView creation, titre, versements, montant;
        public ImageView thumbnail, overflow,
                icon_creation;

        public MyViewHolder(View view) {
            super(view);
            creation = (TextView) view.findViewById(R.id.creation);
            titre = (TextView) view.findViewById(R.id.titre);
            versements = (TextView) view.findViewById(R.id.nb_versements);
            montant = (TextView) view.findViewById(R.id.mt_tontine);
            icon_creation = (ImageView) view.findViewById(R.id.icon_creation);
            //overflow = (ImageView) view.findViewById(R.id.overflow);
            view.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if(Prefs.contains(NMBR_PWD_TENTATIVE_FAILED)){
                if(Prefs.getInt(NMBR_PWD_TENTATIVE_FAILED, 0) >= 3)
                {
                    desactiver_account(mContext);
                } else {
                    // on item click
                    Intent intent = new Intent(mContext,Encaisser.class);
                    //Toast.makeText(mContext, "pos:"+getAdapterPosition()+"//"+retraitsList.get(getAdapterPosition()).getToken(), Toast.LENGTH_SHORT).show();
                    intent.putExtra("id_retrait",retraitsList.get(getAdapterPosition()).getId());
                    ViewDialog1 dialog = new ViewDialog1();
                    dialog.showDialog(((Activity) mContext), intent, mContext);
//                    mContext.startActivity(intent);
                }
            } else {
                Intent intent = new Intent(mContext,Encaisser.class);
                //Toast.makeText(mContext, "pos:"+getAdapterPosition()+"//"+retraitsList.get(getAdapterPosition()).getToken(), Toast.LENGTH_SHORT).show();
                intent.putExtra("id_retrait",retraitsList.get(getAdapterPosition()).getId());
                ViewDialog1 dialog = new ViewDialog1();
                dialog.showDialog(((Activity) mContext), intent, mContext);
//                mContext.startActivity(intent);
            }

        }
    }
    public void desactiver_account(Context context) {
        sendDesactiverAccountRequest(context);
    }

    @SuppressLint("LongLogTag")
    private void sendDesactiverAccountRequest(Context context) {
        RequestQueue queue = Volley.newRequestQueue(context);
        JSONObject params = new JSONObject();
        try {
            params.put("customerNumber", Prefs.getString(TEL_KEY, null));
            Log.e("Le body de sendDesactiverAccountRequest" ,params.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        JsonObjectRequest postRequest = new JsonObjectRequest(Request.Method.POST, url_desactiver_account, params,
                new Response.Listener<JSONObject>() {
                    @SuppressLint("ResourceAsColor")
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            if (response.getBoolean("success")) {
                                Prefs.putString(ID_UTILISATEUR_KEY, null);
                                Prefs.putInt(NMBR_PWD_FAILED, 0);
                                Prefs.putInt(NMBR_PWD_TENTATIVE_FAILED, 0);
                                String msg = "Vos identifiants sont incorrects. Votre compte a été désactivé, Merci de contacter le xxxxxxxx et suivez les instructions de réactivation de compte, Merci.";
                                Intent i = new Intent(context, Message_non.class);
                                i.putExtra("msg_desc", msg);
                                i.putExtra("class", "com.sicmagroup.tondi.Connexion");
                                ((Activity) context).finish();
                                context.startActivity(i);
                            } else {
                                Prefs.putString(ID_UTILISATEUR_KEY, null);
                                Prefs.putInt(NMBR_PWD_FAILED, 0);
                                Prefs.putInt(NMBR_PWD_TENTATIVE_FAILED, 0);
                                String msg = "Vos identifiants sont incorrects. Veuillez réessayer SVP!";
                                Intent i = new Intent(context, Message_non.class);
                                i.putExtra("msg_desc", msg);
                                i.putExtra("class", "com.sicmagroup.tondi.Connexion");
                                ((Activity) context).finish();
                                context.startActivity(i);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {
                        if (volleyError.networkResponse != null && volleyError.networkResponse.statusCode == 401) {
                            refreshAccessToken(context, new TokenRefreshListener() {
                                @Override
                                public void onTokenRefreshed(boolean success) {
                                    if (success) {
                                        sendDesactiverAccountRequest(context);
                                    }
                                }
                            });
                        } else {
                            Log.e("", "");
                            Log.e("ResponseTagMain", String.valueOf(volleyError.getMessage()));

                            Log.e("Stack", "Error StackTrace: \t" + Arrays.toString(volleyError.getStackTrace()));
                        }
                    }
                }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                headers.put("Content-Type", "application/json");
                headers.put("Authorization", "Bearer " + Prefs.getString(TOKEN, ""));
                return headers;
            }
        };

        queue.add(postRequest);
    }

    private void refreshAccessToken(Context context, TokenRefreshListener listener) {
        RequestQueue queue = Volley.newRequestQueue(context);
        JSONObject params = new JSONObject();
        try {
            params.put("refreshToken", Prefs.getString(REFRESH_TOKEN, ""));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        JsonObjectRequest refreshRequest = new JsonObjectRequest(Request.Method.POST, url_refresh_token, params,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            String newAccessToken = response.getString("token");
                            String newRefreshToken = response.getString("refreshToken");
                            Prefs.putString(TOKEN, newAccessToken);
                            Prefs.putString(REFRESH_TOKEN, newRefreshToken);
                            Prefs.putString(TOKEN, newAccessToken);
                            listener.onTokenRefreshed(true);
                        } catch (JSONException e) {
                            e.printStackTrace();
                            listener.onTokenRefreshed(false);
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {
                        Log.e("RefreshToken", "Error: " + volleyError.getMessage());
                        listener.onTokenRefreshed(false);
                    }
                });

        queue.add(refreshRequest);
    }

    private interface TokenRefreshListener {
        void onTokenRefreshed(boolean success);
    }

    void setupFrmPin(Dialog d, Context c){
        mRecyclerView =  d.findViewById(R.id.form_pin_access);
        frm_pin_acces = new FormBuilder(c, mRecyclerView);
        FormElementTextPassword element6 = FormElementTextPassword.createInstance().setTag(TAG_PIN).setTitle("").setRequired(true);
        List<BaseFormElement> formItems = new ArrayList<>();
        formItems.add(element6);
        frm_pin_acces.addFormElements(formItems);
    }

    public class ViewDialog1 {
        void showDialog(Activity activity, final Intent intent, Context context){
            final Dialog dialog = new Dialog(activity);
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.setCancelable(true);
            dialog.setContentView(R.layout.pin_access_2);
            setupFrmPin(dialog,dialog.getContext());
            TextView msg_try_mdp = dialog.findViewById(R.id.msg_pin_trying);
            if(Prefs.contains(NMBR_PWD_TENTATIVE_FAILED))
            {
                if(Prefs.getInt(NMBR_PWD_TENTATIVE_FAILED, 0) == 0)
                {
                    msg_try_mdp.setVisibility(View.GONE);
                }
                else
                {
                    int tentative_restant =  10 - Prefs.getInt(NMBR_PWD_TENTATIVE_FAILED, 0);
                    msg_try_mdp.setVisibility(View.VISIBLE);
                    msg_try_mdp.setText("Il vous reste "+String.valueOf(tentative_restant)+" tentative(s)");
                }

            }
            Button dialogButton = (Button) dialog.findViewById(R.id.btn_continue);
            dialogButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    BaseFormElement pin = frm_pin_acces.getFormElement(TAG_PIN);
                    String pin_value = pin.getValue();
                    pin_verifier(intent,pin_value, context);
                    dialog.dismiss();
                }
            });
            //Toast.makeText(getApplicationContext(),"e",Toast.LENGTH_LONG).show();
            dialog.show();
        }
    }

    private void pin_verifier(Intent intent, String pin, Context context) {
        Connexion.AeSimpleSHA1 AeSimpleSHA1 = new Connexion.AeSimpleSHA1();
        Log.e("pin", Prefs.getString(PASS_KEY,""));
        try {
            pin = AeSimpleSHA1.md5(pin);
            pin = AeSimpleSHA1.SHA1(pin);

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        if (pin.equals(Prefs.getString(PASS_KEY,""))){
            if(Prefs.contains(NMBR_PWD_TENTATIVE_FAILED))
            {
                Prefs.putInt(NMBR_PWD_TENTATIVE_FAILED, 0);
            }
            context.startActivity(intent);
        }else{
            if(Prefs.contains(NMBR_PWD_TENTATIVE_FAILED))
            {
                int tentative = Prefs.getInt(NMBR_PWD_TENTATIVE_FAILED, 0);
                Prefs.putInt(NMBR_PWD_TENTATIVE_FAILED, tentative + 1);
            }
            else
            {
                Prefs.putInt(NMBR_PWD_TENTATIVE_FAILED, 1);
            }

            String msg="Votre mot de passe est erroné! Veuillez réessayer SVP!";
            Intent i = new Intent(context, Message_non.class);
            i.putExtra("msg_desc",msg);
            i.putExtra("class","com.sicmagroup.tondi.MesRetraits");
            context.startActivity(i);
        }
    }



    public RetraitAdapter(Context mContext, List<Retrait> retraitsList) {
        this.mContext = mContext;
        this.retraitsList = retraitsList;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.retrait_row, parent, false);

        //itemView.setTag("cycy");
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, int position) {
        Retrait retrait = retraitsList.get(position);

//        @SuppressLint("DefaultLocale") String versements = String.format("%02d", retrait.getTontine().getVersements());
        Calendar cal = Calendar.getInstance(Locale.FRENCH);
        cal.setTimeInMillis(retrait.getCreation());
        String date = DateFormat.format("yyyy-MM-dd à HH:mm", cal).toString();
        holder.creation.setText("Généré le "+date);
        holder.titre.setText("Code de retrait pour la tontine "+retrait.getTontine().getDenomination().toLowerCase()+" de "+retrait.getTontine().getMise()+" F CFA");


        holder.icon_creation.setColorFilter(mContext.getResources().getColor(R.color.colorPrimaryDark));
        //holder.montant.setText(retrait.getTontine().getMontEncaisse()+" F");
        holder.montant.setText(retrait.getMontant()+" F CFA");
        // loading album cover using Glide library
        //Glide.with(mContext).load(album.getThumbnail()).into(holder.thumbnail);

    }



    @Override
    public int getItemCount() {
        return retraitsList.size();
    }

    void carte(int id_tontine){
        //mContext.startActivity(new Intent(mContext,Carte.class));
        Intent intent = new Intent(new Intent(mContext,CarteMain.class));
        intent.putExtra("id_tontine",id_tontine);
        mContext.startActivity(intent);
    }



    public class ViewDialog {

        void showDialog(Context activity){
            final Dialog dialog = new Dialog(activity);
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.setCancelable(true);
            dialog.setContentView(R.layout.retrait);


            Button dialogButton = (Button) dialog.findViewById(R.id.btn_continue_retrait);
            dialogButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    radioGroup = (RadioGroup) dialog.findViewById(R.id.radio_retrait);

                    // get selected radio button from radioGroup
                    int selectedId = radioGroup.getCheckedRadioButtonId();

                    // find the radiobutton by returned id
                    radioButton = (RadioButton) dialog.findViewById(selectedId);

                    //Toast.makeText(mContext,
                      //      radioButton.getText(), Toast.LENGTH_SHORT).show();
                    /*BaseFormElement pin = frm_pin_acces.getFormElement(TAG_PIN);

                    String pin_value = pin.getValue();
                    if (pin_value.equals(Prefs.getString(PIN_KEY,""))){
                        startActivity(intent);
                    }*/
                    if (radioButton.getTag().equals("1")){
                        confirmer_retrait("Retraits des montants cotisés vers MobileMoney","Confirmez-vous ce retrait?",1);
                    }
                    if (radioButton.getTag().equals("2")){
                        confirmer_retrait("Retraits des montants cotisés en espèces","Confirmez-vous ce retrait?",2);
                    }


                    dialog.dismiss();

                }
            });
            //Toast.makeText(getApplicationContext(),"e",Toast.LENGTH_LONG).show();

            dialog.show();

        }
    }

    private void confirmer_retrait(String title, String message, final int mode) {
        AlertDialog.Builder dialog = new AlertDialog.Builder(mContext);
        dialog.setTitle( title )
                .setIcon(R.drawable.ic_warning)
                .setMessage(message)
                .setNegativeButton("Non", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialoginterface, int i) {
                        dialoginterface.cancel();
                    }})
                .setPositiveButton("Oui", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialoginterface, int i) {
                        if (mode==1){
                            //terminer(id_tontine);
                            Alerter.create((MesTontines) mContext)
                                    .setTitle("Montant de XXX F encaissé")
                                    .setIcon(R.drawable.ic_check)
                                    .setBackgroundColorRes(R.color.colorPrimaryDark) // or setBackgroundColorInt(Color.CYAN)
                                    .setOnHideListener(new OnHideAlertListener() {
                                        @Override
                                        public void onHide() {
                                            ((MesTontines) mContext).finish();
                                            mContext.startActivity(new Intent(mContext,MesTontines.class));

                                        }
                                    })
                                    .show();
                        }

                        if (mode==2){
                            //Toast.makeText(mContext, "id:"+id_tontine, Toast.LENGTH_SHORT).show();
                            requete_retrait(id_tontine);

                        }

                    }
                }).show();
    }

    private void alertView(String title , String message, final int id_tontine ) {
        AlertDialog.Builder dialog = new AlertDialog.Builder(mContext);
        dialog.setTitle( title )
                .setIcon(R.drawable.ic_warning)
                .setMessage(message)
                .setNegativeButton("Non", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialoginterface, int i) {
                        dialoginterface.cancel();
                    }})
                .setPositiveButton("Oui", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialoginterface, int i) {
                       terminer(id_tontine);

                    }
                }).show();
    }

    private void terminer(final int id_tontine) {
        RequestQueue queue = Volley.newRequestQueue(mContext);
        StringRequest postRequest = new StringRequest(Request.Method.POST, Constantes.URL_COMPLETED_TONTINE,
                new Response.Listener<String>()
                {
                    @SuppressLint("ResourceAsColor")
                    @Override
                    public void onResponse(String response) {
                        // response
                        Log.d("Response", response);

                        try {

                            JSONObject result = new JSONObject(response);

                            //Log.d("My App", obj.toString());
                            if (result.getBoolean("success")){
                                //alertView("souscription ok","ok");
                                Alerter.create((MesTontines) mContext)
                                        .setTitle(result.getString("message"))
                                        .setIcon(R.drawable.ic_check)
                                        .setBackgroundColorRes(R.color.colorPrimaryDark) // or setBackgroundColorInt(Color.CYAN)
                                        .setOnHideListener(new OnHideAlertListener() {
                                            @Override
                                            public void onHide() {
                                                ((MesTontines) mContext).finish();
                                                mContext.startActivity(new Intent(mContext,MesTontines.class));
                                            }
                                        })
                                        .show();
                            }else{
                                Alerter.create((MesTontines) mContext)
                                        .setTitle(result.getString("message"))
                                        .setIcon(R.drawable.ic_warning)
                                        .setTitleAppearance(R.style.TextAppearance_AppCompat_Large)
                                        .setIconColorFilter(R.color.colorPrimaryDark)
                                        //.setText("Vous pouvez maintenant vous connecter.")
                                        .setBackgroundColorRes(R.color.colorWhite) // or setBackgroundColorInt(Color.CYAN)
                                        .show();
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
                params.put("customerNumber", Prefs.getString(TEL_KEY, ""));
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


    private void requete_retrait(final int id_tontine) {
        RequestQueue queue = Volley.newRequestQueue(mContext);
        StringRequest postRequest = new StringRequest(Request.Method.POST, url_init_retrait,
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

                            //Log.d("My App", obj.toString());
                            if (result.getBoolean("success")){
                                // Mettre à jour la préférence id tontine
                                //Prefs.putString(ID_TONTINE_USSD, String.valueOf(id_tontine));
                                // Mettre à jour la préférence id tontine
                                //Prefs.putString(MONTANT_VERSE, String.valueOf(Integer.parseInt(montant)-(Integer.parseInt(montant)%mise)));
                                //alertView("souscription ok","ok");

                                Alerter.create((MesTontines) mContext)
                                        .setTitle(result.getString("message"))
                                        .setIcon(R.drawable.ic_check)
                                        .setBackgroundColorRes(R.color.colorPrimaryDark) // or setBackgroundColorInt(Color.CYAN)
                                        .setOnHideListener(new OnHideAlertListener() {
                                            @Override
                                            public void onHide() {
                                                Intent intent = new Intent(mContext,Encaisser.class);
                                                intent.putExtra("id_tontine",id_tontine);
                                                mContext.startActivity(intent);
                                            }
                                        })
                                        .show();
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
                        boolean tokenRefreshed = false;

                        if (error instanceof AuthFailureError && !tokenRefreshed) {
                            // Rafraîchir le token et réessayer
                            refreshAccessToken(mContext, new RetraitAdapter.TokenRefreshListener() {
                                @Override
                                public void onTokenRefreshed(boolean success) {
                                    if (success) {
                                        requete_retrait(id_tontine);
                                    }
                                }
                            });
                        }
                    }
                }
        ) {
            @Override
            protected Map<String, String> getParams()
            {

                Map<String, String>  params = new HashMap<String, String>();
                params.put("idTontine", String.valueOf(id_tontine));
                params.put("customerNumber", Prefs.getString(TEL_KEY, ""));

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
}
