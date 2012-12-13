package corrGeneric.com.tbitsGlobal.server.managers;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import transbit.tbits.common.DataSourcePool;
//import transbit.tbits.exception.CorrException;
import corrGeneric.com.tbitsGlobal.server.cache.UserMapCache;
import corrGeneric.com.tbitsGlobal.server.util.Utility;
import corrGeneric.com.tbitsGlobal.shared.domain.PropertyEntry;
import corrGeneric.com.tbitsGlobal.shared.domain.ReportEntry;
import corrGeneric.com.tbitsGlobal.shared.domain.UserMapEntry;
import corrGeneric.com.tbitsGlobal.shared.key.UserMapKey;
import corrGeneric.com.tbitsGlobal.shared.objects.CorrException;
import static corrGeneric.com.tbitsGlobal.server.util.Utility.LOG;
import static corrGeneric.com.tbitsGlobal.shared.domain.UserMapEntry.*;
import static corrGeneric.com.tbitsGlobal.shared.objects.GenericParams.*;

public class UserMapManager extends AbstractManager
{
	/**
	 * @return the userMapCache
	 */
	public UserMapCache getUserMapCache() {
		return userMapCache;
	}

	private UserMapCache userMapCache;
	private static UserMapManager instance = null;
	
	private UserMapManager() throws CorrException
	{
		initialize();

		ManagerRegistry.getInstance().registerManager(UserMapManager.class, this);
	}
	
	public static ArrayList<UserMapEntry> lookupUserMap( String sysPrefix, String userLogin) throws CorrException
	{
		return UserMapManager.getInstance().getUserMapCache().get(new UserMapKey(sysPrefix, userLogin));
	}
	
	public synchronized static UserMapManager getInstance() throws CorrException
	{
		if( null == instance )
			instance = new UserMapManager();
		
		return instance ;
	}
	
	public static final String GetUserMap = " select * from " + TableName +
											" where " + SysPrefix + "=? and " + UserLogin + "=? ";
	
