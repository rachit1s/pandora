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
 * HtmlSearch.java
 *
 * $Header:
 */
package transbit.tbits.webapps;

//~--- non-JDK imports --------------------------------------------------------
//Third-party imports.
//import net.sf.json.JSONArray;
//import net.sf.json.JSONObject;

import org.apache.log4j.MDC;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import transbit.tbits.ExtUI.SlotFillerFactory;
import transbit.tbits.ExtUI.ISlotFiller;
import transbit.tbits.Helper.ActionHelper;
import transbit.tbits.Helper.Messages;
import transbit.tbits.Helper.TBitsConstants;
import transbit.tbits.Helper.TBitsPropEnum;
import transbit.tbits.api.AttachmentInfo;
import transbit.tbits.authentication.AuthConstants;
import transbit.tbits.common.DTagReplacer;

//TBits Imports.
import transbit.tbits.common.DataSourcePool;
import transbit.tbits.common.DatabaseException;
import transbit.tbits.common.PropertiesHandler;
import transbit.tbits.common.TBitsLogger;
import transbit.tbits.common.Timestamp;
import transbit.tbits.common.Utilities;
import transbit.tbits.config.BAConfig;
import transbit.tbits.config.CaptionsProps;
import transbit.tbits.config.Shortcut;
import transbit.tbits.config.SysConfig;
import transbit.tbits.config.WebConfig;
import transbit.tbits.domain.BAMenu;
import transbit.tbits.domain.BusinessArea;
import transbit.tbits.domain.Field;
import transbit.tbits.domain.FieldDescriptor;
import transbit.tbits.domain.Permission;
import transbit.tbits.domain.RolePermission;
import transbit.tbits.domain.Type;
import transbit.tbits.domain.User;
import transbit.tbits.exception.DSQLParseException;
import transbit.tbits.exception.TBitsException;
import transbit.tbits.search.MultiSearcher;
import transbit.tbits.search.ParseEntry;
import transbit.tbits.search.Result;
import transbit.tbits.search.SearchConstants;
import transbit.tbits.search.SeverityImages;
import transbit.tbits.search.SearchConstants.Connective;
import transbit.tbits.search.Searcher;

//Imports from current package.
import transbit.tbits.webapps.SearchRenderer;

import static transbit.tbits.Helper.TBitsConstants.ASC_ORDER;

//Sort order.
import static transbit.tbits.Helper.TBitsConstants.DESC_ORDER;
import static transbit.tbits.Helper.TBitsConstants.FILTER_ASSIGNEE;

//My Requests filter.
import static transbit.tbits.Helper.TBitsConstants.FILTER_LOGGER;
import static transbit.tbits.Helper.TBitsConstants.FILTER_PRIMARY_ASSIGNEE;
import static transbit.tbits.Helper.TBitsConstants.FILTER_SUBSCRIBER;

//Formats.
import static transbit.tbits.Helper.TBitsConstants.OutputFormat;

//Static imports.
//Package name.
import static transbit.tbits.Helper.TBitsConstants.PKG_WEBAPPS;
import static transbit.tbits.Helper.TBitsPropEnum.KEY_DOMAIN;

//Data types of fields in TBits.
import static transbit.tbits.domain.DataType.BOOLEAN;
import static transbit.tbits.domain.DataType.DATE;
import static transbit.tbits.domain.DataType.DATETIME;
import static transbit.tbits.domain.DataType.INT;
import static transbit.tbits.domain.DataType.USERTYPE;
import static transbit.tbits.domain.DataType.REAL;
import static transbit.tbits.domain.DataType.STRING;
import static transbit.tbits.domain.DataType.TEXT;
import static transbit.tbits.domain.DataType.TIME;
import static transbit.tbits.domain.DataType.TYPE;
import static transbit.tbits.domain.DataType.ATTACHMENTS;
import static transbit.tbits.search.SearchConstants.ADVANCED_VIEW;
import static transbit.tbits.search.SearchConstants.ALL_AREAS_VIEW;
import static transbit.tbits.search.SearchConstants.ANCHOR;

//Connectives in DSQL.
import static transbit.tbits.search.SearchConstants.GROUP_ACTION;
import static transbit.tbits.search.SearchConstants.HIDE_RESULT;

//Search Constants
import static transbit.tbits.search.SearchConstants.JAVASCRIPT;
import static transbit.tbits.search.SearchConstants.LINK_SUBJECT;
import static transbit.tbits.search.SearchConstants.MOUSEOVER;
import static transbit.tbits.search.SearchConstants.MY_REQUESTS_VIEW;
import static transbit.tbits.search.SearchConstants.NORMAL_VIEW;
import static transbit.tbits.search.SearchConstants.NO_FORMATTING;
import static transbit.tbits.search.SearchConstants.NO_HIERARCHY;

//Rendering types.
import static transbit.tbits.search.SearchConstants.RenderType;
import static transbit.tbits.search.SearchConstants.ResultType;

//Search views.
//import static transbit.tbits.search.SearchConstants.SIMPLE_VIEW;
import static transbit.tbits.search.SearchConstants.TEXT_SEVERITY;
import static transbit.tbits.search.SearchConstants.TREE_RESULT;
//import static transbit.tbits.search.SearchConstants.HOME_VIEW;
//~--- JDK imports ------------------------------------------------------------

//Java imports.
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;

import java.net.URLDecoder;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.IllegalFormatConversionException;
import java.util.IllegalFormatException;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TimeZone;

import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.swing.text.html.HTML;

//~--- classes ----------------------------------------------------------------

/**
 * This servlet accomplishes the search requests in TBits.
 *
 * @author  Vaibhav.
 * @version $Id: $
 */
public class HtmlSearch extends HttpServlet {

    // Application logger.
    public static TBitsLogger LOG = TBitsLogger.getLogger(PKG_WEBAPPS);

    // Name of the html interface that renders the main page.
    public static final String HTML_FILE = "web/tbits-search.htm";

    // Name of the html interface that renders the search results.
    public static final String RESULTS_FILE = "web/tbits-search-results.htm";

    // Name of the html interface that renders the search results.
    public static final String RESULTS_EXCEL_FILE = "web/tbits-search-results-excel.htm";

    // Name of the html interface that renders the my-request results.
    public static final String MY_REQUESTS_RESULTS = "web/tbits-my-requests-results.htm";

    // Name of the html interface that renders the all areas results.
    public static final String ALL_AREAS_RESULTS = "web/tbits-allareas-results.htm";
    
    // Name of the html interface that renders the all areas view.
    public static final String ALL_AREAS_FILE = "web/tbits-allareas.htm";

    // Enum to identify the request type.
    private static final int     SEARCH_REQUEST   = 1;
    private static final int     DISPLAY_REQUEST  = 2;
    static final boolean         SORT_LINK        = true;
    static final boolean         SORT_ICON        = true;
    static final boolean         NO_SORT_LINK     = false;
    static final boolean         NO_SORT_ICON     = false;
    private static final boolean MATCHED_RESULT   = true;
    private static final int     LEAF             = 256;
    private static final int     LAST_LEAF        = 512;
    private static final boolean UNMATCHED_RESULT = false;
    private static final int     READ_REQUESTS    = 1;

    // Path where images should be referred to.
    private static final String     IMAGE_STATIC_PATH = "/web/images/";
    private static final int        ALL_REQUESTS    = 0;
    private static final int        UNREAD_REQUESTS = 2;
    public static ArrayList<String> ourAllAreasResultsTagList;
    public static ArrayList<String> ourAllAreasTagList;

    // Current Domain.
    public static String            ourCurrentSite;
    public static ArrayList<String> ourMyRequestsResultsTagList;
    public static ArrayList<String> ourResultTagList;
     
    /*
     * These contain the list of tags in the html interfaces that need to be
     * replaced at the end of processing the request.
     */
    public static ArrayList<String> ourSearchTagList;
    public static ArrayList<String>  tBitsSearchJSs ; 
    public static ArrayList<String>  YUISearchJSs ;
    //~--- static initializers ------------------------------------------------

    static {
        loadStaticData();
    }
//    public void init()
//    {
//    	loadStaticData();    	
//    }
    //~--- methods ------------------------------------------------------------

    /**
     *
     * @param aRootKey
     * @param aParTable
     * @param aResultsTable
     * @param aFinalList
     * @param aFinalTable
     * @param aIdList
     * @param aDefaultFlags
     * @param aIndentation
     */
    private static void addCompleteTreeBelow(String aRootKey, Hashtable<String, ArrayList<String>> aParTable, Hashtable<String, Result> aResultsTable, ArrayList<Result> aFinalList,
            Hashtable<String, Boolean> aFinalTable, ArrayList<String> aIdList, int aDefaultFlags, int aIndentation) {

        /*
         * 1. First add this to the finalList and finalTable.
         *
         * 2. Get the list of children for the RootKey from the parTable.
         *    If the children list is null/empty, add this rootKey to the
         *    final list and return.
         * 3.
         */
        Result result = aResultsTable.get(aRootKey);

        if (result == null) {

            // We can hardly do anything here. RETURN!!!!
            LOG.info("No result corresponding to the key: " + aRootKey);

            return;
        }

        int flags = result.getFlags();

        flags = flags | aDefaultFlags;
        result.setFlags(flags);

        ArrayList<String> children   = aParTable.get(aRootKey);
        ResultType        resultType = getResultType(result.getParentId(), children);

        result.setResultType(resultType);
        result.setIndentation(aIndentation);
        aFinalList.add(result);
        aFinalTable.put(aRootKey, true);

        if (aIndentation != 0) {
            aIdList.remove(aRootKey);
        }

        if ((children == null) || (children.size() == 0)) {
            LOG.info("No children for " + aRootKey);

            return;
        }

        // Increment the indentation by 1 for the children.
        aIndentation = aIndentation + 1;

        int childrenCount = children.size();

        for (int i = 0; i < childrenCount; i++) {
            String childKey    = children.get(i);
            int    indPosition = (int) Math.round(Math.pow(2, aIndentation));

            if (i != childrenCount - 1) {
                aDefaultFlags = aDefaultFlags | indPosition;
            } else {
                if ((aDefaultFlags & indPosition) != 0) {
                    aDefaultFlags = aDefaultFlags ^ indPosition;
                }
            }

            Result child = aResultsTable.get(childKey);

            if (child == null) {
                LOG.info("No children for " + childKey);

                continue;
            }

            int               systemId      = child.getSystemId();
            int               requestId     = child.getRequestId();
            int               parentId      = child.getParentId();
            ArrayList<String> thisChildList = aParTable.get(childKey);

            resultType = getResultType(parentId, thisChildList);
            child.setResultType(resultType);

            switch (resultType) {
            case RESULT_NORMAL :

                // Cannot be a normal result.
                break;

            case RESULT_ROOT :

                // Cannot be a root result.
                break;

            case RESULT_LEAF :

                /*
                 * Set the indentation and add the leaf to the final list and
                 * final table.
                 */
                child.setIndentation(aIndentation);
                flags = child.getFlags();
                flags = flags | LEAF | aDefaultFlags;
                child.setFlags(flags);
                aFinalList.add(child);
                aFinalTable.put(childKey, true);

                if (aIndentation != 0) {
                    aIdList.remove(childKey);
                }

                break;

            case RESULT_PARENT :

                // add the tree below this parent.
                addCompleteTreeBelow(childKey, aParTable, aResultsTable, aFinalList, aFinalTable, aIdList, aDefaultFlags, aIndentation);

                break;
            }    // End Switch
        }        // End For

        if (resultType == ResultType.RESULT_LEAF) {
            int    size       = aFinalList.size();
            Result lastInHier = aFinalList.get(size - 1);

            flags = lastInHier.getFlags();
            flags = flags | LAST_LEAF;
            lastInHier.setFlags(flags);
            aFinalList.set(size - 1, lastInHier);
        }

        return;
    }

    /**
     * This method checks if the user is authorized to search in the specified
     * business area.
     *
     * @param  aBA    Business Area.
     * @param  aUser  User.
     *
     * @return Permission table for this user in this BA.
     */
    private Hashtable<String, Integer> authorizeUser(BusinessArea aBA, User aUser) throws TBitsException, DatabaseException {
        int                        userId      = aUser.getUserId();
        int                        systemId    = aBA.getSystemId();
        String                     displayName = aBA.getDisplayName();
        Hashtable<String, Integer> permTable   = RolePermission.getPermissionsBySystemIdAndUserId(systemId, userId);

        if (permTable == null) {
            permTable = new Hashtable<String, Integer>();
        }

        // Check if the user have view permission on this BA to search.
        Integer tmp = permTable.get(Field.BUSINESS_AREA);

        if (tmp != null) {
            int permission = tmp.intValue();

            if ((permission & Permission.VIEW) == 0) {
                throw new TBitsException(Messages.getMessage("SEARCH_NO_PERMISSION", displayName));
            }
        } else {
            throw new TBitsException(Messages.getMessage("SEARCH_NO_PERMISSION", displayName));
        }

        /*
         * Check if this is a private BA and the user has permissions to view
         * this private BA.
         */
        if(!hasPermission(aBA, permTable))
        	throw new TBitsException(Messages.getMessage("INVALID_BUSINESS_AREA", displayName));
        
        return permTable;
    }

    boolean hasPermission(BusinessArea aBA, Hashtable<String, Integer> permTable)
    {
    	if (aBA.getIsPrivate() == true) {
            Integer tmp = permTable.get(Field.IS_PRIVATE);

            if (tmp != null) {
                int permission = tmp.intValue();

                if ((permission & Permission.VIEW) == 0) {
                    return false;
                }
            } else {
                return false;
            }
        }
    	return true;
    }
    /**
     * This method checks this query contains any shortcut information.
     *
     * @param aQuery DSQL Query.
     * @return Table of query, shortcut name and its default property.
     */
    private Hashtable<String, String> checkForShortcut(String aQuery) {
        Hashtable<String, String> qtable    = new Hashtable<String, String>();
        String                    scname    = "";
        String                    dsqlQuery = "";
        String                    isdefault = "false";

        if (aQuery == null) {
            qtable.put("QUERY", dsqlQuery);
            qtable.put("SHORTCUT", scname);
            qtable.put("DEFAULT", isdefault);

            return qtable;
        }

        aQuery = aQuery.trim();

        // Check if the query starts with @
        if (aQuery.startsWith("@") == true) {

            // Get the index of "," which marks the end of shortcut name.
            int index = aQuery.indexOf(",");

            if (index > 0) {
                scname    = aQuery.substring(1, index);
                dsqlQuery = aQuery.substring(index + 1).trim();
            } else {
                scname    = aQuery.substring(1);
                dsqlQuery = "";
            }

            /*
             * Check if the scname still starts with @, which indicates that
             * the user wants to save this shortcut as the default one.
             */
            if (scname.startsWith("@") == true) {
                scname    = scname.substring(1);
                isdefault = "true";
            }
        } else {
            dsqlQuery = aQuery;
        }

        qtable.put("QUERY", dsqlQuery);
        qtable.put("SHORTCUT", scname);
        qtable.put("DEFAULT", isdefault);

        return qtable;
    }

    /**
     * This method returns true if there exists a request parameter with the
     * given name in the specified request object.
     *
     * @param aRequest  HttpServletRequest Object.
     * @param aParam    Name of the request parameter.
     *
     * @return True if the Request object contains the given param.
     */
    private boolean checkIfPresent(HttpServletRequest aRequest, String aParam) {
        String paramValue = aRequest.getParameter(aParam);

        if (paramValue == null) {
            return false;
        }

        return true;
    }

    /**
     * This method renders the type fields for the given business area.
     *
     * @param aSystemId   Business Area Id.
     * @param aIsPrivate  Permission on Private field.
     * @param aTagTable   Table that holds the tag and its value.
     *
     */
    private void defaultRenderer(int aSystemId, boolean aIsVEEnabled, boolean aIsPrivate, Hashtable<String, String> aTagTable, HttpServletRequest aRequest) throws DatabaseException {
        boolean aAll        = false;
        String  cookieValue = "";

        cookieValue = getCookieValue(aRequest, Field.CATEGORY);

        if ((cookieValue != null) && cookieValue.trim().equalsIgnoreCase("all")) {
            aAll = true;
        }

        aTagTable.put("category_id_list", getTypeList(aSystemId, Field.CATEGORY, aIsPrivate, aAll));
        aAll        = false;
        cookieValue = getCookieValue(aRequest, Field.STATUS);

        if ((cookieValue != null) && cookieValue.trim().equalsIgnoreCase("all")) {
            aAll = true;
        }

        aTagTable.put("status_id_list", getTypeList(aSystemId, Field.STATUS, aIsPrivate, aAll));
        aAll        = false;
        cookieValue = getCookieValue(aRequest, Field.SEVERITY);

        if ((cookieValue != null) && cookieValue.trim().equalsIgnoreCase("all")) {
            aAll = true;
        }

        aTagTable.put("severity_id_list", getTypeList(aSystemId, Field.SEVERITY, aIsPrivate, aAll));
        aAll        = false;
        cookieValue = getCookieValue(aRequest, Field.REQUEST_TYPE);

        if ((cookieValue != null) && cookieValue.trim().equalsIgnoreCase("all")) {
            aAll = true;
        }

        aTagTable.put("request_type_id_list", getTypeList(aSystemId, Field.REQUEST_TYPE, aIsPrivate, aAll));
        aAll        = false;
        cookieValue = getCookieValue(aRequest, Field.OFFICE);

        if ((cookieValue != null) && cookieValue.trim().equalsIgnoreCase("all")) {
            aAll = true;
        }

        aTagTable.put("office_id_list", getTypeList(aSystemId, Field.OFFICE, aIsPrivate, aAll));
        cookieValue = Utilities.htmlEncode(getCookieValue(aRequest, Field.LOGGER));
        cookieValue = (cookieValue == null)
                      ? ""
                      : cookieValue.trim();
        aTagTable.put("logger_ids", cookieValue);
        cookieValue = Utilities.htmlEncode(getCookieValue(aRequest, Field.ASSIGNEE));
        cookieValue = (cookieValue == null)
                      ? ""
                      : cookieValue.trim();
        aTagTable.put("assignee_ids", cookieValue);
        cookieValue = Utilities.htmlEncode(getCookieValue(aRequest, Field.SUBSCRIBER));
        cookieValue = (cookieValue == null)
                      ? ""
                      : cookieValue.trim();
        aTagTable.put("subscriber_ids", cookieValue);
        cookieValue = Utilities.htmlEncode(getCookieValue(aRequest, Field.USER));
        cookieValue = (cookieValue == null)
                      ? ""
                      : cookieValue.trim();
        aTagTable.put("user_id", cookieValue);
        cookieValue = Utilities.htmlEncode(getCookieValue(aRequest, "cbLast_checked"));

        if ((cookieValue == null) || cookieValue.trim().equals("") || cookieValue.trim().equalsIgnoreCase("checked")) {
            cookieValue = "CHECKED";
        } else if (cookieValue.trim().equals("unchecked")) {
            cookieValue = "";
        } else {
            cookieValue = "CHECKED";
        }

        aTagTable.put("cbLast_checked", cookieValue);
        aTagTable.put("readFilter", getReadUnreadFilter(ALL_REQUESTS, aIsVEEnabled));
        cookieValue = Utilities.htmlEncode(getCookieValue(aRequest, "normalDesc"));
        cookieValue = (cookieValue == null)
                      ? ""
                      : cookieValue.trim();
        aTagTable.put("normalDesc", cookieValue);

        return;
    }

    /**
     * Method to service http-get requests.
     *
     * @param  aRequest  HttpServletRequest object.
     * @param  aResponse HttpServletResponse object.
     *
     * @exception ServletException
     * @exception IOException
     */
    public void doGet(HttpServletRequest aRequest, HttpServletResponse aResponse) throws ServletException, IOException {
    	aResponse.setContentType("text/html; charset=UTF-8");
    	aResponse.setCharacterEncoding("UTF-8");
    	aRequest.setCharacterEncoding("UTF-8");
    	
    	HttpSession aSession = aRequest.getSession(true);

        Utilities.registerMDCParams(aRequest);
        String IMAGE_PATH = WebUtil.getNearestPath(aRequest, IMAGE_STATIC_PATH);
        try {
            long start = System.currentTimeMillis();

            handleRequest(aRequest, aResponse);

            long end = System.currentTimeMillis();

            LOG.info("Total time taken to search: " + (end - start) + " ms");
        } catch (FileNotFoundException fnfe) {
            LOG.warn("",(fnfe));

            TBitsException de = new TBitsException(Messages.getMessage("HTML_INTERFACE_MISSING"));

            aSession.setAttribute("ExceptionObject", de);
            aResponse.sendRedirect(WebUtil.getServletPath(aRequest, "/error"));

            return;
        }
        /*catch (TBitsException te){
        	try{
        		User user = User.lookupAllByUserLogin(aRequest.getRemoteUser());
        		WebConfig aconfig = user.getWebConfigObject();
        		String sysprefix = getDefaultBA(user);
        		//find out BA from myDescription
        		String strBA = te.getDescription();
        		
        		if (strBA.indexOf(":") > 0) {
                    strBA = strBA.substring(strBA.indexOf(":") + 1);
                    strBA = strBA.substring(strBA.indexOf(":") + 1);
                    strBA.replace('.','\0');
                    
                }
        		
        		BusinessArea ba ;
        		ArrayList<BusinessArea> listBA = BusinessArea.getAllBusinessAreas();
        		for( BusinessArea BA : listBA)
        		{
        			if(BA.getDisplayName()==strBA)
        			{
        				ba = BA;
        				break;
        			}
        		}
        		if(sysprefix == ba.getSystemPrefix())
        		{
        			LOG.warn("",(te));
       	            aSession.setAttribute("ExceptionObject", te);
       	            aResponse.sendRedirect(WebUtil.getServletPath("/error1"));
        		}
        		else
        		{
        			 Exception de = te;
        			 LOG.warn("",(de));
        	         aSession.setAttribute("ExceptionObject", de);
        	         aResponse.sendRedirect(WebUtil.getServletPath("/error"));
        		}
        	}
        	return;
        }*/
        
           catch (DatabaseException de) {
            LOG.severe("",(de));

            TBitsException e = new TBitsException(de.toString());

            aSession.setAttribute("ExceptionObject", e);
            aResponse.sendRedirect(WebUtil.getServletPath(aRequest, "/error"));

            return;
        } 
        

        	catch (Exception de) {
            LOG.warn("",(de));
            aSession.setAttribute("ExceptionObject", de);
            
            aResponse.sendRedirect(WebUtil.getServletPath(aRequest, "/error"));

            return;
        } finally {
            Utilities.clearMDCParams();
        }
    }

    /**
     * Method to service http-post requests.
     *
     * @param  aRequest  HttpServletRequest object.
     * @param  aResponse HttpServletResponse object.
     *
     * @exception ServletException
     * @exception IOException
     */
    public void doPost(HttpServletRequest aRequest, HttpServletResponse aResponse) throws ServletException, IOException {
        doGet(aRequest, aResponse);
    }

