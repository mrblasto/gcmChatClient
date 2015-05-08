package com.example.chatdemo;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;
import com.example.chatdemo.gcm.RegisterWithGCMServer;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.gcm.GoogleCloudMessaging;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

public class MainActivity extends Activity implements RegisterWithGCMServer.RegisterListener{
    public static final String EXTRA_MESSAGE = "message";
    public static final String PROPERTY_REG_ID = "registration_id";


    private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    private String PROJECT_NUMBER = "1019696244371";
    static final String TAG = "MainActivity";

    TextView mDisplay;
    GoogleCloudMessaging gcm;
    AtomicInteger msgId = new AtomicInteger();
    SharedPreferences prefs;
    Context context;
    String regid;
    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);


        if (savedInstanceState == null) {
            if(hasAccount()) {
                Log.w("OnCreate", "hasAccount");
                getFragmentManager()
                        .beginTransaction()

                        .add(R.id.fragmentParentViewGroup, new TestFragment())
                        .commit();
            }
            else {
                Log.w("OnCreate", "needs account");
                Intent intent = new Intent(this, AccountActivity.class);
                startActivity(intent);
                finish();
            }
        }

        context = getApplicationContext();

        doGCMRegistration();

    }

    // You need to do the Play Services APK check here too.
    @Override
    protected void onResume() {
        Log.w("onResume", "onResume");
        super.onResume();
        doGCMRegistration();

//        if(regid != null && !regid.isEmpty()) {
//            //Already registered, just send registration
//            new Register(Common.getEmail(), regid, MainActivity.this).execute();
//            Toast.makeText(MainActivity.this, "Stored regid from onResume: " + regid, Toast.LENGTH_SHORT).show();
//        }
//        else {
//            getRegisterIdInBackground();
//        }
//        checkPlayServices();
    }

    /**
    * Checks for google play services, then gets registration id and registraters with server if necessary
     */
    private void doGCMRegistration() {
        // Check device for Play Services APK. If check succeeds, proceed with
        //  GCM registration.
        if (checkPlayServices()) {
            gcm = GoogleCloudMessaging.getInstance(this);
            regid = getGCMRegistrationId();

            Log.i(TAG, "regid from on Create: " + regid);

            if (regid == null || regid.isEmpty()) {
                getGCMRegisterIdInBackground();
            }
            else {
                //already registered, just send registration
                //new RegisterWithGCMServer(Common.getEmail(), regid, MainActivity.this).execute();
                Toast.makeText(MainActivity.this, "Stored regid from preferences: " + regid, Toast.LENGTH_SHORT).show();
            }
        } else {
            Log.i(TAG, "No valid Google Play Services APK found.");
        }
    }
    /**
     * Check the device to make sure it has the Google Play Services APK. If
     * it doesn't, display a dialog that allows users to download the APK from
     * the Google Play Store or enable it in the device's system settings.
     */
    private boolean checkPlayServices() {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                GooglePlayServicesUtil.getErrorDialog(resultCode, this,
                        PLAY_SERVICES_RESOLUTION_REQUEST).show();
            } else {
                Log.i(TAG, "This device is not supported.");
                finish();
            }
            return false;
        }
        return true;
    }

    public void getGCMRegisterIdInBackground() {
        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... params) {
                String msg = "";
                try {
                    if (gcm == null) {
                        gcm = GoogleCloudMessaging.getInstance(getApplicationContext());
                    }
                    regid = gcm.register(PROJECT_NUMBER);
                    msg = "Device registered, registration ID=" + regid;
                    Log.i("GCM", msg);

                } catch (IOException ex) {
                    msg = "Error :" + ex.getMessage();

                }
                return msg;
            }

            @Override
            protected void onPostExecute(String msg) {
                Common.setGCMRegId(MainActivity.this, MainActivity.this.regid);
                new RegisterWithGCMServer(Common.getAccountName(MainActivity.this)
                        ,Common.getAccountEmail(MainActivity.this), regid, MainActivity.this).execute();
                Toast.makeText(MainActivity.this, "regid from gcm: " + regid, Toast.LENGTH_SHORT).show();

            }
        }.execute(null, null, null);
    }


    /**
     * Gets the current registration ID for application on GCM service.
     * <p>
     * If result is empty, the app needs to register.
     *
     * @return registration ID, or empty string if there is no existing
     *         registration ID.
     */
    private String getGCMRegistrationId() {
        String registrationId = Common.getGCMRegId(this);
        if (registrationId == null || registrationId.isEmpty()) {
            Log.i(TAG, "Registration not found.");
            return "";
        }
        // Check if app was updated; if so, it must clear the registration ID
        // since the existing registration ID is not guaranteed to work with
        // the new app version.
        int registeredVersion = Common.getRegisteredAppVersion(this);
        int currentVersion = getAppVersion(context);
        if (registeredVersion != currentVersion) {
            Common.setRegisteredAppVersion(this, currentVersion);
            Log.i(TAG, "App version changed.");

            return "";
        }
        return registrationId;
    }

    /**
     * @return Application's {@code SharedPreferences}.
     */
    private SharedPreferences getGCMPreferences(Context context) {
        // This sample app persists the registration ID in shared preferences, but
        // how you store the registration ID in your app is up to you.
        return getSharedPreferences(MainActivity.class.getSimpleName(),
                Context.MODE_PRIVATE);
    }

    /**
     * @return Application's version code from the {@code PackageManager}.
     */
    private static int getAppVersion(Context context) {
        try {
            PackageInfo packageInfo = context.getPackageManager()
                    .getPackageInfo(context.getPackageName(), 0);
            return packageInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            // should never happen
            throw new RuntimeException("Could not get package name: " + e);
        }
    }

    @Override
    public Activity getRegisterActivity() {
        return this;
    }

    @Override
    public void onPostRegisterExecute(RegisterWithGCMServer.Result result) {
        String msg = !result.status.equalsIgnoreCase("error")
                        ? getResources().getString(R.string.registrationSuccess)
                        : result.error;
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    // --------------------- Account check
    private boolean hasAccount() {
        String accountEmail = Common.getAccountEmail(this);
        if(accountEmail == null ) {
            Log.i("MainActivity", "account email is null");
            return false;
        }
        else {
            Log.i("MainActivity", "account is " + accountEmail);
            return true;
        }
    }

}
