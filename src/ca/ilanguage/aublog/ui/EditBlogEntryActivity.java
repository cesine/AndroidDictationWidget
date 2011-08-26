package ca.ilanguage.aublog.ui;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Locale;

import com.google.android.apps.analytics.GoogleAnalyticsTracker;
import com.google.gdata.client.youtube.YouTubeQuery.SafeSearch;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.DialogInterface.OnClickListener;
import android.content.res.Configuration;
import android.database.Cursor;
import android.database.SQLException;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.Toast;

import ca.ilanguage.aublog.R;
import ca.ilanguage.aublog.db.AuBlogHistoryDatabase;
import ca.ilanguage.aublog.db.AuBlogHistoryDatabase.AuBlogHistory;
import ca.ilanguage.aublog.preferences.NonPublicConstants;
import ca.ilanguage.aublog.preferences.PreferenceConstants;
import ca.ilanguage.aublog.preferences.SetPreferencesActivity;
import ca.ilanguage.aublog.service.DictationRecorderService;
import ca.ilanguage.aublog.service.NotifyingTranscriptionIntentService;
import ca.ilanguage.aublog.service.NotifyingTranscriptionService;

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
    private Menu mMenu;
    private String mBloggerAccount;
	private String mBloggerPassword;
    private Long mStartTime;
    private Long mEndTime;
    private Long mTimeAudioWasRecorded;
    private String mAudioSource;//bluetooth(record,play), phone(recordmic, play earpiece) for privacy, speaker(record mic, play speaker)
    private Boolean mUseBluetooth;
    private Boolean mUseEarPhones;
    private Boolean mUsePhoneEarPiece;
    private String mDateString ="";
    private AudioFileUpdateReceiver audioFileUpdateReceiver;
    private String mAuBlogDirectory = PreferenceConstants.OUTPUT_AUBLOG_DIRECTORY;//"/sdcard/AuBlog/";
    private AudioManager mAudioManager;
    private MediaPlayer mMediaPlayer;
    Boolean mRecordingNow;
    Boolean mPlayingNow;
    private Boolean mReadBlog;
    private Boolean mSendForTranscription = false;
    //DONE adde recording logic 
    //DONE figure out the problems with the account database,decoup0le the account database with the blog entry screen
    
	//uri of the entry being edited.
	private Uri mUri;
	private Cursor mCursor;
	//savedInstanceState
	public static final String EXTRA_CURRENT_CONTENTS ="currentContents";
	public static final String EXTRA_TRANSCRIPTION_RETURNED = "returnedTranscriptionBoolean";
	public static final String EXTRA_FROM_NOTIFICATION_RECORDING_STILL_RUNNING ="recordingStillRunning";
	//private Boolean mReturnedTranscription; //check on reload?
	public static final String REFRESH_AUDIOFILE_INTENT = NonPublicConstants.NONPUBLIC_INTENT_AUDIOFILE_RECORDED_AND_SAVED;
	public static final String REFRESH_TRANSCRIPTION_INTENT = NonPublicConstants.NONPUBLIC_INTENT_TRANSCRIPTION_RECEIVED;
	public static final String DICTATION_SENT_INTENT = NonPublicConstants.NONPUBLIC_INTENT_DICTATION_SENT;
	public static final String DICTATION_STILL_RECORDING_INTENT = NonPublicConstants.NON_PUBLIC_INTENT_DICTATION_STILL_RECORDING;
	public static final String TRANSCRIPTION_STILL_CONTACTING_INTENT = NonPublicConstants.NON_PUBLIC_INTENT_TRANSCRIPTION_STILL_CONTACTING;
	
	private static final int CHANGED_SETTINGS = 0;
	int selectionStart;
	int selectionEnd;
	Bundle mWebViewsState;
	String mPostContent;
	String mPostTitle;
	String mPostLabels;
	String mPostParent;
	String mPostId;
	String mAudioResultsFile;
	String mAudioResultsFileStatus;
	String mTranscription;
	String mTranscriptionAndContents;
	Boolean mFreshEditScreen;
	private Boolean mDeleted;
	private Boolean mURIDeleted =false;;
	String mLongestEverContent;
	private  String[] PROJECTION = new String[] {
		AuBlogHistory._ID, //0
		AuBlogHistory.ENTRY_TITLE, 
		AuBlogHistory.ENTRY_CONTENT, //2
		AuBlogHistory.ENTRY_LABELS,
		AuBlogHistory.PUBLISHED, //4
		AuBlogHistory.DELETED,
		AuBlogHistory.PARENT_ENTRY, //6
		AuBlogHistory.PUBLISHED_IN,
		AuBlogHistory.TIME_CREATED,//8
		AuBlogHistory.LAST_MODIFIED, ////this is a value generated by a database, use LAST_EDITED is a value generated the UI 
		AuBlogHistory.AUDIO_FILE,//10
		AuBlogHistory.AUDIO_FILE_STATUS
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
	/*
	 * Important Potential Hazard:
	 * 
	 * Using the bluetooth for audio in 2.2 has a bug which has been documented here:
	 * http://code.google.com/p/android/issues/detail?id=9503
	 * Bottom line: this activity can crash the phone if the user turns off the bluetooth device in this activity, in Android 2.2.  
	 * 
	 * 
	 * - Steps to reproduce the problem (including sample code if appropriate).
		
		using startBluetoothSco/stopBluetoothSco on Android 2.2 (FRF85B)
		don't exit the app that called them
		then disable or disconnect link to bluetooth headset
		
		- What happened.
		
		The system rebooted because of a crash in AudioService.java. When the headset gets disconnected it tries to call unlinkToDeath with "noSuchElementExceptions: death link does not exist"
		
		- What you think the correct behavior should be.
		
		When calling stopBluetoothSco the ScoClient should get removed from the list of ScoClients.
		
		
	 */
    


	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
		case CHANGED_SETTINGS:
			if(mRecordingNow  || mPlayingNow){
				/*
				 * if recording now, or playing now is true, don't change the audio settings.
				 * instead after user clicks on stop then can change the audio
				 * settings. maybe android is robust enough to start recording
				 * for example with the blue tooth, then switch to normal mode
				 * but dont want to risk it.
				 */
			}else{
				recheckAublogSettings();
			}
			break;
		default:
			break;
		}
	}
	/**
	 * Re-checks the audio settings and the installid from the settings.
	 * 
	 */
	private void recheckAublogSettings() {
		Boolean oldBluetooth=mUseBluetooth;
		Boolean oldEarphones=mUseEarPhones;  
		Boolean oldPhoneEarPiece=mUsePhoneEarPiece;
		SharedPreferences prefs = getSharedPreferences(
				PreferenceConstants.PREFERENCE_NAME, MODE_PRIVATE);
		/*
		 * set the installid for appending to the labels
		 */
		mAuBlogInstallId = prefs.getString(PreferenceConstants.AUBLOG_INSTALL_ID, "0");
		mReadBlog = prefs.getBoolean(PreferenceConstants.PREFERENCE_SOUND_ENABLED, true);
		
		mUseBluetooth = prefs.getBoolean(PreferenceConstants.PREFERENCE_USE_BLUETOOTH_AUDIO, false);
		//mUseEarPhones = prefs.getBoolean(PreferenceConstants.PREFERENCE_USE_EARPHONES_AUDIO, false);
		//controlled properly by Android, dont need to use but included here for completeness.
		mUseEarPhones = mAudioManager.isWiredHeadsetOn();
		mUsePhoneEarPiece = prefs.getBoolean(PreferenceConstants.PREFERENCE_USE_PHONE_EARPIECE_AUDIO, false);
		
		if (mUseBluetooth){
			
			if( (mAudioManager.isBluetoothScoOn() == false ) || (oldBluetooth ==null ) || (mUseBluetooth != oldBluetooth) )  {
		
			/*
			 * If the user wants bluetooth, but it wasnt set up yet, 
			 * or the user changed the bluetooth to on...
			 * 
			 * TODO As the SCO connection establishment can take several seconds,
			 * applications should not rely on the connection to be available
			 * when the method returns but instead register to receive the
			 * intent ACTION_SCO_AUDIO_STATE_CHANGED and wait for the state to
			 * be SCO_AUDIO_STATE_CONNECTED. Even if a SCO connection is
			 * established, the following restrictions apply on audio output
			 * streams so that they can be routed to SCO headset: - the stream
			 * type must be STREAM_VOICE_CALL - the format must be mono - the
			 * sampling must be 16kHz or 8kHz
			 * 
			 * Similarly, if a call is received or sent while an application is
			 * using the SCO connection, the connection will be lost for the
			 * application and NOT returned automatically when the call ends.
			 * Notes: Use of the blue tooth does not affect the ability to
			 * recieve a call while using the app, However, the app will not
			 * have control of hte bluetooth connection when teh phone call
			 * comes back. Aublog refreshes the settings at key recording/play situations to reestablish the connection.
			 */
			mAudioManager.startBluetoothSco();
			mAudioManager.setSpeakerphoneOn(false);
			mAudioManager.setBluetoothScoOn(true);

			setVolumeControlStream(AudioManager.STREAM_VOICE_CALL);
			mAudioManager.setMode(AudioManager.MODE_IN_CALL);
			mAudioSource = "maybe bluetooth";
			}//end iff to change bluetooth settings
			/*
			 * else blue tooth is on, and nothing changed in the user preferences. it should still be on, so dont change any audio manager.
			 * if other areas of the app turn off the blue tooth then this else might be needed to confirm that bluetooth is set up.
			 */
		}else if (oldBluetooth != null && oldBluetooth){
			/*
			 * mUseBluetooth is off now, but it was ON then the user turned off
			 * bluetooth 
			 * 
			 * NOTE: in Android 2.2 (fixed in Android 2.2.1) stop
			 * bluetooth doest completely release it. the only way to release it
			 * is to kill the process that called it, this is why AuBlog kills
			 * it self in the ondestroy of the main menu, it doesnt have to kill
			 * itself in the on destroy of the notification controller (which
			 * can be called without running the main menu) because it doesnt do anything with the audio manager.
			 */
			mAudioManager.setMode(AudioManager.MODE_NORMAL);
			mAudioManager.setSpeakerphoneOn(true);
			mAudioManager.setBluetoothScoOn(false);
			mAudioManager.stopBluetoothSco();
		}
		/*
		 * regardless of whether user wants either bluetooth or the phone's internal incall speaker
		 * the speakerphone and audiomanager settings are the same 
		 */
		if (mUsePhoneEarPiece || mUseBluetooth){
			if ((oldPhoneEarPiece ==null)||(mUsePhoneEarPiece != oldPhoneEarPiece)) {
			/*
			 * If user wants to play audio through the phone's in-call speaker,
			 */
			mAudioManager.setSpeakerphoneOn(false);
			setVolumeControlStream(AudioManager.STREAM_VOICE_CALL);
			mAudioManager.setMode(AudioManager.MODE_IN_CALL);//TODO try changing this to MODE_IN_COMMUNICATION, all bluetooth discussions say must use IN_CALL but it appears to be inappropriate for non-telephoney apps.
			}
			if (!mAudioManager.isBluetoothScoOn()){
				mAudioSource = "internal microphone";
			}
		}else{
			/*
			 * if mUseBluetooth and the Phones earpiece are supposed to be off,
			 * This sets audio values to normal
			 * 
			 * This is all that is needed to enable the Headset/earphones
			 * option. As the earphones settings are controlled by simply
			 * plugging them in. The android, if in normal mode, will switch to
			 * earphones mode.
			 */
			mAudioManager.setMode(AudioManager.MODE_NORMAL);
			mAudioManager.setSpeakerphoneOn(true);
		}
		/*
		 * then the app can use the media player, and the recorder as usual
		 */
	}
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(savedInstanceState ==null){
        	mFreshEditScreen=true;
        }else{
        	/*
    		 * make sure non of the variables are still null. they should have been filled in by the cursor or by the onrestorestate
    		 */
    		
        	//mWebView.restoreState(savedInstanceState);
            //super.onRestoreInstanceState(savedInstanceState);
        	
    		if(mPostTitle ==null){
    			mPostTitle = "";
    		}
    		if(mPostContent == null){
    			mPostContent = "";
    		}
    		if(mPostLabels == null){
    			mPostLabels = "";
    		}
    		if(mDeleted == null){
    			mDeleted = false;
    		}
    		if( mLongestEverContent ==null){
    			mLongestEverContent=mPostTitle+mPostContent+mPostLabels;
    		}
    		if(mAudioResultsFile == null){
    			mAudioResultsFile = "";
    		}
    		if(mAudioResultsFileStatus == null){
    			mAudioResultsFileStatus = "";
    		}
    		if(mTimeAudioWasRecorded ==null){
    			mTimeAudioWasRecorded = (long) 0;
    		}
    		
        }//end else to control if its the first oncreate (then fetch from database)
        mTts = new TextToSpeech(this, this);
        mAudioManager = (AudioManager)getSystemService(Context.AUDIO_SERVICE);

	    
        tracker = GoogleAnalyticsTracker.getInstance();

	    // Start the tracker in manual dispatch mode...
	    tracker.start(NonPublicConstants.NONPUBLIC_GOOGLE_ANALYTICS_UA_ACCOUNT_CODE, 20, this);

	    // ...alternatively, the tracker can be started with a dispatch interval (in seconds).
	    //tracker.start("UA-YOUR-ACCOUNT-HERE", 20, this);
		
        
	    mPlayingNow = false;
	    if(mRecordingNow == null){
			mRecordingNow = false;
		}
	    if(mRecordingNow == false){
	    	recheckAublogSettings();
	    }else{
	    	//just check the settings whcih are safe to change when recording in background
	    	SharedPreferences prefs = getSharedPreferences(
					PreferenceConstants.PREFERENCE_NAME, MODE_PRIVATE);
			/*
			 * set the installid for appending to the labels
			 */
			mAuBlogInstallId = prefs.getString(PreferenceConstants.AUBLOG_INSTALL_ID, "0");
			mReadBlog = prefs.getBoolean(PreferenceConstants.PREFERENCE_SOUND_ENABLED, true);
	    }
        mDateString = (String) android.text.format.DateFormat.format("yyyy-MM-dd_hh.mm", new java.util.Date());
	    mDateString = mDateString.replaceAll("/","-").replaceAll(" ","-");
     
        
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
				if(mFreshEditScreen == true){//TODO change to true now that true is being set on first create only
					//mFreshEditScreen = false;
					mPostId = mCursor.getString(0);
					mPostTitle = mCursor.getString(1);
					mPostContent = mCursor.getString(2);
					mPostLabels =mCursor.getString(3);
					mLongestEverContent =mPostTitle+mPostContent+mPostLabels;
					mPostParent = mCursor.getString(6);
					mAudioResultsFile = mCursor.getString(10);
					mAudioResultsFileStatus = mCursor.getString(11);
					//Toast.makeText(EditBlogEntryActivity.this, "The audio results file is "+mAudioResultsFile, Toast.LENGTH_LONG).show();
		    		if (mAudioResultsFile.length() > 5){
		    			//SET the media player to point to this audio file so that the play button will work. 
//			    		mMediaPlayer.setDataSource(mAudioResultsFile);
//			    		mMediaPlayer.prepareAsync();
		    			if(mPostTitle.length() <1){
		    				mPostTitle= "(Audio only)";
		    			}
		    			preparePlayerAttachedAudioFile();
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
//			mPostLabels="";
//			mPostTitle="";
			
		}
		
		mWebView.loadUrl("file:///android_asset/edit_blog_entry_wysiwyg.html");
    }
    /**
     * This inner class contains functions which are available to the javascript in the webview
     * It is called using the name in the line:
     *  mWebView.addJavascriptInterface(new JavaScriptInterface(this), "Android");
     *  
     * So for example, the button in the javascript of the webview, would have as its onClick()
     * Android.stopRecordJS()
     * 
     * Convention: methods in this interface are suffixed with JS to distinguish between Android methods and the JavaScript functions defined in the html
     * 
     * @author cesine
     *
     */
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
        
        public String downloadTranscriptionFromServerJS(String strContents){
        	return downloadTranscription(strContents);
        	
        }
        public int getValueRecordingNowJS(){
        	if(mRecordingNow==null){
        		return 0;
        	}else{
        		if(mRecordingNow){
        			return 1;
        		}else{
        			return 0;
        		}
        	}
        	
        }
        public String importTranscriptionJS(String strContents){
        	askUserIfImport(strContents);
        	return mTranscriptionAndContents;
//        	if(mTranscription == null){
//        		return mAudioResultsFileStatus;
//        	}else{
//        		return mAudioResultsFileStatus+"\n\n"+mTranscription;
//        	}
        }
        public void zeroOutParentResultFileJS(){
        	mAudioResultsFile="";
        	mAudioResultsFileStatus="";
        }
        public Boolean findOutIfFreshDataJS(){
        	return mFreshEditScreen;
        }
        public void showToastJS(String toast) {
            //readTTS(toast);
        	Toast.makeText(mContext, toast, Toast.LENGTH_SHORT).show();
        }
        public void readToTTSJS(String message){
        	//recheckAublogSettings(); //if user turned off tts dont read it
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
        /**
         * A wrapper for the edit blog activity's method which creates a new audio file name TODO using the posts title
         * and sends it off to the DictaitonRecorder service to be recorded. 
         * @param postTitle The current title of the blog post, it will show up in the audio file's name
         * @return an internal status message
         */
        public String startToRecordJS(){
        	return beginRecording();
        }
        /**
         * A wrapper which stops the Dictation recorder service. It doesn't set up the media player immediately as the recording service takes some time to finalize and write the .mp3 to the sdcard. 
         * @return an internal status message
         */
        public String stopRecordJS(){
        	return stopSaveRecording();
        }
        /** 
         * Potentially unnecesary function to prepare the player from the javascript.
         * @return
         */
        public String preparePlayerJS(){
        	preparePlayerAttachedAudioFile();
        	return "player prepared with audio result file";
        }
        /**
         * A wrapper for the play or pause audio function in the edit blog activity
         * @return a string which can be used for the button (ie Play if the media player is paused or stopped, or Pause if the media player is playing)
         */
        public String playOrPauseAudioJS(){
        	return playOrPauseAudioFile();
        }
        /**
         * Returns a Long of the time recorded. TODO The time recorded is extracted out of the audiofile status message when 
         * the audio file data is fetched. This happens in each onstart, and also after the user clicks Stop dictation.
         * This can be used to find out if the blog post has an audio file (it will return a value greater than 0).
         * 
         * @return time in milliseconds of the recording
         */
        public Long getTimeRecordedJS(){
        	return returnTimeRecorded();
        }
        /**
         * Depreciated, use getTimeRecordedJS instead
         * @return
         */
        public String hasAudioFileJS(){
        	return hasAudioFileAttached().toString();
        }
        public String fetchPostContentJS(){
        	if (mPostContent == null){
        		return "";
        	}else{
        		return mPostContent;
        	}        	
        }
        public String fetchPostTitleJS(){
        	if (mPostTitle == null){
        		return "";
        	}else{
        		return mPostTitle;
        	}
        }
        public String fetchPostLabelsJS(){
        	if (mPostLabels == null){
        		return "";
        	}else{
        		return mPostLabels;
        	}
        }
        public String fetchDebugInfoJS(){
        	return "Id: "+mPostId+" Parent: "+mPostParent+" Deleted: "+mDeleted.toString()+" LongestEverString:"+mLongestEverContent;
        }
        public void saveStateJS(String strTitle, String strContent, String strLabels){
        	mPostContent=strContent;
        	mPostTitle=strTitle;
        	mPostLabels=strLabels;
        	
        	tracker.trackEvent(
    	            "AuBlogLifeCycleEvent",  // Category
    	            "saveSTate",  // Action
    	            "state was saved via javascript "+strTitle+" : "+strLabels+" : "+strContent+" : "+mAuBlogInstallId, // Label
    	            34);       // Value
        	saveStateToActivity(strTitle, strContent, strLabels);
        }
        /**
         * Wrapper for the edit blog activty save post as a daughter to the database method. TODO rename method to reflect its action as saving a daughter.
         * 
         * @param strTitle
         * @param strContent
         * @param strLabels
         */
        public void savePostJS(String strTitle, String strContent, String strLabels){
//        	mPostContent= strContent;
//        	mPostTitle=strTitle;
//        	mPostLabels=strLabels;
//        	saveState(strTitle, strContent, strLabels);//dont save the post to this entry, instead it should only go in the next entry.
        	saveAsDaughterToDB(strTitle, strContent, strLabels);
    		//Toast.makeText(EditBlogEntryActivity.this, "Saved \n\""+mPostTitle+"\"", Toast.LENGTH_LONG).show();

        }
        public void deletePostJS(String strTitle, String strContent, String strLabels){
//        	saveState(strTitle, strContent, strLabels);
        	deleteEntry(mUri);
        	finish();
        }
        /**
         * Saves the post as a daughter to the database, then calls the Publish activity, which then gets the info out of the database and publishes it.
         * TODO instead send Title, content, labels to publish activity as extras, and let it save them to the database too? otherwise have to register a content listener in the publish activity. 
         * @param strTitle
         * @param strContent
         * @param strLabels
         */
        public void publishPostJS(String strTitle, String strContent, String strLabels){
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
        			startActivityForResult(i, CHANGED_SETTINGS);
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
    }//end javascript interface
    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
    	//CAREFUL: you need to call super.onSaveInstanceState(savedInstanceState) before adding your values to the Bundle, or they will get wiped out on that call 
    	//http://stackoverflow.com/questions/151777/how-do-i-save-an-android-applications-state
    	mWebView.loadUrl("javascript:savePostToState()");
    	super.onSaveInstanceState(savedInstanceState);
      // Save UI state changes to the savedInstanceState.
      // This bundle will be passed to onCreate if the process is
      // killed and restarted.
    	
    	mFreshEditScreen = false; 
    	//mWebView.saveState(savedInstanceState);//http://stackoverflow.com/questions/4726637/android-how-to-savestate-of-a-webview-with-an-addjavascriptinterface-attached

	      savedInstanceState.putString("title", mPostTitle);
	      savedInstanceState.putString("content", mPostContent);
	      savedInstanceState.putString("labels", mPostLabels);
	      savedInstanceState.putString("longestcontentever", mLongestEverContent);
	      savedInstanceState.putString("audiofile",mAudioResultsFile);
	      savedInstanceState.putBoolean("fresheditscreen",mFreshEditScreen);
	      savedInstanceState.putString("audiofilestatus",mAudioResultsFileStatus);
	      savedInstanceState.putBoolean("deleted", mDeleted);
	      savedInstanceState.putBoolean("recordingNow", mRecordingNow);
	      savedInstanceState.putString("parentid", mPostParent);
	      savedInstanceState.putString("id",mPostId);
//	      savedInstanceState.putString("uri", mUri.getPath());
 
      
      // etc.
     
    }
    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
    	super.onRestoreInstanceState(savedInstanceState);
        if(savedInstanceState != null){
    		//mWebView.restoreState(savedInstanceState);
    	}
    	
      // Restore UI state from the savedInstanceState.
      // This bundle has also been passed to onCreate.
      mPostTitle = savedInstanceState.getString("title");
      mPostContent = savedInstanceState.getString("content");
      mPostLabels = savedInstanceState.getString("labels");
      mLongestEverContent = savedInstanceState.getString("longestcontentever");
      mAudioResultsFile = savedInstanceState.getString("audiofile");
      mFreshEditScreen = savedInstanceState.getBoolean("fresheditscreen");
      mAudioResultsFileStatus = savedInstanceState.getString("audiofilestatus");
      mRecordingNow = savedInstanceState.getBoolean("recordingNow");
      mDeleted = savedInstanceState.getBoolean("deleted");
      mPostParent = savedInstanceState.getString("parentid");
      mPostId = savedInstanceState.getString("id");
      
