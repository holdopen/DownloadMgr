package com.gionee.download.core;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.annotation.SuppressLint;
import android.content.Context;

import com.gionee.download.manager.DownloadMgr;
import com.gionee.download.manager.DownloadRequest;

@SuppressLint("UseSparseArrays")
public class DataHolder implements IOnControllerDestroy {

    private DBFacade mDbFacade;

    private List<DownloadInfo> mUncompletedTasks;
    private List<DownloadInfo> mCompleteTasks;
    private Map<Integer, DownloadInfo> mIdTaskMap;

    private Context mContext;

    private ObserverMgr mObserverMgr;

    public DataHolder(Context context, ObserverMgr observerManager) {
        this.mContext = context;
        this.mObserverMgr = observerManager;

        mUncompletedTasks = new ArrayList<DownloadInfo>();
        mCompleteTasks = new ArrayList<DownloadInfo>();
        mIdTaskMap = new HashMap<Integer, DownloadInfo>();

        mDbFacade = new DBFacade(mContext);

        retrieveTasksExsits();
        repairRunningStatus();
    }

    private void retrieveTasksExsits() {

        List<DownloadInfo> list = mDbFacade.getDownloadInfoList();
        if (null == list || list.size() == 0) {
            return;
        }

        for (int i = 0; i < list.size(); i++) {
            DownloadInfo downloadInfo = list.get(i);
            mIdTaskMap.put(downloadInfo.getDownId(), downloadInfo);
            if (downloadInfo.getStatus() == DownloadMgr.STATUS_SUCCESSFUL) {
                mCompleteTasks.add(downloadInfo);
            } else {
                mUncompletedTasks.add(downloadInfo);
            }
        }
    }

    private void repairRunningStatus() {
        for (int i = 0; i < mUncompletedTasks.size(); i++) {
            DownloadInfo downloadInfo = mUncompletedTasks.get(i);
            if (downloadInfo.getStatus() == DownloadMgr.STATUS_RUNNING) {
                setStatus(downloadInfo, DownloadMgr.STATUS_PENDING, DownloadMgr.REASON_NONE);
            }
        }
    }

    public void batchPendingTasks(int pendingReason) {
        batchSetStatus(DownloadMgr.STATUS_PENDING, pendingReason);
    }

    public void batchPausedTasks() {
        batchSetStatus(DownloadMgr.STATUS_PAUSED, DownloadMgr.REASON_NONE);
    }

    private void batchSetStatus(int status, int reason) {
        for (int i = 0; i < mUncompletedTasks.size(); i++) {
            DownloadInfo info = mUncompletedTasks.get(i);
            if (shouldToSetStatus(info, status)) {
                setStatus(info, status, reason);
            }
        }
    }

    private boolean shouldToSetStatus(DownloadInfo info, int dstStatus) {
        final int status = info.getStatus();
        return status == DownloadMgr.STATUS_PENDING || status == DownloadMgr.STATUS_RUNNING;
    }

    public DownloadInfo createInfo(DownloadRequest request) {
        DownloadInfo info = new DownloadInfo(request);

        updateFilePath(info);

        int id = mDbFacade.addDownloadInfo(info);
        if (id == DownloadInfo.INVALID_DOWN_ID) {
            return null;
        }

        mIdTaskMap.put(info.getDownId(), info);
        mUncompletedTasks.add(info);

        mObserverMgr.postChanged(info, ObserverMgr.TYPE_STATUS);

        return info;
    }

    private void updateFilePath(DownloadInfo info) {
        final String path = info.getLocalPath();
        String newPath = path;
        int index = 1;
        String sign = ""; 
        while (isFilePathExist(newPath)) {
            sign = "(" + index + ")";
            newPath = fitSign(path, sign);
            
            index++;
        }
        
        info.mLocalPath = newPath;
        info.mTitle = fitSign(info.getTitle(), sign);
    }

    public boolean isFilePathExist(String filePath) {
        for (DownloadInfo info : getAllDownloadInfoList()) {
            if (info.getLocalPath().equalsIgnoreCase(filePath)) {
                return true;
            }
        }
        
        if (new File(filePath).exists()) {
            return true;
        }
        
        return false;
    }
    
    private String fitSign(final String path, String sign) {
        String point = ".";
        if (path.contains(point)) {
            int lastIndexOfPoint = path.lastIndexOf(point);
            String pathName = path.substring(0, lastIndexOfPoint);
            String extensionName = path.substring(lastIndexOfPoint);
            return pathName + sign + extensionName;
            
        } else {
            return path + sign;
        }
    }

