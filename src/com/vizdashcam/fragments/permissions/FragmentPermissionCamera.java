package com.vizdashcam.fragments.permissions;

import android.Manifest;

import com.vizdashcam.R;
import com.vizdashcam.fragments.FragmentPermissionBase;

public class FragmentPermissionCamera extends FragmentPermissionBase {

    @Override
    public int getImageResource() {
        return R.drawable.ic_permission_camera;
    }

    @Override
    public int getTextResource() {
        return R.string.permission_explanation_camera_new;
    }

    @Override
    public boolean hasPermission() {
        return false;
    }

    @Override
    public String getPermission() {
        return Manifest.permission.CAMERA;
    }
}
