package corrGeneric.com.tbitsGlobal.server;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

import transbit.tbits.domain.User;

//import corrGeneric.com.tbitsGlobal.server.objects.OnBehalf;
import corrGeneric.com.tbitsGlobal.shared.domain.OnBehalfEntry;
import corrGeneric.com.tbitsGlobal.shared.objects.OnBehalfClient;

public class ClientUtility 
{
//	public static OnBehalfClient createFromOnBehalf(ArrayList<OnBehalfEntry> ob)
//	{
//		String sysPrefix = ob.getSysPrefix();
//		String userLogin = ob.getUserLogin();
//		String type1 = ob.getType1();
//		String type2 = ob.getType2();
//		String type3 = ob.getType3();
//		
//		ArrayList<String> onBehalfUsers = new ArrayList<String>(ob.getOnBehalfUsers().size());
//		
//		for( User user : ob.getOnBehalfUsers() )
//		{
//			onBehalfUsers.add(user.getUserLogin());
//		}
//		
//		return new OnBehalfClient(sysPrefix, userLogin, type1, type2, type3, onBehalfUsers);
//	}
	
	public static HashMap<String,HashMap<String,HashMap<String,Collection<String>>>> getOnBehalfMap(ArrayList<OnBehalfEntry> obArr )
	{
		if( null == obArr )
			return null;
		
		HashMap<String,HashMap<String,HashMap<String,Collection<String>>>> map = new HashMap<String,HashMap<String,HashMap<String,Collection<String>>>>();
		
		for( OnBehalfEntry ob : obArr )
		{
			String type1 = ob.getType1();
			String type2 = ob.getType2();
			String type3 = ob.getType3();
//			Collection<User> users = ob.getOnBehalfUsers() ;
			
			HashMap<String,HashMap<String,Collection<String>>> map2 = map.get(type1);
			if( null == map2 )
				map2 = new HashMap<String,HashMap<String,Collection<String>>>();
			
			HashMap<String,Collection<String>> map3 = map2.get(type2);
			if( null == map3 )
				map3 = new HashMap<String,Collection<String>>();
			
			Collection<String> userLogins = map3.get(type3);
			if( null == userLogins )
				userLogins = new ArrayList<String>();
			
//			for( User user : users )
//			{
//				userLogins.add(user.getUserLogin());
//			}
			userLogins.add(ob.getOnBehalfUser());
			map3.put(type3, userLogins);
			map2.put(type2, map3);
			map.put(type1, map2);
		}
		
		return map;
	}
}
