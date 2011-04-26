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

package ca.ilanguage.aublog.ui;


import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import com.google.gdata.data.Feed;
import com.google.gdata.data.contacts.Gender;

import ca.ilanguage.aublog.db.DBAdapter;
import ca.ilanguage.aublog.db.AuBlogHistoryDatabase.AuBlogHistory;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.SQLException;
import android.media.AudioManager;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.Toast;
import ca.ilanguage.aublog.R;
import ca.ilanguage.aublog.util.AndouKun;
import ca.ilanguage.aublog.util.DebugLog;
import ca.ilanguage.aublog.util.DifficultyMenuActivity;
import ca.ilanguage.aublog.util.ExtrasMenuActivity;
import ca.ilanguage.aublog.util.LevelTree;
import ca.ilanguage.aublog.util.MultiTouchFilter;
import ca.ilanguage.aublog.util.PreferenceConstants;
import ca.ilanguage.aublog.util.SetPreferencesActivity;
import ca.ilanguage.aublog.util.SingleTouchFilter;
import ca.ilanguage.aublog.util.TouchFilter;
import ca.ilanguage.aublog.util.UIConstants;

public class MainMenuActivity extends Activity {
    private boolean mPaused;
    private View mStartButton;
    private View mOptionsButton;
    private View mExtrasButton;
    private View mDraftsButton;
    private View mBackground;
    private View mTicker;
    private Animation mButtonFlickerAnimation;
    private Animation mFadeOutAnimation;
    private Animation mAlternateFadeOutAnimation;
    private Animation mFadeInAnimation;
    private boolean mJustCreated;
    private String mSelectedControlsString;
    
    private ProgressDialog viewProgress = null;
	private final String MSG_KEY = "value";
	public static Feed resultFeed = null;
	private DBAdapter mDbHelper;
	private static Cursor setting = null;
	int viewStatus = 0;
	public static final int AMOUNTOFATTEMPTS = 7;
	private int attempt = 0;

    
    private final static int WHATS_NEW_DIALOG = 0;
    private final static int TILT_TO_SCREEN_CONTROLS_DIALOG = 1;
    private final static int CONTROL_SETUP_DIALOG = 2;
	protected static final String TAG = "MainMenuActivity";
        
    // Create an anonymous implementation of OnClickListener
    private View.OnClickListener sContinueButtonListener = new View.OnClickListener() {
        public void onClick(View v) {
            if (!mPaused) {
                Intent i = new Intent(getBaseContext(), AndouKun.class);
                v.startAnimation(mButtonFlickerAnimation);
                mFadeOutAnimation.setAnimationListener(new StartActivityAfterAnimation(i));
                mBackground.startAnimation(mFadeOutAnimation);
                mOptionsButton.startAnimation(mAlternateFadeOutAnimation);
                mExtrasButton.startAnimation(mAlternateFadeOutAnimation);
                mDraftsButton.startAnimation(mAlternateFadeOutAnimation);
                mTicker.startAnimation(mAlternateFadeOutAnimation);
                mPaused = true;
            }
        }
    };
    
    private View.OnClickListener sOptionButtonListener = new View.OnClickListener() {
        public void onClick(View v) {
            if (!mPaused) {
                Intent i = new Intent(getBaseContext(), SetPreferencesActivity.class);

                v.startAnimation(mButtonFlickerAnimation);
                mFadeOutAnimation.setAnimationListener(new StartActivityAfterAnimation(i));
                mBackground.startAnimation(mFadeOutAnimation);
                mStartButton.startAnimation(mAlternateFadeOutAnimation);
                mExtrasButton.startAnimation(mAlternateFadeOutAnimation);
                mDraftsButton.startAnimation(mAlternateFadeOutAnimation);
                mTicker.startAnimation(mAlternateFadeOutAnimation);
                mPaused = true;
            }
        }
    };
    
    private View.OnClickListener sExtrasButtonListener = new View.OnClickListener() {
        public void onClick(View v) {
            if (!mPaused) {
            	//Intent i = new Intent(getBaseContext(), Settings.class);
            	Intent i = new Intent(getBaseContext(), AboutActivity.class);

                v.startAnimation(mButtonFlickerAnimation);
                mButtonFlickerAnimation.setAnimationListener(new StartActivityAfterAnimation(i));
                mPaused = true;
                
            }
        }
    };
    private View.OnClickListener sDraftsButtonListener = new View.OnClickListener() {
        public void onClick(View v) {
            if (!mPaused) {
            	
            	Toast.makeText(MainMenuActivity.this, "Creating drafts tree, this may take a few moments. ", Toast.LENGTH_LONG).show();
            	generateDraftTree();
            	//Intent i = new Intent(getBaseContext(), Settings.class);
            	Intent i = new Intent(getBaseContext(), ViewDraftTreeActivity.class);

                v.startAnimation(mButtonFlickerAnimation);
                mButtonFlickerAnimation.setAnimationListener(new StartActivityAfterAnimation(i));
                mPaused = true;
                
            }
        }
    };
    
