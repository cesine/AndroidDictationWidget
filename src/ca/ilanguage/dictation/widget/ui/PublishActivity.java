package ca.ilanguage.dictation.widget.ui;

import java.io.IOException;
import java.sql.Date;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.DialogInterface.OnDismissListener;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.Spannable; //import android.util.Log;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.apps.analytics.GoogleAnalyticsTracker;
import com.google.gdata.util.ServiceException;



import ca.ilanguage.aublog.R;
import ca.ilanguage.dictation.widget.db.BlogEntry;
import ca.ilanguage.dictation.widget.db.AuBlogHistoryDatabase.AuBlogHistory;
import ca.ilanguage.dictation.widget.preferences.NonPublicConstants;
import ca.ilanguage.dictation.widget.preferences.PreferenceConstants;
import ca.ilanguage.dictation.widget.preferences.SetPreferencesActivity;
import ca.ilanguage.dictation.widget.service.*;
import ca.ilanguage.dictation.widget.util.Alert;
import ca.ilanguage.dictation.widget.util.SpannableBufferHelper;

@Deprecated
public class PublishActivity extends Activity  {
	GoogleAnalyticsTracker tracker;
	private String mAuBlogInstallId;
	// private static final String TAG = "PreviewAndPublish";
	private String mTitle;
	private String mContent;
	private String mLabels;
	private BlogEntry myEntry = null;
	private final String MSG_KEY = "value";
	int publishStatus = 0;
	private ProgressDialog publishProgress = null;
	private int attempt = 0;
	private String mBloggerAccount;
	private String mBloggerPassword;
	private static final int BLOGGER_ACCOUNT_ENTERED = 0;
	
	//	private DBTextAdapter mDbTextHelper;
	private static Cursor mCursor = null;
	private Uri mUri;
	private static final String[] PROJECTION = new String[] {
		AuBlogHistory.ENTRY_TITLE, 
		AuBlogHistory.ENTRY_CONTENT,
		AuBlogHistory.ENTRY_LABELS,
		AuBlogHistory.PUBLISHED,
		AuBlogHistory.PUBLISHED_IN
	};
	@Override
	  protected void onDestroy() {
	    super.onDestroy();
	    // Stop the tracker when it is no longer needed.
	    tracker.stop();
	  }
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.transparent_activity);
		
		tracker = GoogleAnalyticsTracker.getInstance();
	    // Start the tracker in 20 sec interval dispatch mode...
	    tracker.start(NonPublicConstants.NONPUBLIC_GOOGLE_ANALYTICS_UA_ACCOUNT_CODE, 20, this);
	
		SharedPreferences prefs = getSharedPreferences(PreferenceConstants.PREFERENCE_NAME, MODE_PRIVATE);
		/*
		 * set the installid for appending to the labels
		 */
		mAuBlogInstallId = prefs.getString(PreferenceConstants.AUBLOG_INSTALL_ID, "0");
		/*
		 * get blogger infomation out of the preferences
		 */
		mBloggerAccount = prefs.getString(PreferenceConstants.PREFERENCE_ACCOUNT, "see settings");
		mBloggerPassword = prefs.getString(PreferenceConstants.PREFERENCE_PASSWORD, "see settings");
		Toast.makeText(PublishActivity.this, "Publishing to: "+mBloggerAccount, Toast.LENGTH_LONG).show();
		
		if( (!mBloggerAccount.contains("@") ) || mBloggerPassword.length()<4 ){
			tracker.trackEvent(
					mAuBlogInstallId,  // Category
		            "Publish missing info",  // Action
		            "displayed Toast: Taking you to the settings to add a Blogger account.: "+System.currentTimeMillis() +" : "+mAuBlogInstallId, // Label
		            (int)System.currentTimeMillis());       // Value
			
			//Toast.makeText(PublishActivity.this, "Taking you to the settings to add a Blogger account.", Toast.LENGTH_LONG).show();
			//Intent i = new Intent(PublishActivity.this, SetPreferencesActivity.class);
    		//startActivityForResult(i, BLOGGER_ACCOUNT_ENTERED);
			//startActivity(i);
		}
		
		/*
		 * Get the data out of the database for the relevent blog post taht this activity was called on, display the information to the user.
		 */
		mUri = getIntent().getData();
