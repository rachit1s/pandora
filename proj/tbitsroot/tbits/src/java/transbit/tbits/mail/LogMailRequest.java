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
 * LogMailRequest.java
 *
 * $Header:
 */
package transbit.tbits.mail;

//~--- non-JDK imports --------------------------------------------------------

import static transbit.tbits.Helper.TBitsConstants.PKG_MAIL;
import static transbit.tbits.Helper.TBitsConstants.TR_ACK_HEADER;
import static transbit.tbits.Helper.TBitsConstants.TR_FWD_HEADER;
import static transbit.tbits.Helper.TBitsConstants.TR_REQ_HEADER;
import static transbit.tbits.Helper.TBitsConstants.TR_SRC_REQUEST;
import static transbit.tbits.api.APIUtil.INSERT_RELATED_HEADER;
import static transbit.tbits.domain.Permission.VIEW;
import static transbit.tbits.exception.APIException.SEVERE;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Hashtable;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.mail.Address;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Message.RecipientType;
import javax.mail.internet.MimeMessage;

import transbit.tbits.Helper.ActionHelper;
import transbit.tbits.Helper.IUCValidator;
import transbit.tbits.Helper.LinkFormatter;
import transbit.tbits.Helper.SysPrefixes;
import transbit.tbits.Helper.TBitsConstants;
import transbit.tbits.Helper.TBitsHelper;
import transbit.tbits.Helper.TBitsPropEnum;
import transbit.tbits.api.APIUtil;
import transbit.tbits.api.AddRequest;
import transbit.tbits.api.AttachmentInfo;
import transbit.tbits.api.UpdateRequest;
import transbit.tbits.common.Configuration;
import transbit.tbits.common.DTagReplacer;
import transbit.tbits.common.DatabaseException;
import transbit.tbits.common.Mail;
import transbit.tbits.common.MailProcessor;
import transbit.tbits.common.PropertiesEnum;
import transbit.tbits.common.PropertiesHandler;
import transbit.tbits.common.TBitsLogger;
import transbit.tbits.common.Utilities;
import transbit.tbits.config.CaptionsProps;
import transbit.tbits.config.SysConfig;
import transbit.tbits.domain.Action;
import transbit.tbits.domain.BAMailAccount;
import transbit.tbits.domain.BusinessArea;
import transbit.tbits.domain.DActionLog;
import transbit.tbits.domain.Field;
import transbit.tbits.domain.Request;
import transbit.tbits.domain.RolePermission;
import transbit.tbits.domain.TransferredRequest;
import transbit.tbits.domain.User;
import transbit.tbits.events.BeforeEmailSubmitEvent;
import transbit.tbits.events.EventManager;
import transbit.tbits.exception.APIException;
import transbit.tbits.exception.TBitsException;
import transbit.tbits.webapps.WebUtil;

//~--- classes ----------------------------------------------------------------

/**
 * The LogMailRequest class is used to log/append the request into
 * the database via Mail
 *
 * @author Vinod Gupta, Nitiraj
 * @version $Id: $
 */
public class LogMailRequest implements TBitsPropEnum {

    // Application Logger.
    public static TBitsLogger   LOG           = TBitsLogger.getLogger(PKG_MAIL);
    private static final String PATH          = WebUtil.getServletPath("");
    private static final String MAILQ_FILE    = "etc/tbits.mailq";
    private static final String HTML_FILE     = "web/tbits-euc-notification-template.htm";
    private static final String FAIL_STATUS   = "Fail";
    private static final String CSS_FILE      = "web/css/";
    private static final String REMOVE_STATUS = "Remove";

    //~--- fields -------------------------------------------------------------

    private boolean       myIsCategorySpecified  = false;
    private boolean       myIsUnauthorizedUser   = true;
    private boolean       myIsPrivateByBA        = false;
    private boolean       myIsPrivate            = false;
    private MailProcessor myMailProcessor        = null;
    private BusinessArea  myBusinessArea         = null;
    private SysConfig     mySysConfig            = null;
    private Request       myRequest              = null;
    private User          myUser                 = null;
    private int           myTransferSrcRequestId = -1;
    private String        myTransferSrcPrefix    = "";
    private int           myReturnStatus         = 0;
    private int           myRepliedToAction      = 0;

    // This contains eucs not allowed on private requests,
    // populated while parsing eucs.
    private ArrayList<String> myNotAllowedPrivateEUCs = new ArrayList<String>();

    // This contains eucs not allowed on field, populated while parsing eucs.
    private ArrayList<String> myNotAllowedEUCs = new ArrayList<String>();

    // This contains all eucs for which user doesn't have permissions,
    // populated from API's thrown exceptions.
    private ArrayList<String> myNoPermissionEUCs = new ArrayList<String>();
    private boolean           myIsTransferReq    = false;
    private String            myFwdHeaderValue   = "";

    // This contains completed euc failure log of all types,
    // mailed to only logs
    private ArrayList<String> myFailedEUCs = new ArrayList<String>();
    private String            myErrorLog   = "";
    private String            myCategory   = "";

    // This contains all wrong descriptor eucs populated
    // while validating eucs.
    private ArrayList<String> myBadSyntaxEUCs = new ArrayList<String>();

    // This contains eucs not allowed on private requests,
    // populated while validating eucs.
    private ArrayList<String> myBadOperandPrivateEUCs = new ArrayList<String>();

    // This contains all wrong operand eucs populated from API's
    // thrown exceptions.
    private ArrayList<String> myBadOperandEUCs = new ArrayList<String>();

    // This contains all wrong descriptor eucs popultaed while parsing eucs.
    private ArrayList<String> myBadDescriptorEUCs = new ArrayList<String>();

    // All these EUCs arraylist are used to send proper euc errors to users.
    // This contains list of all user specified eucs
    private ArrayList<String> myAllEUCs       = new ArrayList<String>();
    private APIException      myAPIExceptions = null;

    // This stores userIds who have view private permissions on the request.
    private ArrayList<String> myAuthUsersForRequest;

    //~--- constructors -------------------------------------------------------

    /**
     * Default constructor.
     */
    public LogMailRequest() {

        // do-nothing.
    }

    //~--- methods ------------------------------------------------------------

    /**
     * This method sends EUC failure notification to the user and Cc a copy to
     * dev team.
     */
    public void SendEUCFailureUserNotification() {
        String smartLink         = myBusinessArea.getSystemPrefix() + "#" + myRequest.getRequestId() + "#" + myRequest.getMaxActionId();
        String subject           = "IUC Error Notification for " + smartLink;
        String failureLogAddress = PropertiesHandler.getProperty(KEY_LOGGER_NOTIFY_FROM);

        //
        // Load Html Template
        //
        DTagReplacer hp = null;

        try {
            hp = new DTagReplacer(HTML_FILE);
        } catch (Exception e) {
            LOG.severe("",(e));

            return;
        }

        Hashtable<String, String> tagTable    = new Hashtable<String, String>();
        String                    cssFileName = CSS_FILE + WebUtil.getCSSFile(mySysConfig.getEmailStylesheet(), myBusinessArea.getSystemPrefix(), true);
        DTagReplacer              cssParser   = null;

        try {
            cssParser = new DTagReplacer(cssFileName);
        } catch (Exception e) {
            LOG.severe("",(e));

            return;
        }

        tagTable.put("css", cssParser.parse(myBusinessArea.getSystemId()));
        tagTable.put("path", PATH);
        tagTable.put("sysPrefix", myBusinessArea.getSystemPrefix());
        tagTable.put("nearestPath", WebUtil.getNearestPath(""));
        WebUtil.setInstanceBold(tagTable, myBusinessArea.getSystemPrefix());

        String hydPath = tagTable.get("instancePathHyd");
        String nycPath = tagTable.get("instancePathNyc");

        if (hydPath.startsWith("/search")) {

            // PATH ends with "/". So, remove the "/" in hydPath.
            tagTable.put("instancePathHyd", PATH + hydPath.substring(1));
        } else if (nycPath.startsWith("/search")) {

            // PATH ends with "/". So, remove the "/" in nycPath.
            tagTable.put("instancePathNyc", PATH + nycPath.substring(1));
        }

        StringBuilder content = new StringBuilder();

        content.append("One or more of the specified IUCs for ");

        if (myRequest.getMaxActionId() == 1) {
            content.append("Request ");
        } else {
            content.append("Update ");
        }

        content.append("<a class=\"l cb\" href=\"").append(PATH).append("/q/").append(myBusinessArea.getSystemPrefix()).append("/").append(myRequest.getRequestId()).append("#").append(
            myRequest.getMaxActionId()).append("\">").append(smartLink).append("</a> failed.");
        tagTable.put("introLine", content.toString());
        content = new StringBuilder();

        for (String str : myAllEUCs) {
            content.append("&nbsp;&nbsp;&nbsp;&nbsp;").append(str).append("<br>");
        }

        tagTable.put("allEucs", content.toString());
        content = new StringBuilder();

        if (myBadDescriptorEUCs.size() > 0) {
            content.append("<LI>").append("Unsupported descriptor:<br>");

            for (String str : myBadDescriptorEUCs) {
                content.append("&nbsp;&nbsp;&nbsp;&nbsp;").append(str).append("<br>");
            }
        }

        if (myNotAllowedEUCs.size() > 0) {
            content.append("<LI>").append("Unsupported IUC:<br>").append("&nbsp;&nbsp;- List of IUCs where either the ").append("descriptor or the value pattern is not supported.").append("<br>");

            for (String str : myNotAllowedEUCs) {
                content.append("&nbsp;&nbsp;&nbsp;&nbsp;").append(str).append("<br>");
            }
        }

        if (myNotAllowedPrivateEUCs.size() > 0) {
            content.append("<LI>").append("In private requests, ").append("IUCs for list type fields are not supported ").append("in email. So these IUCs failed:").append("<br>").append(
                "&nbsp;&nbsp;- List of IUCs on list type fields.").append("<br>");

            for (String str : myNotAllowedPrivateEUCs) {
                content.append("&nbsp;&nbsp;&nbsp;&nbsp;").append(str).append("<br>");
            }
        }

        if (myBadOperandPrivateEUCs.size() > 0) {
            content.append("<LI>").append("In private requests, ").append("only authorized users can receive mail. ").append("So these IUCs failed:").append("<br>").append(
                "&nbsp;&nbsp;- List of IUCs on user fields ").append("with unauthorized users specified.").append("<br>");

            for (String str : myBadOperandPrivateEUCs) {
                content.append("&nbsp;&nbsp;&nbsp;&nbsp;").append(str).append("<br>");
            }
        }

        if (myBadSyntaxEUCs.size() > 0) {
            content.append("<LI>").append("Unsupported argument syntax:<br>");

            for (String str : myBadSyntaxEUCs) {
                content.append("&nbsp;&nbsp;&nbsp;&nbsp;").append(str).append("<br>");
            }
        }

        if (myBadOperandEUCs.size() > 0) {
            content.append("<LI>").append("Unsupported argument:<br>");

            for (String str : myBadOperandEUCs) {
                content.append("&nbsp;&nbsp;&nbsp;&nbsp;").append(str).append("<br>");
            }
        }

        if (myNoPermissionEUCs.size() > 0) {
            content.append("<LI>").append("You are not authorized to use an IUC ").append("for this descriptor:<br>");

            for (String str : myNoPermissionEUCs) {
                content.append("&nbsp;&nbsp;&nbsp;&nbsp;").append(str).append("<br>");
            }
        }

        tagTable.put("failedEucs", content.toString());
        ActionHelper.replaceTags(hp, tagTable, null);
        Mail.sendWithHtml(myUser.getEmail(), failureLogAddress, failureLogAddress, subject, hp.parse(myBusinessArea.getSystemId()));

        return;
    }

