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
 * TBitsAdmin.java
 *
 * $Header:
 */
package transbit.tbits.admin;

//~--- non-JDK imports --------------------------------------------------------
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

import transbit.tbits.Helper.AD2TBitsSync;
import transbit.tbits.Helper.Messages;
import transbit.tbits.Helper.TBitsConstants;
import transbit.tbits.Helper.TBitsPropEnum;
import transbit.tbits.admin.common.AdminProxyServlet;
import transbit.tbits.admin.common.MenuItem;
import transbit.tbits.admin.common.NavMenu;
import transbit.tbits.admin.common.URLRegistry;
import transbit.tbits.api.Mapper;
import transbit.tbits.authentication.AuthConstants;
import transbit.tbits.authentication.AuthUtils;
import transbit.tbits.common.DTagReplacer;
import transbit.tbits.common.DatabaseException;
import transbit.tbits.common.PropertiesHandler;
import transbit.tbits.common.TBitsLogger;
import transbit.tbits.common.Utilities;
import transbit.tbits.config.WebConfig;
import transbit.tbits.domain.Field;
import transbit.tbits.domain.NotificationRule;
import transbit.tbits.domain.Role;
import transbit.tbits.domain.RoleUser;
import transbit.tbits.domain.Type;
import transbit.tbits.domain.TypeUser;
import transbit.tbits.domain.User;
import transbit.tbits.domain.UserType;
import transbit.tbits.exception.TBitsException;
import transbit.tbits.webapps.WebUtil;

//~--- classes ----------------------------------------------------------------

/**
 * This is the servlet for rendering the Users page in admin.
 *
 * @author  : Vinod Gupta
 * @version : $Id: $
 */
public class AdminAllUsers extends HttpServlet implements TBitsConstants, TBitsPropEnum {
                                                             
    // Logger that logs information/error messages to the Application Log.
    public static final TBitsLogger LOG        = TBitsLogger.getLogger(PKG_ADMIN);
    private static final int        USERS      = 4;
    private static final String     HTML_ROLES = "web/tbits-admin-users-roles.htm";
    private static final String     HTML_CAT   = "web/tbits-admin-users-categories.htm";
    private static final String     HTML_DETAILS   = "web/tbits-admin-user-details-edit.htm";
    private static final String     HTML_CHPASSWD   = "web/tbits-admin-user-change-password.htm";
    
    // HTML Interfaces used to display the admin-users pages in Tbits.
    private static final String HTML = "web/tbits-admin-allusers.htm";

    static
    {
    	 //urls
        String url = "allusers";
    	String completeURL = url + ".admin";
    	
        //Create Mapping
		URLRegistry.getInstance().addMapping(AdminProxyServlet.class, url, AdminAllUsers.class);
		
		//Create Menu
		NavMenu nav = NavMenu.getInstance();
		nav.AppMenu.add(new MenuItem("All Users", completeURL, "The administration (Add/Delete/Update) of fields of the Business Area."));
    }
    //~--- methods ------------------------------------------------------------

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
        PrintWriter out     = aResponse.getWriter();
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

