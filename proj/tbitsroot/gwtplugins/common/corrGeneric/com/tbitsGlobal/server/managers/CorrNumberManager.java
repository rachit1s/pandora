package corrGeneric.com.tbitsGlobal.server.managers;

import static corrGeneric.com.tbitsGlobal.server.util.Utility.LOG;
import static corrGeneric.com.tbitsGlobal.shared.domain.CorrNumberEntry.Id;
import static corrGeneric.com.tbitsGlobal.shared.domain.CorrNumberEntry.MaxIdFields;
import static corrGeneric.com.tbitsGlobal.shared.domain.CorrNumberEntry.MaxIdFormat;
import static corrGeneric.com.tbitsGlobal.shared.domain.CorrNumberEntry.NumFields;
import static corrGeneric.com.tbitsGlobal.shared.domain.CorrNumberEntry.NumFormat;
import static corrGeneric.com.tbitsGlobal.shared.domain.CorrNumberEntry.NumType1;
import static corrGeneric.com.tbitsGlobal.shared.domain.CorrNumberEntry.NumType2;
import static corrGeneric.com.tbitsGlobal.shared.domain.CorrNumberEntry.NumType3;
import static corrGeneric.com.tbitsGlobal.shared.domain.CorrNumberEntry.SysPrefix;
import static corrGeneric.com.tbitsGlobal.shared.domain.CorrNumberEntry.TableName;
import static corrGeneric.com.tbitsGlobal.shared.objects.GenericParams.FAILED_TO_RETRIEVE;
import static corrGeneric.com.tbitsGlobal.shared.objects.GenericParams.PropCorrNumCacheSize;
import static corrGeneric.com.tbitsGlobal.shared.objects.GenericParams.PropCorrNumCacheWindowSize;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;

import transbit.tbits.common.DataSourcePool;
import corrGeneric.com.tbitsGlobal.server.cache.CorrNumberCache;
import corrGeneric.com.tbitsGlobal.server.util.Utility;
import corrGeneric.com.tbitsGlobal.shared.domain.CorrNumberEntry;
import corrGeneric.com.tbitsGlobal.shared.domain.PropertyEntry;
import corrGeneric.com.tbitsGlobal.shared.key.CorrNumberKey;
import corrGeneric.com.tbitsGlobal.shared.objects.CorrException;

public class CorrNumberManager extends AbstractManager
{
	/**
	 */
	public CorrNumberCache getCorrNumberCache() {
		return corrNumberCache;
	}

	private CorrNumberCache corrNumberCache = null;
	
	private static CorrNumberManager instance = null;
	
	private CorrNumberManager() throws CorrException
	{
		initialize();

		ManagerRegistry.getInstance().registerManager(CorrNumberManager.class, this);
	}
	
	public synchronized static CorrNumberManager getInstance() throws CorrException
	{
		if( null == instance )
			instance = new CorrNumberManager();
		
		return instance;
	}
	
	public static HashMap<CorrNumberKey,CorrNumberEntry> getCorrNumberFromCache(String sysPrefix) throws CorrException
	{
		return CorrNumberManager.getInstance().getCorrNumberCache().get(sysPrefix);
	}
	
	public static final String GetCorrNumberMap = " select * from " + TableName +
											 " where " + SysPrefix + "=? ";
	
