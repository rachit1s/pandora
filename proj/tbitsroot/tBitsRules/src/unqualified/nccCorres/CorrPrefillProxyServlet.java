package nccCorres;

import java.io.IOException;
import java.util.Hashtable;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import transbit.tbits.api.IProxyServlet;
import transbit.tbits.domain.BusinessArea;
import transbit.tbits.domain.Field;
import transbit.tbits.domain.Request;
import transbit.tbits.exception.TBitsException;
import transbit.tbits.webapps.AddHtmlRequest;
import transbit.tbits.webapps.WebUtil;

public class CorrPrefillProxyServlet implements IProxyServlet {

	public static final String servletName = "prefillCORRFromDI" ;
	public void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException 
	{
		handleRequest(request,response) ;
	}

	private void handleRequest(HttpServletRequest request,
			HttpServletResponse response) throws IOException 
	{
		try
		{
			String corrSysId = request.getParameter(CorresConstants.CORR_SYS_ID);
			String corrReqId = request.getParameter(CorresConstants.CORR_REQUEST_ID) ;
			String diSysId = request.getParameter(CorresConstants.DI_SYS_ID);
			String diReqId = request.getParameter(CorresConstants.DI_REQUEST_ID) ;
			if( null == corrSysId || null == corrReqId || null == diSysId || null == diReqId ) 
				throw new TBitsException("Illegal parameters in the request.") ;
			
			corrSysId = corrSysId.trim() ;
			corrReqId = corrReqId.trim() ;
			diSysId = diSysId.trim() ;
			diReqId = diReqId.trim() ;
			int cSysId = Integer.parseInt(corrSysId) ;
			int dSysId = Integer.parseInt(diSysId) ;
			int cReqId = Integer.parseInt(corrReqId) ;
			int dReqId = Integer.parseInt(diReqId) ;
		
			BusinessArea corrba = BusinessArea.lookupBySystemId(cSysId) ;
			BusinessArea diba = BusinessArea.lookupBySystemId(dSysId);
			Request diRequest = Request.lookupBySystemIdAndRequestId(diba.getSystemId(), dReqId) ;
			
			if( null == corrba || null == diba || null == diRequest  || ! corrba.getSystemPrefix().equalsIgnoreCase(CorresConstants.CORR_SYSPREFIX))
				throw new TBitsException( "Illegal parameters in the request" ) ;

			Hashtable<String,String> prefillTable = new Hashtable<String,String>() ;
			prefillTable.put(CorresConstants.CORR_DESCRIPTION_FIELD_NAME, diRequest.get(CorresConstants.DI_SUMMARY_FIELD_NAME)) ;
			request.setAttribute(AddHtmlRequest.PREFILL_TABLE, prefillTable) ;
			RequestDispatcher rd = request.getRequestDispatcher(WebUtil.getNearestPath(request, "q/" + corrba.getSystemPrefix() + "/" + cReqId + "?u=1")) ;
			rd.forward(request, response);
			return ;
		}
		catch(TBitsException e )
		{
			e.printStackTrace() ;
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Exception during prefilling ; " + e.getDescription() ) ;
			return ;
		}
		catch(Exception e )
		{
			e.printStackTrace() ;
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Exception during prefilling ; " + e.getMessage() ) ;
			return ;
		}
	}

	public void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException 
	{
		handleRequest(request,response) ;
	}

	public String getName() {
		return servletName;
	}

}
