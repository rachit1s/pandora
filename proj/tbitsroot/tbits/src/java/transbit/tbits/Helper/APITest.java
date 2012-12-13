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
 * APITest.java
 *
 * $Header:
 */
package transbit.tbits.Helper;

//~--- non-JDK imports --------------------------------------------------------

//TBits Imports
import static transbit.tbits.Helper.TBitsConstants.PKG_UTIL;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Properties;
import java.util.StringTokenizer;

import transbit.tbits.api.APIUtil;
import transbit.tbits.api.AddRequest;
import transbit.tbits.api.UpdateRequest;
import transbit.tbits.common.Configuration;
import transbit.tbits.common.DataSourcePool;
import transbit.tbits.common.DatabaseException;
import transbit.tbits.common.TBitsLogger;
import transbit.tbits.domain.Request;
import transbit.tbits.domain.User;
import transbit.tbits.exception.APIException;
import transbit.tbits.exception.DSQLParseException;
import transbit.tbits.exception.TBitsException;
import transbit.tbits.search.Result;
import transbit.tbits.search.Searcher;

//~--- classes ----------------------------------------------------------------

public class APITest {
    public static TBitsLogger LOG = TBitsLogger.getLogger(PKG_UTIL);
    public static PrintWriter PW;

    //~--- fields -------------------------------------------------------------

    // Parameters of input/output files
    private String         inFile1     = null;
    private String         inFile2     = null;
    private String         inFile3     = null;
    private String         inFile4     = null;
    private String         inFile5     = null;
    private String         outFile1    = null;
    private String         outFile2    = null;
    private String         outFile3    = null;
    private String         outFile4    = null;
    private String         outFile5    = null;
    private String         outFile6    = null;
    private int            userId      = 0;
    private String         user        = null;
    private int            sysId       = 0;
    private String         server      = null;
    private String         passwd      = null;
    private String         driverTag   = null;
    private String         driverClass = null;
    private String         db          = null;
    private BufferedReader br;
    private FileReader     fr;

    // Parameters of the database
    private Connection myDbConnection;

    //~--- methods ------------------------------------------------------------

    /*
     *     private void buildDatabase()
     *   throws SQLException
     * {
     *   File file = null;
     *   FileReader fr = null;
     *   Statement ps = null;
     *   boolean batch;
     *   int[] updateCounts;
     *   String scStr = null;
     *   try
     *   {
     *       file = new File("src/scripts/db/"+tbitsSchema);
     *       fr = new FileReader(file);
     *       br = new BufferedReader(fr);
     *       scStr = getString(br);
     * /          scStr = scStr.replaceAll("'", "''");
     * /          scStr = scStr.replaceAll("\\[", "/[");
     * /          scStr = scStr.replaceAll("\\]", "/]");
     * /          scStr = scStr + "\n {escape ''/''}\n\n";
     *       ps = myDbConnection.createStatement();
     *       ps.addBatch(scStr);
     *       LOG.info(scStr);
     *       ps.executeBatch();
     *       LOG.info("Schema run successfully");
     *
     *       file = new File("src/scripts/db/"+tbitsData);
     *       fr = new FileReader(file);
     *       br = new BufferedReader(fr);
     *       scStr = getString(br);
     *       ps = myDbConnection.createStatement();
     *       batch = ps.execute(scStr);
     *       LOG.info("Data run successfully");
     *
     *       file = new File("src/scripts/db/"+tbitsProcs);
     *       fr = new FileReader(file);
     *       br = new BufferedReader(fr);
     *       scStr = getString(br);
     *       ps = myDbConnection.createStatement();
     *       batch = ps.execute(scStr);
     *       LOG.info("Procedures run successfully");
     *
     *       file = new File("src/scripts/db/"+tbitsUsers);
     *       fr = new FileReader(file);
     *       br = new BufferedReader(fr);
     *       scStr = getString(br);
     *       ps = myDbConnection.createStatement();
     *       batch = ps.execute(scStr);
     *       LOG.info("users added successfully");
     *
     *   }
     *   catch (IOException ioe)
     *   {
     *       LOG.info("An exception occured while getting files",ioe);
     *       System.exit(1);
     *   }
     *   catch (NullPointerException npe)
     *   {
     *       LOG.info("\n\tPlease Check if the schema files are present.");
     *       System.exit(1);
     *   }
     *
     *   }
     */
    private void buildBusinessArea() throws SQLException {
        CallableStatement stmt = myDbConnection.prepareCall("stp_tbits_createBusinessArea ?, ?");

        stmt.setString(1, "TestBA");
        stmt.setString(2, "TBA");

        boolean flag = stmt.execute();
    }

