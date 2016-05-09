package com.vizdashcam.managers;

import java.io.File;
import java.util.Arrays;

import android.content.Context;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;

import com.vizdashcam.GlobalState;
import com.vizdashcam.ServicePreview;
import com.vizdashcam.VideoItem;
import com.vizdashcam.utils.Utils;
import com.vizdashcam.utils.VideoDetector;

public class StorageManager extends Thread {

	private static final String TAG = "StorageManager";

	private static File mediaStorageDir = new File(Environment
			.getExternalStorageDirectory().getAbsolutePath() + "/vizDashcamApp");

	private static long totalSpace = mediaStorageDir.getTotalSpace();
	private static long full95 = (long) (0.05 * totalSpace);
	private static long full90 = (long) (0.10 * totalSpace);
	private boolean isStopped = false;

	private static GlobalState mAppState;
	private static ServicePreview mPreviewService;
	private static Handler mHandler;

	public void setStopped() {
		isStopped = true;
	}

	public StorageManager(Context appState, ServicePreview previewService,
			Handler handler) {
		mAppState = (GlobalState) appState;
		mPreviewService = previewService;
		mHandler = handler;
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

	@Override
	public void run() {
		super.run();
		while (!isStopped) {
			if (hasLowSpace()) {
				if (hasRunOutOufSpace()) {
					if (mPreviewService != null) {
						if (mAppState.isRecording()) {
							mHandler.post(new Runnable() {

								@Override
								public void run() {
									mPreviewService.stopRecording();
									mPreviewService.displayStorageDialog();
								}
							});

							isStopped = true;
						} else {
							mHandler.post(new Runnable() {

								@Override
								public void run() {
									mPreviewService.displayStorageDialog();
								}
							});

							isStopped = true;
						}
					}
				} else {
					if (mAppState.detectLoopModeActive()) {
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

								mHandler.post(new Runnable() {
									@Override
									public void run() {
										
										if (mAppState.getAllVideosFragment() != null) {
											mAppState.getAllVideosFragment()
													.removeVideoFromDataset(
															new VideoItem(
																	toDelete));
										}
										if (VideoItem.isMarked(toDelete
												.getName())
												&& mAppState
														.getMarkedVideosFragment() != null) {
											mAppState.getMarkedVideosFragment()
													.removeVideoFromDataset(
															new VideoItem(
																	toDelete));
										}
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
}