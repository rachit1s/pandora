package transbit.tbits.domain;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import transbit.tbits.Helper.TBitsConstants;
import transbit.tbits.common.TBitsLogger;
import transbit.tbits.plugin.PluginManager;

public class CalenderUtils {
	public static final TBitsLogger LOG = TBitsLogger
	.getLogger(TBitsConstants.PKG_SCHEDULER);
	/*
	 * Find a date after days of startDate considering the holidays using HolidayCalender.
	 */
	public static Date slideDate(Date startDate, int days,
			IHolidayCalender hc) {
		
		Calendar c = Calendar.getInstance();
		c.setTime(startDate);
		
		while(true)
		{
			if(hc.isHoliday(c.getTime()))
			{
				c.add(Calendar.DAY_OF_MONTH, 1);
				continue;
			}
			
			if(days == 0)
				break;
			else
			{
				c.add(Calendar.DAY_OF_MONTH, 1);
				days--;
			}
		}
		return c.getTime();
	}

	public static Date slideDate(Date time, int days) {
		// TODO Auto-generated method stub
		IHolidayCalender ihc = null;
		ArrayList<Class> calClasses = PluginManager.getInstance().findPluginsByInterface(IHolidayCalender.class.getName());
		if( (calClasses == null) || (calClasses.size() > 0))
		{
			Class calClass = calClasses.get(0);
			LOG.info("Using the calender: " + calClass.getName());
			try {
				ihc = (IHolidayCalender) calClass.newInstance();
			} catch (InstantiationException e) {
				LOG.error("Unable to instantiate " + calClass.getName() 
						+ ". So using default calender.", e);
			} catch (IllegalAccessException e) {
				LOG.error("Illegal Access Exception while loading " + calClass.getName() 
						+ ". So using default calender.", e);
			}
		}
		if(ihc == null)
			ihc = new DefaultHolidayCalendar();
		return slideDate(time, days, ihc);
	}

}
