package transbit.tbits.api;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 
 * @author nitiraj
 *
 */
/**
 * This interface acts as a Plugin servlet. Implementing classes will override the 
 * doGet() and doPost methods and we can call them after loading them
 */
public interface IProxyServlet 
{
	/**
	 *  * doGet method should be overriden to handle the doGet method of the servlet
	 * @param request
	 * @param response
	 * @throws ServletException
	 * @throws IOException
	 */
	public void doGet( HttpServletRequest request , HttpServletResponse response ) throws ServletException, IOException ;
	
	/**
	 * doPost method should be overriden to handle the doPost method of the servlet
	 * @param request
	 * @param response
	 * @throws ServletException
	 * @throws IOException
	 */
	public void doPost( HttpServletRequest request , HttpServletResponse response ) throws ServletException, IOException ;
	
	/**
	 * 
	 * @return the name of the plugin servlet (servlet-name) by which it should be called. 
	 */
	public String getName() ; 
	
}
