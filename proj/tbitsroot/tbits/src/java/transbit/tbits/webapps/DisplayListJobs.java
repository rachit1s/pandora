package transbit.tbits.webapps;

import static transbit.tbits.Helper.TBitsConstants.PKG_WEBAPPS;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.Scheduler;

import org.quartz.SchedulerException;
import org.quartz.Trigger;


import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import transbit.tbits.common.DataSourcePool;
import transbit.tbits.common.DatabaseException;
import transbit.tbits.common.TBitsLogger;
import transbit.tbits.domain.Report;
import transbit.tbits.domain.User;
import transbit.tbits.exception.TBitsException;
import transbit.tbits.scheduler.TBitsScheduler;

public class DisplayListJobs extends HttpServlet{
	
	private static final long serialVersionUID = 1L;
	private static final TBitsLogger LOG = TBitsLogger.getLogger(PKG_WEBAPPS); 
	
	private static Scheduler myScheduler = TBitsScheduler.getScheduler();
	
	private static final String LIST_JOB_HTML = "web/tbits-admin-listJobs.html";
	
	private JSONArray ListJobArray;
	
	protected void doGet(HttpServletRequest aRequest, HttpServletResponse aResponse)
	throws ServletException, IOException {
		try {
			System.out.println("DisplayListJobServlet Called");
			handleRequest (aRequest, aResponse);
		} catch (DatabaseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (TBitsException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	protected void doPost(HttpServletRequest aRequest, HttpServletResponse aResponse)
		throws ServletException, IOException {
		try {
			handleRequest (aRequest, aResponse);
		} catch (DatabaseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (TBitsException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	private void handleRequest (HttpServletRequest aRequest, HttpServletResponse aResponse) throws ServletException, IOException, DatabaseException, TBitsException {
		
		aResponse.setContentType("text/html");
		PrintWriter out = aResponse.getWriter();		
	//	User user = WebUtil.validateUser(aRequest);
		
		ListJobArray = new JSONArray();
		ListJobArray = getListJob();
		out.print(ListJobArray);
		
		
	}
  //getting list Qrtz jobs detail from Data base and pass to json Array       
	
	private JSONArray getListJob() {
		JSONArray ListJobArr = new JSONArray();
		
		JSONObject ListJob = new JSONObject();
		Connection connection = null;

        try {
            connection = DataSourcePool.getConnection();

            PreparedStatement ListJobsStatement = connection.prepareStatement("select * from QRTZ_JOB_DETAILS");
            ResultSet rs = ListJobsStatement.executeQuery();
            
            if (rs != null) {
               while (rs.next()) {
                	
                	  if(myScheduler.getTriggerState(rs.getString("job_name"),
                		  rs.getString("job_group")) == Trigger.STATE_PAUSED)
                	      {
                		   ListJob.put("job_state","Pause");
                	      }
                	   else
                	     {
                		   ListJob.put("job_state","Running");
                	     }
                	  ListJob.put("job_name", rs.getString("job_name"));
                  	  ListJob.put("Job_group", rs.getString("job_group"));
                  	  ListJob.put("description", rs.getString("description"));
                  	  ListJob.put("Job_class_name", rs.getString("job_class_name"));
                	  
                	ListJobArr.add(ListJob);
                }

                // Close the result set.
                rs.close();
            }

            // Close the statement.
            ListJobsStatement.close();

            //
            // Release the memory by nullifying the references so that these
            // are recovered by the Garbage collector.
            //
            rs = null;
            ListJobsStatement = null;
        } catch (SQLException sqle) {
            StringBuilder message = new StringBuilder();
            message.append("An exception occured while retrieving the existing").append("\n");
            try {
				throw new DatabaseException(message.toString(), sqle);
			} catch (DatabaseException e) {
				LOG.error("Exception while closing the connection:", sqle);
			}
        } catch (SchedulerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
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
       
		return (ListJobArr);
	}
	
	
	

}
