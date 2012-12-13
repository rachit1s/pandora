package transbit.tbits.admin;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Properties;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import transbit.tbits.Helper.Messages;
import transbit.tbits.admin.AdminUtil;
import transbit.tbits.admin.common.AdminProxyServlet;
import transbit.tbits.admin.common.MenuItem;
import transbit.tbits.admin.common.NavMenu;
import transbit.tbits.admin.common.URLRegistry;
import transbit.tbits.authentication.AuthConstants;
import transbit.tbits.common.DTagReplacer;
import transbit.tbits.common.DataSourcePool;
import transbit.tbits.common.DatabaseException;
import transbit.tbits.common.PropertiesHandler;
import transbit.tbits.config.CaptionsProps;
import transbit.tbits.config.SysConfig;
import transbit.tbits.config.WebConfig;
import transbit.tbits.domain.BusinessArea;
import transbit.tbits.domain.Field;
import transbit.tbits.domain.RoleUser;
import transbit.tbits.domain.User;
import transbit.tbits.exception.TBitsException;
import transbit.tbits.webapps.WebUtil;

/*
 * @author:Abhishek Agarwal
 */

public class AdminAppProperties extends HttpServlet {
	
	public static final String APP_TABLE_NAME = "tbits_properties";
	public static final int    APPPROP = 7;
	
	public static final String PROP_WEB_INTERFACE = "web/tbits-admin-app.html";
	
	public static final String APP_COL_NAME = "name";
	public static final String APP_COL_VALUE = "value";
	
	
	Properties ourAppProperties;
	
	static
	{
		 //urls
        String url = "appproperties";
    	String completeURL = url + ".admin";
    	
        //Create Mapping
		URLRegistry.getInstance().addMapping(AdminProxyServlet.class, url, AdminAppProperties.class);
		
		//Create Menu
		NavMenu nav = NavMenu.getInstance();
		nav.AppMenu.add(new MenuItem("App Properties", completeURL, "The administration (Add/Delete/Update) of fields of the Business Area."));
		
	}
	public void doPost(HttpServletRequest aRequest, 
			HttpServletResponse aResponse) throws ServletException, FileNotFoundException, IOException {
		
		try {
            handlePostRequest(aRequest, aResponse);
        } catch (DatabaseException de) {
            aResponse.sendRedirect(WebUtil.getServletPath(aRequest, "/error"));

            return;
        } catch (TBitsException de) {
            aResponse.sendRedirect(WebUtil.getServletPath(aRequest, "/error"));

            return;
        }

        return;
		
	}
		private void handlePostRequest(HttpServletRequest aRequest,
			HttpServletResponse aResponse) throws DatabaseException, TBitsException, IOException, ServletException {
			
			User    user       = WebUtil.validateUser(aRequest);
			String userLogin = user.getUserLogin();
			int userId = user.getUserId();
			if(!RoleUser.isSuperUser(userId))
	            throw new TBitsException(Messages.getMessage("ADMIN_NO_PERMISSION"));
			
			String action = aRequest.getParameter("action");
			if((action != null) && (action.equals("save-app-props") == true)) {
				try {
					insertAppProps(aRequest,aResponse);
					PropertiesHandler.reload();
				} catch (SQLException e) {
					e.printStackTrace();
					System.err.println("SQL Exception occured: " +
							"could not store the App Properties in database");
				}
			}
			
			String path = aRequest.getServletPath();
			
			PrintWriter pw = aResponse.getWriter();
			DTagReplacer hp = new DTagReplacer(PROP_WEB_INTERFACE);
			hp.replace("title", "TBits Admin: Application Properties");
			hp.replace("userLogin",userLogin);
			hp.replace("cssFile",WebUtil.getCSSFile("tbits.css", "", false));
			hp.replace("nearestPath", aRequest.getContextPath() + "/");
			hp.replace("appProperties",loadAppProperties());
			hp.replace("target",path);
			
			String display_logout = "none";
			if(aRequest.getAuthType() == AuthConstants.AUTH_TYPE)
				display_logout = "";
			hp.replace("display_logout", display_logout);

	        String trnProperty = PropertiesHandler.getAppProperties().getProperty("transbit.tbits.transmittal");
	        if ((trnProperty == null) || Boolean.parseBoolean(trnProperty) == false)
	        	hp.replace("trn_display", "none");
	        else
	        	hp.replace("trn_display", "");
	        
			pw.print(hp.parse(0));
			pw.close();
			pw.flush();
	 			
	}

	
	private void insertAppProps(HttpServletRequest aRequest,
			HttpServletResponse aResponse) throws SQLException{
		Properties oldAppProperties = PropertiesHandler.getAppProperties();
		
		Connection conn = null;
		try {
			conn = DataSourcePool.getConnection();
			PreparedStatement ps = null;
			for (Object obj : oldAppProperties.keySet()) {
				String key = (String) obj;
				String oldValue = (String) oldAppProperties.get(obj);
				String value = aRequest.getParameter(key);
				if ((null == value) || (value.trim().equals(""))
						|| (value.trim().equals(oldValue)))
					continue;

				ps = conn.prepareStatement("UPDATE " + APP_TABLE_NAME + " SET "
						+ APP_COL_VALUE + " = '" + value.replaceAll("'", "''") + "' WHERE "
						+ APP_COL_NAME + " = '" + key + "'");

				if (null != ps) {
					ps.execute();
					ps.close();
				}
			}
		}
		catch(SQLException sqle)
		{
			System.out.println("Error while updating tbits.");
			throw sqle;
		}
		finally
		{
			if(conn != null)
			{
				try {
					conn.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}
	}

	public void doGet(HttpServletRequest aRequest, 
			HttpServletResponse aResponse) throws ServletException, FileNotFoundException, IOException {
		doPost(aRequest, aResponse);
	}
	
	
	private String loadAppProperties() {
		StringBuilder innerHtml = new StringBuilder();
		ourAppProperties = PropertiesHandler.getAppProperties();
		for(Object obj:ourAppProperties.keySet()) {
			String key = (String)obj;
			String value = (String)ourAppProperties.get(obj);
			innerHtml.append("<tr><td class=\"sx\">" + key + "</td><td align=\"left\" class=\"sx\"><input style=\"width: 300px\" type=\"text\" name=\"" + key + 
					"\" value=\"" + value + "\"></input></td></tr>");
			}

		return innerHtml.toString();
	}

}
