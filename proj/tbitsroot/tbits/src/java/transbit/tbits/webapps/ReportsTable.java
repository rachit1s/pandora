/**
 * 
 */
package transbit.tbits.webapps;

import static transbit.tbits.Helper.TBitsConstants.PKG_WEBAPPS;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import transbit.tbits.Helper.Messages;
import transbit.tbits.Helper.TBitsConstants;
import transbit.tbits.common.DataSourcePool;
import transbit.tbits.common.DatabaseException;
import transbit.tbits.common.TBitsLogger;
import transbit.tbits.domain.Report;
import transbit.tbits.domain.RoleUser;
import transbit.tbits.domain.User;
import transbit.tbits.exception.TBitsException;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

/**
 * @author Lokesh
 *
 */
public class ReportsTable extends HttpServlet {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static final TBitsLogger LOG = TBitsLogger.getLogger(PKG_WEBAPPS);
	public static final String KEY_REPORTSDIR      = "transbit.tbits.reportsdir";
	
	private JSONArray reportsArray;
	
	protected void doGet(HttpServletRequest aRequest, HttpServletResponse aResponse)
	throws ServletException, IOException {
		handleRequest (aRequest, aResponse);
	}
	
	protected void doPost(HttpServletRequest aRequest, HttpServletResponse aResponse)
		throws ServletException, IOException {
		handleRequest (aRequest, aResponse);
	}
	
	private void handleRequest (HttpServletRequest aRequest, HttpServletResponse aResponse) throws ServletException, IOException {		
		User user;		
		HttpSession aSession = aRequest.getSession(true);
		try {
			user = WebUtil.validateUser(aRequest);
			
			String view = aRequest.getParameter("reportView");
			if ((view == null) || view.trim().equals(""))
				throw new TBitsException ("Invalid reports view");
			else{
				try {
					view = view.trim();
					aResponse.setContentType("text/plain");
					PrintWriter out = aResponse.getWriter();				

					if (view.equals("admin"))
						if (RoleUser.isSuperUser(user.getUserId())){
							reportsArray = new JSONArray();
							reportsArray = getAdminReports();
							out.print(reportsArray);
						}
						else
							throw new TBitsException(Messages.getMessage("ADMIN_NO_PERMISSION"));
					else if (view.equals("user")){		
						String systemId = aRequest.getParameter("sysId");
						if ((systemId != null) || (!systemId.trim().equals("")))
							systemId = systemId.trim();
						else 
							System.out.println("Please provide proper system id");
						
						reportsArray = getUserReports(Integer.parseInt(systemId),user.getUserLogin());		
						//JSONObject reportsObject = new JSONObject();
						//reportsObject.put ("publicReports", getPublicReports());	
						//reportsObject.put ("userReports", getUserReports(user.getUserLogin()));						
						out.print(reportsArray);
					}		
				} catch (IOException e) {
					e.printStackTrace();
				} catch (NumberFormatException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}		
			}						
		} catch (DatabaseException e) {
			LOG.severe("",(e));
		} catch (TBitsException tbe) {
			aSession.setAttribute("ExceptionObject", tbe);
            aResponse.sendRedirect(WebUtil.getServletPath("/error"));
		}
	}
		
