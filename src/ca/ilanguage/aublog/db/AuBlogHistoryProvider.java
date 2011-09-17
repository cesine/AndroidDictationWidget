package ca.ilanguage.aublog.db;

import java.util.HashMap;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.provider.LiveFolders;
import android.text.TextUtils;
import android.util.Log;
import ca.ilanguage.aublog.db.AuBlogHistoryDatabase;
import ca.ilanguage.aublog.db.AuBlogHistoryDatabase.AuBlogHistory;


public class AuBlogHistoryProvider extends ContentProvider {
	//procedure from DBTextAdapter
//	public final Context mCtx;
//	private DatabaseHelper mDbHelper;
//	private SQLiteDatabase mDb;
//	private AuBlogHistoryDatabase ???;  //the database has a null constructor so its essentially just a definition of constants.. with no fucntions anyway. 
	
    private static final String TAG = "AuBlogHistoryProvider";

    private static final String DATABASE_NAME = "aubloghistory.db";
    private static final int DATABASE_VERSION = 5;
    private static final int DATABASE_CHANGED_COLUMN_NAMES = 3;
    //private static final String  AUBLOG_HISTORY_TABLE_NAME= AuBlogHistoryDatabase.AUBLOG_HISTORY_TABLE_NAME;

    private static HashMap<String, String> sAuBlogHistoryProjectionMap;
    private static HashMap<String, String> sLiveFolderProjectionMap;

    //retrieve options?
    private static final int AUBLOGHISTORIES = 1;
    private static final int AUBLOGHISTORY_ID = 2;
    private static final int LIVE_FOLDER_AUBLOGHISTORYS = 3;

    private static final UriMatcher sUriMatcher;
    
   
    public Uri createPost(ContentValues initialValues) throws SQLException {
		return insert(AuBlogHistory.CONTENT_URI, initialValues);
	}
    public Cursor fetchPostByUri( Uri uri) throws SQLException {
    	return query(uri, null, null, null, null);
    }
	public boolean deletePostByUri(Uri uri) {
		return delete(uri, null, null) > 0;
	}
	public Cursor fetchAllPosts() {
		return query(AuBlogHistory.CONTENT_URI, null, null, null, null);
	}

//	/*
//	 * add a where clause where publised=1 not published=0
//	 */
//	public Cursor fetchAllPublishedPosts() {
//		//TODO
//		return null;
//	}
//	/* 
//	 * select all posts where this id is mentioned in the parent or daughter, and their parent and daughters...
//	 */
//	public Cursor fetchPostTree(long rowId) {
//		//TODO
//		return  null;
//	}
//    

    private DatabaseHelper mOpenHelper;
//
//    /*
//     * from DBTextAdapter, probably not best practices...
//     * 
//     * to improve them use the providers own methods (which wrap the sql database methods) rather than the methods from the sql database...
//     */
    //couldnt compile with this constructor
//    public AuBlogHistoryProvider(Context context){
//    	this.mCtx=context;
//    }
//    public AuBlogHistoryProvider open() throws SQLException{
////    	mDbHelper = new DatabaseHelper(mCtx);
////		mDb = mDbHelper.getWritableDatabase();
//		return this;
//    }

//    /*
//     * probably broken
//     */
//    public Cursor fetchPostdById(long rowId) throws SQLException {
////		Cursor mCursor = mDb.query(true, AuBlogHistoryDatabase.AUBLOG_HISTORY_TABLE_NAME, new String[]{
////				AuBlogHistory._ID, AuBlogHistory.ENTRY_TITLE, AuBlogHistory.ENTRY_CONTENT}, AuBlogHistory._ID + "=" + rowId,
////				null, null, null, null, null);
//    	Cursor cursor = query(AuBlogHistory.CONTENT_URI, new String[]{
//				AuBlogHistory._ID, AuBlogHistory.ENTRY_TITLE, AuBlogHistory.ENTRY_CONTENT}, null,null, AuBlogHistory.DEFAULT_SORT_ORDER);
//		if (cursor != null) {
//			cursor.moveToFirst();
//		}
//		return cursor;
//	}

//    public boolean updatePostById(Long rowId, String title, String content) throws SQLException {
//		//TODO: put the args into what it takes, rather than constructing them here
//    	ContentValues args = new ContentValues();
//		args.put(AuBlogHistory.ENTRY_TITLE, title);
//		args.put(AuBlogHistory.ENTRY_CONTENT, content);
////		return mDb.update(AuBlogHistoryDatabase.AUBLOG_HISTORY_TABLE_NAME, args, AuBlogHistory._ID + "=" + rowId, null) > 0;
//		return update(AuBlogHistory.CONTENT_URI.buildUpon().appendPath(rowId.toString()).build(), args, null, null) > 0;
//    }
//	public boolean deletePost(Long rowId) {
//		//DONE might be broken, just included for compatability wiht existing code
//		return delete(AuBlogHistory.CONTENT_URI.buildUpon().appendPath(rowId.toString()).build(), null, null) > 0;
//	}
	public Cursor fetchDaughters(Uri uri){
		return query(uri, null, AuBlogHistory.PARENT_ENTRY +"="+uri.getLastPathSegment(), null, null);
	}
     
