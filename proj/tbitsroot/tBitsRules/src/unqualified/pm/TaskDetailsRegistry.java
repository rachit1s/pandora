package pm;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

import transbit.tbits.Helper.TBitsConstants;
import transbit.tbits.common.DataSourcePool;
import transbit.tbits.common.TBitsLogger;

public class TaskDetailsRegistry {
	public static final TBitsLogger LOG = TBitsLogger.getLogger(TBitsConstants.PKG_COMMON);
	
	/**
	 * 
	 * @param whereClause should start with 'and' or it can be blank
	 * @return
	 */
	private static ArrayList<TaskDetails> getTasks(String whereClause)
	{
		StringBuilder sb = new StringBuilder();
		ArrayList<TaskDetails> allTasks = new ArrayList<TaskDetails>();
		sb.append("")
		.append(" select")
		.append("	t.sys_id, t.request_id request_id, r.status_id status_id, t.int_value taskid, ")
		.append("	sd.datetime_value startdate, ed.datetime_value enddate, ")
		.append("	asd.datetime_value astartdate, aed.datetime_value aenddate, ")
		.append("	pred.varchar_value predecessors, du.real_value duration ")
		.append(" from requests r ")
		.append(" JOIN requests_ex t on t.sys_id = r.sys_id and t.request_id = r.request_id ")
		.append(" JOIN requests_ex sd on t.sys_id = sd.sys_id and t.request_id = sd.request_id ")
		.append(" JOIN requests_ex ed on t.sys_id = ed.sys_id and t.request_id = ed.request_id ")
		.append(" JOIN requests_ex asd on t.sys_id = asd.sys_id and t.request_id = asd.request_id ")
		.append(" JOIN requests_ex aed on t.sys_id = aed.sys_id and t.request_id = aed.request_id ")
		.append(" JOIN requests_ex pred on t.sys_id = pred.sys_id and t.request_id = pred.request_id ")
		.append(" JOIN requests_ex du on t.sys_id = du.sys_id and t.request_id = du.request_id ")
		.append(" JOIN fields fsd on fsd.name = 'startdate' and t.sys_id = fsd.sys_id and sd.field_id = fsd.field_id ")
		.append(" JOIN fields fed on fed.name = 'enddate' and t.sys_id = fed.sys_id and ed.field_id = fed.field_id ")
		.append(" JOIN fields fpred on fpred.name = 'predecessors' and t.sys_id =fpred.sys_id and pred.field_id = fpred.field_id ")
		.append(" JOIN fields fdur on fdur.name = 'duration' and t.sys_id = fsd.sys_id and du.field_id =  fdur.field_id ")
		.append(" JOIN fields ftid on ftid.name = 'taskid' and t.sys_id = fsd.sys_id and t.field_id = ftid.field_id ")
		.append(" JOIN fields fasd on fasd.name = 'astartdate' and t.sys_id = fasd.sys_id and asd.field_id = fasd.field_id ")
		.append(" JOIN fields faed on faed.name = 'aenddate' and t.sys_id = faed.sys_id and aed.field_id = faed.field_id ")
		.append(" where 1=1 ")
		.append(whereClause)
		.append("");

		Connection conn = null;
		try {
			
			conn = DataSourcePool.getConnection();
			PreparedStatement stmt  = conn.prepareStatement(sb.toString());
			//stmt.setInt(1, sysId);
			Calendar c = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
			ResultSet rs = stmt.executeQuery();
			while(rs.next())
			{
				int sysId = rs.getInt("sys_id");
				int requestId = rs.getInt("request_id");
				int taskId = rs.getInt("taskid");
				String predStr = rs.getString("predecessors");
				Date startDate = rs.getDate("startdate", c);
				Date endDate = rs.getDate("enddate", c);
				Date astartDate = rs.getDate("astartdate", c);
				Date aendDate = rs.getDate("aenddate", c);
				double duration = rs.getDouble("duration");
				int statusId = rs.getInt("status_id");
				
				ArrayList<Predecessor> preds = PredeccesorParser.parse(predStr);
				TaskDetails td = new TaskDetails(startDate, endDate, astartDate, aendDate, requestId, (long) taskId, (int) duration, sysId);
				td.predList = preds;
				td.statusId = statusId;
				allTasks.add(td);
				
			}
			rs.close();
			stmt.close();
			
		} catch (SQLException e) {
			LOG.error("Invalid Query: " + sb.toString(), e);
		}
		finally
		{
			try {
				if(conn != null)
					conn.close();
			} catch (SQLException e) {
				LOG.error("Could not close connection.");
			}
		}
		return allTasks;
	}
	//Key = sys_id-taskid
	public static HashMap<String, TaskDetails> getAllTasks()
	{
		HashMap<String, TaskDetails> registry = new HashMap<String, TaskDetails>();
		
		ArrayList<TaskDetails> allTasks = getTasks("");
		for(TaskDetails td:allTasks)
		{
			registry.put(td.sysId + "-" + td.taskId, td);
		}
		return registry;
	}
	
