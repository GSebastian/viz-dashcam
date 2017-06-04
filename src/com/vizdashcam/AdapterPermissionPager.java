package com.vizdashcam;

import android.Manifest;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.util.SparseArray;
import android.view.ViewGroup;

import com.vizdashcam.fragments.permissions.FragmentPermissionAudio;
import com.vizdashcam.fragments.permissions.FragmentPermissionCamera;
import com.vizdashcam.fragments.permissions.FragmentPermissionExternalStorage;
import com.vizdashcam.fragments.permissions.FragmentPermissionOverlay;
import com.vizdashcam.utils.VizPermissionUtils;

import java.util.List;

public class AdapterPermissionPager extends FragmentPagerAdapter {

    private static final String TAG = "AdapterPermissionPager";

    public List<String> mNecessaryPermissions;

    public SparseArray<Fragment> registeredFragments = new SparseArray<Fragment>();

    public AdapterPermissionPager(List<String> necessaryPermissions,
                                  FragmentManager fm) {
        super(fm);
        mNecessaryPermissions = necessaryPermissions;
    }

    @Override
    public Fragment getItem(int position) {
        String permission = mNecessaryPermissions.get(position);
        if (permission.equals(Manifest.permission.CAMERA)) {
            return new FragmentPermissionCamera();
        } else if (permission.equals(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            return new FragmentPermissionExternalStorage();
        } else if (permission.equals(Manifest.permission.RECORD_AUDIO)) {
            return new FragmentPermissionAudio();
        } else if (permission.equals(VizPermissionUtils.PERMISSION_OVERLAY)) {
            return new FragmentPermissionOverlay();
        }

        throw new RuntimeException(TAG + "getItem: Invalid permission necessary");
    }

    @Override
    public int getCount() {
        return mNecessaryPermissions.size();
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        Fragment fragment = (Fragment) super.instantiateItem(container, position);
        registeredFragments.put(position, fragment);
        return fragment;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        registeredFragments.remove(position);
        super.destroyItem(container, position, object);
    }
}
