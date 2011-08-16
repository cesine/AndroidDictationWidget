/*
 * Copyright (C) 2007 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ca.ilanguage.aublog.service;


import java.util.ArrayList;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.location.GpsStatus.NmeaListener;
import android.media.AudioTrack;
import android.os.Binder;
import android.os.ConditionVariable;
import android.os.IBinder;
import android.os.Parcel;
import android.os.RemoteException;
import android.widget.RemoteViews;
import android.widget.Toast;

import ca.ilanguage.aublog.R;

/**
 * This is an example of service that will update its status bar balloon 
 * every 5 seconds for a minute.
 * 
 */
public class NotifyingTranscriptionService extends Service {

    private NotificationManager mNM;
   
    private AudioTrack mAudioTrack;
	private String mAudioFilePath;
	private int mSplitType;
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
    private static int MOOD_NOTIFICATIONS = R.layout.status_bar_notifications;
    private String mNotificationMessage;

    // variable which controls the notification thread 
    private ConditionVariable mCondition;
 
    @Override
	public int onStartCommand(Intent intent, int flags, int startId) {
    	/*
         * TODO get data from bundle, store it in the member variables
         */
        try {
            mAudioFilePath = intent.getExtras().getString(EXTRA_AUDIOFILE_FULL_PATH);
           
        } catch (Exception e) {
        	//Toast.makeText(SRTGeneratorActivity.this, "Error "+e,Toast.LENGTH_LONG).show();
        }
        if (mAudioFilePath.length() > 0) {
            mNotificationMessage= mAudioFilePath;
        }else{
        	mNotificationMessage ="No file";
        }
		return super.onStartCommand(intent, flags, startId);
	}

	@Override
    public void onCreate() {
    	
        mNM = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        
        // Start up the thread running the service.  Note that we create a
        // separate thread because the service normally runs in the process's
        // main thread, which we don't want to block.
        Thread notifyingThread = new Thread(null, mTask, "NotifyingService");
        mCondition = new ConditionVariable(false);
        notifyingThread.start();
    }

    @Override
    public void onDestroy() {
        // Cancel the persistent notification.
        mNM.cancel(MOOD_NOTIFICATIONS);
        // Stop the thread from generating further notifications
        mCondition.open();
    }

    private Runnable mTask = new Runnable() {
        public void run() {
        	
        	/*
        	 * Send audio file for transcription to server. 
        	 * 
        	 */
        	
            for (int i = 0; i < 4; ++i) {
                showNotification(R.drawable.stat_happy,  mNotificationMessage);
                if (mCondition.block(5 * 1000)) 
                    break;
                showNotification(R.drawable.stat_happy,  mNotificationMessage);
                if (mCondition.block(5 * 1000)) 
                    break;
                showNotification(R.drawable.stat_happy,  mAudioFilePath);
                if (mCondition.block(5 * 1000)) 
                    break;
            }
            
            /*
             * Generate the SRT
             */
            String message = generateSRT();
            
            /*
             * Return results
             * 
             * As an intent:
            Intent resultIntent = new Intent();
            resultIntent.putExtra(EXTRA_RESULTS, mTimeCodes);
            setResult(Activity.RESULT_OK, resultIntent);
            finish();
            * As a .srt file in the audio directory
             */
            
            // Done with our work...  stop the service!
            NotifyingTranscriptionService.this.stopSelf();
            
            
        }
    };

    @Override
    public IBinder onBind(Intent intent) {
    	/*
    	 * TODO unknown if it is necessary to get data from the intent here in addition to onstartcommand
         * TODO get data from bundle, store it in the member variables
         */
        try {
            mAudioFilePath = intent.getExtras().getString(EXTRA_AUDIOFILE_FULL_PATH);
           
        } catch (Exception e) {
        	//Toast.makeText(SRTGeneratorActivity.this, "Error "+e,Toast.LENGTH_LONG).show();
        }
        if (mAudioFilePath.length() > 0) {
            mNotificationMessage= mAudioFilePath;
        }else{
        	mNotificationMessage ="No file";
        }
        return mBinder;
    }
    
    private void showNotification(int moodId, String message) {
        // In this sample, we'll use the same text for the ticker and the expanded notification
        CharSequence text = message;

        // Set the icon, scrolling text and timestamp.
        // Note that in this example, we pass null for tickerText.  We update the icon enough that
        // it is distracting to show the ticker text every time it changes.  We strongly suggest
        // that you do this as well.  (Think of of the "New hardware found" or "Network connection
        // changed" messages that always pop up)
        Notification notification = new Notification(moodId, null, System.currentTimeMillis());

        // The PendingIntent to launch our activity if the user selects this notification
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
                new Intent(this, NotifyingController.class), 0);

        // Set the info for the views that show in the notification panel.
        notification.setLatestEventInfo(this, "Transcription in background",
                       text, contentIntent);

        // Send the notification.
        // We use a layout id because it is a unique number.  We use it later to cancel.
        mNM.notify(MOOD_NOTIFICATIONS, notification);
    }
    public String generateSRT(){
    	String messageToReturn = "";
    	/*
    	 * TODO 
    	 * make sure the audioTrack is open
    	 */

    	/* 
    	 * TODO 
    	 * implement switch on mSplitType
    	 */
    	switch (mSplitType){
    		default:
    			messageToReturn = splitOnSilence();
    			break;
    	}
    	
    	
    	return messageToReturn;
    }
    public String splitOnSilence(){
    	mTimeCodes = new ArrayList<String>();
    	mTimeCodes.add("0:00:02.350,0:00:06.690");
    	mTimeCodes.add("0:00:07.980,0:00:12.780");
    	mTimeCodes.add("0:00:14.529,0:00:17.970");
    	mTimeCodes.add("0:00:17.970,0:00:20.599");
    	return "right now, these are fake timecodes";
    }
    public String testSplitOnSilence(){
    	/*
    	 * TODO
    	 * open raw/res/...wav sample
    	 * set the audioTrack to that file
    	 * run splitOnSilence on it
    	 * compare the output against the raw/res...corrected.sbv time stamps
    	 * return test failed/passed
    	 */
    	return "fail";
    }
    // This is the object that receives interactions from clients.  See
    // RemoteService for a more complete example.
    private final IBinder mBinder = new Binder() {
        @Override
        protected boolean onTransact(int code, Parcel data, Parcel reply,
                int flags) throws RemoteException {
            return super.onTransact(code, data, reply, flags);
        }
    };
}
