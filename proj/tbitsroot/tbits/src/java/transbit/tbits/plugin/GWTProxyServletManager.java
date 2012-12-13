package transbit.tbits.plugin;

import java.util.Hashtable;
import java.util.Set;

public class GWTProxyServletManager {
	private static GWTProxyServletManager manager;
	
	private Hashtable<String, Class<? extends TbitsRemoteServiceServlet>> map;
	
	private GWTProxyServletManager(){
		map = new Hashtable<String, Class<? extends TbitsRemoteServiceServlet>>();
	}
	
	public static GWTProxyServletManager getInstance(){
		if(manager == null)
			manager = new GWTProxyServletManager();
		return manager;
	}
	
	public void subscribe(String service, Class<? extends TbitsRemoteServiceServlet> className){
		map.put(service, className);
	}
	
	public Class<? extends TbitsRemoteServiceServlet> getServlet(String service){
		return map.get(service);
	}
	
	public Set<String> getUrls(){
		return map.keySet();
	}
}
