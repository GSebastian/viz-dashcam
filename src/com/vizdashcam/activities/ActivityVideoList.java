package com.vizdashcam.activities;

import java.lang.reflect.Method;

import com.vizdashcam.GlobalState;

import com.vizdashcam.R;
import com.vizdashcam.fragments.FragmentAllVideos;
import com.vizdashcam.fragments.FragmentMarkedVideos;

import android.app.ActionBar;
import android.app.ActionBar.Tab;
import android.app.ActionBar.TabListener;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;

public class ActivityVideoList extends FragmentActivity implements TabListener {

	ActionBar mActionBar;
	ViewPager mViewPager;
	GlobalState mAppState;

	@Override
	protected void onCreate(Bundle arg0) {
		super.onCreate(arg0);
		setContentView(R.layout.activity_video_list);

		getActionBar().setTitle("Videos");

		mAppState = (GlobalState) getApplicationContext();

		mActionBar = getActionBar();
		mActionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

		mViewPager = (ViewPager) findViewById(R.id.vp_pager);
		mViewPager
				.setAdapter(new VideoListAdapter(getSupportFragmentManager()));
		mViewPager
				.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {

					@Override
					public void onPageSelected(int arg0) {
						mActionBar.setSelectedNavigationItem(arg0);
					}

					@Override
					public void onPageScrolled(int arg0, float arg1, int arg2) {
					}

					@Override
					public void onPageScrollStateChanged(int arg0) {
					}
				});

		ActionBar.Tab allVideosTab = mActionBar.newTab();
		allVideosTab.setText("All Videos");
		allVideosTab.setTabListener(this);

		ActionBar.Tab markedVideosTab = mActionBar.newTab();
		markedVideosTab.setText("Marked Videos");
		markedVideosTab.setTabListener(this);

		mActionBar.addTab(allVideosTab);
		mActionBar.addTab(markedVideosTab);

		try {
			final Method setHasEmbeddedTabsMethod = mActionBar.getClass()
					.getDeclaredMethod("setHasEmbeddedTabs", boolean.class);
			setHasEmbeddedTabsMethod.setAccessible(true);
			setHasEmbeddedTabsMethod.invoke(mActionBar, false);
		} catch (final Exception e) {

		}
	}

	@Override
	public void onTabReselected(Tab arg0, FragmentTransaction arg1) {

	}

	@Override
	public void onTabSelected(Tab arg0, FragmentTransaction arg1) {
		mViewPager.setCurrentItem(arg0.getPosition());
	}

	@Override
	public void onTabUnselected(Tab arg0, FragmentTransaction arg1) {

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
}