    /**
     * This method executes the query on all business areas.
     *
     * @param aUser   User
     * @param aQuery  DSQL Query
     *
     */
    private String executeAllAreaQuery(HttpServletRequest aRequest, User aUser, String aQuery, TimeZone aZone, Hashtable<String, Integer> aPermTable) throws DatabaseException {
        int          userId  = aUser.getUserId();
        WebConfig    config  = aUser.getWebConfigObject();
        StringBuffer buffer  = new StringBuffer();
        HttpSession  session = aRequest.getSession();

        // Get the rendering type.
        RenderType            renderType = getRenderType(aRequest, config);
        long                  dbTime     = 0;
        long                  inTime     = 0;
        long                  start      = 0,
                              end        = 0;
        MultiSearcher         ms         = new MultiSearcher(aUser, aQuery);
        ArrayList<String>     prefixList;
        ArrayList<ParseEntry> parseTable;
        int                   aFlags = JAVASCRIPT | ANCHOR | LINK_SUBJECT | MOUSEOVER;

        try {
            ms.parse();
            prefixList = ms.getPrefixList();
            parseTable = ms.getParseTable();

            ArrayList<BusinessArea> baList = getBAList(prefixList);

            for (BusinessArea ba : baList) {
                String prefix  = ba.getSystemPrefix();
                String uprefix = prefix.toUpperCase();

                if (ba == null) {
                    continue;
                }

                int               systemId      = ba.getSystemId();
                String            sysPrefix     = ba.getSystemPrefix();
                BAConfig          baConfig      = config.getBAConfig(prefix);
                ArrayList<String> displayHeader = baConfig.getDisplayHeader();
                String            sortField     = getSortField(aRequest, sysPrefix, ALL_AREAS_VIEW, baConfig);
                int               sortOrder     = getSortOrder(aRequest, systemId, sysPrefix, sortField, ALL_AREAS_VIEW);

                start = System.currentTimeMillis();

                Searcher searcher = new Searcher(systemId, userId, parseTable);

                searcher.setDisplayHeader(displayHeader);
                searcher.setSortField(sortField);
                searcher.setSortOrder(sortOrder);
                searcher.setPermissionTable(aPermTable);

                int rowsPerPage = config.getRowsPerPage();

                searcher.setMaximumPageSize(rowsPerPage);

                boolean success = true;

                try {
                    searcher.search();
                } catch (Exception dpe) {
                    LOG.warn("",(dpe));
                    success = false;
                }

                end    = System.currentTimeMillis();
                dbTime = dbTime + (end - start);

                if (success == true) {

                    // Execution was successful.
                    // Get the output params of Searcher.
                    Hashtable<String, Integer> permTable   = searcher.getPermissionTable();
                    Hashtable<String, Field>   fieldsTable = searcher.getFieldsTable();
                    int                        resultsSize = searcher.getRetrievedResultCount();
                    int                        resultCount = searcher.getTotalResultCount();

                    displayHeader = searcher.getDisplayHeader();
                    sortField     = searcher.getSortField();
                    sortOrder     = searcher.getSortOrder();

                    if (resultsSize == 0) {
                        continue;
                    }

                    StringBuffer       html           = new StringBuffer();
                    ArrayList<Integer> sessionIdValue = renderResults(ba, config, searcher, aZone, renderType, html, aFlags, OutputFormat.HTML, aRequest);
                    String             colGroup       = getColumnGroup(ba, displayHeader, aFlags);
                    String             header         = getHeader(ba, searcher, SORT_LINK, SORT_ICON, aFlags, aRequest.getContextPath());
                    String             sessionIdName  = sysPrefix + "_sessionId";
                    String             sessionIdKey   = "AASession_" + sysPrefix + "_" + System.currentTimeMillis();

                    session.setAttribute(sessionIdKey, sessionIdValue);

                    String resultsTableName = sysPrefix + "_searchResults";

                    buffer.append("<TR><TD width=\"100%\">").append("<INPUT type='hidden' name='").append(sessionIdName).append("' id='").append(sessionIdName).append("' value='").append(
                        sessionIdKey).append("' />").append("<input type='hidden' ").append("name='aa_sortPrefix' id='aa_sortPrefix' />").append("<input type='hidden' ").append(
                        "name='aa_sortField' id='aa_sortField' />").append("<input type='hidden' ").append("name='aa_sortOrder' id='aa_sortOrder' />").append(
                        "<TABLE class='results' cellspacing='0' ").append("cellpadding='0' width='100%' border='0' ").append("name='").append(resultsTableName).append("' id='").append(
                        resultsTableName).append("'>").append("\n\t<TR>\n\t\t").append("<TH class='rs' align='left' colspan='").append(displayHeader.size() + 1).append("'>").append(
                        ba.getDisplayName()).append(" [").append(getEmailLink(ba.getEmail())).append("] ").append(" - ").append(getResultComment(systemId, resultsSize, resultCount)).append(
                        "\n\t\t</TH>\n\t</TR>").append(colGroup).append(header).append(html.toString()).append("\n</TABLE>").append("</TR></TD>");
                } else {}

                start  = System.currentTimeMillis();
                inTime = inTime + (start - end);
            }

            buffer.append("<INPUT type='hidden' name='databaseTime' ").append("id='databaseTime' value='").append(dbTime).append("' />").append("<INPUT type='hidden' name='interfaceTime' ").append(
                "id='interfaceTime' value='").append(inTime).append("' />");
        } catch (DSQLParseException dsqle) {
            LOG.warn("",(dsqle));
        }

        return buffer.toString();
    }

    /**
     * This method executes the given query and returns the results formatted
     * as specified by the format parameters.
     *
     */
    private Searcher executeQuery(HttpServletRequest aRequest, BusinessArea aBA, User aUser, String aQuery, String aFilter, OutputFormat aFormat, int aSearchView, RenderType aRenderType,
                                  boolean aIsListAll, Hashtable<String, String> aTagTable, Hashtable<String, Integer> aPermTable, HttpSession aSession)
            throws FileNotFoundException, IOException {

        // Get the user details that are frequently used during the process.
        int       userId      = aUser.getUserId();
        String    userLogin   = aUser.getUserLogin();
        WebConfig userConfig  = aUser.getWebConfigObject();
        int       systemId    = aBA.getSystemId();
        String    displayName = aBA.getDisplayName();
        String    sysPrefix   = aBA.getSystemPrefix();
        BAConfig  baconfig    = userConfig.getBAConfig(sysPrefix);

        // Check if the page number is present in the request object.
        int    pageNumber    = 1;
        String strPageNumber = aRequest.getParameter("pageNumber");

        if ((strPageNumber != null) &&!strPageNumber.trim().equals("")) {
            try {
                pageNumber = Integer.parseInt(strPageNumber);
            } catch (Exception e) {
                LOG.info("",(e));
            }
        }

        String sortField = getSortField(aRequest, sysPrefix, aSearchView, baconfig);
        int    sortOrder = getSortOrder(aRequest, systemId, sysPrefix, sortField, aSearchView);

        // Times to be noted.
        long startDatabase = 0;
        long endDatabase   = 0;

        // Status of query execution.
        boolean                   success = true;
        Hashtable<String, String> qtable  = checkForShortcut(aQuery);

        aQuery = qtable.get("QUERY");

        /*
         * Exception object that holds the exception, if any, occurred during
         * query execution.
         */
        Exception exception = null;
        String    desc      = aTagTable.get(Field.DESCRIPTION);

        desc = (desc == null)
               ? ""
               : desc.trim();
        aTagTable.put("searchText", desc);

        // Buffers to hold the results, exception messages etc.,
        StringBuffer          results    = new StringBuffer();
        StringBuffer          errors     = new StringBuffer();
        StringBuffer          comment    = new StringBuffer();
        ArrayList<ParseEntry> parseTable = null;

        // Create a searcher object and execute the query and get the results.
        Searcher searcher = new Searcher(systemId, userId, aQuery);

        searcher.setDisplayHeader(baconfig.getDisplayHeader());
        searcher.setSortField(sortField);
        searcher.setSortOrder(sortOrder);
        searcher.setPermissionTable(aPermTable);
        searcher.setSortField(sortField);
        searcher.setSortOrder(sortOrder);
        searcher.setPageNumber(pageNumber);

        int rowsPerPage = userConfig.getRowsPerPage();

        searcher.setCurrentPageSize(rowsPerPage);
        searcher.setIsListAll(aIsListAll);

        /*
         * If the output format chosen is not HTML_FORMAT, then lets bring
         * atmost 1000 rows.
         */
        if (aFormat != OutputFormat.HTML) {
            searcher.setMaximumPageSize(1000);
            searcher.setCurrentPageSize(1000);
            searcher.setPageNumber(1);
        }

        try {
            startDatabase = System.currentTimeMillis();
            searcher.search();
            endDatabase = System.currentTimeMillis();
        } catch (Exception dpe) {
            exception = new TBitsException( "Message : Sorry your search cannot be continued, because of database exception.");
            LOG.warn("",(dpe));
            success = false;
        }

        long databaseTime = endDatabase - startDatabase;

        aTagTable.put("databaseTime", Long.toString(databaseTime));

        if (success == true) {
            try {

                // Check if this should be stored as a shortcut.
                String scname = qtable.get("SHORTCUT");

                if ((scname != null) && (scname.trim().equals("") == false)) {
                    String isdefault = qtable.get("DEFAULT");

                    Shortcut.saveShortcut(aUser, aBA, sysPrefix, scname, aQuery, desc, aFilter, aSearchView, Boolean.parseBoolean(isdefault), false, aIsListAll, false);    // User level by default
                }
            } catch (Exception e) {
                exception = new TBitsException("Your shortcut could not be saved.");
            }

            return searcher;
        } else {

            // In case of an exception
            aTagTable.put("rcDisplay", " NONE");
            aTagTable.put("pageNumberDisplay", " NONE ");
            aTagTable.put("groupActionDisplay", " NONE ");
            aTagTable.put("miscFuncDisplay", " NONE ");
            aTagTable.put("excepDisplay", "  ");

            // Exception occurred during query execution
            String eMessage = exception.toString();
            int    index    = eMessage.indexOf(":");

            if (index > 0) {
                eMessage = eMessage.substring(index + 1).trim();
            }

            errors.append(eMessage);
            aTagTable.put("exception", errors.toString().replaceAll("\n", "\n<BR>").replaceAll("\t", "&nbsp;&nbsp;&nbsp;&nbsp;").replaceAll(" ", "&nbsp;"));

            return null;
        }
    }

    /**
     * This method renders the results without considering their parent-child
     * relationships.
     *
     * @param aBA          Business Area.
     * @param aConfig      User Config.
     * @param aSearcher    Searcher object.
     * @param aZone        User Preferred Zone.
     * @param aHtml        Out parameter that holds the results.
     * @param aReqIdList   List of request ids.
     *
     */
    public static void flatRenderer(BusinessArea aBA, WebConfig aConfig, Searcher aSearcher, TimeZone aZone, StringBuffer aHtml, ArrayList<Integer> aReqIdList, int aFlags, OutputFormat aFormat, String aContextPath) {

        /*
         * Flat rendering does not consider any parent-child relationships.
         * It just renders the results that matched the criteria.
         */
        Hashtable<String, Field>  fieldsTable  = aSearcher.getFieldsTable();
        ArrayList<String>         header       = aSearcher.getDisplayHeader();
        ArrayList<String>         results      = aSearcher.getResultIdList();
        Hashtable<String, Result> resultsTable = aSearcher.getRequestsTable();
        int                       treeId       = getTreeId(aSearcher.getParseTable());

        for (String key : results) {
            Result result    = resultsTable.get(key);
            int    requestId = result.getRequestId();

            /*
             * In this kind of rendering, all requests are treated as normal.
             */
            result.setResultType(ResultType.RESULT_NORMAL);

            /*
             * Add this to the RequestIdList that will be used by View-Request
             * page for navigation.
             */
            aReqIdList.add(requestId);
            result.setIndentation(0);

            if (requestId == treeId) {
                aFlags = aFlags | TREE_RESULT;
            } else {
                if ((aFlags & TREE_RESULT) != 0) {
                    aFlags = aFlags ^ TREE_RESULT;
                }
            }

            // Html Serialize this as a normal request.
            String html = serialize(aBA, result, aConfig, fieldsTable, header, aZone, MATCHED_RESULT, false,    // Not an incomplete tree by default
                                    aFlags, aFormat, aContextPath);

            aHtml.append(html);
        }

        return;
    }

    /**
     * This method services the requests to search in all BAs.
     *
     *
     * @param aRequest  HttpServletRequest object.
     * @param aResponse HttpServletResponse object.
     * @exception ServletException
     * @exception IOException
     * @exception DESTBitsExceptionexception DatabaseException
     */
    public void handleAllAreasRequest(HttpServletRequest aRequest, HttpServletResponse aResponse, BusinessArea aBA, User aUser, TimeZone aZone, Hashtable<String, Integer> aPermTable)
            throws ServletException, IOException, TBitsException, DatabaseException {

        // PrintStream.
        PrintWriter out = aResponse.getWriter();

        aResponse.setContentType("text/html");

        // Get the session object.
        HttpSession aSession = aRequest.getSession(true);

        // Get the User Config.
        WebConfig                 userConfig = aUser.getWebConfigObject();
        String                    sysPrefix  = aBA.getSystemPrefix();
        BAConfig                  baconfig   = userConfig.getBAConfig(sysPrefix);
        Hashtable<String, String> tagTable   = new Hashtable<String, String>();

        tagTable.put("searchShortcuts", ShortcutHandler.renderShortcuts(aRequest, aUser, aBA));

        String query = getQuery(aRequest);

        if ((query != null) || (query.trim().equals("") != true)) {
            String searchResults = executeAllAreaQuery(aRequest, aUser, query, aZone, aPermTable);

            tagTable.put("searchResults", searchResults);
        } else {
            StringBuffer buffer = new StringBuffer();

            buffer.append("<SPAN class='rcomment'>").append("No records found for the specified search criteria.").append("</SPAN>");
            tagTable.put("searchResults", buffer.toString());
        }

        DTagReplacer hp = new DTagReplacer(ALL_AREAS_RESULTS);

        SearchRenderer.replaceTags(hp, tagTable, ourAllAreasResultsTagList);
        out.println(hp.parse(aBA.getSystemId()));

        return;
    }

    /**
     * This method displays my-requests page.
     *
     *
     * @param aRequest  HttpServletRequest object.
     * @param aCurrentBA
     * @param aUser
     * @param aZone
     * @param aTagTable
     * @exception ServletException
     * @exception IOException
     * @exception TBitsExceptioneption DatabaseException
     */
    public void handleMyRequests(HttpServletRequest aRequest, BusinessArea aCurrentBA, User aUser, TimeZone aZone, Hashtable<String, String> aTagTable, Hashtable<String, Integer> aPermTable)
            throws ServletException, IOException, TBitsException, DatabaseException {
        int         userId     = aUser.getUserId();
        String      userLogin  = aUser.getUserLogin();
        WebConfig   webConfig  = aUser.getWebConfigObject();
        HttpSession session    = aRequest.getSession();
        MyRequests  myRequests = new MyRequests();

        // Get the rendering type.
        RenderType renderType = getRenderType(aRequest, webConfig);

        // Get the filter on my-requests
        int               filter         = myRequests.getMyRequestsFilter(aRequest, webConfig.getFilter());
        ArrayList<String> userTypeIdList = myRequests.getRoleFilter(filter);
        boolean           primary        = myRequests.getIsPrimary();

        // Set the view to my-requests.
        getFilterList(aTagTable, filter);
        setSearchView(MY_REQUESTS_VIEW, aTagTable);

        /*
         * Get the list of business areas where the user is present in the
         * above contextual roles.
         */
        ArrayList<String> prefixList = BusinessArea.getUserBAList(userId, Utilities.arrayListToString(userTypeIdList), primary);

        // Filter out the BAs that need not be shown by default.
        prefixList = myRequests.filterBAsToHide(webConfig, prefixList);

        // Get the corresponding Business Areas.
        ArrayList<BusinessArea> baList = new ArrayList<BusinessArea>();

        for (String sysPrefix : prefixList) {
            BusinessArea ba = BusinessArea.lookupBySystemPrefix(sysPrefix);

            baList.add(ba);
        }

        BusinessArea.setSortField(BusinessArea.DISPLAYNAME);
        baList = BusinessArea.sort(baList);

        /*
         * For each BA get the following and invoke the Searcher.
         *    - BAConfig object
         *    - Display Header
         *    - Sort field.
         *    - Sort Order.
         */
        StringBuffer buffer = new StringBuffer();

        if (baList.size() == 0) {
            buffer.append("<TR><TD><SPAN class='sx'>").append(getComment(filter)).append("</SPAN></TR></TD>");
        } else {
            long   dbTime            = 0,
                   inTime            = 0,
                   start             = 0,
                   end               = 0;
            int    aFlags            = JAVASCRIPT | ANCHOR | LINK_SUBJECT | MOUSEOVER;
            String myRequestsContent = myRequests.getMyRequests(aRequest, userId, baList, webConfig, userTypeIdList, primary, aZone, aFlags);

            if ((myRequestsContent == null) || myRequestsContent.trim().equals("")) {
                buffer.append("<SPAN class='sx'>").append(getComment(filter)).append("</SPAN>");
            } else {
                buffer.append(myRequestsContent);
            }

            dbTime = myRequests.getDatabaseTime();
            inTime = myRequests.getRenderTime();
            buffer.append("<INPUT type='hidden' name='databaseTime' ").append("id='databaseTime' value='").append(dbTime).append("' />").append("<INPUT type='hidden' name='interfaceTime' ").append(
                "id='interfaceTime' value='").append(inTime).append("' />");
        }

        Hashtable<String, String> tagTable = new Hashtable<String, String>();

        // Get the WebConfig corresponding to the current BA.
        BAConfig baconfig = webConfig.getBAConfig(aCurrentBA.getSystemPrefix());

        tagTable.put("searchShortcuts", ShortcutHandler.renderShortcuts(aRequest, aUser, aCurrentBA));
        tagTable.put("searchResults", buffer.toString());

        DTagReplacer hp = new DTagReplacer(ALL_AREAS_RESULTS);

        SearchRenderer.replaceTags(hp, tagTable, ourAllAreasResultsTagList);
        aTagTable.put("searchResultsHolder", hp.parse(aCurrentBA.getSystemId()));

        return;
    }

