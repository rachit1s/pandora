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
 * AddHtmlRequest.java
 *
 * $Header:
 */
package transbit.tbits.webapps;

//~--- non-JDK imports --------------------------------------------------------
import static transbit.tbits.Helper.TBitsConstants.PKG_WEBAPPS;
import static transbit.tbits.Helper.TBitsPropEnum.KEY_DOMAIN;
import static transbit.tbits.Helper.TBitsPropEnum.KEY_TMPDIR;
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
import static transbit.tbits.webapps.WebUtil.ADD_ACTION;
import static transbit.tbits.webapps.WebUtil.ADD_REQUEST;
import static transbit.tbits.webapps.WebUtil.ADD_SUBREQUEST;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Set;
import java.util.TimeZone;
import java.util.Map.Entry;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import transbit.tbits.ExtUI.SlotFillerFactory;
import transbit.tbits.ExtUI.ISlotFiller;
import transbit.tbits.Helper.ActionHelper;
import transbit.tbits.Helper.DateTimeParser;
import transbit.tbits.Helper.IUCValidator;
import transbit.tbits.Helper.Messages;
import transbit.tbits.Helper.TBitsConstants;
import transbit.tbits.TVN.WebdavUtil;
import transbit.tbits.api.APIUtil;
import transbit.tbits.api.AddRequest;
import transbit.tbits.api.DiffEntry;
import transbit.tbits.api.UpdateRequest;
import transbit.tbits.authentication.AuthConstants;
import transbit.tbits.common.Configuration;
import transbit.tbits.common.DTagReplacer;
import transbit.tbits.common.DatabaseException;
import transbit.tbits.common.PropertiesHandler;
import transbit.tbits.common.TBitsLogger;
import transbit.tbits.common.Timestamp;
import transbit.tbits.common.Utilities;
import transbit.tbits.config.CaptionsProps;
import transbit.tbits.config.DependencyConfig;
import transbit.tbits.config.DraftConfig;
import transbit.tbits.config.SysConfig;
import transbit.tbits.config.TypeDependencyMap;
import transbit.tbits.config.WebConfig;
import transbit.tbits.domain.Action;
import transbit.tbits.domain.BAMenu;
import transbit.tbits.domain.BusinessArea;
import transbit.tbits.domain.DataType;
import transbit.tbits.domain.Dependency;
import transbit.tbits.domain.DependentField;
import transbit.tbits.domain.DisplayGroup;
import transbit.tbits.domain.Field;
import transbit.tbits.domain.IPreRenderer;
import transbit.tbits.domain.Permission;
import transbit.tbits.domain.Request;
import transbit.tbits.domain.RolePermission;
import transbit.tbits.domain.TransferredRequest;
import transbit.tbits.domain.Type;
import transbit.tbits.domain.User;
import transbit.tbits.domain.UserDraft;
import transbit.tbits.domain.Dependency.DepLevel;
import transbit.tbits.domain.Dependency.DepType;
import transbit.tbits.domain.DependentField.DepRole;
import transbit.tbits.exception.APIException;
import transbit.tbits.exception.TBitsException;
import transbit.tbits.plugin.PluginManager;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.oreilly.servlet.multipart.FilePart;
import com.oreilly.servlet.multipart.MultipartParser;
import com.oreilly.servlet.multipart.ParamPart;
import com.oreilly.servlet.multipart.Part;

//~--- classes ----------------------------------------------------------------

/**
 * This servlet is used to log Request from web to the TBits system.
 *
 * @author  : Vaibhav,g,Vinod Gupta,Nitiraj
 * @version : $Id: $
 */
public class AddHtmlRequest extends HttpServlet implements TBitsConstants {

    public static final String REQUEST_FILES = "requestFiles";

	// Logger that logs information/error messages to the Application Log.
    public static final TBitsLogger LOG = TBitsLogger.getLogger(PKG_WEBAPPS);

    // Constant that holds the form encoding type.
    private static final String MULTIPART_CONTENT_TYPE		= "multipart/form-data";
    private static final String HTML_TEXT           	    = "web/tbits-ext-text.htm";
	private static final String HTML_MULTIVALUE             = "web/tbits-multi-value.htm";
    private static final String HTML_SUMMARY       		    = "web/tbits-summary.htm";
    private static final String HTML_RELATED          		= "web/tbits-related.htm";
    private static final String HTML_MEMO              		= "web/tbits-memo.htm";
    private static final String HTML_DISPLAY_GROUP_HEADER 	= "web/tbits-display-group-header.htm";
    private static final String HTML_DISPLAY_GROUP_FOOTER = "web/tbits-display-group-footer.htm";
    

    // HTML Interfaces used to display the Add-Request page in TBits.
    private static final String HTML_MAIN   = "web/tbits-add-request.htm";
    private static final String HTML_ATTACH = "web/tbits-attach.htm";
    private static final String HTML_ASSOC  = "web/tbits-associate.htm";

    // Default time zone.
    private static final TimeZone DEFAULT_ZONE = TimeZone.getDefault();
    private static final String   CLOSE_FILE   = "web/tbits-close.htm";

	public static final String PREFILL_TABLE = "prefill_table";

//	private static final int IS_POST_FOR_ADD_REQUEST = 0;
//	private static final int IS_ADD_REQUEST_GET = 1;
//	private static final int IS_POST_FOR_UPDATE_REQUEST = 0;
//	private static final int IS_ADD_ACTION = 3 ;

    // Location of attachments prior to processing.
//    public static String ourTmpLocation;

    // ArrayList which contains all the tags to be replaced.
    private static ArrayList<String> tagList;
    private static ArrayList<String> myYUIJSs ;
    private static ArrayList<String> myTbitsJSs ;
//    private static ArrayList<String> myAddReqCSSs ;
    
    //~--- static initializers ------------------------------------------------

    static /* public void init() */ {
    	// added tag called extSubmitButtonList for extended header links on the bottom of add request page : filled by ExtUIRenderer plugins ( if required )
        tagList = Utilities.toArrayList(

			//append("sub_category_label,") and .append("sub_category_list,"). were added while trying to add Education ont the search panel.
            new StringBuilder().append("title,").append("cssFile,").append("systemId,").append("sysPrefix,").append("loggedDate,").append("action_subject_display,").append("sendMail,").append(
                "mailLoggers,").append("caller,").append("requestId,").append("show_exception_block,").append("action_diff_exception,").append("sys_id_list,").append("sys_id_list_disabled,").append(
                "successMessage,").append("successMsgDisplay,").append("is_private_label,").append("is_private_display,").append("is_private_disabled,").append("is_private,").append(
                "privateClassName,").append("category_id_label,").append("category_id_disabled,").append("category_id_list,").append("exception_list,").append("assignee_ids_label,").append(
                "assignee_ids_disabled,").append("assignee_ids,").append("logger_ids_label,").append("logger_ids_disabled,").append("logger_ids,").append("severity_id_label,").append(
                "severity_id_disabled,").append("severity_id_list,").append("status_id_label,").append("status_id_disabled,").append("status_id_list,").append("request_type_id_label,").append(
                "request_type_id_disabled,").append("request_type_id_list,").append("office_id_label,").append("office_id_disabled,").append("office_id_list,").append("office_id_display,").append(
                "datetime_display,").append("due_datetime_label,").append("due_datetime_disabled,").append("due_time_disabled,").append("due_datetime,").append("due_datetime_img_display,").append(
                "due_datetime_box_display,").append("due_date_option,").append("disableTime,").append("time_option_disabled,").append("notify_label,").append("notify_disabled,").append(
                "notify,").append("notify_loggers,").append("notify_loggers_disabled,").append("cc_ids_label,").append("cc_ids_disabled,").append("cc_ids,").append("subscriber_ids_label,").append(
                "subscriber_ids,").append("subscriber_ids_disabled,").append("subject_label,").append("subject,").append("subject_disabled,").append("attachLink,").append("associateLink,").append(
                "summaryLink,").append("memoLink,").append("efLink,").append("relatedLink,").append("associateCtrl,").append("extended_fields,").append("attachCtrl,").append("summaryCtrl,").append(
                "memoCtrl,").append("relatedCtrl,").append("description_label,").append("description,").append("description_disabled,").append("openSummary,").append("openAssociate,").append(
                "openExtended,").append("openRelated,").append("csList,").append("execFunctions,").append("action_subject,").append("showDiff,").append("diffList,").append("diffFieldList,").append(
                "repliedToAction,").append("is_draft,").append("DTimestamp,").append("userDraftsInfo,").append("instanceBoldHyd,").append("instanceBoldNyc,").append("instancePathHyd,").append(
                "instancePathNyc,").append("autoSaveRate,").append("nearestPath,").append("sysName,").append("onMouseOverHyd,").append("onMouseOverNyc,").append("userLogin,")
				.append("target_search,").append("requestFiles,").append("baJsonArray,")
                .append("baMenuList,").append("sysPrefixLabel,").append("category_id_dependency_list,").append("category_id_dep_fun_call,").append("request_type_id_dependency_list,")
                .append("request_type_id_dep_fun_call,").append("severity_id_dependency_list,").append("severity_id_dep_fun_call,").append("status_id_dependency_list,").append("status_id_dep_fun_call")
                .append("office_id_dep_fun_call,").append("office_id_dependency_list,").append("calendar_date_format,").append("addRequestFooterSlot,")
                .append("tbitsJSs,").append("YUIJSs,").append("reqCss,").append("draftId").toString());

        	myYUIJSs = Utilities.toArrayList( 
											"web/yui/build/yahoo-dom-event/yahoo-dom-event.js,"+
											"web/yui/build/container/container_core-min.js,"+
											"web/yui/build/menu/menu-min.js,"+
											"web/yui/build/element/element-min.js,"+
											"web/yui/build/button/button-min.js,"+
											"web/yui/build/datasource/datasource-min.js,"+
											"web/yui/build/datatable/datatable-min.js,"+
											"web/yui/build/uploader/uploader-min.js,"+
											"web/yui/build/json/json-min.js,"+
											"web/yui/build/utilities/utilities.js,"+
											"web/yui/build/yahoo/yahoo-min.js,"+
											"web/yui/build/event/event-min.js"
        								 );
        	
        	myTbitsJSs = Utilities.toArrayList(
							        			"web/scripts/add-request-min.js,"+
							        			"web/scripts/common-min.js,"+
							        			"web/ckeditor/ckeditor.js,"	+
							        			"web/scripts/wac-min.js,"+
							        			"web/scripts/messages-min.js,"+
							        			"web/scripts/wtt-min.js,"+
							        			"web/scripts/tbits-yui-utils-min.js,"+
							        			"web/scripts/cal-min.js,"+
							        			"web/scripts/uploader-min.js,"+
							        			"web/scripts/ajax-uploader-min.js,"+
							        			"web/yui/build/connection/connection-min.js," /*+
							        			"web/scripts/tt-min.js,"*/
        									   ) ;
        	
//        	myAddReqCSSs = Utilities.toArrayList( "web/yui/build/fonts/fonts-min.css,"+
//        										  "web/yui/build/assets/skins/sam/skin.css" 
//        										) ;
        	// Get the location of the temporary directory.
//        try {
//            ourTmpLocation = Configuration.findAbsolutePath(PropertiesHandler.getProperty(KEY_TMPDIR));
//        } catch (IllegalArgumentException e) {
//            LOG.severe(e.toString(), e);
//        }
    }

	

    //~--- methods ------------------------------------------------------------

    /**
     * This method is used to fill the tags if the caller is add-request
     * or add-subrequest.
     */
    public void addActionForm(HttpServletRequest aRequest, Hashtable<String, Object> aTagTable, Hashtable<String, Object> aParamInfo, Hashtable<String, Integer> permTable, BusinessArea aBA,
                              User aUser, Hashtable<String, String> paramTable)
            throws ServletException, TBitsException, DatabaseException, FileNotFoundException, IOException {

        // Get the session.
        HttpSession session = aRequest.getSession();

        // Get the BA and User Configs.
        SysConfig sc               = aBA.getSysConfigObject();
        WebConfig wc               = aUser.getWebConfigObject();
        boolean   allowNullDueDate = sc.getAllowNullDueDate();
        boolean   duedateDisabled  = sc.getIsDueDateDisabled();
        boolean   isTimeDisabled   = sc.getIsTimeDisabled();

        // Get the systemId and the system Prefix.
        int    systemId      = aBA.getSystemId();
        String sysPrefix     = aBA.getSystemPrefix();
        String actionSubject = null;

        // Get the userId and userLogin.
        int    userId    = aUser.getUserId();
        String userLogin = aUser.getUserLogin();

        Request request = (Request) aParamInfo.get(Field.REQUEST);
        
        Hashtable<String,Field> allFields = Field.getFieldsTableBySystemId(systemId) ;
        
        getParamsFromRequest(allFields, request, paramTable ) ;
        
        getParamTable(aRequest, userLogin, paramTable);
        
        // this will override the attributes set from request parameter. It is done for prefilling feature
        Hashtable<String,String> prefillTable = (Hashtable<String, String>) aRequest.getAttribute(PREFILL_TABLE) ;

        getParamsFromPrefill(paramTable, prefillTable) ;

        String draftLoad = paramTable.get("drafts");

        if ((draftLoad != null) && draftLoad.equalsIgnoreCase("true")) {
            getDraftValues(aRequest, aParamInfo, aUser, paramTable);
        }

        if (request == null) {
            throw invalidRequest();
        }

        int requestId = request.getRequestId();

        // Get the permission table for this user.
        if (permTable == null) {
            permTable = RolePermission.getPermissionsBySystemIdAndRequestIdAndUserId(systemId, requestId, userId);
        }

        //
        // Check the basic permissions required to allow a user to come to
        // this page.
        //
        checkBasicPermissions(aBA, permTable, ADD_ACTION);

        //
        // Now that the user has basic permissions to append to a request,
        // perform the following steps.
        // - Get the list of fixed fields for this BA.
        // - Replace the labels of fields with their display names.
        // - Decide if the fields should be enabled/disabled.
        //
        ArrayList<Field> fields = Field.getFixedFieldsBySystemId(systemId);

        showDisplayNames(systemId, fields, aTagTable);
        decideFieldAbility(systemId, ADD_ACTION, fields, aTagTable, permTable, duedateDisabled, allowNullDueDate, sc.getDefaultDueDate());

        // Check if Confidential checkbox be shown.
        boolean viewPrivate = false;

        if (permTable.get(Field.IS_PRIVATE) != null) {
            int permValue = permTable.get(Field.IS_PRIVATE).intValue();

            if ((permValue & Permission.VIEW) == 0) {
                viewPrivate = false;
                aTagTable.put("is_private_display", "none");
            } else {
                viewPrivate = true;
                aTagTable.put("is_private_display", "inline-block");
            }
        } else {
            aTagTable.put("is_private_display", "none");
        }

        // Store them in the tag table.
        aTagTable.put("systemId", Integer.toString(systemId));
        aTagTable.put("sysPrefix", sysPrefix);
        aTagTable.put("userId", Integer.toString(systemId));
        aTagTable.put("userLogin", userLogin);
        aTagTable.put("autoSaveRate", getAutoSaveRate(wc));
        aTagTable.put("requestId", Integer.toString(requestId));
        aTagTable.put("repliedToAction", Integer.toString(request.getMaxActionId()));

		String display_logout = "none";
		if(aRequest.getAuthType() == AuthConstants.AUTH_TYPE)
			display_logout = "";
        
		// The list of BAs the user can view.
        aTagTable.put("sys_id_list", getBAList(systemId, userId));
        ArrayList<BusinessArea> baList = BusinessArea.getBusinessAreasByUserId(userId);
        aTagTable.put("baJsonArray", BAMenu.getBAMenuJsonArray(baList).toString());
        aTagTable.put("baMenuList", BAMenu.getJsonArrayOfAllBAMenus().toString());
        aTagTable.put("sys_id_list_disabled", "disabled");

        // Renders the Categories, Statuses, Severities and Request Types.
        renderFixedTypes(systemId, viewPrivate, aTagTable, paramTable, WebUtil.ADD_ACTION);
        //renderFixedTypes(systemId, viewPrivate, aTagTable, aUser, paramTable, request, aRequest);

        // Render the multi-valued fields.
        String logger = paramTable.get(Field.LOGGER);
        if (logger != null)
            aTagTable.put(Field.LOGGER, logger);
        
        String assignee = paramTable.get(Field.ASSIGNEE);
        if (assignee != null)
            aTagTable.put(Field.ASSIGNEE, assignee);

        String subscriber = paramTable.get(Field.SUBSCRIBER);
        if (subscriber != null)
            aTagTable.put(Field.SUBSCRIBER, subscriber);

        String cc = paramTable.get(Field.CC);
        if (cc != null)
            aTagTable.put(Field.CC, cc);
        
        // Render the text fields.
        String subject = paramTable.get(Field.SUBJECT);
        if(subject != null)
            aTagTable.put(Field.SUBJECT, subject);
        
        // Set the is_drafts paramter
        String draft = paramTable.get("drafts");
        String draftId =paramTable.get("draftid");

        if (draft != null) {
            aTagTable.put("is_draft", "true");
        }

        if (draftId != null) {
            aTagTable.put("draftId", draftId);
        }
        else
        {
        	aTagTable.put("draftId","");
        }
        
        String description = paramTable.get(Field.DESCRIPTION);
        if(description != null)
            aTagTable.put(Field.DESCRIPTION, description);
        

        actionSubject = sysPrefix + "#" + requestId + ": " + Utilities.htmlEncode(request.getSubject());
        aTagTable.put("action_subject", actionSubject);

        if (request.getIsPrivate() == true) {
            aTagTable.put("is_private", " CHECKED ");
            aTagTable.put("privateClassName", "sx b cr");
        } else if ((aBA.getIsPrivate() == true) || (request.getCategoryId().getIsPrivate() == true) || (request.getStatusId().getIsPrivate() == true)
                   || (request.getSeverityId().getIsPrivate() == true) || (request.getRequestTypeId().getIsPrivate() == true)) {

            /*
             * Check the Confidential check box if the category/BA is private
             * and disable it: DFlow#2964
             */
            aTagTable.put("is_private", " CHECKED ");
            aTagTable.put("privateClassName", "sx b cr");
            aTagTable.put("is_private_disabled", " DISABLED ");
        } else {
            aTagTable.put("privateClassName", "sx cr");
        }
        
        String strOpenExtended = paramTable.get("openExtended") ;
        if( strOpenExtended == null )
        	strOpenExtended = "true" ;
        
        aTagTable.put("openExtended", strOpenExtended );

        Field field = null;

        // Check if the user has permission to Change to the parent.
        addAssociateLink(permTable, aTagTable, systemId, ADD_ACTION, request.get(Field.PARENT_REQUEST_ID) );

        // Check if the user has permission to Change to the related requests.
        addRelatedLink(permTable, aTagTable, systemId, ADD_ACTION, paramTable.get(Field.RELATED_REQUESTS) );

        // Fill Logged and DueDate Time.
        if (allowNullDueDate == true) {
            aTagTable.put("due_date_option", "true");
        } else {
            aTagTable.put("due_date_option", "false");
        }

        if (isTimeDisabled == true) {
            aTagTable.put("due_time_disabled", "true");
            aTagTable.put("disableTime", "Enable Time");
        } else {
            aTagTable.put("due_time_disabled", "false");
            aTagTable.put("disableTime", "Disable Time");
        }
        
        String webDateFormat = wc.getWebDateFormat();
        aTagTable.put("calendar_date_format", webDateFormat);
        
        if (isOnlyDateFormat(webDateFormat))
        	aTagTable.put("time_option_disabled", "none");

        Timestamp loggedTS  = (request.getLoggedDate() == null ? null : new Timestamp(request.getLoggedDate().getTime()));
        Timestamp dueDateTS = (request.getDueDate() == null ? null : new Timestamp(request.getDueDate().getTime()));

        if (dueDateTS != null) {
            fillDueDateTime(aTagTable, loggedTS, dueDateTS, isTimeDisabled, wc.getWebDateFormat());
        } else if (allowNullDueDate == false) {
            fillDueDateTime(aTagTable, (int) sc.getDefaultDueDate(), isTimeDisabled, wc.getWebDateFormat());
        } else {
            aTagTable.put("due_datetime", "");
            aTagTable.put("due_datetime" + "_box_display", "none");
        }

        // Rendering Extended Types if any.
        renderExtendedFields(systemId, userId, permTable, aTagTable, paramTable, aRequest, null, ADD_ACTION );

        String requestFiles = paramTable.get(REQUEST_FILES) ;
        if( null == requestFiles )
        {
        	JsonObject reqFiles = ActionHelper.getAttachmentDetailsJSONInternal(permTable, aBA, request, aUser) ; 
        	requestFiles =  reqFiles.toString() ;
        }
        aTagTable.put(REQUEST_FILES, requestFiles) ;
        // Check if the user has permissions to add attachments.
        addAttachmentLink(permTable, aTagTable, systemId, ADD_ACTION);

        // Check if the user has permission to Change to the summary field.
        addSummaryLink(permTable, aTagTable, paramTable, request, systemId);

        // Check if the user has permission to change memo.
        if (permTable.get(Field.MEMO) != null) {
            field = Field.lookupBySystemIdAndFieldName(systemId, Field.MEMO);

            if (field != null) {
                int permission = permTable.get(Field.MEMO);

                if ((permission & Permission.CHANGE) != 0) {
                    StringBuilder ctrlHtml = new StringBuilder();
                    String        linkText = getMemoHtml(ADD_ACTION, field.getDisplayName(), request.getMemo(), ctrlHtml);

                    aTagTable.put("memoLink", linkText);
                    aTagTable.put("memoCtrl", ctrlHtml.toString());
                }
            }
        }

       if (aBA.getIsEmailActive() == true) 
        {
            if (sc.getActionNotify() != 0) 
            {
                aTagTable.put("notify", " checked ");
                aTagTable.put("sendMail", "true");
            } else 
            {
                aTagTable.put("sendMail", "false");
            }
            // Decide on the state of Loggers CheckBox.
            if (sc.getActionNotifyLoggers() == true) 
            {
                aTagTable.put("notify_loggers", " checked ");
                aTagTable.put("mailLoggers", "true");
            } else 
            {
                aTagTable.put("mailLoggers", "false");
            }
        } else {
            aTagTable.put("notify_disabled", " disabled ");
            aTagTable.put("notify_loggers_disabled", "disabled");
            aTagTable.put("sendMail", "false");
            aTagTable.put("mailLoggers", "false");
        }

       String notify = paramTable.get(Field.NOTIFY) ; 
       if( null != notify && ( notify.trim().equalsIgnoreCase("true") || notify.trim().equalsIgnoreCase("checked")) )
       {
    	   aTagTable.put(Field.NOTIFY, "checked") ;    	   
       }
       String sendMail = paramTable.get("sendMail");
       if( null != sendMail && ( sendMail.trim().equalsIgnoreCase("true") || sendMail.trim().equalsIgnoreCase("false") ) )
       {
    	   aTagTable.put("sendMail", sendMail) ;
       }
       String notify_loggers = paramTable.get(Field.NOTIFY_LOGGERS) ; 
       if( null != notify_loggers && ( notify_loggers.trim().equalsIgnoreCase("true") || notify_loggers.trim().equalsIgnoreCase("checked")) )
       {
    	   aTagTable.put(Field.NOTIFY_LOGGERS, "checked") ;
       }
       String mailLoggers = paramTable.get("mailLoggers") ;
       if( null!=mailLoggers && ( mailLoggers.trim().equalsIgnoreCase("true") || mailLoggers.equalsIgnoreCase("false")) )
       {
    	   aTagTable.put("mailLoggers", mailLoggers) ; 
       }
             
        //
        // restore draft timestamp, if any
        //
        String timeStr = aRequest.getParameter("DTimestamp");

        if ((timeStr != null) && (timeStr.trim().equals("") == false)) {
            try {
                aTagTable.put("DTimestamp", timeStr);
            } catch (Exception e) {
                LOG.debug("",(e));
                aTagTable.put("DTimestamp", "0");
            }
        } else {
            aTagTable.put("DTimestamp", "0");
        }

        String draftIdStr = aRequest.getParameter("draftid");

        if ((draftIdStr != null) && (draftIdStr.trim().equals("") == false)) {
            try {
                aTagTable.put("draftid", draftIdStr);
            } catch (Exception e) {
                LOG.debug("",(e));
                aTagTable.put("draftid", "0");
            }
        } else {
            aTagTable.put("draftid", "0");
        }
        //
        // List User Drafts, if any
        //
        aTagTable.put("userDraftsInfo", WebUtil.listUserDrafts(aRequest, userId, true, true));

        return;
    }

    private void getParamsFromRequest(Hashtable<String,Field> allFields , Request request, Hashtable<String, String> paramTable) 
    {
    	for( Enumeration<String> keys = allFields.keys() ; keys.hasMoreElements() ; ) 
    	{
    		String fieldName = keys.nextElement() ;
    		Field field = allFields.get(fieldName) ;
    		String value = request.get(fieldName) ;
    		int dataTypeId = field.getDataTypeId() ;
    		
    		if( dataTypeId == DataType.ATTACHMENTS )
    			continue ;
    		
    		if( field.getIsSetEnabled() && null != value  )
    		{
    			paramTable.put(fieldName, value) ;
    		}
    	}
	}

//	private void addAssociateLink(Hashtable<String, Integer> aPermTable, Hashtable<String, Object> aTagTable, Request aRequest, int aSystemId)
//            throws DatabaseException, IOException, FileNotFoundException {
//        Field field = null;
//
//        // Check if the user has permission to Change to the parent.
//        if (aPermTable.get(Field.PARENT_REQUEST_ID) != null) {
//            field = Field.lookupBySystemIdAndFieldName(aSystemId, Field.PARENT_REQUEST_ID);
//
//            if (field != null) {
//                int permission = aPermTable.get(Field.PARENT_REQUEST_ID);
//
//                if ((permission & Permission.CHANGE) != 0) {
//                    int           id       = aRequest.getParentRequestId();
//                    StringBuilder ctrlHtml = new StringBuilder();
//                    String        linkText = "";
//
//                    if (id != 0) {
//                        linkText = getAssociationHtml(field.getDisplayName(), "", Integer.toString(id), ctrlHtml);
//                    } else {
//                        linkText = getAssociationHtml(field.getDisplayName(), "", "", ctrlHtml);
//                    }
//
//                    aTagTable.put("associateLink", linkText);
//                    aTagTable.put("associateCtrl", ctrlHtml.toString());
//                }
//            } else {
//                LOG.warn("Parent Request Id Field Object is NULL.");
//            }
//        } else {
//            LOG.warn("No Entry for Parent Request Id is Permissions Table.");
//        }
//    }
//
//    private void addAssociateLink(Hashtable<String, Integer> aPermTable, Hashtable<String, Object> aTagTable, String aParentId, int aSystemId)
//            throws DatabaseException, IOException, FileNotFoundException {
//        Field field = null;
//
//        // Check if the user has permission to Change to the parent.
//        if (aPermTable.get(Field.PARENT_REQUEST_ID) != null) {
//            field = Field.lookupBySystemIdAndFieldName(aSystemId, Field.PARENT_REQUEST_ID);
//
//            if (field != null) {
//                int permission = aPermTable.get(Field.PARENT_REQUEST_ID);
//
//                if ((permission & Permission.CHANGE) != 0) {
//                    StringBuilder ctrlHtml = new StringBuilder();
//                    String        linkText = "";
//
//                    if (aParentId != null) {
//                        linkText = getAssociationHtml(field.getDisplayName(), "", aParentId, ctrlHtml);
//                    } else {
//                        linkText = getAssociationHtml(field.getDisplayName(), "", "", ctrlHtml);
//                    }
//
//                    aTagTable.put("associateLink", linkText);
//                    aTagTable.put("associateCtrl", ctrlHtml.toString());
//                }
//            } else {
//                LOG.warn("Parent Request Id Field Object is NULL.");
//            }
//        } else {
//            LOG.warn("No Entry for Parent Request Id is Permissions Table.");
//        }
//    }
//
//    private void addAssociateLink(Hashtable<String, Integer> aPermTable, Hashtable<String, Object> aTagTable, int aSystemId, int aCaller, String aParent)
//            throws DatabaseException, IOException, FileNotFoundException {
//        Field field = null;
//
//        if (aPermTable.get(Field.PARENT_REQUEST_ID) != null) {
//            field = Field.lookupBySystemIdAndFieldName(aSystemId, Field.PARENT_REQUEST_ID);
//
//            if (field != null) {
//                int permission = aPermTable.get(Field.PARENT_REQUEST_ID);
//
//                if ((permission & Permission.CHANGE) != 0) {
//                    StringBuilder ctrlHtml = new StringBuilder();
//                    String        linkText = "";
//
//                    if (aCaller == ADD_REQUEST) {
//                        linkText = getAssociationHtml(field.getDisplayName(), "", aParent, ctrlHtml);
//                    } else {
//                        linkText = getAssociationHtml(field.getDisplayName(), " disabled=\"true\" ", aParent, ctrlHtml);
//                    }
//
//                    aTagTable.put("associateLink", linkText);
//                    aTagTable.put("associateCtrl", ctrlHtml.toString());
//                }
//            }
//        }
//    }

