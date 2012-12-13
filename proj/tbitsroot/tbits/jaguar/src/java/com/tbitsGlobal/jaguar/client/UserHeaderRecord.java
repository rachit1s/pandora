package com.tbitsGlobal.jaguar.client;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;

import commons.com.tbitsGlobal.utils.client.GlobalConstants;

public class UserHeaderRecord {
	
	public enum EntryType { ADDED, COMMON, DELETED }
	
	// Enum sort of fields for Attributes.
    public static final int  USERLOGIN = 1;
    public static final int  ORDERING  = 2;
    private static final int ENTRYTYPE = 3;
	
	private EntryType entryType;
    private int       ordering;

    // Attributes of this Domain Object.
    private String userLogin;
    
    private static int sortField = ORDERING;
    private static int sortOrder = GlobalConstants.ASC_ORDER;
    
    
    public UserHeaderRecord(EntryType entryType, int ordering, String userLogin) {
		super();
		this.entryType = entryType;
		this.ordering = ordering;
		this.userLogin = userLogin;
	}

	public int compareTo(UserHeaderRecord aObject) {
        switch (sortField) {
        case USERLOGIN : {
            String login1 = userLogin;
            String login2 = aObject.userLogin;

            if (sortOrder == GlobalConstants.ASC_ORDER) {
                return login1.compareToIgnoreCase(login2);
            } else {
                return login2.compareToIgnoreCase(login1);
            }
        }

        case ORDERING : {
            Integer i1 = ordering;
            Integer i2 = aObject.ordering;

            if (i1.equals(i2)) {
                String login1 = userLogin;
                String login2 = aObject.userLogin;

                if (sortOrder == GlobalConstants.ASC_ORDER) {
                    return login1.compareToIgnoreCase(login2);
                } else {
                    return login2.compareToIgnoreCase(login1);
                }
            }

            return (sortOrder == GlobalConstants.ASC_ORDER)
                   ? i1.compareTo(i2)
                   : i2.compareTo(i1);
        }
        }

        return 0;
    }
    
    /**
     * Method to return the source arraylist in the sorted order
     *
     * @param  source the array list of Type objects
     * @return the ArrayList of the UserHeaderRecord objects in sorted order
     */
    public static ArrayList<UserHeaderRecord> sort(ArrayList<UserHeaderRecord> source) {
        int                size     = source.size();
        UserHeaderRecord[] srcArray = new UserHeaderRecord[size];

        for (int i = 0; i < size; i++) {
            srcArray[i] = source.get(i);
        }

        Arrays.sort(srcArray, new UserHeaderRecordComparator());

        ArrayList<UserHeaderRecord> target = new ArrayList<UserHeaderRecord>();

        for (int i = 0; i < size; i++) {
            target.add(srcArray[i]);
        }

        return target;
    }
    
    /**
     * Accessor method for EntryType property.
     *
     * @return Current Value of EntryType
     *
     */
    public EntryType getEntryType() {
        return entryType;
    }

    /**
     * Accessor method for Ordering property.
     *
     * @return Current Value of Ordering
     *
     */
    public int getOrdering() {
        return ordering;
    }

    /**
     * Accessor method for UserLogin property.
     *
     * @return Current Value of UserLogin
     *
     */
    public String getUserLogin() {
        return userLogin;
    }

    //~--- set methods --------------------------------------------------------

    /**
     * Mutator method for EntryType property.
     *
     * @param aEntryType New Value for EntryType
     *
     */
    public void setEntryType(EntryType aEntryType) {
        entryType = aEntryType;
    }

    /**
     * Mutator method for Ordering property.
     *
     * @param aOrdering New Value for Ordering
     *
     */
    public void setOrdering(int aOrdering) {
        ordering = aOrdering;
    }

    /**
     * Mutator method for SortField property.
     *
     * @param aSortField New Value of SortField
     *
     */
    public static void setSortField(int aSortField) {
        sortField = aSortField;
    }

    /**
     * Mutator method for SortOrder property.
     *
     * @param aSortOrder New Value of SortOrder
     *
     */
    public static void setSortOrder(int aSortOrder) {
        sortOrder = aSortOrder;
    }

    /**
     * Mutator method for ourSortField and ourSortOrder properties.
     *
     * @param aSortField New Value of SortField
     * @param aSortOrder New Value of SortOrder
     *
     */
    public static void setSortParams(int aSortField, int aSortOrder) {
        sortField = aSortField;
        sortOrder = aSortOrder;
    }

    /**
     * Mutator method for UserLogin property.
     *
     * @param aUserLogin New Value for UserLogin
     *
     */
    public void setUserLogin(String aUserLogin) {
        userLogin = aUserLogin;
    }
}

/**
 * This class is the comparator of UserHeaderRecords.
 *
 *
 * @author : Vaibhav
 * @version : $Id: $
 */
class UserHeaderRecordComparator implements Comparator<UserHeaderRecord> {
    public int compare(UserHeaderRecord obj1, UserHeaderRecord obj2) {
        return obj1.compareTo(obj2);
    }

    public boolean equals(UserHeaderRecord o) {
        return this.equals(o);
    }
}
