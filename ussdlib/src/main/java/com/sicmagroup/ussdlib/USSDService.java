package com.sicmagroup.ussdlib;

import android.accessibilityservice.AccessibilityService;
import android.app.ActivityManager;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

import com.pixplicity.easyprefs.library.Prefs;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * AccessibilityService for ussd windows on Android mobile Telcom
 *
 * @author Romell Dominguez
 * @version 1.1.c 27/09/2018
 * @since 1.0.a
 */
public class USSDService extends AccessibilityService {

    private static String TAG = USSDService.class.getSimpleName();

    private static AccessibilityEvent event;
    static String ACCESS_RETURN_KEY = "accessibity_return";
    static String ACCESS_BOOL = "accessibity_bool";
    boolean autoriser =false;

    /**
     * Catch widget by Accessibility, when is showing at mobile display
     * @param event AccessibilityEvent
     */
    @Override
    public void onAccessibilityEvent(final AccessibilityEvent event) {
        this.event=event;
        new Prefs.Builder()
                .setContext(this)
                .setMode(ContextWrapper.MODE_PRIVATE)
                .setPrefsName(getPackageName())
                .setUseDefaultSharedPreference(true)
                .build();
        Log.d(TAG, "onAccessibilityEvent");

        Log.d(TAG, String.format(
                "onAccessibilityEvent: [type] %s [class] %s [package] %s [time] %s [text] %s",
                event.getEventType(), event.getClassName(), event.getPackageName(),
                event.getEventTime(), event.getText()));
        if(!Prefs.getBoolean(ACCESS_BOOL, false)){
            //return ;
        }

        if (LoginView(event) && notInputText(event)) {
            // first view or logView, do nothing, pass / FIRST MESSAGE
            clickOnButton(event, 0);
            USSDController.instance.callbackInvoke.over(event.getText().get(0).toString());
        }else if (problemView(event) || LoginView(event)) {
            // deal down
            clickOnButton(event, 1);
            USSDController.instance.callbackInvoke.over(event.getText().get(0).toString());
        }else if (isUSSDWidget(event)) {
            // ready for work
            String response = event.getText().get(0).toString();
            if (response.contains("\n")) {
                response = response.substring(response.indexOf('\n') + 1);
            }
            if (notInputText(event)) {
                // not more input panels / LAST MESSAGE
                /*if(Prefs.getBoolean(ACCESS_BOOL, false)){
                    // sent 'OK' button
                    //clickOnButton(event, 0);
                    USSDController.instance.callbackInvoke.over(response);
                }*/

                // sent 'OK' button
                clickOnButton(event, 0);
                USSDController.instance.callbackInvoke.over(response);

                Handler handler = new Handler();
                final String finalResponse = response;
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if(Prefs.getBoolean(ACCESS_BOOL, false)){
                            USSDController.instance.callbackInvoke.over(finalResponse);
                        }
                    }
                }, 1);


