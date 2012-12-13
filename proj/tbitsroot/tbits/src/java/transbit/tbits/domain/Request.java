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
 * Request.java
 *
 * $Header:
 *
 */
package transbit.tbits.domain;

//~--- non-JDK imports --------------------------------------------------------

//Other TBits Imports.
import static transbit.tbits.Helper.TBitsConstants.ASC_ORDER;
import static transbit.tbits.Helper.TBitsConstants.DB_DATE_FORMAT;
import static transbit.tbits.Helper.TBitsConstants.PKG_DOMAIN;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.TimeZone;

import transbit.tbits.Helper.HashCodeUtil;
import transbit.tbits.Helper.TBitsConstants;
import transbit.tbits.api.APIUtil;
import transbit.tbits.api.AttachmentInfo;
import transbit.tbits.common.DataSourcePool;
import transbit.tbits.common.DatabaseException;
import transbit.tbits.common.TBitsLogger;
import transbit.tbits.common.Timestamp;
import transbit.tbits.common.Utilities;

//~--- classes ----------------------------------------------------------------

//Third party imports.

/**
 * This class is the domain object corresponding to the requests table
 * in the database.
 *
 * @author  : Nitiraj
 * @version : $Id: $
 *
 */
public class Request implements Comparable<Request>, Cloneable, Serializable {
	
	private static final long serialVersionUID = 2L;
    public static final TBitsLogger LOG = TBitsLogger.getLogger(PKG_DOMAIN);

    // Enum sort of fields for Attributes.
    private static final int SYSTEMID        = 1;
    private static final int SUBJECT         = 7;
    private static final int STATUSID        = 4;
    private static final int SEVERITYID      = 5;
    private static final int REQUESTTYPEID   = 6;
    private static final int REQUESTID       = 2;
    private static final int PARENTREQUESTID = 10;
    private static final int ISPRIVATE       = 9;
    private static final int DESCRIPTION     = 8;
    private static final int DESCRIPTIONCONTENTTYPE = 25;
    private static final int CATEGORYID      = 3;
    private static final int USERID          = 11;
    private static final int SUMMARY         = 18;
    private static final int SUMMARYCONTENTTYPE = 26;
    private static final int REPLIEDTOACTION = 23;
    protected static final int OP_UPDATE       = 902;

    // Enums to convey the operation being performed.
    protected static final int OP_INSERT         = 901;
    private static final int OFFICEID          = 24;
    private static final int NOTIFYLOGGERS     = 22;
    private static final int NOTIFY            = 21;
    private static final int MEMO              = 19;
    private static final int MAXACTIONID       = 12;
    private static final int LOGGEDDATE        = 14;
    private static final int LASTUPDATEDDATE   = 15;
    private static final int HEADERDESCRIPTION = 16;
    private static final int DUEDATE           = 13;
    private static final int ATTACHMENTS       = 17;
    private static final int APPENDINTERFACE   = 20;

	private static final String MESSAGE_NULL = "Null value not allowed.";

	private static final String TYPE_NOT_FOUND = "The provided type was not found.";

    // Static attributes related to sorting.
    private static int ourSortField;
    private static int ourSortOrder;

    //~--- fields -------------------------------------------------------------

//    private int                    myAppendInterface ;
//    private ArrayList<RequestUser> myAssignees = new ArrayList<RequestUser>();
//    private ArrayList<AttachmentInfo> myAttachments = new ArrayList<AttachmentInfo>();
//    private Type                   myCategoryId;
//    private ArrayList<RequestUser> myCcs = new ArrayList<RequestUser>();
//    private String                 myDescription;
//    private int                    myDescriptionContentType;
//    private Date              myDueDate;

    // RequestEx records into requests_ex table.
//    private Hashtable<Field, RequestEx> myExtendedFields;
//    private String                      myHeaderDescription;
//    private boolean                     myIsPrivate;
//    private Date                   myLastUpdatedDate;
//    private Date                   myLoggedDate;

    // Records related to request that go into a different table.
    // RequestUser records into request_users table.
//    private ArrayList<RequestUser>   myLoggers = new ArrayList<RequestUser>();
    private HashMap<Field, Object> myMapFieldToObjects = new HashMap<Field,Object>();
//    public Hashtable<Integer,ArrayList<RequestUser>> userTypeMap = new Hashtable<Integer,ArrayList<RequestUser>>() ;

    // Map of request values as <field-name, value)
//    public Hashtable<String, String>  myMapFieldToValues;
//    private int                       myMaxActionId;
//    private String                    myMemo;
//    private int                       myNotify;
//    private boolean                   myNotifyLoggers;
//    private Type                      myOfficeId;
//    private int                       myParentRequestId;
//    private Hashtable<String, String> myParentRequests;
//    private Hashtable<String, String> myRelatedRequests;
//    private int                       myRepliedToAction;
    private Integer                       myRequestId;
    
    // TODO turn into field
    private int __childCount = -1;
    
    public void setChildCount(int childCount) {
		this.__childCount = childCount;
	}

    /**
     * Gets the number of child requests for this request.
     * Returns -1 in case the child count has not been set.
     * 
     * @return childCount of the request
     */
	public int getChildCount() {
		return __childCount;
	}
    
//    private Type                      myRequestTypeId;
//    private Type                      mySeverityId;
//    private Hashtable<String, String> mySiblingRequests;
//    private Type                      myStatusId;
//    private int                       smsId;  
    // Records related to sub-requests, sibling requests, related-requests
//    private Hashtable<String, String> mySubRequests;
//    private String                    mySubject;
//    private ArrayList<RequestUser>    mySubscribers = new ArrayList<RequestUser>();
//    private String                    mySummary;
//    private int                 	  mySummaryContentType;

    // Attributes of this Domain Object.
    private Integer                    mySystemId;
//    private ArrayList<RequestUser> myTos = new ArrayList<RequestUser>();
//    private User                   myUserId;
    // To store the context path of the webapps, so that it is easy to give proper links to various resources.
    private String				   myContext = null; 
    //~--- constructors -------------------------------------------------------

	private int version;
	
	private int source = 0;
	
	/**
     * The default constructor.
     */
    public Request(int systemId) 
    {
//        myMapFieldToObjects = new Hashtable<String, Object>();
        this.setSystemId(systemId);
    }

    // should be removed : as the request without its sys_id is useless.
    // because most of the internal functions uses systemId 
    public Request() {
//        myMapFieldToObjects = new Hashtable<String, Object>();
    }
    
    public int getSource(){
    	return source;
    }
    
    public void setSource(int src){
    	source = src;
    }
    
    public Object getObject(Field field)
    {
    	disAllowNull(field);
    	
    	if( field.getSystemId() != this.getSystemId() )
    		throw new IllegalArgumentException("The field " + field + " does not belong to the ba with sys_id = " + this.getSystemId());
    	
    	return myMapFieldToObjects.get(field);
    }
    
    public Object getObject(String fieldName)
    {
    	disAllowNull(fieldName) ;
    	Field field = null ;
    	try {
			 field = Field.lookupBySystemIdAndFieldName(this.getSystemId(), fieldName);
		} 
    	catch (DatabaseException e) 
		{
    		LOG.info("",(e));
		}
    	
    	if( null == field )
    		throw new IllegalArgumentException("Cannot find the field with name = " + fieldName + " in the ba with sys_id " + this.getSystemId());
    	
    	return this.getObject(field);
    }
    
//    public Request(int aSystemId, int aRequestId, Type aCategoryId,
//			Type aStatusId, Type aSeverityId, Type aRequestTypeId,
//			String aSubject, String aDescription, boolean aIsPrivate,
//			int aParentRequestId, Integer aUserId, int aMaxActionId,
//			Date aDueDate, Date aLoggedDate,
//			Date aLastUpdatedDate, String aHeaderDescription,
//			String aAttachments, String aSummary,
//			String aMemo, int aAppendInterface, int aNotify,
//			boolean aNotifyLoggers, int aRepliedToAction, Type aOffice) {
//    	
//    	this(aSystemId, aRequestId, aCategoryId,
//    			aStatusId, aSeverityId, aRequestTypeId,
//    			aSubject, aDescription, aIsPrivate,
//    			aParentRequestId, aUserId, aMaxActionId,
//    			aDueDate, aLoggedDate,
//    			aLastUpdatedDate, aHeaderDescription,
//    			AttachmentInfo.fromJson(aAttachments), aSummary,
//    			aMemo, aAppendInterface, aNotify,
//    			aNotifyLoggers, aRepliedToAction, aOffice);
//	}
    /**
     * The complete constructor.
     *
     *  @param aSystemId
     *  @param aRequestId
     *  @param aCategoryId
     *  @param aStatusId
     *  @param aSeverityId
     *  @param aRequestTypeId
     *  @param aSubject
     *  @param aDescription
     *  @param aIsPrivate
     *  @param aParentRequestId
     *  @param aUserId
     *  @param aMaxActionId
     *  @param aDueDate
     *  @param aLoggedDate
     *  @param aLastUpdatedDate
     *  @param aHeaderDescription
     *  @param aAttachments
     *  @param aSummary
     *  @param aMemo
     *  @param aAppendInterface
     *  @param aNotify
     *  @param aNotifyLoggers
     *  @param aRepliedToAction
     *  @param aOffice
     */
    public Request(int aSystemId, int aRequestId, Type aCategoryId, Type aStatusId, Type aSeverityId, Type aRequestTypeId, String aSubject, TextDataType aDescription, boolean aIsPrivate,
                   int aParentRequestId, Integer aUserId, int aMaxActionId, Date aDueDate, Date aLoggedDate, Date aLastUpdatedDate, String aHeaderDescription, Collection<AttachmentInfo> aAttachments,
                   TextDataType aSummary, String aMemo, int aAppendInterface, Boolean aNotify, boolean aNotifyLoggers, int aRepliedToAction, Type aOffice) {
        setSystemId(aSystemId);
        setRequestId(aRequestId);
        setCategoryId(aCategoryId);
        setStatusId(aStatusId);
        setSeverityId(aSeverityId);
        setRequestTypeId(aRequestTypeId);
        setSubject(aSubject);
        setDescription(aDescription);
        setIsPrivate(aIsPrivate);
        setParentRequestId(aParentRequestId);
        setUserId(aUserId);
        setMaxActionId(aMaxActionId);
        setDueDate(aDueDate);
        setLoggedDate(aLoggedDate);
        setLastUpdatedDate(aLastUpdatedDate);
        setHeaderDescription(aHeaderDescription);
        setAttachments(aAttachments);
        setSummary(aSummary);
        setMemo(aMemo);
        setAppendInterface(aAppendInterface);
        setNotify(aNotify);
        setNotifyLoggers(aNotifyLoggers);
        setRepliedToAction(aRepliedToAction);
        
        if(aOffice != null)
        	setOfficeId(aOffice);
    }

    //~--- methods ------------------------------------------------------------
@Deprecated // don't use this cloning.
    public Object clone() {
        try {
            return deepClone();
        }
        catch (CloneNotSupportedException e) {
            // This should never happen
            throw new InternalError(e.toString());
        }
    }

@Deprecated
    private Object deepClone() throws CloneNotSupportedException {
        try {
            Request copy = (Request)super.clone();
//            if(myLastUpdatedDate != null)
//	            copy.myLastUpdatedDate = (Date)myLastUpdatedDate.clone();
//            if(myLoggedDate != null)
//	            copy.myLoggedDate = (Date)myLoggedDate.clone();
//            if(myExtendedFields != null)
//	            copy.myExtendedFields = (Hashtable)myExtendedFields.clone();
            if(myMapFieldToObjects != null)
	            copy.myMapFieldToObjects = (HashMap)myMapFieldToObjects.clone();
//            if(myLoggers != null)
//	            copy.myLoggers = (ArrayList)myLoggers.clone();
//            if(myParentRequests != null)
//			    copy.myParentRequests = (Hashtable)myParentRequests.clone();
//            if(myRelatedRequests != null)
//				copy.myRelatedRequests = (Hashtable)myRelatedRequests.clone();
//            if(mySiblingRequests != null)
//                copy.mySiblingRequests = (Hashtable)mySiblingRequests.clone();
//            if(mySubRequests != null)
//                copy.mySubRequests = (Hashtable)mySubRequests.clone();
//            if(mySubscribers != null )
//				copy.mySubscribers = (ArrayList)mySubscribers.clone();
//			if(myTos != null )
//	            copy.myTos = (ArrayList)myTos.clone();
            return copy;
        } catch (CloneNotSupportedException e) {
            throw e;
        }
    }

    /**
     * Nitiraj msg : This is incorrect implementation. It uses the ourSortField and ourSortOrder
     * static variables which can be changed by some other part of tBits while some one is sorting.
     *     
     * Method that compares this object with the one passed W.R.T the
     * ourSortField.
     *
     * @param aObject  Object to be compared.
     *
     * @return 0 - If they are equal.
     *         1 - If this is greater.
     *        -1 - If this is smaller.
     *
     */
    public int compareTo(Request aObject) {
        switch (ourSortField) {
        case SYSTEMID : {
            Integer i1 = this.getSystemId();
            Integer i2 = aObject.getSystemId();

            if (ourSortOrder == ASC_ORDER) {
                return i1.compareTo(i2);
            }

            return i2.compareTo(i1);
        }

        case REQUESTID : {
            Integer i1 = this.getRequestId();
            Integer i2 = aObject.getRequestId();

            if (ourSortOrder == ASC_ORDER) {
                return i1.compareTo(i2);
            }

            return i2.compareTo(i1);
        }

        case CATEGORYID : {
            if (ourSortOrder == ASC_ORDER) {
                return this.getCategoryId().compareTo(aObject.getCategoryId());
            }

            return aObject.getCategoryId().compareTo(this.getCategoryId());
        }

        case STATUSID : {
            if (ourSortOrder == ASC_ORDER) {
                return this.getStatusId().compareTo(aObject.getStatusId());
            }

            return aObject.getStatusId().compareTo(this.getStatusId() );
        }

        case SEVERITYID : {
            if (ourSortOrder == ASC_ORDER) {
                return this.getSeverityId().compareTo(aObject.getSeverityId());
            }

            return aObject.getSeverityId().compareTo(this.getSeverityId());
        }

        case REQUESTTYPEID : {
            if (ourSortOrder == ASC_ORDER) {
                return this.getRequestTypeId().compareTo(aObject.getRequestTypeId());
            }

            return aObject.getRequestTypeId().compareTo(this.getRequestTypeId());
        }

        case SUBJECT : {
            if (ourSortOrder == ASC_ORDER) {
                return this.getSubject().compareTo(aObject.getSubject());
            }

            return aObject.getSubject().compareTo(this.getSubject());
        }

        case DESCRIPTION : {
            if (ourSortOrder == ASC_ORDER) {
                return this.getDescription().compareTo(aObject.getDescription());
            }

            return aObject.getDescription().compareTo(this.getDescription());
        }

        case ISPRIVATE : {
            Boolean b1 = this.getIsPrivate();
            Boolean b2 = aObject.getIsPrivate();

            if (ourSortOrder == ASC_ORDER) {
                return b1.compareTo(b2);
            }

            return b2.compareTo(b1);
        }

        case PARENTREQUESTID : {
            Integer i1 = this.getParentRequestId();
            Integer i2 = aObject.getParentRequestId();

            if (ourSortOrder == ASC_ORDER) {
                return i1.compareTo(i2);
            }

            return i2.compareTo(i1);
        }

        case USERID : {
            if (ourSortOrder == ASC_ORDER) {
                return this.getUserId().compareTo(aObject.getUserId());
            }

            return aObject.getUserId().compareTo(this.getUserId());
        }

        case MAXACTIONID : {
            Integer i1 = this.getMaxActionId();
            Integer i2 = aObject.getMaxActionId();

            if (ourSortOrder == ASC_ORDER) {
                return i1.compareTo(i2);
            }

            return i2.compareTo(i1);
        }

        case DUEDATE : {
            if (ourSortOrder == ASC_ORDER) {
                return this.getDueDate().compareTo(aObject.getDueDate());
            }

            return aObject.getDueDate().compareTo(this.getDueDate());
        }

        case LOGGEDDATE : {
            if (ourSortOrder == ASC_ORDER) {
                return this.getLoggedDate().compareTo(aObject.getLoggedDate());
            }

            return aObject.getLoggedDate().compareTo(this.getLoggedDate());
        }

        case LASTUPDATEDDATE : {
            if (ourSortOrder == ASC_ORDER) {
                return this.getLastUpdatedDate().compareTo(aObject.getLastUpdatedDate());
            }

            return aObject.getLastUpdatedDate().compareTo(this.getLastUpdatedDate());
        }

        case HEADERDESCRIPTION : {
            if (ourSortOrder == ASC_ORDER) {
                return this.getHeaderDescription().compareTo(aObject.getHeaderDescription());
            }

            return aObject.getHeaderDescription().compareTo(this.getHeaderDescription());
        }

        case ATTACHMENTS : {
        	String myAttStr = AttachmentInfo.toJson(this.getAttachments());
        	String aAttStr = AttachmentInfo.toJson(aObject.getAttachments()); 
            if (ourSortOrder == ASC_ORDER) {
                return myAttStr.compareTo(aAttStr);
            }
            return aAttStr.compareTo(myAttStr);
        }

        case SUMMARY : {
            if (ourSortOrder == ASC_ORDER) {
                return this.getSummary().compareTo(aObject.getSummary());
            }

            return aObject.getSummary().compareTo(this.getSummary());
        }

        case MEMO : {
            if (ourSortOrder == ASC_ORDER) {
                return this.getMemo().compareTo(aObject.getMemo());
            }

            return aObject.getMemo().compareTo(this.getMemo());
        }

        case APPENDINTERFACE : {
            Integer i1 = this.getAppendInterface();
            Integer i2 = aObject.getAppendInterface();

            if (ourSortOrder == ASC_ORDER) {
                return i1.compareTo(i2);
            }

            return i2.compareTo(i1);
        }

        case NOTIFY : {
            Boolean i1 = this.getNotify();
            Boolean i2 = aObject.getNotify();

            if (ourSortOrder == ASC_ORDER) {
                return i1.compareTo(i2);
            }

            return i2.compareTo(i1);
        }

        case NOTIFYLOGGERS : {
            Boolean b1 = this.getNotifyLoggers();
            Boolean b2 = aObject.getNotifyLoggers();

            if (ourSortOrder == ASC_ORDER) {
                return b1.compareTo(b2);
            }

            return b2.compareTo(b1);
        }

        case OFFICEID : {
            if (ourSortOrder == ASC_ORDER) {
                return this.getOfficeId().compareTo(aObject.getOfficeId());
            }

            return aObject.getOfficeId().compareTo(this.getOfficeId());
        }
        }

        return 0;
    }

    /**
     * 
     * @param systemId : the systemId of the BA
     * @param requestIdList : this is the comma separated list of request_ids which needs to be retrived.
     * 						  The maximum length of this string is 7999 characters ( limitation due to database )
     * @return : ArrayList of Request objects which where found in the db
     * @throws DatabaseException 
     * @throws SQLException 
     */
    public static ArrayList<Request> lookupBySystemIdAndRequestIdList(int systemId, ArrayList<Integer> reqIds ) throws DatabaseException
    {
    	ArrayList<Request> allRequests = new ArrayList<Request>() ;
    	if(null == reqIds )
    		throw new IllegalArgumentException("The list of requests cannot be null.");
    		
    	if( reqIds.size() == 0 )
    		return allRequests ;
    	
    	boolean first = true ;
    	String reqIdStr = "" ;
    	
    	for( Integer i : reqIds )
    	{
    		if( null == i )
    			throw new IllegalArgumentException("The request id cannot be null." );
    		
    		if( first == true )
    		{
    			first = false ;
    			reqIdStr += i ;
    		}
    		else
    		{
    			reqIdStr += "," + i ;
    		}   		
    	}
    	
    	return  lookupBySystemIdAndRequestIdList(systemId,reqIdStr);
    }

