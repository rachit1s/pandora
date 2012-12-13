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
 * UserDraft.java
 *
 * $Header:
 *
 */
package transbit.tbits.domain;

//~--- non-JDK imports --------------------------------------------------------

import static java.sql.Types.INTEGER;
import static transbit.tbits.Helper.TBitsConstants.PKG_DOMAIN;

import java.io.Serializable;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import transbit.tbits.common.DataSourcePool;
import transbit.tbits.common.DatabaseException;
import transbit.tbits.common.TBitsLogger;
import transbit.tbits.common.Timestamp;
import transbit.tbits.config.DraftConfig;

//Third party imports.

/**
 * This class is the domain object corresponding to the user_drafts
 * table in the database.
 *
 * @author  :
 * @version : $Id: $
 *
 */
public class UserDraft implements Serializable {
    public static final TBitsLogger LOG = TBitsLogger.getLogger(PKG_DOMAIN);

    // Enum sort of fields for Attributes.
    private static final int USERID    = 1;
    private static final int TIMESTAMP = 2;
    private static final int SYSTEMID  = 3;
    private static final int REQUESTID = 4;
    private static final int DRAFT     = 5;

    //~--- fields -------------------------------------------------------------

    private String    myDraft;
    private int       myRequestId;
    private int       mySystemId;
    private Timestamp myTimestamp;
    private int myDraftId;
    // Attributes of this Domain Object.
    private int myUserId;

    //~--- constructors -------------------------------------------------------

    /**
     * The default constructor.
     */
    public UserDraft() {}

    /**
     * The complete constructor.
     *
     *  @param aUserId
     *  @param aTimestamp
     *  @param aSystemId
     *  @param aRequestId
     *  @param aDraft
     */
    public UserDraft(int aUserId, Timestamp aTimestamp, int aSystemId, int aRequestId, int aDraftId, String aDraft) {
        myUserId    = aUserId;
        myTimestamp = aTimestamp;
        mySystemId  = aSystemId;
        myRequestId = aRequestId;
        myDraft     = aDraft;
        myDraftId = aDraftId;
    }

    //~--- methods ------------------------------------------------------------

    /**
     * This method constructs a UserDarft object from the resultset.
     *
     * @param aRS    ResultSet which points to a UserDarft record.
     *
     * @return UserDarft object
     *
     * @exception SQLException
     */
    public static UserDraft createFromResultSet(ResultSet aRS) throws SQLException, Exception {
        UserDraft ud = new UserDraft(aRS.getInt("user_id"), Timestamp.getTimestamp(aRS.getTimestamp("time_stamp")), 
        		aRS.getInt("sys_id"), aRS.getInt("request_id"),aRS.getInt("draft_id"), 
        		((aRS.getBytes("draft") != null) ? aRS.getString("draft") : ""));

        return ud;
    }

    /**
     * Method to delete the corresponding UserDraft object
     * in the database.
     *
     * @param aObject Object to be updated
     *
     *
     */
    public static void delete(UserDraft aObject) {

        // delete logic here.
        if (aObject == null) {
            return;
        }

        Connection aCon = null;

        try {
            aCon = DataSourcePool.getConnection();
            aCon.setAutoCommit(false);

            CallableStatement cs = aCon.prepareCall("stp_user_draft_delete ?, ?, ?");

            cs.setInt(1, aObject.getUserId());
            //cs.setTimestamp(TIMESTAMP, aObject.getTimestamp().toSqlTimestamp());
            cs.setInt(2, aObject.getSystemId());
            cs.setInt(3, aObject.getDraftId());
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
            LOG.severe("",(sqle));
        } finally {
            if (aCon != null) {
                try {
                    aCon.close();
                } catch (SQLException sqle) {
                    LOG.severe("",(sqle));
                }
            }
        }

        return;
    }

