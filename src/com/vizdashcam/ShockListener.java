package com.vizdashcam;

import android.content.Context;

import com.vizdashcam.utils.FeedbackSoundPlayer;

public class ShockListener implements AccelerometerListener {
	public static final String TAG = "ShockListener";

	private GlobalState mAppState;

	public ShockListener(Context context) {
		this.mAppState = (GlobalState) context;
	}

	@Override
	public void onAccelerationChanged(float x, float y, float z) {
	}

	@Override
	public void onShake(float force) {
		String lastGlobalFilename = mAppState.getLastFilename();
		String lastMarkedFilename = mAppState.getLastMarkedFilename();
		if (lastGlobalFilename.compareTo(lastMarkedFilename) != 0) {
			mAppState.setMustMarkFile(true);
			mAppState.setLastMarkedFilename(lastGlobalFilename);
			audioFeedback();
		}
	}

	private void audioFeedback() {
		if (mAppState.detectAudioFeedbackShockActive()) {
			FeedbackSoundPlayer.playSound(FeedbackSoundPlayer.SOUND_SHOCK);
		}
	}
}
