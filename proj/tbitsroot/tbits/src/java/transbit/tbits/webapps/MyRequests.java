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
 * MyRequests.java
 *
 * $Header:
 */
package transbit.tbits.webapps;

//~--- non-JDK imports --------------------------------------------------------

import transbit.tbits.Helper.Messages;
import transbit.tbits.common.DTagReplacer;
import transbit.tbits.common.DatabaseException;

//TBits Imports.
import transbit.tbits.common.TBitsLogger;
import transbit.tbits.common.Utilities;
import transbit.tbits.config.BAConfig;
import transbit.tbits.config.WebConfig;
import transbit.tbits.domain.BusinessArea;
import transbit.tbits.domain.DataType;
import transbit.tbits.domain.Field;
import transbit.tbits.domain.FieldDescriptor;
import transbit.tbits.domain.Permission;
import transbit.tbits.domain.Type;
import transbit.tbits.domain.User;
import transbit.tbits.exception.TBitsException;
import transbit.tbits.search.Result;
import transbit.tbits.search.Searcher;

import static transbit.tbits.Helper.TBitsConstants.FILTER_ASSIGNEE;
import static transbit.tbits.Helper.TBitsConstants.FILTER_LOGGER;
import static transbit.tbits.Helper.TBitsConstants.FILTER_PRIMARY_ASSIGNEE;
import static transbit.tbits.Helper.TBitsConstants.FILTER_SUBSCRIBER;
import static transbit.tbits.Helper.TBitsConstants.OutputFormat;
import static transbit.tbits.Helper.TBitsConstants.PKG_WEBAPPS;

//Static imports.
import static transbit.tbits.api.Mapper.ourDistinctFieldTable;
import static transbit.tbits.search.SearchConstants.ANCHOR;
import static transbit.tbits.search.SearchConstants.DESC_ORDER;
import static transbit.tbits.search.SearchConstants.HIDE_RESULT;
import static transbit.tbits.search.SearchConstants.JAVASCRIPT;
import static transbit.tbits.search.SearchConstants.LAST_ACTION_ID;
import static transbit.tbits.search.SearchConstants.LINK_SUBJECT;
import static transbit.tbits.search.SearchConstants.MOUSEOVER;
import static transbit.tbits.search.SearchConstants.MY_REQUESTS_VIEW;
import static transbit.tbits.search.SearchConstants.RenderType;

//~--- JDK imports ------------------------------------------------------------

//Java Imports.
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;

import java.net.URLEncoder;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.TimeZone;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

//~--- classes ----------------------------------------------------------------

public class MyRequests extends HttpServlet {

    // Application LOG for logging events or messages.
    public static TBitsLogger LOG = TBitsLogger.getLogger(PKG_WEBAPPS);

    // Name of the html interface that renders the my-request results of a BA.
    private static final String MY_REQUESTS_RESULTS = "web/tbits-my-requests-results.htm";
    private static boolean      DOUBLE_QUOTE        = true;
    private static boolean      SINGLE_QUOTE        = false;

    //~--- fields -------------------------------------------------------------

    // Database query.
    private String myDatabaseQuery = "";

    // Comma separated list of roles.
    private ArrayList<String> myRoleFilter = new ArrayList<String>();

    // Time spent for database interaction.
    private long myDatabaseTime;

    // True if the filter list contains Primary Assignee filter also.
    private boolean myIsPrimary;

    // Time spent for Html Serializing the results.
    private long myRenderTime;

    //~--- methods ------------------------------------------------------------

