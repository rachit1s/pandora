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
 * Searcher.java
 *
 * $Header:
 *
 */
package transbit.tbits.search;

//~--- non-JDK imports --------------------------------------------------------

import static transbit.tbits.Helper.TBitsConstants.PKG_SEARCH;
import static transbit.tbits.Helper.TBitsConstants.PRIMARY_SIGN;
import static transbit.tbits.domain.DataType.BOOLEAN;
import static transbit.tbits.domain.DataType.DATE;
import static transbit.tbits.domain.DataType.DATETIME;
import static transbit.tbits.domain.DataType.INT;
import static transbit.tbits.domain.DataType.REAL;
import static transbit.tbits.domain.DataType.STRING;
import static transbit.tbits.domain.DataType.TEXT;
import static transbit.tbits.domain.DataType.TIME;
import static transbit.tbits.domain.DataType.TYPE;
import static transbit.tbits.domain.DataType.USERTYPE;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.Stack;
import java.util.StringTokenizer;

import transbit.tbits.common.DataSourcePool;
import transbit.tbits.common.DatabaseException;
import transbit.tbits.common.TBitsLogger;
import transbit.tbits.common.Timestamp;
import transbit.tbits.domain.BusinessArea;
import transbit.tbits.domain.DataType;
import transbit.tbits.domain.Field;
import transbit.tbits.domain.FieldDescriptor;
import transbit.tbits.domain.MailListUser;
import transbit.tbits.domain.Permission;
import transbit.tbits.domain.RolePermission;
import transbit.tbits.domain.User;
import transbit.tbits.domain.UserReadAction;
import transbit.tbits.domain.UserType;
import transbit.tbits.exception.DSQLParseException;
import transbit.tbits.indexer.LuceneSearcher;
import transbit.tbits.search.ParseEntry.ParseEntryType;

//~--- classes ----------------------------------------------------------------

//Third Party Imports.

/**
 * This class encapsulates the input and output attributes of search request.
 *
 * @author  : Vaibhav.
 * @version : $Id: $
 */
public class Searcher implements SearchConstants {

    // Application Logger.
    public static final TBitsLogger LOG                   = TBitsLogger.getLogger(PKG_SEARCH);
    private static String           ourDefaultSortField   = Field.REQUEST;
    private static int              ourDefaultSortOrder   = DESC_ORDER;
    private static int              ourDefaultMaxPageSize = 100000;

    // Defaults.
    public static ArrayList<String> ourDefaultHeader;

    //~--- static initializers ------------------------------------------------

    /*
     * Initalize the static variables.
     */
    static {

        // Fill the default header columns.
        ourDefaultHeader = new ArrayList<String>();

        /*
         * Severity | Request | Subject | Category | Status | Logger | Assignee
         */
        ourDefaultHeader.add(Field.SEVERITY);
        ourDefaultHeader.add(Field.REQUEST);
        ourDefaultHeader.add(Field.SUBJECT);
        ourDefaultHeader.add(Field.CATEGORY);
        ourDefaultHeader.add(Field.STATUS);
        ourDefaultHeader.add(Field.LOGGER);
        ourDefaultHeader.add(Field.ASSIGNEE);
    }

    //~--- fields -------------------------------------------------------------

    private int                                   myPrivatePerm   = NO_PRIVATE;
    private int                                   myRolePermLevel = NO_PRIVATE;
    private boolean                               myHasTreeQuery  = false;
    private boolean                               myHasTextField  = false;
    private boolean                               myHasAllTextField  = false;
    private boolean                               myHasCloseInfo  = false;
    private boolean                               myHasAppender   = false;
    private boolean                               myHasAppendDate = false;
    private ParseEntry                            myAllTextEntry  = null;
    private ArrayList<String>                     myAllRequestIdList;
    private Hashtable<Integer, ArrayList<String>> myAllRequestIdListByBA;
    private BusinessArea                          myBusinessArea;
    private int                                   myCurrentPageSize;
    private String                                myDSQLQuery;
    private String                                myDatabaseQuery;
    private ArrayList<String>                     myDisplayHeader;
    private String                                myDropQuery;
    private Hashtable<String, String>             myFDtoName;
    private Hashtable<String, Field>              myFieldsTable;
    private String                                myFilteringQuery;
    private boolean                               myHasExtFields;
    private boolean                               myHasUserFields;
    private String                                myHierarchyQuery;
    private boolean                               myIsListAll;
    private String                                myLuceneQuery;
    private int                                   myMaximumPageSize;
    private int                                   myPageNumber;
    private Hashtable<String, ArrayList<String>>  myParentsTable;
    private ArrayList<ParseEntry>                 myParseTable;
    private Hashtable<String, Integer>            myPermissionTable;
    private RequestType                           myRequestType;
    private Hashtable<String, Result>             myRequestsTable;
    private ArrayList<String>                     myRequestsWithIncompleteTrees;
    private ArrayList<String>                     myResultIdList;
    private Hashtable<Integer, ArrayList<String>> myResultIdListByBA;
    private ArrayList<Result>                     myResultList;
    private Hashtable<Integer, ArrayList<Result>> myResultListByBA;
    private int                                   myRetrievedResultCount;
    private String                                mySelectionQuery;
    private String                                mySortField;
    private int                                   mySortOrder;
    private int                                   mySystemId;
    private String                                mySystemPrefix;
    private ArrayList<ParseEntry>                 myTextEntries;

    // Datastructures to store the output of the database query.
    private int myTotalResultCount;

    // Input variables that are not settable.
    private int myTreeId;

    // Input variables that are settable.
    private int               myUserId;
    private ArrayList<String> myVEHeader;

    //~--- constant enums -----------------------------------------------------

    // Invoked with a criteria request.
    private static enum RequestType {
        NO_OP_REQUEST,    // Instantiated without any parameters.
        QUERY_REQUEST,    // Request to use searcher with a query.
        PARSE_TABLE_REQUEST, CUSTOM_FILTER_REQUEST
    }

    ;

    //~--- constructors -------------------------------------------------------

    /*
     * START: Constructors.
     *
     */

    /**
     * Default constructor. Does nothing.
     */
    public Searcher() {
        myRequestType = RequestType.NO_OP_REQUEST;
    }

    /**
     * Initialize search to execute the query directly.
     *
     * @param aUserId
     * @param aFTable
     * @param aFQ
     */
    public Searcher(int aUserId, Hashtable<String, Field> aFTable, String aFQ) {
        setDefaults();
        myUserId         = aUserId;
        myFieldsTable    = aFTable;
        myFilteringQuery = aFQ;
        myRequestType    = RequestType.CUSTOM_FILTER_REQUEST;
    }

    /**
     * Initialize search with parse table.
     *
     * @param aSystemId
     * @param aUserId
     * @param aParseTable
     */
    public Searcher(int aSystemId, int aUserId, ArrayList<ParseEntry> aParseTable) {
        setDefaults();
        mySystemId    = aSystemId;
        myUserId      = aUserId;
        myParseTable  = aParseTable;
        myRequestType = RequestType.PARSE_TABLE_REQUEST;
    }

    /**
     * Initialize a search request with the DSQL Query.
     *
     * @param aSystemId  Business Area Id.
     * @param aUserId    Id of the user who requested for a search.
     * @param aDSQLQuery Query in DSQL.
     *
     */
    public Searcher(int aSystemId, int aUserId, String aDSQLQuery) {
        setDefaults();
        mySystemId    = aSystemId;
        myUserId      = aUserId;
        myDSQLQuery   = aDSQLQuery;
        myRequestType = RequestType.QUERY_REQUEST;
    }

    //~--- methods ------------------------------------------------------------

    /**
     * This method returns the CREATE table statement for the table holds
     * the results returned by the filtering query.
     *
     * @return Create table statement.
     */
    private String declareTempTable(String tableName, String sortField) {
        StringBuffer buffer = new StringBuffer();

        buffer
        .append("\nDECLARE ").append(tableName).append(" TABLE ")
        .append("\n(")
        .append("\n\tsys_id INT,")
        .append("\n\trequest_id INT,")
        .append("\n\tparent_request_id INT")
        .append(sortField)
        .append("\n)").append("\n");

        return buffer.toString();
    }

    /*
     * Create temporary table. #xyz as opposed to declare table which is @xyz
     * It also drops a table if already present.
     */
    private String createTmpTable(String tableName, String sortField) {
        StringBuffer buffer = new StringBuffer();
        //if exists 
        buffer.append("if object_id('tempdb..").append(tableName).append("') is not null\n\tdrop table ").append(tableName).append("\n");
        buffer.append("\nCREATE TABLE ").append(tableName).append("\n(\n\tsys_id INT,\n\trequest_id INT,\n\tparent_request_id INT").append(
            sortField).append("\n)\n");

        return buffer.toString();
    }
    
    /**
     * This method executes the query and retrieves the results.
     *
     * @exception DatabaseException
     */
    private void executeQuery() throws DatabaseException {
        Connection con = null;

        try {
            con = DataSourcePool.getConnection();

            Statement cs   = con.createStatement();
            boolean   flag = false;
//            System.out.println("Nitiraj : Database query :\n" + myDatabaseQuery + "\n*******end***");
            try
            {
                flag = cs.execute(myDatabaseQuery);
            }
            catch(SQLException sqle)
            {
                LOG.error("Error in executing : " + myDatabaseQuery);
                sqle.printStackTrace();
                throw sqle;
            }
            
//          int i = 0 ; 
//          while (true) 
//          {
//          	System.out.println("Iteration # " + ++i );
//          	if((cs.getMoreResults() == false))
//          	{
//          		if((cs.getUpdateCount() == -1))
//          		{
//          			System.out.println("This is not a Result set nor a update count So this is the end hence breaking.");
//          			break;
//          		}
//          		System.out.println("Skipping the result set for iteration # " + i);
//          		// Skip them...
//          		continue;
//          	}
//          	
//          	ResultSet rs = cs.getResultSet() ;
//          	if( rs != null )
//          	{
//          		ResultSetMetaData rsmd = rs.getMetaData() ;
//          		String columns = "" ;
//	            	int numColumns = rsmd.getColumnCount(); // Get the column names; column indices start from 1 
//	            	for (int x=1; x<numColumns+1; x++) 
//	            	{ 
//	            		String columnName = rsmd.getColumnName(x); // Get the name of the column's table name 
//	            		String tableName = rsmd.getTableName(x);  
//	            		columns += "," + columnName;
//	            	}
//	            	
//	            	System.out.println("This result set has columns as : " + columns );
//          	}
//          	else
//          	{
//          		System.out.println("Error in processing iteration # " + i);
//          	}
//          		
//          }

            /*
             * int count = cs.getUpdateCount();
             * LOG.info("Count: " + count + " Flag: " + flag);
             * while ((flag == false && count == -1) == false)
             * {
             *   flag = cs.getMoreResults();
             *   count = cs.getUpdateCount();
             *   LOG.info("Count: " + count + " Flag: " + flag);
             * }
             */
            myResultIdList                = new ArrayList<String>();
            myResultIdListByBA            = new Hashtable<Integer, ArrayList<String>>();
            myAllRequestIdList            = new ArrayList<String>();
            myAllRequestIdListByBA        = new Hashtable<Integer, ArrayList<String>>();
            myRequestsWithIncompleteTrees = new ArrayList<String>();
            myRequestsTable               = new Hashtable<String, Result>();
            myParentsTable                = new Hashtable<String, ArrayList<String>>();

            /*
             * This query returns multiple resultsets and update counts.
             * The format of the output is as follows:
             *
             * 1. UPDATE_COUNT due to insertion of matched requests into @tmp
             * 2. RESULTSET returning the count of actually matched requests.
             * 3. UPDATE_COUNT due to insertion of current page into @tmp1
             * 4. RESULT_SET returning the sys_id and request_id of the results.
             * 5. series of UPDATE_COUNTs due to processing hierarchical
             *    requests.
             * 6. RESULT_SET of all requests(matched results and their unmatched
             *    parents).
             * 7. RESULT_SET of List of parents with unmatched children.
             * 8. RESULT_SETs based on the presence of user fields, extended
             *    fields in the display header.
             */

            // 1. Ignore the update count due to insertion into @tmp and move.
            while ((cs.getMoreResults() == false)) {

//            	System.out.println("Skipping the update counts");
            }

            // 2. Read the actual result count.
            ResultSet rs = cs.getResultSet();
//            printResultSetMetaData(rs, "ResultCount");
            if ((rs != null) && (rs.next() != false)) {
                myTotalResultCount = rs.getInt("result_count");
            }

            while ((cs.getMoreResults() == false)) {

//            	System.out.println("Skipping the update counts");
            }

          
            ResultSet tableKeyResultSet = cs.getResultSet();
//            printResultSetMetaData(tableKeyResultSet, "All Results : ");
            readTableKeys(tableKeyResultSet, myResultIdList, myResultIdListByBA);
            myRetrievedResultCount = myResultIdList.size();
            
            // Skip all the update counts due to hierarchical request filtering.
            while ((cs.getMoreResults() == false)) {
//            	System.out.println("Skipping the update counts");
            }


            // 6. Current result set returns all sys_id and request_ids. // Nitiraj msg : This is wrong it receives ( sys_id, request_id and parent_request_id )
            ResultSet requestSysIdRs = cs.getResultSet();
//            printResultSetMetaData(requestSysIdRs, "Parent Relations not handled");
            readTableKeys(requestSysIdRs, myAllRequestIdList, myAllRequestIdListByBA);

            // 7. Next one is Parents with unmatched children.
            while ((cs.getMoreResults() == false)) {

//            	System.out.println("Skipping the update counts");
            }


            ResultSet rsIncomplete = cs.getResultSet();
//            printResultSetMetaData(rsIncomplete, "Incomplete Trees");
            if (rsIncomplete != null) {
                while (rsIncomplete.next() != false) {
                    int    systemId = rsIncomplete.getInt("sys_id");
                    int    parentId = rsIncomplete.getInt("parent_request_id");
                    String key      = formTableKey(systemId, parentId);

                    myRequestsWithIncompleteTrees.add(key);
                }
            }
        // Move next.
            while ((cs.getMoreResults() == false)) {

//            	System.out.println("Skipping the update counts");
            }
 

            Hashtable<String, Hashtable<String, Object>> extValues  = new Hashtable<String, Hashtable<String, Object>>();
            Hashtable<String, ArrayList<String>>         userValues = new Hashtable<String, ArrayList<String>>();

            if (myHasExtFields == true) {

                // The current result set contains request ex records.
                ResultSet rsExt = cs.getResultSet();
//                printResultSetMetaData(rsExt, "Extended fields");
                processExtFields(rsExt, extValues);

                while ((cs.getMoreResults() == false)) {

//                    	System.out.println("Skipping the update counts");
                }
            }

           
            
            if (myHasUserFields == true) 
            {
                // The current result set contains request user records.
                ResultSet rsUser = cs.getResultSet();
//            	printResultSetMetaData(rsUser, "Users");
                processUsers(rsUser, userValues);
                
                // Skip all the update counts due to  request level permission filtering.
                while ((cs.getMoreResults() == false)) {
                	System.out.println("Skipping the update counts");
                }

            }
            
            // The current result set contains request records.
          
            ResultSet rsReq = cs.getResultSet();
//            printResultSetMetaData(rsReq, "RequestParams");
            processRequests(rsReq, myHasUserFields, myHasExtFields, userValues, extValues);
            cs.close();
            cs = null;
        } catch (SQLException sqle) {
            StringBuilder message = new StringBuilder();

            message.append("An exception occurred while retrieving the search ").append("results.\n");

            throw new DatabaseException(message.toString(), sqle);
        } finally {
            try {
                if (con != null) {
                    con.close();
                }
            } catch (SQLException sqle) {
                LOG.warn("Exception while closing the connection\n." + "",(sqle));
            }
        }
    }

    /**
     * This method executes the query and retrieves the results.
     *
     * @exception DatabaseException
     */
    private void executeTreeQuery() throws DatabaseException {
        Connection con = null;

        try {
            con = DataSourcePool.getConnection();

            Statement cs   = con.createStatement();
            LOG.info("Executing database query : " + myDatabaseQuery);
            boolean   flag = cs.execute(myDatabaseQuery);

            /*
             * int count = cs.getUpdateCount();
             * LOG.info("Count: " + count + " Flag: " + flag);
             * while ((flag == false && count == -1) == false)
             * {
             *   flag = cs.getMoreResults();
             *   count = cs.getUpdateCount();
             *   LOG.info("Count: " + count + " Flag: " + flag);
             * }
             */

            /*
             * This query returns multiple resultsets and update counts.
             * The format of the output is as follows:
             *
             * 1. series of UPDATE_COUNTs due to processing hierarchical
             *    requests.
             * 2. RESULT_SET that returns the count of requests.
             * 3. RESULT_SET returning the sys_id and request_id of the results.
             * 4. RESULT_SETs based on the presence of user fields, extended
             *    fields in the display header.
             */

            // 1. Skip the update counts due to hierarchical request filtering.
            while ((cs.getMoreResults() == false) && (cs.getUpdateCount() != -1)) {

                // Skip them...
            }

            // 2. Read the actual result count.
            ResultSet rs = cs.getResultSet();

            if ((rs != null) && (rs.next() != false)) {
                myTotalResultCount = rs.getInt("result_count");
            }

            // Move next.
            flag = cs.getMoreResults();

            /*
             * 3.
             * This one is a result set returning the sys_id and request of
             * matched results.
             */
            if (flag == true) {
                ResultSet rsReq = cs.getResultSet();

                readTableKeys(rsReq, myResultIdList, myResultIdListByBA);
                myAllRequestIdList.addAll(myResultIdList);
                myAllRequestIdListByBA = myResultIdListByBA;
                myRetrievedResultCount = myResultIdList.size();
            }

            // Move Next.
            flag = cs.getMoreResults();

            Hashtable<String, Hashtable<String, Object>> extValues  = new Hashtable<String, Hashtable<String, Object>>();
            Hashtable<String, ArrayList<String>>         userValues = new Hashtable<String, ArrayList<String>>();

            if (myHasExtFields == true) {

                // The current result set contains request ex records.
                ResultSet rsExt = cs.getResultSet();

                processExtFields(rsExt, extValues);

                // Move ahead.
                cs.getMoreResults();
            }

            if (myHasUserFields == true) {

                // The current result set contains request user records.
                ResultSet rsUser = cs.getResultSet();

                processUsers(rsUser, userValues);

                // Move ahead.
                cs.getMoreResults();
            }

            // The current result set contains request records.
            ResultSet rsReq = cs.getResultSet();

            processRequests(rsReq, myHasUserFields, myHasExtFields, userValues, extValues);
            cs.close();
            cs = null;
        } catch (SQLException sqle) {
            StringBuilder message = new StringBuilder();

            message.append("An exception occurred while retrieving the search ").append("results.\n");
            LOG.info(message.toString() + "\n" + "",(sqle));

            throw new DatabaseException(message.toString(), sqle);
        } finally {
            try {
                if (con != null) {
                    con.close();
                }
            } catch (SQLException sqle) {
                LOG.warn("Exception while closing the connection\n." + "",(sqle));
            }
        }
    }