	private static boolean containsCycle(TaskDetails td, String originalKey,HashMap<String, TaskDetails> registry, 
			ArrayList<String> alreadyVisited) throws IllegalAccessException
	{	
		
		// get the task ids of the all predecessors for task td
		if(td.predList.size() == 0) {
			return false;
		}
		for(Predecessor pred:td.predList) {
			String key = td.sysId + "-" + pred.taskId;
		
			if (!alreadyVisited.contains(key)) {
				alreadyVisited.add(key);
				if(alreadyVisited.contains(originalKey)) 
					return true;

				TaskDetails predTD = registry.get(key);
				if(predTD == null)
				{
					throw new IllegalAccessException("Task with id " + pred.taskId + " doesn't exist.");
				} 
				boolean isCycle = containsCycle(predTD,originalKey,registry, alreadyVisited);
				if(isCycle)
					return isCycle;
			}
		
		}
		
		return false;
	}
	
	public static boolean checkForCycleDependency(TaskDetails td, HashMap<String, TaskDetails> registry) throws IllegalAccessException {
		ArrayList<String> alreadyVisited = new ArrayList<String>();
		String key = td.sysId + "-" + td.taskId;
	
		boolean isCycle = containsCycle(td,key,registry, alreadyVisited);
		return isCycle;
		
	}
	
	//TODO: it doesn't traverse the complete map
	public static Integer[] getImmediateSuccessors(long taskId, int sysId) 
	{
		ArrayList<Integer> a = new ArrayList<Integer>();
		Collection<TaskDetails> tds = getAllTasks().values();
		for(TaskDetails td:tds)
		{
			if(td.sysId == sysId)
			{
				for(Predecessor p: td.predList)
				{
					if(p.taskId == taskId)
					{
						if(!a.contains(td.requestId))
							a.add(td.requestId);
					}
				}
			}
		}
		Integer[] output = new Integer[a.size()];
		System.out.println("DEP Requests: " + output);
		output = a.toArray(output);
		return output;
	}
	
	/**
	 * Gets all the tasks with taskIds and systemId
	 * @param taskIds comma separated task ids. Blank means all
	 * @param systemId 0 means all
	 * @return
	 * @throws SQLException 
	 * @throws SQLException
	 */	
	public static ArrayList<TaskDetails> getTaskDetails(String taskIds, int systemId) throws SQLException
	{
		
		StringBuilder whereClause = new StringBuilder();
		//TODO: parse taskids and validate
		if(taskIds.trim().length() != 0)
		{
			whereClause.append(" and t.int_value in ( ")
			.append(taskIds)
			.append(" ) ");
		}
		if(systemId != 0)
		{
			whereClause.append(" and t.sys_id = ").append(systemId);
		}
		ArrayList<TaskDetails> tasks = getTasks(whereClause.toString());
		return tasks;
	}

	public static boolean alreadyExists(Integer taskId, int systemId) throws SQLException{
		
		ArrayList<TaskDetails>td = getTaskDetails(taskId + "", systemId);
		return (td.size() > 0);
	}
	public static void main(String[] args) {
		try {
			ArrayList<TaskDetails> allTasks = getTaskDetails("", 0);
			for(TaskDetails td:allTasks)
			{
				System.out.println(td);
			}
			System.out.println("Done!");
			Map<String, TaskDetails> all = getAllTasks();
			for(String key:all.keySet())
			{
				System.out.println(key + ":" + all.get(key).toString());
			}
			System.out.println(all);
			System.out.println("Done!");
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
}
