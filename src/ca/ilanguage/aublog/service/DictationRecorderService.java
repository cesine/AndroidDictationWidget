package ca.ilanguage.aublog.service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import com.google.android.apps.analytics.GoogleAnalyticsTracker;

import ca.ilanguage.aublog.R;
import ca.ilanguage.aublog.db.AuBlogHistoryDatabase.AuBlogHistory;
import ca.ilanguage.aublog.preferences.NonPublicConstants;
import ca.ilanguage.aublog.preferences.PreferenceConstants;
import ca.ilanguage.aublog.ui.EditBlogEntryActivity;
import ca.ilanguage.aublog.ui.MainMenuActivity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.media.AudioManager;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.IBinder;

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
	public static final String EXTRA_DELEGATE_KILL_AUBLOG_TO_YOU ="killAublog";
	public static final String EXTRA_DEVICE_INFO = "deviceInfo";
	

	private Long mStartTime;
    private Long mEndTime;
    private Long mTimeAudioWasRecorded;
    private String mAudioSource ="internal mic";;//bluetooth(record,play), phone(recordmic, play earpiece) for privacy, speaker(record mic, play speaker)
    private Boolean mUseBluetooth;
    private Boolean mUsePhoneEarPiece;
    private String mDateString ="";
    private String mAuBlogInstallId;
    private String mDeviceInfo="";
    
    private String mAuBlogDirectory = PreferenceConstants.OUTPUT_AUBLOG_DIRECTORY;//"/sdcard/AuBlog/";
    private MediaRecorder mRecorder;
    private AudioManager mAudioManager;
    private Boolean mRecordingNow = false;
    private RecordingReceiver audioFileUpdateReceiver;
    private Boolean mKillAuBlog;
    
	
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
		if (audioFileUpdateReceiver == null){
			audioFileUpdateReceiver = new RecordingReceiver();
		}
		IntentFilter intentDictRunning = new IntentFilter(MainMenuActivity.IS_DICTATION_STILL_RECORDING_INTENT);
		registerReceiver(audioFileUpdateReceiver, intentDictRunning);
		IntentFilter intentkill = new IntentFilter(MainMenuActivity.KILL_AUBLOG_INTENT);
		registerReceiver(audioFileUpdateReceiver, intentkill);

		mNM = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		// The PendingIntent to launch our activity if the user selects this notification
		Intent i = new Intent(this, NotifyingController.class);
		i.setData(mUri);
		mContentIntent = PendingIntent.getActivity(this, 0, i, 0);
		
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
		sendForTranscription();
		saveMetaDataToDatabase();
		mNM.cancel(NOTIFICATION);
		Intent i = new Intent(EditBlogEntryActivity.REFRESH_AUDIOFILE_INTENT);
		i.putExtra(DictationRecorderService.EXTRA_AUDIOFILE_STATUS, mAudioResultsFileStatus);
		sendBroadcast(i);
		
		/*pass on the kill aublog message to the transcription server*/
		if (mKillAuBlog != null){
			if(mKillAuBlog){
				Intent intent = new Intent(MainMenuActivity.KILL_AUBLOG_INTENT);
				sendBroadcast(intent);
			}
		}
		super.onDestroy();
		if (audioFileUpdateReceiver != null) {
			unregisterReceiver(audioFileUpdateReceiver);
		}
		tracker.stop();