    /**
     * This method filters the entries from the parse table based on the
     * following two conditions.
     * <UL>
     *     <LI>Field is not searchable.</LI>
     *     <LI>User is not authorized to view the field.</LI>
     * </UL>
     *
     */
    private void filterParseTable() {
        ArrayList<ParseEntry> ptable = new ArrayList<ParseEntry>();

        for (ParseEntry pe : myParseTable) {
            if ((pe.getEntryType() == ParseEntryType.NESTING_OPEN) || (pe.getEntryType() == ParseEntryType.NESTING_CLOSE)) {
                ptable.add(pe);

                continue;
            }

            Field  field      = null;
            String descriptor = pe.getDescriptor();

            // Check if the descriptor is "tree"
            if (descriptor.equals("tree")) {
                myHasTreeQuery = true;

                /*
                 * When the descriptor "tree" is present in the parse table,
                 * Check if the argument list has exactly two entries.
                 * Even if not, we generally ignore every thing else.
                 * And perform the following steps.
                 *      1. empty the temporary parse table
                 *      2. re-initialize it.
                 *      3. Add the treeEntry to it
                 *      4. Get out of this for loop.
                 * If the arglist is either empty or has more than two entries,
                 * we simply ignore this tree entry completely.
                 */
                ArrayList<String> argList = pe.getArgList();

                if ((argList == null) || (argList.size() != 2)) {
                    continue;
                }

                ptable = null;
                ptable = new ArrayList<ParseEntry>();
                ptable.add(pe);
                myTreeId = Integer.parseInt(argList.get(1));

                break;
            }

            /*
             * In case the descriptor is "has" or "is", then it should be
             * handled separately.
             */
            if (descriptor.equals("has") || descriptor.equals("is")) {
                ArrayList<String> argList = pe.getArgList();

                if (argList == null) {
                    continue;
                }

                /*
                 * The first element in the argument list should be the name
                 * of a field.
                 */
                String arg   = argList.get(0);
                String fname = myFDtoName.get(arg);

                if (fname == null) {
                    continue;
                }

                // Get the corresponding field object.
                field = myFieldsTable.get(fname);

                if (field == null) {
                    continue;
                }

                ArrayList<String> hasArgList = new ArrayList<String>();

                /*
                 * Based on the data type of the field, the parse entry varies.
                 */
                int datatype = field.getDataTypeId();

                switch (datatype) {
                case BOOLEAN :
                    hasArgList.add("0");

                    break;

                case DATE :
                case TIME :
                case DATETIME :
                    break;

                case INT :
                    if (fname.equals(Field.PARENT_REQUEST_ID)) {
                        if (descriptor.equals("is")) {
                            hasArgList.add("-1");
                        } else {
                            hasArgList.add("0");
                        }
                    } else {
                        hasArgList.add("0");
                    }

                    break;

                case REAL :
                    hasArgList.add("0");

                    break;

                case STRING :
                case TEXT :
                case TYPE :
                case USERTYPE :
                    break;
                }

                /*
                 * Finally, prepare the parse entry and insert into the table.
                 */
                ParseEntry entry = new ParseEntry();

                entry.setEntryType(ParseEntryType.DATA);
                entry.setDescriptor(fname);
                entry.setFieldName(fname);
                entry.setDepth(pe.getDepth());

                Connective connective = pe.getConnective();

                connective = negateConnective(connective);
                entry.setConnective(connective);
                entry.setArgList(hasArgList);
                ptable.add(entry);

                continue;
            } else if (descriptor.equals("alltext") || descriptor.equals("all")) {
                myHasAllTextField = true;
                myHasTextField = true ;
                /*
                 * Map the field name of this entry to Field.DESCRIPTION.
                 * But retain the descriptor as is since it is different from
                 * description field in lucene.
                 */
                pe.setFieldName(Field.DESCRIPTION);
                ptable.add(pe);

                continue;
            } else if ((descriptor.equals("actionuser") == true) || (descriptor.equals("appender") == true)) {
                myHasAppender = true;
                ptable.add(pe);

                continue;
            } else if (descriptor.equals("closedby") == true) {
                myHasCloseInfo = true;
                ptable.add(pe);

                continue;
            } else if ((descriptor.equals("appenddate") == true) || (descriptor.equals("dateappended") == true)) {
                myHasAppendDate = true;
                ptable.add(pe);

                continue;
            } else if ((descriptor.equals("dateclosed") == true) || (descriptor.equals("closeddate") == true)) {
                myHasCloseInfo = true;
                ptable.add(pe);

                continue;
            } else if (descriptor.equals("read")) {
                pe.setFieldName(READ_REQUEST);
                ptable.add(pe);

                continue;
            } else if (descriptor.equals("unread")) {
                pe.setFieldName(UNREAD_REQUEST);
                ptable.add(pe);

                continue;
            }

            String fieldName = pe.getFieldName();

            field = myFieldsTable.get(fieldName);

            /*
             * Skip the descriptors which do not have any associated field.
             */
            if (field == null) {
                LOG.info("Field object not found for this descriptor: " + fieldName);

                continue;
            }

            int permission = field.getPermission();

            /*
             * Check if this field is a searchable field.
             */
//            if ((permission & Permission.SEARCH) == 0) {
//                LOG.info("Search is not allowed on this field: " + fieldName);
//
//                continue;
//            }

            /*
             * If this is an extended field, check if the user has permissions
             * to perform search over it.
             */
            if (field.getIsExtended() == true) {
                Integer temp = myPermissionTable.get(field.getName());

                if (temp == null) {
                    LOG.info("User is not authorized to view this field: " + fieldName);

                    continue;
                }

                permission = temp.intValue();

                if ((permission & Permission.VIEW) == 0) {
                    LOG.info("User is not authorized to view this field: " + fieldName);

                    continue;
                }
            }

            ptable.add(pe);

            /*
             * Check if this is a text field.
             */
            int dataType = field.getDataTypeId();

            if (dataType == TEXT) {
                myHasTextField = true;
            }
        }

        myParseTable = null;
        myParseTable = ptable;
        removeRedundantParens();

        return;
    }

    /**
     *
     * @throws DatabaseException
     */
    private void formMyRequestsQuery() throws DatabaseException {

        /*
         * Steps followed while forming the query to get MyRequests.
         *  1. Get Declarations and get the values for these variables.
         *  2. Create the temporary table to hold the Ids of requests that
         *     matched the criteria. Name this temporary table as @tmp.
         *  3. Get the filtering query.
         *  4. Print the Number of results that matched the criteria.
         *  5. Create the temporary table to hold the Ids of requests that
         *     fall in the requested page. Name this temporary table as
         *     @tmp1.
         *  6. Get the SQL that extracts Ids present in the requested page.
         *  7. Print the request ids returned in the above step.
         *  8. Create the temporary table to hold the Ids of unmatched
         *     parents of matched requests. Name this temporary table as
         *     @mptmp1.
         *  9. Get the SQL that gets the list of unmatched parents and grand
         *     parents of all matched requests.
         * 10. Create a temporary table to hold the ids of unmatched parents
         *     of matched results and ids of matched requests sorted on the
         *     requested field in the requested order. Name this table as
         *     #rtmp.
         * 12. Insert the ids of matched results and unmatched parents into
         *     this temporary table, #rtmp.
         * 13. SELECT union of ids from @tmp1 and @mptmp1 order by the
         *     sortfield in requested order.
         * 14. Selection query from #rtmp table.
         */
        StringBuilder sortCreate = new StringBuilder();
        StringBuilder sortSelect = new StringBuilder();
        StringBuilder sortJoin   = new StringBuilder();
        StringBuilder sortStmt   = new StringBuilder();

        mySortField = Field.REQUEST;
        mySortOrder = DESC_ORDER;

        // Get the sorting related SQL.
        getSortingQuery(sortCreate, sortSelect, sortJoin, sortStmt);

        String        sCreate = sortCreate.toString();
        String        sSelect = sortSelect.toString();
        String        sJoin   = sortJoin.toString();
        String        sStmt   = sortStmt.toString();
        StringBuilder dbQuery = new StringBuilder();

        // Step 1
        dbQuery.append(getDeclarations()).append(declareTempTable("@tmp1", sCreate)).append(myFilteringQuery).append(sqlMatchedRequests()).append(declareTempTable("@mptmp1",
                sCreate)).append(declareTempTable("@mptmp2", sCreate)).append(declareTempTable("@mptmp3", sCreate)).append(getUnmatchedParents(sSelect, sJoin)).append(createTmpTable("#rtmp",
                    sCreate)).append(getAllRequestsList(mySortField, mySortOrder)).append(getSQLForRequestsWithIncompleteTrees()).append(getSelectionQuery(sSelect, sJoin, sStmt, "#rtmp",
                        false)).append(dropTmpTable("#rtmp")).append("\n");
        myDatabaseQuery = dbQuery.toString();

        return;
    }

    /**
     *
     * @throws DatabaseException
     */
    private void formSearchQuery() throws DatabaseException {

        /*
         * Steps in the database query.
         *  1. Get Declarations and get the values for these variables.
         *  2. Create the temporary table to hold the Ids of requests that
         *     matched the criteria. Name this temporary table as @tmp.
         *  3. Get the filtering query.
         *  4. Print the Number of results that matched the criteria.
         *  5. Create the temporary table to hold the Ids of requests that
         *     fall in the requested page. Name this temporary table as
         *     @tmp1.
         *  6. Get the SQL that extracts Ids present in the requested page.
         *  7. Print the request ids returned in the above step.
         *  8. Create the temporary table to hold the Ids of unmatched
         *     parents of matched requests. Name this temporary table as
         *     @mptmp1.
         *  9. Get the SQL that gets the list of unmatched parents and grand
         *     parents of all matched requests.
         * 10. Create a temporary table to hold the ids of unmatched parents
         *     of matched results and ids of matched requests sorted on the
         *     requested field in the requested order. Name this table as
         *     #rtmp.
         * 12. Insert the ids of matched results and unmatched parents into
         *     this temporary table, #rtmp.
         * 13. SELECT union of ids from @tmp1 and @mptmp1 order by the
         *     sortfield in requested order.
         * 14. Selection query from #rtmp table.
         */
        StringBuilder sortCreate = new StringBuilder();
        StringBuilder sortSelect = new StringBuilder();
        StringBuilder sortJoin   = new StringBuilder();
        StringBuilder sortStmt   = new StringBuilder();

        // Get the sorting related SQL.
        getSortingQuery(sortCreate, sortSelect, sortJoin, sortStmt);

        String sCreate = sortCreate.toString();
        String sSelect = sortSelect.toString();
        String sJoin   = sortJoin.toString();
        String sStmt   = sortStmt.toString();

        // Get the filtering part of the database query.
        myFilteringQuery = getFilteringQuery(sSelect, sJoin, sStmt);

        StringBuilder dbQuery = new StringBuilder();

        // Step 1
        dbQuery.append(getDeclarations())
        .append(declareTempTable("@tmp", sCreate))
        .append(myFilteringQuery)
        .append(getResultCountSQL())
        .append(declareTempTable("@tmp1", sCreate))
        .append(getPagingSQL())
        .append(sqlMatchedRequests())
        .append(declareTempTable("@mptmp1", sCreate))
        .append(declareTempTable("@mptmp2", sCreate))
        .append(declareTempTable("@mptmp3", sCreate))
        .append(getUnmatchedParents(sSelect, sJoin))
        .append(createTmpTable("#rtmp", sCreate))
        .append(getAllRequestsList(mySortField, mySortOrder))
        .append(getSQLForRequestsWithIncompleteTrees())
        .append(getSelectionQuery(sSelect, sJoin, sStmt, "#rtmp", true))
        .append(dropTmpTable("#rtmp"))
        .append("\n").append("\n");
        myDatabaseQuery = dbQuery.toString();
        //System.out.println("\n***    \n "+myDatabaseQuery+"   *****\n");
       // System.out.println("\nMY filtering Query :\n"+myFilteringQuery+"\n");
        return;
    }

