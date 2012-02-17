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

package ca.ilanguage.dictation.widget.preferences;

import java.io.File;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.widget.Toast;

import ca.ilanguage.dictation.widget.R;
import ca.ilanguage.dictation.widget.model.Constants;

public class SetPreferencesActivity extends PreferenceActivity implements
		YesNoDialogPreference.YesNoDialogListener {

	/*
	 * The Install ID is a timestamp generated on install in the Main Menu
	 * what's new logic with 10 random digits appended to it. It is used to
	 * identify anonymously the install to the webserver. It can be tied to an
	 * user id, if user ids logic is added to the server side logic.
	 */
	private String mInstallId;
	
	@Override
	protected void onDestroy() {
		super.onDestroy();

	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		getPreferenceManager().setSharedPreferencesMode(MODE_PRIVATE);
		getPreferenceManager().setSharedPreferencesName(
				Constants.PREFERENCE_NAME);

		// Load the preferences from an XML resource
		addPreferencesFromResource(R.xml.preferences);

		SharedPreferences prefs = getSharedPreferences(
				Constants.PREFERENCE_NAME, MODE_PRIVATE);
		SharedPreferences.Editor editor = prefs.edit();
		/*
		 * set the installid for appending to the labels
		 */
		mInstallId = prefs.getString(Constants.INSTALL_ID, "0");

		/*
		 * Find out if user has a file manager (specifically
		 * org.openintents.filemanager) to be able to open files in the settings
		 * 
		 * if they have it, activate those settings (they were greyed out) If
		 * they don't have it, they can click on the checkbox, it will take them
		 * to the market to get it.
		 */
		final boolean fileManagerAvailable = isIntentAvailable(this,
				"org.openintents.action.PICK_FILE");
		Boolean fileManagerChecked = prefs.getBoolean(
				Constants.PREFERENCE_FILE_MANAGER_INSTALLED, false);
		if (fileManagerChecked && !fileManagerAvailable) {
			Toast.makeText(
					SetPreferencesActivity.this,
					"To open and export recorded files or "
							+ "draft data you can install the OI File Manager, "
							+ "it allows you to browse your SDCARD directly on your mobile device.",
					Toast.LENGTH_LONG).show();
			Intent goToMarket = new Intent(Intent.ACTION_VIEW).setData(Uri
					.parse("market://details?id=org.openintents.filemanager"));

			startActivity(goToMarket);
		}
		if (!fileManagerChecked && fileManagerAvailable) {

			/*
			 * Enable the export and open possibilities
			 */
			editor.putBoolean(Constants.PREFERENCE_FILE_MANAGER_INSTALLED, true);
			editor.commit();
		}
		/*
		 * If the file manager is available ungrey the export/open settings.
		 * Only if there has been audio recorded, ungrey the audio open dir
		 * setting
		 */
		if (fileManagerAvailable) {
			editor.putBoolean(Constants.PREFERENCE_FILE_MANAGER_INSTALLED, true);
			editor.commit();
			Preference exportJson = getPreferenceManager().findPreference(
					"openJsonTxt");
			exportJson.setEnabled(true);
			exportJson.setSelectable(true);
			Preference openAudioDir = getPreferenceManager().findPreference(
					"openAudioDir");
			if (new File(Constants.OUTPUT_DIRECTORY + "/audio").exists()) {
				openAudioDir.setEnabled(true);
				openAudioDir.setSelectable(true);
			} else {
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
	 * @param context
	 *            The application's environment.
	 * @param action
	 *            The Intent action to check for availability.
	 * 
	 * @return True if an Intent with the specified action can be sent and
	 *         responded to, false otherwise.
	 */
	public static boolean isIntentAvailable(Context context, String action) {
		final PackageManager packageManager = context.getPackageManager();
		final Intent intent = new Intent(action);
		List<ResolveInfo> list = packageManager.queryIntentActivities(intent,
				PackageManager.MATCH_DEFAULT_ONLY);
		return list.size() > 0;
	}

	public void onDialogClosed(boolean positiveResult) {
		if (positiveResult) {
			/*
			 * TODO open the database provider, delete database and recreate
			 * with the default entries
			 */
			startActivity(new Intent(
					android.provider.Settings.ACTION_MANAGE_APPLICATIONS_SETTINGS));
			// wipeUserData

			Toast.makeText(this, R.string.saved_data_erased_notification,
					Toast.LENGTH_SHORT).show();
		}
	}
}