    private void addAssociateLink(Hashtable<String, Integer> aPermTable, Hashtable<String, Object> aTagTable, int aSystemId, int aCaller, String aParent)
    throws DatabaseException, IOException, FileNotFoundException {
		Field field = null;
		
		if (aPermTable.get(Field.PARENT_REQUEST_ID) != null) 
		{
		    field = Field.lookupBySystemIdAndFieldName(aSystemId, Field.PARENT_REQUEST_ID);
		
		    if (field != null) 
		    {
		        int permission = aPermTable.get(Field.PARENT_REQUEST_ID);
		
				if( ( aCaller == ADD_REQUEST || aCaller == ADD_SUBREQUEST ) && ( ( permission & Permission.ADD ) == 0 )  )
					return ;
				else if( aCaller == ADD_ACTION && ( ( permission & Permission.CHANGE ) == 0 ) )
					return ;
					
				String disabled = "" ;
				if( aCaller == ADD_SUBREQUEST ) 
					disabled = "disabled='true'" ;				
				
			    StringBuilder ctrlHtml = new StringBuilder();
			    String        linkText = "";
				
		        if( aParent == null )
		        	aParent = "" ;
		        
			    linkText = getAssociationHtml(field.getDisplayName(), disabled , aParent, ctrlHtml);               
		
		        aTagTable.put("associateLink", linkText);
		        aTagTable.put("associateCtrl", ctrlHtml.toString());
		
		    }
		}
    }
    
    private void addAttachmentLink(Hashtable<String, Integer> aPermTable, Hashtable<String, Object> aTagTable, int aSystemId, int caller) throws DatabaseException, IOException 
    {   // takes the permission of each attachment field and if all have 
        // no add and no update permission then does not add the attachment table and link in the form.
        int addPerm = 0 ;
        int updatePerm = 0 ;
        for( Field f : Field.lookupBySystemId(aSystemId))
        {
        	if( f.getDataTypeId() == DataType.ATTACHMENTS )
        	{
        		Integer attPerm = aPermTable.get(f.getName());
        		if( null != attPerm )
        		{
        			addPerm = ( addPerm | attPerm ) == 0 ? 0 : 1 ;
        			updatePerm = ( updatePerm | attPerm ) == 0 ? 0 : 1 ;
        		}        		
        	}        		
        }
        if( caller == ADD_REQUEST || caller == ADD_SUBREQUEST )
        {
        	if( addPerm == 0  )
        		return ;
        }
        else if ( (addPerm == 0) && (updatePerm == 0) ) // this is update request ..
        	return ;
        
        StringBuilder ctrlHtml = new StringBuilder();
        String        linkText = getAttachmentHtml(ctrlHtml);

        if ((aTagTable.get("extended_fields") != null) && (aTagTable.get("extended_fields").equals("") == false)) {
            linkText = "|&nbsp;" + linkText;
        }

        aTagTable.put("attachLink", linkText);
        aTagTable.put("attachCtrl", ctrlHtml.toString());
    }

//    private void addRelatedLink(Hashtable<String, Integer> aPermTable, Hashtable<String, Object> aTagTable, String aRelatedId, int aSystemId)
//            throws DatabaseException, IOException, FileNotFoundException {
//        Field field = null;
//
//        // Check if the user has permission to Change to the parent.
//        if (aPermTable.get(Field.RELATED_REQUESTS) != null) {
//            field = Field.lookupBySystemIdAndFieldName(aSystemId, Field.RELATED_REQUESTS);
//
//            if (field != null) {
//                int permission = aPermTable.get(Field.RELATED_REQUESTS);
//
//                if ((permission & Permission.CHANGE) != 0) {
//                    StringBuilder ctrlHtml = new StringBuilder();
//                    String        linkText = "";
//
//                    if (aRelatedId != null) {
//                        linkText = getRelatedHtml(aSystemId, field.getDisplayName(), "", aRelatedId, ctrlHtml);
//                    } else {
//                        linkText = getRelatedHtml(aSystemId, field.getDisplayName(), "", "", ctrlHtml);
//                    }
//
//                    aTagTable.put("relatedLink", linkText);
//                    aTagTable.put("relatedCtrl", ctrlHtml.toString());
//                }
//            } else {
//                LOG.warn("Related Request Id of  Field Object is NULL.");
//            }
//        } else {
//            LOG.warn("No Entry for Related Request Id in Permissions Table.");
//        }
//    }
//
//    private void addRelatedLink(Hashtable<String, Integer> aPermTable, Hashtable<String, Object> aTagTable, int aSystemId, int aCaller, Hashtable<String, String> aParamTable)
//            throws DatabaseException, IOException, FileNotFoundException {
//        Field field = null;
//        
//        if (aPermTable.get(Field.RELATED_REQUESTS) != null) 
//        {
//            field = Field.lookupBySystemIdAndFieldName(aSystemId, Field.RELATED_REQUESTS);
//
//            if (field != null) 
//            {
//                int permission = aPermTable.get(Field.RELATED_REQUESTS);
//
//                if ((permission & Permission.ADD) != 0) {
//                    StringBuilder ctrlHtml = new StringBuilder();
//                    String        linkText = "";
//                    String        ids      = "";
//
//                    if (aParamTable.get(Field.RELATED_REQUESTS) != null) {
//                        ids = aParamTable.get(Field.RELATED_REQUESTS);
//                    }
//
//                    linkText = getRelatedHtml(aSystemId, field.getDisplayName(), "", ids, ctrlHtml);
//                    aTagTable.put("relatedLink", linkText);
//                    aTagTable.put("relatedCtrl", ctrlHtml.toString());
//                }
//            } else {
//                LOG.warn("Related Request Id Field object is null");
//            }
//        } else {
//            LOG.warn("No Entry for Related Request Id in Permissions Table.");
//        }
//    }

    private void addRelatedLink(Hashtable<String, Integer> aPermTable, Hashtable<String, Object> aTagTable, int aSystemId, int caller , String value )
            throws DatabaseException, IOException, FileNotFoundException 
    {
        Field field = null;

        // Check if the user has permission to Change to the parent.
        if (aPermTable.get(Field.RELATED_REQUESTS) != null) 
        {
            field = Field.lookupBySystemIdAndFieldName(aSystemId, Field.RELATED_REQUESTS);

            if (field != null) 
            {
                int permission = aPermTable.get(Field.RELATED_REQUESTS);

                if( ( caller == ADD_REQUEST || caller == ADD_SUBREQUEST ) && ((permission & Permission.ADD) == 0 ) )
                	return ;
                else if( caller == ADD_ACTION && (permission & Permission.CHANGE) == 0 ) // this is ADD_ACTION
                	return ;
                
                if( null == value )
                	value = "" ;

                    StringBuilder ctrlHtml = new StringBuilder();
                    String        linkText = "";

                    linkText = getRelatedHtml(aSystemId, field.getDisplayName(), " ", value, ctrlHtml);

                    aTagTable.put("relatedLink", linkText);
                    aTagTable.put("relatedCtrl", ctrlHtml.toString());
            }
        }
    }

    /**
     * This method is used to fill the tags if the caller is add-request
     * or add-subrequest.
     *
     * @param aCaller The page being accessed.
     * @param aRequest HttpServletRequest Object
     * @param aTagTable Hashtable containing the [tagName,Replacement] pairs
     * @param aParamInfo ParamInfo
     * @param aBA Business Area being accessed.
     * @param aUser User accessing the page.
     */
    private void addRequestForm(int aCaller, HttpServletRequest aRequest, Hashtable<String, Object> aTagTable, Hashtable<String, Object> aParamInfo, BusinessArea aBA, User aUser,
                                Hashtable<String, String> paramTable)
            throws ServletException, TBitsException, DatabaseException, FileNotFoundException, IOException {

        // Get the session.
        HttpSession session = aRequest.getSession();

        // Get the BA and User Configs.
        SysConfig sc = aBA.getSysConfigObject();
        WebConfig wc = aUser.getWebConfigObject();

        // Get the systemId and the system Prefix.
        int    systemId  = aBA.getSystemId();
        String sysPrefix = aBA.getSystemPrefix();

        // Get the userId and userLogin.
        int    userId    = aUser.getUserId();
        String userLogin = aUser.getUserLogin();

        // this parameter table will be created from the reqeust-parameters
        // but will be overridden by the parameters explicitly set in the request attributes
        LOG.info("User: " + userLogin + " BusinessArea: " + aBA.getSystemPrefix());
        getParamTable(aRequest, userLogin, paramTable);
        
        // this will override the attributes set from request parameter. It is done for prefilling feature
        Hashtable<String,String> prefillTable = (Hashtable<String, String>) aRequest.getAttribute(PREFILL_TABLE) ;

        getParamsFromPrefill( paramTable, prefillTable ) ;

        
        String draftLoad = paramTable.get("draftload");
        // draft values will override all other values 
        if ((draftLoad != null) && draftLoad.equalsIgnoreCase("true")) {
            getDraftValues(aRequest, aParamInfo, aUser, paramTable);
        }
      
        boolean allowNullDueDate = sc.getAllowNullDueDate();
        boolean duedateDisabled  = sc.getIsDueDateDisabled();
        boolean isTimeDisabled   = sc.getIsTimeDisabled();

        // Get the permission table for this user.
        Hashtable<String, Integer> permTable = RolePermission.getPermissionsBySystemIdAndUserId(systemId, userId);

        //
        // Check the basic permissions required to allow a user to come to
        // this page.
        //
        checkBasicPermissions(aBA, permTable, aCaller);

        //
        // Now that the user has basic permissions to log a request, perform
        // the following steps.
        // - Get the list of fixed fields for this BA.
        // - Replace the labels of fields with their display names.
        // - Decide if the fields should be enabled/disabled.
        ArrayList<Field> fields = Field.getFixedFieldsBySystemId(systemId);

        showDisplayNames(systemId, fields, aTagTable);
        decideFieldAbility(systemId, ADD_REQUEST, fields, aTagTable, permTable, duedateDisabled, allowNullDueDate, sc.getDefaultDueDate());

        // Check if Confidential checkbox be shown.
        boolean viewPrivate = false;

        if (permTable.get(Field.IS_PRIVATE) != null) {
            int permValue = permTable.get(Field.IS_PRIVATE).intValue();

            if ((permValue & Permission.VIEW) == 0) {
                viewPrivate = false;
                aTagTable.put("is_private_display", "none");
            } else {
                viewPrivate = true;
                aTagTable.put("is_private_display", "inline-block");
                aTagTable.put("privateClassName", "sx cr");
            }
        } else {
            aTagTable.put("is_private_display", "none");
        }

        // Store them in the tag table.
        aTagTable.put("systemId", Integer.toString(systemId));
        aTagTable.put("sysPrefix", sysPrefix);
        aTagTable.put("userId", Integer.toString(systemId));
        aTagTable.put("userLogin", userLogin);

        // The list of BAs the user can view.
        aTagTable.put("sys_id_list", getBAList(systemId, userId));
        ArrayList<BusinessArea> baList = BusinessArea.getBusinessAreasByUserId(userId);
        aTagTable.put("baJsonArray", BAMenu.getBAMenuJsonArray(baList).toString());
        aTagTable.put("baMenuList", BAMenu.getJsonArrayOfAllBAMenus().toString());
        aTagTable.put("sysPrefixLabel", aBA.getDisplayName() + " [" + sysPrefix + "]");
        aTagTable.put("autoSaveRate", getAutoSaveRate(wc));

        if ((aCaller == ADD_ACTION) || (aCaller == ADD_SUBREQUEST)) {
            aTagTable.put("sys_id_list_disabled", "disabled");
        }

        // Fill the user fields.
        aTagTable.put(Field.LOGGER, aUser.getUserLogin());
        
        if (allowNullDueDate == true) {
            aTagTable.put("due_date_option", "true");
        } else {
            aTagTable.put("due_date_option", "false");
        }

        if (isTimeDisabled == true) {
            aTagTable.put("due_time_disabled", "true");
            aTagTable.put("disableTime", "Enable Time");
        } else {
            aTagTable.put("due_time_disabled", "false");
            aTagTable.put("disableTime", "Disable Time");
        }

        String webDateFormat = wc.getWebDateFormat();
		if ((allowNullDueDate == false) || (sc.getDefaultDueDate() != 0)) {
            fillDueDateTime(aTagTable, (int) sc.getDefaultDueDate(), isTimeDisabled, webDateFormat);
        } else {
            aTagTable.put("due_datetime", "");
            aTagTable.put("due_datetime" + "_box_display", "none");
        }
		
		String dueDate = paramTable.get(Field.DUE_DATE) ;
		if( null != dueDate )
		{
			String apidd = parseHTMLDate(dueDate) ;			
			if( ! apidd.equalsIgnoreCase(""))
			{
				Timestamp ts;
				try {
					ts = new Timestamp(apidd,TBitsConstants.API_DATE_FORMAT);
					String userdd = ts.toCustomFormat(webDateFormat) ;
					aTagTable.put(Field.DUE_DATE, userdd) ;
				} catch (ParseException e) 
				{
					LOG.info("The received date is not in correct format.") ;
					e.printStackTrace();
				}			
			}
		}
		
        aTagTable.put("calendar_date_format", webDateFormat);
        if (isOnlyDateFormat(webDateFormat))
        	aTagTable.put("time_option_disabled", "none");

        // Set the is_drafts paramter
        String draft = paramTable.get("drafts");
        String draftId = paramTable.get("draftid");

        if (draft != null) {
            aTagTable.put("is_draft", "true");
        }
        
        if (draftId != null) {
            aTagTable.put("draftId", draftId);
        }
        else
        {
        	aTagTable.put("draftId", "") ; 
        }

        // SysId, RequestId need not be rendered.
        // Render the four standard types: Cat, Stat, Sev, Type
        renderFixedTypes(systemId, viewPrivate, aTagTable, paramTable, WebUtil.ADD_REQUEST);
        //renderFixedTypes(systemId, viewPrivate, aTagTable, aUser, paramTable, null, aRequest);

        // Only Logger Field should be given a default value: user login
        String logger = paramTable.get(Field.LOGGER);
        if(logger != null)
            aTagTable.put(Field.LOGGER, logger);
        
        // Other multi-valued fields need not be given a value.
        // Assignee, Subscriber, Cc = "";
        String assignee = paramTable.get(Field.ASSIGNEE);
        if(assignee != null)
            aTagTable.put(Field.ASSIGNEE, assignee);
        
        String subscriber = paramTable.get(Field.SUBSCRIBER);
        if(subscriber != null)
            aTagTable.put(Field.SUBSCRIBER, subscriber);
        
        String cc = paramTable.get(Field.CC);
        if(cc != null)
            aTagTable.put(Field.CC, cc);
       
        // Subject and description are empty by default.
        String subject = paramTable.get(Field.SUBJECT);
        if(subject != null)
            aTagTable.put(Field.SUBJECT, Utilities.htmlEncode(subject));
        
        String description = paramTable.get(Field.DESCRIPTION);
        if(description != null)
            aTagTable.put(Field.DESCRIPTION, Utilities.htmlEncode(description));
       
        
        Field field = null;

        // Check if we are rendering add-subrequest page.
        String parent = "";

        if (paramTable.get(Field.PARENT_REQUEST_ID) != null) {
            parent = paramTable.get(Field.PARENT_REQUEST_ID);
        }

        if (aCaller == ADD_SUBREQUEST) {
            aTagTable.put(Field.PARENT_REQUEST_ID + "_disabled", "disabled=\"true\" ");

            Request req = (Request) aParamInfo.get(Field.REQUEST);

            if (req != null) {
                parent = "" + req.getRequestId();
            }

            if (parent != null) {
                aTagTable.put(Field.PARENT_REQUEST_ID, parent);
                aTagTable.put("requestId", parent);
            } else {
                LOG.warn("parent value is null");
                aTagTable.put("requestId", "0");
                parent = "";
            }
        }
        aTagTable.put("openExtended", "true");
        //
        // Association is allowed only the if the user has change permission on
        // parent request id field.
        //
        addAssociateLink(permTable, aTagTable, systemId, aCaller, parent);

        //
        // Relating Requests is allowed only the if the user has
        // add permission on parent request id field.
        //
        addRelatedLink(permTable, aTagTable, systemId, aCaller, paramTable.get(Field.RELATED_REQUESTS));

        // Check if the user has permission to Add the summary.
        addSummaryLink(permTable, aTagTable, paramTable, systemId, aCaller);

        // Render the mail related options.
        if (aBA.getIsEmailActive() == true) {
            if (sc.getRequestNotify() != 0) {
                aTagTable.put("notify", " checked ");
                aTagTable.put("sendMail", "true");
            } else {
                aTagTable.put("sendMail", "false");
            }

            // Decide on the state of Loggers CheckBox.
            if (sc.getRequestNotifyLoggers() == true) {
                aTagTable.put("notify_loggers", " checked ");
                aTagTable.put("mailLoggers", "true");
            } else {
                aTagTable.put("mailLoggers", "false");
            }
        } else {
            aTagTable.put("notify_disabled", " disabled ");
            aTagTable.put("notify_loggers_disabled", "disabled");
            aTagTable.put("sendMail", "false");
            aTagTable.put("mailLoggers", "false");
        }

        String notify = paramTable.get(Field.NOTIFY) ; 
        if( null != notify && ( notify.trim().equalsIgnoreCase("true") || notify.trim().equalsIgnoreCase("checked") || notify.trim().equalsIgnoreCase("selected") || notify.trim().equalsIgnoreCase("on") || notify.trim().equalsIgnoreCase("1")) )
        {
     	   aTagTable.put(Field.NOTIFY, "checked") ;    	   
        }
        else if( null != notify ) // user/ prefiller does want to prefill notify but not as selected
        {
        	aTagTable.put(Field.NOTIFY, "") ;
        } // else the default set above should continue
        String sendMail = paramTable.get("sendMail");
        if( null != sendMail && ( sendMail.trim().equalsIgnoreCase("true") || sendMail.trim().equalsIgnoreCase("false") ) )
        {
     	   aTagTable.put("sendMail", sendMail) ;
        }
        String notify_loggers = paramTable.get(Field.NOTIFY_LOGGERS) ; 
        if( null != notify_loggers && ( notify_loggers.trim().equalsIgnoreCase("true") || notify_loggers.trim().equalsIgnoreCase("checked")))
        {
     	   aTagTable.put(Field.NOTIFY_LOGGERS, "checked") ;
        }
        String mailLoggers = paramTable.get("mailLoggers") ;
        if( null!=mailLoggers && ( mailLoggers.trim().equalsIgnoreCase("true") || mailLoggers.equalsIgnoreCase("false")))
        {
     	   aTagTable.put("mailLoggers", mailLoggers) ; 
        }       
        
        // Rendering Extended Types if any.
        //(int aSystemId, int aUserId, Hashtable<String, Integer> aPermTable, Hashtable<String, Object> aTagTable, Hashtable<String, String> paramTable,
        //HttpServletRequest req4Context, Hashtable<String, String> aReq, int caller)
        renderExtendedFields(systemId, userId, permTable, aTagTable,paramTable, aRequest, null, aCaller );

        // Check if the user has permissions to add attachments.
        addAttachmentLink(permTable, aTagTable, systemId, aCaller );

        String requestFiles = paramTable.get(REQUEST_FILES) ;
        if( null == requestFiles )
        {
        	JsonObject reqFiles = ActionHelper.getAttachmentDetailsJSONInternal(permTable, aBA, null, aUser);
        	requestFiles = reqFiles.toString() ;
        }
        aTagTable.put(REQUEST_FILES, requestFiles) ;
        //
        // Initialize draft timestamp to 0
        //
        String timeStr = paramTable.get("DTimestamp");

        if (timeStr != null) {
            try {
                long timestamp = Long.parseLong(timeStr);

                aTagTable.put("DTimestamp", Long.toString(timestamp));
            } catch (Exception e) {
                aTagTable.put("DTimestamp", "0");
            }
        } else {
            aTagTable.put("DTimestamp", "0");
        }

        //
        // List User Drafts, if any
        //
        aTagTable.put("userDraftsInfo", WebUtil.listUserDrafts(aRequest, userId, true, true));

        // Check if this is after a successful submission of a request
        String str = (String) session.getAttribute("SUCCESS_MESSAGE");

        if (str != null) {
            StringBuilder message = new StringBuilder();

            message.append("<a href=\"").append(WebUtil.getServletPath(aRequest, "/Q/")).append(sysPrefix).append("/").append(str).append("\" target=\"_blank\">").append(sysPrefix).append("#").append(str).append("</a> - Request ").append(
                " logged ").append("successfully.");
            session.setAttribute("SUCCESS_MESSAGE", null);
            aTagTable.put("successMessage", message.toString());
        } else {
            aTagTable.put("successMsgDisplay", "none");
        }

        return;
    }

    private void getParamsFromPrefill(Hashtable<String, String> paramTable,
			Hashtable<String, String> prefillTable) 
    {
    	if( null != prefillTable )
        {
	        for(Enumeration<String> keys = prefillTable.keys() ; keys.hasMoreElements() ; )
	        {
	        	String key = keys.nextElement() ;
	        	String value = prefillTable.get(key) ;
	        	// add/replace this value in existing paramtable
	        	paramTable.put(key, value) ;
	        }
        }		
	}

	private void addSummaryLink(Hashtable<String, Integer> aPermTable, Hashtable<String, Object> aTagTable, Hashtable<String, String> aParamTable, int aSystemId, int aCaller)
            throws DatabaseException, IOException, FileNotFoundException {
        Field field = null;

        if (aPermTable.get(Field.SUMMARY) != null) {
            field = Field.lookupBySystemIdAndFieldName(aSystemId, Field.SUMMARY);

            String summary = aParamTable.get(Field.SUMMARY);

            if (field != null) {
                int permission = aPermTable.get(Field.SUMMARY);

                if ((permission & Permission.ADD) != 0) {
                    StringBuilder ctrlHtml = new StringBuilder();
                    String        linkText = null;

                    if ((summary != null) && (aTagTable.get(Field.SUMMARY + "_disabled") == null)) {
                        linkText = getSummaryHtml(aCaller, aSystemId, field.getDisplayName(), summary, ctrlHtml);

                        if (!summary.equals("")) {
                            aTagTable.put("openSummary", "true");
                        }
                    } else {
                        linkText = getSummaryHtml(aCaller, aSystemId, field.getDisplayName(), "", ctrlHtml);
                    }

                    aTagTable.put("summaryLink", linkText);
                    aTagTable.put("summaryCtrl", ctrlHtml.toString());
                }
            }
        }
    }

    private void addSummaryLink(Hashtable<String, Integer> aPermTable, Hashtable<String, Object> aTagTable, Hashtable<String, String> aParamTable, Request aRequest, int aSystemId)
            throws DatabaseException, IOException, FileNotFoundException {
        Field field = null;

        if (aPermTable.get(Field.SUMMARY) != null) {
            field = Field.lookupBySystemIdAndFieldName(aSystemId, Field.SUMMARY);

            String summary = aParamTable.get("summary");

            if (field != null) {
                int permission = aPermTable.get(Field.SUMMARY);

                if ((permission & Permission.CHANGE) != 0) {
                    StringBuilder ctrlHtml = new StringBuilder();
                    String        linkText = null;

                    if ((summary != null) && (aTagTable.get(Field.SUMMARY + "_disabled") == null)) {
                        linkText = getSummaryHtml(ADD_ACTION, aSystemId, field.getDisplayName(), summary, ctrlHtml);

                        if (!summary.equals("")) {
                            aTagTable.put("openSummary", "true");
                        }
                    } else {
                    	// reseting the value if not found in the request.
                        linkText = getSummaryHtml(ADD_ACTION, aSystemId, field.getDisplayName(), "", ctrlHtml);
                    }

                    aTagTable.put("summaryLink", linkText);
                    aTagTable.put("summaryCtrl", ctrlHtml.toString());
                }
            }
        }
    }

    private void addSummaryLink(Hashtable<String, Integer> aPermTable, Hashtable<String, Object> aTagTable, String aSummary, int aSystemId, int aCaller)
            throws DatabaseException, IOException, FileNotFoundException {
        Field field = null;

        // Check if the user has permission to Change to the parent.
        if (aPermTable.get(Field.SUMMARY) != null) {
            field = Field.lookupBySystemIdAndFieldName(aSystemId, Field.SUMMARY);

            if (field != null) {
                int permission = aPermTable.get(Field.SUMMARY);

                if ((permission & Permission.CHANGE) != 0) {
                    StringBuilder ctrlHtml = new StringBuilder();
                    String        linkText = "";

                    if (aSummary != null) {
                        linkText = getSummaryHtml(aCaller, aSystemId, field.getDisplayName(), aSummary, ctrlHtml);
                    }

                    aTagTable.put("summaryLink", linkText);
                    aTagTable.put("summaryCtrl", ctrlHtml.toString());
                }
            } else {
                LOG.warn("Summary of  Field Object is NULL.");
            }
        } else {
            LOG.warn("No Entry for Summary in Permissions Table.");
        }
    }

