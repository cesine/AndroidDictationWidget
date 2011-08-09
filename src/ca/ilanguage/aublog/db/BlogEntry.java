package ca.ilanguage.aublog.db;

import java.text.SimpleDateFormat;
import java.util.Date;

public class BlogEntry {

	/*TODO add and publish with labels
	 * 
	 */
	private CharSequence blogEntry;
	private String title;
	private Date created;
	private int id;
	private int publishedIn;
	private boolean isDraft;

	public CharSequence getBlogEntry() {
		return blogEntry;
	}
	public void setBlogEntry(CharSequence blogEntry) {
		this.blogEntry = blogEntry;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public Date getCreated() {
		return created;
	}
	public void setCreated(Date created) {
		this.created = created;
	}
	public int getPublishedIn() {
		return publishedIn;
	}
	public void setPublishedIn(int publishedIn) {
		this.publishedIn = publishedIn;
	}
	public boolean isDraft() {
		return isDraft;
	}
	public boolean getDraft() {
		return isDraft();
	}
	public void setDraft(boolean isDraft) {
		this.isDraft = isDraft;
	}

	public String toString() {
		SimpleDateFormat df = new SimpleDateFormat("MM/dd/yyyy HH:mm");
		return "" + getTitle() + " - " + df.format(created);
	}

	public int getId() {
		return this.id;
	}

	public void setId(int id) {
		this.id = id;
	}
}