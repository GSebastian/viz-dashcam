package com.vizdashcam.activities;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.vizdashcam.R;
import com.vizdashcam.fragments.FragmentPreferences;

public class SettingsActivity extends AppCompatPreferenceActivity {

    public static final int CODE_AUDIO_RECORDING_PERMISSION = 555;
    public static final int CODE_FINE_LOCATION_PERMISSION = 777;
    private static final String TAG = "Settings";
    private Toolbar toolbar;

    private FragmentPreferences preferencesFragment;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        initViews();
        findViews();

        preferencesFragment = new FragmentPreferences();
        // Android API looks through rootView for @android:id/list and replaces that with the
        // FragmentPreferences instance
        getFragmentManager().beginTransaction()
                .replace(R.id.rootView, preferencesFragment).commit();
    }

    private void initViews() {
        toolbar = (Toolbar) findViewById(R.id.toolbar);
    }

    private void findViews() {
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }

    private void startSettingsActivity(int requestCode) {
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package", getPackageName(), null);
        intent.setData(uri);
        startActivityForResult(intent, requestCode);
    }

    @TargetApi(Build.VERSION_CODES.M)
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == CODE_AUDIO_RECORDING_PERMISSION) {
            if (hasAudioRecordingPermission()) {
                preferencesFragment.obtainedAudioRecordingPermission();
            } else {
                if (shouldShowRequestPermissionRationale(Manifest.permission.RECORD_AUDIO)) {
                    // User denied without permission without checking "Never ask again"

                    com.vizdashcam.utils.ViewUtils.createTwoButtonDialog(this, R.string.permission_explanation_audio,
                            R.string.cancel, R.string.grant_permission, null, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    requestAudioRecordingPermission();
                                }
                            }
                    ).show();
                } else {
                    // User denied and clicked "Never ask again"

                    com.vizdashcam.utils.ViewUtils.createTwoButtonDialog(this, R.string.permission_explanation_audio,
                            R.string.cancel, R.string.grant_permission, null, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    startSettingsActivity(CODE_AUDIO_RECORDING_PERMISSION);
                                }
                            }
                    ).show();
                }
            }
        } else if (requestCode == CODE_FINE_LOCATION_PERMISSION) {
            if (hasFineLocationPermission()) {
                preferencesFragment.obtainedLocationPermission();
            } else {
                if (shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION)) {
                    // User denied without permission without checking "Never ask again"

                    com.vizdashcam.utils.ViewUtils.createTwoButtonDialog(this, R.string.permission_explanation_location,
                            R.string.cancel, R.string.grant_permission, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    preferencesFragment.lostLocationPermission();
                                }
                            }, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    requestFineLocationPermission();
                                }
                            }
                    ).show();
                } else {
                    // User denied and clicked "Never ask again"

                    com.vizdashcam.utils.ViewUtils.createTwoButtonDialog(this, R.string.permission_explanation_location,
                            R.string.cancel, R.string.grant_permission, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    preferencesFragment.lostLocationPermission();
                                }
                            }, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    startSettingsActivity(CODE_FINE_LOCATION_PERMISSION);
                                }
                            }
                    ).show();
                }
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CODE_AUDIO_RECORDING_PERMISSION)
            if (hasAudioRecordingPermission())
                preferencesFragment.obtainedAudioRecordingPermission();
            else preferencesFragment.lostAudioRecordingPermission();
        else if (requestCode == CODE_FINE_LOCATION_PERMISSION)
            if (hasFineLocationPermission())
                preferencesFragment.obtainedLocationPermission();
            else preferencesFragment.lostLocationPermission();
    }

    @TargetApi(Build.VERSION_CODES.M)
    private void requestAudioRecordingPermission() {
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.RECORD_AUDIO},
                SettingsActivity.CODE_AUDIO_RECORDING_PERMISSION);
    }

    @TargetApi(Build.VERSION_CODES.M)
    private void requestFineLocationPermission() {
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                SettingsActivity.CODE_FINE_LOCATION_PERMISSION);
    }

    @TargetApi(Build.VERSION_CODES.M)
    private boolean hasAudioRecordingPermission() {
        int permissionResultCheck = ContextCompat.checkSelfPermission(this, Manifest.permission
                .RECORD_AUDIO);
        return permissionResultCheck == PackageManager.PERMISSION_GRANTED;
    }

    @TargetApi(Build.VERSION_CODES.M)
    private boolean hasFineLocationPermission() {
        int permissionResultCheck = ContextCompat.checkSelfPermission(this, Manifest.permission
                .ACCESS_FINE_LOCATION);
        return permissionResultCheck == PackageManager.PERMISSION_GRANTED;
    }
}
