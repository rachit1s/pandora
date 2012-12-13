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
 * UpdateRequest.java
 *
 * $Header:
 */
package transbit.tbits.api;

//~--- non-JDK imports --------------------------------------------------------

import static transbit.tbits.Helper.TBitsPropEnum.KEY_INDEXDIR;
import static transbit.tbits.domain.DataType.BOOLEAN;
import static transbit.tbits.domain.DataType.DATE;
import static transbit.tbits.domain.DataType.DATETIME;
import static transbit.tbits.domain.DataType.INT;
import static transbit.tbits.domain.DataType.REAL;
import static transbit.tbits.domain.DataType.STRING;
import static transbit.tbits.domain.DataType.TEXT;
import static transbit.tbits.domain.DataType.TIME;
import static transbit.tbits.domain.DataType.TYPE;
import static transbit.tbits.domain.DataType.USERTYPE;
import static transbit.tbits.domain.Permission.CHANGE;
import static transbit.tbits.domain.Permission.VIEW;
import static transbit.tbits.exception.APIException.FATAL;
import static transbit.tbits.exception.APIException.INFO;
import static transbit.tbits.exception.APIException.PERROR;
import static transbit.tbits.exception.APIException.SEVERE;
import static transbit.tbits.exception.APIException.WARNING;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.StringTokenizer;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import transbit.tbits.Helper.LinkFormatter;
import transbit.tbits.Helper.Messages;
import transbit.tbits.Helper.SysPrefixes;
import transbit.tbits.Helper.TBitsConstants;
import transbit.tbits.Helper.TBitsHelper;
import transbit.tbits.TVN.FileAction;
import transbit.tbits.TVN.WebdavConstants;
import transbit.tbits.TVN.WebdavUtil;
import transbit.tbits.common.Configuration;
import transbit.tbits.common.DataSourcePool;
import transbit.tbits.common.DatabaseException;
import transbit.tbits.common.MailResourceManager;
import transbit.tbits.common.PropertiesHandler;
import transbit.tbits.common.TBitsLogger;
import transbit.tbits.common.TBitsResourceManager;
import transbit.tbits.common.Timestamp;
import transbit.tbits.common.Utilities;
import transbit.tbits.config.BusinessRule;
import transbit.tbits.config.DependencyConfig;
import transbit.tbits.config.RuleAction;
import transbit.tbits.config.RuleCondition;
import transbit.tbits.config.SysConfig;
import transbit.tbits.config.BusinessRule.Operator;
import transbit.tbits.config.RuleAction.ActionType;
import transbit.tbits.config.RuleCondition.State;
import transbit.tbits.domain.Action;
import transbit.tbits.domain.BARule;
import transbit.tbits.domain.BusinessArea;
import transbit.tbits.domain.DataType;
import transbit.tbits.domain.Dependency;
import transbit.tbits.domain.ExternalResource;
import transbit.tbits.domain.Field;
import transbit.tbits.domain.Permission;
import transbit.tbits.domain.Request;
import transbit.tbits.domain.RequestEx;
import transbit.tbits.domain.RequestUser;
import transbit.tbits.domain.RolePermission;
import transbit.tbits.domain.TextDataType;
import transbit.tbits.domain.Type;
import transbit.tbits.domain.TypeUser;
import transbit.tbits.domain.User;
import transbit.tbits.domain.UserReadAction;
import transbit.tbits.domain.UserType;
import transbit.tbits.domain.Dependency.DepLevel;
import transbit.tbits.domain.Dependency.DepType;
import transbit.tbits.events.EventFailureException;
import transbit.tbits.events.EventManager;
import transbit.tbits.events.IPostRequestCommitEvent;
import transbit.tbits.events.IUpdatePostEvent;
import transbit.tbits.events.IUpdatePreEvent;
import transbit.tbits.events.UpdatePostEvent;
import transbit.tbits.events.UpdatePreEvent;
import transbit.tbits.exception.APIException;
import transbit.tbits.exception.TBitsException;
import transbit.tbits.external.Resource;
import transbit.tbits.external.ResourceAttr;
import transbit.tbits.external.ResourceResultMap;
import transbit.tbits.sms.SMS;
import transbit.tbits.webapps.WebUtil;

//~--- classes ----------------------------------------------------------------

//Third Party Imports.

/**
 * This class is used to add a request to a business area.
 *
 * @author  : Vaibhav
 *
 * @version : $Id: $
 *
 */
public class UpdateRequest implements TBitsConstants {

    // Application Logger.
    public static final TBitsLogger LOG = TBitsLogger.getLogger(PKG_API);

    // Enum to indicate if the external user should be inserted into the DB.
    private static final boolean INSERT            = true;
    private static final boolean NO_INSERT         = false;
    private static final boolean NO_EXCLUSION_LIST = false;
    private static final boolean NO_BA_EMAIL       = false;
    private static final boolean NO_AUTO           = false;

    // Enum to indicate if the exlusionList user should be added in user field.
    private static final boolean EXCLUSION_LIST = true;

    // Enum to indicate if BA Email address can be part of a given user field.
    private static final boolean BA_EMAIL = true;

    // Enum to indicate if "auto" is allowed in a given user field.
    private static final boolean AUTO = true;

    //~--- fields -------------------------------------------------------------

    // The Request Object that will be built, inserted and returned finally
    private Request                    myRequest   = null;
    private Hashtable<String, Integer> myPermTable = null;

    // The Request Object in previous state.
    private Request myOldRequest = null;

    // <FieldName, userlist> of defunct users and mail Ids.
    // To be notified in Hader Description
    private Hashtable<String, String> myDefunctUsers = new Hashtable<String, String>();
    private Hashtable<String, String> myBaMailIds    = new Hashtable<String, String>();

    // Indicator for private requests appended By Unauthorized Users
    private boolean myUnauthorized = false;

    // My Replied to action
    private Action myRepliedToActionObject = null;

    // Acceptable level of exceptions.
    private int myLevel = INFO;

    // Flag  if the current append is a made to a stale version of the request.
    private boolean appendStale = false;
    private String  assigneeList;
    private String  ccList;

    // Current rr-volunteer.
    private String curVolunteer;

    // Default Due Date.
    private Timestamp defDueDate;

    // Default mail notification values.
    private int     defNotify;
    private boolean defNotifyLoggers;

    // Comma separated list of request users.
    private String                 loggerList;
    private int                    myAppendInterface;
    private ArrayList<RequestUser> myAssignees;
    private Collection<AttachmentInfo>                 myAttachments;

    // Business Area object corresponding to the system id.
    private BusinessArea           myBusinessArea;
    private Type                   myCategory;
    private ArrayList<RequestUser> myCcs;
    private String                 myDescription;
    private int                 myDescriptionContentType;

    // List of Diff Entries in case of append to a stale version of the request
    ArrayList<DiffEntry> myDiffList;
    private Timestamp    myDueDate;

    // API Exception that will be built during the process.
    private APIException myException;

    // List of Diff Entries for extended fields.
    Hashtable<String, DiffEntry> myExtDiffTable;
    private ArrayList<Field>     myExtFieldList;

    // These into requests_ex table.
    private Hashtable<Field, RequestEx> myExtendedFields = new Hashtable<Field, RequestEx>();

    // Field table for this business area.
    private Hashtable<String, Field> myFieldTable;
    private String                   myHeaderDescription;
    private boolean                  myIsPrivate;
    private Timestamp                myLoggedDate;

    // Columns that go into request-related tables.
    // These into request_users table.
    private ArrayList<RequestUser>      myLoggers;
    private int                         myMaxActionId;
    private String                      myMemo;
    private int                         myNotify;
    private boolean                     myNotifyLoggers;
    private Type                        myOffice;
//    private Hashtable<Field, RequestEx> myOldExtendedFields;
    private int                         myParentId;

    // This hold related Requests information
    private String myRelatedRequests;
    private int    myRepliedToAction;
    private int    myRequestId;
    private Type   myRequestType;

    // List of warnings that should appear in header description from rules.
    private StringBuilder          myRuleWarnings;
    private Type                   mySeverity;
    private Type                   myStatus;
    private String                 mySubject;
    private ArrayList<RequestUser> mySubscribers;
    private String                 mySummary = new String();
    private int                 mySummaryContentType = TBitsConstants.CONTENT_TYPE_TEXT;
    private SysConfig              mySysConfig;

    // Columns that go into the requests table.
    private int                    mySystemId;
    private ArrayList<RequestUser> myTos;
    private Timestamp              myUpdatedDate;
    private User                   myUser;

    // Next rr-volunteer.
    private String nextVolunteer;

    // Source of add request invocation.
    private int    ourSource;
    private String subscriberList;
    private String toList;
    
  //This holds the version at which this request is going to be updated
    private int verNum;

 // To store the context path of the webapps, so that it is easy to give proper links to various resources.
    private String				   myContext = null; 
    //~--- constructors -------------------------------------------------------

    // Messages

    /**
     * Constructor.
     */
    public UpdateRequest() {

        // Set the acceptable level of exceptions to INFO by default.
        myLevel = INFO;

        // Initialize the api-exception object.
        myException = new APIException();

        // Initialize the rule warnings.
        myRuleWarnings = new StringBuilder();

        // Detect the source of request.
        detectSource();
    }

    /**
     * Constructor.
     */
    public UpdateRequest(int aLevel) throws TBitsException {

        // Check if the level specified is within in the acceptable range.
        if ((aLevel < INFO) || (aLevel > SEVERE)) {
            StringBuilder message = new StringBuilder();

            message.append("Acceptable levels of exceptions are:").append("\n - INFO\n - WARNING\n - PERROR\n - SEVERE\n");

            throw new TBitsException(message.toString());
        }

        // Set the acceptable level of exceptions.
        myLevel = aLevel;

        // Initialize the api-exception object.
        myException = new APIException();

        // Initialize the rule warnings.
        myRuleWarnings = new StringBuilder();

        // Detect the source of request.
        detectSource();
    }

    //~--- methods ------------------------------------------------------------
    
    private void initializeRequestObject(Hashtable<String, String> aParamTable) throws APIException {
    	//
        // We will look in the table for the required parameters in an order.
        // For types, we take the defaults if values are not specified.
        // Summary and Memo if specified are taken. Otherwise, they are empty.
        //
        // 1. System Id
        // Key      : Field.BUSINESS_AREA.
        // Required : Yes.
        String key   = Field.BUSINESS_AREA;
        String value = aParamTable.get(key);

        if ((value == null) || (value.trim().equals("") == true)) {
            myException.addException(new TBitsException("System Id is required."), FATAL);

            // We cannot continue further. Throw it here.
            throw myException;
        }

        // So we got a value. Parse this. It is an integer and if not check
        // if system prefix is specified else throw an exception.
        try {
            mySystemId = Integer.parseInt(value);
        } catch (NumberFormatException nfe) {

            // Check if the prefix of the BA is passed.
            try {
                myBusinessArea = BusinessArea.lookupBySystemPrefix(value);
            } catch (DatabaseException de) {
                myException.addException(new TBitsException(de.toString()), FATAL);

                // We cannot continue further. Throw it here.
                throw myException;
            }

            if (myBusinessArea == null) {
                myException.addException(new TBitsException("System Id should be numeric."), FATAL);

                // We cannot continue further. Throw it here.
                throw myException;
            }
        }

        /**
         * We will come here with BA Object as null only if a numeric value is
         * specified for BUSINESS_AREA key.
         */
        if (myBusinessArea == null) {

            // Now check if this System Id corresponds to a valid BusinessArea.
            try {
                myBusinessArea = BusinessArea.lookupBySystemId(mySystemId);
            } catch (DatabaseException de) {
                myException.addException(new TBitsException(de.toString()), FATAL);
            }

            if (myBusinessArea == null) {
                myException.addException(new TBitsException(Messages.getMessage("INVALID_BUSINESS_AREA", mySystemId)), FATAL);

                // We cannot continue. So throw the exception here itself.
                throw myException;
            }
        } else {
            mySystemId = myBusinessArea.getSystemId();
        }
        
        myRequest = new Request(mySystemId);
		
	}

    
    /**
     * This method builds the Request object from the attributes that are
     * obtained from the param table passed to the addRequest method.
     * @throws TBitsException 
     */
    private void buildRequestObject(){
        myDescription = LinkFormatter.replaceHrefWithSmartLinks(myDescription);
        try {
			String html = WebUtil.prepareValidHtml(myDescription);
			myDescription = html;
		} catch (IOException e) {
			e.printStackTrace();
			LOG.error("Could not prepare valid html for description", e);
		}
        
//        if (mySummary != null) {
            mySummary = LinkFormatter.replaceHrefWithSmartLinks(mySummary);
            try {
    			String html = WebUtil.prepareValidHtml(mySummary);
    			mySummary = html;
    		} catch (IOException e) {
    			e.printStackTrace();
    			LOG.error("Could not prepare valid html for summary", e);
    		}
//        }
        
        myRequest.setRequestId(myRequestId);
        myRequest.setCategoryId(myCategory);
        myRequest.setStatusId(myStatus);
        myRequest.setSeverityId(mySeverity);
        myRequest.setRequestTypeId(myRequestType);
        myRequest.setSubject(mySubject);
//        myRequest.setDescription(myDescription);
//        myRequest.setDescriptionContentType(myDescriptionContentType);
        myRequest.setDescription(new TextDataType(myDescription,myDescriptionContentType));
        myRequest.setIsPrivate(myIsPrivate);
        myRequest.setParentRequestId(myParentId);
        myRequest.setUserId(myUser.getUserId());
        myRequest.setAttachments(myAttachments);

        //
        // This is an out parameter kind of member. We get the value back once
        // the request is updated successfully.
        //
        myRequest.setMaxActionId(myMaxActionId);
        myRequest.setDueDate(myDueDate);
        myRequest.setLoggedDate(myLoggedDate);
        myRequest.setLastUpdatedDate(myUpdatedDate);

        // Header description is not yet generated. So this is not required.
        // myRequest.setHeaderDescription(myHeaderDescription);
        // Attachments are yet to be processed. So even this is not required.
        // myRequest.setAttachments(myAttachments);
//        myRequest.setSummary(mySummary);
//        myRequest.setSummaryContentType(mySummaryContentType);
        myRequest.setSummary(new TextDataType(mySummary,mySummaryContentType));
        myRequest.setMemo(myMemo);
        myRequest.setAppendInterface(myAppendInterface);
        myRequest.setNotify((myNotify != 0));
        myRequest.setNotifyLoggers(myNotifyLoggers);
        myRequest.setRepliedToAction(myRepliedToAction);
        myRequest.setOfficeId(myOffice);

        //
        // Set the fields which actually reside in a different table but are
        // related to the request.
        //
        myRequest.setLoggers(myLoggers);
        myRequest.setAssignees(myAssignees);
        myRequest.setSubscribers(mySubscribers);
        myRequest.setTos(myTos);
        myRequest.setCcs(myCcs);
        
        myRequest.setRelatedRequests(myRelatedRequests);

        // Set the extended fields
        setExtendedFields();

        // Set the following information from the old request.
//        myRequest.setSubRequests(myOldRequest.getSubRequests());
//        myRequest.setSiblingRequests(myOldRequest.getSiblingRequests());
//        myRequest.setRelatedRequests(myOldRequest.getRelatedRequests());
//        myRequest.setParentRequests(myOldRequest.getParentRequests());

        return;
    }

    private void checkAppGenerateDependency(Dependency dep) throws Exception {

        /*
         * 1. Get the dependency configuration.
         * 2. Get the Input attributes required for generation and prepare
         *    intput map.
         * 3. Get the resource and realize it.
         */

        // Step 1
        DependencyConfig dconfig = dep.getDepConfigObject();

        // Step 2
        boolean                   throwError      = dconfig.getThrowError();
        String                    errorMessage    = dconfig.getErrorMessage();
        Hashtable<String, String> inputAttrMap    = dconfig.getInputMap();
        Hashtable<String, String> outputAttrMap   = dconfig.getOutputMap();
        Hashtable<String, Object> inputValueMap   = new Hashtable<String, Object>();
        boolean                   realizeResource = true;

        /*
         * Check if there is any input to this resource.
         */
        if ((inputAttrMap != null) && (inputAttrMap.size() > 0)) {

            // Check if there is any change in any of the inputs.
            boolean             inputValuesChanged = false;
            Enumeration<String> reqFieldList       = inputAttrMap.keys();

            while (reqFieldList.hasMoreElements()) {

                // Get the request field name and its value as an object.
                String reqFieldName  = reqFieldList.nextElement();
                Object reqFieldValue = myRequest.getObject(reqFieldName);

                /*
                 * If the value is not null, the put the value in the input map
                 * where key is the resource attr name.
                 */
                if (reqFieldValue != null) {
                    String resAttrName = inputAttrMap.get(reqFieldName);

                    inputValueMap.put(resAttrName, reqFieldValue);
                }

                // Check if there is any change in the value of this field.
                if (isChanged(reqFieldName) == true) {
                    inputValuesChanged = true;
                }
            }

            /*
             * If the values of none of the inputs have been changed, and this
             * resource is a deterministic resource, then there is no need to
             * realize it again and we can continue with the old values.
             */
            if ((inputValuesChanged == false) && (dconfig.getIsDeterministic() == true)) {
                LOG.info("The values of fields that are inputs to this " + "resource have not been changed. Moreover, this " + "resource is a deterministic resource and hence "
                         + "we are continuing with the old values." + "\nDependency: " + dep.toString());
                realizeResource = false;
            }
        }

        /*
         * Check if we should still continue with the process of realizing the
         * resource
         */
        if (realizeResource == false) {
            return;
        }

        String           resourceName = dconfig.getResourceName();
        ExternalResource exres        = ExternalResource.lookupByName(resourceName);

        if (exres == null) {
            LOG.severe("Invalid resource name: " + resourceName);

            return;
        }

        LOG.info(inputValueMap.toString());

        Resource resource = exres.getResource();

        resource.realizeResource(inputValueMap);

        Iterator iter = resource;

        if (iter.hasNext() == false) {
            if (throwError == true) {
                throw new TBitsException(errorMessage);
            } else {

                /*
                 * TODO: Check with Ritesh if this should be part of the
                 * header description.
                 */
                LOG.info(errorMessage);
            }
        } else {
            ResourceResultMap   rrm             = (ResourceResultMap) iter.next();
            Enumeration<String> outReqFieldList = outputAttrMap.keys();

            while (outReqFieldList.hasMoreElements()) {
                String       outReqField  = outReqFieldList.nextElement();
                String       outResAttr   = outputAttrMap.get(outReqField);
                ResourceAttr resAttrValue = rrm.get(outResAttr);
                Field        reqField     = myFieldTable.get(outReqField);

                if ((reqField != null) && (reqField.getIsExtended() == true)) {
                    RequestEx reqEx = new RequestEx();

                    reqEx.setSystemId(mySystemId);
                    reqEx.setRequestId(myRequestId);
                    reqEx.setFieldId(reqField.getFieldId());

                    int dataType = reqField.getDataTypeId();

                    switch (dataType) {
                    case BOOLEAN :
                        reqEx.setBitValue(resAttrValue.getBitValue());

                        break;

                    case DATE :
                    case TIME :
                    case DATETIME :
                        reqEx.setDateTimeValue(resAttrValue.getDateValue());

                        break;

                    case INT :
                        reqEx.setIntValue(resAttrValue.getIntValue());

                        break;

                    case REAL :
                        reqEx.setRealValue(resAttrValue.getRealValue());

                        break;

                    case STRING :
                        reqEx.setVarcharValue(resAttrValue.getStringValue());

                        break;

                    case TEXT :
                        reqEx.setTextValue(resAttrValue.getStringValue());

                        break;

                    case TYPE :
                        reqEx = handleExtendedField(reqField, resAttrValue.getStringValue(), true);

                        break;
                    }

                    if (reqEx != null) {
                        myExtendedFields.put(reqField, reqEx);
                    }
                } else {
                    LOG.info("Not implemented: " + reqField);
                }
            }
        }

        return;
    }

    /**
     * This method checks if everyone present in the assignee list is valid
     * by some type field in this business area including the extended type
     * fields.
     *
     * @exception APIException incase of fatal errors.
     */
    private void checkAssigneeList() throws APIException {
        if ((myAssignees == null) || (myAssignees.size() <= 0)) {
            return;
        }

        ArrayList<RequestUser> invalidUsers = new ArrayList<RequestUser>();
        ArrayList<RequestUser> oldList      = (myOldRequest.getAssignees() == null ? null : new ArrayList<RequestUser>(myOldRequest.getAssignees()));

        if (oldList == null) {
            oldList = new ArrayList<RequestUser>();
        }

        int systemId = mySystemId;
        int fieldId  = 0;
        int typeId   = 0;
        int userId   = 0;

        for (RequestUser reqUser : myAssignees) {
            if (oldList.contains(reqUser) == true) {
                continue;
            }

            TypeUser typeUser = null;
            User     user     = null;

            // get the user id from this requestuser object.
            try {
                user = reqUser.getUser();
            } catch (DatabaseException de) {
                LOG.warn("",(de));
                myException.addException(new TBitsException(de.toString()), SEVERE);
            }

            userId = user.getUserId();

            // Check if the user is a valid assignee by category.
            fieldId = myCategory.getFieldId();
            typeId  = myCategory.getTypeId();

            try {
                typeUser = TypeUser.lookupBySystemIdAndFieldIdAndTypeIdAndUserId(systemId, fieldId, typeId, userId);
            } catch (DatabaseException de) {
                LOG.warn("",(de));
                myException.addException(new TBitsException(de.toString()), SEVERE);
            }

            if ((typeUser != null) && (typeUser.getUserTypeId() == UserType.ASSIGNEE)) {

                //
                // User is a valid type-assignee based on the current
                // category. So continue with the next one.
                //
                continue;
            }

            // Check if the user is a valid assignee by status.
            fieldId = myStatus.getFieldId();
            typeId  = myStatus.getTypeId();

            try {
                typeUser = TypeUser.lookupBySystemIdAndFieldIdAndTypeIdAndUserId(systemId, fieldId, typeId, userId);
            } catch (DatabaseException de) {
                LOG.warn("",(de));
                myException.addException(new TBitsException(de.toString()), SEVERE);
            }

            if ((typeUser != null) && (typeUser.getUserTypeId() == UserType.ASSIGNEE)) {

                //
                // User is a valid type-assignee based on the current
                // status. So continue with the next one.
                //
                continue;
            }

            // Check if the user is a valid assignee by severity.
            fieldId = mySeverity.getFieldId();
            typeId  = mySeverity.getTypeId();

            try {
                typeUser = TypeUser.lookupBySystemIdAndFieldIdAndTypeIdAndUserId(systemId, fieldId, typeId, userId);
            } catch (DatabaseException de) {
                LOG.warn("",(de));
                myException.addException(new TBitsException(de.toString()), SEVERE);
            }

            if ((typeUser != null) && (typeUser.getUserTypeId() == UserType.ASSIGNEE)) {

                //
                // User is a valid type-assignee based on the current
                // severity. So continue with the next one.
                //
                continue;
            }

            // Check if the user is a valid assignee by request type.
            fieldId = myRequestType.getFieldId();
            typeId  = myRequestType.getTypeId();

            try {
                typeUser = TypeUser.lookupBySystemIdAndFieldIdAndTypeIdAndUserId(systemId, fieldId, typeId, userId);
            } catch (DatabaseException de) {
                LOG.warn("",(de));
                myException.addException(new TBitsException(de.toString()), SEVERE);
            }

            if ((typeUser != null) && (typeUser.getUserTypeId() == UserType.ASSIGNEE)) {

                //
                // User is a valid type-assignee based on the current
                // request type. So continue with the next one.
                //
                continue;
            }

            // Check if user is a valid assginee based on any of the extended
            // types.
            ArrayList<Field> fieldList = new ArrayList<Field>(myFieldTable.values());

            for (Field field : fieldList) {
                if ((field.getIsExtended() == true) && (field.getDataTypeId() == DataType.TYPE)) {
                    RequestEx reqEx = myExtendedFields.get(field);

                    if (reqEx == null) {
                        continue;
                    }

                    // Check if the user is a valid assignee by this extended
                    // type.
                    fieldId = field.getFieldId();
                    typeId  = reqEx.getTypeValue();

                    try {
                        typeUser = TypeUser.lookupBySystemIdAndFieldIdAndTypeIdAndUserId(systemId, fieldId, typeId, userId);
                    } catch (DatabaseException de) {
                        LOG.warn("",(de));
                        myException.addException(new TBitsException(de.toString()), SEVERE);
                    }

                    if ((typeUser != null) && (typeUser.getUserTypeId() == UserType.ASSIGNEE)) {

                        //
                        // User is a valid type-assignee based on the current
                        // severity. So continue with the next one.
                        //
                        continue;
                    }
                }
            }

            invalidUsers.add(reqUser);
            myException.addException(new TBitsException(user.getDisplayName() + " is not a valid assignee"), SEVERE);
        }

        //
        // Remove all invalid Users incase acceptable exception level is Severe
        //
        for (RequestUser reqUser : invalidUsers) {
            myAssignees.remove(reqUser);
        }

        // Check if there are any exceptions in the list above the acceptable
        // level.
        if (myException.getExceptionCount(myLevel) > 0) {
            throw myException;
        }

        return;
    }

