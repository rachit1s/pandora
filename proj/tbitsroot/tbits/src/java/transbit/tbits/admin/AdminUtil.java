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
 * WebUtil.java
 *
 * $Header:
 */
package transbit.tbits.admin;

//~--- non-JDK imports --------------------------------------------------------

import transbit.tbits.Helper.Messages;
import transbit.tbits.common.DTagReplacer;
import transbit.tbits.common.DataSourcePool;
import transbit.tbits.common.DatabaseException;

//TBits Imports
import transbit.tbits.common.TBitsLogger;
import transbit.tbits.config.SysConfig;
import transbit.tbits.config.WebConfig;
import transbit.tbits.domain.BusinessArea;
import transbit.tbits.domain.DataType;
import transbit.tbits.domain.Field;
import transbit.tbits.domain.Role;
import transbit.tbits.domain.RolePermission;
import transbit.tbits.domain.RoleUser;
import transbit.tbits.domain.Type;
import transbit.tbits.domain.User;
import transbit.tbits.exception.TBitsException;
import transbit.tbits.webapps.WebUtil;

//Static Imports.
import static transbit.tbits.Helper.TBitsConstants.PKG_ADMIN;

//~--- JDK imports ------------------------------------------------------------

//Java Imports.
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.StringTokenizer;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

//import org.apache.batik.dom.util.HashTable;

import com.google.gson.Gson;

//~--- classes ----------------------------------------------------------------

/**
 * This is an utility class that contains methods used by almost all the
 * servlets in the webapps package.
 *
 * @author  : Vaibhav, g, Vinod Gupta
 * @version : $Id: $
 */
public class AdminUtil extends HttpServlet {
    public static final TBitsLogger LOG = TBitsLogger.getLogger(PKG_ADMIN);

    // Enums to represent the pages.
    public static final int    PROPERTIES         = 1;
    public static final int    FIELDS             = 2;
    public static final int    ROLES              = 3;
    public static final int    USERS              = 4;
    public static final int    MANAGE             = 5;
    public static final int    CAPTIONS			  = 6;
    public static final int    APPPROP		  	  = 7;    
    public static final String ADD_USER_HTML      = "web/tbits-admin-users-adduser.htm";
    public static final String ADD_TYPE_HTML      = "web/tbits-admin-addtype.htm";
    public static final String ADD_EXT_FIELD_HTML = "web/tbits-admin-addextendedfield.htm";
    public static final String ADD_BA_HTML        = "web/tbits-admin-addbusinessarea.htm";

   
    
    //~--- methods ------------------------------------------------------------

    public static ArrayList<String> checkBasicPermissions(int aSystemId, int aUserId, int aPage) throws DatabaseException, TBitsException {
        boolean           flag      = false;
        ArrayList<String> adminList = RoleUser.lookupBySystemIdAndUserId(aSystemId, aUserId);

        if ((adminList.contains("ADMIN") == false) && (adminList.contains("PERMISSION_ADMIN") == false) && (adminList.contains("SUPER_ADMIN") == false)) {
            throw new TBitsException(Messages.getMessage("ADMIN_NO_PERMISSION"));
        }

        return adminList;
    }

    public static void disableAllFields(ArrayList<String> aTagList, Hashtable<String, String> aTagTable, boolean aFlag) {
        ArrayList<String> tempList = new ArrayList<String>();

        for (String tag : aTagList) {
            tempList.add(tag);
            tempList.add(tag + "_disabled");

            if (aFlag == true) {
                aTagTable.put(tag + "_disabled", "disabled");
            }
        }

        aTagList = tempList;
    }

    /**
     * Method to service http-get requests.
     *
     * @param  aRequest  HttpServletRequest object.
     * @param  aResponse HttpServletResponse object.
     *
     * @exception ServletException
     * @exception IOException
     */
    public void doGet(HttpServletRequest aRequest, HttpServletResponse aResponse) throws ServletException, IOException {
        HttpSession aSession = aRequest.getSession(true);

        try {
            long start = System.currentTimeMillis();

            handleRequest(aRequest, aResponse);

            long end = System.currentTimeMillis();
        } catch (FileNotFoundException fnfe) {
            LOG.warn("",(fnfe));

            TBitsException de = new TBitsException(Messages.getMessage("HTML_INTERFACE_MISSING"));

            aSession.setAttribute("ExceptionObject", de);
            aResponse.sendRedirect(WebUtil.getServletPath(aRequest, "/error"));

            return;
        } catch (Exception de) {
            LOG.warn("",(de));
            aSession.setAttribute("ExceptionObject", de);
            aResponse.sendRedirect(WebUtil.getServletPath(aRequest, "/error"));

            return;
        }
    }

