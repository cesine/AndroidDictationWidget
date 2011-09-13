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

// Need the following import to get access to the app resources, since this
// class is in a sub-package.

import com.google.android.apps.analytics.GoogleAnalyticsTracker;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import ca.ilanguage.aublog.R;
import ca.ilanguage.aublog.preferences.NonPublicConstants;
import ca.ilanguage.aublog.preferences.PreferenceConstants;
import ca.ilanguage.aublog.ui.EditBlogEntryActivity;


/**
 * Controller to start and stop a service. 

Demonstrates how to pass information to the service via extras

Clicking on the notification brings user here, this is where user can do extra actions, like schedule uplaods for later, import transcriptions into aublog?
TODO button to stop recording. (sent from dictationrecorderservice)
add buttons
Turn on wifi
Open aublog settings
Retry xxx audio file (add files to cue)
 */
public class NotifyingController extends Activity {
	private Uri mUri;
	GoogleAnalyticsTracker tracker;
	private String mAuBlogInstallId;
	
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mUri = getIntent().getData();
        setContentView(R.layout.notifying_controller);

        Button button = (Button) findViewById(R.id.notifyStart);
        button.setOnClickListener(mStartListener);
        button = (Button) findViewById(R.id.notifyStop);
        button.setVisibility(Button.INVISIBLE);
        button.setOnClickListener(mStopListener);
        
        tracker = GoogleAnalyticsTracker.getInstance();

	    // Start the tracker in manual dispatch mode...
	    tracker.start(NonPublicConstants.NONPUBLIC_GOOGLE_ANALYTICS_UA_ACCOUNT_CODE, 20, this);
	    SharedPreferences prefs = getSharedPreferences(PreferenceConstants.PREFERENCE_NAME, MODE_PRIVATE);
	    mAuBlogInstallId = prefs.getString(PreferenceConstants.AUBLOG_INSTALL_ID, "0");
		
	    tracker.trackEvent(
				mAuBlogInstallId,  // Category
	            "Notifications",  // Action
	            "User found the notification controller by clicking on the recording notification. "+System.currentTimeMillis() +" : "+mAuBlogInstallId, // Label
	            (int)System.currentTimeMillis());       // Value
		

    }

    @Override
	protected void onDestroy() {
    	String release = Build.VERSION.RELEASE;
    	tracker.stop();
		super.onDestroy();
		/*
		if(release.equals("2.2")){
	    	//this does not show a force close, but does sucessfully allow the user to disconnect the bluetooth after they close aublog. 
	    	//if they have android 2.2 and they disconnect the bluetooth without quitting aublog then the device will reboot.
	    	 android.os.Process.killProcess(android.os.Process.myPid());
	    }else{
	    	//do nothing, bluetooth issue is fixed in 2.2.1 and above
	    }*/
	}

	private OnClickListener mStartListener = new OnClickListener() {
        public void onClick(View v) {
        	Intent intent = new Intent(NotifyingController.this, DictationRecorderService.class);
        	stopService(intent);
    		tracker.trackEvent(
    				mAuBlogInstallId,  // Category
    	            "Dictation stopped",  // Action
    	            "User clicked Stop Dictation  in Notifying controller : "+System.currentTimeMillis() +" : "+mAuBlogInstallId, // Label
    	            (int)System.currentTimeMillis());       // Value
        }
    };

    private OnClickListener mStopListener = new OnClickListener() {
        public void onClick(View v) {

            Intent i = new Intent(EditBlogEntryActivity.DICTATION_STILL_RECORDING_INTENT);
            i.setData(mUri);
    		sendBroadcast(i);
        }
    };
}

