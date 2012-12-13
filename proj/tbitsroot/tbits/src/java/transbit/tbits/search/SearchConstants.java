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
 * SearchConstants.java
 *
 * $Header:
 *
 */
package transbit.tbits.search;

//~--- JDK imports ------------------------------------------------------------

//Java Imports.
import java.util.Arrays;
import java.util.List;

//~--- interfaces -------------------------------------------------------------

/**
 * This class contains the enums that can be used across classes in TBits.
 *
 * @author  : Vaibhav.
 * @version : $Id: $
 *
 */
public interface SearchConstants {

    // Alphabets of DSQL
    public static final String SPACE        = " ";
    public static final String SEMICOLON    = ";";
    public static final String QUOTE        = "\"";
    public static final String PLUS         = "+";
    public static final String OR           = "or";
    public static final String OPEN_SQUARE  = "[";
    public static final String OPEN_PAREN   = "(";
    public static final String NO_EXPAND    = "noexpand";
    public static final String NOT          = "not";
    public static final String NEGATION     = "-";
    public static final String NE           = "!=";
    public static final String MINUS        = "-";
    public static final String MEMBER_OF    = "memberof";
    public static final String MEMBERS_OF   = "membersof";
    public static final String LT           = "<";
    public static final String LE           = "<=";
    public static final String IN           = "in";
    public static final String GT           = ">";
    public static final String GE           = ">=";
    public static final String EXPAND       = "expand";
    public static final String EQ           = "=";
    public static final String COMMA        = ",";
    public static final String COLON        = ":";
    public static final String CLOSE_SQUARE = "]";
    public static final String CLOSE_PAREN  = ")";
    public static final String BETWEEN      = "between";
    public static final String BEFORE       = "before";

    // Operators of DSQL
    public static final String AND   = "and";
    public static final String AFTER = "after";

    // Constants in DSQL
    public static final String TRUE  = "true";
    public static final String ONE   = "1";
    public static final String FALSE = "false";
    public static final String TWO   = "0";

    // Regular Expression to validate date in MM/dd/yyyy format.
    public static final String MM_DD_YYYY = "^((0?[1-9])|(1[012]))/" +    // Month Part.
        "((0?[1-9])|([12][0-9])|(3[01]))/" +                              // Date Part.
            "([0-9]{2}|[0-9]{4})$";                                       // Year Part.

    // Key to the private permission for logger in the permission table.
    public static final String KEY_SUBSCRIBER_PRIVATE = "__SUBSCRIBER_PRIVATE__";

    // Key to the private permission for logger in the permission table.
    public static final String KEY_LOGGER_PRIVATE = "__LOGGER_PRIVATE__";

    // Key to the private permission for logger in the permission table.
    public static final String KEY_ASSIGNEE_PRIVATE = "__ASSIGNEE_PRIVATE__";

    // Regular Expression to validate date in yyyy-MM-dd format.
    public static final String YYYY_MM_DD = "^([0-9]{2}|[0-9]{4})\\-" +    // Year Part.
        "((0?[1-9])|(1[012]))\\-" +                                        // Month Part.
            "((0?[1-9])|([12][0-9])|(3[01]))$";                            // Date Part.

    // Regular Expression to validate date in yyyyMMdd format.
    public static final String YYYYMMDD = "^([0-9]{2}|[0-9]{4})" +    // Year Part.
        "((0?[1-9])|(1[012]))" +                                      // Month Part.
            "((0?[1-9])|([12][0-9])|(3[01]))$";                       // Date Part.

    // Regular Expression to validate time in hh:mm:ss format.
    public static final String HH_MM_SS = "^([0-1]?[0-9]|2[0-3]):([0-5]?[0-9]):([0-5]?[0-9])$";

    // Regular Expression to validate time in hh:mm format.
    public static final String HH_MM = "^([0-1]?[0-9]|2[0-3]):([0-5]?[0-9])$";

    // Regular Expression to validate zone.
    public static final String ZONE_PATTERN = "est|ist|pst|gmt";
    public static final String YESTERDAY    = "yesterday";
    public static final int    XML_FORMAT   = 103;

    // Final Attributes that represent the output medium.
    public static final int    WEB_MEDIUM     = 201;
    public static final String UNREAD_REQUEST = "unread";
    public static final int    TREE_RESULT    = 512;
    public static final String TOMORROW       = "tomorrow";
    public static final String TODAY          = "today";
    public static final int    TEXT_SEVERITY  = 16;

    // Final Attributes that represent the output format.
    public static final int          TEXT_FORMAT        = 101;
    public static final int          SUBSCRIBER_PRIVATE = 4;
   

