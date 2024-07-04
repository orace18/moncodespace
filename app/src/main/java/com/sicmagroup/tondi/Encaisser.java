package com.sicmagroup.tondi;

import android.app.Dialog;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.provider.Settings;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.phone.SmsRetriever;
import com.google.android.gms.auth.api.phone.SmsRetrieverClient;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.zxing.WriterException;
import com.orm.SugarRecord;
import com.pixplicity.easyprefs.library.Prefs;
import com.romellfudi.permission.PermissionService;

import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import androidmads.library.qrgenearator.QRGContents;
import androidmads.library.qrgenearator.QRGEncoder;
import com.sicmagroup.formmaster.FormBuilder;
import com.sicmagroup.formmaster.model.BaseFormElement;
import com.sicmagroup.formmaster.model.FormElementTextNumber;
import com.sicmagroup.tondi.utils.Constantes;

import static com.sicmagroup.tondi.Connexion.ID_UTILISATEUR_KEY;

import static com.sicmagroup.tondi.Connexion.TEL_KEY;
import static com.sicmagroup.tondi.utils.Constantes.SERVEUR;


public class Encaisser extends AppCompatActivity {
    GridView versements_grid;
    List<Versement> versement_list =new ArrayList<Versement>();
    //String url_afficher = "http://"+SERVEUR+"/api/v1/tontines/afficher_carte";
    String url_afficher_token = SERVEUR+"/api/v1/retraits/afficher_token";
    long id_retrait;
    int mise;

    private static final int TAG_MONTANT = 11;
    RecyclerView mRecyclerView;
    FormBuilder frm_versement = null;
    String TAG = "GenerateQRCode";
    EditText edtValue;
    TextView mText;
    //ImageView qrImage;

    Button start, save;
    String inputValue;
    String savePath = Environment.getExternalStorageDirectory().getPath() + "/QRCode/";
    Bitmap bitmap;
    QRGEncoder qrgEncoder;
    ImageView qr_code;
    Utilitaire utilitaire;
//    private static final int SMS_CONSENT_REQUEST = 2;  // Set to an unused request code



//    private static final int SERVERPORT = 5000;
//    private static final String SERVER_IP = "10.41.248.130";

//    ActivityResultLauncher<Intent> someActivityResultLauncher = registerForActivityResult(
//            new ActivityResultContracts.StartActivityForResult(),
//            new ActivityResultCallback<ActivityResult>() {
//                @Override
//                public void onActivityResult(ActivityResult result) {
//                    if (result.getResultCode() == Activity.RESULT_OK) {
//                        // There are no request codes
//                        Intent data = result.getData();
//                        String message = data.getStringExtra(SmsRetriever.EXTRA_SMS_MESSAGE);
//                        Toast.makeText(Encaisser.this, "", Toast.LENGTH_SHORT).show();
//                    }
//                }
//            });
//    public void openSomeActivityForResult(Intent intent) {
////        Intent intent = new Intent(this, CodeOtpVerification.class);
//        someActivityResultLauncher.launch(intent);
//    }
//    private final BroadcastReceiver smsVerificationReceiver = new BroadcastReceiver() {
//        @Override
//        public void onReceive(Context context, Intent intent) {
//            if (SmsRetriever.SMS_RETRIEVED_ACTION.equals(intent.getAction())) {
//                Bundle extras = intent.getExtras();
//                Status smsRetrieverStatus = (Status) extras.get(SmsRetriever.EXTRA_STATUS);
//
//                switch (smsRetrieverStatus.getStatusCode()) {
//                    case CommonStatusCodes.SUCCESS:
//                        // Get consent intent
//                        Intent consentIntent = extras.getParcelable(SmsRetriever.EXTRA_CONSENT_INTENT);
//                        try {
//                            // Start activity to show consent dialog to user, activity must be started in
//                            // 5 minutes, otherwise you'll receive another TIMEOUT intent
//
////                             startActivityForResult(consentIntent, SMS_CONSENT_REQUEST);
//                            openSomeActivityForResult(consentIntent);
//                        } catch (ActivityNotFoundException e) {
//                            // Handle the exception ...
//                        }
//                        break;
//                    case CommonStatusCodes.TIMEOUT:
//                        // Time out occurred, handle the error.
//                        break;
//                }
//            }
//        }
//    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Toast.makeText(getApplicationContext(),"id_tontine="+id_tontine,Toast.LENGTH_LONG).show();
        // remove title
        //requestWindowFeature(Window.FEATURE_NO_TITLE);
        //getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
               // WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_encaisser_3);
        new Prefs.Builder()
                .setContext(this)
                .setMode(ContextWrapper.MODE_PRIVATE)
                .setPrefsName(getPackageName())
                .setUseDefaultSharedPreference(true)
                .build();
        Intent getIntent = getIntent();
        id_retrait = getIntent.getLongExtra("id_retrait",0);
        //qrImage =  findViewById(R.id.qr_code);
        qr_code = (ImageView) findViewById(R.id.qr_code_view);
        utilitaire = new Utilitaire(this);

