package com.vizdashcam.fragments;

import android.content.Context;
import android.os.Bundle;
import android.os.Vibrator;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceFragment;

import com.vizdashcam.GlobalState;
import com.vizdashcam.R;
import com.vizdashcam.utils.FeedbackSoundPlayer;

public class FragmentPreferences extends PreferenceFragment {

	public static final String TAG = "FragmentPreferences";

	private static final String KEY_LP_VIDEO_QUALITY = "defaultCamcorderProfile";
	private static final String KEY_LP_VIDEO_LENGTH = "defaultVideoLength";
	private static final String KEY_CP_LOOP_ACTIVE = "loopModeActive";
	private static final String KEY_CP_SHOCK_ACTIVE = "shockModeActive";
	private static final String KEY_LP_SHOCK_SENSITIVITY = "defaultShockSensitivity";
	private static final String KEY_CP_LONG_PRESS = "longPressToMarkActive";
	private static final String KEY_CP_AUDIO_FEEDBACK_SHOCK = "audioFeedbackShockActive";
	private static final String KEY_CP_AUDIO_FEEDBACK_BUTTON = "audioFeedbackButtonActive";
	private static final String KEY_CP_TACTILE_FEEDBACK = "tactileFeedbackActive";
	private static final String KEY_CP_SPEEDOMETER_ACTIVE = "speedometerActive";
	private static final String KEY_LP_SPEEDOMETER_UNITS = "speedometerUnitsMeasure";

	GlobalState mAppState;

	@SuppressWarnings("unused")
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		FeedbackClickListener feedbackClickListener = new FeedbackClickListener();
		FeedbackPreferenceChangeListener feedbackPreferenceChangeListener = new FeedbackPreferenceChangeListener();

		mAppState = (GlobalState) getActivity().getApplicationContext();

		ListPreference videoQualityList = (ListPreference) findPreference(KEY_LP_VIDEO_QUALITY);
		ListPreference videoLengthList = (ListPreference) findPreference(KEY_LP_VIDEO_LENGTH);
		final CheckBoxPreference useLoopMode = (CheckBoxPreference) findPreference(KEY_CP_LOOP_ACTIVE);
		final CheckBoxPreference shockModeActive = (CheckBoxPreference) findPreference(KEY_CP_SHOCK_ACTIVE);
		final ListPreference shockSensitivity = (ListPreference) findPreference(KEY_LP_SHOCK_SENSITIVITY);
		final CheckBoxPreference longPressToMarkActive = (CheckBoxPreference) findPreference(KEY_CP_LONG_PRESS);
		final CheckBoxPreference audioFeedbackButtonActive = (CheckBoxPreference) findPreference(KEY_CP_AUDIO_FEEDBACK_BUTTON);
		final CheckBoxPreference audioFeedbackShockActive = (CheckBoxPreference) findPreference(KEY_CP_AUDIO_FEEDBACK_SHOCK);
		final CheckBoxPreference tactileFeedbackActive = (CheckBoxPreference) findPreference(KEY_CP_TACTILE_FEEDBACK);
		final CheckBoxPreference speedometerActive = (CheckBoxPreference) findPreference(KEY_CP_SPEEDOMETER_ACTIVE);
		final ListPreference speedometerUnitsMeasure = (ListPreference) findPreference(KEY_LP_SPEEDOMETER_UNITS);

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
					.setSummary("Set video quality. Please stop recording before changing this.");
		} else {
			videoQualityList.setEnabled(true);
			videoQualityList.setSummary("Set video quality");
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
					.setSummary("Set video length. Please stop recording before changing this.");
		} else {
			videoLengthList.setEnabled(true);
			videoLengthList.setSummary("Set video length");
		}
		videoLengthList.setOnPreferenceClickListener(feedbackClickListener);
		videoLengthList
				.setOnPreferenceChangeListener(feedbackPreferenceChangeListener);

		// Loop Mode
		useLoopMode.setDefaultValue(false);
		if (mAppState.isRecording()) {
			useLoopMode.setEnabled(false);
			useLoopMode
					.setSummary("Delete old videos when the phone runs out of storage. Please stop recording before changing this.");
		} else {
			useLoopMode.setEnabled(true);
			useLoopMode
					.setSummary("Delete old videos when the phone runs out of storage");
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
					.setSummary("Mark videos during which a shock occured. This prevents LOOP MODE from deleting these videos. Please stop recording before changing this. ");
		} else {
			shockModeActive.setEnabled(true);
			shockModeActive
					.setSummary("Mark videos during which a shock occured. This prevents LOOP MODE from deleting these videos. ");
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
					.setSummary("Set shock detection sensitivity. Please stop recording before changing this.");
		} else {
			if (shockModeActive.isChecked()) {
				shockSensitivity.setEnabled(true);
			} else {
				shockSensitivity.setEnabled(false);
			}
			shockSensitivity.setSummary("Set shock detection sensitivity");
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
		speedometerActive
				.setOnPreferenceChangeListener(feedbackPreferenceChangeListener);

		// Speedometer Units
		if (speedometerUnitsMeasure.getValue() == null) {
			speedometerUnitsMeasure.setValueIndex(1);
		}
		speedometerUnitsMeasure
				.setOnPreferenceClickListener(feedbackClickListener);
		speedometerUnitsMeasure
				.setOnPreferenceChangeListener(feedbackPreferenceChangeListener);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Load the preferences from an XML resource
		addPreferencesFromResource(R.xml.preference_fragment);

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
}
