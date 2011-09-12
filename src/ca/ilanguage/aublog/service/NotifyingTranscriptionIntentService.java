package ca.ilanguage.aublog.service;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;

import ca.ilanguage.aublog.R;
import ca.ilanguage.aublog.db.AuBlogHistoryDatabase.AuBlogHistory;
import ca.ilanguage.aublog.preferences.NonPublicConstants;
import ca.ilanguage.aublog.preferences.PreferenceConstants;
import ca.ilanguage.aublog.ui.EditBlogEntryActivity;
import ca.ilanguage.aublog.ui.MainMenuActivity;
import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.media.AudioManager;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.net.NetworkInfo.State;
import android.util.Log;

public class NotifyingTranscriptionIntentService extends IntentService {
	protected static String TAG = "NotifyingTranscriptionIntentService";

	private NotificationManager mNM;
	private Notification mNotification;
	private int NOTIFICATION = 7030;
	private Boolean mShowNotification = true;
	private PendingIntent mContentIntent;
	private int mAuBlogIconId = R.drawable.stat_aublog;  
	public NotifyingTranscriptionIntentService() {
		super(TAG);
	}

	private Boolean mTranscriptionReturned =false;
	private int mMaxFileUploadOverMobileNetworkSize = 0;
	private int mMaxUploadFileSize = 15000000;  // Set maximum upload size to 1.5MB roughly 15 minutes of audio, 
	// users shouldn't abuse transcription service by sending meetings and other sorts of audio.
	// If you change this value, change the value in the arrays.xml as well look for:
	// 15 minutes (AuBlog\'s max transcription length)
	private String mAudioFilePath ="";
	private String mAudioResultsFileStatus="";
	private Uri mUri;
	private String mAuBlogInstallId;
	private String mPostContents="";
	private Boolean mKillAuBlog;
	private AudioManager mAudioManager;
	private KillAuBlogReciever mKillAublogReceiver;
	private Boolean mAskUserImport;
	private String mDBLastModified="";
	private Cursor mCursor;
	private  String[] PROJECTION = new String[] {
			AuBlogHistory._ID, //0
			AuBlogHistory.LAST_MODIFIED,
			AuBlogHistory.TIME_EDITED,//2
			AuBlogHistory.AUDIO_FILE,
			AuBlogHistory.AUDIO_FILE_STATUS//4
	};
	private String mFileNameOnServer="";
	private int mSplitType = 0;
	private ArrayList<String> mTimeCodes;
	private String mTranscription = "";
	private String mTranscriptionStatus = "";

	public static final String EXTRA_RESULTS = "splitUpResults";
	public static final String EXTRA_SPLIT_TYPE = "splitOn";

	/**
	 * Splitting on Silence is relatively quick it only requires mathematic calculation on the audio sample, 
	 * this is used by default by all other split types.
	 */
	public static final int SPLIT_ON_SILENCE = 1;
	/**
	 * Subtitles should not exceed a certain length to fit on the screen, use this if you actually have the goal of generating subtitles
	 */
	public static final int SPLIT_ON_TOO_MANY_CHARS = 9;
	/**
	 * Warning: using prosodic cues requires more audio analysis (fourier transforms FFT) and so they will slow the service down substantially
	 */
	public static final int SPLIT_ON_ANY_PROSODIC_CUE = 2;

	/*
	 * List of Optional Prosodic cues
	 * 
	 * For more info: 
	 * 		http://en.wikipedia.org/wiki/Prosody_(linguistics)
	 * For more bleeding-edge info: 
	 * 		Experimental and Theoretical Advances in Prosody Conference http://prosodylab.org/etap/
	 */
	public static final int SPLIT_ON_LENGTHENED_VOWEL = 3;
	public static final int SPLIT_ON_LENGHTENED_CONSONANT = 4;
	public static final int SPLIT_ON_LENGTHENED_ANYTHING = 5;
	public static final int SPLIT_ON_GLOTTALIZATION = 6;
	public static final int SPLIT_ON_BREATH = 7;
	/**
	 * For those speakers of "valley-girl" English prosodic phrases can be 
	 * discovered using upspeak also known as: {high rising terminal (HRT), uptalk, upspeak, rising inflection or high rising intonation }
	 * 
	 * For more info: http://en.wikipedia.org/wiki/High_rising_terminal
	 */
	public static final int SPLIT_ON_UPSPEAK = 8;

