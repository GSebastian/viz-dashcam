package com.vizdashcam;

import android.Manifest;
import android.app.Activity;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.vizdashcam.fragments.permissions.FragmentPermissionAudio;
import com.vizdashcam.fragments.permissions.FragmentPermissionCamera;
import com.vizdashcam.fragments.permissions.FragmentPermissionExternalStorage;
import com.vizdashcam.fragments.permissions.FragmentPermissionOverlay;
import com.vizdashcam.utils.PermissionUtils;

import java.util.List;

public class AdapterPermissionPager extends FragmentPagerAdapter {

    private static final String TAG = "AdapterPermissionPager";

    public List<String> necessaryPermissions;

    public AdapterPermissionPager(Activity activity,
                                  FragmentManager fm) {
        super(fm);
        necessaryPermissions = PermissionUtils.computeNecessaryPermissions(activity);
    }

    @Override
    public Fragment getItem(int position) {
        String permission = necessaryPermissions.get(position);
        if (permission.equals(Manifest.permission.CAMERA)) {
            return new FragmentPermissionCamera();
        } else if (permission.equals(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            return new FragmentPermissionExternalStorage();
        } else if (permission.equals(Manifest.permission.RECORD_AUDIO)) {
            return new FragmentPermissionAudio();
        } else if (permission.equals(PermissionUtils.PERMISSION_OVERLAY)) {
            return new FragmentPermissionOverlay();
        }

        throw new RuntimeException(TAG + "getItem: Invalid permission necessary");
    }

    @Override
    public int getCount() {
        return necessaryPermissions.size();
    }
}