    /**
     * This method checks for the basic permissions required to perform action
     * specified by aCaller.
     * Caller can any of the following.
     *       <UL>
     *       <LI>Add Request.
     *       <LI>Add Action.
     *       <LI>Add SubRequest.
     *       </UL>
     * The basic permissions that are checked for encompass
     *  <UL>
     *  <LI>View permission on business area.
     *  <LI>View permission on Private if Business Area is private.
     *  <LI>Add permission on Request field if the caller is not add-action.
     *      If the caller is add-action, then change permission should be
     *      checked for.
     *  <UL>
     */
    private void checkBasicPermissions(BusinessArea aBA, Hashtable<String, Integer> aPermTable, int aCaller) throws TBitsException {
        Integer temp = null;

        // View Permission On Business Area is the preliminary requirement.
        temp = aPermTable.get(Field.BUSINESS_AREA);

        if (temp != null) {
            int permValue = temp.intValue();

            if ((permValue & Permission.VIEW) == 0) {
                throw new TBitsException(Messages.getMessage("ADD_REQUEST_NO_PERMISSION", aBA.getDisplayName()));
            }
        } else {
            throw new TBitsException(Messages.getMessage("ADD_REQUEST_NO_PERMISSION", aBA.getDisplayName()));
        }

        // Check for View On IS_PRIVATE if Business Area is private.
        if (aBA.getIsPrivate() == true) {
            temp = aPermTable.get(Field.IS_PRIVATE);

            if (temp != null) {
                int permValue = temp.intValue();

                if ((permValue & Permission.VIEW) == 0) {
                    throw new TBitsException(Messages.getMessage("ADD_REQUEST_NO_PERMISSION", aBA.getDisplayName()));
                }
            } else {
                throw new TBitsException(Messages.getMessage("ADD_REQUEST_NO_PERMISSION", aBA.getDisplayName()));
            }
        }

        //
        // If the caller is add-request/add-sub-request, the add permission
        // on request field should be checked for.
        //
        if ((aCaller == ADD_REQUEST) || (aCaller == ADD_SUBREQUEST)) {

            // Check For Add Permission On Request.
            temp = aPermTable.get(Field.REQUEST);

            if (temp != null) {
                int permValue = ((Integer) temp).intValue();

                if ((permValue & Permission.ADD) == 0) {
                    throw new TBitsException(Messages.getMessage("ADD_REQUEST_NO_PERMISSION", aBA.getDisplayName()));
                }
            } else {
                throw new TBitsException(Messages.getMessage("ADD_REQUEST_NO_PERMISSION", aBA.getDisplayName()));
            }
        }

        // for add-action, change permission on request should be checked for.
        else {

            // Check For Add Permission On Request.
            temp = aPermTable.get(Field.REQUEST);

            if (temp != null) {
                int permValue = ((Integer) temp).intValue();

                if ((permValue & Permission.CHANGE) == 0) {
                    throw new TBitsException(Messages.getMessage("ADD_ACTION_NO_PERMISSION", aBA.getDisplayName()));
                }
            } else {
                throw new TBitsException(Messages.getMessage("ADD_ACTION_NO_PERMISSION", aBA.getDisplayName()));
            }
        }

        //
        // If the caller is add-sub-request, the add permission on parent
        // request field should be checked for.
        //
        if (aCaller == ADD_SUBREQUEST) {

            // Check For Add Permission On Parent Request.
            temp = aPermTable.get(Field.PARENT_REQUEST_ID);

            if (temp != null) {
                int permValue = ((Integer) temp).intValue();

                if ((permValue & Permission.ADD) == 0) {
                    throw new TBitsException(Messages.getMessage("ADD_SUB_REQUEST_NO_PERMISSION", aBA.getDisplayName()));
                }
            } else {
                throw new TBitsException(Messages.getMessage("ADD_SUB_REQUEST_NO_PERMISSION", aBA.getDisplayName()));
            }
        }
    }

    /**
     * This method fills the Tagtable with [Field-Label, Displaynames] and
     * returns the Field List.
     *
     * @param aSystemId  SystemId of the Business Area.
     * @param aCaller    The current page being accessed.
     * @param aFieldList List of all the fixed fields.
     * @param aTagTable  Hashtable containing [Field-Label, DisplayName] pairs.
     * @param aPermTable Hashtable containing [Field-Label, Permission] pairs
     * @param aDueDateDisabled boolean value indicating if the due-date field
     *                         for the business-area is disabled.
     * @param aAllowNullDueDate boolean field indicating if the business-area
     *                          allows null due dates.
     * @param aDefaultDueDate  Default due date for the business area.
     *
     */
    private void decideFieldAbility(int aSystemId, int aCaller, ArrayList<Field> aFieldList, Hashtable<String, Object> aTagTable, Hashtable<String, Integer> aPermTable, boolean aDueDateDisabled,
                                    boolean aAllowNullDueDate, long aDefaultDueDate)
            throws DatabaseException, TBitsException 
    {
    	int chkPerm = 0 ;
    	if( aCaller == ADD_REQUEST || aCaller == ADD_SUBREQUEST )
    		chkPerm = Permission.ADD ;
    	else // this is add_action
    		chkPerm = Permission.CHANGE ;
    	
        for (Field field : aFieldList) 
        {
            int    permission = 0;
            String fieldName  = field.getName();
    
            // Read the permission value for this field from the table.
            Integer temp = aPermTable.get(fieldName);

            if (temp != null) {
                permission = temp.intValue();
            }

            if (fieldName.equalsIgnoreCase(Field.DUE_DATE))  // special treatment of duedate
            {
                if (aDueDateDisabled == true) {
                    aTagTable.put("datetime_display", "none");
                }

                if ((aAllowNullDueDate == true) && (aDefaultDueDate != 0)) {
                    aTagTable.put(fieldName + "_box_display", "");
                } else {
                    aTagTable.put(fieldName + "_box_display", "none");
                }

                if ((permission & chkPerm) == 0) 
                {
                    aTagTable.put(fieldName + "_disabled", "disabled=\"true\"");
                    aTagTable.put(fieldName + "_img_display", "none");
                    aTagTable.put(fieldName + "_box_display", "none");
                    aTagTable.put("time_option_disabled", "none");
                } else 
                {
                    aTagTable.put(fieldName + "_img_display", "inline-block");
                }
            }
            else if( fieldName.equalsIgnoreCase(Field.OFFICE ) ) // special treatment of office_id
            {
            	if( (permission & chkPerm) == 0 )
            		aTagTable.put("office_id_display", "none") ;
            	else
            		aTagTable.put("office_id_display", "") ;
            }            	
           else if ((permission & chkPerm) == 0) 
           {
        	   	aTagTable.put(fieldName + "_disabled", "disabled");        	   
           }           
       }
    }

    /**
     * This method services the HTTP-Get request to this servlet.
     * Basically, it does display of the page ready for user to start filling
     * it and submit.
     *
     * @param  aRequest          the HttpServlet Request Object
     * @param  aResponse         the HttpServlet Response Object
     *
     * @exception ServeletException
     * @exception IOException
     */
    public void doGet(HttpServletRequest aRequest, HttpServletResponse aResponse) throws ServletException, IOException {
    	aResponse.setContentType("text/html; charset=UTF-8");
    	aResponse.setCharacterEncoding("UTF-8");
    	aRequest.setCharacterEncoding("UTF-8");
    	
    	PrintWriter out     = aResponse.getWriter();
        HttpSession session = aRequest.getSession();

        Utilities.registerMDCParams(aRequest);

        try {
            long start = new Date().getTime();

            handleGetRequest(aRequest, aResponse);

            long end = new Date().getTime();

            LOG.debug("Time taken: " + (end - start));
        } catch (DatabaseException de) {
            session.setAttribute(TBitsError.EXCEPTION_OBJECT, de);
            aResponse.sendRedirect(WebUtil.getServletPath(aRequest, "/error"));

            return;
        } catch (TBitsException de) {
            session.setAttribute(TBitsError.EXCEPTION_OBJECT, de);
            aResponse.sendRedirect(WebUtil.getServletPath(aRequest, "/error"));

            return;
        } finally {
            Utilities.clearMDCParams();
        }
    }

    /**
     * The doPost method of the servlet.
     *
     * @param  aRequest          the HttpServlet Request Object
     * @param  aResponse         the HttpServlet Response Object
     *
     * @exception ServeletException
     * @exception IOException
     */
    public void doPost(HttpServletRequest aRequest, HttpServletResponse aResponse) throws ServletException, IOException {
    	aResponse.setContentType("text/html; charset=UTF-8");
    	aResponse.setCharacterEncoding("UTF-8");
    	aRequest.setCharacterEncoding("UTF-8");
    	
        PrintWriter out     = aResponse.getWriter();
        HttpSession session = aRequest.getSession();

        Utilities.registerMDCParams(aRequest);

        try {
            handlePostRequest(aRequest, aResponse);
        } catch (DatabaseException de) {
            session.setAttribute(TBitsError.EXCEPTION_OBJECT, de);
            aResponse.sendRedirect(WebUtil.getNearestPath(aRequest,"/error")) ;
            return;
        } catch (TBitsException de) {
            session.setAttribute(TBitsError.EXCEPTION_OBJECT, de);
            aResponse.sendRedirect(WebUtil.getNearestPath(aRequest,"/error")) ;
            return;
        } finally {
            Utilities.clearMDCParams();
        }
    }

    /**
     * This method populates the Tagtable with the tags and the values that
     * fill the due-date in the HTML Page.
     *
     * @param  aTagTable Table which should be filled with tags and values.
     * @param  aStrDueDate  Due Date set by user
     */
    private void fillDueDateTime(Hashtable<String, Object> aTagTable, String aStrDueDate, String dateFormat) {

        // Value of client offset.
        int clientOffset = 0;

        // Current time.
        long currentTime = System.currentTimeMillis();

        // Check if the tag table has clientOffset object.
        Integer co = (Integer) aTagTable.get("clientOffset");

        // If not present, take the current zone's offset by default.
        if (co == null) {
            clientOffset = DEFAULT_ZONE.getOffset(currentTime);
        } else {
            clientOffset = co.intValue();
        }

        TimeZone localZone = getLocalZone(clientOffset);

        // Get Gmt date for the present site time
        Timestamp temp = Timestamp.getGMTNow();

        // convert Gmt time to local time
        aTagTable.put("loggedDate", WebUtil.getDateInFormat(temp, localZone, dateFormat));//"MM/dd/yyyy HH:mm"));

        // First Token is date part in MM/dd/yyyy format.
        aTagTable.put("due_datetime", aStrDueDate);

        return;
    }

    /**
     * This method populates the Tagtable with the tags and the values that
     * fill the due-date in the HTML Page.
     *
     * @param aTagTable Table which should be filled with tags and values.
     * @param aAdd      Default Due Date for the Business Area.
     * @param isTimeDisabled True if time is disabled.
     */
    private void fillDueDateTime(Hashtable<String, Object> aTagTable, int aAdd, boolean isTimeDisabled, String dateFormat) {

        // Always Site's Time is printed.
        Calendar now          = Calendar.getInstance();
        int      clientOffset = ((Integer) aTagTable.get("clientOffset")).intValue();
        TimeZone localZone    = getLocalZone(clientOffset);

        // Get Gmt date for the present site time
        Timestamp temp = getGmtDate((Timestamp.getTimestamp(now.getTime())).toCustomFormat("MM/dd/yyyy hh:mm a"), TimeZone.getDefault(), "MM/dd/yyyy hh:mm a");

        // convert Gmt time to local time
        aTagTable.put("loggedDate", WebUtil.getDateInFormat(temp, localZone, "MM/dd/yyyy HH:mm"));

        if (isTimeDisabled == false) {

            // Add the default due date of BA to the current time.
            now.add(Calendar.MINUTE, aAdd);
        }

        // Get Gmt time for default BA due date
        temp = getGmtDate((Timestamp.getTimestamp(now.getTime())).toCustomFormat("MM/dd/yyyy hh:mm a"), TimeZone.getDefault(), "MM/dd/yyyy hh:mm a");

        // convert Gmt to local time
        if (isTimeDisabled == false) {
            String today = WebUtil.getDateInFormat(temp, TimeZone.getDefault(), dateFormat);//"MM/dd/yyyy HH:mm");

            aTagTable.put("due_datetime", today);
        } else {
            String today = WebUtil.getDateInFormat(temp, TimeZone.getDefault(), dateFormat);//"MM/dd/yyyy");

            aTagTable.put("due_datetime", today);
        }

        return;
    }

    /**
     * This method populates the Tagtable with the tags and the values that
     * fill the due-date in the HTML Page.
     *
     * @param  aTagTable Table which should be filled with tags and values.
     * @param  aLoggedDateTS Logged Date Timestamp.
     * @param  aDueDateTS Due Date Timestamp
     * @param format 
     */
    private void fillDueDateTime(Hashtable<String, Object> aTagTable, Timestamp aLoggedDateTS, Timestamp aDueDateTS, boolean aTimeDisabled, String format) {
        int      clientOffset = ((Integer) aTagTable.get("clientOffset")).intValue();
        TimeZone localZone    = getLocalZone(clientOffset);
        
        //String format = "MM/dd/yyyy HH:mm";
        DateFormat df = new SimpleDateFormat(format);
        df.setTimeZone(localZone);
        
        // convert Gmt time to local time
        //aTagTable.put("loggedDate", WebUtil.getDateInFormat(aLoggedDateTS, localZone, format));
        aTagTable.put("loggedDate", df.format(aLoggedDateTS));
        
        if (aDueDateTS != null) {

            // convert Gmt to local time
            String dueDate = null;

            //dueDate = WebUtil.getDateInFormat(aDueDateTS, TimeZone.getDefault(), format);
            dueDate = df.format(aDueDateTS);            
            
            /*if (!isOnlyDateFormat(format)){
            	String[] dueDateSplit = dueDate.split(" ");
            	if (dueDateSplit[1].equals("23:59:00")) {
            		dueDate = dueDateSplit[0];
            		aTagTable.put("disableTime", "Enable Time");
            		aTagTable.put("due_time_disabled", "true");
            	} else {
            		aTagTable.put("disableTime", "Disable Time");
            		aTagTable.put("due_time_disabled", "false");
            	}
            }*/
            aTagTable.put("disableTime", "Disable Time");
    		aTagTable.put("due_time_disabled", "false");
            aTagTable.put("due_datetime", dueDate);
        }

        return;
    }
    
    private boolean isOnlyDateFormat(String dateFormat){
    	if (dateFormat.equals("MMM dd, yyyy") || dateFormat.equals("yyyy-MM-dd")||
    			dateFormat.equals("dd-MMM-yyyy")|| dateFormat.equals("MM/dd/yyyy"))
    		return true;
    	return false;
    }

    /**
     * This method handles the file part which represents an attachment.
     *
     * @param fp      FilePart object.
     * @param aLogin  User Login.
     * @param counter Counter to be used in case of identical filenames.
     * @param table   Table that contains the names of attached files.
     *
     * @return Attachment Information.
     *
     * @exception IOException
     */
    private static String handleFilePart(FilePart fp, String aLogin, int counter, Hashtable<String, Boolean> table) throws IOException {
        StringBuilder attList = new StringBuilder();

        // Get the name and the path of the file.
        String fileName = fp.getFileName();
        String filePath = fp.getFilePath();

        //
        // If the filename is null, then there was an empty file
        // control in the form. So, skip this.
        //
        if (fileName == null) {
            return attList.toString();
        }

        LOG.info("File Name: " + fileName);
        LOG.info("File Path: " + filePath);

        String displayName = fileName;
        String storedName  = fileName.replaceAll("[^a-zA-Z0-9._]", "_");

        if (table.get(storedName) != null) {
            storedName = counter + "_" + storedName;
        }

        // Store the filename in the
        table.put(storedName, true);
        
        int dotIndex = storedName.lastIndexOf(".");
        String prefix = null;
        String suffix = null;
        if(dotIndex > -1)
        {
	        prefix = aLogin + "-" + storedName.substring(0, dotIndex);
	        suffix = storedName.substring(dotIndex + 1);
        }
        else
        {
        	prefix = aLogin + "-" + storedName;
        	suffix = null;
        }
        if(prefix.length() < 3)
        {
        	prefix = "tmp_" + prefix;
        }
        File ourTmpLocationDir = new File(APIUtil.getTMPDir());
        File targetFile = File.createTempFile(prefix, suffix, ourTmpLocationDir);

        // Store the file content to the target location.
        
        fp.writeTo(targetFile);
        attList.append("\n").append(targetFile.getAbsolutePath()).append("\t").append(displayName);
        return attList.toString();
    }

    /**
     * Method that actually handles the Get Request.
     *
     *
     * @param aRequest          the HttpServlet Request Object
     * @param aResponse         the HttpServlet Response Object
     * @exception ServletException
     * @exception IOException
     * @exception TBitsExceptionception DatabaseException
     * @exception FileNotFoundException
     */
    private void handleGetRequest(HttpServletRequest aRequest, HttpServletResponse aResponse) throws ServletException, IOException, TBitsException, DatabaseException, FileNotFoundException {
    	
        PrintWriter out     = aResponse.getWriter();
        HttpSession session = aRequest.getSession();
        LOG.info("AddHtmlRequest.handleGetRequest called.") ;
        //
        // Steps followed while servicing a Get Request to this page.
        // 1. Validate the user.
        //
        // 2. Identify the caller.
        //
        // 3. Get the request params and thereby the BusinessArea
        //
        // 4. Get the client offset.
        //
        // 5. If caller is Add-Request, then
        //
        // (i) Get the permissions table.
        //
        // (ii) Check the Basic permissions requrired to add a request.
        // - View on BA.
        // - View on Confidential If BA is private.
        // - Add on Request.
        //
        // (iii) Render the fixed fields.
        // - Replace the labels with the corresponding display names.
        // - Replace the values with the field defaults.
        //
        // (iv) Render the extended fields.
        // - Get the extended fields segregated by their data types.
        // - Generate the HTML to display the control corresponding
        // to each field.
        //
        // (v) Replace all the DTags and write the final HTML into servlet
        // output stream.
        //
        // (vi) Return.
        //
        // 6. If the caller is Add Action, then
        //
        // (i) Get the the Request object.
        //
        // (ii) Get the permissions table.
        //
        // (iii) Check the Basic permissions requrired to update a request.
        // - View on BA.
        // - View on Confidential If BA is private.
        // - Change on Request.
        //
        // (iv) Render the fixed fields.
        // - Replace the labels with the corresponding display names.
        // - Replace the values with the previous values from Request.
        //
        // (v) Render the extended fields.
        // - Get the extended fields segregated by their data types.
        // - Generate the HTML to display the control corresponding
        // to each field with the value present in the Request.
        //
        // (vi) Replace all the DTags and write the final HTML into servlet
        // output stream.
        //
        // (vii) Return.
        //
        // 7. If the caller is Add Sub Request, then
        //
        // (i) Get the Request object.
        //
        // (ii) Get the permissions table.
        //
        // (iii) Check the Basic permissions requrired to add a sub-request.
        // - View on BA.
        // - View on Confidential If BA is private.
        // - Add on Request.
        // - Add on Parent Request Id.
        //
        // (iv) Render the fixed fields.
        // - Replace the labels with the corresponding display names.
        // - Replace the values with the previous values from Request.
        //
        // (v) Render the extended fields.
        // - Get the extended fields segregated by their data types.
        // - Generate the HTML to display the control corresponding
        // to each field with the value present in the Request.
        //
        // (vi) Replace all the DTags and write the final HTML into servlet
        // output stream.
        //
        // (vii) Return.
        //
        // -------
        // Step 1: Validate the User.
        // -------
        User      user       = WebUtil.validateUser(aRequest);
        WebConfig userConfig = user.getWebConfigObject();
        int       userId     = user.getUserId();

        // -------
        // Step 2: Identify the caller.
        // -------
        int caller = getCaller(aRequest.getRequestURI());

        // -------
        // Step 3: Get the request parameters.
        // -------
        Hashtable<String, Object> paramInfo = WebUtil.getRequestParams(aRequest, userConfig, caller);
        BusinessArea              ba        = (BusinessArea) paramInfo.get(Field.BUSINESS_AREA);
        int                       systemId  = ba.getSystemId();
        String                    sysPrefix = ba.getSystemPrefix();
        SysConfig                 sc        = ba.getSysConfigObject();

        //
        // redirect to inline update request form.
        //
        if (caller == ADD_ACTION) {
            Request req    = (Request) paramInfo.get(Field.REQUEST);
            String  url    = WebUtil.getServletPath(aRequest, "/q/") + sysPrefix + "/" + req.getRequestId() + "?u=1";
            String  drafts = aRequest.getParameter("drafts");

            if ((drafts != null) && (drafts.equalsIgnoreCase("true") == true)) {
                url = url + "&drafts=true";
            }

            String dTimestamp = aRequest.getParameter("DTimestamp");

            if (dTimestamp != null) {
                url = url + "&DTimestamp=" + dTimestamp + "&timeStamp=" + dTimestamp;
            }

            String draftid = aRequest.getParameter("draftid");

            if (draftid != null) {
                url = url + "&draftid=" + draftid;
            }
            
            String sysId = aRequest.getParameter("sys_id");

            if (sysId != null) {
                url = url + "&sys_id=" + sysId;
            }

            String reqId = aRequest.getParameter("request_id");

            if (reqId != null) {
                url = url + "&request_id=" + reqId;
            }

            aResponse.sendRedirect(url);


            return;
        }

        Request req = null ;
        //
        // Don't allow subRequests for Transferred requests.
        //
        if (caller == ADD_SUBREQUEST) {
            req = (Request) paramInfo.get(Field.REQUEST);
            TransferredRequest tr  = TransferredRequest.lookupBySourcePrefixAndRequestId(ba.getSystemPrefix(), req.getRequestId());

            if (tr != null) {
                throw new TBitsException(Messages.getMessage("SUB_REQUEST_NOT_ALLOWED"));
            }
        }

        // -------
        // Step 4: Get the client offset.
        // -------
        int clientOffset = WebUtil.getClientOffset(aRequest, aResponse);

        if (clientOffset == -1) {

            //
            // Redirect to the browser to get the clientOffset as a request
            // parameter.
            //
            String url = "";

            switch (caller) {
            case ADD_REQUEST :
                url = "/add-request/" + sysPrefix;

                break;

            case ADD_SUBREQUEST :
                Request req1 = (Request) paramInfo.get(Field.REQUEST);

                url = "/add-subrequest/" + sysPrefix + "/" + req1.getRequestId();

                break;

            default :
                url = "/add-request/";

                break;
            }

            out.println(WebUtil.getRedirectionHtml(aRequest, url));

            return;
        }

        // Tag Table contains all the [tag_name, value] pairs.
        Hashtable<String, Object> tagTable = new Hashtable<String, Object>();

        //
        // Get the TimeZone based on the user's preferred zone. Also pass the
        // clientoffset which will be used incase the user opted for Browser
        // time
        //
        int      zone          = userConfig.getPreferredZone();
        TimeZone preferredZone = WebUtil.getPreferredZone(zone, clientOffset);

        tagTable.put("sysPrefix", sysPrefix);
        tagTable.put("sysName", ba.getDisplayName());
        tagTable.put("preferredZone", preferredZone);
        tagTable.put("clientOffset", new Integer(clientOffset));
        setInstanceBold(aRequest, tagTable, sysPrefix);

        //
        // Now we need to render the appropriate values depending upon the
        // caller of the servlet.
        //
        tagTable.put("caller", Integer.toString(caller));
        
        //
        // Now separate the flow for add-request,add-action and add-subrequest.
        // Except mail options,replace the tags in tag table by default values
        // for add-request and add-subrequest and current request values for
        // add-action.
        //
        Hashtable<String, String> paramTable = new Hashtable<String, String>();

        HashMap<String, String> captionsHash = CaptionsProps.getInstance().getCaptionsHashMap(systemId);
        SlotFillerFactory euf = SlotFillerFactory.getInstance() ;
        switch (caller) {
        case ADD_REQUEST :
        	addRequestForm(ADD_REQUEST, aRequest, tagTable, paramInfo, ba, user, paramTable);
        	// call the SlotFillerFactory's for appropriate plugins for ADD_REQUEST            
    		euf.runAddRequestSlotFillers(aRequest, aResponse, ba, user, tagTable) ;
            tagTable.put("title", captionsHash.get(CaptionsProps.CAPTIONS_VIEW_ADD_REQUEST));
            tagTable.put("action_subject_display", "none");

            break;

        case ADD_ACTION :
            addActionForm(aRequest, tagTable, paramInfo, null, ba, user, paramTable);
            tagTable.put("title", captionsHash.get(CaptionsProps.CAPTIONS_VIEW_UPDATE_REQUEST));
            
            break;

        case ADD_SUBREQUEST :
            addRequestForm(ADD_SUBREQUEST, aRequest, tagTable, paramInfo, ba, user, paramTable);
        	// call the SlotFillerFactory's for appropriate plugins for ADD_REQUEST 
            euf.runSubRequestSlotFillers(aRequest, aResponse, ba, req, user, tagTable) ;
            tagTable.put("title", captionsHash.get(CaptionsProps.CAPTIONS_VIEW_ADD_SUBREQUEST));
            tagTable.put("action_subject_display", "none");

            break;
        }

        tagTable.put("autoSaveRate", getAutoSaveRate(userConfig));
        tagTable.put("nearestPath", WebUtil.getNearestPath(aRequest, ""));
        tagTable.put("cssFile", WebUtil.getCSSFile(sc.getWebStylesheet(), sysPrefix, false));
        tagTable.put("openExtended", "true");
        //Added to handle pre-filling requests
        tagList.add("prefillData");
        tagTable.put("prefillData","");        
        
        runPreRenders(aRequest, aResponse, tagTable, tagList);
        //
        // Finally read the Html Interface into the parser and replace the tags
        // and print it out.
        // 
        String YUIJss = ActionHelper.getResourceString(myYUIJSs, WebUtil.getNearestPath(aRequest, "")) ;
    //    LOG.info("YUIjss ="+YUIJss) ;
        String tbitsJss = ActionHelper.getResourceString(myTbitsJSs, WebUtil.getNearestPath(aRequest, "")) ;
     //   LOG.info("tBitsjss ="+tbitsJss) ;
//        String reqCss = ActionHelper.getResourceString(myAddReqCSSs, WebUtil.getNearestPath(aRequest, ""));
//        LOG.info("reqCss = " + reqCss) ;
//        String xAddReqCSSs = ActionHelper.getResourceString(myAddReqCSSs, WebUtil.getNearestPath(aRequest, "")) ;
//        LOG.info("myAddReqCSSs ="+xAddReqCSSs) ;
//        tagTable.put("myAddReqCSSs", xAddReqCSSs ) ;
        tagTable.put("YUIJSs", YUIJss) ;
        tagTable.put("tbitsJSs", tbitsJss ) ;
//        tagTable.put("reqCss", reqCss);
        
        DTagReplacer hp = new DTagReplacer(HTML_MAIN);     
        replaceTags(hp, tagTable);
        out.println(hp.parse(systemId));
    }

	/**
	 * This methods runs various preLoader. It essentials is run just before rendering.
	 * @param aRequest
	 * @param aResponse
	 * @param tagTable
	 * @throws TBitsException
	 */
	private void runPreRenders(HttpServletRequest aRequest,
			HttpServletResponse aResponse, Hashtable<String, Object> tagTable, ArrayList<String>tagList)
			throws TBitsException {
		PluginManager pm = PluginManager.getInstance();
        ArrayList<Class> preRendererClasses = pm.findPluginsByInterface(IPreRenderer.class.getName());
        ArrayList<IPreRenderer> preLoaders = new ArrayList<IPreRenderer>();
        if(preRendererClasses != null)
        {
        	for(Class preRendererClass:preRendererClasses)
        	{
        		IPreRenderer preRenderer;
				try {
					preRenderer = (IPreRenderer) preRendererClass.newInstance();
					preLoaders.add(preRenderer);
				} catch (InstantiationException e) {
					LOG.error("Could not instantiate the pre renderer class: " + preRendererClass.getClass().getName());
				} catch (IllegalAccessException e) {
					// TODO Auto-generated catch block
					LOG.error("Could not access the renderer class: " + preRendererClass.getClass().getName());
				}
        	}
        }
        
        Comparator<IPreRenderer> c = new Comparator<IPreRenderer>(){

			public int compare(IPreRenderer arg0, IPreRenderer arg1) {
				double diff = arg0.getSequence() - arg1.getSequence();
				if(diff > 0)
					return 1;
				else if(diff == 0)
					return 0;
				else 
					return -1;
			}
		};
		Collections.sort(preLoaders, c);
		
        for(IPreRenderer preLoader:preLoaders)
        {
        	preLoader.process(aRequest, aResponse, tagTable, tagList);
        }
	}

