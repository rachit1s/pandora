/*
 * Copyright (c) 2005 Transbit Technologies Pvt. Ltd. All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of Transbit Technologies Pvt. Ltd. ("Confidential Information").  You
 * shall not disclose such Confidential Information and shall use
 * it only in accordance with the terms of the license agreement
 * you entered into with Transbit Technologies Pvt. Ltd.
 */



/*
 * ActionDetails.java
 *
* $Header:$
 */
package transbit.tbits.webapps;

//~--- non-JDK imports --------------------------------------------------------
import transbit.tbits.api.AttachmentInfo;
import transbit.tbits.authentication.AuthConstants;
import transbit.tbits.ExtUI.SlotFillerFactory;
import transbit.tbits.Helper.ActionHelper;
import transbit.tbits.Helper.Messages;
import transbit.tbits.Helper.TBitsConstants;
import transbit.tbits.Helper.TBitsHelper;
import transbit.tbits.Helper.TBitsPropEnum;
import transbit.tbits.common.DTagReplacer;

//TBits Imports
import transbit.tbits.common.ActionFileInfo;
import transbit.tbits.common.DatabaseException;
import transbit.tbits.common.PropertiesHandler;
import transbit.tbits.common.TBitsLogger;
import transbit.tbits.common.Timestamp;
import transbit.tbits.common.Utilities;
import transbit.tbits.config.CaptionsProps;
import transbit.tbits.config.SysConfig;
import transbit.tbits.config.WebConfig;
import transbit.tbits.domain.Action;
import transbit.tbits.domain.BusinessArea;
import transbit.tbits.domain.DataType;
import transbit.tbits.domain.DisplayGroup;
import transbit.tbits.domain.Field;
import transbit.tbits.domain.Permission;
import transbit.tbits.domain.Request;
import transbit.tbits.domain.RequestEx;
import transbit.tbits.domain.RolePermission;
import transbit.tbits.domain.TransferredRequest;
import transbit.tbits.domain.Type;
import transbit.tbits.domain.User;
import transbit.tbits.domain.UserReadAction;
import transbit.tbits.exception.TBitsException;
import transbit.tbits.report.GetAuditInfo;
import transbit.tbits.search.ParseEntry;
import transbit.tbits.search.SearchConstants.Connective;

import static transbit.tbits.Helper.ActionHelper.FIRST_ACTION_CLASS;
import static transbit.tbits.Helper.ActionHelper.NEW_ACTION_CLASS;
import static transbit.tbits.Helper.ActionHelper.READ_ACTION_CLASS;
import static transbit.tbits.Helper.ActionHelper.READ_ACTION_HEADER_CLASS;

//TBits static imports
import static transbit.tbits.Helper.ActionHelper.TEXT_CLASS;
import static transbit.tbits.Helper.ActionHelper.TEXT_CONTENT_CLASS;
import static transbit.tbits.Helper.ActionHelper.TEXT_HEADER_CLASS;
import static transbit.tbits.Helper.TBitsConstants.DYNAMIC_TOOLTIP;
import static transbit.tbits.Helper.TBitsConstants.NO_TOOLTIP;
import static transbit.tbits.Helper.TBitsConstants.PKG_WEBAPPS;
import static transbit.tbits.Helper.TBitsConstants.RSS_DATE_FORMAT;
import static transbit.tbits.Helper.TBitsConstants.SOURCE_CMDLINE;
import static transbit.tbits.Helper.TBitsConstants.SOURCE_WEB;

//~--- JDK imports ------------------------------------------------------------

//Java Imports.
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Serializable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.TimeZone;
import java.util.TreeMap;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

//import org.apache.batik.dom.util.HashTable;

import com.google.gson.JsonObject;

//~--- classes ----------------------------------------------------------------

/**
 * This servlet is used to display all the action details of the Request
 *
 * @author  Vinod Gupta
 * @version $Id: $
 */
public class ActionDetails extends HttpServlet implements TBitsPropEnum, TBitsConstants {
    public static final String REQUEST_UPDATE_FILES = "requestUpdateFiles";
	public static final TBitsLogger LOG         = TBitsLogger.getLogger(PKG_WEBAPPS);
    private static final String     PATH        = PropertiesHandler.getProperty(KEY_REDIRECTION_URL);
    private static final String     IMG_PATH    = "web/images/";
    public static final String      UPDATE_FORM = "web/tbits-update-request.htm";
    private static final String     HTML_FILE   = "web/tbits-action-details.htm";
    public static ArrayList<String> TAG_LIST;
    public static ArrayList<String> ourUpdateTagList;
    private HashMap<String, String> captions = CaptionsProps.getInstance().getCaptionsHashMap(0);
    private static final int DEFAULT_DISPLAY_GROUP = 1;

    private static ArrayList<String> myActionYUIJSs ;
    private static ArrayList<String> myActionTbitsJSs ;
    //~--- static initializers ------------------------------------------------

    static /*public void init()*/ {
    	// added tag called extHeaderLinks for extended header links on the top of the view request page : filled by ExtUIRenderer plugins ( if required )
        TAG_LIST = Utilities.toArrayList("requestId,addAction,addSubRequest,addRequest,parent," + "extendedFieldsHeader,summaryHeader,memoHeader,"
                                         + "extendedTextFieldsHeader,userDraftsInfo,transferDisplay," + "nearestPath,cssFile,updateDivDisplay,singleIEWindow,"
                                         + "updateDivContent,isTransferred,isLocked,transferInfo,actionDetailsHeaderSlot,YUIJSs,tbitsJSs," + ActionDetails.REQUEST_UPDATE_FILES);
    	// added tag called extSubmitButtonList for extended header links on the bottom of update request page : filled by ExtUIRenderer plugins ( if required )
        ourUpdateTagList = Utilities.toArrayList(

			// append("sub_category_label,") and append("sub_category_list,") added while trying to add Education on the search panel.
            new StringBuffer().append("systemId,").append("sysPrefix,").append("loggedDate,").append("sendMail,").append("mailLoggers,").append("caller,").append("requestId,").append(
                "show_exception_block,").append("action_diff_exception,").append("openSummary,").append("openExtended,").append("openAssociate,").append("openRelated,").append("csList,").append(
                "execFunctions,").append("maxActionId,").append("diffList,").append("repliedToAction,").append("diffFieldList,").append("due_datetime_option,").append("DTimestamp,").append(
                "autoSaveRate,").append("nearestPath,").append("category_id_label,").append("category_id_disabled,").append("category_id_list,").append("severity_id_label,").append(
                "severity_id_disabled,").append("severity_id_list,").append("privateClassName,").append("is_private,").append("is_private_label,").append("is_private_disabled,").append(
                "exception_list,").append("assignee_ids_label,").append("assignee_ids,").append("assignee_ids_disabled,").append("logger_ids_label,").append("logger_ids,").append(
                "logger_ids_disabled,").append("status_id_label,").append("status_id_disabled,").append("status_id_list,").append("request_type_id_label,").append("request_type_id_disabled,").append(
                "request_type_id_list,").append("office_id_label,").append("office_id_disabled,").append("office_id_list,").append("office_id_display,").append("notify_label,").append(
                "notify,").append("notify_disabled,").append("notify_loggers,").append("notify_loggers_disabled,").append("datetime_display,").append("due_datetime_label,").append(
                "due_datetime,").append("due_time_disabled,").append("due_datetime_disabled,").append("due_datetime_img_display,").append("due_datetime_box_display,").append(
                "due_date_option,").append("nearestPath,").append("time_option_disabled,").append("disableTime,").append("cc_ids_label,").append("cc_ids,").append("cc_ids_disabled,").append(
                "subscriber_ids_label,").append("subscriber_ids,").append("subscriber_ids_disabled,").append("subject_label,").append("subject,").append("subject_disabled,").append("efLink,").append(
                "attachLink,").append("associateLink,").append("relatedLink,").append("summaryLink,").append("extended_fields,").append("attachCtrl,").append("associateCtrl,").append(
                "relatedCtrl,").append("summaryCtrl,").append("description_label,").append("description,").append("description_disabled,").append("showDiff,").append("is_draft,userLogin,display_logout").append(
                "adminLink,").append("category_id_dependency_list,").append("category_id_dep_fun_call,").append("request_type_id_dependency_list,")
                .append("request_type_id_dep_fun_call,").append("severity_id_dependency_list,").append("severity_id_dep_fun_call,").append("status_id_dependency_list,").append("status_id_dep_fun_call")
                .append("office_id_dep_fun_call,").append("office_id_dependency_list,").append("calendar_date_format,").append("updateRequestFooterSlot,").append("draftId,").append(AddHtmlRequest.REQUEST_FILES).toString());
        
        //<%=nearestPath%>c?<%=nearestPath%>web/yui/build/yahoo-dom-event/yahoo-dom-event.js&<%=nearestPath%>web/yui/build/element/element-min.js&<%=nearestPath%>web/yui/build/datasource/datasource-min.js&<%=nearestPath%>web/yui/build/datatable/datatable-min.js&<%=nearestPath%>web/yui/build/logger/logger-debug.js&<%=nearestPath%>web/yui/build/uploader/uploader.js&<%=nearestPath%>web/yui/build/utilities/utilities.js&<%=nearestPath%>web/yui/build/container/container-min.js&<%=nearestPath%>web/yui/build/menu/menu-min.js&<%=nearestPath%>web/yui/build/button/button-min.js&<%=nearestPath%>web/yui/build/editor/editor-min.js&<%=nearestPath%>web/yui/build/json/json-min.js&<%=nearestPath%>web/yui/build/utilities/utilities.js&<%=nearestPath%>web/yui/build/yahoo/yahoo-min.js&<%=nearestPath%>web/yui/build/event/event-min.js&<%=nearestPath%>web/yui/build/connection/connection-min.js"
        myActionYUIJSs = Utilities.toArrayList("web/yui/build/yahoo-dom-event/yahoo-dom-event.js,"+
											"web/yui/build/element/element-min.js,"+
											"web/yui/build/datasource/datasource-min.js,"+
											"web/yui/build/datatable/datatable-min.js,"+
											"web/yui/build/logger/logger-debug.js,"+
											"web/yui/build/uploader/uploader.js,"+
											"web/yui/build/container/container-min.js,"+
											"web/yui/build/menu/menu-min.js,"+
											"web/yui/build/button/button-min.js,"+
											"web/yui/build/json/json-min.js,"+
											"web/yui/build/utilities/utilities.js,"+
											"web/yui/build/yahoo/yahoo-min.js,"+
											"web/yui/build/event/event-min.js,"+
											"web/yui/build/connection/connection-min.js") ;
        
        // <%=nearestPath%>c?<%=nearestPath%>web/scripts/tbits-yui-utils.js&<%=nearestPath%>web/scripts/cal-min.js&<%=nearestPath%>web/scripts/uploader.js&<%=nearestPath%>web/scripts/ajax-uploader.js
        myActionTbitsJSs = Utilities.toArrayList(
							        		"web/scripts/tbits-yui-utils.js,"+
							        		"web/scripts/cal-min.js,"+
							        		"web/scripts/uploader.js,"+
							        		"web/scripts/add-request.js,"+
							        		"web/ckeditor/ckeditor.js,"	+
							        		"web/scripts/ajax-uploader.js"
        									);
        
    }

    //~--- fields -------------------------------------------------------------

    private String myOldSummary           = null;
    
    private String EXTENDED_FIELD_HTML_FILE = "web/tbits-extended_fields_header.htm";
    private String EXTENDED_FIELD_HTML      = "\n<td class=\"b ch\" align=\"right\" valign=\"top\" nowrap>" + "<=label></td>" + "\n<td align=\"left\" valign=\"top\" width=\"30%\"><=value></td>";
    private String DISPLAY_GROUP_HTML		= "\n<tr valign=\"top\" align=\"left\"><td style=\"font-weight: bold;\" colspan=\"6\">\n	<table width=\"100%\" cellspacing=\"0\" cellpadding=\"0\" border=\"0\">\n		<tbody><tr>\n				<td width=\"2%\" valign=\"middle\"><img height=\"1\" width=\"100%\" align=\"absmiddle\" src=\"/web/images/hline.gif\"> </td>\n				<td nowrap=\"\" class=\"s l cb b\"><=display_group_name></td>\n				<td width=\"100%\">\n					<img height=\"1\" width=\"100%\" align=\"absmiddle\" src=\"/web/images/hline.gif\">\n				</td>\n			</tr>\n		</tbody>\n	</table>\n</td></tr>";
    //~--- methods ------------------------------------------------------------

