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

//TBits imports.
import transbit.tbits.api.Mapper;
import transbit.tbits.common.DTagReplacer;
import transbit.tbits.common.DataSourcePool;
import transbit.tbits.common.DatabaseException;
import transbit.tbits.common.PropertiesHandler;
import transbit.tbits.common.TBitsLogger;
import transbit.tbits.common.Utilities;
import transbit.tbits.config.SysConfig;
import transbit.tbits.config.WebConfig;
import transbit.tbits.domain.BusinessArea;
import transbit.tbits.domain.DataType;
import transbit.tbits.domain.DisplayGroup;
import transbit.tbits.domain.Field;
import transbit.tbits.domain.FieldDescriptor;
import transbit.tbits.domain.Permission;
import transbit.tbits.domain.Role;
import transbit.tbits.domain.Type;
import transbit.tbits.domain.TypeDescriptor;
import transbit.tbits.domain.User;
import transbit.tbits.exception.TBitsException;
import transbit.tbits.webapps.WebUtil;

//~--- JDK imports ------------------------------------------------------------

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;

import java.sql.Connection;
import java.sql.SQLException;
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
public class AdminFields extends HttpServlet implements TBitsConstants {

    // Logger that logs information/error messages to the Application Log.
    private static final TBitsLogger LOG        = TBitsLogger.getLogger(PKG_ADMIN);
    private static final String      HTML_TYPES = "web/tbits-admin-fields-types.htm";

    // HTML Interfaces used to display the Add-Request page in TBits.
    private static final String HTML   = "web/tbits-admin-fields.htm";
    private static final int    FIELDS = 2;

    // ArrayList which contains all the tags to be replaced.
    private static ArrayList<String> tagList;

    //~--- static initializers ------------------------------------------------

    static {
        tagList = Utilities.toArrayList(
            new StringBuilder().append("sys_ids,display_name,cssFile,title,extended_display,field_ids,field_name,").append(
                "field_datatypes,field_displayName,field_tracking,field_descriptors,field_regex,field_add,field_change,").append(
                "field_view,field_display,field_daction,field_search,field_set,field_hyperlink,field_is_request_unique,field_is_action_unique,field_types,").append(
                "type_display,addTypeHtml,field_description,field_active,submit,submit_disabled,allTypeFields,").append(
                "instanceBoldHyd,instanceBoldNyc,instancePathHyd,instancePathNyc,nearestPath,baAdminList,field_display_order,field_display_group,userLogin,display_logout").toString());

        ArrayList<String> tempList = new ArrayList<String>();

        for (String tag : tagList) {
            tempList.add(tag);
            tempList.add(tag + "_disabled");
        }

        tagList = tempList;
        
        //Added by Lokesh to hide/show transmittal tab based on transmittal property in app-properties
		tagList.add("trn_display");

        //urls
        String url = "adminroles";
    	String completeURL = url + ".admin";
    	
        //Create Mapping
		URLRegistry.getInstance().addMapping(AdminProxyServlet.class, url, AdminFields.class);
		
		//Create Menu
		NavMenu nav = NavMenu.getInstance();
		nav.BAMenu.add(new MenuItem("Admin Fields", completeURL, "The administration (Add/Delete/Update) of fields of the Business Area."));
		
        // Get the location of the temporary directory.
    }

    //~--- methods ------------------------------------------------------------

