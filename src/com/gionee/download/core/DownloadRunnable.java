package com.gionee.download.core;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;

import android.content.Context;

import com.gionee.download.manager.DownloadMgr.Setting;
import com.gionee.download.manager.DownloadMgr;
import com.gionee.download.manager.IFileVerify;
import com.gionee.download.utils.LogUtils;
import com.gionee.download.utils.StorageUtils;
import com.gionee.download.utils.Utils;

public class DownloadRunnable implements Runnable {

    private static final int CONNECT_TIMEOUT = 15000;
    private static final int READ_TIMEOUT = 15000;
    
    private static final int STATE_CONNECTING = 1;
    private static final int STATE_RUNNING = 2;
    private static final int STATE_STOP = 3;
    private static final int STATE_COMPLETE = 4;

    private static final String TAG = "DownloadRunnable";

    private int mState = STATE_CONNECTING;

    private InfoWriteProxy mInfoWriteProxy;
    private DownloadInfo mInfo;

    private HttpURLConnection mConnection;
    private RandomAccessFile mFileOutStream;
    private BufferedInputStream mNetInputStream;

    private boolean mIsActive = false;
    private DownloadRetryRecord mRetryRecord;

    public DownloadRunnable(InfoWriteProxy infoWriteProxy) {
        this.mInfo = infoWriteProxy.getInfo();
        this.mInfoWriteProxy = infoWriteProxy;
        mRetryRecord = new DownloadRetryRecord();
    }

    public boolean isActive() {
        return mIsActive;
    }

    public void stop() {
        setState(STATE_STOP);
        mInfoWriteProxy.close();
    }

    @Override
    public void run() {
        mIsActive = true;
        try {
            download();
        } catch (Exception e) {
            handleExeption(e);
        } finally {
            closeDownload();

            mIsActive = false;
        }
    }

    private HttpURLConnection createConnection() throws DownloadException {
        try {
            URL url = new URL(mInfo.getUrl());
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setConnectTimeout(CONNECT_TIMEOUT);
            connection.setReadTimeout(READ_TIMEOUT);
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Accept-Language", "zh-CN");
            connection.setRequestProperty("Referer", url.toString());
            connection.setRequestProperty("Charset", "UTF-8");
            connection.setRequestProperty("Connection", "Keep-Alive");
            connection.setRequestProperty("Accept-Encoding", "identity");
            
            return connection;
        } catch (IOException e) {
            throw new DownloadException(DownloadException.CODE_CREATE_CONNECTION_FAIL, e);
        }
    }

    private void download() throws DownloadException {
        mConnection = createConnection();
        
        setDownloadRange();

        initTotal();

//        checkResponseCode();

        mNetInputStream = preNetInputStream();

        mFileOutStream = preFileOutStream();

        doingDownload();

        if (STATE_COMPLETE == mState) {
            onDownloadComplete();
        }
    }

    private void setDownloadRange() {
        mConnection.setRequestProperty("Range", "bytes=" + mInfo.getProgress() + "-");
    }

//    private void checkResponseCode() throws DownloadException {
//        int responseCode = getResponseCode();
//
//        if (responseCode != HttpURLConnection.HTTP_OK && responseCode != HttpURLConnection.HTTP_PARTIAL) {
//            mConnection.disconnect();
//            throw new DownloadException(DownloadException.CODE_CHECK_RESOPNSE_CODE_FAIL);
//        }
//    }

//    private int getResponseCode() throws DownloadException {
//        try {
//            return mConnection.getResponseCode();
//        } catch (IOException e) {
//            throw new DownloadException(DownloadException.CODE_CHECK_RESOPNSE_CODE_FAIL, e);
//        }
//    }

    private void initTotal() throws DownloadException {
        if (mInfo.getProgress() == 0) {
            int totalSize = mConnection.getContentLength();
            mInfoWriteProxy.setTotal(totalSize);
        }
    }

    private BufferedInputStream preNetInputStream() throws DownloadException {
        InputStream inputStream;
        try {
            inputStream = mConnection.getInputStream();
            BufferedInputStream bis = new BufferedInputStream(inputStream);
            
            return bis;
        } catch (IOException e) {
            throw new DownloadException(DownloadException.CODE_CREATE_INPUT_STREAM_FAIL, e);
        }
    }

    private RandomAccessFile preFileOutStream() throws DownloadException {
        try {
            File file = prepareFile();

            RandomAccessFile outFileStream = new RandomAccessFile(file, "rw");
            outFileStream.seek(mInfo.getProgress() >= 0 ? mInfo.getProgress() : 0);

            return outFileStream;
        } catch (IOException e) {
            throw new DownloadException(DownloadException.CODE_CREATE_FILE_STREAM_FAIL, e);
        }
    }

    private File prepareFile() throws DownloadException {
        File file = new File(mInfo.getLocalTempFilePath());

        if (false == file.getParentFile().exists()) {
            file.getParentFile().mkdirs();
        }

        if (false == file.exists()) {
            file = createNewFile(file);
        }

        return file;
    }

