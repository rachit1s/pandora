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
 * Dependency.java
 *
 * $Header:
 *
 */
package transbit.tbits.domain;

//~--- non-JDK imports --------------------------------------------------------

//TBits Imports
import transbit.tbits.common.DataSourcePool;
import transbit.tbits.common.DatabaseException;
import transbit.tbits.common.TBitsLogger;
import transbit.tbits.config.DependencyConfig;

//Static imports.
import static transbit.tbits.Helper.TBitsConstants.PKG_DOMAIN;
import static transbit.tbits.api.Mapper.ourDependencyMap;

//~--- JDK imports ------------------------------------------------------------

//Java Imports
import java.io.Serializable;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;

//~--- classes ----------------------------------------------------------------

/**
 * TODO: Appropriate Javadoc needs to be written here.
 *
 * @author  : Vaibhav
 * @version : $Id: $
 *
 */
public class Dependency implements Serializable{
    public static final TBitsLogger LOG = TBitsLogger.getLogger(PKG_DOMAIN);

    // Enum sort of fields for Attributes.
    private static final int SYSTEMID       = 1;
    private static final int LEVEL          = 4;
    private static final int DEPENDENCYNAME = 3;
    private static final int DEPENDENCYID   = 2;
    private static final int TYPE           = 5;
    private static final int DEPCONFIG      = 6;

    // Static attributes related to sorting.
    private static int ourSortField;
    private static int ourSortOrder;

    //~--- fields -------------------------------------------------------------

    private String           myDepConfig;
    private DependencyConfig myDepConfigObject;
    private int              myDependencyId;
    private String           myDependencyName;
    private DepLevel         myLevel;

    // Attributes of this Domain Object.
    private int     mySystemId;
    private DepType myType;

    //~--- constant enums -----------------------------------------------------

    // List of dependency levels.
    public enum DepLevel { FIELD_DEPENDENCY, APP_DEPENDENCY; }

    ;

    //~--- constant enums -----------------------------------------------------

    public enum DepType { GENERATE, VALIDATE, SYNCHRONIZE; }

    ;

    //~--- constructors -------------------------------------------------------

    /**
     * The default constructor.
     */
    public Dependency() {}

    /**
     * The complete constructor.
     *
     *  @param aSystemId
     *  @param aDependencyId
     *  @param aDependencyName
     *  @param aLevel
     *  @param aType
     *  @param aDepConfig
     */
    public Dependency(int aSystemId, int aDependencyId, String aDependencyName, DepLevel aLevel, DepType aType, String aDepConfig) {
        mySystemId       = aSystemId;
        myDependencyId   = aDependencyId;
        myDependencyName = aDependencyName;
        myLevel          = aLevel;
        myType           = aType;
        myDepConfig      = aDepConfig;

        try {
            myDepConfigObject = DependencyConfig.parse(myLevel, myType, myDepConfig);
        } catch (Exception e) {
            LOG.warn("An exception has occurred while parsing the " + "dependency configuration: " + "",(e));
        }
    }

    //~--- methods ------------------------------------------------------------

    /**
     * Method that compares this object with the one passed W.R.T the ourSortField.
     *
     * @param aObject  Object to be compared.
     *
     * @return 0 - If they are equal.
     *         1 - If this is greater.
     *        -1 - If this is smaller.
     *
     */
    public int compareTo(Dependency aObject) {
        switch (ourSortField) {
        case SYSTEMID : {
            Integer i1 = mySystemId;
            Integer i2 = aObject.mySystemId;

            return i1.compareTo(i2);
        }

        case DEPENDENCYID : {
            Integer i1 = myDependencyId;
            Integer i2 = aObject.myDependencyId;

            return i1.compareTo(i2);
        }

        case DEPENDENCYNAME : {
            return myDependencyName.compareTo(aObject.myDependencyName);
        }

        case DEPCONFIG : {
            return myDepConfig.compareTo(aObject.myDepConfig);
        }
        }

        return 0;
    }