    /**
     * Method that actually handles the Post Request.
     *
     *
     * @param aRequest          the HttpServlet Request Object
     * @param aResponse         the HttpServlet Response Object
     * @exception ServeletException
     * @exception IOException
     * @exception TBitsExceptionxception DatabaseException
     * @exception FileNotFoundException
     */
    public void handlePostRequest(HttpServletRequest aRequest, HttpServletResponse aResponse) throws ServletException, IOException, TBitsException, DatabaseException, FileNotFoundException {
    	
        PrintWriter out     = aResponse.getWriter();
        HttpSession session = aRequest.getSession();

        // Hashtable for storing (tag,replacement) pairs
        Hashtable<String, Object> tagTable = new Hashtable<String, Object>();

        // Hashtable containing Fieldname,value pairs for adding a request.
        // We shall be adding these fields to the hashtable if their value
        // is not null.
        Hashtable<String, String> request = new Hashtable<String, String>();

        // This contains completed euc failure log of all types,
        // mailed to only logs
        ArrayList<String> aFailedEUCs = new ArrayList<String>();

        // All these EUCs arraylist are used to display proper
        // euc errors to users.
        // This contains list of all user specified eucs
        ArrayList<String> aAllEUCs = new ArrayList<String>();

        // This contains all wrong descriptor eucs
        // popultaed while parsing eucs.
        ArrayList<String> aBadDescriptorEUCs = new ArrayList<String>();

        // This contains eucs not allowed on field,
        // populated while parsing eucs.
        ArrayList<String> aNotAllowedEUCs = new ArrayList<String>();

        // This contains all wrong descriptor eucs populated
        // while validating eucs.
        ArrayList<String> aBadSyntaxEUCs = new ArrayList<String>();

        // This contains eucs not allowed on private requests,
        // populated while parsing and validating eucs.
        ArrayList<String> aNotAllowedPrivateEUCs = new ArrayList<String>();

        // This contains all wrong operand eucs populated from API's
        // thrown exceptions.
        ArrayList<String> aBadOperandEUCs = new ArrayList<String>();

        // This contains all eucs for which user doesn't have permissions,
        // populated from API's thrown exceptions.
        ArrayList<String> aNoPermissionEUCs = new ArrayList<String>();

        // Validate the user, get the user Object, Read the configuration.
        User user = null;

        user = WebUtil.validateUser(aRequest);

        WebConfig userConfig = user.getWebConfigObject();

        // User Id.
        int                       userId     = user.getUserId();
        String                    userLogin  = user.getUserLogin();
        Hashtable<String, String> paramTable = new Hashtable<String, String>();

        getParamTable(aRequest, userLogin, paramTable);
        
        
        //
        // Now we need to render the appropriate values depending upon
        // The caller of the servlet.
        //
        String requestURL  = aRequest.getRequestURI();
        int    caller      = getCaller(aRequest.getRequestURI());
        String redirection = paramTable.get("redirection");

        if ((redirection != null) && (redirection.trim().equalsIgnoreCase("true"))) {
            handleGetRequest(aRequest, aResponse);

            return;
        }

        //
        // Call the WebUtil's getParamInfo, which reads the pathInfo and
        // get the corresponding BA object.
        //
        Hashtable<String, Object> paramInfo        = WebUtil.getRequestParams(aRequest, userConfig, caller);
        BusinessArea              ba               = (BusinessArea) paramInfo.get(Field.BUSINESS_AREA);
        SysConfig                 sc               = ba.getSysConfigObject();
        boolean                   allowNullDueDate = sc.getAllowNullDueDate();
        boolean                   isTimeDisabled   = sc.getIsTimeDisabled();
        boolean                   duedateDisabled  = sc.getIsDueDateDisabled();
        int                       systemId         = ba.getSystemId();
        String                    sysPrefix        = ba.getSystemPrefix();

        tagTable.put("systemId", Integer.toString(systemId));
        tagTable.put("sysPrefix", sysPrefix);
        tagTable.put("sysName", ba.getDisplayName());
        tagTable.put("attachFrmSrc", "?systemId=" + systemId);
        tagTable.put("sysPrefixLabel", ba.getDisplayName() + " [" + sysPrefix + "]");
        
        setInstanceBold(aRequest, tagTable, sysPrefix);

        //
        // Check if we can get the clientOffset in some way, i.e. either
        // from cookies or as a request parameter or a request attribute.
        //
        int clientOffset = WebUtil.getClientOffset(aRequest, aResponse);

        if (clientOffset == -1) {

            //
            // Redirect to the browser to get the clientOffset as a request
            // parameter.
            //
            String url = "";

            switch (caller) {
            case ADD_REQUEST :
                url = "/add-request/" + sysPrefix;

                break;

            case ADD_SUBREQUEST :
                Request req1 = (Request) paramInfo.get(Field.REQUEST);

                url = "/add-subrequest/" + sysPrefix + "/" + req1.getRequestId();

                break;

            default :
                url = "/add-request/";

                break;
            }

            out.println(WebUtil.getRedirectionHtml(aRequest, url));

            return;
        }

        Request req       = null;
        int     requestId = 0;

        // Get the permission table for this user.
        Hashtable<String, Integer> permTable = null;

        if (caller == ADD_ACTION) {
            req       = (Request) paramInfo.get(Field.REQUEST);
            requestId = req.getRequestId();
            request.put(Field.REQUEST, "" + requestId);
            permTable = RolePermission.getPermissionsBySystemIdAndRequestIdAndUserId(systemId, requestId, userId);

            String repliedToAction = paramTable.get("repliedToAction");

            if ((null != repliedToAction) && (true != repliedToAction.trim().equals(""))) {
                request.put(Field.REPLIED_TO_ACTION, repliedToAction);
            }
        } else {

            // Adding Parent Request id if add-subrequest is the caller
            if (caller == ADD_SUBREQUEST) {
                req       = (Request) paramInfo.get(Field.REQUEST);
                requestId = req.getRequestId();
                request.put(Field.PARENT_REQUEST_ID, "" + requestId);
                paramInfo.put(Field.PARENT_REQUEST_ID, "" + requestId);
            }

            permTable = RolePermission.getPermissionsBySystemIdAndUserId(systemId, userId);
        }

        // Add userLogin to the request Hashtable.
        request.put(Field.USER, userLogin);

        // Check the basic permissions required to allow a user to come to
        // this page.
        checkBasicPermissions(ba, permTable, caller);

        //
        // Get the TimeZone based on the user's preferred zone. Also pass the
        // clientoffset which will be used incase the user opted for Browser
        // time
        //
        TimeZone preferredZone = WebUtil.getPreferredZone(userConfig.getPreferredZone(), clientOffset);

        tagTable.put("preferredZone", preferredZone);
        tagTable.put("clientOffset", new Integer(clientOffset));

        // Mandatory fields for adding a request.
        // System id.
        request.put(Field.BUSINESS_AREA, Integer.toString(systemId));

        Request currentRequest = null;

        if (caller == ADD_ACTION) {
            currentRequest = (Request) paramInfo.get(Field.REQUEST);
        }

        // Read the standard Type values from the request object.
        // Category
        String strCategory = paramTable.get("category_id");

        if (strCategory != null) {
            request.put(Field.CATEGORY, strCategory);
        } else if (caller == ADD_ACTION) {
            strCategory = currentRequest.get(Field.CATEGORY);
        }

        // Severity
        String strSeverity = paramTable.get("severity_id");

        if (strSeverity != null) {
            request.put(Field.SEVERITY, strSeverity);
        } else if (caller == ADD_ACTION) {
            strSeverity = currentRequest.get(Field.SEVERITY);
        }

        // Status
        String strStatus = paramTable.get("status_id");

        if (strStatus != null) {
            request.put(Field.STATUS, strStatus);
        } else if (caller == ADD_ACTION) {
            strStatus = currentRequest.get(Field.STATUS);
        }

        // RequestType
        String strType = paramTable.get("request_type_id");

        if (strType != null) {
            request.put(Field.REQUEST_TYPE, strType);
        } else if (caller == ADD_ACTION) {
            strType = currentRequest.get(Field.REQUEST_TYPE);
        }

        // Office
        String strOffice = paramTable.get("office_id");

        if (strOffice != null) {
            request.put(Field.OFFICE, strOffice);
        } else if (caller == ADD_ACTION) {
            strOffice = currentRequest.get(Field.OFFICE);
        }

        //
        // Read user fields.Add it to the request hashtable only
        // if the value is not null
        //
        // LoggerId
        String strLogger = paramTable.get("logger_ids");

        if ((strLogger != null) && (strLogger.trim().equals("") == false)) {
            request.put(Field.LOGGER, strLogger.trim());
            tagTable.put(Field.LOGGER, Utilities.htmlEncode(strLogger));
        } else if ((strLogger == null) && (caller == ADD_REQUEST)) {
            tagTable.put(Field.LOGGER, userLogin);
        }

        if ((strLogger == null) && (caller == ADD_ACTION)) {
            strLogger = currentRequest.get(Field.LOGGER);

            if (strLogger != null) {
                tagTable.put(Field.LOGGER, Utilities.htmlEncode(strLogger));
            }
        }

        // AssigneeId
        String strAssignee = paramTable.get("assignee_ids");

        if ((strAssignee != null) && (strAssignee.trim().equals("") == false)) {
            request.put(Field.ASSIGNEE, strAssignee.trim());
            tagTable.put(Field.ASSIGNEE, Utilities.htmlEncode(strAssignee));
        }

        if ((strAssignee != null) && (strAssignee.trim().equals("") == true) && (caller == ADD_ACTION)) {
            request.put(Field.ASSIGNEE, "");
            tagTable.put(Field.ASSIGNEE, "");
        }

        if ((strAssignee == null) && (caller == ADD_ACTION)) {
            strAssignee = currentRequest.get(Field.ASSIGNEE);

            if (strAssignee != null) {
                tagTable.put(Field.ASSIGNEE, Utilities.htmlEncode(strAssignee));
            }
        }

        // Subscriber
        String strSubscriber = paramTable.get("subscriber_ids");

        if ((strSubscriber != null) && (strSubscriber.trim().equals("") == false)) {
            request.put(Field.SUBSCRIBER, strSubscriber.trim());
            tagTable.put(Field.SUBSCRIBER, Utilities.htmlEncode(strSubscriber));
        }

        if ((strSubscriber != null) && (strSubscriber.trim().equals("") == true) && (caller == ADD_ACTION)) {
            request.put(Field.SUBSCRIBER, "");
            tagTable.put(Field.SUBSCRIBER, "");
        }

        if ((strSubscriber == null) && (caller == ADD_ACTION)) {
            strSubscriber = currentRequest.get(Field.SUBSCRIBER);

            if (strSubscriber != null) {
                tagTable.put(Field.SUBSCRIBER, Utilities.htmlEncode(strSubscriber));
            }
        }

        // Cc
        String strCc = paramTable.get("cc_ids");

        if ((strCc != null) && (strCc.trim().equals("") == false)) {
            request.put(Field.CC, strCc.trim());
            tagTable.put(Field.CC, Utilities.htmlEncode(strCc));
        }

        if ((strCc == null) && (caller == ADD_ACTION)) {
            strCc = currentRequest.get(Field.CC);

            if (strCc != null) {
                tagTable.put(Field.CC, Utilities.htmlEncode(strCc));
            }
        }

        // Get Subject, Description and Summary
        // Subject
        String strSubject = paramTable.get("subject");

        if ((strSubject == null) && ((caller == ADD_REQUEST) || (caller == ADD_SUBREQUEST))) {
            strSubject = "";
        }

        if (strSubject != null) {
            request.put(Field.SUBJECT, strSubject);
            tagTable.put(Field.SUBJECT, Utilities.htmlEncode(strSubject));
        }

        if ((strSubject == null) && (caller == ADD_ACTION)) {
            strSubject = currentRequest.get(Field.SUBJECT);
            tagTable.put(Field.SUBJECT, Utilities.htmlEncode(strSubject));
        }

        // Description
        String strDescription = paramTable.get("description");

        if ((strDescription == null) || strDescription.trim().equals("")) {
            strDescription = "";
        }

        //
        // Parse IUCs from description
        //
        Hashtable<Field, String> aIUCMetaData = new Hashtable<Field, String>();
        boolean                  isAppend     = (caller == ADD_ACTION)
                ? true
                : false;
        String                   description  = IUCValidator.parseEUCMetaData(strDescription, aIUCMetaData, isAppend, false, false, ba, aFailedEUCs, aAllEUCs, aBadDescriptorEUCs, aNotAllowedEUCs,
                                                    aNotAllowedPrivateEUCs);

        request.put(Field.DESCRIPTION, description);
        tagTable.put(Field.DESCRIPTION, Utilities.htmlEncode(strDescription));
        
    
        String strDescriptionContentType = paramTable.get("description_content_type");

        if ((strDescriptionContentType == null) || strDescriptionContentType.trim().equals("")) {
        	strDescriptionContentType = "";
        }
        
        String descriptionContentType = CONTENT_TYPE_HTML + "";
        if(strDescriptionContentType.equals("text"))
        	descriptionContentType = CONTENT_TYPE_TEXT + "";
        
        request.put(Field.DESCRIPTION + "_content_type", descriptionContentType);
        
        // Summary
        String strSummary = paramTable.get("summary");

        if (strSummary != null) {
            if ((caller == ADD_ACTION) && strSummary.trim().equals("")) {
                currentRequest = (Request) paramInfo.get(Field.REQUEST);

                String previousSummary = currentRequest.get(Field.SUMMARY);

                if ((previousSummary != null) && (previousSummary.trim().equals("") == false)) {
                    request.put(Field.SUMMARY, strSummary);
                }
            } else {
                request.put(Field.SUMMARY, strSummary);
            }

            tagTable.put(Field.SUMMARY, Utilities.htmlEncode(strSummary));
        }

        if ((strSummary == null) && (caller == ADD_ACTION)) {
            currentRequest = (Request) paramInfo.get(Field.REQUEST);
            strSummary     = currentRequest.get(Field.SUMMARY);
            tagTable.put(Field.SUMMARY, Utilities.htmlEncode(strSummary));
        }
        
        String strSummaryContentType = paramTable.get("summary_content_type");

        if ((strSummaryContentType == null) || strSummaryContentType.trim().equals("")) {
        	strSummaryContentType = "";
        }
        
        String summaryContentType = CONTENT_TYPE_HTML + "";
        if(strSummaryContentType.equals("text"))
        	summaryContentType = CONTENT_TYPE_TEXT + "";
        
        request.put(Field.SUMMARY + "_content_type", summaryContentType);

        // Related Requests
        String strRelatedRequests = paramTable.get(Field.RELATED_REQUESTS);

        if (strRelatedRequests != null) {
            request.put(Field.RELATED_REQUESTS, strRelatedRequests);
            tagTable.put(Field.RELATED_REQUESTS, strRelatedRequests);
        }

        if ((strRelatedRequests == null) && (caller == ADD_ACTION)) {
            strRelatedRequests = currentRequest.getRelatedRequests();
            tagTable.put(Field.RELATED_REQUESTS, Utilities.htmlEncode(strRelatedRequests));
        }

        // Parent Request
        String strParentRequest = paramTable.get(Field.PARENT_REQUEST_ID);

        if (strParentRequest != null) {
            request.put(Field.PARENT_REQUEST_ID, strParentRequest);
            tagTable.put(Field.PARENT_REQUEST_ID, strParentRequest);
        }

        if ((strParentRequest == null) && (caller == ADD_ACTION)) {
            strParentRequest = "" + currentRequest.getParentRequestId();
            tagTable.put(Field.PARENT_REQUEST_ID, Utilities.htmlEncode(strParentRequest));
        }

        //Handle Attachments
        String attachList = paramTable.get(Field.ATTACHMENTS);
        if(attachList != null)
        {
        	LOG.info("The attachments are: " + attachList);
        	JsonParser jp = new JsonParser();
        	JsonObject mainObj = jp.parse(attachList).getAsJsonObject();
        	Set<Entry<String, JsonElement>> mainNode = mainObj.entrySet();
        	Iterator<Entry<String, JsonElement>> iter = mainNode.iterator();
        	while(iter.hasNext())
        	{
        		Entry<String, JsonElement> element = iter.next();
        		JsonElement filesElement = element.getValue().getAsJsonObject().get("files");
        		if(filesElement.getAsJsonArray().size() > 0)
        		request.put(element.getKey(), filesElement.toString());
        	}
        	
        }
        else
        {
        	attachList = "[]";
        	LOG.info("The attachments are null");
        }
        tagTable.put(Field.ATTACHMENTS, attachList);
        tagTable.put(REQUEST_FILES, attachList);

        // Is Private
        String  strIsPrivate = paramTable.get("is_private");
        boolean bIsPrivate   = false;

        if ((strIsPrivate == null) || strIsPrivate.trim().equals("") || strIsPrivate.trim().equals("false")) {
            bIsPrivate = false;
        } else {
            bIsPrivate   = true;
            strIsPrivate = "true";
        }

        request.put(Field.IS_PRIVATE, "" + bIsPrivate);

        // Send Mail
        boolean bSendMail = false;
        String  sendMail  = paramTable.get("sendMail");

        if ((sendMail == null) || sendMail.trim().equals("") || sendMail.trim().equals("false")) {
            bSendMail = false;
        } else {
            bSendMail = true;
        }

        if (sendMail != null) {
            request.put(Field.NOTIFY, sendMail);
        }

        // Mail Loggers
        boolean bChkLoggers = false;
        String  chkLoggers  = paramTable.get("mailLoggers");

        if ((chkLoggers == null) || chkLoggers.trim().equals("") || chkLoggers.trim().equals("false")) {
            bChkLoggers = false;
        } else {
            bChkLoggers = true;
        }

        if (chkLoggers != null) {
            request.put(Field.NOTIFY_LOGGERS, chkLoggers);
        }

        // Due Date
        // Get the Timestamp object for due date in GMT.
        // Due date related fields.
        String    strDueDate  = paramTable.get("due_datetime");
        Timestamp dueDateTime = null;

        if (strDueDate != null) {
            if (strDueDate.trim().equals("") == true) {
                request.put(Field.DUE_DATE, "");
            }
            
            String format = parseHTMLDate(strDueDate, user);
            
            request.put(Field.DUE_DATE, format);
            tagTable.put(Field.DUE_DATE, strDueDate);
        }

        if ((strDueDate == null) && (caller == ADD_REQUEST)) {
            if (allowNullDueDate == true) {
                tagTable.put("due_date_option", "true");
            } else {
                tagTable.put("due_date_option", "false");
            }

            if ((allowNullDueDate == false) || (sc.getDefaultDueDate() != 0)) {
                fillDueDateTime(tagTable, (int) sc.getDefaultDueDate(), isTimeDisabled, userConfig.getWebDateFormat());
            } else {
                tagTable.put("due_datetime", "");
                tagTable.put("due_datetime_box_display", "none");
            }
        }

        if (isTimeDisabled == true) {
            tagTable.put("disableTime", "Enable Time");
        } else {
            tagTable.put("disableTime", "Disable Time");
        }

        if ((strDueDate == null) && (caller == ADD_ACTION) && (currentRequest.getDueDate() != null)) {
            TimeZone localZone = getLocalZone(clientOffset);

            // convert Gmt to local time
            strDueDate = WebUtil.getDateInFormat(currentRequest.getDueDate(), localZone, userConfig.getWebDateFormat());//"MM/dd/yyyy HH:mm");
        }

        //
        // The Request ID will be zero until the request is successfully
        // inserted into the database.
        //
        requestId = 0;

        // Now get extended Fields.
              
       renderExtendedFields(systemId, userId, permTable, tagTable, paramTable, aRequest, request, caller );
       
       boolean viewPrivate = false;

        if (permTable.get(Field.IS_PRIVATE) != null) {
            int permValue = (permTable.get(Field.IS_PRIVATE)).intValue();

            if ((permValue & Permission.VIEW) == 0) {
                viewPrivate = false;
            } else {
                viewPrivate = true;
            }
        }

        //Handle extended Dates
        // extended dates are already handled in renderExtendedFields. What is this loop doing here.
//        ArrayList<Field> extDateFields = Field.getExtendedFields(systemId).get("__Datetime__");
//        if(extDateFields != null)
//        {
//        	for(Field f:extDateFields)
//        	{
//        		String strDate = paramTable.get(f.getName());
//        		String apiFormatDate = "";
//        		if(strDate != null)
//        		{
//                   apiFormatDate = parseHTMLDate(strDate);
//        		}
//        		System.out.println("Formatted the date '" + strDate + "' into : '" + apiFormatDate + "' for field '" + f.getName() + "'");
//        		request.put(f.getName(), apiFormatDate);
//        	}
//        }
        
        //
        // Now add all these values to the TagTable to rerender the page
        // if there are exceptions. The list of BAs the user can view.
        //
        tagTable.put("sys_id_list", getBAList(systemId, userId));
        ArrayList<BusinessArea> baList = BusinessArea.getBusinessAreasByUserId(userId);
        tagTable.put("baJsonArray", BAMenu.getBAMenuJsonArray(baList).toString());
        tagTable.put("baMenuList", BAMenu.getJsonArrayOfAllBAMenus().toString());
  
        if ((caller == ADD_ACTION) || (caller == ADD_SUBREQUEST)) {
            tagTable.put("sys_id_list_disabled", "disabled");
        }

        // Renders the Categories, Statuses, Severities and Request Types.
        renderFixedTypes(systemId, viewPrivate, tagTable, paramTable, WebUtil.ADD_REQUEST) ;
        //
        // Due date related fields.Render these fields only if null
        // due dates are not allowed.
        //
        if (allowNullDueDate == false) {
            if (((strDueDate == null) || strDueDate.trim().equals(""))) {
                fillDueDateTime(tagTable, (int) sc.getDefaultDueDate(), isTimeDisabled, userConfig.getWebDateFormat());
            } else {
                fillDueDateTime(tagTable, strDueDate, userConfig.getWebDateFormat());
            }
        } else {

            //
            // Due date is not mandatory
            //
            if ((strDueDate != null) && (!strDueDate.trim().equals(""))) {
                fillDueDateTime(tagTable, strDueDate, userConfig.getWebDateFormat());
            }

            /*
             * else
             * {
             *   tagTable.put("due_datetime","");
             *   tagTable.put("due_datetime" + "_box_display",
             *                "none");
             * }
             */
        }

        addAttachmentLink(permTable, tagTable, systemId, caller);

        if (strParentRequest == null) 
        {
        	if( caller == ADD_ACTION )
        		strParentRequest = req.get( Field.PARENT_REQUEST_ID ) ;
        	else if( caller == ADD_SUBREQUEST )
        		strParentRequest = req.get(Field.REQUEST);
        	
        	if( null == strParentRequest )
        		strParentRequest = "" ;
        	
        }       
        addAssociateLink(permTable, tagTable, systemId, caller, strParentRequest );
        
        addRelatedLink(permTable, tagTable, systemId, caller, strRelatedRequests );
        
        if (caller == ADD_ACTION) {       
            if (strSummary == null) {
                addSummaryLink(permTable, tagTable, paramTable, (Request) paramInfo.get(Field.REQUEST), systemId);
            } else {
                addSummaryLink(permTable, tagTable, strSummary, systemId, caller);
            }
        } else if (caller == ADD_SUBREQUEST) {
            if (strSummary == null) {
                addSummaryLink(permTable, tagTable, paramTable, systemId, caller);
            } else {
                addSummaryLink(permTable, tagTable, strSummary, systemId, caller);
            }
        } else if (caller == ADD_REQUEST) {            
            if (strSummary == null) {
                addSummaryLink(permTable, tagTable, paramTable, systemId, caller);
            } else {
                addSummaryLink(permTable, tagTable, strSummary, systemId, caller);
            }
        }

        // Mail related parameters.
        if (bSendMail == true) {
            tagTable.put("notify", " checked ");
            tagTable.put("sendMail", "true");

            if (bChkLoggers == true) {
                tagTable.put("chkLoggers", " checked ");
                tagTable.put("mailLoggers", "true");
            } else {
                tagTable.put("mailLoggers", "false");
            }
        } else {
            tagTable.put("sendMail", "false");
            tagTable.put("mailLoggers", "false");

            if (permTable.get(Field.NOTIFY) != null) {
                int notifyPermission = permTable.get(Field.NOTIFY);

                if ((notifyPermission & Permission.CHANGE) == 0) {
                    tagTable.put(Field.NOTIFY + "_disabled", "disabled");
                }

                tagTable.put("chkLoggers", "");
            }
        }

        // IsPrivate
        if (bIsPrivate == true) {
            tagTable.put(Field.IS_PRIVATE, "checked");
            tagTable.put("privateClassName", "sx b cr");
        } else if (viewPrivate == false) {
            tagTable.put("is_private_display", "none");
        } else if (viewPrivate == true) {
            if (permTable.get(Field.IS_PRIVATE) != null) {
                int privatePermission = permTable.get(Field.IS_PRIVATE);

                if ((privatePermission & Permission.CHANGE) == 0) {
                    tagTable.put(Field.IS_PRIVATE + "_disabled", "disabled");
                }
            }

            tagTable.put(Field.IS_PRIVATE, "");
            tagTable.put("privateClassName", "sx cr");
        }

        tagTable.put("autoSaveRate", getAutoSaveRate(userConfig));
        IUCValidator.validateEUCs(request, aIUCMetaData, isAppend, currentRequest, ba, false, false, aFailedEUCs, aBadSyntaxEUCs, aNotAllowedPrivateEUCs, new ArrayList<String>());

        UpdateRequest ur = new UpdateRequest();
        ur.setSource(TBitsConstants.SOURCE_WEB);
        ur.setContext(aRequest.getContextPath());
        
        AddRequest    ar = new AddRequest();
        ar.setSource(TBitsConstants.SOURCE_WEB);
        ar.setContext(aRequest.getContextPath());
        
        try {

            //
            // throw APIException if any IUC error.
            //
            reportAnyIUCError(aAllEUCs, aFailedEUCs, aBadDescriptorEUCs, aNotAllowedEUCs, aBadSyntaxEUCs, aBadOperandEUCs);

            if (caller == ADD_ACTION) {
                req = ur.updateRequest(request);
            } else {
                req = ar.addRequest(request);
            }

            //
            // Delete draft for the request, if any
            //
            String    timeStr    = paramTable.get("DTimestamp");
            Timestamp dTimestamp = null;
            UserDraft userDraft  = null;

            if ((timeStr != null) && (timeStr.trim().equals("") == false) && (timeStr.trim().equals("0") == false)) {
                try {
                    dTimestamp = new Timestamp(Long.parseLong(timeStr));
                } catch (Exception e) {
                    LOG.debug("",(e));
                }
            }

            if (dTimestamp != null) {
                userDraft = new UserDraft();
                userDraft.setUserId(userId);
                userDraft.setSystemId(systemId);
                userDraft.setTimestamp(dTimestamp.toGmtTimestamp());
                userDraft.setDraft("");
            }

            // Request has been submitted successfully.Now redirect the page.
            DTagReplacer hp = new DTagReplacer(CLOSE_FILE);

            if ((caller == ADD_REQUEST) || (caller == ADD_SUBREQUEST)) {
                if (userDraft != null) {
                    userDraft.setRequestId(0);
                    UserDraft.delete(userDraft);
                }
            } else if (caller == ADD_ACTION) {
                if (userDraft != null) {
                    userDraft.setRequestId(req.getRequestId());
                    UserDraft.delete(userDraft);
                }
            }

            if (userConfig.getIEAutoClose() == true) {
                hp.replace("close", "true");
            } else {
                hp.replace("close", "false");
            }

            String forwardUrl = WebUtil.getServletPath(aRequest, "/q/") + sysPrefix + "/" + req.getRequestId();

            hp.replace("url", forwardUrl);
            hp.replace("caller", Integer.toString(caller));

            String htmlContent  = hp.parse(systemId);
            String parentWindow = aRequest.getParameter("parentWindow");

            if ((parentWindow != null) && parentWindow.equals("view-request")) {
                if (userConfig.getIEAutoClose() == false) {
                    aResponse.sendRedirect(forwardUrl);
                } else {
                    out.println(htmlContent);
                }

                return;
            }

            out.println(htmlContent);

            return;
        } catch (TBitsException de) {

            //
            // TBitsException is thrown only while appending to a stale
            // version of a request and there are conflicting changes
            // Todo: Much better explanation is needed here.
            //
            // Request could not be submitted successfully.
            LOG.warn(de.toString(), de);

            ArrayList<DiffEntry> diffArraylist   = ur.getDiffList();
            int                  currentActionId = ur.getMaxActionId();
            boolean              showDiff        = getDiffHTML(diffArraylist, tagTable, aRequest, currentActionId, aAllEUCs);

            tagTable.put("userDraftsInfo", WebUtil.listUserDrafts(aRequest, userId, true, true));

            //
            // restore draft timestamp, if any
            //
            String timeStr = paramTable.get("DTimestamp");

            if ((timeStr != null) && (timeStr.trim().equals("") == false)) {
                try {
                    tagTable.put("DTimestamp", timeStr);
                } catch (Exception e) {
                    LOG.debug("",(e));
                    tagTable.put("DTimestamp", "0");
                }
            } else {
                tagTable.put("DTimestamp", "0");
            }

            String strOpenExtended = paramTable.get("openExtended");

            if ((strOpenExtended != null) && (strOpenExtended.trim().equals("") == false)) {
                tagTable.put("openExtended", strOpenExtended);
            }

            String strOpenSummary = paramTable.get("openSummary");

            if ((strOpenSummary != null) && (strOpenSummary.trim().equals("") == false)) {
                tagTable.put("openSummary", strOpenSummary);
            }

            String strOpenAssociate = paramTable.get("openAssociate");

            if ((strOpenAssociate != null) && (strOpenAssociate.trim().equals("") == false)) {
                tagTable.put("openAssociate", strOpenAssociate);
            }

            String strOpenRelated = paramTable.get("openRelated");

            if ((strOpenRelated != null) && (strOpenRelated.trim().equals("") == false)) {
                tagTable.put("openRelated", strOpenRelated);
            }

            ArrayList<Field> fields = Field.getFixedFieldsBySystemId(systemId);

            // Replace the field_label tags with their display names.
            showDisplayNames(systemId, fields, tagTable);
            decideFieldAbility(systemId, caller, fields, tagTable, permTable, duedateDisabled, allowNullDueDate, sc.getDefaultDueDate());

            ArrayList<TBitsException> exceptionArraylist = new ArrayList<TBitsException>();

            exceptionArraylist.add(de);

            if (showDiff == true) {
                tagTable.put("action_diff_exception", "true");
            } else {
                de.setDescription(de.getDescription() + "<br>&nbsp;&nbsp;&nbsp;" + "Some IUCs are also involved in conflicts.");
            }

            tagTable.put("cssFile", WebUtil.getCSSFile(sc.getWebStylesheet(), sysPrefix, false));
            redirectAndPrintExceptions(exceptionArraylist, tagTable, showDiff, aRequest);

            DTagReplacer hp           = null;
            String       parentWindow = paramTable.get("parentWindow");

            if ((parentWindow != null) && parentWindow.equals("view-request")) {
                Hashtable<String, String> rTagTable = new Hashtable<String, String>();
                ActionDetails             ad        = new ActionDetails();

                try {
                    DTagReplacer aReplacer = new DTagReplacer(ActionDetails.UPDATE_FORM);
                    SlotFillerFactory euf = SlotFillerFactory.getInstance() ;
                    euf.runUpdateRequestSlotFillers(aRequest, aResponse, ba, req, user, tagTable) ;
                    ad.replaceUpdateFormTags(aReplacer, tagTable);
                    rTagTable.put("updateDivContent", aReplacer.parse(systemId));
                    rTagTable.put("updateDivDisplay", "BLOCK");
                    hp = ad.renderRequestDetails(aRequest, aResponse, ba, rTagTable, permTable, currentRequest, user, clientOffset);
                    ActionHelper.replaceTags(hp, rTagTable, ActionDetails.TAG_LIST);
                    out.println(hp.parse(systemId));
                } catch (Exception e) {
                    LOG.severe("",(e));
                    out.println(TBitsLogger.getStackTrace(e));
                }

                return;
            }

            /*
             * Finally read the Html Interface into the parser and replace the
             * tags and finally print it out.
             */
            try {
                hp = new DTagReplacer(HTML_MAIN);
            } catch (FileNotFoundException fnfe) {
                LOG.severe("Add-Request template hasnt been found.", fnfe);
            } catch (IOException ioe) {
                LOG.severe("IO Exception", ioe);
            }

            // NITIRAJ msg : calling the ExtUIFactory
            String YUIJss = ActionHelper.getResourceString(myYUIJSs, WebUtil.getNearestPath(aRequest, "")) ;
       //     LOG.info("YUIJss ="+YUIJss) ;
            String tbitsJSs = ActionHelper.getResourceString(myTbitsJSs, WebUtil.getNearestPath(aRequest, "")) ;
        //    LOG.info("tbitsJSs ="+tbitsJSs) ;
//            String reqCss = ActionHelper.getResourceString(myAddReqCSSs, WebUtil.getNearestPath(aRequest, ""));
//            LOG.info("reqCss = " + reqCss) ;
            
            tagTable.put("YUIJSs", YUIJss ) ;
            tagTable.put("tbitsJSs", tbitsJSs ) ;
//            tagTable.put("reqCss", reqCss);
//            String xAddReqCSSs = ActionHelper.getResourceString(myAddReqCSSs, WebUtil.getNearestPath(aRequest, "")) ;
//            LOG.info("myAddReqCSSs ="+xAddReqCSSs) ;
//            tagTable.put("myAddReqCSSs", xAddReqCSSs ) ;
            
            SlotFillerFactory euf = SlotFillerFactory.getInstance() ;
            switch( caller ) 
            {
            	case ADD_REQUEST :
            		euf.runAddRequestSlotFillers(aRequest, aResponse, ba, user, tagTable) ;
            		break ;
            		
            	case ADD_SUBREQUEST :
            		euf.runSubRequestSlotFillers(aRequest, aResponse, ba, req, user, tagTable ) ;
            		break ;
            }
            tagTable.put("caller", Integer.toString(caller));                        
            replaceTags(hp, tagTable);

            String html = hp.parse(systemId);

            // Get the list of diff entry where there are conflicts.
            ArrayList<DiffEntry> delist = ur.getDiffList();

            out.println(html);

            return;
        } catch (APIException apie) {

            // Request could not be submitted successfully.
            String toString = apie.toString();

            if ((toString.indexOf("runWorkflowRules") > 0) || (toString.indexOf("checkAssigneeList") > 0) || (toString.indexOf("Unsupported") > 0)) {

                /*
                 * Log this as info if this submission failed due to invalid
                 * assignee/workflow rule violation.
                 */
                LOG.info(apie.toString(), apie);
            } else {
                LOG.severe(apie.toString(), apie);
            }

            //
            // List User Drafts, if any
            //
            tagTable.put("userDraftsInfo", WebUtil.listUserDrafts(aRequest, userId, true, true));

            //
            // restore draft timestamp, if any
            //
            String timeStr = paramTable.get("DTimestamp");

            if ((timeStr != null) && (timeStr.trim().equals("") == false)) {
                try {
                    tagTable.put("DTimestamp", timeStr);
                } catch (Exception e) {
                    LOG.debug("",(e));
                    tagTable.put("DTimestamp", "0");
                }
            } else {
                tagTable.put("DTimestamp", "0");
            }

            String strOpenExtended = paramTable.get("openExtended");

            if ((strOpenExtended != null) && (strOpenExtended.trim().equals("") == false)) {
                tagTable.put("openExtended", strOpenExtended);
            }

            String strOpenSummary = paramTable.get("openSummary");

            if ((strOpenSummary != null) && (strOpenSummary.trim().equals("") == false)) {
                tagTable.put("openSummary", strOpenSummary);
            }

            String strOpenAssociate = paramTable.get("openAssociate");

            if ((strOpenAssociate != null) && (strOpenAssociate.trim().equals("") == false)) {
                tagTable.put("openAssociate", strOpenAssociate);
            }

            String strOpenRelated = paramTable.get("openRelated");

            if ((strOpenRelated != null) && (strOpenRelated.trim().equals("") == false)) {
                tagTable.put("openRelated", strOpenRelated);
            }

            ArrayList<Field> fields = Field.getFixedFieldsBySystemId(systemId);

            // Replace the field_label tags with their display names.
            showDisplayNames(systemId, fields, tagTable);
            decideFieldAbility(systemId, caller, fields, tagTable, permTable, duedateDisabled, allowNullDueDate, sc.getDefaultDueDate());
            tagTable.put("cssFile", WebUtil.getCSSFile(sc.getWebStylesheet(), sysPrefix, false));

            //
            // Iterate through these exceptions to render them on
            // refreshed page.
            //
            ArrayList<TBitsException> exceptionArraylist = (apie.getExceptionList()).get(3);

            exceptionArraylist.addAll(apie.getExceptionList().get(2));
            redirectAndPrintExceptions(exceptionArraylist, tagTable, false, aRequest);

            /*
             * Finally read the Html Interface into the parser and replace the
             * tags and finally print it out.
             */
            DTagReplacer hp           = null;
            String       parentWindow = paramTable.get("parentWindow");

            if ((parentWindow != null) && parentWindow.equals("view-request")) {
                Hashtable<String, String> rTagTable = new Hashtable<String, String>();
                ActionDetails             ad        = new ActionDetails();

                try {
                    DTagReplacer aReplacer = new DTagReplacer(ActionDetails.UPDATE_FORM);
                    SlotFillerFactory euf = SlotFillerFactory.getInstance() ;
                    euf.runUpdateRequestSlotFillers(aRequest, aResponse, ba, req, user, tagTable) ;                    
                    ad.replaceUpdateFormTags(aReplacer, tagTable);
                    rTagTable.put("updateDivContent", aReplacer.parse(systemId));
                    rTagTable.put("updateDivDisplay", "BLOCK");
                    hp = ad.renderRequestDetails(aRequest, aResponse, ba, rTagTable, permTable, currentRequest, user, clientOffset);
                    ActionHelper.replaceTags(hp, rTagTable, ActionDetails.TAG_LIST);
                    out.println(hp.parse(systemId));
                } catch (Exception e) {
                    LOG.severe("",(e));
                }

                return;
            }

            try {
                hp = new DTagReplacer(HTML_MAIN);
                
                String YUIJSs = ActionHelper.getResourceString(myYUIJSs, WebUtil.getNearestPath(aRequest, "")) ;
          //      LOG.info("YUIJSs ="+YUIJSs) ;
                String tBitsJSs = ActionHelper.getResourceString(myTbitsJSs, WebUtil.getNearestPath(aRequest, "")) ;
          //      LOG.info("tBitsJSs ="+tBitsJSs) ;
//                String reqCss = ActionHelper.getResourceString(myAddReqCSSs, WebUtil.getNearestPath(aRequest, ""));
//                LOG.info("reqCss = " + reqCss) ;
                
                tagTable.put("YUIJSs", YUIJSs ) ;
                tagTable.put("tbitsJSs", tBitsJSs ) ;
//                tagTable.put("reqCss", reqCss);
//                String xAddReqCSSs = ActionHelper.getResourceString(myAddReqCSSs, WebUtil.getNearestPath(aRequest, "")) ;
//                LOG.info("myAddReqCSSs ="+xAddReqCSSs) ;
//                tagTable.put("myAddReqCSSs", xAddReqCSSs ) ;
//                
             // NITIRAJ msg : calling the SlotFiller                
                SlotFillerFactory euf = SlotFillerFactory.getInstance() ;
                switch( caller ) 
                {
                	case ADD_REQUEST :
                		euf.runAddRequestSlotFillers(aRequest, aResponse, ba, user, tagTable);
                		break ;
                		
                	case ADD_SUBREQUEST :
                		euf.runSubRequestSlotFillers(aRequest, aResponse, ba, req, user, tagTable );
                		break ;
                }
                tagTable.put("caller", Integer.toString(caller));        
                replaceTags(hp, tagTable);

                String html = hp.parse(systemId);

                out.println(html);

                return;

            } catch (FileNotFoundException fnfe) {
                LOG.severe("Add-Request template hasnt been found.", fnfe);
            } catch (IOException ioe) {
                LOG.severe("IO Exception", ioe);
            }

        }
    }

