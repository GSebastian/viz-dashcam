package com.vizdashcam;

import android.media.MediaMetadataRetriever;
import android.os.Environment;

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

	private File file;
	private String path;
	private String name;
	private String size;
	private String duration;
	private DateTime date;
	private boolean showDate;
	private boolean isMarked;

	public VideoItem(final File file) {
		this.file = file;
		path = this.file.getAbsolutePath();
		name = this.file.getName();
		setFileSize();
		setVideoDuration();
		setDate();
		showDate = false;
		if (isMarked()) {
			isMarked = true;
		} else {
			isMarked = false;
		}
	}

	public VideoItem(VideoItem video) {
		this.file = video.getFile();
		this.path = this.file.getAbsolutePath();
		this.name = this.file.getName();
		this.size = video.getSize();
		this.duration = video.getDuration();
		this.date = video.getDate();
		this.showDate = false;
		if (isMarked()) {
			isMarked = true;
		} else {
			isMarked = false;
		}
	}

	private void setDate() {
		if (checkFileNameValidity()) {
			int year = Integer.parseInt(name.substring(4, 8));
			int month = Integer.parseInt(name.substring(8, 10));
			int day = Integer.parseInt(name.substring(10, 12));
			int hour = Integer.parseInt(name.substring(13, 15));
			int minutes = Integer.parseInt(name.substring(15, 17));
			int seconds = Integer.parseInt(name.substring(17, 19));

			date = new DateTime(year, month, day, hour, minutes, seconds);
		} else {
			date = new DateTime(1993, 10, 1, 5, 0, 0);
		}
	}

	private void setFileSize() {
		long length = file.length();
		long divBy = 1024 * 1024;

		if (length > divBy) {
			size = String.valueOf(length / divBy);
		} else {
			size = String.format(Locale.US, "%.1f",
					((float) ((float) file.length() / (float) divBy)));
		}
	}

	private void setVideoDuration() {
		try {
			MediaMetadataRetriever retriever = new MediaMetadataRetriever();
			retriever.setDataSource(path);
			String time = retriever
					.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
			long timeInmillisec = Long.parseLong(time);
			long seconds = timeInmillisec / 1000;
			duration = String.valueOf(seconds / 60);
		} catch (RuntimeException e) {
			duration = "-1";
		}
	}

	public boolean isMarked() {
		int lastPointIndex = file.getName().lastIndexOf(".");
		String lastTwoChars = file.getName()
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

	public String setMarked(boolean value) {

		if (value) {
			if (!isMarked()) {
				StringBuilder stringBuilder = new StringBuilder(name);
				int lastPointPosition = name.lastIndexOf(".");
				if (lastPointPosition != -1) {
					stringBuilder.insert(lastPointPosition,
							VideoItem.EXTENSION_MARKED_FILE);
					String markedFileName = stringBuilder.toString();
					File from = new File(mediaStorageDir, name);
					File to = new File(mediaStorageDir, markedFileName);
					name = markedFileName;

					if (from.exists())
						from.renameTo(to);
					file = to;
					path = to.getPath();
					isMarked = true;

					return name;
				}
			}
		} else if (isMarked()) {
			StringBuilder stringBuilder = new StringBuilder(name);
			int lastPointPosition = name.lastIndexOf(".");
			if (lastPointPosition != -1) {
				stringBuilder.delete(lastPointPosition - VideoItem.EXTENSION_MARKED_FILE.length(), lastPointPosition);
				String unmarkedFileName = stringBuilder.toString();
				File from = new File(mediaStorageDir, name);
				File to = new File(mediaStorageDir, unmarkedFileName);
				name = unmarkedFileName;

				if (from.exists())
					from.renameTo(to);
				file = to;
				path = to.getPath();
				isMarked = false;

				return name;
			}
		}

		return name;
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
		return showDate;
	}

	public void setShowDate(Boolean mShowDate) {
		this.showDate = mShowDate;
	}

	public DateTime getDate() {
		return date;
	}

	public File getFile() {
		return file;
	}

	public void setFile(File mFile) {
		this.file = mFile;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String mPath) {
		this.path = mPath;
	}

	public String getName() {
		return name;
	}

	public void setName(String mName) {
		this.name = mName;
	}

	public String getSize() {
		return size;
	}

	public void setSize(String mSize) {
		this.size = mSize;
	}

	public String getDuration() {
		return duration;
	}

	public void setDuration(String mDuration) {
		this.duration = mDuration;
	}

	private boolean checkFileNameValidity() {
		String REGEX = "VID_\\d\\d\\d\\d\\d\\d\\d\\d_\\d\\d\\d\\d\\d\\d.*";

		Pattern pattern = Pattern.compile(REGEX);
		Matcher matcher = pattern.matcher(name);
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
