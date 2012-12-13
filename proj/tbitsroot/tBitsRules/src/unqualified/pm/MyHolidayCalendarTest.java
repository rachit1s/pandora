package pm;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import transbit.tbits.domain.IHolidayCalender;

import junit.framework.TestCase;


public class MyHolidayCalendarTest extends TestCase {
	IHolidayCalender hcal = null;
	SimpleDateFormat sdf = null;
	public void setUp()
	{
		hcal = new HolidayCalendarForTesting();
		 sdf = new SimpleDateFormat("MM/dd/yyyy");
	}
	public void testIsHoliday() throws ParseException {
		assertTrue(hcal.isHoliday(sdf.parse("01/19/2008")));
		assertTrue(hcal.isHoliday(sdf.parse("01/20/2008")));
		assertTrue(hcal.isHoliday(sdf.parse("01/26/2008")));
		assertTrue(hcal.isHoliday(sdf.parse("01/27/2008")));
	}
	public void testIsHoliday3() throws ParseException {
		assertTrue(hcal.isHoliday(sdf.parse("03/06/2008")));
	}
	public void testIsHoliday4() throws ParseException {
		assertTrue(hcal.isHoliday(sdf.parse("03/20/2008")));
	}
	public void testIsHoliday5() throws ParseException {
		assertTrue(hcal.isHoliday(sdf.parse("03/21/2008")));
	}
	public void testIsHoliday6() throws ParseException {
		assertTrue(hcal.isHoliday(sdf.parse("03/22/2008")));
	}
	public void testIsHoliday7() throws ParseException {
		assertTrue(hcal.isHoliday(sdf.parse("04/14/2008")));
	}
	public void testIsHoliday8() throws ParseException {
		assertTrue(hcal.isHoliday(sdf.parse("04/18/2008")));
	}
	public void testIsHoliday9() throws ParseException {
		assertTrue(hcal.isHoliday(sdf.parse("05/01/2008")));
	}
	public void testIsHoliday10() throws ParseException {
		assertTrue(hcal.isHoliday(sdf.parse("05/19/2008")));
	}
	public void testIsHoliday11() throws ParseException {
		assertTrue(hcal.isHoliday(sdf.parse("08/15/2008")));
	}
	public void testIsHoliday12() throws ParseException {
		assertTrue(hcal.isHoliday(sdf.parse("09/03/2008")));
	}
	public void testIsHoliday13() throws ParseException {
		assertTrue(hcal.isHoliday(sdf.parse("10/02/2008")));
	}
	public void testIsHoliday14() throws ParseException {
		assertTrue(hcal.isHoliday(sdf.parse("10/09/2008")));
	}
	public void testIsHoliday15() throws ParseException {
		assertTrue(hcal.isHoliday(sdf.parse("10/28/2008")));
	}
	public void testIsHoliday16() throws ParseException {
		assertTrue(hcal.isHoliday(sdf.parse("10/30/2008")));
	}
	public void testIsHoliday17() throws ParseException {
		assertTrue(hcal.isHoliday(sdf.parse("11/13/2008")));
	}
	public void testIsHoliday18() throws ParseException {
		assertTrue(hcal.isHoliday(sdf.parse("12/09/2008")));
	}
	public void testIsHoliday19() throws ParseException {
		assertTrue(hcal.isHoliday(sdf.parse("12/25/2008")));
	}
	public void testIsHoliday20() throws ParseException {
		assertFalse(hcal.isHoliday(sdf.parse("12/26/2008")));
	}
}