	@Override
	public int delete(Uri uri, String where, String[] whereArgs) {
		SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        int count;
        switch (sUriMatcher.match(uri)) {
        case AUBLOGHISTORIES:
            count = db.delete(AuBlogHistoryDatabase.AUBLOG_HISTORY_TABLE_NAME, where, whereArgs);
            break;

        case AUBLOGHISTORY_ID:
            String itemId = uri.getPathSegments().get(1);
            count = db.delete(AuBlogHistoryDatabase.AUBLOG_HISTORY_TABLE_NAME, AuBlogHistory._ID + "=" + itemId
                    + (!TextUtils.isEmpty(where) ? " AND (" + where + ')' : ""), whereArgs);
            break;

        default:
            throw new IllegalArgumentException("Unknown URI " + uri);
        }

        getContext().getContentResolver().notifyChange(uri, null);
        return count;
	}

	@Override
	public String getType(Uri uri) {
		switch (sUriMatcher.match(uri)) {
        case AUBLOGHISTORIES:
        case LIVE_FOLDER_AUBLOGHISTORYS:
            return AuBlogHistory.CONTENT_TYPE;

        case AUBLOGHISTORY_ID:
            return AuBlogHistory.CONTENT_ITEM_TYPE;

        default:
            throw new IllegalArgumentException("Unknown URI " + uri);
        }
	}

