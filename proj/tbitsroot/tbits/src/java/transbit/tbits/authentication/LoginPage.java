/*
 * LoginPage.java
 *
 * Created on September 14, 2006, 1:58 AM
 */

package transbit.tbits.authentication;

import java.io.*;
import java.net.*;
import java.util.Properties;
import java.util.Random;
import java.util.StringTokenizer;
import javax.servlet.*;
import javax.servlet.http.*;

import transbit.tbits.common.Configuration;
import transbit.tbits.common.DTagReplacer;
import transbit.tbits.common.PropertiesHandler;
import transbit.tbits.webapps.WebUtil;

//import org.apache.log4j.MDC;
/**
 *
 * @author Administrator
 * @version
 */
public class LoginPage extends HttpServlet {
    
    /** Processes requests for both HTTP <code>GET</code> and <code>POST</code> methods.
     * @param request servlet request
     * @param response servlet response
     */
    public static final String HTML_FILE = "web/login1.html";
    
	protected void processRequest(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = response.getWriter();
        DTagReplacer hp = new DTagReplacer(HTML_FILE);
		Object errorMessageObj = request.getAttribute(AuthConstants.ERROR_MESSAGE);
		
		
        if(errorMessageObj != null )
        {
            String errorMessage = (String) errorMessageObj;
            hp.replace("message", errorMessage);
        }
        else
            hp.replace("message", "");
        
        String redirectionUrl = (String) request.getAttribute(AuthenticationFilter.REDIRECTION_URL_KEY);
        if(redirectionUrl != null )
        {
            hp.replace(AuthenticationFilter.REDIRECTION_URL_KEY, URLEncoder.encode(redirectionUrl, "UTF-8"));
        }
        else
            hp.replace(AuthenticationFilter.REDIRECTION_URL_KEY, "");
        
        hp.replace("nearestPath", WebUtil.getNearestPath(request,""));
        //***********************************************************************
        /**
         * These methods converts tags present in html page with their appropriate values 
         */
        hp.replace("tip",randomTipGenerarator(PropertiesHandler.getProperty("transbit.tbits.auth.tip")));
        hp.replace("Address.contact", PropertiesHandler.getProperty("transbit.tbits.address.contact"));
        hp.replace("Email_to", getObfuscatedEmailTo(PropertiesHandler.getProperty("transbit.tbits.auth.emailto")));
        hp.replace("File",fileContents("web/customizable/custom_file.html"));
        //********************************************************************
        out.println(hp.parse(0));
		out.close();
    }
	
	/**
	 * it converts the given string into a java script int array to obfuscate it.
	 * @param email
	 * @return returns the obfuscated string
	 */
	private String getObfuscatedEmailTo(String email) {
		StringBuffer retStr = new StringBuffer();
		if(null == email )
			return retStr.toString();
		email = "mailto:" + email ;
		
		boolean first = true;
		for( int i = 0 ; i < email.length() ; i++ )
		{
			int c = email.charAt(i);
			if( first )
			{
				retStr.append( c );
				first = false;
			}
			else
				retStr.append(","+c);
		}
		
		return "[" + retStr.toString() + "]";
	}
	/**
	 * It appends the contents of custom file to the login page
	 * @param Path
	 * @return
	 * @throws FileNotFoundException
	 */

	public String fileContents(String Path) throws FileNotFoundException{
		File file=Configuration.findPath(Path);
		if(file==null){
			throw new FileNotFoundException(Path+" is not found");
			
		}
		else{
			try{
			BufferedReader br=new BufferedReader(new FileReader(file));
			StringBuilder sb=new StringBuilder();
			 String        aLine   = "";

             while ((aLine = br.readLine()) != null) {
                 sb.append(aLine).append("\n");
             }

             return sb.toString();
			}catch (Exception e) {
				// TODO: handle exception
				e.printStackTrace();
			}
		}
		return "";
		
	}
	
	/**
	 *This method is responsible for 
	 *generating one tip from lots of tips which
	 *are seperated by the $ sign.
	 * @param tip 
	 * @return
	 */
    public String randomTipGenerarator(String tip){
    	if(tip==null)
    		return "";
    	String tips[]=tip.split("\\$");
    	Random r=new Random();
    	int count=tips.length;
    	int tipno=r.nextInt(count);
    	return tips[tipno];
    	
    /*	StringTokenizer st=new StringTokenizer(tip,"$");
		StringTokenizer st1=new StringTokenizer(tip,"$");
		int count=0;
		while(st.hasMoreElements())
		{
			st.nextToken();
			
	//	System.out.println(str1[i]);
		count++;
		}
		
		String alltips[]=new String[count];
		count=0;
		while(st1.hasMoreElements()){
			alltips[count]=st1.nextToken();
			count++;
		}
		Random r=new Random();
		int tipno=r.nextInt(count);
    	
    	return alltips[tipno];*/
    }
	
    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /** Handles the HTTP <code>GET</code> method.
     * @param request servlet request
     * @param response servlet response
     */
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {
        processRequest(request, response);
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
