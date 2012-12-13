package corrGeneric.com.tbitsGlobal.server.managers;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Hashtable;

import transbit.tbits.common.DataSourcePool;
import transbit.tbits.common.TBitsLogger;

import corrGeneric.com.tbitsGlobal.server.cache.BaFieldMapCache;
import corrGeneric.com.tbitsGlobal.server.util.Utility;
import corrGeneric.com.tbitsGlobal.shared.domain.BaFieldEntry;
import corrGeneric.com.tbitsGlobal.shared.domain.PropertyEntry;
import corrGeneric.com.tbitsGlobal.shared.key.BaFieldKey;
import corrGeneric.com.tbitsGlobal.shared.objects.CorrException;
import static corrGeneric.com.tbitsGlobal.shared.domain.BaFieldEntry.*;
import static corrGeneric.com.tbitsGlobal.shared.objects.GenericParams.*;
import static corrGeneric.com.tbitsGlobal.server.util.Utility.LOG;

public class BaFieldManager extends AbstractManager
{
	
	private BaFieldMapCache baFieldMapCache = null;
	private static BaFieldManager instance = null;
	
	private BaFieldManager() throws CorrException
	{
		initialize();
		
		ManagerRegistry.getInstance().registerManager(BaFieldManager.class, this);
	}

	protected void initialize() throws CorrException
	{
		PropertyEntry capacity = PropertyManager.lookupProperty(PropBaFieldMapCacheSize);
		PropertyEntry windowCap = PropertyManager.lookupProperty(PropBaFieldMapCacheWindowSize);
		
		if( null == capacity || null == windowCap )
			throw new CorrException("The properties : " + PropBaFieldMapCacheSize + " Or " + PropBaFieldMapCacheWindowSize + " not found. They must contain integer values.");
		
		Integer cap= null;
		Integer winCap = null;
		try
		{
			cap = Integer.parseInt(capacity.getValue());
			winCap = Integer.parseInt(windowCap.getValue());
		}
		catch(NumberFormatException nfe)
		{
			throw new CorrException("The property : " + capacity + " Or " + windowCap + " does not contain Integer values." );
		}
		
		baFieldMapCache = new BaFieldMapCache(cap, winCap);
	}
	
	public synchronized static BaFieldManager getInstance() throws CorrException
	{
		if( null == instance )
			instance = new BaFieldManager();
		
		return instance;
	}
	
	
	public static Hashtable<String, BaFieldEntry> lookupBaFieldMap(BaFieldKey bfk) throws CorrException
	{
		return BaFieldManager.getInstance().getBaFieldMapCache().get(bfk);
	}
	private static final String GetBaFieldMap = " select * from " + TableName +
												" where " + FromSysPrefix + "=? and " + ToSysPrefix + "=? ";
	
	public static Hashtable<String, BaFieldEntry> getBaFieldMapFromDB(BaFieldKey t)
	throws CorrException
	{
		Connection con = null;
		try
		{
			con = DataSourcePool.getConnection();
			if( null != con )
			{
				return getBaFieldMapFromDB(con, t);
			}
			else
			{
				Utility.LOG.warn(FAILED_CON);
				throw new CorrException(FAILED_CON);
			}
			
		} catch (SQLException e) {
			Utility.LOG.warn(FAILED_TO_RETRIEVE + " the BA Field Mapping for " + t);
			Utility.LOG.warn(TBitsLogger.getStackTrace(e));
			throw new CorrException(FAILED_TO_RETRIEVE + " the BA Field Mapping for " + t);
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
	
	public static Hashtable<String, BaFieldEntry> getBaFieldMapFromDB(Connection con,BaFieldKey t)
	throws SQLException
	{
		Hashtable<String,BaFieldEntry> map = new Hashtable<String,BaFieldEntry>();
	
		PreparedStatement ps = con.prepareStatement(GetBaFieldMap);
		ps.setString(1, t.getFromSysPrefix());
		ps.setString(2, t.getToSysPrefix());
		ResultSet rs = ps.executeQuery();
		
		if( null != rs )
		{
			while( rs.next() )
			{
				BaFieldEntry cbf = corrGeneric.com.tbitsGlobal.server.util.Utility.createBaFieldEntryFromResultSet(rs);
				map.put(cbf.getFromFieldName(), cbf);
			}
		}
		
		if( map.size() == 0)
			return null;
		
		return map;
	}

	public void refresh() throws CorrException 
	{
		initialize();
	}

	/**
	 * @return the baFieldMapCache
	 */
	private BaFieldMapCache getBaFieldMapCache() {
		return baFieldMapCache;
	}

	public int persistEntry(BaFieldEntry entry) throws CorrException
	{
		if( null == entry )
			throw new CorrException("BaFieldEntry was null.");
		
		if( entry.getId() == -1 )
		{
			return insertEntry(entry);
		}
		else 
		{
			return updateEntry(entry);
		}
	}
	
	public int insertEntry( BaFieldEntry entry ) throws CorrException
	{
		Connection con = null;
		try
		{
			con = DataSourcePool.getConnection();
			return insertEntry(con,entry);
		} catch (SQLException e) {
			e.printStackTrace();
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
	
	public int insertEntry(Connection con, BaFieldEntry entry) throws CorrException 
	{
		try
		{
			if( null == entry )
				throw new CorrException("Provided BaFieldEntry was null.");
	
			if( null == con || con.isClosed() == true )
				throw new CorrException("Connection was either null or closed.");
		
			String sql = "insert into " + BaFieldEntry.TableName + " values (?,?,?,?)" ;

			PreparedStatement ps = con.prepareStatement(sql);
			ps.setString(1, entry.getFromSysPrefix());
			ps.setString(2, entry.getFromFieldName());
			ps.setString(3, entry.getToSysPrefix());
			ps.setString(4, entry.getToFieldName());
			
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

	public int updateEntry( BaFieldEntry entry ) throws CorrException
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
	
	public int updateEntry(Connection con, BaFieldEntry entry) throws CorrException 
	{
		try {
			if( null == entry )
				throw new CorrException("Provided BaFieldEntry was null.");

			if( null == con || con.isClosed() == true )
				throw new CorrException("Connection was either null or closed.");
		
			String sql = "update " + TableName + " set " + FromSysPrefix + " = ?, " + FromFieldName + " = ? , " + ToSysPrefix + " = ? ," + ToFieldName + " = ? "  + " where " +  Id + "= ?";
		
			PreparedStatement ps = con.prepareStatement(sql);
			ps.setString(1, entry.getFromSysPrefix());
			ps.setString(2, entry.getFromFieldName());
			ps.setString(3, entry.getToSysPrefix());
			ps.setString(4, entry.getToFieldName());
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

	public int deleteEntry( BaFieldEntry entry ) throws CorrException
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
	
	public int deleteEntry( Connection con, BaFieldEntry entry ) throws CorrException
	{
		try {
			
			if( null == entry )
				throw new CorrException("Provided BaFieldEntry was null.");

			if( null == con || con.isClosed() == true )
				throw new CorrException("Connection was either null or closed.");
		
			String sql = "delete from " + BaFieldEntry.TableName + " where " +  BaFieldEntry.Id + "= ?";
		
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
}
