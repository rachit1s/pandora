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
 * MultiSearcher.java
 *
 * $Header:
 *
 */
package transbit.tbits.search;

//~--- non-JDK imports --------------------------------------------------------

//Imports from TBits.
import transbit.tbits.common.DatabaseException;
import transbit.tbits.common.TBitsLogger;
import transbit.tbits.domain.BusinessArea;
import transbit.tbits.domain.Field;
import transbit.tbits.domain.FieldDescriptor;
import transbit.tbits.domain.Permission;
import transbit.tbits.domain.User;
import transbit.tbits.exception.DSQLParseException;
import transbit.tbits.search.ParseEntry.ParseEntryType;

//Static imports.
import static transbit.tbits.Helper.TBitsConstants.PKG_SEARCH;

//~--- JDK imports ------------------------------------------------------------

//Java Imports.
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Stack;

//~--- classes ----------------------------------------------------------------

//Third Party Imports.

/**
 * This class accomplishes search across multiple business areas in the
 * current TBits Instance.
 *
 * @author  : Vaibhav.
 * @version : $Id: $
 */
public class MultiSearcher implements SearchConstants {

    // Application Logger.
    public static final TBitsLogger LOG = TBitsLogger.getLogger(PKG_SEARCH);

    // Max number of rows that shall be retrieved from the database for each BA
    private static final int MAX_ROWS = 100;

    // Default list of display fields for search.
    private static ArrayList<String> ourDefaultHeader;

    //~--- static initializers ------------------------------------------------

    static {

        // Fill the default header columns.
        ourDefaultHeader = new ArrayList<String>();

        //
        // Request | Severity | Subject | Category | Status | Logger | Assignee
        //
        ourDefaultHeader.add(Field.REQUEST);
        ourDefaultHeader.add(Field.SEVERITY);
        ourDefaultHeader.add(Field.SUBJECT);
        ourDefaultHeader.add(Field.CATEGORY);
        ourDefaultHeader.add(Field.STATUS);
        ourDefaultHeader.add(Field.LOGGER);
        ourDefaultHeader.add(Field.ASSIGNEE);
    }

    //~--- fields -------------------------------------------------------------

    // Lucene related params.
    private boolean luceneSearched = false;

    // Level of the permission on Private field for this user.
    private int                             myPrivatePerm = NO_PRIVATE;
    private int                             myContextPerm = NO_PRIVATE;
    public long                             businessObjTime;
    public long                             headerGenTime;
    public long                             initTime;
    public long                             luceneSearchTime;
    private Hashtable<String, BusinessArea> myBATable;

    // Table [ field descriptor, field name ].
    private Hashtable<String, String> myDescNames;

    // Table [ field name, data type ].
    private Hashtable<String, Integer> myDescTypes;

    // Temporary variables used during display header formation.
    private ArrayList<String> myDisplayHeader;

    // Table [ field descriptor, field object ].
    private Hashtable<String, Field>                    myFields;
    private Hashtable<String, Hashtable<String, Field>> myFieldsTable;

    // Parse table produced as a result of parsing the DSQL Query.
    private ArrayList<ParseEntry> myParseTable;

    // Output parameters.
    private ArrayList<String> myPrefixList;

    // DSQL Query.
    private String                               myQuery;
    private Hashtable<String, ArrayList<String>> myReqIdList;

    // user object.
    private User myUser;

    // Id of the user who initiated the search request.
    private int               myUserId;
    private ArrayList<String> myVEHeader;
    public long               parsingTime;
    public long               queryExecTime;
    public long               queryGenTime;
    public long               rsProcessTime;

    //~--- constructors -------------------------------------------------------

    /**
     * The only constructor.
     *
     */
    public MultiSearcher(User aUser, String aQuery) {
        myUser  = aUser;
        myQuery = aQuery;
    }

    //~--- methods ------------------------------------------------------------