    private String searchQueryPermissionFilter() {
		StringBuffer sb= new StringBuffer();
		sb.append("\n CREATE TABLE #resultSet \n"+
		"( \n"+
		"	mailListId INT \n"+
		") \n"+
		" \n"+
		"/* \n"+
		" * Get the mailing lists where the user is a direct member. \n"+
		" */ \n"+
		"INSERT INTO #resultSet(mailListId) \n"+
		"select distinct \n"+
		"	mail_list_id  \n"+
		"from  \n"+
		"	mail_list_users  \n"+
		"where  \n"+
		"	user_id ="+myUserId+"\n"+
		" \n"+
		" \n"+
		"create table #iterationSet \n"+
		"( \n"+
		"	mailListId int \n"+
		") \n"+
		"insert INTO #iterationSet select * FROM #resultSet \n"+
		" \n"+
		"create table #iterationElement \n"+
		"( \n"+
		"	mailListId int \n"+
		") \n"+
		" \n"+
		"create table #iterationResult \n"+
		"( \n"+
		"	mailListId int \n"+
		") \n"+
		" \n"+
		"WHILE (EXISTS(SELECT * FROM #iterationSet)) \n"+
		"BEGIN \n"+
		"	insert into #iterationElement select top 1 * from #iterationSet  \n"+
		" \n"+
		"	insert into #iterationResult select mail_list_users.mail_list_id \n"+
		"		FROM \n"+
		"			mail_list_users \n"+
		"			JOIN #iterationElement \n"+
		"			ON mail_list_users.user_id = #iterationElement.mailListId			 \n"+
		"			LEFT JOIN #resultSet  \n"+
		"			ON mail_list_users.mail_list_id = #resultSet.mailListId \n"+
		"		WHERE \n"+
		"			#resultSet.mailListId IS NULL \n"+
		" \n"+
		"	insert INTO #iterationSet select * FROM #iterationResult	 \n"+
		" \n"+
		"	insert INTO #resultSet select * FROM #iterationResult \n"+
		" \n"+
		"	DELETE from #iterationSet \n"+
		"	where #iterationSet.mailListId in ( select #iterationElement.mailListId from #iterationElement ) -- join #iterationSet on #iterationElement.mailListId=#iterationSet.mailListId ) \n"+
		" \n"+
		"	DELETE FROM #iterationElement \n"+
		"	DELETE FROM #iterationResult \n"+
		"	 \n"+
		"END \n"+
		" \n"+
		"DROP TABLE #iterationSet \n"+
		"drop table #iterationElement \n"+
		"drop table #iterationResult \n"+
		" \n"+
		"/* permissions by static roles in the system*/ \n"+
		"SELECT \n"+
		"	CASE SUM(p.padd) \n"+
		"	WHEN 0 then 0 \n"+
		"	ELSE 1 \n"+
		"	END +  \n"+
		"	CASE SUM(p.pchange) \n"+
		"	WHEN 0 then 0 \n"+
		"	ELSE 2 \n"+
		"	END +  \n"+
		"	CASE SUM(p.pview) \n"+
		"	WHEN 0 then 0 \n"+
		"	ELSE 4 \n"+
		"	END +  \n"+
		"	case sum( p.pEmailView ) \n"+
		"	when 0 then 0  \n"+
		"	else 8  \n"+
		"	end 'gpermissions', \n"+
		"	t.name 'name', \n"+
		"	t.field_id 'field_id' \n"+
		"    into #statpermtab \n"+
		"FROM \n"+
		"	permissions p \n"+
		"	JOIN \n"+
		"	( \n"+
		"	/* \n"+
		"	 * Get the permissions the user gets by virtue of being a user of the system. \n"+
		"	 */ \n"+
		"	SELECT \n"+
		"		f.name, \n"+
		"		f.field_id, \n"+
		"		rp.gpermissions \n"+
		"	FROM \n"+
		"		roles_permissions rp \n"+
		"		JOIN fields f \n"+
		"		ON rp.sys_id = f.sys_id AND rp.field_id = f.field_id AND (f.name='request_id' or f.name='is_private') \n"+
		"	WHERE \n"+
		"		rp.sys_id = "+mySystemId+" AND  \n"+
		"		rp.role_id = 1 \n"+
		"	 \n"+
		"	UNION \n"+
		"	 \n"+
		"	/* \n"+
		"	 * Get the permissions the user gets by virtue of being a part of the BA. \n"+
		"	 */ \n"+
		"	SELECT \n"+
		"		f.name, \n"+
		"		f.field_id, \n"+
		"		rp.gpermissions \n"+
		"	FROM \n"+
		"		roles_permissions rp \n"+
		"		JOIN fields f \n"+
		"		ON rp.sys_id = f.sys_id AND rp.field_id = f.field_id AND (f.name='request_id' or f.name='is_private') \n"+
		"		JOIN roles_users ru \n"+
		"		ON ru.sys_id = rp.sys_id AND ru.role_id = rp.role_id \n"+
		"	WHERE \n"+
		"		rp.sys_id = "+mySystemId+" AND \n"+
		"		( \n"+
		"			ru.user_id = "+myUserId+" OR \n"+
		"			ru.user_id IN (SELECT mailListId FROM #resultSet) \n"+
		"		) \n"+
		"	) t \n"+
		"	ON p.permission = t.gpermissions  \n"+
		"GROUP BY t.name, t.field_id \n"+
		" \n"+
		"SELECT  \n"+
		"		ru.request_id, \n"+
		"        rp.gpermissions, \n"+
		"        f.name, \n"+
		"        f.field_id \n"+
		"        into #temppermtab \n"+
		"       \n"+
		"	FROM			 \n"+
		"		roles_permissions rp	 \n"+
		"		JOIN fields f \n"+
		"   		ON rp.sys_id = f.sys_id AND rp.field_id = f.field_id and (f.name='request_id' or f.name='is_private') and rp.sys_id=1 \n"+
		"		JOIN roles r \n"+
		"        ON rp.sys_id = r.sys_id and rp.role_id=r.role_id and r.sys_id=1 \n"+
		"		JOIN request_users ru \n"+
		"        ON ru.sys_id = "+mySystemId+" and   \n"+
		"           ru.field_id = r.field_id and \n"+
		"           (ru.user_id = "+myUserId+" or ru.user_id IN (SELECT mailListId FROM #resultSet)) and  \n"+
		"           ru.request_id in ( select request_id from #reqtab ) 		 \n"+
		"	   \n"+
		" \n"+
		"      select   \n"+
		"            tmp.gpermissions, \n"+
		"            tmp.name, \n"+
		"            tmp.field_id, \n"+
		"            rb.request_id, \n"+
		"            rb.is_private   \n"+
		"      into #dynpermtab  \n"+
		"      from  \n"+
		"      #reqtab rb \n"+
		"      join #temppermtab tmp \n"+
		"      on rb.request_id=tmp.request_id \n"+
		"      order by request_id \n"+
		" \n"+
		" \n"+
		"Create table #finalpermtab \n"+
		" ( \n"+
		"    request_id int, \n"+
		"    name   nvarchar(50), \n"+
		"    field_id int, \n"+
		"    permission int , \n"+
		"    is_private int \n"+
		" ) \n"+
		" \n"+
		"declare @reqid int \n"+
		"declare @isprivate int \n"+
		" select distinct request_id into #reqitr from #dynpermtab \n"+
		" WHILE(EXISTS(select * from #reqitr)) \n"+
		"     Begin \n"+
		"             select @reqid = MIN(request_id) from #reqitr \n"+
		"             select @isprivate = is_private from #dynpermtab where request_id=@reqid and name='is_private' \n"+
		"             if @isprivate=1 \n"+
		"              Begin \n"+
		"               insert into #finalpermtab \n"+
		"               SELECT \n"+
		"                 @reqid 'request_id', \n"+
		"                 t.name 'name', \n"+
		"                 t.field_id 'field_id',   \n"+
		"			    	CASE SUM(p.pview) \n"+
		"				    WHEN 0 then 0 \n"+
		"				    ELSE 1 \n"+
		"				    END \n"+
		"				'permission', \n"+
		"                @isprivate 'is_private' \n"+
		"				 \n"+
		"				 FROM permissions p \n"+
		"                join \n"+
		"                ( \n"+
		"                 select  \n"+
		"                       gpermissions, \n"+
		"                       name, \n"+
		"                       field_id  \n"+
		"                 from #dynpermtab  \n"+
		"                 where request_id = @reqid \n"+
		"                UNION \n"+
		"                  select * from #statpermtab \n"+
		"                 )t \n"+
		"                 on p.permission=t.gpermissions \n"+
		"                 GROUP BY t.name, t.field_id \n"+
		"                 having t.name='is_private' \n"+
		"               \n"+
		"           end \n"+
		"           else \n"+
		"                begin \n"+
		"                insert into #finalpermtab   \n"+
		"                SELECT \n"+
		"                @reqid 'request_id', \n"+
		"                t.name 'name', \n"+
		"                t.field_id 'field_id',   \n"+
		"				CASE SUM(p.pview) \n"+
		"				WHEN 0 then 0 \n"+
		"				ELSE 1 \n"+
		"				END \n"+
		"				'permission', \n"+
		"                @isprivate 'is_private' \n"+
		"				 \n"+
		"				            \n"+
		" \n"+
		"			FROM permissions p \n"+
		"                join \n"+
		"                ( \n"+
		"                 select  \n"+
		"                       gpermissions, \n"+
		"                       name, \n"+
		"                       field_id  \n"+
		"                 from #dynpermtab  \n"+
		"                 where request_id = @reqid \n"+
		"                UNION \n"+
		"                  select * from #statpermtab \n"+
		"                 )t \n"+
		"                 on p.permission=t.gpermissions \n"+
		"                 GROUP BY t.name, t.field_id \n"+
		"                 having t.name='request_id'    \n"+
		"              end \n"+
		"delete from #reqitr where request_id=@reqid \n"+
		"    \n"+
		"End    \n"+
		"  select distinct request_id into #reqids from #dynpermtab \n"+
		"  \n"+
		"select r.* from #reqtab r \n"+
		"  join #finalpermtab fpt \n"+
		"   on r.request_id = fpt.request_id and r.request_id in (select * from #reqids) \n"+
		"    where fpt.permission >0 \n"+
		"  \n"+
		" drop table #reqids \n"+
		" drop table #reqtab \n"+
		" drop table #temppermtab \n"+
		" drop table #dynpermtab \n"+
		" drop table #statpermtab \n"+
		" drop table #reqitr \n"+
		" drop table #resultSet \n"+
		" drop table #finalpermtab \n");

		return sb.toString();
	}

	//filters the requests where user has view permissions in search results 
//    private String requestlevelPermissioncheck(){
//    	StringBuilder  filterquery = new StringBuilder();
//    	filterquery.append
//    	
//    	
//    	return filterquery.toString();
//    	
//    	
//    }
    
    private String dropTmpTable(String tableName) {
		// TODO Auto-generated method stub
		return "\ndrop table " + tableName + "\n";
	}

	/**
     *
     * @param systemId
     * @param requestId
     *
     * @return Key formed from systemid and requestid.
     */
    public static String formTableKey(int systemId, int requestId) {
        return systemId + "_" + requestId;
    }

    /**
     *
     * @param systemId
     * @param treeId
     * @throws DatabaseException
     */
    private void formTreeQuery(int systemId, int treeId) throws DatabaseException {
        StringBuffer  query      = new StringBuffer();
        StringBuilder sortCreate = new StringBuilder();
        StringBuilder sortSelect = new StringBuilder();
        StringBuilder sortJoin   = new StringBuilder();
        StringBuilder sortStmt   = new StringBuilder();

        // Get the sorting related SQL.
        getSortingQuery(sortCreate, sortSelect, sortJoin, sortStmt);

        String sCreate = sortCreate.toString();
        String sSelect = sortSelect.toString();
        String sJoin   = sortJoin.toString();
        String sStmt   = sortStmt.toString();

        query.append("\nDECLARE @systemId INT").append("\nDECLARE @requestId INT").append("\n").append("\nSELECT @systemId = ").append(systemId).append("\nSELECT @requestId = ").append(treeId).append(
            "\n").append("\nDECLARE @treetmp1 TABLE").append("\n(").append("\n\tsys_id            INT,").append("\n\trequest_id        INT,").append("\n\tparent_request_id INT").append("\n)").append(
            "\n").append("\nDECLARE @treetmp2 TABLE").append("\n(").append("\n\tsys_id            INT,").append("\n\trequest_id        INT,").append("\n\tparent_request_id INT").append("\n)").append(
            "\n").append("\nDECLARE @treetmp3 TABLE").append("\n(").append("\n\tsys_id            INT,").append("\n\trequest_id        INT,").append("\n\tparent_request_id INT").append("\n)").append(
            "\n").append("\nINSERT INTO @treetmp1 ").append("\nSELECT").append("\n\tsys_id,").append("\n\trequest_id,").append("\n\tparent_request_id").append("\nFROM").append("\n\trequests").append(
            "\nWHERE\n").append("\n\tsys_id = @systemId AND").append("\n\trequest_id = @requestId").append("\n").append("\nINSERT INTO @treetmp2 SELECT * FROM @treetmp1").append(
            "\nWHILE (EXISTS ( SELECT * FROM @treetmp2 ) )").append("\nBEGIN").append("\n\tINSERT INTO @treetmp3").append("\n\tSELECT DISTINCT").append("\n\t\tr.sys_id,").append(
            "\n\t\tr.request_id,").append("\n\t\tr.parent_request_id").append("\n\tFROM").append("\n\t\trequests r").append("\n\t\tJOIN @treetmp2 res").append(
            "\n\t\tON r.sys_id = res.sys_id AND ").append("\n\t(").append("\n\t\tr.parent_request_id = res.request_id -- Child").append("\n\t\tOR").append(
            "\n\t\tr.request_id = res.parent_request_id -- Parent").append("\n\t)").append("\n\tLEFT JOIN @treetmp1 t").append("\n\t\tON r.sys_id = t.sys_id AND ").append(
            "r.request_id = t.request_id ").append("\n\tWHERE").append("\n\t\tt.sys_id IS NULL ").append("\n").append("\n\tINSERT INTO @Treetmp1 SELECT * FROM @treetmp3").append(
            "\n\tDELETE @treetmp2").append("\n\tINSERT INTO @treetmp2 SELECT * FROM @treetmp3").append("\n\tDELETE @treetmp3").append("\nEND").append("\n").append("\nDECLARE @tmp TABLE ").append(
            "\n(").append("\n\tsys_id INT,").append("\n\trequest_id INT,").append("\n\tparent_request_id INT").append(sCreate).append("\n)").append("\n");

        StringBuilder wherePart = new StringBuilder();
        StringBuilder joinPart  = new StringBuilder();

        joinPart.append("\n\trequests r").append("\n\tJOIN @treetmp1 rt").append("\n\tON r.sys_id = rt.sys_id AND ").append("r.request_id = rt.request_id");
        getStandardJoins(joinPart);
        getConfidentialPart(wherePart, joinPart);
        query.append("\nINSERT INTO @tmp").append("\nSELECT DISTINCT ").append("\n\tr.sys_id,").append("\n\tr.request_id,").append("\n\tr.parent_request_id").append(sortSelect).append(
            "\nFROM").append(joinPart.toString()).append(sortJoin).append("\nWHERE").append("\n\tr.sys_id = ").append(mySystemId).append(wherePart.toString()).append(sortStmt).append("\n").append(
            "\nSELECT @@ROWCOUNT 'result_count'").append("\n").append("\nSELECT sys_id, request_id FROM @tmp").append("\n").append(getSelectionQuery(sSelect, sJoin, sStmt, "@tmp", true)).append(
            "\n").append("\n");
        myDatabaseQuery = query.toString();
    }

    /*
     * END: Getters and Setters for the input/output variables.
     *
     */

    /**
     *
     */
    public void init() {
        myTextEntries                 = new ArrayList<ParseEntry>();
        myResultList                  = new ArrayList<Result>();
        myResultIdList                = new ArrayList<String>();
        myParentsTable                = new Hashtable<String, ArrayList<String>>();
        myRequestsTable               = new Hashtable<String, Result>();
        myResultListByBA              = new Hashtable<Integer, ArrayList<Result>>();
        myAllRequestIdList            = new ArrayList<String>();
        myResultIdListByBA            = new Hashtable<Integer, ArrayList<String>>();
        myAllRequestIdListByBA        = new Hashtable<Integer, ArrayList<String>>();
        myRequestsWithIncompleteTrees = new ArrayList<String>();
    }

    /**
     * This method obtains the fields table for this BA.
     *
     * @exception DatabaseException
     */
    private void initializeBAFields() throws DatabaseException {

        // Get the business area object.
        myBusinessArea = BusinessArea.lookupBySystemId(mySystemId);
        mySystemPrefix = myBusinessArea.getSystemPrefix();

        // Get the fields list.
        myFieldsTable = Field.getFieldsTableBySystemId(mySystemId);
    }

    /**
     * This method obtains the permissions table for this user in this BA.
     *
     * @exception DatabaseException
     */
    private void initializeUserPermissionTable() throws DatabaseException {

        // Get the table of permissions the user has.
        if (myPermissionTable == null) {
            myPermissionTable = RolePermission.getPermissionsBySystemIdAndUserId(mySystemId, myUserId);
        }

        // Get the level of permission this user has on Confidential field.
        Integer temp = myPermissionTable.get(Field.IS_PRIVATE);

        if (temp != null) {
            int perm = temp;

            if ((perm & Permission.VIEW) > 0) {

                //
                // User himself has view permission on private by virtue
                // of his association with the Business Area.
                //
                myPrivatePerm = SELF_PRIVATE;
            } else {
                temp = myPermissionTable.get(KEY_LOGGER_PRIVATE);

                if ((temp != null) && ((temp & Permission.VIEW) != 0)) {

                    //
                    // User get see private requests only if he happens to be
                    // the logger of the request.
                    //
                    myPrivatePerm   = ROLE_PRIVATE;
                    myRolePermLevel = myRolePermLevel | LOGGER_PRIVATE;
                }

                temp = myPermissionTable.get(KEY_ASSIGNEE_PRIVATE);

                if ((temp != null) && ((temp & Permission.VIEW) != 0)) {

                    //
                    // User get see private requests only if he happens to be
                    // the logger of the request.
                    //
                    myPrivatePerm   = ROLE_PRIVATE;
                    myRolePermLevel = myRolePermLevel | ASSIGNEE_PRIVATE;
                }

                temp = myPermissionTable.get(KEY_SUBSCRIBER_PRIVATE);

                if ((temp != null) && ((temp & Permission.VIEW) != 0)) {

                    //
                    // User get see private requests only if he happens to be
                    // the logger of the request.
                    //
                    myPrivatePerm   = ROLE_PRIVATE;
                    myRolePermLevel = myRolePermLevel | SUBSCRIBER_PRIVATE;
                }
            }
        } else {
            temp = myPermissionTable.get(KEY_LOGGER_PRIVATE);

            if ((temp != null) && ((temp & Permission.VIEW) != 0)) {

                //
                // User get see private requests only if he happens to be
                // the logger of the request.
                //
                myPrivatePerm   = ROLE_PRIVATE;
                myRolePermLevel = myRolePermLevel | LOGGER_PRIVATE;
            }

            temp = myPermissionTable.get(KEY_ASSIGNEE_PRIVATE);

            if ((temp != null) && ((temp & Permission.VIEW) != 0)) {

                //
                // User get see private requests only if he happens to be
                // the logger of the request.
                //
                myPrivatePerm   = ROLE_PRIVATE;
                myRolePermLevel = myRolePermLevel | ASSIGNEE_PRIVATE;
            }

            temp = myPermissionTable.get(KEY_SUBSCRIBER_PRIVATE);

            if ((temp != null) && ((temp & Permission.VIEW) != 0)) {

                //
                // User get see private requests only if he happens to be
                // the logger of the request.
                //
                myPrivatePerm   = ROLE_PRIVATE;
                myRolePermLevel = myRolePermLevel | SUBSCRIBER_PRIVATE;
            }
        }
    }

