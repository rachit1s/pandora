/**
 * 
 */
package transbit.tbits.webapps;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Hashtable;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import transbit.tbits.Helper.Messages;
import transbit.tbits.admin.AdminUtil;
import transbit.tbits.admin.common.AdminProxyServlet;
import transbit.tbits.admin.common.MenuItem;
import transbit.tbits.admin.common.NavMenu;
import transbit.tbits.admin.common.URLRegistry;
import transbit.tbits.common.DTagReplacer;
import transbit.tbits.common.DatabaseException;
import transbit.tbits.common.PropertiesHandler;
import transbit.tbits.config.SysConfig;
import transbit.tbits.domain.BusinessArea;
import transbit.tbits.domain.Field;
import transbit.tbits.domain.RoleUser;
import transbit.tbits.domain.User;
import transbit.tbits.exception.TBitsException;

/**
 * @author Lokesh
 *
 */
public class AdminReports extends HttpServlet {
	 static
	    {
	    	 //urls
	        String url = "reports";
	    	String completeURL = url + ".admin";
	    	
	        //Create Mapping
			URLRegistry.getInstance().addMapping(AdminProxyServlet.class, url, AdminReports.class);
			
			//Create Menu
			NavMenu nav = NavMenu.getInstance();
			nav.AppMenu.add(new MenuItem("Reports", completeURL, "The administration (Add/Delete/Update) of fields of the Business Area."));
	    }
	private static final long serialVersionUID = 1L;
	private static final int        USERS      = 4;
	private static final String WIZARD_HTML = "web/tbits-admin-reports.htm";
	public void doPost(HttpServletRequest aRequest, HttpServletResponse aResponse) throws ServletException, IOException 
	{
		HttpSession aSession = aRequest.getSession(true);
		try {
            handleRequest(aRequest, aResponse);
        } catch (DatabaseException de) {
            aResponse.sendRedirect(WebUtil.getServletPath(aRequest, "/error"));

            return;
        } catch (TBitsException tbe) {        	
        	aSession.setAttribute("ExceptionObject", tbe);
            aResponse.sendRedirect(WebUtil.getServletPath(aRequest, "/error"));
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
		aResponse.setContentType("text/html");
		User user = WebUtil.validateUser(aRequest);
		int userId = user.getUserId();
		
//		Hashtable<String, Object> paramTable = AdminUtil.getRequestParams(aRequest, user.getWebConfigObject(), USERS);
//        BusinessArea ba = (BusinessArea) paramTable.get(Field.BUSINESS_AREA);
//        int systemId = ba.getSystemId();
		
        // Check Basic Permissions to come to this page
//        ArrayList<String> adminList = AdminUtil.checkBasicPermissions(systemId, userId, USERS);
        
//		String baList  = AdminUtil.getSysIdList(ba.getSystemId(), user.getUserId());
//		String typeFieldHtml = AdminUtil.getTypeFields(systemId);
		if (!RoleUser.isSuperUser(userId))
			throw new TBitsException(Messages.getMessage("ADMIN_NO_PERMISSION"));			
				
		DTagReplacer dTag = new DTagReplacer(WIZARD_HTML);
		dTag.replace("nearestPath", WebUtil.getNearestPath(aRequest, ""));
		dTag.replace("title", "TBits Admin: Reports");
		dTag.replace("baRoleMap", ReportUtil.getBARoles ());	
		dTag.replace("usersList", ReportUtil.getJSONArrayOfUsers().toString());
		dTag.replace("selectedBARoles", "[]");
//		dTag.replace("sys_ids", baList);
//		if ((adminList.contains("SUPER_ADMIN") == false) && (adminList.contains("PERMISSION_ADMIN") == false)) {
//            dTag.replace("superuser_display", "none");
//        } else {
//            dTag.replace("superuser_display", "");
//        }
		
		//Added by Lokesh to show/hide transmittal tab based on the transmittal property in app-properties
		String trnProperty = PropertiesHandler.getAppProperties().getProperty("transbit.tbits.transmittal");
		if ((trnProperty == null) || Boolean.parseBoolean(trnProperty) == false)
			dTag.replace("trn_display", "none");
		else
			dTag.replace("trn_display", "");
		
		// Get sys_config
//        SysConfig sc = ba.getSysConfigObject();
		
		dTag.replace("userLogin", user.getUserLogin());
		dTag.replace("cssFile", WebUtil.getCSSFile("tbits.css", "", false));
//		dTag.replace("baAdminList", AdminUtil.getBAAdminEmailList());
//		dTag.replace("allTypeFields", typeFieldHtml);
		aResponse.getWriter().println(dTag.parse(0));
	}
}
