package commons.com.tbitsGlobal.utils.client;

import java.util.List;

import com.google.gwt.core.client.GWT;

import commons.com.tbitsGlobal.utils.client.domainObjects.AttachmentInfoClient;
import commons.com.tbitsGlobal.utils.client.service.UtilService;
import commons.com.tbitsGlobal.utils.client.service.UtilServiceAsync;

public class GlobalConstants {
	
	public static final UtilServiceAsync utilService = GWT.create(UtilService.class);
	
	public static int appState = 0;
	
	public static List<AttachmentInfoClient> filesClipboard = null;
	
	public static final String TIME_FORMAT 		= "h:mm a";
	public static final String API_DATE_FORMAT 	= "yyyy-MM-dd HH:mm:ss";
	
	public static final String copyRightText = "\u00a9 tBits Global Pvt. Ltd. | " +
			"<a target='_blank' href='http://www.tbitsglobal.com'>tbitsGlobal.com</a>";

	public static boolean isTagsSupported = false;
	public static boolean isTvnSupported = false;

	final public static int SEARCH_PAGESIZE = 50;

	public static TbitsTreeRequestData requestClipboard = null;
	
	// Possible sources of request.
    public static final int SOURCE_WEB     = 101;
    public static final int SOURCE_EMAIL   = 102;
    public static final int SOURCE_CMDLINE = 103;
    public static final int SOURCE_TVN = 104;
    
    // Mail formats.
    public static int           TEXT_FORMAT      = 0;
    public static int           HTML_FORMAT      = 1;
    
    public static final String      HTML_LINE_BREAK   = "<br>";
    public static final String      TEXT_LINE_BREAK   = "\n";
    
    public static final int ASC_ORDER = 0;

	public static String TOKEN_VIEW 	= "view";

	public static String TOKEN_UPDATE 	= "update";

	public static String TOKEN_BA 		= "ba";

	public static String TOKEN_DQL 		= "dql";

	public static String TOKEN_TAGS_PUBLIC 	= "publicTag";

	public static String TOKEN_TAGS_PRIVATE = "privateTag";
}
