package transbit.tbits.authentication;

import java.io.*;
import java.sql.*;

import javax.mail.Session;
import javax.servlet.*;
import javax.servlet.http.*;
import org.apache.commons.lang.RandomStringUtils;

import com.ibm.icu.util.Calendar;

import transbit.tbits.common.DTagReplacer;
import transbit.tbits.common.DataSourcePool;
import transbit.tbits.common.DatabaseException;
import transbit.tbits.webapps.WebUtil;

import transbit.tbits.common.Mail;

/**
 * this servlet is used to reset users passwords if user forget password.
 * 
 * @author paritosh
 * 
 */
public class PasswordRecovery extends HttpServlet {

	private static final long serialVersionUID = 1L;
	private static final String HTML_FILE = "web/login-password-notification.htm";

	private static final String HTML_MAIL_FILE = "web/login-password-mail-notification.htm";

	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		// doPost(request, response);
	}

	/**
	 * Handles the HTTP <code>POST</code> method.
	 * 
	 * @param request
	 *            servlet request
	 * @param response
	 *            servlet response
	 */
	protected void doPost(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		response.setContentType("text/html;charset=UTF-8");
		PrintWriter out = response.getWriter();

		DTagReplacer dt = new DTagReplacer(HTML_FILE);
		dt.replace("nearestPath", WebUtil.getNearestPath(request, ""));
		String email = request.getParameter("email");
		if (email.length() == 0) {
			request.getRequestDispatcher("/error");
		}

		Connection con = null;

		PreparedStatement ps = null;
		ResultSet rs = null;
		String user_login = "";
		String password = "abc123";

		password = RandomStringUtils.randomAlphanumeric(8);

		try {
			con = DataSourcePool.getConnection();

			String query = "select user_login from users where is_active = 1 and email "
					+ " in (?)";
			ps = con.prepareStatement(query);
			ps.setString(1, email);

			rs = ps.executeQuery();
			if (rs.next() == true)
				user_login = rs.getString(1);

			System.out.println("user_login for user:" + user_login);
			if (user_login.length() == 0) {

				dt.replace("statement",
						"User does not exist in tBits With Given email ID");
				out.println(dt.parse(0));
				

			}

			else {

				// generate url
				StringBuilder genUrl = new StringBuilder();

				genUrl.append(request.getRequestURL());

				int i = genUrl.lastIndexOf("/");
				int j = genUrl.length();
				genUrl.delete(i, j);
				genUrl.append("/web/reset-password.htm");

				String resetKey = null;
				resetKey = RandomStringUtils.randomAlphabetic(16);
				genUrl.append("?mailIndex=" + resetKey);

				// send mail to email users
				String toAddress = email;
				String fromAddress = "tBits@tbitsglobal.com";
				String subject = "tBits Reset Password Notification mail";

				DTagReplacer hp = new DTagReplacer(HTML_MAIL_FILE);
				hp.replace("tbitslink", genUrl.toString());

				Mail.sendWithHtml(toAddress, fromAddress, subject, hp.parse());

				// set the user_login entry into reset_password tbale in DB.
				Calendar calendar = Calendar.getInstance();

				String insertSql = "insert into reset_password values (?,?,getdate())";
				ps = con.prepareStatement(insertSql);
				ps.setString(1, user_login);
				ps.setString(2, resetKey);
				ps.execute();

				dt
						.replace("statement",
								"Your user Login & Password Reset request has been Sent in your mail ID");
				out.println(dt.parse(0));
				

			}

		} catch (SQLException e) {
			// TODO Auto-generated catch lock
			e.printStackTrace();
		} finally {
			try {
				rs.close();
				ps.close();
				con.close();
				out.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}

	}

	/**
	 * Returns a short description of the servlet.
	 */
	public String getServletInfo() {
		return "Short description";
	}

}
