package com.gionee.download.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.StatFs;

public class Utils {
	private static final String TAG = "Utils";
	// public static final int DELTA_DRAW_MS = 30;
	public static final int KB = 1024;
	public static final int MB = KB * KB;

	public static final int NETWORK_NO_NET = -1;

	public static final int SD_NOT_MOUNTED = -1;
	public static final int SD_LOW_SPACE = 0;
	public static final int SD_ENOUGH_ROOM = 1;

	private static final int MIN_SPACE_REQUIRED = 5 * MB;

	@SuppressWarnings("deprecation")
    public static int checkSDCard(String fileSize) { // fileSize:MB
		if (!StorageUtils.isSDCardMounted()) {
			return SD_NOT_MOUNTED;
		}
		float size = calculateFileSizeMb(fileSize);
		StatFs stat = new StatFs(StorageUtils.getAPKDownloadCard());
		if ((double) stat.getAvailableBlocks() * (double) stat.getBlockSize() > MIN_SPACE_REQUIRED
				+ (int) size * MB) {
			return SD_ENOUGH_ROOM;
		}
		return SD_LOW_SPACE;
	}

	@SuppressWarnings("deprecation")
    public static int checkSDCard(long size) {
		if (!StorageUtils.isSDCardMounted()) {
			return SD_NOT_MOUNTED;
		}
		StatFs stat = new StatFs(StorageUtils.getAPKDownloadCard());
		if ((double) stat.getAvailableBlocks() * (double) stat.getBlockSize() > MIN_SPACE_REQUIRED
				+ (int) size) {
			return SD_ENOUGH_ROOM;
		}
		return SD_LOW_SPACE;
	}

	public static float calculateFileSizeMb(String fileSize) {
		float size = 0;
		Matcher matcher = Pattern.compile("[M*B*]").matcher(fileSize);
		if (matcher.find()) {
			fileSize = fileSize.substring(0, matcher.start());
		}
		try {
			size = Float.parseFloat(fileSize);
		} catch (NumberFormatException e) {
			LogUtils.logi(TAG, LogUtils.getFunctionName() + e.getMessage());
			size = 0;
		}
		return size;
	}

	private static NetworkInfo getActiveNetworkInfo(Context context) {
		ConnectivityManager connectivity = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		return connectivity.getActiveNetworkInfo();
	}

	public static boolean hasNetwork(Context context) {
		return getActiveNetworkInfo(context) != null;
	}

	public static int getNetworkType(Context context) {
		NetworkInfo networkInfo = getActiveNetworkInfo(context);
		if (networkInfo == null) {
			return NETWORK_NO_NET;
		}
		return networkInfo.getType();
	}

	public static boolean isWifiNet(Context context) {
		return getNetworkType(context) == ConnectivityManager.TYPE_WIFI;
	}

	public static boolean isMobileNet(Context context) {
		switch (getNetworkType(context)) {
		case ConnectivityManager.TYPE_MOBILE:
		case ConnectivityManager.TYPE_MOBILE_DUN:
		case ConnectivityManager.TYPE_MOBILE_HIPRI:
		case ConnectivityManager.TYPE_MOBILE_MMS:
		case ConnectivityManager.TYPE_MOBILE_SUPL:
			return true;

		default:
			return false;
		}
	}
}
