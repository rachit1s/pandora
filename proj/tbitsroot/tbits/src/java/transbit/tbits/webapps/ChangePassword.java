package transbit.tbits.webapps;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import transbit.tbits.authentication.AuthUtils;
import transbit.tbits.common.DatabaseException;
import transbit.tbits.events.EventFailureException;

public class ChangePassword extends HttpServlet
{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	 /**
     * The doGet method of the servlet.
     *
     * @param  aHttpRequest          the HttpServlet Request Object
     * @param  aHttpResponse         the HttpServlet Response Object
     * @throws ServeletException
     * @throws IOException
     */
    public void doPost(HttpServletRequest aHttpRequest, HttpServletResponse aHttpResponse) throws ServletException, IOException {
        doGet(aHttpRequest, aHttpResponse);
    }
    
    /**
     * The doGet method of the servlet.
     *
     * @param  aHttpRequest          the HttpServlet Request Object
     * @param  aHttpResponse         the HttpServlet Response Object
     * @throws ServeletException
     * @throws IOException
     */
    public void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
    	 PrintWriter out     = res.getWriter();
         String user = req.getRemoteUser();
         if((user == null)|| (user.length() == 0))
         {
        	 out.print("Invalid user. Please try to login again.");
        	 return;
         }
         String oldPassword = req.getParameter("oldpasswd");
         String newPassword = req.getParameter("newpasswd");
         try {
			if(!AuthUtils.validateUser(user, oldPassword))
			 {
				 out.print("Password incorrect.");
				 return;
			 }
			AuthUtils.setPassword(user, newPassword);
			out.print("Successfully changed password");
		} catch (DatabaseException e) {
			e.printStackTrace();
			out.print("Database error. Contact the system administrator.");
		} catch (EventFailureException e) {
			e.printStackTrace();
			out.print("An Exception occured while setting password. Cause : " + e.getMessage());
		}
    }
}