	public static HashMap<CorrNumberKey,CorrNumberEntry> getCorrNumberFromDB(String sysPrefix) throws CorrException
	{
		Connection con = null;
		try
		{
			con = DataSourcePool.getConnection();
			return getCorrNumberFromDB(con, sysPrefix);
		} catch (SQLException e) {
			Utility.LOG.info(FAILED_TO_RETRIEVE + " the corr number parameters for sysPrefix : " + sysPrefix);
			throw new CorrException(FAILED_TO_RETRIEVE + " the corr number parameters for sysPrefix : " + sysPrefix);
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

	public static HashMap<CorrNumberKey,CorrNumberEntry> getCorrNumberFromDB(Connection con,
			String sysPrefix) throws SQLException 
	{
		if( null == con )
			throw new IllegalArgumentException("The supplied connection object was null.");
		
		HashMap<CorrNumberKey,CorrNumberEntry> map = new HashMap<CorrNumberKey,CorrNumberEntry>();
		
		PreparedStatement ps = con.prepareStatement(GetCorrNumberMap);
		ps.setString(1, sysPrefix);
		ResultSet rs = ps.executeQuery();
		if( null != rs )
		{
			while( rs.next() ) 
			{
				CorrNumberEntry rm = corrGeneric.com.tbitsGlobal.server.util.Utility.createCorrNumberEntryFromResultSet(rs);
				if( null != rm )
					map.put(rm.getCnk(),rm);
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

	public int persistEntry(CorrNumberEntry entry) throws CorrException {
		if (null == entry)
			throw new CorrException("CorrNumberEntry was null.");

		if (entry.getId() == -1) {
			return insertEntry(entry);
		} else {
			return updateEntry(entry);
		}
	}
	
	public int insertEntry( CorrNumberEntry entry ) throws CorrException
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
	
	private int insertEntry(Connection con, CorrNumberEntry entry) throws CorrException 
	{
		try
		{
			if( null == entry )
				throw new CorrException("Provided CorrNumberEntry was null.");
	
			if( null == con || con.isClosed() == true )
				throw new CorrException("Connection was either null or closed.");
		
			String sql = "insert into " + TableName + " values (?,?,?,?,?,?,?,?)" ;

			PreparedStatement ps = con.prepareStatement(sql);
			ps.setString(1, entry.getSysPrefix());
			ps.setString(2, entry.getNumType1());
			ps.setString(3, entry.getNumType2());
			ps.setString(4, entry.getNumType3());
			ps.setString(5, entry.getNumberFormat());
			ps.setString(6, entry.getNumberFields());
			ps.setString(7, entry.getMaxIdFormat());
			ps.setString(8, entry.getMaxIdFields());
			
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

	public int updateEntry( CorrNumberEntry entry ) throws CorrException
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
	
	private int updateEntry(Connection con, CorrNumberEntry entry) throws CorrException 
	{
		try {
			if( null == entry )
				throw new CorrException("Provided CorrNumberEntry was null.");

			if( null == con || con.isClosed() == true )
				throw new CorrException("Connection was either null or closed.");
		
			String sql = "update " + TableName + " set " + SysPrefix + " = ?, " + NumType1 + " = ?, " + NumType2 + " = ? , " + NumType3 + " = ?," + NumFormat + " = ?," + NumFields + " = ?," + MaxIdFormat + " = ?," + MaxIdFields + " = ?" + " where " +  Id + "= ?";
		
			PreparedStatement ps = con.prepareStatement(sql);
			ps.setString(1, entry.getSysPrefix());
			ps.setString(2, entry.getNumType1());
			ps.setString(3, entry.getNumType2());
			ps.setString(4, entry.getNumType3());
			ps.setString(5, entry.getNumberFormat());
			ps.setString(6, entry.getNumberFields());
			ps.setString(7, entry.getMaxIdFormat());
			ps.setString(8, entry.getMaxIdFields());
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

	public int deleteEntry( CorrNumberEntry entry ) throws CorrException
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
	
	public int deleteEntry( Connection con, CorrNumberEntry entry ) throws CorrException
	{
		try {
			
			if( null == entry )
				throw new CorrException("Provided CorrNumberEntry was null.");

			if( null == con || con.isClosed() == true )
				throw new CorrException("Connection was either null or closed.");
		
			String sql = "delete from " + TableName + " where " +  Id + "=?";
		
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
	
	public static void main(String argv[])
	{
		CorrNumberEntry re = new CorrNumberEntry(-1, "sysPrefix", "type1", "type2", "type3", "numberformat", "numfields", "maxIdformat", "maxIdFields");		
		System.out.println("inserting corr number entry re : " + re);
		try {
			CorrNumberManager.getInstance().insertEntry(re);
			System.out.println("inserted corr number entry re : " + re);
		} catch (CorrException e) {
			e.printStackTrace();
		}
	}

	protected void initialize() throws CorrException {
		PropertyEntry cap = PropertyManager.lookupProperty(PropCorrNumCacheSize);
		PropertyEntry winSize = PropertyManager.lookupProperty(PropCorrNumCacheWindowSize);
		
		if( null == cap || null == winSize )
		{
			throw new CorrException("The propertis : " + PropCorrNumCacheSize + " Or " + PropCorrNumCacheWindowSize + " not found. Their values must be integers.");
		}
		Integer capacity = null;
		Integer windowSize = null;
		try
		{
			capacity = Integer.parseInt(cap.getValue());
			windowSize = Integer.parseInt(winSize.getValue());
		}
		catch( NumberFormatException nfe)
		{
			Utility.LOG.warn("The properties : " + PropCorrNumCacheWindowSize + " and " + PropCorrNumCacheSize + " must be integers. But their respective supplied values are : " + cap.getValue() + "," + winSize.getValue());
			throw new CorrException("The properties : " + PropCorrNumCacheWindowSize + " and " + PropCorrNumCacheSize + " must be integers. But their respective supplied values are : " + cap.getValue() + "," + winSize.getValue());
		}
		corrNumberCache = new CorrNumberCache(capacity, windowSize);
	}
}
