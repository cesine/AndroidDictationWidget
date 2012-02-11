package ca.ilanguage.aublog.util;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.text.style.ImageSpan; //import android.util.Log;
import ca.ilanguage.aublog.R;

@Deprecated
public class HTMLEmbedSpan extends ImageSpan {

	// private static final String TAG = "HTMLEmbedSpan";
	private String myHtml = null;
	private Context parentRef = null;

	public HTMLEmbedSpan(String html, Context caller) {
		super(caller, R.drawable.html_icon);
		this.myHtml = html;
		this.parentRef = caller;
	}

	@Override
	public Drawable getDrawable() {
		Context context = parentRef;
		Resources resources = null;
		if (context != null) {
			resources = context.getResources();
		} else {
			// Log.e(TAG,
			// "Context is null, we're not able to access resources!");
			return null;
		}
		Drawable d = resources.getDrawable(R.drawable.html_icon);
		// Log.d(TAG, "Intrinsic height: " + d.getIntrinsicHeight());
		// Log.d(TAG, "Intrinsic width: " + d.getIntrinsicWidth());
		// Log.d(TAG, "Minimum height:" + d.getMinimumHeight());
		// Log.d(TAG, "Minumum width:" + d.getMinimumWidth());
		d.setVisible(true, true);
		d.setAlpha(255);
		d.setBounds(new Rect(0, 0, d.getIntrinsicWidth(), d
				.getIntrinsicHeight()));
		return d;
	}

	public String getHtml() {
		return myHtml;
	}

	public void setHtml(String myHtml) {
		this.myHtml = myHtml;
	}

}