//		Toast tellUser = Toast.makeText(this, 
//        		"The data in the uri is: \n"+mUri.toString(), Toast.LENGTH_LONG);
//        tellUser.show();
        mCursor = managedQuery(mUri, PROJECTION, null, null, null);
		if (mCursor != null) {
			// Requery in case something changed while paused (such as the title)
			mCursor.requery();
            // Make sure we are at the one and only row in the cursor.
            mCursor.moveToFirst();
			try {
				mTitle = mCursor.getString(0);
				mContent = mCursor.getString(1);
			} catch (IllegalArgumentException e) {
				// Log.e(TAG, "IllegalArgumentException (DataBase failed)");
				tracker.trackEvent(
			            "Database",  // Category
			            "Bug",  // Action
			            "Retrieval from DB failed with an illegal argument exception "+e+" : "+mAuBlogInstallId, // Label
			            601);       // Value
				Toast.makeText(PublishActivity.this, "Retrieval from DB failed with an illegal argument exception "+e, Toast.LENGTH_LONG).show();
			} catch (Exception e) {
				tracker.trackEvent(
			            "Database",  // Category
			            "Bug",  // Action
			            "The cursor returned is "+e+" : "+mAuBlogInstallId, // Label
			            602);       // Value
				// Log.e(TAG, "Exception (DataBase failed)");
				Toast.makeText(PublishActivity.this, "The cursor returned is "+e, Toast.LENGTH_LONG).show();
			}
			myEntry = new BlogEntry();
			myEntry.setBlogEntry(mContent+"<p>"+prefs.getString(PreferenceConstants.PREFERENCE_BLOG_SIGNATURE, "")+"</p>");
			myEntry.setTitle(mTitle);
			myEntry.setLabels(mLabels);
			/*
			 * DONE add and publish labels
			 */
			myEntry.setCreated(new Date(System.currentTimeMillis()));
			publishBlogEntry();
		}else{
			Toast.makeText(PublishActivity.this, "There was a problem retriveing the data. not publishing... "+myEntry.toString(), Toast.LENGTH_LONG).show();
		}
		
	}
	private void publishBlogEntry() {
		final Activity thread_parent = this;
		publishProgress = ProgressDialog.show(this, "Publishing blog entry",
		"Starting to publish blog entry...");
		tracker.trackEvent(
				mAuBlogInstallId,  // Category
	            "Publish started",  // Action
	            "starting to publish blog entry "+myEntry.toString()+" : "+System.currentTimeMillis() +" : "+mAuBlogInstallId, // Label
	            (int)System.currentTimeMillis());       // Value
		
		Thread publish = new Thread() {
			@SuppressWarnings("static-access")
			public void run() {
				Bundle status = new Bundle();
				Looper loop = mHandler.getLooper();
				loop.prepare();
				Message statusMsg = mHandler.obtainMessage();
				publishStatus = 0;
				status.putString(MSG_KEY, "1");
				statusMsg.setData(status);
				mHandler.sendMessage(statusMsg);
				boolean publishOk = false;
				BlogConfigBLOGGER.BlogInterfaceType typeEnum = BlogConfigBLOGGER
				.getInterfaceTypeByNumber(1);
				BlogInterface blogapi = null;
				blogapi = BlogInterfaceFactory.getInstance(typeEnum);
				// Log.d(TAG, "Using interface type: " + typeEnum);
				blogapi.setInstanceConfig("");
				status.putString(MSG_KEY, "2");
				statusMsg = mHandler.obtainMessage();
				statusMsg.setData(status);
				mHandler.sendMessage(statusMsg);
				String auth_id = null;
				boolean authFlag = false;
				attempt = 0;
				while ((attempt <= AuBlog.AMOUNTOFATTEMPTS)
						&& (!authFlag)) {
					try {
						auth_id = blogapi.getAuthId(mBloggerAccount,mBloggerPassword);
						authFlag = true;
						attempt = 0;
					} catch (com.google.gdata.util.AuthenticationException e) {
						attempt++;
						// Log.e(TAG, "AuthenticationException " +
						// e.getMessage());

						tracker.trackEvent(
								mAuBlogInstallId,  // Category
					            "Publish error",  // Action
					            "AuthenticationException " +e.getMessage()+" : "+System.currentTimeMillis() +" : "+mAuBlogInstallId, // Label
					            (int)System.currentTimeMillis());       // Value
						
					}  catch (Exception e) {
						// Log.e(TAG, "Exception: " + e.getMessage());
						Toast.makeText(PublishActivity.this, "Internet connection failed, please check your Wireless and network settings.", Toast.LENGTH_LONG).show();
						//stop the thread and go back to publish activity
						tracker.trackEvent(
								mAuBlogInstallId,  // Category
					            "Publish error",  // Action
					            "General exception, maybe internet " +e.getMessage()+" : "+System.currentTimeMillis() +" : "+mAuBlogInstallId, // Label
					            (int)System.currentTimeMillis());       // Value
						
						finish();
					}

				}
				publishStatus = 1;
				Toast.makeText(PublishActivity.this, "Got auth token:" + auth_id, Toast.LENGTH_LONG).show();
				// Log.d(TAG, "Got auth token:" + auth_id);
				publishStatus = 2;
				if (auth_id != null) {
					status.putString(MSG_KEY, "3");
					statusMsg = mHandler.obtainMessage();
					statusMsg.setData(status);
					mHandler.sendMessage(statusMsg);
					String postUri = null;
					authFlag = false;
					attempt = 0;
					while ((attempt <= AuBlog.AMOUNTOFATTEMPTS)
							&& (!authFlag)) {
						try {
							postUri = blogapi.getPostUrl();
							authFlag = true;
							attempt = 0;
						} catch (ServiceException e) {
							// Log.e(TAG, "ServiceException " + e.getMessage());
							attempt++;
						} catch (IOException e) {
							// Log.e(TAG, "IOException " + e.getMessage());
							attempt++;
						} catch (Exception e) {
							// Log.e(TAG, "Exception: " + e.getMessage());
							Toast.makeText(PublishActivity.this, "Internet connection failed, please check your Wireless and network settings.", Toast.LENGTH_LONG).show();
							tracker.trackEvent(
									mAuBlogInstallId,  // Category
						            "Publish error",  // Action
						            "General exception, maybe internet " +e.getMessage()+" : "+System.currentTimeMillis() +" : "+mAuBlogInstallId, // Label
						            (int)System.currentTimeMillis());       // Value
							
							finish();
						}
					}
					SpannableBufferHelper helper = new SpannableBufferHelper();
					CharSequence cs = myEntry.getBlogEntry();
					EditText et = new EditText(thread_parent);
					et.setText(cs);
					Spannable spa = et.getText();
					spa.setSpan(cs, 0, 1, 1);
					String entry = helper.SpannableToXHTML(spa);
					status.putString(MSG_KEY, "4");
					statusMsg = mHandler.obtainMessage();
					statusMsg.setData(status);
					mHandler.sendMessage(statusMsg);
					authFlag = false;
					attempt = 0;
					
					/*TODO clean from here
					 * 
					 */
					while ((attempt <= AuBlog.AMOUNTOFATTEMPTS)
							&& (!authFlag)) {
						try {
							
							publishOk = blogapi
							.createPost(
									thread_parent,
									auth_id,
									postUri,
									null,
									myEntry.getTitle(),
									null,
									entry,
									mBloggerAccount,
											mBloggerPassword,
													myEntry.isDraft());
							
							authFlag = true;
							attempt = 0;
						} catch (ServiceException e) {
							// Log.e(TAG, "ServiceException: " +
							// e.getMessage());
							attempt++;
						} catch (Exception e) {
							// Log.e(TAG, "Exception: " + e.getMessage());
							Toast.makeText(PublishActivity.this, "Internet connection failed, please check your Wireless and network settings.", Toast.LENGTH_LONG).show();
							//finish thread?
							tracker.trackEvent(
									mAuBlogInstallId,  // Category
						            "Publish error",  // Action
						            "General exception, maybe internet " +e.getMessage()+" : "+System.currentTimeMillis() +" : "+mAuBlogInstallId, // Label
						            (int)System.currentTimeMillis());       // Value
							
							finish();
						}
					}
				} else {
					publishStatus = 3;
					
				}
				status.putString(MSG_KEY, "5");
				statusMsg = mHandler.obtainMessage();
				statusMsg.setData(status);
				mHandler.sendMessage(statusMsg);
				if (publishOk) {
					// Log.d(TAG, "Post published successfully!");
					publishStatus = 5;
				} else {
					// Log.d(TAG, "Publishing of the post failed!");
					publishStatus = 4;
				}
				mHandler.post(mPublishResults);
			}
		};
		publish.start();
		publishProgress.setMessage("Publishing in progress...");
	}

	final Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			Bundle content = msg.getData();
			String progressId = content.getString(MSG_KEY);
			if (progressId != null) {
				if (progressId.equals("1")) {
					publishProgress.setMessage("Preparing blog config...");
				} else if (progressId.equals("2")) {
					publishProgress.setMessage("Authenticating...");
				} else if (progressId.equals("3")) {
					publishProgress.setMessage("Contacting server...");
				} else if (progressId.equals("4")) {
					publishProgress.setMessage("Creating new entry...");
				} else if (progressId.equals("5")) {
					publishProgress.setMessage("Done...");
				}
			}
		}
	};

	final Runnable mPublishResults = new Runnable() {
		public void run() {
			showPublishedStatus();
		}
	};
	private void showPublishedStatus() {
		publishProgress.dismiss();
		if (publishStatus == 5) {
			/*
			 * Update mUri to have a published flag
			 */

			tracker.trackEvent(
					mAuBlogInstallId,  // Category
		            "Publish completed",  // Action
		            "Successfully published blog entry "+myEntry.toString()+" : "+System.currentTimeMillis() +" : "+mAuBlogInstallId, // Label
		            (int)System.currentTimeMillis());       // Value
			
			final Dialog dlg = new AlertDialog.Builder(PublishActivity.this)
			//					.setIcon(ca.ilanguage.dictation.widget.R.drawable.ic_dialog_alert)
			.setTitle("Publish status").setPositiveButton("OK", null)
			.setMessage("Published").create();
			dlg.setOnDismissListener(new OnDismissListener() {
				
				public void onDismiss(DialogInterface dialog) {
					/*
					 * Dont launch the main activity again, just finish it will return to main automatically
					 */
//					Intent i = new Intent(PublishActivity.this,
//							MainMenuActivity.class);
//					startActivity(i);
					finish();
				}
			});
			dlg.show();
		} else {
			attempt = 0;

			tracker.trackEvent(
					mAuBlogInstallId,  // Category
		            "Publish error",  // Action
		            "Failedto publish blog entry "+myEntry.toString()+" Error code "+ publishStatus+" : "+System.currentTimeMillis() +" : "+mAuBlogInstallId, // Label
		            (int)System.currentTimeMillis());       // Value
			
			Alert.showAlert(this, "Publishing failed, your Blogger account and/or password may be incorrectly entered in the Settings.", "Error code "
					+ publishStatus, "Try again",
					new DialogInterface.OnClickListener() {
				
				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();
					tracker.trackEvent(
							mAuBlogInstallId,  // Category
				            "Publish error",  // Action
				            "User clicked on Try again button in publish"+myEntry.toString()+" Error code "+ publishStatus+" : "+System.currentTimeMillis() +" : "+mAuBlogInstallId, // Label
				            (int)System.currentTimeMillis());       // Value
					
					publishBlogEntry();
				}
			}, "Cancel", new DialogInterface.OnClickListener() {
				
				public void onClick(DialogInterface dialog, int which) {
					tracker.trackEvent(
							mAuBlogInstallId,  // Category
				            "Publish error",  // Action
				            "User clicked on cancel button in publish"+myEntry.toString()+" Error code "+ publishStatus+" : "+System.currentTimeMillis() +" : "+mAuBlogInstallId, // Label
				            (int)System.currentTimeMillis());       // Value
					
					dialog.cancel();
				}
			});
		}
	}

}
