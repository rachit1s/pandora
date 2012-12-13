package corrGeneric.com.tbitsGlobal.server.managers;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Hashtable;

import transbit.tbits.common.DataSourcePool;
import transbit.tbits.common.TBitsLogger;
import corrGeneric.com.tbitsGlobal.server.cache.FieldNameCache;
import corrGeneric.com.tbitsGlobal.server.util.Utility;
import corrGeneric.com.tbitsGlobal.shared.domain.FieldNameEntry;
import corrGeneric.com.tbitsGlobal.shared.domain.PropertyEntry;
import corrGeneric.com.tbitsGlobal.shared.objects.CorrException;
import corrGeneric.com.tbitsGlobal.shared.objects.GenericParams;
import static corrGeneric.com.tbitsGlobal.shared.domain.FieldNameEntry.*;
import static corrGeneric.com.tbitsGlobal.shared.objects.GenericParams.*;

public class FieldNameManager extends AbstractManager
{
	private FieldNameCache fieldNameCache = null;	
	private static FieldNameManager instance = null;
	
	private FieldNameManager() throws CorrException
	{
		initialize();
		
		ManagerRegistry.getInstance().registerManager(FieldNameManager.class, this);
	}
	
	public synchronized static FieldNameManager getInstance() throws CorrException
	{
		if( null == instance )
			instance = new FieldNameManager();
		
		return instance;
	}
	
	public static Hashtable<String,FieldNameEntry> lookupFieldNameMap( String sysPrefix ) throws CorrException
	{
		return FieldNameManager.getInstance().getFieldNameCache().get(sysPrefix);
	}
	
	public static FieldNameEntry lookupFieldNameEntry( String sysPrefix, String corrFieldName ) throws CorrException
	{
		Hashtable<String, FieldNameEntry> map = FieldNameManager.getInstance().getFieldNameCache().get(sysPrefix);
		if( null == map )
			return null;
		 
		return map.get(corrFieldName);
	}
	/**
	 * @return the fieldNameCache
	 */
	private FieldNameCache getFieldNameCache() {
		return fieldNameCache;
	}
	public static final String GetAllFromDB = "select * from " + TableName +
												" where " + SysPrefix + "=?";
	
	public static Hashtable<String,FieldNameEntry> getFieldNameMapFromDB(String sysPrefix) throws CorrException
	{
		
		Connection con = null;
		try
		{
			con = DataSourcePool.getConnection();
			return getFieldNameMapFromDB(con, sysPrefix);
		} catch (SQLException e) 
		{
			Utility.LOG.warn(TBitsLogger.getStackTrace(e));
			throw new CorrException(GenericParams.FAILED_TO_RETRIEVE + "fields map.", e);
		}
		finally
		{
			try {
				if( null != con && con.isClosed() == false )
					con.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}
	
	public static Hashtable<String,FieldNameEntry> getFieldNameMapFromDB(Connection con,String sysPrefix) throws CorrException, SQLException
	{
		Hashtable<String,FieldNameEntry> fieldMap = new Hashtable<String,FieldNameEntry>();
		
		if( null == con || con.isClosed() == true)
			throw new IllegalArgumentException("The supplied connection object was null or was closed. con="+con);

		PreparedStatement ps = con.prepareStatement(GetAllFromDB);
		ps.setString(1, sysPrefix);
		
		ResultSet rs = ps.executeQuery();
		if( null != rs)
		{
			while( rs.next() )
			{
				FieldNameEntry cfn = corrGeneric.com.tbitsGlobal.server.util.Utility.createFieldNameEntryFromResultSet(rs);
				fieldMap.put(cfn.getCorrFieldName(),cfn);
			}
		}
	
		if( fieldMap.size() == 0) 
			return null;
		
		return fieldMap;
	}

	public void refresh() throws CorrException 
	{
		initialize();
	}
	

	public int persistEntry(FieldNameEntry entry) throws CorrException
	{
		if( null == entry )
			throw new CorrException("FieldNameEntry was null.");
		
		if( entry.getId() == -1 )
		{
			return insertEntry(entry);
		}
		else
		{
			return updateEntry(entry);
		}
	}
	
	public int insertEntry( FieldNameEntry entry ) throws CorrException
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
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	public int insertEntry(Connection con, FieldNameEntry entry) throws CorrException 
	{
		try
		{
			if( null == entry )
				throw new CorrException("Provided FieldNameEntry was null.");
	
			if( null == con || con.isClosed() == true )
				throw new CorrException("Connection was either null or closed.");
		
			String sql = "insert into " + TableName + " values (?,?,?)" ;

			PreparedStatement ps = con.prepareStatement(sql);
			ps.setString(1, entry.getCorrFieldName());
			ps.setString(2, entry.getSysPrefix());
			ps.setString(3, entry.getBaFieldName());
			
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

	public int updateEntry( FieldNameEntry entry ) throws CorrException
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
	
	public int updateEntry(Connection con, FieldNameEntry entry) throws CorrException 
	{
		try {
			if( null == entry )
				throw new CorrException("Provided FieldNameEntry was null.");

			if( null == con || con.isClosed() == true )
				throw new CorrException("Connection was either null or closed.");
		
			String sql = "update " + TableName + " set " + CorrFieldName + " = ?, " + SysPrefix + " = ? , " + FieldName + " = ?" + " where " +  Id + "= ?";
		
			PreparedStatement ps = con.prepareStatement(sql);
			ps.setString(1, entry.getCorrFieldName());
			ps.setString(2, entry.getSysPrefix());
			ps.setString(3, entry.getBaFieldName());
			
			ps.setLong(4, entry.getId());
			
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

	public int deleteEntry( FieldNameEntry entry ) throws CorrException
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
	
	public int deleteEntry( Connection con, FieldNameEntry entry ) throws CorrException
	{
		try {
			
			if( null == entry )
				throw new CorrException("Provided FieldNameEntry was null.");

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

	protected void initialize() throws CorrException 
	{
		PropertyEntry cap = PropertyManager.lookupProperty(PropFieldNameCacheSize);
		PropertyEntry winSize = PropertyManager.lookupProperty(PropFieldNameCacheWindowSize);
		
		if( null == cap || null == winSize )
			throw new CorrException("The property : " + PropFieldNameCacheSize + " Or " + PropFieldNameCacheWindowSize + " was not found. Thier Integer values are expected." );
		
		Integer capacity = null;
		Integer windowSize = null;
		try
		{
			capacity = Integer.parseInt(cap.getValue());
			windowSize = Integer.parseInt(winSize.getValue());
		}
		catch( NumberFormatException nfe)
		{
			throw new CorrException("The property : " + cap + " Or " + winSize + " does not contain an Integer value.");
		}
		
		fieldNameCache = new FieldNameCache(capacity, windowSize);
	}
}
