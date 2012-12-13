/**
 * 
 */
package transbit.tbits.admin;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import transbit.tbits.Helper.TBitsConstants;
import transbit.tbits.admin.common.AdminProxyServlet;
import transbit.tbits.admin.common.MenuItem;
import transbit.tbits.admin.common.NavMenu;
import transbit.tbits.admin.common.URLRegistry;
import transbit.tbits.common.DTagReplacer;
import transbit.tbits.common.DatabaseException;
import transbit.tbits.common.TBitsLogger;
import transbit.tbits.domain.User;
import transbit.tbits.exception.TBitsException;
import transbit.tbits.webapps.WebUtil;

/**
 * @author Lokesh
 *
 */
public class AdminMailingLists extends HttpServlet {
	
	/**
	 * 
	 */	
	private static final long serialVersionUID = 1L;
	// Logger that logs information/error messages to the Application Log.
    public static final TBitsLogger LOG        = TBitsLogger.getLogger(TBitsConstants.PKG_ADMIN);
	private static final String EMPTY_STRING = "";
	private static final String TBITS_ADMIN_MAILING_LISTS_HTM = "web/tbits-admin-mailing-lists.htm";
	
	static
    {
    	 //urls
        String url = "mailinglists";
    	String completeURL = url + ".admin";
    	
        //Create Mapping
		URLRegistry.getInstance().addMapping(AdminProxyServlet.class, url, AdminMailingLists.class);
		
		//Create Menu
		NavMenu nav = NavMenu.getInstance();
		nav.AppMenu.add(new MenuItem("Mailing Lists", completeURL, "The administration of mailing lists."));
    }
	
	 /**
     * This method services the HTTP-Get request to this servlet.
     * Basically, it does display of the page ready for user to start filling
     * it and submit.
     *
     * @param  aRequest          the HttpServlet Request Object
     * @param  aResponse         the HttpServlet Response Object
     *
     * @exception ServeletException
     * @exception IOException
     */
    public void doGet(HttpServletRequest aRequest, HttpServletResponse aResponse) throws ServletException, IOException {
        
        HttpSession session = aRequest.getSession();
       try {
            handleRequest(aRequest, aResponse);
        } catch (DatabaseException de) {
            session.setAttribute("ExceptionObject", de);
            aResponse.sendRedirect(WebUtil.getServletPath(aRequest, "/error"));
            de.printStackTrace();

            return;
        } catch (TBitsException de) {
            session.setAttribute("ExceptionObject", de);
            aResponse.sendRedirect(WebUtil.getServletPath(aRequest, "/error"));
            de.printStackTrace();

            return;
        }

        return;
    }

	/**
     * The doPost method of the servlet.
     *
     * @param  aRequest          the HttpServlet Request Object
     * @param  aResponse         the HttpServlet Response Object
     *
     * @exception ServeletException
     * @exception IOException
     */
    public void doPost(HttpServletRequest aRequest, HttpServletResponse aResponse) throws ServletException, IOException {
        
        HttpSession session = aRequest.getSession();

        try {
            handleRequest(aRequest, aResponse);
        } catch (DatabaseException de) {
        	session.setAttribute("ExceptionObject", de);
            aResponse.sendRedirect(WebUtil.getServletPath(aRequest, "/error"));
            de.printStackTrace();
            return;
        } catch (TBitsException de) {
        	session.setAttribute("ExceptionObject", de);
            aResponse.sendRedirect(WebUtil.getServletPath(aRequest, "/error"));
            de.printStackTrace();
            return;
        }

        return;
    }    

    private void handleRequest(HttpServletRequest aRequest,
			HttpServletResponse aResponse) throws IOException, DatabaseException, TBitsException, ServletException {
    	
    	PrintWriter out = aResponse.getWriter();
    	User user = WebUtil.validateUser(aRequest);
    	
    	DTagReplacer dTag = new DTagReplacer(TBITS_ADMIN_MAILING_LISTS_HTM);
    	dTag.replace("nearestPath", WebUtil.getNearestPath(aRequest, EMPTY_STRING));
    	dTag.replace("title", "TBits Admin: Display Groups");
    	dTag.replace("userLogin", user.getUserLogin());
    	dTag.replace("submit_disabled", EMPTY_STRING);
    	out.println(dTag.parse(0));		
	}    
    
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
