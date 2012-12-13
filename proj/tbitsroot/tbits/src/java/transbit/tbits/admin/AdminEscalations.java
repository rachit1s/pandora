/**
 * 
 */
package transbit.tbits.admin;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Hashtable;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import transbit.tbits.Escalation.EscalationUtils;
import transbit.tbits.Helper.TBitsConstants;
import transbit.tbits.Helper.Messages;
import transbit.tbits.admin.common.AdminProxyServlet;
import transbit.tbits.admin.common.MenuItem;
import transbit.tbits.admin.common.NavMenu;
import transbit.tbits.admin.common.URLRegistry;
import transbit.tbits.common.DTagReplacer;
import transbit.tbits.common.DatabaseException;
import transbit.tbits.common.PropertiesHandler;
import transbit.tbits.common.TBitsLogger;
import transbit.tbits.config.SysConfig;
import transbit.tbits.config.WebConfig;
import transbit.tbits.domain.BusinessArea;
import transbit.tbits.domain.Field;
import transbit.tbits.domain.User;
import transbit.tbits.exception.TBitsException;
import transbit.tbits.webapps.ReportUtil;
import transbit.tbits.webapps.WebUtil;

/**
 * @author Lokesh
 *
 */
public class AdminEscalations extends HttpServlet {	
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	// Logger that logs information/error messages to the Application Log.
    public static final TBitsLogger LOG        = TBitsLogger.getLogger(TBitsConstants.PKG_ADMIN);
	private static final String EMPTY_STRING = "";
	private static final String ESCALATIONS_HTML = "web/tbits-admin-escalations.htm";
	private static final String TBITS_ADMIN_ESCALATION_HIERARCHY_HTM = "web/tbits-admin-escalation-hierarchy.htm";
	private static final int        USERS      = 4;
	
	static
	{
		 //urls
        String url = "escalations";
    	String completeURL = url + ".admin";
    	
        //Create Mapping
		URLRegistry.getInstance().addMapping(AdminProxyServlet.class, url, AdminEscalations.class);
		
		//Create Menu
		NavMenu nav = NavMenu.getInstance();
		nav.BAMenu.add(new MenuItem("Escalations", completeURL, "Manage the escalation."));
		
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
    	DTagReplacer dTag = new DTagReplacer(ESCALATIONS_HTML);
    	User user = WebUtil.validateUser(aRequest);    	
    	WebConfig userConfig = user.getWebConfigObject();
        
        Hashtable<String, Object> paramTable = AdminUtil.getRequestParams(aRequest, userConfig, USERS);
        BusinessArea ba = (BusinessArea) paramTable.get(Field.BUSINESS_AREA);

        if (ba == null) {
            throw new TBitsException(Messages.getMessage("INVALID_BUSINESS_AREA"));
        }
	
        int userId = user.getUserId();
        int systemId  = ba.getSystemId();
        String sysPrefix = ba.getSystemPrefix();
        SysConfig sc = ba.getSysConfigObject();        
        String baList = AdminUtil.getSysIdList(systemId, userId);
        
        ArrayList<User> activeUsers = User.getActiveUsers();
        User.setSortParams(2, 0);
        activeUsers = User.sort(activeUsers);
        String activeUserHtml = AdminAllUsers.getBAUserHtml(activeUsers);
        
        String strUserId = aRequest.getParameter("user_id");
        int selectedUserId = -1;
        if(strUserId == null){
            //throw new TBitsException(Messages.getMessage("INVALID_TRANSBIT_USER"));
        	selectedUserId = activeUsers.get(0).getUserId();        	
        }
        else
        	selectedUserId = Integer.parseInt(strUserId);        
        
        User selectedUser = User.lookupByUserId(selectedUserId);
        if(selectedUser == null)
            throw new TBitsException(Messages.getMessage("INVALID_TRANSBIT_USER"));
        
    	dTag.replace("nearestPath", WebUtil.getNearestPath(aRequest, EMPTY_STRING));
    	dTag.replace("cssFile", WebUtil.getCSSFile(sc.getWebStylesheet(), sysPrefix, false));
    	dTag.replace("title", "TBits Admin: " + ba.getDisplayName() + " Escalations");
    	dTag.replace("sysPrefix", ba.getSystemPrefix());
    	dTag.replace("sys_ids", baList);
    	dTag.replace("userLogin", user.getUserLogin());
    	dTag.replace("user_ids", activeUserHtml);
    	dTag.replace("divReplacement", getUserHierarchy(systemId, selectedUserId));
    	dTag.replace("usersList", ReportUtil.getJSONArrayOfUsers().toString());
    	dTag.replace("submit_disabled", EMPTY_STRING);
    	
    	//Added by Lokesh to show/hide transmittal tab based on the transmittal property in app-properties
		String trnProperty = PropertiesHandler.getAppProperties().getProperty("transbit.tbits.transmittal");
		if ((trnProperty == null) || Boolean.parseBoolean(trnProperty) == false)
			dTag.replace("trn_display", "none");
		else
			dTag.replace("trn_display", "");
    	
    	out.println(dTag.parse(0));		
	}
    
    private String getUserHierarchy(int aSystemId, int aUserId) throws FileNotFoundException, IOException, TBitsException{    	
    	DTagReplacer tempTag = new DTagReplacer(TBITS_ADMIN_ESCALATION_HIERARCHY_HTM);
    	/*ArrayList<String> userList = EscalationUtils.getParentUsers(aSystemId, aUserId);
    	tempTag.replace("parent_ids", getUserHTML(userList));*/
    	ArrayList<String>userList = EscalationUtils.getChildUsers(aSystemId, aUserId);    	
    	tempTag.replace("child_ids", EscalationUtils.getUserHTML(userList));    	
    	return tempTag.parse(aSystemId);
    }    
    
    public static void main(String[] args){
    	try {
			System.out.println("Child users: " + EscalationUtils.getChildUsers(6, 50049).toString());
		} catch (TBitsException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}   	
    }
}