    /**
     * This method is used to create the Dependency object from ResultSet
     *
     * @param  aRS the result object containing the fields corresponding to a
     *             row of the dependencies table in the database
     *
     * @return the corresponding Dependency object
     */
    public static Dependency createFromResultSet(ResultSet aRS) throws SQLException {
        Dependency d = new Dependency(aRS.getInt("sys_id"), aRS.getInt("dep_id"), aRS.getString("dep_name"), DepLevel.valueOf(aRS.getString("dep_level")), DepType.valueOf(aRS.getString("dep_type")),
                                      aRS.getString("dep_config"));

        return d;
    }

    /**
     * This method returns the dependency for specified system id and with given
     * dep id.
     *
     * @param sysId
     * @param depId
     *
     * @return Dependency object.
     */
    public static Dependency lookupBySystemIdAndDependencyId(int sysId, int depId) {
        Dependency dep = null;

        if (ourDependencyMap != null) {
            String                key  = sysId + "-" + depId;
            ArrayList<Dependency> list = ourDependencyMap.get(key);

            if (list.size() != 0) {
                dep = list.get(0);
            }
        }

        return dep;
    }

    /**
     *
     * @param arg
     * @throws Exception
     */
    public static void main(String arg[]) throws Exception {
        LOG.info(getDependenciesByLevelAndType(DepLevel.APP_DEPENDENCY, DepType.GENERATE));
    }

    /**
     * Method to return the source arraylist in the sorted order
     *
     * @param  source the array list of Type objects
     * @return the ArrayList of the Dependency objects in sorted order
     */
    public static ArrayList<Dependency> sort(ArrayList<Dependency> source) {
        int          size     = source.size();
        Dependency[] srcArray = new Dependency[size];

        for (int i = 0; i < size; i++) {
            srcArray[i] = source.get(i);
        }

        Arrays.sort(srcArray, new DependencyComparator());

        ArrayList<Dependency> target = new ArrayList<Dependency>();

        for (int i = 0; i < size; i++) {
            target.add(srcArray[i]);
        }

        return target;
    }

    /**
     *
     */
    public String toString() {
        StringBuffer buffer = new StringBuffer();

        buffer.append("[ ").append(mySystemId).append(", ").append(myDependencyId).append(", ").append(myDependencyName).append(", ").append(myLevel).append(", ").append(myType).append(" ]");

        return buffer.toString();
    }

    //~--- get methods --------------------------------------------------------

    /**
     * This method returns the list of APP-Sync dependencies.
     *
     * @return List of app-sync dependencies.
     * @throws DatabaseException
     */
    public static ArrayList<Dependency> getAppSyncDependencies() throws DatabaseException {
        ArrayList<Dependency> list = new ArrayList<Dependency>();
        Connection            con  = null;

        try {
            con = DataSourcePool.getConnection();

            CallableStatement cs = con.prepareCall("stp_dependencies_getAppSyncDependencies");
            ResultSet         rs = cs.executeQuery();

            if (rs != null) {
                while (rs.next() != false) {
                    Dependency obj = Dependency.createFromResultSet(rs);

                    list.add(obj);
                }

                rs.close();
                rs = null;
            }

            cs.close();
            cs = null;
        } catch (SQLException sqle) {
            StringBuffer buffer = new StringBuffer();

            buffer.append("An exception has occurred while retrieving the ").append("APP-SYNC dependencies from the database.");

            throw new DatabaseException(buffer.toString(), sqle);
        } finally {
            try {
            	if(con != null )
                con.close();
            } catch (SQLException sqle) {
                LOG.debug("Exception while closing the connection: " + "",(sqle));
            }
        }

        return list;
    }

    /**
     * Accessor method for DepConfig property.
     *
     * @return Current Value of DepConfig
     *
     */
    public String getDepConfig() {
        return myDepConfig;
    }

