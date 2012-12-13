package nccCorres;

import static nccCorres.CorresConstants.* ;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import transbit.tbits.common.DataSourcePool;
import transbit.tbits.common.DatabaseException;
import transbit.tbits.common.TBitsLogger;
import transbit.tbits.domain.User;

public class CorresMapManager 
{	
	public static String USER_COLUMN = "user_id" ;
	public static String LOGGER_COLUMN = "logger_id" ;
	public static final String MAP_TABLE = "corres_map";
	
	public static String ALLOWED_LOGGERS = "select " + USER_COLUMN + ", " + LOGGER_COLUMN + " from " + MAP_TABLE 
										   + " where " + USER_COLUMN + " =?"	;
	public static ArrayList<User> getCorresAllowedLogger( User user )
	{

		ArrayList<User> loggers = new ArrayList<User>() ;
		if( null == user )
			return loggers;
		
		Connection con = null ;
		
		try
		{
			con = DataSourcePool.getConnection(); 
			PreparedStatement ps = con.prepareStatement(ALLOWED_LOGGERS) ;
			ps.setInt(1, user.getUserId());
			
			ResultSet rs = ps.executeQuery() ;
			
			if( null != rs )
			{
				while( rs.next() )
				{
					int loggerId = rs.getInt(LOGGER_COLUMN);
					User logger = null;
					try {
						logger = User.lookupAllByUserId(loggerId);
					} catch (DatabaseException e) {
						LOG.info(TBitsLogger.getStackTrace(e));
						LOG.warn("Unable to access the user with id : " + loggerId);
					}
					if( null != logger )
						loggers.add(logger);
				}
				
			}
		} catch (SQLException e) {			
			LOG.info(TBitsLogger.getStackTrace(e));
		} 
		finally
		{
			if( null != con )
				try {
					con.close() ;
				} catch (SQLException e) 
				{
					LOG.info(TBitsLogger.getStackTrace(e));
				}
		}
		
		return loggers ;
	}
}
