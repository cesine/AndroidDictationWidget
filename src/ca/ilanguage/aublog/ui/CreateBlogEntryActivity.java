package ca.ilanguage.aublog.ui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.SQLException;
import android.graphics.Color;
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
import ca.ilanguage.aublog.db.DBTextAdapter;
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

    private static final String LOG_TAG = "WebViewDemo";
	private DBTextAdapter mDbTextHelper;
	private static Cursor post = null;
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
        
        mDbTextHelper = new DBTextAdapter(this);
		try {
			mDbTextHelper.open();
		} catch (SQLException e) {
			// Log.e(TAG, "Database has not opened");
			Toast.makeText(CreateBlogEntryActivity.this, "Database connection problem "+e, Toast.LENGTH_LONG).show();
		}
		/*
		 * if there are unpublished posts in the database put them into the fields
		 */
		post = mDbTextHelper.fetchPostdById(1);
		startManagingCursor(post);
		if (post.getCount() != 0) {
			try {
				mPostTitle = (post.getString(post
						.getColumnIndexOrThrow(DBTextAdapter.KEY_TITLE)));
				mPostContent = (post.getString(post
						.getColumnIndexOrThrow(DBTextAdapter.KEY_CONTENT)));
			} catch (IllegalArgumentException e) {
				// Log.e(TAG, "IllegalArgumentException (DataBase failed)");
				Toast.makeText(CreateBlogEntryActivity.this, "Database connection problem "+e, Toast.LENGTH_LONG).show();
			} catch (Exception e) {
				// Log.e(TAG, "Exception (DataBase failed)");
				Toast.makeText(CreateBlogEntryActivity.this, "Database connection problem "+e, Toast.LENGTH_LONG).show();
			}
		}
		
		mWebView.loadUrl("file:///android_asset/create_blog_entry.html");
		/*
		 * Dont know what this does, gests the intent, and the bundle, and looks at subselections, or it puts the text into the content edit text..?
		 */
		Intent i = getIntent();
		if (i != null)
		{
			Bundle b = i.getExtras();
			if (b != null)
			{
				boolean isTextColor = b.getBoolean("isTextColor");
			    selectionStart = b.getInt("selStart");
				selectionEnd = b.getInt("selEnd");
				String startTag = "<span style='";
				startTag += (isTextColor) ? "color: " : "background: ";
				int color = b.getInt("color");
				startTag += "rgb(" + Color.red(color) + ", " + Color.green(color) + ", " + Color.blue(color) + ");'>";
				String endTag = "</span>";
				if (selectionStart >= 0 && selectionEnd >= 0) {
					String currentText = mPostContent;
					String selectedText = currentText.substring(selectionStart,
							selectionEnd);
					currentText = currentText.substring(0, selectionStart)
							+ startTag
							+ selectedText
							+ endTag
							+ currentText.substring(selectionEnd, currentText
									.length());
					mPostContent = currentText;
					Toast.makeText(CreateBlogEntryActivity.this, "Post content: "+mPostContent, Toast.LENGTH_LONG).show();
					int selPosition = selectionStart + startTag.length()
							+ selectedText.length();
//					postContent.requestFocus();
//					postContent.setSelection(selPosition);
				}
			}
		}
		
		
        
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
        	
			if (post.getCount() == 0) {
				try {
//					strTitle = mPostTitle;
//					strContent = mPostContent;
					mPostContent= strContent;
					mPostTitle=strTitle;
					mPostLabels=strLabels;
					mDbTextHelper.createPost(strTitle, strContent);
					mDbTextHelper.close();
					post.close();
					// Log.d(TAG, "Post saved to database.");
					Toast.makeText(CreateBlogEntryActivity.this, "Post saved to database\n\nTitle: "+strTitle+"\nLabels: "+strLabels+"\n\nPost: "+strContent, Toast.LENGTH_LONG).show();
				} catch (SQLException e) {
					// Log.e(TAG,"SQLException (createPost(title, content))");
					Toast.makeText(CreateBlogEntryActivity.this, "Database connection problem "+e, Toast.LENGTH_LONG).show();
				} catch (Exception e) {
					// Log.e(TAG, "Exception: " + e.getMessage());
					Toast.makeText(CreateBlogEntryActivity.this, "exception "+e, Toast.LENGTH_LONG).show();
				}
			} else {
				try {
					mPostContent= strContent;
					mPostTitle=strTitle;
					mPostLabels=strLabels;
					mDbTextHelper.updatePostById((long) 1,
							strTitle, strContent);
					mDbTextHelper.close();
					post.close();
					// Log.d(TAG, "Post updated in database.");
					Toast.makeText(CreateBlogEntryActivity.this, "Post updated to database\n\nTitle: "+strTitle+"\nLabels: "+strLabels+"\n\nPost: "+strContent, Toast.LENGTH_LONG).show();
				} catch (SQLException e) {
					// Log.e(TAG,"SQLException (updatePostById(rowId, title, content))");
					Toast.makeText(CreateBlogEntryActivity.this, "Database connection problem SQLException (updatePostById(rowId, title, content)) "+e, Toast.LENGTH_LONG).show();
				}
			}
