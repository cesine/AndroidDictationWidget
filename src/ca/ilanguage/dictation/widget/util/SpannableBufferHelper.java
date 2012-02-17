package ca.ilanguage.dictation.widget.util;

import java.util.StringTokenizer;

import com.google.gdata.util.common.util.Base64;
import com.google.gdata.util.common.util.Base64DecoderException;

import android.content.Context; //import android.graphics.Typeface;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.CharacterStyle;
import android.text.style.StyleSpan;
import android.text.style.URLSpan;
//import android.util.Log;

@Deprecated
public class SpannableBufferHelper {

	// private final String TAG = "SpannableBufferHelper";
	private final int URL_SPAN_TYPE = 65534;
	private final int HTML_SPAN_TYPE = 65533;
	private final int IMG_SPAN_TYPE = 65532;
	@SuppressWarnings("unused")
	private Context c = null;

	public void debugWriteBuffer(String title, Spannable seq) {
		// Log.d(TAG, title);
		SpannableStringBuilder ssb = new SpannableStringBuilder(seq);
		Object[] spans = ssb.getSpans(0, ssb.length(), StyleSpan.class);
		Object[] urls = ssb.getSpans(0, ssb.length(), URLSpan.class);
		Object[] htmls = ssb.getSpans(0, ssb.length(), HTMLEmbedSpan.class);
		Object[] allobjs = ssb.getSpans(0, ssb.length(), Object.class);
		Object[] imgs = ssb.getSpans(0, ssb.length(), ImageEmbedSpan.class);
		// Log.d(TAG, "Total number of spans in buffer: " + allobjs.length);
		for (int i = 0; i < allobjs.length; i++) {
			// String onespan = allobjs[i].getClass().getName();
			// Log.d(TAG, "Name #" + i + ":" + onespan);
		}
		for (int i = 0; i < spans.length; i++) {
			// StyleSpan current = (StyleSpan) (spans[i]);
			// boolean isBold = current.getStyle() == Typeface.BOLD;
			// boolean isItalic = current.getStyle() == Typeface.ITALIC;
			// Log.d(TAG, "Spans[" + i + "], SpanStart="+
			// ssb.getSpanStart(current) + "SpanEnd="+ ssb.getSpanEnd(current) +
			// " flags:"+ ssb.getSpanFlags(current) + " Bold:" + isBold+
			// " Italic: " + isItalic);
		}
		for (int i = 0; i < urls.length; i++) {
			// URLSpan current = (URLSpan) (urls[i]);
			// Log.d(TAG, "Urls[" + i + "], SpanStart="+
			// ssb.getSpanStart(current) + "SpanEnd="+ ssb.getSpanEnd(current) +
			// " flags:"+ ssb.getSpanFlags(current) + " URL:" +
			// current.getURL());
		}
		for (int i = 0; i < htmls.length; i++) {
			// HTMLEmbedSpan current = (HTMLEmbedSpan) (htmls[i]);
			// Log.d(TAG, "HTML[" + i + "], SpanStart="+
			// ssb.getSpanStart(current) + "SpanEnd="+ ssb.getSpanEnd(current) +
			// " flags:"+ ssb.getSpanFlags(current) + " Markup:"+
			// current.getHtml());
		}
		for (int i = 0; i < imgs.length; i++) {
			// ImageEmbedSpan current = (ImageEmbedSpan) (imgs[i]);
			// Log.d(TAG, "Images[" + i + "], SpanStart="+
			// ssb.getSpanStart(current) + "SpanEnd="+ ssb.getSpanEnd(current) +
			// " flags:"+ ssb.getSpanFlags(current) + " Src:" +
			// current.getSrc());
		}
		int next = 0;
		int total = 0;
		int iteration = 0;
		@SuppressWarnings("unused")
		StyleSpan curTran = null;
		if (ssb != null) {
			long failsafe = 0;
			next = ssb.nextSpanTransition(0, ssb.length(), StyleSpan.class);
			total += next;
			while (total < ssb.length()) {
				try {
					curTran = (StyleSpan) spans[iteration++];
				} catch (ArrayIndexOutOfBoundsException ae) {
					// Log.d(TAG,"More transitions while looking at buffer than returned by getSpans!");
				} catch (ClassCastException ce) {
					// Log.d(TAG,"Getspans returned an object which is not a StyleSpan!");
				}
				// Log.d(TAG, "Transition at index:" + total);
				next = ssb.nextSpanTransition(total, ssb.length() - total,
						StyleSpan.class);
				total += next;
				if ((failsafe++) > ssb.length()) {
					break;
				}
			}
		} else {
			// Log.d(TAG, "Buffer is null.");
		}
	}

