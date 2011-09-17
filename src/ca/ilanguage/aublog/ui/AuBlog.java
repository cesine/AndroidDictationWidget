package ca.ilanguage.aublog.ui;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.DisplayMetrics;
import ca.ilanguage.aublog.preferences.*;
//import ca.ilanguage.aublog.util.DebugLog;

public class AuBlog extends Activity{
	
	// If the version is a negative number, debug features (logging and a debug menu)
    // are enabled.
    public static final int VERSION = 5;
    public static final int AMOUNTOFATTEMPTS = 3;
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        SharedPreferences prefs = getSharedPreferences(PreferenceConstants.PREFERENCE_NAME, MODE_PRIVATE);
        final boolean debugLogs = prefs.getBoolean(PreferenceConstants.PREFERENCE_ENABLE_DEBUG, false);
        
        /*
        if (VERSION < 0 || debugLogs) {
        	DebugLog.setDebugLogging(true);
        } else {
        	DebugLog.setDebugLogging(false);
        }
        
        DebugLog.d("AuBlog", "onCreate");
        */
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);

        int defaultWidth = 480;
        int defaultHeight = 320;
        if (dm.widthPixels != defaultWidth) {
        	float ratio =((float)dm.widthPixels) / dm.heightPixels;
        	defaultWidth = (int)(defaultHeight * ratio);
        }
        
    }


}
