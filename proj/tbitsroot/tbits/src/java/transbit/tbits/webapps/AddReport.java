/**
 * 
 */
package transbit.tbits.webapps;

import static transbit.tbits.Helper.TBitsConstants.PKG_WEBAPPS;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/*import org.eclipse.birt.report.engine.api.EngineException;
import org.eclipse.birt.report.model.api.activity.SemanticException;
*/
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import com.oreilly.servlet.multipart.FilePart;
import com.oreilly.servlet.multipart.MultipartParser;
import com.oreilly.servlet.multipart.ParamPart;
import com.oreilly.servlet.multipart.Part;

import transbit.tbits.common.Configuration;
import transbit.tbits.common.DataSourcePool;
import transbit.tbits.common.DatabaseException;
import transbit.tbits.common.TBitsLogger;
import transbit.tbits.domain.BusinessArea;
import transbit.tbits.domain.Report;
import transbit.tbits.domain.Role;
import transbit.tbits.domain.User;
import transbit.tbits.exception.TBitsException;
//import transbit.tbits.report.TBitsReportEngine;

/**
 * @author Lokesh
 *
 */

public class AddReport extends HttpServlet {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	// Logger that logs information/error messages to the Application Log.
    private static final TBitsLogger LOG = TBitsLogger.getLogger(PKG_WEBAPPS); 
    private static final String TBITSREPORTS = "/tbitsreports";
    
	private static final String MULTIPART_CONTENT_TYPE = "multipart/form-data";
	public static String ourReportsLocation;

	protected void doGet(HttpServletRequest aRequest, HttpServletResponse aResponse)
			throws ServletException, IOException {
		//handleRequest (aRequest, aResponse);
	}

	protected void doPost(HttpServletRequest aRequest, HttpServletResponse aResponse)
			throws ServletException, IOException {
		handleRequest (aRequest, aResponse);
	}

