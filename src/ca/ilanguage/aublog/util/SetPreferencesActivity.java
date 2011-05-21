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

package ca.ilanguage.aublog.util;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.widget.TextView;
import android.widget.Toast;

import ca.ilanguage.aublog.R;

public class SetPreferencesActivity extends PreferenceActivity implements 
		YesNoDialogPreference.YesNoDialogListener {
	String mBloggerAccount;
	String mBloggerPassword;
	
	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        getPreferenceManager().setSharedPreferencesMode(MODE_PRIVATE);
        getPreferenceManager().setSharedPreferencesName(PreferenceConstants.PREFERENCE_NAME);
        
        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.preferences);
        
        SharedPreferences settings = getSharedPreferences(PreferenceConstants.PREFERENCE_NAME, 0);
        mBloggerAccount = settings.getString(PreferenceConstants.PREFERENCE_ACCOUNT, "create");
        mBloggerPassword = settings.getString(PreferenceConstants.PREFERENCE_PASSWORD, "");
        EditTextPreference acc = (EditTextPreference) getPreferenceScreen().findPreference(PreferenceConstants.PREFERENCE_ACCOUNT);
        acc.setText(mBloggerAccount);
        
        
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
        
    }
	protected void onStop(){
	       super.onStop();

	      // We need an Editor object to make preference changes.
	      // All objects are from android.context.Context
	      SharedPreferences settings = getSharedPreferences(PreferenceConstants.PREFERENCE_NAME, 0);
	      SharedPreferences.Editor editor = settings.edit();
	      editor.putString(PreferenceConstants.PREFERENCE_ACCOUNT, mBloggerAccount);
	      editor.putString(PreferenceConstants.PREFERENCE_PASSWORD, mBloggerPassword);
	      // Commit the edits!
	      editor.commit();
	    }

	@Override
	protected void onResume(){
	    super.onResume();
	    SharedPreferences settings = getSharedPreferences(PreferenceConstants.PREFERENCE_NAME, 0);
	    mBloggerAccount = settings.getString(PreferenceConstants.PREFERENCE_ACCOUNT, "resume");
        mBloggerPassword = settings.getString(PreferenceConstants.PREFERENCE_PASSWORD, "");
	}

	public void onDialogClosed(boolean positiveResult) {
		if (positiveResult) {
			SharedPreferences prefs = getSharedPreferences(PreferenceConstants.PREFERENCE_NAME, MODE_PRIVATE);
			SharedPreferences.Editor editor = prefs.edit();
			editor.remove(PreferenceConstants.PREFERENCE_LEVEL_ROW);
			editor.remove(PreferenceConstants.PREFERENCE_LEVEL_INDEX);
			editor.remove(PreferenceConstants.PREFERENCE_LEVEL_COMPLETED);
			editor.remove(PreferenceConstants.PREFERENCE_LINEAR_MODE);
			editor.remove(PreferenceConstants.PREFERENCE_TOTAL_GAME_TIME);
			editor.remove(PreferenceConstants.PREFERENCE_PEARLS_COLLECTED);
			editor.remove(PreferenceConstants.PREFERENCE_PEARLS_TOTAL);
			editor.remove(PreferenceConstants.PREFERENCE_ROBOTS_DESTROYED);
			editor.remove(PreferenceConstants.PREFERENCE_DIFFICULTY);

			editor.commit();
			Toast.makeText(this, R.string.saved_game_erased_notification,
                    Toast.LENGTH_SHORT).show();
		}
	}
}
