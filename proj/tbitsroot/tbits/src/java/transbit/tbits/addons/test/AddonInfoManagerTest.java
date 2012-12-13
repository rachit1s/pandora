/**
 * 
 */
package transbit.tbits.addons.test;

import transbit.tbits.addons.AddonException;
import transbit.tbits.addons.AddonInfo;
import transbit.tbits.addons.AddonInfoManager;
import transbit.tbits.addons.AddonInfoWithBytes;
import transbit.tbits.exception.PersistenceException;

/**
 * @author Nitiraj Singh Rathore ( nitiraj.r@tbitsglobal.com )
 *
 */
public class AddonInfoManagerTest {
	public static void main(String argv[])
	{
		AddonInfoWithBytes addonInfo = new AddonInfoWithBytes(0, "jarName", AddonInfo.STATUS_UPLOADED, new byte[33]);
		try 
		{
			AddonInfo naddinfo = AddonInfoManager.getInstance().persistAddonInfoWithBytes(addonInfo);
			System.out.println("addons : " + addonInfo);
			naddinfo.setJarName("333");
			AddonInfo xaddinfo = AddonInfoManager.getInstance().persistAddonInfo(naddinfo);
			System.out.println("xaddoninfo : " + xaddinfo);
			AddonInfoManager.getInstance().delete(xaddinfo);
			
		} catch (PersistenceException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
