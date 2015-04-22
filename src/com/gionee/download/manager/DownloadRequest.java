package com.gionee.download.manager;

import org.json.JSONException;
import org.json.JSONObject;

import com.gionee.download.core.DownloadInfo;
import com.gionee.download.core.msg.IMsgData;

public final class DownloadRequest implements IMsgData {
	private String mTitle;
	private String mUrl;
	private String mLocalPath;
	private boolean mIsAllowByMobileNet;
	private JSONObject mExtraInfo;
	
	private ResultCallback mResultCallback;

	public DownloadRequest(String url, String title, String localPath) {
		this.mUrl = url;
		this.mTitle = title;
		this.mLocalPath = localPath;
	}

	public String getTitle() {
		return mTitle;
	}

	public void setTitle(String title) {
		this.mTitle = title;
	}

	public String getUrl() {
		return mUrl;
	}

	public void setUrl(String url) {
		this.mUrl = url;
	}

	public boolean isAllowByMobileNet() {
		return mIsAllowByMobileNet;
	}

	public void setAllowByMobileNet(boolean isAllowByMobileNet) {
		this.mIsAllowByMobileNet = isAllowByMobileNet;
	}

	public String getLocalPath() {
		return mLocalPath;
	}

	public void setLocalPath(String localPath) {
		this.mLocalPath = localPath;
	}

	public JSONObject getExtraInfo() {
		return mExtraInfo;
	}
	
	public void putExtra(String name, String value) {
		if (null == mExtraInfo) {
			mExtraInfo = new JSONObject();
		}
		try {
			mExtraInfo.put(name, value);
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
	
	public ResultCallback getResultCallback() {
        return mResultCallback;
    }

    public void setRequestCallback(ResultCallback resultCallback) {
        this.mResultCallback = resultCallback;
    }

    public static interface ResultCallback {
	    
	    void onResult(boolean isExists, DownloadInfo info);
	}
}
