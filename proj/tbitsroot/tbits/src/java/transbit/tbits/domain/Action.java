/*
 * Copyright (c) 2005 Transbit Technologies Pvt. Ltd. All rights reserved. *
 *
 * This software is the confidential and proprietary information
 * of Transbit Technologies Pvt. Ltd. ("Confidential Information").  You
 * shall not disclose such Confidential Information and shall use
 * it only in accordance with the terms of the license agreement
 * you entered into with Transbit Technologies Pvt. Ltd.
 */



/*
 * Action.java
 *
 * $Header:
 *
 */
package transbit.tbits.domain;

//~--- non-JDK imports --------------------------------------------------------

import transbit.tbits.Helper.HashCodeUtil;
import transbit.tbits.api.DiffEntry;
import transbit.tbits.common.DataSourcePool;

//Other TBits Imports.
import transbit.tbits.common.ActionFileInfo;
import transbit.tbits.common.DatabaseException;
import transbit.tbits.common.TBitsLogger;
import transbit.tbits.common.Timestamp;

//Imports from the current package.
import transbit.tbits.domain.ActionEx;
import transbit.tbits.domain.ActionUser;
import transbit.tbits.domain.User;

//static imports
import static transbit.tbits.Helper.TBitsConstants.ASC_ORDER;
import static transbit.tbits.Helper.TBitsConstants.PKG_DOMAIN;

//~--- JDK imports ------------------------------------------------------------

//Java Imports.
import java.io.Serializable;
import java.io.UnsupportedEncodingException;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Comparator;
import java.util.Hashtable;
import java.util.TimeZone;

//~--- classes ----------------------------------------------------------------

//Third party imports.

/**
 * This class is the domain object corresponding to the actions table
 * in the database.
 *
 * @author  : nitiraj
 * @version : $Id: $
 *
 */
public class Action implements Comparable<Action>, Serializable {
    public static final TBitsLogger LOG = TBitsLogger.getLogger(PKG_DOMAIN);

    // Enum sort of fields for Attributes.
    private static final int SYSTEMID          = 1;
    private static final int SUBJECT           = 8;
    private static final int STATUSID          = 5;
    private static final int SEVERITYID        = 6;
    private static final int REQUESTTYPEID     = 7;
    private static final int REQUESTID         = 2;
    private static final int PARENTREQUESTID   = 11;
    private static final int ISPRIVATE         = 10;
    private static final int DESCRIPTION       = 9;
    private static final int CATEGORYID        = 4;
    private static final int ACTIONID          = 3;
    private static final int USERID            = 12;
    private static final int SUMMARY           = 18;
    private static final int REPLIEDTOACTION   = 23;
    private static final int OFFICEID          = 24;
    private static final int NOTIFYLOGGERS     = 22;
    private static final int NOTIFY            = 21;
    private static final int MEMO              = 19;
    private static final int LOGGEDDATE        = 14;
    private static final int LASTUPDATEDDATE   = 15;
    private static final int HEADERDESCRIPTION = 16;
    private static final int DUEDATE           = 13;
    private static final int ATTACHMENTS       = 17;
    private static final int APPENDINTERFACE   = 20;

    // Static attributes related to sorting.
    private static int ourSortField;
    private static int ourSortOrder;

    //~--- fields -------------------------------------------------------------

    private int                         myActionId;
    private int                         myAppendInterface;
    private ArrayList<Integer>          myAssigneeIds;
    private String                      myAttachments;
    private int                         myCategoryId;
    private ArrayList<Integer>          myCcIds;
    private String                      myDescription;
    private int		                    myDescriptionContentType;
    private Timestamp                   myDueDate;
    private Hashtable<String, ActionEx> myExtendedFields;
    private String                      myHeaderDescription;
    private boolean                     myIsPrivate;
    private Timestamp                   myLastUpdatedDate;
    private Timestamp                   myLoggedDate;
    private ArrayList<Integer>          myLoggerIds;
    private String                      myMemo;
    private int                         myNotify;
    private boolean                     myNotifyLoggers;
    private int                         myOfficeId;
    private int                         myParentRequestId;
    private int                         myRepliedToAction;
    private int                         myRequestId;
    private int                         myRequestTypeId;
    private int                         mySeverityId;
    private int                         myStatusId;
    private String                      mySubject;
    private ArrayList<Integer>          mySubscriberIds;
    private String                      mySummary;
    private int		                    mySummaryContentType;

    // Attributes of this Domain Object.
    private int                mySystemId;
    private ArrayList<Integer> myToIds;
    private int                myUserId;

    //~--- constructors -------------------------------------------------------

    /**
     * The default constructor.
     */
    public Action() {}

    /**
     * The complete constructor.
     *
     *  @param aSystemId
     *  @param aRequestId
     *  @param aActionId
     *  @param aCategoryId
     *  @param aStatusId
     *  @param aSeverityId
     *  @param aRequestTypeId
     *  @param aLoggerIds
     *  @param aAssigneeIds
     *  @param aSubscriberIds
     *  @param aToIds
     *  @param aCcIds
     *  @param aSubject
     *  @param aDescription
     *  @param aIsPrivate
     *  @param aParentRequestId
     *  @param aUserId
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
     *  @param aOfficeId
     *  @param aExtendedFields
     */
    public Action(int aSystemId, int aRequestId, int aActionId, int aCategoryId, int aStatusId, int aSeverityId, int aRequestTypeId, ArrayList<Integer> aLoggerIds, ArrayList<Integer> aAssigneeIds,
                  ArrayList<Integer> aSubscriberIds, ArrayList<Integer> aToIds, ArrayList<Integer> aCcIds, String aSubject, String aDescription, boolean aIsPrivate, int aParentRequestId, int aUserId,
                  Timestamp aDueDate, Timestamp aLoggedDate, Timestamp aLastUpdatedDate, String aHeaderDescription, String aAttachments, String aSummary, String aMemo, int aAppendInterface,
                  int aNotify, boolean aNotifyLoggers, int aRepliedToAction, int aOfficeId, Hashtable aExtendedFields) {
        mySystemId          = aSystemId;
        myRequestId         = aRequestId;
        myActionId          = aActionId;
        myCategoryId        = aCategoryId;
        myStatusId          = aStatusId;
        mySeverityId        = aSeverityId;
        myRequestTypeId     = aRequestTypeId;
        myLoggerIds         = aLoggerIds;
        myAssigneeIds       = aAssigneeIds;
        mySubscriberIds     = aSubscriberIds;
        myToIds             = aToIds;
        myCcIds             = aCcIds;
        mySubject           = aSubject;
        myDescription       = aDescription;
        myIsPrivate         = aIsPrivate;
        myParentRequestId   = aParentRequestId;
        myUserId            = aUserId;
        myDueDate           = aDueDate;
        myLoggedDate        = aLoggedDate;
        myLastUpdatedDate   = aLastUpdatedDate;
        myHeaderDescription = aHeaderDescription;
        myAttachments       = aAttachments;
        mySummary           = aSummary;
        myMemo              = aMemo;
        myAppendInterface   = aAppendInterface;
        myNotify            = aNotify;
        myNotifyLoggers     = aNotifyLoggers;
        myRepliedToAction   = aRepliedToAction;
        myOfficeId          = aOfficeId;
        myExtendedFields    = new Hashtable<String, ActionEx>();
    }

