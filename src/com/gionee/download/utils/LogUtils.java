package com.gionee.download.utils;

import java.io.File;
import java.io.RandomAccessFile;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

public class LogUtils {

	private static final String GLOBAL_TAG = "dm_v1.1.0";
	private static final String SAVELOG_FILE_NAME = "gamelog.txt";
	private static final String ENABLE_SAVELOG_FLAG_FOLDER = "GameHallSaveLog";
	private static final String TAG = "LogUtils";
	public static boolean sEnableLog = true;
	private static boolean sIsSaveLog = false;

	@SuppressLint("SimpleDateFormat")
    private static SimpleDateFormat sFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSSS");

	public static void showToast(Context context, String str) {
		if (sEnableLog) {
			Toast.makeText(context, str, Toast.LENGTH_SHORT).show();
			
		}
	}

	public static void loadInitConfigs() {
		Log.d(TAG, "loadInitConfigs ...");
		if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
			String sdCardDir = getExternalStorageDirectory();
			File saveLogFlagFile = new File(sdCardDir + ENABLE_SAVELOG_FLAG_FOLDER);
			if (saveLogFlagFile.exists()) {
				Log.d(TAG, "DownloadManager savelog flag is true");
				LogUtils.sIsSaveLog = true;
			}
		}

	}

	public static void logi(String tag, String msg) {
		if (sEnableLog) {
			Log.i(GLOBAL_TAG + "." + tag, "" + msg);
			if (sIsSaveLog) {
				try {
					saveToSDCard(formatLog(msg, tag, "i"));
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}

	public static void logv(String tag, String msg) {
		if (sEnableLog) {
			Log.v(GLOBAL_TAG + "." + tag, "" + msg);
			if (sIsSaveLog) {
				try {
					saveToSDCard(formatLog(msg, tag, "V"));
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}

	public static void logd(String tag, String msg) {
		if (sEnableLog) {
			Log.d(GLOBAL_TAG + "." + tag, msg);
			if (sIsSaveLog) {
				try {
					saveToSDCard(formatLog(msg, tag, "D"));
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	public static void logw(String tag, String msg) {
		if (sEnableLog) {
			Log.w(GLOBAL_TAG + "." + tag, msg);
			if (sIsSaveLog) {
				try {
					saveToSDCard(formatLog(msg, tag, "W"));
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	public static void logw(String tag, String msg, Throwable tr) {
        if (sEnableLog) {
            Log.w(GLOBAL_TAG + "." + tag, msg, tr);
            if (sIsSaveLog) {
                try {
                    saveToSDCard(formatLog(msg, tag, "W"));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

	public static void loge(String tag, String msg) {
		if (sEnableLog) {
			Log.e(GLOBAL_TAG + "." + tag + ".E", "" + msg);
			if (sIsSaveLog) {
				try {
					saveToSDCard(formatLog(msg, tag, "E"));
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}

	public static void loge(String tag, String msg, Throwable e) {
		if (sEnableLog) {
			Log.e(GLOBAL_TAG + "." + tag, "" + msg, e);
			if (sIsSaveLog) {
				try {
					saveToSDCard(formatLog(msg, tag, "E"));
				} catch (Exception e1) {
					e1.printStackTrace();
				}
			}
		}
	}

	public static void saveToSDCard(String content) throws Exception {
		if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
			try {
				String sdCardDir = getExternalStorageDirectory();
				File file = new File(sdCardDir + File.separator + ENABLE_SAVELOG_FLAG_FOLDER,
						SAVELOG_FILE_NAME);
				RandomAccessFile raf = new RandomAccessFile(file, "rw");
				raf.seek(file.length());
				raf.write(content.getBytes());
				raf.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public static String getFunctionName() {
		StringBuffer sb = new StringBuffer();
		sb.append("-> ");
		sb.append(Thread.currentThread().getStackTrace()[3].getMethodName());
		sb.append("()");
		sb.append("-> ");
		return sb.toString();
	}

	public static String getThreadName() {
		StringBuffer sb = new StringBuffer();
		try {
			sb.append(Thread.currentThread().getName());
			sb.append("-> ");
			sb.append(Thread.currentThread().getStackTrace()[3].getMethodName());
			sb.append("()");
			sb.append(" ");
		} catch (Exception e) {
			LogUtils.loge(TAG, e.getMessage());
		}
		return sb.toString();
	}

	public static void printStack() {
		if (sEnableLog) {
			try {
				throw new Exception("printStack");
			} catch (Exception e) {
				Log.e("TAG", e.getLocalizedMessage(), e);
			}
		}
	}

	private static String formatLog(String log, String type, String level) {
		StringBuilder builder = new StringBuilder();
		builder.append("[");
		synchronized (sFormatter) {
			builder.append(sFormatter.format(Calendar.getInstance().getTime()));
		}
		builder.append("][");
		builder.append(type);
		builder.append("][");
		builder.append(level);
		builder.append("]");
		builder.append(log);
		builder.append("\n");
		return builder.toString();
	}

	private static String getExternalStorageDirectory() {
		String rootpath = Environment.getExternalStorageDirectory().getPath();
		if (!rootpath.endsWith(File.separator)) {
			rootpath += File.separator;
		}
		Log.d(TAG, "getExternalStorageDirectory() path = " + rootpath);
		return rootpath;
	}

}
