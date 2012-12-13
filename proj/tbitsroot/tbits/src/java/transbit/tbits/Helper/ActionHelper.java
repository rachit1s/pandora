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
 * ActionUtil.java
 *
 * $Header:
 */
package transbit.tbits.Helper;

//~--- non-JDK imports --------------------------------------------------------

import transbit.tbits.Helper.Messages;
import transbit.tbits.Helper.TBitsConstants;
import transbit.tbits.TVN.WebdavConstants;

//TBits Imports
import transbit.tbits.api.APIUtil;
import transbit.tbits.api.AttachmentInfo;
import transbit.tbits.common.ActionFileInfo;
import transbit.tbits.common.Configuration;
import transbit.tbits.common.DTagReplacer;
import transbit.tbits.common.DatabaseException;
import transbit.tbits.common.TBitsLogger;
import transbit.tbits.common.Timestamp;
import transbit.tbits.common.Utilities;
import transbit.tbits.common.PropertiesHandler;
import transbit.tbits.config.Attachment;
import transbit.tbits.config.CustomLink;
import transbit.tbits.config.SysConfig;
import transbit.tbits.config.WebConfig;
import transbit.tbits.domain.BusinessArea;
import transbit.tbits.domain.DataType;
import transbit.tbits.domain.Field;
import transbit.tbits.domain.FieldDescriptor;
import transbit.tbits.domain.FileRepoIndexObject;
import transbit.tbits.domain.Permission;
import transbit.tbits.domain.Request;
import transbit.tbits.domain.RequestUser;
import transbit.tbits.domain.Type;
import transbit.tbits.domain.User;
import transbit.tbits.exception.TBitsException;
import transbit.tbits.webapps.AddHtmlRequest;
import transbit.tbits.webapps.WebUtil;

import static transbit.tbits.Helper.TBitsConstants.ASC_ORDER;

//TBits Static Imports
import static transbit.tbits.search.SearchConstants.STOP_WORD_LIST;

//---import static properties related to AutoVue-------------------
import static transbit.tbits.Helper.TBitsPropEnum.IS_AUTOVUE_ENABLED;
//~--- JDK imports ------------------------------------------------------------



//Java Imports
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;

import java.net.URLEncoder;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.htmlcleaner.HtmlCleaner;
import org.htmlcleaner.TagNode;

import net.sf.json.JSON;
import net.sf.json.JSONObject;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

//~--- classes ----------------------------------------------------------------

/**
 * This class provides modules used across to display action details.
 *
 *
 * @author : Vinod Gupta
 * @version : $Id: $
 */
public class ActionHelper implements TBitsConstants {

    // Application Logger.
    public static final TBitsLogger LOG               = TBitsLogger.getLogger(PKG_UTIL);
    public static final String      LINK_SPACER       = "&nbsp;|&nbsp;";
    public static final String      HTML_LINE_BREAK   = "<br>";
    public static final String      TEXT_LINE_BREAK   = "\n";
    public static final String      TEXT_HEADER_CLASS = "hsp";
    public static String            TEXT_FIELD_HTML   = "\n<div id=\"<=id>\" style='border: 2px solid white'>" + "<a name=\"<=actionId>\" style='font-size: 1px'></a>" + "<div class=\"<=class>\">"
                                                        + "<div class=\"<=headerClass>\">" + "<table cellpadding=\"0\" cellspacing=\"0\">"
                                                        + "<tbody>\n<tr class=\"s\"><td align=\"left\" width=\"100%\">" + "<span class=\"b <=textColor>\">"
                                                        + "<=label></span><span style=\"color: #4A5782\">" + "<=updated_by></span> <=diff>" + "</td><td align=\"right\" "
                                                        + "noWrap><=actionLogLink>&nbsp;<=editContentLink>" + "<span class=\"b <=textColor>\">"
                                                        + "<=updated_time></span></td></tr></tbody></table></div>" + "<div id=\"<=contentId>\" " + "style=\"clear:none;display:<=showHeader>\""
                                                        + " class=\"<=contentClass> bw ff sf ww\">" + "<=value></div>" + "<=attachments><=actionLog></div></div>";
    public static final String TEXT_CONTENT_CLASS       = "bhs dp";
    public static final String TEXT_CLASS               = "bh";
    public static final String READ_ACTION_HEADER_CLASS = "dp";
    public static final String READ_ACTION_CLASS        = "adm drb";
    public static String       PARENT_REQUEST_HTML      = "<span class=\"cw b crhb\">&nbsp;<%=parent_request_id_label%>: </span>" + "<a class=\"l cw b\" <%=parent_subject%> href="
                                                          + "\"<=path>/q/<%=sysPrefix%>/<%=parent_request_id%>\" target=\"" + "<%=sysPrefix%>_<%=parent_request_id%>\">" + "<%=parent_request_id%></a>";
    public static final String NEW_ACTION_HEADER_CLASS   = "bdn dp";
    public static final String NEW_ACTION_CLASS          = "adm dnb";
    public static final String FIRST_ACTION_HEADER_CLASS = "bdf dp";
    public static final String FIRST_ACTION_CLASS        = "adm dfb";
    public static String       ATTACHMENT_CONTAINER_HTML = "\n<div class=\"dvm sf ff\">\n<=attachments></div>";
    public static String       ACTION_LOG_CONTAINER_HTML = "\n<div id=\"<=id>\" class=\"sf\" " + "style=\"clear:none;display:<=display>\">\n"
                                                         + "<div class=\"bw ff  dvm cal\">\n<=actionLog>\n</div>\n</div>";
    
    
    
    
    
    
    //~--- methods ------------------------------------------------------------

    /**
     * This method fills the custom links For Ba, if any.
     *
     * @param aTagTable      Hashtable collecting <field,value> pairs
     * @param aBusinessArea  BusienssArea object
     */
    public static void fillCustomLinks(Hashtable<String, String> aTagTable, BusinessArea aBusinessArea) {
        StringBuffer Links = new StringBuffer();

        try {
            SysConfig baConfig = aBusinessArea.getSysConfigObject();

            if (baConfig == null) {
                aTagTable.put("customLink", "");

                return;
            }

            ArrayList<CustomLink> customLinks = baConfig.getCustomLinks();

            if (customLinks == null) {
                aTagTable.put("customLink", "");

                return;
            }

            int size = customLinks.size();

            if (size == 0) {
                aTagTable.put("customLink", "");

                return;
            }

            for (int i = 0; i < size; i++) {
                CustomLink cl = (CustomLink) customLinks.get(i);

                if (Links.toString().trim().equals("") == false) {
                    Links.append(LINK_SPACER);
                }

                Links.append("<a class=\"l sx b cw\" href='").append(cl.getValue()).append("' target=\"_blank\">").append(cl.getName()).append("</a>");
            }
        } catch (Exception e) {
            LOG.severe("",(e));
        }

        aTagTable.put("customLink", Links.toString());

        return;
    }

    /* this is kind of temporary method to format the email data separately as it also requires the permissions
     * to send only values which have email permission on. This should be mearged with the fillHeaderData method
     * later.
     * 
     * This method  fills fixed and extended fields Header with values
     * by replacing <%=field_label%> and <%=field_value%> tags.
     *
     * @param aTagTable      Hashtable collecting <field,value> pairs
     * @param aRequest       Request object
     * @param aPath          the server path
     */ 
    public static void fillEmailHeaderData(User user, Hashtable<String, String> aTagTable, Request aRequest, String aPath, int aMaxEmailActionId, String aDateFormat, TimeZone aZone,
                                      ArrayList<String> aSearchTextList, int aSystemId, int aAppendInterface, int aToolTipType, Hashtable<String,Integer> permissions, Hashtable<Integer,Collection<ActionFileInfo>> actionFileHash )
            throws Exception {
        int                    systemId         = aRequest.getSystemId();
        BusinessArea           ba               = BusinessArea.lookupBySystemId(systemId);
        String                 sysPrefix        = ba.getSystemPrefix();
        Collection<RequestUser> list             = null;
        String                 fieldName        = "";
        String                 fieldValue       = "";
        FieldDescriptor        fd               = null;
        ArrayList<Field>       fieldsList       = Field.lookupBySystemId(systemId);
        String                 statusDescriptor = Field.STATUS;

        fd = FieldDescriptor.getPrimaryDescriptor(systemId, Field.STATUS);

        Collection<ActionFileInfo> actionFiles = actionFileHash.get(aRequest.getMaxActionId());
        if (fd != null) {
            statusDescriptor = fd.getDescriptor();
        }

        for (Field field : fieldsList) 
        {
            fieldName  = field.getName();
            fieldValue = aRequest.get(fieldName);
            Integer perm = permissions.get(fieldName) ;
            if( null == perm )
            	perm = 0 ;
            
            if( ( perm & Permission.EMAIL_VIEW ) == 0 )
            	continue;

            switch (field.getDataTypeId()) 
            {
	            case DataType.USERTYPE :
	            {
	            	list = (Collection<RequestUser>) aRequest.getObject(fieldName);
	                fieldValue = ActionHelper.getUserFieldHtml(list, field, ba, aPath, statusDescriptor);
	            }
	                break;
	
	            case DataType.DATE :
	            case DataType.TIME :
	            case DataType.DATETIME :
	            {
	            		
	                    String dateFormat = null;
	                    if(user == null)
	                    	System.out.println("USER IS NULL...");
	                    
	                    WebConfig wc = user.getWebConfigObject();
	                    if(wc != null)
	                    	dateFormat = wc.getWebDateFormat();
	                    
	                    if( null == dateFormat || dateFormat.trim().equals(""))
	                    	dateFormat = aDateFormat ;
	                    
	                    Date date = (Date) aRequest.getObject(fieldName);
	                    if( null != date && null != dateFormat )
	                    {
		                    DateFormat df = new SimpleDateFormat(dateFormat);
		                    df.setTimeZone(aZone);
		                    fieldValue = df.format(date);
	                    }
	            }       
	            break;
	
	            case DataType.TEXT :
	            {
	                if (fieldName.equals(Field.DESCRIPTION) || fieldName.equals(Field.HEADER_DESCRIPTION)) {
	                    break;
	                }
	
	                fieldValue = ActionHelper.formatDescription(fieldValue, sysPrefix, "", aRequest.getRequestId(), aRequest.getMaxActionId(), aMaxEmailActionId, aSearchTextList, aAppendInterface,
	                        aToolTipType, false);
	            }
	                break;
	
	            case DataType.STRING :
	                fieldValue = Utilities.htmlEncode(fieldValue);
	                break;
	            case DataType.TYPE : {
	                    Type type = (Type) aRequest.getObject(field);
	                    
	                    if( type != null )
	                    {
	    	                fieldValue = type.getDisplayName() ;
	                    }
	                }
	            break;
	            case DataType.ATTACHMENTS :
	            {
	            	if( null != actionFiles )
	            	{
	            		Collection<AttachmentInfo> attInfos = (Collection<AttachmentInfo>) aRequest.getObject(field);
	            		if( null == attInfos || attInfos.size() == 0 )
	            			break ;
	            		ArrayList<Integer> fileRepos = new ArrayList<Integer>();
	            		Hashtable<Integer,AttachmentInfo> attachs = new Hashtable<Integer,AttachmentInfo>();
	            		for(Iterator<AttachmentInfo> iterator = attInfos.iterator() ; iterator.hasNext() ; )
	            		{
	            			AttachmentInfo ai = iterator.next();
	            			fileRepos.add(ai.getRepoFileId());
	            			attachs.put(ai.getRepoFileId(), ai	);
	            		}
	            		
	            		Hashtable<Integer,FileRepoIndexObject> repoFiles = APIUtil.getFileRepoIndexObjectList(fileRepos);
	            		Hashtable<Integer,ActionFileInfo> fieldFiles = new Hashtable<Integer,ActionFileInfo>();
	            		for( ActionFileInfo afi : actionFiles )
	            		{
	            			if( afi.getFieldId() == field.getFieldId())
	            				fieldFiles.put(afi.getFileId(),afi);
	            		}
	            		// String attachStr = ActionHelper.formatEmailAttachments(actionAttachments, TBitsConstants.HTML_FORMAT, PATH, sysPrefix, requestId, action.getActionId(), permissions);
	            		fieldValue = formatEmailAttachmentsHeader(fieldFiles,repoFiles , attachs ,aPath, sysPrefix, aRequest.getRequestId(),aRequest.getMaxActionId(),field);//(fieldFiles, TBitsConstants.HTML_FORMAT, aPath, sysPrefix, aRequest.getRequestId(), aRequest.getMaxActionId(), permissions);
	            	}
	            	else
	            		fieldValue = null;
	            }
            }

            if( fieldValue == null )
            	fieldValue = "-";
            aTagTable.put(fieldName, fieldValue);
            aTagTable.put(fieldName + "_label", field.getDisplayName());
        }
    }

