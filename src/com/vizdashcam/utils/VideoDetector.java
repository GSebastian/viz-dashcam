package com.vizdashcam.utils;

import java.io.File;
import java.util.HashMap;
import java.util.Locale;
import java.util.regex.Pattern;

import org.apache.commons.io.FilenameUtils;

import android.webkit.MimeTypeMap;

public class VideoDetector {
    private static final HashMap<String, String> MIME_TYPES = new HashMap<String, String>();

    static {
        MIME_TYPES.put("jpgv", "video/jpeg");
        MIME_TYPES.put("jpgm", "video/jpm");
        MIME_TYPES.put("jpm", "video/jpm");
        MIME_TYPES.put("mj2", "video/mj2");

        MIME_TYPES.put("mjp2", "video/mj2");
        MIME_TYPES.put("mpa", "video/mpeg");
        MIME_TYPES.put("ogv", "video/ogg");
        MIME_TYPES.put("flv", "video/x-flv");
        MIME_TYPES.put("mkv", "video/x-matroska");
    }

    public static String getFileType(File file) {
        if (file.isDirectory()) {
            return null;
        }

        String type = null;
        final String extension = FilenameUtils.getExtension(file.getName());

        if (!extension.isEmpty()) {
            final String extensionLowerCase = extension.toLowerCase(Locale.US);
            final MimeTypeMap mime = MimeTypeMap.getSingleton();
            type = mime.getMimeTypeFromExtension(extensionLowerCase);
            if (type == null) {
                type = MIME_TYPES.get(extensionLowerCase);
            }
        }
        return type;
    }

    private static boolean checkTypeMatch(String type, String input) {
        return Pattern.matches(type.replace("*", ".*"), input);
    }

    public static boolean isVideo(File f) {
        final String type = getFileType(f);
        return type != null && checkTypeMatch("video/*", type);
    }
}
