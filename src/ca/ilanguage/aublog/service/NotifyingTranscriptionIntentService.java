package ca.ilanguage.aublog.service;

import java.util.ArrayList;

import ca.ilanguage.aublog.R;
import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;

public class NotifyingTranscriptionIntentService extends IntentService {
	protected static String TAG = "NotifyingTranscriptionIntentService";
	  
    public NotifyingTranscriptionIntentService() {
		super(TAG);
		// TODO Auto-generated constructor stub
	}

	private NotificationManager mNM;
   
    private int mMaxFileUploadOverMobileNetworkSize;
    private int mMaxUploadFileSize = 15000000;  // Set maximum upload size to 1.5MB roughly 15 minutes of audio, 
    											// users shouldn't abuse transcription service by sending meetings and other sorts of audio.
    											// If you change this value, change the value in the arrays.xml as well look for:
    											// 15 minutes (AuBlog\'s max transcription length)
    private String mAudioFilePath;
	private String mFileNameOnServer="";
    private int mSplitType = 0;
	private ArrayList<String> mTimeCodes;
	
	public static final String EXTRA_AUDIOFILE_FULL_PATH = "audioFilePath";
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
		super.onCreate();
		mNM = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

	}

	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
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
		
		/*
         * get data from extras bundle, store it in the member variables
         */
        try {
            mAudioFilePath = intent.getExtras().getString(EXTRA_AUDIOFILE_FULL_PATH);
            mSplitType = intent.getExtras().getInt(EXTRA_SPLIT_TYPE);
            
        } catch (Exception e) {
        	//Toast.makeText(SRTGeneratorActivity.this, "Error "+e,Toast.LENGTH_LONG).show();
        }
        if (mAudioFilePath.length() > 0) {
            mNotificationMessage= mAudioFilePath;
        }else{
        	mNotificationMessage ="No file";
        }
        showNotification(R.drawable.stat_aublog,  mNotificationMessage);
    	
	}//end onhandle intent

	  
    private void showNotification(int iconId, String message) {
        // In this sample, we'll use the same text for the ticker and the expanded notification
        //CharSequence text = message;

        // Set the icon, scrolling text and timestamp.
        // Note that in this example, we pass null for tickerText.  We update the icon enough that
        // it is distracting to show the ticker text every time it changes.  We strongly suggest
        // that you do this as well.  (Think of of the "New hardware found" or "Network connection
        // changed" messages that always pop up)
        Notification notification = new Notification(iconId, message, System.currentTimeMillis());

        // The PendingIntent to launch our activity if the user selects this notification
        //Intent intent = new Intent(this, NotifyingController.class);
        //intent.setdata to the post uri
        //intent.setextra to add a boolean to update content to append transcription
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
                new Intent(this, NotifyingController.class), 0);

        // Set the info for the views that show in the notification panel.
        notification.setLatestEventInfo(this, "AuBlog Transcription Service",
                       message, contentIntent);

        // Send the notification.
        // We use a layout id because it is a unique number.  We use it later to cancel.
        mNM.notify(AUBLOG_NOTIFICATIONS, notification);
    }
}
