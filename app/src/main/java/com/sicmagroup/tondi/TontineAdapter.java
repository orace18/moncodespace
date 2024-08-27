package com.sicmagroup.tondi;



import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.PopupMenu;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
        import android.view.MenuInflater;
        import android.view.MenuItem;
        import android.view.View;
        import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;


import com.orm.SugarRecord;
import com.pixplicity.easyprefs.library.Prefs;
import com.sicmagroup.tondi.Enum.PeriodiciteEnum;
import com.sicmagroup.tondi.Enum.RetraitEnum;
import com.tapadoo.alerter.Alerter;
import com.tapadoo.alerter.OnHideAlertListener;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Random;

import static com.sicmagroup.tondi.Accueil.CARTE_NAV_KEY;
import static com.sicmagroup.tondi.Connexion.ID_UTILISATEUR_KEY;
import static com.sicmagroup.tondi.utils.Constantes.SERVEUR;

/**
 * Created by Ravi Tamada on 18/05/16.
 */
public class TontineAdapter extends RecyclerView.Adapter<TontineAdapter.MyViewHolder> {

    private Context mContext;
    private List<Tontine> tontineList;

    String url_terminer = SERVEUR+"/api/v1/tontines/terminer";
    String url_init_retrait = SERVEUR+"/api/v1/withdraw/init";
    int position;
    int id_tontine;
    private RadioGroup radioGroup;
    private RadioButton radioButton;
    private String TAG = "tontineAdpter";


    public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public TextView creation, titre, versements, montant, montant_cumule_txt, montant_cumule, denomination;
        public ImageView icon_creation, icon_manuel;
        public RelativeLayout relativeLayoutCard;