    /**
     * This method check if user has permissions to access the request.
     * @param aHttpRequest TODO
     * @param aPermissions   User permissions
     * @param aTagTable      Hashtable collecting <field,value> pairs
     * @param aBusinessArea  Business area object.
     * @param aRequest       Request object.
     *
     *
     * @exception DETBitsExceptionhen user doesn't have permissions
     * @exception DatabaseException
     */
    private void checkAccessPermissions(HttpServletRequest aHttpRequest, User user, Hashtable<String, Integer> aPermissions, Hashtable<String, String> aTagTable, BusinessArea aBusinessArea, Request aRequest)
            throws TBitsException, DatabaseException {
        int permission = 0;
        int systemId   = aBusinessArea.getSystemId();
        String reqTag = aBusinessArea.getSystemPrefix() + "#" + aRequest.getRequestId();
        //
        // Check if user has VIEW permission over business area.
        //
        Integer perm = aPermissions.get(Field.BUSINESS_AREA);

        if (perm != null) {
            permission = perm;
        }

        if ((permission & Permission.VIEW) == 0) 
        {
        	LOG.info("User " + user.getUserLogin() + " does not have view permission on " + Field.BUSINESS_AREA + " for the request : " + reqTag);
        	throw new TBitsException(Messages.getMessage("VIEW_REQUEST_ERROR", aBusinessArea.getDisplayName()));
        }

        // check if hte user has permission to view the request.
        Integer rperm = aPermissions.get(Field.REQUEST);
        if( null == rperm )
        	rperm = 0 ;
        if( (rperm & Permission.VIEW) == 0 )
        {
        	LOG.info("User " + user.getUserLogin() + " does not have view permission on " + Field.REQUEST + " for the request : " + reqTag);        	
        	throw new TBitsException("You do not have sufficient permissions to view this request.");
        }
        //
        // Checking if this is a private request, or if this request is in
        // private type or a private business area. In this case, check if
        // the user has VIEW permission on IS_PRIVATE field.
        //
        perm = aPermissions.get(Field.IS_PRIVATE);

        if (perm != null) 
        {
            permission = perm;
        }
        else
        {
        	permission = 0 ;
        }
        
        boolean noPrivateViewPerm = ((permission & Permission.VIEW) == 0);

        if ((aRequest.getIsPrivate() == true) || (aRequest.getCategoryId().getIsPrivate() == true) || (aRequest.getSeverityId().getIsPrivate() == true)
                || (aRequest.getStatusId().getIsPrivate() == true) || (aRequest.getRequestTypeId().getIsPrivate() == true) || (aBusinessArea.getIsPrivate() == true)) {
            if (noPrivateViewPerm == true) {
                throw new TBitsException((Messages.getMessage("PRIVATE_REQUEST_ERROR", aBusinessArea.getDisplayName())));
            }

            StringBuilder tooltip = new StringBuilder();

            tooltip.append("Private (");

            boolean first = true;

            if (aBusinessArea.getIsPrivate() == true) {
                tooltip.append("BA");
                first = false;
            }

            if (aRequest.getCategoryId().getIsPrivate() == true) {
                if (first == false) {
                    tooltip.append(", ");
                } else {
                    first = false;
                }

                tooltip.append("Category");
            }

            if (aRequest.getStatusId().getIsPrivate() == true) {
                if (first == false) {
                    tooltip.append(", ");
                } else {
                    first = false;
                }

                tooltip.append("Status");
            }

            if (aRequest.getSeverityId().getIsPrivate() == true) {
                if (first == false) {
                    tooltip.append(", ");
                } else {
                    first = false;
                }

                tooltip.append(captions.get("captions.all.camel_case_severity"));
            }

            if (aRequest.getRequestTypeId().getIsPrivate() == true) {
                if (first == false) {
                    tooltip.append(", ");
                } else {
                    first = false;
                }

                tooltip.append(captions.get("captions.all.request_type"));
            }

            if (aRequest.getIsPrivate() == true) {
                if (first == false) {
                    tooltip.append(", ");
                } else {
                    first = false;
                }

                tooltip.append(captions.get("captions.all.camel_case_request"));
            }

            tooltip.append(")");
            aTagTable.put("privateSymbol", "<td align=\"left\" width=\"16\">" + "<img class=\"at\" alt=\"" + tooltip.toString() 
            		+ "\" src=\"" + WebUtil.getNearestPath(aHttpRequest, IMG_PATH) + "private_big.gif\"></td>");
        }

        //
        // If any parent in the request hierarchy is private,
        // check if the user has VIEW permission on IS_PRIVATE field.
        //
//        if (noPrivateViewPerm == true) {
//            int     parentId      = aRequest.getParentRequestId();
//            Request parentRequest = null;
//
//            while (parentId != 0) {
//                parentRequest = Request.lookupBySystemIdAndRequestId(systemId, parentId);
//
//                if (parentRequest != null) {
//                    if (parentRequest.getIsPrivate() == true) {
//                        String displayName = aBusinessArea.getDisplayName();
//
//                        throw new TBitsException(Messages.getMessage("PRIVATE_REQUEST_ERROR", displayName));
//                    }
//
//                    parentId = parentRequest.getParentRequestId();
//                } else {
//                    parentId = 0;
//                }
//            }
//        }

        return;
    }

    /**
     * The doGet method of the servlet.
     *
     * @param  aHttpRequest          the HttpServlet Request Object
     * @param  aHttpResponse         the HttpServlet Response Object
     * @throws ServeletException
     * @throws IOException
     */
    public void doGet(HttpServletRequest aHttpRequest, HttpServletResponse aHttpResponse) throws ServletException, IOException {
        aHttpResponse.setContentType("text/html; charset=UTF-8");
        aHttpResponse.setCharacterEncoding("UTF-8");

    	PrintWriter out     = aHttpResponse.getWriter();
        HttpSession session = aHttpRequest.getSession();

        Utilities.registerMDCParams(aHttpRequest);

        try {
            long start = System.currentTimeMillis();

            handleGetRequest(aHttpRequest, aHttpResponse);

            long end = System.currentTimeMillis();

            LOG.debug("Time taken to display View Request Page: " + (end - start) + " mecs");
        } catch (TBitsException e) {
            LOG.info("User: " + aHttpRequest.getRemoteUser() + "\npathInfo: " + aHttpRequest.getPathInfo() + "\n" + "",(e));
            session.setAttribute("ExceptionObject", e);
            aHttpResponse.sendRedirect(WebUtil.getServletPath(aHttpRequest, "/error"));

            return;
        } catch (RuntimeException e) {
            LOG.info("User: " + aHttpRequest.getRemoteUser() + "\npathInfo: " + aHttpRequest.getPathInfo() + "\n" + "",(e));
            session.setAttribute("ExceptionObject", e);
            aHttpResponse.sendRedirect(WebUtil.getServletPath(aHttpRequest, "/error"));

            return;
        } catch (Exception e) {
            LOG.info("User: " + aHttpRequest.getRemoteUser() + "\npathInfo: " + aHttpRequest.getPathInfo() + "\n" + "",(e));
            session.setAttribute("ExceptionObject", e);
            aHttpResponse.sendRedirect(WebUtil.getServletPath(aHttpRequest, "/error"));

            return;
        } finally {
            Utilities.clearMDCParams();
        }
    }

    /**
     * The doGet method of the servlet.
     *
     * @param  aHttpRequest          the HttpServlet Request Object
     * @param  aHttpResponse         the HttpServlet Response Object
     * @throws ServeletException
     * @throws IOException
     */
    public void doPost(HttpServletRequest aHttpRequest, HttpServletResponse aHttpResponse) throws ServletException, IOException {
        doGet(aHttpRequest, aHttpResponse);
    }

