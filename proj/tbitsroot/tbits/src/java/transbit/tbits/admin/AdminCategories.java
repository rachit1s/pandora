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
import transbit.tbits.Helper.TBitsPropEnum;
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
import transbit.tbits.config.SysConfig;
import transbit.tbits.config.WebConfig;
import transbit.tbits.domain.BAUser;
import transbit.tbits.domain.BusinessArea;
import transbit.tbits.domain.Field;
import transbit.tbits.domain.NotificationRule;
import transbit.tbits.domain.Type;
import transbit.tbits.domain.TypeUser;
import transbit.tbits.domain.User;
import transbit.tbits.domain.UserType;
import transbit.tbits.exception.TBitsException;
import transbit.tbits.webapps.WebUtil;

//~--- JDK imports ------------------------------------------------------------

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

/**
 * This is the servlet for rendering the Users page in admin.
 *
 * @author  : Vinod Gupta
 * @version : $Id: $
 */
public class AdminCategories extends HttpServlet implements TBitsConstants, TBitsPropEnum {

    // Logger that logs information/error messages to the Application Log.
    public static final TBitsLogger LOG        = TBitsLogger.getLogger(PKG_ADMIN);
    private static final int        MANAGE     = 5;
    private static final String     HTML_USERS = "web/tbits-admin-manage-users.htm";

    // HTML Interfaces used to display the admin-users pages in TBits.
    private static final String HTML = "web/tbits-admin-manage.htm";

    //~--- methods ------------------------------------------------------------

