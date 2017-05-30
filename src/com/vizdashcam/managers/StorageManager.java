package com.vizdashcam.managers;

import android.content.Context;
import android.content.Intent;
import android.os.Environment;
import android.os.Handler;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.vizdashcam.GlobalState;
import com.vizdashcam.ServicePreview;
import com.vizdashcam.SharedPreferencesHelper;
import com.vizdashcam.VideoItem;
import com.vizdashcam.activities.VideoListActivity;
import com.vizdashcam.utils.Utils;
import com.vizdashcam.utils.VideoDetector;

import java.io.File;
import java.util.Arrays;

public class StorageManager extends Thread {

    private static final String TAG = "StorageManager";

    private static File mediaStorageDir = new File(Environment
            .getExternalStorageDirectory().getAbsolutePath() + "/vizDashcamApp");

    private static long totalSpace = mediaStorageDir.getTotalSpace();
    private static long full95 = (long) (0.05 * totalSpace);
    private static long full90 = (long) (0.10 * totalSpace);
    private static GlobalState appState;
    private static ServicePreview previewService;
    private static Handler handler;
    private boolean isStopped = false;

    public StorageManager(Context appState, ServicePreview previewService,
                          Handler handler) {
        StorageManager.appState = (GlobalState) appState;
        StorageManager.previewService = previewService;
        StorageManager.handler = handler;
    }

    public static long getFreeSpace() {
        long freeBytesExternal = mediaStorageDir.getUsableSpace();
        return freeBytesExternal;
    }

    public static boolean hasRunOutOufSpace() {
        long freeSpace = getFreeSpace();
        if (freeSpace <= full95)
            return true;
        return false;
    }

    public static boolean hasLowSpace() {
        long freeSpace = getFreeSpace();
        if (freeSpace <= full90)
            return true;
        return false;
    }

    public void setStopped() {
        isStopped = true;
    }

    @Override
    public void run() {
        super.run();
        while (!isStopped) {
            if (hasLowSpace()) {
                if (hasRunOutOufSpace()) {
                    if (previewService != null) {
                        if (appState.isRecording()) {
                            handler.post(new Runnable() {

                                @Override
                                public void run() {
                                    previewService.stopRecording();
                                    previewService.displayStorageDialog();
                                }
                            });

                            isStopped = true;
                        } else {
                            handler.post(new Runnable() {

                                @Override
                                public void run() {
                                    previewService.displayStorageDialog();
                                }
                            });

                            isStopped = true;
                        }
                    }
                } else {
                    if (SharedPreferencesHelper.detectLoopModeActive(appState)) {
                        File[] dirEnt = mediaStorageDir.listFiles();
                        Arrays.sort(dirEnt, Utils.DATE_ORDER);

                        if (dirEnt.length > 1) {
                            boolean foundVideoToDelete = false;
                            int videoIndex = dirEnt.length - 1;
                            while ((!foundVideoToDelete) && (videoIndex >= 0)) {
                                if (VideoDetector.isVideo(dirEnt[videoIndex])
                                        && !VideoItem
                                        .isMarked(dirEnt[videoIndex]
                                                .getName())) {
                                    foundVideoToDelete = true;
                                } else {
                                    videoIndex--;
                                }
                            }

                            if (foundVideoToDelete) {
                                final File toDelete = dirEnt[videoIndex];
                                Log.e(TAG,
                                        "Removing video from fragments");

                                handler.post(new Runnable() {
                                    @Override
                                    public void run() {

                                        broadcastUpdate();
                                    }
                                });

                                toDelete.delete();

                                System.gc();
                            }
                        }
                        dirEnt = null;
                    }
                }
            }

            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                Log.e(TAG, "InterruptedException: " + e.getMessage());
            }
        }
    }

    private void broadcastUpdate() {
        Intent intent = new Intent(VideoListActivity.ACTION_UPDATE);
        LocalBroadcastManager.getInstance(appState).sendBroadcast(intent);
    }
}