/**
 * 
 */
package transbit.tbits.webapps;

import static transbit.tbits.Helper.TBitsConstants.PKG_WEBAPPS;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.quartz.SchedulerException;

import transbit.tbits.common.Configuration;
import transbit.tbits.common.DataSourcePool;
import transbit.tbits.common.DatabaseException;
import transbit.tbits.common.TBitsLogger;
import transbit.tbits.domain.Report;
import transbit.tbits.scheduler.TBitsScheduler;

/**
 * @author Lokesh
 *
 */
public class ReportsTableModifier extends HttpServlet {
	
	private static final TBitsLogger LOG = TBitsLogger.getLogger(PKG_WEBAPPS);
	private static final int JOB_NAME = 1;
	private static final int JOB_GROUP = 2;
	
	public void doPost(HttpServletRequest aRequest, HttpServletResponse aResponse) throws ServletException, IOException 
	{
		handleRequest(aRequest, aResponse);        
	}
	public void doGet(HttpServletRequest aRequest, HttpServletResponse aResponse) throws ServletException, IOException 
	{
		handleRequest(aRequest, aResponse);      
	}
	
	private void handleRequest(HttpServletRequest aRequest, HttpServletResponse aResponse) throws ServletException, IOException {
		aResponse.setContentType("text/html");
		PrintWriter out = aResponse.getWriter();
		String reportTableAction = aRequest.getParameter("action");		
		if ((reportTableAction == null) || (reportTableAction.trim().equals(""))){
			out.println("Please provide the action to be taken on report table (Update/Delete).");
		}
		reportTableAction = reportTableAction.trim();
		
		String repId = aRequest.getParameter("reportId");
		if ((repId == null) || repId.trim().equals(""))
			out.println("Please provide proper report id");
		int reportId = Integer.parseInt(repId);
		
		if (reportTableAction.equals("delete")){			
			if (handleDeleteReport(reportId) == 1){				
				out.println("true");
				return;
			}
			else{
				out.println("false");
				return;
			}
		}		
		if (reportTableAction.equals("update")){
			String reportName = aRequest.getParameter ("reportName");
			if ((reportName != null) || (!reportName.trim().equals(""))){
				reportName = reportName.trim();
			}
			else {
				out.println("false");
				return;
			}
					
			String description = aRequest.getParameter ("description");
			if ((description != null)|| (!description.trim().equals("")))
				description = description.trim();
			else {
				out.println ("false");
				return;
			}
			String group = aRequest.getParameter ("group");
			if ((group != null)|| (!group.trim().equals("")))
				group = group.trim();
			else {
				out.println ("false");
				return;
			}
			
			String fileName = aRequest.getParameter ("fileName");
			if ((fileName != null) || (!fileName.trim().equals("")))
				fileName = fileName.trim();
			else{
				out.println("false");
				return;
			}
			
			boolean isPrivate = false;
			String isPrivateStr = aRequest.getParameter ("isPrivate");
			if ((isPrivateStr != null)||(!isPrivateStr.trim().equals("")))
				isPrivate = Boolean.parseBoolean(isPrivateStr.trim());
			else{
				out.println("false");
				return;
			}

			boolean isEnabled= false;
			String isEnabledStr = aRequest.getParameter ("isEnabled");
			if ((isEnabledStr !=null)||(!isEnabledStr.trim().equals("")))
				isEnabled = Boolean.parseBoolean(isEnabledStr.trim());
			else{
				out.println("false");
				return;
			}
			
			Report report = new Report(reportId, reportName, description, fileName, isPrivate, isEnabled,group);
			try {
				Report.update(report);
			} catch (DatabaseException e) {
				LOG.error("Error occurred while updating report info");
				e.printStackTrace();
				out.print("false");
				return;
			}
			out.println("true");
			return;
		}
	}
	
	public static int handleDeleteReport(int reportId){
		Connection connection = null;
		try {
			//First delete any corresponding scheduled job
			Report report = Report.lookupByReportId(reportId);
			deleteReportJob(report.getReportName());
			connection = DataSourcePool.getConnection();
			
			CallableStatement cs = connection.prepareCall("stp_report_delete ?, ?, ?");
			cs.setInt(1, reportId);
			cs.registerOutParameter(2, java.sql.Types.INTEGER);
			cs.registerOutParameter(3, java.sql.Types.VARCHAR);			
			cs.execute();
			int retVal = cs.getInt(2);
			String dFileName = cs.getString(3);
			cs.close();			
			if (retVal == 1){
				File file = new File (Configuration.getAppHome() + "/tbitsreports/" + dFileName);
				file.delete();				
			}			
			cs = null;
			return retVal;
		} catch (SQLException e) {	
			e.printStackTrace();			
		} catch (SchedulerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (DatabaseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally {
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException sqle) {
                    LOG.warn("Exception while closing the connection:", sqle);
                }
                connection = null;
            }            
        }
		return 0;
	}
	
	/**
	 * @param reportId
	 * @throws DatabaseException
	 * @throws SchedulerException
	 */
	private static void deleteReportJob(String aReportName) throws DatabaseException,
			SchedulerException {		
		
		String reportJob = ReportCirculation.lookupReportJobsByReportName(aReportName);		
		if ((reportJob == null) || (reportJob.trim().equals("")))
			LOG.info("No scheduled job found for this job. Hence skipping deletion of job.");
		else{
			String[] rDetails = reportJob.split(",");
			TBitsScheduler.getScheduler().deleteJob(rDetails[JOB_NAME], rDetails[JOB_GROUP]);
			ReportCirculation.deleteReportJob(aReportName);
		}
	}
}