    /**
     * Do not make this as public as sql injection might be possible here.
     * @param systemId : the systemId of the BA
     * @param requestIdList : this is the comma separated list of request_ids which needs to be retrived.
     * 						  The maximum length of this string is 7999 characters ( limitation due to database )
     * @return : ArrayList of Request objects which where found in the db
     * @throws DatabaseException 
     * @throws SQLException 
     */
    public static ArrayList<Request> lookupBySystemIdAndRequestIdList(int systemId, String requestIdList ) throws DatabaseException
    {    	
    	ArrayList<Request> allRequests = new ArrayList<Request>() ;
    	if( null == requestIdList )
    		return allRequests ;
    	
    	Hashtable<Integer, Request> reqMap = new Hashtable<Integer,Request>() ;
    	Connection con = null ;
    	try
    	{
    		con = DataSourcePool.getConnection();
    		if( null != con )
    		{
    			CallableStatement cs = con.prepareCall("stp_request_lookupBySystemIdAndRequestIdList ? , ? ");
    			cs.setInt(1, systemId);
    			cs.setString(2, requestIdList);
    			ResultSet rs = cs.executeQuery() ;
    			// get All the requests
    			if( rs != null )
    			{
    				// create the Request objects from these result sets.
    				while ( rs.next() == true )
    				{
    					Request req = Request.createFromResultSet(rs);
    					if( null != req )
    					{
    						reqMap.put(req.getRequestId(), req);
    					}
    				}
    				
    				// now get more results for RequestUsers
    				// TODO : Nitiraj : how do i know if there are no rows returned from the select statement
    				// of this query ?. I think there will be non null resultSet but rs.next() will return false ?
    				while( cs.getMoreResults() == false ) 
    				{
    					// remove any update counts
    				}
    				
    				ResultSet reqUsersRS = cs.getResultSet();
    				if( null != reqUsersRS )
    				{
    					while( reqUsersRS.next() == true  )
						{
    						RequestUser reqUser = RequestUser.createFromResultSet(reqUsersRS);
    						Request req = reqMap.get(reqUser.getRequestId());
    						Field f = Field.lookupBySystemIdAndFieldId(systemId, reqUser.getFieldId());    						
    						if( null != req && null != f )
    						{
    							Collection<RequestUser> ruc = (Collection<RequestUser>) req.getObject(f);
    							if( null == ruc )
    								ruc = new ArrayList<RequestUser>() ;
    							
    							ruc.add(reqUser) ;
    							req.setObject(f, ruc);
//    							reqMap.put(reqUser.getRequestId(), req);
    						}
						}    						 
    				}
    				
    				while( cs.getMoreResults() == false ) 
    				{
    					// remove any update counts
    				}
    				
    				ResultSet reqExRS = cs.getResultSet() ;
    				if( null != reqExRS )
    				{
    					while( reqExRS.next() == true )
    					{
    						RequestEx rex = RequestEx.createFromResultSet(reqExRS);
    						if( null != rex )
    						{
    							Request req  = reqMap.get(rex.getRequestId());
        						if(null != req)
        						{
        							req.setRequestEx(rex);
        						}
    						}
    					}
    				}
    				
    				while( cs.getMoreResults() == false ) 
    				{
    					// remove any update counts
    				}
    				
    				ResultSet relReqRS = cs.getResultSet() ;
    				if( null != relReqRS )
    				{
    					while( relReqRS.next() == true )
    					{
    						
    						int psysId = relReqRS.getInt("primary_sys_id");
    						int prequestId = relReqRS.getInt("primary_request_id");
                        	int sysId = relReqRS.getInt("related_sys_id");
                        	int requestId = relReqRS.getInt("related_request_id");
                        	int actionId = relReqRS.getInt("related_action_id");                        	
                        	Request req = reqMap.get(prequestId);
                        	if( null != req )
                        	{
                        		
                        		ArrayList<RequestDataType> relatedRequests = (ArrayList<RequestDataType>) req.getObject(Field.RELATED_REQUESTS);
                        		if( null == relatedRequests )
                        			relatedRequests = new ArrayList<RequestDataType>();
                        		
                        		relatedRequests.add(new RequestDataType(sysId,requestId,actionId));
                        		req.setRelatedRequests(relatedRequests);
                        	}    	                    
    					}
    				}
    				allRequests.addAll(reqMap.values());
    			}
    		}
    	}
	   	catch(SQLException e )
    	{
    		e.printStackTrace();
    		throw new DatabaseException("Exception occured while retriving requests.", e);
    	}
	   	finally
	   	{
	   		if( null != con )
	   		{
	   			try {
					con.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
	   		}
	   	}

    	return  allRequests;
    }
 
    /*
     * This method creates an Request object from the resultset's current row.
     *
     * @param aRS Resultset object.
     *
     * @return Request Object.
     *
     * @exception SQLException incase of database errors.
     */
    public static Request createFromResultSet(ResultSet aRS) throws SQLException, DatabaseException {
        Integer systemId = aRS.getInt(Field.BUSINESS_AREA);        
        if(aRS.wasNull() )
        	throw new IllegalStateException( Field.BUSINESS_AREA + " of the request cannot be null.");
        
		Integer requestId = aRS.getInt(Field.REQUEST);
		if( aRS.wasNull() )
			throw new IllegalStateException(Field.REQUEST + " of the request cannot be null.");

		Integer userId = aRS.getInt(Field.USER);
		if( aRS.wasNull() )
			throw new IllegalStateException(Field.USER + " of the request cannot be null.");
	
		Integer maxActionId = aRS.getInt(Field.MAX_ACTION_ID) ;
		if( aRS.wasNull() )
			throw new IllegalStateException(Field.MAX_ACTION_ID + " of the request cannot be null.");
		
       	Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
		
    	Date loggedDate = aRS.getTimestamp(Field.LOGGED_DATE, cal) ;
    	if( null == loggedDate )
    		LOG.severe(Field.LOGGED_DATE + " of the request cannot be null. Ignoring this fact now but should be corrected ASAP.");
		Date lastUpdateDate = aRS.getTimestamp(Field.LASTUPDATED_DATE, cal) ;
		if( null == lastUpdateDate )
			LOG.severe(Field.LASTUPDATED_DATE + " of the request cannot be null. Ignoring this fact now but should be corrected ASAP.");
			
       	Integer catId = aRS.getInt(Field.CATEGORY) ;
       	Type categoryId = null;
       	if( !aRS.wasNull() )       	
       		categoryId = Type.lookupBySystemIdAndFieldNameAndTypeId(systemId, Field.CATEGORY, catId) ;
       	
       	Integer statId = aRS.getInt(Field.STATUS);
       	Type statusId = null ;
       	if( !aRS.wasNull() )
       		statusId = Type.lookupBySystemIdAndFieldNameAndTypeId(systemId, Field.STATUS, statId);
       	
       	Integer sevId = aRS.getInt(Field.SEVERITY) ;
       	Type severityId = null ;
       	if( !aRS.wasNull() )
       		severityId = Type.lookupBySystemIdAndFieldNameAndTypeId(systemId, Field.SEVERITY, sevId ) ;
       	
       	Integer reqTId = aRS.getInt(Field.REQUEST_TYPE);
       	Type reqTypeId = null ;
       	if( !aRS.wasNull() )
       		reqTypeId = Type.lookupBySystemIdAndFieldNameAndTypeId(systemId, Field.REQUEST_TYPE,reqTId);
       	
		String subject = aRS.getString(Field.SUBJECT) ;
		String description = aRS.getString(Field.DESCRIPTION) ;
				
		Boolean isPrivate = aRS.getBoolean(Field.IS_PRIVATE) ;
		if( aRS.wasNull() )
			isPrivate = null ;
		
		Integer parentId = aRS.getInt(Field.PARENT_REQUEST_ID) ;
		if( aRS.wasNull() )
			parentId = null ;	
		
		Date dueDate = aRS.getTimestamp(Field.DUE_DATE, cal) ;	
		String header = aRS.getString(Field.HEADER_DESCRIPTION) ;
		String attStr = aRS.getString(Field.ATTACHMENTS) ;		 
		Collection<AttachmentInfo> attachments = AttachmentInfo.fromJson(attStr);
		String summary = aRS.getString(Field.SUMMARY) ;
		String memo = aRS.getString(Field.MEMO) ;
		
		Integer appendInterface = aRS.getInt(Field.APPEND_INTERFACE) ;
		if( aRS.wasNull() )
			appendInterface = null ;
		
		Integer not = aRS.getInt(Field.NOTIFY) ;
		Boolean notify = null ;
		if( !aRS.wasNull() )
			notify = (not == 0 ? false : true ) ;
		
		Boolean notifyLoggers = aRS.getBoolean(Field.NOTIFY_LOGGERS) ;
		if( aRS.wasNull() )
			notifyLoggers = null ;
		
		Integer repliedToAction = aRS.getInt(Field.REPLIED_TO_ACTION) ;
		if(aRS.wasNull())
			repliedToAction = null ;
		
		Integer offId = aRS.getInt(Field.OFFICE);
		Type officeId = null ;
		if( !aRS.wasNull() )
			officeId =	Type.lookupBySystemIdAndFieldNameAndTypeId(systemId, Field.OFFICE, offId ) ;
			

		 Integer descriptionContentType = null ;
		 try
		 {
			 descriptionContentType = aRS.getInt("description_content_type");
		 }
		 catch(Exception e)
		 {
			 LOG.info("",(e));
		 }
		 if( null == descriptionContentType )
			 descriptionContentType = TBitsConstants.CONTENT_TYPE_HTML ;
			 
		 Integer summaryContentType = null ;
		 try
		 {
			 summaryContentType = aRS.getInt("summary_content_type");
		 }
		 catch(Exception e )
		 {
			 LOG.info("",(e));
		 }
		 if( null == summaryContentType )
			 summaryContentType = TBitsConstants.CONTENT_TYPE_HTML ;
		 
		 TextDataType desc = null ;
		 if( null != description )
		  desc = new TextDataType(description,descriptionContentType);
		 
		  TextDataType sum = null ;
		  if( null != summary )
		  sum = new TextDataType(summary,summaryContentType);

		 Request request = new Request(systemId, requestId, categoryId, statusId, severityId, reqTypeId, subject, desc, isPrivate, parentId, userId, maxActionId, dueDate, loggedDate, lastUpdateDate, header, attachments, sum, memo, appendInterface,notify, notifyLoggers, repliedToAction, officeId);
		 
		 return request;
    }

    /**
     * Method to insert a Request object into database.
     *
     * @param aObject Object to be inserted
     */
    public static boolean update(Request aObject) throws DatabaseException{
    	
        // Insert logic here.
        if (aObject == null) {
            return false;
        }

        int        requestId   = 0;
        Connection aCon        = null;
        boolean    returnValue = false;

        try {
            aCon = DataSourcePool.getConnection();

            // Enter into transaction
            // mode and set autocommit to false.
            // This will allow us to rollback incase of any errors during the
            // process.
            //
            aCon.setAutoCommit(false);

            update(aObject, aCon);

            returnValue = true;
            aCon.commit();
        } catch (SQLException sqle) {
        	try {
        		if(aCon != null)
					aCon.rollback();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
            StringBuilder message = new StringBuilder();

            message.append("An exception occurred while updating a request.").append("\nSystem Id: ").append(aObject.getSystemId()).append("\nRequestId: ").append(aObject.getRequestId()).append(
                "\n");

            // Rollback the transaction if the connection is not null.
            try {
                LOG.debug("Rolling back the transaction.");

                if (aCon != null) {
                    aCon.rollback();
                }
            } catch (SQLException sql) {
                message.append("An exception occurred while rolling back the ").append("transaction while inserting a request.").append("Exception: ").append(TBitsLogger.getStackTrace(sql)).append(
                    "\n");
            }

            throw new DatabaseException(message.toString(), sqle);
        } finally {
            if (aCon != null) {
                try {
                    aCon.close();
                } catch (SQLException sqle) {

                    // Should this be logged.?
                }
            }
        }
        return returnValue;
    }
    /**
     * Method to insert a Request object into database.
     *
     * @param aObject Object to be inserted
     */
    public static boolean insert(Request aObject) throws DatabaseException{
    	
        // Insert logic here.
        if (aObject == null) {
            return false;
        }

        int        requestId   = 0;
        Connection aCon        = null;
        boolean    returnValue = false;

        try {
            aCon = DataSourcePool.getConnection();

            // Enter into transaction
            // mode and set autocommit to false.
            // This will allow us to rollback incase of any errors during the
            // process.
            //
            aCon.setAutoCommit(false);

            insert(aObject, aCon);

            returnValue = true;
            aCon.commit();
        } catch (SQLException sqle) {
        	try {
        		if(aCon != null)
					aCon.rollback();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
            StringBuilder message = new StringBuilder();

            message.append("An exception occurred while inserting a request.").append("\nSystem Id: ").append(aObject.getSystemId()).append("\nRequestId: ").append(aObject.getRequestId()).append(
                "\n");

            // Rollback the transaction if the connection is not null.
            try {
                LOG.debug("Rolling back the transaction.");

                if (aCon != null) {
                    aCon.rollback();
                }
            } catch (SQLException sql) {
                message.append("An exception occurred while rolling back the ").append("transaction while inserting a request.").append("Exception: ").append(TBitsLogger.getStackTrace(sql)).append(
                    "\n");
            }

            throw new DatabaseException(message.toString(), sqle);
        } finally {
            if (aCon != null) {
                try {
                    aCon.close();
                } catch (SQLException sqle) {

                    // Should this be logged.?
                }
            }
        }
        return returnValue;
    }

	public static void insert(Request request, Connection aCon) throws DatabaseException
	{
		int requestId;
		validateStateForInsertion(request);
		try
		{
			//
			// Insert the record into the requests table. This procedure will
			// insert the corresponding action record also.
			//
			// Get and increment
			requestId = Request.getAndIncrement(request.getSystemId());
			request.setRequestId(requestId);
			CallableStatement cs = aCon.prepareCall("stp_request_insert "
					+ "?, ?, ?, ?, ?, ?, ?, ?, ?, ?, "
					+ "?, ?, ?, ?, ?, ?, ?, ?, ?, ?, " + "?, ?, ?,?,?,?");
			request.setCallableParameters(cs);
			cs.execute();
		}
		catch(SQLException sqle)
		{
			sqle.printStackTrace() ;
			throw new DatabaseException("Unable to insert the request", sqle);
		}
		
		try
		{
			// Generate the Batch to insert the request users.
			
			try {
				// Insert the request users by generating a preparedstatement.
				ArrayList<GenericValue> params = new ArrayList<GenericValue>();
				String reqUserBatch = getRequestUserBatch(request, OP_INSERT, params);
				if( null != reqUserBatch && !reqUserBatch.trim().equals(""))
				{
					PreparedStatement ps = aCon.prepareStatement(reqUserBatch);
					GenericValue.setParametersPS(ps, params);
					ps.execute();
				}
			} catch (DatabaseException de) {
				de.printStackTrace() ;
				throw de;
			}
			try {
				//Insert the extended fields by generating a prepared statements
				ArrayList<GenericValue> params = new ArrayList<GenericValue>();
				String reqExBatch = getRequestExBatch(request, OP_INSERT, params);
				if( reqExBatch != null && !reqExBatch.trim().equals(""))
				{
					PreparedStatement ps = aCon.prepareStatement(reqExBatch);
					GenericValue.setParametersPS(ps, params);
					ps.execute();
				}
				
			} catch (DatabaseException de) {
				de.printStackTrace() ;
				throw de;
			}
		}
		catch(SQLException sqle)
		{
			sqle.printStackTrace() ;
			throw new DatabaseException("Unable to insert request users and extended fields.", sqle);
		}
	}

    private static void validateStateForInsertion(Request request) 
    {
    	disAllowNull(request.getSystemId());
    	disAllowNull(request.getRequestId());
    	disAllowNull(request.getUserId());
    	disAllowNull(request.getMaxActionId());
    	disAllowNull(request.getLastUpdatedDate());
    	disAllowNull(request.getLoggedDate());
	}

	public static int getAndIncrement(int systemId) throws DatabaseException {
		// TODO Auto-generated method stub
		Connection connection = null;
		try
		{
			connection = DataSourcePool.getConnection();
			CallableStatement cs = connection.prepareCall("stp_ba_incrAndGetRequestId ?");
			cs.setInt(1, systemId);
			ResultSet rs = cs.executeQuery();
			if(rs.next())
			{
				int n = rs.getInt(1);
				rs.close();
				cs.close();
				return n;
			}
			else
			{
				throw new DatabaseException("Unable to get the id.", null);
			}
		}
		catch(SQLException sqle)
		{
			throw new DatabaseException("Unable to get the id.", sqle);
		}
		finally
		{
			if(connection != null)
			{
				try
				{
					connection.close();
				}
				catch (Exception e) {
					// TODO: handle exception
					LOG.warn("Unable to close the connection while getting and incrementing the requestid");
					e.printStackTrace();
				}
			}
		}
	}

	/**
     * Method to insert Related requests Batch.
     */
    public static void insertRelatedRequests(String aBatch) throws Exception {
		Connection connection = null;
		try {
			connection = DataSourcePool.getConnection();
			connection.setAutoCommit(false);
			insertRelatedRequests(connection, aBatch);
			connection.commit();
		} catch (SQLException sqle) {
			try {
        		if(connection != null)
					connection.rollback();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			throw sqle;
		} finally {
			if (connection != null) {
				try {
					connection.close();
				} catch (SQLException sqle) {
					LOG.warning("Exception occurred while closing the connection.");
				}
			}
			connection = null;
		}
	}
    
    public static void insertRelatedRequests(Connection connection, String aBatch) throws SQLException {
            Statement stmt = connection.createStatement();
            stmt.addBatch(aBatch);
            stmt.executeBatch();
            stmt.close();
            stmt = null;
    }

    /**
     */
    public static int lookupBySystemIdAndRequestData(int aSystemId, Request aRequest) throws Exception {
    	Connection connection = null;
    	
    	try {
    		 connection = DataSourcePool.getConnection();
    		 connection.setAutoCommit(false);
    		 int retValue = lookupBySystemIdAndRequestData(connection, aSystemId, aRequest);
    		 connection.commit();
    		 return retValue;
		} catch (SQLException sqle) {
			throw sqle;
		} finally {
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException sqle) {
                    LOG.warning("Exception occurred while closing the connection.");
                }
            }
            connection = null;
        }
    }
    public static int lookupBySystemIdAndRequestData(Connection connection, int aSystemId, Request aRequest) throws Exception {
        int        requestId  = -1;
        try {
            CallableStatement cs = connection.prepareCall("stp_request_lookupBySystemIdAndRequestData ?, ?, ?, ?");

            cs.setInt(1, aSystemId);
            cs.setInt(2, aRequest.getUserId());
            cs.setString(3, aRequest.getSubject());
            cs.setTimestamp(4, Timestamp.toSqlTimestamp(aRequest.getLastUpdatedDate()));

            ResultSet rs = cs.executeQuery();

            if (rs != null) {
                if (rs.next() != false) {
                    requestId = rs.getInt("request_id");
                }

                rs.close();
            }

            cs.close();
            rs = null;
            cs = null;
        } catch (SQLException sqle) {
            throw sqle;
        } 
        return requestId;
    }

    public static Request lookupBySystemIdAndRequestId(int aSystemId, int aRequestId) throws DatabaseException
    {
    	Connection connection = null;
		try {
			connection = DataSourcePool.getConnection();
			return lookupBySystemIdAndRequestId(connection, aSystemId, aRequestId);
		} catch (SQLException sqle) {
			StringBuilder message = new StringBuilder();

			message.append("An exception while retrieving the request.")
					.append("\nSystem Id  : ").append(aSystemId).append(
							"\nRequest Id : ").append(aRequestId).append("\n");

			throw new DatabaseException(message.toString(), sqle);
		} finally {
			if (connection != null) {
				try {
					connection.close();
				} catch (SQLException sqle) {
					LOG.warn("Exception while closing the connection:", sqle);
				}

				connection = null;
			}
		}
    }
    
    /**
     * This method returns the Request object corresponding to the given
     * Request Id.
     *
     * @param aSystemId  System Id.
     * @param aRequestId Request Id.
     *
     * @return Request object corresponding to this Request Id.
     *
     * @exception DatabaseException incase of any error while interacting with
     *            the database.
     */
    public static Request lookupBySystemIdAndRequestId(Connection connection, int aSystemId, int aRequestId) throws DatabaseException{
        BusinessArea                ba              = BusinessArea.lookupBySystemId(aSystemId);
        String                      prefix          = ba.getSystemPrefix() + "#";
        Request                     request         = null;
        ArrayList<RequestUser>      logList         = new ArrayList<RequestUser>();
        ArrayList<RequestUser>      assList         = new ArrayList<RequestUser>();
        ArrayList<RequestUser>      subList         = new ArrayList<RequestUser>();
        ArrayList<RequestUser>      toList          = new ArrayList<RequestUser>();
        ArrayList<RequestUser>      ccList          = new ArrayList<RequestUser>();
        ArrayList<RequestDataType> relatedRequests = new ArrayList<RequestDataType>() ;
        Hashtable<Field, RequestEx> reqExList       = new Hashtable<Field, RequestEx>();
        ArrayList<RequestUser>      userTypeList    = new ArrayList<RequestUser>();
        
        try {
            CallableStatement cs = connection.prepareCall("stp_request_lookupBySystemIdAndRequestId ?, ?");

            cs.setInt(1, aSystemId);
            cs.setInt(2, aRequestId);

            boolean flag = cs.execute();

            //
            // This flag should be true, because, the underlying stored
            // procedure returns three result sets which contains
            // - Request Information.
            // - Request user Information.
            // - Request Ex Information.
            // - RelatedRequests Information.
            //
            if (flag == true) {
                ResultSet rsRequest = cs.getResultSet();

                if ((rsRequest != null) && (rsRequest.next() != false)) {
                    request = createFromResultSet(rsRequest);

                    //
                    // Do not close rsRequest. Because, a call to
                    // cs.getMoreResults() closes this internally. If we close
                    // the result set, then call to cs.getMoreResults() throws
                    // an SQLException.
                    //
                    cs.getMoreResults();

                    ResultSet rsReqUser = cs.getResultSet();

                    if (rsReqUser != null) {
                        while (rsReqUser.next() != false) {
                            RequestUser reqUser = RequestUser.createFromResultSet(rsReqUser);

                            switch (reqUser.getUserTypeId()) {
                            case UserType.LOGGER :
                                logList.add(reqUser);

                                break;

                            case UserType.ASSIGNEE :
                                assList.add(reqUser);

                                break;

                            case UserType.SUBSCRIBER :
                                subList.add(reqUser);

                                break;

                            case UserType.TO :
                                toList.add(reqUser);

                                break;

                            case UserType.CC :
                                ccList.add(reqUser);
                                break;
                            case UserType.USERTYPE:
                                 userTypeList.add(reqUser);
                            }
                        }

                        request.setLoggers(logList);
                        request.setAssignees(assList);
                        request.setSubscribers(subList);
                        request.setTos(toList);
                        request.setCcs(ccList);
                        request.setExUserTypes(userTypeList);                
                    }

                    // Again, no need to close the previous result set.
                    cs.getMoreResults();

                    ResultSet rsReqEx = cs.getResultSet();

                    if (rsReqEx != null) {
                        while (rsReqEx.next() != false) {
                            RequestEx reqEx   = RequestEx.createFromResultSet(rsReqEx);
                            int       fieldId = rsReqEx.getInt("field_id");
                            Field     field   = Field.lookupBySystemIdAndFieldId(aSystemId, fieldId);
                            
                            if (field == null) {
                                continue;
                            }

                            reqExList.put(field, reqEx);
                        }
                    }

                    request.setExtendedFields(reqExList);

                    // Again, no need to close the previous result set.
                    // Get RelatedRequests
                    // Skip all the update counts.
                    while ((cs.getMoreResults() == false) && (cs.getUpdateCount() != -1));

                    ResultSet relReq = cs.getResultSet();

                    if (relReq != null) 
                    {
                        while (relReq.next() != false) 
                        {
                        	int sysId = relReq.getInt("related_sys_id");
                        	int requestId = relReq.getInt("related_request_id");
                        	int actionId = relReq.getInt("related_action_id");
                        	relatedRequests.add(new RequestDataType(sysId,requestId,actionId));
                        }
                    }

                    request.setRelatedRequests(relatedRequests);
                }
            }

            // Close the statement.
            cs.close();

            // Release the memory by nullifying the references so that these
            // are recovered by the Garbage collector.
            cs = null;
        } catch (SQLException sqle) {
        	sqle.printStackTrace() ;
            StringBuilder message = new StringBuilder();
            message.append("An exception while retrieving the request.").append("\nSystem Id  : ").append(aSystemId).append("\nRequest Id : ").append(aRequestId).append("\n");
            throw new DatabaseException(message.toString(), sqle);
        }
        return request;
    }
    
    
    
    /**
     * This method returns the Request object corresponding to the given
     * Request Id for Viewing Request info.
     *
     * @param aSystemId  System Id.
     * @param aRequestId Request Id.
     *
     * @return Request object corresponding to this Request Id.
     *
     * @exception DatabaseException incase of any error while interacting with
     *            the database.
     */
//    public static Request lookupBySystemIdAndRequestIdForViewRequest(int aSystemId, int aRequestId) throws DatabaseException{
//    	Connection conn = null;
//		try {
//			conn = DataSourcePool.getConnection();
//			return lookupBySystemIdAndRequestIdForViewRequest(conn, aSystemId,
//					aRequestId);
//		} catch (SQLException e) {
//			throw new DatabaseException("Error while getting all the actions.",
//					e);
//		} finally {
//			try {
//				if ((conn != null) && !conn.isClosed()) {
//					conn.close();
//				}
//			} catch (SQLException e) {
//				LOG.error(new Exception(
//						"Unable to close the connection to the database.", e));
//			}
//		}
//	}
    
//    public static Request lookupBySystemIdAndRequestIdForViewRequest(Connection connection, int aSystemId, int aRequestId) throws DatabaseException{
//        BusinessArea                ba              = BusinessArea.lookupBySystemId(aSystemId);
//        String                      prefix          = ba.getSystemPrefix() + "#";
//        Request                     request         = null;
//        ArrayList<RequestUser>      logList         = new ArrayList<RequestUser>();
//        ArrayList<RequestUser>      assList         = new ArrayList<RequestUser>();
//        ArrayList<RequestUser>      subList         = new ArrayList<RequestUser>();
//        ArrayList<RequestUser>      toList          = new ArrayList<RequestUser>();
//        ArrayList<RequestUser>      ccList          = new ArrayList<RequestUser>();
//        ArrayList<RequestUser>      userTypeList    = new ArrayList<RequestUser>();
//        Hashtable<String, String>   subRequests     = new Hashtable<String, String>();
//        Hashtable<String, String>   siblingRequests = new Hashtable<String, String>();
//        Hashtable<String, String>   relatedRequests = new Hashtable<String, String>();
//        Hashtable<String, String>   parentRequests  = new Hashtable<String, String>();
//        Hashtable<Field, RequestEx> reqExList       = new Hashtable<Field, RequestEx>();
//        
//       // Connection                  connection      = null;
//
//        try {
//            //connection = DataSourcePool.getConnection();
//            CallableStatement cs = connection.prepareCall("stp_request_lookupBySystemIdAndRequestIdForViewRequest ?,?");
//            cs.setInt(1, aSystemId);
//            cs.setInt(2, aRequestId);
//
//            boolean flag = cs.execute();
//
//            //
//            // This flag should be true, because, the underlying stored
//            // procedure returns following result sets which contains
//            // - Request Information.
//            // - Request user Information.
//            // - Request Ex Information.
//            // - SubRequests Information.
//            // - SiblingRequests Information.
//            // - RelatedRequests Information.
//            //
//            if (flag == true) {
//                ResultSet rsRequest = cs.getResultSet();
//
//                if ((rsRequest != null) && (rsRequest.next() != false)) {
//                    request = createFromResultSet(rsRequest);
//
//                    //
//                    // Do not close rsRequest. Because, a call to
//                    // cs.getMoreResults() closes this internally. If we close
//                    // the result set, then call to cs.getMoreResults() throws
//                    // an SQLException.
//                    //
//                    cs.getMoreResults();
//
//                    ResultSet rsReqUser = cs.getResultSet();
//                    int uFieldId=0;
//                    if (rsReqUser != null) {
//                        while (rsReqUser.next() != false) {
//                            RequestUser reqUser = RequestUser.createFromResultSet(rsReqUser);
//
//                            switch (reqUser.getUserTypeId()) {
//                            case UserType.LOGGER :
//                                logList.add(reqUser);
//
//                                break;
//
//                            case UserType.ASSIGNEE :
//                                assList.add(reqUser);
//
//                                break;
//
//                            case UserType.SUBSCRIBER :
//                                subList.add(reqUser);
//
//                                break;
//
//                            case UserType.TO :
//                                toList.add(reqUser);
//
//                                break;
//
//                            case UserType.CC :
//                                ccList.add(reqUser);
//                                break;
//                            case UserType.USERTYPE :
//                            	userTypeList.add(reqUser);
//                                break;
//                            }
//                        }
//
//                        request.setLoggers(logList);
//                        request.setAssignees(assList);
//                        request.setSubscribers(subList);
//                        request.setTos(toList);
//                        request.setCcs(ccList);
//                        request.setExUserTypes(userTypeList);
//                        
//                        
//                    
//                    }
//                    
//                    // Again, no need to close the previous result set.
//                    cs.getMoreResults();
//
//                    ResultSet rsReqEx = cs.getResultSet();
//
//                    if (rsReqEx != null) {
//                        while (rsReqEx.next() != false) {
//                            RequestEx reqEx   = RequestEx.createFromResultSet(rsReqEx);
//                            int       fieldId = rsReqEx.getInt("field_id");
//                            Field     field   = Field.lookupBySystemIdAndFieldId(aSystemId, fieldId);
//
//                            if (field == null) {
//                                continue;
//                            }
//
//                            reqExList.put(field, reqEx);
//                        }
//                    }
//
//                    request.setExtendedFields(reqExList);
//
//                    // Again, no need to close the previous result set.
//                    // Get RelatedRequests
//                    // Skip all the update counts.
//                    while ((cs.getMoreResults() == false) && (cs.getUpdateCount() != -1));
//
//                    ResultSet relReq = cs.getResultSet();
//
//                    if (relReq != null) 
//                    {
//                        while (relReq.next() != false) 
//                        {
//                        	int sysId = relReq.getInt("related_sys_id") ;                        	
//                        	int reqId = relReq.getInt("related_request_id");
//                        	int actionId = relReq.getInt("related_action_id");
//                            relatedRequests.
//                        }
//
//                        relReq.close();
//                    }
//
//                    request.setRelatedRequests(relatedRequests);
//
//                    //
//                    // Again, no need to close the previous result set.
//                    // Get Parent Requests
//                    //
//                    while ((cs.getMoreResults() == false) && (cs.getUpdateCount() != -1));
//
//                    ResultSet parReq = cs.getResultSet();
//
//                    if (parReq != null) {
//                        while (parReq.next() != false) {
//                            parentRequests.put(prefix + parReq.getInt("request_id"), parReq.getString("subject"));
//                        }
//                    }
//
//                    request.setParentRequests(parentRequests);
//
//                    // This will close the previous resultset.
//                    cs.getMoreResults();
//
//                    // We are no more interested on any thing that follows.
//                }
//            }
//
//            // Close the statement.
//            cs.close();
//
//            //
//            // Release the memory by nullifying the references so that these
//            // are recovered by the Garbage collector.
//            //
//            cs = null;
//        } catch (SQLException sqle) {
//            StringBuilder message = new StringBuilder();
//
//            message.append("An exception while retrieving the request.").append("\nSystem Id  : ").append(aSystemId).append("\nRequest Id : ").append(aRequestId).append("\n");
//
//            throw new DatabaseException(message.toString(), sqle);
//        } 
////        finally {
////            if (connection != null) {
////                try {
////                    connection.close();
////                } catch (SQLException sqle) {
////                    LOG.warn("Exception while closing the connection:", sqle);
////                }
////
////                connection = null;
////            }
////        }
//
//        return request;
//    }

    public static String lookupSubject(String aSysPrefix, int aRequestId, int aUserId, boolean aEmail) throws Exception {
    	Connection conn = null;
		try {
			conn = DataSourcePool.getConnection();
			return lookupSubject(conn, aSysPrefix, aRequestId, aUserId, aEmail);
		} catch (SQLException e) {
			throw new DatabaseException("Error while getting all the actions.", e);
		} finally {
			try {
				if ((conn != null) && !conn.isClosed()) {
					conn.close();
				}
			} catch (SQLException e) {
				LOG.error(new Exception(
						"Unable to close the connection to the database.", e));
			}
		}
    }
    public static String lookupSubject(Connection conn, String aSysPrefix, int aRequestId, int aUserId, boolean aEmail) throws Exception {
        BusinessArea ba = null;

        try {
            ba = BusinessArea.lookupBySystemPrefix(aSysPrefix);
        } catch (DatabaseException dbe) {
            LOG.severe("",(dbe));

            return "";
        }

        if (ba == null) {
            return "";
        }

        int        systemId   = ba.getSystemId();
        String     subject    = null;
        Connection connection = null;

        try {
            connection = DataSourcePool.getConnection();

            CallableStatement cs = connection.prepareCall("stp_request_lookupsubject ?, ?, ?, ?");

            cs.setInt(1, systemId);
            cs.setInt(2, aRequestId);
            cs.setInt(3, aUserId);
            cs.setBoolean(4, aEmail);

            boolean flag = cs.execute();

            if (flag == true) {
                ResultSet rs = cs.executeQuery();

                if (rs != null) {
                    if (rs.next() != false) {
                        subject = rs.getString("subject");
                    }

                    rs.close();
                }

                cs.close();
                rs = null;
                cs = null;
            }
        } catch (SQLException sqle) {
            throw sqle;
        } finally {
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException sqle) {
                    LOG.warning("Exception occurred while closing the connection.");
                }
            }

            connection = null;
        }

        return subject;
    }

