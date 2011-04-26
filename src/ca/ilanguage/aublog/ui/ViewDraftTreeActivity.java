package ca.ilanguage.aublog.ui;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;


import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.database.Cursor;
import android.database.SQLException;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
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
import ca.ilanguage.aublog.db.AuBlogHistoryDatabase;
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
public class ViewDraftTreeActivity extends Activity {

    private static final String TAG = "CreateBlogEntryActivity";

	//uri of the entry being edited.
	private Uri mUri;
	private Cursor mCursor;
	//savedInstanceState
	
	private static final int GROUP_BASIC = 0;
	private static final int GROUP_FORMAT = 1;
	int selectionStart;
	int selectionEnd;
	String mPostContent ="";
	String mPostTitle ="";
	String mPostLabels ="";
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
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setContentView(R.layout.main_webview);
        mWebView = (WebView) findViewById(R.id.webview);
        mWebView.addJavascriptInterface(new JavaScriptInterface(this), "Android");
        WebSettings webSettings = mWebView.getSettings();
        webSettings.setSavePassword(false);
        webSettings.setSaveFormData(false);
        webSettings.setJavaScriptEnabled(true);
        webSettings.setSupportZoom(true);
        webSettings.setBuiltInZoomControls(true);
        
        
		mWebView.loadUrl("file:///android_asset/view_draft_tree.html");
    }
    public class JavaScriptInterface {
        Context mContext;

        /** Instantiate the interface and set the context */
        JavaScriptInterface(Context c) {
            mContext = c;
            
        }
        public void showToast(String toast) {
            Toast.makeText(mContext, toast, Toast.LENGTH_SHORT).show();
        }
        public void editId(String id){
        	Toast.makeText(mContext, "Editing post number "+id, Toast.LENGTH_SHORT).show();
        	Intent i = new Intent(getBaseContext(), EditBlogEntryActivity.class);
        	i.setData( AuBlogHistory.CONTENT_URI.buildUpon().appendPath(id).build() );
        	startActivity(i);
        }

	    public void deleteId(String id){
	    	Toast.makeText(mContext, "Are you sure you want to delete post number "+id, Toast.LENGTH_SHORT).show();
	    	Uri uri = AuBlogHistory.CONTENT_URI.buildUpon().appendPath(id).build();
			/*
			 * Flag entry as deleted
			 */
			ContentValues values = new ContentValues();
			values.put(AuBlogHistory.DELETED,"1");//sets deleted flag to true
			getContentResolver().update(uri, values,null, null);
//			getContentResolver().delete(uri, null, null);
			Toast.makeText(ViewDraftTreeActivity.this, "Will refresh here Post " +uri.getLastPathSegment()+" deleted.", Toast.LENGTH_LONG).show();
			refreshTree();
		}
	    public void refreshTree(){

	    	finish();
	    }
        
        
    }
    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
      // Save UI state changes to the savedInstanceState.
      // This bundle will be passed to onCreate if the process is
      // killed and restarted.
      super.onSaveInstanceState(savedInstanceState);
    }
    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
      super.onRestoreInstanceState(savedInstanceState);
      // Restore UI state from the savedInstanceState.
      // This bundle has also been passed to onCreate.

    }
    @Override
	protected void onPause() {
		super.onPause();
	}
	@Override
	protected void onDestroy() {
		super.onDestroy();

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