    /**
     * This is primarily for testing.
     */
    public static void main(String arg[]) throws Exception {
//        if (arg.length < 1) {
//            LOG.info("Usage:\n\t\tSearcher <query>");
//
//            return ;
//        }
//        /////
//        try {
//            String         fileName = arg[0].trim();
//            BufferedReader br       = new BufferedReader(new InputStreamReader(new FileInputStream(fileName)));
//            String         str      = "";
//            int            counter  = 0;
//            
//           // Searcher searcher = null;
//            //searcher = new Searcher(2, 438, str);
//            
//            
//            
//            while ((str = br.readLine()) != null) {
//                counter++;
//                
//                
//                
//                if ((str.trim().equals("") == false) && (str.startsWith("#") == false)) {
//                    LOG.info(counter + ". " + str);
//
//                   // DSQLParser app = new DSQLParser(str, aDescTable, aFDMap);
//                    
//                    Searcher searcher = new Searcher(1, 438, str);
//                    searcher.init();
//                    searcher.initializeBAFields();
//                    searcher.initializeUserPermissionTable();
//                    
//                    /*Hashtable<String, Field> fieldsTable = new Hashtable<String, Field>();
//                    try {
//                        fieldsTable = Field.getFieldsTableBySystemId(1);
//                    } catch (Exception e) {
//                        fieldsTable = new Hashtable<String, Field>();
//                    }
//                    searcher.setFieldsTable(fieldsTable);*/
//                    
//                    try {
//                        searcher.parseQuery();
//                        String strquery = searcher.getLuceneQuery();
//                        String path = "tbits";
//                        ArrayList<String> requestIDs = LuceneSearcher.search(path, strquery,false);
//                        //searcher.getFilteringQuery(sortSelect, sortJoin, sortStmt)
//                        //System.out.println("\n****\n"+requestIDs.toString()+"\n----\n");
//                    } catch (Exception e) {
//                        e.printStackTrace();
//                    }
//
//                    
////                    System.out.println("-----------------------------------" + "-----------------------------------" + "-----------------------------------" + "-----------------------------------"
////                                       + "-----------------------------------");
//                }
//            }
//
//            br.close();
//            br = null;
//        } catch (Exception e) {
//            e.printStackTrace();
//            return ;
//        }
/////
        /*Searcher searcher = null;

        try {
        	arg[0] = "NOT category:pending";
            searcher = new Searcher(2, 438, arg[0]);
            searcher.parse(systemId);
        } catch (DSQLParseException dsqle) {
            LOG.info("",(dsqle));
            return ;
        } finally {
            //transbit.tbits.api.Mapper.stop();
        }*/
    	
    	Searcher searcher = new Searcher(52, 391, "");
    	searcher.search();
    	ArrayList<String> ids = searcher.getAllRequestIdListByBA().get(52);
		if(ids != null){
			for(String id : ids){
				int requestId = Integer.parseInt(id.split("_")[1]);
				UserReadAction check = new UserReadAction();
				check = UserReadAction.lookupBySystemIdAndRequestIdAndUserId(52, requestId, 391);
				System.out.println("RequestId:" + requestId);
			}
		}
    	System.out.println("All requestId list : " + ids);
    	System.out.println("\n\nDatabase query =\n***************\n\n" + searcher.getDatabaseQuery() + "\n\n--------------");
        return ;
    }

    /**
     * This method negates the connective passed.
     *
     * @param c Connective to be negated.
     *
     * @return Negation of the given connective.
     */
    private Connective negateConnective(Connective c) {
        switch (c) {
        case C_AND :
            c = Connective.C_AND_NOT;

            break;

        case C_OR :
            c = Connective.C_OR_NOT;

            break;

        case C_NOT :
            c = Connective.C_NOT_NOT;

            break;

        case C_AND_NOT :
            c = Connective.C_AND;

            break;

        case C_OR_NOT :
            c = Connective.C_OR;

            break;

        case C_NOT_NOT :
            c = Connective.C_NOT;

            break;

        default :
            c = Connective.C_NOT;

            break;
        }

        return c;
    }

    /**
     * This method parses the given query.
     *
     * @throws DatabaseException
     * @throws DSQLParseException
     */
    public void parse() throws DatabaseException, DSQLParseException {
        if (myRequestType != RequestType.QUERY_REQUEST) {
            throw new DSQLParseException("No query is supplied for parsing.");
        }

        // Get the list of fields present in this BA along with BA Record.
        initializeBAFields();

        // Get the permissions table for this user in this BA.
        initializeUserPermissionTable();

        // Fill the parse table if this is a query request.
        if (myRequestType == RequestType.QUERY_REQUEST) {
            parseQuery();
        }
    }

    /**
     * This method parses the query using the DSQLParser and obtains the
     * parse table.
     *
     * @exception DatabaseException
     * @exception DSQLParseException
     */
    private void parseQuery() throws DatabaseException, DSQLParseException {

        // Initialize the descriptor table.
        Hashtable<String, Integer> dTable = new Hashtable<String, Integer>();

        // Get the master descriptor table from the domain object.
        Hashtable<String, Field> masterDescTable = FieldDescriptor.getDescriptorTable(mySystemId);

        // Initialize the descriptor -> fieldname map.
        myFDtoName = new Hashtable<String, String>();

        // Run through the table with keys.
        Enumeration<String> keys = masterDescTable.keys();

        while (keys.hasMoreElements()) {

            //
            // For each field, store the following in the descriptor table.
            // [ descriptor, data type ]
            // [ field_name, data type ]
            // [ display_name, data type ]
            // and place the following in the fdmap.
            // [ descriptor, fieldname ]
            //
            String desc  = keys.nextElement();
            Field  field = masterDescTable.get(desc);

            // [ descriptor, data type ]
            dTable.put(desc, field.getDataTypeId());

            // [ field_name, data type ]
            dTable.put(field.getName(), field.getDataTypeId());

            // [ display_name, data type ]
            dTable.put(field.getDisplayName(), field.getDataTypeId());

            // [ descriptor, fieldname ]
            myFDtoName.put(desc, field.getName());
        }

        /*
         * Custom descriptors
         *      1. tree
         *      2. closedBy
         *      3. appender/actionuser
         *      4. dateclosed/closeddate
         *      5. appenddate/dateappended
         */
        myFDtoName.put("tree", Field.REQUEST);
        dTable.put("tree", INT);
        myFDtoName.put("closedby", Field.USER);
        dTable.put("closedby", USERTYPE);
        myFDtoName.put("appender", Field.USER);
        dTable.put("appender", USERTYPE);
        myFDtoName.put("actionuser", Field.USER);
        dTable.put("actionuser", USERTYPE);
        myFDtoName.put("dateclosed", Field.LASTUPDATED_DATE);
        dTable.put("dateclosed", DATE);
        myFDtoName.put("closeddate", Field.LASTUPDATED_DATE);
        dTable.put("closeddate", DATE);
        myFDtoName.put("appenddate", Field.LASTUPDATED_DATE);
        dTable.put("appenddate", DATE);
        myFDtoName.put("dateappended", Field.LASTUPDATED_DATE);
        dTable.put("dateappended", DATE);

        //
        // Return if the invocation is an exec_type, because we are already
        // provided with a parse table.
        //
        if (myRequestType == RequestType.CUSTOM_FILTER_REQUEST) {
            return;
        }

        // Initialize the DSQLParser object.
        DSQLParser parser = new DSQLParser(myDSQLQuery, dTable, myFDtoName);

        try {
            parser.parse();
        } catch (DSQLParseException de) {
            LOG.info("",(de));

            throw de;
        } finally {

            //
            // Irrespective of whether parsing is successful or not, we want
            // the parse table.
            //
            myParseTable = parser.getParseTable();
           // System.out.println("\n\n\n\n\n"+myParseTable + "\n\n\n\n\n");
            // Check if display header is specified as a part of the query.
            ArrayList<String> tmpHdr = parser.getDisplayHeader();

            if ((tmpHdr != null) && (tmpHdr.size() != 0)) {
                myDisplayHeader = toFieldNameArray(tmpHdr);
            }

            // Check if sort field is specified as a part of the query.
            String tmpSortField = parser.getSortField();

            if ((tmpSortField != null) &&!tmpSortField.trim().equals("")) {
                mySortField = myFDtoName.get(tmpSortField);
            }

            // Check if sort order is specified as a part of the query.
            int tmpSortOrder = parser.getSortOrder();

            if (tmpSortOrder != -1) {
                mySortOrder = tmpSortOrder;
            }

            // Check if result count is specified as a part of the query.
            int limit = parser.getLimit();

            if (limit > 0) {
                myCurrentPageSize = limit;
            }
        }
    }

    public static void printResultSetMetaData(ResultSet rs, String yourMessage) {
        if (rs == null) {
            System.out.println(yourMessage + " : The result set was null");
            return;
        }
        try {
            ResultSetMetaData rsmd   = rs.getMetaData();
            int               count  = rsmd.getColumnCount();
            StringBuffer      buffer = new StringBuffer();

            for (int i = 1; i <= count; i++) {
                buffer.append(",").append(rsmd.getColumnName(i));
            }

           System.out.println( yourMessage + " : Table Name  " + rsmd.getTableName(1) + " : Column Info: " + buffer.toString());
        } catch (Exception e) {
            LOG.info("",(e));
        }
    }

    public static void printTableData(Hashtable<?, ?> table) {
        Enumeration<?> keys   = table.keys();
        StringBuffer   buffer = new StringBuffer();

        while (keys.hasMoreElements()) {
            Object key   = (Object) keys.nextElement();
            Object value = (Object) table.get(key);

            buffer.append("\n").append(key.toString()).append(": ").append(value.toString());
        }

        LOG.info(buffer.toString());
    }

    /**
     * This method processes the extended fields resultset which is returned
     * as a result of executing the sql batch.
     *
     * @param  rs          Result set.
     * @param  extValues   Out Parameter that will hold the extended field
     *                     values.
     *
     * @exception SQLException      Incase of database related errors.
     * @exception DatabaseException Incase of database related errors.
     *
     */
    private void processExtFields(ResultSet rs, Hashtable<String, Hashtable<String, Object>> extValues) throws SQLException, DatabaseException {
        if ((rs == null) || (rs.next() == false)) {
            return;
        }
        
        do {
            int    systemId  = rs.getInt(Field.BUSINESS_AREA);
            int    requestId = rs.getInt(Field.REQUEST);
            String fieldName = rs.getString("field_name");
            Field  field     = myFieldsTable.get(fieldName);

            // If the field could not be found, log it as a warning.
            if (field == null) {
                LOG.warn("No field corresponding to the column: " + fieldName);

                continue;
            }

            Hashtable<String, Object> value = null;

            value = extValues.get(systemId + "_" + requestId);

            if (value == null) {
                value = new Hashtable<String, Object>();
            }

            // Based on the data type, retrieve the value.
            int datatype = field.getDataTypeId();

            switch (datatype) {
            case BOOLEAN :
                value.put(field.getName(), new Boolean(rs.getBoolean("bit_value")));

                break;

            case DATE :
            case TIME :
            case DATETIME : {
                Timestamp ts = Timestamp.getTimestamp(rs.getTimestamp("datetime_value"));

                if (ts != null) {
                    value.put(field.getName(), ts);
                }
            }

            break;

            case INT :
                value.put(field.getName(), rs.getInt("int_value"));

                break;

            case REAL :
                value.put(field.getName(), rs.getDouble("real_value"));

                break;

            case STRING : {
                String str = rs.getString("varchar_value");

                if (str != null) {
                    value.put(field.getName(), str);
                }
            }

            break;

            
            case TEXT : {
                String str = rs.getString("text_value");

                if (str != null) {
                    value.put(field.getName(), str);
                }
            }

            break;

            case TYPE : {
                String str = rs.getString("type_value");

                if (str != null) {
                    value.put(field.getName(), str);
                }
            }

            break;
            
            case DataType.ATTACHMENTS: {
                String str = rs.getString("text_value");

                if (str != null && !str.equals("")) {
                    value.put(field.getName(), str);
                }
            }

            break;


            case USERTYPE :
            default :
                LOG.warn("Invalid data type: " + field.getName());
            }

            extValues.put(systemId + "_" + requestId, value);
        } while (rs.next() == true);
    }

    /**
     *
     * @param rs
     * @param userFields
     * @param extFields
     * @param userValues
     * @param extValues
     * @throws SQLException
     * @throws DatabaseException
     */
    private void processRequests(ResultSet rs, boolean userFields, boolean extFields, Hashtable<String, ArrayList<String>> userValues, Hashtable<String, Hashtable<String, Object>> extValues)
            throws SQLException, DatabaseException {
        if ((rs == null) || (rs.next() == false)) {
            return;
        }

        // printResultSetMetaData(rs);
        ResultSetMetaData rsmd        = rs.getMetaData();
        int               columnCount = rsmd.getColumnCount();
       
  		String columns = "" ;
        	int numColumns = rsmd.getColumnCount(); // Get the column names; column indices start from 1 
        	for (int x=1; x<numColumns+1; x++) 
        	{ 
        		String columnName = rsmd.getColumnName(x); // Get the name of the column's table name 
        		String tableName = rsmd.getTableName(x);  
        		columns += "," + columnName;
        	}
        	
        	//System.out.println("Process Requests This result set has columns as : " + columns );
        
        do {

            // Get the SystemId and Request id.
            int systemId  = rs.getInt(Field.BUSINESS_AREA);
            int requestId = rs.getInt(Field.REQUEST);

            // Get the fields needed for visual enhancements.
            String    status       = rs.getString(Field.STATUS);
            String    severity     = rs.getString(Field.SEVERITY);
            boolean   isPrivate    = rs.getBoolean(Field.IS_PRIVATE);
            int       maxActionId  = rs.getInt(Field.MAX_ACTION_ID);
            int       lastActionId = rs.getInt(LAST_ACTION_ID);
            int       parentId     = rs.getInt(Field.PARENT_REQUEST_ID);
            Timestamp dueDate      = Timestamp.getTimestamp(rs.getTimestamp(Field.DUE_DATE));
            Result    result       = new Result();

            result.setSystemId(systemId);
            result.setRequestId(requestId);
            result.setStatus(status);
            result.setSeverity(severity);
            result.setIsPrivate(isPrivate);
            result.setMaxActionId(maxActionId);
            result.setLastActionId(lastActionId);
            result.setParentId(parentId);
            result.setDueDate(dueDate);

            for (int i = 1; i <= columnCount; i++) {
                String colName = rsmd.getColumnName(i);

                // If the column name is last action id, skip this as this is
                // already handled.
                if (colName.equals(LAST_ACTION_ID)) {
                    continue;
                }

                Field field = myFieldsTable.get(colName);

                // If the field could not be found, log it as a warning.
                if (field == null) {
                    continue;
                }

                String fieldName = field.getName();

                // Based on the data type, retrieve the value.
                int datatype = field.getDataTypeId();

                switch (datatype) {
                case BOOLEAN :
                    result.set(fieldName, rs.getBoolean(colName));

                    break;

                case DATE :
                case TIME :
                case DATETIME :
                    result.set(fieldName, Timestamp.getTimestamp(rs.getTimestamp(colName)));

                    break;

                case INT :
                    result.set(fieldName, rs.getInt(colName));

                    break;

                case REAL :
                    result.set(fieldName, rs.getDouble(colName));

                    break;

                case STRING :
                case TEXT :
                case TYPE :
                case USERTYPE :
                    result.set(fieldName, rs.getString(colName));

                    break;

                default :
                    LOG.warn("Invalid data type: " + fieldName);
                }
            }

            if (userFields == true) {

                // Check if any user fields are present corresponding to this
                // request.
                String            key;
                ArrayList<String> value;

                // Check for loggers.
                key   = systemId + "_" + requestId + "_" + Field.LOGGER;
                value = userValues.get(key);

                if (value != null) {
                    result.set(Field.LOGGER, toCSS(value));
                }

                // Check for assignees.
                key   = systemId + "_" + requestId + "_" + Field.ASSIGNEE;
                value = userValues.get(key);

                if (value != null) {
                    result.set(Field.ASSIGNEE, toCSS(value));
                }

                // Check for Subscribers.
                key   = systemId + "_" + requestId + "_" + Field.SUBSCRIBER;
                value = userValues.get(key);

                if (value != null) {
                    result.set(Field.SUBSCRIBER, toCSS(value));
                }

                // Check for loggers.
                key   = systemId + "_" + requestId + "_" + Field.TO;
                value = userValues.get(key);

                if (value != null) {
                    result.set(Field.TO, toCSS(value));
                }

                // Check for loggers.
                key   = systemId + "_" + requestId + "_" + Field.CC;
                value = userValues.get(key);

                if (value != null) {
                    result.set(Field.CC, toCSS(value));
                }
            }

            // Check if any extended fields are present.
            if (extFields == true) {
                Hashtable<String, Object> value = extValues.get(systemId + "_" + requestId);

                result.addAll(value);
            }

            // Add this result to the requests table.
            String requestKey = formTableKey(systemId, requestId);

            myRequestsTable.put(requestKey, result);

            ArrayList<Result> sysList = myResultListByBA.get(systemId);

            if (sysList == null) {
                sysList = new ArrayList<Result>();
            }

            sysList.add(result);
            myResultListByBA.put(systemId, sysList);

            // check if this is a child request.
            if (parentId != 0) {
                String parentKey = formTableKey(systemId, parentId);

                // Get the corresponding list of children
                ArrayList<String> children = myParentsTable.get(parentKey);

                if (children == null) {
                    children = new ArrayList<String>();
                }
                children.add(requestKey);               
                myParentsTable.put(parentKey, children);
            }
        } while (rs.next() == true);
        
        // removing children like this is just a fix.
//        The better thing would be that our search query does not return redundant results
//        but I remember that using the distinct keyword in our query created some other kind
//        of problem. So using this fix for now. But when DSQL will be rewritten this should be
//        taken into account.
        removeDuplicateChildren() ; 
    }

