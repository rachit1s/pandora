package transmittal.com.tbitsGlobal.server;
import java.util.Calendar;
import java.util.Date;

import transbit.tbits.domain.DefaultHolidayCalendar;

public class MyHolidayCalendar extends DefaultHolidayCalendar {

	public boolean isHoliday(Date date) {
		Calendar c = Calendar.getInstance();
		c.setTime(date);
		int dow  = c.get(Calendar.DAY_OF_WEEK);
		if( (dow == Calendar.SUNDAY) || (dow == Calendar.SATURDAY))
		{
			return true;
		}
		else
		{
			return super.isHoliday(date);
		}
	}
	
	public static void main(String[] args){
		MyHolidayCalendar mhc = new MyHolidayCalendar();
		Date date = new Date(Calendar.getInstance().getTime().parse("11/02/2009"));
		System.out.println("test date: " + mhc.isHoliday(date ) );
	}
}