    /**
     * Method to insert a UserDraft object into database.
     *
     * @param aObject Object to be inserted
     *
     */
    public static boolean insert(UserDraft aObject) throws Exception {

        // Insert logic here.
        if (aObject == null) {
            return false;
        }

        Connection aCon        = null;
        boolean    returnValue = false;

        try {
            aCon = DataSourcePool.getConnection();
            //aCon.setAutoCommit(false);

            CallableStatement cs = aCon.prepareCall("stp_user_draft_insert ?, ?, ?, ?, ?, ?");

            aObject.setCallableParameters(cs);
            cs.registerOutParameter(6, INTEGER);
            
            cs.execute();
            aObject.setDraftId(cs.getInt(6));
            cs.close();
            returnValue = true;
            //aCon.commit();
        } catch (SQLException sqle) {
//        	try {
//        		if(aCon != null)
//					aCon.rollback();
//			} catch (SQLException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
            LOG.severe("",(sqle));
            returnValue = false;
        } finally {
            if (aCon != null) {
                try {
                    aCon.close();
                } catch (SQLException sqle) {
                    LOG.severe("",(sqle));
                }
            }
        }

        return returnValue;
    }

    public void setDraftId(int aDraftId) {
		myDraftId = aDraftId;
		
	}

	/**
     * This method returns the Draft objects corresponding to the specified
     * user id.
     *
     * @param aUserId  Id of the User.
     *
     * @return List of User Drafts.
     *
     * @exception DatabaseException incase of any database related errors.
     */
    public static ArrayList<UserDraft> lookupByUserId(int aUserId) throws DatabaseException, Exception {
        ArrayList<UserDraft> drafts     = new ArrayList<UserDraft>();
        Connection           connection = null;

        try {
            connection = DataSourcePool.getConnection();

            CallableStatement cs = connection.prepareCall("stp_user_draft_lookupByUserId ?");

            cs.setInt(1, aUserId);

            ResultSet rs = cs.executeQuery();

            if (rs != null) {
                while (rs.next() != false) {
                    UserDraft ud = createFromResultSet(rs);

                    drafts.add(ud);
                }

                rs.close();
                rs = null;
            }

            cs.close();
            cs = null;
        } catch (SQLException sqle) {
            StringBuilder message = new StringBuilder();

            message.append("An exception occurred while retrieving the ").append("UserDarft Object.").append("\nUser Id: ").append(aUserId).append("\n");

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

        return drafts;
    }

    /**
     * This method returns the UserDrafts object corresponding to
     * the given userId, sysId and request Id
     *
     * @param aUserId
     * @param aSystemId
     * @param aRequestId
     *
     * @return  ArrayList of UserDraft Objects
     *
     * @exception DatabaseException incase of any error while interacting with
     *            the database.
     */
    public static ArrayList<UserDraft> lookupByUserIdAndSystemIdAndRequestId(int aUserId, int aSystemId, int aRequestId) throws DatabaseException, Exception {
        ArrayList<UserDraft> drafts     = new ArrayList<UserDraft>();
        UserDraft            ud         = null;
        Connection           connection = null;

        try {
            connection = DataSourcePool.getConnection();

            CallableStatement cs = connection.prepareCall("stp_user_draft_lookupByUserIdAndSystemIdAndRequestId " + "?, ? ,?");

            cs.setInt(1, aUserId);
            cs.setInt(2, aSystemId);
            cs.setInt(3, aRequestId);

            ResultSet rs = cs.executeQuery();

            if (rs != null) {
                while (rs.next() != false) {
                    ud = createFromResultSet(rs);
                    drafts.add(ud);
                }

                // Close the result set.
                rs.close();
            }

            // Close the statement.
            cs.close();

            //
            // Release the memory by nullifying the references so that these
            // are recovered by the Garbage collector.
            //
            rs = null;
            cs = null;
        } catch (SQLException sqle) {
            StringBuilder message = new StringBuilder();

            message.append("An exception occurred while retrieving the ").append("UserDarft Object.").append("\nUser Id: ").append(aUserId).append("\nSystem Id: ").append(aSystemId).append(
                "\nRequest Id: ").append(aRequestId).append("\n");

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

        return drafts;
    }

    
    /**
     * This method returns the UserDraft object corresponding to
     * the given userId, sysId and request Id and Timestamp
     *
     * @param aUserId
     * @param aSystemId
     * @param aRequestId
     * @param aTimestamp
     *
     * @return   UserDraft Objects
     *
     * @exception DatabaseException incase of any error while interacting with
     *            the database.
     */
    public static UserDraft lookupByUserIdAndSystemIdAndDraftId(int aUserId, int aSystemId, int aDraftId) throws DatabaseException, Exception {
        UserDraft  ud         = null;
        Connection connection = null;

        try {
            connection = DataSourcePool.getConnection();

            CallableStatement cs = connection.prepareCall("stp_user_draft_lookupByUserIdAndSystemIdAndDraftId ?, ?, ?");

            cs.setInt(1, aUserId);
            cs.setInt(2, aSystemId);
            cs.setInt(3, aDraftId);
            
            ResultSet rs = cs.executeQuery();

            if (rs != null) {
                if (rs.next() != false) {
                    ud = createFromResultSet(rs);
                }

                // Close the result set.
                rs.close();
            }

            // Close the statement.
            cs.close();

            //
            // Release the memory by nullifying the references so that these
            // are recovered by the Garbage collector.
            //
            rs = null;
            cs = null;
        } catch (SQLException sqle) {
            StringBuilder message = new StringBuilder();

            message.append("An exception occurred while retrieving the ").append("UserDarft Object.").append("\nUser Id: ").append(aUserId).append("\nSystem Id: ").append(aSystemId).append(
                "\nDraft Id: ").append(aDraftId);

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
        //System.out.println("Loaded User Draft: " + ud.getDraft());
        return ud;
    }

    
    /**
     * This method returns the UserDraft object corresponding to
     * the given userId, sysId and request Id and Timestamp
     *
     * @param aUserId
     * @param aSystemId
     * @param aRequestId
     * @param aTimestamp
     *
     * @return   UserDraft Objects
     *
     * @exception DatabaseException incase of any error while interacting with
     *            the database.
     */
    public static UserDraft lookupByUserIdAndSystemIdAndRequestIdAndTimestamp(int aUserId, int aSystemId, int aRequestId, Timestamp aTimestamp) throws DatabaseException, Exception {
        UserDraft  ud         = null;
        Connection connection = null;

        try {
            connection = DataSourcePool.getConnection();

            CallableStatement cs = connection.prepareCall("stp_user_draft_lookupByUserIdAndSystemIdAndRequestId" + "AndTimestamp ?, ? ,?, ?");

            cs.setInt(1, aUserId);
            cs.setInt(2, aSystemId);
            cs.setInt(3, aRequestId);
            cs.setTimestamp(4, aTimestamp.toSqlTimestamp());

            ResultSet rs = cs.executeQuery();

            if (rs != null) {
                if (rs.next() != false) {
                    ud = createFromResultSet(rs);
                }

                // Close the result set.
                rs.close();
            }

            // Close the statement.
            cs.close();

            //
            // Release the memory by nullifying the references so that these
            // are recovered by the Garbage collector.
            //
            rs = null;
            cs = null;
        } catch (SQLException sqle) {
            StringBuilder message = new StringBuilder();

            message.append("An exception occurred while retrieving the ").append("UserDarft Object.").append("\nUser Id: ").append(aUserId).append("\nSystem Id: ").append(aSystemId).append(
                "\nRequest Id: ").append(aRequestId).append("\nTimestamp: ").append(aTimestamp).append("\n");

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
        //System.out.println("Loaded User Draft: " + ud.getDraft());
        return ud;
    }

    /**
     * main method.
     */
    public static void main(String arg[]) throws Exception {
    	for(UserDraft ud: UserDraft.lookupByUserId(1))
    	{
    		System.out.println("xml:" + ud.getDraft());
    		System.out.println( DraftConfig.xmlDeSerialize(ud.getDraft()));
    	}
//        Hashtable<String, String> test = new Hashtable<String, String>();
//
//        test.put("description", "<<fhgfdgsdf>>djhjh");
//        test.put("subject", "subject.....");
//        test.put("due_datetime", "12/12/05 23:45");
//
//        UserDraft ud = new UserDraft(1083, new Timestamp(), 1, 0, DraftConfig.xmlSerialize(1, test));
//
//        UserDraft.insert(ud);
//
//        ArrayList<UserDraft> drafts = UserDraft.lookupByUserIdAndSystemIdAndRequestId(1083, 1, 0);
//
//        for (UserDraft ud1 : drafts) {
//            System.out.println(ud1.getDraft());
//        }
//
//        UserDraft.delete(ud);
//        System.out.println("********After Deleting****");
//        drafts = UserDraft.lookupByUserIdAndSystemIdAndRequestId(1083, 1, 0);
//
//        for (UserDraft ud1 : drafts) {
//            System.out.println(ud1.getDraft());
//        }
//
//        drafts = UserDraft.lookupByUserId(1083);
//        LOG.info(drafts.size());
//        //transbit.tbits.api.Mapper.stop();
    }

    /**
     * Method to update the corresponding UserDraft object
     * in the database.
     *
     * @param aObject Object to be updated
     *
     * @return Update domain object.
     *
     */
    public static UserDraft update(UserDraft aObject) throws Exception {

        // Update logic here.
        if (aObject == null) {
            return aObject;
        }

        Connection aCon = null;

        try {
            aCon = DataSourcePool.getConnection();
            aCon.setAutoCommit(false);

            CallableStatement cs = aCon.prepareCall("stp_user_draft_update ?, ?, ?, ?,?, ?");
            
            aObject.setCallableParameters(cs);
            cs.setInt(6, aObject.getDraftId());
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
            LOG.severe("",(sqle));
        } finally {
            if (aCon != null) {
                try {
                    aCon.close();
                } catch (SQLException sqle) {
                    LOG.severe("",(sqle));
                }
            }
        }

        return aObject;
    }

    public int getDraftId()
    {
    	return myDraftId;
    }
    //~--- get methods --------------------------------------------------------

    /**
     * Accessor method for Draft property.
     *
     * @return Current Value of Draft
     *
     */
    public String getDraft() {
        return myDraft;
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
     * Accessor method for SystemId property.
     *
     * @return Current Value of SystemId
     *
     */
    public int getSystemId() {
        return mySystemId;
    }

    /**
     * Accessor method for Timestamp property.
     *
     * @return Current Value of Timestamp
     *
     */
    public Timestamp getTimestamp() {
        return myTimestamp;
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

    //~--- set methods --------------------------------------------------------

    /**
     * This method sets the parameters in the CallableStatement.
     *
     * @param aCS     CallableStatement whose params should be set.
     *
     * @exception SQLException
     */
    public void setCallableParameters(CallableStatement aCS) throws SQLException, Exception {
        aCS.setInt(USERID, myUserId);
        aCS.setTimestamp(TIMESTAMP, myTimestamp.toSqlTimestamp());
        aCS.setInt(SYSTEMID, mySystemId);
        aCS.setInt(REQUESTID, myRequestId);
        aCS.setString(DRAFT, myDraft);
    }

    /**
     * Mutator method for Draft property.
     *
     * @param aDraft New Value for Draft
     *
     */
    public void setDraft(String aDraft) {
        myDraft = aDraft;
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
     * Mutator method for SystemId property.
     *
     * @param aSystemId New Value for SystemId
     *
     */
    public void setSystemId(int aSystemId) {
        mySystemId = aSystemId;
    }

    /**
     * Mutator method for Timestamp property.
     *
     * @param aTimestamp New Value for Timestamp
     *
     */
    public void setTimestamp(Timestamp aTimestamp) {
        myTimestamp = aTimestamp;
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
}
