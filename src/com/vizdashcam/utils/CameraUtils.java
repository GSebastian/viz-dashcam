package com.vizdashcam.utils;

import android.content.Context;
import android.content.res.Resources;
import android.hardware.Camera;
import android.util.DisplayMetrics;
import android.view.WindowManager;

import java.util.List;

/**
 * Created by sebastian on 23/05/16.
 */
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
}
