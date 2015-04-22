package com.gionee.download.core;

import java.io.File;
import java.util.List;

import android.content.Context;
import android.os.Looper;

import com.gionee.download.core.msg.DeleteMsgData;
import com.gionee.download.core.msg.IMsgData;
import com.gionee.download.manager.DownloadMgr;
import com.gionee.download.manager.DownloadRequest;
import com.gionee.download.manager.DownloadRequest.ResultCallback;
import com.gionee.download.utils.LogUtils;

public class MainController {

    private static final String TAG = "MainControler";

    private static Context mContext;

    private MsgHandler mMsgHandler;
    private MsgSender mMsgSender;

    private ObserverMgr mObserverManager;

    private DataHolder mDataHolder;

    private Runtime mRuntime;
    private SystemStateHolder mSystemStatus;

    public MainController(Context context) {
        mContext = context.getApplicationContext();
        mObserverManager = new ObserverMgr();

        initMsgThread();
    }

    private void initMsgThread() {
        new Thread() {
            public void run() {
                Looper.prepare();

                initMsg();
                initData();
                initSystemStatus();

                Looper.loop();
            }
        }.start();
    }

    private void initMsg() {
        mMsgHandler = new MsgHandler(MainController.this);
        mMsgSender = new MsgSender(mMsgHandler);
    }

    private void initData() {
        mDataHolder = new DataHolder(mContext, mObserverManager);

        mRuntime = new Runtime();
    }

    private void initSystemStatus() {
        mSystemStatus = new SystemStateHolder(mContext, mMsgSender);
    }

    public void sendUserMsg(int what, IMsgData msgData) {
        mMsgSender.send(what, msgData);
    }

    public ObserverMgr getObserverManager() {
        return mObserverManager;
    }

    public void quit() {
        mRuntime.onDestroy();
        mMsgHandler.onDestroy();
    }

    public static Context getContext() {
        return mContext;
    }

    protected void enqueue(DownloadRequest request) {
        DownloadInfo existsInfo = mDataHolder.getDownloadInfo(request.getUrl());
        if (null != existsInfo) {
            onRequestRsult(true, request, existsInfo);

            return;
        }

        DownloadInfo newInfo = mDataHolder.createInfo(request);

        tryStartTask(newInfo);
        
        onRequestRsult(false, request, newInfo);
    }

    private void onRequestRsult(boolean isExists, DownloadRequest request,
            DownloadInfo info) {
        ResultCallback resultCallback = request.getResultCallback();
        if (null != resultCallback) {
            resultCallback.onResult(isExists, info);
        }
    }

    private void tryStartTask(DownloadInfo info) {
        LogUtils.logi(TAG, "tryStartTask  info.id=" + info.getDownId());
        
        if (false == mSystemStatus.isReady(info.isAllowMobileNet())) {
            setTaskPendingBySystem(info);
            return;
        }

        if (false == mRuntime.isCanStartTask(info.getDownId())) {
            setTaskPendingByRunTime(info);
            return;
        }

        startTask(info);
    }

    private void setTaskPendingBySystem(DownloadInfo task) {
        int reason = DownloadMgr.REASON_NONE;

        if (false == mSystemStatus.isSDCardMounted()) {
            reason = DownloadMgr.PENDING_DEVICE_NOT_FOUND;

        } else if (false == mSystemStatus.isNetReady(task.isAllowMobileNet())) {
            reason = DownloadMgr.PENDING_WAITING_FOR_NETWORK;
        }

        setTaskPending(task, reason);
    }

    private void setTaskPendingByRunTime(DownloadInfo task) {
        if (mRuntime.isTaskRunning(task.getDownId())) {
            return;
        }

        int reason = DownloadMgr.REASON_NONE;
        if (mRuntime.isMaxRunning()) {
            reason = DownloadMgr.PENDING_MAX_TASK_RUNNING;
        }

        setTaskPending(task, reason);
    }