    /**
     *
     */
    private void checkDependencies() throws APIException {
        try {
            ArrayList<Dependency> dList = Dependency.getDependenciesBySystemId(mySystemId);

            /*
             * Check if there are any dependencies to be checked. Otherwise
             * return;
             */
            if ((dList == null) || (dList.size() == 0)) {
                return;
            }

            for (Dependency dep : dList) {
                DepLevel dLevel = dep.getLevel();
                DepType  dType  = dep.getType();

                switch (dType) {
                case GENERATE :
                    switch (dLevel) {
                    case APP_DEPENDENCY :
                        checkAppGenerateDependency(dep);

                        break;

                    case FIELD_DEPENDENCY :
                        break;
                    }

                    break;

                case VALIDATE :
                    switch (dLevel) {
                    case APP_DEPENDENCY :
                        break;

                    case FIELD_DEPENDENCY :
                        break;
                    }

                    break;

                case SYNCHRONIZE :

                    /*
                     * We are not interested in Sync dependencies
                     */
                    continue;
                }
            }
        } catch (TBitsException de) {
            myException.addException(de, SEVERE);

            throw myException;
        } catch (Exception e) {
            myException.addException(new TBitsException(e.toString()), SEVERE);

            throw myException;
        }
    }

    /**
     * This method compares the following values of a field using the specified
     * operator.
     *     - Value of the field in the request.
     *     - Value of the field specified in the rule.
     *
     * @param fieldName  Name of the field.
     * @param ruleValue  Value specified by the rule.
     * @param operator   Operator to be applied.
     * @param isNewRequest      true: new state
     *                   false: old state
     *
     * @return true if the comparison is successful.
     *
     */
    private boolean compare(String fieldName, String ruleValue, Operator operator, boolean isNewRequest) {
        boolean flag  = false;
        Field   field = myFieldTable.get(fieldName);

        if (field == null) {
            return false;
        }

        String fieldValue = (isNewRequest == true)
                            ? myRequest.get(field.getName())
                            : myOldRequest.get(field.getName());
        int    dataType   = field.getDataTypeId();

        ruleValue = APIUtil.getVarValue(myRequest, field, ruleValue);
        
        switch (dataType) {
        case BOOLEAN :
            flag = APIUtil.compareBoolean(operator, fieldValue, ruleValue);

            break;

        case TIME :
        case DATE :
        case DATETIME :
            flag = APIUtil.compareDate(operator, fieldValue, ruleValue);

            break;

        case INT :
            flag = APIUtil.compareInteger(operator, fieldValue, ruleValue);

            break;

        case REAL :
            flag = APIUtil.compareReal(operator, fieldValue, ruleValue);

            break;

        case STRING :
            flag = APIUtil.compareString(operator, fieldValue, ruleValue);

            break;

        case TEXT :
            flag = APIUtil.compareString(operator, fieldValue, ruleValue);

            break;

        case TYPE :
            flag = APIUtil.compareString(operator, fieldValue, ruleValue);

            break;

        case USERTYPE :
            flag = APIUtil.compareMultiValue(operator, fieldValue, ruleValue);

            break;
        }

        LOG.info("Comparing: [ " + fieldName + ", " + ruleValue + ", " + fieldValue + ", " + operator + " ]: " + flag);

        return flag;
    }

    /**
     * This method checks if aToken is present in the comma/colonseparated
     * list of items in aList.
     */
    private boolean contains(String aList, String aToken) {
        ArrayList<String> list = Utilities.toArrayList(aList.toLowerCase(), ";,");

        return list.contains(aToken.toLowerCase());
    }

    /**
     * This methods finds out the source of the request.
     */
    private void detectSource() {

        // We assume that this is a request from app server.
        ourSource = SOURCE_WEB;

        // Check if this is a call in response to an email.
        try {
            PropertiesHandler.getProperty(PROP_BA_NAME);
            ourSource = SOURCE_EMAIL;
        } catch (IllegalArgumentException iae) {

            // This is not for email invocation.
        }

        if (ourSource == SOURCE_WEB) {

            // If ourSource is still web, check if this is cmdline invocation.
            try {
                PropertiesHandler.getProperty(PROP_BA_PREFIX);
                ourSource = SOURCE_CMDLINE;
            } catch (IllegalArgumentException iae) {

                // This is not for command-line invocation.
            }
        }
    }

    private void formTrackRecord(Field aField, Collection<RequestUser> aOldList, Collection<RequestUser> aNewList, StringBuilder aRecord) {

        /*
         * Steps to deduce the diff for these user fields:
         * 1. Prepare newTable to hold request users as [Login, RequestUser]
         *    records.
         * 2. Maintain three StringBuilders: addedList, commonList, deleteList.
         *    - addedList will hold the user logins added in this update.
         *    - commonList will hold the user logins retained in this update.
         *    - deleteList will hold the user logins removed in this update.
         * 3. Iterate through the old list.
         *    - Check if the entry is present in new list also.
         *    - If present, get the ordering of this entry in the new list and
         *      prefix it to the login and add this to commonList.
         *    - If not present, get the ordering of this entry in the old list
         *      and prefix it to the login and add this to
         *
         */
        String                         login    = "";
        Hashtable<String, RequestUser> newTable = new Hashtable<String, RequestUser>();

        for (RequestUser ru : aNewList) {
            User user = null;

            try {
                user = ru.getUser();
            } catch (DatabaseException de) {
                continue;
            }

            if (user == null) {
                continue;
            }

            login = ((ru.getIsPrimary() == true)
                     ? "*"
                     : "") + user.getUserLogin().replace(".transbittech.com", "");
            newTable.put(login, ru);
        }

        StringBuilder addedList   = new StringBuilder();
        StringBuilder commonList  = new StringBuilder();
        StringBuilder deletedList = new StringBuilder();
        boolean       cFirst      = true;
        boolean       aFirst      = true;
        boolean       dFirst      = true;
        boolean       changed     = false;

        for (RequestUser ru : aOldList) {
            User user = null;

            try {
                user = ru.getUser();
            } catch (DatabaseException de) {
                continue;
            }

            if (user == null) {
                continue;
            }

            login = ((ru.getIsPrimary() == true)
                     ? "*"
                     : "") + user.getUserLogin().replace(".transbittech.com", "");

            RequestUser ruNew = newTable.get(login);

            if (ruNew != null) {

                // This is a common entry. take the ordering from the new one.
                int ordering = ruNew.getOrdering();

                if (cFirst == false) {
                    commonList.append(",");
                } else {
                    cFirst = false;
                }

                // Add this entry as ordering:login.
                commonList.append(ordering).append(":").append(login);

                // Remove this entry from the newTable.
                newTable.remove(login);
            } else {

                // This entry is deleted during this append.
                int ordering = ru.getOrdering();

                if (dFirst == false) {
                    deletedList.append(",");
                } else {
                    dFirst = false;
                }

                // Add this entry as ordering:login.
                deletedList.append(ordering).append(":").append(login);
                changed = true;
            }    // End If
        }        // End For

        /*
         * Now, the entries left in the newTable are added during this append.
         * Added these to the addedList.
         */
        ArrayList<RequestUser> newList = new ArrayList<RequestUser>(newTable.values());

        for (RequestUser ru : newList) {
            User user = null;

            try {
                user = ru.getUser();
            } catch (DatabaseException de) {
                continue;
            }

            if (user == null) {
                continue;
            }

            login = ((ru.getIsPrimary() == true)
                     ? "*"
                     : "") + user.getUserLogin().replace(".transbittech.com", "");

            int ordering = ru.getOrdering();

            if (aFirst == false) {
                addedList.append(",");
            } else {
                aFirst = false;
            }

            addedList.append(ordering).append(":").append(login);
            changed = true;
        }

        StringBuffer entryPrefix = new StringBuffer();

        entryPrefix.append(aField.getName()).append("##").append(aField.getFieldId()).append("##");

        /*
         * Add the common list only in atleas one of the following cases:
         * 1. There is a change.
         * 2. Tracking option is 4.
         * 3. Tracking option is 5 and the newList is not empty.
         */
        int     trackingOption = aField.getTrackingOption();
        boolean isNewListEmpty = ((aNewList.size() > 0)
                                  ? false
                                  : true);

        if ((changed == true) || (trackingOption == 4) || ((trackingOption == 5) && (isNewListEmpty == false))) {
            if (commonList.toString().trim().equals("") == false) {
                aRecord.append("*").append(entryPrefix).append("[").append(commonList.toString().trim()).append("]").append("\n");
            }
        }

        /*
         * Add the addedList and deleteList unless they are empty.
         */
        if (addedList.toString().trim().equals("") == false) {
            aRecord.append("+").append(entryPrefix).append("[").append(addedList.toString().trim()).append("]").append("\n");
        }

        if (deletedList.toString().trim().equals("") == false) {
            aRecord.append("-").append(entryPrefix).append("[").append(deletedList.toString().trim()).append("]").append("\n");
        }
    }

    /**
     * This method generates the diff list by comparing the old request object
     * with the one formed during this process.
     *
     * @return True  if there is actually a change in the entries that qualify
     *               for the diff.
     *         False Otherwise.
     */
    private boolean generateDiff() throws APIException {
        boolean returnValue = false;

        myDiffList = new ArrayList<DiffEntry>();

        Type                   oldType       = null;
        Type                   newType       = null;
        int                    repliedToType = 0;
        ArrayList<RequestUser> oldList       = null;
        ArrayList<RequestUser> newList       = null;
        ArrayList<Integer>     repliedToList = null;

        // Check if there is a change in the category.
        newType       = myRequest.getCategoryId();
        oldType       = myOldRequest.getCategoryId();
        repliedToType = myRepliedToActionObject.getCategoryId();

        if (newType != null) {
            if ((repliedToType != oldType.getTypeId()) && (newType.equals(oldType) == false)) {
                Field field = myFieldTable.get(Field.CATEGORY);

                if (field != null) {
                    DiffEntry de = new DiffEntry(field.getName(), field.getDisplayName(), oldType.getDisplayName(), newType.getDisplayName());

                    myDiffList.add(de);
                    returnValue = true;
                }
            }
        }

        // Check if there is a change in the status.
        newType       = myRequest.getStatusId();
        oldType       = myOldRequest.getStatusId();
        repliedToType = myRepliedToActionObject.getStatusId();

        if (newType != null) {
            if ((repliedToType != oldType.getTypeId()) && (newType.equals(oldType) == false)) {
                Field field = myFieldTable.get(Field.STATUS);

                if (field != null) {
                    DiffEntry de = new DiffEntry(field.getName(), field.getDisplayName(), oldType.getDisplayName(), newType.getDisplayName());

                    myDiffList.add(de);
                    returnValue = true;
                }
            }
        }

        // Check if there is a change in the severity.
        newType       = myRequest.getSeverityId();
        oldType       = myOldRequest.getSeverityId();
        repliedToType = myRepliedToActionObject.getSeverityId();

        if (newType != null) {
            if ((repliedToType != oldType.getTypeId()) && (newType.equals(oldType) == false)) {
                Field field = myFieldTable.get(Field.SEVERITY);

                if (field != null) {
                    DiffEntry de = new DiffEntry(field.getName(), field.getDisplayName(), oldType.getDisplayName(), newType.getDisplayName());

                    myDiffList.add(de);
                    returnValue = true;
                }
            }
        }

        // Check if there is a change in the request type.
        newType       = myRequest.getRequestTypeId();
        oldType       = myOldRequest.getRequestTypeId();
        repliedToType = myRepliedToActionObject.getRequestTypeId();

        if (newType != null) {
            if ((repliedToType != oldType.getTypeId()) && (newType.equals(oldType) == false)) {
                Field field = myFieldTable.get(Field.REQUEST_TYPE);

                if (field != null) {
                    DiffEntry de = new DiffEntry(field.getName(), field.getDisplayName(), oldType.getDisplayName(), newType.getDisplayName());

                    myDiffList.add(de);
                    returnValue = true;
                }
            }
        }

        // Check if there is a change in the logger list.
        oldList       = (myOldRequest.getLoggers() == null ? null : new ArrayList<RequestUser>(myOldRequest.getLoggers()));
        newList       = (myRequest.getLoggers() == null ? null : new ArrayList<RequestUser>(myRequest.getLoggers()));
        repliedToList = myRepliedToActionObject.getLoggerIds();

        if ((isChangedUsers(repliedToList, oldList) == true) && (isChanged(newList, oldList) == true)) {
            Field field = myFieldTable.get(Field.LOGGER);

            if (field != null) {
                DiffEntry de = new DiffEntry(field.getName(), field.getDisplayName(), APIUtil.toLoginList(oldList), APIUtil.toLoginList(newList));

                myDiffList.add(de);
                returnValue = true;
            }
        }

        // Check if there is a change in the assignee list.
        oldList       = (myOldRequest.getAssignees() == null ? null : new ArrayList<RequestUser>(myOldRequest.getAssignees()));
        newList       = (myRequest.getAssignees() == null ? null : new ArrayList<RequestUser>(myRequest.getAssignees()));
        repliedToList = myRepliedToActionObject.getAssigneeIds();

        if ((isChangedUsers(repliedToList, oldList) == true) && (isChanged(newList, oldList) == true)) {
            Field field = myFieldTable.get(Field.ASSIGNEE);

            if (field != null) {
                DiffEntry de = new DiffEntry(field.getName(), field.getDisplayName(), APIUtil.toLoginList(oldList), APIUtil.toLoginList(newList));

                myDiffList.add(de);
                returnValue = true;
            }
        }

        // Check if there is a change in the Subscriber list.
        oldList       = (myOldRequest.getSubscribers() == null ? null : new ArrayList<RequestUser>(myOldRequest.getSubscribers()));
        newList       = (myRequest.getSubscribers() == null ? null : new ArrayList<RequestUser>(myRequest.getSubscribers()));
        repliedToList = myRepliedToActionObject.getSubscriberIds();

        if ((isChangedUsers(repliedToList, oldList) == true) && (isChanged(newList, oldList) == true)) {
            Field field = myFieldTable.get(Field.SUBSCRIBER);

            if (field != null) {
                DiffEntry de = new DiffEntry(field.getName(), field.getDisplayName(), APIUtil.toLoginList(oldList), APIUtil.toLoginList(newList));

                myDiffList.add(de);
                returnValue = true;
            }
        }

        // Check if there is a change in the Subject.
        String oldSubject         = myOldRequest.getSubject();
        String newSubject         = myRequest.getSubject();
        String myRepliedToSubject = myRepliedToActionObject.getSubject();

        if ((myRepliedToSubject.equals(oldSubject) == false) && (oldSubject.equals(newSubject) == false)) {
            Field field = myFieldTable.get(Field.SUBJECT);

            if (field != null) {
                DiffEntry de = new DiffEntry(field.getName(), field.getDisplayName(), oldSubject, newSubject);

                myDiffList.add(de);
                returnValue = true;
            }
        }

        // Check if there is a change in the Summary.
        String oldSummary = myOldRequest.getSummary();
        String newSummary = myRequest.getSummary();

        if ((newSummary != null) && (oldSummary.equals(newSummary) == false)) {
            Field field = myFieldTable.get(Field.SUMMARY);

            if (field != null) {
                DiffEntry de = new DiffEntry(field.getName(), field.getDisplayName(), oldSummary, newSummary);

                myDiffList.add(de);
                returnValue = true;
            }
        }

        // Check if this BA has any extended fields.
        if ((myExtFieldList != null) && (myExtFieldList.size() > 0)) {
            LOG.info("Checking the extended diff list.");

            // For each extended field, check if there is a DiffEntry in the
            // ExtDiffTable has entry corresponding to it.
            for (Field field : myExtFieldList) {

                // Skip inactive fields.
                if (field.getIsActive() == false) {
                    continue;
                }

                String    key   = field.getName().toUpperCase();
                DiffEntry value = myExtDiffTable.get(key);

                if (value != null) {
                    myDiffList.add(value);
                    returnValue = true;
                }
            }
        }

        return returnValue;
    }

    /**
     * This method generates the header description.
     */
    private void generateHeaderDesc() {
    	String header = myRequest.getHeaderDescription();
    	if(header == null)
    		header = "";
        StringBuilder headerDesc = new StringBuilder(header);

        // We proceed in the order of fields.
        Field field = null;

        // Category.
        field = myFieldTable.get(Field.CATEGORY);
        headerDesc.append(getTrackRecord(field, myRequest.getCategoryId().getDisplayName(), myOldRequest.getCategoryId().getDisplayName()));

        // Status.
        field = myFieldTable.get(Field.STATUS);
        headerDesc.append(getTrackRecord(field, myRequest.getStatusId().getDisplayName(), myOldRequest.getStatusId().getDisplayName()));

        // Severity.
        field = myFieldTable.get(Field.SEVERITY);
        headerDesc.append(getTrackRecord(field, myRequest.getSeverityId().getDisplayName(), myOldRequest.getSeverityId().getDisplayName()));

        // Request type.
        field = myFieldTable.get(Field.REQUEST_TYPE);
        headerDesc.append(getTrackRecord(field, myRequest.getRequestTypeId().getDisplayName(), myOldRequest.getRequestTypeId().getDisplayName()));

        // Office.
        field = myFieldTable.get(Field.OFFICE);

        if (myOldRequest.getOfficeId() != null) {
            headerDesc.append(getTrackRecord(field, ((myRequest.getOfficeId() != null)
                    ? myRequest.getOfficeId().getDisplayName()
                    : ""), ((myOldRequest.getOfficeId() != null)
                            ? myOldRequest.getOfficeId().getDisplayName()
                            : "")));
        }

        // Logger.
        field = myFieldTable.get(Field.LOGGER);

        if ((field != null) && (field.getTrackingOption() != 0)) {
            headerDesc.append(trackUserList(field, myRequest.getLoggers(), (myOldRequest.getLoggers() == null ? null : new ArrayList<RequestUser>(myOldRequest.getLoggers()))));
        }

        // Assignees.
        field = myFieldTable.get(Field.ASSIGNEE);

        if ((field != null) && (field.getTrackingOption() != 0)) {
            headerDesc.append(trackUserList(field, myRequest.getAssignees(), (myOldRequest.getAssignees() == null ? null : new ArrayList<RequestUser>(myOldRequest.getAssignees()))));
        }

        // Auto Assignee.
        if ((curVolunteer != null) && (curVolunteer.trim().equals("") == false)) {
            headerDesc.append(field.getName()).append("##").append(field.getFieldId()).append("##[ Auto-assigned to '").append(curVolunteer).append("' ]\n");
        }

        // Subscribers.
        field = myFieldTable.get(Field.SUBSCRIBER);

        if ((field != null) && (field.getTrackingOption() != 0)) {
            headerDesc.append(trackUserList(field, myRequest.getSubscribers(), (myOldRequest.getSubscribers() == null ? null : new ArrayList<RequestUser>(myOldRequest.getSubscribers()))));
        }

        // All ExUserTypes
        ArrayList<Field> utFields = null ;
		try {
			utFields = Field.lookupBySystemId(myBusinessArea.getSystemId(), true, DataType.USERTYPE);
		} catch (DatabaseException e) {	
			myException.addException(new TBitsException(e),INFO);
			LOG.info("",(e));
		}
		
		if( utFields != null )
		{
	        for(Field f : utFields )
	        {        	
	        	if( field != null && field.getTrackingOption() != 0 )
	        		headerDesc.append(trackUserList(f, myRequest.getExUserType(f),myOldRequest.getExUserType(f)));		        
	        }
		}
        
//        if ((field != null) && (field.getTrackingOption() != 0)) {
//            headerDesc.append(trackUserList(field, mySubscribers, (myOldRequest.getSubscribers() == null ? null : new ArrayList<RequestUser>(myOldRequest.getSubscribers()))));
//        }
//        
        
        // Subject.
        field = myFieldTable.get(Field.SUBJECT);

        if ((field != null) && (field.getTrackingOption() != 0)) {
            if (mySubject.trim().equals(myOldRequest.getSubject()) == false) {
                headerDesc.append(getTrackRecord(field, myRequest.getSubject(), myOldRequest.getSubject()));
            }
        }

        // Summary.
        field = myFieldTable.get(Field.SUMMARY);

        if ((field != null) && (field.getTrackingOption() != 0)) {
            if ((myRequest.getSummary() != null) && (myRequest.getSummary().trim().equals(myOldRequest.getSummary()) == false)) {
                headerDesc.append(field.getName()).append("##").append(field.getFieldId()).append("##[ " + field.getDisplayName() + " Updated ]\n");
            }
        }

        // Description. No tracking is required.
        // field = myFieldTable.get(Field.DESCRIPTION);
        // Is Private.
        field = myFieldTable.get(Field.IS_PRIVATE);

        if ((field != null) && (field.getTrackingOption() != 0) && (myRequest.getIsPrivate() != myOldRequest.getIsPrivate())) {
            headerDesc.append(field.getName()).append("##").append(field.getFieldId());

            if (myRequest.getIsPrivate() == true) {
                headerDesc.append("##[ Marked private ]\n");
            } else {
                headerDesc.append("##[ Marked public ]\n");
            }
        }

        // Parent Request Id.
        field = myFieldTable.get(Field.PARENT_REQUEST_ID);

        if ((field != null) && (field.getTrackingOption() != 0) && (myRequest.getParentRequestId() != myOldRequest.getParentRequestId())) {
            headerDesc.append(getTrackRecord(field, Integer.toString(myRequest.getParentRequestId()), Integer.toString(myOldRequest.getParentRequestId())));
        }

        // Due Date.
        headerDesc.append(trackDueDate());

        // Linked Requests
        field = myFieldTable.get(Field.RELATED_REQUESTS);

        if ((field != null) && (field.getTrackingOption() != 0) && (myRequest.getRelatedRequests() != null)) {
            headerDesc.append(trackRelatedRequests(field));
        }

        // Track extended fields.
        for (Field f : myExtFieldList) {

            // No need to track an inactive field.
            if (f.getIsActive() == false || f.getDataTypeId() == DataType.USERTYPE ) 
            {
                continue;
            }

            RequestEx reqEx    = myRequest.getRequestExObject(f);
            RequestEx oldReqEx = myOldRequest.getRequestExObject(f);

            if ((reqEx != null) && (f.getTrackingOption() != 0)) {
                String oldValue = "";
                String newValue = "";

                switch (f.getDataTypeId()) 
                {
	                case DataType.BOOLEAN :
	                    if (oldReqEx == null) {
	                        oldValue = "";
	                    } else {
	                        oldValue = Boolean.toString(oldReqEx.getBitValue());
	                    }
	
	                    newValue = Boolean.toString(reqEx.getBitValue());
	
	                    break;
	
	                case DataType.DATE :
	                case DataType.TIME :
	                case DataType.DATETIME :
	                    if ((oldReqEx == null) || (oldReqEx.getDateTimeValue() == null)) {
	                        oldValue = "";
	                    } else {
	                        oldValue = oldReqEx.getDateTimeValue().toDateMin();
	                    }
	                    if(reqEx.getDateTimeValue() != null)
	                    	newValue = reqEx.getDateTimeValue().toDateMin();
	
	                    break;
	
	                case DataType.INT :
	                    if (oldReqEx == null) {
	                        oldValue = "";
	                    } else {
	                        oldValue = Integer.toString(oldReqEx.getIntValue());
	                    }
	
	                    newValue = Integer.toString(reqEx.getIntValue());
	
	                    break;
	
	                case DataType.REAL :
	                    if (oldReqEx == null) {
	                        oldValue = "";
	                    } else {
	                        oldValue = Double.toString(oldReqEx.getRealValue());
	                    }
	
	                    newValue = Double.toString(reqEx.getRealValue());
	
	                    break;
	
	                case DataType.STRING :
	                    if (oldReqEx == null) {
	                        oldValue = "";
	                    } else {
	                        oldValue = oldReqEx.getVarcharValue();
	                    }
	
	                    newValue = reqEx.getVarcharValue();
	
	                    break;                    
	                    
	                 case DataType.TEXT :
	                    if (oldReqEx == null) {
	                        oldValue = "";
	                    } else {
	                        oldValue = oldReqEx.getTextValue();
	                    }
	
	                    newValue = reqEx.getTextValue();
	
	                    break;
	
	                case DataType.TYPE : {
	                    Type type = null;
	
	                    if (oldReqEx == null) {
	                        oldValue = "";
	                    } else {
	                        try {
	                            type = Type.lookupBySystemIdAndFieldIdAndTypeId(mySystemId, f.getFieldId(), oldReqEx.getTypeValue());
	
	                            if (type != null) {
	                                oldValue = type.getDisplayName();
	                            } else {
	                                LOG.info("\nField: " + f.getName() + "\nValue: " + oldReqEx.getTypeValue());
	                            }
	                        } catch (Exception e) {
	                            LOG.severe("",(e));
	                        }
	                    }
	
	                    try {
	                        type = Type.lookupBySystemIdAndFieldIdAndTypeId(mySystemId, f.getFieldId(), reqEx.getTypeValue());
	
	                        if (type != null) {
	                            newValue = type.getDisplayName();
	                        } else {
	                            LOG.info("\nField: " + f.getName() + "\nValue: " + reqEx.getTypeValue());
	                        }
	                    } catch (Exception e) {
	                        LOG.severe("",(e));
	                    }
	
	                    break;
	                }
	                
	                case DataType.USERTYPE :
	                default :
	                	break;
	
	
                }

                headerDesc.append(getTrackRecord(f, newValue, oldValue));
            }
        }

        // Notify.
        field = myFieldTable.get(Field.NOTIFY);

        if ((field != null) && (field.getTrackingOption() != 0)) {
            if (myRequest.getNotify() == false) {
                headerDesc.append(field.getName()).append("##").append(field.getFieldId()).append("##[ No e-mail notification ]\n");
            } else {

                // To.
                field = myFieldTable.get(Field.TO);

                if ((field != null) && (field.getTrackingOption() != 0)) {
                    headerDesc.append(trackUserList(field, myRequest.getTos(), null));
                }

                // Cc.
                field = myFieldTable.get(Field.CC);

                if ((field != null) && (field.getTrackingOption() != 0)) {
                    headerDesc.append(trackUserList(field, myRequest.getCcs(), null));
                }

                // Notify Loggers.
                field = myFieldTable.get(Field.NOTIFY_LOGGERS);

                if ((field != null) && (field.getTrackingOption() != 0) && (myRequest.getNotifyLoggers() == false)) {
                    headerDesc.append(field.getName()).append("##").append(field.getFieldId());
                    headerDesc.append("##[ Mail not sent to loggers ]\n");
                }
            }
        }

        headerDesc.append(myRuleWarnings);

        //
        // Log message if unauthorized user appended to a pruvate request
        //
        if (myUnauthorized == true) {
            headerDesc.append("[ ").append(Messages.getMessage("UNAUTHORIZED_UPDATER")).append(" ]\n");
        }

        myHeaderDescription = headerDesc.toString();
        myRequest.setHeaderDescription(myHeaderDescription);
    }

