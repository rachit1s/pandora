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
 * DSQLParser.java
 *
 * $Header:
 *
 */
package transbit.tbits.search;

//~--- non-JDK imports --------------------------------------------------------

import transbit.tbits.Helper.Messages;

//TBits Imports.
import transbit.tbits.common.TBitsLogger;
import transbit.tbits.common.Timestamp;
import transbit.tbits.domain.Field;
import transbit.tbits.exception.DSQLParseException;
import transbit.tbits.search.ParseEntry.ParseEntryType;

//Static imports
import static transbit.tbits.Helper.TBitsConstants.PKG_SEARCH;
import static transbit.tbits.domain.DataType.BOOLEAN;
import static transbit.tbits.domain.DataType.DATE;
import static transbit.tbits.domain.DataType.DATETIME;
import static transbit.tbits.domain.DataType.INT;
import static transbit.tbits.domain.DataType.USERTYPE;
import static transbit.tbits.domain.DataType.REAL;
import static transbit.tbits.domain.DataType.STRING;
import static transbit.tbits.domain.DataType.TEXT;
import static transbit.tbits.domain.DataType.TIME;
import static transbit.tbits.domain.DataType.TYPE;

//~--- JDK imports ------------------------------------------------------------

//Java Imports.
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;

import java.text.SimpleDateFormat;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Hashtable;
import java.util.Stack;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

//~--- classes ----------------------------------------------------------------

/**
 * This class parses the query in DSQL.
 *
 * @author  Vaibhav.
 * @version $Id: $
 *
 */
public class DSQLParser implements SearchConstants {

    // Application Logger.
    public static final TBitsLogger LOG = TBitsLogger.getLogger(PKG_SEARCH);

    //~--- fields -------------------------------------------------------------

    // Current Operator.
    private Connective     myCOptr    = Connective.C_AND;
    private ParseEntryType myPrevType = ParseEntryType.DATA;

    // Member variable to hold the sort order if specified in the query.
    private int mySortOrder = -1;

    // Member variable to hold the count of parse entries.
    private int myPECount = 0;

    // Member variable to hold the limit if specified in the query.
    private int myLimit = -1;

    // Member variale to hold the value related to expand option.
    private boolean myExpand = false;

    // Member variable to hold the depth of query.
    private int myDepth = 0;

    // Argument List for Free text.
    private ArrayList<String> freeText;

    // Member variable that holds the current pointer in the token list.
    private int myCurPos;

    // Member variable to hold the value list in case of date parse entries.
    private ArrayList<String> myDateArgList;

    // Member variable that holds the list of descriptors and their datatypes
    private Hashtable<String, Integer> myDescTable;

    // Member variable to hold the Display header if specified in the query.
    private ArrayList<String> myDisplayHeader;

    // Member variable that holds the list of descriptors and their field names
    private Hashtable<String, String> myFDMap;

    // Member variable that holds the input.
    private String myInput;

    // Member variable that holds the Parse table at the end of parsing.
    private ArrayList<ParseEntry> myParseTable;

    // Member variable that holds the Refined Input.
    private String myProcInput;

    // Memeber variable to hold the sort field if specified in the query.
    private String mySortField;

    // Member variable to hold the clause in case of date/multivalue
    // parse entries.
    private String mySubClause;

    // Member variable that holds the Token list.
    private ArrayList<String> myTokenList;

    // Member variable that holds the size of token list.
    private int myTokenListSize;
    
    //Member variable to resolve the conflict of NOTs across parantheses. Set to zero with each '('
    private int notCount;
    
    //Member variable for testing purpose only
    private int afterNelement;

    //~--- constructors -------------------------------------------------------

    /**
     * The only constructor.
     *
     * @param aInput      DSQL Query.
     * @param aDescTable  Table that maps the descriptor to its data type.
     * @param aFDMap      Table that maps the descriptor to its field name.
     *
     */
    public DSQLParser(String aInput, Hashtable<String, Integer> aDescTable, Hashtable<String, String> aFDMap) {
        myInput     = aInput;
        myDescTable = aDescTable;
        myFDMap     = aFDMap;
        notCount = 0;
        afterNelement = 1;
        
        // Process the input to remove redundant spaces etc.,
        //TODO: WHY WHY WHY?
        //SHould be taken care by parser.
        processInput();

//        myProcInput = myInput;
        
        // Pass the input through the tokenizer.
        tokenizeInput();
        
        // Get the size of token's list.
        myTokenListSize = myTokenList.size();
        
        // Initialize the parse table and the freeText array list.
        myParseTable = new ArrayList<ParseEntry>();
        freeText     = new ArrayList<String>();
    }

    //~--- methods ------------------------------------------------------------

    /**
     * This method prepares a ParseEntry and adds it to the table.
     *
     * @param desc    Descriptor
     * @param argList Argument List next to this descriptor.
     *
     */
    private void addToParseTable(String desc, ArrayList<String> argList) {
        if ((argList == null) || (argList.size() == 0)) {
            return;
        }

        String fieldName = myFDMap.get(desc);

        if (fieldName == null) {

            /*
             * Check if this is one of the predefined descriptors
             *   - sortfield
             *   - sortorder
             *   - disphdr
             */
            if (desc.equals("sortfield")) {
                int size = argList.size();

                mySortField = argList.get(size - 1);
            } else if (desc.equals("sortorder")) {
                int    size      = argList.size();
                String sortOrder = argList.get(size - 1);

                sortOrder = (sortOrder == null)
                            ? ""
                            : sortOrder;

                if (sortOrder.equals("desc") || sortOrder.equals("d") || sortOrder.equals("1") || sortOrder.equals("y") || sortOrder.equals("yes") || sortOrder.equals("t")
                        || sortOrder.equals("true")) {
                    mySortOrder = DESC_ORDER;
                } else if (sortOrder.equals("asc") || sortOrder.equals("a") || sortOrder.equals("0") || sortOrder.equals("n") || sortOrder.equals("no") || sortOrder.equals("f")
                           || sortOrder.equals("false")) {
                    mySortOrder = ASC_ORDER;
                }
            } else if (desc.equals("expand")) {
                int    size   = argList.size();
                String expand = argList.get(size - 1);

                expand = (expand == null)
                         ? ""
                         : expand;

                if (expand.equals("1") || expand.equals("yes") || expand.equals("y") || expand.equals("t") || expand.equals("true")) {
                    myExpand = true;
                } else if (expand.equals("0") || expand.equals("no") || expand.equals("n") || expand.equals("f") || expand.equals("false")) {
                    myExpand = false;
                }
            } else if (desc.equals("disphdr")) {
                myDisplayHeader = argList;
            } else if (desc.equals("limit")) {
                myLimit = Integer.parseInt(argList.get(1));
            } else {
                fieldName = desc;

                ParseEntry pe = new ParseEntry(desc, fieldName, myDepth, myCOptr, argList);

                myPrevType = ParseEntryType.DATA;
                myParseTable.add(pe);
                myPECount++;
            }
        } else {
            ParseEntry pe       = new ParseEntry(desc, fieldName, myDepth, myCOptr, argList);
            int        dataType = getDataType(desc);

            if ((dataType == DATE) || (dataType == DATETIME) || (dataType == TIME) || (dataType == USERTYPE)) {
                pe.setClause(mySubClause);
                pe.setExtraInfo(myDateArgList);
            }

            mySubClause   = "";
            myDateArgList = null;
            myPrevType    = ParseEntryType.DATA;
            myParseTable.add(pe);
            myPECount++;
        }
    }

    /**
     * This method adds the AFTER token and the date next to it to the
     * argList. (Arglist is an out parameter here.)
     *
     * @param optr    Operator text.
     * @param argList Argument list.
     *
     * @exception DSQLParseException.
     */
    private void after(String optr, ArrayList<String> argList) throws DSQLParseException {

        /*
         * Format can be
         *   - due:after <date>
         *   - due:after <date> <time>
         *   - due:after <date> <time <zone>
         *   - due:after:<date>
         *   - due:after:<date> <time>
         *   - due:after:<date> <time <zone>
         */
        String token = optr;

        // Add the operator.
        argList.add(token);

        // Skip the space/colon after the operator.
        nextToken();

        // get the date that follows this space.
        String dateValue = getDate(true);

        argList.add(dateValue);
        myDateArgList.add(dateValue);

        return;
    }

    /**
     * This method adds the BEFORE token and the date next to it to the
     * argList. (Arglist is an out parameter here.)
     *
     * @param optr    Operator text.
     * @param argList Argument list.
     *
     * @exception DSQLParseException.
     */
    private void before(String optr, ArrayList<String> argList) throws DSQLParseException {

        /*
         * Format can be
         *   - due:before <date>
         *   - due:before <date> <time>
         *   - due:before <date> <time <zone>
         *   - due:before:<date>
         *   - due:before:<date> <time>
         *   - due:before:<date> <time <zone>
         */
        String token = optr;

        // Add the operator.
        argList.add(token);

        // Skip the space/colon after the operator.
        nextToken();

        // get the date that follows this space.
        String dateValue = getDate(true);

        argList.add(dateValue);
        myDateArgList.add(dateValue);

        return;
    }

    /**
     * This method adds the BETWEEN token and the dates next to it to the
     * argList. (Arglist is an out parameter here.)
     *
     * @param optr    Operator text.
     * @param argList Argument list.
     *
     * @exception DSQLParseException.
     */
    private void between(String optr, ArrayList<String> argList) throws DSQLParseException {

        /*
         * Format can be
         *   - due:between [date]
         *   - due:between [date, date]
         *   - due:between [date date]
         *   - due:between [date AND date]
         *   - due:between date date
         *   - due:between date, date
         *   - due:between date AND date
         *   - due:between:[date]
         *   - due:between:[date, date]
         *   - due:between:[date date]
         *   - due:between:[date AND date]
         *   - due:between:date date
         *   - due:between:date, date
         *   - due:between:date AND date
         */
        boolean square    = false;
        String  dateValue = "";
        String  token     = optr;

        // Add the operator.
        argList.add(token);

        // Skip the space/colon after the operator.
        nextToken();

        /*
         * Check if the next token is a open_square.
         * If so, we should read out the corresponding closing square at the end
         */
        token = nextToken();

        if (token.equals(OPEN_SQUARE)) {
            square = true;
        } else {
            putBack();    // Put back the token as it is not open_square.
        }

        // get the date that follows this space.
        dateValue = getDate(true);
        argList.add(dateValue);
        myDateArgList.add(dateValue);

        if (isLast() == false) {
            token = nextToken();

            if (token.equals(COMMA)) {
                dateValue = getDate(false);
                argList.add(dateValue);
                myDateArgList.add(dateValue);
            } else if (token.equals(SPACE)) {

                // check if the next token is AND.
                token = nextToken();

                if (token.equals(AND)) {

                    // Skip the next space.
                    nextToken();
                    dateValue = getDate(false);
                    argList.add(dateValue);
                    myDateArgList.add(dateValue);
                } else {

                    /*
                     * Put the back the token which was read assuming it to be
                     * AND.
                     */
                    putBack();
                    dateValue = getDate(false);
                    argList.add(dateValue);
                    myDateArgList.add(dateValue);
                }
            } else if (token.equals(CLOSE_SQUARE)) {

                // Put the back the close_square as parseQuery expects it.
                putBack();

                // Take the end date as today.
                dateValue = toStandardFormat(new Timestamp(), false);
                argList.add(dateValue);
                myDateArgList.add(dateValue);
            }
        } else {

            // Take the end date as today.
            dateValue = toStandardFormat(new Timestamp(), false);
            argList.add(dateValue);
            myDateArgList.add(dateValue);
        }

        if (square == true) {

            // Read out the closing square bracket.
            token = nextToken();

            // Put the back the token if it is not close square.
            if (token.equals(CLOSE_SQUARE) == false) {
                putBack();
            }
        }

        return;
    }

