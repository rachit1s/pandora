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
 * ExternalResource.java
 *
 * $Header:
 *
 */
package transbit.tbits.domain;

//~--- non-JDK imports --------------------------------------------------------

import transbit.tbits.common.DataSourcePool;
import transbit.tbits.common.DatabaseException;
import transbit.tbits.common.TBitsLogger;
import transbit.tbits.exception.TBitsException;
import transbit.tbits.external.Resource;
import transbit.tbits.external.ResourceConfig;

//Static imports.
import static transbit.tbits.Helper.TBitsConstants.PKG_DOMAIN;
import static transbit.tbits.api.Mapper.ourExternalResourceMap;

//~--- JDK imports ------------------------------------------------------------

//Java imports.
import java.io.Serializable;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

//~--- classes ----------------------------------------------------------------

/**
 * TODO: Appropriate Javadoc needs to be written here.
 *
 * @author  :
 * @version : $Id: $
 *
 */
public class ExternalResource implements Serializable {
    public static final TBitsLogger LOG          = TBitsLogger.getLogger(PKG_DOMAIN);
    public static final int         RESOURCEID   = 1;
    public static final int         RESOURCENAME = 2;
    public static final int         RESOURCEDEF  = 3;

    //~--- fields -------------------------------------------------------------

    private Resource myResource;
    private String   myResourceDef;

    // Attributes of this Domain Object.
    private int    myResourceId;
    private String myResourceName;

    //~--- constructors -------------------------------------------------------

    /**
     * The default constructor.
     */
    public ExternalResource() {}

    /**
     * The complete constructor.
     *
     *  @param aResourceId
     *  @param aResourceName
     *  @param aResourceDef
     */
    public ExternalResource(int aResourceId, String aResourceName, String aResourceDef) {
        myResourceId   = aResourceId;
        myResourceName = aResourceName;
        myResourceDef  = aResourceDef;

        try {
            myResource = ResourceConfig.parseResourceConfig(myResourceDef);
            myResource.setResourceId(myResourceId);
            myResource.setResourceName(myResourceName);
        } catch (TBitsException de) {
            LOG.warn("Exception while parsing the configuration of a " + "resource.");
        }
    }

    //~--- methods ------------------------------------------------------------

    /**
     * This method creates an object from the data in the current row of the
     * resultset.
     *
     * @param aRS Result set object
     * @return ExternalResource object.
     * @throws SQLException
     */
    public static ExternalResource createFromResultSet(ResultSet aRS) throws SQLException {
        ExternalResource er = new ExternalResource(aRS.getInt("resource_id"), aRS.getString("resource_name"), aRS.getString("resource_def"));

        return er;
    }

    /*
     *
     *         Look up methods for this domain object.
     *
     */

    /**
     * This method returns the ExternalResource object corresponding to the
     * given ID.
     *
     * @param aId   Id of the resource.
     * @return ExternalResource object.
     * @throws DatabaseException incase of any database related errors.
     */
    public static ExternalResource lookupById(int aId) throws DatabaseException {
        ExternalResource er  = null;
        String           key = Integer.toString(aId);

        if (ourExternalResourceMap != null) {
            er = ourExternalResourceMap.get(key);

            if (er == null) {
                LOG.debug("No External resource found with Id:" + aId);
            }
        } else {
            Connection con = null;

            try {
                con = DataSourcePool.getConnection();

                CallableStatement cs = con.prepareCall("stp_er_lookupById ?");

                cs.setInt(1, aId);

                ResultSet rs = cs.executeQuery();

                if (rs != null) {
                    while (rs.next() == true) {
                        er = createFromResultSet(rs);
                    }

                    rs.close();
                    rs = null;
                }

                cs.close();
                cs = null;
            } catch (SQLException sqle) {
                StringBuffer message = new StringBuffer();

                message.append("An exception has occurred while retrieving ").append("the resource corresponding to ID: ").append(aId);

                throw new DatabaseException(message.toString(), sqle);
            } finally {
                if (con != null) {
                    try {
                        con.close();
                    } catch (SQLException sqle) {
                        LOG.info("Exception while closing the connection" + sqle.toString());
                    }
                }
            }
        }

        return er;
    }

    /**
     * This method returns the ExternalResource object corresponding to the
     * given Name.
     *
     * @param aName   Name of the resource.
     * @return ExternalResource object.
     * @throws DatabaseException incase of any database related errors.
     */
    public static ExternalResource lookupByName(String aName) throws DatabaseException {
        ExternalResource er  = null;
        String           key = aName.toUpperCase();

        if (ourExternalResourceMap != null) {
            er = ourExternalResourceMap.get(key);

            if (er == null) {
                LOG.debug("No External resource found with name:" + aName);
            }
        } else {
            Connection con = null;

            try {
                con = DataSourcePool.getConnection();

                CallableStatement cs = con.prepareCall("stp_er_lookupByName ?");

                cs.setString(1, aName);

                ResultSet rs = cs.executeQuery();

                if (rs != null) {
                    while (rs.next() == true) {
                        er = createFromResultSet(rs);
                    }

                    rs.close();
                    rs = null;
                }

                cs.close();
                cs = null;
            } catch (SQLException sqle) {
                StringBuffer message = new StringBuffer();

                message.append("An exception has occurred while retrieving ").append("the resource corresponding to name: ").append(aName);

                throw new DatabaseException(message.toString(), sqle);
            } finally {
                if (con != null) {
                    try {
                        con.close();
                    } catch (SQLException sqle) {
                        LOG.info("Exception while closing the connection" + sqle.toString());
                    }
                }
            }
        }

        return er;
    }

    //~--- get methods --------------------------------------------------------

    /**
     * Accessor method for Resource property.
     *
     * @return Current Value of Resource
     *
     */
    public Resource getResource() {
        return myResource;
    }

    /**
     * Accessor method for ResourceDef property.
     *
     * @return Current Value of ResourceDef
     *
     */
    public String getResourceDef() {
        return myResourceDef;
    }

    /**
     * Accessor method for ResourceId property.
     *
     * @return Current Value of ResourceId
     *
     */
    public int getResourceId() {
        return myResourceId;
    }

    /**
     * Accessor method for ResourceName property.
     *
     * @return Current Value of ResourceName
     *
     */
    public String getResourceName() {
        return myResourceName;
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
        aCS.setInt(RESOURCEID, myResourceId);
        aCS.setString(RESOURCENAME, myResourceName);
        aCS.setString(RESOURCEDEF, myResourceDef);
    }

    /**
     * Mutator method for ResourceDef property.
     *
     * @param aResourceDef New Value for ResourceDef
     *
     */
    public void setResourceDef(String aResourceDef) {
        myResourceDef = aResourceDef;
    }

    /**
     * Mutator method for ResourceId property.
     *
     * @param aResourceId New Value for ResourceId
     *
     */
    public void setResourceId(int aResourceId) {
        myResourceId = aResourceId;
    }

    /**
     * Mutator method for ResourceName property.
     *
     * @param aResourceName New Value for ResourceName
     *
     */
    public void setResourceName(String aResourceName) {
        myResourceName = aResourceName;
    }
}