    private void setTaskPending(DownloadInfo task, int reason) {
        mDataHolder.setStatus(task, DownloadMgr.STATUS_PENDING, reason);
    }

    private void startTask(DownloadInfo info) {
        LogUtils.logi(TAG, "startTask  info.id=" + info.getDownId());
        
        mDataHolder.setStatus(info, DownloadMgr.STATUS_RUNNING,
                DownloadMgr.RUNNING_DOWNLOADING);

        if (false == taskMatchingFile(info)) {
            reset(info);
        }

        mRuntime.notifyRunTask(new InfoWriteProxy(info, mMsgSender));
    }

    private void reset(DownloadInfo task) {
        mDataHolder.reset(task);

        new File(task.getLocalTempFilePath()).delete();
        new File(task.getLocalPath()).delete();
    }

    private boolean taskMatchingFile(DownloadInfo task) {
        boolean isDownloaded = task.getProgress() > 0;
        boolean isTempFileExists = new File(task.getLocalTempFilePath())
                .exists();

        return isDownloaded == isTempFileExists;
    }

    protected void onProgress(DownloadInfo info) {
        mDataHolder.onProgress(info);
    }

    protected void onDownloadSucceed(DownloadInfo info) {
        mDataHolder.setStatus(info, DownloadMgr.STATUS_SUCCESSFUL, DownloadMgr.REASON_NONE);
        mRuntime.onDownloadOver(info);

        tryStartNextPaddingTask();
    }

    public void pendingTask(DownloadInfo info, int reason) {
        mDataHolder.setStatus(info, DownloadMgr.STATUS_PENDING, reason);
        mRuntime.onDownloadOver(info);
    }

    protected void onDownloadFail(DownloadInfo info, int reason) {
        if (mRuntime.isNotInRunningMap(info.getDownId())) {
			return;
		}
        
        mDataHolder.setStatus(info, DownloadMgr.STATUS_FAILED, reason);
        mRuntime.onDownloadOver(info);

        tryStartNextPaddingTask();
    }
    
    private void tryStartNextPaddingTask() {
        DownloadInfo paddingTask = mDataHolder.nextPaddingTask();
        if (paddingTask != null) {
            tryStartTask(paddingTask);
        }
    }

    protected void pause(int downId) {
        DownloadInfo info = mDataHolder.getUncompletedTask(downId);
        if (null == info) {
            return;
        }

        mDataHolder.setStatus(info, DownloadMgr.STATUS_PAUSED,
                DownloadMgr.REASON_NONE);
        mRuntime.stopTask(downId);

        tryStartNextPaddingTask();
    }

    protected void start(int downId) {
        DownloadInfo info = mDataHolder.getUncompletedTask(downId);
        if (null == info) {
            return;
        }

        tryStartTask(info);
    }

    protected void reDownload(int downId) {
        DownloadInfo info =  mDataHolder.getDownloadInfo(downId);
        
        mRuntime.stopTask(downId);
        
        reset(info);
        
        tryStartTask(info);
    }

    protected void hangTasks(int pendingReason) {
        mDataHolder.batchPendingTasks(pendingReason);
        mRuntime.stopAllTasks();
    }
    
    protected void tryResumeTasks() {
        if (mRuntime.isMaxRunning()) {
            return;
        }

        List<DownloadInfo> paddingTasks = mDataHolder.getPaddingTasks();
        for (int i = 0; i < paddingTasks.size(); i++) {
            tryStartTask(paddingTasks.get(i));
        }
    }

    public void delete(DeleteMsgData deleteData) {
        int downId = deleteData.getDownId();
        DownloadInfo info = mDataHolder.getDownloadInfo(downId);
        if (null == info) {
            return;
        }

        mRuntime.stopTask(downId);
        mDataHolder.delete(info);
        
        if (deleteData.needDeleteFile()) {
            new File(info.getLocalTempFilePath()).delete();
            new File(info.getLocalPath()).delete();
        }
    }

    public DataHolder getDataHolder() {
        return mDataHolder;
    }

}
