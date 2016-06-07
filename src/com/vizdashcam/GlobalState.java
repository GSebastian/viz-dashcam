package com.vizdashcam;

import android.app.Application;
import android.os.Environment;
import android.util.Log;
import android.util.Pair;

import com.vizdashcam.utils.FeedbackSoundPlayer;

import java.io.File;

public class GlobalState extends Application {

    private static final String TAG = "GlobalState";

    private File mediaStorageDir;

    private Pair<Float, Float> lastFeedbackCoords;

    private String lastFilename;
    private String lastMarkedFilename;

    private boolean recording = false;
    private boolean previewBound = false;
    private boolean activityPaused = false;
//	private boolean splashscreenOpen = true;

    private boolean mustMarkFile;

    @Override
    public void onCreate() {
        super.onCreate();

        createVideoFolder();
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

    public boolean isRecording() {
        return this.recording;
    }

    public void setRecording(boolean recording) {
        this.recording = recording;
    }

    public File getMediaStorageDir() {
        return mediaStorageDir;
    }

    public void setMustMarkFile(boolean b) {
        this.mustMarkFile = b;
    }

    public boolean getMustMarkFile() {
        return this.mustMarkFile;
    }
}