	private void handleRequest(HttpServletRequest aRequest,
			HttpServletResponse aResponse) {
		
		String reportName = "";
		String reportDescription = ""; 
		String group = "";
		String aLogin;
		String baRoleString = "";
		String includeUsers = "";
		String excludeUsers = "";
		String requestType = "";
		boolean isChanged = false;
		boolean isPrivate = false;
		
		boolean isEnable  = false;
		int reportId=-1;
		JSONArray baRoleArray = null;
		
		String contentType = aRequest.getContentType();
		
		PrintWriter out = null;
		try {
			out = aResponse.getWriter();
			aResponse.setContentType("text/html");	
		} catch (IOException e1) {
			LOG.severe("",(e1));
		}		
		User user = null;
		
		try {
			user = WebUtil.validateUser(aRequest);
		} catch (DatabaseException e) {
			LOG.severe("",(e));
		} catch (TBitsException e) {
			LOG.severe("",(e));
		}
			
		aLogin = user.getUserLogin();
		
		if ((contentType != null) && (contentType.startsWith(MULTIPART_CONTENT_TYPE) == true)) {
            MultipartParser parser = null;

            try {
                parser = new MultipartParser(aRequest, 1024 * 1024 * 1024);    // 1GB
            } catch (IOException e) {
                LOG.severe("",(e));
            }

            if (parser != null) {
                Part part = null;
                
                String fileName = null;
                ParamPart pp = null;
                Report oldReport = null;
                // Iterate the parts in the parser and process them accordingly
                try {
					while ((part = parser.readNextPart()) != null) {
						if (part instanceof ParamPart) {
	                        pp = (ParamPart) part;
	                        String paramName  = pp.getName();
	                        String paramValue = pp.getStringValue();
	                        	                                           
	                        if (paramName.equals("report-name") && ((paramValue.trim().equals("") || (paramValue!= null))))
	                        	reportName = paramValue;
	                        if (paramName.equals("group-name") && ((paramValue.trim().equals("") || (paramValue!= null))))
	                        	group = paramValue;
	                        
	                        if (paramName.equals("report-description") && ((paramValue.trim().equals("") || (paramValue!= null))))
	                        	reportDescription = paramValue;
	                        
	                        if (paramName.equals("is_private") && (paramValue!= null)){
	                        	if (paramValue.equals("on"))
	                        		isPrivate = true;
	                        	else
	                        		isPrivate = false;
	                        }
	                        if (paramName.equals("is_enable") && (paramValue!= null)){
	                        	if (paramValue.equals("on"))
	                        		isEnable = true;
	                        	else
	                        		isEnable = false;
	                        }
	                        
	                        
	                        if (paramName.equals("selectedBARoles") && ((paramValue!=null) || (paramValue.trim().equals("")))){	                        	
	                        	baRoleString = paramValue;
	                        	baRoleArray = JSONArray.fromObject(baRoleString);    	                        	
	                        }
	                        
	                        if (paramName.equals("includeList") && ((paramValue!=null) || (paramValue.trim().equals("")))){
	                        	includeUsers = paramValue;
	                        }
	                        
	                        if (paramName.equals("excludeList") && ((paramValue!=null) || (paramValue.trim().equals("")))){
	                        	excludeUsers = paramValue;
	                        }
	                        
	                        if (paramName.equals("requestType") && ((paramValue!=null) || (paramValue.trim().equals("")))){
	                        	requestType = paramValue;
	                        }
	                        
	                        if (paramName.equals("isChanged") && ((paramValue!=null) || (!paramValue.trim().equals("")))){
	                        	isChanged = Boolean.parseBoolean(paramValue);
	                        }	    
	                        
	                        if (paramName.equals("reportId") && requestType.equals("edit") && ((paramValue!=null) || (!paramValue.trim().equals("")))){
	                        	reportId = Integer.parseInt(paramValue);
	                        }	 
						}
						
						oldReport = Report.lookupByReportId(reportId);
						
						if (part instanceof FilePart) {
							if (requestType.equals("add") || (requestType.equals("edit") && isChanged)){
								String oldFileName = "";
								if (oldReport != null)
									oldFileName = oldReport.getFileName();
									
								fileName = handleFilePart((FilePart) part, aLogin, requestType, oldFileName);
								if ((fileName != null) && !(fileName.trim().equals(""))){
							    	if (reportName.equals(""))
							    		reportName = fileName;
							    	
							    	if (reportDescription.equals(""))
							    		reportDescription = fileName;
							    }
							}
						}					    
					}
					
					if (requestType.equals("add")){
						JSONObject newRecord = addNewReport (reportName, reportDescription, fileName, isPrivate,isEnable,group);					
						if (newRecord != null){						
							int newReportId = (Integer)newRecord.get("report_id");
							insertReportRoles (baRoleArray, newReportId);
							insertReportSpecificUser(includeUsers, newReportId, true);
							insertReportSpecificUser(excludeUsers, newReportId, false);							
						}
						out.print("Uploaded the report. Please close this window...");
					}
					else if (requestType.equals("edit")){
						if (oldReport != null){		
							if ((fileName == null) || fileName.trim().equals("")){
								fileName = oldReport.getFileName();
							}							
						}
						Report tempRep = new Report(reportId, reportName, reportDescription, fileName, isPrivate, isEnable,group);
						Report.update(tempRep);
						insertReportRoles(baRoleArray, reportId);
						insertReportSpecificUser(includeUsers, reportId, true);
						insertReportSpecificUser(excludeUsers, reportId, false);
						out.print("Updated the report. Click Ok to close this window...");
					}
					else
						out.print("Could not upload the report \"" + fileName + "\"." );					
				} catch (UnsupportedEncodingException e) {
					LOG.severe("",(e));
				} catch (IOException e) {
					LOG.severe("",(e));
				} catch (DatabaseException dbe) {
					LOG.severe("",(dbe));
				}
            }
		}
	}
	
