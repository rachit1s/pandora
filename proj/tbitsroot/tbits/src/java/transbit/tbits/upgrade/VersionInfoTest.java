package transbit.tbits.upgrade;

import junit.framework.TestCase;


public class VersionInfoTest extends TestCase{
	public void testVersion1() throws InvalidVersionException
	{
		int i = VersionInfo.compareMajors("6.0", "7");
		assertEquals(-1, i);
	}
	public void testVersion2() throws InvalidVersionException
	{
		int i = VersionInfo.compareMajors("7", "6.1");
		assertEquals(1, i);
	}
	public void testVersion3() throws InvalidVersionException
	{
		int i = VersionInfo.compareMajors("6.0.1", "6.0.1");
		assertEquals(0, i);
	}
	public void testVersion4() throws InvalidVersionException
	{
		int i = VersionInfo.compareMajors("6.0", "6.0.1");
		assertEquals(-1, i);
	}
	public void testVersion5() throws InvalidVersionException
	{
		int i = VersionInfo.compareMajors("6.0.1", "6.0");
		assertEquals(1, i);
	}
}
