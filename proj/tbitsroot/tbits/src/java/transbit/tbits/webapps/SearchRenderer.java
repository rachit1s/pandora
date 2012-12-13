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
 * SearchRenderer.java
 *
 * $Header:
 *
 */
package transbit.tbits.webapps;

//~--- non-JDK imports --------------------------------------------------------

import transbit.tbits.Helper.Messages;
import transbit.tbits.common.DTagReplacer;

//Search views.
//TBits Imports.
import transbit.tbits.common.DatabaseException;
import transbit.tbits.common.TBitsLogger;
import transbit.tbits.config.WebConfig;
import transbit.tbits.domain.BusinessArea;
import transbit.tbits.domain.DataType;
import transbit.tbits.domain.Field;
import transbit.tbits.domain.FieldDescriptor;
import transbit.tbits.domain.Permission;
import transbit.tbits.domain.Type;
import transbit.tbits.domain.User;
import transbit.tbits.exception.DSQLParseException;
import transbit.tbits.exception.TBitsException;
import transbit.tbits.search.Searcher;

import static transbit.tbits.Helper.TBitsConstants.ASC_ORDER;

//Static imports.
import static transbit.tbits.Helper.TBitsConstants.PKG_WEBAPPS;
import static transbit.tbits.webapps.HtmlSearch.ALL_AREAS_FILE;
import static transbit.tbits.webapps.HtmlSearch.ourAllAreasTagList;

//~--- JDK imports ------------------------------------------------------------

//Java imports.
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;

import java.util.ArrayList;
import java.util.Hashtable;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

//~--- classes ----------------------------------------------------------------

//Third-party imports.

/**
 * This servlet mostly does rendering of different views in search page.
 *
 * @author  Vaibhav.
 * @version $Id: $
 */
public class SearchRenderer extends HttpServlet {

    // Application logger.
    public static TBitsLogger LOG = TBitsLogger.getLogger(PKG_WEBAPPS);

    // Html interface to save shortcut.
    public static final String MY_REQUESTS_CONFIG = "web/tbits-configure-myrequests.htm";

    // Table that maps the currentFilter with its corresponding filter options.
    public static Hashtable<Integer, String> ourFilterOptions;

    // Table that maps the text filter option with its corresponding filter.
    public static Hashtable<String, String> ourTextFilterOptions;

    //~--- static initializers ------------------------------------------------

    static {

        // Options based on my-requests filter.
        StringBuilder buffer = new StringBuilder();
        String        filter = "";

        ourTextFilterOptions = new Hashtable<String, String>();
        buffer               = new StringBuilder();
        filter               = "subject";
        buffer.append("\n<OPTION value='subject' SELECTED>Subject</OPTION>").append("\n<OPTION value='alltext'>All Text</OPTION>").append("\n<OPTION value='none'>-------------------</OPTION>").append(
            "\n<OPTION value='summary'>Summary</OPTION>").append("\n<OPTION value='all'>Text + Attachments</OPTION>");
        ourTextFilterOptions.put(filter, buffer.toString());
        buffer = new StringBuilder();
        filter = "alltext";
        buffer.append("\n<OPTION value='subject'>Subject</OPTION>").append("\n<OPTION value='alltext' SELECTED>All Text</OPTION>").append("\n<OPTION value='none'>-------------------</OPTION>").append(
            "\n<OPTION value='summary'>Summary</OPTION>").append("\n<OPTION value='all'>Text + Attachments</OPTION>");
        ourTextFilterOptions.put(filter, buffer.toString());
        buffer = new StringBuilder();
        filter = "summary";
        buffer.append("\n<OPTION value='subject'>Subject</OPTION>").append("\n<OPTION value='alltext'>All Text</OPTION>").append("\n<OPTION value='none'>-------------------</OPTION>").append(
            "\n<OPTION value='summary' SELECTED>Summary</OPTION>").append("\n<OPTION value='all'>Text + Attachments</OPTION>");
        ourTextFilterOptions.put(filter, buffer.toString());
        buffer = new StringBuilder();
        filter = "all";
        buffer.append("\n<OPTION value='subject'>Subject</OPTION>").append("\n<OPTION value='alltext'>All Text</OPTION>").append("\n<OPTION value='none'>-------------------</OPTION>").append(
            "\n<OPTION value='summary'>Summary</OPTION>").append("\n<OPTION value='all' SELECTED>Text + Attachments").append("</OPTION>");
        ourTextFilterOptions.put(filter, buffer.toString());
    }

    //~--- methods ------------------------------------------------------------

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
        HttpSession aSession = aRequest.getSession(true);

