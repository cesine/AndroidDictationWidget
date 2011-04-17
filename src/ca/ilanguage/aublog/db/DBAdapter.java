package ca.ilanguage.aublog.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
//import android.util.Log;

public class DBAdapter {

	public static final String KEY_ROWID = "_id";
	public static final String KEY_LOGIN = "login";
	public static final String KEY_PASSWORD = "password";

	// private static final String TAG = "DBAdapter";
	private DatabaseHelper mDbHelper;
	private SQLiteDatabase mDb;
	private static final String DATABASE_NAME = "aublog.db";
	private static final String DATABASE_TABLE = "settings";
	private static final int DATABASE_VERSION = 1;
	private static final String DATABASE_CREATE = "create table settings (_id integer primary key autoincrement, "
			+ "login text not null, password text not null);";
	private final Context mCtx;

	private static class DatabaseHelper extends SQLiteOpenHelper {

		DatabaseHelper(Context context) {
			super(context, DATABASE_NAME, null, DATABASE_VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			db.execSQL(DATABASE_CREATE);
			// Log.i(TAG, "DB '" + DATABASE_NAME + "' successfully created");
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			// Log.w(TAG, "Upgrading database from version " + oldVersion +
			// " to "+ newVersion + ", which will destroy all old data");
			db.execSQL("DROP TABLE IF EXISTS settings");
			onCreate(db);
		}
	}

	public DBAdapter(Context ctx) {
		this.mCtx = ctx;
	}

	public DBAdapter open() throws SQLException {
		mDbHelper = new DatabaseHelper(mCtx);
		mDb = mDbHelper.getWritableDatabase();
		return this;
	}

	public void close() {
		mDbHelper.close();
	}

	public long createSetting(String login, String password)
			throws SQLException {
		ContentValues initialValues = new ContentValues();
		initialValues.put(KEY_LOGIN, login);
		initialValues.put(KEY_PASSWORD, password);
		// Log.i(TAG, "Login: '" + login + "', password: '" + password+
		// "' successfuly inserted");
		return mDb.insert(DATABASE_TABLE, null, initialValues);
	}

	public boolean deleteSetting(long rowId) {
		return mDb.delete(DATABASE_TABLE, KEY_ROWID + "=" + rowId, null) > 0;
	}

	public Cursor fetchAllSettings() {
		return mDb.query(DATABASE_TABLE, new String[]{KEY_ROWID, KEY_LOGIN,
				KEY_PASSWORD}, null, null, null, null, null);
	}

	public Cursor fetchSettindById(long rowId) throws SQLException {
		Cursor mCursor = mDb.query(true, DATABASE_TABLE, new String[]{
				KEY_ROWID, KEY_LOGIN, KEY_PASSWORD}, KEY_ROWID + "=" + rowId,
				null, null, null, null, null);
		if (mCursor != null) {
			mCursor.moveToFirst();
		}
		return mCursor;
	}

	public boolean updateSettingById(long rowId, String login, String password)
			throws SQLException {
		ContentValues args = new ContentValues();
		args.put(KEY_LOGIN, login);
		args.put(KEY_PASSWORD, password);
		return mDb.update(DATABASE_TABLE, args, KEY_ROWID + "=" + rowId, null) > 0;
	}
}