    /**
     * This method actually services the http requests.
     *
     *
     * @param aRequest  HttpServletRequest object.
     * @param aResponse HttpServletResponse object.
     * @exception ServletException
     * @exception IOException
     * @exception DESTBitsExceptionexception DatabaseException
     */
    public void handleRequest(HttpServletRequest aRequest, HttpServletResponse aResponse) throws ServletException, IOException, TBitsException, DatabaseException, FileNotFoundException {

        // By default, the content-type of output is HTML.
        aResponse.setContentType("text/html");

        /*
         * Get all the mandatory objects required from the Http Request object.
         *   - PrintWriter to output the HTML content.
         *   - Session object to store user options to be persisted.
         */
        PrintWriter out      = aResponse.getWriter();
        HttpSession aSession = aRequest.getSession();

        // Authenticate the user accessing TBits.
        User user = WebUtil.validateUser(aRequest);

        /*
         * Get the user details that are frequently used during the process.
         *  1. User Id
         *  2. User Login
         *  3. User's Configuration.
         */
        int       userId     = user.getUserId();
        String    userLogin  = user.getUserLogin();
        WebConfig userConfig = user.getWebConfigObject();

        /*
         * Get the client offset. ClientOffset is needed while rendering dates
         * in the browser's timezone if the user opted to do so.
         */
        int clientOffset = WebUtil.getClientOffset(aRequest, aResponse);

        if (clientOffset == -1) {

            /*
             * Redirect to the browser to get the clientOffset as a request
             * parameter.
             */
            String pathInfo = aRequest.getPathInfo();
            if (null == pathInfo) {
                pathInfo = "";
            }

            String url = "/search" + pathInfo;

            out.println(WebUtil.getRedirectionHtml(aRequest, url));

            return;
        }

        // Get the TimeZone based on the user's preferred zone.
        int      zone     = userConfig.getPreferredZone();
        TimeZone prefZone = WebUtil.getPreferredZone(zone, clientOffset);

        
        // Get the request parameters from the path info.
        Hashtable<String, Object> reqParams = parseURL(aRequest, user);

        // If we are able to come here, it means the table has BA object.
        BusinessArea ba = (BusinessArea) reqParams.get(Field.BUSINESS_AREA);

        /*
         * Get the following details of BusinessArea, needed during the process.
         *   1. System Id.
         *   2. Display Name.
         *   3. System Prefix.
         *   4. Business Area Configuration details.
         *   5. User's Configuration related to this Business Area.
         */
        int       systemId    = ba.getSystemId();
        String    displayName = ba.getDisplayName();
        String    sysPrefix   = ba.getSystemPrefix();
        SysConfig sysConfig   = ba.getSysConfigObject();
        BAConfig  baconfig    = userConfig.getBAConfig(sysPrefix);

        // Store the sys prefix in MDC for the Logger to use during mailing.
        MDC.put("SYS_PREFIX", sysPrefix);

        // Check if the user is authorized to search in this BA.
        Hashtable<String, Integer> permTable = authorizeUser(ba, user);
        boolean                    isPrivate = getIsPrivate(permTable);

        /*
         * Get the values of following parameters.
         *     01. Page Number
         *     02. Output Format
         *     03. Search View
         *     04. Sort Field
         *     05. Sort Order
         *     06. Text Filter
         *     07. Render Type
         *     08. Query
         *     09. Shortcut
         *     10. isAllAreas
         *     11. isXmlHttp
         *     12. isMyRequests
         */
        int          pageNumber   = getPageNumber(aRequest);
        OutputFormat outputFormat = getOutputFormat(aRequest);
        int          searchView   = getSearchView(aRequest, aSession, sysPrefix, userConfig);
        String       sortField    = getSortField(aRequest, sysPrefix, searchView, baconfig);
        int          sortOrder    = getSortOrder(aRequest, systemId, sysPrefix, sortField, searchView);
        String       textFilter   = getTextFilter(aRequest);
        RenderType   renderType   = getRenderType(aRequest, userConfig);
        String       query        = getQuery(aRequest);
        Shortcut     shortcut     = (Shortcut) reqParams.get("SHORTCUT");
        boolean      isAllAreas   = checkIfPresent(aRequest, "allAreas");
        boolean      isXmlHttp    = checkIfPresent(aRequest, "xmlHttp");
        boolean      isMyRequests = (reqParams.get("MY-REQUESTS") != null)
                                    ? true
                                    : false;
        boolean      isListAll    = checkIfPresent(aRequest, "listAll");

        /*
         * Check if this is an XML Http request. An XML Http request is sent
         * in three cases:
         *      1. User wanted to search in All Areas view of search page.
         *      2. User wanted to refresh the my-requests view of search page.
         *      3. User wanted to search in one of simple/normal/advanced views
         *         in search page.
         */
        if (isXmlHttp == true) {
            if (isAllAreas == true) {

                // This is a request to display the results of allArea search.
                handleAllAreasRequest(aRequest, aResponse, ba, user, prefZone, permTable);
            } else if (isMyRequests == true) {
                Hashtable<String, String> tagTable = new Hashtable<String, String>();

                handleMyRequests(aRequest, ba, user, prefZone, tagTable, permTable);

                String searchResults = tagTable.get("searchResultsHolder");

                searchResults = (searchResults != null)
                                ? searchResults
                                : "";
                out.println(searchResults);
            } else {
                handleXMLHttpRequest(aRequest, aResponse, user, ba, permTable, prefZone);
            }

            return;
        }

        // Tag table.
        Hashtable<String, String> tagTable = new Hashtable<String, String>();

        /*
         * Let us assume that this is request to display search page in
         * default view.
         */
        int searchRequestType = DISPLAY_REQUEST;

        /*
         * If, at this point, query is null, then this is a request
         *      1. to display the search page in its default/requested view.
         *      2. to process a given shortcut.
         */
        if (query == null) {
            if (shortcut == null) {
                searchRequestType = DISPLAY_REQUEST;
            } else {
                searchRequestType = SEARCH_REQUEST;
                query             = shortcut.getQuery();
                searchView        = shortcut.getView();
                isListAll         = shortcut.getIsListAll();
                tagTable.put("shortcutTitle", ": " + Utilities.htmlEncode(shortcut.getName()));
            }
        } else {
            searchRequestType = SEARCH_REQUEST;
            //reconsider
            if ((searchView == ALL_AREAS_VIEW) || (searchView == MY_REQUESTS_VIEW)) { // || searchView == HOME_VIEW) {
                searchView = NORMAL_VIEW;
            }
        }

        /*
         * Check if this is a request to display the my-requests page. This
         * overwrites the rest of other kinds of invocations.
         */
        if (isMyRequests == true) {
            searchRequestType = DISPLAY_REQUEST;
            searchView        = MY_REQUESTS_VIEW;
        }

        // Get the list of business areas the user can view.
        ArrayList<BusinessArea> baList = BusinessArea.getBusinessAreasByUserId(userId);

        // Replace the tags related to search view.
        setSearchView(searchView, tagTable);

        // Replace the tags related to My Requests checkboxes.
        getFilterList(tagTable, userConfig.getFilter());

        Searcher              searcher = null;
        ArrayList<ParseEntry> pTable   = null;

        /*
         * The content for the searchResultsHolder in search page is generated
         * as follows:
         *      1. In case of my-requests view, call the handleMyRequests to
         *         get the content.
         *      2. In case of a SEARCH_REQUEST, execute the query and render
         *         the results.
         *      3. In case of DISPLAY_REQUEST, get the default.
         */
        if (searchRequestType == SEARCH_REQUEST) {
            renderGroupActionInterface(permTable, tagTable, baconfig);
            searcher = executeQuery(aRequest, ba, user, query, textFilter, outputFormat, searchView, renderType, isListAll, tagTable, permTable, aSession);

            if (searcher != null) {
                if (outputFormat == OutputFormat.EXCEL) {
                    aResponse.setContentType("application/vnd.ms-excel");

                    int                flags     = ANCHOR | TEXT_SEVERITY | NO_FORMATTING | NO_HIERARCHY;
                    StringBuffer       aHtml     = new StringBuffer();
                    ArrayList<Integer> reqIdList = new ArrayList<Integer>();
                    String             colGroup  = getColumnGroup(ba, searcher.getDisplayHeader(), flags);
                    String url = aRequest.getRequestURL().toString()+ "/../../";
                    String             header    = getHeader(ba, searcher, NO_SORT_LINK, NO_SORT_ICON, flags, url);

                    flatRenderer(ba, userConfig, searcher, prefZone, aHtml, reqIdList, flags, OutputFormat.HTML, url);

                    StringBuffer buffer = new StringBuffer();

                    buffer.append(colGroup).append(header).append(aHtml.toString());

                    String shortcutName = aRequest.getParameter("shortcutName");

                    if ((shortcutName != null) &&!shortcutName.trim().equals("")) {
                        shortcutName = "[ " + shortcutName + " ]";
                    } else {
                        shortcutName = "";
                    }

                    Timestamp    ts    = new Timestamp().toGmtTimestamp();
                    String       cdate = WebUtil.getDateInFormat(ts, prefZone, userConfig.getListDateFormat());
                    DTagReplacer hp    = new DTagReplacer(RESULTS_EXCEL_FILE);

                    hp.replace("nearestPath", WebUtil.getNearestPath(aRequest, ""));
                    hp.replace("businessArea", ba.getDisplayName());
                    hp.replace("query", query);
                    hp.replace("shortcutName", shortcutName);
                    hp.replace("currentDate", cdate);
                    hp.replace("searchResults", buffer.toString());
                    out.println(hp.parse(systemId));

                    return;
                } else if (outputFormat == OutputFormat.XML) {
                    int flags = 0;

                    aResponse.setContentType("text/xml");

                    StringBuffer output = new StringBuffer();

                    output.append("<?xml version=\"1.0\" ?>");
                    output.append("<TBitsSearch>");
                    output.append(getXMLHeader(ba, searcher));
                    renderResults(ba, userConfig, searcher, prefZone, renderType, output, flags, OutputFormat.XML, aRequest);
                    output.append("</TBitsSearch>");
                    out.println(output.toString());

                    return;
                }

                renderResultsInHTML(searcher, ba, user, prefZone, renderType, tagTable, aRequest);
                pTable = searcher.getParseTable();
                tagTable.put("renderType", renderType.toString());
                tagTable.put(searchView + "_sortField", searcher.getSortField());
                tagTable.put(searchView + "_sortOrder", Integer.toString(searcher.getSortOrder()));

                ArrayList<ParseEntry> textEntries = searcher.getTextEntries();

                if ((textEntries != null) && (textEntries.size() > 0)) {
                    String sessionId    = tagTable.get("sessionId");
                    String txtSessionId = sessionId + "_TextEntries";

                    aSession.setAttribute(txtSessionId, textEntries);
                }
            }
//            mySearchNearestPath =  WebUtil.getNearestPath(aRequest, "") ;
            tagTable.put("nearestPath", WebUtil.getNearestPath(aRequest, ""));
            tagTable.put("isVEEnabled", Boolean.toString(baconfig.getEnableVE()));

            DTagReplacer dtr = new DTagReplacer(RESULTS_FILE);
            if (PropertiesHandler.isTransmittalEnabled())
            	dtr.replace("transmittal", "inline");
            else
            	dtr.replace("transmittal", "NONE");
            
            // adding exteneded UI components
            SlotFillerFactory euf = SlotFillerFactory.getInstance() ;
    		euf.runSearchResultsSlotFillers(aRequest, aResponse, ba, user, tagTable) ;//.runExtUIRenderers(aRequest, aResponse, tagTable, ourResultTagList, ba, ISlotFiller.SEARCH_RESULT ) ;
            
            SearchRenderer.replaceTags(dtr, tagTable, ourResultTagList);
            tagTable.put("searchResultsHolder", dtr.parse(systemId));
        } else if (searchRequestType == DISPLAY_REQUEST) {
            if (searchView == MY_REQUESTS_VIEW) {
                handleMyRequests(aRequest, ba, user, prefZone, tagTable, permTable);
            } else {            	
                tagTable.put("searchResultsHolder", renderSearchResults(ba, user, renderType));
            }
        }

        /*
         * Its time to render the following views
         *      1. Simple View
         *      2. Normal View
         *      3. Advanced View
         *      4. All Areas View.
         *
         *  In case of My_Requests or All_Areas, default render the other three
         *  views.
         */
        switch (searchView) {
        case ALL_AREAS_VIEW :
            tagTable.put("allAreasLoaded", "true");
            tagTable.put("allAreasHtml", renderAllBAView(aRequest, ba, baList, user, textFilter));
        case MY_REQUESTS_VIEW :
//        case SIMPLE_VIEW :
            renderSimpleView(aRequest, tagTable, query);
            renderNormalView(ba, user, textFilter, isPrivate, null, tagTable, aRequest);
            renderAdvancedView(aRequest, tagTable, "");
            break;
//        case HOME_VIEW :
//            renderSimpleView(aRequest, tagTable, "");
//            renderNormalView(ba, user, textFilter, isPrivate, null, tagTable, aRequest);
//            renderAdvancedView(aRequest, tagTable, "");
//            break;
        case NORMAL_VIEW :
            renderSimpleView(aRequest, tagTable, "");
            renderNormalView(ba, user, textFilter, isPrivate, searcher, tagTable, aRequest);
            renderAdvancedView(aRequest, tagTable, "");
            break;

        case ADVANCED_VIEW :
            renderSimpleView(aRequest, tagTable, "");
            renderNormalView(ba, user, textFilter, isPrivate, null, tagTable, aRequest);
            renderAdvancedView(aRequest, tagTable, query);

            break;
        }

        if (searchView != ALL_AREAS_VIEW) {
            tagTable.put("allAreasLoaded", "false");
            tagTable.put("allAreasHtml", "");
        }

        tagTable.put("currentSite", ourCurrentSite);
        tagTable.put("userLogin", userLogin);

        if (shortcut != null) {
            tagTable.put("shortcutName", Utilities.htmlEncode(shortcut.getName()));
        }
        
        //Get the JsonArray for menuButton
        JsonArray baJsonArray = BAMenu.getBAMenuJsonArray(baList); //getBAJSONArray(baList);
        
        tagTable.put("sysPrefix", sysPrefix);
        tagTable.put("sysName", displayName);
        tagTable.put("sysPrefixLabel", displayName + " [" + sysPrefix + "]");
        tagTable.put("baList", renderBAList(systemId, baList));
        tagTable.put("baJsonArray", baJsonArray.toString());
        tagTable.put("baMenuList", BAMenu.getJsonArrayOfAllBAMenus().toString());
        tagTable.put("singleIEWindow", Boolean.toString(userConfig.getSingleIEWindow()));
        tagTable.put("adminBlock", isAdmin(permTable, aRequest));
        tagTable.put("refreshRate", Long.toString(msec(userConfig.getRefreshRate())));
        WebUtil.setInstanceBold(tagTable, ba.getSystemPrefix());
        tagTable.put("searchShortcuts", ShortcutHandler.renderShortcuts(aRequest, user, ba));
        tagTable.put("userDraftsInfo", WebUtil.listUserDrafts(aRequest, user.getUserId(), false, false));
        tagTable.put("searchBuilderData", SearchRenderer.getAdvancedSearchData(ba, isPrivate));
        tagTable.put("primaryDescTable", getPrimaryDescTableInJSON(systemId));
        tagTable.put("nearestPath", WebUtil.getNearestPath(aRequest, ""));
        tagTable.put("cssFile", WebUtil.getCSSFile(sysConfig.getWebStylesheet(), sysPrefix, false));
        String display_logout = "none";
        if(aRequest.getAuthType() == AuthConstants.AUTH_TYPE)
        	display_logout = "";
        //System.out.println("Display Logout: " + display_logout);
        tagTable.put("display_logout", display_logout);
        
        if (userConfig.getSimpleView() == true) {
            tagTable.put("simpleRequired", "INLINE-BLOCK");
        } else {
            tagTable.put("simpleRequired", "NONE");
        }
        
        tagTable.put("current_user_id", userId + "");
        tagTable.put("sys_id", ba.getSystemId() + "");
     
        // Finally, read the html file and replace all the tags.
        DTagReplacer hp = new DTagReplacer(HTML_FILE);
        String xtBitsSearchJSs = ActionHelper.getResourceString(tBitsSearchJSs, WebUtil.getNearestPath(aRequest, "") ) ;
   //     LOG.info("tBitsSearchJSs ="+xtBitsSearchJSs) ;
        String xYUISearchJSs = ActionHelper.getResourceString(YUISearchJSs, WebUtil.getNearestPath(aRequest, "") ) ;
  //      LOG.info("YUISearchJSs =" + xYUISearchJSs) ;
        tagTable.put("YUISearchJSs",  xYUISearchJSs) ;
        tagTable.put("tBitsSearchJSs", xtBitsSearchJSs ) ;
        
        SlotFillerFactory slotFactory = SlotFillerFactory.getInstance();
        slotFactory.runSearchSlotFillers(aRequest, aResponse, ba, user, tagTable) ;
    //    ArrayList<ISlotFiller> renderers = slotFactory.getExtUIRendererList();
    //    String renderersString = "";
//        for(ISlotFiller renderer : renderers){
//        	String html = renderer.process(aRequest, aResponse, tagTable, ba, ISlotFiller.SEARCH);
//        	if (html != null)
//        		renderersString += html;
//        }
        
      //  tagTable.put("searchResExtUIList", renderersString) ;
        // Replace the tags and print the content.
        SearchRenderer.replaceTags(hp, tagTable, ourSearchTagList);
        out.println(hp.parse(systemId));

        return;
    }
    
   
    /**
     * This method actually services the http requests.
     *
     *
     * @param aRequest  HttpServletRequest object.
     * @param aResponse HttpServletResponse object.
     * @exception ServletException
     * @exception IOException
     * @exception DESTBitsExceptionexception DatabaseException
     */
    public void handleXMLHttpRequest(HttpServletRequest aRequest, HttpServletResponse aResponse, User aUser, BusinessArea aBA, Hashtable<String, Integer> aPermTable, TimeZone aZone)
            throws ServletException, IOException, TBitsException, DatabaseException {

        // PrintStream.
        PrintWriter out = aResponse.getWriter();

        aResponse.setContentType("text/html");

        // Get the session object.
        HttpSession aSession = aRequest.getSession(true);

        // Get the User Config.
        WebConfig webConfig = aUser.getWebConfigObject();
        int       systemId  = aBA.getSystemId();
        String    sysPrefix = aBA.getSystemPrefix();
        BAConfig  baConfig  = webConfig.getBAConfig(sysPrefix);

        // Get the search view.
        int    searchView = getSearchView(aRequest, aSession, sysPrefix, webConfig);
        String sessionKey = sysPrefix + "_view";

        aSession.setAttribute(sessionKey, Integer.toString(searchView));

        // Get the query string.
        String query = getQuery(aRequest);

        if (query == null) {
            out.println(Messages.getMessage("NO_SEARCH_RECORDS"));
        }

        String  filter    = SearchRenderer.getTextFilter(aRequest);
        boolean isListAll = checkIfPresent(aRequest, "listAll");

        // Get the rendering type.
        RenderType renderType = getRenderType(aRequest, webConfig);
        String     desc       = aRequest.getParameter(Field.DESCRIPTION);

        desc = (desc == null)
               ? ""
               : desc.trim();

        Hashtable<String, String> tagTable = new Hashtable<String, String>();

        tagTable.put(Field.DESCRIPTION, desc);
        tagTable.put("shortcutName", "");
        tagTable.put("shortcutDisplay", "none");
        tagTable.put("searchResultsDisplay", "");
        renderGroupActionInterface(aPermTable, tagTable, baConfig);

        Searcher searcher = executeQuery(aRequest, aBA, aUser, query, filter, OutputFormat.HTML, searchView, renderType, isListAll, tagTable, aPermTable, aSession);

        if (searcher != null) {
            renderResultsInHTML(searcher, aBA, aUser, aZone, renderType, tagTable, aRequest);

            ArrayList<ParseEntry> textEntries = searcher.getTextEntries();

            if ((textEntries != null) && (textEntries.size() > 0)) {
                String sessionId    = tagTable.get("sessionId");
                String txtSessionId = sessionId + "_TextEntries";

                aSession.setAttribute(txtSessionId, textEntries);
            }

            tagTable.put("renderType", renderType.toString());
            tagTable.put(searchView + "_sortField", searcher.getSortField());
            tagTable.put(searchView + "_sortOrder", Integer.toString(searcher.getSortOrder()));
        }

        tagTable.put("isVEEnabled", Boolean.toString(baConfig.getEnableVE()));
        tagTable.put("nearestPath", WebUtil.getNearestPath(aRequest, ""));

        DTagReplacer hp = new DTagReplacer(RESULTS_FILE);
        // NITI msg : adding exteneded UI components
        SlotFillerFactory euf = SlotFillerFactory.getInstance() ;
		euf.runSearchResultsSlotFillers(aRequest, aResponse, aBA, aUser, tagTable) ;//.runExtUIRenderers(aRequest, aResponse, tagTable, ourResultTagList, aBA, ISlotFiller.SEARCH_RESULT ) ;

        if (PropertiesHandler.isTransmittalEnabled())
        	hp.replace("transmittal", "inline");
        else
        	hp.replace("transmittal", "NONE");
        
        SearchRenderer.replaceTags(hp, tagTable, ourResultTagList);

        String tmp = aRequest.getParameter("onlyResults");

        if ((tmp != null) && (tmp.trim().equals("true") == true)) {
            String results = tagTable.get("searchResults");

            out.println(results);

            return;
        }

        // Print this out and return.
        out.println(hp.parse(systemId));

        return;
    }

    /**
     * This method renders the results considering their parent-child
     * relationships.
     *
     * @param aBA          Business Area.
     * @param aConfig      User Config.
     * @param aSearcher    Searcher object.
     * @param aZone        User Preferred Zone.
     * @param aHtml        Out parameter that holds the results.
     * @param aReqIdList   List of request ids.
     * @param aRequest TODO
     *
     */
    private static void hierRenderer(BusinessArea aBA, WebConfig aConfig, Searcher aSearcher, TimeZone aZone, StringBuffer aHtml, ArrayList<Integer> aReqIdList, int aFlags, OutputFormat aFormat, HttpServletRequest aRequest) {
        if (aSearcher == null) {
            return;
        }

        Hashtable<String, Field>             fieldsTable       = aSearcher.getFieldsTable();
        ArrayList<String>                    header            = aSearcher.getDisplayHeader();
        ArrayList<String>                    resultIds         = aSearcher.getResultIdList();
        ArrayList<String>                    incompleteParents = aSearcher.getRequestsWithIncompleteTrees();
        ArrayList<String>                    completeIdList    = aSearcher.getAllRequestIdList();
        Hashtable<String, Result>            resultsTable      = aSearcher.getRequestsTable();
        Hashtable<String, ArrayList<String>> parTable          = aSearcher.getParentsTable();
        Hashtable<String, ArrayList<Result>> xTable            = new Hashtable<String, ArrayList<Result>>();
        Hashtable<String, Boolean>           finalTbl          = new Hashtable<String, Boolean>();

        for (String resultKey : resultIds) {
            Result result = resultsTable.get(resultKey);

            if (result == null) {
                LOG.info("No result corresponding to ID: " + resultKey);

                continue;
            }

            int systemId  = result.getSystemId();
            int requestId = result.getRequestId();
            int parentId  = result.getParentId();

            aReqIdList.add(requestId);

            ArrayList<String> children   = parTable.get(resultKey);
            ResultType        resultType = getResultType(parentId, children);

            result.setResultType(resultType);

            ArrayList<Result> xList = new ArrayList<Result>();

            switch (resultType) {
            case RESULT_NORMAL :

                /*
                 * RESULT_NORMAL:
                 *    1. Set the indentation of the result to 0.
                 *    2. Add the result to the final list.
                 *    3. Add the key of the result to the finalTbl.
                 */
                result.setIndentation(0);
                xList = new ArrayList<Result>();
                xList.add(result);
                xTable.put(resultKey, xList);
                finalTbl.put(resultKey, true);

                break;

            case RESULT_ROOT :

                /*
                 * Add the complete tree.
                 */
                if (finalTbl.get(resultKey) == null) {
                    xList = new ArrayList<Result>();
                    addCompleteTreeBelow(resultKey, parTable, resultsTable, xList, finalTbl, completeIdList, 0,    // No default flags.
                                         0                                                                         // Indentation is 0 to start with.
                                             );
                    xTable.put(resultKey, xList);
                }

                break;

            case RESULT_PARENT :
            case RESULT_LEAF :

            /*
             * 1. Find out the root of the tree in which this parent/leaf
             *    is a member.
             * 2. Check if that root is already considered.
             * 3. If not, add the complete tree.
             */
            {
                String rootKey = getRootKey(resultKey, resultsTable);

                if (finalTbl.get(rootKey) == null) {
                    xList = new ArrayList<Result>();
                    addCompleteTreeBelow(rootKey, parTable, resultsTable, xList, finalTbl, completeIdList, 0,      // No default flags.
                                         0                                                                         // Indentation is 0 to start with
                                             );
                    xTable.put(rootKey, xList);
                } else {

                    /*
                     * This root is already considered which means the
                     * current request is also considered.
                     */
                }
            }

            break;
            }                                                                                                      // End Switch.
        }                                                                                                          // End For.

        /*
         * TODO:
         * This list will now contain all the top level requests. present in
         * the correct sort order. A better approach can be thought out for
         * this if this proves costly.
         */
        ArrayList<Result> finalList = new ArrayList<Result>();

        for (String key : completeIdList) {
            ArrayList<Result> yList = xTable.get(key);

            if (yList != null) {
                finalList.addAll(yList);
            }
        }

        int treeId = getTreeId(aSearcher.getParseTable());

        /*
         * The finalList is the one to be rendered in that order.
         * Check if each of the result in finalList has actually matched the
         * criteria, or it is present because of being a part of hierarchy.
         */
        for (Result result : finalList) {
            int               systemId   = result.getSystemId();
            int               requestId  = result.getRequestId();
            String            key        = Searcher.formTableKey(systemId, requestId);
            boolean           matched    = resultIds.contains(key);
            boolean           incomplete = incompleteParents.contains(key);
            ArrayList<String> children   = parTable.get(key);

            if (requestId == treeId) {
                aFlags = aFlags | TREE_RESULT;
            } else {
                if ((aFlags & TREE_RESULT) != 0) {
                    aFlags = aFlags ^ TREE_RESULT;
                }
            }

            String html = serialize(aBA, result, aConfig, fieldsTable, header, aZone, matched, incomplete, aFlags, aFormat, aRequest.getContextPath());

            aHtml.append(html);
        }
    }