    /**
     * Check if this is a request transferred to a different BA.
     * If request is under transfer, throw TBits Exception and
     * let the mail append stay in queue.
     * If the request is already transferred, fwd the mail to new
     * formed request and return;
     *
     *
     * @return true, if append to transferred one and mail is fwded,
     * else false if request not transferred
     * @exception DTBitsException
     */
    private boolean checkAppendToTransferredRequest() throws TBitsException {

        // Check if this is a request transferred to a different BA.
        TransferredRequest tr = TBitsHelper.isTransferred(myBusinessArea.getSystemPrefix(), myRequest.getRequestId());

        if (tr != null) {
            if (TBitsHelper.isRequestLocked(tr) == true) {

                //
                // The request is under transfer. So, discard this now and
                // Let this stay in the mail queue until the request is
                // completely transferred.
                //
                throw new TBitsException("The request is under transfer.");
            } else {

                //
                // Forward the message to the target BA with following
                // modifications.
                // - Change the subject to contain TargetBA#ReqId
                // - To address should be email of TargetBA.
                //
                MimeMessage message = myMailProcessor.getMimeMessage();

                LOG.info("Forwarding the message to the target BA.");
                forward(message, tr);

                return true;
            }
        }

        return false;
    }

    /**
     * This function checks if the request is transferred one and
     * set the srcPrefix and requestId.
     *
     * @return true if request is transferred, else return false.
     */
    private boolean checkIfTransferRequest() {
        String trReqId = myMailProcessor.getHeaderValue(TR_REQ_HEADER);

        if ((trReqId != null) && (trReqId.trim().equals("") == false)) {
            try {
                String[] arr = trReqId.split("#");

                myTransferSrcPrefix    = arr[0];
                myTransferSrcRequestId = Integer.parseInt(arr[1].trim());

                return true;
            } catch (Exception e) {
                LOG.severe("Error while parsing the value of " + "X-TBits-Transfer-REQ header: \n" + trReqId + "",(e));
            }
        }

        return false;
    }

    /*
     * Check if user is unauthorized to log/append.
     *
     */
    private boolean checkIfUnauthorizedUser(User aUser, BusinessArea aBusinessArea, Request aRequest, boolean aIsAppend, boolean aIsPrivate) throws DatabaseException {
        boolean unAuthorized = false;

        if ((aIsAppend == false) && (aBusinessArea.getIsPrivate() == true)) {
            ArrayList<BusinessArea> baList = BusinessArea.getBusinessAreasByUserId(aUser.getUserId());

            if (baList.contains(aBusinessArea) == false) {
                unAuthorized = true;
            }
        } else if ((aIsAppend == true) && (aIsPrivate == true) && (aRequest != null)) {
            Hashtable<String, Integer> permissions = RolePermission.getPermissionsBySystemIdAndRequestIdAndUserId(aRequest.getSystemId(), aRequest.getRequestId(), myUser.getUserId());

            if (permissions == null) {
                unAuthorized = true;
            } else {
                String  key  = Field.IS_PRIVATE;
                int     perm = 0;
                Integer temp = permissions.get(key);

                if (temp != null) {
                    perm = temp;
                } else {
                    perm = 0;
                }

                if ((perm & VIEW) == 0) {
                    unAuthorized = true;
                }
            }
        }

        return unAuthorized;
    }

    /**
     * This method checks if subject starts with the Prefix#Id pattern
     *  where prefix can be main prefix or any of the legacy prefix
     *
     * @param aSubject
     *
     * @return the Prefix#Id pattern matched, if any.
     */
    private String checkPatternInSubject(String aSubject) {
        if ((aSubject == null) || aSubject.trim().equals("")) {
            return "";
        }

        aSubject = aSubject.toUpperCase().trim();

        Pattern p = Pattern.compile("([a-zA-Z0-9_-]+)#([0-9]+)");
        Matcher m = p.matcher(aSubject);

        //
        // Check if any such prefix#Id pattern exist in subject
        //
        while (m.find() == true) {
            String pattern   = m.group();
            String sysPrefix = m.group(1);

            //
            // If pattern exist, subject should start with it.
            // "RE:" and "FW:" has already been trimmed, if existed.
            // return if pattern not at start.
            //
            if (!aSubject.startsWith(pattern)) {
                return "";
            }

            //
            // If pattern at start, check if SysPrefix belongs to the
            // Business area or its legacy prefix list
            //
            else {
                if (sysPrefix.equals(myBusinessArea.getSystemPrefix().toUpperCase())) {
                    return pattern;
                } else {
                    ArrayList<String> legacyPrefixList = mySysConfig.getLegacyPrefixList();

                    if (legacyPrefixList.size() == 0) {
                        return "";
                    }

                    for (String prefix : legacyPrefixList) {
                        if (sysPrefix.equals(prefix.toUpperCase().trim())) {
                            return pattern;
                        }
                    }
                }
            }
        }

        return "";
    }

    /**
     *  This method checks for severity rules, if any, when the
     * severity is not medium.
     *
     * @param aDactionMetaData
     *
     * @exception DatabaseException
     */
    private void checkSeverityRules(Hashtable<Field, String> aDactionMetaData) throws DatabaseException {
        String severity = myMailProcessor.getImportance();

        if ((severity == null) || severity.equals("") || severity.equalsIgnoreCase("medium") || severity.equalsIgnoreCase("normal")) {
            return;
        }

        Field field = Field.lookupBySystemIdAndFieldName(myBusinessArea.getSystemId(), Field.SEVERITY);

        if (severity.equalsIgnoreCase("low") == true) {
            String incomingLow = mySysConfig.getIncomingSeverityLow();

            if ((incomingLow != null) &&!incomingLow.trim().equals("")) {
                severity = incomingLow;
            }
        } else if (severity.equalsIgnoreCase("high") == true) {
            String incomingHigh = mySysConfig.getIncomingSeverityHigh();

            if ((incomingHigh != null) &&!incomingHigh.trim().equals("")) {
                severity = incomingHigh;
            }
        }

        aDactionMetaData.put(field, severity);

        return;
    }

    /**
     * This method parses the description to extract related request data
     * and return a sql batch string to insert the records in
     * related requests table of this instance.
     *
     * @param aContent  the description string
     *
     * @return the related request sql batch string or empty string if
     *         format is incorrect.
     *
     */
    private String formRelatedRequestBatch(String aContent) {
        if ((aContent == null) || aContent.trim().equals("")) {
            return "";
        }

        // Remove all new lines and spaces before and after the content.
        aContent = aContent.trim();

        StringBuilder sb    = new StringBuilder();
        Pattern       p1    = Pattern.compile("\r?\n");
        String[]      lines = p1.split(aContent);
        Pattern       p     = Pattern.compile("^( *)/([^/ :\r\n]+)( *)([ |:])( *)(.*)");

        //
        // Parse lines to match pattern.
        //
        for (int i = 0; i < lines.length; i++) {
            Matcher m = p.matcher(lines[i]);

            //
            // If line matches pattern
            //
            if (m.find() == true) {
                String desc = ((m.group(2) == null)
                               ? ""
                               : m.group(2)).trim();
                String op   = ((m.group(6) == null)
                               ? ""
                               : m.group(6)).trim();

                if (i == 0) {
                    if ((desc.toLowerCase().equals("delete") == true) && (op.equals("") == false)) {
                        int index = op.indexOf("#");

                        if (index == -1) {
                            return "";
                        }

                        String primaryPrefix    = op.substring(0, index);
                        String primaryRequestId = op.substring(index + 1);

                        sb.append("delete from related_requests where ").append("primary_sys_prefix = '").append(primaryPrefix).append("' and primary_request_id = ").append(primaryRequestId).append(
                            "\n");
                    } else {
                        return "";
                    }
                } else if (i > 0) {
                    if ((desc.toLowerCase().equals("insert") == true) && (op.equals("") == false)) {
                        sb.append("insert into related_requests values").append(op).append("\n");
                    } else {
                        return "";
                    }
                }
            }
        }

        return sb.toString();
    }