//			Intent i = new Intent(CreateBlogEntryActivity.this,
//					MainMenuActivity.class);
//			startActivity(i);
			finish();
			//finishActivity(0);
        }
        public void publishPost(String strTitle, String strContent, String strLabels){
			mPostContent= strContent;
			mPostTitle=strTitle;
			mPostLabels=strLabels;
        	if ((mPostTitle.length() == 0)
					|| (mPostTitle == null)
					|| (mPostContent.length() == 0)
					|| (mPostContent == null)) {
				Alert.showAlert(CreateBlogEntryActivity.this,
						"Empty title or content",
						"Please fill all fields");
			} else {
				Intent i = new Intent(CreateBlogEntryActivity.this,
						PreviewAndPublish.class);
				
				if (post.getCount() == 0) {
					try {
						mPostContent= strContent;
						mPostTitle=strTitle;
						mPostLabels=strLabels;
						mDbTextHelper.createPost(strTitle,
								strContent);
						// Log.d(TAG, "Post saved to database.");
						Toast.makeText(CreateBlogEntryActivity.this, "Post saved to database\n\nTitle: "+strTitle+"\nLabels: "+strLabels+"\n\nPost: "+strContent, Toast.LENGTH_LONG).show();
					} catch (SQLException e) {
						// Log.e(TAG,"SQLException (createPost(title, content))");
						Toast.makeText(CreateBlogEntryActivity.this, "Database connection problem SQLException (updatePostById(rowId, title, content)) "+e, Toast.LENGTH_LONG).show();

					} catch (Exception e) {
						// Log.e(TAG, "Exception: " +
						// e.getMessage());
						Toast.makeText(CreateBlogEntryActivity.this, "Exception "+e, Toast.LENGTH_LONG).show();

					}
				} else {
					try {
						mPostContent= strContent;
						mPostTitle=strTitle;
						mPostLabels=strLabels;
						mDbTextHelper.updatePostById((long) 1,
								strTitle, strContent);
						// Log.d(TAG, "Post updated in database.");
						Toast.makeText(CreateBlogEntryActivity.this, "Post updated to database\n\nTitle: "+strTitle+"\nLabels: "+strLabels+"\n\nPost: "+strContent, Toast.LENGTH_LONG).show();
					} catch (SQLException e) {
						// Log.e(TAG,"SQLException (updatePostById(rowId, title, content))");
						Toast.makeText(CreateBlogEntryActivity.this, "Database connection problem SQLException (updatePostById(rowId, title, content)) "+e, Toast.LENGTH_LONG).show();

					}
				}
//				mDbTextHelper.close();
//				post.close();
				startActivity(i);
				finish();
			}
        }
    }
    @Override
	protected void onPause() {
		super.onPause();
		// Log.i(TAG, "Method 'onPause()' launched");
		
		//saveOrUpdateToDB();
		mWebView.loadUrl("javascript:savePostToDB()");
	}
	@Override
	protected void onDestroy() {
		super.onDestroy();
		// Log.i(TAG, "Method 'onDestroy()' launched");
//		tracker.stop();
		//saveOrUpdateToDB();
		mWebView.loadUrl("javascript:savePostToDB()");

	}
	private void saveOrUpdateToDB(){
		mDbTextHelper = new DBTextAdapter(this);
		try {
			mDbTextHelper.open();
		} catch (SQLException e) {
			// Log.e(TAG, "Database has not opened");
		}
		post = mDbTextHelper.fetchPostdById(1);
		startManagingCursor(post);

		String strTitle = "";
		String strContent = "";
		if (post.getCount() == 0) {
			try {
				strTitle = mPostTitle;
				strContent = mPostContent;
				mDbTextHelper.createPost(strTitle, strContent);
				mDbTextHelper.close();
				post.close();
				// Log.d(TAG, "Post saved to database.");
			} catch (SQLException e) {
				// Log.e(TAG, "SQLException (createPost(title, content))");
			} catch (Exception e) {
				// Log.e(TAG, "Exception: " + e.getMessage());
			}
		} else {
			try {
				strTitle = mPostTitle;
				strContent = mPostContent;
				mDbTextHelper.updatePostById((long) 1, strTitle, strContent);
				mDbTextHelper.close();
				post.close();
				// Log.d(TAG, "Post updated in database.");
			} catch (SQLException e) {
				// Log.e(TAG,"SQLException (updatePostById(rowId, title, content))");
			}
		}
	}
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			mWebView.loadUrl("javascript:savePostToDB()");
//			saveOrUpdateToDB();
//			Intent i = new Intent(CreateBlogEntryActivity.this, MainMenuActivity.class);
//			startActivity(i);
			finish();
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
            Log.d(LOG_TAG, message);
            result.confirm();
            return true;
        }
    }
}