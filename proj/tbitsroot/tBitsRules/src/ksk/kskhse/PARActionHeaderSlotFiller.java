package kskhse;

import java.util.ArrayList;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import transbit.tbits.ExtUI.IActionDetailsHeaderSlotFiller;
import transbit.tbits.common.Utilities;
import transbit.tbits.domain.BusinessArea;
import transbit.tbits.domain.Field;
import transbit.tbits.domain.Request;
import transbit.tbits.domain.User;
import transbit.tbits.webapps.WebUtil;
import static kskhse.HSEConstants.*;


public class PARActionHeaderSlotFiller implements
		IActionDetailsHeaderSlotFiller {

	public String getActionDetailsHeaderHtml(HttpServletRequest httpRequest,
			HttpServletResponse httpResponse, BusinessArea ba, Request request,
			User user) {
		
		 if( null == ba || ! ba.getSystemPrefix().equals(PAR_PREFIX ))
			return null ;
		
		String relatedReqString = request.get(Field.RELATED_REQUESTS) ;
		System.out.println("relatedRequests string : " + relatedReqString);
		ArrayList<String> relReqs = Utilities.toArrayList(relatedReqString) ;
		String relCorrReq = null ;
		for( String r : relReqs )
		{
			String[] parts = r.split("#") ;
			if(null != parts && parts.length > 1 )
			{
				String sysPrefix = parts[0].trim() ;
				if( sysPrefix.equalsIgnoreCase(AIR_PREFIX))
				{	relCorrReq = parts[1].trim() ;
					break;
				}
			}
		}
		if( null == relCorrReq )
		{
			try
			{
				//int cSysId = Integer.parseInt(corrSysId) ;
				BusinessArea airba = BusinessArea.lookupBySystemPrefix(AIR_PREFIX) ;
				if( null == airba )
					return null ;			
							
				String path = "proxy/" + PARProxyServlet.servletName + "?"+AIR_SYS_ID+"=" + airba.getSystemId() + "&" + PAR_SYS_ID + "=" + request.getSystemId() + "&" + PAR_REQ_ID + "=" + request.getRequestId(); // + "&u=1" ;
				String relativePath = WebUtil.getNearestPath(httpRequest, path) ;
				String linkHtml = "<a href='" + relativePath + "' target='_blank'>" + HSE_ADD_LINK_NAME + "</a>" ;
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
		else try{//int cSysId = Integer.parseInt(corrSysId) ;
			BusinessArea airba = BusinessArea.lookupBySystemPrefix(AIR_PREFIX) ;
			if( null == airba )
				return null ;			
						
			String path = "proxy/" + PARProxyServlet.servletName + "?"+AIR_SYS_ID+"=" + airba.getSystemId() + "&"
			+ AIR_REQ_ID+"="+relCorrReq+ "&"+ PAR_SYS_ID + "=" + request.getSystemId() + "&" + PAR_REQ_ID + "=" + request.getRequestId(); // + "&u=1" ;
			String relativePath = WebUtil.getNearestPath(httpRequest, path) ;
			String linkHtml = "<a href='" + relativePath + "' target='_blank'>" + HSE_UPDATE_LINK_NAME + "</a>" ;
			return linkHtml ;
			
			
		}catch(NumberFormatException e)
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

	@Override
	public double getActionDetailsHeaderOrder() {
		// TODO Auto-generated method stub
		return 0;
	}

}
