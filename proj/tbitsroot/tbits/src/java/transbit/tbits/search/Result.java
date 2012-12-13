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
 * Result.java
 *
 * $Header:
 *
 */
package transbit.tbits.search;

//~--- non-JDK imports --------------------------------------------------------

//Imports from TBits.
import transbit.tbits.common.TBitsLogger;
import transbit.tbits.common.Timestamp;
import transbit.tbits.domain.Field;
import transbit.tbits.domain.Type;
import transbit.tbits.search.SearchConstants.ResultType;

//Static imports.
import static transbit.tbits.Helper.TBitsConstants.PKG_SEARCH;

//~--- JDK imports ------------------------------------------------------------

import java.io.Serializable;
import java.util.ArrayList;

//Java Imports.
import java.util.Arrays;
import java.util.Comparator;
import java.util.Hashtable;
import java.util.TreeMap;

//~--- classes ----------------------------------------------------------------

//Third Party Imports.

/**
 * This class encapsulates the input and output attributes of search request.
 *
 * @author  : Vaibhav.
 * @version : $Id: $
 */
public class Result {

    // Application Logger.
    public static final TBitsLogger LOG = TBitsLogger.getLogger(PKG_SEARCH);

    // Enum sort of fields for Attributes.
    private static final int SYSTEMID          = 1;
    private static final int SUBSCRIBER        = 9;
    private static final int STATUSID          = 4;
    private static final int SEVERITYID        = 5;
    private static final int REQUESTTYPEID     = 6;
    private static final int REQUESTID         = 2;
    private static final int LOGGER            = 7;
    private static final int CATEGORYID        = 3;
    private static final int ASSIGNEE          = 8;
    private static final int TO                = 10;
    private static final int SUBJECT           = 12;
    private static final int PARENTREQUESTID   = 15;
    private static final int ISPRIVATE         = 14;
    private static final int DESCRIPTION       = 13;
    private static final int CC                = 11;
    private static final int USERID            = 16;
    private static final int SUMMARY           = 23;
    private static final int REPLIED_TO_ACTION = 28;
    private static final int RELATED_REQUESTS  = 29;
    private static final int OFFICE_ID         = 30;
    private static final int NOTIFYLOGGERS     = 27;
    private static final int NOTIFY            = 26;
    private static final int MEMO              = 24;
    private static final int MAXACTIONID       = 17;
    private static final int LOGGEDDATE        = 19;
    private static final int LASTUPDATEDDATE   = 20;
    private static final int HEADERDESCRIPTION = 21;
    private static final int DUEDATE           = 18;
    private static final int ATTACHMENTS       = 22;
    private static final int APPENDINTERFACE   = 25;

    // Static attributes related to sorting.
    private static int ourSortField;
    private static int ourSortOrder;

    //~--- fields -------------------------------------------------------------

    private int myFlags = 0;

    // Due date of the request.
    private Timestamp myDueDate;

    // Indentation and leaf bi
    private int myIndentation;

    // Confidential property of the request.
    private Boolean myIsPrivate;

    // Last action read by the user in this request.
    private int myLastActionId;

    // Latest action on the request.
    private int myMaxActionId;

    // Parent Request Id.
    private int myParentId;

    // Request Id.
    private int myRequestId;

    // Underlying data structure that stores the data.
    private TreeMap<String, Object> myResult;

    // ResultType.
    private ResultType myResultType;

    // Severity of the request.
    private String mySeverity;

    // Status of the request.
    private String myStatus;

    // These fields are required for accomplish visual enhancements.
    // System Id.
    private int mySystemId;

    //~--- constructors -------------------------------------------------------

    /**
     * Default Constructor.
     */
    public Result() {
        myResult = new TreeMap<String, Object>();
    }

    //~--- methods ------------------------------------------------------------

    /**
     * Setter method.
     */
    public void addAll(Hashtable<String, Object> value) {
        if (value == null) {
            return;
        }

        myResult.putAll(value);
    }

