package transbit.tbits.scheduler.ui;

import java.lang.reflect.Array;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import transbit.tbits.common.DataSourcePool;

public class JobUtil{
	private static Connection conn;
	
	public static ArrayList getSuperUsers() throws SQLException{
		conn = DataSourcePool.getConnection();
		String sql = "select user_login,email from super_users su join users u on su.user_id = u.user_id";
		PreparedStatement ps = conn.prepareStatement(sql);
		ResultSet rs = ps.executeQuery();
		
		ArrayList emails = new ArrayList();//[rs.getFetchSize()];
		int count = 0;
		while(rs.next()){
			emails.add(rs.getString(2));
		}
		return emails;
	}
}
