package com.vizdashcam.activities;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.view.Menu;
import android.view.View;
import android.widget.Button;

import com.vizdashcam.GlobalState;
import com.vizdashcam.R;
import com.vizdashcam.ServicePreview;
import com.vizdashcam.utils.ViewUtils;

public class ActivityMain extends Activity {

    private static final int CODE_OVERLAY_PERMISSION = 111;
    private static final int CODE_CAMERA_PERMISSION = 222;
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
    private View viewNoOverlay;
    private View viewNoCamera;
    private Button btnRetryOverlay;
    private Button btnRetryCamera;
    private View viewPermissionsGranted;

    // If the user has been asked for the overlay permission and has declined it, don't re-request it automatically
    // but rather let him press the retry button (otherwise onResume requests it over and over)
    // The role of this is to prevent onResume from re-checking a permission that has already been declined by the
    // user manually
    private boolean declinedOverlayPermission = false;
    private boolean declinedCameraPermission = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViews();
        initViews();

        mAppState = (GlobalState) getApplicationContext();
    }

    private void findViews() {
        viewNoOverlay = findViewById(R.id.llNoOverlay);
        viewNoCamera = findViewById(R.id.llNoCamera);
        btnRetryOverlay = (Button) findViewById(R.id.btnRetryOverlay);
        btnRetryCamera = (Button) findViewById(R.id.btnRetryCamera);
    }

    private void initViews() {
        btnRetryOverlay.setOnClickListener(new View.OnClickListener() {
            @TargetApi(Build.VERSION_CODES.M)
            @Override
            public void onClick(View v) {
                requestOverlayPermission();
            }
        });

        btnRetryCamera.setOnClickListener(new View.OnClickListener() {
            @TargetApi(Build.VERSION_CODES.M)
            @Override
            public void onClick(View v) {
                // By the time this is checked, the user has already been asked at least once about the permission,
                // so shouldShowRequestPermissionRationale should return true

                // If the user has selected "Don't show again" and denied the permission, it returns false, making me
                // show a dialog and redirecting the user to the app's settings page to manually approve everything
                if (!shouldShowRequestPermissionRationale(Manifest.permission.WRITE_CONTACTS)) {
                    ViewUtils.createOneButtonDialog(ActivityMain.this, R.string.permission_explanation_camera_dont_show, new
                            DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                    Uri uri = Uri.fromParts("package", getPackageName(), null);
                                    intent.setData(uri);
                                    startActivityForResult(intent, CODE_CAMERA_PERMISSION);
                                }
                            }).show();
                }
            }
        });

        viewNoOverlay.setVisibility(View.GONE);
        viewNoCamera.setVisibility(View.GONE);
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (shouldRequestOverlayPermission())
            requestOverlayPermission();
        else if (shouldRequestCameraPermission())
            requestCameraPermission();
        else if (canShowCameraPreview()) {
            // If the preview can be shown, reset these to false
            // Need to do this because user might handle the permissions outside the app
            // which messes up the states
            declinedOverlayPermission = false;
            declinedCameraPermission = false;

            // Typical case below marshmallow, others shouldn't even occur
            initService();
        }
    }

    private boolean shouldRequestOverlayPermission() {
        return !declinedOverlayPermission && !hasOverlayPermission();
    }

    private boolean shouldRequestCameraPermission() {
        return hasOverlayPermission() && !declinedCameraPermission && !hasCameraPermission();
    }

    private boolean canShowCameraPreview() {
        return hasOverlayPermission() && hasCameraPermission();
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
                viewNoOverlay.setVisibility(View.GONE);
                viewNoCamera.setVisibility(View.VISIBLE);
            } else {
                // If permission was asked for but has not been granted, don't retry (only explicitly when the user
                // presses retry)
                declinedOverlayPermission = true;

                viewNoOverlay.setVisibility(View.VISIBLE);
                viewNoCamera.setVisibility(View.GONE);
            }
        } else if (requestCode == CODE_CAMERA_PERMISSION) {
            if (hasCameraPermission()) {

                viewNoOverlay.setVisibility(View.GONE);
                viewNoCamera.setVisibility(View.GONE);

                // Makes onResume re-check the permission if the user accepted it the first place and then disabled
                // it manually
                // Accept permission - becomes false - user goes to settings - disables permission - onResume - this
                // is false, hasOverlayPermission is false - rechecks permission
                declinedCameraPermission = false;
            } else {

                viewNoOverlay.setVisibility(View.GONE);
                viewNoCamera.setVisibility(View.VISIBLE);

                // If permission was asked for but has not been granted, don't retry (only explicitly when the user
                // presses retry)
                declinedCameraPermission = true;
            }
        }
    }

    private void initService() {

        Intent startForegroundIntent = new Intent(
                ServicePreview.ACTION_FOREGROUND);
        startForegroundIntent.setClass(ActivityMain.this, ServicePreview.class);
        startService(startForegroundIntent);

        bindService(new Intent(this, ServicePreview.class), mConnection,
                Context.BIND_AUTO_CREATE);
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

    @TargetApi(Build.VERSION_CODES.M)
    private boolean hasOverlayPermission() {
        return Settings.canDrawOverlays(this);
    }

    @TargetApi(Build.VERSION_CODES.M)
    private void requestOverlayPermission() {
        Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:" + getPackageName()));
        this.startActivityForResult(intent, CODE_OVERLAY_PERMISSION);
    }

    @TargetApi(Build.VERSION_CODES.M)
    private boolean hasCameraPermission() {
        int permissionResultCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA);
        return permissionResultCheck == PackageManager.PERMISSION_GRANTED;
    }

    @TargetApi(Build.VERSION_CODES.M)
    private void requestCameraPermission() {
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.CAMERA},
                CODE_CAMERA_PERMISSION);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case CODE_CAMERA_PERMISSION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    viewNoOverlay.setVisibility(View.GONE);
                    viewNoCamera.setVisibility(View.GONE);

                    // Makes onResume re-check the permission if the user accepted it the first place and then disabled
                    // it manually
                    // Accept permission - becomes false - user goes to settings - disables permission - onResume - this
                    // is false, hasOverlayPermission is false - rechecks permission
                    declinedCameraPermission = false;
                } else {

                    viewNoOverlay.setVisibility(View.GONE);
                    viewNoCamera.setVisibility(View.VISIBLE);

                    // If permission was asked for but has not been granted, don't retry (only explicitly when the user
                    // presses retry)
                    declinedCameraPermission = true;
                }

                break;
            }
        }
    }
}