    /**
     * Method to return the source arraylist in the sorted order
     *
     * @param  source the array list of Type objects
     * @return the ArrayList of the Request objects in sorted order
     */
    public static ArrayList<Request> sort(ArrayList<Request> source) {
        int       size     = source.size();
        Request[] srcArray = new Request[size];

        for (int i = 0; i < size; i++) {
            srcArray[i] = source.get(i);
        }

        Arrays.sort(srcArray);

        ArrayList<Request> target = new ArrayList<Request>();

        for (int i = 0; i < size; i++) {
            target.add(srcArray[i]);
        }

        return target;
    }

    /**
     * This method returns the String Representation of object of this class.
     *
     * @return String representation.
     */
    public String toString() {
        StringBuilder message = new StringBuilder();

        message.append("\nSystem  Id    : ").append(this.getSystemId()).append("\nRequest Id    : ").append(this.getRequestId()).append("\nCategory      : ").append(this.getCategoryId()).append("\nStatus        : ").append(
            this.getStatusId()).append("\nSeverity      : ").append(this.getSeverityId()).append("\nRequest Type  : ").append(this.getRequestTypeId()).append("\nSubject       : ").append(this.getSubject()).append(
            "\nPrivate       : ").append(this.getIsPrivate()).append("\nParent Id     : ").append(this.getParentRequestId()).append("\nUser          : ").append(this.getUserId()).append("\nMax Action Id : ").append(
            this.getMaxActionId()).append("\nLogged Date   : ").append(Timestamp.toDateMin(this.getLoggedDate())).append("\nUpdated Date  : ").append(Timestamp.toDateMin(this.getLastUpdatedDate())).append("\nAttachments   : ").append(
            this.getAttachments()).append("\nAppend        : ").append(this.getAppendInterface()).append("\nNotify        : ").append(this.getNotify()).append("\nNotify Logger : ").append(this.getNotifyLoggers()).append(
            "\nOfficeId      : ").append(this.getOfficeId());

        return message.toString();
    }

    /**
     * Method to update the corresponding Request object in the database.
     *
     * @param aObject Object to be updated
     *
     * @return Update domain object.
     *
     * @exception DatabaseException Incase of any database errors.
     */
    public static Request update(Request aObject, Connection aCon) throws DatabaseException{

        // Update logic here.
        if (aObject == null) {
            return aObject;
        }

        int        actionId = aObject.getMaxActionId();

        try {
            //
            // Enter into transaction mode and set autocommit to false.
            // This will allow us to rollback incase of any errors during the
            // process.
            //
           //aCon.setAutoCommit(false);

            //
            // Update the record in the requests table. This procedure will
            // insert the corresponding action record also.
            //
            CallableStatement cs = aCon.prepareCall("stp_request_update " + "?, ?, ?, ?, ?, ?, ?, ?, ?, ?, " + "?, ?, ?, ?, ?, ?, ?, ?, ?, ?, " + "?, ?, ?, ?, ?, ?");

            // Register actionId as out parameter.
            cs.registerOutParameter(12, Types.INTEGER);
            aObject.setCallableParameters(cs);
            cs.execute();

            // Read the action id.
            actionId = cs.getInt(12);
            cs.close();
            cs = null;

            //
            // Set the action id of the request to the value obtained from
            // the callable statement which is an OUT parameter.
            //
            aObject.setMaxActionId(actionId);

            
            try {
				ArrayList<GenericValue> params = new ArrayList<GenericValue>();
				String reqUserBatch = getRequestUserBatch(aObject, OP_UPDATE, params);
				if(reqUserBatch.trim().length() > 0)
				{
					PreparedStatement ps = aCon.prepareStatement(reqUserBatch);
					GenericValue.setParametersPS(ps, params);
					ps.execute();
				}
			} catch (DatabaseException de) {
				de.printStackTrace() ;
				throw de;
			}
			try {
				ArrayList<GenericValue> params = new ArrayList<GenericValue>();
				String reqExBatch = getRequestExBatch(aObject, OP_UPDATE, params);
				if(reqExBatch.trim().length() != 0)
				{
					PreparedStatement ps = aCon.prepareStatement(reqExBatch);
					GenericValue.setParametersPS(ps, params);
					ps.execute();
				}
				
			} catch (DatabaseException de) {
				de.printStackTrace() ;
				throw de;
			}
        } catch (SQLException sqle) {
            StringBuilder message = new StringBuilder();
            message.append("An exception occurred while updating a request.").append("\nSystem Id: ").append(aObject.getSystemId()).append("\nRequestId: ").append(aObject.getRequestId()).append("\n");
            throw new DatabaseException(message.toString(), sqle);
        }
        return aObject;
    }

