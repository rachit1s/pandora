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
 * User.java
 *
 * $Header:
 *
 */
package transbit.tbits.domain;

//~--- non-JDK imports --------------------------------------------------------

//Other TBits Imports.
import static transbit.tbits.Helper.TBitsConstants.ASC_ORDER;
import static transbit.tbits.Helper.TBitsConstants.PKG_DOMAIN;
import static transbit.tbits.api.Mapper.ourMailListUserMap;
import static transbit.tbits.api.Mapper.ourUserEmailMap;
import static transbit.tbits.api.Mapper.ourUserLoginMap;
import static transbit.tbits.api.Mapper.ourUserMap;

import java.io.Serializable;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;

import org.apache.commons.lang.RandomStringUtils;
import org.apache.poi.util.ArrayUtil;


import transbit.tbits.Helper.TBitsHelper;
import transbit.tbits.api.Mapper;
import transbit.tbits.authentication.AuthUtils;
import transbit.tbits.common.DataSourcePool;
import transbit.tbits.common.DatabaseException;
import transbit.tbits.common.TBitsLogger;
import transbit.tbits.config.WebConfig;
import transbit.tbits.events.BeforeUserUpdateEvent;
import transbit.tbits.events.EventFailureException;
import transbit.tbits.events.EventManager;
import transbit.tbits.exception.TBitsException;

//~--- classes ----------------------------------------------------------------

//Third party imports.

/**
 * This class is the domain object corresponding to the users table
 * in the database.
 *
 * @author  : ,Nitiraj
 * @version : $Id: $
 *
 */
public class User implements Comparable<User>, Serializable{
	
    public static final String HOME_PHONE_COLUMN_NAME = "home_phone";

	public static final String MOBILE_COLUMN_NAME = "mobile";

	public static final String EXTENSION_COLUMN_NAME = "extension";

	public static final String LOCATION_COLUMN_NAME = "location";

	public static final String MAIL_NICKNAME_COLUMN_NAME = "mail_nickname";

	public static final String MEMBER_COLUMN_NAME = "member";

	public static final String MEMBER_OF_COLUMN_NAME = "member_of";

	public static final String NAME_COLUMN_NAME = "name";

	public static final String DISTINGUISHED_NAME_COLUMN_NAME = "distinguished_name";

	public static final String CN_COLUMN_NAME = "cn";

	public static final String IS_DISPLAY_COLUMN_NAME = "is_display";

	public static final String IS_ON_VACATION_COLUMN_NAME = "is_on_vacation";

	public static final String WINDOWS_CONFIG_COLUMN_NAME = "windows_config";

	public static final String WEB_CONFIG_COLUMN_NAME = "web_config";

	public static final String USER_TYPE_ID_COLUMN_NAME = "user_type_id";

	public static final String IS_ACTIVE_COLUMN_NAME = "is_active";

	public static final String EMAIL_COLUMN_NAME = "email";

	public static final String DISPLAY_NAME_COLUMN_NAME = "display_name";

	public static final String LAST_NAME_COLUMN_NAME = "last_name";

	public static final String FIRST_NAME_COLUMN_NAME = "first_name";

	public static final String USER_LOGIN_COLUMN_NAME = "user_login";

	public static final String USER_ID_COLUMN_NAME = "user_id";

	public static final String FIRM_CODE_COLUMN_NAME = "firm_code" ;
	
	public static final String FIRM_ADDRESS_COLUMN_NAME = "firm_address" ;
	
	public static final String FULL_FIRM_NAME_COLUMN_NAME = "full_firm_name" ;
	
	public static final String SEX_COLUMN_NAME = "sex" ;
	
	public static final String DESIGNATION_COLUMN_NAME = "designation" ;
	public static final String OTHER_EMAILS_COLUMN_NAME = "other_emails" ;
	
	
	public static final TBitsLogger LOG = TBitsLogger.getLogger(PKG_DOMAIN);

    // Enum sort of fields for Attributes.
    private static final int USERID            = 1;
    private static final int USERLOGIN         = 2;
    private static final int LASTNAME          = 4;
    private static final int ISACTIVE          = 7;
    private static final int FIRSTNAME         = 3;
    private static final int EMAIL             = 6;
    private static final int DISPLAYNAME       = 5;
    private static final int USERTYPEID        = 8;
    private static final int WEBCONFIG         = 9;
    private static final int WINDOWSCONFIG     = 10;
    private static final int NAME              = 15;
    private static final int MOBILE            = 21;
    private static final int MEMBEROF          = 16;
    private static final int MEMBER            = 17;
    private static final int MAILNICKNAME      = 18;
    private static final int LOCATION          = 19;
    private static final int ISONVACATION      = 11;
    private static final int ISDISPLAY         = 12;
    private static final int HOMEPHONE         = 22;
    private static final int EXTENSION         = 20;
    private static final int DISTINGUISHEDNAME = 14;
    private static final int CN                = 13;
    private static final int FIRM_CODE 		   = 23;
    private static final int DESIGNATION 	   = 24;
    private static final int FIRM_ADDRESS	   = 25;
    private static final int SEX 			   = 26;
    private static final int FULL_FIRM_NAME    = 27;
    private static final int OTHER_EMAILS    = 28;
    

    // Static attributes related to sorting.
    private static int ourSortField;
    private static int ourSortOrder;

    //~--- fields -------------------------------------------------------------

    private String  myCn;
    private String  myDisplayName;
    private String  myDistinguishedName;
    private String  myEmail;
    private String  myExtension;
    private String  myFirstName;
    private String  myHomePhone;
    private boolean myIsActive;
    private boolean myIsDisplay;
    private boolean myIsOnVacation;
    private String  myLastName;
    private String  myLocation;
    private String  myMailNickname;
    private String  myMember;
    private String  myMemberOf;
    private String  myMobile;
    private String  myName;

    // Attributes of this Domain Object.
    private int       myUserId;
    private String    myUserLogin;
    private int       myUserTypeId;
    private String    myWebConfig;
    private WebConfig myWebConfigObject;
    private String    myWindowsConfig;

    // Nitiraj msg : new exteneded members
    private String myFirmCode = "" ;
    private String myDesignation = "" ;
    private String myFirmAddress = "";
    private String mySex = "" ;
    private String myFullFirmName = "" ;

	private ArrayList<String> myOtherEmails = new ArrayList<String>();
	
    //~--- constant enums -----------------------------------------------------

    public static enum UserColumn {
        USER_ID, USER_LOGIN, FIRST_NAME, LAST_NAME, DISPLAY_NAME, EMAIL, IS_ACTIVE, USER_TYPE_ID, WEB_CONFIG, WINDOWS_CONFIG, IS_ON_VACATION, IS_DISPLAY, CN, DISTINGUISHED_NAME, NAME, MEMBER_OF,
        MEMBER, MAIL_NICKNAME, LOCATION, EXTENSION, MOBILE, HOME_PHONE, FIRM_CODE, DESIGNATION , FIRM_ADDRESS, SEX, FULL_FIRM_NAME   
    }

    ;

    //~--- constructors -------------------------------------------------------

    /**
     * The default constructor.
     */
    public User() {}