	private String parseHTMLDate(String strDueDate, User user) {
		String format = null;
		//First try using the user's format
        String webDateFormat = null;
        WebConfig webConfigObj = user.getWebConfigObject();
        if(webConfigObj != null)
        {
        	webDateFormat = webConfigObj.getWebDateFormat();
        }
        if((webDateFormat != null) && (webDateFormat.trim().length() != 0))
        {
        	try
            {
            	SimpleDateFormat sdf = new SimpleDateFormat(webDateFormat);
            	Date parsedDate = sdf.parse(strDueDate);
            	SimpleDateFormat sdf1 = new SimpleDateFormat(API_DATE_FORMAT);
            	format = sdf1.format(parsedDate);
            }
            catch(Exception e)
            {
            	e.printStackTrace();
            	LOG.warn("Unable to reformat date to API format", e);
            }
        }
        if(format == null)
        	format = parseHTMLDate(strDueDate);
        return format;
	}

	private String parseHTMLDate(String strDueDate) {
		Hashtable<String, Date> dateHash  = DateTimeParser.parse(strDueDate);
		Timestamp               temp      = null;
		String                  format    = "";

		if (dateHash != null) {
		    Date date = dateHash.get("DATE_TIME");

		    if (date != null) {
		        temp   = Timestamp.getTimestamp(date);
		        format = temp.toCustomFormat("yyyy-MM-dd HH:mm:ss");
		    } else {
		        date = dateHash.get("DATE");

		        if (date != null) {
		            temp   = Timestamp.getTimestamp(date);
		            format = temp.toCustomFormat("yyyy-MM-dd 23:59:00");
		        } else {
		            date = dateHash.get("SHORT_DATE");

		            if (date != null) {
		                temp   = Timestamp.getTimestamp(date);
		                format = temp.toCustomFormat("yyyy-MM-dd 23:59:00");
		            }
		        }
		    }
		}
		return format;
	}

    /**
     * This method returns the exception object for invalid request.
     */
    private TBitsException invalidRequest() {
        return new TBitsException(Messages.getMessage("INVALID_REQUEST_ID"));
    }

    /*
     * This method is used to render list of all exceptions in the api.
     *
     *
     */
    private void redirectAndPrintExceptions(ArrayList<TBitsException> aExceptionlist, Hashtable<String, Object> aTagTable, boolean viewDetails, HttpServletRequest aRequest) {
        String       exceptionBeginning = "<tr><td class=\"s\" align=\"left\">";
        String       exceptionEnding    = "</td></tr>";
        StringBuffer strBuf             = new StringBuffer();
        int          size               = aExceptionlist.size();

        strBuf.append("<table cellspacing=\"0\" ").append("cellpadding=\"0\" width=\"100%\" >");

        for (int i = 0; i < size; i++) {
            if ((i == size - 1) && (viewDetails == true)) {

                /*
                 *  append("domTT_activate(").
                 *  append("this,event,").
                 *  append("'caption','Action Diff &nbsp;&nbsp;").
                 *  append("&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;").
                 *  append("&nbsp;&nbsp;',").
                 *  append("'content',document.getElementById(").
                 *  append("'showDiff').innerHTML,").
                 *  append("'type','Sticky',").
                 *  append("'StatusText','Action Diff','closeAction','remove');
                 *  \">").
                 */
                strBuf.append(exceptionBeginning).append((i + 1)
                              + ".").append((aExceptionlist.get(i)).getDescription()).append("<span class=\"l cb\">").append("(View Details) </span>").append(exceptionEnding);
            } else {
                strBuf.append(exceptionBeginning).append((i + 1) + ".").append((aExceptionlist.get(i)).getDescription()).append(exceptionEnding);
            }
        }

        strBuf.append("</table>");
        aTagTable.put("exception_list", strBuf.toString());
        aTagTable.put("show_exception_block", "true");
        aTagTable.put("nearestPath", WebUtil.getNearestPath(aRequest, ""));
    }

    /**
     * This method returns an HTML that has an extended-Text-Field rendered.
     *
     * @param aField        Field Object corresponding to this Extended Type.
     * @param aTabIndex     Tab Index of this type in the HTML.
     * @param aDisabled     String that decides whether this field should be
     *                      enabled or disabled.
     * @param req4Context   The HttpServletRequest used specifically for determining context. This is done to avoid the conflict with other request which is being used for determining whether there is any paramaters specified to the query.
     * @return String with HTML code for rendering this Field.
     */
    private String renderExtendedBit(Field aField, int aTabIndex, String aDisabled, StringBuilder aValFunctions, Hashtable<String,String> paramTable, HttpServletRequest req4Context, int caller)
            throws TBitsException, DatabaseException {
        StringBuilder buffer        = new StringBuilder();
        String        fieldName     = aField.getName();
        boolean       checked       = false;
        String        bitName       = null;

        if (paramTable != null) {
            bitName = paramTable.get(fieldName);

            if ((bitName == null) || (bitName.trim().equals("") || bitName.trim().equalsIgnoreCase("false") || bitName.trim().equalsIgnoreCase("unchecked") || bitName.trim().equalsIgnoreCase("unselected") || bitName.trim().equalsIgnoreCase("off") || bitName.trim().equalsIgnoreCase("0") )) {
                checked = false;
            } else 
            {
                checked = true;
            }
//            if (bitName != null) {
//                if (((bitName.trim()).compareTo("true") == 0) || ((bitName.trim()).compareTo("1") == 0)) {
//                    checked = true;
//                }
//            }
        }

        buffer.append("<tr style=\"display:none\" id=\"" + fieldName + "_ex\" >").append("<td class=\"s b\" align=\"right\" nowrap>").append(aField.getDisplayName()).append(
            "</td>\n<td>&nbsp;</td>\n").append("<td class=\"s b\" align=\"left\" ").append("colspan=\"5\">").append("<INPUT onchange='javascript:frmChanged();' ").append(
            " style=\"display:none\" onkeydown=\"onKeyDownField(event);\" ").append(aDisabled).append(" type=\"checkbox\"  tabindex='").append(aTabIndex).append("' id=\"").append(fieldName).append(
            "\"").append(" name=\"").append(fieldName).append("\"");

        if (checked == true) {
            buffer.append(" checked ");
        }

        buffer.append(">\n</td>\n</tr>");

        return buffer.toString();
    }

    /**
     * This method returns an HTML that has an extended-Date-Field rendered.
     *
     * @param aField        Field Object corresponding to this Extended Type.
     * @param aDefaultDate  Default Date value in the field.
     * @param aTabIndex     Tab Index of this type in the HTML.
     * @param aDisabled     String that decides whether this field should be
     *                      enabled or disabled.
     * @param req4Context TODO
     * @return String with HTML code for rendering this Field.
     */
    private String renderExtendedDate(Field aField, String aDefaultDate, int aTabIndex, String aDisabled, StringBuilder aValFunctions, Hashtable<String,String> paramTable, HttpServletRequest req4Context, int caller)
            throws TBitsException, DatabaseException {
        StringBuilder buffer        = new StringBuilder();
        String        fieldName     = aField.getName();
        String        stringName    = null;
        String        extendedDate  = null;
        User user = WebUtil.validateUser(req4Context);

        
        stringName = paramTable.get(fieldName);
        if (stringName != null) 
            {
//                Timestamp ts = new Timestamp(stringName, "yyyy-MM-dd HH:mm:ss");
//
//                if (ts != null) {
//                	user.get
//                	Timestamp.
            	String apiFormatDate = null ;
        		if(stringName != null)
        		{
                   apiFormatDate = parseHTMLDate(stringName, user);
                   if( apiFormatDate != null )
                   {
                	   System.out.println("Formatted the date '" + stringName + "' into : '" + apiFormatDate + "' for field '" + fieldName + "'");
                	   Timestamp ts =null;
						try {
							ts = new Timestamp( apiFormatDate, TBitsConstants.API_DATE_FORMAT);
							extendedDate = ts.toCustomFormat(user.getWebConfigObject().getWebDateFormat());
						} catch (ParseException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}                	   
                   }
        		}
        	}
        
        aValFunctions.append("|").append("date").append(",").append(fieldName).append("|");
        buffer.append("<tr style=\"display:none\" id=\"" + fieldName + "_ex\" >").append("<td class=\"s b\" align=\"right\" nowrap>").append(aField.getDisplayName()).append(
            "</td>\n<td>&nbsp;</td>\n<td class=\"s\" align=\"left\" ").append("colspan=\"5\">").append("<TABLE cellspacing=0 cellpadding=0 width=\"100%\" >").append("<TR>\n<TD>\n").append(
            "<INPUT style=\"display:none\" tabindex='").append(aTabIndex).append("' onchange='javascript:frmChanged();' ").append(" onkeydown=\"onKeyDownField(event);\" ").append(" type='text' ").    // onfocusout='validateExtDate(this)' ").
            append(aDisabled).append(" value='");

        if (extendedDate != null)  {
            buffer.append(extendedDate);
        } else {
            buffer.append(aDefaultDate);
        }

        buffer.append("' id=\"").append(fieldName).append("\"").append(" name=\"").append(fieldName).append("\"").append("'>").append("\n</td>").append("<td>");

        if ((aDisabled != null) && (!aDisabled.trim().equalsIgnoreCase("disabled"))) {
            buffer.append("<img id=\"_due_date_img_cal_").append(fieldName).append("\" src=\"").append(WebUtil.getNearestPath(req4Context, "/web/images/cal.gif")).append("\" height=\"22\" align=\"left\">").append(
                "</img>").append("<script type=\"text/javascript\">\n").append("Calendar.setup({\n").append("inputField : \"").append(fieldName).append("\",\n").append(
                "button : \"_due_date_img_cal_").append(fieldName).append("\",\n").append("ifFormat : ").append(getCurDateFormat(user.getWebConfigObject().getWebDateFormat())).append(",\n").append("showsTime : \"true\",\n").append("timeFormat : \"24\" ").append(
                "});\n").append("</script>\n");
        }

        buffer.append("</td>\n<td width=\"100%\">&nbsp;</td>\n</tr>").append("</table></td></tr>");

        return buffer.toString();
    }

    /**
     * Returns Date field type's date format.
     * @param webDateFormat
     * @return
     */
    private String getCurDateFormat(String webDateFormat){    	
    	String dateFormat="\"%m/%d/%Y %H:%M\"";
    	if (webDateFormat.equals("dd-MMM-yyyy"))
    		dateFormat = "\"%d-%b-%Y\"";
    	else if (webDateFormat.equals("MM/dd/yyyy HH:mm:ss"))
    		dateFormat = "\"%m/%d/%Y %H:%M:%S\"";
    	else if (webDateFormat.equals("MM/dd/yyyy HH:mm:ss.SSS"))
    		dateFormat = "\"%m/%d/%Y %H:%M:%S\"";
    	else if (webDateFormat.equals("yyyy-MM-dd"))
    		dateFormat = "\"%Y-%m-%d\"";
    	else if (webDateFormat.equals("MMM dd, yyyy"))
    		dateFormat = "\"%b %d, %Y\"";
    	else if (webDateFormat.equals("dd-MMM-yyyy HH:mm:ss"))
    		dateFormat = "\"%d-%b-%Y %H:%M:%S\"";
    	else if (webDateFormat.equals("MM/dd/yyyy HH:mm:ss zzz"))
    		dateFormat = "\"%m/%d/%Y %H:%M:%S\"";
    	else if (webDateFormat.equals("MM/dd/yyyy"))
    		dateFormat = "\"%m/%d/%Y\"";
    	else
    	{
    		Hashtable<String, String> formatMap = new Hashtable<String, String>();
    		formatMap.put("yyyy", "%Y");
    		formatMap.put("yy", "%y");
    		formatMap.put("MMM", "%b");
    		formatMap.put("MM", "%m");
    		formatMap.put("dd", "%d");
    		formatMap.put("HH", "%H");
    		formatMap.put("mm", "%M");
    		formatMap.put("ss", "%S");
    		formatMap.put("SSS", "");
    		formatMap.put("zzz", "");
    		dateFormat = "\"" + webDateFormat + "\"";
    		for(String key:formatMap.keySet())
    		{
    			dateFormat = dateFormat.replace(key, formatMap.get(key));
    		}
    	}
    	return dateFormat;
    }
    
    /**
     * This method renders the extended fields according to the permissions
     * the user has as specified in the aPermTable. The default content in the
     * field controls is
     * <ul>
     * <li> Free-Form-Text Fields are empty.
     * <li> Type-Fields have their default value selected by default.
     * <li> Date-Fields have the current date as the default.
     * </ul>
     *
     * @param aSystemId  Current Business Area ID.
     * @param aUserId    Current User Id.
     * @param aPermTable Table of Permissions [FieldId/FieldName, Permission].
     * @param aTagTable  This is filled with [ Tag Name, Html List].
     * @param aRequest TODO
     */
    // caller is one of AddRequest (get) , AddRequest (post), AddAction
//    private void renderExtendedFields(int aSystemId, int aUserId, Hashtable<String, Integer> aPermTable, Hashtable<String, Object> aTagTable, Hashtable<String,String> paramTable, HttpServletRequest req4Context, int caller )
//            throws TBitsException, DatabaseException {
//        Hashtable<String, ArrayList<Field>> extendedFields = Field.getExtendedFields(aSystemId);
//        ArrayList<Field>                    eString        = (ArrayList<Field>) extendedFields.get("__String__");
//        ArrayList<Field>                    eTypes         = (ArrayList<Field>) extendedFields.get("__Type__");
//        ArrayList<Field>                    eDates         = (ArrayList<Field>) extendedFields.get("__Datetime__");
//        ArrayList<Field>                    eBoolean       = (ArrayList<Field>) extendedFields.get("__Boolean__");
//        ArrayList<Field>                    eInt           = (ArrayList<Field>) extendedFields.get("__Int__");
//        ArrayList<Field>                    eReal          = (ArrayList<Field>) extendedFields.get("__Real__");
//        ArrayList<Field>                    eText          = (ArrayList<Field>) extendedFields.get("__Text__");
//        ArrayList<Field>                    fields         = new ArrayList<Field>();
//
//        fields.addAll(eString);
//        fields.addAll(eInt);
//        fields.addAll(eReal);
//        fields.addAll(eTypes);
//        fields.addAll(eDates);
//        fields.addAll(eBoolean);
//        fields.addAll(eText);
//        
//        // getHtml(ArrayList<Field> aFieldList, int aSystemId, int aUserId, Hashtable<String, Object> aTagTable, Hashtable<String, Integer> aPermTable,Hashtable<String, String> paramTable, Hashtable<String, String> aReq)
//        ArrayList     html   = getHtml(fields, aSystemId, aUserId, aTagTable, aPermTable, paramTable, null, req4Context, caller );
//        int           size   = html.size();
//        StringBuilder buffer = new StringBuilder();
//
//        for (int i = 0; i < size; i++) {
//            buffer.append(html.get(i).toString());
//        }
//
//        aTagTable.put("extended_fields", buffer.toString());
//    }

