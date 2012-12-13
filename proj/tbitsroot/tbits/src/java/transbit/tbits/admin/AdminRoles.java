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
import transbit.tbits.common.DTagReplacer;
import transbit.tbits.common.DatabaseException;
import transbit.tbits.common.PropertiesHandler;
import transbit.tbits.common.TBitsLogger;
import transbit.tbits.common.Utilities;
import transbit.tbits.config.SysConfig;
import transbit.tbits.config.WebConfig;
import transbit.tbits.domain.BusinessArea;
import transbit.tbits.domain.Field;
import transbit.tbits.domain.Permission;
import transbit.tbits.domain.Role;
import transbit.tbits.domain.RolePermission;
import transbit.tbits.domain.RoleUser;
import transbit.tbits.domain.User;
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
 * This is the servlet for rendering the properties page in admin.
 *
 * @author  : Vinod Gupta
 * @version : $Id: $
 */
public class AdminRoles extends HttpServlet implements TBitsConstants {

    // Logger that logs information/error messages to the Application Log.
    public static final TBitsLogger LOG   = TBitsLogger.getLogger(PKG_ADMIN);
    private static final int        ROLES = 3;

    // HTML Interfaces used to display the Add-Request page in TBits.
    private static final String HTML = "web/tbits-admin-roles.htm";

    // ArrayList which contains all the tags to be replaced.
    private static ArrayList<String> tagList;

    //~--- static initializers ------------------------------------------------

