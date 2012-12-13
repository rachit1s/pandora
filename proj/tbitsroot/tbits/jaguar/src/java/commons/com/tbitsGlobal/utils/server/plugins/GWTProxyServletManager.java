package commons.com.tbitsGlobal.utils.server.plugins;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Set;

import transbit.tbits.plugin.TbitsRemoteServiceServlet;

public class GWTProxyServletManager {
	private static GWTProxyServletManager manager;
	
	private Hashtable<String, Class<? extends TbitsRemoteServiceServlet>> map;
	private List<GWTProxyServletKey> keyList;
	
	private GWTProxyServletManager(){
		map = new Hashtable<String, Class<? extends TbitsRemoteServiceServlet>>();
		keyList = new ArrayList<GWTProxyServletKey>();
	}
	
	public static GWTProxyServletManager getInstance(){
		if(manager == null)
			manager = new GWTProxyServletManager();
		return manager;
	}
	
	public void subscribe(String service, Class<? extends TbitsRemoteServiceServlet> className){
		map.put(service, className);
	}
	
	public void subscribe(String service, String moduleName, Class<? extends TbitsRemoteServiceServlet> className){
		GWTProxyServletKey key = new GWTProxyServletKey(service, moduleName, className);
		keyList.add(key);
	}
	
	public Class<? extends TbitsRemoteServiceServlet> getServlet(String service){
		return map.get(service);
	}
	
	public GWTProxyServletKey getServletForAdmin(String service){
		for(GWTProxyServletKey key : keyList){
			if(key.getServiceClassName().equals(service)){
				return key;
			}
		}
		return null;
	}
	
//	public Set<String> getUrls(){
//		return map.keySet();
//	}
}
