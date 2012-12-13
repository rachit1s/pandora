package corrGeneric.com.tbitsGlobal.server.managers;

import java.util.Enumeration;
import java.util.Hashtable;

public class ManagerRegistry 
{
	private Hashtable<Class<? extends AbstractManager>,AbstractManager> managers = null;
	private static ManagerRegistry instance = null;
	private ManagerRegistry()
	{
		managers = new Hashtable<Class<? extends AbstractManager>, AbstractManager>();
	}
	
	public synchronized static ManagerRegistry getInstance()
	{
		if( null == instance )
			instance = new ManagerRegistry();
		
		return instance;
	}
	
	public <T extends AbstractManager> T getManager( Class<T> c )
	{
		return (T) managers.get(c);
	}
	
	public <T extends AbstractManager> void registerManager( Class<T> c, T t)
	{
		managers.put(c, t);
	}
	
	public <T extends AbstractManager> T unRegisterManager( Class<T> c )
	{
		T t = (T) managers.get(c);
		managers.remove(c);
		return t;
	}
	
	public String clearCaches(Class<?> c)
	{
		try
		{			
			AbstractManager am = managers.get(c);
			am.refresh();
			return c + " : cache cleared successfully.";
		}
		catch(Exception e)
		{
			e.printStackTrace();
			return c + " : Failed cache clear.";
		}
	}
	
	/**
	 * 
	 * @return a message about the caches cleared.
	 */
	public String clearCaches()
	{
		StringBuffer sb = new StringBuffer();
		for( Enumeration<Class<? extends AbstractManager>> keys = managers.keys() ; keys.hasMoreElements() ;)
		{
			Class<?> c = keys.nextElement() ;
			String msg = this.clearCaches(c);
			sb.append(msg + "\n");
		}
		
		return sb.toString();
	}
}
