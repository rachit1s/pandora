package lancoCorres;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import transbit.tbits.ExtUI.IActionDetailsHeaderSlotFiller;
import transbit.tbits.domain.BusinessArea;
import transbit.tbits.domain.Request;
import transbit.tbits.domain.User;
import transbit.tbits.webapps.WebUtil;

public class DICreateLink implements IActionDetailsHeaderSlotFiller 
{
	public String getActionDetailsHeaderHtml(HttpServletRequest httpRequest,
			HttpServletResponse httpResponse, BusinessArea ba, Request request,
			User user) 
	{
		if( null == ba || ! ba.getSystemPrefix().equals( KskConstants.CORR_SYSPREFIX ))
			return null ;
		
		
		String sysId = ba.getSystemId() + "" ;
		String requestId = request.getRequestId() + "";
		String path = "proxy/" + IDPrefillProxyServlet.servletName + "?"+DIConstants.CORR_SYS_ID+"=" + sysId + "&" + DIConstants.CORR_REQUEST_ID + "=" + requestId  ;
		String relativePath = WebUtil.getNearestPath(httpRequest, path) ;
		String linkHtml = "<a href='" + relativePath + "' target='_blank'>" + DIConstants.CORR_LINK_NAME + "</a>" ; 
		
		return linkHtml ;
	}

	public double getActionDetailsHeaderOrder() 
	{
		return 0;
	}
}
