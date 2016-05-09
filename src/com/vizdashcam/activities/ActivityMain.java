package com.vizdashcam.activities;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;
import android.view.Menu;

import com.vizdashcam.GlobalState;
import com.vizdashcam.ServicePreview;
import com.vizdashcam.R;

public class ActivityMain extends Activity {

	public String TAG = "MainActivity";

	private Messenger mMessenger;
	private ServiceConnection mConnection;

	private GlobalState mAppState = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mAppState = (GlobalState) getApplicationContext();

		if (mAppState.isLoggingEnabled()) {
			Log.v(TAG, "onCreate");
		}

		if (mAppState.isSplashscreenOpen()) {
			Intent splashscreenActivityIntent = new Intent(mAppState,
					ActivitySplashscreen.class);
			startActivity(splashscreenActivityIntent);
		}

		initUI();

		mConnection = new ServiceConnection() {
			public void onServiceConnected(ComponentName className,
					IBinder service) {

				if (mAppState.isLoggingEnabled())
					Log.v(TAG, "onServiceConnected");

				mMessenger = new Messenger(service);
				mAppState.setPreviewBound(true);

				if (mAppState.isActivityPaused()) {
					mAppState.setActivityPaused(false);
					sendMessageToService(ServicePreview.MSG_RESIZE);
				}
			}

			public void onServiceDisconnected(ComponentName className) {
				if (mAppState.isLoggingEnabled())
					Log.v(TAG, "onServiceDisconnected");

				mMessenger = null;
				mAppState.setPreviewBound(false);
			}
		};
	}

	@Override
	protected void onPause() {
		super.onPause();

		if (mAppState.isLoggingEnabled())
			Log.v(TAG, "onPause");

		if (!mAppState.isActivityPaused()) {

			mAppState.setActivityPaused(true);

			if (mAppState.isRecording()) {
				sendMessageToService(ServicePreview.MSG_RESIZE);
				if (mAppState.isPreviewBound())
					unbindService(mConnection);
			} else {
				if (mAppState.isPreviewBound())
					unbindService(mConnection);
				stopService(new Intent(ActivityMain.this, ServicePreview.class));
			}
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();

		if (mAppState.isLoggingEnabled())
			Log.v(TAG, "onDestroy");

		if (!mAppState.isActivityPaused()) {
			
			mAppState.setActivityPaused(true);

			if (mAppState.isRecording()) {
				sendMessageToService(ServicePreview.MSG_RESIZE);
				if (mAppState.isPreviewBound())
					unbindService(mConnection);
			} else {
				if (mAppState.isPreviewBound())
					unbindService(mConnection);
				stopService(new Intent(ActivityMain.this, ServicePreview.class));
			}
		} else {
			if (mAppState.isLoggingEnabled())
				Log.v(TAG, "onDestroy - already paused");
		}
	}

	@Override
	protected void onResume() {
		super.onResume();

		if (mAppState.isLoggingEnabled())
			Log.v(TAG, "onResume");

		if (!mAppState.isSplashscreenOpen()) {
			startForegroundPreviewService();
			doBindService();
		}

	}

	public boolean onCreateOptionsMenu(Menu menu) {
		if (mAppState.isLoggingEnabled()) {
			Log.v(TAG, "onCreateOptionsMenu");
		}
		sendMessageToService(ServicePreview.MSG_OM);
		return false;
	}

	private void initUI() {
		setContentView(R.layout.activity_main);
	}

	void doBindService() {
		bindService(new Intent(this, ServicePreview.class), mConnection,
				Context.BIND_AUTO_CREATE);
	}

	private void sendMessageToService(int intvaluetosend) {
		if (mAppState.isPreviewBound()) {
			if (mMessenger != null) {
				try {
					Message msg = Message.obtain(null, intvaluetosend, 0, 0);
					mMessenger.send(msg);
				} catch (RemoteException e) {
				}
			}
		}
	}

	private void startForegroundPreviewService() {
		Intent startForegroundIntent = new Intent(
				ServicePreview.ACTION_FOREGROUND);
		startForegroundIntent.setClass(ActivityMain.this, ServicePreview.class);
		startService(startForegroundIntent);
	}
}