    /**
     *
     * @param aBA
     * @param aResult
     * @param aConfig
     * @param aFieldsTable
     * @param aHeader
     * @param aPrefZone
     * @param aMatched
     * @param aIncomplete
     * @param aFlags
     *
     * @return Html representation of the result.
     */
    private static String htmlSerialize(BusinessArea aBA, Result aResult, WebConfig aConfig, Hashtable<String, Field> aFieldsTable, ArrayList<String> aHeader, TimeZone aPrefZone, boolean aMatched,
                                       boolean aIncomplete, int aFlags, String aContextPath) {

        // Buffer is the return value of this method.
        StringBuilder buffer = new StringBuilder();

        // Get the list of parameters generally needed.
        int       requestId = aResult.getRequestId();
        String    sysPrefix = aBA.getSystemPrefix();
        BAConfig  baConfig  = aConfig.getBAConfig(sysPrefix);
        SysConfig sysConfig = aBA.getSysConfigObject();

        // Retrieve the params related to visual enhancements.
        String    status       = aResult.getStatus().trim();
        String    severity     = aResult.getSeverity().trim();
        boolean   isPrivate    = aResult.getIsPrivate();
        int       maxActionId  = aResult.getMaxActionId();
        int       lastActionId = aResult.getLastActionId();
        Timestamp dueDate      = aResult.getDueDate();
        int       leafFlags    = aResult.getFlags();

        String IMAGE_PATH = "";
        //Set path
        if(aContextPath != null)
        	IMAGE_PATH = WebUtil.getNearestPath(aContextPath, IMAGE_STATIC_PATH);
        else
        	IMAGE_PATH = WebUtil.getNearestPath(IMAGE_STATIC_PATH);
        /*
         * Name of the group action check box. This is needed for Select All
         * functionality.
         */
        String grpName = "grp_chbox";

        // Id of the Group action check box.
        String grpId = "grp_" + sysPrefix + "_" + requestId;

        // Id of the row.
        String rowId = "row_" + sysPrefix + "_" + requestId;

        // Id of the new image.
        String newId = "new_" + sysPrefix + "_" + requestId;

        /*
         * Expand/Collapse related vars...
         */
        String expId      = "exp_" + sysPrefix + "_" + requestId;
        String colId      = "col_" + sysPrefix + "_" + requestId;
        String requestURL = WebUtil.getServletPath(aContextPath, "/Q/" + sysPrefix + "/" + requestId);
        String subjClass  = "class='mrsubject'";
        String newImg     = "";
        String indImg     = "";
        String rowStyle   = "";

        // Prepare the indentation text.
        int     ctr         = 0;
        int     indentation = aResult.getIndentation();
        boolean isLeaf      = ((leafFlags & LEAF) != 0
                               ? true
                               : false);
        boolean isLastLeaf  = ((leafFlags & LAST_LEAF) != 0
                               ? true
                               : false);

        // String indentLine = "&nbsp;&nbsp;&nbsp;";
        String lineName    = "child-line.gif";
        String emptyName   = "child-empty.gif";
        String nodeName    = "child.gif";
        int    indPosition = ((int) Math.round(Math.pow(2, indentation)));

        if (((isLeaf == true) && (isLastLeaf == true)) || (leafFlags & indPosition) == 0) {
            nodeName = "leaf.gif";
        }

        String lineImg  = "<img src='" + IMAGE_PATH + lineName + "'>";
        String nodeImg  = "<img src='" + IMAGE_PATH + nodeName + "'>";
        String emptyImg = "<img src='" + IMAGE_PATH + emptyName + "'>";

        for (ctr = 1; ctr < indentation; ctr++) {
            int value = ((int) Math.round(Math.pow(2, ctr)));

            if ((leafFlags & value) != 0) {
                indImg = indImg + lineImg;
            } else {
                indImg = indImg + emptyImg;
            }
        }

        if (indentation > 0) {
            indImg = indImg + nodeImg;
        }

        String  dBlock    = "''";
        String  dNone     = "'display: none'";
        String  className = "";    // Class of the row.
        boolean track     = false;
        boolean read      = true;

        /*
         * Row will be in bold if the user wanted us to track read/unread
         * requests and this result is updated since last visited.
         */
        if (baConfig.getEnableVE() == true) {
            track = true;

            if (lastActionId < maxActionId) {

                /*
                 * Mark this as unread if either
                 *   - the request is not closed (OR)
                 *   - the request is closed but updated in last 'N' days
                 *     where 'N' is a configuration for this BA.
                 */
                Timestamp updDate = (Timestamp) aResult.get(Field.LASTUPDATED_DATE);
                Timestamp today   = new Timestamp();
                long      n       = sysConfig.getMaxGrayPeriod();

                // N is in days convert into milliseconds.
                long maxAllowedDiff = n * 86400000;
                long actualDiff     = today.getTime() - updDate.getTime();

                status = (status == null)
                         ? ""
                         : status.trim();

                if ((status.equalsIgnoreCase("closed") == false) || (actualDiff < maxAllowedDiff)) {
                    className = "b";
                    read      = false;
                }
            }

            /*
             * New image is needed if the request is new and no other action
             * happened on that since it is logged.
             */
            if ((lastActionId == 0) && (maxActionId == 1)) {
                newImg = (new StringBuffer()).append("<IMG height='10' id='").append(newId).append("' width='10' ").append("src='").append(IMAGE_PATH).append("new.gif' />").toString();
            }
        }

        // onclick handler.
        String onClick = "onr('" + sysPrefix + "', " + requestId + ", '" + rowId + "')";

        // onmouseout handler
        String onMouseOut = " onmouseout=\"setMouseOutTimer();\" ";
        
        int systemId = aBA.getSystemId();
        String viewRequestProperty = "View " + Utilities.getCaptionBySystemId(systemId, CaptionsProps.CAPTIONS_ALL_CAMEL_CASE_REQUEST);
        String updateRequestProperty = Utilities.getCaptionBySystemId(systemId, CaptionsProps.CAPTIONS_VIEW_UPDATE_REQUEST);
		String subRequestProperty = Utilities.getCaptionBySystemId(systemId, CaptionsProps.CAPTIONS_VIEW_ADD_SUBREQUEST);
        
        // onmouseover handler.
        String onMouseOver = "this.T_STICKY = true; " + "this.T_FIX = [gal(this) + 15, gat(this) + 20];" + "this.T_DELAY = 100; " + "this.T_TEMP = 0; " + "this.T_BORDERWIDTH = 0; "
                             + "this.T_WIDTH  = '150px';" + "return escape(stt('" + sysPrefix + "'," + requestId + ", '" + rowId + "', " + track + ", " + read + ", '" + 
                             viewRequestProperty + "', '" + updateRequestProperty + "', '" + subRequestProperty +"'));";
        String style = "";

        if (aMatched == UNMATCHED_RESULT) {
            className = className + " unmatched";
            subjClass = "class='ursubject'";
        }

        if ((aFlags & TREE_RESULT) != 0) {
            className = className + " tree";
            subjClass = "class='ursubject'";
        }

        ResultType resultType = aResult.getResultType();

        if (resultType == null) {
            LOG.severe("ResultType not found: " + sysPrefix + "#" + requestId);
            resultType = ResultType.RESULT_NORMAL;
        }

        switch (resultType) {
        case RESULT_LEAF :
            className = className + " leaf";

            break;

        case RESULT_NORMAL :
            className = className + " normal";

            break;

        case RESULT_PARENT :
            className = className + " parent";

            break;

        case RESULT_ROOT :
            className = className + " root";

            break;
        }

        if (className.trim().equals("") == false) {
            className = "class='" + className.trim() + "'";
        }

        /*
         * Finally if the HIDE_RESULT flag is set then the display property
         * of this request should be set to none.
         */
        if ((aFlags & HIDE_RESULT) != 0) {
            style = "style='display: none'";
        }

        buffer.append("<TR ").append(className).append(" ").append(rowStyle).append(" id='").append(rowId).append("' ").append(style).append(">");

        /*
         * If this user has permissions to perform group actions, then this
         * column should be rendered.
         */
        if ((aFlags & GROUP_ACTION) != 0) {
            buffer.append("<TD><input type='checkbox' class='grp-chbox' name='").append(grpName).append("' id='").append(grpId).append("' ").append("value='").append(requestId).append(
                "' onclick=\"").append("cfga(this);\"/></TD>");
        }

        for (String fieldName : aHeader) {
            Field  field = null;
            Object value = aResult.get(fieldName);

            field = aFieldsTable.get(fieldName);

            if (field == null) {
                continue;
            }

            if (fieldName.equals(Field.REQUEST) == true) {

                /*
                 * This column will have
                 *  - "+/-" icons to indicate the hierarchy of requests
                 *  - ">>" icon to indicate a request which is newly logged
                 * since the user last searched in this business area.
                 */
                if ((aFlags & NO_HIERARCHY) == 0) {
                    buffer.append("<TD class=\"ri\" align='left' nowrap>").append(newImg);

                    /*
                     * If this is root of a hierarchy, then include the expand
                     * and collapse icons and hide the expand icon by default.
                     */
                    if (resultType == ResultType.RESULT_ROOT) {
                        String expImg = "<IMG id='" + expId + "' src='" + IMAGE_PATH + "plus.gif' style='display: none' name='expIcon' " + "onclick=\"ec('" + sysPrefix + "', '" + requestId
                                        + "', 'true');\"/>";
                        String colImg = "<IMG id='" + colId + "' src='" + IMAGE_PATH + "minus.gif' style='display: ' name='colIcon' " + "onclick=\"ec('" + sysPrefix + "', '" + requestId
                                        + "', 'false');\"/>";

                        buffer.append(expImg).append(colImg);
                    }

                    /*
                     * Check if the request is past due date.
                     */
                    if ((dueDate != null) && (status.equalsIgnoreCase("closed") == false)) {
                        long dueTime = dueDate.toSiteTimestamp().getTime();
                        long nowTime = Timestamp.getGMTNow().getTime();
                        if (dueTime < nowTime) {
                            String dueBy  = dueDate.toDateMin();
                            String dueImg = "<IMG src='" + IMAGE_PATH + "alarm.gif'" + " title='Due By: " + dueBy + "' />";

                            buffer.append(dueImg);
                        }
                    }

                    buffer.append("</TD>");
                }

                /*
                 * Next comes the actual column that holds the request id
                 * value.If this is a critical request, then render it with a
                 * different style.
                 */
                buffer.append("<TD>");
                buffer.append(indImg);
                buffer.append("<SPAN class='l");

                // check if severity is critical
                if (severity.equalsIgnoreCase("critical") == true) {
                    buffer.append(" crit ");
                }

                if (aMatched == MATCHED_RESULT) {
                    buffer.append(" cb ");
                }

                buffer.append("'");

                /*
                 * If javascript is needed then in the onclick event of span,
                 * call the corresponding javascript function.
                 */
                if ((aFlags & JAVASCRIPT) != 0) {
                    buffer.append(" onclick=\"return ").append(onClick).append("\"");
                }

                /*
                 * If the mouseover on requestId should bring up sticky tool
                 * tip, then call the corresponding handler in onmouseover
                 * event.
                 */
                if ((aFlags & MOUSEOVER) != 0) {
                    buffer.append(" onmouseover=\"").append(onMouseOver).append("\"").append(onMouseOut);
                }

                buffer.append(">");

                /*
                 * If anchor elements are needed, then embed the request Id in
                 * the anchor tags.
                 */
                if ((aFlags & ANCHOR) != 0) {
                    buffer.append("<A href='").append(requestURL).append("'>").append(requestId).append("</A>");
                } else {
                    buffer.append(requestId);
                }

                buffer.append("</SPAN>");

                if (aIncomplete == true) {
                    String incHierImg         = "<img src='" + IMAGE_PATH + "inc-hier.gif'>";
                    String msgIncHier         = Messages.getMessage("HELP_INCOMPLETE_HIERARCHY");
                    String onMouseOverIncHier = " onmouseover=\"domTT_activate(this, event," + "'content','" + Utilities.htmlEncode(msgIncHier) + "');\" ";

                    buffer.append("<span class='sx l' ").append("style='vertical-align: center' ").append(onMouseOverIncHier).append("onclick=\"showCompleteTree('").append(sysPrefix).append(
                        "', ").append(requestId).append(")\">").append(incHierImg).append("</span>");
                }

                /*
                 * Append the dagger symbol if the request is private and the
                 * user did not opt to remove formatting.
                 */
                if ((isPrivate == true) && (aFlags & NO_FORMATTING) == 0) {
                    buffer.append("&nbsp;&dagger;");
                }

                buffer.append("</TD>");

                continue;
            } else if (fieldName.equals(Field.SEVERITY) == true) {
//                if ((aFlags & TEXT_SEVERITY) == 0) {
//
//                    // Render severity column.
//                    String image = SeverityImages.getInstance(IMAGE_PATH).getSeverityImgTable().get(severity.toLowerCase());
//
//                    image = (image == null)
//                            ? ""
//                            : image;
//                    buffer.append("<TD>").append(image).append("</TD>");
//                } else {
                    buffer.append("<TD>").append("<SPAN style=\"white-space: nowrap;\" id=\"").append(severity.toLowerCase().replace(' ',
                            '_')).append("\">").append(severity).append("</span>").append("</TD>");
//                }

                continue;
            } else if (fieldName.equals(Field.SUBJECT) == true) {
                String strValue = ((value == null)
                                   ? ""
                                   : value.toString());

                if (strValue == null) {
                    strValue = "-";
                }

                buffer.append("<TD><SPAN class='l'");

                if ((aFlags & JAVASCRIPT) != 0 && (aFlags & LINK_SUBJECT) != 0) {
                    buffer.append(" onclick=\"return ").append(onClick).append("\"");
                }

                buffer.append(">");

                if ((aFlags & ANCHOR) != 0 && (aFlags & LINK_SUBJECT) != 0) {
                    buffer.append("<A ").append(subjClass).append(" href='").append(requestURL).append("'>").append(Utilities.htmlEncode(strValue)).append("</A>");
                } else {
                    buffer.append(Utilities.htmlEncode(strValue));
                }

                buffer.append("</SPAN></TD>");

                continue;
            }

            int dataType = field.getDataTypeId();

            buffer.append("<TD>");

            switch (dataType) {
            case BOOLEAN : {
                String strValue = (value == null)
                                  ? "-"
                                  : value.toString();

                buffer.append(strValue);
            }

            break;

            case DATE :
            case TIME :
            case DATETIME : {
                if (value instanceof Timestamp) {
                    Timestamp ts = (Timestamp) value;

                    if (ts == null) {
                        buffer.append("-");
                    } else {
                        String dateValue = WebUtil.getDateInFormat(ts, aPrefZone, aConfig.getListDateFormat());

                        dateValue = dateValue.replaceAll(" ", "&nbsp;").replaceAll("-", "&#8209;");
                        buffer.append(dateValue);
                    }
                } else {
                    buffer.append("-");
                }
            }

            break;

            case INT : {
                if (value == null) {
                    buffer.append("-");
                } else {
                    buffer.append(value.toString());
                }
            }

            break;

            case REAL : {
                if (value == null) {
                    buffer.append("-");
                } else {
                    buffer.append(value.toString());
                }
            }

            break;

            case STRING :
            case TEXT : {
                if (value == null) {
                    buffer.append("-");
                } else {
                    buffer.append(Utilities.htmlEncode(value.toString()));
                }
            }

            break;

            case USERTYPE : {
                if (value == null) {
                    buffer.append("-");
                } else {
                    String str = value.toString();

                    str = str.replaceAll("\\.transbittech\\.com", "");

                    if ((aFlags & NO_FORMATTING) == 0) {
                        str = str.replaceAll(",", ",<BR>");
                    }

                    buffer.append(str);
                }
            }

            break;

            case TYPE : {
                if (value == null) {
                    buffer.append("-");
                } else {

                    /*
                     * Check if this starts with a + in which case, it is
                     * a private type.
                     */
                    String strValue = value.toString();

                    strValue = strValue.replaceAll(" ", "&nbsp;").replaceAll("-", "&#8209;");

                    if (strValue.startsWith("+") == true) {
                        strValue = strValue.substring(1) + "&nbsp;&dagger;";
                    }

                    buffer.append(strValue);
                }
            }
            break;
            case ATTACHMENTS:
            {
            	if (value == null) {
                    buffer.append("");
                } else {

                    /*
                     * Check if this starts with a + in which case, it is
                     * a private type.
                     */
                    String strValue = value.toString();
                    if(!strValue.trim().equals("")){
                    	Collection<AttachmentInfo> attachments = AttachmentInfo.fromJson(strValue);
                    	String attString = "";
                    	for(AttachmentInfo att : attachments){
                    		String url = aContextPath + "/read-attachment/" + sysPrefix + "?request_id=" 
                				+ requestId + "&request_file_id=" + att.requestFileId + "&field_id=" + field.getFieldId() + "&saveAs=true";
                    		strValue = "<a href = '" + url +"'>" + att.name + "</a>";
                    		if(!attString.equals(""))
                    			attString = attString + ", " + strValue;
                    		else
                    			attString = strValue;
                    	}
                    	buffer.append(attString);
                    }else{
                    	 buffer.append("");
                    }
                }
            	break;
            }
            

            }

            buffer.append("</TD>");
        }

        buffer.append("</TR>");

        return buffer.toString();
    }

    /**
     * This method loads the static data
     */
    private static void loadStaticData() {
        StringBuffer tagList = new StringBuffer();

        tagList.append("current_user_id,sys_id,currentSite,sysPrefix,sv,allAreasLoaded,baList,baJsonArray,baMenuList,sysPrefixLabel,").append("simpleClass,normalClass,advancedClass,allAreasClass,").append("myRequestsClass,shortcutTitle,").append(
            "simpleRequired,simpleDisplay,simpleQuery,normalDisplay,").append("textFilters,logger_ids_label,category_id_label,").append(
            "status_id_label,severity_id_label,request_type_id_label,").append("assignee_ids_label,subscriber_ids_label,cbLast_checked,").append("office_id_list,office_id_label,").append(
            "logger_ids,category_id_list,status_id_list,").append("severity_id_list,request_type_id_list,assignee_ids,").append("subscriber_ids,user_id,advancedDisplay,advancedQuery,").append(
            "myRequestsDisplay,allAreasDisplay,allAreasHtml,").append("searchResultsHolder,singleIEWindow,userLogin,").append("adminBlock,configureLine,sessionId,searchShortcuts,").append(
            "refreshRate,userDraftsInfo,primaryDescTable,").append("instanceBoldHyd,instanceBoldNyc,cssList,").append("instancePathHyd,instancePathNyc,normalDesc,").append(
            "loggerChecked,assigneeChecked,subscriberChecked,").append("primaryAssigneeChecked,nearestPath,searchBuilderData,").append("sysName,shortcutName,onMouseOverHyd,onMouseOverNyc,").append(
            "cssFile,userDrafts,dateDivDisplay,").append("log_defValue,upd_defValue,due_defValue,")
            .append("log_defClause,upd_defClause,due_defClause,readFilter,display_logout,submitted_label,last_modified_label,due_date_label,")
            .append("YUISearchJSs,tBitsSearchJSs,searchFooterSlot");
        ourSearchTagList = Utilities.toArrayList(tagList.toString());
        tagList          = null;
        tagList          = new StringBuffer();
        
        // NITI msg : adding the extra tag required for adding exteneded UI components
        tagList.append("sessionId,groupActionList,").append("databaseTime,interfaceTime,resultComment,exception,").append("searchText,searchResults,renderType,expState,").append(
            "pageNumber,pageCounter,").append("searchResultsDisplay,rcDisplay,pageNumberDisplay,").append("groupActionDisplay,miscFuncDisplay,excepDisplay,").append("1_sortField,1_sortOrder,").append(
            "2_sortField,2_sortOrder,").append("3_sortField,3_sortOrder,").append("listAllDisplay,listAllChecked,").append("isVEEnabled,nearestPath,").append("searchResultsHeaderSlot,");
        ourResultTagList = Utilities.toArrayList(tagList.toString());
        tagList          = null;
        tagList          = new StringBuffer();
        tagList.append("aa_textFilters,aa_business_areas,aa_status_id_list,")
        .append("aa_severity_id_list,aa_logger_ids,aa_assignee_ids,")
        .append("aa_subscriber_ids,aa_user_id,sysPrefix,nearestPath,submitted_label,last_modified_label,due_date_label");
        ourAllAreasTagList = Utilities.toArrayList(tagList.toString());
        tagList            = null;
        tagList            = new StringBuffer();
        tagList.append("searchShortcuts,searchResults");
        ourAllAreasResultsTagList = Utilities.toArrayList(tagList.toString());
        tagList                   = null;
        tagList                   = new StringBuffer();
        tagList.append("sessionIdName,sessionIdKey,colGroup,colSpan,displayName,").append("emailLink,resultComment,sysPrefix,header,results,").append("resultsTableName,rowId,collapseText,");
        ourMyRequestsResultsTagList = Utilities.toArrayList(tagList.toString());

        try {
            ourCurrentSite = PropertiesHandler.getProperty(KEY_DOMAIN);
        } catch (Exception e) {}

        if ((ourCurrentSite == null) || ourCurrentSite.trim().equals("")) {
            ourCurrentSite = "HYD";
        } else {
            ourCurrentSite = ourCurrentSite.toUpperCase();
        }  
        
        // <%=nearestPath%>c?<%=nearestPath%>web/scripts/search-min.js&<%=nearestPath%>web/scripts/common-min.js&<%=nearestPath%>web/scripts/tt-min.js&<%=nearestPath%>web/scripts/cal-min.js&<%=nearestPath%>web/scripts/dialogpanel-min.js&<%=nearestPath%>web/scripts/tbits-yui-utils-min.js&<%=nearestPath%>web/scripts/json-min.js&<%=nearestPath%>web/scripts/transmittal-panel-min.js&<%=nearestPath%>web/scripts/wac-min.js&<%=nearestPath%>web/scripts/wtt-min.js&<%=nearestPath%>web/scripts/user-reports-min.js&<%=nearestPath%>web/scripts/dashboard-functions-min.js
        tBitsSearchJSs = Utilities.toArrayList(
								        		"web/scripts/search-min.js,"+
								        		"web/scripts/common-min.js,"+
								        		"web/scripts/tt-min.js,"+
								        		"web/scripts/cal-min.js,"+
								        		"web/scripts/dialogpanel-min.js,"+
								        		"web/scripts/tbits-yui-utils-min.js,"+
								        		"web/scripts/json-min.js,"+
								        		"web/scripts/transmittal-panel-min.js,"+
								        		"web/scripts/wac-min.js,"+
								        		"web/scripts/wtt-min.js,"+
								        		"web/scripts/user-reports-min.js,"+
								        		"web/scripts/dashboard-functions-min.js"

        										);
        
        YUISearchJSs = Utilities.toArrayList(
								        		"web/yui/build/utilities/utilities.js,"+
								        		"web/yui/build/container/container-min.js,"+
								        		"web/yui/build/menu/menu-min.js,"+
								        		"web/yui/build/button/button-min.js,"+
								        		"web/yui/build/datasource/datasource-min.js,"+
								        		"web/yui/build/datatable/datatable-min.js,"+
								        		"web/yui/build/json/json-min.js,"+
								        		"web/yui/build/resize/resize-min.js,"+
								        		"web/yui/build/tabview/tabview-min.js"

        								 	) ;
    }
    
    
    
    /**
     * This method returns the milliseconds value equivalent to the given
     * minutes value.
     *
     * @param minuteValue Value in minutes.
     *
     * @return Value in milli seconds.
     */
    private static long msec(int minuteValue) {
        if (minuteValue < 0) {
            return (long) minuteValue;
        }

        long value = minuteValue * 60 * 1000;

        return value;
    }

