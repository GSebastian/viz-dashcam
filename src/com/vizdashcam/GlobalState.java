package com.vizdashcam;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import android.app.Application;
import android.content.SharedPreferences;
import android.hardware.Camera;
import android.media.CamcorderProfile;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.util.Log;
import android.util.Pair;

import com.vizdashcam.fragments.FragmentAllVideos;
import com.vizdashcam.fragments.FragmentMarkedVideos;
import com.vizdashcam.utils.FeedbackSoundPlayer;

public class GlobalState extends Application {
	private static final String TAG = "GlobalState";

	private File mediaStorageDir = new File(
			Environment.getExternalStorageDirectory(), "vizDashcamApp");

	public static final float SHOCK_SENSITIVITY_LOW = 5;
	public static final float SHOCK_SENSITIVITY_MEDIUM = 7;
	public static final float SHOCK_SENSITIVITY_HIGH = 11;

	public static final int SPEEDOMETER_MPH = 0;
	public static final int SPEEDOMETER_KPH = 1;

	private Pair<Integer, Integer> lastFeedbackCoords;

	private String lastFilename;
	private String lastMarkedFilename;
	public static final String KEY_MARKED_FILES = "MARKED";

	private boolean recording = false;
	private boolean loggingEnabled = false;
	private boolean previewBound = false;
	private boolean activityPaused = false;
	private boolean previewActive = false;
	private boolean splashscreenOpen = true;

	private boolean supports1080p;
	private boolean supports720p;
	private boolean supports480p;
	private boolean supportsCIF;
	private boolean supportsQCIF;
	private boolean supportsQVGA;

	private int defaultCamcorderProfile;
	private int defaultVideoLength;
	private boolean loopModeActive;
	private boolean shockModeActive;
	private boolean audioFeedbackButtonActive;
	private boolean audioFeedbackShockActive;
	private boolean tactileFeedbackActive;
	private int defaultShockSensitivity;
	private boolean longPressToMarkActive;
	private boolean speedometerActive;
	private int defaultSpeedometerUnitsMeasure;

	private FragmentAllVideos allVideosFragment = null;
	private FragmentMarkedVideos markedVideosFragment = null;

	private boolean mustMarkFile;

	@Override
	public void onCreate() {
		super.onCreate();
		createVideoFolder();
		detectSupportedCamcorderProfiles();
		FeedbackSoundPlayer.init(this);
		new VideoPreview(this);
		Locale.setDefault(Locale.US);
	}

	public void createVideoFolder() {
		File mediaStorageDir = new File(
				Environment.getExternalStorageDirectory(), "vizDashcamApp");

		if (!mediaStorageDir.exists()) {
			if (!mediaStorageDir.mkdirs()) {
				Log.e(TAG, "Failed to create directory");
			}
		}
	}

	public void initializeCameraParams() {
		setDefaultCamcorderProfile();
		setDefaultVideoLength();
		setDefaultShockSensitivity();
	}

	public void setLastFeedbackCoords(Pair<Integer, Integer> coords) {
		this.lastFeedbackCoords = coords;
	}

	public Pair<Integer, Integer> getLastFeedbackCoords() {
		return lastFeedbackCoords;
	}

	public String getLastFilename() {
		return lastFilename;
	}

	public void setLastFilename(String lastFilename) {
		this.lastFilename = lastFilename;
		if (this.lastMarkedFilename == null) {
			this.lastMarkedFilename = "";
		}
	}

	public String getLastMarkedFilename() {
		return lastMarkedFilename;
	}

	public void setLastMarkedFilename(String lastFilename) {
		this.lastMarkedFilename = lastFilename;
	}

	public void setPreviewBound(boolean previewBound) {
		this.previewBound = previewBound;
	}

	public boolean isPreviewBound() {
		return this.previewBound;
	}

	public boolean isActivityPaused() {
		return activityPaused;
	}

	public void setActivityPaused(boolean activityPaused) {
		this.activityPaused = activityPaused;
	}

	public boolean isPreviewActive() {
		return previewActive;
	}

	public void setPreviewActive(boolean previewActive) {
		this.previewActive = previewActive;
	}

	public boolean isLoopModeActive() {
		return loopModeActive;
	}

	public void setLoopModeActive(boolean loopModeActive) {
		this.loopModeActive = loopModeActive;
	}

	public int getDefaultCamcorderProfile() {
		return defaultCamcorderProfile;
	}

	public void setDefaultCamcorderProfile(int defaultCamcorderProfile) {
		this.defaultCamcorderProfile = defaultCamcorderProfile;
	}

