package com.vizdashcam.activities;

import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.vizdashcam.R;

public class StorageDialogActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final StorageDialogActivity activity = this;

        Builder builder = new Builder(this);

        String dialogTitle = getString(R.string.app_name);
        String dialogDescription = getString(R.string.storage_dialog_description);
        String dismiss = getString(R.string.dismiss);

        builder.setTitle(dialogTitle);
        builder.setMessage(dialogDescription);
        builder.setNeutralButton(dismiss, new OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                activity.finish();
            }
        });
        builder.setCancelable(false);

        builder.create().show();
    }
}