        try {
            long start = System.currentTimeMillis();

            handleRequest(aRequest, aResponse);

            long end = System.currentTimeMillis();
        } catch (FileNotFoundException fnfe) {
            LOG.warn("",(fnfe));

            TBitsException de = new TBitsException(Messages.getMessage("HTML_INTERFACE_MISSING"));

            aSession.setAttribute("ExceptionObject", de);
            aResponse.sendRedirect(WebUtil.getServletPath(aRequest, "/error"));

            return;
        } catch (Exception de) {
            LOG.warn("",(de));
            aSession.setAttribute("ExceptionObject", de);
            aResponse.sendRedirect(WebUtil.getServletPath(aRequest, "/error"));

            return;
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

        //
        // Get all the mandatory objects required from the Http Request object.
        // - PrintWriter to output the HTML content.
        // - Session object to store the results and other configurations.
        // - User object of the user accessing this page.
        //
        aResponse.setContentType("text/html");

        PrintWriter out        = aResponse.getWriter();
        HttpSession aSession   = aRequest.getSession();
        User        user       = WebUtil.validateUser(aRequest);
        WebConfig   userConfig = user.getWebConfigObject();
        int         userId     = user.getUserId();
        String      searchView = aRequest.getParameter("sv");

        if (searchView != null) {
            String sysPrefix = aRequest.getParameter("sysPrefix");

            if ((sysPrefix != null) && (sysPrefix.equals("") != true)) {
                String key    = sysPrefix + "_view";
                String genKey = "view";

                aSession.setAttribute(key, searchView);
                aSession.setAttribute(genKey, searchView);
            }

            if (!searchView.equalsIgnoreCase("allareas") &&!searchView.equalsIgnoreCase("aa") &&!searchView.equalsIgnoreCase("4")) {
                out.println("");

                return;
            }

            // Get the list of business areas the user can view.
            ArrayList<BusinessArea> baList = BusinessArea.getBusinessAreasByUserId(userId);

            // Get the text filter if any.
            String filter = getTextFilter(aRequest);

            // Render the all_areas_view.
            String renderedHtml = renderAllAreasView(aRequest, sysPrefix, baList, user, filter);

            out.println(renderedHtml);

            return;
        }

        String parseQuery = aRequest.getParameter("parseQuery");

        if (parseQuery != null) {
            long   start = System.currentTimeMillis();
            String query = aRequest.getParameter("query");

            if ((query == null) || (query.trim().equals("") == true)) {

                // Null/emtpy queries are well formed queries.
                out.println("true");

                return;
            }

            String       sysPrefix = aRequest.getParameter("sysPrefix");
            BusinessArea ba        = null;

            try {
                ba = BusinessArea.lookupBySystemPrefix(sysPrefix);
            } catch (Exception e) {
                LOG.info("Exception while looking up the BA table by prefix: " + sysPrefix);
                out.println("false");

                return;
            }

            int      systemId = ba.getSystemId();
            Searcher searcher = new Searcher(systemId, userId, query);

            try {
                searcher.parse();
            } catch (DSQLParseException dsqle) {
                LOG.info("Exception while parsing query: " + query + "\n" + "",(dsqle));
                out.println("false");
                out.println(dsqle.toString());

                return;
            }

            long end = System.currentTimeMillis();

            LOG.info("Time taken to validate a query: " + (end - start) + " ms");
            out.println("true");

            return;
        }
    }

    /**
     * This method renders the All Areas View of search page.
     * @param aRequest TODO
     * @param aBAList     List of business areas.
     * @param aUser       User.
     * @param aTextFilter current text filter.
     */
    public static String renderAllAreasView(HttpServletRequest aRequest, String aPrefix, ArrayList<BusinessArea> aBAList, User aUser, String aTextFilter) throws DatabaseException, FileNotFoundException, IOException {
        DTagReplacer              hp       = new DTagReplacer(ALL_AREAS_FILE);
        Hashtable<String, String> tagTable = new Hashtable<String, String>();

        tagTable.put("aa_textFilters", renderTextFilters(aTextFilter));
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

        tagTable.put("aa_status_id_list", renderStatusList(statList));
        tagTable.put("aa_severity_id_list", renderSeverityList(sevList));
        tagTable.put("sysPrefix", aPrefix);
        tagTable.put("nearestPath", WebUtil.getNearestPath(aRequest, ""));

        // Other tags will be empty.
        replaceTags(hp, tagTable, ourAllAreasTagList);

        return hp.parse(0);
    }

