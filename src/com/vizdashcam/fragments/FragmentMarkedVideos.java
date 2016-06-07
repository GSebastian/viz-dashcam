package com.vizdashcam.fragments;

import java.io.File;
import java.util.Vector;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Vibrator;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView.MultiChoiceModeListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import com.vizdashcam.AdapterVideoList;
import com.vizdashcam.GlobalState;
import com.vizdashcam.R;
import com.vizdashcam.SharedPreferencesHelper;
import com.vizdashcam.VideoItem;
import com.vizdashcam.activities.ActivityVideoItem;
import com.vizdashcam.activities.ActivityVideoList;
import com.vizdashcam.utils.FeedbackSoundPlayer;
import com.vizdashcam.utils.VideoDetector;

public class FragmentMarkedVideos extends Fragment {

	public static final String TAG = "MarkedVideosFragment";

	private File mediaStorageDir = new File(
			Environment.getExternalStorageDirectory(), "vizDashcamApp");
	private Vector<VideoItem> directoryEntries;
	private AdapterVideoList mAdapter;

	private GlobalState mAppState;

	private ListView videoList;

	private Handler handler;

	private VideoItem lastClicked;
	public static final int CODE_DELETE = 90;

	public FragmentMarkedVideos() {
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mAppState = (GlobalState) getActivity().getApplicationContext();

		directoryEntries = new Vector<VideoItem>();
		mAdapter = new AdapterVideoList(getActivity(), R.layout.row_item,
				directoryEntries);

		handler = new Handler();

		fillWithVideos();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		return inflater.inflate(R.layout.fragment_all_videos, container, false);
	}

	@Override
	public void onStart() {
		super.onStart();
		videoList = (ListView) getView().findViewById(R.id.lv_videolist);
		initVideoList();
		videoList.invalidateViews();
	}

	private void initVideoList() {
		videoList.setAdapter(mAdapter);
		videoList.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
		videoList.setMultiChoiceModeListener(new MultiChoiceModeListener() {

			@Override
			public boolean onPrepareActionMode(ActionMode mode, Menu menu) {

				return false;
			}

			@Override
			public void onDestroyActionMode(ActionMode mode) {
				mAdapter.removeSelection();

			}

			@Override
			public boolean onCreateActionMode(ActionMode mode, Menu menu) {

				mode.getMenuInflater().inflate(R.menu.activity_filebrowser,
						menu);
				return true;
			}

			@Override
			public boolean onActionItemClicked(ActionMode mode, MenuItem item) {

				int itemId = item.getItemId();
				if (itemId == R.id.delete) {
					SparseBooleanArray selected = mAdapter.getSelectedIds();
					for (int i = (selected.size() - 1); i >= 0; i--) {
						if (selected.valueAt(i)) {
							VideoItem selecteditem = mAdapter.getItem(selected
									.keyAt(i));

							mAdapter.remove(selecteditem);

							broadcastRemove(selecteditem);
						}
					}

					mode.finish();
					return true;
				} else {
					return false;
				}
			}

			@Override
			public void onItemCheckedStateChanged(ActionMode mode,
					int position, long id, boolean checked) {

				final int checkedCount = videoList.getCheckedItemCount();
				mode.setTitle(checkedCount + " Selected");
				mAdapter.toggleSelection(position);

			}
		});

		videoList.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1,
					int position, long arg3) {
				Intent i = new Intent(getActivity(), ActivityVideoItem.class);
				i.putExtra(ActivityVideoItem.KEY_VIDEO_ITEM, directoryEntries.elementAt(position)
						.getFile());
				startActivityForResult(i, CODE_DELETE);
				lastClicked = directoryEntries.get(position);

				audioFeedback();
				tactileFeedback();
			}
		});

	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == 90) {
			if (resultCode == ActivityVideoItem.RESULT_DELETE) {
				if (lastClicked != null) {
					mAdapter.remove(lastClicked);

                    broadcastRemoveFromDataset(lastClicked);

                    lastClicked = null;
				}
			} else if (resultCode == ActivityVideoItem.RESULT_BECAME_NORMAL) {
				if (lastClicked != null) {

                    broadcastRemoveFromDataset(lastClicked);

					lastClicked.setMarked(false);
					removeVideoFromDataset(lastClicked);

                    broadcastAdd(lastClicked);
				}
			}
		}
	}

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
						if (videoList != null) {
							videoList.invalidateViews();
						}
					}
				});
			}
		}).start();
	}

	public void addVideoToDataset(VideoItem video) {
		if (directoryEntries != null) {
			directoryEntries.add(new VideoItem(video));
			mAdapter.notifyDataSetChanged();
			if (videoList != null) videoList.invalidateViews();
		}
	}

	public void removeVideo(VideoItem video) {
		mAdapter.remove(video);
	}

	public void removeVideoFromDataset(VideoItem video) {
		directoryEntries.remove(video);
		mAdapter.notifyDataSetChanged();
	}

	private void broadcastRemove(VideoItem video) {
		Intent intent = new Intent(ActivityVideoList.ACTION_REMOVE_VIDEO);
		intent.putExtra(ActivityVideoList.KEY_VIDEO, video);
		LocalBroadcastManager.getInstance(getContext()).sendBroadcast(intent);
	}

	private void broadcastAdd(VideoItem video) {
		Intent intent = new Intent(ActivityVideoList.ACTION_ADD_VIDEO);
		intent.putExtra(ActivityVideoList.KEY_VIDEO, video);
		LocalBroadcastManager.getInstance(getContext()).sendBroadcast(intent);
	}

	private void broadcastRemoveFromDataset(VideoItem video) {
		Intent intent = new Intent(ActivityVideoList.ACTION_REMOVE_VIDEO_FROM_DATASET);
		intent.putExtra(ActivityVideoList.KEY_VIDEO, video);
		LocalBroadcastManager.getInstance(getContext()).sendBroadcast(intent);
	}

	private void audioFeedback() {

		if (SharedPreferencesHelper.detectAudioFeedbackButtonActive(mAppState)) {
			FeedbackSoundPlayer.playSound(FeedbackSoundPlayer.SOUND_BTN);
		}
	}

	private void tactileFeedback() {
		if (SharedPreferencesHelper.detectTactileFeedbackActive(mAppState)) {
			Vibrator vibrator = (Vibrator) getActivity().getSystemService(
					Context.VIBRATOR_SERVICE);
			vibrator.vibrate(100);
		}
	}
}
