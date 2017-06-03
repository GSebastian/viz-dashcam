package com.vizdashcam.utils;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.provider.Settings;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import rebus.permissionutils.PermissionEnum;

public class VizPermissionUtils {

    public static final int REQUEST_CODE_PERMISSIONS = 100;
    public static final String PERMISSION_OVERLAY = "PERMISSION_OVERLAY";
    public static final String PERMISSION_GRANTED_BROADCAST = "PERMISSION_GRANTED_BROADCAST";
    private static final String TAG = "VizPermissionUtils";

    public static List<String> computeNecessaryPermissions(Activity activity) {
        Log.d(TAG, "computeNecessaryPermissions: Checking for permissions ...");

        List<String> neededPermissions = new ArrayList<>();

        neededPermissions.add(Manifest.permission.CAMERA);
        neededPermissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        neededPermissions.add(Manifest.permission.RECORD_AUDIO);

        neededPermissions.add(PERMISSION_OVERLAY);

        List<String> result = new ArrayList<>();

        for (String permission : neededPermissions) {
            if (permission.equals(PERMISSION_OVERLAY) ?
                    !canDrawOverlays(activity.getApplicationContext()) :
                    !rebus.permissionutils.PermissionUtils.isGranted(activity, PermissionEnum.fromManifestPermission
                            (permission))) {
                result.add(permission);
            }
        }

        Log.d(TAG, "computeNecessaryPermissions: Permissions needed: " + result.toString());

        return result;
    }

    @TargetApi(Build.VERSION_CODES.M)
    private static boolean canDrawOverlays(Context context) {
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.M || Settings.canDrawOverlays(context);
    }
}
