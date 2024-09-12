package com.sicmagroup.tondi;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.pixplicity.easyprefs.library.Prefs;
import com.sicmagroup.tondi.utils.Constantes;

import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;


import java.util.Locale;

import static com.sicmagroup.tondi.Accueil.CGU_FR_KEY;
public class CGU extends AppCompatActivity {


    private TextToSpeech textToSpeech;
    private AlertDialog progressDialog;
    private boolean isReading = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //demanderLectureCgu();
        // remove title
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_cgu);


        Intent intent = getIntent();
        String origine = "";
        if (intent.hasExtra("origine"))
            origine = intent.getStringExtra("origine");

        ImageView btn_fermer_cgu = findViewById(R.id.btn_fermer_cgu);
        btn_fermer_cgu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isReading) {
                    CGU.this.finish();
                } else {
                    Toast.makeText(CGU.this, "Veuillez attendre la fin de la lecture", Toast.LENGTH_SHORT).show();
                }
            }
        });

        Button btn_cgu_lu = findViewById(R.id.btn_cgu_lu);
        btn_cgu_lu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isReading) {
                    Prefs.putInt(CGU_FR_KEY, 1);
                    CGU.this.finish();
                } else {
                    Toast.makeText(CGU.this, "Veuillez attendre la fin de la lecture", Toast.LENGTH_SHORT).show();
                }
            }
        });

        if (!origine.equals("")) {
            btn_cgu_lu.setVisibility(View.GONE);
        } else {
            btn_cgu_lu.setVisibility(View.VISIBLE);
        }

        // Initialiser le moteur Text-to-Speech
        textToSpeech = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status == TextToSpeech.SUCCESS) {
                    int result = textToSpeech.setLanguage(Locale.FRENCH);
                    if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                        Toast.makeText(CGU.this, "La langue n'est pas prise en charge", Toast.LENGTH_SHORT).show();
                    } else {
                        // Demander l'autorisation de lecture d'écran
                        demanderLectureCgu();
                    }
                } else {
                    Toast.makeText(CGU.this, "Échec de l'initialisation de Text-to-Speech", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    // Méthode pour demander la lecture des CGU
    private void demanderLectureCgu() {
        Dialog dialog_alert = new Dialog(this);
        dialog_alert.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog_alert.setCancelable(true);
        dialog_alert.setContentView(R.layout.dialog_attention);

        TextView titre = dialog_alert.findViewById(R.id.deco_title);
        titre.setText("Lecture des CGU");
        TextView message_deco = dialog_alert.findViewById(R.id.deco_message);
        message_deco.setText("Voulez-vous écouter les CGU ?");
        Button oui = dialog_alert.findViewById(R.id.btn_oui);
        oui.setText("Oui");

        oui.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog_alert.cancel();

                lireCgu();
                afficherProgressDialog();
            }

        });

        Button non = dialog_alert.findViewById(R.id.btn_non);
        non.setText("Non");
        non.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog_alert.cancel();
            }
        });

        dialog_alert.show();
    }
    // Méthode pour lire les CGU
    private void lireCgu() {

        isReading = true;

        String cguText = "Les présentes conditions générales d’utilisation (ci-après dénommées « CGU ») ont pour objet de déterminer les règles d’accès et les conditions d’utilisation de la plateforme « COMUBA Tontine Digitale » éditée par la société « SICMa & Associés S.A.R.L. » au bénéfice de l’IMF la COMUBA. En accédant à la plateforme, en installant et/ou en utilisant l’application sur son terminal, l’utilisateur reconnaît avoir lu et accepté, l’intégralité des dispositions ci-après sans restriction ni réserve. Toute utilisation de COMUBA Tontine Digitale doit se faire dans le respect des présentes CGU. 1. Votre relation avec COMUBA Tontine Digitale 1.1 Présentation de COMUBA Tontine Digitale COMUBA Tontine Digitale est une application numérique de tontine digitale, une plateforme sécurisée, qui joue le rôle essentiel de collecteur de l’épargne de manière digitale pour la COMUBA et permet à l’épargnant de réaliser des tontines journalières, hebdomadaires et mensuelles en étant déconnecté/et ou connecté à internet. Elle met à la disposition de ses utilisateurs les services de paiement de compte mobile électronique (MOOV mobile money) pour effectuer le paiement et le retrait instantané de leur tontine via leurs comptes de paiement électroniques chez l’opérateur mobile Moov Africa. COMUBA Tontine Digitale permet également aux particuliers de recevoir leurs montants cotisés en espèces chez un marchand agréé par la COMUBA et utilisant l’application marchande COMUBA Tontine Digitale. 1.2 Respect de la vie privée La confidentialité et le respect de vos données à caractère personnel et de paiements sont importants pour la COMUBA et les services de COMUBA Tontine Digitale mis à votre disposition. Veuillez consulter à ce propos notre Politique de Confidentialité. 1.3 Propriété intellectuelle L’utilisateur reconnaît expressément que le site/Application et les contenus (images, photographies, marques, logos, articles, URLs, gifs, …) sont la propriété exclusive de l’éditeur et sont protégés par le code de la propriété intellectuelle béninoise ainsi que les traités et les accords internationaux applicables en la matière. En conséquence, toute reproduction, représentation, adaptation, traduction et/ou transformation partielle ou intégrale, ou transfert vers un autre site, sans l’autorisation écrite, expresse et préalable de l’éditeur, sont interdits. 1.4 Cession de compte COMUBA Tontine Digitale Vous ne pouvez céder aucun droit ni aucune obligation ou transférer votre compte COMUBA Tontine Digitale à un tiers, dans le cadre du présent contrat, sans le consentement écrit préalable de la COMUBA. 1.5 Notifications qui vous sont destinées Toute notification ou toute autre information de COMUBA Tontine Digitale vous sera envoyée à votre adresse email ou par appel téléphonique ou par SMS.Excepté les notifications relatives aux modifications apportées au présent contrat, nous considèrerons que vous aurez reçu toute notification que nous vous aurons envoyée dans les 24 heures suivant son envoi par email ou SMS. 1.6 Notifications destinées à la COMUBA Toute notification envoyée à COMUBA Tontine Digitale dans le cadre du présent contrat doit être adressée par courrier électronique à l’adresse https://support_comuba@e-tondi.com.1.7 Historique des transactions Vous pouvez accéder aux détails des transactions de paiement exécutées et à d’autres informations relatives à l’historique de vos transactions dans votre compte sur l’application. 1.8 Modifications du présent Contrat sur les Conditions Générales d’Utilisation Nous pouvons à tout moment modifier le présent contrat ou le règlement sur le respect de la vie privée, supprimer certaines de ses clauses ou en ajouter de nouvelles, y compris s’agissant des commissions ou frais appliqués aux services de COMUBA Tontine Digitale. Toute modification sera faite de manière unilatérale par la COMUBA et vous serez réputé avoir accepté les modifications après en avoir reçu notification. Nous vous informerons des modifications avant leur entrée en vigueur, sauf dans les cas où elles sont exigées par la loi ou relatives à l’ajout d’un nouveau service ou d’une nouvelle fonctionnalité aux services existants ou toute autre modification dont nous estimons de manière raisonnable qu’elle ne diminue vos droits ni n’accroît vos responsabilités. Dans ce cas, les modifications seront effectuées sans qu’aucune notification préalable ne vous soit adressée et seront immédiatement applicables. 1.9 Activités interdites COMUBA Tontine Digitale ne doit être utilisée en aucun cas pour des activités :qui violent une loi, un contrat, une ordonnance ou une réglementation ;en lien avec des transactions impliquant des produits qui présentent un risque pour la sécurité du consommateur ; des drogues et objets associés ; des objets illégaux ou objets visant à encourager, promouvoir et faciliter des activités illégales ou à expliquer à d’autres personnes comment les mener ; des objets qui prônent la haine, la violence, l’intolérance raciale ou l’exploitation financière d’actes criminels ; des objets pouvant être considérés comme étant de nature obscène ;qui impliquent la vente de produits ou services identifiés par les agences gouvernementales comme présentant une forte probabilité de fraude ;à caractère illicite ou de blanchiment d’argent ;en relation et/ou en financement du terrorisme. 2. Application COMUBA Tontine Digitale 2.1 Objet de l’Application Les services et fonctionnalités proposés par l’Application sont mis à la disposition de l’Utilisateur par la COMUBA et accessibles depuis les menus pour les parties ci-après énumérées :Créer son compte ;Se connecter et Gérer son compte ;Souscrire à une ou plusieurs tontines ;Consulter le solde de ses tontines ;Retirer le montant de ses tontines. Les services et fonctionnalités proposés par l’Application COMUBA Tontine Digitale sont mis à la disposition de l’Utilisateur par la COMUBA. 2.2 Accès à l’Application L’Application est téléchargeable gratuitement sur le lien https://comuba.e-tondi.com hors coût d’abonnement auprès de l’opérateur de téléphonie mobile, hors coût de connexion et d’accès au réseau Internet et hors surcoût éventuel facturé pour chargement des données. La version du système Android est susceptible d’être mise à jour pour ajouter de nouvelles fonctionnalités et de nouveaux services. La réalisation des mises à jour du logiciel de l’application, régulièrement proposées par la Société SICMa & Associés, relèvent de la responsabilité exclusive de la COMUBA. À cet égard, toute Application téléchargée par un Utilisateur a une durée de validité limitée dans le temps, compte tenu de ces mises à jour à télécharger impérativement et des éventuelles évolutions techniques, législatives ou réglementaires, dont la COMUBA n’a pas le contrôle. Les services et fonctionnalités sont accessibles à l’Utilisateur à tout moment 24h sur 24 et 7 jours sur 7, à l’exception des périodes de maintenance dans les conditions définies ci-après. Pour accéder à son compte personnel, l’Utilisateur doit préalablement compléter le formulaire d’inscription, auquel il accède via le menu, et entrer dans l’Application le code d’authentification qui lui est adressé par SMS après réception de la demande d’inscription. Ce code d’authentification rattaché au numéro de téléphone de l’Utilisateur lui est strictement personnel et lui permet d’opérer le paramétrage et la reconnaissance automatique de son téléphone mobile ou smartphone à son compte personnel. Pour accéder à et utiliser l’Application COMUBA Tontine Digitale ou ses services, l’Utilisateur doit posséder un téléphone compatible ou un terminal mobile et un accès au réseau Internet muni d’un appareil photo."; //2.3 Accès aux données L’utilisation des données de l’utilisateur de l’Application COMUBA Tontine Digitale nécessite le consentement préalable exprès de l’Utilisateur. À cet effet, l’Utilisateur devra activer, s’il le souhaite, la fonction d’accessibilité aux services de l’Application COMUBA Tontine Digitale directement dans les réglages de son téléphone mobile et accepter que l’Application COMUBA Tontine Digitale puisse y avoir recours. Cette fonctionnalité peut à tout moment et sans frais, être désactivée ou activée.Grâce à l’acceptation de la fonction d’accessibilité aux services de l’Application COMUBA Tontine Digitale l’Utilisateur peut bénéficier de l’ensemble des services et fonctionnalités de l’Application COMUBA Tontine Digitale. 3. Responsabilités et garanties La COMUBA n’est tenue que par une obligation de moyens ; sa responsabilité ne pourra être engagée pour des dommages résultant de l’Application COMUBA Tontine Digitale tels que : perte de données, intrusion, virus, rupture du service, ou autres. La COMUBA ne saurait notamment être tenue pour responsable d’une rupture de service causée par des dysfonctionnements liés à des causes extérieures à elle ou à la survenance d’un cas de force majeure. À titre d’exemple, la COMUBA ne saurait être tenue pour responsable des conséquences liées à :Des défaillances affectant le réseau internet et/ou la connexion au wifi ; la qualité du débit et de la performance de la connexion internet ;L’indisponibilité technique de la connexion, qu’elle soit due notamment à un cas de force majeure, à une coupure de courant, à une panne de réseau, à une défaillance de votre fournisseur d’accès Internet ou à une saturation du réseau internet à certaines périodes ;Une utilisation de l’Application COMUBA Tontine Digitale par l’Utilisateur dans des conditions non conformes à sa destination.La COMUBA ne garantit pas que les services proposés fonctionneront sans interruption, ou qu’ils seront exempts d’erreurs, ni que les erreurs constatées seront corrigées. Les services sont fournis en l’état et dans la mesure où ils sont disponibles. La COMUBA ne fournit aucune garantie expresse ou implicite, y compris, sans que cela soit limitatif, les garanties relatives à la qualité et à la compatibilité de l’Application COMUBA Tontine Digitale à un usage spécifique, et à la non-violation des règles d’utilisation des services par ses Utilisateurs. 4. Suspension d’accès et réclamation La COMUBA peut suspendre l’accès à l’Application COMUBA Tontine Digitale dans les cas suivants :Violation des présentes CGU par l’Utilisateur ;Demande émanant d’une autorité compétente, judiciaire ou administrative ;Maintenance de l’Application ;Existence d’un cas de force majeure ;Piratage ou tentative d’utilisation non autorisée des données.L’Utilisateur peut formuler des réclamations auprès de la COMUBA par courrier électronique ou en se rendant dans ses locaux. 5. Utilisation des services de COMUBA Tontine Digitale L’utilisateur doit s’abstenir de tenter d’accéder sans autorisation à une quelconque partie des services de l’Application COMUBA Tontine Digitale ou à toute autre Application ou réseau connecté aux services de l’Application COMUBA Tontine Digitale. Les Utilisateurs s’interdisent de recueillir des données ou informations personnelles d’autres Utilisateurs pour un usage inapproprié. 6. Responsabilité des Utilisateurs Les Utilisateurs s’engagent à utiliser l’Application COMUBA Tontine Digitale conformément aux présentes CGU. Les Utilisateurs doivent signaler à la COMUBA toute violation ou utilisation non autorisée de l’Application COMUBA Tontine Digitale par d’autres Utilisateurs ou toute autre atteinte à la sécurité dont ils ont connaissance. Les Utilisateurs sont tenus responsables de l’utilisation de leur identifiant et de leur mot de passe ainsi que de toutes les activités réalisées sous ce compte. 7. Protection des données personnelles Les informations personnelles des Utilisateurs sont soumises à la Politique de Confidentialité. 8. Loi applicable et règlement des litiges Les présentes CGU sont régies par la loi béninoise. Tout différend ou litige relatif à la validité, à l’interprétation ou à l’exécution des présentes CGU sera de la compétence exclusive des juridictions béninoises. 9. Acceptation et modifications des CGU L’acceptation des présentes CGU s’effectue dès l’inscription de l’Utilisateur sur la plateforme COMUBA Tontine Digitale. COMUBA se réserve le droit de modifier à tout moment les termes, conditions et mentions des présentes CGU. Les utilisateurs seront informés des modifications lors de leur connexion à l’Application.";

        textToSpeech.speak(cguText, TextToSpeech.QUEUE_FLUSH, null, "1");

        // Ajouter un écouteur pour détecter quand la lecture est terminée
        textToSpeech.setOnUtteranceProgressListener(new UtteranceProgressListener() {
            @Override
            public void onStart(String utteranceId) {
                // Lecture commencée
            }

            @Override
            public void onDone(String utteranceId) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        // Actions à effectuer à la fin de la lecture
                        Prefs.putInt(CGU_FR_KEY, 1);
                        isReading = false;
                        dismissProgressDialog();
                        CGU.this.finish();
                    }
                });
            }

            @Override
            public void onError(String utteranceId) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        isReading = false;
                        dismissProgressDialog();
                        Toast.makeText(CGU.this, "Erreur lors de la lecture", Toast.LENGTH_SHORT).show();
                        Log.e("L'erreur","Une erreur");
                    }
                });
            }
        });
    }

    private void afficherProgressDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = getLayoutInflater().inflate(R.layout.progress_dialog, null);
        builder.setView(view);
        builder.setCancelable(false);
        progressDialog = builder.create();
        progressDialog.show();
    }

    private void dismissProgressDialog() {
        if (progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
    }

    @Override
    public void onBackPressed() {
        if (isReading) {
            super.onBackPressed();
        } else {
            Toast.makeText(CGU.this, "Veuillez attendre la fin de la lecture", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onDestroy() {
        if (textToSpeech != null) {
            textToSpeech.stop();
            textToSpeech.shutdown();
        }
        super.onDestroy();
    }


}