	public boolean isRecording() {
		return this.recording;
	}

	public boolean isLoggingEnabled() {
		return this.loggingEnabled;
	}

	public void setRecording(boolean recording) {
		this.recording = recording;

		if (this.isLoggingEnabled())
			Log.v(TAG, "setRecording");
	}

	public int getDefaultVideoLength() {
		return defaultVideoLength;
	}

	public void setDefaultVideoLength(int defaultVideoLength) {
		this.defaultVideoLength = defaultVideoLength;
	}

	public int getDefaultShockSensitivity() {
		return defaultShockSensitivity;
	}

	private void detectSupportedCamcorderProfiles() {
		SharedPreferences camcorderProfiles = PreferenceManager
				.getDefaultSharedPreferences(this);

		if (!camcorderProfiles.contains("1080p")) {
			saveSupportedCamcorderProfiles();
		}

		supports1080p = camcorderProfiles.getBoolean("1080p", false);
		supports720p = camcorderProfiles.getBoolean("720p", false);
		supports480p = camcorderProfiles.getBoolean("480p", false);

		supportsCIF = camcorderProfiles.getBoolean("CIF", false);
		supportsQCIF = camcorderProfiles.getBoolean("QCIF", false);
		supportsQVGA = camcorderProfiles.getBoolean("QVGA", false);

	}

	private void saveSupportedCamcorderProfiles() {
		SharedPreferences camcorderProfiles = PreferenceManager
				.getDefaultSharedPreferences(this);
		SharedPreferences.Editor camcorderProfilesEditor = camcorderProfiles
				.edit();

		// 1920 x 1080 || 1920 x 1088
		if (CamcorderProfile.hasProfile(CamcorderProfile.QUALITY_1080P))
			camcorderProfilesEditor.putBoolean("1080p", true);
		else
			camcorderProfilesEditor.putBoolean("1080p", false);

		// 640 × 480 || 720 x 480 || 704 x 480
		if (CamcorderProfile.hasProfile(CamcorderProfile.QUALITY_480P)) {
			camcorderProfilesEditor.putBoolean("480p", true);
		} else
			camcorderProfilesEditor.putBoolean("480p", false);

		// 1280 x 720
		if (CamcorderProfile.hasProfile(CamcorderProfile.QUALITY_720P))
			camcorderProfilesEditor.putBoolean("720p", true);
		else
			camcorderProfilesEditor.putBoolean("720p", false);

		// 352 × 288
		if (CamcorderProfile.hasProfile(CamcorderProfile.QUALITY_CIF))
			camcorderProfilesEditor.putBoolean("CIF", true);
		else
			camcorderProfilesEditor.putBoolean("CIF", false);

		// 320 x 240
		if (CamcorderProfile.hasProfile(CamcorderProfile.QUALITY_QVGA))
			camcorderProfilesEditor.putBoolean("QVGA", true);
		else
			camcorderProfilesEditor.putBoolean("QVGA", false);

		// 176 x 144
		if (CamcorderProfile.hasProfile(CamcorderProfile.QUALITY_QCIF))
			camcorderProfilesEditor.putBoolean("QCIF", true);
		else
			camcorderProfilesEditor.putBoolean("QCIF", false);

		camcorderProfilesEditor.apply();
	}

	public Camera.Size getPreviewSize(List<Camera.Size> sizes, int w, int h) {
		final double ASPECT_TOLERANCE = 0.1;
		double targetRatio = (double) h / w;

		if (sizes == null)
			return null;

		Camera.Size optimalSize = null;
		double minDiff = Double.MAX_VALUE;

		int targetHeight = h;

		for (Camera.Size size : sizes) {
			double ratio = (double) size.width / size.height;
			if (Math.abs(ratio - targetRatio) > ASPECT_TOLERANCE)
				continue;
			if (Math.abs(size.height - targetHeight) < minDiff) {
				optimalSize = size;
				minDiff = Math.abs(size.height - targetHeight);
			}
		}

		if (optimalSize == null) {
			minDiff = Double.MAX_VALUE;
			for (Camera.Size size : sizes) {
				if (Math.abs(size.height - targetHeight) < minDiff) {
					optimalSize = size;
					minDiff = Math.abs(size.height - targetHeight);
				}
			}
		}
		return optimalSize;
	}

