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
 * DActionLog.java
 *
 * $Header:
 *
 */
package transbit.tbits.domain;

//~--- non-JDK imports --------------------------------------------------------

//Other TBits Imports.
import transbit.tbits.common.DataSourcePool;
import transbit.tbits.common.TBitsLogger;

//Static Imports.
import static transbit.tbits.Helper.TBitsConstants.PKG_DOMAIN;

//~--- JDK imports ------------------------------------------------------------

//Java Imports.
import java.io.Serializable;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

//~--- classes ----------------------------------------------------------------

//Third party imports.

/**
 * This class is the domain object corresponding to the daction_log table
 * in the database.
 *
 * @author  :
 * @version : $Id: $
 *
 */
public class DActionLog implements Serializable {
    public static final TBitsLogger LOG = TBitsLogger.getLogger(PKG_DOMAIN);

    // Enum sort of fields for Attributes.
    private static final int SYSTEMID    = 1;
    private static final int REQUESTID   = 2;
    private static final int DACTION_LOG = 4;
    private static final int ACTIONID    = 3;

    //~--- fields -------------------------------------------------------------

    private int    myActionId;
    private String myDActionLog;
    private int    myRequestId;

    // Attributes of this Domain Object.
    private int mySystemId;

    //~--- constructors -------------------------------------------------------

    /**
     * The default constructor.
     */
    public DActionLog() {}

    /**
     * The complete constructor.
     *
     *  @param aSystemId
     *  @param aRequestId
     *  @param aActionId
     *  @param aDActionLog
     */
    public DActionLog(int aSystemId, int aRequestId, int aActionId, String aDActionLog) {
        mySystemId   = aSystemId;
        myRequestId  = aRequestId;
        myActionId   = aActionId;
        myDActionLog = aDActionLog;
    }

    //~--- methods ------------------------------------------------------------

    /**
     * This method constructs a DActionLog object from the resultset.
     *
     * @param aRS     ResultSet which points to a DActionLog record.
     *
     * @return DActionLog object
     *
     * @exception SQLException
     */
    public static DActionLog createFromResultSet(ResultSet aRS) throws SQLException {
        DActionLog dal = new DActionLog(aRS.getInt("sys_id"), aRS.getInt("request_id"), aRS.getInt("action_id"), aRS.getString("daction_log"));

        return dal;
    }

    /**
     * Method to insert a DActionLog object into database.
     *
     * @param aObject Object to be inserted
     *
     */
    public static boolean insert(DActionLog aObject) {

        // Insert logic here.
        if (aObject == null) {
            return false;
        }

        Connection aCon        = null;
        boolean    returnValue = false;

        try {
            aCon = DataSourcePool.getConnection();
            aCon.setAutoCommit(false);

            CallableStatement cs = aCon.prepareCall("stp_daction_insert ?, ?, ?, ?");

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
            LOG.severe(sqle);
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
     * Accessor method for DActionLog property.
     *
     * @return Current Value of DActionLog
     *
     */
    public String getDActionLog() {
        return myDActionLog;
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
        aCS.setString(DACTION_LOG, myDActionLog);
    }

    /**
     * Mutator method for dActionLog property.
     *
     * @param aDActionLog New Value for DActionLog
     *
     */
    public void setDActionLog(String aDActionLog) {
        myDActionLog = aDActionLog;
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
}
