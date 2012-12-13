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
 * BAConfig.java
 *
 * $Header:
 *
 */
package transbit.tbits.config;

//~--- non-JDK imports --------------------------------------------------------

import transbit.tbits.Helper.TBitsConstants;

//Imports from other packages in TBits.
import transbit.tbits.common.TBitsLogger;
import transbit.tbits.domain.Field;

//Static Imports.
import static transbit.tbits.search.Searcher.ourDefaultHeader;

//~--- JDK imports ------------------------------------------------------------

//Java Imports.
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Hashtable;

//~--- classes ----------------------------------------------------------------

/**
 * This class encapsulates the business-area specific user settings.
 *
 * @author  Vaibhav.
 * @version $Id: $
 */
public class BAConfig implements TBitsConstants, Serializable {

    // Application logger.
    public static final TBitsLogger LOG = TBitsLogger.getLogger(PKG_CONFIG);

    //~--- fields -------------------------------------------------------------

    private boolean myCollapseBA;
    private String  myDefaultShortcutName;

    // Display Header.
    private ArrayList<String> myDisplayHeader;

    // enableVE bit
    private boolean myEnableVE;

    // Notify appender.
    private boolean myNotify;

    // Prefix of this BA.
    private String myPrefix;
    private int    myRoleFilter;

    // Table of short cuts.
    private Hashtable<String, Shortcut> myShortcuts;

    // My Requests Role and Status filters.
    private boolean myShowBA;

    // Sort field.
    private String mySortField;

    // Sort Order.
    private int               mySortOrder;
    private ArrayList<String> myStatusFilter;

    // Vacation bit.
    private boolean myVacation;

    //~--- constructors -------------------------------------------------------

    /**
     * The default constructor.
     */
    public BAConfig() {
        myPrefix              = "";
        myVacation            = false;
        myEnableVE            = true;
        myNotify              = true;
        myDisplayHeader       = ourDefaultHeader;
        myShowBA              = true;
        myCollapseBA          = false;
        myRoleFilter          = FILTER_LOGGER;
        myStatusFilter        = new ArrayList<String>();
        mySortField           = Field.REQUEST;
        mySortOrder           = DESC_ORDER;
        myShortcuts           = new Hashtable<String, Shortcut>();
        myDefaultShortcutName = "";
    }

    /**
     * The complete constructor.
     *
     * @param aPrefix
     * @param aVacation
     * @param aNotify
     * @param aEnableVE
     * @param aDisplayHeader
     * @param aShowBA
     * @param aCollapseBA
     * @param aRoleFilter
     * @param aStatusFilter
     * @param aSortField
     * @param aSortOrder
     * @param aShortcuts
     */
    public BAConfig(String aPrefix, boolean aVacation, boolean aNotify, boolean aEnableVE, ArrayList<String> aDisplayHeader, boolean aShowBA, boolean aCollapseBA, int aRoleFilter,
                    ArrayList<String> aStatusFilter, String aSortField, int aSortOrder, Hashtable<String, Shortcut> aShortcuts) {
        myPrefix        = aPrefix;
        myVacation      = aVacation;
        myEnableVE      = aEnableVE;
        myNotify        = aNotify;
        myDisplayHeader = aDisplayHeader;
        myShowBA        = aShowBA;
        myCollapseBA    = aCollapseBA;
        myRoleFilter    = aRoleFilter;
        myStatusFilter  = aStatusFilter;
        mySortField     = aSortField;
        mySortOrder     = aSortOrder;
        myShortcuts     = aShortcuts;
    }

    //~--- methods ------------------------------------------------------------

    /**
     * Method to add a shortcut to the list of shortcuts.
     *
     * @param aShortcut Shortcut to be added.
     *
     */
    public void addShortcut(Shortcut aShortcut) {
        if (aShortcut == null) {
            return;
        }

        if (myShortcuts == null) {
            myShortcuts = new Hashtable<String, Shortcut>();
        }

        myShortcuts.put(aShortcut.getName().toUpperCase(), aShortcut);

        if (aShortcut.getIsDefault() == true) {
            myDefaultShortcutName = aShortcut.getName();
        }
    }

    /**
     * This method removes the entry corresponding to given shortcut name from
     * the shortcut table.
     *
     * @param shortcutName      Name of the shortcut.
     */
    public void deleteShortcut(String shortcutName) {
        myShortcuts.remove(shortcutName.toUpperCase());
    }

    //~--- get methods --------------------------------------------------------

    /**
     * Accessor method for CollapseBA property.
     *
     * @return Current Value of CollapseBA
     *
     */
    public boolean getCollapseBA() {
        return myCollapseBA;
    }

    /**
     * Method to get the default shortcut if any for this user.
     *
     * @return The Default Shortcut object if it exists. Null otherwise.
     */
    public Shortcut getDefaultShortcut() {
        Shortcut sc = myShortcuts.get(myDefaultShortcutName);

        return sc;
    }

    /**
     * Accessor method for DefaultShortcutName property.
     *
     * @return Current Value of DefaultShortcutName
     *
     */
    public String getDefaultShortcutName() {
        return myDefaultShortcutName;
    }

    /**
     * Accessor method for DisplayHeader property.
     *
     * @return Current Value of DisplayHeader
     *
     */
    public ArrayList<String> getDisplayHeader() {
        if ((myDisplayHeader == null) || (myDisplayHeader.size() == 0)) {
            myDisplayHeader = ourDefaultHeader;
        }

        return myDisplayHeader;
    }