	private void insertReportSpecificUser(String users, int reportId, boolean include) throws DatabaseException{		
		Connection connection = null;
		try{
			connection = DataSourcePool.getConnection();
			connection.setAutoCommit(false);
			JSONArray userLogin = null;
			if (users != null)
				userLogin = JSONArray.fromObject(users);
			else
				return;
			
			if (userLogin.isEmpty()){
				PreparedStatement psDelete = connection.prepareStatement("DELETE FROM report_specific_users WHERE report_id=" + reportId +
						" and is_included='" + include + "'");
				psDelete.execute();
				connection.commit();
				psDelete.close();
			}
			else{				
				for (int index=0; index < userLogin.size(); index++ ){
					System.out.println("UserLogin: " + userLogin);
					User user = User.lookupByUserLogin(((String)userLogin.get(index)));
					int userId = user.getUserId();
					if (user != null){
						boolean isExisting = false;
						PreparedStatement ps = connection.prepareStatement("SELECT * FROM report_specific_users WHERE " +
								"report_id=" + reportId + " and user_id=" + userId);
						ResultSet rs = ps.executeQuery();
						if(rs.next()){
							isExisting = true;
						}
						rs.close();
						ps.close();
						
						if (isExisting){
							PreparedStatement psUpdate = connection.prepareStatement("UPDATE report_specific_users " +
									"SET is_included='" + include + "' WHERE report_id=" + reportId + " and user_id=" +
									userId);
							psUpdate.execute();
							connection.commit();
							psUpdate.close();
						}												
						else{
							CallableStatement cs = connection.prepareCall("stp_report_specific_user_insert ?, ?, ?");
							cs.setInt (1, reportId);
							cs.setInt (2, userId);
							cs.setBoolean (3, include);
							cs.execute();
							cs.close();
							connection.commit();														
							cs = null;
						}
					}
					else
						LOG.info("No user found with login: " + userLogin);
				}
			}
//	        connection.close();
		}catch (SQLException sqle) {
			try {
        		if(connection != null)
					connection.rollback();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			StringBuilder message = new StringBuilder();

			message.append("An exception occurred while inserting ").append("Report Object.").append("\nReport Id: ").append(reportId).append("\n");

			throw new DatabaseException(message.toString(), sqle);
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
	}

	private void insertReportRoles(JSONArray baRoleArray, int reportId) throws DatabaseException {
		Connection connection = null;
		try{
			connection = DataSourcePool.getConnection();	
			connection.setAutoCommit(false);
			if (baRoleArray.isEmpty() || (baRoleArray == null)){
				PreparedStatement psDeleteAll = connection.prepareStatement("DELETE FROM report_roles WHERE report_id=" + reportId);
				psDeleteAll.execute();
				connection.commit();
				psDeleteAll.close();					
			}
			else{
				BusinessArea tempBA;				
				for (int i = 0; i < baRoleArray.size(); i++){					
					JSONObject jsonBAObj = (JSONObject) baRoleArray.get(i);					
					tempBA = BusinessArea.lookupByName(jsonBAObj.getString("ba"));
					int aSystemId = tempBA.getSystemId();
					
					PreparedStatement psDelete = connection.prepareStatement ("DELETE FROM report_roles WHERE report_id=" + reportId + " and sys_id=" + aSystemId);
					psDelete.execute();
					connection.commit();
					psDelete.close();
					
					JSONArray roles = (JSONArray)jsonBAObj.get ("roles");
					//roles = roles.substring (1, (roles.length() - 1));
					for (int index=0; index < roles.size(); index++){						
						Role role = Role.lookupBySystemIdAndRoleName(aSystemId, roles.getString(index));
						int roleId = role.getRoleId();							
						PreparedStatement ps = connection.prepareStatement("SELECT * FROM report_roles WHERE " +
								" sys_id=" + aSystemId + " and report_id=" + reportId  + " and role_id=" + roleId);
						ResultSet rs = ps.executeQuery();
						if (!rs.next()){						
							CallableStatement cs = connection.prepareCall("stp_report_role_insert ?, ?, ?");
							cs.setInt (1, aSystemId);
							cs.setInt (2, reportId);
							cs.setInt (3, roleId);
							cs.execute(); 
							connection.commit();
							cs.close();			            
							cs = null;
						}
						ps.close();
						rs.close();						
					}
				}
			}
		//	connection.close();
		}catch (SQLException sqle) {
			try {
        		if(connection != null)
					connection.rollback();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			StringBuilder message = new StringBuilder();

			message.append("An exception occurred while inserting ").append("Report Object.").append("\nReport Id: ").append(reportId).append("\n");

			throw new DatabaseException(message.toString(), sqle);
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
	}

	private JSONObject addNewReport(String reportName, String reportDescription, String fileName, boolean isPrivate,boolean isEnable,String group) throws DatabaseException{
		int reportId = -1;
		
		//boolean isEnabled = false;
		JSONObject newReport = new JSONObject();
		Report tempReport = new Report (reportId, reportName, reportDescription, fileName, isPrivate, isEnable,group);
		reportId = Report.insert(tempReport);		
            
		if (reportId != -1){
			newReport.put("report_id", reportId);
			newReport.put("name", reportName);
			newReport.put("group", group);
			newReport.put("description", reportDescription);
			newReport.put("fileName", fileName);
			newReport.put("is_private", isPrivate + "");
			newReport.put("enabled", isEnable + "");
		}        		
		return newReport;		
	}
	
	private static String handleFilePart(FilePart fp, String aLogin, String requestType, String previousFileName) throws IOException {        
        // Get the name and the path of the file.
        String fileName = fp.getFileName();
        String filePath = fp.getFilePath();
        
        LOG.info("File Name: " + fileName);
        LOG.info("File Path: " + filePath);
        
        String storedName  = fileName.replaceAll("[^a-zA-Z0-9._]", "_");
        
        // Get the location of the temporary directory.
        try {
            ourReportsLocation = Configuration.getAppHome() + TBITSREPORTS;
        } catch (IllegalArgumentException e) {
            LOG.severe(e.toString(), e);
        }
        String prevFilePath="";
        if (!previousFileName.trim().equals(""))
        	prevFilePath = ourReportsLocation + "/" + previousFileName;
                	
        String targetPath = ourReportsLocation + "/" + storedName;
               
      // Store the file content to the target location.
        /*File targetFile = new File(targetPath);        
        String targetFileName =*/ 
        File targetFile = createFile(targetPath, prevFilePath, requestType); 
        
        if(targetFile != null){
        	fp.writeTo(targetFile);
        	
        	 /*  //Change database name and properties in the ".rptdesign" file
            TBitsReportEngine tre = new TBitsReportEngine();
            try {
    			tre.getReportDesign(targetFileName);
    		} catch (SemanticException e) {
    			// TODO Auto-generated catch block
    			e.printStackTrace();
    		} catch (EngineException e) {
    			// TODO Auto-generated catch block
    			e.printStackTrace();
    		} catch (IllegalArgumentException e) {
    			// TODO Auto-generated catch block
    			e.printStackTrace();
    		}*/
        	
        	return targetFile.getName();
        }
        else
        	throw new FileNotFoundException("Could not create .rpt file while uploading a report");        
    }	
	
	/**
	 * @param file - File type 
	 * @param requestType 
	 * @return - file name of the file created of type String 
	 */
	private static File createFile(String targetFilePath, String prevFilePath, String requestType){
		int lastIndex;
		
		if (!prevFilePath.trim().equals("")){
			File prevFile = new File(prevFilePath);
			if (requestType.trim().equals("edit") && (prevFile.exists())){
				prevFile.delete();
			}
		}
		
		File file = new File(targetFilePath);
		String orgName = file.getAbsolutePath();		
		try {
			while (!file.createNewFile()){
				lastIndex = orgName.lastIndexOf(".");
				String tempStr = orgName.substring(0, lastIndex) + "-copy" + orgName.substring(lastIndex);
				File dest = new File (tempStr); 
				if (file.renameTo(dest)){
					LOG.info("File name : " + "\"" + file.getName() + "\" has been renamed to: " + dest.getName());
					return dest;	
				}
				else {
					orgName = tempStr;
					continue;
				}
			}			
		} catch (IOException e) {
			e.printStackTrace();
		}	
		return file;		
	}	
}
