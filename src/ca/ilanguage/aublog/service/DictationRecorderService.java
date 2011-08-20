package ca.ilanguage.aublog.service;

import java.io.IOException;

import com.google.android.apps.analytics.GoogleAnalyticsTracker;

import ca.ilanguage.aublog.R;
import ca.ilanguage.aublog.db.AuBlogHistoryDatabase.AuBlogHistory;
import ca.ilanguage.aublog.preferences.NonPublicConstants;
import ca.ilanguage.aublog.preferences.PreferenceConstants;
import ca.ilanguage.aublog.ui.EditBlogEntryActivity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.media.AudioManager;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.IBinder;
import android.widget.Toast;

/**
 * A service to record and save a dictation to the SDCard and its filename and status messages to the 
 * Database.
 * 
 * Can be called from anywhere in Aublog. It will modify the database entry of the audiofile, so if the 
 * activity is displaying or using the audiofile info of a blog entry, the activity should fetch that info from the database after calling stop. 
 * 
 * Notes: 
 *  *the service is started with startForeground which means it will display a notification in the notification area that the uesr can click on
 * while its running.
 *  *the service can be turned off by the operating system if its low in memory or for other reasons. In these cases the service tries to 
 *  save the audio, but there is no guarantee that the entire process will be carried out. Make sure the audio file exists before you play it.
 *  *the service saves to the database twice, once just before the recorder is started, and once just after the recorder is stopped (which generally coincides with when the service is killed)
 * TODO could be implemented as an IntentService.
 * 
 * Sample client code:
     //Start dictation service
 	 Intent intent = new Intent(this, DictationRecorderService.class);
 	 intent.setData(mUri);
 	 intent.putExtra(DictationRecorderService.EXTRA_AUDIOFILE_FULL_PATH, mAudioResultsFile);
     intent.putExtra(DictationRecorderService.EXTRA_AUDIOFILE_STATUS, mAudioResultsFileStatus);
     startService(intent); 
	 mRecording = true;
	 
	 //Stop dictation service
	 if (mRecording == true){
		 Intent intent = new Intent(this, DictationRecorderService.class);
		 stopService(intent);
	 }
 * 
 * @author gina
 *
 */
public class DictationRecorderService extends Service {
	
	GoogleAnalyticsTracker tracker;
	
	protected static String TAG = "DictationRecorderService";
	private NotificationManager mNM;
	private Notification mNotification;
	private int NOTIFICATION = 7029;
	private PendingIntent mContentIntent;

	private int mAuBlogIconId = R.drawable.stat_aublog;
	
	private String mAudioResultsFile ="";
	private String mAudioResultsFileStatus ="";
	public static final String EXTRA_AUDIOFILE_FULL_PATH = "audioFilePath";
	public static final String EXTRA_AUDIOFILE_STATUS = "audioFileStatus";
	

	private Long mStartTime;
    private Long mEndTime;
    private Long mTimeAudioWasRecorded;
    private String mAudioSource;//bluetooth(record,play), phone(recordmic, play earpiece) for privacy, speaker(record mic, play speaker)
    private Boolean mUseBluetooth;
    private Boolean mUsePhoneEarPiece;
    private String mDateString ="";
    private String mAuBlogInstallId;
    
    private String mAuBlogDirectory = PreferenceConstants.OUTPUT_AUBLOG_DIRECTORY;//"/sdcard/AuBlog/";
    private MediaRecorder mRecorder;
    private AudioManager mAudioManager;
    private Boolean mRecordingNow = false;
    
