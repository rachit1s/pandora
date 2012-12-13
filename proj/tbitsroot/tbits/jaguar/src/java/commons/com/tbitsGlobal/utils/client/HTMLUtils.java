package commons.com.tbitsGlobal.utils.client;

/**
 * This will contain the html utils for example: Encode/Decode HTML for XSS
 * @author sandeepgiri
 *
 */
public class HTMLUtils {
	/**
	 * Encodes the html tags such that those tags can be displayed as such instead of being interpreted as html by browser.
	 * Once such use is while displaying error.
	 * < to &lt; 
	 * > to &gt;
	 */
	public static String encodeHTML(String html)
	{
		return html.replaceAll("\\&","&amp;")
		.replaceAll("<", "&lt;")
		.replaceAll(">", "&gt;")
		.replaceAll("'", "&apos;")
		.replaceAll("\"", "&quot;");
	}
	
	public static String decodeHTML(String html)
	{
		return html
		.replaceAll("&lt;", "<")
		.replaceAll("&gt;", ">")
		.replaceAll("&apos;", "'")
		.replaceAll("&quot;", "\"")
		.replaceAll("&amp;", "&");
	}
	public static void main(String[] args) {
		String s = "<html name=\"sandee\"> <tag value='sandee&sss'>";
		String enStr = encodeHTML(s);
		String decString = decodeHTML(enStr);
		if(s.equals(decString))
			System.out.println("Decoded is lossless");
		System.out.println("Original: " + s);
		System.out.println("Encoded: " + enStr);
		System.out.println("decoded: " + decString);
	}
}
