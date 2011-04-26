package ca.ilanguage.aublog.ui;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.database.Cursor;
import android.database.SQLException;
import android.graphics.Color;
import android.net.Uri;
import android.net.NetworkInfo.DetailedState;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.JsResult;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.Toast;

import ca.ilanguage.aublog.R;
import ca.ilanguage.aublog.db.AuBlogHistoryDatabase.AuBlogHistory;
import ca.ilanguage.aublog.db.DBTextAdapter;
import ca.ilanguage.aublog.db.AuBlogHistoryProvider;
import ca.ilanguage.aublog.util.Alert;
/**
 * Demonstrates how to embed a WebView in your activity. Also demonstrates how
 * to have javascript in the WebView call into the activity, and how the activity 
 * can invoke javascript.
 * <p>
 * In this example, clicking on the android in the WebView will result in a call into
 * the activities code in {@link DemoJavaScriptInterface#clickOnAndroid()}. This code
 * will turn around and invoke javascript using the {@link WebView#loadUrl(String)}
 * method.
 * <p>
 * Obviously all of this could have been accomplished without calling into the activity
 * and then back into javascript, but this code is intended to show how to set up the 
 * code paths for this sort of communication.
 *
 */
public class EditBlogEntryActivity extends Activity {

    private static final String TAG = "CreateBlogEntryActivity";

	//uri of the entry being edited.
	private Uri mUri;
	private Cursor mCursor;
	private Boolean mDeleted = false;
	//savedInstanceState
	
	private static final int GROUP_BASIC = 0;
	private static final int GROUP_FORMAT = 1;
	int selectionStart;
	int selectionEnd;
	String mPostContent ="";
	String mPostTitle ="";
	String mPostLabels ="";
	String mLongestEverContent ="";
	private static final String[] PROJECTION = new String[] {
		AuBlogHistory._ID, //0
		AuBlogHistory.ENTRY_TITLE, 
		AuBlogHistory.ENTRY_CONTENT, //2
		AuBlogHistory.ENTRY_LABELS,
		AuBlogHistory.PUBLISHED, //4
		AuBlogHistory.DELETED,
		AuBlogHistory.PARENT_ENTRY //6
	};
	