    /**
     * This function fills the action details, summary and memo data.
     * @param aHttpRequest TODO
     * @param aTagTable      Hashtable collecting <field,value> pairs
     * @param aPermissions   User permissions
     * @param aUser          User object
     * @param aBusinessArea  BusinessArea object
     * @param aRequest       Request object
     * @param aSortOrder     Sort order for actions
     *
     * @return List of attachments from all actions.
     */
    private Hashtable<String, String> fillActionDetails(HttpServletRequest aHttpRequest, Hashtable<String, String> aTagTable, Hashtable<String, Integer> aPermissions, User aUser, BusinessArea aBusinessArea,
            Request aRequest, String aSortOrder, String aDateFormat, TimeZone aZone, ArrayList<String> aSearchTextList, boolean aShowLog)
            throws DatabaseException {
        Hashtable<String, String> attachmentsList = new Hashtable<String, String>();
        int                       systemId        = aBusinessArea.getSystemId();
        int                       requestId       = aRequest.getRequestId();
        int                       userId          = aUser.getUserId();

        myOldSummary = null;

        //
        // Get all actions of the request.
        //
        ArrayList<Action> actionList = Action.getAllActions(systemId, requestId, aSortOrder);
        Hashtable<Integer, Collection<ActionFileInfo>> actionFileHash = Action.getAllActionFiles(systemId, requestId);
        //
        // Show History Tag sort order/image based on
        // if request has more than one append and user's default sort order
        //
        if (actionList.size() == 1) {
            aTagTable.put("requestHistory", "<span class=\"b cw\">" + captions.get("captions.view.request_history") + "</span>");
        } else {
            if (aSortOrder.equals("asc")) {
                aTagTable.put("requestHistory",
                              "<a class=\"l b cw\"" + "href=\"javascript:sortActions('desc');\"" + "onmouseover=\"textMouseOver" + "(event,'" + captions.get("captions.view.request_history") + "');\" "
                              + "onmouseout=\"textMouseOut();\">" + captions.get("captions.view.request_history") + "&nbsp;&nbsp;" + "<img class=\"am\" src=\"" + WebUtil.getNearestPath(aHttpRequest, IMG_PATH) + "up-white.gif\" " + "onmouseover=\"textMouseOver"
                              + "(event,'" + captions.get("captions.view.request_history") + "');\" " + "onmouseout=\"textMouseOut();\"/></a>");
            } else {
                aTagTable.put("requestHistory",
                              "<a class=\"l b cw\"" + "href=\"javascript:sortActions('asc');\"" + "onmouseover=\"textMouseOver" + "(event,'" + captions.get("captions.view.request_history") + "');\" "
                              + "onmouseout=\"textMouseOut();\">" + captions.get("captions.view.request_history") + "&nbsp;&nbsp;" + "<img class=\"am\" src=\"" + WebUtil.getNearestPath(aHttpRequest, IMG_PATH) + "down-white.gif\" " + "onmouseover=\"textMouseOver"
                              + "(event,'" + captions.get("captions.view.request_history") + "');\" " + "onmouseout=\"textMouseOut();\"/></a>");
            }
        }

        int maxActionId = aRequest.getMaxActionId();

        aTagTable.put("maxActionId", "" + maxActionId);

        /*
         * users's last read action will be tracked if user has
         * enabled Visual Enhancements.
         *  -- If entry present in user_read_actions, pick it else
         *  -- Mark as completely unread if
         *      - the request is not closed or if
         *      - the request is closed but updated in last 'N' days
         *        where 'N' is a configuration for this BA.
         */
        int readActionId = 0;

        if (TBitsHelper.isVEEnable(aUser.getUserId(), systemId) == false) {
            readActionId = maxActionId;
        } else {
            UserReadAction ura = UserReadAction.lookupBySystemIdAndRequestIdAndUserId(systemId, requestId, userId);

            if (ura != null) {
                readActionId = ura.getActionId();
            } else {
                String    status    = aRequest.getStatusId().getName();
                Timestamp updDate   = new Timestamp(aRequest.getLastUpdatedDate().getTime());
                Timestamp today     = new Timestamp();
                SysConfig sysConfig = aBusinessArea.getSysConfigObject();
                long      n         = (sysConfig == null)
                                      ? 90
                                      : sysConfig.getMaxGrayPeriod();

                // N is in days convert into milliseconds.
                long maxAllowedDiff = n * 86400000;
                long actualDiff     = today.getTime() - updDate.getTime();

                status = (status == null)
                         ? ""
                         : status.trim();

                if ((status.equalsIgnoreCase("closed") == false) || (actualDiff < maxAllowedDiff)) {
                    readActionId = 0;
                } else {
                    readActionId = maxActionId;
                }
            }
        }

        aTagTable.put("userReadAction", "" + readActionId);

        StringBuilder sb                = new StringBuilder();
        String        actionClass       = "";
        String        actionHeaderClass = "";
        String        contentClass      = "dvm bw ";
        String        textColorClass    = "";
        String        actionLog         = "";
        String        actionLogLink     = "";
        String        editContentLink   = "";
        String        attachments       = "";
        String        description       = "";
        String        appender          = "";
        String        repliedTo         = "";
        String        updateTime        = "";
        int           actionId          = 0;

        // Check if the user can change descritpion.
        boolean cDesc = WebUtil.canChange(aPermissions, Field.DESCRIPTION);

        // Check if the user can change attachments.
        boolean cAtt           = WebUtil.canChange(aPermissions, Field.ATTACHMENTS);
        boolean canEditContent = true;

        if ((cDesc == false) && (cAtt == false)) {
            canEditContent = false;
        }

        //
        // Set if summary info is to be loaded
        //
        boolean loadSummary = (aTagTable.get("summaryHeader") == null)
                              ? false
                              : true;

        if (loadSummary == true) {
            if (aSortOrder.equals("asc")) {
                for (int i = actionList.size() - 1; i >= 0; i--) {
                    Action action = actionList.get(i);

                    if (loadSummary == true) {
                        loadSummary = loadTextFieldsData(aTagTable, Field.SUMMARY, action, aDateFormat, aZone);
                    }

                    if ((myOldSummary == null) && (action.getSummary() != null) &&!action.getSummary().trim().equals("")) {
                        if ((aRequest.getSummary() != null) && (action.getSummary().equals(aRequest.getSummary()) == true)) {
                            continue;
                        }

                        myOldSummary = action.getSummary();

                        // mySummaryActions.add(action);
                    }
                }
            } else {
                for (Action action : actionList) {
                    if (loadSummary == true) {
                        loadSummary = loadTextFieldsData(aTagTable, Field.SUMMARY, action, aDateFormat, aZone);
                    }

                    if ((myOldSummary == null) && (action.getSummary() != null) &&!action.getSummary().trim().equals("")) {
                        if ((aRequest.getSummary() != null) && (action.getSummary().equals(aRequest.getSummary()) == true)) {
                            continue;
                        }

                        myOldSummary = action.getSummary();

                        // mySummaryActions.add(action);
                    }
                }
            }
        }

        if (myOldSummary == null) {
            myOldSummary = "";
        }

        String img = "<span class=\"l b cbk\" " + "onclick=\"javascript:showHide('" + Field.SUMMARY + "_id');\"><img src=\"" + WebUtil.getNearestPath(aHttpRequest, IMG_PATH) + "t-r.gif\" class=\"am\">&nbsp;";

        aTagTable.put("diffSummary",
                      ActionHelper.getTextFieldHtml(TEXT_CLASS, TEXT_HEADER_CLASS, TEXT_CONTENT_CLASS, "", "", Field.SUMMARY + "_id", "", img + "<%=" + Field.SUMMARY + "_label%>" + "</span>",
                          TBitsHelper.getDiffHtml(myOldSummary, aRequest.getSummary()), "<%=" + Field.SUMMARY + "_updated_by%>", "<%=" + Field.SUMMARY + "_updated_time%>", "", "", "", "",
                          "<%=diff%>"));
        aTagTable.put("diff", "true");

        for (Action action : actionList) {
            actionClass       = "";
            actionHeaderClass = "";
            textColorClass    = "";
            actionLog         = "";
            actionLogLink     = "";
            editContentLink   = "";
            attachments       = "";
            description       = "";
            appender          = "";
            repliedTo         = "";
            updateTime        = "";
            actionId          = 0;
            actionId          = action.getActionId();

            //
            // Format Description
            //
            description = ActionHelper.formatDescription(action.getDescription(), aBusinessArea.getSystemPrefix(), WebUtil.getNearestPath(aHttpRequest, ""), aRequest.getRequestId(), maxActionId, maxActionId,
                    aSearchTextList, SOURCE_WEB, DYNAMIC_TOOLTIP, action.getDescriptionContentType() == CONTENT_TYPE_TEXT);

            //
            // Set Appender information
            //
            appender = actionId + ". " + ActionHelper.getUserDisplayName(action.getUserId());

            //
            // Set replied-to info, if any
            //
            if (action.getRepliedToAction() != 0) {
                int repliedAction = action.getRepliedToAction();

                repliedTo = "<span class=\"cal\">&nbsp;&nbsp;&nbsp;" + "[ in response to " + "<a class=\"l cb\" href=\"#" + repliedAction + "\">Update#" + repliedAction + "</a> ]</span>";
            } else {
                repliedTo = "";
            }

            //
            // Set lasdt update time
            //
            updateTime = WebUtil.getDateInFormat(action.getLastUpdatedDate(), aZone, aDateFormat);

            //
            // Format attachments, if any
            //
//            attachments = (action.getAttachments() == null)
//                          ? ""
//                          : action.getAttachments();
//
//            if (attachments.equals("") == false) {
//                String attachStr = ActionHelper.formatAttachments(attachments, TBitsConstants.HTML_FORMAT, WebUtil.getServletPath(aHttpRequest, ""), aBusinessArea.getSystemPrefix());
//
//                attachments = ActionHelper.ATTACHMENT_CONTAINER_HTML.replace("<=attachments>", attachStr);
//
//                //
//                // Put attachments in the Hashtable for tooltip
//                //
//                String[] temp = attachStr.split("<br>");
//
//                for (int i = 0; i < temp.length; i++) {
//                    attachmentsList.put("Update#" + actionId + "/" + i, temp[i]);
//                }
//            }
            
            Collection<ActionFileInfo> actionFiles = actionFileHash.get(action.getActionId());
            if((actionFiles != null) && (actionFiles.size() > 0))
            {
            	String attachStr = ActionHelper.formatAttachments(actionFiles,TBitsConstants.HTML_FORMAT, WebUtil.getServletPath(aHttpRequest, ""), 
            			aBusinessArea.getSystemPrefix(), action.getRequestId(), action.getActionId());
            	attachments = ActionHelper.ATTACHMENT_CONTAINER_HTML.replace("<=attachments>", attachStr);
            }

            //
            // Format action Log, if any, based on permissions
            //
            actionLog = (action.getHeaderDescription() == null)
                        ? ""
                        : action.getHeaderDescription();

            String headerDesc = ActionHelper.formatActionLog(systemId, actionLog, aPermissions, TBitsConstants.HTML_FORMAT, false, aBusinessArea.getSystemPrefix(), WebUtil.getNearestPath(aHttpRequest, ""),
                                    aRequest.getRequestId(), maxActionId, maxActionId, SOURCE_WEB, DYNAMIC_TOOLTIP);

            actionLog = ActionHelper.ACTION_LOG_CONTAINER_HTML.replace("<=actionLog>", headerDesc).replace("<=id>", "actionLog" + actionId);

            //
            // Display Action log for 1st and last Action.
            // For other actions hise it and show onmouseover.
            //
            if ((actionId == 1) || (actionId == maxActionId) || (aShowLog == true)) {
                actionLog     = actionLog.replace("<=display>", "");
                actionLogLink = "";
            } else {
                actionLog = actionLog.replace("<=display>", "none");

                if (headerDesc.equals("") == false) {
                    actionLogLink = "<span class=\"l cb sx\" " + "onmouseover=\"return " + "makeTrue(domTT_activate(this, event, 'content', " + "document.getElementById('actionLog" + actionId
                                    + "').innerHTML, 'statusText', 'Update Log'));\">" + "Update Log</span>";
                }
            }

            if (canEditContent == true) {
                if (actionLogLink.equals("") == false) {
                    editContentLink = "&nbsp;|&nbsp;&nbsp";
                } else {
                    editContentLink = "";
                }

                // NITI EDIT : Commented this line 
              //  editContentLink = editContentLink + "<a class=\"l cb sx\" " + "href=\"javascript:editContent('" + WebUtil.getServletPath(aHttpRequest, "/edit-action/") + aBusinessArea.getSystemPrefix() + "?sysPrefix="
              //                    + aBusinessArea.getSystemPrefix() + "&requestId=" + aRequest.getRequestId() + "&actionId=" + actionId + "')\">Edit Content</a>&nbsp&nbsp;";
               
                // NITI EDIT : ADDED THIS LINE 
               editContentLink = "" ; 
            } else {
                editContentLink = "";
            }

            //
            // Setting action class based on first/read/new action
            //
            if (actionId > readActionId) {
                actionClass       = NEW_ACTION_CLASS;
                actionHeaderClass = READ_ACTION_HEADER_CLASS;

                // actionHeaderClass = NEW_ACTION_HEADER_CLASS;
                textColorClass = "";
            } else if (actionId == 1) {
                actionClass       = FIRST_ACTION_CLASS;
                actionHeaderClass = READ_ACTION_HEADER_CLASS;

                // actionHeaderClass = FIRST_ACTION_HEADER_CLASS;
                textColorClass = "";
            } else {
                actionClass       = READ_ACTION_CLASS;
                actionHeaderClass = READ_ACTION_HEADER_CLASS;
                textColorClass    = "";
            }

            //
            // Form the action html form the above formatted data.
            //
            sb.append(ActionHelper.getTextFieldHtml(actionClass, actionHeaderClass, contentClass, textColorClass, "action" + actionId, "", "", appender, description, repliedTo, updateTime,
                    attachments, actionLog, actionLogLink, editContentLink, ""));
        }

        aTagTable.put("actionDetails", sb.toString());

        return attachmentsList;
    }

    /**
     * This method  fills attachments toolTip tag. I think it is not required anymore -SG
     * @param aHttpRequest TODO
     * @param aTagTable      Hashtable collecting <field,value> pairs
     * @param aAttachments   Hashtable of <actionId, attachment>
     */
    private void fillAttachmentsToolTip(HttpServletRequest aHttpRequest, Hashtable<String, String> aTagTable, Hashtable<String, String> aAttachments) {
        if (aAttachments.size() > 0) {
            getRelationsHtml(aTagTable, aAttachments, "Attachments", "attachmentList",
                             !aTagTable.get("parent").equals("") ||!aTagTable.get("siblingRequests").equals("") ||!aTagTable.get("subRequests").equals("")
                             ||!aTagTable.get("relatedRequests").equals(""), aHttpRequest);
        } else {
            aTagTable.put("attachmentList", "");
        }

        return;
    }

