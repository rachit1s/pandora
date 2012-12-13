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
 * TBitsMailer.java
 *
 * $Header:
 */
package transbit.tbits.mail;

//~--- non-JDK imports --------------------------------------------------------

import static transbit.tbits.Helper.ActionHelper.FIRST_ACTION_CLASS;
import static transbit.tbits.Helper.ActionHelper.NEW_ACTION_CLASS;
import static transbit.tbits.Helper.ActionHelper.READ_ACTION_CLASS;
import static transbit.tbits.Helper.ActionHelper.READ_ACTION_HEADER_CLASS;
import static transbit.tbits.Helper.ActionHelper.TEXT_CLASS;
import static transbit.tbits.Helper.ActionHelper.TEXT_CONTENT_CLASS;
import static transbit.tbits.Helper.ActionHelper.TEXT_HEADER_CLASS;
import static transbit.tbits.Helper.TBitsConstants.NO_TOOLTIP;
import static transbit.tbits.Helper.TBitsConstants.PKG_MAIL;
import static transbit.tbits.Helper.TBitsConstants.SOURCE_EMAIL;
import static transbit.tbits.Helper.TBitsConstants.STATIC_TOOLTIP;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Serializable;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.TreeMap;

import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import javax.mail.Part;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMultipart;

import transbit.tbits.Helper.ActionHelper;
import transbit.tbits.Helper.LinkFormatter;
import transbit.tbits.Helper.SerialObjectCloner;
import transbit.tbits.Helper.TBitsConstants;
import transbit.tbits.Helper.TBitsPropEnum;
import transbit.tbits.api.APIUtil;
import transbit.tbits.common.ActionFileInfo;
import transbit.tbits.common.Configuration;
import transbit.tbits.common.DTagReplacer;
import transbit.tbits.common.DatabaseException;
import transbit.tbits.common.Mail;
import transbit.tbits.common.PropertiesHandler;
import transbit.tbits.common.TBitsLogger;
import transbit.tbits.common.Timestamp;
import transbit.tbits.common.Uploader;
import transbit.tbits.common.Utilities;
import transbit.tbits.common.PropertiesHandler.MailProperties;
import transbit.tbits.config.BAConfig;
import transbit.tbits.config.NotificationConfig;
import transbit.tbits.config.NotifyRule;
import transbit.tbits.config.SysConfig;
import transbit.tbits.config.WebConfig;
import transbit.tbits.domain.Action;
import transbit.tbits.domain.BusinessArea;
import transbit.tbits.domain.DataType;
import transbit.tbits.domain.DateTimeFormat;
import transbit.tbits.domain.DisplayGroup;
import transbit.tbits.domain.Field;
import transbit.tbits.domain.HolidaysList;
import transbit.tbits.domain.NotificationRule;
import transbit.tbits.domain.Permission;
import transbit.tbits.domain.RelatedRequest;
import transbit.tbits.domain.Request;
import transbit.tbits.domain.RequestUser;
import transbit.tbits.domain.RolePermission;
import transbit.tbits.domain.TransferredRequest;
import transbit.tbits.domain.Type;
import transbit.tbits.domain.TypeUser;
import transbit.tbits.domain.User;
import transbit.tbits.domain.UserType;
import transbit.tbits.exception.TBitsException;
import transbit.tbits.webapps.WebUtil;

//~--- classes ----------------------------------------------------------------

class PrefixComparator implements Comparator<String>, Serializable {
    public int compare(String obj1, String obj2) {
        obj1 = obj1.toUpperCase();
        obj2 = obj2.toUpperCase();

        //
        // Related Requests are of the form prefix#NNN.
        //
        String prefix1 = obj1.substring(0, obj1.indexOf("#"));

        obj1 = obj1.substring(obj1.indexOf("#") + 1);

        int id1 = 0;

        if (obj1.indexOf("#") != -1) {
            id1 = Integer.parseInt(obj1.substring(obj1.indexOf("#") + 1));
        } else {
            id1 = Integer.parseInt(obj1);
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

        diff = prefix1.compareTo(prefix2);

        if (diff == 0) {
            diff = new Integer(id1).compareTo(new Integer(id2));
        }

        return diff;
    }
}


/**
 * This class sends a mail to the analysts whenever a new request is logged
 * or a new action is done.
 *
 *
 * @author Vinod Gupta, Nitiraj
 * @version $Id: $
 */
public class TBitsMailer implements TBitsPropEnum, TBitsConstants {
    public static final TBitsLogger LOG                      = TBitsLogger.getLogger(PKG_MAIL);
    
    private static final String IMG_STATIC_PATH = "/web/image/";
	private  String     IMG_PATH                 = null;
    private static final String     HTML_FILE                = "web/tbits-email-template.htm";
    private static final String     EXTERNAL_HTML_FILE       = "web/tbits-external-email-template.htm";
    private static final String     CSS_FILE                 = "web/css/";
    private static final String     ASSOCIATED_REQUESTS_HTML = "\n<div class=\"dm bh h\">\n<div class=\"dp\">\n" + "<table cellpadding=\"0\"" + "cellspacing=\"0\">\n<tbody>\n"
                                                               + "<=siblingRequestsDetails>" + "\n<=subRequestsDetails>\n<=relatedRequestsDetails>\n</tbody>" + "\n</table>\n</div>\n</div>";
    private String EXTENDED_FIELD_HTML = "\n<td style=\"font-weight:bold;font-family:verdana;color:#536291;\" align=\"left\" width=\"16%\">" + "<=label></td>" 
    										+ "\n<td style=\"font-family: tahoma, arial, verdana, trebuchet ms, sans-serif;font-family:verdana;\" align=\"left\" width=\"16%\"><=value></td>";
    private String EXTENDED_FULL_LENGTH_FIELD_HTML = "\n<td style=\"font-weight:bold;font-family:verdana;color:#536291;\" align=\"left\" width=\"16%\">" + "<=label></td>" 
	+ "\n<td style=\"font-family: tahoma, arial, verdana, trebuchet ms, sans-serif;font-family:verdana;\" align=\"left\" width=\"82%\"  colspan='5' ><=value></td>";

    //~--- fields -------------------------------------------------------------

    private HashSet<User>             myRemovedUsers       = new HashSet<User>();
    private String                      myXTBitsRecepients = "";
    private String                      mySmartLink          = "";
//    private ArrayList<Action>           myActionList;
    private BusinessArea                myBusinessArea;
//    private boolean                     myIsPrivate;
//    private Hashtable<String, Integer>  myPermissions;
//    private Hashtable<Integer, Integer> myPrivatePermissions;
    private Request                     myRequest;
    private SysConfig                   mySysConfig;
    private Hashtable<Integer, Collection<ActionFileInfo>> myActionFileHash;
    private ArrayList<Action> myActionList ;
//    private User                        myUser;
    private String     PATH;
    //~--- methods ------------------------------------------------------------

    private ArrayList<User> failedMailUserList = new ArrayList<User>() ;
    
    public TBitsMailer( Request request ) throws DatabaseException
    {
    	if( null == request )
    		throw new IllegalArgumentException("The request object to TBitsMailer was null.");
    	
    	myRequest = request ;

    	myBusinessArea = BusinessArea.lookupBySystemId(myRequest.getSystemId());
    	
		if( null == myBusinessArea )
    		throw new IllegalArgumentException("Cannot find the business area associated with the request : " + request);
		
//		mySmartLink = 
			
		isClassicUI = WebUtil.isClassicInterface();
		
		if(myRequest.getContext() != null)
        {
        	IMG_PATH = WebUtil.getNearestPath(myRequest.getContext(), IMG_STATIC_PATH);
        	PATH = WebUtil.getServletPath(myRequest.getContext(), "");
        }
        else
        {
        	IMG_PATH = WebUtil.getNearestPath(IMG_STATIC_PATH);
        	PATH = WebUtil.getServletPath("");
        }
		
        int systemId = myRequest.getSystemId();
		myBusinessArea = BusinessArea.lookupBySystemId(systemId);

//        String sysPrefix = myBusinessArea.getSystemPrefix();

        mySysConfig = myBusinessArea.getSysConfigObject();
		mySmartLink = myBusinessArea.getSystemPrefix() + "#" + myRequest.getRequestId() + "#" + myRequest.getMaxActionId();
		
		myActionFileHash = Action.getAllActionFiles(myRequest.getSystemId(), myRequest.getRequestId());
		
		myActionList = Action.getAllActions(myRequest.getSystemId(), myRequest.getRequestId(), "asc");
    }
    
    public ArrayList<Action> getActionList()
    {
    	return myActionList ;
    }
    
    public Hashtable<Integer, Collection<ActionFileInfo>> getActionFileHash()
    {
    	return myActionFileHash ;
    }
    
    public Request getRequest()
    {
    	return myRequest ;
    }
    /**
     */
    
    public Collection<User> getFailedMailUserList()
    {
    	return failedMailUserList ;
    }
    
    public Collection<User> getRemovedUsers()
    {
    	return myRemovedUsers ;
    }
    
    private String addHeader(String fieldDisplayName, String aValue,  int aMaxNameLength) 
    {

    	StringBuffer aTextContents = new StringBuffer() ;      
        aTextContents.append(fieldDisplayName);

        int tempLength = fieldDisplayName.length();

        for (int a = 0; a < (aMaxNameLength - tempLength); a++) {
            aTextContents.append(" ");
        }

        aTextContents.append(" ").append(aValue).append("\n");
        
        return aTextContents.toString() ;
    }

    /**
     * @throws DatabaseException 
     */
    private void addRequestUsersToList(Collection<User> aUsersSet, Collection<RequestUser> aRequestUsers) throws DatabaseException 
    {
        for (RequestUser ru : aRequestUsers) 
        {
            User user = ru.getUser();
            aUsersSet.add(user);
        }
    }

    /**
     */
//    private void checkAccessPermissions(HashSet<User> userList) {
//        int             permission   = 0;
//        ArrayList<User> removedUsers = new ArrayList<User>();
//
//        // if this request is private then check
//        if (myRequest.getIsPrivate() == true) 
//        {
//        	// check users list
//        	for (User user : userList) 
//        	{
//                if (user.getIsActive() == true) {
//                    Integer userPerm = myPrivatePermissions.get(new Integer(user.getUserId()));
//
//                    permission = (userPerm == null)
//                                 ? 0
//                                 : userPerm.intValue();
//
//                    // remove users that don't have view permission for private request
//                    if ((permission & Permission.VIEW) == 0) {
//                        removedUsers.add(user);
//                    }
//                }
//            }
//
//        	myRemovedUsers.addAll(removedUsers) ;
//            userList.removeAll(removedUsers);
//        }
//    }

    /**
     */
    private static User checkNotificationRules(TypeUser aTypeUser, Request request) throws Exception {
        NotificationRule ruleId = NotificationRule.lookupByNotificationRuleId(aTypeUser.getNotificationId());

        LOG.info( ": Notification Rule for [" + aTypeUser.getUser().getUserLogin() + ((ruleId == null)
                ? " {invalid ruleId " + aTypeUser.getNotificationId() + "} "
                : " {" + ruleId.getDisplayName() + "} ") + " ]");

        //
        // If no rule_id associated, don't include user.
        //
        if (ruleId == null) {
            return null;
        }

        NotificationConfig config = ruleId.getNotificationConfigObject();

        if (config.getSendMail().equalsIgnoreCase("never")) {
            LOG.info("Rule passed: " + aTypeUser.getUser().getUserLogin() + " not added as TypeUser Recepient");

            return null;
        }

        if (config.getSendMail().equalsIgnoreCase("always")) {
            LOG.info("Rule passed: " + aTypeUser.getUser().getUserLogin() + " added as TypeUser Recepient");

            return aTypeUser.getUser();
        }

        if (config.getSendMail().equalsIgnoreCase("rules")) {
            ArrayList<NotifyRule> rules    = config.getNotifyRules();
            boolean               nrStatus = false;

            for (NotifyRule nr : rules) {
                ArrayList<String> days = Utilities.toArrayList(nr.getDay());

                //
                // Get updatetime in user chosen zone.
                //
                Calendar cal = Calendar.getInstance();

                cal.setTime(Timestamp.toPreferredZone(request.getLastUpdatedDate(), nr.getZone()));

                if (nr.getDay().equalsIgnoreCase("holiday")) {
                    try {
                        HolidaysList hl = HolidaysList.lookupByDateAndZone(cal.getTime(), nr.getZone());

                        // if (day is holiday) notify user
                        if (hl != null) {
                            LOG.info("Processing Rule: " + nr.toString() + " : true");

                            return aTypeUser.getUser();
                        } else {
                            LOG.info("Processing Rule: " + nr.toString() + " : false");

                            continue;
                        }
                    } catch (Exception e) {
                        LOG.debug("",(e));

                        continue;
                    }
                }

                //
                // Check if day is valid, else return null.
                //
                if (days.contains(getDay(cal.get(Calendar.DAY_OF_WEEK))) == false) {
                    LOG.info("Processing Rule: " + nr.toString() + " : false");

                    continue;
                }

                //
                // Check if updateTime > startTime, else return null.
                //
                if (compareTimeStr(nr.getStartTime(), cal.get(Calendar.HOUR_OF_DAY) + ":" + cal.get(Calendar.MINUTE)) > 0) {
                    LOG.info("Processing Rule: " + nr.toString() + " : false");

                    continue;
                }

                //
                // Check if updateTime < endTime, else return null.
                //
                if (compareTimeStr(nr.getEndTime(), cal.get(Calendar.HOUR_OF_DAY) + ":" + cal.get(Calendar.MINUTE)) < 0) {
                    LOG.info("Processing Rule: " + nr.toString() + " : false");

                    continue;
                }

                //
                // Rule passed, return user.
                //
                nrStatus = true;
                LOG.info("Processing Rule: " + nr.toString() + " : true");

                break;
            }

            if (nrStatus == true) {
                LOG.info("Rule passed: " + aTypeUser.getUser().getUserLogin() + " added as TypeUser Recepient");

                return aTypeUser.getUser();
            }
        }

        LOG.info("Rule failed: " + aTypeUser.getUser().getUserLogin() + " not added as TypeUser Recepient");

        return null;
    }

    /**
     */
    private static int compareTimeStr(String aTime1, String aTime2) {
        int index = aTime1.indexOf(":");
        int hour1 = Integer.parseInt(aTime1.substring(0, index));
        int min1  = Integer.parseInt(aTime1.substring(index + 1));

        index = aTime2.indexOf(":");

        int hour2 = Integer.parseInt(aTime2.substring(0, index));
        int min2  = Integer.parseInt(aTime2.substring(index + 1));

        if (hour1 == hour2) {
            return min1 - min2;
        } else {
            return hour1 - hour2;
        }
    }