    /**
     * This method renders the extended fields according to the permissions
     * the user has as specified in the aPermTable. The default content in the
     * field controls is read from the HttpRequest object. If it's not found
     * here then the default values are as follows.
     * <ul>
     * <li> Free-Form-Text Fields are empty.
     * <li> Type-Fields have their default value selected by default.
     * <li> Date-Fields have the current date as the default.
     * </ul>
     *
     * @param aSystemId  Current Business Area ID.
     * @param aUserId    Current User Id.
     * @param aPermTable Table of Permissions [FieldId/FieldName, Permission].
     * @param aTagTable  This is filled with [ Tag Name, Html List].
     * @param aRequest   HttpRequest Object to read the values
     * @param aReq       request Hastable.
     *
     */
    // caller is one of AddRequest ( get ) , AddRequest (post), AddAction 
    private void renderExtendedFields(int aSystemId, int aUserId, Hashtable<String, Integer> aPermTable, Hashtable<String, Object> aTagTable, Hashtable<String, String> paramTable,
                                      HttpServletRequest req4Context, Hashtable<String, String> aReq, int caller)
            throws TBitsException, DatabaseException {
        Hashtable<String, ArrayList<Field>> extendedFields = Field.getExtendedFields(aSystemId);
        ArrayList<Field>                    eString        = (ArrayList<Field>) extendedFields.get("__String__");
        ArrayList<Field>                    eTypes         = (ArrayList<Field>) extendedFields.get("__Type__");
        ArrayList<Field>                    eDates         = (ArrayList<Field>) extendedFields.get("__Datetime__");
        ArrayList<Field>                    eBoolean       = (ArrayList<Field>) extendedFields.get("__Boolean__");
        ArrayList<Field>                    eInt           = (ArrayList<Field>) extendedFields.get("__Int__");
        ArrayList<Field>                    eReal          = (ArrayList<Field>) extendedFields.get("__Real__");
        ArrayList<Field>                    eText          = (ArrayList<Field>) extendedFields.get("__Text__");
		ArrayList<Field>                    eUserType      = (ArrayList<Field>) extendedFields.get("__MultiValue__");
        ArrayList<Field>                    fields         = new ArrayList<Field>();

        fields.addAll(eString);
        fields.addAll(eInt);
        fields.addAll(eReal);
        fields.addAll(eTypes);
        fields.addAll(eDates);
        fields.addAll(eBoolean);
        fields.addAll(eText);
        fields.addAll(eUserType);

        ArrayList<String> html   = getHtml(fields, aSystemId, aUserId, aTagTable, aPermTable, paramTable, aReq, req4Context, caller );
        int               size   = html.size();
        StringBuilder     buffer = new StringBuilder();

        for (int i = 0; i < size; i++) {
            buffer.append(html.get(i).toString());
        }

        aTagTable.put("extended_fields", buffer.toString());
    }

    /**
     * This method returns an HTML that has an extended-Text-Field rendered.
     *
     * @param aField        Field Object corresponding to this Extended Type.
     * @param aTabIndex     Tab Index of this type in the HTML.
     * @param aDisabled     String that decides whether this field should be
     *                      enabled or disabled.
     * @param req4Context TODO
     * @return String with HTML code for rendering this Field.
     */
    private String renderExtendedInt(Field aField, int aTabIndex, String aDisabled, StringBuilder aValFunctions, Hashtable<String,String>paramTable, HttpServletRequest req4Context, int caller)
            throws TBitsException, DatabaseException {
        StringBuilder buffer        = new StringBuilder();
        String        fieldName     = aField.getName();
        String        intName       = null;

        intName = paramTable.get(fieldName);

        if (intName != null && intName.trim().equalsIgnoreCase(""))  
        {
        	intName = null ;
        }

        String regex = aField.getRegex();

        buffer.append("<tr style=\"display:none\" id=\"" + fieldName + "_ex\" >").append("<td class=\"s b\" align=\"right\" nowrap>").append(aField.getDisplayName()).append(
            "</td>\n<td>&nbsp;</td>\n").append("<td class=\"s b\" align=\"left\" ").append("colspan=\"5\">").append("<INPUT onchange='javascript:frmChanged();' ").append(
            " style=\"display:none\" onkeydown=\"onKeyDownField(event);\" ").append(aDisabled).append(" tabindex='").append(aTabIndex).append("' ").append(" type='text' ").append(" size='100' ").append(
            "id=\"").append(fieldName).append("\"").append(" name=\"").append(fieldName).append("\"");

        if (intName != null)  {
            buffer.append(" value=\"" + intName + "\"");
        }
        else 
        {
        	buffer.append(" value=\"" + "\"");
        }

        if ((regex == null) || regex.trim().equals("")) {
            if (aField.getDataTypeId() == DataType.REAL) {
                regex = "^[\\+\\-]?[0-9]*\\.?[0-9]*?[Ee]?[\\+\\-]?[0-9]*$";
                buffer.append(" maxlength='20' ");
            } else if (aField.getDataTypeId() == DataType.INT) {
                regex = "^[0-9]*$";
                buffer.append(" maxlength='9' ");
            }
        }

        //
        // If the regular-expression for this field is not null and not
        // empty then check for validity on focus out.
        //
        if ((regex != null) &&!regex.trim().equals("")) {
            aValFunctions.append("|");

            if (aField.getDataTypeId() == DataType.INT) {
                aValFunctions.append("int");
            }

            if (aField.getDataTypeId() == DataType.REAL) {
                aValFunctions.append("real");
            }

            aValFunctions.append(",").append(fieldName).append(",").append(regex).append(",").append(aField.getDisplayName()).append("|");
        }

        buffer.append(">\n</td>\n</tr>");

        return buffer.toString();
    }

    /**
     * This method returns an HTML that has an extended-Text-Field rendered.
     *
     * @param aField        Field Object corresponding to this Extended Type.
     * @param aTabIndex     Tab Index of this type in the HTML.
     * @param aDisabled     String that decides whether this field should be
     *                      enabled or disabled.
     * @param req4Context TODO
     * @return String with HTML code for rendering this Field.
     */
    private String renderExtendedString(Field aField, int aTabIndex, String aDisabled, StringBuilder aValFunctions, Hashtable<String,String> paramTable, HttpServletRequest req4Context, int caller)
            throws TBitsException, DatabaseException {
        StringBuilder buffer        = new StringBuilder();
        String        fieldName     = aField.getName();
        String        stringName    = null;

        stringName = paramTable.get(fieldName);
        String regex = aField.getRegex();

        buffer.append("<tr style=\"display:none\" id=\"" + fieldName + "_ex\" >").append("<td class=\"s b\" align=\"right\" nowrap>").append(aField.getDisplayName()).append(
            "</td>\n<td>&nbsp;</td>\n").append("<td class=\"s b\" align=\"left\" ").append("colspan=\"5\">").append("<INPUT onchange='javascript:frmChanged();' ").append(
            " style=\"display:none\" onkeydown=\"onKeyDownField(event);\" ").append(aDisabled).append(" style='width:99%' tabindex='").append(aTabIndex).append("' type='text' ").append(
            " size='100' ").append("id=\"").append(fieldName).append("\"").append(" name=\"").append(fieldName).append("\"");

        if (stringName != null) {
            stringName = Utilities.htmlEncode(stringName);
            buffer.append(" value=\"" + stringName + "\"");
        }

        buffer.append(">\n</td>\n</tr>");

        return buffer.toString();
    }

    /**
     * This method returns an HTML that has an extended-Text-Field rendered.
     *
     * @param aField        Field Object corresponding to this Extended Type.
     * @param aTabIndex     Tab Index of this type in the HTML.
     * @param aDisabled     String that decides whether this field should be
     *                      enabled or disabled.
     * @param req4Context TODO
     * @return String with HTML code for rendering this Field.
     */
    private String renderExtendedText(Field aField, int aTabIndex, String aDisabled, StringBuilder aValFunctions, Hashtable<String,String> paramTable, HttpServletRequest req4Context, int caller)
            throws TBitsException, DatabaseException {
        StringBuilder buffer        = new StringBuilder();
        String        fieldName     = aField.getName();
        String        stringName    = null;

        stringName = paramTable.get(fieldName);
        String extendedText = "" ;
        try {
            extendedText = getExtendedTextHtml(aField.getDisplayName(), fieldName, aDisabled, stringName);
        } catch (FileNotFoundException fnfe) {
            LOG.severe("Extended field text html missing!!", fnfe);
        } catch (IOException ioe) {
            LOG.severe("IOException while parsing text html", ioe);
        }

        buffer.append(extendedText) ;
        return buffer.toString();
    }

    private String renderExtendedMultiValue(Field aField, String aDisabled, Hashtable<String,String> paramTable)
	throws TBitsException, DatabaseException {
		StringBuilder buffer        = new StringBuilder();
		String        fieldName     = aField.getName();
		String        stringName    = null;

		stringName = paramTable.get(fieldName);
		String extendedMultiValue = "" ;
		try {
			extendedMultiValue = getMultiValueHtml(aField.getDisplayName(), fieldName, aDisabled, stringName);
		} catch (FileNotFoundException fnfe) {
			LOG.severe("MultiValue html File missing!!", fnfe);
		} catch (IOException ioe) {
			LOG.severe("IOException while parsing MultiValue html", ioe);
		}

		buffer.append(extendedMultiValue) ;
		return buffer.toString();
	}


	private String getMultiValueHtml(String label, String fieldName, String disabled, String fieldValue) throws FileNotFoundException, IOException {
		DTagReplacer  hp       = new DTagReplacer(HTML_MULTIVALUE);

		hp.replace("field_label", label);
		hp.replace("field_name", fieldName);

		if (fieldValue != null) {
			fieldValue = Utilities.htmlEncode(fieldValue);
			hp.replace("field_value", fieldValue);
		} else {
			hp.replace("field_value", "");
		}

		hp.replace("field_disabled", disabled);
		return hp.parse(0);
	}

    /**
     * This method returns an HTML that has an extended-Type-Field rendered.
     *
     * @param aField       Field Object corresponding to this Extended Type.
     * @param aSystemId    Current Business Area Id.
     * @param aViewPrivate True if user can view private items.
     * @param aTabIndex    Tab Index of this type in the HTML.
     * @param aDisabled    String that decides whether this field should be
     *                     enabled or disabled.
     * @param aRequestObject The request object
     * @param aRequest		The HTTP request				
     * @param aUserId 		User Id
     * @param aDepField		The dependent field definition for the current field
     * @param req4Context TODO
     * @return String with HTML code for rendering this Field.
     */
    private String renderExtendedType(Field aField, int aSystemId, boolean aViewPrivate, int aTabIndex, String aDisabled, Hashtable<String,String> paramTable, int aUserId,
                                      DependentField aDepField, HttpServletRequest req4Context, int caller )
            throws TBitsException, DatabaseException {
        StringBuilder buffer        = new StringBuilder();
        String        fieldName     = aField.getName();
        String        typeName      = null;
//        int           setPermission = ((aField.getPermission()) & (Permission.SET));

        // This method is used to populate the fields in all the three cases
        // viz add-request, add-action and form failure. In case of
        // add-action we pass the requestObject from which you get the
        // field value.This is set only if the setPermission of the field
        // is 0.In case of form failure we look at the
        // HttpServletRequest and get the field value. In case of
        // add-request we get the default value.
        if (paramTable != null) {
            typeName = paramTable.get(fieldName);

            if (typeName != null) 
            {
                Type type = Type.lookupBySystemIdAndFieldNameAndTypeName(aSystemId, fieldName, typeName);

                if( null != type )
                	typeName = type.getName();
                else
                	typeName = null ;
            }
        }
//        else if (aRequestObject != null) {
//            if (setPermission == 0) {
//                typeName = aRequestObject.myMapFieldToValues.get(fieldName);
//            }
//
//            if (typeName != null) {
//                Type type = Type.lookupBySystemIdAndFieldNameAndTypeName(aSystemId, fieldName, typeName);
//
//                typeName = type.getName();
//            }
//        }

        if ((typeName == null) || typeName.trim().equals("")) {
            Type defType = Type.getDefaultTypeBySystemIdAndFieldName(aSystemId, fieldName);

            if (defType == null) {
                LOG.info("No default type for : " + fieldName);

                return buffer.toString();
            }

            typeName = defType.getName();
        }

        StringBuffer onChangeFunList = new StringBuffer();

        onChangeFunList.append("frmChanged();");

        StringBuffer jsonList       = new StringBuffer();
        StringBuffer typeOptionList = new StringBuffer();
        boolean      optionsListed  = false;

        /*
         * Check if this Type'd field is a part of any dependency.
         */
        if (aDepField != null) {

            /*
             * Get the role played by this field in the dependency.
             * If this is the primary field then
             *   a. Get the dependency configuration.
             *   b. Get the list of types for the primary and secondary fields.
             *   c. Generate a JSON object that has the mapping of each type
             *      with the types in the secondary field.
             *   c. Update the onchange method to consider this map and change
             *      the contents of the secondary type'd field.
             *
             * If this is a dependent field then
             *   a. Call onChange method onload. This can be done by appending
             *      the method call to the JSON object which inturn will be
             *      embeded in script tags that gets executed on load.
             */
            if (aDepField.getDepRole() == DepRole.PRIMARY) {

                // Step a.
                int        sysId      = aDepField.getSystemId();
                int        depId      = aDepField.getDependencyId();
                Dependency dependency = Dependency.lookupBySystemIdAndDependencyId(sysId, depId);

                if (dependency != null) {
                    DepType  depType  = dependency.getType();
                    DepLevel depLevel = dependency.getLevel();

                    if (depLevel != DepLevel.APP_DEPENDENCY) {
                        DependencyConfig             dConfig       = dependency.getDepConfigObject();
                        String                       primFieldName = dConfig.getPrimaryField();
                        String                       secFieldName  = dConfig.getDependentField();
                        Field                        primField     = Field.lookupBySystemIdAndFieldName(sysId, primFieldName);
                        Field                        secField      = Field.lookupBySystemIdAndFieldName(sysId, secFieldName);
                        ArrayList<TypeDependencyMap> typeMaps      = dConfig.getDependentList();

                        if ((secField != null) && (primField != null)) {
                            getTypeList(aSystemId, primField, secField, typeName, typeMaps, aViewPrivate, typeOptionList, jsonList);
                            optionsListed = true;

                            String funCall = (new StringBuffer().append("changeSecList(").append(Utilities.htmlEncode(primFieldName)).append("_map, \"").append(
                                                 Utilities.htmlEncode(primFieldName)).append("\", \"").append(Utilities.htmlEncode(secFieldName)).append("\");")).toString();

                            onChangeFunList.append(funCall);
                        } else {
                            LOG.info("Some fields in the dependency are null");
                        }
                    } else {
                        LOG.info("This is an APP_Dependency. Ignoring this.");
                    }
                } else {
                    LOG.info("Dependency information could not be obtained.");
                }
            }    // END IF PRIMARY
                    else if (aDepField.getDepRole() == DepRole.DEPENDENT) {

                // Step a.
                int        sysId      = aDepField.getSystemId();
                int        depId      = aDepField.getDependencyId();
                Dependency dependency = Dependency.lookupBySystemIdAndDependencyId(sysId, depId);

                if (dependency != null) {
                    DependencyConfig dConfig       = dependency.getDepConfigObject();
                    String           primFieldName = dConfig.getPrimaryField();
                    String           secFieldName  = dConfig.getDependentField();
                    String           funCall       = (new StringBuffer().append("changeSecList(").append(Utilities.htmlEncode(primFieldName)).append("_map, \"").append(
                                                         Utilities.htmlEncode(primFieldName)).append("\", \"").append(Utilities.htmlEncode(secFieldName)).append("\");")).toString();

                    jsonList.append("\n").append(funCall);
                }
            }
        } else {
            LOG.info("This is not a dependent field: " + aField.getDisplayName());
        }

        if (optionsListed == false) {
            if ((typeName != null)) {              
                    typeOptionList.append(getTypeList(aSystemId, fieldName, aViewPrivate, typeName, caller ));              
            } 
//            else {
//                User user = User.lookupByUserId(aUserId);
//
//                typeOptionList.append(getTypeList(aSystemId, fieldName, aViewPrivate, user));
//            }
        }

        buffer.append("<tr style=\"display:none\" id=\"" + fieldName + "_ex\" >").append("<td class=\"s b\" align=\"right\" nowrap>").append(aField.getDisplayName()).append(
            "</td>\n<td>&nbsp;</td>\n").append("<td class=\"s b\" align=\"left\" style='display:none'").append("colspan=\"5\" id=\"" + fieldName + "_excol\">").append(
            "<div id=\"" + fieldName + "_exdiv\" style='display:none'>").append("<select onchange='").append(onChangeFunList.toString()).append("' ").append(
            " onkeydown=\"onKeyDownField(event);\" style='display:none'").append(" size='1' tabindex='").append(aTabIndex).append("' ").append(aDisabled).append(" id=\"").append(fieldName).append(
            "\" ").append(" name=\"").append(fieldName).append("\"").append(">\n").append(typeOptionList.toString()).append("\n</select>").append("\n<script language='javascript'>").append(
            jsonList.toString()).append("\n</script>").append("\n</div>").append("\n</td>").append("\n</tr>");

        return buffer.toString();
    }



    /**
     * This method Renders the fixed types namely Category, Status, Severity
     * and Request Type SELECT boxes with the default values to be selected
     * by default.
     *
     *
     * @param aSystemId    Current Business Area ID.
     * @param aViewPrivate True if user has permissions to view private items.
     * @param aTagTable    This is filled with [ Tag Name, Html List].
     * @param aReq         Request object.
     * @exception DETBitsExceptionncase of any application related errors.
     * @exception DatabaseException incase of any database related errors.
     */
    private void renderFixedTypes(int aSystemId, boolean aViewPrivate, Hashtable<String, Object> aTagTable, Hashtable<String, String> aParamTable, int action)
            throws TBitsException, DatabaseException {
    	
    	String category = aParamTable.get(Field.CATEGORY) ;
    	if( null == category )
    	{
    		Type type = Type.getDefaultTypeBySystemIdAndFieldName(aSystemId, Field.CATEGORY ) ;
    		if( null != type )
    			category = type.getName() ;
    	}
    	aTagTable.put(Field.CATEGORY + "_list", getTypeList(aSystemId, Field.CATEGORY, aViewPrivate, category , action));

    	String status = aParamTable.get(Field.STATUS) ;
    	if( null == status )
    	{
    		Type type = Type.getDefaultTypeBySystemIdAndFieldName(aSystemId, Field.STATUS) ;
    		if( null != type )
    			status = type.getName();
    	}
    	aTagTable.put(Field.STATUS + "_list", getTypeList(aSystemId, Field.STATUS, aViewPrivate, status , action));
    	

    	String severity = aParamTable.get(Field.SEVERITY) ;
    	if( null == severity )
    	{
    		Type type = Type.getDefaultTypeBySystemIdAndFieldName(aSystemId, Field.SEVERITY) ;
    		if( null != type )
    			severity = type.getName() ;
    	}
        aTagTable.put(Field.SEVERITY + "_list", getTypeList(aSystemId, Field.SEVERITY, aViewPrivate, severity, action));

        String requestType = aParamTable.get(Field.REQUEST_TYPE) ; 
        if( null == requestType )
        {
        	Type type = Type.getDefaultTypeBySystemIdAndFieldName(aSystemId, Field.REQUEST_TYPE) ;
        	if( null != type )
        		requestType = type.getName() ;
        }
        aTagTable.put(Field.REQUEST_TYPE + "_list", getTypeList(aSystemId, Field.REQUEST_TYPE, aViewPrivate, requestType, action));

        String office = aParamTable.get(Field.OFFICE) ; 
        if( null == office )
        {
        	Type type = Type.getDefaultTypeBySystemIdAndFieldName(aSystemId, Field.OFFICE) ;
        	if( null != type )
        		office = type.getName() ;
        }
        aTagTable.put(Field.OFFICE + "_list", getTypeList(aSystemId, Field.OFFICE, aViewPrivate, office, action));        
    }

     /**
     * This method Renders the fixed types namely Category, Status, Severity
     * and Request Type SELECT boxes with the default values to be selected
     * by default.
     *
     * @param aSystemId    Current Business Area ID.
     * @param aViewPrivate True if user has permissions to view private items.
     * @param aTagTable    This is filled with [ Tag Name, Html List].
     * @param aCategory    Category to be selected
     * @param aStatus      Status to be selected
     * @param aSeverity    Severity to be selected
     * @param aType        Request Type to be selected
     *
     */
//    private void renderFixedTypes(int aSystemId, boolean aViewPrivate, Hashtable<String, Object> aTagTable, String aCategory, String aSeverity, String aStatus, String aType, String aOffice,
//                                  User aUser)
//            throws TBitsException, DatabaseException {
//        if (aCategory == null) {
//            aTagTable.put(Field.CATEGORY + "_list", getTypeList(aSystemId, Field.CATEGORY, aViewPrivate, aUser));
//        } else {
//            aTagTable.put(Field.CATEGORY + "_list", getTypeList(aSystemId, Field.CATEGORY, aViewPrivate, aCategory.trim(), ADD_REQUEST));
//        }
//
//        if (aSeverity == null) {
//            aTagTable.put(Field.SEVERITY + "_list", getTypeList(aSystemId, Field.SEVERITY, aViewPrivate, aUser));
//        } else {
//            aTagTable.put(Field.SEVERITY + "_list", getTypeList(aSystemId, Field.SEVERITY, aViewPrivate, aSeverity.trim(), ADD_REQUEST));
//        }
//
//        if (aStatus == null) {
//            aTagTable.put(Field.STATUS + "_list", getTypeList(aSystemId, Field.STATUS, aViewPrivate, aUser));
//        } else {
//            aTagTable.put(Field.STATUS + "_list", getTypeList(aSystemId, Field.STATUS, aViewPrivate, aStatus.trim(), ADD_REQUEST));
//        }
//
//        if (aType == null) {
//            aTagTable.put(Field.REQUEST_TYPE + "_list", getTypeList(aSystemId, Field.REQUEST_TYPE, aViewPrivate, aUser));
//        } else {
//            aTagTable.put(Field.REQUEST_TYPE + "_list", getTypeList(aSystemId, Field.REQUEST_TYPE, aViewPrivate, aType.trim(), ADD_REQUEST));
//        }
//
//        if (aOffice == null) {
//            aTagTable.put(Field.OFFICE + "_list", getTypeList(aSystemId, Field.OFFICE, aViewPrivate, aUser));
//        } else {
//            aTagTable.put(Field.OFFICE + "_list", getTypeList(aSystemId, Field.OFFICE, aViewPrivate, aOffice.trim(), ADD_REQUEST));
//        }
//    }


