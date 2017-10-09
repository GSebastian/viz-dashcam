package com.vizdashcam.utils;

import android.content.Context;
import android.content.res.Resources;
import android.hardware.Camera;
import android.media.CamcorderProfile;
import android.util.DisplayMetrics;
import android.view.WindowManager;

import com.vizdashcam.SharedPreferencesHelper;

import java.util.ArrayList;
import java.util.List;

public class CameraUtils {

    public static Camera.Size getOptimalPreviewResolution(List<Camera.Size> supportedSizes, int screenWidth, int
            screenHight) {

        // Use a very small tolerance because we want an exact match.
        final double ASPECT_TOLERANCE = 0.1;
        double targetRatio = (double) screenWidth / screenHight;

        Camera.Size optimalSize = null;

        // Start with max value and refine as we iterate over available video sizes. This is the
        // minimum difference between view and camera height.
        double minDiff = Double.MAX_VALUE;

        // Target view height
        int targetHeight = screenHight;

        // Try to find a preview size that matches aspect ratio and the target view size.
        // Iterate over all available sizes and pick the largest size that can fit in the view and
        // still maintain the aspect ratio.
        for (Camera.Size size : supportedSizes) {
            double ratio = (double) size.width / size.height;
            if (Math.abs(ratio - targetRatio) > ASPECT_TOLERANCE)
                continue;
            if (Math.abs(size.height - targetHeight) < minDiff) {
                optimalSize = size;
                minDiff = Math.abs(size.height - targetHeight);
            }
        }

        // Cannot find preview size that matches the aspect ratio, ignore the requirement
        if (optimalSize == null) {
            minDiff = Double.MAX_VALUE;
            for (Camera.Size size : supportedSizes) {
                if (Math.abs(size.height - targetHeight) < minDiff) {
                    optimalSize = size;
                    minDiff = Math.abs(size.height - targetHeight);
                }
            }
        }
        return optimalSize;
    }

    public static int getStatusBarHeight(Context context) {
        Resources resources = context.getResources();
        int resourceId = resources.getIdentifier("status_bar_height", "dimen",
                "android");
        if (resourceId > 0) {
            return resources.getDimensionPixelSize(resourceId);
        }
        return 0;
    }

    public static int getDisplayWidth(Context context) {
        WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);

        DisplayMetrics dm = new DisplayMetrics();
        windowManager.getDefaultDisplay().getMetrics(dm);
        int displayWidthLandscape = dm.widthPixels;
        int displayHeightLandscape = dm.heightPixels;

        if (displayHeightLandscape > displayWidthLandscape)
            displayWidthLandscape = displayHeightLandscape;

        return displayWidthLandscape;
    }

    public static int getDisplayHeight(Context context) {
        WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);

        DisplayMetrics dm = new DisplayMetrics();
        windowManager.getDefaultDisplay().getMetrics(dm);
        int displayWidthLandscape = dm.widthPixels;
        int displayHeightLandscape = dm.heightPixels;

        if (displayHeightLandscape > displayWidthLandscape)
            displayHeightLandscape = displayWidthLandscape;

        displayHeightLandscape -= getStatusBarHeight(context);

        return displayHeightLandscape;
    }

    public static Integer[] getSupportedCamcorderProfiles() {

        List<Integer> supportedProfiles = new ArrayList<>();

        // 1920 x 1080 || 1920 x 1088
        if (CamcorderProfile.hasProfile(CamcorderProfile.QUALITY_1080P))
            supportedProfiles.add(CamcorderProfile.QUALITY_1080P);

        // 640 x 480 || 720 x 480 || 704 x 480
        if (CamcorderProfile.hasProfile(CamcorderProfile.QUALITY_480P))
            supportedProfiles.add(CamcorderProfile.QUALITY_480P);

        // 1280 x 720
        if (CamcorderProfile.hasProfile(CamcorderProfile.QUALITY_720P))
            supportedProfiles.add(CamcorderProfile.QUALITY_720P);

        // 352 x 288
        if (CamcorderProfile.hasProfile(CamcorderProfile.QUALITY_CIF))
            supportedProfiles.add(CamcorderProfile.QUALITY_CIF);

        // 320 x 240
        if (CamcorderProfile.hasProfile(CamcorderProfile.QUALITY_QVGA))
            supportedProfiles.add(CamcorderProfile.QUALITY_QVGA);

        // 176 x 144
        if (CamcorderProfile.hasProfile(CamcorderProfile.QUALITY_QCIF))
            supportedProfiles.add(CamcorderProfile.QUALITY_QCIF);

        return supportedProfiles.toArray(new Integer[supportedProfiles.size()]);
    }

    public static CharSequence[] getSupportedCamcorderProfilesIDAsCharArray() {

        List<CharSequence> supportedProfiles = new ArrayList<>();

        // 1920 x 1080 || 1920 x 1088
        if (CamcorderProfile.hasProfile(CamcorderProfile.QUALITY_1080P))
            supportedProfiles.add(Integer.toString(CamcorderProfile.QUALITY_1080P));

        // 640 x 480 || 720 x 480 || 704 x 480
        if (CamcorderProfile.hasProfile(CamcorderProfile.QUALITY_480P))
            supportedProfiles.add(Integer.toString(CamcorderProfile.QUALITY_480P));

        // 1280 x 720
        if (CamcorderProfile.hasProfile(CamcorderProfile.QUALITY_720P))
            supportedProfiles.add(Integer.toString(CamcorderProfile.QUALITY_720P));

        // 352 x 288
        if (CamcorderProfile.hasProfile(CamcorderProfile.QUALITY_CIF))
            supportedProfiles.add(Integer.toString(CamcorderProfile.QUALITY_CIF));

        // 320 x 240
        if (CamcorderProfile.hasProfile(CamcorderProfile.QUALITY_QVGA))
            supportedProfiles.add(Integer.toString(CamcorderProfile.QUALITY_QVGA));

        // 176 x 144
        if (CamcorderProfile.hasProfile(CamcorderProfile.QUALITY_QCIF))
            supportedProfiles.add(Integer.toString(CamcorderProfile.QUALITY_QCIF));

        return supportedProfiles.toArray(new CharSequence[supportedProfiles.size()]);
    }

    public static CharSequence[] getSupportedCamcorderProfilesNAMEAsCharArray() {

        ArrayList<String> temp = new ArrayList<>();

        if (CamcorderProfile.hasProfile(CamcorderProfile.QUALITY_1080P))
            temp.add("1080p");

        if (CamcorderProfile.hasProfile(CamcorderProfile.QUALITY_720P))
            temp.add("720p");

        if (CamcorderProfile.hasProfile(CamcorderProfile.QUALITY_480P))
            temp.add("480p");

        if (CamcorderProfile.hasProfile(CamcorderProfile.QUALITY_CIF))
            temp.add("CIF");

        if (CamcorderProfile.hasProfile(CamcorderProfile.QUALITY_QCIF))
            temp.add("QCIF");

        if (CamcorderProfile.hasProfile(CamcorderProfile.QUALITY_QVGA))
            temp.add("QVGA");

        return temp.toArray(new CharSequence[temp.size()]);
    }

    public static int getCamcorderProfile(Context context) throws Exception {
        int temp = SharedPreferencesHelper.detectStoredCamcorderProfile(context);
        if (temp != -1) return temp;

        Integer[] supportedCamcorderProfiles = CameraUtils.getSupportedCamcorderProfiles();

        if (supportedCamcorderProfiles.length > 0)
            return supportedCamcorderProfiles[0];

        throw new Exception("Could not find any camcorder profile");
    }
}