    /**
     * This method handles the value specified for a boolean extended field.
     *
     * @param field Field object corresponding to this extended field.
     * @param value Value specified for this extended field.
     * @param isFieldSpecified 
     *
     * @return RequestEx object.
     */
    private RequestEx handleBooleanExtendedField(Field field, String value, boolean isFieldSpecified) {
        RequestEx reqEx = new RequestEx();

        reqEx.setSystemId(mySystemId);
        reqEx.setRequestId(myRequestId);
        reqEx.setFieldId(field.getFieldId());

        // Get the old value for this field if present in the table.
        RequestEx oldReqEx = myExtendedFields.get(field);
        boolean   bValue   = false;

        if (value == null) {
            if (oldReqEx == null) {

                // Possibly this is a newly added extended field.
                // insert with default value into requests_ex table.
                reqEx.setBitValue(false);
            } 
            else if((field.getPermission() & Permission.SET) == 0){
            	reqEx.setBitValue(false);
            }
            else {
                // retain the old value.
                reqEx = oldReqEx;
            }
        } else {

            // User specified some value.
            if ((value.trim().equalsIgnoreCase("true") == true) || (value.trim().equalsIgnoreCase("1") == true)
            		||(value.trim().equalsIgnoreCase("yes"))) {
                bValue = true;
            } else {
                bValue = false;
            }

            reqEx.setBitValue(bValue);

            // Check if this value is different from the old value.
            if ((oldReqEx == null) || (oldReqEx.getBitValue() != reqEx.getBitValue())) {

                // User tried to change the value of this field.
                // Check if he has change permission on the field.
                String  key        = field.getName();
                int     permission = 0;
                Integer temp       = myPermTable.get(key);

                if (temp != null) {
                    permission = temp;
                } else {
                    permission = 0;
                }

                if ((permission & Permission.CHANGE) == 0) {
                    myException.addException(new TBitsException(noPermission(field.getDisplayName(), "Change", myBusinessArea.getSystemPrefix(),  myUser.getUserLogin())), PERROR);

                    // Continue with the old value., if any ,else add default
                    if (oldReqEx == null) {
                        reqEx.setBitValue(false);
                    } else {
                        reqEx = oldReqEx;
                    }
                } else if (oldReqEx != null) {

                    //
                    // Since the user has permission to change this extended
                    // bit field, put an entry in the extdifflist which will
                    // be used if this append qualified for diff checking.
                    //
                    DiffEntry de = new DiffEntry(field.getName(), field.getDisplayName(), Boolean.toString(oldReqEx.getBitValue()), Boolean.toString(reqEx.getBitValue()));

                    myExtDiffTable.put(field.getName().toUpperCase(), de);
                }
            }
        }

        return reqEx;
    }

    /**
     * This method handles the value specified for a datetime extended field.
     *
     * @param field Field object corresponding to this extended field.
     * @param value Value specified for this extended field.
     * @param isFieldSpecified 
     *
     * @return RequestEx object.
     */
    private RequestEx handleDateExtendedField(Field field, String value, boolean isFieldSpecified) {
        RequestEx reqEx = new RequestEx();

        reqEx.setSystemId(mySystemId);
        reqEx.setRequestId(myRequestId);
        reqEx.setFieldId(field.getFieldId());

        // Get the old value for this field if present in the table.
        RequestEx oldReqEx = myExtendedFields.get(field);

        /**
         * Here goes the logic:
         * If the value is null, 
         * 	if specified, 
         * 		set it null
         * 	if !specified
         * 		if reset
         * 			set old value
         * 		else
         * 			set null
         */
        if (value == null) {
        	
        	
        	/*
        	 * IF the field is not specified, roll back its value if its not "SET" .
        	 */
        	
        	if(isFieldSpecified)
        	{
        		reqEx.setDateTimeValue(null);
        	}
        	else
        	{
        		if( (field.getPermission() & Permission.SET) == 0)
        		{
        			reqEx.setDateTimeValue(null);
        		}
        		else
        		{
        			if(oldReqEx != null)
        				reqEx.setDateTimeValue(oldReqEx.getDateTimeValue());
        			else
        				reqEx.setDateTimeValue(null);
        		}
        	}
        	
        }
        else if(value.trim().length() == 0)
        {
        	reqEx.setDateTimeValue(null);
        }
        else {

            // Fields of these types will be string form of dates
            // in yyyy-MM-dd HH:mm:ss format.
            try {
                //Timestamp tValue = APIUtil.parseDateTime(value);
            	String webDateFormat = myUser.getWebConfigObject().getWebDateFormat();
//            	if (webDateFormat.equals("MM/dd/yyyy HH:mm:ss.SSS") || webDateFormat.equals("MM/dd/yyyy HH:mm:ss zzz"))
            		webDateFormat = TBitsConstants.API_DATE_FORMAT;
            	DateFormat df = new SimpleDateFormat(webDateFormat);
            	df.setTimeZone(TimeZone.getDefault());
            	Date d = null;
				try {
					d = df.parse(value);
				} catch (ParseException e) {
					throw new TBitsException("Couldnt parse '" + value + "'. Format should be " + TBitsConstants.API_DATE_FORMAT);
				}
            	Timestamp tValue = new Timestamp(d.getTime());
                
                reqEx.setDateTimeValue(tValue);

                // Check if this value is different from the old value.
                if ((oldReqEx == null) || (oldReqEx.getDateTimeValue() == null) || (oldReqEx.getDateTimeValue().equals(reqEx.getDateTimeValue()) == false)) {

                    // User tried to change the value of this field.
                    // Check if he has change permission on the field.
                    String  key        = field.getName();
                    int     permission = 0;
                    Integer temp       = myPermTable.get(key);

                    if (temp != null) {
                        permission = temp;
                    } else {
                        permission = 0;
                    }

                    if ((permission & Permission.CHANGE) == 0) {
                        myException.addException(new TBitsException(noPermission(field.getDisplayName(), "Change", myBusinessArea.getSystemPrefix(),  myUser.getUserLogin())), PERROR);

                        // Continue with the old value.
                        if (oldReqEx == null) {
                            reqEx.setDateTimeValue(Timestamp.getGMTNow());
                        } else {
                            reqEx = oldReqEx;
                        }
                    }

                    /*
                     *   else if (oldReqEx != null)
                     * {
                     *
                     *   // Since the user has permission to change this
                     *   // extended date field, put an entry in the extdifflist
                     *   // which will be used if this append qualified for diff
                     *   // checking.
                     *
                     *   DiffEntry de = new DiffEntry
                     *       (field.getName(),
                     *        field.getDisplayName(),
                     *        oldReqEx.getDateTimeValue().
                     *        toCustomFormat("MM/dd/yyyy HH:mm"),
                     *        reqEx.getDateTimeValue().
                     *        toCustomFormat("MM/dd/yyyy HH:mm"));
                     *   myExtDiffTable.put(field.getName().toUpperCase(), de);
                     *   }
                     */
                }
            } catch (TBitsException de) {
                LOG.severe("",(de));
                reqEx = oldReqEx;
            }
        }

        return reqEx;
    }

    /**
     * This method handles the value specified for an extended field.
     *
     * @param field Field object corresponding to this extended field.
     * @param value Value specified for this extended field.
     * @param isFieldSpecified 
     *
     * @return RequestEx object.
     */
    private RequestEx handleExtendedField(Field field, String value, boolean isFieldSpecified) {
        RequestEx reqEx = new RequestEx();

        reqEx.setSystemId(mySystemId);
        reqEx.setRequestId(myRequestId);
        reqEx.setFieldId(field.getFieldId());

        int dataTypeId = field.getDataTypeId();

        // Parse the value based on the data type.
        switch (dataTypeId) {
        case DataType.BOOLEAN :
            reqEx = handleBooleanExtendedField(field, value, isFieldSpecified);

            break;

        case DataType.DATE :
        case DataType.TIME :
        case DataType.DATETIME :
            reqEx = handleDateExtendedField(field, value, isFieldSpecified);

            break;

        case DataType.INT :
            reqEx = handleIntegerExtendedField(field, value, isFieldSpecified);

            break;

        case DataType.REAL :
            reqEx = handleRealExtendedField(field, value, isFieldSpecified);

            break;

        case DataType.STRING :
        case DataType.TEXT :
            reqEx = handleStringExtendedField(field, value, isFieldSpecified);

            break;

        case DataType.TYPE :
            reqEx = handleTypeExtendedField(field, value, isFieldSpecified);

            break;

        case DataType.USERTYPE :
        { 	
        	LOG.info("The extended UserTypes are handled while handling the fixed usertypes.So Ignoring here.");
        	// not implemented.
        	break;
        }        
        case DataType.ATTACHMENTS :
        	reqEx = handleAttachmentType(field, value, isFieldSpecified);
        	break;
        }
        return reqEx;
    }

    /**
     * This method handles the value specified for a integer extended field.
     *
     * @param field Field object corresponding to this extended field.
     * @param value Value specified for this extended field.
     * @param isFieldSpecified 
     *
     * @return RequestEx object.
     */
    private RequestEx handleIntegerExtendedField(Field field, String value, boolean isFieldSpecified) {
        RequestEx reqEx = new RequestEx();

        reqEx.setSystemId(mySystemId);
        reqEx.setRequestId(myRequestId);
        reqEx.setFieldId(field.getFieldId());

        // Get the old value for this field if present in the table.
        RequestEx oldReqEx = myExtendedFields.get(field);

        if (value == null) {

            // Check if there is an old value for this field.
            if (oldReqEx == null) {

                // Possibly this is a newly added extended field.
                // insert with default value into requests_ex table.
                reqEx.setIntValue(0);
            } 
            else if((field.getPermission() & Permission.SET) == 0){
            	reqEx.setIntValue(0);
            }
            else
            {
                // retain the old value.
                reqEx = oldReqEx;
            }
        } else {

            // Parse it as an integer.
            int iValue = 0;

            try {
                iValue = Integer.parseInt(value);
            } catch (NumberFormatException e) {
                iValue = 0;
            }

            reqEx.setIntValue(iValue);

            // Check if this value is different from the old value.
            if ((oldReqEx == null) || (oldReqEx.getIntValue() != reqEx.getIntValue())) {

                // User tried to change the value of this field.
                // Check if he has change permission on the field.
                String  key        = field.getName();
                int     permission = 0;
                Integer temp       = myPermTable.get(key);

                if (temp != null) {
                    permission = temp;
                } else {
                    permission = 0;
                }

                if ((permission & Permission.CHANGE) == 0) {
                    myException.addException(new TBitsException(noPermission(field.getDisplayName(), "Change", myBusinessArea.getSystemPrefix(),  myUser.getUserLogin())), PERROR);

                    // Continue with the old value.
                    if (oldReqEx == null) {
                        reqEx.setIntValue(0);
                    } else {
                        reqEx = oldReqEx;
                    }
                } else if (oldReqEx != null) {

                    //
                    // Since the user has permission to change this extended
                    // int field, put an entry in the extdifflist which will
                    // be used if this append qualified for diff checking.
                    //
                    DiffEntry de = new DiffEntry(field.getName(), field.getDisplayName(), Integer.toString(oldReqEx.getIntValue()), Integer.toString(reqEx.getIntValue()));

                    myExtDiffTable.put(field.getName().toUpperCase(), de);
                }
            }
        }

        return reqEx;
    }

    private RequestEx handleAttachmentType(Field field, String value, boolean isFieldSpecified) {

        RequestEx reqEx = new RequestEx();

        reqEx.setSystemId(mySystemId);
        reqEx.setRequestId(myRequestId);
        reqEx.setFieldId(field.getFieldId());
        

    	if(!isFieldSpecified)
    	{
            // Get the old value for this field if present in the table.
            RequestEx oldReqEx = myExtendedFields.get(field);
            if(oldReqEx == null)
            {
            	reqEx = null;
            }
            else
            	reqEx.setTextValue(oldReqEx.getTextValue());
    	}
    	else
    	{
    	       reqEx.setTextValue(value);
    	}
        return reqEx;
    }
    /**
     * This method handles the value specified for a double extended field.
     *
     * @param field Field object corresponding to this extended field.
     * @param value Value specified for this extended field.
     * @param isFieldSpecified 
     *
     * @return RequestEx object.
     */
    private RequestEx handleRealExtendedField(Field field, String value, boolean isFieldSpecified) {
        RequestEx reqEx = new RequestEx();

        reqEx.setSystemId(mySystemId);
        reqEx.setRequestId(myRequestId);
        reqEx.setFieldId(field.getFieldId());

        // Get the old value for this field if present in the table.
        RequestEx oldReqEx = myExtendedFields.get(field);

        if (value == null) {

            // Check if there is an old value for this field.
            if (oldReqEx == null) {

                // Possibly this is a newly added extended field.
                // insert with default value into requests_ex table.
                reqEx.setRealValue(0);
            } 
            else if((field.getPermission() & Permission.SET) == 0){
            	reqEx.setRealValue(0.0);
            }
            else {

                // retain the old value.
                reqEx = oldReqEx;
            }
        } else {

            // Parse it as a double value.
            double dValue = 0;

            try {
                dValue = Double.parseDouble(value);
            } catch (NumberFormatException e) {
                dValue = 0;
            }

            reqEx.setRealValue(dValue);

            // Check if this value is different from the old value.
            if ((oldReqEx == null) || (oldReqEx.getRealValue() != reqEx.getRealValue())) {

                // User tried to change the value of this field.
                // Check if he has change permission on the field.
                String  key        = field.getName();
                int     permission = 0;
                Integer temp       = myPermTable.get(key);

                if (temp != null) {
                    permission = temp;
                } else {
                    permission = 0;
                }

                if ((permission & Permission.CHANGE) == 0) {
                    myException.addException(new TBitsException(noPermission(field.getDisplayName(), "Change", myBusinessArea.getSystemPrefix(),  myUser.getUserLogin())), PERROR);

                    // Continue with the old value.
                    if (oldReqEx == null) {
                        reqEx.setRealValue(0);
                    } else {
                        reqEx = oldReqEx;
                    }
                } else if (oldReqEx != null) {

                    //
                    // Since the user has permission to change this extended
                    // real field, put an entry in the extdifflist which will
                    // be used if this append qualified for diff checking.
                    //
                    DiffEntry de = new DiffEntry(field.getName(), field.getDisplayName(), Double.toString(oldReqEx.getRealValue()), Double.toString(reqEx.getRealValue()));

                    myExtDiffTable.put(field.getName().toUpperCase(), de);
                }
            }
        }

        return reqEx;
    }

    /**
     * This method handles the value specified for a String extended field.
     *
     * @param field Field object corresponding to this extended field.
     * @param value Value specified for this extended field.
     * @param isFieldSpecified 
     *
     * @return RequestEx object.
     */
    private RequestEx handleStringExtendedField(Field field, String value, boolean isFieldSpecified) {
        RequestEx reqEx = new RequestEx();

        reqEx.setSystemId(mySystemId);
        reqEx.setRequestId(myRequestId);
        reqEx.setFieldId(field.getFieldId());

        // Get the old value for this field if present in the table.
        RequestEx oldReqEx = myExtendedFields.get(field);
        String    strValue = "";

        if (value == null) {

            // Check if there is an old value for this field.
            if (oldReqEx == null) {

                // Possibly this is a newly added extended field.
                // insert with default value into requests_ex table.
                strValue = "";
            } 
            else if((field.getPermission() & Permission.SET) == 0){
            	reqEx.setVarcharValue("");
            }
            else {
                // retain the old value.
                reqEx = oldReqEx;
            }
        } else {

            // Set as is.
            strValue = value;

            if (field.getDataTypeId() == DataType.STRING) {
                reqEx.setVarcharValue(strValue);

                // Check if this value is different from the old value.
                if ((oldReqEx == null) || (oldReqEx.getVarcharValue().equals(reqEx.getVarcharValue()) == false)) {

                    // User tried to change the value of this field.
                    // Check if he has change permission on the field.
                    String  key        = field.getName();
                    int     permission = 0;
                    Integer temp       = myPermTable.get(key);

                    if (temp != null) {
                        permission = temp;
                    } else {
                        permission = 0;
                    }

                    if ((permission & Permission.CHANGE) == 0) {
                        myException.addException(new TBitsException(noPermission(field.getDisplayName(), "Change", myBusinessArea.getSystemPrefix(),  myUser.getUserLogin())), PERROR);

                        // Continue with the old value.
                        if (oldReqEx == null) {
                            reqEx.setVarcharValue("");
                        } else {
                            reqEx = oldReqEx;
                        }
                    } else if (oldReqEx != null) {

                        //
                        // Since the user has permission to change this
                        // extended String field, put an entry in the
                        // extdifflist which will be used if this append
                        // qualified for diff checking.
                        //
                        DiffEntry de = new DiffEntry(field.getName(), field.getDisplayName(), oldReqEx.getVarcharValue(), reqEx.getVarcharValue());

                        myExtDiffTable.put(field.getName().toUpperCase(), de);
                    }
                }
            } else if (field.getDataTypeId() == DataType.TEXT) {
                strValue = LinkFormatter.replaceHrefWithSmartLinks(value);
                reqEx.setTextValue(strValue);
                System.out.println("oldReqEx = " + oldReqEx);
//                System.out.println("oldReqEx.text = " + oldReqEx.getTextValue());
                // Check if this value is different from the old value.
                if ((oldReqEx == null) || (oldReqEx.getTextValue().equals(reqEx.getTextValue()) == false)) {

                    // User tried to change the value of this field.
                    // Check if he has change permission on the field.
                    String  key        = field.getName();
                    int     permission = 0;
                    Integer temp       = myPermTable.get(key);

                    if (temp != null) {
                        permission = temp;
                    } else {
                        permission = 0;
                    }

                    if ((permission & Permission.CHANGE) == 0) {
                        myException.addException(new TBitsException(noPermission(field.getDisplayName(), "Change", myBusinessArea.getSystemPrefix(),  myUser.getUserLogin())), PERROR);

                        // Continue with the old value.
                        if (oldReqEx == null) {
                            reqEx.setTextValue("");
                        } else {
                            reqEx = oldReqEx;
                        }
                    } else if (oldReqEx != null) {

                        //
                        // Since the user has permission to change this
                        // extended String field, put an entry in the
                        // extdifflist which will be used if this append
                        // qualified for diff checking.
                        //
                        DiffEntry de = new DiffEntry(field.getName(), field.getDisplayName(), oldReqEx.getTextValue(), reqEx.getTextValue());

                        myExtDiffTable.put(field.getName().toUpperCase(), de);
                    }
                }
            }
        }

        return reqEx;
    }

    /**
     * This method handles the value specified for a MultiValue extended field.
     *
     * @param field Field object corresponding to this extended field.
     * @param value Value specified for this extended field.
     * @param isFieldSpecified 
     *
     * @return RequestEx object.
     * @throws APIException 
     */
    