    private File createNewFile(File file) throws DownloadException {
        try {
            file.createNewFile();
            return file;
        } catch (IOException e) {
            throw new DownloadException(DownloadException.CODE_CREATE_FILE_FAIL, e);
        }
    }

    private void doingDownload() throws DownloadException {
        setState(STATE_RUNNING);

        byte[] cache = new byte[10 * 1024];
        while (mState == STATE_RUNNING) {
            int len = read(cache);
            if (len == -1) {
                setState(STATE_COMPLETE);
                return;
            }

            write(cache, len);
            mInfoWriteProxy.addProgress(len);
        }
    }

    private void setState(int state) {
        if (isStopped()) {
            return;
        }

        this.mState = state;
    }

    private boolean isStopped() {
        return STATE_STOP == mState;
    }

    private int read(byte[] cache) throws DownloadException {
        try {
            return mNetInputStream.read(cache);
        } catch (IOException e) {
            throw new DownloadException(DownloadException.CODE_READ_STREAM_FAIL, e);
        }
    }

    private void write(byte[] cache, int len) throws DownloadException {
        try {
            mFileOutStream.write(cache, 0, len);
        } catch (IOException e) {
            throw new DownloadException(DownloadException.CODE_WRITE_STREAM_FAIL, e);
        }
    }

    private void onDownloadComplete() throws DownloadException {
        renameFile();

        verifyFile();

        mInfoWriteProxy.setDownloadSuccee();
    }

    private void renameFile() throws DownloadException {
        File file = new File(mInfo.getLocalTempFilePath());

        File newPath = new File(mInfo.getLocalPath());
        if (newPath.exists()) {
            newPath.delete();
        }

        if (false == file.renameTo(newPath)) {
            throw new DownloadException(DownloadException.CODE_FILE_RENAME_FAIL);
        }
    }

    private void verifyFile() throws DownloadException {
        IFileVerify fileVerify = Setting.sFileVerify;

        if (fileVerify == null) {
            return;
        }

        if (false == fileVerify.onVerify(mInfo, new File(mInfo.getLocalPath()))) {
            throw new DownloadException(DownloadException.CODE_FILE_VERIFY_FAIL);
        }
    }

    private void handleExeption(Exception exception) {
        if (isStopped()) {
            return;
        }
        closeDownload();
        LogUtils.logi(TAG, "handleExeption target info = " + mInfo.getTitle() + "  mInfo.isAllowMobileNet()" + mInfo.isAllowMobileNet());
        if (mRetryRecord.hasRetryTime()) {
            retry();
        } else {
            DownloadException targetException = translationException(exception);
            mInfoWriteProxy.setDownloadFial(targetException);
            LogUtils.logw(TAG, "handleExeption", targetException);
        }
    }

    private void retry() {
        if (DownloadMgr.STATUS_RUNNING != mInfo.getStatus()) {
            return;
        }
        
        if (isStopped()) {
            return;
        }
        
        Context context = MainController.getContext();
        LogUtils.logi(TAG, "handleExeption network = " + Utils.getNetworkType(context));
        if (!Utils.hasNetwork(context) || (Utils.isMobileNet(context) && !mInfo.isAllowMobileNet())) {
            mInfoWriteProxy.setStautsPendingNet();
        } else if(!StorageUtils.isSDCardMounted()) {
            mInfoWriteProxy.setStautsPendingSD();
        } else {
            run();
        }
    }

    private DownloadException translationException(Exception exception) {
        if (exception instanceof DownloadException) {
            DownloadException downloadException = (DownloadException) exception;
            if (downloadException.getCause() instanceof IOException) {
                if (isLowSpaceSize()) {
                    return new DownloadException(DownloadException.CODE_SDCARD_POOR,
                            downloadException.getCause());
                }
            }
            return downloadException;
        } else {
            return new DownloadException(exception);
        }
    }

    private boolean isLowSpaceSize() {
        long freeSpace = new File(mInfo.getLocalPath()).getParentFile().getFreeSpace();
        return freeSpace < DownloadMgr.Setting.LOW_SPACE_SIZE;
    }

    private void closeDownload() {
        try {
            closeFileOutStream();
            closeNetInputStream();
            disConnection();
        } catch (Exception e) {
//            e.printStackTrace();
        }
    }

    private void closeNetInputStream() {
        if (null == mNetInputStream) {
            return;
        }

        try {
            mNetInputStream.close();
            mNetInputStream = null;
        } catch (IOException e) {
            // e.printStackTrace();
        }
    }

    private void closeFileOutStream() {
        if (null == mFileOutStream) {
            return;
        }

        try {
            mFileOutStream.close();
            mFileOutStream = null;
        } catch (IOException e) {
            // e.printStackTrace();
        }
    }

    private void disConnection() {
        if (null == mConnection) {
            return;
        }
        mConnection.disconnect();
        mConnection = null;
    }
}