                //
            } else {
                // sent option 1
                if (USSDController.instance.callbackMessage == null)
                    USSDController.instance.callbackInvoke.responseInvoke(response);
                else {
                    USSDController.instance.callbackMessage.responseMessage(response);
                    USSDController.instance.callbackMessage = null;
                }
            }
        }

    }

    /**
     * Send whatever you want via USSD
     * @param text any string
     */
    public static void send(String text) {
        setTextIntoField(event, text);
        clickOnButton(event, 1);
    }

    /**
     * set text into input text at USSD widget
     * @param event AccessibilityEvent
     * @param data Any String
     */
    private static void setTextIntoField(AccessibilityEvent event, String data) {
        USSDController ussdController = USSDController.instance;
        Bundle arguments = new Bundle();
        arguments.putCharSequence(
                AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, data);

        for (AccessibilityNodeInfo leaf : getLeaves(event)) {
            if (leaf.getClassName().equals("android.widget.EditText")
                    && !leaf.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, arguments)) {
                ClipboardManager clipboardManager = ((ClipboardManager) ussdController.context
                        .getSystemService(Context.CLIPBOARD_SERVICE));
                if(clipboardManager != null) {
                    clipboardManager.setPrimaryClip(ClipData.newPlainText("text", data));
                }

                leaf.performAction(AccessibilityNodeInfo.ACTION_PASTE);
            }
        }
    }

    /**
     * Method evaluate if USSD widget has input text
     * @param event AccessibilityEvent
     * @return boolean has or not input text
     */
    protected static boolean notInputText(AccessibilityEvent event) {
        boolean flag = true;
        for (AccessibilityNodeInfo leaf : getLeaves(event)) {
            if (leaf.getClassName().equals("android.widget.EditText")) flag = false;
        }
        return flag;
    }

    /**
     * The AccessibilityEvent is instance of USSD Widget class
     * @param event AccessibilityEvent
     * @return boolean AccessibilityEvent is USSD
     */
    private boolean isUSSDWidget(AccessibilityEvent event) {
        return (event.getClassName().equals("amigo.app.AmigoAlertDialog")
                || event.getClassName().equals("android.app.AlertDialog"));
    }

    /**
     * The View has a login message into USSD Widget
     * @param event AccessibilityEvent
     * @return boolean USSD Widget has login message
     */
    private boolean LoginView(AccessibilityEvent event) {
        new Prefs.Builder()
                .setContext(this)
                .setMode(ContextWrapper.MODE_PRIVATE)
                .setPrefsName(getPackageName())
                .setUseDefaultSharedPreference(true)
                .build();
        /*if (USSDController.isAccessiblityServices(this)){
            return isUSSDWidget(event)
                    && USSDController.instance.map.get(USSDController.KEY_LOGIN)
                    .contains(event.getText().get(0).toString());
        }else{
            return false;
        }*/
        /*boolean foregroud = false;
        try {
            foregroud = new ForegroundCheckTask().execute(USSDService.this).get();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        if (foregroud){
            return isUSSDWidget(event)
                    && USSDController.instance.map.get(USSDController.KEY_LOGIN)
                    .contains(event.getText().get(0).toString());
        }
        return false;*/

        //return  false;
        if(Prefs.getBoolean(ACCESS_BOOL, false)){
            return isUSSDWidget(event)
                    && USSDController.instance.map.get(USSDController.KEY_LOGIN)
                    .contains(event.getText().get(0).toString());
        }else{
            return  false;
        }

        /*if (USSDController.getInstance(USSDService.this)!=null){
            return isUSSDWidget(event)
                    && USSDController.instance.map.get(USSDController.KEY_LOGIN)
                    .contains(event.getText().get(0).toString());
        }*/


    }

    /**
     * The View has a problem message into USSD Widget
     * @param event AccessibilityEvent
     * @return boolean USSD Widget has problem message
     */
    protected boolean problemView(AccessibilityEvent event) {
        new Prefs.Builder()
                .setContext(this)
                .setMode(ContextWrapper.MODE_PRIVATE)
                .setPrefsName(getPackageName())
                .setUseDefaultSharedPreference(true)
                .build();

        if(Prefs.getBoolean(ACCESS_BOOL, false)) {
            return isUSSDWidget(event)
                    && USSDController.instance.map.get(USSDController.KEY_ERROR)
                    .contains(event.getText().get(0).toString());
        }else{
            return  false;
        }
    }

    /**
     * click a button using the index
     * @param event AccessibilityEvent
     * @param index button's index
     */
    protected static void clickOnButton(AccessibilityEvent event,int index) {
        int count = -1;
        for (final AccessibilityNodeInfo leaf : getLeaves(event)) {
            if (leaf.getClassName().toString().toLowerCase().contains("button")) {
                count++;
                if (count == index) {
                    Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            if(Prefs.getBoolean(ACCESS_BOOL, false)){
                                leaf.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                            }
                        }
                    }, 1);


                }
            }
        }
    }

    private static List<AccessibilityNodeInfo> getLeaves(AccessibilityEvent event) {
        List<AccessibilityNodeInfo> leaves = new ArrayList<>();
        if (event.getSource() != null) {
            getLeaves(leaves, event.getSource());
        }

        return leaves;
    }

    private static void getLeaves(List<AccessibilityNodeInfo> leaves, AccessibilityNodeInfo node) {
        if (node.getChildCount() == 0) {
            leaves.add(node);
            return;
        }

        for (int i = 0; i < node.getChildCount(); i++) {
            getLeaves(leaves, node.getChild(i));
        }
    }

    /**
     * Active when SO interrupt the application
     */
    @Override
    public void onInterrupt() {
        //Log.d(TAG, "onInterrupt");
    }

    /**
     * Configure accessibility server from Android Operative System
     */
    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        Log.d(TAG, "onServiceConnected");

        new Prefs.Builder()
                .setContext(USSDService.this)
                .setMode(ContextWrapper.MODE_PRIVATE)
                .setPrefsName(getPackageName())
                .setUseDefaultSharedPreference(true)
                .build();
        /*ScheduledExecutorService scheduleTaskExecutor = Executors.newScheduledThreadPool(1);
        scheduleTaskExecutor.scheduleAtFixedRate(new Runnable() {
            public void run() {
                serviceChecker();
            }
        }, 0, 1, TimeUnit.SECONDS);*/

        String finalStr_class = Prefs.getString(ACCESS_RETURN_KEY,"");
        Intent i = new Intent();
        i.setClassName(getApplicationContext(), finalStr_class);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        if (!Prefs.getString(ACCESS_RETURN_KEY, "").equals("")) {
            startActivity(i);
        }


        /*Intent dialogIntent = new Intent(this, MyActivity.class);
        dialogIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(dialogIntent);*/
    }

    /*private void serviceChecker(){
        autoriser = Prefs.getBoolean(ACCESS_BOOL, false);
        Log.d("checker_access_bool", "value:"+Prefs.getBoolean(ACCESS_BOOL, false));
    }*/

    protected Boolean isActivityRunning(Class activityClass)
    {
        ActivityManager activityManager = (ActivityManager) getBaseContext().getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningTaskInfo> tasks = activityManager.getRunningTasks(Integer.MAX_VALUE);

        for (ActivityManager.RunningTaskInfo task : tasks) {
            if (activityClass.getCanonicalName().equalsIgnoreCase(task.baseActivity.getClassName()))
                return true;
        }

        return false;
    }

    class ForegroundCheckTask extends AsyncTask<Context, Void, Boolean> {

        @Override
        protected Boolean doInBackground(Context... params) {
            final Context context = params[0].getApplicationContext();
            return isAppOnForeground(context);
        }

        private boolean isAppOnForeground(Context context) {
            ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
            List<ActivityManager.RunningAppProcessInfo> appProcesses = activityManager.getRunningAppProcesses();
            if (appProcesses == null) {
                return false;
            }
            final String packageName = context.getPackageName();
            for (ActivityManager.RunningAppProcessInfo appProcess : appProcesses) {
                if (appProcess.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND && appProcess.processName.equals(packageName)) {
                    return true;
                }
            }
            return false;
        }
    }

}