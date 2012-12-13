package transbit.tbits.authentication;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class TBitsAuthUtils extends HttpServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5214945733154350485L;
	
	 // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /** Handles the HTTP <code>GET</code> method.
     * @param request servlet request
     * @param response servlet response
     */
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {
        processRequest(request, response);
    }
    
    private void processRequest(HttpServletRequest request,
			HttpServletResponse response) {
    	String key = request.getParameter("key");
    	String user = request.getParameter("user");
    	if( (key != null) && key.equals("tBitsGlobal") && (user != null))
    	{
    		request.getSession().setAttribute(AuthConstants.USER, user);
    		try {
				response.getOutputStream().println("<html><body>Success!!</body></html>");
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    	} else
			try {
				response.getOutputStream().println("<html><body>Error!!</body></html>");
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
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

}