    /**
     * This method renders the BA list.
     *
     * @param  aBAList  List of business areas.
     *
     * @return HTML.
     */
    public static String renderBAList(ArrayList<BusinessArea> aBAList) throws DatabaseException {
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
     * This method returns the given list of severities as Options in an HTML
     * SELECT with Low, Medium, High, Critical at the top followed by
     * the other severities.
     *
     * @param sevList List of Severities.
     *
     * @return HTML Options of these Severities.
     */
    public static String renderSeverityList(ArrayList<String> sevList) {
        StringBuffer buffer = new StringBuffer();

        buffer.append("\n<OPTION value=''>All</OPTION>").append("\n<OPTION value='Low' SELECTED>Low</OPTION>").append("\n<OPTION value='Medium' SELECTED>Medium</OPTION>").append(
            "\n<OPTION value='High' SELECTED>High</OPTION>").append("\n<OPTION value='Critical' SELECTED>Critical</OPTION>");

        for (String str : sevList) {
            String lstr = str.toLowerCase();

            if (lstr.equals("low") || lstr.equals("medium") || lstr.equals("high") || lstr.equals("critical")) {
                continue;
            }

            buffer.append("\n<OPTION value='").append(str).append("' SELECTED>").append(str).append("</OPTION>");
        }

        return buffer.toString();
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
    public static String renderStatusList(ArrayList<String> statList) {
        StringBuffer buffer = new StringBuffer();

        buffer.append("\n<OPTION value=''>All</OPTION>").append("\n<OPTION value='Open' SELECTED>open</OPTION>").append("\n<OPTION value='Active' SELECTED>active</OPTION>").append(
            "\n<OPTION value='Suspended' SELECTED>suspended</OPTION>").append("\n<OPTION value='Closed'>closed</OPTION>");

        for (String str : statList) {
            String lstr = str.toLowerCase();

            if (lstr.equals("open") || lstr.equals("active") || lstr.equals("suspended") || lstr.equals("closed")) {
                continue;
            }

            buffer.append("\n<OPTION value='").append(str).append("' SELECTED>").append(str.toLowerCase()).append("</OPTION>");
        }

        return buffer.toString();
    }

    /**
     * This method returns the HTML for rendering the Text Filters.
     *
     * @param filter  Filter that should be selected.
     *
     * @return HTML for rendering text filters.
     */
    public static String renderTextFilters(String filter) {
        if ((filter == null) || filter.trim().equals("")) {
            filter = "subject";
        } else {
            filter = filter.trim();
        }

        String value = ourTextFilterOptions.get(filter);

        return value;
    }

    /**
     * This method replaces all the tags with their corresponding values
     * reading them from the given table in the given HTML Parser object.
     *
     * @param aDTagReplacer Parser that holds the Html Content with tags.
     * @param aTagTable     Table with [tag, value object]
     * @param aTagList      List of tags to be replaced.
     *
     */
    public static void replaceTags(DTagReplacer aDTagReplacer, Hashtable<String, String> aTagTable, ArrayList<String> aTagList) {
        if (aTagList == null) {
            return;
        }

        int tagListSize = aTagList.size();

        for (String key : aTagList) {
            String value = aTagTable.get(key);

            if (value != null) {
                aDTagReplacer.replace(key, value);
            } else {
                aDTagReplacer.replace(key, "");
            }
        }
    }

    //~--- get methods --------------------------------------------------------

    /**
     * This method returns the information required by the advanced search
     * builder in JavaScriptObjectNotation format.
     *
     * @param ba         Business Area object.
     * @param isPrivate  Flag to specify if user have view on private.
     *
     * @return JSON object encapsulating the data need for Advanced search
     *         interface.
     */
    public static String getAdvancedSearchData(BusinessArea ba, boolean isPrivate) throws DatabaseException {
        StringBuffer buffer    = new StringBuffer();
        StringBuffer fieldInfo = new StringBuffer();
        StringBuffer typeInfo  = new StringBuffer();

        fieldInfo.append("var fieldInfo = \n{");
        typeInfo.append("var typeInfo = \n{");

        int    systemId  = ba.getSystemId();
        String sysPrefix = ba.getSystemPrefix();

        // Get the field information.
        ArrayList<Field> fieldList = Field.lookupBySystemId(systemId);

        //
        // Sort the fields in ascending order of their display names.
        //
        Field.setSortParams(Field.DISPLAYNAME, ASC_ORDER);
        fieldList = Field.sort(fieldList);

        boolean first     = true;
        boolean typeFirst = true;

        for (Field field : fieldList) {
            if (field == null) {
                continue;
            }

            // Skip if the field is inactive.
            if (field.getIsActive() == false) {
                continue;
            }

            // Skip if the field is not searchable.
            if ((field.getPermission() & Permission.SEARCH) == 0) {
                continue;
            }

            // Skip if the field is private and the user does not have
            // permission to view private fields.
            if ((field.getIsPrivate() == true) && (isPrivate == false)) {
                continue;
            }

            int                        fieldId     = field.getFieldId();
            String                     fieldName   = field.getName();
            String                     displayName = field.getDisplayName();
            int                        dataTypeId  = field.getDataTypeId();
            ArrayList<FieldDescriptor> descList    = FieldDescriptor.lookupListBySystemIdAndFieldId(systemId, fieldId);

            FieldDescriptor.setSortParams(FieldDescriptor.DESCRIPTOR, ASC_ORDER);
            descList = FieldDescriptor.sort(descList);

            if (first == false) {
                fieldInfo.append(",");
            } else {
                first = false;
            }

            fieldInfo.append("\n\t\"").append(fieldName).append("\":").append("\n\t{").append("\n\t\t\"displayName\": \"").append(displayName).append("\",").append("\n\t\t\"dataType\": \"").append(
                dataTypeId).append("\",").append("\n\t\t\"descList\": \"").append(getDescList(descList)).append("\"").append("\n\t}");

            if (dataTypeId == DataType.TYPE) {

                // Get the type information for this field.
                ArrayList<Type> typeList = Type.lookupBySystemIdAndFieldName(systemId, fieldName);

                Type.setSortParams(Type.ORDERING, ASC_ORDER);
                typeList = Type.sort(typeList);

                if (typeFirst == false) {
                    typeInfo.append(",");
                } else {
                    typeFirst = false;
                }

                typeInfo.append("\n\t\"").append(fieldName).append("\":").append("\n\t[").append(getTypeJSON(typeList, isPrivate)).append("\n\t]");
            }
        }

        /*
         * Now that we are done with adding the fields in this business area,
         * add the custom fields to the adv search panel.
         */

        /*
         * Add the custom field to search on requests appended by a user.
         */
        if (first == false) {
            fieldInfo.append(",");
        } else {
            first = false;
        }

        fieldInfo.append("\n\t\"updater\":").append("\n\t{").append("\n\t\t\"displayName\": \"Updated By\",").append("\n\t\t\"dataType\": \"10\",").append(
            "\n\t\t\"descList\": \"appender,actionuser\"").append("\n\t}");

        /*
         * Add the read filter to the list of field details for the advanced
         * search builder to include this in the field drop down.
         */
        fieldInfo.append(",");
        fieldInfo.append("\n\t\"read\":").append("\n\t{").append("\n\t\t\"displayName\": \"Read\",").append("\n\t\t\"dataType\": \"1\",").append("\n\t\t\"descList\": \"read\"").append("\n\t}");
        fieldInfo.append("\n};\n");
        typeInfo.append("\n};\n");
        buffer.append(fieldInfo.toString()).append("\n").append(typeInfo.toString());

        return buffer.toString();
    }

    /**
     * This method returns the comma separated list of descriptors.
     *
     * @param descList Descriptor list.
     *
     * @return Comma separated list of descriptors.
     */
    private static String getDescList(ArrayList<FieldDescriptor> descList) {
        StringBuffer buffer = new StringBuffer();
        boolean      first  = true;

        for (FieldDescriptor fd : descList) {
            String desc = fd.getDescriptor();

            if (first == false) {
                buffer.append(",");
            } else {
                first = false;
            }

            buffer.append(desc);
        }

        return buffer.toString();
    }

    /**
     * This method returns the Text Filter if present in the QueryString of the
     * Http Request.
     *
     * @param aRequest HttpServletRequest Object.
     *
     * @return Filter if present. Empty otherwise.
     */
    public static String getTextFilter(HttpServletRequest aRequest) {
        String filter = aRequest.getParameter("filter");

        if (filter == null) {
            filter = aRequest.getParameter("f");

            if (filter == null) {
                filter = "";
            }
        }

        return filter;
    }

    /**
     * This method return the types in JSON format.
     *
     * @param typeList  List of types
     * @param isPrivate User's ability to view private types.
     *
     * @return Returns the JSON of the type information.
     */
    private static String getTypeJSON(ArrayList<Type> typeList, boolean isPrivate) {
        StringBuffer buffer = new StringBuffer();
        boolean      first  = true;

        for (Type type : typeList) {
            if (type.getIsActive() == false) {
                continue;
            }

            if ((type.getIsPrivate() == true) && (isPrivate == false)) {
                continue;
            }

            if (first == false) {
                buffer.append(",");
            } else {
                first = false;
            }

            buffer.append("\n\t\t{").append("\n\t\t\t\"name\": \"").append(type.getName()).append("\",")
            				.append("\n\t\t\t\"displayname\": \"").append(type.getDisplayName()).append("\",")
            				.append("\n\t\t\t\"selected\": \"").append(type.getIsChecked()).append("\"").append(
                "\n\t\t}");
        }

        return buffer.toString();
    }
}
