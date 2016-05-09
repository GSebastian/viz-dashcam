package com.vizdashcam.activities;

import java.io.File;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Vibrator;
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
import com.vizdashcam.VideoItem;
import com.vizdashcam.VideoPreview;
import com.vizdashcam.utils.FeedbackSoundPlayer;

public class ActivityVideoItem extends Activity {

	public static final String TAG = "VideoItemActivity";

	ImageView ivPreview;
	ImageView ivPlay;
	VideoItem mVideoItem;
	TextView tvLength;
	TextView tvSize;
	LinearLayout llShock;
	TextView tvTitle;

	Button btnDelete;
	Button btnUpload;

	boolean isVideoMarked;
	String videoItemName;

	GlobalState mAppState;

	public static final int RESULT_BECAME_MARKED = 5;
	public static final int RESULT_BECAME_NORMAL = 6;
	public static final int RESULT_DELETE = 7;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_video_item);

		mAppState = (GlobalState) getApplicationContext();

		Bundle extras = getIntent().getExtras();
		if (extras != null) {
			File value = (File) extras.getSerializable("video_item");
			mVideoItem = new VideoItem(value);
			if (mVideoItem.isMarked())
				isVideoMarked = true;
			else
				isVideoMarked = false;
			videoItemName = mVideoItem.getName();
		}

		ivPreview = (ImageView) findViewById(R.id.iv_preview);
		VideoPreview.getFileIcon(mVideoItem.getFile(), ivPreview);

		ivPlay = (ImageView) findViewById(R.id.iv_play);
		ivPlay.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				try {
					tactileFeedback();
					audioFeedback();

					Uri videoUri = Uri.parse(mVideoItem.getPath());
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

		tvTitle = (TextView) findViewById(R.id.tv_title);
		tvTitle.setText(mVideoItem.getName());
		getActionBar().setTitle(mVideoItem.getName());

		tvSize = (TextView) findViewById(R.id.tv_size);
		tvSize.setText(mVideoItem.getSize() + "MB");

		tvLength = (TextView) findViewById(R.id.tv_length);
		tvLength.setText(mVideoItem.getDuration() + " min");

		llShock = (LinearLayout) findViewById(R.id.ll_shock);
		if (!mVideoItem.isMarked()) {
			llShock.setAlpha(0.1f);
		}
		llShock.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				if (!isVideoMarked) {
					isVideoMarked = true;
					markVideoTitle();
					tvTitle.setText(videoItemName);
					getActionBar().setTitle(videoItemName);

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
					getActionBar().setTitle(videoItemName);

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
								+ mVideoItem.getFile().getAbsolutePath()));
				intentShareFile.putExtra(Intent.EXTRA_SUBJECT,
						mVideoItem.getName());

				Intent chooser = Intent.createChooser(intentShareFile,
						"Share file");
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
		if (mAppState.detectTactileFeedbackActive()) {
			Vibrator vibrator = (Vibrator) this
					.getSystemService(Context.VIBRATOR_SERVICE);
			vibrator.vibrate(100);
		}
	}

	private void audioFeedback() {
		if (mAppState.detectAudioFeedbackButtonActive()) {
			FeedbackSoundPlayer.playSound(FeedbackSoundPlayer.SOUND_BTN);
		}
	}
}
