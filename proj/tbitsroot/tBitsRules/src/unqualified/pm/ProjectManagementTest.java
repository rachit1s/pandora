package pm;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;


import junit.framework.TestCase;


public class ProjectManagementTest extends TestCase {
	ProjectManagement pm;
	@Override
	protected void setUp() throws Exception {
		ArrayList<TaskDetails> tasks = new ArrayList<TaskDetails>();
		Calendar sc = Calendar.getInstance();
		sc.set(2008, 4, 14);
		
		Calendar ed = Calendar.getInstance();
		ed.set(2008, 4, 19);
		
		TaskDetails td = new TaskDetails(sc.getTime(), ed.getTime(), null, null, 1, 1, null,1);
		tasks.add(td);
		
		
		sc = Calendar.getInstance();
		sc.set(2008, 4, 19);
		ed = Calendar.getInstance();
		ed.set(2008, 4, 20);

		tasks.add(new TaskDetails(sc.getTime(), ed.getTime(), null, null, 1, 2, null, 1));

		MyIssueReader myIssueReader = new MyIssueReader(tasks);
		
		pm = new ProjectManagement();
		pm.issueReader = myIssueReader;
		pm.holidayCal = new HolidayCalendarForTesting();
	}
	public void test1SS0_1()
	{
		Calendar c = Calendar.getInstance();
		c.set(2008, 4, 1);

		ArrayList<Predecessor> preds = new ArrayList<Predecessor>();
		preds.add(new Predecessor(1, 0,DepType.SS));
		TaskDetails td = pm.getSchedule(c.getTime(), c.getTime(), 1, preds, null, true);
		
		c.setTime(td.startDate);
		assertEquals(14, c.get(Calendar.DATE));
		assertEquals(4, c.get(Calendar.MONTH));
		
		c.setTime(td.endDate);
		assertEquals(14, c.get(Calendar.DATE));
		assertEquals(4, c.get(Calendar.MONTH));
	}
	public void test1SF0_1()
	{
		Calendar c = Calendar.getInstance();
		c.set(2008, 4, 1);

		ArrayList<Predecessor> preds = new ArrayList<Predecessor>();
		preds.add(new Predecessor(1, 0,DepType.SF));
		TaskDetails td = pm.getSchedule(c.getTime(), c.getTime(), 1, preds, null, true);
		
		c.setTime(td.startDate);
		assertEquals(13, c.get(Calendar.DATE));
		assertEquals(4, c.get(Calendar.MONTH));
		
		c.setTime(td.endDate);
		assertEquals(13, c.get(Calendar.DATE));
		assertEquals(4, c.get(Calendar.MONTH));
	}
	public void test1FS0_1()
	{
		Calendar c = Calendar.getInstance();
		c.set(2008, 4, 1);

		ArrayList<Predecessor> preds = new ArrayList<Predecessor>();
		preds.add(new Predecessor(1, 0,DepType.FS));
		TaskDetails td = pm.getSchedule(c.getTime(), c.getTime(), 1, preds, null, true);
		
		c.setTime(td.startDate);
		assertEquals(20, c.get(Calendar.DATE));
		assertEquals(4, c.get(Calendar.MONTH));
		
		c.setTime(td.endDate);
		assertEquals(20, c.get(Calendar.DATE));
		assertEquals(4, c.get(Calendar.MONTH));
	}
	public void test1FF0_1()
	{
		Calendar c = Calendar.getInstance();
		c.set(2008, 4, 1);

		ArrayList<Predecessor> preds = new ArrayList<Predecessor>();
		preds.add(new Predecessor(1, 0,DepType.FF));
		TaskDetails td = pm.getSchedule(c.getTime(), c.getTime(), 1, preds, null, true);
		
		c.setTime(td.startDate);
		assertEquals(19, c.get(Calendar.DATE));
		assertEquals(4, c.get(Calendar.MONTH));
		
		c.setTime(td.endDate);
		assertEquals(19, c.get(Calendar.DATE));
		assertEquals(4, c.get(Calendar.MONTH));
	}
	public void test1SS1_2()
	{
		Calendar c = Calendar.getInstance();
		c.set(2008, 4, 1);

		ArrayList<Predecessor> preds = new ArrayList<Predecessor>();
		preds.add(new Predecessor(1, 1,DepType.SS));
		TaskDetails td = pm.getSchedule(c.getTime(), c.getTime(), 2, preds, null, true);
		
		c.setTime(td.startDate);
		assertEquals(15, c.get(Calendar.DATE));
		assertEquals(4, c.get(Calendar.MONTH));
		
		c.setTime(td.endDate);
		assertEquals(16, c.get(Calendar.DATE));
		assertEquals(4, c.get(Calendar.MONTH));
	}
	public void test1SF1_2()
	{
		Calendar c = Calendar.getInstance();
		c.set(2008, 4, 1);

		ArrayList<Predecessor> preds = new ArrayList<Predecessor>();
		preds.add(new Predecessor(1, 1,DepType.SF));
		TaskDetails td = pm.getSchedule(c.getTime(), c.getTime(), 2, preds, null, true);
		
		c.setTime(td.startDate);
		assertEquals(13, c.get(Calendar.DATE));
		assertEquals(4, c.get(Calendar.MONTH));
		
		c.setTime(td.endDate);
		assertEquals(14, c.get(Calendar.DATE));
		assertEquals(4, c.get(Calendar.MONTH));
	}
	public void test1FS1_2()
	{
		Calendar c = Calendar.getInstance();
		c.set(2008, 4, 1);

		ArrayList<Predecessor> preds = new ArrayList<Predecessor>();
		preds.add(new Predecessor(1, 1,DepType.FS));
		TaskDetails td = pm.getSchedule(c.getTime(), c.getTime(), 2, preds, null, true);
		
		c.setTime(td.startDate);
		assertEquals(21, c.get(Calendar.DATE));
		assertEquals(4, c.get(Calendar.MONTH));
		
		c.setTime(td.endDate);
		assertEquals(22, c.get(Calendar.DATE));
		assertEquals(4, c.get(Calendar.MONTH));
	}
	public void test1FF1_2()
	{
		Calendar c = Calendar.getInstance();
		c.set(2008, 4, 1);

		ArrayList<Predecessor> preds = new ArrayList<Predecessor>();
		preds.add(new Predecessor(1, 1,DepType.FF));
		TaskDetails td = pm.getSchedule(c.getTime(), c.getTime(), 2, preds, null, true);
		
		c.setTime(td.startDate);
		assertEquals(19, c.get(Calendar.DATE));
		assertEquals(4, c.get(Calendar.MONTH));
		
		c.setTime(td.endDate);
		assertEquals(20, c.get(Calendar.DATE));
		assertEquals(4, c.get(Calendar.MONTH));
	}
	public void testHoliday()
	{
		Calendar c = Calendar.getInstance();
		c.set(2008, 7, 15);

		ArrayList<Predecessor> preds = new ArrayList<Predecessor>();
		//preds.add(new Predecessor(1, 1,DepType.FF));
		TaskDetails td = pm.getSchedule(c.getTime(), c.getTime(), 1, preds, null, true);
		
		c.setTime(td.startDate);
		assertEquals(15, c.get(Calendar.DATE));
		assertEquals(7, c.get(Calendar.MONTH));
		
		c.setTime(td.endDate);
		assertEquals(15, c.get(Calendar.DATE));
		assertEquals(7, c.get(Calendar.MONTH));
	}
	public void testWorkingDayCalculations()
	{
		Calendar c = Calendar.getInstance();
		c.set(2008, 4, 17);
		assertEquals(Calendar.SATURDAY, c.get(Calendar.DAY_OF_WEEK));
		
		Date nextWorkingDay = pm.getWorkingDay(c.getTime());
		Calendar nwdc = Calendar.getInstance();
		nwdc.setTimeInMillis(nextWorkingDay.getTime());
		assertEquals(19, nwdc.get(Calendar.DATE));
		assertEquals(Calendar.MONDAY, nwdc.get(Calendar.DAY_OF_WEEK));
		
	}
	