    /**
     * Accessor method for DepConfigObject property.
     *
     * @return Current Value of DepConfigObject
     *
     */
    public DependencyConfig getDepConfigObject() {
        return myDepConfigObject;
    }

    /**
     * This method returns the dependencies of given level and type.
     *
     * @param aLevel Dependency Level
     * @param aType  Dependency Type.
     *
     * @return List of dependencies of given level and type.
     */
    public static ArrayList<Dependency> getDependenciesByLevelAndType(DepLevel aLevel, DepType aType) {
        ArrayList<Dependency> list = new ArrayList<Dependency>();

        if (ourDependencyMap != null) {
            String key = aLevel.toString() + "-" + aType.toString();

            list = ourDependencyMap.get(key);
        }

        return list;
    }

    /**
     * This method returns the dependencies of given System Id.
     *
     * @param aSystemId BA ID.
     *
     * @return List of dependencies in the given BA.
     */
    public static ArrayList<Dependency> getDependenciesBySystemId(int aSystemId) {
        ArrayList<Dependency> list = new ArrayList<Dependency>();

        if (ourDependencyMap != null) {
            String key = Integer.toString(aSystemId);

            list = ourDependencyMap.get(key);
        }

        return list;
    }

    /**
     * Accessor method for DependencyId property.
     *
     * @return Current Value of DependencyId
     *
     */
    public int getDependencyId() {
        return myDependencyId;
    }

    /**
     * Accessor method for DependencyName property.
     *
     * @return Current Value of DependencyName
     *
     */
    public String getDependencyName() {
        return myDependencyName;
    }

    /**
     * Accessor method for Level property.
     *
     * @return Current Value of Level
     *
     */
    public DepLevel getLevel() {
        return myLevel;
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
     * Accessor method for Type property.
     *
     * @return Current Value of Type
     *
     */
    public DepType getType() {
        return myType;
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
        aCS.setInt(SYSTEMID, mySystemId);
        aCS.setInt(DEPENDENCYID, myDependencyId);
        aCS.setString(DEPENDENCYNAME, myDependencyName);
        aCS.setString(LEVEL, myLevel.toString());
        aCS.setString(TYPE, myType.toString());
        aCS.setString(DEPCONFIG, myDepConfig);
    }

    /**
     * Mutator method for DepConfig property.
     *
     * @param aDepConfig New Value for DepConfig
     *
     */
    public void setDepConfig(String aDepConfig) {
        myDepConfig = aDepConfig;
    }

    /**
     * Mutator method for DepConfigObject property.
     *
     * @param aDepConfigObject New Value for DepConfigObject
     *
     */
    public void setDepConfigObject(DependencyConfig aDepConfigObject) {
        myDepConfigObject = aDepConfigObject;
    }

    /**
     * Mutator method for DependencyId property.
     *
     * @param aDependencyId New Value for DependencyId
     *
     */
    public void setDependencyId(int aDependencyId) {
        myDependencyId = aDependencyId;
    }

    /**
     * Mutator method for DependencyName property.
     *
     * @param aDependencyName New Value for DependencyName
     *
     */
    public void setDependencyName(String aDependencyName) {
        myDependencyName = aDependencyName;
    }

    /**
     * Mutator method for Level property.
     *
     * @param aLevel New Value for Level
     *
     */
    public void setLevel(DepLevel aLevel) {
        myLevel = aLevel;
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
     * Mutator method for Type property.
     *
     * @param aType New Value for Type
     *
     */
    public void setType(DepType aType) {
        myType = aType;
    }
}


/**
 * This class is the comparator for domain object corresponding to the
 * dependencies table in the database.
 *
 * @author  : Vaibhav
 * @version : $Id: $
 *
 */
class DependencyComparator implements Comparator<Dependency>, Serializable {
    public int compare(Dependency obj1, Dependency obj2) {
        return obj1.compareTo(obj2);
    }
}
