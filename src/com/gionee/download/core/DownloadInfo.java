package com.gionee.download.core;

import org.json.JSONObject;

import com.gionee.download.core.msg.IMsgData;
import com.gionee.download.manager.DownloadMgr;
import com.gionee.download.manager.DownloadRequest;

public final class DownloadInfo implements IMsgData {

    private static final String TEMP_FILE_SUFFIX = ".TEMP";

    public static final int INVALID_DOWN_ID = -1;

    protected int mDownId = INVALID_DOWN_ID;
    protected int mTotal = -1;
    protected int mProgress = 0;
    protected int mStatus = DownloadMgr.STATUS_PENDING;
    protected int mReason = DownloadMgr.REASON_NONE;
    protected boolean mIsAllowMobileNet = false;

    protected String mLocalPath = "";
    protected String mTitle = "";
    protected String mUrl = "";
    protected long mSucceedTime = 0;
    protected JSONObject mExtraInfo;

    protected int mSpeedBPerS;

    DownloadInfo() {
    }

    public DownloadInfo(DownloadRequest request) {
        this.mUrl = request.getUrl();
        this.mTitle = request.getTitle();
        this.mIsAllowMobileNet = request.isAllowByMobileNet();
        this.mLocalPath = request.getLocalPath().trim();
        this.mExtraInfo = request.getExtraInfo();
    }

    public int getDownId() {
        return mDownId;
    }

    public int getTotal() {
        return mTotal;
    }

    public int getProgress() {
        return mProgress;
    }

    public int getStatus() {
        return mStatus;
    }

    public int getReason() {
        return mReason;
    }

    public boolean isAllowMobileNet() {
        return mIsAllowMobileNet;
    }

    public String getLocalPath() {
        return mLocalPath;
    }

    public String getLocalTempFilePath() {
        return mLocalPath + TEMP_FILE_SUFFIX;
    }

    public String getTitle() {
        return mTitle;
    }

    public String getUrl() {
        return mUrl;
    }

    public long getSucceedTime() {
        return mSucceedTime;
    }

    public String getStringExtra(String name) {
        if (null == mExtraInfo) {
            return null;
        }

        return mExtraInfo.optString(name);
    }

    public int getSpeedBPerS() {
        return mSpeedBPerS;
    }

    @Override
    public String toString() {
        return "DownloadInfo [mDownId=" + mDownId + ", mTotal=" + mTotal + ", mProgress=" + mProgress
                + ", mStatus=" + mStatus + ", mReason=" + mReason + ", mIsAllowMobileNet="
                + mIsAllowMobileNet + ", mLocalPath=" + mLocalPath + ", mTitle=" + mTitle + ", mUrl=" + mUrl
                + ", mSucceedTime=" + mSucceedTime + "]";
    }
    
}
