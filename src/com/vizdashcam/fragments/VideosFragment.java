package com.vizdashcam.fragments;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Vibrator;
import android.support.v4.app.Fragment;
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

import com.vizdashcam.GlobalState;
import com.vizdashcam.R;
import com.vizdashcam.SharedPreferencesHelper;
import com.vizdashcam.VideoItem;
import com.vizdashcam.VideoListAdapter;
import com.vizdashcam.activities.VideoItemActivity;
import com.vizdashcam.utils.FeedbackSoundPlayer;

import java.io.File;
import java.util.Vector;

public abstract class VideosFragment extends Fragment {

    protected VideoListAdapter mAdapter;
    protected File mediaStorageDir = new File(Environment.getExternalStorageDirectory(), "viz");
    protected Handler handler;
    protected Vector<VideoItem> directoryEntries;
    private GlobalState appState;
    private ListView videoList;

    public VideosFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        appState = (GlobalState) getActivity().getApplicationContext();

        directoryEntries = new Vector<VideoItem>();
        mAdapter = new VideoListAdapter(getActivity(), R.layout.row_item,
                directoryEntries);

        handler = new Handler();

        fillWithVideos();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_all_videos, container, false);
        videoList = (ListView) v.findViewById(R.id.lv_videolist);

        initVideoList();

        return v;
    }

    @Override
    public void onStart() {
        super.onStart();

        fillWithVideos();
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

                mode.getMenuInflater().inflate(R.menu.menu_filebrowser,
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
                Intent i = new Intent(getActivity(), VideoItemActivity.class);
                i.putExtra(VideoItemActivity.KEY_VIDEO_ITEM, directoryEntries.elementAt(position));
                startActivity(i);

                audioFeedback();
                tactileFeedback();
            }
        });
    }

    public abstract void fillWithVideos();

    public void updateList() {
        fillWithVideos();
    }

    private void audioFeedback() {
        if (SharedPreferencesHelper.detectAudioFeedbackButtonActive(appState)) {
            FeedbackSoundPlayer.playSound(FeedbackSoundPlayer.SOUND_BTN);
        }
    }

    private void tactileFeedback() {
        if (SharedPreferencesHelper.detectTactileFeedbackActive(appState)) {
            Vibrator vibrator = (Vibrator) getActivity().getSystemService(
                    Context.VIBRATOR_SERVICE);
            vibrator.vibrate(100);
        }
    }
}
