package transbit.tbits.webapps;

import static transbit.tbits.Helper.TBitsConstants.PKG_WEBAPPS;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import transbit.tbits.common.TBitsLogger;
import transbit.tbits.domain.TransmittalHandler;
import transbit.tbits.exception.TBitsException;
import transbit.tbits.plugin.PluginManager;

public class TransmittalCaller extends HttpServlet{
	private static final TBitsLogger LOG = TBitsLogger.getLogger(PKG_WEBAPPS);
		
	public void doGet(HttpServletRequest aRequest, HttpServletResponse aResponse) throws ServletException, IOException {
		PrintWriter out = aResponse.getWriter();
		try {
			TransmittalHandler th = findAhandler();
			if(th != null)
				th.doGet(aRequest, aResponse);
		} catch (Exception e) {
			LOG.info(e.toString());
			out.println("Operation could not be completed." + e.toString());
			e.printStackTrace();
			return;			
		} finally {
		}
	}
	
	public void doPost(HttpServletRequest aRequest, HttpServletResponse aResponse) throws ServletException, IOException {
		PrintWriter out = aResponse.getWriter();
		try {
			TransmittalHandler th = findAhandler();
			if(th != null)
				th.doPost(aRequest, aResponse);
		} catch (Exception e) {
			e.printStackTrace();
			LOG.info(e.toString());
			out.println("Operation could not be completed." + e.toString());
			return;
		} finally {
		}
	}
		
	private TransmittalHandler findAhandler() throws TBitsException
	{
		TransmittalHandler th = null;
		ArrayList<Class> getHandlers = PluginManager.getInstance().findPluginsByInterface(TransmittalHandler.class.getName());
		if((getHandlers != null) && (getHandlers.size() > 0))
		{
			try {
				th = (TransmittalHandler) getHandlers.get(0).newInstance();
			} catch (InstantiationException e) {
				LOG.error("Could not instantiate the class found : " + getHandlers.get(0).getName());
				throw new TBitsException(e);
			} catch (IllegalAccessException e) {
				LOG.error("Could not access the class found : " + getHandlers.get(0).getName());
				throw new TBitsException(e);
			}			
		}
		else
		{
			LOG.error("No transmittal handlers found.");
		}
		return th;
	}
}
