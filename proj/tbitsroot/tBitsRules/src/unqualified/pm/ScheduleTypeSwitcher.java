package pm;

import java.util.Date;

public interface ScheduleTypeSwitcher {
	public Date getStartDate(TaskDetails td);
	public Date getEndDate(TaskDetails td);
	public TaskDetails getTaskDetails(Date startDate, Date endDate, int requestId, int taskId, Integer duration, int systemId);
}
