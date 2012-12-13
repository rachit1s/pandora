
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
 * AddRequest.java
 *
 * $Header:
 */
package transbit.tbits.api;

//--- non-JDK imports --------------------------------------------------------

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
import static transbit.tbits.domain.Permission.ADD;
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

import transbit.tbits.Helper.LinkFormatter;
import transbit.tbits.Helper.Messages;
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
import transbit.tbits.config.BusinessRule.Operator;
import transbit.tbits.config.DependencyConfig;
import transbit.tbits.config.RuleAction;
import transbit.tbits.config.RuleAction.ActionType;
import transbit.tbits.config.RuleCondition;
import transbit.tbits.config.RuleCondition.State;
import transbit.tbits.config.SysConfig;
import transbit.tbits.domain.BARule;
import transbit.tbits.domain.BusinessArea;
import transbit.tbits.domain.DataType;
import transbit.tbits.domain.Dependency;
import transbit.tbits.domain.Dependency.DepLevel;
import transbit.tbits.domain.Dependency.DepType;
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
import transbit.tbits.events.AddPostEvent;
import transbit.tbits.events.AddPreEvent;
import transbit.tbits.events.EventFailureException;
import transbit.tbits.events.EventManager;
import transbit.tbits.events.IAddPostEvent;
import transbit.tbits.events.IAddPreEvent;
import transbit.tbits.exception.APIException;
import transbit.tbits.exception.TBitsException;
import transbit.tbits.external.Resource;
import transbit.tbits.external.ResourceAttr;
import transbit.tbits.external.ResourceResultMap;
import transbit.tbits.sms.SMS;
import transbit.tbits.webapps.MyRequests;
import transbit.tbits.webapps.WebUtil;

//~--- classes ----------------------------------------------------------------

//Third Party Imports.

/**
 * This class is used to add a request to a business area.
 *
 * @author  : Vinod Gupta
 *
 * @version : $Id: $
 *
 */
public class AddRequest implements TBitsConstants {

    // Application Logger.
    public static final TBitsLogger LOG = TBitsLogger.getLogger(PKG_API);

    // Enum to indicate if the external user should be inserted into the DB.
    private static final boolean INSERT            = true;
    private static final boolean NO_INSERT         = false;
    private static final boolean NO_INACTIVE       = false;
    private static final boolean NO_EXCLUSION_LIST = false;
    private static final boolean NO_BA_EMAIL       = false;
    private static final boolean NO_AUTO           = false;

    // Enum to indicate if inactive user is allowed in a given user field.
    private static final boolean INACTIVE = true;

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

    // <FieldName, userlist> of defunct users and mail Ids.
    // To be notified in Hader Description
    private Hashtable<String, String> myDefunctUsers = new Hashtable<String, String>();
    private Hashtable<String, String> myBaMailIds    = new Hashtable<String, String>();

    // Indictaor for private requests Logged By Unauthorized Users
    private boolean myUnauthorized       = false;
    private String  myTransferredRequest = "";

    // Acceptable level of exceptions.
    private int myLevel = PERROR;

    /*
     * Check if this is a request as a result of transfer.
     */
    private boolean myIsTransferRequest = false;

    /*
     * Check if category specified is by the email address.
     */
    private boolean myIsEmailCategory = false;
    private String  assigneeList;
    private String  ccList;

    // Current rr-volunteer.
    private String curVolunteer;

    // Default values of standard types.
    private Type defCategory;

    // Default Due Date.
    private Timestamp defDueDate;

    // Default mail notification values.
    private int     defNotify;
    private boolean defNotifyLoggers;
    private Type    defOffice;
    private Type    defRequestType;
    private Type    defSeverity;
    private Type    defStatus;

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
    private int                    myDescriptionContentType;
    private Timestamp              myDueDate;

    // API Exception that will be built during the process.
    private APIException     myException;
    private ArrayList<Field> myExtFieldList;

    // These into requests_ex table.
    private Hashtable<Field, RequestEx> myExtendedFields;

    // Field table for this business area.
    private Hashtable<String, Field> myFieldTable;
    private String                   myHeaderDescription;
    private boolean                  myIsPrivate;
    private Timestamp                myLoggedDate;

    // Columns that go into request-related tables.
    // These into request_users table.
    private ArrayList<RequestUser> myLoggers;
    private int                    myMaxActionId;
    private String                 myMemo;
    private int                    myNotify;
    private boolean                myNotifyLoggers;
    private Type                   myOffice;
    private int                    myParentId;

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
    private String                 mySummary;
    private int	                   mySummaryContentType;
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
    
    // To store the context path of the webapps, so that it is easy to give proper links to various resources.
    private String				   myContext = null; 
    
    //This holds the version at which this request is going to be added.. if its zero,then it means
    // that version for this request is not known yet..
    private int verNum;
    
    //This holds the unique TVN Name for any request
    private String tvnName;
    
    
    
    //~--- constructors -------------------------------------------------------