    /**
     * This method extracts the business area prefix, shortcuts from the URL.
     *
     * @param aRequest Http Request object.
     * @param aUser    User object.
     *
     * @return Hashtable that contains the BA Prefix and the Shortcut if any.
     *
     */
    private Hashtable<String, Object> parseURL(HttpServletRequest aRequest, User aUser) throws ServletException, TBitsException, DatabaseException {

        /*
         * Positive Cases to be tested when this method is changed:
         *
         *  1. http://tbits.hyd.transbittech.com/search
         *      - Default BA
         *      - Default shortcut if any in the Default BA
         *
         *  2. http://tbits.hyd.transbittech.com/search/my-requests
         *      - Should show my-requests page.
         *      - Underlying BA should be the default one.
         *
         *  3. http://tbits.hyd.transbittech.com/search/bmw/my-requests
         *      - Should show my-requests page.
         *      - Underlying BA should be bmw.
         *
         *  4. http://mytbits.com/search/bmw
         *      - BA must be bmw or throw an error.
         *      - Default shortcut in BMW for this user, if any.
         *
         *  5. http://mytbits.com/search/bmw/test
         *      - BA should be bmw or throw an error.
         *      - Shortcut should be test or throw an error.
         *
         *  6. http://mytbits.com/search/bmw/Vaibhav:test
         *      - BA must be bmw or throw an error.
         *      - Shortcut must be test or throw an error.
         *
         *  7. http://mytbits.com/search/bmw/Vinod Gupta:test
         *      - BA must be bmw or throw an error.
         *      - test must be a public shortcut or throw an error.
         */

        // Return value.
        Hashtable<String, Object> result    = new Hashtable<String, Object>();
        WebConfig                 aConfig   = aUser.getWebConfigObject();
        String                    userLogin = aUser.getUserLogin();

        // Get the path information from the request object.
        String       pathInfo     = aRequest.getPathInfo();
        BusinessArea ba           = null;
        Shortcut     shortcut     = null;
        String       shortcutName = "";
        boolean      isDefault    = false;
        boolean      isMyRequests = false;

        /*
         * If path info is null, then
         *      BA        - Default BA configured by the user. If this is not
         *                  valid, then consider the first one in the list.
         *      Shortcut  - Default shortcut, if any, configured by the user in
         *                  this BA.
         */
        
        if (null == pathInfo) {
        	String sysPrefix = getDefaultBA(aUser);
            /*
             * Get the BA Object corresponding to this prefix. We would like
             * to get the first BA in the list if this prefix is invalid. So,
             * the second param to getBAObject method is set to true. The third
             * params instructs the method to throw exception if the prefix is
             * invalid.
             */
            ba = getBAObject(sysPrefix, true, true);

            // Get the sysPrefix value again from the BA object.
            sysPrefix = ba.getSystemPrefix();

            /*
             * Get the default shortcut, if any, configured by this user in
             * this business area.
             */
            BAConfig baconfig = aConfig.getBAConfig(sysPrefix);

            shortcutName = baconfig.getDefaultShortcutName();
            isDefault    = true;
        } else {

            // Check if the pathInfo starts with / or \. If so remove that.
            if (pathInfo.startsWith("/") || pathInfo.startsWith("\\")) {
                pathInfo = pathInfo.substring(1);
            }

            // If there is nothing in the path info.
            if (pathInfo.trim().equals("") == true) {
                String sysPrefix = getDefaultBA(aUser);

                ba = getBAObject(sysPrefix, true, true);

                // Get the sysPrefix value again from the BA object.
                sysPrefix = ba.getSystemPrefix();

                /*
                 * Get the default shortcut, if any, configured by this user in
                 * this business area.
                 */
                BAConfig baconfig = aConfig.getBAConfig(sysPrefix);

                shortcutName = baconfig.getDefaultShortcutName();
                isDefault    = true;
            } else {

                // Split the path info and process each part.
                String[] arr = pathInfo.split("\\/");

                if (arr.length > 0) {

                    /*
                     * The first part in the path info can be
                     *  1. BA Prefix
                     *  2. My Requests
                     */
                    String str = arr[0];

                    if (true == str.equalsIgnoreCase("my-requests")) {
                        isMyRequests = true;

                        String sysPrefix = getDefaultBA(aUser);

                        ba = getBAObject(sysPrefix, true, true);
                    } else {

                        // Check if this corresponds to a valid BA Prefix.
                        ba = getBAObject(str, false, true);

                        String sysPrefix = ba.getSystemPrefix();

                        /*
                         * Get the default shortcut, if any, configured by this
                         * user in this business area. If the user specified any
                         * other shortcut in the pathinfo, this will be
                         * overwritten in the next step.
                         */
                        BAConfig baconfig = aConfig.getBAConfig(sysPrefix);

                        shortcutName = baconfig.getDefaultShortcutName();
                        isDefault    = true;
                    }
                }

                /*
                 * We consider the other parts in the path info only if this is
                 * not a request to display my-requests page.
                 */
                if ((isMyRequests == false) && (arr.length > 1)) {

                    /*
                     * The second element in the path info can be
                     * 1. This user's Shortcut in the BA.
                     * 2. Some other user's shortcut in this BA.
                     * 3. my-requests.
                     */
                    String str = arr[1];

                    if (str.equalsIgnoreCase("my-requests") == true) {
                        isMyRequests = true;
                    } else {
                        shortcutName = str;
                    }
                }
            }
        }

        result.put(Field.BUSINESS_AREA, ba);

        if ((shortcutName != null) && (shortcutName.equals("") == false)) {
            String sysPrefix = ba.getSystemPrefix();

            shortcut = ShortcutHandler.getShortcutByName(userLogin, sysPrefix, shortcutName);

            /*
             * If the shortcut name could not be resolved to a shortcut object
             * throw out an exception only if the shortcut name was not taken
             * from user's default value. This is needed for following case:
             *
             *  A BA admin defines a BA_level shortcut and this user configures
             *  that as his default shortcut. The BA admin can delete the
             *  shortcut in which case it wouldn't be deleted from this user's
             *  config. So instead of throwing an exception, we ignore that.
             *
             */
            if (null == shortcut) {
                if (isDefault == false) {
                    throw new TBitsException(Messages.getMessage("NO_SEARCH_SHORTCUT", ba.getDisplayName(), shortcutName));
                }
            }
        }

        if (shortcut != null) {
            result.put("SHORTCUT", shortcut);
        }

        if (true == isMyRequests) {
            result.put("MY-REQUESTS", true);
        }

        return result;
    }

    /*
     * If the default BA in Config is  invalid or user doesnt have view permission on it
     *  then find the first available BA. 
     */
    private String getDefaultBA(User user) throws DatabaseException
    {
    	
    	WebConfig aConfig = user.getWebConfigObject();
    	String sysPrefix = aConfig.getSystemPrefix();
    	if(sysPrefix != null)
    	{
    		BusinessArea ba = BusinessArea.lookupBySystemPrefix(sysPrefix);
    		if((ba != null))
    			{
	    			Hashtable<String, Integer> permTable = RolePermission.getPermissionsBySystemIdAndUserId(ba.getSystemId(), user.getUserId());        		
	    			if(hasPermission(ba, permTable))
	    			{
		    			return sysPrefix;
	    			}
    			}
    	}
    	ArrayList<BusinessArea> bas = BusinessArea.getBusinessAreasByUserId(user.getUserId());
    	for(BusinessArea ba:bas)
    	{
    		Hashtable<String, Integer> permTable = RolePermission.getPermissionsBySystemIdAndUserId(ba.getSystemId(), user.getUserId());
    		if(hasPermission(ba, permTable))
    		{
    			return ba.getSystemPrefix();
    		}
    	}
    	return sysPrefix;
    }
    
    /**
     * This method renders the Advanced View of search page.
     *
     * @param aTagTable Tags related to Advanced View and their values are put
     *                  in this table.
     * @param aQuery    Query
     */
    private void renderAdvancedView(HttpServletRequest aRequest, Hashtable<String, String> aTagTable, String aQuery) {
        if ((aQuery == null) || aQuery.trim().equals("")) {
            aQuery = getCookieValue(aRequest, "advancedQuery");
        }

        if ((aQuery == null) || aQuery.trim().equals("")) {
            aQuery = "";
        } else {
            aQuery = aQuery.trim();
        }

        aTagTable.put("advancedQuery", Utilities.htmlEncode(aQuery));

        return;
    }

    /**
     * This method renders the All Areas View of search page.
     * @param aRequest TODO
     * @param aBAList     List of business areas.
     * @param aUser       User.
     * @param aTextFilter current text filter.
     */
    private static String renderAllBAView(HttpServletRequest aRequest, BusinessArea aBA, ArrayList<BusinessArea> aBAList, User aUser, String aTextFilter) throws DatabaseException, FileNotFoundException, IOException {
        DTagReplacer              hp       = new DTagReplacer(ALL_AREAS_FILE);
        Hashtable<String, String> tagTable = new Hashtable<String, String>();

        tagTable.put("aa_textFilters", SearchRenderer.renderTextFilters(aTextFilter));
        tagTable.put("aa_business_areas", renderBAList(aBAList));

        StringBuffer sysIdList = new StringBuffer();
        boolean      first     = true;

        for (BusinessArea ba : aBAList) {
            if (first == false) {
                sysIdList.append(",");
            } else {
                first = false;
            }

            sysIdList.append(Integer.toString(ba.getSystemId()));
        }

        Hashtable<String, ArrayList<String>> table    = Type.getStatusAndSeverities(sysIdList.toString());
        ArrayList<String>                    statList = table.get(Field.STATUS);
        ArrayList<String>                    sevList  = table.get(Field.SEVERITY);

        int systemId = aBA.getSystemId();
        Field field = Field.lookupBySystemIdAndFieldName(systemId, Field.DUE_DATE);
        String label = (field == null)
                ? "Due Date"
                : field.getDisplayName();
        tagTable.put("due_date_label", label);
        
        field = Field.lookupBySystemIdAndFieldName(systemId, Field.LASTUPDATED_DATE);
        label = (field == null)
                ? "Last Modified"
                : field.getDisplayName();
        tagTable.put("last_modified_label", label);
        
        field = Field.lookupBySystemIdAndFieldName(systemId, Field.LOGGED_DATE);
        label = (field == null)
                ? "Submitted"
                : field.getDisplayName();
        tagTable.put("submitted_label", label);
        
        tagTable.put("aa_status_id_list", renderStatusList(statList));
        tagTable.put("aa_severity_id_list", renderSeverityList(sevList));
        tagTable.put("sysPrefix", aBA.getSystemPrefix());
        tagTable.put("nearestPath", WebUtil.getNearestPath(aRequest, ""));

        // Other tags will be empty.
        SearchRenderer.replaceTags(hp, tagTable, ourAllAreasTagList);

        return hp.parse(systemId);
    }

    /**
     * This method renders the BA list.
     *
     * @param  aBAList  List of business areas.
     *
     * @return HTML.
     */
    private static String renderBAList(ArrayList<BusinessArea> aBAList) throws DatabaseException {
        if ((aBAList == null) || (aBAList.size() == 0)) {
            return "";
        }

        StringBuffer list = new StringBuffer();

        list.append("<OPTION value='<%PrefixList%>'>All</OPTION>");

        StringBuffer prefixList = new StringBuffer();
        boolean      first      = true;

        for (BusinessArea ba : aBAList) {
            int    sysId  = ba.getSystemId();
            String prefix = ba.getSystemPrefix();

            if (first == false) {
                prefixList.append(",");
            } else {
                first = false;
            }

            prefixList.append(prefix);
            list.append("\n<OPTION value='").append(prefix).append("' SELECTED>").append(ba.getDisplayName()).append(" [").append(prefix).append("]</OPTION>");
        }

        String value = list.toString().replaceAll("<%PrefixList%>", prefixList.toString());

        return value;
    }

    /**
     * This method returns the list of Business Areas the user can view with
     * the specified BA preselected.
     *
     * @param  aSystemId  BA to be pre-selected.
     * @param  aBAList      Out param that holds the list of BAs.
     *
     * @return List of Business Area objects.
     */
    private String renderBAList(int aSystemId, ArrayList<BusinessArea> aBAList) throws DatabaseException {
        StringBuffer list = new StringBuffer();

        if (aBAList != null) {
            for (BusinessArea ba : aBAList) {
                int sysId = ba.getSystemId();

                list.append("\n<OPTION value='").append(ba.getSystemPrefix()).append("'");

                if (aSystemId == sysId) {
                    list.append(" SELECTED ");
                }

                list.append(">").append(ba.getDisplayName()).append(" [").append(ba.getSystemPrefix()).append("]").append((ba.getIsPrivate() == true)
                        ? "&nbsp;&dagger;"
                        : "").append("</OPTION>");
            }
        }

        return list.toString();
    }

    /**
     * This method returns the options to be present in the Group Actions
     * drop down in the interface.
     *
     * @param pTable  Permission table.
     * @param tTable  Tag Table that holds the values of the tags present in
     *                the html interface.
     * @param config   User Config.
     *
     * @return True  If the group action interface should be rendered
     *         False If the group action interface should not be rendered.
     */
    private static boolean renderGroupActionInterface(Hashtable<String, Integer> pTable, Hashtable<String, String> tTable, BAConfig config) {
        StringBuffer buffer    = new StringBuffer();
        boolean      grpAction = false;

        if (WebUtil.canChange(pTable, Field.STATUS) == true) {
            grpAction = true;
            buffer.append("\n<OPTION value='1'>Closed</OPTION>");
        }

        if (WebUtil.canChange(pTable, Field.IS_PRIVATE) == true) {
            grpAction = true;
            buffer.append("\n<OPTION value='2'>Private</OPTION>");
            buffer.append("\n<OPTION value='3'>Public</OPTION>");
        }

        if (config.getEnableVE() == true) {
            grpAction = true;
            buffer.append("\n<OPTION value='4'>Read</OPTION>");
            buffer.append("\n<OPTION value='5'>Unread</OPTION>");
        }

        if (grpAction == true) {
            tTable.put("groupActionDisplay", "inline-block");
            tTable.put("groupActionList", buffer.toString());
        } else {
            tTable.put("groupActionDisplay", "NONE");
            tTable.put("groupActionList", "");
        }

        return grpAction;
    }

    /**
     * This page renders the normal view of search page.
     *
     * @param aBA          Business Area object.
     * @param aUser        User object.
     * @param aTextFilter  Text filter.
     * @param aIsPrivate   True if user can view private items.
     * @param aParseTable  Parse table.
     * @param aTagTable    Tagtable.
     *
     * @throws DatabaseException Incase of database related errors.
     */
    private void renderNormalView(BusinessArea aBA, User aUser, String aTextFilter, boolean aIsPrivate, Searcher aSearcher, Hashtable<String, String> aTagTable, HttpServletRequest aRequest)
            throws DatabaseException {
        int                   systemId    = aBA.getSystemId();
        String                sysPrefix   = aBA.getSystemPrefix();
        WebConfig             webConfig   = aUser.getWebConfigObject();
        BAConfig              baConfig    = webConfig.getBAConfig(sysPrefix);
        boolean               isVEEnabled = baConfig.getEnableVE();
        ArrayList<ParseEntry> aParseTable = (aSearcher == null)
                ? null
                : aSearcher.getParseTable();

        // First replace the labels with display names.
        replaceLabels(systemId, aTagTable);
        aTagTable.put("dateDivDisplay", "display: NONE");

        if (aParseTable == null) {
            defaultRenderer(systemId, isVEEnabled, aIsPrivate, aTagTable, aRequest);
        } else {
            Hashtable<String, ParseEntry> table = new Hashtable<String, ParseEntry>();

            for (ParseEntry pe : aParseTable) {
                String fieldName = pe.getFieldName().toLowerCase();

                table.put(fieldName, pe);
            }

            ParseEntry                 pe         = null;
            Connective                 connective = Connective.C_AND;
            Hashtable<String, Boolean> argTable   = null;

            // Check category.
            pe = table.get(Field.CATEGORY.toLowerCase());

            if (pe != null) {
                argTable   = Utilities.toHash(pe.getArgList());
                connective = pe.getConnective();
            } else {
                argTable   = new Hashtable<String, Boolean>();
                connective = Connective.C_AND;
            }

            aTagTable.put(Field.CATEGORY + "_list", getTypeList(systemId, Field.CATEGORY, aIsPrivate, connective, argTable));

            // Check status.
            pe = table.get(Field.STATUS.toLowerCase());

            if (pe != null) {
                argTable   = Utilities.toHash(pe.getArgList());
                connective = pe.getConnective();
            } else {
                argTable   = new Hashtable<String, Boolean>();
                connective = Connective.C_AND;
            }

            aTagTable.put(Field.STATUS + "_list", getTypeList(systemId, Field.STATUS, aIsPrivate, connective, argTable));

            // Check severity.
            pe = table.get(Field.SEVERITY.toLowerCase());

            if (pe != null) {
                argTable   = Utilities.toHash(pe.getArgList());
                connective = pe.getConnective();
            } else {
                argTable   = new Hashtable<String, Boolean>();
                connective = Connective.C_AND;
            }

            aTagTable.put(Field.SEVERITY + "_list", getTypeList(systemId, Field.SEVERITY, aIsPrivate, connective, argTable));

            // Check request type.
            pe = table.get(Field.REQUEST_TYPE.toLowerCase());

            if (pe != null) {
                argTable   = Utilities.toHash(pe.getArgList());
                connective = pe.getConnective();
            } else {
                argTable   = new Hashtable<String, Boolean>();
                connective = Connective.C_AND;
            }

            aTagTable.put(Field.REQUEST_TYPE + "_list", getTypeList(systemId, Field.REQUEST_TYPE, aIsPrivate, connective, argTable));

            // Check office.
            pe = table.get(Field.OFFICE.toLowerCase());

            if (pe != null) {
                argTable   = Utilities.toHash(pe.getArgList());
                connective = pe.getConnective();
                aTagTable.put("dateDivDisplay", "");
            } else {
                argTable   = new Hashtable<String, Boolean>();
                connective = Connective.C_AND;
            }

            aTagTable.put(Field.OFFICE + "_list", getTypeList(systemId, Field.OFFICE, aIsPrivate, connective, argTable));

            // Check the user fields.
            // Check Logger.
            pe = table.get(Field.LOGGER.toLowerCase());

            if (pe != null) {
                aTagTable.put(Field.LOGGER, Utilities.htmlEncode(toCSS(pe.getArgList())));
            }

            // Check Assignee.
            pe = table.get(Field.ASSIGNEE.toLowerCase());

            if (pe != null) {
                aTagTable.put(Field.ASSIGNEE, Utilities.htmlEncode(toCSS(pe.getArgList())));
            }

            // Check Subscriber.
            pe = table.get(Field.SUBSCRIBER.toLowerCase());

            if (pe != null) {
                aTagTable.put(Field.SUBSCRIBER, Utilities.htmlEncode(toCSS(pe.getArgList())));
            }

            // Check User.
            pe = table.get(Field.USER.toLowerCase());

            if (pe != null) {
                aTagTable.put(Field.USER, Utilities.htmlEncode(toCSS(pe.getArgList())));
            }

            // Check the text field.
            pe = table.get(Field.SUBJECT.toLowerCase());

            if (pe != null) {
                aTagTable.put("normalDesc", Utilities.htmlEncode(toCSS(pe.getArgList())));
            }

            // Check the date fields.
            pe = table.get(Field.LOGGED_DATE.toLowerCase());

            if (pe != null) {
                aTagTable.put("log_defClause", pe.getClause());
                aTagTable.put("log_defValue", Utilities.htmlEncode(Utilities.toCSS(pe.getExtraInfo())));
                aTagTable.put("dateDivDisplay", "");
            }

            pe = table.get(Field.LASTUPDATED_DATE.toLowerCase());

            if (pe != null) {
                aTagTable.put("upd_defClause", pe.getClause());
                aTagTable.put("upd_defValue", Utilities.htmlEncode(Utilities.toCSS(pe.getExtraInfo())));
                aTagTable.put("dateDivDisplay", "");
            }

            pe = table.get(Field.DUE_DATE.toLowerCase());

            if (pe != null) {
                aTagTable.put("due_defClause", pe.getClause());
                aTagTable.put("due_defValue", Utilities.htmlEncode(Utilities.toCSS(pe.getExtraInfo())));
                aTagTable.put("dateDivDisplay", "");
            }

            pe = table.get(SearchConstants.READ_REQUEST);

            if (pe == null) {
                pe = table.get(SearchConstants.UNREAD_REQUEST);
            }

            if (pe != null) {
                Connective        conn    = pe.getConnective();
                ArrayList<String> argList = pe.getArgList();
                boolean           arg     = false;
                int               flag    = ALL_REQUESTS;

                if ((argList == null) || (argList.size() == 0)) {
                    if ((conn == Connective.C_AND_NOT) || (conn == Connective.C_OR_NOT) || (conn == Connective.C_NOT)) {
                        flag = READ_REQUESTS;
                    } else {
                        flag = UNREAD_REQUESTS;
                    }
                } else {
                    if (argList.get(0).equals("0")) {
                        flag = UNREAD_REQUESTS;
                    } else {
                        flag = READ_REQUESTS;
                    }
                }

                aTagTable.put("readFilter", getReadUnreadFilter(flag, isVEEnabled));
            } else {
                aTagTable.put("readFilter", getReadUnreadFilter(ALL_REQUESTS, isVEEnabled));
            }

            /*
             * Check if any text entries are present in the parse table.
             */
            ArrayList<String>     argList     = null;
            ArrayList<ParseEntry> textEntries = aSearcher.getTextEntries();

            if ((textEntries != null) && (textEntries.size() > 0)) {
                try {
                    if (textEntries.size() == 1) {

                        /*
                         * If the size of text entry list is one, it means
                         * the text filter selected is either subject or
                         * summary. If it is none of these, then assume
                         * it to be alltext.
                         */
                        ParseEntry entry     = textEntries.get(0);
                        String     fieldName = entry.getFieldName();

                        if (fieldName.equals(Field.SUBJECT)) {
                            aTextFilter = "subject";
                        } else if (fieldName.equals(Field.SUMMARY)) {
                            aTextFilter = "summary";
                        } else {
                            aTextFilter = "alltext";
                        }

                        argList = entry.getArgList();
                    } else {

                        /*
                         * In case of multiple text entries, the text filter
                         * might be alltext or all. Iterate through the list
                         * and the moment we find attachment field, we set
                         * the textfilter to "all" and come out of the loop.
                         */
                        aTextFilter = "alltext";

                        for (ParseEntry entry : textEntries) {
                            argList = entry.getArgList();

                            String fieldName = entry.getFieldName();

                            if (fieldName.equals(Field.ATTACHMENTS)) {
                                aTextFilter = "all";

                                break;
                            }
                        }
                    }

                    if (argList != null) {
                        StringBuffer buffer = new StringBuffer();

                        for (String str : argList) {

                            /*
                             * remove any leading +/-
                             */
                            if (str.startsWith("+") || str.startsWith("-")) {
                                str = str.substring(1);
                            }

                            /*
                             * If the argument contains spaces, enclose it in
                             * quotes.
                             */
                            if (str.indexOf(" ") > 0) {
                                str = "\"" + str + "\"";
                            }

                            buffer.append(str).append(" ");
                        }

                        aTagTable.put("normalDesc", Utilities.htmlEncode(buffer.toString().trim()));
                    }
                } catch (Exception e) {
                    LOG.info("",(e));
                }
            }
        }

        // Get the textFilters.
        aTagTable.put("textFilters", SearchRenderer.renderTextFilters(aTextFilter));
        Field field = Field.lookupBySystemIdAndFieldName(systemId, Field.DUE_DATE);
        String label = (field == null)
                ? "Due Date"
                : field.getDisplayName();
        aTagTable.put("due_date_label", label);
        
        field = Field.lookupBySystemIdAndFieldName(systemId, Field.LASTUPDATED_DATE);
        label = (field == null)
                ? "Last Modified"
                : field.getDisplayName();
        aTagTable.put("last_modified_label", label);
        
        field = Field.lookupBySystemIdAndFieldName(systemId, Field.LOGGED_DATE);
        label = (field == null)
                ? "Submitted"
                : field.getDisplayName();
        aTagTable.put("submitted_label", label);
        
        return;
    }

    /**
     * This method returns the results rendered in HTML.
     *
     * @param aBA          Business Area.
     * @param aConfig      User Config.
     * @param aSearcher    Searcher object.
     * @param aZone        User Preferred Zone.
     * @param aRenderType  Type of rendering.
     * @param aHtml        Out parameter that holds the results.
     * @param aFlags       Flags.
     * @param aFormat      Format of results output.
     * @param aRequest TODO
     * @return List of requestIds.
     */
    static ArrayList<Integer> renderResults(BusinessArea aBA, WebConfig aConfig, Searcher aSearcher, TimeZone aZone, RenderType aRenderType, StringBuffer aHtml, int aFlags, OutputFormat aFormat, HttpServletRequest aRequest) {
        ArrayList<Integer> reqIdList = new ArrayList<Integer>();

        switch (aRenderType) {
        case RENDER_FLAT :
            flatRenderer(aBA, aConfig, aSearcher, aZone, aHtml, reqIdList, aFlags, aFormat, aRequest.getContextPath());

            break;

        case RENDER_HIER :
            hierRenderer(aBA, aConfig, aSearcher, aZone, aHtml, reqIdList, aFlags, aFormat, aRequest);

            break;
        }

        return reqIdList;
    }