	@Override
	public Uri insert(Uri uri, ContentValues initialValues) {
//initial values are null
    	
    	
        // Validate the requested uri
        if (sUriMatcher.match(uri) != AUBLOGHISTORIES) {
            throw new IllegalArgumentException("Unknown URI " + uri);
        }

        ContentValues values;
        if (initialValues != null) {
            values = new ContentValues(initialValues);
        } else {
            values = new ContentValues();
        }

        Long now = Long.valueOf(System.currentTimeMillis());

        // Make sure that the fields are all set
        if (values.containsKey(AuBlogHistory.TIME_CREATED) == false) {
            values.put(AuBlogHistory.TIME_CREATED, now);
        }
        if (values.containsKey(AuBlogHistory.LAST_MODIFIED) == false) {
            values.put(AuBlogHistory.LAST_MODIFIED, now);
        }
        if (values.containsKey(AuBlogHistory.TIME_EDITED) == false) {
            values.put(AuBlogHistory.TIME_EDITED, "0");
        }
        if (values.containsKey(AuBlogHistory.PUBLISHED) == false) {
            values.put(AuBlogHistory.PUBLISHED, "0");
        }       
        if (values.containsKey(AuBlogHistory.PARENT_ENTRY) == false) {
            values.put(AuBlogHistory.PARENT_ENTRY, AuBlogHistoryDatabase.ROOT_ID_DEFAULT);
        }  
        if (values.containsKey(AuBlogHistory.DELETED) == false) {
            values.put(AuBlogHistory.DELETED, "0");
        }  

      // initialize nullable fields here
        if (values.containsKey(AuBlogHistory.ENTRY_TITLE) == false) {
//            Resources r = Resources.getSystem();
            values.put(AuBlogHistory.ENTRY_TITLE, "");// r.getString(android.R.string.untitled_blog_title));
        }
        if (values.containsKey(AuBlogHistory.ENTRY_CONTENT) == false) {
            values.put(AuBlogHistory.ENTRY_CONTENT, "");
        }       
        if (values.containsKey(AuBlogHistory.ENTRY_LABELS) == false) {
            values.put(AuBlogHistory.ENTRY_LABELS, "");
        }       
     
     
        if (values.containsKey(AuBlogHistory.AUDIO_FILE) == false) {
            values.put(AuBlogHistory.AUDIO_FILE, "");
        }
        if (values.containsKey(AuBlogHistory.AUDIO_FILE_STATUS) == false) {
            values.put(AuBlogHistory.AUDIO_FILE_STATUS, "");
        }
        
        if (values.containsKey(AuBlogHistory.TRANSCRIPTION_STATUS) == false) {
            values.put(AuBlogHistory.TRANSCRIPTION_STATUS, "");
        }
        
        if (values.containsKey(AuBlogHistory.TRANSCRIPTION_RESULT) == false) {
            values.put(AuBlogHistory.TRANSCRIPTION_RESULT, "");
        }
        if (values.containsKey(AuBlogHistory.LANGUAGE_MODEL_PDFS) == false) {
            values.put(AuBlogHistory.LANGUAGE_MODEL_PDFS, "");
        }
        if (values.containsKey(AuBlogHistory.LANGUAGE_MODEL_WEBPAGES) == false) {
            values.put(AuBlogHistory.LANGUAGE_MODEL_WEBPAGES, "");
        }
        if (values.containsKey(AuBlogHistory.EVOLVING_INFORMATION_STRUCTURE) == false) {
            values.put(AuBlogHistory.EVOLVING_INFORMATION_STRUCTURE, "");
        }
        if (values.containsKey(AuBlogHistory.PUBLISHED_IN) == false) {
            values.put(AuBlogHistory.PUBLISHED_IN, "");
        }       
       
        
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        // it seems suspicious to only be the content of PARENT_ENTRY, ah its the nullcolumnhack
        
        long rowId = db.insert(AuBlogHistoryDatabase.AUBLOG_HISTORY_TABLE_NAME, AuBlogHistory.PARENT_ENTRY, values);
        if (rowId > 0) {
            Uri resultUri = ContentUris.withAppendedId(AuBlogHistory.CONTENT_URI, rowId);
            getContext().getContentResolver().notifyChange(resultUri, null);
            return resultUri;
        }

        throw new SQLException("Failed to insert row into " + uri);
	}

