package com.vizdashcam;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.vizdashcam.fragments.permissions.FragmentPermissionAudio;
import com.vizdashcam.fragments.permissions.FragmentPermissionLocation;

public class AdapterPermissionPager extends FragmentPagerAdapter {

    public AdapterPermissionPager(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                return new FragmentPermissionAudio();
            case 1:
                return new FragmentPermissionLocation();
            default:
                return null;
        }
    }

    @Override
    public int getCount() {
        return 2;
    }
}