    /**
     * This function fills the action details, summary and memo data.
     *
     * @param aTagTable      Hashtable collecting <field,value> pairs
     * @param permissions   User permissions
     *
     * @return List of attachments from all actions.
     */
    private void fillActionDetails( Request request, User recepient, ArrayList<Action> actionList, Hashtable<String, String> aTagTable, Hashtable<String, Integer> permissions, Hashtable<Integer, Collection<ActionFileInfo>> actionFileHash) throws DatabaseException 
    {
        int                       systemId          = myBusinessArea.getSystemId();
        int                       requestId         = request.getRequestId();
        int                       maxEmailActions   = myBusinessArea.getMaxEmailActions();
        String                    sysPrefix         = myBusinessArea.getSystemPrefix();
        int                       maxActionId       = request.getMaxActionId();
        StringBuilder             sb                = new StringBuilder();
        String                    actionClass       = READ_ACTION_CLASS;
        String                    actionHeaderClass = READ_ACTION_HEADER_CLASS;
        String                    textColorClass    = "";
        String                    contentClass      = "dvm bw ";
        String                    actionLog         = "";
        String                    actionLogLink     = "";
        String                    attachments       = "";
        String                    description       = "";
        String                    appender          = "";
        String                    repliedTo         = "";
        String                    updateTime        = "";
        int                       actionId          = 0;
 

              
        Integer fperm = permissions.get(Field.OFFICE) ; 
        if( null == fperm )
        	fperm = 0 ;
        // Disable office if the user does not have a email-view permission
        if (( fperm & Permission.EMAIL_VIEW) == 0) {
            aTagTable.put("office_id_display", "none");
        }

        // Show down arrow, showing descending sort order,
        // for more than one action.
        if (actionList.size() > 1) {
            aTagTable.put("historyImage", "<img class=\"am\" src=\"" + IMG_PATH + "down-white.gif\"/>");
        } else {
            aTagTable.put("historyImage", "");
        }

        // render action details
        for (Action action : actionList) {
            if (action.getActionId() > maxActionId) {
                continue;
            }

            maxEmailActions--;

            if (maxEmailActions < 0) {
                break;
            }

            actionClass       = "";
            actionHeaderClass = "";
            textColorClass    = "";
            actionLog         = "";
            actionLogLink     = "";
            attachments       = "";
            description       = "";
            appender          = "";
            repliedTo         = "";
            updateTime        = "";
            actionId          = 0;

            // When No. of actions of the request exceeds the max no.
            // of actions to be displayed in email, show the first action
            // in the end inplace of the maxth no. of action.
            if ((maxEmailActions == 0) && (action.getActionId() > 1)) {
                action = (Action) actionList.get(actionList.size() - 1);
                sb.append("<div><a class=\"l sx cb ft\" href=\"").append(getRequestUrl(requestId +"", sysPrefix)).append("\" valign=\"bottom\">").append(
            		"&nbsp;View complete request history</a>").append("</div>");
            }

            actionId = action.getActionId();

            if ((actionId == 1) && (maxActionId > 1)) {
                actionClass       = FIRST_ACTION_CLASS;
                actionHeaderClass = READ_ACTION_HEADER_CLASS;

                // actionHeaderClass = FIRST_ACTION_HEADER_CLASS;
                textColorClass = "";
            } else if (actionId == maxActionId) {
                actionClass       = NEW_ACTION_CLASS;
                actionHeaderClass = READ_ACTION_HEADER_CLASS;

                // actionHeaderClass = NEW_ACTION_HEADER_CLASS;
                textColorClass = "";
            } else {
                actionClass = READ_ACTION_CLASS;

                // actionHeaderClass = FIRST_ACTION_HEADER_CLASS;
                actionHeaderClass = READ_ACTION_HEADER_CLASS;
                textColorClass    = "";
            }

            Integer dperm = permissions.get(Field.DESCRIPTION) ;
            if( null == dperm )
            	dperm = 0 ;
            
            if( ( dperm & Permission.EMAIL_VIEW ) != 0 )
            description = ActionHelper.formatDescription(action.getDescription() + "<br>", sysPrefix, PATH, requestId, maxActionId, myBusinessArea.getMaxEmailActions(), new ArrayList<String>(), SOURCE_EMAIL,
	                    STATIC_TOOLTIP, action.getDescriptionContentType() == CONTENT_TYPE_TEXT);
	        
            // Set Appender information
            appender = "<a name=\"" + actionId + "\">" + actionId + ". " + ActionHelper.getUserDisplayName(action.getUserId()) + "</a>";

            // Set last update time

            String dateFormat = null; 
            
            if((recepient != null) &&  recepient.getWebConfigObject() != null)
            	dateFormat = recepient.getWebConfigObject().getWebDateFormat();

            if( null == dateFormat )
            {
	            try{
	                DateTimeFormat dateTimeFormat = DateTimeFormat.lookupByDateTimeFormatId(myBusinessArea.getSysConfigObject().getEmailDateFormat());
	
	                if (dateTimeFormat != null) {
	                    dateFormat = dateTimeFormat.getFormat();
	                }
	            } catch (NullPointerException e) {
	                LOG.debug("",(e));
	            }
            }
            if( null == dateFormat )
            	updateTime = Timestamp.toDateMin(action.getLastUpdatedDate());
            else
            {
            	DateFormat df = new SimpleDateFormat(dateFormat);
            	 Calendar cal        = Calendar.getInstance();
                 TimeZone serverZone = cal.getTimeZone();
                df.setTimeZone(serverZone);
                updateTime = df.format(action.getLastUpdatedDate());
            }

            // Set replied-to info, if any
            if (action.getRepliedToAction() != 0) {
                int repliedAction = action.getRepliedToAction();

                repliedTo = "<span class=\"cal\">&nbsp;&nbsp;&nbsp;" + "[ in response to Update#" + repliedAction + " ]</span>";
                repliedTo = LinkFormatter.hyperSmartLinks(repliedTo, sysPrefix + "#" + requestId, maxActionId, myBusinessArea.getMaxEmailActions(), PATH, SOURCE_EMAIL, STATIC_TOOLTIP);
            } else {
                repliedTo = "";
            }

            // Format attachments, if any
        	Collection<ActionFileInfo> actionAttachments = actionFileHash.get(action.getActionId());        	
            String attachStr = ActionHelper.formatEmailAttachments(actionAttachments, TBitsConstants.HTML_FORMAT, PATH, sysPrefix, requestId, action.getActionId(), permissions);

            attachments = ActionHelper.ATTACHMENT_CONTAINER_HTML.replace("<=attachments>", attachStr);
            
            // Format action Log, if any, based on permissions
            actionLog = (action.getHeaderDescription() == null)
                        ? ""
                        : action.getHeaderDescription();

            String headerDesc = ActionHelper.formatActionLog(systemId, actionLog, permissions, TBitsConstants.HTML_FORMAT, true, sysPrefix, PATH, requestId, maxActionId,
                                    myBusinessArea.getMaxEmailActions(), SOURCE_EMAIL, STATIC_TOOLTIP);

            if ((actionId == maxActionId) && (myRemovedUsers.size() > 0)) {
                headerDesc = headerDesc + "[ Removed from recipients list: " + getUserLoginCSS(myRemovedUsers) + " ]<br>";
            }

            actionLog = ActionHelper.ACTION_LOG_CONTAINER_HTML.replace("<=actionLog>", headerDesc).replace("<=id>", "actionLog" + actionId).replace("<=display>", "");

            // Form the action html form the above formatted data.
            sb.append(ActionHelper.getTextFieldHtml(actionClass, actionHeaderClass, contentClass, textColorClass, "action" + actionId, "", "", appender, description, repliedTo, updateTime,
                    attachments, actionLog, actionLogLink, "", ""));

        }

        aTagTable.put("actionDetails", sb.toString());
    }

    public static String getUserLoginCSS(Collection<User> users ) 
    {
    	String loginCSS = "" ;
    	for( User user : users )
    	{
    		loginCSS += user.getUserLogin() + "," ;
    	}
    	
    	if( !loginCSS.equals("") )
    		loginCSS = loginCSS.substring(0, loginCSS.length()-1) ;
    	
    	return loginCSS ;
	}

	private void fillCssTag(Hashtable<String, String> aTagTable) throws Exception {
        String       cssFileName = CSS_FILE + WebUtil.getCSSFile(mySysConfig.getEmailStylesheet(), myBusinessArea.getSystemPrefix(), true);
        DTagReplacer cssParser   = new DTagReplacer(cssFileName);
        //DTagReplacer cssParser   = new DTagReplacer("D:/tbits/dist/build/webapps/web/css/tbits_email.css");

        aTagTable.put("css", cssParser.parse(myBusinessArea.getSystemId()));
    }

    /**
     * This function fills the action details, summary and memo data.
     *
     * @param aTagTable      Hashtable collecting <field,value> pairs
     * @param aPermissions   User permissions
     *
     * @return List of attachments from all actions.
     */
    private Hashtable<String, String> fillExternalActionDetails(Request request, ArrayList<Action> actionList, Hashtable<String, String> aTagTable, Hashtable<String, Integer> aPermissions, Hashtable<Integer, Collection<ActionFileInfo>> actionFileHash) throws DatabaseException {
        Hashtable<String, String> attachmentsList   = new Hashtable<String, String>();
        int                       requestId         = request.getRequestId();
        int                       maxEmailActions   = myBusinessArea.getMaxEmailActions();
        String                    sysPrefix         = myBusinessArea.getSystemPrefix();
        int                       maxActionId       = request.getMaxActionId();
        StringBuilder             sb                = new StringBuilder();
        String                    actionClass       = READ_ACTION_CLASS;
        String                    actionHeaderClass = READ_ACTION_HEADER_CLASS;
        String                    textColorClass    = "";
        String                    contentClass      = "dvm bw ";
        String                    actionLog         = "";
        String                    actionLogLink     = "";
        String                    attachments       = "";
        String                    description       = "";
        String                    appender          = "";
        String                    repliedTo         = "";
        String                    updateTime        = "";
        int                       actionId          = 0;

        // Attachment links should not be sent to the external user so no need to do this.
//        Hashtable<Integer, Collection<ActionFileInfo>> actionFileHash = Action.getAllActionFiles(myBusinessArea.getSystemId(), requestId);
 
        // render action details 
        for (Action action : actionList) {
            if (action.getActionId() > maxActionId) {
                continue;
            }

            maxEmailActions--;

            if (maxEmailActions < 0) {
                break;
            }

            actionClass       = "";
            actionHeaderClass = "";
            textColorClass    = "";
            actionLog         = "";
            actionLogLink     = "";
            attachments       = "";
            description       = "";
            appender          = "";
            repliedTo         = "";
            updateTime        = "";
            actionId          = 0;

            // When No. of actions of the request exceeds the max no.
            // of actions to be displayed in email, show the first action
            // in the end inplace of the maxth no. of action.
            if ((maxEmailActions == 0) && (action.getActionId() > 1)) {
                action = (Action) actionList.get(actionList.size() - 1);
                sb.append("<div class=\"l sx cb ft\"").append("&nbsp;View complete request history").append("</div>");
            }

            actionId = action.getActionId();

            if ((actionId == 1) && (maxActionId > 1)) {
                actionClass       = FIRST_ACTION_CLASS;
                actionHeaderClass = READ_ACTION_HEADER_CLASS;

                // actionHeaderClass = FIRST_ACTION_HEADER_CLASS;
                textColorClass = "";
            } else if (actionId == maxActionId) {
                actionClass       = NEW_ACTION_CLASS;
                actionHeaderClass = READ_ACTION_HEADER_CLASS;

                // actionHeaderClass = NEW_ACTION_HEADER_CLASS;
                textColorClass = "";
            } else {
                actionClass = READ_ACTION_CLASS;

                // actionHeaderClass = FIRST_ACTION_HEADER_CLASS;
                actionHeaderClass = READ_ACTION_HEADER_CLASS;
                textColorClass    = "";
            }

            Integer dperm = aPermissions.get(Field.DESCRIPTION) ;
            if( null == dperm )
            	dperm = 0 ;
            
            if( ( dperm & Permission.EMAIL_VIEW) != 0 )
	        {//
	            // Format Description
	            //
	            description = action.getDescription();
	        

	            //
	            // Render all requests links as smart links, if they have not been
	            // converted correctly in the api for old requests.
	            //
	            description = LinkFormatter.replaceHrefWithSmartLinks(description);
	
	            // Replace the \n with <br> and \t with 4 spaces.
	            //description = description.replaceAll("^[\n]+", "");
	            if (action.getDescriptionContentType() == CONTENT_TYPE_TEXT){
	            	description = Utilities.htmlEncode(description);
		            description = description.replaceAll("\\r\\n", "<br>");
		            description = description.replaceAll("\\n", "<br>");
		            description = description.replaceAll("\\r", "<br>");
		            description = description.replaceAll("  ", "&nbsp;&nbsp;");
		            description = description.replaceAll("\\t", "&nbsp;&nbsp;&nbsp;&nbsp;");
	            }
	            //description = description.replaceAll("<br>", "\n<br>");
	            //description = description.replaceAll("<br> ", "<br>&nbsp;");
	            if( !description.equals("") )
	            	description += "<br>";
	        }
            //
            // Set Appender information
            //
            String displayName = "-";
            User   user        = User.lookupAllByUserId(action.getUserId());

            if (user != null) {
                displayName = user.getDisplayName();
            }

            appender = "<a name=\"" + actionId + "\">" + actionId + ". " + displayName + "</a>";

            //
            // Set last update time
            //
            updateTime = getDateString(action.getLastUpdatedDate());

            //
            // Set replied-to info, if any
            //
            if (action.getRepliedToAction() != 0) {
                int repliedAction = action.getRepliedToAction();

                repliedTo = "<span class=\"cal\">&nbsp;&nbsp;&nbsp;" + "[ in response to Update#" + repliedAction + " ]</span>";
                repliedTo = LinkFormatter.hyperSmartLinks(repliedTo, sysPrefix + "#" + requestId, maxActionId, maxActionId, PATH, SOURCE_EMAIL, NO_TOOLTIP);
            } else {
                repliedTo = "";
            }

             // Format attachments, if any 
        	Collection<ActionFileInfo> actionAttachments = actionFileHash.get(action.getActionId());        	
            String attachStr = ActionHelper.formatEmailAttachments(actionAttachments, TBitsConstants.HTML_FORMAT, PATH, sysPrefix, requestId, action.getActionId(), aPermissions);
            attachStr   = attachStr.replaceAll("\\n", "<br>");
            attachments = ActionHelper.ATTACHMENT_CONTAINER_HTML.replace("<=attachments>", attachStr);

            // Format action Log, if any, based on permissions
            actionLog = (action.getHeaderDescription() == null)
                        ? ""
                        : action.getHeaderDescription();

            String headerDesc = ActionHelper.formatActionLog(myBusinessArea.getSystemId(), actionLog, aPermissions, TBitsConstants.HTML_FORMAT, true, sysPrefix, PATH, requestId, maxActionId,
                                    myBusinessArea.getMaxEmailActions(), SOURCE_EMAIL, NO_TOOLTIP);

            if ((actionId == maxActionId) && (myRemovedUsers.size() > 0)) {
                headerDesc = headerDesc + "[ Removed from recipients list: " + getUserLoginCSS(myRemovedUsers) + " ]<br>";
            }

            actionLog = ActionHelper.ACTION_LOG_CONTAINER_HTML.replace("<=actionLog>", headerDesc).replace("<=id>", "actionLog" + actionId).replace("<=display>", "");
            
            //
            // Form the action html form the above formatted data.
            //
            sb.append(ActionHelper.getTextFieldHtml(actionClass, actionHeaderClass, contentClass, textColorClass, "action" + actionId, "", "", appender, description, repliedTo, updateTime,
                    attachments, actionLog, actionLogLink, "", ""));
        }

        aTagTable.put("actionDetails", sb.toString());

        return attachmentsList;
    }

