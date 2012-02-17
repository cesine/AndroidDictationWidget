package ca.ilanguage.dictation.widget.service;


import ca.ilanguage.dictation.widget.R;
import ca.ilanguage.dictation.widget.model.Constants;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.media.MediaRecorder;
import android.os.IBinder;

/**
 * A service to record and save a dictation to the SDCard and its filename and status messages to the 
 * Database.
 * 
 * It will modify the database entry of the transcription
 * 
 * Notes: 
 *  *the service is started with startForeground which means it will display a notification in the notification area that the uesr can click on
 * while its running.
 *  *the service can be turned off by the operating system if its low in memory or for other reasons. In these cases the service tries to 
 *  save the audio, but there is no guarantee that the entire process will be carried out. Make sure the audio file exists before you play it.
 *  *the service saves to the database twice, once just before the recorder is started, and once just after the recorder is stopped (which generally coincides with when the service is killed)
 *   
 * Sample client code:
     //Start dictation service
 	 Intent intent = new Intent(this, DictationRecorderService.class);
 	 intent.setData(mUri);
 	 intent.putExtra(DictationRecorderService.EXTRA_AUDIOFILE_FULL_PATH, mAudioResultsFile);
     intent.putExtra(DictationRecorderService.EXTRA_AUDIOFILE_STATUS, mAudioResultsFileStatus);
     startService(intent); 
	 mRecording = true;
	 
	 //Stop dictation service
	 if (mRecording == true){
		 Intent intent = new Intent(this, DictationRecorderService.class);
		 stopService(intent);
	 }
 * 
 * @author gina
 *
 */
public class DictationRecorderService extends Service {
	
	protected static String TAG = "DictationRecorderService";
	private NotificationManager mNM;
	private Notification mNotification;
	private int NOTIFICATION = 7029;
	private PendingIntent mContentIntent;
	private int mIconId = R.drawable.stat_aublog;
	
	private String mAudioResultsFile ="";
	private String mAudioResultsFileStatus ="";
	public static final String EXTRA_AUDIOFILE_FULL_PATH = "audioFilePath";
	public static final String EXTRA_AUDIOFILE_STATUS = "audioFileStatus";
	public static final String EXTRA_DEVICE_INFO = "deviceInfo";
	

	private Long mStartTime;
    private Long mEndTime;
    private Long mTimeAudioWasRecorded;
    private String mAudioSource ="internal mic";;//bluetooth(record,play), phone(recordmic, play earpiece) for privacy, speaker(record mic, play speaker)
    private Boolean mUseBluetooth;
    private Boolean mUsePhoneEarPiece;
    private String mDateString ="";
    private String mInstallId;
    private String mDeviceInfo="";
    
    private String mDirectory = Constants.OUTPUT_DIRECTORY;
    private MediaRecorder mRecorder;
    
	
	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		//TODO
	}

	@Override
	public void onDestroy() {
		//TODO
		super.onDestroy();
		
	}

	@Override
	public void onLowMemory() {
		//TODO
		
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		//TODO
		return START_STICKY;
	}

	private void saveMetaDataToDatabase(){
		//TODO
		
	}

	private void saveRecording(){
		//TODO
	}
	private void sendForTranscription(){
		//TODO
	}
}