    // this will iterate through the myParentsTable and remove redundant children
    void removeDuplicateChildren() 
    {
    	// just making it go through the HashSet<String> to find the uniques    	
    	for( Enumeration<String> allParents = myParentsTable.keys() ; allParents.hasMoreElements() ; )
    	{
    		String parentKey = allParents.nextElement() ;
    		ArrayList<String> children = myParentsTable.get(parentKey);
    		if( null != children )
    		{
		        HashSet<String> uniqueChildren = new HashSet<String>() ;
		        uniqueChildren.addAll(children) ;
		        children.clear() ;
		        children.addAll(uniqueChildren) ;
		        
		        // as the results were retrived in order by request_id,
		        // so supposedly the children should always be in the order of their ids.
		        // please confirm this so that sort by some other field do not get broken by this
		        Collections.sort(children) ; //this sort requires custom sort
		        
		        // although no need to put back the same list reference back into
		        // the hashtable. but do it for clearity and self satisfaction
		        myParentsTable.put(parentKey, children);
    		}
    	}
    }
    /**
     * This method processes the user-lists resultset which is returned
     * as a result of executing the sql batch.
     *
     * @param  rs          Result set.
     * @param  userValues  Out Parameter that will hold the request user
     *                     values.
     *
     * @exception SQLException      Incase of database related errors.
     * @exception DatabaseException Incase of database related errors.
     *
     */
    private void processUsers(ResultSet rs, Hashtable<String, ArrayList<String>> userValues) throws SQLException {
        if ((rs == null) || (rs.next() == false)) {
            return;
        }

        do {
            String            key       = "";
            ArrayList<String> value     = null;
            int               systemId  = rs.getInt(Field.BUSINESS_AREA);
            int               requestId = rs.getInt(Field.REQUEST);
            int               userType  = rs.getInt("user_type_id");
            boolean           isPrimary = rs.getBoolean("is_primary");
            String            userLogin = ((isPrimary == true)
                                           ? PRIMARY_SIGN
                                           : "") + rs.getString("user_login");

            switch (userType) {
            case UserType.LOGGER :
                key = systemId + "_" + requestId + "_" + Field.LOGGER;

                break;

            case UserType.ASSIGNEE :
                key = systemId + "_" + requestId + "_" + Field.ASSIGNEE;

                break;

            case UserType.SUBSCRIBER :
                key = systemId + "_" + requestId + "_" + Field.SUBSCRIBER;

                break;

            case UserType.TO :
                key = systemId + "_" + requestId + "_" + Field.TO;

                break;

            case UserType.CC :
                key = systemId + "_" + requestId + "_" + Field.CC;

                break;

            default :
                key = "";

                break;
            }

            value = userValues.get(key);

            if (value == null) {
                value = new ArrayList<String>();
            }

            value.add(userLogin);
            userValues.put(key, value);
        } while (rs.next() == true);
    }

    /**
     *
     * @param rs
     * @param list
     * @param listByBA
     * @throws SQLException
     */
    private void readTableKeys(ResultSet rs, ArrayList<String> list, Hashtable<Integer, ArrayList<String>> listByBA) throws SQLException {
        if (rs != null) {
            while (rs.next() != false) {
                int    systemId  = rs.getInt("sys_id");
                int    requestId = rs.getInt("request_id");
                String key       = formTableKey(systemId, requestId);

                list.add(key);

                ArrayList<String> sysList = listByBA.get(systemId);

                if (sysList == null) {
                    sysList = new ArrayList<String>();
                }

                sysList.add(key);
                listByBA.put(systemId, sysList);
            }
        }

        return;
    }

    /**
     * This method returns any rendundant parenthesis in the parse table.
     */
    private void removeRedundantParens() {
    	myParseTable = removeRedundantParens(myParseTable);
    }
    
    /**
     * Removes the consecutive () and return an arralist that after 
     * @param aParseTable
     * @return
     */
    private ArrayList<ParseEntry> removeRedundantParens(ArrayList<ParseEntry> aParseTable) {
        

        // Remove all the redundant parenthesis.
        ArrayList<ParseEntry> ptable    = new ArrayList<ParseEntry>();
        Stack<ParseEntryType> typeStack = new Stack<ParseEntryType>();

        for (ParseEntry pe : aParseTable) {

            // If this is a closing entry, check the previous one.
            if (pe.getEntryType() == ParseEntryType.NESTING_CLOSE) {

                // If the previous entry is a nesting_open entry,
                // pop the stack and continue.
                if (typeStack.peek() == ParseEntryType.NESTING_OPEN) {
                    typeStack.pop();
                    ptable.remove(ptable.size() - 1);
                }

                // If the previous entry is not a nesting_open entry,
                // add this to the ptable.
                else {
                    ptable.add(pe);
                }
            }

            // If this is not a closing entry, add this to the ptable.
            else {
                ptable.add(pe);
                typeStack.push(pe.getEntryType());
            }
        }

        return ptable;
    }

    /**
     * This method performs the necessary steps to accomplish search based on
     * the criteria specified.
     *
     */
    public void search() throws Exception {

        // Initialization...
        init();

        if ((myRequestType == RequestType.QUERY_REQUEST) || (myRequestType == RequestType.PARSE_TABLE_REQUEST)) {

            // Get the list of fields present in this BA along with BA Record.
            initializeBAFields();

            // Get the permissions table for this user in this BA.
            initializeUserPermissionTable();

            // Fill the parse table if this is a query request.
            if (myRequestType == RequestType.QUERY_REQUEST) {
                parseQuery();
            }

            if ((myRequestType == RequestType.QUERY_REQUEST) || (myRequestType == RequestType.PARSE_TABLE_REQUEST)) {

                /*
                 * Pass the parse table through filters to remove parse entries
                 * on fields on which
                 *        - user does not have permissions
                 *        - searching is not allowed.
                 */
                filterParseTable();
            }

            if ((myHasTextField == true) || (myHasAppendDate == true) || (myHasAppender == true) 
            		|| (myHasCloseInfo == true) || ( myHasAllTextField == true )) {
                boolean inActionStore = false;

                /*
                 * We goto the actionstore to perform the search only in two
                 * cases:
                 *      1. AppendDate is part of the filters.
                 *      2. Both appender and text field are part of the filters.
                 */
                if ((myHasAppendDate == true) || ((myHasAppender == true) && (myHasTextField == true))) {
                    inActionStore = true;
                }
                
             // Added by Nitiraj : this will search all the requests ( from action_id =1 to max_action_id ) hence 
                // the current value and the older values will be searched. This should be replaced by a better 
                // method to tell whether to search in actions or not. May be toggle button on the search page
                // which can be set if user also wanted to search in historical ( action ) data
                if( myHasAllTextField == true ) 
                	inActionStore = true ;

                searchLucene(inActionStore);

                ParseEntry oe = new ParseEntry();

                oe.setEntryType(ParseEntryType.NESTING_OPEN);

                ParseEntry ce = new ParseEntry();

                ce.setEntryType(ParseEntryType.NESTING_CLOSE);

                /*
                 * Incase of search in all text, just display the requests
                 * returned by the lucene search and ignore other filters as
                 * these are already considered while searching in lucene.
                 * One incomplete thing is when the query contains filters on
                 * real fields. Indexing/Searching real fields is not supported
                 * yet.
                 */
                myParseTable = new ArrayList<ParseEntry>();
                myParseTable.add(0, oe);
                myParseTable.add(ce);
                myParseTable.add(myAllTextEntry);
                removeRedundantParens();
            }
        }

        if (myHasTreeQuery == false) {
            if ((myRequestType == RequestType.QUERY_REQUEST) || (myRequestType == RequestType.PARSE_TABLE_REQUEST)) {
                validateOutputParams();
                formSearchQuery();
            } else if (myRequestType == RequestType.CUSTOM_FILTER_REQUEST) {
                formMyRequestsQuery();
            }

            // Execute the query and get the results.
            try {
                executeQuery();
            } catch (DatabaseException de) {
                String msg = de.toString().toLowerCase();
                String str = "there is already an object named";

                if (msg.indexOf(str) > 0) {
                    executeQuery();
                } else {
                    throw de;
                }
            }
        } else {
            validateOutputParams();
            formTreeQuery(mySystemId, myTreeId);

            try {
                executeTreeQuery();
            } catch (DatabaseException de) {
                LOG.info("",(de));

                String msg = de.toString().toLowerCase();
                String str = "there is already an object named";

                if (msg.indexOf(str) > 0) {
                    executeTreeQuery();
                } else {
                    throw de;
                }
            }
        }
    }

    /**
     * This method searches the lucene index and retrives the matching request
     * ids.
     */
    private void searchLucene(boolean aInActionStore) {
        ArrayList<String> reqIds = new ArrayList<String>();
        reqIds.add(IN) ;
        myLuceneQuery = getLuceneQuery();
        reqIds.addAll(LuceneSearcher.search(mySystemPrefix, myLuceneQuery, aInActionStore));

        LOG.info("Lucene Results : " + reqIds);
        // Add to this to the parse table.
        myAllTextEntry = new ParseEntry(Field.REQUEST, Field.REQUEST, 0, Connective.C_AND, reqIds);

        return;
    }

    private String sqlMatchedRequests() {
        StringBuffer query = new StringBuffer();

        query.append("\n\nSELECT sys_id, request_id FROM @tmp1");

        return query.toString();
    }

    /**
     * This method returns the arraylist of the strings given the comma
     * separated list.
     *
     * @param aList    Comma separated list of values.
     *
     * @return Array list of string values.
     *
     */
    private ArrayList<String> toArrayList(String aList) {
        ArrayList<String> list = new ArrayList<String>();

        // Handling the null reference or empty string.
        if ((aList == null) || (aList.trim().equals("") == true)) {
            return list;
        }

        StringTokenizer st = new StringTokenizer(aList, ",");

        while (st.hasMoreTokens()) {
            list.add(st.nextToken());
        }

        return list;
    }

    /**
     * This method returns the comma separated list of the entries in the list.
     *
     * @param aList    Array list of string values.
     *
     * @return Comma separated list of values.
     *
     */
    private static String toCSS(ArrayList<String> aList) {
        StringBuilder buffer = new StringBuilder();
        boolean       first  = true;

        for (String str : aList) {
            if (first == false) {
                buffer.append(",");
            } else {
                first = false;
            }

            buffer.append(str);
        }

        return buffer.toString();
    }

    /**
     * This method returns the field names corresponding to the descriptors
     * in the given array list.
     *
     * @param descList   Descriptor list.
     *
     * @return Arraylist of corresponding field names.
     *
     */
    private ArrayList<String> toFieldNameArray(ArrayList<String> descList) {
        ArrayList<String> nameList = new ArrayList<String>();

        if (descList == null) {
            return nameList;
        }

        for (String desc : descList) {
            String name = myFDtoName.get(desc);

            if (name != null) {
                nameList.add(name);
            }
        }

        return nameList;
    }

    /**
     *
     * @throws DatabaseException
     */
    private void validateOutputParams() throws DatabaseException {

        /*
         * Check if the display header is empty. In such case take the default
         * one.
         */
        if (myDisplayHeader.size() == 0) {
            myDisplayHeader = ourDefaultHeader;
        } else {

            // request_id is a mandatory field.
            if (myDisplayHeader.contains(Field.REQUEST) == false) {
                myDisplayHeader.add(0, Field.REQUEST);
            }

            /*
             * Browse through the display header and remove the fields that
             * are not supposed to be displayed in search results.
             */
            ArrayList<String>          tmpHdr = new ArrayList<String>();
            Hashtable<String, Boolean> tbl    = new Hashtable<String, Boolean>();

            for (String fieldName : myDisplayHeader) {
                Field field = myFieldsTable.get(fieldName);

                // Skip if we could not get the field object.
                if (field == null) {
                    continue;
                }

                // Skip if the field is inactive.
                if (field.getIsActive() == false) {
                    continue;
                }

                // Skip if this is a TEXT Field.
                if (field.getDataTypeId() == TEXT) {
                    continue;
                }

                // Skip if this field is not supposed to be displayed in search.
                if ((field.getPermission() & Permission.DISPLAY) == 0) {
                    continue;
                }

                Integer temp = myPermissionTable.get(field.getName());

                if (temp == null) {
                    LOG.info("User is not authorized to view this field: " + fieldName);

                    continue;
                }

                int permission = temp.intValue();

                if ((permission & Permission.VIEW) == 0) {
                    LOG.info("User is not authorized to view this field: " + fieldName);

                    continue;
                }

                if (tbl.get(fieldName) != null) {
                    continue;
                }

                // Put this in the table to check duplicates.
                tbl.put(fieldName, true);

                // Add this to the temporary header.
                tmpHdr.add(fieldName);
            }

            myDisplayHeader = tmpHdr;
        }

        // Confidential is required to render the result as a private request.
        if (myDisplayHeader.contains(Field.IS_PRIVATE) == false) {
            myVEHeader.add(Field.IS_PRIVATE);
        }

        // Severity field is required to identify critical requests.
        if (myDisplayHeader.contains(Field.SEVERITY) == false) {
            myVEHeader.add(Field.SEVERITY);
        }

        // Status field is required to identify closed requests.
        if (myDisplayHeader.contains(Field.STATUS) == false) {
            myVEHeader.add(Field.STATUS);
        }

        // request_id is required for identifying the request. Not including this was
        // creating an exception in retriving the results from the search.
        if( myDisplayHeader.contains(Field.REQUEST) == false )
        {
        	myVEHeader.add(Field.REQUEST) ;
        }
        /*
         * Due Date field is required to identify requests whose due date
         * occurs in past.
         */
        if (myDisplayHeader.contains(Field.DUE_DATE) == false) {
            myVEHeader.add(Field.DUE_DATE);
        }

        // Max Action Id is required to identify newly logged requests.
        if (myDisplayHeader.contains(Field.MAX_ACTION_ID) == false) {
            myVEHeader.add(Field.MAX_ACTION_ID);
        }

        // Parent Id is required to identify requests hierarchy.
        if (myDisplayHeader.contains(Field.PARENT_REQUEST_ID) == false) {
            myVEHeader.add(Field.PARENT_REQUEST_ID);
        }

        /*
         * Last updated date is required to identify requests which are
         * closed and not updated since last 'N' days, so that such requests
         * will not be shown unread for any one.
         */
        if (myDisplayHeader.contains(Field.LASTUPDATED_DATE) == false) {
            myVEHeader.add(Field.LASTUPDATED_DATE);
        }

        /*
         * Action Id from the user_read_actions is needed to see if any
         * new appends are made to the requests that matched the criteria.
         */
        myVEHeader.add(LAST_ACTION_ID);

        // Check if the sort field is correct.
        Field field = Field.lookupBySystemIdAndFieldName(mySystemId, mySortField);

        if (field == null) {
            mySortField = Field.REQUEST;
        }

        // Check the sort order value.
        if ((mySortOrder != ASC_ORDER) && (mySortOrder != DESC_ORDER)) {
            mySortOrder = DESC_ORDER;
        }
    }

    //~--- get methods --------------------------------------------------------

    /**
     * Accessor method for AllRequestIdList property.
     *
     * @return Current Value of AllRequestIdList
     *
     */
    public ArrayList<String> getAllRequestIdList() {
        return myAllRequestIdList;
    }

    /**
     * Accessor method for AllRequestIdList property.
     *
     * @return Current Value of AllRequestIdList
     *
     */
    public Hashtable<Integer, ArrayList<String>> getAllRequestIdListByBA() {
        return myAllRequestIdListByBA;
    }

    private String getAllRequestsList(String sortField, int sortOrder) {
        StringBuffer query = new StringBuffer();

        query.append("\nINSERT INTO #rtmp").append("\nSELECT * FROM @tmp1").append("\nUNION").append("\nSELECT * FROM @mptmp1").append("\n").append("\nSELECT * FROM @tmp1").append("\nUNION").append(
            "\nSELECT * FROM @mptmp1").append("\nORDER BY ");

        /*
         * User_id is a special field
         */
        if (sortField.equals(Field.USER)) {
            query.append("sortUserLogin");
        } else {
            query.append(sortField);
        }

        if (sortOrder == ASC_ORDER) {
            query.append(" ASC");
        } else {
            query.append(" DESC");
        }

        if (sortField.equals(Field.REQUEST) == false) {
            query.append(", request_id DESC");
        }

        query.append("\n");

        return query.toString();
    }

    /**
     * This method returns the query portion related to private field.
     *
     * @param wherePart Output parameter to hold the where portion of query.
     * @param joinPart  Output parameter to hold the join portion of query.
     *
     */
    private void getConfidentialPart(StringBuilder wherePart, StringBuilder joinPart) {

        //
        // A request is considered private if
        // - It is explicitly marked private (OR)
        // - Its business area is marked private (OR)
        // - The fixed type it is currently in is marked private.
        //
        // A user can view such a private request if
        // - he has view permission on private by virtue of his
        // association with the business area (OR)
        // - the logger role in that business area has view permission
        // on Confidential field.
        //
        //
        if (myPrivatePerm == NO_PRIVATE) {

            //
            // There is no view permission on Confidential for this user.
            // So, lets retrieve only those requests which are not private
            // according to the above definition.
            //
            wherePart.append("\n\tAND ").append("\n\t(").append("\n\t\t(").append("\n\t\t\tba.is_private = 0 AND ").append("\n\t\t\tr.is_private = 0 AND ").append(
                "\n\t\t\tcat.is_private = 0 AND ").append("\n\t\t\tstat.is_private = 0 AND ").append("\n\t\t\tsev.is_private = 0 AND ").append("\n\t\t\treqtype.is_private = 0").append(
                "\n\t\t)\n\t)\n");
        } else if (myPrivatePerm == ROLE_PRIVATE) {

            //
            // The user as such does not have view permission on Confidential.
            // But a contextual role in this BA has permissions. So, by virtue
            // of being in that role, the user can view his private tickets.
            // So, lets retrieve those private requests where the user is
            // in that particular contextual role.
            //
            String userList = "";

            if ((myRolePermLevel & LOGGER_PRIVATE) != 0) {
                userList = Integer.toString(UserType.LOGGER);
            }

            if ((myRolePermLevel & ASSIGNEE_PRIVATE) != 0) {
                if (userList.trim().equals("") == false) {
                    userList = userList + "," + UserType.ASSIGNEE;
                } else {
                    userList = Integer.toString(UserType.ASSIGNEE);
                }
            }

            if ((myRolePermLevel & SUBSCRIBER_PRIVATE) != 0) {
                if (userList.trim().equals("") == false) {
                    userList = userList + "," + UserType.SUBSCRIBER;
                } else {
                    userList = Integer.toString(UserType.SUBSCRIBER);
                }
            }

            /*
             * Since the user as such does not have permissions to view private
             * requests, get the mailing lists the user is a member of at any
             * level. We would like to retrieve the requests where the user or
             * his mailing lists are present in the context roles for which
             * this business area granted view permission on private field.
             *
             * NOTE: set the userMailingList to null, if you are not interested
             * in doing this part. That will make the search SQL batch retrieve
             * only the requests where the user is present in the contextual
             * roles with view on private.
             */
            ArrayList<User> userMailingList = MailListUser.getMailListsByRecursiveMembership(myUserId);
            String        optr = "";
            StringBuilder list = new StringBuilder();

            if ((userMailingList == null) || (userMailingList.size() == 0)) {
                optr = "=";
                list.append(myUserId);
            } else {
                optr = "IN";
                list.append("(").append(myUserId);

                for (User user : userMailingList) {
                    list.append(",").append(user.getUserId());
                }

                list.append(")");
            }

            //
            // Join with the request_users table to get the logger for the
            // matched request.
            //
            joinPart.append("\n\tLEFT JOIN request_users rup").append("\n\tON r.sys_id = rup.sys_id AND ").append("r.request_id = rup.request_id ") /*.append("rup.user_type_id in (").append(
                userList).append(") (*/ .append(" AND rup.user_id ").append(optr).append(" ").append(list.toString());

            //
            // Get the requests matched if they are not private or the logger
            // of those tickets is the user if they are private.
            //
            wherePart.append("\n\tAND ").append("\n\t(").append("\n\t\t(").append("\n\t\t\tba.is_private = 0 AND ").append("\n\t\t\tr.is_private = 0 AND").append(
                "\n\t\t\tcat.is_private = 0 AND ").append("\n\t\t\tstat.is_private = 0 AND ").append("\n\t\t\tsev.is_private = 0 AND ").append("\n\t\t\treqtype.is_private = 0").append(
                "\n\t\t)").append("\n\t\tOR ").append("\n\t\t(").append("\n\t\t\t(").append("\n\t\t\t\tba.is_private = 1 OR").append("\n\t\t\t\tr.is_private = 1 OR").append(
                "\n\t\t\t\tcat.is_private = 1 OR ").append("\n\t\t\t\tstat.is_private = 1 OR ").append("\n\t\t\t\tsev.is_private = 1 OR ").append("\n\t\t\t\treqtype.is_private = 1").append(
                "\n\t\t\t)").append("\n\t\t\tAND").append("\n\t\t\trup.user_id IS NOT NULL").append("\n\t\t)\n\t)\n");
        }
    }

