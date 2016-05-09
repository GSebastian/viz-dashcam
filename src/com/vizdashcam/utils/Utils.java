package com.vizdashcam.utils;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Comparator;
import java.util.Date;
import java.util.Locale;

import org.joda.time.DateTime;

import com.vizdashcam.VideoItem;

import android.os.Environment;

public class Utils {

	public static final String TAG = "Utils";

	public static final int MEDIA_TYPE_IMAGE = 1;
	public static final int MEDIA_TYPE_VIDEO = 2;

	/** Create a File for saving an image or video */
	public static File getOutputMediaFile() {
		File mediaStorageDir = new File(
				Environment.getExternalStorageDirectory(), "vizDashcamApp");

		if (!mediaStorageDir.exists()) {
			if (!mediaStorageDir.mkdirs()) {
				return null;
			}
		}

		String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US)
				.format(new Date());

		File mediaFile = new File(mediaStorageDir.getPath() + File.separator
				+ "VID_" + timeStamp + ".mp4");

		return mediaFile;
	}

	public static Comparator<File> DATE_ORDER = new Comparator<File>() {

		public int compare(File f1, File f2) {

			DateTime date1 = Utils.getDateFromFile(f1);
			DateTime date2 = Utils.getDateFromFile(f2);

			if (date1.isBefore(date2))
				return 1;
			else if (date1.isAfter(date2))
				return -1;
			return 0;

		}
	};

	public static Comparator<File> ALPHABETICAL_ORDER = new Comparator<File>() {

		public int compare(File f1, File f2) {
			String str1 = f1.getName();
			String str2 = f2.getName();
			int res = String.CASE_INSENSITIVE_ORDER.compare(str1, str2);
			if (res == 0) {
				res = str1.compareTo(str2);
			}
			return res;
		}
	};

	public static DateTime getDateFromFile(File f) {
		String name = f.getName();
		if (VideoItem.checkFileNameValidity(name)) {
			int year = Integer.parseInt(name.substring(4, 8));
			int month = Integer.parseInt(name.substring(8, 10));
			int day = Integer.parseInt(name.substring(10, 12));
			int hour = Integer.parseInt(name.substring(13, 15));
			int minutes = Integer.parseInt(name.substring(15, 17));
			int seconds = Integer.parseInt(name.substring(17, 19));

			return new DateTime(year, month, day, hour, minutes, seconds);
		} else {
			return new DateTime(1993, 10, 1, 5, 0, 0);
		}
	}
}
