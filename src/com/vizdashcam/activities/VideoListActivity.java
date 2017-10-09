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
import com.vizdashcam.fragments.AllVideosFragment;
import com.vizdashcam.fragments.MarkedVideosFragment;

public class VideoListActivity extends AppCompatActivity {

    public static final String ACTION_UPDATE = "add-video";
    public static final String KEY_VIDEO = "video";
    Toolbar toolbar;
    ViewPager viewPager;
    TabLayout tabLayout;
    BroadcastReceiver videoActionsReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            AllVideosFragment allVids = (AllVideosFragment) getSupportFragmentManager().findFragmentByTag
                    (makeFragmentName(R.id.pager, 0));
            MarkedVideosFragment markedVids = (MarkedVideosFragment) getSupportFragmentManager().findFragmentByTag
                    (makeFragmentName(R.id.pager, 1));

            if (intent.getAction().equals(ACTION_UPDATE)) {
                if (allVids != null) allVids.updateList();
                if (markedVids != null) markedVids.updateList();
            }
        }
    };
    private IntentFilter videoActionsFilter;

    private static String makeFragmentName(@IdRes int viewPagerId, int index) {
        return "android:switcher:" + viewPagerId + ":" + index;
    }

    @Override
    protected void onCreate(Bundle arg0) {
        super.onCreate(arg0);
        setContentView(R.layout.activity_video_list);

        findViews();
        initViews();

        videoActionsFilter = new IntentFilter();
        videoActionsFilter.addAction(ACTION_UPDATE);
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

        viewPager.setAdapter(new VideoListFragmentAdapter(getSupportFragmentManager(), getApplicationContext()));
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
}

class VideoListFragmentAdapter extends FragmentPagerAdapter {

    Context context;

    public VideoListFragmentAdapter(FragmentManager fm, Context context) {
        super(fm);
        this.context = context;
    }

    @Override
    public Fragment getItem(int arg0) {
        Fragment fragment = null;

        if (arg0 == 0) {
            fragment = new AllVideosFragment();
        } else if (arg0 == 1) {
            fragment = new MarkedVideosFragment();
        }

        return fragment;
    }

    @Override
    public int getCount() {
        return 2;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return position == 0 ? context.getString(R.string.all_videos) : context.getString(R.string.marked_videos);
    }
}