    /**
     * Method to service http-post requests.
     *
     * @param  aRequest  HttpServletRequest object.
     * @param  aResponse HttpServletResponse object.
     *
     * @exception ServletException
     * @exception IOException
     */
    public void doPost(HttpServletRequest aRequest, HttpServletResponse aResponse) throws ServletException, IOException {
        doGet(aRequest, aResponse);
    }

    /**
     * This method actually services the http requests.
     *
     *
     * @param aRequest  HttpServletRequest object.
     * @param aResponse HttpServletResponse object.
     * @exception ServletException
     * @exception IOException
     * @exception DESTBitsExceptionexception DatabaseException
     */
    public void handleRequest(HttpServletRequest aRequest, HttpServletResponse aResponse) throws ServletException, IOException, TBitsException, DatabaseException, FileNotFoundException {

        //
        // Get all the mandatory objects required from the Http Request object.
        // - PrintWriter to output the HTML content.
        // - Session object to store the results and other configurations.
        // - User object of the user accessing this page.
        //
        aResponse.setContentType("text/html");

        PrintWriter out        = aResponse.getWriter();
        HttpSession aSession   = aRequest.getSession();
        User        user       = WebUtil.validateUser(aRequest);
        WebConfig   userConfig = user.getWebConfigObject();
        int         userId     = user.getUserId();
        String      action     = aRequest.getParameter("action");

        if ((action == null) || action.trim().equals("")) {
            out.println("");

            return;
        }

        action = action.trim().toLowerCase();

        if (action.equals("add-ba")) {
            Hashtable<String, Integer> permTable = RolePermission.getPermissionsBySystemIdAndUserId(-1, userId);

            if (isAdmin(permTable) == true) {

                // User is a super user and so can be allowed to add BA.
                DTagReplacer hp = new DTagReplacer(ADD_BA_HTML);

                hp.replace("cssFile", WebUtil.getCSSFile("", "", false));
                hp.replace("nearestPath", WebUtil.getNearestPath(aRequest, ""));
                out.println(hp.parse(0));
            } else {
                out.println(Messages.getMessage("ADMIN_NO_ADD_BA_PERMISSION"));
            }

            return;
        }

        // For any other requests we need the sysprefix.
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

        sysPrefix = ba.getSystemPrefix();

        int                        systemId  = ba.getSystemId();
        SysConfig                  sc        = ba.getSysConfigObject();
        Hashtable<String, Integer> permTable = RolePermission.getPermissionsBySystemIdAndUserId(systemId, userId);

        //
        // Check if the user is atleast one of the two before page rendering
        // __SUPER_USER__
        // __ADMIN__
        //
        if (action.equals("add-ext-field")) {
            if (isAdmin(permTable) == true) {

                // User is a super user and so can be allowed to add BA.
                DTagReplacer hp = new DTagReplacer(ADD_EXT_FIELD_HTML);

                hp.replace("cssFile", WebUtil.getCSSFile(sc.getWebStylesheet(), sysPrefix, false));
                hp.replace("nearestPath", WebUtil.getNearestPath(aRequest, ""));
                out.println(hp.parse(systemId));
            } else {
                out.println(Messages.getMessage("ADMIN_NO_ADD_BA_PERMISSION", sysPrefix));
            }

            return;
        } else if (action.equals("add-type")) {
            if (isAdmin(permTable) == true) {

                // User is a super user and so can be allowed to add BA.
                DTagReplacer hp = new DTagReplacer(ADD_TYPE_HTML);

                hp.replace("cssFile", WebUtil.getCSSFile(sc.getWebStylesheet(), sysPrefix, false));
                hp.replace("nearestPath", WebUtil.getNearestPath(aRequest, ""));
                out.println(hp.parse(systemId));
            } else {
                out.println(Messages.getMessage("ADMIN_NO_ADD_BA_PERMISSION"));
            }

            return;
        } else if (action.equals("add-user")) {
            if (isAdmin(permTable) == true) {

                // User is a super user and so can be allowed to add BA.
                DTagReplacer hp = new DTagReplacer(ADD_USER_HTML);

                hp.replace("cssFile", WebUtil.getCSSFile(sc.getWebStylesheet(), sysPrefix, false));
                hp.replace("sysPrefix", sysPrefix);
                hp.replace("nearestPath", WebUtil.getNearestPath(aRequest, ""));
                out.println(hp.parse(systemId));
            } else {
                out.println(Messages.getMessage("ADMIN_NO_ADD_BA_PERMISSION"));
            }

            return;
        }

        return;
    }

