/**
 * 
 */
package transbit.tbits.events;

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
 
 CREATE TABLE dbo.event_registry
(
event_id bigint IDENTITY (1,1) NOT NULL,
source_id varchar(255) NOT NULL,
event_class varchar(255) NOT NULL,
event_handler_class varchar(255) NOT NULL,
is_enabled bit NOT NULL,
event_order int NOT NULL,
description varchar(1023),
PRIMARY KEY (event_id),
UNIQUE (event_class, event_handler_class)
)
 
 */
public class EventRegistryManager 
{
	Logger logger = Logger.getLogger("com.tbitsglobal.event");

	private static EventRegistryManager instance = null;
	private EventRegistryManager()
	{
	}
	
	public synchronized static EventRegistryManager getInstance()
	{
		if( null == instance )
			instance = new EventRegistryManager();
		
		return instance;
	}
	
	public static final String TableEventRegistry = "event_registry";
	public static final String ColumnEventId = "event_id";
	public static final String ColumnSourceId = "source_id";
	public static final String ColumnEventClass = "event_class";
	public static final String ColumnEventHandlerClass = "event_handler_class";
	public static final String ColumnIsEnabled = "is_enabled";
	public static final String ColumnEventOrder = "event_order";
	public static final String ColumnDescription = "description";
	
	public static final String SELECT_ALL = "select * from " + TableEventRegistry + " order by " + ColumnEventOrder;
	public static final String SELECT_BY_ID = "select * from " + TableEventRegistry + " where " + ColumnEventId + "=? order by " + ColumnEventOrder ;
	public static final String SELECT_BY_SOURCEID = "select * from " + TableEventRegistry + " where " + ColumnSourceId + "=? order by " + ColumnEventOrder  ;
	
	public static final String INSERT = "insert into " + TableEventRegistry + " (" + ColumnSourceId + "," + ColumnEventClass + "," 
											+ ColumnEventHandlerClass + "," +  ColumnIsEnabled + "," + ColumnEventOrder + "," + ColumnDescription  + ")" 
											+ " values(?,?,?,?,?,?)" ;
	
	public static final String UPDATE = "update " + TableEventRegistry + " set " + ColumnSourceId + "=?, " + ColumnEventClass + "=? ," 
			+ ColumnEventHandlerClass + "=?, " + ColumnIsEnabled + "=?, " + ColumnEventOrder + "=?, " + ColumnDescription + "=? "  
			+ " where " + ColumnEventId + " =?" ;

	public static final String DELETE = "delete from " + TableEventRegistry 
										+ " where " + ColumnEventId + " =?" ;
	
	public EventRegistry createFromResultSet(ResultSet rs) throws SQLException
	{
		long eventId = rs.getLong(ColumnEventId);
		String sourceId = rs.getString(ColumnSourceId);
		String eventClass = rs.getString(ColumnEventClass);
		String eventHandlerClass = rs.getString(ColumnEventHandlerClass);
		boolean isEnabled = rs.getBoolean(ColumnIsEnabled);
		int order = rs.getInt(ColumnEventOrder);
		String description = rs.getString(ColumnDescription);
		
		return new EventRegistry(eventId, sourceId, eventClass, eventHandlerClass, isEnabled, order,description);
	}
	
	public List<EventRegistry> lookupAllEventRegistry() throws PersistenceException
	{
		return getAllEventRegistry();
	}
	
