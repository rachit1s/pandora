package transbit.tbits.admin.common;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * This servlet would act a proxy for various registered servlets for each Admin Tab page.
 * So, each of the Tab page would register with this servlet in their static blocks.
 * And each Tab Page should be mentions in tBitsContextLister in the form of Class.forname(TabPageName), 
 * so that their static() block gets initialized.
 * 
 */
public class AdminProxyServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	   
    /**
     * @see HttpServlet#HttpServlet()
     */
    public AdminProxyServlet() {
        super();
    }

	/**
	 * @see Servlet#init(ServletConfig)
	 */
	public void init(ServletConfig config) throws ServletException {
		// TODO Auto-generated method stub
		System.out.println("Admin Proxy servlet context serverinfo: " + config.getServletContext().getServerInfo());
	}
	
    private String  encoding="UTF-8";
	private void myService(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException
	{
		request.setCharacterEncoding(encoding);
		response.setContentType("text/html; charset=" + encoding);
		response.setCharacterEncoding(encoding);
		
		String pathInfo = request.getServletPath();
		if(pathInfo != null)
		{
			HttpServlet childServlet = null;
			try
			{
				childServlet = Helper.resolveChildServlet(pathInfo, AdminProxyServlet.class, ".admin");
				if(childServlet != null)
				{
					System.out.println("Found the servlet: " + childServlet.getClass().getCanonicalName());
					childServlet.service(request, response);
				}
				else
				{
					response.getWriter().println("Unable to get the correct servlet");
				}
			}
			catch(Exception exp)
			{
				exp.printStackTrace();
				response.getWriter().println("Unable to find and call the correct service.");
			}
		}
		else
		{
			response.getWriter().println("Hitting the root servlet. No page to display.");
		}
	}
	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		myService(request, response);
	}
	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		myService(request, response);
	}
	public static void main(String[] args) {
		
	}
}
