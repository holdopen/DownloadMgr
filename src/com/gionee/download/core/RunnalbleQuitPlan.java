package com.gionee.download.core;

import android.annotation.SuppressLint;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

@SuppressLint("UseSparseArrays")
public class RunnalbleQuitPlan extends DelaySyncExecutor {

    private static final int CHECK_SPACE_TIME = 500;
    
    private Map<Integer, DownloadRunnable> mQuittingRunnables;
    private Runtime mRuntime;
    
    public RunnalbleQuitPlan(Runtime runtime) {
        mQuittingRunnables = new HashMap<Integer, DownloadRunnable>();
        this.mRuntime = runtime;
    }

    public void post(int downId, DownloadRunnable runnable) {
        if (mQuittingRunnables.size() == 0) {
            setDelayed(CHECK_SPACE_TIME);
        }
        
        mQuittingRunnables.put(downId, runnable);
    }
    
    @Override
    protected void onExecute() {
        removeQuitted();
        
        if (mQuittingRunnables.size() > 0) {
            setDelayed(CHECK_SPACE_TIME);
        }
    }

    public boolean isQuitting(int downId) {
        if (false == mQuittingRunnables.containsKey(downId)) {
            return false;
        }
        
        if (false == mQuittingRunnables.get(downId).isActive()) {
            remove(downId);
            return false;
        }
        
        return true;
    }

    private void remove(int downId) {
        mQuittingRunnables.remove(downId);
        
        if (mQuittingRunnables.size() == 0) {
            cancelTask();
        }
    }

    private void removeQuitted() {
        boolean isSomeRemove = false;
        Set<Entry<Integer, DownloadRunnable>> quitTask = new HashSet<Map.Entry<Integer, DownloadRunnable>>();
        quitTask.addAll(mQuittingRunnables.entrySet());
        for (Entry<Integer, DownloadRunnable> entry : quitTask) {
            if (false == entry.getValue().isActive()) {
                mQuittingRunnables.remove(entry.getKey());
                isSomeRemove = true;
            }
        }

        if (isSomeRemove) {
            onSomeRemove();
        }
    }

    private void onSomeRemove() {
        mRuntime.checkPrepareRuns();
    }
}