    /**
     * This method  fills fixed and extended fields Header with values
     * by replacing <%=field_label%> and <%=field_value%> tags.
     *
     * @param aTagTable      Hashtable collecting <field,value> pairs
     * @param aRequest       Request object
     * @param aDateFormat    Date format.
     * @param aZone          Zone value.
     */
    private void fillExternalHeaderData(Hashtable<String, String> aTagTable, Request aRequest, String aDateFormat, TimeZone aZone, Hashtable<String,Integer> permissions ) throws Exception {
        int              systemId   = aRequest.getSystemId();
        String           fieldName  = "";
        String           fieldValue = "";
        ArrayList<Field> fieldsList = Field.lookupBySystemId(systemId);

        for (Field field : fieldsList) 
        {
        	try
        	{
	            fieldName  = field.getName();
	            fieldValue = aRequest.get(fieldName);
	
	            if (fieldValue == null) {
	                fieldValue = "-";
	            }
	
	            switch (field.getDataTypeId()) {
	            case DataType.TYPE :
	                Type t = Type.lookupBySystemIdAndFieldNameAndTypeName(systemId, fieldName, fieldValue);
	
	                if ((t != null) )
	                {
		                fieldValue = t.getDisplayName() ;	                	                
	                    fieldValue = (t.getIsPrivate()
	                                  ? "&dagger;"
	                                  : "") + fieldValue;		                
	                }
	
	                break;
	
	            case DataType.DATE :
	            case DataType.TIME :
	            case DataType.DATETIME :
	                if ((aDateFormat != null) && (fieldValue.equals("-") == false)) {
	                    String dateFormat = aDateFormat;
	
	                    if (aRequest.get(fieldName).indexOf("23:59") != -1) {
	                        if ((aDateFormat.trim().equals("MMM dd, yyyy") == false) && (aDateFormat.indexOf(" ") != -1)) {
	                            dateFormat = aDateFormat.substring(0, aDateFormat.indexOf(" "));
	                        }
	                    }
	
	                    fieldValue = WebUtil.getDateInFormat(APIUtil.parseDateTime(aRequest.get(fieldName)), aZone, dateFormat);
	                }
	
	                break;
	            }
	
	            Integer perm = permissions.get( fieldName );
	            if(null == perm )
	            	perm = 0;
	            
	            if( ( perm & Permission.EMAIL_VIEW ) == 0 )
	            	fieldValue = "-" ;
	            
	            aTagTable.put(fieldName, fieldValue);
	            aTagTable.put(fieldName + "_label", field.getDisplayName());
        	}
        	catch(Exception e )
        	{
        		LOG.info("Exception occurred while filling the external html header data for reqeust : " + mySmartLink,e ) ;
        		LOG.info("",(e)) ;
        	}
        }
    }

    /**
     */
    private void fillRegisterActionTag(Request request, Hashtable<String, String> aTagTable) {
        aTagTable.put("registerAction", "<img src=\"" + PATH + "register-action/" + myBusinessArea.getSystemPrefix() + "/" + request.getRequestId() + "/" + request.getMaxActionId() + "/" +

        // "<Register-User>" + "/" +
        System.currentTimeMillis() + "\" width=\"0\"" + "height=\"0\">");
    }

    /**
     * This method  fills sub,subling, related requests tags.
     *
     * @param aTagTable      Hashtable collecting <field,value> pairs
     * @param aRequest       Request object
     */
    private void fillRequestAssociations(Hashtable<String, String> aTagTable, Request aRequest) {
        boolean       associate        = false;
        StringBuilder header           = new StringBuilder();
        String        relatedBeginning = "<td align='left' class='m'" + "style='white-space:nowrap'>&nbsp;";
        String        relatedEnd       = "&nbsp;</td>";
        boolean       parentFlag       = !aTagTable.get("parent").equals("");
        Hashtable<String,String> siblingRequests =  Request.getSiblingRequests(aRequest.getSystemId(),aRequest.getRequestId()) ;
        Hashtable<String,String> subRequests = Request.getSubRequests(aRequest.getSystemId(), aRequest.getRequestId());
        Hashtable<String,String> relRequests = Request.getRelatedRequests(aRequest.getSystemId(), aRequest.getRequestId());
        int           headerSize       = findHeaderSize(siblingRequests, relRequests, subRequests, header, aTagTable);

        if ((headerSize == 0) && (parentFlag == false)) {
            aTagTable.put("relatedBeginning", "");
            aTagTable.put("relatedEnd", "");
            aTagTable.put("requestHistoryLine", "");
        } else {
            aTagTable.put("relatedBeginning", relatedBeginning);
            aTagTable.put("relatedEnd", relatedEnd);
            aTagTable.put("requestHistoryLine", "|");
        }

        if (headerSize < 100) {
            aTagTable.put("siblingRequests", header.toString());
            aTagTable.put("subRequests", "");
            aTagTable.put("relatedRequests", "");
            aTagTable.put("associatedRequests", "");
        } else {
            Hashtable<String, String> requestsHash = siblingRequests;

            if ((requestsHash != null) && (requestsHash.size() > 0)) {
                getRelationsHtml(aTagTable, requestsHash, "Siblings", "siblingRequests", !aTagTable.get("parent").equals(""));
                associate = true;
            } else {
                aTagTable.put("siblingRequests", "");
                aTagTable.put("siblingRequestsDetails", "");
            }

            requestsHash = subRequests;

            if ((requestsHash != null) && (requestsHash.size() > 0)) {
                getRelationsHtml(aTagTable, requestsHash, "Subrequests", "subRequests", !aTagTable.get("parent").equals("") ||!aTagTable.get("siblingRequests").equals(""));
                associate = true;
            } else {
                aTagTable.put("subRequests", "");
                aTagTable.put("subRequestsDetails", "");
            }

            requestsHash = relRequests;

            if ((requestsHash != null) && (requestsHash.size() > 0)) {
                getRelationsHtml(aTagTable, requestsHash, "Linked", "relatedRequests",
                                 !aTagTable.get("parent").equals("") ||!aTagTable.get("siblingRequests").equals("") ||!aTagTable.get("subRequests").equals(""));
                associate = true;
            } else {
                aTagTable.put("relatedRequests", "");
                aTagTable.put("relatedRequestsDetails", "");
            }

            if (associate == true) {
                aTagTable.put("associatedRequests",
                              ASSOCIATED_REQUESTS_HTML.replace("<=subRequestsDetails>", aTagTable.get("subRequestsDetails")).replace("<=siblingRequestsDetails>",
                                  aTagTable.get("siblingRequestsDetails")).replace("<=relatedRequestsDetails>", aTagTable.get("relatedRequestsDetails")));
            } else {
                aTagTable.put("associatedRequests", "");
            }
        }

        return;
    }

    /**
     * This method adds Summary field if non empty for thsi append
     *
     * @param aTagTable      Hashtable collecting <field,value> pairs
     */
    private void fillSummary(Request request, ArrayList<Action> actionList, Hashtable<String, String> aTagTable, Hashtable<String,Integer> permissions ) throws Exception {
        Field field = Field.lookupBySystemIdAndFieldName(request.getSystemId(), Field.SUMMARY);

        Integer sperm = permissions.get(Field.SUMMARY) ;
        if( null == sperm )
        	sperm = 0;
        
        if ( ( (actionList.size() == 1) && ( (request.getSummary() == null) || (request.getSummary().trim().equals("") == true) ) ) || ( ( sperm & Permission.EMAIL_VIEW ) == 0 ) ) {
            aTagTable.put("summaryHeader", "");
            aTagTable.put("summaryUpdate", "");
            aTagTable.put("summaryDetail", "");

            return;
        }

        if (request.getSummary() != null) {
            if ((request.getSummary().trim().equals("") == false)
                    || ((request.getSummary().trim().equals("") == true) && ((actionList.get(1).getSummary() == null) || (actionList.get(1).getSummary().trim().equals("") == false)))) {
                String summary = ActionHelper.formatDescription(request.getSummary(), myBusinessArea.getSystemPrefix(), PATH, request.getRequestId(), request.getMaxActionId(),
                                     myBusinessArea.getMaxEmailActions(), new ArrayList<String>(), SOURCE_EMAIL, STATIC_TOOLTIP, request.getSummaryObject().getContentType() == CONTENT_TYPE_TEXT);
                String img = "<span class=\"l b cbk\">" + "<img src=\"" + IMG_PATH + "t-r.gif\" class=\"am\">&nbsp;";

                aTagTable.put("summaryHeader",
                              ActionHelper.getTextFieldHtml(TEXT_CLASS, TEXT_HEADER_CLASS, TEXT_CONTENT_CLASS, "", "", Field.SUMMARY, "", img + field.getDisplayName() + "</span>", summary,
                                  "[ updated by " + ActionHelper.getUserDisplayName(request.getUserId()) + " in <a class=\"l cb\" href=\"#" + request.getMaxActionId() + "\">Update#"
                                  + request.getMaxActionId() + "</a> ]", getDateString(request.getLastUpdatedDate()), "", "", "", "", ""));
                aTagTable.put("summaryUpdate", "");
                aTagTable.put("summaryDetail", "");

                return;
            }
        }

        String summary = "";

        for (Action action : actionList) {
            if (action.getActionId() > request.getMaxActionId()) {
                continue;
            }

            if (action.getSummary() != null) {
                if (action.getSummary().trim().equals("") == true) {
                    break;
                }

                summary = ActionHelper.formatDescription(action.getSummary(), myBusinessArea.getSystemPrefix(), PATH, request.getRequestId(), request.getMaxActionId(),
                        myBusinessArea.getMaxEmailActions(), new ArrayList<String>(), SOURCE_EMAIL, STATIC_TOOLTIP, action.getSummaryContentType() == CONTENT_TYPE_TEXT);

                String img              = "<span class=\"l b cbk\">" + "<img src=\"" + IMG_PATH + "t-r.gif\" class=\"am\">&nbsp;";
                
                String actionUpdateLink = " in <a class=\"l cb\" href=\""
                                          + (((action.getActionId() > 1) && (action.getActionId() <= (myBusinessArea.getMaxEmailActions() - request.getMaxActionId() + 1)))
                                             ? (getRequestUrl(request.getRequestId() + "", myBusinessArea.getSystemPrefix()))
                                             : "#" + action.getActionId()) + "\">Update#" + action.getActionId() + "</a>";

                summary = ActionHelper.getTextFieldHtml("dm bh h", "hsp", TEXT_CONTENT_CLASS, "", "", Field.SUMMARY, "", "<a name=\"summary\">" + img + field.getDisplayName() + "</span></a>",
                        summary, "[ updated by " + ActionHelper.getUserDisplayName(action.getUserId()) + actionUpdateLink + " ]", getDateString(action.getLastUpdatedDate()), "", "", "", "", "");

                break;
            }
        }

        if ((summary != null) && (summary.equals("") == false)) {
            aTagTable.put("summaryHeader", "");

            StringBuffer sb = new StringBuffer();

            if ((aTagTable.get("siblingRequests").equals("") == false) || (aTagTable.get("subRequests").equals("") == false) || (aTagTable.get("relatedRequests").equals("") == false)
                    || (aTagTable.get("parent").equals("") == false)) {
                sb.append("&nbsp;<span class='b'>|</span>&nbsp;");
            }

            sb.append("\n<td><a class=\"l cw b crhb\" href=\"#summary\">").append("Summary").append("</a></td>");
            aTagTable.put("summaryUpdate", sb.toString());
            aTagTable.put("summaryDetail", summary);
        } else {
            aTagTable.put("summaryHeader", "");
            aTagTable.put("summaryUpdate", "");
            aTagTable.put("summaryDetail", "");
        }
    }

    /**
     * This method returns the size of the request associations header.
     *
     * @param aSiblingRequests  Hashtable containing Sibling request id,subject
     *                           pairs
     * @param aRelatedRequests  Hashtable containing Related request id,subject
     *                           pairs
     * @param aSubRequests      Hashtable containing  subrequest id,subject
     *                          pairs
     * @param actualHeader      StringBuilder containing the complete header.
     * @param aTagTable         HashTable containing the tag,value pairs.
     */
    private int findHeaderSize(Hashtable<String, String> aSiblingRequests, Hashtable<String, String> aRelatedRequests, Hashtable<String, String> aSubRequests, StringBuilder actualHeader,
                               Hashtable<String, String> aTagTable) {

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

                String reqId = requestID.substring(requestID.indexOf("#") + 1);
                String sysPrefix = requestID.substring(0, requestID.indexOf("#"));
                sb.append(reqId);
                

                actualHeader.append("<a class=\"l cw b tt\" href=\"").append(getRequestUrl(reqId, sysPrefix)).append("\" title=\"").append("").append(Utilities.htmlEncode(aSiblingRequests.get(requestID))).append("\" >").append(requestID.substring(requestID.indexOf("#") + 1));

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
                        sb.append("| Subrequests: ");
                        actualHeader.append("&nbsp;<span class='b'>|</span>&nbsp;").append("&nbsp;<span class=\"cw b crhb\">").append("Subrequests: </span>");
                    } else {
                        sb.append("Subrequests: ");
                        actualHeader.append("<span class=\"cw b crhb\">").append("Subrequests: </span>");
                    }
                }

                count2++;

                String requestID = e.nextElement();
                String requestIdStr = requestID.substring(requestID.indexOf("#") + 1);
                String sysPrefix = requestID.substring(0, requestID.indexOf("#"));
                sb.append(requestIdStr);
                
                actualHeader.append("<a class=\"l cw b tt\" href=\"").append(getRequestUrl(requestIdStr, sysPrefix)).append("\" ").append("title=\"").append(Utilities.htmlEncode(aSubRequests.get(requestID))).append("\" >").append(requestID.substring(requestID.indexOf("#") + 1));

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
                
                String requestIdStr = requestID.substring(requestID.indexOf("#") + 1);
                String sysPrefix = requestID.substring(0, requestID.indexOf("#"));

                actualHeader.append("<a class=\"l b cw \" href=\"").append(getRequestUrl(requestIdStr, sysPrefix)).append("\" ").append("title=\"").append(Utilities.htmlEncode(aRelatedRequests.get(requestID))).append("\" >").append(requestID);

                if ((count3++ > 0) && e.hasMoreElements()) {
                    actualHeader.append(",");
                }

