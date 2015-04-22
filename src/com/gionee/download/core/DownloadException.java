package com.gionee.download.core;

import com.gionee.download.core.msg.IMsgData;

public class DownloadException extends Exception implements IMsgData {

    /**
     * 
     */
    private static final long serialVersionUID = 4509935684739611377L;

    public static final int CODE_UNDEFINED_EXCEPTION = -1;

    public static final int BASE_CODE_NET = 100;
    public static final int CODE_CREATE_CONNECTION_FAIL = BASE_CODE_NET + 1;
    public static final int CODE_CHECK_RESOPNSE_CODE_FAIL = BASE_CODE_NET + 2;
    public static final int CODE_CREATE_INPUT_STREAM_FAIL = BASE_CODE_NET + 3;
    public static final int CODE_READ_STREAM_FAIL = BASE_CODE_NET + 4;

    public static final int BASE_CODE_FILE = 1000;
    public static final int CODE_SDCARD_POOR = BASE_CODE_FILE + 1;
    public static final int CODE_CREATE_FILE_FAIL = BASE_CODE_FILE + 2;
    public static final int CODE_CREATE_FILE_STREAM_FAIL = BASE_CODE_FILE + 3;
    public static final int CODE_WRITE_STREAM_FAIL = BASE_CODE_FILE + 4;
    public static final int CODE_FILE_RENAME_FAIL = BASE_CODE_FILE + 5;
    public static final int CODE_FILE_VERIFY_FAIL = BASE_CODE_FILE + 6;

    private int mCode = CODE_UNDEFINED_EXCEPTION;
    
    private DownloadInfo mTargetInfo;

    public DownloadException(int code, Throwable src) {
        super(buildMsg(code), src);
        this.mCode = code;
    }

    public DownloadException(int code) {
        super(buildMsg(code));
        mCode = code;
    }

    public DownloadException(Throwable src) {
        super(src);
    }

    private static String buildMsg(int code) {
        StringBuilder msg = new StringBuilder();
        msg.append("code=").append(code).append("  ").append(getCodeMsg(code)).append("  ");
        return msg.toString();
    }

    private static String getCodeMsg(int code) {
        return "";
    }

    public int getCode() {
        return mCode;
    }

    public DownloadInfo getTargetInfo() {
        return mTargetInfo;
    }

    public void setTargetInfo(DownloadInfo targetInfo) {
        this.mTargetInfo = targetInfo;
    }

}
