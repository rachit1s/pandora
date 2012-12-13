package transbit.tbits.webapps;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import transbit.tbits.common.DTagReplacer;

public class ImportDataWizard extends HttpServlet {

	private static final long serialVersionUID = 1L;
	private static final String WIZARD_HTML = "web/import-data-wizard.htm";
	public void doPost(HttpServletRequest aRequest, HttpServletResponse aResponse) throws ServletException, IOException 
	{
		handleRequest(aRequest, aResponse);
	}
	public void doGet(HttpServletRequest aRequest, HttpServletResponse aResponse) throws ServletException, IOException 
	{
		handleRequest(aRequest, aResponse);
	}
	public void handleRequest(HttpServletRequest aRequest, HttpServletResponse aResponse) throws ServletException, IOException 
	{
		aResponse.setContentType("text/html");
		String ba = aRequest.getParameter("ba");
		DTagReplacer dTag = new DTagReplacer(WIZARD_HTML);
		dTag.replace("sysPrefix", ba);
		dTag.replace("nearestPath", WebUtil.getNearestPath(aRequest, ""));
		aResponse.getWriter().println(dTag.parse(0));
	}
}
