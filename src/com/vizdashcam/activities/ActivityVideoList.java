package com.vizdashcam.activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.vizdashcam.R;
import com.vizdashcam.fragments.FragmentAllVideos;
import com.vizdashcam.fragments.FragmentMarkedVideos;

public class ActivityVideoList extends AppCompatActivity { //implements TabListener {

    public static final String ACTION_ADD_VIDEO = "add-video";
    public static final String ACTION_REMOVE_VIDEO = "remove-video";
    public static final String ACTION_REMOVE_VIDEO_FROM_DATASET = "remove-video-from-dataset";
    public static final String KEY_VIDEO = "video";

    private IntentFilter videoActionsFilter;

    Toolbar toolbar;
    ViewPager viewPager;
    TabLayout tabLayout;

    BroadcastReceiver videoActionsReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            FragmentAllVideos allVids = (FragmentAllVideos) getSupportFragmentManager().findFragmentByTag
                    (makeFragmentName(R.id.pager, 0));
            FragmentMarkedVideos markedVids = (FragmentMarkedVideos) getSupportFragmentManager().findFragmentByTag
                    (makeFragmentName(R.id.pager, 1));

            if (intent.getAction().equals(ACTION_ADD_VIDEO)) {

            } else if (intent.getAction().equals(ACTION_REMOVE_VIDEO)) {

            } else if (intent.getAction().equals(ACTION_REMOVE_VIDEO_FROM_DATASET)) {

            }

        }
    };

    @Override
    protected void onCreate(Bundle arg0) {
        super.onCreate(arg0);
        setContentView(R.layout.activity_video_list);

        findViews();
        initViews();

        videoActionsFilter = new IntentFilter();
        videoActionsFilter.addAction(ACTION_ADD_VIDEO);
        videoActionsFilter.addAction(ACTION_REMOVE_VIDEO);
        videoActionsFilter.addAction(ACTION_REMOVE_VIDEO_FROM_DATASET);
    }

    private void findViews() {
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        viewPager = (ViewPager) findViewById(R.id.pager);
        tabLayout = (TabLayout) findViewById(R.id.tabLayout);
    }

    private void initViews() {
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        viewPager.setAdapter(new VideoListAdapter(getSupportFragmentManager()));
        tabLayout.setupWithViewPager(viewPager);
    }

    @Override
    protected void onResume() {
        super.onResume();

        LocalBroadcastManager.getInstance(this).registerReceiver(videoActionsReceiver, videoActionsFilter);
    }

    @Override
    protected void onPause() {
        super.onPause();

        LocalBroadcastManager.getInstance(this).unregisterReceiver(videoActionsReceiver);
    }

    private static String makeFragmentName(@IdRes int viewPagerId, int index) {
        return "android:switcher:" + viewPagerId + ":" + index;
    }
}

class VideoListAdapter extends FragmentPagerAdapter {

    public VideoListAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int arg0) {
        Fragment fragment = null;

        if (arg0 == 0) {
            fragment = new FragmentAllVideos();
        }

        if (arg0 == 1) {
            fragment = new FragmentMarkedVideos();
        }

        return fragment;
    }

    @Override
    public int getCount() {
        return 2;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return position == 0 ? "All Videos" : "Marked Videos";
    }
}
