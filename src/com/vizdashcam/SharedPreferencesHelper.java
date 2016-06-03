package com.vizdashcam;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.vizdashcam.utils.Constants;

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

    public static boolean checkBooleanValue(Context context, String key, boolean defaultValue) {
        SharedPreferences prefs = PreferenceManager
                .getDefaultSharedPreferences(context);

        return prefs.getBoolean(key, defaultValue);
    }

    public static String checkStringValue(Context context, String key, String defaultValue) {
        SharedPreferences prefs = PreferenceManager
                .getDefaultSharedPreferences(context);

        return prefs.getString(key, defaultValue);
    }

    //region Individual Settings
    public static boolean detectShockModeActive(Context context) {
        return SharedPreferencesHelper.checkBooleanValue(context, Constants.PREF_SHOCK_ACTIVE, Constants
                .PREF_SHOCK_ACTIVE_DEFAULT);
    }

    public static boolean detectLoopModeActive(Context context) {
        return SharedPreferencesHelper.checkBooleanValue(context, Constants.PREF_LOOP_ACTIVE,
                Constants.PREF_LOOP_ACTIVE_DEFAULT);
    }

    public static boolean detectAudioFeedbackButtonActive(Context context) {
        return SharedPreferencesHelper.checkBooleanValue(context, Constants
                .PREF_AUDIO_FEEDBACK_BUTTON, Constants.PREF_AUDIO_FEEDBACK_BUTTON_DEFAULT);
    }

    public static boolean detectAudioFeedbackShockActive(Context context) {
        return SharedPreferencesHelper.checkBooleanValue(context, Constants
                .PREF_AUDIO_FEEDBACK_SHOCK, Constants.PREF_AUDIO_FEEDBACK_SHOCK_DEFAULT);
    }

    public static boolean detectTactileFeedbackActive(Context context) {
        return SharedPreferencesHelper.checkBooleanValue(context, Constants
                .PREF_TACTILE_FEEDBACK, Constants.PREF_TACTILE_FEEDBACK_DEFAULT);
    }

    public static boolean detectLongPressToMarkActive(Context context) {
        return SharedPreferencesHelper.checkBooleanValue(context, Constants.PREF_LONG_PRESS,
                Constants.PREF_LONG_PRESS_DEFAULT);
    }

    public static boolean detectSpeedometerActive(Context context) {
        return SharedPreferencesHelper.checkBooleanValue(context, Constants
                .PREF_SPEEDOMETER_ACTIVE, Constants.PREF_SPEEDOMETER_ACTIVE_DEFAULT);
    }

    public static int detectSpeedometersUnitsMeasure(Context context) {
        String temp = SharedPreferencesHelper.checkStringValue(context, Constants.PREF_SPEEDOMETER_UNITS, Constants
                .PREF_SPEEDOMETER_UNITS_DEFAULT);
        if (temp.compareTo("kph") == 0) {
            return Constants.SPEEDOMETER_KPH;
        } else {
            return Constants.SPEEDOMETER_MPH;
        }
    }

    public static int detectShockSensitivity(Context context) {
        String temp = SharedPreferencesHelper.checkStringValue(context, Constants.PREF_SHOCK_SENSITIVITY, Constants
                .PREF_SHOCK_SENSITIVITY_DEFAULT);
        return Integer.parseInt(temp);
    }

    public static int detectVideoLength(Context context) {
        String temp = SharedPreferencesHelper.checkStringValue(context, Constants.PREF_VIDEO_LENGTH, Constants
                .PREF_VIDEO_LENGTH_DEFAULT);
        return Integer.parseInt(temp);
    }
    //endregion
}
