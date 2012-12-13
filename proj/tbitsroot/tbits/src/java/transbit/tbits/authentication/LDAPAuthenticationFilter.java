/*
 * AuthenticationFilter.java
 *
 * Created on September 13, 2006, 10:23 PM
 */

package transbit.tbits.authentication;

import java.io.*;
import java.sql.SQLException;

import javax.servlet.*;
import javax.servlet.http.*;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import transbit.tbits.common.DatabaseException;
import transbit.tbits.domain.User;

/**
 *
 * @author  Administrator
 * @version
 */

public class LDAPAuthenticationFilter implements Filter {
    
    // The filter configuration object we are associated with.  If
    // this value is null, this filter instance is not currently
    // configured.
    private FilterConfig filterConfig = null;
    
    public LDAPAuthenticationFilter() {
        
    }
    
    private void doBeforeProcessing(RequestWrapper request, ResponseWrapper response)
    throws IOException, ServletException {
        if (debug) log("InternalAuthenticationFilter:DoBeforeProcessing");
    }
    
    private void doAfterProcessing(RequestWrapper request, ResponseWrapper response)
    throws IOException, ServletException {
        if (debug) log("InternalAuthenticationFilter:DoAfterProcessing");
    }
    
    private String encoding;
	static final String REDIRECTION_URL_KEY = "redirectionurl";
    
    /**
     *
     * @param request The servlet request we are processing
     * @param result The servlet response we are creating
     * @param chain The filter chain we are processing
     *
     * @exception IOException if an input/output error occurs
     * @exception ServletException if a servlet error occurs
     */
    public void doFilter(ServletRequest request, ServletResponse response,
            FilterChain chain)
            throws IOException, ServletException {
        if (debug) log("InternalAuthenticationFilter:doFilter()");
        
        String isFilterEnabled = filterConfig.getInitParameter("enabled");
        
    	// Respect the client-specified character encoding
		// (see HTTP specification section 3.4.1)
		if (null == request.getCharacterEncoding())
			request.setCharacterEncoding(encoding);

		/**
		 * Set the default response content type and encoding
		 */
		response.setContentType("text/html; charset=UTF-8");
		response.setCharacterEncoding("UTF-8");


        if((isFilterEnabled != null) &&  (isFilterEnabled.equals("false"))) {
            chain.doFilter(request, response);
        } else {
            /*
            * user requests for page
            * check if the user is authenticated by getting username from session
            * if user is authenticated
            *   go to the request page.
            * else
            * if user is authenticating, then verify the username and password. 
            *   if username is correct Set username in session. and 
            * else set the header to authenticating and send the login page.
             */
            
            HttpServletRequest req = (HttpServletRequest) request;
            HttpServletResponse res = (HttpServletResponse) response;
            HttpSession session = req.getSession(true);
            
            Throwable problem = null;
            String user = null;
            Object userObj = session.getAttribute("user");
            if(userObj != null )
                user = (String) userObj;
            if(user == null)
            {
            	// check the authentication done by waffle or other such lib.
            	String ldapUser = req.getRemoteUser();
            	if( null != ldapUser )
            	{
            		User tbitsUser = null;
					try {
						tbitsUser = validateLdapUser( ldapUser);
					} catch (Exception e) {
						e.printStackTrace();
						sendProcessingError(new Exception("An exception occurred while authenticating user '" + ldapUser + "'. Please contact your Administrator." ), res);
						return;
					}
            		if( null == tbitsUser )
            		{
            			sendProcessingError(new Exception("LDAP user with identifier : '" + ldapUser + "' cannot be mapped to any TBITS user. Please contact your administrator."), res) ;
            			return;
            		}
            		else
            		{
            			session.setAttribute("user", tbitsUser.getUserLogin());
            		}
            	}
            	else
            	{
	            	String persistUrlStr = req.getParameter("persist_url");
	            	if ((persistUrlStr == null) || (persistUrlStr.trim().equals("")) || (persistUrlStr.trim().equals("true"))){	
	                    String actualuri = req.getRequestURI()  + ( req.getQueryString() == null ? "" : "?" + req.getQueryString() ) ;                    
	                    session.setAttribute(AuthConstants.ACTUAL_REQUEST, actualuri);
	                    RequestDispatcher loginRd = session.getServletContext().getRequestDispatcher("/loginpage");
	                    req.setAttribute(REDIRECTION_URL_KEY, actualuri);
	                    //res.sendRedirect(WebUtil.getServletPath(req, "/loginpage"));  
	                    loginRd.forward(req, res);
	                    //loginRd.forward(request, response);
	                    return;
	            	}
            	}
            }
            //
            // Create wrappers for the request and response objects.
            // Using these, you can extend the capabilities of the
            // request and response, for example, allow setting parameters
            // on the request before sending the request to the rest of the filter chain,
            // or keep track of the cookies that are set on the response.
            //
            // Caveat: some servers do not handle wrappers very well for forward or
            // include requests.
            //
            RequestWrapper  wrappedRequest  = new RequestWrapper(req, user);
            wrappedRequest.setAuthType(req.getAuthType());
            ResponseWrapper wrappedResponse = new ResponseWrapper(res);
            
//            String remoteURI = wrappedRequest.getRequestURI();
//            if(!remoteURI.contains("/jaguar")){
//            	remoteURI = parseURI(remoteURI);
//            	if(remoteURI != null){
//            		res.sendRedirect(WebUtil.getServletPath(req, remoteURI));
//            		return;
//            	}
//            }
            
            doBeforeProcessing(wrappedRequest, wrappedResponse);
            
            try {
                chain.doFilter(wrappedRequest, wrappedResponse);
            } catch(Throwable t) {
                //
                // If an exception is thrown somewhere down the filter chain,
                // we still want to execute our after processing, and then
                // rethrow the problem after that.
                //
                problem = t;
                t.printStackTrace();
            }
            
            doAfterProcessing(wrappedRequest, wrappedResponse);
            
            //
            // If there was a problem, we want to rethrow it if it is
            // a known type, otherwise log it.
            //
            if (problem != null) {
                if (problem instanceof ServletException) throw (ServletException)problem;
                if (problem instanceof IOException) throw (IOException)problem;
                sendProcessingError(problem, response);
            }
        }
    }
    