    private ArrayList<RequestUser> handleUserTypeExtendedField(Field field, String value) throws APIException {
//        RequestEx reqEx = new RequestEx();
//
//        reqEx.setSystemId(mySystemId);
//        reqEx.setRequestId(myRequestId);
//        reqEx.setFieldId(field.getFieldId());
//    	ArrayList<RequestUser> reqUsers = null ; //processUserField(value,field, UserType.USERTYPE, INSERT, NO_AUTO, NO_BA_EMAIL, NO_EXCLUSION_LIST,oldUsers );
    	Collection<RequestUser> oldUsers = null ;
        oldUsers = myOldRequest.getExUserType(field);
       
        String    strValue = null;

        if (value == null) {
            // Check if there is an old value for this field.
            if (oldUsers == null) {
                // Possibly this is a newly added extended field.
                // insert with default value into requests_ex table.
                strValue = "";
            } 
            else if((field.getPermission() & Permission.SET) == 0){
            	strValue = "" ;
            }
            else {
                // retain the old value.
            	strValue = APIUtil.toLoginList(oldUsers);
            }
        } else {

        	Integer perm = myPermTable.get(field.getName());
        	if( null == perm )
        		perm = 0 ;
        	if( (perm & Permission.CHANGE) == 0 )
        		myException.addException(new TBitsException(noPermission(field.getDisplayName(), "Change", myBusinessArea.getSystemPrefix(),  myUser.getUserLogin())), PERROR);

        	// Set as is.
        	strValue = value;        	
        	// Check if this value is different from the old value.
        }

        return processUserField(strValue,field, UserType.USERTYPE, INSERT, NO_AUTO, NO_BA_EMAIL, NO_EXCLUSION_LIST,oldUsers );
    }
    
    
    /**
     * This method handles the value specified for a type extended field.
     *
     * @param field Field object corresponding to this extended field.
     * @param value Value specified for this extended field.
     * @param isFieldSpecified 
     *
     * @return RequestEx object.
     */
    private RequestEx handleTypeExtendedField(Field field, String value, boolean isFieldSpecified) {
        RequestEx reqEx = new RequestEx();

        reqEx.setSystemId(mySystemId);
        reqEx.setRequestId(myRequestId);
        reqEx.setFieldId(field.getFieldId());

        // Get the old value for this field if present in the table.
        RequestEx oldReqEx = myExtendedFields.get(field);
        int       iValue   = 0;
        Type      type     = null;

        if (value == null) {

            // Check if there is an old value for this field.
            if ((oldReqEx == null) || ((field.getPermission() & Permission.SET) == 0)) {

                // Possibly this is a newly added extended field.
                // insert with default value into requests_ex table.
                try {
                    type = Type.getDefaultTypeBySystemIdAndFieldName(mySystemId, field.getName());

                    if (type == null) {
                        myException.addException(new TBitsException("No default specified for " + field.getName()), SEVERE);
                    }
                    else
                    {
                    	// creating the new RequestEx from this type value
                    	reqEx.setTypeValue(type.getTypeId()) ;
                    }
                } catch (DatabaseException de) {
                    LOG.warn("",(de));
                    myException.addException(new TBitsException(de.toString()), SEVERE);
                }
            }
            else {
                // retain the old value.
                reqEx = oldReqEx;
            }
        } else {
            try {
                type = Type.lookupBySystemIdAndFieldNameAndTypeName(mySystemId, field.getName(), value);

                if (type == null) {
                    myException.addException(new TBitsException(Messages.getMessage("INVALID_TYPE", field.getDisplayName(), value)), SEVERE);
                } else {
                    iValue = type.getTypeId();
                    reqEx.setTypeValue(iValue);

                    // Check if this value is different from the old value.
                    if ((oldReqEx == null) || (oldReqEx.getTypeValue() != reqEx.getTypeValue())) 
                    {
                        // User tried to change the value of this field.
                        // Check if he has change permission on the field.
                        String  key        = field.getName();
                        int     permission = 0;
                        Integer temp       = myPermTable.get(key);

                        if (temp != null) {
                            permission = temp;
                        } else {
                            permission = 0;
                        }

                        if ((permission & Permission.CHANGE) == 0) {
                            myException.addException(new TBitsException(noPermission(field.getDisplayName(), "Change", myBusinessArea.getSystemPrefix(),  myUser.getUserLogin())), SEVERE);
                        }   
                        else if( oldReqEx != null ){                        	
                            String oldValue = "";
                            String newValue = type.getDisplayName();
                            Type   oldType  = Type.lookupBySystemIdAndFieldIdAndTypeId(mySystemId, field.getFieldId(), oldReqEx.getTypeValue());

                            if (oldType != null) {
                                oldValue = oldType.getDisplayName();
                            }

                            //
                            // Since the user has permission to change this
                            // extended type field, put an entry in the
                            // extdifflist which will be used if this append
                            // qualified for diff checking.
                            //
                            DiffEntry de = new DiffEntry(field.getName(), field.getDisplayName(), oldValue, newValue);

                            myExtDiffTable.put(field.getName().toUpperCase(), de);
                        }

                     }
                }
            } catch (DatabaseException de) {
                LOG.warn("",(de));
                myException.addException(new TBitsException(de.toString()), SEVERE);
            }
        }

        return reqEx;
    }

    /**
     * @param conn 
     *
     *
     *
     */
    private void init(Connection conn) throws APIException {

        // Get the BA Configuration.
        mySysConfig      = myBusinessArea.getSysConfigObject();
        defNotify        = mySysConfig.getActionNotify();
        defNotifyLoggers = mySysConfig.getActionNotifyLoggers();

        //
        // Get the default due date if the BA does not allow nulls or
        // allow nulls but have a default also.
        //
        if ((mySysConfig.getAllowNullDueDate() == false) || (mySysConfig.getDefaultDueDate() != 0)) {
            defDueDate = getGMTDefaultDueDate();
        }

        // Get the fields table for this business area.
        try {
            myFieldTable = Field.getFieldsTableBySystemId(mySystemId);
        } catch (DatabaseException de) {
            LOG.warn("",(de));
            myException.addException(new TBitsException(de.toString()), FATAL);

            throw myException;
        }

        // Get the list of extended fields if any for this business area.
        try {
            myExtFieldList = Field.getExtendedFieldsBySystemId(mySystemId);
        } catch (DatabaseException de) {

            //
            // We could not get the list of fields. So, it is not advisable
            // to continue in such a case. So re-throw this exception as
            // APIException.
            //
            LOG.error("",(de));
            myException.addException(new TBitsException(de.toString()), FATAL);

            throw myException;
        }

        try {
            myPermTable = RolePermission.getPermissionsBySystemIdAndRequestIdAndUserId(conn, mySystemId, myRequestId, myUser.getUserId());
        } catch (DatabaseException de) {
            LOG.warn("",(de));
            myException.addException(new TBitsException(de.toString()), SEVERE);

            throw myException;
        } catch (SQLException e) {
            LOG.warn("",(e));
            myException.addException(new TBitsException(e.toString()), SEVERE);

            throw myException;
		}

        if (myPermTable == null) {
            StringBuilder message = new StringBuilder();

            message.append("\n\nUnable to obtain permissions for the user:").append("\nUser     : " + myUser.getUserLogin()).append("\nSystem Id: " + myBusinessArea.getDisplayName());
            myException.addException(new TBitsException(message.toString()), FATAL);

            throw myException;
        }
    }

    /**
     * The main method.
     */
    public static void main(String arg[]) throws Exception {
        if (arg.length != 1) {
            System.err.println("Usage: UpdateRequest <Comma-separated list of " + "colon separated field-value pairs.>");
            return;
        }

        StringTokenizer           st         = new StringTokenizer(arg[0], ",");
        Hashtable<String, String> paramTable = new Hashtable<String, String>();

        while (st.hasMoreTokens()) {
            String          str = st.nextToken();
            StringTokenizer ist = new StringTokenizer(str, ":=");

            try {
                String key = ist.nextToken().trim();
                String val = ist.nextToken().trim();

                paramTable.put(key, val);
            } catch (Exception e) {
                LOG.warn("",(e));
            }
        }

        try {
        	if(paramTable.get(Field.USER) == null)
        		paramTable.put(Field.USER, System.getProperties().getProperty("user.name"));

            UpdateRequest app     = new UpdateRequest();
            app.setSource(TBitsConstants.SOURCE_CMDLINE);
            Request       request = app.updateRequest(paramTable);

            LOG.info("Request Id: " + request.getRequestId());
            LOG.info("Action  Id: " + request.getMaxActionId());
        } catch (APIException api) {
            LOG.warn("",(api));
            return;
        } catch (Exception e) {
            LOG.warn("",(e));
            return;
        } finally {
            //transbit.tbits.api.Mapper.stop();
            //DataSourcePool.shutdownPooling();
        }
        return;
    }

    /**
     * This method modifies the current value of a field using the specified
     * operator.
     *
     * @param fieldName  Name of the field.
     * @param value      Value specified by the rule.
     * @param operator   Operator to be applied.
     *
     */
    private void modify(String fieldName, String value, Operator operator) {
        Field field = myFieldTable.get(fieldName);

        if (field == null) {
            return;
        }

        RequestEx reqEx = null;

        if (field.getIsExtended() == true) {
            reqEx = myExtendedFields.get(field);

            if (reqEx == null) {
                return;
            }
        }

        int dataType = field.getDataTypeId();

        switch (dataType) {
        case BOOLEAN :
            APIUtil.modifyBoolean(myRequest, reqEx, field, value, operator);

            break;

        case TIME :
        case DATE :
        case DATETIME :
            APIUtil.modifyDate(myRequest, reqEx, field, value, operator);

            break;

        case INT :
            APIUtil.modifyInteger(myRequest, reqEx, field, value, operator);

            break;

        case REAL :
            APIUtil.modifyReal(myRequest, reqEx, field, value, operator);

            break;

        case STRING :
            APIUtil.modifyString(myRequest, reqEx, field, value, operator);

            break;

        case TEXT :
            APIUtil.modifyText(myRequest, reqEx, field, value, operator);

            break;

        case TYPE :
            APIUtil.modifyType(myRequest, reqEx, field, value, operator);

            break;

        case USERTYPE :
            APIUtil.modifyUserType(myRequest, field, value, operator);

            break;
        }

        if (reqEx != null) {
            myExtendedFields.put(field, reqEx);
        }
    }

    private String noPermission(String aField, String aPermission,String sysPrefix, String userLogin) {
        return Messages.getMessage("NO_PERMISSION", aField, aPermission.toLowerCase(), sysPrefix, userLogin);
    }

    /**
     * This method perform the integrity checks.
     *
     */
    private void performIntegrityChecks() throws APIException {

        //
        // 1. Logger list cannot be empty.
        // If it is, we consider the old list but register an exception in the
        // myExceptionList at severe level.
        //
        String defunctUsers = myDefunctUsers.get(Field.LOGGER);

        if ((myLoggers == null) || (myLoggers.size() == 0) || (defunctUsers != null)) {

            //
            // If loggers was intentionally empty , throw wmtpty list error
            // else if inactive users added, throw their list.
            //
            if ((defunctUsers == null) || defunctUsers.trim().equals("")) {
                myException.addException(new TBitsException(Messages.getMessage("LOGGER_MANDATORY")), SEVERE);
            } else {
                ArrayList<String> users = Utilities.toArrayList(defunctUsers);
                User              user  = null;

                for (String login : users) {
                    try {
                        user = User.lookupAllByUserLogin(login);
                    } catch (DatabaseException de) {
                        myException.addException(new TBitsException(de.toString()), WARNING);
                        LOG.warn("",(de));
                    }

                    if (user != null) {
                        myException.addException(new TBitsException(user.getDisplayName() + " is not a valid logger."), SEVERE);
                    } else {
                        myException.addException(new TBitsException(login + " is not a valid logger."), SEVERE);
                    }
                }
            }

            myLoggers = (myOldRequest.getLoggers() == null ? null : new ArrayList<RequestUser>(myOldRequest.getLoggers()));
        }

        //
        // 2. Due date cannot be earlier than logged date.
        // If it is, we consider the old due date and register an exception in
        // the myExceptionList at severe level.
        //
//        if (myDueDate != null) {
//            if (myDueDate.getTime() < myLoggedDate.getTime()) {
//                myException.addException(new TBitsException(Messages.getMessage("PAST_DUE_DATE")), SEVERE);
//                myDueDate = myOldRequest.getDueDate();
//
//                if ((myDueDate == null) && (mySysConfig.getAllowNullDueDate() == false)) {
//                    myDueDate = defDueDate;
//                }
//            }
//        }

        //
        // 3. If the user is appending to a request without view on is_private
        // then none of the following can be private.
        // - Business Area.
        // - Request.
        // - Standard Types: Category, Status, Severity, Request Type.
        //
        String  key        = "";
        int     permission = 0;
        Integer temp       = null;

        if (myPermTable != null) {
            key  = Field.IS_PRIVATE;
            temp = myPermTable.get(key);

            if (temp != null) {
                permission = temp;
            } else {
                permission = 0;
            }

            if ((permission & VIEW) == 0) {

                // User does not view permission on is_private.
                if ((myBusinessArea.getIsPrivate() == true) || (myOldRequest.getIsPrivate() == true) || (myCategory.getIsPrivate() == true) || (myStatus.getIsPrivate() == true)
                        || (mySeverity.getIsPrivate() == true) || (myRequestType.getIsPrivate() == true)) {
                    myException.addException(new TBitsException(Messages.getMessage("NO_PRIVATE_PERMISSION")), SEVERE);
                }
            }
        } else {
            LOG.warn("Permission table is null during integrity checking.");
        }
    }

    /**
     * This method obtains the permissions the user has in this Business Area.
     * Then it checks if the state of request object is valid according to the
     * permissions the user has.
     * While checking each field, if the user does not have permissions, it
     * considers the oldvalue for that field incase we continue to update the
     * request.
     */
    private void performPermissionChecking() throws APIException {
        String  key        = "";
        int     permission = 0;
        Field   field      = null;
        Integer temp       = null;

        //
        // For each fixed field, we see if the user has relevant permissions.
        // We perform a check only if the user does not have permissions.
        //
        // 1. SystemId
        // Key        : Field.BUSINESS_AREA
        // Permission : Permission.VIEW
        // Exemption  : If from email.
        key   = Field.BUSINESS_AREA;
        field = myFieldTable.get(key);
        temp  = myPermTable.get(key);

        if (temp != null) {
            permission = temp;
        } else {
            permission = 0;
        }

        if ((permission & VIEW) == 0) {
            myException.addException(new TBitsException(noPermission(field.getDisplayName(), "View", myBusinessArea.getSystemPrefix(),  myUser.getUserLogin())), PERROR);
        }

        // 2. Request Id
        // Key        : Field.REQUEST
        // Permission : Permission.CHANGE
        // Exemption  : If from email.
        key   = Field.REQUEST;
        field = myFieldTable.get(key);
        temp  = myPermTable.get(key);

        if (temp != null) {
            permission = temp;
        } else {
            permission = 0;
        }

        if ((permission & CHANGE) == 0) {
            myException.addException(new TBitsException(noPermission(field.getDisplayName(), "Change", myBusinessArea.getSystemPrefix(),  myUser.getUserLogin())), PERROR);
        }

        // 3. Category
        // Key        : Field.CATEGORY
        // Permission : Permission.CHANGE
        // Exemption  : If from email.
        key   = Field.CATEGORY;
        field = myFieldTable.get(key);
        temp  = myPermTable.get(key);

        if (temp != null) {
            permission = temp;
        } else {
            permission = 0;
        }

        if (((permission & CHANGE) == 0) && (myCategory.equals(myOldRequest.getCategoryId()) == false)) {
            myException.addException(new TBitsException(noPermission(field.getDisplayName(), "Change", myBusinessArea.getSystemPrefix(),  myUser.getUserLogin())), PERROR);

            // Take the previous value in case you update the request.
            myCategory = myOldRequest.getCategoryId();
        }

        // 4. Status
        // Key        : Field.STATUS
        // Permission : Permission.CHANGE
        // Exemption  : If from email.
        key   = Field.STATUS;
        field = myFieldTable.get(key);
        temp  = myPermTable.get(key);

        if (temp != null) {
            permission = temp;
        } else {
            permission = 0;
        }

        if (((permission & CHANGE) == 0) && (myStatus.equals(myOldRequest.getStatusId()) == false)) {
            myException.addException(new TBitsException(noPermission(field.getDisplayName(), "Change", myBusinessArea.getSystemPrefix(),  myUser.getUserLogin())), PERROR);

            // Take the previous value in case you update the request.
            myStatus = myOldRequest.getStatusId();
        }

        // 5. Severity
        // Key        : Field.SEVERITY
        // Permission : Permission.CHANGE
        // Exemption  : If from email.
        key   = Field.SEVERITY;
        field = myFieldTable.get(key);
        temp  = myPermTable.get(key);

        if (temp != null) {
            permission = temp;
        } else {
            permission = 0;
        }

        if (((permission & CHANGE) == 0) && (mySeverity.equals(myOldRequest.getSeverityId()) == false)) {
            myException.addException(new TBitsException(noPermission(field.getDisplayName(), "Change", myBusinessArea.getSystemPrefix(),  myUser.getUserLogin())), PERROR);

            // Take the previous value in case you update the request.
            mySeverity = myOldRequest.getSeverityId();
        }

        // 6. Request Type
        // Key        : Field.REQUEST_TYPE
        // Permission : Permission.CHANGE
        // Exemption  : If from email.
        key   = Field.REQUEST_TYPE;
        field = myFieldTable.get(key);
        temp  = myPermTable.get(key);

        if (temp != null) {
            permission = temp;
        } else {
            permission = 0;
        }

        if (((permission & CHANGE) == 0) && (myRequestType.equals(myOldRequest.getRequestTypeId()) == false)) {
            myException.addException(new TBitsException(noPermission(field.getDisplayName(), "Change", myBusinessArea.getSystemPrefix(),  myUser.getUserLogin())), PERROR);

            // Take the previous value in case you update the request.
            myRequestType = myOldRequest.getRequestTypeId();
        }

        // 7. Logger
        // Key        : Field.LOGGER;
        // Permission : Permission.CHANGE
        // Exemption  : If from email.
        key   = Field.LOGGER;
        field = myFieldTable.get(key);
        temp  = myPermTable.get(key);

        if (temp != null) {
            permission = temp;
        } else {
            permission = 0;
        }

        if ((permission & CHANGE) == 0 && (isChanged(myLoggers, (myOldRequest.getLoggers() == null ? null : new ArrayList<RequestUser>(myOldRequest.getLoggers()))) == true)) {
            myException.addException(new TBitsException(noPermission(field.getDisplayName(), "Change", myBusinessArea.getSystemPrefix(),  myUser.getUserLogin())), PERROR);

            // Take the previous value in case you update the request.
            myLoggers = (myOldRequest.getLoggers() == null ? null : new ArrayList<RequestUser>(myOldRequest.getLoggers()));
        }

        // 8. Assignee
        // Key        : Field.ASSIGNEE
        // Permission : Permission.CHANGE
        // Exemption  : If from email.
        key   = Field.ASSIGNEE;
        field = myFieldTable.get(key);
        temp  = myPermTable.get(key);

        if (temp != null) {
            permission = temp;
        } else {
            permission = 0;
        }

        if ((permission & CHANGE) == 0 && (isChanged(myAssignees, myOldRequest.getAssignees()) == true)) {
            myException.addException(new TBitsException(noPermission(field.getDisplayName(), "Change", myBusinessArea.getSystemPrefix(),  myUser.getUserLogin())), PERROR);

            // Take the previous value in case you update the request.
            myAssignees = (myOldRequest.getAssignees() == null ? null : new ArrayList<RequestUser>(myOldRequest.getAssignees()));
        }

        // 9. Subscriber
        // Key        : Field.SUBSCRIBER
        // Permission : Permission.CHANGE
        // Exemption  : If from email.
        key   = Field.SUBSCRIBER;
        field = myFieldTable.get(key);
        temp  = myPermTable.get(key);

        if (temp != null) {
            permission = temp;
        } else {
            permission = 0;
        }

        if ((permission & CHANGE) == 0 && (isChanged(mySubscribers, myOldRequest.getSubscribers()) == true)) {
            myException.addException(new TBitsException(noPermission(field.getDisplayName(), "Change", myBusinessArea.getSystemPrefix(),  myUser.getUserLogin())), PERROR);
            mySubscribers = (myOldRequest.getSubscribers() == null ? null : new ArrayList<RequestUser>(myOldRequest.getSubscribers()));
        }

        // 10.  To
        // Key        : Field.TO
        // Permission : Permission.ADD
        // Exemption  : If from email.
        key   = Field.TO;
        field = myFieldTable.get(key);
        temp  = myPermTable.get(key);

        if (temp != null) {
            permission = temp;
        } else {
            permission = 0;
        }

        if ((permission & CHANGE) == 0) {
            if (myTos.size() > 0) {
                myException.addException(new TBitsException(noPermission(field.getDisplayName(), "Change", myBusinessArea.getSystemPrefix(),  myUser.getUserLogin())), PERROR);

                // Empty the To list.
                myTos = new ArrayList<RequestUser>();
            }
        }

        // 11. Cc
        // Key        : Field.CC
        // Permission : Permission.ADD
        // Exemption  : If from email.
        key   = Field.CC;
        field = myFieldTable.get(key);
        temp  = myPermTable.get(key);

        if (temp != null) {
            permission = temp;
        } else {
            permission = 0;
        }

        if ((permission & CHANGE) == 0) {
            if (myCcs.size() > 0) {
                myException.addException(new TBitsException(noPermission(field.getDisplayName(), "Change", myBusinessArea.getSystemPrefix(),  myUser.getUserLogin())), PERROR);

                // Empty the Cc list.
                myCcs = new ArrayList<RequestUser>();
            }
        }

        // 12. Subject
        // Key        : Field.SUBJECT
        // Permission : Permission.CHANGE
        // Exemption  : If from email.
        key   = Field.SUBJECT;
        field = myFieldTable.get(key);
        temp  = myPermTable.get(key);

        if (temp != null) {
            permission = temp;
        } else {
            permission = 0;
        }

        if ((permission & CHANGE) == 0) {
            if (mySubject.equals(myOldRequest.getSubject()) == false) {
                myException.addException(new TBitsException(noPermission(field.getDisplayName(), "Change", myBusinessArea.getSystemPrefix(),  myUser.getUserLogin())), PERROR);

                // Take the previous value.
                mySubject = myOldRequest.getSubject();
            }
        }

        // 13. Description
        // Key        : Field.DESCRIPTION
        // Permission : Permission.ADD
        // Exemption  : If from email.
        key   = Field.DESCRIPTION;
        field = myFieldTable.get(key);
        temp  = myPermTable.get(key);

        if (temp != null) {
            permission = temp;
        } else {
            permission = 0;
        }

        if ((permission & CHANGE) == 0) {
            if (myDescription.equals("") == false) {
                myException.addException(new TBitsException(noPermission(field.getDisplayName(), "Change", myBusinessArea.getSystemPrefix(),  myUser.getUserLogin())), PERROR);

                // Lets not clear the description.
                // myDescription = "";
            }
        }

        // 14. Is Private
        // Key        : Field.IS_PRIVATE
        // Permission : Permission.CHANGE
        // Default    : False.
        // Exemption  : If from email.
        key   = Field.IS_PRIVATE;
        field = myFieldTable.get(key);
        temp  = myPermTable.get(key);

        if (temp != null) {
            permission = temp;
        } else {
            permission = 0;
        }

        if ((permission & CHANGE) == 0 && (myIsPrivate != myOldRequest.getIsPrivate())) {
            myException.addException(new TBitsException(noPermission(field.getDisplayName(), "Change", myBusinessArea.getSystemPrefix(),  myUser.getUserLogin())), PERROR);

            // Take the old value.
            myIsPrivate = myOldRequest.getIsPrivate();
        }

        // 15. Parent Request Id
        // Key        : Field.PARENT_REQUEST_ID
        // Permission : Permission.CHANGE
        // Default    : 0
        // Exemption  : If from email.
        key   = Field.PARENT_REQUEST_ID;
        field = myFieldTable.get(key);
        temp  = myPermTable.get(key);

        if (temp != null) {
            permission = temp;
        } else {
            permission = 0;
        }

        if ((permission & CHANGE) == 0 && (myParentId != myOldRequest.getParentRequestId())) {
            myException.addException(new TBitsException(noPermission(field.getDisplayName(), "Change", myBusinessArea.getSystemPrefix(),  myUser.getUserLogin())), PERROR);

            // Take the old value.
            myParentId = myOldRequest.getParentRequestId();
        }

        // 16. User
        // Key        : Field.USER
        // Permission : Permission.VIEW
        // Exemption  : If from email.
        // This need not be checked as this is not a field exposed to the user.
        // 17. Max Action Id
        // Key        : Field.MAX_ACTION_ID
        // Permission : Permission.VIEW
        // Exemption  : If from email.
        // This need not be checked as this is not a field exposed to the user.
        // 18. Due Date
        // Key        : Field.DUE_DATE
        // Permission : Permission.CHANGE
        // Exemption  : If from email.
        key   = Field.DUE_DATE;
        field = myFieldTable.get(key);
        temp  = myPermTable.get(key);

        if (temp != null) {
            permission = temp;
        } else {
            permission = 0;
        }

        if ((permission & CHANGE) == 0) {
            if ((myOldRequest.getDueDate() == null) && (myDueDate == null)) {

                // There is no change since both are null.
            } else if ((myDueDate != null) && (myDueDate.equals(defDueDate) == true)) {

                // default taken so no check required.
            } else if (((myOldRequest.getDueDate() != null) && (myDueDate == null)) || ((myOldRequest.getDueDate() == null) && (myDueDate != null))
                       || (myDueDate.equals(myOldRequest.getDueDate()) == false)) {
                myException.addException(new TBitsException(noPermission(field.getDisplayName(), "Change", myBusinessArea.getSystemPrefix(),  myUser.getUserLogin())), PERROR);

                // Take the old value.
                myDueDate = ( myOldRequest.getDueDate() == null ? null : new Timestamp(myOldRequest.getDueDate().getTime()));
            }
        }

        // 19. Logged Date
        // Key        : Field.LOGGED_DATE
        // Permission : Permission.VIEW
        // Exemption  : If from email.
        // This need not be checked as this is not a field exposed to the user.
        // 20. Updated Date
        // Key        : Field.LASTUPDATED_DATE
        // Permission : Permission.VIEW
        // Exemption  : If from email.
        // This need not be checked as this is not a field exposed to the user.
        // 21. Header Description
        // Key        : Field.HEADER_DESCRIPTION
        // Permission : Permission.VIEW
        // Exemption  : If from email.
        // This need not be checked as this is not a field exposed to the user.
        // 22. Attachments
        // Key        : Field.ATTACHMENTS
        // Permission : Permission.ADD
        // Exemption  : If from email.
        // Nitiraj msg : We cannot judge if the attachments are added /updated / modified 
        // by the following comparison. Leave this to be done at the end where we are actually
        // looking at every attachment in the end.
//        key   = Field.ATTACHMENTS;
//        field = myFieldTable.get(key);
//        temp  = myPermTable.get(key);
//
//        if (temp != null) {
//            permission = temp;
//        } else {
//            permission = 0;
//        }
//
//        if ((permission & CHANGE) == 0 && (myAttachments.equals("") == false)) {
//            myException.addException(new TBitsException(noPermission(field.getDisplayName(), "Change")), PERROR);
//
//            // Lets not clear the attachment field.
//            // myAttachments = "";
//        }

        // 23. Summary
        // Key        : Field.SUMMARY
        // Permission : Permission.CHANGE
        // Exemption  : If from email.
        key   = Field.SUMMARY;
        field = myFieldTable.get(key);
        temp  = myPermTable.get(key);

        if (temp != null) {
            permission = temp;
        } else {
            permission = 0;
        }

        if ((permission & CHANGE) == 0 && ((mySummary != null) && (mySummary.equals(myOldRequest.getSummary()) == false))) {
            myException.addException(new TBitsException(noPermission(field.getDisplayName(), "Change", myBusinessArea.getSystemPrefix(),  myUser.getUserLogin())), PERROR);

            // Take the old summary.
            mySummary = myOldRequest.getSummary();
        }

        // 24. Memo
        // Key        : Field.MEMO
        // Permission : Permission.CHANGE
        // Exemption  : If from email.
        key   = Field.MEMO;
        field = myFieldTable.get(key);
        temp  = myPermTable.get(key);

        if (temp != null) {
            permission = temp;
        } else {
            permission = 0;
        }

        if ((permission & CHANGE) == 0 && (myMemo.equals(myOldRequest.getMemo()) == false)) {
            myException.addException(new TBitsException(noPermission(field.getDisplayName(), "Change", myBusinessArea.getSystemPrefix(),  myUser.getUserLogin())), PERROR);

            // Take the old memo.
            myMemo = myOldRequest.getMemo();
        }

        // 25. Append Interface
        // Key        :
        // Permission : Permission.VIEW
        // Exemption  : If from email.
        // This need not be checked as this is not a field exposed to the user.
        // 26. Notify
        // Key        : Field.NOTIFY
        // Permission : Permission.CHANGE
        // Default    : BA Default.
        // Exemption  : If from email.
        key   = Field.NOTIFY;
        field = myFieldTable.get(key);
        temp  = myPermTable.get(key);

        if (temp != null) {
            permission = temp;
        } else {
            permission = 0;
        }

        if ((permission & CHANGE) == 0 && (myNotify != defNotify)) {
            myException.addException(new TBitsException(noPermission(field.getDisplayName(), "Change", myBusinessArea.getSystemPrefix(),  myUser.getUserLogin())), PERROR);

            // Take the default value.
            myNotify = defNotify;
        }

        // 28. Notify Loggers
        // Key        : Field.NOTIFY_LOGGERS
        // Permission : Permission.CHANGE
        // Exemption  : If from email.
        key   = Field.NOTIFY_LOGGERS;
        field = myFieldTable.get(key);
        temp  = myPermTable.get(key);

        if (temp != null) {
            permission = temp;
        } else {
            permission = 0;
        }

        if ((permission & CHANGE) == 0 && (myNotifyLoggers != defNotifyLoggers)) {
            myException.addException(new TBitsException(noPermission(field.getDisplayName(), "Change", myBusinessArea.getSystemPrefix(),  myUser.getUserLogin())), PERROR);

            // Take the default value.
            myNotifyLoggers = defNotifyLoggers;
        }

        // 30. Office
        // Key        : Field.OFFICE
        // Permission : Permission.CHANGE
        // Exemption  : If from email.
        key   = Field.OFFICE;
        field = myFieldTable.get(key);
        temp  = myPermTable.get(key);

        if (temp != null) {
            permission = temp;
        } else {
            permission = 0;
        }

        if (((permission & CHANGE) == 0) && ((myOffice != null) && (myOffice.equals(myOldRequest.getOfficeId()) == false))) {
            myException.addException(new TBitsException(noPermission(field.getDisplayName(), "Change", myBusinessArea.getSystemPrefix(),  myUser.getUserLogin())), PERROR);

            // Take the previous value in case you update the request.
            myOffice = myOldRequest.getOfficeId();
        }

        // Check if there are any exceptions in the list above the acceptable
        // level.
        if (myException.getExceptionCount(myLevel) > 0) {
            throw myException;
        }

        return;
    }