    /**
     * This method forms the Hashtable of <field-name, value> pair,
     * from the d-actions table .
     *
     * @param aDactionMetaData the d-action values
     * @param aContent the request description
     * @param aIsAppend boolean true, if mail is an append
     *
     * @return the Hashtable of <field-name, value> pair
     * @exception DatabaseException
     */
    private Hashtable<String, String> formRequestHash(Hashtable<Field, String> aDactionMetaData, String aContent, boolean aIsAppend) throws DatabaseException {
        Hashtable<String, String> requestHash = new Hashtable<String, String>();

        //
        // Populate request Hashtable from d-actions.
        // only if user is authorized, else ignore them
        //
        if (myIsUnauthorizedUser == false) {
            loadAuthUsers();
            IUCValidator.validateEUCs(requestHash, aDactionMetaData, aIsAppend, myRequest, myBusinessArea, myIsPrivate, myIsPrivateByBA, myFailedEUCs, myBadSyntaxEUCs, myBadOperandPrivateEUCs,
                                      myAuthUsersForRequest);
        }

        /*
         * If a category is passed on the command line, pass that value to the
         * API, becuase this should take the precedence over the one in
         * d-action if any.
         */
        if ((myCategory != null) && (myCategory.trim().equals("") == false)) {
            requestHash.put(Field.CATEGORY, myCategory);
        }

        if (aIsAppend == false) {

            //
            // For new request subject is taken as secified in mail subject
            //
            requestHash.put(Field.SUBJECT, myMailProcessor.getSubject());
        } else {

            //
            // For append, requestId need to be specified.
            //
            requestHash.put(Field.REQUEST, "" + myRequest.getRequestId());
        }

        //
        // These values are common to new request/ append. and these values
        // cannot be overRidden by d-actions.
        //
        requestHash.put(Field.BUSINESS_AREA, "" + myBusinessArea.getSystemId());
        requestHash.put(Field.USER, myUser.getUserLogin());
        
        String mailDescFieldName = PropertiesHandler.getProperty(EMAIL_DESCRIPTION_FIELD);
        if( null == mailDescFieldName )
        	mailDescFieldName = Field.DESCRIPTION;
        
        // limit the scope of mailDescField
        {
        	Field mailDescField = null;
	        try
	        {
	        	mailDescField = Field.lookupBySystemIdAndFieldName(myBusinessArea.getSystemId(), mailDescFieldName);
	        }
	        catch(Exception e)
	        {
	        	e.printStackTrace();
	        }
	        
	        if( null == mailDescField )
	        {
	        	mailDescFieldName = Field.DESCRIPTION;
	        }
        }
        
        requestHash.put(mailDescFieldName, aContent);
        
        requestHash.put(Field.MEMO, myMailProcessor.getCompleteMessage());
        requestHash.put(Field.REPLIED_TO_ACTION, "" + myRepliedToAction);

        // Key      : Field.DESCRIPTION + "_content_type"
        // Required : No
        // Default  : Empty String.
       	requestHash.put(Field.DESCRIPTION + "_content_type", myMailProcessor.getDescriptionContentType()+"");
       	
        String includeRecipientsStr = null;
        boolean includeRecipients = true;
        try
        {
        	includeRecipientsStr = PropertiesHandler.getProperty(PropertiesEnum.TRANSBIT_TBITS_INCLUDE_RECIPIENTS);
        	includeRecipients = Boolean.parseBoolean(includeRecipientsStr);
        }
        catch(Exception e)
        {
        	LOG.warn("Cant not load '" + PropertiesEnum.TRANSBIT_TBITS_INCLUDE_RECIPIENTS + "' key", e);
        }

        //
        // These may be specified as d-actions and will be appended in
        // that case. Business area mail Id should be removed from these list.
        //
        if(includeRecipients)
        {
        if (!myMailProcessor.getTo().equals("")) {
            String toAddress = myMailProcessor.getTo().replaceAll(myBusinessArea.getEmail(), "");
            ArrayList<BAMailAccount> bamas = BAMailAccount.lookupByBA(myBusinessArea.getSystemPrefix());
            for(BAMailAccount bam:bamas)
            {
            	toAddress = toAddress.replaceAll(bam.getBAEmailAddress(), "");
            }
            
//            if (aIsAppend == false) {
                ArrayList<String> toList    = Utilities.toArrayList(toAddress, ",;");
                ArrayList<String> baIdsList = new ArrayList<String>();

                for (String str : toList) {
                    if (APIUtil.isBAEmail(str) == true) {
                        baIdsList.add(str);
                    }
                }

                for (String str : baIdsList) {
                    toList.remove(str);
                }

                String emailToFieldName = PropertiesHandler.getProperty(TBitsPropEnum.EMAIL_TO_FIELD);
                if( null == emailToFieldName )
                	emailToFieldName = Field.ASSIGNEE;
                // scoping the emailToField // TODO this is to be tested again
                {
	                Field emailToField = null;
	                try
	                {
	                	emailToField = Field.lookupBySystemIdAndFieldName(myBusinessArea.getSystemId(), emailToFieldName);
	                }
	                catch(Exception e)
	                {
	                	e.printStackTrace();
	                }
	                
	                if(emailToField == null )
	                	emailToFieldName = Field.ASSIGNEE;
                }
                
//                if (toList.size() > 0) {
//                    String subDAction = requestHash.get(emailToFieldName);
//
//                    if ((subDAction != null) && (subDAction.equals("") == false)) {
//                        requestHash.put(emailToFieldName, subDAction + "," + Utilities.arrayListToString(toList));
//                    } else {
//                        requestHash.put(emailToFieldName, Utilities.arrayListToString(toList));
//                    }
//                }

                // if this is update request then don't change the assignee_ids
                if(!aIsAppend)
                	requestHash.put(emailToFieldName, Utilities.arrayListToString(toList));
                
                // no need to update the TO field. Its not used now.
//                if (baIdsList.size() > 0) {
//                    String toDAction = requestHash.get(Field.TO);
//
//                    if ((toDAction != null) && (toDAction.equals("") == false)) {
//                        requestHash.put(Field.TO, toDAction + "," + Utilities.arrayListToString(baIdsList));
//                    } else {
//                        requestHash.put(Field.TO, Utilities.arrayListToString(baIdsList));
//                    }
//                }
//            } 
//            else {
//                String toDAction = requestHash.get(Field.TO);
//
//                if ((toDAction != null) && (toDAction.equals("") == false)) {
//                    requestHash.put(Field.TO, toDAction + "," + toAddress);
//                } else {
//                    requestHash.put(Field.TO, toAddress);
//                }
//            }
        }
        
        String emailCCFieldName = PropertiesHandler.getProperty(EMAIL_CC_FIELD);
        if( null == emailCCFieldName )
        	emailCCFieldName = Field.SUBSCRIBER ;
        // scoping the emailCCField 
        {
	        Field emailCCField = null;
	        try
	        {
	        	emailCCField = Field.lookupBySystemIdAndFieldName(myBusinessArea.getSystemId(), emailCCFieldName);
	        }
	        catch(Exception e)
	        {
	        	e.printStackTrace();
	        }
	        
	        if( null == emailCCField )
	        	emailCCFieldName = Field.SUBSCRIBER;
        }
        
        if (!myMailProcessor.getCc().equals("")) {
        		String ccAddress = myMailProcessor.getCc().replaceAll(myBusinessArea.getEmail(), "");
//        		String ccDAction = requestHash.get(emailCCFieldName);
        		String oldSubs = null;
        		if( null != myRequest )
        			oldSubs = myRequest.get(emailCCFieldName);
        		
        		String shouldAppend = PropertiesHandler.getProperty(EMAIL_CC_APPEND_IN_REQUEST);
        		boolean appendCC = false;
        		if( null != shouldAppend )
        		{
/*
 * Parses the string argument as a boolean. The boolean returned represents the value true if the string argument is not null and is equal, ignoring case, to the string "true".
Example: Boolean.parseBoolean("True") returns true.
Example: Boolean.parseBoolean("yes") returns false.
 */
        			appendCC = Boolean.parseBoolean(shouldAppend);
        		}
        		
        		if ((oldSubs != null) && (oldSubs.equals("") == false) && (appendCC ==true) ) {
        			requestHash.put(emailCCFieldName, oldSubs + "," + ccAddress);
        		} else {
        			requestHash.put(emailCCFieldName, ccAddress);
        		}
        	}
        }

        String attachments = myMailProcessor.getAttachments();
        //Check with the previous attachments if its being replaced.
        if (myRequest != null){
        	String prevAttachments = AttachmentInfo.toJson(myRequest.getAttachments());
        	if ((prevAttachments != null) && (!prevAttachments.trim().equals(""))){
        		Collection<AttachmentInfo> curAIList = new ArrayList<AttachmentInfo>();
        		if((attachments != null) && (attachments.trim().length() > 0))
        		{
        			curAIList = AttachmentInfo.fromJson(attachments);
        		}
        		Collection<AttachmentInfo> oldAI = new ArrayList<AttachmentInfo>();
        		for(AttachmentInfo ai : AttachmentInfo.fromJson(prevAttachments)){
        			for(AttachmentInfo cAI : curAIList)
        				if(ai.name.equals(cAI.name))
        					cAI.requestFileId = ai.requestFileId;
        				else
        					oldAI.add(ai);     			
        		}
        		curAIList.addAll(oldAI);
        		attachments = AttachmentInfo.toJson(curAIList);        		
        	}       
        }
        
        String emailAttachmentFieldName = PropertiesHandler.getProperty(EMAIL_ATTACHMENT_FIELD);
        if( null == emailAttachmentFieldName )
        	emailAttachmentFieldName = Field.ATTACHMENTS;
        
        {
        	Field emailAttachmentField = null;
        	try
        	{
        		emailAttachmentField = Field.lookupBySystemIdAndFieldName(myBusinessArea.getSystemId(), emailAttachmentFieldName);
        	}
        	catch(Exception e)
        	{
        		e.printStackTrace();
        	}
        	
        	if( emailAttachmentField == null )
        		emailAttachmentFieldName = Field.ATTACHMENTS;
        }
        
        if (attachments.equals("") == false) {
            requestHash.put(emailAttachmentFieldName, attachments);
        }

        return requestHash;
    }