	public String SpannableToXHTML(Spannable seq) {
		SpannableStringBuilder ssb = new SpannableStringBuilder(seq);
		Object[] spans = ssb.getSpans(0, ssb.length(), StyleSpan.class);
		Object[] urls = ssb.getSpans(0, ssb.length(), URLSpan.class);
		Object[] htmls = ssb.getSpans(0, ssb.length(), HTMLEmbedSpan.class);
		Object[] imgs = ssb.getSpans(0, ssb.length(), ImageEmbedSpan.class);
		for (int i = 0; i < spans.length; i++) {
			StyleSpan current = (StyleSpan) (spans[i]);
			insertMarkupAndFixSpans(current.getStyle(), ssb
					.getSpanStart(current), ssb.getSpanEnd(current), ssb);
		}
		for (int i = 0; i < urls.length; i++) {
			URLSpan current = (URLSpan) (urls[i]);
			insertLinksAndFixSpans(current.getURL(), ssb.getSpanStart(current),
					ssb.getSpanEnd(current), ssb);
		}
		for (int i = 0; i < htmls.length; i++) {
			HTMLEmbedSpan current = (HTMLEmbedSpan) (htmls[i]);
			insertHTMLAndFixSpans(current.getHtml(), ssb.getSpanStart(current),
					ssb);
		}
		for (int i = 0; i < imgs.length; i++) {
			ImageEmbedSpan current = (ImageEmbedSpan) (imgs[i]);
			insertImgsAndFixSpans(current.getSrc(), ssb.getSpanStart(current),
					ssb);
		}
		// Log.d(TAG, "SpannableToXHTML returns: " + ssb.toString());
		return ssb.toString();
	}

	private int insertMarkupAndFixSpans(int style, int startoffset,
			int endoffset, SpannableStringBuilder ssb) {
		String starttag = null;
		String endtag = null;
		switch (style) {
			case android.graphics.Typeface.BOLD :
				starttag = "<b>";
				endtag = "</b>";
				break;
			case android.graphics.Typeface.ITALIC :
				starttag = "<i>";
				endtag = "</i>";
				break;
			case android.graphics.Typeface.BOLD_ITALIC :
				starttag = "<i><b>";
				endtag = "</b></i>";
				break;
			case android.graphics.Typeface.NORMAL :
				starttag = "";
				endtag = "";
		}
		ssb.insert(startoffset, starttag);
		ssb.insert(endoffset + starttag.length(), endtag);
		return starttag.length() + endtag.length();
	}

	private int insertLinksAndFixSpans(String URL, int startoffset,
			int endoffset, SpannableStringBuilder ssb) {
		String starttag = "<a href = \'" + URL + "\'>";
		String endtag = "</a>";
		ssb.insert(startoffset, starttag);
		ssb.insert(endoffset + starttag.length(), endtag);
		return starttag.length() + endtag.length();
	}

	private int insertHTMLAndFixSpans(String html, int startoffset,
			SpannableStringBuilder ssb) {
		ssb.insert(startoffset, html);
		return html.length();
	}

	private int insertImgsAndFixSpans(String imgsrc, int startoffset,
			SpannableStringBuilder ssb) {
		String html = "<img src = \"" + imgsrc + "\"/>";
		ssb.insert(startoffset, html);
		return html.length();
	}

	public Spannable XHTMLToSpannable(CharSequence xhtml) {
		new SpannableStringBuilder();
		for (int i = 0; i < xhtml.length(); i++) {
			char ch = xhtml.charAt(i);
			if (isTagCharacter(ch)) {

			}
		}
		return null;
	}

	private boolean isTagCharacter(char ch) {
		switch (ch) {
			case '<' :
			case '>' :
			case '/' :
				return true;
			default :
				return false;
		}
	}

