package com.gionee.download.core.msg;

public class IdMsgData implements IMsgData {

    private int mDownId;

    public IdMsgData(int downId) {
        this.mDownId = downId;
    }

    public int getDownId() {
        return mDownId;
    }
    
}