	@Override
	public boolean onCreate() {
		mOpenHelper = new DatabaseHelper(getContext());
        return true;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
		SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        qb.setTables(AuBlogHistoryDatabase.AUBLOG_HISTORY_TABLE_NAME);

        switch (sUriMatcher.match(uri)) {
        case AUBLOGHISTORIES:
        	//gets a cursor of all rows, with all columns (all should be entered into the projectionmap)
            qb.setProjectionMap(sAuBlogHistoryProjectionMap);
            break;

        case AUBLOGHISTORY_ID:
            qb.setProjectionMap(sAuBlogHistoryProjectionMap);
            //get the row (of selected columns in projetion, it should be all of them) for that ID
            //gets a cursor of the row which matches the id from the uri
            qb.appendWhere(AuBlogHistory._ID + "=" + uri.getPathSegments().get(1));
            break;

        case LIVE_FOLDER_AUBLOGHISTORYS:
        	//gets a cursor of all rows, but with just a few columns
            qb.setProjectionMap(sLiveFolderProjectionMap);
            break;

        default:
            throw new IllegalArgumentException("Unknown URI " + uri);
        }

        // If no sort order is specified use the default
        String orderBy;
        if (TextUtils.isEmpty(sortOrder)) {
            orderBy = AuBlogHistory.DEFAULT_SORT_ORDER;
        } else {
            orderBy = sortOrder;
        }

        // Get the database and run the query
        SQLiteDatabase db = mOpenHelper.getReadableDatabase();
        Cursor c = qb.query(db, projection, selection, selectionArgs, null, null, orderBy);

        // Tell the cursor what uri to watch, so it knows when its source data changes
        c.setNotificationUri(getContext().getContentResolver(), uri);
        return c;
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection,
			String[] selectionArgs) {
		//TODO add modified set to current time in the values ?
		SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        int count;
        switch (sUriMatcher.match(uri)) {
        case AUBLOGHISTORIES:
            count = db.update(AuBlogHistoryDatabase.AUBLOG_HISTORY_TABLE_NAME, values, selection, selectionArgs);
            break;

        case AUBLOGHISTORY_ID:
            String audiobookId = uri.getPathSegments().get(1);
            //set last modified to now.
            Long now = Long.valueOf(System.currentTimeMillis());
			values.put(AuBlogHistory.LAST_MODIFIED, now);
			
            //update teh row using the values provided
            //this takes updates from the title editor
            //this takes updates from the audiobooks editor
            count = db.update(AuBlogHistoryDatabase.AUBLOG_HISTORY_TABLE_NAME, values, AuBlogHistory._ID + "=" + audiobookId
                    + (!TextUtils.isEmpty(selection) ? " AND (" + selection + ')' : ""), selectionArgs);
            break;

        default:
            throw new IllegalArgumentException("Unknown URI " + uri);
        }

        getContext().getContentResolver().notifyChange(uri, null);
        return count;
	}
	static {
	        sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
	        sUriMatcher.addURI(AuBlogHistoryDatabase.AUTHORITY, AuBlogHistoryDatabase.AUBLOG_HISTORY_TABLE_NAME, AUBLOGHISTORIES);
	        sUriMatcher.addURI(AuBlogHistoryDatabase.AUTHORITY, AuBlogHistoryDatabase.AUBLOG_HISTORY_TABLE_NAME+"/#", AUBLOGHISTORY_ID);
	        sUriMatcher.addURI(AuBlogHistoryDatabase.AUTHORITY, "live_folders/"+AuBlogHistoryDatabase.AUBLOG_HISTORY_TABLE_NAME, LIVE_FOLDER_AUBLOGHISTORYS);

	        sAuBlogHistoryProjectionMap = new HashMap<String, String>();
	        sAuBlogHistoryProjectionMap.put(AuBlogHistory._ID, AuBlogHistory._ID);
	        
	        sAuBlogHistoryProjectionMap.put(AuBlogHistory.ENTRY_TITLE, AuBlogHistory.ENTRY_TITLE);
	        sAuBlogHistoryProjectionMap.put(AuBlogHistory.ENTRY_CONTENT, AuBlogHistory.ENTRY_CONTENT);
	        sAuBlogHistoryProjectionMap.put(AuBlogHistory.ENTRY_LABELS, AuBlogHistory.ENTRY_LABELS);
	        sAuBlogHistoryProjectionMap.put(AuBlogHistory.PUBLISHED, AuBlogHistory.PUBLISHED);
	        sAuBlogHistoryProjectionMap.put(AuBlogHistory.PUBLISHED_IN, AuBlogHistory.PUBLISHED_IN);
	        sAuBlogHistoryProjectionMap.put(AuBlogHistory.PARENT_ENTRY, AuBlogHistory.PARENT_ENTRY);
	        sAuBlogHistoryProjectionMap.put(AuBlogHistory.DELETED, AuBlogHistory.DELETED);
	        sAuBlogHistoryProjectionMap.put(AuBlogHistory.LAST_MODIFIED, AuBlogHistory.LAST_MODIFIED);
	        sAuBlogHistoryProjectionMap.put(AuBlogHistory.TIME_CREATED, AuBlogHistory.TIME_CREATED);
	        sAuBlogHistoryProjectionMap.put(AuBlogHistory.TIME_EDITED, AuBlogHistory.TIME_EDITED);
	        sAuBlogHistoryProjectionMap.put(AuBlogHistory.AUDIO_FILE, AuBlogHistory.AUDIO_FILE);
	        sAuBlogHistoryProjectionMap.put(AuBlogHistory.AUDIO_FILE_STATUS, AuBlogHistory.AUDIO_FILE_STATUS);
	        sAuBlogHistoryProjectionMap.put(AuBlogHistory.TRANSCRIPTION_STATUS, AuBlogHistory.TRANSCRIPTION_STATUS);
	        sAuBlogHistoryProjectionMap.put(AuBlogHistory.TRANSCRIPTION_RESULT, AuBlogHistory.TRANSCRIPTION_RESULT);
	        sAuBlogHistoryProjectionMap.put(AuBlogHistory.LANGUAGE_MODEL_PDFS, AuBlogHistory.LANGUAGE_MODEL_PDFS);
	        sAuBlogHistoryProjectionMap.put(AuBlogHistory.LANGUAGE_MODEL_WEBPAGES, AuBlogHistory.LANGUAGE_MODEL_WEBPAGES);
	        sAuBlogHistoryProjectionMap.put(AuBlogHistory.EVOLVING_INFORMATION_STRUCTURE, AuBlogHistory.EVOLVING_INFORMATION_STRUCTURE);

	        // Support for Live Folders.
	        sLiveFolderProjectionMap = new HashMap<String, String>();
	        sLiveFolderProjectionMap.put(LiveFolders._ID, AuBlogHistory._ID + " AS " +
	                LiveFolders._ID);
	        sLiveFolderProjectionMap.put(LiveFolders.NAME, AuBlogHistory.ENTRY_TITLE + " AS " +
	                LiveFolders.NAME);
	        sLiveFolderProjectionMap.put(LiveFolders.DESCRIPTION, AuBlogHistory.ENTRY_CONTENT + " AS " +
	                LiveFolders.DESCRIPTION);
	        
	        // Add more columns here for more robust Live Folders.
	    }
	   
