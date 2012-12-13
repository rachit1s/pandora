package corrGeneric.com.tbitsGlobal.server.managers;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import transbit.tbits.common.DataSourcePool;
//import transbit.tbits.exception.CorrException;

import corrGeneric.com.tbitsGlobal.server.cache.ReportMapCache;
import corrGeneric.com.tbitsGlobal.server.util.Utility;
import corrGeneric.com.tbitsGlobal.shared.domain.PropertyEntry;
import corrGeneric.com.tbitsGlobal.shared.domain.ProtocolOptionEntry;
import corrGeneric.com.tbitsGlobal.shared.domain.ReportEntry;
import corrGeneric.com.tbitsGlobal.shared.objects.CorrException;
import static corrGeneric.com.tbitsGlobal.server.util.Utility.LOG;
import static corrGeneric.com.tbitsGlobal.shared.domain.ReportEntry.*;
import static corrGeneric.com.tbitsGlobal.shared.objects.GenericParams.*;

public class ReportManager extends AbstractManager
{
	/**
	 * @return the reportMapCache
	 */
	public ReportMapCache getReportMapCache() {
		return reportMapCache;
	}

	private ReportMapCache reportMapCache = null;
	
	private static ReportManager instance = null;
	
	private ReportManager() throws CorrException
	{
		initialize();

		ManagerRegistry.getInstance().registerManager(ReportManager.class, this);
	}
	
	public synchronized static ReportManager getInstance() throws CorrException
	{
		if( null == instance )
			instance = new ReportManager();
		
		return instance;
	}
	
	public static ArrayList<ReportEntry> getReportMapFromCache(String sysPrefix) throws CorrException
	{
		return ReportManager.getInstance().getReportMapCache().get(sysPrefix);
	}
	
	public static final String GetReportMap = " select * from " + TableName +
											 " where " + SysPrefix + "=? ";
	
	public static ArrayList<ReportEntry> getReportMapFromDB(String sysPrefix) throws CorrException
	{
		Connection con = null;
		try
		{
			con = DataSourcePool.getConnection();
			return getReportMapFromDB(con, sysPrefix);
		} catch (SQLException e) {
			Utility.LOG.info(FAILED_TO_RETRIEVE + " the report parameters for sysPrefix : " + sysPrefix);
			throw new CorrException(FAILED_TO_RETRIEVE + " the report parameters for sysPrefix : " + sysPrefix);
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

	public static ArrayList<ReportEntry> getReportMapFromDB(Connection con,
			String sysPrefix) throws SQLException 
	{
		if( null == con )
			throw new IllegalArgumentException("The supplied connection object was null.");
		
		ArrayList<ReportEntry> map = new ArrayList<ReportEntry>();
		
		PreparedStatement ps = con.prepareStatement(GetReportMap);
		ps.setString(1, sysPrefix);
		ResultSet rs = ps.executeQuery();
		if( null != rs )
		{
			while( rs.next() ) 
			{
				ReportEntry rm = corrGeneric.com.tbitsGlobal.server.util.Utility.createReportEntryFromResultSet(rs);
				if( null != rm )
					map.add(rm);
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

	public int persistEntry(ReportEntry entry) throws CorrException {
		if (null == entry)
			throw new CorrException("ReportEntry was null.");

		if (entry.getId() == -1) {
			return insertEntry(entry);
		} else {
			return updateEntry(entry);
		}
	}
	
	public int insertEntry( ReportEntry entry ) throws CorrException
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
	
	private int insertEntry(Connection con, ReportEntry entry) throws CorrException 
	{
		try
		{
			if( null == entry )
				throw new CorrException("Provided ReportEntry was null.");
	
			if( null == con || con.isClosed() == true )
				throw new CorrException("Connection was either null or closed.");
		
			String sql = "insert into " + TableName + " values (?,?,?,?,?,?,?)" ;

			PreparedStatement ps = con.prepareStatement(sql);
			ps.setString(1, entry.getSysPrefix());
			ps.setString(2, entry.getType1());
			ps.setString(3, entry.getType2());
			ps.setString(4, entry.getType3());
			ps.setString(5, entry.getType4());
			ps.setString(6, entry.getType5());
			ps.setInt(7, entry.getReportId());
			
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

	public int updateEntry( ReportEntry entry ) throws CorrException
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
	
	private int updateEntry(Connection con, ReportEntry entry) throws CorrException 
	{
		try {
			if( null == entry )
				throw new CorrException("Provided ReportEntry was null.");

			if( null == con || con.isClosed() == true )
				throw new CorrException("Connection was either null or closed.");
		
			String sql = "update " + TableName + " set " + SysPrefix + " = ?, " + Type1 + " = ?, " + Type2 + " = ? , " + Type3 + " = ?," + Type4 + " = ?," + Type5 + " = ?," + ReportId + " = ?" + " where " +  Id + "= ?";
		
			PreparedStatement ps = con.prepareStatement(sql);
			ps.setString(1, entry.getSysPrefix());
			ps.setString(2, entry.getType1());
			ps.setString(3, entry.getType2());
			ps.setString(4, entry.getType3());
			ps.setString(5, entry.getType4());
			ps.setString(6, entry.getType5());
			ps.setInt(7, entry.getReportId());
			ps.setLong(8, entry.getId());
			
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

	public int deleteEntry( ReportEntry entry ) throws CorrException
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
	
	public int deleteEntry( Connection con, ReportEntry entry ) throws CorrException
	{
		try {
			
			if( null == entry )
				throw new CorrException("Provided ReportEntry was null.");

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
		ReportEntry re = new ReportEntry(-1, "sysPrefix", "type1", "type2", "type3", "type4", "type5", 1);		
		System.out.println("inserting report entry re : " + re);
		try {
			ReportManager.getInstance().insertEntry(re);
			System.out.println("inserted report entry re : " + re);
		} catch (CorrException e) {
			e.printStackTrace();
		}
	}

	protected void initialize() throws CorrException {
		PropertyEntry cap = PropertyManager.lookupProperty(PropReportMapCacheSize);
		PropertyEntry winSize = PropertyManager.lookupProperty(PropReportMapCacheWindowSize);
		
		if( null == cap || null == winSize )
		{
			throw new CorrException("The propertis : " + PropReportMapCacheSize + " Or " + PropReportMapCacheWindowSize + " not found. Their values must be integers.");
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
			Utility.LOG.warn("The propertis : " + PropReportMapCacheSize + " and " + PropReportMapCacheWindowSize + " must be integers. But their respective supplied values are : " + cap.getValue() + "," + winSize.getValue());
			throw new CorrException("The propertis : " + PropReportMapCacheSize + " and " + PropReportMapCacheWindowSize + " must be integers. But their respective supplied values are : " + cap.getValue() + "," + winSize.getValue());
		}
		reportMapCache = new ReportMapCache(capacity, windowSize);
	}
}