    static {
        tagList = Utilities.toArrayList(
            new StringBuilder().append("sys_ids,").append("display_name,").append("cssFile,").append("title,").append("roles_list,").append("roles_list_disabled,").append(
                "field_permission_list,").append("role_members,").append("submit_disabled,").append("allTypeFields,").append("instanceBoldHyd,").append("instanceBoldNyc,").append(
                "instancePathHyd,").append("instancePathNyc,").append("nearestPath,").append("baAdminList,userLogin,display_logout,").append("trn_display").append(",deleteRoleButton").toString());
        
        //urls
        String url = "permissions";
    	String completeURL = url + ".admin";
    	
        //Create Mapping
		URLRegistry.getInstance().addMapping(AdminProxyServlet.class, url, AdminRoles.class);
		
		//Create Menu
		NavMenu nav = NavMenu.getInstance();
		nav.BAMenu.add(new MenuItem("Field Pemissions", completeURL, "The administration (Add/Delete/Update) of fields of the Business Area."));
		
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
        WebConfig                 userConfig = user.getWebConfigObject();
        int                       userId     = user.getUserId();
        Hashtable<String, Object> paramTable = AdminUtil.getRequestParams(aRequest, userConfig, ROLES);
        BusinessArea              ba         = (BusinessArea) paramTable.get(Field.BUSINESS_AREA);

        if (ba == null) {
            throw new TBitsException(Messages.getMessage("INVALID_BUSINESS_AREA"));
        }

        int       systemId  = ba.getSystemId();
        String    sysPrefix = ba.getSystemPrefix();
        SysConfig sc        = ba.getSysConfigObject();

        // Check Basic Permissions to come to this page
        ArrayList<String> adminList = AdminUtil.checkBasicPermissions(systemId, userId, ROLES);

        if ((adminList.contains("SUPER_ADMIN") == false) && (adminList.contains("PERMISSION_ADMIN") == false)) {
            throw new TBitsException(Messages.getMessage("INVALID_USER"));
        }

        // Tag Table contains all the [tag_name, value] pairs.
        Hashtable<String, String> tagTable = new Hashtable<String, String>();

        WebUtil.setInstanceBold(tagTable, ba.getSystemPrefix());

        // Get BusinessArea List in which the user has permissions to view
        // the admin page.
        String baList = AdminUtil.getSysIdList(systemId, userId);

        tagTable.put("sys_ids", baList);
        tagTable.put("display_name", ba.getDisplayName());

        String location = ba.getLocation();

        tagTable.put("title", "TBits Admin: " + ba.getDisplayName() + " Roles");

        // Get all the type-fields.
        String typeFieldHtml = AdminUtil.getTypeFields(systemId);

        tagTable.put("allTypeFields", typeFieldHtml);

        // Now start replacing all the tags.
        // Replace the list of all roles
        Role            role  = (Role) paramTable.get("ROLE");
        ArrayList<Role> roles = Role.getRolesBySysId(systemId);

        Role.setSortParams(3, 0);
        roles = Role.sort(roles);

        if (role == null) {
            role = Role.lookupBySystemIdAndRoleName(systemId, "Analyst");
        }

        String roleList = getRoleList(systemId, roles, role);

        tagTable.put("roles_list", roleList);

        if (adminList.contains("SUPER_ADMIN") == false) {
            tagTable.put("submit_disabled", "disabled");
        }

        tagTable.put("nearestPath", WebUtil.getNearestPath(aRequest, ""));
        tagTable.put("cssFile", WebUtil.getCSSFile(sc.getWebStylesheet(), sysPrefix, false));
        tagTable.put("baAdminList", AdminUtil.getBAAdminEmailList());
		tagTable.put("userLogin", user.getUserLogin());

		String trnProperty = PropertiesHandler.getAppProperties().getProperty("transbit.tbits.transmittal");
		if ((trnProperty == null) || Boolean.parseBoolean(trnProperty) == false)
			tagTable.put("trn_display", "none");
		else
			tagTable.put("trn_display", "");

		String display_logout = "none";
		if(aRequest.getAuthType() == AuthConstants.AUTH_TYPE)
			display_logout = "";
		tagTable.put("display_logout", display_logout);

		if( role.getCanBeDeleted() != 0 )
		{
			tagTable.put("deleteRoleButton", "<input type='button' value='Delete current role' onClick='javascript:deleteRole(YAHOO.transbit.tbits.contextPath, sysPrefix)'></input>") ;
		}
		else
		{
			tagTable.put("deleteRoleButton", "") ;
		}
        // Now replace all the field permissions
        replaceFieldPermissions(aRequest, systemId, role, tagTable, adminList);

        // Replace roles users
        replaceRolesUsers(aRequest, systemId, role, tagTable, adminList);
	
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
        Hashtable<String, Object> paramTable = AdminUtil.getRequestParams(aRequest, userConfig, ROLES);
        BusinessArea              ba         = (BusinessArea) paramTable.get(Field.BUSINESS_AREA);

        if (ba == null) {
            throw new TBitsException(Messages.getMessage("INVALID_BUSINESS_AREA"));
        }

        int       systemId  = ba.getSystemId();
        String    sysPrefix = ba.getSystemPrefix();
        SysConfig sc        = ba.getSysConfigObject();
        Role      role      = (Role) paramTable.get("ROLE");
        String    strRevert = aRequest.getParameter("revert");

        if ((strRevert != null) && (strRevert.equalsIgnoreCase("true") == true)) {
            RolePermission.revertToDefaultSettings(systemId, role.getRoleName());

            String forwardUrl = "admin-roles/" + sysPrefix + "/" + role.getRoleName();

            aResponse.sendRedirect(WebUtil.getServletPath(aRequest, forwardUrl));

            return;
        }

        // Now get he permission for each field and update the
        // corresponding role permission.
        updateRolePermissions(systemId, role, aRequest);

        // Now check if a particular user has been denied permission
        // for a particular role and delete all such records.
//        updateRoleUsers(systemId, role, aRequest);

        String forwardUrl = "admin-roles/" + sysPrefix + "/" + role.getRoleName();

        aResponse.sendRedirect(WebUtil.getServletPath(aRequest, forwardUrl));

        return;
    }