    /**
     * This method processes the user field passed as a comma-separted string.
     *
     * @param aList        Comma-separated list of users.
     * @param aField       Field object corresponding to this user field.
     * @param aUserTypeId  UserTypeId of the user field.
     * @param bInsert      Flag to see if new users should be inserted.
     * @param bAuto        Flag to see if auto is valid in this field.
     * @param bSysEmail    Flag to see if BA Emails can be part
     * @param bExclusion   Flag to see if ExclusionList users should be allowed
     *
     * @return List of request users.
     *
     * @exception
     */
    private ArrayList<RequestUser> processUserField(String aList, Field aField, int aUserTypeId, boolean bInsert, boolean bAuto, boolean bSysEmail, boolean bExclusion, Collection<RequestUser> aOldList)
            throws APIException {
        ArrayList<User> oldUsers = new ArrayList<User>();

        if (aOldList != null) {
            try {
                for (RequestUser reqUser : aOldList) {
                    oldUsers.add(reqUser.getUser());
                }
            } catch (DatabaseException de) {
                myException.addException(new TBitsException(de.toString()), WARNING);
            }
        }

        //
        // These strings are used to form custom headers.
        // Not added to <Field_Name> List : defunctUsers + emailDefunctUsers
        // <FieldName> : baMails if allowed + other valid users.
        // If append through email then :
        // Send Through Email so not resent To : baMails + emailDefunctUsers
        //
        String                 defunctUsers      = "";
        String                 emailDefunctUsers = "";
        String                 baMails           = "";
        ArrayList<RequestUser> list              = new ArrayList<RequestUser>();
        StringTokenizer        st                = new StringTokenizer(aList, ";,");
        int                    counter           = 0;
        ArrayList<User>        elList            = new ArrayList<User>();

        //
        // If Exlusion list users are not allowed
        // Load Exclusion list for the field including global list.     //
        //
        //
        // Commenting this code of accpeting exclusion list users for now
        //
        // if (bExclusion == false)
        // {
        // try
        // {
        // elList =
        // ExclusionList.lookupBySystemIdAndUserTypeIdAndGlobalList
        // (aField.getSystemId(), aUserTypeId);
        // }
        // catch (DatabaseException de)
        // {
        // myException.addException
        // (new TBitsException(de.toString()), SEVERE);
        // }
        // }
        boolean hasPrimary = false;

        while (st.hasMoreTokens() == true) {
            String  aToken    = st.nextToken().trim();
            boolean isPrimary = false;

            // check if this is the primary one.
            if (aToken.startsWith(PRIMARY_SIGN) == true) {
                if (aToken.length() > 1) {
                    aToken = aToken.substring(1, aToken.length()).trim();
                }

                // Only the first occurence of asterisk will be considered.
                // Subsequent occurences are ignored.
                if (hasPrimary == false) {
                    isPrimary  = true;
                    hasPrimary = true;
                } else {

                    // Should this be an exception/warning in the header
                    // description?
                }
            }

            // Check if it is auto
            if (aToken.equalsIgnoreCase("auto") == true) {

                // Check if auto is allowed in this user field.
                if (bAuto == true) {

                    //
                    // Find the volunteer and get the corresponding user login
                    // to proceed. Skipping this for now!!!
                    //
                    if (mySysConfig.getVolunteer() == NO_VOLUNTEER) {
                        LOG.warn("This BA is not configured for volunteer" + "generation: " + myBusinessArea.getDisplayName());

                        continue;
                    }

                    //
                    // Find the volunteer and get the corresponding user login
                    // to proceed.
                    //
                    try {
                        aToken = APIUtil.getVolunteer(mySystemId, myCategory.getTypeId(), mySysConfig.getVolunteer());
                    } catch (DatabaseException de) {
                        myException.addException(new TBitsException(de.toString()), SEVERE);

                        continue;
                    }

                    if ((aToken == null) || (aToken.trim().equals("") == true)) {
                        myException.addException(new TBitsException(Messages.getMessage("NOT_AUTO_ASSIGNED")), SEVERE);

                        continue;
                    } else {
                        int index = aToken.indexOf(',');

                        if (index > 0) {
                            nextVolunteer = aToken.split(",")[1];
                            curVolunteer  = aToken.split(",")[0];
                            aToken        = curVolunteer;
                        } else {
                            curVolunteer = aToken;
                        }

                        if (contains(aList, aToken) == true) {

                            // volunteer is already in the list.
                            continue;
                        }
                    }
                }
            }

            // Get the corresponding user object for this login.
            User user = null;

            try {

                //
                // Try to get the user object even though it is inactive.
                // TODO:
                // Any new inactive user introduced in this action will be
                // rejected in a later check.
                //
                user = TBitsHelper.getAPIUser(aToken);
                // If new external user, insert new user
                if ( null != user && (user.getUserId() == -1) && ((user.getUserTypeId() == UserType.EXTERNAL_USER) || (user.getUserTypeId() == UserType.INTERNAL_HIDDEN_LIST))) 
                {

                    // This might be some external user login. Check
                    // if this field allows inserting external
                    // user logins.
                    if (bInsert == true) 
                    {
                        // Insert this as an external user and get the
                        // corresponding user object.                     
                        try {

                            //
                            // Insert this as an external user and get
                            // the corresponding user object.
                            //
                            user = User.insertExternalUser(user.getUserLogin(), user.getUserTypeId(), myUser, true);
                        } catch (TBitsException de) {
                            LOG.warn("",(de));
                            myException.addException(new TBitsException(de.toString()), SEVERE);

                            continue;
                        } catch (DatabaseException de) {
                            LOG.warn("",(de));
                            myException.addException(new TBitsException(de.toString()), SEVERE);

                            continue;
                        }
                    } else {

                        //
                        // Add to Defunct users list
                        //
                        defunctUsers = defunctUsers + aToken + ", ";

                        continue;
                    }
                }
                else if( null == user)
                {
                	// Check if user is present in old list               
                	user = User.lookupAllByUserLogin(aToken) ;
                
	                if( null == user )
	                	user = User.lookupAllByEmail(aToken);
	                
	                if( null == user )
	                {
	                	myException.addException(new TBitsException("No user found with login : " + aToken), SEVERE ); 
	                	continue ;
	                }
	                else if (user.getIsActive() == false && oldUsers.contains(user) == false) 
	                {
	                	// Add to Defunct users list                 
	                    defunctUsers = defunctUsers + aToken + ", ";
	                    continue;
	                }
                }
 
            } catch (DatabaseException de) {
                LOG.debug("",(de));
                myException.addException(new TBitsException(de.toString()), SEVERE);

                continue;
            }

            //
            // if user part of Exclusion List, add to Defunct users list
            //
            if (elList.contains(user) == true) {
                if (myAppendInterface == SOURCE_EMAIL) {
                    emailDefunctUsers = emailDefunctUsers + aToken + ", ";
                } else {
                    defunctUsers = defunctUsers + aToken + ", ";
                }

                continue;
            }

            try {

                // Check if this user object corresponds to a BA Email Address.
                if (APIUtil.isBAEmail(user.getEmail()) == true) {

                    //
                    // If the same BA is  added, Ignore and don't report
                    //
                    if ((myBusinessArea.getEmail().equals(user.getEmail()) == true) || (myBusinessArea.getEmailList().contains(user.getEmail()) == true)) {
                        continue;
                    }

                    // Check if this field allows BA Emails to be a part
                    // of the list.
                    if (bSysEmail == true) {

                        //
                        // If send through Email, Don't add to the requets
                        // users list and report through header description.
                        //
                        if (myAppendInterface == SOURCE_EMAIL) {
                            baMails = baMails + user.getEmail() + ", ";

                            continue;
                        }
                    } else {

                        //
                        // Add to Defunct users list
                        //
                        if (myAppendInterface == SOURCE_EMAIL) {
                            emailDefunctUsers = emailDefunctUsers + aToken + ", ";
                        } else {
                            defunctUsers = defunctUsers + aToken + ", ";
                        }

                        continue;
                    }
                }
            } catch (DatabaseException de) {
                myException.addException(new TBitsException(de.toString()), WARNING);
            }

            counter++;

            //public RequestUser(int aSystemId, int aRequestId, int aUserId, int aOrdering, boolean aIsPrimary,int aFieldId) {
            RequestUser ru = new RequestUser(mySystemId,myRequestId,user.getUserId(),counter,isPrimary,aField.getFieldId());

//            ru.setSystemId(mySystemId);
//            ru.setRequestId(myRequestId);
//            ru.setUserTypeId(aUserTypeId);
//            ru.setMyFieldId(aField.getFieldId());
//            ru.setUserId(user.getUserId());
//            ru.setOrdering(counter);
//            ru.setIsPrimary(isPrimary);

            //
            // Add to the list only if it is not added.
            // Equals method of Request_users check for isPrimary also
            // and its needed to find diff for creating header-desc.
            // So here check for Primary or non -primary both.
            // Hence if assignee:Vaibhav,*Vaibhavis specified,
            // *Vaibhavwill be taken i,e importance given to *
            //
            if (list.contains(ru) == false) {
                ru.setIsPrimary(!ru.getIsPrimary());

                if (list.contains(ru) == true) {
                    list.remove(ru);

                    // set primary since this is case of duplicay.
                    ru.setIsPrimary(true);
                    list.add(ru);
                } else {

                    // set primary if user has specified, since this is not
                    // case of duplicay
                    ru.setIsPrimary(isPrimary);
                    list.add(ru);
                }
            } else {
                counter--;
            }

            ru = null;
        }

        // Check if there are any exceptions in the list above the acceptable
        // level.
        if (myException.getExceptionCount(myLevel) > 0) {
            throw myException;
        }

        //
        // Add Defunct Users for this field to the hashTable.
        //
        if (defunctUsers.equals("") == false) {
            myDefunctUsers.put(aField.getName(), defunctUsers.substring(0, defunctUsers.length() - 2));
        }

        if (emailDefunctUsers.equals("") == false) {
            myDefunctUsers.put("EMAIL_" + aField.getName(), emailDefunctUsers.substring(0, emailDefunctUsers.length() - 2));
        }

        //
        // Add BaMails Addresses send through email
        // for this field to the hashTable.
        //
        if (baMails.equals("") == false) {
            myBaMailIds.put(aField.getName(), baMails.substring(0, baMails.length() - 2));
        }

        return list;
    }

    /**
     * This method converts the comma-separated list of user logins to an
     * ArrayList of RequestUser objects for each user field.
     */
    private void processUserLists(Hashtable<String,String> params) throws APIException {
        ArrayList<RequestUser> diff = null;

        //
        // We have few business rules associated with these fields. Some of
        // them apply to all, and some are specific to a particular type.
        // Listing all the rules here...
        //
        // Rule(s) that apply to all:
        // - '*' can be present next to atmost one user login indicating
        // it as the primary one.
        //
        // Rules that apply to Logger and Assignee fields:
        // - All loggers and assignees should be valid users in our user
        // Database. No external user insertions can happen in this case.
        //
        // Rules that apply to Assignee field:
        // - "auto" can be part of assignee list which should be replaced
        // by the selected volunteer.
        //
        // Rules that apply to Subscriber/Cc/To fields:
        // - All email addresses need not be present in our user database.
        // New email addresses will be inserted as external users.
        //
        // Rules that apply to Subscriber field:
        // - Email addresses of business areas cannot be subscribers.
        //
        // Rules that apply to Cc Field:
        // - Email addresses of business areas can be Cc'ed, But a specific
        // mention about this is needed in the Header Description..
        //
        if (loggerList != null) {
            myLoggers = processUserField(loggerList, myFieldTable.get(Field.LOGGER), UserType.LOGGER, INSERT, NO_AUTO, NO_BA_EMAIL, EXCLUSION_LIST, myOldRequest.getLoggers());
        }

        if (assigneeList != null) {
            myAssignees = processUserField(assigneeList, myFieldTable.get(Field.ASSIGNEE), UserType.ASSIGNEE, INSERT, AUTO, NO_BA_EMAIL, EXCLUSION_LIST, myOldRequest.getAssignees());

            //
            // If the assignToAll property of the BA is not set, then check
            // if the assingees are valid for the type attributes of this
            // request i.e category/status/severity/requesttype.
            //
            if (mySysConfig.getAssignToAll() == false) {
                checkAssigneeList();
            }
        }

        if (subscriberList != null) {
            mySubscribers = processUserField(subscriberList, myFieldTable.get(Field.SUBSCRIBER), UserType.SUBSCRIBER, INSERT, NO_AUTO, NO_BA_EMAIL, NO_EXCLUSION_LIST, myOldRequest.getSubscribers());
        }

        if (toList != null) {
            myTos = processUserField(toList, myFieldTable.get(Field.TO), UserType.TO, INSERT, NO_AUTO, BA_EMAIL, NO_EXCLUSION_LIST, null);
        }

        if (ccList != null) {
            myCcs = processUserField(ccList, myFieldTable.get(Field.CC), UserType.CC, INSERT, NO_AUTO, BA_EMAIL, NO_EXCLUSION_LIST, null);
        }

        try {
			processExUserTypeFields(params);
		} catch (TBitsException e) {			
			LOG.info("",(e));
			myException.addException(e);
		}
        // Check if there are any exceptions in the list above the acceptable
        // level.
        if (myException.getExceptionCount(myLevel) > 0) {
            throw myException;
        }

        return;
    }

    private void processExUserTypeFields(Hashtable<String,String>params) throws TBitsException, APIException 
    {
    	
    	ArrayList<Field> utFields;
		try {
			utFields = Field.lookupBySystemId(this.myBusinessArea.getSystemId(), DataType.USERTYPE);
		} catch (DatabaseException e) 
		{	
			LOG.info("",(e));
			throw new TBitsException("Exception occured while accesing the UserType Fields.");
		}
    	
    	for( Field field : utFields )
    	{
    		// ignore the default usertypes.
    		if( field.getIsExtended() == false )
    			continue ;
    		
    		String value = params.get(field.getName()); 
    		boolean isFieldSpecified = false ;
    		if( params.contains(field.getName()))
    				isFieldSpecified = true ;
    		// private RequestEx handleUserTypeExtendedField(Field field, String value, boolean isFieldSpecified) {
			ArrayList<RequestUser> reqUsers = handleUserTypeExtendedField(field,value); // processUserField(value,field, UserType.USERTYPE, INSERT, NO_AUTO, NO_BA_EMAIL, NO_EXCLUSION_LIST, NO_INACTIVE);
			if( null != reqUsers )
				myRequest.setExUserType(field, reqUsers);
    	}
    }

    /**
     * This method reads the values of extended fields if any from the given
     * table.
     *
     * @param aParamTable Table that contain the key value pairs.
     *
     * @exception APIException incase of any severe errors.
     */
    private void readExtendedFieldValues(Hashtable<String, String> aParamTable) throws APIException {

        //
        // Check if the list of extended fields for this Business Area is
        // empty. If so, return.
        //
        if ((myExtFieldList == null) || (myExtFieldList.size() == 0)) {

            // We need not parse for any fields. So, return back.
            return;
        }

        myExtDiffTable = new Hashtable<String, DiffEntry>();

        // Initialize the extended fields map.
//        myOldExtendedFields = myOldRequest.getExtendedFields();
        ArrayList<Field> exFields;
		try {
			exFields = Field.lookupBySystemId(mySystemId, true);
		} catch (DatabaseException e) {
			myException.addException(new TBitsException(e));
			throw myException ;
		}
        for( Field field : exFields )
        {
        	if( field.getIsActive() == false || field.getDataTypeId() == DataType.USERTYPE )
        		continue ;
        	
        	RequestEx rex = myOldRequest.getRequestExObject(field);
        	if(rex != null )
        	myExtendedFields.put(field, rex);
        }

        // Key, value of the param table.
        String key   = "";
        String value = "";

        // For each extended field, check if there is a value specified in the
        // table.
        for (Field field : myExtFieldList) {

            // Skip inactive fields.
            if (field.getIsActive() == false) {
                continue;
            }

            if( field.getDataTypeId() == DataType.USERTYPE )
            { 
            	// skip the user-type fields.
            	continue ;
            }
            key   = field.getName();
            value = aParamTable.get(key);
            boolean isFieldSpecified = false;
            if(aParamTable.containsKey(key))
            {
            	isFieldSpecified = true;
            }

            RequestEx reqEx = handleExtendedField(field, value, isFieldSpecified);
            
            if(reqEx != null && (field.getDataTypeId() == DataType.TEXT)){
            	key = "text_content_type";
            	value = aParamTable.get(key);
            	
            	int contentType = 0;
            	if(value != null && value.equals("html"))
            		contentType = 1;
            	
            	reqEx.setTextContentType(contentType);
            }
            
            if(reqEx != null)
            	myExtendedFields.put(field, reqEx);
        }

        // Check if there are any exceptions in the list above the acceptable
        // level.
        if (myException.getExceptionCount(myLevel) > 0) {
            throw myException;
        }

        return;
    }