    /**
     * This method replaces all the tags with their corresponding values
     * reading them from the given table in the given HTML Parser object.
     *
     * @param aTagTable Table with [tag, value object]
     */
    public static void replaceTags(DTagReplacer aDTagReplacer, Hashtable<String, String> aTagTable, ArrayList<String> aTagList) {
        if (aTagList == null) {
            return;
        }

        int tagListSize = aTagList.size();

        for (int i = 0; i < tagListSize; i++) {
            String key   = (String) aTagList.get(i);
            String value = (String) aTagTable.get(key);

            if (value != null) {
                aDTagReplacer.replace(key, value);
            } else {
                aDTagReplacer.replace(key, "");
            }
        }
    }

    //~--- get methods --------------------------------------------------------

    public static String getBAAdminEmailList() {
        StringBuffer emailList = new StringBuffer();
        Connection   con       = null;

        try {
            con = DataSourcePool.getConnection();

            CallableStatement cs = con.prepareCall("stp_ru_getBAAdminList");
            ResultSet         rs = cs.executeQuery();

            if (rs != null) {
                boolean first = true;

                while (rs.next() != false) {
                    String email = rs.getString("email");

                    if (first == false) {
                        emailList.append(";");
                    } else {
                        first = false;
                    }

                    emailList.append(email);
                }

                rs.close();
                rs = null;
            }

            cs.close();
            cs = null;
        } catch (Exception e) {
            LOG.error("Exception while retrieving the BA Admin List");
        } finally {
            if (con != null) {
                try {
                    con.close();
                } catch (Exception e) {
                	e.printStackTrace();
                }
            }
        }

        return emailList.toString();
    }

    public static ArrayList<String> getExclusionTags(int AdminId) {
        ArrayList<String> exclusionList = new ArrayList<String>();

        return exclusionList;
    }

    /**
     * This method parses the PathInfo of the HttpRequest object and extracts
     * the required parameters based on the caller.
     * URL Format:
     *  baseurl: url/ba
     *  fieldspage: url/ba/fieldname/fieldtype or url/ba/fieldname
     *  rolepage : url/ba/rolename
     *  managepage : url/ba/fieldname
     *  
     *  It can be also be in the following way:
     *  baseurl: url?q=ba
     *  fieldspage: url?q=ba/fieldname/fieldtype or url?q=ba/fieldname
     *  rolepage : url?q=ba/rolename
     *  managepage : url?q=ba/fieldname
     *  
     * @param aRequest    Http Request Object.
     * @param aConfig     UserConfig.
     * @param aPage       Caller of the function.
     * @return Hashtable that contains the BA and Request Object based on
     *            the caller.
     * @exception ServletException  Thrown in case of servlet related errors.
     * @exception TBitsException of any application errors.
     * @exception DatabaseException Thrown in case of any database errors.
     */
    public static Hashtable<String, Object> getRequestParams(HttpServletRequest aRequest, WebConfig aConfig, int aPage) throws ServletException, TBitsException, DatabaseException {

        // This hashtable will contain the BusinessArea object in all cases
        // and Request object also in case of View-Request page.
        Hashtable<String, Object> result   = new Hashtable<String, Object>();
        String pathInfo = aRequest.getParameter("q");
//        if( (queryString != null) && (queryString.length() != 0))
//        {
//        	pathInfo = queryString.split("\\&")[0];
//        }
        if( (pathInfo == null) || ( pathInfo.length() == 0))
        {
        	pathInfo = aRequest.getPathInfo();
        }
        
        // Check if pathInfo is null.
        if (pathInfo == null) {
        	System.out.println("The pathinfo is null");
            throw new TBitsException(Messages.getMessage("INVALID_BUSINESS_AREA", ""));
        } else {
        	System.out.println("The pathifo is : " + pathInfo);
            
            // There is something in the pathInfo we received.
            StringTokenizer st = new StringTokenizer(pathInfo, "/\\");
            int             systemId;
            String          strField = null;
            String          strType  = null;
            String          strRole  = null;

            //
            // Once the pathInfo is tokenized, the first token is expected to
            // be the SystemPrefix.
            //
            if (st.hasMoreTokens() == true) {
                String       sysPrefix = st.nextToken().trim();
                BusinessArea ba        = BusinessArea.lookupBySystemPrefix(sysPrefix);

                if (ba == null) {
                    throw new TBitsException(Messages.getMessage("INVALID_BUSINESS_AREA", sysPrefix));
                }

                result.put(Field.BUSINESS_AREA, ba);
                systemId = ba.getSystemId();

                //
                // If Fields is the caller, then consider the next token
                // as the Field Name
                //
                if (aPage == FIELDS) {
                    if (st.hasMoreTokens() == true) {
                        strField = st.nextToken().trim();
                        //System.out.println("FIELD " + strField);

                        Field field = Field.lookupBySystemIdAndFieldName(systemId, strField);

                        if (field == null) {
                            throw new TBitsException(Messages.getMessage("INVALID_FIELD"));
                        }

                        result.put("FIELD", field);
                    }

                    if (st.hasMoreTokens() == true) {
                        strType = st.nextToken().trim();

                        Type type = Type.lookupBySystemIdAndFieldNameAndTypeName(systemId, strField, strType);

                        result.put("TYPE", type);
                    }
                }

                if (aPage == ROLES) {
                    if (st.hasMoreTokens() == true) {
                        strRole = st.nextToken().trim();

                        Role role = Role.lookupBySystemIdAndRoleName(systemId, strRole);

                        if (role == null) {
                            throw new TBitsException(Messages.getMessage("INVALID_ROLE"));
                        }

                        result.put("ROLE", role);
                    }
                }

                if (aPage == MANAGE) {
                    if (st.hasMoreTokens() == true) {
                        strField = st.nextToken().trim();
                        result.put("FIELD", strField);
                    }
                }
            }
        }

        return result;
    }

