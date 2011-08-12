package ca.ilanguage.aublog.ui;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Locale;

import com.google.android.apps.analytics.GoogleAnalyticsTracker;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.database.Cursor;
import android.database.SQLException;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.KeyEvent;
import android.webkit.JsResult;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.Toast;

import ca.ilanguage.aublog.R;
import ca.ilanguage.aublog.db.AuBlogHistoryDatabase;
import ca.ilanguage.aublog.db.AuBlogHistoryDatabase.AuBlogHistory;
import ca.ilanguage.aublog.preferences.NonPublicConstants;
import ca.ilanguage.aublog.preferences.PreferenceConstants;
import ca.ilanguage.aublog.preferences.SetPreferencesActivity;
import ca.ilanguage.aublog.service.AudioToText;

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
public class EditBlogEntryActivity extends Activity implements TextToSpeech.OnInitListener {

	GoogleAnalyticsTracker tracker;
	private String mAuBlogInstallId;
    private static final String TAG = "CreateBlogEntryActivity";
    /** Talk to the user */
    private TextToSpeech mTts;
    private String mBloggerAccount;
	private String mBloggerPassword;
    private Long mStartTime;
    private Long mEndTime;
    private Long mTimeAudioWasRecorded;
    private String mAudioSource;
    private String mDateString ="";
    
    private String mAuBlogDirectory = PreferenceConstants.OUTPUT_AUBLOG_DIRECTORY;//"/sdcard/AuBlog/";
    private MediaRecorder mRecorder;
    MediaPlayer mMediaPlayer;
    Boolean mPlayingNow;
    private Boolean mReadBlog;
    //DONE adde recording logic 
    //DONE figure out the problems with the account database,decoup0le the account database with the blog entry screen
    
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
	String mPostParent ="";
	String mPostId ="";
	String mAudioResultsFile;
	Boolean mFreshEditScreen;
	private Boolean mDeleted = false;
	String mLongestEverContent ="";
	private static final String[] PROJECTION = new String[] {
		AuBlogHistory._ID, //0
		AuBlogHistory.ENTRY_TITLE, 
		AuBlogHistory.ENTRY_CONTENT, //2
		AuBlogHistory.ENTRY_LABELS,
		AuBlogHistory.PUBLISHED, //4
		AuBlogHistory.DELETED,
		AuBlogHistory.PARENT_ENTRY, //6
		AuBlogHistory.PUBLISHED_IN,
		AuBlogHistory.TIME_CREATED,//8
		AuBlogHistory.LAST_MODIFIED,
		AuBlogHistory.AUDIO_FILES//10
	};
	
	
	private WebView mWebView;
    private Handler mHandler = new Handler();
  
    //implement on Init for the text to speech
	public void onInit(int status) {
		if (status == TextToSpeech.SUCCESS) {
			// Set preferred language to US english.
			// Note that a language may not be available, and the result will
			// indicate this.
			int result = mTts.setLanguage(Locale.US);
			// Try this someday for some interesting results.
			// int result mTts.setLanguage(Locale.FRANCE);
			if (result == TextToSpeech.LANG_MISSING_DATA
					|| result == TextToSpeech.LANG_NOT_SUPPORTED) {
				// Language data is missing or the language is not supported.
				Log.e(TAG, "Language is not available.");
				//Toast.makeText(EditBlogEntryActivity.this, "The English TextToSpeech isn't installed, you can go into the \nAndroid's settings in the \nVoice Input and Output menu to turn it on. ", Toast.LENGTH_LONG).show();

			} else {
				//everything is working.
			}
		} else {
			// Initialization failed.
			tracker.trackEvent(
		            "DependantPackages",  // Category
		            "FileManager",  // Action
		            "user doesnt have TTS, in the init failed section, didnt take them to package manager: "+mAuBlogInstallId, // Label
		            301);       // Value
        	
			Log.e(TAG, "Sorry, I can't talk to you because I could not initialize TextToSpeech.");
		}
	}
    
    
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
      super.onConfigurationChanged(newConfig);
//      setContentView(R.layout.myLayout);
      Toast.makeText(EditBlogEntryActivity.this, "Configuration changed ", Toast.LENGTH_LONG).show();

    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mTts = new TextToSpeech(this, this);
        mMediaPlayer = new MediaPlayer();
        mMediaPlayer.setLooping(true);
        tracker = GoogleAnalyticsTracker.getInstance();

