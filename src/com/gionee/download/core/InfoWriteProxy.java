package com.gionee.download.core;

import android.os.SystemClock;

public class InfoWriteProxy {

    private DownloadInfo mInfo;
    private MsgSender mMsgSender;

    private boolean mIsCloseWrite = false;
    
    private long mSpeedStartTime = -1;
    private long mProgressInTime = 0;

    public InfoWriteProxy(DownloadInfo mInfo, MsgSender mMsgSender) {
        super();
        this.mInfo = mInfo;
        this.mMsgSender = mMsgSender;
    }

    public DownloadInfo getInfo() {
        return mInfo;
    }

    public void addProgress(int downSize) {
        countSpeed(downSize);
        
        mInfo.mProgress += downSize;
        sendMsg(MsgSender.MSG_RUN_PROGRESS);
    }

    private void countSpeed(int downSize) {
        long curTime = SystemClock.elapsedRealtime();
        if (-1 == mSpeedStartTime) {
            mSpeedStartTime = curTime;
            mProgressInTime = 0;
        } if (curTime - mSpeedStartTime > 1000) {
            mProgressInTime += downSize;
            mInfo.mSpeedBPerS = (int) (((double)mProgressInTime) * 1000 / (curTime - mSpeedStartTime));
            
            mSpeedStartTime = -1;
        } else {
            mProgressInTime += downSize;
        }
    }
    
    public void setTotal(int total) {
        mInfo.mTotal = total;
        sendMsg(MsgSender.MSG_RUN_TOTAL);
    }

    public void setDownloadSuccee() {
        sendMsg(MsgSender.MSG_RUN_SUCCEED);
    }
    
    public void setStautsPendingNet() {
        sendMsg(MsgSender.MSG_RUN_PENDING_NET);
    }
    
    public void setStautsPendingSD() {
        sendMsg(MsgSender.MSG_RUN_PENDING_SD);
    }
    
    public void setDownloadFial(DownloadException exception) {
        if (mIsCloseWrite) {
            return;
        }

        exception.setTargetInfo(mInfo);
        mMsgSender.send(MsgSender.MSG_RUN_FAIL, exception);
    }
    
    public void close() {
        resetSpeed();
        
        mIsCloseWrite = true;
        mMsgSender = null;
    }

    private void resetSpeed() {
        mInfo.mSpeedBPerS = 0;
        sendMsg(MsgSender.MSG_RUN_PROGRESS);
    }
    
    private void sendMsg(int what) {
        if (mIsCloseWrite) {
            return;
        }
        
        mMsgSender.send(what, mInfo);
    }

}
