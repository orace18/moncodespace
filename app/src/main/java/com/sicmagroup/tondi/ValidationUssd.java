package com.sicmagroup.tondi;

import static com.sicmagroup.tondi.utils.Constantes.SERVEUR;
import static com.sicmagroup.tondi.utils.Constantes.accessToken;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;



public class ValidationUssd extends AsyncTask<Integer, Void, String> {

    //private final TextView textView;
    Context mcontext;
    String montant_t;
    String msg;
    String url_valider = SERVEUR+"/api/v1/versements/valider_ussd";

    private String numero_t;
    public ValidationUssd(String msg, Context context) {
        mcontext = context;
        this.msg = msg;
    }

    @Override
    protected String doInBackground(Integer... params) {
        traiterSms(msg);
        int seconds = params[0];
        try { Thread.sleep(2000); }
        catch (InterruptedException e) { e.printStackTrace(); }

        return "OK";
    }

    @Override
    protected void onPostExecute(String result) {
        //textView.setText(result);
    }

    private void traiterSms(String msg){
        Pattern p_mt = Pattern.compile("^(Transferteffectuepour)\\d+");
        Matcher m = p_mt.matcher(msg);

        // if our pattern matches the string, we can try to extract our groups
        if (m.find())
        {
            String group1 = m.group(0);
            Pattern p_montant = Pattern.compile("\\d+");
            Matcher match_mt = p_montant.matcher(group1);
            // get the two groups we were looking for
            if (match_mt.find()) {
                String montant = match_mt.group(0);
                montant_t = montant;
                //Log.d("match_mt",montant);
            }
        }

        // recupeper id transaction ussd
        Pattern p_id = Pattern.compile("(IDdelatransaction:)\\d+");
        Matcher m_id = p_id.matcher(msg);

        // if our pattern matches the string, we can try to extract our groups
        if (m_id.find())
        {
            String group1 = m_id.group(0);
            Pattern p_transac = Pattern.compile("\\d+");
            Matcher match_id = p_transac.matcher(group1);
            // get the two groups we were looking for
            if (match_id.find()) {
                String id_transac = match_id.group(0);
                //Log.d("match_id",id_transac);
            }
        }

        // recupeper numero ussd
        Pattern p_numero = Pattern.compile("^(Transferteffectuepour)\\d+FCFA.+\\(\\d+\\)");
        Matcher m_numero = p_numero.matcher(msg);

        // if our pattern matches the string, we can try to extract our groups
        if (m_numero.find())
        {
            String group1 = m_numero.group(0);
            Pattern p_num = Pattern.compile("\\(\\d+\\)");
            Matcher match_num = p_num.matcher(group1);
            // get the two groups we were looking for
            if (match_num.find()) {
                String group2 = match_num.group(0);
                Pattern p_num_1 = Pattern.compile("\\d+");
                Matcher match_num_1 = p_num_1.matcher(group2);
                // get the two groups we were looking for
                if (match_num_1.find()) {
                    String numero = match_num_1.group(0);
                    //Log.d("match_numero",numero);
                    numero_t = numero;
                }

                //Log.d("match_numero",numero);
            }
        }
        valider();
    }


    private void valider() {
        RequestQueue queue = Volley.newRequestQueue(mcontext);
        StringRequest postRequest = new StringRequest(Request.Method.POST, url_valider,
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
                                //Prefs.putString(ID_TONTINE_USSD, "");
                                // Mettre à jour la préférence id tontine
                                //Prefs.putString(MONTANT_VERSE, "");
                            }else{

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
                //params.put("id_tontine", Prefs.getString(ID_TONTINE_USSD,null));
                //params.put("id_tontine", periode_value);
                params.put("montant", montant_t);

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