    /**
     * Method to update the corresponding Request object in the database.
     *
     * @param aSystemId    Business Area Id.
     * @param aRequestId   Request Id.
     * @param aAttachments Attachment names.
     *
     * @return Update domain object.
     *
     * @exception DatabaseException Incase of any database errors.
     */
    public static boolean updateAttachments(int aSystemId, int aRequestId, int aActionId, String aAttachments) throws DatabaseException {
    	  Connection aCon = null;
    	  try {
    		  aCon = DataSourcePool.getConnection();
    		  aCon.setAutoCommit(false);
    		   boolean returnValue = updateAttachments(aCon, aSystemId, aRequestId, aActionId, aAttachments);
    		   aCon.commit() ;
    		   return returnValue ;
    		   
    	  }
    	  catch (SQLException sqle) {
			try {
				if (aCon != null)
					aCon.rollback();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			StringBuilder message = new StringBuilder();
			message.append("An exception occurred while updating attachments.")
					.append("\nSystem Id  : ").append(aSystemId).append(
							"\nRequestId  : ").append(aRequestId).append(
							"\nAction Id  : ").append(aActionId).append(
							"\nAttachments: ").append(aAttachments)
					.append("\n");
              throw new DatabaseException(message.toString(), sqle);
    	  }
    	  catch (DatabaseException dbe) {
    	  if( aCon != null )
			try {
				aCon.rollback();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
              throw dbe;
          } finally {
              if (aCon != null) {
                  try {
                      aCon.close();
                  } catch (SQLException sqle) {
                     sqle.printStackTrace();
                  }
              }
          }

    }
    public static boolean updateAttachments(Connection aCon, int aSystemId, int aRequestId, int aActionId, String aAttachments) throws DatabaseException {
        // Update the attachments column of the given request record.
        try {
            CallableStatement cs = aCon.prepareCall("stp_request_updateAttachments ?, ?, ?, ? ");

            cs.setInt(1, aSystemId);
            cs.setInt(2, aRequestId);
            cs.setInt(3, aActionId);
            cs.setString(4, aAttachments);
            cs.execute();
            cs.close();
            cs = null;
        } catch (SQLException sqle) {
            StringBuilder message = new StringBuilder();
            message.append("An exception occurred while updating attachments.").append("\nSystem Id  : ").append(aSystemId).append("\nRequestId  : ").append(aRequestId).append(
                "\nAction Id  : ").append(aActionId).append("\nAttachments: ").append(aAttachments).append("\n");
            throw new DatabaseException(message.toString(), sqle);
        } 
        return true;
    }
    /*
     * Updates the attachments info on the extended field of type attachment
     */
    public static boolean updateAttachmentsExt(Connection aCon, int aSystemId, int aRequestId,int aFieldId, int aActionId, String aAttachments) throws DatabaseException {
        // Update the attachments column of the given request record.
        try {
            CallableStatement cs = aCon.prepareCall("stp_request_updateAttachments_ex ?, ?, ?, ?,? ");

            cs.setInt(1, aSystemId);
            cs.setInt(2, aRequestId);
            cs.setInt(3, aActionId);
            cs.setInt(4, aFieldId);
            cs.setString(5, aAttachments);
            cs.execute();
            cs.close();
            cs = null;
        } catch (SQLException sqle) {
            StringBuilder message = new StringBuilder();
            message.append("An exception occurred while updating attachments.").append("\nSystem Id  : ").append(aSystemId).append("\nRequestId  : ").append(aRequestId).append(
                "\nAction Id  : ").append(aActionId).append("\nAttachments: ").append(aAttachments).append("\n");
            throw new DatabaseException(message.toString(), sqle);
        } 
        return true;
    }

    /**
     * Method to update the corresponding Request object in the database.
     *
     * @param aSystemId    Business Area Id.
     * @param aRequestId   Request Id.
     * @param aActionId    Action Id
     * @param aHeaderDesc  Header Description.
     *
     * @return Update domain object status.
     *
     * @exception DatabaseException Incase of any database errors.
     */
    public static boolean updateHeaderDesc(int aSystemId, int aRequestId, int aActionId, String aHeaderDesc) throws DatabaseException {

        // Update the Header description column of the given request record.
        Connection aCon = null;

        try {
            aCon = DataSourcePool.getConnection();
            aCon.setAutoCommit(false);

            CallableStatement cs = aCon.prepareCall("stp_request_updateHeaderDesc ?, ?, ?, ? ");

            cs.setInt(1, aSystemId);
            cs.setInt(2, aRequestId);
            cs.setInt(3, aActionId);
            cs.setString(4, aHeaderDesc);
            cs.execute();
            cs.close();
            cs = null;
            aCon.commit();
        } catch (SQLException sqle) {
        	try {
        		if(aCon != null)
					aCon.rollback();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
            StringBuilder message = new StringBuilder();

            message.append("An exception occurred while updating header desc").append("\nSystem Id  : ").append(aSystemId).append("\nRequestId  : ").append(aRequestId).append(
                "\nAction Id  : ").append(aActionId).append("\nHeaderDesc : ").append(aHeaderDesc).append("\n");

            throw new DatabaseException(message.toString(), sqle);
        } finally {
            if (aCon != null) {
                try {
                    aCon.close();
                } catch (SQLException sqle) {

                    // Should this be logged.?
                }
            }
        }

        return true;
    }

    //~--- get methods --------------------------------------------------------

    public String get(String fieldName) 
    {
    	disAllowNull(fieldName);
    	
//        String value = myMapFieldToValues.get(fieldName);
//        if(null == value )
//        	value = "";
//        return value ;
    	
    	Field field = null ;
		try 
		{
			field = Field.lookupBySystemIdAndFieldName(this.getSystemId(), fieldName);
		} catch (DatabaseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	if( null == field )
    		throw new IllegalArgumentException("Field not found with name : " + fieldName );
    	
    	Object obj = this.getObject(fieldName);
    	
    	if( null == obj )
    		return null ;
    	
    	switch(field.getDataTypeId())
    	{   		
    		case DataType.USERTYPE :
    		{
    			Collection<RequestUser> reqUsers = (Collection<RequestUser>) obj ;
    			return APIUtil.toLoginList(reqUsers);
    		}
    		case DataType.ATTACHMENTS :
    		{
    			Collection<AttachmentInfo> attInfos = (Collection<AttachmentInfo>) obj ;
    			return AttachmentInfo.toJson(attInfos);
    		}
    		case DataType.DATE:
    		{
    			Date d = (Date) obj ;
    			return Timestamp.toCustomFormat(d, TBitsConstants.API_DATE_ONLY_FORMAT);
    		}
    		case DataType.DATETIME :
    		{
    			Date d = (Date) obj; 
    			return Timestamp.toCustomFormat(d, TBitsConstants.API_DATE_FORMAT);
    		}
    		case DataType.TIME :
    		{
    			Date d = (Date) obj ;
    			return Timestamp.toCustomFormat(d, TBitsConstants.API_TIME_FORMAT);
    		}
    		case DataType.TYPE :
    		{
    			Type type = (Type) obj;
    			return type.getName() ;
    		}
    		case DataType.TEXT :
    		{
    			TextDataType tdt = (TextDataType)obj ;
    			return tdt.getText() ;
    		}
    		case DataType.BOOLEAN :    		
    		case DataType.INT :    		
    		case DataType.REAL :
    		case DataType.STRING :
    		
    		default :
    		{
    			return obj.toString() ;
    		}
    	}
    	
    }   
    

    /**
     * Accessor method for AppendInterface property.
     *
     * @return Current Value of AppendInterface
     *
     */
    public Integer getAppendInterface() {
        return (Integer)getObject(Field.APPEND_INTERFACE) ;
    }

    /**
     * Accessor method for Assignees property.
     *
     * @return Current Value of Assignees
     *
     */
    public Collection<RequestUser> getAssignees() {
        return (Collection<RequestUser>)getObject(Field.ASSIGNEE);
    }

    /**
     * Accessor method for Attachments property.
     *
     * @return Current Value of Attachments
     *
     */
    public Collection<AttachmentInfo> getAttachments() {
        return (Collection<AttachmentInfo>) getObject(Field.ATTACHMENTS);
    }
    
    @Deprecated
    public Collection<AttachmentInfo> getAttachmentsOfType(String attType){
    	return (Collection<AttachmentInfo>) getObject(attType);
    }

    /**
     * Accessor method for CategoryId property.
     *
     * @return Current Value of CategoryId
     *
     */
    public Type getCategoryId() {
        return (Type) getObject(Field.CATEGORY);
    }

    /**
     * Accessor method for Ccs property.
     *
     * @return Current Value of Ccs
     *
     */
    public Collection<RequestUser> getCcs() {
        return (Collection<RequestUser>)getObject(Field.CC);
    }
    
    /**
     * Method to get the parents in this BA.
     *
     * @param  aSystemId the SysId by which the table has to be looked up
     *
     * @return the Table of [Request, Parent].
     *
     * @throws DatabaseException In case of any database related error
     */
    public static Hashtable<Integer, Integer> getChildrenBySystemId(Connection connection, int aSystemId) throws DatabaseException {
        Hashtable<Integer, Integer> table      = new Hashtable<Integer, Integer>();

        try {
            CallableStatement cs = connection.prepareCall("stp_request_getChildrenBySystemId ?");

            cs.setInt(1, aSystemId);

            ResultSet rs = cs.executeQuery();

            if (rs != null) {
                while (rs.next() != false) {
                    int childId  = rs.getInt("childId");
                    int parentId = rs.getInt("parentId");

                    table.put(new Integer(childId), new Integer(parentId));
                }

                rs.close();
            }

            cs.close();
            rs = null;
            cs = null;
        } catch (SQLException sqle) {
            StringBuilder message = new StringBuilder();

            message.append("An exception occurred while retrieving the children ").append("in the business area.").append("\nSystem Id : ").append(aSystemId).append("\n");

            throw new DatabaseException(message.toString(), sqle);
        }

        return table;
    }

    /**
     * Method to get the parents in this BA.
     *
     * @param  aSystemId the SysId by which the table has to be looked up
     *
     * @return the Table of [Request, Parent].
     *
     * @throws DatabaseException In case of any database related error
     */
    public static Hashtable<Integer, Integer> getChildrenBySystemId(int aSystemId) throws DatabaseException {
        Connection                  connection = null;

        try {
            connection = DataSourcePool.getConnection();
            
            return getChildrenBySystemId(connection, aSystemId);
        } catch (SQLException sqle) {
            StringBuilder message = new StringBuilder();

            message.append("An exception occurred while retrieving the children ").append("in the business area.").append("\nSystem Id : ").append(aSystemId).append("\n");

            throw new DatabaseException(message.toString(), sqle);
        } finally {
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException sqle) {
                    LOG.warning("Exception occurred while closing the connection.");
                }
            }

            connection = null;
        }
    }

    /**
     * Accessor method for Description property.
     *
     * @return Current Value of Description
     *
     */
    public String getDescription() {
        TextDataType desc = (TextDataType)this.getObject(Field.DESCRIPTION);
        if( null == desc ) return null ;
        else
        	return desc.getText() ; 
    }
    
    /**
     * Accessor method for Description Object.
     *
     * @return Current Value of Description
     *
     */
    public TextDataType getDescriptionObject() {
        return (TextDataType)this.getObject(Field.DESCRIPTION);
    }

    /**
     * Accessor method for DueDate property.
     *
     * @return Current Value of DueDate
     *
     */
    // TODO : Nitiraj : this should return Date object
    public Date getDueDate() {
        return (Date)this.getObject(Field.DUE_DATE);
    }

    /**
     * Accessor method for Extended Fields.
     *
     * @return Current Value of Extended Fields.
     *
     */
//    public Hashtable<Field, RequestEx> getExtendedFields() {
//        return myExtendedFields;
//    }

    /**
     * Accessor method for HeaderDescription property.
     *
     * @return Current Value of HeaderDescription
     *
     */
    public String getHeaderDescription() {
        TextDataType tdt = (TextDataType)this.getObject(Field.HEADER_DESCRIPTION);
        if( null == tdt )
        	return null ;
        else return tdt.getText() ;
    }

    public TextDataType getHeaderDescriptionObject() {
        return (TextDataType)this.getObject(Field.HEADER_DESCRIPTION);
    }
    
    /**
     * Accessor method for IsPrivate property.
     *
     * @return Current Value of IsPrivate
     *
     */
    public Boolean getIsPrivate() {
        return (Boolean)this.getObject(Field.IS_PRIVATE);
    }

    /**
     * Accessor method for LastUpdatedDate property.
     *
     * @return Current Value of LastUpdatedDate
     *
     */
    public Date getLastUpdatedDate() {
        return (Date)this.getObject(Field.LASTUPDATED_DATE);
    }

    /**
     * Accessor method for LoggedDate property.
     *
     * @return Current Value of LoggedDate
     *
     */
    public Date getLoggedDate() {
        return (Date)this.getObject(Field.LOGGED_DATE);
    }

    /**
     * Accessor method for Loggers property.
     *
     * @return Current Value of Loggers
     *
     */
    public Collection<RequestUser> getLoggers() {
        return (Collection<RequestUser>) this.getObject(Field.LOGGER);
    }

    /**
     * Accessor method for MaxActionId property.
     *
     * @return Current Value of MaxActionId
     *
     */
    public Integer getMaxActionId() {
        return (Integer)this.getObject(Field.MAX_ACTION_ID);
    }

    /**
     * Accessor method for Memo property.
     *
     * @return Current Value of Memo
     *
     */
    public TextDataType getMemoObject() {
        return (TextDataType)this.getObject(Field.MEMO);
    }

    public String getMemo() {
        TextDataType tdt = (TextDataType)this.getObject(Field.MEMO);
        if( null == tdt ) return null ;
        else return tdt.getText() ;
    }
    /**
     * Accessor method for Notify property.
     *
     * @return Current Value of Notify
     *
     */
    public Boolean getNotify() {
        return (Boolean)this.getObject(Field.NOTIFY);
    }

    /**
     * Accessor method for NotifyLoggers property.
     *
     * @return Current Value of NotifyLoggers
     *
     */
    public Boolean getNotifyLoggers() {
        return (Boolean)this.getObject(Field.NOTIFY_LOGGERS);
    }
//
//    public Object getObject(String fieldName) {
//        Object obj = myMapFieldToObjects.get(fieldName);
//
//        return obj;
//    }

    /**
     * Accessor method for OfficeId property.
     *
     * @return Current Value of OfficeId
     *
     */
    public Type getOfficeId() {
        return (Type)this.getObject(Field.OFFICE);
    }

    /**
     * Accessor method for ParentRequestId property.
     *
     * @return Current Value of ParentRequestId
     *
     */
    public Integer getParentRequestId() {
        return (Integer)this.getObject(Field.PARENT_REQUEST_ID);
    }

    /**
     * Accessor method for parentRequests property.
     *
     * @return Current Value for ParentRequest
     *
     */
//    public Integer getParentRequests() {
//        return (Integer)getObject(Field.PARENT_REQUEST_ID);
//    }

    /**
     * Accessor method for relatedRequests property.
     *
     * @return Current Value for RelatedRequets
     *
     */
    
    public static Hashtable<String,String> getParentRequests(int systemId, int requestId)
    {
	    Hashtable<String,String> parReqs = new Hashtable<String,String>() ;
		Connection con = null ;
		try
		{
			con = DataSourcePool.getConnection() ;
			CallableStatement cs = con.prepareCall("stp_request_getParentRequests ?, ? ");
			cs.setInt(1, systemId);
			cs.setInt(2, requestId);
			
			ResultSet rs = cs.executeQuery() ;
			if( rs != null )
			{
				while(rs.next()!=false)
				{
					int sysId = rs.getInt("sys_id");
					int reqId = rs.getInt("request_id");
					
					String subject = rs.getString("subject");
					subject = ( subject!=null ? subject : "" );
					BusinessArea ba = null ;
					try {
						ba = BusinessArea.lookupBySystemId(sysId) ;
					} catch (DatabaseException e) {
						LOG.info("",(e));
					}
					if( ba == null )
						continue ;
					
					String key = ba.getSystemPrefix() + "#" + reqId ;
					parReqs.put(key, subject); 
				}
			}
		}
		catch(SQLException sqle)
		{
			LOG.info("",(sqle));
		}
		finally
		{
			if( con != null )
				try {
					con.close() ;
				} catch (SQLException e) {
					LOG.info("",(e));
				}
		}
		
		return parReqs ;
    }
    
    public String getRelatedRequests() 
    {
        return (String)this.getObject(Field.RELATED_REQUESTS);
    }
    
    public static Hashtable<String,String> getRelatedRequests(Connection con, int systemId ,int requestId)
    {
    	Hashtable<String,String> subReqs = new Hashtable<String,String>() ;
    	try
    	{
    		CallableStatement cs = con.prepareCall("stp_request_getRelatedRequests ?, ? ");
    		cs.setInt(1, systemId);
    		cs.setInt(2, requestId);
    		
    		ResultSet rs = cs.executeQuery() ;
    		if( rs != null )
    		{
    			while(rs.next()!=false)
    			{
    				int sysId = rs.getInt("related_sys_id");
    				int reqId = rs.getInt("related_request_id");
    				
    				String subject = rs.getString("subject");
    				subject = ( subject!=null ? subject : "" );
    				BusinessArea ba = null ;
    				try {
						ba = BusinessArea.lookupBySystemId(sysId) ;
					} catch (DatabaseException e) {
						LOG.info("",(e));
					}
					if( ba == null )
						continue ;
					
    				String key = ba.getSystemPrefix() + "#" + reqId ;
    				subReqs.put(key, subject); 
    			}
    		}
    	}
    	catch(SQLException sqle)
    	{
    		LOG.info("",(sqle));
    	}
    	
    	return subReqs ;
    }
  
    public static Hashtable<String,String> getRelatedRequests(int systemId ,int requestId)
    {
    	Hashtable<String,String> subReqs = new Hashtable<String,String>() ;
    	Connection con = null ;
    	try
    	{
    		con = DataSourcePool.getConnection() ;
    		
    		return getRelatedRequests(con, systemId, requestId);
    	}
    	catch(SQLException sqle)
    	{
    		LOG.info("",(sqle));
    	}
    	finally
    	{
    		if( con != null )
				try {
					con.close() ;
				} catch (SQLException e) {
					LOG.info("",(e));
				}
    	}
    	
    	return subReqs ;
    }
    
    public static Hashtable<String,String> getSubRequests(int systemId ,int requestId)
    {
    	Hashtable<String,String> subReqs = new Hashtable<String,String>() ;
    	Connection con = null ;
    	try
    	{
    		con = DataSourcePool.getConnection() ;
    		CallableStatement cs = con.prepareCall("stp_request_getSubRequests ?, ? ");
    		cs.setInt(1, systemId);
    		cs.setInt(2, requestId);
    		
    		ResultSet rs = cs.executeQuery() ;
    		if( rs != null )
    		{
    			while(rs.next()!=false)
    			{
    				int sysId = rs.getInt(Field.BUSINESS_AREA);
    				int reqId = rs.getInt(Field.REQUEST);
    				
    				String subject = rs.getString("subject");
    				subject = ( subject!=null ? subject : "" );
    				BusinessArea ba = null ;
    				try {
						ba = BusinessArea.lookupBySystemId(sysId) ;
					} catch (DatabaseException e) {
						LOG.info("",(e));
					}
					if( ba == null )
						continue ;
					
    				String key = ba.getSystemPrefix() + "#" + reqId ;
    				subReqs.put(key, subject); 
    			}
    		}
    	}
    	catch(SQLException sqle)
    	{
    		LOG.info("",(sqle));
    	}
    	finally
    	{
    		if( con != null )
				try {
					con.close() ;
				} catch (SQLException e) {
					LOG.info("",(e));
				}
    	}
    	
    	return subReqs ;
    }
   
    /**
     * 
     * @param systemd
     * @param requestId
     * @return hashtable< [sysPrefix#requestId], subject > of all the sibling requests
     */
    
    public static Hashtable<String,String> getSiblingRequests(int systemId, int requestId)
    {
    	Hashtable<String,String> sibReqs = new Hashtable<String,String>() ;
    	Connection con = null ;
    	try
    	{
    		con = DataSourcePool.getConnection() ;
    		CallableStatement cs = con.prepareCall("stp_request_getSiblingRequests ?, ? ");
    		cs.setInt(1, systemId);
    		cs.setInt(2, requestId);
    		
    		ResultSet rs = cs.executeQuery() ;
    		if( rs != null )
    		{
    			while(rs.next()!=false)
    			{
    				int sysId = rs.getInt(Field.BUSINESS_AREA);
    				int reqId = rs.getInt(Field.REQUEST);
    				
    				String subject = rs.getString("subject");
    				subject = ( subject!=null ? subject : "" );
    				BusinessArea ba = null ;
    				try {
						ba = BusinessArea.lookupBySystemId(sysId) ;
					} catch (DatabaseException e) {
						LOG.info("",(e));
					}
					if( ba == null )
						continue ;
					
    				String key = ba.getSystemPrefix() + "#" + reqId ;
    				sibReqs.put(key, subject); 
    			}
    		}
    	}
    	catch(SQLException sqle)
    	{
    		LOG.info("",(sqle));
    	}
    	finally
    	{
    		if( con != null )
				try {
					con.close() ;
				} catch (SQLException e) {
					LOG.info("",(e));
				}
    	}
    	
    	return sibReqs ;
    }
    
    /**
     * Accessor method for RepliedToAction property.
     *
     * @return Current Value of RepliedToAction
     *
     */
    public Integer getRepliedToAction() 
    {
        return (Integer)this.getObject(Field.REPLIED_TO_ACTION);
    }

    /**
     * This method prepares the insert statements for the request ex objects
     * specified.
     *
     * @param aRequestId   Id of the request.
     * @param aReqEx       Request User List.
     * @param aFieldName   Field Name
     * @param aFieldValue  Field Value.
     * @param params 
     * @return SQLBatch of insert statements.
     *
     */
    private static String getReqExSQLBatch(int systemId, int requestId, int actionId, Field field, String colName, GenericValue fieldValue, GenericValue contentType, ArrayList<GenericValue> params) {
        StringBuilder buffer = new StringBuilder();

        // Handle the null reference.
        if( fieldValue == null )
        	return buffer.toString() ;

        buffer.append("INSERT INTO requests_ex (sys_id, request_id, field_id");

        buffer.append(", ").append(colName);
        
        if(field.getDataTypeId() == DataType.TEXT){
        	buffer.append(", text_content_type");
        }
        
        buffer.append(") \nVALUES(?, ?, ?");
        params.add(new GenericValue(systemId));
        params.add(new GenericValue(requestId));
        params.add(new GenericValue(field.getFieldId()));

       	buffer.append(", ? ");
       	params.add(fieldValue);
        
        if(field.getDataTypeId() == DataType.TEXT){
        	buffer.append(", ? ");
        	params.add(contentType);
        }
        
        buffer.append(")\n");
        
        buffer.append("INSERT INTO actions_ex (sys_id, request_id, action_id, field_id");
        
       	buffer.append(", ").append(colName);
        
        if(field.getDataTypeId() == DataType.TEXT){
        	buffer.append(", text_content_type");
        }
       	
        buffer.append(") \nVALUES(?, ?, ?, ?");
        params.add(new GenericValue(systemId));
        params.add(new GenericValue(requestId));
        params.add(new GenericValue(actionId));
        params.add(new GenericValue(field.getFieldId()));

       	buffer.append(", ? ");
       	params.add(fieldValue);
       	
        if(field.getDataTypeId() == DataType.TEXT)
        {
        	buffer.append(", ? ");
        	params.add(contentType);
        }
       	
        buffer.append(")\n");
        return buffer.toString();
    }

    /**
     * This method prepares the insert statements for the list of request user
     * objects specified.
     *
     * @param aRequestId   Id of the request.
     * @param aReqUserList Request User List.
     * @param params 
     * @return SQLBatch of insert statements.
     *
     * @exception  DatabaseException
     */
    private static String getReqUserSQLBatch(int aRequestId, int aActionId, Collection<RequestUser> aReqUserList, ArrayList<GenericValue> params) throws DatabaseException {
        StringBuilder buffer = new StringBuilder();

        // Handle the null reference.
        if ((aReqUserList == null) || (aReqUserList.size() == 0)) {
            return buffer.toString();
        }

        for (RequestUser reqUser : aReqUserList) {

            buffer.append("INSERT INTO request_users (sys_id, request_id, user_type_id, user_id, ordering, is_primary,field_id) \nVALUES(?, ?, ?, ?, ?, ?, ?)\n");
        	params.add(new GenericValue(reqUser.getSystemId()));
        	params.add(new GenericValue(aRequestId));
        	params.add(new GenericValue(reqUser.getUserTypeId()));
        	params.add(new GenericValue(reqUser.getUser().getUserId()));
        	params.add(new GenericValue(reqUser.getOrdering()));
        	params.add(new GenericValue(reqUser.getIsPrimary()));
        	params.add(new GenericValue(reqUser.getFieldId()));
        	
        	buffer.append("INSERT INTO action_users (sys_id, request_id, action_id, user_type_id, user_id, ordering, is_primary,field_id) \nVALUES(?, ?, ?, ?, ?, ?, ?, ?)");
        	params.add(new GenericValue(reqUser.getSystemId()));
        	params.add(new GenericValue(aRequestId));
        	params.add(new GenericValue(aActionId));
        	params.add(new GenericValue(reqUser.getUserTypeId()));
        	params.add(new GenericValue(reqUser.getUser().getUserId()));
        	params.add(new GenericValue(reqUser.getOrdering()));
        	params.add(new GenericValue(reqUser.getIsPrimary()));
        	params.add(new GenericValue(reqUser.getFieldId()));
        }

        return buffer.toString();
    }

    
  
    /**
     * This method prepares the batch required to insert the requestex
     * records into the database.
     *
     * @param aRequest   Request object.
     * @param aOperation Operation for which batch is required.
     * @param params 
     *
     * @return SQL Batch of insert statements for the requets_ex.
     * @throws DatabaseException 
     */
    private static String getRequestExBatch(Request aRequest, int aOperation, ArrayList<GenericValue> params) throws DatabaseException 
    {
        StringBuilder buffer = new StringBuilder();

        // Handling the null reference case.
        if (aRequest == null) {
            return buffer.toString();
        }

        int systemId = aRequest.getSystemId() ;
        int                         requestId      = aRequest.getRequestId();
        int                         actionId       = aRequest.getMaxActionId();

        // For update operation, delete the old records before inserting new
        // ones.
        if (aOperation == OP_UPDATE) {
            buffer.append("DELETE requests_ex WHERE sys_id = ? AND request_id = ?\n");
            params.add(new GenericValue(systemId));
            params.add(new GenericValue(requestId));
        }

        // Get the list of Fields from the map.
        ArrayList<Field> allFields = Field.lookupBySystemId(systemId);
        for(Field field : allFields) 
        {
        	if( field.getIsExtended() == false )
        		continue ;
        	
        	Object obj = aRequest.getObject(field);
        	if( null == obj )
        		continue ;
        	
        	switch (field.getDataTypeId()) 
        	{
	            case DataType.BOOLEAN : 
	            {
	            	Boolean bitValue = (Boolean) obj ;
	                buffer.append(getReqExSQLBatch(systemId, requestId, actionId, field, "bit_value", new GenericValue(bitValue), null ,params));
	                
	                break;
	            }           
	
	            case DataType.DATE :
	            case DataType.TIME :
	            case DataType.DATETIME : 
	            {
	                Date date  = (Date)obj;
	                GenericValue gv = new GenericValue(date);
	               
	                buffer.append(getReqExSQLBatch(systemId,requestId, actionId, field, "datetime_value", gv, null, params));
	                
	                break;
	            }
	            
	            case DataType.INT : 
	            {
	                buffer.append(getReqExSQLBatch(systemId, requestId, actionId, field, "int_value", new GenericValue((Integer)obj),null, params));
	                
		            break;
	            }
		
	            case DataType.REAL : 
	            {
	                buffer.append(getReqExSQLBatch(systemId, requestId, actionId, field, "real_value", new GenericValue((Double)obj), null ,params));
	                
	                break;
	            }
	
	            case DataType.STRING :
	            {
	                buffer.append(getReqExSQLBatch(systemId,requestId, actionId, field, "varchar_value", new GenericValue((String)obj), null,params));
		            
		            break;
	            }
	                       
	            case DataType.ATTACHMENTS:
	            {
	            	Collection<AttachmentInfo> atts = (Collection<AttachmentInfo>)obj ;
	            	String attJson = AttachmentInfo.toJson(atts);
	            	buffer.append(getReqExSQLBatch(systemId,requestId, actionId,field, "text_value", new GenericValue(attJson), null,params));
	            	break ;
	            }
	            case DataType.TEXT : 
	            {
	            	TextDataType tdt = (TextDataType) obj ;
	            	String text = tdt.getText() ;
	            	if( null == text )
	            		continue ;
//	            	text = (text != null ? text : "");
	            	int ctype = tdt.getContentType() ;
	                buffer.append(getReqExSQLBatch(systemId,requestId, actionId, field, "text_value", new GenericValue(text), new GenericValue(ctype), params));
	                
	                break;	            	
	            }
	
	            case DataType.TYPE : 
	            {
	            	Type t = (Type)obj ;	            	
	                buffer.append(getReqExSQLBatch(systemId,requestId, actionId, field, "type_value", new GenericValue(t.getTypeId()),null , params));
	                
	                break;
	            }	            
	            
	            case DataType.USERTYPE : 
	            {
	            	LOG.info("Ignoring the UserType extended fields while considering Extended Field. They are taken care with the other user Types." + field);
	            	break ;
	            }
	            default :
	            {
	            	LOG.severe("Request has reached an illegal state : detected during inserting the request into the DB : the datatypeid = " + field.getDataTypeId() + " is not supported.");
	            }
            }
        }

        return buffer.toString();
    }

    /**
     * Accessor method for RequestId property.
     *
     * @return Current Value of RequestId
     *
     */
    public Integer getRequestId() {
        return (Integer)this.getObject(Field.REQUEST);
    }

    /**
     * Accessor method for RequestTypeId property.
     *
     * @return Current Value of RequestTypeId
     *
     */
    public Type getRequestTypeId() {
        return (Type) this.getObject(Field.REQUEST_TYPE);
    }

    /**
     * This method prepares the batch required to insert the requestuser
     * records into the database.
     *
     * @param aRequest   Request object.
     * @param aOperation Operation for which the batch is required.
     * @param params The parameters
     *
     * @return SQL Batch of insert statements for the request users.
     *
     * @exception DatabseException
     */
    private static String getRequestUserBatch(Request aRequest, int aOperation, ArrayList<GenericValue> params) throws DatabaseException{
        StringBuilder buffer = new StringBuilder();

        // Handling the null reference case.
        if (aRequest == null) {
            return buffer.toString();
        }

        int requestId = aRequest.getRequestId();
        int actionId  = aRequest.getMaxActionId();

        //For update operation, delete the old records before inserting new
        // ones.
        if (aOperation == OP_UPDATE) {
            buffer.append("DELETE request_users WHERE sys_id = ").append("?").append(" AND request_id = ? \n");
            params.add( new GenericValue(aRequest.getSystemId()) );
            params.add( new GenericValue(requestId) );
        }

        buffer.append(getReqUserSQLBatch(requestId, actionId, aRequest.getLoggers(), params));
        buffer.append(getReqUserSQLBatch(requestId, actionId, aRequest.getAssignees(), params));
        buffer.append(getReqUserSQLBatch(requestId, actionId, aRequest.getSubscribers(), params));
        buffer.append(getReqUserSQLBatch(requestId, actionId, aRequest.getTos(), params));
        buffer.append(getReqUserSQLBatch(requestId, actionId, aRequest.getCcs(), params));
        buffer.append(getExReqUserSQLBatch(aRequest, params));
        return buffer.toString();
    }

    
    private static String getExReqUserSQLBatch(Request aRequest, ArrayList<GenericValue> params) throws DatabaseException 
    {
    	ArrayList<Field> userTypeFields = Field.lookupBySystemId(aRequest.getSystemId(), true, DataType.USERTYPE);
    	
    	if( null == userTypeFields || userTypeFields.size() == 0 )    	
    		return "";
    	
    	StringBuffer sb = new StringBuffer() ;
    	for( Field field : userTypeFields )
    	{    		
    		String sql = getExReqUserSQLBatch(aRequest,field,params);
    		if( null != sql )
    			sb.append(sql);
    	}
    	
    	return sb.toString() ;
	}

	/**
     * This method prepares the batch required to insert the requestuser
     * records into the database.
     *
     * @param aRequest   Request object.
     * @param aOperation Operation for which the batch is required.
     *
     * @return SQL Batch of insert statements for the request users.
     * @exception DatabseException
     */
    private static String getExReqUserSQLBatch(Request aRequest,Field field, ArrayList<GenericValue> params) throws DatabaseException{
        StringBuilder buffer = new StringBuilder();

        // Handling the null reference case.
        if (aRequest == null) {
            return buffer.toString();
        }

        int requestId = aRequest.getRequestId();
        int actionId  = aRequest.getMaxActionId();

        Collection<RequestUser> userTypes = aRequest.getExUserType(field);
        if( null != userTypes )
           buffer.append(getReqUserSQLBatch(requestId, actionId,userTypes, params));
             return buffer.toString();
    }
    
    
    
    /**
     * Method to get the resultant privacy of a request.
     *
     * @param  aSysId the SysId of the business area.
     * @param  aReqId request Id of the request.
     *
     * @return the resultant privateness.
     *
     * @throws DatabaseException In case of any database related error
     */
    public static boolean getResultantPrivacy(int aSysId, int aReqId) throws DatabaseException {
        Connection aCon        = null;
        boolean    returnValue = false;

        try {
            aCon = DataSourcePool.getConnection();

            CallableStatement cs = aCon.prepareCall("stp_request_getPrivacy ?, ?, ? ");

            cs.setInt(1, aSysId);
            cs.setInt(2, aReqId);
            cs.setBoolean(3, false);
            cs.registerOutParameter(3, Types.BOOLEAN);
            cs.execute();
            returnValue = cs.getBoolean(3);
            cs.close();
        } catch (SQLException sqle) {
            StringBuilder message = new StringBuilder();

            message.append("An exception occured while getting the privacy.").append("\n");

            throw new DatabaseException(message.toString(), sqle);
        } finally {
            if (aCon != null) {
                try {
                    aCon.close();
                } catch (SQLException sqle) {

                    // Should this be logged.?
                }
            }
        }

        return returnValue;
    }

    /**
     * Accessor method for SeverityId property.
     *
     * @return Current Value of SeverityId
     *
     */
    public Type getSeverityId() {
        return (Type)this.getObject(Field.SEVERITY);
    }

    /**
     * Accessor method for siblingRequests property.
     *
     * @return Current Value for SiblingRequets
     *
     */
//    public Hashtable<String, String> getSiblingRequests() {
//        return mySiblingRequests;
//    }

    /**
     * Accessor method for StatusId property.
     *
     * @return Current Value of StatusId
     *
     */
    public Type getStatusId() {
        return (Type)this.getObject(Field.STATUS);
    }

    /**
     * Accessor method for subRequests property.
     *
     * @return Current Value for SubRequets
     *
     */
//    public Hashtable<String, String> getSubRequests() {
//        return mySubRequests;
//    }

    /**
     * Accessor method for Subject property.
     *
     * @return Current Value of Subject
     *
     */
    public String getSubject() {
        return (String)this.getObject(Field.SUBJECT);
    }

    /**
     * Accessor method for Subscribers property.
     *
     * @return Current Value of Subscribers
     *
     */
    public Collection<RequestUser> getSubscribers() {
        return (Collection<RequestUser>)this.getObject(Field.SUBSCRIBER);
    }

    /**
     * Accessor method for Summary property.
     *
     * @return Current Value of Summary
     *
     */
    public String getSummary() 
    {
    	TextDataType tdt = (TextDataType)this.getObject(Field.SUMMARY);
    	if( null == tdt )
    		return null ;
    	
        return tdt.getText() ;
    }

    /**
     * Accessor method for Summary Object.
     *
     * @return Current Value of Summary
     *
     */
    public TextDataType getSummaryObject() 
    {
    	return (TextDataType)this.getObject(Field.SUMMARY);
    }
    
    /**
     * Accessor method for SystemId property.
     *
     * @return Current Value of SystemId
     *
     */
    public int getSystemId() {
        return mySystemId;
    }

    /**
     * Accessor method for Tos property.
     *
     * @return Current Value of Tos
     *
     */
    public Collection<RequestUser>
    getTos() {
        return (Collection)this.getObject(Field.TO);
    }

    /**
     * Accessor method for UserId property.
     *
     * @return Current Value of UserId
     *
     */
    public Integer getUserId() 
    {
    	Collection<RequestUser> cru = (Collection<RequestUser>)this.getObject(Field.USER);
    	if( null == cru || cru.size() == 0 )
    		return null ;
    	
    	if( cru.size() > 1 )
    		throw new IllegalStateException("The Request Object is in illegal state where it has more than on users set for USER field.") ;
    	
    	Iterator<RequestUser> iter = cru.iterator() ;
    	RequestUser ru = iter.next() ;
    	
    	return ru.getUserId() ;
    }

    //~--- set methods --------------------------------------------------------

    /**
     * Mutator method for AppendInterface property.
     *
     * @param aAppendInterface New Value for AppendInterface
     *
     */
    public void setAppendInterface(int aAppendInterface) {
//        myAppendInterface = new Integer(aAppendInterface);
//        myMapFieldToObjects.put(Field.APPEND_INTERFACE, myAppendInterface );
    	this.setObject(Field.APPEND_INTERFACE,aAppendInterface);
    }

    public void setObject(String fieldName, Object obj) 
    {
    	disAllowNull(fieldName);
    	
    	Field f = null ;
		try {
			f = Field.lookupBySystemIdAndFieldName(this.getSystemId(), fieldName);
		} catch (DatabaseException e) {			
			LOG.info("",(e));
		}
    	
    	setObject(f,obj);
	}

	/**
     * Mutator method for Assignees property.
     *
     * @param aAssignees New Value for Assignees
     *
     */
    public void setAssignees(Collection<RequestUser> aAssignees) 
    {
    	setObject(Field.ASSIGNEE,aAssignees);
    }

    /**
     * Mutator method for Attachments property.
     *
     * @param aAttachments New Value for Attachments
     *
     */
    public void setAttachments(Collection<AttachmentInfo> aAttachments) 
    {
    	setObject(Field.ATTACHMENTS,aAttachments);
    }

    /**
     * This method sets the parameters in the CallableStatement.
     *
     * @param aCS     CallableStatement whose params should be set.
     *
     * @exception SQLException
     */
    public void setCallableParameters(CallableStatement aCS) throws SQLException 
    {
    	disAllowNull(this.getSystemId());    	
        aCS.setInt(SYSTEMID, this.getSystemId());
        
        disAllowNull(this.getRequestId());        
        aCS.setInt(REQUESTID, this.getRequestId());
        
        disAllowNull(this.getUserId());
        aCS.setInt(USERID, this.getUserId());
      
        // QUES : Nitiraj : should MaxActionId Allow null ??
        // ANS : Nitiraj : null should not be allowed.
        Integer maid = this.getMaxActionId();
        disAllowNull(maid);
        aCS.setInt(MAXACTIONID, maid);
        
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
        
       	disAllowNull( this.getLastUpdatedDate());
       	disAllowNull(this.getLoggedDate());
        aCS.setTimestamp(LOGGEDDATE, Timestamp.toSqlTimestamp(this.getLoggedDate()), cal);
        aCS.setTimestamp(LASTUPDATEDDATE, Timestamp.toSqlTimestamp(this.getLastUpdatedDate()), cal);

       	aCS.setTimestamp(DUEDATE, Timestamp.toSqlTimestamp(this.getDueDate()), cal);
        
        Type cat = this.getCategoryId() ;
        if( null == cat )
        	aCS.setNull(CATEGORYID, java.sql.Types.INTEGER);
        else
        	aCS.setInt(CATEGORYID,cat.getTypeId());
       
        Type stat = this.getStatusId() ;        
        if(null == stat )
        	aCS.setNull(STATUSID, java.sql.Types.INTEGER);
        else
        	aCS.setInt(STATUSID, stat.getTypeId());

        Type sev = this.getSeverityId() ;
        if( null == sev )
        	aCS.setNull(SEVERITYID,java.sql.Types.INTEGER );
        else
        	aCS.setInt(SEVERITYID, sev.getTypeId());
        
        Type reqtype = this.getRequestTypeId() ;
        if( null == reqtype )
        	aCS.setNull(REQUESTTYPEID, java.sql.Types.INTEGER);
        else
        	aCS.setInt(REQUESTTYPEID, reqtype.getTypeId());

        Boolean priv = this.getIsPrivate() ;
        if( null == priv )
        	aCS.setNull(ISPRIVATE, java.sql.Types.BOOLEAN);
        else
        	aCS.setBoolean(ISPRIVATE, priv);

        Integer pid = this.getParentRequestId();
        if( null == pid )
        	aCS.setNull(PARENTREQUESTID, java.sql.Types.INTEGER);
        else
        	aCS.setInt(PARENTREQUESTID, pid);
        
        String tvnName = this.getSubject();
        if(this.source == TBitsConstants.SOURCE_TVN){
        	String requestID = Integer.toString(this.getRequestId());
	        if(tvnName.contains("_")){
				String check = tvnName.substring(tvnName.lastIndexOf("_")+1);
				if(check.equals(requestID)){
					tvnName = tvnName.substring(0, tvnName.lastIndexOf("_"));
				}
			}
        }
        aCS.setString(SUBJECT, tvnName);
        
        
        TextDataType tdt = (TextDataType) this.getObject(Field.DESCRIPTION);
        if( null == tdt )
        	tdt = new TextDataType("",TBitsConstants.CONTENT_TYPE_TEXT);
        
        aCS.setString(DESCRIPTION, tdt.getText());    
        aCS.setInt(DESCRIPTIONCONTENTTYPE, tdt.getContentType());
        
        aCS.setString(HEADERDESCRIPTION, this.getHeaderDescription());
        
        TextDataType tsum = (TextDataType) this.getObject(Field.SUMMARY) ;
        if( null == tsum )
        	tsum = new TextDataType("",TBitsConstants.CONTENT_TYPE_TEXT);
        
        aCS.setString(SUMMARY, tsum.getText());        
        aCS.setInt(SUMMARYCONTENTTYPE, tsum.getContentType());

        aCS.setString(MEMO, this.getMemo());

        aCS.setString(ATTACHMENTS, AttachmentInfo.toJson(this.getAttachments()));
        
        if( this.getAppendInterface() == null )
        	aCS.setNull(APPENDINTERFACE, java.sql.Types.INTEGER	);
        else
        	aCS.setInt(APPENDINTERFACE, this.getAppendInterface());
        
        Boolean not = this.getNotify() ;
        if( null == not )
        	aCS.setNull(NOTIFY, java.sql.Types.INTEGER);
        else
        	aCS.setInt(NOTIFY, (this.getNotify() == false ? 0 : 1 ) );
        
        Boolean notL = this.getNotifyLoggers() ;
        if( null == notL )
        	aCS.setNull(NOTIFYLOGGERS, java.sql.Types.BOOLEAN);
        else
        	aCS.setBoolean(NOTIFYLOGGERS, this.getNotifyLoggers());
        
        Integer repAct = this.getRepliedToAction() ;
        if( null == repAct )
        	aCS.setNull(REPLIEDTOACTION, java.sql.Types.INTEGER);
        else
        	aCS.setInt(REPLIEDTOACTION, repAct );
        
        Type off = this.getOfficeId() ;
        if( null == off )
        	aCS.setNull(OFFICEID,java.sql.Types.INTEGER);
        else
        	aCS.setInt(OFFICEID, off.getTypeId());
    }

    /**
     * Mutator method for CategoryId property.
     *
     * @param aCategoryId New Value for CategoryId
     *
     */
    public void setCategoryId(Type aCategoryId) 
    {
    	setObject(Field.CATEGORY,aCategoryId);
    }

    /**
     * Mutator method for Ccs property.
     *
     * @param aCcs New Value for Ccs
     *
     */
    public void setCcs(Collection<RequestUser> aCcs)
    {
    	setObject(Field.CC , aCcs);
    }

    /**
     * Mutator method for Description property.
     *
     * @param aDescription New Value for Description
     *
     */
    @Deprecated
    public void setDescription(String aDescription) 
    {
    	setObject(Field.DESCRIPTION,aDescription);
    }

    /**
     * Mutator method for Description property.
     *
     * @param aDescription New Value for Description
     *
     */
    public void setDescription(TextDataType aDescription) 
    {
    	setObject(Field.DESCRIPTION,aDescription);
    }
    
    /**
     * Mutator method for DueDate property.
     *
     * @param aDueDate New Value for DueDate
     *
     */
    @Deprecated
    public void setDueDate(Timestamp aDueDate) 
    {
    	Date d = null ;
    	if( null != aDueDate )
    		d = new Date(aDueDate.getTime()) ;
    	
    	setDueDate(d);
    }
    
    /**
     * Mutator method for DueDate property.
     *
     * @param aDueDate New Value for DueDate
     *
     */
    public void setDueDate(Date aDueDate) 
    {
    	setObject(Field.DUE_DATE,aDueDate);
    }

    /**
     * Mutator method for ExtendedFields property.
     *
     * @param aExtendedFields New Value for ExtendedFields
     */
    public void setExtendedFields(Hashtable<Field, RequestEx> aExtendedFields) throws DatabaseException{
    	disAllowNull(aExtendedFields);
    	
        Enumeration<Field> e     = aExtendedFields.keys();
        Field              field = null;

        while (e.hasMoreElements()) {
            field = e.nextElement();

            RequestEx reqEx      = aExtendedFields.get(field);
            
            setRequestEx(reqEx);            
        }
    }
    
    public void addExtendedField(RequestEx reqEx) throws DatabaseException{
    	disAllowNull(reqEx);
    	setRequestEx(reqEx);
    }

    private void setRequestEx( RequestEx reqEx ) throws DatabaseException
    {
    	if( null == reqEx )
    	{
    		throw new IllegalArgumentException("The supplied request Ex object was null.");
    	}
    	
    	if( reqEx.getSystemId() != this.getSystemId() || reqEx.getRequestId() != this.getRequestId() )
    	{
    		throw new IllegalArgumentException("The supplied request Ex object does not belong to request : " + this.getSystemId() + "#" + this.getRequestId() );
    	}
    	
    	int fieldId = reqEx.getFieldId(); 
    	Field field = Field.lookupBySystemIdAndFieldId(reqEx.getSystemId(), fieldId);
    	if( null == field )
		{
    		throw new IllegalArgumentException("The supplied request Ex object contains field which is non-existent : field_id = " + fieldId );
		}
    	
    	String    key        = field.getName();
        int       dataTypeId = field.getDataTypeId();

        // Put the value based on the data type.
        switch (dataTypeId) {
        case DataType.BOOLEAN :
        	setObject(field,reqEx.getBitValue());
            break;

        case DataType.DATE :
        case DataType.TIME :
        case DataType.DATETIME :
        	this.setObject(field, reqEx.getDateTimeValue());
            break;

        case DataType.INT :
            this.setObject(field, reqEx.getIntValue());
            break;

        case DataType.REAL :
            this.setObject(field, reqEx.getRealValue());
            break;

        case DataType.STRING :
            this.setObject(field, reqEx.getVarcharValue());
            break;

        case DataType.TEXT :
        	TextDataType tdt = new TextDataType(reqEx.getTextValue(),reqEx.getTextContentType());
            this.setObject(field, tdt);
            break;

        case DataType.TYPE :
            Type type = Type.lookupBySystemIdAndFieldNameAndTypeId(mySystemId, field.getName(), reqEx.getTypeValue());
            if (type == null) {
            	throw new IllegalArgumentException(TYPE_NOT_FOUND);
            }
            this.setObject(field, type);
            break;
           
        case DataType.ATTACHMENTS :
        	// This is where the attachment information is being set. 
        	// The fromJson method returns a string even if the text value is null.
        	// See where the fix needs to be made.
        	// Making a temporary fix by the if condition.
        	if(reqEx.getTextValue() != null)
        		this.setObject(field, AttachmentInfo.fromJson(reqEx.getTextValue()));
        	else
        		this.setObject(field, null);
            break;
            
        case DataType.USERTYPE :
        default :
        	throw new IllegalArgumentException("You cannot set the userType values in extended fields");
        }
    }
    /**
     * Mutator method for HeaderDescription property.
     *
     * @param aHeaderDescription New Value for HeaderDescription
     *
     */
    public void setHeaderDescription(String aHeaderDescription) 
    {
    	this.setObject(Field.HEADER_DESCRIPTION, aHeaderDescription);
    }
    
    public void setHeaderDescriptionObject(TextDataType aHeaderDescription) 
    {
    	this.setObject(Field.HEADER_DESCRIPTION, aHeaderDescription);
    }
    
   protected static void disAllowNull(Object obj) 
   {
	   if( null == obj )
		   throw new IllegalArgumentException(MESSAGE_NULL);
   }

/**
	 * Adds the header description to the current request. Header description is
	 * something which is used for displaying the tracking of fields.
	 * 
	 * @param fieldName
	 * @param fieldId
	 * @param message
	 */
    public void addHeaderDesc(String fieldName, String fieldId, String message)
    {
    	String header = this.getHeaderDescription();
    	if(header == null)
    		header = "";
    	header =  fieldName + "##" + fieldId + "##" + "[ " + message + "]\n" + header;
    	this.setHeaderDescription(header);
    }
    
    /**
     * Mutator method for IsPrivate property.
     *
     * @param aIsPrivate New Value for IsPrivate
     *
     */
    public void setIsPrivate(Boolean aIsPrivate) 
    {
    	this.setObject(Field.IS_PRIVATE, aIsPrivate);
    }

    /**
     * Mutator method for LastUpdatedDate property.
     *
     * @param aLastUpdatedDate New Value for LastUpdatedDate
     *
     */
    @Deprecated
    public void setLastUpdatedDate(Timestamp aLastUpdatedDate) 
    {    	
    	Date d = null ;
    	if( null != aLastUpdatedDate )
    	{
    		d = new Date(aLastUpdatedDate.getTime());
    	}
   		setLastUpdatedDate(d);
    }
    
    /**
     * Mutator method for LastUpdatedDate property.
     *
     * @param aLastUpdatedDate New Value for LastUpdatedDate
     *
     */    
    public void setLastUpdatedDate(Date aLastUpdatedDate) 
    {
    	this.setObject(Field.LASTUPDATED_DATE, aLastUpdatedDate);
    }

    /**
     * Mutator method for LoggedDate property.
     *
     * @param aLoggedDate New Value for LoggedDate
     *
     */
    @Deprecated
    public void setLoggedDate(Timestamp aLoggedDate) 
    {
    	Date d = null ;
    	if(aLoggedDate != null )
    	{
    		d = new Date(aLoggedDate.getTime());
    	}
    	setLoggedDate(d);
    }

    /**
     * Mutator method for LoggedDate property.
     *
     * @param aLoggedDate New Value for LoggedDate
     *
     */
    public void setLoggedDate(Date aLoggedDate) 
    {
    	this.setObject(Field.LOGGED_DATE, aLoggedDate);
    }

    
    /**
     * Mutator method for Loggers property.
     *
     * @param aLoggers New Value for Loggers
     *
     */
    public void setLoggers(Collection<RequestUser> aLoggers) 
    {
        this.setObject(Field.LOGGER, aLoggers);
    }

    
    public void setExUserType(String fieldName,ArrayList<RequestUser> aList) throws DatabaseException
    {
    	
    	
    	Field f = Field.lookupBySystemIdAndFieldName(this.getSystemId(), fieldName);
		
    	if(f == null)
			throw new IllegalArgumentException("The field '" + fieldName + "' doesnt exist.");
    	
		if(!f.getIsExtended())
		{
			throw new IllegalArgumentException("'" + fieldName + "' is not an extended field");
		}

		if(f.getDataTypeId() != DataType.USERTYPE)
			throw new IllegalArgumentException("This field is not a UserType Field");
		
		setExUserType(f,aList);
    }
    
    public void setExUserType(Field field , Collection<RequestUser> reqUser )
    {    	
//    	disAllowNull(reqUser);
    	
    	ArrayList<RequestUser> ru = new ArrayList<RequestUser>() ;
    	if( null != reqUser )
    		ru.addAll(reqUser);    	
    	
    	setObject(field,ru);
    }
  
	 public void setObject(Field field, Object obj) 
	 {
		disAllowNull(field);
		
		if( field.getSystemId() != this.getSystemId())
			throw new IllegalArgumentException("The field " + field + " does not belong to this BusinessArea : " + this.getSystemId() );
		
		switch(field.getDataTypeId())
		{
			case DataType.INT :
			{
//				disAllowNull(obj);
				if( null != obj && !(obj instanceof Integer) )
					throw new IllegalArgumentException("An integer object is expected.");
				
				Integer integer = (Integer)obj;
				validateInteger(field,integer);
				myMapFieldToObjects.put(field,obj);
				break;
			}
			case DataType.REAL :
			{
//				disAllowNull(obj);
				if( null != obj && !(obj instanceof Double) )
					throw new IllegalArgumentException("A Double object is expected.");
				
				myMapFieldToObjects.put(field,obj);
				break ;
			}
			case DataType.BOOLEAN :
			{
//				disAllowNull(obj);
				if( null != obj && !(obj instanceof Boolean) )
					throw new IllegalArgumentException("A Boolean object is expected.");
				
				myMapFieldToObjects.put(field,obj);
				break;
			}
			case DataType.DATE :
			case DataType.TIME :
			case DataType.DATETIME :
			{
				// dis-allow null only if the field is logged-date or last-update-date
//				if(field.getName() == Field.LOGGED_DATE || field.getName() == Field.LASTUPDATED_DATE )
//					disAllowNull(obj) ; 
						
				// we have to allow nulls for dates because we cannot put empty strings in 
				// database for the date type of fields.
				// if the field is not null then we will set the field's value in our map
				// else if it is null then we will clear the value from our map.
//				if( null != obj )
//				{
				if( null != obj && !(obj instanceof Date) )
					throw new IllegalArgumentException("A Date object is expected.");
				
				Date d = (Date) obj ;
				validateDate(field,d);
				myMapFieldToObjects.put(field,obj);
//				}
//				else
//				{
//					myMapFieldToObjects.remove(field);
//				}
				
				break ;
			}
			case DataType.USERTYPE : 
			{
//				disAllowNull(obj);
				
				// TODO : I am not able to check th concrete type of this collection here.
			
				if( null != obj && !(obj instanceof Collection))
					throw new IllegalArgumentException("A Collection<RequestUser> object is expected.");
				
				// may throw ClassCastException if the object is not of type Collection<RequestUser> 
				Collection<RequestUser> reqUsers = (Collection<RequestUser>)obj;	
				
				myMapFieldToObjects.put(field, validateAndCreateUserTypes(field,reqUsers) );
				break ;
			}
			
			case DataType.ATTACHMENTS :
			{
//				disAllowNull(obj);
				
				// TODO : I am not able to check th concrete type of this collection here.
				if( null != obj && !(obj instanceof Collection) )
					throw new IllegalArgumentException("A Collection<AttachmentInfo> object is exptected.");
				
				// may throw ClassCastException if the object is not of type Collection<AttachmentInfo>
				Collection<AttachmentInfo> attInfos = (Collection<AttachmentInfo>) obj ;
				
				myMapFieldToObjects.put(field,validateAndCreateAttachments(field,attInfos));
				break ;
			}
			case DataType.STRING :
			{
//				disAllowNull(obj);
				
				if( null != obj && !(obj instanceof String) )
					throw new IllegalArgumentException("A String object is expected.");
				
				myMapFieldToObjects.put(field, obj);
				break ;
			}
			case DataType.TEXT :
			{
//				disAllowNull(obj);
				if( null != obj )
				{	
					if( obj instanceof TextDataType )
					{	
						myMapFieldToObjects.put(field, obj);
					}				
					else if( obj instanceof String )
					{
						LOG.debug("The DataType TEXT expects the object of class TextDataType.\n As the content-type was not provided so assuming it to be HTML");
						String text = (String) obj;
						TextDataType tdt = new TextDataType(text,TBitsConstants.CONTENT_TYPE_HTML);
						myMapFieldToObjects.put(field, tdt);
					}
					else
					{
						throw new IllegalArgumentException("A TextDataType object was expected.");
					}
				}
				else 
					myMapFieldToObjects.put(field, obj);
				
				break ;
			}
			
			case DataType.TYPE :
			{
//				disAllowNull(obj);
				
				if( null != obj &&  !(obj instanceof Type) )
					throw new IllegalArgumentException("A Type object is expected.");
				
				Type type = (Type) obj;
				validateType(field,type);
				myMapFieldToObjects.put(field, obj) ;
				break ;
			}
			
			default :
				throw new IllegalArgumentException("The given DataType" + field.getDataTypeId() + " is not supported by Request.");
			
		}		
	 }

	private void validateInteger(Field field, Integer integer) 
	{
		if( field.getName().equals(Field.BUSINESS_AREA) || field.getName().equals(Field.REQUEST))
		{
			disAllowNull(integer);
			
			if(field.getName().equals(Field.BUSINESS_AREA) && !integer.equals(this.getSystemId()))
				throw new IllegalArgumentException("The system Id cannot be changed after creating the request object.");
				
		}
	}

	private void validateDate(Field field, Date d) 
	{
		// TODO : Nitiraj : what todo ??
	}

	private void validateType(Field field, Type type) 
	{
		if( type.getSystemId() != this.getSystemId() || type.getFieldId() != field.getFieldId() )
			throw new IllegalArgumentException("The Type was expected for sys_id " + this.getSystemId() + " and field_id " + field.getFieldId() + " but was found for sys_id = " + type.getSystemId() + " and field_id = " + type.getFieldId() ) ;
	}

	private Collection<AttachmentInfo> validateAndCreateAttachments(Field field,
			Collection<AttachmentInfo> attInfos) 
	{
		if( null == attInfos )
			return null ;
		// I am keeping all Attachments as ArrayList. But this should not be
		// visible to any class other than Request itself
		ArrayList<AttachmentInfo> ats = new ArrayList<AttachmentInfo>() ;
		for( AttachmentInfo ai : attInfos )
		{
			// TODO : not validating anything. Just treating everything to be correct as 
			// was assumed previously also.
			ats.add(ai);
		}
		
		return ats;
	}

	private Collection<RequestUser> validateAndCreateUserTypes(Field field,
			Collection<RequestUser> reqUsers) 
	{	
		if( field.getName().equals(Field.USER) && ((null == reqUsers) || (reqUsers.size() != 1 )))
			throw new IllegalArgumentException("Excatly one user is allowed in USER_ID field");

		if( null == reqUsers )
			return null ;
		// I am keeping all user types as ArrayList. But this should not be
		// visible to any class other than Request itself
		ArrayList<RequestUser> rus = new ArrayList<RequestUser>() ;
		for( RequestUser ru : reqUsers )
		{
			// TODO : I think we should remove the 
			// members sys_id, request_id and field_id from the RequestUser class.
			// they serve purpose.
			// not validating anything. Just treating everything to be correct as 
			// was assumed previously also.
			rus.add(ru);
		}
		
		return rus;
	}

	public Collection<RequestUser> getExUserType(String fieldName)
     {
    	disAllowNull(fieldName);
    	 Field field = null;
		try {
			field = Field.lookupBySystemIdAndFieldName(this.getSystemId(), fieldName);
		} catch (DatabaseException e) {
			LOG.info("",(e));
		}
    	 if( null == field )
    		 throw new IllegalArgumentException("Cannot find the field with name : " + fieldName + " for ba with sys_id = " + this.getSystemId() );
    	 
    	 return getExUserType(field);    	 
     }
     
     public Collection<RequestUser> getExUserType(Field field)
     {
    	 disAllowNull(field);
    	
    	 if( field.getIsExtended() == false )
    		 throw new IllegalArgumentException("The field " + field + " is not extended type" );
    	 
    	 if( field.getDataTypeId()  != DataType.USERTYPE )
    		 throw new IllegalArgumentException("The field " + field + " is not of UserType.");
    	 
    	 return (Collection<RequestUser>) this.getObject(field);
     }
     
	private void setExUserTypes(Collection<RequestUser> userTypeList) {
    	
		if( null != userTypeList )
		{
			for( RequestUser ru : userTypeList)
			{
				int fieldId = ru.getFieldId() ;
				Field field = null ;
				try {
					field = Field.lookupBySystemIdAndFieldId(this.getSystemId(), fieldId);
				} catch (DatabaseException e) {
					LOG.info("",(e));					
				}
				if( null == field )
					throw new IllegalArgumentException("Exception occured while accessing the field with id : " + fieldId +
				" for sys_id : " + this.getSystemId() );
				
				Collection<RequestUser> aru = (Collection<RequestUser>) this.getObject(field);
				if( null == aru )
					aru = new ArrayList<RequestUser>();
				
				aru.add(ru);
				
				this.setObject(field,aru);
			}
		}
//    	ArrayList<RequestUser> tempList = new ArrayList<RequestUser>();
//    	ArrayList<RequestUser> sourceList = new ArrayList<RequestUser>();
//    	
//    	RequestUser.setSortParams(4,0);
//    	sourceList=RequestUser.sort(userTypeList);
//    	int oldFieldId=-1,currFieldId=-1,flag=0;
//       
//         for(RequestUser req:sourceList){
//        	 currFieldId=req.getMyFieldId();
//           if(flag==0 && currFieldId!=oldFieldId){
//        	   tempList.add(req);
//        	   flag=1;
//        	   
//           }
//           else if(flag==1 && currFieldId!=oldFieldId){
//        	   userTypeMap.put(oldFieldId,tempList);
//        	   tempList.clear();
//        	   tempList.add(req);
//        	   
//           }
//           else tempList.add(req);
//        	
//        	oldFieldId=currFieldId;
//        	
//        }
//        userTypeMap.put(oldFieldId,tempList);
    	
    }
    /**
     * Mutator method for MaxActionId property.
     *
     * @param aMaxActionId New Value for MaxActionId
     *
     */
    public void setMaxActionId(Integer aMaxActionId) 
    {
    	this.setObject(Field.MAX_ACTION_ID, aMaxActionId);
    }

    /**
     * Mutator method for Memo property.
     *
     * @param aMemo New Value for Memo
     *
     */
    public void setMemo(String aMemo) 
    {
    	this.setObject(Field.MEMO,aMemo);
    }
    
    public void setMemoObject(TextDataType aMemo) 
    {
    	this.setObject(Field.MEMO,aMemo);
    }

    /**
     * Mutator method for Notify property.
     *
     * @param aNotify New Value for Notify
     *
     */
    public void setNotify(Boolean aNotify) 
    {
    	this.setObject(Field.NOTIFY,aNotify);
    }

    /**
     * Mutator method for NotifyLoggers property.
     *
     * @param aNotifyLoggers New Value for NotifyLoggers
     *
     */
    public void setNotifyLoggers(Boolean aNotifyLoggers) 
    {
    	this.setObject(Field.NOTIFY_LOGGERS, aNotifyLoggers);
    }

    /**
     * Mutator method for OfficeId property.
     *
     * @param aOfficeId New Value for OfficeId
     *
     */
    public void setOfficeId(Type aOfficeId) 
    {
    	this.setObject(Field.OFFICE, aOfficeId);
    }

    /**
     * Mutator method for ParentRequestId property.
     *
     * @param aParentRequestId New Value for ParentRequestId
     *
     */
    public void setParentRequestId(Integer aParentRequestId) 
    {
    	this.setObject(Field.PARENT_REQUEST_ID,aParentRequestId);
    }

    /**
     * Mutator method for parentRequests property.
     *
     * @param aParentRequests New Value for ParentRequets
     *
     */
//    public void setParentRequests(Hashtable<String, String> aParentRequests) 
//    {
//        myParentRequests = aParentRequests;
//    }

    /**
     * Mutator method for relatedRequests property.
     *
     * @param aRelatedRequests New Value for RelatedRequets
     *
     */
    public void setRelatedRequests(Collection<RequestDataType> aRelatedRequests) 
    {
//    	disAllowNull(aRelatedRequests);
    	String relReqs = validateAndCreateRelatedRequests(aRelatedRequests);
    	this.setObject(Field.RELATED_REQUESTS, relReqs);
    }

    private String validateAndCreateRelatedRequests(Collection<RequestDataType> aRelatedRequests) 
    {
    	if( null == aRelatedRequests )
    		return null ;
    	// passing them from a HashSet to remove any duplicate values.
    	HashSet<RequestDataType> relReqs = new HashSet<RequestDataType>() ;
    	relReqs.addAll(aRelatedRequests);
    	return APIUtil.getRequestsString(relReqs);
	}

	public void setRelatedRequests(String aRelatedRequests) 
	{
//		disAllowNull(aRelatedRequests);
		// doing this step to clean the aRelatedRequests of any errors.
		
		Collection<RequestDataType> reqs = null;
		try
		{
			if((aRelatedRequests != null) && (aRelatedRequests.length() > 0))
					reqs = APIUtil.getRequestCollection(aRelatedRequests);
		}
		catch(IllegalArgumentException iae)
		{
			LOG.warn("Improper linked requests", iae);
		}
        this.setRelatedRequests(reqs);
    }
	
	@Deprecated
	public void setRelatedRequests(Hashtable<String,String> aRelatedRequests) 
	{
//		disAllowNull(aRelatedRequests);
		// doing this step to clean the aRelatedRequests of any errors.
		Collection<RequestDataType> reqs = APIUtil.getRequestCollection(aRelatedRequests);
        this.setRelatedRequests(reqs);
    }
    /**
     * Mutator method for RepliedToAction property.
     *
     * @param aRepliedToAction New Value for aRepliedToAction
     *
     */
    public void setRepliedToAction(Integer aRepliedToAction) 
    {
    	this.setObject(Field.REPLIED_TO_ACTION, aRepliedToAction);
    }

    /**
     * Mutator method for RequestId property.
     *
     * @param aRequestId New Value for RequestId
     *
     */
    public void setRequestId(Integer aRequestId) 
    {
        myRequestId = aRequestId;
        this.setObject(Field.REQUEST, myRequestId);
    }

    /**
     * Mutator method for RequestTypeId property.
     *
     * @param aRequestTypeId New Value for RequestTypeId
     *
     */
    public void setRequestTypeId(Type aRequestTypeId) 
    {
    	this.setObject(Field.REQUEST_TYPE, aRequestTypeId);
    }

    /**
     * Mutator method for SeverityId property.
     *
     * @param aSeverityId New Value for SeverityId
     *
     */
    public void setSeverityId(Type aSeverityId) 
    {
    	this.setObject(Field.SEVERITY, aSeverityId);
    }

    /**
     * Mutator method for siblingRequests property.
     *
     * @param aSiblingRequests New Value for SiblingRequets
     *
     */
//    public void setSiblingRequests(Hashtable<String, String> aSiblingRequests) {
//        mySiblingRequests = aSiblingRequests;
//    }

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
     * Mutator method for StatusId property.
     *
     * @param aStatusId New Value for StatusId
     *
     */
    public void setStatusId(Type aStatusId) 
    {
    	this.setObject(Field.STATUS,aStatusId);
    }

    /**
     * Mutator method for subRequests property.
     *
     * @param aSubRequests New Value for SubRequets
     *
     */
//    public void setSubRequests(Hashtable<String, String> aSubRequests) {
//        mySubRequests = aSubRequests;
//    }

    /**
     * Mutator method for Subject property.
     *
     * @param aSubject New Value for Subject
     *
     */
    public void setSubject(String aSubject) 
    {
    	this.setObject(Field.SUBJECT, aSubject);
    }

    /**
     * Mutator method for Subscribers property.
     *
     * @param aSubscribers New Value for Subscribers
     *
     */
    public void setSubscribers(Collection<RequestUser> aSubscribers) 
    {
    	this.setObject(Field.SUBSCRIBER, aSubscribers);
    }

    /**
     * Mutator method for Summary property.
     *
     * @param aSummary New Value for Summary
     *
     */
    @Deprecated
    public void setSummary(String aSummary) 
    {
    	this.setObject(Field.SUMMARY, aSummary);
    }

    /**
     * Mutator method for Summary property.
     *
     * @param aSummary New Value for Summary
     *
     */
    public void setSummary(TextDataType aSummary) 
    {
    	this.setObject(Field.SUMMARY, aSummary);
    }
    /**
     * Mutator method for SystemId property.
     *
     * @param aSystemId New Value for SystemId
     * 
     */
    // NITI :should be made private
    @Deprecated // this will be made private in future.
    public void setSystemId(int aSystemId) 
    {
        mySystemId = aSystemId;
        this.setObject(Field.BUSINESS_AREA, aSystemId);
    }

    /**
     * Mutator method for Tos property.
     *
     * @param aTos New Value for Tos
     *
     */
    public void setTos(Collection<RequestUser> aTos) 
    {
    	this.setObject(Field.TO,aTos);
    }

    /**
     * Mutator method for UserId property.
     *
     * @param aUserId New Value for UserId
     *
     */
    public void setUserId(Integer userId) 
    {
    	disAllowNull(userId);
    	User user = null ;
    	try {
			 user = User.lookupAllByUserId(userId);
		} catch (DatabaseException e) 
		{			
			LOG.info("",(e));
		}
		
		if( null == user )
			throw new IllegalArgumentException("Cannot find user with id : " + userId ) ;
		
//		public RequestUser(int aSystemId, int aRequestId, int aUserId, int aOrdering, boolean aIsPrimary,int aFieldId) {
		RequestUser ru = null ;
		try {
			ru = new RequestUser(this.getSystemId(),this.getRequestId(),user.getUserId(),1,true,Field.lookupBySystemIdAndFieldName(this.getSystemId(),Field.USER).getFieldId());
		} catch (DatabaseException e) 
		{
			LOG.severe("",(e));
		}
		if( null == ru )
			throw new IllegalStateException("Exception occured while creating the RequestUser for Field.USER and sys_id = " + this.getSystemId() );
		
		ArrayList<RequestUser> reqUsers = new ArrayList<RequestUser>() ;
		reqUsers.add(ru);
		
		this.setObject(Field.USER, reqUsers);
    }
	
	/*
	 * If the request is coming from the web interface, you can set this to HttpServletRequest.getContextPath().
	 * other wise you can leave it as such.
	 */
    // TODO : Nitiraj : do we need context path in request class .? /?? 
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
	
	@Deprecated // Nitiraj : this will not be support for long 
	public RequestEx getRequestExObject(Field f)
	{
		disAllowNull(f);
		if( f.getSystemId() != this.getSystemId() )
			throw new IllegalArgumentException("The field " + f + " does not belong to the ba with sys_id = " + this.getSystemId() ) ;
		if( f.getIsExtended() == false )
			throw new IllegalArgumentException("The field " + f + " is not extended field.") ;
		
		Object obj = this.getObject(f);
		if( null == obj )
			return null ;
			
		RequestEx rex = new RequestEx() ;
		rex.setSystemId(this.getSystemId());
		rex.setRequestId(this.getRequestId());
		rex.setFieldId(f.getFieldId());
		
		switch(f.getDataTypeId())
    	{   		
    		case DataType.USERTYPE :
    		{
    			throw new IllegalArgumentException("Extended Usertype fields cannot be accessed by this method.");
    		}
    		case DataType.ATTACHMENTS :
    		{    			
    			Collection<AttachmentInfo> attInfos = (Collection<AttachmentInfo>) obj ;
    			String attJson = AttachmentInfo.toJson(attInfos);    			
    			rex.setVarcharValue(attJson);
    			break; 
    		}
    		case DataType.DATE:
    		case DataType.DATETIME :
    		case DataType.TIME :
    		{
    			Date d = (Date) obj ;
    			rex.setDateTimeValue(new Timestamp(d.getTime()));
    			break ;
    		}    		
    		case DataType.TYPE :
    		{
    			Type type = (Type) obj;
    			rex.setTypeValue(type.getTypeId());
    			break ;
    		}
    		case DataType.BOOLEAN :
    		{
    			Boolean b = (Boolean) obj ;
    			rex.setBitValue(b);
    			break; 
    		}
    		case DataType.INT :
    		{
    			Integer i = (Integer)obj ;
    			rex.setIntValue(i);
    			break ;
    		}
    		case DataType.REAL :
    		{
    			Double doub = (Double)obj ;
    			rex.setRealValue(doub);
    			break ;
    		}
    		case DataType.STRING :
    		{
    			String str = (String)obj;
    			rex.setVarcharValue(str);
    			break;
    		}
    		case DataType.TEXT :
    		{
    			TextDataType tdt = (TextDataType)obj;
    			rex.setTextValue(tdt.getText());
    			rex.setTextContentType(tdt.getContentType());
    			break;
    		}
    		default :
    		{
    			throw new IllegalArgumentException("Request and RequestEx does not support data type for field : " + f);
    		}
    	}
		
		return rex;
	}
		
	// Getters for the Extended Field Values.
	@Deprecated
	public Timestamp getExDate(String fieldName) throws DatabaseException
	{
		Field f = Field.lookupBySystemIdAndFieldName(this.getSystemId(), fieldName);
		if(f == null)
			throw new IllegalArgumentException("The field '" + fieldName + "' doesnt exist.");
		if(!f.getIsExtended())
		{
			throw new IllegalArgumentException("'" + fieldName + "' is not an extended field");
		}
		
		if( f.getDataTypeId() != DataType.DATE && f.getDataTypeId() != DataType.DATETIME && f.getDataTypeId() != DataType.TIME )
			throw new IllegalArgumentException("The field " + f + " is not of type date, datetime or time");
		
		Date d = (Date) this.getObject(f);
		if( null == d ) return null ;
		else return new Timestamp(d.getTime()) ;
	}
	
	@Deprecated
	public Timestamp getExDateTime(String fieldName) throws DatabaseException, IllegalStateException
	{
		Field f = Field.lookupBySystemIdAndFieldName(this.getSystemId(), fieldName);
		if(f == null)
			throw new IllegalArgumentException("The field '" + fieldName + "' doesnt exist.");
		if(!f.getIsExtended())
		{
			throw new IllegalArgumentException("'" + fieldName + "' is not an extended field");
		}
		
		if(f.getDataTypeId() != DataType.DATETIME)
			throw new IllegalArgumentException("The field " + f + " is not of type datetime");
		
		Date d = (Date) this.getObject(f);
		if( null == d ) return null ;
		else return new Timestamp(d.getTime()) ;
	}

	@Deprecated
	public Boolean getExBoolean(String fieldName) throws DatabaseException, IllegalStateException
	{
		Field f = Field.lookupBySystemIdAndFieldName(this.getSystemId(), fieldName);
		if(f == null)
			throw new IllegalArgumentException("The field '" + fieldName + "' doesnt exist.");
		if(!f.getIsExtended())
		{
			throw new IllegalArgumentException("'" + fieldName + "' is not an extended field");
		}
		
		if(f.getDataTypeId() != DataType.BOOLEAN)
			throw new IllegalArgumentException("The field " + f + " is not of type Boolean");
		
		return (Boolean)this.getObject(f);
	}
	
	@Deprecated
	public int getExInt(String fieldName) throws DatabaseException, IllegalStateException
	{
		Field f = Field.lookupBySystemIdAndFieldName(this.getSystemId(), fieldName);
		if(f == null)
			throw new IllegalArgumentException("The field '" + fieldName + "' doesnt exist.");
		if(!f.getIsExtended())
		{
			throw new IllegalArgumentException("'" + fieldName + "' is not an extended field");
		}
		
		if(f.getDataTypeId() != DataType.INT)
			throw new IllegalArgumentException("The field " + f + " is not of type Integer");
		
		return (Integer)this.getObject(f);
	}
	
	@Deprecated
	public double getExReal(String fieldName) throws DatabaseException, IllegalStateException
	{
		Field f = Field.lookupBySystemIdAndFieldName(this.getSystemId(), fieldName);
		if(f == null)
			throw new IllegalArgumentException("The field '" + fieldName + "' doesnt exist.");
		if(!f.getIsExtended())
		{
			throw new IllegalArgumentException("'" + fieldName + "' is not an extended field");
		}
		
		if(f.getDataTypeId() != DataType.REAL)
			throw new IllegalArgumentException("The field " + f + " is not of type Real");
		
		return (Double) this.getObject(f);
	}
	
	/**
	 * Used for both Text and varchar
	 * @param fieldName
	 * @return
	 * @throws DatabaseException
	 * @throws IllegalStateException
	 */
	@Deprecated
	public String getExString(String fieldName) throws DatabaseException, IllegalStateException
	{
		Field f = Field.lookupBySystemIdAndFieldName(this.getSystemId(), fieldName);
		if(f == null)
			throw new IllegalStateException("The field '" + fieldName + "' doesnt exist.");
		if(!f.getIsExtended())
		{
			throw new IllegalStateException("'" + fieldName + "' is not an extended field");
		}
		
		if(f.getDataTypeId() == DataType.TEXT)
		{
			TextDataType tdt = (TextDataType) this.getObject(f);
			if( null == tdt ) return null; 
			else return tdt.getText() ;
		}
		if(f.getDataTypeId() == DataType.ATTACHMENTS)
		{
			Collection<AttachmentInfo> atts = (Collection<AttachmentInfo>) this.getObject(f);
			if( null == atts ) return null ;
			else return AttachmentInfo.toJson(atts);
		}
		else if(f.getDataTypeId() == DataType.STRING)
		{
			return (String)this.getObject(f);
		}
		else throw new IllegalArgumentException("This field is neither Text nor Varchar");
	}
	
	@Deprecated
	public Type getExType(String fieldName) throws DatabaseException, IllegalStateException
	{
		Field f = Field.lookupBySystemIdAndFieldName(this.getSystemId(), fieldName);
		if(f == null)
			throw new IllegalArgumentException("The field '" + fieldName + "' doesnt exist.");
		if(!f.getIsExtended())
		{
			throw new IllegalArgumentException("'" + fieldName + "' is not an extended field");
		}
		if( f.getDataTypeId() != DataType.TYPE )
			throw new IllegalArgumentException("The field " + f + " is not of type Type " );

		return (Type)this.getObject(f);
	}
	
	
	
//	private RequestEx getRequestEx(String fieldName, int dataTypeId) throws DatabaseException 
//	{	
//		Field f = Field.lookupBySystemIdAndFieldName(this.getSystemId(), fieldName);
//		if(f == null)
//			throw new IllegalStateException("The field '" + fieldName + "' doesnt exist.");
//		if(!f.getIsExtended())
//		{
//			throw new IllegalStateException("'" + fieldName + "' is not an extended field");
//		}
//		if(f.getDataTypeId() != dataTypeId)
//			throw new IllegalStateException("The field '" + fieldName + "' is not of the type '" + dataTypeId + "'");
//		RequestEx rex = this.getExtendedFields().get(f);
//		return rex;
//	}
	
//	private void updateObjectMapAndValueMap(String fieldName, RequestEx reqEx, int dataTypeId ) throws DatabaseException
//	{
//		 switch (dataTypeId) {
//         case DataType.BOOLEAN :
//             myMapFieldToValues.put(fieldName, String.valueOf(reqEx.getBitValue()));
//             myMapFieldToObjects.put(fieldName, reqEx.getBitValue());
//
//             break;
//
//         case DataType.DATE :
//         case DataType.TIME :
//         case DataType.DATETIME :
//             if (reqEx.getDateTimeValue() != null) {
//                 myMapFieldToValues.put(fieldName, reqEx.getDateTimeValue().toCustomFormat("yyyy-MM-dd HH:mm:ss"));
//                 myMapFieldToObjects.put(fieldName, reqEx.getDateTimeValue());
//             }
//
//             break;
//
//         case DataType.INT :
//             myMapFieldToValues.put(fieldName, Integer.toString(reqEx.getIntValue()));
//             myMapFieldToObjects.put(fieldName, reqEx.getIntValue());
//
//             break;
//
//         case DataType.REAL :
//             myMapFieldToValues.put(fieldName, Double.toString(reqEx.getRealValue()));
//             myMapFieldToObjects.put(fieldName, reqEx.getRealValue());
//
//             break;
//
//         case DataType.STRING :
//             if (reqEx.getVarcharValue() != null) {
//                 myMapFieldToValues.put(fieldName, reqEx.getVarcharValue());
//                 myMapFieldToObjects.put(fieldName, reqEx.getVarcharValue());
//             }
//
//             break;
//
//         case DataType.TEXT :
//             if (reqEx.getTextValue() != null) {
//                 myMapFieldToValues.put(fieldName, reqEx.getTextValue());
//                 myMapFieldToObjects.put(fieldName, reqEx.getTextValue());
//             }
//
//             break;
//
//         case DataType.TYPE :
//             Type type = Type.lookupBySystemIdAndFieldNameAndTypeId(mySystemId, fieldName, reqEx.getTypeValue());
//
//             if (type != null) {
//                 myMapFieldToValues.put(fieldName, type.getName());
//                 myMapFieldToObjects.put(fieldName, type.getName());
//             }
//
//             break;
//
//         case DataType.USERTYPE :
//             myMapFieldToValues.put(fieldName, reqEx.getVarcharValue());
//             myMapFieldToObjects.put(fieldName, reqEx.getVarcharValue());
//
//             break;
//             
//         case DataType.ATTACHMENTS :
//             myMapFieldToValues.put(fieldName, reqEx.getTextValue());
//             myMapFieldToObjects.put(fieldName, reqEx.getTextValue());
//
//             break;
//         }
//
//	}
	
	//Setters for extended field values
	@Deprecated
	public void setExInt(String fieldName, int value) throws DatabaseException
	{
		Field f = Field.lookupBySystemIdAndFieldName(this.getSystemId(), fieldName);
		if(f == null)
			throw new IllegalArgumentException("The field '" + fieldName + "' doesnt exist.");
		if(!f.getIsExtended())
		{
			throw new IllegalArgumentException("'" + fieldName + "' is not an extended field");
		}
		this.setObject(f, value);
	}
	
	//Setters for extended field values
	@Deprecated
	public void setExDate(String fieldName, Timestamp value) throws DatabaseException
	{
		disAllowNull(fieldName);
		Field f = Field.lookupBySystemIdAndFieldName(this.getSystemId(), fieldName);
		if(f == null)
			throw new IllegalArgumentException("The field '" + fieldName + "' doesnt exist.");
		if(!f.getIsExtended())
		{
			throw new IllegalArgumentException("'" + fieldName + "' is not an extended field");
		}
		Date d = null ;
		if( null != value )
			d = new Date( value.getTime() );
		
		this.setObject(fieldName, d);
	}
	
	@Deprecated
	public void setExDate(String fieldName, Date value) throws DatabaseException
	{
		disAllowNull(fieldName);
		Field f = Field.lookupBySystemIdAndFieldName(this.getSystemId(), fieldName);
		if(f == null)
			throw new IllegalArgumentException("The field '" + fieldName + "' doesnt exist.");
		if(!f.getIsExtended())
		{
			throw new IllegalArgumentException("'" + fieldName + "' is not an extended field");
		}
		this.setObject(fieldName, value);
	}
	//Setters for extended field values
	@Deprecated
	public void setExReal(String fieldName, double value) throws DatabaseException
	{
		Field f = Field.lookupBySystemIdAndFieldName(this.getSystemId(), fieldName);
		if(f == null)
			throw new IllegalArgumentException("The field '" + fieldName + "' doesnt exist.");
		if(!f.getIsExtended())
		{
			throw new IllegalArgumentException("'" + fieldName + "' is not an extended field");
		}
		
		this.setObject(f, value);
	}
	
	//Setters for extended field values
	@Deprecated
	public void setExBoolean(String fieldName, boolean value) throws DatabaseException
	{
		Field f = Field.lookupBySystemIdAndFieldName(this.getSystemId(), fieldName);
		if(f == null)
			throw new IllegalArgumentException("The field '" + fieldName + "' doesnt exist.");
		if(!f.getIsExtended())
		{
			throw new IllegalArgumentException("'" + fieldName + "' is not an extended field");
		}
		
		this.setObject(f, value);
	}
	//Setters for extended field values
	@Deprecated
	public void setExString(String fieldName, String value) throws DatabaseException
	{
		Field f = Field.lookupBySystemIdAndFieldName(this.getSystemId(), fieldName);
		if(f == null)
			throw new IllegalArgumentException("The field '" + fieldName + "' doesnt exist.");
		if(!f.getIsExtended())
		{
			throw new IllegalArgumentException("'" + fieldName + "' is not an extended field");
		}
		if(f.getDataTypeId() == DataType.TEXT)
		{		
			this.setObject(f,value);
		}
		else if(f.getDataTypeId() == DataType.STRING)
		{
			this.setObject(f, value);
		}
		else if(f.getDataTypeId() == DataType.ATTACHMENTS)
		{
			Collection<AttachmentInfo> atts = AttachmentInfo.fromJson(value);
			this.setObject(f, atts);
		}
		else throw new IllegalStateException("This field is neither Text nor Varchar");
	}
	
	//Setters for extended field values
	@Deprecated
	public void setExType(String fieldName, int value) throws DatabaseException
	{
		Field f = Field.lookupBySystemIdAndFieldName(this.getSystemId(), fieldName);
		if(f == null)
			throw new IllegalArgumentException("The field '" + fieldName + "' doesnt exist.");
		if(!f.getIsExtended())
		{
			throw new IllegalArgumentException("'" + fieldName + "' is not an extended field");
		}
		
		Type type = Type.lookupBySystemIdAndFieldIdAndTypeId(this.getSystemId(), f.getFieldId(), value);
		if( null == type )
			throw new IllegalArgumentException("There is no type with id = " + value + " for field = " + f);
		
		this.setObject(f, type);
	}
	//Setters for extended field values
	@Deprecated
	public void setExType(String fieldName, Type type) throws DatabaseException
	{
		Field f = Field.lookupBySystemIdAndFieldName(this.getSystemId(), fieldName);
		if(f == null)
			throw new IllegalArgumentException("The field '" + fieldName + "' doesnt exist.");
		if(!f.getIsExtended())
		{
			throw new IllegalArgumentException("'" + fieldName + "' is not an extended field");
		}
		
		this.setObject(f, type);
	}

	//Setters for extended field values
	@Deprecated
	public void setExType(String fieldName, String value) throws DatabaseException
	{
		Field f = Field.lookupBySystemIdAndFieldName(this.getSystemId(), fieldName);
		if(f == null)
			throw new IllegalArgumentException("The field '" + fieldName + "' doesnt exist.");
		if(!f.getIsExtended())
		{
			throw new IllegalArgumentException("'" + fieldName + "' is not an extended field");
		}
		
		Type type = Type.lookupBySystemIdAndFieldNameAndTypeName(this.getSystemId(), fieldName, value);
		
		this.setObject(f, type);
	}


	public void setVersionNum(int version) 
	{
		this.version = version;		
	}

	public boolean equals(Object o)
	{
		if( null == o)
			return false ;
		
		if( !(o instanceof Request ) )
			return false ;
		
		Request req = (Request) o ;
		
		if( req.getSystemId() == this.getSystemId() && req.getRequestId() == this.getRequestId() )
			return true ;
		
		return false ;
	}
	
	public int hashCode()
	{
		int hc = HashCodeUtil.SEED ;
		
		hc = HashCodeUtil.hash(hc, this.getSystemId());
		hc = HashCodeUtil.hash(hc, this.getRequestId());
		
		return hc ;
	}

	public int getVersion() {
		return version;
	}
	
	public static void main( String argv[] )
	{
		int sysId = 14 ;
		String requestIds = "1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,21,22,23,24,25,26,27,28,29,30,31,32,33,34,35,36,37,38,39,40,41,42,43,44,45,46,47,48,49,50,51,52,53,54,55,56,57,58,59,60,61,62,63,64,65,66,67,68,69,70,71,72,73,74,75,76,77,78,79,80,81,82,83,84,85,86,87,88,89,90,91,92,93,94,95,96,97,98,99,100,101,102,103,104,105,106,107,108,109,110,111,112,113,114,115,116,117,118,119,120,121,122,123,124,125,126,127,128,129,130,131,132,133,134,135,136,137,138,139,140,141,142,143,144,145,146,147,148,149,150,151,152,153,154,155,156,157,158,159,160,161,162,163,164,165,166,167,168,169,170,171,172,173,174,175,176,177,178,179,180,181,182,183,184,185,186,187,188,189,190,191,192,193,194,195,196,197,198,199,200,201,202,203,204,205,206,207,208,209,210,211,212,213,214,215,216,217,218,219,220,221,222,223,224,225,226,227,228,229,230,231,232,233,234,235,236,237,238,239,240,241,242,243,244,245,246,247,248,249,250,251,252,253,254,255,256,257,258,259,260,261,262,263,264,265,266,267,268,269,270,271,272,273,274,275,276,277,278,279,280,281,282,283,284,285,286,287,288,289,290,291,292,293,294,295,296,297,298,299,300,301,302,303,304,305,306,307,308,309,310,311,312,313,314,315,316,317,318,319,320,321,322,323,324,325,326,327,328,329,330,331,332,333,334,335,336,337,338,339,340,341,342,343,344,345,346,347,348,349,350,351,352,353,354,355,356,357,358,359,360,361,362,363,364,365,366,367,368,369,370,371,372,373,374,375,376,377,378,379,380,381,382,383,384,385,386,387,388,389,390,391,392,393,394,395,396,397,398,399,400,401,402,403,404,405,406,407,408,409,410,411,412,413,414,415,416,417,418,419,420,421,422,423,424,425,426,427,428,429,430,431,432,433,434,435,436,437,438,439,440,441,442,443,444,445,446,447,448,449,450,451,452,453,454,455,456,457,458,459,460,461,462,463,464,465,466,467,468,469,470,471,472,473,474,475,476,477,478,479,480,481,482,483,484,485,486,487,488,489,490,491,492,493,494,495,496,497,498,499,500,501,502,503,504,505,506,507,508,509,510,511,512,513,514,515,516,517,518,519,520,521,522,523,524,525,526,527,528,529,530,531,532,533,534,535,536,537,538,539,540,541,542,543,544,545,546,547,548,549,550,551,552,553,554,555,556,557,558,559,560,561,562,563,564,565,566,567,568,569,570,571,572,573,574,575,576,577,578,579,580,581,582,583,584,585,586,587,588,589,590,591,592,593,594,595,596,597,598,599,600,601,602,603,604,605,606,607,608,609,610,611,612,613,614,615,616,617,618,619,620,621,622,623,624,625,626,627,628,629,630,631,632,633,634,635,636,637,638,639,640,641,642,643,644,645,646,647,648,649,650,651,652,653,654,655,656,657,658,659,660,661,662,663,664,665,666,667,668,669,670,671,672,673,674,675,676,677,678,679,680,681,682,683,684,685,686,687,688,689,690,691,692,693,694,695,696,697,698,699,700,701,702,703,704,705,706,707,708,709,710,711,712,713,714,715,716,717,718,719,720,721,722,723,724,725,726,727,728,729,730,731,732,733,734,735,736,737,738,739,740,741,742,743,744,745,746,747,748,749,750,751,752,753,754,755,756,757,758,759,760,761,762,763,764,765,766,767,768,769,770,771,772,773,774,775,776,777,778,779,780,781,782,783,784,785,786,787,788,789,790,791,792,793,794,795,796,797,798,799,800,801,802,803,804,805,806,807,808,809,810,811,812,813,814,815,816,817,818,819,820,821,822,823,824,825,826,827,828,829,830,831,832,833,834,835,836,837,838,839,840,841,842,843,844,845,846,847,848,849,850,851,852,853,854,855,856,857,858,859,860,861,862,863,864,865,866,867,868,869,870,871,872,873,874,875,876,877,878,879,880,881,882,883,884,885,886,887,888,889,890,891,892,893,894,895,896,897,898,899,900,901,902,903,904,905,906,907,908,909,910,911,912,913,914,915,916,917,918,919,920,921,922,923,924,925,926,927,928,929,930,931,932,933,934,935,936,937,938,939,940,941,942,943,944,945,946,947,948,949,950,951,952,953,954,955,956,957,958,959,960,961,962,963,964,965,966,967,968,969,970,971,972,973,974,975,976,977,978,979,980,981,982,983,984,985,986,987,988,989,990,991,992,993,994,995,996,997,998,999,1000,1001,1002,1003,1004,1005,1006,1007,1008,1009,1010,1011,1012,1013,1014,1015,1016,1017,1018,1019,1020,1021,1022,1023,1024,1025,1026,1027,1028,1029,1030,1031,1032,1033,1034,1035,1036,1037,1038,1039,1040,1041,1042,1043,1044,1045,1046,1047,1048,1049,1050,1051,1052,1053,1054,1055,1056,1057,1058,1059,1060,1061,1062,1063,1064,1065,1066,1067,1068,1069,1070,1071,1072,1073,1074,1075,1076,1077,1078,1079,1080,1081,1082,1083,1084,1085,1086,1087,1088,1089,1090,1091,1092,1093,1094,1095,1096,1097,1098,1099,1100,1101,1102,1103,1104,1105,1106,1107,1108,1109,1110,1111,1112,1113,1114,1115,1116,1117,1118,1119,1120,1121,1122,1123,1124,1125,1126,1127,1128,1129,1130,1131,1132,1133,1134,1135,1136,1137,1138,1139,1140,1141,1142,1143,1144,1145,1146,1147,1148,1149,1150,1151,1152,1153,1154,1155,1156,1157,1158,1159,1160,1161,1162,1163,1164,1165,1166,1167,1168,1169,1170,1171,1172,1173,1174,1175,1176,1177,1178,1179,1180,1181,1182,1183,1184,1185,1186,1187,1188,1189,1190,1191,1192,1193,1194,1195,1196,1197,1198,1199,1200,1201,1202,1203,1204,1205,1206,1207,1208,1209,1210,1211,1212,1213,1214,1215,1216,1217,1218,1219,1220,1221,1222,1223,1224,1225,1226,1227,1228,1229,1230,1231,1232,1233,1234,1235,1236,1237,1238,1239,1240,1241,1242,1243,1244,1245,1246,1247,1248,1249,1250,1251,1252,1253,1254,1255,1256,1257,1258,1259,1260,1261,1262,1263,1264,1265,1266,1267,1268,1269,1270,1271,1272,1273,1274,1275,1276,1277,1278,1279,1280,1281,1282,1283,1284,1285,1286,1287,1288,1289,1290,1291,1292,1293,1294,1295,1296,1297,1298,1299,1300,1301,1302,1303,1304,1305,1306,1307,1308,1309,1310,1311,1312,1313,1314,1315,1316,1317,1318,1319,1320,1321,1322,1323,1324,1325,1326,1327,1328,1329,1330,1331,1332,1333,1334,1335,1336,1337,1338,1339,1340,1341,1342,1343,1344,1345,1346,1347,1348,1349,1350,1351,1352,1353,1354,1355,1356,1357,1358,1359,1360,1361,1362,1363,1364,1365,1366,1367,1368,1369,1370,1371,1372,1373,1374,1375,1376,1377,1378,1379,1380,1381,1382,1383,1384,1385,1386,1387,1388,1389,1390,1391,1392,1393,1394,1395,1396,1397,1398,1399,1400,1401,1402,1403,1404,1405,1406,1407,1408,1409,1410,1411,1412,1413,1414,1415,1416,1417,1418,1419,1420,1421,1422,1423,1424,1425,1426,1427,1428,1429,1430,1431,1432,1433,1434,1435,1436,1437,1438,1439,1440,1441,1442,1443,1444,1445,1446,1447,1448,1449,1450,1451,1452,1453,1454,1455,1456,1457,1458,1459,1460,1461,1462,1463,1464,1465,1466,1467,1468,1469,1470,1471,1472,1473,1474,1475,1476,1477";
		
		try 
		{
			long start = System.currentTimeMillis() ;
			ArrayList<Request> reqs = Request.lookupBySystemIdAndRequestIdList(sysId, requestIds);
			long end = System.currentTimeMillis() ;
			
			ArrayList<String> reqIdStr = Utilities.toArrayList(requestIds);
			ArrayList<Integer> reqIdInt = new ArrayList<Integer>() ;
			for( String str : reqIdStr)
			{				
				int reqId = Integer.parseInt(str);
				reqIdInt.add(reqId);
			}
			
			long start2 = System.currentTimeMillis();
			for( int reqId : reqIdInt )
			{
				Request req = Request.lookupBySystemIdAndRequestId(sysId, reqId);
			}
			long end2 = System.currentTimeMillis() ;
			
			long start3 = System.currentTimeMillis() ;
			ArrayList<Request> reqs3 = Request.lookupBySystemIdAndRequestIdList(sysId, requestIds);
			long end3 = System.currentTimeMillis() ;
			
			long start4 = System.currentTimeMillis();
			for( int reqId : reqIdInt )
			{
				Request req = Request.lookupBySystemIdAndRequestId(sysId, reqId);
			}
			long end4 = System.currentTimeMillis() ;
			
			System.out.println("1. time taken to fetch " + reqs.size() + " no. of records : " + (end-start) + " ms.");			
			System.out.println("2. time taken to fetch : " + (end2-start2) + " ms.");
			System.out.println("3. time taken to fetch " + reqs3.size() + " no. of records : " + (end3-start3) + " ms.");
			System.out.println("4. time taken to fetch : " + (end4-start4) + " ms.");
		} 
		catch (DatabaseException e) {
			e.printStackTrace();
		}
		
	}

}


/**
 * This class is the comparator for domain object corresponding to the
 * requests table in the database.
 *
 * @author : giris
 * @version : $Id: $
 *
 */
class RequestComparator implements Comparator<Request>, Serializable {
    public int compare(Request obj1, Request obj2) {
        return obj1.compareTo(obj2);
    }
}

