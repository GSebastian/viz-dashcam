package com.vizdashcam;

import android.media.MediaMetadataRetriever;
import android.os.Environment;
import android.util.Log;

import org.joda.time.DateTime;

import java.io.File;
import java.io.Serializable;
import java.util.Comparator;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class VideoItem implements Serializable {

	public static final String TAG = "VideoItem";
	public static final String EXTENSION_MARKED_FILE = "_M";
	private File mediaStorageDir = new File(
			Environment.getExternalStorageDirectory(), "vizDashcamApp");

	private File mFile;
	private String mPath;
	private String mName;
	private String mSize;
	private String mDuration;
	private DateTime mDate;
	private boolean mShowDate;
	@SuppressWarnings("unused")
	private boolean isMarked;

	public VideoItem(final File file) {
		mFile = file;
		mPath = mFile.getAbsolutePath();
		mName = mFile.getName();
		setFileSize();
		setVideoDuration();
		setDate();
		mShowDate = false;
		if (isMarked()) {
			isMarked = true;
		} else {
			isMarked = false;
		}
	}

	public VideoItem(VideoItem video) {
		this.mFile = video.getFile();
		this.mPath = this.mFile.getAbsolutePath();
		this.mName = this.mFile.getName();
		this.mSize = video.getSize();
		this.mDuration = video.getDuration();
		this.mDate = video.getDate();
		this.mShowDate = false;
		if (isMarked()) {
			isMarked = true;
		} else {
			isMarked = false;
		}
	}

	private void setDate() {
		if (checkFileNameValidity()) {
			int year = Integer.parseInt(mName.substring(4, 8));
			int month = Integer.parseInt(mName.substring(8, 10));
			int day = Integer.parseInt(mName.substring(10, 12));
			int hour = Integer.parseInt(mName.substring(13, 15));
			int minutes = Integer.parseInt(mName.substring(15, 17));
			int seconds = Integer.parseInt(mName.substring(17, 19));

			mDate = new DateTime(year, month, day, hour, minutes, seconds);
		} else {
			mDate = new DateTime(1993, 10, 1, 5, 0, 0);
		}
	}

	private void setFileSize() {
		long length = mFile.length();
		long divBy = 1024 * 1024;

		if (length > divBy) {
			mSize = String.valueOf(length / divBy);
		} else {
			mSize = String.format(Locale.US, "%.1f",
					((float) ((float) mFile.length() / (float) divBy)));
		}
	}

	private void setVideoDuration() {
		try {
			MediaMetadataRetriever retriever = new MediaMetadataRetriever();
			retriever.setDataSource(mPath);
			String time = retriever
					.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
			long timeInmillisec = Long.parseLong(time);
			long seconds = timeInmillisec / 1000;
			mDuration = String.valueOf(seconds / 60);
		} catch (RuntimeException e) {
			mDuration = "-1";
		}
	}

	public boolean isMarked() {
		int lastPointIndex = mFile.getName().lastIndexOf(".");
		String lastTwoChars = mFile.getName()
				.substring(lastPointIndex - EXTENSION_MARKED_FILE.length(),
						lastPointIndex);

		if (lastTwoChars.compareTo(VideoItem.EXTENSION_MARKED_FILE) == 0) {
			return true;
		}

		return false;
	}

	public static boolean isMarked(String name) {
		int lastPointIndex = name.lastIndexOf(".");
		String lastTwoChars = name.substring(lastPointIndex
				- VideoItem.EXTENSION_MARKED_FILE.length(), lastPointIndex);

		if (lastTwoChars.compareTo(VideoItem.EXTENSION_MARKED_FILE) == 0) {
			return true;
		}

		return false;
	}

	public void setMarked(boolean value) {
		if (value) {
			if (!isMarked()) {
				StringBuilder stringBuilder = new StringBuilder(mName);
				int lastPointPosition = mName.lastIndexOf(".");
				if (lastPointPosition != -1) {
					stringBuilder.insert(lastPointPosition,
							VideoItem.EXTENSION_MARKED_FILE);
					String markedFileName = stringBuilder.toString();
					File from = new File(mediaStorageDir, mName);
					File to = new File(mediaStorageDir, markedFileName);
					mName = markedFileName;
					if (from.exists())
						from.renameTo(to);
					mFile = to;
					mPath = to.getPath();
					isMarked = true;
				}
			}
		} else if (isMarked()) {
			StringBuilder stringBuilder = new StringBuilder(mName);
			int lastPointPosition = mName.lastIndexOf(".");
			if (lastPointPosition != -1) {
				stringBuilder.delete(lastPointPosition - VideoItem.EXTENSION_MARKED_FILE.length(), lastPointPosition);
				String unmarkedFileName = stringBuilder.toString();
				File from = new File(mediaStorageDir, mName);
				File to = new File(mediaStorageDir, unmarkedFileName);
				mName = unmarkedFileName;
				Log.e(TAG, "New mName: " + mName);
				if (from.exists())
					from.renameTo(to);
				mFile = to;
				mPath = to.getPath();
				isMarked = false;
			}
		}
	}

	@Override
	public boolean equals(Object o) {
		if (o instanceof VideoItem) {
			if (((VideoItem) o).getPath().compareTo(this.getPath()) == 0) {
				return true;
			} else {
				return false;
			}
		} else {
			return false;
		}
	}

	public Boolean getShowDate() {
		return mShowDate;
	}

	public void setShowDate(Boolean mShowDate) {
		this.mShowDate = mShowDate;
	}

	public DateTime getDate() {
		return mDate;
	}

	public File getFile() {
		return mFile;
	}

	public void setFile(File mFile) {
		this.mFile = mFile;
	}

	public String getPath() {
		return mPath;
	}

	public void setPath(String mPath) {
		this.mPath = mPath;
	}

	public String getName() {
		return mName;
	}

	public void setName(String mName) {
		this.mName = mName;
	}

	public String getSize() {
		return mSize;
	}

	public void setSize(String mSize) {
		this.mSize = mSize;
	}

	public String getDuration() {
		return mDuration;
	}

	public void setDuration(String mDuration) {
		this.mDuration = mDuration;
	}

	private boolean checkFileNameValidity() {
		String REGEX = "VID_\\d\\d\\d\\d\\d\\d\\d\\d_\\d\\d\\d\\d\\d\\d.*";

		Pattern pattern = Pattern.compile(REGEX);
		Matcher matcher = pattern.matcher(mName);
		if (matcher.find())
			return true;

		return false;
	}

	public static boolean checkFileNameValidity(String name) {
		String REGEX = "VID_\\d\\d\\d\\d\\d\\d\\d\\d_\\d\\d\\d\\d\\d\\d.*";

		Pattern pattern = Pattern.compile(REGEX);
		Matcher matcher = pattern.matcher(name);
		if (matcher.find())
			return true;

		return false;
	}

	public static Comparator<VideoItem> VideoSizeComparator = new Comparator<VideoItem>() {

		public int compare(VideoItem vi1, VideoItem vi2) {

			int size1 = Integer.parseInt(vi1.getSize());
			int size2 = Integer.parseInt(vi2.getSize());

			if (size1 > size2)
				return 1;
			else if (size1 < size2)
				return -1;
			else
				return 0;
		}
	};

	public static Comparator<VideoItem> VideoSizeComparatorDesc = new Comparator<VideoItem>() {

		public int compare(VideoItem vi1, VideoItem vi2) {

			int size1 = Integer.parseInt(vi1.getSize());
			int size2 = Integer.parseInt(vi2.getSize());

			if (size1 < size2)
				return 1;
			else if (size1 > size2)
				return -1;
			else
				return 0;
		}
	};

	public static Comparator<VideoItem> VideoDateComparatorDesc = new Comparator<VideoItem>() {

		public int compare(VideoItem vi1, VideoItem vi2) {

			DateTime d1 = vi1.getDate();
			DateTime d2 = vi2.getDate();

			return d2.compareTo(d1);
		}
	};

}
