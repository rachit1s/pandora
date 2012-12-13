package corrGeneric.others;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import transbit.tbits.api.IProxyServlet;

public class ValidateCorrConfiguration implements IProxyServlet {

	public void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException 
	{
		handleRequest(request, response);
	}

	public void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException 
	{
		handleRequest(request,response);
	}

	private void handleRequest(HttpServletRequest request,
			HttpServletResponse response) 
	{
		PrintWriter pw;
		try {
			pw = response.getWriter();
		String typeMsg = typeTest();
		
		String userMsg = userTest();
		
		pw.flush();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private String userTest() {
		// TODO Auto-generated method stub
		return null;
	}

	private String typeTest() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getName() {
		return "corr_validate_conf";
	}

}
