package com.vizdashcam.fragments;

import com.vizdashcam.VideoItem;
import com.vizdashcam.utils.VideoDetector;

import java.io.File;
import java.util.Vector;

public class FragmentAllVideos extends VideosFragment {

    public static final String TAG = "AllVideosFragment";

    public void fillWithVideos() {

        final Vector<VideoItem> newData = new Vector<VideoItem>();

        new Thread(new Runnable() {

            @Override
            public void run() {
                File files[] = mediaStorageDir.listFiles();
                if (files != null) {
                    if (files.length > 0) {
                        for (File file : files) {
                            if (VideoDetector.isVideo(file)) {
                                newData.add(new VideoItem(file));
                            }
                        }
                    }
                }

                handler.post(new Runnable() {

                    @Override
                    public void run() {
                        directoryEntries.clear();
                        directoryEntries.addAll(newData);
                        mAdapter.notifyDataSetChanged();
                    }
                });
            }
        }).start();
    }
}