        afficher_token(id_retrait);
        Point point = new Point();
        int width = point.x;
        int height = point.y;
        int smallerDimension = width < height ? width : height;
        smallerDimension = smallerDimension * 3 / 2;
        mText= findViewById(R.id.decompteur);


        FloatingActionButton btn_cmt_retirer = findViewById(R.id.btn_cmt_retirer);
        btn_cmt_retirer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                //Encaisser.this.finish();
                startActivity(new Intent(Encaisser.this,Ccm.class));
            }
        });

        Button back_to = findViewById(R.id.back_to);
        back_to.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
//                utilitaire.refreshDatabse();
                Intent intent = new Intent(Encaisser.this,Home.class);
                intent.putExtra(Constantes.BOTTOM_NAV_DESTINATION, Constantes.DESTINATION_RETRAITS);
                startActivity(intent);
                Encaisser.this.finish();
            }
        });

//        Button btn_supprimer_code = findViewById(R.id.btn_supprimer_code);
//        btn_supprimer_code.setVisibility(View.GONE);
//        btn_supprimer_code.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                // TODO Auto-generated method stub
//                alertView("Confirmez la suppression","Êtes vous sûr de vouloir supprimer ce code de retrait?");
//            }
//        });

//        Button btn_deconnexion = findViewById(R.id.btn_deconnexion);
//        btn_deconnexion.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                // TODO Auto-generated method stub
//                alertViewDeco("Déconnexion", "Êtes vous sûr de vouloir vous déconnecter?");
//
//            }
//        });

        SmsRetrieverClient client = SmsRetriever.getClient(Encaisser.this);
        Task<Void> task = client.startSmsRetriever();

        task.addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Toast.makeText(Encaisser.this, "Session activée", Toast.LENGTH_SHORT).show();
            }
        });
        task.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(Encaisser.this, "Erreur interne, réessayer svp! Si cela persiste contactez le support technique svp.", Toast.LENGTH_SHORT).show();
