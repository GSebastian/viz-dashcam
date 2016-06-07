package com.vizdashcam.fragments;

import com.vizdashcam.VideoItem;
import com.vizdashcam.utils.VideoDetector;

import java.io.File;
import java.util.Vector;

public class FragmentMarkedVideos extends VideosFragment {

    public static final String TAG = "MarkedVideosFragment";

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
                                if (VideoItem.isMarked(file.getName())) {
                                    newData.add(new VideoItem(file));
                                }
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