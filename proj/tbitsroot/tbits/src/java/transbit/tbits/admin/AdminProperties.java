/*
 * Copyright (c) 2005 Transbit Technologies Pvt. Ltd. All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of Transbit Technologies Pvt. Ltd. ("Confidential Information").  You
 * shall not disclose such Confidential Information and shall use
 * it only in accordance with the terms of the license agreement
 * you entered into with Transbit Technologies Pvt. Ltd.
 */



package transbit.tbits.admin;

//~--- non-JDK imports --------------------------------------------------------
import transbit.tbits.authentication.AuthConstants;
import transbit.tbits.Helper.Messages;
import transbit.tbits.Helper.TBitsConstants;
import transbit.tbits.admin.AdminUtil;
import transbit.tbits.admin.common.AdminProxyServlet;
import transbit.tbits.admin.common.MenuItem;
import transbit.tbits.admin.common.NavMenu;
import transbit.tbits.admin.common.URLRegistry;
import transbit.tbits.api.Mapper;
import transbit.tbits.common.DTagReplacer;
import transbit.tbits.common.DatabaseException;
import transbit.tbits.common.PropertiesHandler;
import transbit.tbits.common.TBitsLogger;
import transbit.tbits.common.Utilities;
import transbit.tbits.config.CustomLink;
import transbit.tbits.config.SysConfig;
import transbit.tbits.config.WebConfig;
import transbit.tbits.domain.BAMailAccount;
import transbit.tbits.domain.BusinessArea;
import transbit.tbits.domain.DateTimeFormat;
import transbit.tbits.domain.Field;
import transbit.tbits.domain.Type;
import transbit.tbits.domain.User;
import transbit.tbits.exception.TBitsException;
import transbit.tbits.webapps.WebUtil;

//~--- JDK imports ------------------------------------------------------------

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.IllegalFormatException;
import java.util.StringTokenizer;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.oreilly.servlet.multipart.PartInputStream;

import net.sf.json.JSONArray;

//~--- classes ----------------------------------------------------------------

/**
 * This is the servlet for rendering the properties page in admin.
 *
 * @author  : Vinod Gupta
 * @version : $Id: $
 */
public class AdminProperties extends HttpServlet implements TBitsConstants {

    // Logger that logs information/error messages to the Application Log.
    private static final TBitsLogger LOG        = TBitsLogger.getLogger(PKG_ADMIN);
    private static final int         ADMIN      = 10;
    private static final int         PROPERTIES = 1;

    // HTML Interfaces used to display the Add-Request page in TBits.
    private static final String HTML = "web/tbits-admin-properties.htm";

    // ArrayList which contains all the tags to be replaced.
    private static ArrayList<String> tagList;

  
    //~--- static initializers ------------------------------------------------

    static {
        tagList = Utilities.toArrayList(
            new StringBuilder().append("sys_ids,title,cssFile,sys_prefix,ba_name,display_name,location_hyd,")
			.append("location_nyc,volunteer_round_checked,volunteer_random_checked,volunteer_none_checked,request_prefix,assign_to_any,")
			.append("supported_prefixes,email,email_actions,send_email_request,send_email_action,date_time_formats,")
			.append("default_due_date,is_time_enabled,is_time_enabled_display,due_date_display,due_date_options,notify_addRequest,")
			.append("notify_addAction,notify_loggers_addRequest,notify_loggers_addAction,incoming_high,incoming_low,outgoing_high,")
			.append("outgoing_low,severity_list,time_zone,help_display,help_link,administrative_contact,description,")
			.append("private_ba,active_ba,submit,revert,allTypeFields,instanceBoldHyd,instanceBoldNyc,")
			.append("instancePathHyd,instancePathNyc,nearestPath,baAdminList,ba_mail_server,ba_mail_login,ba_mail_password, ba_mail_protocol, ba_mail_port, ba_mail_is_active,userLogin,display_logout,")
			.append("baMailConfig,myCategories")
			.toString());

        ArrayList<String> tempList = new ArrayList<String>();

        for (String tag : tagList) {
            tempList.add(tag);
            tempList.add(tag + "_disabled");
        }

		tagList = tempList;
		
		//Added by Lokesh for hide/show transmittal tab based on transmittal property in app-properties
		tagList.add("trn_display");

        //urls
        String url = "baprops";
    	String completeURL = "baprops.admin";
    	
        //Create Mapping
		URLRegistry.getInstance().addMapping(AdminProxyServlet.class, url, AdminProperties.class);
		
		//Create Menu
		NavMenu nav = NavMenu.getInstance();
		nav.BAMenu.add(new MenuItem("BA Properties", completeURL, "The general properties of the Business Area can be configured here."));
		
        // Get the location of the temporary directory.
    }

