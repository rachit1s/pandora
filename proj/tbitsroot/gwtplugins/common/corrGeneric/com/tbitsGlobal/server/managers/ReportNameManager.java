package corrGeneric.com.tbitsGlobal.server.managers;

import static corrGeneric.com.tbitsGlobal.server.util.Utility.LOG;
import static corrGeneric.com.tbitsGlobal.shared.domain.ReportNameEntry.Id;
import static corrGeneric.com.tbitsGlobal.shared.domain.ReportNameEntry.ReportFileName;
import static corrGeneric.com.tbitsGlobal.shared.domain.ReportNameEntry.ReportId;
import static corrGeneric.com.tbitsGlobal.shared.domain.ReportNameEntry.TableName;
import static corrGeneric.com.tbitsGlobal.shared.objects.GenericParams.FAILED_TO_RETRIEVE;
import static corrGeneric.com.tbitsGlobal.shared.objects.GenericParams.PropReportNameMapCacheSize;
import static corrGeneric.com.tbitsGlobal.shared.objects.GenericParams.PropReportNameMapCacheWindowSize;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import transbit.tbits.common.DataSourcePool;
//import transbit.tbits.exception.CorrException;
import corrGeneric.com.tbitsGlobal.server.cache.ReportNameMapCache;
import corrGeneric.com.tbitsGlobal.server.util.Utility;
import corrGeneric.com.tbitsGlobal.shared.domain.PropertyEntry;
import corrGeneric.com.tbitsGlobal.shared.domain.ReportNameEntry;
import corrGeneric.com.tbitsGlobal.shared.objects.CorrException;

public class ReportNameManager extends AbstractManager
{
	private ReportNameMapCache reportNameMapCache = null;
	/**
	 * @return the reportNameMapCache
	 */
	public ReportNameMapCache getReportNameMapCache() {
		return reportNameMapCache;
	}

	private static ReportNameManager instance ;
	private ReportNameManager() throws CorrException
	{
		initialize();
		
		ManagerRegistry.getInstance().registerManager(ReportNameManager.class, this);
	}
	
	public synchronized static ReportNameManager getInstance() throws CorrException
	{
		if( null == instance )
			instance = new ReportNameManager();
		return instance ;
	}
	
	public static ReportNameEntry lookupReportNameEntry( int reportId ) throws CorrException
	{
		return ReportNameManager.getInstance().getReportNameMapCache().get(reportId);
	}
	
	public static final String GetReportNameMap = " select * from " + TableName + 
													" where " + ReportId + "=? ";
	
	public static final String GetCompleteReportNameMap = " select * from " + TableName ; 
	
	public static ArrayList<ReportNameEntry> lookupCompleteReportNameMap() throws CorrException
	{
		return getCompleteReportNameMapFromDB();
	}
	