    /**
     * This method renders the results in HTML format.
     *
     * @param searcher
     * @param aBA
     * @param aUser
     * @param aZone
     * @param aRenderType
     * @param aTagTable
     * @param aSession
     */
    private static void renderResultsInHTML(Searcher searcher, BusinessArea aBA, User aUser, TimeZone aZone, RenderType aRenderType, Hashtable<String, String> aTagTable, HttpServletRequest aRequest) {
        HttpSession session = aRequest.getSession();

        if (searcher == null) {

            // There is nothing much here that we can do. return to the caller.
            LOG.info("Searcher object is null.");

            return;
        }

        long startRender = System.currentTimeMillis();

        // Get the user details that are frequently used during the process.
        int       userId      = aUser.getUserId();
        String    userLogin   = aUser.getUserLogin();
        WebConfig userConfig  = aUser.getWebConfigObject();
        int       systemId    = aBA.getSystemId();
        String    displayName = aBA.getDisplayName();
        String    sysPrefix   = aBA.getSystemPrefix();
        BAConfig  baconfig    = userConfig.getBAConfig(sysPrefix);

        // Execution was successful. Get the output params of Searcher.
        ArrayList<String>          searchResults = searcher.getResultIdList();
        Hashtable<String, Integer> permTable     = searcher.getPermissionTable();
        Hashtable<String, Field>   fieldsTable   = searcher.getFieldsTable();
        ArrayList<String>          displayHeader = searcher.getDisplayHeader();
        String                     sortField     = searcher.getSortField();
        int                        sortOrder     = searcher.getSortOrder();

        // Buffers to hold the results, exception messages etc.,
        StringBuffer results   = new StringBuffer();
        StringBuffer errors    = new StringBuffer();
        StringBuffer comment   = new StringBuffer();
        int          aSystemId = aBA.getSystemId();

        if ((searchResults == null) || (searchResults.size() == 0)) {
            comment.append("<SPAN class='rcomment'>").append("No records found for the specified search ").append("criteria.</SPAN>");
            aTagTable.put("rcDisplay", "  ");
            aTagTable.put("groupActionDisplay", " NONE ");
            aTagTable.put("miscFuncDisplay", "NONE");
            aTagTable.put("pageNumberDisplay", "NONE");
            aTagTable.put("excepDisplay", " NONE");
            aTagTable.put("searchResults", results.toString());
            aTagTable.put("resultComment", comment.toString());
            aTagTable.put("pageNumber", "");
            aTagTable.put("listAllDisplay", "NONE");
            aTagTable.put("listAllChecked", "");
        } else {

            // Maximum page size.
            int maxPageSize = searcher.getMaximumPageSize();

            // Number of results in current page.
            int pageCount = searchResults.size();

            // Maximum number of results the current page can hold.
            int pageSize = searcher.getCurrentPageSize();

            // Total number of results that matched the criteria.
            int matchedCount = searcher.getTotalResultCount();

            // Current page number.
            int pageNumber = searcher.getPageNumber();

            // isListAll selected.
            boolean isListAll = searcher.getIsListAll();
            String  sessionId = "SearchSession_" + sysPrefix + "_" + System.currentTimeMillis();

            aTagTable.put("sessionId", sessionId);

            ArrayList<Integer> reqIdList            = null;
            int                flags                = JAVASCRIPT | ANCHOR | LINK_SUBJECT | MOUSEOVER;
            boolean            isOnlyResultsRequest = false;
            String             tmp                  = aRequest.getParameter("onlyResults");

            if ((tmp != null) && (tmp.trim().equals("true") == true)) {
                isOnlyResultsRequest = true;
            }

            boolean allowGroupAction = false;

            allowGroupAction = renderGroupActionInterface(permTable, aTagTable, baconfig);

            if ((allowGroupAction == true) && (isOnlyResultsRequest == false)) {
                flags = flags | GROUP_ACTION;
            }

            StringBuffer htmlResults = new StringBuffer();

            reqIdList = renderResults(aBA, userConfig, searcher, aZone, aRenderType, htmlResults, flags, OutputFormat.HTML, aRequest);

            String colGroup         = getColumnGroup(aBA, displayHeader, flags);
            String htmlHeader       = getHeader(aBA, searcher, SORT_LINK, SORT_ICON, flags, aRequest.getContextPath());
            String resultsTableName = sysPrefix + "_searchResults";

            results.append("\n<TABLE class='results' cellspacing='0' ").append("cellpadding='0' width='100%' border='0' ").append("name='")
            .append(resultsTableName).append("' id='").append(resultsTableName).append("'>").append(colGroup).append(htmlHeader)
            .append(htmlResults.toString()).append("\n</TABLE>");

            if (reqIdList != null) {
                session.setAttribute(sessionId, reqIdList);
            }

            comment.append(getResultComment(systemId, matchedCount, pageNumber, pageCount, pageSize));

            if (isListAll == true) {
                getPageNumbers(matchedCount, pageCount, pageNumber, aTagTable);

                if (matchedCount > pageSize) {
                    aTagTable.put("listAllDisplay", "INLINE");
                } else {
                    aTagTable.put("listAllDisplay", "NONE");
                }

                aTagTable.put("listAllChecked", "CHECKED");
            } else {
                getPageNumbers(matchedCount, pageSize, pageNumber, aTagTable);
                aTagTable.put("listAllChecked", "");

                if ((pageCount < 1000) && (matchedCount > pageSize)) {
                    aTagTable.put("listAllDisplay", "INLINE");
                } else {
                    aTagTable.put("listAllDisplay", "NONE");
                }
            }

            aTagTable.put("searchResults", results.toString());
            aTagTable.put("resultComment", comment.toString());
            aTagTable.put("rcDisplay", "  ");
            aTagTable.put("miscFuncDisplay", "INLINE-BLOCK");
            aTagTable.put("excepDisplay", " NONE");
        }

        long endRender     = System.currentTimeMillis();
        long interfaceTime = (endRender - startRender);

        aTagTable.put("interfaceTime", Long.toString(interfaceTime));
    }

    /**
     * This method returns the search results HTML with the shortcuts HTML
     * properly generated.
     *
     * @param aBA     Business Area Object.
     * @param aUser   User Object.
     *
     * @return HTML
     *
     * @exception FileNotFoundException
     * @exception IOException
     */
    private String renderSearchResults(BusinessArea aBA, User aUser, RenderType aRenderType) throws FileNotFoundException, IOException {
        int       systemId    = aBA.getSystemId();
        String    displayName = aBA.getDisplayName();
        String    sysPrefix   = aBA.getSystemPrefix();
        int       userId      = aUser.getUserId();
        WebConfig userConfig  = aUser.getWebConfigObject();

        // Get the user's configuration for this Business area.
        BAConfig     baconfig = userConfig.getBAConfig(sysPrefix);        
     
        DTagReplacer hp       = new DTagReplacer(RESULTS_FILE);

        hp.replace("sessionId", "");
        hp.replace("searchText", "");
        hp.replace("databaseTime", "");
        hp.replace("interfaceTime", "");
        hp.replace("resultComment", "");
        hp.replace("pageNumber", "");
        hp.replace("groupActionList", "");
        hp.replace("searchResults", "");
        hp.replace("exception", "");
        hp.replace("renderType", aRenderType.toString());
        hp.replace("expState", "false");
        hp.replace("isVEEnabled", Boolean.toString(baconfig.getEnableVE()));
        hp.replace("1_sortField", "");
        hp.replace("2_sortField", "");
        hp.replace("3_sortField", "");
        hp.replace("1_sortOrder", "");
        hp.replace("2_sortOrder", "");
        hp.replace("3_sortOrder", "");
        hp.replace("searchResultsDisplay", "NONE");
        hp.replace("rcDisplay", "NONE");
        hp.replace("pageNumberDisplay", "NONE");
        hp.replace("groupActionDisplay", "none");
        hp.replace("miscFuncDisplay", "NONE");
        hp.replace("excepDisplay", "NONE");
        if (PropertiesHandler.isTransmittalEnabled())
        	hp.replace("transmittal", "inline");
        else
        	hp.replace("transmittal", "NONE");
        
        return hp.parse(systemId);
    }

    /**
     * This method returns the given list of severities as Options in an HTML
     * SELECT with Low, Medium, High, Critical at the top followed by
     * the other severities.
     *
     * @param sevList List of Severities.
     *
     * @return HTML Options of these Severities.
     */
    private static String renderSeverityList(ArrayList<String> sevList) {
        StringBuffer buffer = new StringBuffer();

        buffer.append("\n<OPTION value=''>All</OPTION>").append("\n<OPTION value='Low' SELECTED>Low</OPTION>").append("\n<OPTION value='Medium' SELECTED>Medium</OPTION>").append(
            "\n<OPTION value='High' SELECTED>High</OPTION>").append("\n<OPTION value='Critical' SELECTED>Critical</OPTION>");

        for (String str : sevList) {
            String lstr = str.toLowerCase();

            if (lstr.equals("low") || lstr.equals("medium") || lstr.equals("high") || lstr.equals("critical")) {
                continue;
            }

            buffer.append("\n<OPTION SELECTED value='").append(str).append("'>").append(str).append("</OPTION>");
        }

        return buffer.toString();
    }

    /**
     * This method renders the Simple View of search page.
     *
     * @param aTagTable Tags related to Simple View and their values are put
     *                  in this table.
     * @param aQuery    Query
     */
    private void renderSimpleView(HttpServletRequest aRequest, Hashtable<String, String> aTagTable, String aQuery) {
        if ((aQuery == null) || aQuery.trim().equals("")) {
            aQuery = getCookieValue(aRequest, "simpleQuery");
        }

        if ((aQuery == null) || aQuery.trim().equals("")) {
            aQuery = "";
        } else {
            aQuery = aQuery.trim();
        }

        aTagTable.put("simpleQuery", Utilities.htmlEncode(aQuery));

        return;
    }

    /**
     * This method returns the given list of statuses as Options in an HTML
     * SELECT with Open, Active, Suspended, Closed at the top followed by
     * the other statuses.
     *
     * @param statList List of statuses.
     *
     * @return HTML Options of these statuses.
     */
    private static String renderStatusList(ArrayList<String> statList) {
        StringBuffer buffer = new StringBuffer();

        buffer.append("\n<OPTION value=''>All</OPTION>").append("\n<OPTION value='Open' SELECTED>Open</OPTION>").append("\n<OPTION value='Active' SELECTED>Active</OPTION>").append(
            "\n<OPTION value='Suspended'>Suspended</OPTION>").append("\n<OPTION value='Closed'>Closed</OPTION>");

        for (String str : statList) {
            String lstr = str.toLowerCase();

            if (lstr.equals("open") || lstr.equals("active") || lstr.equals("suspended") || lstr.equals("closed")) {
                continue;
            }

            buffer.append("\n<OPTION value='").append(str).append("'>").append(str).append("</OPTION>");
        }

        return buffer.toString();
    }

    /**
     * This method renders the Normal View of search page.
     *
     * @param systemId Id of the BA.
     * @param aTable   Tags related to Simple View and their values are put
     *                 in this table.
     */
    private void replaceLabels(int systemId, Hashtable<String, String> aTable) throws DatabaseException {
        Field  field = null;
        String label = "";

        // Category.
        field = Field.lookupBySystemIdAndFieldName(systemId, Field.CATEGORY);
        label = (field == null)
                ? "Category"
                : field.getDisplayName();
        aTable.put("category_id_label", label);

        // Status.
        field = Field.lookupBySystemIdAndFieldName(systemId, Field.STATUS);
        label = (field == null)
                ? "Status"
                : field.getDisplayName();
        aTable.put("status_id_label", label);

        // Severity.
        field = Field.lookupBySystemIdAndFieldName(systemId, Field.SEVERITY);
        label = (field == null)
                ? "Severity"
                : field.getDisplayName();
        aTable.put("severity_id_label", label);

        // Request Type.
        field = Field.lookupBySystemIdAndFieldName(systemId, Field.REQUEST_TYPE);
        label = (field == null)
                ? "Request Type"
                : field.getDisplayName();
        aTable.put("request_type_id_label", label);

        // Office.
        field = Field.lookupBySystemIdAndFieldName(systemId, Field.OFFICE);
        label = (field == null)
                ? "Office"
                : field.getDisplayName();
        aTable.put("office_id_label", label);

        // Logger.
        field = Field.lookupBySystemIdAndFieldName(systemId, Field.LOGGER);
        label = (field == null)
                ? "Logger"
                : field.getDisplayName();
        aTable.put("logger_ids_label", label);

        // Assignee.
        field = Field.lookupBySystemIdAndFieldName(systemId, Field.ASSIGNEE);
        label = (field == null)
                ? "Assignee"
                : field.getDisplayName();
        aTable.put("assignee_ids_label", label);

        // Subscriber.
        field = Field.lookupBySystemIdAndFieldName(systemId, Field.SUBSCRIBER);
        label = (field == null)
                ? "Subscriber"
                : field.getDisplayName();
        aTable.put("subscriber_ids_label", label);

        field = Field.lookupBySystemIdAndFieldName(systemId, Field.DUE_DATE);
        label = (field == null)
                ? "Due Date"
                : field.getDisplayName();
        aTable.put("due_date_label", label);
        
        field = Field.lookupBySystemIdAndFieldName(systemId, Field.LASTUPDATED_DATE);
        label = (field == null)
                ? "Last Modified"
                : field.getDisplayName();
        aTable.put("last_modified_label", label);
        
        field = Field.lookupBySystemIdAndFieldName(systemId, Field.LOGGED_DATE);
        label = (field == null)
                ? "Submitted"
                : field.getDisplayName();
        aTable.put("submitted_label", label);
        
        return;
    }

    /**
     * This method serializes the given request object to HTML.
     *
     * @param aBA          Business Area Object.
     * @param aResult      Result that should be rendered.
     * @param aConfig      User's configuration properties.
     * @param aFieldsTable
     * @param aHeader      User's configured header.
     * @param aPrefZone    User's Preferred Zone.
     * @param aMatched     If this is a matched request.
     * @param aIncomplete  true if the result is followed by incomplete hierarchy
     * @param aFlags       Flags that indicate the options needed on Request Id
     *                     field.
     * @param aFormat      Format of rendering.
     *
     * @return Serialized request.
     */
    private static String serialize(BusinessArea aBA, Result aResult, WebConfig aConfig, Hashtable<String, Field> aFieldsTable, ArrayList<String> aHeader, TimeZone aPrefZone, boolean aMatched,
                                   boolean aIncomplete, int aFlags, OutputFormat aFormat, String aContextPath) {
        if (aFormat == OutputFormat.XML) {
            return xmlSerialize(aBA, aResult, aConfig, aFieldsTable, aHeader, aPrefZone, aMatched, aIncomplete, aFlags);
        }

        return htmlSerialize(aBA, aResult, aConfig, aFieldsTable, aHeader, aPrefZone, aMatched, aIncomplete, aFlags, aContextPath);
    }

    /**
     * This method returns a comma-separated list of user logins from the given
     * arraylist.
     *
     * @param userList List of users.
     *
     * @return Comma-separated list of users.
     */
    private String toCSS(ArrayList<String> userList) {
        StringBuffer buffer = new StringBuffer();
        boolean      first  = true;

        for (String arg : userList) {
            arg = arg.toLowerCase();

            if (arg.equalsIgnoreCase("and") || arg.equalsIgnoreCase("or") || arg.equalsIgnoreCase("not")) {
                continue;
            }

            if (first == false) {
                buffer.append(",");
            } else {
                first = false;
            }

            buffer.append(arg);
        }

        return buffer.toString();
    }

    /**
     * This method serializes the request in XML.
     *
     * @param aBA
     * @param aResult
     * @param aConfig
     * @param aFieldsTable
     * @param aHeader
     * @param aPrefZone
     * @param aMatched
     * @param aIncomplete
     * @param aFlags
     *
     * @return XML corresponding to this search result.
     */
    public static String xmlSerialize(BusinessArea aBA, Result aResult, WebConfig aConfig, Hashtable<String, Field> aFieldsTable, ArrayList<String> aHeader, TimeZone aPrefZone, boolean aMatched,
                                      boolean aIncomplete, int aFlags) {

        // Buffer is the return value of this method.
        StringBuilder buffer = new StringBuilder();

        // Get the list of parameters generally needed.
        int       requestId = aResult.getRequestId();
        String    sysPrefix = aBA.getSystemPrefix();
        BAConfig  baConfig  = aConfig.getBAConfig(sysPrefix);
        SysConfig sysConfig = aBA.getSysConfigObject();

        // Retrieve the params related to visual enhancements.
        String    status       = aResult.getStatus().trim();
        String    severity     = aResult.getSeverity().trim();
        boolean   isPrivate    = aResult.getIsPrivate();
        int       maxActionId  = aResult.getMaxActionId();
        int       lastActionId = aResult.getLastActionId();
        Timestamp dueDate      = aResult.getDueDate();
        boolean   isNew        = false;
        boolean   isRead       = true;
        boolean   isMatched    = true;
        boolean   isOverDue    = false;
        boolean   isCritical   = false;

        /*
         * Row will be in bold if the user wanted us to track read/unread
         * requests and this result is updated since last visited and is not
         * closed.
         */
        if (baConfig.getEnableVE() == true) {
            if (lastActionId < maxActionId) {

                /*
                 * Mark this as unread if either
                 *   - the request is not closed (OR)
                 *   - the request is closed but updated in last 'N' days
                 *     where 'N' is a configuration for this BA.
                 */
                Timestamp updDate = (Timestamp) aResult.get(Field.LASTUPDATED_DATE);
                Timestamp today   = new Timestamp();
                long      n       = sysConfig.getMaxGrayPeriod();

                // N is in days convert into milliseconds.
                long maxAllowedDiff = n * 86400000;
                long actualDiff     = today.getTime() - updDate.getTime();

                status = (status == null)
                         ? ""
                         : status.trim();

                if ((status.equalsIgnoreCase("closed") == false) || (actualDiff < maxAllowedDiff)) {
                    isRead = false;
                }
            }

            /*
             * New image is needed if the request is new and no other action
             * happened on that since it is logged.
             */
            if ((lastActionId == 0) && (maxActionId == 1) &&!status.equalsIgnoreCase("closed")) {
                isNew = true;
            }
        }

        // Check if this is matched reques or an unmatched request.
        if (aMatched == UNMATCHED_RESULT) {
            isMatched = false;
        }

        // Check the severity level of the request.
        if ((severity != null) && severity.trim().equalsIgnoreCase("critical")) {
            isCritical = true;
        }

        if ((dueDate != null) && (status.equalsIgnoreCase("closed") == false)) {
            long dueTime = dueDate.getTime();
            long nowTime = Timestamp.getGMTNow().getTime();

            if (dueTime < nowTime) {
                isOverDue = true;
            }
        }

        buffer.append("\n<Result>");

        for (String fieldName : aHeader) {
            Field  field = null;
            Object value = aResult.get(fieldName);

            field = aFieldsTable.get(fieldName);

            if (field == null) {
                continue;
            }

            String displayName = field.getDisplayName();

            if (fieldName.equals(Field.REQUEST) == true) {
                buffer.append("<").append(fieldName).append("   isNew=\"").append(isNew).append("\" isRead=\"").append(isRead).append("\" isMatched=\"").append(isMatched).append(
                    "\" isOverDue=\"").append(isOverDue).append("\" isCritical=\"").append(isCritical).append("\" >").append(requestId).append("</").append(fieldName).append(">");

                continue;
            } else if (fieldName.equals(Field.SEVERITY) == true) {
                buffer.append("<").append(fieldName).append(">").append(severity).append("</").append(fieldName).append(">");

                continue;
            } else if (fieldName.equals(Field.SUBJECT) == true) {
                String strValue = ((value == null)
                                   ? ""
                                   : value.toString());

                if (strValue == null) {
                    strValue = "-";
                }

                buffer.append("<").append(fieldName).append("><![CDATA[").append(strValue).append("]]>").append("</").append(fieldName).append(">");

                continue;
            }

            int dataType = field.getDataTypeId();

            switch (dataType) {
            case BOOLEAN : {
                String strValue = (value == null)
                                  ? "-"
                                  : value.toString();

                buffer.append("<").append(fieldName).append(">").append(strValue).append("</").append(fieldName).append(">");
            }

            break;

            case DATE :
            case TIME :
            case DATETIME : {
                if (value instanceof Timestamp) {
                    Timestamp ts       = (Timestamp) value;
                    String    strValue = "";

                    if (ts == null) {
                        strValue = "-";
                    } else {
                        strValue = WebUtil.getDateInFormat(ts, aPrefZone, aConfig.getListDateFormat());
                    }

                    buffer.append("<").append(fieldName).append(">").append(strValue).append("</").append(fieldName).append(">");
                } else {
                    buffer.append("<").append(fieldName).append("/>");
                }
            }

            break;

            case INT : {
                buffer.append("<").append(fieldName).append(">");

                if (value == null) {
                    buffer.append("-");
                } else {
                    buffer.append(value.toString());
                }

                buffer.append("</").append(fieldName).append(">");
            }

            break;

            case REAL : {
                buffer.append("<").append(fieldName).append(">");

                if (value == null) {
                    buffer.append("-");
                } else {
                    buffer.append(value.toString());
                }

                buffer.append("</").append(fieldName).append(">");
            }

            break;

            case STRING :
            case TEXT : {
                buffer.append("<").append(fieldName).append("><![CDATA[ ");

                if (value == null) {
                    buffer.append("-");
                } else {
                    buffer.append(Utilities.htmlEncode(value.toString()));
                }

                buffer.append("]]></").append(fieldName).append(">");
            }

            break;

            case USERTYPE : {
                buffer.append("<").append(fieldName).append(">");

                if (value == null) {
                    buffer.append("-");
                } else {
                    String str = value.toString();

                    str = str.replaceAll("\\.transbittech\\.com", "");
                    buffer.append(str);
                }

                buffer.append("</").append(fieldName).append(">");
            }

            break;

            case TYPE : {
                String  strValue    = "";
                boolean typePrivate = false;

                if (value == null) {
                    strValue = "-";
                } else {
                    strValue = value.toString();

                    if (strValue.startsWith("+") == true) {
                        typePrivate = true;
                        strValue    = strValue.substring(1);
                    }
                }

                buffer.append("<").append(fieldName).append(" isPrivate=\"").append(typePrivate).append("\">").append(strValue).append("</").append(fieldName).append(">");
            }
            break;
            
            case ATTACHMENTS:
            {
//            	buffer.append("XXXX");
            }
            
            break;
            }
        }

        buffer.append("</Result>");

        return buffer.toString();
    }

    //~--- get methods --------------------------------------------------------

    /**
     * This method returns the Business area objects corresponding to the given
     * list of prefixes in ascending order of the display name of BAs.
     *
     * @param aPrefixList List of BA Prefixes.
     *
     * @return List of BA Objects in ascending order.
     */
    private ArrayList<BusinessArea> getBAList(ArrayList<String> aPrefixList) {
        ArrayList<BusinessArea> baList = new ArrayList<BusinessArea>();

        // Check if the prefixList is null or empty.
        if ((aPrefixList == null) || (aPrefixList.size() == 0)) {
            return baList;
        }

        for (String prefix : aPrefixList) {
            try {
                BusinessArea ba = BusinessArea.lookupBySystemPrefix(prefix);

                if (ba != null) {
                    baList.add(ba);
                }
            } catch (Exception e) {
                LOG.warn("",(e));
            }
        }

        BusinessArea.setSortField(BusinessArea.DISPLAYNAME);
        baList = BusinessArea.sort(baList);

        return baList;
    }

    /**
     * This method returns the BA object corresponding to the given sysPrefix.
     * If the sysPrefix is invalid and aFirst is true, then it returns the
     * first business area in the list. otherwise it throws a TBitsException,
     * only if the user prefers us to throw.
     *
     *
     * @param aSysPrefix Prefix of the BA.
     * @param aFirst     True if the first BA in the list can be returned if the
     *                   prefix does not correspond to a valid BA.
     * @param aThrow     True if the method should throw an exception if the
     *                   prefix does not correspond to a valid BA.
     * @return BusinessArea object.
     * @throws TBitsExceptionse prefix is not valid.
     */
    private BusinessArea getBAObject(String aSysPrefix, boolean aFirst, boolean aThrow) throws TBitsException, DatabaseException {
        
    	BusinessArea ba = BusinessArea.lookupBySystemPrefix(aSysPrefix);

        if (null == ba) {

            /*
             * The given prefix does not correspond to a valid BA. If the caller
             * wanted us to return the first BA in this case, do so. otherwise
             * throw an exception.
             */
            if (aFirst == true) {

                // Get the first BA in the list.
                ba = BusinessArea.getFirstActiveBusinessArea();

                // If this is null, then throw an exception if the caller wants.
                if ((null == ba) && (aThrow == true)) {
                    throw new TBitsException(Messages.getMessage("INVALID_BUSINESS_AREA", aSysPrefix));
                }
            } else {

                /*
                 * Caller wants us to check if the BA exists or otherwise throw
                 * an exception.
                 */
                if (aThrow == true) {
                    throw new TBitsException(Messages.getMessage("INVALID_BUSINESS_AREA", aSysPrefix));
                }
            }
        }

        return ba;
    }