	// public Pair<Integer, Integer> getPreviewSize2(int camcorderProfile) {
	//
	// if (camcorderProfile == CamcorderProfile.QUALITY_1080P) {
	// int width, height;
	// width =
	// CamcorderProfile.get(CamcorderProfile.QUALITY_1080P).videoFrameWidth;
	// height =
	// CamcorderProfile.get(CamcorderProfile.QUALITY_1080P).videoFrameHeight;
	// return new Pair<Integer, Integer>(width, height);
	// } else if (camcorderProfile == CamcorderProfile.QUALITY_720P) {
	// int width, height;
	// width =
	// CamcorderProfile.get(CamcorderProfile.QUALITY_720P).videoFrameWidth;
	// height =
	// CamcorderProfile.get(CamcorderProfile.QUALITY_720P).videoFrameHeight;
	// return new Pair<Integer, Integer>(width, height);
	// } else if (camcorderProfile == CamcorderProfile.QUALITY_480P) {
	// int width, height;
	// width =
	// CamcorderProfile.get(CamcorderProfile.QUALITY_480P).videoFrameWidth;
	// height =
	// CamcorderProfile.get(CamcorderProfile.QUALITY_480P).videoFrameHeight;
	// return new Pair<Integer, Integer>(width, height);
	// } else if (camcorderProfile == CamcorderProfile.QUALITY_CIF) {
	// int width, height;
	// width =
	// CamcorderProfile.get(CamcorderProfile.QUALITY_CIF).videoFrameWidth;
	// height =
	// CamcorderProfile.get(CamcorderProfile.QUALITY_CIF).videoFrameHeight;
	// return new Pair<Integer, Integer>(width, height);
	// } else if (camcorderProfile == CamcorderProfile.QUALITY_QVGA) {
	// int width, height;
	// width =
	// CamcorderProfile.get(CamcorderProfile.QUALITY_QVGA).videoFrameWidth;
	// height =
	// CamcorderProfile.get(CamcorderProfile.QUALITY_QVGA).videoFrameHeight;
	// return new Pair<Integer, Integer>(width, height);
	// } else if (camcorderProfile == CamcorderProfile.QUALITY_QCIF) {
	// int width, height;
	// width =
	// CamcorderProfile.get(CamcorderProfile.QUALITY_QCIF).videoFrameWidth;
	// height =
	// CamcorderProfile.get(CamcorderProfile.QUALITY_QCIF).videoFrameHeight;
	// return new Pair<Integer, Integer>(width, height);
	// }
	// return new Pair<Integer, Integer>(-1, -1);
	// }

	public CharSequence[] getSupportedVideoQualities() {

		ArrayList<String> temp = new ArrayList<String>();

		if (supports1080p)
			temp.add("1080p");
		if (supports720p)
			temp.add("720p");
		if (supports480p)
			temp.add("480p");
		if (supportsCIF)
			temp.add("CIF");
		if (supportsQCIF)
			temp.add("QCIF");
		if (supportsQVGA)
			temp.add("QVGA");

		CharSequence[] result = temp.toArray(new CharSequence[temp.size()]);
		return result;
	}

	public CharSequence[] getSupportedVideoQualitiesIntegers() {

		ArrayList<String> temp = new ArrayList<String>();

		if (supports1080p)
			temp.add(Integer.toString(CamcorderProfile.QUALITY_1080P));
		if (supports720p)
			temp.add(Integer.toString(CamcorderProfile.QUALITY_720P));
		if (supports480p)
			temp.add(Integer.toString(CamcorderProfile.QUALITY_480P));
		if (supportsCIF)
			temp.add(Integer.toString(CamcorderProfile.QUALITY_CIF));
		if (supportsQCIF)
			temp.add(Integer.toString(CamcorderProfile.QUALITY_QCIF));
		if (supportsQVGA)
			temp.add(Integer.toString(CamcorderProfile.QUALITY_QVGA));

		CharSequence[] result = temp.toArray(new CharSequence[temp.size()]);
		return result;
	}

	private void setDefaultShockSensitivity() {
		SharedPreferences preferences = PreferenceManager
				.getDefaultSharedPreferences(this);

		String temp = preferences.getString("defaultShockSensitivity", "2");
		defaultShockSensitivity = Integer.parseInt(temp);
	}

