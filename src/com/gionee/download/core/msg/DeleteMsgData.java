package com.gionee.download.core.msg;

public class DeleteMsgData extends IdMsgData {

    private boolean mNeedDeleteFile = true;
    
    public DeleteMsgData(int downId) {
        super(downId);
    }

    public DeleteMsgData(int downId, boolean needDeleteFile) {
        super(downId);
        this.mNeedDeleteFile = needDeleteFile;
    }
    
    public boolean needDeleteFile() {
        return mNeedDeleteFile;
    }

}
