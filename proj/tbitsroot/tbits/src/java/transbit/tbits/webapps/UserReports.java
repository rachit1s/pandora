/**
 * 
 */
package transbit.tbits.webapps;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import transbit.tbits.common.DTagReplacer;
import transbit.tbits.common.DatabaseException;
import transbit.tbits.domain.BusinessArea;
import transbit.tbits.domain.User;
import transbit.tbits.exception.TBitsException;

/**
 * @author Lokesh
 *
 */
public class UserReports extends HttpServlet {

	private static final long serialVersionUID = 1L;
	private static final String WIZARD_HTML = "web/tbits-user-reports.htm";
	public void doPost(HttpServletRequest aRequest, HttpServletResponse aResponse) throws ServletException, IOException 
	{
		HttpSession aSession = aRequest.getSession(true);
		try {
            handleRequest(aRequest, aResponse);
        } catch (DatabaseException de) {
            aResponse.sendRedirect(WebUtil.getServletPath("/error"));

            return;
        } catch (TBitsException tbe) {        	
        	aSession.setAttribute("ExceptionObject", tbe);
            aResponse.sendRedirect(WebUtil.getServletPath("/error"));
            return;
        }

        return;
	}
	public void doGet(HttpServletRequest aRequest, HttpServletResponse aResponse) throws ServletException, IOException 
	{
		HttpSession aSession = aRequest.getSession(true);
		try {
            handleRequest(aRequest, aResponse);
        } catch (DatabaseException de) {
            aResponse.sendRedirect(WebUtil.getServletPath("/error"));
            return;
        } catch (TBitsException tbe) {        	
        	aSession.setAttribute("ExceptionObject", tbe);
            aResponse.sendRedirect(WebUtil.getServletPath("/error"));
            return;
        }

        return;
	}
	public void handleRequest(HttpServletRequest aRequest, HttpServletResponse aResponse) throws ServletException, IOException, DatabaseException, TBitsException 
	{
		int systemId = -1;
		aResponse.setContentType("text/html");
		User user = WebUtil.validateUser(aRequest);
		PrintWriter out = aResponse.getWriter();
		String sysPrefix = aRequest.getParameter("ba");
		if (sysPrefix != null)
			sysPrefix = sysPrefix.trim();
		else{
			out.println("Please provide proper system prefix");
			return;
		}
		
		BusinessArea ba = BusinessArea.lookupBySystemPrefix(sysPrefix);
		if (ba == null){
			out.println("Invalid business area");
			return;
		}
		else{
			systemId = ba.getSystemId();
		}
		
		DTagReplacer dTag = new DTagReplacer(WIZARD_HTML);
		dTag.replace("nearestPath", WebUtil.getNearestPath(aRequest, ""));
		dTag.replace("userId", user.getUserId() + "");		
		dTag.replace("sys_id", systemId + "");
		aResponse.getWriter().println(dTag.parse(0));
	}
}