    /**
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
    public int compareTo(Result aObject) {
        try {
            switch (ourSortField) {
            case SYSTEMID : {
                Integer i1 = mySystemId;
                Integer i2 = aObject.mySystemId;

                return (ourSortOrder == 0)
                       ? i1.compareTo(i2)
                       : i2.compareTo(i1);
            }

            case REQUESTID : {
                Integer l1 = (Integer) myResult.get(Field.REQUEST);
                Integer l2 = (Integer) aObject.myResult.get(Field.REQUEST);

                return (ourSortOrder == 0)
                       ? l1.compareTo(l2)
                       : l2.compareTo(l1);
            }

            case CATEGORYID : {
                String s1 = (String) myResult.get(Field.CATEGORY);
                String s2 = (String) aObject.myResult.get(Field.CATEGORY);

                try {
                    int     systemId  = mySystemId;
                    String  fieldName = Field.CATEGORY;
                    Integer i1        = Type.lookupBySystemIdAndFieldNameAndTypeName(systemId, fieldName, s1).getOrdering();
                    Integer i2        = Type.lookupBySystemIdAndFieldNameAndTypeName(systemId, fieldName, s2).getOrdering();

                    return (ourSortOrder == 0)
                           ? i1.compareTo(i2)
                           : i2.compareTo(i1);
                } catch (Exception e) {}

                return (ourSortOrder == 0)
                       ? s1.compareToIgnoreCase(s2)
                       : s2.compareToIgnoreCase(s1);
            }

            case STATUSID : {
                String s1 = (String) myResult.get(Field.STATUS);
                String s2 = (String) aObject.myResult.get(Field.STATUS);

                try {
                    int     systemId  = mySystemId;
                    String  fieldName = Field.STATUS;
                    Integer i1        = Type.lookupBySystemIdAndFieldNameAndTypeName(systemId, fieldName, s1).getOrdering();
                    Integer i2        = Type.lookupBySystemIdAndFieldNameAndTypeName(systemId, fieldName, s2).getOrdering();

                    return (ourSortOrder == 0)
                           ? i1.compareTo(i2)
                           : i2.compareTo(i1);
                } catch (Exception e) {}

                return (ourSortOrder == 0)
                       ? s1.compareToIgnoreCase(s2)
                       : s2.compareToIgnoreCase(s1);
            }

            case SEVERITYID : {
                String s1 = (String) myResult.get(Field.SEVERITY);
                String s2 = (String) aObject.myResult.get(Field.SEVERITY);

                try {
                    int     systemId  = mySystemId;
                    String  fieldName = Field.SEVERITY;
                    Integer i1        = Type.lookupBySystemIdAndFieldNameAndTypeName(systemId, fieldName, s1).getOrdering();
                    Integer i2        = Type.lookupBySystemIdAndFieldNameAndTypeName(systemId, fieldName, s2).getOrdering();

                    return (ourSortOrder == 0)
                           ? i1.compareTo(i2)
                           : i2.compareTo(i1);
                } catch (Exception e) {}

                return (ourSortOrder == 0)
                       ? s1.compareToIgnoreCase(s2)
                       : s2.compareToIgnoreCase(s1);
            }

            case REQUESTTYPEID : {
                String s1 = (String) myResult.get(Field.REQUEST_TYPE);
                String s2 = (String) aObject.myResult.get(Field.REQUEST_TYPE);

                try {
                    int     systemId  = mySystemId;
                    String  fieldName = Field.REQUEST_TYPE;
                    Integer i1        = Type.lookupBySystemIdAndFieldNameAndTypeName(systemId, fieldName, s1).getOrdering();
                    Integer i2        = Type.lookupBySystemIdAndFieldNameAndTypeName(systemId, fieldName, s2).getOrdering();

                    return (ourSortOrder == 0)
                           ? i1.compareTo(i2)
                           : i2.compareTo(i1);
                } catch (Exception e) {}

                return (ourSortOrder == 0)
                       ? s1.compareToIgnoreCase(s2)
                       : s2.compareToIgnoreCase(s1);
            }

            case LOGGER : {
                String s1 = (String) myResult.get(Field.LOGGER);
                String s2 = (String) aObject.myResult.get(Field.LOGGER);

                s1 = (s1 == null)
                     ? ""
                     : s1;
                s2 = (s2 == null)
                     ? ""
                     : s2;

                return (ourSortOrder == 0)
                       ? s1.compareToIgnoreCase(s2)
                       : s2.compareToIgnoreCase(s1);
            }

            case ASSIGNEE : {
                String s1 = (String) myResult.get(Field.ASSIGNEE);
                String s2 = (String) aObject.myResult.get(Field.ASSIGNEE);

                s1 = (s1 == null)
                     ? ""
                     : s1;
                s2 = (s2 == null)
                     ? ""
                     : s2;

                return (ourSortOrder == 0)
                       ? s1.compareToIgnoreCase(s2)
                       : s2.compareToIgnoreCase(s1);
            }

            case SUBSCRIBER : {
                String s1 = (String) myResult.get(Field.SUBSCRIBER);
                String s2 = (String) aObject.myResult.get(Field.SUBSCRIBER);

                s1 = (s1 == null)
                     ? ""
                     : s1;
                s2 = (s2 == null)
                     ? ""
                     : s2;

                return (ourSortOrder == 0)
                       ? s1.compareToIgnoreCase(s2)
                       : s2.compareToIgnoreCase(s1);
            }

            case TO : {
                String s1 = (String) myResult.get(Field.TO);
                String s2 = (String) aObject.myResult.get(Field.TO);

                s1 = (s1 == null)
                     ? ""
                     : s1;
                s2 = (s2 == null)
                     ? ""
                     : s2;

                return (ourSortOrder == 0)
                       ? s1.compareToIgnoreCase(s2)
                       : s2.compareToIgnoreCase(s1);
            }

            case CC : {
                String s1 = (String) myResult.get(Field.CC);
                String s2 = (String) aObject.myResult.get(Field.CC);

                s1 = (s1 == null)
                     ? ""
                     : s1;
                s2 = (s2 == null)
                     ? ""
                     : s2;

                return (ourSortOrder == 0)
                       ? s1.compareToIgnoreCase(s2)
                       : s2.compareToIgnoreCase(s1);
            }

            case SUBJECT : {
                String s1 = (String) myResult.get(Field.SUBJECT);
                String s2 = (String) aObject.myResult.get(Field.SUBJECT);

                s1 = (s1 == null)
                     ? ""
                     : s1;
                s2 = (s2 == null)
                     ? ""
                     : s2;

                return (ourSortOrder == 0)
                       ? s1.compareToIgnoreCase(s2)
                       : s2.compareToIgnoreCase(s1);
            }

            case DESCRIPTION : {
                String s1 = (String) myResult.get(Field.DESCRIPTION);
                String s2 = (String) aObject.myResult.get(Field.DESCRIPTION);

                s1 = (s1 == null)
                     ? ""
                     : s1;
                s2 = (s2 == null)
                     ? ""
                     : s2;

                return (ourSortOrder == 0)
                       ? s1.compareToIgnoreCase(s2)
                       : s2.compareToIgnoreCase(s1);
            }

            case ISPRIVATE : {
                Boolean b1 = (Boolean) myResult.get(Field.IS_PRIVATE);
                Boolean b2 = (Boolean) aObject.myResult.get(Field.IS_PRIVATE);

                return (ourSortOrder == 0)
                       ? b1.compareTo(b2)
                       : b2.compareTo(b1);
            }

            case PARENTREQUESTID : {
                Integer l1 = (Integer) myResult.get(Field.PARENT_REQUEST_ID);
                Integer l2 = (Integer) aObject.myResult.get(Field.PARENT_REQUEST_ID);

                return (ourSortOrder == 0)
                       ? l1.compareTo(l2)
                       : l2.compareTo(l1);
            }

            case USERID : {
                String s1 = (String) myResult.get(Field.USER);
                String s2 = (String) aObject.myResult.get(Field.USER);

                s1 = (s1 == null)
                     ? ""
                     : s1;
                s2 = (s2 == null)
                     ? ""
                     : s2;

                return (ourSortOrder == 0)
                       ? s1.compareToIgnoreCase(s2)
                       : s2.compareToIgnoreCase(s1);
            }

            case MAXACTIONID : {
                return 0;
            }

            case DUEDATE : {
                Timestamp t1 = (Timestamp) myResult.get(Field.DUE_DATE);
                Timestamp t2 = (Timestamp) aObject.myResult.get(Field.DUE_DATE);

                return (ourSortOrder == 0)
                       ? t1.compareTo(t2)
                       : t2.compareTo(t1);
            }

            case LOGGEDDATE : {
                Timestamp t1 = (Timestamp) myResult.get(Field.LOGGED_DATE);
                Timestamp t2 = (Timestamp) aObject.myResult.get(Field.LOGGED_DATE);

                return (ourSortOrder == 0)
                       ? t1.compareTo(t2)
                       : t2.compareTo(t1);
            }

            case LASTUPDATEDDATE : {
                Timestamp t1 = (Timestamp) myResult.get(Field.LASTUPDATED_DATE);
                Timestamp t2 = (Timestamp) aObject.myResult.get(Field.LASTUPDATED_DATE);

                return (ourSortOrder == 0)
                       ? t1.compareTo(t2)
                       : t2.compareTo(t1);
            }

            case HEADERDESCRIPTION : {
                String s1 = (String) myResult.get(Field.HEADER_DESCRIPTION);
                String s2 = (String) aObject.myResult.get(Field.HEADER_DESCRIPTION);

                return (ourSortOrder == 0)
                       ? s1.compareToIgnoreCase(s2)
                       : s2.compareToIgnoreCase(s1);
            }

            case ATTACHMENTS : {
                String s1 = (String) myResult.get(Field.ATTACHMENTS);
                String s2 = (String) aObject.myResult.get(Field.ATTACHMENTS);

                s1 = (s1 == null)
                     ? ""
                     : s1;
                s2 = (s2 == null)
                     ? ""
                     : s2;

                return (ourSortOrder == 0)
                       ? s1.compareToIgnoreCase(s2)
                       : s2.compareToIgnoreCase(s1);
            }

            case SUMMARY : {
                String s1 = (String) myResult.get(Field.SUMMARY);
                String s2 = (String) aObject.myResult.get(Field.SUMMARY);

                s1 = (s1 == null)
                     ? ""
                     : s1;
                s2 = (s2 == null)
                     ? ""
                     : s2;

                return (ourSortOrder == 0)
                       ? s1.compareToIgnoreCase(s2)
                       : s2.compareToIgnoreCase(s1);
            }

            case MEMO : {
                String s1 = (String) myResult.get(Field.MEMO);
                String s2 = (String) aObject.myResult.get(Field.MEMO);

                s1 = (s1 == null)
                     ? ""
                     : s1;
                s2 = (s2 == null)
                     ? ""
                     : s2;

                return (ourSortOrder == 0)
                       ? s1.compareToIgnoreCase(s2)
                       : s2.compareToIgnoreCase(s1);
            }

            case APPENDINTERFACE : {
                Integer l1 = (Integer) myResult.get(Field.APPEND_INTERFACE);
                Integer l2 = (Integer) aObject.myResult.get(Field.APPEND_INTERFACE);

                return (ourSortOrder == 0)
                       ? l1.compareTo(l2)
                       : l2.compareTo(l1);
            }

            case NOTIFY : {
                Boolean b1 = (Boolean) myResult.get(Field.NOTIFY);
                Boolean b2 = (Boolean) aObject.myResult.get(Field.NOTIFY);

                return (ourSortOrder == 0)
                       ? b1.compareTo(b2)
                       : b2.compareTo(b1);
            }

            case NOTIFYLOGGERS : {
                Boolean b1 = (Boolean) myResult.get(Field.NOTIFY_LOGGERS);
                Boolean b2 = (Boolean) aObject.myResult.get(Field.NOTIFY_LOGGERS);

                return (ourSortOrder == 0)
                       ? b1.compareTo(b2)
                       : b2.compareTo(b1);
            }

            case OFFICE_ID : {
                String s1 = (String) myResult.get(Field.OFFICE);
                String s2 = (String) aObject.myResult.get(Field.OFFICE);

                try {
                    int     systemId  = mySystemId;
                    String  fieldName = Field.OFFICE;
                    Integer i1        = Type.lookupBySystemIdAndFieldNameAndTypeName(systemId, fieldName, s1).getOrdering();
                    Integer i2        = Type.lookupBySystemIdAndFieldNameAndTypeName(systemId, fieldName, s2).getOrdering();

                    return (ourSortOrder == 0)
                           ? i1.compareTo(i2)
                           : i2.compareTo(i1);
                } catch (Exception e) {}

                return (ourSortOrder == 0)
                       ? s1.compareToIgnoreCase(s2)
                       : s2.compareToIgnoreCase(s1);
            }
            }
        } catch (Exception e) {
            LOG.warn(e.toString(), e);
        }

        return 0;
    }

    /**
     * Equals method overridden for this class.
     */
    public boolean equals(Object obj) {
        try {
            Result sr   = (Result) obj;
            int    req1 = ((Integer) this.get(Field.REQUEST)).intValue();
            int    req2 = ((Integer) sr.get(Field.REQUEST)).intValue();

            if (req1 == req2) {
                return true;
            }

            return false;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Method to return the source arraylist in the sorted order
     *
     * @param  source the array list of Type objects
     * @return the ArrayList of the Result objects in sorted order
     */
    public static ArrayList<Result> sort(ArrayList<Result> source) {
        int      size     = source.size();
        Result[] srcArray = new Result[size];

        for (int i = 0; i < size; i++) {
            srcArray[i] = source.get(i);
        }

        Arrays.sort(srcArray, new ResultComparator());

        ArrayList<Result> target = new ArrayList<Result>();

        for (int i = 0; i < size; i++) {
            target.add(srcArray[i]);
        }

        return target;
    }

    /**
     *
     */
    public String toString() {
        ArrayList<String> keys = new ArrayList<String>(myResult.keySet());
        StringBuilder     sb   = new StringBuilder();

        for (String key : keys) {
            sb.append(key).append(": ").append(myResult.get(key)).append("; ");
        }

        return sb.toString();
    }

    //~--- get methods --------------------------------------------------------

    /**
     * Getter method.
     */
    public Object get(String aFieldName) {
        return myResult.get(aFieldName);
    }

    /**
     * Accessor method for DueDate property.
     *
     */
    public Timestamp getDueDate() {
        return myDueDate;
    }

    /**
     * ResultType
     * Accessor method for Flags property.
     *
     * @return Current Value of Flags
     *
     */
    public int getFlags() {
        return myFlags;
    }

    /**
     * Accessor method for Indentation property.
     *
     */
    public int getIndentation() {
        return myIndentation;
    }

    /**
     * Accessor method for IsPrivate property.
     *
     */
    public boolean getIsPrivate() {
        return myIsPrivate;
    }

    /**
     * Accessor method for LastActionId property.
     *
     */
    public int getLastActionId() {
        return myLastActionId;
    }

    /**
     * Accessor method for MaxActionId property.
     *
     */
    public int getMaxActionId() {
        return myMaxActionId;
    }

    /**
     * Accessor method for ParentId property.
     *
     */
    public int getParentId() {
        return myParentId;
    }

    /**
     * Accessor method for RequestId property.
     *
     */
    public int getRequestId() {
        return myRequestId;
    }

    /**
     * Accessor method for ResultType property.
     *
     * @return Current Value of ResultType
     *
     */
    public ResultType getResultType() {
        return myResultType;
    }

    /**
     * Accessor method for Severity property.
     *
     */
    public String getSeverity() {
        return mySeverity;
    }

    /**
     * Accessor method for Status property.
     *
     */
    public String getStatus() {
        return myStatus;
    }

    /**
     * Accessor method for SystemId property.
     *
     */
    public int getSystemId() {
        return mySystemId;
    }

    //~--- set methods --------------------------------------------------------

    /**
     * Setter method.
     */
    public void set(String aFieldName, Object aValue) {
        if (aValue == null) {
            aValue = "-";
        }

        myResult.put(aFieldName, aValue);
    }

    /**
     * Mutator method for DueDate property.
     *
     * @param aDueDate New Value of DueDate
     *
     */
    public void setDueDate(Timestamp aDueDate) {
        myDueDate = aDueDate;
    }

    /**
     * Mutator method for Flags property.
     *
     * @param aFlags New Value for Flags
     *
     */
    public void setFlags(int aFlags) {
        myFlags = aFlags;
    }

    /**
     * Mutator method for Indentation property.
     *
     * @param aIndentation New Value of Indentation
     *
     */
    public void setIndentation(int aIndentation) {
        myIndentation = aIndentation;
    }

    /**
     * Mutator method for IsPrivate property.
     *
     * @param aIsPrivate New Value of IsPrivate
     *
     */
    public void setIsPrivate(boolean aIsPrivate) {
        myIsPrivate = aIsPrivate;
    }

    /**
     * Mutator method for LastActionId property.
     *
     * @param aLastActionId New Value of LastActionId
     *
     */
    public void setLastActionId(int aLastActionId) {
        myLastActionId = aLastActionId;
    }

    /**
     * Mutator method for MaxActionId property.
     *
     * @param aMaxActionId New Value of MaxActionId
     *
     */
    public void setMaxActionId(int aMaxActionId) {
        myMaxActionId = aMaxActionId;
    }

    /**
     * Mutator method for ParentId property.
     *
     * @param aParentId New Value of ParentId
     *
     */
    public void setParentId(int aParentId) {
        myParentId = aParentId;
    }

    /**
     * Mutator method for RequestId property.
     *
     * @param aRequestId New Value of RequestId
     *
     */
    public void setRequestId(int aRequestId) {
        myRequestId = aRequestId;
    }

    /**
     * Mutator method for ResultType property.
     *
     * @param aResultType New Value for ResultType
     *
     */
    public void setResultType(ResultType aResultType) {
        myResultType = aResultType;
    }

    /**
     * Mutator method for Severity property.
     *
     * @param aSeverity New Value of Severity
     *
     */
    public void setSeverity(String aSeverity) {
        if (aSeverity == null) {
            aSeverity = "-";
        }

        mySeverity = aSeverity;
    }

    /**
     * Mutator method for SortField property.
     *
     * @param aSortField New Value of SortField
     *
     */
    public static void setSortField(String aSortField) {
        setSortParams(aSortField, ourSortOrder);
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
    public static void setSortParams(String aSortField, int aSortOrder) {
        ourSortOrder = aSortOrder;

        if (aSortField.equals(Field.BUSINESS_AREA)) {
            ourSortField = REQUESTID;
        } else if (aSortField.equals(Field.REQUEST)) {
            ourSortField = REQUESTID;
        } else if (aSortField.equals(Field.CATEGORY)) {
            ourSortField = CATEGORYID;
        } else if (aSortField.equals(Field.STATUS)) {
            ourSortField = STATUSID;
        } else if (aSortField.equals(Field.SEVERITY)) {
            ourSortField = SEVERITYID;
        } else if (aSortField.equals(Field.REQUEST_TYPE)) {
            ourSortField = REQUESTTYPEID;
        } else if (aSortField.equals(Field.LOGGER)) {
            ourSortField = LOGGER;
        } else if (aSortField.equals(Field.ASSIGNEE)) {
            ourSortField = ASSIGNEE;
        } else if (aSortField.equals(Field.SUBSCRIBER)) {
            ourSortField = SUBSCRIBER;
        } else if (aSortField.equals(Field.TO)) {
            ourSortField = TO;
        } else if (aSortField.equals(Field.CC)) {
            ourSortField = CC;
        } else if (aSortField.equals(Field.SUBJECT)) {
            ourSortField = SUBJECT;
        } else if (aSortField.equals(Field.DESCRIPTION)) {
            ourSortField = DESCRIPTION;
        } else if (aSortField.equals(Field.IS_PRIVATE)) {
            ourSortField = ISPRIVATE;
        } else if (aSortField.equals(Field.PARENT_REQUEST_ID)) {
            ourSortField = PARENTREQUESTID;
        } else if (aSortField.equals(Field.USER)) {
            ourSortField = USERID;
        } else if (aSortField.equals(Field.MAX_ACTION_ID)) {
            ourSortField = MAXACTIONID;
        } else if (aSortField.equals(Field.DUE_DATE)) {
            ourSortField = DUEDATE;
        } else if (aSortField.equals(Field.LOGGED_DATE)) {
            ourSortField = LOGGEDDATE;
        } else if (aSortField.equals(Field.LASTUPDATED_DATE)) {
            ourSortField = LASTUPDATEDDATE;
        } else if (aSortField.equals(Field.HEADER_DESCRIPTION)) {
            ourSortField = HEADERDESCRIPTION;
        } else if (aSortField.equals(Field.ATTACHMENTS)) {
            ourSortField = ATTACHMENTS;
        } else if (aSortField.equals(Field.SUMMARY)) {
            ourSortField = SUMMARY;
        } else if (aSortField.equals(Field.MEMO)) {
            ourSortField = MEMO;
        } else if (aSortField.equals(Field.APPEND_INTERFACE)) {
            ourSortField = APPENDINTERFACE;
        } else if (aSortField.equals(Field.NOTIFY)) {
            ourSortField = NOTIFY;
        } else if (aSortField.equals(Field.NOTIFY_LOGGERS)) {
            ourSortField = NOTIFYLOGGERS;
        } else if (aSortField.equals(Field.REPLIED_TO_ACTION)) {
            ourSortField = REPLIED_TO_ACTION;
        } else if (aSortField.equals(Field.RELATED_REQUESTS)) {
            ourSortField = RELATED_REQUESTS;
        } else if (aSortField.equals(Field.OFFICE)) {
            ourSortField = OFFICE_ID;
        }
    }

    /**
     * Mutator method for Status property.
     *
     * @param aStatus New Value of Status
     *
     */
    public void setStatus(String aStatus) {
        if (aStatus == null) {
            aStatus = "-";
        }

        myStatus = aStatus;
    }

    /**
     * Mutator method for SystemId property.
     *
     * @param aSystemId New Value of SystemId
     *
     */
    public void setSystemId(int aSystemId) {
        mySystemId = aSystemId;
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
class ResultComparator implements Comparator<Result>, Serializable {
    public int compare(Result obj1, Result obj2) {
        return obj1.compareTo(obj2);
    }
}
