package com.vizdashcam.activities;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.provider.Settings;
import android.view.Menu;
import android.view.View;
import android.widget.Button;

import com.vizdashcam.GlobalState;
import com.vizdashcam.R;
import com.vizdashcam.ServicePreview;

public class ActivityMain extends Activity {

    private static final int CODE_OVERLAY_PERMISSION = 111;
    public String TAG = "MainActivity";
    private Messenger mMessenger;
    private GlobalState mAppState = null;
    private ServiceConnection mConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {

            mMessenger = new Messenger(service);
            mAppState.setPreviewBound(true);

            if (mAppState.isActivityPaused()) {
                mAppState.setActivityPaused(false);
                sendMessageToService(ServicePreview.MSG_RESIZE);
            }
        }

        public void onServiceDisconnected(ComponentName className) {
            mMessenger = null;
            mAppState.setPreviewBound(false);
        }
    };
    private View viewNoPermissions;
    private View btnRetry;
    private View viewPermissions;

    // If the user has been asked for the overlay permission and has declined it, don't re-request it automatically
    // but rather let him press the retry button (otherwise onResume requests it over and over)
    // The role of this is to prevent onResume from re-checking a permission that has already been declined by the
    // user manually
    private boolean declinedOverlayPermission = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViews();
        initViews();

        mAppState = (GlobalState) getApplicationContext();

        // TODO: REFACTOR SPLASH SCREEN
//          if (mAppState.isSplashscreenOpen()) {
//            Intent splashscreenActivityIntent = new Intent(mAppState,
//                    ActivitySplashscreen.class);
//            startActivity(splashscreenActivityIntent);
//        }
    }

    private void findViews() {
        viewNoPermissions = findViewById(R.id.llNoPermission);
        btnRetry = (Button) findViewById(R.id.btnRetry);
    }

    private void initViews() {
        btnRetry.setOnClickListener(new View.OnClickListener() {
            @TargetApi(Build.VERSION_CODES.M)
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:" + getPackageName()));
                ActivityMain.this.startActivityForResult(intent, CODE_OVERLAY_PERMISSION);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (!declinedOverlayPermission && !hasOverlayPermission())
            requestOverlayPermission();
        else if (!declinedOverlayPermission && hasOverlayPermission())
            // Typical case below marshmallow
            initService();
        else if (declinedOverlayPermission && hasOverlayPermission())
            throw new IllegalStateException("Couldn't have requested for overlay permission, have it declined and " +
                    "have overlay permission");
        else if (declinedOverlayPermission && !hasOverlayPermission())
            if (viewNoPermissions.getVisibility() != View.VISIBLE) throw new IllegalStateException("Overlay " +
                    "permission not granted view should be visible");
    }

    @Override
    protected void onPause() {
        super.onPause();

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

    @TargetApi(Build.VERSION_CODES.M)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CODE_OVERLAY_PERMISSION) {
            if (Settings.canDrawOverlays(this)) {
                // Makes onResume re-check the permission if the user accepted it the first place and then disabled
                // it manually
                // Accept permission - becomes false - user goes to settings - disables permission - onResume - this
                // is false, hasOverlayPermission is false - rechecks permission
                declinedOverlayPermission = false;

                // onResume gets fired after this, starting the preview
                viewNoPermissions.setVisibility(View.GONE);
            } else {
                // If permission was asked for but has not been granted, don't retry (only explicitly) when the user
                // presses retry
                declinedOverlayPermission = true;

                viewNoPermissions.setVisibility(View.VISIBLE);
            }
        }
    }

    private void initService() {

        // if (!mAppState.isSplashscreenOpen()) {
        Intent startForegroundIntent = new Intent(
                ServicePreview.ACTION_FOREGROUND);
        startForegroundIntent.setClass(ActivityMain.this, ServicePreview.class);
        startService(startForegroundIntent);

        bindService(new Intent(this, ServicePreview.class), mConnection,
                Context.BIND_AUTO_CREATE);
        //}
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        sendMessageToService(ServicePreview.MSG_OM);
        return false;
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

    // PERMISSIONS
    @TargetApi(Build.VERSION_CODES.M)
    private boolean hasOverlayPermission() {
        return Settings.canDrawOverlays(this);
    }

    private void requestOverlayPermission() {
        Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:" + getPackageName()));
        this.startActivityForResult(intent, CODE_OVERLAY_PERMISSION);
    }
}
