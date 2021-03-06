package com.mrblasto.gcmchatclient;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by jeffreyfried on 4/4/15.
 */
public class Common extends Application {
    // change this to your web service's endpoint
    private static String baseUrl = "http://10.0.0.4:3000";


    private static final String PROPERTY_APP_VERSION = "appVersion";

    private static String ACCOUNT_NAME = "accountName";
    private static String ACCOUNT_EMAIL = "accountEmail";
    private static String AUTH_TOKEN = "authToken";
    private static String GCM_REGID = "gcmRegid";

    public final static String REGID = "regid";
    public final static String EMAIL = "email";
    public final static String MESSAGE = "message";
    public final static String TO = "to";
    public final static String FROM = "from";
    public final static String NAME = "name";

    private final static String registerUrl = baseUrl + "/register";
    private static String currentContact = "";
    private static String lastFragment = "";

    public static String getLastFragment() {
        return lastFragment;
    }

    public static void setLastFragment(String lastFragment) {
        Common.lastFragment = lastFragment;
    }

    public static String getCurrentContact() {
        return currentContact;
    }

    public static void setCurrentContact(String currentContact) {
        Common.currentContact = currentContact;
    }



    @Override
    public void onCreate() {
        super.onCreate();
    }

    public static String getServerUrl() {
        return baseUrl;

    }

    public static String getRegisterUrl() {
        return registerUrl;
    }

    private static SharedPreferences getSharedPreferences(Context context) {
        return context.getSharedPreferences(MainActivity.class.getSimpleName(),
                Context.MODE_PRIVATE);
    }
    public static String getAccountEmail(Context context) {
        SharedPreferences prefs = getSharedPreferences(context);
        return prefs.getString(ACCOUNT_EMAIL, null);
    }
    public static void setAccountEmail(Context context, String value) {
        SharedPreferences prefs = getSharedPreferences(context);
        SharedPreferences.Editor editor=prefs.edit();
        editor.putString(ACCOUNT_EMAIL, value);
        editor.commit();
    }
    public static String getGCMRegId(Context context) {
        SharedPreferences prefs = getSharedPreferences(context);
        return prefs.getString(GCM_REGID, null);
    }
    public static void setGCMRegId(Context context, String value) {
        SharedPreferences prefs = getSharedPreferences(context);
        SharedPreferences.Editor editor=prefs.edit();
        editor.putString(GCM_REGID, value);
        editor.commit();
    }
    public static String getAccountName(Context context) {
        SharedPreferences prefs = getSharedPreferences(context);
        return prefs.getString(ACCOUNT_NAME, "");
    }
    public static void setAccountName(Context context, String value) {
        SharedPreferences prefs = getSharedPreferences(context);
        SharedPreferences.Editor editor=prefs.edit();
        editor.putString(ACCOUNT_NAME, value);
        editor.commit();
    }

    public static int getRegisteredAppVersion(Context context) {
        SharedPreferences prefs = getSharedPreferences(context);
        return prefs.getInt(PROPERTY_APP_VERSION, Integer.MIN_VALUE);
    }

    public static void setRegisteredAppVersion(Context context, int value) {
        SharedPreferences prefs = getSharedPreferences(context);
        SharedPreferences.Editor editor=prefs.edit();
        editor.putInt(PROPERTY_APP_VERSION, value);
        editor.commit();
    }

}
