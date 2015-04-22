package com.gionee.download.core;

import android.os.Handler;

public abstract class DelaySyncExecutor {

    private Handler mHandler;
    private boolean mIsInDelay;
    
    public DelaySyncExecutor() {
        mHandler = new Handler();
    }
    
    public void setDelayed(int delayMillis) {
        cancelTask();
        
        mIsInDelay = true;
        
        mHandler.postDelayed(mDelayedTask, delayMillis);
    }

    private Runnable mDelayedTask = new Runnable() {

        @Override
        public void run() {
            mIsInDelay = false;
            
            onExecute();
        }
    };
    
    public final void cancelTask() {
        mIsInDelay = false;
        
        mHandler.removeCallbacks(mDelayedTask);
    }
    
    public final void cancelDelay() {
        if (false == isInDelay()) {
            return;
        }
        
        cancelTask();
        
        onExecute();
    }
    
    public boolean isInDelay() {
        return mIsInDelay;
    }

    protected abstract void onExecute();
}