    /**
     * Accessor method for myEnableVE property.
     *
     * @return Current Value of myEnableVE
     *
     */
    public boolean getEnableVE() {
        return myEnableVE;
    }

    /**
     * Accessor method for Notify property.
     *
     * @return Current Value of Notify
     *
     */
    public boolean getNotify() {
        return myNotify;
    }

    /**
     * Accessor method for Prefix property.
     *
     * @return Current Value of Prefix
     *
     */
    public String getPrefix() {
        return myPrefix;
    }

    /**
     * Accessor method for RoleFilter property.
     *
     * @return Current Value of RoleFilter
     *
     */
    public int getRoleFilter() {
        return myRoleFilter;
    }

    /**
     * Method to get a shortcut with specified name.
     *
     * @param aName Shortcut to be added.
     *
     */
    public Shortcut getShortcut(String aName) {
        Shortcut sc = myShortcuts.get(aName.toUpperCase());

        return sc;
    }

    /**
     * Accessor method for myShortcuts property.
     *
     * @return Current Value of myShortcuts
     *
     */
    public Hashtable<String, Shortcut> getShortcuts() {
        return myShortcuts;
    }

    /**
     * Accessor method for ShowBA property.
     *
     * @return Current Value of ShowBA
     *
     */
    public boolean getShowBA() {
        return myShowBA;
    }

    /**
     * Accessor method for SortField property.
     *
     * @return Current Value of SortField
     *
     */
    public String getSortField() {
        return mySortField;
    }

    /**
     * Accessor method for SortOrder property.
     *
     * @return Current Value of SortOrder
     *
     */
    public int getSortOrder() {
        return mySortOrder;
    }

    /**
     * Accessor method for StatusFilter property.
     *
     * @return Current Value of StatusFilter
     *
     */
    public ArrayList<String> getStatusFilter() {
        return myStatusFilter;
    }

    /**
     * Accessor method for Vacation property.
     *
     * @return Current Value of Vacation
     *
     */
    public boolean getVacation() {
        return myVacation;
    }

    //~--- set methods --------------------------------------------------------

    /**
     * Mutator method for CollapseBA property.
     *
     * @param aCollapseBA New Value for CollapseBA
     *
     */
    public void setCollapseBA(boolean aCollapseBA) {
        myCollapseBA = aCollapseBA;
    }

    /**
     * Mutator method for DefaultShortcutName property.
     *
     * @param aDefaultShortcutName New Value for DefaultShortcutName
     *
     */
    public void setDefaultShortcutName(String aDefaultShortcutName) {
        myDefaultShortcutName = aDefaultShortcutName;
    }

    /**
     * Mutator method for DisplayHeader property.
     *
     * @param aDisplayHeader New Value for DisplayHeader
     *
     */
    public void setDisplayHeader(ArrayList<String> aDisplayHeader) {
        if ((aDisplayHeader == null) || (aDisplayHeader.size() == 0)) {
            aDisplayHeader = ourDefaultHeader;
        }

        myDisplayHeader = aDisplayHeader;
    }

    /**
     * Mutator method for EnableVE property.
     *
     * @param aEnableVE New Value for EnableVE
     *
     */
    public void setEnableVE(boolean aEnableVE) {
        myEnableVE = aEnableVE;
    }

    /**
     * Mutator method for notify property.
     *
     * @param aNotify New Value for Notify
     *
     */
    public void setNotify(boolean aNotify) {
        myNotify = aNotify;
    }

    /**
     * Mutator method for Prefix property.
     *
     * @param aPrefix New Value for Prefix
     *
     */
    public void setPrefix(String aPrefix) {
        myPrefix = aPrefix;
    }

    /**
     * Mutator method for RoleFilter property.
     *
     * @param aRoleFilter New Value for RoleFilter
     *
     */
    public void setRoleFilter(int aRoleFilter) {
        myRoleFilter = aRoleFilter;
    }

    /**
     * Mutator method for myShortcuts property.
     *
     * @param aShortcuts New Value for myShortcuts
     *
     */
    public void setShortcuts(Hashtable<String, Shortcut> aShortcuts) {
        myShortcuts = aShortcuts;
    }

    /**
     * Mutator method for ShowBA property.
     *
     * @param aShowBA New Value for ShowBA
     *
     */
    public void setShowBA(boolean aShowBA) {
        myShowBA = aShowBA;
    }

    /**
     * Mutator method for SortField property.
     *
     * @param aSortField New Value for SortField
     *
     */
    public void setSortField(String aSortField) {
        mySortField = aSortField;
    }

    /**
     * Mutator method for SortOrder property.
     *
     * @param aSortOrder New Value for SortOrder
     *
     */
    public void setSortOrder(int aSortOrder) {
        mySortOrder = aSortOrder;
    }

    /**
     * Mutator method for StatusFilter property.
     *
     * @param aStatusFilter New Value for StatusFilter
     *
     */
    public void setStatusFilter(ArrayList<String> aStatusFilter) {
        myStatusFilter = aStatusFilter;
    }

    /**
     * Mutator method for Vacation property.
     *
     * @param aVacation New Value for Vacation
     *
     */
    public void setVacation(boolean aVacation) {
        myVacation = aVacation;
    }
}