    /**
     * From Google IO 2010 app best practices
     */
    
    /**
     * This class helps open, create, and upgrade the database file.
     */
    private static class DatabaseHelper extends SQLiteOpenHelper {

        DatabaseHelper(Context context) {
        	super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }
        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL("CREATE TABLE " + AuBlogHistoryDatabase.AUBLOG_HISTORY_TABLE_NAME + " ("
            		+ AuBlogHistory._ID+ " INTEGER PRIMARY KEY AUTOINCREMENT,"
            		+ AuBlogHistory.PUBLISHED + " INTEGER,"
            		+ AuBlogHistory.ENTRY_CONTENT + " TEXT,"
            		+ AuBlogHistory.ENTRY_LABELS + " TEXT,"
            		+ AuBlogHistory.ENTRY_TITLE + " TEXT,"
            		+ AuBlogHistory.PARENT_ENTRY + " INTEGER,"
            		+ AuBlogHistory.DELETED   + " INTEGER,"
            		+ AuBlogHistory.PUBLISHED_IN + " TEXT,"
            		+ AuBlogHistory.AUDIO_FILE + " TEXT,"
            		+ AuBlogHistory.AUDIO_FILE_STATUS + " TEXT,"
            		+ AuBlogHistory.TRANSCRIPTION_STATUS + " TEXT,"
            		+ AuBlogHistory.TRANSCRIPTION_RESULT + " TEXT,"
            		+ AuBlogHistory.LANGUAGE_MODEL_PDFS + " TEXT,"
            		+ AuBlogHistory.LANGUAGE_MODEL_WEBPAGES + " TEXT,"
            		+ AuBlogHistory.EVOLVING_INFORMATION_STRUCTURE + " TEXT,"
            		+ AuBlogHistory.LAST_MODIFIED + " INTEGER,"
            		+ AuBlogHistory.TIME_CREATED + " INTEGER,"
            		+ AuBlogHistory.TIME_EDITED + " INTEGER"
            		+ ");");
        
