package corrGeneric.com.tbitsGlobal.server.managers;

import static corrGeneric.com.tbitsGlobal.server.util.Utility.LOG;
import static corrGeneric.com.tbitsGlobal.shared.domain.ProtocolOptionEntry.*;
import static corrGeneric.com.tbitsGlobal.shared.objects.GenericParams.*;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Hashtable;

import transbit.tbits.common.DataSourcePool;
import transbit.tbits.common.TBitsLogger;
//import transbit.tbits.exception.CorrException;
import corrGeneric.com.tbitsGlobal.server.cache.ProtocolOptionCache;
import corrGeneric.com.tbitsGlobal.server.cache.ProtocolOptionNameCache;
import corrGeneric.com.tbitsGlobal.server.util.Utility;
import corrGeneric.com.tbitsGlobal.shared.domain.OnBehalfEntry;
import corrGeneric.com.tbitsGlobal.shared.domain.PropertyEntry;
import corrGeneric.com.tbitsGlobal.shared.domain.ProtocolOptionEntry;
import corrGeneric.com.tbitsGlobal.shared.objects.CorrException;

public final class ProtocolOptionsManager extends AbstractManager
{
	private ProtocolOptionCache protocolOptionCache ;
	private static ProtocolOptionsManager instance ;
	private ProtocolOptionNameCache protocolOptionNameCache;
	
	private ProtocolOptionsManager() throws CorrException
	{
		initialize();
		
		ManagerRegistry.getInstance().registerManager(ProtocolOptionsManager.class, this);
	}
	
	public synchronized static ProtocolOptionsManager getInstance() throws CorrException
	{
		if( null == instance )
			instance = new ProtocolOptionsManager();
		
		return instance;
	}

	public static Hashtable<String,ProtocolOptionEntry> lookupAllProtocolEntry(String sysPrefix) throws CorrException{
		return ProtocolOptionsManager.getInstance().getProtocolOptionCache().get(sysPrefix);
	}
	
	public static ArrayList<ProtocolOptionEntry> lookupAllProtocolEntriesByName(String optionName) throws CorrException{
		return ProtocolOptionsManager.getInstance().getProtocolOptionNameCache().get(optionName);
	}

	public static ProtocolOptionEntry lookupProtocolEntry(String sysPrefix, String optionName) throws CorrException{
		Hashtable<String,ProtocolOptionEntry> map = ProtocolOptionsManager.getInstance().getProtocolOptionCache().get(sysPrefix);
		if( null == map )
			return null;
		return map.get(optionName);
	}
	
	public static String GetProtocolOptions = " select * from " + TableName + 
												" where " + SysPrefix + "=?";
	
	public static String GetProtocolOptionsWithName = " select * from " + TableName + 
	" where " + OptionName + "=?";
	
