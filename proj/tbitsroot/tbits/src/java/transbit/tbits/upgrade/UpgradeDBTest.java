package transbit.tbits.upgrade;

import java.io.File;
import java.io.IOException;

import junit.framework.TestCase;

public class UpgradeDBTest extends TestCase {
	public void testFileReader()
	{
		String fileL = "/Users/sandeepgiri/tbits/dist/build/db/upgrades/6.0.32/mssql/common/1.tbits_quartz_sqlServer.sql";
		try {
			System.out.println(FileUtils.getContents(new File(fileL)));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