    /**
     * This method fills the extended header Container for fields over which
     * user has view permissions.
     * @param aHttpRequest TODO
     * @param aTagTable      Hashtable collecting <field,value> pairs
     * @param aPermissions   User permissions
     * @param aRequest       Request object
     */
    private void fillExtendedHeaderHtmlContainer(HttpServletRequest aHttpRequest, Hashtable<String, String> aTagTable, Hashtable<String, Integer> aPermissions, Request aRequest, boolean aExpandHeader)
            throws DatabaseException, FileNotFoundException, IOException {
        int systemId   = aRequest.getSystemId();
        int permission = 0;

        //
        // Insert Extended Fields Header Container
        // and Extended Text Fields Containers, if extended fields present.
        //
        
        ArrayList<Field> fieldsList = Field.getExtendedFieldsBySystemId(systemId);
        
       // Field.setSortParams(Field.DISPLAYORDER, TBitsConstants.ASC_ORDER);
       // fieldsList = Field.sort(fieldsList);
        
        if ((fieldsList != null) && (fieldsList.size() > 0)) {
            StringBuilder extendedFields     = new StringBuilder();
            StringBuilder extendedTextFields = new StringBuilder();
            StringBuilder extendedUserTypeFields=new StringBuilder();
            int counter=0;

            ArrayList<DisplayGroup> dgList = DisplayGroup.lookupIncludingDefaultForSystemId(systemId);
            int dgCount = dgList.size();
            StringBuffer[] bufArray = new StringBuffer[dgCount];
            Hashtable<Integer,Integer> dgMap = new Hashtable<Integer, Integer>();
            int count = 0;
            for (DisplayGroup dg : dgList){
            	dgMap.put(dg.getId(), count++);
            }
            
            Hashtable<Integer,Integer> dgCountMap = new Hashtable<Integer, Integer>();
            for (DisplayGroup dg: dgList){
            	dgCountMap.put(dg.getId(), 0);
            }

            for (Field field : fieldsList) {

            	//
            	// Check if user has VIEW permission over the field,
            	// if no, don't display it and continue;
            	//
            	Integer perm = aPermissions.get(field.getName());

            	if (perm != null) {
            		permission = perm;
            	}

            	if ((permission & Permission.VIEW) == 0) {
            		continue;
            	}
            	
            	if(field.getDataTypeId() == DataType.ATTACHMENTS)
            			continue;

            	//
            	// User has VIEW permision, so add field placeholder based on
            	// if its text field or any other type
            	//
            	//
            	// If text non empty field, add separate Text container
            	//
            	if (field.getDataTypeId() == DataType.TEXT) {
            		if ((aRequest.get(field.getName()) != null) && (aRequest.get(field.getName()).equals("") == false)) {
            			String display = ((aExpandHeader == true)
            					? ""
            							: "none");
            			String img     = "<span class=\"l b cbk\" " + 
            			"onclick=\"javascript:showHide('" + field.getName() + "_content','"+field.getName()+"_img','"+WebUtil.getNearestPath(aHttpRequest, IMG_PATH) + "t-r.gif','"+WebUtil.getNearestPath(aHttpRequest, IMG_PATH) + "t-b.gif');\">" +
            					"<img id=\""+field.getName()+"_img\" src=\"" + WebUtil.getNearestPath(aHttpRequest, IMG_PATH) + "t-b.gif\" class=\"am\"/>";

            			extendedTextFields.append(ActionHelper.getTextFieldHtml(field.getName(), display, img));
            		}
            	}

            	//            	else if(field.getDataTypeId()==DataType.MULTI_VALUE){
//            		
//            		if ((aRequest.myMapFieldToValues.get(field.getName()) != null) && (aRequest.myMapFieldToValues.get(field.getName()).equals("") == false)) {
//            			            			
//            			extendedUserTypeFields.append(EXTENDED_FIELD_HTML.replace("<=label>", "<%=" + field.getName() + "_label%>:").replace("<=value>", "<%=" + field.getName() + "%>"));
//            		}
//            		
//            	}

            	//
            	// Add placeholder in the extendedFields div.
            	//
            	else {
            		boolean isDisplayGroupExists = false;
            		int bufIndex = 0;
            		int displayGroup = field.getDisplayGroup();
            		
            		//Check whether display group exists
            		for (DisplayGroup tempDG : dgList){
            			if (tempDG.getId() == displayGroup)
            				isDisplayGroupExists = true;
            		}
            		
            		//To handle fields whose display group id does not match any of the existent display groups.
            		if (isDisplayGroupExists){            		
            			counter = dgCountMap.get(displayGroup);
            			bufIndex = dgMap.get(displayGroup);
            		}
            		else{
            			LOG.info("Display Group with id: " + displayGroup + " of field: " + field.getDisplayName() + " does not exists match any display group, " +
            						"hence grouping it into default display group.");
            			counter = dgCountMap.get(DEFAULT_DISPLAY_GROUP);
            			bufIndex = dgMap.get(DEFAULT_DISPLAY_GROUP);
            			displayGroup = DEFAULT_DISPLAY_GROUP;
            		}
            		
            		/* if (counter == 0) {
                        extendedFields.append("\n<tr align=\"left\" ").append("valign=\"top\">").append(EXTENDED_FIELD_HTML.replace("<=label>",
                                "<%=" + field.getName() + "_label%>:").replace("<=value>", "<%=" + field.getName() + "%>"));
	                        counter++;
	                    } else if (counter == 1) {
	                        extendedFields.append(EXTENDED_FIELD_HTML.replace("<=label>", "<%=" + field.getName() + "_label%>:").replace("<=value>", "<%=" + field.getName() + "%>"));
	                        counter++;
	                    } else if (counter == 2) {
	                        extendedFields.append(EXTENDED_FIELD_HTML.replace("<=label>", "<%=" + field.getName() + "_label%>:").replace("<=value>", "<%=" + field.getName() + "%>")).append("</tr>");
	                        counter = 0;
	                    }*/
            		
            		if (bufArray[bufIndex] == null)
            			bufArray[bufIndex] = new StringBuffer();
            		if ((counter == 0) || ((counter%3)==0)){
            			//if ((counter==0) && (displayGroup != DEFAULT_DISPLAY_GROUP)){
            			if (counter==0){
            				bufArray[bufIndex].append(DISPLAY_GROUP_HTML.replace(
            						"<=display_group_name>", 
            						DisplayGroup.lookupByDisplayGroupId(displayGroup).getDisplayName()
            						));
            			}
            			bufArray[bufIndex].append("\n<tr align=\"left\" ").append("valign=\"top\">").append(EXTENDED_FIELD_HTML.replace("<=label>",
            					"<%=" + field.getName() + "_label%>:").replace("<=value>", "<%=" + field.getName() + "%>"));
            			dgCountMap.put(displayGroup, ++counter);
            		} else if ((counter == 1) || ((counter%3)==1)){
            			bufArray[bufIndex].append(EXTENDED_FIELD_HTML.replace("<=label>", "<%=" + field.getName() + "_label%>:").replace("<=value>", "<%=" + field.getName() + "%>"));
            			dgCountMap.put(displayGroup, ++counter);
            		} else if ((counter == 2) || ((counter%3)==2)){
            			bufArray[bufIndex].append(EXTENDED_FIELD_HTML.replace("<=label>", "<%=" + field.getName() + "_label%>:").replace("<=value>", "<%=" + field.getName() + "%>")).append("</tr>");
            			dgCountMap.put(displayGroup, ++counter);
            		}                	
            	}
            }
            for (int i=0; i<dgList.size(); i++){
            	if ((bufArray[i]!= null) && (!bufArray[i].toString().trim().equals("")))
            		extendedFields.append(bufArray[i]);
            }
           
            if (extendedFields.length() > 0) 
            {
            	DTagReplacer              hp   = new DTagReplacer(EXTENDED_FIELD_HTML_FILE);
                 Hashtable<String, String> tags = new Hashtable<String, String>();
                if (aExpandHeader == true) {
                    tags.put("showHeader", "");
                } else {
                    tags.put("showHeader", "none");
                }

                if (extendedFields.toString().endsWith("</tr>") == false) {
                    if ((counter == 1) || ((counter%3)==1)) {
                        extendedFields.append(EXTENDED_FIELD_HTML.replace("<=label>", "").replace("<=value>", "")).append(EXTENDED_FIELD_HTML.replace("<=label>", "").replace("<=value>",
                                "")).append("</tr>");
                    } else if ((counter == 2) || ((counter%3)==2)) {
                        extendedFields.append(EXTENDED_FIELD_HTML.replace("<=label>", "").replace("<=value>", "")).append("</tr>");
                    }
                }               
                
                tags.put("extendedFields", extendedFields.toString());
                tags.put("extendedTextFieldsHeader", extendedTextFields.toString());

                ActionHelper.replaceTags(hp, tags, null);
                aTagTable.put("extendedFieldsHeader", hp.parse(systemId));
            }
            else
            {
            	aTagTable.put("extendedFieldsHeader", "");
            }            
        }

        return;
    }

    /**
     * This method fills form link tags based on user permissions.
     *
     * @param aPermissions   User permissions
     * @param aTagTable      Hashtable collecting <field,value> pairs
     * @param aBusinessArea  Business area object.
     * @param aRequest       Request object.
     * @param aHttpRequest TODO
     *
     */
    private void fillFormTags(Hashtable<String, Integer> aPermissions, Hashtable<String, String> aTagTable, BusinessArea aBusinessArea, Request aRequest, HttpServletRequest aHttpRequest) {
        int    permission = 0;
        String sysPrefix  = aBusinessArea.getSystemPrefix();

        //
        // Show AddRequest link if  user has ADD permission over request_id.
        //
        Integer perm = aPermissions.get(Field.REQUEST);

        if (perm != null) {
            permission = perm;
        }

        if ((permission & Permission.ADD) != 0) {
            aTagTable.put("addRequest", "<a class=\"l\" href=\"" + WebUtil.getServletPath(aHttpRequest, "/add-request/") + sysPrefix + "\" target=\"_blank\">"+captions.get("captions.view.add_request") +"</a>" + "&nbsp;&nbsp;&nbsp;");
        }

        //
        // Show AddSubRequest link if user has ADD permission
        // over parent_request_id.
        //
        perm = aPermissions.get(Field.PARENT_REQUEST_ID);

        if (perm != null) {
            permission = perm;
        }

        if ((permission & Permission.ADD) != 0) {
            aTagTable.put("addSubRequest", "<img src='" + WebUtil.getServletPath(aHttpRequest,"") +"web/images/subtasks.gif'>&nbsp;<a class=\"l cb\" href=\"" + WebUtil.getServletPath(aHttpRequest, "/add-subrequest/") + sysPrefix + "/" + aRequest.getRequestId() + "\" target=\"_blank\">"
            		+ captions.get("captions.view.add_subrequest")
            		+"</a>" + "&nbsp;&nbsp;&nbsp;");
        }

        //
        // Show office link if the user has view permission on the office id
        //
        perm = aPermissions.get(Field.OFFICE);

        if (perm != null) {
            permission = perm;
        }

        if ((permission & Permission.VIEW) == 0) {
            aTagTable.put("office_id_display", "none");
        }

        //
        // Show AddAction link if user has CHANGE permission over request_id.
        //
        perm = aPermissions.get(Field.REQUEST);

        if (perm != null) {
            permission = perm;
        }

        if ((permission & Permission.CHANGE) != 0) {
            aTagTable.put("addAction",
                          "<span id='spnUpdateRequest' " + "class=\"l cb b\" " + "onclick=\"javascript:openUpdateDiv();\" " + "target=\"_blank\">"
                          +  captions.get("captions.view.update_request")
                          +"</span>" + "&nbsp;&nbsp;&nbsp;");
        }

        return;
    }

    /**
     * Method to fill search results navigation tags
     *
     * @param aHttpRequest  HttpRequestObject
     * @param aTagTable     Table with [tag, value object]
     * @param aRequest      Request object
     * @param aBusinessArea BusienssArea object
     */
    private void fillNavigationHeader(HttpServletRequest aHttpRequest, Hashtable<String, String> aTagTable, Request aRequest, BusinessArea aBusinessArea) throws IOException {
        int                requestId   = aRequest.getRequestId();
        String             sysPrefix   = aBusinessArea.getSystemPrefix();
        String             sessionId   = (aHttpRequest.getParameter("sessionId") == null)
                                         ? ""
                                         : aHttpRequest.getParameter("sessionId");
        int                pageCounter = ((aHttpRequest.getParameter("pageCounter") == null) ||
        									(aHttpRequest.getParameter("pageCounter").length() == 0))
                                         ? 0
                                         : Integer.parseInt(aHttpRequest.getParameter("pageCounter"));
        ArrayList<Integer> resultsList = getResultsList(aHttpRequest, requestId);
        StringBuilder      sb          = new StringBuilder();
        int                index       = 0;
        String             temp        = "";

        
        String SEARCH_NAVIGATION_HTML = "<a id=\"<=Id>\" href=\"<=link>\" class=\"<=class>\" " + "onmouseover=\"return makeTrue(domTT_activate(this, event,"
        + "'content', '<=alt>', 'statusText', '<=alt>','delay', 50, " + "'closeAction', 'remove'));\">" + "<img class=\"am\" height=\"16\" " + " src=\"" 
        + WebUtil.getNearestPath(aHttpRequest, IMG_PATH) + "<=image>\" " + "width=\"16\"></a>";
        
        
        //
        // If no search results in session, or
        // request not part of results , load Independent Header File
        //
        if ((resultsList == null) || (index = resultsList.indexOf(new Integer(aRequest.getRequestId()))) == -1) {
            if (requestId < 2) {
                temp = SEARCH_NAVIGATION_HTML.replace("<=link>", "javascript:nop();").replace("<=class>", "disabled").replace("<=Id>", "onlyRequest");
            } else {
                temp = SEARCH_NAVIGATION_HTML.replace("<=link>", WebUtil.getServletPath(aHttpRequest, "/q/") + sysPrefix + "/" + (requestId - 1)).replace("<=class>", "l");
            }

            sb.append(temp.replace("<=image>", "previous-white.gif").replace("<=alt>", "Previous").replace("<=Id>", "prevLink"));

//          sb.append("<span class=\"sx b am\">&nbsp;Request&nbsp;").
//              append("#&nbsp;</span>");
            sb.append("<INPUT class=\"sx am\" id=\"requestBox\"").append(" onmouseover=\"this.T_HIDE_ONMOUSEOUT = 'true';").append(" return escape('"+ captions.get("captions.all.camel_case_request")+" Number');\" ").append(
                " type=\"text\" size=\"12\" onfocus='onFocusReqBox();'").append(" value=' [" + captions.get("captions.main.all.request_number") + "]' style='color:darkgray;' ").append(" onkeyup=").append(
                "\"if (event.keyCode == 13) callGoRequest();\">").append("&nbsp;<INPUT style=\"vertical-align:middle; ").append("font: 8pt verdana; Height: 22px\" type=\"button\" ").append(
                "value=\"GO\" onclick=\"").append("callGoRequest();\">").append("<span>&nbsp;");

            if (requestId == aBusinessArea.getMaxRequestId()) {
                temp = SEARCH_NAVIGATION_HTML.replace("<=link>", "javascript:nop();").replace("<=class>", "disabled");
            } else {
                temp = SEARCH_NAVIGATION_HTML.replace("<=link>", WebUtil.getServletPath(aHttpRequest, "/q/") + sysPrefix + "/" + (requestId + 1)).replace("<=class>", "l");
            }

            sb.append(temp.replace("<=image>", "next-white.gif").replace("<=alt>", "Next").replace("<=Id>", "nextLink"));

            // sb.append("<span class=\"sx am\">&nbsp;|&nbsp;</span>").
            // append("<span class=\"sx l cw am\">Help</span>");
        }

        //
        // else If search results in session load Search Header File
        //
        else {
            int size = resultsList.size();

            if (index == 0) {
                temp = SEARCH_NAVIGATION_HTML.replace("<=link>", "javascript:nop();").replace("<=class>", "disabled");
            } else {
                temp = SEARCH_NAVIGATION_HTML.replace("<=link>",
                        "javascript:openRequest('" + sysPrefix + "','" + resultsList.get(0) + "','" + sessionId + "','" + pageCounter + "');").replace("<=class>", "l");
            }

            sb.append(temp.replace("<=image>", "start-white.gif").replace("<=alt>", "First").replace("<=Id>", "firstLink"));

            if (index == 0) {
                temp = SEARCH_NAVIGATION_HTML.replace("<=link>", "javascript:nop();").replace("<=class>", "disabled");
            } else {
                temp = SEARCH_NAVIGATION_HTML.replace("<=link>",
                        "javascript:openRequest('" + sysPrefix + "','" + resultsList.get(index - 1) + "','" + sessionId + "','" + pageCounter + "');").replace("<=class>", "l");
            }

            sb.append(temp.replace("<=image>", "previous-white.gif").replace("<=alt>", "Previous").replace("<=Id>", "prevLink"));
            sb.append("<span class=\"sx b am\">Result ").append(index + 1 + pageCounter).append(" of ").append(size + pageCounter).append("</span>");

            if (index == size - 1) {
                temp = SEARCH_NAVIGATION_HTML.replace("<=link>", "javascript:nop();").replace("<=class>", "disabled");
            } else {
                temp = SEARCH_NAVIGATION_HTML.replace("<=link>",
                        "javascript:openRequest('" + sysPrefix + "','" + resultsList.get(index + 1) + "','" + sessionId + "','" + pageCounter + "');").replace("<=class>", "l");
            }

            sb.append(temp.replace("<=image>", "next-white.gif").replace("<=alt>", "Next").replace("<=Id>", "nextLink"));

            if (index == size - 1) {
                temp = SEARCH_NAVIGATION_HTML.replace("<=link>", "javascript:nop();").replace("<=class>", "disabled");
            } else {
                temp = SEARCH_NAVIGATION_HTML.replace("<=link>",
                        "javascript:openRequest('" + sysPrefix + "','" + resultsList.get(size - 1) + "','" + sessionId + "','" + pageCounter + "');").replace("<=class>", "l");
            }

            sb.append(temp.replace("<=image>", "end-white.gif").replace("<=alt>", "Last").replace("<=Id>", "lastLink"));

            // sb.append("<span class=\"sx am\">&nbsp;|&nbsp;</span>").
            // append("<span class=\"sx l cw am\">Help</span>");
        }

        aTagTable.put("navigationHeader", sb.toString());
    }

