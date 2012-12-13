package transbit.tbits.upgrade;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

import transbit.tbits.authentication.AuthUtils;
import transbit.tbits.common.DatabaseException;
import transbit.tbits.exception.TBitsException;

public class Base64PasswdEncode implements IUpgrade{

	public boolean upgrade(Connection conn, String folder, String sysType)
			throws SQLException, DatabaseException, TBitsException {
		Statement stmt = conn.createStatement();
		ResultSet rs = stmt.executeQuery("select * from user_passwords");
		Map<String, String> loginEncPasswd = new HashMap<String, String>();
		while (rs.next()) {
			String login = rs.getString("user_login");
			String pass = rs.getString("password");

			if ((login == null) || (pass == null))
				continue;

			String encPassowrd = AuthUtils.encrypt(pass);
			loginEncPasswd.put(login, encPassowrd);
		}
		rs.close();
		stmt.close();
		
		PreparedStatement ps = conn
				.prepareStatement("update user_passwords set password = ? where user_login = ?");
		for (String login : loginEncPasswd.keySet()) {
			String pass = loginEncPasswd.get(login);
			ps.setString(1, pass);
			ps.setString(2, login);
			ps.executeUpdate();
			System.out.println("Updated: " + login + ":" + pass);
		}
		System.out.println("Finished updating the logins.");
		ps.close();
		
		return true;
	}
}
