package com.gionee.download.core;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

import android.annotation.SuppressLint;

import com.gionee.download.manager.DownloadMgr.Setting;
import com.gionee.download.utils.LogUtils;

@SuppressLint("UseSparseArrays")
public class Runtime implements IOnControllerDestroy {
    
    private static final String TAG = "Runtime";
    
    private ThreadPoolExecutor mThreadPool;
    private Map<Integer, DownloadRunnable> mRunning;
    
    private RunnalbleQuitPlan mRunnalbleQuitPlan;

    private Map<Integer, InfoWriteProxy> mPrepareRuns;

    public Runtime() {
        mThreadPool = (ThreadPoolExecutor) Executors.newCachedThreadPool();
        mRunning = new HashMap<Integer, DownloadRunnable>();
        
        mPrepareRuns = new HashMap<Integer, InfoWriteProxy>();
        mRunnalbleQuitPlan = new RunnalbleQuitPlan(this);
    }

    @Override
    public void onDestroy() {
    	stopAllTasks();
    }

    public boolean isCanStartTask(int downId) {
        return (false == isTaskRunning(downId)) && (false == isMaxRunning());
    }

    public boolean isTaskRunning(int downId) {
        return mRunning.containsKey(downId) || mPrepareRuns.containsKey(downId);
    }
    
    public boolean isNotInRunningMap(int downId) {
    	return false == mRunning.containsKey(downId);
    }

    public boolean isMaxRunning() {
        return runningCount() >= Setting.getMaxDownloadTask();
    }

    public int runningCount() {
        return mRunning.size() + mPrepareRuns.size();
    }

    public void notifyRunTask(InfoWriteProxy infoWriteProxy) {
        LogUtils.logi(TAG, "notifyRunTask  info.id=" + infoWriteProxy.getInfo().getDownId());
        
        int downId = infoWriteProxy.getInfo().getDownId();
        if (isQuitting(downId)) {
            mPrepareRuns.put(downId, infoWriteProxy);
        } else {
            runTask(infoWriteProxy);
        }
    }

    private boolean isQuitting(int downId) {
        return mRunnalbleQuitPlan.isQuitting(downId);
    }

    private void runTask(InfoWriteProxy infoWriteProxy) {
        LogUtils.logi(TAG, "runTask  info.id=" + infoWriteProxy.getInfo().getDownId());
        
        final int downId = infoWriteProxy.getInfo().getDownId();

        DownloadRunnable runnable = new DownloadRunnable(infoWriteProxy);

        mRunning.put(downId, runnable);
        mThreadPool.execute(runnable);
    }

    public void stopAllTasks() {
        mPrepareRuns.clear();
        
        if (mRunning.size() == 0) {
            return;
        }
        
        Set<Entry<Integer, DownloadRunnable>> running = mRunning.entrySet();
        for (Entry<Integer, DownloadRunnable> entry : running) {
            int downId = entry.getKey();
            DownloadRunnable runnable = entry.getValue();
            runnable.stop();
            
            mRunnalbleQuitPlan.post(downId, runnable);
        }
        mRunning.clear();
    }
    
    public void stopTask(int downId) {
        mPrepareRuns.remove(downId);

        if (mRunning.containsKey(downId)) {
            DownloadRunnable runnable = mRunning.remove(downId);
            runnable.stop();
            
            mRunnalbleQuitPlan.post(downId, runnable);
        }
    }

    public void onDownloadOver(DownloadInfo info) {
        int downId = info.getDownId();
        DownloadRunnable runnable = mRunning.get(downId);
        mRunning.remove(downId);

        if (null != runnable && runnable.isActive()) {
            mRunnalbleQuitPlan.post(downId, runnable);
        }
    }

    protected void checkPrepareRuns() {
        if (mPrepareRuns.size() == 0) {
            return;
        }
        
        Set<Entry<Integer, InfoWriteProxy>> waitRuns = new HashSet<Map.Entry<Integer,InfoWriteProxy>>();
        waitRuns.addAll(mPrepareRuns.entrySet());
        for (Entry<Integer, InfoWriteProxy> entry : waitRuns) {
            InfoWriteProxy infoProxy = entry.getValue();
            int downId = infoProxy.getInfo().getDownId();
            if (false == isQuitting(downId)) {
                mPrepareRuns.remove(downId);
                runTask(infoProxy);
            }
        }
    }

}
