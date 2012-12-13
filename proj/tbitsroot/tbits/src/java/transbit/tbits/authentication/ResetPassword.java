package transbit.tbits.authentication;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.activation.DataSource;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jfree.util.Log;

import transbit.tbits.common.DTagReplacer;
import transbit.tbits.common.DataSourcePool;
import transbit.tbits.common.DatabaseException;
import transbit.tbits.events.EventFailureException;
import transbit.tbits.webapps.WebUtil;

/**
 * this servlet is used to reset users passwords if user forget password.
 * 
 * @author paritosh.
 * 
 */

public class ResetPassword extends HttpServlet {

	private static final String HTML_FILE = "web/login-password-notification.htm";

	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		// doPost(request, response);
	}

	protected void doPost(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {

		response.setContentType("text/html");
		PrintWriter out = response.getWriter();
		String password = request.getParameter("pass");
		String conPassword = request.getParameter("confirmPass");
		String reset_key = request.getParameter("resetKey");

		DTagReplacer dt = new DTagReplacer(HTML_FILE);
		dt.replace("nearestPath", WebUtil.getNearestPath(request, ""));

		if (reset_key.isEmpty()) {
			dt
					.replace(
							"statement",
							"Your Reset Password key has been null plase contact tBits Admin or Send Reset password request again. ");
			out.println(dt.parse(0));
			out.close();
		} else {

			Connection con = null;
			PreparedStatement ps = null;
			ResultSet rs = null;
			String user_login = "";

			String query = "select user_login from reset_password where reset_key "
					+ "in (?)";

			try {
				con = DataSourcePool.getConnection();
				ps = con.prepareStatement(query);
				ps.setString(1, reset_key);
				rs = ps.executeQuery();
				if (rs.next() == true)
					user_login = rs.getString(1);
				if (user_login.isEmpty()) {
					dt
							.replace("statement",
									"Your Reset Password request has been expired please send it again tBits DMS");
					out.println(dt.parse(0));
					
				}

				else {
					try {
						AuthUtils.setPassword(user_login, password);
					} catch (DatabaseException e) {
						e.printStackTrace();
						dt.replace("statement",
								"An Exception occured while updating the password. Cause:" +e.getMessage());
						out.println(dt.parse(0));
						return;
					}
					catch (EventFailureException e) {
						e.printStackTrace();
						dt.replace("statement",
								"An Exception occured while updating the password. Cause:" +e.getMessage());
						out.println(dt.parse(0));
						return;
					}
					// deleting the user password request from reset_password
					// table
					
					String deleteQuery = "delete from reset_password where reset_key "
							+ "in (?)";
					ps = con.prepareStatement(deleteQuery);
					ps.setString(1, reset_key);
					ps.execute();

					// show the output page on submit

					dt.replace("statement",
							"Your Password has been Updated in tBits DMS");
					out.println(dt.parse(0));
					
				}

			} catch (SQLException e) {
				// TODO Auto-generated catch block
					dt.replace("statement","Exception occurred while reset the password Please contact administrator.");
					out.println(dt.parse(0));
					
				    Log.info("Exception occurred while reset the password ");
			} finally {
				try {
					ps.close();
					rs.close();
					con.close();
					out.close();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}

		}
	}

}
