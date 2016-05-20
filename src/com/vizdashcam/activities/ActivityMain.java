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
import com.vizdashcam.SharedPreferencesHelper;
import com.vizdashcam.utils.ViewUtils;

public class ActivityMain extends Activity {

    private static final int CODE_OVERLAY_PERMISSION = 111;
    private static final int CODE_BASIC_PERMISSIONS = 222;
    private static final int CODE_BASIC_AND_AUDIO_PERMISSIONS = 666;
    private static final int CODE_CAMERA_PERMISSION = 333;
    private static final int CODE_WRITE_PERMISSION = 444;

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
    private View viewNoBasicPermissions;
    private View viewNoCamera;
    private View viewNoWrite;
    private Button btnRetryOverlay;
    private Button btnRetryBasic;
    private Button btnRetryCamera;
    private Button btnRetryWrite;
    private View viewPermissionsGranted;

    // If the user has been asked for the overlay permission and has declined it, don't
    // re-request it automatically
    // but rather let him press the retry button (otherwise onResume requests it over and over)
    // The role of this is to prevent onResume from re-checking a permission that has already
    // been declined by the
    // user manually
    private boolean declinedOverlayPermission = false;
    // Basic permissions are the bare minimum needed for the app to function
    // These are CAMERA and WRITE_EXTERNAL_STORAGE
    private boolean declinedBasicPermissions = false;

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
        viewNoBasicPermissions = findViewById(R.id.llNoBasic);
        viewNoCamera = findViewById(R.id.llNoCamera);
        viewNoWrite = findViewById(R.id.llNoWrite);
        btnRetryOverlay = (Button) findViewById(R.id.btnRetryOverlay);
        btnRetryBasic = (Button) findViewById(R.id.btnRetryBasic);
        btnRetryCamera = (Button) findViewById(R.id.btnRetryCamera);
        btnRetryWrite = (Button) findViewById(R.id.btnRetryWrite);
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
            @Override
            @TargetApi(Build.VERSION_CODES.M)
            public void onClick(View v) {

                // By the time this is checked, the user has already been asked at least once
                // about the permission,
                // so shouldShowRequestPermissionRationale should return true

                // If the user has selected "Don't show again" and denied the permission, it
                // returns false, making me
                // show a dialog and redirecting the user to the app's settings page to manually
                // approve everything
                if (!shouldShowRequestPermissionRationale(Manifest.permission.CAMERA)) {
                    ViewUtils.createOneButtonDialog(ActivityMain.this, R.string
                            .permission_explanation_basic_dont_show, new
                            DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    startSettingsActivity();
                                }
                            }).show();
                } else requestCameraPermission();
            }
        });

        btnRetryWrite.setOnClickListener(new View.OnClickListener() {
            @Override
            @TargetApi(Build.VERSION_CODES.M)
            public void onClick(View v) {

                if (!shouldShowRequestPermissionRationale(Manifest.permission
                        .WRITE_EXTERNAL_STORAGE)) {
                    ViewUtils.createOneButtonDialog(ActivityMain.this, R.string
                            .permission_explanation_basic_dont_show, new
                            DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    startSettingsActivity();
                                }
                            }).show();
                } else requestWritePermission();
            }
        });

        btnRetryBasic.setOnClickListener(new View.OnClickListener() {
            @Override
            @TargetApi(Build.VERSION_CODES.M)
            public void onClick(View v) {

                if (!shouldShowRequestPermissionRationale(Manifest.permission.CAMERA) ||
                        !shouldShowRequestPermissionRationale(Manifest.permission
                                .WRITE_EXTERNAL_STORAGE)) {
                    ViewUtils.createOneButtonDialog(ActivityMain.this, R.string
                            .permission_explanation_basic_dont_show, new
                            DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    startSettingsActivity();
                                }
                            }).show();
                } else requestBasicPermissions();
            }
        });

        setViewsPermissionsGranted();
    }

    private void startSettingsActivity() {
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package", getPackageName(), null);
        intent.setData(uri);
        startActivityForResult(intent, CODE_BASIC_PERMISSIONS);
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (shouldRequestOverlayPermission())
            requestOverlayPermission();
        else if (shouldRequestCameraPermission())
            requestCameraPermission();
        else if (shouldRequestWritePermission())
            requestWritePermission();
        else if (shouldRequestBasicPermissionAndAudio())
            requestBasicPermissionsAndAudio();
        else if (shouldRequestBasicPermission())
            requestBasicPermissions();
        else if (canShowCameraPreview()) {
            // If the preview can be shown, reset these to false
            // Need to do this because user might handle the permissions outside the app
            // which messes up the states
            declinedOverlayPermission = false;
            declinedBasicPermissions = false;

            // Typical case below marshmallow, others shouldn't even occur
            initService();
        }
    }

    private boolean shouldRequestOverlayPermission() {
        return !declinedOverlayPermission && !hasOverlayPermission();
    }

    private boolean shouldRequestBasicPermissionAndAudio() {
        return hasOverlayPermission() && !declinedBasicPermissions && !hasBasicPermissions() &&
                shouldRequestAudioRecordingPermission();
    }

    private boolean shouldRequestBasicPermission() {
        return hasOverlayPermission() && !declinedBasicPermissions && !hasBasicPermissions();
    }

    private boolean shouldRequestCameraPermission() {
        return hasOverlayPermission() && !declinedBasicPermissions && !hasCameraPermission() && hasWritePermission();
    }

    private boolean shouldRequestWritePermission() {
        return hasOverlayPermission() && !declinedBasicPermissions && !hasWritePermission() && hasCameraPermission();
    }

    private boolean shouldRequestAudioRecordingPermission() {
        // Unlike the CAMERA and WRITE permissions, the user's choice is persistent and can only be changed from the
        // app's Settings Activity of the Android-provided Settings activity

        // Once the user denies the audio recording permission, he won't be asked for it again and recordings won't
        // have audio. If the user wishes to have audio in his recordings, he can go to Settings and click a button
        // to have audio permissions requested
        return !SharedPreferencesHelper.getHasDeclinedAudio(this) && !hasAudioRecordingPermission();
    }

    private boolean canShowCameraPreview() {
        return hasOverlayPermission() && hasBasicPermissions();
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

        if (requestCode == CODE_OVERLAY_PERMISSION) {
            if (Settings.canDrawOverlays(this)) {
                // Makes onResume re-check the permission if the user accepted it the first place
                // and then disabled
                // it manually
                // Accept permission - becomes false - user goes to settings - disables
                // permission - onResume - this
                // is false, hasOverlayPermission is false - rechecks permission
                declinedOverlayPermission = false;

                // onResume gets fired after this, starting the preview
                setViewsNoBasic();
            } else {
                // If permission was asked for but has not been granted, don't retry (only
                // explicitly when the user
                // presses retry)
                declinedOverlayPermission = true;

                setViewsNoOverlay();
            }
        } else if (requestCode == CODE_BASIC_PERMISSIONS || requestCode == CODE_CAMERA_PERMISSION
                || requestCode ==
                CODE_WRITE_PERMISSION) {
            if (hasBasicPermissions()) {

                setViewsPermissionsGranted();

                // Makes onResume re-check the permission if the user accepted it the first place
                // and then disabled
                // it manually
                // Accept permission - becomes false - user goes to settings - disables
                // permission - onResume - this
                // is false, hasOverlayPermission is false - rechecks permission
                declinedBasicPermissions = false;
            } else {

                if (hasCameraPermission() && !hasWritePermission()) setViewsNoWrite();
                else if (!hasCameraPermission() && hasWritePermission()) setViewsNoCamera();
                else if (!hasCameraPermission() && !hasWritePermission()) setViewsNoBasic();

                // If permission was asked for but has not been granted, don't retry (only
                // explicitly when the user
                // presses retry)
                declinedBasicPermissions = true;
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
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.M || Settings.canDrawOverlays(this);
    }

    @TargetApi(Build.VERSION_CODES.M)
    private void requestOverlayPermission() {
        Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:" + getPackageName()));
        this.startActivityForResult(intent, CODE_OVERLAY_PERMISSION);
    }

    @TargetApi(Build.VERSION_CODES.M)
    private boolean hasBasicPermissions() {
        return hasCameraPermission() && hasWritePermission();
    }

    @TargetApi(Build.VERSION_CODES.M)
    private boolean hasCameraPermission() {
        int permissionResultCheck = ContextCompat.checkSelfPermission(this, Manifest.permission
                .CAMERA);
        return permissionResultCheck == PackageManager.PERMISSION_GRANTED;
    }

    @TargetApi(Build.VERSION_CODES.M)
    private boolean hasWritePermission() {
        int permissionResultCheck = ContextCompat.checkSelfPermission(this, Manifest.permission
                .WRITE_EXTERNAL_STORAGE);
        return permissionResultCheck == PackageManager.PERMISSION_GRANTED;
    }

    @TargetApi(Build.VERSION_CODES.M)
    private boolean hasAudioRecordingPermission() {
        int permissionResultCheck = ContextCompat.checkSelfPermission(this, Manifest.permission
                .RECORD_AUDIO);
        return permissionResultCheck == PackageManager.PERMISSION_GRANTED;
    }

    @TargetApi(Build.VERSION_CODES.M)
    private void requestBasicPermissionsAndAudio() {
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.CAMERA, Manifest.permission
                        .WRITE_EXTERNAL_STORAGE, Manifest.permission.RECORD_AUDIO},
                CODE_BASIC_AND_AUDIO_PERMISSIONS);
    }

    @TargetApi(Build.VERSION_CODES.M)
    private void requestBasicPermissions() {
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.CAMERA, Manifest.permission
                        .WRITE_EXTERNAL_STORAGE},
                CODE_BASIC_PERMISSIONS);
    }

    @TargetApi(Build.VERSION_CODES.M)
    private void requestCameraPermission() {
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.CAMERA},
                CODE_CAMERA_PERMISSION);
    }

    private void requestWritePermission() {
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                CODE_WRITE_PERMISSION);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case CODE_BASIC_PERMISSIONS: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length == 2
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED
                        && grantResults[1] == PackageManager.PERMISSION_GRANTED) {

                    setViewsPermissionsGranted();

                    // Makes onResume re-check the permission if the user accepted it the first
                    // place and then disabled
                    // it manually
                    // Accept permission - becomes false - user goes to settings - disables
                    // permission - onResume - this
                    // is false, hasOverlayPermission is false - rechecks permission
                    declinedBasicPermissions = false;
                } else {
                    if (grantResults.length == 2
                            && grantResults[0] == PackageManager.PERMISSION_DENIED
                            && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                        setViewsNoCamera();
                    } else if (grantResults.length == 2
                            && grantResults[0] == PackageManager.PERMISSION_GRANTED
                            && grantResults[1] == PackageManager.PERMISSION_DENIED) {
                        setViewsNoWrite();
                    } else if (grantResults.length == 2
                            && grantResults[0] == PackageManager.PERMISSION_DENIED
                            && grantResults[1] == PackageManager.PERMISSION_DENIED) {
                        setViewsNoBasic();
                    } else {
                        throw new IllegalStateException("OnRequestPermission result should have 2" +
                                " items");
                    }

                    // If permission was asked for but has not been granted, don't retry (only
                    // explicitly when the user
                    // presses retry)
                    declinedBasicPermissions = true;
                }

                break;
            }

            case CODE_BASIC_AND_AUDIO_PERMISSIONS: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length == 3
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED
                        && grantResults[1] == PackageManager.PERMISSION_GRANTED) {

                    setViewsPermissionsGranted();

                    // Makes onResume re-check the permission if the user accepted it the first
                    // place and then disabled
                    // it manually
                    // Accept permission - becomes false - user goes to settings - disables
                    // permission - onResume - this
                    // is false, hasOverlayPermission is false - rechecks permission
                    declinedBasicPermissions = false;
                } else {
                    if (grantResults.length == 3
                            && grantResults[0] == PackageManager.PERMISSION_DENIED
                            && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                        setViewsNoCamera();
                    } else if (grantResults.length == 3
                            && grantResults[0] == PackageManager.PERMISSION_GRANTED
                            && grantResults[1] == PackageManager.PERMISSION_DENIED) {
                        setViewsNoWrite();
                    } else if (grantResults.length == 3
                            && grantResults[0] == PackageManager.PERMISSION_DENIED
                            && grantResults[1] == PackageManager.PERMISSION_DENIED) {
                        setViewsNoBasic();
                    } else {
                        throw new IllegalStateException("OnRequestPermission result should have 3" +
                                " items");
                    }

                    // If permission was asked for but has not been granted, don't retry (only
                    // explicitly when the user
                    // presses retry)
                    declinedBasicPermissions = true;
                }

                if (grantResults.length == 3 && grantResults[2] == PackageManager.PERMISSION_GRANTED)
                    SharedPreferencesHelper.putHasDeclinedAudio(this, false);
                else if (grantResults.length == 3 && grantResults[2] == PackageManager.PERMISSION_DENIED)
                    SharedPreferencesHelper.putHasDeclinedAudio(this, true);

                break;
            }

            case CODE_CAMERA_PERMISSION: {
                if (hasCameraPermission()) {
                    if (hasWritePermission())
                        setViewsPermissionsGranted();
                    else setViewsNoWrite();

                    declinedBasicPermissions = false;
                } else {
                    if (!hasWritePermission())
                        setViewsNoBasic();
                    else
                        setViewsNoCamera();

                    declinedBasicPermissions = true;
                }

                break;
            }

            case CODE_WRITE_PERMISSION: {
                if (hasWritePermission()) {
                    if (hasCameraPermission())
                        setViewsPermissionsGranted();
                    else setViewsNoCamera();

                    declinedBasicPermissions = false;
                } else {
                    if (!hasCameraPermission())
                        setViewsNoBasic();
                    else
                        setViewsNoWrite();

                    declinedBasicPermissions = true;
                }

                break;
            }
        }
    }

    private void setViewsNoOverlay() {
        viewNoOverlay.setVisibility(View.VISIBLE);
        viewNoCamera.setVisibility(View.GONE);
        viewNoWrite.setVisibility(View.GONE);
        viewNoBasicPermissions.setVisibility(View.GONE);
    }

    private void setViewsNoCamera() {
        viewNoOverlay.setVisibility(View.GONE);
        viewNoCamera.setVisibility(View.VISIBLE);
        viewNoWrite.setVisibility(View.GONE);
        viewNoBasicPermissions.setVisibility(View.GONE);
    }

    private void setViewsNoWrite() {
        viewNoOverlay.setVisibility(View.GONE);
        viewNoCamera.setVisibility(View.GONE);
        viewNoWrite.setVisibility(View.VISIBLE);
        viewNoBasicPermissions.setVisibility(View.GONE);
    }

    private void setViewsNoBasic() {
        viewNoOverlay.setVisibility(View.GONE);
        viewNoCamera.setVisibility(View.GONE);
        viewNoWrite.setVisibility(View.GONE);
        viewNoBasicPermissions.setVisibility(View.VISIBLE);
    }

    private void setViewsPermissionsGranted() {
        viewNoOverlay.setVisibility(View.GONE);
        viewNoCamera.setVisibility(View.GONE);
        viewNoWrite.setVisibility(View.GONE);
        viewNoBasicPermissions.setVisibility(View.GONE);
    }
}
