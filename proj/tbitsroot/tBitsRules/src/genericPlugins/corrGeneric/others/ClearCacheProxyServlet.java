package corrGeneric.others;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import corrGeneric.com.tbitsGlobal.server.managers.ManagerRegistry;

import transbit.tbits.api.IProxyServlet;

public class ClearCacheProxyServlet implements IProxyServlet 
{	
	public void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException 
	{
		PrintWriter pw = response.getWriter() ;
		try		
		{	
			String msg = "<HTML><body>Starting to clear the cache.<br/><br />" ;
			msg += ManagerRegistry.getInstance().clearCaches() + "<br />";
			msg += "<br />Cache clearing finished.</body></HTML>";
			pw.write(msg);
		}
		catch(Exception e)
		{
			pw.write("Clearing cache failed.");
			e.printStackTrace();
		}		
	}

	public void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		doGet(request, response);
	}

	public String getName() {
		return "corr_clear_cache";
	}

}
