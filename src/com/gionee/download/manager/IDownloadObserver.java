package com.gionee.download.manager;

import java.util.List;

import com.gionee.download.core.DownloadInfo;

public interface IDownloadObserver {
	
	public void onProgressChange(List<DownloadInfo> changedInfos);

	public void onStatusChange(List<DownloadInfo> changedInfos);

	public void onDelete(List<DownloadInfo> deletedInfos);

}
