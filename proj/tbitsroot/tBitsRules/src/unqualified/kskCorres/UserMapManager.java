package kskCorres;


import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;

import transbit.tbits.common.DataSourcePool;
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
 * This is a helper class for maintaining the user_map as required by the
 * KSK correspondence project. All actions to the database table ksk_user_map
 * should be done via this class.
 */
public class UserMapManager 
{
	private static final String ERR_DB_ACCESS = "Exception while accessing the database table the database";
	private static final String ERR_CON_ONCLOSE = "Exception while closing the connection object.";
	private static final String ERR_CON_CREATE = "SQLException while creating the connection.";
	private static final String ERR_CON_ACCESS = "SQLException while accessing the connection object";
	private static final String ERR_CON_NULL_CLOSED = "The supplied connection object was null or was closed.";
	public static final String PKG_KSK = "transbit.tbits.KSK";
	/**
	 * logger for this class
	 */
	public static final TBitsLogger LOG = TBitsLogger.getLogger(PKG_KSK);
	
	/**
	 * @param TABLE_NAME the name of the table
	 * Table Structure 
	 * user_Id | subject of correspondence | recepient type | recepient id 
	 */	
	public static final String TABLE_NAME = "ksk_user_map" ;
	
	/**
	 * column name for user_id of the concerned user
	 */
	public static final String USER_ID = "user_id" ; // int not null
	/**
	 * column name for subject of the correspondence it is registered for 
	 */
	public static final String SUB_CORRES = "sub_corres" ; // varchar(50) not null
	/**
	 * column name for type of the other user : value = one of OUR_CC, TO, YOUR_CC, value of type is int. and it should never be 0 
	 */
	public static final String RECEPIENT_TYPE = "recepient_type" ; // int null
	
	/**
	 * column name for user_id of the other user
	 */
	public static final String  RECEPIENT_ID = "recepient_id" ;
	
	/**
	 * types of users
	 */
	public static final int TO = 1 ;
	public static final int OUR_CC = 2 ;
	public static final int YOUR_CC = 3 ;
		
	public static final int[] ALL_RECEPIENT_TYPES = { TO, OUR_CC, YOUR_CC } ;
	/** 
	 * TODO: subjects of correspondence
	 */
	public static final String CM = "CM" ;
	 
	/**
	 * sql statements for operations
	 */
	
	//TODO: for all of the functions in this class check whether the 
	// supplied user_id is regestered with TBITS or not
	public static final String CREATE_TABLE = "CREATE TABLE " + TABLE_NAME + 
											  " ( " +
													  USER_ID + " INT NOT NULL, " +
													  SUB_CORRES + " VARCHAR(50) NOT NULL, " + 
													  RECEPIENT_TYPE + " INT NULL, " + 
													  RECEPIENT_ID + " INT NULL, " +													  													  
											   " ) " ;
	
	public static final String DROP_TABLE = "DROP TABLE " + TABLE_NAME ;
	
	public static final String ADD_USER_AND_CORRES_TYPE = "INSERT INTO " + TABLE_NAME +
														  " VALUES (?,?,NULL,NULL) " ;
	
	public static final String ADD_RECEPIENT = "INSERT INTO " + TABLE_NAME +
										" VALUES (?,?,?,?) " ;
		
	public static final String GET_RECEPIENT = "SELECT " + RECEPIENT_ID +
										" FROM " + TABLE_NAME + 
										" WHERE " + USER_ID + "=? " +
										" AND " + SUB_CORRES + "=? " + 
										" AND " + RECEPIENT_TYPE + "=? " ; 
										
	public static final String USER_CORRES_EXISTS = "SELECT * " + 
													" FROM " + TABLE_NAME +
													" WHERE " + USER_ID + "=? " +
													" AND " + SUB_CORRES + "=? " + 
													" AND " + RECEPIENT_TYPE + " IS NULL " +
													" AND " + RECEPIENT_ID + " IS NULL " ;													
	