    /**
     * This method returns the Column group information depending on the
     * list of fields that will be rendered in the search results.
     *
     * @param aBA     Business Area object.
     * @param aHeader Display Header
     *
     * @return ColGroup information.
     */
    public static String getColumnGroup(BusinessArea aBA, ArrayList<String> aHeader, int aFlags) {
        int          aSystemId  = aBA.getSystemId();
        StringBuffer sb         = new StringBuffer();
        int          perSubject = 35;
        int          perTotal   = 0;
        boolean      hasSubject = false;

        if ((aFlags & GROUP_ACTION) != 0) {
            sb.append("\n\t<COL width=\"40\" align=\"right\" />");
        }

        for (String fieldName : aHeader) {
            if (fieldName.equals(Field.REQUEST)) {
                if ((aFlags & NO_HIERARCHY) == 0) {
                    sb.append("\n\t<COL width=\"40\" ").append("align=\"right\" nowrap='true'/>");
                }

                sb.append("\n\t<COL width=\"10%\" />");
                perTotal += 13;
            } else if (fieldName.equals(Field.SEVERITY)) {
                if ((aFlags & TEXT_SEVERITY) != 0) {
                    sb.append("\n\t<COL width=\"6%\" />");
                } else {
                    sb.append("\n\t<COL width=\"20\" />");
                }
            } else if (fieldName.equals(Field.SUBJECT)) {
                sb.append("\n\t<COL width=\"SUBJECT%\" />");
                hasSubject = true;
            } else {
                Field field = null;

                try {
                    field = Field.lookupBySystemIdAndFieldName(aSystemId, fieldName);

                    if (field == null) {
                        LOG.warn("Field not found for " + fieldName);

                        continue;
                    }
                } catch (DatabaseException de) {
                    LOG.warn("Field not found for " + fieldName + "",(de));

                    continue;
                }

                /*
                 * Width is based on the datatype of the field.
                 *      BIT      3%
                 *      DATE    10%
                 *      NUMERIC  3%
                 *      STRING  10%
                 *      TYPE     6%
                 *      USER     7%
                 */
                int dataType = field.getDataTypeId();

                switch (dataType) {
                case BOOLEAN :
                    sb.append("\n\t<COL width=\"3%\" />");
                    perTotal += 3;

                    break;

                case DATE :
                case TIME :
                case DATETIME :
                    sb.append("\n\t<COL width=\"10%\" />");
                    perTotal += 10;

                    break;

                case INT :
                case REAL :
                    sb.append("\n\t<COL width=\"3%\" />");
                    perTotal += 3;

                    break;

                case STRING :
                case TEXT :
                    sb.append("\n\t<COL width=\"10%\" />");
                    perTotal += 10;

                    break;

                case TYPE :
                    sb.append("\n\t<COL width=\"6%\" />");
                    perTotal += 6;
                    break;

                case USERTYPE :
                    sb.append("\n\t<COL width=\"7%\" />");
                    perTotal += 7;

                    break;
                case ATTACHMENTS :
                    sb.append("\n\t<COL width=\"3%\" />");
                    perTotal += 7;
                    break;
                }
            }
        }

        String colGroup = sb.toString();

        // Check if the subject is a part of the Display Header.
        if (hasSubject == true) {
            perSubject = (((100 - perTotal) > perSubject)
                          ? (100 - perTotal)
                          : perSubject);
            colGroup   = colGroup.replace("SUBJECT", Integer.toString(perSubject));
        } else {

            /*
             * If subject is not present, set the width of the last column to
             * '*'.
             */
            int start = colGroup.lastIndexOf(" width=\"");

            if (start > 0) {
                colGroup = colGroup.substring(0, start) + " width=\"*\" />";
            }
        }

        return colGroup;
    }

    /**
     * This method returns the comment corresponding to the given filter
     * when no requests are found.
     *
     */
    private String getComment(int filter) {
        StringBuffer message = new StringBuffer();

        /*
         * If the user did not select anything in the Options page to filter
         * his requests, consider all the filters.
         */
        if (filter == 0) {
            filter = FILTER_LOGGER + FILTER_ASSIGNEE + FILTER_SUBSCRIBER + FILTER_PRIMARY_ASSIGNEE;
        }

        boolean first = true;

        if ((filter & FILTER_LOGGER) != 0) {
            message.append(Messages.getMessage("NO_LOGGED_MYACTIVITIES"));
            first = false;
        }

        if ((filter & FILTER_ASSIGNEE) != 0) {
            if (first == false) {
                message.append("<BR>");
            }

            message.append(Messages.getMessage("NO_ASSIGNED_MYACTIVITIES"));
            first = false;
        }

        if ((filter & FILTER_SUBSCRIBER) != 0) {
            if (first == false) {
                message.append("<BR>");
            }

            message.append(Messages.getMessage("NO_SUBSCRIBED_MYACTIVITIES"));
            first = false;
        }

        if ((filter & FILTER_PRIMARY_ASSIGNEE) != 0) {
            if (first == false) {
                message.append("<BR>");
            }

            message.append(Messages.getMessage("NO_PRIMARY_ASSIGNED_MYACTIVITIES"));
            first = false;
        }

        return message.toString();
    }

    /**
     * This method searches the cookies for the given key and returns its value
     * if found, empty string otherwise.
     *
     * @param aRequest  Http Request object.
     * @param aKey      Cookie Key.
     * @return
     */
    private static String getCookieValue(HttpServletRequest aRequest, String aKey) {
        String   value   = "";
        Cookie[] cookies = aRequest.getCookies();

        if (cookies == null) {
            return value;
        }

        for (int i = 0; i < cookies.length; i++) {
            String name = cookies[i].getName();

            if (name.equals(aKey)) {
                try {
                    value = URLDecoder.decode(cookies[i].getValue(), "UTF-8");
                } catch (Exception e) {
                    LOG.info("",(e));
                }

                break;
            }
        }

        return value;
    }

    /**
     * This method returns the default sortorder for the given field.
     *
     * @param aFieldName Field Name.
     *
     * @return Default Sort Order.
     */
    private static int getDefaultSortOrder(int aSystemId, String aFieldName) {

        // Check for exception cases.
        if (aFieldName.equals(Field.SEVERITY)) {
            return DESC_ORDER;
        } else if (aFieldName.equals(Field.STATUS)) {
            return ASC_ORDER;
        } else if (aFieldName.equals(Field.CATEGORY)) {
            return ASC_ORDER;
        } else if (aFieldName.equals(Field.IS_PRIVATE)) {
            return DESC_ORDER;
        }

        // Now the sorting is based on data type
        int   sortOrder = DESC_ORDER;
        Field field     = null;

        try {
            field = Field.lookupBySystemIdAndFieldName(aSystemId, aFieldName);
        } catch (Exception e) {}

        if (field == null) {
            return sortOrder;
        }

        int dataType = field.getDataTypeId();

        switch (dataType) {
        case BOOLEAN :
            break;

        case DATE :
        case TIME :
        case DATETIME :
            sortOrder = DESC_ORDER;

            break;

        case INT :
            sortOrder = DESC_ORDER;

            break;

        case REAL :
            sortOrder = DESC_ORDER;

            break;

        case STRING :
            sortOrder = ASC_ORDER;

            break;

        case TEXT :
            sortOrder = ASC_ORDER;

            break;

        case TYPE :
            sortOrder = ASC_ORDER;

            break;

        case USERTYPE :
            sortOrder = ASC_ORDER;

            break;
        }

        return sortOrder;
    }

    /**
     * This method returns the email link.
     *
     * @param email Email Address.
     *
     * @return Anchor element for an email.
     */
    static String getEmailLink(String email) {
        StringBuffer buffer = new StringBuffer();

        buffer.append("<A href=\"mailto:").append(email).append("\">").append(email).append("</A>");

        return buffer.toString();
    }

    /**
     * Method to get the Logged/Assgined options in the String format
     *
     * @param aTagTable Tagtable.
     * @param aFilter   My Requests filter.
     *
     */
    private static void getFilterList(Hashtable<String, String> aTagTable, int aFilter) {
        if ((aFilter & FILTER_LOGGER) != 0) {
            aTagTable.put("loggerChecked", " CHECKED ");
        } else {
            aTagTable.put("loggerChecked", "");
        }

        if ((aFilter & FILTER_ASSIGNEE) != 0) {
            aTagTable.put("assigneeChecked", " CHECKED ");
        } else {
            aTagTable.put("assigneeChecked", "");
        }

        if ((aFilter & FILTER_SUBSCRIBER) != 0) {
            aTagTable.put("subscriberChecked", " CHECKED ");
        } else {
            aTagTable.put("subscriberChecked", "");
        }

        if ((aFilter & FILTER_PRIMARY_ASSIGNEE) != 0) {
            aTagTable.put("primaryAssigneeChecked", " CHECKED ");
        } else {
            aTagTable.put("primaryAssigneeChecked", "");
        }

        return;
    }

    /**
     * This method returns the Display Header for the search results.
     *
     * @param aBA        Business Area.
     * @param aSearcher  Searcher object.
     * @param aSortLink  True if the column should be linked for sortlink.
     * @param aSortIcon  True if the sorticon should be rendered.
     * @param aFlags     Flags
     * @param aContextPath TODO
     * @return Display Header in HTML.
     */
    public static String getHeader(BusinessArea aBA, Searcher aSearcher, boolean aSortLink, boolean aSortIcon, int aFlags, String aContextPath) {
        int                      aSystemId   = aBA.getSystemId();
        String                   sysPrefix   = aBA.getSystemPrefix();
        int                      sortOrder   = aSearcher.getSortOrder();
        String                   sortField   = aSearcher.getSortField();
        ArrayList<String>        header      = aSearcher.getDisplayHeader();
        Hashtable<String, Field> fieldsTable = aSearcher.getFieldsTable();
        StringBuffer             buffer      = new StringBuffer();
        String                   sortIcon    = (sortOrder == DESC_ORDER)
                ? "down-white.gif"
                : "up-white.gif";
        String                   style       = "";
        
        String IMAGE_PATH;
        if(aContextPath == null)
        	IMAGE_PATH = WebUtil.getNearestPath(IMAGE_STATIC_PATH);
        else
        	IMAGE_PATH = WebUtil.getNearestPath(aContextPath, IMAGE_STATIC_PATH);

        if ((aFlags & HIDE_RESULT) != 0) {
            style = "style='display: none'";
        }

        buffer.append("<TR class=\"header\" ").append(style).append(">");

        if ((aFlags & GROUP_ACTION) != 0) {
            buffer.append("<TD><input id='chboxCheckAll' name='chboxCheckAll' ").append("type='checkbox' class='grp-chbox' ").append("onclick='checkAll()'/></TD>");
        }

        for (String fieldName : header) {
            Field field = null;

            field = fieldsTable.get(fieldName);

            if (field == null) {
                LOG.warn("Field not found for " + fieldName);

                continue;
            }

            fieldName = field.getName();

            /*
             * Class for the TD includes link only if column should be linked
             * for sorting purpose.
             */
            String clsName = (aSortLink == true)
                             ? "mht l"
                             : "b";

            if (fieldName.equals(Field.REQUEST) == true) {
                if ((aFlags & NO_HIERARCHY) == 0) {
                    buffer.append("<TD class=\"ri\"><img src=\"").append(IMAGE_PATH).append("node-spcr.gif\" /></TD>");
                }
            }

            buffer.append("\n\t\t<TD class=\"").append(clsName).append("\" ");

            if (aSortLink == true) {
                buffer.append("onclick=\"javascript:sortResults('").append(sysPrefix).append("', '").append(fieldName).append("', ").append(sortOrder).append(");\" ");
            }

            buffer.append(">");

            // Heading for Severity field is an Icon instead of display name.
            if (fieldName.equals(Field.SEVERITY)) {
//                if ((aFlags & TEXT_SEVERITY) != 0) {
                    buffer.append(field.getDisplayName());
//                } else {
//                    buffer.append("<img src=\"").append(IMAGE_PATH).append("severity-white.gif\" ").append("alt=\"Severity Level\" />");
//                }
            }/*else if (fieldName.equals(Field.REQUEST)){
            	buffer.append(Utilities.getCaptionBySystemId(aSystemId, CaptionsProps.CAPTIONS_ALL_CAMEL_CASE_REQUEST));
            }*/else {
                buffer.append(field.getDisplayName());
            }
            
            /*
             * Including the sort icon if this field is the one on which
             * sorting is performed.
             */
            if (fieldName.equalsIgnoreCase(sortField) && (aSortIcon == true)) {
                buffer.append("<IMG src=\"").append(IMAGE_PATH).append(sortIcon).append("\" />");
            }

            buffer.append("</TD>");
        }

        buffer.append("\n\t</TR>");

        return buffer.toString();
    }

    /**
     * This method returns true if the user has view permission on confidential
     * field in this business area..
     *
     * @param  aPermTable Permission table of this user.
     *
     * @return True  If the user has view on private field.
     *         False Otherwise.
     */
    public boolean getIsPrivate(Hashtable<String, Integer> aPermTable) {
        boolean isPrivate = false;
        Integer tmp       = aPermTable.get(Field.IS_PRIVATE);

        if (tmp != null) {

            // Check if the user has view on is_private.
            int permission = tmp.intValue();

            if ((permission & Permission.VIEW) != 0) {
                isPrivate = true;
            }
        } else {
            tmp = aPermTable.get("__LOGGER_PRIVATE__");

            /*
             * Check if the user has view on is_private by virtue of
             * being a logger.
             */
            if (tmp != null) {
                int permission = tmp.intValue();

                if ((permission & Permission.VIEW) != 0) {
                    isPrivate = true;
                }
            } else {
                tmp = aPermTable.get("__ASSIGNEE_PRIVATE__");

                /*
                 * Check if the user has view on is_private by virtue of
                 * being an assignee.
                 */
                if (tmp != null) {
                    int permission = tmp.intValue();

                    if ((permission & Permission.VIEW) != 0) {
                        isPrivate = true;
                    }
                } else {
                    tmp = aPermTable.get("__SUBSCRIBER_PRIVATE__");

                    /*
                     * Check if the user has view on is_private by virtue of
                     * being a subscriber.
                     */
                    if (tmp != null) {
                        int permission = tmp.intValue();

                        if ((permission & Permission.VIEW) != 0) {
                            isPrivate = true;
                        }
                    } else {
                        isPrivate = false;
                    }
                }
            }
        }

        return isPrivate;
    }

    /**
     * This method returns the output format reading it from the Request Object.
     * If not found, it returns the default value.
     *
     * @param aRequest   HttpRequest Object.
     *
     * @return Render Type value.
     */
    private OutputFormat getOutputFormat(HttpServletRequest aRequest) {
        OutputFormat format = OutputFormat.HTML;

        // Get the value from the query string.
        String strFormat = aRequest.getParameter("format");

        if ((strFormat == null) || strFormat.trim().equals("")) {
            format = OutputFormat.HTML;
        } else if (strFormat.equalsIgnoreCase("xls") || strFormat.equalsIgnoreCase("excel")) {
            format = OutputFormat.EXCEL;
        } else if (strFormat.equalsIgnoreCase("xml")) {
            format = OutputFormat.XML;
        } else if (strFormat.equalsIgnoreCase("rss")) {
            format = OutputFormat.RSS;
        } else {
            format = OutputFormat.HTML;
        }

        return format;
    }

    /**
     * This method returns the pageNumber if found in the request object, else
     * 1.
     *
     * @param aRequest Http Request object.
     * @return 1 or the page number found in the request object.
     */
    private int getPageNumber(HttpServletRequest aRequest) {
        int    pageNumber    = 1;
        String strPageNumber = aRequest.getParameter("pageNumber");

        if ((strPageNumber != null) &&!strPageNumber.trim().equals("")) {
            try {
                pageNumber = Integer.parseInt(strPageNumber);
            } catch (Exception e) {
                LOG.info("",(e));
            }
        }

        return pageNumber;
    }

    /**
     * This method generates the page numbers
     *
     * @param resultCount  No of results that matched the search criteria.
     * @param PageSize     No of results in a page.
     * @param pageNumber   Current page number.
     * @param aTagTable    Tagtable.
     *
     */
    private static void getPageNumbers(int matchedCount, int pageSize, int pageNumber, Hashtable<String, String> aTagTable) {

        // Case 1: Result count <= pageSize
        if (matchedCount <= pageSize) {
            aTagTable.put("pageNumberDisplay", "NONE");
            aTagTable.put("pageNumber", "");
        }

        // Case 2: Result Count > pageSize
        else if (matchedCount > pageSize) {
            StringBuffer buffer = new StringBuffer();

            aTagTable.put("pageNumberDisplay", "INLINE-BLOCK");

            int pageCount = matchedCount / pageSize;

            if (matchedCount % pageSize != 0) {
                pageCount = pageCount + 1;
            }

            for (int i = 1; i <= pageCount; i++) {
                buffer.append("\n<OPTION value=").append(i).append((i == pageNumber)
                        ? " SELECTED "
                        : "").append(">").append(i).append("</OPTION>");
            }

            aTagTable.put("pageNumber", buffer.toString());
        }

        aTagTable.put("pageCounter", Integer.toString((pageNumber - 1) * pageSize));

        return;
    }

    /**
     * This method returns the primary descriptor table in JSON for given BA.
     *
     * @param aSystemId Business Area ID.
     *
     * @return table in JSON
     */
    private static String getPrimaryDescTableInJSON(int aSystemId) {
        StringBuffer buffer = new StringBuffer();

        try {
            Hashtable<String, String> ptable = FieldDescriptor.getPrimaryDescTable(aSystemId);
            Enumeration<String>       flist  = ptable.keys();
            boolean                   first  = true;

            buffer.append("{");

            while (flist.hasMoreElements()) {
                String fieldName   = flist.nextElement();
                String primaryDesc = ptable.get(fieldName);

                if (first == false) {
                    buffer.append(",");
                } else {
                    first = false;
                }

                buffer.append("\"").append(fieldName).append("\":\"").append(primaryDesc).append("\"");
            }

            if (first == false) {
                buffer.append(",");
            } else {
                first = false;
            }

            buffer.append("\"updater\":\"appender\"");

            /*
             * Add the read/unread option as a field for the advance search
             * builder to include this in the drop down.
             */
            buffer.append(",");
            buffer.append("\"read\":\"read\"");
            buffer.append("}");
        } catch (Exception e) {
            LOG.warn("",(e));
        }

        return buffer.toString();
    }

    /**
     * This method returns the DSQL query if present in the Query String of the
     * Http Request.
     *
     * @param aRequest HttpServletRequest Object.
     *
     * @return DSQLQuery if present. Null otherwise.
     */
    private String getQuery(HttpServletRequest aRequest) {
        String query = aRequest.getParameter("query");

        if (query == null) {
            query = aRequest.getParameter("q");
        }

        return query;
    }

    /**
     * This method returns the options to fill up the SELECT control for
     * read/unread filter in html search page.
     *
     * @param flag One of All/Read/Unread values.
     * @return Html Select Options.
     */
    private String getReadUnreadFilter(int flag, boolean isVEEnabled) {
        StringBuffer buffer = new StringBuffer();

        if (isVEEnabled == false) {
            buffer.append("<OPTION value='' SELECTED class='list-item-default'>").append("All</OPTION>");

            return buffer.toString();
        }

        if (flag == ALL_REQUESTS) {
            buffer.append("<OPTION value='' SELECTED class='list-item-default'>").append("All</OPTION>").append("\n<OPTION value='true'>Read </OPTION>").append(
                "\n<OPTION value='false'>Unread</OPTION>").append("");
        } else if (flag == READ_REQUESTS) {
            buffer.append("<OPTION value='' class='list-item-default'>").append("All</OPTION>").append("\n<OPTION value='true' SELECTED>").append("Read</OPTION>").append(
                "\n<OPTION value='false'>Unread</OPTION>").append("");
        } else if (flag == UNREAD_REQUESTS) {
            buffer.append("\n<OPTION value='' class='list-item-default'>").append("All</OPTION>").append("\n<OPTION value='true'>Read</OPTION>").append("\n<OPTION value='false' SELECTED>").append(
                "Unread</OPTION>").append("");
        } else {
            buffer.append("<OPTION value='' SELECTED class='list-item-default'>").append("All</OPTION>").append("\n<OPTION value='true'>Read</OPTION>").append(
                "\n<OPTION value='false'>Unread</OPTION>").append("");
        }

        return buffer.toString();
    }

    /**
     * This method returns the render type reading it from the Request Object
     * or the Session object. If not found, it returns the default value.
     *
     * @param aRequest   HttpRequest Object.
     *
     * @return Render Type value.
     */
    private RenderType getRenderType(HttpServletRequest aRequest, WebConfig aConfig) {
        RenderType renderType = RenderType.RENDER_HIER;

        // Get the session object associated with this request.
        HttpSession session = aRequest.getSession();

        // Get the value from the query string.
        String strRenderType = aRequest.getParameter("renderType");

        if ((strRenderType != null) && (strRenderType.trim().equals("") == false)) {
            renderType = RenderType.toRenderType(strRenderType);
        } else {
            try {

                // Check if this is present in the session.
                String temp = (String) session.getAttribute("renderType");

                if (temp != null) {
                    renderType = RenderType.toRenderType(temp);
                } else {
                    renderType = aConfig.getRenderType();
                }
            } catch (Exception e) {

                // Only a ClassCastException can occur.
            }
        }

        if ((renderType != RenderType.RENDER_FLAT) && (renderType != RenderType.RENDER_HIER)) {
            renderType = aConfig.getRenderType();
        }

        // Store this in the session.
        session.setAttribute("renderType", renderType.toString());

        return renderType;
    }

    /**
     * This method forms the result comment.
     *
     * @param resultsSize  No of results retrieved from the database.
     * @param resultCount  No of results that matched the search criteria.
     *
     * @return ResultComment based on the given parameters.
     */
    static String getResultComment(int aSystemId, int resultsSize, int resultCount) {
        StringBuffer comment = new StringBuffer();

        HashMap<String, String> captions = CaptionsProps.getInstance().getCaptionsHashMap(aSystemId); 
        if (resultsSize > 0) {
            if (resultsSize == resultCount) {
                comment.append(resultsSize).append(" ").append(captions.get(CaptionsProps.CAPTIONS_ALL_REQUEST)).append((resultsSize != 1)
                        ? "s"
                        : "");
            } else {
                comment.append("Top ").append(resultsSize).append(" ").append(captions.get(CaptionsProps.CAPTIONS_ALL_REQUESTS)).append(" of ").append(resultCount);
            }

            comment.append("&nbsp;&nbsp;&nbsp;&nbsp;");
        }

        return comment.toString();
    }

    /**
     * This method forms the result comment.
     *
     * @param resultCount  No of results that matched the search criteria.
     * @param pageNumber   Current page number.
     * @param pageSize     Number of results in the current page.
     * @param maxPageSize  Maximum number of results in a page.
     *
     * @return ResultComment based on the given parameters.
     */
    static String getResultComment(int aSystemId, int resultCount, int pageNumber, int pageCount, int pageSize) {
        StringBuffer comment = new StringBuffer();

        HashMap<String, String> captions = CaptionsProps.getInstance().getCaptionsHashMap(aSystemId);
        // Case 1: Result count = 1
        if (resultCount == 1) {
            comment.append("1 "  + captions.get(CaptionsProps.CAPTIONS_ALL_REQUEST));
        }

        // Case 2: Result count < pageSize
        else if (resultCount <= pageSize) {
            comment.append(resultCount).append(" " + captions.get(CaptionsProps.CAPTIONS_ALL_REQUESTS));
        }

        // Case 3: Result Count > pageSize
        else if (resultCount > pageSize) {
            int start = (pageNumber - 1) * pageSize + 1;
            int end   = (pageNumber - 1) * pageSize + pageCount;

            if (end > resultCount) {
                end = resultCount;
            }

            comment.append(start).append(" to ").append(end).append(" results of ").append(resultCount);
        }

        // no other case could be thought of as of now.
        return comment.toString();
    }