    /**
     * This method returns the OR separated list of the entries.
     *
     * @param aList    Array list of string values.
     *
     * @return OR separated list of values.
     *
     */
    private static String ORify(ArrayList<String> aList) {
        if (aList == null) {
            return "";
        }

        StringBuilder buffer = new StringBuilder();
        boolean       first  = true;

        for (String str : aList) {
            if (first == false) {
                buffer.append(" OR ");
            } else {
                first = false;
            }

            buffer.append(str);
        }

        return buffer.toString();
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
            ParseEntryType entryType = pe.getEntryType();

            if ((entryType == ParseEntryType.NESTING_OPEN) || (entryType == ParseEntryType.NESTING_CLOSE)) {
                ptable.add(pe);

                continue;
            }

            Field  field = null;
            String desc  = pe.getDescriptor();

            // If the descriptor is areas ignore it.
            if (desc.equals("areas") == true) {
                myPrefixList = pe.getArgList();

                // Check if the prefix list contains any duplicates.
                myPrefixList = removeDuplicates(myPrefixList);

                continue;
            }

            // Check if the descriptor is "has"
            if (desc.equals("has") == true) {

                // Check the first element of argument list.
                ArrayList<String> argList = pe.getArgList();

                if (argList == null) {
                    continue;
                }

                String arg   = argList.get(0);
                String fname = myDescNames.get(arg);

                if (fname == null) {
                    continue;
                }

                ParseEntry entry = getDataEntry(fname, pe.getDepth(), negateConnective(pe.getConnective()));

                ptable.add(entry);

                continue;
            }
            
            if (desc.equals("alltext") || desc.equals("all")) {
                /*
                 * Map the field name of this entry to Field.DESCRIPTION.
                 * But retain the descriptor as is since it is different from
                 * description field in lucene.
                 */
                pe.setFieldName(Field.DESCRIPTION);
                ptable.add(pe);

                continue;
            }
            
            String fieldName = pe.getFieldName();

            field = myFields.get(fieldName);

            // Skip the descriptors which do not have any associated field.
            if (field == null) {
                LOG.info("Field not found for this descriptor: " + fieldName);

                continue;
            }

            // Skip the field if it is not searchable.
            int permission = field.getPermission();

            if ((permission & Permission.SEARCH) == 0) {
                LOG.info("Field is not searchable: " + fieldName);

                continue;
            }

            ptable.add(pe);
        }

        myParseTable = null;
        myParseTable = ptable;

        // Normalize the parse table.
        normalizeParseTable();