    /**
     * The complete constructor.
     *
     *  @param aUserId
     *  @param aUserLogin
     *  @param aFirstName
     *  @param aLastName
     *  @param aDisplayName
     *  @param aEmail
     *  @param aIsActive
     *  @param aUserTypeId
     *  @param aWebConfig
     *  @param aWindowsConfig
     *  @param aIsOnVacation
     *  @param aIsDisplay
     *  @param aCn
     *  @param aDistinguishedName
     *  @param aName
     *  @param aMemberOf
     *  @param aMember
     *  @param aMailNickname
     *  @param aLocation
     *  @param aExtension
     *  @param aMobile
     *  @param aHomePhone
     */
    public User(int aUserId, String aUserLogin, String aFirstName, String aLastName, String aDisplayName, String aEmail, boolean aIsActive, int aUserTypeId, String aWebConfig, String aWindowsConfig,
                boolean aIsOnVacation, boolean aIsDisplay, String aCn, String aDistinguishedName, String aName, String aMemberOf, String aMember, String aMailNickname, String aLocation,
                String aExtension, String aMobile, String aHomePhone) {
        myUserId            = aUserId;
        myUserLogin         = aUserLogin;
        myFirstName         = aFirstName;
        myLastName          = aLastName;
        myDisplayName       = aDisplayName;
        myEmail             = aEmail;
        myIsActive          = aIsActive;
        myUserTypeId        = aUserTypeId;
        myWebConfig         = aWebConfig;
        myWindowsConfig     = aWindowsConfig;
        myIsOnVacation      = aIsOnVacation;
        myIsDisplay         = aIsDisplay;
        myCn                = aCn;
        myDistinguishedName = aDistinguishedName;
        myName              = aName;
        myMemberOf          = aMemberOf;
        myMember            = aMember;
        myMailNickname      = aMailNickname;
        myLocation          = aLocation;
        myExtension         = aExtension;
        myMobile            = aMobile;
        myHomePhone         = aHomePhone;
    }

    public User(int aUserId, String aUserLogin, String aFirstName, String aLastName, String aDisplayName, String aEmail, boolean aIsActive, int aUserTypeId, String aWebConfig, String aWindowsConfig,
            boolean aIsOnVacation, boolean aIsDisplay, String aCn, String aDistinguishedName, String aName, String aMemberOf, String aMember, String aMailNickname, String aLocation,
            String aExtension, String aMobile, String aHomePhone, String aFirmCode, String aDesignation, String aFirmAddress, String aSex, String aFullFirmName, String aOtherEmails){
   
    myUserId            = aUserId;
    myUserLogin         = aUserLogin;
    myFirstName         = aFirstName;
    myLastName          = aLastName;
    myDisplayName       = aDisplayName;
    myEmail             = aEmail;
    myIsActive          = aIsActive;
    myUserTypeId        = aUserTypeId;
    myWebConfig         = aWebConfig;
    myWindowsConfig     = aWindowsConfig;
    myIsOnVacation      = aIsOnVacation;
    myIsDisplay         = aIsDisplay;
    myCn                = aCn;
    myDistinguishedName = aDistinguishedName;
    myName              = aName;
    myMemberOf          = aMemberOf;
    myMember            = aMember;
    myMailNickname      = aMailNickname;
    myLocation          = aLocation;
    myExtension         = aExtension;
    myMobile            = aMobile;
    myHomePhone         = aHomePhone;
    
    setFirmCode( aFirmCode ) ;
    setDesignation(aDesignation) ;
    setFirmAddress( aFirmAddress ) ;
    setSex( aSex ) ;
    setFullFirmName( aFullFirmName ) ;
    setOtherEmails(aOtherEmails);
}

    //~--- methods ------------------------------------------------------------

    /**
     * Method that compares this object with the one passed W.R.T the
     * ourSortField.
     *
     * @param aObject Object to be compared.
     * @return 0 - If they are equal. 1 - If this is greater. -1 - If this is
     *         smaller.
     */
    public int compareTo(User aObject) {
        switch (ourSortField) {
        case USERID : {
            Integer i1 = myUserId;
            Integer i2 = aObject.myUserId;

            if (ourSortOrder == ASC_ORDER) {
                return i1.compareTo(i2);
            }

            return i2.compareTo(i1);
        }

        case USERLOGIN : {
            if (ourSortOrder == ASC_ORDER) {
                return myUserLogin.compareToIgnoreCase(aObject.myUserLogin);
            }

            return myUserLogin.compareToIgnoreCase(aObject.myUserLogin);
        }

        case FIRSTNAME : {
            if (ourSortOrder == ASC_ORDER) {
                return myFirstName.compareToIgnoreCase(aObject.myFirstName);
            }

            return aObject.myFirstName.compareToIgnoreCase(myFirstName);
        }

        case LASTNAME : {
            if (ourSortOrder == ASC_ORDER) {
                return myLastName.compareToIgnoreCase(aObject.myLastName);
            }

            return aObject.myLastName.compareToIgnoreCase(myLastName);
        }

        case DISPLAYNAME : {
            if (ourSortOrder == ASC_ORDER) {
                return myDisplayName.compareToIgnoreCase(aObject.myDisplayName);
            }

            return aObject.myDisplayName.compareToIgnoreCase(myDisplayName);
        }

        case EMAIL : {
            if (ourSortOrder == ASC_ORDER) {
                return myEmail.compareToIgnoreCase(aObject.myEmail);
            }

            return aObject.myEmail.compareToIgnoreCase(myEmail);
        }

        case ISACTIVE : {
            Boolean b1 = myIsActive;
            Boolean b2 = aObject.myIsActive;

            if (ourSortOrder == ASC_ORDER) {
                return b1.compareTo(b2);
            }

            return b2.compareTo(b1);
        }

        case USERTYPEID : {
            Integer i1 = myUserTypeId;
            Integer i2 = aObject.myUserTypeId;

            if (ourSortOrder == ASC_ORDER) {
                return i1.compareTo(i2);
            }

            return i2.compareTo(i1);
        }

        case WEBCONFIG : {
            if (ourSortOrder == ASC_ORDER) {
                return myWebConfig.compareTo(aObject.myWebConfig);
            }

            return aObject.myWebConfig.compareTo(myWebConfig);
        }

        case WINDOWSCONFIG : {
            if (ourSortOrder == ASC_ORDER) {
                return myWindowsConfig.compareTo(aObject.myWindowsConfig);
            }

            return aObject.myWindowsConfig.compareTo(myWindowsConfig);
        }

        case ISONVACATION : {
            Boolean b1 = myIsOnVacation;
            Boolean b2 = aObject.myIsOnVacation;

            if (ourSortOrder == ASC_ORDER) {
                return b1.compareTo(b2);
            }

            return b2.compareTo(b1);
        }

        case ISDISPLAY : {
            Boolean b1 = myIsDisplay;
            Boolean b2 = aObject.myIsDisplay;

            if (ourSortOrder == ASC_ORDER) {
                return b1.compareTo(b2);
            }

            return b2.compareTo(b1);
        }

        case CN : {
            if (ourSortOrder == ASC_ORDER) {
                return myCn.compareTo(aObject.myCn);
            }

            return aObject.myCn.compareTo(myCn);
        }

        case DISTINGUISHEDNAME : {
            if (ourSortOrder == ASC_ORDER) {
                return myDistinguishedName.compareToIgnoreCase(aObject.myDistinguishedName);
            }

            return aObject.myDistinguishedName.compareToIgnoreCase(myDistinguishedName);
        }

        case NAME : {
            if (ourSortOrder == ASC_ORDER) {
                return myName.compareToIgnoreCase(aObject.myName);
            }

            return aObject.myName.compareToIgnoreCase(myName);
        }

        case MEMBEROF : {
            if (ourSortOrder == ASC_ORDER) {
                return myMemberOf.compareToIgnoreCase(aObject.myMemberOf);
            }

            return aObject.myMemberOf.compareToIgnoreCase(myMemberOf);
        }

        case MEMBER : {
            if (ourSortOrder == ASC_ORDER) {
                return myMember.compareToIgnoreCase(aObject.myMember);
            }

            return aObject.myMember.compareToIgnoreCase(myMember);
        }

        case MAILNICKNAME : {
            if (ourSortOrder == ASC_ORDER) {
                return myMailNickname.compareToIgnoreCase(aObject.myMailNickname);
            }

            return aObject.myMailNickname.compareToIgnoreCase(myMailNickname);
        }
        
        case SEX : 
        {
        	if( ourSortOrder == ASC_ORDER )
        	{
        		return mySex.compareToIgnoreCase(aObject.mySex) ;
        	}
        	return aObject.mySex.compareToIgnoreCase(mySex) ;
        	
        }
        
        case FIRM_CODE :
        {
        	if( ourSortOrder == ASC_ORDER )
        	{
        		return myFirmCode.compareToIgnoreCase(aObject.myFirmCode) ;
        	}
        	
        	return aObject.myFirmCode.compareToIgnoreCase(myFirmCode) ;
        }
        
        case FIRM_ADDRESS : 
        {
        	if( ourSortOrder == ASC_ORDER )
        	{
        		return myFirmAddress.compareToIgnoreCase(aObject.myFirmAddress) ;
        	}
        	
        	return aObject.myFirmAddress.compareToIgnoreCase(myFirmAddress) ;
        }
        
        case FULL_FIRM_NAME : 
        {
        	if( ourSortOrder == ASC_ORDER )
        	{
        		return myFullFirmName.compareToIgnoreCase(aObject.myFullFirmName) ;        		
        	}
        	
        	return aObject.myFullFirmName.compareToIgnoreCase(myFullFirmName);
        }
        
        case DESIGNATION : 
        {
        	if( ourSortOrder == ASC_ORDER )
        	{
        		return myDesignation.compareToIgnoreCase(aObject.myDesignation) ;
        	}
        	
        	return aObject.myDesignation.compareToIgnoreCase(myDesignation) ;
        }
        }

        return 0;
    }