                actualHeader.append("</a>&nbsp;");
            }
        }

        return sb.toString().length();
    }

    /* Jaguar compatible URL */
    private String getAddRequestUrl(String systemPrefix) {
    	if(isClassicUI)
    		return PATH + "add-request/" + systemPrefix;
		return PATH + "jaguar/#ba=" + systemPrefix;
	}

	private String getUpdateRequestUrl(String systemPrefix, Integer requestId) {
    	if(isClassicUI)
    		return PATH + "Q/" + systemPrefix + "/" + requestId + "?u=1";
		return PATH + "jaguar/#ba=" + systemPrefix + "&update=" + requestId;
	}

	private String getAddSubRequestUrl(String systemPrefix, Integer requestId) {
    	if (isClassicUI)
			return PATH + "add-subrequest/" + systemPrefix + "/" + requestId;
    	return PATH + "jaguar/#ba="+systemPrefix + "&view=" + requestId;
	}

	private String getMyRequestUrl(String systemPrefix) {
    	if(isClassicUI)
    		return PATH + "search/" + systemPrefix + "/my-requests";
		return PATH + "jaguar/#ba=" + systemPrefix;
	}

    private String getRequestUrl(String smartTag) 
    {
    	int idx = smartTag.indexOf('#');
    	String reqId = smartTag.substring(0, idx);
    	String sysPrefix = smartTag.substring(idx+1);
    	return getRequestUrl(reqId, sysPrefix);
    }
	private String getRequestUrl(String reqId, String sysPrefix) {
		StringBuilder url = new StringBuilder(PATH);
		if(isClassicUI)
		{
			url  = url.append("q/").append(sysPrefix).append("/").append(reqId);
		}
		else
		{
			url  = url.append("jaguar/#ba=").append(sysPrefix).append("&view=").append(reqId);			
		}
		return url.toString();
	}

	/**
     */
//    private void loadPermissions(HashSet<User> userList) throws Exception 
//    {
//        myPrivatePermissions = new Hashtable<Integer, Integer>();
////        myIsPrivate          = false;
//
//        StringBuilder strUserIds = new StringBuilder();
//
//        for (User user : userList) {
//            if (user.getIsActive() == true) {
//                strUserIds.append(user.getUserId());
//                strUserIds.append(",");
//            }
//        }
//
//        if (strUserIds.length() > 0) {
//            strUserIds = strUserIds.deleteCharAt(strUserIds.length() - 1);
//        }
//
//        // Load users is_private permissions, if request is private
////        if ((myRequest.getIsPrivate() == true) || (myRequest.getCategoryId().getIsPrivate() == true) || (myRequest.getSeverityId().getIsPrivate() == true)
////                || (myRequest.getStatusId().getIsPrivate() == true) || (myRequest.getRequestTypeId().getIsPrivate() == true) || (myBusinessArea.getIsPrivate() == true)) {
////            myIsPrivate = true;
////        }
//
//        // load Private Permissions
//        if ((myRequest.getIsPrivate() == true) && (strUserIds.length() > 0)) {
//            myPrivatePermissions = RolePermission.getPrivatePermissionsBySystemIdAndRequestIdAndActionIdAndUserIdList(myRequest.getSystemId(), myRequest.getRequestId(), myRequest.getMaxActionId(),
//                    strUserIds.toString());
//        }
//
//        return;
//    }

    /**
     */
//    private void mailBaList(String aTextContent, String aSubject, int aSeverity, ArrayList<User> aBaMailList) throws Exception {
//        aTextContent = "/" + Field.RELATED_REQUESTS + " : " + myBusinessArea.getSystemPrefix() + "#" + myRequest.getRequestId() + "\n\n" + aTextContent;
//        Mail.sendWithBCC(getUserEmails(aBaMailList), myUser.getEmail(), aSubject, aTextContent, myUser.getEmail(), aSeverity, "");
//
//        return;
//    }
    
    /**
     * @throws Exception 
     */
//    private void mailUser(int systemId, Request aRequest, String aSubject,User recipient, int aSeverity, String aTBitsRecepients,
//                                   Hashtable<String, String> aExtHdrs, Hashtable<String,Integer> permissions) throws Exception
//    {
//    	int requestId = aRequest.getRequestId();    	
//    	MimeMultipart mp = null;
//		
//		boolean exceptionInCreatingTextContents = false ;
//		String textContent = "" ;
//		try
//		{
//		  textContent = getTextContent(permissions);
//		}
//		catch( Exception e )
//		{
//			e.printStackTrace() ;
//			LOG.info("Exception in creting text email contents for user : " + recipient.getUserLogin() + " : for the request : " + aRequest.getSystemId() + "#" + aRequest.getRequestId() ,e ) ;
//			LOG.info("",(e)) ;
//			exceptionInCreatingTextContents = true ;
//		}
//		String aHtmlContent = "" ;
//		try
//		{
//			aHtmlContent = getExternalHtmlContent(permissions);
//		}
//		catch(Exception e )
//		{
//			e.printStackTrace() ;
//			LOG.info("Exception in creting html email contents for user : " + recipient.getUserLogin() + " : for the request : " + aRequest.getSystemId() + "#" + aRequest.getRequestId() ,e ) ;
//			LOG.info("",(e)) ;
//			if( exceptionInCreatingTextContents == true )
//			{
//				LOG.info("Neither the text nor html contents for the user can be generated so not sending mail to the user : " + recipient.getUserLogin()) ;
//				throw e ;
//			}
//		}
//		
//		//
//		// If action has attachment and any external address to send
//		// mails to, send separate mails with attachment attached
//		// in the mail itself.
//		//
//		mp = new MimeMultipart();
//
//		//
//		// create and Fill the Html message part
//		//
//		MimeBodyPart mbp = new MimeBodyPart();
//
//		mbp.setContent(aHtmlContent, "text/html");
//		mbp.setHeader("MIME-Version", "1.0");
//		mbp.setHeader("Content-Type", "text/html");
//
//		//
//		// create and fill the Text message part
//		//
//		MimeBodyPart mbp1 = new MimeBodyPart();
//
//		mbp1.setText(textContent);
//		mbp1.setHeader("MIME-Version", "1.0");
//		mbp1.setHeader("Content-Type", "text/plain");
//		mp.addBodyPart(mbp, 0);
//   
//        ArrayList<Field> allAttachmentsFileIds = new ArrayList<Field>();       
//        
//        for(Field f:Field.lookupBySystemId(systemId))
//        {
//        	if(f.getDataTypeId() == DataType.ATTACHMENTS)
//			{
//        		Integer fieldPermInteger = permissions.get(f.getName());
//				int fieldPerm = 0;
//				if(fieldPermInteger != null)
//					fieldPerm = fieldPermInteger.intValue();
//				
//				if( (fieldPerm & Permission.EMAIL_VIEW) != 0)
//				{
//					allAttachmentsFileIds.add(f);
//				}
//			}
//        }
//        
//        File attDirfile = new File(Configuration
//					.findAbsolutePath(PropertiesHandler
//							.getProperty(KEY_ATTACHMENTDIR)));
//		int a = 0;
//        for (TBitsFileInfo fileInfo : Uploader.getNewOrModifiedFilesLocations(systemId, requestId, aRequest.getMaxActionId(), allAttachmentsFileIds)) 
//        {
//            a++;
//           // String location = Uploader.getFileLocation(attachment.repoFileId);
//            File file = new File(attDirfile, fileInfo.getFileLocation());
//            FileDataSource fds  = new FileDataSource(file);
//            MimeBodyPart   mbp2 = new MimeBodyPart();
//            mbp2.setDataHandler(new DataHandler(fds));
//            mbp2.setDisposition(Part.ATTACHMENT);
//            mbp2.setFileName(fileInfo.getFileName());
//            mp.addBodyPart(mbp2, a);
//        }
//        BusinessArea ba = BusinessArea.lookupBySystemId(systemId);
//        String fromEmail =  "\""+aRequest.getUserId().getDisplayName()+"\""+"<"+ba.getEmail()+">"; 
//	    Mail.sendHtmlAndAttachments(getUserEmail(recipient), fromEmail, aSubject, myBusinessArea.getFirstEmail(), aSeverity, aTBitsRecepients, aExtHdrs, mp);
//	    
//	    LOG.info( "Finished sending mail to the user : " + recipient.getUserLogin() + " : for the request : " + mySmartLink ) ;    		    	
//    }

    /**
     */
