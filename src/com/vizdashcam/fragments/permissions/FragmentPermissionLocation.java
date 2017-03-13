package com.vizdashcam.fragments.permissions;

import com.vizdashcam.R;
import com.vizdashcam.fragments.FragmentPermissionBase;

public class FragmentPermissionLocation extends FragmentPermissionBase {

    @Override
    public int getImageResource() {
        return R.drawable.ic_permission_audio;
    }

    @Override
    public int getTextResource() {
        return R.string.permission_explanation_location_new;
    }
}
