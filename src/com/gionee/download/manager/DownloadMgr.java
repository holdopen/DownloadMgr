package com.gionee.download.manager;

import java.util.List;

import android.content.Context;
import android.widget.ScrollView;

import com.gionee.download.core.DownloadInfo;
import com.gionee.download.core.MainController;
import com.gionee.download.core.MsgSender;
import com.gionee.download.core.msg.DeleteMsgData;
import com.gionee.download.core.msg.IdMsgData;

public final class DownloadMgr {

    public static final int STATUS_PENDING = 1 << 0;
    public static final int STATUS_RUNNING = 1 << 1;
    public static final int STATUS_PAUSED = 1 << 2;
    public static final int STATUS_SUCCESSFUL = 1 << 3;
    public static final int STATUS_FAILED = 1 << 4;

    public static final int REASON_NONE = -1;

//    public static final int RUNNING_START_CONNECTION = 100;
    public static final int RUNNING_DOWNLOADING = 101;

    public static final int ERROR_UNKNOWN = 1000;
    public static final int ERROR_UNHANDLED_HTTP_CODE = ERROR_UNKNOWN + 1;
    public static final int ERROR_CANNOT_CONNECT = ERROR_UNKNOWN + 2;
    public static final int ERROR_HTTP_DATA_ERROR = ERROR_UNKNOWN + 3;
    public static final int ERROR_FILE_ERROR = ERROR_UNKNOWN + 4;
    public static final int ERROR_INSUFFICIENT_SPACE = ERROR_UNKNOWN + 5;
    public static final int ERROR_FILE_DELETED = ERROR_UNKNOWN + 6;
    public static final int ERROR_FILE_VERIFY_FAIL = ERROR_UNKNOWN + 7;

    public static final int PENDING_MAX_TASK_RUNNING = 1;
    public static final int PENDING_WAITING_FOR_NETWORK = 2;
    public static final int PENDING_DEVICE_NOT_FOUND = 3;

    private static DownloadMgr sInstance;
    private MainController mMainControler;

    private DownloadMgr() {
    }

    public static DownloadMgr getInstance() {
        if (null == sInstance) {
            sInstance = new DownloadMgr();
        }
        return sInstance;
    }

    public void init(Context context) {
        if (null != mMainControler) {
            return;
        }
        mMainControler = new MainController(context);
    }

    public void registerObserver(IDownloadObserver observer) {
        mMainControler.getObserverManager().registerObserver(observer);
    }

    public void unregisterObserver(IDownloadObserver observer) {
        mMainControler.getObserverManager().unregisterObserver(observer);
    }

    public void enqueue(DownloadRequest request) {
        mMainControler.sendUserMsg(MsgSender.MSG_USER_ENQUEUE, request);
    }
    
    public void deleteTask(int downId) {
        mMainControler.sendUserMsg(MsgSender.MSG_USER_DELETE, new DeleteMsgData(downId));
    }
    
    public void deleteTask(int downId, boolean needDeleteFile) {
        mMainControler.sendUserMsg(MsgSender.MSG_USER_DELETE, new DeleteMsgData(downId, needDeleteFile));
    }

    public void pauseTask(int downId) {
        mMainControler.sendUserMsg(MsgSender.MSG_USER_PAUSE, new IdMsgData(downId));
    }

    public void startTask(int downId) {
        mMainControler.sendUserMsg(MsgSender.MSG_USER_CONTINUE, new IdMsgData(downId));
    }
    
    public void reDownload(int downId) {
        mMainControler.sendUserMsg(MsgSender.MSG_USER_RE_DOWNLOAD, new IdMsgData(downId));
    }

    public DownloadInfo getDownloadInfo(int downId) {
        return mMainControler.getDataHolder().getDownloadInfo(downId);
    }
    
    public DownloadInfo getDownloadInfoByUrl(String url) {
        return mMainControler.getDataHolder().getDownloadInfo(url);
    }

    public List<DownloadInfo> getCompleteList() {
        return mMainControler.getDataHolder().getCompleteList();
    }
    
    public List<DownloadInfo> getUncompletedList() {
        return mMainControler.getDataHolder().getUncompleteList();
    }

    public List<DownloadInfo> getAllDownloadInfoList() {
        return mMainControler.getDataHolder().getAllDownloadInfoList();
    }

    public void setAllowByMobileNet(DownloadInfo info, boolean isAllowMobileNet) {
        mMainControler.getDataHolder().setAllowMobileNet(info, isAllowMobileNet);
    }
    
    public void quit() {
        mMainControler.quit();
        mMainControler = null;
    }
    
    public static final class Setting {
        public static INotification sNotification;
        public static IFileVerify sFileVerify;

        private static int sMaxTaskNum = 2;
        private static int sObserverSpaceTime = 1000;
        public static long LOW_SPACE_SIZE = 5*1024*1024;

        public static int getMaxDownloadTask() {
            return sMaxTaskNum;
        }

        public static void setMaxDownloadTask(int maxDownloadTask) {

            Setting.sMaxTaskNum = Math.max(1, maxDownloadTask);
        }

        public static int getObserverSpaceTime() {
            return sObserverSpaceTime;
        }

        public static void setObserverSpaceTime(int observerSpaceTime) {
            if (observerSpaceTime < 100) {
                observerSpaceTime = 100;
            }
            Setting.sObserverSpaceTime = observerSpaceTime;
        }
    }

}
