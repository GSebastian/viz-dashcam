package com.vizdashcam;

import android.graphics.Bitmap;
import android.media.ThumbnailUtils;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.widget.ImageView;

import java.io.File;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class VideoPreview {

    private static BitmapLruCache<String> bitmapCache;
    private static ExecutorService pool = null;
    private static Map<ImageView, String> imageViews = Collections
            .synchronizedMap(new ConcurrentHashMap<ImageView, String>());

    public VideoPreview() {
        pool = Executors.newFixedThreadPool(3);
        bitmapCache = new BitmapLruCache<String>();
    }

    public static void getFileIcon(File file, final ImageView icon) {
        loadBitmap(file, icon);
    }

    private static void queueJob(final File uri, final ImageView imageView) {
        final Handler handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                String tag = imageViews.get(imageView);
                if (tag != null && tag.equals(uri.getAbsolutePath())) {
                    if (msg.obj != null) {
                        imageView.setImageBitmap((Bitmap) msg.obj);
                    } else {
                        imageView.setImageBitmap(null);
                    }
                }
            }
        };

        pool.submit(new Runnable() {
            public void run() {
                final Bitmap bmp = getPreview(uri);
                Message message = Message.obtain();
                message.obj = bmp;

                handler.sendMessage(message);
            }
        });
    }

    private static void loadBitmap(final File file, final ImageView imageView) {
        imageViews.put(imageView, file.getAbsolutePath());
        Bitmap cachedIcon = bitmapCache.get(file.getAbsolutePath());

        if (cachedIcon != null) {
            imageView.setImageBitmap(cachedIcon);
        } else {
            imageView.setImageBitmap(null);
            queueJob(file, imageView);
        }
    }

    private static Bitmap getPreview(File file) {
        Bitmap mBitmap = null;
        String path = file.getAbsolutePath();

        mBitmap = ThumbnailUtils.createVideoThumbnail(path,
                MediaStore.Video.Thumbnails.MINI_KIND);

        bitmapCache.put(path, mBitmap);
        return mBitmap;
    }

    public static void clearCache() {
        bitmapCache.evictAll();
    }
}