    /**
     * This method constructs a drop down of all the roles.
     * @param aRequest TODO
     * @param aSystemId SystemId of the Business Area.
     * @param role      Current selected role.
     * @param aTagTable Hashtable containing [tag,value] pairs.
     *
     * @exception DatabaseException.
     */
    private void replaceFieldPermissions(HttpServletRequest aRequest, int aSystemId, Role role, Hashtable<String, String> aTagTable, ArrayList<String> aAdminList) throws DatabaseException {
        StringBuilder buffer    = new StringBuilder();
        StringBuilder fieldList = new StringBuilder();

        // Get all the fields for the system Id
        ArrayList<Field>                  fields    = Field.lookupBySystemId(aSystemId);
        Hashtable<String, RolePermission> permTable = RolePermission.getPermissionsBySystemIdAndRoleId(aSystemId, role.getRoleId());

        for (Field field : fields) {
            String fieldName = field.getName();

            /*
             *   if ( fieldName.equals(Field.APPEND_INTERFACE) ||
             *    fieldName.equals(Field.TO) ||
             *    fieldName.equals(Field.USER) ||
             *    fieldName.equals(Field.MAX_ACTION_ID) ||
             *    fieldName.equals(Field.LOGGED_DATE) ||
             *    fieldName.equals(Field.LASTUPDATED_DATE) ||
             *    fieldName.equals(Field.HEADER_DESCRIPTION) )
             * {
             *   continue;
             *   }
             */
            RolePermission rolePermission = permTable.get(field.getName());

            if (rolePermission != null) {
                fieldList.append(field.getName() + ",");

                int gpermissions = rolePermission.getPermission();
                int dpermissions = rolePermission.getDPermission();

                getFieldHtml(aRequest, field, buffer, gpermissions, dpermissions, aAdminList);
            }
        }

        aTagTable.put("field_permission_list", buffer.toString());
    }

    /**
     * This method constructs a drop down of all the roles.
     * @param aRequest TODO
     * @param aSystemId SystemId of the Business Area.
     * @param role      Currently selected role.
     * @param aTagTable Hashtable containing [tag,value] pairs.
     *
     * @exception DatabaseException.
     */
    private void replaceRolesUsers(HttpServletRequest aRequest, int aSystemId, Role role, Hashtable<String, String> aTagTable, ArrayList<String> aAdminList) throws DatabaseException {
        StringBuilder buffer = new StringBuilder();

        // Get all the fields for the system Id
        ArrayList<RoleUser> roleUsers = RoleUser.lookupBySystemIdAndRoleId(aSystemId, role.getRoleId());

        if (roleUsers != null) {
            for (RoleUser roleUser : roleUsers) {
                getRoleUserHtml(aRequest, roleUser, buffer, aAdminList);
            }
        }

        aTagTable.put("role_members", buffer.toString());
    }

    /**
     * This method constructs a drop down of all the roles.
     *
     * @param aSystemId SystemId of the Business Area.
     * @param aRole     Currently selected role.
     * @param aRequest  HttpServletRequest object.
     *
     * @exception DatabaseException.
     */
    private void updateRolePermissions(int aSystemId, Role aRole, HttpServletRequest aRequest) throws DatabaseException {
        ArrayList<Field>                  fields     = Field.lookupBySystemId(aSystemId);
        int                               size       = fields.size();
        Hashtable<String, RolePermission> permTable  = RolePermission.getPermissionsBySystemIdAndRoleId(aSystemId, aRole.getRoleId());
        String[]                          permission = aRequest.getParameterValues("permission");
        String                            view, add, change, email;
        RolePermission                    previousRP = null;
        RolePermission                    rp         = null;

        for (int i = 0; i < size; i++) {
            Field  field     = fields.get(i);
            String fieldName = field.getName();

            /*
             *     if ( fieldName.equals(Field.APPEND_INTERFACE) ||
             *    fieldName.equals(Field.TO) ||
             *    fieldName.equals(Field.USER) ||
             *    fieldName.equals(Field.MAX_ACTION_ID) ||
             *    fieldName.equals(Field.LOGGED_DATE) ||
             *    fieldName.equals(Field.LASTUPDATED_DATE) ||
             *    fieldName.equals(Field.HEADER_DESCRIPTION) )
             * {
             *   continue;
             * }
             */
            rp = new RolePermission();
            rp.setSystemId(aSystemId);
            rp.setRoleId(aRole.getRoleId());
            rp.setFieldId(field.getFieldId());
            previousRP = permTable.get(field.getName());

            if (previousRP != null) {
                int oldgpermission = previousRP.getPermission();
                int olddpermission = previousRP.getDPermission();
                int gpermission    = 0;
                int dpermission    = 0;

                view   = permission[4 * i + 0];
                add    = permission[4 * i + 1];
                change = permission[4 * i + 2];
                email = permission[4 * i + 3] ;

                if (view.equalsIgnoreCase("grant")) {
                    gpermission = gpermission + Permission.VIEW;
                } else if (view.equalsIgnoreCase("deny")) {
                    dpermission = dpermission + Permission.VIEW;

                    // if((oldgpermission & Permission.VIEW) != 0)
                    // gpermission = gpermission + Permission.VIEW;
                }

                if (add.equalsIgnoreCase("grant")) {
                    gpermission = gpermission + Permission.ADD;
                } else if (add.equalsIgnoreCase("deny")) {
                    dpermission = dpermission + Permission.ADD;

                    // if((oldgpermission & Permission.ADD) != 0)
                    // gpermission = gpermission + Permission.ADD;
                }

                if (change.equalsIgnoreCase("grant")) {
                    gpermission = gpermission + Permission.CHANGE;
                } else if (change.equalsIgnoreCase("deny")) {
                    dpermission = dpermission + Permission.CHANGE;

                    // if((oldgpermission & Permission.CHANGE) != 0)
                    // gpermission = gpermission + Permission.CHANGE;
                }
                
                if (email.equalsIgnoreCase("grant")) {
                    gpermission = gpermission + Permission.EMAIL_VIEW;
                } else if (email.equalsIgnoreCase("deny")) {
                    dpermission = dpermission + Permission.EMAIL_VIEW;
                }

                rp.setPermission(gpermission);
                rp.setDPermission(dpermission);

                if (rp.equals(previousRP) == true) {}
                else {
                    RolePermission.update(rp);
                }
            }
        }
    }

