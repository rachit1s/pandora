/**
 * 
 */
package transbit.tbits.addons;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import transbit.tbits.common.DataSourcePool;
import transbit.tbits.exception.PersistenceException;

/**
 * @author Nitiraj Singh Rathore ( nitiraj.r@tbitsglobal.com )
 CREATE TABLE addon_info 
           (
				jar_id bigint IDENTITY (1,1) NOT NULL,
				jar_name varchar(255) NOT NULL,
				status int NOT NULL,
				addon_name varchar(255),
				addon_description varchar(3999),
				addon_author varchar(255),
				jar_bytes varbinary(MAX) NOT NULL,
				PRIMARY KEY (jar_id),
				UNIQUE (addon_name)
            )
 */
public class AddonInfoManager
{
	private static AddonInfoManager instance = null;
	private AddonInfoManager()
	{
	}

	public synchronized static AddonInfoManager getInstance()
	{
		if( null == instance )
			instance = new AddonInfoManager();
		return instance;
	}
	
	private static Logger logger = Logger.getLogger("com.tbitsglobal.addon");
	private static final String TableAddonInfo = "addon_info";
	private static final String ColumnJarId = "jar_id";
	private static final String ColumnJarName = "jar_name";
	private static final String ColumnStatus = "status";
	private static final String ColumnAddonName = "addon_name";
	private static final String ColumnAddonDescription = "addon_description";
	private static final String ColumnAddonAuthor = "addon_author";
	private static final String ColumnJarBytes = "jar_bytes";
	
	private static final String INSERT_JAR = "\n insert into " + TableAddonInfo + "( "
											+ ColumnJarName + "," + ColumnStatus + "," + ColumnJarBytes + 
											" ) values (?,?,?)"; 
	
	private static final String UPDATE_INFO = "update " + TableAddonInfo + 
			" set " + ColumnJarName + "=?, " + ColumnStatus + "=?, " + ColumnAddonName + "=?, " + ColumnAddonDescription + "=?, " + ColumnAddonAuthor + "=? " + " where " + ColumnJarId + "=?";
	
	private static final String DELETE_INFO = "delete from " + TableAddonInfo + " where " + ColumnJarId + "=?" ;
	
	private static final String SELECT_ALL = "select " + ColumnJarId + "," + ColumnJarName + "," + ColumnStatus + "," + ColumnAddonName + "," + ColumnAddonDescription + "," + ColumnAddonAuthor + " from " + TableAddonInfo + " order by " + ColumnJarId ;
	
	private static final String SELECT_ADDON_INFO_BY_ID = "select " + ColumnJarId + "," + ColumnJarName + "," + ColumnStatus + "," + ColumnAddonName + "," + ColumnAddonDescription + "," + ColumnAddonAuthor + " from " + TableAddonInfo + " where " + ColumnJarId + "=?";
	
	private static final String SELECT_ADDON_INFO_BYTES_BY_ID = "select " + ColumnJarId + "," + ColumnJarName + "," + ColumnStatus + "," + ColumnAddonName + "," + ColumnAddonDescription + "," + ColumnAddonAuthor + ", " + ColumnJarBytes  + " from " + TableAddonInfo + " where " + ColumnJarId + "=?";
	
	private static final String SELECT_ADDON_INFO_BY_NAME = "select " + ColumnJarId + "," + ColumnJarName + "," + ColumnStatus + "," + ColumnAddonName + "," + ColumnAddonDescription + "," + ColumnAddonAuthor + " from " + TableAddonInfo + " where " + ColumnJarName + "=?";
	
	public  AddonInfoWithBytes createAddonInfoWithBytes(ResultSet rs) throws SQLException
	{
		long jarId = rs.getLong(ColumnJarId);
		String jarName = rs.getString(ColumnJarName);
		int status = rs.getInt(ColumnStatus);
		byte [] jarBytes = rs.getBytes(ColumnJarBytes);
		
		return new AddonInfoWithBytes(jarId, jarName, status, jarBytes);
	}
	