    /**
     * This method forwards the given message based on the information in the
     * specified transferred request.
     *
     * @param message MimeMessage to be forwarded.
     */
    private void forward(MimeMessage message, TransferredRequest tr) throws TBitsException {
        String sourcePrefix    = tr.getSourcePrefix();
        int    sourceRequestId = tr.getSourceRequestId();
        String targetPrefix    = tr.getTargetPrefix();
        int    targetRequestId = tr.getTargetRequestId();
        String sourceAddress   = SysPrefixes.getEmail(sourcePrefix);
        String targetAddress   = SysPrefixes.getEmail(targetPrefix);
        String key             = sourcePrefix + "#" + sourceRequestId;
        String replace         = targetPrefix + "#" + targetRequestId;

        try {
            String subject = message.getSubject();

            subject = subject.replaceAll(key, replace);
            LOG.info("Subject: " + subject);
            message.setSubject(subject);

            //
            // Pass this forward header for proper trunaction of reply message.
            // Since its not straight fwd to replace TBits tag and
            // subject with reply header in description here.
            //
            String headerKey   = TR_FWD_HEADER;
            String headerValue = sourcePrefix + "#" + sourceRequestId;

            message.setHeader(headerKey, headerValue);

            // Get the from address.
            String fromAddress = myUser.getEmail();
            String bccAddress  = targetAddress;

            // Replace the source email address with the target email address.
            Address[] toList = message.getRecipients(RecipientType.TO);

            toList = Mail.replace(toList, sourceAddress, targetAddress);

            Address[] ccList = message.getRecipients(RecipientType.CC);

            ccList = Mail.replace(ccList, sourceAddress, targetAddress);
            message.setRecipients(RecipientType.TO, toList);
            message.setRecipients(RecipientType.CC, ccList);
            Mail.postMessageToSMTPServer(Mail.getSession(), message, fromAddress, bccAddress);
        } catch (Exception e) {
            LOG.severe("Error while forwarding the message to " + replace);

            throw new TBitsException("Error while forwarding the message to " + replace);
        }
    }

    /**
     * This method inserts a record into transferred_requests table with the
     * given prefix#Id.
     *
     * @param sourceRequest Prefix#Id
     *
     */
    public void insertTransferTable(String sourceRequest) {
        try {
            String[]           sArr       = sourceRequest.split("#");
            String             sPrefix    = sArr[0];
            int                sRequestId = Integer.parseInt(sArr[1].trim());
            String             tPrefix    = "";
            int                tRequestId = -1;
            TransferredRequest tr         = new TransferredRequest(sPrefix, sRequestId, tPrefix, tRequestId);

            TransferredRequest.insert(tr);
        } catch (Exception e) {
            StringBuffer message = new StringBuffer();

            message.append("An error occurred while inserting into ").append("transferred_requests table, the following ").append("information: ").append("\nSource Request: ").append(
                sourceRequest).append("\n\n").append(TBitsLogger.getStackTrace(e));
            LOG.severe(message.toString());
        }

        return;
    }

    /*
     *
     */
    private void loadAuthUsers() throws DatabaseException {
        if (myRequest == null) {
            myAuthUsersForRequest = new ArrayList<String>();

            return;
        }

        if (myAuthUsersForRequest == null) {
            myAuthUsersForRequest = RolePermission.getAuthUsersBySystemIdAndRequestIdAndActionId(myRequest.getSystemId(), myRequest.getRequestId(), myRequest.getMaxActionId());
        }

        return;
    }

    /**
     * This method logs the D-action errors in DB and reports to Dev team.
     */
    public void logDactionErrors() {

        //
        // For unauthorized user, ignore all Eucs and log the same message
        //
        if (myIsUnauthorizedUser == true) {
            if (myAllEUCs.size() > 0) {
                StringBuilder DactionLog = new StringBuilder();

                if (myRequest.getMaxActionId() == 1) {
                    DactionLog.append("All IUCs are ignored since user is not").append(" authorized to log to the business area.\n");
                } else {
                    DactionLog.append("All IUCs are ignored since user is not").append(" authorized to append to the request.\n");
                }

                for (String str : myAllEUCs) {
                    DactionLog.append(str);
                }

                LOG.severe("Daction Log For: " + "\nUser         : " + myUser.getUserLogin() + "\nBusinessArea : " + myBusinessArea.getSystemPrefix() + "\nRequestId    : " + myRequest.getRequestId()
                           + "\nActionId     : " + myRequest.getMaxActionId() + "\n\n" + DactionLog.toString());
                DActionLog.insert(new DActionLog(myRequest.getSystemId(), myRequest.getRequestId(), myRequest.getMaxActionId(), DactionLog.toString()));
            }

            return;
        }

        ArrayList<TBitsException> severeExs = myAPIExceptions.getExceptionList().get(APIException.SEVERE);
        ArrayList<TBitsException> pErrorExs = myAPIExceptions.getExceptionList().get(APIException.PERROR);

        if (severeExs.size() > 0) {
            myFailedEUCs.add("\n\n-------Bad Operand Values: Severe Exceptions\n");
        }

        for (TBitsException ex : severeExs) {
            myFailedEUCs.add(ex.getDescription() + "\n");
            myBadOperandEUCs.add(ex.getDescription() + "\n");
        }

        if ((myIsUnauthorizedUser == false) && (pErrorExs.size() > 0)) {
            myFailedEUCs.add("\n\n------Insufficient permissions: PError Exceptions\n");

            for (TBitsException ex : pErrorExs) {
                myFailedEUCs.add(ex.getDescription() + "\n");
                myNoPermissionEUCs.add(ex.getDescription() + "\n");
            }
        }

        int size = myFailedEUCs.size();

        if ((myAllEUCs.size() > 0) && (myFailedEUCs.get(size - 1).equals("\n\n-----Failed D-actions: Bad Descriptors:\n") == false)) {
            StringBuilder DactionLog = new StringBuilder();

            for (String str : myFailedEUCs) {
                DactionLog.append(str);
            }

            LOG.severe("Daction Log For: " + "\nUser         : " + myUser.getUserLogin() + "\nBusinessArea : " + myBusinessArea.getSystemPrefix() + "\nRequestId    : " + myRequest.getRequestId()
                       + "\nActionId     : " + myRequest.getMaxActionId() + "\n\n" + DactionLog.toString());
            DActionLog.insert(new DActionLog(myRequest.getSystemId(), myRequest.getRequestId(), myRequest.getMaxActionId(), DactionLog.toString()));
            SendEUCFailureUserNotification();
        }

        return;
    }

