package com.sicmagroup.tondi.utils;

import com.pixplicity.easyprefs.library.Prefs;
import com.sicmagroup.tondi.R;

public class Constantes {

    public static final String SERVEUR = "http://test-etondi.tech:8081";

    public static final String ACCESS_NOUVELLE_TONTINE = "nouvelle_tontine_access";
    public static final String STATUT_UTILISATEUR = "statut";
    public static final String CODE_MARCHAND_KEY = "code_marchand";
    public static String TOKEN = "token";
    public static String REFRESH_TOKEN = "refresh_token";

    //bottom nav destination start
    public static final String BOTTOM_NAV_DESTINATION = "bottom_nav_destination";
    public static final String DESTINATION_ACCUEIL = "ACCUEIL";
    public static final String DESTINATION_RETRAITS = "RETRAITS";
    public static final String DESTINATION_TONTINES = "TONTINES";
    public static final String DESTINATION_AVANCES = "AVANCES";
    public static final String DESTINATION_PLAINTES = "PLAINTES";
    //bottom nav destination end

    //Tab title start
    public static final String TAB_TITLE_EN_COURS = "En cours";
    public static final String TAB_TITLE_TERMINEE = "Terminées";
    public static final String TAB_TITLE_EN_ATTENTE = "En attentes";
    public static final String TAB_TITLE_ENCAISSEE = "Encaissées";
    //Tab title end

    // Extrat key start
    public static final String USER_NOM_EXTRAT_KEY = "nom";
    public static final String USER_PRENOMS_EXTRAT_KEY = "prenoms";
    public static final String USER_TEL_EXTRAT_KEY = "tel";
    public static final String USER_MDP_EXTRAT_KEY = "mdp";
    public static final String USER_CF_MDP_EXTRAT_KEY = "cnf_mdp";
    //Extrat key end

    //API URL
    public static final String TYPE_OP_KEY = "type_operation";

    public static final String URL_MEDIA_PP = SERVEUR + "/api/v1/utilisateurs/download/pp/";
    public static final String URL_MEDIA_CNI = SERVEUR + "/api/v1/utilisateurs/download/cni/";

    public static final String URL_GET_CUSTOMER_INFO_FROM_QOS = SERVEUR + "/api/v1/utilisateurs/get_data_from_qos";

    public static final String URL_GENERATE_OTP = SERVEUR + "/api/v1/codesotp/generate";
    public static final String URL_VERIFY_OTP = SERVEUR + "/api/v1/codesotp/verify";
    public static final String URL_INSCRIPTION = SERVEUR + "/api/v1/utilisateurs/inscrire";
    public static final String URL_INSCRIPTION_NEXT = SERVEUR +"/api/v1/utilisateurs/registration_next/";
    public static final String URL_NEW_TONTINE = SERVEUR + "/api/v1/tontine/new";
    public static final String URL_HISTORIES = SERVEUR + "/api/v1/utilisateurs/histories";
    public static final String URL_LOGIN = SERVEUR + "/api/v1/utilisateurs/login";
    public static final String URL_PAY_TONTINE = SERVEUR + "/api/v1/tontine/pay_tontine";
    public static final String URL_COMPLETED_TONTINE = SERVEUR + "/api/v1/tontine/completed";
    public static final String URL_COLLECTED_MOMO_TONTINE = SERVEUR + "/api/v1/tontine/withdrawal/momo";
    public static final String URL_INIT_WITHDRAW = SERVEUR + "/api/v1/withdraw/init";
    public static final String URL_PLAINTE_NEW = SERVEUR + "/api/v1/plainte/new";
    public static final String url_refresh_token = SERVEUR + "/api/v1/utilisateurs/refresh-token";
    public static final String URL_ALL_DATA = SERVEUR + "/api/v1/utilisateurs/allData";
    public static final String URL_CHANGE_PWD = SERVEUR + "/api/v1/utilisateurs/change_password";
    public static final String URL_GET_MERCHANT_DETAILS = SERVEUR + "/api/v1/merchant/get_merchant_details";
    public static final String URL_LINK_MERCHANT_TO_USER = SERVEUR + "/api/v1/merchant/link_merchant_to_customer";
    public static final String URL_FREQ_UPDATE_DB = SERVEUR + "/api/v1/utilisateurs/updateDatabase";
    public static final String URL_VALIDATE_FREQ_UPDATE_DB = SERVEUR +"/api/v1/utilisateurs/validateUpdate";
    public static final String URL_RETRY_WITHDRAW = SERVEUR + "/api/v1/withdraw/init/retry";
    public static final String UPDATE_PROFIL_IMAGE = SERVEUR + "/api/v1/utilisateurs/change-pp";

    public static String accessToken = Prefs.getString(TOKEN,"");
    public static String refreshToken = Prefs.getString(REFRESH_TOKEN,"");

}