	// Use a layout id for a unique identifier
	private static int AUBLOG_NOTIFICATIONS = R.layout.status_bar_notifications;
	private String mNotificationMessage;


	@Override
	public void onCreate() {
		if(mKillAublogReceiver == null){
			mKillAublogReceiver = new KillAuBlogReciever();
		}
		IntentFilter intentkill = new IntentFilter(MainMenuActivity.KILL_AUBLOG_INTENT);
		registerReceiver(mKillAublogReceiver, intentkill);
		IntentFilter intentDictRunning = new IntentFilter(MainMenuActivity.IS_DICTATION_STILL_RECORDING_INTENT);
		registerReceiver(mKillAublogReceiver, intentDictRunning);

		super.onCreate();

		mAudioManager = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
		if(mKillAuBlog == null){
			mKillAuBlog = false;
		}
		mTimeCodes = new ArrayList<String>();
	}

	public class KillAuBlogReciever extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			/*if main menu says to kill aublog, pass the message on to the transcription service*/
			if (intent.getAction().equals(MainMenuActivity.KILL_AUBLOG_INTENT)) {
				mKillAuBlog = true;
			}
			/*
			 * If main menu asks if you're recording (it is trying to kill aublog), reply that yes, you are still working.
			 */
			if (intent.getAction().equals(MainMenuActivity.IS_DICTATION_STILL_RECORDING_INTENT)) {
				Intent i = new Intent(EditBlogEntryActivity.TRANSCRIPTION_STILL_CONTACTING_INTENT);
				i.setData(mUri);
				i.putExtra(DictationRecorderService.EXTRA_AUDIOFILE_STATUS, mAudioResultsFileStatus);
				sendBroadcast(i);
			}
		}
	}
	@Override
	public void onDestroy() {
		if (mKillAublogReceiver != null) {
			unregisterReceiver(mKillAublogReceiver);
		}//never unregister, then maybe we will get the kill commands?
		super.onDestroy();
		/*
		 * This is a terrible workaround for issue
		 * http://code.google.com/p/android/issues/detail?id=9503 of
		 * using bluetooth audio on Android 2.2 phones. Summary: it
		 * kills the app instead of finishing normally
		 * 
		 * this will execute if MainMenu tells this service that it is the last AuBlog process running.
		 */
		if (mKillAuBlog != null){
			if(mKillAuBlog){
				if (mAudioManager.isBluetoothScoOn() ){
					mAudioManager.setBluetoothScoOn(false);
					mAudioManager.stopBluetoothSco();
				}
				mAudioManager.setMode(AudioManager.MODE_NORMAL);
				mAudioManager.setSpeakerphoneOn(true);
				android.os.Process.killProcess(android.os.Process.myPid());	
			}
		}
	}
	/**
	 * {@inheritDoc}
	 * 
	 * This method is called each time an intent is delivered to this service. 
	 * 
	 * 1. check if audio file is shorter than the users upload preferences, check if wifi is on
	 * 2. if conditions are satisfied, upload the file and set a pending intent that can load the result from the server into the corresponding AuBlog draft
	 * 
	 * 
	 */
	@Override
	protected void onHandleIntent(Intent intent) {

		/*
		 * get data from extras bundle, store it in the member variables
		 */
		try {
			mUri = intent.getData();
			mAudioFilePath = intent.getExtras().getString(DictationRecorderService.EXTRA_AUDIOFILE_FULL_PATH);
			mAudioResultsFileStatus = intent.getExtras().getString(DictationRecorderService.EXTRA_AUDIOFILE_STATUS);
			mPostContents=intent.getExtras().getString(EditBlogEntryActivity.EXTRA_CURRENT_CONTENTS);
			mAskUserImport=intent.getExtras().getBoolean(EditBlogEntryActivity.EXTRA_PROMPT_USER_TO_IMPORT_TRANSCRIPTION_INTO_BLOG);
			if(mAskUserImport == true){
				mTranscriptionStatus= "transcription fresh";
			}else{
				mTranscriptionStatus = "transcription not fresh"; 
			}
			
			mSplitType = intent.getExtras().getInt(EXTRA_SPLIT_TYPE);

		} catch (Exception e) {
			//Toast.makeText(SRTGeneratorActivity.this, "Error "+e,Toast.LENGTH_LONG).show();
		}
		if (mAudioFilePath.length() > 0) {
			mNotificationMessage= mAudioFilePath;
		}else{
			mNotificationMessage ="No file";
			return;
		}
		if (mPostContents == null){
			mPostContents="";
		}
		if(mAskUserImport == null){
			mAskUserImport= false;
		}
		File outSRTFile;
		if(mAudioFilePath.endsWith(".mp3")){
			outSRTFile =  new File(mAudioFilePath.replace(".mp3","_client.srt"));
		}else if (mAudioFilePath.contains("exported_drafts")){
			mShowNotification = false;
			outSRTFile =  new File(mAudioFilePath.replace(".js","backup.js"));
		}else{
			outSRTFile =  new File(mAudioFilePath.replace("_client.srt","_server.srt"));
		}

		if (mNM == null){
			mNM = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		}
		// The PendingIntent to launch our activity if the user selects this notification
		// View the results file
		Intent notifyingIntent = new Intent();
		notifyingIntent.setAction(android.content.Intent.ACTION_VIEW);
		notifyingIntent.setDataAndType(Uri.fromFile(outSRTFile), "text/*");
		mContentIntent = PendingIntent.getActivity(this, 0, notifyingIntent, 0);

		mNotification = new Notification(mAuBlogIconId, "AuBlog Transcription in progress", System.currentTimeMillis());
		mNotification.setLatestEventInfo(this, "AuBlog Transcription", "Checking for Wifi connection...", mContentIntent);
		mNotification.flags |= Notification.FLAG_AUTO_CANCEL;
		//startForeground(startId, mNotification);
		if (mShowNotification){
			mNM.notify(NOTIFICATION, mNotification);
		}

		Intent inten = new Intent(EditBlogEntryActivity.TRANSCRIPTION_STILL_CONTACTING_INTENT);
		inten.setData(mUri);
		inten.putExtra(DictationRecorderService.EXTRA_AUDIOFILE_STATUS, mAudioResultsFileStatus);
		sendBroadcast(inten);



		/*
		 * Check if wifi is active, or if this file can be uploaded as per the users
		 * preference settings
		 */
		SharedPreferences prefs = getSharedPreferences(PreferenceConstants.PREFERENCE_NAME, MODE_PRIVATE);
		mAuBlogInstallId = prefs.getString(PreferenceConstants.AUBLOG_INSTALL_ID, "0");
		mMaxFileUploadOverMobileNetworkSize = prefs.getInt(PreferenceConstants.PREFERENCE_MAX_UPLOAD_ON_MOBILE_NETWORK, 2000000);
		Boolean wifiOnly = prefs.getBoolean(PreferenceConstants.PREFERENCE_UPLOAD_WAIT_FOR_WIFI, true);
		File audioFile = new File(mAudioFilePath);
		//audioFile.length() < mMaxFileUploadOverMobileNetworkSize ||
		ConnectivityManager conMan = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		State wifi = conMan.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState();


		if ( audioFile.length() < mMaxUploadFileSize && 
				( 	    	(audioFile.length() < mMaxFileUploadOverMobileNetworkSize || wifiOnly == false ) 
						|| (wifi == State.CONNECTED || wifi == State.CONNECTING) 
						|| mAudioFilePath.endsWith(".srt")
				) 
		){
			//if the audio file 
			//   A: is  smaller than max upload size, and
			//   either B: smaller than the limit allowed on mobile, or user doesnt care about being connected to wifi
			//   or C: the wifi is on
			//	or D: its a subtitle so send it always
			//then, upload it for transcription. otherwise say it was too big to upload

			/*
			 * Upload file
			 */

			try {
				HttpClient httpClient = new DefaultHttpClient();
				HttpContext localContext = new BasicHttpContext();
				Long uniqueId = System.currentTimeMillis();
				HttpPost httpPost = new HttpPost(NonPublicConstants.NONPUBLIC_TRANSCRIPTION_WEBSERVICE_URL+NonPublicConstants.NONPUBLIC_TRANSCRIPTION_WEBSERVICE_API_KEY+mAudioFilePath.replace(PreferenceConstants.OUTPUT_AUBLOG_DIRECTORY+"audio/","") );


				MultipartEntity entity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);


				//				ByteArrayOutputStream bos = new ByteArrayOutputStream();
				//				bitmap.compress(CompressFormat.JPEG, 100, bos);
				//				byte[] data = bos.toByteArray();
				entity.addPart("title", new StringBody("thetitle"));
				///entity.addPart("returnformat", new StringBody("json"));
				//entity.addPart("uploaded", new ByteArrayBody(data,"myImage.jpg"));
				//entity.addPart("aublogInstallID",new StringBody(mAuBlogInstallId));
				String splitCode=""+mSplitType;
				entity.addPart("splitCode",new StringBody(splitCode));
				entity.addPart("file", new FileBody(audioFile));
				///entity.addPart("photoCaption", new StringBody("thecaption"));
				httpPost.setEntity(entity);

				mNotification.setLatestEventInfo(this, "AuBlog Transcription", "Connecting to transcription server...", mContentIntent);
				if (mShowNotification){
					mNM.notify(NOTIFICATION, mNotification);
				}

				HttpResponse response = httpClient.execute(httpPost,localContext);

				BufferedReader reader = new BufferedReader(
						new InputStreamReader(
								response.getEntity().getContent(), "UTF-8"));

				String firstLine = reader.readLine();
				mNotification.setLatestEventInfo(this, "AuBlog Transcription", firstLine, mContentIntent);
				if (mShowNotification){
					mNM.notify(NOTIFICATION, mNotification);
				}
				reader.readLine();//mFileNameOnServer = reader.readLine().replaceAll(":filename","");
				mFileNameOnServer = reader.readLine().replaceAll(":path","");
				/*
				 * Read response into timecodes
				 */
				String line ="";
				while((line = reader.readLine()) != null){
					mTimeCodes.add(line);
				}
				reader.close();
				//mAudioResultsFileStatus=mAudioResultsFileStatus+":::"+"File saved on server as "+mFileNameOnServer+" .";
			} catch (Exception e) {
				Log.e(e.getClass().getName(), e.getMessage(), e);
				//this is showing up for when the audio is not sent, but the client srt is...
				//mNotificationMessage = "...";// null;
			}
			FileOutputStream outSRT;
			try {
				outSRT = new FileOutputStream(outSRTFile);
				outSRT.write("0:00:00.000,0:00:00.000\n".getBytes());
				outSRT.write(mAudioResultsFileStatus.getBytes());
				outSRT.write("\n\n".getBytes());

				outSRT.write("0:00:00.010,0:00:00.010\n".getBytes());
				outSRT.write(mPostContents.getBytes());
				outSRT.write("\n\n".getBytes());
				/*
				 * Append time codes SRT array to srt file.
				 * the time codes and transcription are read line by line from the in the server's response. 
				 */
				for(int i = 0; i < mTimeCodes.size(); i++){
					mTranscription = mTranscription + mTimeCodes.get(i) + "\n"; 
					outSRT.write(mTimeCodes.get(i).getBytes());
					outSRT.write("\n".getBytes());
					//						outSRT.write("\n--Unknown--".getBytes());
					//						outSRT.write("\n\n".getBytes());
				}
				outSRT.flush();
				outSRT.close();
				if(mAudioFilePath.endsWith(".mp3")){
					mAudioResultsFileStatus=mAudioResultsFileStatus+":::"+"Dictation successfully sent to Transcription server.";
				}else{
					mAudioResultsFileStatus=mAudioResultsFileStatus+":::"+"Recieved transcription response from Transcription server, saved as _server.srt in the AuBlog folder.";
				}
				mTranscriptionReturned = true;
				saveMetaDataToDatabase();
				mNotificationMessage = "Transcription response saved as _server.srt";
				//					mNotification.setLatestEventInfo(this, "AuBlog Transcription", mNotificationMessage, mContentIntent);
				//					if (mShowNotification){
				//					mNM.notify(NOTIFICATION, mNotification);
				//				}

			} catch (IOException e) {
				// TODO Auto-generated catch block
				//e.printStackTrace();
				mNotificationMessage ="Cannot write results to SDCARD";
				mNotification.setLatestEventInfo(this, "AuBlog Transcription", mNotificationMessage, mContentIntent);
				if (mShowNotification){
					mNM.notify(NOTIFICATION, mNotification);
				}

			}

		}else{
			//no wifi, and the file is larger than the users settings for upload over mobile network.
			mNotificationMessage = "Dication was not sent for transcription: no wifi or too long. Check Aublog settings.";
			mNotification.setLatestEventInfo(this, "AuBlog Transcription", mNotificationMessage, mContentIntent);
			if (mShowNotification){
				mNM.notify(NOTIFICATION, mNotification);
			}
			mAudioResultsFileStatus=mAudioResultsFileStatus+":::"+"Dictation audio wasn't sent for transcription, either user has wifi only or the file is larger than the settings the user has chosen, or its larger than 10min.";
			saveMetaDataToDatabase();
			if(mAudioFilePath.endsWith(".mp3")){
				//if unable to send the mp3, create a client srt anyway.
				//overwrite the srt file witht he most recent status message, saying why the file wasn't sent for transcription.
				File outSRTFileClient =  new File(mAudioFilePath.replace(".mp3","_client.srt"));
				FileOutputStream outSRT;
				try {
					outSRT = new FileOutputStream(outSRTFileClient);
					outSRT.write("0:00:00.000,0:00:00.000\n".getBytes());
					outSRT.write(mAudioResultsFileStatus.getBytes());
					outSRT.write("\n\n".getBytes());

					outSRT.write("0:00:01.000,0:00:01.000\n".getBytes());
					outSRT.write(mPostContents.getBytes());
					outSRT.write("\n\n".getBytes());
					outSRT.flush();
					outSRT.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					//e.printStackTrace();
					mNotificationMessage ="Cannot write null results to SDCARD";
				}
			}//end if to make an empty srt file if the mp3 was not uploaded

		}//end if for max file size for upload

		if(mAudioFilePath.endsWith(".srt")){
			Intent i = new Intent(EditBlogEntryActivity.REFRESH_TRANSCRIPTION_INTENT);
			//i.setData(mUri);
			i.putExtra(DictationRecorderService.EXTRA_AUDIOFILE_STATUS, mAudioResultsFileStatus);
			i.putExtra(EditBlogEntryActivity.EXTRA_PROMPT_USER_TO_IMPORT_TRANSCRIPTION_INTO_BLOG, mAskUserImport);
			sendBroadcast(i);
			if (mAskUserImport){
				mNotificationMessage = "Transcription merged with server results.";
			}else{
				mNotificationMessage = "Transcription results sent and received.";
			}
			//			mNotification.setLatestEventInfo(this, "AuBlog Transcription", mNotificationMessage, mContentIntent);
			/*if (mShowNotification){
				mNM.notify(NOTIFICATION, mNotification);
			}*/			

		}else{
			Intent i = new Intent(EditBlogEntryActivity.DICTATION_SENT_INTENT);
			i.putExtra(DictationRecorderService.EXTRA_AUDIOFILE_STATUS, mAudioResultsFileStatus);
			sendBroadcast(i);
			mNotificationMessage = "Dication sent for transcription.";
			//			mNotification.setLatestEventInfo(this, "AuBlog Transcription", mNotificationMessage, mContentIntent);
			/*if (mShowNotification){
			mNM.notify(NOTIFICATION, mNotification);
			}*/	
		}
		//mNM.cancel(NOTIFICATION);

	}//end onhandle intent
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
					/*
					 * To avoid save walking, check if it contains it. if i contains it do nothing. if it doesnt contain it, then send the walking message.
					 * Recording service running:::/sdcard/AuBlog/audio/13137125920433253_2011-08-20_06.05_1313877906018_transcription-wifi-with-exceptions.mp3:::Recording started.:::maybebluetooth:::1313877911675:::Walking on this status message that was in the database.------:::Recording stopped.:::Attached a 529 second Recording.
:::Recording flagged for transcription.:::Sent to transcription service.:::File saved on server as data/e1fd831e1f6b913b9c8504d081271b4f.mp3 .:::Transcription server response saved as .srt in the AuBlog folder.:::Walking on this status message that was in the database.---Recording service running:::/sdcard/AuBlog/audio/13137125920433253_2011-08-20_06.05_1313877906018_transcription-wifi-with-exceptions.mp3:::Recording started.:::maybebluetooth:::1313877911675:::Walking on this status message that was in the database.------:::Recording stopped.:::Attached a 529 second Recording.
:::Recording flagged for transcription.:::Sent to transcription service.---

					 */
					if (! (mAudioResultsFileStatus.contains(mCursor.getString(4))) ){
						mAudioResultsFileStatus = mAudioResultsFileStatus+":::Walking on this status message that was in the database.--__"+ mCursor.getString(4)+"-__";
					}
				}
				ContentValues values = new ContentValues();
				values.put(AuBlogHistory.AUDIO_FILE_STATUS, mAudioResultsFileStatus);
				values.put(AuBlogHistory.TRANSCRIPTION_STATUS, mTranscriptionStatus);
				values.put(AuBlogHistory.TRANSCRIPTION_RESULT, mTranscription);
				getContentResolver().update(mUri, values,null, null);
				mDBLastModified = Long.toString(System.currentTimeMillis());
				getContentResolver().notifyChange(AuBlogHistory.CONTENT_URI, null);

				// Tell the user we saved recording meta info to the database.
				//Toast.makeText(this, "Audiofile info saved to DB.", Toast.LENGTH_SHORT).show();
				//mNotification.setLatestEventInfo(this, "AuBlog Dictation", "Saved to DB", mContentIntent);
				//mNM.notify(NOTIFICATION, mNotification);


			} catch (IllegalArgumentException e) {

			} catch (Exception e) {

			}




		}//end if where cursor has content.

	}

	
	/**
	 * 
	 * @param filePath TODO remove and just use mTimeCodes
	 * @return
	 * @throws java.io.IOException
	 */
	public static String readAsTranscriptionString(String filePath) throws java.io.IOException
	{
	    BufferedReader reader = new BufferedReader(new FileReader(filePath));
	    String line;
	    String results="";
	    Pattern pattern = Pattern.compile("^\\d:\\d\\d:\\d\\d.\\d\\d\\d,\\d:\\d\\d:\\d\\d.\\d\\d\\d");
		Matcher matcher;
	    while((line = reader.readLine()) != null)
	    {
	    	//throw away file info by detecting the timecodes and discarding 2 lines after. 
            if (line.contains("0:00:00.000,0:00:00.000")){
            	line = reader.readLine();
            	while ((line != null) && ! line.contains("0:00:00.020,0:00:00.020")){
            		line = reader.readLine();
            	}
            	line = reader.readLine();
            	line = reader.readLine();
            	line = reader.readLine();
            }
            if(line != null){
	            //throw away additional time codes
	            matcher = pattern.matcher(line);
	    		if (matcher.find()){
	    			//its a time code do nothing.
	    		}else{
	    			results += line+ " ";
	    		}
            }
	    }
	    reader.close();
	    return results;
	}
}