    /**
     * This method reads the message Standard input, process it and
     * logs request to the specified Ba and category, if mentioned.
     *
     *
     * @param aInputStream InputStream that contains the message.
     * @param aBaName the business area name
     * @param aCategory the category name within the BA
     * @throws Throwable 
     * @exception DTBitsException
     */
    public void logRequest(Message aMessage, String aBaName, String aCategory)
            throws Throwable {
    	boolean isAppend = false;
        // Setting email invocation
        System.setProperty(TBitsConstants.PROP_BA_NAME, aBaName);

        Hashtable<Field, String> actionMetaData = new Hashtable<Field, String>();

        // Put the category value if any into the datamember.
        if ((aCategory != null) && (aCategory.trim().equals("") == false)) {
            myCategory            = aCategory;
            myIsCategorySpecified = true;
        }

        LOG.info("Category: " + myCategory);

        //
        // Look up in Mapper by BaName. Throws illegalArgumentException
        // if Ba doesn't exist.
        //
        myBusinessArea = BusinessArea.lookupBySystemPrefix(aBaName); //validateBaName(aBaName);
        mySysConfig    = myBusinessArea.getSysConfigObject();

        //
        // Process mail message. Set repairLinks property to true to remove
        // soft line breaks from lines in case of text/rtf mails
        //
        myMailProcessor = new MailProcessor(APIUtil.getTMPDir(), null);

        String[] linksList = { "http:", "file:", "ftp:", "outlook:/" };
        
        try
        {
        //
        // setting repairLinks to false, so as to not
        // do any wrapping processing on plain-text mail content.
        //
        myMailProcessor.setRepairLinks(false);
        myMailProcessor.setLinksList(linksList);
        myMailProcessor.processMail(aMessage);

        //
        // if this an acknowledgement for a transferred request.
        // process it and return;
        //
        boolean ack = processIfTransferAcknowledgement();

        if (ack == true) {
            return;
        }

        //
        // else if this is to update related_requests table in cross-site.
        // process it and return;
        // Throws illegalArgumentException if data format is incorrect.
        //
        boolean rel = processIfRelatedRequestUpdate();

        if (rel == true) {
            return;
        }

        //
        // validate logger/appender using replyto/from address.
        // Throws illegalArgumentException if invalid user.
        //
        myUser = validateUser();

        //
        // if this is a transfer request set myIsTransferReq variable
        // and also set sourcePrefix and srcRequestId in checkIsTransferred()
        // This is used to send acknowledgement back once the request is
        // successfully transferred
        //
        myIsTransferReq = checkIfTransferRequest();

        //
        // Check if this a fwded request, from a transferred request.
        // This is used to truncated the reply header.
        //
        myFwdHeaderValue = myMailProcessor.getHeaderValue(TR_FWD_HEADER);
        myFwdHeaderValue = ((myFwdHeaderValue == null)
                            ? ""
                            : myFwdHeaderValue.trim());

        //
        // If request is transferred it will be treated as new request else
        // Check if new request or append by parsing Subject
        // for SysPrefix and RequestId.
        // if append, set myRequest object.
        //
        isAppend = ((myIsTransferReq == true)
                            ? false
                            : parseSubject());

        //
        // Check if request is private by virtue of type.
        // This is used to validate D-actions for private request.
        //
        if (isAppend == true) {
            if ((myRequest.getIsPrivate() == true) || (myRequest.getCategoryId().getIsPrivate() == true) || (myRequest.getSeverityId().getIsPrivate() == true)
                    || (myRequest.getStatusId().getIsPrivate() == true) || (myRequest.getSeverityId().getIsPrivate() == true)) {
                myIsPrivate = true;
            } else if (myBusinessArea.getIsPrivate() == true) {
                myIsPrivate     = true;
                myIsPrivateByBA = true;
            }
        }
        
        //
        // Check if user is Unauthorized to log/append.
        // If yes, ignore all eucs and just accept description.
        // All this Info is passed to api, to be added in header_description.
        //
        myIsUnauthorizedUser = checkIfUnauthorizedUser(myUser, myBusinessArea, myRequest, isAppend, myIsPrivate);

        //
        // Register log parameters
        //
        String userLogin = myUser.getUserLogin();
        String sysPrefix = myBusinessArea.getSystemPrefix();
        String requestId = ((myRequest == null)
                            ? null
                            : Integer.toString(myRequest.getRequestId()));

        Utilities.registerMDCParams(userLogin, sysPrefix, requestId);

        //
        // Check if this is a request transferred to a different BA.
        // If request is under transfer, throw TBits Exception
        // and let the mail append stay in queue.
        // If the request is already transferred, fwd the mail to new
        // formed request and return;
        //
        if (isAppend == true) {
            boolean fwd = checkAppendToTransferredRequest();

            if (fwd == true) {
                return;
            }
        }

        //
        // Check severity rules, if applicable
        //
        checkSeverityRules(actionMetaData);

        //
        // Process description
        // 1) remove replied headers in case of append.
        // 2) to get action metatdata hashtable,
        // 3) replace request links by smart link text
        // 4) remove <<embedded link>> by outlook
        // 5) remove <<ole embedded>> text
        //
        String description = parseDescription(actionMetaData, isAppend);

        //
        // Form the request hashtable based on if its a new request and
        // any meta data parsed from description
        //
        Hashtable<String, String> requestHash = formRequestHash(actionMetaData, description, isAppend);


        // setting the logger
        String emailLoggerFieldName = PropertiesHandler.getProperty(EMAIL_LOGGER_FIELD);
        if( null == emailLoggerFieldName )
        	emailLoggerFieldName = Field.LOGGER;
        
        {
        	Field loggerField = null;
        	try
        	{
        		loggerField = Field.lookupBySystemIdAndFieldName(myBusinessArea.getSystemId(), emailLoggerFieldName);
        	}
        	catch(Exception e)
        	{
        		e.printStackTrace();
        	}
        	
        	if( null == loggerField )
        		emailLoggerFieldName = Field.LOGGER;
        }
        
        String loggers = myUser.getUserLogin();
        // if this is update request then don't change the logger_ids.
        if( !isAppend )
        	requestHash.put(emailLoggerFieldName, loggers);
        
        	// fire the event that will enable the changes in the submitting request
        	BeforeEmailSubmitEvent bese = new BeforeEmailSubmitEvent(requestHash,aMessage,this);
        	EventManager.getInstance().fireEvent(bese);
        	
	        //
	        // Call API to insert/update request
	        //
	        if (!isAppend) {
	            AddRequest addAPI = new AddRequest(SEVERE);
	            addAPI.setSource(TBitsConstants.SOURCE_EMAIL);
	            addAPI.setIsEmailCategory(myIsCategorySpecified);
	            addAPI.setIsUnauthorized(myIsUnauthorizedUser);
	            addAPI.setIsTransferRequest(myIsTransferReq);
	            requestHash.put(TR_SRC_REQUEST, myTransferSrcPrefix + "#" + myTransferSrcRequestId);
	            myRequest       = addAPI.addRequest(requestHash);
	            myAPIExceptions = addAPI.getExceptions();
	        } else {
            UpdateRequest updateAPI = new UpdateRequest(SEVERE);
	            updateAPI.setSource(TBitsConstants.SOURCE_EMAIL);
	            updateAPI.setIsUnauthorized(myIsUnauthorizedUser);
	            myRequest       = updateAPI.updateRequest(requestHash);
	            myAPIExceptions = updateAPI.getExceptions();
	        }
        }
        catch(Throwable apie)
        {	
//        	myUser = validateUser();
        	sendAPIException(apie, isAppend);
        	throw apie;
        }
        //
        // LOG D-action errors in DB and also report to Dev team.
        //
        logDactionErrors();

        if (myIsTransferReq == true) {
            String tarPrefix    = myBusinessArea.getSystemPrefix();
            int    tarRequestId = myRequest.getRequestId();

            sendTransferAck(myTransferSrcPrefix, myTransferSrcRequestId, tarPrefix, tarRequestId);
        }

        return;
    }

    private void sendAPIException(Throwable apie, boolean isAppend)
    {
    	 try {
    	CaptionsProps captions = CaptionsProps.getInstance();
    	String request = captions.getCaptionsHashMap(myBusinessArea.getSystemId()).get(CaptionsProps.CAPTIONS_ALL_CAMEL_CASE_REQUEST);
    	//Send mail to notify the user.
    	String subject           = "Error while adding a " + request;
    	if(isAppend)
    		subject = "Error while appending a " + request;
    	String failureLogAddress = PropertiesHandler.getProperty(KEY_LOGGER_NOTIFY_FROM); 
    	String replyToAddress = myMailProcessor.getFrom();
    	
    	 
			if ((replyToAddress.equals("") == false) && (APIUtil.isBAEmail(replyToAddress) == true)) {
//              setReturnStatus(2);
//              setErrorLog("Request not logged. BA mail id in reply To\nFrom: " + fromAddress + "\nReplyTo: " + replyToAddress);

//              throw new IllegalArgumentException("Request not logged. BA mail id in reply To\nFrom: " + fromAddress + "\nReplyTo: " + replyToAddress);
				LOG.error("The email id in the from address is a ba-mail id: " + replyToAddress + " So not sending the error message.");
				return;
			  }
			else if((failureLogAddress.equals("") == false) && (APIUtil.isBAEmail(failureLogAddress) == true)){
				LOG.error("The email id in the failureLogAddress is a ba-mail id: " + failureLogAddress + " So not sending the error message.");
				return;
			}
			  else
			  {
				  Mail.sendBouncedMailAsAttachment(replyToAddress, failureLogAddress, subject, apie.getMessage(), myMailProcessor.getMimeMessage());
			  }
		} catch (Throwable e) {
			LOG.error("error occured while sending the email error message to client.", e);
			return;
		}
    }
    