    /**
     * Accessor method for CurrentPageSize property.
     *
     * @return Current Value of CurrentPageSize
     *
     */
    public int getCurrentPageSize() {
        return myCurrentPageSize;
    }

    /**
     * Accessor method for DSQLQuery property.
     *
     * @return Current Value of DSQLQuery
     *
     */
    public String getDSQLQuery() {
        return myDSQLQuery;
    }

    /**
     * Accessor method for DatabaseQuery property.
     *
     * @return Current Value of DatabaseQuery
     *
     */
    public String getDatabaseQuery() {
        return myDatabaseQuery;
    }

    /**
     *
     * @return
     */
    private String getDeclarations() {
        StringBuffer query = new StringBuffer();

        query
        .append("\nDECLARE @pageNumber INT")
        .append("\nDECLARE @pageSize INT")
        .append("\nDECLARE @rowCount INT")
        .append("\nDECLARE @query VARCHAR(7999)")
        .append("\nDECLARE @maxPageSize INT")
        .append("\nDECLARE @listAll BIT")
        .append("\n")
        .append("\nSELECT @pageNumber = ").append(myPageNumber)
        .append("\nSELECT @pageSize = ").append(myCurrentPageSize)
        .append("\nSELECT @maxPageSize = ").append(myMaximumPageSize)
        .append("\nSELECT @listAll = ").append((myIsListAll == true) ? "1": "0").append("\n");

        return query.toString();
    }

    /**
     * Accessor method for DisplayHeader property.
     *
     * @return Current Value of DisplayHeader
     *
     */
    public ArrayList<String> getDisplayHeader() {
        return myDisplayHeader;
    }

    /**
     * This method returns the statments for dropping the temporary tables
     * used during querying process.
     *
     * @return Query to drop the temporary tables.
     */
    private String getDropQuery() {
        StringBuilder query = new StringBuilder();

        query.append("\n\n");

        return query.toString();
    }

    /**
     * This method creates a parse entry of type data and returns it.
     *
     * @return Data entry.
     */
    private ParseEntry getEntry(String fname, int depth, Connective conn, ArrayList<String> argList) {
        ParseEntry pe = new ParseEntry(fname, fname, depth, conn, argList);

        return pe;
    }

    /**
     * Accessor method for FieldsTable property.
     *
     * @return Current Value of FieldsTable
     *
     */
    public Hashtable<String, Field> getFieldsTable() {
        return myFieldsTable;
    }

    /**
     * This method forms the filtering portion of the query.
     *
     * @param sortSelect Output Parameter that holds the sort select stmts.
     * @param sortJoin   Output Parameter that holds the sort join stmts.
     * @param sortStmt   Output Parameter that holds the sort stmts.
     *
     * @return Filtering query.
     *
     * @exception DatabaseException
     */
    private String getFilteringQuery(String sortSelect, String sortJoin, String sortStmt) throws DatabaseException {
        StringBuilder              query        = new StringBuilder();
        StringBuilder              joinPart     = new StringBuilder();
        StringBuilder              wherePart    = new StringBuilder();
        Hashtable<String, Boolean> parsedFields = new Hashtable<String, Boolean>();
        boolean                    topBlock     = true;
        boolean                    blockFirst   = true;

        // Get the standard joins.
        joinPart
        .append("\n\trequests r");
        getStandardJoins(joinPart);
        LOG.info("ParseTable for which filtering query will be generated: \n" + myParseTable.toString());
        LinkedList<ParseEntry> openEntries = new LinkedList<ParseEntry>();
        int                    counter     = 0;

        for (ParseEntry entry : myParseTable) {
            counter++;

            StringBuilder  join      = new StringBuilder();
            StringBuilder  cond      = new StringBuilder();
            ParseEntryType entryType = entry.getEntryType();

            /*
             * If this is a nesting close entry, close and continue with
             * the next one.
             */
            if (entryType == ParseEntryType.NESTING_CLOSE) {
                wherePart.append("\n)");

                continue;
            }

            /*
             * If this is a nesting open entry, then push this entry onto the
             * openEntries queue and continue. when the next entry is
             * parsed, this will be checked.
             */
            else if (entryType == ParseEntryType.NESTING_OPEN) {
                openEntries.add(entry);
                blockFirst = true;

                continue;
            } else if (entryType == ParseEntryType.DATA) {
                int openEntryListSize = openEntries.size();

                if (openEntryListSize != 0) {
                    ParseEntry openEntry = openEntries.get(0);
                    Connective conn      = openEntry.getConnective();

                    if (topBlock == true) {
                        wherePart.append("\n(");
                    } else {
                        wherePart.append(getOpeningBrace(conn));

                        for (int i = 1; i < openEntryListSize; i++) {
                            openEntry = openEntries.get(i);
                            conn      = openEntry.getConnective();
                            wherePart.append("\n1 = 1").append(getOpeningBrace(conn));
                        }
                    }

                    openEntries = null;
                    openEntries = new LinkedList<ParseEntry>();
                }
            }

            topBlock = false;

            String fieldName = entry.getFieldName();

            if (fieldName.equals(READ_REQUEST) || fieldName.equals(UNREAD_REQUEST)) {
                boolean read     = (fieldName.equals(READ_REQUEST) == true)
                                   ? true
                                   : false;
                boolean flagJoin = true;

                if (parsedFields.get("table_user_read_actions") != null) {
                    flagJoin = false;
                }

                parsedFields.put("table_user_read_actions", true);

                if (flagJoin == true) {
                    joinPart.append("\n\tLEFT JOIN user_read_actions ura\n\tON ").append("r.sys_id = ura.sys_id AND ").append("r.request_id = ura.request_id AND ").append("ura.user_id = ").append(
                        myUserId);
                }

                getReadUnreadQuery(wherePart, read, blockFirst, entry);
                blockFirst = false;

                continue;
            }

            if (fieldName.equals(Field.RELATED_REQUESTS)) {
                boolean flagJoin = true;

                if (parsedFields.get(Field.RELATED_REQUESTS) != null) {
                    flagJoin = false;
                }

                parsedFields.put(Field.RELATED_REQUESTS, true);

                if (flagJoin == true) {
                    joinPart.append("\n\tLEFT JOIN related_requests relrq").append("\n\tON ").append("\n\t\t(").append("ba.sys_prefix = relrq.primary_sys_prefix AND ").append(
                        "r.request_id = relrq.primary_request_id) OR ").append("\n\t\t(").append("ba.sys_prefix = relrq.related_sys_prefix AND ").append(
                        "r.request_id = relrq.related_request_id)").append("\n");
                }

                getRelatedRequestQuery(wherePart, blockFirst, entry);
                blockFirst = false;

                continue;
            }

            ArrayList<String> argList = entry.getArgList();
            Field             field   = myFieldsTable.get(fieldName);

            if (field == null) {
                LOG.warn("Field object not found: " + fieldName);

                continue;
            }

            int dataType = field.getDataTypeId();

            // If this is a text/string entry, add this to the textEntries list.
            if ((dataType == DataType.STRING) || (dataType == DataType.TEXT)) {
                myTextEntries.add(entry);
            }

            boolean flagJoin = true;

            // Check if this field is already encountered while forming the
            // query.
            if (parsedFields.get(fieldName) != null) {
                flagJoin = false;
            }

            parsedFields.put(fieldName, true);

            switch (dataType) {
            case BOOLEAN :
                DbQueryGenerator.getBooleanQuery(field, entry, blockFirst, join, cond, flagJoin);

                break;

            case DATE :
            case TIME :
            case DATETIME :
                DbQueryGenerator.getDateQuery(field, entry, blockFirst, join, cond, flagJoin);

                break;

            case INT :
            case REAL :
                DbQueryGenerator.getNumericQuery(field, entry, blockFirst, join, cond, flagJoin);

                break;

            case STRING :
                DbQueryGenerator.getTextQuery(field, entry, blockFirst, join, cond, flagJoin);

                break;

            case TEXT :

                // This text field is for null checking so get the query
                DbQueryGenerator.getTextQuery(field, entry, blockFirst, join, cond, flagJoin);

                break;

            case TYPE :
                DbQueryGenerator.getTypeQuery(field, entry, blockFirst, join, cond, flagJoin, true);

                break;

            case USERTYPE :
                DbQueryGenerator.getUserTypeQuery(field, entry, blockFirst, join, cond, counter);

                break;
            }

            joinPart.append(join.toString());
            wherePart.append(cond.toString());
            blockFirst = false;
        }

        if (wherePart.toString().trim().equals("") == false) {
            wherePart.insert(0, "\n\tAND\n\t(").append("\n\t)");
        }

        if (myCurrentPageSize < 0) {
            myCurrentPageSize = myMaximumPageSize;
        }

        // Get the query portion corresponding to privateness of BA/Types.
        getConfidentialPart(wherePart, joinPart);
        query.append("\nINSERT INTO @tmp").append("\nSELECT DISTINCT ").append("\n\tr.sys_id,").append("\n\tr.request_id,").append("\n\tr.parent_request_id").append(sortSelect).append(
            "\nFROM").append(joinPart.toString()).append(sortJoin).append("\nWHERE").append("\n\tr.sys_id = ").append(mySystemId).append(wherePart.toString()).append(sortStmt).append("\n\n").append(
            "");

        return query.toString();
    }

    /**
     * Accessor method for HasExtFields property.
     *
     * @return Current Value of HasExtFields
     *
     */
    public boolean getHasExtFields() {
        return myHasExtFields;
    }

    /**
     * Accessor method for HasUserFields property.
     *
     * @return Current Value of HasUserFields
     *
     */
    public boolean getHasUserFields() {
        return myHasUserFields;
    }

    /**
     * Accessor method for IsListAll property.
     *
     * @return Current Value of IsListAll
     *
     */
    public boolean getIsListAll() {
        return myIsListAll;
    }

    /**
     * This method returns the terms and operators to be present for the first
     * term in a block.
     *
     * @param conn
     * @return
     */
    private String getLuceneOpeningBrace(Connective conn) {
        StringBuffer query = new StringBuffer();

        switch (conn) {
        case C_AND :
        case C_NOT_NOT :
            query.append(" +(");

            break;

        case C_OR :
            query.append(" (");

            break;

        case C_NOT :
            query.append("+sysPrefix:").append(mySystemPrefix).append(" -(");

            break;

        case C_AND_NOT :
            query.append(" -(");

            break;

        case C_OR_NOT :
            query.append(" OR -(");

            break;
        }

        return query.toString();
    }

    /**
     * This method returns the lucene query to search in the request store.
     */
    private String getLuceneQuery() {
        StringBuilder query = new StringBuilder();

        query.append("+sysPrefix:").append(mySystemPrefix).append(" +(");

        ArrayList<ParseEntry>  table       = new ArrayList<ParseEntry>();
        LinkedList<ParseEntry> openEntries = new LinkedList<ParseEntry>();
        boolean                topBlock    = true;
        boolean                blockFirst  = true;

        LOG.info("ParseTable for which lucene query will be generated: \n" + myParseTable.toString());

        for (ParseEntry entry : myParseTable) {
            ParseEntryType entryType = entry.getEntryType();

            /*
             * If this is a nesting close entry, close and continue with
             * the next one.
             */
            if (entryType == ParseEntryType.NESTING_CLOSE) {
                query.append(")");
                table.add(entry);

                continue;
            }

            /*
             * If this is a nesting open entry, then push this entry onto the
             * openEntries queue and continue. when the next entry is
             * parsed, this will be checked.
             */
            else if (entryType == ParseEntryType.NESTING_OPEN) {
                openEntries.add(entry);
                blockFirst = true;
                table.add(entry);

                continue;
            } else if (entryType == ParseEntryType.DATA) {
                int openEntryListSize = openEntries.size();

                if (openEntryListSize != 0) {
                    ParseEntry openEntry = openEntries.get(0);
                    Connective conn      = openEntry.getConnective();

                    if (topBlock == true) {
                        switch (conn) {
                        case C_AND :
                            query.append(" +(");

                            break;

                        case C_OR :
                            query.append(" (");

                            break;

                        case C_NOT :
                            query.append("+sysPrefix:").append(mySystemPrefix).append(" -(");

                            break;
                        }
                    } else {
                        query.append(getLuceneOpeningBrace(conn));

                        for (int i = 1; i < openEntryListSize; i++) {
                            openEntry = openEntries.get(i);
                            conn      = openEntry.getConnective();
                            query.append(getLuceneOpeningBrace(conn));
                        }
                    }

                    openEntries = null;
                    openEntries = new LinkedList<ParseEntry>();
                }
            }

            topBlock = false;

            String desc      = entry.getDescriptor();
            String fieldName = entry.getFieldName();
            Field  field     = myFieldsTable.get(fieldName);

            if ((field == null) || (field.getIsActive() == false)) {
                continue;
            }

            ArrayList<String> argList = entry.getArgList();

            //
            // If the argument list is empty or null, then it means that, this
            // is inserted by our search program when "has" entry is found in
            // the parse table. Carry this.
            //
            if ((argList == null) || (argList.size() == 0)) {
                table.add(entry);

                continue;
            }

            int dataType = field.getDataTypeId();

            if ((dataType != STRING) && (dataType != TEXT) && (desc.equals("appenddate") == false) && (desc.equals("dateappended") == false) && (desc.equals("closeddate") == false)
                    && (desc.equals("dateclosed") == false) && (desc.equals("closedby") == false) && (desc.equals("actionuser") == false) && (desc.equals("appender") == false)) {
                table.add(entry);
            }

            // If this is a text/string entry, add this to the textEntries list.
            if ((dataType == DataType.STRING) || (dataType == DataType.TEXT)) {
                myTextEntries.add(entry);
            }

            switch (dataType) {
            case BOOLEAN :
                query.append(LuceneQueryGenerator.getBooleanQuery(fieldName, entry, blockFirst, mySystemPrefix));

                break;

            case DATE :
            case TIME :
            case DATETIME :
                query.append(LuceneQueryGenerator.getDateQuery(fieldName, entry, blockFirst, mySystemPrefix));

                break;

            case INT :
            case REAL :
                query.append(LuceneQueryGenerator.getNumericQuery(fieldName, entry, blockFirst, mySystemPrefix));

                break;

            case STRING :
            case TEXT :
                query.append(LuceneQueryGenerator.getTextQuery(desc, entry, blockFirst, mySystemPrefix));

                break;

            case TYPE :
                query.append(LuceneQueryGenerator.getTypeQuery(fieldName, entry, blockFirst, mySystemPrefix));

                break;

            case USERTYPE :
                if (desc.equals("appender") || desc.equals("actionuser")) {
                    query.append(LuceneQueryGenerator.getMultiValueQuery("appender", entry, blockFirst, mySystemPrefix));
                } else {
                    query.append(LuceneQueryGenerator.getMultiValueQuery(fieldName, entry, blockFirst, mySystemPrefix));
                }

                break;
            }

            blockFirst = false;
        }

        myParseTable = table;
        query.append(")");

        return query.toString().replaceAll("[ ]+", " ");
    }

    /**
     * Accessor method for MaximumPageSize property.
     *
     * @return Current Value of MaximumPageSize
     *
     */
    public int getMaximumPageSize() {
        return myMaximumPageSize;
    }

