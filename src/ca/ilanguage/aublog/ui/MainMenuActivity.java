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

//TODO add a preferences activity, get rid of the old account logic and replace it with a proper database provider

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.Toast;
import ca.ilanguage.aublog.R;
import ca.ilanguage.aublog.db.AuBlogHistoryDatabase;
import ca.ilanguage.aublog.db.AuBlogHistoryDatabase.AuBlogHistory;
//import ca.ilanguage.aublog.util.DebugLog;
import ca.ilanguage.aublog.preferences.PreferenceConstants;
import ca.ilanguage.aublog.preferences.SetPreferencesActivity;
import ca.ilanguage.aublog.util.UIConstants;

import com.google.gdata.data.Feed;

public class MainMenuActivity extends Activity {

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
	private String mBloggerAccount;
	private String mBloggerPassword;
	private Runnable generateDraftsTree;
	private ProgressDialog m_ProgressDialog = null; 
	
	private final String MSG_KEY = "value";
	public static Feed resultFeed = null;

	int viewStatus = 0;

	private final static int WHATS_NEW_DIALOG = 0;
	private final static int GENERATING_TREE_DIALOG = 1;
	
	protected static final String TAG = "MainMenuActivity";

	// Create an anonymous implementation of OnClickListener

	private View.OnClickListener sOptionButtonListener = new View.OnClickListener() {
		public void onClick(View v) {

			Intent i = new Intent(getBaseContext(),
					SetPreferencesActivity.class);

			v.startAnimation(mButtonFlickerAnimation);
			mFadeOutAnimation
					.setAnimationListener(new StartActivityAfterAnimation(i));
			mBackground.startAnimation(mFadeOutAnimation);
			mStartButton.startAnimation(mAlternateFadeOutAnimation);
			mExtrasButton.startAnimation(mAlternateFadeOutAnimation);
			mDraftsButton.startAnimation(mAlternateFadeOutAnimation);
			mTicker.startAnimation(mAlternateFadeOutAnimation);

		}
	};

	private View.OnClickListener sExtrasButtonListener = new View.OnClickListener() {
		public void onClick(View v) {

			// Intent i = new Intent(getBaseContext(), Settings.class);
			Intent i = new Intent(getBaseContext(), AboutActivity.class);

			v.startAnimation(mButtonFlickerAnimation);
			mButtonFlickerAnimation
					.setAnimationListener(new StartActivityAfterAnimation(i));

		}
	};
	/*
	 * http://stackoverflow.com/questions/1979524/android-splashscreen
	 */
	public class GenerateTreeTask extends AsyncTask<Void, Void, Boolean>{

		
		@Override
		protected Boolean doInBackground(Void... params) {
			generateDraftTree();
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return true;
		}
		protected void onPreExecute(){
			showDialog(GENERATING_TREE_DIALOG);
			
		}
		protected void onPostExecute(Boolean result){
			/*
			 * Just before control is returned to the UI thread (?) launch an intent to open the 
			 * drafts tree activity
			 */
			Intent i = new Intent(getBaseContext(), ViewDraftTreeActivity.class);
			startActivity(i);
			dismissDialog(GENERATING_TREE_DIALOG);
		}
		
	}
	private View.OnClickListener sDraftsButtonListener = new View.OnClickListener() {
		public void onClick(View v) {

			/*
			 * If the drafts tree is fresh (no new changes) return.
			 */
			SharedPreferences prefs = getSharedPreferences(PreferenceConstants.PREFERENCE_NAME, MODE_PRIVATE);
			if  (true == prefs.getBoolean(PreferenceConstants.PREFERENCE_DRAFT_TREE_IS_FRESH,false) ){
				Toast.makeText(MainMenuActivity.this,
						"Not re-creating drafts tree, using cached. ",
						Toast.LENGTH_LONG).show();
				Intent i = new Intent(getBaseContext(), ViewDraftTreeActivity.class);

				v.startAnimation(mButtonFlickerAnimation);
				mButtonFlickerAnimation
						.setAnimationListener(new StartActivityAfterAnimation(i));
				return ;// "no tree created, it is already fresh";
			}

			/*
			 * Else if the drafts tree is not fresh, create a new Async task to generate the drafts tree
			 */
//			generateDraftsTree = new Runnable(){
//				@Override
//				public void run() {
//					generateDraftTree();
//				}
//			};
//			Thread thread =  new Thread(null, generateDraftsTree, "MagentoBackground");
//			thread.start();
			new GenerateTreeTask().execute();
			
			
			
			/*
			 * Mean while set the flag that the draft tree is fresh
			 */
			SharedPreferences.Editor editor = prefs.edit();
	    	editor.putBoolean(PreferenceConstants.PREFERENCE_DRAFT_TREE_IS_FRESH,true);
	    	editor.commit();
			
			

		}
	};