    /**
     * This method  fills sub,subling, related requests tags.
     *
     * @param aTagTable      Hashtable collecting <field,value> pairs
     * @param aRequest       Request object
     * @param aHttpRequest TODO
     */
    private void fillRequestAssociations(Hashtable<String, String> aTagTable, Request aRequest, HttpServletRequest aHttpRequest) {
        StringBuilder header     = new StringBuilder();
        boolean       parentFlag = !aTagTable.get("parent").equals("");
        Hashtable<String,String> siblingRequests = Request.getSiblingRequests(aRequest.getSystemId(),aRequest.getRequestId()) ;
        Hashtable<String,String> subRequests = Request.getSubRequests(aRequest.getSystemId(),aRequest.getRequestId()) ;
        Hashtable<String,String> relRequests = Request.getRelatedRequests(aRequest.getSystemId(), aRequest.getRequestId());
        int           headerSize = findHeaderSize(aHttpRequest,siblingRequests , relRequests, subRequests, header, aTagTable);

        if (headerSize < 100) {
            aTagTable.put("siblingRequests", header.toString());
            aTagTable.put("subRequests", "");
            aTagTable.put("relatedRequests", "");
            aTagTable.put("associatedRequests", "");
        } else {
            Hashtable<String, String> requestsHash = siblingRequests ;

            if (requestsHash.size() > 0) {
                getRelationsHtml(aTagTable, requestsHash, "Siblings", "siblingRequests", aTagTable.get("parent").equals("") == false, aHttpRequest);
            } else {
                aTagTable.put("siblingRequests", "");
            }

            requestsHash = subRequests ;

            if (requestsHash.size() > 0) {
                getRelationsHtml(aTagTable, requestsHash, captions.get("captions.view.subrequests"), "subRequests", !aTagTable.get("parent").equals("") ||!aTagTable.get("siblingRequests").equals(""), aHttpRequest);
            } else {
                aTagTable.put("subRequests", "");
            }

            requestsHash = relRequests;

            if (requestsHash.size() > 0) {
                getRelationsHtml(aTagTable, requestsHash, "Linked", "relatedRequests",
                                 !aTagTable.get("parent").equals("") ||!aTagTable.get("siblingRequests").equals("") ||!aTagTable.get("subRequests").equals(""), aHttpRequest);
            } else {
                aTagTable.put("relatedRequests", "");
            }
        }

        return;
    }

    /**
     * This function aligns the request history bar appropriately.
     *
     * @param aTagTable      Hashtable collecting <field,value> pairs
     *
     */
    private void fillRequestHistoryBar(Hashtable<String, String> aTagTable) {
        String relatedBeginning = "<td align='left' class='m'>";
        String relatedEnd       = "&nbsp;</td>";

        if (aTagTable.get("parent").equals("") && aTagTable.get("siblingRequests").equals("") && aTagTable.get("subRequests").equals("") && aTagTable.get("relatedRequests").equals("")
                && aTagTable.get("attachmentList").equals("")) {
            aTagTable.put("relatedBeginning", relatedBeginning);
            aTagTable.put("relatedEnd", relatedEnd);
            aTagTable.put("requestHistoryLine", "");
        } else {
            aTagTable.put("relatedBeginning", relatedBeginning);
            aTagTable.put("relatedEnd", relatedEnd);
            aTagTable.put("requestHistoryLine", "|");
        }
    }

    /**
     * Method to fill search results navigation tags
     *
     * @param aTagTable     Table with [tag, value object]
     * @param aShowHeader   user config
     */
    private void fillShowHeaderTags(Hashtable<String, String> aTagTable, boolean aShowHeader) {
        if (aShowHeader == true) {
            aTagTable.put("mode", "Collapse");
            aTagTable.put("modeFunct", "collapse");
        } else {
            aTagTable.put("mode", "Expand");
            aTagTable.put("modeFunct", "expand");
        }
    }

    /**
     * Thsi method adds Summary placeHolder if user has view permissions
     * over summary and field is non empty
     * @param aHttpRequest TODO
     * @param aTagTable      Hashtable collecting <field,value> pairs
     * @param aPermissions   User permissions
     * @param aRequest       Request object
     */
    private void fillSummaryHtmlContainer(HttpServletRequest aHttpRequest, Hashtable<String, String> aTagTable, Hashtable<String, Integer> aPermissions, Request aRequest, boolean aExpand) {

        //
        // Check if user has VIEW permission over the sumamry field,
        // if no, don't add place holder  and  return;
        //
        int     permission = 0;
        Integer perm       = aPermissions.get(Field.SUMMARY);

        if (perm != null) {
            permission = perm;
        }

        if ((permission & Permission.VIEW) == 0) {
            return;
        }

        if ((aRequest.getSummary() != null) && (aRequest.getSummary().equals("") == false)) {
            String display = ((aExpand == true)
                              ? ""
                              : "none");
            String img     = "<span class=\"l b cbk\" " + "onclick=\"javascript:showHide('" + Field.SUMMARY + "_id');\"><img src=\"" + WebUtil.getNearestPath(aHttpRequest, IMG_PATH) + "t-r.gif\" class=\"am\">&nbsp;";

            aTagTable.put("summaryHeader",
                          "<a name='summary'></a>"
                          + ActionHelper.getTextFieldHtml(TEXT_CLASS, TEXT_HEADER_CLASS, TEXT_CONTENT_CLASS, "", "", Field.SUMMARY + "_id", display,
                              img + "<%=" + Field.SUMMARY + "_label%> " + "</span>", "<%=" + Field.SUMMARY + "%>", "<%=" + Field.SUMMARY + "_updated_by%>", "<%=" + Field.SUMMARY + "_updated_time%>",
                              "", "", "", "", "<%=diff%>"));
            aTagTable.put("actualSummary",
                          ActionHelper.getTextFieldHtml(TEXT_CLASS, TEXT_HEADER_CLASS, TEXT_CONTENT_CLASS, "", "", Field.SUMMARY + "_id", "", img + "<%=" + Field.SUMMARY + "_label%>" + "</span>",
                              "<%=" + Field.SUMMARY + "%>", "<%=" + Field.SUMMARY + "_updated_by%>", "<%=" + Field.SUMMARY + "_updated_time%>", "", "", "", "", "<%=diff%>"));
        }
    }

    private void fillUpdateRequestForm(HttpServletRequest aRequest, HttpServletResponse aResponse, BusinessArea aBA, Request oldRequest, User aUser, int aClientOffset, TimeZone aPrefZone, Hashtable<String, String> aTagTable,
                                       Hashtable<String, Integer> aPermTable)
            throws Exception {
        String strUpdate = aRequest.getParameter("u");

        if (strUpdate != null) {
            aTagTable.put("updateDivDisplay", "BLOCK");
        } else {
            aTagTable.put("updateDivDisplay", "NONE");
        }

        if (aTagTable.get("isTransferred").equals("true") && aTagTable.get("isLocked").equals("true")) {
            aTagTable.put("updateDivContent", "<span class=\"cr sx\">" + "The request is under transfer." + " Please try updating later.</span>");
            return;
        }

        DTagReplacer              hp          = new DTagReplacer(UPDATE_FORM);
        Hashtable<String, Object> updTagTable = new Hashtable<String, Object>();

        updTagTable.put("clientOffset", aClientOffset);

        AddHtmlRequest            ahr        = new AddHtmlRequest();
        Hashtable<String, Object> aParamInfo = WebUtil.getRequestParams(aRequest, aUser.getWebConfigObject(), WebUtil.ADD_ACTION);

        //
        // If request is transfferred completely, allow only metadata change
        // of category,assignee,request_type only on update,
        // that too without notification.
        //
        if (aTagTable.get("isTransferred").equals("true") && aTagTable.get("isLocked").equals("false")) {
            Hashtable<String, Integer> newPermTable = new Hashtable<String, Integer>();
            Integer                    baPerm       = aPermTable.get(Field.BUSINESS_AREA);
            Integer                    reqPerm      = aPermTable.get(Field.REQUEST);
            Integer                    catPerm      = aPermTable.get(Field.CATEGORY);
            Integer                    assPerm      = aPermTable.get(Field.ASSIGNEE);
            Integer                    reqTypePerm  = aPermTable.get(Field.REQUEST_TYPE);

            if (baPerm != null) {
                newPermTable.put(Field.BUSINESS_AREA, baPerm);
            }

            if (reqPerm != null) {
                newPermTable.put(Field.REQUEST, reqPerm);
            }

            if (catPerm != null) {
                newPermTable.put(Field.CATEGORY, catPerm);
            }

            if (assPerm != null) {
                newPermTable.put(Field.ASSIGNEE, assPerm);
            }

            if (reqTypePerm != null) {
                newPermTable.put(Field.REQUEST_TYPE, reqTypePerm);
            }

            Hashtable<String, String> paramTable = new Hashtable<String, String>();

            ahr.addActionForm(aRequest, updTagTable, aParamInfo, newPermTable, aBA, aUser, paramTable);
            updTagTable.put("notify", "");
            updTagTable.put("sendMail", "false");
            updTagTable.put("notify_loggers", "");
            updTagTable.put("mailLoggers", "false");
        } else {
            Hashtable<String, String> paramTable = new Hashtable<String, String>();            
            ahr.addActionForm(aRequest, updTagTable, aParamInfo, aPermTable, aBA, aUser, paramTable);            
        }
        // call the ExtUIFactory for appropriate plugins for action details i.e. view_request 
        SlotFillerFactory euf = SlotFillerFactory.getInstance() ;
        euf.runUpdateRequestSlotFillers(aRequest, aResponse, aBA, oldRequest, aUser, updTagTable) ;//(aRequest,null, updTagTable,ourUpdateTagList,aBA, ISlotFiller.UPDATE_REQUEST) ;
        
        //
        // If request is transferred
        //
        updTagTable.put("nearestPath", WebUtil.getNearestPath(aRequest, ""));
        replaceUpdateFormTags(hp, updTagTable);
        aTagTable.put("updateDivContent", hp.parse(aBA.getSystemId()));

        Object temp       = updTagTable.get("userDraftsInfo");
        String draftsInfo = (temp == null)
                            ? ""
                            : temp.toString();

        aTagTable.put("userDraftsInfo", draftsInfo);
    }