  //uri of the entry being edited.
	private Uri mUri;
	private String mDBLastModified="";
	private Cursor mCursor;
	private  String[] PROJECTION = new String[] {
			AuBlogHistory._ID, //0
			AuBlogHistory.LAST_MODIFIED,
			AuBlogHistory.TIME_EDITED,//2
			AuBlogHistory.AUDIO_FILE,
			AuBlogHistory.AUDIO_FILE_STATUS//4
		};

	
	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return null;
	}



	@Override
	public void onCreate() {
		super.onCreate();
		mNM = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		// The PendingIntent to launch our activity if the user selects this notification
        mContentIntent = PendingIntent.getActivity(this, 0,
                new Intent(this, NotifyingController.class), 0);
		mNotification = new Notification(mAuBlogIconId, "AuBlog Dictation in progress", System.currentTimeMillis());
		mNotification.setLatestEventInfo(this, "AuBlog Dictation", "Recording...", mContentIntent);
		mNotification.flags  |= Notification.FLAG_AUTO_CANCEL;
        mAudioManager = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
		tracker = GoogleAnalyticsTracker.getInstance();
	    // Start the tracker in manual dispatch mode...
	    tracker.start(NonPublicConstants.NONPUBLIC_GOOGLE_ANALYTICS_UA_ACCOUNT_CODE, 20, this);
	}



	@Override
	public void onDestroy() {
		saveRecording();
		mNM.cancel(NOTIFICATION);
		super.onDestroy();
		
	}



	@Override
	public void onLowMemory() {
		saveRecording();
		super.onLowMemory();
	}



	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		startForeground(startId, mNotification);
		mUri = intent.getData();
		/*
		 * get data from extras bundle, store it in the member variables
		 */
		try {
			mAudioResultsFile = intent.getExtras().getString(EXTRA_AUDIOFILE_FULL_PATH);
			mAudioResultsFileStatus = intent.getExtras().getString(EXTRA_AUDIOFILE_STATUS);
			//Discard status which was sent.
			mAudioResultsFileStatus = "Recording service running";
		} catch (Exception e) {
			//Toast.makeText(SRTGeneratorActivity.this, "Error "+e,Toast.LENGTH_LONG).show();
		}
		if (mAudioResultsFile.length() > 0) {
			mAudioResultsFileStatus= mAudioResultsFileStatus+":::"+mAudioResultsFile;
		}else{
			mAudioResultsFileStatus =mAudioResultsFileStatus +":::"+"Nofile";
		}
		/*
		 * Set up bluetooth or phone mic recording device
		 */
		SharedPreferences prefs = getSharedPreferences(PreferenceConstants.PREFERENCE_NAME, MODE_PRIVATE);
	    mUseBluetooth = prefs.getBoolean(PreferenceConstants.PREFERENCE_USE_BLUETOOTH_AUDIO, false);
	    mUsePhoneEarPiece = prefs.getBoolean(PreferenceConstants.PREFERENCE_USE_PHONE_EARPIECE_AUDIO, false);
	    if(mUseBluetooth){
			/*
	    	 * As the SCO connection establishment can take several seconds, applications should not rely on the connection to be available when the method returns but instead register to receive the intent ACTION_SCO_AUDIO_STATE_CHANGED and wait for the state to be SCO_AUDIO_STATE_CONNECTED.
	    	 Even if a SCO connection is established, the following restrictions apply on audio output streams so that they can be routed to SCO headset: - the stream type must be STREAM_VOICE_CALL - the format must be mono - the sampling must be 16kHz or 8kHz

				Similarly, if a call is received or sent while an application is using the SCO connection, the connection will be lost for the application and NOT returned automatically when the call ends.
			* Notes:
			* Use of the blue tooth does not affect the ability to recieve a call while using the app,
			* However, the app will not have control of hte bluetooth connection when teh phone call comes back. The user must exit the Edit Blog activity.
			* 
	    	 */
	    	mAudioManager.startBluetoothSco();
	    	mAudioManager.setSpeakerphoneOn(false);
	    	mAudioManager.setBluetoothScoOn(true);
	    	mAudioManager.setMode(AudioManager.MODE_IN_CALL);
	    	mAudioSource= "maybebluetooth";

		}
		if(mUsePhoneEarPiece){
	    	mAudioManager.setSpeakerphoneOn(false);
	    	mAudioManager.setMode(AudioManager.MODE_IN_CALL);
	    	mAudioSource= "microphone";
		}
		/*
		 * set the installid for appending to the labels
		 */
		mAuBlogInstallId = prefs.getString(PreferenceConstants.AUBLOG_INSTALL_ID, "0");
		
		tracker.trackEvent(
	            "Clicks",  // Category
	            "Button",  // Action
	            "Record audio via "+mAudioSource+" : "+mAuBlogInstallId, // Label
	            734);       // Value
		/*
		 * turn on the recorder
		 */
		mRecordingNow = true;
		mStartTime=System.currentTimeMillis();
		mAudioResultsFileStatus = mAudioResultsFileStatus+":::"+"Recording started."+":::"+mAudioSource+":::"+mStartTime;
		
		saveMetaDataToDatabase();
		mRecorder = new MediaRecorder();
		try {
	    	//http://www.benmccann.com/dev-blog/android-audio-recording-tutorial/
			mRecordingNow = true;
	    	mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
		    mRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
		    mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
		    mRecorder.setOutputFile(mAudioResultsFile);
		    mRecorder.prepare();
		    mStartTime=System.currentTimeMillis();
		    mRecorder.start();
		} catch (IllegalStateException e) {
			// TODO Auto-generated catch block
			tracker.trackEvent(
    	            "Record",  // Category
    	            "Bug",  // Action
    	            "The App cannot record audio, maybe the Android has a strange audio configuration?" +e+" : "+mAuBlogInstallId, // Label
    	            7301);       // Value
		} catch (IOException e) {
			// TODO Auto-generated catch block
			tracker.trackEvent(
    	            "Record",  // Category
    	            "Bug",  // Action
    	            "The App cannot save audio, maybe the Android is attached to a computer?" +e+" : "+mAuBlogInstallId, // Label
    	            7302);       // Value
		}
		
		//autofilled by eclipsereturn super.onStartCommand(intent, flags, startId);
		// We want this service to continue running until it is explicitly
        // stopped, so return sticky.
		return START_STICKY;
	}

	private void saveMetaDataToDatabase(){
		/*
		 * Save to database
		 */
        mCursor = getContentResolver().query(mUri, PROJECTION, null, null, null);
		if (mCursor != null) {
			// Requery in case something changed while paused (such as the title)
			mCursor.requery();
            // Make sure we are at the one and only row in the cursor.
            mCursor.moveToFirst();
			try {
					//compare the last time this service modified the database, 
					//if its earlier than the database modified time, then someone else has written in this entry in the database (they might have written in different fields, in just incase if they wrote in the audiofile or audiofilestatus, 
					int result = mDBLastModified.compareTo(mCursor.getString(1));
					if ( result < 0){
						//some other activity or service has edited the important fields in the database!
						//if they edited the filename, over write it with this file name because this one is in process of recording. 
						//if they changed the status message, add their status message and a note about "being walked on" 
						mAudioResultsFileStatus = mAudioResultsFileStatus+":::Walking on this status message that was in the database.---"+ mCursor.getString(4)+"---";
					
					}
					ContentValues values = new ContentValues();
		        	values.put(AuBlogHistory.AUDIO_FILE, mAudioResultsFile);
		        	values.put(AuBlogHistory.AUDIO_FILE_STATUS, mAudioResultsFileStatus);
		        	getContentResolver().update(mUri, values,null, null);
		        	mDBLastModified = Long.toString(System.currentTimeMillis());
		        	getContentResolver().notifyChange(AuBlogHistory.CONTENT_URI, null);
		        	
		        	// Tell the user we saved recording meta info to the database.
		            //Toast.makeText(this, "Audiofile info saved to DB.", Toast.LENGTH_SHORT).show();
		            //mNotification.setLatestEventInfo(this, "AuBlog Dictation", "Saved to DB", mContentIntent);
		    		
		        	
			} catch (IllegalArgumentException e) {
				
			} catch (Exception e) {
				
			}
			
			
			
			
		}//end if where cursor has content.
		
	}

	private void saveRecording(){
		String appendToContent ="";
		if (mRecorder != null) {
			/*
			 * if the recorder is running, save everything essentially simulating a click on the save button in the UI
			 */
			if(mRecordingNow == true){
				/*
				 * Save recording
				 */
				mAudioResultsFileStatus=mAudioResultsFileStatus+":::"+"Recording stopped.";
				mEndTime=System.currentTimeMillis();
				mRecordingNow=false;
			   	mRecorder.stop();
			   	mRecorder.release();
			   	mRecorder = null;
			   	mTimeAudioWasRecorded=mEndTime-mStartTime;
			   	tracker.trackEvent(
			            "AuBlogLifeCycleEvent",  // Category
			            "Dictation",  // Action
			            "Saved audio recording "+mTimeAudioWasRecorded/100+"sec: "+mAuBlogInstallId, // Label
			            735);       // Value
			   	       
				// Tell the user we saved recording.
	            //Toast.makeText(this, "Recording saved to SDCard.", Toast.LENGTH_SHORT).show();
	            //mContentIntent = open file browser? or open in music player playlist?
			   	//mNotification.setLatestEventInfo(this, "AuBlog Dictation", "Saved in AuBlog folder", mContentIntent);
			   	
			   	appendToContent ="Attached a "+mTimeAudioWasRecorded/100+" second Recording.\n";
			   	mAudioResultsFileStatus=mAudioResultsFileStatus+":::"+appendToContent;
	            mAudioResultsFileStatus=mAudioResultsFileStatus+":::"+"Recording flagged for transcription.";
	            sendForTranscription();
				saveMetaDataToDatabase();
			}else{
				//this should not run
				mRecorder.release(); //this is called in the stop save recording
	            mRecorder = null;
			}
		}
	}
	private void sendForTranscription(){
		mAudioResultsFileStatus=mAudioResultsFileStatus+":::"+"Sent to transcription service.";
		Intent intent = new Intent(this, NotifyingTranscriptionIntentService.class);
		intent.setData(mUri);
        intent.putExtra(EXTRA_AUDIOFILE_FULL_PATH, mAudioResultsFile);
        intent.putExtra(NotifyingTranscriptionIntentService.EXTRA_SPLIT_TYPE, NotifyingTranscriptionIntentService.SPLIT_ON_SILENCE);
        intent.putExtra(EXTRA_AUDIOFILE_STATUS, mAudioResultsFileStatus);
        startService(intent); 
       
	}

}