    /**
     * This method Renders the fixed types namely Category, Status, Severity
     * and Request Type SELECT boxes with the default values to be selected
     * by default.
     *
     * @param aSystemId    Current Business Area ID.
     * @param aViewPrivate True if user has permissions to view private items.
     * @param aTagTable    This is filled with [ Tag Name, Html List].
     */
//    private void renderFixedTypes(int aSystemId, boolean aViewPrivate, Hashtable<String, Object> aTagTable, User aUser, Hashtable<String, String> aParamTable)
//            throws TBitsException, DatabaseException {
//    	
//        String type = aParamTable.get(Field.CATEGORY);
//
//        if ((type != null) && (aTagTable.get(Field.CATEGORY + "_disabled") == null)) {
//            aTagTable.put(Field.CATEGORY + "_list", getTypeList(aSystemId, Field.CATEGORY, aViewPrivate, type, ADD_REQUEST));
//        } else {
//            aTagTable.put(Field.CATEGORY + "_list", getTypeList(aSystemId, Field.CATEGORY, aViewPrivate, aUser));
//        }
//
//        type = aParamTable.get(Field.STATUS);
//
//        if ((type != null) && (aTagTable.get(Field.STATUS + "_disabled") == null)) {
//            aTagTable.put(Field.STATUS + "_list", getTypeList(aSystemId, Field.STATUS, aViewPrivate, type, ADD_REQUEST));
//        } else {
//            aTagTable.put(Field.STATUS + "_list", getTypeList(aSystemId, Field.STATUS, aViewPrivate, aUser));
//        }
//
//        type = aParamTable.get(Field.SEVERITY);
//
//        if ((type != null) && (aTagTable.get(Field.SEVERITY + "_disabled") == null)) {
//            aTagTable.put(Field.SEVERITY + "_list", getTypeList(aSystemId, Field.SEVERITY, aViewPrivate, type, ADD_REQUEST));
//        } else {
//            aTagTable.put(Field.SEVERITY + "_list", getTypeList(aSystemId, Field.SEVERITY, aViewPrivate, aUser));
//        }
//
//        type = aParamTable.get(Field.REQUEST_TYPE);
//
//        if ((type != null) && (aTagTable.get(Field.REQUEST_TYPE + "_disabled") == null)) {
//            aTagTable.put(Field.REQUEST_TYPE + "_list", getTypeList(aSystemId, Field.REQUEST_TYPE, aViewPrivate, type, ADD_REQUEST));
//        } else {
//            aTagTable.put(Field.REQUEST_TYPE + "_list", getTypeList(aSystemId, Field.REQUEST_TYPE, aViewPrivate, aUser));
//        }
//
//        type = aParamTable.get(Field.OFFICE);
//
//        if ((type != null) && (aTagTable.get(Field.OFFICE + "_disabled") == null)) {
//            aTagTable.put(Field.OFFICE + "_list", getTypeList(aSystemId, Field.OFFICE, aViewPrivate, type, ADD_REQUEST));
//        } else {
//            aTagTable.put(Field.OFFICE + "_list", getTypeList(aSystemId, Field.OFFICE, aViewPrivate, aUser));
//        }
//    }
    
    
    /**
     * This method Renders the fixed types namely Category, Status, Severity
     * and Request Type SELECT boxes with the default values to be selected
     * by default.
     *
     *
     * @param aSystemId    Current Business Area ID.
     * @param aViewPrivate True if user has permissions to view private items.
     * @param aTagTable    This is filled with [ Tag Name, Html List].
     * @param aReq         Request object.
     * @exception DETBitsExceptionncase of any application related errors.
     * @exception DatabaseException incase of any database related errors.
     */
   /* private void renderFixedTypes(int aSystemId, boolean aViewPrivate, Hashtable<String, Object> aTagTable, User aUser, Hashtable<String, String> aParamTable, Request aRequestObject, HttpServletRequest aRequest)
            throws TBitsException, DatabaseException {    	
        replaceTagsForFixedType(Field.CATEGORY, aSystemId, aViewPrivate, aRequestObject, aRequest, aUser.getUserId(), aTagTable);      
        replaceTagsForFixedType(Field.STATUS, aSystemId, aViewPrivate, aRequestObject, aRequest, aUser.getUserId(), aTagTable);
        replaceTagsForFixedType(Field.SEVERITY, aSystemId, aViewPrivate, aRequestObject, aRequest, aUser.getUserId(), aTagTable);
        replaceTagsForFixedType(Field.REQUEST_TYPE, aSystemId, aViewPrivate, aRequestObject, aRequest, aUser.getUserId(), aTagTable);
        replaceTagsForFixedType(Field.OFFICE, aSystemId, aViewPrivate, aRequestObject, aRequest, aUser.getUserId(), aTagTable);
    }
    
    private void replaceTagsForFixedType (String aFieldName, int aSystemId, boolean aViewPrivate, Request aRequestObject, HttpServletRequest aRequest, int aUserId,
    		Hashtable<String, Object> aTagTable)
    throws TBitsException, DatabaseException {
    	
    	Field aField = Field.lookupBySystemIdAndFieldName(aSystemId, aFieldName);
        Hashtable<Integer,DependentField> depFieldTable = DependentField.getBySystemId(aSystemId);
        DependentField aDepField = depFieldTable.get(aField.getFieldId());    	
    	
    	String        typeName      = null;
    	int           setPermission = ((aField.getPermission()) & (Permission.SET));

    	// This method is used to populate the fields in all the three cases
    	// viz add-request, add-action and form failure. In case of
    	// add-action we pass the requestObject from which you get the
    	// field value.This is set only if the setPermission of the field
    	// is 0.In case of form failure we look at the
    	// HttpServletRequest and get the field value. In case of
    	// add-request we get the default value.
    	if (aRequest != null) {
    		typeName = aRequest.getParameter(aFieldName);

    		if (typeName != null) {
    			Type type = Type.lookupBySystemIdAndFieldNameAndTypeName(aSystemId, aFieldName, typeName);

    			typeName = type.getName();
    		}
    	} else if (aRequestObject != null) {
    		if (setPermission == 0) {
    			typeName = aRequestObject.myMapFieldToValues.get(aFieldName);
    		}

    		if (typeName != null) {
    			Type type = Type.lookupBySystemIdAndFieldNameAndTypeName(aSystemId, aFieldName, typeName);

    			typeName = type.getName();
    		}
    	}

    	if ((typeName == null) || typeName.trim().equals("")) {
    		Type defType = Type.getDefaultTypeBySystemIdAndFieldName(aSystemId, aFieldName);

    		if (defType == null) {
    			LOG.info("No default type for : " + aFieldName);
    		}

    		typeName = defType.getName();
    	}

    	StringBuffer jsonList       = new StringBuffer();
    	StringBuffer typeOptionList = new StringBuffer();
    	boolean      optionsListed  = false;
    	String       funCall = "";
    	
    	 * Check if this Type'd field is a part of any dependency.
    	 
    	if (aDepField != null) {

    		
    		 * Get the role played by this field in the dependency.
    		 * If this is the primary field then
    		 *   a. Get the dependency configuration.
    		 *   b. Get the list of types for the primary and secondary fields.
    		 *   c. Generate a JSON object that has the mapping of each type
    		 *      with the types in the secondary field.
    		 *   c. Update the onchange method to consider this map and change
    		 *      the contents of the secondary type'd field.
    		 *
    		 * If this is a dependent field then
    		 *   a. Call onChange method onload. This can be done by appending
    		 *      the method call to the JSON object which inturn will be
    		 *      embeded in script tags that gets executed on load.
    		 
    		if (aDepField.getDepRole() == DepRole.PRIMARY) {

    			// Step a.
    			int        sysId      = aDepField.getSystemId();
    			int        depId      = aDepField.getDependencyId();
    			Dependency dependency = Dependency.lookupBySystemIdAndDependencyId(sysId, depId);

    			if (dependency != null) {
    				DepType  depType  = dependency.getType();
    				DepLevel depLevel = dependency.getLevel();

    				if (depLevel != DepLevel.APP_DEPENDENCY) {
    					DependencyConfig             dConfig       = dependency.getDepConfigObject();
    					String                       primFieldName = dConfig.getPrimaryField();
    					String                       secFieldName  = dConfig.getDependentField();
    					Field                        primField     = Field.lookupBySystemIdAndFieldName(sysId, primFieldName);
    					Field                        secField      = Field.lookupBySystemIdAndFieldName(sysId, secFieldName);
    					ArrayList<TypeDependencyMap> typeMaps      = dConfig.getDependentList();

    					if ((secField != null) && (primField != null)) {
    						getTypeList(aSystemId, primField, secField, typeName, typeMaps, aViewPrivate, typeOptionList, jsonList);
    						optionsListed = true;

    						funCall = (new StringBuffer().append("changeSecList(").append(Utilities.htmlEncode(primFieldName)).append("_map, '").append(
    								Utilities.htmlEncode(primFieldName)).append("', '").append(Utilities.htmlEncode(secFieldName)).append("');")).toString();

    						//onChangeFunList.append(funCall);
    						//jsonObj.add("funCall", new JsonPrimitive(funCall));
    						aTagTable.put(aFieldName + "_dep_fun_call", funCall);
    					} else {
    						LOG.info("Some fields in the dependency are null");
    					}
    				} else {
    					LOG.info("This is an APP_Dependency. Ignoring this.");
    				}
    			} else {
    				LOG.info("Dependency information could not be obtained.");
    			}
    		}    // END IF PRIMARY
    		else if (aDepField.getDepRole() == DepRole.DEPENDENT) {

    			// Step a.
    			int        sysId      = aDepField.getSystemId();
    			int        depId      = aDepField.getDependencyId();
    			Dependency dependency = Dependency.lookupBySystemIdAndDependencyId(sysId, depId);

    			if (dependency != null) {
    				DependencyConfig dConfig       = dependency.getDepConfigObject();
    				String           primFieldName = dConfig.getPrimaryField();
    				String           secFieldName  = dConfig.getDependentField();
    				funCall       = (new StringBuffer().append("changeSecList(").append(Utilities.htmlEncode(primFieldName)).append("_map, \"").append(
    						Utilities.htmlEncode(primFieldName)).append("\", \"").append(Utilities.htmlEncode(secFieldName)).append("\");")).toString();
    				
    				jsonList.append("\n").append(funCall);
    				aTagTable.put(aFieldName + "_dep_fun_call", "");
    			}
    		}
    	} else {
    		LOG.info("This is not a dependent field: " + aField.getDisplayName());
    	}

    	if (optionsListed == false) {
    		if ((typeName != null)) {
    			if (aRequestObject == null) {
    				typeOptionList.append(getTypeList(aSystemId, aFieldName, aViewPrivate, typeName, ADD_REQUEST));
    			} else {
    				typeOptionList.append(getTypeList(aSystemId, aFieldName, aViewPrivate, typeName, ADD_ACTION));
    			}
    		} else {
    			User user = User.lookupByUserId(aUserId);

    			typeOptionList.append(getTypeList(aSystemId, aFieldName, aViewPrivate, user));
    		}
    	} 	
    	aTagTable.put(aFieldName +"_list", typeOptionList.toString());
    	aTagTable.put(aFieldName + "_dependency_list", "\n<script language='javascript'>" + jsonList.toString() + "\n</script>");
    }*/
      
    /**
     * This method replaces all the tags with their corresponding values
     * reading them from the given table in the given HTML Parser object.
     *
     * @param aTagTable Table with [tag, value object]
     */
    private void replaceTags(DTagReplacer aDTagReplacer, Hashtable aTagTable) {
        if (tagList == null) {
            return;
        }

        int tagListSize = tagList.size();

        for (int i = 0; i < tagListSize; i++) {
            String key   = (String) tagList.get(i);
            String value = (String) aTagTable.get(key);

            if (value != null) {
                aDTagReplacer.replace(key, value);
            } else {
                aDTagReplacer.replace(key, "");
            }
        }
    }

    /*
     */
    private void reportAnyIUCError(ArrayList<String> aAllEUCs, ArrayList<String> aFailedEUCs, ArrayList<String> aBadDescriptorEUCs, ArrayList<String> aNotAllowedEUCs,
                                   ArrayList<String> aBadSyntaxEUCs, ArrayList<String> aBadOperandEUCs)
            throws APIException {
        if ((aAllEUCs.size() > 0) && (aFailedEUCs.size() > 0)) {
            APIException  apie    = new APIException();
            StringBuilder content = new StringBuilder();

            if (aBadDescriptorEUCs.size() > 0) {
                content.append("Unsupported descriptor:<br>");

                for (String str : aBadDescriptorEUCs) {
                    content.append("&nbsp;&nbsp;&nbsp;&nbsp;").append(str).append("<br>");
                }

                apie.addException(new TBitsException(content.toString()), 2);
            }

            content = new StringBuilder();

            if (aNotAllowedEUCs.size() > 0) {
                content.append("Unsupported IUC:<br>");

                for (String str : aNotAllowedEUCs) {
                    content.append("&nbsp;&nbsp;&nbsp;&nbsp;").append(str).append("<br>");
                }

                apie.addException(new TBitsException(content.toString()), 2);
            }

            content = new StringBuilder();

            if (aBadSyntaxEUCs.size() > 0) {
                content.append("Unsupported argument syntax:<br>");

                for (String str : aBadSyntaxEUCs) {
                    content.append("&nbsp;&nbsp;&nbsp;&nbsp;").append(str).append("<br>");
                }

                apie.addException(new TBitsException(content.toString()), 2);
            }

            content = new StringBuilder();

            if (aBadOperandEUCs.size() > 0) {
                content.append("Unsupported argument:<br>");

                for (String str : aBadOperandEUCs) {
                    content.append("&nbsp;&nbsp;&nbsp;&nbsp;").append(str).append("<br>");
                }

                apie.addException(new TBitsException(content.toString()), 2);
            }

            throw apie;
        }
    }

    /**
     * This method fills the Tagtable with [Field-Label, Displaynames] and
     * returns the Field List.
     *
     * @param aSystemId  Id of the Business Area.
     * @param aTagTable  Table that is filled with [Field-Label, DisplayName].
     * @param aFieldList List of fields.
     *
     */
    private void showDisplayNames(int aSystemId, ArrayList<Field> aFieldList, Hashtable<String, Object> aTagTable) throws DatabaseException, TBitsException {
        for (Field field : aFieldList) {
            aTagTable.put(field.getName() + "_label", field.getDisplayName());
        }

        return;
    }

    //~--- get methods --------------------------------------------------------

    private String getAssociationHtml(String label, String disabled, String value, StringBuilder html) throws FileNotFoundException, IOException {
        StringBuilder linkText = new StringBuilder();

        linkText.append("|&nbsp;<span class=\"l cb\" ").append("onclick=\"javascript:associateRequest();\">").append("Parent</span>&nbsp;");

        DTagReplacer hp = new DTagReplacer(HTML_ASSOC);

        hp.replace("value", value);
        hp.replace("disabled", disabled);
        html.append(hp.parse(0));

        return linkText.toString();
    }

    private String getAttachmentHtml(StringBuilder html) throws FileNotFoundException, IOException {
        StringBuilder linkText = new StringBuilder();

        linkText.append("<span class=\"l cb\" ").append("onclick=\"javascript:showAttachmentPanel();\">").append("Attachments</span>&nbsp;");

        DTagReplacer hp = new DTagReplacer(HTML_ATTACH);

        html.append(hp.parse(0));

        return linkText.toString();
    }

    /*
     */
    public static String getAutoSaveRate(WebConfig aUserConfig) {
        if (aUserConfig == null) {
            return "5";
        }

        return (Integer.toString(aUserConfig.getAutoSaveRate()));
    }

    /**
     *
     *
     *
     *
     */
    private String getBAList(int aSystemId, int aUserId) throws DatabaseException, TBitsException {
        StringBuilder           buffer = new StringBuilder();
        ArrayList<BusinessArea> baList = BusinessArea.getBusinessAreasByUserId(aUserId);

        if ((baList == null) || (baList.size() == 0)) {
            return buffer.toString();
        }

        for (BusinessArea ba : baList) {
            int sysId = ba.getSystemId();

            buffer.append("\n<OPTION VALUE='").append(sysId).append("' ");

            if (sysId == aSystemId) {
                buffer.append(" SELECTED ");
            }

            buffer.append(">").append(ba.getDisplayName()).append(" [").append(ba.getSystemPrefix()).append("] ");

            if (ba.getIsPrivate() == true) {
                buffer.append(" &dagger;");
            }

            buffer.append("</OPTION>");
        }

        return buffer.toString();
    }

    /**
     * This method returns the caller of this servlet.
     *
     * @param aPathInfo  URL used to call the servlet.
     *
     * @return Enum that denotes the caller based on the url.
     *         <UL>
     *         <LI>ADD_REQUEST
     *         <LI>ADD_ACTION
     *         <LI>ADD_SUBREQUEST
     *         </UL>
     */
    private int getCaller(String aPathInfo) {
        int index = ADD_REQUEST;

        if (aPathInfo.indexOf("add-request") > 0) {
            index = ADD_REQUEST;
        }

        if (aPathInfo.indexOf("add-action") > 0) {
            index = ADD_ACTION;
        }

        if (aPathInfo.indexOf("add-subrequest") > 0) {
            index = ADD_SUBREQUEST;
        }

        return index;
    }

    private boolean getDiffHTML(ArrayList<DiffEntry> aDiffArrayList, Hashtable<String, Object> aTagTable, HttpServletRequest aRequest, int aCurrentActionId, ArrayList<String> aAllEUCs) {
        int repliedToAction = Integer.parseInt(aRequest.getParameter("repliedToAction"));
        int requestId       = Integer.parseInt(aRequest.getParameter("requestId"));
        int systemId        = Integer.parseInt(aRequest.getParameter("systemId"));

        //
        // If IUC present, dont show diff table.
        //
        if (aAllEUCs.size() > 0) {
            aTagTable.put("showDiff", "");
            aTagTable.put("diffList", "");
            aTagTable.put("diffFieldList", "");

            return false;
        }

        String sysPrefix = aRequest.getParameter("sysPrefix");

        // Action act = Action.lookupBySystemIdAndRequestIdAndActionId(systemId,
        //                                                                  requestId,
        //                                                                  actionId);
        ArrayList<String> diffUsers = null;

        try {
            diffUsers = Action.getDiffUserList(aDiffArrayList, systemId, requestId, repliedToAction, aCurrentActionId);
        } catch (DatabaseException dbe) {
            LOG.severe("An exception has occured while retrieving the diff" + "user information", dbe);
        }

        StringBuffer strBuf        = new StringBuffer();
        String       fieldName     = null;
        String       displayName   = null;
        String       oldValue      = null;
        String       newValue      = null;
        String       diffList      = "";
        String       diffFieldList = "";

        strBuf.append("<table class='results' id=\"diffEntry\" ").append(" cellspacing=\"0\" style='layout:fixed;' ").append(" cellpadding=\"1\" width=\"780\" border=\"0\" >").append(
            "<colgroup>").append("<col width='100' valign='top'>").append("<col width='300'>").append("<col width='300'>").append("<col width='80'>").append("</colgroup>").append(
            "<tr class=\"header\">\n").append("<td class=\"b m\">Field</td>\n").append("<td>").append("<span class=\"b m\">").append("Your Change " + " ").append("<br>").append(
            "<span class=\"m u cw l\" ").append("onclick=\"javascript:onClickNewAll()\">").append("Select All").append("</span>").append("</td>\n").append("<td class=\"b m\">").append(
            "Conflicting Change [in Update #] <br>").append("<span class=\"m u cw l\" ").append("onclick=\"javascript:onClickOldAll()\">").append("Select All").append("</span>").append(
            "</td>\n").append("<td class=\"b m\">Made By</td>\n").append("</tr>");

        int size = aDiffArrayList.size();

        for (int i = 0; i < size; i++) {
            DiffEntry de           = aDiffArrayList.get(i);
            String[]  userInfo     = diffUsers.get(i).split(",");
            String    userLogin    = userInfo[0];
            String    userActionId = userInfo[1];
            String    url          = WebUtil.getServletPath(aRequest, "/q/") + sysPrefix + "/" + requestId + "/" + "action#" + userActionId;

            fieldName     = de.getName();
            displayName   = de.getDisplayName();
            oldValue      = de.getOldValue();
            newValue      = de.getNewValue();
            diffList      = diffList + fieldName + "|";
            diffFieldList = diffFieldList + oldValue + "|";
            strBuf.append("<tr>\n").append("<td class=\"b\">").append(displayName).append("</td>").append("<td id=\"").append(fieldName).append("_new\" >").append("<input id=\"" + fieldName
                          + "_newch\"").append(" type=\"radio\"  checked  name='" + fieldName + "'").append(" onclick=\"onClickField('" + fieldName
                                               + "_newch')\">").append(newValue).append("</td>\n ").append("<td id=\"").append(fieldName).append("_old\" >").append("<input id=\"" + fieldName
                                                   + "_oldch\"").append(" type=\"radio\" name='" + fieldName + "'").append(" onclick=\"onClickField('" + fieldName
                                                       + "_oldch')\">").append(oldValue).append("<a href=" + url + " >").append(" [Update#" + userActionId
                                                           + "]").append("</a>").append("</td>\n").append("<td>").append(userLogin).append("</td>\n").append("</tr>");
        }

        strBuf.append("</table>").append("<div class=\"m ac\">").append("<input style='WIDTH:70px' type=\"button\" ").append(" onclick='onSubmitDiff()' class=\"cw b bn sxs lsb\" ").append(
            " value='Submit' align='right'>").append("<input style='WIDTH:70px' type=\"button\" class=\"sx\"").append(" onclick='onApplyDiff()'").append(" value='Apply' align='right'>").append(
            "<input style='WIDTH:70px' type=\"button\" class=\"sx\"").append(" onclick='onCancelDiff()'").append(" value='Cancel' id='cancel' align='right'>").append("</div>");
        aTagTable.put("showDiff", strBuf.toString());
        aTagTable.put("diffList", diffList);
        aTagTable.put("diffFieldList", diffFieldList);

        return true;
    }

    /**
     * This method is used to get the draft values.
     *
     * @param aRequest HttpServletRequest Object
     * @param aParamInfo ParamInfo
     * @param aUser User accessing the page.
     * @param paramTable Hashtable containing <parameter, value> pairs.
     */
    private void getDraftValues(HttpServletRequest aRequest, Hashtable<String, Object> aParamInfo, User aUser, Hashtable<String, String> paramTable)
            throws ServletException, TBitsException, DatabaseException, FileNotFoundException, IOException {
        String userLogin = aUser.getUserLogin();

        int userId    = aUser.getUserId();
        int systemId  = 0;
        int requestId = 0;

        try {
            systemId = Integer.parseInt(paramTable.get(Field.BUSINESS_AREA));
        } catch (Exception e) {
            LOG.info("incorrect sys_id: draft not loaded");

            return;
        }

        BusinessArea ba        = BusinessArea.lookupBySystemId(systemId);
        String       sysPrefix = ba.getSystemPrefix();

        if (ba == null) {
            LOG.info("incorrect sys_id: draft not loaded");

            return;
        }

        try {
            requestId = Integer.parseInt(paramTable.get(Field.REQUEST));
        } catch (Exception e) {
            requestId = 0;
        }

        Timestamp timeStamp = null;

        try {
            timeStamp = new Timestamp(Long.parseLong(paramTable.get("timeStamp")));
        } catch (Exception e) {
            LOG.info("",(e));

            return;
        }

        int draftId = 0;
        try
        {
        	draftId = Integer.parseInt(paramTable.get("draftid"));
        } catch (Exception e) {
        	LOG.warn("Could not retrieve the draft id while loading the draft.");
        	LOG.info("",(e));
        	return;
        }

        UserDraft draft = null;

        try {
            draft = UserDraft.lookupByUserIdAndSystemIdAndDraftId(userId, systemId, draftId);
        } catch (Exception e) {
            LOG.info("",(e));
        }

        if (draft == null) {
            return;
        }

        if (requestId > 0) {
            Request request = Request.lookupBySystemIdAndRequestId(systemId, requestId);

            aParamInfo.put(Field.REQUEST, request);
        }

        String                    DTimestamp = aRequest.getParameter("DTimestamp");
        StringBuilder             sb         = new StringBuilder();
        Hashtable<String, String> draftHash  = DraftConfig.xmlDeSerialize(draft.getDraft());
        Enumeration<String>       keys       = draftHash.keys();
        while (keys.hasMoreElements()) {
            String key   = keys.nextElement();
            String value = draftHash.get(key);

            if ((value != null) && (value.trim().equals("") == false)) 
            {
            	if(!key.equals(Field.ATTACHMENTS))
            		paramTable.put(key, value);
            	else
            	{
            		paramTable.put(REQUEST_FILES, value) ;
            	}
            }
        }
    }

    private String getExtendedFieldsHtml(String cslist) {
        StringBuilder linkText = new StringBuilder();

        linkText.append("<span class=\"l cb\" ").append("onclick=\"javascript:addExtendedFields('").append(cslist).append("');\">").append("Extended Fields</span>&nbsp;");

        return linkText.toString();
    }

    private String getExtendedTextHtml(String label, String fieldName, String disabled, String fieldValue) throws FileNotFoundException, IOException {
        DTagReplacer  hp       = new DTagReplacer(HTML_TEXT);

        hp.replace("field_label", label);
        hp.replace("field_name", fieldName);

        if (fieldValue != null) {
            fieldValue = Utilities.htmlEncode(fieldValue);
            hp.replace("field_value", fieldValue);
        } else {
            hp.replace("field_value", "");
        }

        hp.replace("field_disabled", disabled);
        return hp.parse(0);
     }

    /**
     * This method returns the GMT Equivalent of the Time specified in
     * aTime, assuming aTime to be in aSourceZone in aSourceFormat.
     *
     * @param aTime         DateTime in string format.
     * @param aSourceZone   Zone in which aTime is valid.
     * @param aSourceFormat Format of aTime.
     *
     * @return GMT Equivalent of aTime in aSourceZone.
     */
    private Timestamp getGmtDate(String aTime, TimeZone aSourceZone, String aSourceFormat) {
        try {

            //
            // Create a SimpleDateFormat with format as the aSourceFormat.
            // Tell the pre object about the zone of the datetime we would be
            // parsing now, so that the output of parse will be the local
            // equivalent of aTime in aSourceZone.
            // eg:
            // aTime: 2004-07-20 12:10:00, aSourceZone: EST
            // Output of parse when executing in IST will be
            // 2004-07-20 22:40:00 (Assuming IST is 10:30 ahead of EST).
            //
            SimpleDateFormat pre = new SimpleDateFormat(aSourceFormat);

            pre.setTimeZone(aSourceZone);

            Date t = pre.parse(aTime);

            //
            // Now create another SimpleDateFormat object and set the time
            // zone to GMT. Format the Date object we got above and that
            // yeilds datetime string in GMT Zone.
            //
            SimpleDateFormat gmt = new SimpleDateFormat(aSourceFormat);

            gmt.setTimeZone(TimeZone.getTimeZone("GMT"));

            String temp = gmt.format(t);

            //
            // Now parse the time we got above without any zone intervention.
            // just to get the Timestamp object.
            //
            SimpleDateFormat sdf = new SimpleDateFormat(aSourceFormat);
            Timestamp        ts  = new Timestamp(sdf.parse(temp).getTime());

            return ts;

            // LOG.info("End: " + ts);
        } catch (Exception e) {
            LOG.info("getGmtDate:" + "",(e));
        }

        return new Timestamp();
    }

    /**
     * This method returns the Current Time in GMT Zone.
     *
     * @return Timestamp that holds the current datetime in GMT Zone.
     */
    private Timestamp getGmtNow() {
        try {
            SimpleDateFormat gmt = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss.SSS a");

            gmt.setTimeZone(TimeZone.getTimeZone("GMT"));

            String           temp = gmt.format(Calendar.getInstance().getTime());
            SimpleDateFormat sdf  = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss.SSS a");
            Timestamp        ts   = new Timestamp(sdf.parse(temp).getTime());

            return ts;
        } catch (Exception e) {
            LOG.info("Exception while getting the current time in GMT", e);
        }

        return new Timestamp();
    }