	public  AddonInfo createAddonInfo(ResultSet rs) throws SQLException
	{
		long jarId = rs.getLong(ColumnJarId);
		String jarName = rs.getString(ColumnJarName);
		int status = rs.getInt(ColumnStatus) ;
		String addonName = rs.getString(ColumnAddonName);
		String addonDescription = rs.getString(ColumnAddonDescription);
		String addonAuthor = rs.getString(ColumnAddonAuthor);
		
		return new AddonInfo(jarId, jarName, status, addonName, addonDescription, addonAuthor);
	}
	
	public  List<AddonInfo> lookupAllAddonInfos() throws PersistenceException
	{
		return getAllAddonInfos();
	}
	
	/**
	 * it returns the list of AddonInfo without the byte array in sorted order of their id
	 * @return
	 * @throws PersistenceException
	 */
	public  List<AddonInfo> getAllAddonInfos() throws PersistenceException
	{
		Connection con = null;
		try
		{
			con = DataSourcePool.getConnection();
			return getAllAddonInfos(con);
		}
		catch(Exception e)
		{
			logger.log(Level.WARNING, "Exception occured while getting all the addon infos.", e);
			throw new PersistenceException("Exception occured while getting all the addon infos. \nCause : " + e.getMessage(), e);
		}
		finally 
		{
			if( null != con )
				try {
					if( !con.isClosed() )
						con.close();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		}
	}
	
	/**
	 * @param con
	 * @return
	 * @throws SQLException 
	 */
	public  List<AddonInfo> getAllAddonInfos(Connection con) throws SQLException 
	{
		ArrayList<AddonInfo> retList = new ArrayList<AddonInfo>();
		PreparedStatement ps = con.prepareStatement(SELECT_ALL);
		ResultSet rs = ps.executeQuery();
		if( null != rs )
		{
			while(rs.next())
			{
				AddonInfo ai = createAddonInfo(rs);
				retList.add(ai);
			}
			rs.close();
		}
		ps.close();
		return retList;
	}
	
	public  AddonInfoWithBytes lookupAddonInfoWithBytes(long id) throws PersistenceException
	{
		return getAddonInfoWithBytes(id);
	}
	
	public  AddonInfoWithBytes getAddonInfoWithBytes(long id) throws PersistenceException
	{
		Connection con = null;
		try
		{
			con = DataSourcePool.getConnection();
			return getAddonInfoWithBytes(id,con);
		}
		catch(Exception e)
		{
			logger.log(Level.WARNING, "Exception occured while getting the addon info with id :" + id, e);
			throw new PersistenceException("Exception occured while getting the addon info with id :" + id + ".\nCause : " + e.getMessage(), e);
		}
		finally 
		{
			if( null != con )
				try {
					if( !con.isClosed() )
						con.close();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		}
	}
	
	public  AddonInfoWithBytes getAddonInfoWithBytes(long id, Connection con) throws SQLException 
	{
		PreparedStatement ps = con.prepareStatement(SELECT_ADDON_INFO_BYTES_BY_ID);
		ps.setLong(1, id);
		ResultSet rs = ps.executeQuery();
		AddonInfoWithBytes ai = null;
		if( null != rs )
		{
			if(rs.next())
			{
				ai = createAddonInfoWithBytes(rs);
			}
			
			rs.close();
		}
		ps.close();
		return ai;
	}
	
	public  AddonInfo lookupAddonInfo(long id) throws PersistenceException
	{
		return getAddonInfo(id);
	}
	
	public  AddonInfo getAddonInfo(long id) throws PersistenceException
	{
		Connection con = null;
		try
		{
			con = DataSourcePool.getConnection();
			return getAddonInfo(id,con);
		}
		catch(Exception e)
		{
			logger.log(Level.WARNING, "Exception occured while getting the addon info with id :" + id, e);
			throw new PersistenceException("Exception occured while getting the addon info with id :" + id + ".\nCause : " + e.getMessage(), e);
		}
		finally 
		{
			if( null != con )
				try {
					if( !con.isClosed() )
						con.close();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		}
	}
	/**
	 * @param con
	 * @return
	 * @throws SQLException 
	 */
	public  AddonInfo getAddonInfo(long id, Connection con) throws SQLException 
	{
		PreparedStatement ps = con.prepareStatement(SELECT_ADDON_INFO_BY_ID);
		ps.setLong(1, id);
		ResultSet rs = ps.executeQuery();
		AddonInfo ai = null;
		if( null != rs )
		{
			if(rs.next())
			{
				ai = createAddonInfo(rs);
			}
			
			rs.close();
		}
		ps.close();
		return ai;
	}
	
	public  AddonInfo lookupAddonInfo(String addonName) throws PersistenceException
	{
		return getAddonInfoByName(addonName);
	}
	
	public  AddonInfo getAddonInfoByName(String addonName) throws PersistenceException
	{
		Connection con = null;
		try
		{
			con = DataSourcePool.getConnection();
			return getAddonInfoByName(addonName,con);
		}
		catch(Exception e)
		{
			logger.log(Level.WARNING, "Exception occured while getting the addon info with addonName :" + addonName, e);
			throw new PersistenceException("Exception occured while getting the addon info with addonName :" + addonName + ".\nCause : " + e.getMessage(), e);
		}
		finally 
		{
			if( null != con )
				try {
					if( !con.isClosed() )
						con.close();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		}
	}
	/**
	 * @param con
	 * @return
	 * @throws SQLException 
	 */
	public  AddonInfo getAddonInfoByName(String addonName, Connection con) throws SQLException 
	{
		PreparedStatement ps = con.prepareStatement(SELECT_ADDON_INFO_BY_NAME);
		ps.setString(1, addonName);
		ResultSet rs = ps.executeQuery();
		AddonInfo ai = null;
		if( null != rs )
		{
			if(rs.next())
			{
				ai = createAddonInfo(rs);
			}
			
			rs.close();
		}
		ps.close();
		return ai;
	}
	
	public  AddonInfo persistAddonInfo(AddonInfo addonInfo) throws PersistenceException
	{
		if( null == addonInfo )
			throw new PersistenceException("The object provided was null.");
		
		Connection con =  null;
		try
		{
			con =  DataSourcePool.getConnection();
			return persist(addonInfo,con);
		}
		catch(Exception e)
		{
			logger.log(Level.WARNING,"Exception occured while inserting AddonInfo : ",e);
			throw new PersistenceException( e);
		}
		finally
		{
			if( null != con )
			{
				try {
					if( !con.isClosed() )
						try {
							con.close();
						} catch (SQLException e1) {
							e1.printStackTrace();
						}
				} catch (SQLException e1) {
					e1.printStackTrace();
				}
			}
		}
	}

	public  AddonInfo persistAddonInfoWithBytes(AddonInfoWithBytes addonInfo) throws PersistenceException
	{
		if( null == addonInfo )
			throw new PersistenceException("The object provided was null.");
		
		Connection con =  null;
		try
		{
			con =  DataSourcePool.getConnection();
			return persist(addonInfo,con);
		}
		catch(Exception e)
		{
			logger.log(Level.WARNING,"Exception occured while inserting AddonInfo : ",e);
			throw new PersistenceException( e);
		}
		finally
		{
			if( null != con )
			{
				try {
					if( !con.isClosed() )
						try {
							con.close();
						} catch (SQLException e1) {
							e1.printStackTrace();
						}
				} catch (SQLException e1) {
					e1.printStackTrace();
				}
			}
		}
	}

	/**
	 * @param addonInfo
	 * @param con
	 * @return
	 * @throws PersistenceException 
	 */
	public  AddonInfo persist(AddonInfo addonInfo, Connection con) throws PersistenceException 
	{
		if( addonInfo.getJarId() < 1 )
			throw new PersistenceException("Cannot update AddonInfo without proper id.");
		try {
			return update(addonInfo,con);
		} catch (SQLException e) {
			e.printStackTrace();
			throw new PersistenceException("Update AddonInfo failed.", e);
		}
	}
	
	public  AddonInfo persist(AddonInfoWithBytes addonInfo, Connection con) throws PersistenceException
	{
		if( addonInfo.getJarId() >= 1 )
			throw new PersistenceException("Cannot update AddonInfo with jar Bytes.");
		try {
			return insert(addonInfo, con);
		} catch (SQLException e) {
			e.printStackTrace();
			throw new PersistenceException("Exception occured while persisting entity : " + addonInfo, e);
		}
	}
	/**
	 * @param addonInfo
	 * @param con
	 * @throws SQLException 
	 * @throws PersistenceException 
	 */
	private  AddonInfo update(AddonInfo addonInfo, Connection con) throws SQLException, PersistenceException 
	{
		PreparedStatement ps = con.prepareStatement(UPDATE_INFO);
		ps.setString(1,addonInfo.getJarName());
		ps.setInt(2, addonInfo.getStatus() );
		ps.setString(3, addonInfo.getAddonName());
		ps.setString(4,addonInfo.getAddonDescription());
		ps.setString(5, addonInfo.getAddonAuthor());
		ps.setLong(6, addonInfo.getJarId());
		
		// see : http://stackoverflow.com/questions/1915166/how-to-get-the-insert-id-in-jdbc
		int affectedRows = ps.executeUpdate();
        if (affectedRows == 0) {
            throw new PersistenceException("Updating AddonInfo failed, no rows affected.");
        }

        ps.close();
		return addonInfo;
	}

	/**
	 * @param addonInfo
	 * @param con
	 * @throws PersistenceException 
	 * @throws SQLException 
	 */
	private  AddonInfo insert(AddonInfoWithBytes addonInfo, Connection con) throws PersistenceException, SQLException {
		int [] colIndexes = new int[1];
		colIndexes[0]=1;
		// http://msdn.microsoft.com/en-us/library/ms378445%28SQL.90%29.aspx
		
		PreparedStatement ps = con.prepareStatement(INSERT_JAR,colIndexes );
		ps.setString(1,addonInfo.getJarName());
		ps.setInt(2, addonInfo.getStatus() );
		ps.setBytes(3, addonInfo.getJarBytes());
		
		ResultSet generatedKeys = null;

		// see : http://stackoverflow.com/questions/1915166/how-to-get-the-insert-id-in-jdbc
		int affectedRows = ps.executeUpdate();
        if (affectedRows == 0) {
            throw new PersistenceException("Creating AddonInfo failed, no rows affected.");
        }

        generatedKeys = ps.getGeneratedKeys();
        if (generatedKeys.next()) {
            addonInfo.setJarId(generatedKeys.getLong(1));
        } else {
            throw new PersistenceException("Creating AddonInfo failed, no generated key obtained.");
        }

        return new AddonInfo(addonInfo.getJarId(),addonInfo.getJarName(),addonInfo.getStatus());
	}
	
	public  void delete(AddonInfo addonInfo)
	{
		Connection con = null;
		try
		{
			con = DataSourcePool.getConnection();
			if( null != con )
			{
				delete(addonInfo, con);
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			if( null != con )
				try {
					if( !con.isClosed() )
						con.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
		}
	}

	/**
	 * @param addonInfo
	 * @param con
	 * @throws SQLException 
	 * @throws PersistenceException 
	 */
	public  void delete(AddonInfo addonInfo, Connection con) throws SQLException, PersistenceException 
	{
		PreparedStatement ps = con.prepareStatement(DELETE_INFO);
		ps.setLong(1, addonInfo.getJarId());
		
		// see : http://stackoverflow.com/questions/1915166/how-to-get-the-insert-id-in-jdbc
		int affectedRows = ps.executeUpdate();
        if (affectedRows == 0) {
            throw new PersistenceException("Deleting AddonInfo failed, no rows affected.");
        }
	}
}
