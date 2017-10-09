package com.vizdashcam.activities;

import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.vizdashcam.R;

import uk.co.jakelee.vidsta.VidstaPlayer;

public class VideoPlayerActivity extends AppCompatActivity {

    static String EXTRA_VIDEO_URI = "EXTRA_VIDEO_URI";

    View root;
    VidstaPlayer vpVideoPlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_player);

        findViews();
        initViews();
    }

    private void findViews() {
        vpVideoPlayer = (VidstaPlayer) findViewById(R.id.vpVideoPlayer);
        root = findViewById(R.id.rootView);
    }

    private void initViews() {
        Uri videoUri = getIntent().getParcelableExtra(EXTRA_VIDEO_URI);
        vpVideoPlayer.setAutoPlay(true);
        vpVideoPlayer.setAutoLoop(true);
        vpVideoPlayer.setFullScreenButtonVisible(false);
        vpVideoPlayer.setVideoSource(videoUri);

        root.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_IMMERSIVE);
    }
}