    /**
     *
     * @param conn
     * @return
     */
    private String getOpeningBrace(Connective conn) {
        StringBuffer buffer = new StringBuffer();

        switch (conn) {
        case C_AND :
            buffer.append("\nAND\n(");

            break;

        case C_OR :
            buffer.append("\nOR\n(");

            break;

        case C_NOT :
            buffer.append("\nAND NOT\n(");

            break;

        case C_AND_NOT :
            buffer.append("\nAND NOT \n(");

            break;

        case C_OR_NOT :
            buffer.append("\nOR NOT \n(");

            break;

        case C_NOT_NOT :
            buffer.append("\nAND\n(");

            break;
        }

        return buffer.toString();
    }

    /**
     * Accessor method for PageNumber property.
     *
     * @return Current Value of PageNumber
     *
     */
    public int getPageNumber() {
        return myPageNumber;
    }

    /**
     *
     * @return
     */
    private String getPagingSQL() {
        StringBuffer query = new StringBuffer();

        query.append("\n").append("\nIF (@rowCount < @pageSize)").append("\nBEGIN").append("\n\tINSERT INTO @tmp1").append("\n\t").append("SELECT * FROM @tmp").append("\nEND").append("\nELSE").append(
            "\nBEGIN").append("\n\tIF (@listAll = 1)").append("\n\tBEGIN").append("\n\t\tIF (@rowCount <= @maxPageSize)").append("\n\t\tBEGIN").append("\n\t\t\tINSERT INTO @tmp1").append(
            "\n\t\t\t").append("SELECT * FROM @tmp").append("\n\t\tEND").append("\n\t\tELSE").append("\n\t\tBEGIN").append("\n\t\t\tINSERT INTO @tmp1").append("\n\t\t\tSELECT TOP ").append(
            myMaximumPageSize).append("\n\t\t\t\t*").append("\n\t\t\tFROM").append("\n\t\t\t\t@tmp").append("\n\t\t\tWHERE").append("\n\t\t\t\trequest_id NOT IN ").append("\n\t\t\t\t(").append(
            "\n\t\t\t\t\tSELECT TOP ").append((myPageNumber - 1) * myMaximumPageSize).append("\n\t\t\t\t\t\trequest_id").append("\n\t\t\t\t\tFROM @tmp").append("\n\t\t\t)").append("\n\t\tEND").append(
            "\n\tEND").append("\n\tELSE").append("\n\tBEGIN").append("\n\t\tINSERT INTO @tmp1").append("\n\t\tSELECT TOP ").append(myCurrentPageSize).append("\n\t\t\t*").append("\n\t\tFROM").append(
            "\n\t\t\t@tmp").append("\n\t\tWHERE").append("\n\t\t\trequest_id NOT IN ").append("\n\t\t\t(").append("\n\t\t\t\tSELECT TOP ").append((myPageNumber - 1) * myCurrentPageSize).append(
            "\n\t\t\t\t\trequest_id").append("\n\t\t\t\tFROM @tmp").append("\n\t\t\t)").append("\n\tEND").append("\nEND").append("\n");

        return query.toString();
    }

    /**
     * Accessor method for ParentsTable property.
     *
     * @return Current Value of ParentsTable
     *
     */
    public Hashtable<String, ArrayList<String>> getParentsTable() {
        return myParentsTable;
    }

    /**
     * Accessor method for ParseTable property.
     *
     * @return Current Value of ParseTable
     *
     */
    public ArrayList<ParseEntry> getParseTable() {
        return myParseTable;
    }

    /**
     * Accessor method for PermissionTable property.
     *
     * @return Current Value of PermissionTable
     *
     */
    public Hashtable<String, Integer> getPermissionTable() {
        return myPermissionTable;
    }

    private void getReadUnreadQuery(StringBuilder filter, boolean read, boolean first, ParseEntry entry) {
        Connective conn = entry.getConnective();

        filter.append("\n\t\t");

        if (first == false) {
            switch (conn) {
            case C_AND :
            case C_AND_NOT :
                filter.append("AND ");

                break;

            case C_OR :
            case C_OR_NOT :
                filter.append("OR  ");

                break;

            case C_NOT :
            case C_NOT_NOT :
                filter.append("AND ");

                break;
            }
        }

        ArrayList<String> argList = entry.getArgList();
        boolean           arg     = false;

        // If the argument list is empty, then check is to see if the boolean
        // field is null or not.
        if ((argList == null) || (argList.size() == 0)) {
            if ((conn == Connective.C_AND_NOT) || (conn == Connective.C_OR_NOT) || (conn == Connective.C_NOT)) {
                arg = true;
            } else {
                arg = false;
            }
        } else {
            if (argList.get(0).equals("0")) {
                arg = false;
            } else {
                arg = true;
            }
        }

        String readSQL   = "r.max_action_id = ura.action_id";
        String unreadSQL = " (" + "ura.action_id IS NULL OR " + "ura.action_id < r.max_action_id" + ") ";

        //
        // Expected formats of argument list are
        // - [ Boolean Value(1/0) ]
        //
        if ((conn == Connective.C_NOT) || (conn == Connective.C_AND_NOT) || (conn == Connective.C_OR_NOT)) {
            if (read == true) {
                if (arg == false) {
                    filter.append(readSQL);
                } else {
                    filter.append(unreadSQL);
                }
            } else {
                if (arg == false) {
                    filter.append(unreadSQL);
                } else {
                    filter.append(readSQL);
                }
            }
        } else {
            if (read == true) {
                if (arg == true) {
                    filter.append(readSQL);
                } else {
                    filter.append(unreadSQL);
                }
            } else {
                if (arg == true) {
                    filter.append(unreadSQL);
                } else {
                    filter.append(readSQL);
                }
            }
        }
    }

    private void getRelatedRequestQuery(StringBuilder filter, boolean first, ParseEntry entry) {
        Connective conn = entry.getConnective();

        filter.append("\n\t\t");

        if (first == false) {
            switch (conn) {
            case C_AND :
            case C_AND_NOT :
                filter.append("AND ");

                break;

            case C_OR :
            case C_OR_NOT :
                filter.append("OR  ");

                break;

            case C_NOT :
            case C_NOT_NOT :
                filter.append("AND ");

                break;
            }
        }

        String optr = "";

        if ((conn == Connective.C_AND_NOT) || (conn == Connective.C_OR_NOT) || (conn == Connective.C_NOT)) {
            optr = "NOT";
        } else {
            optr = "";
        }

        filter.append(" relrq.primary_sys_prefix IS ").append(optr).append(" NULL");

        return;
    }

    /**
     * This method returns the query part related to request ex table.
     *
     * @param reList  List of ids of extended fields.
     *
     * @return Request Ex query.
     */
    private static String getReqExQuery(ArrayList<String> reList, String tableName) {
        StringBuilder query = new StringBuilder();

        query.append("\n\nSELECT ").append("\n\tre.sys_id,").append("\n\tre.request_id,").append("\n\tre.field_id,").append("\n\tf.name 'field_name',").append("\n\tre.bit_value,").append(
            "\n\tre.datetime_value,").append("\n\tre.int_value,").append("\n\tconvert(varchar(1024), re.real_value) 'real_value',").append("\n\tre.varchar_value,").append("\n\tre.text_value,").append(
            "\n\tcase typ.is_private").append("\n\twhen 1 then '+' + typ.display_name").append("\n\twhen 0 then typ.display_name").append("\n\tend 'type_value'").append("\nFROM ").append(
            "\n\t").append(tableName).append(" t ").append("\n\tLEFT JOIN requests_ex re").append("\n\tON t.sys_id = re.sys_id AND ").append("t.request_id = re.request_id").append(
            "\n\tLEFT JOIN fields f").append("\n\tON re.sys_id = f.sys_id AND ").append("re.field_id = f.field_id  ").append("\n\tLEFT JOIN types typ").append(
            "\n\tON re.sys_id = typ.sys_id AND ").append("re.field_id = typ.field_id AND ").append("re.type_value = typ.type_id").append("\nWHERE \n\tre.field_id IN (").append(toCSS(reList)).append(
            ")\nORDER BY re.sys_id, re.request_id, re.field_id");

        return query.toString();
    }

    /**
     * This method returns the query part related to request users table.
     *
     * @param ruList  List of ids of User types.
     *
     * @return Request User query.
     */
    private static String getReqUserQuery(ArrayList<String> ruList, String tableName) {
        StringBuilder query = new StringBuilder();

        query.append("\n\nSELECT").append("\n\tru.sys_id,").append("\n\tru.request_id,").append("\n\tru.field_id,").append("\n\tru.user_type_id,").append("\n\tu.user_login,").append("\n\tru.ordering,").append(
            "\n\tru.is_primary").append("\nFROM \n\t").append(tableName).append(" t ").append("\n\tLEFT JOIN request_users ru").append("\n\tON t.sys_id = ru.sys_id AND ").append(
            "t.request_id = ru.request_id").append("\n\tLEFT JOIN users u").append("\n\tON ru.user_id = u.user_id ").append("\nWHERE \n\tru.field_id IN (").append(toCSS(ruList)).append(
            ")\nORDER BY ").append("ru.sys_id, ru.request_id,ru.field_id,").append("ru.user_type_id, ru.ordering");

        return query.toString();
    }

    /**
     * Accessor method for RequestsTable property.
     *
     * @return Current Value of RequestsTable
     *
     */
    public Hashtable<String, Result> getRequestsTable() {
        return myRequestsTable;
    }

    /**
     * Accessor method for RequestsWithIncompleteTrees property.
     *
     * @return Current Value of RequestsWithIncompleteTrees
     *
     */
    public ArrayList<String> getRequestsWithIncompleteTrees() {
        return myRequestsWithIncompleteTrees;
    }

    /**
     *
     * @return
     */
    private String getResultCountSQL() {
        StringBuffer query = new StringBuffer();

        query.append("\nSELECT @rowCount = @@ROWCOUNT").append("\nSELECT @rowCount 'result_count'");

        return query.toString();
    }

    /**
     * Accessor method for ResultIdList property.
     *
     * @return Current Value of ResultIdList
     *
     */
    public ArrayList<String> getResultIdList() {
        return myResultIdList;
    }

    /**
     * Accessor method for ResultIdListByBA property.
     *
     * @return Current Value of ResultIdListByBA
     *
     */
    public Hashtable<Integer, ArrayList<String>> getResultIdListByBA() {
        return myResultIdListByBA;
    }

    /**
     * Accessor method for ResultIdList property.
     *
     * @return Current Value of ResultIdList
     *
     */
    public ArrayList<Result> getResultList() {
        return myResultList;
    }

    /**
     * Accessor method for ResultListByBA property.
     *
     * @return Current Value of ResultListByBA
     *
     */
    public Hashtable<Integer, ArrayList<Result>> getResultListByBA() {
        return myResultListByBA;
    }

    /**
     * Accessor method for RetrievedResultCount property.
     *
     * @return Current Value of RetrievedResultCount
     *
     */
    public int getRetrievedResultCount() {
        return myRetrievedResultCount;
    }

    private String getSQLForRequestsWithIncompleteTrees() {
        StringBuffer query = new StringBuffer();

        query.append("\n").append("\nSELECT DISTINCT").append("\n\tr.sys_id,").append("\n\tr.parent_request_id").append("\nFROM").append("\n\trequests r").append("\n\tJOIN #rtmp t").append(
            "\n\tON r.sys_id = t.sys_id AND ").append("r.parent_request_id = t.request_id").append("\n\tLEFT JOIN #rtmp t1").append("\n\tON r.sys_id = t1.sys_id AND ").append(
            "r.request_id = t1.request_id").append("\nWHERE").append("\n\tt1.sys_id IS NULL").append("\n");

        return query.toString();
    }