            /*
             * Insert a root node that is null, 
             * There are two options for the parent of the root: 
             * 		1. have itself as its parent.. will this cause a problem later?
             *		2. maybe put 0 as the parent of the root, there by marking it as special. (chose this option)
             */
			ContentValues values = new ContentValues();
			Long now = Long.valueOf(System.currentTimeMillis());
			// the fields are all set by the insert, by default
			// Make sure that the fields are all set
            values.put(AuBlogHistory.TIME_CREATED, now);
            values.put(AuBlogHistory.LAST_MODIFIED, now);
            values.put(AuBlogHistory.TIME_EDITED, "0");
            values.put(AuBlogHistory.PUBLISHED, "0");
            values.put(AuBlogHistory.PARENT_ENTRY, "0");
            values.put(AuBlogHistory.DELETED, "0");
            values.put(AuBlogHistory.ENTRY_TITLE, "");
            values.put(AuBlogHistory.ENTRY_CONTENT, "");
            values.put(AuBlogHistory.ENTRY_LABELS, "");
            values.put(AuBlogHistory.AUDIO_FILE, "");
            values.put(AuBlogHistory.AUDIO_FILE_STATUS, "");
            values.put(AuBlogHistory.TRANSCRIPTION_STATUS, "");
            values.put(AuBlogHistory.TRANSCRIPTION_RESULT, "");
            values.put(AuBlogHistory.PUBLISHED_IN, "");
			// it seems suspicious to only be the content of PARENT_ENTRY, ah its the nullcolumnhack
			long saveRowId = db.insert(AuBlogHistoryDatabase.AUBLOG_HISTORY_TABLE_NAME, AuBlogHistory.PARENT_ENTRY, values);
			
			values.put(AuBlogHistory.ENTRY_TITLE, "Trash");//post 2 is the trash.
			db.insert(AuBlogHistoryDatabase.AUBLOG_HISTORY_TABLE_NAME, AuBlogHistory.PARENT_ENTRY, values);
			/*
			 * Branch for sample Entry About Me
			 * 			*
			 * 			|
			 * 			*
			 * 			|
			 * 			*
			 */
			values.put(AuBlogHistory.PARENT_ENTRY,saveRowId);
			values.put(AuBlogHistory.ENTRY_TITLE, "About me");
			values.put(AuBlogHistory.ENTRY_CONTENT, "Your blog's About Me page should not be overlooked. " +
					"It's an essential tool to establish who you are as a blogger and help readers understand " +
					"what your blog is about. Simply listing your name and contact information is not enough. " +
					"Sell yourself and your blog on your About Me page, and make readers believe you're not only " +
					"an expert in your blog's topic but your blog is also the place for people to find information " +
					"about your topic on the web. From <a href='http://weblogs.about.com/od/partsofablog/qt/AboutPage.htm'>About.com</a>");
			values.put(AuBlogHistory.ENTRY_LABELS, "about me");
			saveRowId = db.insert(AuBlogHistoryDatabase.AUBLOG_HISTORY_TABLE_NAME, AuBlogHistory.PARENT_ENTRY, values);
			
			values.put(AuBlogHistory.PARENT_ENTRY,saveRowId);
			values.put(AuBlogHistory.ENTRY_TITLE, "About my blog");
			values.put(AuBlogHistory.ENTRY_CONTENT, "<p>Your blog's About Me page should not be overlooked. " +
					"It's an essential tool to establish who you are as a blogger and help readers understand " +
					"what your blog is about. Simply listing your name and contact information is not enough. " +
					"Sell yourself and your blog on your About Me page, and make readers believe you're not only " +
					"an expert in your blog's topic but your blog is also the place for people to find information " +
					"about your topic on the web. From <a href='http://weblogs.about.com/od/partsofablog/qt/AboutPage.htm'>About.com</a></p>" +
					"Following are the three most important elements to include on your About Me page:" +
					"Your experience and why you're the best person to blog about your subject matter" +
					"Links to your other websites or blogs self promotion is critical to your success as a blogger" +
					"Your contact information so interested readers can ask questions or reach out to you for other " +
					"business opportunities (which happens often in the blogosphere)");
			values.put(AuBlogHistory.ENTRY_LABELS, "about me");
			Long terminalRowId = db.insert(AuBlogHistoryDatabase.AUBLOG_HISTORY_TABLE_NAME, AuBlogHistory.PARENT_ENTRY, values);
			
