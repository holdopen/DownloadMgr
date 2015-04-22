package com.gionee.download.utils;

import java.io.File;

import android.os.Environment;

public class StorageUtils {
    public static final String GAME_FOLDER = "youxi";
    private static final String SDCARD_DIR = Environment.getExternalStorageDirectory().getAbsolutePath();
    
    private static String sAPKDownloadDir = null;
    
    public static boolean hasAPKDownloadDir() {
        return sAPKDownloadDir != null;
    }

    public static String getAPKDownloadDir() {
        return hasAPKDownloadDir() ? sAPKDownloadDir : getHomeDirAbsolute();
    }
    
    public static String getAPKDownloadCard() {
        String apkDir = getAPKDownloadDir();
        if (apkDir.contains(GAME_FOLDER)) {
            int end = apkDir.indexOf(GAME_FOLDER) - 1;
            return apkDir.substring(0, end);
        }
        return null;
    }

    public static void setAPKDownloadDir(String downloadDir) {
        StorageUtils.sAPKDownloadDir = downloadDir;
    }
    
    public static boolean isMutilCacheDir() {
        String homeDirAbsolute = getHomeDirAbsolute();
        if (homeDirAbsolute == null) {
            return true;
        }
        return !homeDirAbsolute.equals(sAPKDownloadDir);
    }
    
    public static void resetAPKDownloadDir() {
        sAPKDownloadDir = null;
    }
    
    public static boolean isSDCardMounted() {
        return Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState());
    }

    public static String getSDCardDir() {
        if (isSDCardMounted()) {
            return SDCARD_DIR;
        }
        return null;
    }

    public static String getHomeDir() {
        if (isSDCardMounted()) {
            File file = new File(SDCARD_DIR + File.separator + GAME_FOLDER);
            if (!file.exists()) {
                if (file.mkdirs()) {
                    return GAME_FOLDER;
                }
                return null;
            }
            return GAME_FOLDER;
        }
        return null;
    }

    public static String getHomeDirAbsolute() {
        if (getHomeDir() != null) {
            return getSDCardDir() + File.separator + GAME_FOLDER;
        }
        return null;
    }

    public static void deleteFileDir(File fileDir, boolean isNeedDeleteHideFile) {
        if (!fileDir.exists() || fileDir.isFile()) {
            return;
        }
        recursionDeleteFile(fileDir, isNeedDeleteHideFile);
    }

    private static void recursionDeleteFile(File fileDir, boolean isNeedDeleteHideFile) {
        File[] childFiles = fileDir.listFiles();
        for (File file : childFiles) {
            if (file.isDirectory()) {
                recursionDeleteFile(file, isNeedDeleteHideFile);
            } else {
                if (isNeedDeleteHideFile || !file.isHidden()) {
                    file.delete();
                }
            }
        }
    }

}