    private static ArrayList lookUpUserByIds(String Ids) throws DatabaseException
    {
        ArrayList <User> list = new ArrayList <User>();
        System.out.print("\nParsing: ");
        for(String strid: Ids.split(","))
        {
            try{
                int id = Integer.parseInt(strid);
                System.out.print(id + ",");
                list.add(User.lookupAllByUserId(id));
            }
            catch(NumberFormatException nfe)
            {
                System.out.println("Invalid list. It should be something like: 1,2,3");
                continue;
            }
        }
        System.out.print("\n");
        return list;
    }
    
    private static void inTest(String subSet, String superSet)  throws DatabaseException
    {
        //String source = "1,2,3"; String dest = "1,2,4,5";
        if(APIUtil.in(lookUpUserByIds(subSet), lookUpUserByIds(superSet)))
            System.out.println(superSet + " contains " + subSet);
        else
            System.out.println(superSet + " does not contain " + subSet);
    }
    
    /**
     * Tests "in"
     */
    private static void inTest() throws DatabaseException
    {
        inTest("1,2,3", "1,2,3,4");
        inTest("1,2,3,4", "1,2,3,4");
        inTest("1,2,3,5", "1,2,3,4");
        inTest("1,2", "1,2,3,4");
        inTest("1,5,6", "1,2,3,4");
        inTest("", "1,2,3,4");
        inTest("", "");
    }
    public static void main(String args[]) throws DatabaseException {
        APITest apit = new APITest();
        apit.testData();
        //inTest();
    }

    private void printResults(ArrayList<Result> aList) {
        String field = "request_id";

        PW.println("Request numbers for this search are ");

        for (Result sr : aList) {
            Object value    = sr.get(field);
            String strvalue = value.toString();

            LOG.info("strvalue is " + strvalue);
            PW.print(strvalue + ",");
        }

        PW.println();
        PW.println("----------------------------------------------" + "-----------------------------");
    }

    private boolean readConfigurationFile() {
        Properties prop = new Properties();

        try {
            File            testConfFile = Configuration.findPath("test.properties");
            FileInputStream fis          = new FileInputStream(testConfFile);

            prop.load(fis);
        } catch (IOException ioe) {
            LOG.error("An exception occured while setting config file", ioe);
            return false;
            //System.exit(1);
        } catch (NullPointerException npe) {
            LOG.error("\n\tPlease Check if test.prop is present.", npe);
            return false;
            //System.exit(1);
        }

        // Getting input and output files
        inFile1  = "src/test/in/" + prop.getProperty("INFILE1");
        inFile2  = "src/test/in/" + prop.getProperty("INFILE2");
        inFile3  = "src/test/in/" + prop.getProperty("INFILE3");
        inFile4  = "src/test/in/" + prop.getProperty("INFILE4");
        inFile5  = "src/test/in/" + prop.getProperty("INFILE5");
        outFile1 = "src/test/out/" + prop.getProperty("OUTFILE1");
        outFile2 = "src/test/out/" + prop.getProperty("OUTFILE2");
        outFile3 = "src/test/out/" + prop.getProperty("OUTFILE3");
        outFile4 = "src/test/out/" + prop.getProperty("OUTFILE4");
        outFile5 = "src/test/out/" + prop.getProperty("OUTFILE5");
        outFile6 = "src/test/out/" + prop.getProperty("OUTFILE6");

        // SQL Database Driver properties.
        server      = prop.getProperty("SERVER");
        db          = prop.getProperty("DATABASE");
        user        = prop.getProperty("USER");
        passwd      = prop.getProperty("PASSWORD");
        driverClass = prop.getProperty("DRIVER_CLASS");
        driverTag   = prop.getProperty("DRIVER_TAG");

        // user related properties
        userId = Integer.parseInt(prop.getProperty("USER_ID"));

        // sysId = Integer.parseInt(prop.getProperty("SYS_ID"));
        sysId = 1;
        return true;
    }