    /**
     * This method returns the size of the request associations header.
     * @param aRequest TODO
     * @param aSiblingRequests  Hashtable containing Sibling request id,subject
     *                           pairs
     * @param aRelatedRequests  Hashtable containing Related request id,subject
     *                           pairs
     * @param aSubRequests      Hashtable containing  subrequest id,subject
     *                          pairs
     * @param actualHeader      StringBuilder containing the complete header.
     * @param aTagTable         HashTable containing the tag,value pairs.
     */
    private int findHeaderSize(HttpServletRequest aRequest, Hashtable<String, String> aSiblingRequests, Hashtable<String, String> aRelatedRequests, Hashtable<String, String> aSubRequests,
                               StringBuilder actualHeader, Hashtable<String, String> aTagTable) {

        // This buffer contains the text that would appear to the user.
        StringBuilder sb = new StringBuilder();

        // Number of subrequests.
        int count1 = 0;

        // Number of Sibling Requests.
        int count2 = 0;

        // Number of Related Requests.
        int                 count3 = 0;
        Enumeration<String> e      = null;
        boolean             parent = !aTagTable.get("parent").equals("");

        if (aSiblingRequests != null) {
            e = aSiblingRequests.keys();

            while (e.hasMoreElements()) {
                if (count1 == 0) {
                    if (parent == true) {
                        sb.append("| Siblings: ");
                        actualHeader.append("&nbsp;<span class='b'>|</span>&nbsp;").append("&nbsp;<span class=\"cw b crhb\">").append("Siblings: </span>");
                    } else {
                        sb.append("Siblings: ");
                        actualHeader.append("<span class=\"cw b crhb\">").append("Siblings: </span>");
                    }
                }

                count1++;

                String requestID = e.nextElement();

                sb.append(requestID.substring(requestID.indexOf("#") + 1));
                actualHeader.append("<a class=\"l cw b\" href=\"").append(WebUtil.getServletPath(aRequest, "q/")).append(requestID.replace("#", "/")).append("\"").append(
                    " onmouseover=\"this.T_HIDE_ONMOUSEOUT='true';").append("return escape('").append(ActionHelper.untaintForHtml(aSiblingRequests.get(requestID))).append("')\">").append(
                    requestID.substring(requestID.indexOf("#") + 1));

                if ((count1++ > 0) && e.hasMoreElements()) {
                    actualHeader.append(",");
                }

                actualHeader.append("</a>&nbsp;");
            }
        }

        if (aSubRequests != null) {
            e = aSubRequests.keys();

            while (e.hasMoreElements()) {
                if (count2 == 0) {
                    if ((count1 > 0) || (parent == true)) {
                        sb.append("| "+captions.get("captions.view.subrequests")+": ");
                        actualHeader.append("&nbsp;<span class='b'>|</span>&nbsp;").append("&nbsp;<span class=\"cw b crhb\">").append(captions.get("captions.view.subrequests")+": </span>");
                    } else {
                        sb.append(captions.get("captions.view.add_subrequests") + " : ");
                        actualHeader.append("<span class=\"cw b crhb\">" + captions.get("captions.view.subrequests") + ": ").append("</span>");
                    }
                }

                count2++;

                String requestID = e.nextElement();

                sb.append(requestID.substring(requestID.indexOf("#") + 1));
                actualHeader.append("<a class=\"l cw b\" href=\"").append(WebUtil.getServletPath(aRequest, "q/")).append(requestID.replace("#", "/")).append("\"").append(
                    " onmouseover=\"this.T_HIDE_ONMOUSEOUT='true';").append("return escape('").append(ActionHelper.untaintForHtml(aSubRequests.get(requestID))).append("')\">").append(
                    requestID.substring(requestID.indexOf("#") + 1));

                if ((count2++ > 0) && e.hasMoreElements()) {
                    actualHeader.append(",");
                }

                actualHeader.append("</a>&nbsp;");
            }
        }

        if (aRelatedRequests != null) {
            e = aRelatedRequests.keys();

            while (e.hasMoreElements()) {
                if (count3 == 0) {
                    if ((count1 > 0) || (count2 > 0) || (parent == true)) {
                        sb.append("| Linked: ");
                        actualHeader.append("&nbsp;<span class='b'>|</span>&nbsp;").append("&nbsp;<span class=\"cw b crhb\">").append("Linked: </span>");
                    } else {
                        sb.append("Linked: ");
                        actualHeader.append("<span class='cw b crhb'>").append("Linked: </span>");
                    }
                }

                count3++;

                String requestID = e.nextElement();

                sb.append(requestID + " ");
                actualHeader.append("<a class=\"l b cw\" href=\"").append(WebUtil.getServletPath(aRequest, "q/")).append(requestID.replace("#", "/")).append(
                    "\" onmouseover=\"this.T_HIDE_ONMOUSEOUT='true';").append("return escape('").append(ActionHelper.untaintForHtml(aRelatedRequests.get(requestID))).append("')\">").append(requestID);

                if ((count3++ > 0) && e.hasMoreElements()) {
                    actualHeader.append(",");
                }

                actualHeader.append("</a>&nbsp;");
            }
        }

        return sb.toString().length();
    }

    /**
     * Method that actually handles the Get Request.
     *
     * @param  aHttpRequest          the HttpServlet Request Object
     * @param  aHttpResponse         the HttpServlet Response Object
     * @throws ServeletException
     * @throws IOException
     * @throws Exception
     */
    private void handleGetRequest(HttpServletRequest aHttpRequest, HttpServletResponse aHttpResponse) throws ServletException, IOException, Exception {
        long startTime = 0;
        long endTime   = 0;

        aHttpResponse.setContentType("text/html; charset=UTF-8");
        aHttpResponse.setCharacterEncoding("UTF-8");

        PrintWriter out         = aHttpResponse.getWriter();
        HttpSession httpSession = aHttpRequest.getSession();

        startTime = System.currentTimeMillis();

        //
        // Validate the user, get the user Object, Read the configuration.
        //
        User      user       = WebUtil.validateUser(aHttpRequest);
        int       userId     = user.getUserId();
        WebConfig userConfig = user.getWebConfigObject();

//        endTime = System.currentTimeMillis();
//        LOG.debug("Time taken to validate user: " + (endTime - startTime));
        startTime = System.currentTimeMillis();

        //
        // Call the WebUtil\"s getParamInfo, which reads the pathInfo and
        // get the corresponding BA and request object. Throws exception
        // if either Ba or requestId is invalid.
        //
        Hashtable paramInfo = WebUtil.getRequestParams(aHttpRequest, userConfig, WebUtil.VIEW_REQUEST);

//        endTime = System.currentTimeMillis();
//        LOG.debug("Time taken to get param info: " + (endTime - startTime));

        //
        // Get Business Area object
        //
        BusinessArea businessArea = (BusinessArea) paramInfo.get(Field.BUSINESS_AREA);
        int          systemId     = businessArea.getSystemId();
        String       sysPrefix    = businessArea.getSystemPrefix();
        SysConfig    sysConfig    = businessArea.getSysConfigObject();

        //
        // Get Request Object
        //
        Request request   = (Request) paramInfo.get(Field.REQUEST);
        request.setContext(aHttpRequest.getContextPath());
        int     requestId = request.getRequestId();
        String  pathInfo  = aHttpRequest.getPathInfo();

        String redirectOldViewRequestStr = PropertiesHandler.getProperty("transbit.tbits.redirect.oldviewrequest");
        boolean redirectOldViewRequest = false;
        if(redirectOldViewRequestStr != null)
        {
        	try
        	{
        			
        		redirectOldViewRequest = Boolean.parseBoolean(redirectOldViewRequestStr);
        	}
        	catch(Throwable t)
        	{
        		System.out.println("Invalid value for redirect.oldviewrequest");
        		t.printStackTrace();
        	}
        }
        if(redirectOldViewRequest)
        {
        	String url = WebUtil.getNearestPath(aHttpRequest, "/") + "jaguar/#ba="+sysPrefix;
        	String strUpdate = aHttpRequest.getParameter("u");
        	
    		if((strUpdate != null) && strUpdate.trim().equals("1"))
    			url = url + "&update=" + requestId;
    		else
    			url = url +  "&view=" + requestId;
    		
        	aHttpResponse.sendRedirect(url);
        	return;
        }
        captions = CaptionsProps.getInstance().getCaptionsHashMap(systemId);
        if ((pathInfo != null) && pathInfo.trim().endsWith("/rss")) {

            /*
             * Ideally the content-type should be application/rss+xml. But
             * IE and Firefox does not recognize this. So let us set this to
             * text/xml for now.
             */

            // aHttpResponse.setContentType("application/rss+xml");
            aHttpResponse.setContentType("text/xml");

            // Generate an RSS feed and return.
            out.println(rssSerialize(businessArea, request, user, aHttpRequest));

            return;
        }

        //
        // Get the client offset.
        //
        int clientOffset = WebUtil.getClientOffset(aHttpRequest, aHttpResponse);

        if (clientOffset == -1) {

            //
            // Redirect to the browser to get the clientOffset as a request
            // parameter.
            //
            String url =  WebUtil.getServletPath(aHttpRequest, "/Q") + aHttpRequest.getPathInfo();

            out.println(WebUtil.getRedirectionHtml(aHttpRequest, url));

            return;
        }

        LOG.info("User: " + user.getUserLogin() + " Request: " + businessArea.getSystemPrefix() + "#" + requestId);

        //
        // Get the TimeZone based on the user's preferred zone. Also pass the
        // clientoffset which will be used incase the user opted for Browser
        // time
        //
        int      zone            = userConfig.getPreferredZone();
        TimeZone preferredZone   = WebUtil.getPreferredZone(zone, clientOffset);
        String   preferredFormat = userConfig.getWebDateFormat();

        //
        // Tag Table contains all the [tag_name, value] pairs. Finally we call
        // the replaceTags method that checks for each of the tag present in
        // the static tagList ArrayList in this tagTable for a value. If not
        // found such a tag is replaced with empty string. If found, such a
        // tag is replaced with the value found.
        //
        Hashtable<String, String> tagTable = new Hashtable<String, String>();

        //
        // Get User Permissions for this Request
        //
        Hashtable<String, Integer> permissions = RolePermission.getPermissionsBySystemIdAndRequestIdAndUserId(systemId, requestId, user.getUserId());

//        endTime = System.currentTimeMillis();
//        LOG.debug("Time taken to get perm table: " + (endTime - startTime));

        DTagReplacer hp = renderRequestDetails(aHttpRequest, aHttpResponse, businessArea, tagTable, permissions, request, user, clientOffset);

        if( ( permissions.get(Field.REQUEST) & Permission.CHANGE ) != 0 )
        {
        	fillUpdateRequestForm(aHttpRequest, aHttpResponse, businessArea, request, user, clientOffset, preferredZone, tagTable, permissions);
        }
        else
        {
        	
        }
     // call the SlotFillerFactory for appropriate plugins for VIEW_REQUEST
        SlotFillerFactory euf = SlotFillerFactory.getInstance() ;
        euf.runActionDetailsSlotFillers(aHttpRequest, aHttpResponse, businessArea, request, user, tagTable) ;
        
        ActionHelper.replaceTags(hp, tagTable, TAG_LIST);        
        String output = hp.parse(systemId);

        out.println(output);

        //
        // Before quitting  register the latest action user read
        // for the request
        //
        int    maxActionId        = request.getMaxActionId();
        String strMaxReadActionId = tagTable.get("userReadAction");
        int    maxReadActionId    = 0;

        try {
            maxReadActionId = Integer.parseInt(strMaxReadActionId);
        } catch (Exception e) {
            maxReadActionId = 0;
        }

        if (maxReadActionId != maxActionId) {
            UserReadAction.registerUserReadAction(systemId, requestId, maxActionId, userId);
        }

        endTime = System.currentTimeMillis();
        LOG.debug("Time taken to render: " + (endTime - startTime));

        return;
    }

    /**
     * This method loads the TextField data from action object, if
     * text field not empty and returns false when loaded.
     *
     * @param aTagTable Table with [tag, value ]
     * @param aFieldName the field name.
     * @param aAction    the action object.
     *
     * @exception DatabaseException
     */
    private boolean loadTextFieldsData(Hashtable<String, String> aTagTable, String aFieldName, Action aAction, String aDateFormat, TimeZone aZone) throws DatabaseException {
        String text = "";

        if (aFieldName.equals(Field.SUMMARY)) {
            text = aAction.getSummary();
        }

        if ((text != null) && (text.equals("") == false)) {
            aTagTable.put(aFieldName + "_updated_by",
                          "[ updated by " + ActionHelper.getUserDisplayName(aAction.getUserId()) + " in <a class=\"l cb\" href=\"#" + aAction.getActionId() + "\">Update#" + aAction.getActionId()
                          + "</a> ]");
            aTagTable.put(aFieldName + "_updated_time", WebUtil.getDateInFormat(aAction.getLastUpdatedDate(), aZone, aDateFormat));

            return false;
        }

        return true;
    }