	private void setDefaultCamcorderProfile() {
		SharedPreferences camcorderProfiles = PreferenceManager
				.getDefaultSharedPreferences(this);

		if (camcorderProfiles.contains("defaultCamcorderProfile")) {
			String temp = camcorderProfiles.getString(
					"defaultCamcorderProfile", "-1");
			defaultCamcorderProfile = Integer.parseInt(temp);
		} else {
			if (supports1080p)
				defaultCamcorderProfile = CamcorderProfile.QUALITY_1080P;
			else if (supports720p)
				defaultCamcorderProfile = CamcorderProfile.QUALITY_720P;
			else if (supports480p)
				defaultCamcorderProfile = CamcorderProfile.QUALITY_480P;
			else if (supportsCIF)
				defaultCamcorderProfile = CamcorderProfile.QUALITY_CIF;
			else if (supportsQCIF)
				defaultCamcorderProfile = CamcorderProfile.QUALITY_QCIF;
			else if (supportsQVGA)
				defaultCamcorderProfile = CamcorderProfile.QUALITY_QVGA;
		}
	}

	private void setDefaultVideoLength() {
		SharedPreferences sharedPreferences = PreferenceManager
				.getDefaultSharedPreferences(this);
		String temp = sharedPreferences.getString("defaultVideoLength",
				"300000");
		defaultVideoLength = Integer.parseInt(temp);
	}

	public boolean detectLoopModeActive() {
		SharedPreferences sharedPreferences = PreferenceManager
				.getDefaultSharedPreferences(this);
		loopModeActive = sharedPreferences.getBoolean("loopModeActive", false);

		return loopModeActive;
	}

	public boolean detectShockModeActive() {
		SharedPreferences sharedPreferences = PreferenceManager
				.getDefaultSharedPreferences(this);
		shockModeActive = sharedPreferences
				.getBoolean("shockModeActive", false);

		return shockModeActive;
	}

	public boolean detectAudioFeedbackButtonActive() {
		SharedPreferences sharedPreferences = PreferenceManager
				.getDefaultSharedPreferences(this);
		audioFeedbackButtonActive = sharedPreferences.getBoolean(
				"audioFeedbackButtonActive", true);

		return audioFeedbackButtonActive;
	}

	public boolean detectAudioFeedbackShockActive() {
		SharedPreferences sharedPreferences = PreferenceManager
				.getDefaultSharedPreferences(this);
		audioFeedbackShockActive = sharedPreferences.getBoolean(
				"audioFeedbackShockActive", true);

		return audioFeedbackShockActive;
	}

	public boolean detectTactileFeedbackActive() {
		SharedPreferences sharedPreferences = PreferenceManager
				.getDefaultSharedPreferences(this);
		tactileFeedbackActive = sharedPreferences.getBoolean(
				"tactileFeedbackActive", true);

		return tactileFeedbackActive;
	}

	public boolean detectLongPressToMarkActive() {
		SharedPreferences sharedPreferences = PreferenceManager
				.getDefaultSharedPreferences(this);
		longPressToMarkActive = sharedPreferences.getBoolean(
				"longPressToMarkActive", false);

		return longPressToMarkActive;
	}

	public boolean detectSpeedometerActive() {
		SharedPreferences sharedPreferences = PreferenceManager
				.getDefaultSharedPreferences(this);
		speedometerActive = sharedPreferences.getBoolean("speedometerActive",
				false);

		return speedometerActive;
	}

	public int detectSpeedometersUnitsMeasure() {
		SharedPreferences sharedPreferences = PreferenceManager
				.getDefaultSharedPreferences(this);
		String temp = sharedPreferences.getString("speedometerUnitsMeasure",
				"kph");
		if (temp.compareTo("kph") == 0) {
			defaultSpeedometerUnitsMeasure = SPEEDOMETER_KPH;
		} else {
			defaultSpeedometerUnitsMeasure = SPEEDOMETER_MPH;
		}

		return defaultSpeedometerUnitsMeasure;
	}

	public boolean isSplashscreenOpen() {
		return splashscreenOpen;
	}

	public void setSplashscreenOpen(boolean splashscreenOpen) {
		this.splashscreenOpen = splashscreenOpen;
	}

	public FragmentAllVideos getAllVideosFragment() {
		return allVideosFragment;
	}

	public void setAllVideosFragment(FragmentAllVideos allVideosFragment) {
		this.allVideosFragment = allVideosFragment;
	}

	public FragmentMarkedVideos getMarkedVideosFragment() {
		return markedVideosFragment;
	}

	public void setMarkedVideosFragment(
			FragmentMarkedVideos markedVideosFragment) {
		this.markedVideosFragment = markedVideosFragment;
	}

	public File getMediaStorageDir() {
		return mediaStorageDir;
	}

	public void setMediaStorageDir(File mediaStorageDir) {
		this.mediaStorageDir = mediaStorageDir;
	}

	public void setMustMarkFile(boolean b) {
		this.mustMarkFile = b;
	}

	public boolean getMustMarkFile() {
		return this.mustMarkFile;
	}
}
