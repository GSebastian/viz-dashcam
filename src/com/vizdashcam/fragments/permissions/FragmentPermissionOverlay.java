package com.vizdashcam.fragments.permissions;

import android.annotation.TargetApi;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;

import com.vizdashcam.R;
import com.vizdashcam.fragments.FragmentPermissionBase;
import com.vizdashcam.utils.VizPermissionUtils;

public class FragmentPermissionOverlay extends FragmentPermissionBase {

    @Override
    public int getImageResource() {
        return R.drawable.ic_permission_overlay;
    }

    @Override
    public int getTextResource() {
        return R.string.permission_explanation_overlay_new;
    }

    @Override
    @TargetApi(Build.VERSION_CODES.M)
    public boolean hasPermission() {
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.M | Settings.canDrawOverlays(getContext());
    }

    @Override
    public void grantPermission() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return;
        }

        Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:" + getContext().getPackageName()));
        startActivityForResult(intent, VizPermissionUtils.REQUEST_CODE_PERMISSIONS);
    }

    @Override
    public String getPermission() {
        return VizPermissionUtils.PERMISSION_OVERLAY;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == VizPermissionUtils.REQUEST_CODE_PERMISSIONS) {
            if (hasPermission()) {
                markPermissionGranted();
                sendPermissionGranted();
            }
        }
    }
}