    /**
     * This method forms the selecting portion of the query.
     *
     * @param sortSelect Select statement for sort field.
     * @param sortJoin   Join statement for sort field.
     * @param sortStmt   Sorting statement for sort field.
     *
     * @return Selection Query.
     */
    private String getSelectionQuery(String sortSelect, String sortJoin, String sortStmt, String tableName, boolean includeConfPart) throws DatabaseException {
        StringBuilder     query      = new StringBuilder();
        StringBuilder     rjQuery    = new StringBuilder();
        ArrayList<String> rlQuery    = new ArrayList<String>();
        ArrayList<String> ruList     = new ArrayList<String>();
        ArrayList<String> reList     = new ArrayList<String>();
        ArrayList<String> selectList = new ArrayList<String>();

        selectList.addAll(myDisplayHeader);
        selectList.addAll(myVEHeader);
        rlQuery.add("\n\tr.sys_id 'sys_id'");
        getStandardJoins(rjQuery);

        for (String fieldName : selectList) {

            // This field is needed to render requests as read/unread.
            if (fieldName.equals(LAST_ACTION_ID)) {
                rlQuery.add("\n\tISNULL(ura.action_id, 0) '" + LAST_ACTION_ID + "'");
                rjQuery.append("\n\tLEFT JOIN user_read_actions ura\n\tON ").append("t.sys_id = ura.sys_id AND ").append("t.request_id = ura.request_id AND ").append("ura.user_id = ").append(
                    myUserId);

                continue;
            }

            // Get the corresponding field object.
            Field field = myFieldsTable.get(fieldName);

            // This field cannot be null and cannot be inactive.
            if ((field == null) || (field.getIsActive() == false)) {
                continue;
            }

            fieldName = field.getName();

            int permission = field.getPermission();

            if ((permission & Permission.DISPLAY) == 0) {
                continue;
            }

            // Get the data type.
            int dataTypeId = field.getDataTypeId();

            // Ignore text fields.
            if (dataTypeId == TEXT) {
                continue;
            }

            // if the field is a multi-vaule and is not user_id field
            if ((dataTypeId == USERTYPE)) {
//                int utId =  - 5;

                ruList.add(Integer.toString(field.getFieldId()));

                continue;
            } else if (field.getIsExtended() == true) {
                reList.add(Integer.toString(field.getFieldId()));

                continue;
            }

            // all the selected fields here are from requests table.
            String        tableAlias = "r";
            StringBuilder join       = new StringBuilder();
            StringBuilder list       = new StringBuilder();

            switch (dataTypeId) {
            case BOOLEAN :
            case DATE :
            case TIME :
            case DATETIME :
            case INT :
            case REAL :
            case STRING :
                list.append(DbQueryGenerator.select(tableAlias, fieldName, fieldName));

                break;

            case TYPE :
                DbQueryGenerator.selectType(field, join, list);

                break;

            case USERTYPE :

                // This can only be User_id in requests table.
                DbQueryGenerator.selectUsers(field, join, list);

                break;
            }    // End Switch

            if (list.toString().trim().equals("") == false) {
                rlQuery.add(list.toString());
            }

            
            if ((dataTypeId != TYPE) || fieldName.equals(Field.OFFICE)) {

                /*
                 * Consider this join part only if this is not for a type'd
                 * field. If this is for a type'd field, then it should be only
                 * office field, because for all other four standard type fields
                 * namely cat/sta/sev/reqtype, we already have the joins with us
                 */
                rjQuery.append(join.toString());
            }
        }    // End For Each

        StringBuilder rQuery  = new StringBuilder();
        StringBuilder ruQuery = new StringBuilder();
        StringBuilder reQuery = new StringBuilder();

        if (selectList.contains(mySortField)) {
            Field field = myFieldsTable.get(mySortField);

            if (field == null) {
                sortSelect = "";
                sortStmt   = "";
            } else if ((field.getIsExtended() == true) || (field.getDataTypeId() == TYPE) || ((field.getDataTypeId() == USERTYPE) && (mySortField.equals(Field.USER) == true))) {
                sortSelect = sortSelect.replaceAll(mySortField, "mySortField");
                sortStmt   = sortStmt.replaceAll(mySortField, "mySortField");
            }
        }

        StringBuilder wherePart = new StringBuilder();

        /*
         * We need this because getConfidentialPart starts off with connective
         * in all its cases.
         */
        wherePart.append("\n\t1 = 1");

        // Check if the confidential part of the query has to be included.
        if (includeConfPart == true) {
            getConfidentialPart(wherePart, rjQuery);
        }

         rQuery.append("\n\nSELECT ").append(toCSS(rlQuery)).append(sortSelect).append("\nFROM \n\t").append(tableName).append(" t ").append("\n\tLEFT JOIN requests r").append(
            "\n\tON t.sys_id = r.sys_id AND ").append("t.request_id = r.request_id").append(rjQuery.toString()).append(sortJoin).append("\nwhere").append(wherePart.toString()).append("\n").append(
            sortStmt);

        if (ruList.size() > 0) {
            ruQuery.append(getReqUserQuery(ruList, tableName));
            myHasUserFields = true;
        }

        if (reList.size() > 0) {
            reQuery.append(getReqExQuery(reList, tableName));
            myHasExtFields = true;
        }

        String colList  = toCSS(rlQuery);
        String joinList = rjQuery.toString();

        // First we would like to retrieve the extended fields.
        query.append(reQuery.toString());

        // Followed by the request users.
        query.append(ruQuery.toString());

        // Finally the request records.
        query.append(rQuery.toString());

        return query.toString();
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
     * This method forms the sorting portion of the query.
     *
     * @param select
     * @param join
     * @param stmt
     *
     */
    private void getSortingQuery(StringBuilder create, StringBuilder select, StringBuilder join, StringBuilder stmt) {
        Field  field     = myFieldsTable.get(mySortField);
        String sortOrder = (mySortOrder == ASC_ORDER)
                           ? " ASC"
                           : " DESC";

        if (field.getName().equals(Field.REQUEST)) {

            // Sorting on request_id is straightforward.
            stmt.append("\nORDER BY r.request_id ").append(sortOrder);

            return;
        }

        if (field.getName().equals(Field.PARENT_REQUEST_ID)) {

            // Sorting on request_id is straightforward.
            stmt.append("\nORDER BY r.parent_request_id ").append(sortOrder).append(", ORDER BY r.request_id DESC");

            return;
        } else if (field.getName().equals(Field.SEVERITY)) {
            select.append(",\n\tsortType.ordering '").append(field.getName()).append("' ");
            join.append("\n\tLEFT JOIN types sortType").append("\n\tON r.sys_id = sortType.sys_id ").append(" AND sortType.field_id = ").append(field.getFieldId()).append(" AND r.").append(
                field.getName()).append(" = sortType.type_id");
            stmt.append("\nORDER BY severity_id ").append(sortOrder).append(", r.request_id DESC");
            create.append(",\n\tseverity_id INT");

            return;
        } else if (field.getName().equals(Field.USER) == true) {

            //
            // Sorting on userlogin includes
            // - Join the Users table with the requests table on user_id
            // - sort on the user_login field of users table.
            //
            select.append(",\n\tsortUser.user_login 'sortUserLogin' ");
            join.append("\n\tLEFT JOIN users sortUser").append("\n\tON r.user_id = sortUser.user_id");
            stmt.append("\nORDER BY sortUserLogin ").append(sortOrder).append(", r.request_id DESC");
            create.append(",\n\tsortUserLogin VARCHAR(128)");

            return;
        } else if (field.getDataTypeId() == USERTYPE) {

            // UserType     Field Id       UserTypeId
            // ---------------------------------------------
            // Logger           7              2
            // Assignee         8              3
            // Subscriber       9              4
            // To               10             5
            // Cc               11             6
            //
            int    userTypeId = field.getFieldId() - 5;
            String ruName     = "sort_" + field.getName();
            String sName      = field.getName();

            select.append(",\n\tsortUser.user_login '").append(sName).append("' ");
            join.append("\n\tLEFT JOIN request_users ").append(ruName).append("\n\tON r.sys_id = ").append(ruName).append(".sys_id AND r.request_id = ").append(ruName).append(
                ".request_id AND ").append(ruName).append(".user_type_id = ").append(userTypeId).append(" AND ").append(ruName).append(".ordering = 1 ").append("\n\tLEFT JOIN users sortUser").append(
                "\n\tON ").append(ruName).append(".user_id = sortUser.user_id");
            stmt.append("\nORDER BY ").append(sName).append(sortOrder).append(", r.request_id DESC");
            create.append(",\n\t").append(field.getName()).append(" VARCHAR(128)");

            return;
        } else if (field.getIsExtended() == true) {
            if (field.getDataTypeId() == DataType.TYPE) {
                select.append(",\n\tsortType.display_name '").append(field.getName()).append("' ");
                join.append("\n\tLEFT JOIN requests_ex reSort").append("\n\tON r.sys_id = reSort.sys_id ").append("AND r.request_id = reSort.request_id ").append("AND reSort.field_id = ").append(
                    field.getFieldId()).append("\n\tLEFT JOIN types sortType").append("\n\tON reSort.sys_id = sortType.sys_id ").append(" AND reSort.field_id = sortType.field_id ").append(
                    " AND reSort.type_value = sortType.type_id");
                stmt.append("\nORDER BY ").append(field.getName()).append(sortOrder).append(", r.request_id DESC");
                create.append(",\n\t").append(field.getName()).append(" VARCHAR(128)");

                return;
            }

            String fieldName    = "";
            String dataTypeName = "";
            int    dataType     = field.getDataTypeId();

            switch (dataType) {
            case BOOLEAN :
                fieldName    = "bit_value";
                dataTypeName = " BIT ";

                break;

            case DATE :
            case TIME :
            case DATETIME :
                fieldName    = "datetime_value";
                dataTypeName = " DATETIME ";

                break;

            case INT :
                fieldName    = "int_value";
                dataTypeName = " INT ";

                break;

            case REAL :
                fieldName    = "real_value";
                dataTypeName = " REAL ";

                break;

            case STRING :
                fieldName    = "varchar_value";
                dataTypeName = " NVARCHAR(3000) ";

                break;

            case TEXT :
                fieldName    = "text_value";
                dataTypeName = " NTEXT ";

                break;
            }

            select.append(",\n\treSort.").append(fieldName).append(" '").append(field.getName()).append("' ");
            join.append("\n\tLEFT JOIN requests_ex reSort").append("\n\tON r.sys_id = reSort.sys_id ").append("AND r.request_id = reSort.request_id ").append("AND reSort.field_id = ").append(
                field.getFieldId());
            stmt.append("\nORDER BY ").append(field.getName()).append(sortOrder).append(", r.request_id DESC");
            create.append(",\n\t").append(field.getName()).append(" ").append(dataTypeName);

            return;
        } else if (field.getDataTypeId() == DataType.TYPE) {
            select.append(",\n    sortType.ordering '").append(field.getName()).append("' ");
            join.append("\n    LEFT JOIN types sortType").append("\n    ON r.sys_id = sortType.sys_id ").append(" AND sortType.field_id = ").append(field.getFieldId()).append(" AND r.").append(
                field.getName()).append(" = sortType.type_id");
            stmt.append("\nORDER BY ").append(field.getName()).append(sortOrder).append(", r.request_id DESC");
            create.append(",\n\t").append(field.getName()).append(" INT");

            return;
        } else {
            select.append(",\n\tr.").append(field.getName());
            stmt.append("\nORDER BY r.").append(field.getName()).append(sortOrder).append(", r.request_id DESC");
            create.append(",\n\t").append(field.getName());

            int dataType = field.getDataTypeId();

            switch (dataType) {
            case BOOLEAN :
                create.append(" BIT");

                break;

            case DATE :
            case TIME :
            case DATETIME :
                create.append(" DATETIME");

                break;

            case INT :
                create.append(" INT");

                break;

            case REAL :
                create.append(" REAL");

                break;

            case STRING :
                create.append(" NVARCHAR(3000)");

                break;

            case TEXT :
                create.append(" NTEXT");

                break;
            }
        }
    }

    /**
     * This method returns the portion of JOIN statements corresponding to
     * standard types.
     *
     * @param joinPart Output parameter that holds the join statements.
     */
    private void getStandardJoins(StringBuilder joinPart) throws DatabaseException {

        /*
         * "requests" is the main table joined to the business area table
         * to check if the BA is private.
         */
        joinPart
        .append("\n\tJOIN business_areas ba")
        .append("\n\tON r.sys_id = ba.sys_id AND ba.is_active = 1");

        /*
         * Join with the types table on category field to check if
         * it is private.
         */
        Field field = null;

        field = myFieldsTable.get(Field.CATEGORY);
        joinPart
        .append("\n\tJOIN types cat")
        .append("\n\tON r.sys_id = cat.sys_id AND r.category_id = cat.type_id AND cat.is_active = 1 AND cat.field_id = ")
        .append(field.getFieldId());

        /*
         * Join with the types table on status field to check if
         * it is private.
         */
        field = myFieldsTable.get(Field.STATUS);
        joinPart
        .append("\n\tJOIN types stat")
        .append("\n\tON r.sys_id = stat.sys_id AND r.status_id = stat.type_id AND stat.is_active = 1 AND stat.field_id = ")
        .append(field.getFieldId());

        /*
         * Join with the types table on severity field to check if
         * it is private.
         */
        field = myFieldsTable.get(Field.SEVERITY);
        joinPart
        .append("\n\tJOIN types sev")
        .append("\n\tON r.sys_id = sev.sys_id AND r.severity_id = sev.type_id AND sev.is_active = 1 AND sev.field_id = ")
        .append(field.getFieldId());

        /*
         * Join with the types table on request type field to check if
         * it is private.
         */
        field = myFieldsTable.get(Field.REQUEST_TYPE);
        joinPart
        .append("\n\tJOIN types reqtype")
        .append("\n\tON r.sys_id = reqtype.sys_id AND r.request_type_id = reqtype.type_id AND reqtype.is_active = 1 ")
        .append(" AND reqtype.field_id = ").append(field.getFieldId());
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
     * This method returns the string/text parse entries in the parse table.
     *
     * @return Parse table.
     */
    public ArrayList<ParseEntry> getTextEntries() {
        return myTextEntries;
    }

    /**
     * Accessor method for TotalResultCount property.
     *
     * @return Current Value of TotalResultCount
     *
     */
    public int getTotalResultCount() {
        return myTotalResultCount;
    }

    /**
     *
     * @param sortSelect
     * @param sortJoin
     * @return
     */
    private String getUnmatchedParents(String sortSelect, String sortJoin) {
        StringBuffer query = new StringBuffer();

        query.append("\nINSERT INTO @mptmp1").append("\nSELECT DISTINCT").append("\n\tr.sys_id,").append("\n\tr.request_id,").append("\n\tr.parent_request_id")

        // TODONE: Get the correct sort field name here...
        .append(sortSelect).append("\nFROM ").append("\n\trequests r").append("\n\tJOIN @tmp1 res").append("\n\tON r.sys_id = res.sys_id AND ").append(
            "r.request_id = res.parent_request_id -- Parent.").append("\n\tLEFT JOIN @tmp1 res1").append("\n\tON r.sys_id = res1.sys_id AND ").append("r.request_id = res1.request_id")

        // TODONE: Get the correct sort join here...
        .append(sortJoin).append("\nWHERE").append("\n\tres1.sys_id IS NULL").append("\n").append("\nINSERT INTO @mptmp2 SELECT * FROM @mptmp1").append(
            "\nWHILE (EXISTS ( SELECT * FROM @mptmp2 ) )").append("\nBEGIN").append("\n\tINSERT INTO @mptmp3").append("\n\tSELECT DISTINCT").append("\n\t\tr.sys_id,").append(
            "\n\t\tr.request_id,").append("\n\t\tr.parent_request_id")

        // TODONE: Get the correct sort field name here ...
        .append(sortSelect.replaceAll("\n\t", "\n\t\t")).append("\n\tFROM ").append("\n\t\trequests r").append("\n\t\tJOIN @mptmp2 res").append("\n\t\tON r.sys_id = res.sys_id AND ").append(
            "r.request_id = res.parent_request_id -- Parent").append("\n\t\tLEFT JOIN @tmp1 res1").append("\n\t\tON r.sys_id = res1.sys_id AND ").append("r.request_id = res1.request_id")

        // TODONE: Get the correct sort join here...
        .append(sortJoin.replaceAll("\n\t", "\n\t\t")).append("\n\tWHERE").append("\n\t\tres1.sys_id IS NULL").append("\n").append("\n\tINSERT INTO @mptmp1 SELECT * FROM @mptmp3").append(
            "\n\tDELETE @mptmp2").append("\n\tINSERT INTO @mptmp2 SELECT * FROM @mptmp3").append("\n\tDELETE @mptmp3").append("\nEND").append("\n").append("\n");

        return query.toString();
    }

    /*
     * START: Getters and Setters for the input/output variables.
     *
     */

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
     * Mutator method for AllRequestIdList property.
     *
     * @param aAllRequestIdList New Value for AllRequestIdList
     *
     */
    public void setAllRequestIdList(ArrayList<String> aAllRequestIdList) {
        myAllRequestIdList = aAllRequestIdList;
    }

    /**
     * Mutator method for AllRequestIdList property.
     *
     * @param aAllRequestIdListByBA New Value for AllRequestIdList
     *
     */
    public void setAllRequestIdListByBA(Hashtable<Integer, ArrayList<String>> aAllRequestIdListByBA) {
        myAllRequestIdListByBA = aAllRequestIdListByBA;
    }

    /**
     * Mutator method for CurrentPageSize property.
     *
     * @param aCurrentPageSize New Value for CurrentPageSize
     *
     */
    public void setCurrentPageSize(int aCurrentPageSize) {
        myCurrentPageSize = aCurrentPageSize;
    }

    /**
     * Mutator method for DSQLQuery property.
     *
     * @param aDSQLQuery New Value for DSQLQuery
     *
     */
    public void setDSQLQuery(String aDSQLQuery) {
        myDSQLQuery = aDSQLQuery;
    }

    /**
     * Mutator method for DatabaseQuery property.
     *
     * @param aDatabaseQuery New Value for DatabaseQuery
     *
     */
    public void setDatabaseQuery(String aDatabaseQuery) {
        myDatabaseQuery = aDatabaseQuery;
    }

    private void setDefaults() {
        myRequestType     = RequestType.NO_OP_REQUEST;
        myDisplayHeader   = ourDefaultHeader;
        mySortField       = ourDefaultSortField;
        mySortOrder       = ourDefaultSortOrder;
        myMaximumPageSize = 100000;
        myIsListAll       = false;
        myPageNumber      = 1;
        myCurrentPageSize = 100000;
        myVEHeader        = new ArrayList<String>();
    }

    /**
     * Mutator method for DisplayHeader property.
     *
     * @param aDisplayHeader New Value for DisplayHeader
     *
     */
    public void setDisplayHeader(ArrayList<String> aDisplayHeader) {
        myDisplayHeader = aDisplayHeader;
    }

    /**
     * Mutator method for FieldsTable property.
     *
     * @param aFieldsTable New Value for FieldsTable
     *
     */
    public void setFieldsTable(Hashtable<String, Field> aFieldsTable) {
        myFieldsTable = aFieldsTable;
    }

    /**
     * Mutator method for HasExtFields property.
     *
     * @param aHasExtFields New Value for HasExtFields
     *
     */
    public void setHasExtFields(boolean aHasExtFields) {
        myHasExtFields = aHasExtFields;
    }

    /**
     * Mutator method for HasUserFields property.
     *
     * @param aHasUserFields New Value for HasUserFields
     *
     */
    public void setHasUserFields(boolean aHasUserFields) {
        myHasUserFields = aHasUserFields;
    }

    /**
     * Mutator method for IsListAll property.
     *
     * @param aIsListAll New Value for IsListAll
     *
     */
    public void setIsListAll(boolean aIsListAll) {
        myIsListAll = aIsListAll;
    }

    /**
     * Mutator method for MaximumPageSize property.
     *
     * @param aMaximumPageSize New Value for MaximumPageSize
     *
     */
    public void setMaximumPageSize(int aMaximumPageSize) {
        myMaximumPageSize = aMaximumPageSize;
    }

    /**
     * Mutator method for PageNumber property.
     *
     * @param aPageNumber New Value for PageNumber
     *
     */
    public void setPageNumber(int aPageNumber) {
        myPageNumber = aPageNumber;
    }

    /**
     * Mutator method for ParentsTable property.
     *
     * @param aParentsTable New Value for ParentsTable
     *
     */
    public void setParentsTable(Hashtable<String, ArrayList<String>> aParentsTable) {
        myParentsTable = aParentsTable;
    }

    /**
     * Mutator method for ParseTable property.
     *
     * @param aParseTable New Value for ParseTable
     *
     */
    public void setParseTable(ArrayList<ParseEntry> aParseTable) {
        myParseTable = aParseTable;
    }

    /**
     * Mutator method for PermissionTable property.
     *
     * @param aPermissionTable New Value for PermissionTable
     *
     */
    public void setPermissionTable(Hashtable<String, Integer> aPermissionTable) {
        myPermissionTable = aPermissionTable;
    }

    /**
     * Mutator method for RequestsTable property.
     *
     * @param aRequestsTable New Value for RequestsTable
     *
     */
    public void setRequestsTable(Hashtable<String, Result> aRequestsTable) {
        myRequestsTable = aRequestsTable;
    }

    /**
     * Mutator method for RequestsWithIncompleteTrees property.
     *
     * @param aRequestsWithIncompleteTrees New Value for RequestsWithIncompleteTrees
     *
     */
    public void setRequestsWithIncompleteTrees(ArrayList<String> aRequestsWithIncompleteTrees) {
        myRequestsWithIncompleteTrees = aRequestsWithIncompleteTrees;
    }

    /**
     * Mutator method for ResultIdList property.
     *
     * @param aResultIdList New Value for ResultIdList
     *
     */
    public void setResultIdList(ArrayList<String> aResultIdList) {
        myResultIdList = aResultIdList;
    }

    /**
     * Mutator method for ResultIdListByBA property.
     *
     * @param aResultIdListByBA New Value for ResultIdListByBA
     *
     */
    public void setResultIdListByBA(Hashtable<Integer, ArrayList<String>> aResultIdListByBA) {
        myResultIdListByBA = aResultIdListByBA;
    }

    /**
     * Mutator method for ResultIdList property.
     *
     * @param aResultList New Value for ResultIdList
     *
     */
    public void setResultList(ArrayList<Result> aResultList) {
        myResultList = aResultList;
    }

    /**
     * Mutator method for ResultListByBA property.
     *
     * @param aResultListByBA New Value for ResultListByBA
     *
     */
    public void setResultListByBA(Hashtable<Integer, ArrayList<Result>> aResultListByBA) {
        myResultListByBA = aResultListByBA;
    }

    /**
     * Mutator method for RetrievedResultCount property.
     *
     * @param aRetrievedResultCount New Value for RetrievedResultCount
     *
     */
    public void setRetrievedResultCount(int aRetrievedResultCount) {
        myRetrievedResultCount = aRetrievedResultCount;
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
     * Mutator method for SystemId property.
     *
     * @param aSystemId New Value for SystemId
     *
     */
    public void setSystemId(int aSystemId) {
        mySystemId = aSystemId;
    }

    /**
     * Mutator method for TotalResultCount property.
     *
     * @param aTotalResultCount New Value for TotalResultCount
     *
     */
    public void setTotalResultCount(int aTotalResultCount) {
        myTotalResultCount = aTotalResultCount;
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
