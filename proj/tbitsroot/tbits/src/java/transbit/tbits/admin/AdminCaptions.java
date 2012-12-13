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
public class AdminCaptions extends HttpServlet {
	
	public static final String CAPTION_TABLE_NAME = "captions_properties";
	public static final int    CAPTIONS = 6;
	
	public static final String CAPTION_COL_NAME = "name";
	public static final String CAPTION_COL_VALUE = "value";
	public static final String CAPTION_COL_SYS_ID = "sys_id";
	public static final String CAPTION_WEB_INTERFACE = "web/tbits-admin-captions.html";
	
	
	HashMap<String,String> captions; 
	static
	{
		//urls
        String url = "captions";
    	String completeURL = url + ".admin";
    	
        //Create Mapping
		URLRegistry.getInstance().addMapping(AdminProxyServlet.class, url, AdminCaptions.class);
		
		//Create Menu
		NavMenu nav = NavMenu.getInstance();
		nav.BAMenu.add(new MenuItem("Captions", completeURL, "The administration (Add/Delete/Update) of fields of the Business Area."));
		
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
			
			WebConfig   userConfig = user.getWebConfigObject();
			
			Hashtable<String, Object> paramTable = AdminUtil.getRequestParams(aRequest, userConfig, CAPTIONS);
	        BusinessArea              ba         = (BusinessArea) paramTable.get(Field.BUSINESS_AREA);
	       
	        if (ba == null) {
	            throw new TBitsException(Messages.getMessage("INVALID_BUSINESS_AREA"));
	        }
		
	        int       systemId  = ba.getSystemId();
	        String    sysPrefix = ba.getSystemPrefix();
	        SysConfig sc        = ba.getSysConfigObject();
	        
	        // Check Basic Permissions to come to this page
	        ArrayList<String> adminList = AdminUtil.checkBasicPermissions(systemId, userId, CAPTIONS);

	        // Get BusinessArea List in which the user has permissions to view
	        // the admin page.
	        String          baList  = AdminUtil.getSysIdList(systemId, userId);
			
			String action = aRequest.getParameter("action");
			if((action != null) && (action.equals("save-captions") == true)) {
				try {
					insertCaptions(systemId, aRequest, aResponse);
					System.out.println("Now reloading the captions");
					CaptionsProps.reloadCaptions();
				}catch(SQLException sq) {
					sq.printStackTrace();
					System.err.println("SQL Exception occured: could not store the values in database");
				}
			}
			String relPath = aRequest.getServletPath();
			if(aRequest.getPathInfo() != null)
				relPath += aRequest.getPathInfo();
			if(aRequest.getQueryString() != null)
				relPath += "?" + aRequest.getQueryString(); 
				
			String path = WebUtil.getServletPath(aRequest, relPath);
			
			PrintWriter pw = aResponse.getWriter();
			DTagReplacer hp = new DTagReplacer(CAPTION_WEB_INTERFACE);
			hp.replace("title", "TBits Admin: " + ba.getDisplayName() + " Captions");
			hp.replace("userLogin",userLogin);
			hp.replace("cssFile",WebUtil.getCSSFile(sc.getWebStylesheet(), sysPrefix, false));
			hp.replace("sys_ids",baList);
			hp.replace("nearestPath", WebUtil.getNearestPath(aRequest, ""));
			hp.replace("captionsList",loadCaptions(systemId));
			hp.replace("target",path);
			
			String display_logout = "none";
			if(aRequest.getAuthType() == AuthConstants.AUTH_TYPE)
				display_logout = "";
			hp.replace("display_logout", display_logout);
			
			//Added by Lokesh to show/hide transmittal tab based on the transmittal property in app-properties
			String trnProperty = PropertiesHandler.getAppProperties().getProperty("transbit.tbits.transmittal");
			if ((trnProperty == null) || Boolean.parseBoolean(trnProperty) == false)
				hp.replace("trn_display", "none");
			else
				hp.replace("trn_display", "");

	        if ((adminList.contains("SUPER_ADMIN") == false) && (adminList.contains("PERMISSION_ADMIN") == false)) {
	            hp.replace("superuser_display", "none");
	        } else {
	            hp.replace("superuser_display", "");
	        }
	        
			pw.print(hp.parse(0));
			pw.close();
			pw.flush();
	 			
	}

	
	public void doGet(HttpServletRequest aRequest, 
			HttpServletResponse aResponse) throws ServletException, FileNotFoundException, IOException {
		doPost(aRequest, aResponse);
	}
	
	private String loadCaptions(int systemId) {
		StringBuilder innerHtml = new StringBuilder();
		captions = CaptionsProps.getInstance().getCaptionsHashMap(systemId);
		for(String str:captions.keySet()) {
			innerHtml.append("<tr><td class=\"sx\">" + str + "</td><td align=\"left\" class=\"sx\"><input type=\"text\" name=\"" + str + 
					"\" value=\"" + captions.get(str) + "\"></input></td></tr>");
			}
		return innerHtml.toString();
	}

	private void insertCaptions(int systemId,HttpServletRequest aRequest,
								HttpServletResponse aResponse) throws SQLException{
		
		HashMap<String,String> oldCaptions = CaptionsProps.getInstance().getCaptionsHashMap(0);
		System.out.println("Attempting to insert the captions into database");
		Connection conn = null;
		try {
			conn = DataSourcePool.getConnection();
			PreparedStatement ps = null;

			ps = conn.prepareStatement("DELETE FROM " + CAPTION_TABLE_NAME
					+ " WHERE " + CAPTION_COL_SYS_ID + " = " + systemId);
			if (null != ps) {
				ps.execute();
				ps.close();
			}

			for (String str : oldCaptions.keySet()) {
				String value = aRequest.getParameter(str);
				System.out.println(str + ":" + value + ":"
						+ oldCaptions.get(str));

				if ((null == value) || (value.trim().equals(""))
						|| (value.trim().equals(oldCaptions.get(str))))
					continue;
				ps = conn.prepareStatement("INSERT INTO " + CAPTION_TABLE_NAME
						+ "(" + CAPTION_COL_NAME + "," + CAPTION_COL_VALUE
						+ "," + CAPTION_COL_SYS_ID + ") VALUES('" + str + "','"
						+ value + "'," + systemId + ")");
				if (null != ps) {
					ps.execute();
					ps.close();
				}
			}
		} catch (SQLException e) {
			System.out.println("Unable to load captions.");
			e.printStackTrace();
			throw e;
		} finally {
			if(conn != null)
			{
				try {
					conn.close();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}

}