	private WebView mWebView;
    private Handler mHandler = new Handler();

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
      super.onConfigurationChanged(newConfig);
//      setContentView(R.layout.myLayout);
      Toast.makeText(EditBlogEntryActivity.this, "Configuration changed ", Toast.LENGTH_LONG).show();

    }
    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setContentView(R.layout.main_webview);
        mWebView = (WebView) findViewById(R.id.webview);
        mWebView.addJavascriptInterface(new JavaScriptInterface(this), "Android");
        WebSettings webSettings = mWebView.getSettings();
        webSettings.setSavePassword(false);
        webSettings.setSaveFormData(true);
        webSettings.setJavaScriptEnabled(true);
        
        /**
         * Get the uri which was sent to the CreateBlogActivity, put the data into the fields.
         */
        mUri = getIntent().getData();
        mCursor = managedQuery(mUri, PROJECTION, null, null, null);
		if (mCursor != null) {
			// Requery in case something changed while paused (such as the title)
			mCursor.requery();
            // Make sure we are at the one and only row in the cursor.
            mCursor.moveToFirst();
			try {
				mPostTitle = mCursor.getString(1);
				mPostContent = mCursor.getString(2);
				mPostLabels =mCursor.getString(3);
                String nodeAsString="id:"+mCursor.getString(0)+":\ntitle:"+mCursor.getString(1)+":\ncontent:"+mCursor.getString(2)+":\nlabels:"+mCursor.getString(3)+":\npublished:"+mCursor.getString(4)+":\ndeleted:"+mCursor.getString(5)+":\nparent:"+mCursor.getString(6)+":";
                Toast.makeText(EditBlogEntryActivity.this, "Full post info:"+nodeAsString, Toast.LENGTH_LONG).show();

			} catch (IllegalArgumentException e) {
				// Log.e(TAG, "IllegalArgumentException (DataBase failed)");
				Toast.makeText(EditBlogEntryActivity.this, "Retrieval from DB failed with an illegal argument exception "+e, Toast.LENGTH_LONG).show();
			} catch (Exception e) {
				// Log.e(TAG, "Exception (DataBase failed)");
				Toast.makeText(EditBlogEntryActivity.this, "The cursor returned is "+e, Toast.LENGTH_LONG).show();
			}
		}else{
			//this should never be executed
			mPostContent="";
			mPostLabels="";
			mPostTitle="";
		}
		mWebView.loadUrl("file:///android_asset/edit_blog_entry_wysiwyg.html");
    }
    public class JavaScriptInterface {
        Context mContext;

        /** Instantiate the interface and set the context */
        JavaScriptInterface(Context c) {
            mContext = c;
        }

        /** Show a toast from the web page 
         * 
         * the buttons onclick calls a javascript function to call the android one
         * 
         * function showAndroidToast(toast) {
        	//Android.showToast(toast);
    		Android.showToast(toast);
    	}
    
         * call javascript function
         * 
         * showAndroidToast(document.getElementById('markItUp').value)
         * 
         * 
         * */
        public void showToast(String toast) {
            Toast.makeText(mContext, toast, Toast.LENGTH_SHORT).show();
        }
        
        public String fetchPostContent(){
        	return mPostContent;
        }
        public String fetchPostTitle(){
        	return mPostTitle;
        }
        public String fetchPostLabels(){
        	return mPostLabels;
        }
        public void saveState(String strTitle, String strContent, String strLabels){
        	if(mDeleted = true){
        		return;
        	}
        	mPostContent= strContent;
        	mPostTitle=strTitle;
        	mPostLabels=strLabels;
        	if (mLongestEverContent.length() < (mPostTitle+mPostContent+mPostLabels).length() ){
    			mLongestEverContent=mPostTitle+mPostContent+mPostLabels;
    		}
        }
        public void savePost(String strTitle, String strContent, String strLabels){
//        	mPostContent= strContent;
//        	mPostTitle=strTitle;
//        	mPostLabels=strLabels;
        	saveState(strTitle, strContent, strLabels);
        	saveAsDaughterToDB();
    		Toast.makeText(EditBlogEntryActivity.this, "Saved \n\""+mPostTitle+"\"", Toast.LENGTH_LONG).show();

        }
        public void deletePost(String strTitle, String strContent, String strLabels){
//        	saveState(strTitle, strContent, strLabels);
        	deleteEntry(mUri);
        	finish();
        }
        public void publishPost(String strTitle, String strContent, String strLabels){
        	//automaticallly saved using onPause...
//        	savePost(strTitle, strContent, strLabels);
        	if ((mPostTitle.length() == 0)
        			|| (mPostTitle == null)
        			|| (mPostContent.length() == 0)
        			|| (mPostContent == null)) {
        		Toast.makeText(EditBlogEntryActivity.this, R.string.title_or_content_empty_error, Toast.LENGTH_LONG).show();
        	} else {
        		Intent i = new Intent(EditBlogEntryActivity.this, PublishActivity.class);
        		//tell the i the mUri that is supposed to be published
        		i.setData(mUri);
        		startActivity(i);
//        		finish();
        	}
        }
    }
    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
      // Save UI state changes to the savedInstanceState.
      // This bundle will be passed to onCreate if the process is
      // killed and restarted.
    	mWebView.loadUrl("javascript:savePostToState()");
	      savedInstanceState.putString("title", mPostTitle);
	      savedInstanceState.putString("content", mPostContent);
	      savedInstanceState.putString("labels", mPostLabels);
	      savedInstanceState.putString("longestcontentever", mLongestEverContent);
	      savedInstanceState.putBoolean("deleted", mDeleted);
