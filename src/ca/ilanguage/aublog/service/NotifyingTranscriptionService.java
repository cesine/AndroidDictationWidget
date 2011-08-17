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


import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

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

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import android.net.ConnectivityManager;
import android.net.NetworkInfo.State;
import android.os.Binder;
import android.os.ConditionVariable;
import android.os.IBinder;
import android.os.Parcel;
import android.os.RemoteException;
import android.util.Log;
import android.widget.Toast;

import ca.ilanguage.aublog.R;
import ca.ilanguage.aublog.preferences.NonPublicConstants;
import ca.ilanguage.aublog.preferences.PreferenceConstants;

/**
 * This is an example of service that will update its status bar balloon 
 * every 5 seconds for a minute.
 * 
 * See also Alarm Manager
 * http://developer.android.com/reference/android/app/AlarmManager.html
 * 
 * And Checking whether wifi is on, or firing when wifi turns on
 * http://stackoverflow.com/questions/4733617/android-scheduled-files-to-server
 * 
 */
public class NotifyingTranscriptionService extends Service {

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
	public int onStartCommand(Intent intent, int flags, int startId) {
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
        Thread notifyingThread = new Thread(null, mTask, "NotifyingService");
        
        notifyingThread.start();
        
		return super.onStartCommand(intent, flags, startId);
	}

	@Override
    public void onCreate() {
		
		
        mNM = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        
        // Start up the thread running the service.  Note that we create a
        // separate thread because the service normally runs in the process's
        // main thread, which we don't want to block.
        
        
    }

    @Override
    public void onDestroy() {
        // Dont Cancel the persistent notification, let the user click on it.
        //mNM.cancel(AUBLOG_NOTIFICATIONS);
        // Stop the thread from generating further notifications
    	
    	// Done with our work...  stop the service!
        
       
    }

    private Runnable mTask = new Runnable() {
        public void run() {
        	
        	//showNotification(R.drawable.stat_aublog,  mNotificationMessage);
        	/*
        	 * Send audio file for transcription to server. 
        	 * 
        	 */
        	mNotificationMessage = uploadToServer();
        	
        	showNotification(R.drawable.stat_aublog,  mNotificationMessage);
        	         
            /*
             * Append fake time codes for testing purposes
             */
            splitOnSilence();
            
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
            
            new File(PreferenceConstants.OUTPUT_AUBLOG_DIRECTORY).mkdirs();
			File outSRTFile =  new File(mAudioFilePath.replace(".mp3",".srt"));
			try {
				FileOutputStream outSRT = new FileOutputStream(outSRTFile);
				outSRT.write(mFileNameOnServer.getBytes());
				
				/*
				 * Append time codes SRT array to srt file.
				 * the time codes and transcription are read line by line from the in the server's response. 
				 */
				for(int i = 0; i < mTimeCodes.size(); i++){
					outSRT.write(mTimeCodes.get(i).getBytes());
				}
								
				outSRT.flush();
				outSRT.close();
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
//				Toast.makeText(
//						NotifyingTranscriptionService.this,
//						"The SDCARD isn't writeable. Is the device being used as a disk drive on a comptuer?\n "
//								+ e.toString(), Toast.LENGTH_LONG).show();
			} catch (IOException e) {
				// TODO Auto-generated catch block
//				Toast.makeText(
//						NotifyingTranscriptionService.this,
//						"The SDCARD isn't writeable. Is the device being used as a disk drive on a comptuer?\n "
//								+ e.toString(), Toast.LENGTH_LONG).show();
			}
			
            
			NotifyingTranscriptionService.this.stopSelf();
	        
            
        }
    };

    @Override
    public IBinder onBind(Intent intent) {
    	/*
    	 * TODO unknown if it is necessary to get data from the intent here in addition to onstartcommand
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
        notification.setLatestEventInfo(this, "AuBlog Transcription Service",
                       text, contentIntent);

        // Send the notification.
        // We use a layout id because it is a unique number.  We use it later to cancel.
        mNM.notify(AUBLOG_NOTIFICATIONS, notification);
    }
    /*
     * based on 
     * http://vikaskanani.wordpress.com/2011/01/29/android-image-upload-activity/
     * http://stackoverflow.com/questions/2017414/post-multipart-request-with-android-sdk
     */
    public String uploadToServer(){
    	String returnMessage="";
    	SharedPreferences prefs = getSharedPreferences(PreferenceConstants.PREFERENCE_NAME, MODE_PRIVATE);
		mMaxFileUploadOverMobileNetworkSize = prefs.getInt(PreferenceConstants.PREFERENCE_MAX_UPLOAD_ON_MOBILE_NETWORK, 2000000);
		File audioFile = new File(mAudioFilePath);
		if (audioFile.length() > mMaxFileUploadOverMobileNetworkSize || audioFile.length() > mMaxUploadFileSize){
			//if the audio file is larger than the preferences setting for upload over mobile network, dont upload it unless wifi is connected
			ConnectivityManager conMan = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
			State wifi = conMan.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState();
			if (wifi == State.CONNECTED || wifi == State.CONNECTING) {
			    //wifi is on
				try {
					HttpClient httpClient = new DefaultHttpClient();
					HttpContext localContext = new BasicHttpContext();
					Long uniqueId = System.currentTimeMillis();
					HttpPost httpPost = new HttpPost(NonPublicConstants.NONPUBLIC_TRANSCRIPTION_WEBSERVICE_URL+"stuff"+uniqueId.toString());

					MultipartEntity entity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);
					
					
//					ByteArrayOutputStream bos = new ByteArrayOutputStream();
//					bitmap.compress(CompressFormat.JPEG, 100, bos);
//					byte[] data = bos.toByteArray();
					entity.addPart("title", new StringBody("thetitle"));
					///entity.addPart("returnformat", new StringBody("json"));
					//entity.addPart("uploaded", new ByteArrayBody(data,"myImage.jpg"));
					//entity.addPart("aublogInstallID",new StringBody(mAuBlogInstallId));
					String splitCode=""+mSplitType;
					entity.addPart("splitCode",new StringBody(splitCode));
					entity.addPart("file", new FileBody(audioFile));
					///entity.addPart("photoCaption", new StringBody("thecaption"));
					httpPost.setEntity(entity);
					
					HttpResponse response = httpClient.execute(httpPost,localContext);
					
					BufferedReader reader = new BufferedReader(
							new InputStreamReader(
									response.getEntity().getContent(), "UTF-8"));

					String firstLine = reader.readLine();
					reader.readLine();//mFileNameOnServer = reader.readLine().replaceAll(":filename","");
					mFileNameOnServer = reader.readLine().replaceAll(":path","");
					/*
					 * Read response into timecodes
					 */
					String line ="";
					while((line = reader.readLine()) != null){
						mTimeCodes.add(line);
						
					}
					
					//showNotification(R.drawable.stat_stat_aublog,  mFileNameOnServer);
		        	returnMessage = firstLine;
				} catch (Exception e) {
					//Log.e(e.getClass().getName(), e.getMessage(), e);
					returnMessage = "Connection error.";// null;
				}
				
			}else{
				//no wifi, and the file is larger than the users settings for upload over mobile network.
				returnMessage = "Transcription not sent.";
				
			}//end if for wifi connection
			
		}//end if for max file size for upload
		mTimeCodes.add(returnMessage);
		return returnMessage;
    	
    	
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
