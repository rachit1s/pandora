package pm;
import java.util.ArrayList;



import junit.framework.TestCase;


public class PredeccesorParserTest extends TestCase {

	public void testParse1() {
		ArrayList<Predecessor> p = PredeccesorParser.parse("2");
		assertEquals(1, p.size());
		assertEquals(2, p.get(0).taskId);
	}
	public void testParse2()
	{
		ArrayList<Predecessor> p = PredeccesorParser.parse("2 SS");
		
		assertEquals(1, p.size());
		assertEquals(2, p.get(0).taskId);
		assertEquals(DepType.SS, p.get(0).depType);
		
	}
	public void testParse3()
	{
		ArrayList<Predecessor> p = PredeccesorParser.parse("2 SF + 30.4");
		
		assertEquals(1, p.size());
		assertEquals(2, p.get(0).taskId);
		assertEquals(DepType.SF, p.get(0).depType);
		assertEquals(30, p.get(0).lag);
	}
	public void testParse4()
	{
		ArrayList<Predecessor> p = PredeccesorParser.parse("2 SF   +   30.4");
		
		assertEquals(1, p.size());
		assertEquals(2, p.get(0).taskId);
		assertEquals(DepType.SF, p.get(0).depType);
		assertEquals(30, p.get(0).lag);
	}
	public void testParse5()
	{
		ArrayList<Predecessor> p = PredeccesorParser.parse("22981799  			SS 			  -   			30.40505 lkljlkj");
		
		assertEquals(1, p.size());
		assertEquals(22981799, p.get(0).taskId);
		assertEquals(DepType.SS, p.get(0).depType);
		assertEquals(-30, p.get(0).lag);
	}
	public void testParse6()
	{
		ArrayList<Predecessor> p = PredeccesorParser.parse("");
		assertEquals(0, p.size());
	}
	public void testParse7()
	{
		ArrayList<Predecessor> p = PredeccesorParser.parse("  \t  ");
		assertEquals(0, p.size());
	}
	public void testParse8()
	{
		ArrayList<Predecessor> p = PredeccesorParser.parse(" ");
		assertEquals(0, p.size());
	}
	public void testParse9()
	{
		ArrayList<Predecessor> p = PredeccesorParser.parse("    90   ");
		assertEquals(1, p.size());
		assertEquals(90, p.get(0).taskId);
	}
	public void testParse10()
	{
		ArrayList<Predecessor> p = PredeccesorParser.parse("90   ");
		assertEquals(1, p.size());
		assertEquals(90, p.get(0).taskId);
	}
	public void testParse11()
	{
		ArrayList<Predecessor> p = PredeccesorParser.parse("1FS+0");
		assertEquals(1, p.size());
		assertEquals(1, p.get(0).taskId);
		assertEquals(DepType.FS, p.get(0).depType);
		assertEquals(0, p.get(0).lag);
	}
	public void testParse12()
	{
		ArrayList<Predecessor> p = PredeccesorParser.parse("");
		assertEquals(0, p.size());
	}
}