        public MyViewHolder(View view) {
            super(view);
            creation = (TextView) view.findViewById(R.id.creation);
            titre = (TextView) view.findViewById(R.id.titre);
            versements = (TextView) view.findViewById(R.id.nb_versements);
            montant = (TextView) view.findViewById(R.id.mt_tontine);
            //overflow = (ImageView) view.findViewById(R.id.overflow);
            icon_creation = (ImageView) view.findViewById(R.id.icon_creation);
            icon_manuel = (ImageView) view.findViewById(R.id.icon_manuel);
            relativeLayoutCard = (RelativeLayout) view.findViewById(R.id.relativeLayoutCard);
            montant_cumule_txt = (TextView) view.findViewById(R.id.mt_tontine_enc_txt);
            montant_cumule = (TextView) view.findViewById(R.id.mt_tontine_enc);
            denomination = (TextView) view.findViewById(R.id.denomination);
            view.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            carte(tontineList.get(getAdapterPosition()).getId());
        }
    }


    public TontineAdapter(Context mContext, List<Tontine> tontineList, int position) {
        this.mContext = mContext;
        this.tontineList = tontineList;
        this.position = position;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.tontine_row, parent, false);


        return new MyViewHolder(itemView);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(final MyViewHolder holder, int position) {

        Tontine tontine = tontineList.get(position);
        Prefs.putString(CARTE_NAV_KEY, tontine.getStatut());
        int total_versements = tontine.getVersements();
        if(total_versements < 0){
           total_versements = 0;
        }
        @SuppressLint("DefaultLocale") String versements = String.format("%02d", total_versements);
        Calendar cal = Calendar.getInstance(Locale.FRENCH);
        cal.setTimeInMillis(tontine.getCreation());
        String date = DateFormat.format("yyyy-MM-dd HH:mm:ss", cal).toString();
        holder.creation.setText("Démarée le "+date);

        if (tontine.getPeriode().equals(PeriodiciteEnum.JOURNALIERE.toString())){
            //holder.versements.setText("Carte N°"+tontine.getPositionCarteNew(tontine.getId())+ " avec " +versements+" sur 31 versements au total");
            holder.versements.setText("Carte N°"+tontine.getPositionCarteNew(tontine.getId())+ " avec " +versements+" / 31 versements");
            holder.titre.setText("CARNET N° "+tontine.getCarnet()+" - Tontine "+tontine.getPeriode()+" de "+tontine.getMise()+" F");
//            holder.relativeLayoutCard.setBackgroundResource(R.drawable.tontine_bg);
            holder.relativeLayoutCard.setBackgroundColor(ContextCompat.getColor(mContext, R.color.card_jour));
        }else{
            holder.titre.setText("CARNET N° "+tontine.getCarnet()+" - Tontine "+tontine.getPeriode()+" de "+tontine.getMise()+" F");

            //holder.titre.setText("CARTE N° "+tontine.getNumero_plus(tontine.getId(),Prefs.getString(CARTE_NAV_KEY,""))+" - Tontine "+tontine.getPeriode()+" de "+tontine.getMise()+" F");
        }
        if (tontine.getPeriode().equals(PeriodiciteEnum.HEBDOMADAIRE.toString())){
            holder.versements.setText("Carte N°"+tontine.getPositionCarteNew(tontine.getId())+ " avec " +versements+" / 52 versements");
//            holder.relativeLayoutCard.setBackgroundResource(R.drawable.tontine_bg);
            holder.relativeLayoutCard.setBackgroundColor(ContextCompat.getColor(mContext, R.color.card_hebdo));

        }
        if (tontine.getPeriode().equals(PeriodiciteEnum.MENSUELLE.toString())){
//            holder.relativeLayoutCard.setBackgroundResource(R.drawable.tontine_bg);
            holder.versements.setText("Carte N°"+tontine.getPositionCarteNew(tontine.getId())+ " avec " +versements+"/ 12 versements ");
            holder.relativeLayoutCard.setBackgroundColor(ContextCompat.getColor(mContext, R.color.card_mensuel));

        }

        if (tontine.getDateDeblocage() != null )
        {
            holder.icon_manuel.setVisibility(View.VISIBLE);
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
            try {
                Date date_deblocage = format.parse(tontine.getDateDeblocage());
                Date now = Calendar.getInstance().getTime();
                Log.e(TAG, date_deblocage.before(now)+"");

                if(date_deblocage.before(now)){
                    holder.icon_manuel.setImageResource(R.drawable.lock_open_48px);
                } else {
                    holder.icon_manuel.setImageResource(R.drawable.lock_48px);
                }
            } catch(ParseException e){
                e.printStackTrace();
            }
        }
        else
        {
            holder.icon_manuel.setVisibility(View.GONE);
        }
        if(tontine.getStatut().equals("encaissee"))
        {
            holder.montant_cumule.setText(String.valueOf( tontine.getMontEncaisseNow(tontine.getId(), tontine.getStatut())));
            holder.montant_cumule_txt.setVisibility(View.VISIBLE);
            holder.montant_cumule.setVisibility(View.VISIBLE);
        }
        else
        {
            holder.montant_cumule_txt.setVisibility(View.GONE);
            holder.montant_cumule.setVisibility(View.GONE);
        }

        holder.icon_creation.setColorFilter(mContext.getResources().getColor(R.color.colorGray));
        holder.montant.setText(tontine.getMontCumule(tontine.getStatut())+" F");
        if(tontine.getDenomination() != null){
            holder.denomination.setText(tontine.getDenomination());
        }
        else{
            holder.denomination.setText("Ma tontine "+tontine.getCarnet());
            tontine.setDenomination("Ma tontine "+tontine.getCarnet());
            tontine.save();
        }
    }

    /**
     * Showing popup menu when tapping on 3 dots
     */
    /**
     * Click listener for popup menu items
     */

    @Override
    public int getItemCount() {
        return tontineList.size();
    }

    private void carte(Long id_tontine){
        //mContext.startActivity(new Intent(mContext,Carte.class));
        //Toast.makeText(mContext,"id_tontineeee="+Integer.parseInt(String.valueOf(id_tontine)),Toast.LENGTH_LONG).show();
        Intent intent = new Intent(new Intent(mContext,CarteMain.class));
        intent.putExtra("id_tontine",Integer.parseInt(String.valueOf(id_tontine)));
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
        Tontine tontine = SugarRecord.findById(Tontine.class, (long) id_tontine);
        tontine.terminer(this.mContext);
        String msg="Votre tontine "+ tontine.getPeriode().toLowerCase()+" de mise "+tontine.getMise()+"F a été arrêté au solde de "+tontine.getMontant()+"F CFA";
        Intent i = new Intent(mContext, Message_ok.class);
        i.putExtra("msg_desc",msg);
        i.putExtra("class","com.sicmagroup.tondi.MesTontines");
        mContext.startActivity(i);
    }

    public class MySecureRandom {
        private SecureRandom SECURE_RANDOM = new SecureRandom();

        public String nextSessionId() {
            return new BigInteger(130, SECURE_RANDOM).toString(32);
        }
    }

    private static final String ALLOWED_CHARACTERS ="0123456789";

    private static String getRandomString(final int sizeOfRandomString)
    {
        final Random random=new Random();
        final StringBuilder sb=new StringBuilder(sizeOfRandomString);
        for(int i=0;i<sizeOfRandomString;++i)
            sb.append(ALLOWED_CHARACTERS.charAt(random.nextInt(ALLOWED_CHARACTERS.length())));
        return sb.toString();
    }


    private void requete_retrait(final int id_tontine) {
        // enregistrer le retrait
        //MySecureRandom mostSecureRandom = new MySecureRandom();
        final Retrait retrait = new Retrait();
        Tontine tontine = SugarRecord.findById(Tontine.class, (long)id_tontine);
        retrait.setTontine(tontine);
        retrait.setToken(getRandomString(12));
        retrait.setStatut(RetraitEnum.IN_PROGRESS.toString());
        Utilisateur utilisateur = SugarRecord.findById(Utilisateur.class, Long.valueOf(Prefs.getString(ID_UTILISATEUR_KEY,null)));
        retrait.setUtilisateur(utilisateur);
        // maj des dates
        Date currentTime = Calendar.getInstance().getTime();
        long output_creation=currentTime.getTime()/1000L;
        String str_creation=Long.toString(output_creation);
        long timestamp_creation = Long.parseLong(str_creation) * 1000;
        long output_maj=currentTime.getTime()/1000L;
        String str_maj=Long.toString(output_maj);
        long timestamp_maj = Long.parseLong(str_maj) * 1000;
        retrait.setCreation(timestamp_creation);
        retrait.setMaj(timestamp_maj);
        retrait.save();
        if (retrait.getId()!=null){
            String msg="Le code de retrait portant le token "+retrait.getToken()+" est désormais actif pour 24H. Passé ce délai il sera inactif, vous pourrez toujours en générer autant de fois que vous voulez.";
            Intent i = new Intent(mContext, Message_ok.class);
            i.putExtra("msg_desc",msg);
            i.putExtra("id_retrait",Integer.parseInt(String.valueOf(retrait.getId())));
            mContext.startActivity(i);
        }
        /*Alerter.create((MesTontines) mContext)
                .setTitle("Le code de retrait a été correctement généré.")
                .setIcon(R.drawable.ic_check)
                .setBackgroundColorRes(R.color.colorPrimaryDark) // or setBackgroundColorInt(Color.CYAN)
                .setOnHideListener(new OnHideAlertListener() {
                    @Override
                    public void onHide() {
                        Intent intent = new Intent(mContext,Encaisser.class);
                        intent.putExtra("id_retrait",Integer.parseInt(String.valueOf(retrait.getId())));
                        mContext.startActivity(intent);
                    }
                })
                .show();*/


    }
}
