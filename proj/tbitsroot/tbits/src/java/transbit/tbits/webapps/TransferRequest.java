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
 * TransferRequest.java
 *
 * $Header:
 */
package transbit.tbits.webapps;

//~--- non-JDK imports --------------------------------------------------------

import transbit.tbits.Helper.ActionHelper;
import transbit.tbits.Helper.Messages;
import transbit.tbits.Helper.SysPrefixes;
import transbit.tbits.Helper.TBitsConstants;

//TBits Imports.
import transbit.tbits.api.APIUtil;
import transbit.tbits.api.AddRequest;
import transbit.tbits.api.UpdateRequest;
import transbit.tbits.common.DTagReplacer;
import transbit.tbits.common.DatabaseException;
import transbit.tbits.common.Mail;
import transbit.tbits.common.TBitsLogger;
import transbit.tbits.common.Timestamp;
import transbit.tbits.common.Utilities;
import transbit.tbits.config.CaptionsProps;
import transbit.tbits.config.SysConfig;
import transbit.tbits.domain.Action;
import transbit.tbits.domain.BusinessArea;
import transbit.tbits.domain.DateTimeFormat;
import transbit.tbits.domain.Field;
import transbit.tbits.domain.FieldDescriptor;
import transbit.tbits.domain.Request;
import transbit.tbits.domain.TransferredRequest;
import transbit.tbits.domain.Type;
import transbit.tbits.domain.User;
import transbit.tbits.exception.APIException;
import transbit.tbits.exception.TBitsException;

//Imports from current package.
import transbit.tbits.webapps.WebUtil;

//Static imports.
import static transbit.tbits.Helper.TBitsConstants.PKG_WEBAPPS;
import static transbit.tbits.Helper.TBitsConstants.TR_REQ_HEADER;

//~--- JDK imports ------------------------------------------------------------

//Java Imports.
import java.io.IOException;
import java.io.PrintWriter;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Hashtable;

import javax.mail.Message;
import javax.mail.Session;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

//~--- classes ----------------------------------------------------------------

/**
 * The TransferRequest class. This class displays form to edit description and
 * attachments for an action
 *
 * @author  : Vaibhav.
 * @version : $Id: $
 */
public class TransferRequest extends HttpServlet {
    public static final TBitsLogger LOG       = TBitsLogger.getLogger(PKG_WEBAPPS);
    private static final String     HTML_FILE = "web/tbits-transfer-request.htm";

    //~--- methods ------------------------------------------------------------

    /**
     * This method adds the request header in text format to the request history
     *
     * @param aSystemId
     * @param aFieldName
     * @param aValue
     * @param aTextContent
     * @param aMaxNameLength
     */
    private void addHeader(int aSystemId, String aFieldName, String aValue, StringBuffer aTextContent, int aMaxNameLength) {
        Field field = null;

        try {
            field = Field.lookupBySystemIdAndFieldName(aSystemId, aFieldName);
        } catch (Exception e) {
            LOG.debug("",(e));
        }

        String displayName = "-";

        if (field != null) {
            if (field.getName().equals(Field.REQUEST)) {
                displayName = "Original " + field.getDisplayName();
            } else {
                displayName = field.getDisplayName();
            }
        }

        aTextContent.append(displayName);

        int tempLength = displayName.length();

        for (int a = 0; a < (aMaxNameLength - tempLength); a++) {
            aTextContent.append(" ");
        }

        aTextContent.append(" ").append(aValue).append("\n");
    }

    /**
     * This method services the Http-GET Requests to this servlet.
     *
     * @param  aRequest          the HttpServlet Request Object
     * @param  aResponse         the HttpServlet Response Object
     *
     * @throws ServletException
     * @throws IOException
     */
    public void doGet(HttpServletRequest aRequest, HttpServletResponse aResponse) throws ServletException, IOException {
        aResponse.setContentType("text/html");

        PrintWriter out     = aResponse.getWriter();
        HttpSession session = aRequest.getSession(true);

        Utilities.registerMDCParams(aRequest);

        try {
            handleGetRequest(aRequest, aResponse);
        } catch (Exception de) {
            LOG.info("",(de));

            return;
        } finally {
            Utilities.clearMDCParams();
        }
    }

