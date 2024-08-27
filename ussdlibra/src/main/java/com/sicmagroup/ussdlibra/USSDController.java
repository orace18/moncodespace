package com.sicmagroup.ussdlibra;

import android.accessibilityservice.AccessibilityServiceInfo;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.net.Uri;
import android.preference.PreferenceActivity;
import android.provider.Settings;
import android.text.TextUtils;
import android.view.accessibility.AccessibilityManager;
import android.widget.Toast;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;

/**
 *
 * @author Romell Dominguez
 * @version 1.1.c 27/09/2018
 * @since 1.0.a
 */
public class USSDController implements USSDInterface{

    protected static USSDController instance;

    protected Context context;

    protected HashMap<String,HashSet<String>> map;

    protected CallbackInvoke callbackInvoke;

    protected CallbackMessage callbackMessage;

    protected static final String KEY_LOGIN = "KEY_LOGIN";

    protected static final String KEY_ERROR = "KEY_ERROR";

    private USSDInterface ussdInterface;

    /**
     * The Singleton building method
     * @param context An activity that could call
     * @return An instance of USSDController
     */
    public static USSDController getInstance(Context context) {
        if (instance == null)
            instance = new USSDController(context);
        return instance;
    }

    private USSDController(Context context) {
        ussdInterface = this;
        this.context = context;
    }

    /**
     * Invoke a dial-up calling a ussd number
     * @param ussdPhoneNumber ussd number
     * @param map Map of Login and problem messages
     * @param callbackInvoke a callback object from return answer
     */
    @SuppressLint("MissingPermission")
    public void callUSSDInvoke(String ussdPhoneNumber, HashMap<String,HashSet<String>> map, CallbackInvoke callbackInvoke) {
        HashMap<String,HashSet<String>> map1 = new HashMap<>();
        map1.put("KEY_LOGIN", new HashSet<>(Arrays.asList("espere", "waiting", "loading", "esperando")));
        map1.put("KEY_ERROR", new HashSet<>(Arrays.asList("problema", "problem", "error", "null")));
        this.callbackInvoke = callbackInvoke;
        this.map = map1;

        if (map==null || (map!=null && (!map.containsKey(KEY_ERROR) || !map.containsKey(KEY_LOGIN)) )){
            callbackInvoke.over("Bad Mapping structure");
            return;
        }
        // code initial ussdPhoneNumber.isEmpty()
        if (ussdPhoneNumber==null || ussdPhoneNumber.isEmpty()) {
            callbackInvoke.over("Mauvais code ussd");
            return;
        }
        if (verifyAccesibilityAccess(context)) {
            String uri = Uri.encode("#");
            if (uri != null)
                ussdPhoneNumber = ussdPhoneNumber.replace("#", uri);
            Uri uriPhone = Uri.parse("tel:" + ussdPhoneNumber);
            if (uriPhone != null)
                context.startActivity(new Intent(Intent.ACTION_CALL, uriPhone));
        }
    }

    @Override
    public void sendData(String text) {
        USSDService.send(text);
    }

    public void send(String text, CallbackMessage callbackMessage){
        this.callbackMessage = callbackMessage;
        ussdInterface.sendData(text);
    }

    public static boolean verifyAccesibilityAccess(Context context) {
        boolean isEnabled = USSDController.isAccessiblityServicesEnable(context);
        if (!isEnabled) {
            if(context instanceof Activity) {
                openSettingsAccessibility((Activity) context);
            } else {
                Toast.makeText(
                        context,
                        "Le service d'accessibility n'est pas activé",
                        Toast.LENGTH_LONG
                ).show();

            }
        }
        return isEnabled;
    }

    private static void openSettingsAccessibility(final Activity activity) {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(activity);
        alertDialogBuilder.setTitle("Permission d'accessibilité");
        ApplicationInfo applicationInfo = activity.getApplicationInfo();
        int stringId = applicationInfo.labelRes;
        String name = applicationInfo.labelRes == 0 ?
                applicationInfo.nonLocalizedLabel.toString() : activity.getString(stringId);
        alertDialogBuilder
                .setMessage("Tondi a besoin d'exécuter les codes USSD pour les paiements de vos mises en mode déconnecté de l'internet.");
        alertDialogBuilder.setCancelable(false);
        alertDialogBuilder.setNeutralButton("Continuer pour activer", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // lance lassistant
                //Toast.makeText(activity, "lancer lassistant", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent();
                intent.setClassName("com.android.settings",
                        "com.android.settings.Settings");
                intent.setAction(Intent.ACTION_MAIN);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                        | Intent.FLAG_ACTIVITY_CLEAR_TASK
                        | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
                intent.putExtra(PreferenceActivity.EXTRA_SHOW_FRAGMENT,
                        "com.android.settings.accessibility.AccessibilitySettings");
                intent.putExtra(PreferenceActivity.EXTRA_SHOW_FRAGMENT_ARGUMENTS,
                        "com.android.settings.accessibility.AccessibilitySettings");
                activity.startActivityForResult(intent, 1);
                activity.startActivityForResult(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS), 1);
            }
        });
        AlertDialog alertDialog = alertDialogBuilder.create();
        if (alertDialog != null) {
            alertDialog.show();
        }
    }


    protected static boolean isAccessiblityServicesEnable(Context context) {
        AccessibilityManager am = (AccessibilityManager) context
                .getSystemService(Context.ACCESSIBILITY_SERVICE);
        if (am != null) {
            for (AccessibilityServiceInfo service : am.getInstalledAccessibilityServiceList()) {
                if (service.getId().contains(context.getPackageName())) {
                    return USSDController.isAccessibilitySettingsOn(context, service.getId());
                }
            }
        }
        return false;
    }

    public static boolean isAccessibilitySettingsOn(Context context, final String service) {
        int accessibilityEnabled = 0;
        try {
            accessibilityEnabled = Settings.Secure.getInt(
                    context.getApplicationContext().getContentResolver(),
                    Settings.Secure.ACCESSIBILITY_ENABLED);
        } catch (Settings.SettingNotFoundException e) {
            //
        }
        if (accessibilityEnabled == 1) {
            String settingValue = Settings.Secure.getString(
                    context.getApplicationContext().getContentResolver(),
                    Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES);
            if (settingValue != null) {
                TextUtils.SimpleStringSplitter splitter = new TextUtils.SimpleStringSplitter(':');
                splitter.setString(settingValue);
                while (splitter.hasNext()) {
                    String accessabilityService = splitter.next();
                    if (accessabilityService.equalsIgnoreCase(service)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public interface CallbackInvoke {
        void responseInvoke(String message);
        void over(String message);
    }

    public interface CallbackMessage {
        void responseMessage(String message);
    }

    public void destroyInstance(){
        instance = null;
    }
}