    static
    {
    	//urls
        String url = "categories";
    	String completeURL = url + ".admin";
    	
        //Create Mapping
		URLRegistry.getInstance().addMapping(AdminProxyServlet.class, url, AdminCategories.class);
		
		//Create Menu
		NavMenu nav = NavMenu.getInstance();
		nav.BAMenu.add(new MenuItem("Categories", completeURL, "The administration (Add/Delete/Update) of fields of the Business Area."));
		
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
     * Method that actually handles the Get Request.
     *
     *
     * @param aRequest          the HttpServlet Request Object
     * @param aResponse         the HttpServlet Response Object
     * @exception ServletException
     * @exception IOException
     * @exception TBitsException
     * @exception DatabaseException
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
        Hashtable<String, Object> paramTable = AdminUtil.getRequestParams(aRequest, userConfig, MANAGE);
        BusinessArea              ba         = (BusinessArea) paramTable.get(Field.BUSINESS_AREA);

        if (ba == null) {
            throw new TBitsException(Messages.getMessage("INVALID_BUSINESS_AREA"));
        }

        int       systemId  = ba.getSystemId();
        String    sysPrefix = ba.getSystemPrefix();
        SysConfig sc        = ba.getSysConfigObject();
	
	//FIXME: not required, I think. --SG
        String    location  = ba.getLocation();

        // Check Basic Permissions to come to this page
        ArrayList<String> adminList = AdminUtil.checkBasicPermissions(systemId, userId, MANAGE);

        // Get all the type-fields.
        String typeFieldHtml = AdminUtil.getTypeFields(systemId);

        // Get BusinessArea List in which the user has permissions to view
        // the admin page.
        String baList    = AdminUtil.getSysIdList(systemId, userId);
        String fieldName = (String) paramTable.get("FIELD");

        if (fieldName == null) {
            fieldName = Field.CATEGORY;
        }

        Field                      field            = Field.lookupBySystemIdAndFieldName(systemId, fieldName);
        String                     strTypeId        = aRequest.getParameter("type_id");
        String                     categoryUserHtml = null;
        Hashtable<Integer, String> typeUserTable    = null;
        ArrayList<TypeUser>        typeUserList     = null;

        if (strTypeId != null) {
            int currentTypeId = Integer.parseInt(strTypeId);

            fieldName        = aRequest.getParameter("field_name");
            typeUserList     = TypeUser.lookupBySystemIdAndFieldNameAndTypeId(systemId, fieldName, currentTypeId);
            typeUserTable    = getTypeUserTable(typeUserList);
            categoryUserHtml = getCategoryUserHtml(typeUserTable, systemId, fieldName);

            DTagReplacer temphp = new DTagReplacer(HTML_USERS);

            temphp.replace("nearestPath", WebUtil.getNearestPath(aRequest, ""));
            temphp.replace("cssFile", WebUtil.getCSSFile(sc.getWebStylesheet(), sysPrefix, false));
            temphp.replace("ba_categories_prop", categoryUserHtml);

            if (fieldName.equalsIgnoreCase(Field.CATEGORY)) {
                temphp.replace("assignee_display", "");
            } else {
                temphp.replace("assignee_display", "none");
            }

            Mapper.refreshBOMapper();
            out.println(temphp.parse(systemId));

            return;
        }

        ArrayList<Type> typeList = Type.lookupBySystemIdAndFieldName(systemId, fieldName);
        int             typeId   = -1;

        if ((typeList != null) && (typeList.size() > 0)) {
            typeId = (typeList.get(0)).getTypeId();
        }

        String fieldTypeHtml = getFieldTypeHtml(typeList);

        typeUserList     = TypeUser.lookupBySystemIdAndFieldNameAndTypeId(systemId, fieldName, typeId);
        typeUserTable    = getTypeUserTable(typeUserList);
        categoryUserHtml = getCategoryUserHtml(typeUserTable, systemId, fieldName);

        //
        // Now, its time to read the Html Template, replace all the tags
        // with appropriate values and then output it.
        //
        DTagReplacer hp = new DTagReplacer(HTML);

        hp.replace("nearestPath", WebUtil.getNearestPath(aRequest, ""));
        hp.replace("cssFile", WebUtil.getCSSFile(sc.getWebStylesheet(), sysPrefix, false));
        hp.replace("baAdminList", AdminUtil.getBAAdminEmailList());
        hp.replace("sys_ids", baList);
        hp.replace("display_name", ba.getDisplayName());
        hp.replace("field_type_ids", fieldTypeHtml);
        hp.replace("title", "TBits Admin: " + ba.getDisplayName() + " Manage Categories");

        String myDomain = "hyd";

        try {
            myDomain = PropertiesHandler.getProperty(KEY_DOMAIN).toLowerCase();
        } catch (Exception e) {
            myDomain = "hyd";
        }

        if (myDomain.toLowerCase().equals("hyd")) {
            hp.replace("instanceBoldHyd", " b ");
            hp.replace("instanceBoldNyc", " ");
            hp.replace("instancePathHyd", WebUtil.getServletPath(aRequest, "/search/" + ba.getSystemPrefix()));
            hp.replace("instancePathNyc", WebUtil.getServletPath(aRequest, "/search/" + ba.getSystemPrefix()));
        } else {
            hp.replace("instanceBoldHyd", " ");
            hp.replace("instanceBoldNyc", " b ");
            hp.replace("instancePathHyd", WebUtil.getServletPath(aRequest, "/search/" + ba.getSystemPrefix()));
            hp.replace("instancePathNyc", WebUtil.getServletPath(aRequest, "/search/" + ba.getSystemPrefix()));
        }

        DTagReplacer hp1 = new DTagReplacer(HTML_USERS);

        hp1.replace("nearestPath", WebUtil.getNearestPath(aRequest, ""));
        hp1.replace("cssFile", WebUtil.getCSSFile(sc.getWebStylesheet(), sysPrefix, false));
        hp1.replace("ba_categories_prop", categoryUserHtml);

        if (fieldName.equalsIgnoreCase(Field.CATEGORY) == true) {
            hp1.replace("assignee_display", "");
        } else {
            hp1.replace("assignee_display", "none");
        }

        if ((adminList.contains("SUPER_ADMIN") == false) && (adminList.contains("PERMISSION_ADMIN") == false)) {
            hp.replace("superuser_display", "none");
        } else {
            hp.replace("superuser_display", "");
        }
        
      //Added by Lokesh to show/hide transmittal tab based on the transmittal property in app-properties
		String trnProperty = PropertiesHandler.getAppProperties().getProperty("transbit.tbits.transmittal");
		if ((trnProperty == null) || Boolean.parseBoolean(trnProperty) == false)
			hp.replace("trn_display", "none");
		else
			hp.replace("trn_display", "");
		
		/*
		 * Prepare the json
		 */
		String typeFieldJson = AdminUtil.getTypeFieldsJson(ba.getSystemId(), fieldName);
		System.out.println("The type field JSON: " + typeFieldJson);
        hp.replace("divReplacement", hp1.parse(systemId));
        hp.replace("allTypeFields", typeFieldHtml);
        hp.replace("allTypeFieldsJson", typeFieldJson);
        hp.replace("field_name", fieldName);
        hp.replace("field_displayName", field.getDisplayName());
        hp.replace("submit_disabled", "");
		hp.replace("userLogin", user.getUserLogin());

		String display_logout = "none";
		if(aRequest.getAuthType() == AuthConstants.AUTH_TYPE)
			display_logout = "";
		hp.replace("display_logout", display_logout);

        out.println(hp.parse(systemId));
    }

