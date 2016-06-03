package com.vizdashcam.activities;

import java.io.File;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MenuItem.OnMenuItemClickListener;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.vizdashcam.GlobalState;
import com.vizdashcam.R;
import com.vizdashcam.SharedPreferencesHelper;
import com.vizdashcam.VideoItem;
import com.vizdashcam.VideoPreview;
import com.vizdashcam.utils.FeedbackSoundPlayer;

public class ActivityVideoItem extends AppCompatActivity {

    public static final String TAG = "VideoItemActivity";
    public static final String KEY_VIDEO_ITEM = "video_item";

    Toolbar toolbar;
    ImageView ivPreview;
    ImageView ivPlay;
    VideoItem videoItem;
    TextView tvLength;
    TextView tvSize;
    LinearLayout llShock;
    TextView tvTitle;

    Button btnDelete;
    Button btnUpload;

    boolean isVideoMarked;
    String videoItemName;

    GlobalState appState;

    public static final int RESULT_BECAME_MARKED = 5;
    public static final int RESULT_BECAME_NORMAL = 6;
    public static final int RESULT_DELETE = 7;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_item);

        appState = (GlobalState) getApplicationContext();

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            File value = (File) extras.getSerializable(KEY_VIDEO_ITEM);
            videoItem = new VideoItem(value);

            if (videoItem.isMarked())
                isVideoMarked = true;
            else
                isVideoMarked = false;

            videoItemName = videoItem.getName();
        }

        findViews();
        initViews();
    }

    private void findViews() {
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        ivPreview = (ImageView) findViewById(R.id.iv_preview);
        ivPlay = (ImageView) findViewById(R.id.iv_play);
        tvTitle = (TextView) findViewById(R.id.tv_title);
        tvSize = (TextView) findViewById(R.id.tv_size);
        tvLength = (TextView) findViewById(R.id.tv_length);
        llShock = (LinearLayout) findViewById(R.id.ll_shock);
    }

    private void initViews() {
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        VideoPreview.getFileIcon(videoItem.getFile(), ivPreview);

        ivPlay.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                try {
                    tactileFeedback();
                    audioFeedback();

                    Uri videoUri = Uri.parse(videoItem.getPath());
                    Intent intent = new Intent(Intent.ACTION_VIEW, videoUri);
                    intent.setDataAndType(videoUri, "video/*");
                    startActivity(intent);
                } catch (ActivityNotFoundException e) {
                    Toast.makeText(ActivityVideoItem.this,
                            "No video player detected", Toast.LENGTH_LONG)
                            .show();
                }
            }
        });

        tvTitle.setText(videoItem.getName());

        tvSize.setText(videoItem.getSize() + "MB");

        tvLength.setText(videoItem.getDuration() + " min");

        if (!videoItem.isMarked()) {
            llShock.setAlpha(0.1f);
        }

        llShock.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (!isVideoMarked) {
                    isVideoMarked = true;
                    markVideoTitle();
                    tvTitle.setText(videoItemName);
                    getSupportActionBar().setTitle(videoItemName);

                    FeedbackSoundPlayer
                            .playSound(FeedbackSoundPlayer.SOUND_MARKED);
                    llShock.setAlpha(1f);
                    Toast.makeText(ActivityVideoItem.this, "File marked!",
                            Toast.LENGTH_SHORT).show();

                    setResult(RESULT_BECAME_MARKED);
                } else {
                    isVideoMarked = false;
                    unmarkVideoTitle();
                    tvTitle.setText(videoItemName);
                    getSupportActionBar().setTitle(videoItemName);

                    FeedbackSoundPlayer
                            .playSound(FeedbackSoundPlayer.SOUND_MARKED);
                    llShock.setAlpha(0.1f);
                    Toast.makeText(ActivityVideoItem.this, "File unmarked!",
                            Toast.LENGTH_SHORT).show();

                    setResult(RESULT_BECAME_NORMAL);
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_video_item, menu);

        MenuItem miShare = menu.findItem(R.id.menu_item_share);
        MenuItem miDelete = menu.findItem(R.id.menu_item_delete);

        miShare.setOnMenuItemClickListener(new OnMenuItemClickListener() {

            @Override
            public boolean onMenuItemClick(MenuItem item) {
                Intent intentShareFile = new Intent(Intent.ACTION_SEND);
                intentShareFile.setType("video/mp4");
                intentShareFile.putExtra(
                        Intent.EXTRA_STREAM,
                        Uri.parse("file://"
                                + videoItem.getFile().getAbsolutePath()));
                intentShareFile.putExtra(Intent.EXTRA_SUBJECT,
                        videoItem.getName());

                Intent chooser = Intent.createChooser(intentShareFile,
                        getString(R.string.share_video));

                if (intentShareFile.resolveActivity(getPackageManager()) != null) {
                    startActivity(chooser);
                }

                return true;
            }
        });

        miDelete.setOnMenuItemClickListener(new OnMenuItemClickListener() {

            @Override
            public boolean onMenuItemClick(MenuItem arg0) {

                tactileFeedback();
                audioFeedback();

                setResult(RESULT_DELETE);
                finish();

                return true;
            }
        });

        return true;
    }

    private void unmarkVideoTitle() {
        StringBuilder stringBuilder = new StringBuilder(videoItemName);
        int lastPointPosition = videoItemName.lastIndexOf(".");
        if (lastPointPosition != -1) {
            stringBuilder.delete(lastPointPosition
                            - VideoItem.EXTENSION_MARKED_FILE.length(),
                    lastPointPosition);
            videoItemName = stringBuilder.toString();
        }
    }

    private void markVideoTitle() {
        StringBuilder stringBuilder = new StringBuilder(videoItemName);
        int lastPointPosition = videoItemName.lastIndexOf(".");
        if (lastPointPosition != -1) {
            stringBuilder.insert(lastPointPosition,
                    VideoItem.EXTENSION_MARKED_FILE);
            videoItemName = stringBuilder.toString();
        }
    }

    private void tactileFeedback() {
        if (SharedPreferencesHelper.detectTactileFeedbackActive(appState)) {
            Vibrator vibrator = (Vibrator) this
                    .getSystemService(Context.VIBRATOR_SERVICE);
            vibrator.vibrate(100);
        }
    }

    private void audioFeedback() {
        if (SharedPreferencesHelper.detectAudioFeedbackButtonActive(appState)) {
            FeedbackSoundPlayer.playSound(FeedbackSoundPlayer.SOUND_BTN);
        }
    }
}