	private static ArrayList<ReportNameEntry> getCompleteReportNameMapFromDB() throws CorrException
	{
		Connection con = null;
		try
		{
			con = DataSourcePool.getConnection();
			return getCompleteReportNameMapFromDB(con);
		} catch (SQLException e) {
			Utility.LOG.warn(FAILED_TO_RETRIEVE );
			throw new CorrException(FAILED_TO_RETRIEVE );
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
	
	private static ArrayList<ReportNameEntry> getCompleteReportNameMapFromDB(Connection con) throws CorrException, SQLException {
		if( null == con )
			throw new CorrException("The connection object passed was null.");
		
		ArrayList<ReportNameEntry> rnes = new ArrayList<ReportNameEntry>();
		PreparedStatement ps = con.prepareStatement(GetCompleteReportNameMap);
		
		ResultSet rs = ps.executeQuery();
		if( null != rs )
		{
			while( rs.next() )
			{
				ReportNameEntry rne = corrGeneric.com.tbitsGlobal.server.util.Utility.createReportNameEntryFromResultSet(rs);
				rnes.add(rne);
			}
		}
		
		if( rnes.size() != 0 )
			return rnes ;
		
		return null;
	}

	public static ReportNameEntry getReportNameMapFromDB(Integer t) throws CorrException
	{
		Connection con = null;
		try
		{
			con = DataSourcePool.getConnection();
			return getReportNameMapFromDB(con, t);
		} catch (SQLException e) {
			Utility.LOG.warn(FAILED_TO_RETRIEVE + " report_id to report_name map for report Id = " + t);
			throw new CorrException(FAILED_TO_RETRIEVE + " report_id to report_name map for report Id = " + t);
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
	
	public static ReportNameEntry getReportNameMapFromDB(Connection con, Integer reportId) throws CorrException, SQLException
	{
		if( null == con )
			throw new CorrException("The connection object passed was null.");
		
		PreparedStatement ps = con.prepareStatement(GetReportNameMap);
		ps.setInt(1, reportId);
		
		ResultSet rs = ps.executeQuery();
		if( null != rs )
		{
			if( rs.next() )
			{
				return corrGeneric.com.tbitsGlobal.server.util.Utility.createReportNameEntryFromResultSet(rs);
			}
		}
		
		return null;
	}

	@Override
	public void refresh() throws CorrException {
		initialize();
	}
	
	public int persistEntry(ReportNameEntry entry) throws CorrException {
		if (null == entry)
			throw new CorrException("ReportNameEntry was null.");

		if (entry.getId() == -1) {
			return insertEntry(entry);
		} else {
			return updateEntry(entry);
		}
	}
	
	public int insertEntry( ReportNameEntry entry ) throws CorrException
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
	public static ArrayList<ReportNameEntry> getReportParamMapId() throws SQLException
	{ 
		Connection con= null;
		 ArrayList<ReportNameEntry> mapReprtId = new ArrayList<ReportNameEntry>();
		
		 try {
	            con = DataSourcePool.getConnection();
		
		PreparedStatement ps = con.prepareStatement("select * from corr_report_name_map");
		
		ResultSet rs = ps.executeQuery();
		
		if( null != rs )
		{
			while( rs.next())
			{
				ReportNameEntry rne =corrGeneric.com.tbitsGlobal.server.util.Utility.createReportNameEntryFromResultSet(rs);
				mapReprtId.add(rne);
			} 
			if(rs !=null)
                rs.close();
            }
            if(ps !=null)
           ps.close();
		 } catch (SQLException sqle) {
            StringBuilder message = new StringBuilder();

            message.append("An exception occurred while retrieving the report_id.");

        } finally {
            try {
                if (con != null) {
                    con.close();
                }
            } catch (SQLException sqle) {
                LOG.info("Exception while closing the connection");
            }
        }
		
		if( mapReprtId.size() == 0)
			return null;
		
		return mapReprtId;
	}
	
	private int insertEntry(Connection con, ReportNameEntry entry) throws CorrException 
	{
		try
		{
			if( null == entry )
				throw new CorrException("Provided ReportNameEntry was null.");
	
			if( null == con || con.isClosed() == true )
				throw new CorrException("Connection was either null or closed.");
		
			String sql = "insert into " + TableName + " values (?,?)" ;

			PreparedStatement ps = con.prepareStatement(sql);
			ps.setInt(1, entry.getReportId());
			ps.setString(2, entry.getReportFileName());
			
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

	public int updateEntry( ReportNameEntry entry ) throws CorrException
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
	
	private int updateEntry(Connection con, ReportNameEntry entry) throws CorrException 
	{
		try {
			if( null == entry )
				throw new CorrException("Provided ReportNameEntry was null.");

			if( null == con || con.isClosed() == true )
				throw new CorrException("Connection was either null or closed.");
		
			String sql = "update " + TableName + " set " + ReportId + " = ?, " + ReportFileName + " = ?" + " where " +  Id + "= ?";
		
			PreparedStatement ps = con.prepareStatement(sql);
			ps.setInt(1, entry.getReportId());
			ps.setString(2, entry.getReportFileName());
			ps.setLong(3, entry.getId());
			
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

	public int deleteEntry( ReportNameEntry entry ) throws CorrException
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
	
	public int deleteEntry( Connection con, ReportNameEntry entry ) throws CorrException
	{
		try {
			
			if( null == entry )
				throw new CorrException("Provided ReportNameEntry was null.");

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
		PropertyEntry cap = PropertyManager.lookupProperty(PropReportNameMapCacheSize);
		PropertyEntry winSize = PropertyManager.lookupProperty(PropReportNameMapCacheWindowSize);
		
		if( null == cap || null == winSize )
		{
			throw new CorrException("The property : " + PropReportNameMapCacheSize + " Or " + PropReportNameMapCacheWindowSize + " not found. Their values must be integers.");
		}
		
		Integer capacity = null;
		Integer windowSize = null;
		try
		{
			capacity = Integer.parseInt(cap.getValue());
			windowSize = Integer.parseInt(winSize.getValue());
		}
		catch(NumberFormatException nfe)
		{
			throw new CorrException("Propeties " + cap + " OR " + winSize + " do not have an Integer value.");
		}
		
		reportNameMapCache = new ReportNameMapCache(capacity, windowSize);
	}
}