    private View.OnClickListener sStartButtonListener = new View.OnClickListener() {
        public void onClick(View v) {
            if (!mPaused) {
//            	Intent i = new Intent(getBaseContext(), DifficultyMenuActivity.class);
//            	i.putExtra("newGame", true);
            	
            	/*
            	 * A full working sample client, containing all the sample code 
            	 * shown in this document, is available in the Java client 
            	 * library distribution, under the directory gdata/java/sample/blogger/BloggerClient.java.
            	 I. Public feeds don't require any authentication, but they are read-only. If you want to modify blogs, then your client needs to authenticate before requesting private feeds.
            	 this document assume you have an authenticated GoogleService object.
            	 */
            	mDbHelper = new DBAdapter(MainMenuActivity.this);
				try {
					mDbHelper.open();
				} catch (SQLException e) {
					// Log.e(TAG, "Database has not opened");
				}
				setting = mDbHelper.fetchSettindById(1);
//				Toast.makeText(MainMenuActivity.this, "setting "+setting.toString(), Toast.LENGTH_LONG).show();
				startManagingCursor(setting);
				if (setting.getCount() != 0) {
					if ((setting
							.getString(
									setting
											.getColumnIndexOrThrow(DBAdapter.KEY_LOGIN))
							.length() == 0)
							&& (setting
									.getString(
											setting
													.getColumnIndexOrThrow(DBAdapter.KEY_PASSWORD))
									.length() == 0)) 
					{
						mDbHelper.close();
						setting.close();
//						Alert
//								.showAlert(MainMenuActivity.this,
//										"Profile is not created",
//										"Please, input 'login/password' in settings");
						Toast.makeText(MainMenuActivity.this, "Please input your Blogger username and password in the Settings. ", Toast.LENGTH_LONG).show();

						
						Intent i = new Intent(getBaseContext(), Settings.class);

		                v.startAnimation(mButtonFlickerAnimation);
		                mButtonFlickerAnimation.setAnimationListener(new StartActivityAfterAnimation(i));
		                mPaused = true;
					} else {
//						Intent i = new Intent(MainMenuActivity.this, CreateBlogEntry.class);
//						
//						
						mDbHelper.close();
						setting.close();
////						v.startAnimation(mButtonFlickerAnimation);
////		                mButtonFlickerAnimation.setAnimationListener(new StartActivityAfterAnimation(i));
//
//						startActivity(i);
						Intent i = new Intent(getBaseContext(), EditBlogEntryActivity.class);

						
				        Uri uri = getContentResolver().insert(AuBlogHistory.CONTENT_URI, null);
						// If we were unable to create a new blog entry, then just finish
				        // this activity.  A RESULT_CANCELED will be sent back to the
				        // original activity if they requested a result.
				        if (uri == null) {
				            Log.e(TAG, "Failed to insert new blog entry into " + getIntent().getData());
				            Toast.makeText(MainMenuActivity.this, "Failed to insert new blog entry into "+ getIntent().getData()+" with this uri"+AuBlogHistory.CONTENT_URI, Toast.LENGTH_LONG).show();
				            finish();
				            return;
				        }
				        i.setData(uri);
						
		                v.startAnimation(mButtonFlickerAnimation);
		                mButtonFlickerAnimation.setAnimationListener(new StartActivityAfterAnimation(i));
		                mPaused = true;
						
					}
				} else {
					mDbHelper.close();
					setting.close();
//					Alert
//							.showAlert(MainMenuActivity.this,
//									"Profile is not created",
//									"Please, input 'login/password' in settings");
					Toast.makeText(MainMenuActivity.this, "Please input your Blogger username and password in the Settings. ", Toast.LENGTH_LONG).show();

					
					Intent i = new Intent(getBaseContext(), Settings.class);

	                v.startAnimation(mButtonFlickerAnimation);
	                mButtonFlickerAnimation.setAnimationListener(new StartActivityAfterAnimation(i));
	                mPaused = true;
				}
            	
            	
                
                mPaused = true;
                
            }
        }
    };
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mainmenu);
        mPaused = true;
        
        mStartButton = findViewById(R.id.startButton);
        mOptionsButton = findViewById(R.id.optionButton);
        mBackground = findViewById(R.id.mainMenuBackground);
        
        if (mOptionsButton != null) {
            mOptionsButton.setOnClickListener(sOptionButtonListener);
        }
        
        mExtrasButton = findViewById(R.id.extrasButton);
        mExtrasButton.setOnClickListener(sExtrasButtonListener);
        
        mDraftsButton = findViewById(R.id.draftsButton);
        mDraftsButton.setOnClickListener(sDraftsButtonListener);
        
        mButtonFlickerAnimation = AnimationUtils.loadAnimation(this, R.anim.button_flicker);
        mFadeOutAnimation = AnimationUtils.loadAnimation(this, R.anim.fade_out);
        mAlternateFadeOutAnimation = AnimationUtils.loadAnimation(this, R.anim.fade_out);
        mFadeInAnimation = AnimationUtils.loadAnimation(this, R.anim.fade_in);
        
        SharedPreferences prefs = getSharedPreferences(PreferenceConstants.PREFERENCE_NAME, MODE_PRIVATE);
        final int row = prefs.getInt(PreferenceConstants.PREFERENCE_LEVEL_ROW, 0);
        final int index = prefs.getInt(PreferenceConstants.PREFERENCE_LEVEL_INDEX, 0);
        int levelTreeResource = R.xml.level_tree;
        if (row != 0 || index != 0) {
            final int linear = prefs.getInt(PreferenceConstants.PREFERENCE_LINEAR_MODE, 0);
            if (linear != 0) {
            	levelTreeResource = R.xml.linear_level_tree;
            }
        }
        
        if (!LevelTree.isLoaded(levelTreeResource)) {
        	LevelTree.loadLevelTree(levelTreeResource, this);
        	LevelTree.loadAllDialog(this);
        }
        
        mTicker = findViewById(R.id.ticker);
        if (mTicker != null) {
        	mTicker.setFocusable(true);
        	mTicker.requestFocus();
        	mTicker.setSelected(true);
        }
        
        mJustCreated = true;
        
        // Keep the volume control type consistent across all activities.
        setVolumeControlStream(AudioManager.STREAM_MUSIC);
        
        //MediaPlayer mp = MediaPlayer.create(this, R.raw.bwv_115);
        //mp.start();
      
        
    }
    
    
    @Override
    protected void onPause() {
        super.onPause();
        mPaused = true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        mPaused = false;
        
        mButtonFlickerAnimation.setAnimationListener(null);
        
        if (mStartButton != null) {
            
            // Change "start" to "continue" if there's a saved game.
            SharedPreferences prefs = getSharedPreferences(PreferenceConstants.PREFERENCE_NAME, MODE_PRIVATE);
            final int row = prefs.getInt(PreferenceConstants.PREFERENCE_LEVEL_ROW, 0);
            final int index = prefs.getInt(PreferenceConstants.PREFERENCE_LEVEL_INDEX, 0);
            if (row != 0 || index != 0) {
            	((ImageView)mStartButton).setImageDrawable(getResources().getDrawable(R.drawable.ui_button_continue));
                mStartButton.setOnClickListener(sContinueButtonListener);
            } else {
            	((ImageView)mStartButton).setImageDrawable(getResources().getDrawable(R.drawable.ui_button_start));
                mStartButton.setOnClickListener(sStartButtonListener);
            }
            
            TouchFilter touch;
			final int sdkVersion = Integer.parseInt(Build.VERSION.SDK);
            if (sdkVersion < Build.VERSION_CODES.ECLAIR) {
            	touch = new SingleTouchFilter();
            } else {
            	touch = new MultiTouchFilter();
            }
            
            final int lastVersion = prefs.getInt(PreferenceConstants.PREFERENCE_LAST_VERSION, 0);
            if (lastVersion == 0) {
            	// This is the first time the game has been run.  
            	// Pre-configure the control options to match the device.
            	// The resource system can tell us what this device has.
            	// TODO: is there a better way to do this?  Seems like a kind of neat
            	// way to do custom device profiles.
            	final String navType = getString(R.string.nav_type);
            	mSelectedControlsString = getString(R.string.control_setup_dialog_trackball);
            	if (navType != null) {
            		if (navType.equalsIgnoreCase("DPad")) {
            			// Turn off the click-to-attack pref on devices that have a dpad.
            			SharedPreferences.Editor editor = prefs.edit();
                    	editor.putBoolean(PreferenceConstants.PREFERENCE_CLICK_ATTACK, false);
                    	editor.commit();
                    	mSelectedControlsString = getString(R.string.control_setup_dialog_dpad);
            		} else if (navType.equalsIgnoreCase("None")) {
            			SharedPreferences.Editor editor = prefs.edit();
            			
                        // This test relies on the PackageManager if api version >= 5.
            			if (touch.supportsMultitouch(this)) {
            				// Default to screen controls.
            				editor.putBoolean(PreferenceConstants.PREFERENCE_SCREEN_CONTROLS, true);
            				mSelectedControlsString = getString(R.string.control_setup_dialog_screen);
            			} else {
            				// Turn on tilt controls if there's nothing else.
	                    	editor.putBoolean(PreferenceConstants.PREFERENCE_TILT_CONTROLS, true);
	                    	mSelectedControlsString = getString(R.string.control_setup_dialog_tilt);
            			}
                    	editor.commit();
                    	
            		}
            	}
            	
            }

            if (Math.abs(lastVersion) < Math.abs(AndouKun.VERSION)) {
            	// This is a new install or an upgrade.
            	
            	// Check the safe mode option.
            	// Useful reference: http://en.wikipedia.org/wiki/List_of_Android_devices
            	if (Build.PRODUCT.contains("morrison") ||	// Motorola Cliq/Dext
            			Build.MODEL.contains("Pulse") ||	// Huawei Pulse
            			Build.MODEL.contains("U8220") ||	// Huawei Pulse
            			Build.MODEL.contains("U8230") ||	// Huawei U8230
            			Build.MODEL.contains("MB300") ||	// Motorola Backflip
            			Build.MODEL.contains("MB501") ||	// Motorola Quench / Cliq XT
            			Build.MODEL.contains("Behold+II")) {	// Samsung Behold II
            		// These are all models that users have complained about.  They likely use
            		// the same buggy QTC graphics driver.  Turn on Safe Mode by default
            		// for these devices.
            		SharedPreferences.Editor editor = prefs.edit();
                	editor.putBoolean(PreferenceConstants.PREFERENCE_SAFE_MODE, true);
                	editor.commit();
            	}
            	
            	SharedPreferences.Editor editor = prefs.edit();

            	if (lastVersion > 0 && lastVersion < 14) {
            		// if the user has beat the game once, go ahead and unlock stuff for them.
            		if (prefs.getInt(PreferenceConstants.PREFERENCE_LAST_ENDING, -1) != -1) {
            			editor.putBoolean(PreferenceConstants.PREFERENCE_EXTRAS_UNLOCKED, true);
            		}
            	}
            	
            	// show what's new message
            	editor.putInt(PreferenceConstants.PREFERENCE_LAST_VERSION, AndouKun.VERSION);
            	editor.commit();
            	
            	showDialog(WHATS_NEW_DIALOG);
            	
            	// screen controls were added in version 14
            	if (lastVersion > 0 && lastVersion < 14 && 
            			prefs.getBoolean(PreferenceConstants.PREFERENCE_TILT_CONTROLS, false))  {
	    			if (touch.supportsMultitouch(this)) {
	    				// show message about switching from tilt to screen controls
	    				showDialog(TILT_TO_SCREEN_CONTROLS_DIALOG);
	    			}
            	} else if (lastVersion == 0) {
            		// show message about auto-selected control schemes.
            		showDialog(CONTROL_SETUP_DIALOG);
            	}
            	
            }
            
        }
      
        
        if (mBackground != null) {
        	mBackground.clearAnimation();
        }
        
        if (mTicker != null) {
        	mTicker.clearAnimation();
        	mTicker.setAnimation(mFadeInAnimation);
        }
        
        if (mJustCreated) {
        	if (mDraftsButton != null) {
                mDraftsButton.startAnimation(AnimationUtils.loadAnimation(this, R.anim.button_slide));
            }
        	if (mStartButton != null) {
            	Animation anim = AnimationUtils.loadAnimation(this, R.anim.button_slide);
                anim.setStartOffset(500L);
                mStartButton.startAnimation(anim);
            }
            if (mExtrasButton != null) {
            	Animation anim = AnimationUtils.loadAnimation(this, R.anim.button_slide);
                anim.setStartOffset(500L);
                mExtrasButton.startAnimation(anim);
            }
            
            if (mOptionsButton != null) {
            	Animation anim = AnimationUtils.loadAnimation(this, R.anim.button_slide);
                anim.setStartOffset(1000L);
                mOptionsButton.startAnimation(anim);
            }
            mJustCreated = false;
            
        } else {
        	mStartButton.clearAnimation();
        	mOptionsButton.clearAnimation();
        	mExtrasButton.clearAnimation();
        	mDraftsButton.clearAnimation();
        }
        
        
    }
    
    @Override
	protected Dialog onCreateDialog(int id) {
    	Dialog dialog;
		if (id == WHATS_NEW_DIALOG) {
			dialog = new AlertDialog.Builder(this)
            .setTitle(R.string.whats_new_dialog_title)
            .setPositiveButton(R.string.whats_new_dialog_ok, null)
            .setMessage(R.string.whats_new_dialog_message)
            .create();
		} else if (id == TILT_TO_SCREEN_CONTROLS_DIALOG) {
			dialog = new AlertDialog.Builder(this)
            .setTitle(R.string.onscreen_tilt_dialog_title)
            .setPositiveButton(R.string.onscreen_tilt_dialog_ok, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
        			SharedPreferences prefs = getSharedPreferences(PreferenceConstants.PREFERENCE_NAME, MODE_PRIVATE);
        			SharedPreferences.Editor editor = prefs.edit();
        			editor.putBoolean(PreferenceConstants.PREFERENCE_SCREEN_CONTROLS, true);
        			editor.commit();
                }
            })
            .setNegativeButton(R.string.onscreen_tilt_dialog_cancel, null)
            .setMessage(R.string.onscreen_tilt_dialog_message)
            .create();
		} else if (id == CONTROL_SETUP_DIALOG) {
			String messageFormat = getResources().getString(R.string.control_setup_dialog_message);  
			String message = String.format(messageFormat, mSelectedControlsString);
			CharSequence sytledMessage = Html.fromHtml(message);  // lame.
			dialog = new AlertDialog.Builder(this)
            .setTitle(R.string.control_setup_dialog_title)
            .setPositiveButton(R.string.control_setup_dialog_ok, null)
            .setNegativeButton(R.string.control_setup_dialog_change, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                	Intent i = new Intent(getBaseContext(), SetPreferencesActivity.class);
                    i.putExtra("controlConfig", true);
                    startActivity(i);  
                }
            })
            .setMessage(sytledMessage)
            .create();
		} else {
			dialog = super.onCreateDialog(id);
		}
		return dialog;
	}


    public  String generateDraftTree(){
//    	BufferedWriter mOut;
    	
    	String mResultsFile="draft_space_tree.js";
//    	FileWriter fstream;
    	/*
    	 * If you're using API Level 8 or greater, use getExternalFilesDir() to open a File that 
    	 * represents the external storage directory where you should save your files. 
    	 * This method takes a type parameter that specifies the type of subdirectory you want, 
    	 * such as DIRECTORY_MUSIC and DIRECTORY_RINGTONES (pass null to receive the root of your 
    	 * application's file directory). This method will create the appropriate directory if necessary.
    	 * String path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Android/data/ca.ilanguage.aublog/files/";
    	
    	 */
//    	String path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Android/data/ca.ilanguage.aublog/files/";
    	
    	String fname = mResultsFile;
    	File file = new File (getExternalFilesDir(null), mResultsFile);
    	
        try {
//            // Make sure the Pictures directory exists.
//        	 boolean exists = (new File(path)).exists();  
//             if (!exists){ new File(path).mkdirs(); }
             // Open output stream
             FileOutputStream fOut = new FileOutputStream (file);
//            fstream = new FileWriter(mResultsFile,true);
//    		mOut = new BufferedWriter(fstream);
        	String begining="var labelType, useGradients, nativeTextSupport, animate;\n\n(function() {\n  var ua = navigator.userAgent,\n      iStuff = ua.match(/iPhone/i) || ua.match(/iPad/i),\n      typeOfCanvas = typeof HTMLCanvasElement,\n      nativeCanvasSupport = (typeOfCanvas == 'object' || typeOfCanvas == 'function'),\n      textSupport = nativeCanvasSupport \n        && (typeof document.createElement('canvas').getContext('2d').fillText == 'function');\n  //I'm setting this based on the fact that ExCanvas provides text support for IE\n  //and that as of today iPhone/iPad current text support is lame\n  labelType = (!nativeCanvasSupport || (textSupport && !iStuff))? 'Native' : 'HTML';\n  nativeTextSupport = labelType == 'Native';\n  useGradients = nativeCanvasSupport;\n  animate = !(iStuff || !nativeCanvasSupport);\n})();\n\nvar Log = {\n  elem: false,\n  write: function(text){\n    if (!this.elem) \n      this.elem = document.getElementById('log');\n    this.elem.innerHTML = text;\n    this.elem.style.left = (200 - this.elem.offsetWidth / 2) + 'px';\n  }\n};\n\n\n\nfunction init(){\n    //init data\n";
        	
        	String end="\n    //end\n    //init Spacetree\n    //Create a new ST instance\n    var st = new $jit.ST({\n    	orientation: "+'"'+"top"+'"'+",\n    	indent:10,\n        //id of viz container element\n        injectInto: 'infovis',\n        //set duration for the animation\n        duration: 800,\n        //set animation transition type\n        transition: $jit.Trans.Quart.easeInOut,\n        //set distance between node and its children\n        levelDistance: 50,\n        //enable panning\n        Navigation: {\n          enable:true,\n          panning:true\n        },\n        //set node and edge styles\n        //set overridable=true for styling individual\n        //nodes or edges\n        Node: {\n            height: 30,\n            width: 40,\n            type: 'ellipse',\n            color: '#aaa',\n            overridable: true\n        },\n        \n        Edge: {\n            type: 'bezier',\n            overridable: true\n        },\n        \n        onBeforeCompute: function(node){\n            Log.write("+'"'+"loading "
        	+'"'
        	+" + node.name);\n        },\n        \n        onAfterCompute: function(node){\n            Log.write("
        	+'"'
        	+"<input type='button' value='Edit "
        	+'"'
        	+"+node.name+"
        	+'"'
        	+"' onClick='editId("
        	+'"'
        	+"+node.id+"
        	+'"'
        	+")'/><br /><input type='button' value='Delete "
        	+'"'
        	+"+node.name+"
        	+'"'
        	+"' onClick='deleteId("
        	+'"'
        	+"+node.id+"
        	+'"'
        	+")'/>"
        	+'"'
        	+");\n        },\n        \n        //This method is called on DOM label creation.\n        //Use this method to add event handlers and styles to\n        //your node.\n        onCreateLabel: function(label, node){\n            label.id = node.id;            \n            label.innerHTML = node.name;\n            label.onclick = function(){\n            	if(normal.checked) {\n            	  st.onClick(node.id);\n            	} else {\n                st.setRoot(node.id, 'animate');\n            	}\n            };\n            //set label styles\n            var style = label.style;\n            style.width = 40 + 'px';\n            style.height = 17 + 'px';            \n            style.cursor = 'pointer';\n            style.color = '#333';\n            style.fontSize = '0.8em';\n            style.textAlign= 'center';\n            style.paddingTop = '8px';\n        },\n        \n        //This method is called right before plotting\n        //a node. It's useful for changing an individual node\n        //style properties before plotting it.\n        //The data properties prefixed with a dollar\n        //sign will override the global node style properties.\n        onBeforePlotNode: function(node){\n            //add some color to the nodes in the path between the\n            //root node and the selected node.\n            if (node.selected) {\n                node.data.$color = "+'"'+"#ff7"+'"'+";\n            }\n            else {\n                delete node.data.$color;\n                //if the node belongs to the last plotted level\n                if(!node.anySubnode("+'"'+"exist"+'"'+")) {\n                    //count children number\n                    var count = 0;\n                    node.eachSubnode(function(n) { count++; });\n                    //assign a node color based on\n                    //how many children it has\n                    node.data.$color = ['#aaa', '#abb', '#acc', '#add', '#aee', '#aff'][count];                    \n                }\n            }\n        },\n        \n        //This method is called right before plotting\n        //an edge. It's useful for changing an individual edge\n        //style properties before plotting it.\n        //Edge data proprties prefixed with a dollar sign will\n        //override the Edge global style properties.\n        onBeforePlotLine: function(adj){\n            if (adj.nodeFrom.selected && adj.nodeTo.selected) {\n                adj.data.$color = "+'"'+"#eed"+'"'+";\n                adj.data.$lineWidth = 3;\n            }\n            else {\n                delete adj.data.$color;\n                delete adj.data.$lineWidth;\n            }\n        }\n    });\n    //load json data\n    st.loadJSON(json);\n\n    //compute node positions and layout\n    st.compute();\n    //optional: make a translation of the tree\n    st.geom.translate(new $jit.Complex(-200, 0), "+'"'+"current"+'"'+");\n    //emulate a click on the root node.\n    st.onClick(st.root);\n    //end\n    \n    //Add event handlers to switch spacetree orientation.\n    var top = $jit.id('r-top'), \n        left = $jit.id('r-left'), \n        bottom = $jit.id('r-bottom'), \n        right = $jit.id('r-right'),\n        normal = $jit.id('s-normal');\n        \n    \n    function changeHandler() {\n        if(this.checked) {\n             bottom.disabled = right.disabled = left.disabled = top.disabled = true;\n            st.switchPosition(this.value, "+'"'+"animate"+'"'+", {\n                onComplete: function(){\n                    bottom.disabled = right.disabled = left.disabled = top.disabled = false;\n                }\n            });\n        }\n    };\n    \n    top.onchange = left.onchange = bottom.onchange = right.onchange = changeHandler;\n    //end\n\n}\n";
        	//"{ 		        id: "+"'"+"node02"+"'"+", 		        name: "+"'"+"0.2"+"'"+", 		        data: {}, 		        children: [{ 		            id: "+"'"+"node13"+"'"+", 		            name: "+"'"+"1.3"+"'"+", 		            data: {}, 		            children: [{ 		                id: "+"'"+"node24"+"'"+", 		                name: "+"'"+"2.4"+"'"+", 		                data: {}, 		                children: [{ 		                    id: "+"'"+"node35"+"'"+", 		                    name: "+"'"+"3.5"+"'"+", 		                    data: {}, 		                    children: [{ 		                        id: "+"'"+"node46"+"'"+", 		                        name: "+"'"+"4.6"+"'"+", 		                        data: {}, 		                        children: [] 		                    }] 		                }, { 		                    id: "+"'"+"node37"+"'"+", 		                    name: "+"'"+"3.7"+"'"+", 		                    data: {}, 		                    children: [{ 		                        id: "+"'"+"node48"+"'"+", 		                        name: "+"'"+"4.8"+"'"+", 		                        data: {}, 		                        children: [] 		                    }, { 		                        id: "+"'"+"node49"+"'"+", 		                        name: "+"'"+"4.9"+"'"+", 		                        data: {}, 		                        children: [] 		                    }, { 		                        id: "+"'"+"node410"+"'"+", 		                        name: "+"'"+"4.10"+"'"+", 		                        data: {}, 		                        children: [] 		                    }, { 		                        id: "+"'"+"node411"+"'"+", 		                        name: "+"'"+"4.11"+"'"+", 		                        data: {}, 		                        children: [] 		                    }] 		                }, { 		                    id: "+"'"+"node312"+"'"+", 		                    name: "+"'"+"3.12"+"'"+", 		                    data: {}, 		                    children: [{ 		                        id: "+"'"+"node413"+"'"+", 		                        name: "+"'"+"4.13"+"'"+", 		                        data: {}, 		                        children: [] 		                    }] 		                }, { 		                    id: "+"'"+"node314"+"'"+", 		                    name: "+"'"+"3.14"+"'"+", 		                    data: {}, 		                    children: [{ 		                        id: "+"'"+"node415"+"'"+", 		                        name: "+"'"+"4.15"+"'"+", 		                        data: {}, 		                        children: [] 		                    }, { 		                        id: "+"'"+"node416"+"'"+", 		                        name: "+"'"+"4.16"+"'"+", 		                        data: {}, 		                        children: [] 		                    }, { 		                        id: "+"'"+"node417"+"'"+", 		                        name: "+"'"+"4.17"+"'"+", 		                        data: {}, 		                        children: [] 		                    }, { 		                        id: "+"'"+"node418"+"'"+", 		                        name: "+"'"+"4.18"+"'"+", 		                        data: {}, 		                        children: [] 		                    }] 		                }, { 		                    id: "+"'"+"node319"+"'"+", 		                    name: "+"'"+"3.19"+"'"+", 		                    data: {}, 		                    children: [{ 		                        id: "+"'"+"node420"+"'"+", 		                        name: "+"'"+"4.20"+"'"+", 		                        data: {}, 		                        children: [] 		                    }, { 		                        id: "+"'"+"node421"+"'"+", 		                        name: "+"'"+"4.21"+"'"+", 		                        data: {}, 		                        children: [] 		                    }] 		                }] 		            }, { 		                id: "+"'"+"node222"+"'"+", 		                name: "+"'"+"2.22"+"'"+", 		                data: {}, 		                children: [{ 		                    id: "+"'"+"node323"+"'"+", 		                    name: "+"'"+"3.23"+"'"+", 		                    data: {}, 		                    children: [{ 		                        id: "+"'"+"node424"+"'"+", 		                        name: "+"'"+"4.24"+"'"+", 		                        data: {}, 		                        children: [] 		                    }] 		                }] 		            }] 		        }, { 		            id: "+"'"+"node125"+"'"+", 		            name: "+"'"+"1.25"+"'"+", 		            data: {}, 		            children: [{ 		                id: "+"'"+"node226"+"'"+", 		                name: "+"'"+"2.26"+"'"+", 		                data: {}, 		                children: [{ 		                    id: "+"'"+"node327"+"'"+", 		                    name: "+"'"+"3.27"+"'"+", 		                    data: {}, 		                    children: [{ 		                        id: "+"'"+"node428"+"'"+", 		                        name: "+"'"+"4.28"+"'"+", 		                        data: {}, 		                        children: [] 		                    }, { 		                        id: "+"'"+"node429"+"'"+", 		                        name: "+"'"+"4.29"+"'"+", 		                        data: {}, 		                        children: [] 		                    }] 		                }, { 		                    id: "+"'"+"node330"+"'"+", 		                    name: "+"'"+"3.30"+"'"+", 		                    data: {}, 		                    children: [{ 		                        id: "+"'"+"node431"+"'"+", 		                        name: "+"'"+"4.31"+"'"+", 		                        data: {}, 		                        children: [] 		                    }] 		                }, { 		                    id: "+"'"+"node332"+"'"+", 		                    name: "+"'"+"3.32"+"'"+", 		                    data: {}, 		                    children: [{ 		                        id: "+"'"+"node433"+"'"+", 		                        name: "+"'"+"4.33"+"'"+", 		                        data: {}, 		                        children: [] 		                    }, { 		                        id: "+"'"+"node434"+"'"+", 		                        name: "+"'"+"4.34"+"'"+", 		                        data: {}, 		                        children: [] 		                    }, { 		                        id: "+"'"+"node435"+"'"+", 		                        name: "+"'"+"4.35"+"'"+", 		                        data: {}, 		                        children: [] 		                    }, { 		                        id: "+"'"+"node436"+"'"+", 		                        name: "+"'"+"4.36"+"'"+", 		                        data: {}, 		                        children: [] 		                    }] 		                }] 		            }, { 		                id: "+"'"+"node237"+"'"+", 		                name: "+"'"+"2.37"+"'"+", 		                data: {}, 		                children: [{ 		                    id: "+"'"+"node338"+"'"+", 		                    name: "+"'"+"3.38"+"'"+", 		                    data: {}, 		                    children: [{ 		                        id: "+"'"+"node439"+"'"+", 		                        name: "+"'"+"4.39"+"'"+", 		                        data: {}, 		                        children: [] 		                    }, { 		                        id: "+"'"+"node440"+"'"+", 		                        name: "+"'"+"4.40"+"'"+", 		                        data: {}, 		                        children: [] 		                    }, { 		                        id: "+"'"+"node441"+"'"+", 		                        name: "+"'"+"4.41"+"'"+", 		                        data: {}, 		                        children: [] 		                    }] 		                }, { 		                    id: "+"'"+"node342"+"'"+", 		                    name: "+"'"+"3.42"+"'"+", 		                    data: {}, 		                    children: [{ 		                        id: "+"'"+"node443"+"'"+", 		                        name: "+"'"+"4.43"+"'"+", 		                        data: {}, 		                        children: [] 		                    }] 		                }, { 		                    id: "+"'"+"node344"+"'"+", 		                    name: "+"'"+"3.44"+"'"+", 		                    data: {}, 		                    children: [{ 		                        id: "+"'"+"node445"+"'"+", 		                        name: "+"'"+"4.45"+"'"+", 		                        data: {}, 		                        children: [] 		                    }, { 		                        id: "+"'"+"node446"+"'"+", 		                        name: "+"'"+"4.46"+"'"+", 		                        data: {}, 		                        children: [] 		                    }, { 		                        id: "+"'"+"node447"+"'"+", 		                        name: "+"'"+"4.47"+"'"+", 		                        data: {}, 		                        children: [] 		                    }] 		                }, { 		                    id: "+"'"+"node348"+"'"+", 		                    name: "+"'"+"3.48"+"'"+", 		                    data: {}, 		                    children: [{ 		                        id: "+"'"+"node449"+"'"+", 		                        name: "+"'"+"4.49"+"'"+", 		                        data: {}, 		                        children: [] 		                    }, { 		                        id: "+"'"+"node450"+"'"+", 		                        name: "+"'"+"4.50"+"'"+", 		                        data: {}, 		                        children: [] 		                    }, { 		                        id: "+"'"+"node451"+"'"+", 		                        name: "+"'"+"4.51"+"'"+", 		                        data: {}, 		                        children: [] 		                    }, { 		                        id: "+"'"+"node452"+"'"+", 		                        name: "+"'"+"4.52"+"'"+", 		                        data: {}, 		                        children: [] 		                    }, { 		                        id: "+"'"+"node453"+"'"+", 		                        name: "+"'"+"4.53"+"'"+", 		                        data: {}, 		                        children: [] 		                    }] 		                }, { 		                    id: "+"'"+"node354"+"'"+", 		                    name: "+"'"+"3.54"+"'"+", 		                    data: {}, 		                    children: [{ 		                        id: "+"'"+"node455"+"'"+", 		                        name: "+"'"+"4.55"+"'"+", 		                        data: {}, 		                        children: [] 		                    }, { 		                        id: "+"'"+"node456"+"'"+", 		                        name: "+"'"+"4.56"+"'"+", 		                        data: {}, 		                        children: [] 		                    }, { 		                        id: "+"'"+"node457"+"'"+", 		                        name: "+"'"+"4.57"+"'"+", 		                        data: {}, 		                        children: [] 		                    }] 		                }] 		            }, { 		                id: "+"'"+"node258"+"'"+", 		                name: "+"'"+"2.58"+"'"+", 		                data: {}, 		                children: [{ 		                    id: "+"'"+"node359"+"'"+", 		                    name: "+"'"+"3.59"+"'"+", 		                    data: {}, 		                    children: [{ 		                        id: "+"'"+"node460"+"'"+", 		                        name: "+"'"+"4.60"+"'"+", 		                        data: {}, 		                        children: [] 		                    }, { 		                        id: "+"'"+"node461"+"'"+", 		                        name: "+"'"+"4.61"+"'"+", 		                        data: {}, 		                        children: [] 		                    }, { 		                        id: "+"'"+"node462"+"'"+", 		                        name: "+"'"+"4.62"+"'"+", 		                        data: {}, 		                        children: [] 		                    }, { 		                        id: "+"'"+"node463"+"'"+", 		                        name: "+"'"+"4.63"+"'"+", 		                        data: {}, 		                        children: [] 		                    }, { 		                        id: "+"'"+"node464"+"'"+", 		                        name: "+"'"+"4.64"+"'"+", 		                        data: {}, 		                        children: [] 		                    }] 		                }] 		            }] 		        }, { 		            id: "+"'"+"node165"+"'"+", 		            name: "+"'"+"1.65"+"'"+", 		            data: {}, 		            children: [{ 		                id: "+"'"+"node266"+"'"+", 		                name: "+"'"+"2.66"+"'"+", 		                data: {}, 		                children: [{ 		                    id: "+"'"+"node367"+"'"+", 		                    name: "+"'"+"3.67"+"'"+", 		                    data: {}, 		                    children: [{ 		                        id: "+"'"+"node468"+"'"+", 		                        name: "+"'"+"4.68"+"'"+", 		                        data: {}, 		                        children: [] 		                    }, { 		                        id: "+"'"+"node469"+"'"+", 		                        name: "+"'"+"4.69"+"'"+", 		                        data: {}, 		                        children: [] 		                    }, { 		                        id: "+"'"+"node470"+"'"+", 		                        name: "+"'"+"4.70"+"'"+", 		                        data: {}, 		                        children: [] 		                    }, { 		                        id: "+"'"+"node471"+"'"+", 		                        name: "+"'"+"4.71"+"'"+", 		                        data: {}, 		                        children: [] 		                    }] 		                }, { 		                    id: "+"'"+"node372"+"'"+", 		                    name: "+"'"+"3.72"+"'"+", 		                    data: {}, 		                    children: [{ 		                        id: "+"'"+"node473"+"'"+", 		                        name: "+"'"+"4.73"+"'"+", 		                        data: {}, 		                        children: [] 		                    }, { 		                        id: "+"'"+"node474"+"'"+", 		                        name: "+"'"+"4.74"+"'"+", 		                        data: {}, 		                        children: [] 		                    }, { 		                        id: "+"'"+"node475"+"'"+", 		                        name: "+"'"+"4.75"+"'"+", 		                        data: {}, 		                        children: [] 		                    }, { 		                        id: "+"'"+"node476"+"'"+", 		                        name: "+"'"+"4.76"+"'"+", 		                        data: {}, 		                        children: [] 		                    }] 		                }, { 		                    id: "+"'"+"node377"+"'"+", 		                    name: "+"'"+"3.77"+"'"+", 		                    data: {}, 		                    children: [{ 		                        id: "+"'"+"node478"+"'"+", 		                        name: "+"'"+"4.78"+"'"+", 		                        data: {}, 		                        children: [] 		                    }, { 		                        id: "+"'"+"node479"+"'"+", 		                        name: "+"'"+"4.79"+"'"+", 		                        data: {}, 		                        children: [] 		                    }] 		                }, { 		                    id: "+"'"+"node380"+"'"+", 		                    name: "+"'"+"3.80"+"'"+", 		                    data: {}, 		                    children: [{ 		                        id: "+"'"+"node481"+"'"+", 		                        name: "+"'"+"4.81"+"'"+", 		                        data: {}, 		                        children: [] 		                    }, { 		                        id: "+"'"+"node482"+"'"+", 		                        name: "+"'"+"4.82"+"'"+", 		                        data: {}, 		                        children: [] 		                    }] 		                }] 		            }, { 		                id: "+"'"+"node283"+"'"+", 		                name: "+"'"+"2.83"+"'"+", 		                data: {}, 		                children: [{ 		                    id: "+"'"+"node384"+"'"+", 		                    name: "+"'"+"3.84"+"'"+", 		                    data: {}, 		                    children: [{ 		                        id: "+"'"+"node485"+"'"+", 		                        name: "+"'"+"4.85"+"'"+", 		                        data: {}, 		                        children: [] 		                    }] 		                }, { 		                    id: "+"'"+"node386"+"'"+", 		                    name: "+"'"+"3.86"+"'"+", 		                    data: {}, 		                    children: [{ 		                        id: "+"'"+"node487"+"'"+", 		                        name: "+"'"+"4.87"+"'"+", 		                        data: {}, 		                        children: [] 		                    }, { 		                        id: "+"'"+"node488"+"'"+", 		                        name: "+"'"+"4.88"+"'"+", 		                        data: {}, 		                        children: [] 		                    }, { 		                        id: "+"'"+"node489"+"'"+", 		                        name: "+"'"+"4.89"+"'"+", 		                        data: {}, 		                        children: [] 		                    }, { 		                        id: "+"'"+"node490"+"'"+", 		                        name: "+"'"+"4.90"+"'"+", 		                        data: {}, 		                        children: [] 		                    }, { 		                        id: "+"'"+"node491"+"'"+", 		                        name: "+"'"+"4.91"+"'"+", 		                        data: {}, 		                        children: [] 		                    }] 		                }, { 		                    id: "+"'"+"node392"+"'"+", 		                    name: "+"'"+"3.92"+"'"+", 		                    data: {}, 		                    children: [{ 		                        id: "+"'"+"node493"+"'"+", 		                        name: "+"'"+"4.93"+"'"+", 		                        data: {}, 		                        children: [] 		                    }, { 		                        id: "+"'"+"node494"+"'"+", 		                        name: "+"'"+"4.94"+"'"+", 		                        data: {}, 		                        children: [] 		                    }, { 		                        id: "+"'"+"node495"+"'"+", 		                        name: "+"'"+"4.95"+"'"+", 		                        data: {}, 		                        children: [] 		                    }, { 		                        id: "+"'"+"node496"+"'"+", 		                        name: "+"'"+"4.96"+"'"+", 		                        data: {}, 		                        children: [] 		                    }] 		                }, { 		                    id: "+"'"+"node397"+"'"+", 		                    name: "+"'"+"3.97"+"'"+", 		                    data: {}, 		                    children: [{ 		                        id: "+"'"+"node498"+"'"+", 		                        name: "+"'"+"4.98"+"'"+", 		                        data: {}, 		                        children: [] 		                    }] 		                }, { 		                    id: "+"'"+"node399"+"'"+", 		                    name: "+"'"+"3.99"+"'"+", 		                    data: {}, 		                    children: [{ 		                        id: "+"'"+"node4100"+"'"+", 		                        name: "+"'"+"4.100"+"'"+", 		                        data: {}, 		                        children: [] 		                    }, { 		                        id: "+"'"+"node4101"+"'"+", 		                        name: "+"'"+"4.101"+"'"+", 		                        data: {}, 		                        children: [] 		                    }, { 		                        id: "+"'"+"node4102"+"'"+", 		                        name: "+"'"+"4.102"+"'"+", 		                        data: {}, 		                        children: [] 		                    }, { 		                        id: "+"'"+"node4103"+"'"+", 		                        name: "+"'"+"4.103"+"'"+", 		                        data: {}, 		                        children: [] 		                    }] 		                }] 		            }, { 		                id: "+"'"+"node2104"+"'"+", 		                name: "+"'"+"2.104"+"'"+", 		                data: {}, 		                children: [{ 		                    id: "+"'"+"node3105"+"'"+", 		                    name: "+"'"+"3.105"+"'"+", 		                    data: {}, 		                    children: [{ 		                        id: "+"'"+"node4106"+"'"+", 		                        name: "+"'"+"4.106"+"'"+", 		                        data: {}, 		                        children: [] 		                    }, { 		                        id: "+"'"+"node4107"+"'"+", 		                        name: "+"'"+"4.107"+"'"+", 		                        data: {}, 		                        children: [] 		                    }, { 		                        id: "+"'"+"node4108"+"'"+", 		                        name: "+"'"+"4.108"+"'"+", 		                        data: {}, 		                        children: [] 		                    }] 		                }] 		            }, { 		                id: "+"'"+"node2109"+"'"+", 		                name: "+"'"+"2.109"+"'"+", 		                data: {}, 		                children: [{ 		                    id: "+"'"+"node3110"+"'"+", 		                    name: "+"'"+"3.110"+"'"+", 		                    data: {}, 		                    children: [{ 		                        id: "+"'"+"node4111"+"'"+", 		                        name: "+"'"+"4.111"+"'"+", 		                        data: {}, 		                        children: [] 		                    }, { 		                        id: "+"'"+"node4112"+"'"+", 		                        name: "+"'"+"4.112"+"'"+", 		                        data: {}, 		                        children: [] 		                    }] 		                }, { 		                    id: "+"'"+"node3113"+"'"+", 		                    name: "+"'"+"3.113"+"'"+", 		                    data: {}, 		                    children: [{ 		                        id: "+"'"+"node4114"+"'"+", 		                        name: "+"'"+"4.114"+"'"+", 		                        data: {}, 		                        children: [] 		                    }, { 		                        id: "+"'"+"node4115"+"'"+", 		                        name: "+"'"+"4.115"+"'"+", 		                        data: {}, 		                        children: [] 		                    }, { 		                        id: "+"'"+"node4116"+"'"+", 		                        name: "+"'"+"4.116"+"'"+", 		                        data: {}, 		                        children: [] 		                    }] 		                }, { 		                    id: "+"'"+"node3117"+"'"+", 		                    name: "+"'"+"3.117"+"'"+", 		                    data: {}, 		                    children: [{ 		                        id: "+"'"+"node4118"+"'"+", 		                        name: "+"'"+"4.118"+"'"+", 		                        data: {}, 		                        children: [] 		                    }, { 		                        id: "+"'"+"node4119"+"'"+", 		                        name: "+"'"+"4.119"+"'"+", 		                        data: {}, 		                        children: [] 		                    }, { 		                        id: "+"'"+"node4120"+"'"+", 		                        name: "+"'"+"4.120"+"'"+", 		                        data: {}, 		                        children: [] 		                    }, { 		                        id: "+"'"+"node4121"+"'"+", 		                        name: "+"'"+"4.121"+"'"+", 		                        data: {}, 		                        children: [] 		                    }] 		                }, { 		                    id: "+"'"+"node3122"+"'"+", 		                    name: "+"'"+"3.122"+"'"+", 		                    data: {}, 		                    children: [{ 		                        id: "+"'"+"node4123"+"'"+", 		                        name: "+"'"+"4.123"+"'"+", 		                        data: {}, 		                        children: [] 		                    }, { 		                        id: "+"'"+"node4124"+"'"+", 		                        name: "+"'"+"4.124"+"'"+", 		                        data: {}, 		                        children: [] 		                    }] 		                }] 		            }, { 		                id: "+"'"+"node2125"+"'"+", 		                name: "+"'"+"2.125"+"'"+", 		                data: {}, 		                children: [{ 		                    id: "+"'"+"node3126"+"'"+", 		                    name: "+"'"+"3.126"+"'"+", 		                    data: {}, 		                    children: [{ 		                        id: "+"'"+"node4127"+"'"+", 		                        name: "+"'"+"4.127"+"'"+", 		                        data: {}, 		                        children: [] 		                    }, { 		                        id: "+"'"+"node4128"+"'"+", 		                        name: "+"'"+"4.128"+"'"+", 		                        data: {}, 		                        children: [] 		                    }, { 		                        id: "+"'"+"node4129"+"'"+", 		                        name: "+"'"+"4.129"+"'"+", 		                        data: {}, 		                        children: [] 		                    }] 		                }] 		            }] 		        }, { 		            id: "+"'"+"node1130"+"'"+", 		            name: "+"'"+"1.130"+"'"+", 		            data: {}, 		            children: [{ 		                id: "+"'"+"node2131"+"'"+", 		                name: "+"'"+"2.131"+"'"+", 		                data: {}, 		                children: [{ 		                    id: "+"'"+"node3132"+"'"+", 		                    name: "+"'"+"3.132"+"'"+", 		                    data: {}, 		                    children: [{ 		                        id: "+"'"+"node4133"+"'"+", 		                        name: "+"'"+"4.133"+"'"+", 		                        data: {}, 		                        children: [] 		                    }, { 		                        id: "+"'"+"node4134"+"'"+", 		                        name: "+"'"+"4.134"+"'"+", 		                        data: {}, 		                        children: [] 		                    }, { 		                        id: "+"'"+"node4135"+"'"+", 		                        name: "+"'"+"4.135"+"'"+", 		                        data: {}, 		                        children: [] 		                    }, { 		                        id: "+"'"+"node4136"+"'"+", 		                        name: "+"'"+"4.136"+"'"+", 		                        data: {}, 		                        children: [] 		                    }, { 		                        id: "+"'"+"node4137"+"'"+", 		                        name: "+"'"+"4.137"+"'"+", 		                        data: {}, 		                        children: [] 		                    }] 		                }] 		            }, { 		                id: "+"'"+"node2138"+"'"+", 		                name: "+"'"+"2.138"+"'"+", 		                data: {}, 		                children: [{ 		                    id: "+"'"+"node3139"+"'"+", 		                    name: "+"'"+"3.139"+"'"+", 		                    data: {}, 		                    children: [{ 		                        id: "+"'"+"node4140"+"'"+", 		                        name: "+"'"+"4.140"+"'"+", 		                        data: {}, 		                        children: [] 		                    }, { 		                        id: "+"'"+"node4141"+"'"+", 		                        name: "+"'"+"4.141"+"'"+", 		                        data: {}, 		                        children: [] 		                    }] 		                }, { 		                    id: "+"'"+"node3142"+"'"+", 		                    name: "+"'"+"3.142"+"'"+", 		                    data: {}, 		                    children: [{ 		                        id: "+"'"+"node4143"+"'"+", 		                        name: "+"'"+"4.143"+"'"+", 		                        data: {}, 		                        children: [] 		                    }, { 		                        id: "+"'"+"node4144"+"'"+", 		                        name: "+"'"+"4.144"+"'"+", 		                        data: {}, 		                        children: [] 		                    }, { 		                        id: "+"'"+"node4145"+"'"+", 		                        name: "+"'"+"4.145"+"'"+", 		                        data: {}, 		                        children: [] 		                    }, { 		                        id: "+"'"+"node4146"+"'"+", 		                        name: "+"'"+"4.146"+"'"+", 		                        data: {}, 		                        children: [] 		                    }, { 		                        id: "+"'"+"node4147"+"'"+", 		                        name: "+"'"+"4.147"+"'"+", 		                        data: {}, 		                        children: [] 		                    }] 		                }] 		            }] 		        }] 		    }; 		     		";

//"{        id: "+'"'+"node02"+'"'+",        name: "+'"'+"0.2"+'"'+",        data: {},        children: []}";
    		fOut.write((begining).getBytes());
    		String id = "0";
        	String data="json = ";
        	data = data+"{id: \""+ "0"
    		+"\",\nname: \""+"root"
    		+"\",\ndata: {"
        	+"},\nchildren: [";
        	fOut.write((data).getBytes());
        	fOut.write(( getSubtree(id) ).getBytes());
        	fOut.write(("]\n};").getBytes());
    		fOut.write((end).getBytes());
    		fOut.flush();
    		fOut.close();
    	} catch (IOException e) {
    		// TODO Auto-generated catch block
    		Toast.makeText(MainMenuActivity.this, "The SDCARD isn't writeable. Is the device being used as a disk drive on a comptuer?\n "+e.toString(), Toast.LENGTH_LONG).show();

    	}

    	//"{         id: 'node02',         name: '0.2',         data: {},         children: [{             id: 'node13',             name: '1.3',             data: {},             children: [{                 id: 'node24',                 name: '2.4',                 data: {},                 children: [{                     id: 'node35',                     name: '3.5',                     data: {},                     children: [{                         id: 'node46',                         name: '4.6',                         data: {},                         children: []                     }]                 }, {                     id: 'node37',                     name: '3.7',                     data: {},                     children: [{                         id: 'node48',                         name: '4.8',                         data: {},                         children: []                     }, {                         id: 'node49',                         name: '4.9',                         data: {},                         children: []                     }, {                         id: 'node410',                         name: '4.10',                         data: {},                         children: []                     }, {                         id: 'node411',                         name: '4.11',                         data: {},                         children: []                     }]                 }, {                     id: 'node312',                     name: '3.12',                     data: {},                     children: [{                         id: 'node413',                         name: '4.13',                         data: {},                         children: []                     }]                 }, {                     id: 'node314',                     name: '3.14',                     data: {},                     children: [{                         id: 'node415',                         name: '4.15',                         data: {},                         children: []                     }, {                         id: 'node416',                         name: '4.16',                         data: {},                         children: []                     }, {                         id: 'node417',                         name: '4.17',                         data: {},                         children: []                     }, {                         id: 'node418',                         name: '4.18',                         data: {},                         children: []                     }]                 }, {                     id: 'node319',                     name: '3.19',                     data: {},                     children: [{                         id: 'node420',                         name: '4.20',                         data: {},                         children: []                     }, {                         id: 'node421',                         name: '4.21',                         data: {},                         children: []                     }]                 }]             }, {                 id: 'node222',                 name: '2.22',                 data: {},                 children: [{                     id: 'node323',                     name: '3.23',                     data: {},                     children: [{                         id: 'node424',                         name: '4.24',                         data: {},                         children: []                     }]                 }]             }]         }, {             id: 'node125',             name: '1.25',             data: {},             children: [{                 id: 'node226',                 name: '2.26',                 data: {},                 children: [{                     id: 'node327',                     name: '3.27',                     data: {},                     children: [{                         id: 'node428',                         name: '4.28',                         data: {},                         children: []                     }, {                         id: 'node429',                         name: '4.29',                         data: {},                         children: []                     }]                 }, {                     id: 'node330',                     name: '3.30',                     data: {},                     children: [{                         id: 'node431',                         name: '4.31',                         data: {},                         children: []                     }]                 }, {                     id: 'node332',                     name: '3.32',                     data: {},                     children: [{                         id: 'node433',                         name: '4.33',                         data: {},                         children: []                     }, {                         id: 'node434',                         name: '4.34',                         data: {},                         children: []                     }, {                         id: 'node435',                         name: '4.35',                         data: {},                         children: []                     }, {                         id: 'node436',                         name: '4.36',                         data: {},                         children: []                     }]                 }]             }, {                 id: 'node237',                 name: '2.37',                 data: {},                 children: [{                     id: 'node338',                     name: '3.38',                     data: {},                     children: [{                         id: 'node439',                         name: '4.39',                         data: {},                         children: []                     }, {                         id: 'node440',                         name: '4.40',                         data: {},                         children: []                     }, {                         id: 'node441',                         name: '4.41',                         data: {},                         children: []                     }]                 }, {                     id: 'node342',                     name: '3.42',                     data: {},                     children: [{                         id: 'node443',                         name: '4.43',                         data: {},                         children: []                     }]                 }, {                     id: 'node344',                     name: '3.44',                     data: {},                     children: [{                         id: 'node445',                         name: '4.45',                         data: {},                         children: []                     }, {                         id: 'node446',                         name: '4.46',                         data: {},                         children: []                     }, {                         id: 'node447',                         name: '4.47',                         data: {},                         children: []                     }]                 }, {                     id: 'node348',                     name: '3.48',                     data: {},                     children: [{                         id: 'node449',                         name: '4.49',                         data: {},                         children: []                     }, {                         id: 'node450',                         name: '4.50',                         data: {},                         children: []                     }, {                         id: 'node451',                         name: '4.51',                         data: {},                         children: []                     }, {                         id: 'node452',                         name: '4.52',                         data: {},                         children: []                     }, {                         id: 'node453',                         name: '4.53',                         data: {},                         children: []                     }]                 }, {                     id: 'node354',                     name: '3.54',                     data: {},                     children: [{                         id: 'node455',                         name: '4.55',                         data: {},                         children: []                     }, {                         id: 'node456',                         name: '4.56',                         data: {},                         children: []                     }, {                         id: 'node457',                         name: '4.57',                         data: {},                         children: []                     }]                 }]             }, {                 id: 'node258',                 name: '2.58',                 data: {},                 children: [{                     id: 'node359',                     name: '3.59',                     data: {},                     children: [{                         id: 'node460',                         name: '4.60',                         data: {},                         children: []                     }, {                         id: 'node461',                         name: '4.61',                         data: {},                         children: []                     }, {                         id: 'node462',                         name: '4.62',                         data: {},                         children: []                     }, {                         id: 'node463',                         name: '4.63',                         data: {},                         children: []                     }, {                         id: 'node464',                         name: '4.64',                         data: {},                         children: []                     }]                 }]             }]         }, {             id: 'node165',             name: '1.65',             data: {},             children: [{                 id: 'node266',                 name: '2.66',                 data: {},                 children: [{                     id: 'node367',                     name: '3.67',                     data: {},                     children: [{                         id: 'node468',                         name: '4.68',                         data: {},                         children: []                     }, {                         id: 'node469',                         name: '4.69',                         data: {},                         children: []                     }, {                         id: 'node470',                         name: '4.70',                         data: {},                         children: []                     }, {                         id: 'node471',                         name: '4.71',                         data: {},                         children: []                     }]                 }, {                     id: 'node372',                     name: '3.72',                     data: {},                     children: [{                         id: 'node473',                         name: '4.73',                         data: {},                         children: []                     }, {                         id: 'node474',                         name: '4.74',                         data: {},                         children: []                     }, {                         id: 'node475',                         name: '4.75',                         data: {},                         children: []                     }, {                         id: 'node476',                         name: '4.76',                         data: {},                         children: []                     }]                 }, {                     id: 'node377',                     name: '3.77',                     data: {},                     children: [{                         id: 'node478',                         name: '4.78',                         data: {},                         children: []                     }, {                         id: 'node479',                         name: '4.79',                         data: {},                         children: []                     }]                 }, {                     id: 'node380',                     name: '3.80',                     data: {},                     children: [{                         id: 'node481',                         name: '4.81',                         data: {},                         children: []                     }, {                         id: 'node482',                         name: '4.82',                         data: {},                         children: []                     }]                 }]             }, {                 id: 'node283',                 name: '2.83',                 data: {},                 children: [{                     id: 'node384',                     name: '3.84',                     data: {},                     children: [{                         id: 'node485',                         name: '4.85',                         data: {},                         children: []                     }]                 }, {                     id: 'node386',                     name: '3.86',                     data: {},                     children: [{                         id: 'node487',                         name: '4.87',                         data: {},                         children: []                     }, {                         id: 'node488',                         name: '4.88',                         data: {},                         children: []                     }, {                         id: 'node489',                         name: '4.89',                         data: {},                         children: []                     }, {                         id: 'node490',                         name: '4.90',                         data: {},                         children: []                     }, {                         id: 'node491',                         name: '4.91',                         data: {},                         children: []                     }]                 }, {                     id: 'node392',                     name: '3.92',                     data: {},                     children: [{                         id: 'node493',                         name: '4.93',                         data: {},                         children: []                     }, {                         id: 'node494',                         name: '4.94',                         data: {},                         children: []                     }, {                         id: 'node495',                         name: '4.95',                         data: {},                         children: []                     }, {                         id: 'node496',                         name: '4.96',                         data: {},                         children: []                     }]                 }, {                     id: 'node397',                     name: '3.97',                     data: {},                     children: [{                         id: 'node498',                         name: '4.98',                         data: {},                         children: []                     }]                 }, {                     id: 'node399',                     name: '3.99',                     data: {},                     children: [{                         id: 'node4100',                         name: '4.100',                         data: {},                         children: []                     }, {                         id: 'node4101',                         name: '4.101',                         data: {},                         children: []                     }, {                         id: 'node4102',                         name: '4.102',                         data: {},                         children: []                     }, {                         id: 'node4103',                         name: '4.103',                         data: {},                         children: []                     }]                 }]             }, {                 id: 'node2104',                 name: '2.104',                 data: {},                 children: [{                     id: 'node3105',                     name: '3.105',                     data: {},                     children: [{                         id: 'node4106',                         name: '4.106',                         data: {},                         children: []                     }, {                         id: 'node4107',                         name: '4.107',                         data: {},                         children: []                     }, {                         id: 'node4108',                         name: '4.108',                         data: {},                         children: []                     }]                 }]             }, {                 id: 'node2109',                 name: '2.109',                 data: {},                 children: [{                     id: 'node3110',                     name: '3.110',                     data: {},                     children: [{                         id: 'node4111',                         name: '4.111',                         data: {},                         children: []                     }, {                         id: 'node4112',                         name: '4.112',                         data: {},                         children: []                     }]                 }, {                     id: 'node3113',                     name: '3.113',                     data: {},                     children: [{                         id: 'node4114',                         name: '4.114',                         data: {},                         children: []                     }, {                         id: 'node4115',                         name: '4.115',                         data: {},                         children: []                     }, {                         id: 'node4116',                         name: '4.116',                         data: {},                         children: []                     }]                 }, {                     id: 'node3117',                     name: '3.117',                     data: {},                     children: [{                         id: 'node4118',                         name: '4.118',                         data: {},                         children: []                     }, {                         id: 'node4119',                         name: '4.119',                         data: {},                         children: []                     }, {                         id: 'node4120',                         name: '4.120',                         data: {},                         children: []                     }, {                         id: 'node4121',                         name: '4.121',                         data: {},                         children: []                     }]                 }, {                     id: 'node3122',                     name: '3.122',                     data: {},                     children: [{                         id: 'node4123',                         name: '4.123',                         data: {},                         children: []                     }, {                         id: 'node4124',                         name: '4.124',                         data: {},                         children: []                     }]                 }]             }, {                 id: 'node2125',                 name: '2.125',                 data: {},                 children: [{                     id: 'node3126',                     name: '3.126',                     data: {},                     children: [{                         id: 'node4127',                         name: '4.127',                         data: {},                         children: []                     }, {                         id: 'node4128',                         name: '4.128',                         data: {},                         children: []                     }, {                         id: 'node4129',                         name: '4.129',                         data: {},                         children: []                     }]                 }]             }]         }, {             id: 'node1130',             name: '1.130',             data: {},             children: [{                 id: 'node2131',                 name: '2.131',                 data: {},                 children: [{                     id: 'node3132',                     name: '3.132',                     data: {},                     children: [{                         id: 'node4133',                         name: '4.133',                         data: {},                         children: []                     }, {                         id: 'node4134',                         name: '4.134',                         data: {},                         children: []                     }, {                         id: 'node4135',                         name: '4.135',                         data: {},                         children: []                     }, {                         id: 'node4136',                         name: '4.136',                         data: {},                         children: []                     }, {                         id: 'node4137',                         name: '4.137',                         data: {},                         children: []                     }]                 }]             }, {                 id: 'node2138',                 name: '2.138',                 data: {},                 children: [{                     id: 'node3139',                     name: '3.139',                     data: {},                     children: [{                         id: 'node4140',                         name: '4.140',                         data: {},                         children: []                     }, {                         id: 'node4141',                         name: '4.141',                         data: {},                         children: []                     }]                 }, {                     id: 'node3142',                     name: '3.142',                     data: {},                     children: [{                         id: 'node4143',                         name: '4.143',                         data: {},                         children: []                     }, {                         id: 'node4144',                         name: '4.144',                         data: {},                         children: []                     }, {                         id: 'node4145',                         name: '4.145',                         data: {},                         children: []                     }, {                         id: 'node4146',                         name: '4.146',                         data: {},                         children: []                     }, {                         id: 'node4147',                         name: '4.147',                         data: {},                         children: []                     }]                 }]             }]         }]     };      }";
//    	MediaScannerConnection mediaScanner = new MediaScannerConnection(MainMenuActivity.this, );
//    	.scanFile(file.toString(), null);
    	return "file created";
    }
    

    public String getSubtree(String id){
    	String[] PROJECTION = new String[] {
        		AuBlogHistory._ID, //0
        		AuBlogHistory.ENTRY_TITLE, 
        		AuBlogHistory.ENTRY_CONTENT, //2
        		AuBlogHistory.ENTRY_LABELS,
        		AuBlogHistory.PUBLISHED, //4
        		AuBlogHistory.DELETED,
        		AuBlogHistory.PARENT_ENTRY //6
        	};
    	String node ="";
    	Boolean firstChild = true;
    	try {
    
    		/*
    		 * find all nodes with this node as its parent
    		 */
        	Cursor cursor = managedQuery(AuBlogHistory.CONTENT_URI, PROJECTION, AuBlogHistory.PARENT_ENTRY +"="+id, null, null);
//        	Toast.makeText(MainMenuActivity.this, "There are \n"+cursor.getCount()+" daughters", Toast.LENGTH_LONG).show();
        	if ((cursor != null) ) {
    			// Requery in case something changed while paused (such as the title)
    			cursor.requery();
                // Make sure we are at the one and only row in the cursor.
                cursor.moveToFirst();
                /*
                 * if this node is flagged as deleted, abort the subtree and the node
                 */
                String nodeAsString=cursor.getString(0)+"\n"+cursor.getString(1)+"\n"+cursor.getString(2)+"\n"+cursor.getString(3)+"\n"+cursor.getString(4)+"\n"+cursor.getString(5)+"\n"+cursor.getString(6);
                Toast.makeText(MainMenuActivity.this, "Full post info:"+nodeAsString, Toast.LENGTH_LONG).show();

                if ( "1".equals(cursor.getString(5) ) ){
                	Toast.makeText(MainMenuActivity.this, "Skipping a deleted/hidden post:"+nodeAsString, Toast.LENGTH_LONG).show();
                	cursor.moveToLast();
                	cursor.moveToNext();
                }else{
                	
                }
                /*
                 * for each daughter, print the daughter and her subtree
                 */
                while (cursor.isAfterLast() == false){
                	if(!firstChild){
                		node= node+",";
                	}
                	String Id=cursor.getString(0);
                	node = node+"{\nid: \""+ Id
                	+"\",\nname: \""+cursor.getString(1)
                	+"\",\ndata: {"
                	+"},\nchildren: [";
                	
                	/*
                	 * find all nodes with this node as its parent
                	 */
                	node = node + getSubtree(Id);

                	node =node+ "]\n} ";
                	firstChild=false;
                	cursor.moveToNext();
                }
                //firstChild=true;
//                cursor.deactivate();
                String temp ="";
                
        	}
    	} catch (IllegalArgumentException e) {
			// Log.e(TAG, "IllegalArgumentException (DataBase failed)");
			Toast.makeText(MainMenuActivity.this, "Retrieval from DB failed with an illegal argument exception "+e, Toast.LENGTH_LONG).show();
		} catch (Exception e) {
			// Log.e(TAG, "Exception (DataBase failed)");
			Toast.makeText(MainMenuActivity.this, "There was an error with the cursor "+e, Toast.LENGTH_LONG).show();
		}

		//end root node
		//node = node+ "]\n} ";
    	return node;
    }

    
	protected class StartActivityAfterAnimation implements Animation.AnimationListener {
        private Intent mIntent;
        
        StartActivityAfterAnimation(Intent intent) {
            mIntent = intent;
        }
            

        public void onAnimationEnd(Animation animation) {
        	
            startActivity(mIntent);      
            
            if (UIConstants.mOverridePendingTransition != null) {
		       try {
		    	   UIConstants.mOverridePendingTransition.invoke(MainMenuActivity.this, R.anim.activity_fade_in, R.anim.activity_fade_out);
		       } catch (InvocationTargetException ite) {
		           DebugLog.d("Activity Transition", "Invocation Target Exception");
		       } catch (IllegalAccessException ie) {
		    	   DebugLog.d("Activity Transition", "Illegal Access Exception");
		       }
            }
        }

        public void onAnimationRepeat(Animation animation) {
            // TODO Auto-generated method stub
            
        }

        public void onAnimationStart(Animation animation) {
            // TODO Auto-generated method stub
            
        }
        
    }
    
}
