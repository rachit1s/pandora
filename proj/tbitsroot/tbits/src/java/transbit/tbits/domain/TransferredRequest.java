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
 * TransferredRequest.java
 *
 * $Header:
 *
 */
package transbit.tbits.domain;

//~--- non-JDK imports --------------------------------------------------------

import transbit.tbits.common.DataSourcePool;
import transbit.tbits.common.DatabaseException;

//Other TBits Imports.
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
 * This class is the domain object corresponding to the transferred_requests
 * table in the database.
 *
 * @author  : Vaibhav
 * @version : $Id: $
 *
 */
public class TransferredRequest implements Serializable {
    public static final TBitsLogger LOG = TBitsLogger.getLogger(PKG_DOMAIN);

    // Enum sort of fields for Attributes.
    private static final int SOURCEPREFIX    = 1;
    private static final int SOURCEREQUESTID = 2;
    private static final int TARGETPREFIX    = 3;
    private static final int TARGETREQUESTID = 4;

    //~--- fields -------------------------------------------------------------

    // Attributes of this Domain Object.
    private String mySourcePrefix;
    private int    mySourceRequestId;
    private String myTargetPrefix;
    private int    myTargetRequestId;

    //~--- constructors -------------------------------------------------------

    /**
     * The default constructor.
     */
    public TransferredRequest() {}

    /**
     * The complete constructor.
     *
     *  @param aSourcePrefix
     *  @param aSourceRequestId
     *  @param aTargetPrefix
     *  @param aTargetRequestId
     */
    public TransferredRequest(String aSourcePrefix, int aSourceRequestId, String aTargetPrefix, int aTargetRequestId) {
        mySourcePrefix    = aSourcePrefix;
        mySourceRequestId = aSourceRequestId;
        myTargetPrefix    = aTargetPrefix;
        myTargetRequestId = aTargetRequestId;
    }

    //~--- methods ------------------------------------------------------------

    /**
     * This method is used to create the TransferredRequest object from the
     * ResultSet
     *
     * @param  aResultSet the result object containing the fields corresponding
     *                    to a row of transferred_requests in the database
     * @return TransferredRequest object.
     */
    public static TransferredRequest createFromResultSet(ResultSet aResultSet) throws SQLException {
        TransferredRequest tr = new TransferredRequest(aResultSet.getString("source_prefix"), aResultSet.getInt("source_request_id"), aResultSet.getString("target_prefix"),
                                    aResultSet.getInt("target_request_id"));

        return tr;
    }

    /**
     * Method to insert a TransferredRequest object into database.
     *
     * @param aObject Object to be inserted
     *
     */
    public static boolean insert(TransferredRequest aObject) throws DatabaseException {

        // Insert logic here.
        if (aObject == null) {
            return false;
        }

        Connection aCon        = null;
        boolean    returnValue = false;

        try {
            aCon = DataSourcePool.getConnection();
            aCon.setAutoCommit(false);

            CallableStatement cs = aCon.prepareCall("stp_tr_insert ?,?,?,?");

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
            throw new DatabaseException(sqle.toString(), sqle);
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
     * Method to get the transferred request object for given source prefix
     * and request id
     *
     * @param aPrefix    Source Prefix.
     * @param aRequestId Request Id.
     *
     * @return TransferredRequest Object
     *
     */
    public static TransferredRequest lookupBySourcePrefixAndRequestId(String aPrefix, int aRequestId) throws DatabaseException {
        TransferredRequest tr   = null;
        Connection         aCon = null;

        try {
            aCon = DataSourcePool.getConnection();

            CallableStatement cs = aCon.prepareCall("stp_tr_lookupBySourcePrefixAndRequestId ?, ?");

            cs.setString(1, aPrefix);
            cs.setInt(2, aRequestId);

            ResultSet rs = cs.executeQuery();

            if (rs != null) {
                if (rs.next() != false) {
                    tr = createFromResultSet(rs);
                }

                rs.close();
            }

            cs.close();
            rs = null;
            cs = null;
        } catch (SQLException sqle) {
            throw new DatabaseException(sqle.toString(), sqle);
        } finally {
            if (aCon != null) {
                try {
                    aCon.close();
                } catch (SQLException sqle) {

                    // Should this be logged.?
                }
            }
        }

        return tr;
    }

    /**
     * Method to update the corresponding TransferredRequest object in the
     * database.
     *
     * @param aObject Object to be updated
     *
     * @return Update domain object.
     *
     */
    public static TransferredRequest update(TransferredRequest aObject) throws DatabaseException {

        // Update logic here.
        if (aObject == null) {
            return aObject;
        }

        Connection aCon = null;

        try {
            aCon = DataSourcePool.getConnection();
            aCon.setAutoCommit(false);

            CallableStatement cs = aCon.prepareCall("stp_tr_update ?,?,?,?");

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
            throw new DatabaseException(sqle.toString(), sqle);
        } finally {
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

    //~--- get methods --------------------------------------------------------

    /**
     * Accessor method for SourcePrefix property.
     *
     * @return Current Value of SourcePrefix
     *
     */
    public String getSourcePrefix() {
        return mySourcePrefix;
    }

    /**
     * Accessor method for SourceRequestId property.
     *
     * @return Current Value of SourceRequestId
     *
     */
    public int getSourceRequestId() {
        return mySourceRequestId;
    }

    /**
     * Accessor method for TargetPrefix property.
     *
     * @return Current Value of TargetPrefix
     *
     */
    public String getTargetPrefix() {
        return myTargetPrefix;
    }

    /**
     * Accessor method for TargetRequestId property.
     *
     * @return Current Value of TargetRequestId
     *
     */
    public int getTargetRequestId() {
        return myTargetRequestId;
    }

    //~--- set methods --------------------------------------------------------

    /**
     * This method sets the parameters in the CallableStatement.
     *
     * @param aCS     CallableStatement whose params should be set.
     *
     * @exception SQLException
     */
    public void setCallableParameters(CallableStatement aCS) throws SQLException {
        aCS.setString(SOURCEPREFIX, mySourcePrefix);
        aCS.setInt(SOURCEREQUESTID, mySourceRequestId);
        aCS.setString(TARGETPREFIX, myTargetPrefix);
        aCS.setInt(TARGETREQUESTID, myTargetRequestId);
    }

    /**
     * Mutator method for SourcePrefix property.
     *
     * @param aSourcePrefix New Value for SourcePrefix
     *
     */
    public void setSourcePrefix(String aSourcePrefix) {
        mySourcePrefix = aSourcePrefix;
    }

    /**
     * Mutator method for SourceRequestId property.
     *
     * @param aSourceRequestId New Value for SourceRequestId
     *
     */
    public void setSourceRequestId(int aSourceRequestId) {
        mySourceRequestId = aSourceRequestId;
    }

    /**
     * Mutator method for TargetPrefix property.
     *
     * @param aTargetPrefix New Value for TargetPrefix
     *
     */
    public void setTargetPrefix(String aTargetPrefix) {
        myTargetPrefix = aTargetPrefix;
    }

    /**
     * Mutator method for TargetRequestId property.
     *
     * @param aTargetRequestId New Value for TargetRequestId
     *
     */
    public void setTargetRequestId(int aTargetRequestId) {
        myTargetRequestId = aTargetRequestId;
    }
}
