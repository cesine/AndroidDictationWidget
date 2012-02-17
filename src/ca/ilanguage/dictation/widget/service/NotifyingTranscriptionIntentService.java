package ca.ilanguage.dictation.widget.service;


import java.util.ArrayList;

import ca.ilanguage.dictation.widget.R;
import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.net.Uri;

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
	private static int NOTIFICATIONS = R.layout.status_bar_notifications;
	private String mNotificationMessage;
	
	public void onCreate() {
		super.onCreate();
		//TODO
	}

	@Override
	public void onDestroy() {
		//TODO
		super.onDestroy();
		
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
		//TODO

	}
	
	private void saveMetaDataToDatabase(){
		//TODO

	}

	
	/**
	 * 
	 * @return string of transcription results only concatinated with spaces.
	 * @throws java.io.IOException
	 */
	public String readAsTranscriptionString() 
	{
	    String line;
	    String results="";
	    //TODO
	    return results;
	}
}
