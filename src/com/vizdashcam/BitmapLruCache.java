package com.vizdashcam;

import android.graphics.Bitmap;
import android.util.LruCache;

public class BitmapLruCache<T> extends LruCache<T, Bitmap> {

	public BitmapLruCache() {
		super(1024 * 1024);
	}
	
}
