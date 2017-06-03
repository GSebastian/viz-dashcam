package com.vizdashcam;

import android.Manifest;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.util.Log;
import android.view.ViewGroup;

import com.vizdashcam.fragments.FragmentPermissionBase;
import com.vizdashcam.fragments.permissions.FragmentPermissionAudio;
import com.vizdashcam.fragments.permissions.FragmentPermissionCamera;
import com.vizdashcam.fragments.permissions.FragmentPermissionExternalStorage;
import com.vizdashcam.fragments.permissions.FragmentPermissionOverlay;
import com.vizdashcam.utils.VizPermissionUtils;

import java.util.List;

public class AdapterPermissionPager extends FragmentPagerAdapter {

    private static final String TAG = "AdapterPermissionPager";

    private Integer mCurrentPosition = null;

    public List<String> mNecessaryPermissions;

    public AdapterPermissionPager(List<String> necessaryPermissions,
                                  FragmentManager fm) {
        super(fm);
        mNecessaryPermissions = necessaryPermissions;
    }

    @Override
    public Fragment getItem(int position) {
        String permission = mNecessaryPermissions.get(position);
        if (permission.equals(Manifest.permission.CAMERA)) {
            Log.d(TAG, "Adding a camera fragment");
            return new FragmentPermissionCamera();
        } else if (permission.equals(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            Log.d(TAG, "Adding a external storage fragment");
            return new FragmentPermissionExternalStorage();
        } else if (permission.equals(Manifest.permission.RECORD_AUDIO)) {
            Log.d(TAG, "Adding a record audio fragment");
            return new FragmentPermissionAudio();
        } else if (permission.equals(VizPermissionUtils.PERMISSION_OVERLAY)) {
            Log.d(TAG, "Adding a overlay fragment");
            return new FragmentPermissionOverlay();
        }

        throw new RuntimeException(TAG + "getItem: Invalid permission necessary");
    }

    @Override
    public void setPrimaryItem(ViewGroup container, int position, Object object) {
        super.setPrimaryItem(container, position, object);

        if (mCurrentPosition != null && position == mCurrentPosition) {
            return;
        }

        if (object instanceof FragmentPermissionBase) {
            FragmentPermissionBase fragment = (FragmentPermissionBase) object;

            if (fragment.isResumed()) {
                mCurrentPosition = position;
                fragment.animateEntry();
            }
        }
    }

    @Override
    public int getCount() {
        return mNecessaryPermissions.size();
    }
}
