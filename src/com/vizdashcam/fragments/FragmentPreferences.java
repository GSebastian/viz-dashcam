package com.vizdashcam.fragments;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Vibrator;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceFragment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

import com.vizdashcam.GlobalState;
import com.vizdashcam.R;
import com.vizdashcam.SharedPreferencesHelper;
import com.vizdashcam.activities.ActivitySettings;
import com.vizdashcam.utils.Constants;
import com.vizdashcam.utils.FeedbackSoundPlayer;

public class FragmentPreferences extends PreferenceFragment {

    public static final String TAG = "FragmentPreferences";

    private static final String KEY_RECORDING_AUDIO = "recordingAudio";

    GlobalState mAppState;

    Preference audioRecording;
    CheckBoxPreference speedometerActive;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.preference_fragment);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        FeedbackClickListener feedbackClickListener = new FeedbackClickListener();
        FeedbackPreferenceChangeListener feedbackPreferenceChangeListener = new FeedbackPreferenceChangeListener();

        mAppState = (GlobalState) getActivity().getApplicationContext();

        Activity activity = getActivity();

        ListPreference videoQualityList = (ListPreference) findPreference(Constants.PREF_VIDEO_QUALITY);
        ListPreference videoLengthList = (ListPreference) findPreference(Constants.PREF_VIDEO_LENGTH);
        final CheckBoxPreference useLoopMode = (CheckBoxPreference) findPreference(Constants.PREF_LOOP_ACTIVE);
        final CheckBoxPreference shockModeActive = (CheckBoxPreference) findPreference(Constants.PREF_SHOCK_ACTIVE);
        final ListPreference shockSensitivity = (ListPreference) findPreference(Constants.PREF_SHOCK_SENSITIVITY);
        final CheckBoxPreference longPressToMarkActive = (CheckBoxPreference) findPreference(Constants.PREF_LONG_PRESS);
        final CheckBoxPreference audioFeedbackButtonActive = (CheckBoxPreference) findPreference
                (Constants.PREF_AUDIO_FEEDBACK_BUTTON);
        final CheckBoxPreference audioFeedbackShockActive = (CheckBoxPreference) findPreference
                (Constants.PREF_AUDIO_FEEDBACK_SHOCK);
        final CheckBoxPreference tactileFeedbackActive = (CheckBoxPreference) findPreference(Constants
                .PREF_TACTILE_FEEDBACK);
        speedometerActive = (CheckBoxPreference) findPreference(Constants.PREF_SPEEDOMETER_ACTIVE);
        final ListPreference speedometerUnitsMeasure = (ListPreference) findPreference(Constants
                .PREF_SPEEDOMETER_UNITS);
        audioRecording = findPreference(KEY_RECORDING_AUDIO);

        // Video Quality List
        videoQualityList.setEntries(mAppState.getSupportedVideoQualities());
        videoQualityList.setEntryValues(mAppState
                .getSupportedVideoQualitiesIntegers());
        if (videoQualityList.getValue() == null) {
            videoQualityList.setValueIndex(0);
        }
        if (mAppState.isRecording()) {
            videoQualityList.setEnabled(false);
            videoQualityList
                    .setSummary(R.string.prefs_set_video_quality_stop);
        } else {
            videoQualityList.setEnabled(true);
            videoQualityList.setSummary(R.string.prefs_set_video_quality);
        }
        videoQualityList.setOnPreferenceClickListener(feedbackClickListener);
        videoQualityList
                .setOnPreferenceChangeListener(feedbackPreferenceChangeListener);

        // Video Length List
        if (videoLengthList.getValue() == null) {
            videoLengthList.setValueIndex(2);
        }
        if (mAppState.isRecording()) {
            videoLengthList.setEnabled(false);
            videoLengthList
                    .setSummary(R.string.prefs_set_video_length_stop);
        } else {
            videoLengthList.setEnabled(true);
            videoLengthList.setSummary(R.string.prefs_set_video_length);
        }
        videoLengthList.setOnPreferenceClickListener(feedbackClickListener);
        videoLengthList
                .setOnPreferenceChangeListener(feedbackPreferenceChangeListener);

        // Loop Mode
        useLoopMode.setDefaultValue(false);
        if (mAppState.isRecording()) {
            useLoopMode.setEnabled(false);
            useLoopMode
                    .setSummary(R.string.prefs_delete_old_videos_stop);
        } else {
            useLoopMode.setEnabled(true);
            useLoopMode
                    .setSummary(R.string.prefs_delete_old_videos);
        }
        useLoopMode
                .setOnPreferenceChangeListener(new OnPreferenceChangeListener() {

                    @Override
                    public boolean onPreferenceChange(Preference arg0,
                                                      Object arg1) {
                        audioFeedback();
                        tactileFeedback();

                        return true;
                    }
                });

        // Shock Mode
        shockModeActive.setDefaultValue(false);
        if (mAppState.isRecording()) {
            shockModeActive.setEnabled(false);
            shockModeActive
                    .setSummary(R.string.pref_mark_stop);
        } else {
            shockModeActive.setEnabled(true);
            shockModeActive
                    .setSummary(R.string.pref_mark);
        }
        shockModeActive
                .setOnPreferenceChangeListener(new OnPreferenceChangeListener() {

                    @Override
                    public boolean onPreferenceChange(Preference preference,
                                                      Object newValue) {
                        audioFeedback();
                        tactileFeedback();

                        if (shockModeActive.isChecked()) {
                            audioFeedbackShockActive.setEnabled(false);
                            shockSensitivity.setEnabled(false);
                        } else {
                            audioFeedbackShockActive.setEnabled(true);
                            shockSensitivity.setEnabled(true);
                        }

                        return true;
                    }
                });

        // Shock Sensitivity
        if (shockSensitivity.getValue() == null) {
            shockSensitivity.setValueIndex(1);
        }
        if (mAppState.isRecording()) {
            shockSensitivity.setEnabled(false);
            shockSensitivity
                    .setSummary(R.string.prefs_shock_sensitivity_stop);
        } else {
            if (shockModeActive.isChecked()) {
                shockSensitivity.setEnabled(true);
            } else {
                shockSensitivity.setEnabled(false);
            }
            shockSensitivity.setSummary(R.string.prefs_shock_sensitivity);
        }
        shockSensitivity.setOnPreferenceClickListener(feedbackClickListener);
        shockSensitivity
                .setOnPreferenceChangeListener(feedbackPreferenceChangeListener);

        // Audio Feedback for Shocks
        audioFeedbackShockActive
                .setOnPreferenceClickListener(feedbackClickListener);

        // Audio Feedback for Buttons
        audioFeedbackButtonActive
                .setOnPreferenceClickListener(feedbackClickListener);

        // Tactile Feedback for Buttons
        tactileFeedbackActive
                .setOnPreferenceClickListener(feedbackClickListener);

        // Speedometer
        speedometerActive.setOnPreferenceChangeListener(feedbackPreferenceChangeListener);
        speedometerActive.setOnPreferenceClickListener(new OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                boolean newValue = SharedPreferencesHelper.checkBooleanValue(getActivity(),
                        Constants.PREF_SPEEDOMETER_ACTIVE, false);

                if (newValue && !hasFineLocationPermission()) requestFineLocationPermission();

                return true;
            }
        });

        if (hasFineLocationPermission()) obtainedLocationPermission();
        else lostLocationPermission();

        // Speedometer Units
        if (speedometerUnitsMeasure.getValue() == null)
            speedometerUnitsMeasure.setValueIndex(1);

        speedometerUnitsMeasure
                .setOnPreferenceClickListener(feedbackClickListener);
        speedometerUnitsMeasure
                .setOnPreferenceChangeListener(feedbackPreferenceChangeListener);

        // Audio Recording
        if (hasAudioRecordingPermission()) {
            obtainedAudioRecordingPermission();
        } else {
            lostAudioRecordingPermission();
        }
    }

    private void audioFeedback() {
        if (mAppState.detectAudioFeedbackButtonActive()) {
            FeedbackSoundPlayer.playSound(FeedbackSoundPlayer.SOUND_BTN);
        }
    }

    private void tactileFeedback() {
        if (mAppState.detectTactileFeedbackActive()) {
            Vibrator vibrator = (Vibrator) this.getActivity().getSystemService(
                    Context.VIBRATOR_SERVICE);
            vibrator.vibrate(100);
        }
    }

    class FeedbackClickListener implements OnPreferenceClickListener {

        @Override
        public boolean onPreferenceClick(Preference preference) {
            tactileFeedback();
            audioFeedback();
            return true;
        }
    }

    class FeedbackPreferenceChangeListener implements
            OnPreferenceChangeListener {

        @Override
        public boolean onPreferenceChange(Preference preference, Object newValue) {
            tactileFeedback();
            audioFeedback();
            return true;
        }
    }

    @TargetApi(Build.VERSION_CODES.M)
    private boolean hasAudioRecordingPermission() {
        int permissionResultCheck = ContextCompat.checkSelfPermission(getActivity(), Manifest.permission
                .RECORD_AUDIO);
        return permissionResultCheck == PackageManager.PERMISSION_GRANTED;
    }

    @TargetApi(Build.VERSION_CODES.M)
    private void requestAudioRecordingPermission() {
        ActivityCompat.requestPermissions(getActivity(),
                new String[]{Manifest.permission.RECORD_AUDIO},
                ActivitySettings.CODE_AUDIO_RECORDING_PERMISSION);
    }

    @TargetApi(Build.VERSION_CODES.M)
    private boolean hasFineLocationPermission() {
        int permissionResultCheck = ContextCompat.checkSelfPermission(getActivity(), Manifest.permission
                .ACCESS_FINE_LOCATION);
        return permissionResultCheck == PackageManager.PERMISSION_GRANTED;
    }

    @TargetApi(Build.VERSION_CODES.M)
    private void requestFineLocationPermission() {
        ActivityCompat.requestPermissions(getActivity(),
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                ActivitySettings.CODE_FINE_LOCATION_PERMISSION);
    }

    public void obtainedAudioRecordingPermission() {
        audioRecording.setEnabled(false);
        audioRecording.setSummary(R.string.prefs_sound_recording_activated);
    }

    public void lostAudioRecordingPermission() {
        audioRecording.setEnabled(true);
        audioRecording.setSummary(R.string.prefs_sound_recording_not_activated);
        audioRecording.setOnPreferenceClickListener(new OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                requestAudioRecordingPermission();

                return true;
            }
        });
    }

    public void obtainedLocationPermission() {

        if (SharedPreferencesHelper.checkBooleanValue(getActivity(), Constants.PREF_SPEEDOMETER_ACTIVE,
                false))
            speedometerActive.setChecked(true);
    }

    public void lostLocationPermission() {
        speedometerActive.setChecked(false);
    }
}
