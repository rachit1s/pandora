package pm;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import transbit.tbits.domain.IHolidayCalender;


public class ProjectManagement {
	
	private static final long MILLIS_IN_DAY = 24*60*60*1000;
	public IIssueReader issueReader = null;
	public IHolidayCalender holidayCal = null;
	
	
	public TaskDetails getSchedule(Date startDate, Date endDate, Date astartDate, Date aendDate, Integer duration, ArrayList<Predecessor> preds, 
			Date initialDate, Date initialADate)
	{
		//Get Planned Schedule
		TaskDetails plannedSch = getSchedule(startDate, endDate, duration, preds, initialDate, true);
		
		if(initialADate == null)
		{
			initialADate = plannedSch.startDate;
		}
		//Get Actual schedule
		TaskDetails actualSch = getSchedule(astartDate, aendDate, duration, preds, initialADate, false);
		
		plannedSch.actualStartDate = actualSch.actualStartDate;
		plannedSch.actualEndDate = actualSch.actualEndDate;
		return plannedSch;
	}
	
	public TaskDetails getSchedule(Date startDate, Date endDate, Integer duration, ArrayList<Predecessor> preds, Date initialDate, Boolean isPlanned)
	{
		if(isPlanned)
			return getSchedule(startDate, endDate, duration, preds, initialDate, new ScheduleTypeSwitcher(){

				public Date getEndDate(TaskDetails td) {
					return td.endDate;
				}

				public Date getStartDate(TaskDetails td) {
					return td.startDate;
				}

				public TaskDetails getTaskDetails(Date startDate, Date endDate,
						int requestId, int taskId, Integer duration, int systemId) {
					// TODO Auto-generated method stub
					return new TaskDetails(startDate, endDate, null, null, requestId, taskId, duration, systemId);
				}

			});
		else
			return getSchedule(startDate, endDate, duration, preds, initialDate, new ScheduleTypeSwitcher(){

				public Date getEndDate(TaskDetails td) {
					// TODO Auto-generated method stub
					return td.actualEndDate;
				}

				public Date getStartDate(TaskDetails td) {
					// TODO Auto-generated method stub
					return td.actualStartDate;
				}

				public TaskDetails getTaskDetails(Date startDate, Date endDate,
						int requestId, int taskId, Integer duration,
						int systemId) {
					// TODO Auto-generated method stub
					return new TaskDetails(null, null, startDate, endDate, requestId, taskId, duration, systemId);
				}
			});
	}
	public TaskDetails getSchedule(Date startDate, Date endDate, Integer duration, ArrayList<Predecessor> preds, Date initialDate, ScheduleTypeSwitcher sw)
	{
		boolean isDurationSpecified = true;
		
		/*
		 * Assume Defaults
		 */
		//Slide the date until you hit non-holiday
		if(startDate != null)
		{
			startDate = getWorkingDay(startDate);
		}
		if(endDate != null)
		{
			endDate = getWorkingDay(endDate);
		}
		
		if(duration == null)
		{
			duration = 1;
			isDurationSpecified = false;
		}
		
		//Calculate start date and end date based on preds
		if((preds != null) && (issueReader != null))
		{
			//Calculate Start Date based on SNET
			// and end date based on ENET
			for(Predecessor pd: preds)
			{
				TaskDetails td = issueReader.getIssueById(pd.taskId);
				if(td != null)
				{
					if(pd.depType == DepType.FF)
					{
						endDate = max(endDate, addDays( sw.getEndDate(td), pd.lag));
					}
					else if(pd.depType == DepType.SF)
					{
						Date d;
						if(pd.lag == 0)
						{
							d = sub(sw.getStartDate(td), 1);
						}
						else
						{
							d = addDays(sw.getStartDate(td), pd.lag - 1);
						}
						endDate = max(endDate, d);
					}
					else if(pd.depType == DepType.SS)
					{
						startDate = max(startDate, addDays(sw.getStartDate(td) , pd.lag));
					}
					else // FS
					{
						startDate = max(startDate, addDays(sw.getEndDate(td) , pd.lag + 1));
					}
				}
			}
		}
		
		//Initial date calculation
		if(startDate == null)
		{
			if(initialDate == null)
				startDate = getWorkingDay(Calendar.getInstance().getTime());
			else
				startDate = initialDate;
		}
		
		//Conflict Resolution and recalculation
		if(endDate != null)
		{
			int dur1 = sub(endDate,startDate) + 1;
			if( (dur1 > duration) && !isDurationSpecified )
			{
				//Recalculate duration
				duration = dur1;
			}
			if( addDays( startDate, duration).after(endDate))
			{
				endDate = addDays(startDate , duration - 1);
			}
			else if(sub(endDate, duration).after(startDate))
			{
				startDate = sub(endDate, duration - 1);
			}
		}
		else
		{
			endDate = addDays(startDate , duration - 1);
		}
		TaskDetails ts = sw.getTaskDetails(startDate, endDate, 0, 0, duration, 0);
		return ts;
	}
	
	
	/*
	 * Gets the current date or the next working days if current date is holiday
	 */
	public Date getWorkingDay(Date date) {
		if(holidayCal != null)
		{
			while(holidayCal.isHoliday(date))
			{
				date = new Date(date.getTime() + MILLIS_IN_DAY);
			}
		}
		return date;
	}

	/*
	 * Get differences as days minus holidays.
	 */
	public int sub(Date end, Date start)
	{
		int days = 0;
		while(start.before(end))
		{
			days++;
			start = addDays(start, 1);
		}
		return days;
	}
	
	/*
	 * x - days - minus holidays
	 */
	public Date sub(Date x, int days)
	{
		int i = 0;
		if(holidayCal != null)
		{
			while( i < days)
			{
				i++;
				do
				{
					x = new Date(x.getTime() - MILLIS_IN_DAY);
				}
				while(holidayCal.isHoliday(x));
			}
		}
		return x;
	}
	
	public static Date max(Date x, Date y)
	{
		if((x == null) && (y == null))
			return null;
		else if(x == null)
			return y;
		else if(y == null)
			return x;
		else 
		{
			return x.after(y) ? x : y;
		}
	}
	
	/*
	 * return date + days + holidays
	 */
	public Date addDays(Date date, int days)
	{
		date = getWorkingDay(date);
		for(int i = 1; i <= days; i++)
		{
			date = getWorkingDay(new Date(date.getTime() + MILLIS_IN_DAY));
		}
		return date;
	}
	
	public static void main(String []args)
	{
//		ArrayList<PredecessorDetails> preds = new ArrayList<PredecessorDetails> ();
//		preds.add(new PredecessorDetails(1,11,15,2, DepType.SF));
//		
//		TaskSchedule ts = TestProjectManagement.getSchedule(null, null, null, preds);
//		System.out.println("Schedule: " + ts.startDate);
//		if(ts.startDate == 17)
//		{
//			System.out.println("Ok.");
//		}
//		else
//		{
//			System.out.println("Error!");
//		}
			
	}
}