    /**
     * This method checks if the display header contains the fields required
     * for accomplishing visual enhancements.
     *
     * @param userFields  List with fields required for visual effects.
     */
    private void checkVEFields(ArrayList<String> displayHeader) {
        if (displayHeader == null) {
            return;
        }

        // Confidential is required to render the result as a private request.
        if (displayHeader.contains(Field.IS_PRIVATE) == false) {
            displayHeader.add(Field.IS_PRIVATE);
        }

        // Severity field is required to identify critical requests.
        if (displayHeader.contains(Field.SEVERITY) == false) {
            displayHeader.add(Field.SEVERITY);
        }

        // Status field is required to identify closed requests.
        if (displayHeader.contains(Field.STATUS) == false) {
            displayHeader.add(Field.STATUS);
        }

        // Due Date field is required to identify requests whose due date
        // occurs in past.
        if (displayHeader.contains(Field.DUE_DATE) == false) {
            displayHeader.add(Field.DUE_DATE);
        }

        // Max Action Id is required to identify newly logged requests.
        if (displayHeader.contains(Field.MAX_ACTION_ID) == false) {
            displayHeader.add(Field.MAX_ACTION_ID);
        }

        // Parent Id is required to identify requests hierarchy.
        if (displayHeader.contains(Field.PARENT_REQUEST_ID) == false) {
            displayHeader.add(Field.PARENT_REQUEST_ID);
        }

        /*
         * Last updated date is required to identify requests which are
         * closed and not updated since last 'N' days, so that such requests
         * will not be shown unread for any one.
         */
        if (displayHeader.contains(Field.LASTUPDATED_DATE) == false) {
            displayHeader.add(Field.LASTUPDATED_DATE);
        }

        // Action Id from the user_read_actions is needed to see if any
        // new appends are made to the requests that matched the criteria.
        displayHeader.add(LAST_ACTION_ID);
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
    public void doGet(HttpServletRequest aRequest, HttpServletResponse aResponse) throws ServletException, IOException {
        PrintWriter out      = aResponse.getWriter();
        String      filter   = aRequest.getParameter("filter");
        String      show     = aRequest.getParameter("show");
        String      collapse = aRequest.getParameter("collapse");
        String      export   = aRequest.getParameter("export");

        /*
         * Redirect if none of the above request params is found in the
         * request string.
         */
        if (((filter == null) || (filter.trim().equals("") == true)) && ((show == null) || (show.trim().equals("") == true)) && ((collapse == null) || (collapse.trim().equals("") == true))
                && ((export == null) || (export.trim().equals("") == true))) {
            aResponse.sendRedirect(WebUtil.getNearestPath(aRequest, "search/my-requests"));

            return;
        }

        myDatabaseQuery = "";

        long start, end;

        try {
            start = System.currentTimeMillis();
            handleGetRequest(aRequest, aResponse);
            end = System.currentTimeMillis();
            LOG.info("<!-- Total time taken: " + (end - start) + " -->");
        } catch (Exception e) {
            LOG.warn("",(e));
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

        // No specific purpose of a POST request, so just calling GET here.
        doGet(aRequest, aResponse);
    }

    /**
     * This method prepares the query for obtaining the My Requests of a user
     * for the given BA list , HTML serializes these results and returns them.
     *
     * @param aRequest    Http Request object to access session.
     * @param aUserId     Id of the user.
     * @param aBAList     List of BAs from where the user want the MyRequests to
     *                    be retrieved.
     * @param aConfig     User configuration object.
     * @param aRoles      Role filter.
     * @param aIsPrimary  True if only primary assignee filter is selected and
     *                    not assignee filter.
     * @param aPrefZone   Zone where the user is present.
     * @param aFlags      Flags used during rendering.
     * @return            My Requests rendered as HTML.
     * @throws IOException
     * @throws FileNotFoundException
     */
    private String executeQuery(HttpServletRequest aRequest, int aUserId, ArrayList<BusinessArea> aBAList, WebConfig aConfig, ArrayList<String> aRoles, boolean aIsPrimary, TimeZone aPrefZone,
                                int aFlags)
            throws IOException, FileNotFoundException, DatabaseException {
        HttpSession              aSession            = aRequest.getSession();
        StringBuffer             output              = new StringBuffer();
        ArrayList<String>        fixedFields         = new ArrayList<String>();
        ArrayList<String>        userFields          = new ArrayList<String>();
        ArrayList<String>        extFields           = new ArrayList<String>();
        Hashtable<String, Field> distinctFieldsTable = new Hashtable<String, Field>(ourDistinctFieldTable);

        // Get the filtering part of the query.
        String            query         = getQuery(aUserId, aBAList, fixedFields, userFields, extFields, aConfig, aRoles, aIsPrimary);
        ArrayList<String> displayHeader = new ArrayList<String>();

        displayHeader.addAll(fixedFields);
        displayHeader.addAll(userFields);
        displayHeader.addAll(extFields);
        checkVEFields(displayHeader);

        /*
         * Instantiate a Searcher object and execute this query.
         */
        Searcher searcher      = new Searcher(aUserId, distinctFieldsTable, query);
        boolean  hasUserFields = (userFields.size() > 0)
                                 ? true
                                 : false;
        boolean  hasExtFields  = (extFields.size() > 0)
                                 ? true
                                 : false;

        searcher.setDisplayHeader(displayHeader);
        searcher.setSortField(Field.REQUEST);
        searcher.setSortOrder(DESC_ORDER);
        searcher.setHasExtFields(hasExtFields);
        searcher.setHasUserFields(hasUserFields);

        long    start   = System.currentTimeMillis();
        boolean success = true;

        try {
            searcher.search();
        } catch (Exception de) {
            query = query + "\n\n\nException: " + de.toString();
            LOG.info("",(de));
            success = false;
        }

        long end = System.currentTimeMillis();

        myDatabaseTime = (end - start);
        start          = System.currentTimeMillis();

        if (success == true) {
            Hashtable<String, Result>             reqTable            = searcher.getRequestsTable();
            Hashtable<String, ArrayList<String>>  parTable            = searcher.getParentsTable();
            ArrayList<String>                     incompleteParents   = searcher.getRequestsWithIncompleteTrees();
            Hashtable<Integer, ArrayList<String>> resultIdListByBA    = searcher.getResultIdListByBA();
            Hashtable<Integer, ArrayList<Result>> resultListByBA      = searcher.getResultListByBA();
            Hashtable<Integer, ArrayList<String>> allResultIdListByBA = searcher.getAllRequestIdListByBA();
            ArrayList<String>                     resultIdList        = searcher.getResultIdList();

            // Render the results from each Business Area.
            for (BusinessArea ba : aBAList) {
                int                      systemId    = ba.getSystemId();
                String                   sysPrefix   = ba.getSystemPrefix();
                BAConfig                 baConfig    = aConfig.getBAConfig(sysPrefix);
                Hashtable<String, Field> fieldsTable = null;

                try {
                    fieldsTable = Field.getFieldsTableBySystemId(systemId);
                } catch (Exception e) {
                    fieldsTable = new Hashtable<String, Field>();
                }

                /*
                 * Check if this BA should be collapsed by default.
                 */
                int thisBAFlags = aFlags;

                if (baConfig.getCollapseBA() == true) {
                    thisBAFlags = aFlags | HIDE_RESULT;
                }

                Searcher          baSearcher      = new Searcher();
                ArrayList<String> baDisplayHeader = baConfig.getDisplayHeader();
                ArrayList<String> resultIds       = resultIdListByBA.get(systemId);
                ArrayList<Result> results         = resultListByBA.get(systemId);
                ArrayList<String> allIdList       = allResultIdListByBA.get(systemId);

                if ((results == null) || (results.size() == 0)) {
                    LOG.info("No records matched the current status filter for " + sysPrefix);

                    continue;
                }

                baSearcher.setFieldsTable(fieldsTable);
                baSearcher.setDisplayHeader(baDisplayHeader);
                baSearcher.setResultIdList(resultIdList);
                baSearcher.setResultList(results);
                baSearcher.setAllRequestIdList(allIdList);
                baSearcher.setAllRequestIdListByBA(allResultIdListByBA);
                baSearcher.setParentsTable(parTable);
                baSearcher.setRequestsTable(reqTable);
                baSearcher.setRequestsWithIncompleteTrees(incompleteParents);
                baSearcher.setSortField(baConfig.getSortField());
                baSearcher.setSortOrder(baConfig.getSortOrder());

                String content = format(aRequest, ba, aConfig, aUserId, baSearcher, aPrefZone, thisBAFlags);

                output.append(content);
            }
        }

        end          = System.currentTimeMillis();
        myRenderTime = (end - start);

        return output.toString();
    }

    /**
     * This BA returns a subset of BAs whose prefixes are specified in the
     * prefixList after filtering out BAs whose showBA property is false.
     *
     * @param userConfig   Configuation of the current user.
     * @param prefixList   List of business area prefixes.
     * @return  List of BA prefixes whose showBA is set to true.
     */
    ArrayList<String> filterBAsToHide(WebConfig userConfig, ArrayList<String> prefixList) {
        ArrayList<String> list = new ArrayList<String>();

        if ((prefixList == null) || (prefixList.size() == 0)) {
            return list;
        }

        for (String prefix : prefixList) {
            BAConfig config = userConfig.getBAConfig(prefix);

            if (config.getShowBA() == true) {
                list.add(prefix);
            }
        }

        return list;
    }

    /**
     * This method formats the results present the searcher object in html and
     * returns them.
     *
     * @param aRequest   Http Request object.
     * @param aBA        Business Area
     * @param aConfig    User configuration
     * @param aUserId    User Id.
     * @param aSearcher  Searcher object
     * @param aZone      User's zone.
     * @param aFlags     Flags to be used during rendering.
     * @return
     * @throws IOException
     * @throws FileNotFoundException
     */
    private String format(HttpServletRequest aRequest, BusinessArea aBA, WebConfig aConfig, int aUserId, Searcher aSearcher, TimeZone aZone, int aFlags) throws IOException, FileNotFoundException {
        HttpSession  aSession = aRequest.getSession();
        StringBuffer buffer   = new StringBuffer();

        // Read the BA related properties.
        int      systemId    = aBA.getSystemId();
        String   displayName = aBA.getDisplayName();
        String   email       = aBA.getEmail();
        String   sysPrefix   = aBA.getSystemPrefix();
        BAConfig baConfig    = aConfig.getBAConfig(sysPrefix);

        // Read the properties from searcher needed for rendering.
        ArrayList<String>        displayHeader = aSearcher.getDisplayHeader();
        ArrayList<String>        statusFilter  = baConfig.getStatusFilter();
        Hashtable<String, Field> fieldsTable   = aSearcher.getFieldsTable();
        ArrayList<Result>        searchResults = aSearcher.getResultList();

        if (searchResults == null) {
            return "";
        }

        int resultCount = searchResults.size();

        aSearcher.setTotalResultCount(resultCount);
        aSearcher.setRetrievedResultCount(resultCount);

        /*
         * Get the sortField and sortOrder. Values for these fields can be
         * found at multiple places, but the order of preference is
         *      - Http Request
         *      - Http Session
         *      - User Config.
         */
        String sortField = HtmlSearch.getSortField(aRequest, sysPrefix, MY_REQUESTS_VIEW, baConfig);
        int    sortOrder = HtmlSearch.getSortOrder(aRequest, systemId, sysPrefix, sortField, MY_REQUESTS_VIEW);

        /*
         * By default, the results are sorted on RequestId field in descending
         * order. If the sortfield or sortorder configured by user for this
         * BA are any different, then the results need to be sorted again
         * accordingly.
         */
        if ((sortField.equals(Field.REQUEST) == false) || (sortOrder != DESC_ORDER)) {

            // Sort the requests based on their sort field and sort order.
            long start = System.currentTimeMillis();

            // Get the request table.
            Hashtable<String, Result> reqTable = aSearcher.getRequestsTable();

            // Get the table with ids of all requests retrieved by BA.
            Hashtable<Integer, ArrayList<String>> allIdByBA = aSearcher.getAllRequestIdListByBA();

            // Get the list of ids retrieved from this BA.
            ArrayList<String> allIds = allIdByBA.get(systemId);

            // Get the corresponding results.
            ArrayList<Result> allResults = new ArrayList<Result>();

            for (String key : allIds) {
                Result result = reqTable.get(key);

                if (result != null) {
                    allResults.add(result);
                }
            }

            // Sort all results and matched results.
            Result.setSortField(sortField);
            Result.setSortOrder(sortOrder);
            searchResults = Result.sort(searchResults);
            allResults    = Result.sort(allResults);

            // Get the list of Matched ids after sorting.
            ArrayList<String> resultIdList = new ArrayList<String>();

            for (Result result : searchResults) {
                int    requestId = result.getRequestId();
                String key       = Searcher.formTableKey(systemId, requestId);

                resultIdList.add(key);
            }

            // Get the list of all Ids after sorting.
            ArrayList<String> allIdList = new ArrayList<String>();

            for (Result result : allResults) {
                int    requestId = result.getRequestId();
                String key       = Searcher.formTableKey(systemId, requestId);

                allIdList.add(key);
            }

            // Set the value again correctly.
            allIdByBA.put(systemId, allIds);
            aSearcher.setAllRequestIdList(allIdList);
            aSearcher.setResultIdList(resultIdList);

            long end = System.currentTimeMillis();

            // Log the time taken for sorting.
            LOG.info("Sorting (" + searchResults.size() + ") Results In " + sysPrefix + " BA On " + sortField + " In " + ((sortOrder == 0)
                    ? "ASC"
                    : "DESC") + " Order Took " + (end - start) + " ms");
        }

        // Set the searchresults and sort properties of the searcher again.
        aSearcher.setSortField(sortField);
        aSearcher.setSortOrder(sortOrder);

        RenderType                renderType       = aConfig.getRenderType();
        DTagReplacer              hp               = new DTagReplacer(MY_REQUESTS_RESULTS);
        Hashtable<String, String> tgTable          = new Hashtable<String, String>();
        String                    rowId            = sysPrefix + "_MyRequests";
        String                    resultsTableName = sysPrefix + "_searchResults";
        String                    sessionIdName    = sysPrefix + "_sessionId";
        String                    sessionIdKey     = "MRSession_" + sysPrefix + "_" + System.currentTimeMillis();

        tgTable.put("rowId", rowId);
        tgTable.put("resultsTableName", resultsTableName);
        tgTable.put("sessionIdName", sessionIdName);
        tgTable.put("sessionIdKey", sessionIdKey);

        StringBuffer       html           = new StringBuffer();
        ArrayList<Integer> sessionIdValue = HtmlSearch.renderResults(aBA,    // Business Area
            aConfig,                                                         // User configuration
            aSearcher,                                                       // Searcher object
            aZone,                                                           // User's zone.
            renderType,                                                      // Kind of rendering
            html,                                                            // Out param to hold the html
            aFlags,                                                          // Flags for rendering.
            OutputFormat.HTML, aRequest);                                              // format is HTML.

        aSession.setAttribute(sessionIdKey, sessionIdValue);

        String colGroup = HtmlSearch.getColumnGroup(aBA, displayHeader, aFlags);
        int    colSpan  = displayHeader.size() + 1;

        // Get the email related html content.
        String emailLink = HtmlSearch.getEmailLink(email);

        // get the result comment.
        String resultComment = HtmlSearch.getResultComment(systemId, resultCount, resultCount);

        // Get the result header.
        String header = HtmlSearch.getHeader(aBA, aSearcher, HtmlSearch.SORT_LINK, HtmlSearch.SORT_ICON, aFlags, aRequest.getContextPath());

        tgTable.put("colGroup", colGroup);
        tgTable.put("colSpan", Integer.toString(colSpan));
        tgTable.put("displayName", displayName);
        tgTable.put("emailLink", emailLink);
        tgTable.put("resultComment", resultComment);
        tgTable.put("sysPrefix", sysPrefix);
        tgTable.put("header", header);
        tgTable.put("results", html.toString());

        if (baConfig.getCollapseBA() == true) {
            tgTable.put("collapseText", "Expand");
        } else {
            tgTable.put("collapseText", "Collapse");
        }

        SearchRenderer.replaceTags(hp, tgTable, HtmlSearch.ourMyRequestsResultsTagList);
        buffer.append(hp.parse(systemId));

        return buffer.toString();
    }

    /**
     * This method actually services the Http-GET Requests to this servlet.
     *
     *
     * @param aRequest          the HttpServlet Request Object
     * @param aResponse         the HttpServlet Response Object
     * @throws ServletException
     * @throws IOException
     * @throws TBitsExceptionrows DatabaseException
     */
    private void handleGetRequest(HttpServletRequest aRequest, HttpServletResponse aResponse) throws ServletException, IOException, TBitsException, DatabaseException {

        // By default, the content-type of output is HTML.
        aResponse.setContentType("text/plain");

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

            String url = "/my-requests" + pathInfo;

            out.println(WebUtil.getRedirectionHtml(aRequest, url));

            return;
        }

        // Get the TimeZone based on the user's preferred zone.
        int      zone     = userConfig.getPreferredZone();
        TimeZone prefZone = WebUtil.getPreferredZone(zone, clientOffset);
        String   sysList  = aRequest.getParameter("areas");

        /*
         * Check if this request is to collapse/expand business areas.
         * If so, then areas is a mandatory parameter.
         */
        String collapse = aRequest.getParameter("collapse");

        if ((collapse != null) && (collapse.trim().equals("") == false) && (sysList != null) && (sysList.trim().equals("") == false)) {
            ArrayList<String> prefixList = Utilities.toArrayList(sysList);

            if (collapse.trim().equals("true") || collapse.trim().equals("yes") || collapse.trim().equals("1")) {

                // set the collpaseBA property of the BA in this list to true.
                setCollapse(prefixList, user, true);
            } else {

                // set the collpaseBA property of the BA in this list to false.
                setCollapse(prefixList, user, false);
            }

            return;
        }

        /*
         * Check if this request is to show/hide business areas.
         * If so, then areas is a mandatory parameter.
         */
        String show = aRequest.getParameter("show");

        if ((show != null) && (show.trim().equals("") == false) && (sysList != null) && (sysList.trim().equals("") == false)) {
            ArrayList<String> prefixList = Utilities.toArrayList(sysList);

            if (show.trim().equals("true") || show.trim().equals("yes") || show.trim().equals("1")) {

                // set the showBA property of the BA in this list to true.
                setShow(prefixList, user, true);
            } else {

                // set the showBA property of the BA in this list to false.
                setShow(prefixList, user, false);
            }

            return;
        }

        // Get the role filter on my-requests
        int filter = getMyRequestsFilter(aRequest, userConfig.getFilter());

        getRoleFilter(filter);

        /*
         * Check if this request is to export my-requests to excel.
         */
        String export = aRequest.getParameter("export");

        if ((export != null) && (export.trim().equals("") == false)) {

            // SysPrefix is a mandatory parameter.
            String sysPrefix = aRequest.getParameter("sysPrefix");

            if ((sysPrefix == null) || (sysPrefix.trim().equals("") == true)) {
                out.println(Messages.getMessage("INVALID_BUSINESS_AREA"));

                return;
            }

            // Get the corresponding business area object.
            BusinessArea ba = null;

            try {
                ba = BusinessArea.lookupBySystemPrefix(sysPrefix);

                if (ba == null) {
                    out.println(Messages.getMessage("INVALID_BUSINESS_AREA"));

                    return;
                }
            } catch (DatabaseException de) {
                out.println(Messages.getMessage("INVALID_BUSINESS_AREA"));

                return;
            }

            String dql    = getDQL(ba, user, filter);
            String encDQL = "";

            // URL-Encode this dql.
            try {
                encDQL = URLEncoder.encode(dql, "UTF-8");
            } catch (UnsupportedEncodingException uee) {
                encDQL = dql;
            }

            String url = WebUtil.getServletPath(aRequest, "/search/") + sysPrefix + "?q=" + encDQL + "&format=excel";

            LOG.info("URL: " + url);
            aResponse.sendRedirect(url);

            return;
        }

        /*
         * If the sysList is empty/null, then get the list of BAs that qualify
         * for the above role filter and the current user. Filter out those BAs
         * which the user wanted to be hidden by default.
         *
         * If the sysList is neither null nor empty, then convert the sysList
         * into an arraylist of prefixes and set the prefixList to this value.
         */
        ArrayList<String> prefixList = null;

        if ((sysList == null) || (sysList.trim().equals("") == true)) {
            String strRoleFilter = Utilities.arrayListToString(myRoleFilter);

            prefixList = BusinessArea.getUserBAList(userId, strRoleFilter, myIsPrimary);
            prefixList = filterBAsToHide(userConfig, prefixList);
        } else {
            prefixList = Utilities.toArrayList(sysList);
        }

        /*
         * Get the Business Area objects corresponding to the prefixes in the
         * list.
         */
        ArrayList<BusinessArea> baList = new ArrayList<BusinessArea>();

        for (String sysPrefix : prefixList) {
            BusinessArea ba = BusinessArea.lookupBySystemPrefix(sysPrefix);

            baList.add(ba);
        }

        /*
         * Sort the business areas by display name. My Requests will be
         * displayed in this order
         */
        BusinessArea.setSortField(BusinessArea.DISPLAYNAME);
        baList = BusinessArea.sort(baList);

        int    aFlags = JAVASCRIPT | ANCHOR | LINK_SUBJECT | MOUSEOVER;
        String str    = getMyRequests(aRequest, userId, baList, userConfig, myRoleFilter, myIsPrimary, prefZone, aFlags);

        /*
         * Special case where I want to see the query as a part of the output
         * for debugging purpose. The request parameter is test and the value
         * must be "sarma".
         */
        String test = aRequest.getParameter("test");

        if ((test != null) && test.trim().equals("sarma")) {
            out.println(myDatabaseQuery);
        }

        out.println(str);

        return;
    }

    /**
     * This method returns the join part of the database filtering query.
     *
     * @return Join part of filtering query.
     */
    private String prepareJoinPart(int aUserId) {

        /*
         * T0D0->DONE:
         *   1. Get the field id of status from the field object instead
         *      of hard-coding the value.
         */
        Field statusField   = ourDistinctFieldTable.get(Field.STATUS);
        int   statusFieldId = 4;

        if (statusField != null) {
            statusFieldId = statusField.getFieldId();
        }

        StringBuffer buffer = new StringBuffer();

        buffer.append("\n\trequests r").append("\n\tJOIN request_users ru").append("\n\tON r.sys_id = ru.sys_id AND ").append("r.request_id = ru.request_id AND ").append("ru.user_id = ").append(
            aUserId).append("\n\t").append("");

        return buffer.toString();
    }

    //~--- get methods --------------------------------------------------------

    /**
     * This method returns the conditional part corresponding to the given BA.
     *
     * @param aSystemId         BA Id.
     * @param aUserId           User Id
     * @param aStatusFilter     Status filter
     * @param aRoleFilter       Role Filter
     * @param aPrimary          True if ony primary assignee filter is selected
     *                          but not the assignee filter.
     * @return  Conditional part corresponding to this BA.
     */
    private String getBACondition(int aSystemId, int aUserId, ArrayList<String> aStatusFilter, ArrayList<String> aRoleFilter, boolean aPrimary) {
        StringBuffer buffer = new StringBuffer();

        buffer.append("\n\t(").append("\n\t\tr.sys_id = ").append(aSystemId).append(" AND ").append("\n\t\tr.status_id ");

        if (aStatusFilter != null) {
            if (aStatusFilter.size() == 1) {
                buffer.append(" = ").append(aStatusFilter.get(0));
            } else {
                buffer.append(" IN (").append(Utilities.arrayListToString(aStatusFilter)).append(") ");
            }
        }

        buffer.append(" AND \n\t\tru.user_type_id");

        if (aRoleFilter != null) {
            if (aRoleFilter.size() == 1) {
                buffer.append(" = ").append(aRoleFilter.get(0));
            } else {
                buffer.append(" IN (").append(Utilities.arrayListToString(aRoleFilter)).append(") ");
            }
        }

        if (aPrimary == true) {
            buffer.append(" AND ").append("\n\t\t(").append("\n\t\t\t(").append("\n\t\t\t\tru.is_primary = 0 AND ").append("\n\t\t\t\tru.user_type_id <> 3").append("\n\t\t\t) OR ").append(
                "\n\t\t\t(").append("\n\t\t\t\tru.is_primary = 1 AND ").append("\n\t\t\t\tru.user_type_id = 3").append("\n\t\t\t)").append("\n\t\t)").append("");
        }

        buffer.append("\n\t)").append("");

        return buffer.toString();
    }

    /**
     * This method returns the comma separated list of statuses that are
     * selected by default in the specified Business area.
     *
     * @param systemId     Business Area Id.
     * @param isPrivate    True if the user is authorized to view private types.
     *
     * @return Comma separated list of statuses.
     */
    private ArrayList<String> getCheckedStatusIds(int systemId) {
        ArrayList<String> buffer = new ArrayList<String>();
        boolean           first  = true;

        try {
            ArrayList<Type> statusList = Type.lookupBySystemIdAndFieldName(systemId, Field.STATUS);

            for (Type type : statusList) {

                // Ignore inactive types.
                if (type.getIsActive() == false) {
                    continue;
                }

                // Ignore the unchecked types.
                if (type.getIsChecked() == false) {
                    continue;
                }

                buffer.add(Integer.toString(type.getTypeId()));
            }
        } catch (Exception e) {
            LOG.warn("",(e));
        }

        return buffer;
    }

    /**
     * This method returns the comma separated list of statuses that are
     * selected by default in the specified Business area.
     *
     * @param systemId     Business Area Id.
     * @param isPrivate    True if the user is authorized to view private types.
     *
     * @return Comma separated list of statuses.
     */
    private String getCheckedStatuses(int systemId, boolean doubleQuote) {
        String       quote  = (doubleQuote == DOUBLE_QUOTE)
                              ? "\""
                              : "'";
        StringBuffer buffer = new StringBuffer();
        boolean      first  = true;

        try {
            ArrayList<Type> statusList = Type.lookupBySystemIdAndFieldName(systemId, Field.STATUS);

            for (Type type : statusList) {

                // Ignore inactive types.
                if (type.getIsActive() == false) {
                    continue;
                }

                // Ignore the unchecked types.
                if (type.getIsChecked() == false) {
                    continue;
                }

                if (first == false) {
                    buffer.append(",");
                } else {
                    first = false;
                }

                buffer.append(quote).append(type.getDisplayName()).append(quote);
            }
        } catch (Exception e) {
            LOG.warn("",(e));
        }

        if (first == true) {
            buffer.append("-1");
        }

        return buffer.toString();
    }

    /**
     * This method returns the DQL for my-requests for a given BA and given
     * user.
     *
     * @param aBA
     * @param aUser
     * @param aFilter
     * @return
     * @throws DatabaseException
     */
    private String getDQL(BusinessArea aBA, User aUser, int aFilter) throws DatabaseException {
        StringBuffer    dql          = new StringBuffer();
        StringBuffer    ufs          = new StringBuffer();
        StringBuffer    sfs          = new StringBuffer();
        int             systemId     = aBA.getSystemId();
        String          sysPrefix    = aBA.getSystemPrefix();
        String          userLogin    = aUser.getUserLogin();
        WebConfig       userConfig   = aUser.getWebConfigObject();
        String          primDescLog  = "";
        String          primDescAss  = "";
        String          primDescSub  = "";
        String          primDescStat = "";
        FieldDescriptor fd           = null;

        fd           = FieldDescriptor.getPrimaryDescriptor(systemId, Field.LOGGER);
        primDescLog  = ((fd != null)
                        ? fd.getDescriptor()
                        : Field.LOGGER);
        fd           = FieldDescriptor.getPrimaryDescriptor(systemId, Field.ASSIGNEE);
        primDescAss  = ((fd != null)
                        ? fd.getDescriptor()
                        : Field.ASSIGNEE);
        fd           = FieldDescriptor.getPrimaryDescriptor(systemId, Field.SUBSCRIBER);
        primDescSub  = ((fd != null)
                        ? fd.getDescriptor()
                        : Field.SUBSCRIBER);
        fd           = FieldDescriptor.getPrimaryDescriptor(systemId, Field.STATUS);
        primDescStat = ((fd != null)
                        ? fd.getDescriptor()
                        : Field.STATUS);

        /*
         * DQL returned by this method consists of two parts
         * 1. User Role filter.
         * 2. Status filter.
         *
         * User Role filter is prepared from the aFilter parameter.
         */
        ufs.append("(");

        if (aFilter == 0) {
            ufs.append(primDescLog).append(":").append(userLogin).append(" OR ").append(primDescAss).append(":").append(userLogin).append(" OR ").append(primDescSub).append(":").append(userLogin)
            ;
        } else {
            boolean first  = true;
            boolean hasAss = false;

            if ((aFilter & FILTER_LOGGER) != 0) {
                ufs.append(primDescLog).append(":").append(userLogin);
                first = false;
            }

            if ((aFilter & FILTER_ASSIGNEE) != 0) {
                if (first == false) {
                    ufs.append(" OR ");
                } else {
                    first = false;
                }

                ufs.append(primDescAss).append(":").append(userLogin);
                hasAss = true;
            }

            if ((aFilter & FILTER_SUBSCRIBER) != 0) {
                if (first == false) {
                    ufs.append(" OR ");
                } else {
                    first = false;
                }

                ufs.append(primDescSub).append(":").append(userLogin);
            }

            if ((aFilter & FILTER_LOGGER) != 0) {

                /*
                 * If assignee filter is already present, then this is not
                 * required. In the other case, this should be present.
                 */
                if (hasAss == false) {
                    if (first == false) {
                        ufs.append(" OR ");
                    } else {
                        first = false;
                    }

                    ufs.append(primDescAss).append(":*").append(userLogin);
                }
            }
        }

        ufs.append(")");

        /*
         * Get the status filter.
         */
        BAConfig          baConfig     = userConfig.getBAConfig(sysPrefix);
        ArrayList<String> sf           = baConfig.getStatusFilter();
        String            statusFilter = getStatusList(systemId, sf, DOUBLE_QUOTE);

        sfs.append(primDescStat).append(":(").append(statusFilter).append(")");
        dql.append(ufs.toString()).append(" ").append(sfs.toString());

        return dql.toString();
    }

    /**
     * Accessor method for DatabaseTime property.
     *
     * @return Current Value of DatabaseTime
     *
     */
    public long getDatabaseTime() {
        return myDatabaseTime;
    }

    /**
     * Accessor method for IsPrimary property.
     *
     * @return Current Value of IsPrimary
     *
     */
    public boolean getIsPrimary() {
        return myIsPrimary;
    }

    /**
     * This method prepares the query for obtaining the My Requests of a user
     * for the given BA list , HTML serializes these results and returns them.
     *
     * @param aRequest    Http Request object to access session.
     * @param aUserId     Id of the user.
     * @param aBAList     List of BAs from where the user want the MyRequests to
     *                    be retrieved.
     * @param aConfig     User configuration object.
     * @param aRoles      Role filter.
     * @param aIsPrimary  True if only primary assignee filter is selected and
     *                    not assignee filter.
     * @param aPrefZone   Zone where the user is present.
     * @param aFlags      Flags used during rendering.
     * @return            My Requests rendered as HTML.
     * @throws IOException
     * @throws FileNotFoundException
     */
    public String getMyRequests(HttpServletRequest aRequest, int aUserId, ArrayList<BusinessArea> aBAList, WebConfig aConfig, ArrayList<String> aRoles, boolean aIsPrimary, TimeZone aPrefZone,
                                int aFlags)
            throws IOException, FileNotFoundException, DatabaseException {
        StringBuffer content = new StringBuffer();

        // Return if the BA list is either null or empty.
        if ((aBAList == null) || (aBAList.size() == 0)) {
            return content.toString();
        }

        content.append(executeQuery(aRequest, aUserId, aBAList, aConfig, aRoles, aIsPrimary, aPrefZone, aFlags));

        return content.toString();
    }

    /**
     * This method checks if the filter for my-requests page is present in
     * Http Request or Http Session. If not found, then the user's configured
     * filter in the options page is returned.
     *
     * @param aRequest Http Request Object.
     * @param aFilter  Default filter configured by the user in the
     *                 options page.
     *
     * @return Filter for my-requests.
     */
    int getMyRequestsFilter(HttpServletRequest aRequest, int aFilter) {
        HttpSession session = aRequest.getSession();
        int         filter  = aFilter;

        //
        // Check if the filter is present in the Http Request. If so that
        // will take precedence
        //
        String strFilter = aRequest.getParameter("filter");

        if ((strFilter != null) && (strFilter.trim().equals("") == false)) {
            try {
                filter = Integer.parseInt(strFilter);
            } catch (Exception e) {
                LOG.info("Using the default value due to exception: " + aFilter + "\n" + "",(e));
                filter = aFilter;
            }
        } else {

            // Check if it can be found in the session.
            Integer temp = (Integer) session.getAttribute("MY_REQUESTS_FILTER");

            if (temp != null) {
                filter = temp.intValue();
                LOG.info("Using the value obtained from session: " + filter);
            }
        }

        // Store the value in session for later reference.
        session.setAttribute("MY_REQUESTS_FILTER", filter);

        return filter;
    }

    /**
     * This method prepares the database query and returns it.
     *
     * @param aUserId           User Id.
     * @param aBAList           List of BAs to be considered
     * @param fixedFields       Out Param: List of fixed fields.
     * @param userFields        Out Param: List of user fields.
     * @param extFields         Out Param: List of extended fields.
     * @param aConfig           User configuration.
     * @param aRoles            Role Filter
     * @param aIsPrimary        True if only primary assignee is selected
     *
     * @return Database query.
     */
    private String getQuery(int aUserId, ArrayList<BusinessArea> aBAList, ArrayList<String> fixedFields, ArrayList<String> userFields, ArrayList<String> extFields, WebConfig aConfig,
                            ArrayList<String> aRoles, boolean aIsPrimary)
            throws DatabaseException {
        StringBuffer               query     = new StringBuffer();
        StringBuffer               wherePart = new StringBuffer();
        Hashtable<String, Boolean> table     = new Hashtable<String, Boolean>();
        boolean                    first     = true;

        /*
         * For each business area:
         *      1. Get the conditional part for the query.
         *      2. Get the user configured display header and segregate them
         *         into fixed, user and extended fields.
         */
        StringBuilder sysIdList = new StringBuilder();

        for (BusinessArea ba : aBAList) {
            int               systemId     = ba.getSystemId();
            String            sysPrefix    = ba.getSystemPrefix();
            BAConfig          baConfig     = aConfig.getBAConfig(sysPrefix);
            ArrayList<String> sf           = baConfig.getStatusFilter();
            ArrayList<String> statusFilter = getStatusIdList(systemId, sf);

            if (first == false) {
                sysIdList.append(",");
                wherePart.append("\n\tOR");
            } else {
                first = false;
            }

            sysIdList.append(systemId);

            // Step 1.
            wherePart.append(getBACondition(systemId, aUserId, statusFilter, aRoles, aIsPrimary));

            // Step 2.
            ArrayList<String> header = baConfig.getDisplayHeader();

            for (String column : header) {

                // Get the corresponding field object.
                try {
                    Field field = Field.lookupBySystemIdAndFieldName(systemId, column);

                    if (field == null) {
                        continue;
                    }

                    // Ignore inactive fields.
                    if (field.getIsActive() == false) {
                        continue;
                    }

                    int permission = field.getPermission();

                    // Ignore non-displayable fields.
                    if ((permission & Permission.DISPLAY) == 0) {
                        continue;
                    }

                    String fieldName = field.getName().toLowerCase();

                    if (table.get(fieldName) == null) {
                        if (field.getIsExtended() == true) {
                            extFields.add(column);
                        } else if (field.getDataTypeId() == DataType.USERTYPE) {
                            if (fieldName.equals(Field.USER)) {
                                fixedFields.add(column);
                            } else {
                                userFields.add(column);
                            }
                        } else {
                            fixedFields.add(column);
                        }

                        table.put(column, true);
                    }
                } catch (Exception e) {
                    e.printStackTrace();

                    continue;
                }
            }
        }

        // Get the join part of the database query.
        String joinPart = prepareJoinPart(aUserId);

        query.append("\nINSERT INTO @tmp1").append("\nSELECT DISTINCT ").append("\n\tr.sys_id,").append("\n\tr.request_id,").append("\n\tr.parent_request_id").append("\nFROM").append(joinPart).append(
            "\nWHERE ").append("r.sys_id IN (").append(sysIdList).append(") AND ").append(wherePart).append("\nORDER BY r.sys_id").append("\n").append("\nSELECT count(*) 'result_count' from @tmp1");

        /*
         * Second select statement is needed to be in sync with the output
         * of the search query.
         */
        // Nitiraj : removing this ..
//        append("\nSELECT count(*) 'result_count' from @tmp1").append("\n").append("\n");

        // Done.
        return query.toString();
    }

    /**
     * Accessor method for RenderTime property.
     *
     * @return Current Value of RenderTime
     *
     */
    public long getRenderTime() {
        return myRenderTime;
    }

    /**
     * This method returns a string of comma-separated list of role filters
     * for the given filter value.
     *
     * @param filter  Integer representing the composite role filter.
     * @return  Comma-separated list of role filters.
     */
    ArrayList<String> getRoleFilter(int filter) {
        myIsPrimary = false;

        boolean assignee = false;

        if ((filter & FILTER_LOGGER) != 0) {
            myRoleFilter.add("2");
        }

        if ((filter & FILTER_SUBSCRIBER) != 0) {
            myRoleFilter.add("4");
        }

        if ((filter & FILTER_PRIMARY_ASSIGNEE) != 0) {
            myRoleFilter.add("3");
            assignee    = true;
            myIsPrimary = true;
        }

        if ((filter & FILTER_ASSIGNEE) != 0) {
            if (assignee == false) {
                myRoleFilter.add("3");
            }

            /*
             * If both assignee and primary assignee is selected, then
             * assignee is given preference as all primarily assigned tickets
             * come under assigned tickets.
             */
            myIsPrimary = false;
        }

        return myRoleFilter;
    }

    /**
     * This method returns the status filter as comma separated string.
     *
     * @param aSystemId    Id of the BA.
     * @param statusList   List of statuses.
     * @return Status filter as a string.
     */
    private ArrayList<String> getStatusIdList(int aSystemId, ArrayList<String> statusList) {
        ArrayList<String> sList = new ArrayList<String>();

        if (statusList == null) {
            statusList = new ArrayList<String>();
        }

        if (statusList.size() == 0) {
            sList = getCheckedStatusIds(aSystemId);
        } else {
            boolean flag = true;

            for (String str : statusList) {
                Type status = null;

                try {
                    status = Type.lookupBySystemIdAndFieldNameAndTypeName(aSystemId, Field.STATUS, str);

                    if (status != null) {
                        sList.add(Integer.toString(status.getTypeId()));
                    } else {
                        LOG.info("No status object found corresponding to " + str);
                    }
                } catch (Exception e) {
                    StringBuilder message = new StringBuilder();

                    message.append("an exception has occurred while retrieving ").append("the status object for ").append(str).append(TBitsLogger.getStackTrace(e));
                    LOG.severe(message.toString());
                }
            }
        }

        return sList;
    }

    /**
     * This method returns the status filter as comma separated string.
     *
     * @param aSystemId    Id of the BA.
     * @param statusList   List of statuses.
     * @return Status filter as a string.
     */
    private String getStatusList(int aSystemId, ArrayList<String> statusList, boolean doubleQuote) {
        String       quote = (doubleQuote == DOUBLE_QUOTE)
                             ? "\""
                             : "'";
        StringBuffer sList = new StringBuffer();

        if (statusList == null) {
            statusList = new ArrayList<String>();
        }

        if (statusList.size() == 0) {
            sList.append(getCheckedStatuses(aSystemId, doubleQuote));
        } else {
            boolean flag = true;

            for (String str : statusList) {
                if (flag == false) {
                    sList.append(",");
                } else {
                    flag = false;
                }

                sList.append(quote).append(str).append(quote);
            }
        }

        return sList.toString();
    }

    //~--- set methods --------------------------------------------------------

    /**
     * This method alters the showBA property of the BAConfig objects of BAs
     * whose prefixes are specified in the prefixList.
     *
     * @param prefixList  List of prefixes of BAs whose showBA flag should be
     *                    changed.
     * @param user        Current User.
     * @param flag        True/False.
     */
    private void setCollapse(ArrayList<String> prefixList, User user, boolean flag) {
        WebConfig userConfig = user.getWebConfigObject();
        boolean   changed    = false;

        for (String prefix : prefixList) {
            BAConfig config = userConfig.getBAConfig(prefix);

            if (config.getCollapseBA() != flag) {
                config.setCollapseBA(flag);
                changed = true;
                userConfig.setBAConfig(prefix, config);
            }
        }

        /*
         * If atleast one BAConfig is changed, then update the web_config
         * column of the user record in the database.
         */
        if (changed == true) {
            try {
                user.setWebConfig(userConfig.xmlSerialize());
                User.update(user);
            } catch (DatabaseException e) {
                LOG.warn("",(e));
            }
        }
    }

    /**
     * This method alters the showBA property of the BAConfig objects of BAs
     * whose prefixes are specified in the prefixList.
     *
     * @param prefixList  List of prefixes of BAs whose showBA flag should be
     *                    changed.
     * @param user        Current User.
     * @param flag        True/False.
     */
    private void setShow(ArrayList<String> prefixList, User user, boolean flag) {
        WebConfig userConfig = user.getWebConfigObject();
        boolean   changed    = false;

        for (String prefix : prefixList) {
            BAConfig config = userConfig.getBAConfig(prefix);

            if (config.getShowBA() != flag) {
                config.setShowBA(flag);
                changed = true;
                userConfig.setBAConfig(prefix, config);
            }
        }

        /*
         * If atleast one BAConfig is changed, then update the web_config
         * column of the user record in the database.
         */
        if (changed == true) {
            try {
                user.setWebConfig(userConfig.xmlSerialize());
                User.update(user);
            } catch (DatabaseException e) {
                LOG.warn("",(e));
            }
        }
    }
}
