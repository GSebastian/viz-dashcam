package com.vizdashcam;

import android.content.Context;

import com.vizdashcam.utils.FeedbackSoundPlayer;

public class ShockListener implements AccelerometerListener {

    public static final String TAG = "ShockListener";

    private GlobalState appState;

    public ShockListener(Context context) {
        this.appState = (GlobalState) context;
    }

    @Override
    public void onAccelerationChanged(float x, float y, float z) {
    }

    @Override
    public void onShake(float force) {
        String lastGlobalFilename = appState.getLastFilename();
        String lastMarkedFilename = appState.getLastMarkedFilename();
        if (lastGlobalFilename.compareTo(lastMarkedFilename) != 0) {
            appState.setMustMarkFile(true);
            appState.setLastMarkedFilename(lastGlobalFilename);
            audioFeedback();
        }
    }

    private void audioFeedback() {
        if (SharedPreferencesHelper.detectAudioFeedbackShockActive(appState)) {
            FeedbackSoundPlayer.playSound(FeedbackSoundPlayer.SOUND_SHOCK);
        }
    }
}