    public void setStatus(DownloadInfo info, int status, int reason) {
        if (info.getStatus() == status && info.getReason() == reason) {
            return;
        }

        updateTaskList(info, status);

        info.mStatus = status;
        info.mReason = reason;

        postUpdataInfoDB(info);

        mObserverMgr.postChanged(info, ObserverMgr.TYPE_STATUS);
    }

    private void updateTaskList(DownloadInfo info, int newStatus) {
        int oldStatus = info.getStatus();
        if (oldStatus == DownloadMgr.STATUS_SUCCESSFUL) {
            mCompleteTasks.remove(info);
            mUncompletedTasks.add(info);
            info.mSucceedTime = -1;

        } else if (newStatus == DownloadMgr.STATUS_SUCCESSFUL) {
            mUncompletedTasks.remove(info);
            mCompleteTasks.add(info);
            info.mSucceedTime = System.currentTimeMillis();
        }
    }

    public DownloadInfo getUncompletedTask(int downId) {
        DownloadInfo info = mIdTaskMap.get(downId);
        if (null == info) {
            return null;
        }

        if (info.getStatus() == DownloadMgr.STATUS_SUCCESSFUL) {
            return null;
        }

        return info;
    }

    public DownloadInfo getDownloadInfo(int downId) {
        return mIdTaskMap.get(downId);
    }

    public DownloadInfo getDownloadInfo(String url) {
        List<DownloadInfo> list = getAllDownloadInfoList();
        for (DownloadInfo info : list) {
            if (url.trim().equals(info.getUrl().trim())) {
                return info;
            }
        }
        return null;
    }

    public List<DownloadInfo> getPaddingTasks() {
        List<DownloadInfo> paddingTasks = new ArrayList<DownloadInfo>();
        for (int i = 0; i < mUncompletedTasks.size(); i++) {
            if (mUncompletedTasks.get(i).getStatus() == DownloadMgr.STATUS_PENDING) {
                paddingTasks.add(mUncompletedTasks.get(i));
            }
        }
        return paddingTasks;
    }

    public List<DownloadInfo> getCompleteList() {
        List<DownloadInfo> result = new ArrayList<DownloadInfo>();
        result.addAll(mCompleteTasks);
        return result;
    }

    public List<DownloadInfo> getUncompleteList() {
        List<DownloadInfo> result = new ArrayList<DownloadInfo>();
        result.addAll(mUncompletedTasks);
        return result;
    }

    public List<DownloadInfo> getAllDownloadInfoList() {
        List<DownloadInfo> result = new ArrayList<DownloadInfo>();
        result.addAll(mUncompletedTasks);
        result.addAll(mCompleteTasks);
        return result;
    }

    public void reset(DownloadInfo info) {
        info.mProgress = 0;

        postUpdataInfoDB(info);

        mObserverMgr.postChanged(info, ObserverMgr.TYPE_PROGRESS);
    }

    public void onProgress(DownloadInfo info) {
        postUpdataInfoDB(info);
        mObserverMgr.postChanged(info, ObserverMgr.TYPE_PROGRESS);
    }

    public DownloadInfo nextPaddingTask() {
        for (int i = 0; i < mUncompletedTasks.size(); i++) {
            DownloadInfo task = mUncompletedTasks.get(i);
            if (task.getStatus() == DownloadMgr.STATUS_PENDING) {
                return task;
            }
        }
        return null;
    }

    public void setAllowMobileNet(DownloadInfo info, boolean isAllowMobileNet) {
        info.mIsAllowMobileNet = isAllowMobileNet;

        postUpdataInfoDB(info);
    }

    private void postUpdataInfoDB(DownloadInfo info) {
        mDbFacade.postUpdateInfo(info);
    }

    public void delete(DownloadInfo info) {
        removeCache(info);

        deleteInfoDB(info);

        mObserverMgr.postChanged(info, ObserverMgr.TYPE_DELETE);
    }

    private void removeCache(DownloadInfo info) {
        mCompleteTasks.remove(info);
        mUncompletedTasks.remove(info);
        int downId = info.getDownId();
        mIdTaskMap.remove(downId);
    }

    private void deleteInfoDB(DownloadInfo info) {
        mDbFacade.removeUpdateDownloadInfo(info);
        mDbFacade.deleteDownloadInfo(info.getDownId());
    }

    @Override
    public void onDestroy() {
        mDbFacade.onDestroy();
        mObserverMgr.onDestroy();
    }

}
