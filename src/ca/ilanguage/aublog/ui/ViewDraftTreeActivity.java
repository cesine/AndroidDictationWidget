package ca.ilanguage.aublog.ui;

import java.io.File;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Window;
import android.webkit.JsResult;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.Toast;

import ca.ilanguage.aublog.R;
import ca.ilanguage.aublog.db.AuBlogHistoryDatabase.AuBlogHistory;

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
        
        //http://stackoverflow.com/questions/2465432/android-webview-completely-clear-the-cache
        /*
         * Errors in the javascript not loading the json. so trying to clear cache. 
         
        ViewDraftTreeActivity.this.deleteDatabase("webview.db");
        ViewDraftTreeActivity.this.deleteDatabase("webviewCache.db");
        */
        
        mWebView = (WebView) findViewById(R.id.webview);
        mWebView.addJavascriptInterface(new JavaScriptInterface(this), "Android");
        WebSettings webSettings = mWebView.getSettings();
        webSettings.setSavePassword(false);
        webSettings.setSaveFormData(false);
        webSettings.setJavaScriptEnabled(true);
        webSettings.setSupportZoom(true);
        webSettings.setBuiltInZoomControls(true);
        webSettings.setUserAgentString(
        	    webSettings.getUserAgentString() 
        	    + " "
        	    + getString(R.string.user_agent_suffix)
        	);
        
        
        /*
         * Add some debuging info
         */
        mWebView.setWebChromeClient(new WebChromeClient() {
          
          public boolean onJsAlert(WebView view, String url, String message, final android.webkit.JsResult result)  
          {  
              new AlertDialog.Builder(ViewDraftTreeActivity.this) 
              	  
                  .setTitle("Draft Tree")  
                  .setMessage(message)  
                  .setPositiveButton(android.R.string.ok,  
                          new AlertDialog.OnClickListener()  
                          {  
                              public void onClick(DialogInterface dialog, int which)  
                              {  
                                  result.confirm();  
                              }  
                          })  
                  .setCancelable(false)  
                  .create()  
                  .show();  
        
              return true;  
          };  
        });
        

        
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
        	finish();
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
	    	
	    	Intent i = new Intent(getBaseContext(), ViewDraftTreeActivity.class);
	    	startActivity(i);
	    	finish();
	    }
	    public void exportTree(){
//	    	Intent intent = new Intent();
//	    	intent.setAction(android.content.Intent.ACTION_VIEW);
//	    	intent.setDataAndType(Uri.fromFile(file), "text/*");
//	    	startActivity(intent); 
	    	
	    	File file = new File("/sdcard/Android/data/ca.ilanguage.aublog/files/json_only_draft_space_tree.js");

	    	Intent mailto = new Intent(Intent.ACTION_SEND); 
	        mailto.setType("message/rfc822") ; // use from live device
	        mailto.putExtra(Intent.EXTRA_EMAIL, new String[]{""});
	        mailto.putExtra(Intent.EXTRA_SUBJECT,"Backup of AuBlog Drafts");
	        mailto.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(file));
	        mailto.putExtra(Intent.EXTRA_TEXT,"Attached is a backup of the Blog drafts, exported in json format.");
	        startActivity(Intent.createChooser(mailto, "Select email application."));
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