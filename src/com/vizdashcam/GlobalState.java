package com.vizdashcam;

import android.app.Activity;
import android.app.Application;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;

import com.vizdashcam.activities.PermissionsActivity;
import com.vizdashcam.utils.FeedbackSoundPlayer;
import com.vizdashcam.utils.PermissionUtils;

import java.io.File;

public class GlobalState extends Application {

    private static final String TAG = "GlobalState";

    private static final String VIDEO_DIR = "Viz Dashcam Recordings";

    private File mediaStorageDir;

    private String lastFilename;
    private String lastMarkedFilename;

    private boolean recording = false;
    private boolean previewBound = false;
    private boolean activityPaused = false;

    private boolean mustMarkFile;

    @Override
    public void onCreate() {
        super.onCreate();

        createVideoFolder();
        FeedbackSoundPlayer.init(this);
        new VideoPreview();

        registerActivityCallbacks();
    }

    private void registerActivityCallbacks() {
        registerActivityLifecycleCallbacks(new ActivityLifecycleCallbacks() {
            @Override
            public void onActivityCreated(Activity activity, Bundle bundle) {
            }

            @Override
            public void onActivityStarted(Activity activity) {
            }

            @Override
            public void onActivityResumed(Activity activity) {

            }

            @Override
            public void onActivityPaused(Activity activity) {
            }

            @Override
            public void onActivityStopped(Activity activity) {
            }

            @Override
            public void onActivitySaveInstanceState(Activity activity, Bundle bundle) {
            }

            @Override
            public void onActivityDestroyed(Activity activity) {
            }
        });
    }

    public void createVideoFolder() {
        mediaStorageDir = new File(
                Environment.getExternalStorageDirectory(), VIDEO_DIR);

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
