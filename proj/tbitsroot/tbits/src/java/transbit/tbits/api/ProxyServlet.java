package transbit.tbits.api;

import static transbit.tbits.Helper.TBitsConstants.PKG_WEBAPPS;

import java.io.IOException;
import java.util.Date;
import java.util.Hashtable;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import transbit.tbits.common.DatabaseException;
import transbit.tbits.common.TBitsLogger;
import transbit.tbits.common.Utilities;
import transbit.tbits.config.WebConfig;
import transbit.tbits.domain.User;
import transbit.tbits.exception.TBitsException;
import transbit.tbits.plugin.GWTProxyServlet;
import transbit.tbits.plugin.PluginManager;
import transbit.tbits.webapps.TBitsError;
import transbit.tbits.webapps.WebUtil;

/**
 * 
 * @author nitiraj
 *
 */
/**
 * The purpose of this proxy servlet is to receive a request with an extra information
 * about to which plugin servlet should this request be forwarded.
 * This servlet will load that servlet and redirect the request to it. 
 * The URL for the request should be of the following format
 * 
 * http://host:port/proxy/servlet-name[/? ][.........]
 * where "proxy" : is the binding of this "ProxyServlet" class in the build.xml
 * and "servlet-name" is the name of the serlvet to be called. The class
 * binding for the plugin servlet is maintained in a hashtable in singleton class called : ProxyServletManager
 * 
 * I'm not maintaining any other information about the plugin servlet
 * 
 * Any exception occuring in the plugin servlet should be handled in the plugin serlvet
 * it self. or the plugin servlet should decide by itself what has to be done ( redirection etc. )
 */
public class ProxyServlet extends HttpServlet {
	
	private static final long serialVersionUID = 1L;
	private static final TBitsLogger LOG = TBitsLogger.getLogger(PKG_WEBAPPS);
	public static final String PROXY_SERVLET_MAPPING = "proxy";
	/**
	 * extracts the servlet-name from the request URL. 
	 * 1. get the pathInfo ( /.... ) 
	 * 2. check for next / if not found check for ? if not found take name till the end 
	 *  
	 * @param request
	 * @return null if malformed URL or returns the name of the plugin servlet called
	 */
	public String getServletName( HttpServletRequest request )
	{
		String pathInfo = request.getPathInfo() ;
		// pathInfo type : 1 : ../servlet-name/.. 2 : ../servlet-name? ... 3 : .../sevlet-name
		if( null == pathInfo || pathInfo.trim().equals("") ||  !pathInfo.startsWith("/") )
			return null ;
		
		pathInfo = pathInfo.substring(1) ;
		if( null == pathInfo || pathInfo.trim().equals("") )
			return null ;
		
		int index = pathInfo.indexOf('/') ;
		if( index <= 0 )
		{
			index = pathInfo.indexOf('?') ;
			if( index <= 0 )
			{
				index = pathInfo.length() ; // here index will never be 0 as we have already handeled this				
			}
		}
		
		String servName = pathInfo.substring(0,index) ;
		return servName ;
		
	}
	
	/**
	 * 1. receive a request.
	 * 1.1 . authenticate the user
     */	
	public void doGet(HttpServletRequest request, HttpServletResponse response ) throws ServletException, IOException
	{
		// TODO: ask why do we register MDCparams ?
		 Utilities.registerMDCParams(request);
		 HttpSession session = request.getSession();		
		try {
		
				try
				{
					User user = WebUtil.validateUser(request);
			
		        } catch (DatabaseException de) {
	//	        	response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Database Exception : " + de.getMessage() ) ;
		        	de.printStackTrace() ;
		        	session.setAttribute(TBitsError.EXCEPTION_OBJECT, de ) ;
		        	response.sendRedirect(WebUtil.getServletPath(request,"/error")) ;
		            return;
		        } catch (TBitsException de) {
		        	//response.sendError( HttpServletResponse.SC_UNAUTHORIZED, "User not Authenticated") ;
		        	de.printStackTrace() ;
		        	session.setAttribute(TBitsError.EXCEPTION_OBJECT, de) ;
		        	response.sendRedirect(WebUtil.getServletPath(request,"/error")) ;
		            return;
		        }
		        
		        try
		        {
			        long start = new Date().getTime();
		
		            handleGetRequest(request, response);
		
		            long end = new Date().getTime();
		
		            LOG.debug("Time taken: " + (end - start));
		        }
		        catch( Exception e )
		        {
		        	e.printStackTrace() ;
		        	session.setAttribute(TBitsError.EXCEPTION_OBJECT, e) ;
		        	response.sendRedirect(WebUtil.getServletPath(request, "/error")) ;
		        	return ;
		        }
		        
		}finally {
            Utilities.clearMDCParams();
        }
	}
	