    /**
     * This method checks if we are at the end of parsing. If not, it will
     * call parseQuery() again on the rest of the input.
     *
     * @exception DSQLParseException.
     */
    private void checkEnd() throws DSQLParseException {
        String token = "";

        if (isLast() == false) {

            // The next token should be a space.
            token = nextToken();

            if (token.equals(SPACE)) {

                // Check if the next token is a connective or not.
                token = nextToken();

                if ((token.equalsIgnoreCase(AND) == false) && (token.equalsIgnoreCase(OR) == false) && (token.equalsIgnoreCase(NOT) == false)) {
                    myCOptr = Connective.C_AND;

                    /*
                     * Put the back the token which was read assuming it to be
                     * an operator.
                     */
                    putBack();
                    parseQuery();

                    return;
                } else {
                    if (token.equalsIgnoreCase(AND)) {
                        myCOptr = Connective.C_AND;
                    } else if (token.equalsIgnoreCase(OR)) {
                        myCOptr = Connective.C_OR;
                    } else if (token.equalsIgnoreCase(NOT)) {
                        myCOptr = Connective.C_NOT;
                    }

                    token = nextToken();

                    // This token will be a space. What follows is a query.
                    parseQuery();

                    return;
                }
            }    // End If token == space
                    else if (token.equals(CLOSE_PAREN)) {

                // Put the back the close_paren token as parseQuery expects it.
                putBack();
            }    // End If token == Close Paren
                    else {
                LOG.warn("Non-space, Non-close-paren token: " + token);
                freeText.add(token);
                checkEnd();
            }
        }        // End of isLast== false condition check.
                else {

            // So we are the end of the input.
        }

        return;
    }

    /**
     * This method checks if the specified value is a valid numeric value.
     *
     * @param aValue Value that should be checked.
     *
     * @return aValue itself if it is a valid numeric value.
     *
     * @exception DSQLParseException If it is not a valid numeric value.
     */
    private String checkNumericValue(String aValue) throws DSQLParseException {
        try {
            double value = Double.parseDouble(aValue);

            return aValue;
        } catch (NumberFormatException nfe) {
            LOG.info(Messages.getMessage("TBits_DSQL_NUMERIC", aValue));

            throw new DSQLParseException(Messages.getMessage("TBits_DSQL_NUMERIC", aValue));
        }
    }

