package com.vizdashcam.activities;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;

import com.vizdashcam.GlobalState;
import com.vizdashcam.R;
import com.vizdashcam.ServicePreview;
import com.vizdashcam.utils.PermissionUtils;

public class MainActivity extends AppCompatActivity {

    public String TAG = "MainActivity";

    private Messenger messenger;
    private GlobalState appState = null;
    private ServiceConnection connection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {

            messenger = new Messenger(service);
            appState.setPreviewBound(true);

            if (appState.isActivityPaused()) {
                appState.setActivityPaused(false);
                sendMessageToService(ServicePreview.MSG_RESIZE);
            }
        }

        public void onServiceDisconnected(ComponentName className) {
            messenger = null;
            appState.setPreviewBound(false);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViews();
        initViews();

        appState = (GlobalState) getApplicationContext();
    }

    private void findViews() {
    }

    private void initViews() {
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (PermissionUtils.computeNecessaryPermissions(MainActivity.this).size() > 0) {
            Intent intent = new Intent(MainActivity.this, PermissionsActivity.class);
            startActivity(intent);
        } else {
            initService();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (!appState.isActivityPaused()) {

            appState.setActivityPaused(true);

            if (appState.isRecording()) {
                sendMessageToService(ServicePreview.MSG_RESIZE);
                if (appState.isPreviewBound()) {
                    unbindService(connection);
                }
            } else {
                if (appState.isPreviewBound()) {
                    unbindService(connection);
                }
                stopService(new Intent(MainActivity.this, ServicePreview.class));
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (!appState.isActivityPaused()) {

            appState.setActivityPaused(true);

            if (appState.isRecording()) {
                sendMessageToService(ServicePreview.MSG_RESIZE);
                if (appState.isPreviewBound())
                    unbindService(connection);
            } else {
                if (appState.isPreviewBound())
                    unbindService(connection);
                stopService(new Intent(MainActivity.this, ServicePreview.class));
            }
        }
    }

    private void initService() {

        Intent startForegroundIntent = new Intent(
                ServicePreview.ACTION_FOREGROUND);
        startForegroundIntent.setClass(MainActivity.this, ServicePreview.class);
        startService(startForegroundIntent);

        bindService(new Intent(this, ServicePreview.class), connection,
                Context.BIND_AUTO_CREATE);
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        sendMessageToService(ServicePreview.MSG_OM);
        return false;
    }

    private void sendMessageToService(int intvaluetosend) {
        if (appState.isPreviewBound()) {
            if (messenger != null) {
                try {
                    Message msg = Message.obtain(null, intvaluetosend, 0, 0);
                    messenger.send(msg);
                } catch (RemoteException e) {
                }
            }
        }
    }
}