        return;
    }

    /**
     * This method prepares the Desc-Type table and the Desc-Fieldname table.
     *
     * @exception DatabaseException In case of database related errors.
     */
    private void initializeTables() throws DatabaseException {
        long start, end;

        start = System.currentTimeMillis();

        // Get the user object.
        myUserId    = myUser.getUserId();
        myDescTypes = new Hashtable<String, Integer>();
        myDescNames = new Hashtable<String, String>();
        myFields    = new Hashtable<String, Field>();
        FieldDescriptor.getAllDescriptors(myDescTypes, myDescNames, myFields);

        // Initialize the other tables.
        myBATable     = new Hashtable<String, BusinessArea>();
        myFieldsTable = new Hashtable<String, Hashtable<String, Field>>();
        end           = System.currentTimeMillis();
        initTime      = (end - start);
    }

    /**
     * Main method for testing.
     */
    public static void main(String arg[]) {}

    /**
     * This method returns the negation of the specified connective.
     *
     * @param c Connective.
     *
     * @return Negation of specified connective.
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
     * This method removes the redundant parenthesis in the parse table
     * structure.
     */
    private void normalizeParseTable() {

        // Remove all the redundant parenthesis.
        ArrayList<ParseEntry> ptable    = new ArrayList<ParseEntry>();
        Stack<ParseEntryType> typeStack = new Stack<ParseEntryType>();

        for (ParseEntry pe : myParseTable) {

            // If this is a closing entry, check the previous one.
            if (pe.getEntryType() == ParseEntryType.NESTING_CLOSE) {

                // If the previous entry is a nesting_open entry,
                // pop the stack and continue.
                if (typeStack.peek() == ParseEntryType.NESTING_OPEN) {
                    typeStack.pop();
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
            }

            typeStack.push(pe.getEntryType());
        }

        myParseTable = null;
        myParseTable = ptable;
    }

    /**
     * This method just parses the given query.
     *
     * @exception DatabaseException In case of database related errors.
     * @exception DSQLParseException In case of dsql query related errors.
     *
     */
    public void parse() throws DatabaseException, DSQLParseException {

        // Check if the query is non-null and non-empty.
        if ((myQuery == null) || (myQuery.trim().equals("") == true)) {
            return;
        }

        myQuery = myQuery.trim();

        // Initialize the field descriptor tables.
        initializeTables();

        // Parse the query and filter the parse table.
        parseQuery();
    }

    /**
     * This method calls the DSQL parser.
     *
     */
    private void parseQuery() {
        long start, end;

        start = System.currentTimeMillis();

        // Call the DSQL Parser
        DSQLParser parser = new DSQLParser(myQuery, myDescTypes, myDescNames);

        try {
            parser.parse();
        } catch (DSQLParseException de) {
            LOG.warn("",(de));

            // throw de;
        } finally {

            //
            // Irrespective of whether parsing is successful or not, we want
            // the parse table.
            //
            myParseTable = parser.getParseTable();
        }

        // Now filter the parse table.
        filterParseTable();
        end         = System.currentTimeMillis();
        parsingTime = (end - start);
    }

    /**
     * This method prints the time taken at various stages during search
     * process
     */
    public void printTimes() {
        LOG.info("Time to initialize fields  : " + initTime);
        LOG.info("Parsing DSQL Query         : " + parsingTime);
        LOG.info("Retrieving Business Objects: " + businessObjTime);
        LOG.info("Generating Database Query  : " + queryGenTime);
        LOG.info("Generating Display Headers : " + headerGenTime);
        LOG.info("Executing Database Query   : " + queryExecTime);
        LOG.info("Processing Result sets     : " + rsProcessTime);
    }

    /**
     * This method returns the duplicated strings in the list.
     *
     * @param list    List of strings.
     *
     * @return List of unique strings.
     */
    private ArrayList<String> removeDuplicates(ArrayList<String> list) {
        Hashtable<String, Boolean> table = new Hashtable<String, Boolean>();
        ArrayList<String>          iList = new ArrayList<String>();

        for (String prefix : list) {
            if (table.get(prefix.toUpperCase()) == null) {
                iList.add(prefix);
            }

            table.put(prefix.toUpperCase(), true);
        }

        table = null;

        return iList;
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
     * This method returns the comma separated list of the entries enclosed in
     * single quotes.
     *
     * @param aList    Array list of string values.
     *
     * @return Comma separated list of values.
     *
     */
    private static String toQuotedCSS(ArrayList<String> aList) {
        if (aList == null) {
            return "";
        }

        StringBuilder buffer = new StringBuilder();
        boolean       first  = true;

        for (String str : aList) {
            if (first == false) {
                buffer.append(",");
            } else {
                first = false;
            }

            buffer.append("'").append(str).append("'");
        }

        return buffer.toString();
    }

    //~--- get methods --------------------------------------------------------

    /**
     * This method returns the table of business areas keyed by Prefix in
     * uppercase.
     *
     * @return Display Header table.
     */
    public Hashtable<String, BusinessArea> getBATable() {
        return myBATable;
    }

    /**
     * This method prepares a parse entry with the given details.
     *
     */
    private ParseEntry getDataEntry(String fieldName, int depth, Connective connective) {
        ParseEntry entry = new ParseEntry();

        entry.setEntryType(ParseEntryType.DATA);
        entry.setDescriptor(fieldName);
        entry.setFieldName(fieldName);
        entry.setDepth(depth);
        entry.setConnective(connective);
        entry.setArgList(new ArrayList<String>());

        return entry;
    }

    /**
     * This method returns the parse table.
     *
     * @return Display Header table.
     */
    public ArrayList<ParseEntry> getParseTable() {
        return myParseTable;
    }

    /**
     * This method returns the list of prefixes.
     *
     * @return Display Header table.
     */
    public ArrayList<String> getPrefixList() {
        return myPrefixList;
    }
}
