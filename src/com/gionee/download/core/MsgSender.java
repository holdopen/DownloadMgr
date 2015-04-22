package com.gionee.download.core;

import com.gionee.download.core.msg.IMsgData;

import android.os.Message;



public class MsgSender {

    public static final int CATEGORY_USER = 500;
    public static final int CATEGORY_SYSTEM = 1000;
    public static final int CATEGORY_RUN = 10000;

    public static final int MSG_USER_ENQUEUE = CATEGORY_USER + 1;
    public static final int MSG_USER_PAUSE = CATEGORY_USER + 2;
    public static final int MSG_USER_CONTINUE = CATEGORY_USER + 3;
    public static final int MSG_USER_DELETE = CATEGORY_USER + 4;
    public static final int MSG_USER_RE_DOWNLOAD = CATEGORY_USER + 5;

    public static final int MSG_SYS_WIFI_NET = CATEGORY_SYSTEM + 1;
    public static final int MSG_SYS_MOBLIE_NET = CATEGORY_SYSTEM + 2;
    public static final int MSG_SYS_NO_NET = CATEGORY_SYSTEM + 3;
    public static final int MSG_SYS_MEDIA_EJECT = CATEGORY_SYSTEM + 4;
    public static final int MSG_SYS_MEDIA_MOUNTED = CATEGORY_SYSTEM + 5;

    public static final int MSG_RUN_TOTAL = CATEGORY_RUN + 2;
    public static final int MSG_RUN_PROGRESS = CATEGORY_RUN + 3;
    public static final int MSG_RUN_SUCCEED = CATEGORY_RUN + 4;
    public static final int MSG_RUN_FAIL = CATEGORY_RUN + 5;
    public static final int MSG_RUN_PENDING_NET = CATEGORY_RUN + 6;
    public static final int MSG_RUN_PENDING_SD = CATEGORY_RUN + 7;

    private MsgHandler mMsgHandler;

    public MsgSender(MsgHandler mMsgHandler) {
        this.mMsgHandler = mMsgHandler;
    }

    public void send(int what) {
        mMsgHandler.obtainMessage(what).sendToTarget();
    }

    public void send(int what, IMsgData msgData) {
        mMsgHandler.obtainMessage(what, msgData).sendToTarget();
    }
    
    public void sendDelayed(int what, IMsgData msgData, int delayMillis) {
        Message msg = mMsgHandler.obtainMessage(what, msgData);
        mMsgHandler.sendMessageDelayed(msg, delayMillis);
    }
    
    public static int getCategory(int what) {
        if (what > CATEGORY_RUN) {
            return CATEGORY_RUN;
        }

        if (what > CATEGORY_SYSTEM) {
            return CATEGORY_SYSTEM;
        }

        return CATEGORY_USER;
    }
}
