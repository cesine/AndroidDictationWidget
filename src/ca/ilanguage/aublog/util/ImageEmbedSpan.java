package ca.ilanguage.aublog.util;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.text.style.ImageSpan; //import android.util.Log;
import ca.ilanguage.aublog.R;

public class ImageEmbedSpan extends ImageSpan {
	// private static final String TAG = "ImageEmbedSpan";
	private String mySrc = null;
	private Context parentRef = null;

	public ImageEmbedSpan(String src, Context caller) {
		super(caller, R.drawable.img_icon);
		this.mySrc = src;
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
		Drawable d = resources.getDrawable(R.drawable.img_icon);
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

	public String getSrc() {
		return mySrc;
	}

	public void setSrc(String mySrc) {
		this.mySrc = mySrc;
	}

}