    public DTagReplacer renderRequestDetails(HttpServletRequest aHttpRequest, HttpServletResponse aHttpResponse, BusinessArea businessArea, Hashtable<String, String> tagTable,
            Hashtable<String, Integer> permissions, Request request, User user, int clientOffset)
            throws ServletException, IOException, TBitsException, Exception {
        long startTime = 0;
        long endTime   = 0;

        // Get the user details.
        int       userId     = user.getUserId();
        WebConfig userConfig = user.getWebConfigObject();

        endTime = System.currentTimeMillis();

        // Read the BA details.
        int       systemId  = businessArea.getSystemId();
        String    sysPrefix = businessArea.getSystemPrefix();
        SysConfig sysConfig = businessArea.getSysConfigObject();

        // Read the request details.
        int      requestId       = request.getRequestId();
        int      zone            = userConfig.getPreferredZone();
        TimeZone preferredZone   = WebUtil.getPreferredZone(zone, clientOffset);
        String   preferredFormat = userConfig.getWebDateFormat();

        startTime = System.currentTimeMillis();

        //
        // Check General View and Private Security access
        // Fill private Symbol if request private.
        //
        checkAccessPermissions(aHttpRequest, user, permissions, tagTable, businessArea, request);

        //
        // Get Client Offset
        //
        // Put the requestId into the tag table.
        tagTable.put("requestId", Integer.toString(requestId));
        tagTable.put("adminLink", isAdmin(aHttpRequest, permissions));
        tagTable.put("nearestPath", WebUtil.getNearestPath(aHttpRequest, ""));
        tagTable.put("singleIEWindow", Boolean.toString(userConfig.getSingleIEWindow()));
        tagTable.put("cssFile", WebUtil.getCSSFile(sysConfig.getWebStylesheet(), sysPrefix, false));
		tagTable.put("userLogin", user.getUserLogin());
        startTime = System.currentTimeMillis();
		
		String display_logout = "none";
        if((aHttpRequest != null) && (aHttpRequest.getAuthType() == AuthConstants.AUTH_TYPE))
        	display_logout = "";
		tagTable.put("display_logout", display_logout);

        //
        // Load Html Template
        //
        DTagReplacer hp = new DTagReplacer(HTML_FILE);
        String jss = ActionHelper.getResourceString(myActionYUIJSs, WebUtil.getNearestPath(aHttpRequest, "" )) ;
   //     LOG.info("YUIjss ="+jss) ;
        String jss1 = ActionHelper.getResourceString(myActionTbitsJSs, WebUtil.getNearestPath(aHttpRequest, "" )) ;
    //    LOG.info("tbitsjss ="+jss1) ;
        tagTable.put("YUIJSs", jss) ;
        tagTable.put("tbitsJSs", jss1) ;
        //
        // Fill CustomLinks, if any
        //
        ActionHelper.fillCustomLinks(tagTable, businessArea);

        //
        // Fill Navigation header
        //
        fillNavigationHeader(aHttpRequest, tagTable, request, businessArea);

        //
        // Fill Sub_nvaigation(forms) header based on permissions
        //
        fillFormTags(permissions, tagTable, businessArea, request, aHttpRequest);

        //
        // Fill parent header html Container.
        // Values/labels will be filled in fillHeaderData()
        //
        ActionHelper.fillParentHeaderContainer(tagTable, request, "");

        //
        // Fill ExtendedFieldsHeader html Conatiner, if user has view
        // permissions over any extended field.
        // Values/labels will be filled in fillHeaderData()
        //
        fillExtendedHeaderHtmlContainer(aHttpRequest, tagTable, permissions, request, userConfig.getActionHeader());

        //
        // Fill Summary Header html Container, if user has view permisisons
        // over summary field.
        // Values/labels will be filled in fillHeaderData()
        //
        fillSummaryHtmlContainer(aHttpRequest, tagTable, permissions, request, userConfig.getActionHeader());

        try {
            updateTransferLinks(businessArea, request, tagTable, permissions);
        } catch (Exception e) {
            LOG.warn("",(e));
            tagTable.put("transferDisplay", "NONE");
        }

        //
        // Replace all Html conatiner Tags , so the template is now complete,
        // with <%=field_label%> and <%=field_value%> tags.
        // These tags will be filled/replaced after this.
        //

        /*
         *  ActionHelper.replaceTags(hp, tagTable, null);
         * hp.setHtmlFileContents(hp.parse(systemId));
         * hp.setHash(new HashMap<String,String>());
         */

        //
        // Get search text Strings to be highlighted, if any
        //
        ArrayList<String> searchTextList = getSearchTextList(aHttpRequest);

        //
        // Now Fetch all actions from DB and
        // 1) Fill Action Details.
        // 2) Put summary, updated By, and updated time in tagTable.
        // 3) Put memo, updated By, and updated time in tagTable.
        // 4) return Attachments List.
        //
        Hashtable<String, String> attachmentsList = fillActionDetails(aHttpRequest, tagTable, permissions, user, businessArea, request, (userConfig.getActionOrder() == 0)
				        ? "asc"
				        : "desc", preferredFormat, preferredZone, searchTextList, userConfig.getActionHeader());

        JsonObject rootNode = ActionHelper.getAttachmentDetailsJSONInternal(permissions, businessArea, request, user);
		tagTable.put(REQUEST_UPDATE_FILES, rootNode.toString());
		
        //ActionHelper.fillAttachmentDetailsJSON(tagTable, permissions, businessArea, request, user);
        //
        // Replace all Html conatiner Tags , so the template is now complete,
        // with <%=field_label%> and <%=field_value%> tags.
        // These tags will be filled/replaced after this.
        //
        ActionHelper.replaceTags(hp, tagTable, null);
        hp.setHtmlFileContents(hp.parse(systemId));
        hp.setHash(new HashMap<String, String>());

        //
        // Fill fixed and extended fields Header with values
        // by replacing <%=field_label+> and <%=field_value%> tags.
        //
        ActionHelper.fillHeaderData(tagTable, request, WebUtil.getServletPath(aHttpRequest, ""), request.getMaxActionId(), preferredFormat, preferredZone, searchTextList, systemId, SOURCE_WEB, DYNAMIC_TOOLTIP);

        //
        // Fill Sub ,Sibling, Related Requests tooltips
        //
        fillRequestAssociations(tagTable, request, aHttpRequest);

        //
        // Fill Attachments tooltip
        //
        fillAttachmentsToolTip(aHttpRequest, tagTable, attachmentsList);

//        Collection<AttachmentFieldInfo> attachmentFields = new ArrayList<AttachmentFieldInfo>();
//        attachmentFields.add(new AttachmentFieldInfo("attachments", request.getAttachments()));
//        
//        for(Field f: request.getExtendedFields().keySet())
//        {
//        	if(f.getDataTypeId() == DataType.ATTACHMENTS)
//        	{
//        		RequestEx rex = request.getExtendedFields().get(f);
//        		attachmentFields.add(new AttachmentFieldInfo(f.getName(), AttachmentInfo.fromJson(rex.getTextValue())));
//        	}
//        }
        //fill attachments -using the JSON format
//        tagTable.put("requestFiles", AttachmentFieldInfo.toJson(attachmentFields));
        
        //
        // Align the request history bar.
        //
        fillRequestHistoryBar(tagTable);

        //
        // Fill Subject , Severity and over Due symbol.
        //
        ActionHelper.fillSymbols(tagTable, WebUtil.getNearestPath(aHttpRequest, IMG_PATH), request, false);

        //
        // Show/Expand header based on user config
        //
        fillShowHeaderTags(tagTable, userConfig.getActionHeader());

        //
        // Put other information in Tag Table
        //
        String subject = businessArea.getSystemPrefix() + "#" + request.getRequestId() + ": " + Utilities.htmlEncode(request.getSubject());

        tagTable.put("title", subject);
        subject = ActionHelper.highLightText(subject, searchTextList);

        if (request.getSeverityId().getName().equalsIgnoreCase("critical")) {
            subject = "<span class=\"cr\">" + businessArea.getSystemPrefix() + "#" + request.getRequestId() + "</span>: " + request.getSubject();
        }

        tagTable.put("subjectLine", subject);
        tagTable.put("sysPrefix", sysPrefix);
        tagTable.put("sysName", businessArea.getDisplayName());
        tagTable.put("requestId", Integer.toString(requestId));
        tagTable.put("path", PATH);
        WebUtil.setInstanceBold(tagTable, businessArea.getSystemPrefix());

        return hp;
    }

    /**
     * This method replaces all the tags with their corresponding values
     * reading them from the given table in the given HTML Parser object.
     *
     * @param aTagTable Table with [tag, value object]
     */
    public void replaceUpdateFormTags(DTagReplacer aReplacer, Hashtable<String, Object> aTagTable) {
        if (aTagTable == null) {
            return;
        }

        for (String key : ourUpdateTagList) {
            Object obj   = aTagTable.get(key);
            String value = "";

            if (obj != null) {
                value = obj.toString();
            }

            aReplacer.replace(key, value);
        }
    }

    /**
     * This method generates an RSS feed for this request.
     *
     * @param ba
     * @param request
     * @param user
     * @param aRequest TODO
     * @return
     */
    private String rssSerialize(BusinessArea ba, Request request, User user, HttpServletRequest aRequest) throws DatabaseException {
        StringBuffer rss         = new StringBuffer();
        String       sysPrefix   = ba.getSystemPrefix();
        int          systemId    = ba.getSystemId();
        int          requestId   = request.getRequestId();
        int          maxActionId = request.getMaxActionId();
        int          userId      = user.getUserId();

        // Get the permissions table.
        Hashtable<String, Integer> permissions = RolePermission.getPermissionsBySystemIdAndRequestIdAndUserId(systemId, requestId, userId);
        ArrayList<Action>          actionList  = Action.getAllActions(systemId, requestId, "desc");

        /*
         * Following are the values for sub-elements in <channel> element:
         *
         * <title>: Display name of the business area.
         * <link>:  URL to access the request.
         * <description>: Latest subject of the request.
         * <pubDate>: Current time in GMT.
         * <lastBuildDate>: Last updated date in GMT.
         * <category>: Let us set this to the category of the request. Later
         *             this can be changed if Ritesh has other plans.//TODO:
         */
        String       titleInChannel         = ba.getDisplayName();// + " Request System.";
        String       linkInChannel          = WebUtil.getServletPath(aRequest, "/Q/" + sysPrefix + "/" + requestId);
        String       descInChannel          = Utilities.htmlEncode(request.getSubject());
        String       pubDateInChannel       = Timestamp.getGMTNow().toCustomFormat(RSS_DATE_FORMAT) + " GMT";
        String       lastBuildDateInChannel = Timestamp.toCustomFormat(request.getLastUpdatedDate(),RSS_DATE_FORMAT) + " GMT";
        String       categoryInChannel      = request.getCategoryId().getDisplayName();
        StringBuffer items                  = new StringBuffer();

        for (Action action : actionList) {
            User actionuser = null;

            try {
                actionuser = User.lookupAllByUserId(user.getUserId());
            } catch (DatabaseException de) {
                LOG.info(de.toString());
            }

            if (actionuser == null) {
                continue;
            }

            Type category = null;

            try {
                int categoryId = action.getCategoryId();

                category = Type.lookupBySystemIdAndFieldNameAndTypeId(systemId, Field.CATEGORY, categoryId);
            } catch (DatabaseException de) {
                LOG.info(de.toString());
            }

            if (category == null) {
                continue;
            }

            /*
             * Following are the values for sub-elements in <channel> element:
             *
             * <title>: Subject of this action.
             * <link>:  URL to access this action.
             * <description>: description of this action.
             * <author>: Name of the appender.
             * <category>: Let us set this to the category of the request. Later
             *             this can be changed if Ritesh has other plans.//TODO:
             * <pubDate>: Time when the append is made.
             * <comments>: Header description as text.
             *
             */
            String titleInItem    = Utilities.htmlEncode(action.getSubject());
            String linkInItem     = linkInChannel + "#" + action.getActionId();
            String descInItem     = Utilities.htmlEncode(action.getDescription());
            String authorInItem   = actionuser.getDisplayName() + " (" + actionuser.getUserLogin() + ")";
            String categoryInItem = category.getDisplayName();
            String pubDateInItem  = action.getLastUpdatedDate().toCustomFormat(RSS_DATE_FORMAT) + " GMT";
            String commentsInItem = Utilities.htmlEncode(ActionHelper.formatActionLog(systemId, action.getHeaderDescription(), permissions, TBitsConstants.TEXT_FORMAT, false, sysPrefix,
                                        WebUtil.getNearestPath(aRequest, ""), requestId, action.getActionId(), maxActionId, SOURCE_CMDLINE, NO_TOOLTIP));

            items.append("\n        <item>").append("\n            <title>").append(titleInItem).append("</title>").append("\n            <link>").append(linkInItem).append("</link>").append(
                "\n            <description>").append(descInItem).append("</description>").append("\n            <pubDate>").append(pubDateInItem).append("</pubDate>").append(
                "\n            <category>").append(categoryInItem).append("</category>").append("\n            <guid>").append(linkInItem).append("</guid>")

//          .append("\n        <comments>").append(commentsInItem)
//          .append("</comments>")
            .append("\n        </item>");
        }

        rss.append("\n<?xml version=\"1.0\" encoding=\"UTF-8\"?>").append("\n<rss version=\"2.0\">").append("\n    <channel>").append("\n        <title>").append(titleInChannel).append(
            "</title>").append("\n        <link>").append(linkInChannel).append("</link>").append("\n        <description>").append(descInChannel).append("</description>").append(
            "\n        <pubDate>").append(pubDateInChannel).append("</pubDate>").append("\n        <lastBuildDate>").append(lastBuildDateInChannel).append("</lastBuildDate>").append(
            "\n        <category>").append(categoryInChannel).append("</category>").append(items.toString()).append("\n    </channel>").append("\n</rss>");

        return rss.toString().trim();
    }