    /**
     * This method constructs a drop down of all the roles.
     *
     * @param aSystemId SystemId of the Business Area.
     * @param aRole     Currently selected role.
     * @param aRequest  HttpServletRequest object.
     *
     * @exception DatabaseException.
     */
    private void updateRoleUsers(int aSystemId, Role aRole, HttpServletRequest aRequest) throws DatabaseException {
        ArrayList<RoleUser> roleUsers = RoleUser.lookupBySystemIdAndRoleId(aSystemId, aRole.getRoleId());

        if (roleUsers != null) {
            int      size              = roleUsers.size();
            RoleUser roleUser          = null;
            String[] permission        = aRequest.getParameterValues("roleuser_perm");
            String   currentPermission = null;

            for (int i = 0; i < size; i++) {
                roleUser          = roleUsers.get(i);
                currentPermission = permission[i];

                if (currentPermission.equalsIgnoreCase("deny")) {
                    RoleUser.delete(roleUser);
                }
            }
        }
    }

    //~--- get methods --------------------------------------------------------

    /**
     * This method constructs a drop down of all the roles.
     * @param aRequest TODO
     * @param field        Field of a business Area.
     * @param buffer       StringBuilder to which the snippet is appended.
     * @param aGPermission Permission granted for the role.
     * @param aDPermission Permission denied for the role.
     *
     * @exception DatabaseException.
     */
    private void getFieldHtml(HttpServletRequest aRequest, Field field, StringBuilder buffer, int aGPermission, int aDPermission, ArrayList<String> aAdminList) {
        String disabled = "";

        if ((aAdminList.contains("SUPER_ADMIN") == false)) {
            disabled = "disabled ";
        }

        buffer.append("<TR>\n").append("<TD class=\"sx\" noWrap>").append(field.getDisplayName()).append("</TD>\n").append("<TD>").append("<IMG height=\"15\" width=\"15\" ").append("id=\"").append(
            field.getName() + "_view").append("\" ").append("onclick=\"javascript:onClickImage(").append("'" + field.getName() + "_view')\" ").append(disabled);

        if ((aDPermission & Permission.VIEW) != 0) {
            buffer.append("src=\"").append(WebUtil.getNearestPath(aRequest, "/web/images/ckdeny.gif\" ")).append("value=\"deny\" ").append(">").append("<input type=\"hidden\" name=\"permission").append(
                "\" id=\"").append(field.getName() + "_viewp" + "\" value=\"").append("deny\" >").append("</TD>");
        } else if ((aGPermission & Permission.VIEW) != 0) {
            buffer.append("src=\"").append(WebUtil.getNearestPath(aRequest, "/web/images/ckgrant.gif\" ")).append("value=\"grant\" ").append(">").append("<input type=\"hidden\" name=\"permission").append(
                "\" id=\"").append(field.getName() + "_viewp" + "\" value=\"").append("grant\" >").append("</TD>");
        } else {
            buffer.append("src=\"").append(WebUtil.getNearestPath(aRequest, "/web/images/ckempty.gif\" ")).append("value=\"empty\" ").append(">").append("<input type=\"hidden\" name=\"permission").append(
                "\" id=\"").append(field.getName() + "_viewp" + "\" value=\"").append("empty\" >").append("</TD>");
        }

        buffer.append("<TD>").append("<IMG height=\"15\" width=\"15\" ").append("id=\"").append(field.getName() + "_add").append("\" ").append("name=\"").append(field.getName()
                      + "_add").append("\" ").append("onclick=\"javascript:onClickImage(").append("'" + field.getName() + "_add')\" ").append(disabled);

        if ((aDPermission & Permission.ADD) != 0) {
            buffer.append("src=\"").append(WebUtil.getNearestPath(aRequest, "/web/images/ckdeny.gif\" ")).append("value=\"deny\" ").append(">").append("<input type=\"hidden\" name=\"permission").append(
                "\" id=\"").append(field.getName() + "_addp" + "\" value=\"").append("deny\" >").append("</TD>");
        } else if ((aGPermission & Permission.ADD) != 0) {
            buffer.append("src=\"").append(WebUtil.getNearestPath(aRequest, "/web/images/ckgrant.gif\" ")).append("value=\"grant\" ").append(">").append("<input type=\"hidden\" name=\"permission").append(
                "\" id=\"").append(field.getName() + "_addp" + "\" value=\"").append("grant\" >").append("</TD>");
        } else {
            buffer.append("src=\"").append(WebUtil.getNearestPath(aRequest, "/web/images/ckempty.gif\" ")).append("value=\"empty\" ").append(">").append("<input type=\"hidden\" name=\"permission").append(
                "\" id=\"").append(field.getName() + "_addp" + "\" value=\"").append("empty\" >").append("</TD>");
        }

        buffer.append("<TD>").append("<IMG height=\"15\" width=\"15\" ").append("id=\"").append(field.getName() + "_change").append("\" ").append("name=\"").append(field.getName()
                      + "_change").append("\" ").append("onclick=\"javascript:onClickImage(").append("'" + field.getName() + "_change')\" ").append(disabled);

        if ((aDPermission & Permission.CHANGE) != 0) {
            buffer.append("src=\"").append(WebUtil.getNearestPath(aRequest, "/web/images/ckdeny.gif\"")).append("value=\"deny\" ").append(">").append("<input type=\"hidden\" name=\"permission").append(
                "\" id=\"").append(field.getName() + "_changep" + "\" value=\"").append("deny\" >").append("</TD>");
        } else if ((aGPermission & Permission.CHANGE) != 0) {
            buffer.append("src=\"").append(WebUtil.getNearestPath(aRequest, "/web/images/ckgrant.gif\" ")).append("value=\"grant\" ").append(">").append("<input type=\"hidden\" name=\"permission").append(
                "\" id=\"").append(field.getName() + "_changep" + "\" value=\"").append("grant\" >").append("</TD>");
        } else {
            buffer.append("src=\"").append(WebUtil.getNearestPath(aRequest, "/web/images/ckempty.gif\" ")).append("value=\"empty\" ").append(">").append("<input type=\"hidden\" name=\"permission").append(
                "\" id=\"").append(field.getName() + "_changep" + "\" value=\"").append("empty\" >").append("</TD>");
        }

        buffer.append("<TD>").append("<IMG height=\"15\" width=\"15\" ").append("id=\"").append(field.getName() + "_email").append("\" ").append("name=\"").append(field.getName()
                + "_email").append("\" ").append("onclick=\"javascript:onClickImage(").append("'" + field.getName() + "_email')\" ").append(disabled);

	  if ((aDPermission & Permission.EMAIL_VIEW) != 0) {
	      buffer.append("src=\"").append(WebUtil.getNearestPath(aRequest, "/web/images/ckdeny.gif\"")).append("value=\"deny\" ").append(">").append("<input type=\"hidden\" name=\"permission").append(
	          "\" id=\"").append(field.getName() + "_emailp" + "\" value=\"").append("deny\" >").append("</TD>");
	  } else if ((aGPermission & Permission.EMAIL_VIEW) != 0) {
	      buffer.append("src=\"").append(WebUtil.getNearestPath(aRequest, "/web/images/ckgrant.gif\" ")).append("value=\"grant\" ").append(">").append("<input type=\"hidden\" name=\"permission").append(
	          "\" id=\"").append(field.getName() + "_emailp" + "\" value=\"").append("grant\" >").append("</TD>");
	  } else {
	      buffer.append("src=\"").append(WebUtil.getNearestPath(aRequest, "/web/images/ckempty.gif\" ")).append("value=\"empty\" ").append(">").append("<input type=\"hidden\" name=\"permission").append(
	          "\" id=\"").append(field.getName() + "_emailp" + "\" value=\"").append("empty\" >").append("</TD>");
	  }

        
        buffer.append("</TR>");
    }

