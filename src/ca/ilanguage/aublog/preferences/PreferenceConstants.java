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

public class PreferenceConstants {
	/*
	 * TODO consider having three values for sound enabled, 
	 * 0 no TTS
	 * 1 TTS always
	 * 2 TTS only when head set (or blue tooth) is plugged in? (add a listener for this in the main menu activity as it is always present wen the app is running?
	 */
    public static final String PREFERENCE_SOUND_ENABLED = "enableSound";
    public static final String PREFERENCE_SAFE_MODE = "safeMode";
    public static final String PREFERENCE_SESSION_ID = "session";
    public static final String PREFERENCE_LAST_VERSION = "lastVersion";
    public static final String PREFERENCE_STATS_ENABLED = "enableStats";
    public static final String PREFERENCE_CLICK_ATTACK = "enableClickAttack";
   
    public static final String PREFERENCE_ENABLE_DEBUG = "enableDebug";
    
    public static final String PREFERENCE_NAME = "AuBlogPrefs";
    public static final String PREFERENCE_ACCOUNT = "bloggerAccount";
    public static final String PREFERENCE_PASSWORD = "bloggerPassword";
	
    public static final String PREFERENCE_DRAFT_TREE_IS_FRESH = "draftTreeIsFresh";
    public static final String PREFERENCE_BLOG_SIGNATURE = "blogSignature";
    public static final String PREFERENCE_EMAIL_DRAFTS_TREE = "emailDraftsTree";
    public static final String PREFERENCE_FILE_MANAGER_INSTALLED ="fileManagerInstalled";
    public static final String PREFERENCE_OPEN_AUDIO_DIR = "openAudioDir";
    public static final String PREFERENCE_OPEN_JSON_TXT = "openJsonTxt";
    
    public static final String OUTPUT_FILE_NAME_FOR_DRAFT_EXPORT= "aublog_exported_drafts_json_format.txt";
    public static final String OUTPUT_AUBLOG_DIRECTORY = "/sdcard/AuBlog/";
    
    public static final String AUBLOG_INSTALL_ID ="aublogInstallId";
}