	public static final String RECEPIENT_EXISTS = "SELECT * " + 
													" FROM " + TABLE_NAME +
													" WHERE " + USER_ID + "=? " +
													" AND " + SUB_CORRES + "=? " + 
													" AND " + RECEPIENT_TYPE + "=? " +
													" AND " + RECEPIENT_ID + "=? " ;
	
	public static final String DELETE_RECEPIENT = "DELETE FROM " + TABLE_NAME +
												  " WHERE " + USER_ID + "=? " +
												  " AND " + SUB_CORRES + "=? " +
												  " AND " + RECEPIENT_TYPE + "=? " +
												  " AND " + RECEPIENT_ID + "=? " ;
	
	public static final String DELETE_USER_CORRES = "DELETE FROM " + TABLE_NAME +
													" WHERE " + USER_ID + "=? " +
													" AND " + SUB_CORRES + "=? ";
	
	public static final String DELETE_USER = "DELETE FROM " + TABLE_NAME +
											 " WHERE " + USER_ID + "=? " ;
	
	public static final String USER_MAP = "SELECT * FROM " + TABLE_NAME +
										  " WHERE " + USER_ID + "=? " +
										  " AND " + SUB_CORRES + "=? " +
										  " AND " + RECEPIENT_TYPE + " IS NOT NULL ";	
	
	public static final String GET_RECEPIENTS = "SELECT " + RECEPIENT_ID + " FROM " + TABLE_NAME +
													  " WHERE " + USER_ID + "=? " +
													  " AND " + SUB_CORRES + "=? " +
													  " AND " + RECEPIENT_TYPE + "=? ";
	public static String ALL_CORRES = "SELECT DISTINCT " + SUB_CORRES + " FROM " + TABLE_NAME + " WHERE " + USER_ID + "=?" ;
	
//	public static String UNIQUE_USERS = "SELECT DISTINCT " + TABLE_NAME + "." + USER_ID + ", user_login " +
//										"	FROM " + TABLE_NAME + " join users on users.user_id=" + TABLE_NAME + "."  + USER_ID ;  

	public static String GET_UNIQUE_FIRM_USERS = " SELECT DISTINCT " + TABLE_NAME + "." + USER_ID + 
											 " FROM " + TABLE_NAME + " join " + UserInfoManager.TABLE_NAME +  
											 " on " + TABLE_NAME + "." + USER_ID + "=" + UserInfoManager.TABLE_NAME + "." + UserInfoManager.USER_ID + 
											 " where " + UserInfoManager.TABLE_NAME + "." + UserInfoManager.FIRM + "=?";
	
