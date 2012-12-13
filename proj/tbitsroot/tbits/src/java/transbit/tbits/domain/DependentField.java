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
 * DependentField.java
 *
 * $Header:
 *
 */
package transbit.tbits.domain;

//~--- non-JDK imports --------------------------------------------------------

//Static Imports.
import static transbit.tbits.api.Mapper.ourDependentFieldMap;

//~--- JDK imports ------------------------------------------------------------

//JavaImports.
import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;

import java.util.ArrayList;
import java.util.Hashtable;

//~--- classes ----------------------------------------------------------------

/**
 * TODO: Appropriate Javadoc needs to be written here.
 *
 * @author  :
 * @version : $Id: $
 *
 */
public class DependentField implements Serializable{
    private DepRole myDepRole;
    private int     myDependencyId;
    private int     myFieldId;

    // Attributes of this Domain Object.
    private int mySystemId;

    //~--- constant enums -----------------------------------------------------

    public static enum DepRole { PRIMARY, DEPENDENT }

    //~--- constructors -------------------------------------------------------

    /**
     * The default constructor.
     */
    public DependentField() {}

    /**
     * The complete constructor.
     *
     *  @param aSystemId
     *  @param aDependencyId
     *  @param aFieldId
     */
    public DependentField(int aSystemId, int aDependencyId, int aFieldId, DepRole aDepRole) {
        mySystemId     = aSystemId;
        myDependencyId = aDependencyId;
        myFieldId      = aFieldId;
        myDepRole      = aDepRole;
    }

    //~--- methods ------------------------------------------------------------

    /**
     * This method creates an object of this class from the current row of the
     * result set passed.
     *
     * @param rs Resultset object.
     *
     * @return DependentField object.
     *
     * @throws SQLException
     */
    public static DependentField createFromResultSet(ResultSet rs) throws SQLException {
        DependentField df = new DependentField(rs.getInt("sys_id"), rs.getInt("dep_id"), rs.getInt("field_id"), DepRole.valueOf(rs.getString("dep_role").toUpperCase()));

        return df;
    }

    //~--- get methods --------------------------------------------------------

    /**
     * This method returns the dependent fields in a given BA .
     *
     * @param systemId  Business Area ID.
     *
     * @return          List of fields in the specified role.
     */
    public static Hashtable<Integer, DependentField> getBySystemId(int systemId) {
        Hashtable<Integer, DependentField> table = new Hashtable<Integer, DependentField>();

        if (ourDependentFieldMap != null) {
            String                    key  = Integer.toString(systemId);
            ArrayList<DependentField> list = ourDependentFieldMap.get(key);

            if (list != null) {
                for (DependentField df : list) {
                    table.put(df.getFieldId(), df);
                }
            }
        }

        return table;
    }

    /**
     * This method returns the dependent fields in a given BA with a specified
     * role.
     *
     * @param systemId  Business Area ID.
     * @param role      Role of the field in the dependency.
     * @return          List of fields in the specified role.
     */
    public static Hashtable<Integer, DependentField> getBySystemIdAndDepRole(int systemId, DepRole role) {
        Hashtable<Integer, DependentField> table = new Hashtable<Integer, DependentField>();

        if (ourDependentFieldMap != null) {
            String                    key  = systemId + "-" + role;
            ArrayList<DependentField> list = ourDependentFieldMap.get(key);

            if (list != null) {
                for (DependentField df : list) {
                    table.put(df.getFieldId(), df);
                }
            }
        }

        return table;
    }

    /**
     * This method returns the dependent fields in a given BA .
     *
     * @param systemId      Business Area ID.
     * @param dependencyId  Dependency ID.
     *
     * @return          Table of fields in the specified role.
     */
    public static Hashtable<Integer, DependentField> getBySystemIdAndDependencyId(int systemId, int dependencyId) {
        Hashtable<Integer, DependentField> table = new Hashtable<Integer, DependentField>();

        if (ourDependentFieldMap != null) {
            String                    key  = systemId + "-" + dependencyId;
            ArrayList<DependentField> list = ourDependentFieldMap.get(key);

            if (list != null) {
                for (DependentField df : list) {
                    table.put(df.getFieldId(), df);
                }
            }
        }

        return table;
    }

    /**
     * Accessor method for DepRole property.
     *
     * @return Current Value of DepRole
     *
     */
    public DepRole getDepRole() {
        return myDepRole;
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
     * Accessor method for FieldId property.
     *
     * @return Current Value of FieldId
     *
     */
    public int getFieldId() {
        return myFieldId;
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
     * Mutator method for DepRole property.
     *
     * @param aDepRole New Value for DepRole
     *
     */
    public void setDepRole(DepRole aDepRole) {
        myDepRole = aDepRole;
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
     * Mutator method for FieldId property.
     *
     * @param aFieldId New Value for FieldId
     *
     */
    public void setFieldId(int aFieldId) {
        myFieldId = aFieldId;
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
