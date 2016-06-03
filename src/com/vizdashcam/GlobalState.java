package com.vizdashcam;

import java.io.File;
import java.util.ArrayList;

import android.app.Application;
import android.content.SharedPreferences;
import android.media.CamcorderProfile;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.util.Log;
import android.util.Pair;

import com.vizdashcam.fragments.FragmentAllVideos;
import com.vizdashcam.fragments.FragmentMarkedVideos;
import com.vizdashcam.utils.Constants;
import com.vizdashcam.utils.FeedbackSoundPlayer;

public class GlobalState extends Application {

    private static final String TAG = "GlobalState";

    private File mediaStorageDir;

    private Pair<Integer, Integer> lastFeedbackCoords;

    private String lastFilename;
    private String lastMarkedFilename;

    private boolean recording = false;
    private boolean previewBound = false;
    private boolean activityPaused = false;
    private boolean previewActive = false;
//	private boolean splashscreenOpen = true;

    private boolean supports1080p;
    private boolean supports720p;
    private boolean supports480p;
    private boolean supportsCIF;
    private boolean supportsQCIF;
    private boolean supportsQVGA;

    private int camcorderProfile;
    private int videoLength;
    private boolean loopModeActive;
    private boolean shockModeActive;
    private boolean audioFeedbackButtonActive;
    private boolean audioFeedbackShockActive;
    private boolean tactileFeedbackActive;
    private int shockSensitivity;
    private boolean longPressToMarkActive;
    private boolean speedometerActive;
    private int speedometerUnitsMeasure;

    private FragmentAllVideos allVideosFragment = null;
    private FragmentMarkedVideos markedVideosFragment = null;

    private boolean mustMarkFile;

    @Override
    public void onCreate() {
        super.onCreate();

        createVideoFolder();
        detectSupportedCamcorderProfiles();
        FeedbackSoundPlayer.init(this);
        new VideoPreview();
    }

    public void createVideoFolder() {
        mediaStorageDir = new File(
                Environment.getExternalStorageDirectory(), "vizDashcamApp");

        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                Log.e(TAG, "Failed to create directory");
            }
        }
    }

    public void initializeCameraParams() {
        setCamcorderProfile();
        setVideoLength();
        setShockSensitivity();
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

    public int getCamcorderProfile() {
        return camcorderProfile;
    }

    public void setCamcorderProfile(int camcorderProfile) {
        this.camcorderProfile = camcorderProfile;
    }

    public boolean isRecording() {
        return this.recording;
    }

    public void setRecording(boolean recording) {
        this.recording = recording;
    }

    public int getVideoLength() {
        return videoLength;
    }

    public int getShockSensitivity() {
        return shockSensitivity;
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

        // 640 x 480 || 720 x 480 || 704 x 480
        if (CamcorderProfile.hasProfile(CamcorderProfile.QUALITY_480P)) {
            camcorderProfilesEditor.putBoolean("480p", true);
        } else
            camcorderProfilesEditor.putBoolean("480p", false);

        // 1280 x 720
        if (CamcorderProfile.hasProfile(CamcorderProfile.QUALITY_720P))
            camcorderProfilesEditor.putBoolean("720p", true);
        else
            camcorderProfilesEditor.putBoolean("720p", false);

        // 352 x 288
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

        return temp.toArray(new CharSequence[temp.size()]);
    }

    private void setShockSensitivity() {
        String temp = SharedPreferencesHelper.checkStringPreferenceValue(this, Constants.PREF_SHOCK_SENSITIVITY, "2");
        shockSensitivity = Integer.parseInt(temp);
    }

    private void setCamcorderProfile() {
        String temp = SharedPreferencesHelper.checkStringPreferenceValue(this, Constants.PREF_VIDEO_QUALITY, null);
        if (temp != null) camcorderProfile = Integer.parseInt(temp);
        else {
            if (supports1080p)
                camcorderProfile = CamcorderProfile.QUALITY_1080P;
            else if (supports720p)
                camcorderProfile = CamcorderProfile.QUALITY_720P;
            else if (supports480p)
                camcorderProfile = CamcorderProfile.QUALITY_480P;
            else if (supportsCIF)
                camcorderProfile = CamcorderProfile.QUALITY_CIF;
            else if (supportsQCIF)
                camcorderProfile = CamcorderProfile.QUALITY_QCIF;
            else if (supportsQVGA)
                camcorderProfile = CamcorderProfile.QUALITY_QVGA;
        }
    }

    private void setVideoLength() {
        String temp = SharedPreferencesHelper.checkStringPreferenceValue(this, Constants.PREF_VIDEO_LENGTH, "300000");
        videoLength = Integer.parseInt(temp);
    }

    public boolean detectLoopModeActive() {
        loopModeActive = SharedPreferencesHelper.checkBooleanPreferenceValue(this, Constants.PREF_LOOP_ACTIVE, false);
        return loopModeActive;
    }

    public boolean detectShockModeActive() {
        shockModeActive = SharedPreferencesHelper.checkBooleanPreferenceValue(this, Constants.PREF_SHOCK_ACTIVE, false);
        return shockModeActive;
    }

    public boolean detectAudioFeedbackButtonActive() {
        audioFeedbackButtonActive = SharedPreferencesHelper.checkBooleanPreferenceValue(this, Constants
                .PREF_AUDIO_FEEDBACK_BUTTON, true);
        return audioFeedbackButtonActive;
    }

    public boolean detectAudioFeedbackShockActive() {
        audioFeedbackShockActive = SharedPreferencesHelper.checkBooleanPreferenceValue(this, Constants
                .PREF_AUDIO_FEEDBACK_SHOCK, true);
        return audioFeedbackShockActive;
    }

    public boolean detectTactileFeedbackActive() {
        tactileFeedbackActive = SharedPreferencesHelper.checkBooleanPreferenceValue(this, Constants
                .PREF_TACTILE_FEEDBACK, true);
        return tactileFeedbackActive;
    }

    public boolean detectLongPressToMarkActive() {
        longPressToMarkActive = SharedPreferencesHelper.checkBooleanPreferenceValue(this, Constants.PREF_LONG_PRESS,
                false);
        return longPressToMarkActive;
    }

    public boolean detectSpeedometerActive() {
        speedometerActive = SharedPreferencesHelper.checkBooleanPreferenceValue(this, Constants
                .PREF_SPEEDOMETER_ACTIVE, false);
        return speedometerActive;
    }

    public int detectSpeedometersUnitsMeasure() {
        String temp = SharedPreferencesHelper.checkStringPreferenceValue(this, Constants.PREF_SPEEDOMETER_UNITS, "kph");
        if (temp.compareTo("kph") == 0) {
            speedometerUnitsMeasure = Constants.SPEEDOMETER_KPH;
        } else {
            speedometerUnitsMeasure = Constants.SPEEDOMETER_MPH;
        }

        return speedometerUnitsMeasure;
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
