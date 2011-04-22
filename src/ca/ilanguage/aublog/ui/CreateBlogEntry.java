package ca.ilanguage.aublog.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.database.SQLException;
import android.graphics.Color;
import android.os.Bundle; //import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

//import com.google.android.apps.analytics.GoogleAnalyticsTracker;
import ca.ilanguage.aublog.db.DBTextAdapter;
import ca.ilanguage.aublog.util.Alert;
import ca.ilanguage.aublog.R;

public class CreateBlogEntry extends Activity {
	// private static final String TAG = "CreateBlogEntry";
	private DBTextAdapter mDbTextHelper;
	private static Cursor post = null;
	EditText postTitle = null;
	EditText postContent = null;
	//GoogleAnalyticsTracker tracker;
	private static final int GROUP_BASIC = 0;
	private static final int GROUP_FORMAT = 1;
	int selectionStart;
	int selectionEnd;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.createblogentry);
//		tracker = GoogleAnalyticsTracker.getInstance();
//		tracker.start("UA-11702470-1", this);
		
		postTitle = (EditText) this.findViewById(R.id.TextPostTitle);
		postContent = (EditText) this.findViewById(R.id.TextPostContent);
		
		mDbTextHelper = new DBTextAdapter(this);
		try {
			mDbTextHelper.open();
		} catch (SQLException e) {
			// Log.e(TAG, "Database has not opened");
			Toast.makeText(CreateBlogEntry.this, "Database connection problem "+e, Toast.LENGTH_LONG).show();
		}
		post = mDbTextHelper.fetchPostdById(1);
		startManagingCursor(post);
		if (post.getCount() != 0) {
			try {
				postTitle.setText(post.getString(post
						.getColumnIndexOrThrow(DBTextAdapter.KEY_TITLE)));
				postContent.setText(post.getString(post
						.getColumnIndexOrThrow(DBTextAdapter.KEY_CONTENT)));
			} catch (IllegalArgumentException e) {
				// Log.e(TAG, "IllegalArgumentException (DataBase failed)");
			} catch (Exception e) {
				// Log.e(TAG, "Exception (DataBase failed)");
			}
		}
		
		Intent i = getIntent();
		if (i != null)
		{
			Bundle b = i.getExtras();
			if (b != null)
			{
				boolean isTextColor = b.getBoolean("isTextColor");
			    selectionStart = b.getInt("selStart");
				selectionEnd = b.getInt("selEnd");
				String startTag = "<span style='";
				startTag += (isTextColor) ? "color: " : "background: ";
				int color = b.getInt("color");
				startTag += "rgb(" + Color.red(color) + ", " + Color.green(color) + ", " + Color.blue(color) + ");'>";
				String endTag = "</span>";
				if (selectionStart >= 0 && selectionEnd >= 0) {
					String currentText = postContent.getText().toString();
					String selectedText = currentText.substring(selectionStart,
							selectionEnd);
					currentText = currentText.substring(0, selectionStart)
							+ startTag
							+ selectedText
							+ endTag
							+ currentText.substring(selectionEnd, currentText
									.length());
					postContent.setText(currentText);
					int selPosition = selectionStart + startTag.length()
							+ selectedText.length();
					postContent.requestFocus();
					postContent.setSelection(selPosition);
				}
			}
		}
		
		if (this.getWindow().getWindowManager().getDefaultDisplay()
				.getOrientation() == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT) {
			postContent.setHeight(105);
		} else if (this.getWindow().getWindowManager().getDefaultDisplay()
				.getOrientation() == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE) {
			postContent.setHeight(265);
		}
		this.findViewById(R.id.BackToMainActivities).setOnClickListener(
				new OnClickListener() {
					public void onClick(View v) {
						String strTitle = "";
						String strContent = "";
						if (post.getCount() == 0) {
							try {
								strTitle = CreateBlogEntry.this.postTitle
										.getText().toString();
								strContent = CreateBlogEntry.this.postContent
										.getText().toString();
								mDbTextHelper.createPost(strTitle, strContent);
								mDbTextHelper.close();
								post.close();
								// Log.d(TAG, "Post saved to database.");
							} catch (SQLException e) {
								// Log.e(TAG,"SQLException (createPost(title, content))");
							} catch (Exception e) {
								// Log.e(TAG, "Exception: " + e.getMessage());
							}
						} else {
							try {
								strTitle = CreateBlogEntry.this.postTitle
										.getText().toString();
								strContent = CreateBlogEntry.this.postContent
										.getText().toString();
								mDbTextHelper.updatePostById((long) 1,
										strTitle, strContent);
								mDbTextHelper.close();
								post.close();
								// Log.d(TAG, "Post updated in database.");
							} catch (SQLException e) {
								// Log.e(TAG,"SQLException (updatePostById(rowId, title, content))");
							}
						}
						Intent i = new Intent(CreateBlogEntry.this,
								MainMenuActivity.class);
						startActivity(i);
						finish();
					}
				});

		int w = this.getWindow().getWindowManager().getDefaultDisplay()
				.getWidth() - 8;
		((Button) this.findViewById(R.id.BackToMainActivities)).setWidth(w / 2);
		((Button) this.findViewById(R.id.Preview)).setWidth(w / 2);

		this.findViewById(R.id.Preview).setOnClickListener(
				new OnClickListener() {
					public void onClick(View v) {
						if ((postTitle.getText().length() == 0)
								|| (postTitle.getText() == null)
								|| (postContent.getText().length() == 0)
								|| (postContent.getText() == null)) {
							Alert.showAlert(CreateBlogEntry.this,
									"Empty title or content",
									"Please fill all fields");
						} else {
							Intent i = new Intent(CreateBlogEntry.this,
									PreviewAndPublish.class);
							String strTitle = "";
							String strContent = "";
							if (post.getCount() == 0) {
								try {
									strTitle = postTitle.getText().toString();
									strContent = postContent.getText()
											.toString();
									mDbTextHelper.createPost(strTitle,
											strContent);
									// Log.d(TAG, "Post saved to database.");
								} catch (SQLException e) {
									// Log.e(TAG,"SQLException (createPost(title, content))");
								} catch (Exception e) {
									// Log.e(TAG, "Exception: " +
									// e.getMessage());
								}
							} else {
								try {
									strTitle = postTitle.getText().toString();
									strContent = postContent.getText()
											.toString();
									mDbTextHelper.updatePostById((long) 1,
											strTitle, strContent);
									// Log.d(TAG, "Post updated in database.");
								} catch (SQLException e) {
									// Log.e(TAG,"SQLException (updatePostById(rowId, title, content))");
								}
							}
							mDbTextHelper.close();
							post.close();
							startActivity(i);
							finish();
						}
					}
				});
	}

	@Override
	protected void onPause() {
		super.onPause();
		// Log.i(TAG, "Method 'onPause()' launched");
		mDbTextHelper = new DBTextAdapter(this);
		try {
			mDbTextHelper.open();
		} catch (SQLException e) {
			// Log.e(TAG, "Database has not opened");
		}
		post = mDbTextHelper.fetchPostdById(1);
		startManagingCursor(post);
		EditText postTitle = (EditText) findViewById(R.id.TextPostTitle);
		EditText postContent = (EditText) findViewById(R.id.TextPostContent);
		String strTitle = "";
		String strContent = "";
		if (post.getCount() == 0) {
			try {
				strTitle = postTitle.getText().toString();
				strContent = postContent.getText().toString();
				mDbTextHelper.createPost(strTitle, strContent);
				mDbTextHelper.close();
				post.close();
				// Log.d(TAG, "Post saved to database.");
			} catch (SQLException e) {
				// Log.e(TAG, "SQLException (createPost(title, content))");
			} catch (Exception e) {
				// Log.e(TAG, "Exception: " + e.getMessage());
			}
		} else {
			try {
				strTitle = postTitle.getText().toString();
				strContent = postContent.getText().toString();
				mDbTextHelper.updatePostById((long) 1, strTitle, strContent);
				mDbTextHelper.close();
				post.close();
				// Log.d(TAG, "Post updated in database.");
			} catch (SQLException e) {
				// Log.e(TAG,"SQLException (updatePostById(rowId, title, content))");
			}
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		// Log.i(TAG, "Method 'onDestroy()' launched");
//		tracker.stop();
		mDbTextHelper = new DBTextAdapter(this);
		try {
			mDbTextHelper.open();
		} catch (SQLException e) {
			// Log.e(TAG, "Database has not opened");
		}
		post = mDbTextHelper.fetchPostdById(1);
		startManagingCursor(post);
		EditText postTitle = (EditText) findViewById(R.id.TextPostTitle);
		EditText postContent = (EditText) findViewById(R.id.TextPostContent);
		String strTitle = "";
		String strContent = "";
		if (post.getCount() == 0) {
			try {
				strTitle = postTitle.getText().toString();
				strContent = postContent.getText().toString();
				mDbTextHelper.createPost(strTitle, strContent);
				mDbTextHelper.close();
				post.close();
				// Log.d(TAG, "Post saved to database.");
			} catch (SQLException e) {
				// Log.e(TAG, "SQLException (createPost(title, content))");
			} catch (Exception e) {
				// Log.e(TAG, "Exception: " + e.getMessage());
			}
		} else {
			try {
				strTitle = postTitle.getText().toString();
				strContent = postContent.getText().toString();
				mDbTextHelper.updatePostById((long) 1, strTitle, strContent);
				mDbTextHelper.close();
				post.close();
				// Log.d(TAG, "Post updated in database.");
			} catch (SQLException e) {
				// Log.e(TAG,"SQLException (updatePostById(rowId, title, content))");
			}
		}
	}

	@Override
	protected void onStop() {
		super.onStop();
		// Log.i(TAG, "Method 'onStop()' launched");
		mDbTextHelper = new DBTextAdapter(this);
		try {
			mDbTextHelper.open();
		} catch (SQLException e) {
			// Log.e(TAG, "Database has not opened");
		}
		post = mDbTextHelper.fetchPostdById(1);
		startManagingCursor(post);
		EditText postTitle = (EditText) findViewById(R.id.TextPostTitle);
		EditText postContent = (EditText) findViewById(R.id.TextPostContent);
		String strTitle = "";
		String strContent = "";
		if (post.getCount() == 0) {
			try {
				strTitle = postTitle.getText().toString();
				strContent = postContent.getText().toString();
				mDbTextHelper.createPost(strTitle, strContent);
				mDbTextHelper.close();
				post.close();
				// Log.d(TAG, "Post saved to database.");
			} catch (SQLException e) {
				// Log.e(TAG, "SQLException (createPost(title, content))");
			} catch (Exception e) {
				// Log.e(TAG, "Exception: " + e.getMessage());
			}
		} else {
			try {
				strTitle = postTitle.getText().toString();
				strContent = postContent.getText().toString();
				mDbTextHelper.updatePostById((long) 1, strTitle, strContent);
				mDbTextHelper.close();
				post.close();
				// Log.d(TAG, "Post updated in database.");
			} catch (SQLException e) {
				// Log.e(TAG,"SQLException (updatePostById(rowId, title, content))");
			}
		}
	}
		
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		boolean result = super.onCreateOptionsMenu(menu);
		//Log.d(TAG, "Create menu: " + postContent.getSelectionStart() + ", " + postContent.getSelectionEnd());
		SubMenu sub = menu.addSubMenu(GROUP_FORMAT, 1, Menu.NONE, "Format");
		sub.add(GROUP_FORMAT, 2, Menu.NONE, "Bold");
		sub.add(GROUP_FORMAT, 3, Menu.NONE, "Italic");
		sub.add(GROUP_FORMAT, 4, Menu.NONE, "Underline");
		sub.add(GROUP_FORMAT, 5, Menu.NONE, "Text color");
		sub.add(GROUP_FORMAT, 6, Menu.NONE, "Background color");
		menu.add(GROUP_BASIC, 7, Menu.NONE, "Clear all");
		menu.add(GROUP_BASIC, 8, Menu.NONE, "Insert image");
		return result;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		int group = item.getGroupId();
		if (group == GROUP_FORMAT) {
			String startTag = "", endTag = "";
			switch (id) {
			case 2: // Bold
				startTag = "<b>";
				endTag = "</b>";
				break;
			case 3: // Italic
				startTag = "<i>";
				endTag = "</i>";
				break;
			case 4: // Underline
				startTag = "<u>";
				endTag = "</u>";
				break;
			case 5: // Select text color
			case 6: // Select background color
				String strTitle = "";
				String strContent = "";
				if (post.getCount() == 0) {
					try {
						strTitle = postTitle.getText().toString();
						strContent = postContent.getText().toString();
						mDbTextHelper.createPost(strTitle, strContent);
						mDbTextHelper.close();
						post.close();
						// Log.d(TAG, "Post saved to database.");
					} catch (SQLException e) {
						// Log.e(TAG, "SQLException (createPost(title, content))");
					} catch (Exception e) {
						// Log.e(TAG, "Exception: " + e.getMessage());
					}
				} else {
					try {
						strTitle = postTitle.getText().toString();
						strContent = postContent.getText().toString();
						mDbTextHelper
								.updatePostById((long) 1, strTitle, strContent);
						mDbTextHelper.close();
						post.close();
						// Log.d(TAG, "Post updated in database.");
					} catch (SQLException e) {
						// Log.e(TAG,"SQLException (updatePostById(rowId, title, content))");
					}
				}
//				Intent i = new Intent(CreateBlogEntry.this, ColorPickerActivity.class);
//				if (id == 5)
//					i.putExtra("isTextColor", true);
//				else
//					i.putExtra("isTextColor", false);
//				i.putExtra("selStart", selectionStart);
//				i.putExtra("selEnd", selectionEnd);
//				startActivity(i);
				
				
				finish();
				break;
			}
			if (selectionStart >= 0 && selectionEnd >= 0) {
				String currentText = postContent.getText().toString();
				String selectedText = currentText.substring(selectionStart,
						selectionEnd);
				currentText = currentText.substring(0, selectionStart)
						+ startTag
						+ selectedText
						+ endTag
						+ currentText.substring(selectionEnd, currentText
								.length());
				postContent.setText(currentText);
				int selPosition = selectionStart + startTag.length()
						+ selectedText.length();
				postContent.requestFocus();
				postContent.setSelection(selPosition);
			}
		} else if (group == GROUP_BASIC) {
			switch (id) {
			case 7:
				postTitle.setText("");
				postContent.setText("");
				break;
			case 8:
				AlertDialog.Builder alert = new AlertDialog.Builder(
						CreateBlogEntry.this);
				alert.setTitle("Insert image");
//				alert.setIcon(R.drawable.about);
				alert.setMessage("Please input URL:");
				final EditText input = new EditText(CreateBlogEntry.this);
				alert.setView(input);
				alert.setPositiveButton("OK",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int whichButton) {
								String value = input.getText().toString();
								if (value.length() > 0 && selectionStart >= 0
										&& selectionEnd >= 0) {
									if (value.indexOf("http://") != 0)
										value = "http://" + value;
									String currentText = postContent.getText()
											.toString();
									String selectedText = currentText
											.substring(selectionStart,
													selectionEnd);
									if (selectionStart == selectionEnd)
										currentText = currentText.substring(0,
												selectionStart)
												+ "<img src='"
												+ value
												+ "' />"
												+ currentText.substring(
														selectionEnd,
														currentText.length());
									else
										currentText = currentText.substring(0,
												selectionStart)
												+ "<img src='"
												+ value
												+ "' alt='"
												+ selectedText
												+ "' />"
												+ currentText.substring(
														selectionEnd,
														currentText.length());
									postContent.setText(currentText);
									int selPosition = selectionStart
											+ value.length()
											+ ((selectionStart == selectionEnd) ? 14
													: (21 + selectedText
															.length()));
									postContent.requestFocus();
									postContent.setSelection(selPosition);
								}
							}
						});
				alert.setNegativeButton("Cancel",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int whichButton) {
							}
						});
				alert.show();
				break;
			}
		}
		return super.onOptionsItemSelected(item);
	}

	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			EditText postTitle = (EditText) findViewById(R.id.TextPostTitle);
			EditText postContent = (EditText) findViewById(R.id.TextPostContent);
			String strTitle = "";
			String strContent = "";
			if (post.getCount() == 0) {
				try {
					strTitle = postTitle.getText().toString();
					strContent = postContent.getText().toString();
					mDbTextHelper.createPost(strTitle, strContent);
					mDbTextHelper.close();
					post.close();
					// Log.d(TAG, "Post saved to database.");
				} catch (SQLException e) {
					// Log.e(TAG, "SQLException (createPost(title, content))");
				} catch (Exception e) {
					// Log.e(TAG, "Exception: " + e.getMessage());
				}
			} else {
				try {
					strTitle = postTitle.getText().toString();
					strContent = postContent.getText().toString();
					mDbTextHelper
							.updatePostById((long) 1, strTitle, strContent);
					mDbTextHelper.close();
					post.close();
					// Log.d(TAG, "Post updated in database.");
				} catch (SQLException e) {
					// Log.e(TAG,"SQLException (updatePostById(rowId, title, content))");
				}
			}
			Intent i = new Intent(CreateBlogEntry.this, MainMenuActivity.class);
			startActivity(i);
			finish();
		}
		if (keyCode == KeyEvent.KEYCODE_MENU) {
			int tmp1 = 0, tmp2 = 0;
			tmp1 = postContent.getSelectionStart();
			tmp2 = postContent.getSelectionEnd();
			selectionStart = Math.min(tmp1, tmp2);
			selectionEnd = Math.max(tmp1, tmp2);
		}
		return super.onKeyDown(keyCode, event);
	}
}