    public static String getSysIdList(int aSystemId, int aUserId) throws DatabaseException {
        StringBuilder           sb        = new StringBuilder();
        ArrayList<BusinessArea> adminList = BusinessArea.getAdminBusinessAreas(aUserId);

        BusinessArea.setSortParams(BusinessArea.DISPLAYNAME, 0);
        adminList = BusinessArea.sort(adminList);

        if ((adminList == null) || (adminList.size() == 0)) {
            return sb.toString();
        }

        int    sysId;
        String sysPrefix   = null;
        String displayName = null;

        for (BusinessArea ba : adminList) {
            sysId       = ba.getSystemId();
            sysPrefix   = ba.getSystemPrefix();
            displayName = ba.getDisplayName();
            sb.append("\n<OPTION VALUE='").append(sysId).append("' ");

            if (sysId == aSystemId) {
                sb.append(" SELECTED ");
            }

            sb.append(">").append(displayName).append(" [").append(sysPrefix).append("] ");

            if (ba.getIsPrivate() == true) {
                sb.append("&dagger;");
            }

            sb.append("</OPTION>");
        }

        return sb.toString();
    }

    /**
     * This method returns the HTML String for displaying in the sticky tag
     * in the manage users page.
     *
     *
     * @return HTML String of the fieldNames
     */
    public static String getTypeFields(int aSystemId) throws DatabaseException {
        StringBuilder buffer = new StringBuilder();

        // Get the list of shortcuts.
        ArrayList<Field> fieldList = Field.lookupBySystemId(aSystemId);

        buffer.append("\n<TABLE cellspacing=\"0\" cellpadding=\"0\" ").append(" border=\"0\" class=\"shortcuts\" ").append(" style=\"width:100%\" >").append(
            "\n\t<TBODY id='fieldTypeList' name='fieldTypeList'>");

        for (Field field : fieldList) {
            if (field.getDataTypeId() != DataType.TYPE) {
                continue;
            }

            String name = field.getName();

            buffer.append("\n\t\t<TR ID=\"").append(name).append("\">\n\t\t\t<TD><span class=\"l cb");
            buffer.append("\" onclick=\"openManagePage('").append(name).append("')\">").append(field.getDisplayName());
            buffer.append("</SPAN>").append("</TD>\n\t\t\t").append("\n\t\t</TR>");
        }

        buffer.append("\n\t</TBODY>\n</TABLE>");

        return buffer.toString();
    }
    
    public static String getTypeFieldsJson(int aSystemId, String selectedFieldName) throws DatabaseException {
    	 // Get the list of shortcuts.
        ArrayList<Field> fieldList = Field.lookupBySystemId(aSystemId);
        ArrayList<TinyTypeField> tinyFields = new ArrayList<TinyTypeField>();
        for(Field field:fieldList)
        {
        	if (field.getDataTypeId() != DataType.TYPE) {
                continue;
            }
        	 tinyFields.add(new TinyTypeField(field.getName(), field.getDisplayName()));
        }
        Hashtable hash = new Hashtable();
        hash.put("fields", tinyFields);
        hash.put("selected", selectedFieldName);
        return new Gson().toJson(hash);
    }

    private static boolean isAdmin(Hashtable<String, Integer> permTable) {
        if (permTable == null) {
            return false;
        }

        if ((permTable.get("__SUPER_USER__") != null) || (permTable.get("__ADMIN__") != null)) {
            return true;
        }

        return false;
    }
}
class TinyTypeField
{
	public TinyTypeField(String name, String displayName) {
		this.name = name;
		this.displayName = displayName;
	}
	String name;
	String displayName;
}
