package com.vizdashcam.activities;

import android.app.Activity;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;

public class DialogStorage extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		final DialogStorage activity = this;

		Builder builder = new Builder(this);
		builder.setTitle("viz");
		builder.setMessage("Insufficient memory - recording stopped! Free up space first!");
		builder.setNeutralButton("Dismiss", new OnClickListener() {

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