            return;
        } catch (TBitsException de) {
            aResponse.sendRedirect(WebUtil.getServletPath(aRequest, "/error"));

            return;
        }

        return;
    }
    
    /**
     * If the object is null, it return ""
     *
     */
    public static String GetString(Object obj)
    {
        if(obj == null)
            return "";
        return obj.toString();
    }
    /**
     *    Method that actually handles the Get Request.
     *
     *
     *    @param aRequest          the HttpServlet Request Object
     *    @param aResponse         the HttpServlet Response Object
     *    @exception ServletException
     *    @exception IOException
     *    @exception TBitsExceptionception DatabaseException
     *    @exception FileNotFoundException
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
        int                       userId     = user.getUserId();
        String                     strUserPage   = aRequest.getParameter("userPage");
        String                     strUserId     = null;
        if(!RoleUser.isSuperUser(userId))
            throw new TBitsException(Messages.getMessage("ADMIN_NO_PERMISSION"));
        if ((strUserPage != null) && strUserPage.equalsIgnoreCase("details")) {
            strUserId = aRequest.getParameter("user_id");
            int selectedUserId = Integer.parseInt(strUserId);
            User selectedUser = User.lookupAllByUserId(selectedUserId);
            if(selectedUser == null)
                throw new TBitsException(Messages.getMessage("INVALID_TRANSBIT_USER"));
            DTagReplacer hp1  = new DTagReplacer(HTML_DETAILS);
            hp1.replace("user_login", selectedUser.getUserLogin());
            hp1.replace("user_login_disabled", "disabled");
            if(selectedUser.getIsActive())
            	hp1.replace("is_active", "checked");
            else
                hp1.replace("is_active", "");
            hp1.replace("first_name", selectedUser.getFirstName());
            hp1.replace("last_name", selectedUser.getLastName());
            hp1.replace("display_name", selectedUser.getDisplayName());
            hp1.replace("email_id", selectedUser.getEmail());
            hp1.replace("user_type", Integer.toString(selectedUser.getUserTypeId()));
            hp1.replace("mobile_number", selectedUser.getMobile());
            hp1.replace("nearestPath", WebUtil.getNearestPath(aRequest, ""));
            hp1.replace("location", selectedUser.getLocation()) ;
            hp1.replace("firm_code", selectedUser.getFirmCode());
            hp1.replace("designation", selectedUser.getDesignation()) ;
            hp1.replace("firm_address", selectedUser.getFirmAddress()) ;
            hp1.replace("sex", selectedUser.getSex()) ;
            hp1.replace("full_firm_name", selectedUser.getFullFirmName()) ;
            hp1.replace("submit_disabled", "");
            hp1.replace("revert_disabled", "");
            hp1.replace("page_name", "details");
            hp1.replace("message", "Update User");


        hp1.replace("selected_user_id", Integer.toString(selectedUser.getUserId()));
            out.println(hp1.parse(0));
            return;
        }
     
         if ((strUserPage != null) && strUserPage.equalsIgnoreCase("newuserpage")) {
            DTagReplacer hp1  = new DTagReplacer(HTML_DETAILS);
             hp1.replace("user_login", "");
             hp1.replace("user_login_disabled", "");
             hp1.replace("is_active", "checked");
             hp1.replace("first_name", "");
             hp1.replace("last_name", "");
             hp1.replace("display_name", "");
             hp1.replace("email_id", "");
             hp1.replace("user_type", Integer.toString(7));
             hp1.replace("mobile_number", "");
             hp1.replace("location", "") ;
             hp1.replace("firm_code", "");
             hp1.replace("designation", "");
             hp1.replace("firm_address", "") ;
             hp1.replace("sex", "") ;
             hp1.replace("full_firm_name", "") ;
             hp1.replace("nearestPath", WebUtil.getNearestPath(aRequest, ""));
             hp1.replace("submit_disabled", "");
             hp1.replace("revert_disabled", "disabled");
             hp1.replace("page_name", "newuserpage");
             hp1.replace("message", "Create New User");
             hp1.replace("selected_user_id", "");
             out.println(hp1.parse(0));
            return;
        }
        
         if ((strUserPage != null) && strUserPage.equalsIgnoreCase("changepasswordpage")) {
            strUserId = aRequest.getParameter("user_id");
            int selectedUserId = Integer.parseInt(strUserId);
            User selectedUser = User.lookupAllByUserId(selectedUserId);
            
            DTagReplacer hp1  = new DTagReplacer(HTML_CHPASSWD);
            hp1.replace("user_login", selectedUser.getUserLogin());
            hp1.replace("first_name", selectedUser.getFirstName());
            hp1.replace("last_name", selectedUser.getLastName());
            hp1.replace("display_name", selectedUser.getDisplayName());
            hp1.replace("email_id", selectedUser.getEmail());
            hp1.replace("user_type", Integer.toString(selectedUser.getUserTypeId()));
            hp1.replace("nearestPath", WebUtil.getNearestPath(aRequest, ""));            
            hp1.replace("change_passwd_disabled", WebUtil.getNearestPath(aRequest, ""));            
            hp1.replace("cancel_disabled", WebUtil.getNearestPath(aRequest, ""));    
            hp1.replace("page_name", "changepasswordpage");
            hp1.replace("message", "Change Password");
	    hp1.replace("selected_user_id", Integer.toString(selectedUser.getUserId()));
            out.println(hp1.parse(0));
            return;
        }
        
        WebConfig                 userConfig = user.getWebConfigObject();
        
//        Hashtable<String, Object> paramTable = AdminUtil.getRequestParams(aRequest, userConfig, USERS);
//        BusinessArea              ba         = (BusinessArea) paramTable.get(Field.BUSINESS_AREA);
//
//        if (ba == null) {
//            throw new TBitsException(Messages.getMessage("INVALID_BUSINESS_AREA"));
//        }
	
//        int       systemId  = ba.getSystemId();
//        String    sysPrefix = ba.getSystemPrefix();
//        SysConfig sc        = ba.getSysConfigObject();
        int    baUserId      = 0;
        
        // Check Basic Permissions to come to this page
//        ArrayList<String> adminList = AdminUtil.checkBasicPermissions(systemId, userId, USERS);

        // Get BusinessArea List in which the user has permissions to view
        // the admin page.
//        String          baList  = AdminUtil.getSysIdList(systemId, userId);
        ArrayList<User> baUsers = User.getAllUsers(); //BAUser.getBusinessAreaUsers(systemId);

        User.setSortParams(2, 0);
        baUsers = User.sort(baUsers);

        // Get all the type-fields.
//        String typeFieldHtml = AdminUtil.getTypeFields(systemId);
        String baUserHtml    = getBAUserHtml(baUsers);

        if (baUsers.size() != 0) {
            baUserId = baUsers.get(0).getUserId();
        }

//        ArrayList<TypeUser>        typeUserList  = null;
//        Hashtable<Integer, String> typeUserTable = null;
        
//        typeUserList  = TypeUser.lookupTypeUsersBySystemIdAndUserId(systemId, baUserId);
//        typeUserTable = getTypeUserTable(typeUserList);

//        String categoryUserHtml = getCategoryUserHtml(typeUserTable, systemId);
//        String location         = ba.getLocation();

        //
        // Now, its time to read the Html Template, replace all the tags
        // with appropriate values and then output it.
        //
        DTagReplacer hp = new DTagReplacer(HTML);

//        hp.replace("sys_ids", baList);
        hp.replace("cssFile", WebUtil.getCSSFile("tbits.css", "", false));
//        hp.replace("display_name", ba.getDisplayName());
        hp.replace("user_ids", baUserHtml);
        hp.replace("title", "TBits Admin: Users");
        hp.replace("nearestPath", WebUtil.getNearestPath(aRequest, ""));
		hp.replace("userLogin", user.getUserLogin());

		String display_logout = "none";
		if(aRequest.getAuthType() == AuthConstants.AUTH_TYPE)
			display_logout = "";
		hp.replace("display_logout", display_logout);

//        if ((adminList.contains("SUPER_ADMIN") == false) && (adminList.contains("PERMISSION_ADMIN") == false)) {
//            hp.replace("superuser_display", "none");
//        } else {
//            hp.replace("superuser_display", "");
//        }
        
        String trnProperty = PropertiesHandler.getAppProperties().getProperty("transbit.tbits.transmittal");
        if ((trnProperty == null) || Boolean.parseBoolean(trnProperty) == false)
        	hp.replace("trn_display", "none");
        else
        	hp.replace("trn_display", "");

//        if (adminList.contains("SUPER_ADMIN") == false) {
//            hp.replace("web_profile_display", "none");
//        } else {
//            hp.replace("web_profile_display", "");
//        }

//        String myDomain = "hyd";
//
//        try {
//            myDomain = PropertiesHandler.getProperty(KEY_DOMAIN).toLowerCase();
//        } catch (Exception e) {
//            myDomain = "hyd";
//        }

//        if (myDomain.toLowerCase().equals("hyd")) {
//            hp.replace("instanceBoldHyd", " b ");
//            hp.replace("instanceBoldNyc", " ");
//            hp.replace("instancePathHyd", WebUtil.getServletPath(aRequest, "/search/" + ba.getSystemPrefix()));
//            hp.replace("instancePathNyc", WebUtil.getServletPath(aRequest, "/search/" + ba.getSystemPrefix()));
//        } else {
//            hp.replace("instanceBoldHyd", " ");
//            hp.replace("instanceBoldNyc", " b ");
//            hp.replace("instancePathHyd", WebUtil.getServletPath(aRequest, "/search/" + ba.getSystemPrefix()));
//            hp.replace("instancePathNyc", WebUtil.getServletPath(aRequest, "/search/" + ba.getSystemPrefix()));
//        }

//        DTagReplacer hp1 = new DTagReplacer(HTML_CAT);

        hp.replace("nearestPath", WebUtil.getNearestPath(aRequest, ""));
//        hp.replace("baAdminList", AdminUtil.getBAAdminEmailList());
//        hp1.replace("ba_categories_prop", categoryUserHtml);
        //hp.replace("divReplacement", hp1.parse(systemId));
        hp.replace("submit_disabled", "");
//        hp.replace("allTypeFields", typeFieldHtml);
        out.println(hp.parse(0));
    }

    private void handlePostRequest(HttpServletRequest aRequest, HttpServletResponse aResponse) throws ServletException, IOException, TBitsException, DatabaseException, FileNotFoundException {
        aResponse.setContentType("text/html");

        PrintWriter out     = aResponse.getWriter();
        HttpSession session = aRequest.getSession();
        String ERROR = "ERROR:";
        String OK = "OK:";
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
        
        if(!RoleUser.isSuperUser(userId))
        {
            out.println(ERROR + Messages.getMessage("ADMIN_NO_PERMISSION"));
            return;
            //throw new TBitsException(Messages.getMessage("ADMIN_NO_PERMISSION"));
        }
        
        String pageName = GetString(aRequest.getParameter("page_name"));
        if(pageName.equalsIgnoreCase("details"))
        {
            //It means it is an update
            String selectedUserIdStr =  GetString(aRequest.getParameter("selected_user_id"));
            String firstName = GetString(aRequest.getParameter("first_name"));
            String lastName = GetString(aRequest.getParameter("last_name"));
            String displayName = GetString(aRequest.getParameter("display_name"));
            String emailId = GetString(aRequest.getParameter("email_id"));
            String isActiveStr = GetString(aRequest.getParameter("is_active"));
            String userTypeStr = GetString(aRequest.getParameter("user_type"));
            String mobileNumber = GetString(aRequest.getParameter("mobile_number"));
            String location = GetString(aRequest.getParameter("location")) ;
            String firm_code = GetString(aRequest.getParameter("firm_code")) ;
            String designation = GetString(aRequest.getParameter("designation")) ;
            String firm_address = GetString(aRequest.getParameter("firm_address")) ;
            String sex = GetString(aRequest.getParameter("sex")) ;
            String full_firm_name = GetString(aRequest.getParameter("full_firm_name")) ;
            int userType = 0;
            try{
                userType = Integer.parseInt(userTypeStr);
            }
            catch(NumberFormatException fe)
            {
                out.print(ERROR + "User type is invalid.");
                return;
            }
	    int selectedUserId = 0;
	    try {
		selectedUserId = Integer.parseInt(selectedUserIdStr);
	  	}
            catch(NumberFormatException fe)
            {
                out.print(ERROR + "Invalid User.");
                return;
		}


            boolean isActive = false;
            if(isActiveStr.equalsIgnoreCase("true"))
                isActive = true;
            User selectedUser = User.lookupAllByUserId(selectedUserId);
            if(selectedUser == null)
            {
                out.print(ERROR + "No such user exists.");
                return;
            }
            selectedUser.setFirstName(firstName);
            selectedUser.setLastName(lastName);
            selectedUser.setDisplayName(displayName);
            selectedUser.setEmail(emailId);
            selectedUser.setIsActive(isActive);
            selectedUser.setUserTypeId(userType);
            selectedUser.setMobile(mobileNumber);
            selectedUser.setLocation(location) ;
            selectedUser.setFirmCode(firm_code) ;
            selectedUser.setDesignation(designation) ;
            selectedUser.setFirmAddress(firm_address) ;
            selectedUser.setSex(sex) ;
            selectedUser.setFullFirmName(full_firm_name) ;
            User.update(selectedUser);
            out.print(OK + "Account Updated.");
            return;
        }
        else if(pageName.equalsIgnoreCase("newuserpage"))
        {
            //Means new user creation
            String selectedUserLoginStr =  GetString(aRequest.getParameter("user_login"));
            String firstName = GetString(aRequest.getParameter("first_name"));
            String lastName = GetString(aRequest.getParameter("last_name"));
            String displayName = GetString(aRequest.getParameter("display_name"));
            String emailId = GetString(aRequest.getParameter("email_id"));
            String isActiveStr = GetString(aRequest.getParameter("is_active"));
            String userTypeStr = GetString(aRequest.getParameter("user_type"));
            String mobileNumber = GetString(aRequest.getParameter("mobile_number"));
            String location = GetString(aRequest.getParameter("location")) ;
            String firm_code = GetString(aRequest.getParameter("firm_code")) ;
            String designation = GetString(aRequest.getParameter("designation")) ;
            String firm_address = GetString(aRequest.getParameter("firm_address")) ;
            String sex = GetString(aRequest.getParameter("sex")) ;
            String full_firm_name = GetString(aRequest.getParameter("full_firm_name")) ;
            String other_emails = GetString(aRequest.getParameter("other_emails")) ;

            
            int userType = 0;
            try{
                userType = Integer.parseInt(userTypeStr);
            }
            catch(NumberFormatException fe)
            {
                out.print(ERROR + "User type is invalid.");
                return;
            }
            boolean isActive = false;
            if(isActiveStr.equalsIgnoreCase("true"))
                isActive = true;
            boolean  doesUserExist = User.doesUserAlreadyExist(selectedUserLoginStr);

            if(doesUserExist)
            {
                out.print(ERROR + "User already exists.");
                return;
            }
            User newUser = new User(-1, selectedUserLoginStr, firstName, lastName, displayName, emailId, 
                    isActive, userType, (new WebConfig()).xmlSerialize(), "", false, false, "", "", "", "", "", "", location, "", mobileNumber, "", firm_code, designation, firm_address, sex, full_firm_name, other_emails);
            try{
            AD2TBitsSync.insertUser(newUser);
            }
            catch(Exception exp)
            {
                out.print(ERROR + exp.getMessage());
                return;
            }
            Mapper.refreshUserMapper();
            //User.update(newUser);
            out.print(OK + "User Created.");
            return;
        }
        else if(pageName.equalsIgnoreCase("changepasswordpage"))
        {
        	String userLogin =  GetString(aRequest.getParameter("user_login"));
        	String password = GetString(aRequest.getParameter("password"));
        	try
        	{
        		AuthUtils.setPassword(userLogin, password);
        	}
        	catch(Exception exp)
        	{
        		out.print(ERROR + exp.getMessage());
        		return;
        	}
        	out.print(OK + "Password Updated.");
        	return;
        }
        else
        {
            out.print(ERROR + Messages.getMessage("NO_SUCH_FORM")); 
            return;
           //throw new TBitsException(Messages.getMessage("NO_SUCH_FORM")); 
        }
       
        /*
        BusinessArea ba = (BusinessArea) paramTable.get(Field.BUSINESS_AREA);
        
        if (ba == null) {
            throw new TBitsException(Messages.getMessage("INVALID_BUSINESS_AREA"));
        }
        
        
        int               systemId  = ba.getSystemId();
        ArrayList<String> adminList = AdminUtil.checkBasicPermissions(systemId, userId, USERS);
        String            sysPrefix = ba.getSystemPrefix();
        String            strUserId;
        String            newUser             = aRequest.getParameter("new_user");
        String            duplicateUserLogins = "";
        String            invalidUserLogins   = "";
        String            outputMessage       = "";
        int               invalidCount        = 0;
        int               duplicateCount      = 0;

        if ((newUser != null) && (newUser.equalsIgnoreCase("true") == true)) {
            ArrayList<User> baUsers      = User.getAllUsers();//BAUser.getBusinessAreaUsers(systemId);
            String          strUserLogin = aRequest.getParameter("user_login");
            String[]        strUserArray = strUserLogin.split("[;,]");

            for (int i = 0; i < strUserArray.length; i++) {
                if ((strUserArray[i] != null) &&!strUserArray[i].trim().equalsIgnoreCase("")) {
                    strUserLogin = strUserArray[i];

                    User newUserObject = User.lookupByUserLogin(strUserLogin);

                    if (newUserObject == null) {
                        invalidUserLogins = invalidUserLogins + strUserLogin + ";";
                        invalidCount++;
                    } else if (baUsers.contains(newUserObject) == false) {
                        BAUser baUser = new BAUser(systemId, newUserObject.getUser(), true);

                        BAUser.insert(baUser);
                        Mapper.refreshBOMapper();
                    } else {
                        duplicateUserLogins = duplicateUserLogins + strUserLogin + ";";
                        duplicateCount++;
                    }
                }
            }

            if (!invalidUserLogins.equalsIgnoreCase("")) {
                if (invalidCount > 1) {
                    outputMessage = outputMessage + Messages.getMessage("INVALID_USER_LOGIN", invalidUserLogins, "are", "s");
                } else {
                    outputMessage = outputMessage + Messages.getMessage("INVALID_USER_LOGIN", invalidUserLogins, "is", "");
                }

                outputMessage += "\n";
            }

            if (!duplicateUserLogins.equalsIgnoreCase("")) {
                if (duplicateCount > 1) {
                    outputMessage = outputMessage + Messages.getMessage("DUPLICATE_USER_LOGIN", duplicateUserLogins, "are", "s");
                } else {
                    outputMessage = outputMessage + Messages.getMessage("DUPLICATE_USER_LOGIN", duplicateUserLogins, "is", "");
                }
            }

            if (invalidUserLogins.equalsIgnoreCase("") && duplicateUserLogins.equalsIgnoreCase("")) {
                outputMessage = "success";
            }

            out.println(outputMessage);

            return;
        } else if ((newUser != null) && (newUser.equalsIgnoreCase("false") == true)) {
            String delUserId = aRequest.getParameter("user_id");

            if (!adminList.contains("SUPER_ADMIN") && (Integer.parseInt(delUserId) == user.getUser())) {
                out.println(Messages.getMessage("ADMIN_DELETE_NO_PERMISSION"));

                return;
            }

            BAUser baUser = new BAUser(systemId, Integer.parseInt(delUserId), true);

            BAUser.delete(baUser);
            Mapper.refreshBOMapper();
            out.println("success");

            return;
        }

        String strWebProfile = aRequest.getParameter("webConfig");

        if (strWebProfile != null) {
            strUserId = aRequest.getParameter("user_id");

            int  tempUserId = Integer.parseInt(strUserId);
            User tempUser   = User.lookupByUserId(tempUserId);

            tempUser.setWebConfig(strWebProfile);
            User.update(tempUser);

            return;
        }

        String userPage = aRequest.getParameter("userPage");

        if ((userPage != null) && (userPage.equalsIgnoreCase("roles") == true)) {
            strUserId = aRequest.getParameter("user_id");

            int  tempUserId = Integer.parseInt(strUserId);
            User tempUser   = User.lookupByUserId(tempUserId);

            updateRoleUserList(systemId, tempUserId, aRequest);
            Mapper.refreshBOMapper();

            return;
        }

        if ((userPage != null) && (userPage.equalsIgnoreCase("categories") == true)) {
            strUserId = aRequest.getParameter("user_id");

            int  tempUserId = Integer.parseInt(strUserId);
            User tempUser   = User.lookupByUserId(tempUserId);

            updateTypeUserList(systemId, tempUserId, aRequest);
            Mapper.refreshBOMapper();

            return;
        }
        */
        
        //Mapper.refreshBOMapper();

        //String forwardUrl = "admin-allusers/" + sysPrefix;

        //aResponse.sendRedirect(WebUtil.getServletPath(forwardUrl));

        //return;
    }

    private void updateRoleUserList(int aSystemId, int aUserId, HttpServletRequest aRequest) throws DatabaseException {
        ArrayList<String> roles    = Role.getUserRolesBySysIdAndUserId(aSystemId, aUserId);
        RoleUser          roleUser = null;
        String            checked  = null;

        for (String string : roles) {
            String[] splitStr      = string.split(",");
            String   roleName      = splitStr[0];
            String   previousValue = splitStr[1];
            boolean  oldValue      = false;

            if (previousValue.equalsIgnoreCase("true")) {
                oldValue = true;
            }

            String strRoleId = splitStr[2];
            int    roleId    = Integer.parseInt(strRoleId);

            checked = aRequest.getParameter(roleName);

            boolean check;

            if ((checked != null) && (!checked.trim().equals("")) && (!checked.trim().equals("false"))) {
                check = true;
            } else {
                check = false;
            }

            if ((oldValue == false) && (check == true)) {

                // insert RoleUser;
                roleUser = new RoleUser(aSystemId, roleId, aUserId, true);
                RoleUser.insert(roleUser);
                Mapper.refreshBOMapper();
            }

            if ((oldValue == true) && (check == false)) {

                // delete RoleUser;
                roleUser = new RoleUser(aSystemId, roleId, aUserId, true);
                RoleUser.delete(roleUser);
                Mapper.refreshBOMapper();
            }
        }
    }

    private void updateTypeUserList(int aSystemId, int aUserId, HttpServletRequest aRequest) throws DatabaseException {
        ArrayList<TypeUser>        typeUserList = TypeUser.lookupTypeUsersBySystemIdAndUserId(aSystemId, aUserId);
        ArrayList<Type>            categoryList = Type.lookupBySystemIdAndFieldName(aSystemId, Field.CATEGORY);
        Hashtable<Integer, String> tUTable      = getTypeUserTable(typeUserList);
        String                     assignee     = null;
        String                     notification = null;
        String                     volunteer    = null;
        String                     category     = null;
        String[]                   typeUser     = null;
        int                        userTypeId;
        int                        size = categoryList.size();
        Type                       type = null;
        int                        typeId;

        for (int i = 0; i < size; i++) {
            type       = categoryList.get(i);
            typeId     = type.getTypeId();
            category   = type.getName();
            userTypeId = 0;

            String strTypeUser = tUTable.get(typeId);

            if (strTypeUser != null) {
                typeUser = strTypeUser.split(",");
            }

            assignee = aRequest.getParameter(category + "_ass");

            boolean ass;

            if ((assignee != null) && (!assignee.trim().equals("")) && (!assignee.trim().equals("false"))) {
                ass = true;
            } else {
                ass = false;
            }

            boolean vol            = false;
            int     notificationId = 0;

            volunteer = aRequest.getParameter(category + "_vol");

            if ((volunteer != null) && (!volunteer.trim().equals("")) && (!volunteer.trim().equals("false"))) {
                vol = true;
            } else {
                vol = false;
            }

            notification   = aRequest.getParameter(category + "_select");
            notificationId = Integer.parseInt(notification);

            TypeUser tu = null;

            if ((strTypeUser == null) && ((ass != false) || (notificationId != 1))) {
                if (ass == true) {
                    userTypeId = UserType.ASSIGNEE;
                }

                tu = new TypeUser(aSystemId, 3, type.getTypeId(), aUserId, userTypeId, notificationId, vol, false, true);
                TypeUser.insert(tu);
            }

            if ((strTypeUser != null) && ((ass == false) && (notificationId == 1))) {

                // if(typeUser[0].equalsIgnoreCase("true"))
                // userTypeId = UserType.ASSIGNEE;
                tu = new TypeUser(aSystemId, 3, type.getTypeId(), aUserId, userTypeId, 0, false, false, true);
                TypeUser.delete(tu);
            }

            if ((strTypeUser != null) && ((ass == true) || (notificationId != 1))) {
                if (ass == true) {
                    userTypeId = UserType.ASSIGNEE;
                } else {
                    userTypeId = 0;
                }

                tu = new TypeUser(aSystemId, 3, type.getTypeId(), aUserId, userTypeId, notificationId, vol, false, true);
                TypeUser.update(tu);
            }

            if ((strTypeUser != null) && ((ass == false) && (notificationId != 1))) {
                userTypeId = 0;
                tu         = new TypeUser(aSystemId, 3, type.getTypeId(), aUserId, userTypeId, notificationId, vol, false, true);
                TypeUser.update(tu);
            }
        }

        Mapper.refreshBOMapper();
    }

    //~--- get methods --------------------------------------------------------

    public static String getBAUserHtml(ArrayList<User> aBAUsers) throws DatabaseException {
        StringBuilder buffer = new StringBuilder();
        int           size   = aBAUsers.size();
        User          user   = null;

        for (int i = 0; i < size; i++) {
            user = (aBAUsers.get(i));
            buffer.append("<OPTION value='").append(user.getUserId()).append("' ");

            if (i == 0) {
                buffer.append(" SELECTED ");
            }

            buffer.append(">").append(user.getUserLogin());
            buffer.append("</OPTION>\n");
        }

        return buffer.toString();
    }

    private String getCategoryUserHtml(Hashtable<Integer, String> aTUTable, int aSystemId) throws DatabaseException {
        StringBuilder   buffer          = new StringBuilder();
        StringBuilder   strCategoryList = new StringBuilder();
        ArrayList<Type> categoryList    = Type.lookupBySystemIdAndFieldName(aSystemId, Field.CATEGORY);

        if (categoryList == null) {
            return buffer.toString();
        }

        int              size        = categoryList.size();
        Type             type        = null;
        String           strTypeUser = null;
        String           category    = null;
        String[]         typeUser    = null;
        NotificationRule nr          = null;
        int              typeId;

        for (int i = 0; i < size; i++) {
            type     = categoryList.get(i);
            typeId   = type.getTypeId();
            category = type.getName();
            strCategoryList.append(category + ",");
            strTypeUser = aTUTable.get(typeId);

            if (strTypeUser != null) {
                typeUser = strTypeUser.split(",");
            }

            buffer.append("<TR>\n").append("<TD class=\"sx\" noWrap\n>").append(type.getDisplayName()).append("</TD>").append("<TD class=\"sx\" >").append("<SELECT id=\"").append(category
                          + "_select").append("\" name=\"").append(category + "_select").append("\"  onchange=\"frmChanged()\"");
            buffer.append(">");

            if (strTypeUser == null) {
                buffer.append(getNotificationHtml(1));
            } else {
                buffer.append((getNotificationHtml(Integer.parseInt(typeUser[1]))));
            }

            buffer.append("</SELECT>").append("</TD>");
            buffer.append("<TD>").append("<INPUT class=\"chbox\" type=\"checkbox\" id=\"").append(category + "_ass").append("\" name=\"").append(category
                          + "_ass").append("\" ").append("onclick=\"javascript:").append(" onClickAssigneeBox(&quot;").append(category + "&quot;)\" ");

            if ((strTypeUser != null) && (typeUser[0].equalsIgnoreCase("true") == true)) {
                buffer.append(" checked");
            }

            buffer.append("></TD>");
            buffer.append("<TD>").append("<INPUT class=\"chbox\" type=\"checkbox\" id=\"").append(category + "_vol").append("\" name=\"").append(category
                          + "_vol").append("\" ").append("onclick=\"javascript:onClickVolunteerBox(&quot;").append(category + "&quot;)\" ");

            if ((strTypeUser != null) && (typeUser[0].equalsIgnoreCase("true") == true) && (typeUser[2].equalsIgnoreCase("true") == true)) {
                buffer.append(" checked ");
            }

            buffer.append("></TD>").append("</TR>");
        }

        buffer.append("<tr>").append("<td>").append("<input type=\"hidden\" id=\"categoryList\" ").append("name=\"categoryList\" value=\"").append(strCategoryList.toString()
                      + "\" ").append("></td></tr>");

        return buffer.toString();
    }

    private String getNotificationHtml(int aNotificationId) throws DatabaseException {
        StringBuilder               buffer = new StringBuilder();
        ArrayList<NotificationRule> nrList = NotificationRule.getAllNotificationRules();

        for (NotificationRule nr : nrList) {
            buffer.append("<OPTION value=\"").append(nr.getNotificationRuleId()).append("\" ");

            if (nr.getNotificationRuleId() == aNotificationId) {
                buffer.append(" selected ");
            }

            buffer.append(">").append(nr.getDisplayName()).append("</OPTION>");
        }

        return buffer.toString();
    }

    private Hashtable<Integer, String> getTypeUserTable(ArrayList<TypeUser> aTypeUserList) {
        Hashtable<Integer, String> typeUserTable = new Hashtable<Integer, String>();
        StringBuilder              buffer        = new StringBuilder();

        for (TypeUser tu : aTypeUserList) {
            if (tu.getUserTypeId() == UserType.ASSIGNEE) {
                typeUserTable.put(tu.getTypeId(), "true," + tu.getNotificationId() + "," + tu.getIsVolunteer());
            } else {
                typeUserTable.put(tu.getTypeId(), "false," + tu.getNotificationId() + "," + tu.getIsVolunteer());
            }
        }

        return typeUserTable;
    }

    private String getUserRoleHtml(int aSystemId, int aUserId, ArrayList<String> aAdminList) throws DatabaseException {
        StringBuilder     buffer    = new StringBuilder();
        StringBuilder     rolesList = new StringBuilder();
        ArrayList<String> roles     = Role.getUserRolesBySysIdAndUserId(aSystemId, aUserId);

        for (String string : roles) {
            String[] splitStr = string.split(",");
            String   roleName = splitStr[0];

            rolesList.append(roleName + ",");
            buffer.append("<tr>").append("<td class=\"sx\" noWrap>").append(roleName).append("</td>").append("<td class=\"sx\">").append("<input type=\"checkbox\" class=\"chbox\" ").append(
                "id=\"").append(roleName).append("\" name=\"").append(roleName).append("\" onchange=\"javascript:frmChanged()\"");

            if (splitStr[1].equalsIgnoreCase("true")) {
                buffer.append("checked");
            }

            if ((aAdminList.contains("SUPER_ADMIN") == false)) {
                if (roleName.equalsIgnoreCase("PermissionAdmin")) {
                    buffer.append(" disabled ");
                }

                if ((aAdminList.contains("PERMISSION_ADMIN") == false) && (roleName.equalsIgnoreCase("Admin") || roleName.equalsIgnoreCase("Manager"))) {
                    buffer.append(" disabled ");
                }
            }

            buffer.append("></td>").append("</tr>");
        }

        buffer.append("<tr>").append("<td>").append("<input type=\"hidden\" id=\"rolesList\" ").append("name=\"rolesList\" value=\"").append(rolesList.toString() + "\" ").append("></td></tr>");

        return buffer.toString();
    }

    private String getWebProfileHtml(int aUserId) throws DatabaseException {
        StringBuilder buffer    = new StringBuilder();
        User          user      = User.lookupByUserId(aUserId);
        String        webConfig = user.getWebConfig();

        buffer.append("<TABLE cellspacing=\"3\" cellpadding=\"0\" ").append("width=\"100%\" id=\"table1\">").append("<TR>").append("<TD valign=\"top\">").append(
            "<Textarea rows=\"23\" style=\"width:99%; class='sx' ").append("id=\"webConfig\" onchange=\"javascript:frmChanged()\" ").append(" name=\"webConfig\">").append(
            Utilities.htmlEncode(webConfig)).append("</textarea>\n").append("</td></tr>").append("</table>\n");

        return buffer.toString();
    }
}
