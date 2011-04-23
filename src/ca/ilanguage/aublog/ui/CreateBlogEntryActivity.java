package ca.ilanguage.aublog.ui;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.SQLException;
import android.graphics.Color;
import android.net.Uri;
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
public class CreateBlogEntryActivity extends Activity {

    private static final String TAG = "CreateBlogEntryActivity";

	//uri of the entry being edited.
	private Uri mUri;
	private static Uri dataUriToTriggerNewBlogEntry=AuBlogHistory.CONTENT_URI;
	//savedInstanceState
	
	private static final int GROUP_BASIC = 0;
	private static final int GROUP_FORMAT = 1;
	int selectionStart;
	int selectionEnd;
	String mPostContent ="";
	String mPostTitle ="";
	String mPostLabels ="";
	
	private WebView mWebView;
    private Handler mHandler = new Handler();

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setContentView(R.layout.main_webview);
        mWebView = (WebView) findViewById(R.id.webview);
        mWebView.addJavascriptInterface(new JavaScriptInterface(this), "Android");
        WebSettings webSettings = mWebView.getSettings();
        webSettings.setSavePassword(false);
        webSettings.setSaveFormData(false);
        webSettings.setJavaScriptEnabled(true);
        
        mUri = getContentResolver().insert(dataUriToTriggerNewBlogEntry, null);
		// If we were unable to create a new blog entry, then just finish
        // this activity.  A RESULT_CANCELED will be sent back to the
        // original activity if they requested a result.
        if (mUri == null) {
            Log.e(TAG, "Failed to insert new audiobook into " + getIntent().getData());
            Toast.makeText(CreateBlogEntryActivity.this, "Failed to insert new audiobook into "+ getIntent().getData()+" with this uri"+dataUriToTriggerNewBlogEntry, Toast.LENGTH_LONG).show();
            finish();
            return;
        }
		
		/*
		 * if there are unpublished posts in the database put them into the fields
		 */
		mPostContent="";
		mPostLabels="";
		mPostTitle="";
		
		mWebView.loadUrl("file:///android_asset/create_blog_entry.html");
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
        
        public void savePost(String strTitle, String strContent, String strLabels){
        	mPostContent= strContent;
        	mPostTitle=strTitle;
        	mPostLabels=strLabels;
        	saveOrUpdateToDB();
        }
        public void publishPost(String strTitle, String strContent, String strLabels){
        	savePost(strTitle, strContent, strLabels);
        	if ((mPostTitle.length() == 0)
        			|| (mPostTitle == null)
        			|| (mPostContent.length() == 0)
        			|| (mPostContent == null)) {
        		Toast.makeText(CreateBlogEntryActivity.this, R.string.title_or_content_empty_error, Toast.LENGTH_LONG).show();
        	} else {
        		Intent i = new Intent(CreateBlogEntryActivity.this, PublishActivity.class);
        		//tell the i the mUri that is supposed to be published
        		i.setData(mUri);
        		startActivity(i);
        		finish();
        	}
        }
    }
    @Override
	protected void onPause() {
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
	private void saveOrUpdateToDB(){
    	ContentValues values = new ContentValues();
    	values.put(AuBlogHistory.ENTRY_TITLE, mPostTitle);
    	values.put(AuBlogHistory.ENTRY_CONTENT, mPostContent);
    	values.put(AuBlogHistory.ENTRY_LABELS, mPostLabels);
    	try{
    		getContentResolver().update(mUri, values,null, null);
    		Log.d(TAG, "Post saved to database.");
    		Toast.makeText(CreateBlogEntryActivity.this, "Post saved to database\n\nTitle: "+mPostTitle+"\nLabels: "+mPostLabels+"\n\nPost: "+mPostContent, Toast.LENGTH_LONG).show();
    	} catch (SQLException e) {
    		// Log.e(TAG,"SQLException (createPost(title, content))");
    		Toast.makeText(CreateBlogEntryActivity.this, "Database connection problem "+e, Toast.LENGTH_LONG).show();
    	} catch (Exception e) {
    		// Log.e(TAG, "Exception: " + e.getMessage());
    		Toast.makeText(CreateBlogEntryActivity.this, "exception "+e, Toast.LENGTH_LONG).show();
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