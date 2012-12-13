package transbit.tbits.scheduler;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Hashtable;

import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.utils.Pair;

import transbit.tbits.common.DataSourcePool;

public class MoveJobDataMap {
	public static void moveJDMBlobToTable(Connection conn) throws SQLException, SchedulerException
	{
		System.out.println("Moving Job Data map from blob to table.");

		Statement stmt = conn.createStatement();
		stmt.executeUpdate("delete from QRTZ_JOB_DATA_MAP");
		stmt.close();
		System.out.println("Deleted the job data map.");
		PreparedStatement ps = 
			conn.prepareStatement("insert into QRTZ_JOB_DATA_MAP (JOB_NAME, JOB_GROUP, ENTRY, VALUE) VALUES (?,?,?,?)");
		
		Scheduler sch = TBitsScheduler.getScheduler();
		for(String group: sch.getJobGroupNames())
		{
			for(String job: sch.getJobNames(group))
			{
				JobDataMap jdm = sch.getJobDetail(job, group).getJobDataMap();
				for(Object entry: jdm.keySet())
				{
					String value = (String) jdm.get(entry);
					System.out.println("Inserting : [" + job + ", " + group + ", " + entry + ", " + value + "]");
					ps.setString(1, job);
					ps.setString(2, group);
					ps.setString(3, (String) entry);
					ps.setString(4, value);
					ps.execute();
				}
				
			}
		}
		ps.close();
		
	}
	
	public static void moveJDMTableToBlob(Connection conn) throws SQLException, SchedulerException
	{
		System.out.println("Moving Job Data map from table to blob.");
		Scheduler sch = TBitsScheduler.getScheduler();
		
		Hashtable<Pair, Hashtable<String, String>> dataHash = new  Hashtable<Pair, Hashtable<String, String>>();
		PreparedStatement ps = conn.prepareStatement("select * from QRTZ_JOB_DATA_MAP");
		ResultSet rs = ps.executeQuery();
		while(rs.next())
		{
			String name = rs.getString("JOB_NAME");
			String group = rs.getString("JOB_GROUP");
			String entry = rs.getString("ENTRY");
			String value = rs.getString("VALUE");
			
			Pair jobName = new Pair(name, group);
			Hashtable<String, String> jobDataParams;
			if(!dataHash.containsKey(jobName))
			{
				jobDataParams = new Hashtable<String, String>();
				dataHash.put(jobName, jobDataParams);
			}
			else
				jobDataParams = dataHash.get(jobName);
			jobDataParams.put(entry, value);
		}
		System.out.println("Loaded the job data parameters from table.");
		for(Pair jobName:dataHash.keySet())
		{
			JobDetail jd = sch.getJobDetail((String)jobName.getFirst(), (String)jobName.getSecond());
			jd.setRequestsRecovery(true);
			JobDataMap jdm = jd.getJobDataMap();
			jdm.clear();
			jdm.putAll(dataHash.get(jobName));
			sch.addJob(jd, true);
			System.out.println("Update the job: [" + jd.getName() + ", " + jd.getGroup() + "]");
			System.out.println("The params are : " + dataHash.get(jobName));
		}	
	}
	private static void showError()
	{
		System.err.println("Syntax: MoveJobDataMap <table-to-blob|blob-to-table>");
	}
	public static void main(String[] args) {
		if(args.length == 0)
		{
			showError();
			return;
		}
		if(args[0].toLowerCase().equalsIgnoreCase("table-to-blob"))
		{
			
			Connection conn = null;
			try {
				conn = DataSourcePool.getConnection();
				moveJDMTableToBlob(conn);
			} catch (SQLException e) {
				e.printStackTrace();
			} catch (SchedulerException e) {
				e.printStackTrace();
			}
			finally
			{
				try {
					if(conn != null)
						conn.close();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		else if(args[0].toLowerCase().equalsIgnoreCase("blob-to-table"))
		{
			Connection conn = null;
			try {
				conn = DataSourcePool.getConnection();
				moveJDMBlobToTable(conn);
			} catch (SQLException e) {
				e.printStackTrace();
			} catch (SchedulerException e) {
				e.printStackTrace();
			}
			finally
			{
				try {
					if(conn != null)
						conn.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}
		else 
			showError();
	}
}
