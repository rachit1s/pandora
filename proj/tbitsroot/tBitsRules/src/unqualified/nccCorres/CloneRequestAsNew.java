package nccCorres;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import transbit.tbits.ExtUI.IActionDetailsHeaderSlotFiller;
import transbit.tbits.domain.BusinessArea;
import transbit.tbits.domain.Request;
import transbit.tbits.domain.User;
import transbit.tbits.webapps.WebUtil;

public class CloneRequestAsNew implements IActionDetailsHeaderSlotFiller 
{
	public String getActionDetailsHeaderHtml(HttpServletRequest httpRequest,
			HttpServletResponse httpResponse, BusinessArea ba, Request request,
			User user) 
	{
		if( null == ba || ! ba.getSystemPrefix().equals( CorresConstants.CORR_SYSPREFIX ))
			return null ;
		
		
		String sysId = ba.getSystemId() + "" ;
		String requestId = request.getRequestId() + "";
		String path = "proxy/" + ClonePrefillProxyServlet.servletName + "?"+CorresConstants.CORR_SYS_ID+"=" + sysId + "&" + CorresConstants.CORR_REQUEST_ID + "=" + requestId  ;
		String relativePath = WebUtil.getNearestPath(httpRequest, path) ;
		String linkHtml = "<a href='" + relativePath + "' target='_blank' title='Opens a new correspondance form with the data of current request.'>" + "<b> Forward  </b>" + "</a>" ; 
		
		return linkHtml ;
	}

	public double getActionDetailsHeaderOrder() 
	{
		return 1;
	}
}