    // Array of stop words.
    public static final String[] STOP_WORDS = {
        "0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "000", "$", "about", "after", "all", "also", "an", "and", "another", "any", "are", "as", "at", "be", "because", "been", "before", "being",
        "between", "both", "but", "by", "came", "can", "come", "could", "did", "do", "does", "each", "else", "for", "from", "get", "got", "has", "had", "he", "have", "her", "here", "him", "himself",
        "his", "how", "if", "in", "into", "is", "it", "its", "just", "like", "make", "many", "me", "might", "more", "most", "much", "must", "my", "never", "now", "of", "on", "only", "or", "other",
        "our", "out", "over", "re", "said", "same", "see", "should", "since", "so", "some", "still", "such", "take", "than", "that", "the", "their", "them", "then", "there", "these", "they", "this",
        "those", "through", "to", "too", "under", "up", "use", "very", "want", "was", "way", "we", "well", "were", "what", "when", "where", "which", "while", "who", "will", "with", "would", "you",
        "your", "a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "k", "l", "m", "n", "o", "p", "q", "r", "s", "t", "u", "v", "w", "x", "y", "z"
    };
    
    public static final List<String> STOP_WORD_LIST     = Arrays.asList(STOP_WORDS);
    //Final Attribute represent the home panel 
  //   public static final int    HOME_VIEW  = 0;
    
    // Final Attributes that represent the search panel view.

//    public static final int    SIMPLE_VIEW  = 1;
    public static final int    SELF_PRIVATE = 1;
    public static final int    RSS_FORMAT   = 104;
    public static final int    ROLE_PRIVATE = 2;
    public static final String READ_REQUEST = "read";

    // Final Attributes that represent the type of Private Permission the user
    // has.
    public static final int    NO_PRIVATE       = 0;
    public static final int    NO_HIERARCHY     = 128;
    public static final int    NO_FORMATTING    = 64;
    public static final String NEXT             = "next";
    public static final int    MOUSEOVER        = 4;

    // List of roles included in the Role Private attribute.
    public static final int    LOGGER_PRIVATE = 1;
    public static final int    LINK_SUBJECT   = 8;
    public static final String LAST_ACTION_ID = "last_action_id";
    public static final String LASTYEAR       = "lastyear";
    public static final String LASTWEEK       = "lastweek";
    public static final String LASTMONTH      = "lastmonth";

    // Date constants.
    public static final String LAST             = "last";
    public static final int    JAVASCRIPT       = 1;
    public static final int    HTML_FORMAT      = 102;
    public static final int    HIDE_RESULT      = 256;
    public static final int    GROUP_ACTION     = 32;
    public static final int    EMAIL_MEDIUM     = 202;
    public static final int    DESC_ORDER       = 1;
    public static final int    ASSIGNEE_PRIVATE = 2;

    // Final Attributes that represent the sort order.
    public static final int ASC_ORDER      = 0;
    public static final int ANCHOR         = 2;
    
    public static final int NORMAL_VIEW = 0;
	public static final int ADVANCED_VIEW = 1;
	public static final int ALL_AREAS_VIEW = 2;
	public static final int MY_REQUESTS_VIEW = 3;
    
    //~--- constant enums -----------------------------------------------------

    // Connectives used in DSQL.
    public static enum Connective {
        C_AND, C_OR, C_NOT, C_AND_NOT, C_OR_NOT, C_NOT_NOT
    }

    //~--- enums --------------------------------------------------------------

    // Enum to represent Rendering Type.
    public static enum RenderType {
        RENDER_FLAT(0), RENDER_HIER(1);

        private final int value;

        //~--- constructors ---------------------------------------------------

        RenderType(int value) {
            this.value = value;
        }

        //~--- methods --------------------------------------------------------

        public static RenderType toRenderType(String value) {
            if ((value != null) && (value.trim().equals("") == false)) {
                if (value.equals("0")) {
                    return RENDER_FLAT;
                } else if (value.equals("1")) {
                    return RENDER_HIER;
                } else {
                    return RENDER_HIER;
                }
            }

            return RENDER_HIER;
        }

        public String toString() {
            return Integer.toString(this.value);
        }

        public String value() {
            switch (this) {
            case RENDER_FLAT :
                return "RENDER_FLAT";

            case RENDER_HIER :
                return "RENDER_HIER";
            }

            return "RENDER_HIER";
        }
    }

    ;

    //~--- enums --------------------------------------------------------------

    // Enum to represent Result Type.
    public static enum ResultType {
        RESULT_NORMAL(1), RESULT_ROOT(2), RESULT_PARENT(3), RESULT_LEAF(4);

        private final int value;

        //~--- constructors ---------------------------------------------------

        ResultType(int value) {
            this.value = value;
        }

        //~--- methods --------------------------------------------------------

        public static ResultType toRenderType(String value) {
            if ((value != null) && (value.trim().equals("") == false)) {
                if (value.equals("1")) {
                    return RESULT_NORMAL;
                } else if (value.equals("2")) {
                    return RESULT_ROOT;
                } else if (value.equals("3")) {
                    return RESULT_PARENT;
                } else if (value.equals("4")) {
                    return RESULT_LEAF;
                } else {
                    return RESULT_NORMAL;
                }
            }

            return RESULT_NORMAL;
        }

        public String toString() {
            return Integer.toString(this.value);
        }

        public String value() {
            switch (this) {
            case RESULT_NORMAL :
                return "RESULT_NORMAL";

            case RESULT_ROOT :
                return "RESULT_ROOT";

            case RESULT_PARENT :
                return "RESULT_PARENT";

            case RESULT_LEAF :
                return "RESULT_LEAF";
            }

            return "RESULT_NORMAL";
        }
    }

    ;
}