	private View.OnClickListener sStartButtonListener = new View.OnClickListener() {
		public void onClick(View v) {

			// Intent i = new Intent(getBaseContext(),
			// DifficultyMenuActivity.class);
			// i.putExtra("newGame", true);

			/*
			 * A full working sample client, containing all the sample code
			 * shown in this document, is available in the Java client library
			 * distribution, under the directory
			 * gdata/java/sample/blogger/BloggerClient.java. I. Public feeds
			 * don't require any authentication, but they are read-only. If you
			 * want to modify blogs, then your client needs to authenticate
			 * before requesting private feeds. this document assume you have an
			 * authenticated GoogleService object.
			 */

			// Alert
			// .showAlert(MainMenuActivity.this,
			// "Profile is not created",
			// "Please, input 'login/password' in settings");

			Intent i = new Intent(getBaseContext(), EditBlogEntryActivity.class);

			Uri uri = getContentResolver().insert(AuBlogHistory.CONTENT_URI,
					null);
			// If we were unable to create a new blog entry, then just finish
			// this activity. A RESULT_CANCELED will be sent back to the
			// original activity if they requested a result.
			if (uri == null) {
				Log.e(TAG, "Failed to insert new blog entry into "
						+ getIntent().getData());
				Toast.makeText(
						MainMenuActivity.this,
						"Failed to insert new blog entry into "
								+ getIntent().getData() + " with this uri"
								+ AuBlogHistory.CONTENT_URI, Toast.LENGTH_LONG)
						.show();

			} else {
				i.setData(uri);
				v.startAnimation(mButtonFlickerAnimation);
				mButtonFlickerAnimation
						.setAnimationListener(new StartActivityAfterAnimation(i));
			}
		}
	};

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.mainmenu);

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

		mButtonFlickerAnimation = AnimationUtils.loadAnimation(this,
				R.anim.button_flicker);
		mFadeOutAnimation = AnimationUtils.loadAnimation(this, R.anim.fade_out);
		mAlternateFadeOutAnimation = AnimationUtils.loadAnimation(this,
				R.anim.fade_out);
		mFadeInAnimation = AnimationUtils.loadAnimation(this, R.anim.fade_in);

		mTicker = findViewById(R.id.ticker);
		if (mTicker != null) {
			mTicker.setFocusable(true);
			mTicker.requestFocus();
			mTicker.setSelected(true);
		}

		mJustCreated = true;

	}

	@Override
	protected void onPause() {
		super.onPause();

	}

	@Override
	protected void onResume() {
		super.onResume();

		mButtonFlickerAnimation.setAnimationListener(null);

		if (mStartButton != null) {

			// Change "start" to "continue" if there's a saved game.
			SharedPreferences prefs = getSharedPreferences(
					PreferenceConstants.PREFERENCE_NAME, MODE_PRIVATE);
			

			((ImageView) mStartButton).setImageDrawable(getResources()
					.getDrawable(R.drawable.ui_button_start));
			mStartButton.setOnClickListener(sStartButtonListener);

			final int lastVersion = prefs.getInt(
					PreferenceConstants.PREFERENCE_LAST_VERSION, 0);

			if (Math.abs(lastVersion) < Math.abs(AuBlog.VERSION)) {
				// This is a new install or an upgrade.

				// Check the safe mode option.
				// Useful reference:
				// http://en.wikipedia.org/wiki/List_of_Android_devices
				if (Build.PRODUCT.contains("morrison") || // Motorola Cliq/Dext
						Build.MODEL.contains("Pulse") || // Huawei Pulse
						Build.MODEL.contains("U8220") || // Huawei Pulse
						Build.MODEL.contains("U8230") || // Huawei U8230
						Build.MODEL.contains("MB300") || // Motorola Backflip
						Build.MODEL.contains("MB501") || // Motorola Quench /
															// Cliq XT
						Build.MODEL.contains("Behold+II")) { // Samsung Behold
																// II
					// These are all models that users have complained about.
					// They likely use
					// the same buggy QTC graphics driver. Turn on Safe Mode by
					// default
					// for these devices.
					SharedPreferences.Editor editor = prefs.edit();
					editor.putBoolean(PreferenceConstants.PREFERENCE_SAFE_MODE,
							true);
					editor.commit();
				}

				SharedPreferences.Editor editor = prefs.edit();

				if (lastVersion > 0 && lastVersion < 14) {
					// if the user has beat the game once, go ahead and unlock
					// stuff for them.
					
				}

				// show what's new message
				editor.putInt(PreferenceConstants.PREFERENCE_LAST_VERSION,
						AuBlog.VERSION);
				editor.commit();

				showDialog(WHATS_NEW_DIALOG);

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
				mDraftsButton.startAnimation(AnimationUtils.loadAnimation(this,
						R.anim.button_slide));
			}
			if (mStartButton != null) {
				Animation anim = AnimationUtils.loadAnimation(this,
						R.anim.button_slide);
				anim.setStartOffset(500L);
				mStartButton.startAnimation(anim);
			}
			if (mExtrasButton != null) {
				Animation anim = AnimationUtils.loadAnimation(this,
						R.anim.button_slide);
				anim.setStartOffset(500L);
				mExtrasButton.startAnimation(anim);
			}

			if (mOptionsButton != null) {
				Animation anim = AnimationUtils.loadAnimation(this,
						R.anim.button_slide);
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
					.setMessage(R.string.whats_new_dialog_message).create();
		} 
		else if (id == GENERATING_TREE_DIALOG) {
			dialog = new ProgressDialog.Builder(this)
            		.setCancelable(true)
					.setTitle("Please wait")
					.setMessage("Generating the drafts tree, this may take a moment.").create();
		} else {
			dialog = super.onCreateDialog(id);
		}
		return dialog;
	}


	public String generateDraftTree() {

		
		
		/*
		 * TODO: use the appl cache for the drafts tree
		 * http://developer.android.com/guide/topics/data/data-storage.html
		 */
		// BufferedWriter mOut;

		
		
		
		
		String mResultsFile = "draft_space_tree.js";
		// FileWriter fstream;
		/*
		 * If you're using API Level 8 or greater, use getExternalFilesDir() to
		 * open a File that represents the external storage directory where you
		 * should save your files. This method takes a type parameter that
		 * specifies the type of subdirectory you want, such as DIRECTORY_MUSIC
		 * and DIRECTORY_RINGTONES (pass null to receive the root of your
		 * application's file directory). This method will create the
		 * appropriate directory if necessary. String path =
		 * Environment.getExternalStorageDirectory().getAbsolutePath() +
		 * "/Android/data/ca.ilanguage.aublog/files/";
		 */
		// String path =
		// Environment.getExternalStorageDirectory().getAbsolutePath() +
		// "/Android/data/ca.ilanguage.aublog/files/";

		String fname = mResultsFile;
//		File file = new File(getCacheDir(), mResultsFile);
		File file = new File(getExternalFilesDir(null), mResultsFile);
		File jsonOnlyFile =  new File(getExternalFilesDir(null), "json_only_"+mResultsFile);

		try {
			// // Make sure the Pictures directory exists.
			// boolean exists = (new File(path)).exists();
			// if (!exists){ new File(path).mkdirs(); }
			// Open output stream
			FileOutputStream fOut = new FileOutputStream(file);
			FileOutputStream exportJSonOnly = new FileOutputStream(jsonOnlyFile);
			
			// fstream = new FileWriter(mResultsFile,true);
			// mOut = new BufferedWriter(fstream);
			String begining = "var labelType, useGradients, nativeTextSupport, animate;\n\n(function() {\n  var ua = navigator.userAgent,\n      iStuff = ua.match(/iPhone/i) || ua.match(/iPad/i),\n      typeOfCanvas = typeof HTMLCanvasElement,\n      nativeCanvasSupport = (typeOfCanvas == 'object' || typeOfCanvas == 'function'),\n      textSupport = nativeCanvasSupport \n        && (typeof document.createElement('canvas').getContext('2d').fillText == 'function');\n  //I'm setting this based on the fact that ExCanvas provides text support for IE\n  //and that as of today iPhone/iPad current text support is lame\n  labelType = (!nativeCanvasSupport || (textSupport && !iStuff))? 'Native' : 'HTML';\n  nativeTextSupport = labelType == 'Native';\n  useGradients = nativeCanvasSupport;\n  animate = !(iStuff || !nativeCanvasSupport);\n})();\n\nvar Log = {\n  elem: false,\n  write: function(text){\n    if (!this.elem) \n      this.elem = document.getElementById('log');\n    this.elem.innerHTML = text;\n    this.elem.style.left = (200 - this.elem.offsetWidth / 2) + 'px';\n  }\n};\n\n\n\nfunction init(){\n    //init data\n";

			String end = "\n    //end\n    //init Spacetree\n    //Create a new ST instance\n    var st = new $jit.ST({\n    	orientation: "
					+ '"'
					+ "top"
					+ '"'
					+ ",\n    	indent:10,\n        //id of viz container element\n        injectInto: 'infovis',\n        //set duration for the animation\n        duration: 800,\n        //set animation transition type\n        transition: $jit.Trans.Quart.easeInOut,\n        //set distance between node and its children\n        levelDistance: 50,\n        //enable panning\n        Navigation: {\n          enable:true,\n          panning:true\n        },\n        //set node and edge styles\n        //set overridable=true for styling individual\n        //nodes or edges\n        Node: {\n            height: 30,\n            width: 40,\n            type: 'ellipse',\n            color: '#aaa',\n            overridable: true\n        },\n        \n        Edge: {\n            type: 'bezier',\n            overridable: true\n        },\n        \n        onBeforeCompute: function(node){\n            Log.write("
					+ '"'
					+ "loading "
					+ '"'
					+ " + node.name);\n        },\n        \n        onAfterCompute: function(node){\n            Log.write("
					+ '"'
					+ "<input type='button' value='Edit "
					+ '"'
					+ "+node.name+"
					+ '"'
					+ "' onClick='editId("
					+ '"'
					+ "+node.id+"
					+ '"'
					+ ")'/><br /><input type='button' value='Delete "
					+ '"'
					+ "+node.name+"
					+ '"'
					+ "' onClick='deleteId("
					+ '"'
					+ "+node.id+"
					+ '"'
					+ ")'/>"
					+ '"'
					+ ");\n        },\n        \n        //This method is called on DOM label creation.\n        //Use this method to add event handlers and styles to\n        //your node.\n        onCreateLabel: function(label, node){\n            label.id = node.id;            \n            label.innerHTML = node.name;\n            label.onclick = function(){\n            	//if(normal.checked) {\n            	  st.onClick(node.id);\n            	//} else {\n                //st.setRoot(node.id, 'animate');\n            	//}\n            };\n            //set label styles\n            var style = label.style;\n            style.width = 40 + 'px';\n            style.height = 17 + 'px';            \n            style.cursor = 'pointer';\n            style.color = '#333';\n            style.fontSize = '0.8em';\n            style.textAlign= 'center';\n            style.paddingTop = '8px';\n        },\n        \n        //This method is called right before plotting\n        //a node. It's useful for changing an individual node\n        //style properties before plotting it.\n        //The data properties prefixed with a dollar\n        //sign will override the global node style properties.\n        onBeforePlotNode: function(node){\n            //add some color to the nodes in the path between the\n            //root node and the selected node.\n            if (node.selected) {\n                node.data.$color = "
					+ '"'
					+ "#ff7"
					+ '"'
					+ ";\n            }\n            else {\n                delete node.data.$color;\n                //if the node belongs to the last plotted level\n                if(!node.anySubnode("
					+ '"'
					+ "exist"
					+ '"'
					+ ")) {\n                    //count children number\n                    var count = 0;\n                    node.eachSubnode(function(n) { count++; });\n                    //assign a node color based on\n                    //how many children it has\n                    node.data.$color = ['#aff', '#aee', '#add', '#acc', '#abb', '#acb'][count];                    \n                }\n            }\n        },\n        \n        //This method is called right before plotting\n        //an edge. It's useful for changing an individual edge\n        //style properties before plotting it.\n        //Edge data proprties prefixed with a dollar sign will\n        //override the Edge global style properties.\n        onBeforePlotLine: function(adj){\n            if (adj.nodeFrom.selected && adj.nodeTo.selected) {\n                adj.data.$color = "
					+ '"'
					+ "#eed"
					+ '"'
					+ ";\n                adj.data.$lineWidth = 3;\n            }\n            else {\n                delete adj.data.$color;\n                delete adj.data.$lineWidth;\n            }\n        }\n    });\n    //load json data\n    st.loadJSON(json);\n\n    //compute node positions and layout\n    st.compute();\n    //optional: make a translation of the tree\n    st.geom.translate(new $jit.Complex(-200, 0), "
					+ '"'
					+ "current"
					+ '"'
					+ ");\n    //emulate a click on the root node.\n    st.onClick(st.root);\n    //end\n    \n    //Add event handlers to switch spacetree orientation.\n    var top = $jit.id('r-top'), \n        left = $jit.id('r-left'), \n        bottom = $jit.id('r-bottom'), \n        right = $jit.id('r-right'),\n        normal = $jit.id('s-normal');\n        \n    \n    function changeHandler() {\n        if(this.checked) {\n             bottom.disabled = right.disabled = left.disabled = top.disabled = true;\n            st.switchPosition(this.value, "
					+ '"'
					+ "animate"
					+ '"'
					+ ", {\n                onComplete: function(){\n                    bottom.disabled = right.disabled = left.disabled = top.disabled = false;\n                }\n            });\n        }\n    };\n    \n    top.onchange = left.onchange = bottom.onchange = right.onchange = changeHandler;\n    //end\n\n}\n";
			fOut.write((begining).getBytes());
			String id = AuBlogHistoryDatabase.ROOT_ID_DEFAULT;
			String data = "json = ";
			data = data + "{id: \"" + id + "\",\nname: \"" + "Root"
					+ "\",\nhidden: \"" + "0" 
					+ "\",\ndata: {"
					+ "},\nchildren: [";
			fOut.write((data).getBytes());
			exportJSonOnly.write((data).getBytes());
			fOut.write((getSubtree(id)).getBytes());
			exportJSonOnly.write((getSubtree(id)).getBytes());
			fOut.write(("]\n};").getBytes());
			exportJSonOnly.write(("]\n};").getBytes());
			fOut.write((end).getBytes());
			fOut.flush();
			fOut.close();
			exportJSonOnly.flush();
			exportJSonOnly.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			Toast.makeText(
					MainMenuActivity.this,
					"The SDCARD isn't writeable. Is the device being used as a disk drive on a comptuer?\n "
							+ e.toString(), Toast.LENGTH_LONG).show();

		}

		
		
//    	try {
//			Thread.sleep(1000);
//		} catch (InterruptedException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
    	//from retrieving experiments which was original from where?
//    	runOnUiThread(returnRes);
		return "drafts tree file created";
	}
//	private Runnable returnRes = new Runnable() {
//
//		@Override
//		public void run() {
//			
//			m_ProgressDialog.dismiss();
//			
//		}
//	};

	public String getSubtree(String id) {
		String[] PROJECTION = new String[] { AuBlogHistory._ID, // 0
				AuBlogHistory.ENTRY_TITLE, AuBlogHistory.ENTRY_CONTENT, // 2
				AuBlogHistory.ENTRY_LABELS, AuBlogHistory.PUBLISHED, // 4
				AuBlogHistory.DELETED, AuBlogHistory.PARENT_ENTRY // 6
		};
		String node = "";
		Boolean firstChild = true;
		try {

			/*
			 * find all nodes with this node as its parent
			 */
			Cursor cursor = managedQuery(AuBlogHistory.CONTENT_URI, PROJECTION,
					AuBlogHistory.PARENT_ENTRY + "=" + id, null, null);
			// Toast.makeText(MainMenuActivity.this,
			// "There are \n"+cursor.getCount()+" daughters",
			// Toast.LENGTH_LONG).show();
			if ((cursor != null)) {
				// Requery in case something changed while paused (such as the
				// title)
				cursor.requery();
				// Make sure we are at the one and only row in the cursor.
				cursor.moveToFirst();
				/*
				 * if this node is flagged as deleted, abort the subtree and the
				 * node
				 */
				String nodeAsString = "id:" + cursor.getString(0) + ":\ntitle:"
						+ cursor.getString(1) + ":\ncontent:"
						+ cursor.getString(2) + ":\nlabels:"
						+ cursor.getString(3) + ":\npublished:"
						+ cursor.getString(4) + ":\ndeleted:"
						+ cursor.getString(5) + ":\nparent:"
						+ cursor.getString(6) + ":";
				// Toast.makeText(MainMenuActivity.this,
				// "Full post info:"+nodeAsString, Toast.LENGTH_LONG).show();

				if ("1".equals(cursor.getString(5))) {
					// Toast.makeText(MainMenuActivity.this,
					// "A deleted/hidden post:"+nodeAsString,
					// Toast.LENGTH_LONG).show();
					// cursor.moveToLast();
					// cursor.moveToNext();
				} else {
					// Toast.makeText(MainMenuActivity.this,
					// "Post:"+nodeAsString, Toast.LENGTH_LONG).show();

				}
				/*
				 * for each daughter, print the daughter and her subtree
				 */
				while (cursor.isAfterLast() == false) {
					if (!firstChild) {
						node = node + ",";
					}
					String Id = cursor.getString(0);
					node = node + "{\nid: \"" + Id + "\",\nname: \"";
					if ("1".equals(cursor.getString(5))) {
						node = node + "*";
					} // if the node is deleted write a star
					node = node + cursor.getString(1) + "\",\nhidden: \""
							+ cursor.getString(5) + "\",\ndata: {"
							+ "},\nchildren: [";

					/*
					 * find all nodes with this node as its parent
					 */
					node = node + getSubtree(Id);

					node = node + "]\n} ";
					firstChild = false;
					cursor.moveToNext();
				}
				// firstChild=true;
				// cursor.deactivate();
				String temp = "";

			}
		} catch (IllegalArgumentException e) {
			// Log.e(TAG, "IllegalArgumentException (DataBase failed)");
			Toast.makeText(
					MainMenuActivity.this,
					"Retrieval from DB failed with an illegal argument exception "
							+ e, Toast.LENGTH_LONG).show();
		} catch (Exception e) {
			// Log.e(TAG, "Exception (DataBase failed)");
			// Toast.makeText(MainMenuActivity.this,
			// "There was an error with the cursor "+e,
			// Toast.LENGTH_LONG).show();
		}

		// end root node
		// node = node+ "]\n} ";
		return node;
	}

	protected class StartActivityAfterAnimation implements
			Animation.AnimationListener {
		private Intent mIntent;

		StartActivityAfterAnimation(Intent intent) {
			mIntent = intent;
		}

		public void onAnimationEnd(Animation animation) {

			startActivity(mIntent);

			if (UIConstants.mOverridePendingTransition != null) {
				try {
					UIConstants.mOverridePendingTransition.invoke(
							MainMenuActivity.this, R.anim.activity_fade_in,
							R.anim.activity_fade_out);
				} catch (InvocationTargetException ite) {
//					DebugLog.d("Activity Transition",
//							"Invocation Target Exception");
				} catch (IllegalAccessException ie) {
//					DebugLog.d("Activity Transition",
//							"Illegal Access Exception");
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