    /**
     * This method furnishes the attributes of this class from the param table.
     *
     * @param aParamTable Table that contain the key value pairs.
     *
     * @exception APIException incase of any severe errors.
     */
    private void readFixedFieldValues(Connection conn, Hashtable<String, String> aParamTable) throws APIException {

        // Key, value of the param table.
        String key   = "";
        String value = "";

        //
        // We will look in the table for the required parameters in an order.
        // For types, we take the old values if not specified.
        // Summary and Memo if specified are taken. Otherwise, they are empty.
        //
        // 1. System Id
        // Key      : Field.BUSINESS_AREA.
        // Required : Yes.
        key   = Field.BUSINESS_AREA;
        value = aParamTable.get(key);

        if ((value == null) || (value.trim().equals("") == true)) {
            myException.addException(new TBitsException("System Id is required."), FATAL);

            // We cannot continue further. Throw it here.
            throw myException;
        }

        //
        // So we got a value. Parse this. It should be an integer in which case
        // we treat it as System Id. It can be a string in which case we treat
        // it as System prefix.
        //
        try {
        	//TODO:WHhat if a ba's prefix is numeric????
            mySystemId = Integer.parseInt(value);
        } catch (NumberFormatException nfe) {

            // Check if the prefix of the BA is passed.
            try {
                myBusinessArea = BusinessArea.lookupBySystemPrefix(value);
            } catch (DatabaseException de) {
                LOG.warn("",(de));
                myException.addException(new TBitsException(de.toString()), FATAL);

                // We cannot continue further. Throw it here.
                throw myException;
            }

            if (myBusinessArea == null) {
                myException.addException(new TBitsException(Messages.getMessage("INVALID_BUSINESS_AREA", value)), FATAL);

                // We cannot continue further. Throw it here.
                throw myException;
            }
        }

        if (myBusinessArea == null) {

            // Now check if this System Id corresponds to a valid BusinessArea.
            try {
                myBusinessArea = BusinessArea.lookupBySystemId(mySystemId);
            } catch (DatabaseException de) {
                LOG.warn("",(de));
                myException.addException(new TBitsException(de.toString()), FATAL);
            }

            if (myBusinessArea == null) {
                myException.addException(new TBitsException(Messages.getMessage("INVALID_BUSINESS_AREA", value)), FATAL);;

                // We can no more continue. So throw the exception here itself.
                throw myException;
            }
        } else {
            mySystemId = myBusinessArea.getSystemId();
        }

        // 2. Request Id
        // Key      : Field.REQUEST
        // Required : Yes
        key   = Field.REQUEST;
        value = aParamTable.get(key);

        if ((value == null) || (value.trim().equals("") == true)) {
            myException.addException(new TBitsException("Request Id is required."), FATAL);

            // We cannot continue further. Throw it here.
            throw myException;
        }

        // So we got a value. Parse this. It should be a int  and if not throw
        // an exception.
        try {
            myRequestId = Integer.parseInt(value);
        } catch (NumberFormatException nfe) {
            myException.addException(new TBitsException("Request Id should be numeric."), FATAL);

            // We cannot continue further. Throw it here.
            throw myException;
        }

        // Now check if this Request Id corresponds to a valid Request.
        try {
            myOldRequest = Request.lookupBySystemIdAndRequestId(conn, mySystemId, myRequestId);
        } catch (DatabaseException de) {
            LOG.warn("",(de));
            myException.addException(new TBitsException(de.toString()), FATAL);
        }

        if (myOldRequest == null) {
            myException.addException(new TBitsException(Messages.getMessage("INVALID_REQUEST_ID", myRequestId)), FATAL);

            // We can no more continue. So throw the exception here itself.
            throw myException;
        }

        // 16. User Id.
        // Key      : Field.USER
        // Value    : User Login property of the user.
        // Required : Yes.
        key   = Field.USER;
        value = aParamTable.get(key);

        if ((value == null) || (value.trim().equals("") == true)) {
            myException.addException(new TBitsException("User of the action is required."), FATAL);

            throw myException;
        } else {
            try {
                myUser = TBitsHelper.getAPIUser(value);

                if (myUser != null) {
                    if (APIUtil.isBAEmail(myUser.getEmail()) == true) {
                        myException.addException(new TBitsException("BA Mail Ids not accepted as appender."), FATAL);

                        // We cannot continue further. Throw it here.
                        throw myException;
                    }

                    //
                    // If new external user, insert new user
                    //
                    if ((myUser.getUserId() == -1) && ((myUser.getUserTypeId() == UserType.EXTERNAL_USER) || (myUser.getUserTypeId() == UserType.INTERNAL_HIDDEN_LIST))) {

                        //
                        // Insert this as an external user and get the
                        // corresponding user object.
                        //
                        myUser = User.insertExternalUser(myUser.getUserLogin(), myUser.getUserTypeId(), myUser, true);
                    }
                } else {
                    myException.addException(new TBitsException(Messages.getMessage("INVALID_USER", value)), FATAL);

                    throw myException;
                }
            } catch (Exception e) {
                myException.addException(new TBitsException(e.toString()), FATAL);

                throw myException;
            }
        }

        // Initialize the environment for further processing now.
        init(conn);

        Field field = null;

        // 3. Category Id
        // Key      : Field.CATEGORY.
        // Required : No.
        // Default  : Current category of the request.
        // Comment  : In case of exception due to invalid value, we continue
        // with the old value after pushing this situation as an
        // exception at SEVERE level into our list.
        key   = Field.CATEGORY;
        field = myFieldTable.get(key);
        value = aParamTable.get(key);

        if ((value == null) || (value.trim().equals("") == true)) {
            myCategory = myOldRequest.getCategoryId();
        } else {
            try {
                myCategory = Type.lookupBySystemIdAndFieldNameAndTypeName(mySystemId, key, value);

                if (myCategory == null) {
                    myException.addException(new TBitsException(Messages.getMessage("INVALID_TYPE", field.getDisplayName(), value)), SEVERE);

                    // Consider the old value.
                    myCategory = myOldRequest.getCategoryId();
                }
            } catch (DatabaseException de) {
                LOG.warn("",(de));
                myException.addException(new TBitsException(de.toString()), SEVERE);
                myCategory = myOldRequest.getCategoryId();
            }
        }

        // 4. Status Id
        // Key      : Field.STATUS.
        // Required : No.
        // Default  : Current status of the request.
        // Comment  : In case of exception due to invalid value, we continue
        // with the old value after pushing this situation as an
        // exception at SEVERE level into our list.
        key   = Field.STATUS;
        field = myFieldTable.get(key);
        value = aParamTable.get(key);

        if ((value == null) || (value.trim().equals("") == true)) {
            myStatus = myOldRequest.getStatusId();
        } else {
            try {
                myStatus = Type.lookupBySystemIdAndFieldNameAndTypeName(mySystemId, key, value);

                if (myStatus == null) {
                    myException.addException(new TBitsException(Messages.getMessage("INVALID_TYPE", field.getDisplayName(), value)), SEVERE);

                    // Consider the old value.
                    myStatus = myOldRequest.getStatusId();
                }
            } catch (DatabaseException de) {
                LOG.warn("",(de));
                myException.addException(new TBitsException(de.toString()), SEVERE);
                myStatus = myOldRequest.getStatusId();
            }
        }

        // 5. Severity Id
        // Key      : Field.SEVERITY.
        // Required : No.
        // Default  : Current severity of the request.
        // Comment  : In case of exception due to invalid value, we continue
        // with the old value after pushing this situation as an
        // exception at SEVERE level into our list.
        key   = Field.SEVERITY;
        field = myFieldTable.get(key);
        value = aParamTable.get(key);

        if ((value == null) || (value.trim().equals("") == true)) {
            mySeverity = myOldRequest.getSeverityId();
        } else {
            try {
                mySeverity = Type.lookupBySystemIdAndFieldNameAndTypeName(mySystemId, key, value);

                if (mySeverity == null) {
                    myException.addException(new TBitsException(Messages.getMessage("INVALID_TYPE", field.getDisplayName(), value)), SEVERE);
                    mySeverity = myOldRequest.getSeverityId();
                }
            } catch (DatabaseException de) {
                LOG.warn("",(de));
                myException.addException(new TBitsException(de.toString()), SEVERE);
                mySeverity = myOldRequest.getSeverityId();
            }
        }

        // 6. RequestType Id
        // Key      : Field.REQUEST_TYPE.
        // Required : No.
        // Default  : BA's Default RequestType.
        // Comment  : In case of exception due to invalid value, we continue
        // with the old value after pushing this situation as an
        // exception at SEVERE level into our list.
        key   = Field.REQUEST_TYPE;
        field = myFieldTable.get(key);
        value = aParamTable.get(key);

        if ((value == null) || (value.trim().equals("") == true)) {
            myRequestType = myOldRequest.getRequestTypeId();
        } else {
            try {
                myRequestType = Type.lookupBySystemIdAndFieldNameAndTypeName(mySystemId, key, value);

                if (myRequestType == null) {
                    myException.addException(new TBitsException(Messages.getMessage("INVALID_TYPE", field.getDisplayName(), value)), SEVERE);
                    myRequestType = myOldRequest.getRequestTypeId();
                }
            } catch (DatabaseException de) {
                LOG.warn("",(de));
                myException.addException(new TBitsException(de.toString()), SEVERE);
                myRequestType = myOldRequest.getRequestTypeId();
            }
        }

        // 7. Logger
        // Key       : Field.LOGGER
        // Required  : No.
        // Default   : User.
        key   = Field.LOGGER;
        value = aParamTable.get(key);

        if (value != null) {
            if (value.trim().equals("") == false) {
                loggerList = value;
            } else {
                myException.addException(new TBitsException(Messages.getMessage("LOGGER_MANDATORY")), SEVERE);
                loggerList = null;
                myLoggers  = (myOldRequest.getLoggers() == null ? null : new ArrayList<RequestUser>(myOldRequest.getLoggers()));
            }
        } else {
            loggerList = null;
        	//TODO:checkcarryover
            myLoggers  = (myOldRequest.getLoggers() == null ? null : new ArrayList<RequestUser>(myOldRequest.getLoggers()));
        }

        // 8. Assignee
        // Key       : Field.ASSIGNEE
        // Required  : No.
        // Default   : Empty List.
        key   = Field.ASSIGNEE;
        value = aParamTable.get(key);
		try {
			Field keyField = Field.lookupBySystemIdAndFieldName(myBusinessArea
					.getSystemId(), key);
			if (value != null) {
				assigneeList = value;
			} else if ((keyField.getPermission() & Permission.SET) == 0) {
				assigneeList = "";
				myAssignees = new ArrayList<RequestUser>();
			} else {
				assigneeList = null;
	        	//TODO:checkcarryover
	            myAssignees  = (myOldRequest.getAssignees() == null ? null : new ArrayList<RequestUser>(myOldRequest.getAssignees()));
			}
		} catch (DatabaseException e1) {
			// TODO Auto-generated catch block
			myException.addException(new TBitsException(
					"Unable to find the fixed field: " + key), SEVERE);
		}
        
        // 9. Subscribers
        // Key       : Field.SUBSCRIBER
        // Required  : No.
        // Default   : Empty List.
		key = Field.SUBSCRIBER;
		value = aParamTable.get(key);
		try {
			Field keyField = Field.lookupBySystemIdAndFieldName(myBusinessArea
					.getSystemId(), key);
			if (value != null) {
				subscriberList = value;
			} else if ((keyField.getPermission() & Permission.SET) == 0) {
				subscriberList = "";
				mySubscribers = new ArrayList<RequestUser>();
			} else {
				subscriberList = null;
				// TODO:checkcarryover
				mySubscribers = (myOldRequest.getSubscribers() == null ? null
						: new ArrayList<RequestUser>(myOldRequest
								.getSubscribers()));
			}
		} catch (DatabaseException e1) {
			// TODO Auto-generated catch block
			myException.addException(new TBitsException(
					"Unable to find the fixed field: " + key), SEVERE);
		}

       
        // 10 Tos
        // Key       : Field.TO
        // Required  : No.
        // Default   : Empty List.
        key   = Field.TO;
        value = aParamTable.get(key);

        if (value != null) {
            toList = value;
        } else {
            toList = null;
            myTos  = new ArrayList<RequestUser>();
        }

        // 11. Ccs
        // Key       : Field.CC
        // Required  : No.
        // Default   : Empty List.

        if (value != null) {
            
        } else {
            ccList = null;
            myCcs  = new ArrayList<RequestUser>();
        }
        
        key   = Field.CC;
        value = aParamTable.get(key);
		try {
			Field keyField = Field.lookupBySystemIdAndFieldName(myBusinessArea
					.getSystemId(), key);
			if (value != null) {
				ccList = value;
			} else if ((keyField.getPermission() & Permission.SET) == 0) {
				ccList = "";
				myCcs = new ArrayList<RequestUser>();
			} else {
				ccList = null;
				// TODO:checkcarryover
				myCcs = (myOldRequest.getCcs() == null ? null
						: new ArrayList<RequestUser>(myOldRequest
								.getCcs()));
			}
		} catch (DatabaseException e1) {
			// TODO Auto-generated catch block
			myException.addException(new TBitsException(
					"Unable to find the fixed field: " + key), SEVERE);
		}

        // 12. Subject
        // Key      : Field.SUBJECT
        // Required : Yes if this is not from email.
        // Default  : Empty String.
        // Comment  : Null Value   -> Continue with old value.
        // Empty Value  -> New Value is empty String.
        key   = Field.SUBJECT;
        value = aParamTable.get(key);

        if (value == null) {

            // Continue with old value.
            mySubject = myOldRequest.getSubject();;
        } else if (value.trim().equals("") == true) {
            mySubject = "";
        } else {
        	//TODO:checkcarryover
            mySubject = value.trim();
        }

        // 13. Description
        // Key      : Field.DESCRIPTION
        // Required : No
        // Default  : Empty String.
        key   = Field.DESCRIPTION;
        value = aParamTable.get(key);

        if ((value == null) || (value.trim().equals("") == true)) {
            myDescription = "";
        } else {
        	//TODO:checkcarryover
            myDescription = value.trim();
        }
        
        // Key      : Field.DESCRIPTION + "_content_type"
        // Required : No
        // Default  : Empty String.
        key   = Field.DESCRIPTION + "_content_type";
        value = aParamTable.get(key);

        if ((value == null)) {
            myDescriptionContentType = CONTENT_TYPE_HTML;
        } else {
        	//TODO:checkcarryover
            myDescriptionContentType = Integer.parseInt(value.trim());
        }

        // 14. Confidential.
        // Key      : Field.IS_PRIVATE
        // Required : No.
        // Default  : false.
        // Interpretation: "false"/"FALSE"/"0" -> false Otherwise, true.
        key   = Field.IS_PRIVATE;
        value = aParamTable.get(key);

        if ((value == null) || (value.trim().equals("") == true)) {

            // Take the old value.
        	//TODO:checkcarryover
            myIsPrivate = myOldRequest.getIsPrivate();
        } else if ((value.trim().equalsIgnoreCase("false") == true) || (value.trim().equalsIgnoreCase("no") == true) || (value.trim().equalsIgnoreCase("public") == true)
                   || (value.trim().equalsIgnoreCase("0") == true)) {
            myIsPrivate = false;
        } else if ((value.trim().equalsIgnoreCase("true") == true) || (value.trim().equalsIgnoreCase("yes") == true) || (value.trim().equalsIgnoreCase("private") == true)
                   || (value.trim().equalsIgnoreCase("1") == true)) {
            myIsPrivate = true;
        } else {
            myIsPrivate = false;
        }

        // 15. Parent Request Id.
        // Key      : Field.PARENT_REQUEST_ID
        // Required : No.
        // Default  : 0.
        key   = Field.PARENT_REQUEST_ID;
        value = aParamTable.get(key);

        if ((value == null) || (value.trim().equals("") == true)) {

            // Take the old value.
            myParentId = myOldRequest.getParentRequestId();
        } else if (value.trim().equals("0") == true) {
            myParentId = 0;
        } else {
            try {
                myParentId = Integer.parseInt(value);

                if (myParentId != myOldRequest.getParentRequestId()) {

                    // Check if the parent corresponds to a valid request
                    // in this BA.
                    Request tmp = null;
					tmp = Request.lookupBySystemIdAndRequestId(conn, mySystemId, myParentId);

                    if (tmp == null) {
                        myParentId = 0;
                        myException.addException(new TBitsException("Please enter a valid request id to associate: " + myParentId), SEVERE);
                    } else {
                        boolean isValid = isParentValid(conn);

                        if (isValid == false) {
                            StringBuilder message = new StringBuilder();

                            message.append("This association cannot be allowed ").append("as it would result in a loop: ").append(value);
                            myException.addException(new TBitsException(message.toString()), SEVERE);
                        }

                        boolean isTransferred = APIUtil.isTransferred(myBusinessArea.getSystemPrefix(), myParentId);

                        if (isTransferred == true) {
                            StringBuilder message = new StringBuilder();

                            message.append("Transferred request cannot be part ").append("of an association: ").append(value);
                            myException.addException(new TBitsException(message.toString()), SEVERE);
                        }
                    }
                }
            } catch (DatabaseException de) {
                myParentId = 0;
                LOG.warn("",(de));
                myException.addException(new TBitsException(de.toString()), WARNING);
            } catch (NumberFormatException nfe) {
                myParentId = 0;
                myException.addException(new TBitsException("Parent Id must be numeric: " + value), SEVERE);

                // Take the old value.
                myParentId = myOldRequest.getParentRequestId();
            }
        }

        // 17. Max Action Id
        // Key      : Field.MAX_ACTION_ID
        // Required : No
        // Default  : Previous Max Action Id + 1
        key   = Field.MAX_ACTION_ID;
        value = aParamTable.get(key);

        if ((value != null) && (value.trim().equals("") == false)) {
            try {
                myMaxActionId = Integer.parseInt(value);

                //
                // If the maxactionid obtained from the end user is not equal
                // to the maxactionid of the oldrequest, then this is an append
                // to the stale version of the request and so this qualifies
                // for diff generation and checking.
                //
                if (myMaxActionId != myOldRequest.getMaxActionId()) {
                    appendStale = true;
                }
            } catch (Exception e) {
                myMaxActionId = myOldRequest.getMaxActionId() + 1;
            }
        } else {
            myMaxActionId = myOldRequest.getMaxActionId() + 1;
        }

        // 18. Due Date.
        // Key      : Field.DUE_DATE.
        // Required : No.
        // Default  : CurrentTime + Default Due Date For This BA.
        key   = Field.DUE_DATE;
        value = aParamTable.get(key);

        if (value == null) {

            // Get the Default Due Date for this BA.
            myDueDate = (myOldRequest.getDueDate() == null ? null : new Timestamp(myOldRequest.getDueDate().getTime()));
        } else if (value.trim().equals("") == true) {

            // user wanted us to clear the value in the field.
            // Check if BA allows null due dates.
            if (mySysConfig.getAllowNullDueDate() == true) {
                myDueDate = null;
            } else {
                myDueDate = (myOldRequest.getDueDate() == null ? null : new Timestamp(myOldRequest.getDueDate().getTime()));
            }
        } else {
            try {
                //myDueDate = APIUtil.parseDateTime(value);
            	SimpleDateFormat sdf = new SimpleDateFormat(API_DATE_FORMAT);
            	try {
					myDueDate = Timestamp.getTimestamp(sdf.parse(value));
				} catch (ParseException e) {
					throw new TBitsException("Due date should be specified in " + API_DATE_FORMAT + " format. " + e.toString());
				}
            } catch (TBitsException de) {
                myException.addException(de, SEVERE);
                myDueDate = (myOldRequest.getDueDate()==null ? null : new Timestamp(myOldRequest.getDueDate().getTime()));
                LOG.warn(de.toString());
            }
        }

        if ((myDueDate == null) && (mySysConfig.getAllowNullDueDate() == false)) {
            myDueDate = defDueDate;
        }

        // 19. Last Updated Date.
        // Key      : Field.LASTUPDATED_DATE
        // Required : No.
        // Default  : Current Time.
//        myUpdatedDate = Timestamp.getGMTNow();
		key = Field.LASTUPDATED_DATE;
		value = aParamTable.get(key);

		if ((value == null) || (value.trim().equals("") == true)) {
			myUpdatedDate = Timestamp.getGMTNow();
		} else {
			try {
				// myDueDate = APIUtil.parseDateTime(value);
				SimpleDateFormat sdf = new SimpleDateFormat(API_DATE_FORMAT);
				try {
					myUpdatedDate = Timestamp.getTimestamp(sdf.parse(value));
				} catch (ParseException e) {
					throw new TBitsException(
							"UpdatedDate should be specified in "
									+ API_DATE_FORMAT + " format. "
									+ e.toString());
				}
			} catch (TBitsException de) {
				myException.addException(de, SEVERE);
				myUpdatedDate = Timestamp.getGMTNow();
				LOG.warn(de.toString());
			}
		}

        if (myUpdatedDate == null)
        {
        	myUpdatedDate = Timestamp.getGMTNow();;
        }

        // 20. Logged Date.
        // Key      : Field.LOGGED_DATE
        // Required : No.
        key   = Field.LOGGED_DATE;
        value = aParamTable.get(key);

        if (value == null) {

            // Get the Default Due Date for this BA.
        	myLoggedDate = (myOldRequest.getLoggedDate() == null ? null : new Timestamp(myOldRequest.getLoggedDate().getTime()));
        } else if (value.trim().equals("") == true) {
        	myLoggedDate = Timestamp.getGMTNow();
        } else {
            try {
                //myDueDate = APIUtil.parseDateTime(value);
            	SimpleDateFormat sdf = new SimpleDateFormat(API_DATE_FORMAT);
            	try {
            		myLoggedDate = Timestamp.getTimestamp(sdf.parse(value));
				} catch (ParseException e) {
					throw new TBitsException("Logged Date should be specified in " + API_DATE_FORMAT + " format. " + e.toString());
				}
            } catch (TBitsException de) {
                myException.addException(de, SEVERE);
                myLoggedDate = (myOldRequest.getLoggedDate() == null ? null : new Timestamp(myOldRequest.getLoggedDate().getTime()));
                LOG.warn(de.toString());
            }
        }

        if (myLoggedDate == null) {
        	myLoggedDate = Timestamp.getGMTNow();;
        }

        // 21. Header Description
        // Key      : Field.HEADER_DESCRIPTION
        // Required : No.
        // Default  : Empty String.
        // Comment  : Will be generated during request processing.
        myHeaderDescription = "";

        // 22. Attachments
        // Key      : Field.ATTACHMENTS
        // Required : No
        // Default  : Empty String.
        // Comment  : The value is assumed to be a list of file names located
        // in temp location.
        key   = Field.ATTACHMENTS;
        value = aParamTable.get(key);

        if ((value == null)) {
            myAttachments = myOldRequest.getAttachments();
        } else {
            myAttachments = AttachmentInfo.fromJson(value.trim());
        }

        // 23. Summary
        // Key      : Field.SUMMARY
        // Required : No
        // Comment  : Null Value  -> Continue with old value.
        // Empty Value -> New Value is an empty string.
        key   = Field.SUMMARY;
        value = aParamTable.get(key);

        if (value == null || value.trim().equals("") == true) 
        {
            mySummary = "";
        } else 
        {
           mySummary = value.trim();
        }
        
        // Key      : Field.SUMMARY + "_content_type"
        // Required : No
        // Default  : Empty String.
        key   = Field.SUMMARY + "_content_type";
        value = aParamTable.get(key);

        if ((value == null) || (value.trim().equals("") == true)) {
            mySummaryContentType = CONTENT_TYPE_HTML;
        } else {
            mySummaryContentType = Integer.parseInt(value.trim());
        }

        // 24. Memo
        // Key      : Field.MEMO.
        // Required : No
        // Comment  : Null Value  -> Continue with old value.
        // Empty Value -> New Value is an empty string.
        key   = Field.MEMO;
        value = aParamTable.get(key);

        if (value == null) {
            myMemo = myOldRequest.getMemo();
        } else if (value.trim().equals("") == true) {
            myMemo = "";
        } else {
            myMemo = value.trim();
        }

        // 25. Append Interface
        // Key      : Field.APPEND_INTERFACE.
        // Required : No
        // Default  : SOURCE_WEB - 101.
        myAppendInterface = ourSource;

        // 26. Notify.
        // Key      : Field.NOTIFY
        // Required : No.
        // Default  : false.
        // Interpretation: "false"/"FALSE"/"0" -> false Otherwise, true.
        key   = Field.NOTIFY;
        value = aParamTable.get(key);

        if ((value == null) || (value.trim().equals("") == true)) {
            myNotify = defNotify;
        } else {
            try {
                if (value.equalsIgnoreCase("true")) {
                    myNotify = 1;
                } else if (value.equalsIgnoreCase("false")) {
                    myNotify = 0;
                } else {
                    myNotify = Integer.parseInt(value);
                }
            } catch (NumberFormatException nfe) {
                myNotify = defNotify;
            }
        }

        // 27. Notify Loggers.
        // Key      : Field.NOTIFY_LOGGERS
        // Required : No.
        // Default  : false.
        // Interpretation: "false"/"FALSE"/"0" -> false Otherwise, true.
        key   = Field.NOTIFY_LOGGERS;
        value = aParamTable.get(key);

        if ((value == null) || (value.trim().equals("") == true)) {
            myNotifyLoggers = defNotifyLoggers;
        } else if ((value.trim().equalsIgnoreCase("false") == true) || (value.trim().equalsIgnoreCase("0") == true)) {
            myNotifyLoggers = false;
        } else {
            myNotifyLoggers = true;
        }

        // 28. Replied To Action
        // Key      : Field.REPLIED_TO_ACTION
        // Required : No.
        // Default  : 0;
        key   = Field.REPLIED_TO_ACTION;
        value = aParamTable.get(key);

        if ((value != null) && (value.trim().equals("") == false)) {
            try {
                myRepliedToAction       = Integer.parseInt(value);
                myRepliedToActionObject = Action.lookupBySystemIdAndRequestIdAndActionId(conn, mySystemId, myRequestId, myRepliedToAction);

                //
                // If the repliedTo obtained from the end user is not equal
                // to the maxactionid of the oldrequest, then this is an append
                // to the stale version of the request and so this qualifies
                // for diff generation and checking.
                //
                if (myRepliedToAction != myOldRequest.getMaxActionId()) {
                    appendStale = true;
                } else {
                    myRepliedToAction = 0;
                }
            } catch (Exception e) {
                myRepliedToAction = 0;
            }
        } else {
            myRepliedToAction = 0;
        }

        // 29. Related Requests
        // Key      : Field.RELATED_REQUESTS
        // Required : No.
        // Default  : null.
        key   = Field.RELATED_REQUESTS;
        value = aParamTable.get(key);

        /*
         * Get the number of linked requests already present.
         */
        Hashtable<String, String> tmpOldRelReqList = Request.getRelatedRequests(conn, myOldRequest.getSystemId(),myOldRequest.getRequestId());
        int                       tmpOldRelReqSize = (tmpOldRelReqList == null)
                ? 0
                : tmpOldRelReqList.size();

        /*
         * Cases:
         *  1. User did not specify any value => (value == null)
         *      i. If there are no linked requests earlier, set the new
         *         value to null.
         *     ii. If there are any linked requests earlier, carry them forward.
         *
         *  2. User specified some value => (value != null)
         *      i.  Take the new value even if it is empty.
         */

        // Case 1
        if (value == null) {

            // Case 1.i
            if (tmpOldRelReqSize == 0) {
                myRelatedRequests = null;
            }

            // Case 1.ii
            else {
                myRelatedRequests = myOldRequest.getRelatedRequests();
            }
        }

        // Case 2
        else {
            myRelatedRequests = value.trim();
        }

        // Check if there are any exceptions in the list above the acceptable
        // level.
        if (myException.getExceptionCount(myLevel) > 0) {
            throw myException;
        }

        // 30. Office Id
        // Key      : Field.OFFICE.
        // Required : No.
        // Default  : Current office of the request.
        // Comment  : In case of exception due to invalid value, we continue
        // with the old value after pushing this situation as an
        // exception at SEVERE level into our list.
        key   = Field.OFFICE;
        field = myFieldTable.get(key);
        value = aParamTable.get(key);

        if ((value == null) || (value.trim().equals("") == true)) {
            myOffice = myOldRequest.getOfficeId();
        } else {
            try {
                myOffice = Type.lookupBySystemIdAndFieldNameAndTypeName(mySystemId, key, value);

                if (myOffice == null) {
                    myException.addException(new TBitsException(Messages.getMessage("INVALID_TYPE", field.getDisplayName(), value)), SEVERE);
                    myOffice = myOldRequest.getOfficeId();
                }
            } catch (DatabaseException de) {
                LOG.warn("",(de));
                myException.addException(new TBitsException(de.toString()), SEVERE);
                myOffice = myOldRequest.getOfficeId();
            }
        }

        return;
    }

