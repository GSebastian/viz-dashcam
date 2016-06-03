package com.vizdashcam;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * Created by sebastian on 20/05/16.
 */
public class SharedPreferencesHelper {

    public static final String KEY_DECLINED_AUDIO = "declinedAudoRecordingPermission";

    public static boolean getHasDeclinedAudio(Context context) {
        SharedPreferences prefs = PreferenceManager
                .getDefaultSharedPreferences(context);

        return prefs.getBoolean(KEY_DECLINED_AUDIO, false);
    }

    public static void putHasDeclinedAudio(Context context, Boolean value) {
        SharedPreferences prefs = PreferenceManager
                .getDefaultSharedPreferences(context);

        prefs.edit().putBoolean(KEY_DECLINED_AUDIO, value).apply();
    }

    public static boolean checkBooleanPreferenceValue(Context context, String key, boolean defaultValue) {
        SharedPreferences prefs = PreferenceManager
                .getDefaultSharedPreferences(context);

        return prefs.getBoolean(key, defaultValue);
    }

    public static String checkStringPreferenceValue(Context context, String key, String defaultValue) {
        SharedPreferences prefs = PreferenceManager
                .getDefaultSharedPreferences(context);

        return prefs.getString(key, defaultValue);
    }
}