	/**
	 * 1. receive a request.
	 * 1.1 . authenticate the user
     */
	public void doPost( HttpServletRequest request, HttpServletResponse response ) throws ServletException, IOException 
	{
		
		// TODO: ask why do we register MDCparams ?
		 Utilities.registerMDCParams(request);
		 HttpSession session = request.getSession();
		// 1.1 
		try
		{
			try {
				User user = WebUtil.validateUser(request);
		
			} catch (DatabaseException de) {
//	        	response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Database Exception : " + de.getMessage() ) ;
	        	de.printStackTrace() ;
	        	session.setAttribute(TBitsError.EXCEPTION_OBJECT, de ) ;
	        	response.sendRedirect(WebUtil.getServletPath(request,"/error")) ;
	            return;
	        } catch (TBitsException de) {
	        	//response.sendError( HttpServletResponse.SC_UNAUTHORIZED, "User not Authenticated") ;
	        	de.printStackTrace() ;
	        	session.setAttribute(TBitsError.EXCEPTION_OBJECT, de) ;
	        	response.sendRedirect(WebUtil.getServletPath(request,"/error")) ;
	            return;
	        }
	      
	        try
	        {
		  
				long start = new Date().getTime();
			
			    handlePostRequest(request, response);
			
			    long end = new Date().getTime();
			
			    LOG.debug("Time taken: " + (end - start));
	        }
	        catch( Exception e )
	        {
	        	e.printStackTrace() ;
	        	session.setAttribute(TBitsError.EXCEPTION_OBJECT, e) ;
	        	response.sendRedirect(WebUtil.getServletPath(request, "/error")) ;
	        	return ;
	        }
	        
		}
		
       finally {
           Utilities.clearMDCParams();
       }
	}
	
	/**
	 * STEPS : 
	 * 2. extract the name of the plugin servlet to call 
	 * 3. get the class from PluginServletManager 
	 * 4. call their doPost method
	 * @throws Exception 
	 */
	private void handlePostRequest(HttpServletRequest request, HttpServletResponse response) throws Exception  
	{
		// 2.
		String servletName = getServletName(request) ;
		if( null == servletName )
		{
//			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Malformed URL.") ;
//			return ;
			throw new Exception("Illegal call to the proxy-servlet. Malformed URL : " + request.getRequestURI()) ;
		}

		LOG.info("ProxyServlet : handlePost : servletName = " + servletName ) ;
		IProxyServlet ips = ProxyServletManager.getInstance().getServlet(servletName);
		// 3.
		if( null != ips )
			ips.doPost(request, response) ;		
		else
		{
//			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "The servlet with name(" + servletName + ") not found.") ;
//			return ;
			throw new Exception("The servlet with name(" + servletName + ") not found.") ;
		}	
	}
	
	/**
	 * STEPS :
	 * 2. extract the name of the plugin servlet to call 
	 * 3. get the class from PluginServletManager 
	 * 4. call their doGet method	 
	 * @throws Exception 
	 */
	private void handleGetRequest(HttpServletRequest request, HttpServletResponse response) throws  Exception
	{				
		// 2.
		String servletName = getServletName(request) ;
		if( null == servletName )
		{
//			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Malformed URL.") ;
//			return ;
			throw new Exception("Illegal call to the proxy-servlet. Malformed URL : " + request.getRequestURI()) ;
		}

		LOG.info("ProxyServlet : handlePost : servletName = " + servletName ) ;
		IProxyServlet ips = ProxyServletManager.getInstance().getServlet(servletName);
		// 3.
		if( null != ips )
			ips.doGet(request, response) ;		
		else
		{		
			throw new Exception("The servlet with name(" + servletName + ") not found.") ;			
		}
	}

}