    private void handlePostRequest(HttpServletRequest aRequest, HttpServletResponse aResponse) throws ServletException, IOException, TBitsException, DatabaseException, FileNotFoundException {
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
        Hashtable<String, Object> paramTable = AdminUtil.getRequestParams(aRequest, userConfig, MANAGE);
        BusinessArea              ba         = (BusinessArea) paramTable.get(Field.BUSINESS_AREA);

        if (ba == null) {
            throw new TBitsException(Messages.getMessage("INVALID_BUSINESS_AREA"));
        }

        int    systemId  = ba.getSystemId();
        String sysPrefix = ba.getSystemPrefix();
        String fieldName = aRequest.getParameter("field_name");
        String action    = aRequest.getParameter("action");
        String strTypeId;

        if ((action != null) && (action.equalsIgnoreCase("updateTypeUsers"))) {
            strTypeId = aRequest.getParameter("type_id");

            int tempTypeId = Integer.parseInt(strTypeId);

            updateTypeUserList(systemId, fieldName, tempTypeId, aRequest);

            String orderValuePairs = aRequest.getParameter("orderValuePairs");

            if (orderValuePairs.equals("") == false) {
                String[] typeList = orderValuePairs.split(",");

                for (int i = 0; i < typeList.length; i++) {
                    int  typeId = Integer.parseInt(typeList[i]);
                    Type type   = Type.lookupBySystemIdAndFieldNameAndTypeId(systemId, fieldName, typeId);

                    type.setOrdering(i);
                    Type.update(type);
                }
            }

            Mapper.refreshBOMapper();

            return;
        }
    }