    /**
     * This method reverts to the old value for the given field.
     *
     * @param aFieldName  Name of the field.
     *
     */
    private void revertToOldValue(String aFieldName) {
        Field field = myFieldTable.get(aFieldName);

        if (field == null) {
            return;
        }

        String fieldName = field.getName();
        int    fieldType = field.getDataTypeId();

        switch (fieldType) {
        case BOOLEAN : {
            if (fieldName.equals(Field.NOTIFY_LOGGERS)) {
                myRequest.setNotifyLoggers(defNotifyLoggers);
            } else if (fieldName.equals(Field.IS_PRIVATE)) {
                myRequest.setIsPrivate(myOldRequest.getIsPrivate());
            }
        }

        break;

        case DATE :
        case TIME :
        case DATETIME :
            break;

        case INT : {
            if (fieldName.equals(Field.NOTIFY)) {
                myRequest.setNotify((defNotify != 0 ));
            }
        }

        break;

        case REAL :
        case STRING :
        case TEXT : {

            // No harm in retaining the date/numeric/string/text data.
        }
        break;

        case TYPE : {
            if (fieldName.equals(Field.CATEGORY)) {
                myRequest.setCategoryId(myOldRequest.getCategoryId());
            } else if (fieldName.equals(Field.STATUS)) {
                myRequest.setStatusId(myOldRequest.getStatusId());
            } else if (fieldName.equals(Field.SEVERITY)) {
                myRequest.setSeverityId(myOldRequest.getSeverityId());
            } else if (fieldName.equals(Field.REQUEST_TYPE)) {
                myRequest.setRequestTypeId(myOldRequest.getRequestTypeId());
            } else if (fieldName.equals(Field.OFFICE)) {
                myRequest.setOfficeId(myOldRequest.getOfficeId());
            } else if (field.getIsExtended() == true) {
                RequestEx reqEx    = myExtendedFields.get(field);
                RequestEx oldReqEx = myOldRequest.getRequestExObject(field);

                if (oldReqEx != null) {
                    reqEx = oldReqEx;
                    myExtendedFields.put(field, reqEx);
                } else {

                    // Get the default type for this Field.
                    try {
                        Type type = Type.getDefaultTypeBySystemIdAndFieldId(mySystemId, field.getFieldId());

                        if (type != null) {
                            reqEx.setTypeValue(type.getTypeId());
                            myExtendedFields.put(field, reqEx);
                        }
                    } catch (Exception e) {}
                }
            }
        }

        break;

        case USERTYPE : {
            if (fieldName.equals(Field.LOGGER)) {
                myRequest.setLoggers(myOldRequest.getLoggers());
            } else if (fieldName.equals(Field.ASSIGNEE)) {
                myRequest.setAssignees(myOldRequest.getAssignees());
            } else if (fieldName.equals(Field.SUBSCRIBER)) {
                myRequest.setSubscribers(myOldRequest.getSubscribers());
            } else if (fieldName.equals(Field.TO)) {
                myRequest.setTos(new ArrayList<RequestUser>());
            } else if (fieldName.equals(Field.CC)) {
                myRequest.setCcs(new ArrayList<RequestUser>());
            }
        }

        break;
        }
    }

    /**
     * This method executes the business rules if any present in this business
     * area.
     *
     */
    private void runWorkflowRules() throws APIException {
        ArrayList<BARule> rulesList = null;

        try {
            rulesList = BARule.lookupBySystemId(mySystemId);
        } catch (DatabaseException de) {
            myException.addException(new TBitsException(de.toString()), SEVERE);

            throw myException;
        }

        // If the rules list is still null or empty, return.
        if ((rulesList == null) || (rulesList.size() == 0)) {
            return;
        }

        boolean canContinue = true;
        // Iterate through each rule.
        for (BARule rule : rulesList) {
        	if(!canContinue)
        		break;
            // Get the underlying BusinessRule object.
            BusinessRule br = rule.getRule();

            if (br == null) {
                continue;
            }

            String ruleMessage = br.getMessage();

            LOG.info("Executing the rule: " + ruleMessage);

            // Get the conditions to be checked.
            ArrayList<RuleCondition> rcList         = br.getConditionList();
            boolean                  continueAction = true;
            LOG.info("Counditions#: " + rcList.size());
            for (RuleCondition rc : rcList) {
                String fieldName = rc.getFieldName();

                //
                // If the state of condition is change then ignore this rule
                // as this is meant to be executed only when there is a change
                // in the state of the request.
                //
                if (rc.getState() == State.CHANGE) {
                    String   oldValue    = rc.getOldValue();
                    String   newValue    = rc.getNewValue();
                    Operator oldOperator = rc.getOldOperator();
                    Operator newOperator = rc.getNewOperator();

                    //
                    // Since there is a change, check if the old value
                    // matches the old value specified by the rule.
                    //
                    boolean flag = compare(fieldName, oldValue, oldOperator, false);

                    //
                    // If the old state of the request does not suit the rule
                    // to be executed. skip this.
                    //
                    if (flag == false) {
                    	LOG.info("Comparision failed.");
                        continueAction = false;

                        break;
                    }

                    //
                    // Since old state of the request matched the rule, check
                    // if the new value matches the new value specified by the
                    // rule.
                    //
                    flag = compare(fieldName, newValue, newOperator, true);

                    //
                    // If the new state of the request does not suit the rule
                    // to be executed. So, skip this.
                    //
                    if (flag == false) {
                        continueAction = false;

                        break;
                    }
                } else if (rc.getState() == State.CURRENT) {

                    // This is check on the current state of the request.
                    String   fieldValue = rc.getCurrentValue();
                    Operator operator   = rc.getCurrentOperator();
                    boolean  flag       = compare(fieldName, fieldValue, operator, true);

                    if (flag == false) {

                        //
                        // The state of the request does not suit the rule to
                        // be executed. So, skip this.
                        //
                        continueAction = false;

                        break;
                    }
                }
            }

            //
            // Check why we came out of the above for loop. If continueAction
            // is set to false, then we can skip the action part and continue
            // with the next rule.
            //
            if (continueAction == false) {
                continue;
            }

            // Get the list of actions to be performed.
            ArrayList<RuleAction> raList = br.getActionList();

            for (RuleAction ra : raList) {
                String   fieldName = ra.getFieldName();
                String   ruleValue = ra.getValue();
                Operator operator  = ra.getOperator();

                // Check the action type.
                if (ra.getActionType() == ActionType.VALIDATE) {

                    // If the action type is valid, compare the field value and
                    // check if the state is valid.
                    boolean flag = compare(fieldName, ruleValue, operator, true);

                    if (flag == false) {
//                        if (ourSource == SOURCE_EMAIL) {
//                            revertToOldValue(fieldName);
//                            myRuleWarnings.append("\n[ ").append(ruleMessage).append(" ]");
//                        } else {
                            TBitsException de = new TBitsException(ruleMessage);
                            myException.addException(de, SEVERE);
                            canContinue = false;
                            break;
//                        }
                    }
                } else if (ra.getActionType() == ActionType.MODIFY) {
                    modify(fieldName, ruleValue, operator);
                }
            }
        }

        if(!canContinue)
        	throw myException;
        // Check if there are any exceptions in the list above the acceptable
        // level.
        if (myException.getExceptionCount(myLevel) > 0) {
            throw myException;
        }

        return;
    }

    /**
     * This method invokes the TBitsMailer to send mails for this request.
     */
    public void sendMail(MailResourceManager mailResMgr) {
    	 try {
         	mailResMgr.queueForDelivery(myRequest);
         } catch (RuntimeException e) {
             LOG.error("Mails not send for:" + "\nSysPrefix : " + myBusinessArea.getSystemPrefix() + "\nRequestId : " + myRequest.getRequestId() + "\nActionId : " + myRequest.getMaxActionId() + "\n\n"
                       + "",(e));
         } catch (Exception e) {
             LOG.error("Mails not send for:" + "\nSysPrefix : " + myBusinessArea.getSystemPrefix() + "\nRequestId : " + myRequest.getRequestId() + "\nActionId : " + myRequest.getMaxActionId() + "\n\n"
                       + "",(e));
         }
    }

    /**
     * This method tracks the due date.
     */
    private String trackDueDate() {
        Field field = myFieldTable.get(Field.DUE_DATE);
        if ((field != null) && (field.getTrackingOption() != 0)) {

            // If both are null.
            if ((myDueDate == null) && (myOldRequest.getDueDate() == null)) {
                return "";
            }

            // If current value is null.
            if (myDueDate == null) {
                String oldDueDate = "";

                if (Timestamp.toDateMin(myOldRequest.getDueDate()).indexOf("23:59") != -1) {
                    oldDueDate = Timestamp.toCustomFormat(myOldRequest.getDueDate(),"MM/dd/yyyy");
                } else {
                    oldDueDate = Timestamp.toDateMin(myOldRequest.getDueDate());
                }

                return getTrackRecord(field, "", oldDueDate);
            }

            // If old value is null.
            if (myOldRequest.getDueDate() == null) {
                String newDueDate = "";

                if (myDueDate.toDateMin().indexOf("23:59") != -1) {
                    newDueDate = myDueDate.toCustomFormat("MM/dd/yyyy");
                } else {
                    newDueDate = myDueDate.toDateMin();
                }

                return getTrackRecord(field, newDueDate, "");
            }

            // If both are not null.
            if (myDueDate.equals(myOldRequest.getDueDate()) == false) {
                String oldDueDate = "";

                if (Timestamp.toDateMin(myOldRequest.getDueDate()).indexOf("23:59") != -1) {
                    oldDueDate = Timestamp.toCustomFormat(myOldRequest.getDueDate(),"MM/dd/yyyy");
                } else {
                    oldDueDate = Timestamp.toDateMin(myOldRequest.getDueDate());
                }

                String newDueDate = "";

                if (myDueDate.toDateMin().indexOf("23:59") != -1) {
                    newDueDate = myDueDate.toCustomFormat("MM/dd/yyyy");
                } else {
                    newDueDate = myDueDate.toDateMin();
                }

                return getTrackRecord(field, newDueDate, oldDueDate);
            }
        }

        return "";
    }

    /**
     *
     */
    private String trackRelatedRequests(Field aField) {
        Enumeration<String> e       = Request.getRelatedRequests(myOldRequest.getSystemId(), myOldRequest.getRequestId()).keys();
        ArrayList<String>   oldList = new ArrayList<String>();

        while (e.hasMoreElements()) {
            oldList.add(e.nextElement());
        }

        ArrayList<String> newList = new ArrayList<String>();

        if ((myRequest.getRelatedRequests() != null) && (myRequest.getRelatedRequests().equals("") == false)) {
            newList = Utilities.toArrayList(myRequest.getRelatedRequests().toLowerCase(), ";, ");
        }

        newList = getBAPrefixCases(newList);

        // If the size of the lists are equals.
        // Check if the elements are also equal.
        boolean equal = false;

        if (newList.size() == oldList.size()) {
            equal = true;

            for (String str : oldList) {
                if (newList.contains(str) == false) {
                    equal = false;

                    break;
                }
            }
        }

        if (equal == true) {
            return "";
        }

        return getTrackRecord(aField, Utilities.arrayListToString(newList), Utilities.arrayListToString(oldList));
    }

    /**
     * This method checks if there is a change in the user lists and gets the
     * track record appropriately.
     *
     * @param aField   Field that represents this user list.
     * @param aNewList New List of request users.
     * @param aOldList Old List of request users.
     *
     * @return Track record.
     */
    private String trackUserList(Field aField, Collection<RequestUser> aNewList, Collection<RequestUser> aOldList) {
        String oldList = "";
        String newList = "";

        // Handle the null reference.
        if (aOldList == null) {
            aOldList = new ArrayList<RequestUser>();
            oldList  = "";
        } else {
            oldList = APIUtil.toLoginList(aOldList, ", ");
        }

        // Handle the null reference.
        if (aNewList == null) {
            aNewList = new ArrayList<RequestUser>();
            newList  = "";
        } else {
            newList = APIUtil.toLoginList(aNewList, ", ");
        }

        StringBuilder record            = new StringBuilder();
        String        defunctUsers      = myDefunctUsers.get(aField.getName());
        String        emailDefunctUsers = myDefunctUsers.get("EMAIL_" + aField.getName());
        String        baMailIds         = myBaMailIds.get(aField.getName());

        baMailIds    = ((baMailIds == null)
                        ? ""
                        : baMailIds.trim());
        defunctUsers = ((defunctUsers == null)
                        ? ""
                        : defunctUsers.trim());

        if (baMailIds.equals("") == false) {
            newList = ((newList.equals("") == true)
                       ? baMailIds
                       : newList + ", " + baMailIds);
        }

        if ((emailDefunctUsers != null) && (emailDefunctUsers.equals("") == false)) {
            defunctUsers = ((defunctUsers.equals("") == true)
                            ? emailDefunctUsers
                            : defunctUsers + ", " + emailDefunctUsers);
            baMailIds    = ((baMailIds.equals("") == true)
                            ? emailDefunctUsers
                            : baMailIds + ", " + emailDefunctUsers);
        }

        if ((defunctUsers != null) && (defunctUsers.trim().equals("") == false)) {
            record.append("[ Not added to the " + aField.getDisplayName() + " list: ").append(defunctUsers).append(" ]\n");
        }

        if ((baMailIds != null) && (baMailIds.trim().equals("") == false)) {
            record.append("[ Send through email, so not resent to: ").append(baMailIds).append(" ]\n");
        }

        // Get the tracking option of this user field.
        int trackingOption = aField.getTrackingOption();

        if ((trackingOption == 3) || (trackingOption == 4) || (trackingOption == 5)) {
            formTrackRecord(aField, aOldList, aNewList, record);

            return record.toString();
        }

        // If the size of the lists are equals.
        // Check if the elements are also equal.
        boolean equal = false;

        if (aNewList.size() == aOldList.size()) {
            equal = true;

            for (RequestUser ru : aOldList) {
                if (aNewList.contains(ru) == false) {
                    equal = false;

                    break;
                }
            }
        }

        if ((equal == true) && (aField.getTrackingOption() == 3)) {
            return record.toString();
        }

        return getTrackRecord(aField, newList, oldList) + record.toString();
    }

    /**
     * This method builds the Request object from the attributes that are
     * obtained from the param table passed to the addRequest method.
     */
    private void updateLocals() {
        myCategory      = myRequest.getCategoryId();
        myStatus        = myRequest.getStatusId();
        mySeverity      = myRequest.getSeverityId();
        myRequestType   = myRequest.getRequestTypeId();
        myOffice        = myRequest.getOfficeId();
        mySubject       = myRequest.getSubject();
        myIsPrivate     = myRequest.getIsPrivate();
        myParentId      = myRequest.getParentRequestId();
        myDueDate       = (myRequest.getDueDate() == null ? null : new Timestamp(myRequest.getDueDate().getTime()));
        myNotify        = (myRequest.getNotify() == false ? 0 : 1 );
        myNotifyLoggers = myRequest.getNotifyLoggers();

        //
        // Set the fields which actually reside in a different table but are
        // related to the request.
        //
        myLoggers        = (myRequest.getLoggers() == null ? null : new ArrayList<RequestUser>(myRequest.getLoggers()));
        myAssignees      = (myRequest.getAssignees() == null ? null : new ArrayList<RequestUser>(myRequest.getAssignees()));
        mySubscribers    = (myRequest.getSubscribers() == null ? null : new ArrayList<RequestUser>(myRequest.getSubscribers()));
        myTos            = (myRequest.getTos() == null ? null : new ArrayList<RequestUser>(myRequest.getTos()));
        myCcs            = (myRequest.getCcs() == null ? null : new ArrayList<RequestUser>(myRequest.getCcs()));
//        myExtendedFields = myRequest.getExtendedFields();
        myAttachments    = myRequest.getAttachments();
    }

    /**
     * This method calls the insert method with this request object.
     */
    private void updateRequest(Connection connection) throws APIException {
        try {
            Request.update(myRequest, connection);
            myMaxActionId = myRequest.getMaxActionId();

            //
            // For subrequest, load request object from db to get information
            // about sibling requests, if parent Id has been changed.
            //
            // Nitiraj : removing this logic as the sibling requests and sub-requests are no longer
            // member of of the request object
//            try {
//                if (myRequest.getParentRequestId() != myOldRequest.getParentRequestId()) {
//                    myRequest = Request.lookupBySystemIdAndRequestId(connection, myRequest.getSystemId(), myRequestId);
//                }
//
//                myRequest.setSummary(mySummary);
//            } catch (DatabaseException e) {
//                LOG.warn("",(e));
//            }

            StringBuilder message = new StringBuilder();

            message.append("Request Updated [ ").append(myBusinessArea.getDisplayName()).append(", ").append(myRequestId).append(", ").append(myMaxActionId).append(" ]");

            // LOG.debug(message.toString());
            // Check if we need to update the volunteer
            if ((nextVolunteer != null) && (nextVolunteer.equals("") == false)) {
                try {
                    User tmp = User.lookupByUserLogin(nextVolunteer);

                    if ((tmp != null) && (myCategory != null)) {
                        TypeUser.updateNextVolunteer(mySystemId, myRequest.getCategoryId().getFieldId(), myRequest.getCategoryId().getTypeId(), tmp.getUserId());
                    }
                } catch (DatabaseException de) {
                    LOG.error("",(de));
                }
            }
        } catch (DatabaseException de) {
            LOG.warn("",(de));
            myException.addException(new TBitsException(de), FATAL);
        }

        // Check if there are any exceptions in the list above the acceptable
        // level.
        if (myException.getExceptionCount(myLevel) > 0) {
            throw myException;
        }

        return;
    }

