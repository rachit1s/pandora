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
 * TUser.java
 *
 * $Header:
 *
 */
package transbit.tbits.common;

//~--- JDK imports ------------------------------------------------------------

import java.io.BufferedInputStream;
import java.io.IOException;

import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import java.util.ArrayList;

import transbit.tbits.webapps.WebUtil;

//~--- classes ----------------------------------------------------------------

//Imports from the current package.

/**
 * This class is a wrapper that accesses the TBits's user database through
 * its http api.
 *
 * @author  : Vaibhav.
 * @version : $Id: $
 *
 */
public class TUser {
    public static final TBitsLogger LOG = TBitsLogger.getLogger("transbit.tbits.common");

    // TBits URL.
    private static String ourTBitsUserResourceURL;

    //~--- static initializers ------------------------------------------------

    static {
        try {
            ourTBitsUserResourceURL = System.getProperty("tbits.userResource.url");

            if ((ourTBitsUserResourceURL == null) || ourTBitsUserResourceURL.trim().equals("")) {
                ourTBitsUserResourceURL = WebUtil.getNearestPath("/users");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //~--- fields -------------------------------------------------------------

    private String  myDisplayName;
    private String  myEmai;
    private String  myExtension;
    private String  myFirstName;
    private String  myHomePhone;
    private boolean myIsActive;
    private String  myLastName;
    private String  myLocation;
    private String  myMobile;

    // Attributes of this Object.
    private String myUserLogin;
    private String myUserType;

    //~--- constant enums -----------------------------------------------------

    // Type of output.
    public static enum OutputFormat { TEXT, XML, JSON }

    ;

    //~--- constant enums -----------------------------------------------------

    private static enum UserColumn {
        USERLOGIN, FIRSTNAME, LASTNAME, DISPLAYNAME, EMAIL, USERTYPE, LOCATION, EXTENSION, MOBILE, HOMEPHONE
    }

    ;

    //~--- constructors -------------------------------------------------------

    /**
     * The default constructor.
     */
    public TUser() {}

    /**
     * The complete constructor.
     *
     *  @param aUserLogin
     *  @param aFirstName
     *  @param aLastName
     *  @param aDisplayName
     *  @param aEmai
     *  @param aUserType
     *  @param aIsActive
     *  @param aLocation
     *  @param aExtension
     *  @param aMobile
     *  @param aHomePhone
     */
    public TUser(String aUserLogin, String aFirstName, String aLastName, String aDisplayName, String aEmai, String aUserType, boolean aIsActive, String aLocation, String aExtension, String aMobile,
                 String aHomePhone) {
        myUserLogin   = aUserLogin;
        myFirstName   = aFirstName;
        myLastName    = aLastName;
        myDisplayName = aDisplayName;
        myEmai        = aEmai;
        myUserType    = aUserType;
        myIsActive    = aIsActive;
        myLocation    = aLocation;
        myExtension   = aExtension;
        myMobile      = aMobile;
        myHomePhone   = aHomePhone;
    }

    //~--- methods ------------------------------------------------------------

    /**
     * This method retrieves the user objects from the TBits web server
     * by email.
     *
     * @param aEmail            Email to be looked up.
     * @param aLikeMatch        If like match should be performed.
     *
     * @return List of matched users.
     */
    public static ArrayList<TUser> lookupByEmail(String aEmail, boolean aLikeMatch) throws IOException {
        ArrayList<TUser> list = new ArrayList<TUser>();

        if ((aEmail == null) || aEmail.trim().equals("")) {
            return list;
        }

        if (aLikeMatch == true) {
            aEmail = aEmail + "*";
        }

        if (aEmail.indexOf("-") > 0) {
            aEmail = "\"" + aEmail + "\"";
        }

        String query  = "email:" + aEmail;
        String output = queryTBits(query, OutputFormat.TEXT);

        list = parseTextOutput(output);

        return list;
    }

    /**
     * This method retrieves the user objects from the TBits web server
     * by login.
     *
     * @param aLogin            Login to be looked up.
     * @param aLikeMatch        If like match should be performed.
     * @return List of matched users.
     */
    public static ArrayList<TUser> lookupByUserLogin(String aLogin, boolean aLikeMatch) throws IOException {
        ArrayList<TUser> list = new ArrayList<TUser>();

        if ((aLogin == null) || aLogin.trim().equals("")) {
            return list;
        }

        if (aLikeMatch == true) {
            aLogin = aLogin + "*";
        }

        String query  = "login:" + aLogin;
        String output = queryTBits(query, OutputFormat.TEXT);

        list = parseTextOutput(output);

        return list;
    }

    /**
     * Main method for testing.
     *
     * @param arg  Command line arguments.
     */
    public static void main(String arg[]) {
        try {
            long             start = System.currentTimeMillis();
            ArrayList<TUser> list  = TUser.getMembers("ussetrd@nyc", true);
            long             end   = System.currentTimeMillis();

            LOG.info("Time taken: " + (end - start) + " ms");

            for (TUser user : list) {
                System.out.println(user.getUserLogin() + ": " + user);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Parser for text output.
     *
     * @param aOutput Output of the query in text format.
     *
     * @return List of matched users.
     */
    private static ArrayList<TUser> parseTextOutput(String aOutput) {
        ArrayList<TUser> list = new ArrayList<TUser>();

        /*
         * Steps:
         *  1. Trim the argument.
         *  2. Split on \n.
         *  3. First line is semi-colon separated header.
         *     3.1 Split on ';' and read the header.
         *     3.2 Store the column names in an arraylist in uppercase.
         *  4. The next line is empty and can be ignored.
         *  5. All the lines that follow contain data in semi-colon separated
         *     format. Split them and based on the position, set the
         *     corresponding column value.
         */
        aOutput = aOutput.trim();

        ArrayList<String> lines = Utilities.toArrayList(aOutput, "\n");

        if (lines.size() <= 0) {
            LOG.warn("Empty string: " + aOutput);

            return list;
        }

        String            header     = lines.get(0).toUpperCase();
        ArrayList<String> columnList = Utilities.toArrayList(header, ";");

        // Ignore the next line and start processing from line 2.
        int ctr = 1;
        int len = lines.size();

        for (ctr = 1; ctr < len; ctr++) {
            TUser  user = new TUser();
            String line = lines.get(ctr);

            if (line.trim().equals("")) {
                continue;
            }

            line = line.replaceAll(";;", ";-;");

            ArrayList<String> array = Utilities.toArrayList(line, ";");

            for (int i = 0; i < array.size(); i++) {
                String columnName  = columnList.get(i);
                String columnValue = array.get(i);

                user.set(UserColumn.valueOf(columnName), columnValue);
            }

            list.add(user);
        }

        return list;
    }

    /**
     * This methods executes the query and returns the output from the TBits
     * server.
     *
     * @param query             DQL Query to query the TBits User database.
     *
     * @return List of matched users.
     * @throws IOException
     */
    private static String queryTBits(String query, OutputFormat aFormat) throws IOException {
        StringBuffer result = new StringBuffer();

        if ((query == null) || query.trim().equals("")) {
            return result.toString();
        }

        String format = "text";    // Default output format is text.

        switch (aFormat) {
        case JSON :
            format = "json";

            break;

        case TEXT :
            format = "text";

            break;

        case XML :
            format = "xml";

            break;

        default :
            format = "text";

            break;
        }

        String resURL = ourTBitsUserResourceURL + "?q=" + query + "&format=" + format;

        /*
         * Send request to the TBits server using a URL Connection object.
         * Let us handle the malformedurl exception as this wont occur in this
         * case. But lets throw IOException which can be due to problems
         * with establising connection with TBits as the caller should be
         * notified in such cases.
         */
        try {
            URL           url = new URL(resURL);
            URLConnection con = url.openConnection();

            con.connect();

            BufferedInputStream bis = new BufferedInputStream(con.getInputStream());
            int                 ch  = -1;

            while ((ch = bis.read()) != -1) {
                result.append((char) ch);
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        return result.toString().trim();
    }

    /**
     * Returns the string representation of this object.
     */
    public String toString() {
        StringBuffer buffer = new StringBuffer();

        buffer.append("[ ").append(myUserLogin).append(", ").append(myFirstName).append(", ").append(myLastName).append(", ").append(myDisplayName).append(", ").append(myEmai).append(", ").append(
            myUserType).append(", ").append(myLocation).append(", ").append(myExtension).append(", ").append(myMobile).append(", ").append(myHomePhone).append(" ]");

        return buffer.toString();
    }

    //~--- get methods --------------------------------------------------------

    /**
     * Accessor method for DisplayName property.
     *
     * @return Current Value of DisplayName
     *
     */
    public String getDisplayName() {
        return myDisplayName;
    }

    /**
     * Accessor method for Emai property.
     *
     * @return Current Value of Emai
     *
     */
    public String getEmai() {
        return myEmai;
    }

    /**
     * Accessor method for Extension property.
     *
     * @return Current Value of Extension
     *
     */
    public String getExtension() {
        return myExtension;
    }

    /**
     * Accessor method for FirstName property.
     *
     * @return Current Value of FirstName
     *
     */
    public String getFirstName() {
        return myFirstName;
    }

    /**
     * Accessor method for HomePhone property.
     *
     * @return Current Value of HomePhone
     *
     */
    public String getHomePhone() {
        return myHomePhone;
    }

    /**
     * Accessor method for IsActive property.
     *
     * @return Current Value of IsActive
     *
     */
    public boolean getIsActive() {
        return myIsActive;
    }

    /**
     * Accessor method for LastName property.
     *
     * @return Current Value of LastName
     *
     */
    public String getLastName() {
        return myLastName;
    }

    /**
     * Accessor method for Location property.
     *
     * @return Current Value of Location
     *
     */
    public String getLocation() {
        return myLocation;
    }

    /**
     * This method retrieves the members of the given mailing list.
     *
     * @param aEmail   Email address of the mailing list.
     * @param aExpand  True if the mailing list has to be recursively expanded.
     *
     * @return Members of the mailing list.
     */
    public static ArrayList<TUser> getMembers(String aEmail, boolean aExpand) throws IOException {
        ArrayList<TUser> list = new ArrayList<TUser>();

        if ((aEmail == null) || aEmail.trim().equals("")) {
            return list;
        }

        String desc = "membersof";

        if (aExpand == true) {
            desc = desc + ":expand";
        }

        if (aEmail.indexOf("-") > 0) {
            aEmail = "\"" + aEmail + "\"";
        }

        String query  = desc + ":" + aEmail;
        String output = queryTBits(query, OutputFormat.TEXT);

        list = parseTextOutput(output);

        return list;
    }

    /**
     * Accessor method for Mobile property.
     *
     * @return Current Value of Mobile
     *
     */
    public String getMobile() {
        return myMobile;
    }

    /**
     * Accessor method for UserLogin property.
     *
     * @return Current Value of UserLogin
     *
     */
    public String getUserLogin() {
        return myUserLogin;
    }

    /**
     * Accessor method for UserType property.
     *
     * @return Current Value of UserType
     *
     */
    public String getUserType() {
        return myUserType;
    }

    //~--- set methods --------------------------------------------------------

    /**
     * Generic set method.
     *
     * @param aColumnName Name of the column name.
     * @param aValue      Value.
     */
    private void set(UserColumn aColumnName, String aValue) {
        switch (aColumnName) {
        case DISPLAYNAME :
            setDisplayName(aValue);

            break;

        case EMAIL :
            setEmai(aValue);

            break;

        case EXTENSION :
            setExtension(aValue);

            break;

        case FIRSTNAME :
            setFirstName(aValue);

            break;

        case HOMEPHONE :
            setHomePhone(aValue);

            break;

        case LASTNAME :
            setLastName(aValue);

            break;

        case LOCATION :
            setLocation(aValue);

            break;

        case MOBILE :
            setMobile(aValue);

            break;

        case USERLOGIN :
            setUserLogin(aValue);

            break;

        case USERTYPE :
            setUserType(aValue);

            break;
        }
    }

    /**
     * Mutator method for DisplayName property.
     *
     * @param aDisplayName New Value for DisplayName
     *
     */
    public void setDisplayName(String aDisplayName) {
        myDisplayName = aDisplayName;
    }

    /**
     * Mutator method for Emai property.
     *
     * @param aEmai New Value for Emai
     *
     */
    public void setEmai(String aEmai) {
        myEmai = aEmai;
    }

    /**
     * Mutator method for Extension property.
     *
     * @param aExtension New Value for Extension
     *
     */
    public void setExtension(String aExtension) {
        myExtension = aExtension;
    }

    /**
     * Mutator method for FirstName property.
     *
     * @param aFirstName New Value for FirstName
     *
     */
    public void setFirstName(String aFirstName) {
        myFirstName = aFirstName;
    }

    /**
     * Mutator method for HomePhone property.
     *
     * @param aHomePhone New Value for HomePhone
     *
     */
    public void setHomePhone(String aHomePhone) {
        myHomePhone = aHomePhone;
    }

    /**
     * Mutator method for IsActive property.
     *
     * @param aIsActive New Value for IsActive
     *
     */
    public void setIsActive(boolean aIsActive) {
        myIsActive = aIsActive;
    }

    /**
     * Mutator method for LastName property.
     *
     * @param aLastName New Value for LastName
     *
     */
    public void setLastName(String aLastName) {
        myLastName = aLastName;
    }

    /**
     * Mutator method for Location property.
     *
     * @param aLocation New Value for Location
     *
     */
    public void setLocation(String aLocation) {
        myLocation = aLocation;
    }

    /**
     * Mutator method for Mobile property.
     *
     * @param aMobile New Value for Mobile
     *
     */
    public void setMobile(String aMobile) {
        myMobile = aMobile;
    }

    /**
     * Mutator method for UserLogin property.
     *
     * @param aUserLogin New Value for UserLogin
     *
     */
    public void setUserLogin(String aUserLogin) {
        myUserLogin = aUserLogin;
    }

    /**
     * Mutator method for UserType property.
     *
     * @param aUserType New Value for UserType
     *
     */
    public void setUserType(String aUserType) {
        myUserType = aUserType;
    }
}
