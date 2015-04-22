package com.gionee.download.core;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;

public class DBHelper extends SQLiteOpenHelper {
	public static final String TABLE_NAME = "download_info";
	// column name
	public static final String COLUMN_ID = "_id";
	public static final String COLUMN_SUCCEED_TIME = "succeed_timestamp";
	public static final String COLUMN_LOCAL_URI = "local_rui";
	public static final String COLUMN_REASON = "reason";
	public static final String COLUMN_STATUS = "status";
	public static final String COLUMN_TITLE = "title";
	public static final String COLUMN_URI = "uri";
	public static final String COLUMN_PROGRESS = "bytes_so_far";
	public static final String COLUMN_TOTAL = "total_size";
	public static final String COLUMN_ALLOW_MOBILE = "allow_by_mobile_net";
	public static final String COLUMN_EXTRA_JSON = "extra_info_json";

	public DBHelper(Context context, String name, CursorFactory factory, int version) {
		super(context.getApplicationContext(), name, factory, version);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		StringBuilder sb = new StringBuilder();
		sb.append("create table if not exists ");
		sb.append(TABLE_NAME);
		sb.append("(").append(COLUMN_ID).append(" integer primary key,");
		sb.append(COLUMN_SUCCEED_TIME).append(" integer,");
		sb.append(COLUMN_LOCAL_URI).append(" varchar,");
		sb.append(COLUMN_EXTRA_JSON).append(" TEXT,");
		sb.append(COLUMN_REASON).append(" integer,");
		sb.append(COLUMN_STATUS).append(" integer,");
		sb.append(COLUMN_TITLE).append(" varchar,");
		sb.append(COLUMN_URI).append(" varchar,");
		sb.append(COLUMN_PROGRESS).append(" integer,");
		sb.append(COLUMN_ALLOW_MOBILE).append(" integer,");
		sb.append(COLUMN_TOTAL).append(" integer)");
		db.execSQL(sb.toString());
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

	}
	
}
