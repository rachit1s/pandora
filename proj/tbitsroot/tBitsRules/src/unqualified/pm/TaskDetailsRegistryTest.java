package pm;
import java.util.HashMap;


import junit.framework.TestCase;


public class TaskDetailsRegistryTest extends TestCase {
	HashMap<String, TaskDetails> registery = new HashMap<String, TaskDetails>();
	HashMap<String, TaskDetails> registeryDB = new HashMap<String, TaskDetails>();
	
	protected void setUp() throws Exception {
		super.setUp();
		
		TaskDetails td = null;
		td = new TaskDetails(null, null, null, null, 1, 1, 0, 1);
		td.predList = PredeccesorParser.parse("");
		registery.put(td.sysId + "-" + td.taskId, td);
		
		td = new TaskDetails(null, null, null, null, 1, 2, 0, 1);
		td.predList = PredeccesorParser.parse("1FS");
		registery.put(td.sysId + "-" + td.taskId, td);
		
		td = new TaskDetails(null, null, null, null, 1, 3, 0, 1);
		td.predList = PredeccesorParser.parse("2FS");
		registery.put(td.sysId + "-" + td.taskId, td);
		
		td = new TaskDetails(null, null, null, null, 1, 4, 0, 1);
		td.predList = PredeccesorParser.parse("2FS,3FS");
		registery.put(td.sysId + "-" + td.taskId, td);
		
		td = new TaskDetails(null, null, null, null, 1, 5, 0, 1);
		td.predList = PredeccesorParser.parse("3FS,1FS");
		registery.put(td.sysId + "-" + td.taskId, td);
		
		td = new TaskDetails(null, null, null, null, 1, 6, 0, 1);
		td.predList = PredeccesorParser.parse("5FS,3FS");
		registery.put(td.sysId + "-" + td.taskId, td);
		
//		registeryDB = TaskDetailsRegistry.getAllTasks();
//		System.out.println(registeryDB);
	}
	
	public void testcheckForCycleDependency1() {
		TaskDetails td = null;
		td = new TaskDetails(null, null, null, null, 1, 1, 0, 1);
		td.predList = PredeccesorParser.parse("6");
		try {
			assertEquals(true, TaskDetailsRegistry.checkForCycleDependency(td, registery));
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void testcheckForCycleDependency2() {
		TaskDetails td = null;
		td = new TaskDetails(null, null, null, null, 1, 4, 0, 1);
		td.predList = PredeccesorParser.parse("6");
		try {
			assertEquals(false, TaskDetailsRegistry.checkForCycleDependency(td, registery));
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void testcheckForCycleDependency3() {
		TaskDetails td = null;
		td = new TaskDetails(null, null, null, null, 1, 4, 0, 1);
		td.predList = PredeccesorParser.parse("6");
		try {
			assertEquals(false, TaskDetailsRegistry.checkForCycleDependency(td, registery));
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void testcheckForCycleDependency4()
	{
		TaskDetails td = null;
		td = new TaskDetails(null, null, null, null, 1, 1, 0, 1);
		td.predList = PredeccesorParser.parse("2");
	//	registery.put("1-1",td);
		try {
			assertEquals(true, TaskDetailsRegistry.checkForCycleDependency(td, registery));
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void testcheckForCycleDependencyDB1()
	{
		TaskDetails td = null;
		td = new TaskDetails(null, null, null, null, 1, 1, 0, 1);
		td.predList = PredeccesorParser.parse("2");
		try {
			assertEquals(true, TaskDetailsRegistry.checkForCycleDependency(td, registeryDB));
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void testcheckForCycleDependencyDB2()
	{
		TaskDetails td = null;
		td = new TaskDetails(null, null, null, null, 1, 3, 0, 1);
		td.predList = PredeccesorParser.parse("1");
		try {
			assertEquals(false, TaskDetailsRegistry.checkForCycleDependency(td, registeryDB));
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void testcheckForCycleDependencyDB3()
	{
		TaskDetails td = null;
		td = new TaskDetails(null, null, null, null, 1, 1, 0, 1);
		td.predList = PredeccesorParser.parse("4");
		try {
			assertEquals(true, TaskDetailsRegistry.checkForCycleDependency(td, registeryDB));
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void testcheckForCycleDependencyDB4()
	{
		TaskDetails td = null;
		td = new TaskDetails(null, null, null, null, 1, 1, 0, 1);
		td.predList = PredeccesorParser.parse("26");
		try {
			assertEquals(true, TaskDetailsRegistry.checkForCycleDependency(td, registeryDB));
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public void testcheckForCycleDependencyDB5()
	{
		TaskDetails td = null;
		td = new TaskDetails(null, null, null, null, 1, 1, 0, 1);
		td.predList = PredeccesorParser.parse("25");
		try {
			assertEquals(true, TaskDetailsRegistry.checkForCycleDependency(td, registeryDB));
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
