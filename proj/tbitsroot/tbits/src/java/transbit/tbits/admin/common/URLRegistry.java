package transbit.tbits.admin.common;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;

import javax.servlet.http.HttpServlet;

/*
 * A singleton class that maintains the url registry.
 * It essentially is a hashmap <HttpServlet parent, ArrayList<Tuple(Mapping, MappingServletClass)> mappings>
 * The proxy class can find out what the various 
 */
public class URLRegistry {
	private static URLRegistry instance = null;
	public static URLRegistry getInstance()
	{
		if(instance == null)
			instance = new URLRegistry();
		return instance;
	}
	
	Hashtable< Class<? extends HttpServlet>, Hashtable<String, Class<? extends HttpServlet>> > registry;
	private URLRegistry()
	{
		 registry = new Hashtable< Class<? extends HttpServlet>, Hashtable<String, Class<? extends HttpServlet>> >();
	}
	
	public void addMapping(Class<? extends HttpServlet> parent, String url, Class<? extends HttpServlet> childClass)
	{
		Hashtable<String, Class<? extends HttpServlet>> subMappings;
		if(!registry.containsKey(parent))
		{
			subMappings = new Hashtable<String, Class<? extends HttpServlet>>();
			registry.put(parent, subMappings);
		}
		else
			subMappings = registry.get(parent);
		subMappings.put(url, childClass);
	}
	
	public Hashtable<String, Class<? extends HttpServlet>> getMappingTuple(Class<? extends HttpServlet> parent)
	{
		return registry.get(parent);
	}
}
