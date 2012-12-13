package kskCorres;

import java.util.ArrayList;
import java.util.Hashtable;

import transbit.tbits.common.DatabaseException;
import transbit.tbits.common.TBitsLogger;
import transbit.tbits.domain.User;
import transbit.tbits.exception.TBitsException;

import com.google.gson.Gson;

/**
 * 
 * @author nitiraj
 *
 */
/**
 * This is a helper class to manipulate the database table ksk_user_info, as required by
 * the KSK correspondence project
 */
public class UserInfoManager 
{
	public static final String PKG_KSK = "kskCorres";
	public static final TBitsLogger LOG = TBitsLogger.getLogger(PKG_KSK);
		
	public static final String TABLE_NAME = "users" ;	
	public static final String USER_ID = "user_id" ; // int (primary) NOT NULL
	public static final String FIRM = "firm_code" ; // varchar(100) NOT NULL // short name of the firm 
	public static final String LOCATION = "location" ; // varchar(2) NULL 
	public static final String DESIGNATION = "designation" ; // varchar(50) NULL  
	public static final String ADDRESS = "firm_address" ; // varchar(500) NULL 
	public static final String SEX = "sex" ;// ENUM('F','M') NULL
	public static final String FULL_FIRM_NAME = "full_firm_name" ; // varchar(200) NULL // full name of firm 

	/**
	 * gives the complete mapping of userLogin:location for users where firm='WPCL' 
	 * @return
	 * @throws TBitsException 
	 */	
	public static Hashtable<String,Hashtable<String,String>> lookupUserInfoWithFirm(String firm) throws TBitsException
	{
		if( null == firm || firm.trim().equals(""))
			throw new TBitsException( "Firm cannot be empty or null" ) ;
		
		Hashtable<String,Hashtable<String,String>> ul = new Hashtable<String,Hashtable<String,String>>() ;
		
		ArrayList<User> userList = User.getAllUsers() ;
		for( User user : userList )
		{
			String myFirm = user.getFirmCode() ;
			if( null == myFirm || ! myFirm.trim().equalsIgnoreCase(firm))
				continue ;
			
			String location = user.getLocation() ;
			if( null == location || location.trim().equals("") )
				continue ;
			
			String designation = user.getDesignation() ;
			if( null == designation ) designation = "" ;
			String address = user.getFirmAddress() ;
			if( null == address ) address = "" ;
			String sex = user.getSex() ;
			if( null == sex ) sex = "" ;
			String fullFirmName = user.getFullFirmName() ;
			if( null == fullFirmName ) fullFirmName = "" ;
			
			Hashtable<String,String> info = new Hashtable<String,String>() ;
			info.put(FIRM, myFirm) ;
			info.put(LOCATION, location) ;
			info.put(DESIGNATION, designation) ;
			info.put(ADDRESS, address);
			info.put(SEX, sex) ;
			info.put(FULL_FIRM_NAME, fullFirmName);
			
			ul.put(user.getUserLogin(), info);
		}
		
		return ul ;			
	}
	/**
	 * 
	 * @param user_id
	 * @return null if user doesnot exist, else a Hashtable containing following key-value pairs 
	 * firm = String
	 * location = String
	 * designation = String
	 * address = String 
	 * sex = String ( F/M )
	 * @throws TBitsException
	 */
	public static Hashtable<String, String> getUserInfo( int user_id ) throws TBitsException
	{
		User user;
		try {
			user = User.lookupByUserId(user_id);
		} 
		catch (DatabaseException e) 
		{
			e.printStackTrace();
			throw new TBitsException("User does not exist in database table:" + TABLE_NAME ) ;
		}
		Hashtable<String, String > info = new Hashtable<String,String>() ;
			
		if( null == user || 
			null == user.getFirmCode() || user.getFirmCode().trim().equals("") ||
			null == user.getLocation() || user.getLocation().trim().equals("")
		  )
		 {
			LOG.info("Required UserInfo does not exist in database table: " + TABLE_NAME) ;
			throw new TBitsException("Required UserInfo does not exist in database table: " + TABLE_NAME) ; 
		 }
		
		// otherwise extract data 
		String firm = user.getFirmCode().trim() ;
		String location = user.getLocation().trim();
		String designation = user.getDesignation() ; 
		if( null == designation ) designation = "" ;
		String address = user.getFirmAddress() ;
		if( null == address) address = "" ;
		String sex = user.getSex() ;
		if( null == sex ) sex = "" ;
		String full_firm_name = user.getFullFirmName() ; 
		if( null == full_firm_name ) full_firm_name = "" ;
				
		info.put(FIRM, firm ) ;
		info.put(LOCATION, location ) ;
		info.put( DESIGNATION , designation ) ;
		info.put(ADDRESS , address ) ; 
		info.put(SEX, sex ) ;
		info.put(FULL_FIRM_NAME, full_firm_name) ;
		
		return info ;	
	}
	public static void main( String argv[] )
	{
		try {
			System.out.println( new Gson().toJson(lookupUserInfoWithFirm("SEPCO")) ) ;
		} catch (TBitsException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}											
}
