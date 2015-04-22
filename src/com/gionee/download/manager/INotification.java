package com.gionee.download.manager;

import android.app.Notification;

import com.gionee.download.core.DownloadInfo;

public interface INotification {
	
	public Notification getNotification(DownloadInfo info, int aliveCount);
	
}
