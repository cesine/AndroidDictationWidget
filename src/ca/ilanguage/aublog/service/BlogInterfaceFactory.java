package ca.ilanguage.aublog.service;

//import android.util.Log;

import ca.ilanguage.aublog.service.BlogConfigBLOGGER.BlogInterfaceType;

public class BlogInterfaceFactory {

	// private static final String TAG = "BlogInterfaceFactory";

	static BlogInterface instance;

	public static BlogInterface getInstance(BlogInterfaceType type) {
		if (type == BlogConfigBLOGGER.BlogInterfaceType.BLOGGER) {
			if (instance == null || !(instance instanceof BloggerAPI)) {
				instance = new BloggerAPI();
			}
			return instance;
		} else {
			// Log.e(TAG,"Tried to instantiate an unsupported BlogInterface type!");
			return null;
		}
	}

}