//		if (mUseBluetooth){
//			mAudioManager.setBluetoothScoOn(false);
//		}
//		mAudioManager.setMode(AudioManager.MODE_NORMAL);
//		mAudioManager.setSpeakerphoneOn(true);
		
	}



	@Override
	public void onLowMemory() {
		saveRecording();
		sendForTranscription();
		saveMetaDataToDatabase();
		mNM.cancel(NOTIFICATION);
		Intent i = new Intent(EditBlogEntryActivity.REFRESH_AUDIOFILE_INTENT);
		i.putExtra(DictationRecorderService.EXTRA_AUDIOFILE_STATUS, mAudioResultsFileStatus);
		sendBroadcast(i);
		
		/*pass on the kill aublog message to the transcription server*/
		if (mKillAuBlog != null){
			if(mKillAuBlog){
				Intent intent = new Intent(MainMenuActivity.KILL_AUBLOG_INTENT);
				sendBroadcast(intent);
			}
		}
		super.onLowMemory();
		if (audioFileUpdateReceiver != null) {
			unregisterReceiver(audioFileUpdateReceiver);
		}
//		mAudioManager.setMode(AudioManager.MODE_NORMAL);
//		mAudioManager.setSpeakerphoneOn(true);
	}

	public class RecordingReceiver extends BroadcastReceiver {
	    @Override
	    public void onReceive(Context context, Intent intent) {
	    	/*
	    	 * If main menu asks if you're recording (it is trying to close aublog), reply that yes, you are still recording.
	    	 */
	    	if (intent.getAction().equals(MainMenuActivity.IS_DICTATION_STILL_RECORDING_INTENT)) {
	    		Intent i = new Intent(EditBlogEntryActivity.DICTATION_STILL_RECORDING_INTENT);
				i.setData(mUri);
				i.putExtra(DictationRecorderService.EXTRA_AUDIOFILE_STATUS, mAudioResultsFileStatus);
				sendBroadcast(i);
				if (mKillAuBlog != null){
					if(mKillAuBlog){
						Intent inten = new Intent(MainMenuActivity.KILL_AUBLOG_INTENT);
						sendBroadcast(inten);
					}
				}
	    	}
	    	if (intent.getAction().equals(MainMenuActivity.KILL_AUBLOG_INTENT)) {
	    		mKillAuBlog = true;
	    		if (mKillAuBlog != null){
	    			if(mKillAuBlog){
	    				Intent inten = new Intent(MainMenuActivity.KILL_AUBLOG_INTENT);
	    				sendBroadcast(inten);
	    			}
	    		}
	    	}
	    	
	   	}
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Intent i = new Intent(EditBlogEntryActivity.DICTATION_STILL_RECORDING_INTENT);
		i.setData(mUri);
		i.putExtra(DictationRecorderService.EXTRA_AUDIOFILE_STATUS, mAudioResultsFileStatus);
		sendBroadcast(i);

		startForeground(startId, mNotification);
		mUri = intent.getData();
		/*
		 * get data from extras bundle, store it in the member variables
		 */
		try {
			mAudioResultsFile = intent.getExtras().getString(EXTRA_AUDIOFILE_FULL_PATH);
			mAudioResultsFileStatus = intent.getExtras().getString(EXTRA_AUDIOFILE_STATUS);
			mDeviceInfo = intent.getExtras().getString(EXTRA_DEVICE_INFO);
			//Discard status which was sent.
			mAudioResultsFileStatus = "Recording service running";
		} catch (Exception e) {
			//Toast.makeText(SRTGeneratorActivity.this, "Error "+e,Toast.LENGTH_LONG).show();
		}
		if (mAudioResultsFile.length() > 0) {
			mAudioResultsFileStatus= mAudioResultsFileStatus+":::Audio file name: "+mAudioResultsFile;
		}else{
			mAudioResultsFile="/sdcard/temp.mp3";
			mAudioResultsFileStatus =mAudioResultsFileStatus +":::"+"Audio file name: No file recieved from AuBlog using: "+mAudioResultsFile;
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
//	    	mAudioManager.startBluetoothSco();
//	    	mAudioManager.setSpeakerphoneOn(false);
//	    	mAudioManager.setBluetoothScoOn(true);
//	    	mAudioManager.setMode(AudioManager.MODE_IN_COMMUNICATION);
	    	mAudioSource= "maybe bluetooth";

		}
		if(mUsePhoneEarPiece){
//	    	mAudioManager.setSpeakerphoneOn(false);
//	    	mAudioManager.setMode(AudioManager.MODE_IN_COMMUNICATION);
	    	mAudioSource= "internal mic";
		}
		if(mAudioManager.isWiredHeadsetOn()){
			mAudioSource = mAudioSource + "or maybe headset";
		}
		
		/*
		 * set the installid for appending to the labels
		 */
		mAuBlogInstallId = prefs.getString(PreferenceConstants.AUBLOG_INSTALL_ID, "0");
		
		tracker.trackEvent(
	            mAuBlogInstallId,  // Category
	            "DictationStarted",  // Action
	            "Record audio via "+mAudioSource+" : "+System.currentTimeMillis() +" : "+mAuBlogInstallId, // Label
                  (int)System.currentTimeMillis());       // Value
 
		/*
		 * turn on the recorder
		 */
		mRecordingNow = true;
		mStartTime=System.currentTimeMillis();
		
		mAudioResultsFileStatus = mAudioResultsFileStatus+":::"+"Recording started."+":::Audio source: "+mAudioSource+":::Device info: "+mDeviceInfo+":::Start time: "+mStartTime;
		
		saveMetaDataToDatabase();
		mRecorder = new MediaRecorder();
		try {
	    	//http://www.benmccann.com/dev-blog/android-audio-recording-tutorial/
			mRecordingNow = true;
	    	mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
	    	mRecorder.setAudioChannels(1); //mono
	    	mRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
		    int sdk =  android.os.Build.VERSION.SDK_INT;
		    // gingerbread and up can have wide band ie 16,000 hz recordings (much better for transcription)
		    if( sdk >= 10){
		    	mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_WB);
		    	mRecorder.setAudioSamplingRate(16000);
		    }else{
		    	// other devices will have to use narrow band, ie 8,000 hz (same quality as a phone call)
			    mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
		    }
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
						if (! (mAudioResultsFileStatus.contains(mCursor.getString(4))) ){
							mAudioResultsFileStatus = mAudioResultsFileStatus+":::Walking on this status message that was in the database.--__"+ mCursor.getString(4)+"-__";
						}
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
				try{
			   	mRecorder.stop();
			   	mRecorder.release();
				}catch (Exception e) {
					mAudioResultsFileStatus=mAudioResultsFileStatus+":::"+"Recording not saved, your device does not support 16,000 hz recording.: "+e;
				}
			   	mRecorder = null;
			   	mTimeAudioWasRecorded=mEndTime-mStartTime;
				
				tracker.trackEvent(
						mAuBlogInstallId,  // Category
			            "Dictation",  // Action
			            "Dictation saved by service. : "+System.currentTimeMillis() +" : "+mAuBlogInstallId, // Label
			            (int)System.currentTimeMillis());       // Value
				
			   	       
				// Tell the user we saved recording.
	            //Toast.makeText(this, "Recording saved to SDCard.", Toast.LENGTH_SHORT).show();
	            //mContentIntent = open file browser? or open in music player playlist?
			   	//mNotification.setLatestEventInfo(this, "AuBlog Dictation", "Saved in AuBlog folder", mContentIntent);
			   	
			   	appendToContent ="Attached a "+mTimeAudioWasRecorded/100+" second Recording.\n";
			   	mAudioResultsFileStatus=mAudioResultsFileStatus+":::"+appendToContent;
	            mAudioResultsFileStatus=mAudioResultsFileStatus+":::"+"Recording flagged for transcription.";
	            
			}else{
				//this should not run
				mRecorder.release(); //this is called in the stop save recording
	            mRecorder = null;
			}
		}
	}
	private void sendForTranscription(){
		mAudioResultsFileStatus=mAudioResultsFileStatus+":::"+"Sent to transcription service.";
		/*create an empty subtitles file */
		File outSRTFile =  new File(mAudioResultsFile.replace(".mp3","_client.srt"));
		FileOutputStream outSRT;
		try {
			outSRT = new FileOutputStream(outSRTFile,false);
			outSRT.write("0:00:00.000,0:00:00.000\n".getBytes());
			outSRT.write(mAudioResultsFileStatus.getBytes());
			outSRT.write("\n\n".getBytes());
			outSRT.flush();
			outSRT.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
			
		}
		Intent intent = new Intent(this, NotifyingTranscriptionIntentService.class);
		intent.setData(mUri);
        intent.putExtra(EXTRA_AUDIOFILE_FULL_PATH, mAudioResultsFile);
        intent.putExtra(NotifyingTranscriptionIntentService.EXTRA_SPLIT_TYPE, NotifyingTranscriptionIntentService.SPLIT_ON_SILENCE);
        /*pass on the kill aublog message to the transcription server*/
        intent.putExtra(EXTRA_AUDIOFILE_STATUS, mAudioResultsFileStatus);
        //always promt user if they want to import the transcription on the first round of transcription sent to the server. 
        //even though the transcription will be minimal, new users will realize that they will be prompted when further results are ready.
        //can put a settings like 5 times, and the set it to false because the user has learned how to use the aublog. 
        intent.putExtra(EditBlogEntryActivity.EXTRA_PROMPT_USER_TO_IMPORT_TRANSCRIPTION_INTO_BLOG, true);
        startService(intent); 
		
		tracker.trackEvent(
				mAuBlogInstallId,  // Category
	            "Transcription",  // Action
	            "Transcription requested by dictation by service.: "+System.currentTimeMillis() +" : "+mAuBlogInstallId, // Label
	            (int)System.currentTimeMillis());       // Value

	}

}