    //~--- methods ------------------------------------------------------------

    public int hashCode()
    {
    	int hc = HashCodeUtil.SEED ;
    	hc = HashCodeUtil.hash(hc, this.getSystemId());
    	hc = HashCodeUtil.hash(hc, this.getRequestId());
    	hc = HashCodeUtil.hash(hc, this.getActionId());
    	
    	return hc ;
    }
    
    public boolean equals( Object obj )
    {
    	if( null == obj )
    		return false ;
    	
    	if( ! (obj instanceof Action) )
    		return false ;
    	
    	Action act = (Action) obj ;
    	
    	if( this.getSystemId() == act.getSystemId() 
    	 && this.getRequestId() == act.getRequestId() 
    	 && this.getActionId() == act.getActionId() )
    	{
    		return true ;
    	}
    	
    	return false ;
    		
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
    public int compareTo(Action aObject) {
        switch (ourSortField) {
        case SYSTEMID : {
            Integer i1 = mySystemId;
            Integer i2 = aObject.mySystemId;

            if (ourSortOrder == ASC_ORDER) {
                return i1.compareTo(i2);
            }

            return i2.compareTo(i1);
        }

        case REQUESTID : {
            Integer i1 = myRequestId;
            Integer i2 = aObject.myRequestId;

            if (ourSortOrder == ASC_ORDER) {
                return i1.compareTo(i2);
            }

            return i2.compareTo(i1);
        }

        case ACTIONID : {
            Integer i1 = myActionId;
            Integer i2 = aObject.myActionId;

            if (ourSortOrder == ASC_ORDER) {
                return i1.compareTo(i2);
            }

            return i2.compareTo(i1);
        }

        case CATEGORYID : {
            Integer i1 = myCategoryId;
            Integer i2 = aObject.myCategoryId;

            if (ourSortOrder == ASC_ORDER) {
                return i1.compareTo(i2);
            }

            return i2.compareTo(i1);
        }

        case STATUSID : {
            Integer i1 = myStatusId;
            Integer i2 = aObject.myStatusId;

            if (ourSortOrder == ASC_ORDER) {
                return i1.compareTo(i2);
            }

            return i2.compareTo(i1);
        }

        case SEVERITYID : {
            Integer i1 = mySeverityId;
            Integer i2 = aObject.mySeverityId;

            if (ourSortOrder == ASC_ORDER) {
                return i1.compareTo(i2);
            }

            return i2.compareTo(i1);
        }

        case REQUESTTYPEID : {
            Integer i1 = myRequestTypeId;
            Integer i2 = aObject.myRequestTypeId;

            if (ourSortOrder == ASC_ORDER) {
                return i1.compareTo(i2);
            }

            return i2.compareTo(i1);
        }

        case SUBJECT : {
            if (ourSortOrder == ASC_ORDER) {
                return mySubject.compareTo(aObject.mySubject);
            }

            return aObject.mySubject.compareTo(mySubject);
        }

        case DESCRIPTION : {
            if (ourSortOrder == ASC_ORDER) {
                return myDescription.compareTo(aObject.myDescription);
            }

            return aObject.myDescription.compareTo(myDescription);
        }

        case ISPRIVATE : {
            Boolean b1 = myIsPrivate;
            Boolean b2 = aObject.myIsPrivate;

            if (ourSortOrder == ASC_ORDER) {
                return b1.compareTo(b2);
            }

            return b2.compareTo(b1);
        }

        case PARENTREQUESTID : {
            Integer i1 = myParentRequestId;
            Integer i2 = aObject.myParentRequestId;

            if (ourSortOrder == ASC_ORDER) {
                return i1.compareTo(i2);
            }

            return i2.compareTo(i1);
        }

        case USERID : {
            Integer i1 = myUserId;
            Integer i2 = aObject.myUserId;

            if (ourSortOrder == ASC_ORDER) {
                return i1.compareTo(i2);
            }

            return i2.compareTo(i1);
        }

        case DUEDATE : {
            if (ourSortOrder == ASC_ORDER) {
                return myDueDate.compareTo(aObject.myDueDate);
            }

            return aObject.myDueDate.compareTo(myDueDate);
        }

        case LOGGEDDATE : {
            if (ourSortOrder == ASC_ORDER) {
                return myLoggedDate.compareTo(aObject.myLoggedDate);
            }

            return aObject.myLoggedDate.compareTo(myLoggedDate);
        }

        case LASTUPDATEDDATE : {
            if (ourSortOrder == ASC_ORDER) {
                return myLastUpdatedDate.compareTo(aObject.myLastUpdatedDate);
            }

            return aObject.myLastUpdatedDate.compareTo(myLastUpdatedDate);
        }

        case HEADERDESCRIPTION : {
            if (ourSortOrder == ASC_ORDER) {
                return myHeaderDescription.compareTo(aObject.myHeaderDescription);
            }

            return aObject.myHeaderDescription.compareTo(myHeaderDescription);
        }

        case ATTACHMENTS : {
            if (ourSortOrder == ASC_ORDER) {
                return myAttachments.compareTo(aObject.myAttachments);
            }

            return aObject.myAttachments.compareTo(myAttachments);
        }

        case SUMMARY : {
            if (ourSortOrder == ASC_ORDER) {
                return mySummary.compareTo(aObject.mySummary);
            }

            return aObject.mySummary.compareTo(mySummary);
        }

        case MEMO : {
            if (ourSortOrder == ASC_ORDER) {
                return myMemo.compareTo(aObject.myMemo);
            }

            return aObject.myMemo.compareTo(myMemo);
        }

        case APPENDINTERFACE : {
            Integer i1 = myAppendInterface;
            Integer i2 = aObject.myAppendInterface;

            if (ourSortOrder == ASC_ORDER) {
                return i1.compareTo(i2);
            }

            return i2.compareTo(i1);
        }

        case NOTIFY : {
            Integer i1 = myNotify;
            Integer i2 = aObject.myNotify;

            if (ourSortOrder == ASC_ORDER) {
                return i1.compareTo(i2);
            }

            return i2.compareTo(i1);
        }

        case NOTIFYLOGGERS : {
            Boolean b1 = myNotifyLoggers;
            Boolean b2 = aObject.myNotifyLoggers;

            if (ourSortOrder == ASC_ORDER) {
                return b1.compareTo(b2);
            }

            return b2.compareTo(b1);
        }

        case OFFICEID : {
            Integer i1 = myOfficeId;
            Integer i2 = aObject.myOfficeId;

            if (ourSortOrder == ASC_ORDER) {
                return i1.compareTo(i2);
            }

            return i2.compareTo(i1);
        }
        }

        return 0;
    }

    /*
     * This method creates an Action object from the resultset's current row.
     *
     * @param aRS Resultset object.
     *
     * @return Action Object.
     *
     * @exception SQLException incase of database errors.
     */
    public static Action createFromResultSet(ResultSet aRS) throws SQLException {
        ArrayList<Integer>          loggerList     = new ArrayList<Integer>();
        ArrayList<Integer>          assigneeList   = new ArrayList<Integer>();
        ArrayList<Integer>          subscriberList = new ArrayList<Integer>();
        ArrayList<Integer>          toList         = new ArrayList<Integer>();
        ArrayList<Integer>          ccList         = new ArrayList<Integer>();
        Hashtable<String, ActionEx> actionexTable  = new Hashtable<String, ActionEx>();
        
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
        
        Action action = new Action(aRS.getInt("sys_id"), aRS.getInt("request_id"), aRS.getInt("action_id"), aRS.getInt("category_id"), aRS.getInt("status_id"), aRS.getInt("severity_id"),
		                           aRS.getInt("request_type_id"), loggerList, assigneeList, subscriberList, toList, ccList, 
		                           ((aRS.getString("subject") != null) ? aRS.getString("subject"): ""), 
		                           ((aRS.getString("description") != null) ? aRS.getString("description"): ""), 
		                aRS.getBoolean("is_private"), aRS.getInt("parent_request_id"), aRS.getInt("user_id"), Timestamp.getTimestamp(aRS.getTimestamp("due_datetime", cal)),
		                       Timestamp.getTimestamp(aRS.getTimestamp("logged_datetime", cal)), Timestamp.getTimestamp(aRS.getTimestamp("lastupdated_datetime", cal)),
		                       ((aRS.getString("header_description") != null)
		                        ? aRS.getString("header_description")
		                        : ""), ((aRS.getString("attachments") != null)
		                                ? aRS.getString("attachments")
		                                : ""), (aRS.getString("summary") != null)
		        ? aRS.getString("summary"): null, ((aRS.getString("memo") != null)
		                 ? aRS.getString("memo")
		                 : ""), aRS.getInt("append_interface"), aRS.getInt("notify"), aRS.getBoolean("notify_loggers"), aRS.getInt("replied_to_action"), aRS.getInt("office_id"), actionexTable);
		
		try{
			action.setDescriptionContentType(aRS.getInt("description_content_type"));
		}catch(Exception e){
			e.printStackTrace();
		}
		
		try{
			action.setSummaryContentType(aRS.getInt("summary_content_type"));
		}catch(Exception e){
			e.printStackTrace();
		}
		
		return action;
    }

    /**
     * Accessor method for ccIds property
     *
     * @return The ArrayList of CC Ids
     *
     */
    public ArrayList<Integer> getccIds() {
        return myCcIds;
    }

    /**
     * Method to insert a Action object into database.
     *
     * @param aObject Object to be inserted
     */
    public static boolean insert(Action aObject) {

        // Insert logic here.
        if (aObject == null) {
            return false;
        }

        Connection aCon        = null;
        boolean    returnValue = false;

        try {
            aCon = DataSourcePool.getConnection();
            aCon.setAutoCommit(false);

            CallableStatement cs = aCon.prepareCall("stp_actions_insert " + "?, ?, ?, ?, ?, ?, ?, ?, ?, ?, " + "?, ?, ?, ?, ?, ?, ?, ?, ?, ?, " + "?, ?, ?, ?");

            aObject.setCallableParameters(cs);
            cs.execute();
            cs.close();
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
            returnValue = false;
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
     * Method to insert an action correpsonding to a transfer
     *
     * @param  aSystemId    the system id
     * @param  aRequestId   the request id
     * @param  aUserId      the user id
     *
     * @throws DatabaseException In case of any database related error
     */
    public static void insertTransferAction(int aSystemId, int aRequestId, int aUserId) throws DatabaseException {
        Connection connection = null;

        try {
            connection = DataSourcePool.getConnection();
            connection.setAutoCommit(false);

            CallableStatement cs = connection.prepareCall("stp_action_insertTransferAction ?, ?, ?");

            cs.setInt(1, aSystemId);
            cs.setInt(2, aRequestId);
            cs.setInt(3, aUserId);
            cs.execute();
            cs.close();
            cs = null;
            connection.commit();
        } catch (SQLException sqle) {
        	try {
        		if(connection != null)
					connection.rollback();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
            StringBuilder message = new StringBuilder();

            message.append("An exception occurred while inserting an action").append("corresponding to transfer..").append("\nSystem Id:  ").append(aSystemId).append("\nRequest Id: ").append(
                aRequestId).append("\nUser Id:    ").append(aUserId).append("\n");

            throw new DatabaseException(message.toString(), sqle);
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException sqle) {
                LOG.warning("Exception occured while closing the request");
            }
        }

        return;
    }
    
    /**
     * Method to get the action object associated with a request and action
     * Id for the given business area
     *
     * @param  aSystemId the system with which the request is associated with
     * @param  aRequestId the request for which the action are needed.
     * @param  aActionId  The actionId of the action done on request
     * @return the action object associated with the request and action Id.
     * @throws DatabaseException In case of any database related error
     */
    public static Action lookupBySystemIdAndRequestIdAndActionId(Connection connection, int aSystemId, int aRequestId, int aActionId) throws DatabaseException {
        Action     action     = null;

        try {

            CallableStatement cs = connection.prepareCall("stp_action_lookupById ?, ?, ?");

            cs.setInt(1, aSystemId);
            cs.setInt(2, aRequestId);
            cs.setInt(3, aActionId);

            // execute method returns a flag . It is true if the first
            // result is a resultSet object.
            boolean flag = cs.execute();

            if (flag == true) {
                ResultSet rsAction = cs.getResultSet();

                if (rsAction != null) {
                    while (rsAction.next() != false) {
                        action = createFromResultSet(rsAction);
                    }

                    // This statement is not required here as the Statement
                    // object closes this resultset when we request for the
                    // next result in the row.
                    // rsAction.close();
                }

                // this returns the actionUsers.It is used to populate
                // The aggigneeIds,SubscriberIds,toIds,fromIds and
                // cc Ids
                cs.getMoreResults();

                ResultSet rsActionUsers = cs.getResultSet();

                if (rsActionUsers != null) {
                    while (rsActionUsers.next() != false) {
                        action = getActionUsers(action, rsActionUsers);
                    }

                    // rsActionUsers.close();
                    // rsActionUsers = null;
                }
            }

            cs.getMoreResults();
            cs.close();
            cs = null;
        } catch (SQLException sqle) {
            StringBuilder message = new StringBuilder();

            message.append("An exception occurred while retrieving the Action ").append("details for").append("\nSystem Id : ").append(aSystemId).append("\nRequest Id  : ").append(aRequestId).append(
                "\nAction Id   : ").append(aActionId).append("\n");

            throw new DatabaseException(message.toString(), sqle);
        }

        return action;
    }

    /**
     * Method to get the action object associated with a request and action
     * Id for the given business area
     *
     * @param  aSystemId the system with which the request is associated with
     * @param  aRequestId the request for which the action are needed.
     * @param  aActionId  The actionId of the action done on request
     * @return the action object associated with the request and action Id.
     * @throws DatabaseException In case of any database related error
     */
    public static Action lookupBySystemIdAndRequestIdAndActionId(int aSystemId, int aRequestId, int aActionId) throws DatabaseException {
        Action     action     = null;
        Connection connection = null;

        try {
            connection = DataSourcePool.getConnection();

            action = lookupBySystemIdAndRequestIdAndActionId(connection, aSystemId, aRequestId, aActionId);
        } catch (SQLException sqle) {
            StringBuilder message = new StringBuilder();

            message.append("An exception occurred while retrieving the Action ").append("details for").append("\nSystem Id : ").append(aSystemId).append("\nRequest Id  : ").append(aRequestId).append(
                "\nAction Id   : ").append(aActionId).append("\n");

            throw new DatabaseException(message.toString(), sqle);
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException sqle) {
                LOG.warning("An Exception has occured while closing a request");
            }
        }

        return action;
    }

    /**
     * Method to return the source arraylist in the sorted order
     *
     * @param  source the array list of Action objects
     * @return the ArrayList of the Action objects in sorted order
     */
    public static ArrayList<Action> sort(ArrayList<Action> source) {
        int      size     = source.size();
        Action[] srcArray = new Action[size];

        for (int i = 0; i < size; i++) {
            srcArray[i] = source.get(i);
        }

        Arrays.sort(srcArray);

        ArrayList<Action> target = new ArrayList<Action>();

        for (int i = 0; i < size; i++) {
            target.add(srcArray[i]);
        }

        return target;
    }

    /**
     * Method to update the corresponding Action object in the database.
     *
     * @param aObject Object to be updated
     *
     * @return Update domain object.
     *
     */
    public static Action update(Action aObject) {

        // Update logic here.
        if (aObject == null) {
            return aObject;
        }

        Connection aCon = null;

        try {
            aCon = DataSourcePool.getConnection();
            aCon.setAutoCommit(false);

            CallableStatement cs = aCon.prepareCall("stp_actions_update " + "?, ?, ?, ?, ?, ?, ?, ?, ?, ?, " + "?, ?, ?, ?, ?, ?, ?, ?, ?, ?, " + "?, ?, ?, ?");

            aObject.setCallableParameters(cs);
            cs.execute();
            cs.close();
            aCon.commit();
        } catch (SQLException sqle) {
        	try {
        		if(aCon != null)
					aCon.rollback();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        }
        finally {
            if (aCon != null) {
                try {
                    aCon.close();
                } catch (SQLException sqle) {

                    // Should this be logged.?
                }
            }
        }

        return aObject;
    }

//  /
//  This method retrieves the Extended Fields related to this action.
//  
//  @param aExtendedFieldsList Out Parameter for Logger List.
//  /
//  private static Hashtable<String, ActionEx> getExtendedFields
//  (ResultSet aResultSet) throws SQLException
//  {
//  ActionEx ex = null;
//  Hashtable<String ,ActionEx> extendedFields = 
//    new Hashtable<String ,ActionEx>();
//  while (aResultSet.next()) 
//  {
//    String aFieldName = aResultSet.getString("name"); 
//    ex = ActionEx.createFromResultSet(aResultSet);
//    extendedFields.put(aFieldName, ex);
//  }
//  return extendedFields;
//  }

    /**
     * Method to update the action with the transfer information.
     *
     * @param  aSystemId      the source system id
     * @param  aRequestId     the source request id
     * @param  aTargetRequest the target request id.
     *
     * @throws DatabaseException In case of any database related error
     */
    public static void updateWithTransferInformation(int aSystemId, int aRequestId, String aTargetRequest) throws DatabaseException {
        Connection connection = null;

        try {
            connection = DataSourcePool.getConnection();
            connection.setAutoCommit(false);

            CallableStatement cs = connection.prepareCall("stp_action_updateWithTransferInfo ?, ?, ?");

            cs.setInt(1, aSystemId);
            cs.setInt(2, aRequestId);
            cs.setString(3, aTargetRequest);
            cs.execute();
            cs.close();
            cs = null;
            connection.commit();
        } catch (SQLException sqle) {
        	try {
        		if(connection != null)
					connection.rollback();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
            StringBuilder message = new StringBuilder();

            message.append("An exception occurred while updating a request that.").append("is transferred.").append("\nSource System Id:  ").append(aSystemId).append("\nSource Request Id: ").append(
                aRequestId).append("\nTarget Request:    ").append(aTargetRequest).append("\n");

            throw new DatabaseException(message.toString(), sqle);
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException sqle) {
                LOG.warning("Exception occured while closing the request");
            }
        }

        return;
    }

    //~--- get methods --------------------------------------------------------

    /**
     * Accessor method for ActionId property.
     *
     * @return Current Value of ActionId
     *
     */
    public int getActionId() {
        return myActionId;
    }

    /**
     * This method retrieves the Users related to this action.
     *
     * @param aAction        Action Object to be updated
     * @param aResultSet      ResultSet Object.
     */
    private static Action getActionUsers(Action aAction, ResultSet aResultSet) throws DatabaseException, SQLException {
        ArrayList<Integer> loggerList     = new ArrayList<Integer>();
        ArrayList<Integer> assigneeList   = new ArrayList<Integer>();
        ArrayList<Integer> subscriberList = new ArrayList<Integer>();
        ArrayList<Integer> toList         = new ArrayList<Integer>();
        ArrayList<Integer> ccList         = new ArrayList<Integer>();
        ActionUser         obj            = null;

        while (true) {
            obj = ActionUser.createFromResultSet(aResultSet);

            switch (obj.getUserTypeId()) {
            case UserType.LOGGER :
                loggerList.add(new Integer(obj.getUserId()));

                break;

            case UserType.ASSIGNEE :
                assigneeList.add(new Integer(obj.getUserId()));

                break;

            case UserType.SUBSCRIBER :
                subscriberList.add(new Integer(obj.getUserId()));

                break;

            case UserType.TO :
                toList.add(new Integer(obj.getUserId()));

                break;

            case UserType.CC :
                ccList.add(new Integer(obj.getUserId()));

                break;
            }

            if (aResultSet.next() == false) {
                break;
            }
        }

        aAction.setLoggerIds(loggerList);
        aAction.setAssigneeIds(assigneeList);
        aAction.setSubscriberIds(subscriberList);
        aAction.setToIds(toList);
        aAction.setCcIds(ccList);

        return aAction;
    }

    /**
     * Method to get all the actions associated with a request for the given
     * business area
     *
     * @param  aSystemId the system with which the request is associated with
     * @param  aRequestId the request for which the actions are needed.
     * @param  aSortOrder the order of sorting of actions, "asc" or "desc"
     *
     * @return the arrayList of all the actions associated with the request.
     * @throws DatabaseException In case of any database related error
     */
    public static ArrayList<Action> getAllActions(int aSystemId, int aRequestId, String aSortOrder) throws DatabaseException {
    	Connection conn = null;
		try {
			conn = DataSourcePool.getConnection();
			return getAllActions(conn, aSystemId, aRequestId, aSortOrder);
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
    public static ArrayList<Action> getAllActions(Connection connection, int aSystemId, int aRequestId, String aSortOrder) throws DatabaseException {
        ArrayList<Action> actionsList = new ArrayList<Action>();
        //Connection        connection  = null;

        try {
            //connection = DataSourcePool.getConnection();

            CallableStatement cs = connection.prepareCall("{Call stp_action_getAllActions(?, ?, ?)}");

            cs.setInt(1, aSystemId);
            cs.setInt(2, aRequestId);
            cs.setString(3, aSortOrder);

            ResultSet rs = cs.executeQuery();

            if (rs != null) {
                while (rs.next()) {
                    actionsList.add(createFromResultSet(rs));
                }

                rs.close();
                cs.close();
                rs = null;
                cs = null;
            }
        } catch (SQLException sqle) {
            StringBuilder message = new StringBuilder();

            message.append("An exception occurred while retrieving all the.").append("action objects for a given").append("\nSystem Id : ").append(aSystemId).append("\nRequest Id  : ").append(
                aRequestId).append("\n");

            throw new DatabaseException(message.toString(), sqle);
        }
//        } finally {
//            try {
//                if (connection != null) {
//                    connection.close();
//                }
//            } catch (SQLException sqle) {
//                LOG.warning("Exception occured while closing the request");
//            }
//        }

        return actionsList;
    }

    /**
     * Accessor method for AppendInterface property.
     *
     * @return Current Value of AppendInterface
     *
     */
    public int getAppendInterface() {
        return myAppendInterface;
    }

    /**
     * Accessor method for assigneeIds property.
     *
     * @return The ArrayList of Assignee Ids
     *
     */
    public ArrayList<Integer> getAssigneeIds() {
        return myAssigneeIds;
    }

    /**
     * Accessor method for Attachments property.
     *
     * @return Current Value of Attachments
     *
     */
    public String getAttachments() {
        return myAttachments;
    }

    /**
     * Accessor method for CategoryId property.
     *
     * @return Current Value of CategoryId
     *
     */
    public int getCategoryId() {
        return myCategoryId;
    }

    /**
     * Accessor method for Description property.
     *
     * @return Current Value of Description
     *
     */
    public String getDescription() {
        return myDescription;
    }

    /*
     * Method to get the action object associated with a request and action
     * Id for the given business area
     *
     * @param  aDiffArrayList   List of Diff Entries.
     * @param  aSystemId        the system with which the request is associated
     *                          with
     * @param  aRequestId       the request for which the action are needed.
     * @param  aRepliedToAction The actionId of the action done on request
     * @param  aCurrentActionId Current action id.
     *
     * @return the action object associated with the request and action Id.
     *
     * @throws DatabaseException In case of any database related error
     */
    public static ArrayList<String> getDiffUserList(ArrayList<DiffEntry> aDiffArrayList, int aSystemId, int aRequestId, int aRepliedToAction, int aCurrentActionId) throws DatabaseException {
        ArrayList<String>   diffUserList  = new ArrayList<String>();
        ArrayList<Action>   diffActions   = new ArrayList<Action>();
        ArrayList<ActionEx> diffActionsEx = new ArrayList<ActionEx>();
        int                 userId;
        User                user = null;
        Action              act  = Action.lookupBySystemIdAndRequestIdAndActionId(aSystemId, aRequestId, aCurrentActionId);

        if ((aCurrentActionId - aRepliedToAction) == 1) {
            userId = act.getUserId();
            user   = User.lookupByUserId(userId);

            for (DiffEntry de : aDiffArrayList) {
                diffUserList.add(user.getUserLogin() + "," + act.getActionId());
            }

            return diffUserList;
        }

        Connection connection = null;

        try {
            connection = DataSourcePool.getConnection();

            CallableStatement cs = connection.prepareCall("{Call stp_action_getDiffActions(?, ?, ?, ?)}");

            cs.setInt(1, aSystemId);
            cs.setInt(2, aRequestId);
            cs.setInt(3, aRepliedToAction);
            cs.setInt(4, aCurrentActionId);

            boolean flag = cs.execute();

            if (flag == true) {
                ResultSet rsActions = cs.getResultSet();
                Action    rsAction  = null;
                ActionEx  actEx     = null;

                while ((rsActions != null) && (rsActions.next() != false)) {
                    rsAction = createFromResultSet(rsActions);
                    diffActions.add(rsAction);
                }

                cs.getMoreResults();

                ResultSet rsActionUsers = cs.getResultSet();
                int       count         = 0;

                if (rsActionUsers != null) {
                    while (rsActionUsers.next() != false) {
                        rsAction = diffActions.get(count);
                        rsAction = getActionUsers(act, rsActionUsers);
                        diffActions.add(count, rsAction);
                        count++;
                    }
                }

                cs.getMoreResults();

                ResultSet rsActionsEx = cs.getResultSet();

                if (rsActionsEx != null) {
                    while (rsActionsEx.next() != false) {
                        actEx = ActionEx.createFromResultSet(rsActionsEx);
                        diffActionsEx.add(actEx);
                    }
                }

                cs.close();
                cs = null;
            }
        } catch (SQLException sqle) {
            StringBuilder message = new StringBuilder();

            message.append("An exception occurred while retrieving DiffUsers ").append("details for").append("\nReplied To Action : ").append(aRepliedToAction).append("\nCurrent Action  : ").append(
                aCurrentActionId).append("\n");

            throw new DatabaseException(message.toString(), sqle);
        }
        finally
        {
        	if(connection != null)
        	{
        		try {
					connection.close();
				} catch (SQLException e) {
					LOG.warn("Unable to close connection while retirving diff users.", e);
				}
        	}
        }

        for (DiffEntry de : aDiffArrayList) {
            String fieldName = de.getName();

            if (fieldName.equals(Field.CATEGORY) == true) {
                int    categoryId = act.getCategoryId();
                Action action     = null;
                int    size       = diffActions.size();

                for (int i = 0; i < size; i++) {
                    action = diffActions.get(i);

                    int actCategoryId = action.getCategoryId();

                    if (actCategoryId != categoryId) {
                        action = diffActions.get(i - 1);
                        userId = action.getUserId();
                        user   = User.lookupByUserId(userId);
                        diffUserList.add(user.getUserLogin() + "," + action.getActionId());

                        break;
                    }
                }
            }

            if (fieldName.equals(Field.STATUS) == true) {
                int    statusId = act.getStatusId();
                Action action   = null;
                int    size     = diffActions.size();

                for (int i = 0; i < size; i++) {
                    action = diffActions.get(i);

                    int actStatusId = action.getStatusId();

                    if (actStatusId != statusId) {
                        action = diffActions.get(i - 1);
                        userId = action.getUserId();
                        user   = User.lookupByUserId(userId);
                        diffUserList.add(user.getUserLogin() + "," + action.getActionId());

                        break;
                    }
                }
            }

            if (fieldName.equals(Field.SEVERITY) == true) {
                int    severityId = act.getSeverityId();
                Action action     = null;
                int    size       = diffActions.size();

                for (int i = 0; i < size; i++) {
                    action = diffActions.get(i);

                    int actSeverityId = action.getSeverityId();

                    if (actSeverityId != severityId) {
                        action = diffActions.get(i - 1);
                        userId = action.getUserId();
                        user   = User.lookupByUserId(userId);
                        diffUserList.add(user.getUserLogin() + "," + action.getActionId());

                        break;
                    }
                }
            }

            if (fieldName.equals(Field.REQUEST_TYPE) == true) {
                int    requestTypeId = act.getRequestTypeId();
                Action action        = null;
                int    size          = diffActions.size();

                for (int i = 0; i < size; i++) {
                    action = diffActions.get(i);

                    int actRequestTypeId = action.getRequestTypeId();

                    if (actRequestTypeId != requestTypeId) {
                        action = diffActions.get(i - 1);
                        userId = action.getUserId();
                        user   = User.lookupByUserId(userId);
                        diffUserList.add(user.getUserLogin() + "," + action.getActionId());

                        break;
                    }
                }
            }

            if (fieldName.equals(Field.LOGGER) == true) {
                ArrayList<Integer> loggerList = act.getLoggerIds();
                Action             action     = null;
                int                size       = diffActions.size();

                for (int i = 0; i < size; i++) {
                    action = diffActions.get(i);

                    ArrayList<Integer> actLoggerList = action.getLoggerIds();

                    if (isChanged(loggerList, actLoggerList) == true) {
                        action = diffActions.get(i - 1);
                        userId = action.getUserId();
                        user   = User.lookupByUserId(userId);
                        diffUserList.add(user.getUserLogin() + "," + action.getActionId());

                        break;
                    }
                }
            }

            if (fieldName.equals(Field.ASSIGNEE) == true) {
                ArrayList<Integer> assigneeList = act.getAssigneeIds();
                Action             action       = null;
                int                size         = diffActions.size();

                for (int i = 0; i < size; i++) {
                    action = diffActions.get(i);

                    ArrayList<Integer> actAssigneeList = action.getAssigneeIds();

                    if (isChanged(assigneeList, actAssigneeList) == true) {
                        action = diffActions.get(i - 1);
                        userId = action.getUserId();
                        user   = User.lookupByUserId(userId);
                        diffUserList.add(user.getUserLogin() + "," + action.getActionId());

                        break;
                    }
                }
            }

            if (fieldName.equals(Field.SUBSCRIBER) == true) {
                ArrayList<Integer> subscriberList = act.getSubscriberIds();
                Action             action         = null;
                int                size           = diffActions.size();

                for (int i = 0; i < size; i++) {
                    action = diffActions.get(i);

                    ArrayList<Integer> actSubscriberList = action.getSubscriberIds();

                    if (isChanged(subscriberList, actSubscriberList) == true) {
                        action = diffActions.get(i - 1);
                        userId = action.getUserId();
                        user   = User.lookupByUserId(userId);
                        diffUserList.add(user.getUserLogin() + "," + action.getActionId());

                        break;
                    }
                }
            }

            if (fieldName.equals(Field.SUBJECT) == true) {
                String subject = act.getSubject();

                System.out.println("subject " + subject);

                Action action = null;
                int    size   = diffActions.size();

                for (int i = 0; i < size; i++) {
                    action = diffActions.get(i);

                    String actSubject = action.getSubject();

                    System.out.println("subject " + actSubject);

                    if (actSubject.equalsIgnoreCase(subject) == false) {
                        action = diffActions.get(i - 1);
                        userId = action.getUserId();
                        user   = User.lookupByUserId(userId);
                        diffUserList.add(user.getUserLogin() + "," + action.getActionId());

                        break;
                    }
                }
            }

            if (fieldName.equals(Field.SUMMARY) == true) {
                String summary = act.getSummary();

                System.out.println("summary " + summary);

                Action action = null;
                int    size   = diffActions.size();

                for (int i = 0; i < size; i++) {
                    action = diffActions.get(i);

                    String actSummary = action.getSummary();

                    if (actSummary.equalsIgnoreCase(summary) == false) {
                        action = diffActions.get(i - 1);
                        userId = action.getUserId();
                        user   = User.lookupByUserId(userId);
                        diffUserList.add(user.getUserLogin() + "," + action.getActionId());

                        break;
                    }
                }
            }
        }

        return diffUserList;
    }

    /**
     * Accessor method for DueDate property.
     *
     * @return Current Value of DueDate
     *
     */
    public Timestamp getDueDate() {
        return myDueDate;
    }

    /**
     * Access method for the extendedFields property
     *
     * @return the current value of the extendedFields property
     */
    public final Hashtable<String, ActionEx> getExtendedFields() {
        return myExtendedFields;
    }

    /**
     * Accessor method for HeaderDescription property.
     *
     * @return Current Value of HeaderDescription
     *
     */
    public String getHeaderDescription() {
        return myHeaderDescription;
    }

    /**
     * Accessor method for IsPrivate property.
     *
     * @return Current Value of IsPrivate
     *
     */
    public boolean getIsPrivate() {
        return myIsPrivate;
    }

    /**
     * Accessor method for LastUpdatedDate property.
     *
     * @return Current Value of LastUpdatedDate
     *
     */
    public Timestamp getLastUpdatedDate() {
        return myLastUpdatedDate;
    }

    /**
     * Accessor method for LoggedDate property.
     *
     * @return Current Value of LoggedDate
     *
     */
    public Timestamp getLoggedDate() {
        return myLoggedDate;
    }

    /**
     * Accessor method for loggerIds  property.
     *
     * @return The ArrayList of Logger Ids
     *
     */
    public ArrayList<Integer> getLoggerIds() {
        return myLoggerIds;
    }

    /**
     * Accessor method for Memo property.
     *
     * @return Current Value of Memo
     *
     */
    public String getMemo() {
        return myMemo;
    }

    /**
     * Accessor method for Notify property.
     *
     * @return Current Value of Notify
     *
     */
    public int getNotify() {
        return myNotify;
    }

    /**
     * Accessor method for NotifyLoggers property.
     *
     * @return Current Value of NotifyLoggers
     *
     */
    public boolean getNotifyLoggers() {
        return myNotifyLoggers;
    }

    /**
     * Accessor method for OfficeId property.
     *
     * @return Current Value of OfficeId
     *
     */
    public int getOfficeId() {
        return myOfficeId;
    }

    /**
     * Accessor method for ParentRequestId property.
     *
     * @return Current Value of ParentRequestId
     *
     */
    public int getParentRequestId() {
        return myParentRequestId;
    }

    /**
     * Accessor method for RepliedToAction property.
     *
     * @return Current Value of RepliedToAction
     *
     */
    public int getRepliedToAction() {
        return myRepliedToAction;
    }

    /**
     * Accessor method for RequestId property.
     *
     * @return Current Value of RequestId
     *
     */
    public int getRequestId() {
        return myRequestId;
    }

    /**
     * Accessor method for RequestTypeId property.
     *
     * @return Current Value of RequestTypeId
     *
     */
    public int getRequestTypeId() {
        return myRequestTypeId;
    }

    /**
     * Accessor method for SeverityId property.
     *
     * @return Current Value of SeverityId
     *
     */
    public int getSeverityId() {
        return mySeverityId;
    }

    /**
     * Accessor method for StatusId property.
     *
     * @return Current Value of StatusId
     *
     */
    public int getStatusId() {
        return myStatusId;
    }

    /**
     * Accessor method for Subject property.
     *
     * @return Current Value of Subject
     *
     */
    public String getSubject() {
        return mySubject;
    }

    /**
     * Accessor method for subscriberIds property.
     *
     * @return The ArrayList of Subscriber Ids
     *
     */
    public ArrayList<Integer> getSubscriberIds() {
        return mySubscriberIds;
    }

    /**
     * Accessor method for Summary property.
     *
     * @return Current Value of Summary
     *
     */
    public String getSummary() {
        return mySummary;
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
     * Accessor method for To Ids property.
     *
     * @return The ArrayList of To Ids
     *
     */
    public ArrayList<Integer> getToIds() {
        return myToIds;
    }

    /**
     * Accessor method for UserId property.
     *
     * @return Current Value of UserId
     *
     */
    public int getUserId() {
        return myUserId;
    }

    public static boolean isChanged(ArrayList<Integer> aNewList, ArrayList<Integer> aOldList) {
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
            for (Integer actUser : aNewList) {
                if (aOldList.contains(actUser) == false) {
                    returnValue = true;

                    break;
                }
            }
        }

        return returnValue;
    }

    //~--- set methods --------------------------------------------------------

    /**
     * Mutator method for ActionId property.
     *
     * @param aActionId New Value for ActionId
     *
     */
    public void setActionId(int aActionId) {
        myActionId = aActionId;
    }

    /**
     * Mutator method for AppendInterface property.
     *
     * @param aAppendInterface New Value for AppendInterface
     *
     */
    public void setAppendInterface(int aAppendInterface) {
        myAppendInterface = aAppendInterface;
    }

    /**
     * Mutator method for assigneeIds  property.
     *
     * @param aAssigneeIds ArrayList of Assignee Ids
     *
     */
    public void setAssigneeIds(ArrayList<Integer> aAssigneeIds) {
        myAssigneeIds = aAssigneeIds;
    }

    /**
     * Mutator method for Attachments property.
     *
     * @param aAttachments New Value for Attachments
     *
     */
    public void setAttachments(String aAttachments) {
        myAttachments = aAttachments;
    }

    /**
     * This method sets the parameters in the CallableStatement.
     *
     * @param aCS     CallableStatement whose params should be set.
     *
     * @exception SQLException
     */
    public void setCallableParameters(CallableStatement aCS) throws SQLException {
        aCS.setInt(SYSTEMID, mySystemId);
        aCS.setInt(REQUESTID, myRequestId);
        aCS.setInt(ACTIONID, myActionId);
        aCS.setInt(CATEGORYID, myCategoryId);
        aCS.setInt(STATUSID, myStatusId);
        aCS.setInt(SEVERITYID, mySeverityId);
        aCS.setInt(REQUESTTYPEID, myRequestTypeId);
        aCS.setString(SUBJECT, mySubject);
        aCS.setString(DESCRIPTION, myDescription);
        aCS.setInt(DESCRIPTION + "_content_type", myDescriptionContentType);
        aCS.setBoolean(ISPRIVATE, myIsPrivate);
        aCS.setInt(PARENTREQUESTID, myParentRequestId);
        aCS.setInt(USERID, myUserId);
        aCS.setTimestamp(DUEDATE, myDueDate.toSqlTimestamp());
        aCS.setTimestamp(LOGGEDDATE, myLoggedDate.toSqlTimestamp());
        aCS.setTimestamp(LASTUPDATEDDATE, myLastUpdatedDate.toSqlTimestamp());
        aCS.setString(HEADERDESCRIPTION, myHeaderDescription);
        aCS.setString(ATTACHMENTS, myAttachments);
        aCS.setString(SUMMARY, mySummary);
        aCS.setInt(SUMMARY + "_content_type", mySummaryContentType);
        aCS.setString(MEMO, myMemo);
        aCS.setInt(APPENDINTERFACE, myAppendInterface);
        aCS.setInt(NOTIFY, myNotify);
        aCS.setBoolean(NOTIFYLOGGERS, myNotifyLoggers);
        aCS.setInt(REPLIEDTOACTION, myRepliedToAction);
        aCS.setInt(OFFICEID, myOfficeId);
    }

    /**
     * Mutator method for CategoryId property.
     *
     * @param aCategoryId New Value for CategoryId
     *
     */
    public void setCategoryId(int aCategoryId) {
        myCategoryId = aCategoryId;
    }

    /**
     * Mutator method for ccIds  property.
     *
     * @param aCcIds ArrayList of Cc Ids
     *
     */
    public void setCcIds(ArrayList<Integer> aCcIds) {
        myCcIds = aCcIds;
    }

    /**
     * Mutator method for Description property.
     *
     * @param aDescription New Value for Description
     *
     */
    public void setDescription(String aDescription) {
        myDescription = aDescription;
    }

    /**
     * Mutator method for DueDate property.
     *
     * @param aDueDate New Value for DueDate
     *
     */
    public void setDueDate(Timestamp aDueDate) {
        myDueDate = aDueDate;
    }

    /**
     * Mutator method for the extendedFields property
     *
     * @param aExtendedFields value of extendedFields property
     */
    public final void setExtendedFields(Hashtable<String, ActionEx> aExtendedFields) {
        if (aExtendedFields == null) {
            return;
        }

        myExtendedFields = aExtendedFields;
    }

    /**
     * Mutator method for HeaderDescription property.
     *
     * @param aHeaderDescription New Value for HeaderDescription
     *
     */
    public void setHeaderDescription(String aHeaderDescription) {
        myHeaderDescription = aHeaderDescription;
    }

    /**
     * Mutator method for IsPrivate property.
     *
     * @param aIsPrivate New Value for IsPrivate
     *
     */
    public void setIsPrivate(boolean aIsPrivate) {
        myIsPrivate = aIsPrivate;
    }

    /**
     * Mutator method for LastUpdatedDate property.
     *
     * @param aLastUpdatedDate New Value for LastUpdatedDate
     *
     */
    public void setLastUpdatedDate(Timestamp aLastUpdatedDate) {
        myLastUpdatedDate = aLastUpdatedDate;
    }

    /**
     * Mutator method for LoggedDate property.
     *
     * @param aLoggedDate New Value for LoggedDate
     *
     */
    public void setLoggedDate(Timestamp aLoggedDate) {
        myLoggedDate = aLoggedDate;
    }

    /**
     * Mutator method for loggerIds  property.
     *
     * @param aLoggerIds ArrayList of Logger Ids
     *
     */
    public void setLoggerIds(ArrayList<Integer> aLoggerIds) {
        myLoggerIds = aLoggerIds;
    }

    /**
     * Mutator method for Memo property.
     *
     * @param aMemo New Value for Memo
     *
     */
    public void setMemo(String aMemo) {
        myMemo = aMemo;
    }

    /**
     * Mutator method for Notify property.
     *
     * @param aNotify New Value for Notify
     *
     */
    public void setNotify(int aNotify) {
        myNotify = aNotify;
    }

    /**
     * Mutator method for NotifyLoggers property.
     *
     * @param aNotifyLoggers New Value for NotifyLoggers
     *
     */
    public void setNotifyLoggers(boolean aNotifyLoggers) {
        myNotifyLoggers = aNotifyLoggers;
    }

    /**
     * Mutator method for OfficeId property.
     *
     * @param aOfficeId New Value for OfficeId
     *
     */
    public void setOfficeId(int aOfficeId) {
        myOfficeId = aOfficeId;
    }

    /**
     * Mutator method for ParentRequestId property.
     *
     * @param aParentRequestId New Value for ParentRequestId
     *
     */
    public void setParentRequestId(int aParentRequestId) {
        myParentRequestId = aParentRequestId;
    }

    /**
     * Mutator method for RepliedToAction property.
     *
     * @param aRepliedToAction New Value for aRepliedToAction
     *
     */
    public void setRepliedToAction(int aRepliedToAction) {
        myRepliedToAction = aRepliedToAction;
    }

    /**
     * Mutator method for RequestId property.
     *
     * @param aRequestId New Value for RequestId
     *
     */
    public void setRequestId(int aRequestId) {
        myRequestId = aRequestId;
    }

    /**
     * Mutator method for RequestTypeId property.
     *
     * @param aRequestTypeId New Value for RequestTypeId
     *
     */
    public void setRequestTypeId(int aRequestTypeId) {
        myRequestTypeId = aRequestTypeId;
    }

    /**
     * Mutator method for SeverityId property.
     *
     * @param aSeverityId New Value for SeverityId
     *
     */
    public void setSeverityId(int aSeverityId) {
        mySeverityId = aSeverityId;
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
     * Mutator method for StatusId property.
     *
     * @param aStatusId New Value for StatusId
     *
     */
    public void setStatusId(int aStatusId) {
        myStatusId = aStatusId;
    }

    /**
     * Mutator method for Subject property.
     *
     * @param aSubject New Value for Subject
     *
     */
    public void setSubject(String aSubject) {
        mySubject = aSubject;
    }

    /**
     * Mutator method for subscriberIds  property.
     *
     * @param aSubscriberIds ArrayList of Subscriber Ids
     *
     */
    public void setSubscriberIds(ArrayList<Integer> aSubscriberIds) {
        mySubscriberIds = aSubscriberIds;
    }

    /**
     * Mutator method for Summary property.
     *
     * @param aSummary New Value for Summary
     *
     */
    public void setSummary(String aSummary) {
        mySummary = aSummary;
    }

    /**
     * Mutator method for SystemId property.
     *
     * @param aSystemId New Value for SystemId
     *
     */
    public void setSystemId(int aSystemId) {
        mySystemId = aSystemId;
    }

    /**
     * Mutator method for toIds  property.
     *
     * @param aToIds ArrayList of To Ids
     *
     */
    public void setToIds(ArrayList<Integer> aToIds) {
        myToIds = aToIds;
    }

    /**
     * Mutator method for UserId property.
     *
     * @param aUserId New Value for UserId
     *
     */
    public void setUserId(int aUserId) {
        myUserId = aUserId;
    }

    public static Hashtable<Integer, Collection<ActionFileInfo>> getAllActionFiles(int aSystemId, int aRequestId) throws DatabaseException {
    	Connection conn = null;
		try {
			conn = DataSourcePool.getConnection();
			return getAllActionFiles(conn, aSystemId, aRequestId);
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
    

    public String toString()
    {
    	return "{sys_id= " + this.mySystemId + " request_id=" + this.myRequestId + " action_id=" + this.myActionId + "   Subject=" + this.mySubject + "}" ;
    }
    
	public static Hashtable<Integer, Collection<ActionFileInfo>> getAllActionFiles(Connection conn,
			int aSystemId, int aRequestId) throws DatabaseException {
		// TODO Auto-generated method stub
		Hashtable<Integer, Collection<ActionFileInfo>> output = new Hashtable<Integer, Collection<ActionFileInfo>>();
		try
		{
			PreparedStatement ps = conn.prepareStatement("select v.sys_id as sys_id,v.request_id as request_id,v.action_id as action_id, " +
					" v.attachment as name, v.file_action as file_action, v.field_id as field_id, v.file_id as file_id, v.request_file_id as request_file_id, " +
					" fri.location as location, fri.size as size, fri.hash as hash, fri.security_code as security_code"+
					" from versions v JOIN file_repo_index fri on fri.id = v.file_id where v.sys_id = ? and v.request_id = ?");
			ps.setInt(1, aSystemId);
			ps.setInt(2, aRequestId);
			ResultSet result = ps.executeQuery();
			while(result.next())
			{
				Integer actionId = new Integer(result.getInt("action_id"));
				Collection<ActionFileInfo> files = output.get(actionId);
				if(files == null)
				{
					files = new ArrayList<ActionFileInfo>();
					output.put(actionId, files);
				}
//				public ActionFileInfo(int sysId, int reqId, int actionId, String name, String fileAction, int fieldId,
//						Integer fileId, int requestFileId, String location, int size) 
				ActionFileInfo actionFileInfo = new ActionFileInfo( result.getInt("sys_id"), result.getInt("request_id"),
												result.getInt("action_id"), result.getString("name"), result.getString("file_action"), 
												result.getInt("field_id"), result.getInt("file_id"), result.getInt("request_file_id"), 
												result.getString("location"), result.getInt("size"), result.getString("hash"), result.getInt("security_code"));
				files.add(actionFileInfo);
			}
		} catch (SQLException sqle) {
            StringBuilder message = new StringBuilder();

            message.append("An exception occurred while retrieving all the.").append("action objects for a given").append("\nSystem Id : ").append(aSystemId).append("\nRequest Id  : ").append(
                aRequestId).append("\n");

            throw new DatabaseException(message.toString(), sqle);
        }
		return output;
	}

	public void setDescriptionContentType(int myDescriptionContentType) {
		this.myDescriptionContentType = myDescriptionContentType;
	}

	public int getDescriptionContentType() {
		return myDescriptionContentType;
	}

	public void setSummaryContentType(int mySummaryContentType) {
		this.mySummaryContentType = mySummaryContentType;
	}

	public int getSummaryContentType() {
		return mySummaryContentType;
	}
}