    /**
     *
     * @param parentId
     * @param children
     * @return
     */
    private static ResultType getResultType(int parentId, ArrayList<String> children) {
        ResultType resultType = ResultType.RESULT_NORMAL;

        /*
         * 100
         * 101
         *   |- 102
         *        |-104
         *   |- 103
         *
         * CASE - 1: 100:: RESULT_NORMAL (No  Parent, No  Children)
         * CASE - 2: 101:: RESULT_ROOT   (No  Parent, Has Children)
         * CASE - 3: 104:: RESULT_LEAF   (Has Parent, No  Children)
         * CASE - 4: 102:: RESULT_PARENT (Has Parent, Has Children)
         *
         */
        if (parentId == 0) {
            if ((children == null) || (children.size() == 0)) {

                // CASE - 1: (No  Parent, No  Children)
                resultType = ResultType.RESULT_NORMAL;
            } else {

                // CASE - 2: (No  Parent, Has Children)
                resultType = ResultType.RESULT_ROOT;
            }
        } else {
            if ((children == null) || (children.size() == 0)) {

                // CASE - 3: (Has Parent, No  Children)
                resultType = ResultType.RESULT_LEAF;
            } else {

                // CASE - 4: (Has Parent, Has Children)
                resultType = ResultType.RESULT_PARENT;
            }
        }

        return resultType;
    }

    /**
     *
     * @param aChildKey
     * @param aResultsTable
     * @return
     */
    private static String getRootKey(String aChildKey, Hashtable<String, Result> aResultsTable) {
        String rootKey = aChildKey;

        // Get the result object corresponding to this childkey.
        Result result = aResultsTable.get(aChildKey);

        if (result == null) {
            return rootKey;
        }

        int systemId = result.getSystemId();
        int parentId = result.getParentId();

        //
        // Recursive version of this method.
        //
        // if (parentId == 0) return rootKey;
        // else return getRootKey(rootKey, aResultsTable);
        while (parentId != 0) {
            rootKey = Searcher.formTableKey(systemId, parentId);
            result  = aResultsTable.get(rootKey);

            if (result == null) {
                return rootKey;
            }

            systemId = result.getSystemId();
            parentId = result.getParentId();
        }

        return rootKey;
    }

    /**
     * This method returns the view value corresponding to the given string.
     *
     * @param strView
     *                String value of search view.
     */
    private int getSearchView(String strView) {
        int view = NORMAL_VIEW;

//        if (strView.equalsIgnoreCase("simple") || strView.equalsIgnoreCase("s") || strView.equalsIgnoreCase("1")) {
//            view = SIMPLE_VIEW;
//        } else
        if (strView.equalsIgnoreCase("normal") || strView.equalsIgnoreCase("n") || strView.equalsIgnoreCase("0")) {
            view = NORMAL_VIEW;
        } else if (strView.equalsIgnoreCase("advanced") || strView.equalsIgnoreCase("a") || strView.equalsIgnoreCase("1")) {
            view = ADVANCED_VIEW;
        } else if (strView.equalsIgnoreCase("allareas") || strView.equalsIgnoreCase("aa") || strView.equalsIgnoreCase("2")) {
            view = ALL_AREAS_VIEW;
        } else if (strView.equalsIgnoreCase("myrequests") || strView.equalsIgnoreCase("mr") || strView.equalsIgnoreCase("3")) {
            view = MY_REQUESTS_VIEW;
        }
//        else if (strView.equalsIgnoreCase("home") || strView.equalsIgnoreCase("hm") || strView.equalsIgnoreCase("0")) {
//            view = HOME_VIEW;
//        }  
        else {
            view = NORMAL_VIEW;
        }

        return view;
    }

    /**
     * This method returns the current view of the search page.
     *
     * @param aRequest   Http Servlet Request object.
     * @param aSession   Http Session object.
     * @param aSysPrefix Prefix of the Business Area.
     * @param aConfig    User Configuration.
     *
     * @return current search view.
     */
    private int getSearchView(HttpServletRequest aRequest, HttpSession aSession, String aSysPrefix, WebConfig aConfig) throws ServletException, IOException {
        String prefixKey  = aSysPrefix + "_view";
        String generalKey = "view";
        String strView    = aRequest.getParameter("sv");

        /*
         * Check if view is specified in the HttpRequest.
         */
        if ((strView == null) || strView.trim().equals("")) {

            /*
             * Check if query is specified in the HttpRequest.
             */
            String query = getQuery(aRequest);

            if (query != null) {
                strView = Integer.toString(ADVANCED_VIEW);
            } else {

                /*
                 * Check if we have something in the session in general.
                 */
                strView = (String) aSession.getAttribute(generalKey);

                if ((strView == null) || strView.trim().equals("")) {

                    /*
                     * Check if view is present in the session for this BA.
                     */
                    strView = (String) aSession.getAttribute(prefixKey);

                    if ((strView == null) || strView.trim().equals("")) {

                        /*
                         * Get the user default view.
                         */
                        strView = Integer.toString(aConfig.getDefaultView());

                        if ((strView == null) || strView.trim().equals("")) {
                            //strView = Integer.toString(HOME_VIEW);
                            strView = Integer.toString(NORMAL_VIEW);
                        }
                    }    // EndIf no prefix key.
                }        // EndIf no general key.
            }            // End Else query is not present.
        }                // End If view is not present.

        int view = getSearchView(strView);
        aSession.setAttribute(prefixKey, Integer.toString(view));
        aSession.setAttribute(generalKey, Integer.toString(view));

        return view;
    }

    /**
     * This method returns the sort field reading it from the Request Object
     * or the Session object. If not found, it returns the user' default value.
     *
     * @param aRequest   HttpRequest Object.
     * @param aSysPrefix System Prefix.
     * @param aBAConfig  BA Configuration of user.
     *
     * @return Render Type value.
     */
    static String getSortField(HttpServletRequest aRequest, String aSysPrefix, int aSearchView, BAConfig aBAConfig) {
        String sortField = aBAConfig.getSortField();

        // Get the session object associated with this request.
        HttpSession session = aRequest.getSession();
        String      key     = aSysPrefix.toUpperCase() + "_" + aSearchView + "_sortField";

        // Check if present in the request object.
        String strSortField = aRequest.getParameter("sortField");

        if ((strSortField == null) || (strSortField.trim().equals("") == true)) {

            // Check with the session Id key.
            strSortField = aRequest.getParameter(key);

            if ((strSortField == null) || (strSortField.trim().equals("") == true)) {

                // Check in the session.
                try {
                    String temp = (String) session.getAttribute(key);

                    if (temp != null) {
                        sortField = temp;
                    }
                } catch (Exception e) {

                    // Only a ClassCastException can occur.
                }
            } else {
                sortField = strSortField.trim();
            }
        } else {
            sortField = strSortField.trim();
        }

        // Store this in the session.
        session.setAttribute(key, sortField);

        return sortField;
    }

    /**
     * This method returns the sort order reading it from the Request Object
     * or the Session object. If not found, it returns the user' default value.
     *
     * @param aRequest    HttpRequest Object.
     * @param aSystemId   Id of the business area.
     * @param aSysPrefix  System Prefix.
     * @param aSortField  Field on which results are to be sorted.
     * @param aSearchView Current view of the search page.
     *
     * @return Render Type value.
     */
    static int getSortOrder(HttpServletRequest aRequest, int aSystemId, String aSysPrefix, String aSortField, int aSearchView) {
        int sortOrder = getDefaultSortOrder(aSystemId, aSortField);

        // Get the session object associated with this request.
        HttpSession session  = aRequest.getSession();
        String      key      = aSysPrefix.toUpperCase() + "_" + aSearchView + "_" + aSortField + "_sortOrder";
        String      otherKey = aSysPrefix.toUpperCase() + "_" + aSearchView + "_sortOrder";

        // Check if present in the request object.
        String strSortOrder = aRequest.getParameter("sortOrder");

        if ((strSortOrder == null) || (strSortOrder.trim().equals("") == true)) {

            // check with the session key.
            strSortOrder = aRequest.getParameter(otherKey);

            // Check in the session.
            if ((strSortOrder == null) || (strSortOrder.trim().equals("") == true)) {
                try {
                    Integer temp = (Integer) session.getAttribute(key);

                    if (temp != null) {
                        sortOrder = temp.intValue();
                    }
                } catch (Exception e) {

                    // Only a ClassCastException can occur.
                }
            } else {
                try {
                    sortOrder = Integer.parseInt(strSortOrder);
                } catch (Exception e) {}
            }
        } else {
            try {
                sortOrder = Integer.parseInt(strSortOrder);
            } catch (Exception e) {}
        }

        // Store this in the session.
        session.setAttribute(key, sortOrder);

        return sortOrder;
    }

    /**
     * This method returns the Text Filter if present in the QueryString of the
     * Http Request.
     *
     * @param aRequest HttpServletRequest Object.
     *
     * @return Filter if present. Empty otherwise.
     */
    private static String getTextFilter(HttpServletRequest aRequest) {
        String filter = aRequest.getParameter("filter");

        if (filter == null) {
            filter = aRequest.getParameter("f");

            if (filter == null) {
                filter = getCookieValue(aRequest, "filter");
                filter = (filter == null)
                         ? ""
                         : filter.trim();
            }
        }

        return filter;
    }

    /**
     * This method returns the Id of the request for which the user wanted the
     * tree to be shown.
     *
     * @param pTable ParseTable.
     * @return TreeId or 0.
     */
    private static int getTreeId(ArrayList<ParseEntry> pTable) {
        int treeId = 0;

        if ((pTable != null) && (pTable.size() == 1)) {
            ParseEntry pe = pTable.get(0);

            // Get the Descriptor and check if it is tree.
            String desc = pe.getDescriptor();

            if ((desc != null) && desc.trim().equalsIgnoreCase("tree")) {
                ArrayList<String> argList = pe.getArgList();

                if ((argList != null) && (argList.size() != 0)) {
                    String strTreeId = argList.get(1);

                    try {
                        treeId = Integer.parseInt(strTreeId);
                    } catch (Exception e) {
                        treeId = 0;
                    }
                }
            }
        }

        return treeId;
    }

    /**
     * This method returns the HTML required to render the types for the given
     * field.
     *
     * @param aSystemId  Business Area Id.
     * @param aFieldName Name of the field.
     * @param aIsPrivate User's permission on isprivate field.
     *
     * @return HTML for rendering the types.
     */
    private String getTypeList(int aSystemId, String aFieldName, boolean aIsPrivate, boolean aAll) throws DatabaseException {
        StringBuffer    buffer   = new StringBuffer();
        ArrayList<Type> typeList = Type.lookupBySystemIdAndFieldName(aSystemId, aFieldName);

        if (typeList != null) {

            // Sort the fields by the ordering field.
            Type.setSortParams(Type.ORDERING, 1);
            typeList = Type.sort(typeList);

            if (aAll == true) {
                buffer.append("\n<OPTION value='' SELECTED>All</OPTION>");
            } else {
                buffer.append("<OPTION value=''>All</OPTION>");
            }

            for (Type type : typeList) {

                // Skip the nulls.
                if (type == null) {
                    continue;
                }

                // Skip the inactive types.
                if (type.getIsActive() == false) {
                    continue;
                }

                // Skip the private types if user does not have permission.
                if ((type.getIsPrivate() == true) && (aIsPrivate == false)) {
                    continue;
                }

                buffer.append("\n<OPTION value=\"").append(Utilities.htmlEncode(type.getName())).append("\"");

                if (aAll == false) {
                    if (type.getIsChecked() == true) {
                        buffer.append(" SELECTED ");
                    }

                    if (type.getIsFinal() == true) {
                        buffer.append(" class=\"list-item-default\" ");
                    }
                }

                buffer.append(">").append(Utilities.htmlEncode(type.getDisplayName())).append((type.getIsPrivate() == true)
                        ? " &dagger;"
                        : "").append("</OPTION>");
            }
        }

        return buffer.toString();
    }

    /**
     * This method returns the HTML required to render the types for the given
     * field.
     *
     * @param aSystemId  Business Area Id.
     * @param aFieldName Name of the field.
     * @param aIsPrivate User's permission on isprivate field.
     *
     * @return HTML for rendering the types.
     */
    private String getTypeList(int aSystemId, String aFieldName, boolean aIsPrivate, Connective aConnective, Hashtable<String, Boolean> aTable) throws DatabaseException {
        StringBuffer    buffer   = new StringBuffer();
        ArrayList<Type> typeList = Type.lookupBySystemIdAndFieldName(aSystemId, aFieldName);

        if (typeList != null) {

            // Sort the fields by the ordering column.
            Type.setSortParams(Type.ORDERING, 1);
            typeList = Type.sort(typeList);

            boolean selected = false;

            for (Type type : typeList) {

                // Skip the nulls.
                if (type == null) {
                    continue;
                }

                // Skip the inactive types.
                if (type.getIsActive() == false) {
                    continue;
                }

                // Skip the private types if user does not have permission.
                if ((type.getIsPrivate() == true) && (aIsPrivate == false)) {
                    continue;
                }

                buffer.append("\n<OPTION value=\"").append(Utilities.htmlEncode(type.getDisplayName())).append("\"");

                if ((aTable.get(type.getDisplayName().toLowerCase()) != null) || (aTable.get(type.getName().toLowerCase()) != null)) {

                    // If present and connective is not NOT, select this.
                    if ((aConnective != Connective.C_NOT) && (aConnective != Connective.C_OR_NOT) && (aConnective != Connective.C_AND_NOT)) {
                        buffer.append(" SELECTED ");
                        selected = true;
                    }
                } else {

                    // If not present and connective is NOT, select this.
                    if ((aConnective == Connective.C_NOT) || (aConnective == Connective.C_OR_NOT) || (aConnective == Connective.C_AND_NOT)) {
                        buffer.append(" SELECTED ");
                        selected = true;
                    }
                }

                if (type.getIsFinal() == true) {
                    buffer.append(" class=\"list-item-default\" ");
                }

                buffer.append(">").append(Utilities.htmlEncode(type.getDisplayName())).append((type.getIsPrivate() == true)
                        ? " &dagger;"
                        : "").append("</OPTION>");
            }

            if (selected == false) {
                buffer.insert(0, "\n<OPTION value='' SELECTED>All</OPTION>");
            } else {
                buffer.insert(0, "\n<OPTION value=''>All</OPTION>");
            }
        }

        return buffer.toString();
    }

    /**
     * This method returns the Display Header for the search results.
     *
     * @param aBA        Business Area.
     * @param aSearcher  Searcher object.
     *
     * @return Display Header in HTML.
     */
    private static String getXMLHeader(BusinessArea aBA, Searcher aSearcher) {
        int                      aSystemId   = aBA.getSystemId();
        int                      sortOrder   = aSearcher.getSortOrder();
        String                   sortField   = aSearcher.getSortField();
        ArrayList<String>        header      = aSearcher.getDisplayHeader();
        Hashtable<String, Field> fieldsTable = aSearcher.getFieldsTable();
        StringBuffer             buffer      = new StringBuffer();

        buffer.append("\n<Sort field=\"").append(sortField).append("\" order=\"").append(sortOrder).append("\" />");
        buffer.append("\n<Header>");

        for (String fieldName : header) {
            Field field = null;

            field = fieldsTable.get(fieldName);

            if (field == null) {
                LOG.warn("Field not found for " + fieldName);

                continue;
            }

            fieldName = field.getName();

            String  displayName = field.getDisplayName();
            int     dataType    = field.getDataTypeId();
            boolean isPrivate   = field.getIsPrivate();

            buffer.append("\n<Field name=\"").append(fieldName).append("\" displayName=\"").append(Utilities.htmlEncode(displayName)).append("\" dataType=\"").append(dataType).append(
                "\" isPrivate=\"").append(isPrivate).append("\" />");
        }

        buffer.append("</Header>");

        return buffer.toString();
    }

    /**
     * This method returns the HTML to display the Admin link in search page
     * if the user is an admin in this business area in any of the following
     * ways:
     *
     * <UL>
     *  <LI> Change permission on Business Area.
     *  <LI> Admin in the business area.
     *  <LI> Permission Admin in that business area.
     *  <LI> Super user in the TBits database.
     * </UL>
     *
     * @param aPermTable Permission table for this user.
     * @param aRequest TODO
     *
     * @return HTML.
     */
    private String isAdmin(Hashtable<String, Integer> aPermTable, HttpServletRequest aRequest) {
        StringBuffer buffer = new StringBuffer();

        if (aPermTable == null) {
            return buffer.toString();
        }
        String IMAGE_PATH = WebUtil.getNearestPath(aRequest, IMAGE_STATIC_PATH);


        buffer.append("| <span class=\"l sx b cw\"> &nbsp;<SPAN class=\"l cw\" onclick=\"openAdmin()\"").append(">Administration</SPAN>&nbsp;</span>");

        Integer temp = null;

        // Check if the user has change permission on Business Area.
        temp = aPermTable.get(Field.BUSINESS_AREA);

        if ((temp != null) && (temp & Permission.CHANGE) != 0) {
            return buffer.toString();
        } else {

            // Check for the admin tag in the permission table.
            temp = aPermTable.get("__ADMIN__");

            if ((temp != null) && (temp != 0)) {
                return buffer.toString();
            } else {

                // Check for the permission admin tag.
                temp = aPermTable.get("__PERMISSIONADMIN__");

                if ((temp != null) && (temp != 0)) {
                    return buffer.toString();
                } else {

                    // Check for the Super User tag.
                    temp = aPermTable.get("__SUPER_USER__");

                    if ((temp != null) && (temp != 0)) {
                        return buffer.toString();
                    }
                }
            }
        }

        return "";
    }

    //~--- set methods --------------------------------------------------------

    /**
     * This method replaces the search view related tags.
     *
     * @param view      Current View of the search page.
     * @param aTagTable Tag table.
     */
    private void setSearchView(int view, Hashtable<String, String> aTagTable) {
        switch (view) {
//        case HOME_VIEW : {
//            // home view.
//            aTagTable.put("simpleClass", "  l stus");
//            aTagTable.put("simpleDisplay", "none");
//            aTagTable.put("normalClass", " l stus");
//            aTagTable.put("normalDisplay", "none");
//            aTagTable.put("advancedClass", " l stus");
//            aTagTable.put("advancedDisplay", "none");
//            aTagTable.put("allAreasClass", " l stus");
//            aTagTable.put("allAreasDisplay", "none");
//            aTagTable.put("myRequestsClass", " l stus");
//            aTagTable.put("myRequestsDisplay", "none");
//            aTagTable.put("homeClass", " b cw sts");
//            aTagTable.put("homeDisplay", " ");
//        }

//        case SIMPLE_VIEW : {
////            aTagTable.put("simpleClass", "  l stus");
////            aTagTable.put("simpleDisplay", "none");
//             // Simple view.
//            aTagTable.put("simpleClass", " b cw sts");
//            aTagTable.put("simpleDisplay", " ");
//            aTagTable.put("normalClass", " l stus");
//            aTagTable.put("normalDisplay", "none");
//            aTagTable.put("advancedClass", " l stus");
//            aTagTable.put("advancedDisplay", "none");
//            aTagTable.put("allAreasClass", " l stus");
//            aTagTable.put("allAreasDisplay", "none");
//            aTagTable.put("myRequestsClass", " l stus");
//            aTagTable.put("myRequestsDisplay", "none");
////            aTagTable.put("homeClass", " b cw sts");
////            aTagTable.put("homeDisplay", " ");
//        }

//        break;

        case NORMAL_VIEW : {
            view = NORMAL_VIEW;
            aTagTable.put("simpleClass", " l stus");
            aTagTable.put("simpleDisplay", "none");
//            aTagTable.put("normalClass", "  b cw sts");
            aTagTable.put("normalClass", " b cw sts");
            aTagTable.put("normalDisplay", " ");
            aTagTable.put("advancedClass", " l stus");
            aTagTable.put("advancedDisplay", "none");
            aTagTable.put("allAreasClass", " l stus");
            aTagTable.put("allAreasDisplay", "none");
            aTagTable.put("myRequestsClass", " l stus");
            aTagTable.put("myRequestsDisplay", "none");
//            aTagTable.put("homeClass", " l stus");
//            aTagTable.put("homeDisplay", " none");
        }

        break;

        case ADVANCED_VIEW : {
            aTagTable.put("simpleClass", " l stus");
            aTagTable.put("simpleDisplay", "none");
            aTagTable.put("normalClass", " l stus");
            aTagTable.put("normalDisplay", "none");
            aTagTable.put("advancedClass", " b cw sts");
            aTagTable.put("advancedDisplay", " ");
            aTagTable.put("allAreasClass", " l stus");
            aTagTable.put("allAreasDisplay", "none");
            aTagTable.put("myRequestsClass", " l stus");
            aTagTable.put("myRequestsDisplay", "none");
//            aTagTable.put("homeClass", " l stus");
//            aTagTable.put("homeDisplay", " none");
        }

        break;

        case ALL_AREAS_VIEW : {
            aTagTable.put("simpleClass", " l stus");
            aTagTable.put("simpleDisplay", "none");
            aTagTable.put("normalClass", " l stus");
            aTagTable.put("normalDisplay", "none");
            aTagTable.put("advancedClass", " l stus");
            aTagTable.put("advancedDisplay", "none");
            aTagTable.put("allAreasClass", " b cw sts");
            aTagTable.put("allAreasDisplay", " ");
            aTagTable.put("myRequestsClass", " l stus");
            aTagTable.put("myRequestsDisplay", "none");
//            aTagTable.put("homeClass", " l stus");
//            aTagTable.put("homeDisplay", " none");
        }

        break;

        case MY_REQUESTS_VIEW : {
            aTagTable.put("simpleClass", " l stus");
            aTagTable.put("simpleDisplay", "none");
            aTagTable.put("normalClass", " l stus");
            aTagTable.put("normalDisplay", "none");
            aTagTable.put("advancedClass", " l stus");
            aTagTable.put("advancedDisplay", "none");
            aTagTable.put("allAreasClass", " l stus");
            aTagTable.put("allAreasDisplay", "none");
            aTagTable.put("myRequestsClass", " b cw sts");
            aTagTable.put("myRequestsDisplay", " ");
//            aTagTable.put("homeClass", " l stus");
//            aTagTable.put("homeDisplay", " none");
        }

        break;

        default : {
//            view = HOME_VIEW;
            view = NORMAL_VIEW;
            aTagTable.put("simpleClass", " l stus");
            aTagTable.put("simpleDisplay", "none");
            aTagTable.put("normalClass", " b sts");
//            aTagTable.put("normalDisplay", "none ");
            aTagTable.put("normalDisplay", " ");
            aTagTable.put("advancedClass", " l stus");
            aTagTable.put("advancedDisplay", "none");
            aTagTable.put("allAreasClass", " l stus");
            aTagTable.put("allAreasDisplay", "none");
            aTagTable.put("myRequestsClass", " l stus");
            aTagTable.put("myRequestsDisplay", "none");
//            aTagTable.put("homeClass", " l stus");
//            aTagTable.put("homeDisplay", " ");
        }

        break;
        }
        aTagTable.put("sv", Integer.toString(view));
        aTagTable.put("configureLine", " ");
    }
}
