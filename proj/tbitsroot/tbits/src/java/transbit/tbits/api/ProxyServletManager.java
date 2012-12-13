package transbit.tbits.api;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;

import javax.servlet.http.HttpServletResponse;

import transbit.tbits.common.TBitsLogger;
import transbit.tbits.plugin.PluginManager;
import transbit.tbits.Helper.TBitsConstants;

/**
 * 
 * @author nitiraj
 *
 */
/** This class is used to keep the information about the proxy Servlet ( the classes which
 *  implements the IProxyServlet interface). Every plugin which is a proxy servlet should
 *  register to this manager in its static code with its servlet-name servlet-class mapping.
 *  The ProxyServlet.java will query this Manager to get information about a particular 
 *  servlet.
 */
public class ProxyServletManager 
{
	private static final String I_PROXY_SERVLET = "transbit.tbits.api.IProxyServlet";
	public static final TBitsLogger LOG = TBitsLogger.getLogger(TBitsConstants.PKG_API);
	/** 
	 * make it a singleton
	 */
	private Hashtable<String,Class> servletMapping = new Hashtable<String,Class>() ; 
	private ProxyServletManager() {}
	private static ProxyServletManager myInstance = null ; 
	
	/**
	 * 
	 * @return create (if required) and return the instance of the ProxyServletManager 
	 */
	public synchronized static ProxyServletManager getInstance()
	{
		if( null == myInstance )
		{
			myInstance = new ProxyServletManager() ;
			myInstance.loadPluginServlets() ;
			LOG.info("Created an instance of ProxyServletManager") ;
		}
		
		LOG.info("send an instance of ProxyServletManager") ;
		return myInstance ;
	}
	
	/**
	 * Load the plugins which implements the "transbit.tbits.api.IProxyServlet" class
	 * and caches their Class objects for easy retrieval later. 
	 */
	public void loadPluginServlets() // should this be synchronized ? 
	{
		PluginManager pm = PluginManager.getInstance() ;
		if( null != pm ) 
		{
			ArrayList<Class> list = pm.findPluginsByInterface(I_PROXY_SERVLET) ;
			
			for( Iterator<Class> iter = list.iterator() ; iter.hasNext() ; )
			{
				Class klass = iter.next() ;
				try {
					IProxyServlet ips = (IProxyServlet) klass.newInstance() ;
					String servletName = ips.getName() ;
					servletMapping.put(servletName, klass ) ;
				} catch (InstantiationException e) {
					
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					
					e.printStackTrace();
				}
				catch ( ClassCastException e )
				{
					e.printStackTrace() ; // ignore
				}
			}
		}
	}

	/**
	 * Tries to find the plugin in local-cache. If not found loads all the proxy-servlet plugins again
	 * 
	 * @param servletName : name of the servlet by which it should be called
	 * @return the IProxyServlet instance of the plugin servlet, or NULL if the class not found or other error
	 */
	public synchronized IProxyServlet getServlet( String servletName ) // is this synchronised required ? it is hashtable is already synchronized
	{
		LOG.info("Request for '" + servletName + "' received.") ;
		Class servletClass = servletMapping.get(servletName) ;
		if( null == servletClass )
		{
			// load again and then try
			LOG.info( "Cannot find the plugin servlet in cache. So loading again and retrying.") ;
			loadPluginServlets() ;
			
			servletClass = servletMapping.get(servletName) ;
			if( null == servletClass )
			{
				LOG.info("Still cannot find the class name for servlet-name = " + servletName ) ;
				return null ;
			}						
		}
		
		IProxyServlet ips = null ;
		
		// 5. : this may throw class cast exception
		try {
			ips = (IProxyServlet) servletClass.newInstance() ;
		} 
		catch (Exception e) 
		{
			e.printStackTrace();		
			return null ;				
		}
		return ips ;
	}
		
}
