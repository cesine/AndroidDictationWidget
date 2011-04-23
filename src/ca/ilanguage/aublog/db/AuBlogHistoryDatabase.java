package ca.ilanguage.aublog.db;

import java.util.regex.Pattern;

import android.app.SearchManager;
import android.content.ContentProvider;
import android.net.Uri;
import android.provider.BaseColumns;


public class AuBlogHistoryDatabase {
	 /**
     * Special value for {@link SyncColumns#UPDATED} indicating that an entry
     * has never been updated, or doesn't exist yet.
     */
    public static final long UPDATED_NEVER = -2;

    /**
     * Special value for {@link SyncColumns#UPDATED} indicating that the last
     * update time is unknown, usually when inserted from a local file source.
     */
    public static final long UPDATED_UNKNOWN = -1;

    public interface SyncColumns {
    	String UPDATED = "updated";
    }
    /**
     * An interface which states the columns in the AuBlog History table
     */
    interface AuBlogHistoryColumns{
	    String ENTRY_TITLE  ="title";
	    String ENTRY_LABELS = "labels";
	    String ENTRY_CONTENT = "content";
	    String PUBLISHED_IN = "publishedin";
	    //comma seperated list of ordered pairs, of blog entry id and audiofile corresponding directly to that entry)
	    String AUDIO_FILES = "audiofiles";
	    //arrange entry histories in a heirarchy based on edits
	    String PARENT_ENTRY = "parententry";
	    String DAUGHTER_ENTRY = "daughterentry";
	    String PUBLISHED = "published";
	    String TIME_CREATED = "timecreated";
	    String TIME_EDITED = "timeedited";
	    String LAST_MODIFIED = "lastmodified";
    }
    
    public static final String AUTHORITY = "ca.ilanguage.aublog.provider.AuBlogHistory";
    private static final Uri BASE_CONTENT_URI = Uri.parse("content://" + AUTHORITY);
//    private static final String PATH_AUBLOG_HISTORY = "aubloghistory";
    public static final String AUBLOG_HISTORY_TABLE_NAME = "aubloghistory";
    

    //private static final String PATH_AUBLOG_ENTRY = "aublogentrydb";
//    private static final String PATH_AUBLOG_AUDIO = "aublogaudiodb";
    
    
    private static final String PATH_EXPORT = "export";
    private static final String PATH_SEARCH = "search";
    private static final String PATH_SEARCH_SUGGEST = "search_suggest_query";

    /**
     * Changes in entrys are saved in the database so that the user looses no data, the entrys are linked in a
     * heirarchy so that the previous and following versions can be found.
     * 
     * The history entries have the Columns defined in the interface above, as well as the BASE columns from android
     */
    public static class AuBlogHistory implements AuBlogHistoryColumns, BaseColumns{
    	//This class cannot be instantiated
    	private AuBlogHistory(){
    		
    		
    	}
    	
    	//leads to the database file on the data directory of the device
    	public static final Uri CONTENT_URI =
    		BASE_CONTENT_URI.buildUpon().appendPath(AUBLOG_HISTORY_TABLE_NAME).build();
    	public static final Uri CONTENT_EXPORT_URI =
    		CONTENT_URI.buildUpon().appendPath(PATH_EXPORT).build();
    	
    	/**
         * The MIME type of {@link #CONTENT_URI} providing a directory of aublog histories.
         */
    	public static final String CONTENT_TYPE = 
        	"vnd.android.cursor.dir/vnd.ilanguage.aubloghistory";
    	/**
         * The MIME type of a {@link #CONTENT_URI} sub-directory of a single aublog history.
         */
        public static final String CONTENT_ITEM_TYPE =
    		"vnd.android.cursor.item/vnd.openlanguage.ilanguage.aubloghistory";
    	
    	/** Default "ORDER BY" clause in SQL is by date modified */
        public static final String DEFAULT_SORT_ORDER = AuBlogHistoryColumns.LAST_MODIFIED+ " DESC";
        //for alphabetical by title
        //public static final String DEFAULT_SORT = VendorsColumns.NAME + " COLLATE NOCASE ASC";
        

        /*
    	 * Needed for searchable Modules
    	 */
    	public static Uri buildSearchUri(String query){
    		return CONTENT_URI.buildUpon().appendPath(PATH_SEARCH).appendPath(query).build();
    	}
    	public static boolean isSearchUri(Uri uri){
    		//1 refers to the "search" position added in the buildSearchUri funciton above
    		return PATH_SEARCH.equals(uri.getPathSegments().get(1));
    	}
    	public static String getSearchQuery(Uri uri){
    		//2 refers to the "query" position added in the buildSearchUri function above
    		return uri.getPathSegments().get(2);
    	}
    	public static final String SEARCH_SNIPPET = "search_snippet";

        
    }
    /*
     * General classes and useful functions to be used with the Domain classes
     */
    public static class SearchSuggest{
    	public static final Uri CONTENT_URI = 
    		BASE_CONTENT_URI.buildUpon().appendPath(PATH_SEARCH_SUGGEST).build();
    	public static final String DEFAULT_SORT = SearchManager.SUGGEST_COLUMN_TEXT_1
    		+ " COLLATE NOCASE ASC";
    }
    /**
     * * Sanitize the given string to be {@link Uri} safe for building
     * {@link ContentProvider} paths. For more info see best practices in ioschedule utils package.
     * @param input to become a uri
     * @return output safe to become a uri (numbers letters underscores and hyphens)
     */
    public static String santizeId(String input){
    	return sanitizeId(input, false);
    }
    /**
     * * Sanitize the given string to be {@link Uri} safe for building. Allows client to specify if 
     * (parenthetical expressions) should be removed from the string.
     * 
     * {@link ContentProvider} paths.
     * @param input to become a uri
     * @return output safe to become a uri (numbers letters underscores and hyphens)
     */
    public static String sanitizeId(String input, boolean stripParentheses){
    	/** Used to sanitize a string to be {@link Uri} safe. */
        Pattern sSanitizePattern = Pattern.compile("[^a-z0-9-_]");
        Pattern sParenthesesPattern = Pattern.compile("\\(.*?\\)");

    	if (input == null) return null;
    	if (stripParentheses) {
    		//Strip out all parenthetical statements when requested
    		input = sParenthesesPattern.matcher(input).replaceAll("");
    	}
    	return sSanitizePattern.matcher(input.toLowerCase()).replaceAll("");
    }
    //null constructor, This class cannot be instantiated
    private AuBlogHistoryDatabase(){
    	
    	
    	
    }
    
}