//      mUri = new Uri(savedInstanceState.getString("uri"));
    }
    /* dont import transcriptions in android, do it in javascript
    @Override
	protected void onStart() {
    	
		//find out if the intent has a new transcription to insert into the blog content. 
    	mReturnedTranscription = getIntent().getBooleanExtra(EXTRA_TRANSCRIPTION_RETURNED,false);
    	if( ! mReturnedTranscription ){
    		// nothing to do if no returned transcription
    	}else{
    		/*
    		 * Insert transcription into the blog entry content. Keep current content below the transcription.
    		 
    		mAudioResultsFileStatus="transcriptionintegratedinblogentry";
    		BufferedReader in;
    		String transcription="";
			try {
				in = new BufferedReader(new FileReader(mAudioResultsFile.replace(".mp3",".srt")));
				String line ="";
				while((line = in.readLine()) != null){
					transcription = transcription + line + "\n";
				}
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				//e.printStackTrace();
				mAudioResultsFileStatus=mAudioResultsFileStatus+"problemcopyingfromsdcardtoblogpost";
				mPostContent= "There was a problem reading the transcriptions from the SDCARD to, maybe the device is connected to a computer?\n\n"+mPostContent;
			} catch (IOException e) {
				// TODO Auto-generated catch block
				//e.printStackTrace();
				mAudioResultsFileStatus=mAudioResultsFileStatus+"problemcopyingfromsdcardtoblogpost";
				mPostContent= "There was a problem reading the transcriptions from the SDCARD to, maybe the device is connected to a computer?\n\n"+mPostContent;
				//relaunch the pending intent, or create a new method that put transcriptions into aublgo entries, run it out of the main generate drafts tree functions?
			}
			mPostContent=transcription+mPostContent;
    		
    		mWebView.loadUrl("javascript:fillFromAndroidActivity()");
    		saveAsSelfToDB();
    		//Toast.makeText(EditBlogEntryActivity.this, "onResume, there is a transcription to load. Show alert yes no.", Toast.LENGTH_LONG).show();
    	}
    	super.onStart();
	}*/

	/**
	 * 
	 * Un-user-initiated saves do not create a new node in the draft tree
	 * (although, this can be changed by just calling saveAsDaugher here)
	 * 
	 * Rotate screen: save as self to database 
	 * Back button: save as self to database
	 * 
	 * // http://developer.android.com/guide/topics/media/index.html
	 * As you may know, when the user changes the screen orientation (or changes
	 * the device configuration in another way), the system handles that by
	 * restarting the activity (by default), so you might quickly consume all of
	 * the system resources as the user rotates the device back and forth
	 * between portrait and landscape, because at each orientation change, you
	 * create a new MediaPlayer that you never release.
	 * 
	 * DONE: playing and pausing is kept in the EditBlog activity, if the user
	 * rotates the screen or leaves it will probably not stop playing (the media player 
	 * is released in the onDestroy rather than the onPause. It will 
	 * stop playing when the user quits the edit blog activity. This is preferred. If
	 * the user wants to listen to their audio in the background they can use
	 * the normal Music player by opening the settings, and going to the audio
	 * folder.
	 * 
	 * DONE: refactored record as a service (foregrounded so that it will be
	 * less likely to be killed by android) Aublog will record a dictation until
	 * A: the user clicks stop in the EditBlogEntryActivity B: the users quits
	 * Aublog MainMenuActivity (ondestroy method) C: the user clicks on the
	 * notification, goes to the NotificationController and clicks Stop
	 * Recording. D: the system runs out of memory E: the service is killed by
	 * the system F: (aublog is killed by the system?) the service should be
	 * running in the same process id, so technically the service's ondestroy
	 * will be killed of aublog is killed.
	 * 
	 * Consequences: NEGATIVE: to find out if the audio file is valid, or how
	 * long it is, this EditBlogEntry now has to go to the database and the
	 * Sdcard, it cant know on its own. POSITIVE: the user can rotate the screen
	 * while dictating, which is very natural since they will pick up and put
	 * down the phone, walk around, maybe biking etc especially if the user is
	 * using a bluetooth.
	 */
	@Override
	protected void onPause() {
		mFreshEditScreen=false;
		tracker.trackEvent(
	            "Event",  // Category
	            "Pause",  // Action
	            "event was paused: "+mAuBlogInstallId, // Label
	            38);       // Value
    	
		if(mURIDeleted == true){
			//do nothing
		}else{
	    	mWebView.loadUrl("javascript:savePostToState()");
	    	saveAsSelfToDB();
		}
		
		
		
		super.onPause();
	}
	/**
	 * put back in an onResume override, watch out for loss of state side effects.
	 */
	@Override
	protected void onResume() {
		if (audioFileUpdateReceiver == null){
			audioFileUpdateReceiver = new AudioFileUpdateReceiver();
		}
		IntentFilter intentFilter = new IntentFilter(REFRESH_AUDIOFILE_INTENT);
		registerReceiver(audioFileUpdateReceiver, intentFilter);
		IntentFilter intentDictSent = new IntentFilter(DICTATION_SENT_INTENT);
		registerReceiver(audioFileUpdateReceiver, intentDictSent);
		IntentFilter intentDictRunning = new IntentFilter(DICTATION_STILL_RECORDING_INTENT);
		registerReceiver(audioFileUpdateReceiver, intentDictRunning);
		IntentFilter intentFilterTrans = new IntentFilter(REFRESH_TRANSCRIPTION_INTENT);
		registerReceiver(audioFileUpdateReceiver, intentFilterTrans);
		
		super.onResume();
	}


	@Override
	protected void onDestroy() {
		//mTts.shutdown();
		// Log.i(TAG, "Method 'onDestroy()' launched");
		tracker.stop();
		if (audioFileUpdateReceiver != null) {
			unregisterReceiver(audioFileUpdateReceiver);
		}
		mFreshEditScreen=false;
		if(!mURIDeleted){
			SharedPreferences prefs = getSharedPreferences(PreferenceConstants.PREFERENCE_NAME, MODE_PRIVATE);
	    	SharedPreferences.Editor editor = prefs.edit();
	    	editor.putString(PreferenceConstants.PREFERENCE_LAST_SELECTED_DRAFT_NODE,mPostId);
	    	editor.commit();
		}
		/*
		 * Stop the player if its playing, this must be in the ondestroy method,
		 * not the onpause method Reason: onpause is called when user opens the
		 * menu and goes to teh settings, resulting in a non-existant
		 * mediaplayer when they come back to the edit blog activity. if they
		 * click on the Play button the edit blog activity closes silently. In
		 * the debugger we get a class missing error type.
		 */
		if (mMediaPlayer != null) {
			mMediaPlayer.release();
			mMediaPlayer = null;
		}
		mPlayingNow = false;
		//saveOrUpdateToDB();
//		mWebView.loadUrl("javascript:savePostToDB()");
		super.onDestroy();
		

	}
	private void saveStateToActivity(String strTitle, String strContent, String strLabels){
//    	if(mDeleted == true){
//    		return;
//    	} //let user edit deleted posts
    	if (!(mPostTitle.equals(strTitle)) ){
    		flagDraftTreeAsNeedingToBeReGenerated();
    	}
    	mPostContent= strContent;
    	mPostTitle=strTitle;
    	mPostLabels=strLabels;
    	if (mLongestEverContent.length() < (strTitle+strContent+strLabels).length() ){
			mLongestEverContent=strContent+strContent+strLabels;
		}
    }
	private void saveAsSelfToDB(){
		mFreshEditScreen = false;
		if (mLongestEverContent.length() < (mPostTitle+mPostContent+mPostLabels).length() ){
			mLongestEverContent=mPostTitle+mPostContent+mPostLabels;
		}
//		if (mDeleted == true){
//			return ;
//		} //alow users to edit deleted nodes. TODO if the node is deleted, change javascript buton onclick to undelete.
    	try{
    		
    		ContentValues values = new ContentValues();
        	values.put(AuBlogHistory.ENTRY_TITLE, mPostTitle);
        	values.put(AuBlogHistory.ENTRY_CONTENT, mPostContent);
        	values.put(AuBlogHistory.ENTRY_LABELS, mPostLabels);
        	values.put(AuBlogHistory.TIME_EDITED, Long.valueOf(System.currentTimeMillis()));
        	values.put(AuBlogHistory.AUDIO_FILE, mAudioResultsFile);
        	//values.put(AuBlogHistory.AUDIO_FILE_STATUS, mAudioResultsFileStatus);
        	getContentResolver().update(mUri, values,null, null);
    		Log.d(TAG, "Post saved to database.");
    		//Toast.makeText(EditBlogEntryActivity.this, "Post " +mUri.getLastPathSegment()+" saved as self to database\n\nTitle: "+mPostTitle+"\nLabels: "+mPostLabels+"\n\nPost: "+mPostContent, Toast.LENGTH_LONG).show();
    		
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
    	tracker.trackEvent(
	            "AuBlogLifeCycleEvent",  // Category
	            "Delete",  // Action
	            "entry was flagged as deleted in the database"+uri.getLastPathSegment()+" : "+mAuBlogInstallId, // Label
	            39);       // Value
    	/*
		 * Flag entry as deleted
		 */
		ContentValues values = new ContentValues();
		values.put(AuBlogHistory.DELETED,"1");//sets deleted flag to true
		/*
		 * TODO decide if want to change the parent node to "trash" so that the entry appears as deleted.
		 * ** if it has children then its children will be "deleted" too.
		 * 
		 */
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
	/**
	 * Inner class which waits to recieve an intent that the audio file has been updated, This intent generally will come from the dictationRecorder, unless someone else's app broadcasts it. 
	 * 
	 * Notes:
	 * -Beware of security hasard of running code in this reviecer.
	 * In this case, ony rechecking the aduio setings and releaseing the media player and reattaching it. 
	 * -Recievers should be registerd in the manifest, but this is an inner class so that it can access the member functions of EditBlogEntryActivity so it 
	 * doesnt need to be registered in the manifest.xml.
	 * 
	 * http://stackoverflow.com/questions/2463175/how-to-have-android-service-communicate-with-activity
	 * http://thinkandroid.wordpress.com/2010/02/02/custom-intents-and-broadcasting-with-receivers/
	 * 
	 * could pass data in the Intent instead of updating database tables
	 * 
	 * @author cesine
	 */
	public class AudioFileUpdateReceiver extends BroadcastReceiver {
	    @Override
	    public void onReceive(Context context, Intent intent) {
	    	//refresh audiofile REFRESH_AUDIOFILE_INTENT is too soon to check because the transcription service 
	    	//will be in the middle of sending the .mp3 when we want to listen to it. instead, wait until the transcription service replys
			if (intent.getAction().equals(DICTATION_SENT_INTENT)) {
				mAudioResultsFileStatus = intent.getExtras().getString(
						DictationRecorderService.EXTRA_AUDIOFILE_STATUS);

				// Do stuff - maybe update my view based on the changed DB
				// contents
				recheckAublogSettings();// if audio settings have changed use
										// the new ones.
				preparePlayerAttachedAudioFile();
				// request transcription from the server, normally put this in a
				// timer in javascript
			}
			if (intent.getAction().equals(DICTATION_SENT_INTENT)) {
				/* find out the status */
				mAudioResultsFileStatus = intent.getExtras().getString(
						DictationRecorderService.EXTRA_AUDIOFILE_STATUS);
				mWebView.loadUrl("javascript:downloadTranscription()");
			}
			if (intent.getAction().equals(DICTATION_STILL_RECORDING_INTENT)) {
				/* if the uri is the uri we are editing, then set its recording to true so the user can click stop 
				 * case 1: we are editing and click home, then open notification and click on it. it takes us to the notifying controller, we click stop, it takes us back to the 
				 * same edit activty that was open (probelm without this is that user can potentially have two versions of edit editing the same muri, where the old one overwrites changes to the new one.
				 * 
				 * case 2: we have left the edit activity, so it no longer has an instance state taht says its recording. if we click on the notification it will(open a new edit?) load the edit from the database
				 * and then call this section whereby the stop button is displayed?
				 * 
				 * case 3: we are in the middle of editing another uri, click home, click on the notification, click on stop and it brings us here, but this is the wrong uri, so nothing happens. and the only way to stop the uri is to have a stop button and an open button. */
				Uri uri = intent.getData();
				if (mUri == uri){
					mRecordingNow = true;
					mWebView.loadUrl("javascript:checkRecordingNow()");
				}
			}
			if (intent.getAction().equals(REFRESH_TRANSCRIPTION_INTENT)) {
				/* open the srt and extract the text */
				mAudioResultsFileStatus = intent.getExtras().getString(
						DictationRecorderService.EXTRA_AUDIOFILE_STATUS);
				if(intent.getData() == mUri){
					mTranscription = "TODO this is what came back from the server.";
					mWebView.loadUrl("javascript:importTranscription(document.getElementById('markItUp').value)");
					
					/* tell javascript to fill in the transcription stuff */
				}else{
					Toast.makeText(EditBlogEntryActivity.this, "Transcription for post "+intent.getData().getLastPathSegment()+" received.", Toast.LENGTH_LONG).show();
					//TODO perhaps give user the option of importing the transcription here, since this is most likely a daughter of the transcription sent since roughly 1-2 minutes have passed.
				}
			}
	        String tmep;
	        tmep = "wait to see if error is here";
			/*
			 * error after this line, dont need a reciever registered in the manifest if its an inner class.
			 * 
			 * java.lang.ClassNotFoundException: ca.ilanguage.aublog.ui.EditBlogEntryActivity.AudioFileUpdateReceiver 
			 * in loader dalvik.system.PathClassLoader[/mnt/asec/ca.ilanguage.aublog-1/pkg.apk]
			 * 
			 *  ReceiverData{intent=Intent {
			 * act=ca.ilanguage
			 * .aublog.intent.action.BROADCAST_DICTATIONSERVICE_FINISHED
			 * cmp=ca.ilanguage
			 * .aublog/.ui.EditBlogEntryActivity.AudioFileUpdateReceiver }
			 * packageName=ca.ilanguage.aublog resultCode=-1 resultData=null
			 * resultExtras=null}
			 */
	    }
	}
	/**
	 * If the media player is instantiated, release it and make it null
	 * 
	 * Then instantiate it, set it to the audio file name and prepare it.
	 */
	public void preparePlayerAttachedAudioFile(){

		if(mMediaPlayer != null){
			mMediaPlayer.release();
	   		mMediaPlayer = null;
		}
		
	   	try {
	   		
	   		/*
	   		 * bug: was not changing the data source here, so decided to reset the audio player completely and
	   		 * reinitialize it
	   		 */
	   		mMediaPlayer = new MediaPlayer();
	        mMediaPlayer.setLooping(true);
			/*
		   	 * assign this audio recording to the media player
		   	 */
			mMediaPlayer.setDataSource(mAudioResultsFile);
			mMediaPlayer.prepareAsync();
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
		} catch (IllegalStateException e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
		}
	}
	public Boolean hasAudioFileAttached(){
		if (mAudioResultsFile.length() > 5 ){
			//Toast.makeText(EditBlogEntryActivity.this,"There is an audio file.", Toast.LENGTH_SHORT).show();
    		return true;
    	}else{
			//Toast.makeText(EditBlogEntryActivity.this,"No audio file.", Toast.LENGTH_SHORT).show();
    		return false;
    	}
	}
	/**
	 * Launches a service to record, it sends the service the name of the audio file to record
	 * The audio directory is created here if it doesn't exist.
	 * It also sends the service the current status message which is relatively empty. 
	 * (recording service started). This status is started fresh in the dictation recording service
	 * but the status generated here is sent as an extra, and saveable to the database to debug
	 * at which point the dictation recorder service does/does not kick in.
	 * 
	 * This method is generally called in the Record button in the javascript, example:
	 * 
    		1. save what the user sees to this post.
    		
    		savePostToTheDB(); 
    		
    		2. create a new daughter post with the same content 
    		so that the edits teh user makes between turning on the recording 
    		and stopping the reocording go with the recording. 
    		
    		savePostAsDaughterToDB(); 
    		
    		3. start the recording
    		
    		Android.startToRecord(document.getElementById('f-title').value);
	 * 
	 * Notes: the user can play audio while recording. When they hit stop the player will stop and reinitalize to the new dictation.
	 * 
	 * @return an internal status message
	 */
	public String beginRecording(){
		//recheckAublogSettings();//check if bluetooth is ready, use it if it is
		mAudioResultsFileStatus = "recordingstarted";
		tracker.trackEvent(
	            "Clicks",  // Category
	            "Button",  // Action
	            "record audio via "+mAudioSource+" : "+mAuBlogInstallId, // Label
	            34);       // Value

		mStartTime=System.currentTimeMillis();
		mDateString = (String) android.text.format.DateFormat.format("yyyy-MM-dd_hh.mm", new java.util.Date());
		mDateString = mDateString.replaceAll("/","-");
		mAudioResultsFile = mAuBlogDirectory+"audio/";
		new File(mAudioResultsFile).mkdirs();
		/* Characters that are OK in a file are described
        by regular expressions as:

         \w - alphanumeric (A-Za-z0-9)
         \. - dot
         \- - dash
         \: - colon
         \; - semicolon
         \# - number sign
         \_ - underscore

       Each \ above must be escaped to allow javac to parse
       it correctly. That's why it looks so bad below.

       Since we want to replace things that are not the above,
       set negation ([^ and ]) is used.
     */  
		String safePostTitleForFileName =  mPostTitle.replaceAll("[^\\w\\.\\-\\_]", "_");
		if(safePostTitleForFileName.length() >= 50){
			safePostTitleForFileName = safePostTitleForFileName.substring(0,49)+"...";
		}
		mAudioResultsFile=mAudioResultsFile+mAuBlogInstallId+"_"+mDateString+"_"+System.currentTimeMillis()+"_"+safePostTitleForFileName+".mp3"; 
		mAudioResultsFile=mAudioResultsFile.replaceAll(" ","-");

		//Start dictation service
		Intent intent = new Intent(this, DictationRecorderService.class);
		intent.setData(mUri);
		intent.putExtra(DictationRecorderService.EXTRA_AUDIOFILE_FULL_PATH, mAudioResultsFile);
		intent.putExtra(DictationRecorderService.EXTRA_AUDIOFILE_STATUS, mAudioResultsFileStatus);
		startService(intent); 
		mRecordingNow = true;

		return "Recording...";
	}
	/**
	 * A function which starts or pauses the media player.
	 * The media player is run in a loop so the user only has two options (Play,Pause) not three (Play,Pause,Stop)
	 * This design choice was made so that the user can transcribe their dictation. If they would like to listen to the audio they can 
	 * open the AuBlog settings to go to the AuBlog folder (its easy to find, its in the root of the SDCard) and play their dictations using the music player.
	 * Users can also use the Music player to make a play list of their dictations if they would like to listen continously to their dictations rather than transcribe them.
	 * 
	 * @return A message for the button which is the oposite of its current state. (ie, if the player is paused, it returns Play, if the player is started, it returns Pause)
	 */
	public String playOrPauseAudioFile(){
		
		if (mMediaPlayer == null) {
			/*
			 * This is called if the user is playing audio while recording a new dictation. 
			 * after the user hits stop record, it will reset the player to the new dictation.
			 */
			recheckAublogSettings();
			preparePlayerAttachedAudioFile();
		} else if (mMediaPlayer.isPlaying()) {
			mMediaPlayer.pause();
			/*
			 * rewind logic doesnt work //if its playing, pause and rewind ~4
			 * seconds int rewindValue = 2; int startPlayingFromSecond
			 * =mMediaPlayer.getCurrentPosition(); if ( startPlayingFromSecond
			 * <= rewindValue){ startPlayingFromSecond=0; }else{
			 * startPlayingFromSecond = startPlayingFromSecond - rewindValue; }
			 * mMediaPlayer.seekTo(startPlayingFromSecond);
			 * mMediaPlayer.prepare();
			 */
			mPlayingNow = false;
			//TODO might be able to recheck audio settings here recheckAublogSettings();
			return "Play";
		}

		// if its not playing, play it
		try {
			mPlayingNow = true;
			mMediaPlayer.start();
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block

			// try to prepare audio again?
			Log.e("Error reading file", e.toString());
		} catch (IllegalStateException e) {
			// TODO Auto-generated catch block
			Log.e("Error reading file", e.toString());
		}
		return "Pause";

	}
	/**
	 * This method sends a stopservice command to the DictationRecorderService. 
	 * The service saves the audio file, and sets the metadata in teh database.
	 * 
	 * TODO This method queries the database to get back the meta information about the recording.
	 * 
	 * Notes: if the service is stopped, and this method continues concurrently its highly possible that it wont 
	 * fetch the final info from the database. 
	 * 
	 * Consequences:
	 *  NOTCRUCIAL: the audio file will likely be the same as the file that was saved to the database when 
	 *  the service started recording, so the edit activity can simply set this as teh data for the media player.
	 *  By the time the javascript renders the play button, and the uesr clicks on play, the service will have saved
	 *  the audio file and finished. 
	 *  
	 *  POTENTIALLYPROBLEMATIC: the status message will most likley not contain any value for the length of the recording. so this variable
	 *  will not be useable until the databse is queried again. 
	 *  TODO schedule another queriy or create a listener for database updates and then query the database?
	 * 
	 * @return
	 */
	public String stopSaveRecording(){
		mAudioResultsFileStatus="recordingstopped";
		mEndTime=System.currentTimeMillis();
		mRecordingNow=false;
		Intent intent = new Intent(this, DictationRecorderService.class);
		stopService(intent);
	   	//cannot savely recheck audio settings here, the service is still using the audio. instead do it in the prepare player function. recheckAublogSettings();
	   	if(mStartTime != null){
	   		mTimeAudioWasRecorded=mEndTime-mStartTime;
	   	}else{
	   		mTimeAudioWasRecorded=(long)999; //use 999 as code name for unknown
	   	}
	   	//Javascript changes the blog content to add the length of the recording 
	   	//Javascript simpulates a click on the save button, so most likely it will be saved as a daughter. 
	   	
	   	tracker.trackEvent(
	            "AuBlogLifeCycleEvent",  // Category
	            "Dictation",  // Action
	            "stop audio recording "+mTimeAudioWasRecorded/100+"sec, in the edit screen.: "+mAuBlogInstallId, // Label
	            35);       // Value
	   	       
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
        /*
         * launch async notification service which sends file to transcription server.
         */
	   	mSendForTranscription=true;
	   	mAudioResultsFileStatus="Recording flagged for transcription";
        
        
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
		/*
		 * TODO the prepartion of the player shoudl wait until the aduio file is ready.
		 * What kind of listner is needed? 
		 * -ContentObserver waiting until service has written final informatoin
		 * -IO listener for the file to stop changing size?
		 * -is there already a listener for when a service really ends? 
		 */
		//preparePlayerAttachedAudioFile();
	   	//TODO grey out the play button so they cant click it until the broadcastreciever finds out the service is done. 
		return "Attached "+mTimeAudioWasRecorded/100+"~ second Recording.\n";
	}
	
	public Long returnTimeRecorded(){
		//Long timePassed = (System.currentTimeMillis()-mStartTime)/1000;
		
		return mTimeAudioWasRecorded;//timePassed+"min";
	}
	
	
	private void saveAsDaughterToDB(String strTitle, String strContent, String strLabels){
		mFreshEditScreen = false;
		/*
    	 * If it has no content, and no attached audio, save it as itself and return
    	 */
    	if ( ( (mPostTitle+mPostContent+mPostTitle).length() < 2 || (strTitle+strContent+strLabels).length()< 2 ) && mAudioResultsFile.length() <5){
			saveStateToActivity(strTitle, strContent, strLabels);
			saveAsSelfToDB();
			tracker.trackEvent(
    	            "AuBlogLifeCycleEvent",  // Category
    	            "Save",  // Action
    	            "save as self:no new text: "+mAuBlogInstallId, // Label
    	            311);       // Value
			return;
    	}
    	try{
    		
        	/*
        	 * else if it has content, then Create daughter
    		 */
        	ContentValues daughterValues = new ContentValues();
        	daughterValues.put(AuBlogHistory.ENTRY_TITLE, strTitle);
        	daughterValues.put(AuBlogHistory.ENTRY_CONTENT, strContent);
        	daughterValues.put(AuBlogHistory.ENTRY_LABELS, strLabels);
        	daughterValues.put(AuBlogHistory.LAST_MODIFIED, Long.valueOf(System.currentTimeMillis()));
        	daughterValues.put(AuBlogHistory.AUDIO_FILE, mAudioResultsFile); //TODO when to blank out the audio results file?
        	daughterValues.put(AuBlogHistory.AUDIO_FILE_STATUS, mAudioResultsFileStatus); //TODO dont need to write the status ever from here?
          	daughterValues.put(AuBlogHistory.PARENT_ENTRY, mUri.getLastPathSegment());	
    		Uri daughterUri = getContentResolver().insert(AuBlogHistory.CONTENT_URI, daughterValues);
    		tracker.trackEvent(
    	            "AuBlogLifeCycleEvent",  // Category
    	            "Save",  // Action
    	            "save as daughter: "+mAuBlogInstallId, // Label
    	            312);       // Value

    		/*
    		 * Set the daughter to the active mUri, and reinitialize the state values to the daughers values
    		 */
    		mPostParent=mUri.getLastPathSegment();
    		//Toast.makeText(EditBlogEntryActivity.this, "Post "+daughterUri.getLastPathSegment()+" saved as daugher of: " +mUri.getLastPathSegment()+" to database\n\nTitle: "+mPostTitle+"\nLabels: "+mPostLabels+"\n\nPost: "+mPostContent, Toast.LENGTH_LONG).show();
    		mUri=daughterUri;
    		getIntent().setData(mUri);
    		saveStateToActivity(strTitle, strContent, strLabels);
    		
    		/*
    		 * If this save to database includes a new audio file, send it for transcription with this 
    		 * daughter uri to edit when it comes back to the edit activity with transcription content.
    		 */
    		if(mSendForTranscription ==true){
//    			Intent intent = new Intent(this, NotifyingTranscriptionIntentService.class);
//	            intent.putExtra(NotifyingTranscriptionService.EXTRA_AUDIOFILE_FULL_PATH, mAudioResultsFile);
//	            intent.putExtra(NotifyingTranscriptionService.EXTRA_SPLIT_TYPE, NotifyingTranscriptionService.SPLIT_ON_SILENCE);
//	            intent.putExtra(NotifyingTranscriptionIntentService.EXTRA_CORRESPONDING_DRAFT_URI_STRING, mUri.toString());
//	            startService(intent); 
    			Toast.makeText(EditBlogEntryActivity.this, "Check your notification area for transcription status. ", Toast.LENGTH_LONG).show();

	            mSendForTranscription = false;
	            mAudioResultsFileStatus="recordingsenttotranscriptionservice";
            }
    		//mFreshEditScreen=true; //should the edit screen be fresh when creating a daughter?
    		mDeleted=false;
    		//mAudioResultsFile=""; //TODO if i do this here, will it overwrite the starto record creation of the aduiofile?
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
	private String downloadTranscription(String strContents){
		mAudioResultsFileStatus=mAudioResultsFileStatus+":::"+"Requested transcription result at "+System.currentTimeMillis();
		Intent intent = new Intent(this, NotifyingTranscriptionIntentService.class);
		intent.setData(mUri);
        intent.putExtra(EditBlogEntryActivity.EXTRA_CURRENT_CONTENTS,strContents);
		intent.putExtra(DictationRecorderService.EXTRA_AUDIOFILE_FULL_PATH, mAudioResultsFile.replace(".mp3","_client.srt"));
        intent.putExtra(NotifyingTranscriptionIntentService.EXTRA_SPLIT_TYPE, NotifyingTranscriptionIntentService.SPLIT_ON_SILENCE);
        intent.putExtra(DictationRecorderService.EXTRA_AUDIOFILE_STATUS, mAudioResultsFileStatus);
        startService(intent); 
		return "Waiting for service to complete.";
		
	}
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
//			mWebView.loadUrl("javascript:savePostToDB()");
	    	if (mLongestEverContent.length() < (mPostTitle+mPostContent+mPostLabels).length() ){
	    		//if the longestevercontenttitle etc is shorter than the concatination of the current title and content, save the current as the longested ever. 
				mLongestEverContent=mPostTitle+mPostContent+mPostLabels;
			}
	    	
			if (mLongestEverContent.length() <= 2 && mAudioResultsFile.length() < 5) {
				// delete the entry the blog entry  if the user
				// never added anything and there is no attached audio file. this should prevent having empty entrys
				// in the database, but stillkeep entries that are zeroed out and had
				// content before, or more imprtantly, had daughters.
				/*
				 * find all nodes with this node as its parent, if it has no
				 * daughters, just delete it.
				 */
				Cursor cursor = managedQuery(AuBlogHistory.CONTENT_URI,
						PROJECTION, AuBlogHistory.PARENT_ENTRY + "=" + mUri.getLastPathSegment(),
						null, null);
				if (cursor.getCount() < 1) {
					Toast.makeText(
							EditBlogEntryActivity.this,
							"Discarding entry " + mUri.getLastPathSegment(),// + " it's empty and it has no daughters.",
							Toast.LENGTH_LONG).show();
					getContentResolver().delete(mUri, null, null);
					mURIDeleted = true;
					flagDraftTreeAsNeedingToBeReGenerated();//activity finishes when mURI is deleted so no problems
				} else {
					//Toast.makeText(EditBlogEntryActivity.this,"Not Deleting " + mUri.getLastPathSegment()+ " it has" + cursor.getCount()+ " daughters.", Toast.LENGTH_LONG).show();
				}
			}
			mFreshEditScreen =true;
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
	public String askUserIfImport(final String currentPostContents){
		mTranscriptionAndContents= "";
		if (mTranscription != null){
			if(mTranscription.length() <1){
				return "";
			}else{
					
				OnClickListener yes = new OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						mTranscriptionAndContents = mTranscription+currentPostContents;
					}
				};
				OnClickListener no = new OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						mTranscriptionAndContents= "";				
					}
				};
				/*
				prompt user do you want to import
				*/
				//if yes
				/*
				convert srt into text and append to blog.
				*/
				Dialog dialog = new AlertDialog.Builder(this)
				.setTitle("Import Transcription")
				.setPositiveButton("Import", yes)
				.setNegativeButton("Don't Import", no)
				.setMessage("Here is the what your entry will look like.\n\n"+mTranscription+currentPostContents).create();
				dialog.show();
			}
		}
		return "";
	}
    @Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Hold on to this
		mMenu = menu;

		// Inflate the currently selected menu XML resource.
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.edit_blog_entry_menu, menu);


		return true;
	}
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		// For "Title only": Examples of matching an ID with one assigned in
		//                   the XML
		case R.id.transcription_status:
			Dialog dialog = new AlertDialog.Builder(this)
				.setTitle("Status")
				.setPositiveButton("Ok", null)
				.setMessage("Here is the current status of your dictation and its transcription.\n\n"+mAudioResultsFileStatus.replaceAll(":::", "\n  ")).create();
			dialog.show();
			return true;
		case R.id.open_settings:
			tracker.trackPageView("/settingsScreen");
			tracker.trackEvent(
		            "Clicks",  // Category
		            "Button",  // Action
		            "clicked settings in the edit blog entry menu: "+mAuBlogInstallId, // Label
		            34);       // Value
			Intent i = new Intent(getBaseContext(),	SetPreferencesActivity.class);
			startActivity(i);
			return true;
		case R.id.new_entry:
			tracker.trackPageView("/editBlogEntryScreen");
			tracker.trackEvent(
		            "Clicks",  // Category
		            "Button",  // Action
		            "clicked new entry in the edit blog entry menu: "+mAuBlogInstallId, // Label
		            32);       // Value

			Intent intent = new Intent(getBaseContext(), EditBlogEntryActivity.class);

			Uri uri = getContentResolver().insert(AuBlogHistory.CONTENT_URI,
					null);
			// If we were unable to create a new blog entry, then just finish
			// this activity. A RESULT_CANCELED will be sent back to the
			// original activity if they requested a result.
			if (uri == null) {
				Log.e(TAG, "Failed to insert new blog entry into "
						+ getIntent().getData());
				Toast.makeText(
						EditBlogEntryActivity.this,
						"Failed to insert new blog entry into the database. You can go to your devices settings, choose Aublog and click Clear data to re-create the database."
								+ getIntent().getData() + " with this uri"
								+ AuBlogHistory.CONTENT_URI, Toast.LENGTH_LONG)
						.show();
				tracker.trackEvent(
			            "Database",  // Category
			            "Bug",  // Action
			            "cannot create new entry in the edit blog entry menu: "+mAuBlogInstallId, // Label
			            30);       // Value

			} else {
				intent.setData(uri);
				startActivity(intent);
				finish();
			}
			return true;
		default:
			// Do nothing

			break;
		}

		return false;
	}

}