//	      savedInstanceState.putString("uri", mUri.getPath());
      
      // etc.
      super.onSaveInstanceState(savedInstanceState);
    }
    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
      super.onRestoreInstanceState(savedInstanceState);
      // Restore UI state from the savedInstanceState.
      // This bundle has also been passed to onCreate.
      mPostTitle = savedInstanceState.getString("title");
      mPostContent = savedInstanceState.getString("content");
      mPostLabels = savedInstanceState.getString("labels");
      mLongestEverContent = savedInstanceState.getString("longestcontentever");
      mDeleted = savedInstanceState.getBoolean("deleted");
//      mUri = new Uri(savedInstanceState.getString("uri"));
    }
    @Override
	protected void onPause() {
    	mWebView.loadUrl("javascript:savePostToState()");
    	/*
    	 * un-user-initiated saves do not create a new node in the draft tree (although, this can be changed
    	 * by just calling saveAsDaugher here)
    	 */
    	saveAsSelfToDB();
		super.onPause();
		
//		saveOrUpdateToDB();
//		mWebView.loadUrl("javascript:savePostToDB()");
	}
	@Override
	protected void onDestroy() {
		super.onDestroy();
		// Log.i(TAG, "Method 'onDestroy()' launched");
//		tracker.stop();
		//saveOrUpdateToDB();
//		mWebView.loadUrl("javascript:savePostToDB()");

	}
	private void saveAsSelfToDB(){
		if (mDeleted ==true){
			return ;
		}
    	try{
    		if (mLongestEverContent.length() < (mPostTitle+mPostContent+mPostLabels).length() ){
    			mLongestEverContent=mPostTitle+mPostContent+mPostLabels;
    		}
    		if ( mLongestEverContent.length() <=3 ){ 
    			//delete the entry the blog entry is completely empty, or if the user never anything. this should prevent having empty entrys in the database, but keep entries that are zeroed out and had content before
    			deleteEntry(mUri);
    		}else{
	    		ContentValues values = new ContentValues();
	        	values.put(AuBlogHistory.ENTRY_TITLE, mPostTitle);
	        	values.put(AuBlogHistory.ENTRY_CONTENT, mPostContent);
	        	values.put(AuBlogHistory.ENTRY_LABELS, mPostLabels);
//	        	values.put(AuBlogHistory.USER_TOUCHED, "true"); TODO maybe make a field to indicate that the user never touched the entry, that way wont loose branches in the tree? 
	    		getContentResolver().update(mUri, values,null, null);
	    		Log.d(TAG, "Post saved to database.");
	    		Toast.makeText(EditBlogEntryActivity.this, "Post " +mUri.getLastPathSegment()+" saved as self to database\n\nTitle: "+mPostTitle+"\nLabels: "+mPostLabels+"\n\nPost: "+mPostContent, Toast.LENGTH_LONG).show();
    		}
    		    	} catch (SQLException e) {
    		// Log.e(TAG,"SQLException (createPost(title, content))");
    		Toast.makeText(EditBlogEntryActivity.this, "Database connection problem "+e, Toast.LENGTH_LONG).show();
    	} catch (Exception e) {
    		// Log.e(TAG, "Exception: " + e.getMessage());
    		Toast.makeText(EditBlogEntryActivity.this, "exception "+e, Toast.LENGTH_LONG).show();
    	}
	}
	public void deleteEntry(Uri uri){
    	mDeleted = true;
    	Cursor nodeCursor = managedQuery(uri, PROJECTION, null, null, null);
    	nodeCursor.requery();
        // Make sure we are at the one and only row in the cursor.
    	nodeCursor.moveToFirst();
    	String grandParentId=nodeCursor.getString(6);
		/*
		 * find out the node's children, and set their parent id to the parent of this node.
		 */
		Cursor cursor = managedQuery(AuBlogHistory.CONTENT_URI, PROJECTION, AuBlogHistory.PARENT_ENTRY +"="+uri.getLastPathSegment(), null, null);
		if ((cursor != null) ) {
			// Requery in case something changed while paused (such as the title)
			cursor.requery();
            // Make sure we are at the one and only row in the cursor.
            cursor.moveToFirst();
            while (cursor.isAfterLast() == false){
            	String daughterId = cursor.getString(0);
            	ContentValues daughterValues = new ContentValues();
            	daughterValues.put(AuBlogHistory.PARENT_ENTRY, grandParentId);
            	getContentResolver().update(AuBlogHistory.CONTENT_URI.buildUpon().appendPath(daughterId).build(), daughterValues, null, null);
            	cursor.moveToNext();
            }
            //firstChild=true;
//            cursor.deactivate();
    	}
		
		/*
		 * Flag entry as deleted
		 */
		ContentValues values = new ContentValues();
		values.put(AuBlogHistory.DELETED,"1");//sets deleted flag to true
		getContentResolver().update(mUri, values,null, null);
//		getContentResolver().delete(uri, null, null);
		Toast.makeText(EditBlogEntryActivity.this, "Post " +uri.getLastPathSegment()+" deleted.", Toast.LENGTH_LONG).show();
		finish();
	}

	private void saveAsDaughterToDB(){
    	try{
    		/*
    		 * Create daughter
    		 */
        	ContentValues daughterValues = new ContentValues();
        	daughterValues.put(AuBlogHistory.ENTRY_TITLE, mPostTitle);
        	daughterValues.put(AuBlogHistory.ENTRY_CONTENT, mPostContent);
        	daughterValues.put(AuBlogHistory.ENTRY_LABELS, mPostLabels);
        	if ( (mPostTitle+mPostContent+mPostLabels).length() <= 0 ){
        		//if the user blanked out the blog entry, probably they are restarting from scratch so set the parent to zero node
        		daughterValues.put(AuBlogHistory.PARENT_ENTRY, 0);
    		}else{
    			daughterValues.put(AuBlogHistory.PARENT_ENTRY, mUri.getLastPathSegment());
    		}
    		Uri daughterUri = getContentResolver().insert(AuBlogHistory.CONTENT_URI, daughterValues);
    		/*
    		 * Save parent but just tell it has a daughter, dont put the new values into its entry.
    		 * It should stay the way it was last saved when the user pushed the save button.
    		 */
//    		ContentValues parentValues = new ContentValues();
//    		parentValues.put(AuBlogHistory.DELETED,"true");
//    		getContentResolver().update(mUri, parentValues,null, null);
    		/*
    		 * Set the daughter to the active mUri
    		 */
    		Toast.makeText(EditBlogEntryActivity.this, "Post "+daughterUri.getLastPathSegment()+" saved as daugher of: " +mUri.getLastPathSegment()+" to database\n\nTitle: "+mPostTitle+"\nLabels: "+mPostLabels+"\n\nPost: "+mPostContent, Toast.LENGTH_LONG).show();
    		mUri=daughterUri;
    		getIntent().setData(mUri);
    		Log.d(TAG, "Post saved to database.");
    		    	} catch (SQLException e) {
    		// Log.e(TAG,"SQLException (createPost(title, content))");
    		Toast.makeText(EditBlogEntryActivity.this, "Database connection problem "+e, Toast.LENGTH_LONG).show();
    	} catch (Exception e) {
    		// Log.e(TAG, "Exception: " + e.getMessage());
    		Toast.makeText(EditBlogEntryActivity.this, "exception "+e, Toast.LENGTH_LONG).show();
    	}
		
	}
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
//			mWebView.loadUrl("javascript:savePostToDB()");
		}
//		if (keyCode == KeyEvent.KEYCODE_MENU) {
//			int tmp1 = 0, tmp2 = 0;
//			tmp1 = postContent.getSelectionStart();
//			tmp2 = postContent.getSelectionEnd();
//			selectionStart = Math.min(tmp1, tmp2);
//			selectionEnd = Math.max(tmp1, tmp2);
//		}
		return super.onKeyDown(keyCode, event);
	}
    
    /**
     * Provides a hook for calling "alert" from javascript. Useful for
     * debugging your javascript.
     */
    final class MyWebChromeClient extends WebChromeClient {
        @Override
        public boolean onJsAlert(WebView view, String url, String message, JsResult result) {
            Log.d(TAG, message);
            result.confirm();
            return true;
        }
    }
}