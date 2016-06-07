package com.vizdashcam.activities;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;

import com.vizdashcam.GlobalState;
import com.vizdashcam.R;

public class SplashscreenActivity extends Activity {

	private GlobalState appState;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_splashscreen);
		
		appState = (GlobalState) getApplicationContext();
				
		new Handler().postDelayed(new Runnable() {

			@Override
			public void run() {
//				appState.setSplashscreenOpen(false);
				SplashscreenActivity.this.finish();
			}
		}, 3000);
	}
}
