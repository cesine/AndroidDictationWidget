/*
 * Copyright (C) 2010 The Android Open Source Project
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

package ca.ilanguage.aublog.preferences;

import java.io.File;
import java.util.List;

import com.google.android.apps.analytics.GoogleAnalyticsTracker;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceScreen;
import android.widget.Toast;

import ca.ilanguage.aublog.R;
import ca.ilanguage.aublog.db.AuBlogHistoryDatabase.AuBlogHistory;

public class SetPreferencesActivity extends PreferenceActivity implements 
		YesNoDialogPreference.YesNoDialogListener {
	GoogleAnalyticsTracker tracker;
	private AudioManager mAudioManager;
    
	
	String mBloggerAccount;
	String mBloggerPassword;
	/*
	 * The Aublog Install ID is a timestamp generated on install in the Main Menu what's new logic with 10 random digits appended to it.
	 * It is used to identify anonymously the install to the aublog webserver. It can be tied to an Aublog user id, if aublog user ids logic is added to the server side logic.
	 */
	private String mAuBlogInstallId;
	
	@Override
	  protected void onDestroy() {
	    super.onDestroy();
	    // Stop the tracker when it is no longer needed.
	    tracker.stop();
	  }
	@Override
	protected void onStop(){
		SharedPreferences prefs = getSharedPreferences(PreferenceConstants.PREFERENCE_NAME, MODE_PRIVATE);
		/*
		 * Always set audio to normal when the preferecnes activty goes off screen, only turn on earpiece or bluetooth if 
		 * check boxes are checked when user leaves. 
		 */
		mAudioManager.setMode(AudioManager.MODE_NORMAL);
        mAudioManager.setSpeakerphoneOn(true);
    	
        Boolean useBluetooth = prefs.getBoolean(PreferenceConstants.PREFERENCE_USE_BLUETOOTH_AUDIO, false);
	    Boolean usePhoneEarPiece = prefs.getBoolean(PreferenceConstants.PREFERENCE_USE_PHONE_EARPIECE_AUDIO, false);
	   
		if(useBluetooth){
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
	    	
	    	setVolumeControlStream(AudioManager.STREAM_VOICE_CALL);
	    	mAudioManager.setMode(AudioManager.MODE_IN_CALL);
	    	/*
	    	 * then use the media player as usual
	    	 */
		}
		if(usePhoneEarPiece){
			/*
	    	 * This works.
	    	 * 
	    	 * This constant ROUTE_EARPIECE is deprecated. Do not set audio routing directly, use setSpeakerphoneOn(), setBluetoothScoOn() methods instead.
	    	 */
	    	mAudioManager.setSpeakerphoneOn(false);
	    	//routes to earpiece by default when speaker phone is off. 
	    	//mAudioManager.setRouting(AudioManager.MODE_NORMAL, AudioManager.ROUTE_EARPIECE, AudioManager.ROUTE_ALL); 
	    	setVolumeControlStream(AudioManager.STREAM_VOICE_CALL);
	    	mAudioManager.setMode(AudioManager.MODE_IN_CALL);
	    	/*
	    	 * then the app can use the media player as usual
	    	 */
		}
		super.onStop();
		
	}
	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        tracker = GoogleAnalyticsTracker.getInstance();

	    // Start the tracker in manual dispatch mode...
	    tracker.start(NonPublicConstants.NONPUBLIC_GOOGLE_ANALYTICS_UA_ACCOUNT_CODE, 20, this);

	    mAudioManager = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
        
	    
        getPreferenceManager().setSharedPreferencesMode(MODE_PRIVATE);
        getPreferenceManager().setSharedPreferencesName(PreferenceConstants.PREFERENCE_NAME);
        
        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.preferences);
        
        SharedPreferences prefs = getSharedPreferences(PreferenceConstants.PREFERENCE_NAME, MODE_PRIVATE);
		SharedPreferences.Editor editor = prefs.edit();
		/*
		 * set the installid for appending to the labels
		 */
		mAuBlogInstallId = prefs.getString(PreferenceConstants.AUBLOG_INSTALL_ID, "0");
		
        
        Preference exportTree = findPreference(PreferenceConstants.PREFERENCE_EMAIL_DRAFTS_TREE);
        exportTree.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
			
			@Override
			public boolean onPreferenceClick(Preference preference) {
				
				tracker.trackEvent(
			            "Clicks",  // Category
			            "Button",  // Action
			            "user clicked on email from the settings screen : "+mAuBlogInstallId, // Label
			            65);       // Value
				File file = new File(PreferenceConstants.OUTPUT_AUBLOG_DIRECTORY+PreferenceConstants.OUTPUT_FILE_NAME_FOR_DRAFT_EXPORT);

		    	Intent mailto = new Intent(Intent.ACTION_SEND); 
		        mailto.setType("message/rfc822") ; // use from live device
		        mailto.putExtra(Intent.EXTRA_EMAIL, new String[]{""});
		        mailto.putExtra(Intent.EXTRA_SUBJECT,"Backup of AuBlog Drafts");
		        mailto.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(file));
		        mailto.putExtra(Intent.EXTRA_TEXT,getString(R.string.email_exported_json_text_content));
		        startActivity(Intent.createChooser(mailto, "Select email application."));
		   
				return true;
			}
		});
        
        
        Preference eraseGameButton = getPreferenceManager().findPreference("erasegame");
        if (eraseGameButton != null) {
        	YesNoDialogPreference yesNo = (YesNoDialogPreference)eraseGameButton;
        	yesNo.setListener(this);
        }
        if (getIntent().getBooleanExtra("controlConfig", false)) {
        	PreferenceScreen controlConfig = (PreferenceScreen)getPreferenceManager().findPreference("controlConfigScreen");
        	if (controlConfig != null) {
        		setPreferenceScreen(controlConfig);
        	}
        }
        
       
		/*
		 * Find out if user has a file manager (specifically org.openintents.filemanager) to be able to open files in the settings
		 * 
		 * if they have it, activate those settings (they were greyed out)
		 * If they don't have it, they can click on the checkbox, it will take them to the market to get it.
		 * 
		 */
        final boolean fileManagerAvailable = isIntentAvailable(this,
        "org.openintents.action.PICK_FILE");
		Boolean fileManagerChecked = prefs.getBoolean(PreferenceConstants.PREFERENCE_FILE_MANAGER_INSTALLED, false);
        if ( fileManagerChecked && ! fileManagerAvailable ){
        	Toast.makeText(SetPreferencesActivity.this, "To open and export recorded files or " +
        			"draft data you can install the OI File Manager, " +
        			"it allows you to browse your SDCARD directly on your mobile device.", Toast.LENGTH_LONG).show();
        	Intent goToMarket = new Intent(Intent.ACTION_VIEW)
            .setData(Uri.parse("market://details?id=org.openintents.filemanager"));
        	tracker.trackEvent(
		            "DependantPackages",  // Category
		            "FileManager",  // Action
		            "user doesnt have a filemanager so export is disnabled, took them to the market to install it.: "+mAuBlogInstallId, // Label
		            60);       // Value
        	startActivity(goToMarket);
        }
        if ( ! fileManagerChecked &&  fileManagerAvailable){
        	tracker.trackEvent(
		            "DependantPackages",  // Category
		            "FileManager",  // Action
		            "user has a filemanager so export is enabled: "+mAuBlogInstallId, // Label
		            61);       // Value
        	
        	/*
        	 * Enable the export and open possibilities
        	 */
			editor.putBoolean(PreferenceConstants.PREFERENCE_FILE_MANAGER_INSTALLED,
					true);
			editor.commit();
        }
        /*
         * If the file manager is available ungrey the export/open settings. 
         *  Only if there has been audio recorded, ungrey the audio open dir setting
         */
		if (fileManagerAvailable){
			editor.putBoolean(PreferenceConstants.PREFERENCE_FILE_MANAGER_INSTALLED,
					true);
			editor.commit();
			Preference exportJson = getPreferenceManager().findPreference("openJsonTxt");
			exportJson.setEnabled(true);
			exportJson.setSelectable(true);
			Preference openAudioDir = getPreferenceManager().findPreference("openAudioDir");
			if (new  File(PreferenceConstants.OUTPUT_AUBLOG_DIRECTORY+"/audio").exists()){
				openAudioDir.setEnabled(true);
				openAudioDir.setSelectable(true);
			}else{
				openAudioDir.setSummary("No dictations found");
			}
		}

		
        
        
    }
	/**
	 * Indicates whether the specified action can be used as an intent. This
	 * method queries the package manager for installed packages that can
	 * respond to an intent with the specified action. If no suitable package is
	 * found, this method returns false.
	 *
	 * @param context The application's environment.
	 * @param action The Intent action to check for availability.
	 *
	 * @return True if an Intent with the specified action can be sent and
	 *         responded to, false otherwise.
	 */
	public static boolean isIntentAvailable(Context context, String action) {
	    final PackageManager packageManager = context.getPackageManager();
	    final Intent intent = new Intent(action);
	    List<ResolveInfo> list =
	            packageManager.queryIntentActivities(intent,
	                    PackageManager.MATCH_DEFAULT_ONLY);
	    return list.size() > 0;
	}
	public void onDialogClosed(boolean positiveResult) {
		if (positiveResult) {
			/*
			 * TODO open the database provider, delete database and recreate with the default entries
			 */
			startActivity(new Intent(android.provider.Settings.ACTION_MANAGE_APPLICATIONS_SETTINGS));
			//wipeUserData
			tracker.trackEvent(
		            "AuBlogLifeCycleEvent",  // Category
		            "UserWipe",  // Action
		            "user wants to wipe their draft data, taking them to the device settings where they can click clear data, but dont know if they really clicked it.: "+mAuBlogInstallId, // Label
		            63);       // Value
			
			Toast.makeText(this, R.string.saved_data_erased_notification,
                    Toast.LENGTH_SHORT).show();
		}
	}
}
