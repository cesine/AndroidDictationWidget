package ca.ilanguage.aublog.service;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import android.app.Activity; //import android.util.Log;

import com.google.gdata.client.GoogleService;
import com.google.gdata.data.Entry;
import com.google.gdata.data.Feed;
import com.google.gdata.data.PlainTextConstruct;
import com.google.gdata.util.ServiceException;

public class BloggerAPI implements BlogInterface {
	private String userid = null;
	private String password = null;
	private static GoogleService myService;
	private final String APPNAME = "AuBlog";
	private final String SERVICENAME = "blogger";
	// private final String TAG = "BloggerAPI";
	private static String feedUri;
	private static final String FEED_URI_BASE = "https://www.blogger.com/feeds";
	private static final String POSTS_FEED_URI_SUFFIX = "/posts/default";
	private static final String COMMENTS_FEED_URI_SUFFIX = "/comments/default";
	private boolean connected = false;
	protected BloggerAPI() {
		try {
			new URL("http://www.blogger.com/feeds/default/blogs");
		} catch (MalformedURLException e) {
			// Log.e(TAG, "The default blog feed url is malformed!");
		}
	}

	public boolean createPost(Activity parent, String authToken,
			String postUrl, String titleType, String title, String contentType,
			String content, String authorName, String authorEmail,
			boolean isDraft) throws ServiceException {
		Entry entry = new Entry();
		entry.setTitle(new PlainTextConstruct(title));
		entry.setContent(new PlainTextConstruct(content));
		if (myService == null) {
			getAuthId(this.userid, this.password);
			// Log.d(TAG,"GoogleService is null while creating post. Are you calling this from outside the PreviewDialog? ");
		}
		if (myService == null) {
			// Log.e(TAG,"The GoogleService is null while creating post. Are you calling this from outside the PreviewDialog?");
			return false;
		}
		try {
			URL postingUrl = new URL(postUrl);
			if (myService.insert(postingUrl, entry) != null) {
				return true;
			} else {
				return false;
			}
		} catch (MalformedURLException mfue) {
			// Log.e(TAG, "Malformed post URL used: " + postUrl);
			return false;
		} catch (IOException ioe) {
			// Log.e(TAG, "IOException when inserting post. Message:"+
			// ioe.getMessage());
			return false;
		} catch (ServiceException se) {
			// Log.e(TAG, "ServiceException when inserting post. Message:"+
			// se.getMessage());
			return false;
		}
	}

	public Feed getAllPosts(String username, String password)
			throws ServiceException, IOException {
		if (myService == null) {
			getAuthId(username, password);
			// Log.d(TAG,"GoogleService is null while creating post. Are you calling this from outside the PreviewDialog? ");
		}
		String blogId = getBlogId(myService);
		feedUri = FEED_URI_BASE + "/" + blogId;
		URL feedUrl = new URL(feedUri + POSTS_FEED_URI_SUFFIX);
		Feed resultFeed = myService.getFeed(feedUrl, Feed.class);
		System.out.println(resultFeed.getTitle().getPlainText());
		return resultFeed;
	}

	public Feed getAllPostComments(String username, String password,
			String postID) throws ServiceException, IOException {
		if (myService == null) {
			getAuthId(username, password);
			// Log.d(TAG,"GoogleService is null while creating post. Are you calling this from outside the PreviewDialog? ");
		}
		String commentsFeedUri = feedUri + "/" + postID
				+ COMMENTS_FEED_URI_SUFFIX;
		URL feedUrl = new URL(commentsFeedUri);
		Feed resultFeed = myService.getFeed(feedUrl, Feed.class);
		return resultFeed;
	}

	public String getAuthId(String username, String password)
			throws com.google.gdata.util.AuthenticationException {
		this.userid = username;
		this.password = password;
		myService = new GoogleService("blogger", BlogConfigBLOGGER.APPNAME);
		myService.setUserCredentials(username, password);
		return "this is not really needed";
	}

	public String getPostUrl() throws ServiceException, IOException {
		String blogId = getBlogId(myService);
		return "https://www.blogger.com/feeds/" + blogId + "/posts/default";
	}

	private static String getBlogId(GoogleService myService)
			throws ServiceException, IOException {
		final URL feedUrl = new URL(
				"http://www.blogger.com/feeds/default/blogs");
		Feed resultFeed = myService.getFeed(feedUrl, Feed.class);
		if (resultFeed.getEntries().size() > 0) {
			Entry entry = resultFeed.getEntries().get(0);
			return entry.getId().split("blog-")[1];
		}
		throw new IOException("User has no blogs!");
	}

	public BloggerAPI(String userid, String password) {
		this.userid = userid;
		this.password = password;
		myService = new GoogleService(SERVICENAME, APPNAME);
		if (myService != null) {
			try {
				myService.setUserCredentials(this.userid, this.password);
				connected = true;
			} catch (com.google.gdata.util.AuthenticationException e) {
				// Log.e(TAG, "Authentication failed for user " + this.userid+
				// ".");
			}
		} else {
			// Log.e(TAG, "Unable to create GoogleService!");
		}
	}

	public boolean statusOk() {
		return connected;
	}

	public void setInstanceConfig(CharSequence config) {
		if (config != null) {
			config.toString();
		} else {
			// Log.e(TAG,"Trying to set a null installce config when configuring Google Blogger API!");
		}
	}
}