			/*
			 * Branch for sample job entry,
			 * with branching daughters
			 * 			 *
			 * 			 |
			 * 			 *
			 * 			/ \
			 * 		   *   *
			 */
			values.put(AuBlogHistory.PARENT_ENTRY,AuBlogHistoryDatabase.ROOT_ID_DEFAULT);
			values.put(AuBlogHistory.ENTRY_TITLE, "How to use the drafts tree");
			values.put(AuBlogHistory.ENTRY_CONTENT, "The drafts tree lets you visualize/listen to blog entries, " +
					"and re-draft or re-wind until you are ready to publish. Each time you hit the save button, " +
					"a new node is created. ");
			values.put(AuBlogHistory.ENTRY_LABELS, "howto");
			saveRowId = db.insert(AuBlogHistoryDatabase.AUBLOG_HISTORY_TABLE_NAME, AuBlogHistory.PARENT_ENTRY, values);
			
			values.put(AuBlogHistory.PARENT_ENTRY,saveRowId);
			values.put(AuBlogHistory.ENTRY_TITLE, "Use via recording...");
			values.put(AuBlogHistory.ENTRY_CONTENT, "You can brainstorm and build your blog entry by recording your voice, " +
					"and listening to it. Once you have a blog entry that you are satisfied with you can send it for transcription. " +
					"Then you can edit it in text form and publish it. Alternatively, you can use AuBlog as a traditional blog " +
					"client by just typing in the Edit Entry page.");
			values.put(AuBlogHistory.ENTRY_LABELS, "howto, record");
			terminalRowId = db.insert(AuBlogHistoryDatabase.AUBLOG_HISTORY_TABLE_NAME, AuBlogHistory.PARENT_ENTRY, values);
			
