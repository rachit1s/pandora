package nccCorres;

import java.util.ArrayList;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import transbit.tbits.ExtUI.IActionDetailsHeaderSlotFiller;
import transbit.tbits.common.Utilities;
import transbit.tbits.domain.BusinessArea;
import transbit.tbits.domain.Request;
import transbit.tbits.domain.User;
import transbit.tbits.webapps.WebUtil;

public class SendReplyLink implements IActionDetailsHeaderSlotFiller {

	public String getActionDetailsHeaderHtml(HttpServletRequest httpRequest,
			HttpServletResponse httpResponse, BusinessArea ba, Request request,
			User user) 
	{
		if( null == ba || ! ba.getSystemPrefix().equals( CorresConstants.DI_SYSPREFIX ))
			return null ;
		
//		Hashtable<String,String> relReqHash = request.getRelatedRequests() ;
//		System.out.println("relateRequests hashtable : " + relReqHash );
		String relatedReqString = request.getRelatedRequests() ;
		System.out.println("relatedRequests string : " + relatedReqString);
		ArrayList<String> relReqs = Utilities.toArrayList(relatedReqString) ;
		String relCorrReq = null ;
		for( String r : relReqs )
		{
			String[] parts = r.split("#") ;
			if(null != parts && parts.length > 1 )
			{
				String sysPrefix = parts[0].trim() ;
				if( sysPrefix.equalsIgnoreCase(CorresConstants.CORR_SYSPREFIX))
				{	relCorrReq = parts[1].trim() ;
					break;
				}
			}
		}
		if( null == relCorrReq )
		{
			return null ;
		}
				
		try
		{
			//int cSysId = Integer.parseInt(corrSysId) ;
			BusinessArea corrba = BusinessArea.lookupBySystemPrefix(CorresConstants.CORR_SYSPREFIX) ;
			if( null == corrba )
				return null ;			
						
			String path = "proxy/" + CorrPrefillProxyServlet.servletName + "?"+CorresConstants.CORR_SYS_ID+"=" + corrba.getSystemId() + "&" + CorresConstants.CORR_REQUEST_ID + "=" + relCorrReq + "&" + CorresConstants.DI_SYS_ID + "=" + request.getSystemId() + "&" + CorresConstants.DI_REQUEST_ID + "=" + request.getRequestId(); // + "&u=1" ;
			String relativePath = WebUtil.getNearestPath(httpRequest, path) ;
			String linkHtml = "<a href='" + relativePath + "' target='_blank'>" + CorresConstants.DI_LINK_NAME + "</a>" ;
			return linkHtml ;
		}
		catch(NumberFormatException e)
		{
			e.printStackTrace() ;
			return null ;
		}
		catch( Exception e )
		{
			e.printStackTrace() ;
			return null ;
		}
	}

	public double getActionDetailsHeaderOrder() 
	{		
		return 0;
	}

}