	public void testSubtractDayCalculations()
	{
		Calendar c1 = Calendar.getInstance();
		c1.set(2008, 4, 9);
		assertEquals(Calendar.FRIDAY, c1.get(Calendar.DAY_OF_WEEK));
		
		Calendar c2 = Calendar.getInstance();
		c2.set(2008, 4, 23);
		assertEquals(Calendar.FRIDAY, c2.get(Calendar.DAY_OF_WEEK));
		
		int duration = pm.sub(c2.getTime(), c1.getTime());
		assertEquals(10, duration);
	}
	
	public void testAddDays()
	{
		Calendar c = Calendar.getInstance();
		c.set(2008, 4, 2);
		Date d = pm.addDays(c.getTime(), 20);
		c.setTime(d);
		assertEquals(30, c.get(Calendar.DATE));
		assertEquals(4, c.get(Calendar.MONTH));
	}
	
	public void testMaxDay()
	{
		Calendar c1 = Calendar.getInstance();
		c1.set(2008, 1, 1);
		
		Calendar c2 = Calendar.getInstance();
		c2.set(2009, 1, 1);
		
		assertNull(ProjectManagement.max(null, null));
		assertEquals(c1.getTime(), ProjectManagement.max(null, c1.getTime()));
		assertEquals(c2.getTime(), ProjectManagement.max(c1.getTime(), c2.getTime()));
		
	}
	/*
	 * This test check whether the inital date assumption is right.
	 */
	public void testInit()
	{
		Calendar c = Calendar.getInstance();
		c.set(2008, 4, 1);
	
		ArrayList<Predecessor> preds = new ArrayList<Predecessor>();
		//preds.add(new Predecessor(1, 1,DepType.FS));
		TaskDetails td = pm.getSchedule(null, null, null, preds, c.getTime(), true);
		
		c.setTime(td.startDate);
		assertEquals(1, c.get(Calendar.DATE));
		assertEquals(4, c.get(Calendar.MONTH));
		
		c.setTime(td.endDate);
		assertEquals(1, c.get(Calendar.DATE));
		assertEquals(4, c.get(Calendar.MONTH));
	}
	/*
	 * This test check whether the inital date assumption is right.
	 */
	public void testInit1()
	{
		Calendar c = Calendar.getInstance();
		c.set(2008, 4, 1);
	
		ArrayList<Predecessor> preds = new ArrayList<Predecessor>();
		//preds.add(new Predecessor(1, 1,DepType.FS));
		TaskDetails td = pm.getSchedule(null, null, null, preds, null, true);
		
		c.setTime(td.startDate);
		
		Calendar c1 = Calendar.getInstance();
		assertEquals(c1.get(Calendar.DATE), c.get(Calendar.DATE));
		assertEquals(c1.get(Calendar.MONTH), c.get(Calendar.MONTH));
		
		c.setTime(td.endDate);
		assertEquals(c1.get(Calendar.DATE), c.get(Calendar.DATE));
		assertEquals(c1.get(Calendar.MONTH), c.get(Calendar.MONTH));
	}
}
