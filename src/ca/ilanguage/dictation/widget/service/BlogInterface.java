package ca.ilanguage.dictation.widget.service;

import java.io.IOException;

import com.google.gdata.data.Feed;
import com.google.gdata.util.ServiceException;

import android.app.Activity;

public interface BlogInterface {

	/**
	 * This method is used to get the authentication/session id which can be
	 * used to post entries to blogs and perform other operations which require
	 * authentication.
	 * 
	 * @param username
	 * @param password
	 * @return
	 */

	public String getAuthId(String username, String password)
			throws com.google.gdata.util.AuthenticationException;

	/**
	 * Used to create new posting to selected blog.
	 * 
	 * @param parent
	 *            Reference to calling activity
	 * @param authToken
	 * @param postUrl
	 * @param titleType
	 * @param title
	 * @param contentType
	 * @param content
	 * @param authorName
	 * @param authorEmail
	 * @param isDraft
	 * @throws ServiceException
	 * @return
	 */

	public boolean createPost(Activity parent, String authToken,
			String postUrl, String titleType, String title, String contentType,
			String content, String authorName, String authorEmail,
			boolean isDraft) throws ServiceException;

	/**
	 * Get all posts from blog.
	 * 
	 * @param username
	 * @param password
	 * @throws IOException
	 * @throws ServiceException
	 * @return
	 */

	public Feed getAllPosts(String username, String password)
			throws ServiceException, IOException;

	/**
	 * Get all post comments.
	 * 
	 * @param username
	 * @param password
	 * @param postID
	 * @throws IOException
	 * @throws ServiceException
	 * @return
	 */

	public Feed getAllPostComments(String username, String password,
			String postID) throws ServiceException, IOException;

	/**
	 * Returns the url that can be used to manage blog entries
	 * 
	 * @param authToken
	 * @return
	 * @throws IOException
	 * @throws ServiceException
	 */

	public String getPostUrl() throws ServiceException, IOException;

	/**
	 * This method is called when the config editor creates to GUI to create a
	 * certain type of blog configuration. Implementer needs to add new
	 * TableRows to the TableLayout and setId to the TableRow objects using
	 * numbering from startIdFrom onwards.
	 * 
	 * @param parent
	 *            activity, the blogconfig editor will set this to "this"
	 * @param tl
	 *            The table layout used by the editor
	 * @param context
	 *            , this is the context of the app.
	 * @param startIdFrom
	 *            Start the numbering of tablerow objects from this int onwards.
	 * @param populateFromID
	 *            , if positive, it means that the implementer should populate
	 *            the editor fields from DB using this id.
	 */

	// public CharSequence getConfigEditorData();
	/**
	 * You can use this method to repopulate the API with the saved config
	 * editor data. Typically it's used for blog interfaces which first need
	 * some instance-specific configuration which is to be saved, and then
	 * reloaded at the time of posting.
	 * 
	 * CONTRACT: The getConfigEditorData should return the CharSequence that is
	 * understood by the setInstaceConfig. API is required to save the config to
	 * it's state as long as it's alive.
	 * 
	 */

	public void setInstanceConfig(CharSequence config);

}