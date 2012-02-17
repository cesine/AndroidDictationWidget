package ca.ilanguage.dictation.widget.model;


public class Constants {
	public static final String KILL_INTENT = NonPublicConstants.NON_PUBLIC_INTENT_KILL_AUBLOG;
	public static final String IS_DICTATION_STILL_RECORDING_INTENT = NonPublicConstants.NON_PUBLIC_INTENT_IS_DICTATION_STILL_RECORDING;
	public static final String EXTRA_CURRENT_CONTENTS ="currentContents";
	public static final String EXTRA_TRANSCRIPTION_RETURNED = "returnedTranscriptionBoolean";
	public static final String EXTRA_FROM_NOTIFICATION_RECORDING_STILL_RUNNING ="recordingStillRunning";
	public static final String EXTRA_PROMPT_USER_TO_IMPORT_TRANSCRIPTION_INTO_BLOG = "askUserIfWantToImportTranscriptionIntoBlogEntry";
	public static final String EXTRA_FRESH_TRANSCRIPTION_CONTENTS = "freshTranscriptionContents";
	//private Boolean mReturnedTranscription; //check on reload?
	public static final String REFRESH_AUDIOFILE_INTENT = NonPublicConstants.NONPUBLIC_INTENT_AUDIOFILE_RECORDED_AND_SAVED;
	public static final String REFRESH_TRANSCRIPTION_INTENT = NonPublicConstants.NONPUBLIC_INTENT_TRANSCRIPTION_RECEIVED;
	public static final String DICTATION_SENT_INTENT = NonPublicConstants.NONPUBLIC_INTENT_DICTATION_SENT;
	public static final String DICTATION_STILL_RECORDING_INTENT = NonPublicConstants.NON_PUBLIC_INTENT_DICTATION_STILL_RECORDING;
	public static final String TRANSCRIPTION_STILL_CONTACTING_INTENT = NonPublicConstants.NON_PUBLIC_INTENT_TRANSCRIPTION_STILL_CONTACTING;

	
    public static final String PREFERENCE_SOUND_ENABLED = "enableSound";
    public static final String PREFERENCE_USE_BLUETOOTH_AUDIO = "useBluetoothAudio";
    public static final String PREFERENCE_USE_PHONE_EARPIECE_AUDIO = "usePhoneEarPieceAudio";
    public static final String PREFERENCE_USE_EARPHONES_AUDIO = "useEarPhonesAudio";
    public static final String PREFERENCE_SESSION_ID = "session";
    public static final String PREFERENCE_LAST_VERSION = "lastVersion";
    public static final String PREFERENCE_STATS_ENABLED = "enableStats";
    public static final String PREFERENCE_ENABLE_DEBUG = "enableDebug";
    
    public static final String PREFERENCE_NAME = "AuBlogPrefs";
	
    public static final String PREFERENCE_FILE_MANAGER_INSTALLED ="fileManagerInstalled";
    public static final String PREFERENCE_OPEN_AUDIO_DIR = "openAudioDir";
    public static final String PREFERENCE_OPEN_JSON_TXT = "openJsonTxt";
    
    public static final String PREFERENCE_MAX_UPLOAD_ON_MOBILE_NETWORK = "maxUploadOnMobleNetwork";
    public static final String PREFERENCE_UPLOAD_WAIT_FOR_WIFI ="uploadWaitForWifi";
    
    public static final String OUTPUT_FILE_NAME_FOR_DRAFT_EXPORT= "aublog_exported_drafts_json_format.txt";
    public static final String OUTPUT_DIRECTORY = "/sdcard/AuBlog/";
    
    public static final String INSTALL_ID ="aublogInstallId";
	
}