	public String getSerializedSequence(CharSequence xhtml) {
		SpannableStringBuilder ssb = new SpannableStringBuilder(xhtml);
		Object[] spans = ssb.getSpans(0, ssb.length(), CharacterStyle.class);
		StringBuilder sb = new StringBuilder("|"
				+ Base64.encode(xhtml.toString().getBytes()));
		for (int i = 0; i < spans.length; i++) {
			int type = 0;
			int start = 0;
			int end = 0;
			int flags = 0;
			CharacterStyle current = (CharacterStyle) (spans[i]);
			if (current instanceof StyleSpan) {
				StyleSpan currentstyle = (StyleSpan) current;
				type = currentstyle.getStyle();
				start = ssb.getSpanStart(currentstyle);
				end = ssb.getSpanEnd(currentstyle);
				flags = ssb.getSpanFlags(currentstyle);
				sb.append("|" + type + "," + start + "," + end + "," + flags);
			} else if (current instanceof URLSpan) {
				URLSpan currenturl = (URLSpan) current;
				type = URL_SPAN_TYPE;
				start = ssb.getSpanStart(currenturl);
				end = ssb.getSpanEnd(currenturl);
				flags = ssb.getSpanFlags(currenturl);
				sb.append("|" + type + "," + start + "," + end + "," + flags
						+ "," + currenturl.getURL());
			} else if (current instanceof HTMLEmbedSpan) {
				HTMLEmbedSpan htmlspan = (HTMLEmbedSpan) current;
				type = HTML_SPAN_TYPE;
				start = ssb.getSpanStart(htmlspan);
				end = start;
				flags = ssb.getSpanFlags(htmlspan);
				sb.append("|" + type + "," + start + "," + end + "," + flags
						+ "," + htmlspan.getHtml());
			} else if (current instanceof ImageEmbedSpan) {
				ImageEmbedSpan imgspan = (ImageEmbedSpan) current;
				type = IMG_SPAN_TYPE;
				start = ssb.getSpanStart(imgspan);
				end = start;
				flags = ssb.getSpanFlags(imgspan);
				sb.append("|" + type + "," + start + "," + end + "," + flags
						+ "," + imgspan.getSrc());
			}
		}
		return sb.toString();
	}

	public CharSequence deSerializeStringToSequence(String encoded, Context c) {
		String[] splits = encoded.split("\\|");
		String contentBase64 = splits[1];
		this.c = c;
		int item = 2;
		byte[] content = null;
		try {
			content = Base64.decode(contentBase64);
		} catch (Base64DecoderException de) {
			// Log.e(TAG,
			// "Failed to decode content from base 64 to byte array!");
			return null;
		}
		String res = new String(content);
		SpannableStringBuilder ssb = new SpannableStringBuilder(res);
		if (splits.length > 2) {
			for (String istylestr = splits[item]; item < splits.length;) {
				StringTokenizer strTok = new StringTokenizer(istylestr, ",",
						false);
				int count = 0, style = -1, start = -1, flags = 0, end = 0;
				String contentOfSpan = null;
				while (strTok.hasMoreTokens()) {
					String part = strTok.nextToken();
					try {
						if (count == 0) {
							style = Integer.parseInt(part);
						} else if (count == 1) {
							start = Integer.parseInt(part);
						} else if (count == 2) {
							end = Integer.parseInt(part);
						} else if (count == 3) {
							flags = Integer.parseInt(part);
						} else if (count == 4) {
							contentOfSpan = part;
						}
					} catch (NumberFormatException e) {
						// Log.d(TAG, "Exception when parsing spans at phase:"+
						// count + "/" + item);
					}
					count++;
				}
				CharacterStyle currentSpan = null;
				if (style == IMG_SPAN_TYPE) {
					currentSpan = new ImageEmbedSpan(contentOfSpan, c);
					ssb.setSpan(currentSpan, start, end + 1, flags);
					// Log.d(TAG,"Attached ImageEmbedSpan to buffer(start,end,flags):"+
					// start + "," + end + "(+1)," + flags);
				} else if (style == URL_SPAN_TYPE) {
					currentSpan = new URLSpan(contentOfSpan);
					ssb.setSpan(currentSpan, start, end, flags);
					// Log.d(TAG,
					// "Attached UrlSpan to buffer(start,end,flags):"+ start +
					// "," + end + "," + flags);
				} else if (style == HTML_SPAN_TYPE) {
					currentSpan = new HTMLEmbedSpan(contentOfSpan, c);
					ssb.setSpan(currentSpan, start, end + 1, flags);
					// Log.d(TAG,"Attached HTMLEmbedSpan to buffer(start,end,flags):"+
					// start + "," + end + "(+1)," + flags);
				} else {
					currentSpan = new StyleSpan(style);
					ssb.setSpan(currentSpan, start, end, flags);
					// Log.d(TAG,
					// "Attached StyleSpan to buffer(start,end,flags):"+ start +
					// "," + end + "," + flags);
				}
				try {
					istylestr = splits[++item];
				} catch (ArrayIndexOutOfBoundsException e) {
					// Log.d(TAG, "End of parsing reached.");
					continue;
				}
			}
		} else {
			// Log.d(TAG, "Entry does not contain styling information.");
		}
		return ssb;

	}

	public void enableContextAwareness(Context c) {
		this.c = c;
	}
}