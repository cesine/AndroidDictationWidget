package ca.ilanguage.aublog.ui;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.webkit.JsResult;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.FrameLayout;
import android.widget.Toast;

import ca.ilanguage.aublog.R;
/**
 * Demonstrates how to embed a WebView in your activity. Also demonstrates how
 * to have javascript in the WebView call into the activity, and how the activity 
 * can invoke javascript.
 * <p>
 * In this example, clicking on the android in the WebView will result in a call into
 * the activities code in {@link DemoJavaScriptInterface#clickOnAndroid()}. This code
 * will turn around and invoke javascript using the {@link WebView#loadUrl(String)}
 * method.
 * <p>
 * Obviously all of this could have been accomplished without calling into the activity
 * and then back into javascript, but this code is intended to show how to set up the 
 * code paths for this sort of communication.
 *
 */
public class CreateBlogEntryActivity extends Activity {

    private static final String LOG_TAG = "WebViewDemo";

    private WebView mWebView;

    private Handler mHandler = new Handler();

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setContentView(R.layout.main_webview);
        mWebView = (WebView) findViewById(R.id.webview);
        mWebView.addJavascriptInterface(new JavaScriptInterface(this), "Android");
        

//        MyHandlerClass myhandler = new MyHandlerClass (mWebView);

//        FrameLayout mContentView = (FrameLayout) getWindow().
//        getDecorView().findViewById(android.R.id.content);
//        final View zoom = this.mWebView.getZoomControls();
//        mContentView.addView(zoom);
//        zoom.setVisibility(View.VISIBLE);
        
        
        WebSettings webSettings = mWebView.getSettings();
        webSettings.setSavePassword(false);
        webSettings.setSaveFormData(false);
        webSettings.setJavaScriptEnabled(true);
//        webSettings.setSupportZoom(false);

//        mWebView.setWebChromeClient(new MyWebChromeClient());

//        mWebView.addJavascriptInterface(new DemoJavaScriptInterface(CreateBlogEntryActivity.this), "demo");
        
//http://www.saltycrane.com/blog/2010/03/jquery-flot-stacked-bar-chart-example/
        //mWebView.loadUrl("file:///android_asset/stacked_bar_ex.html");
//        mWebView.loadUrl("file:///android_asset/tinymce/examples/index.html");
//        mWebView.loadUrl("file:///android_asset/openwysiwyg/openwysiwyg_v1.4.7/example.html");
//        mWebView.loadUrl("file:///android_asset/elrte/src/elrte.src.html");
//        mWebView.loadUrl("file:///android_asset/markitup/index.html");
        //http://stackoverflow.com/questions/7477/autosizing-textarea
        mWebView.loadUrl("file:///android_asset/create_blog_entry.html");
        //mWebView.loadUrl("file:///android_asset/flot/html/basechart.html");
        //mWebView.loadUrl("http://thejit.org/static/v20/Jit/Examples/Hypertree/example1.html");//("file:///android_asset/demo.html");
        //mWebView.loadDataWithBaseURL("", "outputGraph.html", "html", "utf-8", "");
        /*
         * 
         * The Goal

Let's use a Javascript based graphing package that can render graph in a browser.
We want this to be done locally with just the Javascript and HTML and data we have on hand.
No connections to the internet
It should be an Activity in android that you can seamlessly jump to and from.
In other words, it should be embedded and look awesome.
Enter Flot

 http://code.google.com/p/flot/

After some tinkering around, I found this jQuery based graphing package called Flot. It's incredibly straightforward to use. Correction, it is pretty awesome. You define your data in Javascript via a JSON like object and pass it into the library. It'll automagically draw itself on screen within the boundaries you set autoscaling the axes to fit.

        public void loadDataWithBaseURL (String baseUrl, String data, String mimeType, String encoding, String historyUrl) method description:

        	Note for post 1.0. Due to the change in the WebKit, the access to asset files through "file:///android_asset/" for the sub resources is more restricted. If you provide null or empty string as baseUrl, you won't be able to access asset files. If the baseUrl is anything other than http(s)/ftp(s)/about/javascript as scheme, you can access asset files for sub resources.
	*/
    
    }

//    final class DemoJavaScriptInterface {
//    	Context mContext;
//
//        /** Instantiate the interface and set the context */
//        DemoJavaScriptInterface(Context c) {
//            mContext = c;
//        }
//
//        /**
//         * This is not called on the UI thread. Post a runnable to invoke
//         * loadUrl on the UI thread.
//         */
//        public void clickOnAndroid() {
//            mHandler.post(new Runnable() {
//                public void run() {
//                    mWebView.loadUrl("javascript:wave()");
//                }
//            });
//
//        }
//        public void showToast(String toast) {
//            Toast.makeText(mContext, toast, Toast.LENGTH_SHORT).show();
//        }
//    }

    public class JavaScriptInterface {
        Context mContext;

        /** Instantiate the interface and set the context */
        JavaScriptInterface(Context c) {
            mContext = c;
        }

        /** Show a toast from the web page */
        public void showToast(String toast) {
            Toast.makeText(mContext, toast, Toast.LENGTH_SHORT).show();
        }
    }
    
    /**
     * Provides a hook for calling "alert" from javascript. Useful for
     * debugging your javascript.
     */
    final class MyWebChromeClient extends WebChromeClient {
        @Override
        public boolean onJsAlert(WebView view, String url, String message, JsResult result) {
            Log.d(LOG_TAG, message);
            result.confirm();
            return true;
        }
    }
}