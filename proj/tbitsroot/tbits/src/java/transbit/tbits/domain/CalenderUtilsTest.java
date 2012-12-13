package transbit.tbits.domain;

import static org.junit.Assert.*;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import org.junit.Before;
import org.junit.Test;

public class CalenderUtilsTest {

	IHolidayCalender hc = null;
	@Before
	public void setUp() throws Exception {
		hc = new IHolidayCalender() {
			public boolean isHoliday(Date date) {
				Calendar c = Calendar.getInstance();
				c.setTime(date);
				switch (c.get(Calendar.DAY_OF_WEEK)) {
				case Calendar.TUESDAY:
				case Calendar.WEDNESDAY:
				case Calendar.SUNDAY:
				case Calendar.SATURDAY:
					return true;
				default:
					return false;
				}
			}
		};
	}

	/*
	 * End Falling in holiday
	 */
	@Test
	public final void testSlideDate1() {
		
		Calendar c = Calendar.getInstance();
		c.set(2008, 9, 3);
		
		Calendar c1 = Calendar.getInstance();
		c1.set(2008, 9, 6);
		
		Date d = CalenderUtils.slideDate(c.getTime(), 1, hc);
		
		assertEquals(c1.getTime(), d);
	}

	/*
	 * Starting with Holiday.
	 */
	@Test
	public final void testSlideDate2() {
		
		Calendar c = Calendar.getInstance();
		c.set(2008, 9, 4);
		
		Calendar c1 = Calendar.getInstance();
		c1.set(2008, 9, 9);
		
		Date d = CalenderUtils.slideDate(c.getTime(), 1, hc);
		
		assertEquals(c1.getTime(), d);
	}

	/*
	 * Some five days
	 */
	@Test
	public final void testSlideDate3() {
		
		Calendar c = Calendar.getInstance();
		c.set(2008, 9, 3);
		
		Calendar c1 = Calendar.getInstance();
		c1.set(2008, 9, 16);
		
		Date d = CalenderUtils.slideDate(c.getTime(), 5, hc);
		
		assertEquals(c1.getTime(), d);
	}
	
	@Test
	public final void testHolidays()
	{
		Calendar c = Calendar.getInstance();
		c.set(2008, 9, 7,0,0,0);
		assertTrue(	hc.isHoliday(c.getTime()));
	}
	
}
