package com.gionee.download.core;

import android.os.Handler;
import android.os.Message;

import com.gionee.download.core.msg.DeleteMsgData;
import com.gionee.download.core.msg.IdMsgData;
import com.gionee.download.manager.DownloadMgr;
import com.gionee.download.manager.DownloadRequest;
import com.gionee.download.utils.LogUtils;

public class MsgHandler extends Handler implements IOnControllerDestroy {

    private static final String TAG = "MsgHandler";
	private MainController mControler;

    public MsgHandler(MainController controler) {
        mControler = controler;
    }

    @Override
    public void handleMessage(Message msg) {
        final int msgCategory = MsgSender.getCategory(msg.what);
        
        switch (msgCategory) {
        case MsgSender.CATEGORY_RUN:
            handleRuntimeMsg(msg);
            break;

        case MsgSender.CATEGORY_SYSTEM:
            handleSystemMsg(msg);
            break;

        case MsgSender.CATEGORY_USER:
            handleUserMsg(msg);
            break;

        default:
            break;
        }

    }

    private void handleRuntimeMsg(Message msg) {
        int what = msg.what;
        if(what != MsgSender.MSG_RUN_PROGRESS && what != MsgSender.MSG_RUN_FAIL){
            if (msg.obj instanceof DownloadInfo) {
                LogUtils.logi(TAG, "handleRuntimeMsg() msg.what= " + what + "  title=" + ((DownloadInfo)msg.obj).mTitle);
            }
        }
        
        
        switch (what) {
        case MsgSender.MSG_RUN_PROGRESS:
        case MsgSender.MSG_RUN_TOTAL:
            mControler.onProgress((DownloadInfo) msg.obj);
            break;

        case MsgSender.MSG_RUN_SUCCEED:
            mControler.onDownloadSucceed((DownloadInfo) msg.obj);
            break;

        case MsgSender.MSG_RUN_PENDING_NET:
            mControler.pendingTask((DownloadInfo) msg.obj, DownloadMgr.PENDING_WAITING_FOR_NETWORK);
            break;
            
        case MsgSender.MSG_RUN_PENDING_SD:
            mControler.pendingTask((DownloadInfo) msg.obj, DownloadMgr.PENDING_DEVICE_NOT_FOUND);
            break;
            
        case MsgSender.MSG_RUN_FAIL:
            DownloadException exception = (DownloadException) msg.obj;
            handleFail(exception);
            break;

        default:
            break;
        }
    }

    private void handleFail(DownloadException exception) {
        DownloadInfo info = exception.getTargetInfo();
        int code = exception.getCode();

        int reason = getFailReason(code);
        mControler.onDownloadFail(info, reason);
    }

    private int getFailReason(final int code) {
        switch (code) {
        case DownloadException.CODE_CREATE_CONNECTION_FAIL:
            return DownloadMgr.ERROR_CANNOT_CONNECT;
            
        case DownloadException.CODE_CHECK_RESOPNSE_CODE_FAIL:
            return DownloadMgr.ERROR_UNHANDLED_HTTP_CODE;
            
        case DownloadException.CODE_CREATE_INPUT_STREAM_FAIL:
        case DownloadException.CODE_READ_STREAM_FAIL:
            return DownloadMgr.ERROR_HTTP_DATA_ERROR;
            
        case DownloadException.CODE_SDCARD_POOR:
            return DownloadMgr.ERROR_INSUFFICIENT_SPACE;
            
        case DownloadException.CODE_CREATE_FILE_FAIL:
        case DownloadException.CODE_CREATE_FILE_STREAM_FAIL:
        case DownloadException.CODE_WRITE_STREAM_FAIL:
        case DownloadException.CODE_FILE_RENAME_FAIL:
            return DownloadMgr.ERROR_FILE_ERROR;
            
        case DownloadException.CODE_FILE_VERIFY_FAIL:
            return DownloadMgr.ERROR_FILE_VERIFY_FAIL;
        default:
            return DownloadMgr.ERROR_UNKNOWN;
        }
    }

    private void handleSystemMsg(Message msg) {
        LogUtils.logd(TAG, "handleSystemMsg() what = " + msg.what);
        switch (msg.what) {
        case MsgSender.MSG_SYS_NO_NET:
            mControler.hangTasks(DownloadMgr.PENDING_WAITING_FOR_NETWORK);
            break;
            
        case MsgSender.MSG_SYS_MEDIA_EJECT:
            mControler.hangTasks(DownloadMgr.PENDING_DEVICE_NOT_FOUND);
            break;

        case MsgSender.MSG_SYS_WIFI_NET:
        case MsgSender.MSG_SYS_MOBLIE_NET:
        case MsgSender.MSG_SYS_MEDIA_MOUNTED:
            mControler.tryResumeTasks();
            break;

        default:
            break;
        }
    }

    private void handleUserMsg(Message msg) {
        
        int downId = getDownId(msg);
        switch (msg.what) {
        case MsgSender.MSG_USER_PAUSE:
            
            mControler.pause(downId);
            break;

        case MsgSender.MSG_USER_CONTINUE:
            mControler.start(downId);
            break;

        case MsgSender.MSG_USER_RE_DOWNLOAD:
            mControler.reDownload(downId);
            break;
            
        case MsgSender.MSG_USER_DELETE:
            mControler.delete((DeleteMsgData) msg.obj);
            break;
            
        case MsgSender.MSG_USER_ENQUEUE:
            mControler.enqueue((DownloadRequest) msg.obj);
            break;
            
        default:
            break;
        }
    }

    private int getDownId(Message msg) {
        int downId = -1;
        
        if (msg.obj instanceof IdMsgData) {
            IdMsgData data = (IdMsgData) msg.obj;
            downId = data.getDownId();
        }
        return downId;
    }

    @Override
    public void onDestroy() {
        this.getLooper().quit();
        mControler = null;
    }

}
