package com.vizdashcam.utils;

import android.app.AlertDialog;
import android.content.Context;
import android.support.annotation.StringRes;

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
}