    private User validateLdapUser(String ldapUser) throws SQLException, DatabaseException 
    {
		// TODO change it.
//    	return ldapUser.substring(ldapUser.indexOf("\\") + 1, ldapUser.length() );
    	return UserAliasManager.getTbitsUserForLdapLogin(ldapUser);
	}

	public String parseURI(String remoteURI){
    	if(remoteURI.contains("search/"))
    		remoteURI = remoteURI.replace("search/", "jaguar/#ba=");
    	else if(remoteURI.contains("my-requests"))
    		remoteURI = "/jaguar/";
    	else return null;
    	return remoteURI;
    }
    
    
    /**
     * Return the filter configuration object for this filter.
     */
    public FilterConfig getFilterConfig() {
        return (this.filterConfig);
    }
    
    
    /**
     * Set the filter configuration object for this filter.
     *
     * @param filterConfig The filter configuration object
     */
    public void setFilterConfig(FilterConfig filterConfig) {
        this.filterConfig = filterConfig;
    }
    
    /**
     * Destroy method for this filter
     *
     */
    public void destroy() {
    }
    
    
    /**
     * Init method for this filter
     *
     */
    public void init(FilterConfig filterConfig) {
        
        this.filterConfig = filterConfig;
        if (filterConfig != null) {
            if (debug) {
                log("InternalAuthenticationFilter: Initializing filter");
            }
        }
        encoding = filterConfig.getInitParameter("requestEncoding");
        if( encoding==null ) encoding="UTF-8";
    }
    
    /**
     * Return a String representation of this object.
     */
    public String toString() {
        
        if (filterConfig == null) return ("InternalAuthenticationFilter()");
        StringBuffer sb = new StringBuffer("InternalAuthenticationFilter(");
        sb.append(filterConfig);
        sb.append(")");
        return (sb.toString());
    
    }
    
    private void sendProcessingError(Throwable t, ServletResponse response) {
        
//        String stackTrace = getStackTrace(t);
        
//        if(stackTrace != null && !stackTrace.equals("")) {
            
            try {
                
                response.setContentType("text/html");
                PrintStream ps = new PrintStream(response.getOutputStream());
                PrintWriter pw = new PrintWriter(ps);
                pw.print("<html>\n<head>\n<title>Error</title>\n</head>\n<body>\n"); //NOI18N
                
                // PENDING! Localize this for next official release
                pw.print("<h1>The resource did not process correctly</h1>\n<pre>\n");
                pw.print(t.getMessage());
                pw.print("</pre></body>\n</html>"); //NOI18N
                pw.close();
                ps.close();
                response.getOutputStream().close();;
            }
            
            catch(Exception ex){ }
//        } else {
//            try {
//                PrintStream ps = new PrintStream(response.getOutputStream());
//                t.printStackTrace(ps);
//                ps.close();
//                response.getOutputStream().close();;
//            } catch(Exception ex){ }
//        }
    }
    
    public static String getStackTrace(Throwable t) {
        
        String stackTrace = null;
        
        try {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            t.printStackTrace(pw);
            pw.close();
            sw.close();
            stackTrace = sw.getBuffer().toString();
        } catch(Exception ex) {}
        return stackTrace;
    }
    
    public void log(String msg) {
        filterConfig.getServletContext().log(msg);
    }
    
    private static final boolean debug = false;
}


