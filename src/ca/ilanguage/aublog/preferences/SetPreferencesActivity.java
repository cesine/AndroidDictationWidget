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

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceScreen;
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

	public void onDialogClosed(boolean positiveResult) {
		if (positiveResult) {
			SharedPreferences prefs = getSharedPreferences(PreferenceConstants.PREFERENCE_NAME, MODE_PRIVATE);
			SharedPreferences.Editor editor = prefs.edit();
			//editor.remove(PreferenceConstants.PREFERENCE_LEVEL_ROW);


			editor.commit();
			Toast.makeText(this, R.string.saved_game_erased_notification,
                    Toast.LENGTH_SHORT).show();
		}
	}
}