	// hashtable< user_login, hashtable<sub_corres,Hashtable<TO/OURCC/YOURCC, userLogins>>>
	public static Hashtable<String,Hashtable<String, Hashtable<Integer,ArrayList<String>>>> getCompleteTableAsFirm( String firm ) throws TBitsException
	{
		// steps
		// 2. get all unique users in user_map		
		// 3. make the complete table accordingly 
		Hashtable<Integer,String> uil = new Hashtable<Integer,String>() ; // user_id / loging mapping 
		String au = "select u.user_login 'user_login',k."+SUB_CORRES+",k." + RECEPIENT_TYPE +",r.user_login 'recepient_login' from " + TABLE_NAME + " k " +
					"join users u on k." + USER_ID + "=u.user_id and u.is_active = 1 and u.firm_code=" + "'"+firm+"' " + 
					"join users r on k." + RECEPIENT_ID +"=r.user_id and r.is_active =1 " ;
		Connection con = null ;
		Hashtable<String, Hashtable<String, Hashtable<Integer,ArrayList<String>>>> ct = new Hashtable<String, Hashtable<String, Hashtable<Integer,ArrayList<String>>>>() ;
		try
		{
			con = DataSourcePool.getConnection() ;
			PreparedStatement ps = con.prepareStatement(au) ;
			ResultSet rs = ps.executeQuery() ;
					 
			while( rs.next() )
			{
				// get first user and fill the table for this user
				String log = rs.getString("user_login");
				String subcorres = rs.getString(SUB_CORRES);
				int type = rs.getInt(RECEPIENT_TYPE);
				String recepient_login = rs.getString("recepient_login");
				Hashtable<String, Hashtable<Integer,ArrayList<String>>> corrMap = ct.get(log);
				if( null == corrMap )
					corrMap = new Hashtable<String, Hashtable<Integer,ArrayList<String>>>() ;
				
				Hashtable<Integer,ArrayList<String>> recMap = corrMap.get(subcorres);
				if( null == recMap )
					recMap = new Hashtable<Integer,ArrayList<String>>() ;
				
				ArrayList<String> rec = recMap.get(type);
				if( null == rec )
					rec = new ArrayList<String>() ;
				
				rec.add(recepient_login);
				recMap.put(type, rec);
				corrMap.put(subcorres, recMap);
				ct.put(log, corrMap);			
			}
		}
		catch( Exception e ) 
		{
			e.printStackTrace() ;
			LOG.error( "Exception while creating complete table information." ) ;
			throw new TBitsException( "Exception while creating complete table information.", e ) ;
		}
		finally 
		{
			if( null != con )
				try {
					con.close() ;
				} catch (SQLException e) {
					LOG.error(ERR_CON_ONCLOSE) ;
					e.printStackTrace();
				}
		}
		return ct ;
	}
	// done 
	public static Hashtable<String,Hashtable<Integer,ArrayList<Integer> > > 
					getCompleteMapping( Connection con, int user_id) throws TBitsException 
	{
		try {
			if( null == con || con.isClosed() )
			{
			   	LOG.error(ERR_CON_NULL_CLOSED) ;
				throw new TBitsException(ERR_CON_NULL_CLOSED) ;
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			LOG.error("Cannot access the connection object.") ;
			throw new TBitsException("Cannot access the connection object.",e) ;
		}
			
		Hashtable<String,Hashtable<Integer,ArrayList<Integer> > > res = new Hashtable<String,Hashtable<Integer,ArrayList<Integer> > >() ;			
		ArrayList<String> allcorres = getCorres(con, user_id) ;
		
		for( Iterator<String> iter = allcorres.iterator() ; iter.hasNext() ; ) 
		{
			String corres = iter.next() ;
			Hashtable<Integer, ArrayList<Integer>> table = getMapping(con, user_id, corres) ;
			
			res.put(corres, table ) ;				
		}
		
		return res ;		
	}
	
	// done  
	public static Hashtable<String,Hashtable<Integer,ArrayList<String>>> 
	getCompleteMappingAsUserLogin( Connection con, int user_id) throws TBitsException  
	{			
	    try {
			if( null == con || con.isClosed() )
			{
			   	LOG.error(ERR_CON_NULL_CLOSED) ;
				throw new TBitsException(ERR_CON_NULL_CLOSED) ;
			}
		} catch (SQLException e) {			
			e.printStackTrace();
			LOG.error("Cannot access the connection object.") ;
			throw new TBitsException("Cannot access the connection object.",e) ;
		}
		Hashtable<String,Hashtable<Integer,ArrayList<String> > > res = new Hashtable<String,Hashtable<Integer,ArrayList<String> > >() ;			
		ArrayList<String> allcorres = getCorres(con, user_id) ;
		
		for( Iterator<String> iter = allcorres.iterator() ; iter.hasNext() ; ) 
		{
			String corres = iter.next() ;
			Hashtable<Integer, ArrayList<String>> table = getMappingAsUserLogin(con, user_id, corres) ;
			
			res.put(corres, table ) ;				
		}
		
		return res ;		
	}

	// done 
	public static Hashtable<String,Hashtable<Integer,ArrayList<Integer> > > 
	getCompleteMapping( int user_id) throws TBitsException 
	{
		Connection con = null ;
		try
		{
			con = DataSourcePool.getConnection() ;
			return getCompleteMapping( con , user_id ) ;
		}
		catch( SQLException e)
		{
			e.printStackTrace() ;
			LOG.error("SQLException while creating Connection object.") ;
			throw new TBitsException( "SQLException while creating Connection object.", e) ;
		}
		finally
		{
			if( null != con ) 
				try {
					con.close();
				} catch (SQLException e) {
					e.printStackTrace();
					LOG.error("SQLException while closing the connection.") ;
				}
		}
	}
	
	// done 
	public static Hashtable<String,Hashtable<Integer,ArrayList<String> > > 
	getCompleteMappingAsUserLogin( int user_id) throws TBitsException 
	{
		Connection con = null ;
		try
		{
			con = DataSourcePool.getConnection() ;
			return getCompleteMappingAsUserLogin( con , user_id ) ;
		}
		catch( SQLException e)
		{
			e.printStackTrace() ;
			LOG.error("SQLException while creating Connection object.") ;
			throw new TBitsException( "SQLException while creating Connection object.", e) ;
		}
		finally
		{
			if( null != con )
				try {
					con.close();
				} catch (SQLException e) {
					e.printStackTrace();
					LOG.error("Exception while closing the connection.") ;
				}
		}
	}
	
	/**
	 * 
	 * @param user_id
	 * @return 0 if no such user exists in the user_map. else return the number of rows affected.
	 * @throws TBitsException
	 */
	// done 
	public static int deleteUser( int user_id ) throws TBitsException
	{
		Connection con = null ;
		try
		{
			con = DataSourcePool.getConnection() ;
			
			PreparedStatement pstmt = con.prepareStatement(DELETE_USER) ;
			pstmt.setInt(1, user_id ) ;		
			int n = pstmt.executeUpdate() ;
			if( 0 == n)
				LOG.info("Such user does not exists.") ;
			else
				LOG.info("The user was successfully deleted.") ;
			return n ;
			
		}
		catch( SQLException e )
		{
			e.printStackTrace() ;
			LOG.error("SQLException while deleting user from user_map " ) ;
			throw new TBitsException( "SQLException while deleting user from user_map ", e ) ;
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
	 * @param sub_corres
	 * @return 0 if the user+corres does not exists. else return the number of row affected.
	 * @throws TBitsException 
	 */
	// done 
	public static int deleteUserCorres( int user_id, String sub_corres) throws TBitsException
	{
		Connection con = null ;
		try
		{
			con = DataSourcePool.getConnection() ;
			
			PreparedStatement pstmt = con.prepareStatement(DELETE_USER_CORRES) ;
			pstmt.setInt(1, user_id ) ;
			pstmt.setString(2, sub_corres ) ;
			int n = pstmt.executeUpdate() ;
			if( 0 == n)
				LOG.info("Such user+corres does not exists.") ;
			else
				LOG.info("The user+corres was successfully deleted.") ;
			return n ;
			
		}
		catch( SQLException e )
		{
			e.printStackTrace() ;
			LOG.error("SQLException while deleting user+corres from user_map " ) ;
			throw new TBitsException( "SQLException while deleting user+corres from user_map ", e ) ;
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
	
	// done 
	public static ArrayList<String> getCorres( Connection con, int user_id ) throws TBitsException
	{
		try {
			if( null == con || con.isClosed() )
			{
				LOG.error(ERR_CON_NULL_CLOSED) ;
				throw new TBitsException( ERR_CON_NULL_CLOSED ) ;
			}
		} catch (SQLException e1) {
			LOG.error(ERR_CON_ACCESS ) ;
			e1.printStackTrace();
			throw new TBitsException(ERR_CON_ACCESS, e1 ) ;
		}
		try
		{				
			PreparedStatement pstmt = con.prepareStatement(ALL_CORRES) ;
			pstmt.setInt(1, user_id ) ;		
			ResultSet rs = pstmt.executeQuery() ;
			
			ArrayList<String> corres = new ArrayList<String>() ;
			while( rs.next() )
			{
				String str = rs.getString(SUB_CORRES) ;
				corres.add(str) ;
			}
			
			return corres ;
		}
		catch( SQLException e )
		{
			e.printStackTrace() ;
			LOG.error("SQLException while retrieving user's " + SUB_CORRES ) ;
			throw new TBitsException("Exception while retrieving user's " + SUB_CORRES, e ) ;
		}
		
	}

	// done 
	public static ArrayList<String> getCorres( int user_id ) throws TBitsException 
	{
		Connection con = null ;
		try
		{
			con = DataSourcePool.getConnection() ;		
			return getCorres( con , user_id )  ;
		} catch (SQLException e) 
		{		
			e.printStackTrace();
			LOG.error(ERR_CON_CREATE) ;
			throw new TBitsException( ERR_CON_CREATE, e ) ;
		}	
		finally
		{
			if( null != con )
				try {
					con.close() ;
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					LOG.error(ERR_CON_ONCLOSE ) ;
				}
		}
		
	}
	/**
	 * 
	 * @param user_id
	 * @param sub_corres
	 * @param recepient_type
	 * @param recepient_id
	 * @return 0 if the recepient was present in the user_map else the number of row affected
	 * @throws TBitsException 
	 */
	// done 
	public static int deleteRecepient( int user_id, String sub_corres, int recepient_type, int recepient_id ) throws TBitsException
	{
		Connection con = null ;
		try
		{
			con = DataSourcePool.getConnection() ;
			
			PreparedStatement pstmt = con.prepareStatement(DELETE_RECEPIENT) ;
			pstmt.setInt(1, user_id ) ;
			pstmt.setString(2, sub_corres ) ;
			pstmt.setInt(3, recepient_type );
			pstmt.setInt(4, recepient_id ) ;
			int n = pstmt.executeUpdate() ;
			if( 0 == n)
				LOG.info("Such recepient does not exists.") ;
			else
				LOG.info("The recepient was successfully deleted.") ;
			return n ;
			
		}
		catch( SQLException e )
		{
			e.printStackTrace() ;
			LOG.error("SQLException while deleting recepient from user_map " ) ;
			throw new TBitsException( "SQLException while deleting recepient from user_map ", e ) ;
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
	 * @param con
	 * @param user_id
	 * @param sub_corres
	 * @return true if the user with userID=user_id is registered with subject of correspondence=sub_corres in the user map, else return false
	 * @throws SQLException
	 * @throws TBitsException 
	 */
	// done
	public static boolean userCorresExists( Connection con, int user_id, String sub_corres ) throws TBitsException
	{		
		try {
			if( null == con || con.isClosed() )
			{
				LOG.error(ERR_CON_NULL_CLOSED) ;
				throw new TBitsException(ERR_CON_NULL_CLOSED) ;
			}
		} catch (SQLException e) {
			LOG.error(ERR_CON_ACCESS) ;
			e.printStackTrace();
			throw new TBitsException(ERR_CON_ACCESS,e) ;
		}
		
		try
		{
			PreparedStatement pstmt = con.prepareStatement(USER_CORRES_EXISTS) ;
			pstmt.setInt(1, user_id ) ;
			pstmt.setString(2, sub_corres ) ;
			ResultSet rs = pstmt.executeQuery() ;
			 
			if( rs.next() )
			{
				return true ;
			}
			else return false ;
		}
		catch( SQLException e )
		{
			LOG.error(ERR_DB_ACCESS ) ;
			throw new TBitsException( ERR_DB_ACCESS, e ) ;
		}
	}

	/**
	 * 
	 * @param con
	 * @param user_id
	 * @param sub_corres
	 * @param recepient_type
	 * @param recepient_id
	 * @return returns true if the other user with user_id=recepient_id is mapped to the type=recepient_type for user=user_id registered for correspondence type=corres_type
	 * @throws SQLException
	 */
	// done 
	public static boolean recepientExists( Connection con, int user_id, String sub_corres, int recepient_type, int recepient_id ) throws TBitsException
	{
		try {
			if( null == con || con.isClosed() )
			{
				LOG.error(ERR_CON_NULL_CLOSED) ;
				throw new TBitsException(ERR_CON_NULL_CLOSED) ;
			}
		} catch (SQLException e) {
			LOG.error(ERR_CON_ACCESS) ;
			e.printStackTrace();
			throw new TBitsException(ERR_CON_ACCESS,e) ;
		}
		
		try
		{
			PreparedStatement pstmt = con.prepareStatement(RECEPIENT_EXISTS) ;
			pstmt.setInt(1, user_id ) ;
			pstmt.setString(2, sub_corres ) ;
			pstmt.setInt(3, recepient_type ) ;
			pstmt.setInt(4, recepient_id ) ;
			ResultSet rs = pstmt.executeQuery() ;
			 
			if( rs.next() )
			{
				return true ;
			}
			else return false ;
		}
		catch(SQLException e ) 
		{
			LOG.error(ERR_DB_ACCESS) ;
			throw new TBitsException( ERR_DB_ACCESS, e ) ;
		}
	}
	
	// done 
	public static ArrayList<Integer> getTos( int user_id, String sub_corres ) throws TBitsException 
	{
		return getRecepients( user_id, sub_corres, TO ) ;
	}
	
	// done 
	public static ArrayList<Integer> getYourCCs( int user_id, String sub_corres ) throws TBitsException 
	{
		return getRecepients( user_id, sub_corres, YOUR_CC ) ;
	}
	
	// done 
	public static ArrayList<Integer> getOurCCs( int user_id, String sub_corres ) throws TBitsException 
	{
		return getRecepients( user_id, sub_corres, OUR_CC ) ;
	}

	// done
	public static ArrayList<Integer> getRecepients( Connection con , int user_id, String sub_corres, int recepient_type ) throws TBitsException
	{
		try {
			if( null == con || con.isClosed() )
			{
				LOG.error(ERR_CON_NULL_CLOSED) ;
				throw new TBitsException(ERR_CON_NULL_CLOSED) ;
			}
		} catch (SQLException e) {
			LOG.error(ERR_CON_ACCESS) ;
			e.printStackTrace();
			throw new TBitsException(ERR_CON_ACCESS,e) ;
		}
		
		try
		{
			PreparedStatement pstmt = con.prepareStatement(GET_RECEPIENTS) ;
			pstmt.setInt(1, user_id ) ;
			pstmt.setString(2, sub_corres ) ;
			pstmt.setInt(3, recepient_type ) ;
			
			ResultSet rs = pstmt.executeQuery() ;
				
			ArrayList<Integer> recepients = new ArrayList<Integer>() ;
			while ( rs.next() ) 
					recepients.add(rs.getInt(RECEPIENT_ID)) ;			
			
			return recepients ;
		}
		catch( SQLException e )
		{
			e.printStackTrace() ;
			LOG.error(ERR_DB_ACCESS ) ;
			throw new TBitsException( ERR_DB_ACCESS, e ) ;
		}

	}
	/**
	 * 
	 * @param user_id
	 * @param sub_corres
	 * @param recepient_type
	 * @return null if the there is no recepient for recepient_type, else return ArrayList containing the recepient_ids corresponding to the query
	 * @throws TBitsException
	 */
	// done 
	public static ArrayList<Integer> getRecepients( int user_id, String sub_corres, int recepient_type ) throws TBitsException
	{
		Connection con = null ;
		try
		{					
			con = DataSourcePool.getConnection() ;
			return getRecepients( con, user_id , sub_corres, recepient_type ) ;
		} catch (SQLException e) {
			e.printStackTrace();
			LOG.error(ERR_CON_CREATE) ;
			throw new TBitsException( ERR_CON_CREATE, e ) ;
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
	
	// done
	public static Hashtable<Integer, ArrayList<Integer>> getMapping( Connection con, int user_id, String sub_corres ) throws TBitsException
	{			
		try
		{
			if( null == con || con.isClosed() )
			{
				LOG.error(ERR_CON_NULL_CLOSED ) ;
				throw new TBitsException(ERR_CON_NULL_CLOSED) ;
			}
			
			PreparedStatement pstmt = con.prepareStatement(USER_MAP) ;
			pstmt.setInt(1, user_id ) ;
			pstmt.setString(2, sub_corres ) ;
			ResultSet rs = pstmt.executeQuery() ;
				
			Hashtable<Integer,ArrayList<Integer>> recepientTable = new Hashtable<Integer,ArrayList<Integer>>() ;
			
			// add empty arrays for each type of recepient
			for( int i = 0 ; i < ALL_RECEPIENT_TYPES.length ; i++ )
			{
				recepientTable.put(new Integer(ALL_RECEPIENT_TYPES[i]), new ArrayList<Integer>() ) ;
			}
			
			while( rs.next() ) 
			{
				int rec_type = rs.getInt(RECEPIENT_TYPE) ;					
				recepientTable.get(rec_type).add(rs.getInt(RECEPIENT_ID)) ;				
			}
						
			return recepientTable ;
			
		}
		catch( SQLException e )
		{
			e.printStackTrace() ;
			LOG.error(ERR_DB_ACCESS ) ;
			throw new TBitsException( ERR_DB_ACCESS, e ) ;
		}		
	}
	
	// TODO : NOT efficient : use join instead
	// done
	public static Hashtable<Integer, ArrayList<String>> getMappingAsUserLogin( Connection con, int user_id, String sub_corres ) throws TBitsException
	{
		 try {
				if( null == con || con.isClosed() )
				{
				   	LOG.error(ERR_CON_NULL_CLOSED) ;
					throw new TBitsException(ERR_CON_NULL_CLOSED) ;
				}
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				LOG.error(ERR_CON_ACCESS) ;
				throw new TBitsException(ERR_CON_ACCESS,e) ;
			}
		try
		{
			PreparedStatement pstmt = con.prepareStatement(USER_MAP) ;
			pstmt.setInt(1, user_id ) ;
			pstmt.setString(2, sub_corres ) ;
			ResultSet rs = pstmt.executeQuery() ;
				
			Hashtable<Integer,ArrayList<String>> recepientTable = new Hashtable<Integer,ArrayList<String>>() ;
			
			for( int i = 0 ; i < ALL_RECEPIENT_TYPES.length ; i++ )
				recepientTable.put(ALL_RECEPIENT_TYPES[i], new ArrayList<String>() ) ;
			
			while( rs.next() ) 
			{
				int rec_type = rs.getInt(RECEPIENT_TYPE) ;
				User user = User.lookupAllByUserId(rs.getInt(RECEPIENT_ID)) ;
				if( null != user ) 
				{
					String userLogin = user.getUserLogin() ;
					recepientTable.get(rec_type).add(userLogin) ;
				}
				else
					LOG.error("The user with userID = " + rs.getInt(RECEPIENT_ID) + " does not exist in database.") ;
			}
						
			return recepientTable ;
			
		}
		catch( SQLException e )
		{
			e.printStackTrace() ;
			LOG.error(ERR_DB_ACCESS ) ;
			throw new TBitsException( ERR_DB_ACCESS, e ) ;
		} catch (DatabaseException e) {
			e.printStackTrace();
			LOG.error("DatabaseException while retriving user info into users" ) ;
			throw new TBitsException( "DatabaseException while retriving user info into users. ", e ) ;
		}		
	}
	/** 
	 * @param user_id
	 * @param sub_corres
	 * @return 
	 * else returns a hashtable containing the key value pair, where key = recepient_type, value= ArrayList of all the recepients in it
	 * if there is no recepient for a type then their is no key-value pair corresponding to that type. Hence accessing that type from 
	 * the hashtable using get() will return NULL. 
	 * @throws TBitsException 
 	 */
	// done 
	public static Hashtable<Integer, ArrayList<Integer>> getMapping( int user_id, String sub_corres ) throws TBitsException 
	{
		// get all rows from the table 
		// if the recepient_type is not null then add the recepient id in array of proper key in the hashtable
		Connection con = null ;
		try
		{
			con = DataSourcePool.getConnection() ;
			return getMapping( con, user_id, sub_corres ) ;
		}
		catch( SQLException e )
		{
			e.printStackTrace() ;
			LOG.error(ERR_CON_CREATE ) ;
			throw new TBitsException( ERR_CON_CREATE, e ) ;
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
	
	// done 
	public static Hashtable<Integer, ArrayList<String>> getMappingAsUserLogin( int user_id, String sub_corres ) throws TBitsException 
	{
		// get all rows from the table 
		// if the recepient_type is not null then add the recepient id in array of proper key in the hashtable
		Connection con = null ;
		try
		{
			con = DataSourcePool.getConnection() ;
			return getMappingAsUserLogin( con, user_id, sub_corres ) ;
		}
		catch( SQLException e )
		{
			e.printStackTrace() ;
			LOG.error(ERR_CON_CREATE ) ;
			throw new TBitsException( ERR_CON_CREATE, e ) ;
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
	 * adds user and correspondence type to the user_map 
	 * @param user_id
	 * @param sub_corres
	 * @return 0 if the user + sub_corres already exists in the user_map. else return the number of row affected due to the action
	 * @throws TBitsException
	 */
	// done 
	public static int addUserCorres( int user_id , String sub_corres ) throws TBitsException
	{
		Connection con = null ;
		try
		{
			con = DataSourcePool.getConnection() ;
			if( userCorresExists(con,user_id,sub_corres) )
			{
				LOG.info( "The user and corres already exists.") ;
				return 0 ;
			}
			else 
			{
				PreparedStatement pstmt = con.prepareStatement(ADD_USER_AND_CORRES_TYPE) ;
				pstmt.setInt(1, user_id ) ;
				pstmt.setString(2, sub_corres ) ;
				int n = pstmt.executeUpdate() ;
				LOG.info("The user corres was successfully added.") ;
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
	
	
	public static int addRecepient( int user_id, String sub_corres, int recepient_type, int recepient_id ) throws TBitsException
	{
		Connection con = null ;		
		try 
		{
			con = DataSourcePool.getConnection() ;
			if( userCorresExists(con, user_id, sub_corres) )
			{
				if( !recepientExists(con, user_id,sub_corres,recepient_type, recepient_id) )
				{
					PreparedStatement pstmt = con.prepareStatement(ADD_RECEPIENT) ;
					pstmt.setInt(1, user_id ) ;
					pstmt.setString(2, sub_corres ) ;
					pstmt.setInt(3, recepient_type) ;
					pstmt.setInt(4, recepient_id) ;
					LOG.info("The recepient was successfully added.") ;
					return pstmt.executeUpdate() ;					
				}
				else
				{
					LOG.info("The recepient user already exists." );
					return 0 ;
				}
			}
			else
			{
				LOG.info("The user with user_id=" + user_id + " and sub_corres=" + sub_corres + " does not exist. Please create it first.") ;
				return 0 ;
			}
		} catch (SQLException e) {		
			e.printStackTrace();
			LOG.error(ERR_DB_ACCESS ) ;
			throw new TBitsException( ERR_DB_ACCESS, e ) ;
		}
		finally
		{
			if( null != con )
				try {
					con.close() ;
				} catch (SQLException e) {
					e.printStackTrace();
					LOG.error(ERR_CON_ONCLOSE ) ;
				}
		}
	}
	
	
	
	public static void main( String argv[] )
	{
		try {
//			System.out.println("Entered main.");
			Gson gson = new Gson() ;
			String mapJson = gson.toJson(getCompleteTableAsFirm("WPCL")) ;
			System.out.println( "wpcl map:\n" + mapJson ) ;
		} catch (TBitsException e) {
			
			e.printStackTrace();
		}
	}
}

