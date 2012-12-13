/**
 * 
 */
package transbit.tbits.webapps;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Hashtable;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import transbit.tbits.Helper.Messages;
import transbit.tbits.admin.AdminUtil;
import transbit.tbits.common.DTagReplacer;
import transbit.tbits.common.DataSourcePool;
import transbit.tbits.common.DatabaseException;
import transbit.tbits.config.WebConfig;
import transbit.tbits.domain.BusinessArea;
import transbit.tbits.domain.Field;
import transbit.tbits.domain.Report;
import transbit.tbits.domain.Role;
import transbit.tbits.domain.RoleUser;
import transbit.tbits.domain.User;
import transbit.tbits.exception.TBitsException;

/**
 * @author Lokesh
 *
 */
public class AddReportWizard extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static final String WIZARD_HTML = "web/tbits-add-reports.htm";
	private static final String EMPTY_STRING = "";
	private static final int USERS = 4;
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
	
	public void doGet(HttpServletRequest aRequest, HttpServletResponse aResponse) throws ServletException, IOException {
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
	}
	
	public void handleRequest(HttpServletRequest aRequest, HttpServletResponse aResponse) throws ServletException, IOException, DatabaseException, TBitsException {
		aResponse.setContentType("text/html");
		
		User user = WebUtil.validateUser(aRequest);		
		if (!RoleUser.isSuperUser(user.getUserId()))
			throw new TBitsException(Messages.getMessage("ADMIN_NO_PERMISSION"));
		
		String requestType = aRequest.getParameter("requestType");
		
		if ((requestType == null) || requestType.trim().equals(""))
			aResponse.getWriter().println("Please provide appropriate value for requestType (add/edit)");
		
		if (requestType.equals("add") ){
			handleAddRequest (aRequest, aResponse);
		}
		
		if (requestType.equals("edit")){
			handleEditRequest (aRequest, aResponse);
		}		
	}	
	
	public void handleAddRequest (HttpServletRequest aRequest, HttpServletResponse aResponse) throws ServletException, IOException, DatabaseException, TBitsException{
		User                      user       = WebUtil.validateUser(aRequest);
	    /*WebConfig                 userConfig = user.getWebConfigObject();
	    int                       userId     = user.getUserId();
	    Hashtable<String, Object> paramTable = AdminUtil.getRequestParams(aRequest, userConfig, USERS);
	    BusinessArea              ba         = (BusinessArea) paramTable.get(Field.BUSINESS_AREA);
	    if (ba == null) {
            throw new TBitsException(Messages.getMessage("INVALID_BUSINESS_AREA"));
        }

        int systemId  = ba.getSystemId();
		String baList  = AdminUtil.getSysIdList(systemId, userId);*/
		
		DTagReplacer dTag = new DTagReplacer(WIZARD_HTML);
		dTag.replace("nearestPath", WebUtil.getNearestPath(aRequest, EMPTY_STRING));
		//dTag.replace("sys_ids", baList);
		dTag.replace("userLogin", user.getUserLogin());
		dTag.replace("report_id", EMPTY_STRING);
		dTag.replace("requestType", aRequest.getParameter("requestType".trim()));
		dTag.replace("report_name", EMPTY_STRING);
		dTag.replace("description", EMPTY_STRING);
		dTag.replace("file_name", EMPTY_STRING);	
		dTag.replace("browse_display", "inline");
		dTag.replace("file_name_display", "none");
		dTag.replace("revert_display", "none");
		// is enabled
		dTag.replace("is_enable", "on");
		
		dTag.replace("is_private_checked", "on");
		dTag.replace("display_selection", "none");
		dTag.replace("includeList", "");
		dTag.replace("excludeList", "");
		aResponse.getWriter().println(dTag.parse(0));
	}	
	
	public void handleEditRequest (HttpServletRequest aRequest, HttpServletResponse aResponse) throws ServletException, IOException, DatabaseException, TBitsException{
		int reportId = -1;
		Report report = null;
		DTagReplacer dTag = new DTagReplacer(WIZARD_HTML);
		PrintWriter out = aResponse.getWriter();
		
		User user = WebUtil.validateUser(aRequest);
		
		String reportIdStr = aRequest.getParameter("reportId");
		if ((reportIdStr == null) || reportIdStr.trim().equals(EMPTY_STRING))
			out.println ("Invalid reportId");
		else
			reportId = Integer.parseInt(reportIdStr);
		
		try {
			report = Report.lookupByReportId(reportId);
		} catch (DatabaseException e) {
			e.printStackTrace();
		}
		
		if (report != null){
			JSONArray resp = new JSONArray();
			resp.add(report.getReportName());
			resp.add(report.getDescription());
			resp.add(report.getFileName());
			
			resp.add(getSelectedBARoles(reportId));
			resp.add(getUserOptionList(reportId, "includeUsers"));
			resp.add(getUserOptionList(reportId, "excludeUsers"));
			
			if(report.getIsEnabled())
				resp.add("true");
			else
				resp.add("false");
			if(report.getIsPrivate())
				resp.add("true");
			else
				resp.add("false");
			resp.add(report.getGroup());
			/*dTag.replace("nearestPath", WebUtil.getNearestPath(aRequest, EMPTY_STRING));
			dTag.replace("userLogin", user.getUserLogin());
			dTag.replace("baRoleMap", ReportUtil.getBARoles ());	
			dTag.replace("usersList", ReportUtil.getJSONArrayOfUsers().toString());
			dTag.replace("report_id", report.getReportId() + EMPTY_STRING);
			dTag.replace("requestType", aRequest.getParameter("requestType".trim()));
			dTag.replace("report_name", report.getReportName());
			dTag.replace("description", report.getDescription());
			dTag.replace("file_name", report.getFileName());
			dTag.replace("file_name_display", "inline");
			dTag.replace("browse_display", "none");
			dTag.replace("revert_display", "none");
			dTag.replace("selectedBARoles", getSelectedBARoles(reportId));
			dTag.replace("includeList", getUserOptionList(reportId, "includeUsers"));
			dTag.replace("excludeList", getUserOptionList(reportId, "excludeUsers"));
			//add is enable
			if(report.getIsEnabled())
			    dTag.replace("is_enable", "checked");
			else
				dTag.replace("is_enable", " ");
			//
			if (report.getIsPrivate()){
				dTag.replace("is_private_checked", "checked");
				dTag.replace("display_selection", "inline");
			}
			else{
				dTag.replace("is_private_checked", " ");
				dTag.replace("display_selection", "none");
			}*/
			out.println(resp);
		}
	}

	public String getSelectedBARoles(int reportId) throws DatabaseException {
		JSONObject baRolesObject = new JSONObject();
		Connection connection = null;
		try {
			connection = DataSourcePool.getConnection();
			PreparedStatement ps = connection.prepareStatement("select * from report_roles where report_id = " + reportId);
			ResultSet rs = ps.executeQuery();
			if (rs != null){
				while (rs.next()){
					int systemId = rs.getInt(1);
					int roleId = rs.getInt(3);
					BusinessArea ba = BusinessArea.lookupBySystemId(systemId);
					String baName = ba.getName();
					Role role = Role.lookupBySystemIdAndRoleId(systemId, roleId);
					//TODO: Should use append/accumulate instead of the following 
					//if/else. append is perfect, but current version of JSON library
					//being used does not provide. append adds element in to object 
					//by putting the element into a JSONarray but accumulate puts its 
					//only when there are multiple elements but not for the very first 
					//element being inserted into the object
					//baRolesObject.accumulate(baName, role.getRoleName());
					Object rolesArray = baRolesObject.get(baName); 
					if (rolesArray == null){
						JSONArray tempArray = new JSONArray();
						tempArray.add(role.getRoleName());
						baRolesObject.put(baName, tempArray);
					}
					else{
						JSONArray tempArray = (JSONArray) rolesArray;
						tempArray.add(role.getRoleName());
						baRolesObject.put(baName, tempArray);
					}
				}
			}
			rs.close();
			ps.close();
//			connection.close();
			
		} catch (SQLException sqle) {
            StringBuilder message = new StringBuilder();

            message.append("An exception occured while retrieving the report for report Id: ").append(reportId).append("\n");

            throw new DatabaseException(message.toString(), sqle);
        } finally {
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException sqle) {
                	//TODO: Add logging
                    //LOG.warning("Exception occurred while closing the connection.");
                }
            }

            connection = null;
        }
        String ret = baRolesObject.toString();
		return ret;
	}

	private String getUserOptionList(int aReportId, String aUsersType) throws DatabaseException {
		StringBuffer optionsBuffer = new StringBuffer();
		Hashtable<String, ArrayList<User>> userTable = Report.getUserListByReportId(aReportId);
		if (!userTable.isEmpty()){
			ArrayList<User> usersList = userTable.get(aUsersType);
			if (!usersList.isEmpty()){
				for (User user : usersList){
					String userLogin = user.getUserLogin();
					optionsBuffer.append("<option value=\"").append(userLogin).append("\">")
					.append(userLogin).append("</option>");					
				}
				return optionsBuffer.toString();
			}
			else 
				return "";				
		}
		else
			return "";
	}
	
	public static void main(String[] args){
		AddReportWizard  arw = new AddReportWizard();
		try {
			System.out.println("Selected BA roles: \n" + arw.getSelectedBARoles(6));
		} catch (DatabaseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
}