    /**
     * This method constructs an html snippet of the Field list.
     *
     * @param aSystemId SystemId of the Business Area.
     * @param aFieldName Name of the field currently selected.
     * @param aTypeId Id of the currently selected type.
     * @param aRequest HttpServletRequest object.
     *
     * @exception DatabaseException.
     */
    private void updateTypeUserList(int aSystemId, String aFieldName, int aTypeId, HttpServletRequest aRequest) throws DatabaseException {
        ArrayList<TypeUser>        typeUserList = TypeUser.lookupBySystemIdAndFieldNameAndTypeId(aSystemId, aFieldName, aTypeId);
        ArrayList<User>            baUsers      = BAUser.getBusinessAreaUsers(aSystemId);
        Field                      field        = Field.lookupBySystemIdAndFieldName(aSystemId, aFieldName);
        int                        fieldId      = field.getFieldId();
        Hashtable<Integer, String> tUTable      = getTypeUserTable(typeUserList);
        String                     assignee     = null;
        String                     notification = null;
        String                     volunteer    = null;
        String                     userLogin    = null;
        String[]                   typeUser     = null;
        int                        userTypeId;
        int                        size = baUsers.size();
        User                       user = null;
        int                        userId;

        for (int i = 0; i < size; i++) {
            user       = baUsers.get(i);
            userId     = user.getUserId();
            userLogin  = user.getUserLogin();
            userTypeId = 0;

            String strTypeUser = tUTable.get(userId);

            if (strTypeUser != null) {
                typeUser = strTypeUser.split(",");
            }

            assignee = aRequest.getParameter(userLogin + "_ass");

            boolean ass;

            if ((assignee != null) && (!assignee.trim().equals("")) && (!assignee.trim().equals("false"))) {
                ass = true;
            } else {
                ass = false;
            }

            boolean vol            = false;
            int     notificationId = 0;

            volunteer = aRequest.getParameter(userLogin + "_vol");

            if ((volunteer != null) && (!volunteer.trim().equals("")) && (!volunteer.trim().equals("false"))) {
                vol = true;
            } else {
                vol = false;
            }

            notification   = aRequest.getParameter(userLogin + "_select");
            notificationId = Integer.parseInt(notification);

            TypeUser tu = null;

            if ((strTypeUser == null) && ((ass != false) || (notificationId != 1))) {
                if (ass == true) {
                    userTypeId = UserType.ASSIGNEE;
                }

                tu = new TypeUser(aSystemId, fieldId, aTypeId, userId, userTypeId, notificationId, vol, false, true);
                TypeUser.insert(tu);
            }

            if ((strTypeUser != null) && ((ass == false) && (notificationId == 1))) {
                if (typeUser[0].equalsIgnoreCase("true")) {
                    userTypeId = UserType.ASSIGNEE;
                }

                tu = new TypeUser(aSystemId, fieldId, aTypeId, userId, userTypeId, 0, false, false, true);
                TypeUser.delete(tu);
            }

            if ((strTypeUser != null) && ((ass == true) || (notificationId != 1))) {
                if (ass == true) {
                    userTypeId = UserType.ASSIGNEE;
                }

                tu = new TypeUser(aSystemId, fieldId, aTypeId, userId, userTypeId, notificationId, vol, false, true);
                TypeUser.update(tu);
            }
        }

        Mapper.refreshBOMapper();
    }

    //~--- get methods --------------------------------------------------------

