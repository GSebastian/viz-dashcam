package com.vizdashcam.activities;

import com.vizdashcam.GlobalState;

import com.vizdashcam.R;
import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;

public class ActivitySplashscreen extends Activity {

	private GlobalState mAppState;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_splashscreen);
		
		mAppState = (GlobalState) getApplicationContext();
				
		new Handler().postDelayed(new Runnable() {

			@Override
			public void run() {
//				mAppState.setSplashscreenOpen(false);
				ActivitySplashscreen.this.finish();
			}
		}, 3000);
	}
}
