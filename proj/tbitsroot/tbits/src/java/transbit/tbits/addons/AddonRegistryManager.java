///**
// * 
// */
//package transbit.tbits.addons;
//
//import java.sql.Connection;
//import java.sql.PreparedStatement;
//import java.sql.ResultSet;
//import java.sql.SQLException;
//import java.util.ArrayList;
//import java.util.List;
//import java.util.logging.Level;
//import java.util.logging.Logger;
//
//import transbit.tbits.common.DataSourcePool;
//import transbit.tbits.exception.PersistenceException;
//
///**
// * @author Nitiraj Singh Rathore ( nitiraj.r@tbitsglobal.com )
// *
// */
//public class AddonRegistryManager 
//{
//	private static Logger logger = Logger.getLogger("com.tbitsglobal.addon");
//	
//	private static AddonRegistryManager instance = null;
//	private AddonRegistryManager()
//	{
//	}
//
//	public static synchronized AddonRegistryManager getInstance()
//	{
//		if( null == instance )
//			instance = new AddonRegistryManager();
//		
//		return instance;
//	}
//	
//	private static final String TableAddonRegistry = "addon_registry";
//	private static final String ColumnAddonId = "addon_id";
//	private static final String ColumnJarId = "jar_id";
//	private static final String ColumnStatus = "status";
//	private static final String ColumnAddonName = "addon_name";
//	private static final String ColumnAddonDescription = "addon_description";
//	private static final String ColumnAddonAuthor = "addon_author";
//	
//	private static final String SELECT_ALL = "select * from " + TableAddonRegistry + " order by " + ColumnAddonName;
//	private static final String SELECT_REGISTRY = "select * from " + TableAddonRegistry + " where " + ColumnAddonId + "=?";
//	private static final String SELECT_BY_NAME = "select * from " + TableAddonRegistry + " where " + ColumnAddonName + "=?";
//	private static final String DELETE_REGISTRY = "delete from " + TableAddonRegistry + " where " + ColumnAddonId + " =?";
//	private static final String UPDATE_REGISTRY = "update " + TableAddonRegistry + " set " + ColumnJarId + "=?, " + ColumnStatus + "=?, " + ColumnAddonName + "=?, " + ColumnAddonDescription + "=?, " + ColumnAddonAuthor + "=? where " + ColumnAddonId + "=?"; 
//	private static final String INSERT_REGISTRY = "insert into " + TableAddonRegistry + " (" + ColumnJarId + "," + ColumnStatus + "," + ColumnAddonName + "," + ColumnAddonDescription + "," + ColumnAddonAuthor + " ) values (?,?,?,?,?) ";
//	
//	/**
//	 * This is supposed to return from cache. But in the its absence it does a DB call. 
//	 * @return
//	 * @throws PersistenceException 
//	 */
//	public  List<AddonRegistry> lookupAllAddonRegistry() throws PersistenceException
//	{
//		return getAllAddonRegistry();
//	}
//	/**
//	 * @return : returns all addon registries in order of name
//	 * @throws PersistenceException 
//	 */
//	public  List<AddonRegistry> getAllAddonRegistry() throws PersistenceException
//	{
//		Connection con = null;
//		try
//		{
//			con = DataSourcePool.getConnection();
//			return getAllAddonRegistry(con);
//		}
//		catch(Exception e)
//		{
//			logger.log(Level.WARNING, "Exception occured while getting all the addon registries", e);
//			throw new PersistenceException("Exception occured while getting all the addon registries", e);
//		}
//		finally
//		{
//			if( null != con )
//				try {
//					if( !con.isClosed() )
//						con.close();
//				} catch (SQLException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}
//		}
//	}
//
//	public  AddonRegistry createAddonRegistry( ResultSet rs ) throws SQLException
//	{
//		long addonId = rs.getLong(ColumnAddonId);
//		long jarId = rs.getLong(ColumnJarId);
//		int status = rs.getInt(ColumnStatus);
//		String name = rs.getString(ColumnAddonName);
//		String description = rs.getString(ColumnAddonDescription);
//		String author = rs.getString(ColumnAddonAuthor);
//		return new AddonRegistry(addonId, jarId, status, name, description, author);
//	}
//	/**
//	 * @param con
//	 * @return
//	 * @throws SQLException 
//	 */
//	private  List<AddonRegistry> getAllAddonRegistry(Connection con) throws SQLException 
//	{
//		List<AddonRegistry> list = new ArrayList<AddonRegistry>();
//		PreparedStatement ps = con.prepareCall(SELECT_ALL);
//		ResultSet rs = ps.executeQuery();
//		if( null != rs )
//		{
//			while(rs.next())
//			{
//				AddonRegistry ar = createAddonRegistry(rs);
//				list.add(ar);
//			}
//			rs.close();
//		}
//		ps.close();
//		
//		return list;
//	}
//	
//	public  AddonRegistry lookupAddonRegistryById(long id) throws PersistenceException
//	{
//		return getAddonRegistryById(id);
//	}
//	/**
//	 * @param id
//	 * @return
//	 * @throws PersistenceException 
//	 */
//	public  AddonRegistry getAddonRegistryById(long id) throws PersistenceException 
//	{
//		Connection con = null;
//		try
//		{
//			con = DataSourcePool.getConnection();
//			return getAddonRegistryById(id,con);
//		}
//		catch(Exception e)
//		{
//			logger.log(Level.WARNING, "Exception occured while getting addon with id : " + id ,e);
//			throw new PersistenceException("Exception occured while getting addon with id : " + id ,e);
//		}
//		finally
//		{
//			if( null != con )
//				try {
//					if( !con.isClosed() )
//						con.close();
//				} catch (SQLException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}
//		}
//	}
//	/**
//	 * @param id
//	 * @param con
//	 * @return
//	 * @throws SQLException 
//	 */
//	public  AddonRegistry getAddonRegistryById(long id, Connection con) throws SQLException 
//	{
//		AddonRegistry ar = null;
//		PreparedStatement ps = con.prepareStatement(SELECT_REGISTRY);
//		ps.setLong(1, id);
//		
//		ResultSet rs = ps.executeQuery();
//		if( null != rs )
//			if( rs.next())
//				ar = createAddonRegistry(rs);
//		
//		rs.close();
//		ps.close();
//		return ar;
//	}
//	
//	public  AddonRegistry lookupAddonRegistryByName(String name) throws PersistenceException
//	{
//		return getAddonRegistryByName(name);
//	}
//	/**
//	 * @param id
//	 * @return
//	 * @throws PersistenceException 
//	 */
//	public  AddonRegistry getAddonRegistryByName(String name) throws PersistenceException 
//	{
//		Connection con = null;
//		try
//		{
//			con = DataSourcePool.getConnection();
//			return getAddonRegistryByName(name,con);
//		}
//		catch(Exception e)
//		{
//			logger.log(Level.WARNING, "Exception occured while getting addon with name : " + name ,e);
//			throw new PersistenceException("Exception occured while getting addon with name : " + name ,e);
//		}
//		finally
//		{
//			if( null != con )
//				try {
//					if( !con.isClosed() )
//						con.close();
//				} catch (SQLException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}
//		}
//	}
//	/**
//	 * @param id
//	 * @param con
//	 * @return
//	 * @throws SQLException 
//	 */
//	public  AddonRegistry getAddonRegistryByName(String name, Connection con) throws SQLException 
//	{
//		AddonRegistry ar = null;
//		PreparedStatement ps = con.prepareStatement(SELECT_BY_NAME);
//		ps.setString(1,name);
//		
//		ResultSet rs = ps.executeQuery();
//		if( null != rs )
//			if( rs.next())
//				ar = createAddonRegistry(rs);
//		
//		rs.close();
//		ps.close();
//		return ar;
//	}
//	
//	public  AddonRegistry persist(AddonRegistry ar) throws PersistenceException
//	{
//		if( ar.getAddonId() < 1 )
//			return insert(ar);
//		else
//			return update(ar);
//	}
//	
//	/**
//	 * @param ar
//	 * @return
//	 * @throws PersistenceException 
//	 */
//	private  AddonRegistry insert(AddonRegistry ar) throws PersistenceException 
//	{
//		Connection con = null;
//		try
//		{
//			con = DataSourcePool.getConnection();
//			return insert(ar,con);
//		}
//		catch(Exception e)
//		{
//			logger.log(Level.WARNING,"Exception occured while inserting the addon registry : " + ar, e);
//			throw new PersistenceException("Exception occured while inserting the addon registry : " + ar, e);
//		}
//		finally
//		{
//			if( null != con )
//				try {
//					if( !con.isClosed())
//						con.close();
//				} catch (SQLException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}
//		}
//	}
//	/**
//	 * @param ar
//	 * @param con
//	 * @return
//	 * @throws SQLException 
//	 * @throws PersistenceException 
//	 */
//	private  AddonRegistry insert(AddonRegistry ar, Connection con) throws SQLException, PersistenceException 
//	{
//		String[] identity = new String[1];
//		identity[0] = ColumnAddonId;
//		PreparedStatement ps = con.prepareStatement(INSERT_REGISTRY	,identity);
//		ps.setLong(1, ar.getJarId());
//		ps.setInt(2, ar.getStatus());
//		ps.setString(3, ar.getAddonName());
//		ps.setString(4, ar.getAddonDescription());
//		ps.setString(5, ar.getAddonAuthor());
//		
//		ResultSet generatedKeys = null;
//
//		// see : http://stackoverflow.com/questions/1915166/how-to-get-the-insert-id-in-jdbc
//		int affectedRows = ps.executeUpdate();
//        if (affectedRows == 0) {
//            throw new PersistenceException("Creating AddonRegistry failed, no rows affected.");
//        }
//
//        generatedKeys = ps.getGeneratedKeys();
//        if (generatedKeys.next()) {
//            ar.setAddonId(generatedKeys.getLong(ColumnAddonId));
//        } else {
//            throw new PersistenceException("Creating AddonRegistry failed, no generated key obtained.");
//        }
//		
//		return ar;
//	}
//	
//	public  AddonRegistry persist(AddonRegistry ar, Connection con) throws SQLException, PersistenceException
//	{
//		if( ar.getAddonId() < 1 )
//			return insert(ar, con);
//		else
//			return update(ar, con);
//	}
//	
//	/**
//	 * @param ar
//	 * @return
//	 * @throws PersistenceException 
//	 */
//	public  AddonRegistry update(AddonRegistry ar) throws PersistenceException 
//	{
//		Connection con = null;
//		try
//		{
//			con = DataSourcePool.getConnection();
//			return update(ar,con);
//		}
//		catch(Exception e)
//		{
//			logger.log(Level.WARNING,"Exception occured while updating the addon registry : " + ar, e);
//			throw new PersistenceException("Exception occured while updating the addon registry : " + ar, e);
//		}
//		finally
//		{
//			if( null != con )
//				try {
//					if( !con.isClosed())
//						con.close();
//				} catch (SQLException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}
//		}
//	}
//	/**
//	 * @param ar
//	 * @param con
//	 * @return
//	 * @throws SQLException 
//	 * @throws PersistenceException 
//	 */
//	public AddonRegistry update(AddonRegistry ar, Connection con) throws SQLException, PersistenceException 
//	{
//		PreparedStatement ps = con.prepareStatement(UPDATE_REGISTRY);
//		ps.setLong(1, ar.getJarId());
//		ps.setInt(2, ar.getStatus());
//		ps.setString(3, ar.getAddonName());
//		ps.setString(4, ar.getAddonDescription());
//		ps.setString(5, ar.getAddonAuthor());
//		ps.setLong(6, ar.getAddonId());
//		
//		// see : http://stackoverflow.com/questions/1915166/how-to-get-the-insert-id-in-jdbc
//		int affectedRows = ps.executeUpdate();
//        if (affectedRows == 0) {
//            throw new PersistenceException("Updating AddonRegistry failed, no rows affected.");
//        }
//        
//        return ar;
//	}
//	
//	public void delete(AddonRegistry ar) throws PersistenceException
//	{
//		Connection con = null;
//		try
//		{
//			con = DataSourcePool.getConnection();
//			delete(ar,con);
//		}
//		catch(Exception e)
//		{
//			logger.log(Level.WARNING,"Exception occured while deleting the addon registry : " + ar, e);
//			throw new PersistenceException("Exception occured while deleting the addon registry : " + ar, e);
//		}
//		finally
//		{
//			if( null != con )
//				try {
//					if( !con.isClosed())
//						con.close();
//				} catch (SQLException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}
//		}
//	}
//	/**
//	 * @param ar
//	 * @param con
//	 * @return
//	 * @throws SQLException 
//	 * @throws PersistenceException 
//	 */
//	private void delete(AddonRegistry ar, Connection con) throws SQLException, PersistenceException {
//		PreparedStatement ps = con.prepareStatement(DELETE_REGISTRY);
//		ps.setLong(1, ar.getAddonId());
//		
//		// see : http://stackoverflow.com/questions/1915166/how-to-get-the-insert-id-in-jdbc
//		int affectedRows = ps.executeUpdate();
//		ps.close();
//        if (affectedRows == 0) {
//            throw new PersistenceException("Deleting AddonRegistry failed, no rows affected.");
//        }
//	}
//}
