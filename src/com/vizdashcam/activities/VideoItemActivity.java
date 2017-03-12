package com.vizdashcam.activities;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MenuItem.OnMenuItemClickListener;
import android.view.View;
import android.view.View.OnClickListener;
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

public class VideoItemActivity extends AppCompatActivity {

    public static final String TAG = "VideoItemActivity";
    public static final String KEY_VIDEO_ITEM = "video_item";

    View rootView;
    Toolbar toolbar;
    ImageView ivPreview;
    ImageView ivPlay;
    VideoItem videoItem;
    TextView tvLength;
    TextView tvSize;
    LinearLayout llShock;
    TextView tvTitle;

    GlobalState appState;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_item);

        appState = (GlobalState) getApplicationContext();

        Bundle extras = getIntent().getExtras();
        if (extras != null) videoItem = (VideoItem) extras.getSerializable(KEY_VIDEO_ITEM);

        findViews();
        initViews();
    }

    private void findViews() {
        rootView = findViewById(R.id.rootView);
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
                    Toast.makeText(VideoItemActivity.this,
                            "No video player detected", Toast.LENGTH_LONG)
                            .show();
                }
            }
        });

        tvTitle.setText(videoItem.getName());

        tvSize.setText(getString(R.string.megabyte, videoItem.getSize()));

        tvLength.setText(getResources().getQuantityString(R.plurals.minutes, videoItem.getDuration()));

        if (!videoItem.isMarked()) {
            llShock.setAlpha(0.1f);
        }

        llShock.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (!videoItem.isMarked()) {

                    String newVideoTitle = videoItem.setMarked(true);
                    tvTitle.setText(newVideoTitle);
                    getSupportActionBar().setTitle(newVideoTitle);

                    FeedbackSoundPlayer
                            .playSound(FeedbackSoundPlayer.SOUND_MARKED);
                    llShock.setAlpha(1f);

                    Snackbar.make(rootView, R.string.file_marked, Snackbar.LENGTH_LONG)
                            .setActionTextColor(getResources().getColor(R.color.White))
                            .setAction(R.string.got_it, new OnClickListener() {
                                @Override
                                public void onClick(View v) {}
                            })
                            .show();
                } else {

                    String newVideoTitle = videoItem.setMarked(false);
                    tvTitle.setText(newVideoTitle);
                    getSupportActionBar().setTitle(newVideoTitle);

                    FeedbackSoundPlayer
                            .playSound(FeedbackSoundPlayer.SOUND_MARKED);
                    llShock.setAlpha(0.1f);

                    Snackbar.make(rootView, R.string.file_unmarked, Snackbar.LENGTH_LONG)
                            .setActionTextColor(getResources().getColor(R.color.White))
                            .setAction(R.string.got_it, new OnClickListener() {
                                @Override
                                public void onClick(View v) {}
                            })
                            .show();
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

                videoItem.getFile().delete();

                tactileFeedback();
                audioFeedback();

                finish();

                return true;
            }
        });

        return true;
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