	public static ArrayList<UserMapEntry> getUserMapFromDB(UserMapKey t) throws CorrException 
	{
		Connection con = null;
		try
		{
			con = DataSourcePool.getConnection();
			if( null != con )
			{
				return getUserMapFromDB(con,t);
			}
			else
			{
				throw new CorrException(FAILED_CON);
			}
		} catch (SQLException e) 
		{
			Utility.LOG.warn(FAILED_TO_RETRIEVE + " user map for " + t);
			throw new CorrException(FAILED_TO_RETRIEVE + " user map for " + t);
		}
		finally
		{
			try {
				if( null != con && con.isClosed() == false)
					con.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}		
	}

	public static ArrayList<UserMapEntry> getUserMapFromDB(Connection con,
			UserMapKey t) throws SQLException 
	{
		ArrayList<UserMapEntry> map = new ArrayList<UserMapEntry>();
		
		PreparedStatement ps = con.prepareStatement(GetUserMap);
		ps.setString(1, t.getSysPrefix());
		ps.setString(2, t.getUserLogin());
		
		ResultSet rs = ps.executeQuery();
		if( null != rs )
		{
			while( rs.next() )
			{
				UserMapEntry um = corrGeneric.com.tbitsGlobal.server.util.Utility.createUserMapEntryFromResultSet(rs);
				if( null != um )
					map.add(um);
			}
		}
		
		if( map.size() == 0 )
			return null;
		
		return map;
	}

	@Override
	public void refresh() throws CorrException {
		initialize();
	}
	
	public int persistEntry(UserMapEntry entry) throws CorrException {
		if (null == entry)
			throw new CorrException("UserMapEntry was null.");

		if (entry.getId() == -1) {
			return insertEntry(entry);
		} else {
			return updateEntry(entry);
		}
	}
	
	public int insertEntry( UserMapEntry entry ) throws CorrException
	{
		Connection con = null;
		try
		{
			con = DataSourcePool.getConnection();
			return insertEntry(con,entry);
		} catch (SQLException e) {
			LOG.error(e);
			throw new CorrException(e);
		}
		finally
		{
			try {
				if( null != con && con.isClosed() == false)
				{
					con.close();
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}
	
	private int insertEntry(Connection con, UserMapEntry entry) throws CorrException 
	{
		try
		{
			if( null == entry )
				throw new CorrException("Provided UserMapEntry was null.");
	
			if( null == con || con.isClosed() == true )
				throw new CorrException("Connection was either null or closed.");
		
			String sql = "insert into " + TableName + " values (?,?,?,?,?,?,?,?)" ;

			PreparedStatement ps = con.prepareStatement(sql);
			ps.setString(1, entry.getSysPrefix());
			ps.setString(2, entry.getUserLogin());
			ps.setString(3, entry.getType1());
			ps.setString(4, entry.getType2());
			ps.setString(5, entry.getType3());
			ps.setString(6, entry.getUserTypeFieldName());
			ps.setString(7, entry.getUserLoginValue());
			ps.setInt(8, entry.getStrictNess());
			
			int count = ps.executeUpdate();
			refresh();
			return count ;
		}
		catch(Exception e)
		{
			Utility.LOG.error(e);
			throw new CorrException(e);
		}
	}

	public int updateEntry( UserMapEntry entry ) throws CorrException
	{
		Connection con = null;
		try
		{
			con = DataSourcePool.getConnection();
			return updateEntry(con,entry);
		}
		catch( Exception e)
		{
			Utility.LOG.error(e);
			throw new CorrException(e);
		}
		finally
		{
			try {
				if( null != con && con.isClosed() == false)
				{
					con.close();
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}
	
	private int updateEntry(Connection con, UserMapEntry entry) throws CorrException 
	{
		try {
			if( null == entry )
				throw new CorrException("Provided UserMapEntry was null.");

			if( null == con || con.isClosed() == true )
				throw new CorrException("Connection was either null or closed.");
		
			String sql = "update " + TableName + " set " + SysPrefix + " = ?, " + UserLogin + " = ?," + Type1 + " = ?," + Type2 + " = ?," + Type3 + " = ?, " + UserTypeFieldName + " = ?, " + UserLoginValue + " = ?, " + StrictNess + " = ? " + " where " +  Id + "= ?";
		
			PreparedStatement ps = con.prepareStatement(sql);
			ps.setString(1, entry.getSysPrefix());
			ps.setString(2, entry.getUserLogin());
			ps.setString(3, entry.getType1());
			ps.setString(4, entry.getType2());
			ps.setString(5, entry.getType3());
			ps.setString(6, entry.getUserTypeFieldName());
			ps.setString(7, entry.getUserLoginValue());
			ps.setInt(8, entry.getStrictNess());
			
			ps.setLong(9, entry.getId());
			
			int count = ps.executeUpdate();
			refresh();
			
			return count; 
		}
		catch(Exception e)
		{
			Utility.LOG.error(e);
			throw new CorrException(e);
		}
	}

	public int deleteEntry( UserMapEntry entry ) throws CorrException
	{
		Connection con = null;
		try
		{
			con = DataSourcePool.getConnection();
			return deleteEntry(con,entry);
		}
		catch(Exception e)
		{
			Utility.LOG.error(e);
			throw new CorrException(e);
		}
		finally
		{
			try {
				if( null != con && con.isClosed() == false)
				{
					con.close();
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}
	
	public int deleteEntry( Connection con, UserMapEntry entry ) throws CorrException
	{
		try 
		{
			
			if( null == entry )
				throw new CorrException("Provided UserMapEntry was null.");

			if( null == con || con.isClosed() == true )
				throw new CorrException("Connection was either null or closed.");
		
			String sql = "delete from " + TableName + " where " +  Id + "= ?";
		
			PreparedStatement ps = con.prepareStatement(sql);
			ps.setLong(1, entry.getId());
			
			int count = ps.executeUpdate();
			refresh();
			
			return count; 
		}
		catch(Exception e)
		{
			Utility.LOG.error(e);
			throw new CorrException(e);
		}
	}

	protected void initialize() throws CorrException {
		PropertyEntry cap = PropertyManager.lookupProperty(PropUserMapCacheSize);
		PropertyEntry winCap = PropertyManager.lookupProperty(PropUserMapCacheWindowSize);

		if( null == cap || null == winCap )
			throw new CorrException("Properties " + PropUserMapCacheSize + " and " + PropUserMapCacheWindowSize + " cannot be null. And must be Integers.");
		
		Integer capacity = null;
		Integer windowCapacity = null;
		try
		{
			capacity = Integer.parseInt(cap.getValue());
			windowCapacity = Integer.parseInt(winCap.getValue());
		}
		catch( NumberFormatException nfe )
		{
			Utility.LOG.warn("The values in properties [" + cap + "] and [" + winCap + "] were not found to be Integers.");
			throw new CorrException("The values in properties [" + cap + "] and [" + winCap + "] were not found to be Integers.");
		}
		
		userMapCache = new UserMapCache(capacity, windowCapacity);
	}
}