    /*
     *  Update the mailq and also moves the failed messages to home
     */
    private static int updateMailQ(String strCurrentMsgId, MimeMessage message)
    	throws FileNotFoundException, IOException
    {
            File fileMailQ = Configuration.findPath(MAILQ_FILE);

            if (fileMailQ == null) {
//            	throw new FileNotFoundException(MAILQ_FILE + " is not found.");
            	return 0;
            }

            BufferedReader br                           = new BufferedReader(new FileReader(fileMailQ));
            String         line                         = "";
            String         strFinalListOfMsgIdsStatuses = "";
            boolean        bMsgIdExists                 = false;
            boolean        bMsgIdToBeRemoved            = false;

            /*
             * Each line in the LOGMSGSTATUS_FILE is in the following format.
             *
             *  MSG_ID,STATUS.
             */
            while ((line = br.readLine()) != null) {
                String record = line.trim();

                if (line.startsWith("#")) {
                    continue;
                }

                String[] arr = record.split(",");

                if (arr == null) {
                    continue;
                }
                if(arr.length == 2)
                {
					String msgid = arr[0].trim();
					String status = arr[1].trim();

					if (strCurrentMsgId != null) {
						if (strCurrentMsgId.equals(msgid)) {
							bMsgIdExists = true;

							if (status.equals(REMOVE_STATUS)) {
								bMsgIdToBeRemoved = true;
								continue;
							}
						}
					}
				}
                if (strFinalListOfMsgIdsStatuses.equals("")) {
                    strFinalListOfMsgIdsStatuses += record;
                } else {
                    strFinalListOfMsgIdsStatuses += "\n" + record;
                }
            }
            br.close();

            if (!bMsgIdExists) {
                BufferedWriter bWMailQ = new BufferedWriter(new FileWriter(fileMailQ, true));

                bWMailQ.write("\n" + strCurrentMsgId + "," + FAIL_STATUS);
                bWMailQ.close();
            } else {
                if (bMsgIdToBeRemoved) {
                    BufferedWriter bWMailQ = new BufferedWriter(new FileWriter(fileMailQ));

                    bWMailQ.write(strFinalListOfMsgIdsStatuses);
                    bWMailQ.close();

                    FileOutputStream fos = null;
					try {
						String outputFile = PropertiesHandler.getProperty("tbits.home") + "/tmp/" + message.getMessageID() + ".txt";
						fos = new FileOutputStream(outputFile);
						System.out.println("Writing message to: " + outputFile);
					    message.writeTo(fos);
					} catch (IllegalArgumentException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (MessagingException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} finally {
						if(fos != null)
							fos.close();
					}
					return 0;
                }
            }
            return 0;
    }
    
    /**
     * Main method. This is the entry point and initiates the
     * request processing.
     *
     * @param args BA Name, Category to be passed as command Line argument
     */
    public static int main(String[] args) {
        LogMailRequest lmr = new LogMailRequest();

        //
        // Return Status used:
        //
        // # return status = 1 for invalid BA
        // # return status = 2 for invalid user
        // # return status = -1 for any other failure.
        // # return status = 0 on sucessful logging of request.
        //
        long start = System.currentTimeMillis();

        try {
            if ((args.length < 1) || (args[0] == null) || (args[0].equals(""))) {
                lmr.setReturnStatus(-1);
                lmr.setErrorLog("BA name parameter missing. Message not processed." + "Check details in mailq");
                lmr.reportFailure();
                return lmr.getReturnStatus();
                //return lmr.getReturnStatus();
            } else if (args.length == 1) {

                //
                // Read from the standard input with BA name as param
                //
            	MimeMessage mm = new MimeMessage(Session.getDefaultInstance(new Properties()), System.in);
                lmr.logRequest(mm, args[0], "");
            } else if (args.length == 2) {

                //
                // Read from the standard input with BA name
                // and category as param
                //
            	MimeMessage mm = new MimeMessage(Session.getDefaultInstance(new Properties()), System.in);
                lmr.logRequest(mm, args[0], args[1]);
            }
        } catch (Exception e) {
            if (lmr.getErrorLog().equals("")) {
                lmr.setErrorLog(TBitsLogger.getStackTrace(e));
                lmr.setReturnStatus(-1);
            }

            lmr.reportFailure();

            // Code inserted to move failed msgs to tbits-Home and
            // delete from the queue
            String strCurrentMsgId = null;

            try {
                strCurrentMsgId = lmr.myMailProcessor.getMimeMessage().getMessageID();
            } catch (Exception ex) {

                ex.printStackTrace();
            }
            try {
				return updateMailQ(strCurrentMsgId, lmr.myMailProcessor.getMimeMessage());
			} catch (FileNotFoundException fnfe) {
				LOG.severe("",(fnfe));
			} catch (IOException ioe) {
				LOG.severe("",(ioe));
			}
            // end of Code inserted to move failed msgs to tbits-Home and
            // delete from the queue
            return lmr.getReturnStatus();
            //return lmr.getReturnStatus();
        } catch (Throwable e) {
            if (lmr.getErrorLog().equals("")) {
                lmr.setErrorLog(TBitsLogger.getStackTrace(e));
                lmr.setReturnStatus(-1);
            }

            lmr.reportFailure();

            // Code inserted to move failed msgs to tbits-Home and
            // delete from the queue
            String strCurrentMsgId = null;

            try {
                strCurrentMsgId = lmr.myMailProcessor.getMimeMessage().getMessageID();
            } catch (Exception ex) {

                ex.printStackTrace();
            }

            try {
                return updateMailQ(strCurrentMsgId, lmr.myMailProcessor.getMimeMessage());
            } catch (FileNotFoundException fnfe) {
                LOG.severe("",(fnfe));
            } catch (IOException ioe) {
                LOG.severe("",(ioe));
            }

            // end of Code inserted to move failed msgs to tbits-Home and
            // delete from the queue
            return lmr.getReturnStatus();
            //return lmr.getReturnStatus();
        }

        long end = System.currentTimeMillis();

        LOG.info("Total Time taken to log request: " + (end - start) + " mecs");
        return 0;
    }

    public static int  logMessage(String ba , Message aMessage) throws OutOfMemoryError {
    	return logMessage(ba, aMessage, "");
    }
    /**
     * logMessage method. This is the entry point and initiates the
     * request processing.
     * TODO: Shitty code. Code duplication. Need to be revised. 
     * @param args BA Name, Category to be passed as command Line argument
     */
    public static int  logMessage(String ba , Message aMessage, String category) throws OutOfMemoryError {
        LogMailRequest lmr = new LogMailRequest();
        boolean isOutofMemory = false;
        //
        // Return Status used:
        //
        // # return status = 1 for invalid BA
        // # return status = 2 for invalid user
        // # return status = -1 for any other failure.
        // # return status = 0 on sucessful logging of request.
        //
        long start = System.currentTimeMillis();

        try {
                lmr.logRequest(aMessage, ba, category);
        } catch (Exception e) {
            if (lmr.getErrorLog().equals("")) {
                lmr.setErrorLog(TBitsLogger.getStackTrace(e));
                lmr.setReturnStatus(-1);
            }

            lmr.reportFailure();

            // Code inserted to move failed msgs to tbits-Home and
            // delete from the queue
            String strCurrentMsgId = null;

            try {
                strCurrentMsgId = lmr.myMailProcessor.getMimeMessage().getMessageID();
            } catch (Exception ex) {
                LOG.warn("",(ex));
            }
            //update the mailq
            try {
            	updateMailQ(strCurrentMsgId, lmr.myMailProcessor.getMimeMessage());
            } catch (FileNotFoundException fnfe) {
            	System.out.println("\n\nFile not found");
                LOG.severe("",(fnfe));
            } catch (IOException ioe) {
            	System.out.println("\n\nIO Exception not found");
                LOG.severe("",(ioe));
            }

            // end of Code inserted to move failed msgs to tbits-Home and
            // delete from the queue
            return lmr.getReturnStatus();
        } 
        catch(OutOfMemoryError t)
        {
        	isOutofMemory = true;
        }
        	
        catch (Throwable e) {
        	if (lmr.getErrorLog().equals("")) {
        		lmr.setErrorLog(TBitsLogger.getStackTrace(e));
        		lmr.setReturnStatus(-1);
        	}

        	
        	lmr.reportFailure();

        	// Code inserted to move failed msgs to tbits-Home and
        	// delete from the queue
        	String strCurrentMsgId = null;

        	try {
        		strCurrentMsgId = lmr.myMailProcessor.getMimeMessage().getMessageID();
        	} catch (Exception ex) {

        		ex.printStackTrace();
        	}

        	try {
        		updateMailQ(strCurrentMsgId, lmr.myMailProcessor.getMimeMessage());
        	} catch (FileNotFoundException fnfe) {
        		LOG.severe("",(fnfe));
        	} catch (IOException ioe) {
        		LOG.severe("",(ioe));
        	}

        	// end of Code inserted to move failed msgs to tbits-Home and
        	// delete from the queue
        	return lmr.getReturnStatus();
        }

        if(isOutofMemory) 
        {
        	throw new OutOfMemoryError(); 
        }
        long end = System.currentTimeMillis();

        LOG.info("Total Time taken to log request: " + (end - start) + " mecs");
        return 0;
    }

    
    /**
     *  This method parse description.
     *  1) remove replied headers in case of append.
     *  2) to get action metatdata hashtable,
     *  3) replace request links by smart link text
     *  4) remove <<embedded link>> by outlook
     *  5) remove <<ole embedded>> text
     *
     * @param aDactionMetaData table which will have parsed d-actions
     * @param aIsAppend boolean true, if mail is an append
     *
     * @return the parsed and cleaned description string
     *
     * @exception DatabaseException
     */
    private String parseDescription(Hashtable<Field, String> aDactionMetaData, boolean aIsAppend) throws DatabaseException {
        String description = myMailProcessor.getDescription();

        //
        // remove reply header, if its an append.
        //
        if (aIsAppend == true) {
            description = removeReplyHeader(description);
        }

        //
        // Load Description for reference in case any d-action fails
        //
        myFailedEUCs.add("-----Description before parsing D-actions:\n\n");
        myFailedEUCs.add(description);
        myFailedEUCs.add("\n\n-----Failed D-actions: Bad Descriptors:\n");

        //
        // Remove <Http://> embedded with an Href by outlook
        // only if its actullay followed by the href
        // with display text as the href itself
        // ex: http://xyz.com <http://xyz.com> : is removed
        // and Click <http://xyz.com> : not removed
        //
        description = removeEmbeddedLinks(description);

        //
        // Parse description for d-action tags, if not a transferred request.
        // No meta-data change is allowed while transferring request.
        //
        description = IUCValidator.parseEUCMetaData(description, aDactionMetaData, aIsAppend, myIsPrivate, myIsPrivateByBA, myBusinessArea, myFailedEUCs, myAllEUCs, myBadDescriptorEUCs,
                myNotAllowedEUCs, myNotAllowedPrivateEUCs);

        //
        // Remove <<OLE_Obj>> if any Ole object embedded
        //
        description = description.replaceAll("<<OLE_Obj>>", "");

        return description;
    }

    /**
     * This method parse the subject line for SysPrefix and RequestId to
     * check if its an append.
     * If append its set the myRequestId
     *
     * @exception DatabaseException
     */
    private boolean parseSubject() throws DatabaseException {
        boolean isAppend = true;
        String  subject  = myMailProcessor.getSubject().toUpperCase().trim();

        subject = ((subject.startsWith("RE:") || subject.startsWith("FW:"))
                   ? subject.substring(3).trim()
                   : subject);

        //
        // Check if subject starts with Prefix#Id pattern,
        // where prefix can be main prefix or any of the legacy prefix
        //
        String prefixId = checkPatternInSubject(subject);

        if (prefixId.equals("")) {
            isAppend = false;

            return isAppend;
        } else {
            int startIndex = prefixId.indexOf("#");
            int requestId  = 0;

            try {
                requestId = Integer.parseInt(prefixId.substring(startIndex + 1));
            } catch (NumberFormatException e) {

                // Log as a new request
                isAppend = false;

                return isAppend;
            }

            myRequest = Request.lookupBySystemIdAndRequestId(myBusinessArea.getSystemId(), requestId);

            if (myRequest == null) {
                isAppend = false;

                return isAppend;
            }
        }

        return isAppend;
    }

    /**
     *  This function checks and process the related request info
     *  and sync it in db.
     *
     *  @return true, if its Related Request insert mail, else return false
     *  @exception IllegalArgumentException
     */
    private boolean processIfRelatedRequestUpdate() {
        String strRelRequestHeader = myMailProcessor.getHeaderValue(INSERT_RELATED_HEADER);

        if ((strRelRequestHeader != null) && (strRelRequestHeader.trim().equals("insert") == true)) {
            LOG.info("Received Related Request Header :");

            String data = myMailProcessor.getDescription();

            try {
                String batch = formRelatedRequestBatch(data);

                LOG.info(" Related Request Sql Batch Formed : " + batch);

                if (batch.equals("") == true) {
                    setReturnStatus(-1);
                    setErrorLog("Error handling Insert Related Request Mail");

                    throw new IllegalArgumentException("Error handling Insert Related Request Mail");
                }

                Request.insertRelatedRequests(batch);
            } catch (Exception e) {
                setReturnStatus(-1);
                setErrorLog("Error handling Insert Related Request Mail\n" + TBitsLogger.getStackTrace(e));

                throw new IllegalArgumentException("Error handling Insert Related Request Mail\n" + "",(e));
            }

            return true;
        } else if ((strRelRequestHeader != null) && (strRelRequestHeader.trim().equals("insert") == false)) {
            setReturnStatus(-1);
            setErrorLog("Wrong X-TBits-Insert-RELATED Header value passed " + "for Insert Related Request Mail");

            throw new IllegalArgumentException("Wrong X-TBits-Insert-RELATED Header value passed " + "for Insert Related Request Mail");
        }

        return false;
    }

    /**
     *  This function checks and process the transfer request
     * acknowlewdgement mail
     *
     *  @return true, if its Transfered Request acknowledgement,
     *          else return false
     */
    private boolean processIfTransferAcknowledgement() {
        String strTransferAck = myMailProcessor.getHeaderValue(TR_ACK_HEADER);

        if ((strTransferAck != null) &&!strTransferAck.trim().equals("")) {
            LOG.info("Transfer Ack Value: " + strTransferAck);

            // Format of the value is [SourceRequest,TargetRequest]
            String[] arr = strTransferAck.split(",");

            if ((arr != null) && (arr.length == 2)) {
                String srcRequest = arr[0];    // First one is source request.
                String desRequest = arr[1];    // Second one is target request.

                updateRequestTable(srcRequest, desRequest);
                updateTransferTable(srcRequest, desRequest);

                try {
                    String[] arr1         = srcRequest.split("#");
                    int      srcRequestId = Integer.parseInt(arr1[1].trim());
                    Request  request      = Request.lookupBySystemIdAndRequestId(myBusinessArea.getSystemId(), srcRequestId);

                    sendMail(myBusinessArea.getSystemPrefix(), request);
                } catch (Exception e) {
                    LOG.severe("Error while parsing the value of " + "X-TBits-Transfer-ACK header: \n" + strTransferAck + "",(e));
                }
            }

            return true;
        }

        return false;
    }

    /**
     * This function removes <Http://> etc  embedded with an Href by outlook
     * only if its actullay followed by the href with display text
     * as the href itself
     * ex: http://xyz.com <http://xyz.com> : is removed
     * and Click <http://xyz.com> : not removed
     *
     * @param aContent the content to be cleaned
     *
     * @return aContent after embedded links, if any
     */
    public static String removeEmbeddedLinks(String aContent) {
        Pattern p = Pattern.compile("\\s*<\\s*(http:|file:|ftp:|outlook:)([^<>\r\n]+)>", Pattern.CASE_INSENSITIVE);
        Matcher m = p.matcher(aContent);

        while (m.find() == true) {
            try {
                String embeddedLink = m.group();
                String linkText     = m.group(1) + m.group(2);
                String linkText1    = m.group(2).toLowerCase().replaceAll("\\\\", "").replaceAll("/", "");

                linkText = LinkFormatter.replaceHrefWithSmartLinks(linkText).toLowerCase();
                linkText = linkText.replaceAll("\\\\", "").replaceAll("/", "");

                String tempContent = LinkFormatter.replaceHrefWithSmartLinks(aContent).toLowerCase();

                tempContent = tempContent.replaceAll("\\\\", "").replaceAll("/", "");

                int length      = linkText.length();
                int firstIndex  = tempContent.indexOf(linkText);
                int secondIndex = tempContent.indexOf(linkText, firstIndex + length);

                if ((secondIndex != -1) && ((secondIndex - firstIndex) <= length + 4)) {

                    //
                    // if the embedded link has spaces retain <> part
                    // for proper linking on rendering
                    //
                    String temp   = embeddedLink.trim();
                    int    index1 = aContent.indexOf(temp);
                    String temp1  = aContent.substring(0, index1);
                    int    index2 = temp1.indexOf(temp);

                    if (index2 == -1) {
                        temp = temp.substring(1, temp.length() - 1).trim();

                        if (temp.indexOf(" ") != -1) {
                            embeddedLink = temp;
                        }
                    }

                    int index = aContent.indexOf(embeddedLink);

                    aContent = aContent.substring(0, index) + ((aContent.length() > index + embeddedLink.length())
                            ? aContent.substring(index + embeddedLink.length())
                            : "");

                    continue;
                } else {
                    length      = linkText1.length();
                    firstIndex  = tempContent.indexOf(linkText1);
                    secondIndex = tempContent.indexOf(linkText, firstIndex + length);

                    if ((secondIndex != -1) && ((secondIndex - firstIndex) <= length + 4)) {

                        //
                        // if the embedded link has spaces retain <> part
                        // for proper linking on rendering
                        //
                        String temp   = embeddedLink.trim();
                        int    index1 = aContent.indexOf(temp);
                        String temp1  = aContent.substring(0, index1);
                        int    index2 = temp1.indexOf(temp);

                        if (index2 == -1) {
                            temp = temp.substring(1, temp.length() - 1).trim();

                            if (temp.indexOf(" ") != -1) {
                                embeddedLink = temp;
                            }
                        }

                        int index = aContent.indexOf(embeddedLink);

                        aContent = aContent.substring(0, index) + ((aContent.length() > index + embeddedLink.length())
                                ? aContent.substring(index + embeddedLink.length())
                                : "");
                    }
                }
            } catch (RuntimeException e) {
                LOG.debug("",(e));

                continue;
            } catch (Exception e) {
                LOG.debug("",(e));

                continue;
            }
        }

        return aContent;
    }

    /**
     *  This method removes the message in the reply mail after the
     * -----Original Message----- or other reply Formats by Outlook 2003
     * When the user replies to the message
     * the message after this is the one that belongs to the earlier mail.
     *
     * @param aContent the mail description
     *
     * @return the mail content after removing replied text if any
     */
    private String removeReplyHeader(String aContent) {
        Pattern p1 = Pattern.compile("____TBits__RE__" + "([a-zA-Z0-9_-]+)#([0-9]+)#([0-9]+)____");
        Matcher m1 = p1.matcher(aContent);

        if (m1.find() == true) {
            if (myFwdHeaderValue.equals("") == true) {
                String prefix    = m1.group(1);
                int    requestId = Integer.parseInt(m1.group(2));
                int    actionId  = Integer.parseInt(m1.group(3));

                if (requestId != myRequest.getRequestId()) {
                    return aContent;
                }

                if (checkPatternInSubject(prefix + "#" + requestId).equals("") == false) {
                    myRepliedToAction = actionId;
                    aContent          = aContent.substring(0, aContent.indexOf(m1.group()));
                }
            } else {
                if (checkPatternInSubject(myFwdHeaderValue).equals("") == false) {
                    aContent = aContent.substring(0, aContent.indexOf(m1.group()));
                }
            }
        }

        String            prefixes         = myBusinessArea.getSystemPrefix().trim();
        ArrayList<String> legacyPrefixList = mySysConfig.getLegacyPrefixList();

        for (String prefix : legacyPrefixList) {
            prefixes = prefixes + "|" + prefix.trim();
        }

        String reqId = Integer.toString(myRequest.getRequestId());

        if (myFwdHeaderValue.equals("") == false) {
            try {
                String[] arr       = myFwdHeaderValue.split("#");
                String   prefix    = arr[0];
                String   requestId = arr[1].trim();

                prefixes = prefixes + "|" + prefix.trim();
                reqId    = reqId + "|" + requestId.trim();
            } catch (Exception e) {
                LOG.severe("Error while parsing the value of " + "X-TBits-Transfer-FWD header: \n" + myFwdHeaderValue + "",(e));
            }
        }

        Pattern p = Pattern.compile("(([-]*--Original Message--|" + "_____|" + "----------)[-_]*[\\s]*)?\r?\n\\s*From\\s*:\\s*" + "[^\r\n]+\r?\n(\\s*[^:\r\n)]*:.*\r?\n)*\\s*" + "Subject[\\s:]+("
                                    + prefixes + ")[\\s]*#(" + reqId + ")", Pattern.CASE_INSENSITIVE);
        Matcher m = p.matcher(aContent);

        if (m.find() == true) {
            aContent = aContent.substring(0, aContent.indexOf(m.group()));

            return aContent;
        }

        String requestDisplayName = "Request";

        try {
            Field f = Field.lookupBySystemIdAndFieldName(myBusinessArea.getSystemId(), Field.REQUEST);

            if (f != null) {
                requestDisplayName = f.getDisplayName();
            }
        } catch (Exception e) {
            LOG.debug("",(e));
        }

        p = Pattern.compile("(\r?\n[^\r\n]*)(<*.+@.+\\.[a-z]+>*)" + "\\s+wrote:\\s*\r?\n(\r?\n)*" + "(\\s*>*\\s*" + requestDisplayName + "\\s*(" + prefixes + ")\\s*#(" + reqId + ")\\s*\r?\n)*",
                            Pattern.CASE_INSENSITIVE);
        m = p.matcher(aContent);

        if (m.find() == true) {
            aContent = aContent.substring(0, aContent.indexOf(m.group()));

            return aContent;
        }
        
        //Remove lotus stuff
        //\n\n\n\n\n\t[^\n]+\n[^\n]+\n\n\t\nPlease respond to\n
        p = Pattern.compile("\\n\\n\\n\\n\\n\\t[^\\n]+\\n[^\\n]+\\n\\n\\t\\nPlease respond to\\n");
		m = p.matcher(aContent);
		
		if (m.find() == true) {
			//System.out.println("Found the lotus pattern");
			aContent = aContent.substring(0, aContent.indexOf(m.group()));
			return aContent;
		}
//		else
//			System.out.println("Not found the lotus pattern.");
        return aContent;
    }

    /**
     * This method reports the exception for not processing the request.
     */
    public void reportFailure() {
        boolean includeObjectDump = true;

        if (myMailProcessor == null) {
            myMailProcessor   = new MailProcessor(APIUtil.getAttachmentLocation(), null);
            includeObjectDump = false;
        }

        myMailProcessor.logErrorWithObjectDump(myErrorLog, "", includeObjectDump, "error");
    }

    /**
     * This method invokes the TBitsMailer to send mails for this request.
     */
    public void sendMail(String sysPrefix, Request request) {
        try {
        	TBitsMailer tm = new TBitsMailer(request); 
            tm.sendMail();
        } catch (RuntimeException e) {
            LOG.error("Mails not send for:" + "\nSysPrefix : " + sysPrefix + "\nRequestId : " + request.getRequestId() + "\nActionId : " + request.getMaxActionId() + "\n\n"
                      + "",(e));
        } catch (Exception e) {
            LOG.error("Mails not send for:" + "\nSysPrefix : " + sysPrefix + "\nRequestId : " + request.getRequestId() + "\nActionId : " + request.getMaxActionId() + "\n\n"
                      + "",(e));
        }
    }

    /**
     * This method sends an acknowlegement once the request is successfully
     * transferred.
     *
     * @param aSourcePrefix    Source Prefix.
     * @param aSourceRequestId Id of source request.
     * @param aTargetPrefix    Target Prefix.
     * @param aTargetRequestId Id of target request.
     *
     */
    public void sendTransferAck(String aSourcePrefix, int aSourceRequestId, String aTargetPrefix, int aTargetRequestId) {
        try {
            String toAddress   = SysPrefixes.getEmail(aSourcePrefix);
            String fromAddress = SysPrefixes.getEmail(aTargetPrefix);

            if (toAddress == null) {
                throw new TBitsException("Invalid Prefix: " + aSourcePrefix);
            }

            if (fromAddress == null) {
                throw new TBitsException("Invalid Prefix: " + aTargetPrefix);
            }

            String                    headerValue =
                (new StringBuffer()).append(aSourcePrefix).append("#").append(aSourceRequestId).append(",").append(aTargetPrefix).append("#").append(aTargetRequestId).toString();
            Hashtable<String, String> headerTable = new Hashtable<String, String>();

            headerTable.put(TR_ACK_HEADER, headerValue);
            Mail.send(toAddress, fromAddress, headerTable);
            LOG.info("Ack sent...");
        } catch (Exception e) {
            StringBuffer message = new StringBuffer();

            message.append("An error occurred while sending acknowledgement ").append("after transferring a request: ").append("\nSource Prefix: ").append(aSourcePrefix).append(
                "\nSource Request: ").append(aSourceRequestId).append("\nTarget Prefix: ").append(aTargetPrefix).append("\nTarget Request: ").append(aTargetRequestId).append("\n\n").append(TBitsLogger.getStackTrace(e));
            LOG.severe(message.toString());
        }
    }

    /**
     *
     *
     */
    public void updateRequestTable(String srcRequest, String desRequest) {
        try {
            String[]     sArr      = srcRequest.split("#");
            String       sPrefix   = sArr[0];
            int          requestId = Integer.parseInt(sArr[1].trim());
            BusinessArea ba        = BusinessArea.lookupBySystemPrefix(sPrefix);
            int          systemId  = ba.getSystemId();

            Action.updateWithTransferInformation(systemId, requestId, desRequest);
        } catch (Exception e) {
            StringBuffer message = new StringBuffer();

            message.append("An error occurred while updating the ").append("transferred_requests table with the following ").append("information: ").append("\nSource Request: ").append(
                srcRequest).append("\nTarget Request: ").append(desRequest).append("\n\n").append(TBitsLogger.getStackTrace(e));
            LOG.severe(message.toString());
        }

        return;
    }

    /**
     * This method updates the record in transferred_requests table
     * corresponding to given sourceRequest
     *
     * @param sourceRequest Prefix#Id of source
     * @param targetRequest Prefix#Id of target
     *
     */
    public void updateTransferTable(String sourceRequest, String targetRequest) {
        try {
            String[]           sArr       = sourceRequest.split("#");
            String             sPrefix    = sArr[0];
            int                sRequestId = Integer.parseInt(sArr[1].trim());
            String[]           tArr       = targetRequest.split("#");
            String             tPrefix    = tArr[0];
            int                tRequestId = Integer.parseInt(tArr[1].trim());
            TransferredRequest tr         = new TransferredRequest(sPrefix, sRequestId, tPrefix, tRequestId);

            TransferredRequest.update(tr);
        } catch (Exception e) {
            StringBuffer message = new StringBuffer();

            message.append("An error occurred while updating the ").append("transferred_requests table with the following ").append("information: ").append("\nSource Request: ").append(
                sourceRequest).append("\nTarget Request: ").append(targetRequest).append("\n\n").append(TBitsLogger.getStackTrace(e));
            LOG.severe(message.toString());
        }

        return;
    }

    /**
     * This method validates the Ba name param.
     *
     * @param aBaName the ba name passed as param.
     *
     * @return the business area object for the ba name.
     *
     * @exception DatabaseException
     * @exception IllegalArgumentException
     */
    private BusinessArea validateBaName(String aBaName) throws DatabaseException, IllegalArgumentException {
        BusinessArea ba = BusinessArea.lookupByName(aBaName);

        if (ba == null) {
            setReturnStatus(1);
            setErrorLog("No Business Area with name: " + aBaName + ". Message not processed.Check details in mailq");

            throw new IllegalArgumentException("No Business Area with name: " + aBaName);
        }

        return ba;
    }

    /**
     * This method validates the logger/appender.
     *
     * @return the user object
     *
     * @exception DatabaseException
     * @exception IOException
     * @exception IllegalArgumentException
     */
    private User validateUser() throws DatabaseException, IOException, IllegalArgumentException {
        String fromAddress    = myMailProcessor.getFrom();
        String replyToAddress = myMailProcessor.getReplyTo();

        //
        // If reply-to is a BA mail Id, most probably it is tbits generated
        // mail and reached this Id because of incomplete exclusion list
        // in some instance. Bounce this mail.
        //
        if ((replyToAddress.equals("") == false) && (APIUtil.isBAEmail(replyToAddress) == true)) {
            setReturnStatus(2);
            setErrorLog("Request not logged. BA mail id in reply To\nFrom: " + fromAddress + "\nReplyTo: " + replyToAddress);

            throw new IllegalArgumentException("Request not logged. BA mail id in reply To\nFrom: " + fromAddress + "\nReplyTo: " + replyToAddress);
        }

        User user = null;

        if (fromAddress.equals("") == false) {
            user = TBitsHelper.validateEmailUser(fromAddress);
        }

        if ((user == null) && (replyToAddress.equals("") == false)) {
            user = TBitsHelper.validateEmailUser(replyToAddress);
        }

        if (user == null) {
            setReturnStatus(2);
            setErrorLog("Request not logged. Invalid User\nFrom: " + fromAddress + "\nReplyTo: " + replyToAddress);

            throw new IllegalArgumentException("Request not logged. Invalid User\nFrom: " + fromAddress + "\nReplyTo: " + replyToAddress);
        }

        return user;
    }

    //~--- get methods --------------------------------------------------------

    /**
     * Accessor method to get the errorLog.
     *
     * @return errorLog
     */
    public String getErrorLog() {
        return (myErrorLog == null)
               ? ""
               : myErrorLog;
    }

    /**
     * Accessor method to get the return status.
     *
     * @return ReturnStatus
     */
    public int getReturnStatus() {
        return myReturnStatus;
    }

    //~--- set methods --------------------------------------------------------

    /**
     * Setter method to set the error log.
     *
     * @param aErrorLog string
     */
    public void setErrorLog(String aErrorLog) {
        myErrorLog = aErrorLog;
    }

    /**
     * Setter method to set the return status.
     *
     * @param aReturnStatus
     */
    public void setReturnStatus(int aReturnStatus) {
        myReturnStatus = aReturnStatus;
    }
}