    /**
     * This method returns a list of HTML code snippets for displaying
     * extended fields.
     *
     * @param aFieldList  Lisf of Extended Fields.
     * @param aSystemId   Current Business Area Id.
     * @param aUserId     Current User Id.
     * @param aTagTable   Tag Table that should be filled.
     * @param aPermTable  Table of permissions.
     * @param aRequest    HttpServletRequestObject.
     *
     * @return List of HTML Code snippets to display Extended Fields.
     * @throws DatabaseException 
     */
    private ArrayList<String> getHtml(ArrayList<Field> aFieldList, int aSystemId, int aUserId, Hashtable<String, Object> aTagTable, Hashtable<String, Integer> aPermTable, Hashtable<String, String> paramTable, Hashtable<String, String> aReq, HttpServletRequest req4Context, int caller)
            throws TBitsException, DatabaseException {
        ArrayList<String> htmlList = new ArrayList<String>();

        if ((aFieldList == null) || (aFieldList.size() == 0)) {
            return htmlList;
        }

        int    tCtr             = 0;
        int    tabIndex         = 40;
        int    viewCount        = 0;
        StringBuilder extendedFieldIds = new StringBuilder();
        
        /*
         * Get the fields in this BA that participate in dependencies.
         */
        Hashtable<Integer, DependentField> depFieldTable = DependentField.getBySystemId(aSystemId);
        int                                clientOffset  = ((Integer) aTagTable.get("clientOffset")).intValue();
        TimeZone                           localZone     = getLocalZone(clientOffset);
        String                             today         = WebUtil.getDateInFormat(getGmtNow(), localZone, "MM/dd/yyyy HH:mm");

        aTagTable.put("defaultExtDate", today);

        boolean viewPrivate = false;

        if (aPermTable.get(Field.IS_PRIVATE) != null) {
            int permValue = ((Integer) aPermTable.get(Field.IS_PRIVATE)).intValue();

            if ((permValue & Permission.VIEW) == 0) {
                viewPrivate = false;
            } else {
                viewPrivate = true;
            }
        }

       StringBuilder execFunctions = new StringBuilder();
      
       //Changed by Lokesh to accomodate system Id for display groups.
       //instead of "ArrayList<DisplayGroup> allGroups = DisplayGroup.lookupAll();" 
       //which would add all the existing display groups      
       ArrayList<DisplayGroup> allGroups = DisplayGroup.lookupIncludingDefaultForSystemId(aSystemId);
       
       //Prepare a hash of id=>Group. Need to make it faster to perform grouping
       Hashtable<Integer, DisplayGroup> allGroupsHash = new Hashtable<Integer, DisplayGroup>();
       for(DisplayGroup group: allGroups)
       {
    	   allGroupsHash.put(group.getId(), group);
       }
       
        //Group the fields into various display Group. Group=>Field[]
        Hashtable<DisplayGroup, ArrayList<Field>> displayGroups = new Hashtable<DisplayGroup, ArrayList<Field>>();
        for(Field field: aFieldList)
        {
        	ArrayList<Field> groupFields;
        	int fieldDisplayGroupId = field.getDisplayGroup();
        	DisplayGroup fieldDisplayGroup = allGroupsHash.get(fieldDisplayGroupId);
        	if(fieldDisplayGroup == null)
        	{
        		int DEFAULT_DISPLAY_GROUP_ID = 1;
        		fieldDisplayGroup = allGroupsHash.get(DEFAULT_DISPLAY_GROUP_ID);
        		if(fieldDisplayGroup == null)
        			throw new NullPointerException("Default Display Group Not found.");
        	}
        	if(displayGroups.containsKey(fieldDisplayGroup))
        	{
        		groupFields = displayGroups.get(fieldDisplayGroup);
        	}
        	else {
        		groupFields = new ArrayList<Field>();
        		displayGroups.put(fieldDisplayGroup, groupFields);
        	}
        	groupFields.add(field);
        }
               
        DisplayGroup.setSortField(DisplayGroup.DISPLAYORDER);
        ArrayList<DisplayGroup> sortedDisplayGroups =  DisplayGroup.sort(displayGroups.keySet());
        String pipeSeparater = "|";
        for(DisplayGroup displayGroup: sortedDisplayGroups)
        {
        	ArrayList<Field> groupFields = displayGroups.get(displayGroup);
        	if((groupFields == null) || (groupFields.size() == 0))
        			continue;
        	
        	boolean showDisplayGroupEnvelop = displayGroup.getIsActive();
        	String displayGroupHeader = "" ;
        	if(showDisplayGroupEnvelop)
        	{
        		try {
					displayGroupHeader = renderDisplayGroupHeader(req4Context, displayGroup);
				} catch (FileNotFoundException e) {
					LOG.error("Header template could not be found. So disabling the header", e);
					e.printStackTrace();
				} catch (IOException e) {
					LOG.error("Header template could not be read. So disabling the header", e);
					e.printStackTrace();
				}
        		//Don't worry about the pipeSeparater. We are trimming it
        		extendedFieldIds.append("display_group_header_").append(displayGroup.getId()).append("_ex").append(pipeSeparater);
        		extendedFieldIds.append("display_group_footer_").append(displayGroup.getId()).append("_ex").append(pipeSeparater);
        	}
        	Field.setSortField(Field.DISPLAYORDER);
        	ArrayList<String> fieldsHtml = new ArrayList<String>() ;
        	for(Field field: Field.sort(groupFields))
        	{
	            int            fieldId       = field.getFieldId();
	            int            fieldDataType = field.getDataTypeId();
	            String         fieldName     = field.getName();
	            DependentField depField      = depFieldTable.get(fieldId);
	
	            Integer perm     = aPermTable.get(field.getName());
//	            boolean hasPerm = false ;  
	            
	            if ((aReq != null))  // then caller must be AddRequest (post)
	            {
	                String strField = paramTable.get(fieldName);
		            if( strField != null )
		            {
	                    if ( (fieldDataType != BOOLEAN) && (fieldDataType != DATE) && (fieldDataType != TIME) && (fieldDataType != DATETIME)) 
	                    {
		                    aReq.put(fieldName, strField);
		                }
		
		                if (((fieldDataType == DATE) || (fieldDataType == TIME) || (fieldDataType == DATETIME))) 
		                {
							if (!strField.trim().equals("")) 
							{
				        		String apiFormatDate = "";
				        		apiFormatDate = parseHTMLDate(strField, User.lookupAllByUserId(aUserId));				        		
				        		System.out.println("Formatted the date '" + strField + "' into : '" + apiFormatDate + "' for field '" + fieldName + "'");
				        		aReq.put(fieldName, apiFormatDate);
							}					
							else
							{
								aReq.put(fieldName, "");
							}
		                }
	
		                if (fieldDataType == BOOLEAN) 
		                {
		                    String strExtendedBit = null;
		
		                    if(strField.trim().equals("") || strField.trim().equalsIgnoreCase("false") || strField.trim().equalsIgnoreCase("unchecked") || strField.trim().equalsIgnoreCase("unselected") || strField.trim().equalsIgnoreCase("off") || strField.trim().equalsIgnoreCase("0") ) 
		                    {
		                        strExtendedBit = "false";
		                    } else
		                    {
		                        strExtendedBit = "true";
		                    }
		
		                    if (strExtendedBit != null) {
		                        aReq.put(fieldName, strExtendedBit);
		                    }
		                }
		          }
	         }
	            
	            if( null == perm )
	            {
	            	continue ;
	            }
	            else
	            {
	            	if( caller == ADD_ACTION )
	            	{
	            		if( (perm & Permission.CHANGE) == 0 )
	            			continue ; // don't show this field in update form
	            	}	            		
	            	else
	            	{
	            		if( (perm & Permission.ADD) == 0 )
	            			continue ; // don't show this field in add request form
	            	}            	
	            }
		            
	            //Don't worry about the pipeSeparater. We are trimming it
	            if (fieldDataType == DataType.TEXT) {
	                extendedFieldIds.append(fieldName + "_rteheader").append(pipeSeparater);
	            } else if (fieldDataType == DataType.TYPE) {
	                extendedFieldIds.append(fieldName + "_ex|" + fieldName).append("_exdiv").append(pipeSeparater);
	            } else {
	                extendedFieldIds.append(fieldName).append("_ex").append(pipeSeparater);
	            }
	                    
	            String disabled = "";
	           	viewCount++;
	           	/*
	             * A field should be rendered disabled irrespective of permissions
	             * if
	             *  - It is part of a dependency.
	             *  - It's role in the dependency is DEPENDENT.
	             *  - It is not a type.
	             */
	            if ((depField != null) && (depField.getDepRole() == DepRole.DEPENDENT) && (fieldDataType != DataType.TYPE)) {
	                disabled = " disabled ";
	            }
	
	            if(field.getDataTypeId()==DataType.USERTYPE){

					try{
						fieldsHtml.add(renderExtendedMultiValue(field,disabled,paramTable));
					}catch(Exception e)
					{
						e.printStackTrace();
					}

				} else if (field.getDataTypeId() == DataType.TYPE) {
	                try {
	                	fieldsHtml.add(renderExtendedType(field, aSystemId, viewPrivate, tabIndex, disabled, paramTable, aUserId, depField, req4Context, caller ));
	                } catch (DatabaseException dbe) {
	                    LOG.severe("DatabaseException", dbe);
	                }
	
	                if (tCtr != 0) {
	                    tabIndex = tabIndex + 1;
	                }
	
	                tCtr++;
	            } else if (field.getDataTypeId() == DataType.BOOLEAN) {
	                try {
	                	fieldsHtml.add(renderExtendedBit(field, tabIndex++, disabled, execFunctions, paramTable, req4Context, caller));
	                } catch (DatabaseException dbe) {
	                    LOG.severe("DatabaseException", dbe);
	                }
	
	                if (tCtr != 0) {
	                    tabIndex = tabIndex + 1;
	                }
	
	                tCtr++;
	            } else if ((field.getDataTypeId() == DataType.DATETIME) || (field.getDataTypeId() == DataType.DATE) || (field.getDataTypeId() == DataType.TIME)) {
	                try {
	                	fieldsHtml.add(renderExtendedDate(field, "", tabIndex++, disabled, execFunctions, paramTable, req4Context, caller));
	                } catch (DatabaseException dbe) {
	                    LOG.severe("DatabaseException", dbe);
	                }
	
	                if (tCtr != 0) {
	                    tabIndex = tabIndex + 1;
	                }
	
	                tCtr++;
	            } else if ((field.getDataTypeId() == DataType.INT) || (field.getDataTypeId() == DataType.REAL)) {
	                try {
	                	fieldsHtml.add(renderExtendedInt(field, tabIndex++, disabled, execFunctions, paramTable, req4Context, caller));
	                } catch (DatabaseException dbe) {
	                    LOG.severe("DatabaseException", dbe);
	                }
	
	                if (tCtr != 0) {
	                    tabIndex = tabIndex + 1;
	                }
	
	                tCtr++;
	            } else if (field.getDataTypeId() == DataType.STRING) {
	                try {
	                	fieldsHtml.add(renderExtendedString(field, tabIndex++, disabled, execFunctions, paramTable, req4Context, caller));
	                } catch (DatabaseException dbe) {
	                    LOG.severe("DatabaseException", dbe);
	                }
	
	                if (tCtr != 0) {
	                    tabIndex = tabIndex + 1;
	                }
	
	                tCtr++;
	            } else if (field.getDataTypeId() == DataType.TEXT) {
	                try {
	                	fieldsHtml.add(renderExtendedText(field, tabIndex++, disabled, execFunctions, paramTable, req4Context, caller));
	                } catch (DatabaseException dbe) {
	                    LOG.severe("DatabaseException", dbe);
	                }
	
	                if (tCtr != 0) {
	                    tabIndex = tabIndex + 1;
	                }
	
	                tCtr++;
	            }
	        }
        	//Show display group envelop only if the display group is active and it has fields too.
        	showDisplayGroupEnvelop = (showDisplayGroupEnvelop && fieldsHtml.size() > 0 );
        	
	    	if(showDisplayGroupEnvelop )
        	{    
	    			htmlList.add(displayGroupHeader);
	    	}
	    	if(fieldsHtml.size() > 0 )
	    	{
	    		htmlList.addAll(fieldsHtml);
	    	}
        	if(showDisplayGroupEnvelop)
        	{    
	    		try
	    		{
	    			htmlList.add(renderDisplayGroupFooter(req4Context, displayGroup));
	    		} catch (FileNotFoundException e) {
					LOG.error("Footer template could not be found. So disabling the header", e);
					e.printStackTrace();
				} catch (IOException e) {
					LOG.error("Footer template could not be read. So disabling the header", e);
					e.printStackTrace();
				}
        	}
        }

        aTagTable.put("execFunctions", execFunctions.toString());

        //remove the last | from extended FieldIds
    	int indexOfPipe = extendedFieldIds.lastIndexOf(pipeSeparater);
    	if((indexOfPipe >= 0) && (indexOfPipe == (extendedFieldIds.length() - pipeSeparater.length())))
    	{
    		int end = extendedFieldIds.length();
    		StringBuilder str = extendedFieldIds.delete(indexOfPipe, end);
    	}
        if (viewCount > 0) {
        	aTagTable.put("efLink", getExtendedFieldsHtml(extendedFieldIds.toString()));
        }

        aTagTable.put("csList", extendedFieldIds.toString());

        return htmlList;
    }

    private String renderDisplayGroupHeader(HttpServletRequest aRequest, DisplayGroup displayGroup) throws FileNotFoundException, IOException {
    	StringBuilder html = new StringBuilder();
		DTagReplacer hp = new DTagReplacer(HTML_DISPLAY_GROUP_HEADER);
		hp.replace("display_name", displayGroup.getDisplayName());
		hp.replace("display_group_id", "" + displayGroup.getId());
		hp.replace("nearestPath", WebUtil.getNearestPath(aRequest, ""));
		html.append(hp.parse(0));
		return html.toString();
    }

    private String renderDisplayGroupFooter(HttpServletRequest aRequest, DisplayGroup displayGroup) throws FileNotFoundException, IOException {
    	StringBuilder html = new StringBuilder();
		DTagReplacer hp = new DTagReplacer(HTML_DISPLAY_GROUP_FOOTER);
		hp.replace("display_name", displayGroup.getDisplayName());
		hp.replace("display_group_id", "" + displayGroup.getId());
		hp.replace("nearestPath", WebUtil.getNearestPath(aRequest, ""));
		html.append(hp.parse(0));
		return html.toString();
	}

	
	/**
     * Method to return client local zone based on the given client offset.
     *
     * @param aClientOffset ZoneOffset for client's location.
     *
     * @return TimeZone object for this zoneoffset.
     */
    private TimeZone getLocalZone(int aClientOffset) {
        TimeZone localZone = null;

        if (aClientOffset == 240) {
            localZone = TimeZone.getTimeZone("EST5EDT");
        } else if (aClientOffset == 300) {
            localZone = TimeZone.getTimeZone("EST");
        } else if (aClientOffset == -330) {
            localZone = TimeZone.getTimeZone("IST");
        } else {
            aClientOffset = -1 * aClientOffset;

            String[] zones = TimeZone.getAvailableIDs(aClientOffset * 60000);

            if (zones.length == 0) {
                localZone = (Calendar.getInstance()).getTimeZone();
            } else {
                localZone = TimeZone.getTimeZone(zones[0]);
            }
        }

        return localZone;
    }

    private String getMemoHtml(int caller, String label, String memo, StringBuilder html) throws FileNotFoundException, IOException {
        StringBuilder linkText = new StringBuilder();

        linkText.append("|&nbsp;<span class=\"l cb\" ").append("onclick=\"javascript:controlMemo();\">");

        if (caller == ADD_ACTION) {
            linkText.append("Update Memo");
        } else {
            linkText.append("Add Memo");
        }

        linkText.append("</span>&nbsp;");

        DTagReplacer hp = new DTagReplacer(HTML_MEMO);

        hp.replace("label", label);
        hp.replace("value", Utilities.htmlEncode(memo));
        html.append(hp.parse(0));

        return linkText.toString();
    }

    /**
     * This method returns the table of [parameter, value] depending on the
     * content-type of the request.
     * <UL>
     * <LI>
     * If the content type is www-url-encoded then it obtains this table from
     * the HttpServletRequest object.
     * </LI>
     * <LI>
     * If the content-type is multipart/form-data, then the HttpServletRequest
     * object is used to create a MultiPartParser which is then iterated to
     * get the param parts. It stores the attachments in the application's
     * temporary directory (transbit.tbits.tmpdir);
     * </LI>
     * </UL>
     *
     *
     * @param aReq   HttpSerlvetRequest Object.
     * @param aLogin Login of the user.
     * @param paramTable Out parameter.
     *
     * @exception ServletException
     * @exception IOException
     *
     */
    public static void getParamTable(HttpServletRequest aReq, String aLogin, Hashtable<String, String> paramTable) throws ServletException, IOException {

        // Hashtable<String, String> paramTable = new Hashtable<String, String>();
        // Content type of the request.
        String contentType = aReq.getContentType();

        // Holds the new-line separated list of attachments if any.
        StringBuilder attList = new StringBuilder();

        // Holds the attachment name in the table.
        Hashtable<String, Boolean> table = new Hashtable<String, Boolean>();

        // Counter used incase of files with same names.
        int counter = 0;

        //
        // If the content type is multipart/form-encoded, then obtain a
        // multipart request from this request object.
        //
        if ((contentType != null) && (contentType.startsWith(MULTIPART_CONTENT_TYPE) == true)) {
            MultipartParser parser = null;

            try {
                parser = new MultipartParser(aReq, 1024 * 1024 * 1024);    // 1GB
            } catch (IOException e) {
                LOG.severe("",(e));
            }

            if (parser != null) {
                Part part = null;

                // Iterate the parts in the parser and process them accordingly
                while ((part = parser.readNextPart()) != null) {
                    if (part instanceof ParamPart) {
                        ParamPart pp         = (ParamPart) part;
                        String    paramName  = pp.getName();
                        String    paramValue = pp.getStringValue();

                        paramTable.put(paramName, paramValue);
                    }

//                    if (part instanceof FilePart) {
//                        String attInfo = handleFilePart((FilePart) part, aLogin, counter, table);
//
//                        counter++;
//                        attList.append(attInfo);
//                    }
                }
            }

//            paramTable.put(Field.ATTACHMENTS, attList.toString());
        } else {
            Enumeration fieldList = aReq.getParameterNames();

            while (fieldList.hasMoreElements()) {
                String fieldName  = (String) fieldList.nextElement();
                String fieldValue = aReq.getParameter(fieldName);

                paramTable.put(fieldName, fieldValue);
            }
        }

        // return paramTable;
    }

    private String getRelatedHtml(int aSystemId, String label, String disabled, String value, StringBuilder html) throws FileNotFoundException, IOException {
        StringBuilder linkText = new StringBuilder();
        //Pick systemId specific captions hashmap, if not found pick from common hashMap whose systemId is 0.        
        String linkCaption = Utilities.getCaptionBySystemId(aSystemId, CaptionsProps.CAPTIONS_ADD_LINK_REQUESTS);
        
        linkText.append("|&nbsp;<span class=\"l cb\" ").append("onclick=\"javascript:addRelatedRequests();\">")
        .append(linkCaption).append("</span>&nbsp;");

        DTagReplacer hp = new DTagReplacer(HTML_RELATED);

        hp.replace("value", value);
        hp.replace("disabled", disabled);
        html.append(hp.parse(0));

        return linkText.toString();
    }
    
    private String getSummaryHtml(int caller, int aSystemId, String label, String summary, StringBuilder html) throws FileNotFoundException, IOException {
        StringBuilder linkText = new StringBuilder();
        linkText.append("|&nbsp;<a href =\"#\" class=\"l cb\" ").append(" id=\"ctrlsum\">");
        //Pick systemId specific captions hashmap, if not found pick from common hashMap whose systemId is 0.
        HashMap<String, String> captionsHash = CaptionsProps.getInstance().getCaptionsHashMap(aSystemId);
        String linkCaption = "";       
        
        if (caller == ADD_ACTION) {
            if ((summary != null) && (!(summary.trim().equals("")))) {
                linkCaption = captionsHash.get(CaptionsProps.CAPTIONS_ADD_SUMMARY);
            } else {
            	linkCaption = captionsHash.get(CaptionsProps.CAPTIONS_UPDATE_SUMMARY);
            }
        } else {
        	linkCaption = captionsHash.get(CaptionsProps.CAPTIONS_ADD_SUMMARY);
        }
        
        linkText.append(linkCaption);
        linkText.append("</a>&nbsp;");

        DTagReplacer hp = new DTagReplacer(HTML_SUMMARY);

        hp.replace("label", label);
        hp.replace("value", Utilities.htmlEncode(summary));
        html.append(hp.parse(0));

        return linkText.toString();
    }

    /**
     * This method retrieves the list of Types for given field based on the
     * permissions the user has and returns and HTML Options List.
     *
     * @param aSystemId       Current Business Area Id.
     * @param aFieldName      Name of the Field.
     * @param aViewPrivate    PrivateType.
     *
     * @return HTML Options List of types for this field .
     */
//    private String getTypeList(int aSystemId, String aFieldName, boolean aViewPrivate, User aUser) throws TBitsException, DatabaseException {
//        StringBuilder   buffer   = new StringBuilder();
//        ArrayList<Type> types    = Type.lookupBySystemIdAndFieldName(aSystemId, aFieldName);
//
//        for (Type type : types) {
//
//            // Skip this if it is inactive.
//            if (type.getIsActive() == false) {
//                continue;
//            }
//
//            // Skip this type if it is final.
//            if (type.getIsFinal() == true) {
//                continue;
//            }
//
//            // Skip the type if it is private and the user does not have
//            // view permissions.
//            if ((type.getIsPrivate() == true) && (aViewPrivate == false)) {
//                continue;
//            }
//
//            buffer.append("<OPTION value=\"").append(Utilities.htmlEncode(type.getName())).append("\" ");
//
//            if (type.getIsDefault()) {
//                buffer.append(" SELECTED ");
//            }
//
//            buffer.append(">").append(Utilities.htmlEncode(type.getDisplayName()));
//
//            if (type.getIsPrivate() == true) {
//                buffer.append(" &dagger;");
//            }
//
//            buffer.append("</OPTION>\n");
//        }
//
//        return buffer.toString();
//    }

    /**
     * This method retrieves the list of Types for given field based on the
     * permissions the user has and returns and HTML Options List.
     *
     * @param aSystemId       Current Business Area Id.
     * @param aFieldName      Name of the Field.
     * @param aViewPrivate    True if user can view private items.
     * @param aType           Type to be selected.
     *
     * @return HTML Options List of types for this field .
     */
    private String getTypeList(int aSystemId, String aFieldName, boolean aViewPrivate, String aType, int page) throws TBitsException, DatabaseException {
        StringBuilder   buffer              = new StringBuilder();
        ArrayList<Type> types               = Type.lookupBySystemIdAndFieldName(aSystemId, aFieldName);
        
        Type selectedType = null ;
        
        if( null != aType )
        {
	        selectedType = Type.lookupBySystemIdAndFieldNameAndTypeName(aSystemId, aFieldName, aType.trim());
	    //    String          currentSelectedType = aType.trim();
	        
	        if( (null != selectedType) && (selectedType.getIsFinal() == true) && (page == ADD_ACTION) )
	        {
	        	buffer.append("<OPTION value=\"").append(Utilities.htmlEncode(selectedType.getName())).append("\" ").append(">").append(Utilities.htmlEncode(selectedType.getDisplayName()));
	
	            if (selectedType.getIsPrivate() == true){
	                buffer.append(" &dagger;");
	            }
	
	            buffer.append("</OPTION>\n");
	            return buffer.toString();            
	        }
        }
        
        for (Type type : types) {

            // Skip this if it is inactive.
            if (type.getIsActive() == false) {
                continue;
            }

            // Skip this type if it is final.
            if (type.getIsFinal() == true) {
                continue;
            }

            // Skip the type if it is private and the user does not have
            // view permissions.
            if ((type.getIsPrivate() == true) && (aViewPrivate == false)) {
                continue;
            }

            buffer.append("<OPTION value=\"").append(Utilities.htmlEncode(type.getName())).append("\" ");

            if( null != selectedType && type.getName().equals(selectedType.getName()) )
            {           
	           buffer.append(" SELECTED");
	        }

            buffer.append(">").append(Utilities.htmlEncode(type.getDisplayName()));

            if (type.getIsPrivate() == true) {
                buffer.append(" &dagger;");
            }

            buffer.append("</OPTION>\n");
        }

        return buffer.toString();
    }

    /**
     * TODO: Document this method's purpose.
     *
     *
     * @param aSystemId
     * @param aPrimField
     * @param aSecField
     * @param aPrimTypeName
     * @param aTypeMappingList
     * @param aViewPrivate
     * @param aTypeOptionList
     * @param aJSONList
     * @throws TBitsException
     * @throws DatabaseException
     */
    private void getTypeList(int aSystemId, Field aPrimField, Field aSecField, String aPrimTypeName, ArrayList<TypeDependencyMap> aTypeMappingList, boolean aViewPrivate, StringBuffer aTypeOptionList,
                             StringBuffer aJSONList)
            throws TBitsException, DatabaseException {
        String                  primFieldName   = aPrimField.getName();
        String                  secFieldName    = aSecField.getName();
        ArrayList<Type>         primTypeList    = Type.lookupAllBySystemIdAndFieldName(aSystemId, primFieldName);
        ArrayList<Type>         secTypeList     = Type.lookupAllBySystemIdAndFieldName(aSystemId, secFieldName);
        Hashtable<String, Type> secTypeTable    = new Hashtable<String, Type>();
        StringBuffer            jsonList        = new StringBuffer();
        StringBuffer            completeSecList = new StringBuffer();
        boolean                 clFirst         = true;

        /*
         * Prepare a table of [Name, Type] of the secondary type list.
         * During this process, prepare the complete list of secondary types
         * as a JSON object. This complete list will be used for primary types
         * that do not have a mapping to secondary type list.
         */
        for (Type type : secTypeList) {

            // Skip inactive types.
            if (type.getIsActive() == false) {
                continue;
            }

            // Skip if the type is private and user is not authorized to see
            // them
            if ((type.getIsPrivate() == true) && (aViewPrivate == false)) {
                continue;
            }

            secTypeTable.put(type.getName(), type);

            String name        = type.getName();
            String displayName = type.getDisplayName();

            /*
             * Check if this is the first record into the complete JSON list.
             */
            if (clFirst == false) {
                completeSecList.append(",");
            } else {
                clFirst = false;
            }

            completeSecList.append("\n\t\t{\"name\":\"").append(Utilities.htmlEncode(name)).append("\", \"display_name\":\"").append(Utilities.htmlEncode(displayName)).append("\"}");
        }

        Hashtable<String, TypeDependencyMap> typeMapTable = new Hashtable<String, TypeDependencyMap>();

        /*
         * Prepare a table of [TypeName, DepMap] from the type maps.
         */
        for (TypeDependencyMap tdm : aTypeMappingList) {
            typeMapTable.put(tdm.getPrimaryType().toUpperCase(), tdm);
        }

        /*
         * Iterate through the primary type list
         *  a. Skip inactive types.
         *  b. Skip types private for the user.
         *  c. Check if this is selected.
         *  d. Check if the type is private
         *  e. Form the Option tag for this type and append to type option list.
         *  f. Check if this primary type has any specific mapping to the
         *     dependent type list. If so, form a json object with name and
         *     display name as the members.
         */
        for (Type type : primTypeList) {

            // Skip inactive types.
            if (type.getIsActive() == false) {
                continue;
            }

            // Skip if private and user is not authorized to see them.
            if ((type.getIsPrivate() == true) && (aViewPrivate == false)) {
                continue;
            }

            String selected        = ((type.getName().equalsIgnoreCase(aPrimTypeName) == true)
                                      ? " SELECTED "
                                      : "");
            String privateSymbol   = (type.getIsPrivate() == true)
                                     ? " &dagger;"
                                     : "";
            String primName        = type.getName();
            String primDisplayName = type.getDisplayName();

            aTypeOptionList.append("<OPTION value=\"").append(Utilities.htmlEncode(primName)).append("\" ").append(selected).append(">").append(Utilities.htmlEncode(primDisplayName)).append(
                privateSymbol).append("</OPTION>\n");

            /*
             * Check if this in the dependency map. If there is a mapping
             * present for this primary type, then form the JSON object
             * with name and display_name members of each of the dependent
             * types.
             */
            TypeDependencyMap tdm = typeMapTable.get(primName.toUpperCase());

            if (tdm != null) {
                ArrayList<String> depList = tdm.getDependentList();

                if ((depList != null) && (depList.size() != 0)) {
                    jsonList.append(",").append("\n\t\"").append(Utilities.htmlEncode(primName)).append("\":").append("\n\t[");

                    boolean typeFirst = true;

                    for (String secTypeName : depList) {
                        Type secType = secTypeTable.get(secTypeName);

                        if (secType == null) {
                            continue;
                        }

                        String name = secType.getName();
                        String disp = secType.getDisplayName();

                        if (typeFirst == false) {
                            jsonList.append(",");
                        } else {
                            typeFirst = false;
                        }

                        jsonList.append("\n\t\t{\"name\":\"").append(Utilities.htmlEncode(name)).append("\", \"display_name\":\"").append(Utilities.htmlEncode(disp)).append("\" }");
                    }

                    jsonList.append("\n\t]");
                }
            } else {

                // LOG.info("No Map for " + primName);
            }
        }    // End For

        /*
         * If both jsonList and completeList are not empty then form a
         * valid JSON variable and put this in the OUT parameter aJSONList.
         */
        if ((jsonList.toString().trim().equals("") == false) && (completeSecList.toString().trim().equals("") == false)) {
            aJSONList.append("var ").append(primFieldName).append("_map = ").append("\n{").append("\n\t\"__complete_list__\":").append("\n\t[").append(completeSecList.toString()).append(
                "\n\t]").append(jsonList.toString()).append("\n};");
        }

        return;
    }

    //~--- set methods --------------------------------------------------------

    /*
     */
    public static void setInstanceBold(HttpServletRequest aRequest, Hashtable<String, Object> aTagTable, String aSysPrefix) {

        // onmouseover handler.
        String onMouseOver = "this.T_STICKY = true; " + "this.T_FIX = [gal(this) + 15, gat(this) + 20];" + "this.T_TEMP = 0; " + "this.T_WIDTH  = '250px';" + "return escape(";
        String hydUrl      = PropertiesHandler.getProperty("transbit.tbits.hydUrl");
        String nycUrl      = PropertiesHandler.getProperty("transbit.tbits.nycUrl");

        if (PropertiesHandler.getProperty(KEY_DOMAIN).toLowerCase().equals("hyd")) {
            aTagTable.put("instanceBoldHyd", " b ");
            aTagTable.put("instanceBoldNyc", " ");
            aTagTable.put("instancePathHyd", WebUtil.getServletPath(aRequest, "/search/") + aSysPrefix);
            aTagTable.put("instancePathNyc", WebUtil.getServletPath(aRequest, "/search/"));
            aTagTable.put("onMouseOverNyc", "onmouseover=\"" + onMouseOver + "showBAList" + "('" + nycUrl + "'," + "'add-request'));\"" + " onmouseout=\"setMouseOutTimer();\" ");
            aTagTable.put("onMouseOverHyd", "onmouseover=\"" + onMouseOver + "showBAList" + "('" + hydUrl + "'," + "'add-request'));\"" + " onmouseout=\"setMouseOutTimer();\" ");
            aTagTable.put("target_search", "search_hyd");
        } else {
            aTagTable.put("instanceBoldHyd", " ");
            aTagTable.put("instanceBoldNyc", " b ");
            aTagTable.put("instancePathHyd", hydUrl + "/search");
            aTagTable.put("instancePathNyc", "/search/" + aSysPrefix);
            aTagTable.put("onMouseOverHyd", "onmouseover=\"" + onMouseOver + "showBAList" + "('" + hydUrl + "'," + "'add-request'));\"" + " onmouseout=\"setMouseOutTimer();\" ");
            aTagTable.put("target_search", "search_nyc");
        }
    }
}
