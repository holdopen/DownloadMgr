package com.gionee.download.core;

import com.gionee.download.utils.LogUtils;

import android.os.SystemClock;

public class DownloadRetryRecord {

    private static final int RETRY_SPAC_TIME = 5000;
    private static final int MAX_RETRY_COUNT = 6;
    private static final int  RETRY_COUNT_VALID_TIME= 10000;
    private static final String TAG = "DownloadRetryRecord";
    
    private long lastRetryTime = -1;
    private int retryCount = 0;
    
    public synchronized boolean hasRetryTime() {
        checkRetryCount();
        if (retryCount >= MAX_RETRY_COUNT) {
            return false;
        } 
        LogUtils.logd(TAG, "hasRetryTime   wait retry  retryCount= " + retryCount );
        try {
            wait(RETRY_SPAC_TIME);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        LogUtils.logd(TAG, "hasRetryTime   do retry  ");
        retryCount ++;
        lastRetryTime = SystemClock.elapsedRealtime();
        return true;
    }
    
    public void checkRetryCount() {
        if (lastRetryTime < 0) {
            return;
        }
        
        if (SystemClock.elapsedRealtime() - lastRetryTime > RETRY_COUNT_VALID_TIME) {
            retryCount = 0;
            LogUtils.logd(TAG, "checkRetryCount  reset retryCount");
        }
    }
}