			values.put(AuBlogHistory.PARENT_ENTRY,saveRowId);
			values.put(AuBlogHistory.ENTRY_TITLE, "Use via typing...");
			values.put(AuBlogHistory.ENTRY_CONTENT, "You can also simply type your blog entry by clicking on the Edit button.");
			values.put(AuBlogHistory.ENTRY_LABELS, "howto, type");
			terminalRowId = db.insert(AuBlogHistoryDatabase.AUBLOG_HISTORY_TABLE_NAME, AuBlogHistory.PARENT_ENTRY, values);
			
        }
        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			/*
			 * export user database
			 * http://stackoverflow.com/questions/805363/how-do-i-rename-a-column-in-a-sqlite-database-table
			 * The SQLite ALTER TABLE documentation can be found here. If you
			 * add new columns you can use ALTER TABLE to insert them into a
			 * live table. If you rename or remove columns you can use ALTER
			 * TABLE to rename the old table, then create the new table and then
			 * populate the new table with the contents of the old table.
			 */
        	String copyTableToBackup = "ALTER TABLE "+AuBlogHistoryDatabase.AUBLOG_HISTORY_TABLE_NAME+" RENAME TO "+AuBlogHistoryDatabase.AUBLOG_HISTORY_TABLE_NAME + "backup1;";
        	db.execSQL(copyTableToBackup);
        	
        	Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
        			+ newVersion + ", will try to copy all old data");
        	db.execSQL("DROP TABLE IF EXISTS "+ AuBlogHistoryDatabase.AUBLOG_HISTORY_TABLE_NAME);
        	onCreate(db);
        	
        	//delete data
        	String clearSampleData = "DELETE FROM "+AuBlogHistoryDatabase.AUBLOG_HISTORY_TABLE_NAME+";";
        	try{
        		db.execSQL(clearSampleData);
        	}catch (Exception e) {
				// TODO: handle exception
        		Log.w(TAG, "Problem upgrading, unable to clear sample data."+e);
			}
        	String performUpgrade = "";
        	/*
        	 * Prior to version 3, the Audio_file column was called Audio_files
        	 */
        	if (oldVersion < DATABASE_CHANGED_COLUMN_NAMES){
        		performUpgrade = 
        			"INSERT INTO "+ AuBlogHistoryDatabase.AUBLOG_HISTORY_TABLE_NAME
        			+"("+AuBlogHistory._ID
        			+", "+AuBlogHistory.PUBLISHED
        			+", "+AuBlogHistory.ENTRY_CONTENT
        			+", "+AuBlogHistory.ENTRY_LABELS
        			+", "+AuBlogHistory.ENTRY_TITLE
        			+", "+AuBlogHistory.PARENT_ENTRY
        			+", "+AuBlogHistory.DELETED
        			+", "+AuBlogHistory.PUBLISHED_IN
        			+", "+AuBlogHistory.AUDIO_FILE
        			+", "+AuBlogHistory.LAST_MODIFIED
        			+", "+AuBlogHistory.TIME_CREATED
        			+", "+AuBlogHistory.TIME_EDITED
        			+") " +
        			"SELECT "+AuBlogHistory._ID
        			+", "+AuBlogHistory.PUBLISHED
        			+", "+AuBlogHistory.ENTRY_CONTENT
        			+", "+AuBlogHistory.ENTRY_LABELS
        			+", "+AuBlogHistory.ENTRY_TITLE
        			+", "+AuBlogHistory.PARENT_ENTRY
        			+", "+AuBlogHistory.DELETED
        			+", "+AuBlogHistory.PUBLISHED_IN
        			+", "+AuBlogHistory.AUDIO_FILES_DEPRECIATED
        			+", "+AuBlogHistory.LAST_MODIFIED
        			+", "+AuBlogHistory.TIME_CREATED
        			+", "+AuBlogHistory.TIME_EDITED
        			+" " +
        			"FROM "+ AuBlogHistoryDatabase.AUBLOG_HISTORY_TABLE_NAME+"backup1;";
        	}else if (oldVersion == 3){
        		/*
        		 * Version 3 and above the columns are not renamed but just new columns are added
        		 */
        		performUpgrade = 
        			"INSERT INTO "+ AuBlogHistoryDatabase.AUBLOG_HISTORY_TABLE_NAME
        			+"("+AuBlogHistory._ID
        			+", "+AuBlogHistory.PUBLISHED
        			+", "+AuBlogHistory.ENTRY_CONTENT
        			+", "+AuBlogHistory.ENTRY_LABELS
        			+", "+AuBlogHistory.ENTRY_TITLE
        			+", "+AuBlogHistory.PARENT_ENTRY
        			+", "+AuBlogHistory.DELETED
        			+", "+AuBlogHistory.PUBLISHED_IN
        			+", "+AuBlogHistory.AUDIO_FILE
        			+", "+AuBlogHistory.AUDIO_FILE_STATUS
        			+", "+AuBlogHistory.LAST_MODIFIED
        			+", "+AuBlogHistory.TIME_CREATED
        			+", "+AuBlogHistory.TIME_EDITED
        			+") " +
        			"SELECT "+AuBlogHistory._ID
        			+", "+AuBlogHistory.PUBLISHED
        			+", "+AuBlogHistory.ENTRY_CONTENT
        			+", "+AuBlogHistory.ENTRY_LABELS
        			+", "+AuBlogHistory.ENTRY_TITLE
        			+", "+AuBlogHistory.PARENT_ENTRY
        			+", "+AuBlogHistory.DELETED
        			+", "+AuBlogHistory.PUBLISHED_IN
        			+", "+AuBlogHistory.AUDIO_FILE
        			+", "+AuBlogHistory.AUDIO_FILE_STATUS
        			+", "+AuBlogHistory.LAST_MODIFIED
        			+", "+AuBlogHistory.TIME_CREATED
        			+", "+AuBlogHistory.TIME_EDITED
        			+" " +
        			"FROM "+ AuBlogHistoryDatabase.AUBLOG_HISTORY_TABLE_NAME+"backup1;";
        	}/* else if (oldVersion  == 5){
        	//look at create table version of db 5 to copy over all the columns of 5 to the new db
        	        	
        	}*/
        	
        	try{
	        	db.execSQL(performUpgrade);
			}catch (Exception e) {
				// TODO: handle exception
				Log.w(TAG, "Problem upgrading, unable to copy user data."+e);
			}
        	
        }
        /*
         * un used function to clear user's drafts, instead taking user to the manage application settings page. 
         */
        public void wipeUserData(SQLiteDatabase db) {
        	Log.w(TAG, "Deleting database, and inserting original sample entries, which will destroy all old data");
        	db.execSQL("DROP TABLE IF EXISTS "+ AuBlogHistoryDatabase.AUBLOG_HISTORY_TABLE_NAME);
        	onCreate(db);
        }
    }//end databasehelper

}