    /**
     * This method replaces all the *_disabled tags with disabled if the user
     * is not in super-user list.
     *
     * @param aTagTable Hashtable containing name,value pairs.
     * @param aAdminList ArrayList containing all the roles of the user.
     *
     * @exception DatabaseException.
     */
    public static void disableAllFields(Hashtable<String, String> aTagTable, ArrayList<String> aAdminList) {
        int index;

        for (String tag : tagList) {
            index = tag.indexOf("_disabled");

            if (aAdminList.contains("SUPER_ADMIN") == false) {
                if (index > 0) {
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
    private String  encoding="UTF-8";
    public void doGet(HttpServletRequest aRequest, HttpServletResponse aResponse) throws ServletException, IOException {

    	aRequest.setCharacterEncoding(encoding);
        aResponse.setContentType("text/html; charset=" + encoding);
        aResponse.setCharacterEncoding(encoding);
        HttpSession session = aRequest.getSession();
        try {
            PrintWriter out     = aResponse.getWriter();

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
    	aRequest.setCharacterEncoding(encoding);
        aResponse.setContentType("text/html; charset=" + encoding);
        aResponse.setCharacterEncoding(encoding);

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
        Hashtable<String, Object> paramTable = AdminUtil.getRequestParams(aRequest, userConfig, FIELDS);
        BusinessArea              ba         = (BusinessArea) paramTable.get(Field.BUSINESS_AREA);

        if (ba == null) {
            throw new TBitsException(Messages.getMessage("INVALID_BUSINESS_AREA"));
        }

        int       systemId  = ba.getSystemId();
        String    sysPrefix = ba.getSystemPrefix();
        SysConfig sc        = ba.getSysConfigObject();

        // Check Basic Permissions to come to this page
        ArrayList<String> adminList = AdminUtil.checkBasicPermissions(systemId, userId, FIELDS);

        if ((adminList.contains("SUPER_ADMIN") == false) && (adminList.contains("PERMISSION_ADMIN") == false)) {
            throw new TBitsException(Messages.getMessage("INVALID_USER"));
        }

        // Tag Table contains all the [tag_name, value] pairs.
        Hashtable<String, String> tagTable = new Hashtable<String, String>();

        disableAllFields(tagTable, adminList);
        WebUtil.setInstanceBold(tagTable, ba.getSystemPrefix());

        // Get BusinessArea List in which the user has permissions to view
        // the admin page.
        String baList = AdminUtil.getSysIdList(systemId, userId);

        tagTable.put("sys_ids", baList);

        String location = ba.getLocation();

        tagTable.put("title", "TBits Admin: " + ba.getDisplayName() + " Fields");
        tagTable.put("display_name", ba.getDisplayName());

        // Get all the type-fields.
        String typeFieldHtml = AdminUtil.getTypeFields(systemId);

        tagTable.put("allTypeFields", typeFieldHtml);

        // Now start replacing all the tags.
        // Replace the field_id tag.
        Field field = (Field) paramTable.get("FIELD");

        if (field == null) {
            field = Field.lookupBySystemIdAndFieldName(systemId, Field.CATEGORY);
        }

        tagTable.put("field_ids", getFieldList(systemId, field));

        if (field.getIsExtended() == true) {
            tagTable.put("extended_display", "");
        } else {
            tagTable.put("extended_display", "none");
        }

        // Get the field id
        int    fieldId = field.getFieldId();
        String newType = aRequest.getParameter("newType");

        if (newType != null) {
            String currentField = aRequest.getParameter("currentField");
            Field  tempField    = Field.lookupBySystemIdAndFieldName(systemId, currentField);
            Type   type         = Type.lookupBySystemIdAndFieldNameAndTypeName(systemId, currentField, newType);
            String typeHtml     = replaceTypes(aRequest, systemId, tempField, type, adminList);

            out.println(typeHtml);

            return;
        }

        // Replace name and displayName.
        tagTable.put("field_name", field.getName());
        tagTable.put("field_name_disabled", "disabled");
        tagTable.put("field_displayName", field.getDisplayName());
        tagTable.put("field_display_order", field.getDisplayOrder() + "");
        tagTable.put("field_display_group", getFieldDisplayGroups(systemId, field.getDisplayGroup()) + "");
        
        // Replace Descriptors and Regex
        String                     temp   = null;
        ArrayList<FieldDescriptor> fdList = FieldDescriptor.lookupFDListBySystemIdAndFieldId(systemId, fieldId);

        if (fdList != null) {
            int             size = fdList.size();
            FieldDescriptor fd   = null;

            temp = "";

            for (int i = 0; i < size; i++) {
                fd = fdList.get(i);

                if (i != (size - 1)) {
                    temp = temp + fd.getDescriptor() + ";";
                } else {
                    temp = temp + fd.getDescriptor();
                }
            }

            tagTable.put("field_descriptors", temp);
        }

        temp = field.getRegex();

        if (temp != null) {
            tagTable.put("field_regex", temp);
        }

        // Replace datatype
        int dataTypeId = field.getDataTypeId();

        tagTable.put("field_datatypes", getFieldDataTypes(dataTypeId));
        tagTable.put("field_datatypes_disabled", "disabled");

        // Replace field-tracking.
        int tracking = field.getTrackingOption();

        tagTable.put("field_tracking", getTrackingHtml(tracking));

        // Replace field-Permissions
        replaceFieldPermissions(tagTable, field);

        // Now Replace the types.
        if (field.getDataTypeId() != DataType.TYPE) {
            tagTable.put("type_display", "none");
        } else {
            Type   type     = (Type) paramTable.get("TYPE");
            String typeHtml = replaceTypes(aRequest, systemId, field, type, adminList);

            tagTable.put("addTypeHtml", typeHtml);
        }

        // Replace the description and other check boxes.
        tagTable.put("field_description", field.getDescription());

        boolean bool;

        bool = field.getIsActive();

        if (bool == true) {
            tagTable.put("field_active", "checked");
        }

        bool = field.getIsPrivate();

        if (bool == true) {
            tagTable.put("field_private", "checked");
        }

        tagTable.put("nearestPath", aRequest.getContextPath() + "/");
        tagTable.put("cssFile", WebUtil.getCSSFile(sc.getWebStylesheet(), sysPrefix, false));
        tagTable.put("baAdminList", AdminUtil.getBAAdminEmailList());

		tagTable.put("userLogin", user.getUserLogin());
		
		String trnProperty = PropertiesHandler.getAppProperties().getProperty("transbit.tbits.transmittal");
		if ((trnProperty == null) || (Boolean.parseBoolean(trnProperty) == false))
			tagTable.put("trn_display", "none");
		else
			tagTable.put("trn_display", "");

		String display_logout = "none";
		if(aRequest.getAuthType() == AuthConstants.AUTH_TYPE)
			display_logout = "";
		tagTable.put("display_logout", display_logout);

        DTagReplacer dtr = new DTagReplacer(HTML);

        AdminUtil.replaceTags(dtr, tagTable, tagList);
        String str = dtr.parse(systemId);
        out.println(str);
    }

    private void handlePostRequest(HttpServletRequest aRequest, HttpServletResponse aResponse) throws ServletException, IOException, TBitsException, DatabaseException, FileNotFoundException {
        
        PrintWriter out     = aResponse.getWriter();
        HttpSession session = aRequest.getSession();

        // Step 1: Validate the User.
        User                      user       = WebUtil.validateUser(aRequest);
        WebConfig                 userConfig = user.getWebConfigObject();
        int                       userId     = user.getUserId();
        Hashtable<String, Object> paramTable = AdminUtil.getRequestParams(aRequest, userConfig, FIELDS);
        BusinessArea              ba         = (BusinessArea) paramTable.get(Field.BUSINESS_AREA);

        if (ba == null) {
            throw new TBitsException(Messages.getMessage("INVALID_BUSINESS_AREA"));
        }

        int       systemId  = ba.getSystemId();
        String    sysPrefix = ba.getSystemPrefix();
        SysConfig sc        = ba.getSysConfigObject();

        // Now start getting each of the parameters and update them at end.
        String fieldName = aRequest.getParameter("field_ids");

        // Now get the actual field object from the system id and name.
        Field field   = Field.lookupBySystemIdAndFieldName(systemId, fieldName);
        int   fieldId = field.getFieldId();

        // Get newfieldName
        String newFieldName     = aRequest.getParameter("new_field_name");
        String newFieldDatatype = aRequest.getParameter("new_field_data_type");
        
       // System.out.println("FieldName: " + fieldName + ", New Field: " 
        //		+ newFieldName + " , New Type: " + newFieldDatatype);
        
        if ((newFieldName != null) && (!newFieldName.equals(""))) {
            int newFieldDatatypeID = DataType.STRING;

            try {
                newFieldDatatypeID = Integer.parseInt(newFieldDatatype);
            } catch (NumberFormatException nfe) {
                LOG.info("Exception occured while parsing field type", nfe);
            }
            
            
            ArrayList<Field> aList = Field.lookupBySystemId(systemId);

            for (Field fld : aList) {
                if (fld.getName().equalsIgnoreCase(newFieldName)) {
                	//System.out.println(fld.toString());
                    out.println(Messages.getMessage("DUPLICATE_FIELD"));

                    return;
                }
            }
            //System.out.println("Ready to create field.");
            Field newField = new Field(systemId, 1, newFieldName, newFieldName, newFieldName, newFieldDatatypeID, true, true, false, 0, 47, "", false, "");
            
            try
            {
            	Field insertedField = Field.insert(newField);
            	Mapper.refreshBOMapper();
            	out.println(insertedField.getName() + " was inserted successfully.");
            }
            catch(TBitsException te)
            {
            	out.println(te.getDescription());
            }
            catch(Exception e )
            {
            	out.println("Creating " + newField.getName() + " failed.");
            }
            
            return;
        }

        // Get new Type name.If it is not null create a new Type.
        String newTypeName = aRequest.getParameter("new_type_name");

        if ((newTypeName != null) && (!newTypeName.equals(""))) {
            newTypeName = newTypeName.trim();

            ArrayList<Type> aList        = Type.lookupAllBySystemIdAndFieldName(systemId, field.getName());
            int             orderingType = 0;

            for (Type type : aList) {
                if (type.getName().equalsIgnoreCase(newTypeName)) {
                    out.println(Messages.getMessage("DUPLICATE_TYPE"));
                    return;
                }

                if (!type.getName().equalsIgnoreCase("pending") && (orderingType == 0)) {
                    int rank = type.getName().compareToIgnoreCase(newTypeName);

                    if (rank > 0) {
                        orderingType = type.getOrdering();
                    }
                }
            }

            ArrayList<Type> allTypes = Type.lookupAllBySystemIdAndFieldName(systemId, field.getName());
            
            boolean defaultValue = true;
            //If none of the existing types is set to default, then set the new type object's isDefault to false, else to true. 
            for (Type t : allTypes){
            	if (t.getIsActive()){
            		if (t.getIsDefault())
            			defaultValue = false;            		
            	}
            }           
            
            Type newType = new Type(systemId, fieldId, 1, newTypeName, newTypeName, newTypeName, orderingType, true, defaultValue, true, false, false);
            
            Type.insert(newType);
            Mapper.refreshBOMapper();

            
            String          typeHtml = getAllTypes(systemId, field, allTypes, newType);

            out.println(typeHtml);

            return;
        }

        String strAction = aRequest.getParameter("action");

        if (strAction.equalsIgnoreCase("deleteField")) {
        	Field deletedField = null ;    
        	try
        	{
        		deletedField = Field.delete(field);
        		Mapper.refreshBOMapper();
                out.println(Messages.getMessage("FIELD_DELETED", deletedField.getDisplayName()));
        	}
        	catch(TBitsException e)
        	{
        		out.println(e.getDescription());        		
        	}
        	catch(Exception e)
        	{
        		out.println(Messages.getMessage("FIELD_NOT_DELETABLE", field.getDisplayName()));
        	}
       		
        	return;
        }

        if (strAction.equalsIgnoreCase("typesUpdate")) {
            String strAdminType = aRequest.getParameter("adminType");

            // Get Attributes of the type
            String  typeList           = aRequest.getParameter("field_types");
            Type    type               = Type.lookupAllBySystemIdAndFieldNameAndTypeName(systemId, fieldName, typeList);
            String  typeDisplayName    = aRequest.getParameter("type_displayName");
            
            String  typeDescriptorList = aRequest.getParameter("type_descriptors");
            String  typeSearchSelected = aRequest.getParameter("type_searchSelected");
            boolean searchSelected;

            if ((typeSearchSelected != null) && (!typeSearchSelected.trim().equals("")) && (!typeSearchSelected.trim().equals("false"))) {
                searchSelected = true;
            } else {
                searchSelected = false;
            }

            String  typeAddSelected = aRequest.getParameter("type_addSelected");
            boolean addSelected;

            if ((typeAddSelected != null) && (!typeAddSelected.trim().equals("")) && (!typeAddSelected.trim().equals("false"))) {
                addSelected = true;
            } else {
                addSelected = false;
            }

            type.setDisplayName(typeDisplayName);
            type.setIsChecked(searchSelected);
            type.setIsDefault(addSelected);

            if (strAdminType.equalsIgnoreCase("super")) {
                String                    typeDescription = aRequest.getParameter("type_description");
                String                    typeDescriptors = aRequest.getParameter("type_descriptors");
                ArrayList<TypeDescriptor> tdList          = TypeDescriptor.lookupListBySystemIdAndFieldIdAndTypeId(type.getSystemId(), type.getFieldId(), type.getTypeId());

                if (tdList != null) {
                    for (TypeDescriptor td : tdList) {
                        if (td.getIsPrimary() == false) {
                            TypeDescriptor.delete(td);
                        }
                    }
                }

                String[] tdListArray;

                tdListArray = typeDescriptors.split("[;,]");

                TypeDescriptor tdesc = TypeDescriptor.getPrimaryDescriptor(systemId, field.getName(), type.getName());

                if (!typeDescriptors.trim().equals("")) {
                    for (int i = 0; i < tdListArray.length; i++) {
                        if (!(tdListArray[i].equalsIgnoreCase(tdesc.getDescriptor()))) {
                            TypeDescriptor newDescriptor = new TypeDescriptor(type.getSystemId(), type.getFieldId(), type.getTypeId(), tdListArray[i], false);

                            TypeDescriptor.insert(newDescriptor);
                        }
                    }
                }

                String  typePrivate = aRequest.getParameter("type_private");
                boolean privateType;

                if ((typePrivate != null) && (!typePrivate.trim().equals("")) && (!typePrivate.trim().equals("false"))) {
                    privateType = true;
                } else {
                    privateType = false;
                }

                String  typeActive = aRequest.getParameter("type_active");
                boolean activeType;

                if ((typeActive != null) && (!typeActive.trim().equals("")) && (!typeActive.trim().equals("false"))) {
                    activeType = true;
                } else {
                    activeType = false;
                }

                String  typeFinal = aRequest.getParameter("type_final");
                boolean finalType;

                if ((typeFinal != null) && (!typeFinal.trim().equals("")) && (!typeFinal.trim().equals("false"))) {
                    finalType = true;
                } else {
                    finalType = false;
                }

                type.setDescription(typeDescription);
                type.setIsActive(activeType);
                type.setIsPrivate(privateType);
                type.setIsFinal(finalType);
            }

            // Now update the type value
            Type.update(type);

            String orderValuePairs = aRequest.getParameter("orderValuePairs");

            if ((orderValuePairs != null) && (orderValuePairs.equalsIgnoreCase("") == false)) {
                String[] orderTypes = orderValuePairs.split(",");
                Type     orderType  = null;

                for (int i = 0; i < orderTypes.length; i++) {
                    orderType = Type.lookupAllBySystemIdAndFieldNameAndTypeName(systemId, fieldName, orderTypes[i]);
                    orderType.setOrdering(i + 1);
                    Type.update(orderType);
                }
            }

            Mapper.refreshBOMapper();

            return;
        }

        if (strAction.equalsIgnoreCase("typesDelete")) {
            String strAdminType = aRequest.getParameter("adminType");

            // Get Attributes of the type
            String typeList    = aRequest.getParameter("field_types");
            Type   type        = Type.lookupAllBySystemIdAndFieldNameAndTypeName(systemId, fieldName, typeList);
            Type   deletedType = Type.delete(type);

            if (deletedType == null) {
                out.println(Messages.getMessage("TYPE_NOT_DELETABLE", field.getDisplayName(), type.getDisplayName()));
                return;
            }

            Mapper.refreshBOMapper();
            out.println(Messages.getMessage("TYPE_DELETED", type.getDisplayName()));

            return;
        }

        String fieldDisplayName = aRequest.getParameter("field_displayName");
        String fieldTracking    = aRequest.getParameter("field_tracking");
        int    trackingOption   = 0;

        try {
            trackingOption = Integer.parseInt(fieldTracking);
        } catch (NumberFormatException nfe) {
            LOG.info("An exception occured while parsing field" + " tracking info");
        }

        String                     fieldDescriptors = aRequest.getParameter("field_descriptors");
        ArrayList<FieldDescriptor> fdList           = FieldDescriptor.lookupListBySystemIdAndFieldId(systemId, fieldId);

        if (fdList != null) {
            for (FieldDescriptor fd : fdList) {
                if (fd.getIsPrimary() == false) {
                    FieldDescriptor.delete(fd);
                }
            }
        }

        String[] fdListArray;

        if (fieldDescriptors.indexOf(";") > 0) {
            fdListArray = fieldDescriptors.split(";");
        } else {
            fdListArray = fieldDescriptors.split(",");
        }

        FieldDescriptor fdesc = FieldDescriptor.getPrimaryDescriptor(systemId, field.getName());

        if (!fieldDescriptors.trim().equals("")) {
            for (int i = 0; i < fdListArray.length; i++) {
                if (!(fdListArray[i].equalsIgnoreCase(fdesc.getDescriptor()))) {
                    FieldDescriptor newDescriptor = new FieldDescriptor(systemId, fieldId, fdListArray[i], false);

                    FieldDescriptor.insert(newDescriptor);
                }
            }
        }

        String fieldRegex = aRequest.getParameter("field_regex");

        if (fieldRegex == null) {
            fieldRegex = "";
        }
        
        String fieldDisplayOrderStr =  aRequest.getParameter("field_display_order");

        if (fieldDisplayOrderStr == null) {
        	fieldDisplayOrderStr = "";
        }
        int fieldDisplayOrder;
        try
        {
        	fieldDisplayOrder = Integer.parseInt(fieldDisplayOrderStr);
        }
        catch(NumberFormatException nfe)
        {
        	out.println("Invalid value of display order.");
            return;
        }
        
        String fieldDisplayGroupStr =  aRequest.getParameter("field_display_group");

        if (fieldDisplayGroupStr == null) {
        	fieldDisplayGroupStr = "";
        }
        int fieldDisplayGroup;
        try
        {
        	fieldDisplayGroup = Integer.parseInt(fieldDisplayGroupStr);
        }
        catch(NumberFormatException nfe)
        {
        	out.println("Invalid value of display group.");
            return;
        }
        
        if (strAction.equalsIgnoreCase("fieldsSuperAdmin") == true) {
            int    permission = 0;
            String fieldAdd   = aRequest.getParameter("field_add");

            if ((fieldAdd != null) && (!fieldAdd.trim().equals("")) && (!fieldAdd.trim().equals("false"))) {
                permission = permission + Permission.ADD;
            }

            String fieldChange = aRequest.getParameter("field_change");

            if ((fieldChange != null) && (!fieldChange.trim().equals("")) && (!fieldChange.trim().equals("false"))) {
                permission = permission + Permission.CHANGE;
            }

            String fieldView = aRequest.getParameter("field_view");

            if ((fieldView != null) && (!fieldView.trim().equals("")) && (!fieldView.trim().equals("false"))) {
                permission = permission + Permission.VIEW;
            }

            String fieldDisplay = aRequest.getParameter("field_display");

            if ((fieldDisplay != null) && (!fieldDisplay.trim().equals("")) && (!fieldDisplay.trim().equals("false"))) {
                permission = permission + Permission.DISPLAY;
            }

            String fieldDaction = aRequest.getParameter("field_daction");

            if ((fieldDaction != null) && (!fieldDaction.trim().equals("")) && (!fieldDaction.trim().equals("false"))) {
                permission = permission + Permission.D_ACTION;
            }

            String fieldSearch = aRequest.getParameter("field_search");

            if ((fieldSearch != null) && (!fieldSearch.trim().equals("")) && (!fieldSearch.trim().equals("false"))) {
                permission = permission + Permission.SEARCH;
            }

            String fieldSet = aRequest.getParameter("field_set");

            if ((fieldSet != null) && (!fieldSet.trim().equals("")) && (!fieldSet.trim().equals("false"))) {
                permission = permission + Permission.SET;
            }

            String fieldHyperlink = aRequest.getParameter("field_hyperlink");

            if ((fieldHyperlink != null) && (!fieldHyperlink.trim().equals("")) && (!fieldHyperlink.trim().equals("false"))) {
                permission = permission + Permission.HYPERLINK;
            }

            String fieldRequestUniqueness = aRequest.getParameter("field_is_request_unique");

            if ((fieldRequestUniqueness != null) && (!fieldRequestUniqueness.trim().equals("")) && (!fieldRequestUniqueness.trim().equals("false"))) {
                permission = permission + Permission.IS_REQUEST_UNIQUE;
            }

            String fieldActionUniqueness = aRequest.getParameter("field_is_action_unique");

            if ((fieldActionUniqueness != null) && (!fieldActionUniqueness.trim().equals("")) && (!fieldActionUniqueness.trim().equals("false"))) {
                permission = permission + Permission.IS_ACTION_UNIQUE;
            }

            String  fieldDescription = aRequest.getParameter("field_description");
            String  fieldPrivate     = aRequest.getParameter("field_private");
            boolean privateField;

            if ((fieldPrivate != null) && (!fieldPrivate.trim().equals("")) && (!fieldPrivate.trim().equals("false"))) {
                privateField = true;
            } else {
                privateField = false;
            }

            String  fieldActive = aRequest.getParameter("field_active");
            boolean activeField;

            if ((fieldActive != null) && (!fieldActive.trim().equals("")) && (!fieldActive.trim().equals("false"))) {
                activeField = true;
            } else {
                activeField = false;
            }

            field.setDescription(fieldDescription);
            field.setIsPrivate(privateField);
            field.setIsActive(activeField);
            field.setPermission(permission);
        }

        field.setDisplayName(fieldDisplayName);
        field.setRegex(fieldRegex);
        field.setTrackingOption(trackingOption);
        field.setDisplayOrder(fieldDisplayOrder);
        field.setDisplayGroup(fieldDisplayGroup);
        Field.update(field);
        Mapper.refreshBOMapper();

        String forwardUrl = "admin-fields/" + sysPrefix + "/" + fieldName;

        aResponse.sendRedirect(WebUtil.getServletPath(aRequest, forwardUrl));

        return;
    }

    /**
     * This method replaces the field control tags.
     *
     * @param aTagTable Hashtable containing [name,value] pairs.
     * @param field Field currently selected
     *
     * @exception DatabaseException.
     */
    private void replaceFieldPermissions(Hashtable<String, String> aTagTable, Field field) {
        int permission = field.getPermission();

        if ((permission & Permission.ADD) != 0) {
            aTagTable.put("field_add", "checked");
        }

        if ((permission & Permission.CHANGE) != 0) {
            aTagTable.put("field_change", "checked");
        }

        if ((permission & Permission.VIEW) != 0) {
            aTagTable.put("field_view", "checked");
        }

        if ((permission & Permission.DISPLAY) != 0) {
            aTagTable.put("field_display", "checked");
        }

        if ((permission & Permission.D_ACTION) != 0) {
            aTagTable.put("field_daction", "checked");
        }

        if ((permission & Permission.SEARCH) != 0) {
            aTagTable.put("field_search", "checked");
        }

        if ((permission & Permission.SET) != 0) {
            aTagTable.put("field_set", "checked");
        }

        if ((permission & Permission.HYPERLINK) != 0) {
            aTagTable.put("field_hyperlink", "checked");
        }

        if ((permission & Permission.IS_REQUEST_UNIQUE) != 0) {
            aTagTable.put("field_is_request_unique", "checked");
        }

        if ((permission & Permission.IS_ACTION_UNIQUE) != 0) {
            aTagTable.put("field_is_action_unique", "checked");
        }
    }

    /**
     * This method returns the html snippet of the currently selected type.
     * @param aRequest TODO
     * @param aSystemId SystemId of the Business Area.
     * @param aField currently selected Field.
     * @param aType currently selected type.
     * @param aAdminList arrayList containing the roles of the user.
     *
     * @return Html Snippet of currently selected type.
     *
     * @exception DatabaseException.
     */
    private String replaceTypes(HttpServletRequest aRequest, int aSystemId, Field aField, Type aType, ArrayList<String> aAdminList) throws DatabaseException {

        // get All Types
        ArrayList<Type>           types  = Type.lookupAllBySystemIdAndFieldName(aSystemId, aField.getName());
        ArrayList<TypeDescriptor> tdList = null;
        DTagReplacer              dtr    = null;

        try {
            dtr = new DTagReplacer(HTML_TYPES);
        } catch (FileNotFoundException fnfe) {
            LOG.severe("The Html template for replacing types has not" + "been found", fnfe);
        } catch (IOException ioe) {
            LOG.severe("An IOException has occured when initializing" + " template for types", ioe);
        }

        if ((types != null) && (types.size() != 0)) {
            if (aType == null) {
                aType = types.get(0);
            }

            tdList = TypeDescriptor.lookupListBySystemIdAndFieldIdAndTypeId(aSystemId, aField.getFieldId(), aType.getTypeId());

            BusinessArea ba        = BusinessArea.lookupBySystemId(aSystemId);
            String       sysPrefix = ba.getSystemPrefix();
            SysConfig    sc        = ba.getSysConfigObject();
            String       allTypes  = getAllTypes(aSystemId, aField, types, aType);

            dtr.replace("nearestPath", WebUtil.getNearestPath(aRequest, ""));
            dtr.replace("cssFile", WebUtil.getCSSFile(sc.getWebStylesheet(), sysPrefix, false));
            dtr.replace("field_types", allTypes);
            dtr.replace("type_name", aType.getName());
            dtr.replace("type_name_disabled", "disabled");
            StringBuilder sb = new StringBuilder();
            sb.append(aType.getDisplayName());
            dtr.replace("type_displayName", sb.toString());
            dtr.replace("type_description", aType.getDescription());

            String typeDescList = "";

            if (tdList != null) {
                int            size = tdList.size();
                TypeDescriptor td   = null;

                for (int i = 0; i < size; i++) {
                    td = tdList.get(i);

                    if (i != (size - 1)) {
                        typeDescList = typeDescList + td.getDescriptor() + ";";
                    } else {
                        typeDescList = typeDescList + td.getDescriptor();
                    }
                }

                dtr.replace("type_descriptors", typeDescList);
            }

            boolean temp;

            temp = aType.getIsPrivate();

            if (temp == true) {
                dtr.replace("type_private", "checked");
            } else {
                dtr.replace("type_private", "");
            }

            temp = aType.getIsFinal();

            if (temp == true) {
                dtr.replace("type_final", "checked");
            } else {
                dtr.replace("type_final", "");
            }

            temp = aType.getIsActive();

            if (temp == true) {
                dtr.replace("type_active", "checked");
            } else {
                dtr.replace("type_active", "");
            }

            temp = aType.getIsChecked();

            if (temp == true) {
                dtr.replace("type_searchSelected", "checked");
            } else {
                dtr.replace("type_searchSelected", "");
            }

            temp = aType.getIsDefault();

            if (temp == true) {
                dtr.replace("type_addSelected", "checked");
            } else {
                dtr.replace("type_addSelected", "");
            }

            if (aAdminList.contains("SUPER_ADMIN") == true) {
                dtr.replace("field_types_disabled", "");
                dtr.replace("type_displayName_disabled", "");
                dtr.replace("type_description_disabled", "");
                dtr.replace("type_descriptors_disabled", "");
                dtr.replace("type_searchSelected_disabled", "");
                dtr.replace("type_addSelected_disabled", "");
                dtr.replace("type_private_disabled", "");
                dtr.replace("type_final_disabled", "");
                dtr.replace("type_active_disabled", "");
                dtr.replace("type_description_disabled", "");
                dtr.replace("on_up_disabled", "");
                dtr.replace("on_down_disabled", "");
                dtr.replace("submit_disabled", "");
            } else {
                dtr.replace("field_types_disabled", "disabled");
                dtr.replace("type_displayName_disabled", "disabled");
                dtr.replace("type_description_disabled", "disabled");
                dtr.replace("type_searchSelected_disabled", "disabled");
                dtr.replace("type_addSelected_disabled", "disabled");
                dtr.replace("type_private_disabled", "disabled");
                dtr.replace("type_final_disabled", "disabled");
                dtr.replace("type_active_disabled", "disabled");
                dtr.replace("type_description_disabled", "disabled");
                dtr.replace("type_descriptors_disabled", "disabled");
                dtr.replace("on_up_disabled", "");
                dtr.replace("on_down_disabled", "");
                dtr.replace("submit_disabled", "disabled");
            }
        } else {
            dtr.replace("field_types", "");
            dtr.replace("field_types_disabled", "");
            dtr.replace("type_name", "");
            dtr.replace("type_name_disabled", "");
            dtr.replace("type_displayName", "");
            dtr.replace("type_displayName_disabled", "");
            dtr.replace("type_description", "");
            dtr.replace("type_descriptors", "");
            dtr.replace("type_descriptors_disabled", "");
            dtr.replace("type_description_disabled", "");
            dtr.replace("type_searchSelected", "");
            dtr.replace("type_searchSelected_disabled", "");
            dtr.replace("type_addSelected", "");
            dtr.replace("type_addSelected_disabled", "");
            dtr.replace("type_private", "");
            dtr.replace("type_private_disabled", "");
            dtr.replace("type_active", "");
            dtr.replace("type_active_disabled", "");
            dtr.replace("type_final_disabled", "");
            dtr.replace("type_final", "");
            dtr.replace("submit_disabled", "disabled");
            dtr.replace("on_up_disabled", "disabled");
            dtr.replace("on_down_disabled", "disabled");
        }

        dtr.replace("nearestPath", WebUtil.getNearestPath(aRequest, ""));

        return dtr.parse(aSystemId);
    }

    //~--- get methods --------------------------------------------------------

    /**
     * This method returns the html snippet of the type List
     *
     * @param aSystemId SystemId of the Business Area.
     * @param aField currently selected Field.
     * @param aTypes arrayList of all the types.
     * @param aType  currently selected type.
     *
     * @return Html Snippet of the type list.
     *
     * @exception DatabaseException.
     */
    private String getAllTypes(int aSystemId, Field aField, ArrayList<Type> aTypes, Type aType) throws DatabaseException {
        StringBuilder buffer   = new StringBuilder();
        int           size     = aTypes.size();
        String        typeName = null;

        for (int i = 0; i < size; i++) {
            typeName = aTypes.get(i).getName();
            buffer.append("<OPTION value='").append(typeName).append("' ");

            if ((aType != null) && aType.getName().equalsIgnoreCase(typeName)) {
                buffer.append(" SELECTED ");
            }

            if ((aType == null) && (i == 0)) {
                buffer.append(" SELECTED ");
            }

            buffer.append(">").append(aTypes.get(i).getDisplayName());
            buffer.append("</OPTION>\n");
        }

        return buffer.toString();
    }

    /**
     * This method returns the html snippet of the datatypes list.
     *
     * @param aDataTypeId DataTypeId of the field selected.
     *
     * @return Html String of the DataTypeList
     *
     * @exception DatabaseException.
     */
    private String getFieldDataTypes(int aDataTypeId) throws DatabaseException {
        StringBuilder       buffer = new StringBuilder();
        ArrayList<DataType> dt     = DataType.getAllDataTypes();

        for (DataType dti : dt) {
            buffer.append("<OPTION value='").append(dti.getDataTypeId()).append("' ");

            if (dti.getDataTypeId() == aDataTypeId) {
                buffer.append(" SELECTED ");
            }

            buffer.append(">").append(dti.getDataType());
            buffer.append("</OPTION>\n");
        }

        return buffer.toString();
    }
    
    /**
     * This method returns the html snippet of the datatypes list.
     *
     * @param aDataTypeId DataTypeId of the field selected.
     *
     * @return Html String of the DataTypeList
     *
     * @exception DatabaseException.
     */
    private String getFieldDisplayGroups(int aSystemId, int aDisplayGroupId) throws DatabaseException {
        StringBuilder       buffer = new StringBuilder();
        ArrayList<DisplayGroup> dgList = DisplayGroup.lookupIncludingDefaultForSystemId(aSystemId);

        for (DisplayGroup dg : dgList) {
            int dgId = dg.getId();
			buffer.append("<OPTION value='").append(dgId).append("' ");

            if (dgId == aDisplayGroupId) {
                buffer.append(" SELECTED ");
            }

            buffer.append(">").append(dg.getDisplayName());
            buffer.append("</OPTION>\n");
        }

        return buffer.toString();
    }

    /**
     * This method constructs an html snippet of the Field list.
     *
     * @param aSystemId SystemId of the Business Area.
     * @param aSelected Field currently selected.
     *
     * @return Html String of the field list.
     *
     * @exception DatabaseException.
     */
    private String getFieldList(int aSystemId, Field aSelected) throws DatabaseException {
        StringBuilder    buffer = new StringBuilder();
        ArrayList<Field> fields = Field.lookupBySystemId(aSystemId);

        for (Field field : fields) {
            buffer.append("<OPTION value='").append(field.getName()).append("' ");

            if ((aSelected != null) && field.getName().equalsIgnoreCase(aSelected.getName())) {
                buffer.append(" SELECTED ");
            }

            buffer.append(">").append(field.getDisplayName());
            buffer.append("</OPTION>\n");
        }

        if (aSelected == null) {
            aSelected = fields.get(3);
        }

        return buffer.toString();
    }

    /**
     * This method returns the html snippet of the tracking options
     *
     * @param aTracking Tracking option selected.
     *
     * @return Html Snippet of tracking option list.
     *
     * @exception DatabaseException.
     */
    private String getTrackingHtml(int aTracking) {
        StringBuilder buffer = new StringBuilder();

        for (int i = 0; i < 6; i++) {
            buffer.append("<OPTION value='").append(i).append("' ");

            if (i == aTracking) {
                buffer.append(" SELECTED ");
            }

            buffer.append(">");

            switch (i) {
            case 0 :
                buffer.append("Do not track the field");

                break;

            case 1 :
                buffer.append("Always display the current value");

                break;

            case 2 :
                buffer.append("Display the current value iff" + " it is not empty");

                break;

            case 3 :
                buffer.append("Display the change in the value");

                break;

            case 4 :
                buffer.append("Display the change in the value or the" + " current value if there is no change");

                break;

            case 5 :
                buffer.append("Display the change in the value or the" + " current value if there is no change and " + "the value is not empty");

                break;
            }

            buffer.append("</OPTION>\n");
        }

        return buffer.toString();
    }
}
