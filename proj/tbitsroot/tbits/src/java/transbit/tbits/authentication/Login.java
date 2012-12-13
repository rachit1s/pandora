/*
 * Login.java
 *
 * Created on September 14, 2006, 1:59 AM
 */

package transbit.tbits.authentication;

import java.io.*;
import java.net.URLDecoder;
//import java.net.*;
import javax.servlet.*;
import javax.servlet.http.*;
import transbit.tbits.common.DatabaseException;
import transbit.tbits.common.PropertiesHandler;
import transbit.tbits.common.TBitsLogger;
import transbit.tbits.webapps.WebUtil;

/**
 *
 * @author Administrator
 * @version
 */
public class Login extends HttpServlet {
    
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static final TBitsLogger LOG = TBitsLogger.getLogger("transbit.tbits.authentication") ;
	
	/** Processes requests for both HTTP <code>GET</code> and <code>POST</code> methods.
     * @param request servlet request
     * @param response servlet response
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {
        boolean isSuccessful = false;
                
        HttpSession session = request.getSession();
        //checkUserCredentials(request, response, chain);
        String login = request.getParameter("login");
        String password = request.getParameter("password");
        String rememberMe = request.getParameter("persistent");
        String redirectionUrl = request.getParameter(AuthenticationFilter.REDIRECTION_URL_KEY);
        
        if(redirectionUrl == null)
        	redirectionUrl = WebUtil.getNearestPath(request, "");
        else
        	redirectionUrl = URLDecoder.decode(redirectionUrl, "UTF-8");
        
        if((login != null ) && (password != null)){
            try{
            	if (AuthUtils.validateUser(login, password))
            		isSuccessful = true;
            	
            	if ((rememberMe != null) && rememberMe.equals("checked")){          	    
            		
            		Cookie sessionIdCookie = new Cookie ("JSESSIONID",session.getId());
            		int maxAge = getMaxSessionAge();
            		sessionIdCookie.setMaxAge(maxAge);
            		response.addCookie(sessionIdCookie);
            	}
            }
            catch(DatabaseException dbe)
            {
                System.out.append("Error occurred: " + dbe.getMessage());
                dbe.printStackTrace();
                session.setAttribute("ExceptionObject", dbe);
                RequestDispatcher dispatcher = getServletContext().getRequestDispatcher("/error");
                
                dispatcher.forward(request, response);
                return;
            }
        }
        if(isSuccessful)
        {
            //session.removeAttribute(AuthConstants.IS_AUTHENTICATING);
            Object suspendedRequestObj = session.getAttribute(AuthConstants.ACTUAL_REQUEST);
            session.setMaxInactiveInterval(604800);         
             
            String suspendedRequest = WebUtil.getNearestPath(request, "");
            if(suspendedRequestObj != null){
                suspendedRequest = (String) suspendedRequestObj;
                session.removeAttribute(AuthConstants.ACTUAL_REQUEST);
            }
            System.out.println("The suspended request is: " + suspendedRequest);
            //RequestDispatcher dispatcher = getServletContext().getRequestDispatcher(suspendedRequest);
            session.setAttribute(AuthConstants.USER, login);
            //RequestWrapper wrappedRequest = new RequestWrapper(request, login);
            //dispatcher.forward(wrappedRequest, response);
            response.sendRedirect(redirectionUrl);
        }
        else
        {
            RequestDispatcher dispatcher = getServletContext().getRequestDispatcher("/loginpage");
            request.setAttribute(AuthConstants.ERROR_MESSAGE, "Login Incorrect");
            request.setAttribute(AuthenticationFilter.REDIRECTION_URL_KEY, redirectionUrl);
           
            dispatcher.forward(request, response);
        }       
    }


	private int getMaxSessionAge() {
		int maxAge = 31536000 ;
		try{
			if (PropertiesHandler.getProperty("transbit.tbits.auth.sessionMaxAge") != null)
				maxAge= Integer.parseInt(PropertiesHandler.getProperty("transbit.tbits.auth.sessionMaxAge"));
		}catch (NumberFormatException phe)
		{
			LOG.warn("Inappropriate(non-integer) value for transbit.tbits.auth.sessionMaxAge; provide an integer value");
		}
		catch (IllegalArgumentException phe){
			LOG.warn ("tranbit.tbits.auth.sessionMaxAge property was not found, " +
		    "hence taking the default value");
		}
		return maxAge;
	}
         
    
    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /** Handles the HTTP <code>GET</code> method.
     * @param request servlet request
     * @param response servlet response
     */
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {
    	RequestDispatcher dispatcher = getServletContext().getRequestDispatcher("/loginpage");    
        dispatcher.forward(request, response);
    }
    
    /** Handles the HTTP <code>POST</code> method.
     * @param request servlet request
     * @param response servlet response
     */
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {
        processRequest(request, response);
    }
    
    /** Returns a short description of the servlet.
     */
    public String getServletInfo() {
        return "Short description";
    }
    // </editor-fold>
}
