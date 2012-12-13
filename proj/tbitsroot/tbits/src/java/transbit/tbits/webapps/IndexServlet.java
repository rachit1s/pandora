package transbit.tbits.webapps;

import java.io.IOException;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;



public class IndexServlet extends HttpServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public void doGet(HttpServletRequest aRequest,
			HttpServletResponse aResponse) throws ServletException, IOException {
		boolean isClassicInterface = WebUtil.isClassicInterface();
		
		String classicURL = getInitParameter("classic_url");
		if(classicURL == null)
			classicURL = "my-requests";
		String jaguarURL = getInitParameter("jaguar_url");
		if(jaguarURL == null)
			jaguarURL = "jaguar";
		
		String url = jaguarURL;
		if(isClassicInterface)
			url = classicURL;
		aResponse.sendRedirect(aRequest.getContextPath() + "/" + url);
	}
	
	public void doPost(HttpServletRequest aRequest,
			HttpServletResponse aResponse) throws ServletException, IOException {
		doGet(aRequest, aResponse);
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		IndexServlet idxSrv = new IndexServlet();
		System.out.println(WebUtil.isClassicInterface());
	}
	
	
}