	public List<EventRegistry> getAllEventRegistry() throws PersistenceException
	{
		Connection con = null;
		try
		{
			con = DataSourcePool.getConnection();
			return getAllEventRegistry(con);
		}
		catch(Exception e)
		{
			logger.log(Level.WARNING, "Exception occured while getting all the event registries", e);
			throw new PersistenceException("Exception occured while getting all the event registries", e);
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
	public List<EventRegistry> getAllEventRegistry(Connection con) throws SQLException 
	{
		List<EventRegistry> list = new ArrayList<EventRegistry>();
		PreparedStatement ps = con.prepareStatement(SELECT_ALL);
		ResultSet rs = ps.executeQuery();
		if( null != rs )
			while( rs.next() )
			{
				EventRegistry er = createFromResultSet(rs);
				list.add(er);
			}
		rs.close();
		ps.close();
		
		return list;
	}

	public EventRegistry lookupEventRegistryById(long id) throws PersistenceException
	{
		return getEventRegistryById(id);
	}
	
	public EventRegistry getEventRegistryById(long id) throws PersistenceException
	{
		Connection con = null;
		try
		{
			con = DataSourcePool.getConnection();
			return getEventRegistryById(id,con);
		}
		catch(Exception e)
		{
			logger.log(Level.WARNING, "Exception occured while getting the event registry with id : " + id, e);
			throw new PersistenceException("Exception occured while getting the event registry with id : " + id, e);
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
	 * @param id
	 * @param con
	 * @return
	 * @throws SQLException 
	 */
	public EventRegistry getEventRegistryById(long id, Connection con) throws SQLException 
	{
		EventRegistry er = null;
		PreparedStatement ps = con.prepareStatement(SELECT_BY_ID);
		ps.setLong(1, id);
		ResultSet rs = ps.executeQuery();
		if( null != rs )
			if( rs.next() )
			{
				er = createFromResultSet(rs);
			}
		rs.close();
		ps.close();
		
		return er;
	}

	public List<EventRegistry> lookupAllEventRegistryBySourceId(String sourceId) throws PersistenceException
	{
		return getAllEventRegistryBySourceId(sourceId);
	}
	
	public List<EventRegistry> getAllEventRegistryBySourceId(String sourceId) throws PersistenceException
	{
		Connection con = null;
		try
		{
			con = DataSourcePool.getConnection();
			return getAllEventRegistryBySourceId(sourceId,con);
		}
		catch(Exception e)
		{
			logger.log(Level.WARNING, "Exception occured while getting all the event registries for sourceId " + sourceId, e);
			throw new PersistenceException("Exception occured while getting all the event registries for sourceId " + sourceId, e);
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
	 * @param ar
	 * @param con
	 * @return
	 * @throws SQLException 
	 */
	public List<EventRegistry> getAllEventRegistryBySourceId(String sourceId,
			Connection con) throws SQLException {
		List<EventRegistry> list = new ArrayList<EventRegistry>();
		PreparedStatement ps = con.prepareStatement(SELECT_BY_SOURCEID);
		ps.setString(1, sourceId);
		ResultSet rs = ps.executeQuery();
		if( null != rs )
			while( rs.next() )
			{
				EventRegistry er = createFromResultSet(rs);
				list.add(er);
			}
		rs.close();
		ps.close();
		
		return list;
	}
	
	public EventRegistry persist(EventRegistry er) throws PersistenceException
	{
		if( null == er )
			throw new PersistenceException("The provided EventRegistry was null.");
		
		if( er.getEventId() < 1 )
			return insert(er);
		else
			return update(er);
	}
	
	public EventRegistry persist(EventRegistry er, Connection con) throws PersistenceException, SQLException
	{
		if( null == er )
			throw new PersistenceException("The provided EventRegistry was null.");
		
		if( er.getEventId() < 1 )
			return insert(er,con);
		else
			return update(er,con);
	}

	/**
	 * @param er
	 * @param con
	 * @return
	 * @throws SQLException 
	 * @throws PersistenceException 
	 */
	private EventRegistry insert(EventRegistry er, Connection con) throws SQLException, PersistenceException 
	{
		int genCols[] = new int[1];
		genCols[0] = 1;
		PreparedStatement ps = con.prepareStatement(INSERT,genCols);
		ps.setString(1, er.getSourceId());
		ps.setString(2, er.getEventClass());
		ps.setString(3, er.getEventHandlerClass());
		ps.setBoolean(4, er.isEnabled());
		ps.setInt(5, er.getEventOrder());
		ps.setString(6, er.getDescription());
		
		ResultSet generatedKeys = null;

		// see : http://stackoverflow.com/questions/1915166/how-to-get-the-insert-id-in-jdbc
		int affectedRows = ps.executeUpdate();
        if (affectedRows == 0) {
            throw new PersistenceException("Creating EventRegistry failed, no rows affected.");
        }

        generatedKeys = ps.getGeneratedKeys();
        if (generatedKeys.next()) {
            er.setEventId(generatedKeys.getLong(genCols[0]));
        } else {
            throw new PersistenceException("Creating EventRegistry failed, no generated key obtained.");
        }
		
		return er;
		
	}	

	public EventRegistry insert(EventRegistry er) throws PersistenceException
	{
		Connection con = null;
		try
		{
			con = DataSourcePool.getConnection();
			return insert(er,con);
		}
		catch(Exception e)
		{
			logger.log(Level.WARNING, "Exception occured while inserting event registry : " + er, e);
			throw new PersistenceException("Exception occured while inserting the event registry : " + er, e);
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
	 * @param er
	 * @param con
	 * @return
	 */
	public EventRegistry update(EventRegistry er) throws PersistenceException
	{
		Connection con = null;
		try
		{
			con = DataSourcePool.getConnection();
			return update(er,con);
		}
		catch(Exception e)
		{
			logger.log(Level.WARNING, "Exception occured while updating event registry : " + er, e);
			throw new PersistenceException("Exception occured while update the event registry : " + er, e);
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
	 * @param er
	 * @param con
	 * @return
	 * @throws SQLException 
	 * @throws PersistenceException 
	 */
	private EventRegistry update(EventRegistry er, Connection con) throws SQLException, PersistenceException 
	{
		PreparedStatement ps = con.prepareStatement(UPDATE);
		ps.setString(1, er.getSourceId());
		ps.setString(2, er.getEventClass());
		ps.setString(3, er.getEventHandlerClass());
		ps.setBoolean(4, er.isEnabled());
		ps.setInt(5, er.getEventOrder());
		ps.setString(6,er.getDescription());
		ps.setLong(7, er.getEventId());
		
		// see : http://stackoverflow.com/questions/1915166/how-to-get-the-insert-id-in-jdbc
		int affectedRows = ps.executeUpdate();
        if (affectedRows == 0) {
            throw new PersistenceException("Creating EventRegistry failed, no rows affected.");
        }

		return er;
	}
	
	public void delete(EventRegistry er) throws PersistenceException
	{
		Connection con = null;
		try
		{
			con = DataSourcePool.getConnection();
			delete(er,con);
		}
		catch(Exception e)
		{
			logger.log(Level.WARNING, "Exception occured while deleting event registry : " + er, e);
			throw new PersistenceException("Exception occured while deleting the event registry : " + er, e);
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
	 * @param er
	 * @param con
	 * @return
	 * @throws PersistenceException 
	 * @throws SQLException 
	 */
	public void delete(EventRegistry er, Connection con) throws PersistenceException, SQLException 
	{
		PreparedStatement ps = con.prepareStatement(DELETE);
		ps.setLong(1, er.getEventId());
		
		// see : http://stackoverflow.com/questions/1915166/how-to-get-the-insert-id-in-jdbc
		int affectedRows = ps.executeUpdate();
        if (affectedRows == 0) {
            throw new PersistenceException("Deleting EventRegistry failed, no rows affected.");
        }
        
        ps.close();
	}
	
}