    /**
     * This method constructs an html snippet of the type users for
     * a selected type.
     *
     * @param aTUTable Hashtable mapping the user id to type user.
     * @param aSystemId systemId of the Business Area.
     * @param aFieldName name of the field currently selected.
     *
     * @exception DatabaseException.
     */
    private String getCategoryUserHtml(Hashtable<Integer, String> aTUTable, int aSystemId, String aFieldName) throws DatabaseException {
        StringBuilder   buffer        = new StringBuilder();
        StringBuilder   strBAUserList = new StringBuilder();
        ArrayList<User> baUsers       = BAUser.getBusinessAreaUsers(aSystemId);

        if (baUsers == null) {
            return buffer.toString();
        }

        int              size        = baUsers.size();
        User             user        = null;
        String           strTypeUser = null;
        String           userLogin   = null;
        String[]         typeUser    = null;
        NotificationRule nr          = null;
        int              userId;

        for (int i = 0; i < size; i++) {
            user      = baUsers.get(i);
            userId    = user.getUserId();
            userLogin = user.getUserLogin();
            strBAUserList.append(userLogin + ",");
            strTypeUser = aTUTable.get(userId);

            if (strTypeUser != null) {
                typeUser = strTypeUser.split(",");
            }

            buffer.append("<TR>\n").append("<TD class=\"sx\" noWrap\n>").append(userLogin).append("</TD>").append("<TD class=\"sx\" >").append("<SELECT id=\"").append(userLogin
                          + "_select").append("\" name=\"").append(userLogin + "_select").append("\"  onchange=\"frmChanged()\" ");
            buffer.append(">");

            if (strTypeUser == null) {
                buffer.append(getNotificationHtml(1));
            } else {
                buffer.append((getNotificationHtml(Integer.parseInt(typeUser[1]))));
            }

            buffer.append("</SELECT>").append("</TD>");

            if (aFieldName.equalsIgnoreCase(Field.CATEGORY) == false) {
                buffer.append("<TD></TD><TD></TD><TD></TD></TR>");

                continue;
            }

            buffer.append("<TD>").append("<INPUT class=\"chbox\" type=\"checkbox\" id=\"").append(userLogin + "_ass").append("\" name=\"").append(userLogin
                          + "_ass").append("\" ").append("onclick=\"javascript:onClickAssigneeBox('").append(userLogin + "')\" ");

            if ((strTypeUser != null) && (typeUser[0].equalsIgnoreCase("true") == true)) {
                buffer.append(" checked");
            }

            buffer.append("></TD>");
            buffer.append("<TD>").append("<INPUT class=\"chbox\" type=\"checkbox\" id=\"").append(userLogin + "_vol").append("\" name=\"").append(userLogin
                          + "_vol").append("\" ").append("onclick=\"javascript:onClickVolunteerBox('").append(userLogin + "')\" ");

            if ((strTypeUser != null) && (typeUser[0].equalsIgnoreCase("true") == true) && (typeUser[2].equalsIgnoreCase("true") == true)) {
                buffer.append(" checked ");
            }

            buffer.append("></TD>").append("</TR>");
        }

        buffer.append("<tr>").append("<td>").append("<input type=\"hidden\" id=\"usersList\" ").append("name=\"usersList\" value=\"").append(strBAUserList.toString() + "\" ").append("></td></tr>");

        return buffer.toString();
    }

    /**
     * This method constructs an html snippet of the field type list.
     *
     * @param aTypeList ArrayList of all the types for that field.
     *
     * @return Html String of the type list.
     *
     * @exception DatabaseException.
     */
    private String getFieldTypeHtml(ArrayList<Type> aTypeList) throws DatabaseException {
        StringBuilder buffer = new StringBuilder();
        int           size   = aTypeList.size();
        Type          type   = null;

        for (int i = 0; i < size; i++) {
            type = (aTypeList.get(i));
            buffer.append("<OPTION value='").append(type.getTypeId()).append("' ");

            if (i == 0) {
                buffer.append(" SELECTED ");
            }

            buffer.append(">").append(type.getDisplayName());
            buffer.append("</OPTION>\n");
        }

        return buffer.toString();
    }

    /**
     * This method constructs an html snippet of notification list box.
     *
     * @param aNotificationId notification id.
     *
     * @return Html snippet of the notification list box.
     *
     * @exception DatabaseException.
     */
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

    /**
     * This method constructs a hastable mapping userId and the typeUser.
     *
     * @param aTypeUserList ArrayList of all the types.
     *
     * @exception DatabaseException.
     */
    private Hashtable<Integer, String> getTypeUserTable(ArrayList<TypeUser> aTypeUserList) throws DatabaseException {
        Hashtable<Integer, String> typeUserTable = new Hashtable<Integer, String>();

        if (aTypeUserList == null) {
            return typeUserTable;
        }

        StringBuilder buffer = new StringBuilder();

        for (TypeUser tu : aTypeUserList) {
            if (tu.getUserTypeId() == UserType.ASSIGNEE) {
                typeUserTable.put(tu.getUserId(), "true," + tu.getNotificationId() + "," + tu.getIsVolunteer());
            } else {
                typeUserTable.put(tu.getUserId(), "false," + tu.getNotificationId() + "," + tu.getIsVolunteer());
            }
        }

        return typeUserTable;
    }
}
