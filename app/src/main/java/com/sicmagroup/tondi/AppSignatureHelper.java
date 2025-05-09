package com.sicmagroup.tondi;

import android.content.Context;
import android.content.ContextWrapper;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.util.Base64;
import android.util.Log;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;

public class  AppSignatureHelper extends ContextWrapper {
    public static  final String TAG = AppSignatureHelper.class.getSimpleName();
    public static final String HASH_TYPE = "SHA-256";
    public static final int NUM_HASHED_BYTES = 9;
    public static final int NUM_BASE64_CHAR = 11;
    public AppSignatureHelper(Context base) {
        super(base);
    }

    public ArrayList<String> getAppSignatures(){
        ArrayList<String> appCodes = new ArrayList<>();

        try{
            String packageName = getPackageName();
            PackageManager packageManager = getPackageManager();
            Signature[] signatures = packageManager.getPackageInfo(packageName, PackageManager.GET_SIGNATURES).signatures;


            for (Signature signature : signatures){
                String hash = hash(packageName, signature.toCharsString());
                if (hash != null){
                    appCodes.add(String.format("%s", hash));
                }
            }
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            Log.v(TAG, "Unable to find package to obtain hash.", e);
        }
        
        return appCodes;
    }

    private String hash(String packageName, String signature) {

        String appInfo = packageName + " " + signature;
        try {
            MessageDigest messageDigest = MessageDigest.getInstance(HASH_TYPE);
            messageDigest.update(appInfo.getBytes(StandardCharsets.UTF_8));
            byte[] hashSignature = messageDigest.digest();

            // truncated into NUM_HASHED_BYTES
            hashSignature = Arrays.copyOfRange(hashSignature, 0, NUM_HASHED_BYTES);
            // encode into Base64
            String base64Hash = Base64.encodeToString(hashSignature, Base64.NO_PADDING | Base64.NO_WRAP);
            base64Hash = base64Hash.substring(0, NUM_BASE64_CHAR);

            Log.v(TAG + "sms_sample_test", String.format("pkg: %s -- hash: %s", packageName, base64Hash));
            return base64Hash;
        } catch (NoSuchAlgorithmException e) {
            Log.v(TAG+ "sms_sample_test", "hash:NoSuchAlgorithm", e);
        }
        return null;
    }
}