//    private void mailInternalUser(int systemId, Request aRequest, final String aSubject, User user , final int aSeverity, final String aXTBitsRecepients,
//                                   final Hashtable<String, String> aExtHdrs, Hashtable<String,Integer>permissions)
//            throws Exception {
////    	LOG.info("Start sending mail to internal users : " + getUserLoginCSS(aInternalList) + " : for the request : " + mySmartLink ) ;
////    	
////    	for (User user: aInternalList)
////    	{
//    		LOG.info("Sending mail to : " + user.getUserLogin() + " : for the request : " + mySmartLink ) ;
//    		try
//    		{
////	    		Hashtable<String, Integer> permissions = RolePermission.getPermissionsBySystemIdAndRequestIdAndUserId(systemId, aRequest.getRequestId(), user.getUserId());
//	    		boolean exceptionInCreatingTextContent = false ;
//	    		String textContent = " An Error might have occured for creating the text form of mail. Please try to see the mail in HTML format. If still not able to view the mail please contact tBits team." ;
//	    		try
//	    		{
//	    			textContent = getTextContent(permissions);
//	    		}
//	    		catch( Exception e )
//	    		{
////	    			e.printStackTrace() ;
//	    			LOG.info("Exception while creating text contents for user : " + user.getUserLogin() + " : for request : " + mySmartLink , e ) ;
//	    			LOG.info("",(e)) ;
//	    			exceptionInCreatingTextContent = true ;
//	    		}
//	    		
//	    		String aHtmlContent = "" ;
//	    		try
//	    		{
//	    			aHtmlContent = getHtmlContent(permissions);
//	    		}
//	    		catch(Exception e ) 
//	    		{
////	    			e.printStackTrace() ;
//	    			LOG.info("Exception occured while creating HTML email for user : " + user.getUserLogin() + " for request : " + mySmartLink, e ) ;
//	    			LOG.info("",(e)) ;
//	    			// neither the text or html content can be created then throw exception
//	    			if( exceptionInCreatingTextContent == true )
//	    			{
//	    				LOG.info("Neither the text nor html contents for the user can be generated so not sending mail to the user : " + user.getUserLogin() + " for request : " + mySmartLink ) ;
//	    				throw e ;
//	    			}
//	    		}
//	    		BusinessArea ba = BusinessArea.lookupBySystemId(systemId);
//	    		String fromEmail =  "\""+aRequest.getUserId().getDisplayName()+"\""+"<"+ba.getEmail()+">"; 
//	    		Mail.sendWithHtmlAndReplyTo(getUserEmail(user), "", fromEmail, aSubject, aHtmlContent, textContent, myBusinessArea.getFirstEmail(), aSeverity, aXTBitsRecepients,
//	                                    aExtHdrs);
//	    		
//	    		LOG.info( "Finished sending mail to the user : " + user.getUserLogin() + " : for the request : " + mySmartLink ) ;
//    		}
//    		catch(Exception e )
//    		{
////    			e.printStackTrace() ;
//    			LOG.warn("Exception occured while sending mail to user : " + user.getUserLogin() + " and email id = " + getUserEmail(user) + " : for the request : " + mySmartLink, e ) ;
//    			LOG.info("",(e)) ;
//    			// add this user to failedMailUserList
//    			failedMailUserList.add(user);
//    		}
////    	}
//
//    	return;
//    }

    public static void main(String[] args) throws Exception {

        // Setting email invocation
//        System.setProperty(TBitsConstants.PROP_BA_NAME, args[0]);
//        LOG.debug(BusinessArea.lookupBySystemId(Integer.parseInt(args[1])).getSystemPrefix());
    	if(args.length != 2)
    	{
    		System.err.print("Incorrect Syntax.");
    		System.err.print("Syntax: program <sys_prefix> <request_id>");
    		return;
    	}
    	int sysId = 0;
    	try
    	{
    		BusinessArea ba = BusinessArea.lookupBySystemPrefix(args[0]);
    		if(ba == null)
    		{
    			System.err.println("Invalid BA.");
    			return;
    		}
    		sysId = ba.getSystemId();
    	}
    	catch(Exception exp)
    	{
    		exp.printStackTrace();
    		return;
    	}
    	
    	int requestId = 0;
    	try
    	{
    		requestId = Integer.parseInt(args[1]);
    	}
    	catch(Exception exp)
    	{
    		exp.printStackTrace();
    		System.err.println("Invalid request id.");
    		return ;
    	}
    	
        Request request = Request.lookupBySystemIdAndRequestId(sysId, requestId);
        if (request == null) {
            System.out.println("request null");
        } else {
            (new TBitsMailer(request)).sendMail();
        }

//    	ArrayList<User> users = User.getAllUsers() ;
//    	for( User user : users )
//    	{
//    		if( user.getUserTypeId() == UserType.INTERNAL_MAILINGLIST )
//    		{
//    			HashSet<User> resolvedUsers = resolveMailList(user) ;
//    			System.out.println("MailList( " + user.getUserLogin() + " ) : " + getUserLoginCSS(resolvedUsers) );
//    		}
//    	}
    	
        // User user = User.lookupByUserId(Integer.parseInt(args[1]));
        // System.out.println(resolveMailList(user));
    }

    private boolean isClassicUI = false;
    
    /**
     * This method is used to send the mail notification to the users
     * when a request or action is logged.
     *
     * @param aRequest  the Request Object. The mail has to be sent for this
     *                  state(action) of the Request.
     *
     * @exception DatabaseException
     */
    public void sendMail() {
    	Request aRequest = myRequest ;
    	
    	long start = System.currentTimeMillis();   	
//    	
//        LOG.info(" for request : " + aRequest ) ;
//        if (aRequest == null) {
//            LOG.warn("Request object null: exiting from TBitsMailer");
//            return;
//        }
        
        try
        {
        	// If emails are set Inactive for the Business Area
            // don't send mails and return
            //
			
			if (myBusinessArea.getIsEmailActive() == false) {
                LOG.info(mySmartLink + ": Mails Inactive for BA." + " Exiting from TBitsMailer");

                return;
            }

            //
            // If Notify By email unchecked, don't send mails and return
            //
            if (aRequest.getNotify() == false) {
                LOG.info(mySmartLink + ": Notification tutned off." + " Skipping from TBitsMailer");

                return;
            }

//            myRequest = aRequest;

            Hashtable<String, String> parentRequests = new Hashtable<String, String>();

            if (myRequest.getParentRequestId() > 0) {
                String parentSubject = Request.lookupSubject(myBusinessArea.getSystemPrefix(), myRequest.getParentRequestId(), 0, true);

                if (parentSubject != null) {
                    parentRequests.put(myBusinessArea.getSystemPrefix() + "#" + myRequest.getRequestId(), parentSubject);
                }
            }

//            myRequest.setParentRequests(parentRequests);

            //
            // Get Request with all transitive related requests also,
            // if related
            //
            // Removed by Nitiraj
//            try {
//                Request request = Request.lookupBySystemIdAndRequestId(systemId, requestId);
//
//                if (request != null) {
//                    myRequest.setRelatedRequests(request.getRelatedRequests());
//                }
//            } catch (Exception e) {
//                LOG.warn("Error for " + mySmartLink + "\n" + "",(e));
//            }

            //
            // Set memo to empty String, as its used internally only.
            // This is populated only if request object is passed via api.
            //
            myRequest.setMemo("");
//            myUser = User.lookupByUserId(myRequest.getUserId());

            //
            // form separate recepients list for
            // 1) internal users/ groups.
            // 2) external users
            // considering business areas rules for appender etc
            // and also notification ruels set by users..
            // 4) form XTBitsRecepients List

           ArrayList<User> recepientsList = getRecepientsList();

            if (recepientsList == null) {
                LOG.info(mySmartLink + ": Recepients list empty." + " Exiting from TBitsMailer");

                return;
            }
             LOG.info( mySmartLink + " : recepient list : " + recepientsList ) ;
             LOG.info(mySmartLink + ": myXTBitsRecepients: " + myXTBitsRecepients);
             
            // Get all actions of the request.
            //
//            myActionList = Action.getAllActions(myRequest.getSystemId(), myRequest.getRequestId(), "asc");

            long end1 = System.currentTimeMillis();

            //
            // Get Severity Flag
            //
            int aSeverityFlag = getSeverityFlag(myRequest.getSeverityId().getName());

            /*
             * Lets put category as an extended header in the message.
             * DFlow#2767
             */
            Hashtable<String, String> extHdrs  = new Hashtable<String, String>();
            String                    category = "";

            /*
             * Not sure if any exception would occur here, but this should
             * not stop the mailing process. So enclosing this in try-catch
             * block.
             */
            try {
                category = aRequest.getCategoryId().getDisplayName();
            } catch (Exception e) {
                LOG.info("",(e));
                category = "";
            }

            extHdrs.put(TBitsConstants.X_TBITS_CATEGORY, category);
            extHdrs.put(TBitsConstants.X_AUTOREPLY, "yes");
            //
            // Send Mail to internal users/groups.
            //
            if ((recepientsList != null) && (recepientsList.size() > 0)) {
                mailAllUsers(recepientsList, aSeverityFlag, myXTBitsRecepients, extHdrs);
            }

            // update the removed user's info in the tracking options.
            if( (null != myRemovedUsers && myRemovedUsers.size() != 0  ) || ( null != failedMailUserList && failedMailUserList.size() !=0 ) )
            {
//	            Action currAction = myActionList.get(myRequest.getMaxActionId()-1) ;
	            String actionLog =  myRequest.getHeaderDescription() ;// currAction.getHeaderDescription() ;
	            if( null == actionLog )
	            	actionLog = "" ;
	            // update the last action in db and put the RemovedUsers in the headerDescriptor
	            // get the last action's actionLog : 1. get action descriptor 2. create actionLog 3. put the removed users 4. update the header description
	            String updatedActionLog = actionLog ;
	            if( null != myRemovedUsers && myRemovedUsers.size() != 0 )
	            {
	            	updatedActionLog  += "\n[ Removed from recepients list: " + getUserLoginCSS(myRemovedUsers) + " ]";
	            }
	            
	            if( null != failedMailUserList && failedMailUserList.size() != 0 )
	            {	            	
	            	updatedActionLog += "\n[ Sending Mail failed for recepient list : " + getUserLoginCSS(failedMailUserList) + " ]\n" ;  
	            }
	            
	            LOG.info("Updating the action header description for : " + mySmartLink + " is : " + updatedActionLog ) ;	            
	            //
	            // Update this information in DB
	            //
	            try {
	                Request.updateHeaderDesc(myRequest.getSystemId(),myRequest.getRequestId(), aRequest.getMaxActionId(), updatedActionLog);
	            } catch (Exception e) {
	            	LOG.info("Updating the action header Failed." ) ;
	                LOG.severe("",(e));	                
	            }
            }
            
            long end = System.currentTimeMillis();

            LOG.info(mySmartLink + ": Total time taken to send mails : " + (end - start) + " msecs.");
        } catch (DatabaseException e) {
            LOG.severe("Error for " + mySmartLink + "\nTrying to Send again\n." + "",(e));
        } catch (Exception e) {
            LOG.severe("Error for " + mySmartLink + "\n" + "",(e));
        }

        return;
    }

    //~--- get methods --------------------------------------------------------

    public void mailAllUsers( ArrayList<User> recepientsList, int aSeverityFlag,String myXTBitsRecepients2, Hashtable<String, String> extHdrs) throws DatabaseException 
    {
        
		Collection<ActionFileInfo> actionFileInfos = myActionFileHash.get(myRequest.getMaxActionId()) ;
		
		if( null != actionFileInfos )
		{			
			// make the files that have been "deleted" to appear as link by default
			for( ActionFileInfo afi : actionFileInfos )
			{
				if(afi.getFileAction().equalsIgnoreCase("D"))
					afi.setPriority(-1);
			}		
		}

		LOG.info("Start sending mail to all users : " + getUserLoginCSS(recepientsList) + " : for the request : " + mySmartLink ) ;    	
    	for (User recepient: recepientsList )
    	{
    		if( false == recepient.getIsActive())
    		{
    			LOG.info("Not sending e-mail to as the user is inactive : " + recepient + " for request : " + mySmartLink);
    			continue;
    		}
    		if( null == recepient.getEmail() || recepient.getEmail().trim().equals("") )
    		{
    			LOG.error("Mail will not be sent to user " + recepient.getUserLogin() + "(email-id :"  + recepient.getEmail() + ") for the request : " + mySmartLink + " because of illegal email-id." );
    			failedMailUserList.add(recepient);
    			continue;
    		}
    		
    		try
    		{
	    		Hashtable<String, Integer> origPermissions = RolePermission.getPermissionsBySystemIdAndRequestIdAndUserId(myRequest.getSystemId(), myRequest.getRequestId(),  recepient.getUserId());	            
	    		Hashtable<String,Integer> clonedPermissions = (Hashtable<String,Integer>) SerialObjectCloner.copy(origPermissions);
	    		
	    		// cloning the request object 
	    		Request clonedRequest = (Request) SerialObjectCloner.copy(myRequest)  ;
    		
	    		User clonedRecepient = (User) SerialObjectCloner.copy(recepient);
	    		
	    		Hashtable<Integer,Collection<ActionFileInfo>> clonedActionFileHash = (Hashtable<Integer,Collection<ActionFileInfo>>) SerialObjectCloner.copy(myActionFileHash);
	    		ArrayList<Action> clonedActionList = (ArrayList<Action>)SerialObjectCloner.copy(myActionList);	    		
	    		
	    		MimeMultipart mp = generateMailForUser(clonedRecepient, clonedRequest, clonedActionList, clonedActionFileHash, clonedPermissions);
	    		
	    		String subject = myBusinessArea.getSystemPrefix() + "#" + clonedRequest.getRequestId() + ": " + clonedRequest.getSubject();
	    		
	            String fromEmail =  "\""+User.lookupByUserId(clonedRequest.getUserId()).getDisplayName()+"\""+"<"+myBusinessArea.getEmail()+">";
	            
	    	    Mail.sendHtmlAndAttachments(getUserEmail(recepient), fromEmail, subject, myBusinessArea.getFirstEmail(), aSeverityFlag, myXTBitsRecepients2, extHdrs, mp);
	    	   	    	   
	    	    LOG.info( "Finished sending mail to the user : " + recepient.getUserLogin() + " with email_id= " + getUserEmail(recepient) +" : for the request : " + mySmartLink ) ;    		    	
    		}
    		catch(NoEmailPermException e)
    		{
    			LOG.info(e.getMessage());
    			LOG.info("",(e));
    		}
    		catch(Exception e)
    		{
    			LOG.info("",(e)) ; 
    			// add this user to failedMailUserList
    			failedMailUserList.add(recepient);
    			LOG.info("Exception while sending mail to the user : " + recepient.getUserLogin() + " : for the request : " + mySmartLink);
    		}
    	}    	
	}
   private void checkEmailPermissions(BusinessArea ba, Request request,User user, Hashtable<String,Integer> permissions) throws NoEmailPermException 
   {
	   if( null == permissions )
	   {
		   LOG.info("Null Permission table for user(" + user.getUserLogin() + ") for request " + mySmartLink );
		   throw new NoEmailPermException("Null Permission table for user(" + user.getUserLogin() + ") for request " + mySmartLink);
	   }
	   
	   Integer privatePerm = permissions.get(Field.IS_PRIVATE);
	   if( null == privatePerm )
		   privatePerm = 0 ;
	   
	   Boolean isReqPrivate = request.getIsPrivate() ;
	   Boolean isBaPrivate = ba.getIsPrivate() ;
	   
	   Integer rperm = permissions.get(Field.REQUEST);
	   if( null == rperm )
		   rperm = 0 ;
	   
	   Integer bperm = permissions.get(Field.BUSINESS_AREA);
	   if( null == bperm )
		   bperm = 0 ; 
	   
	   if( (bperm & Permission.EMAIL_VIEW) == 0 )
	   {
		   LOG.warn("User (" + user.getUserLogin() + ") does not have Email_View permission on the " + Field.BUSINESS_AREA + " field for " + mySmartLink ) ;
		   throw new NoEmailPermException("User (" + user.getUserLogin() + ") does not have Email_View permission on the " + Field.BUSINESS_AREA + " field for " + mySmartLink);
	   }
	   
	   if( (rperm & Permission.EMAIL_VIEW) == 0  )
	   {
		   LOG.warn("User (" + user.getUserLogin() + ")does not have Email_View permission on the " + Field.REQUEST + " field for " + mySmartLink );
		   throw new NoEmailPermException("User (" + user.getUserLogin() + ")does not have Email_View permission on the " + Field.REQUEST + " field for " + mySmartLink);
	   }
	   
	   if( isBaPrivate && ( (privatePerm & Permission.EMAIL_VIEW) == 0 ) )
	   {
		   LOG.warn("The Business Area (" + ba.getSystemPrefix() + ") is private but user (" + user.getUserLogin() + ") does not have Email_View permission on the " + Field.IS_PRIVATE + " field for " + mySmartLink );
		   throw new NoEmailPermException("The Business Area (" + ba.getSystemPrefix() + ") is private but user (" + user.getUserLogin() + ") does not have Email_View permission on the " + Field.IS_PRIVATE + " field for " + mySmartLink);
	   }
	   
	   if( isReqPrivate && ( (privatePerm & Permission.EMAIL_VIEW) == 0))
	   {
		   LOG.warn("The Request (" + mySmartLink +") is private but user(" + user.getUserLogin() + ") does not have Email_View permission on the " + Field.IS_PRIVATE + " field for " + mySmartLink);
		   throw new NoEmailPermException("The Request (" + mySmartLink +") is private but user(" + user.getUserLogin() + ") does not have Email_View permission on the " + Field.IS_PRIVATE + " field for " + mySmartLink);
	   }
   }

