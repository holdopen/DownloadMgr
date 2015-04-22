package com.gionee.download.core;

import java.util.HashSet;
import java.util.Set;

import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DBDataDeletePlan extends DelaySyncExecutor {

    private static final int SPCE_TIME = 80;

    private Set<Integer> mDeleteIds;
    private SQLiteOpenHelper mDBHelper;

    public DBDataDeletePlan(SQLiteOpenHelper databaseHelper) {
        this.mDBHelper = databaseHelper;
        mDeleteIds = new HashSet<Integer>();
    }

    @Override
    protected void onExecute() {
        if (mDeleteIds.size() == 0) {
            return;
        }

        deleteDownloadInfos();

        mDeleteIds.clear();
    }

    public void putDeleteId(int deleteDownId) {
        mDeleteIds.add(deleteDownId);

        setDelayed(SPCE_TIME);
    }

    private void deleteDownloadInfos() {
        SQLiteDatabase db = null;
        try {
            db = mDBHelper.getWritableDatabase();
            db.beginTransaction();
        
            for (int id : mDeleteIds) {
                db.delete(DBHelper.TABLE_NAME, DBHelper.COLUMN_ID + " = ?",
                        new String[] { id + "" });
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
