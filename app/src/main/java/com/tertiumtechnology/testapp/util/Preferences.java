package com.tertiumtechnology.testapp.util;

import android.content.Context;
import android.content.SharedPreferences;

import com.tertiumtechnology.api.rfidpassiveapilib.util.BleSettings;
import com.tertiumtechnology.testapp.R;

public class Preferences {

    static final String PREF_CONNECTION_TIMEOUT = "com.tertiumtechnology.testapp.PREF_CONNECTION_TIMEOUT";
    static final String PREF_WRITE_TIMEOUT = "com.tertiumtechnology.testapp.PREF_WRITE_TIMEOUT";
    static final String PREF_FIRST_READ_TIMEOUT = "com.tertiumtechnology.testapp.PREF_FIRST_READ_TIMEOUT";
    static final String PREF_LATER_READ_TIMEOUT = "com.tertiumtechnology.testapp.PREF_LATER_READ_TIMEOUT";

    public static BleSettings getBleSettings(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(context.getString(R.string
                .prefs_name), Context.MODE_PRIVATE);
        long connectionTimeout = sharedPreferences.getLong(PREF_CONNECTION_TIMEOUT, BleSettings.getDefaultBleSettings()
                .getConnectTimeout());
        long writeTimeout = sharedPreferences.getLong(PREF_WRITE_TIMEOUT, BleSettings.getDefaultBleSettings()
                .getWriteTimeout());
        long firstReadTimeout = sharedPreferences.getLong(PREF_FIRST_READ_TIMEOUT, BleSettings.getDefaultBleSettings()
                .getFirstReadTimeout());
        long laterReadTimeout = sharedPreferences.getLong(PREF_LATER_READ_TIMEOUT, BleSettings.getDefaultBleSettings()
                .getLaterReadTimeout());

        return new BleSettings(connectionTimeout, writeTimeout, firstReadTimeout, laterReadTimeout);
    }

    public static void saveBleSettings(Context context, BleSettings bleSettings) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(context.getString(R.string
                .prefs_name), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putLong(PREF_CONNECTION_TIMEOUT, bleSettings.getConnectTimeout());
        editor.putLong(PREF_WRITE_TIMEOUT, bleSettings.getWriteTimeout());
        editor.putLong(PREF_FIRST_READ_TIMEOUT, bleSettings.getFirstReadTimeout());
        editor.putLong(PREF_LATER_READ_TIMEOUT, bleSettings.getLaterReadTimeout());
        editor.apply();
    }
}