    /**
     * The main method.
     */
    public static void main(String arg[]) {
        if (arg.length != 1) {
            System.err.println("Usage: DSQLParser <File name>");
            return;
        }

        Hashtable<String, Integer> aDescTable = new Hashtable<String, Integer>();
        Hashtable<String, String>  aFDMap     = new Hashtable<String, String>();

        aDescTable.put("ba", new Integer(STRING));
        aDescTable.put("req", new Integer(INT));
        aDescTable.put("tree", new Integer(INT));
        aDescTable.put("cat", new Integer(TYPE));
        aDescTable.put("stat", new Integer(TYPE));
        aDescTable.put("sev", new Integer(TYPE));
        aDescTable.put("reqtype", new Integer(TYPE));
        aDescTable.put("log", new Integer(USERTYPE));
        aDescTable.put("ass", new Integer(USERTYPE));
        aDescTable.put("sub", new Integer(USERTYPE));
        aDescTable.put("to", new Integer(USERTYPE));
        aDescTable.put("cc", new Integer(USERTYPE));
        aDescTable.put("subj", new Integer(STRING));
        aDescTable.put("desc", new Integer(TEXT));
        aDescTable.put("conf", new Integer(BOOLEAN));
        aDescTable.put("par", new Integer(INT));
        aDescTable.put("user", new Integer(USERTYPE));
        aDescTable.put("act", new Integer(INT));
        aDescTable.put("duedate", new Integer(DATETIME));
        aDescTable.put("logdate", new Integer(DATETIME));
        aDescTable.put("upddate", new Integer(DATETIME));
        aDescTable.put("hd", new Integer(TEXT));
        aDescTable.put("att", new Integer(TEXT));
        aDescTable.put("sum", new Integer(TEXT));
        aDescTable.put("memo", new Integer(TEXT));
        aDescTable.put("ai", new Integer(INT));
        aDescTable.put("notify", new Integer(BOOLEAN));
        aDescTable.put("notlog", new Integer(BOOLEAN));
        aFDMap.put("ba", Field.BUSINESS_AREA);
        aFDMap.put("req", Field.REQUEST);
        aFDMap.put("tree", Field.REQUEST);
        aFDMap.put("cat", Field.CATEGORY);
        aFDMap.put("stat", Field.STATUS);
        aFDMap.put("sev", Field.SEVERITY);
        aFDMap.put("reqtype", Field.REQUEST_TYPE);
        aFDMap.put("request type", Field.REQUEST_TYPE);
        aFDMap.put("log", Field.LOGGER);
        aFDMap.put("ass", Field.ASSIGNEE);
        aFDMap.put("sub", Field.SUBSCRIBER);
        aFDMap.put("to", Field.TO);
        aFDMap.put("cc", Field.CC);
        aFDMap.put("subj", Field.SUBJECT);
        aFDMap.put("desc", Field.DESCRIPTION);
        aFDMap.put("conf", Field.IS_PRIVATE);
        aFDMap.put("user", Field.USER);
        aFDMap.put("par", Field.PARENT_REQUEST_ID);
        aFDMap.put("act", Field.MAX_ACTION_ID);
        aFDMap.put("duedate", Field.DUE_DATE);
        aFDMap.put("logdate", Field.LOGGED_DATE);
        aFDMap.put("upddate", Field.LASTUPDATED_DATE);
        aFDMap.put("hd", Field.HEADER_DESCRIPTION);
        aFDMap.put("att", Field.ATTACHMENTS);
        aFDMap.put("sum", Field.SUMMARY);
        aFDMap.put("memo", Field.MEMO);
        aFDMap.put("ai", Field.APPEND_INTERFACE);
        aFDMap.put("notify", Field.NOTIFY);
        aFDMap.put("notlog", Field.NOTIFY_LOGGERS);

        try {
            String         fileName = arg[0].trim();
            BufferedReader br       = new BufferedReader(new InputStreamReader(new FileInputStream(fileName)));
            String         str      = "";
            int            counter  = 0;

            str = br.readLine();
            
            while (str != null) {
                counter++;

                if ((str.trim().equals("") == false) && (str.startsWith("#") == false)) {
                   // LOG.info(counter + ". " + str);

                    DSQLParser app = new DSQLParser(str, aDescTable, aFDMap);

                    try {
                        app.parse();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    
                    boolean isMatching = true;
                    int entryNum = 0;
                    int size = app.myParseTable.size();
                    
                    while(!(str=br.readLine()).contains("****"))
                    {
                    	if(str.trim().equals("") == true) 
                    		continue;
                    	size--;
                    	if (!app.checkExpectedOutput(str,entryNum++,aDescTable, aFDMap))
                    	 {
                    		 isMatching = false;
                    		 break;	
                    	 }
                    }
                    
                    if(isMatching && size==0) System.out.println("\nTest Case " +counter + " is working fine " );
                    else System.out.println("\nError: Test Case " +counter + " is not working fine " );
                    while(!str.contains("****")) 
                    	str= br.readLine();
                    str = br.readLine();
                    if (str== null) break;
                    while(str.trim().equals("\n")||str.trim().equals("")) 
                    		str = br.readLine();
                    
                    //LOG.info(app.printParseTable());
                    
                    System.out.println("-----------------------------------" + "-----------------------------------" + "-----------------------------------" + "-----------------------------------"
                                       + "-----------------------------------");
                }
                else str = br.readLine();
            }

            br.close();
            br = null;
        } catch (Exception e) {
            e.printStackTrace();
            return ;
        }
        return ;
    }
    
    /**
     * This method checks the parse table with the expected output stored in the 
     * file
     * 
     * @return true if matched else false
     *
     */
    private boolean checkExpectedOutput(String str,int entryNum,Hashtable<String, Integer> aDescTable ,Hashtable<String, String>  aFDMap)
    {
    	DSQLParser check = new DSQLParser(str, aDescTable, aFDMap);
    	ArrayList<String> tokenList = check.getTokenList();
    	int size = tokenList.size();
    	
    	boolean isAccepted = false;
    	
    	//StringBuilder pentryType = new StringBuilder();
    	//pentryType.append(myParseTable.get(entryNum).getEntryType());
    	
    	StringBuilder connectiveType = new StringBuilder();
    	connectiveType.append(myParseTable.get(entryNum).getConnective());
		
    	   	
    	try
    	{
    		if(ParseEntryType.NESTING_OPEN == myParseTable.get(entryNum).getEntryType())
        	{
    			afterNelement = 1;
    			String token = getNextToken(tokenList,afterNelement,size);
    			
        		if(token.equals("("))
            	{
        			getNextToken(tokenList,afterNelement,size); 
        			token = getNextToken(tokenList,afterNelement,size); 
        			if(!token.equalsIgnoreCase(connectiveType.toString())) 
        				return false;
        			
        			isAccepted = true;
        			getNextToken(tokenList,afterNelement,size);
        			return false;
            	}
        		
        		else return false;
        	}
        	
        	else if(ParseEntryType.NESTING_CLOSE == myParseTable.get(entryNum).getEntryType())
        	{
        		afterNelement = 1;
    			String token = getNextToken(tokenList,afterNelement,size);
    			
        		if (token.equals(")"))
        		{
        			getNextToken(tokenList,afterNelement,size); 
        			isAccepted = true;
        			getNextToken(tokenList,afterNelement,size);
        			return false;
        			
        		}
        		else return false;
        	}
        	
        	else     		//Type : Data
        	{
        		
            	
            		afterNelement = 1;
            		
            		String token = getNextToken(tokenList,afterNelement,size);
            		if(!token.equalsIgnoreCase(myParseTable.get(entryNum).getDescriptor())) return false;
            		
            		getNextToken (tokenList,afterNelement,size); 
            		token = getNextToken(tokenList,afterNelement,size);
            		if(!token.equalsIgnoreCase(myParseTable.get(entryNum).getFieldName())) return false;
            		
            		getNextToken (tokenList,afterNelement,size);
            		token = getNextToken(tokenList,afterNelement,size);
            		Integer depth = myParseTable.get(entryNum).getDepth();
            		if(!token.equalsIgnoreCase(depth.toString())) return false;
            		
            		getNextToken (tokenList,afterNelement,size);
            		token = getNextToken(tokenList,afterNelement,size);
            		if(!token.equalsIgnoreCase(myParseTable.get(entryNum).getConnective().toString())) return false;
            		
            		getNextToken (tokenList,afterNelement,size);  // for ,
            		getNextToken (tokenList,afterNelement,size);  //for [
            		
            		for(String arg: myParseTable.get(entryNum).getArgList())
            		{
            			token = getNextToken(tokenList,afterNelement,size);
                		if(!token.equalsIgnoreCase(arg)) return false;
                		getNextToken(tokenList,afterNelement,size); //for ,
            		}
//            		getNextToken(tokenList,afterNelement,size);   // for ]
            		getNextToken(tokenList,afterNelement,size);   // for ]
            		
            		isAccepted = true;
            		
            		getNextToken(tokenList,afterNelement,size);   // shud return NULL 
            		return false;			//since it will reach here only if there is something 
            								// more after the parse entry .which shud not be
            		
            	}
            
    	}
    		
    	
    	catch(DSQLParseException exception)
    	{
    		if(isAccepted) return true;
    		else return false;
    	}
        
    	//return true;
    }

    /**
     * returns next token for testing purpose only used in checkExpectedOutput
     * @param tokenList
     * @param x
     * @param size
     * @return
     * @throws DSQLParseException
     */
    private String getNextToken (ArrayList<String> tokenList,int x,int size)
    	throws DSQLParseException
    {
    	while(true)
    	{
    		
    		if(afterNelement>=size) 
    			throw new DSQLParseException("Size scanned");
    		if (tokenList.get(afterNelement).equals(" ")||tokenList.get(afterNelement).equals("")) 
    		{
    			afterNelement = afterNelement+1; 
    			continue;
    		}
    		else {
    			return tokenList.get(afterNelement++);
    		}
    		
    	}
    }
    /**
     * This method returns the next token in the list. If we are past the token
     * list it throws DSQLParseException.
     *
     * @return Next token in the list.
     *
     * @exception DSQLParseException.
     */
    private String nextToken() throws DSQLParseException {

        // check if we are past the input.
        if (myCurPos < myTokenListSize) {
            String token = myTokenList.get(myCurPos);

            myCurPos = myCurPos + 1;

            return token;
        } else {
            throw new DSQLParseException(Messages.getMessage("TBits_DSQL_PREMATURE_END"));
        }
    }

    /**
     * This method applies the date component table to the current date with
     * the given multiplier.
     *
     * @param dctable     Date Component table.
     * @param multiplier  -1 if LAST, +1 if NEXT.
     *
     * @return Resultant Date.
     */
    private String operate(Hashtable<Integer, Integer> dctable, int multiplier) {
        Calendar now  = Calendar.getInstance();
        Integer  temp = null;
        int      dc   = 0;

        dc   = Calendar.MINUTE;
        temp = dctable.get(dc);

        if (temp != null) {
            now.add(dc, multiplier * temp.intValue());
        }

        dc   = Calendar.HOUR;
        temp = dctable.get(dc);

        if (temp != null) {
            now.add(dc, multiplier * temp.intValue());
        }

        dc   = Calendar.DAY_OF_YEAR;
        temp = dctable.get(dc);

        if (temp != null) {
            now.add(dc, multiplier * temp.intValue());
        }

        dc   = Calendar.WEEK_OF_YEAR;
        temp = dctable.get(dc);

        if (temp != null) {
            now.add(dc, multiplier * temp.intValue());
        }

        dc   = Calendar.MONTH;
        temp = dctable.get(dc);

        if (temp != null) {
            now.add(dc, multiplier * temp.intValue());
        }

        dc   = Calendar.YEAR;
        temp = dctable.get(dc);

        if (temp != null) {
            now.add(dc, multiplier * temp.intValue());
        }

        Timestamp ts = Timestamp.getTimestamp(now.getTime());

        return ts.toCustomFormat("yyyy-MM-dd HH:mm:ss");
    }

    /**
     * This method initializes the process of parsing the given DSQL Query.
     *
     * @exception DSQLParseException Incase of any exceptions during parsing.
     */
    public void parse() throws DSQLParseException {
        if (myTokenListSize == 0) {
            return;
        }

        try {
            parseQuery();
        } finally {

            /*
             * Check if the argument list for free text is empty.
             * If not, prepare a parse entry and add it to the parse table.
             */
            if ((freeText != null) && (freeText.size() > 0)) {
                ParseEntry pe = new ParseEntry("alltext", Field.DESCRIPTION, 0, Connective.C_AND, freeText);

                // Add this to the parse table.
                myPrevType = ParseEntryType.DATA;
                myParseTable.add(pe);
            }

            // Check for any unmatched parenthesis and cover them.
            while (myDepth > 0) {
                ParseEntry pe = new ParseEntry();

                pe.setEntryType(ParseEntryType.NESTING_CLOSE);
                myPrevType = ParseEntryType.NESTING_CLOSE;
                myParseTable.add(pe);
                myDepth--;
            }

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

                    /*
                     * If the previous entry is not a nesting_open entry,
                     * add this to the ptable.
                     */
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

            myParseTable = ptable;
        }
    }

    /**
     * This method actually parses the argument list depending on the data type
     * of the descriptor.
     *
     * @param aDesc Descriptor name.
     *
     * @exception DSQLParseException
     */
    private ArrayList<String> parseArgList(String aDesc) throws DSQLParseException {
        ArrayList<String> argList  = new ArrayList<String>();
        int               dataType = getDataType(aDesc);

        // Call the parser corresponding to the data type of the descriptor.
        switch (dataType) {
        case BOOLEAN :
            argList = parseBooleanArg();

            break;

        case DATE :
        case TIME :
        case DATETIME :
            argList = parseDateArg();

            break;

        case INT :
        case REAL :
            argList = parseNumericArg();

            break;

        case STRING :
        case TEXT :
            argList = parseTextArg();

            break;

        case TYPE :
            argList = parseSVArg();

            break;

        case USERTYPE :
            argList = parseMVArg();

            break;
        }

        return argList;
    }

    /**
     * Method that parses the boolean argument.
     *
     * @return ArraryList<String> Single element arraylist.
     *
     * @exception DSQLParseException.
     */
    private ArrayList<String> parseBooleanArg() throws DSQLParseException {
        ArrayList<String> argList = new ArrayList<String>();
        String            token   = nextToken();

        /*
         * "0", "false", "f", "no", "n", "public" qualify as false.
         * "1", "true", "t", "yes", "y", "private" qualify as true.
         * Anything else qualifies as false.
         */
        if (token.equals("0") || token.equals("false") || token.equals("f") || token.equals("no") || token.equals("n") || token.equals("public")) {
            argList.add("0");
        } else if (token.equals("1") || token.equals("true") || token.equals("t") || token.equals("yes") || token.equals("y") || token.equals("private")) {
            argList.add("1");
        } else {
            argList.add("0");
        }

        return argList;
    }

    /**
     * This method parses a comma separated list of arguments.
     *
     * @param token     Current Token.
     * @param argList  Argument List.
     *
     * @exception DSQLParseException
     */
    private void parseCSV(String token, ArrayList<String> argList) throws DSQLParseException {
        try {
            while (token.equals(COMMA)) {
                token = nextToken();
                argList.add(token);

                if (isLast() == false) {
                    token = nextToken();
                } else {
                    break;
                }
            }

            /*
             * We will be out of the loop if we encountered a non-comma token or
             * we are at the end of the token list. In first case, put back that
             * non-comma token.
             */
            if (isLast() == true) {
                return;
            } else if (token.equals(COMMA) == false) {
                putBack();
            }
        } catch (DSQLParseException e) {
            String message = e.toString();
            String pre     = Messages.getMessage("TBits_DSQL_PREMATURE_END");

            if (message.indexOf(pre) < 0) {
                throw e;
            }
        }
    }

    /**
     * This method parses a comma separated list of arguments.
     *
     * @param token     Current Token.
     * @param argList  Argument List.
     *
     * @exception DSQLParseException
     */
    private void parseConnectiveList(String token, ArrayList<String> argList) throws DSQLParseException {
        token = nextToken();
        boolean andorNot = false; 
        if (token.equals(OR) || token.equals(AND) || token.equals(NOT)) {
            while (token.equals(OR) || token.equals(AND) || token.equals(NOT)) {
                Connective optr = Connective.C_OR;

                if (token.equals(AND)) {
                    optr = Connective.C_AND;
                } else if (token.equals(NOT)) {
                    optr = Connective.C_NOT;
                }
                
                String previous = token; 
                // Skip the next token as it would be space
                nextToken();
                

                /*
                 * The next token could be another argument to this
                 * text field or another descriptor or minus OR open brace.
                 * To confirm this we need to look at the other
                 * one too.
                 */
                token = nextToken();
                
                if(token.equals(NOT) ) 
                {
                	andorNot = true;
                	break;
                }

                if (token.equals(MINUS) || token.equals(OPEN_PAREN)) {
                    break;
                }

                if (isLast() == false) {
                    token = nextToken();

                    if (token.equals(COLON)) {
                        break;
                    } else if (token.equals(SPACE)) {
                        String opnd = prevToken(2);

                        switch (optr) {
                        case C_AND :
                            opnd = "+" + opnd;

                            break;

                        case C_NOT :
                            opnd = "-" + opnd;

                            break;
                        }

                        argList.add(opnd);
                        token = nextToken();
                    }
                } else {
                    String opnd = token;

                    switch (optr) {
                    case C_AND :
                        opnd = "+" + opnd;

                        break;

                    case C_NOT :
                        opnd = "-" + opnd;

                        break;
                    }

                    argList.add(opnd);

                    break;
                }
            }

            /*
             * Check if we are out of the while loop because we
             * are the end of the input.
             */
            if (isLast() == false) {

                /*
                 * If we are out of while loop not because that we
                 * are at the end of the input, check the reason.
                 */
                if (token.equals(COLON)) {

                    // Move back five times.
                    putBack();    // put back the colon
                    putBack();    // put back the descriptor
                    putBack();    // put back the space
                    putBack();    // put back the OR
                    putBack();    // put back the space
                } else if (token.equals(MINUS) || token.equals(OPEN_PAREN) || andorNot ) {

                    // Move back four times.
                    putBack();    // put back the minus/Open paren
                    putBack();    // put back the space
                    putBack();    // put back the OR
                    putBack();    // put back the space
                } else {

                    // Move back two times.
                    putBack();    // put back the non-OR token.
                    putBack();    // put back the space.
                }
            }
        } else {

            // Put back the token read assuming it to be a connective.
            putBack();

            // Put back the space token read before calling this method.
            putBack();
        }
    }

    /**
     * Method that parses the date argument list.
     *
     * @return ArraryList<String> Date argument list.
     *
     * @exception DSQLParseException.
     */
    private ArrayList<String> parseDateArg() throws DSQLParseException {
        mySubClause   = "";
        myDateArgList = new ArrayList<String>();

        ArrayList<String> argList = new ArrayList<String>();
        String            token   = nextToken();

        // Check if the token is a TBits Date Constant.
        if (token.equals(TODAY) || token.equals(YESTERDAY) || token.equals(LASTWEEK) || token.equals(LASTMONTH) || token.equals(LASTYEAR) || token.equals(TOMORROW)) {
            argList = getRelativeDate(token);
        }

        // Check if the token is a TBits Date Expression.
        else if (token.equals(LAST) || token.equals(NEXT)) {
            argList = parseDateExpr(token);
        }

        // Check if the argument is before a particular date.
        else if (token.equalsIgnoreCase(BEFORE)) {
            before(token, argList);
        }

        // Check if the argument is after a particular date.
        else if (token.equalsIgnoreCase(AFTER)) {
            after(token, argList);
        }

        // Check if the argument is between given range.
        else if (token.equalsIgnoreCase(BETWEEN)) {
            between(token, argList);
        } else {

            /*
             * May be the token is directly the date value itself. Put it back
             * so that getDate can read the date correctly.
             */
            putBack();
            token = "on";
            parseSingleDate(argList);
        }

        mySubClause = token;

        return argList;
    }

    /**
     * This method adds the LAST/NEXT token, parses the date expression next
     * to these tokens and returns the range in an argument list.
     *
     * @param token Operator text.
     *
     * @return argList Argument list.
     *
     * @exception DSQLParseException.
     */
    private ArrayList<String> parseDateExpr(String token) throws DSQLParseException {
        ArrayList<String> argList = new ArrayList<String>();
        String            optr    = token;

        // Next token is a space of a colon.
        token = nextToken();

        /*
         * Next token can be one of the following.
         *     '-' followed by a date expr like -2d+3w+4d-1h
         *     a date expr like 2d+4w
         */
        token = nextToken();

        StringBuilder expr = new StringBuilder();

        while (token.equals(SPACE) == false) {
            expr.append(token);

            if (isLast() == true) {
                break;
            }

            token = nextToken();
        }

        if (token.equals(SPACE)) {

            // Put back the space token read.
            putBack();
        }

        String pdate = parseExpr(optr, expr.toString());
        String cdate = (new Timestamp()).toCustomFormat("yyyy-MM-dd HH:mm:ss");

        if (optr.equals(LAST)) {
            argList.add(BETWEEN);
            argList.add(pdate);    // Start date is current date.
            argList.add(cdate);    // End date is parsed date.
        } else                     // if (optr.equals(NEXT))
        {
            argList.add(BETWEEN);
            argList.add(cdate);    // Start date is Current date
            argList.add(pdate);    // End date is Parsed date.
        }

        return argList;
    }

    /**
     * This method parses the date expressions allowed for date fields in
     * TBits.
     *
     * @param clause Can be LAST/NEXT.
     * @param expr   Date expression.
     *
     * @return Date corresponding to the expression.
     */
    private String parseExpr(String clause, String expr) {
        Hashtable<Integer, Integer> dctable = new Hashtable<Integer, Integer>();
        StringBuilder               pattern = new StringBuilder();

        pattern.append("(\\+|\\-)?([0-9]+)(").append("months|month|mon|mth|").    // Month is given precedence
            append("minutes|minute|min|m|").append("hours|hour|hr|h|").append("days|day|d|").append("weeks|week|wk|w|").append("years|year|yr|y").append(")");

        Pattern p = Pattern.compile(pattern.toString());
        Matcher m = p.matcher(expr);

        while (m.find() == true) {
            String optr  = m.group(1);
            int    value = Integer.parseInt(m.group(2));
            String dvar  = m.group(3);
            int    dc    = Calendar.MINUTE;

            if (dvar.equals("minutes") || dvar.equals("minute") || dvar.equals("min") || dvar.equals("m")) {
                dc = Calendar.MINUTE;
            } else if (dvar.equals("hours") || dvar.equals("hour") || dvar.equals("hr") || dvar.equals("h")) {
                dc = Calendar.HOUR;
            } else if (dvar.equals("days") || dvar.equals("day") || dvar.equals("d")) {
                dc = Calendar.DAY_OF_YEAR;
            } else if (dvar.equals("weeks") || dvar.equals("week") || dvar.equals("wk") || dvar.equals("w")) {
                dc = Calendar.WEEK_OF_YEAR;
            } else if (dvar.equals("months") || dvar.equals("month") || dvar.equals("mon") || dvar.equals("mth")) {
                dc = Calendar.MONTH;
            } else if (dvar.equals("years") || dvar.equals("year") || dvar.equals("yr") || dvar.equals("y")) {
                dc = Calendar.YEAR;
            }

            t(dctable, optr, value, dc);
            myDateArgList.add(value + dvar);
        }

        if (clause.equals(LAST)) {
            return operate(dctable, -1);
        }

        return operate(dctable, 1);
    }

    /**
     * This method parses the input string from the current position according
     * to productions with &lt;mvArg&gt; on LHS.
     *
     * @return ArraryList<String> Argument list.
     *
     * @exception DSQLParseException.
     */
    private ArrayList<String> parseMVArg() throws DSQLParseException {
        ArrayList<String> argList = new ArrayList<String>();
        String            token   = nextToken();

        if (token.equals(NO_EXPAND)) {
            mySubClause = MEMBER_OF;
            token       = nextToken();

            if (token.equals(COLON) || token.equals(SPACE)) {
                argList = parseMVArg();
            }

            return argList;
        }

        /*
         * By default member_of means expand.
         */
        if (token.equals(MEMBER_OF) || token.equals(MEMBERS_OF)) {
            mySubClause = EXPAND;
            token       = nextToken();

            if (token.equals(COLON) || token.equals(SPACE)) {
                argList = parseMVArg();
            }

            return argList;
        }

        if (token.equals(EXPAND)) {
            mySubClause = EXPAND;
            token       = nextToken();

            if (token.equals(COLON) || token.equals(SPACE)) {
                argList = parseMVArg();
            }

            return argList;
        } else if (token.equals(OPEN_PAREN)) {

            // <mvArg>    -> (<mvArgList>)
            ArrayList<String> temp = parseMVArgList(Connective.C_OR);

            if (temp != null) {
                argList.addAll(temp);
                temp = null;
            }

            token = nextToken();

            if (token.equals(CLOSE_PAREN) == false) {

                /*
                 * Let us not throw exception here and assume that there is a
                 * closing parenthesis.
                 */
            }
        } else {

            // <mvArg>    -> <mvSingleArg>
            argList.add(token);

            if (isLast() == false) {
                token = nextToken();

                if (token.equals(CLOSE_PAREN)) {
                    putBack();
                } else if (token.equals(COMMA)) {
                    parseCSV(token, argList);
                } else if (token.equals(SPACE)) {
                    parseConnectiveList(token, argList);
                } else {
                    putBack();
                }
            }
        }

        return argList;
    }

    /**
     * This method parses the input string from the current position according
     * to productions with &lt;mvArgList&gt; on LHS.
     *
     * @return ArraryList<String> Argument list.
     *
     * @exception DSQLParseException.
     */
    private ArrayList<String> parseMVArgList(Connective optr) throws DSQLParseException {
        ArrayList<String> argList = new ArrayList<String>();
        String            token   = nextToken();

        switch (optr) {
        case C_AND :
            token = "+" + token;

            break;

        case C_NOT :
            token = "-" + token;

            break;
        }

        argList.add(token);
        token = nextToken();

        if (token.equals(COMMA) || token.equals(SEMICOLON) || token.equals(SPACE)) {

            // <mvArgList>    -> <singleMVArg><mvIncList>
            putBack();

            ArrayList<String> temp = parseMVIncList();

            if (temp != null) {
                argList.addAll(temp);
                temp = null;
            }
        } else {

            // <mvArgList>  -> <mvSingleArg>
            putBack();
        }

        return argList;
    }

    /**
     * This method parses the input string from the current position according
     * to productions with &lt;mvIncList&gt; on LHS.
     *
     * @return ArraryList<String> Argument list.
     *
     * @exception DSQLParseException.
     */
    private ArrayList<String> parseMVIncList() throws DSQLParseException {
        ArrayList<String> argList = new ArrayList<String>();
        String            token   = nextToken();

        if (token.equals(COMMA) || token.equals(SEMICOLON)) {
            ArrayList<String> temp = parseMVArgList(Connective.C_OR);

            if (temp != null) {
                argList.addAll(temp);
                temp = null;
            }
        } else if (token.equals(SPACE)) {
            Connective optr = Connective.C_OR;

            // Check the operator.
            token = nextToken();

            if (token.equals(AND)) {
                optr = Connective.C_AND;
            } else if (token.equals(NOT)) {
                optr = Connective.C_NOT;
            }

            // Skip the space.
            token = nextToken();

            ArrayList<String> temp = parseMVArgList(optr);

            if (temp != null) {
                argList.addAll(temp);
                temp = null;
            }
        } else {
            putBack();
        }

        return argList;
    }

    /**
     * This method parses a range of numeric values or negative values.
     *
     * @param token     Current Token.
     * @param argList  Argument List.
     *
     * @exception DSQLParseException
     */
    private void parseNegativeOrRangeNumericValues(String token, ArrayList<String> argList) throws DSQLParseException {
        boolean minus = true;

        token = nextToken();
        token = checkNumericValue(token);

        if (minus == true) {
            token = "-" + token;
        }

        argList.add(IN);
        argList.add(token);

        if (isLast() == false) {
            token = nextToken();

            if (token.equals(COMMA)) {
                while (token.equals(COMMA)) {
                    minus = false;

                    if (isLast() == true) {
                        break;
                    }

                    token = nextToken();

                    if (token.equals(MINUS)) {
                        minus = true;
                        token = nextToken();
                    }

                    token = checkNumericValue(token);

                    if (minus == true) {
                        token = "-" + token;
                    }

                    argList.add(token);

                    if (isLast() == true) {
                        break;
                    }

                    token = nextToken();
                }

                if (isLast() == false) {
                    putBack();    // Put back token and return.
                }
            } else if (token.equals(MINUS)) {

                /*
                 * We are parsing the following formats.
                 *     req:10-20      OR
                 *     req:-10--20
                 */
                minus = false;
                token = nextToken();

                if (token.equals(MINUS)) {
                    minus = true;
                    token = nextToken();
                }

                token = checkNumericValue(token);

                if (minus == true) {
                    token = "-" + token;
                }

                argList.add(token);
                argList.set(0, BETWEEN);
            } else if (token.equals(SPACE)) {

                /*
                 * We are parsing the following formats.
                 *     req:10-20      OR
                 *     req:-10 --20
                 */
                token = nextToken();

                if (token.equals(MINUS)) {
                    minus = false;
                    token = nextToken();

                    if (token.equals(MINUS)) {
                        minus = true;
                        token = nextToken();
                    }

                    token = checkNumericValue(token);

                    if (minus == true) {
                        token = "-" + token;
                    }

                    argList.add(token);
                    argList.set(0, BETWEEN);
                } else {
                    putBack();    // Put back the token.
                }
            } else {
                putBack();        // Put back the token.
            }
        }
    }

    /**
     * This method parses the integer argument.
     *
     * @return ArraryList<String> Integer argument list.
     *
     * @exception DSQLParseException.
     */
    private ArrayList<String> parseNumericArg() throws DSQLParseException {
        ArrayList<String> argList = new ArrayList<String>();
        String            token   = nextToken();

        // Check if the token is an operator.
        if (token.equals(LT) || token.equals(LE) || token.equals(GT) || token.equals(GE)) {

            /*
             * we are parsing
             *    req: > 10 OR
             *    req: < 10 OR
             *    req: >= 10 OR
             *    req: <= 10
             */
            boolean minus = false;

            argList.add(token);
            token = nextToken();

            if (token.equals(MINUS)) {
                minus = true;
            }

            token = nextToken();
            token = checkNumericValue(token);

            if (minus == true) {
                token = "-" + token;
            }

            argList.add(token);
        } else if (token.startsWith(LE)) {

            // req: <=10 OR
            argList.add(LE);
            token = token.substring(2);
            argList.add(checkNumericValue(token));
        } else if (token.startsWith(LT)) {

            // req: <10 OR
            argList.add(LT);
            token = token.substring(1);
            argList.add(checkNumericValue(token));
        } else if (token.startsWith(GE)) {

            // req: >=10 OR
            argList.add(GE);
            token = token.substring(2);
            argList.add(checkNumericValue(token));
        } else if (token.startsWith(GT)) {

            // req: >10 OR
            argList.add(GT);
            token = token.substring(1);
            argList.add(checkNumericValue(token));
        } else if (token.equals(MINUS)) {

            /*
             * We are parsing one of the following formats.
             *    - req: -10
             *    - req: -10-20
             *    - req: -10--20
             *    - req: -10, 20, 30
             */
            parseNegativeOrRangeNumericValues(token, argList);
        } else if (token.equals(OPEN_SQUARE)) {

            /*
             * We are parsing one of the following formats.
             *    - req: [10, 20]
             *    - req: [10, -20]
             *    - req: [-10, 20]
             *    - req: [-10, -20]
             */
            parseNumericRange(token, argList);
        } else if (token.equals(OPEN_PAREN)) {

            /*
             * We are parsing one of the following formats.
             *    - req: (10, 20)
             *    - req: (10)
             */
            token = nextToken();
            parseNumericCSV(token, argList);
            token = nextToken();

            if (token.equals(CLOSE_PAREN)) {

                // So far, so good.
            } else {
                putBack();    // Going to fail somewhere.
            }
        } else {

            /*
             * We are parsing one of the following formats.
             *   - req: 10
             *   - req: 10,20
             *   - req: 10,20
             *   - req: -10,20
             */
            parseNumericCSV(token, argList);
        }

        return argList;
    }

    /**
     * This method parses a single numeric value or a comma separated list of
     * numeric values
     *
     * @param token     Current Token.
     * @param argList  Argument List.
     *
     * @exception DSQLParseException
     */
    private void parseNumericCSV(String token, ArrayList<String> argList) throws DSQLParseException {

        /*
         * We are parsing for following formats.
         *   - req: 10
         *   - req: 10,20,30
         *   - req: 10,-20,30
         *   - req: 10-20
         *   - req: 10--20
         *   - req:(-10,20, 30)
         */
        boolean minus = false;

        if (token.equals(MINUS)) {
            minus = true;
            token = nextToken();
        }

        token = checkNumericValue(token);

        if (minus == true) {
            token = "-" + token;
        }

        argList.add(IN);
        argList.add(token);

        if (isLast() == false) {
            token = nextToken();

            if (token.equals(COMMA)) {
                while (token.equals(COMMA)) {
                    minus = false;

                    if (isLast() == true) {
                        break;
                    }

                    token = nextToken();

                    if (token.equals(MINUS)) {
                        minus = true;
                        token = nextToken();
                    }

                    token = checkNumericValue(token);

                    if (minus == true) {
                        token = "-" + token;
                    }

                    argList.add(token);

                    if (isLast() == true) {
                        break;
                    }

                    token = nextToken();
                }

                /*
                 * If we came out not because of end of input, put back the
                 * token.
                 */
                if (isLast() == false) {
                    putBack();    // Put back the token and return.

                    /*
                     * If we came out because of end of input, put back the token
                     * if we read the closing paren.
                     */
                } else if (token.equals(CLOSE_PAREN) == true) {
                    putBack();
                }
            } else if (token.equals(MINUS)) {
                minus = false;
                token = nextToken();

                if (token.equals(MINUS)) {
                    minus = true;
                    token = nextToken();
                }

                token = checkNumericValue(token);

                if (minus == true) {
                    token = "-" + token;
                }

                argList.add(token);
                argList.set(0, BETWEEN);
            } else if (token.equals(SPACE)) {
                token = nextToken();

                if (token.equals(MINUS)) {
                    minus = false;
                    token = nextToken();

                    if (token.equals(MINUS)) {
                        minus = true;
                        token = nextToken();
                    }

                    try {
                        token = checkNumericValue(token);
                    } catch (DSQLParseException de) {

                        /*
                         * we are trying to parse another entry.
                         * Put back the descriptor.
                         */
                        putBack();

                        // put back the minus token.
                        putBack();

                        // putback the space token.
                        putBack();

                        return;
                    }

                    if (minus == true) {
                        token = "-" + token;
                    }

                    argList.add(token);
                    argList.set(0, BETWEEN);
                } else {
                    putBack();    /*
                                   *  Put back the token read assuming it would be
                                   * a minus.
                                   */
                    putBack();    // Put back the space token.
                }
            } else {
                putBack();        // Put back the token and return.
            }
        }
    }

    /**
     * This method parses a range of numeric values.
     *
     * @param token     Current Token.
     * @param argList  Argument List.
     *
     * @exception DSQLParseException
     */
    private void parseNumericRange(String token, ArrayList<String> argList) throws DSQLParseException {
        boolean minus = false;

        /*
         * Format is [start_value, end_value]
         * e.g.
         *     req:[10, 20]
         *     req:[19, -29]
         *     req:[-19, 29]
         *     req:[-19, -29]
         */
        argList.add(BETWEEN);
        token = nextToken();

        // Check if this is minus.
        if (token.equals(MINUS)) {

            // Set minus to true and read the next token.
            minus = true;
            token = nextToken();
        }

        token = checkNumericValue(token);

        if (minus == true) {
            token = "-" + token;
        }

        // add the start value to the list.
        argList.add(token);
        minus = false;

        // Skip the next token. It could be a comma or a space.
        token = nextToken();
        token = nextToken();

        // Check if this is minus.
        if (token.equals(MINUS)) {
            minus = true;
            token = nextToken();
        }

        token = checkNumericValue(token);

        if (minus == true) {
            token = "-" + token;
        }

        // add the end value to the list.
        argList.add(token);

        // Next token should be a closing square bracket.
        token = nextToken();

        if (token.equals(CLOSE_SQUARE)) {

            // So far, so good.
        } else {
            putBack();
        }
    }

    /**
     * This method parses a comma separated list of arguments.
     *
     * @param token     Current Token.
     * @param argList  Argument List.
     *
     * @exception DSQLParseException
     */
    private void parseORList(String token, ArrayList<String> argList) throws DSQLParseException {

        // Next token expected is OR.
        token = nextToken();

        if (token.equals(OR)) {
            while (token.equals(OR)) {

                // Skip the next token as it would be space
                nextToken();

                /*
                 * The next token could be another argument to this type field
                 * or another descriptor or negation. To confirm this we need
                 * to look at the other one too.
                 */
                token = nextToken();

                if (token.equals(MINUS) || token.equals(OPEN_PAREN)) {
                    break;
                } else if (isLast() == false) {
                    token = nextToken();

                    if (token.equals(COLON)) {
                        break;
                    } else if (token.equals(SPACE)) {
                        argList.add(prevToken(2));
                        token = nextToken();
                    }
                } else {
                    argList.add(token);

                    break;
                }
            }

            /*
             * Check if we are out of the while loop because we
             * are the end of the input.
             */
            if (isLast() == false) {

                /*
                 * If we are out of while loop not because that we
                 * are at the end of the input, check the reason.
                 */
                if (token.equals(COLON)) {
                    putBack();    // put back the colon
                    putBack();    // put back the descriptor
                    putBack();    // put back the space
                    putBack();    // put back the OR
                    putBack();    // put back the space
                } else if (token.equals(MINUS) || token.equals(OPEN_PAREN)) {
                    putBack();    // put back the minus or open paren.
                    putBack();    // put back the space
                    putBack();    // put back the OR
                    putBack();    // put back the space
                } else {
                    putBack();    // put back the non-OR token.
                    putBack();    // put back the space.
                }
            }
        } else {

            // Put back the token read assuming it to be an OR.
            putBack();

            // Put back the space read before calling this method.
            putBack();
        }
    }

    /**
     * This method parses the input using the production with query on LHS.
     *
     * @exception DSQLParseException.
     */
    private void parseQuery() throws DSQLParseException {
        String token = nextToken();

        if (token.equals(OPEN_PAREN)) {

            // <query> -> (<query>)
            myDepth++;
            notCount = 0;
            // Add a nesting open entry. and start parsing from there.
            ParseEntry pe = new ParseEntry();

            pe.setEntryType(ParseEntryType.NESTING_OPEN);
            pe.setConnective(myCOptr);
            myPrevType = ParseEntryType.NESTING_OPEN;
            myParseTable.add(pe);

            // Reset the COptr value to C_AND
            myCOptr = Connective.C_AND;
            parseQuery();

            try {

                // Check if the next token is a closing paren.
                token = nextToken();

                if (token.equals(CLOSE_PAREN)) {
                    myDepth--;

                    ParseEntry tmp = new ParseEntry();

                    tmp.setEntryType(ParseEntryType.NESTING_CLOSE);
                    myPrevType = ParseEntryType.NESTING_CLOSE;
                    myParseTable.add(tmp);
                } else {

                    /*
                     * Put the back the token which was read assuming it to be
                     * closing paren.
                     */
                    putBack();
                }
            } catch (DSQLParseException dsqle) {
                String message = dsqle.toString().toLowerCase();
                String pre     = Messages.getMessage("TBits_DSQL_PREMATURE_END");

                if (message.indexOf(pre) >= 0) {

                    // Actually, a closing paren is expected here.
                } else {
                    throw dsqle;
                }
            }
        } else if (token.equals(NEGATION) || token.equals(NOT)) {

            /*
             * <query> -> -query  OR
             * <query> -> NOT query
             */
            if (token.equals(NOT)) {

                // read out that space after NOT.
                nextToken();
            }

            /*
             * If we just started parsing or we are at the beginning of another
             * nesting, then do not associate this NOT with any thing.
             */
            
                     
            if (myParseTable.size() == 0 || myPrevType == ParseEntryType.NESTING_OPEN) {
            	notCount++;
            	if(notCount%2 ==0) myCOptr = Connective.C_NOT_NOT;
            	else myCOptr = Connective.C_NOT;
            	/*if(myCOptr == Connective.C_NOT) 
            		myCOptr = Connective.C_NOT_NOT;
            	else
            		myCOptr = Connective.C_NOT;*/
            }
            
            else {
                if (myCOptr == Connective.C_AND) {
                    myCOptr = Connective.C_AND_NOT;
                } else if (myCOptr == Connective.C_OR) {
                    myCOptr = Connective.C_OR_NOT;
                } else if (myCOptr == Connective.C_NOT) {
                    myCOptr = Connective.C_NOT_NOT;
                }else if (myCOptr == Connective.C_NOT_NOT){
                	myCOptr = Connective.C_NOT ;
                }
            }

            parseQuery();
        } else {

            // <query> -> desc:arglist
            String desc = token;

            try {
                token = nextToken();

                if (token.equals(COLON)) {
                    if (isLast() == true) {

                        /*
                         * Add the token that matched the descriptor to the
                         * free text list.
                         */
                        freeText.add(desc);
                    } else {

                        /*
                         * Parse the argument list next to the descriptor and
                         * add it to the parse table.
                         */
                        ArrayList<String> argList = parseArgList(desc);

                        addToParseTable(desc, argList);
                    }
                } else {

                    /*
                     * The token that is matched to be a descriptor will be
                     * treated as free text and added to the freeText list.
                     */
                    try {
                        freeText.add(desc);

                        // Put back the token we read expecting a colon.
                        putBack();
                    } catch (Exception e) {
                        LOG.severe("",(e));
                    }
                }
            } catch (DSQLParseException dsqle) {
                String msg       = dsqle.toString().toLowerCase();
                String searchStr = Messages.getMessage("TBits_DSQL_PREMATURE_END").toLowerCase();

                if (msg.indexOf(searchStr) > 0) {

                    /*
                     * The token that is matched to be a descriptor will be
                     * treated as free text and added to the freeText list.
                     * putBack and read it again just to be sure that token
                     * holds the correct value.
                     */
                    putBack();
                    token = nextToken();
                    freeText.add(token);
                } else {
                    throw dsqle;
                }
            }
        }

        checkEnd();
    }

    /**
     * This method parses the input string from the current position to get
     * a list of single valued arguments.
     *
     * @return ArraryList<String> Single Value argument list.
     *
     * @exception DSQLParseException.
     */
    private ArrayList<String> parseSVArg() throws DSQLParseException {
        ArrayList<String> argList = new ArrayList<String>();
        String            token   = nextToken();

        if (token.equals(OPEN_PAREN)) {

            // <svArg>    -> (<svArgList>)
            ArrayList<String> temp = parseSVArgList();

            if (temp != null) {
                argList.addAll(temp);
                temp = null;
            }

            token = nextToken();

            if (token.equals(CLOSE_PAREN) == false) {

                /*
                 * Let us not throw exception here and assume that there is a
                 * closing parenthesis.
                 */
            }
        } else {

            // <svArg>    -> <svSingleArg>
            argList.add(token);

            if (isLast() == false) {
                token = nextToken();

                if (token.equals(CLOSE_PAREN)) {
                    putBack();
                } else if (token.equals(COMMA)) {
                    parseCSV(token, argList);
                } else if (token.equals(SPACE)) {
                    parseORList(token, argList);
                } else {
                    putBack();
                }
            }
        }

        return argList;
    }

    /**
     * This method parses the input string from the current position according
     * to productions with &lt;svArgList&gt; on LHS.
     *
     * @return ArraryList<String> Argument list.
     *
     * @exception DSQLParseException.
     */
    private ArrayList<String> parseSVArgList() throws DSQLParseException {
        ArrayList<String> argList = new ArrayList<String>();
        String            token   = nextToken();

        argList.add(token);
        token = nextToken();

        if (token.equals(COMMA) || token.equals(SPACE)) {

            // <svArgList>    -> <singleSVArg><svIncList>
            putBack();

            ArrayList<String> temp = parseSVIncList();

            if (temp != null) {
                argList.addAll(temp);
                temp = null;
            }
        } else {

            // <svArgList>  -> <svSingleArg>
            putBack();
        }

        return argList;
    }

    /**
     * This method parses the input string from the current position according
     * to productions with &lt;svIncList&gt; on LHS.
     *
     * @return ArraryList<String> Argument list.
     *
     * @exception DSQLParseException.
     */
    private ArrayList<String> parseSVIncList() throws DSQLParseException {
        ArrayList<String> argList = new ArrayList<String>();
        String            token   = nextToken();

        if (token.equals(COMMA)) {
            ArrayList<String> temp = parseSVArgList();

            if (temp != null) {
                argList.addAll(temp);
                temp = null;
            }
        } else if (token.equals(SPACE)) {

            // Skip the operator.
            token = nextToken();

            // Skip the space.
            token = nextToken();

            ArrayList<String> temp = parseSVArgList();

            if (temp != null) {
                argList.addAll(temp);
                temp = null;
            }
        } else {
            putBack();
        }

        return argList;
    }

    /**
     * This method parses the single date.
     *
     * @param argList
     *            Argument List.
     *
     * @exception DSQLParseException.
     */
    private void parseSingleDate(ArrayList<String> argList) throws DSQLParseException {
        String        token       = nextToken();
        StringBuilder start       = new StringBuilder();
        StringBuilder end         = new StringBuilder();
        StringBuilder datePattern = new StringBuilder();

        if (token.matches(YYYYMMDD)) {
            datePattern.append("yyyyMMdd");
        } else if (token.matches(MM_DD_YYYY)) {
            datePattern.append("MM/dd/yyyy");
        } else {
            String tmp = nextToken();

            if (tmp.equals(MINUS)) {
                token = token +                 // Year
                    tmp +                       // hyphen
                        nextToken() +           // Month
                            nextToken() +       // hyphen
                                nextToken();    // Date;
                datePattern.append("yyyy-MM-dd");
            } else {
                StringBuilder message = new StringBuilder();

                throw new DSQLParseException(Messages.getMessage("TBits_DSQL_SUPPORTED_DATEFORMATS"));
            }
        }

        start.append(token);
        end.append(token);

        if (isLast() == false) {

            // The next token is a space or a closing square bracket
            token = nextToken();

            if (token.equals(CLOSE_SQUARE)) {

                // Append the time part based on "start".
                datePattern.append(" HH:mm:ss");
                start.append(" 00:00:00");
                end.append(" 23:59:59");

                // Put the back the token if it is close square.
                putBack();
            } else {
                token = nextToken();

                // See if this token matches the time pattern.
                if (token.matches(HH_MM)) {
                    start.append(" ").append(token).append(":00");
                    end.append(" ").append(token).append(":59");
                    datePattern.append(" HH:mm:ss");

                    if (isLast() == false) {

                        // the next token is space or closing square bracket.
                        token = nextToken();

                        if (token.equals(CLOSE_SQUARE)) {

                            // Put back the closing square brace.
                            putBack();
                        } else {
                            token = nextToken();

                            if (token.matches(ZONE_PATTERN)) {
                                start.append(" ").append(token.toUpperCase());
                                end.append(" ").append(token.toUpperCase());
                                datePattern.append(" ZZZ");
                            } else {

                                /*
                                 * Put back the token read assuming it to be
                                 * zone value.
                                 */
                                putBack();

                                /*
                                 * Put back the token read assuming it to be
                                 * a space.
                                 */
                                putBack();
                            }
                        }
                    }
                } else if (token.matches(HH_MM_SS)) {
                    start.append(" ").append(token);
                    end.append(" ").append(token);
                    datePattern.append(" HH:mm:ss");

                    if (isLast() == false) {

                        // the next token is space or closing square bracket.
                        token = nextToken();

                        if (token.equals(CLOSE_SQUARE)) {

                            // Put back the closing square brace.
                            putBack();
                        } else {
                            token = nextToken();

                            if (token.matches(ZONE_PATTERN)) {
                                String zone = token.toUpperCase();

                                start.append(" ").append(zone);
                                end.append(" ").append(zone);
                                datePattern.append(" ZZZ");
                            } else {

                                /*
                                 * Put back the token read assuming it to be
                                 * zone value.
                                 */
                                putBack();

                                /*
                                 * Put back the token read assuming it to be
                                 * a space.
                                 */
                                putBack();
                            }
                        }
                    }
                }

                // See if this token matches the Zone pattern.
                else if (token.matches(ZONE_PATTERN)) {
                    String zone = token.toUpperCase();

                    datePattern.append(" HH:mm:ss ZZZ");
                    start.append(" 00:00:00 ").append(zone);
                    end.append(" 23:59:59 ").append(zone);
                } else {
                    datePattern.append(" HH:mm:ss");
                    start.append(" 00:00:00");
                    end.append(" 23:59:59");

                    // Put back the token read assuming it to be time.
                    putBack();

                    // Put back the token read assuming it to be space.
                    putBack();
                }
            }
        } else {
            datePattern.append(" HH:mm:ss");
            start.append(" 00:00:00");
            end.append(" 23:59:59");
        }

        String pattern = datePattern.toString();

        mySubClause = "on";
        myDateArgList.add(start.toString());
        argList.add(BETWEEN);
        argList.add(toStandardFormat(start.toString(), pattern));
        argList.add(toStandardFormat(end.toString(), pattern));

        return;
    }

    /**
     * This method parses the input string from the current position according
     * to productions with &lt;textArg&gt; on LHS.
     *
     * @return ArraryList<String> Argument list.
     *
     * @exception DSQLParseException.
     */
    private ArrayList<String> parseTextArg() throws DSQLParseException {
        ArrayList<String> argList = new ArrayList<String>();
        String            token   = nextToken();

        if (token.equals(OPEN_PAREN)) {

            // <textArg>    -> (<textArgList>)
            ArrayList<String> temp = parseTextArgList(Connective.C_OR);

            if (temp != null) {
                argList.addAll(temp);
                temp = null;
            }

            token = nextToken();

            if (token.equals(CLOSE_PAREN) == false) {

                /*
                 * Let us not throw exception here and assume that there is a
                 * closing parenthesis.
                 */
            }
        } else if (token.equals(QUOTE)) {
            token = nextToken();
            argList.add(token);

            // This is closing quote.
            token = nextToken();
        } else {

            // <textArg>    -> <svSingleArg>
            argList.add(token);

            if (isLast() == false) {
                token = nextToken();

                if (token.equals(CLOSE_PAREN)) {
                    putBack();
                } else if (token.equals(COMMA)) {
                    parseCSV(token, argList);
                } else if (token.equals(SPACE)) {
                    parseConnectiveList(token, argList);
                } else {

                    // Put back this unknown token.
                    putBack();
                }
            }
        }

        return argList;
    }

    /**
     * This method parses the input string from the current position according
     * to productions with &lt;textArgList&gt; on LHS.
     *
     * @return ArraryList<String> Argument list.
     *
     * @exception DSQLParseException.
     */
    private ArrayList<String> parseTextArgList(Connective optr) throws DSQLParseException {
        ArrayList<String> argList = new ArrayList<String>();
        String            token   = nextToken();

        switch (optr) {
        case C_AND :
            token = "+" + token;

            break;

        case C_NOT :
            token = "-" + token;

            break;
        }

        argList.add(token);
        token = nextToken();

        if (token.equals(COMMA) || token.equals(SPACE)) {

            // <textArgList>    -> <singleTextArg><textIncList>
            putBack();

            ArrayList<String> temp = parseTextIncList();

            if (temp != null) {
                argList.addAll(temp);
                temp = null;
            }
        } else {

            // <textArgList>  -> <textSingleArg>
            putBack();
        }

        return argList;
    }

    /**
     * This method parses the input string from the current position according
     * to productions with &lt;textIncList&gt; on LHS.
     *
     * @return ArraryList<String> Argument list.
     *
     * @exception DSQLParseException.
     */
    private ArrayList<String> parseTextIncList() throws DSQLParseException {
        ArrayList<String> argList = new ArrayList<String>();
        String            token   = nextToken();

        if (token.equals(COMMA)) {
            ArrayList<String> temp = parseTextArgList(Connective.C_OR);

            if (temp != null) {
                argList.addAll(temp);
                temp = null;
            }
        } else if (token.equals(SPACE)) {
            Connective optr = Connective.C_OR;

            // Check the operator.
            token = nextToken();

            if (token.equals(AND)) {
                optr = Connective.C_AND;
            } else if (token.equals(NOT)) {
                optr = Connective.C_NOT;
            }

            // Skip the space.
            token = nextToken();

            ArrayList<String> temp = parseTextArgList(optr);

            if (temp != null) {
                argList.addAll(temp);
                temp = null;
            }
        } else {
            putBack();
        }

        return argList;
    }

    /**
     * This method returns the token at aPrev position towards left of the
     * current position.
     *
     * @return the previous token to the current one.
     *
     * @exception DSQLParseException.
     */
    private String prevToken(int aPrev) throws DSQLParseException {

        // Check for underflow.
        if ((myCurPos - aPrev) <= 0) {
            return myTokenList.get(0);
        } else {
            return myTokenList.get(myCurPos - aPrev);
        }
    }

    /**
     * Method to print the ParseTable.
     *
     * @return String representation of the parse table.
     */
    public String printParseTable() {
        StringBuilder buffer = new StringBuilder();

        buffer.append("ParseTable: \n");

        int ctr = 1;

        for (ParseEntry pe : myParseTable) {
            buffer.append(ctr++).append(". ").append(pe.toString());
        }

        return buffer.toString();
    }

    /**
     * Method to print the token list.
     *
     * @return String representation of the token list.
     */
    public String printTokenList() {
        StringBuilder buffer = new StringBuilder();

        buffer.append("TokenList: \n");

        for (String token : myTokenList) {
            buffer.append(" '").append(token).append("' ");
        }

        return buffer.toString();
    }

    /**
     * This method processes the input by removing the redundant spaces.
     *
     */
    private void processInput() {
        myProcInput = ((myInput == null)
                       ? ""
                       : myInput);

        // convert the input to lowercase.
        myProcInput = myProcInput.toLowerCase().trim();

        // Replace multiple spaces with a single one.
        myProcInput = myProcInput.replaceAll("[ ]+", " ");

        // Remove the spaces preceeding ':' character.
        myProcInput = myProcInput.replaceAll("[ ]*:", ":");

        // Remove the spaces after ':' character.
        // myProcInput = myProcInput.replaceAll(":[ ]*", ":");
        // Remove the spaces surrounding ',' character.
        myProcInput = myProcInput.replaceAll("[ ]?,[ ]?", ",");

        // Remove the spaces surrounding ';' character.
        myProcInput = myProcInput.replaceAll("[ ]?;[ ]?", ";");

        // Replace one or more spaces following '(' with a single one.
        myProcInput = myProcInput.replaceAll("\\([ ]?", "(");

        // Replace one or more spaces following '[' with a single one.
        myProcInput = myProcInput.replaceAll("\\[[ ]?", "[");

        // Remove the spaces preceeding ']'
        myProcInput = myProcInput.replaceAll("[ ]?\\]", "]");

        // Remove the spaces preceeding ')'
        myProcInput = myProcInput.replaceAll("[ ]?\\)", ")");

        // Remove one or more spaces following '-'.
        // myProcInput = myProcInput.replaceAll("-[ ]?", "-");
        // Remove the spaces between two successive hyphens.
        myProcInput = myProcInput.replaceAll("-[ ]*-", "--");

        // Remove the spaces between hyphen and integers.
        myProcInput = myProcInput.replaceAll("-[ ]*([0-9])", "-" + "$1");
    }

    /**
     * This method moves the cursor back by one step.
     *
     * @exception DSQLParseException.
     */
    private void putBack() throws DSQLParseException {
        if (myCurPos == 0) {
            return;
        } else {
            myCurPos = myCurPos - 1;
        }
    }

    /**
     * This method updates the given date component table based on the
     * specified operators and values.
     *
     * @param dctable Date component table.
     * @param optr    Operator.
     * @param value   Integer value.
     * @param dc      Date component to be operated on.
     */
    private void t(Hashtable<Integer, Integer> dctable, String optr, int value, int dc) {
        Integer temp     = dctable.get(dc);
        int     oldValue = 0;

        if (temp != null) {
            oldValue = temp.intValue();
        }

        if (optr == null) {
            value = value + oldValue;
        } else if (optr.equals(PLUS)) {
            value = oldValue + value;
        } else if (optr.equals(MINUS)) {
            value = oldValue - value;;
        }

        dctable.put(dc, value);

        return;
    }

    private String timePart(boolean start) {
        return ((start == true)
                ? "00:00:00"
                : "23:59:59");
    }

    /**
     * This method takes the date value and the pattern and converts it into
     * standard pattern.
     *
     * @param value    date value.
     * @param pattern  date pattern.
     *
     * @return Date in yyyy-MM-dd HH:mm:ss format is returned.
     */
    private String toStandardFormat(String value, String pattern) {
        try {
            value = validateDate(value, pattern);

            /*
             * Date returned by validateDate will always be in
             * yyyy-MM-dd HH:mm:ss format.
             */
            pattern = "yyyy-MM-dd HH:mm:ss";

            SimpleDateFormat sdf = new SimpleDateFormat(pattern);
            Date             d   = sdf.parse(value);
            Calendar         cal = Calendar.getInstance();

            cal.setTime(d);

            /*
             * Year can be specified in two-pad or four-pad format.
             * Check that and set it accordingly.
             */
            int year = cal.get(Calendar.YEAR);

            if (year < 80) {
                year = year + 2000;
            } else if ((year >= 80) && (year < 100)) {
                year = year + 1900;
            }

            cal.set(Calendar.YEAR, year);

            return Timestamp.getTimestamp(cal.getTime()).toCustomFormat("yyyy-MM-dd HH:mm:ss");
        } catch (Exception e) {
            LOG.warn("\nValue: " + value + "\nPattern: " + pattern + "\n" + "",(e));

            Timestamp ts = new Timestamp();

            return ts.toCustomFormat("yyyy-MM-dd HH:mm:ss");
        }
    }

    /**
     * This method takes the date part from the given timestamp and appends
     * starting or ending time of the day based on the value of start.
     *
     * @param ts     Timestamp
     * @param start  If true, start time is appended to the date.
     *               If false, end time is appended to the date.
     *
     * @return Date in yyyy-MM-dd HH:mm:ss format is returned.
     */
    private String toStandardFormat(Timestamp ts, boolean start) {
        if (ts == null) {
            ts = new Timestamp();
        }

        StringBuilder value = new StringBuilder();

        value.append(ts.toCustomFormat("yyyy-MM-dd")).append((start == true)
                ? " 00:00:00"
                : " 23:59:59");

        return value.toString();
    }

    /**
     * Method that tokenizes the processed input.
     */
    private void tokenizeInput() {
        myTokenList = new ArrayList<String>();

        byte[]        chars  = myProcInput.getBytes();
        StringBuilder buffer = new StringBuilder();

        for (int i = 0; i < chars.length; i++) {
            byte ch = chars[i];

//            if ((ch == '(') || ( // Opening brace.
//					ch == ')') || ( // Closing brace.
//					ch == '[') || ( // Opening Square Bracket.
//					ch == ']') || ( // Closing Square Bracket.
//					ch == ',') || ( // Comma separator.
//					ch == ';') || ( // Comma separator.
//					ch == '-') || ( // Hyphen.
//					ch == ' ' // Space.
//					)) 
            
            if("()[],;- ".indexOf(ch) >= 0)
            {

                /*
				 * if the buffer is not empty, then add the buffer content to
				 * the tokens list and reinitialize it.
				 */
                if (buffer.toString().equals("") == false) {
                    myTokenList.add(buffer.toString());
                    buffer = new StringBuilder();
                }

                // Add the character to token list.
                myTokenList.add("" + (char) ch);

                if (ch == '-')    // Hyphen
                {
                    i++;

                    // Ignore all the spaces following this.
                    while ((i < chars.length) && (ch = chars[i]) == ' ') {
                        i++;
                    }

                    if (i != chars.length) {
                        i--;
                    }
                }

                continue;
            } else if (ch == ':') {
                if (i != 0) {
                    if ((chars[i - 1] < '0') || (chars[i - 1] > '9')) {

                        /*
                         * if the buffer is not empty, then add the buffer
                         * content to the tokens list and reinitialize it.
                         */
                        if (buffer.toString().equals("") == false) {
                            myTokenList.add(buffer.toString());
                            buffer = new StringBuilder();
                        }

                        // Add the character to token list.
                        myTokenList.add("" + (char) ch);
                    } else {
                        buffer.append((char) ch);
                    }
                } else {

                    /*
                     * if the buffer is not empty, then add the buffer
                     * content to the tokens list and reinitialize it.
                     */
                    if (buffer.toString().equals("") == false) {
                        myTokenList.add(buffer.toString());
                        buffer = new StringBuilder();
                    }

                    // Add the character to token list.
                    myTokenList.add("" + (char) ch);
                }

                i++;

                // Ignore all the spaces following this.
                while ((i < chars.length) && (ch = chars[i]) == ' ') {
                    i++;
                }

                if (i != chars.length) {
                    i--;
                }

                continue;
            }

            if (ch == '\"') {

                /*
                 * if the buffer is not empty, then add the buffer content to
                 * the tokens list and reinitialize it.
                 */
                if (buffer.toString().equals("") == false) {
                    myTokenList.add(buffer.toString());
                    buffer = new StringBuilder();
                }

                // Add the character to token list.
                i++;

                while ((i < chars.length) && (ch = chars[i]) != '\"') {
                    ch = chars[i];
                    buffer.append((char) ch);
                    i++;
                }

                if (buffer.toString().equals("") == false) {
                    myTokenList.add(buffer.toString());
                    buffer = new StringBuilder();
                }

                continue;
            }

            // Add the characters to token buffer.
            buffer.append((char) ch);
        }

        /*
         * We are done with the scanning of input. Check if the buffer has
         * anything left. if so, add it to the tokens list.
         */
        if (buffer.toString().equals("") == false) {
            myTokenList.add(buffer.toString());
        }
        
        //to solve errors of type not(...) , add entry action:>0 (ie universal 
        // set of requests)
        
        int index = -1;
        for(String str: myTokenList)
        {
        	index++;
        	if(str.equalsIgnoreCase("NOT"))
        	{
        		myTokenList.add(index,"request");
        		myTokenList.add(index+1,":");
        		myTokenList.add(index+2,">0");
        		myTokenList.add(index+3," ");
        	 	break;
        	}
        	else if(str.equals("("))
        		continue;
        	else break;
        }
        
        /*
         * Now concatenate all the elements in TokenList to get the effective
         * query we would be parsing.
         */
        buffer = new StringBuilder();

        for (String str : myTokenList) {
            buffer.append(str);
        }

        myProcInput = buffer.toString().trim();

        return;
    }

    /**
     *
     * @param value
     * @param pattern
     * @return
     */
    private String validateDate(String value, String pattern) {
        StringBuilder validDate = new StringBuilder();
        Calendar      today     = Calendar.getInstance();

        /*
         * Our aim here is to get the individual parts of the date and validate
         * for correctness. If any of the above conditions fail, we correct that
         * date part.
         *      (1) Date cannot be greater than 31
         *              - set date to 31 if condition fails.
         *      (2) Month cannot be greater than 12
         *              - set month to 12 if condition fails.
         *      (3) Date cannot be greater than 30 for months 2, 4, 6, 9, 11
         *             - set date to 30 if condition fails.
         *      (4) Date cannot be greater than 28 for febraury in a non-leap
         *          year
         *              - set date to 28 if condition fails.
         *      (5) Date cannot be greater than 29 for febraury in a leap year
         *              - set date to 29 if condition fails.
         */
        String year  = "";
        String month = "";
        String day   = "";
        String date  = "";
        String time  = "";

        /*
         * Split on space first to get the date and time parts separately.
         */
        StringTokenizer dt = new StringTokenizer(value, " ");

        if (dt.hasMoreTokens() == true) {
            date = dt.nextToken();
        }

        if (dt.hasMoreTokens() == true) {
            time = dt.nextToken();
        }

        /*
         * Now split the date part based on its pattern and get the values of
         * year/month/day.
         */
        if (pattern.indexOf("/") > 0)           // MM/dd/yyyy format.
        {
            StringTokenizer st = new StringTokenizer(date, "/");

            if (st.hasMoreTokens() == true) {
                month = st.nextToken();
            }

            if (st.hasMoreTokens() == true) {
                day = st.nextToken();
            }

            if (st.hasMoreTokens() == true) {
                year = st.nextToken();
            }
        } else if (pattern.indexOf("-") > 0)    // yyyy-MM-dd format.
        {
            StringTokenizer st = new StringTokenizer(date, "-");

            if (st.hasMoreTokens() == true) {
                year = st.nextToken();
            }

            if (st.hasMoreTokens() == true) {
                month = st.nextToken();
            }

            if (st.hasMoreTokens() == true) {
                day = st.nextToken();
            }
        } else                                  // yyyyMMdd format.
        {
            year  = date.substring(0, 4);
            month = date.substring(4, 6);
            day   = date.substring(6, 8);
        }

        int iYear  = 0;
        int iMonth = 0;
        int iDay   = 0;

        try {
            iYear = Integer.parseInt(year);
        } catch (NumberFormatException nfe) {
            LOG.severe("An exception occurred while parsing the year: " + year, nfe);
            iYear = today.get(Calendar.YEAR);
        }

        try {
            iMonth = Integer.parseInt(month);
        } catch (NumberFormatException nfe) {
            LOG.severe("An exception occurred while parsing the month: " + month, nfe);
            iMonth = today.get(Calendar.MONTH);
        }

        try {
            iDay = Integer.parseInt(day);
        } catch (NumberFormatException nfe) {
            LOG.severe("An exception occurred while parsing the day: " + day, nfe);
            iDay = today.get(Calendar.DAY_OF_MONTH);
        }

        if (iYear < 80) {
            iYear = iYear + 2000;
        } else if ((iYear >= 80) && (iYear < 100)) {
            iYear = iYear + 1900;
        }

        if (iDay > 31) {
            iDay = 31;
        }

        if (iDay < 1) {
            iDay = 1;
        }

        if (iMonth > 12) {
            iMonth = 12;
        }

        if (iMonth < 1) {
            iMonth = 1;
        }

        if ((iDay > 30) && ((iMonth == 2) || (iMonth == 4) || (iMonth == 6) || (iMonth == 9) || (iMonth == 11))) {
            iDay = 30;
        }

        if (iMonth == 2) {

            // Feb cannot have a date greater than 29.
            if (iDay > 29) {
                iDay = 29;
            }

            if (!((iYear % 4 == 0) && ((iYear % 100 != 0) || (iYear % 400 == 0)))) {

                // In a non-leap year feb cannot have date greater than 28.
                if (iDay > 28) {
                    iDay = 28;
                }
            } else {

                // In a leap year feb can have date anything from 1 to 29.
            }
        }

        year  = Integer.toString(iYear);
        month = ((iMonth < 10)
                 ? "0"
                 : "") + Integer.toString(iMonth);
        day   = ((iDay < 10)
                 ? "0"
                 : "") + Integer.toString(iDay);
        validDate.append(year).append("-").append(month).append("-").append(day).append(" ").append(time);

        return validDate.toString();
    }

    //~--- get methods --------------------------------------------------------

    /**
     * This method returns the data type of the given descriptor.
     *
     * @param aDesc Descriptor value.
     * @return      Data type of the descriptor.
     */
    private int getDataType(String aDesc) {
        Integer temp     = myDescTable.get(aDesc);
        int     dataType = 0;

        if (temp == null) {

            // Decide the datatype of this on its name. Default is String.
            if (aDesc.equalsIgnoreCase("sortfield")) {
                dataType = STRING;
            } else if (aDesc.equalsIgnoreCase("sortorder")) {
                dataType = STRING;
            } else if (aDesc.equalsIgnoreCase("disphdr")) {
                dataType = TYPE;
            } else if (aDesc.equalsIgnoreCase("alltext")) {
                dataType = TEXT;
            } else if (aDesc.equalsIgnoreCase("limit")) {
                dataType = INT;
            } else if (aDesc.equalsIgnoreCase("expand")) {
                dataType = STRING;
            } else if (aDesc.equalsIgnoreCase("read")) {
                dataType = BOOLEAN;
            } else if (aDesc.equalsIgnoreCase("unread")) {
                dataType = BOOLEAN;
            } else {
                dataType = STRING;
            }
        } else {
            dataType = temp.intValue();
        }

        return dataType;
    }

    /**
     * This method identifies the date part in the argument list.
     *
     * @return Date in yyyy-MM-dd HH:mm:ss format.
     *
     * @exception DSQLParseException.
     */
    private String getDate(boolean start) throws DSQLParseException {
        String        token       = nextToken();
        StringBuilder dateValue   = new StringBuilder();
        StringBuilder datePattern = new StringBuilder();

        if (token.matches(YYYYMMDD)) {
            datePattern.append("yyyyMMdd");
        } else if (token.matches(MM_DD_YYYY)) {
            datePattern.append("MM/dd/yyyy");
        } else {
            String tmp = nextToken();

            if (tmp.equals(MINUS)) {
                token = token +                 // Year
                    tmp +                       // hyphen
                        nextToken() +           // Month
                            nextToken() +       // hyphen
                                nextToken();    // Date;
                datePattern.append("yyyy-MM-dd");
            } else {
                StringBuilder message = new StringBuilder();

                throw new DSQLParseException(Messages.getMessage("TBits_DSQL_SUPPORTED_DATEFORMATS"));
            }
        }

        dateValue.append(token);

        if (isLast() == true) {

            /*
             * We are already at the end of input. So the date argument does not
             * have any time or zone part. Append the time part based on the
             * value of start and return the date in standard format.
             */
            datePattern.append(" HH:mm:ss");
            dateValue.append(" ").append(timePart(start));

            return toStandardFormat(dateValue.toString(), datePattern.toString());
        }

        /*
         * Since we are not at the end of the input, what follows might as well
         * belong to the date argument.
         */
        token = nextToken();

        if (token.equals(CLOSE_SQUARE)) {

            /*
             * Put back the closing square. It will be read by the caller.
             */
            putBack();
            datePattern.append(" HH:mm:ss");
            dateValue.append(" ").append(timePart(start));

            return toStandardFormat(dateValue.toString(), datePattern.toString());
        }

        /*
         * The token that is read assuming to be a CLOSING square must be a
         * space. So the next token should be time part of the date
         */
        token = nextToken();
        LOG.info(token);

        if (token.matches(HH_MM)) {
            dateValue.append(" ").append(token).append(":00");
            datePattern.append(" HH:mm:ss");
        } else if (token.matches(HH_MM_SS)) {
            dateValue.append(" ").append(token);
            datePattern.append(" HH:mm:ss");
        } else {

            /*
             * This might be directly the zone information. Put back two tokens
             *    - one assumed to be a space.
             *    - one assumed to be time info.
             */
            putBack();
            putBack();
            dateValue.append(" ").append(timePart(start));
            datePattern.append(" HH:mm:ss");
        }

        if (isLast() == true) {

            /*
             * Already at the end of the input, so no Zone information.
             * Return the date in standard format.
             */
            return toStandardFormat(dateValue.toString(), datePattern.toString());
        }

        /*
         * Again, we are not at the end of input, so the zone information might
         * follow.
         */
        token = nextToken();

        if (token.equals(CLOSE_SQUARE)) {

            /*
             * End of date argument. put the square back so that the caller will
             * read that. return the date.
             */
            putBack();

            return toStandardFormat(dateValue.toString(), datePattern.toString());
        }

        /*
         * The token read earlier is not a closing square, so it must be a space
         * What follows the space can be zone information.
         */
        token = nextToken();

        if (token.matches(ZONE_PATTERN)) {
            dateValue.append(" ").append(token.toUpperCase());
            datePattern.append(" ZZZ");
        } else {

            /*
             * Put back the token read assuming them to be
             *  - space
             *  - zone value.
             */
            putBack();
            putBack();
        }

        return toStandardFormat(dateValue.toString(), datePattern.toString());
    }

    /**
     * Accessor Method for DisplayHeader.
     *
     * @return current value of DisplayHeader.
     */
    public ArrayList<String> getDisplayHeader() {
        return myDisplayHeader;
    }

    /**
     * Accessor method for Limit.
     *
     * @return Current Value of Limit.
     */
    public boolean getExpand() {
        return myExpand;
    }

    /**
     * Accessor Method for input.
     *
     * @return current value of input.
     */
    public String getInput() {
        return myInput;
    }

    /**
     * Accessor method for Limit.
     *
     * @return Current Value of Limit.
     */
    public int getLimit() {
        return myLimit;
    }

    /**
     * Accessor Method for Parsetable.
     *
     * @return current value of ParseTable.
     */
    public ArrayList<ParseEntry> getParseTable() {
        return myParseTable;
    }

    /**
     * Accessor Method for processed input.
     *
     * @return current value of Processed Input.
     */
    public String getProcessedInput() {
        return myProcInput;
    }

    /**
     * This method returns the argument list corresponding to the date
     * constants in DSQL.
     *
     * @param rel Name of the date constant.
     *
     * @return Argument list.
     */
    private ArrayList<String> getRelativeDate(String rel) {

        /*
         * TBits Date Constants
         *      - today
         *      - yesterday
         *      - tomorrow
         *      - lastweek
         *      - lastmonth
         *      - lastyear
         */
        ArrayList<String> list = new ArrayList<String>();
        Timestamp         ts   = null;
        Calendar          now  = Calendar.getInstance();

        if (rel.equals(TODAY)) {

            /*
             * Calendar is pointing to today. Get today's date and add start
             * and ending times to it.
             */
            ts = Timestamp.getTimestamp(now.getTime());

            String start = toStandardFormat(ts, true);
            String end   = toStandardFormat(ts, false);

            list.add(BETWEEN);
            list.add(start);
            list.add(end);
        } else if (rel.equals(YESTERDAY)) {

            /*
             * Move the calendar to yesterday.
             * Now get the date part of yesterday and add starting and ending
             * times.
             */
            now.add(Calendar.DATE, -1);
            ts = Timestamp.getTimestamp(now.getTime());

            String start = toStandardFormat(ts, true);
            String end   = toStandardFormat(ts, false);

            list.add(BETWEEN);
            list.add(start);
            list.add(end);
        } else if (rel.equals(TOMORROW)) {

            /*
             * Move the calendar to tomorrow.
             * Now get the date part of tomorrow and add starting and ending
             * times.
             */
            now.add(Calendar.DATE, +1);
            ts = Timestamp.getTimestamp(now.getTime());

            String start = toStandardFormat(ts, true);
            String end   = toStandardFormat(ts, false);

            list.add(BETWEEN);
            list.add(start);
            list.add(end);
        } else if (rel.equals(LASTWEEK)) {

            // Move to yesterday, get the date part and add end time.
            now.add(Calendar.DATE, -1);
            ts = Timestamp.getTimestamp(now.getTime());

            String end = toStandardFormat(ts, false);

            // Reset, Move to last week, get the date part and add start time.
            now.add(Calendar.DATE, 1);
            now.add(Calendar.WEEK_OF_YEAR, -1);
            ts = Timestamp.getTimestamp(now.getTime());

            String start = toStandardFormat(ts, true);

            list.add(BETWEEN);
            list.add(start);
            list.add(end);
        } else if (rel.equals(LASTMONTH)) {

            // Move to yesterday, get the date part and add end time.
            now.add(Calendar.DATE, -1);
            ts = Timestamp.getTimestamp(now.getTime());

            String end = toStandardFormat(ts, false);

            // Reset, Move to last month, get the date part and add start time.
            now.add(Calendar.DATE, 1);
            now.add(Calendar.MONTH, -1);
            ts = Timestamp.getTimestamp(now.getTime());

            String start = toStandardFormat(ts, true);

            list.add(BETWEEN);
            list.add(start);
            list.add(end);
        } else if (rel.equals(LASTYEAR)) {

            // Move to yesterday, get the date part and add end time.
            now.add(Calendar.DATE, -1);
            ts = Timestamp.getTimestamp(now.getTime());

            String end = toStandardFormat(ts, false);

            // Reset, Move to last year, get the date part and add start time.
            now.add(Calendar.DATE, 1);
            now.add(Calendar.YEAR, -1);
            ts = Timestamp.getTimestamp(now.getTime());

            String start = toStandardFormat(ts, true);

            list.add(BETWEEN);
            list.add(start);
            list.add(end);
        }

        return list;
    }

    /**
     * Accessor Method for SortField.
     *
     * @return current value of sortField..
     */
    public String getSortField() {
        return mySortField;
    }

    /**
     * Accessor Method for SortOrder
     *
     * @return current value of sortOrder
     */
    public int getSortOrder() {
        return mySortOrder;
    }

    /**
     * Returns today's date in the specified format.
     *
     * @param aFormat   Format of the date.
     *
     * @return Date in specified zone.
     */
    public String getToday(String aFormat) {
        SimpleDateFormat sdf = new SimpleDateFormat(aFormat);

        return sdf.format((Calendar.getInstance()).getTime());
    }

    /**
     * Accessor Method for token list.
     *
     * @return current value of Token List.
     */
    public ArrayList<String> getTokenList() {
        return myTokenList;
    }

    /**
     * This method returns true if we are at the end of tokenlist.
     */
    private boolean isLast() {
        if (myCurPos > (myTokenListSize - 1)) {
            return true;
        }

        return false;
    }
}
