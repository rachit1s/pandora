package transbit.tbits.authentication;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import transbit.tbits.common.DataSourcePool;
import transbit.tbits.common.DatabaseException;
import transbit.tbits.domain.User;

public class UserAliasManager 
{
	public final static String TABLE_NAME = "user_login_alias";
	public final static String LDAPUSERLOGIN = "ldap_user_login";
	public final static String TBITSUSERLOGIN = "tbits_user_login";
	
	public final static String lookupQuery = " select " + TBITSUSERLOGIN + " from " + TABLE_NAME + " where " + LDAPUSERLOGIN + "=?";
	
	public static String getTbitsLoginForLdapLogin( String ldapUserLogin ) throws SQLException
	{
		Connection con = null; 
		try
		{
			con = DataSourcePool.getConnection();
			PreparedStatement ps = con.prepareStatement(lookupQuery);
			ps.setString(1, ldapUserLogin);
			ResultSet result = ps.executeQuery();
			String tbitsUserLogin = null ;
			if( null != result && result.next() )
			{
					tbitsUserLogin = result.getString(TBITSUSERLOGIN);
			}
			
			result.close();
			ps.close();
			
			return tbitsUserLogin ;
		}
		finally
		{
			try {
				if( null != con && con.isClosed() == false )
				{
					con.close();
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}
	
	public static User getTbitsUserForLdapLogin( String ldapUserLogin ) throws SQLException, DatabaseException
	{
		String userLogin = getTbitsLoginForLdapLogin(ldapUserLogin);
		if( null == userLogin )
			return null;
		// search only active users.
		return User.lookupByUserLogin(userLogin);
	}
}
