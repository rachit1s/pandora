package corrGeneric.com.tbitsGlobal.server.managers;

import static corrGeneric.com.tbitsGlobal.shared.domain.PropertyEntry.*;
import static corrGeneric.com.tbitsGlobal.shared.objects.GenericParams.*;
import static corrGeneric.com.tbitsGlobal.server.util.Utility.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import corrGeneric.com.tbitsGlobal.server.cache.PropertyCache;
import corrGeneric.com.tbitsGlobal.server.util.Utility;
import corrGeneric.com.tbitsGlobal.shared.domain.PropertyEntry;
import corrGeneric.com.tbitsGlobal.shared.objects.CorrException;

import transbit.tbits.common.DataSourcePool;
import transbit.tbits.common.TBitsLogger;

public class PropertyManager extends AbstractManager
{
	private static PropertyManager instance = null;
	
	private PropertyCache pc = null; 
	
	public PropertyCache getPropertyCache() {
		return pc;
	}

	private PropertyManager() throws CorrException
	{
		initialize();
		
		ManagerRegistry.getInstance().registerManager(PropertyManager.class, this);
	}
	
	public synchronized static PropertyManager getInstance() throws CorrException
	{
		if( null == instance )
			instance = new PropertyManager();
		
		return instance;
	}
	
	public static final String GetFromDB = "select * from " + TableName +
											" where " + PropertyName + "=?";
	public static final String GetAllFromDB = "select * from " + TableName ;
	public static PropertyEntry lookupProperty(String propertyName) throws CorrException
	{
		return PropertyManager.getInstance().getPropertyCache().get(propertyName);
	}
	
	public static ArrayList<PropertyEntry> lookupAllProperties() throws CorrException
	{
		return getAllPropertiesFromDB();
	}
	
	private static ArrayList<PropertyEntry> getAllPropertiesFromDB() throws CorrException
	{
		ArrayList<PropertyEntry> props = new ArrayList<PropertyEntry>();
		
		Connection con = null;
		try
		{
			con = DataSourcePool.getConnection();
			PreparedStatement ps = con.prepareStatement(GetAllFromDB);
			ResultSet rs = ps.executeQuery();
			while(rs.next())
			{
				PropertyEntry pe = Utility.createPropertyEntryFromResultSet(rs);
				props.add(pe);
			}
			
			return props;
		}
		catch(Exception e)
		{
			e.printStackTrace();
			throw new CorrException("Exception occured while getting all properties from db.", e);
		}
	}
	
	public static PropertyEntry getFromDB(String propertyName) throws CorrException
	{
		PropertyEntry cp = null;
		Connection con = null;
		try
		{
			con = DataSourcePool.getConnection();
			if( null != con )
			{
				PreparedStatement ps = con.prepareStatement(GetFromDB);
				ps.setString(1, propertyName);
				
				ResultSet rs = ps.executeQuery();
				if( null != rs && rs.next())
				{
					cp = corrGeneric.com.tbitsGlobal.server.util.Utility.createPropertyEntryFromResultSet(rs);
				}
			}
			else
			{
				throw new CorrException(FAILED_CON);
			}
		} catch (SQLException e) 
		{
			Utility.LOG.severe(TBitsLogger.getStackTrace(e));
			throw new CorrException(FAILED_TO_RETRIEVE + " property with name " + propertyName, e);
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
		
		return cp;
	}

	@Override
	public void refresh() throws CorrException 
	{
		initialize();
	}
	
	public int persistEntry( PropertyEntry entry ) throws CorrException
	{
		if( null == entry )
			throw new CorrException("The PropertyEntry was null.");
		
		if( entry.getId() == -1 )
		{
			return insertEntry(entry);
		}
		else 
		{
			return updateEntry(entry);
		}
	}
	
	public int insertEntry( PropertyEntry entry ) throws CorrException
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
	
	private int insertEntry(Connection con, PropertyEntry entry) throws CorrException 
	{
		try
		{
			if( null == entry )
				throw new CorrException("Provided PropertyEntry was null.");
	
			if( null == con || con.isClosed() == true )
				throw new CorrException("Connection was either null or closed.");
		
			String sql = "insert into " + TableName + " values (?,?,?)" ;

			PreparedStatement ps = con.prepareStatement(sql);
			ps.setString(1, entry.getName());
			ps.setString(2, entry.getValue());
			ps.setString(3, entry.getDescription());
			
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

	public int updateEntry( PropertyEntry entry ) throws CorrException
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
	
	private int updateEntry(Connection con, PropertyEntry entry) throws CorrException 
	{
		try {
			if( null == entry )
				throw new CorrException("Provided PropertyEntry was null.");

			if( null == con || con.isClosed() == true )
				throw new CorrException("Connection was either null or closed.");
		
			String sql = "update " + TableName + " set " + PropertyName + " = ?, " + PropertyValue + " = ? , " + PropertyDescription + " = ?" + " where " +  Id + "= ?";
		
			PreparedStatement ps = con.prepareStatement(sql);
			ps.setString(1, entry.getName());
			ps.setString(2, entry.getValue());
			ps.setString(3, entry.getDescription());
			
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

	public int deleteEntry( PropertyEntry entry ) throws CorrException
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
	
	public int deleteEntry( Connection con, PropertyEntry entry ) throws CorrException
	{
		try {
			
			if( null == entry )
				throw new CorrException("Provided PropertyEntry was null.");

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
	
	public static void main(String argv[])
	{
		PropertyEntry pe = new PropertyEntry(-1, "name", null, "desc");
		System.out.println("Adding the PE : " + pe);
		try 
		{
			PropertyManager.getInstance().insertEntry(pe);
			System.out.println("Added the pe : " + pe);

		} catch (CorrException e) {
			e.printStackTrace();
		}
	}

	protected void initialize() throws CorrException 
	{
		pc = new PropertyCache();
	}
}