    /**
     * Constructor.
     */
    public AddRequest() {

        // Set the acceptable level of exceptions to INFO by default.
        myLevel = PERROR;

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
    public AddRequest(int aLevel) throws TBitsException {
    	this();
        if ((aLevel < INFO) || (aLevel > SEVERE)) {
            StringBuilder message = new StringBuilder();

            message.append("Acceptable levels of exceptions are:").append("\n - INFO\n - WARNING\n - PERROR\n - SEVERE\n");

            throw new TBitsException(message.toString());
        }

        // Set the acceptable level of exceptions.
        myLevel = aLevel;
     }

    public void setSource(int source)
    {
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

	/**
	 * This calls {@link AddRequest#addRequest(Connection, TBitsResourceManager, Hashtable)} 
	 * after creating an instance of {@link TBitsResourceManager} and {@link Connection}
	 * @param aParamTable
	 * @return
	 * @throws APIException
	 */
	public Request addRequest(Hashtable<String, String> aParamTable) throws APIException {
		Connection conn = null;
		Request req = null;
		
		TBitsResourceManager tbitsResMgr = new TBitsResourceManager() ;
		
		try {
			conn = DataSourcePool.getConnection();
			conn.setAutoCommit(false);
			
			req = addRequest(conn, tbitsResMgr, aParamTable);
			
			conn.commit();
			//conn.close() ;			
			tbitsResMgr.commit();			
		} catch (APIException apiException) {
			try {
				tbitsResMgr.rollback() ;
				conn.rollback();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			throw apiException;
		} catch (SQLException e) {
			try {
				tbitsResMgr.rollback() ;
				if(conn != null)
					conn.rollback();
			} catch (SQLException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			APIException apie = new APIException();
			apie.addException(new TBitsException(
					"Unable to get connection to the database", e));
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
	 /**
     * This method is used to add request to the database.
     *
     * @param aParamTable Table of (key, value) pairs.
     *
     * @return Request object after successful insertion into the database.
     *
     * @exception APIException List of exceptions occurred during processing.
	 * @throws TBitsException 
     */
	 public Request addRequest(Connection connection,TBitsResourceManager tbitsResMgr, Hashtable<String, String> aParamTable) throws APIException {
    	
    	long start = System.currentTimeMillis();
    	
    	initializeRequestObject(aParamTable);
    	
        //
        // Call the method that reads the values of fixed fields from the
        // param table. This throws APIException only in SEVERE cases.
        // So, lets not catch it as we cannot continue in any of those cases.
        //
        readFixedFieldValues(connection,aParamTable);

    	//
        // Now validate the values against the regular expressions
        // 
        /*performRegExChecks(aParamTable,true,myUser);*/
        
        //
        // Make sure that the fields' uniqueness is honored
        // putting this code snnipt after the rule execution
       // performUniquenessChecks(connection, aParamTable, false, mySystemId, myRequestId);
        
    
        //Read the version information from paramsTable if present
         readVersion(aParamTable);
        
        //read the updateVersion information
       // readUpdateVersion(aParamTable);
        
        //Read the TVN Name if present
        readTVNName(aParamTable);
        //
        // Now, Call the method that reads the values of Extended fields from
        // the param table. This throws APIException only in SEVERE cases.
        // So, lets not catch it as we cannot continue in any of those cases.
        //
        readExtendedFieldValues(aParamTable);

        // Process the values obtained for users lists and build the
        // corresponding user lists.
        processUserLists(aParamTable);

        //
        // Perform the integrity checks which encompasses
        // - Logger list cannot be empty.
        // - Due date cannot be earlier than logged date.
        // - When adding a request without view on is_private,
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
        performPermissionChecks();

        // Now, Put all the attributes together and build the Request Object.
        try {
			buildRequestObject();
		} catch (TBitsException e5) {			
			LOG.severe("",(e5));
			myException.addException(e5,SEVERE);
		}

        // Check if we have any APP-Generate dependencies to be executed.
        checkDependencies();

        // Run the request object through workflow rules validator. XML Rules
        runWorkflowRules();

        //Run the plug-in rules. Java Rules
        try
        {	
        	RuleFactory.runPreRules(connection, myBusinessArea, null, myRequest, this.ourSource, myUser,true);
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
        
        /**
         * fire the preRequest commit event
         */
        IAddPreEvent prce = new AddPreEvent(connection, myBusinessArea, null, myRequest, this.ourSource, myUser, true ,tbitsResMgr);
        try {
			EventManager.getInstance().fireEvent(prce);
		} catch (EventFailureException e4) {
			myException.addException(new TBitsException(e4),SEVERE);
			throw myException;
		};
        
        performUniquenessChecks(connection,false,myRequest);
        performRegExChecks(myRequest);
        
        // There might be some changes after running workflow rules.
        // Update the locals.
        // Nitiraj : Why do we need to update the locals ?????
        updateLocals();

        // NITI msg : this should not be required if the myRequest object is directly changed in the prerules
        // passing the myExtendedFields into prerules for changes and then again setting the values in myRequest object
        // is unnecessary and confusing, moreover it is complicated to change the myExtendedFields hashtable
        // There might be some changes to the extended fields
//        try {
//			setExtendedFields();
//		} catch (TBitsException e4) {			
//			LOG.info("",(e4));
//			myException.addException(e4,SEVERE);
//		}

        //Now generate the header description.
        generateHeaderDesc();

        // Now, the request object is ready to be inserted.
        try {
			insertRequest(connection);
		} catch (TBitsException e3) {
			LOG.severe("",(e3));
			myException.addException(e3);
			throw myException;
		}
        
        //Steps for processing attachments
        //Find the differences betweeen current and previous
        //Insert newly added and update the existing
        //Update request attachments
		myAttachments = myRequest.getAttachments() ;
         if (myAttachments != null) {
        	
        	 int     permission = 0;
             Integer temp       = myPermTable.get(Field.ATTACHMENTS);

             if (temp != null) {
                 permission = temp;
             } else {
                 permission = 0;
             }

             if ((permission & Permission.ADD) == 0) {
				myException.addException(new TBitsException(noPermission(
						Field.ATTACHMENTS, "ADD", myBusinessArea.getSystemPrefix(),  myUser.getUserLogin())), PERROR);
				throw myException;
			}
			
			try {
				for (AttachmentInfo ai : myAttachments) {
					ai.requestFileId = APIUtil.getAndCreateRequestFileId(
							myRequest.getSystemId(), myRequest.getRequestId());
				}
				myRequest.setAttachments(myAttachments);
				Request.updateAttachments(connection, myRequest.getSystemId(),
						myRequest.getRequestId(), myRequest.getMaxActionId(),
						AttachmentInfo.toJson(myRequest.getAttachments()));
			} catch (DatabaseException de) {
				myException.addException(new TBitsException(de.toString()),
						FATAL);
				LOG.warn("",(de));
				throw myException;
			} catch (Exception e) {
				LOG.warn("",(e));
				myException.addException(new TBitsException(e.toString()),
						FATAL);
				throw myException;
			}
        }
        
        //Update the other attachment type fields
		
		//Extract the attachment type fields and corresponding attinfo objects
        ArrayList<Field> allFields = null;
        ArrayList<Field> attFields = new ArrayList<Field>();
        try {
			allFields = Field.lookupBySystemId(mySystemId);
	       
	        if(allFields != null)
	        {
		        for(Field f:allFields)
		        {
		        	if(f.getIsExtended() && (f.getDataTypeId() == DataType.ATTACHMENTS))
		        	{
		        		attFields.add(f);
		        	}
		        }
	        }

		} catch (DatabaseException e2) {
			e2.printStackTrace();
			myException.addException(new TBitsException(e2));
			throw myException;
		}
		
		//generate the request file id and update the attachments
		try {
			for (Field f : attFields) {
				Collection<AttachmentInfo> exAttachments = (Collection<AttachmentInfo>) myRequest.getObject(f);
				if (exAttachments == null)
					continue;

				String key = f.getName();
				int permission = 0;
				Integer temp = myPermTable.get(key);

				if (temp != null) {
					permission = temp;
				} else {
					permission = 0;
				}

				if ((permission & Permission.ADD) == 0) {
					myException.addException(new TBitsException(noPermission(
							f.getDisplayName(), "Change", myBusinessArea.getSystemPrefix(),  myUser.getUserLogin())), PERROR);
					throw myException;
				}

				for (AttachmentInfo ai : exAttachments) {
					ai.requestFileId = APIUtil.getAndCreateRequestFileId(
							myRequest.getSystemId(), myRequest.getRequestId());
				}
				Request.updateAttachmentsExt(connection, myRequest.getSystemId(), myRequest.getRequestId(), f.getFieldId(), myRequest.getMaxActionId(), AttachmentInfo.toJson(exAttachments));
				myRequest.setObject(f, exAttachments);
			}
		} catch (DatabaseException de) {
			myException.addException(new TBitsException(de.toString()), FATAL);
			LOG.warn("",(de));
			throw myException;
		} catch (Exception e) {
			LOG.warn("",(e));
			myException.addException(new TBitsException(e.toString()), FATAL);
			throw myException;
		}
		
		//Added By Abhishek, increment the version
        try {
        	ArrayList<FileAction> fileActions = new ArrayList<FileAction>();
        	if(myAttachments != null){
        		for(AttachmentInfo att:myAttachments)
        		{
            		fileActions.add(new FileAction(att, WebdavConstants.FILE_ADDED, 22));
            	}
        	}
        	
        	for (Field f : attFields) {
        		Collection<AttachmentInfo> attInfos = (Collection<AttachmentInfo>) myRequest.getObject(f);
        		if(attInfos != null){
        			for (AttachmentInfo ai : (Collection<AttachmentInfo>) myRequest.getObject(f)) {
    					fileActions.add(new FileAction(ai, WebdavConstants.FILE_ADDED, f.getFieldId()));
    				}
        		}
			}
        	
        	String from  = aParamTable.get("from");
    		boolean fromWeb = true;
    		if(from != null)
    			fromWeb = !from.equals("client");
    			
        	int version = WebdavUtil.updateVersion(connection, myRequest, fileActions, true,tvnName,verNum,fromWeb);
        	if(version == -1)
    			throw new TBitsException("TVN Name already present");
        	myRequest.setVersionNum(version);
        	
		} catch (TBitsException e1) {
			myException.addException(e1);
			throw myException;
		}
		
        //
        // insert Related requests entries if any
        // TODO : Nitiraj : to be discarded.
        try {
            myRequest = APIUtil.insertRelatedRequests(connection, myRequest, myBaMailIds, myRelatedRequests);
        } catch (Exception e) {
        	String msg = "Unable to update the related requests. \nUserId: " + myRequest.getUserId() + "\nBusinessArea: " + myBusinessArea.getSystemPrefix() + "\nRequestId: " + myRequest.getRequestId() ;
            LOG.severe(msg + "\n\n"
                       + "",(e));
            myException.addException(new TBitsException(msg, e));
            throw myException;
        }

    //    performRegExChecks(myRequest);
        //Running the post process in rules
        try
        {
        	RuleFactory.runPostRules(connection, myBusinessArea, null, myRequest, this.ourSource, myUser, true);
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
        IAddPostEvent postEvent = new AddPostEvent(connection, myBusinessArea, null, myRequest, this.ourSource, myUser, true, tbitsResMgr);
        try {
			EventManager.getInstance().fireEvent(postEvent);
		} catch (EventFailureException e4) {
			myException.addException(new TBitsException(e4),SEVERE);
			throw myException;
		};
        //
        // Register this action as read for the appender, if status is
        // not closed.
        //
        try {

            /*
             * Mark this request as read irrespective of the status.
             */
            UserReadAction.registerUserReadAction(connection, myRequest.getSystemId(), myRequest.getRequestId(), myRequest.getMaxActionId(), myRequest.getUserId());
        } catch (DatabaseException de) {
            myException.addException(new TBitsException(de.toString()), WARNING);
            LOG.warn("",(de));
        }

        long end = System.currentTimeMillis();

        LOG.info("Total Time taken to add request " + myBusinessArea.getSystemPrefix() + "#" + myRequest.getRequestId() + ": " + (end - start) + " mecs");

        //
        // Mailing component:
        // If notify is true, then based on the source of invocation,
        // invoke the mailer component.
        //

        if (myRequest.getNotify()) {
//            if (ourSource == SOURCE_WEB) {
//                new Thread() {
//                    public void run() {
                        sendMail(tbitsResMgr.getMailResourceManager());
//                    }
//               }.start();
//            } else {
//                sendMail();
//            }
        }
        System.out.println("Finished sending mail.");
        
        // queue the request for indexing  // added by Nitiraj
        //  String aSysPrefix, int aSystemId, int aRequestId,String aPrimaryIndexLocation
        try
        {
        tbitsResMgr.getIndexerResourceManager().queueForIndexing(myBusinessArea.getSystemPrefix(),
        												mySystemId, myRequestId, Configuration.findAbsolutePath(PropertiesHandler.getProperty(KEY_INDEXDIR)));
        }
        catch(Throwable exp)
        {
        	LOG.warn("Unable to queue for indexing", exp);
        }
        if(TBitsHelper.isSMSEnabled())
        {
//	        if (ourSource == SOURCE_WEB) {
	        	new Thread() {
	        		public void run() {
	        			try {
	        				new SMS().sendSMS(myRequest);
	        			} catch (SQLException e) {
	        				e.printStackTrace();
	        			}
	        			catch(Exception e)
	        			{
	        				e.printStackTrace();
	        			}
	        		}
	        	}.start();
//	        } else {
//	        	try {
//	        		new SMS().sendSMS(myRequest);
//	        	} catch (SQLException e) {
//	        		e.printStackTrace();
//	        	}
//	        	catch(Exception e)
//    			{
//    				e.printStackTrace();
//    			}
//	        }
        }
        else
        {
        	LOG.debug("SMS is disabled.");
        }
        return myRequest;
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

/*	private void performRegExChecks(Hashtable<String,String> paramTable, Boolean isAddRequest,User user) throws APIException {
    	try
    	{
    		APIUtil.performRegExChecksOnFieldValues(paramTable, myFieldTable,true,myUser);
    	}
    	catch(TBitsException tex)
    	{
    		myException.addException(tex, PERROR);
    		throw myException;
    	}
    }*/
    
	private void performRegExChecks(Request myRequest) throws APIException {
    		
    		try {
				APIUtil.performRegExpChecksOnFieldValues(myRequest);
			} catch (TBitsException e) {
				myException.addException(e, PERROR);
				throw myException;
			}
    	
    }
   

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
    
    private void readTVNName(Hashtable<String, String> paramTable) {
    	if(null == paramTable) {
    		tvnName = null;
    		return;
    	}
    	
    	tvnName = paramTable.get(WebdavConstants.TVN_NAME);
    }

	/**
     * This method builds the Request object from the attributes that are
     * obtained from the param table passed to the addRequest method.
	 * @throws TBitsException 
	 * @throws TBitsException 
     */




    private void buildRequestObject() throws TBitsException{       
        myRequest.setRequestId(myRequestId);
        myRequest.setCategoryId(myCategory);
        myRequest.setStatusId(myStatus);
        myRequest.setSeverityId(mySeverity);
        myRequest.setRequestTypeId(myRequestType);
        myDescription = LinkFormatter.replaceHrefWithSmartLinks(myDescription);
        myRequest.setRelatedRequests(myRelatedRequests);
        
        try {
			String html = WebUtil.prepareValidHtml(myDescription);
			myDescription = html;
		} catch (IOException e) {
			e.printStackTrace();
			LOG.error("Could not prepare valid html for description", e);
		}

        if (mySummary != null) {
            mySummary = LinkFormatter.replaceHrefWithSmartLinks(mySummary);
            try {
    			String html = WebUtil.prepareValidHtml(mySummary);
    			mySummary = html;
    		} catch (IOException e) {
    			e.printStackTrace();
    			LOG.error("Could not prepare valid html for summary", e);
    		}
        }

        myRequest.setSubject(mySubject);
//        myRequest.setDescription(myDescription);
//        myRequest.setDescriptionContentType(myDescriptionContentType);
        myRequest.setDescription(new TextDataType(myDescription,myDescriptionContentType));
        myRequest.setIsPrivate(myIsPrivate);
        myRequest.setParentRequestId(myParentId);
        myRequest.setUserId(myUser.getUserId());
        myRequest.setMaxActionId(myMaxActionId);
        myRequest.setDueDate(myDueDate);
        myRequest.setLoggedDate(myLoggedDate);
        myRequest.setLastUpdatedDate(myUpdatedDate);

        // Header description is not yet formed.
        // myRequest.setHeaderDescription(myHeaderDescription);
        // Attachments are yet to be processed. So insert empty string.
        myRequest.setAttachments(myAttachments);
//        myRequest.setSummary(mySummary);
//        myRequest.setSummaryContentType(mySummaryContentType);
        myRequest.setSummary(new TextDataType(mySummary,mySummaryContentType));
        myRequest.setMemo(myMemo);
        myRequest.setAppendInterface(myAppendInterface);
        myRequest.setNotify((myNotify != 0 ));
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

        // Set the extended fields
        try {
			setExtendedFields();
		} catch (TBitsException e) {			
			LOG.info("",(e));
			throw e;
		}
    }

    private void checkAppGenerateDependency(Dependency dep) throws Exception {

        /*
         * 1. Get the dependency configuration.
         * 2. Get the Input attributes required for generation and prepare
         *    input map.
         * 3. Get the resource and realize it.
         */

        // Step 1
        DependencyConfig dconfig = dep.getDepConfigObject();

        // Step 2
        boolean                   throwError           = dconfig.getThrowError();
        String                    errorMessage         = dconfig.getErrorMessage();
        Hashtable<String, String> inputAttrMap         = dconfig.getInputMap();
        Hashtable<String, String> outputAttrMap        = dconfig.getOutputMap();
        Hashtable<String, Object> inputValueMap        = new Hashtable<String, Object>();
        Enumeration<String>       reqFieldList         = inputAttrMap.keys();
        boolean                   realizeResource      = true;
        int                       emptyStringAttrCount = 0;
        int                       totalAttrCount       = inputAttrMap.size();

        while (reqFieldList.hasMoreElements()) {

            // Get the request field name and its value as an object.
            String reqFieldName  = reqFieldList.nextElement();
            Object reqFieldValue = myRequest.getObject(reqFieldName);

            /*
             * If the value is not null, then put the value in the input map
             * where key is the resource attr name.
             */
            if (reqFieldValue != null) {
                String resAttrName = inputAttrMap.get(reqFieldName);

                inputValueMap.put(resAttrName, reqFieldValue);

                /*
                 * If this is a string check if it is not empty.
                 */
                if (reqFieldValue instanceof String) {
                    if (reqFieldValue.toString().trim().equals("") == true) {
                        emptyStringAttrCount += 1;
                    }
                }
            }
        }

        /*
         * If all the input attributes are strings and the input values of all
         * these attributes are empty strings, then there is no need to proceed
         * with the process of realizing the resource.
         */
        if (emptyStringAttrCount == totalAttrCount) {
            LOG.info("All the input attributes are strings and the values " + "are empty. Returning from here without realizing the" + " resource.");

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
                        reqEx = prepareReqEx(reqField, resAttrValue.getStringValue(), true);

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
        int                    systemId     = mySystemId;
        int                    fieldId      = 0;
        int                    typeId       = 0;
        int                    userId       = 0;

        for (RequestUser reqUser : myAssignees) {
            TypeUser typeUser = null;
            User     user     = null;

            // get the user id from this requestuser object.
            try {
                user = reqUser.getUser();
            } catch (DatabaseException de) {
                myException.addException(new TBitsException(de.toString()), SEVERE);
            }

            userId = user.getUserId();

            // Check if the user is a valid assignee by category.
            fieldId = myCategory.getFieldId();
            typeId  = myCategory.getTypeId();

            try {
                typeUser = TypeUser.lookupBySystemIdAndFieldIdAndTypeIdAndUserId(systemId, fieldId, typeId, userId);
            } catch (DatabaseException de) {
                myException.addException(new TBitsException(de.toString()), SEVERE);
            }

            if ((typeUser != null) && (typeUser.getUserTypeId() == UserType.ASSIGNEE)) {

                //
                // User is a valid type-assignee based on the current
                // category. So continue with the next one.
                //
                continue;
            } else {
                LOG.debug("User is not a valid category assignee.");
            }

            // Check if the user is a valid assignee by status.
            fieldId = myStatus.getFieldId();
            typeId  = myStatus.getTypeId();

            try {
                typeUser = TypeUser.lookupBySystemIdAndFieldIdAndTypeIdAndUserId(systemId, fieldId, typeId, userId);
            } catch (DatabaseException de) {
                myException.addException(new TBitsException(de.toString()), SEVERE);
            }

            if ((typeUser != null) && (typeUser.getUserTypeId() == UserType.ASSIGNEE)) {

                //
                // User is a valid type-assignee based on the current
                // status. So continue with the next one.
                //
                continue;
            } else {
                LOG.debug("User is not a valid status assignee.");
            }

            // Check if the user is a valid assignee by severity.
            fieldId = mySeverity.getFieldId();
            typeId  = mySeverity.getTypeId();

            try {
                typeUser = TypeUser.lookupBySystemIdAndFieldIdAndTypeIdAndUserId(systemId, fieldId, typeId, userId);
            } catch (DatabaseException de) {
                myException.addException(new TBitsException(de.toString()), SEVERE);
            }

            if ((typeUser != null) && (typeUser.getUserTypeId() == UserType.ASSIGNEE)) {

                //
                // User is a valid type-assignee based on the current
                // severity. So continue with the next one.
                //
                continue;
            } else {
                LOG.debug("User is not a valid severity assignee.");
            }

            // Check if the user is a valid assignee by request type.
            fieldId = myRequestType.getFieldId();
            typeId  = myRequestType.getTypeId();

            try {
                typeUser = TypeUser.lookupBySystemIdAndFieldIdAndTypeIdAndUserId(systemId, fieldId, typeId, userId);
            } catch (DatabaseException de) {
                myException.addException(new TBitsException(de.toString()), SEVERE);
            }

            if ((typeUser != null) && (typeUser.getUserTypeId() == UserType.ASSIGNEE)) {

                //
                // User is a valid type-assignee based on the current
                // request type. So continue with the next one.
                //
                continue;
            } else {
                LOG.debug("User is not a valid request type assignee.");
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
     * Checks the fieldName against ruleValue for the operation specified by operation in a
     * in context of Type of a Field
     * @param fieldName
     * @param ruleValue
     * @param operator
     * @return false if a field is not available in myFieldTable or the condition returns false
     */
    private boolean compare(String fieldName, String ruleValue, Operator operator) {
        String fieldValue = myRequest.get(fieldName);
        return APIUtil.compareFieldValueAndRuleValue(myFieldTable, fieldName, fieldValue, ruleValue, operator);
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

        // Check if this is a call from an email.
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

        myException = new APIException();
    }

    /**
     * This method generates the header description.
     * @throws TBitsException 
     */
    private void generateHeaderDesc()  {
    	String header = myRequest.getHeaderDescription();
    	if(header == null)
    		header = "";
        StringBuilder headerDesc = new StringBuilder(header);

        // We proceed in the order of fields.
        Field field = null;

        // Assignees.
        field = myFieldTable.get(Field.ASSIGNEE);

        // Auto Assignee.
        if ((curVolunteer != null) && (curVolunteer.trim().equals("") == false)) {
            headerDesc.append(field.getName()).append("##").append(field.getFieldId()).append("##[ Auto-assigned to '").append(curVolunteer).append("' ]\n");
        }

        // Track Invalid Assignee
        field = myFieldTable.get(Field.ASSIGNEE);

        String value = APIUtil.toLoginList(myRequest.getAssignees(), ", ");

        headerDesc.append(getTrackRecord(field, value));

        // Track Inavlid Loggers
        field = myFieldTable.get(Field.LOGGER);
        value = APIUtil.toLoginList(myRequest.getLoggers(), ", ");
        headerDesc.append(getTrackRecord(field, value));

        // Subscribers.
        field = myFieldTable.get(Field.SUBSCRIBER);
        value = APIUtil.toLoginList(myRequest.getSubscribers(), ", ");
        headerDesc.append(getTrackRecord(field, value));
        
        
        //UserTypes
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
	        	String value1 = "";
	        	value1 = APIUtil.toLoginList(myRequest.getExUserType(f), ", ");
				headerDesc.append(getTrackRecord(f, value1));		        
	        }
		}
//        Enumeration<Integer> uKeys = myRequest.userTypeMap.keys();
//        while(uKeys.hasMoreElements()){
//        int fieldId=uKeys.nextElement();
//          try {
//			Field f = Field.lookupBySystemIdAndFieldId(mySystemId,fieldId);
//			  value = APIUtil.toLoginList(myRequest.userTypeMap.get(fieldId), " ");
//		        headerDesc.append(getTrackRecord(f, value));
//		} catch (DatabaseException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//        }
       
      

        // Is Private.
        field = myFieldTable.get(Field.IS_PRIVATE);

        if ((field != null) && (field.getTrackingOption() != 0) && (myRequest.getIsPrivate() == true)) {
            headerDesc.append(field.getName()).append("##").append(field.getFieldId()).append("##[ Marked private ]\n");
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
                    value = APIUtil.toLoginList(myRequest.getTos(), ", ");
                    headerDesc.append(getTrackRecord(field, value));
                }

                // Cc.
                field = myFieldTable.get(Field.CC);

                if ((field != null) && (field.getTrackingOption() != 0)) {
                    value = APIUtil.toLoginList(myRequest.getCcs(), ", ");
                    headerDesc.append(getTrackRecord(field, value));
                }

                field = myFieldTable.get(Field.NOTIFY_LOGGERS);

                if ((field != null) && (field.getTrackingOption() != 0) && (myRequest.getNotifyLoggers() == false)) {
                    headerDesc.append(field.getName()).append("##").append(field.getFieldId());
                    headerDesc.append("##[ Mail not sent to loggers ]\n");
                }
            }
        }

       try {
		myUser = User.lookupByUserId(myRequest.getUserId());
	} catch (DatabaseException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
        /*
         * Check if this a transferred request.
         */
        if ((myIsTransferRequest == true) && (myTransferredRequest != null) && (myTransferredRequest.trim().equals("") == false)) {

            // Check if TR_SRC_REQUEST is present.
            headerDesc.append("[ Request transferred from ").append(myTransferredRequest).append(" by '").append(myUser.getUserLogin().replace(".transbittech.com", "")).append("' ]\n");
        }

        try {

            // Check if logger is the user or someone else.
            field = myFieldTable.get(Field.LOGGER);
            myLoggers = new ArrayList<RequestUser>(myRequest.getLoggers());
            if ((field != null) && ((myRequest.getLoggers().size() != 1) || (myLoggers.get(0).getUser().equals(myUser) == false))) {
                headerDesc.append(field.getName()).append("##").append(field.getFieldId()).append("##[ Logged by '").append(myUser.getUserLogin().replace(".transbittech.com",
                        "")).append("' on behalf of '").append(APIUtil.toLoginList(myLoggers)).append("' ]\n");
            } else {

                //
                // Log message if unauthorized user logged in a private
                // Ba/Type.
                //
                if (myUnauthorized == true) {
                    headerDesc.append(field.getName()).append("##").append(field.getFieldId()).append("##[ ").append(Messages.getMessage("UNAUTHORIZED_LOGGER")).append(" ]\n");
                } else {
                    headerDesc.append(field.getName()).append("##").append(field.getFieldId()).append("##[ Logged ]\n");
                }
            }
        } catch (DatabaseException de) {
            myException.addException(new TBitsException(de.toString()), WARNING);
        }

        headerDesc.append(myRuleWarnings);
        myHeaderDescription = headerDesc.toString();
        myRequest.setHeaderDescription(myHeaderDescription);
    }

    /**
     *
     *
     *
     */
    private void init() throws APIException {

        // Get the fields table for this business area.
        try {
            myFieldTable = Field.getFieldsTableBySystemId(mySystemId);
        } catch (DatabaseException de) {
            myException.addException(new TBitsException(de.toString()), FATAL);

            // We cannot continue. So throw the exception here itself.
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
            LOG.error(de.toString());
            myException.addException(new TBitsException(de.toString()), FATAL);

            throw myException;
        }

        try {
            myPermTable = RolePermission.getPermissionsBySystemIdAndUserId(mySystemId, myUser.getUserId());
        } catch (DatabaseException de) {
            myException.addException(new TBitsException(de.toString()), SEVERE);

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
     * This method calls the insert method with this request object.
     * @throws TBitsException 
     */
    private void insertRequest(Connection connection) throws APIException, TBitsException {
        try {
            Request.insert(myRequest, connection);
            
            myRequestId = myRequest.getRequestId();

            //
            // For subrequest, load request object from db to get information
            // about sibling requests.
            // Nitiraj : What is this logic about ? ?
            try {
                if (myRequest.getParentRequestId() > 0) {
                	//TODO: needs to be done in the same connection
                    myRequest = Request.lookupBySystemIdAndRequestId(connection, myRequest.getSystemId(), myRequestId);
                }
            } catch (DatabaseException e) {
                LOG.warn("",(e));
            }

            //
            // Update BusinessArea MaxRequestId in the Mapper
            //
            // Niti msg : this assignment is not correct. As if requests are being added in parallel 
            // then there is no guarantee that this request will have the maximum request
            myBusinessArea.setMaxRequestId(myRequestId);
            Mapper.updateBA(myBusinessArea);

            StringBuilder message = new StringBuilder();

            message.append("Request Inserted [ ").append(myBusinessArea.getDisplayName()).append(", ").append(myRequestId).append(" ]");

            // LOG.debug(message.toString());
            // Check if we need to update the volunteer
            if ((nextVolunteer != null) && (nextVolunteer.equals("") == false)) {
                try {
                    User tmp = User.lookupByUserLogin(nextVolunteer);

                    if ((tmp != null) && (myCategory != null)) {
                        TypeUser.updateNextVolunteer(mySystemId, myCategory.getFieldId(), myCategory.getTypeId(), tmp.getUserId());
                    }
                } catch (DatabaseException de) {
                    LOG.error("",(de));
                }
            }
        } catch (DatabaseException de) {
            myException.addException(new TBitsException(de), FATAL);
        }

        if (myException.getExceptionCount(myLevel) > 0) {
            throw myException;
        }

        return;
    }

    /**
     * The main method.
     */
    public static void main(String arg[]) throws Exception {
    	
//    	String content = "cewc<script></script> ecce <script language=javascript>alert(\"swqs');</script> eqwd";
//    	
//    	content = content.replaceAll(regex, "");
//    	
//    	System.out.println("Content : " + content);
    	
        //String fileName = "addrequest.txt";

//        if (arg.length != 1) {
//            System.out.println("Usage: AddRequest <field_name1>=<field_value1>[,<field_name2>=<field_value2>...]");
//        }
//
//        StringTokenizer st = new StringTokenizer(arg[0], ",");
//        Hashtable<String, String> paramTable = new Hashtable<String, String>();
//
//        while (st.hasMoreTokens()) {
//            String          str = st.nextToken();
//            StringTokenizer ist = new StringTokenizer(str, ":=");
//
//            try {
//                String key = ist.nextToken().trim();
//                String val = ist.nextToken().trim();
//
//                paramTable.put(key, val);
//            } catch (Exception e) {
//                LOG.warn("",(e));
//            }
//        }
//    	java.io.RandomAccessFile dataFile = new java.io.RandomAccessFile("/Users/sandeepgiri/dataFile1.txt","r") ;
//    	String sub = null ;
//    	String desc = null ;
//    	String rootID = "root" ;
//    	String sysID = BusinessArea.lookupBySystemPrefix("tBits").getSystemId() + "" ;
//    	int i = 0 ;
//    	
//    	while((null != (sub = dataFile.readLine())) && (null != (desc = dataFile.readLine())))
//    	{
//    		i++;
//    		try
//    		{
//	    		// subject,description                 // logger,assignee, 
////	    		String [] values = line.split(",") ;    		
//		    	Hashtable<String,String> paramTable = new Hashtable<String,String>() ;
////		    	paramTable.put(Field.LOGGER, values[0]) ;
////		    	paramTable.put(Field.ASSIGNEE, values[1]) ;
//		    	paramTable.put(Field.SUBJECT, sub) ;
//		    	paramTable.put(Field.DESCRIPTION, desc) ;
//		    	paramTable.put(Field.USER, rootID ) ;
//		        paramTable.put(Field.BUSINESS_AREA, sysID ) ;
//		        //paramTable.put("testatt", "");
//		        
//		        AddRequest app     = new AddRequest();
//		        app.setSource(SOURCE_CMDLINE);
//		        Request    request = app.addRequest(paramTable);
//		        System.out.println(request.myMapFieldToValues);
//    		}
//    		catch(Exception e )
//    		{
//    			e.printStackTrace() ;
//    			System.out.println("Exception while adding req # " + i );
//    		} catch (APIException e) {				
//				e.printStackTrace();
//				System.out.println("APIException while adding req # " + i );
//			}
//    	}
//    	long totalTimeTaken = 0;
//    	for(int i=0; i<200;i++)
//    	{
//    		long start = Calendar.getInstance().getTimeInMillis();
//	    	AddRequest ar = new AddRequest();
//	    	Hashtable<String, String> p = new Hashtable<String, String>();
//	    	p.put(Field.BUSINESS_AREA, "tbits");
//	    	p.put(Field.USER, "root");
//	    	
//	    	p.put(Field.SUBJECT, "testing the world");
//	    	p.put(Field.DESCRIPTION, "testing the world");
//	    	
//	    	p.put("testvarchar", "testing the world");
//	    	p.put("testtext", "testing the world");
//	    	p.put("testdate", "2001-01-01 01:01:01");
//	    	p.put("testbit", "true");
//	    	p.put("testtime", "2001-01-01 01:01:01");
//	    	p.put("testdatetime", "2001-01-01 01:01:01");
//	    	p.put("testint", "123");
//	    	p.put("testreal", "123.123");
//	    	
//			try {
//				ar.addRequest(p);
//			} catch (APIException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//    		long end = Calendar.getInstance().getTimeInMillis();
//    		long diff = (start-end);
//    		System.out.println("TimeTakenToAdd: " + diff);
//    		totalTimeTaken += diff;
//    	}
//    	System.out.println("TotalTimeTakenToAdd: " + totalTimeTaken);
//    	System.exit(0);
    	
    	Connection con = null;
    	try
    	{
    		con = DataSourcePool.getConnection();
    		con.setAutoCommit(false);
    		Hashtable<String,String> params = new Hashtable<String,String>();
    		params.put(Field.BUSINESS_AREA, "tbits");
    		params.put(Field.USER, "amit.saxena");
    		
    		params.put(Field.SUBJECT, "This is the parent");
    		params.put("intuniq", "1234");
    		params.put("realuniq", "987.34");
    		params.put("phone", "1234.1");
    		
    		
    		AddRequest ar = new AddRequest();
    		ar.setSource(TBitsConstants.SOURCE_CMDLINE);
    		
    		TBitsResourceManager tbitsResMgr = new TBitsResourceManager();
    		Request parent = ar.addRequest(con, tbitsResMgr, params); 
//    		System.out.println("Nitiraj :  first request id : " + parent.getRequestId());
    		
    		
//    		params.put(Field.PARENT_REQUEST_ID, parent.getRequestId()+"");
//    		params.put(Field.SUBJECT, "This is the child");
//    		Request child = ar.addRequest(con, tbitsResMgr, params);
    		
//    		System.out.println("Nitiraj : secmainmainond request id : " + child.getRequestId());
    		con.commit();
    		tbitsResMgr.commit();
    	}
    	catch(Exception e)
    	{
    		e.printStackTrace();
    	} catch (APIException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	finally
    	{
    		if( null != con && con.isClosed() == false )
    			con.close();
    	}
    	
    	System.out.println("Main finished.");
    }

    /**
     * @throws TBitsException 
     *
     *
     */
    private void makeUserAsLogger() throws TBitsException {
        myLoggers = new ArrayList<RequestUser>();

        RequestUser ru = null;
		try {
			ru = new RequestUser(mySystemId,myRequestId,myUser.getUserId(),1,false,Field.lookupBySystemIdAndFieldName(mySystemId, Field.LOGGER).getFieldId());
		} catch (DatabaseException e) {			
			LOG.info("",(e));
			throw new TBitsException(e.getDescription());
		}
        myLoggers.add(ru);
    }


    private String noPermission(String aField, String aPermission, String sysPrefix, String userLogin) {
        return Messages.getMessage("NO_PERMISSION", aField, aPermission, myBusinessArea.getSystemPrefix(),  myUser.getUserLogin());
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

        if ((myLoggers == null) || (myLoggers.size() == 0) || (defunctUsers != null)) 
        {
            // If loggers was intentionally empty , throw wmtpty list error
            // else if inactive users added, throw their list.
            //
            if ((defunctUsers == null) || defunctUsers.trim().equals("")) {
                myException.addException(new TBitsException(Messages.getMessage("LOGGER_MANDATORY")), SEVERE);
            } else {
                ArrayList<String> users = Utilities.toArrayList(defunctUsers);
                User              user  = null;

                for (String login : users) 
                {
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

            // Make user as logger to continue.
            try {
				makeUserAsLogger();
			} catch (TBitsException e) {
				LOG.warn("",(e));
			}
        }

        //
        // 2. Due date cannot be earlier than logged date.
        // If it is, we consider the old due date and register an exception in
        // the myExceptionList at severe level.
        //
//        if (myDueDate != null) {
//            if (myDueDate.getTime() < myLoggedDate.getTime()) {
//                myException.addException(new TBitsException(Messages.getMessage("PAST_DUE_DATE")), SEVERE);
//
//                // Check if the BA allows null due dates.
//                if (mySysConfig.getAllowNullDueDate() == true) {
//                    myDueDate = null;
//                } else {
//                    myDueDate = defDueDate;
//                }
//            }
//        }

        //
        // 3. If the user is adding a request without view on is_private
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
                if ((myBusinessArea.getIsPrivate() == true) || (myCategory.getIsPrivate() == true) || (myStatus.getIsPrivate() == true) || (mySeverity.getIsPrivate() == true)
                        || (myRequestType.getIsPrivate() == true)) {
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
     */
    private void performPermissionChecks() throws APIException {
        String  key        = "";
        int     permission = 0;
        Integer temp       = null;
        Field   field      = null;

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
        // Permission : Permission.ADD
        // Exemption  : If from email.
        key   = Field.REQUEST;
        field = myFieldTable.get(key);
        temp  = myPermTable.get(key);

        if (temp != null) {
            permission = temp;
        } else {
            permission = 0;
        }

        if ((permission & ADD) == 0) {
            myException.addException(new TBitsException(noPermission(field.getDisplayName(), "Add", myBusinessArea.getSystemPrefix(),  myUser.getUserLogin())), PERROR);
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

        if (((permission & ADD) == 0) && (myCategory.equals(defCategory) == false)) {

            /*
             *  Check if this category is a command-line option to
             *  mailParser.
             */
            if ((ourSource == SOURCE_EMAIL) && (myIsEmailCategory == true)) {

                // Ignore this permisssion check failure and continue.
                LOG.info(myCategory.getDisplayName() + " is considered " + "though the user does not have permission to change " + "category, as this is specified as a command line "
                         + "parameter to the mailparser.");
            } else {

                // throw an exception and consider the default category.
                myException.addException(new TBitsException(noPermission(field.getDisplayName(), "Add", myBusinessArea.getSystemPrefix(),  myUser.getUserLogin())), PERROR);

                // Take the default value.
                myCategory = defCategory;
            }
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

        if (((permission & ADD) == 0) && (myStatus.equals(defStatus) == false)) {
            myException.addException(new TBitsException(noPermission(field.getDisplayName(), "Add", myBusinessArea.getSystemPrefix(),  myUser.getUserLogin())), PERROR);

            // Take the default value.
            myStatus = defStatus;
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

        if (((permission & ADD) == 0) && (mySeverity.equals(defSeverity) == false)) {
            myException.addException(new TBitsException(noPermission(field.getDisplayName(), "Add", myBusinessArea.getSystemPrefix(),  myUser.getUserLogin())), PERROR);

            // Take the default value.
            mySeverity = defSeverity;
        }

        // 6. Request Type
        // Key        : Field.REQUEST_TYPE
        // Permission : Permission.CHANGE
        // Exemption  : If from email.
        key   = Field.REQUEST_TYPE;
        temp  = myPermTable.get(key);
        field = myFieldTable.get(key);

        if (temp != null) {
            permission = temp;
        } else {
            permission = 0;
        }

        if (((permission & ADD) == 0) && (myRequestType.equals(defRequestType) == false)) {
            myException.addException(new TBitsException(noPermission(field.getDisplayName(), "Add", myBusinessArea.getSystemPrefix(),  myUser.getUserLogin())), PERROR);

            // Take the default value.
            myRequestType = defRequestType;
        }

        // 7. Logger
        // Key        : Field.LOGGER;
        // Permission : Permission.ADD
        // Exemption  : If from email.
        key   = Field.LOGGER;
        temp  = myPermTable.get(key);
        field = myFieldTable.get(key);

        if (temp != null) {
            permission = temp;
        } else {
            permission = 0;
        }

        if ((permission & ADD) == 0) {
            try {

                /*
                 * User does not have change permission.
                 * If this is a transfer request then ignore.
                 * Otherwise Check if the logger list length is 1 and the only
                 * item in the list is the user.
                 */
                if (myIsTransferRequest == false) {
                    if ((myLoggers.size() != 1) || (myLoggers.get(0).getUser().equals(myUser) == false)) {
                        myException.addException(new TBitsException(noPermission(field.getDisplayName(), "Add", myBusinessArea.getSystemPrefix(),  myUser.getUserLogin())), PERROR);

                        // Consider the user as logger.
                        try {
							makeUserAsLogger();
						} catch (TBitsException e) 
						{
							LOG.warn("",(e));
						}
                    }
                }
            } catch (DatabaseException de) {

                // Consider the user as logger.
                try {
					makeUserAsLogger();
				} catch (TBitsException e) {
					LOG.warn("",(e));
				}
                myException.addException(new TBitsException(de.toString()), WARNING);
            }
        }

        // 8. Assignee
        // Key        : Field.ASSIGNEE
        // Permission : Permission.ADD
        // Exemption  : If from email.
        key   = Field.ASSIGNEE;
        field = myFieldTable.get(key);
        temp  = myPermTable.get(key);

        if (temp != null) {
            permission = temp;
        } else {
            permission = 0;
        }

        if ((permission & ADD) == 0) {
            if (myAssignees.size() > 0) {
                myException.addException(new TBitsException(noPermission(field.getDisplayName(), "Add", myBusinessArea.getSystemPrefix(),  myUser.getUserLogin())), PERROR);
                myAssignees = new ArrayList<RequestUser>();
            }
        }

        // 9. Subscriber
        // Key        : Field.SUBSCRIBER
        // Permission : Permission.ADD
        // Exemption  : If from email.
        key   = Field.SUBSCRIBER;
        field = myFieldTable.get(key);
        temp  = myPermTable.get(key);

        if (temp != null) {
            permission = temp;
        } else {
            permission = 0;
        }

        if ((permission & ADD) == 0) {
            if (mySubscribers.size() > 0) {
                myException.addException(new TBitsException(noPermission(field.getDisplayName(), "Add", myBusinessArea.getSystemPrefix(),  myUser.getUserLogin())), PERROR);
                mySubscribers = new ArrayList<RequestUser>();
            }
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

        if ((permission & ADD) == 0) {
            if (myTos.size() > 0) {
                myException.addException(new TBitsException(noPermission(field.getDisplayName(), "Add", myBusinessArea.getSystemPrefix(),  myUser.getUserLogin())), PERROR);
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

        if ((permission & ADD) == 0) {
            if (myCcs.size() > 0) {
                myException.addException(new TBitsException(noPermission(field.getDisplayName(), "Add", myBusinessArea.getSystemPrefix(),  myUser.getUserLogin())), PERROR);
                myCcs = new ArrayList<RequestUser>();
            }
        }

        // 12. Subject
        // Key        : Field.SUBJECT
        // Permission : Permission.ADD
        // Exemption  : If from email.
        key   = Field.SUBJECT;
        field = myFieldTable.get(key);
        temp  = myPermTable.get(key);

        if (temp != null) {
            permission = temp;
        } else {
            permission = 0;
        }

        if ((permission & ADD) == 0) {
            if (mySubject.equals("") == false) {
                myException.addException(new TBitsException(noPermission(field.getDisplayName(), "Add", myBusinessArea.getSystemPrefix(),  myUser.getUserLogin())), PERROR);

                // Lets not clear the subject
                // mySubject = "";
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

        if ((permission & ADD) == 0) {
            if (myDescription.equals("") == false) {
                myException.addException(new TBitsException(noPermission(field.getDisplayName(), "Add", myBusinessArea.getSystemPrefix(),  myUser.getUserLogin())), PERROR);

                // Lets not clear the description
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

        if ((permission & ADD) == 0 && (myIsPrivate == true)) {
            myException.addException(new TBitsException(noPermission(field.getDisplayName(), "Add", myBusinessArea.getSystemPrefix(),  myUser.getUserLogin())), PERROR);
            myIsPrivate = false;
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

        if ((permission & ADD) == 0 && (myParentId != 0)) {
            myException.addException(new TBitsException(noPermission(field.getDisplayName(), "Add", myBusinessArea.getSystemPrefix(),  myUser.getUserLogin())), PERROR);
            myParentId = 0;
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
        // Permission : Permission.CHANGE If BA does not allow null due dates,
        // Permission.ADD If BA allows null due dates.
        // Exemption  : If from email.
        key   = Field.DUE_DATE;
        field = myFieldTable.get(key);
        temp  = myPermTable.get(key);

        if (temp != null) {
            permission = temp;
        } else {
            permission = 0;
        }

        if ((permission & ADD) == 0) {

            // Check if user has specified anything different from the
            // default due date.
            if (((myDueDate != null) && (defDueDate == null)) || ((myDueDate != null) && (myDueDate.equals(defDueDate) == false))) {

                // User has no change permission. Still he specified a
                // value different from the default due date.
                // we throw an exception.
                myException.addException(new TBitsException(noPermission(field.getDisplayName(), "Add", myBusinessArea.getSystemPrefix(),  myUser.getUserLogin())), PERROR);
            }

            myDueDate = defDueDate;
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
        // Attachment field not considered here !
//        key   = Field.ATTACHMENTS;
//        field = myFieldTable.get(key);
//        temp  = myPermTable.get(key);
//
//        if (temp != null) {
//            permission = temp;
//        } else {
//            permission = 0;
//        }

        // Nitiraj msg : Comparing myAttachments.equals("") is incorrect as it is comparing for string match.
        // instead addition/updation of attachments in the request should be dealt in the end where we are 
        // updating the versions table by checking for 'A', 'M', 'D' attachment actions 
        // note addition of attachments is also possible in Update request.
        // check Add permission for attachment_action 'A'
        // check Change permission for attachment_action 'M' or 'D' !!
//        if ((permission & ADD) == 0 && (myAttachments.equals("") == false)) {
//            myException.addException(new TBitsException(noPermission(field.getDisplayName(), "Add")), PERROR);
//
//            // Lets not clear the attachments
//            // myAttachments = "";
//        }

        // 23. Summary
        // Key        : Field.SUMMARY
        // Permission : Permission.ADD
        // Exemption  : If from email.
        key   = Field.SUMMARY;
        field = myFieldTable.get(key);
        temp  = myPermTable.get(key);

        if (temp != null) {
            permission = temp;
        } else {
            permission = 0;
        }

        if ((permission & ADD) == 0 && (mySummary.equals("") == false)) {
            myException.addException(new TBitsException(noPermission(field.getDisplayName(), "Add", myBusinessArea.getSystemPrefix(),  myUser.getUserLogin())), PERROR);

            // Lets not clear the summary
            // mySummary = "";
        }

        // 24. Memo
        // Key        : Field.MEMO
        // Permission : Permission.ADD
        // Exemption  : If from email.
        key   = Field.MEMO;
        field = myFieldTable.get(key);
        temp  = myPermTable.get(key);

        if (temp != null) {
            permission = temp;
        } else {
            permission = 0;
        }

        if ((permission & ADD) == 0 && (myMemo.equals("") == false)) {
            myException.addException(new TBitsException(noPermission(field.getDisplayName(), "Add", myBusinessArea.getSystemPrefix(),  myUser.getUserLogin())), PERROR);

            // Lets not clear the memo
            // myMemo = "";
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

        if ((permission & ADD) == 0 && (myNotify != defNotify)) {
            myException.addException(new TBitsException(noPermission(field.getDisplayName(), "Add", myBusinessArea.getSystemPrefix(),  myUser.getUserLogin())), PERROR);
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

        if ((permission & ADD) == 0 && (myNotifyLoggers != defNotifyLoggers)) {
            myException.addException(new TBitsException(noPermission(field.getDisplayName(), "Add", myBusinessArea.getSystemPrefix(),  myUser.getUserLogin())), PERROR);
            myNotifyLoggers = defNotifyLoggers;
        }

        // 29. Office
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

        if (((permission & ADD) == 0) && (((myOffice != null) && (myOffice.equals(defOffice) == false)))) {
            myException.addException(new TBitsException(noPermission(field.getDisplayName(), "Add", myBusinessArea.getSystemPrefix(),  myUser.getUserLogin())), PERROR);

            // Take the default value.
            myOffice = defOffice;
        }

        // Check if there are any exceptions in the list above the acceptable
        // level.
        if (myException.getExceptionCount(myLevel) > 0) {
            throw myException;
        }

        return;
    }

    private RequestEx prepareReqEx(Field field, String value, boolean isFieldSpecified) {
        RequestEx reqEx = new RequestEx();

        reqEx.setSystemId(mySystemId);
        reqEx.setRequestId(myRequestId);
        reqEx.setFieldId(field.getFieldId());

        int dataTypeId = field.getDataTypeId();

        // Parse the value based on the data type.
        switch (dataTypeId) {
        case DataType.BOOLEAN : {
            boolean bValue = false;

            // Null/EmptyString/False/0/No -> false.
            // True/1 -> true.
            if ((value == null) || (value.trim().equals("") == true)) {
                bValue = false;
            } else if ((value.trim().equalsIgnoreCase("false") == true) || (value.trim().equalsIgnoreCase("0") == true)
            				||(value.trim().equalsIgnoreCase("no"))) {
                bValue = false;
            } else {
                bValue = true;
            }

            reqEx.setBitValue(bValue);
        }

        break;

        case DataType.DATE :
        case DataType.TIME :
        case DataType.DATETIME : {
            if ((value == null) || (value.trim().length() == 0)) {
                //reqEx.setDateTimeValue(Timestamp.getGMTNow());
            } else {

                // Fields of these types will be string form of dates
                // in yyyy-MM-dd HH:mm:ss format.
                try {
                    //Timestamp tValue = APIUtil.parseDateTime(value);
                   	//String webDateFormat = myUser.getWebConfigObject().getWebDateFormat();
//                	if (webDateFormat.equals("MM/dd/yyyy HH:mm:ss.SSS") || webDateFormat.equals("MM/dd/yyyy HH:mm:ss zzz"))
                   	String	webDateFormat = TBitsConstants.API_DATE_FORMAT;
                	DateFormat df = new SimpleDateFormat(webDateFormat);
                	//df.setTimeZone(TimeZone.getDefault());
                	Date d = null;
					try {
						d = df.parse(value);
					} catch (ParseException e) {
						throw new TBitsException("Couldnt parse '" + value + "'");
					}
                	Timestamp tValue = Timestamp.getTimestamp(d);
                    reqEx.setDateTimeValue(tValue);
                } catch (TBitsException de) {
                    myException.addException(de, WARNING);
                    LOG.warn("Exception while parsing the datetime" + " value passed for " + field.getDisplayName() + ": " + value);
                    reqEx.setDateTimeValue(Timestamp.getGMTNow());
                }
            }
        }

        break;

        case DataType.INT : {

            // Parse it as an integer.
            int iValue = 0;

            if (value == null) {
                iValue = 0;
            } else {
                try {
                    iValue = Integer.parseInt(value);
                } catch (NumberFormatException e) {
                    myException.addException(new TBitsException(e.toString()), WARNING);
                    LOG.warn("Exception while parsing the integer" + " value passed for " + field.getDisplayName() + ": " + value);
                    iValue = 0;
                }
            }

            reqEx.setIntValue(iValue);
        }

        break;

        case DataType.REAL : {

            // Parse it as a double value.
            double dValue = 0;

            if (value == null) {
                dValue = 0;
            } else {
                try {
                    dValue = Double.parseDouble(value);
                } catch (NumberFormatException e) {
                    myException.addException(new TBitsException(e.toString()), WARNING);
                    LOG.warn("Exception while parsing the double" + " value passed for " + field.getDisplayName() + ": " + value);
                    dValue = 0;
                }
            }

            reqEx.setRealValue(dValue);
        }

        break;

        case DataType.STRING : {

            // Set as is.
            if (value == null) {
                value = "";
            }

            reqEx.setVarcharValue(value);
        }

        break;

        case DataType.TEXT : {

            // Set as is.
            if (value != null) {
                value = LinkFormatter.replaceHrefWithSmartLinks(value);
            } else {
                value = "";
            }

            reqEx.setTextValue(value);
        }

        break;

        case DataType.TYPE : {
            int  iValue = 0;
            Type type   = null;

            try {
                if (value == null) {
                    type = Type.getDefaultTypeBySystemIdAndFieldName(mySystemId, field.getName());

                    if (type == null) {
                        myException.addException(new TBitsException("No default specified specified for " + field.getDisplayName()), SEVERE);
                        
                        return null;
                    }
                } else {
                    type = Type.lookupBySystemIdAndFieldNameAndTypeName(mySystemId, field.getName(), value);

                    if (type == null) {
                        myException.addException(new TBitsException(Messages.getMessage("INVALID_TYPE", field.getDisplayName(), value)), SEVERE);

                        return null;
                    }
                }

                iValue = type.getTypeId();
            } catch (DatabaseException de) {
                myException.addException(new TBitsException(de.toString()), WARNING);

                return null;
            }

            reqEx.setTypeValue(iValue);
        }

        break;

        case DataType.USERTYPE : {
        	LOG.info("Ignoring the UserType fields in while processing the RequestEx. They will be taken care with other UserTypes.");
        	break;
        }
        case DataType.ATTACHMENTS: {
        	reqEx.setTextValue(value);
        }
        break;
        }

        return reqEx;
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
     * @param bInactiveAllowed  Flag to see if inactive users should be
     *                          allowed
     *
     * @return List of request users.
     *
     * @exception APIException
     */
    private ArrayList<RequestUser> processUserField(String aList, Field aField, int aUserTypeId, boolean bInsert,
                                                    boolean bAuto, boolean bSysEmail, boolean bExclusion,
                                                    boolean bInactiveAllowed)
            throws APIException {

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
        // Load Exclusion list for the field including global list.
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
                    // description.
                }
            }

            // Check if it is auto
            if (aToken.equalsIgnoreCase("auto") == true) {

                // Check if auto is allowed in this user field.
                if (bAuto == true) {

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
                // Vaildate user against login/email and returns user object
                // returns null only if invalid login (transbit users not added)
                // else returns new external user object
                //
                user = TBitsHelper.getAPIUser(aToken);

                if ((user == null) && (bInactiveAllowed == true)) {
                    user = User.lookupAllByUserLogin(aToken);
                }

                // Check if we could not get a User Object.
                if (user != null) {

                    //
                    // If new external user, insert new user
                    //
                    if ((user.getUserId() == -1) && ((user.getUserTypeId() == UserType.EXTERNAL_USER) || (user.getUserTypeId() == UserType.INTERNAL_HIDDEN_LIST))) {

                        // This might be some external user login. Check
                        // if this field allows inserting external
                        // user logins.
                        if (bInsert == true) {

                            //
                            // Insert this as an external user and get the
                            // corresponding user object.
                            //
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
                } else {

                    //
                    // Add to Defunct users list
                    //
                    defunctUsers = defunctUsers + aToken + ", ";

                    continue;
                }
            } catch (DatabaseException de) {
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

                    // Check if this field allows BA Emails to be a
                    // part of the list.
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
            // Hence if assignee:giris,*giris is specified,
            // *giris will be taken i,e importance given to *
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
    private void processUserLists(Hashtable<String,String> paramsTable) throws APIException {

        //
        // We have few business rules associated with these fields. Some of
        // them apply to all, and some are specific to a particular type.
        // Listing all the rules here...
        //
        // Rule(s) that apply to all:
        // - '*' can be present next to atmost one user login indicating
        // it as the primary one.
        //
        // Rules that apply to Assignee field:
        // - All assignees should be valid users in our user
        // Database. No external user insertions can happen in this case.
        // - Any user in the assignee field should be part of the type user
        // list for the given BA.
        // - "auto" can be part of assignee list which should be replaced
        // by the selected volunteer.
        //
        // Rules that apply to Logger/Subscriber/Cc/To fields:
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
        // Rules that apply to Logger fields of Transferred Requests.
        // - Inactive users can be allowed as loggers for transferred requests.
        //
        myLoggers   = processUserField(loggerList, myFieldTable.get(Field.LOGGER), UserType.LOGGER, INSERT, NO_AUTO, NO_BA_EMAIL, EXCLUSION_LIST, INACTIVE && myIsTransferRequest);
        myAssignees = processUserField(assigneeList, myFieldTable.get(Field.ASSIGNEE), UserType.ASSIGNEE, INSERT, AUTO, NO_BA_EMAIL, EXCLUSION_LIST, NO_INACTIVE);

        //
        // If the assignToAll property of the BA is not set, then check
        // if the assingees are valid for the type attributes of this request
        // i.e category/status/severity/requesttype.
        //
        if (mySysConfig.getAssignToAll() == false) {
            checkAssigneeList();
        }

        mySubscribers = processUserField(subscriberList, myFieldTable.get(Field.SUBSCRIBER), UserType.SUBSCRIBER, INSERT, NO_AUTO, NO_BA_EMAIL, NO_EXCLUSION_LIST, NO_INACTIVE);
        myTos         = processUserField(toList, myFieldTable.get(Field.TO), UserType.TO, INSERT, NO_AUTO, BA_EMAIL, NO_EXCLUSION_LIST, NO_INACTIVE);
        myCcs         = processUserField(ccList, myFieldTable.get(Field.CC), UserType.CC, INSERT, NO_AUTO, BA_EMAIL, NO_EXCLUSION_LIST, NO_INACTIVE);

     
        // process Extended User Types
		   try {
			processExUserTypeFields(paramsTable);
		} catch (TBitsException e) {
			LOG.severe("",(e));
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
    		// ignore the default userType fields
    		if( field.getIsExtended() == false )
    			continue ;
    		
    		String value = params.get(field.getName());
    		if( null == value )
    		{
    			 // user did not provided the value.
    			value = "" ;
    		}
			ArrayList<RequestUser> reqUsers = processUserField(value,field, UserType.USERTYPE, INSERT, NO_AUTO, NO_BA_EMAIL, NO_EXCLUSION_LIST, NO_INACTIVE);
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

        // Initialize the extended fields map.
        myExtendedFields = new Hashtable<Field, RequestEx>();

        // Key, value of the param table.
        String key   = "";
        String value = "";

        // Check if the list of extended fields for this Business Area is
        // empty.
        if ((myExtFieldList == null) || (myExtFieldList.size() == 0)) {

            // We need not parse for any fields. So, return back.
            return;
        }

        // For each extended field check if there is a value specified in the
        // table.
        for (Field field : myExtFieldList) {

        	if( field.getIsExtended() == false )
        	{
        		LOG.error("This + " + field + " is not an extended field. Still it is in extended field list. Something is wrong. ") ;
        		continue ;
        	}
        	
        	// ignore the usertype extended fields. They will be taken care 
        	// when users are processed.
        	if( field.getDataTypeId() == DataType.USERTYPE )
        		continue ;
            // Skip inactive fields.
            if (field.getIsActive() == false ) {
                continue;
            }

            key   = field.getName();
            value = aParamTable.get(key);

            boolean isFieldSpecified = false;
            if(aParamTable.containsKey(key))
            {
            	isFieldSpecified = true;
            }
            
            //In case of Attachments, we check the permissions later.
            if(field.getDataTypeId() != DataType.ATTACHMENTS)
            {
	            //
	            // If the user has specified some value, then he should have
	            // change permission.
	            //
	            if (value != null) {
	                int     permission = 0;
	                Integer temp       = myPermTable.get(key);
	
	                if (temp != null) {
	                    permission = temp;
	                } else {
	                    permission = 0;
	                }
	
	                if ((permission & Permission.ADD) == 0) {
	                    myException.addException(new TBitsException(noPermission(key, "Add", myBusinessArea.getSystemPrefix(),  myUser.getUserLogin())), PERROR);
	
	                    // take the null value and continue.
	                    value = null;
	                }
	            }
            }else if( value == null )
            {
            	value = "";
            }
            RequestEx reqEx = prepareReqEx(field, value, isFieldSpecified);
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
    private void readFixedFieldValues(Connection con, Hashtable<String, String> aParamTable) throws APIException {

        // Key, value of the param table.
        String key   = "";
        String value = "";       

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
                        LOG.info("Obtained an external user to be inserted.");
                        myUser = User.insertExternalUser(myUser.getUserLogin(), myUser.getUserTypeId(), myUser, true);
                    }
                } else {
                    StringBuilder message = new StringBuilder();

                    message.append("No record found corresponding to this user in the ").append("database: ").append(value);
                    myException.addException(new TBitsException(message.toString()), FATAL);

                    throw myException;
                }
            } catch (Exception e) {
                myException.addException(new TBitsException(e.toString()), FATAL);

                throw myException;
            }
        }

        // Get the defaults for this business area.
        try {
            init();
            getDefaults();
        } catch (TBitsException de) {
            myException.addException(de, SEVERE);
        }

        Field field = null;

        // 2. Request Id
        // Key      : Field.REQUEST
        // Required : No
        // Default  : MaxRequestId of this BA + 1.
        // Comment  : This value is returned by the insert procedure for
        // requests table. We start with a value 0.
        myRequestId = 0;

        // 3. Category Id
        // Key      : Field.CATEGORY.
        // Required : No.
        // Default  : BA's Default Category.
        key   = Field.CATEGORY;
        field = myFieldTable.get(key);
        value = aParamTable.get(key);

        if ((value == null) || (value.trim().equals("") == true)) {
            myCategory = defCategory;
        } else {
            try {
                myCategory = Type.lookupBySystemIdAndFieldNameAndTypeName(mySystemId, key, value);

                if (myCategory == null) {
                    myException.addException(new TBitsException(Messages.getMessage("INVALID_TYPE", field.getDisplayName(), value)), SEVERE);

                    // Consider the default category in case we are supposed
                    // to proceed.
                    myCategory = defCategory;
                }
            } catch (DatabaseException de) {
                myException.addException(new TBitsException(de.toString()), SEVERE);

                // Consider the default category in case we are supposed
                // to proceed.
                myCategory = defCategory;
            }
        }

        // 4. Status Id
        // Key      : Field.STATUS.
        // Required : No.
        // Default  : BA's Default Status.
        key   = Field.STATUS;
        field = myFieldTable.get(key);
        value = aParamTable.get(key);

        if ((value == null) || (value.trim().equals("") == true)) {
            myStatus = defStatus;
        } else {
            try {
                myStatus = Type.lookupBySystemIdAndFieldNameAndTypeName(mySystemId, key, value);

                if (myStatus == null) {
                    myException.addException(new TBitsException(Messages.getMessage("INVALID_TYPE", field.getDisplayName(), value)), SEVERE);

                    // Consider the default status in case we are supposed
                    // to proceed.
                    myStatus = defStatus;
                }
            } catch (DatabaseException de) {
                myException.addException(new TBitsException(de.toString()), SEVERE);

                // Consider the default status in case we are supposed
                // to proceed.
                myStatus = defStatus;
            }
        }

        // 5. Severity Id
        // Key      : Field.SEVERITY.
        // Required : No.
        // Default  : BA's Default Severity.
        key   = Field.SEVERITY;
        field = myFieldTable.get(key);
        value = aParamTable.get(key);

        if ((value == null) || (value.trim().equals("") == true)) {
            mySeverity = defSeverity;
        } else {
            try {
                mySeverity = Type.lookupBySystemIdAndFieldNameAndTypeName(mySystemId, key, value);

                if (mySeverity == null) {
                    myException.addException(new TBitsException(Messages.getMessage("INVALID_TYPE", field.getDisplayName(), value)), SEVERE);

                    // Consider the default severity in case we are supposed
                    // to proceed.
                    mySeverity = defSeverity;
                }
            } catch (DatabaseException de) {
                myException.addException(new TBitsException(de.toString()), SEVERE);

                // Consider the default severity in case we are supposed
                // to proceed.
                mySeverity = defSeverity;
            }
        }

        // 6. RequestType Id
        // Key      : Field.REQUEST_TYPE.
        // Required : No.
        // Default  : BA's Default RequestType.
        key   = Field.REQUEST_TYPE;
        field = myFieldTable.get(key);
        value = aParamTable.get(key);

        if ((value == null) || (value.trim().equals("") == true)) {
            myRequestType = defRequestType;
        } else {
            try {
                myRequestType = Type.lookupBySystemIdAndFieldNameAndTypeName(mySystemId, key, value);

                if (myRequestType == null) {
                    myException.addException(new TBitsException(Messages.getMessage("INVALID_TYPE", field.getDisplayName(), value)), SEVERE);

                    // Consider the default req type in case we are supposed
                    // to proceed.
                    myRequestType = defRequestType;
                }
            } catch (DatabaseException de) {
                myException.addException(new TBitsException(de.toString()), SEVERE);

                // Consider the default req type in case we are supposed
                // to proceed.
                myRequestType = defRequestType;
            }
        }

        // 7. Logger
        // Key       : Field.LOGGER
        // Required  : No.
        // Default   : User.
        key   = Field.LOGGER;
        value = aParamTable.get(key);

        if ((value != null) && (value.trim().equals("") == false)) {
            loggerList = value;
        } else {
            loggerList = "";
        }

        // Check if loggerList is empty. In such case, consider the user of
        // this request as the logger.
        if ((loggerList == null) || (loggerList.equals("") == true)) {
            loggerList = myUser.getUserLogin().replace(".transbittech.com", "");
        }

        // 8. Assignee
        // Key       : Field.ASSIGNEE
        // Required  : No.
        // Default   : Empty List.
        key   = Field.ASSIGNEE;
        value = aParamTable.get(key);

        if ((value != null) && (value.trim().equals("") == false)) {
            assigneeList = value;
        } else {
            assigneeList = "";
        }

        // 9. Subscribers
        // Key       : Field.SUBSCRIBER
        // Required  : No.
        // Default   : Empty List.
        key   = Field.SUBSCRIBER;
        value = aParamTable.get(key);

        if ((value != null) && (value.trim().equals("") == false)) {
            subscriberList = value;
        } else {
            subscriberList = "";
        }

        // 10 Tos
        // Key       : Field.TO
        // Required  : No.
        // Default   : Empty List.
        key   = Field.TO;
        value = aParamTable.get(key);

        if ((value != null) && (value.trim().equals("") == false)) {
            toList = value;
        } else {
            toList = "";
        }

        // 11. Ccs
        // Key       : Field.CC
        // Required  : No.
        // Default   : Empty List.
        key   = Field.CC;
        value = aParamTable.get(key);

        if ((value != null) && (value.trim().equals("") == false)) {
            ccList = value;
        } else {
            ccList = "";
        }

        // 12. Subject
        // Key      : Field.SUBJECT
        // Required : Yes if this is not from email.
        // Default  : Empty String.
        key   = Field.SUBJECT;
        value = aParamTable.get(key);

        if ((value == null) || (value.trim().equals("") == true)) {
            mySubject = "";
        } else {
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
            myIsPrivate = false;
        } else if ((value.trim().equalsIgnoreCase("false") == true) || (value.trim().equalsIgnoreCase("no") == true) || (value.trim().equalsIgnoreCase("public") == true)
                   || (value.trim().equalsIgnoreCase("0") == true)) {
            myIsPrivate = false;
        } else if ((value.trim().equalsIgnoreCase("true") == true) || (value.trim().equalsIgnoreCase("yes") == true) || (value.trim().equalsIgnoreCase("private") == true)
                   || (value.trim().equalsIgnoreCase("1") == true)) {
            myIsPrivate = true;
        }

        // 15. Parent Request Id.
        // Key      : Field.PARENT_REQUEST_ID
        // Required : No.
        // Default  : 0.
        key   = Field.PARENT_REQUEST_ID;
        value = aParamTable.get(key);

        if ((value == null) || (value.trim().equals("") == true) || (value.trim().equals("0") == true)) {
            myParentId = 0;
        } else {
            try {
                myParentId = Integer.parseInt(value);
            } catch (NumberFormatException nfe) {
                myParentId = 0;
                myException.addException(new TBitsException("Parent Id must be numeric: " + value), SEVERE);
            }

            // Check if this is a valid request in this BA.
            try {
                Request tmp = Request.lookupBySystemIdAndRequestId(con,mySystemId, myParentId);

                if (tmp == null) {
                    myParentId = 0;
                    myException.addException(new TBitsException("Please enter a valid request id to associate: " + value), SEVERE);
                }

                boolean isTransferred = APIUtil.isTransferred(myBusinessArea.getSystemPrefix(), myParentId);

                if (isTransferred == true) {
                    StringBuilder message = new StringBuilder();

                    message.append("Transferred request cannot be part ").append("of an association: ").append(value);
                    myException.addException(new TBitsException(message.toString()), SEVERE);
                }
            } catch (DatabaseException de) {
            	LOG.info("",(de));
                myParentId = 0;
                myException.addException(new TBitsException(de.toString()), SEVERE);
            }
        }

        // 17. Max Action Id
        // Key      : Field.MAX_ACTION_ID
        // Required : No
        // Default  : 1 - for a new request, the max action id is 1.
        myMaxActionId = 1;

        // 18. Due Date.
        // Key      : Field.DUE_DATE.
        // Required : No.
        // Default  : CurrentTime + Default Due Date For This BA.
        key   = Field.DUE_DATE;
        value = aParamTable.get(key);

        if ((value == null) || (value.trim().equals("") == true)) {

            // Check if the BA allows null due dates.
            if (mySysConfig.getAllowNullDueDate() == true) {
                myDueDate = null;
            } else {
                myDueDate = defDueDate;
            }
        } else {

            // This can be time/date/datetime. So act accordingly.
            try {
                //myDueDate = APIUtil.parseDateTime(value);
                SimpleDateFormat sdf = new SimpleDateFormat(API_DATE_FORMAT);

                try {
					myDueDate = Timestamp.getTimestamp(sdf.parse(value));
				} catch (ParseException e) {
					throw new TBitsException("Date should be specified in " + API_DATE_FORMAT + " format. " + e.toString());
				}

                LOG.info("The due date after APIUtil.parseDateTime : " + APIUtil.printDate(myDueDate.getTime()));
            } catch (TBitsException de) {
                myException.addException(de, SEVERE);
                myDueDate = defDueDate;
            }
        }

     // 18. Due Date.
        // Key      : Field.DUE_DATE.
        // Required : No.
        // Default  : CurrentTime + Default Due Date For This BA.
        key   = Field.LASTUPDATED_DATE;
        value = aParamTable.get(key);

        if ((value == null) || (value.trim().equals("") == true)) {
            	myUpdatedDate = Timestamp.getGMTNow();;
        } else {

            // This can be time/date/datetime. So act accordingly.
            try {
                //myDueDate = APIUtil.parseDateTime(value);
                SimpleDateFormat sdf = new SimpleDateFormat(API_DATE_FORMAT);

                try {
                	myUpdatedDate = Timestamp.getTimestamp(sdf.parse(value));
				} catch (ParseException e) {
					throw new TBitsException("Date should be specified in " + API_DATE_FORMAT + " format. " + e.toString());
				}

                LOG.info("The last update date fter APIUtil.parseDateTime : " + APIUtil.printDate(myUpdatedDate.getTime()));
            } catch (TBitsException de) {
                myException.addException(de, SEVERE);
                myUpdatedDate = Timestamp.getGMTNow();
            }
        }
        
        key   = Field.LOGGED_DATE;
        value = aParamTable.get(key);

        if ((value == null) || (value.trim().equals("") == true)) {
        	myLoggedDate = Timestamp.getGMTNow();;
        } else {

            // This can be time/date/datetime. So act accordingly.
            try {
                //myDueDate = APIUtil.parseDateTime(value);
                SimpleDateFormat sdf = new SimpleDateFormat(API_DATE_FORMAT);

                try {
                	myLoggedDate = Timestamp.getTimestamp(sdf.parse(value));
				} catch (ParseException e) {
					throw new TBitsException("Date should be specified in " + API_DATE_FORMAT + " format. " + e.toString());
				}

                LOG.info("The last update date fter APIUtil.parseDateTime : " + APIUtil.printDate(myLoggedDate.getTime()));
            } catch (TBitsException de) {
                myException.addException(de, SEVERE);
                myLoggedDate = Timestamp.getGMTNow();
            }
        }
        // 19. Last Updated Date.
        // Key      : Field.LASTUPDATED_DATE
        // Required : No.
        // Default  : Current Time.
//        myUpdatedDate = Timestamp.getGMTNow();

        // 20. Logged Date.
        // Key      : Field.LOGGED_DATE
        // Required : No.
        // Default  : Current Time.
//        myLoggedDate = Timestamp.getGMTNow();

//        // Check if due date is greater than the logged date.
//        if ((myDueDate != null) && (myDueDate.getTime() < myLoggedDate.getTime())) {
//            myException.addException(new TBitsException("The due date specified cannot occur in the past."), SEVERE);
//        }

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
        // in Attachments/temp location.
        key   = Field.ATTACHMENTS;
        value = aParamTable.get(key);
        // Treat null as no-op request
        if(value == null)
        	myAttachments = null;
        else{
        	myAttachments = new ArrayList<AttachmentInfo>();
            if (!value.trim().equals("")) {
            	try
            	{
            		myAttachments = AttachmentInfo.fromJson(value);
            	}
            	catch(Exception e)
            	{
            		LOG.error("Error while parsing the attachments.", e);
            	}
            }
        }

        // 23. Summary
        // Key      : Field.SUMMARY
        // Required : No
        // Default  : Empty String.
        key   = Field.SUMMARY;
        value = aParamTable.get(key);

        if ((value == null) || (value.trim().equals("") == true)) {
            mySummary = "";
        } else {
            mySummary = value.trim();
        }
        
        // Key      : Field.SUMMARY + "_content_type"
        // Required : No
        // Default  : Empty String.
        key   = Field.SUMMARY + "_content_type";
        value = aParamTable.get(key);

        if ((value == null)) {
            mySummaryContentType = CONTENT_TYPE_HTML;
        } else {
            mySummaryContentType = Integer.parseInt(value.trim());
        }

        // 24. Memo
        // Key      : Field.MEMO.
        // Required : No
        // Default  : Empty String.
        key   = Field.MEMO;
        value = aParamTable.get(key);

        if ((value == null) || (value.trim().equals("") == true)) {
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
                if (value.equalsIgnoreCase("true") || value.trim().equalsIgnoreCase("yes") || value.trim().equalsIgnoreCase("1")) {
                    myNotify = 1;
                } else if (value.equalsIgnoreCase("false") || value.trim().equalsIgnoreCase("no") || value.trim().equalsIgnoreCase("0")) {
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
        } else if ((value.trim().equalsIgnoreCase("false") == true) || (value.trim().equalsIgnoreCase("no") == true) || (value.trim().equalsIgnoreCase("0") == true)) {
            myNotifyLoggers = false;
        } else if ((value.trim().equalsIgnoreCase("true") == true) || (value.trim().equalsIgnoreCase("yes") == true) || (value.trim().equalsIgnoreCase("1") == true)) {
            myNotifyLoggers = true;
        } else {
            myNotifyLoggers = true;
        }

        // 28. Replied To Action
        // Key      : Field.REPLIED_TO_ACTION
        // Required : No.
        // Default  : 0.
        myRepliedToAction = 0;

        // 29. Related Requests
        // Key      : Field.RELATED_REQUESTS
        // Required : No.
        // Default  :
        // Nitiraj msg : related request are not being checked for validation ?
        key   = Field.RELATED_REQUESTS;
        value = aParamTable.get(key);

        if ((value == null) || (value.trim().equals("") == true)) {
            myRelatedRequests = null;
        } else {
            myRelatedRequests = value.trim();
        }

        // 30. Office Id
        // Key      : Field.OFFICE.
        // Required : Yes.
        // Default  : User's default status.
        key   = Field.OFFICE;
        field = myFieldTable.get(key);
        value = aParamTable.get(key);

        if ((value == null) || (value.trim().equals("") == true)) {
            myOffice = defOffice;
        } else {
            try {
                myOffice = Type.lookupBySystemIdAndFieldNameAndTypeName(mySystemId, key, value);

                if (myOffice == null) {
                    myException.addException(new TBitsException(Messages.getMessage("INVALID_TYPE", field.getDisplayName(), value)), SEVERE);

                    // Consider the default status in case we are supposed
                    // to proceed.
                    myOffice = defOffice;
                }
            } catch (DatabaseException de) {
                myException.addException(new TBitsException(de.toString()), SEVERE);

                // Consider the default Office in case we are supposed
                // to proceed.
                myOffice = defOffice;
            }
        }

        // Check if this is a transferred request. If so read the source value.
        if (myIsTransferRequest == true) {
            myTransferredRequest = aParamTable.get(TR_SRC_REQUEST);
            myTransferredRequest = ((myTransferredRequest == null) || myTransferredRequest.trim().equals(""))
                                   ? ""
                                   : myTransferredRequest;
        }

        // Check if there are any exceptions in the list above the acceptable
        // level.
        if ((myException != null) && (myException.getExceptionCount(myLevel) > 0)) {
            throw myException;
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

            LOG.info("Running the rule: " + ruleMessage);

            // Get the conditions to be checked.
            ArrayList<RuleCondition> rcList         = br.getConditionList();
            boolean                  continueAction = true;

            for (RuleCondition rc : rcList) {

                //
                // If the state of condition is change then ignore this rule
                // as this is meant to be executed only when there is a change
                // in the state of the request.
                //
                if (rc.getState() == State.CHANGE) {
                    continueAction = false;

                    break;
                }

                // This is check on the current state of the request.
                String   fieldName  = rc.getFieldName();
                String   fieldValue = rc.getCurrentValue();
                Operator operator   = rc.getCurrentOperator();
                boolean  flag       = compare(fieldName, fieldValue, operator);

                if (flag == false) {

                    //
                    // The state of the request does not suit the rule to be
                    // executed. So, skip this.
                    //
                    continueAction = false;

                    break;
                }
            }

            //
            // Check why we came out of the above for loop. If continueAction
            // is set to false, then we can skip the action part and continue
            // with the next rule.
            //
            if (continueAction == false) {
                LOG.info("Rule condition failed: " + ruleMessage);

                continue;
            }

            // Get the list of actions to be performed.
            ArrayList<RuleAction> raList = br.getActionList();

            for (RuleAction ra : raList) {
                String   fieldName = ra.getFieldName();
                String   ruleValue = ra.getValue();
                Operator operator  = ra.getOperator();

                LOG.info(fieldName + ", " + ruleValue + ", " + operator);

                // Check the action type.
                if (ra.getActionType() == ActionType.VALIDATE) {

                    // If the action type is valid, compare the field value and
                    // check if the state is valid.
                    boolean flag = compare(fieldName, ruleValue, operator);

                    if (flag == false) {
                        LOG.info("Comparison failed: " + ruleMessage);

                    	TBitsException de = new TBitsException(ruleMessage);
                        myException.addException(de, SEVERE);
                        canContinue = false;
                        break;
                    }
                } else if (ra.getActionType() == ActionType.MODIFY) {
                    APIUtil.modify(fieldName, ruleValue, operator, myFieldTable, myExtendedFields, myRequest);
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
     * @param mailResMgr 
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

    public static Hashtable<String, String> tokenize(String input) {
        Hashtable<String, String> paramTable = new Hashtable<String, String>();

        if (input == null) {
            return paramTable;
        }

        StringTokenizer ost = new StringTokenizer(input.trim(), "|");

        while (ost.hasMoreTokens()) {
            String str   = ost.nextToken().trim();
            int    index = str.indexOf(':');

            if (index > 0) {
                String key   = str.substring(0, index);
                String value = str.substring(index + 1);

                paramTable.put(key, value);
            } else {
                LOG.warn("Bad token: " + str);
            }
        }

        return paramTable;
    }

    /**
     * This method builds the Request object from the attributes that are
     * obtained from the param table passed to the addRequest method.
     */
    private void updateLocals() 
    {
        myCategory      = myRequest.getCategoryId();
        myStatus        = myRequest.getStatusId();
        mySeverity      = myRequest.getSeverityId();
        myRequestType   = myRequest.getRequestTypeId();
        mySubject       = myRequest.getSubject();
        myIsPrivate     = myRequest.getIsPrivate();
        myParentId      = myRequest.getParentRequestId();
        myDueDate       = (myRequest.getDueDate() == null ? null : new Timestamp( myRequest.getDueDate().getTime() ));
        myNotify        = (myRequest.getNotify() == false ? 0 : 1 ) ;
        myNotifyLoggers = myRequest.getNotifyLoggers();

        //
        // Set the fields which actually reside in a different table but are
        // related to the request.
        //
        myLoggers        = (myRequest.getLoggers() == null ? null : new ArrayList<RequestUser>(myRequest.getLoggers()) );
        myAssignees      = (myRequest.getAssignees() == null ? null : new ArrayList<RequestUser>(myRequest.getAssignees()) );
        mySubscribers    = (myRequest.getSubscribers() == null ? null : new ArrayList<RequestUser>(myRequest.getSubscribers()) );
        myTos            = (myRequest.getTos() == null ? null : new ArrayList<RequestUser>(myRequest.getTos()) );
        myCcs            = (myRequest.getCcs() == null ? null : new ArrayList<RequestUser>(myRequest.getCcs()) );
//        myExtendedFields = myRequest.getExtendedFields();
        myAttachments 	 = myRequest.getAttachments();
        myDescription = myRequest.get(Field.DESCRIPTION);
        mySummary = myRequest.get(Field.SUMMARY);
        myRelatedRequests = myRequest.getRelatedRequests();
        myOffice = myRequest.getOfficeId();
    }

    //~--- get methods --------------------------------------------------------

    /**
     * This method retrieves the default values for certain fields for the
     * current business area.
     */
    private void getDefaults() throws TBitsException {
        mySysConfig = myBusinessArea.getSysConfigObject();

        // Get the default category.
        try {
            defCategory = Type.getDefaultTypeBySystemIdAndFieldName(mySystemId, Field.CATEGORY);
        } catch (DatabaseException de) {
            throw new TBitsException(de.toString());
        }

        // Get the default Status.
        try {
            defStatus = Type.getDefaultTypeBySystemIdAndFieldName(mySystemId, Field.STATUS);
        } catch (DatabaseException de) {
            throw new TBitsException(de.toString());
        }

        // Get the default Severity.
        try {
            defSeverity = Type.getDefaultTypeBySystemIdAndFieldName(mySystemId, Field.SEVERITY);
        } catch (DatabaseException de) {
            throw new TBitsException(de.toString());
        }

        // Get the default Request Type.
        try {
            defRequestType = Type.getDefaultTypeBySystemIdAndFieldName(mySystemId, Field.REQUEST_TYPE);
        } catch (DatabaseException de) {
            throw new TBitsException(de.toString());
        }

        // Get the default Office.
        try {
            if (myUser.getLocation() != null) {
                defOffice = Type.lookupBySystemIdAndFieldNameAndTypeName(mySystemId, Field.OFFICE, myUser.getLocation());

                if (defOffice == null) {
                    defOffice = Type.getDefaultTypeBySystemIdAndFieldName(mySystemId, Field.OFFICE);
                }
            } else {
                defOffice = Type.getDefaultTypeBySystemIdAndFieldName(mySystemId, Field.OFFICE);
            }
        } catch (DatabaseException de) {
            throw new TBitsException(de.toString());
        }

        //
        // Get the default due date if the BA does not allow nulls or
        // allow nulls but have a default also.
        //
        if ((mySysConfig.getAllowNullDueDate() == false) || (mySysConfig.getDefaultDueDate() != 0)) {
            defDueDate = getGMTDefaultDueDate();
        }

        // Get the default mail options of this Business Area.
        defNotify        = mySysConfig.getRequestNotify();
        defNotifyLoggers = mySysConfig.getRequestNotifyLoggers();
    }

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
     * This method returns the track record for the given field and value
     * according to the tracking option 1 i.e.
     * [ Field: Value ]
     *
     * @param aField Field object to generate the track record.
     * @param aValue current value in the field.
     *
     * @return Track record.
     */
    private String getTrackRecord(Field aField, String aValue) {
        StringBuilder record = new StringBuilder();

        // Handle the null reference case.
        if (aField == null) {
            return record.toString();
        }

        int    trackOption       = aField.getTrackingOption();
        String defunctUsers      = myDefunctUsers.get(aField.getName());
        String emailDefunctUsers = myDefunctUsers.get("EMAIL_" + aField.getName());
        String baMailIds         = myBaMailIds.get(aField.getName());

        aValue       = ((aValue == null)
                        ? ""
                        : aValue.trim());
        baMailIds    = ((baMailIds == null)
                        ? ""
                        : baMailIds.trim());
        defunctUsers = ((defunctUsers == null)
                        ? ""
                        : defunctUsers.trim());

        if (baMailIds.equals("") == false) {
            aValue = ((aValue.equals("") == true)
                      ? baMailIds
                      : aValue + ", " + baMailIds);
        }

        if ((aField.getName().equals(Field.ASSIGNEE) == false) && (aField.getName().equals(Field.LOGGER) == false)) {
            if ((trackOption == 1) || (trackOption == 4)) {
                record.append(aField.getName()).append("##").append(aField.getFieldId()).append("##[ ").append(aField.getDisplayName()).append(": ").append(aValue).append(" ]\n");
            } else if (((trackOption == 2) || (trackOption == 5)) && (aValue.equals("") == false)) {
                record.append(aField.getName()).append("##").append(aField.getFieldId()).append("##[ ").append(aField.getDisplayName()).append(": ").append(aValue).append(" ]\n");
            }
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

        return record.toString();
    }

    //~--- set methods --------------------------------------------------------

    /**
     * This method sets the extended fields value of the request.
     * @throws TBitsException 
     */
    public void setExtendedFields() throws TBitsException {
        try {
            myRequest.setExtendedFields(myExtendedFields);
        } catch (DatabaseException de) {
            LOG.warn(de.toString(), de);
        }

        return;
    }

    /**
     * Mutator method for IsEmailCategory.
     */
    public void setIsEmailCategory(boolean aIsEmailCategory) {
        myIsEmailCategory = aIsEmailCategory;
    }

    /**
     * Mutator method for IsTransferRequest.
     */
    public void setIsTransferRequest(boolean aIsTransferRequest) {
        myIsTransferRequest = aIsTransferRequest;
    }

    /**
     * Mutator method for myUnauthorized
     */
    public void setIsUnauthorized(boolean aIsUnauthorized) {
        myUnauthorized = aIsUnauthorized;
    }

    /**
     * This method reverts to the default value for the given field.
     *
     * @param aFieldName  Name of the field.
     *
     */
    private void setToDefault(String aFieldName) {
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
            }
        }

        break;

        case DATE :
        case TIME :
        case DATETIME :
            break;

        case INT : {
            if (fieldName.equals(Field.NOTIFY)) {
                myRequest.setNotify(( defNotify != 0 ));
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
                myRequest.setCategoryId(defCategory);
            } else if (fieldName.equals(Field.STATUS)) {
                myRequest.setStatusId(defStatus);
            } else if (fieldName.equals(Field.SEVERITY)) {
                myRequest.setSeverityId(defSeverity);
            } else if (fieldName.equals(Field.REQUEST_TYPE)) {
                myRequest.setRequestTypeId(defRequestType);
            } else if (fieldName.equals(Field.OFFICE)) {
                myRequest.setOfficeId(defOffice);
            } else if (field.getIsExtended() == true) {
                RequestEx reqEx = myExtendedFields.get(field);

                if (reqEx != null) {

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
                try {
					makeUserAsLogger();
				} catch (TBitsException e) {
					LOG.severe("",(e));
				}
                myRequest.setLoggers(myLoggers);
            } else if (fieldName.equals(Field.ASSIGNEE)) {
                myRequest.setAssignees(new ArrayList<RequestUser>());
            } else if (fieldName.equals(Field.SUBSCRIBER)) {
                myRequest.setSubscribers(new ArrayList<RequestUser>());
            } else if (fieldName.equals(Field.TO)) {
                myRequest.setTos(new ArrayList<RequestUser>());
            } else if (fieldName.equals(Field.CC)) {
                myRequest.setCcs(new ArrayList<RequestUser>());
            }
        }

        break;
        }
    }
}