	private JSONArray getAdminReports(){
		
		
		JSONArray reportsArr = new JSONArray();
		
		JSONObject report = new JSONObject();
		Connection connection = null;

        try {
            connection = DataSourcePool.getConnection();

            PreparedStatement reportsStatement = connection.prepareStatement("select * from reports");
            ResultSet rs = reportsStatement.executeQuery();
            
            if (rs != null) {
               while (rs.next()) {
                	report.put("report_id", rs.getInt("report_id"));
                	report.put("name", rs.getString("report_name"));
                	report.put("description", rs.getString("description"));
                	report.put("filename", rs.getString("file_name"));
                	report.put("is_private", rs.getBoolean("is_private"));
                	report.put("enabled", rs.getBoolean("is_enabled"));
                	report.put("group_name", rs.getString("group_name"));
                	report.put("edit", "Edit");
                	report.put("schedule", "Schedule");
                	report.put("save", "Save");
                	reportsArr.add(report);
                }

                // Close the result set.
                rs.close();
            }

            // Close the statement.
            reportsStatement.close();

            //
            // Release the memory by nullifying the references so that these
            // are recovered by the Garbage collector.
            //
            rs = null;
            reportsStatement = null;
        } catch (SQLException sqle) {
            StringBuilder message = new StringBuilder();
            message.append("An exception occured while retrieving the existing reports").append("\n");
            try {
				throw new DatabaseException(message.toString(), sqle);
			} catch (DatabaseException e) {
				LOG.error("Exception while closing the connection:", sqle);
			}
        } finally {
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException sqle) {
                    LOG.warn("Exception while closing the connection:", sqle);
                }
                connection = null;
            }
        }        
		return (reportsArr);
	}
	
	private JSONArray getUserReports(int systemId, String aUserLogin) throws DatabaseException, SQLException{
		JSONArray userReports = new JSONArray();
		ArrayList<Report> userReportsList = Report.lookupByUserlogin(aUserLogin);
		ArrayList<Report> publicReportsList = Report.lookupPublicReports();
		getReportsList(userReports, userReportsList);
		getReportsList(userReports, publicReportsList);
		return userReports;
	}

	/**
	 * @param userReports
	 * @param reportsList
	 * @throws DatabaseException 
	 */
	private void getReportsList(JSONArray userReports,
			ArrayList<Report> reportsList) throws DatabaseException {
		
		Map<Integer, String> map = getAllReportsUrls();
		
		if (reportsList != null)
			for (Report report : reportsList){
				JSONObject reportObj = new JSONObject();
				reportObj.put("report_id", report.getReportId());
	        	reportObj.put("name", report.getReportName());
	        	reportObj.put("filename", report.getFileName());
	        	reportObj.put("description", report.getDescription());
	        	reportObj.put("Group", report.getGroup());
	        	String url = map.get(report.getReportId());
	        	if(url == null)
	        		url = "";
	        	reportObj.put("urlParamPart", url);
	        	reportObj.put("view", "view");
	        	userReports.add(reportObj);		 
	        	
			}
	}
	
	public static JSONArray getPublicReports () throws DatabaseException{
		JSONArray publicReports = new JSONArray();
		ArrayList<Report> reportsList = Report.lookupPublicReports();
		
	
		
		if (reportsList != null)
			for (Report report : reportsList){
				JSONObject reportObj = new JSONObject();
				reportObj.put("report_id", report.getReportId());
	        	reportObj.put("name", report.getReportName());
	        	reportObj.put("filename", report.getFileName());
	        	reportObj.put("description", report.getDescription());
	        	reportObj.put("Group", report.getGroup());
	        	reportObj.put("view", "view");
	        	
	        	publicReports.add(reportObj);			
			}
		return publicReports;
	}
	
	
	public static Map<Integer, String> getAllReportsUrls() throws DatabaseException
	{
		String url;

		HashMap<Integer, String> urlMap = new HashMap<Integer, String>();
		
		Connection connection = null;
		try {
			connection = DataSourcePool.getConnection();
			PreparedStatement ps = connection.prepareStatement("select * from report_params");

			ResultSet rs = ps.executeQuery();
			HashMap<Integer, HashMap<String, String>> params = new HashMap<Integer, HashMap<String, String>>();


			while(rs.next())
			{
				int reportId = rs.getInt("report_id");
				HashMap<String, String> reportParams = params.get(new Integer(reportId));
				if(reportParams == null)
					reportParams = new HashMap<String, String>();
				reportParams.put(rs.getString("param_name"),rs.getString("param_value"));
				params.put(reportId, reportParams);
			}

			//form urls from map
			for(int repoId:params.keySet())
			{
				HashMap<String, String> reportParams = params.get(repoId);
				StringBuilder urlSb = new StringBuilder();
				for(String key:reportParams.keySet())
				{
					String val = reportParams.get(key);
					urlSb.append("&");
					try {
						urlSb.append(URLEncoder.encode(key,"UTF-8"))
						.append("=")
						.append(URLEncoder.encode(val,"UTF-8"));
					} catch (UnsupportedEncodingException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				urlMap.put(repoId, urlSb.toString());
			}
			rs.close();
			ps.close();
			rs = null;
			ps = null;
		} catch (SQLException sqle) {
			StringBuilder message = new StringBuilder();

			message.append("An exception occured while retrieving the reports \n");

			throw new DatabaseException(message.toString(), sqle);
		} finally {
			if (connection != null) {
				try {
					connection.close();
				} catch (SQLException sqle) {
					LOG.warning("Exception occurred while closing the connection.");
				}
			}

			connection = null;
		}
		System.out.println(urlMap);
		return urlMap;

	}
	
	public static void main(String[] args) throws DatabaseException{
		ReportsTable rt = new ReportsTable();
		try {
			System.out.println("User reports: \n" + rt.getUserReports(6, "root").toString());
		} catch (SQLException e) {
			e.printStackTrace();
		}
		//System.out.println(ReportsTable.getPublicReports());
	}	
}