    private void updateTransferLinks(BusinessArea aBA, Request aRequest, Hashtable<String, String> aTagTable, Hashtable<String, Integer> aPermTable) throws Exception {
        String sysPrefix = aBA.getSystemPrefix();
        int    requestId = aRequest.getRequestId();

        /*
         * Check if the request is already transferred.
         */
        TransferredRequest tr = TransferredRequest.lookupBySourcePrefixAndRequestId(sysPrefix, requestId);

        if (tr != null) {
            String targetPrefix    = tr.getTargetPrefix();
            int    targetRequestId = tr.getTargetRequestId();

            if (targetRequestId == -1) {
                aTagTable.put("transferInfo", "<span class=\"b\" >Transfer to " + targetPrefix + "# pending</span>" + "&nbsp;&nbsp;&nbsp;");
            } else {
                aTagTable.put("transferInfo",
                              "<span><a class=\"l cal b\" " + "href=\"" + WebUtil.getServletPath(aRequest.getContext(), "/q/") + targetPrefix + "/" + targetRequestId + "\">Transferred to " + targetPrefix + "#" + targetRequestId
                              + "</a></span>&nbsp;&nbsp;&nbsp;");
            }

            aTagTable.put("isTransferred", "true");
            aTagTable.put("transferDisplay", "NONE");
            aTagTable.put("addSubRequest", "");

            if (TBitsHelper.isRequestLocked(tr) == true) {
                aTagTable.put("isLocked", "true");
            } else {
                aTagTable.put("isLocked", "false");
            }

            return;
        } else {
            aTagTable.put("isTransferred", "false");
        }

        /*
         * Check if the user is in role analyst table.
         */
        if (aPermTable.get("__ROLE_ANALYST__") == null) {

            // Disable transfer link and return.
            aTagTable.put("transferDisplay", "NONE");

            return;
        }
    }

    //~--- get methods --------------------------------------------------------

    private void getRelationsHtml(Hashtable<String, String> aTagTable, Hashtable<String, String> aRequestHash, String aDisplayText, String aTag, boolean aAddDelim, HttpServletRequest aHttpRequest) {
        String           req        = "";
        String           sub        = "";
        StringBuilder    sb         = new StringBuilder();
        PrefixComparator prefixComp = new PrefixComparator();

        if (aDisplayText.equals("Attachments")) {

            // set sort order descending
            prefixComp.setSortOrder(1);
        } else {

            // set sort order ascending
            prefixComp.setSortOrder(0);
        }

        Map<String, String> m = Collections.synchronizedMap(new TreeMap<String, String>(prefixComp));

        m.putAll(aRequestHash);

        Iterator<String> e      = m.keySet().iterator();
        StringBuilder    buffer = new StringBuilder();
        int              cnt    = 0;

        if (aAddDelim == true) {
            sb.append("&nbsp;&nbsp;<span class='b'>|</span>&nbsp;&nbsp;");
        }

        while (e.hasNext() &&!aDisplayText.equals("Attachments")) {
            req = e.next();
            sub = aRequestHash.get(req);
            buffer.append("<a class=\"cw b\" href=\"");

            if (req.startsWith("Update#")) {
                buffer.append(req.replace("Update", ""));
            } else {
                buffer.append(WebUtil.getServletPath(aHttpRequest, "/q/")).append(req.replace("#", "/")).append("\" target='").append(req.replace("#", "_"));
            }

            buffer.append("' onmouseover=\"this.T_HIDE_ONMOUSEOUT='true';").append("return escape('").append(ActionHelper.untaintForHtml(aRequestHash.get(req))).append("')\">");

            if (aTag.equalsIgnoreCase("relatedRequests")) {
                buffer.append(req);
            } else {
                buffer.append(req.substring(req.indexOf("#") + 1));
            }

            buffer.append("</a>");
            cnt++;

            if ((cnt == 1) && e.hasNext()) {
                buffer = buffer.append(",&nbsp;");
            }

            if (cnt == 2) {
                break;
            }
        }

        int i = 1;

        req = "";
        e   = m.keySet().iterator();

        if (m.size() > 0) {
            if (!aDisplayText.equalsIgnoreCase("Attachments")) {
                sb.append("<span class=\" b cw crhb\" >").append(aDisplayText);

                if (m.size() > 2) {
                    sb.append("&nbsp;(").append(m.size()).append(")");
                }

                sb.append(":&nbsp;").append(buffer.toString());
            }

            if ((m.size() > 2) || aDisplayText.equalsIgnoreCase("Attachments")) {
                sb.append("<span class=\"l b cw\" onclick=").append("\"return makeTrue(domTT_activate(this, event,").append("'caption','").append(aDisplayText).append("','content',").append(
                    "document.getElementById('").append(aTag).append("').innerHTML").append(", 'type','velcro','statusText','").append(aDisplayText).append("'));\">");

                if (!aDisplayText.equalsIgnoreCase("Attachments")) {
                    sb.append(",&nbsp;more...");
                } else {
                    sb.append("Attachments ").append("(").append(m.size()).append(")");
                }

                sb.append("\n</span>").append("\n<div id='").append(aTag).append("' style='clean:none;display:none;'>").append("\n<table class='att' cellpadding='0' ").append(
                    "cellspacing='0' width='100%'>").append("\n<tbody>");

                while (e.hasNext()) {
                    req = e.next();
                    sub = aRequestHash.get(req);
                    sb.append("\n<tr class='sx' valign='top'>").append("\n<td nowrap>").append("\n<a class='l cb' ");

                    if (req.startsWith("Update#")) {
                        req = req.substring(0, req.indexOf("/"));
                        sb.append("href=\"").append(req.replace("Update", "")).append("\">");
                    } else {
                        sb.append("href=\""+ WebUtil.getServletPath(aHttpRequest, "/q/")).append(req.replace("#", "/")).append("\" target=\"").append(req.replace("#", "_")).append("\">");
                    }

                    sb.append(req).append("</a></td>").append("<td nowrap>").append(sub).append("</td></tr>");
                }

                sb.append("\n</tbody>\n</table>\n</div>");
            }
        }

        sb.append("\n</span>");
        aTagTable.put(aTag, sb.toString());

        return;
    }

    /**
     * Method to get search results ids
     *
     * @param aHttpRequest  HttpRequestObject
     * @param aRequestId    request id
     */
    @SuppressWarnings("unchecked")
    private ArrayList<Integer> getResultsList(HttpServletRequest aHttpRequest, int aRequestId) throws IOException {
        HttpSession session = aHttpRequest.getSession(true);

        if (session.isNew() == true) {
            return null;
        }

        String sessionId = aHttpRequest.getParameter("sessionId");

        if ((sessionId == null) || (sessionId.equals("") == true)) {
            return null;
        }

        ArrayList          obj         = (ArrayList) session.getAttribute(sessionId);
        ArrayList<Integer> resultsList = new ArrayList<Integer>();

        if (obj == null) {
            return null;
        }

        int size = obj.size();

        for (int i = 0; i < size; i++) {
            Object o = obj.get(i);

            if (o instanceof Integer) {
                resultsList.add((Integer) o);
            }
        }

        if ((resultsList.size() < 2) || session.isNew() || (resultsList.indexOf(new Integer(aRequestId)) == -1)) {
            return null;
        }

        return resultsList;
    }

    /*
     */
    @SuppressWarnings("unchecked")
    private static ArrayList<String> getSearchTextList(HttpServletRequest aHttpRequest) {
        HttpSession session = aHttpRequest.getSession(true);

        if (session.isNew() == true) {
            return (new ArrayList<String>());
        }

        String sessionId = (aHttpRequest.getParameter("sessionId") == null)
                           ? ""
                           : aHttpRequest.getParameter("sessionId").trim();

        if (sessionId.equals("") == true) {
            return (new ArrayList<String>());
        }

        String    txtSessionId = sessionId + "_TextEntries";
        ArrayList obj          = (ArrayList) session.getAttribute(txtSessionId);

        if (obj == null) {
            return (new ArrayList<String>());
        }

        ArrayList<ParseEntry> textEntries = new ArrayList<ParseEntry>();
        int                   size        = obj.size();

        for (int i = 0; i < size; i++) {
            Object o = obj.get(i);

            if (o instanceof ParseEntry) {
                textEntries.add((ParseEntry) o);
            }
        }

        if ((textEntries == null) || (textEntries.size() == 0)) {
            return (new ArrayList<String>());
        }

        ArrayList<String> stringList = new ArrayList<String>();

        for (ParseEntry pe : textEntries) {
            if (pe.getConnective() == Connective.C_NOT) {
                continue;
            }

            ArrayList<String> argList = pe.getArgList();

            if ((argList == null) || (argList.size() == 0)) {
                continue;
            }

            for (String str : argList) {
                str = str.trim();

                if (str.startsWith("+")) {
                    str = str.substring(1);
                }

                if (stringList.contains(str) == false) {
                    stringList.add(str);
                }
            }
        }

        return stringList;
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
     * @param aHttpRequest TODO
     * @param aPermTable Permission table for this user.
     *
     * @return HTML.
     */
    private String isAdmin(HttpServletRequest aHttpRequest, Hashtable<String, Integer> aPermTable) {
        StringBuffer buffer = new StringBuffer();

        if (aPermTable == null) {
            return buffer.toString();
        }

        //buffer.append("&nbsp;<SPAN class=\"l cw sx\" onclick=\"openAdmin()\"").append(">Administration</SPAN>&nbsp;");
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
}


class PrefixComparator implements Comparator<String>, Serializable {
    private int mySortOrder;

    //~--- methods ------------------------------------------------------------

    public int compare(String obj1, String obj2) {
        obj1 = obj1.toUpperCase();
        obj2 = obj2.toUpperCase();

        //
        // Attachments Strings are of the form Update#10/1, Update#1/1,
        // Update#1/2. They are prefixed by /NN counter as Map cannot have
        // multiple Update#NN key values for actions with multiple
        // attachments.
        // Related Requests are of the form prefix#NNN.
        //
        int counter1 = 0;
        int counter2 = 0;
        int index    = obj1.indexOf("/");

        if (index > 0) {
            counter1 = Integer.parseInt(obj1.substring(index + 1));
            obj1     = obj1.substring(0, index);
        }

        String prefix1 = obj1.substring(0, obj1.indexOf("#"));

        obj1 = obj1.substring(obj1.indexOf("#") + 1);

        int id1 = 0;

        if (obj1.indexOf("#") != -1) {
            id1 = Integer.parseInt(obj1.substring(obj1.indexOf("#") + 1));
        } else {
            id1 = Integer.parseInt(obj1);
        }

        index = obj2.indexOf("/");

        if (index > 0) {
            counter2 = Integer.parseInt(obj2.substring(index + 1));
            obj2     = obj2.substring(0, index);
        }

        String prefix2 = obj2.substring(0, obj2.indexOf("#"));

        obj2 = obj2.substring(obj2.indexOf("#") + 1);

        int id2 = 0;

        if (obj2.indexOf("#") != -1) {
            id2 = Integer.parseInt(obj2.substring(obj2.indexOf("#") + 1));
        } else {
            id2 = Integer.parseInt(obj2);
        }

        int diff = 0;

        if (mySortOrder == 0) {
            diff = prefix1.compareTo(prefix2);

            if (diff == 0) {
                diff = new Integer(id1).compareTo(new Integer(id2));
            }

            if (diff == 0) {
                diff = counter1 - counter2;
            }
        } else {
            diff = prefix2.compareTo(prefix1);

            if (diff == 0) {
                diff = new Integer(id2).compareTo(new Integer(id1));
            }

            if (diff == 0) {
                diff = counter2 - counter1;
            }
        }

        return diff;
    }

    //~--- set methods --------------------------------------------------------

    public void setSortOrder(int aOrder) {

        //
        // set mySortOrder = 0 for Ascending
        //
        mySortOrder = aOrder;
    }
}
