package com.vizdashcam;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

public class PreviewIncomingHandler extends Handler {

	private ServicePreview mService;
	private GlobalState mAppState;
	private static final String TAG = "PreviewIncomingHandler";

	public PreviewIncomingHandler(ServicePreview service, GlobalState appState) {
		mAppState = appState;
		mService = service;
	}

	@Override
	public void handleMessage(Message msg) {
		
		switch (msg.what) {
		case ServicePreview.MSG_OM:
			if (mAppState.isLoggingEnabled())
				Log.v(TAG, "Message Received: OM");
			
			mService.createOptionsMenu();
			break;
		case ServicePreview.MSG_RESIZE:
			if (mAppState.isLoggingEnabled())
				Log.v(TAG, "Message Received: RESIZE");
			
			mService.adjustPreview();
			break;
		default:
			super.handleMessage(msg);
		}
	}
}
