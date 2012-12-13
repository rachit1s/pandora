package pm;
import java.util.ArrayList;
import java.util.Date;

public class TaskDetails {
		public Date startDate;
		public Date endDate;
		public Date actualStartDate;
		public Date actualEndDate;
		public int requestId;
		public long taskId;
		public Integer duration;
		public int sysId;
		public ArrayList<Predecessor> predList = new ArrayList<Predecessor>();
		public int statusId;
		public TaskDetails(Date startDate, Date endDate, Date actualStartDate, Date actualEndDate, int requestId,
				long taskId, Integer duration, int sysId) {
			super();
			this.startDate = startDate;
			this.endDate = endDate;
			this.actualStartDate = actualStartDate;
			this.actualEndDate = actualEndDate;
			this.requestId = requestId; 
			this.taskId = taskId;
			this.duration = duration;
			this.sysId = sysId;
		}
		public TaskDetails() {
			super();
		}
		
		public String toString()
		{
			return "{[startDate: " + startDate + "], [endDate: " + endDate 
			+ " ], [ActualStartDate: " + actualStartDate + "], [endDate: " + actualEndDate 
			+ " ], [requestId:" + requestId + "], [taskId: " + taskId 
			+ "],[duration: " + duration + "], [sysId: " + sysId + "],[pred: " + predList + "]}";
		}
}