    /**
     * This method  fills fixed and extended fields Header with values
     * by replacing <%=field_label%> and <%=field_value%> tags.
     *
     * @param aTagTable      Hashtable collecting <field,value> pairs
     * @param aRequest       Request object
     * @param aPath          the server path
     */
    public static void fillHeaderData(Hashtable<String, String> aTagTable, Request aRequest, String aPath, int aMaxEmailActionId, String aDateFormat, TimeZone aZone,
                                      ArrayList<String> aSearchTextList, int aSystemId, int aAppendInterface, int aToolTipType)
            throws Exception {
        int                    systemId         = aRequest.getSystemId();
        BusinessArea           ba               = BusinessArea.lookupBySystemId(systemId);
        String                 sysPrefix        = ba.getSystemPrefix();
        Collection<RequestUser> list             = null;
        String                 fieldName        = "";
        String                 fieldValue       = "";
        String                 descriptor       = "";
        FieldDescriptor        fd               = null;
        ArrayList<Field>       fieldsList       = Field.lookupBySystemId(systemId);
        String                 statusDescriptor = Field.STATUS;

        fd = FieldDescriptor.getPrimaryDescriptor(systemId, Field.STATUS);

        if (fd != null) {
            statusDescriptor = fd.getDescriptor();
        }

        //Commenting if so that this does not block other context path patterns.
        //if (aPath.equals("") || aPath.equals("/")) {
        String diff = "<span class='l cb' id='diffLabel' onclick=" + "javascript:replaceSummary()>" + "  [diff to previous]" + "</span>";
        aTagTable.put("diff", diff);
        //}

        for (Field field : fieldsList) {
            fieldName  = field.getName();
            fieldValue = aRequest.get(fieldName);

            if (fieldName.equals(Field.PARENT_REQUEST_ID)) {
                Hashtable<String, String> parentRequests = Request.getParentRequests(aRequest.getSystemId(),aRequest.getRequestId());
                String                    parentSubject  = parentRequests.get(sysPrefix + "#" + aRequest.getRequestId());

                if (parentSubject != null) {
                    if (aPath.equals("") == true) {
                        String subject = new StringBuilder().append("onmouseover=\"")
                        .append("this.T_HIDE_ONMOUSEOUT='true';").append("return escape('")
                        .append(ActionHelper.untaintForHtml(parentSubject)).append("');\"").toString();

                        aTagTable.put("parent_subject", subject);
                    } else {
                        String subject = "title='" + parentSubject + "'";

                        aTagTable.put("parent_subject", subject);
                    }
                } else {
                    aTagTable.put("parent_subject", "");
                }
            }

            String priv = "";

            if (field.getDataTypeId() == DataType.TYPE) {
                Type t = Type.lookupBySystemIdAndFieldNameAndTypeName(aSystemId, fieldName, fieldValue);
                
                if( t != null )
                {
	                fieldValue = t.getDisplayName() ;
	                if ( t.getIsPrivate() == true) {
	                    priv = t.getIsPrivate()
	                           ? "&dagger;"
	                           : "";
	                }
                }
            }

            if ((fieldValue != null) && (!fieldValue.trim().equals(""))) {
                if ((field.getPermission() & Permission.HYPERLINK) != 0) {
                    fd = FieldDescriptor.getPrimaryDescriptor(systemId, field.getName());

                    if (fd != null) {
                        descriptor = fd.getDescriptor();
                    } else {
                        descriptor = field.getName();
                    }

                    fieldValue = "<a class=\"l cb\" href=\"" + aPath + "/search/" + sysPrefix + "?q=" + descriptor + ":&quot;" + fieldValue + "&quot;" + "+-" + statusDescriptor + ":closed&sv=2"
                                 + " \"target=\"_blank\">" + fieldValue + priv + "</a>";
                }
            } else {
                fieldValue = "-";
            }

            switch (field.getDataTypeId()) {
            case DataType.USERTYPE :
            	if(field.getIsExtended()){
                  list=aRequest.getExUserType(field);
            	}
            	// assuming that cc subscribers to are not shown in the header.
            	else {
            		if (fieldName.equals(Field.ASSIGNEE)) {
            			list = aRequest.getAssignees();
            		} else {
            			list = aRequest.getLoggers();
            		}
            	}
            	fieldValue = ActionHelper.getUserFieldHtml(list, field, ba, aPath, statusDescriptor);

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
                    Date t = (Date) aRequest.getObject(fieldName);
                    DateFormat df = new SimpleDateFormat(dateFormat);
                    df.setTimeZone(aZone);
                    fieldValue = df.format(t);
                    
                }

                break;

            case DataType.TEXT :
                if (fieldName.equals(Field.DESCRIPTION) || fieldName.equals(Field.HEADER_DESCRIPTION)) {
                    break;
                }

                fieldValue = ActionHelper.formatDescription(fieldValue, sysPrefix, "", aRequest.getRequestId(), aRequest.getMaxActionId(), aMaxEmailActionId, aSearchTextList, aAppendInterface,
                        aToolTipType, false);

                break;

            case DataType.STRING :
                fieldValue = Utilities.htmlEncode(fieldValue);
            }

            aTagTable.put(fieldName, fieldValue);
            aTagTable.put(fieldName + "_label", field.getDisplayName());
            // Niti : we should not treat Office as special
            if (fieldName.equalsIgnoreCase(Field.OFFICE)) {
                Type type = Type.getDefaultTypeBySystemIdAndFieldName(aSystemId, Field.OFFICE);

                if (fieldValue.equalsIgnoreCase(type.getDisplayName())) {
                    aTagTable.put(fieldName, "");
                    aTagTable.put(fieldName + "_display", "none");
                }
            }
        }
    }

    /**
     * This method fills the parent header Container if parent_id > 0
     *
     * @param aTagTable   Hashtable collecting <field,value> pairs
     * @param aRequest    Request object
     * @param aPath       Server path
     */
    public static void fillParentHeaderContainer(Hashtable<String, String> aTagTable, Request aRequest, String aPath) {

        //
        // Insert Parent Id Table if, parent > 0.
        //
        if (aRequest.getParentRequestId() > 0) {
            aTagTable.put("parent", PARENT_REQUEST_HTML.replace("<=path>", aPath));
        } else {
            aTagTable.put("parent", "");
        }

        return;
    }

    /**
     * This method adds severity and over due symbols.
     *
     * @param aTagTable      Hashtable collecting <field,value> pairs
     * @param aRequest       Request object
     */
    public static void fillSymbols(Hashtable<String, String> aTagTable, String aIMG_PATH, Request aRequest, boolean aSource) {
        StringBuilder sb = new StringBuilder();

        if (aTagTable.get("privateSymbol") != null) {
            sb.append(aTagTable.get("privateSymbol"));
        }

        String severity    = aRequest.getSeverityId().getName();
        String displayName = aRequest.getSeverityId().getDisplayName();
        String imageName   = "";
        String Tag         = "<td align=\"left\" width=\"16\">" + "<img class=\"at\" alt=\"<=alt>\" src=\"" + aIMG_PATH + "<=image>\"></td>";
        String emailTag    = "<td align=\"left\" width=\"16\">" + "<=htmlCode>" + "</td>";

        // String highSeverity = "<H1><font color:red size:4 >" +
        // "<font size='4'><strong>!&nbsp;</strong></H1>";
//        String highSeverity   = "<H1 style=\"font:bold 1.8em times new roman;" + "color:red ; margin :-6px 3px -8px 0px;\">!</H1>";
//        String lowSeverity    = "<font size='4'><strong>&darr;</strong>";
//        String verLowSeverity = "<font size='4' color='darkgray'><strong>&darr;</strong>";
        String overDue        = "<font face='MS Outlook'><font size='4'>" + "<strong>A</strong>&nbsp;";
//        String critical       = "<div style=\"display:inline;color:#FFCC66;" + "font-family:Wingdings;font-weight:bold;margin-top:2px;" + " font-size:24px;\">u&nbsp;</div>"
//                                + "<div style=\"display:inline;color:#FF0000;font-size:21px;" + "font-weight:bold;margin-left:-40px;text-align:top;\">" + "!&nbsp;&nbsp;</div>";

//        if (severity.equalsIgnoreCase("medium") == false) {
//            if (aSource == false) {
//                String fileName = "web/images/" + severity.toLowerCase() + "_big.gif";
//                File   file     = null;
//
//                try {
//                    file = Configuration.findPath(fileName);
//
//                    if (file != null) {
//                        imageName = severity.toLowerCase() + "_big.gif";
//                        sb.append(Tag.replace("<=image>", imageName).replace("<=alt>", displayName));
//                    }
//                } catch (Exception e) {
//
//                    /*
//                     * File is not found. Let us not render the image.
//                     */
//                }
//            } else {
//                if (severity.equalsIgnoreCase("high")) {
//                    sb.append(emailTag.replace("<=htmlCode>", highSeverity));
//                }
//
//                if (severity.equalsIgnoreCase("critical")) {
//                    sb.append(emailTag.replace("<=htmlCode>", critical));
//                }
//
//                if (severity.equalsIgnoreCase("low")) {
//                    sb.append(emailTag.replace("<=htmlCode>", lowSeverity));
//                }
//
//                if (severity.equalsIgnoreCase("very_low") || severity.equalsIgnoreCase("very low") || severity.equalsIgnoreCase("very-low")) {
//                    sb.append(emailTag.replace("<=htmlCode>", verLowSeverity));
//                }
//            }
//        }

        Date dueDate = aRequest.getDueDate();

        if (dueDate != null) {
            String status = aRequest.getStatusId().getName();

            if ((dueDate.getTime() < Timestamp.getGMTNow().getTime()) && (status.equals("closed") == false)) {
                if (aSource == false) {
                    sb.append(Tag.replace("<=image>", "alarm_big.gif").replace("<=alt>", "Over Due"));
                } else {
                    sb.append(emailTag.replace("<=htmlCode>", overDue));
                }
            }
        }

        aTagTable.put("symbols", sb.toString());
    }

    /**
     * This method prepares the header corresponding to the given field in the
     * given business area.
     *
     * @param aSystemId         BA Id.
     * @param aFieldName        Name of the User field.
     * @param aTable            Table that has added/delete/common entries.
     * @param aHeaderDesc       Outparam to hold the header for this field.
     * @param aFormat           Format of header text/html.
     * @param aLineBreak        Line break based on the format.
     */
    private static void formUserHeader(int aSystemId, String aFieldName, Hashtable<String, String> aTable, StringBuilder aHeaderDesc, int aFormat, String aLineBreak) {
        ArrayList<UserHeaderRecord> recList = new ArrayList<UserHeaderRecord>();
        Field                       field   = null;

        try {
            field = Field.lookupBySystemIdAndFieldName(aSystemId, aFieldName);
        } catch (DatabaseException de) {
            LOG.warn("",(de));

            return;
        }

        // Check for common list.
        String commonList = aTable.get("*" + aFieldName);

        if ((commonList != null) && (commonList.trim().equals("") == false)) {
            processUserHeader(recList, commonList, UserHeaderRecord.EntryType.COMMON);
        }

        // Check for added list.
        String addedList = aTable.get("+" + aFieldName);

        if ((addedList != null) && (addedList.trim().equals("") == false)) {
            processUserHeader(recList, addedList, UserHeaderRecord.EntryType.ADDED);
        }

        /*
         * In DFlow#2726#7, Peter wanted us to sort the existing and the
         * newly added entries to be sorted based on their ordering but
         * display the deleted entries at the end.
         *
         * So let us sort the recList before adding the deleted entries.
         */
        UserHeaderRecord.setSortParams(UserHeaderRecord.ORDERING, ASC_ORDER);
        recList = UserHeaderRecord.sort(recList);

        // Check for deleted list.
        String deletedList = aTable.get("-" + aFieldName);

        if ((deletedList != null) && (deletedList.trim().equals("") == false)) {
            processUserHeader(recList, deletedList, UserHeaderRecord.EntryType.DELETED);
        }

        if (recList.size() == 0) {
            return;
        }

        String        displayName   = field.getDisplayName();
        StringBuilder formattedList = new StringBuilder();
        boolean       first         = true;

        if (aFormat == HTML_FORMAT) {
            for (UserHeaderRecord record : recList) {
                String userLogin = record.getUserLogin();

                userLogin = userLogin.replace(".transbittech.com", "");

                if (first == false) {
                    formattedList.append(", ");
                } else {
                    first = false;
                }

                switch (record.getEntryType()) {
                case COMMON :
                    formattedList.append(userLogin);

                    break;

                case ADDED :
                    formattedList.append("<b>").append(userLogin).append("</b>");

                    break;

                case DELETED :
                    formattedList.append("<s>").append(userLogin).append("</s>");

                    break;
                }
            }
        } else {
            for (UserHeaderRecord record : recList) {
                String userLogin = record.getUserLogin();

                userLogin = userLogin.replace(".transbittech.com", "");

                if (first == false) {
                    formattedList.append(", ");
                } else {
                    first = false;
                }

                switch (record.getEntryType()) {
                case COMMON :
                    formattedList.append(userLogin);

                    break;

                case ADDED :
                    formattedList.append("+").append(userLogin);

                    break;

                case DELETED :
                    formattedList.append("-").append(userLogin);

                    break;
                }
            }
        }

        aHeaderDesc.append("[ ").append(displayName).append(": ").append(formattedList).append(" ]").append(aLineBreak);
    }

    /**
     * This method formats the Update Log to be displayed in request details.
     *
     * @param aSystemId
     * @param aActionLog
     * @param aActPermTable
     * @param aFormat
     * @param aReportError
     * @param aSystemPrefix
     * @param aServer
     * @param aRequestId
     * @param aActionId
     * @param aMaxEmailActionId
     * @param aAppendInterface
     * @param aToolTipType
     *
     * @return Formatted version of the update log.
     */
    public static String formatActionLog(int aSystemId, String aActionLog, Hashtable<String, Integer> aActPermTable, int aFormat, boolean aReportError, String aSystemPrefix, String aServer,
            int aRequestId, int aActionId, int aMaxEmailActionId, int aAppendInterface, int aToolTipType) {
        boolean noPermission = false;
        String  LINE_BREAK   = TEXT_LINE_BREAK;

        if (aFormat == TBitsConstants.HTML_FORMAT) {
            LINE_BREAK = HTML_LINE_BREAK;
        }

        StringBuilder     headerDesc = new StringBuilder();
        ArrayList<String> hdrLogList = Utilities.toArrayList(aActionLog, "\n");
        int               size       = hdrLogList.size();

        try {
            for (int i = 0; i < size; i++) {
                String aLine = hdrLogList.get(i);

                if (aFormat == TBitsConstants.HTML_FORMAT) {
                    aLine = Utilities.htmlEncode(aLine);
                    aLine = LinkFormatter.hyperSmartLinks(aLine, aSystemPrefix + "#" + aRequestId, aActionId, aMaxEmailActionId, aServer, aAppendInterface, aToolTipType);
                }

                if (aLine.indexOf("##") < 0) {
                    headerDesc.append(aLine).append(LINE_BREAK);

                    continue;
                }

                // Header is of format <FieldName>##<FieldId>##trackingInfo
                int    index     = aLine.indexOf("##");
                String fieldName = aLine.substring(0, index).trim();

                aLine = aLine.substring(index + 2).trim();
                index = aLine.indexOf("##");

                String fieldId = aLine.substring(0, index).trim();
                String desc    = aLine.substring(index + 2).trim();

                // patch to hyperlink parent Id in action log
                if (fieldName.equals(Field.PARENT_REQUEST_ID) == true) {

                    // Check if aServer ends with a slash.
                    if (aServer.endsWith("/") == false) {
                        aServer = aServer + "/";
                    }

                    desc = desc.replaceAll("'([1-9][0-9]*)'", "<a class='l cb' href='" + aServer + "q/" + aSystemPrefix + "/$1' target='" + aSystemPrefix + "_" + "$1'>'$1'</a>");
                }

                /*
                 * Check if this fieldName starts with +/-/*. Such entries
                 * corresponding to the logs for user fields and have to be
                 * dealt with separately.
                 */
                else if (fieldName.startsWith("*") || fieldName.startsWith("+") || fieldName.startsWith("-")) {
                    String actFieldName = fieldName.substring(1);

                    /*
                     * Skip if user does not have permssion to view this field.
                     */
//                    if (hasPermissions(aActPermTable, actFieldName) == false) {
//                        continue;
//                    }
                 //   LOG.info("Nitiraj : formating action log for the fieldId = " + fieldId + " : fieldName = " + fieldName );
                    // Skip if user does not have permssion to view this field.
                    if( aAppendInterface == TBitsConstants.SOURCE_WEB &&  hasPermissions(aActPermTable, actFieldName) == false )
                    {
                    	continue;	             
                    }
                    else if( aAppendInterface == TBitsConstants.SOURCE_EMAIL && ( (getPerm(aActPermTable,actFieldName) & Permission.EMAIL_VIEW ) == 0 ) )
                    {
                    	continue ;
                    }

                    Hashtable<String, String> eTable = new Hashtable<String, String>();

                    eTable.put(fieldName, desc);
                    i = i + 1;

                    while (i < size) {

                        /*
                         * Check if the next one starts with +/-/*.
                         */
                        aLine = hdrLogList.get(i);

                        if (aLine.startsWith("*") || aLine.startsWith("+") || aLine.startsWith("-")) {
                            index     = aLine.indexOf("##");
                            fieldName = aLine.substring(0, index).trim();

                            String curFieldName = fieldName.substring(1);

                            LOG.info("Field: " + curFieldName + "  " + "Actual: " + actFieldName);

                            /*
                             * Check if we are dealing with the same field
                             * as actFieldName.
                             */
                            if (curFieldName.equals(actFieldName) == false) {

                                /*
                                 * We are dealing with a different user field.
                                 * This means we are done with processing log
                                 * records corresponding to this user field.
                                 * Its time to come out of the loop and generate
                                 * final log record for this user field. Before
                                 * that put back the item read, i.e. decrement
                                 * the counter and come out of the loop.
                                 */
                                i = i - 1;

                                break;
                            }

                            /*
                             * This log record corresponds to the same field
                             * as actFieldName. Put this in the table and
                             * check the next one, i.e. increment the counter.
                             */
                            aLine   = aLine.substring(index + 2).trim();
                            index   = aLine.indexOf("##");
                            fieldId = aLine.substring(0, index).trim();
                            desc    = aLine.substring(index + 2).trim();
                            eTable.put(fieldName, desc);
                            i = i + 1;
                        } else {

                            /*
                             * We are no more dealing with user fields.
                             * Put back the item read, i.e. decrement the
                             * counter and come out of the loop.
                             */
                            i = i - 1;

                            break;
                        }
                    }

                    /*
                     * Form the header description for this user field.
                     */
                    formUserHeader(aSystemId, actFieldName, eTable, headerDesc, aFormat, LINE_BREAK);

                    continue;
                }

                headerDesc.append(desc).append(LINE_BREAK);
            }

            if ((noPermission == true) && (aReportError == true)) {
                headerDesc.append(Messages.getMessage("HEADER_NOT_INCLUDED")).append(LINE_BREAK);
            }
        } catch (Exception e) {
            LOG.severe("",(e));

            return "";
        }

        return headerDesc.toString();
    }

    // this is same as formatAttachments but we also pass permissions and add only those attachments
    // in email which have Permission.EMAIL_VIEW set
    public static String formatEmailAttachments(Collection<ActionFileInfo> attachmentList, int aFormat, 
    		String aPath, String aSysPrefix, int requestId, int actionId, Hashtable<String,Integer> permissions) 
    		throws DatabaseException {
        if((attachmentList == null) || (attachmentList.size() == 0))
        	return "";
        
    	String                LINE_BREAK     = TEXT_LINE_BREAK;
        String                CLASS          = "";

        if (aFormat == TBitsConstants.HTML_FORMAT) {
            LINE_BREAK = HTML_LINE_BREAK;
            CLASS      = "class=\"l cb\"";
        } else if (aFormat == TBitsConstants.TEXT_FORMAT) {
            LINE_BREAK = TEXT_LINE_BREAK;
        }

        

        StringBuilder sbf = new StringBuilder();

        Comparator<ActionFileInfo> fieldComp = new Comparator<ActionFileInfo>()
        {
			public int compare(ActionFileInfo arg0, ActionFileInfo arg1) 
			{
				if( arg0.getFieldId() < arg1.getFieldId() )
					return -1 ;
				else if( arg0.getFieldId() > arg1.getFieldId() )
					return 1 ;
				
				return 0;	
			}
        	
        };
        
        Collections.sort((List<ActionFileInfo>)attachmentList, fieldComp );
        
        BusinessArea ba = BusinessArea.lookupBySystemPrefix(aSysPrefix);
        for (ActionFileInfo attachment : attachmentList) 
        {
        	Field f = Field.lookupBySystemIdAndFieldId(ba.getSystemId(), attachment.getFieldId());
        	String fieldName = f.getName() ;
        	Integer perm = permissions.get(fieldName) ;
        	if( null == perm )
        		perm = 0 ;
        	if( ( perm & Permission.EMAIL_VIEW ) == 0  )
        		continue ;
        	
        	String size = "";
        	int attSize = attachment.getSize();
        	
        	if (attSize >= (1024*1024))
        		size = "[ " + String.format("%.0f", ((float)attSize)/(1024*1024)) + " MB ]";
        	else if (attSize < (1024*1024) && attSize >= 1024)
        		size = "[ " + String.format("%.0f", ((float)attSize)/1024) + " KB ]";        	
        	else if (attSize < 1024)
        		size = "[ " + attSize + " B ]";
        	
//            String conversion  = (attachment.getIsConverted() == true)
//                                 ? " (converted from .bmp)"
//                                 : "";
//            String extraction  = (attachment.getIsExtracted() == true)
//                                 ? " (extracted from .msg)"
//                                 : "";
            String encodedName = "";

            try {
                encodedName = URLEncoder.encode(attachment.getName(), "UTF-8");
            } catch (UnsupportedEncodingException uee) {
                LOG.severe("",(uee));
            }

            String httpLink = aPath + "read-attachment/" + aSysPrefix + "?" + "request_id=" + requestId + "&request_file_id=" + attachment.getRequestFileId() +"&field_id=" + attachment.getFieldId() + "&action_id=" + actionId;
            if(attachment.isAnonymousDownload())
            	httpLink = aPath + "download-file/" + aSysPrefix + "?" + "filerepoid=" + attachment.getFileId() + "&hash=" + attachment.getHash() +"&securitycode=" + attachment.getSecurityCode();
            String saveLink = "<A " + CLASS + " HREF='" + httpLink + "&saveAs=true" + "' title='Hash : " + attachment.getHash() + "'>Save..." + "</A>&nbsp;";
            
//AutoDesk FreeWheel  Integration by Nirmal Agarwal on 29-12-11
            
            
            String httpFreeWheelLink=aPath + "download-file/" + aSysPrefix + "?" + "filerepoid=" + attachment.getFileId() + "&hash=" + attachment.getHash() +"&securitycode=" + attachment.getSecurityCode();
            
            try {
				httpFreeWheelLink = URLEncoder.encode(httpFreeWheelLink, "UTF-8");
			} catch (UnsupportedEncodingException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			
			String freeWheelUrl = "";
			boolean isFreeWheelEnable = false;
			String freewheelProperty = "false";
			boolean isAutodeskEnable = false;
			String freeWheelUrlProperty="";
			
			 try {
	            	freewheelProperty = PropertiesHandler.getProperty(TBitsPropEnum.IS_FREEWHEEL_ENABLED);
	            	freeWheelUrlProperty=PropertiesHandler.getProperty(TBitsPropEnum.FREEWHEEL_URL);
	                if(freewheelProperty != null && freewheelProperty.trim().equalsIgnoreCase("true") == true) {
	                	isAutodeskEnable = true;
	                	}
	                if(freeWheelUrlProperty != null && !freeWheelUrlProperty.trim().equals(""))
	                {
	                	freeWheelUrl=freeWheelUrlProperty.toString();
	                }
	                else
	                {
	                	isAutodeskEnable=false;
	                }
	                	
	                }
	            	
	            catch(IllegalArgumentException e) {
	            	LOG.severe(e.toString());
	            }	
			
            String FreeWheelLink="<A "  + CLASS + " TARGET=\"BLANK\" HREF=\""+ freeWheelUrl +"?path="+ httpFreeWheelLink +"\">[View in FreeWheel...]" + "</A>&nbsp;";
            
           
            String fileName=attachment.getName().trim();
            
            if( fileName.toUpperCase().endsWith(".DWF") || fileName.toUpperCase().endsWith(".DWFX"))
            isFreeWheelEnable=true;
           
         //AutoDesk FreeWheel  Integration 
           
           
            //AutoVue Added By Abhishek on 21 May
               String autovueProperty = new String();
               boolean isAutovueEnabled = false;
               
            try {
            	autovueProperty = PropertiesHandler.getProperty(IS_AUTOVUE_ENABLED);
                if(autovueProperty.trim().equalsIgnoreCase("true") == true) {
                	isAutovueEnabled = true;
                	}
            	}
            catch(IllegalArgumentException e) {
            	LOG.severe(e.toString());
            }	
            
            String autoVueHttpParams = "request_id=" + requestId + "&request_file_id=" + attachment.getRequestFileId() +"&field_id=" + attachment.getFieldId();
            String autoVueLink=  "<A "  + CLASS + " TARGET=\"BLANK\" HREF='" + aPath + "open-attachment/" + aSysPrefix + "?" + autoVueHttpParams + "'>[View in Browser...]" + "</A>&nbsp;";
                        
            sbf.append("[ " + f.getDisplayName() + " ] ");
        	if(attachment.getFileAction().equals(WebdavConstants.FILE_ADDED))
        	{
        		sbf.append("Added ");
        	}
        	else if(attachment.getFileAction().equals(WebdavConstants.FILE_MODIFIED))
        	{
        		sbf.append("Modified ");
        	}else if(attachment.getFileAction().equals(WebdavConstants.FILE_DELETED))
        	{
        		sbf.append("Deleted ");
        	}
        	
            // Start the Anchor element for HTML format.            
            if (aFormat == TBitsConstants.HTML_FORMAT) {            	
                sbf.append("<a " + CLASS + " href='" + httpLink + "'  title='Hash : " + attachment.getHash() + "' target='_blank'>");
            }

            sbf.append(attachment.getName());

            // Close the Anchor element for HTML format.
            if (aFormat == HTML_FORMAT) {
                sbf.append("</a>");
            }

            sbf.append(" " + size);
            //sbf.append(conversion).append(extraction);

            if (aFormat == HTML_FORMAT) {
                sbf.append("&nbsp;&nbsp;").append(saveLink);
            }
            
            if(isFreeWheelEnable==true && isAutodeskEnable==true)
            {
            sbf.append("&nbsp;&nbsp;").append(FreeWheelLink);
            }
            
            if (aFormat == HTML_FORMAT && isAutovueEnabled == true) {
                sbf.append("&nbsp;&nbsp;").append(autoVueLink);
            }
            
            
            
            if ((aFormat == TEXT_FORMAT) && (aPath.trim().equals("") == false)) {
                sbf.append(" <").append(httpLink).append(">");
            }

            if( aFormat == HTML_FORMAT && attachment.isAttached() == true )
            {
            	sbf.append("&nbsp;&nbsp;[attached]");
            }
            
            if( aFormat == TEXT_FORMAT && attachment.isAttached() == true )
            {
            	sbf.append("  [attached]");
            }
            
            sbf.append(LINE_BREAK);
        }

        return sbf.toString();
    }
    
    public static String formatEmailAttachmentsHeader(Hashtable<Integer,ActionFileInfo> actionFileList, Hashtable<Integer,FileRepoIndexObject> repoFiles,Hashtable<Integer,AttachmentInfo> attachs, String aPath, String aSysPrefix, int requestId, int actionId, Field field) 
    		throws DatabaseException {
        if((actionFileList == null) || (actionFileList.size() == 0))
        	return "";
        
    	String                LINE_BREAK     = HTML_LINE_BREAK;
        String                CLASS          = "";

        StringBuilder sbf = new StringBuilder();

        for (FileRepoIndexObject attachment : repoFiles.values() ) 
        {
        	String size = "";
        	long attSize = attachment.getSize();
        	
        	if (attSize >= (1024*1024))
        		size = "[ " + String.format("%.0f", ((float)attSize)/(1024*1024)) + " MB ]";
        	else if (attSize < (1024*1024) && attSize >= 1024)
        		size = "[ " + String.format("%.0f", ((float)attSize)/1024) + " KB ]";        	
        	else if (attSize < 1024)
        		size = "[ " + attSize + " B ]";
        	
            String encodedName = "";

            try {
                encodedName = URLEncoder.encode(attachment.getName(), "UTF-8");
            } catch (UnsupportedEncodingException uee) {
                LOG.severe("",(uee));
            }

            ActionFileInfo afi = actionFileList.get(attachment.getId());
            AttachmentInfo ai = attachs.get(attachment.getId());
            String httpLink = aPath + "read-attachment/" + aSysPrefix + "?" + "request_id=" + requestId + "&request_file_id=" + ai.getRequestFileId() +"&field_id=" + field.getFieldId() + "&action_id=" + actionId;
            if(null != afi && afi.isAnonymousDownload())
            	httpLink = aPath + "download-file/" + aSysPrefix + "?" + "filerepoid=" + attachment.getId() + "&hash=" + attachment.getHash() +"&securitycode=" + attachment.getSecurityCode();
            String saveLink = "<A " + CLASS + " HREF='" + httpLink + "&saveAs=true" + "' title='Hash : " + attachment.getHash() + "'>Save..." + "</A>&nbsp;";
            
			String freeWheelUrl = "";
			boolean isFreeWheelEnable = false;
			String freewheelProperty = "false";
			boolean isAutodeskEnable = false;
			String freeWheelUrlProperty="";
			
			 try {
	            	freewheelProperty = PropertiesHandler.getProperty(TBitsPropEnum.IS_FREEWHEEL_ENABLED);
	            	freeWheelUrlProperty=PropertiesHandler.getProperty(TBitsPropEnum.FREEWHEEL_URL);
	                if(freewheelProperty != null && freewheelProperty.trim().equalsIgnoreCase("true") == true) {
	                	isAutodeskEnable = true;
	                	}
	                if(freeWheelUrlProperty != null && !freeWheelUrlProperty.trim().equals(""))
	                {
	                	freeWheelUrl=freeWheelUrlProperty.toString();
	                }
	                else
	                {
	                	isAutodeskEnable=false;
	                }
	                	
	                }
	            	
	            catch(Exception e) {
	            	LOG.severe(e.toString());
	            }	
			
            
            
           
            String fileName=attachment.getName().trim();
            
            if( fileName.toUpperCase().endsWith(".DWF") || fileName.toUpperCase().endsWith(".DWFX"))
            isFreeWheelEnable=true;
         //AutoDesk FreeWheel  Integration 
            
            //AutoVue Added By Abhishek on 21 May
           String autovueProperty = new String();
           boolean isAutovueEnabled = false;
               
            try {
            	autovueProperty = PropertiesHandler.getProperty(IS_AUTOVUE_ENABLED);
                if(autovueProperty.trim().equalsIgnoreCase("true") == true) {
                	isAutovueEnabled = true;
                	}
            	}
            catch(IllegalArgumentException e) {
            	LOG.severe(e.toString());
            }	
            
            
            
            
            
            String autoVueHttpParams = "request_id=" + requestId + "&request_file_id=" + ai.getRequestFileId() +"&field_id=" + field.getFieldId();
            String autoVueLink=  "<A "  + CLASS + " TARGET=\"BLANK\" HREF='" + aPath + "open-attachment/" + aSysPrefix + "?" + autoVueHttpParams + "'>[View in Browser...]" + "</A>&nbsp;";
                
            if(null != afi )
            {
	            if( afi.getFileAction().equals(WebdavConstants.FILE_DELETED))
	            	sbf.append("[Deleted] ");
	            else if ( afi.getFileAction().equals(WebdavConstants.FILE_ADDED))
	            	sbf.append("[Added] ");
	            else if ( afi.getFileAction().equals(WebdavConstants.FILE_MODIFIED))
	            	sbf.append("[Modified] ");
            }
            
            // Start the Anchor element for HTML format.            
            sbf.append("<a " + CLASS + " href='" + httpLink + "'  title='Hash : " + attachment.getHash() + "' target='_blank'>");

            sbf.append(attachment.getName());

            // Close the Anchor element for HTML format.
            sbf.append("</a>");

            sbf.append(" " + size);
            //sbf.append(conversion).append(extraction);

           sbf.append("&nbsp;&nbsp;").append(saveLink);
           
           if(isFreeWheelEnable==true && isAutodeskEnable==true)
           {
	            try {
	            	 String httpFreeWheelLink = null;
	                 if( null != afi )
	                 	httpFreeWheelLink=aPath + "download-file/" + aSysPrefix + "?" + "filerepoid=" + afi.getFileId() + "&hash=" + attachment.getHash() +"&securitycode=" + attachment.getSecurityCode();
	                 
	   				httpFreeWheelLink = URLEncoder.encode(httpFreeWheelLink, "UTF-8");
	   			   String freeWheelLink="<A "  + CLASS + " TARGET=\"BLANK\" HREF=\""+ freeWheelUrl +"?path="+ httpFreeWheelLink +"\">[View in FreeWheel...]" + "</A>&nbsp;";
	   	           sbf.append("&nbsp;&nbsp;").append(freeWheelLink);
	   			} catch (Exception e1) {
	   				LOG.error(e1.getMessage(), e1);
	   			}
           }
            
            if (isAutovueEnabled == true) {
                sbf.append("&nbsp;&nbsp;").append(autoVueLink);
            }
            
            if( null != afi && afi.isAttached() == true )
            {
            	sbf.append("&nbsp;&nbsp;[attached]");
            }
            sbf.append(LINE_BREAK);
        }

        return sbf.toString();
    }

    
    /**
     * @throws DatabaseException 
     */
    public static String formatAttachments(Collection<ActionFileInfo> attachmentList, int aFormat, String aPath, String aSysPrefix, int requestId, int actionId) throws DatabaseException {
        if((attachmentList == null) || (attachmentList.size() == 0))
        	return "";
        
    	String                LINE_BREAK     = TEXT_LINE_BREAK;
        String                CLASS          = "";

        if (aFormat == TBitsConstants.HTML_FORMAT) {
            LINE_BREAK = HTML_LINE_BREAK;
            CLASS      = "class=\"l cb\"";
        } else if (aFormat == TBitsConstants.TEXT_FORMAT) {
            LINE_BREAK = TEXT_LINE_BREAK;
        }

        

        StringBuilder sbf = new StringBuilder();

        BusinessArea ba = BusinessArea.lookupBySystemPrefix(aSysPrefix);
        for (ActionFileInfo attachment : attachmentList) {
        	String size = "";
        	int attSize = attachment.getSize();
        	
        	if (attSize >= (1024*1024))
        		size = "[ " + String.format("%.0f", ((float)attSize)/(1024*1024)) + " MB ]";
        	else if (attSize < (1024*1024) && attSize >= 1024 )
        		size = "[ " + String.format("%.0f", ((float)attSize)/1024) + " KB ]";        	
        	else if (attSize < 1024)
        		size = "[ " + attSize + " B ]";
        	
//            String conversion  = (attachment.getIsConverted() == true)
//                                 ? " (converted from .bmp)"
//                                 : "";
//            String extraction  = (attachment.getIsExtracted() == true)
//                                 ? " (extracted from .msg)"
//                                 : "";
            String encodedName = "";

            try {
                encodedName = URLEncoder.encode(attachment.getName(), "UTF-8");
            } catch (UnsupportedEncodingException uee) {
                LOG.severe("",(uee));
            }

            String httpLink = aPath + "read-attachment/" + aSysPrefix + "?" + "request_id=" + requestId + "&request_file_id=" + attachment.getRequestFileId() +"&field_id=" + attachment.getFieldId() + "&action_id=" + actionId;
            String saveLink = "<A " + CLASS + " HREF='" + httpLink + "&saveAs=true" + "' title='Hash : " + attachment.getHash() + "'>Save..." + "</A>&nbsp;";
            
//AutoDesk FreeWheel  Integration by Nirmal Agarwal on 29-12-11
            
            String httpFreeWheelLink=aPath + "download-file/" + aSysPrefix + "?" + "filerepoid=" + attachment.getFileId() + "&hash=" + attachment.getHash() +"&securitycode=" + attachment.getSecurityCode();
            
            try {
				httpFreeWheelLink = URLEncoder.encode(httpFreeWheelLink, "UTF-8");
			} catch (UnsupportedEncodingException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			
			String freeWheelUrl = "";
			boolean isFreeWheelEnable = false;
			String freewheelProperty = "false";
			boolean isAutodeskEnable = false;
			String freeWheelUrlProperty="";
			
			 try {
	            	freewheelProperty = PropertiesHandler.getProperty(TBitsPropEnum.IS_FREEWHEEL_ENABLED);
	            	freeWheelUrlProperty=PropertiesHandler.getProperty(TBitsPropEnum.FREEWHEEL_URL);
	                if(freewheelProperty != null && freewheelProperty.trim().equalsIgnoreCase("true") == true) {
	                	isAutodeskEnable = true;
	                	}
	                if(freeWheelUrlProperty != null && !freeWheelUrlProperty.trim().equals(""))
	                {
	                	freeWheelUrl=freeWheelUrlProperty.toString();
	                }
	                else
	                {
	                	isAutodeskEnable=false;
	                }
	                	
	                }
	            	
	            catch(IllegalArgumentException e) {
	            	LOG.severe(e.toString());
	            }	
			
            String FreeWheelLink="<A "  + CLASS + " TARGET=\"BLANK\" HREF=\""+ freeWheelUrl +"?path="+ httpFreeWheelLink +"\">[View in FreeWheel...]" + "</A>&nbsp;";
            
           
            String fileName=attachment.getName().trim();
            
            if( fileName.toUpperCase().endsWith(".DWF") || fileName.toUpperCase().endsWith(".DWFX"))
            isFreeWheelEnable=true;
            
         //AutoDesk FreeWheel  Integration 
            
            //AutoVue Added By Abhishek on 21 May
               String autovueProperty = new String();
               boolean isAutovueEnabled = false;
               
            try {
            	autovueProperty = PropertiesHandler.getProperty(IS_AUTOVUE_ENABLED);
                if(autovueProperty.trim().equalsIgnoreCase("true") == true) {
                	isAutovueEnabled = true;
                	}
            	}
            catch(IllegalArgumentException e) {
            	LOG.severe(e.toString());
            }	
            
            //Freewheel integration
            

            
            String autoVueHttpParams = "request_id=" + requestId + "&request_file_id=" + attachment.getRequestFileId() +"&field_id=" + attachment.getFieldId() + "&action_id=" + actionId;;
            String autoVueLink=  "<A "  + CLASS + " TARGET=\"BLANK\" HREF='" + aPath + "open-attachment/" + aSysPrefix + "?" + autoVueHttpParams + "'>[View in Browser...]" + "</A>&nbsp;";
            
            Field f = Field.lookupBySystemIdAndFieldId(ba.getSystemId(), attachment.getFieldId());
            // Start the Anchor element for HTML format.
            if (aFormat == TBitsConstants.HTML_FORMAT) {
            	sbf.append("\n [ " + f.getDisplayName() + " ] ");
            	if(attachment.getFileAction().equals(WebdavConstants.FILE_ADDED))
            	{
            		sbf.append("Added ");
            	}
            	else if(attachment.getFileAction().equals(WebdavConstants.FILE_MODIFIED))
            	{
            		sbf.append("Modified ");
            	}else if(attachment.getFileAction().equals(WebdavConstants.FILE_DELETED))
            	{
            		sbf.append("Deleted ");
            	}
                sbf.append("\n" + "<a " + CLASS + " href='" + httpLink + "' title='Hash : " + attachment.getHash() + "' target='_blank'>");
            }

            sbf.append(attachment.getName());

            // Close the Anchor element for HTML format.
            if (aFormat == HTML_FORMAT) {
                sbf.append("</a>");
            }

            sbf.append(" " + size);
            //sbf.append(conversion).append(extraction);

            if (aFormat == HTML_FORMAT) {
                sbf.append("&nbsp;&nbsp;").append(saveLink);
            }
            
            if(isFreeWheelEnable==true && isAutodeskEnable==true)
            {
            sbf.append("&nbsp;&nbsp;").append(FreeWheelLink);
            }
            
            if (aFormat == HTML_FORMAT && isAutovueEnabled == true) {
                sbf.append("&nbsp;&nbsp;").append(autoVueLink);
            }
            
            if ((aFormat == TEXT_FORMAT) && (aPath.trim().equals("") == false)) {
                sbf.append(" <").append(httpLink).append(">");
            }

            sbf.append(LINE_BREAK);
        }

        return sbf.toString();
    }

    public static String formatDescription(String aDescription, String aSystemPrefix, String aServer, int aRequestId, int aActionId, int aMaxEmailActionId, ArrayList<String> aSearchTextList,
            int aAppendInterface, int aToolTipType, boolean isPlainText)
            throws DatabaseException {
        if ((aDescription == null) || (aDescription.equals("") == true)) {
            return "";
        }

        //
        // Render all requests links as smart links, if they have not been
        // converted correctly in the api for old requests.
        //
        aDescription = LinkFormatter.replaceHrefWithSmartLinks(aDescription);

        // Replace the \n with <br> and \t with 4 spaces.
        if(isPlainText){
        	aDescription = Utilities.htmlEncode(aDescription);
        	aDescription = aDescription.replaceAll("\n", "<br />");
        	aDescription = aDescription.replaceAll(" ", "&nbsp;");
        }
//        aDescription = aDescription.replaceAll("^[\n]+", "");
        //aDescription = Utilities.htmlEncode(aDescription);
       /* aDescription = aDescription.replaceAll("\\r\\n", "<br>");
        aDescription = aDescription.replaceAll("\\n", "<br>");
        aDescription = aDescription.replaceAll("\\r", "<br>");
        aDescription = aDescription.replaceAll("<br>", "\n<br>");
        aDescription = aDescription.replaceAll("<br> ", "<br>&nbsp;");
        aDescription = aDescription.replaceAll("  ", "&nbsp;&nbsp;");
        aDescription = aDescription.replaceAll("\\t", "&nbsp;&nbsp;&nbsp;&nbsp;");
*/
        //
        // Replaces the BusinessArea#Number with the corresponding link
        //
        aDescription = LinkFormatter.hyperSmartLinks(aDescription, aSystemPrefix + "#" + aRequestId, aActionId, aMaxEmailActionId, aServer, aAppendInterface, aToolTipType);

        //
        // replacing the http:,file: etc
        // with the corresponding hyperlinks
        // escaping the above inserted smartLinks.
        // this is done for proper rendering of ex:
        // http://www/,dflow#10 (where , is interpreted as part of link)
        //
//        aDescription = LinkFormatter.hyperLinks(aDescription, aSearchTextList);
        
        return aDescription;
    }

    /*
     */
    public static String highLightText(String aDescription, ArrayList<String> aSearchTextList) {
        if ((aSearchTextList == null) || (aSearchTextList.size() == 0)) {
            return aDescription;
        }

        //
        // Remove duplicate strs and make a unquie list of strings
        //
        ArrayList<String> uniqueList = new ArrayList<String>();

        for (String str : aSearchTextList) {
            if (uniqueList.contains(str) == false) {
                uniqueList.add(str);
            }
        }

        StringBuffer pattern = new StringBuffer("");

        //
        // Form ORed pattren string after joning unique list of strs
        // using | , ignoring stop words and some common tags like " & etc.
        //
        try {
            boolean first = true;

            for (String str : uniqueList) {

                //
                // trim all spaces,quotes,<> etc.
                //
                str = trim(str);

                if (str.equals("") == true) {
                    continue;
                }

                //
                // Ignore stop words
                //
                if (STOP_WORD_LIST.contains(str.toLowerCase()) == true) {
                    continue;
                }

                str = Utilities.htmlEncode(str);

                if (first == false) {
                    pattern.append("|");
                }

                pattern.append(str);
                first = false;
            }

            String patternStr = pattern.toString();

            //
            // If all are unqualified strings,and patternStr empty
            // return Original text
            //
            if (patternStr.trim().equals("") == true) {
                return aDescription;
            }

            //
            // Escape sepcial characters
            //
            patternStr = patternStr.replace("\\", "\\\\").replace("[", "\\[").replace("]", "\\]").replace("^", "\\^").replace("$", "\\$").replace(".", "\\.").replace("(", "\\(").replace(")",
                                            "\\)").replace("{", "\\{").replace("}", "\\}").replace("*", "");
            patternStr = "(" + patternStr + ")";

            //
            // Form pattern to do exact match of words,
            // allowing punctuations.
            //
            // Pattern p = Pattern.compile
            // ("(^|[^a-z^0-9])" + patternStr + "([^a-z^0-9]|$)",
            // Pattern.CASE_INSENSITIVE);
            //
            // Form pattern to support steming searcg i.e postfix *.
            // (.) is read post pattern to recognise Ba prefix related
            // info inserted by app to form smartlinks etc.
            //
            Pattern       p      = Pattern.compile("(^|[^a-z^0-9])" + patternStr + "(.|$)", Pattern.CASE_INSENSITIVE);
            StringBuilder buffer = new StringBuilder();
            String        str    = aDescription;
            Matcher       m      = p.matcher(aDescription);

            //
            // Highlight search text
            //
            while (m.find() == true) {
                String group1   = (m.group(1) == null)
                                  ? ""
                                  : m.group(1);
                String group2   = (m.group(2) == null)
                                  ? ""
                                  : m.group(2);
                String group3   = (m.group(3) == null)
                                  ? ""
                                  : m.group(3);
                String matchStr = group1 + group2 + group3;

                //
                // Don't Highlight SysPrefixes which are part of links
                //
                BusinessArea ba     = null;
                String       prefix = ((group2.endsWith("#") == true)
                                       ? group2.substring(0, group2.length() - 1)
                                       : group2);

                try {

                    // Lookup Business Area by prefix
                    ba = BusinessArea.lookupBySystemPrefix(prefix);
                } catch (DatabaseException e) {}

                if (ba == null) {

                    // Lets look in Prefix file
                    if (SysPrefixes.getPrefix(prefix) != null) {
                        ba = new BusinessArea();
                    }
                }

                if ((ba != null) && (group1.equals("/") || group3.equals("/") || group1.equals("'") || group3.equals("'") || group3.equals("_"))) {
                    buffer.append(str.substring(0, str.indexOf(matchStr))).append(matchStr);
                } else {
                    buffer.append(str.substring(0, str.indexOf(matchStr))).append(group1).append("<span name='matchedText' class=\"cy\">").append(group2).append("</span>").append(group3);
                }

                str = str.substring(str.indexOf(matchStr) + matchStr.length());
            }

            buffer.append(str);

            return buffer.toString();
        } catch (RuntimeException e) {
            LOG.info("",(e));
        }

        return aDescription;
    }

    /**
     * This method adds to aList, a list of UserHeaderRecords prepared from
     * the comma separated list in the aStrList.
     *
     * @param aList     Out parameter.
     * @param aStrList  Comma separated list of entries
     * @param aType     Type of entries.
     */
    private static void processUserHeader(ArrayList<UserHeaderRecord> aList, String aStrList, UserHeaderRecord.EntryType aType) {

        /*
         * remove the leading and trailing square brackets.
         */
        if (aStrList.startsWith("[")) {
            aStrList = aStrList.substring(1);
        }

        if (aStrList.endsWith("]")) {
            aStrList = aStrList.substring(0, aStrList.length() - 1);
        }

        // Split them on comma.
        ArrayList<String>           entries   = Utilities.toArrayList(aStrList, ",");
        ArrayList<UserHeaderRecord> localList = new ArrayList<UserHeaderRecord>();

        for (String entry : entries) {

            // Each entry is of the form Ordering:Login.
            String[] arr = entry.split(":");

            if ((arr == null) || (arr.length != 2)) {
                LOG.warn("Invalid format of user header entry: " + entry);

                continue;
            }

            int    ordering  = 0;
            String userLogin = arr[1];

            try {
                ordering = Integer.parseInt(arr[0]);
            } catch (NumberFormatException nfe) {
                LOG.warn("Exception while parsing the ordering field: " + nfe);
                ordering = 0;
            }

            UserHeaderRecord record = new UserHeaderRecord(userLogin, ordering, aType);

            localList.add(record);
        }

        /*
         * If these are DELETED entries, then sort them before adding them to
         * aList.
         */
        if (aType == UserHeaderRecord.EntryType.DELETED) {
            UserHeaderRecord.setSortParams(UserHeaderRecord.ORDERING, ASC_ORDER);
            localList = UserHeaderRecord.sort(localList);
        }

        // Finally add the localList formed to aList.
        aList.addAll(localList);
        localList = null;

        return;
    }

    /**
     * This method replaces all the tags with their corresponding values
     * reading them from the given table in the given HTML Parser object.
     *
     * @param aTagTable Table with [tag, value ]
     */
    public static void replaceTags(DTagReplacer aDTagReplacer, Hashtable<String, String> aTagTable, ArrayList<String> aTagList) {
        Enumeration<String> keys = aTagTable.keys();
        String              key;

        while (keys.hasMoreElements()) {
            key = keys.nextElement();
            aDTagReplacer.replace(key, aTagTable.get(key));
        }

        if (aTagList != null) {
            for (String tag : aTagList) {
                if (aTagTable.get(tag) == null) {
                    aDTagReplacer.replace(tag, "");
                }
            }
        }

        return;
    }

    /**
     */
    public static String trim(String aString) {
        if (aString == null) {
            return null;
        }

        aString = aString.trim().toLowerCase();

        if ((aString.equals("") == true) || (aString.equals("&quot;") == true) || (aString.equals("\"") == true) || (aString.equals("&nbsp;") == true) || (aString.equals("<") == true)
                || (aString.equals("&lt;") == true) || (aString.equals(">") == true) || (aString.equals("&gt;") == true) || (aString.equals("&") == true) || (aString.equals("&amp;") == true)) {
            return "";
        }

        if (aString.startsWith("\"") || aString.startsWith("<")) {
            aString = aString.substring(1);
        }

        if (aString.endsWith("\"") || aString.endsWith(">")) {
            int len = aString.length();

            len     = (len <= 1)
                      ? 0
                      : len - 1;
            aString = aString.substring(0, len);
        }

        if ((aString.equals("class") == true) || (aString.equals("href") == true) || (aString.equals("a") == true) || (aString.equals("/a") == true)) {
            return "";
        }

        return aString;
    }

    public static String untaintForHtml(String arg) {
        if ((arg == null) || arg.trim().equals("")) {
            arg = "";
        } else {
            arg = Utilities.htmlEncode(arg);
            arg = arg.replace("'", "\\'");
        }

        return arg;
    }

    //~--- get methods --------------------------------------------------------

    /**
     * This method Form the TextField Container Html.
     *
     * @param aFieldName the field name
     */
    public static String getTextFieldHtml(String aFieldName, String aDisplay, String aImg) {
        return getTextFieldHtml(TEXT_CLASS, TEXT_HEADER_CLASS, TEXT_CONTENT_CLASS, "", "", aFieldName+"_content", aDisplay, aImg + "<%=" + aFieldName + "_label%>" + "</span>", "<%=" + aFieldName + "%>", "", "",
                                "", "", "", "", "");
    }
    
    /**
     * This method Form the Action Html.
     *
     * @param aClass
     * @param aHeaderClass
     * @param aTextColorClass
     * @param aId
     * @param aLabel
     * @param aValue
     * @param aUpdated
     * @param aDateTime
     * @param aAttachments
     * @param aActionLog
     * @param aActionLogLink
     * @param aEditContentLink
     */
    public static String getTextFieldHtml(String aClass, String aHeaderClass, String aContentClass, String aTextColorClass, String aId, String aContentId, String aDisplay, String aLabel,
            String aValue, String aUpdated, String aDateTime, String aAttachments, String aActionLog, String aActionLogLink, String aEditContentLink, String aDiff) {
        return (TEXT_FIELD_HTML.replace("<=id>", aId).replace("<=actionId>", aId.replace("action", "")).replace("<=contentId>", aContentId).replace("<=showHeader>", aDisplay).replace("<=class>",
                                        aClass).replace("<=headerClass>", aHeaderClass).replace("<=contentClass>", aContentClass).replace("<=textColor>", aTextColorClass).replace("<=label>",
                                            aLabel).replace("<=updated_by>", aUpdated).replace("<=updated_time>", aDateTime).replace("<=attachments>", aAttachments).replace("<=actionLog>",
                                                aActionLog).replace("<=actionLogLink>", aActionLogLink).replace("<=value>", aValue).replace("<=editContentLink>", aEditContentLink).replace("<=diff>",
                                                    aDiff));
    }

    public static String getUserDisplayName(int aUserId) throws DatabaseException {
        User user = User.lookupAllByUserId(aUserId);

        if (user == null) {
            return "-";
        }

        String displayName = user.getDisplayName();
        String login       = user.getUserLogin();

        if (displayName.equals(login) || displayName.trim().equals("")) {
            return login;
        } else {
            return displayName + " (" + login + ")";
        }
    }

    public static String getUserFieldHtml(Collection<RequestUser> aList, Field aField, BusinessArea aBusinessArea, String aPath, String aStatusDesc) {
        StringBuilder buffer = new StringBuilder();

        if ((aList == null) || (aList.size() == 0)) {
            return "-";
        }

        String          sysPrefix  = aBusinessArea.getSystemPrefix();
        User            user       = null;
        boolean         first      = true;
        String          descriptor = aField.getName();
        FieldDescriptor fd         = null;

        try {
            fd = FieldDescriptor.getPrimaryDescriptor(aBusinessArea.getSystemId(), aField.getName());
        } catch (Exception e) {
            LOG.info("",(e));
        }

        if (fd != null) {
            descriptor = fd.getDescriptor();
        }

        //
        // Show primary user always at first.
        //
//        RequestUser primaryUser = null;
//
//        for (RequestUser reqUser : aList) {
//            if (reqUser.getIsPrimary() == true) {
//                primaryUser = reqUser;
//
//                break;
//            }
//        }

//        if (primaryUser != null) {
//            aList.remove(primaryUser);
//            aList.add(0, primaryUser);
//        }

        for (RequestUser reqUser : aList) {
            String userLogin = "";
            String userName  = "";
            String primary   = "";

            try {
                user      = reqUser.getUser();
                userLogin = user.getUserLogin().replace(".transbittech.com", "");
                userName  = ActionHelper.getUserDisplayName(user.getUserId()).replace(".transbittech.com", "");

                if (reqUser.getIsPrimary() == true) {

                    //
                    // aPath == "" means called from web
                    // aPath = primary_server means call from email
                    //
                    if (aPath.trim().equals("") == false) {
                        primary = "*";
                    } else {
                        primary = "<span onmouseover=\"return makeTrue" + "(domTT_activate(this, event," + " 'content', '[Primary]'," + " 'statusText', '[Primary]'," + " 'delay', 50,"
                                  + " 'closeAction', 'remove'));\">*</span>";
                    }
                }
            } catch (DatabaseException de) {
                LOG.warn(de.toString(), de);

                continue;
            }

            if (first == false) {
                buffer.append("; ");
            } else {
                first = false;
            }

            if ((aField.getPermission() & Permission.HYPERLINK) != 0) {
            	buffer.append(userName);
//                buffer.append("<a class=\"l cb\" href=\"").append(aPath).append("/search/").append(sysPrefix).append("?q=").append(descriptor).append(":").append(userLogin).append("+-").append(
//                    aStatusDesc).append(":closed&sv=2").append("\"").append(" target=\"_blank\">").append(primary).append(userName).append("</a>");
            } else {
                buffer.append(userLogin);
            }
        }

        return buffer.toString();
    }

    /**
     *
     * @param permTable
     * @param fieldName
     * @return
     */
    private static boolean hasPermissions(Hashtable<String, Integer> permTable, String fieldName) {

        //
        // User can view request only if view on private is granted
        // Hence we can always show its action_log. This a is done to avoid
        // False header message "Few fields not shown..etc" in emails.
        if (fieldName.equals(Field.IS_PRIVATE)) {
            return true;
        }

        boolean hasPerm = false;
        Integer temp    = permTable.get(fieldName);

        if (temp == null) {
            hasPerm = true;
        } else {
            int perm = temp.intValue();

            if ((perm & Permission.VIEW) != 0) {
                hasPerm = true;
            }
        }

        return hasPerm;
    }

    private static int getPerm(Hashtable<String, Integer> permTable, String fieldName)
    {
    	if( null == permTable || null == fieldName )
    		return 0 ;
    	
    	Integer perm = permTable.get(fieldName);
    	if( null == perm )
    		perm = 0 ;
    	
    	return perm ;
    }
    
	public static void fillAttachmentDetailsJSON(
			Hashtable<String, Object> tagTable,
			Hashtable<String, Integer> permissions, BusinessArea businessArea,
			Request request, User user) throws DatabaseException {
		JsonObject rootNode = getAttachmentDetailsJSONInternal(permissions, businessArea, request, user);
		tagTable.put("requestFiles", rootNode.toString());
	}

	public static JsonObject getAttachmentDetailsJSONInternal(
			Hashtable<String, Integer> permissions, BusinessArea businessArea,
			Request request, User user) throws DatabaseException {
		JsonObject rootNode = new JsonObject();
		JsonParser jsonParser = new JsonParser();
		for(Field f:Field.lookupBySystemId(businessArea.getSystemId()))
		{
			if(f.getDataTypeId() == DataType.ATTACHMENTS)
			{
				Integer fieldPermInteger = permissions.get(f.getName());
				int fieldPerm = 0;
				if(fieldPermInteger != null)
					fieldPerm = fieldPermInteger.intValue();
				
				if( (fieldPerm & Permission.VIEW) != 0)
				{
					Boolean canChange = ( (fieldPerm & Permission.CHANGE) != 0);
					Boolean canAdd = ( (fieldPerm & Permission.ADD) != 0);
					JsonObject fieldNode = new JsonObject();
					fieldNode.addProperty("fieldDisplayName", f.getDisplayName());
					fieldNode.addProperty("canChange", canChange);
					fieldNode.addProperty("canAdd", canAdd);
					fieldNode.addProperty("displayOder", f.getDisplayOrder());
					fieldNode.addProperty("fieldId", f.getFieldId());
					fieldNode.addProperty("numberOfFiles", 0);
					String filesStr = null;
					
					if (request != null) {
						if (f.getIsExtended()) {
							filesStr = request.getExString(f.getName());
						} else {
							filesStr = AttachmentInfo.toJson(request
									.getAttachments());
						}
					}
					
					if( (filesStr == null) || (filesStr.trim().length() == 0))
						filesStr = "[]";
					
					fieldNode.add("files", jsonParser.parse(filesStr));
					rootNode.add(f.getName(), fieldNode);
				}
			}
		}
		return rootNode;
 	}

	public static String getResourceString(ArrayList<String>resources, String nearestPath )
	{
//		String nearestPathTag = "<%=nearestPath%>" ;
		String rs = nearestPath + "c?" ;
		Long maxLastModifiedTime = null ;
		for(String path :resources )
		{
			File file = Configuration.findPath(path);
	
	    	if (file == null) {
	    		LOG.info("File Not Found : " + path ) ;
	    		continue ;
	    	}
	    	
	    	if(file.isDirectory())
	    	{
	    		LOG.info("File is a Directory : Skipping : " + path ) ;
	    	    continue ;
	    	}
	    	
	    	rs += nearestPath + path + "&";
	    	long lm = file.lastModified() ;
//	    	LOG.info("path=" +path + "    last-modified=" + lm ) ;
	    	if( null == maxLastModifiedTime )
	    		maxLastModifiedTime = new Long( lm ) ;
	    	else
	    	{
	    		if( maxLastModifiedTime.longValue() < lm )
	    		{
	    			maxLastModifiedTime = new Long(lm);
	    		}
	    	}
		}
				
//		LOG.info("maxLastModifiedTime=" +  ) ;
		if( maxLastModifiedTime != null ) 
		{
			maxLastModifiedTime = maxLastModifiedTime / 1000 * 1000 ; // convert to latest seconds
		//	LOG.info("setting maxLastModifiedTime=" + maxLastModifiedTime + " : " + DateFormat.getInstance().format(new Date(maxLastModifiedTime) ) ) ;
			rs += maxLastModifiedTime ;
		}
		
		return rs ;
	}
}


class UserHeaderRecord implements Comparable<UserHeaderRecord> {

    // Enum sort of fields for Attributes.
    public static final int  USERLOGIN = 1;
    public static final int  ORDERING  = 2;
    private static final int ENTRYTYPE = 3;

    // Static attributes related to sorting.
    private static int ourSortField = ORDERING;
    private static int ourSortOrder = ASC_ORDER;

    //~--- fields -------------------------------------------------------------

    private EntryType myEntryType;
    private int       myOrdering;

    // Attributes of this Domain Object.
    private String myUserLogin;

    //~--- constant enums -----------------------------------------------------

    enum EntryType { ADDED, COMMON, DELETED }

    ;

    //~--- constructors -------------------------------------------------------

    /**
     * The default constructor.
     */
    public UserHeaderRecord() {}

    /**
     * The complete constructor.
     *
     *  @param aUserLogin
     *  @param aOrdering
     *  @param aEntryType
     */
    public UserHeaderRecord(String aUserLogin, int aOrdering, EntryType aEntryType) {
        myUserLogin = aUserLogin;
        myOrdering  = aOrdering;
        myEntryType = aEntryType;
    }

    //~--- methods ------------------------------------------------------------

    /**
     * Method that compares this object with the one passed
     * W.R.T the ourSortField.
     *
     * @param aObject  Object to be compared.
     *
     * @return 0 - If they are equal.
     *         1 - If this is greater.
     *        -1 - If this is smaller.
     *
     */
    public int compareTo(UserHeaderRecord aObject) {
        switch (ourSortField) {
        case USERLOGIN : {
            String login1 = myUserLogin;
            String login2 = aObject.myUserLogin;

            if (ourSortOrder == ASC_ORDER) {
                return login1.compareToIgnoreCase(login2);
            } else {
                return login2.compareToIgnoreCase(login1);
            }
        }

        case ORDERING : {
            Integer i1 = myOrdering;
            Integer i2 = aObject.myOrdering;

            if (i1.equals(i2)) {
                String login1 = myUserLogin;
                String login2 = aObject.myUserLogin;

                if (ourSortOrder == ASC_ORDER) {
                    return login1.compareToIgnoreCase(login2);
                } else {
                    return login2.compareToIgnoreCase(login1);
                }
            }

            return (ourSortOrder == TBitsConstants.ASC_ORDER)
                   ? i1.compareTo(i2)
                   : i2.compareTo(i1);
        }
        }

        return 0;
    }

    /**
     * Method to return the source arraylist in the sorted order
     *
     * @param  source the array list of Type objects
     * @return the ArrayList of the UserHeaderRecord objects in sorted order
     */
    public static ArrayList<UserHeaderRecord> sort(ArrayList<UserHeaderRecord> source) {
        int                size     = source.size();
        UserHeaderRecord[] srcArray = new UserHeaderRecord[size];

        for (int i = 0; i < size; i++) {
            srcArray[i] = source.get(i);
        }

        Arrays.sort(srcArray, new UserHeaderRecordComparator());

        ArrayList<UserHeaderRecord> target = new ArrayList<UserHeaderRecord>();

        for (int i = 0; i < size; i++) {
            target.add(srcArray[i]);
        }

        return target;
    }

    /**
     *
     */
    public String toString() {
        return (new StringBuffer().append("[ ").append(myUserLogin).append(", ").append(myOrdering).append(", ").append(myEntryType).append(" ]")).toString();
    }

    //~--- get methods --------------------------------------------------------

    /**
     * Accessor method for EntryType property.
     *
     * @return Current Value of EntryType
     *
     */
    public EntryType getEntryType() {
        return myEntryType;
    }

    /**
     * Accessor method for Ordering property.
     *
     * @return Current Value of Ordering
     *
     */
    public int getOrdering() {
        return myOrdering;
    }

    /**
     * Accessor method for UserLogin property.
     *
     * @return Current Value of UserLogin
     *
     */
    public String getUserLogin() {
        return myUserLogin;
    }

    //~--- set methods --------------------------------------------------------

    /**
     * Mutator method for EntryType property.
     *
     * @param aEntryType New Value for EntryType
     *
     */
    public void setEntryType(EntryType aEntryType) {
        myEntryType = aEntryType;
    }

    /**
     * Mutator method for Ordering property.
     *
     * @param aOrdering New Value for Ordering
     *
     */
    public void setOrdering(int aOrdering) {
        myOrdering = aOrdering;
    }

    /**
     * Mutator method for SortField property.
     *
     * @param aSortField New Value of SortField
     *
     */
    public static void setSortField(int aSortField) {
        ourSortField = aSortField;
    }

    /**
     * Mutator method for SortOrder property.
     *
     * @param aSortOrder New Value of SortOrder
     *
     */
    public static void setSortOrder(int aSortOrder) {
        ourSortOrder = aSortOrder;
    }

    /**
     * Mutator method for ourSortField and ourSortOrder properties.
     *
     * @param aSortField New Value of SortField
     * @param aSortOrder New Value of SortOrder
     *
     */
    public static void setSortParams(int aSortField, int aSortOrder) {
        ourSortField = aSortField;
        ourSortOrder = aSortOrder;
    }

    /**
     * Mutator method for UserLogin property.
     *
     * @param aUserLogin New Value for UserLogin
     *
     */
    public void setUserLogin(String aUserLogin) {
        myUserLogin = aUserLogin;
    }
}


/**
 * This class is the comparator of UserHeaderRecords.
 *
 *
 * @author : Vaibhav
 * @version : $Id: $
 */
class UserHeaderRecordComparator implements Comparator<UserHeaderRecord>, Serializable {
    public int compare(UserHeaderRecord obj1, UserHeaderRecord obj2) {
        return obj1.compareTo(obj2);
    }
}