/**
    * This function implements the basic logic for deciding which files can be attached in the mail
    * and which should be sent as link
    * @param request : The request object corresponding to the mail
    * @param actionFileHash : All the attachments related to this requests and actions. After the execution. The files corresponding to the maxAction will contain the files that should appear as link in email
    * @param attachedFiles : After the execution this will contain all the files that needs to be attached to the mail
    * @param permissions
    */
	private void attachmentDecision(Request request, Hashtable<Integer,Collection<ActionFileInfo>> actionFileHash, ArrayList<ActionFileInfo> attachedFiles, Hashtable<String,Integer> permissions ) 
	{
		List<ActionFileInfo> actionFileInfos = (List<ActionFileInfo>) actionFileHash.get(request.getMaxActionId());
		
		if(null == actionFileInfos) 
			return ;
			
		long maxAttSize = 1024*1024 ;
		try
		{
			maxAttSize = Long.parseLong(PropertiesHandler.getProperty(MailProperties.MAX_EMAIL_ATT_SIZE_PROP_NAME)) ;
		}
		catch(NumberFormatException nfe)
		{
			LOG.info("",(nfe));
			LOG.info(MailProperties.MAX_EMAIL_ATT_SIZE_PROP_NAME + " was not set properly. Taking this value to be 1024*1024.");
		}

		// sort in descending order
		Comparator<ActionFileInfo> comp = new Comparator<ActionFileInfo>()
		{
			public int compare(ActionFileInfo arg0, ActionFileInfo arg1) {
				double d = arg0.getPriority() - arg1.getPriority() ;
				if( 0 == d )
				{
					return arg0.getName().compareTo(arg1.getName()) ;
				}
				else if ( d > 0 )
				{
					return -1 ;
				}				
				
				return 1 ;
			}
		};
				
		Collections.sort(actionFileInfos, comp);
		
		long sizeLeft = maxAttSize ;
		for( Iterator<ActionFileInfo> afIter = actionFileInfos.iterator() ; afIter.hasNext() ; )
		{	
			ActionFileInfo afi = afIter.next() ;
			// check the permissions of the attachement field
			Integer perm = 0 ;
			String fieldName = null ; 
			try
			{
				fieldName = Field.lookupBySystemIdAndFieldId(afi.getSystemId(), afi.getFieldId()).getName() ;
				if(fieldName == null )
					continue ;
				perm = permissions.get(fieldName);
				if( perm == null || ( (perm.intValue() & Permission.EMAIL_VIEW) == 0 ) )
					continue ;
			}
			catch(Exception e)
			{
				LOG.info("Exception occured while accessing field  (sysId#fieldId): " + afi.getSystemId() + "#" + afi.getFieldId() + ". The attachments of this field will not be sent" );
				LOG.info("",(e));
				continue ;
			}
			
			// if the priority is negative then it will definitely be sent as link.
			// else we will try to add it into attachment before any file whose priority is less then its priority
			if( afi.getPriority() >= 0 && afi.getSize() > 0 && sizeLeft >=  afi.getSize() )				
			{
				attachedFiles.add(afi) ;
				sizeLeft -= afi.getSize() ;				
//				afIter.remove() ;			
				afi.setAttached(true);
			}		
		}	
	}

	public MimeMultipart generateMailForUser(User clonedRecepient, Request clonedRequest, ArrayList<Action> clonedActionList, Hashtable<Integer, Collection<ActionFileInfo>> clonedActionFileHash, Hashtable<String, Integer> clonedPermissions) throws Exception
	{
		/*
		 * Execute the MailPreProcessors
		 */
		
		try
		{
			MailProcessorFactory.getInstance().executeMailPreProcessors(clonedRecepient, clonedRequest, clonedActionList, clonedActionFileHash, clonedPermissions);
		}
		catch(Exception e)
		{
			e.printStackTrace();
			LOG.error(e);
		}


		BusinessArea ba = BusinessArea.lookupBySystemId(clonedRequest.getSystemId());
		
		// check the permissionsrecepient
		checkEmailPermissions(ba, clonedRequest,clonedRecepient,clonedPermissions);
		
		ArrayList<ActionFileInfo> attachedFiles = new ArrayList<ActionFileInfo>() ;

//	 * Decide which files will be sent as link and which will be attached in the mail.
		attachmentDecision( clonedRequest, clonedActionFileHash, attachedFiles, clonedPermissions );	    		
				
        Comparator<Action> cact = new Comparator<Action>()
        {
			public int compare(Action arg0, Action arg1) 
			{
				return ( arg0.getActionId() - arg1.getActionId() ) * (-1) ;
			}	            	
        };
        
        Collections.sort(clonedActionList,cact);
        
		MimeMultipart mp = null;
		
//		boolean exceptionInCreatingTextContents = false;
//		String textContent = "" ;
//		try
//		{
//		  textContent = getTextContent(clonedRecepient,clonedRequest, clonedActionList, clonedPermissions, clonedActionFileHash );
//		}
//		catch( Exception e )
//		{
//			e.printStackTrace() ;
//			LOG.info("Exception in creting text email contents for user : " + clonedRecepient.getUserLogin() + " : for the request : " + mySmartLink ,e ) ;
//			LOG.info("",(e)) ;
//			exceptionInCreatingTextContents = true ;
//		}
		
		String aHtmlContent = "" ;
		try
		{
			aHtmlContent = getHtmlContent( clonedRecepient,clonedRequest, clonedActionList, clonedPermissions, clonedActionFileHash );
		}
		catch(Exception e )
		{
			e.printStackTrace() ;
			LOG.info("Exception in creting html email contents for user : " + clonedRecepient.getUserLogin() + " email_id= " + getUserEmail(clonedRecepient) + " : for the request : " + mySmartLink ,e ) ;
			LOG.info("",(e)) ;
//			if( exceptionInCreatingTextContents == true )
//			{
//				LOG.info("Neither the text nor html contents for the user can be generated so not sending mail to the user : " + clonedRecepient.getUserLogin() + " for the request : " + mySmartLink) ;
//				throw e ;
//			}
			throw e;
		}

		// If action has attachment and any external address to send
		// mails to, send separate mails with attachment attached
		// in the mail itself.
		mp = new MimeMultipart("mixed");
		int a = 0;
		
//		MimeBodyPart mbp1 = new MimeBodyPart();	
		
//		mbp1.setText(textContent, "utf-8");
//		mbp1.setHeader("MIME-Version", "1.0");
//		mbp1.setHeader("Content-Type","text/plain; charset=\"utf-8\"");
//		mbp1.setHeader("Content-Transfer-Encoding", "quoted-printable");
//		mp.addBodyPart(mbp1, a);
//		a++;
		
		// create and Fill the Html message part
		MimeBodyPart mbp = new MimeBodyPart();	
		mbp.setContent(aHtmlContent, "text/html; charset=\"utf-8\"");
		mbp.setHeader("MIME-Version", "1.0");
		mbp.setHeader("Content-Type","text/html; charset=\"utf-8\"");
		mbp.setHeader("Content-Transfer-Encoding", "quoted-printable");
		mp.addBodyPart(mbp, a);
		a++;
		
    	            
        File attDirfile = new File(APIUtil.getAttachmentLocation());
		
        for (ActionFileInfo fileInfo : attachedFiles ) 
        {
           // String location = Uploader.getFileLocation(attachment.repoFileId);
            File file = new File(attDirfile, fileInfo.getLocation());
            FileDataSource fds  = new FileDataSource(file);
            MimeBodyPart   mbp2 = new MimeBodyPart();
            mbp2.setDataHandler(new DataHandler(fds));
            mbp2.setDisposition(Part.ATTACHMENT);
            mbp2.setFileName(fileInfo.getName());
            mp.addBodyPart(mbp2, a);
            a++;
        }
        
        return mp ;

	}
	
	/**
     * Method to get Date disaplyed as per Business area DateTimeFormat
     *
     * @param aDate DateTime as Timestamp
     *
     * @return DateString as per Business area DateTimeFormat configuration
     */
    private String getDateString(Timestamp aDate) {
        if (aDate == null) {
            return "-";
        }

        try {
            DateTimeFormat dateTimeFormat = DateTimeFormat.lookupByDateTimeFormatId(myBusinessArea.getSysConfigObject().getEmailDateFormat());

            if (dateTimeFormat == null) {
                return aDate.toDateMin();
            }

            return aDate.toSiteTimestamp().toCustomFormat(dateTimeFormat.getFormat());
        } catch (NullPointerException e) {
            LOG.debug("",(e));

            return aDate.toDateMin();
        } catch (Exception e) {
            LOG.debug("",(e));

            return aDate.toDateMin();
        }
    }

    /**
     * Method to get Date disaplyed as per Business area DateTimeFormat
     *
     * @param aDate DateTime as Date
     *
     * @return DateString as per Business area DateTimeFormat configuration
     */
    private String getDateString(Date aDate) {
        if (aDate == null) {
            return "-";
        }

        try {
            DateTimeFormat dateTimeFormat = DateTimeFormat.lookupByDateTimeFormatId(myBusinessArea.getSysConfigObject().getEmailDateFormat());

            if (dateTimeFormat == null) {
                return Timestamp.toDateMin(aDate);
            }

            return Timestamp.toCustomFormat(Timestamp.toSiteTimestamp(aDate),dateTimeFormat.getFormat());
        } catch (NullPointerException e) {
            LOG.debug("",(e));

            return Timestamp.toDateMin(aDate);
        } catch (Exception e) {
            LOG.debug("",(e));

            return Timestamp.toDateMin(aDate);
        }
    }

    
    /**
     */
    private static String getDay(int aDay) {
        switch (aDay) {
        case Calendar.MONDAY :
            return "monday";

        case Calendar.TUESDAY :
            return "tuesday";

        case Calendar.WEDNESDAY :
            return "wednesday";

        case Calendar.THURSDAY :
            return "thursday";

        case Calendar.FRIDAY :
            return "friday";

        case Calendar.SATURDAY :
            return "saturday";

        case Calendar.SUNDAY :
            return "sunday";
        }

        return "";
    }

    /**
     */
    private String getTBitsRecepients(ArrayList<User> aRecepientsList) {
        StringBuilder sb = new StringBuilder();

        if ((aRecepientsList != null) && (aRecepientsList.size() > 0)) {
            for (User user : aRecepientsList) {
                if (user.getIsActive()) {
                    sb.append(user.getUserLogin()).append(",");
                }
            }
        }

        if (sb.length() > 0) {
            String emailStr = (sb.toString()).substring(0, (sb.toString()).length() - 1);

            emailStr = emailStr.replaceAll(".transbittech.com", "");
            emailStr = emailStr.replaceAll("@transbittech.com", "");

            return emailStr;
        }

        return "";
    }

    /**
     */
    private String getExternalHtmlContent(Request request, ArrayList<Action> actionList, Hashtable<String,Integer> permissions, Hashtable<Integer, Collection<ActionFileInfo>> actionFileHash) throws Exception {

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
//        Hashtable<String, Integer> permissions = RolePermission.getPermissionsBySystemIdAndRequestIdAndUserId(systemId, requestId, aUser.getUserId());

        //
        // Load Html Template
        //
        DTagReplacer hp = new DTagReplacer(EXTERNAL_HTML_FILE);
        //DTagReplacer hp = new DTagReplacer("D:/tbits/dist/build/webapps/web/tbits-external-email-template.htm");     
       
        //
        // Now Fetch all actions from DB and
        // 1) Fill Action Details.
        // 2) Put summary, updated By, and updated time in tagTable.
        fillExternalActionDetails(request, actionList, tagTable, permissions, actionFileHash);
        
        fillHeaderHtmlContainer(tagTable, request, permissions);
        ActionHelper.replaceTags(hp, tagTable, null); 
        hp.setHtmlFileContents(hp.parse(myBusinessArea.getSystemId()));
        hp.setHash(new HashMap<String, String>());
        //System.out.println("Tag Table: \n" + tagTable.toString());
        //
        // Fill fixed and extended fields Header with values
        // by replacing <%=field_label+> and <%=field_value%> tags.
        //
        Calendar cal        = Calendar.getInstance();
        TimeZone serverZone = cal.getTimeZone();
        String   dateFormat = null;

        try {
            DateTimeFormat dateTimeFormat = DateTimeFormat.lookupByDateTimeFormatId(myBusinessArea.getSysConfigObject().getEmailDateFormat());

            if (dateTimeFormat != null) {
                dateFormat = dateTimeFormat.getFormat();
            }
        } catch (NullPointerException e) {
            LOG.debug("",(e));
        }

        fillExternalHeaderData(tagTable, request, dateFormat, serverZone, permissions );
       
        ActionHelper.replaceTags(hp, tagTable, null); 
        //
        // Fill Severity and over Due symbol.
        //
        if (myRequest.getIsPrivate() == true) {
            tagTable.put("privateSymbol", "<td align=\"center\" width=\"16\">" + "<font size='4'><strong>" + "&dagger;" + "&nbsp;</strong></td>");
        }

        ActionHelper.fillSymbols(tagTable, IMG_PATH, request, true);

        //
        // Add Css file inline.
        //
        fillCssTag(tagTable);

        //
        // Put other information in Tag Table
        //
        String subject = myBusinessArea.getSystemPrefix() + "#" + request.getRequestId() + ": " + Utilities.htmlEncode(request.getSubject());

        tagTable.put("subjectLine", subject);
        tagTable.put("sysPrefix", myBusinessArea.getSystemPrefix());
        tagTable.put("sysName", myBusinessArea.getDisplayName());
        tagTable.put("requestId", "" + request.getRequestId());
        tagTable.put("path", PATH);
        tagTable.put("maxActionId", "" + request.getMaxActionId());

        /*
         * Check if the request is transferred one.
         */
        TransferredRequest tr = TransferredRequest.lookupBySourcePrefixAndRequestId(myBusinessArea.getSystemPrefix(), request.getRequestId());

        if (tr != null) {
            String targetPrefix    = tr.getTargetPrefix();
            int    targetRequestId = tr.getTargetRequestId();

            if (targetRequestId == -1) {
                tagTable.put("transferInfo", " <span class=\"b sx cbk\">Transfer to " + targetPrefix + "# pending" + "</span>&nbsp;&nbsp;&nbsp;");
            } else {
                tagTable.put("transferInfo", "<span class=\"l sx cbk b\">Transferred to " + targetPrefix + "#" + targetRequestId + "</span>&nbsp;&nbsp;&nbsp;");
            }

            tagTable.put("subRequestDisplay", "none");
        } else {
            tagTable.put("transferInfo", "");
            tagTable.put("subRequestDisplay", "");
        }

        //
        // Finally replace the tags
        // and print it out.
        //
        ActionHelper.replaceTags(hp, tagTable, null); 
        /*hp.setHtmlFileContents(hp.parse(myBusinessArea.getSystemId()));
        hp.setHash(new HashMap<String, String>());*/
        String outputwithBlockCSS = hp.parse(myBusinessArea.getSystemId());
        
        String       cssFileName = CSS_FILE + WebUtil.getCSSFile(mySysConfig.getEmailStylesheet(), myBusinessArea.getSystemPrefix(), true);
        DTagReplacer cssParser   = new DTagReplacer(cssFileName);
        //DTagReplacer cssParser   = new DTagReplacer("D:/tbits/dist/build/webapps/web/css/tbits_email.css");
        String myCss =  cssParser.parse(myBusinessArea.getSystemId());

        
        CSS css = new CSS(myCss);
//      WriteToFile("c:\\temp\\html\\tbits.css", myCss);        
//        WriteToFile("c:\\temp\\html\\outputwithblockcss.html", outputwithBlockCSS);
//        WriteToFile("c:\\temp\\html\\tbits_css.txt", css.GetStylesMap().toString());
        
        
//        WriteToFile("c:\\temp\\html\\newmail.html", s);
        String s = BlockToInlineCSS.ReplaceCSSClassWithInlineStyle(outputwithBlockCSS, css);
        return s;
    }
    
    public static String getHtmlMailContent(User forUser, Request request, ArrayList<Action> actionList, Hashtable<String,Integer> permissions, Hashtable<Integer, Collection<ActionFileInfo>> actionFileHash){
		
    	try {
			return (new TBitsMailer(request)).getHtmlContent(forUser, request, actionList, permissions, actionFileHash);
		}
		catch (DatabaseException e) {
			e.printStackTrace();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
    	return null;
    }
    
    /**
     * @param permissions 
     */
    private String getHtmlContent(User forUser, Request request, ArrayList<Action> actionList, Hashtable<String,Integer> permissions, Hashtable<Integer, Collection<ActionFileInfo>> actionFileHash) throws Exception {

        //
        // Tag Table contains all the [tag_name, value] pairs. Finally we call
        // the replaceTags method that checks for each of the tag present in
        // the static tagList ArrayList in this tagTable for a value. If not
        // found such a tag is replaced with empty string. If found, such a
        // tag is replaced with the value found.
        //
        Hashtable<String, String> tagTable = new Hashtable<String, String>();

        //
        // Load Html Template
        //
        DTagReplacer hp = new DTagReplacer(HTML_FILE);

        fillHeaderHtmlContainer(tagTable, request, permissions);
        
        //
        // Replace all Html container Tags , so the template is now complete,
        // with <%=field_label%> and <%=field_value%> tags.
        // These tags will be filled/replaced after this.
        //
        ActionHelper.replaceTags(hp, tagTable, null);
        hp.replace("Email_to", PropertiesHandler.getProperty("transbit.tbits.auth.emailto"));
        hp.setHtmlFileContents(hp.parse(myBusinessArea.getSystemId()));
        hp.setHash(new HashMap<String, String>());

        // Now Fetch all actions from DB and
        // 1) Fill Action Details.
        // 2) Put summary, updated By, and updated time in tagTable.
        // 3) Put memo, updated By, and updated time in tagTable.        
        fillActionDetails( request, forUser, actionList, tagTable, permissions, actionFileHash);

        Calendar cal        = Calendar.getInstance();
        TimeZone serverZone = cal.getTimeZone();
        String   dateFormat = null;

        try{
            DateTimeFormat dateTimeFormat = DateTimeFormat.lookupByDateTimeFormatId(myBusinessArea.getSysConfigObject().getEmailDateFormat());

            if (dateTimeFormat != null) {
                dateFormat = dateTimeFormat.getFormat();
            }
        } catch (NullPointerException e) {
            LOG.debug("",(e));
        }
        
        ActionHelper.fillEmailHeaderData(forUser, tagTable, request, PATH, myBusinessArea.getMaxEmailActions(), dateFormat, serverZone, new ArrayList<String>(), myBusinessArea.getSystemId(), SOURCE_EMAIL,
                                    STATIC_TOOLTIP, permissions, actionFileHash);

        //
        // Put other information in Tag Table
        //
        String subject = "<a class=\"l cb\" href=\"" + getRequestUrl(request.getRequestId() + "", myBusinessArea.getSystemPrefix()) + "\" target=\"" + myBusinessArea.getSystemPrefix() + "_"
                         + request.getRequestId() + "\">" + myBusinessArea.getSystemPrefix() + "#" + request.getRequestId() + "</a>: " + Utilities.htmlEncode(request.getSubject());

        tagTable.put("subjectLine", subject);
        tagTable.put("titleSubject", subject);
        tagTable.put("sysPrefix", myBusinessArea.getSystemPrefix());
        tagTable.put("requestId", "" + request.getRequestId());
        tagTable.put("path", PATH);
        tagTable.put("maxActionId", "" + request.getMaxActionId());

        tagTable.put("add-subrequest-url", getAddSubRequestUrl(myBusinessArea.getSystemPrefix(), request.getRequestId()));
        tagTable.put("update-request-url", getUpdateRequestUrl(myBusinessArea.getSystemPrefix(), request.getRequestId()));
        tagTable.put("add-request-url", getAddRequestUrl(myBusinessArea.getSystemPrefix()));
        WebUtil.setInstanceBold(tagTable, myBusinessArea.getSystemPrefix());

        //
        // Finally replace the tags
        // and print it out.
        //
        ActionHelper.replaceTags(hp, tagTable, null);
        
        String outputwithBlockCSS = hp.parse(myBusinessArea.getSystemId());
        
        String       cssFileName = CSS_FILE + WebUtil.getCSSFile(mySysConfig.getEmailStylesheet(), myBusinessArea.getSystemPrefix(), true);
        DTagReplacer cssParser   = new DTagReplacer(cssFileName);
        String myCss =  cssParser.parse(myBusinessArea.getSystemId());

        
        CSS css = new CSS(myCss);
        String s = BlockToInlineCSS.ReplaceCSSClassWithInlineStyle(outputwithBlockCSS, css);
        return s;
    }
    
    
	/**
     * This method fills the extended header Container for fields over which
     * user has view permissions.
     * @param aHttpRequest TODO
     * @param aTagTable      Hashtable collecting <field,value> pairs
     * @param aPermissions   User permissions
     * @param aRequest       Request object
     */
    private void fillHeaderHtmlContainer(Hashtable<String, String> aTagTable, Request aRequest, Hashtable<String, Integer> aPermissions)
            throws DatabaseException, FileNotFoundException, IOException {

    	int systemId   = aRequest.getSystemId();        
        ArrayList<Field> fieldsList = Field.lookupBySystemId(systemId);
        StringBuilder extendedFields     = new StringBuilder();
        if ((fieldsList != null) && (fieldsList.size() > 0)) {            
            int counter=0;

            ArrayList<DisplayGroup> dgList = DisplayGroup.lookupIncludingDefaultForSystemId(systemId);
            Collections.sort(dgList, new Comparator<DisplayGroup>(){

				@Override
				public int compare(DisplayGroup o1, DisplayGroup o2) 
				{
					if( o1.getDisplayOrder() < o2.getDisplayOrder() )
						return -1;
					else return 1;
				}
            	
            });
            
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
            Hashtable<Integer,String> fullLengthFieldValues = new Hashtable<Integer,String>() ;
            
            for (Field field : fieldsList) {   
            	Integer perm = aPermissions.get(field.getName());
            	int permission = 0;
            	
            	if (perm != null) {
            		permission = perm;
            	}

            	if ((permission & Permission.EMAIL_VIEW) == 0) {
            		continue;
            	}
            	
//            	if( field.getDataTypeId() == DataType.ATTACHMENTS )
//            		continue ;
            	
            	if(field.getName().equals(Field.DESCRIPTION) || field.getName().equals(Field.REQUEST) || field.getName().equals(Field.BUSINESS_AREA) || field.getName().equals(Field.SUBJECT))
            		continue; // description will come in action details also. So not showing here.
            	
            	int displayGroup = field.getDisplayGroup();
            	if(displayGroup == 0 || displayGroup == 1 )  //since earlier the id of display_group was 0. now it is 1
            	{
            		DisplayGroup defaultDP = getDefaultDisplayGroup(systemId);
            		if( null == defaultDP )
            			continue ;
            		displayGroup = defaultDP.getId();
            	}
            	Integer counterInteger = dgCountMap.get(displayGroup);
            	if(counterInteger != null)
            	{
            		counter = counterInteger.intValue();
            	}
            	else
            		counter = 0;
            	
            	Integer bufIndexInt = dgMap.get(displayGroup);
            	int bufIndex = 0;
				if(bufIndexInt != null)
            		bufIndex = bufIndexInt.intValue();
            	if (bufArray[bufIndex] == null)
            		bufArray[bufIndex] = new StringBuffer();
            	
            	// for putting all text fields at the end of each display group
            	if( field.getDataTypeId() == DataType.TEXT || field.getDataTypeId() == DataType.ATTACHMENTS)
            	{
            		// assuming the default to true for the property
            		if( field.getDataTypeId() == DataType.ATTACHMENTS && PropertiesHandler.getProperty(SHOW_ATTACHMENT_IN_EMAIL_HEADER) != null && PropertiesHandler.getProperty(SHOW_ATTACHMENT_IN_EMAIL_HEADER).equalsIgnoreCase("false"))
            			continue;
            		
            		StringBuffer fullLengthHTML = new StringBuffer() ;
            		fullLengthHTML.append("\n<tr align=\"left\" valign=\"top\">").append("<td width=\"2%\"></td>").append(EXTENDED_FULL_LENGTH_FIELD_HTML.replace("<=label>",
            				"<%=" + field.getName() + "_label%>:").replace("<=value>", "<%=" + field.getName() + "%>") ).append("</tr>") ;
            		
            		String allFullLengthHtml = fullLengthFieldValues.get(bufIndex) ;
            		if( allFullLengthHtml == null )
            			allFullLengthHtml = "" ;
            		
            		allFullLengthHtml += fullLengthHTML.toString() ;
            		fullLengthFieldValues.put(bufIndex, allFullLengthHtml);
            		// we will not increase counter in this case.
            	}         	
            	else if ((counter == 0) || ((counter%3)==0)){
            		//if ((counter==0) && (displayGroup != DEFAULT_DISPLAY_GROUP)){
            		if (counter==0){
            			bufArray[bufIndex].append("<tr align=\"left\" style=\"font-weight:bold;font-family:verdana;font-weight:bold;color:#000000;white-space:nowrap\"")
            			.append("valign=\"top\">").append("<td colspan='3'>").append(DisplayGroup.lookupByDisplayGroupId(displayGroup).getDisplayName())
            			.append("</td>").append("</tr>");                			
            		}
            		bufArray[bufIndex].append("\n<tr align=\"left\" ").append("valign=\"top\">").append("<td width=\"2%\"></td>").append(EXTENDED_FIELD_HTML.replace("<=label>",
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
            
                       
            // append all textareavalues in the bufArray of corresponding displaygroups
            for( DisplayGroup dg : dgList )
            {
            	Integer counterInteger = dgCountMap.get(dg.getId());
            	int counter1 = 0 ;
            	if(counterInteger != null)            
            		counter1 = counterInteger.intValue();
            	           	
            	Integer bufIndexInt = dgMap.get(dg.getId());
            	int bufIndex = 0;
				if(bufIndexInt != null)
            		bufIndex = bufIndexInt.intValue();
				
            	if( null != bufArray[bufIndex] && ! bufArray[bufIndex].toString().trim().equals(""))
            	{
	            	if (bufArray[bufIndex].toString().trim().endsWith("</tr>") == false) 
	            	{
	                    if ((counter1 == 1) || ((counter1%3)==1)) {
	                        bufArray[bufIndex].append(EXTENDED_FIELD_HTML.replace("<=label>", "").replace("<=value>", "")).append(EXTENDED_FIELD_HTML.replace("<=label>", "").replace("<=value>",
	                                "")).append("</tr>");
	                        counter1 += 2 ;
	                    } else if ((counter1 == 2) || ((counter1%3)==2)) {
	                    	bufArray[bufIndex].append(EXTENDED_FIELD_HTML.replace("<=label>", "").replace("<=value>", "")).append("</tr>");
	                        counter1++ ;
	                    }
	                }  
            	}
            	
            	String allTextHtml = fullLengthFieldValues.get(bufIndex) ;
            	
            	if( allTextHtml != null && ! allTextHtml.toString().trim().equals(""))
            	{            		
            		if (counter1==0){
            			bufArray[bufIndex].append("<tr align=\"left\" style=\"font-weight:bold;font-family:verdana;font-weight:bold;color:#000000;white-space:nowrap\"")
            			.append("valign=\"top\">").append("<td colspan='3'>").append(DisplayGroup.lookupByDisplayGroupId(dg.getId()).getDisplayName())
            			.append("</td>").append("</tr>");                			
            		}
            		
            		bufArray[bufIndex].append(allTextHtml) ;
            	}
            	
            	if ((bufArray[bufIndex]!= null) && (!bufArray[bufIndex].toString().trim().equals("")))
            		extendedFields.append(bufArray[bufIndex]);
            }
        }

        aTagTable.put("allFields", extendedFields.toString());
        
        return;
    }

    private DisplayGroup getDefaultDisplayGroup(int systemId) {
    	ArrayList<DisplayGroup> list = null;	
    	try
    	{
    		list = DisplayGroup.lookupBySystemId(systemId);
    	}
    	catch(Exception e)
    	{
    		e.printStackTrace();
    	}
    	if( null != list )
    	{
    		for( DisplayGroup dg : list )
    		{
    			if( dg.getIsDefault() && dg.getIsActive() )
    				return dg ;
    		}
    		
    	}
		return null;
	}			

	private void WriteToFile(String fileName, String contents)
    {
    	File f = new File(fileName);
    	BufferedWriter bw;
		try {
			bw = new BufferedWriter(new FileWriter(f));
			bw.write(contents);
			bw.flush();
			bw.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
    /**
     * This method returns the length of longest DisplayName of any
     *  Fixed Field. This is used for formatting of Header in Text mail.
     */
    private int getMaxLength(ArrayList<Field> fieldList) 
    {                
        if(fieldList.size() == 0 )
        	return 50 ;

        int maxLength = 0;

        for (Field field : fieldList) {
            maxLength = (maxLength > field.getDisplayName().length())
                        ? maxLength
                        : field.getDisplayName().length();
        }

        return maxLength;
    }

    private ArrayList<User> getRecepientsList() throws Exception {
          return getRecepientsList(myBusinessArea, myRequest);
    }
    
    /**
     * 
     * @param aBA
     * @param aRequest
     * @return
     * @throws Exception
     */
    private ArrayList<User> getRecepientsList(BusinessArea aBA, Request aRequest) throws Exception {
        int systemId = aBA.getSystemId();
        User aUser = User.lookupByUserId( aRequest.getUserId() );

        //
        // This will conatin the list of typeusers for all fields
        // and global list also
        //
        ArrayList<TypeUser> typeUsers = new ArrayList<TypeUser>();

        //
        // Get Category User
        //
        ArrayList<TypeUser> temp = TypeUser.lookupBySystemIdAndFieldNameAndTypeId(systemId, Field.CATEGORY,
                aRequest.getCategoryId().getTypeId());

        if (temp != null) {
            typeUsers.addAll(temp);
        }

        //
        // Get Status User
        //
        temp = TypeUser.lookupBySystemIdAndFieldNameAndTypeId(systemId, Field.STATUS, aRequest.getStatusId().getTypeId());

        if (temp != null) {
            typeUsers.addAll(temp);
        }

        //
        // Get Severity User
        //
        temp = TypeUser.lookupBySystemIdAndFieldNameAndTypeId(systemId, Field.SEVERITY, aRequest.getSeverityId().getTypeId());

        if (temp != null) {
            typeUsers.addAll(temp);
        }

        //
        // Get Request type User
        //
        temp = TypeUser.lookupBySystemIdAndFieldNameAndTypeId(systemId, Field.REQUEST_TYPE, aRequest.getRequestTypeId().getTypeId());

        if (temp != null) {
            typeUsers.addAll(temp);
        }

        //
        // Get Global Users
        //
        temp = TypeUser.getGlobalListBySystemId(systemId);

        if (temp != null) {
            typeUsers.addAll(temp);
        }


        // This will contain list of all users to be notified.
        HashSet<User> users = new HashSet<User>();
        
        // add all typeUsers to the users list after checking the notification rules.
        
        users.addAll(getUsersByNotificationRules(typeUsers));
       
        if (aRequest.getNotifyLoggers() != false) 
        {
        	// Add Loggers
        	addRequestUsersToList(users, aRequest.getLoggers());        	
        }

        // Add Assignees
        addRequestUsersToList(users, aRequest.getAssignees());

        // Add Subscribers
        addRequestUsersToList(users, aRequest.getSubscribers());

        // Add Tos
        addRequestUsersToList(users, aRequest.getTos());

        // Add Ccs
        addRequestUsersToList(users,aRequest.getCcs());
        
        addAllExUserTypesToList(users,aRequest);

        // add appender if the ba's notify appender is true
        if( aRequest.getMaxActionId() > 1 && myBusinessArea.getSysConfigObject().getNotifyAppender() == true )
        	users.add(User.lookupByUserId(aRequest.getUserId())) ;
      
        // Resolve all mailling lists
        
        HashSet<User> usersList = new HashSet<User>() ;
        
        for( User user : users ) // Iterator<User> iter = users.iterator() ; iter.hasNext() ; )
        {
//        	User user = iter.next() ;
        	if( user.getUserTypeId() != UserType.INTERNAL_MAILINGLIST  )
        	{
        		usersList.add(user) ;
        	}
        	else
        	{
        		HashSet<User> resolvedUsers = APIUtil.resolveMailList(user) ;
        		if( resolvedUsers != null )
        			usersList.addAll(resolvedUsers);
        	}
        }

        // Load permissions
//        loadPermissions(usersList);

        // If request private, remove unauthorized users from mail List
        // this will be checked separately when sending mail to each particular user.
  //      checkAccessPermissions(usersList);

        ArrayList<User> uniqueUsers = new ArrayList<User>() ;
                
        // if user.email == someBA.email --> remove user : add into removed users list
        // if user.thisba.notify == false --> remove user
        // else add them to the recepient list
        for( User user : usersList )
        {
        	if( APIUtil.isBAEmail(user.getEmail()) == true )
        	{
        		LOG.info("Removing user " + user.getUserLogin() + "  from recepient list for request : " + myBusinessArea.getSystemPrefix() + "#" + myRequest.getRequestId() + " : because user.email is same as some ba's email.") ;
        		myRemovedUsers.add(user) ;
        		continue ;
        	}
        	
        	WebConfig uwc = user.getWebConfigObject();
        	BAConfig ubac = null ;
        	
        	if( uwc != null )
        		ubac = uwc.getBAConfig(myBusinessArea.getSystemPrefix());
        	
        	if( null != ubac && ubac.getNotify() == false )
        	{
        		LOG.info("Removing user " + user.getUserLogin() + "  from recepient list for request : " + myBusinessArea.getSystemPrefix() + "#" + myRequest.getRequestId() + " : because BA config has disabled mail.") ;
        		continue ;
        	}       	
       
        	uniqueUsers.add(user);
        	
        }
        
        // remove the users in exclusion list.
		String emailsList = PropertiesHandler.getProperty(MailProperties.AllowAutoReplyFrom_WithoutNotification);

		ArrayList<String> emails = Utilities.toArrayList(emailsList);
		if( null == emails )
			emails = new ArrayList<String>();

		for( Iterator<User> iter = uniqueUsers.iterator() ; iter.hasNext() ; )
		{
			User user = iter.next();
			if(null != user.getEmail() && emails.contains(user.getEmail().trim()))
			{
				iter.remove();
			}
		}
		
        myXTBitsRecepients = getTBitsRecepients(uniqueUsers);
        
        return uniqueUsers;
    }
    
    private void addAllExUserTypesToList(Collection<User> users, Request request) 
    {       	
    	try 
    	{
			ArrayList<Field> exUserTypeFields = Field.lookupBySystemId(request.getSystemId(), true, DataType.USERTYPE);
			
			for( Field f : exUserTypeFields )
			{
				Collection<RequestUser> rus = (Collection<RequestUser>) request.getObject(f);
				if( null != rus )
					addRequestUsersToList(users, rus);
			}
		} 
    	catch (DatabaseException e) 
    	{
    		LOG.info("Exception occured while finding the users in extended user-type for mailing : " + mySmartLink);
    		LOG.info("",(e));
    		
		}
	}

	/**
	 * @param aRequest
	 * @param aUser
	 * @return
	 */
	private boolean isExistsInRequestUsersAsLogger(Request aRequest, User aUser) {
		boolean isUserLogger = false;
        for (RequestUser ru : aRequest.getLoggers()){
        	if (ru.getUserId() == aUser.getUserId()){
        		isUserLogger = true;
        		break;
        	}        		
        }
		return isUserLogger;
	}

    private void getRelationsHtml(Hashtable<String, String> aTagTable, Hashtable<String, String> aRequestHash, String aDisplayText, String aTag, boolean aAddDelim) {
        String              req = "";
        String              sub = "";
        StringBuilder       sb  = new StringBuilder();
        Map<String, String> m   = Collections.synchronizedMap(new TreeMap<String, String>(new PrefixComparator()));

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
                buffer.append(getRequestUrl(req)).append("\" target='").append(req.replace("#", "_"));
            }

            buffer.append("' title=\"").append(Utilities.htmlEncode(aRequestHash.get(req))).append("\" >");

            if (aTag.equalsIgnoreCase("relatedRequests")) {
                buffer.append(req);
            } else {
                buffer.append(req.substring(req.indexOf("#") + 1));
            }

            cnt++;

            if ((cnt == 1) && e.hasNext()) {
                buffer = buffer.append(",&nbsp;");
            }

            buffer.append("</a>");

            if (cnt == 2) {
                break;
            }
        }

        int i = 1;

        req = "";
        e   = m.keySet().iterator();

        if (m.size() > 0) {
            sb.append("<span class=\" b cw crhb\" >").append(aDisplayText);

            if (m.size() > 2) {
                sb.append("&nbsp;(").append(m.size()).append(")");
            }

            sb.append(":&nbsp;").append("</span>").append(buffer.toString());

            if (m.size() > 2) {
                sb.append("\n<a class=\"l b cw\" href=\"#").append(aTag).append("\">").append(",&nbsp;more...").append("</a>");
            }

            aTagTable.put(aTag, sb.toString());
            e = m.keySet().iterator();

            if (m.size() > 0) {
                sb = new StringBuilder();
                sb.append("\n<div>\n<a name=\"").append(aTag).append("\" class=\"s b ch\">").append(aDisplayText).append(": </a>");

                while (e.hasNext()) {
                    req = e.next();

                    if (i != 1) {
                        sb.append(",");
                    }

                    sb.append("\n<a class=\"l sx cb\" href=\"").append(getRequestUrl(req)).append("\" target=\"").append(req.replace("#", "_")).append("\">");

                    if (aTag.equals("relatedRequests") == true) {
                        sb.append(req);
                    } else {
                        sb.append(req.substring(req.indexOf("#") + 1));
                    }

                    sb.append("</a>");
                    i++;
                }

                sb.append("\n</div>");
                aTagTable.put(aTag + "Details", "\n<tr>\n<td align=\"left\" " + "width=\"100%\">\n" + sb.toString() + "\n</td>\n</tr>\n");
            } else {
                aTagTable.put(aTag + "Details", "");
            }
        }

        return;
    }

    /**
     */
    private int getSeverityFlag(String aSeverity) {
        try {
            SysConfig         sysConfig = myBusinessArea.getSysConfigObject();
            ArrayList<String> lowList   = sysConfig.getOutgoingSeverityLow();
            ArrayList<String> highList  = sysConfig.getOutgoingSeverityHigh();

            if (lowList.contains(aSeverity) == true) {
                return -1;
            } else if (highList.contains(aSeverity) == true) {
                return 1;
            } else {
                return 0;
            }
        } catch (Exception e) {
            return 0;
        }
    }

    /**
     */
    private String getTextActionDetails( Request request,  ArrayList<Action> actionList,int aSystemId, String aSysPrefix, int aRequestId, boolean aBaMail, Hashtable<String,Integer> permissions, Hashtable<Integer, Collection<ActionFileInfo>> actionFileHash ) throws DatabaseException {
        StringBuilder actionDescription = new StringBuilder();
        int           maxEmailActions   = myBusinessArea.getMaxEmailActions();
        int           maxActionId       = request.getMaxActionId();

//        Hashtable<Integer, Collection<ActionFileInfo>> actionFileHash = Action.getAllActionFiles(myBusinessArea.getSystemId(), request.getRequestId());
        for (Action action : actionList) {
            if (action.getActionId() > maxActionId) {
                continue;
            }

            maxEmailActions--;

            if (maxEmailActions < 0) {
                break;
            }

            // When No. of actions of the request exceeds the max no.
            // of actions to be displayed in email, show the first action
            // in the end inplace of the maxth no. of action.
            if ((maxEmailActions == 0) && (action.getActionId() > 1)) {
                action = (Action) actionList.get(actionList.size() - 1);
            }

            actionDescription.append("------------------------------------").append("------------------------------------").append("\n").append(action.getActionId()).append(". ").append(
                ActionHelper.getUserDisplayName(action.getUserId()));
            actionDescription.append(": ").append(getDateString(action.getLastUpdatedDate())).append("\n\n") ; 
            
            if( ( permissions.get(Field.DESCRIPTION) & Permission.EMAIL_VIEW ) != 0 )
            {
            	// Getting the description
            	String description = action.getDescription();
            	actionDescription.append(description.trim()).append("\n");
            }

            //
            // Add headers description and attachments if not called for
            // Cc'ed Ba text mail
            //
            if (aBaMail == false) 
            {
            	Collection<ActionFileInfo> attachedFiles = actionFileHash.get(action.getActionId()) ;
                // Add Attachments links, if any            
                String attachStr = ActionHelper.formatEmailAttachments( attachedFiles, TBitsConstants.TEXT_FORMAT, PATH, aSysPrefix, request.getRequestId(), action.getActionId(), permissions);

                if ((attachStr != null) && (attachStr.trim().equals("") == false)) {
                    actionDescription.append("\n").append(attachStr);
                }

                String headerDesc = ActionHelper.formatActionLog(aSystemId, action.getHeaderDescription(), permissions, TBitsConstants.TEXT_FORMAT, true, "", "", 0, 0, 0, SOURCE_EMAIL, NO_TOOLTIP);

                if (!(headerDesc.trim()).equals("")) {
                    actionDescription.append("\n").append(headerDesc);
                }
            }
        }

        return actionDescription.toString();
    }

    /**
     * @param permissions 
     */
    private String getTextContent(User forUser, Request request, ArrayList<Action> actionList, Hashtable<String, Integer> permissions, Hashtable<Integer, Collection<ActionFileInfo>> actionFileHash ) throws Exception {
        StringBuffer textContent = new StringBuffer();
        int          requestId   = request.getRequestId();
        String       sysPrefix   = myBusinessArea.getSystemPrefix();

        //
        // add TBits reply Tag
        //
        textContent.append("____TBits__RE__" + sysPrefix + "#" + requestId + "#" + request.getMaxActionId() + "____").append("\n\n");

        //
        // To get the length of field with longest display Name
        // for formatting the Text mail header fields.
        //
        
        ArrayList<Field> fieldList = new ArrayList<Field>();

        ArrayList<Field> fixedFields = null ;
        try {
            fixedFields = Field.getFixedFieldsBySystemId(myBusinessArea.getSystemId());
        } catch (Exception e) {
            LOG.debug("",(e));
        }
        
        ArrayList<Field> extFields = null ;
        try {
           extFields = Field.getExtendedFieldsBySystemId(myBusinessArea.getSystemId());
        } catch (Exception e) {
            LOG.debug("",(e));
        }

        if ((fixedFields != null) && (fixedFields.size() != 0)) {
            fieldList.addAll(fixedFields) ;
        }
        
        if ((extFields != null) && (extFields.size() != 0)) {
            fieldList.addAll(extFields) ;
        }

        int    maxNameLength = getMaxLength( fieldList );
        String requestLink   = WebUtil.getServletPath("/Q/") + sysPrefix + "/" + requestId;

        Field reqField = Field.lookupBySystemIdAndFieldName(myBusinessArea.getSystemId(), Field.REQUEST) ;        
        textContent.append(addHeader( reqField.getDisplayName(), sysPrefix + "#" + requestId + " <" + requestLink + ">", maxNameLength) );

        int parentId = request.getParentRequestId();

        Field parentReq = Field.lookupBySystemIdAndFieldName(myBusinessArea.getSystemId(),Field.PARENT_REQUEST_ID) ;
        // If parent present add to header
        if (parentId > 0) {
            textContent.append(addHeader(parentReq.getDisplayName(), sysPrefix + "#" + parentId, maxNameLength));
        }

        StringBuffer myTextFieldsContents = new StringBuffer() ;
        for( Field field : fieldList )
        {
        	try
        	{
	        	String fieldName = field.getName() ;
	        	String fieldDisplayName = field.getDisplayName() ;
	        	int fieldDataTypeId = field.getDataTypeId() ;
	        	
	        	Integer mperm = permissions.get(fieldName) ;
	        	if( null == mperm )
	        		mperm = 0 ;
	        	
	        	if( (mperm.intValue() & Permission.EMAIL_VIEW ) == 0 )
	        		continue ;
	        	
	        	if( fieldName.equals(Field.BUSINESS_AREA) || 
	        		fieldName.equals(Field.REQUEST) ||
	        		fieldName.equals(Field.MEMO) ||        		 
	        		fieldName.equals(Field.APPEND_INTERFACE) ||
	        		fieldName.equals(Field.ACTION) || 
	        		fieldName.equals(Field.DESCRIPTION) || 
	        		fieldName.equals(Field.HEADER_DESCRIPTION)
	        	   )
	        	continue ;
	
	        	if( fieldDataTypeId == DataType.ATTACHMENTS ) 
	        		continue ;
	
	        	// add all other fields for which this user has the email permission
	        	switch( fieldDataTypeId )
	        	{
	        		case DataType.TEXT :
	        			myTextFieldsContents.append(addHeader(fieldDisplayName,request.get(fieldName),maxNameLength)) ;
	        			break ;
	        		case DataType.TYPE :
	        			String typeName = request.get( fieldName ) ;
	        			Type type =  Type.lookupAllBySystemIdAndFieldNameAndTypeName(myBusinessArea.getSystemId(), fieldName, typeName) ;
	        			textContent.append(addHeader(fieldDisplayName,type.getDisplayName(),maxNameLength));
	        			break ;
	        		default :
	        			textContent.append(addHeader(fieldDisplayName,request.get(fieldName),maxNameLength));       	
	        	}
        	}
        	catch(Exception e )
        	{
        		LOG.info("Exception while sending the mail for field : " + field.getName() + " for request : " + mySmartLink ) ;
        		e.printStackTrace() ;
        		LOG.info("",(e)) ;
        	}
        }
        
        textContent.append(myTextFieldsContents) ;
        
        // this is the action details
        textContent.append("\n\n").append(getTextActionDetails( request, actionList, request.getSystemId(), sysPrefix, request.getRequestId(), false, permissions, actionFileHash));

        return textContent.toString();
    }

	/**
     * Method to get the emails for the corresponing userIds
     *
     * @param  aUserIds the ArrayList of Ids
     * @return the corresponing emails for the Ids, seperated by commas
     * @throws DatabaseException
     */
    private String getUserEmails(ArrayList<User> aUserIds) throws Exception {
        StringBuilder emails   = new StringBuilder();
        User          tempUser = null;

        for (int i = 0; i < aUserIds.size(); i++) {
            tempUser = aUserIds.get(i);

            if (tempUser.getIsActive() == true) {
                emails.append("\"" + tempUser.getDisplayName() + "\"<" + tempUser.getEmail() +  ">");
                emails.append(",");
            }
        }

        if (emails.length() > 0) {
            String emailStr = (emails.toString()).substring(0, (emails.toString()).length() - 1);

            return emailStr;
        } else {
            return "";
        }
    }
    
    /**
     * Method to get the emails for the corresponing userIds
     *
     * @param  aUserIds the ArrayList of Ids
     * @return the corresponing emails for the Ids, seperated by commas
     * @throws DatabaseException
     */
    private String getUserEmail(User aUserId) throws Exception 
    {
    	// Niti msg : it is NOT the responsibility of this utility function to check whether the user is active or not
//        if (aUserId.getIsActive() == true) {
        	return "\"" + aUserId.getDisplayName() + "\"<" + aUserId.getEmail() +  ">";
//        }
//        else
//        	return "";
    }

    private ArrayList<User> getUsersByNotificationRules(ArrayList<TypeUser> aTypeUsers) throws Exception
    {
    	return getUsersByNotificationRules(aTypeUsers, myBusinessArea, myRequest);
    }
    /**
     * 
     */
    public static ArrayList<User> getUsersByNotificationRules(ArrayList<TypeUser> aTypeUsers, BusinessArea aBA, Request request ) throws Exception {
        ArrayList<User> users = new ArrayList<User>();
        if(aBA == null)
        	throw new IllegalArgumentException("Business area can not be null.");

        for (TypeUser typeUser : aTypeUsers) {

            //
            // Don't send mail as type analyst if user on vacation
            //
        		if (APIUtil.isOnVacation(typeUser.getUser(), aBA.getSystemId()) == false) {
                BAConfig baconfig = typeUser.getUser().getWebConfigObject().getBAConfig(aBA.getSystemPrefix());

                if ((baconfig != null) && (baconfig.getNotify() == false)) {
                    continue;
                }

                User user = checkNotificationRules(typeUser, request);

                if ((user != null) && (users.contains(user) == false)) {
                    users.add(user);
                }
            } else {
                LOG.info(aBA.getName() + ": TypeUser " + typeUser.getUser().getUserLogin() + " on vacation");
            }
        }

        return users;
    }
}