    /**
     * This method is used to create the User object from the ResultSet
     *
     * @param  aResultSet the result object containing the fields
     * corresponding to a row of the User table in the database
     * @return the corresponding User object created from the ResutlSet
     */
    public static User createFromResultSet(ResultSet aResultSet) throws SQLException 
    {
    	/*
    	 * public User(int aUserId, String aUserLogin, String aFirstName, String aLastName, String aDisplayName, String aEmail, boolean aIsActive, int aUserTypeId, String aWebConfig, String aWindowsConfig,
            boolean aIsOnVacation, boolean aIsDisplay, String aCn, String aDistinguishedName, String aName, String aMemberOf, String aMember, String aMailNickname, String aLocation,
            String aExtension, String aMobile, String aHomePhone, String aFirmCode, String aDesignation, String aFirmAddress, String aSex, String aFullFirmName )
    	 */
        User user = new User(aResultSet.getInt(USER_ID_COLUMN_NAME), aResultSet.getString(USER_LOGIN_COLUMN_NAME), aResultSet.getString(FIRST_NAME_COLUMN_NAME), aResultSet.getString(LAST_NAME_COLUMN_NAME),
                             aResultSet.getString(DISPLAY_NAME_COLUMN_NAME), aResultSet.getString(EMAIL_COLUMN_NAME), aResultSet.getBoolean(IS_ACTIVE_COLUMN_NAME), aResultSet.getInt(USER_TYPE_ID_COLUMN_NAME),
                             aResultSet.getString(WEB_CONFIG_COLUMN_NAME), aResultSet.getString(WINDOWS_CONFIG_COLUMN_NAME), aResultSet.getBoolean(IS_ON_VACATION_COLUMN_NAME), aResultSet.getBoolean(IS_DISPLAY_COLUMN_NAME),
                             aResultSet.getString(CN_COLUMN_NAME), aResultSet.getString(DISTINGUISHED_NAME_COLUMN_NAME), aResultSet.getString(NAME_COLUMN_NAME), aResultSet.getString(MEMBER_OF_COLUMN_NAME), aResultSet.getString(MEMBER_COLUMN_NAME),
                             aResultSet.getString(MAIL_NICKNAME_COLUMN_NAME), aResultSet.getString(LOCATION_COLUMN_NAME), aResultSet.getString(EXTENSION_COLUMN_NAME), aResultSet.getString(MOBILE_COLUMN_NAME),
                             aResultSet.getString(HOME_PHONE_COLUMN_NAME)) ; 
//                           , aResultSet.getString(FIRM_CODE_COLUMN_NAME), aResultSet.getString(DESIGNATION_COLUMN_NAME), aResultSet.getString(FIRM_ADDRESS_COLUMN_NAME), aResultSet.getString(SEX_COLUMN_NAME), aResultSet.getString(FULL_FIRM_NAME_COLUMN_NAME));

        return user;
    }

    /**
     * This method is used to create the User object from the ResultSet
     * This method is in addition to createFromResultSet( ) method to handle the 
     * extended columns in users table
     * @param  aResultSet the result object containing the fields
     * corresponding to a row of the User table in the database
     * @return the corresponding User object created from the ResutlSet
     */
    public static User createFromResultSetAll(ResultSet aResultSet) throws SQLException 
    {
    	/*
    	 * public User(int aUserId, String aUserLogin, String aFirstName, String aLastName, String aDisplayName, String aEmail, boolean aIsActive, int aUserTypeId, String aWebConfig, String aWindowsConfig,
            boolean aIsOnVacation, boolean aIsDisplay, String aCn, String aDistinguishedName, String aName, String aMemberOf, String aMember, String aMailNickname, String aLocation,
            String aExtension, String aMobile, String aHomePhone, String aFirmCode, String aDesignation, String aFirmAddress, String aSex, String aFullFirmName )
    	 */
        User user = new User(aResultSet.getInt(USER_ID_COLUMN_NAME), aResultSet.getString(USER_LOGIN_COLUMN_NAME), aResultSet.getString(FIRST_NAME_COLUMN_NAME), aResultSet.getString(LAST_NAME_COLUMN_NAME),
                             aResultSet.getString(DISPLAY_NAME_COLUMN_NAME), aResultSet.getString(EMAIL_COLUMN_NAME), aResultSet.getBoolean(IS_ACTIVE_COLUMN_NAME), aResultSet.getInt(USER_TYPE_ID_COLUMN_NAME),
                             aResultSet.getString(WEB_CONFIG_COLUMN_NAME), aResultSet.getString(WINDOWS_CONFIG_COLUMN_NAME), aResultSet.getBoolean(IS_ON_VACATION_COLUMN_NAME), aResultSet.getBoolean(IS_DISPLAY_COLUMN_NAME),
                             aResultSet.getString(CN_COLUMN_NAME), aResultSet.getString(DISTINGUISHED_NAME_COLUMN_NAME), aResultSet.getString(NAME_COLUMN_NAME), aResultSet.getString(MEMBER_OF_COLUMN_NAME), aResultSet.getString(MEMBER_COLUMN_NAME),
                             aResultSet.getString(MAIL_NICKNAME_COLUMN_NAME), aResultSet.getString(LOCATION_COLUMN_NAME), aResultSet.getString(EXTENSION_COLUMN_NAME), aResultSet.getString(MOBILE_COLUMN_NAME),
                             aResultSet.getString(HOME_PHONE_COLUMN_NAME), 
                             aResultSet.getString(FIRM_CODE_COLUMN_NAME), aResultSet.getString(DESIGNATION_COLUMN_NAME), aResultSet.getString(FIRM_ADDRESS_COLUMN_NAME), aResultSet.getString(SEX_COLUMN_NAME), aResultSet.getString(FULL_FIRM_NAME_COLUMN_NAME), 
                             aResultSet.getString(OTHER_EMAILS_COLUMN_NAME));

        return user;
    }