	    // Start the tracker in manual dispatch mode...
	    tracker.start(NonPublicConstants.NONPUBLIC_GOOGLE_ANALYTICS_UA_ACCOUNT_CODE, 20, this);

	    // ...alternatively, the tracker can be started with a dispatch interval (in seconds).
	    //tracker.start("UA-YOUR-ACCOUNT-HERE", 20, this);
		
        
        mDateString = (String) android.text.format.DateFormat.format("yyyy-MM-dd_hh.mm", new java.util.Date());
	    mDateString = mDateString.replaceAll("/","_").replaceAll(" ","_");
     
	    SharedPreferences prefs = getSharedPreferences(PreferenceConstants.PREFERENCE_NAME, MODE_PRIVATE);
	    mReadBlog = prefs.getBoolean(PreferenceConstants.PREFERENCE_SOUND_ENABLED, true);
	    /*
		 * set the installid for appending to the labels
		 */
		mAuBlogInstallId = prefs.getString(PreferenceConstants.AUBLOG_INSTALL_ID, "0");
		
        
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
				//if the edit blog entry screen is fresh (ie, made from some external ativity not from an on puase or rotate screen, then get the values from the db
				if(mPostId.equals("")|| mFreshEditScreen==true){
					mFreshEditScreen = false;
					mPostId = mCursor.getString(0);
					mPostTitle = mCursor.getString(1);
					mPostContent = mCursor.getString(2);
					mPostLabels =mCursor.getString(3);
					mPostParent = mCursor.getString(6);
					mAudioResultsFile = mCursor.getString(10);
					//Toast.makeText(EditBlogEntryActivity.this, "The audio results file is "+mAudioResultsFile, Toast.LENGTH_LONG).show();
		    		if (mAudioResultsFile.length() > 5){
		    			//SET the media player to point to this audio file so that the play button will work. 
			    		mMediaPlayer.setDataSource(mAudioResultsFile);
			    		mMediaPlayer.prepare();
					}
					if("0".equals(mCursor.getString(5))){ 
						mDeleted=false;
					}else{
						mDeleted=true;
					}
					
	                String nodeAsString="id:"+mCursor.getString(0)+":\ntitle:"+mCursor.getString(1)+":\ncontent:"+mCursor.getString(2)+":\nlabels:"+mCursor.getString(3)+":\npublished:"+mCursor.getString(4)+":\ndeleted:"+mCursor.getString(5)+":\nparent:"+mCursor.getString(6)+":";
	                //Toast.makeText(EditBlogEntryActivity.this, "Full post info:"+nodeAsString, Toast.LENGTH_LONG).show();
	                //Toast.makeText(EditBlogEntryActivity.this, "First load of edit blog screen, all info came from db. ", Toast.LENGTH_LONG).show();
				}else{//else, use the saved state variables
					String tmp = "";
					tmp = "dont look in the db for the values, get them from the state";
					//Toast.makeText(EditBlogEntryActivity.this, "Returning from rotate, no info should be lost. ", Toast.LENGTH_LONG).show();
				}

			} catch (IllegalArgumentException e) {
				// Log.e(TAG, "IllegalArgumentException (DataBase failed)");
				tracker.trackEvent(
			            "Database",  // Category
			            "Bug",  // Action
			            "Retrieval from DB failed with an illegal argument exception "+e+" : "+mAuBlogInstallId, // Label
			            301);       // Value
				Toast.makeText(EditBlogEntryActivity.this, "Retrieval from DB failed with an illegal argument exception "+e, Toast.LENGTH_LONG).show();
			} catch (Exception e) {
				// Log.e(TAG, "Exception (DataBase failed)");
				tracker.trackEvent(
			            "Database",  // Category
			            "Bug",  // Action
			            "The cursor returned is "+e+" : "+mAuBlogInstallId, // Label
			            302);       // Value
				//Toast.makeText(EditBlogEntryActivity.this, "The cursor returned is "+e, Toast.LENGTH_LONG).show();
			}
		}else{
			//this should never be executed
			//this is geting executed when click on a view drafts tree and edit !! not supposed to, the uri came in properly
//			mPostContent="";
			mPostLabels="";
//			mPostTitle="";
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
            //readTTS(toast);
        	Toast.makeText(mContext, toast, Toast.LENGTH_SHORT).show();
        }
        public void readToTTS(String message){
        	if(mReadBlog){
        		readTTS(message);
        	}else{
        		tracker.trackEvent(
        	            "TTS",  // Category
        	            "notUsed",  // Action
        	            "there was a message that was not read via TTS because it is off in the settings: "+message+" : "+mAuBlogInstallId, // Label
        	            362);       // Value
        	}
        }
        /*
         * methods to record and manage recording of blog entry
         * TODO add some infomesages into the tool bar
         */
        public String startToRecord(){
        	return beginRecording();
        }
        public String stopRecord(){
        	return stopSaveRecording();
        }
        
        public String playOrPauseAudio(){
        	return playOrPauseAudioFile();
        }
        
        public Long getTimeRecorded(){
        	return returnTimeRecorded();
        }
        public String hasAudioFile(){
        	return hasAudioFileAttached().toString();
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
        public String fetchDebugInfo(){
        	return "Id: "+mPostId+" Parent: "+mPostParent+" Deleted: "+mDeleted.toString()+" LongestEverString:"+mLongestEverContent;
        }
        public void saveState(String strTitle, String strContent, String strLabels){
        	tracker.trackEvent(
    	            "AuBlogLifeCycleEvent",  // Category
    	            "saveSTate",  // Action
    	            "state was saved via javascript "+strTitle+" : "+strLabels+" : "+strContent+" : "+mAuBlogInstallId, // Label
    	            34);       // Value
        	saveStateToActivity(strTitle, strContent, strLabels);
        }
        public void savePost(String strTitle, String strContent, String strLabels){
//        	mPostContent= strContent;
//        	mPostTitle=strTitle;
//        	mPostLabels=strLabels;
//        	saveState(strTitle, strContent, strLabels);//dont save the post to this entry, instead it should only go in the next entry.
        	saveAsDaughterToDB(strTitle, strContent, strLabels);
    		//Toast.makeText(EditBlogEntryActivity.this, "Saved \n\""+mPostTitle+"\"", Toast.LENGTH_LONG).show();

        }
        public void deletePost(String strTitle, String strContent, String strLabels){
//        	saveState(strTitle, strContent, strLabels);
        	deleteEntry(mUri);
        	finish();
        }
        public void publishPost(String strTitle, String strContent, String strLabels){
        	//act like publish is both save+publish
        	saveAsDaughterToDB(strTitle, strContent, strLabels);
        	if ((mPostTitle.length() == 0)
        			|| (mPostTitle == null)
        			|| (mPostContent.length() == 0)
        			|| (mPostContent == null)) {
        		tracker.trackEvent(
			            "Publish",  // Category
			            "Error",  // Action
			            "displayed Toast:"+R.string.title_or_content_empty_error+" : "+mAuBlogInstallId, // Label
			            30);       // Value
        		Toast.makeText(EditBlogEntryActivity.this, R.string.title_or_content_empty_error, Toast.LENGTH_LONG).show();
        	} else {
        		SharedPreferences prefs = getSharedPreferences(PreferenceConstants.PREFERENCE_NAME, MODE_PRIVATE);
    		    mBloggerAccount = prefs.getString(PreferenceConstants.PREFERENCE_ACCOUNT, "see settings");
        		mBloggerPassword = prefs.getString(PreferenceConstants.PREFERENCE_PASSWORD, "see settings");
        		if( (!mBloggerAccount.contains("@") ) || mBloggerPassword.length()<4 ){
        			tracker.trackEvent(
    			            "Publish",  // Category
    			            "Error",  // Action
    			            "displayed Toast: Taking you to the settings to add a Blogger account.: "+mAuBlogInstallId, // Label
    			            302);       // Value
        			Toast.makeText(EditBlogEntryActivity.this, "No Blogger account found.\n\nTaking you to the settings to \n\nConfigure a Blogger account.", Toast.LENGTH_LONG).show();
        			Intent i = new Intent(EditBlogEntryActivity.this, SetPreferencesActivity.class);
            		startActivity(i);
        		}else{
        		
	        		tracker.setCustomVar(1, "Navigation Type", "Button click", 22);
	    			tracker.trackPageView("/publishBlogEntryScreen");
	    			
	        		Intent i = new Intent(EditBlogEntryActivity.this, PublishActivity.class);
	        		//tell the i the mUri that is supposed to be published
	        		i.setData(mUri);
	        		startActivity(i);
	        		finish();
        		}
        	}
        }
    }
    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
      // Save UI state changes to the savedInstanceState.
      // This bundle will be passed to onCreate if the process is
      // killed and restarted.
    	mWebView.loadUrl("javascript:savePostToState()");
    	/*THIS PUTS IN THE OLD STUFF, SEEMS TO WORK WITH OUT IT.
	      savedInstanceState.putString("title", mPostTitle);
	      savedInstanceState.putString("content", mPostContent);
	      savedInstanceState.putString("labels", mPostLabels);
	      savedInstanceState.putString("longestcontentever", mLongestEverContent);
	      savedInstanceState.putBoolean("deleted", mDeleted);
	      savedInstanceState.putString("parentid", mPostParent);
	      savedInstanceState.putString("id",mPostId);
//	      savedInstanceState.putString("uri", mUri.getPath());
 * 
 */
      
      // etc.
      super.onSaveInstanceState(savedInstanceState);
    }
    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
      super.onRestoreInstanceState(savedInstanceState);
      // Restore UI state from the savedInstanceState.
      // This bundle has also been passed to onCreate.
      /*THIS PUTS UP THE OLD VERSION
      mPostTitle = savedInstanceState.getString("title");
      mPostContent = savedInstanceState.getString("content");
      mPostLabels = savedInstanceState.getString("labels");
      mLongestEverContent = savedInstanceState.getString("longestcontentever");
      mDeleted = savedInstanceState.getBoolean("deleted");
      mPostParent = savedInstanceState.getString("parentid");
      mPostId = savedInstanceState.getString("id");
      */
