package com.vizdashcam;

import android.os.Handler;
import android.os.Message;

public class PreviewIncomingHandler extends Handler {

    private ServicePreview mService;

    public PreviewIncomingHandler(ServicePreview service) {
        mService = service;
    }

    @Override
    public void handleMessage(Message msg) {

        switch (msg.what) {
            case ServicePreview.MSG_RESIZE:
                mService.adjustPreview();
                break;
            default:
                super.handleMessage(msg);
        }
    }
}