    /**
     * This method is used to update a request in the database.
     *
     * @param aParamTable Table of (key, value) pairs.
     *
     * @return Request object after successful updation in the database.
     *
     * @exception APIException List of exceptions occurred during processing.
     * @throws TBitsException 
     */
    public Request updateRequest(Hashtable<String, String> aParamTable) throws APIException, TBitsException {
    	Connection conn = null;
		Request req = null;
		
		TBitsResourceManager tbitsResMgr = new TBitsResourceManager() ;
		
		try {
			conn = DataSourcePool.getConnection();
			conn.setAutoCommit(false);
			req = updateRequest(conn, tbitsResMgr , aParamTable);
			 
			conn.commit();
			// conn.close();
			tbitsResMgr.commit() ;			
						
		} catch (APIException apiException) {
			try {
				tbitsResMgr.rollback();				
				if(conn != null)
					conn.rollback();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			throw apiException;
		} catch (SQLException e) {
			try {
				tbitsResMgr.rollback();				
				conn.rollback();
			} catch (SQLException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			APIException apie = new APIException();
			apie.addException(new TBitsException(
					"Unable to get connection to the database"));
			throw apie;
		}
		finally
		{
			try {
				if((conn != null) && !conn.isClosed())
				{
					conn.close();
				}
			} catch (SQLException e) {
				LOG.error(new Exception("Unable to close the connection to the database.", e));
			}
		}
		return req;
    }
    public Request updateRequest(Connection connection,TBitsResourceManager tbitsResMgr, Hashtable<String, String> aParamTable) throws APIException, TBitsException {
    	
        long start = System.currentTimeMillis();

        initializeRequestObject(aParamTable);
        //
        // Call the method that reads the values of fixed fields from the
        // param table. This throws APIException only in SEVERE cases.
        // So, lets not catch it as we cannot continue in any of those cases.
        //
        readFixedFieldValues(connection, aParamTable);

    	//
        // Now validate the values against the regular expressions
        // 
     //   performRegExChecks(aParamTable,false,myUser);

      
        //
        // Now, Call the method that reads the values of Extended fields from
        // the param table. This throws APIException only in SEVERE cases.
        // So, lets not catch it as we cannot continue in any of those cases.
        //
        readExtendedFieldValues(aParamTable);
        
        //Read the version information from paramsTable if present
        readVersion(aParamTable);
        //read the updateVersion information
       // readUpdateVersion(aParamTable);

        // Process the values obtained for users lists and build the
        // corresponding user lists.
        processUserLists(aParamTable);

        //
        // Perform the integrity checks which encompasses
        // - Logger list cannot be empty.
        // - Due date cannot be earlier than logged date.
        // - When appending to a request without view on is_private then
        // none of the following can be private.
        // - Business Area.
        // - Request.
        // - Standard Types: Category, Status, Severity, Request Type
        //
        performIntegrityChecks();

        //
        // Get the permissions and see if the state of Request object
        // is valid according to the permissions the user has.
        //
        performPermissionChecking();

        // Now, Put all the attributes together and build the Request Object.
        buildRequestObject();

        // Check if we have any APP-Generate dependencies to be executed.
        checkDependencies();
        
        // Set the attachment info to be updated. 
        // All permissions and checks on carryover etc need to be performed here.
        setAttachmentInfo();
        
        //
        // Make sure that the fields' uniqueness is honored
        // 
        performUniquenessChecks(connection,true,myRequest);

        //
        // Check if this is an append to the stale version of the request,
        // only if the append is not made through email.
        //
        if ((ourSource != SOURCE_EMAIL) && (appendStale == true)) {

            //
            // Generate the diff and throw a TBits Exception if the return
            // value is true.
            //
            if (generateDiff() == true) {
                String prefixId = myBusinessArea.getSystemPrefix() + "#" + myRequest.getRequestId();

                LOG.info("Conflicting update is made to this request " + prefixId);

                throw new TBitsException("Conflicting updates have been made to this request.");
            }
        }

        // Run the request object through workflow rules validator.
        runWorkflowRules();

      //Running the plugged in rules
        try
        {
        	RuleFactory.runPreRules(connection, myBusinessArea, myOldRequest, myRequest, this.ourSource, myUser,false);
        }
        catch(TBitsException te)
        {
        	myException.addException(te, SEVERE);
        	throw myException;
        }
        catch(Exception e)
        {
        	LOG.error(e);
        }
        
        /*
         * fire the prerequest commit event
         */
        IUpdatePreEvent prce = new UpdatePreEvent(connection, myBusinessArea, myOldRequest, myRequest, this.ourSource, myUser, false, tbitsResMgr);
        try {
			EventManager.getInstance().fireEvent(prce);
		} catch (EventFailureException e4) {
			myException.addException(new TBitsException(e4),SEVERE);
			throw myException;
		};
        
		performRegExChecks(myRequest);
        // There might be some changes after running workflow rules.
        // Update the locals.
        updateLocals();

        // There might be some changes to the extended fields
        // Nitiraj : removed 
//        setExtendedFields();
        
        // Now generate the header description.
        generateHeaderDesc();

        // Now, the request object is ready to be updated in the database.
        String tvnName = "";
        try{
    		tvnName = aParamTable.get(WebdavConstants.TVN_NAME);
	    	if(null != tvnName && !tvnName.equals("") && aParamTable.get("from").equals("client")){
				myRequest.setSubject(tvnName);
				myRequest.setSource(TBitsConstants.SOURCE_TVN);
	    	}
    	}catch(Exception ex){
    		System.out.println(ex.getMessage());
    	}
    	
        updateRequest(connection);

        ArrayList<FileAction> fileActions = new ArrayList<FileAction>();
    	
        // Extract the attachment type fields
        ArrayList<Field> attFields = new ArrayList<Field>();
        try {
			attFields = Field.lookupBySystemId(mySystemId, DataType.ATTACHMENTS);
		} catch (DatabaseException e) {
			e.printStackTrace();
			myException.addException(new TBitsException(e));
			throw myException;
		}
		
		// Process the attachment infos
        for(Field attField : attFields){
        	
        	Collection<AttachmentInfo> oldAttachments = (Collection<AttachmentInfo>) myOldRequest.getObject(attField);
			Collection<AttachmentInfo> newAttachments = (Collection<AttachmentInfo>) myRequest.getObject(attField);
        	if (myAttachments != null)
            {
        		try {
        			for(AttachmentInfo ai:newAttachments)
                	{
                		//Check whether it was added
                		if(ai.requestFileId == 0)
                		{
            				ai.requestFileId = APIUtil.getAndCreateRequestFileId(myRequest.getSystemId(), myRequest.getRequestId());
                		}
                	}
             		
        			ArrayList<FileAction> fieldFileActions = APIUtil.getAttachmentDiff(attField.getFieldId(), newAttachments, oldAttachments);
        			
        			ArrayList<FileAction> ffaCopy = new ArrayList<FileAction>();
        			ffaCopy.addAll(fieldFileActions);
        			for(FileAction fa : ffaCopy){
        				User locker = APIUtil.getFileLocker(connection, myRequest.getSystemId(), myRequest.getRequestId(), attField.getFieldId(), fa.getAttachmentInfo().getRequestFileId());
        				if(locker != null){
        					if(locker.getUserId()!=myUser.getUserId()){
        						fieldFileActions.remove(fa);
            					throw new Exception(("The following file is locked : " + fa.getAttachmentInfo().getName() + "\nLocked by : " + locker.getUserLogin()));
        					}
        					else{
        						APIUtil.removeFileLock(connection, myRequest.getSystemId(), myRequest.getRequestId(), attField.getFieldId(), fa.getAttachmentInfo().getRequestFileId());
        					}
        				}
        			}
            		
    				checkAttachmentFieldPermissions(attField, fieldFileActions);
            		
    				fileActions.addAll(fieldFileActions);
    				
    				myRequest.setObject(attField.getName(), newAttachments);
    				if(attField.getIsExtended()){
	    				Request.updateAttachmentsExt(connection, myRequest.getSystemId(),
	    						myRequest.getRequestId(), attField.getFieldId(), myRequest.getMaxActionId(), 
	    						AttachmentInfo.toJson(newAttachments));
    				}
    				else{
	             		Request.updateAttachments(connection, myRequest.getSystemId(), 
	             				myRequest.getRequestId(), 
	             				myRequest.getMaxActionId(), 
	             				AttachmentInfo.toJson(myRequest.getAttachments()));
    				}
             	} 
        		catch (Exception e) {
             			LOG.warn("",(e));
             			myException.addException(new TBitsException(e));
             			throw myException;
             	}
             }
        }
		
        //Added By Abhishek, increment the version
        try {
        	/* this block is commented as now version can be incremented even if 
        	 * there are no attachments.reason being that category, status or request type
        	 * can be changed without any attachments being added.
        	 */
        	/*if((null != myRequest.getAttachments() 
        			&& !myRequest.getAttachments().equals("")) 
        			|| fileActions.size() != 0) {*/
        		int version = 0;
        		String from  = aParamTable.get("from");
        		boolean fromWeb = true;
        		if(from != null)
        			fromWeb = !from.equals("client");
        		version = WebdavUtil.updateVersion(connection, myRequest,fileActions,false,tvnName,verNum, fromWeb);
        		if(version == -1)
        			throw new TBitsException("TVN Name already present");
        		myRequest.setVersionNum(version);
        	/*}*/
		} catch (TBitsException e1) {
			APIException apiE = new APIException();
			apiE.addException(e1);
			throw apiE;
		}
        
        //
        // insert Related requests entries if there is any change from
        // the previous value or any Ba cc'ed in this append.
        //
        String tmpNewRelReq = myRelatedRequests;

        tmpNewRelReq = (tmpNewRelReq == null)
                       ? ""
                       : tmpNewRelReq.trim().toLowerCase();

        String tmpOldRelReq = myOldRequest.getRelatedRequests();

        tmpOldRelReq = (tmpOldRelReq == null)
                       ? ""
                       : tmpOldRelReq.trim().toLowerCase();

        try {
            if (!(tmpNewRelReq.equals(tmpOldRelReq)) && (myBaMailIds.size() == 0)) {
                myRequest = APIUtil.insertRelatedRequests(connection, myRequest, myBaMailIds, myRelatedRequests);
            }
        } catch (Exception e) {
            LOG.severe("\nUserId: " + myRequest.getUserId() + "\nBusinessArea: " + myBusinessArea.getSystemPrefix() + "\nRequestId: " + myRequest.getRequestId() + "\n\n"
                       + "",(e));
        }
  
      //  performRegExChecks(myRequest);
        
      //Running the post process in rules
        try
        {
        	RuleFactory.runPostRules(connection, myBusinessArea, myOldRequest, myRequest, this.ourSource, myUser, false);
        }
        catch(TBitsException te)
        {
        	myException.addException(te, SEVERE);
        	throw myException;
        }
        catch(Exception e)
        {
        	LOG.error(e);
        }
        
        /*
         * fire post commit event
         */
        IUpdatePostEvent postEvent = new UpdatePostEvent(connection, myBusinessArea, myOldRequest, myRequest, this.ourSource, myUser, false , tbitsResMgr);
        try {
			EventManager.getInstance().fireEvent(postEvent);
		} catch (EventFailureException e4) {
			myException.addException(new TBitsException(e4),SEVERE);
			throw myException;
		};
        
        try {
            UserReadAction.registerUserReadAction(connection, myRequest.getSystemId(), myRequest.getRequestId(), myRequest.getMaxActionId(), myRequest.getUserId());
        } catch (DatabaseException de) {
            LOG.warn("",(de));
            myException.addException(new TBitsException(de.toString()), WARNING);
        }

        long end = System.currentTimeMillis();

        LOG.info("Total Time taken to update request " + myBusinessArea.getSystemPrefix() + "#" + myRequest.getRequestId() + "#" + myRequest.getMaxActionId() + ": " + (end - start) + " mecs");


        sendMail(tbitsResMgr.getMailResourceManager());

        try
        {
	        // send this request for indexing to indexResourceManager // added by Nitiraj
	        tbitsResMgr.getIndexerResourceManager().queueForIndexing(myBusinessArea.getSystemPrefix()
	        														, mySystemId,  myRequestId
	        														, Configuration.findAbsolutePath(PropertiesHandler.getProperty(KEY_INDEXDIR))
	        													   ) ;
        }catch(Throwable t)
        {
        	LOG.error("Unable to index. ", t);
        }
		if (TBitsHelper.isSMSEnabled()) {
			new Thread() {
				public void run() {
					try {
						new SMS().sendSMS(myRequest);
					} catch (SQLException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}.start();
		}
        // Finally return the latest request object.
        return myRequest;
    }

    /**
     * Checks the attachments in the current request and the old request and corrects the information to be updated 
     * based on the attachment infos and the carryover permission of the field. 
     * @throws APIException 
     */
    private void setAttachmentInfo() throws APIException {
		
    	// Extract the attachment type fields and corresponding attinfo objects
        ArrayList<Field> attFields = new ArrayList<Field>();
        try {
			attFields = Field.lookupBySystemId(mySystemId, DataType.ATTACHMENTS);
		} catch (DatabaseException e) {
			e.printStackTrace();
			myException.addException(new TBitsException(e));
			throw myException;
		}
        
		for (Field atf : attFields) {
			
			Collection<AttachmentInfo> oldAttachments = (Collection<AttachmentInfo>) myOldRequest.getObject(atf);
			if(oldAttachments == null)
				oldAttachments = (Collection<AttachmentInfo>) new ArrayList<AttachmentInfo>();
			
			Collection<AttachmentInfo> newAttachments = (Collection<AttachmentInfo>) myRequest.getObject(atf);
			
			// Check for carry over
			if(newAttachments == null && (atf.getPermission() & Permission.SET) != 0){
				// Add all the old attachments to new attachments
				newAttachments = (Collection<AttachmentInfo>) new ArrayList<AttachmentInfo>();
				newAttachments.addAll(oldAttachments);
			}
			
			if(newAttachments == null)
				newAttachments = (Collection<AttachmentInfo>) new ArrayList<AttachmentInfo>();
			
			
			// Add the attachment info back to the old and new requests
			myOldRequest.setObject(atf, oldAttachments);
			myRequest.setObject(atf, newAttachments);
		}
		
	}

	private void performUniquenessChecks(Connection connection,boolean isUpdate,Request myRequest) throws APIException {
    	try
    	{
    		APIUtil.performUniquenessChecksOnFieldValues(connection,myFieldTable, isUpdate,myRequest);
    	}
    	catch(TBitsException tex)
    	{
    		myException.addException(tex, PERROR);
    		throw myException;
    	}
	}

/*	private void performRegExChecks(Hashtable<String,String> paramTable, boolean isAddRequest,User user) throws APIException {
    	try
    	{
    		APIUtil.performRegExChecksOnFieldValues(paramTable, myFieldTable, false,myUser);
    	}
    	catch(TBitsException tex)
    	{
    		myException.addException(tex, PERROR);
    		throw myException;
    	}
    }*/
	
	private void performRegExChecks(Request myRequest) throws APIException {
    	try
    	{
    		APIUtil.performRegExpChecksOnFieldValues(myRequest);
    	}
    	catch(TBitsException tex)
    	{
    		myException.addException(tex, PERROR);
    		throw myException;
    	}
    }
 
	private void checkAttachmentFieldPermissions(Field atf,
			ArrayList<FileAction> fieldFileActions) throws APIException {
		Integer permInteger = myPermTable.get(atf.getName());
		if(permInteger == null)
			permInteger = 0;
		
		boolean canChange = ((permInteger.intValue() & Permission.CHANGE) != 0);
		boolean canAdd = ((permInteger.intValue() & Permission.ADD) != 0);
		
		
		for(FileAction fa:fieldFileActions)
		{
			if( (fa.getFileAction().equals(WebdavConstants.FILE_MODIFIED) || fa.getFileAction().equals(WebdavConstants.FILE_DELETED)) && !canChange)
			{
				//throw exception
				myException.addException(new TBitsException(noPermission(atf.getDisplayName(), "Change", myBusinessArea.getSystemPrefix(),  myUser.getUserLogin())), PERROR);
				throw myException;
			}
			if( fa.getFileAction().equals(WebdavConstants.FILE_ADDED) && !canAdd)
			{
				myException.addException(new TBitsException(noPermission(atf.getDisplayName(), "Add", myBusinessArea.getSystemPrefix(),  myUser.getUserLogin())), PERROR);
				throw myException;
			}
		}
	}


	private void readVersion(Hashtable<String, String> paramTable) throws APIException {
		String strVerNum = paramTable.get(WebdavConstants.VERSION_NUM);
		if(strVerNum == null) {
			verNum = 0;
			return;
		}
		
		 // So we got a value. Parse this. It should be a int  and if not throw
        // an exception.
        try {
            verNum = Integer.parseInt(strVerNum);
        } catch (NumberFormatException nfe) {
            myException.addException(new TBitsException("Version number should be numeric."), FATAL);

            // We cannot continue further. Throw it here.
            throw myException;
        }

	}

	// ~--- get methods --------------------------------------------------------

    private void runRulePlugins() {
		// TODO Auto-generated method stub
		
	}

	/*
	 * 
	 */
    public ArrayList<String> getBAPrefixCases(ArrayList<String> aList) {
        ArrayList<String> newList = new ArrayList<String>();

        if ((aList == null) || (aList.size() == 0)) {
            return newList;
        }

        String primaryPrefix    = myBusinessArea.getSystemPrefix();
        int    primaryRequestId = myRequest.getRequestId();

        //
        // Check each string in list for sysPrefix#NNN pattern.
        //
        Pattern      p  = Pattern.compile("(([a-zA-Z0-9_]+)#)?([0-9]+)(#([0-9]+))?", Pattern.CASE_INSENSITIVE);
        Matcher      m  = null;
        BusinessArea ba = null;

        for (String str : aList) {
            m = p.matcher(str);

            //
            // If string matches pattern sysPrefix#NNN
            //
            if (m.matches() == true) {
                String prefix = (m.group(1) == null)
                                ? primaryPrefix
                                : m.group(2);

                //
                // Check if its a valid Ba prefix.
                //
                if (primaryPrefix.equals(prefix) == false) {
                    try {
                        ba = BusinessArea.lookupBySystemPrefix(prefix);
                    } catch (Exception e) {
                        LOG.severe("",(e));
                    }

                    if (ba == null) {
                        if (SysPrefixes.getPrefix(prefix) == null) {

                            // should this be reported?
                            continue;
                        } else {
                            prefix = SysPrefixes.getPrefix(prefix);
                        }
                    } else {
                        prefix = ba.getSystemPrefix();
                    }
                }

                int requestId = Integer.parseInt(m.group(3));
                int actionId  = (m.group(4) == null)
                                ? 0
                                : Integer.parseInt(m.group(5));

                //
                // accept only if not relating to the same request
                //
                if (str.equalsIgnoreCase(primaryPrefix + "#" + primaryRequestId) == false) {
                    String str1 = prefix + "#" + requestId;
                    String str2 = str1 + ((actionId > 0)
                                          ? "#" + actionId
                                          : "");

                    if (newList.contains(str1) == true) {
                        continue;
                    }

                    if (newList.contains(str2) == false) {
                        newList.add(str2);
                    }
                }
            }
        }

        return newList;
    }

    /**
     * Accessor method for the myDiffList object.
     */
    public ArrayList<DiffEntry> getDiffList() {
        return myDiffList;
    }

    /**
     * Accessor method for the myException object.
     */
    public APIException getExceptions() {
        return myException;
    }

    /**
     * This method returns the default due date of this BA in GMT.
     *
     * @return Default due date for this BA in GMT.
     */
    private Timestamp getGMTDefaultDueDate() {
        Calendar cal      = Calendar.getInstance();
        int      duration = (int) mySysConfig.getDefaultDueDate();

        cal.add(Calendar.MINUTE, duration);

        Timestamp dueDate = Timestamp.getTimestamp(cal.getTime());

        return dueDate.toGmtTimestamp();
    }

    /**
     * Accessor method for the myDiffList object.
     */
    public int getMaxActionId() {
        return myOldRequest.getMaxActionId();
    }

    /**
     * This method returns the track record for the given field and values.
     *
     * @param aField    Field object to generate the track record.
     * @param aNewValue current value in the field.
     * @param aOldValue current value in the field.
     *
     * @return Track record.
     */
    public static String getTrackRecord(Field aField, String aNewValue, String aOldValue) {
        StringBuilder record = new StringBuilder();

        // Handle the null reference case.
        if (aField == null) {
            return record.toString();
        }

        aNewValue = ((aNewValue == null)
                     ? ""
                     : aNewValue.trim());
        aOldValue = ((aOldValue == null)
                     ? ""
                     : aOldValue.trim());

        switch (aField.getTrackingOption()) {
        case 0 :

            // No tracking.
            break;

        case 1 :

            // [ Field: 'newvalue' ]
            record.append(aField.getName()).append("##").append(aField.getFieldId()).append("##[ ").append(aField.getDisplayName()).append(": ").append(aNewValue).append(" ]\n");

            break;

        case 2 :

            // [ Field: 'newvalue' ] when newValue not empty
            if (aNewValue.trim().equals("") == false) {
                record.append(aField.getName()).append("##").append(aField.getFieldId()).append("##[ ").append(aField.getDisplayName()).append(": ").append(aNewValue).append(" ]\n");
            }

            break;

        case 3 :
            if (aOldValue.equals(aNewValue) == false) {

                // [ Field changed from 'oldvalue' to 'newvalue' ]
                record.append(aField.getName()).append("##").append(aField.getFieldId()).append("##[ ").append(aField.getDisplayName()).append(" changed from '").append(aOldValue).append(
                    "' to '").append(aNewValue).append("' ]\n");
            }

            break;

        case 4 :

            // [ Field changed from 'oldvalue' to 'newvalue' ]
            // Or
            // [ Field: 'newvalue' ]
            if (aOldValue.equals(aNewValue) == false) {
                record.append(aField.getName()).append("##").append(aField.getFieldId()).append("##[ ").append(aField.getDisplayName()).append(" changed from '").append(aOldValue).append(
                    "' to '").append(aNewValue).append("' ]\n");
            } else {
                record.append(aField.getName()).append("##").append(aField.getFieldId()).append("##[ ").append(aField.getDisplayName()).append(": ").append(aNewValue).append(" ]\n");
            }

            break;

        case 5 :

            // [ Field changed from 'oldvalue' to 'newvalue' ]
            // Or
            // [ Field: 'newvalue' ] when new value not empty
            if (aOldValue.equals(aNewValue) == false) {
                record.append(aField.getName()).append("##").append(aField.getFieldId()).append("##[ ").append(aField.getDisplayName()).append(" changed from '").append(aOldValue).append(
                    "' to '").append(aNewValue).append("' ]\n");
            } else if (aNewValue.trim().equals("") == false) {
                record.append(aField.getName()).append("##").append(aField.getFieldId()).append("##[ ").append(aField.getDisplayName()).append(": ").append(aNewValue).append(" ]\n");
            }

            break;
        }

        return record.toString();
    }

    /**
     * This method checks if there is any change in the value of the field
     * specified by the aFieldName.
     *
     * @param aFieldName  Field name.
     *
     * @return True if there is any change, False otherwise.
     */
    private boolean isChanged(String aFieldName) {
        String oldValue = myOldRequest.get(aFieldName);
        String newValue = myRequest.get(aFieldName);

        // If both are null, there is no change.
        if ((oldValue == null) && (newValue == null)) {
            return false;
        }

        // If only one of them is null, then there is a change.
        if ((oldValue == null) || (newValue == null)) {
            return true;
        }

        // if both are not null, then string comparison will decide if there
        // is any change.
        return !oldValue.trim().equalsIgnoreCase(newValue.trim());
    }

    /**
     * This method checks if the request user list has been changed during
     * this update.
     *
     * @param aNewList  New list of request users.
     * @param aOldList  Old list of request users.
     * @return          True if there is a change, False otherwise.
     * @throws APIException
     */
    private boolean isChanged(Collection<RequestUser> aNewList, Collection<RequestUser> aOldList) throws APIException {
        boolean returnValue = false;
        int     newSize     = 0;
        int     oldSize     = 0;

        if (aNewList != null) {
            newSize = aNewList.size();
        }

        if (aOldList != null) {
            oldSize = aOldList.size();
        }

        // If the sizes are different then there is a change in the list.
        if (newSize != oldSize) {
            returnValue = true;
        } else {

            // Check if everyone in the new-list are in the old-list too.
            for (RequestUser reqUser : aNewList) {
                if (aOldList.contains(reqUser) == false) {
                    returnValue = true;

                    break;
                }
            }
        }

        return returnValue;
    }

    /**
     * This method checks if the request user list has been changed during
     * this update.
     *
     * @param aRepliedToList  New list of  users.
     * @param aOldList  Old list of request users.
     * @return          True if there is a change, False otherwise.
     * @throws APIException
     */
    private boolean isChangedUsers(ArrayList<Integer> aRepliedToList, ArrayList<RequestUser> aOldList) throws APIException {
        boolean returnValue = false;
        int     newSize     = 0;
        int     oldSize     = 0;

        if (aRepliedToList != null) {
            newSize = aRepliedToList.size();
        }

        if (aOldList != null) {
            oldSize = aOldList.size();
        }

        // If the sizes are different then there is a change in the list.
        if (newSize != oldSize) {
            returnValue = true;
        } else {
            try {

                // Check if everyone in the new-list are in the old-list too.
                for (RequestUser reqUser : aOldList) {
                    Integer intUser = new Integer(reqUser.getUser().getUserId());

                    if (aRepliedToList.contains(intUser) == false) {
                        returnValue = true;

                        break;
                    }
                }
            } catch (DatabaseException dbe) {
                LOG.info("An exception occured while retrieving the user Id" + "from the request user object. ");
            }
        }

        return returnValue;
    }
    
    /**
     * This method checks if the association is valid.
     */
    private boolean isParentValid(Connection conn) {

        // Check if there is any potential loop if this association is allowed.
        try {
            Hashtable<Integer, Integer> table     = Request.getChildrenBySystemId(conn, mySystemId);
            Integer                     parentId  = new Integer(myParentId);
            int                         requestId = myRequestId;

            while ((parentId != null) && (parentId.intValue() != requestId)) {
                parentId = table.get(new Integer(parentId));
            }

            if (parentId == null) {
                return true;
            } else if (parentId.intValue() == requestId) {
                return false;
            }
        } catch (DatabaseException de) {
            LOG.warn("",(de));
            myParentId = 0;
            myException.addException(new TBitsException(de.toString()), SEVERE);

            return false;
        }

        return false;
    }

    //~--- set methods --------------------------------------------------------

    /**
     * This method sets the extended fields value of the request.
     */
    private void setExtendedFields() {
        try {
            myRequest.setExtendedFields(myExtendedFields);
        } catch (DatabaseException de) {
            LOG.warn("",(de));
        }
    }

    /**
     * Mutator method for myUnauthorized
     */
    public void setIsUnauthorized(boolean aIsUnauthorized) {
        myUnauthorized = aIsUnauthorized;
    }

	public void setSource(int source) {
		this.ourSource = source;
	}
	
	public int getSource()
	{
		return this.ourSource;
	}
	/*
	 * If the request is coming from the web interface, you can set this to HttpServletRequest.getContextPath().
	 * other wise you can leave it as such.
	 */
	public void setContext(String aContext)
	{
		myContext = aContext;
	}
	
	/*
	 * returns the context path of application. If the request is not generated from webapps, it is null.
	 */
	public String getContext()
	{
		return myContext;
	}
}