    /**
     * Method that decides if two user objects are equal.
     *
     */
    public boolean equals(Object o) {
        if (o == null) {
            return false;
        }

        User user = null;

        try {
            user = (User) o;
        } catch (ClassCastException cce) {
            return false;
        }

        return user.myUserId == this.myUserId;
    }

    /**
     * Method that decides if two user objects are equal.
     *
     */
    public boolean equals(User user) {
        if (user == null) {
            return false;
        }

        return user.myUserId == this.myUserId;
    }

    /**
     * Method to insert an external User object formed from the given login
     *
     *
     * @param aEmail Object to be inserted
     * @return User Object.
     * @throws DatabaseException incase of database related errors.
     * @throws DESTBitsExceptioncase of general errors.
     */
    public static User insertExternalUser(String aEmail, int aUserType, User aUser, boolean shouldSendWelcomeMail) throws TBitsException, DatabaseException {

        // Insert logic here.
        if ((aEmail == null) || (aEmail.trim().equals("") == true)) {
            throw new TBitsException("Empty user email cannot be inserted.");
        }

        aEmail = aEmail.trim();

        Connection aCon = null;
        User       user = new User();

        user.setUserId(0);        // to start with, we set this to 0.
        user.setUserLogin(aEmail);
        user.setFirstName(aEmail);
        user.setLastName(aEmail);
        user.setDisplayName(aEmail);
        user.setEmail(aEmail);
        user.setIsActive(true);
        user.setUserTypeId(aUserType);
        user.setWebConfig("");    // Default config will be inserted in the SP.
        user.setWindowsConfig("");
        user.setIsOnVacation(false);
        user.setIsDisplay(true);
        user.setCn("");
        user.setDistinguishedName("");
        user.setName("");
        user.setMemberOf("");
        user.setMember("");
        user.setMailNickname("");
        user.setLocation("");
        user.setExtension("");
        user.setMobile("");
        user.setHomePhone("");

        try {
            aCon = DataSourcePool.getConnection();
            aCon.setAutoCommit(false);

            CallableStatement cs = aCon.prepareCall("stp_user_insertExternalUser " + "?, ?, ?, ?, ?, " + "?, ?, ?, ?, ?, " + "?, ?, ?, ?, ?, " + "?, ?, ?, ?, ?, " + "?, ?");

            cs.registerOutParameter(1, Types.INTEGER);
            user.setCallableParameters(cs);
            cs.execute();
            user.setUserId(cs.getInt(1));
            cs.close();
            aCon.commit();
            if(shouldSendWelcomeMail)
            {
	            String newPassword = RandomStringUtils.randomAlphanumeric(5);
	            AuthUtils.setPassword(aEmail, newPassword);
	            TBitsHelper.informExtUser(aEmail, newPassword, aUser);
            }
         // ADd this user to mapper only if no exception occurs. 
            // Add this new user to the Mapper.
            //
            Mapper.updateUser(user);
       } catch (SQLException sqle) {
        	try {
        		if(aCon != null)
					aCon.rollback();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
            StringBuilder message = new StringBuilder();

            message.append("Exception while inserting an external user.").append("Email: ").append(aEmail).append("\n");

            throw new DatabaseException(message.toString(), sqle);
        } catch (EventFailureException e) {
		e.printStackTrace();
		// ignoring the password related error in inserting external users.
	} finally {
            if (aCon != null) {
                try {
                    aCon.close();
                } catch (SQLException sqle) {

                    // Should this be logged.?
                }
            }
        }       
       
        return user;
    }

    /**
     * This method looks up the entire user database by email address.
     *returns null if the aEmail parameter provided is null or the user with 
     *that email is not found. 
     * @param  aEmail the mail id of the user
     *
     * @return the User object associated with the mail id.
     *
     * @throws DatabaseException In case of any database related error
     */
    public static User lookupAllByEmail(String aEmail) throws DatabaseException {
        User user = null;

        if (aEmail == null) {
            return user;
        }

        // Look in the mapper first.
        String key = aEmail.trim().toUpperCase();

        if (ourUserEmailMap != null) {
            user = ourUserEmailMap.get(key);

            return user;
        }

        // Else go to the database.
        Connection connection = null;

        try {
            connection = DataSourcePool.getConnection();

            CallableStatement cs = connection.prepareCall("stp_user_lookupByEmail ?");

            cs.setString(1, aEmail);

            ResultSet rs = cs.executeQuery();

            if (rs != null) {
                if (rs.next() != false) {
                    user = createFromResultSet(rs);                   
                }

                rs.close();
            }

            cs.close();
            rs = null;
            cs = null;
        } catch (SQLException sqle) {
            StringBuilder message = new StringBuilder();

            message.append("An exception occurred while retrieving the user.").append("\nUser Email: ").append(aEmail).append("\n");

            throw new DatabaseException(message.toString(), sqle);
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException sqle) {
                LOG.info("Exception while closing the connection");
            }
        }

        return user;
    }

    /**
     * Method to lookup the Entire User table by the user id.
     *
     * @param  aUserId the userId by which the table has to be looked up
     * @return the User object associated with the userId
     * @throws DatabaseException In case of any database related error
     */
    public static User lookupAllByUserId(int aUserId) throws DatabaseException {
        User user = null;

        // Look in the mapper first.
        Integer key = new Integer(aUserId);

        if (ourUserMap != null) {
            user = ourUserMap.get(key);

            return user;
        }

        // Else go to the database.
        Connection connection = null;

        try {
            connection = DataSourcePool.getConnection();

            CallableStatement cs = connection.prepareCall("stp_user_lookupByUserId ?");

            cs.setInt(1, aUserId);

            ResultSet rs = cs.executeQuery();

            if (rs != null) {
                if (rs.next() != false) {
                    user = (User) createFromResultSet(rs);
                }

                rs.close();
            }

            cs.close();
            rs = null;
            cs = null;
        } catch (SQLException sqle) {
            StringBuilder message = new StringBuilder();

            message.append("An exception occurred while retrieving the user.").append("\nUser Id: ").append(aUserId).append("\n");

            throw new DatabaseException(message.toString(), sqle);
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException sqle) {
                LOG.info("Exception while closing the connection");
            }
        }