    /**
     * This method constructs a drop down of all the roles.
     *
     * @param aSystemId SystemId of the Business Area.
     * @param aRoles    ArrayList of all roles.
     * @param aSelected The Role that is currently selected.
     *
     * @return String representation of the listbox.
     *
     * @exception DatabaseException.
     */
    private String getRoleList(int aSystemId, ArrayList<Role> aRoles, Role aSelected) throws DatabaseException {
        StringBuilder buffer = new StringBuilder();
        String        roleName;

        for (Role role : aRoles) {
            roleName = role.getRoleName();

//            if (roleName.equalsIgnoreCase("to") || roleName.equalsIgnoreCase("cc") || roleName.equalsIgnoreCase("permissionadmin")) {
//                continue;
//            }

            buffer.append("<OPTION value='").append(role.getRoleName()).append("' ");

            if ((aSelected != null) && role.getRoleName().equalsIgnoreCase(aSelected.getRoleName())) {
                buffer.append(" SELECTED ");
            }

            buffer.append(">").append(role.getRoleName());
            buffer.append("</OPTION>\n");
        }

        return buffer.toString();
    }

    /**
     * This method constructs a drop down of all the roles.
     * @param aRequest TODO
     * @param aRoleUser RoleUser Object
     * @param aBuffer   StringBuilder to which the snippet will be appended.
     *
     * @exception DatabaseException.
     */
    private void getRoleUserHtml(HttpServletRequest aRequest, RoleUser aRoleUser, StringBuilder aBuffer, ArrayList<String> aAdminList) throws DatabaseException {
        String disabled = "";

        if ((aAdminList.contains("SUPER_ADMIN") == false)) {
            disabled = "disabled ";
        }

        User user = User.lookupByUserId(aRoleUser.getUserId());

        /*
         * user will be null for inactive users. Ignore them as they are not
         * supposed to be shown anywhere in the admin interface.
         */
        if (user != null) {
            aBuffer.append("<TR>\n").append("<TD class=\"sx\" noWrap>").append(user.getUserLogin()).append("</TD>").append("<TD>").append("<input type=\"hidden\" ").append("name=\"").append(
                "roleuser_perm").append("\" id=\"").append(user.getUserLogin() + "p").append("\"").append(" value=\"grant\" ").append(">").append("<IMG height=\"21\" width=\"21\" ").append(
                "id=\"").append(user.getUserLogin()).append("\" ").append(disabled).append("src = \"").append(WebUtil.getNearestPath(aRequest, "/web/images/ckgrant.gif\" ")).append("value=\"grant\" ").append(
                "onclick=\"javascript:onClickRoleUserImage('").append(user.getUserLogin()).append("')\"").append(">").append("</IMG>").append("</TD>\n </TR>");
        }
    }
}
