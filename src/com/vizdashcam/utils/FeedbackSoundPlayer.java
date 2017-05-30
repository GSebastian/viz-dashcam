package com.vizdashcam.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.media.AudioManager;
import android.media.SoundPool;

import com.vizdashcam.R;

import java.util.HashMap;

@SuppressLint("UseSparseArrays")
public class FeedbackSoundPlayer {
    public static final int SOUND_BTN = R.raw.button;
    public static final int SOUND_SHOCK = R.raw.shock_alarm;
    public static final int SOUND_MARKED = R.raw.marked_alarm;
    private static SoundPool soundPool;
    private static HashMap<Integer, Integer> soundPoolMap;

    public static void init(Context context) {
        soundPool = new SoundPool(1, AudioManager.STREAM_MUSIC, 0);
        soundPoolMap = new HashMap<Integer, Integer>(3);

        soundPoolMap.put(SOUND_BTN, soundPool.load(context, R.raw.button, 1));
        soundPoolMap.put(SOUND_SHOCK,
                soundPool.load(context, R.raw.shock_alarm, 2));
        soundPoolMap.put(SOUND_MARKED,
                soundPool.load(context, R.raw.marked_alarm, 3));
    }

    public static void playSound(int soundID) {
        if (soundPool != null && soundPoolMap != null) {
            soundPool.play((Integer) soundPoolMap.get(soundID), 1.0f, 1.0f, 1,
                    0, 1f);
        }
    }
}