    private void test(Hashtable<String, String> aHashtable, boolean flag) {
        String        expectedResult = aHashtable.get("expected_result");
        AddRequest    addrequest     = new AddRequest();
        addrequest.setSource(TBitsConstants.SOURCE_CMDLINE);
        
        UpdateRequest updateRequest  = new UpdateRequest();
        updateRequest.setSource(TBitsConstants.SOURCE_CMDLINE);
        Request       r              = null;

        try {
            if (flag == false) {
                r = addrequest.addRequest(aHashtable);
            }

            if (flag == true) {
                r = updateRequest.updateRequest(aHashtable);
            }

            if ((expectedResult.compareTo("pass") == 0) && (r != null)) {
                LOG.info("Testcase  has passed successfully");
                PW.println("Testcase  has passed successfully");
            }

            if ((expectedResult.compareTo("fail") == 0) && (r != null)) {
                LOG.info("Testcase  has failed miserably");
                PW.println("Testcase  has failed ");
            }
        } catch (TBitsException de) {}
        catch (APIException apie) {
            if (expectedResult.compareTo("pass") == 0) {
                LOG.info("Test case  has failed miserably");
                PW.println("Test case  has failed ");
                PW.println("Stacktrace : ");
                apie.printStackTrace();
                apie.printStackTrace(PW);
            }

            if (expectedResult.compareTo("fail") == 0) {
                LOG.info("Testcase has passed successfully");
                PW.println("Testcase has passed successfully");
                PW.println("Stacktrace : ");
                apie.printStackTrace(PW);
            }
        }

        PW.println("------------------------------------------------" + "------------------------------------------------" + "--------------------------");
        PW.flush();
        LOG.info("----------------------------------------" + "----------------------------------------" + "-------------------------------------");
    }

    private boolean testData() {
        try {
            boolean flag = false;

            readConfigurationFile();
            myDbConnection = DataSourcePool.getConnection(server, db, user, passwd, driverClass, driverTag);

            if (myDbConnection == null) {
                LOG.error("Connection to " + db + "@" + server + "Failed");
                return false;
                //System.exit(0);
            }

            // buildDatabase();
            buildBusinessArea();
            testHashtable(inFile1, outFile1, false, false);
            testHashtable(inFile2, outFile2, false, true);
            testHashtable(inFile3, outFile3, false, false);
            testHashtable(inFile4, outFile4, true, false);
            testHashtable(inFile1, outFile5, false, true);
            testSearch(inFile5, outFile6);

            // startTime = (new Date()).getTime();
            // endTime = (new Date()).getTime();
        } catch (Exception e) {
            e.printStackTrace();
            LOG.error("An Exception has occured during APITest", e);
            return false;
        }
        return true;
    }

