package corrGeneric.com.tbitsGlobal.server.managers;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Hashtable;

import transbit.tbits.common.DataSourcePool;
import transbit.tbits.common.DatabaseException;
//import transbit.tbits.exception.CorrException;

import corrGeneric.com.tbitsGlobal.server.cache.ReportParamCache;
import corrGeneric.com.tbitsGlobal.server.util.Utility;
import corrGeneric.com.tbitsGlobal.shared.domain.PropertyEntry;
import corrGeneric.com.tbitsGlobal.shared.domain.ReportEntry;
import corrGeneric.com.tbitsGlobal.shared.domain.ReportNameEntry;
import corrGeneric.com.tbitsGlobal.shared.domain.ReportParamEntry;
import corrGeneric.com.tbitsGlobal.shared.objects.CorrException;
import static corrGeneric.com.tbitsGlobal.server.util.Utility.LOG;
import static corrGeneric.com.tbitsGlobal.shared.domain.ReportParamEntry.*;
import static corrGeneric.com.tbitsGlobal.shared.objects.GenericParams.*;

public class ReportParamsManager  extends AbstractManager
{
	/**
	 * @return the reportParamCache
	 */
	public ReportParamCache getReportParamCache() {
		return reportParamCache;
	}
	private ReportParamCache reportParamCache = null;
	private static ReportParamsManager instance = null;
	private ReportParamsManager() throws CorrException
	{
		initialize();
		
		ManagerRegistry.getInstance().registerManager(ReportParamsManager.class, this);
	}
	
	public synchronized static ReportParamsManager getInstance() throws CorrException
	{
		if( null == instance )
			instance = new ReportParamsManager();
		
		return instance;
	}
	
	public static Hashtable<String, ReportParamEntry> getReportParamMapFromCache(int reportId) throws CorrException
	{
		return ReportParamsManager.getInstance().getReportParamCache().get(reportId);
	}
	
	public static ReportParamEntry getReportParamEntryFromCache(int reportId, String paramName) throws CorrException
	{
		Hashtable<String, ReportParamEntry> map = ReportParamsManager.getInstance().getReportParamCache().get(reportId);
		if( null == map )
			return null;
		
		return map.get(paramName);
	}
	
	public static String GetReportParam = " select * from " + TableName + 
										" where " + ReportId + "=? "; 	
	public static Hashtable<String,ReportParamEntry> getReportParamMap(int reportId) throws CorrException
	{
		Connection con = null;
		try
		{
			con = DataSourcePool.getConnection();
			return getReportParamMap(con, reportId);
		} catch (SQLException e) 
		{
			throw new CorrException(FAILED_TO_RETRIEVE + " report param map for reportId=" + reportId , e);
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

	public static Hashtable<String,ReportParamEntry> getReportParamMap(Connection con, int reportId) throws SQLException
	{
		Hashtable<String,ReportParamEntry> map = new Hashtable<String,ReportParamEntry>();
		
		if( null == con || con.isClosed() )
			throw new IllegalArgumentException("Either the connection was null or closed. con = " + con );
		
		PreparedStatement ps = con.prepareStatement(GetReportParam);
		ps.setInt(1, reportId);
		ResultSet rs = ps.executeQuery();
		
		if( null != rs )
		{
			while( rs.next())
			{
				ReportParamEntry rpe = corrGeneric.com.tbitsGlobal.server.util.Utility.createReportParamEntryFromResultSet(rs);
				map.put(rpe.getParamName(), rpe	);
			}
		}
		
		if( map.size() == 0)
			return null;
		
		return map;
	}
	
	@Override
	public void refresh() throws CorrException {
		initialize();
	}
	
	public int persistEntry(ReportParamEntry entry) throws CorrException {
		if (null == entry)
			throw new CorrException("ReportParamEntry was null.");

		if (entry.getId() == -1) {
			return insertEntry(entry);
		} else {
			return updateEntry(entry);
		}
	}
	
	public int insertEntry( ReportParamEntry entry ) throws CorrException
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
	
	private int insertEntry(Connection con, ReportParamEntry entry) throws CorrException 
	{
		try
		{
			if( null == entry )
				throw new CorrException("Provided ReportParamEntry was null.");
	
			if( null == con || con.isClosed() == true )
				throw new CorrException("Connection was either null or closed.");
		
			String sql = "insert into " + TableName + " values (?,?,?,?,?)" ;

			PreparedStatement ps = con.prepareStatement(sql);
			ps.setInt(1, entry.getReportId());
			ps.setString(2, entry.getParamType());
			ps.setString(3, entry.getParamName());
			ps.setString(4, entry.getParamValueType());
			ps.setString(5, entry.getParamValue());
			
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

	public int updateEntry( ReportParamEntry entry ) throws CorrException
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
	
	private int updateEntry(Connection con, ReportParamEntry entry) throws CorrException 
	{
		try {
			if( null == entry )
				throw new CorrException("Provided ReportParamEntry was null.");

			if( null == con || con.isClosed() == true )
				throw new CorrException("Connection was either null or closed.");
		
			String sql = "update " + TableName + " set " + ReportId + " = ?, " + ParamType + " = ?," + ParamName + " = ?," + ParamValueType + " = ?," + ParamValue + " = ?" + " where " +  Id + "= ?";
		
			PreparedStatement ps = con.prepareStatement(sql);
			ps.setInt(1, entry.getReportId());
			ps.setString(2, entry.getParamType());
			ps.setString(3, entry.getParamName());
			ps.setString(4, entry.getParamValueType());
			ps.setString(5, entry.getParamValue());
			ps.setLong(6, entry.getId());
			
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

	public int deleteEntry( ReportParamEntry entry ) throws CorrException
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
	
	public int deleteEntry( Connection con, ReportParamEntry entry ) throws CorrException
	{
		try {
			
			if( null == entry )
				throw new CorrException("Provided ReportParamEntry was null.");

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
		PropertyEntry cap = PropertyManager.lookupProperty(PropReportParamsCacheSize);
		PropertyEntry winSize = PropertyManager.lookupProperty(PropReportParamsCacheWindowSize);
		if( null == cap || null == winSize )
			throw new CorrException("The properties : " + PropReportParamsCacheSize + " Or " + PropReportParamsCacheWindowSize + " was null. Integer values are expected.");

		Integer capacity = null;
		Integer windowSize = null;
		try
		{
			capacity = Integer.parseInt(cap.getValue());
			windowSize = Integer.parseInt(winSize.getValue());
		}
		catch(NumberFormatException nfe)
		{
			throw new CorrException("The properties : " + cap + " Or " + winSize + " does not have a integer value.", nfe);
		}
		
		reportParamCache = new ReportParamCache(capacity, windowSize);
	}
}
