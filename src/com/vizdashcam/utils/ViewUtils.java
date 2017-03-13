package com.vizdashcam.utils;

import android.app.AlertDialog;
import android.content.Context;
import android.content.res.Resources;
import android.support.annotation.StringRes;
import android.util.DisplayMetrics;

import com.vizdashcam.R;

/**
 * Created by Sebastian on 5/10/2016.
 */
public class ViewUtils {

    public static AlertDialog createOneButtonDialog(Context context, @StringRes int stringRes, AlertDialog
            .OnClickListener listener) {

        return new AlertDialog.Builder(context).setMessage(stringRes).setPositiveButton(R.string.got_it, listener)
                .create();
    }

    public static AlertDialog createTwoButtonDialog(Context context, @StringRes int stringRes, @StringRes int
            negativeRes, @StringRes int positiveRes, AlertDialog.OnClickListener negativeListener, AlertDialog
            .OnClickListener positiveListener) {

        return new AlertDialog.Builder(context)
                .setMessage(stringRes)
                .setPositiveButton(positiveRes,  positiveListener)
                .setNegativeButton(negativeRes, negativeListener)
                .create();
    }

    public static float dp2px(float dp){
        DisplayMetrics metrics = Resources.getSystem().getDisplayMetrics();
        float px = dp * (metrics.densityDpi / 160f);
        return Math.round(px);
    }
}
