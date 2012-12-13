/*
 * Copyright (c) 2005 Transbit Technologies Pvt. Ltd. All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of Transbit Technologies Pvt. Ltd. ("Confidential Information").  You
 * shall not disclose such Confidential Information and shall use
 * it only in accordance with the terms of the license agreement
 * you entered into with Transbit Technologies Pvt. Ltd.
 */



/**
 * TBitsConstants.java
 *
 * $Header:
 *
 */
package transbit.tbits.Helper;

//~--- JDK imports ------------------------------------------------------------

//Java Imports.
import java.util.regex.Pattern;

//~--- interfaces -------------------------------------------------------------

/**
 * This class contains the enums that can be used across classes in TBits.
 *
 *
 * @author : Vaibhav.
 * @version : $Id: $
 */
public interface TBitsConstants {

    // Package names to be used while getting the logger.
    public static final String PKG_ADMIN     = "transbit.tbits.admin";
    public static final String PKG_API       = "transbit.tbits.api";
    public static final String PKG_COMMON    = "transbit.tbits.common";
    public static final String PKG_CONFIG    = "transbit.tbits.config";
    public static final String PKG_DOMAIN    = "transbit.tbits.domain";
    public static final String PKG_EXCEPTION = "transbit.tbits.exception";
    public static final String PKG_EXTERNAL  = "transbit.tbits.external";
    public static final String PKG_INDEXER   = "transbit.tbits.indexer";
    public static final String PKG_MAIL      = "transbit.tbits.mail";
    public static final String PKG_REPORT    = "transbit.tbits.report";
    public static final String PKG_SCHEDULER = "transbit.tbits.scheduler";
    public static final String PKG_SEARCH    = "transbit.tbits.search";
    public static final String PKG_UTIL      = "transbit.tbits.util";
    public static final String PKG_WEBAPPS   = "transbit.tbits.webapps";

    // Possible sources of request.
    public static final int SOURCE_WEB     = 101;
    public static final int SOURCE_EMAIL   = 102;
    public static final int SOURCE_CMDLINE = 103;
    public static final int SOURCE_TVN = 104;

    // Format of Date printed in the RSS feed.
    public static final String RSS_DATE_FORMAT = "EEE, dd MMM yyyy HH:mm:ss";
    public static final String PROP_BA_PREFIX  = "tbits.cmd.sysPrefix";

    //
    // Property keys that will be part of system properties depending on the
    // source.
    //
    public static final String PROP_BA_NAME = "tbits.email.sysName";

    // Character used to indicate that a user is primary in that field.
    public static final String PRIMARY_SIGN = "*";

    // Format of Date while indexing/searching in lucene.
    public static final String LUCENE_DATE_FORMAT = "yyyyMMddHHmmss";

    // Format of Date when inserting into database as a string.
    public static final String DB_DATE_FORMAT = "yyyy-MM-dd HH:mm:ss.sss";

    // Format of Date expected by the API.
    public static final String API_DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";

    public static final String API_TIME_FORMAT = "HH:mm:ss";
    
    public static final String API_DATE_ONLY_FORMAT = "yyyy-MM-dd" ;
    
    // Regular expressions to match date/time/datetime.
    public static final String STR_DATE_PATTERN     = "([0-9]{1,2})/([0-9]{1,2})(/([0-9]{2,4}))?";
    public static final String STR_TIME_PATTERN     = "([0-9]{1,2}):([0-9]{1,2})(:[0-9]{1,2})?";
    public static final String STR_DATETIME_PATTERN = "([0-9]{4})\\-([0-9]{1,2})\\-([0-9]{2,4}) " +    // Date Part.
        "([0-9]{1,2}):([0-9]{1,2}):([0-9]{1,2})?";                                                     // Time part.

    // Pattern objects used to parse date/time/datetime.
    public static final Pattern DATE_PATTERN = Pattern.compile(STR_DATE_PATTERN);
    public static final Pattern TIME_PATTERN = Pattern.compile(STR_TIME_PATTERN);

    // Mail formats.
    public static int           TEXT_FORMAT      = 0;
    public static int           HTML_FORMAT      = 1;
    public static final Pattern DATETIME_PATTERN = Pattern.compile(STR_DATETIME_PATTERN);
    public static int           XML_FORMAT       = 2;
    public static final String  TR_SRC_REQUEST   = "transferred_request";

   //   These properties are related to the DataBase pool and are moved
   //   from tbits.properties to here
    
    public static final String poolList = "tbitsPool";
    public static final String primaryPool = "tbitsPool";
    
    // Related to Request Transfer functionality.
    public static final String TR_REQ_HEADER    = "X-TBits-Transfer-REQ";
    public static final String TR_FWD_HEADER    = "X-TBits-Transfer-FWD";
    public static final String TR_ACK_HEADER    = "X-TBits-Transfer-ACK";
    public static final int    STATIC_TOOLTIP   = 1;
    public static final int    SITE_ZONE        = 2;
    public static int          RSS_FORMAT       = 4;
    public static int          RR_VOLUNTEER     = 1;
    public static int          RANDOM_VOLUNTEER = 2;
    public static final int    PERMISSION_ADMIN = 10;

    // Volunteer generation.
    public static int NO_VOLUNTEER = 0;

    // Related to nature of Tooltip over smart links
    public static final int NO_TOOLTIP = 0;

    // Notify related enum.
    public static int NO_NOTIFY    = 0;
    public static int NOTIFY_EMAIL = 1;

    // Roles
    public static final int MANAGER = 8;

    // List of valid preferred zones.
    public static final int LOCAL_ZONE              = 1;
    public static final int GMT_ZONE                = 3;
    public static final int FILTER_SUBSCRIBER       = 4;
    public static final int FILTER_PRIMARY_ASSIGNEE = 8;

    // List of valid filters in My Requests Page.
    public static final int FILTER_LOGGER   = 1;
    public static final int FILTER_ASSIGNEE = 2;
    public static int       EXCEL_FORMAT    = 3;
    public static final int DYNAMIC_TOOLTIP = 2;
    public static final int DESC_ORDER      = 1;
    
    //Related To AutoVue
    public static final int LEFT_FRAME_NUM = 10;
    public static final int APPLET_FRAME_NUM = 11;

    // Default Web Date Format.
    public static final String DEFAULT_WEB_FORMAT = "MM/dd/yyyy HH:mm:ss zzz";

    // Default List Date Format.
    public static final String DEFAULT_LIST_FORMAT = "MM/dd/yyyy HH:mm:ss zzz";

    // Default draft auto save rate, 5 mins
    public static final int DEFAULT_AUTO_SAVE_RATE = 5;

    // Final Attributes that represent the order in which actions should be
    // displayed.
    public static final int ASC_ORDER = 0;
    public static final int ADMIN     = 9;

    //~--- constant enums -----------------------------------------------------

    public static enum OutputFormat {
        TEXT, HTML, XML, XLS, EXCEL, RSS
    }
    public static enum Source { WEB, EMAIL, CMDLINE };
	public static final String X_AUTOREPLY = "X-Autoreply";
	public static final String X_TBITS_CATEGORY = "X-TBits-Category";
	
	public static final String JAVASCRIPT_REGEX = "(?i)(?:<[ \n\r]*script[^>]*>)(.*?)(?:<[ \n\r]*/script[^>]*>)";

	public static final int CONTENT_TYPE_TEXT = 1;
	public static final int CONTENT_TYPE_HTML = 0;
	public static final String CHARSET = "UTF-8";
}