    //~--- methods ------------------------------------------------------------

    public static void disableAllFields(Hashtable<String, String> aTagTable, ArrayList<String> aAdminList) {
        int index;

        for (String tag : tagList) {
            index = tag.indexOf("_disabled");

            if (aAdminList.contains("SUPER_ADMIN") == false) {
                if (index > 0) {
                    //System.out.println(tag);
                    aTagTable.put(tag, "disabled");
                }
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
        HttpSession session = aRequest.getSession();

        try {
            handleGetRequest(aRequest, aResponse);
        } catch (DatabaseException de) {
            session.setAttribute("ExceptionObject", de);
            aResponse.sendRedirect(WebUtil.getServletPath(aRequest, "/error"));
            de.printStackTrace();
            return;
        } catch (TBitsException de) {
            session.setAttribute("ExceptionObject", de);
            aResponse.sendRedirect(WebUtil.getServletPath(aRequest, "/error"));
            de.printStackTrace();

            return;
        }

        return;
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
        PrintWriter out     = aResponse.getWriter();
        HttpSession session = aRequest.getSession();

        try {
            handlePostRequest(aRequest, aResponse);
        } catch (DatabaseException de) {
            aResponse.sendRedirect(WebUtil.getServletPath(aRequest, "/error"));
            de.printStackTrace();

            return;
        } catch (TBitsException de) {
            aResponse.sendRedirect(WebUtil.getServletPath(aRequest, "/error"));
            de.printStackTrace();

            return;
        }

        return;
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
        aResponse.setContentType("text/html");

        PrintWriter out     = aResponse.getWriter();
        HttpSession session = aRequest.getSession();

        // Steps followed while servicing a Get Request to this page.
        // 1. Validate the user.
        // 2. Get the request params and thereby the BusinessArea.
        // 3. Check Basic Permissions to come to this page.
        // 4. Get Exclusion List by ROLE.
        // 5. Replace the tags in the form by their corresponding value.
        // Step 1: Validate the User.
        User                      user       = WebUtil.validateUser(aRequest);
        WebConfig                 userConfig = user.getWebConfigObject();
        int                       userId     = user.getUserId();
        Hashtable<String, Object> paramTable = AdminUtil.getRequestParams(aRequest, userConfig, ADMIN);
        BusinessArea              ba         = (BusinessArea) paramTable.get(Field.BUSINESS_AREA);
        
        if (ba == null) {
            throw new TBitsException(Messages.getMessage("INVALID_BUSINESS_AREA"));
        }
        
        ArrayList<BAMailAccount> bAMailAccounts = BAMailAccount.lookupByBA(ba.getSystemPrefix());
        
        int    systemId  = ba.getSystemId();
        String sysPrefix = ba.getSystemPrefix();

        // Tag Table contains all the [tag_name, value] pairs.
        Hashtable<String, String> tagTable = new Hashtable<String, String>();

        // Check Basic Permissions to come to this page
        ArrayList<String> adminList = AdminUtil.checkBasicPermissions(systemId, userId, PROPERTIES);

        if ((adminList.contains("SUPER_ADMIN") == false) && (adminList.contains("PERMISSION_ADMIN") == false)) {
            throw new TBitsException(Messages.getMessage("INVALID_USER"));
        } else {
            tagTable.put("superuser_display", "");
        }
        
      //Added by Lokesh for hide/show transmittal tab based on transmittal property in app-properties
        String trnProperty = PropertiesHandler.getAppProperties().getProperty("transbit.tbits.transmittal");
        if ((trnProperty == null) || Boolean.parseBoolean(trnProperty) == false)
			tagTable.put("trn_display", "none");
		else
			tagTable.put("trn_display", "");


        disableAllFields(tagTable, adminList);
        WebUtil.setInstanceBold(tagTable, ba.getSystemPrefix());

        // Get BusinessArea List in which the user has permissions to view
        // the admin page.
        String baList = AdminUtil.getSysIdList(systemId, userId);

        tagTable.put("sys_ids", baList);

        // Get sys_config
        SysConfig sc = ba.getSysConfigObject();

        // Get all the type-fields.
        String typeFieldHtml = AdminUtil.getTypeFields(systemId);

        tagTable.put("allTypeFields", typeFieldHtml);

        // Get the name,display-name,prefix and supported prefixes.
        tagTable.put("ba_name", ba.getName());
        tagTable.put("display_name", ba.getDisplayName());
        tagTable.put("request_prefix", ba.getSystemPrefix());
        tagTable.put("sys_prefix", ba.getSystemPrefix());
        tagTable.put("baAdminList", AdminUtil.getBAAdminEmailList());
        tagTable.put("description", ba.getDescription());
        
        tagTable.put("baMailConfig", JSONArray.fromObject(bAMailAccounts).toString());
        ArrayList<Type> categories = (ArrayList<Type>) Type.lookupAllBySystemIdAndFieldName(ba.getSystemId(), "category_id").clone();
        Type noneCat = new Type(0, 0, 0, "none", "None", "You should select it if you do not want any category to be selected during addition of request.", 0, true,true, true,false, false);
        categories.add(noneCat);
        tagTable.put("myCategories", JSONArray.fromObject(categories).toString());
//        BAMailAccount bAMailAccount = bAMailAccounts.get(0);
//        if(bAMailAccount != null)
//        {
//        	tagTable.put("ba_mail_server", Utilities.checkNull(bAMailAccount.getMyMailServer()));
//        	tagTable.put("ba_mail_login", Utilities.checkNull(bAMailAccount.getMyEmailID()));
//        	tagTable.put("ba_mail_password", Utilities.checkNull(bAMailAccount.getMyPassward()));
//        	tagTable.put("ba_mail_protocol", Utilities.checkNull(bAMailAccount.getMyProtocol()));
//        	tagTable.put("ba_mail_port", "" + bAMailAccount.getPort());
//        	if(bAMailAccount.isActive())
//        	{
//        		tagTable.put("ba_mail_is_active", "checked");
//        	}
//        	else
//        	{
//        		tagTable.put("ba_mail_is_active", "");
//        	}
//        }
//        else
//        {
//        	tagTable.put("ba_mail_server", "");
//        	tagTable.put("ba_mail_login", "");
//        	tagTable.put("ba_mail_password", "");
//        	tagTable.put("ba_mail_protocol", "pop3");
//        	tagTable.put("ba_mail_port", "110");
//        	tagTable.put("ba_mail_is_active", "");
//        }
        // Get the location.
        String location = ba.getLocation();

        if ((location != null) && location.equalsIgnoreCase("hyd")) {
            tagTable.put("location_hyd", "checked");
        }

        if ((location != null) && location.equalsIgnoreCase("nyc")) {
            tagTable.put("location_nyc", "checked");
        }

        tagTable.put("title", "TBits Admin: " + ba.getDisplayName() + " Properties");

        // Get Volunteer
        int volunteer = sc.getVolunteer();

        if (volunteer == RR_VOLUNTEER) {
            tagTable.put("volunteer_round_checked", "checked");
        } else if (volunteer == RANDOM_VOLUNTEER) {
            tagTable.put("volunteer_random_checked", "checked");
        } else {
            tagTable.put("volunteer_none_checked", "checked");
        }

        // Get Assign to Any
        boolean assignToAll = sc.getAssignToAll();

        if (assignToAll == true) {
            tagTable.put("assign_to_any", "checked");
        }

        // Get Supported Prefixes
        ArrayList<String> legacyPrefixes = sc.getLegacyPrefixList();
        String            lpList         = "";
        String            legacyPrefix   = null;
        int               size           = legacyPrefixes.size();

        for (int i = 0; i < size; i++) {
            legacyPrefix = legacyPrefixes.get(i);

            if (i != 0) {
                lpList = lpList + ";" + legacyPrefix;
            } else {
                lpList = legacyPrefix;
            }
        }

        tagTable.put("supported_prefixes", lpList);

        // Get DateTime Formats.
        int currentDTF = sc.getEmailDateFormat();

        tagTable.put("date_time_formats", getDateTimeFormats(currentDTF));

        // Get DateTime Options.
        getDatetimeOptions(sc, tagTable);

        long defaultDuedate = sc.getDefaultDueDate();

        if (!(sc.getIsDueDateDisabled())) {
            tagTable.put("default_due_date", "" + defaultDuedate);

            /*
             *       if(sc.getAllowNullDueDate() == false)
             * {
             *   tagTable.put("is_time_enabled_display","none");
             *   }
             */
        }

        if ((sc.getIsDueDateDisabled() == true)) {
            tagTable.put("is_time_enabled_display", "none");
        } else {
            if (sc.getIsTimeDisabled() == true) {
                tagTable.put("is_time_enabled", "");
                tagTable.put("default_due_date", "0");
            } else if (sc.getIsTimeDisabled() == false) {
                tagTable.put("is_time_enabled", "checked");
            }
        }

        // Get timeZones.
        tagTable.put("time_zone", getTimezones(systemId, sc.getPreferredZone()));
        tagTable.put("time_zone_disabled", "disabled");

        // Email Related Tags
        tagTable.put("email", ba.getEmail());
        tagTable.put("email_actions", Integer.toString(ba.getMaxEmailActions()));

        boolean mailActive = ba.getIsEmailActive();

        if (mailActive == true) {
            tagTable.put("send_email_request", "checked");
        } else {
            tagTable.put("notify_addRequest_disabled", "disabled");
            tagTable.put("notify_loggers_addRequest_disabled", "disabled");
            tagTable.put("notify_addAction_disabled", "disabled");
            tagTable.put("notify_loggers_addAction_disabled", "disabled");
            tagTable.put("send_email_action_disabled", "disabled");
        }

        boolean mailAction = sc.getNotifyAppender();

        if ((mailAction == true) && (mailActive == true)) {
            tagTable.put("send_email_action", "checked");
        }

//      else
//      {
//          tagTable.put("notify_addAction_disabled","disabled");
//          tagTable.put("notify_loggers_addAction_disabled","disabled");
//      }
        // Get mail actions for add-request and add-action;
        int mail;

        mail = sc.getRequestNotify();

        if ((mailActive == true) && (mail == NOTIFY_EMAIL)) {
            tagTable.put("notify_addRequest", "checked");
        }

        mail = sc.getActionNotify();

        if ((mail == NOTIFY_EMAIL)) {
            tagTable.put("notify_addAction", "checked");
        }

        boolean temp;

        temp = sc.getRequestNotifyLoggers();

        if ((mailActive == true) && (temp == true)) {
            tagTable.put("notify_loggers_addRequest", "checked");
        }

        temp = sc.getActionNotifyLoggers();

        if ((temp == true)) {
            tagTable.put("notify_loggers_addAction", "checked");
        }

        StringBuilder sevList = new StringBuilder();

        // Get the severity related options.
        tagTable.put("incoming_high", getSeverityList(systemId, sc.getIncomingSeverityHigh(), sevList));
        tagTable.put("severity_list", sevList.toString());
        tagTable.put("incoming_low", getSeverityList(systemId, sc.getIncomingSeverityLow(), sevList));
        tagTable.put("outgoing_high", getOutgoingSeverityList(systemId, sc.getOutgoingSeverityHigh(), true, adminList));
        tagTable.put("outgoing_low", getOutgoingSeverityList(systemId, sc.getOutgoingSeverityLow(), false, adminList));

        // Get help name and links
        ArrayList<CustomLink> cl = sc.getCustomLinks();

        if ((cl != null) && (cl.size() > 0)) {
            tagTable.put("help_display", cl.get(0).getName());
            tagTable.put("help_link", cl.get(0).getValue());
        }

        tagTable.put("administrative_contact", sc.getAdministrator());

        // Get Private and is_active attributes
        temp = ba.getIsActive();

        if (temp == true) {
            tagTable.put("active_ba", "checked");
        }

        temp = ba.getIsPrivate();

        if (temp == true) {
            tagTable.put("private_ba", "checked");
        }

        tagTable.put("nearestPath", aRequest.getContextPath() + "/");
        tagTable.put("cssFile", WebUtil.getCSSFile(sc.getWebStylesheet(), sysPrefix, false));
		tagTable.put("userLogin", user.getUserLogin());

		String display_logout = "none";
		if(aRequest.getAuthType() == AuthConstants.AUTH_TYPE)
			display_logout = "";
		tagTable.put("display_logout", display_logout);
		
		DTagReplacer dtr = new DTagReplacer(HTML);

        AdminUtil.replaceTags(dtr, tagTable, tagList);
        out.println(dtr.parse(systemId));
    }

    private void handlePostRequest(HttpServletRequest aRequest, HttpServletResponse aResponse) throws ServletException, IOException, TBitsException, DatabaseException, FileNotFoundException {
        aResponse.setContentType("text/html");

        PrintWriter out     = aResponse.getWriter();
        HttpSession session = aRequest.getSession();

        // Step 1: Validate the User.
        User                      user       = WebUtil.validateUser(aRequest);
        WebConfig                 userConfig = user.getWebConfigObject();
        int                       userId     = user.getUserId();
        
        // Get newfieldName
        String newBA = aRequest.getParameter("new_ba");

        if ((newBA != null) && (newBA.trim().equalsIgnoreCase("true") == true)) {
            String       newBAName   = aRequest.getParameter("new_ba_name");
            String       newBAPrefix = aRequest.getParameter("new_ba_prefix");
            BusinessArea ba1         = BusinessArea.lookupBySystemPrefix(newBAName);
            BusinessArea ba2         = BusinessArea.lookupBySystemPrefix(newBAPrefix);

            if ((ba1 == null) && (ba2 == null)) {
                BusinessArea.createBusinessArea(newBAName, newBAPrefix);
                Mapper.refreshBOMapper();
                out.println("Success");

                return;
            } else if (ba1 != null) {
                out.println(Messages.getMessage("DUPLICATE_BUSINESS_AREA_NAME"));

                return;
            } else if (ba2 != null) {
                out.println(Messages.getMessage("DUPLICATE_BUSINESS_AREA_PREFIX"));

                return;
            }
        }

        Hashtable<String, Object> paramTable = AdminUtil.getRequestParams(aRequest, userConfig, ADMIN);
        BusinessArea              ba         = (BusinessArea) paramTable.get(Field.BUSINESS_AREA);

        if (ba == null) {
            throw new TBitsException(Messages.getMessage("INVALID_BUSINESS_AREA"));
        }

        int       systemId = ba.getSystemId();
        SysConfig sc       = ba.getSysConfigObject();

        String requestPrefix = aRequest.getParameter("request_prefix");

        if (requestPrefix == null) {
            requestPrefix = ba.getSystemPrefix();
        }

        String strAction = aRequest.getParameter("action");

        // Now start getting each of the parameters and update them at end.
        if (strAction.equalsIgnoreCase("onlySevLists") == false) {

            // BA Properties
            String            name                = aRequest.getParameter("ba_name");
            String            displayName         = aRequest.getParameter("display_name");
            String            sysPrefix           = aRequest.getParameter("sys_prefix");
            String            description         = aRequest.getParameter("description");
            String            supportedPrefixes   = aRequest.getParameter("supported_prefixes");
            ArrayList<String> legacyPrefixList    = new ArrayList<String>();
            String[]          supportedPrefixList = supportedPrefixes.split(";");

            for (int i = 0; i < supportedPrefixList.length; i++) {
                if (supportedPrefixList[i].equals("") == false) {
                    legacyPrefixList.add(supportedPrefixList[i]);
                }
            }

            String  location    = aRequest.getParameter("location");
            String  volunteer   = aRequest.getParameter("volunteer");
            String  assignToAny = aRequest.getParameter("assign_to_any");
            boolean assAny;

            if ((assignToAny != null) && (!assignToAny.trim().equals("")) && (!assignToAny.trim().equals("false"))) {
                assAny = true;
            } else {
                assAny = false;
            }

            // Email
            String email          = aRequest.getParameter("email");
            String emailActionsNo = aRequest.getParameter("email_actions");
            int    emailActions   = 10;

            try {
                emailActions = Integer.parseInt(emailActionsNo);
            } catch (NumberFormatException nfe) {
                System.out.println("The field in max email actions should be a +ve integer.");
            }

            String emailRequest = aRequest.getParameter("send_email_request");
            String emailAction  = aRequest.getParameter("send_email_action");
            String dtFormat     = aRequest.getParameter("date_time_formats");
            int    dateFormatId = 0;

            try {
                dateFormatId = Integer.parseInt(dtFormat);
            } catch (NumberFormatException nfe) {
                LOG.info("The values in Date Format should be integers.");
            }

            String timeZone   = aRequest.getParameter("time_zone");
            int    timeZoneId = 0;

            try {
                timeZoneId = Integer.parseInt(timeZone);
            } catch (NumberFormatException nfe) {
                LOG.info("The values in timeZone should be integers");
            }

            // Get DueDate Options
            String duedateOption   = aRequest.getParameter("due_by_field");
            String strDueDateValue = aRequest.getParameter("default_due_date");
            long   dueDateValue    = 0;

            if ((strDueDateValue != null)) {
                dueDateValue = Integer.parseInt(strDueDateValue);
            }

            String  dueTimeOption = aRequest.getParameter("is_time_enabled");
            boolean emailReq, emailAct;

            if ((emailRequest == null) || emailRequest.trim().equals("") || emailRequest.trim().equals("false")) {
                emailReq = false;
            } else {
                emailReq = true;
            }

            if ((emailAction == null) || emailAction.trim().equals("") || emailAction.trim().equals("false")) {
                emailAct = false;
            } else {
                emailAct = true;
            }

            boolean dueTime;

            if ((dueTimeOption == null) || dueTimeOption.trim().equals("") || dueTimeOption.trim().equals("false")) {
                dueTime = true;
            } else {
                dueTime = false;
            }

            // Add Request:Add Action Defaults.
            int     nAR, nAA;
            boolean nARL, nAAL;
            String  notifyAR = aRequest.getParameter("notify_addRequest");

            if ((notifyAR == null) || notifyAR.trim().equals("") || notifyAR.trim().equals("false")) {
                nAR = NO_NOTIFY;
            } else {
                nAR = NOTIFY_EMAIL;
            }

            String notifyAA = aRequest.getParameter("notify_addAction");

            if ((notifyAA == null) || notifyAA.trim().equals("") || notifyAA.trim().equals("false")) {
                nAA = NO_NOTIFY;
            } else {
                nAA = NOTIFY_EMAIL;
            }

            String notifyARL = aRequest.getParameter("notify_loggers_addRequest");

            if ((notifyARL == null) || notifyARL.trim().equals("") || notifyARL.trim().equals("false")) {
                nARL = false;
            } else {
                nARL = true;
            }

            String notifyAAL = aRequest.getParameter("notify_loggers_addAction");

            if ((notifyAAL == null) || notifyAAL.trim().equals("") || notifyAAL.trim().equals("false")) {
                nAAL = false;
            } else {
                nAAL = true;
            }

            // Help Link
            String helpDisplay = aRequest.getParameter("help_display");
            String helpLink    = aRequest.getParameter("help_link");
            String contact     = aRequest.getParameter("administrative_contact");

            // POP Account Settings
            String totalAccountsStr = aRequest.getParameter("totalAccounts");
            ArrayList<BAMailAccount> bamailaccounts = BAMailAccount.lookupByBA(ba.getSystemPrefix());
            ArrayList<BAMailAccount> newBaMailAcs = new ArrayList<BAMailAccount>();
            int maxAccounts = 0;
            try
            {
            	maxAccounts = Integer.parseInt(totalAccountsStr);
            	for(int i = maxAccounts;i > 0; i--)
            	{
            		String idStr = aRequest.getParameter("ba_mail_ac_id_" + i);
            		int id = 0;
            		try{
            			id = Integer.parseInt(idStr);
            		}
            		catch (Exception e) {
						continue;
					}
            		String newBaMailServer = aRequest.getParameter("ba_mail_server_" + i);
                    String newBaMailLogin = aRequest.getParameter("ba_mail_login_" + i);
                    String newBaMailPassword = aRequest.getParameter("ba_mail_password_" + i);
                    String protocol = aRequest.getParameter("ba_mail_protocol_" + i);
                    String emailAddress = aRequest.getParameter("ba_email_address_" + i);
                    int port = 110;
                    try
                    {
                    	port = Integer.parseInt(aRequest.getParameter("ba_mail_port_" + i));
                    }
                    catch (Exception e) {
						
					}
                    
                    boolean isActive = false;
                    String baMailIsActiveStr = aRequest.getParameter("ba_mail_is_active_" + i);
                	if((baMailIsActiveStr != null) && (baMailIsActiveStr.toLowerCase().trim().equals("on")))
                	{
                		isActive = true;
                	}	
                	int categoryId = 0;
					 try
					 {
						 categoryId = Integer.parseInt(aRequest.getParameter("category_id_" + i));
					 }
					 catch (Exception e) {
						
					}
                    BAMailAccount account = new BAMailAccount(id, newBaMailLogin, newBaMailPassword, newBaMailServer, ba.getSystemPrefix(), protocol, port, isActive, categoryId, emailAddress);
                    newBaMailAcs.add(account);
            	}
            }
            catch (Exception e) {
				LOG.error("Unable to get max accounts.");
			}
            
            
            //Reconcile
            //Find deleted ones
            for(BAMailAccount bama:bamailaccounts)
            {
            	boolean found = false;
            	for(BAMailAccount newBama:newBaMailAcs)
            	{
            		if(newBama.getMyBAMailAcId() == bama.getMyBAMailAcId())
            		{
            			found = true;
            			break;
            		}
            	}
            	if(!found)
            	{
            		bama.deleteAc();
            	}
            }
            
            //Update or Add. Doing it 
            for(BAMailAccount newBama:newBaMailAcs)
        	{
        			newBama.SaveToDB();
         	}
            // Get Private/Active BAs
            String  privateBA = aRequest.getParameter("private_ba");
            String  activeBA  = aRequest.getParameter("active_ba");
            boolean pBA, aBA;

            if ((privateBA == null) || privateBA.trim().equals("") || privateBA.trim().equals("false")) {
                pBA = false;
            } else {
                pBA = true;
            }

            if ((activeBA == null) || activeBA.trim().equals("") || activeBA.trim().equals("false")) {
                aBA = false;
            } else {
                aBA = true;
            }

            ba.setName(name);
            ba.setDisplayName(displayName);
            ba.setSystemPrefix(requestPrefix);
            ba.setDescription(description);

            if ((location != null) && location.equalsIgnoreCase("location_nyc")) {
                ba.setLocation("nyc");
            }

            if ((location != null) && location.equalsIgnoreCase("location_hyd")) {
                ba.setLocation("hyd");
            }

            if ((volunteer != null) && volunteer.equalsIgnoreCase("volunteer_round")) {
                sc.setVolunteer(RR_VOLUNTEER);
            }else if ((volunteer != null) && volunteer.equalsIgnoreCase("volunteer_random")) {
                sc.setVolunteer(RANDOM_VOLUNTEER);
            }
            else
            	sc.setVolunteer(NO_VOLUNTEER);

            sc.setAssignToAll(assAny);
            sc.setLegacyPrefixList(legacyPrefixList);

            // Set Duedate Options
            if ((duedateOption != null) && duedateOption.equalsIgnoreCase("disabled")) {
                sc.setIsDueDateDisabled(true);
                sc.setIsTimeDisabled(false);
            }

            if ((duedateOption != null) && duedateOption.equalsIgnoreCase("allownull")) {
                sc.setIsDueDateDisabled(false);
                sc.setAllowNullDueDate(true);
                sc.setDefaultDueDate(dueDateValue);
                sc.setIsTimeDisabled(dueTime);
            }

            if ((duedateOption != null) && duedateOption.equalsIgnoreCase("mandatory")) {
                sc.setIsDueDateDisabled(false);
                sc.setAllowNullDueDate(false);
                sc.setDefaultDueDate(dueDateValue);
                sc.setIsTimeDisabled(dueTime);
            }

            // Set email options
            ba.setEmail(email);
            ba.setMaxEmailActions(emailActions);
            ba.setIsEmailActive(emailReq);
            sc.setAdministrator(contact);
            sc.setNotifyAppender(emailAct);
            sc.setEmailDateFormat(dateFormatId);
            sc.setListDateFormat(dateFormatId);
            
            sc.setPreferredZone(timeZoneId);

            // Add Request/ Add Action defaults
            sc.setRequestNotify(nAR);
            sc.setActionNotify(nAA);
            sc.setRequestNotifyLoggers(nARL);
            sc.setActionNotifyLoggers(nAAL);

            // Now set help links
            CustomLink            cl        = new CustomLink(helpDisplay, helpLink);
            ArrayList<CustomLink> helpLinks = sc.getCustomLinks();

            if (helpLinks == null) {
                helpLinks = new ArrayList<CustomLink>();
            }

            boolean helpFlag = false;

            for (CustomLink link : helpLinks) {
                if (link.getName().equalsIgnoreCase(helpDisplay)) {
                    helpFlag = true;
                    link.setValue(helpLink);
                }
            }

            if ((helpFlag == false) &&!helpDisplay.equals("")) {
                helpLinks.add(cl);
            }

            sc.setCustomLinks(helpLinks);
            ba.setIsPrivate(pBA);
            ba.setIsActive(aBA);
        }

        // Severity Options
        String incomingHigh = aRequest.getParameter("incoming_high_value");
        String incomingLow  = aRequest.getParameter("incoming_low_value");

        // Outgoing options
        String severityList = aRequest.getParameter("severity_list");

        // Severity List
        ArrayList<String> outgoingLowSeverityList  = new ArrayList<String>();
        ArrayList<String> outgoingHighSeverityList = new ArrayList<String>();
        StringTokenizer   st                       = new StringTokenizer(severityList, ",");

        while (st.hasMoreTokens()) {
            String currentSeverity = st.nextToken();

            if (currentSeverity != null) {

                // Get outgoing severities
                String outgoingSeverity = aRequest.getParameter(currentSeverity.trim() + "_low");

                if ((outgoingSeverity != null) && (!outgoingSeverity.trim().equals("")) && (!outgoingSeverity.trim().equals("false"))) {
                    outgoingLowSeverityList.add(currentSeverity);
                }

                // Get outgoing severities
                outgoingSeverity = aRequest.getParameter(currentSeverity.trim() + "_high");

                if ((outgoingSeverity != null) && (!outgoingSeverity.trim().equals("")) && (!outgoingSeverity.trim().equals("false"))) {
                    outgoingHighSeverityList.add(currentSeverity);
                }
            }
        }

        sc.setOutgoingSeverityHigh(outgoingHighSeverityList);
        sc.setOutgoingSeverityLow(outgoingLowSeverityList);
        sc.setIncomingSeverityLow(incomingLow);
        sc.setIncomingSeverityHigh(incomingHigh);
        ba.setSysConfigObject(sc);
        BusinessArea.update(ba);
        Mapper.refreshBOMapper();

        String forwardUrl = "/admin-properties/" + requestPrefix;

        aResponse.sendRedirect(WebUtil.getServletPath(aRequest, forwardUrl));

        return;
    }

    //~--- get methods --------------------------------------------------------

    private String getDateTimeFormats(int aFormat) {
        StringBuilder             buffer = new StringBuilder();
        ArrayList<DateTimeFormat> dtf    = DateTimeFormat.getAllDateTimeFormats();

        for (DateTimeFormat dt : dtf) {
            String format   = dt.getFormat();
            int    formatId = dt.getFormatId();

            buffer.append("<OPTION value='").append(formatId).append("' ");

            if (formatId == aFormat) {
                buffer.append(" SELECTED ");
            }

            buffer.append(">").append(format);
            buffer.append("</OPTION>\n");
        }

        return buffer.toString();
    }

    private void getDatetimeOptions(SysConfig aSC, Hashtable<String, String> aTagTable) {
        StringBuilder buffer           = new StringBuilder();
        boolean       disableDueDate   = aSC.getIsDueDateDisabled();
        boolean       allowNullDueDate = aSC.getAllowNullDueDate();

        buffer.append("<OPTION value='").append("disabled").append("' ");

        if (disableDueDate == true) {
            buffer.append(" SELECTED ");
            aTagTable.put("due_date_display", "none");
        }

        buffer.append(">").append("Disable Due Date");
        buffer.append("</OPTION>\n");
        buffer.append("<OPTION value='").append("allownull").append("' ");

        if ((disableDueDate == false) && (allowNullDueDate == true)) {
            buffer.append(" SELECTED ");
        }

        buffer.append(">").append("Allow Null DueDate");
        buffer.append("</OPTION>\n");
        buffer.append("<OPTION value='").append("mandatory").append("' ");

        if ((disableDueDate == false) && (allowNullDueDate == false)) {
            buffer.append(" SELECTED ");
        }

        buffer.append(">").append("Mandatory DueDate");
        buffer.append("</OPTION>\n");
        aTagTable.put("due_date_options", buffer.toString());
    }

    private String getOutgoingSeverityList(int aSystemId, ArrayList<String> aSevList, boolean outgoingOption, ArrayList<String> aAdminList) throws DatabaseException {
        String          extension = null;
        StringBuilder   buffer    = new StringBuilder();
        ArrayList<Type> types     = Type.lookupBySystemIdAndFieldName(aSystemId, Field.SEVERITY);

        if (outgoingOption == true) {
            extension = "_high";
        } else {
            extension = "_low";
        }

        boolean first = true;

        for (Type type : types) {
            if (first == false) {
                buffer.append("&nbsp;&nbsp;&nbsp;&nbsp;");
            }

            first = false;
            buffer.append("<INPUT class=\"sx\" ").append(" type=\"checkbox\"").append("id=\"").append(type.getName()).append(extension).append("\"").append(" name=\"").append(type.getName()).append(
                extension).append("\"");

            if ((aAdminList.contains("SUPER_ADMIN") == false)) {
                buffer.append(" disabled");
            }

            if (aSevList.contains(type.getName()) == true) {
                buffer.append(" checked ");
            }

            buffer.append(">\n").append(type.getDisplayName());
        }

        return buffer.toString();
    }

    private String getSeverityList(int aSystemId, String aSelected, StringBuilder aSeverityList) throws DatabaseException {
        StringBuilder   buffer = new StringBuilder();
        ArrayList<Type> types  = Type.lookupBySystemIdAndFieldName(aSystemId, Field.SEVERITY);

        for (Type type : types) {
            buffer.append("<OPTION value='").append(type.getName()).append("' ");

            if (type.getName().equalsIgnoreCase(aSelected)) {
                buffer.append(" SELECTED ");
            }

            buffer.append(">").append(type.getDisplayName());

            if (type.getIsPrivate() == true) {
                buffer.append("&dagger;");
            }

            buffer.append("</OPTION>\n");
            aSeverityList.append(type.getName()).append(",");
        }

        return buffer.toString();
    }

    private String getTimezones(int aSystemId, int aPreferredZone) throws DatabaseException {
        StringBuilder buffer = new StringBuilder();

        buffer.append("<OPTION value='").append(SITE_ZONE).append("' ");

        if (aPreferredZone == SITE_ZONE) {
            buffer.append(" SELECTED ");
        }

        buffer.append(">").append("Server");
        buffer.append("</OPTION>\n");
        buffer.append("<OPTION value='").append(LOCAL_ZONE).append("' ");

        if (aPreferredZone == LOCAL_ZONE) {
            buffer.append(" SELECTED ");
        }

        buffer.append(">").append("Browser");
        buffer.append("</OPTION>\n");
        buffer.append("<OPTION value='").append(GMT_ZONE).append("' ");

        if (aPreferredZone == GMT_ZONE) {
            buffer.append(" SELECTED ");
        }

        buffer.append(">").append("GMT");
        buffer.append("</OPTION>\n");

        return buffer.toString();
    }
}
