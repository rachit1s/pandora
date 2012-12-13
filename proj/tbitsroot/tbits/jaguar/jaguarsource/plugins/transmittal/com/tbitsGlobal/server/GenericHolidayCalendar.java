/**
 * 
 */
package transmittal.com.tbitsGlobal.server;

import java.util.Calendar;
import java.util.Date;

import transbit.tbits.domain.HolidaysList;
import transbit.tbits.domain.IHolidayCalender;

/**
 * @author lokesh
 *
 */
public class GenericHolidayCalendar implements IHolidayCalender {
	String timeZone = "";
	
	public GenericHolidayCalendar() {
		
	}
	
	public GenericHolidayCalendar(String timeZone){
		this.timeZone = timeZone;
	}
	
	/* (non-Javadoc)
	 * @see transbit.tbits.domain.IHolidayCalender#isHoliday(java.util.Date)
	 */
	public boolean isHoliday(Date date) {
		boolean isHoliday = HolidaysList.isHoliday(date, this.timeZone);
		return isHoliday;
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		GenericHolidayCalendar ghc = new GenericHolidayCalendar();
		System.out.println("Is Holiday: " + ghc.isHoliday(new Date(Calendar.getInstance().getTime().parse("08/26/2005"))));
		System.exit(0);
	}

}