    /**
     * This method services the Http-POST Requests to this servlet.
     *
     * @param  aRequest          the HttpServlet Request Object
     * @param  aResponse         the HttpServlet Response Object
     *
     * @throws ServletException
     * @throws IOException
     */
    public void doPost(HttpServletRequest aRequest, HttpServletResponse aResponse) throws ServletException, IOException {
        aResponse.setContentType("text/html");

        PrintWriter out     = aResponse.getWriter();
        HttpSession session = aRequest.getSession(true);

        Utilities.registerMDCParams(aRequest);

        try {
        	//throw new DatabaseException("Hellow", new SQLException());
            handlePostRequest(aRequest, aResponse);
            
        }catch (DatabaseException de) {
            session.setAttribute("ExceptionObject", de);
            //aResponse.getOutputStream().print("from the lord server.");
        }
//        catch (TBitsException e) {
//			// TODO: handle exception
//        	 session.setAttribute("ExceptionObject", e);
//        	 aResponse.sendRedirect(WebUtil.getServletPath("/error"));
//		}
        catch (Exception de) {
            LOG.info("",(de));
        } finally {
            Utilities.clearMDCParams();
        }
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
    private void handleGetRequest(HttpServletRequest aRequest, HttpServletResponse aResponse) throws ServletException, IOException, TBitsException, DatabaseException, Exception {
        aResponse.setContentType("text/html");

        PrintWriter out = aResponse.getWriter();

        // Validate the user.
        User user   = WebUtil.validateUser(aRequest);
        int  userId = user.getUserId();

        // Get the System Prefix.
        String sysPrefix = aRequest.getParameter("sysPrefix");

        if ((sysPrefix == null) || sysPrefix.trim().equals("")) {
            out.println(Messages.getMessage("INVALID_BUSINESS_AREA", sysPrefix));

            return;
        }

        BusinessArea ba = BusinessArea.lookupBySystemPrefix(sysPrefix);

        if (ba == null) {
            out.println(Messages.getMessage("INVALID_BUSINESS_AREA", sysPrefix));

            return;
        }

        int       systemId = ba.getSystemId();
        SysConfig sc       = ba.getSysConfigObject();

        // Get the request id.
        String strRequestId = aRequest.getParameter("requestId");
        int    requestId    = 0;

        try {
            requestId = Integer.parseInt(strRequestId);
        } catch (Exception e) {
            requestId = 0;
        }

        if (requestId == 0) {
            out.println(Messages.getMessage("INVALID_BUSINESS_AREA", strRequestId));

            return;
        }

        Request request = Request.lookupBySystemIdAndRequestId(systemId, requestId);

        if (request == null) {
            out.println(Messages.getMessage("INVALID_BUSINESS_AREA", strRequestId));

            return;
        }

        String prefixId = sysPrefix + "#" + Integer.toString(requestId);

        // Check if this request is already transferred.
        TransferredRequest tr = TransferredRequest.lookupBySourcePrefixAndRequestId(sysPrefix, requestId);

        if (tr != null) {
            String targetRequest = tr.getTargetPrefix() + "#" + tr.getTargetRequestId();

            out.println(Messages.getMessage("TRANSFER_ALLOWED_ONLY_ONCE", targetRequest));

            return;
        }

        ArrayList<Action> actionList = Action.getAllActions(systemId, requestId, "asc");
        ArrayList<BusinessArea> baList     = BusinessArea.getBusinessAreasByUserId(userId);
        DTagReplacer      hp         = new DTagReplacer(HTML_FILE);

        hp.replace("nearestPath", WebUtil.getNearestPath(aRequest, ""));
        hp.replace("cssFile", WebUtil.getCSSFile(sc.getWebStylesheet(), sysPrefix, false));
        hp.replace("prefixId", prefixId);
        hp.replace("sysPrefix", sysPrefix);
        hp.replace("systemId", Integer.toString(systemId));
        hp.replace("requestId", Integer.toString(requestId));
        hp.replace("baList", renderBAList(baList, systemId));
        hp.replace("logger_ids", APIUtil.toLoginList(request.getLoggers()));
        hp.replace("subscriber_ids", APIUtil.toLoginList(request.getSubscribers()));
        hp.replace("subject", request.getSubject());
        hp.replace("statusList", getStatusList(systemId));

//      String transferInfo = "Request Transferred from " + 
//          sysPrefix + "#" + requestId + 
//          " by '" + user.getUserLogin() + "'.\n\n";
//
        hp.replace("description", Utilities.htmlEncode(    // transferInfo +
            getTextContent(ba, request, actionList)));
        out.println(hp.parse(ba.getSystemId()));

        return;
    }
    
   

    /**
     * This method actually services the Http-POST Requests to this servlet.
     *
     *
     * @param aRequest          the HttpServlet Request Object
     * @param aResponse         the HttpServlet Response Object
     * @throws ServletException
     * @throws IOException
     * @throws TBitsExceptionrows DatabaseException
     */
    private void handlePostRequest(HttpServletRequest aRequest, HttpServletResponse aResponse) throws ServletException, IOException, TBitsException, DatabaseException {
        aResponse.setContentType("text/html");

        PrintWriter out = aResponse.getWriter();

        // Validate the user and get the required information.
        User   user      = WebUtil.validateUser(aRequest);
        int    userId    = user.getUserId();
        String userLogin = user.getUserLogin();

        // Get the System Prefix.
        String sysPrefix = aRequest.getParameter("sysPrefix");

        if ((sysPrefix == null) || sysPrefix.trim().equals("")) {
            out.println(Messages.getMessage("TRANSFER_FAILED"));

            return;
        }

        BusinessArea ba = BusinessArea.lookupBySystemPrefix(sysPrefix);

        if (ba == null) {
            out.println(Messages.getMessage("INVALID_BUSINESS_AREA", sysPrefix));

            return;
        }

        int systemId = ba.getSystemId();

        // Get the request id.
        String strRequestId = aRequest.getParameter("requestId");
        int    requestId    = 0;

        try {
            requestId = Integer.parseInt(strRequestId);
        } catch (Exception e) {
            requestId = 0;
        }

        if (requestId == 0) {
            out.println(Messages.getMessage("INVALID_BUSINESS_AREA", strRequestId));

            return;
        }

        String prefixId = sysPrefix + "#" + Integer.toString(requestId);

        LOG.info("Attempt to transfer a request: " + "\nUser: " + user.getUserLogin() + "\nRequest: " + prefixId);

        // Check if this request is already transferred.
        TransferredRequest tr = TransferredRequest.lookupBySourcePrefixAndRequestId(sysPrefix, requestId);

        if (tr != null) {
            String targetRequest = tr.getTargetPrefix() + "#" + tr.getTargetRequestId();

            out.println(Messages.getMessage("TRANSFER_ALLOWED_ONLY_ONCE", targetRequest));

            return;
        }

        String targetPrefix = aRequest.getParameter("targetPrefix");

        if ((targetPrefix == null) || (targetPrefix.trim().equals("") == true)) {
            out.println(Messages.getMessage("INVALID_BUSINESS_AREA", targetPrefix));

            return;
        }

        String loggers = aRequest.getParameter("logger_ids");

        if ((loggers == null) || (loggers.trim().equals("") == true)) {
            out.println(Messages.getMessage("TRANSFER_LOGGER_EMPTY"));
            return;
        }
        String closedStatus = aRequest.getParameter("closedStatus");
        if ((closedStatus == null) || (closedStatus.trim().equals("") == true)) {
            out.println( "Select the status to which current "
            		+ CaptionsProps.getInstance().getCaptionsHashMap(ba.getSystemId()).get("captions.all.request") 
            		+" should be marked.");
            return;
        }


        String subscribers = aRequest.getParameter("subscriber_ids");

        if ((subscribers == null) || (subscribers.trim().equals("") == true)) {
            subscribers = "";
        }

        String subject = aRequest.getParameter("subject");

        if ((subject == null) || (subject.trim().equals("") == true)) {
            subject = "";
        }

        String description = aRequest.getParameter("description");

        if ((description == null) || (description.trim().equals("") == true)) {
            description = "";
        }
        String reqHistory = aRequest.getParameter("reqHistory");
        if(reqHistory == null)
        	reqHistory = "";
        
        try {
        	System.out.println("Transferring: sysid: " + ba.getSystemId() + " pref: " +  sysPrefix + " rid: " 
        			+  requestId + " tprefix: " + targetPrefix + " login: " + userLogin + " loggers: " +  loggers + " subs: " 
        			+ subscribers + " subj: " + subject + " <desc> '" + description + "' </desc>");
            //sendTransferMessage(ba.getSystemId(), sysPrefix, requestId, targetPrefix, userLogin, loggers, subscribers, subject, description);
            //It just closes a source request. Should be avoided. Instead use update request.
            //Action.insertTransferAction(systemId, requestId, userId);
        	
        	
        	
            Hashtable<String, String> newReqParams = new Hashtable<String, String>();
            newReqParams.put(Field.BUSINESS_AREA, targetPrefix);
            newReqParams.put(Field.LOGGER, loggers);
            newReqParams.put(Field.SUBJECT, subject);
            newReqParams.put(Field.DESCRIPTION, description);
            newReqParams.put(Field.USER, userLogin);
            // Submit this to the AddRequest API.
            AddRequest addRequest = new AddRequest();
            addRequest.setSource(TBitsConstants.SOURCE_WEB);
            addRequest.setContext(aRequest.getContextPath());
            
            Request request = addRequest.addRequest(newReqParams);
            
            Hashtable<String, String> updateRequestParams = new Hashtable<String, String>();
            updateRequestParams.put(Field.BUSINESS_AREA,sysPrefix);
            updateRequestParams.put(Field.REQUEST, Integer.toString(requestId));
            updateRequestParams.put(Field.USER, userLogin);
            updateRequestParams.put(Field.STATUS, closedStatus);
            updateRequestParams.put(Field.DESCRIPTION, "[Transferred to " + targetPrefix + "#" + request.getRequestId() + "]");
            UpdateRequest updateRequest = new UpdateRequest();
            updateRequest.setSource(TBitsConstants.SOURCE_WEB);
            updateRequest.setContext(aRequest.getContextPath());
            updateRequest.updateRequest(updateRequestParams);
            
            tr = new TransferredRequest(sysPrefix, requestId, targetPrefix, request.getRequestId());
            TransferredRequest.insert(tr);
            
        }catch (APIException e) {
        	 LOG.severe("Error while adding a request" + "",(e));
             out.println(Messages.getMessage("TRANSFER_FAILED"));
		} 
        catch (Exception e) {
            LOG.severe("",(e));
            out.println(Messages.getMessage("TRANSFER_FAILED"));
        } 
        out.println("true");

        return;
    }

    /**
     * This method renders the list of BAs available for the request to be
     * transferred.
     *
     * @param baList     List of BAs from all the sites.
     * @param sysPrefix  Prefix of the BA which the current request belongs to.
     *                   This will not be rendered as transferring within a
     *                   BA is not allowed.
     *
     * @return HTML list of BAs.
     */
    private String renderBAList(ArrayList<String> baList, String sysPrefix) {
        if ((baList == null) || (baList.size() == 0)) {
            return "";
        }

        StringBuffer buffer = new StringBuffer();

        for (String str : baList) {
            String[] arr    = str.split("\n");
            String   name   = arr[0].trim();
            String   prefix = arr[1].trim();

            /*
             * Ignore if this is the current BA.
             */
            if (prefix.equalsIgnoreCase(sysPrefix) == true) {
                continue;
            }
            buffer.append("\n<OPTION value='").append(prefix).append("'>").append(name).append(" [").append(prefix).append("]").append("</OPTION>");
        }
        return buffer.toString();
    }

    private String renderBAList(ArrayList<BusinessArea> baList, int sysId) {
        if ((baList == null) || (baList.size() == 0)) {
            return "";
        }

        StringBuffer buffer = new StringBuffer();

        for (BusinessArea ba : baList) {
           
            /*
             * Ignore if this is the current BA.
             */
            if (ba.getSystemId() == sysId) {
                continue;
            }
            String prefix = ba.getSystemPrefix();
            String name = ba.getName();

            buffer.append("\n<OPTION value='").append(prefix).append("'>").append(name).append(" [").append(prefix).append("]").append("</OPTION>");
        }

        return buffer.toString();
    }
    
    private String getStatusList(int aSystemId) {
    	 StringBuilder   buffer              = new StringBuilder();
    	ArrayList<Type> types = null;
		try {
			types = Type.lookupBySystemIdAndFieldName(aSystemId, Field.STATUS);
		} catch (DatabaseException e) {
			// TODO Auto-generated catch block
			LOG.error("Unable to get a list of statuses for ba: "+ aSystemId + "\n" + e);
			return buffer.toString();
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

           
            buffer.append("<OPTION value=\"").append(Utilities.htmlEncode(type.getName())).append("\" ");

            buffer.append(">").append(Utilities.htmlEncode(type.getDisplayName()));

            if (type.getIsPrivate() == true) {
                buffer.append(" &dagger;");
            }

            buffer.append("</OPTION>\n");
        }
    	return buffer.toString();
    }
    /**
     * This method sends an email to the target business area.
     *
     * @param sourcePrefix
     * @param sourceRequestId
     * @param targetPrefix
     * @param loggers
     * @param subscribers
     * @param subject
     * @param description
     * @throws Exception
     */
    private void sendTransferMessage(int sourceSystemId, String sourcePrefix, int sourceRequestId, String targetPrefix, String transferUser, String loggers, String subscribers, String subject,
                                     String description)
            throws Exception {

        // Parameters needed while constructing the message.
        // From address
        InternetAddress fromAddress = null;

        // Reply To Address
        InternetAddress[] replyTo = new InternetAddress[1];

        // List of users who appear in to-list.
        InternetAddress[] toList = null;

        // Subject is the one passed to this method.
        // Text content is again the argument passed.
        String targetEmail = SysPrefixes.getEmail(targetPrefix);

        if ((targetEmail == null) || (targetEmail.trim().equals("") == true)) {
            throw new TBitsException("Invalid target prefix.");
        }

        loggers     = loggers.replace(';', ',');
        subscribers = subscribers.replace(';', ',');

        String[] arrLoggers     = loggers.split(",");
        String[] arrSubscribers = subscribers.split(",");

        /*
         * User performing the transfer is the From/ReplyTo for this email.
         */
        User   fromUser  = User.lookupAllByUserLogin(transferUser);
        String fromEmail = fromUser.getEmail();

        fromAddress = new InternetAddress(fromUser.getEmail());
        replyTo[0]  = fromAddress;

        StringBuffer recList = new StringBuffer();

        recList.append(targetEmail);

        /*
         * for (int i=0; i < arrLoggers.length; i++)
         * {
         *   User user = User.lookupAllByUserLogin(arrLoggers[i]);
         *   if (user == null) continue;
         *   recList.append(",").
         *       append(user.getUserLogin());
         * }
         */

        // Add the subscribers to the To: list.
        for (int i = 0; i < arrSubscribers.length; i++) {
            User user = User.lookupAllByUserLogin(arrSubscribers[i]);

            if (user == null) {
                continue;
            }

            recList.append(",").append(user.getEmail());
        }

        // Get the recipients list.
        toList = InternetAddress.parse(recList.toString());

        // The only header we want to pass.
        String headerKey   = TR_REQ_HEADER;
        String headerValue = sourcePrefix + "#" + sourceRequestId;

        // create properties and get the default Session.
        java.util.Properties properties = new java.util.Properties();
        Session              session    = Session.getDefaultInstance(properties, null);
        MimeMessage          message    = new MimeMessage(session);

        message.setFrom(fromAddress);
        message.setReplyTo(replyTo);
        message.setRecipients(Message.RecipientType.TO, toList);
        message.setSubject(subject);

        MimeMultipart mp  = new MimeMultipart();
        MimeBodyPart  mbp = new MimeBodyPart();

        /*
         * Prefix the logger list to the description as an EUC.
         */
        String          primDescLog = "";
        FieldDescriptor fd          = null;

        try {
            fd = FieldDescriptor.getPrimaryDescriptor(sourceSystemId, Field.LOGGER);
        } catch (Exception e) {}

        primDescLog = (fd == null)
                      ? Field.LOGGER
                      : fd.getDescriptor();
        description = "/" + primDescLog + ": " + loggers + "\n\n" + description;
        description = description.replaceAll("^[\n]+", "");
        description = Utilities.htmlEncode(description);
        description = description.replaceAll("\\r\\n", "<br>");
        description = description.replaceAll("\\n", "<br>");
        description = description.replaceAll("\\r", "<br>");
        description = description.replaceAll("<br>", "\n<br>");
        description = description.replaceAll("<br> ", "<br>&nbsp;");
        description = description.replaceAll("  ", "&nbsp;&nbsp;");
        description = description.replaceAll("\\t", "&nbsp;&nbsp;&nbsp;&nbsp;");
        mbp.setContent(description, "text/html");
        mbp.setHeader("Content-Type", "text/html; charset=UTF-8");
        mp.addBodyPart(mbp);
        message.setContent(mp);
        message.setHeader(headerKey, headerValue);

        //
        // finally handover the message to the sendMail running on this
        // Machine for queuing this message for further processing.
        //
        try {
            Mail.postMessageToSMTPServer(session, message, fromEmail, targetEmail);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //~--- get methods --------------------------------------------------------

    /**
     * Method to get Date disaplyed as per Business area DateTimeFormat
     *
     * @param aDate DateTime as Timestamp
     *
     * @return DateString as per Business area DateTimeFormat configuration
     */
    public String getDateString(BusinessArea aBusinessArea, Timestamp aDate) {
        if (aDate == null) {
            return "-";
        }

        try {
            DateTimeFormat dateTimeFormat = DateTimeFormat.lookupByDateTimeFormatId(aBusinessArea.getSysConfigObject().getEmailDateFormat());

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

    public String getDateString(BusinessArea aBusinessArea, Date aDate) {
        if (aDate == null) {
            return "-";
        }

        try {
            DateTimeFormat dateTimeFormat = DateTimeFormat.lookupByDateTimeFormatId(aBusinessArea.getSysConfigObject().getEmailDateFormat());

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
     * This method returns the length of longest DisplayName of any
     *  Fixed Field. This is used for formatting of Header in Text mail.
     */
    private int getMaxLength(int systemId) {
        ArrayList<Field> fieldsList = null;

        try {
            fieldsList = Field.getFixedFieldsBySystemId(systemId);
        } catch (Exception e) {
            LOG.debug("",(e));
        }

        if ((fieldsList == null) || (fieldsList.size() == 0)) {
            return 50;
        }

        int maxLength = 0;

        for (Field field : fieldsList) {
            String displayName = "-";

            if (field.getName().equals(Field.REQUEST)) {
                displayName = "Original " + field.getDisplayName();
            } else {
                displayName = field.getDisplayName();
            }

            maxLength = (maxLength > displayName.length())
                        ? maxLength
                        : displayName.length();
        }

        return maxLength;
    }

    /**
     * Thss method obtains the text format of request history.
     *
     * @param aBusinessArea
     * @param aRequestId
     * @param aActionList
     * @param aBaMail
     * @return Action Details in text format.
     * @throws DatabaseException
     */
    public String getTextActionDetails(BusinessArea aBusinessArea, int aRequestId, ArrayList<Action> aActionList, boolean aBaMail) throws DatabaseException {
        int           systemId          = aBusinessArea.getSystemId();
        StringBuilder actionDescription = new StringBuilder();

        for (Action action : aActionList) {

            // Getting the description
            String description = action.getDescription();

            actionDescription.append("\n").append(action.getActionId()).append(". ").append(ActionHelper.getUserDisplayName(action.getUserId()));
            actionDescription.append(": ").append(getDateString(aBusinessArea, action.getLastUpdatedDate())).append("\n")
            .append("************************************").append(
                "************************************\n\n").append(description.trim()).append("\n");
        }

        return actionDescription.toString();
    }

    /**
     * This method obtains the text format of the request details.
     *
     * @param aBusinessArea
     * @param aRequest
     * @param aActionList
     *
     * @return Text format of request history.
     * @throws Exception
     */
    private String getTextContent(BusinessArea aBusinessArea, Request aRequest, ArrayList<Action> aActionList) throws Exception {
        StringBuffer textContent = new StringBuffer();
        int          requestId   = aRequest.getRequestId();
        int          systemId    = aRequest.getSystemId();
        String       sysPrefix   = aBusinessArea.getSystemPrefix();

        //
        // To get the length of field with longest display Name
        // for formatting the Text mail header fields.
        //
        int maxNameLength = getMaxLength(systemId);

        addHeader(systemId, Field.REQUEST, sysPrefix + "#" + requestId, textContent, maxNameLength);

        int parentId = aRequest.getParentRequestId();

        // If parent present add to header
        if (parentId > 0) {
            addHeader(systemId, Field.PARENT_REQUEST_ID, sysPrefix + "#" + parentId, textContent, maxNameLength);
        }

        addHeader(systemId, Field.SUBJECT, aRequest.getSubject(), textContent, maxNameLength);
        addHeader(systemId, Field.CATEGORY, aRequest.getCategoryId().getDisplayName() + ((aRequest.getCategoryId().getIsPrivate() == false)
                ? ""
                : "*"), textContent, maxNameLength);
        addHeader(systemId, Field.LOGGER, APIUtil.toLoginList(aRequest.getLoggers()), textContent, maxNameLength);
        addHeader(systemId, Field.ASSIGNEE, APIUtil.toLoginList(aRequest.getAssignees()), textContent, maxNameLength);
        addHeader(systemId, Field.LOGGED_DATE, getDateString(aBusinessArea, aRequest.getLoggedDate()), textContent, maxNameLength);
        addHeader(systemId, Field.LASTUPDATED_DATE, getDateString(aBusinessArea, aRequest.getLastUpdatedDate()), textContent, maxNameLength);
        addHeader(systemId, Field.DUE_DATE, getDateString(aBusinessArea, aRequest.getDueDate()), textContent, maxNameLength);
        addHeader(systemId, Field.REQUEST_TYPE, aRequest.getRequestTypeId().getDisplayName(), textContent, maxNameLength);
        addHeader(systemId, Field.SEVERITY, aRequest.getSeverityId().getDisplayName(), textContent, maxNameLength);
        addHeader(systemId, Field.STATUS, aRequest.getStatusId().getDisplayName(), textContent, maxNameLength);
        textContent.append("\n\n").append(getTextActionDetails(aBusinessArea, aRequest.getRequestId(), aActionList, false));

        /*
         * check if this request has any summary in it. If so,append it to the
         * text-action-details part.
         */
        String summary = aRequest.getSummary();

        if ((summary != null) && (summary.trim().equals("") == false)) {
            textContent.append("----------------------------------------")
            .append("----------------------------------------")
            .append("\n\n").append(" ===================================\n")
            .append(
                "/ Summary for ")
                .append(sysPrefix).append("#")
                .append(requestId).append(": /\n")
                .append("===================================\n")
                .append("\n")
                .append(summary.trim()).append("");
        }

        return textContent.toString();
    }
}