    private void testHashtable(String aInFile, String aOutFile, boolean ext, boolean update) {
        ArrayList<String>         list      = null;
        Hashtable<String, String> hashtable = null;

        try {
            fr = new FileReader(aInFile);
            br = new BufferedReader(fr);
            PW = new PrintWriter(new BufferedWriter(new FileWriter(aOutFile)));

            String line                = null;
            String outfile             = null;
            String testCaseDescription = null;
            int    count               = 1;

            br.readLine();

            while ((line = br.readLine()) != null) {
                hashtable = new Hashtable<String, String>();

                StringTokenizer st = new StringTokenizer(line, "|");

                list                = new ArrayList<String>();
                outfile             = st.nextToken();
                testCaseDescription = st.nextToken();
                LOG.info("testcase no " + count);
                PW.println("Testcase no " + count);
                PW.println("Testcase Description : " + testCaseDescription);
                PW.println("Data for this testcase is ");

                while (st.hasMoreTokens()) {
                    String string = ((String) st.nextToken());

                    PW.print(string + "|");
                    list.add(string);
                }

                PW.println();
                hashtable = getHashtable(list, ext);
                test(hashtable, update);
                hashtable = null;
                count++;
            }
        } catch (FileNotFoundException fe) {
            LOG.info("The test case file " + aInFile + " has not been found");
        } catch (IOException ioe) {
            LOG.info("An IOException has occured");
        }
    }

    private void testSearch(String aInFile, String aOutFile) {
        ArrayList<String>         list      = null;
        Hashtable<String, String> hashtable = null;
        String                    line      = null;
        Searcher                  searchReq = null;

        try {
            fr = new FileReader(aInFile);
            br = new BufferedReader(fr);
            PW = new PrintWriter(new BufferedWriter(new FileWriter(aOutFile)));

            int count = 1;

            while ((line = br.readLine()) != null) {
                String dsqlQuery = line;

                searchReq = new Searcher(sysId, userId, dsqlQuery);

                try {
                    searchReq.search();
                } catch (DatabaseException dbe) {
                    dbe.printStackTrace(PW);
                } catch (DSQLParseException dsqlpe) {
                    dsqlpe.printStackTrace();
                } catch (Exception dbe) {
                    dbe.printStackTrace(PW);
                }

                /*
                 * ArrayList<Result> alist = searchReq.getSearchResults();
                 * PW.println("Query for this testcase is ");
                 * PW.println(line);
                 * printResults(alist);
                 */
                count++;
            }
        } catch (FileNotFoundException fe) {
            LOG.info("The test case file " + aInFile + " has not been found");
        } catch (IOException ioe) {
            LOG.info("An IOException has occured");
        }
    }

    //~--- get methods --------------------------------------------------------

