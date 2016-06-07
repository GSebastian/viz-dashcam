package com.vizdashcam;

import android.Manifest;
import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.animation.ObjectAnimator;
import android.annotation.TargetApi;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.PixelFormat;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.location.LocationManager;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Messenger;
import android.os.Vibrator;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.MarginLayoutParams;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.vizdashcam.activities.ActivityMain;
import com.vizdashcam.activities.ActivitySettings;
import com.vizdashcam.activities.ActivityVideoList;
import com.vizdashcam.activities.DialogStorage;
import com.vizdashcam.managers.AccelerometerManager;
import com.vizdashcam.managers.StorageManager;
import com.vizdashcam.utils.CameraUtils;
import com.vizdashcam.utils.FeedbackSoundPlayer;
import com.vizdashcam.utils.Utils;

import java.io.File;
import java.io.IOException;


public class ServicePreview extends Service implements
        MediaRecorder.OnInfoListener {

    private static final String TAG = "PreviewService";

    private static final int previewNotificationId = 222;

    private static final int VALUE_MENU_TRANSITION_TIME = 1000;

    public static final String ACTION_FOREGROUND = "com.vizdashcam.PrevievService.FOREGROUND";

    private GlobalState mAppState;
    private WindowManager mWindowManager;

    private Camera mServiceCamera;
    private MediaRecorder mMediaRecorder;

    private LocationManager mLocationManager;
    private SpeedListener mSpeedListener;

    private ViewGroup fullLayout;
    private ImageView ivRecord;
    private TextView tvSpeed;
    private RelativeLayout rlMenu;
    private RelativeLayout rlMenuLeftColumn;
    private TextureView cameraPreview;
    private ExpandingCircleView expandingCircle;

    private Handler mHandler;

    public enum SidebarState {
        OPEN, CLOSED
    }

    private SidebarState sidebarState;

    private LayoutParams mLayoutParamsFull;
    private LayoutParams mLayoutParamsMinimised;

    public static final int MSG_OM = 1;
    public static final int MSG_RESIZE = 2;

    public static final int DELAY_RECORD_DISABLE = 2000;

    private Messenger mMessenger;

    private StorageManager mStorageManager;

    private float lastTouchX;
    private float lastTouchY;

    @Override
    public IBinder onBind(Intent arg0) {
        return mMessenger.getBinder();
    }

    @Override
    public void onCreate() {
        super.onCreate();

        mAppState = (GlobalState) getApplicationContext();

        mMessenger = new Messenger(new PreviewIncomingHandler(this));

        mWindowManager = (WindowManager) this
                .getSystemService(Context.WINDOW_SERVICE);

        mLocationManager = (LocationManager) this
                .getSystemService(LOCATION_SERVICE);

        initUI();

        mHandler = new Handler();

        mMediaRecorder = new MediaRecorder();
        mMediaRecorder.setOnInfoListener(this);

        startForeground(previewNotificationId, getPreviewNotification());
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (mServiceCamera != null) {
            if (mAppState.isRecording()) {
                stopRecording();
            }

            mServiceCamera.stopPreview();
            mServiceCamera.setPreviewCallback(null);
            mServiceCamera.release();
            mServiceCamera = null;
        }
        mWindowManager.removeView(fullLayout);
        removeLocationUpdates();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_NOT_STICKY;
    }

    private void performSidebarOutAnimation() {
        fullLayout.setLayerType(View.LAYER_TYPE_HARDWARE, null);
        ObjectAnimator anim = ObjectAnimator.ofFloat(rlMenu,
                "translationX", 0);
        anim.setInterpolator(new SmoothInterpolator());
        anim.addListener(new AnimatorListener() {

            @Override
            public void onAnimationEnd(Animator animation) {
                fullLayout.setLayerType(View.LAYER_TYPE_NONE, null);
            }

            @Override
            public void onAnimationCancel(Animator animation) {
            }

            @Override
            public void onAnimationRepeat(Animator animation) {
            }

            @Override
            public void onAnimationStart(Animator animation) {
            }

        });
        anim.setDuration(VALUE_MENU_TRANSITION_TIME);
        anim.start();
        sidebarState = SidebarState.CLOSED;
    }

    private void performSidebarInAnimation() {
        final int sidebarWidth = rlMenuLeftColumn.getLayoutParams().width;

        rlMenu.setLayerType(View.LAYER_TYPE_HARDWARE, null);
        ObjectAnimator anim = ObjectAnimator.ofFloat(rlMenu,
                "translationX", sidebarWidth);
        anim.setInterpolator(new SmoothInterpolator());
        anim.addListener(new AnimatorListener() {

            @Override
            public void onAnimationEnd(Animator animation) {
                fullLayout.setLayerType(View.LAYER_TYPE_NONE, null);
            }

            @Override
            public void onAnimationCancel(Animator animation) {
            }

            @Override
            public void onAnimationRepeat(Animator animation) {
            }

            @Override
            public void onAnimationStart(Animator animation) {
            }
        });
        anim.setDuration(VALUE_MENU_TRANSITION_TIME);
        anim.start();
        sidebarState = SidebarState.OPEN;
    }

    private void initCameraPreview() {
        try {
            mServiceCamera = Camera.open(CameraInfo.CAMERA_FACING_BACK);
            if (mServiceCamera != null) {

                setCameraParams();
            }
        } catch (RuntimeException re) {
            Log.e(TAG, "Error opening camera! " + re.getMessage());
        }
    }

    private void setCameraParams() {
        Camera.Parameters parameters = mServiceCamera.getParameters();
        parameters.setRecordingHint(true);

        Camera.Size previewSize = CameraUtils.getOptimalPreviewResolution(
                parameters.getSupportedPreviewSizes(), CameraUtils.getDisplayWidth(this),
                CameraUtils.getDisplayHeight(this));

        parameters.setPreviewSize(previewSize.width, previewSize.height);

        mServiceCamera.setParameters(parameters);
    }

    private void initUI() {

        initLayoutParams();

        LayoutInflater inflater = (LayoutInflater) ServicePreview.this.getSystemService(LAYOUT_INFLATER_SERVICE);
        fullLayout = (ViewGroup) inflater.inflate(R.layout.service_preview,
                null);
        fullLayout.setLayerType(View.LAYER_TYPE_HARDWARE, null);

        rlMenu = (RelativeLayout) fullLayout.findViewById(R.id.rl_menu);
        rlMenuLeftColumn = (RelativeLayout) rlMenu
                .findViewById(R.id.rl_menu_left_column);

        MarginLayoutParams params = (MarginLayoutParams) rlMenu
                .getLayoutParams();
        params.leftMargin = -rlMenuLeftColumn.getLayoutParams().width;
        rlMenu.setLayoutParams(params);

        sidebarState = SidebarState.CLOSED;

        Button videos = (Button) rlMenu.findViewById(R.id.btn_videos);
        videos.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                Intent intent = new Intent(ServicePreview.this,
                        ActivityVideoList.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
        });

        Button settings = (Button) rlMenu.findViewById(R.id.btn_settings);
        settings.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                Intent intent = new Intent(ServicePreview.this,
                        ActivitySettings.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
        });

        Button help = (Button) rlMenu.findViewById(R.id.btn_help);
        help.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                String url = "http://www.vizdashcam.com";
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                i.setData(Uri.parse(url));
                startActivity(i);
            }
        });

        tvSpeed = (TextView) fullLayout.findViewById(R.id.tv_speed);

        ivRecord = (ImageView) fullLayout.findViewById(R.id.iv_record);
        ivRecord.setImageResource(R.drawable.btn_not_recording);

        ivRecord.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                tactileFeedback();

                if (!mAppState.isRecording()) {
                    startRecording();
                } else {
                    stopRecording();
                }
            }
        });

        ImageView ivMenu = (ImageView) fullLayout.findViewById(R.id.iv_menu);
        ivMenu.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                tactileFeedback();
                audioFeedback();

                if (sidebarState == SidebarState.CLOSED) {
                    performSidebarInAnimation();
                } else {
                    performSidebarOutAnimation();
                }

            }
        });

        initCameraPreview();
        cameraPreview = (TextureView) fullLayout.findViewById(R.id.txtvCameraPreview);
        cameraPreview.setOpaque(true);
        cameraPreview.setSurfaceTextureListener(new TextureView.SurfaceTextureListener() {
            @Override
            public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
                try {
                    mServiceCamera.setPreviewTexture(cameraPreview.getSurfaceTexture());
                    mServiceCamera.startPreview();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {

            }

            @Override
            public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
                return false;
            }

            @Override
            public void onSurfaceTextureUpdated(SurfaceTexture surface) {

            }
        });


        FrameLayout flFeedback = (FrameLayout) fullLayout
                .findViewById(R.id.fl_feedback);
        final ExpandingCircleView expandingCircle = (ExpandingCircleView) flFeedback.findViewById(R.id.expandingCircle);
        flFeedback.setOnLongClickListener(new View.OnLongClickListener() {

            @Override
            public boolean onLongClick(View arg0) {
                if (mAppState.isRecording()
                        && SharedPreferencesHelper.detectLongPressToMarkActive(mAppState)) {

                    expandingCircle.startAnimation(lastTouchX, lastTouchY);

                    rememberFileForMarking();
                }
                return true;
            }
        });

        flFeedback.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (mAppState.isRecording()
                        && SharedPreferencesHelper.detectLongPressToMarkActive(mAppState)) {
                    final int action = event.getAction();
                    switch (action & MotionEvent.ACTION_MASK) {
                        case MotionEvent.ACTION_DOWN:
                            lastTouchX = event.getX();
                            lastTouchY = event.getY();
                            break;
                    }
                }

                return false;
            }
        });

        flFeedback.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                if (sidebarState == SidebarState.OPEN) {
                    performSidebarOutAnimation();
                }
            }
        });

        View.OnClickListener togglesListener = new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                tactileFeedback();
                audioFeedback();

                Intent intent = new Intent(ServicePreview.this,
                        ActivitySettings.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
        };

        TextView tvQuality = (TextView) fullLayout.findViewById(R.id.tv_quality);
        try {
            int defaultCamcorderProfile = CameraUtils.getCamcorderProfile(this);
            if (defaultCamcorderProfile == CamcorderProfile.QUALITY_1080P) {
                tvQuality.setText("1080P");
            } else if (defaultCamcorderProfile == CamcorderProfile.QUALITY_720P) {
                tvQuality.setText("720P");
            } else if (defaultCamcorderProfile == CamcorderProfile.QUALITY_480P) {
                tvQuality.setText("480P");
            } else if (defaultCamcorderProfile == CamcorderProfile.QUALITY_CIF) {
                tvQuality.setText("CIF");
            } else if (defaultCamcorderProfile == CamcorderProfile.QUALITY_QCIF) {
                tvQuality.setText("QCIF");
            } else if (defaultCamcorderProfile == CamcorderProfile.QUALITY_QVGA) {
                tvQuality.setText("QVGA");
            }
            tvQuality.setOnClickListener(togglesListener);
        } catch (Exception e) {
            Log.e(TAG, "Exception when initialising UI: " + e.getMessage());

            tvQuality.setVisibility(View.GONE);
        }

        TextView tvLength = (TextView) fullLayout.findViewById(R.id.tv_length);
        int ms = SharedPreferencesHelper.detectVideoLength(mAppState);
        int s = ms / 1000;
        if (s < 60) {
            tvLength.setText(s + "SEC");
        } else {
            int min = s / 60;
            tvLength.setText(min + "MIN");
        }
        tvLength.setOnClickListener(togglesListener);

        TextView tvShockActive = (TextView) fullLayout
                .findViewById(R.id.tv_shock_toggled);
        if (SharedPreferencesHelper.detectShockModeActive(mAppState)) {
            tvShockActive.setVisibility(View.VISIBLE);
        } else {
            tvShockActive.setVisibility(View.GONE);
        }
        tvShockActive.setOnClickListener(togglesListener);

        TextView tvLoopActive = (TextView) fullLayout
                .findViewById(R.id.tv_loop_toggled);
        if (SharedPreferencesHelper.detectLoopModeActive(mAppState)) {
            tvLoopActive.setVisibility(View.VISIBLE);
        } else {
            tvLoopActive.setVisibility(View.GONE);
        }
        tvLoopActive.setOnClickListener(togglesListener);

        mWindowManager.addView(fullLayout, mLayoutParamsFull);
    }

    private void initLayoutParams() {

        int displayWidthLandscape = CameraUtils.getDisplayWidth(this);
        int displayHeightLandscape = CameraUtils.getDisplayHeight(this);

        mLayoutParamsMinimised = new WindowManager.LayoutParams(1, 1,
                WindowManager.LayoutParams.TYPE_SYSTEM_ALERT,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE |
                        WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL |
                        WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED,
                PixelFormat.TRANSPARENT);
        mLayoutParamsMinimised.gravity = Gravity.LEFT | Gravity.BOTTOM;

        mLayoutParamsFull = new WindowManager.LayoutParams(
                displayWidthLandscape, displayHeightLandscape,
                WindowManager.LayoutParams.TYPE_SYSTEM_ALERT,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE |
                        WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL |
                        WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED,
                PixelFormat.TRANSLUCENT);
        mLayoutParamsFull.gravity = Gravity.LEFT | Gravity.BOTTOM;
        mLayoutParamsFull.screenOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
    }

    public void adjustPreview() {

        if (mAppState.isActivityPaused()) {
            mWindowManager
                    .updateViewLayout(fullLayout, mLayoutParamsMinimised);
            removeLocationUpdates();
        } else {
            mWindowManager.updateViewLayout(fullLayout, mLayoutParamsFull);
            requestLocationUpdates();
        }
    }

    public void createOptionsMenu() {

        tactileFeedback();
        audioFeedback();

        if (sidebarState == SidebarState.CLOSED) {
            performSidebarInAnimation();
        } else {
            performSidebarOutAnimation();
        }

    }

    private boolean configureVideoRecorder() {
        try {
            mServiceCamera.unlock();
            mMediaRecorder.setCamera(mServiceCamera);

            if (hasAudioRecordingPermission())
                mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.CAMCORDER);

            mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);

            mMediaRecorder.setMaxDuration(SharedPreferencesHelper.detectVideoLength(mAppState));

            mMediaRecorder.setProfile(CamcorderProfile.get(CameraUtils.getCamcorderProfile(this)));

            File outputFile = Utils.getOutputMediaFile();
            String outputFileName = outputFile.toString();
            mMediaRecorder.setOutputFile(outputFileName);
            mAppState.setLastFilename(outputFile.getName());

        } catch (IllegalStateException ise) {
            Log.e(TAG,
                    "IllegalStateException configuring recorder: "
                            + ise.getMessage());
            resetMediaRecorder();
            return false;
        } catch (RuntimeException re) {
            Log.e(TAG, "RuntimeException unlocking camera: " + re.getMessage());
            return false;
        } catch (Exception e) {
            Log.e(TAG, "Exception when unlocking camera: " + e.getMessage());
            return false;
        }

        return true;
    }

    private boolean prepareVideoRecorder() {

        if (configureVideoRecorder()) {

            try {
                mMediaRecorder.prepare();
            } catch (IllegalStateException e) {
                Log.e(TAG, "IllegalStateException preparing MediaRecorder: "
                        + e.getMessage());
                resetMediaRecorder();
                return false;
            } catch (IOException e) {
                Log.e(TAG,
                        "IOException preparing MediaRecorder: "
                                + e.getMessage());
                resetMediaRecorder();
                return false;
            }

            return true;
        }

        return false;
    }

    private void resetMediaRecorder() {
        if (mMediaRecorder != null) {
            mMediaRecorder.reset();
        }
    }

    public void startRecording() {
        if (mServiceCamera != null) {

            if (!mAppState.isRecording()) {

                mStorageManager = new StorageManager(getApplicationContext(),
                        this, mHandler);

                if (!StorageManager.hasRunOutOufSpace()) {

                    if (prepareVideoRecorder()) {

                        disableRecordButton();
                        mAppState.setRecording(true);
                        mAppState.setMustMarkFile(false);

                        mMediaRecorder.start();

                        mStorageManager.start();

                        if (SharedPreferencesHelper.detectShockModeActive(mAppState)) {
                            startListeningForShocks();
                        }

                        ivRecord.setImageResource(R.drawable.btn_recording);

                    } else {
                        resetMediaRecorder();
                    }
                } else {
                    displayStorageDialog();
                }
            }
        }
    }

    public void stopRecording() {
        if (mServiceCamera != null) {
            if (mAppState.isRecording()) {
                disableRecordButton();
                mAppState.setRecording(false);

                try {
                    mMediaRecorder.stop();
                    mMediaRecorder.reset();
                } catch (RuntimeException re) {
                    Log.e(TAG, "RuntimeException when stopping MediaRecorder: "
                            + re.getMessage());
                }

                try {
                    mServiceCamera.lock();
                } catch (RuntimeException re) {
                    Log.e(TAG,
                            "RuntimeException when locking camera: "
                                    + re.getMessage());
                }

                mStorageManager.setStopped();
                mStorageManager = null;

                if (SharedPreferencesHelper.detectShockModeActive(mAppState)) {
                    stopListeningForShocks();
                }

                markFileByRenaming();
                updateFragments();

                ivRecord.setImageResource(R.drawable.btn_not_recording);

            }
        }
    }

    private Notification getPreviewNotification() {

        PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
                new Intent(this, ActivityMain.class), 0);

        Notification.Builder builder = new Notification.Builder(this)
                .setSmallIcon(R.drawable.ic_stat_notify).setContentTitle("viz")
                .setContentText("viz is Running")
                .setContentIntent(contentIntent);
        return builder.build();
    }

    private void disableRecordButton() {
        ivRecord.setOnClickListener(null);

        ivRecord.postDelayed(new Runnable() {

            @Override
            public void run() {
                ivRecord.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        tactileFeedback();

                        if (!mAppState.isRecording()) {
                            startRecording();
                        } else {
                            stopRecording();
                        }
                    }
                });
            }
        }, DELAY_RECORD_DISABLE);
    }

    @Override
    public void onInfo(MediaRecorder mr, int what, int extra) {
        if (what == MediaRecorder.MEDIA_RECORDER_INFO_MAX_DURATION_REACHED) {
            Log.v(TAG, "Max Duration");

            disableRecordButton();

            mMediaRecorder.stop();
            mMediaRecorder.reset();

            markFileByRenaming();
            updateFragments();
            mAppState.setMustMarkFile(false);

            if (mServiceCamera != null) {
                if (prepareVideoRecorder()) {
                    mMediaRecorder.start();
                }
            } else {
                ivRecord.setImageResource(R.drawable.btn_not_recording);
            }
        }
    }

    private void updateFragments() {

        File dir = mAppState.getMediaStorageDir();
        if (dir.exists()) {
            File file = new File(dir, mAppState.getLastFilename());

            broadcastAdd(new VideoItem(file));
        }
    }

    private void markFileByRenaming() {
        if (mAppState.getMustMarkFile()) {
            File dir = mAppState.getMediaStorageDir();
            if (dir.exists()) {
                String lastFileName = mAppState.getLastFilename();
                StringBuilder stringBuilder = new StringBuilder(lastFileName);
                int lastPointPosition = lastFileName.lastIndexOf(".");
                if (lastPointPosition != -1) {
                    stringBuilder.insert(lastPointPosition,
                            VideoItem.EXTENSION_MARKED_FILE);
                    String markedFileName = stringBuilder.toString();
                    File from = new File(dir, lastFileName);
                    File to = new File(dir, markedFileName);
                    if (from.exists())
                        from.renameTo(to);

                    mAppState.setLastFilename(markedFileName);
                    mAppState.setLastMarkedFilename(markedFileName);
                }
            }
        }
    }

    public void displayStorageDialog() {
        if (!mAppState.isActivityPaused()) {
            mHandler.post(new Runnable() {

                @Override
                public void run() {
                    Intent intent = new Intent(getApplicationContext(),
                            DialogStorage.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);

                }
            });
        }
    }

    private void stopListeningForShocks() {
        if (AccelerometerManager.isListening()) {
            AccelerometerManager.stopListening();
        }
    }

    private void startListeningForShocks() {
        if (AccelerometerManager.isSupported(mAppState)) {
            AccelerometerManager.startListening(new ShockListener(mAppState));
        }
    }

    private void tactileFeedback() {
        if (SharedPreferencesHelper.detectTactileFeedbackActive(mAppState)) {
            Vibrator vibrator = (Vibrator) this
                    .getSystemService(Context.VIBRATOR_SERVICE);
            vibrator.vibrate(100);
        }
    }

    private void audioFeedback() {
        if (SharedPreferencesHelper.detectAudioFeedbackButtonActive(mAppState)) {
            FeedbackSoundPlayer.playSound(FeedbackSoundPlayer.SOUND_BTN);
        }
    }

    private void rememberFileForMarking() {
        String lastGlobalFilename = mAppState.getLastFilename();
        String lastMarkedFilename = mAppState.getLastMarkedFilename();
        if (lastGlobalFilename.compareTo(lastMarkedFilename) != 0) {
            mAppState.setMustMarkFile(true);
            FeedbackSoundPlayer.playSound(FeedbackSoundPlayer.SOUND_MARKED);
            mAppState.setLastMarkedFilename(lastGlobalFilename);
        }
    }

    private void requestLocationUpdates() {
        if (SharedPreferencesHelper.detectSpeedometerActive(mAppState)) {
            if (hasFineLocationPermission()) {
                if (mSpeedListener != null) {
                    mSpeedListener.initView();
                    mLocationManager.requestLocationUpdates(
                            LocationManager.GPS_PROVIDER, 0, 0, mSpeedListener);
                } else {
                    mSpeedListener = new SpeedListener(tvSpeed, mAppState);
                    mSpeedListener.initView();
                    mLocationManager.requestLocationUpdates(
                            LocationManager.GPS_PROVIDER, 0, 0, mSpeedListener);
                }
            } else {

            }
        }
    }

    private void removeLocationUpdates() {
        if (mSpeedListener != null) {
            mLocationManager.removeUpdates(mSpeedListener);
            tvSpeed.setVisibility(View.INVISIBLE);
        }
    }

    public class SmoothInterpolator extends LinearInterpolator {

        @Override
        public float getInterpolation(float input) {
            return (float) Math.pow(input - 1, 5) + 1;
        }

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

    private void broadcastAdd(VideoItem video) {
        Intent intent = new Intent(ActivityVideoList.ACTION_ADD_VIDEO);
        intent.putExtra(ActivityVideoList.KEY_VIDEO, video);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }
}