//                Encaisser.this.finish();
            }
        });

        //verifier chaque seconde l'état de la transaction


    }



    @Override
    public void onBackPressed() {
        // your code.
//        utilitaire.refreshDatabse();
        Encaisser.this.finish();
        Intent intent = new Intent(Encaisser.this,Home.class);
        intent.putExtra(Constantes.BOTTOM_NAV_DESTINATION, Constantes.DESTINATION_RETRAITS);
        startActivity(intent);
    }

    private void alertViewDeco( String title ,String message ) {
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
                        Encaisser.this.finish();
                        startActivity(new Intent(Encaisser.this,Connexion.class));
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
    private void afficher_token(final long id_retrait) {
        final Retrait retrait = SugarRecord.findById(Retrait.class, id_retrait);
        Log.e("test_Encaissee", String.valueOf(id_retrait));
        //Toast.makeText(getApplicationContext(), "id:"+id_retrait,Toast.LENGTH_LONG).show();
        long id_tontine = retrait.getTontine().getId();
        TextView token = findViewById(R.id.token);
        token.setText(String.valueOf(retrait.getToken()));
        Calendar cal = Calendar.getInstance(Locale.FRENCH);
        cal.setTimeInMillis(retrait.getCreation());
        String date = DateFormat.format("dd/MM/yyyy à HH:mm", cal).toString();
        TextView creation_txt = findViewById(R.id.creation);
        creation_txt.setText(String.valueOf(date));
        TextView montant_cotis_txt = findViewById(R.id.montant_cumule);
        TextView commission_txt = findViewById(R.id.commission);
        TextView mt_encaisser_txt = findViewById(R.id.montant_a_encaisser);

        montant_cotis_txt.setText(retrait.getTontine().getMontCumuleNow(id_tontine,retrait.getTontine().getStatut())+" F");
        commission_txt.setText(String.format("%02f", retrait.getTontine().getMontCommisNow(id_tontine,retrait.getTontine().getStatut()))+" F");
        mt_encaisser_txt.setText(String.format("%02f", retrait.getTontine().getMontEncaisseNow(id_tontine,retrait.getTontine().getStatut()))+" F");


        Connexion.AeSimpleSHA1 AeSimpleSHA1 = new Connexion.AeSimpleSHA1();
        inputValue=retrait.getToken()+"/"+id_retrait;
        /**/try {
            inputValue = AeSimpleSHA1.md5(inputValue);
            inputValue = AeSimpleSHA1.SHA1(inputValue);
        } catch (NoSuchAlgorithmException | UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        WindowManager manager = (WindowManager) getSystemService(WINDOW_SERVICE);

        // initializing a variable for default display.
        Display display = manager.getDefaultDisplay();

        // creating a variable for point which
        // is to be displayed in QR Code.
        Point point = new Point();
        display.getSize(point);

        // getting width and
        // height of a point
        int width = point.x;
        int height = point.y;

        // generating dimension from width and height.
        int dimen = width < height ? width : height;
        dimen = dimen * 3 / 4;

        // setting this dimensions inside our qr code
        // encoder to generate our qr code.
        qrgEncoder = new QRGEncoder(token.getText().toString(), null, QRGContents.Type.TEXT, dimen);
        try {
            // getting our qrcode in the form of bitmap.
            bitmap = qrgEncoder.encodeAsBitmap();
            // the bitmap is set inside our image
            // view using .setimagebitmap method.
            qr_code.setImageBitmap(bitmap);
        } catch (WriterException e) {
            // this method is called for
            // exception handling.
            Log.e("Tag", e.toString());
        }

        // trouver le temps restant
        Long diff = Calendar.getInstance().getTime().getTime()-retrait.getCreation();
        int remaining_seconds = (int) (86400000 - diff);//((-Calendar.getInstance().getTime().getTime())/1000);//GetDifference();//86400000;
        new CountDownTimer(remaining_seconds, 1000) {

            public void onTick(long millisUntilFinished) {
                long heure = TimeUnit.MILLISECONDS.toHours(millisUntilFinished) - TimeUnit.DAYS.toHours(TimeUnit.MILLISECONDS.toDays(millisUntilFinished));
                long minute = (TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished) - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(millisUntilFinished)));
                long seconde = (TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished)));
                String decompte = "Expire dans "+heure+" heure(s) "+minute+" minute(s) "+seconde+" seconde(s)";
                mText.setText( decompte);
            }

            public void onFinish() {
                mText= findViewById(R.id.decompteur);
                mText.setText("Expiré!");
                //Affichage d'un button pour renouveller si expirer et statut en cours
                if (retrait.getStatut().equals("en cours"))
                {
                    retrait.setStatut("expire");
                    retrait.save();
                    Log.e("test", "en cours mais timer fini");
                }
            }
        }.start();


    }

    public int GetDifference(long start,long end){
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(start);
        int hour = cal.get(Calendar.HOUR_OF_DAY);
        int min = cal.get(Calendar.MINUTE);
        long t=(23-hour)*3600000+(59-min)*60000;

        t=start+t;

        int diff=0;
        if(end>t){
            diff=(int)((end-t)/ TimeUnit.DAYS.toMillis(1))+1;
        }

        return  diff;
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
                        // supprimer le code
                        Retrait retrait = SugarRecord.findById(Retrait.class, Long.valueOf(id_retrait));
                        String code = retrait.getToken();
                        retrait.delete();
                        String msg="Votre code de retait n° "+code+" a été supprimé";
                        Intent i1 = new Intent(Encaisser.this, Message_ok.class);
                        i1.putExtra("msg_desc",msg);
                        i1.putExtra("class","com.sicmagroup.tondi.MesRetraits");
                        startActivity(i1);
                        //startActivity(new Intent(Encaisser.this,MesRetraits.class));
                    }
                }).show();
    }



    private PermissionService.Callback callback = new PermissionService.Callback() {
        @Override
        public void onRefuse(ArrayList<String> RefusePermissions) {
            Toast.makeText(Encaisser.this,
                    getString(R.string.refuse_permissions),
                    Toast.LENGTH_SHORT).show();
            Encaisser.this.finish();
        }

        @Override
        public void onFinally() {
            // pass
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (!Settings.canDrawOverlays(Encaisser.this)) {
                    Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                            Uri.parse("package:" + Encaisser.this.getPackageName()));
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

    @Override
    protected void onStop() {
        super.onStop();
        utilitaire.refreshDatabse();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