    private Hashtable<String, String> getHashtable(ArrayList<String> aLL, boolean aFlag) {
        Hashtable<String, String> ht    = new Hashtable<String, String>();
        String                    key   = null;
        String                    value = null;

        key   = "sys_id";
        value = aLL.get(0);

        if (value.compareTo("null") != 0) {
            ht.put(key, value);
        }

        key   = "request_id";
        value = aLL.get(1);

        if (value.compareTo("null") != 0) {
            ht.put(key, value);
        }

        key   = "category_id";
        value = aLL.get(2);

        if (value.compareTo("null") != 0) {
            ht.put(key, value);
        }

        key   = "status_id";
        value = aLL.get(3);

        if (value.compareTo("null") != 0) {
            ht.put(key, value);
        }

        key   = "severity_id";
        value = aLL.get(4);

        if (value.compareTo("null") != 0) {
            ht.put(key, value);
        }

        key   = "request_type_id";
        value = aLL.get(5);

        if (value.compareTo("null") != 0) {
            ht.put(key, value);
        }

        key   = "logger_ids";
        value = aLL.get(6);

        if (value.compareTo("null") != 0) {
            ht.put(key, value);
        }

        key   = "assignee_ids";
        value = aLL.get(7);

        if (value.compareTo("null") != 0) {
            ht.put(key, value);
        }

        key   = "subscriber_ids";
        value = aLL.get(8);

        if (value.compareTo("null") != 0) {
            ht.put(key, value);
        }

        key   = "to_ids";
        value = aLL.get(9);

        if (value.compareTo("null") != 0) {
            ht.put(key, value);
        }

        key   = "cc_ids";
        value = aLL.get(10);

        if (value.compareTo("null") != 0) {
            ht.put(key, value);
        }

        key   = "subject";
        value = aLL.get(11);

        if (value.compareTo("null") != 0) {
            ht.put(key, value);
        }

        key   = "description";
        value = aLL.get(12);

        if (value.compareTo("null") != 0) {
            ht.put(key, value);
        }

        key   = "is_private";
        value = aLL.get(13);

        if (value.compareTo("null") != 0) {
            ht.put(key, value);
        }

        key   = "parent_request_id";
        value = aLL.get(14);

        if (value.compareTo("null") != 0) {
            ht.put(key, value);
        }

        key   = "user_id";
        value = aLL.get(15);

        if (value.compareTo("null") != 0) {
            ht.put(key, value);
        }

        key   = "max_action_id";
        value = aLL.get(16);

        if (value.compareTo("null") != 0) {
            ht.put(key, value);
        }

        key   = "due_datetime";
        value = aLL.get(17);

        if (value.compareTo("null") != 0) {
            ht.put(key, value);
        }

        key   = "logged_datetime";
        value = aLL.get(18);

        if (value.compareTo("null") != 0) {
            ht.put(key, value);
        }

        key   = "lastupdated_datetime";
        value = aLL.get(19);

        if (value.compareTo("null") != 0) {
            ht.put(key, value);
        }

        key   = "header_description";
        value = aLL.get(20);

        if (value.compareTo("null") != 0) {
            ht.put(key, value);
        }

        key   = "attachments";
        value = aLL.get(21);

        if (value.compareTo("null") != 0) {
            ht.put(key, value);
        }

        key   = "summary";
        value = aLL.get(22);

        if (value.compareTo("null") != 0) {
            ht.put(key, value);
        }

        key   = "memo";
        value = aLL.get(23);

        if (value.compareTo("null") != 0) {
            ht.put(key, value);
        }

        key   = "append_interface";
        value = aLL.get(24);

        if (value.compareTo("null") != 0) {
            ht.put(key, value);
        }

        key   = "notify";
        value = aLL.get(25);

        if (value.compareTo("null") != 0) {
            ht.put(key, value);
        }

        key   = "notify_loggers";
        value = aLL.get(27);

        if (value.compareTo("null") != 0) {
            ht.put(key, value);
        }

        if (aFlag == false) {
            key   = "expected_result";
            value = aLL.get(28);

            if (value.compareTo("null") != 0) {
                ht.put(key, value);
            }

            return ht;
        }

        // Extended fields are added starting from here...
        key   = "bit";
        value = aLL.get(28);

        if (value.compareTo("null") != 0) {
            ht.put(key, value);
        }

        key   = "date";
        value = aLL.get(29);

        if (value.compareTo("null") != 0) {
            ht.put(key, value);
        }

        key   = "time";
        value = aLL.get(30);

        if (value.compareTo("null") != 0) {
            ht.put(key, value);
        }

        key   = "datetime";
        value = aLL.get(31);

        if (value.compareTo("null") != 0) {
            ht.put(key, value);
        }

        key   = "int";
        value = aLL.get(32);

        if (value.compareTo("null") != 0) {
            ht.put(key, value);
        }

        key   = "real";
        value = aLL.get(33);

        if (value.compareTo("null") != 0) {
            ht.put(key, value);
        }

        key   = "varchar";
        value = aLL.get(34);

        if (value.compareTo("null") != 0) {
            ht.put(key, value);
        }

        key   = "text";
        value = aLL.get(35);

        if (value.compareTo("null") != 0) {
            ht.put(key, value);
        }

        key   = "type";
        value = aLL.get(36);

        if (value.compareTo("null") != 0) {
            ht.put(key, value);
        }

        key   = "expected_result";
        value = aLL.get(37);

        if (value.compareTo("null") != 0) {
            ht.put(key, value);
        }

        return ht;
    }

    public String getString(BufferedReader br) throws IOException {
        String       line   = null;
        StringBuffer strBuf = new StringBuffer();

        while ((line = br.readLine()) != null) {
            if (line.toLowerCase().startsWith("go") == true) {
                continue;
            }

            strBuf.append(line).append("\n");
        }

        return strBuf.toString();
    }
}