//      mUri = new Uri(savedInstanceState.getString("uri"));
    }
    @Override
	protected void onPause() {
    	tracker.trackEvent(
	            "Event",  // Category
	            "Pause",  // Action
	            "event was paused: "+mAuBlogInstallId, // Label
	            38);       // Value
    	mFreshEditScreen=false;
    	mWebView.loadUrl("javascript:savePostToState()");
    	/*
    	 * un-user-initiated saves do not create a new node in the draft tree (although, this can be changed
    	 * by just calling saveAsDaugher here)
    	 * 1. asks javascript to put current values into state
    	 * 2. saves state to database as self
    	 * 
    	 * Potential bug: this needs to operate syncronously, if operated async, then the changed values in the javascript will never be perserved unless the user hits save first (before hitting back button or rotating screen). 
    	 * 
    	 */
    	mWebView.loadUrl("javascript:savePostToState()");
    	saveAsSelfToDB();
		//mWebView.loadUrl("javascript:savePostToDB()");
		super.onPause();
		
//		saveOrUpdateToDB();

	}
	@Override
	protected void onDestroy() {
		super.onDestroy();
		// Log.i(TAG, "Method 'onDestroy()' launched");
		tracker.stop();
		//saveOrUpdateToDB();
//		mWebView.loadUrl("javascript:savePostToDB()");

	}
	private void saveStateToActivity(String strTitle, String strContent, String strLabels){
    	if(mDeleted == true){
    		return;
    	}
    	if (!(mPostTitle.equals(strTitle)) ){
    		flagDraftTreeAsNeedingToBeReGenerated();
    	}
    	mPostContent= strContent;
    	mPostTitle=strTitle;
    	mPostLabels=strLabels;
    	if (mLongestEverContent.length() < (mPostTitle+mPostContent+mPostLabels).length() ){
			mLongestEverContent=mPostTitle+mPostContent+mPostLabels;
		}
    }
	private void saveAsSelfToDB(){
		if (mDeleted == true){
			return ;
		}
    	try{
    		if (mLongestEverContent.length() < (mPostTitle+mPostContent+mPostLabels).length() ){
    			mLongestEverContent=mPostTitle+mPostContent+mPostLabels;
    		}
//    		if ( mLongestEverContent.length() <=3 ){ 
//    			//delete the entry the blog entry is completely empty, or if the user never anything. this should prevent having empty entrys in the database, but keep entries that are zeroed out and had content before
//    			deleteEntry(mUri);
//    		}
    		else{
	    		ContentValues values = new ContentValues();
	        	values.put(AuBlogHistory.ENTRY_TITLE, mPostTitle);
	        	values.put(AuBlogHistory.ENTRY_CONTENT, mPostContent);
	        	values.put(AuBlogHistory.ENTRY_LABELS, mPostLabels);
	        	values.put(AuBlogHistory.LAST_MODIFIED, Long.valueOf(System.currentTimeMillis()));
	        	values.put(AuBlogHistory.AUDIO_FILES, mAudioResultsFile);
//	        	values.put(AuBlogHistory.USER_TOUCHED, "true"); TODO maybe make a field to indicate that the user never touched the entry, that way wont loose branches in the tree? 
	    		getContentResolver().update(mUri, values,null, null);
	    		Log.d(TAG, "Post saved to database.");
	    		//Toast.makeText(EditBlogEntryActivity.this, "Post " +mUri.getLastPathSegment()+" saved as self to database\n\nTitle: "+mPostTitle+"\nLabels: "+mPostLabels+"\n\nPost: "+mPostContent, Toast.LENGTH_LONG).show();
    		}
    	} catch (SQLException e) {
    		// Log.e(TAG,"SQLException (createPost(title, content))");
    		tracker.trackEvent(
    	            "Database",  // Category
    	            "Bug",  // Action
    	            "Database connection problem "+e+" : "+mAuBlogInstallId, // Label
    	            3201);       // Value
    		Toast.makeText(EditBlogEntryActivity.this, "Database connection problem "+e, Toast.LENGTH_LONG).show();
    	} catch (Exception e) {
    		// Log.e(TAG, "Exception: " + e.getMessage());
    		tracker.trackEvent(
    	            "Database",  // Category
    	            "Bug",  // Action
    	            "exception "+e+" : "+mAuBlogInstallId, // Label
    	            3202);       // Value
    		Toast.makeText(EditBlogEntryActivity.this, "exception "+e, Toast.LENGTH_LONG).show();
    	}
	}
	public void deleteEntry(Uri uri){
    	mDeleted = true;
    	/*
		 * Flag entry as deleted
		 */
    	
    	tracker.trackEvent(
	            "AuBlogLifeCycleEvent",  // Category
	            "Delete",  // Action
	            "entry was flagged as deleted in the database"+uri.getLastPathSegment()+" : "+mAuBlogInstallId, // Label
	            39);       // Value
		ContentValues values = new ContentValues();
		values.put(AuBlogHistory.DELETED,"1");//sets deleted flag to true
		getContentResolver().update(uri, values,null, null);
//		getContentResolver().delete(uri, null, null);
		flagDraftTreeAsNeedingToBeReGenerated();
		Toast.makeText(EditBlogEntryActivity.this, "Post " +uri.getLastPathSegment()+" deleted.", Toast.LENGTH_LONG).show();
		finish();
	}
	/*
	 * An android method to wrap a call to the TTS engine, the logic of if the app should use text to speech (based on settings check box) is handled in the javascript interface. 
	 */
	public void readTTS(String message){
		tracker.trackEvent(
	            "TTS",  // Category
	            "Use",  // Action
	            "spoke message: "+message+" : "+mAuBlogInstallId, // Label
	            361);       // Value
		mTts.speak(message,TextToSpeech.QUEUE_ADD, null);
		
	}
	public Boolean hasAudioFileAttached(){
		if (mAudioResultsFile.length() > 5 ){
			Toast.makeText(EditBlogEntryActivity.this,"There is an audio file.", Toast.LENGTH_SHORT).show();
    		return true;
    	}else{
			Toast.makeText(EditBlogEntryActivity.this,"No audio file.", Toast.LENGTH_SHORT).show();
    		return false;
    	}
	}
	public String beginRecording(){
		mAudioSource= "microphone";
		tracker.trackEvent(
	            "Clicks",  // Category
	            "Button",  // Action
	            "record audio via "+mAudioSource+" : "+mAuBlogInstallId, // Label
	            34);       // Value
		/*
		 * TODO get audio from blue tooth or mic or usb mic?
		 */
		
		mStartTime=System.currentTimeMillis();
		mDateString = (String) android.text.format.DateFormat.format("yyyy-MM-dd_hh.mm", new java.util.Date());
		mDateString = mDateString.replaceAll("/","_").replaceAll(" ","_");
		mAudioResultsFile = mAuBlogDirectory+"audio/";
		new File(mAudioResultsFile).mkdirs();
		mAudioResultsFile=mAudioResultsFile+mDateString+"_"+System.currentTimeMillis()+"_"+mPostTitle+".mp3";    
		mRecorder = new MediaRecorder();
		try {
	    	//http://www.benmccann.com/dev-blog/android-audio-recording-tutorial/
	    	mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
		    mRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
		    mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
		    mRecorder.setOutputFile(mAudioResultsFile);
		    mRecorder.prepare();
		    mRecorder.start();
		} catch (IllegalStateException e) {
			// TODO Auto-generated catch block
			Toast.makeText(EditBlogEntryActivity.this, "The App cannot save audio, maybe the Android is attached to a computer?", Toast.LENGTH_SHORT).show();
			tracker.trackEvent(
    	            "Record",  // Category
    	            "Bug",  // Action
    	            "The App cannot save audio, maybe the Android is attached to a computer?" +e+" : "+mAuBlogInstallId, // Label
    	            3301);       // Value
		} catch (IOException e) {
			// TODO Auto-generated catch block
			Toast.makeText(EditBlogEntryActivity.this, "The App cannot save audio, maybe the Android is attached to a computer?", Toast.LENGTH_SHORT).show();
			tracker.trackEvent(
    	            "Record",  // Category
    	            "Bug",  // Action
    	            "The App cannot save audio, maybe the Android is attached to a computer?" +e+" : "+mAuBlogInstallId, // Label
    	            3302);       // Value
		}
		return "Recording...";
	}
	public String playOrPauseAudioFile(){
		if(mMediaPlayer.isPlaying()){
			//if its playing, pause and rewind ~4 seconds
			
			mMediaPlayer.pause();
			/* rewind logic doesnt work
			int rewindValue = 2;
			int startPlayingFromSecond =mMediaPlayer.getCurrentPosition();
			if ( startPlayingFromSecond <= rewindValue){
				startPlayingFromSecond=0;
			}else{
				startPlayingFromSecond = startPlayingFromSecond - rewindValue;
			}
			mMediaPlayer.seekTo(startPlayingFromSecond);
			mMediaPlayer.prepare();
			*/
			return "Play";
		}else{
			//if its not playing, play it
		    try {
		    	
		    	mMediaPlayer.start();
			} catch (IllegalArgumentException e) {
				// TODO Auto-generated catch block
				Log.e("Error reading file", e.toString());
			} catch (IllegalStateException e) {
				// TODO Auto-generated catch block
				Log.e("Error reading file", e.toString());
			} 
			return "Pause";
		}
		
	}
	public String stopSaveRecording(){
		
		mEndTime=System.currentTimeMillis();
	   	mRecorder.stop();
	   	mRecorder.release();
	   	
	   	/*
	   	 * assign this audio recording to the media player
	   	 */
	   	try {
			mMediaPlayer.setDataSource(mAudioResultsFile);
			mMediaPlayer.prepare();
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalStateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	   	
	   	mTimeAudioWasRecorded=mEndTime-mStartTime;
	   	
	   	//Javascript changes the blog content to add the length of the recording 
	   	//Javascript simpulates a click on the save button, so most likely it will be saved as a daughter. 
	   	
	   	tracker.trackEvent(
	            "AuBlogLifeCycleEvent",  // Category
	            "Dictation",  // Action
	            "stop audio recording "+mTimeAudioWasRecorded/100+"sec: "+mAuBlogInstallId, // Label
	            35);       // Value
	   	
	    // Keep the volume control type consistent across all activities.
        setVolumeControlStream(AudioManager.STREAM_MUSIC);
//        Uri uri = new Uri();
//        uri.fromFile(new File(mAudioResultsFile));
//        MediaPlayer mp = MediaPlayer.create(this, uri);
//        mp.start();
        
        /*
         * Transcription possibilities:
         * 1. using googles not published speech API
         * 	http://src.chromium.org/viewvc/chrome/trunk/src/content/browser/speech/speech_recognition_request.cc?view=markup
         *  Perl example: http://mikepultz.com/2011/03/accessing-google-speech-api-chrome-11/
         *  Java example: ?
         * 2. using the Voice Recognition sample app, tweeked to automate the button to cut up audio chunks
         *   http://developer.android.com/resources/samples/ApiDemos/src/com/example/android/apis/app/VoiceRecognition.html
         *   http://developer.android.com/resources/articles/speech-input.html
         * 3. Sphinx project
         * 	http://cmusphinx.sourceforge.net/
         * 
         * Audio splitting based on silence
         * 1. c: https://github.com/taf2/audiosplit/graphs/languages
         */
        /* Code to do a voice recognition via google voice:
        try {
			URL url = new URL("https://www.google.com/speech-api/v1/recognize?xjerr=1&client=chromium&lang=en-US");
			
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
			Toast.makeText(EditBlogEntryActivity.this, "The App cannot transcribe audio, maybe the Android has no network connection?"+e, Toast.LENGTH_SHORT).show();

		}
		Intent i = new Intent(EditBlogEntryActivity.this, AudioToText.class);
		//tell the i the mUri that is supposed to be published
		/*
		 * TODO, start activity for result 
		 * get the array of results, use some internal aublog logic to determine which is most likely and append the text into the blog content
		 
		startActivity(i);
		*/
		
		
		return "Attached "+mTimeAudioWasRecorded/100+" second Recording.\n";
	}
	
	public Long returnTimeRecorded(){
		//Long timePassed = (System.currentTimeMillis()-mStartTime)/1000;
		
		return mTimeAudioWasRecorded;//timePassed+"min";
	}
	
	
	private void saveAsDaughterToDB(String strTitle, String strContent, String strLabels){
    	try{
    		/*
    		 * Create daughter
    		 */
        	ContentValues daughterValues = new ContentValues();
        	daughterValues.put(AuBlogHistory.ENTRY_TITLE, strTitle);
        	daughterValues.put(AuBlogHistory.ENTRY_CONTENT, strContent);
        	daughterValues.put(AuBlogHistory.ENTRY_LABELS, strLabels);
        	daughterValues.put(AuBlogHistory.LAST_MODIFIED, Long.valueOf(System.currentTimeMillis()));
        	daughterValues.put(AuBlogHistory.AUDIO_FILES, mAudioResultsFile);
        	if ( (mPostTitle+mPostContent+mPostLabels).length() <= 0 ){
        		if (mLongestEverContent.length() <= 0 ){
        			saveStateToActivity(strTitle, strContent, strLabels);
        			saveAsSelfToDB();
        			tracker.trackEvent(
            	            "AuBlogLifeCycleEvent",  // Category
            	            "Save",  // Action
            	            "save as self:no new text: "+mAuBlogInstallId, // Label
            	            311);       // Value
        			return;
        		}
        		//if the user blanked out the blog entry, probably they are restarting from scratch so set the parent to zero node
        		daughterValues.put(AuBlogHistory.PARENT_ENTRY, AuBlogHistoryDatabase.ROOT_ID_DEFAULT);
        		
    		}else{
    			daughterValues.put(AuBlogHistory.PARENT_ENTRY, mUri.getLastPathSegment());
    		}
    		Uri daughterUri = getContentResolver().insert(AuBlogHistory.CONTENT_URI, daughterValues);
    		tracker.trackEvent(
    	            "AuBlogLifeCycleEvent",  // Category
    	            "Save",  // Action
    	            "save as daughter: "+mAuBlogInstallId, // Label
    	            312);       // Value
    		/*
    		 * Save parent but just tell it has a daughter, dont put the new values into its entry.
    		 * It should stay the way it was last saved when the user pushed the save button.
    		 */
//    		ContentValues parentValues = new ContentValues();
//    		parentValues.put(AuBlogHistory.DELETED,"true");
//    		getContentResolver().update(mUri, parentValues,null, null);
    		/*
    		 * Set the daughter to the active mUri, and reinitialize the state values to the daughers values
    		 */
    		mPostParent=mUri.getLastPathSegment();
    		//Toast.makeText(EditBlogEntryActivity.this, "Post "+daughterUri.getLastPathSegment()+" saved as daugher of: " +mUri.getLastPathSegment()+" to database\n\nTitle: "+mPostTitle+"\nLabels: "+mPostLabels+"\n\nPost: "+mPostContent, Toast.LENGTH_LONG).show();
    		mUri=daughterUri;
    		getIntent().setData(mUri);
    		saveStateToActivity(strTitle, strContent, strLabels);
    		mDeleted=false;
    		mPostId=mUri.getLastPathSegment();
    		
//    		mTts.speak("The text to speech is working. This means I can talk to you so that you don't have to look at the screen.",
//    		        TextToSpeech.QUEUE_FLUSH,  // Drop all pending entries in the playback queue.
//    		        null);
    		
    		Log.d(TAG, "Post saved to database.");
    	} catch (SQLException e) {
    		tracker.trackEvent(
    	            "Database",  // Category
    	            "Bug",  // Action
    	            "Database connection problem "+e+" : "+mAuBlogInstallId, // Label
    	            3101);       // Value
    		// Log.e(TAG,"SQLException (createPost(title, content))");
    		Toast.makeText(EditBlogEntryActivity.this, "Database connection problem "+e, Toast.LENGTH_LONG).show();
    	} catch (Exception e) {
    		// Log.e(TAG, "Exception: " + e.getMessage());
    		
    		//Toast.makeText(EditBlogEntryActivity.this, "Exception "+e, Toast.LENGTH_LONG).show();
    		tracker.trackEvent(
    	            "Database",  // Category
    	            "Bug",  // Action
    	            "Unknown exception "+e+" : "+mAuBlogInstallId, // Label
    	            3102);       // Value
    	}
    	flagDraftTreeAsNeedingToBeReGenerated();

	}
	private void flagDraftTreeAsNeedingToBeReGenerated(){
		/*
    	 * Flag the draft tree as needing to be regenerated
    	 */
    	SharedPreferences prefs = getSharedPreferences(PreferenceConstants.PREFERENCE_NAME, MODE_PRIVATE);
    	SharedPreferences.Editor editor = prefs.edit();
    	editor.putBoolean(PreferenceConstants.PREFERENCE_DRAFT_TREE_IS_FRESH,false);
    	editor.commit();
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
     
    final class MyWebChromeClient extends WebChromeClient {
        @Override
        public boolean onJsAlert(WebView view, String url, String message, JsResult result) {
            Log.d(TAG, message);
            result.confirm();
            return true;
        }
        
    }*/
    
}