	public static Hashtable<String,ProtocolOptionEntry> getProtocolOptionsFromDB(String sysPrefix) throws CorrException
	{
		Connection con = null;
		try
		{
			con = DataSourcePool.getConnection();
			return getProtocolOptionsFromDB(con, sysPrefix);
		} catch (SQLException e) {
			e.printStackTrace();
			throw new CorrException(FAILED_TO_RETRIEVE + " the protocol Options Entries for sysPrefix : " + sysPrefix);
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
	
	public static ArrayList<ProtocolOptionEntry> getProtocolOptionNamesFromDB(String optionName) throws CorrException
	{
		Connection con = null;
		try
		{
			con = DataSourcePool.getConnection();
			return getProtocolOptionNamesFromDB(con, optionName);
		} catch (SQLException e) {
			e.printStackTrace();
			throw new CorrException(FAILED_TO_RETRIEVE + " the protocol Options Entries for sysPrefix : " + optionName);
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
	public static Hashtable<String,ProtocolOptionEntry> getProtocolOptionsFromDB(Connection con,String sysPrefix) throws SQLException
	{
		Hashtable<String,ProtocolOptionEntry> map = new Hashtable<String,ProtocolOptionEntry>();
		
		if( null == con || con.isClosed() == true )
			throw new IllegalArgumentException("The supplied connection object was null or was closed.");
		
		PreparedStatement ps = con.prepareStatement(GetProtocolOptions);
		ps.setString(1, sysPrefix);
		
		ResultSet rs = ps.executeQuery();
		if( null != rs )
		{
			while( rs.next() )
			{
				ProtocolOptionEntry poe = corrGeneric.com.tbitsGlobal.server.util.Utility.createProtocolOptionEntryFromResultSet(rs);
				if(null != ps )
					map.put(poe.getName(), poe);
			}
		}
		
		if( map.size() == 0 )
			return null;
		
		return map;
	}
	
	public static ArrayList<ProtocolOptionEntry> getProtocolOptionNamesFromDB(Connection con,String optionName) throws SQLException
	{
		ArrayList<ProtocolOptionEntry> list = new ArrayList<ProtocolOptionEntry>();
		
		if( null == con || con.isClosed() == true )
			throw new IllegalArgumentException("The supplied connection object was null or was closed.");
		
		PreparedStatement ps = con.prepareStatement(GetProtocolOptionsWithName);
		ps.setString(1, optionName);
		
		ResultSet rs = ps.executeQuery();
		if( null != rs )
		{
			while( rs.next() )
			{
				ProtocolOptionEntry poe = corrGeneric.com.tbitsGlobal.server.util.Utility.createProtocolOptionEntryFromResultSet(rs);
				if(null != ps )
					list.add(poe);
			}
		}
		
		if( list.size() == 0 )
			return null;
		
		return list;
	}
	
	/**
	 * @return the protocolOptionCache
	 */
	public ProtocolOptionCache getProtocolOptionCache() {
		return protocolOptionCache;
	}

	/**
	 * @return the protocolOptionNameCache
	 */
	private ProtocolOptionNameCache getProtocolOptionNameCache() {
		return protocolOptionNameCache;
	}

	@Override
	public void refresh() throws CorrException {
		initialize();
	}
	
	public int persistEntry(ProtocolOptionEntry entry) throws CorrException {
		if (null == entry)
			throw new CorrException("ProtocolOptionEntry was null.");

		if (entry.getId() == -1) {
			return insertEntry(entry);
		} else {
			return updateEntry(entry);
		}
	}
	
	public int insertEntry( ProtocolOptionEntry entry ) throws CorrException
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
	
	private int insertEntry(Connection con, ProtocolOptionEntry entry) throws CorrException 
	{
		try
		{
			if( null == entry )
				throw new CorrException("Provided ProtocolOptionEntry was null.");
	
			if( null == con || con.isClosed() == true )
				throw new CorrException("Connection was either null or closed.");
		
			String sql = "insert into " + TableName + " values (?,?,?,?)" ;

			PreparedStatement ps = con.prepareStatement(sql);
			ps.setString(1, entry.getSysPrefix());
			ps.setString(2, entry.getName());
			ps.setString(3, entry.getValue());
			ps.setString(4, entry.getDescription());
			
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

	public int updateEntry( ProtocolOptionEntry entry ) throws CorrException
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
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	private int updateEntry(Connection con, ProtocolOptionEntry entry) throws CorrException 
	{
		try {
			if( null == entry )
				throw new CorrException("Provided ProtocolOptionEntry was null.");

			if( null == con || con.isClosed() == true )
				throw new CorrException("Connection was either null or closed.");
		
			String sql = "update " + TableName + " set " + SysPrefix + " = ?, " + OptionName + " = ?, " + OptionValue + " = ? , " + OptionDescription + " = ?" + " where " +  Id + "= ?";
		
			PreparedStatement ps = con.prepareStatement(sql);
			ps.setString(1, entry.getSysPrefix());
			ps.setString(2, entry.getName());
			ps.setString(3, entry.getValue());
			ps.setString(4, entry.getDescription());
			
			ps.setLong(5, entry.getId());
			
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

	public int deleteEntry( ProtocolOptionEntry entry ) throws CorrException
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
	
	public int deleteEntry( Connection con, ProtocolOptionEntry entry ) throws CorrException
	{
		try {
			
			if( null == entry )
				throw new CorrException("Provided ProtocolOptionEntry was null.");

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
		PropertyEntry cap = PropertyManager.lookupProperty(PropProtocolOptionsCacheSize);
		PropertyEntry winSize = PropertyManager.lookupProperty(PropProtocolOptionsCacheWindowSize);
		
		if( null == cap || null == winSize )
			throw new CorrException("The properties : " + PropProtocolOptionsCacheSize + " Or " + PropProtocolOptionsCacheWindowSize + " was null. " + "Their values respectively are : " + cap + "," + winSize + ". Integer value is expected.");
		
		Integer capacity = null;
		Integer windowSize = null;
		try
		{
			capacity = Integer.parseInt(cap.getValue());
			windowSize = Integer.parseInt(winSize.getValue());
		}
		catch(NumberFormatException nfe)
		{
			Utility.LOG.warn(TBitsLogger.getStackTrace(nfe));
			throw new CorrException("The propeties : " + cap + " Or " + winSize + " were not Integers.");
		}
		
		PropertyEntry ncap = PropertyManager.lookupProperty(PropProtocolOptionsNameCacheSize);
		PropertyEntry nwinSize = PropertyManager.lookupProperty(PropProtocolOptionsNameCacheWindowSize);
		
		if( null == ncap || null == nwinSize )
			throw new CorrException("The properties : " + PropProtocolOptionsNameCacheSize + " Or " + PropProtocolOptionsNameCacheWindowSize + " was null. " + "Their values respectively are : " + ncap + "," + nwinSize + ". Integer value is expected.");
		
		Integer ncapacity = null;
		Integer nwindowSize = null;
		try
		{
			ncapacity = Integer.parseInt(ncap.getValue());
			nwindowSize = Integer.parseInt(nwinSize.getValue());
		}
		catch(NumberFormatException nfe)
		{
			Utility.LOG.warn(TBitsLogger.getStackTrace(nfe));
			throw new CorrException("The propeties : " + ncap + " Or " + nwinSize + " were not Integers.");
		}
		
		protocolOptionCache = new ProtocolOptionCache(capacity, windowSize);
		protocolOptionNameCache = new ProtocolOptionNameCache(ncapacity, nwindowSize);
		
	}
}
