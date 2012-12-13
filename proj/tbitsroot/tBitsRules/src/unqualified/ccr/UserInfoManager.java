package ccr;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Hashtable;

import transbit.tbits.common.DataSourcePool;
import transbit.tbits.common.TBitsLogger;
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
	private static final String ERR_DB_ACCESS = "Exception while accessing the database table the database";
	private static final String ERR_CON_ONCLOSE = "Exception while closing the connection object.";
	private static final String ERR_CON_CREATE = "SQLException while creating the connection.";
	private static final String ERR_CON_ACCESS = "SQLException while accessing the connection object";
	private static final String ERR_CON_NULL_CLOSED = "The supplied connection object was null or was closed.";

	public static final String PKG_NAME = "ccr";
	/**
	 * logger for this class
	 */
	public static final TBitsLogger LOG = TBitsLogger.getLogger(PKG_NAME);
	
	/**
	 * @param TABLE_NAME the name of the table
	 * Table Structure 
	 * userid(primary) | Name of firm | location | designation | Address | sex 
	 */	
	public static final String TABLE_NAME = "ccr_user_info" ;
	
	public static final String USER_ID = "user_id" ; // int (primary) NOT NULL
	public static final String FIRM = "firm" ; // varchar(100) NOT NULL // short name of the firm 
	public static final String LOCATION = "location" ; // varchar(2) NULL 
	public static final String DESIGNATION = "designation" ; // varchar(50) NULL  
	public static final String ADDRESS = "address" ; // varchar(500) NULL 
	public static final String SEX = "sex" ;// ENUM('F','M') NULL
	public static final String FULL_FIRM_NAME = "full_firm_name" ; // varchar(200) NULL // full name of firm 
	
	public static final String CREATE_TABLE = "CREATE TABLE " + TABLE_NAME +
											  " ( " +
											  USER_ID + " INT NOT NULL, " +		
											  " PRIMARY KEY (" + USER_ID + "), " +
											  FIRM + " VARCHAR(20) NOT NULL, " + 
											  LOCATION + " VARCHAR(2) NOT NULL, " +										
											  DESIGNATION + " VARCHAR(50) NULL, " +
											  ADDRESS + " VARCHAR(500) NULL, " +
											  SEX + " VARCHAR(1) NULL, " +
											  FULL_FIRM_NAME + " VARCHAR(200) NULL "+
										     " ) " ;
	
	
	public static final String DROP_TABLE = " DROP TABLE " + TABLE_NAME ;
	
	public static final String ADD_USER_FIRM_LOCATION = "INSERT INTO " + TABLE_NAME +
	  									  " VALUES (?,?,?,NULL,NULL,NULL,NULL) " ;
	
	public static final String ADD_COMPLETE = "INSERT INTO " + TABLE_NAME +
	  										   " VALUES (?,?,?,?,?,?,?) " ;
	
	public static final String UPDATE_FIRM = "UPDATE " + TABLE_NAME +
	  										 " SET " + FIRM + "=?" +
	  										 " WHERE " + USER_ID + "=?";
	
	public static final String UPDATE_LOCATION = "UPDATE " + TABLE_NAME +
												 " SET " + LOCATION + "=?" +
												 " WHERE " + USER_ID + "=?";
	
	public static final String UPDATE_ADDRESS = "UPDATE " + TABLE_NAME +
												 " SET " + ADDRESS + "=?" +
												 " WHERE " + USER_ID + "=?";
	
	public static final String UPDATE_DESIGNATION = "UPDATE " + TABLE_NAME +
													 " SET " + DESIGNATION + "=?" +
													 " WHERE " + USER_ID + "=?";
	
	public static final String UPDATE_SEX = "UPDATE " + TABLE_NAME +
											 " SET " + SEX + "=?" +
											 " WHERE " + USER_ID + "=?";
	
	public static final String UPDATE_ALL = "UPDATE " + TABLE_NAME +
											 " SET " + FIRM + "=?" +
											 " SET " + LOCATION + "=?" +
											 " SET " + DESIGNATION + "=?" +
											 " SET " + ADDRESS + "=?" +
											 " SET " + SEX + "=?" +
											 " SET " + FULL_FIRM_NAME + "=?" +
											 " WHERE " + USER_ID + "=?";
	
	public static final String USER_EXISTS = "SELECT * " + 
											 " FROM " + TABLE_NAME + 
											 " WHERE " + USER_ID + "=?" ;
	
	public static final String DELETE_USER = "DELETE FROM " + TABLE_NAME + 
											 " WHERE " + USER_ID + "=?" ;
	
	public static final String USER_INFO = "SELECT " + USER_ID +" , " + FIRM + " , " + LOCATION + " , " + DESIGNATION + " , " + ADDRESS + " , " + SEX + " , " + FULL_FIRM_NAME +
										   " FROM " + TABLE_NAME +
										   " WHERE " + USER_ID + "=?" ;
	
	public static final String USER_LOGIN_INFO = " SELECT users.user_login, " + TABLE_NAME + "." + FIRM + "," + TABLE_NAME + "." + LOCATION + "," + TABLE_NAME + "." + DESIGNATION + "," + TABLE_NAME + "." + ADDRESS + "," + TABLE_NAME + "." + SEX + "," + TABLE_NAME + "." + FULL_FIRM_NAME +  
											   " FROM users JOIN " + TABLE_NAME + " ON users.user_id=" + TABLE_NAME + "." + USER_ID +
											   " WHERE "  + TABLE_NAME + "." + FIRM + "=?" ;
	/**
	 * gives the complete mapping of userLogin:location for users where firm='WPCL' 
	 * @return
	 * @throws TBitsException 
	 */
	public static Hashtable<String,Hashtable<String,String>> lookupUserInfoWithFirm(String firm) throws TBitsException
	{
		Connection con = null ;
		Hashtable<String,Hashtable<String,String>> ul = new Hashtable<String,Hashtable<String,String>>() ;
		
		try
		{
			con = DataSourcePool.getConnection();
			PreparedStatement ps = con.prepareStatement(USER_LOGIN_INFO) ;
			ps.setString(1, firm) ;
			ResultSet rs = ps.executeQuery() ;
			while( rs.next() ) 
			{
				String l = rs.getString("user_login" ) ;
				Hashtable<String,String> info = new Hashtable<String,String>() ;
				String ufirm = rs.getString(FIRM) ;
				if( null == ufirm )
					ufirm = "" ;
				info.put(FIRM, ufirm ) ;
				
				String uloc = rs.getString(LOCATION) ;
				if( null == uloc )
					uloc = "" ;				
				info.put(LOCATION, uloc ) ;
				
				String udesignation = rs.getString(DESIGNATION) ;
				if( null == udesignation )
					udesignation = "" ;				
				info.put(DESIGNATION,udesignation ) ;
				
				String uadd = rs.getString(ADDRESS) ; 
				if( null == uadd )
					uadd = "" ;				
				info.put(ADDRESS, uadd ) ;
				
				String usex = rs.getString(SEX) ;
				if( null == usex )
					usex = "" ;
				info.put(SEX, usex ) ;
				
				String uffn = rs.getString(FULL_FIRM_NAME) ;
				if( null == uffn )
					uffn = "" ;
				info.put(FULL_FIRM_NAME, uffn ) ;
				
				ul.put( l, info ) ;
			}
		} catch (SQLException e) 
		{		
			e.printStackTrace();
			LOG.error("Exception while getting userlogin + location info." ) ;
			throw new TBitsException("Exception while getting userlogin + location info.",e) ;
		}
		finally
		{
			if( null != con )
				try {
					con.close();
				} catch (SQLException e) 
				{					
					e.printStackTrace();
					LOG.error(ERR_CON_ONCLOSE) ;					
				}			
		}
		
		return ul ;
	}
	public static int addComplete( int user_id, String firm, String location, String designation, String address, String sex, String ffn ) throws TBitsException 
	{
		Connection con = null ;
		if( null == firm || null == location )
			throw new TBitsException("Firm and Location cannot be null.") ;
		
		if( null == designation )
			designation = "" ;
		if( null == address ) 
			address = "" ;
		if( null == sex ) 
			sex = "" ;
		if( null == ffn ) 
			ffn = "" ;
		
		try
		{
				con = DataSourcePool.getConnection() ;
				PreparedStatement pstmt = con.prepareStatement(ADD_COMPLETE) ;
				pstmt.setInt(1, user_id ) ;
				pstmt.setString(2, firm ) ;
				pstmt.setString(3, location ) ;
				pstmt.setString(4, designation ) ;
				pstmt.setString(5, address ) ;
				pstmt.setString(6, sex ) ;
				pstmt.setString(7, ffn) ;
				int n = pstmt.executeUpdate() ;
				LOG.info("The user was successfully added.") ;
				return n ;
	
		}
		catch( SQLException e )
		{
			e.printStackTrace() ;
			LOG.error(ERR_DB_ACCESS ) ;
			throw new TBitsException( ERR_DB_ACCESS, e ) ;
		}
		finally
		{
			if( null != con )
			{
				try 
				{
					con.close() ;
				} catch (SQLException e) 
				{					
					e.printStackTrace();
					LOG.error(ERR_CON_ONCLOSE) ;					
				}
			}
		}
	}
	
	
	public static void createTable() throws TBitsException 
	{
		Connection con = null ;
		try
		{
			con = DataSourcePool.getConnection() ;
			
			PreparedStatement pstmt = con.prepareStatement(CREATE_TABLE) ;
			pstmt.executeUpdate() ;
			LOG.info("The table = " + TABLE_NAME + " has been created .") ;
		}
		catch( SQLException e ) 
		{
			e.printStackTrace() ;
			LOG.error(ERR_DB_ACCESS) ;
			throw new TBitsException( ERR_DB_ACCESS, e ) ;
		}
		finally
		{
			if( null != con )
			{
				try 
				{
					con.close() ;
				} catch (SQLException e) 
				{					
					e.printStackTrace();
					LOG.error(ERR_CON_ONCLOSE) ;					
				}
			}
		}
	}
	
	public static void dropTable() throws TBitsException
	{
		Connection con = null ;
		try
		{
			con = DataSourcePool.getConnection() ;
			
			PreparedStatement pstmt = con.prepareStatement(DROP_TABLE) ;
			pstmt.executeUpdate() ;
			LOG.info("The table = " + TABLE_NAME + " has been delete.") ;
		}
		catch( SQLException e ) 
		{
			e.printStackTrace() ;
			LOG.error(ERR_DB_ACCESS) ;
			throw new TBitsException( ERR_DB_ACCESS, e ) ;
		}
		finally
		{
			if( null != con )
			{
				try 
				{
					con.close() ;
				} catch (SQLException e) 
				{					
					e.printStackTrace();
					LOG.error(ERR_CON_ONCLOSE ) ;					
				}
			}
		}
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
	 * @throws TBitsException , IllegalArgumentException
	 */
	public static Hashtable<String, String> getUserInfo( int user_id ) throws TBitsException
	{
		Connection con = null ;
		try
		{
			con = DataSourcePool.getConnection() ;
			
			PreparedStatement pstmt = con.prepareStatement(USER_INFO) ;
			pstmt.setInt(1, user_id ) ;
			
			Hashtable<String, String > info = new Hashtable<String,String>() ;
			
			ResultSet rs = pstmt.executeQuery() ;
			if( ! rs.next() )
			 {
				LOG.info("User does not exist int database table: " + TABLE_NAME) ;
				throw new IllegalArgumentException("User does not exist in database table: " + TABLE_NAME) ; 
			 }
			
			// otherwise extract data 
			String firm = rs.getString(FIRM) ;
			if( null == firm ) firm = "" ;
			String location = rs.getString(LOCATION) ;
			if(null == location ) location = "" ;
			String designation = rs.getString(DESIGNATION); 
			if( null == designation ) designation = "" ;
			String address = rs.getString(ADDRESS) ;
			if( null == address) address = "" ;
			String sex = rs.getString(SEX) ;
			if( null == sex ) sex = "" ;
			String full_firm_name = rs.getString(FULL_FIRM_NAME); 
			if( null == full_firm_name ) full_firm_name = "" ;
					
			info.put(FIRM, firm ) ;
			info.put(LOCATION, location ) ;
			info.put( DESIGNATION , designation ) ;
			info.put(ADDRESS , address ) ; 
			info.put(SEX, sex ) ;
			info.put(FULL_FIRM_NAME, full_firm_name) ;
			
			return info ;
			
		}
		catch( SQLException e )
		{
			e.printStackTrace() ;
			LOG.error(ERR_DB_ACCESS ) ;
			throw new TBitsException( ERR_DB_ACCESS, e ) ;
		}
		finally
		{
			if( null != con )
			{
				try 
				{
					con.close() ;
				} catch (SQLException e) 
				{					
					e.printStackTrace();
					LOG.error(ERR_CON_ONCLOSE ) ;					
				}
			}
		}
		
	}
	public static int updateAll( int user_id, String firm, String location, String designation, String address, String sex, String ffn ) throws TBitsException 
	{
		Connection con = null ;
		if( null == firm || null == location )
			throw new TBitsException("Firm and Location cannot be null.") ;
		
		if( null == designation )
			designation = "" ;
		if( null == address )
			address = "" ;
		if( null == sex )
			sex = "" ;
		if( null == ffn )
			ffn = "" ;
		try
		{
			con = DataSourcePool.getConnection() ;
			if( !userExists(con,user_id) )
			{
				LOG.info( "The user does not exists. Please add user first.") ;
				return 0 ;
			}
			else 
			{
				PreparedStatement pstmt = con.prepareStatement(UPDATE_ALL) ;
				pstmt.setInt(1, user_id ) ;
				pstmt.setString(2, firm ) ;
				pstmt.setString(3, location ) ;
				pstmt.setString(4, designation ) ;
				pstmt.setString(5, address ) ;
				pstmt.setString(6, sex ) ;
				pstmt.setString(7, ffn) ;
				int n = pstmt.executeUpdate() ;
				LOG.info("The user firm was successfully added.") ;
				return n ;
			}
		}
		catch( SQLException e )
		{
			e.printStackTrace() ;
			LOG.error(ERR_DB_ACCESS ) ;
			throw new TBitsException( ERR_DB_ACCESS, e ) ;
		}
		finally
		{
			if( null != con )
			{
				try 
				{
					con.close() ;
				} catch (SQLException e) 
				{					
					e.printStackTrace();
					LOG.error(ERR_CON_ONCLOSE ) ;					
				}
			}
		}
	}
	
	public static int deleteUser( int user_id ) throws TBitsException 
	{
		Connection con = null ;
		try
		{
			con = DataSourcePool.getConnection() ;
			if( !userExists(con,user_id) )
			{
				LOG.info( "The user does not exists.") ;
				return 0 ;
			}
			else 
			{
				PreparedStatement pstmt = con.prepareStatement(DELETE_USER) ;
				pstmt.setInt(1, user_id ) ;
				
				int n = pstmt.executeUpdate() ;
				LOG.info("The user was successfully deleted") ;
				return n ;
			}
		}
		catch( SQLException e )
		{
			e.printStackTrace() ;
			LOG.error(ERR_DB_ACCESS) ;
			throw new TBitsException( ERR_DB_ACCESS, e ) ;
		}
		finally
		{
			if( null != con )
			{
				try 
				{
					con.close() ;
				} catch (SQLException e) 
				{					
					e.printStackTrace();
					LOG.error(ERR_CON_ONCLOSE ) ;					
				}
			}
		}

	}
	
	/*
	public static int addUserFirmLocation( int user_id , String firm , String location ) throws TBitsException
	{
		Connection con = null ;
		try
		{
			con = DataSourcePool.getConnection() ;
			if( userExists(con,user_id) )
			{
				LOG.info( "The user already exists.") ;
				return 0 ;
			}
			else 
			{
				PreparedStatement pstmt = con.prepareStatement(ADD_USER_FIRM_LOCATION) ;
				pstmt.setInt(1, user_id ) ;
				pstmt.setString(2, firm ) ;
				pstmt.setString(3, location) ;
				int n = pstmt.executeUpdate() ;
				LOG.info("The user firm location was successfully added.") ;
				return n ;
			}
		}
		catch( SQLException e )
		{
			e.printStackTrace() ;
			LOG.error("SQLException while adding user+firm+location to user_info " ) ;
			throw new TBitsException( "SQLException while adding user+firm+location to user_info. ", e ) ;
		}
		finally
		{
			if( null != con )
			{
				try 
				{
					con.close() ;
				} catch (SQLException e) 
				{					
					e.printStackTrace();
					LOG.error("Exception while closing the connection object." ) ;					
				}
			}
		}
	}
	*/
	
	private static boolean userExists(Connection con, int userId) throws TBitsException 
	{
		try {
			if( null == con || con.isClosed() ) 
			{
				LOG.error(ERR_CON_NULL_CLOSED) ;
				throw new TBitsException(ERR_CON_NULL_CLOSED) ;
			}
		} catch (SQLException e) {			
			e.printStackTrace();
			LOG.error(ERR_CON_ACCESS) ;
			throw new TBitsException(ERR_CON_ACCESS, e ) ;
		}
		
		try
		{
			PreparedStatement ps = con.prepareStatement(USER_EXISTS) ;
			ps.setInt(1, userId) ;
			ResultSet rs = ps.executeQuery() ;
			
			if( rs.next() )
				return true ;
			else
				return false;
		}
		catch( SQLException e ) 
		{
			e.printStackTrace() ;
			LOG.error(ERR_DB_ACCESS) ;
			throw new TBitsException(ERR_DB_ACCESS,e) ;
		}
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
	/*
	public static void main( String argv[] )
	{
		try
		{
//			RandomAccessFile raf = new RandomAccessFile("/home/nitiraj/Shared_Folder/userinfo.csv","r") ;
//			raf.readLine() ;
//			String line = null ;
//			while( null != ( line = raf.readLine()) )
//			{
//				String[] parts = line.split(";") ;
////				int user_id = Integer.parseInt(parts[0]) ;
////				String firm = parts[1] ;
////				String location = parts[2] ;
////				// id;firm;location;designation;address;sex;ffn
////				String des = parts[3] ;
////				String add
////				addUserFirmLocation(Integer.parseInt(parts[0]), parts[1], parts[2]) ;
//				addComplete(Integer.parseInt(parts[0]), parts[1], parts[2], parts[3], parts[4], parts[5], parts[6]) ;
//			}
		//	addUserFirmLocation(user_id, firm, location) ;
		//	updateAll(user_id, firm, location, designataition, address, sex, ffn) ;
		ArrayList<User> au = User.lookupAll() ;
//		System.out.print( "AllUsers :" ) ;
		for( Iterator<User> iter = au.iterator() ; iter.hasNext() ; )
		{
			User u = iter.next();
			if( !u.getUserLogin().equalsIgnoreCase("root"))
			{
				System.out.println( u.getUserLogin() + ":") ;
				Hashtable<String,String> map = getUserInfo(u.getUserId()) ;
				System.out.println(map);
			}			
		}
		}
		catch (DatabaseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (TBitsException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	*/
											
}
