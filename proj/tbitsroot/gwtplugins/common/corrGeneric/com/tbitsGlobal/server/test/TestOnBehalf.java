package corrGeneric.com.tbitsGlobal.server.test;

import java.util.ArrayList;

import corrGeneric.com.tbitsGlobal.server.cache.OnBehalfTableCache;
import corrGeneric.com.tbitsGlobal.server.managers.OnBehalfManager;
import corrGeneric.com.tbitsGlobal.shared.domain.OnBehalfEntry;
import corrGeneric.com.tbitsGlobal.shared.key.OnBehalfKey;
import corrGeneric.com.tbitsGlobal.shared.objects.CorrException;

public class TestOnBehalf 
{
	public static void main(String argv[]) throws CorrException
	{
		try
		{
			OnBehalfManager c =  OnBehalfManager.getInstance();
			OnBehalfTableCache tabCache = c.getOnBehalfTableCache();
//			OnBehalfUserCache userCache = c.getOnBehalfUserCache();

			ArrayList<OnBehalfEntry> uc5 = tabCache.get(new OnBehalfKey("tbits1", "root"));
			System.out.println("userCache = " + uc5);
			
//			ArrayList<OnBehalf> uc1 = userCache.get(new OnBehalfKey("tbits2", "dc_desein"));
//			System.out.println("userCache = " + userCache);
//			ArrayList<OnBehalf> uc2 = userCache.get(new OnBehalfKey("tbits2", "dc_desein"));
//			System.out.println("userCache = " + userCache);
//			ArrayList<OnBehalf> uc4 = userCache.get(new OnBehalfKey("tbits3", "dc_desein"));
//			System.out.println("userCache = " + userCache);
//			userCache.get(new OnBehalfKey("tbits3", "dc_desein"));
//			System.out.println("userCache = " + userCache);
//			userCache.get(new OnBehalfKey("tbits3", "dc_desein"));
//			System.out.println("userCache = " + userCache);
//			userCache.get(new OnBehalfKey("tbits3", "dc_desein"));
//			System.out.println("userCache = " + userCache);
//			ArrayList<OnBehalf> uc6 = userCache.get(new OnBehalfKey("tbits1", "root"));
//			System.out.println("userCache = " + userCache);
//			ArrayList<OnBehalf> uc7 = userCache.get(new OnBehalfKey("tbits1", "root"));
//			System.out.println("userCache = " + userCache);
//			ArrayList<OnBehalf> uc8 = userCache.get(new OnBehalfKey("tbits1", "root"));
//			System.out.println("userCache = " + userCache);
//			ArrayList<OnBehalf> uc9 = userCache.get(new OnBehalfKey("tbits4", "dc_desein"));
//			System.out.println("userCache = " + userCache);
//			ArrayList<OnBehalf> uc3 = userCache.get(new OnBehalfKey("tbits5", "dc_desein"));
//			System.out.println("userCache = " + userCache);
//			System.out.println("tabCache = " + tabCache);
//			System.out.println("userCache = " + userCache);
//			
//			userCache.get(new OnBehalfKey("tits", "root"));
//			System.out.println("tabCache = " + tabCache);
//			System.out.println("userCache = " + userCache);
			
//			System.out.println("tabCache = " + tabCache);
//			tabCache.get("tbits1");
//			System.out.println("tabCache = " + tabCache);
		}
		finally
		{
			
		}		
	}
}
