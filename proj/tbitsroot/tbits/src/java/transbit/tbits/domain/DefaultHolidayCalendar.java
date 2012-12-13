package transbit.tbits.domain;
import java.util.Date;

import transbit.tbits.domain.HolidaysList;
import transbit.tbits.domain.IHolidayCalender;
/*
 * Returns true if the date is in listed in Holiday Calender
 */
public class DefaultHolidayCalendar implements IHolidayCalender {
	public DefaultHolidayCalendar()
	{
		
	}
	public  boolean isHoliday(Date date) {
			boolean isHoliday = HolidaysList.isHoliday(date, "HYD");
			return isHoliday;
	}
}
