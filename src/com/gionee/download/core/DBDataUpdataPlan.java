package com.gionee.download.core;

import java.util.HashSet;
import java.util.Set;

import com.gionee.download.manager.DownloadMgr;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DBDataUpdataPlan extends DelaySyncExecutor {

    private static final int UPDATA_SPCE_TIME = 1000;
    
    private Set<DownloadInfo> mNeedUpdataInfos;
    private SQLiteOpenHelper mDBHelper;
    
    public DBDataUpdataPlan(SQLiteOpenHelper databaseHelper) {
        this.mDBHelper = databaseHelper;
        mNeedUpdataInfos = new HashSet<DownloadInfo>();
    }
    
    @Override
    protected void onExecute() {
        if (mNeedUpdataInfos.size() == 0) {
            return;
        }
        
        updateDownloadInfos();
        
        mNeedUpdataInfos.clear();
    }
    
    public void putUpdateDownloadInfo(DownloadInfo downloadInfo) {
        if (mNeedUpdataInfos.size() == 0) {
            setDelayed(UPDATA_SPCE_TIME);
        }
        mNeedUpdataInfos.add(downloadInfo);
    }

    public void removeUpdateDownloadInfo(DownloadInfo downloadInfo) {
        mNeedUpdataInfos.remove(downloadInfo);
        if (mNeedUpdataInfos.size() == 0) {
            cancelTask();
        }
    }

    private void updateDownloadInfos() {
        SQLiteDatabase db = null;
        try {
            db = mDBHelper.getWritableDatabase();
            db.beginTransaction();
            for (DownloadInfo info : mNeedUpdataInfos) {
                ContentValues values = new ContentValues();
                values.put(DBHelper.COLUMN_TOTAL, info.mTotal);
                values.put(DBHelper.COLUMN_PROGRESS, info.mProgress);
                values.put(DBHelper.COLUMN_STATUS, info.mStatus);
                values.put(DBHelper.COLUMN_REASON, info.mReason);
                values.put(DBHelper.COLUMN_ALLOW_MOBILE, info.mIsAllowMobileNet ? 1 : 0);
                if (info.mStatus == DownloadMgr.STATUS_SUCCESSFUL) {
                    values.put(DBHelper.COLUMN_SUCCEED_TIME, info.mSucceedTime);
                }
                db.update(DBHelper.TABLE_NAME, values,
                        DBHelper.COLUMN_ID + " = ?", new String[] { info.mDownId + "" });
            }
            
            db.setTransactionSuccessful();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                db.endTransaction();
                db.close();
            } catch (Exception e) {
            }
        }
    }
}
