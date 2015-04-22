package com.gionee.download.core;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;

public final class DBFacade implements IOnControllerDestroy {

    public static final String DATABASE_NAME = "download_mgr_info.db";
    private static final int DATABASE_VER = 2;
    private DBHelper mDatabaseHelper;

    private DBDataUpdataPlan mUpdataPlan;
    private DBDataDeletePlan mDeletePlan;
    

    protected DBFacade(Context context) {
        mDatabaseHelper = new DBHelper(context, DATABASE_NAME, null, DATABASE_VER);
        mUpdataPlan = new DBDataUpdataPlan(mDatabaseHelper);
        mDeletePlan = new DBDataDeletePlan(mDatabaseHelper);
    }

    public void deleteDownloadInfo(int id) {
        mDeletePlan.putDeleteId(id);
    }
    
    public void postUpdateInfo(DownloadInfo downloadInfo) {
        mUpdataPlan.putUpdateDownloadInfo(downloadInfo);
    }

    public void removeUpdateDownloadInfo(DownloadInfo downloadInfo) {
        mUpdataPlan.removeUpdateDownloadInfo(downloadInfo);
    }
    
    public List<DownloadInfo> getDownloadInfoList() {
        List<DownloadInfo> list = new ArrayList<DownloadInfo>();
        SQLiteDatabase db = null;
        Cursor cursor = null;
        try {
            db = mDatabaseHelper.getReadableDatabase();
            cursor = db.query(DBHelper.TABLE_NAME, null,
                    null, null, null, null, null);
            if (cursor != null && cursor.getCount() > 0) {
                for (int i = 0; i < cursor.getCount(); i++) {
                    cursor.moveToPosition(i);
                    DownloadInfo info = createDowloadInfo(cursor);
                    list.add(info);
                }
            }
        }catch(Exception e) {
            e.printStackTrace();
        } finally {
            try {
                cursor.close();
                db.close();
            } catch (Exception e) {
            }
        }
        return list;
    }

    private DownloadInfo createDowloadInfo(Cursor sor) {
        DownloadInfo info = new DownloadInfo();
        info.mDownId = sor.getInt(sor.getColumnIndex(DBHelper.COLUMN_ID));
        info.mTotal = sor.getInt(sor.getColumnIndex(DBHelper.COLUMN_TOTAL));
        info.mProgress = sor.getInt(sor.getColumnIndex(DBHelper.COLUMN_PROGRESS));
        info.mStatus = sor.getInt(sor.getColumnIndex(DBHelper.COLUMN_STATUS));
        info.mReason = sor.getInt(sor.getColumnIndex(DBHelper.COLUMN_REASON));
        info.mLocalPath = sor.getString(sor.getColumnIndex(DBHelper.COLUMN_LOCAL_URI));
        info.mTitle = sor.getString(sor.getColumnIndex(DBHelper.COLUMN_TITLE));
        info.mUrl = sor.getString(sor.getColumnIndex(DBHelper.COLUMN_URI));
        info.mSucceedTime = sor.getLong(sor.getColumnIndex(DBHelper.COLUMN_SUCCEED_TIME));
        info.mIsAllowMobileNet = sor.getInt(sor.getColumnIndex(DBHelper.COLUMN_ALLOW_MOBILE)) == 1;
        String json = sor.getString(sor.getColumnIndex(DBHelper.COLUMN_EXTRA_JSON));
        if (!TextUtils.isEmpty(json)) {
            try {
                info.mExtraInfo = new JSONObject(json);
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }
        return info;
    }

    public int addDownloadInfo(DownloadInfo downloadInfo) {
        ContentValues values = new ContentValues();
        values.put(DBHelper.COLUMN_TOTAL, downloadInfo.mTotal);
        values.put(DBHelper.COLUMN_PROGRESS, downloadInfo.mProgress);
        values.put(DBHelper.COLUMN_STATUS, downloadInfo.mStatus);
        values.put(DBHelper.COLUMN_REASON, downloadInfo.mReason);
        values.put(DBHelper.COLUMN_LOCAL_URI, downloadInfo.mLocalPath);
        values.put(DBHelper.COLUMN_TITLE, downloadInfo.mTitle);
        values.put(DBHelper.COLUMN_URI, downloadInfo.mUrl);
        values.put(DBHelper.COLUMN_SUCCEED_TIME, downloadInfo.mSucceedTime);
        values.put(DBHelper.COLUMN_ALLOW_MOBILE, downloadInfo.mIsAllowMobileNet ? 1 : 0);
        if (downloadInfo.mExtraInfo != null && downloadInfo.mExtraInfo.length() > 0) {
            values.put(DBHelper.COLUMN_EXTRA_JSON, downloadInfo.mExtraInfo.toString());
        }
        try {
            downloadInfo.mDownId = (int) mDatabaseHelper.getWritableDatabase().insert(
                    DBHelper.TABLE_NAME, null, values);
            return downloadInfo.mDownId;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return DownloadInfo.INVALID_DOWN_ID;
    }

    @Override
    public void onDestroy() {
        mUpdataPlan.cancelDelay();
        mUpdataPlan.cancelDelay();
    }

}
