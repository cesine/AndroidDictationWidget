package ca.ilanguage.aublog.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
//import android.util.Log;

public class DBTextAdapter {

	public static final String KEY_ROWID = "_id";
	public static final String KEY_TITLE = "title";
	public static final String KEY_CONTENT = "content";

	// private static final String TAG = "DBTextAdapter";
	private DBTextHelper mDbHelper;
	private SQLiteDatabase mDb;
	private static final String DATABASE_NAME = "text.db";
	private static final String DATABASE_TABLE = "post";
	private static final int DATABASE_VERSION = 1;
	private static final String DATABASE_CREATE = "create table post (_id integer primary key autoincrement, "
			+ "title text not null, content text not null);";
	private final Context mCtx;

	private static class DBTextHelper extends SQLiteOpenHelper {

		DBTextHelper(Context context) {
			super(context, DATABASE_NAME, null, DATABASE_VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			db.execSQL(DATABASE_CREATE);
			// Log.i(TAG, "DB '" + DATABASE_NAME + "' successfuly created");
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			// Log.w(TAG, "Upgrading database from version " + oldVersion +
			// " to "+ newVersion + ", which will destroy all old data");
			db.execSQL("DROP TABLE IF EXISTS post");
			onCreate(db);
		}
	}

	public DBTextAdapter(Context ctx) {
		this.mCtx = ctx;
	}

	public DBTextAdapter open() throws SQLException {
		mDbHelper = new DBTextHelper(mCtx);
		mDb = mDbHelper.getWritableDatabase();
		return this;
	}

	public void close() {
		mDbHelper.close();
	}

	public long createPost(String title, String content) throws SQLException {
		ContentValues initialValues = new ContentValues();
		initialValues.put(KEY_TITLE, title);
		initialValues.put(KEY_CONTENT, content);
		// Log.i(TAG, "Title: '" + title + "', content: '" + content+
		// "' successfully inserted");
		return mDb.insert(DATABASE_TABLE, null, initialValues);
	}

	public boolean deletePost(long rowId) {
		return mDb.delete(DATABASE_TABLE, KEY_ROWID + "=" + rowId, null) > 0;
	}

	public Cursor fetchAllPosts() {
		return mDb.query(DATABASE_TABLE, new String[]{KEY_ROWID, KEY_TITLE,
				KEY_CONTENT}, null, null, null, null, null);
	}

	public Cursor fetchPostdById(long rowId) throws SQLException {
		Cursor mCursor = mDb.query(true, DATABASE_TABLE, new String[]{
				KEY_ROWID, KEY_TITLE, KEY_CONTENT}, KEY_ROWID + "=" + rowId,
				null, null, null, null, null);
		if (mCursor != null) {
			mCursor.moveToFirst();
		}
		return mCursor;
	}

	public boolean updatePostById(long rowId, String title, String conten)
			throws SQLException {
		ContentValues args = new ContentValues();
		args.put(KEY_TITLE, title);
		args.put(KEY_CONTENT, conten);
		return mDb.update(DATABASE_TABLE, args, KEY_ROWID + "=" + rowId, null) > 0;
	}
}