        return user;
    }

    /**
     * Method to lookup the Entire User table by the user id.
     *
     * @param  aUserId the userId by which the table has to be looked up
     * @return the User object associated with the userId
     * @throws DatabaseException In case of any database related error
     */
    public static ArrayList<User> lookupAll() throws DatabaseException {
       
    	ArrayList<User>  users = null;
        
        // Else go to the database.
        Connection connection = null;

        try {
            connection = DataSourcePool.getConnection();
            Statement cs = connection.createStatement();

            ResultSet rs = cs.executeQuery("select * from users");

            if (rs != null) {
            	users = new ArrayList<User>();
                while (rs.next() != false) {
                	User user  = (User) createFromResultSetAll(rs);
                    users.add(user);
                }
                rs.close();
            }

            cs.close();
            rs = null;
            cs = null;
        } catch (SQLException sqle) {
            StringBuilder message = new StringBuilder();

            message.append("An exception occurred while retrieving the users.");

            throw new DatabaseException(message.toString(), sqle);
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException sqle) {
                LOG.info("Exception while closing the connection");
            }
        }

        return users;
    }
    /**
     * Method to lookup the Entire User table by the user login
     *
     * @param  aUserLogin the userId by which the table has to be looked up
     *
     * @return the User object associated with the userId
     *
     * @throws DatabaseException In case of any database related error
     */
    public static User lookupAllByUserLogin(String aUserLogin) throws DatabaseException {
        User user = null;

        if (aUserLogin == null) {
            return user;
        }

        // Look in the mapper first.
        String key = aUserLogin.trim().toUpperCase();

        if (ourUserLoginMap != null) {
            user = ourUserLoginMap.get(key);

            return user;
        }

        // Else go to the database.
        Connection connection = null;

        try {
            connection = DataSourcePool.getConnection();

            CallableStatement cs = connection.prepareCall("stp_user_lookupByUserLogin ?");

            cs.setString(1, aUserLogin);

            ResultSet rs = cs.executeQuery();

            if (rs != null) {
                if (rs.next() != false) {
                    user = (User) createFromResultSet(rs);                 
                }

                rs.close();
            }

            cs.close();
            rs = null;
            cs = null;
        } catch (SQLException sqle) {
            StringBuilder message = new StringBuilder();

            message.append("An exception occurred while retrieving the user.").append("\nUser Login: ").append(aUserLogin).append("\n");

            throw new DatabaseException(message.toString(), sqle);
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException sqle) {
                LOG.info("Exception while closing the connection");
            }
        }

        return user;
    }

    /**
     * Method to get the User object for the given mailid
     *
     * @param  aEmail the mail id of the user
     *
     * @return the User object associated with the mail id.
     *
     * @throws DatabaseException In case of any database related error
     */
    public static User lookupByEmail(String aEmail) throws DatabaseException {
        User user = null;

        if (aEmail == null) {
            return user;
        }

        // Look in the mapper first.
        String key = aEmail.trim().toUpperCase();

        if (ourUserEmailMap != null) {
            user = ourUserEmailMap.get(key);

            if (user != null) {
                if (user.getIsActive() == true) {
                    return user;
                } else {
                    return null;
                }
            } else {
                return null;
            }
        }

        // Else go to the database.
        Connection connection = null;

        try {
            connection = DataSourcePool.getConnection();

            CallableStatement cs = connection.prepareCall("stp_user_lookupByEmail ?");

            cs.setString(1, aEmail);

            ResultSet rs = cs.executeQuery();

            if (rs != null) {
                if (rs.next() != false) {
                    user = createFromResultSet(rs);
                }

                rs.close();
            }

            cs.close();
            rs = null;
            cs = null;
        } catch (SQLException sqle) {
            StringBuilder message = new StringBuilder();

            message.append("An exception occurred while retrieving the user.").append("\nUser Email: ").append(aEmail).append("\n");

            throw new DatabaseException(message.toString(), sqle);
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException sqle) {
                LOG.info("Exception while closing the connection");
            }
        }

        return user;
    }

    /**
     * Method to lookup the User table by the user id
     *
     * @param  aUserId the userId by which the table has to be looked up
     * @return the User object associated with the userId
     * @throws DatabaseException In case of any database related error
     */
    public static User lookupByUserId(int aUserId) throws DatabaseException {
        User user = null;

        // Look in the mapper first.
        Integer key = new Integer(aUserId);

        if (ourUserMap != null) {
            user = ourUserMap.get(key);

            if (user != null) {
                if (user.getIsActive() == true) {
                    return user;
                } else {
                    return null;
                }
            } else {
                return null;
            }
        }

        // Else go to the database.
        Connection connection = null;

        try {
            connection = DataSourcePool.getConnection();

            CallableStatement cs = connection.prepareCall("stp_user_lookupByUserId ?");

            cs.setInt(1, aUserId);

            ResultSet rs = cs.executeQuery();

            if (rs != null) {
                if (rs.next() != false) {
                    user = (User) createFromResultSet(rs);
                }

                rs.close();
            }

            cs.close();
            rs = null;
            cs = null;
        } catch (SQLException sqle) {
            StringBuilder message = new StringBuilder();

            message.append("An exception occurred while retrieving the user.").append("\nUser Id: ").append(aUserId).append("\n");

            throw new DatabaseException(message.toString(), sqle);
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException sqle) {
                LOG.info("Exception while closing the connection");
            }
        }

        return user;
    }
    /**
     * Method to check if the user already exists in the database.\
     * 
     * @param aUserLogin The user id which needs to be checked.
     * @return returns true if users exists otherwise false.
     * @throws DatabaseException In case of any database related error
     */
    public static boolean doesUserAlreadyExist(String aUserLogin) throws DatabaseException
    {
//    	 Else go to the database.
        Connection connection = null;
        boolean doesUserExist = false;
        try {
            LOG.info("Checking against db.");
            connection = DataSourcePool.getConnection();

            CallableStatement cs = connection.prepareCall("stp_user_lookupByUserLogin ?");

            cs.setString(1, aUserLogin);

            ResultSet rs = cs.executeQuery();

            if (rs != null) {
                if (rs.next()) {
                	System.out.println("Resultset has next");
                	doesUserExist = true;
                }
                rs.close();
            }

            cs.close();
            rs = null;
            cs = null;
        } catch (SQLException sqle) {
            LOG.warn(sqle);

            StringBuilder message = new StringBuilder();

            message.append("An exception occurred while retrieving the user.").append("\nUser Login: ").append(aUserLogin).append("\n");
            LOG.info(message.toString());

            throw new DatabaseException(message.toString(), sqle);
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException sqle) {
                LOG.warn("Exception while closing the connection");
            }
        }
        return doesUserExist;
    }
    /**
     * Method to lookup the User table by the login.
     * 
     * @param  aUserLogin the userId by which the table has to be looked up
     *
     * @return the User object associated with the login if the user is active
     *
     * @throws DatabaseException In case of any database related error
     */
    public static User lookupByUserLogin(String aUserLogin) throws DatabaseException {
        User user = null;

        if (aUserLogin == null) {
            return user;
        }

        // Look in the mapper first.
        String key = aUserLogin.trim().toUpperCase();

        if (ourUserLoginMap != null) {
            LOG.debug("Checking against ourUserMap");
            user = ourUserLoginMap.get(key);

            if (user != null) {
                if (user.getIsActive() == true) {
                    return user;
                } else {
                    return null;
                }
            } else {
                return null;
            }
        }

        // Else go to the database.
        Connection connection = null;

        try {
            LOG.info("Checking against db.");
            connection = DataSourcePool.getConnection();

            CallableStatement cs = connection.prepareCall("stp_user_lookupByUserLogin ?");

            cs.setString(1, aUserLogin);

            ResultSet rs = cs.executeQuery();

            if (rs != null) {
                if (rs.next() != false) {
                    user = (User) createFromResultSet(rs);
                    
                }

                rs.close();
            }

            cs.close();
            rs = null;
            cs = null;
        } catch (SQLException sqle) {
            LOG.warn(sqle);

            StringBuilder message = new StringBuilder();

            message.append("An exception occurred while retrieving the user.").append("\nUser Login: ").append(aUserLogin).append("\n");
            LOG.info(message.toString());

            throw new DatabaseException(message.toString(), sqle);
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException sqle) {
                LOG.warn("Exception while closing the connection");
            }
        }

        return user;
    }

    /**
     * Method to lookup the Entire User table by the user login
     *
     * @param  aUserLogin the userId by which the table has to be looked up
     *
     * @return the User object associated with the userId
     *
     * @throws DatabaseException In case of any database related error
     */
    public static ArrayList<User> lookupByUserLoginLike(String aUserLogin, boolean aNoInActive) throws DatabaseException {
        ArrayList<User> userList   = new ArrayList<User>();
        Connection      connection = null;

        try {
            connection = DataSourcePool.getConnection();

            CallableStatement cs = connection.prepareCall("stp_user_lookupAllByUserLoginLike ?, ?");

            cs.setString(1, aUserLogin);
            cs.setBoolean(2, aNoInActive);

            ResultSet rs = cs.executeQuery();

            if (rs != null) {
                while (rs.next() != false) {
                    int  userId = rs.getInt(USER_ID_COLUMN_NAME);
                    User user   = User.lookupAllByUserId(userId);

                    userList.add(user);
                }

                rs.close();
            }

            cs.close();
            rs = null;
            cs = null;
        } catch (SQLException sqle) {
            StringBuilder message = new StringBuilder();

            message.append("An exception occurred while retrieving the users ").append("whose login is like ").append(aUserLogin).append("\n");

            throw new DatabaseException(message.toString(), sqle);
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException sqle) {
                LOG.info("Exception while closing the connection");
            }
        }

        return userList;
    }

    /**
     * Method to return the source arraylist in the sorted order
     *
     * @param  source the array list of Type objects
     * @return the ArrayList of the User objects in sorted order
     */
    public static ArrayList<User> sort(ArrayList<User> source) {
        int    size     = source.size();
        User[] srcArray = new User[size];

        for (int i = 0; i < size; i++) {
            srcArray[i] = source.get(i);
        }

        Arrays.sort(srcArray, new UserComparator());

        ArrayList<User> target = new ArrayList<User>();

        for (int i = 0; i < size; i++) {
            target.add(srcArray[i]);
        }

        return target;
    }

    public int hashCode()
    {
    	return myUserId;
    }
    
    /**
     * This method returns the String Representation of object of this class.
     *
     * @return String representation.
     */
    public String toString() {
        StringBuilder message = new StringBuilder();

        message.append("[ ").append(myUserId).append(", ").append(myUserLogin).append(", ").append(myDisplayName).append(" ]");

        return message.toString();
    }

    /**
     * Method to update the corresponding User object in the database.
     *
     * @param aObject Object to be updated
     *
     * @return Update domain object.
     *
     */
    public static User update(User aObject) throws DatabaseException {

        // Update logic here.
        if (aObject == null) {
            return aObject;
        }

        Connection aCon = null;

        try {
        	BeforeUserUpdateEvent buue = new BeforeUserUpdateEvent(aObject);
        	EventManager.getInstance().fireEvent(buue);
            aCon = DataSourcePool.getConnection();
            aCon.setAutoCommit(false);

            CallableStatement cs = aCon.prepareCall("stp_user_update  " + "?, ?, ?, ?, ?, " + "?, ?, ?, ?, ?, " + "?, ?, ?, ?, ?, " + "?, ?, ?, ?, ?, " + "?, ?," + "?, ?, ?, ?, ?, ?");

            aObject.setCallableParametersAll(cs);
            cs.execute();
            cs.close();
            aCon.commit();
            
            // update in the Mapper should occur only if the database update was successfull            //
            // Update this  user in the Mapper
            Mapper.updateUser(aObject);
            
        } catch (SQLException sqle) {
        	try {
        		if(aCon != null)
					aCon.rollback();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
            StringBuilder message = new StringBuilder();

            message.append("An exception occurred while updating a user record.").append("\nUser Id: " + aObject.getUserId());
            LOG.severe(message.toString(), sqle);

            throw new DatabaseException(message.toString(), sqle);
        } catch (EventFailureException e) 
        {
			e.printStackTrace();
			throw new DatabaseException(e.getMessage(),e);
		} finally {
            if (aCon != null) {
                try {
                    aCon.close();
                } catch (SQLException sqle) {

                    // Should this be logged.?
                }
            }
        }

        return aObject;
    }

    //~--- get methods --------------------------------------------------------

    public String get(UserColumn column) {
        String value = "";

        switch (column) {
        case USER_ID :
            value = Integer.toString(myUserId);

            break;

        case USER_LOGIN :
            value = myUserLogin;

            break;

        case FIRST_NAME :
            value = myFirstName;

            break;

        case LAST_NAME :
            value = myLastName;

            break;

        case DISPLAY_NAME :
            value = myDisplayName;

            break;

        case EMAIL :
            value = myEmail;

            break;

        case IS_ACTIVE :
            value = Boolean.toString(myIsActive);

            break;

        case USER_TYPE_ID :
            value = Integer.toString(myUserTypeId);

            break;

        case WEB_CONFIG :
            value = myWebConfig;

            break;

        case WINDOWS_CONFIG :
            value = myWindowsConfig;

            break;

        case IS_ON_VACATION :
            value = Boolean.toString(myIsOnVacation);

            break;

        case IS_DISPLAY :
            value = Boolean.toString(myIsDisplay);

            break;

        case CN :
            value = myCn;

            break;

        case DISTINGUISHED_NAME :
            value = myDistinguishedName;

            break;

        case NAME :
            value = myName;

            break;

        case MEMBER_OF :
            value = myMemberOf;

            break;

        case MEMBER :
            value = myMember;

            break;

        case MAIL_NICKNAME :
            value = myMailNickname;

            break;

        case LOCATION :
            value = myLocation;

            break;

        case EXTENSION :
            value = myExtension;

            break;

        case MOBILE :
            value = myMobile;

            break;

        case HOME_PHONE :
            value = myHomePhone;

            break;
            
        case FIRM_CODE :
        	value = myFirmCode ;
        	break ;
        	
        case FIRM_ADDRESS : 
        	value = myFirmAddress ;
        	break ;
        	
        case FULL_FIRM_NAME : 
        	value = myFullFirmName ;
        	break ;
        	
        case SEX :
        	value = mySex ;
        	break ;
        
        case DESIGNATION : 
        	value = myDesignation ;
        	break ;     	
        
        }

        return value;
    }

    /**
     * This method returns all active User objects
     *
     * @return Arraylist of active user Object
     */
    public static ArrayList<User> getActiveUsers() {
        return getUsers(UserStatus.Active);
    }
   
    /**
     * This method returns all User objects
     *
     * @return Arraylist of active user Object
     */
    public static ArrayList<User> getAllUsers() {
        return getUsers(UserStatus.Any);
    }
    
    /*
     * Gets the users 
     *
     */
    public static ArrayList<User> getUsers(UserStatus userStatus)
    {
        ArrayList<User> UsersList  = new ArrayList<User>();
        User            user       = null;
        Collection      collection = ourUserMap.values();
        Iterator        iterator   = collection.iterator();

        while (iterator.hasNext()) {
            user = (User) iterator.next();
            if(
                    ((userStatus == UserStatus.Active) && (user.getIsActive()))
                    || ((userStatus == UserStatus.InActive) && !user.getIsActive())
                    || (userStatus == UserStatus.Any)
                    && !UsersList.contains(user)
              )
            {
            	if(!UsersList.contains(user))
            		UsersList.add(user);
            }
            
        }

        User.setSortParams(0, 0);
        UsersList = User.sort(UsersList);

        return UsersList;
    }

    /**
     * Accessor method for Cn property.
     *
     * @return Current Value of Cn
     *
     */
    public String getCn() {
        return myCn;
    }

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
     * Accessor method for DistinguishedName property.
     *
     * @return Current Value of DistinguishedName
     *
     */
    public String getDistinguishedName() {
        return myDistinguishedName;
    }

    /**
     * Accessor method for Email property.
     *
     * @return Current Value of Email
     *
     */
    public String getEmail() {
        return myEmail;
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
     * Accessor method for IsDisplay property.
     *
     * @return Current Value of IsDisplay
     *
     */
    public boolean getIsDisplay() {
        return myIsDisplay;
    }

    /**
     * Accessor method for IsOnVacation property.
     *
     * @return Current Value of IsOnVacation
     *
     */
    public boolean getIsOnVacation() {
        return myIsOnVacation;
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
     * This method returns the list of users present in the mail list
     * corresponding to the given mail_list_userId.
     *
     * @param aUserId User Id
     *
     * @return List of User objects.
     *
     * @exception DatabaseException
     */
    public static ArrayList<User> getMailListUsers(int aUserId) throws DatabaseException {
        ArrayList<User> list = new ArrayList<User>();

        // Look in the mapper.
        if (ourMailListUserMap != null) {
            Integer         key  = new Integer(aUserId);
            ArrayList<User> temp = ourMailListUserMap.get(key);

            if (temp != null) {
                list.addAll(temp);
            }
        }

        return list;
    }

    /**
     * Accessor method for MailNickname property.
     *
     * @return Current Value of MailNickname
     *
     */
    public String getMailNickname() {
        return myMailNickname;
    }

    /**
     * Accessor method for Member property.
     *
     * @return Current Value of Member
     *
     */
    public String getMember() {
        return myMember;
    }

    /**
     * Accessor method for MemberOf property.
     *
     * @return Current Value of MemberOf
     *
     */
    public String getMemberOf() {
        return myMemberOf;
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
     * Accessor method for Name property.
     *
     * @return Current Value of Name
     *
     */
    public String getName() {
        return myName;
    }

    /**
     * Accessor method for UserId property.
     *
     * @return Current Value of UserId
     *
     */
    public int getUserId() {
        return myUserId;
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
     * Accessor method for UserTypeId property.
     *
     * @return Current Value of UserTypeId
     *
     */
    public int getUserTypeId() {
        return myUserTypeId;
    }

    /**
     * Accessor method for WebConfig property.
     *
     * @return Current Value of WebConfig
     *
     */
    public String getWebConfig() {
        return myWebConfig;
    }

    /**
     * Accessor method for WebConfigObject property.
     *
     * @return Current Value of WebConfigObject
     *
     */
    public WebConfig getWebConfigObject() {
        if (myWebConfigObject == null) {
            try {
                myWebConfigObject = WebConfig.getWebConfig(myWebConfig);
            } catch (Exception e) {
                LOG.warn("",(e));
            }
        }

        return myWebConfigObject;
    }

    /**
     * Accessor method for WindowsConfig property.
     *
     * @return Current Value of WindowsConfig
     *
     */
    public String getWindowsConfig() {
        return myWindowsConfig;
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
        aCS.setInt(USERID, myUserId);
        aCS.setString(USERLOGIN, myUserLogin);
        aCS.setString(FIRSTNAME, myFirstName);
        aCS.setString(LASTNAME, myLastName);
        aCS.setString(DISPLAYNAME, myDisplayName);
        aCS.setString(EMAIL, myEmail);
        aCS.setBoolean(ISACTIVE, myIsActive);
        aCS.setInt(USERTYPEID, myUserTypeId);
        aCS.setString(WEBCONFIG, myWebConfig);
        aCS.setString(WINDOWSCONFIG, myWindowsConfig);
        aCS.setBoolean(ISONVACATION, myIsOnVacation);
        aCS.setBoolean(ISDISPLAY, myIsDisplay);
        aCS.setString(CN, myCn);
        aCS.setString(DISTINGUISHEDNAME, myDistinguishedName);
        aCS.setString(NAME, myName);
        aCS.setString(MEMBEROF, myMemberOf);
        aCS.setString(MEMBER, myMember);
        aCS.setString(MAILNICKNAME, myMailNickname);
        aCS.setString(LOCATION, myLocation);
        aCS.setString(EXTENSION, myExtension);
        aCS.setString(MOBILE, myMobile);
        aCS.setString(HOMEPHONE, myHomePhone);
    }
    
    /**
     * This method sets the parameters in the CallableStatement.
     * This method is in addition to the setCallableParameters(CallableStatement aCS) 
     * to cater the newly added columns in the users table
     *
     * @param aCS     CallableStatement whose params should be set.
     *
     * @exception SQLException
     */
    public void setCallableParametersAll(CallableStatement aCS) throws SQLException {
        aCS.setInt(USERID, myUserId);
        aCS.setString(USERLOGIN, myUserLogin);
        aCS.setString(FIRSTNAME, myFirstName);
        aCS.setString(LASTNAME, myLastName);
        aCS.setString(DISPLAYNAME, myDisplayName);
        aCS.setString(EMAIL, myEmail);
        aCS.setBoolean(ISACTIVE, myIsActive);
        aCS.setInt(USERTYPEID, myUserTypeId);
        aCS.setString(WEBCONFIG, myWebConfig);
        aCS.setString(WINDOWSCONFIG, myWindowsConfig);
        aCS.setBoolean(ISONVACATION, myIsOnVacation);
        aCS.setBoolean(ISDISPLAY, myIsDisplay);
        aCS.setString(CN, myCn);
        aCS.setString(DISTINGUISHEDNAME, myDistinguishedName);
        aCS.setString(NAME, myName);
        aCS.setString(MEMBEROF, myMemberOf);
        aCS.setString(MEMBER, myMember);
        aCS.setString(MAILNICKNAME, myMailNickname);
        aCS.setString(LOCATION, myLocation);
        aCS.setString(EXTENSION, myExtension);
        aCS.setString(MOBILE, myMobile);
        aCS.setString(HOMEPHONE, myHomePhone);
        aCS.setString(FIRM_CODE, myFirmCode) ;
        aCS.setString(DESIGNATION, myDesignation) ;
        aCS.setString(FIRM_ADDRESS, myFirmAddress) ;
        aCS.setString(SEX, mySex) ;
        aCS.setString(FULL_FIRM_NAME, myFullFirmName) ;
        aCS.setString(OTHER_EMAILS, getOtherEmailsAsCommaSeparatedStr()) ;
        
    }

    /**
     * Mutator method for Cn property.
     *
     * @param aCn New Value for Cn
     *
     */
    public void setCn(String aCn) {
        myCn = aCn;
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
     * Mutator method for DistinguishedName property.
     *
     * @param aDistinguishedName New Value for DistinguishedName
     *
     */
    public void setDistinguishedName(String aDistinguishedName) {
        myDistinguishedName = aDistinguishedName;
    }

    /**
     * Mutator method for Email property.
     *
     * @param aEmail New Value for Email
     *
     */
    public void setEmail(String aEmail) {
        myEmail = aEmail;
    }
    
    public void setOtherEmails(Collection<? extends String> otherEmails)
    {
    	this.myOtherEmails.clear();
    	if(otherEmails != null)
    		this.myOtherEmails.addAll(otherEmails);
    }
    
    public void setOtherEmails(String otherEmails)
    {
    	this.myOtherEmails.clear();
    	if( (otherEmails != null) && (otherEmails.length() > 0) )
		{
    		for(String s:otherEmails.split(","))
    		{
    			if( (s != null) && (s.trim().length() > 0) )
    					this.myOtherEmails.add(s);
    		}
		}
    }
    
    public String[] getOtherEmails()
    {
    	return this.myOtherEmails.toArray(new String[]{});
    }
    
    
    public String getOtherEmailsAsCommaSeparatedStr()
    {
    	StringBuilder sb = new StringBuilder();
    	boolean isFirst = true;
    	for(String s:this.myOtherEmails)
    	{
    		if(isFirst)
    			isFirst = false;
    		else 
    			sb.append(",").append(s);
    	}
    	return sb.toString();
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
     * Mutator method for IsDisplay property.
     *
     * @param aIsDisplay New Value for IsDisplay
     *
     */
    public void setIsDisplay(boolean aIsDisplay) {
        myIsDisplay = aIsDisplay;
    }

    /**
     * Mutator method for IsOnVacation property.
     *
     * @param aIsOnVacation New Value for IsOnVacation
     *
     */
    public void setIsOnVacation(boolean aIsOnVacation) {
        myIsOnVacation = aIsOnVacation;
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
    public void setLocation(String aLocation) 
    {
    	if( null == aLocation )
    		aLocation = " " ;
        myLocation = aLocation.trim() ;
    }

    /**
     * Mutator method for MailNickname property.
     *
     * @param aMailNickname New Value for MailNickname
     *
     */
    public void setMailNickname(String aMailNickname) {
        myMailNickname = aMailNickname;
    }

    /**
     * Mutator method for Member property.
     *
     * @param aMember New Value for Member
     *
     */
    public void setMember(String aMember) {
        myMember = aMember;
    }

    /**
     * Mutator method for MemberOf property.
     *
     * @param aMemberOf New Value for MemberOf
     *
     */
    public void setMemberOf(String aMemberOf) {
        myMemberOf = aMemberOf;
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
     * Mutator method for Name property.
     *
     * @param aName New Value for Name
     *
     */
    public void setName(String aName) {
        myName = aName;
    }

    /**
     * Mutator method for SortField property.
     *
     * @param aSortField New Value of SortField
     *
     */
    public static void setSortField(int aSortField) {
        ourSortField = aSortField;
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
    public static void setSortParams(int aSortField, int aSortOrder) {
        ourSortField = aSortField;
        ourSortOrder = aSortOrder;
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
     * Mutator method for UserTypeId property.
     *
     * @param aUserTypeId New Value for UserTypeId
     *
     */
    public void setUserTypeId(int aUserTypeId) {
        myUserTypeId = aUserTypeId;
    }

    /**
     * Mutator method for WebConfig property.
     *
     * @param aWebConfig New Value for WebConfig
     *
     */
    public void setWebConfig(String aWebConfig) {
        myWebConfig = aWebConfig;
    }

    /**
     * Mutator method for WebConfiObject property.
     *
     * @param aWebConfigObject New Value for WebConfigObject
     *
     */
    public void setWebConfigObject(WebConfig aWebConfigObject) {
        myWebConfigObject = aWebConfigObject;
    }

    /**
     * Mutator method for WindowsConfig property.
     *
     * @param aWindowsConfig New Value for WindowsConfig
     *
     */
    public void setWindowsConfig(String aWindowsConfig) {
        myWindowsConfig = aWindowsConfig;
    }
    
    public String getFirmCode()
    {
    	return myFirmCode ;
    }
    
    public String getDesignation()
    {
    	return myDesignation ;
    }
    
    public String getFirmAddress()
    {
    	return myFirmAddress ;
    }
    
    public String getSex()
    {
    	return mySex ;
    }
    
    public String getFullFirmName()
    {
    	return myFullFirmName ;
    }
    
    public void setFirmCode( String firmCode ) 
    {
    	if( null == firmCode )
    		firmCode = "" ;
		
		myFirmCode = firmCode.trim() ;   	
    }
    
    public void setDesignation( String designation)
    {
    	if(null == designation )
    		designation = "" ;
    	
    	myDesignation = designation.trim() ;
    	
    }
    
    public void setFirmAddress( String firmAddress )
    {
    	if( null == firmAddress )
    		firmAddress = "" ;
    	
    		myFirmAddress = firmAddress.trim() ;
    }
    
    public void setSex( String sex )
    {    	
    	if( null != sex )
    	{
    		sex = sex.trim() ;
    	}
    	
    	if( null == sex || sex.length() == 0 ) 
    		sex = " " ;
    	// not checking whether the first letter is a Alpha or other thing. Just trying to convert it
    	// to upper case set it .. 
    	mySex = sex.toUpperCase().substring(0,1) ;
    }
    
    public void setFullFirmName(String fullFirmName ) 
    {
    	if( null == fullFirmName )
    		fullFirmName = "" ;

    	myFullFirmName = fullFirmName.trim() ;
    }
    
    public static void main(String argv[])
    {   
//    	try {
//			System.setOut(new PrintStream("/home/nitiraj/checkouts/tmp/users/output")) ;
//		} catch (FileNotFoundException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		System.out.println("Our User email Map =\n" + ourUserEmailMap ) ;  
//		
//		return ;
    }
}


/**
 * This class is the comparator for domain object corresponding to the users
 * table in the database.
 *
 * @author  :
 * @version : $Id: $
 *
 */
class UserComparator implements Comparator<User>, Serializable {
    public int compare(User obj1, User obj2) {
        return obj1.compareTo(obj2);
    }
}
