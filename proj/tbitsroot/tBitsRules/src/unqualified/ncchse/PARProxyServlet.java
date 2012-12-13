package ncchse;

import java.io.IOException;
import java.util.Hashtable;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import transbit.tbits.api.IProxyServlet;
import transbit.tbits.domain.BusinessArea;
import transbit.tbits.domain.Request;
import transbit.tbits.domain.Type;
import transbit.tbits.exception.TBitsException;
import transbit.tbits.webapps.AddHtmlRequest;
import transbit.tbits.webapps.WebUtil;
import static ncchse.HSEConstants.*;

public class PARProxyServlet implements IProxyServlet {
	public static final String servletName = "PARProxyServlet" ;

	public void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		    handleRequest(request,response);

	}

	public void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		    handleRequest(request,response);

	}
	private void handleRequest(HttpServletRequest request, HttpServletResponse response)
	throws ServletException, IOException {
		
		try
		{
			String parSysId = request.getParameter(PAR_SYS_ID);
			String parReqId = request.getParameter(PAR_REQ_ID) ;
			String airSysId = request.getParameter(AIR_SYS_ID);
			String airReqId = request.getParameter(AIR_REQ_ID) ;
			
			if( null == airSysId || null == parSysId || null == parReqId ) 
				throw new TBitsException("Illegal parameters in the request.") ;
			
			airSysId = airSysId.trim() ;
			//airReqId = airReqId.trim() ;
			parSysId = parSysId.trim() ;
			parReqId = parReqId.trim() ;
			int aSysId = Integer.parseInt(airSysId) ;
			int pSysId = Integer.parseInt(parSysId) ;
			//int aReqId = Integer.parseInt(airReqId) ;
			int pReqId = Integer.parseInt(parReqId) ;
		
			BusinessArea parba = BusinessArea.lookupBySystemId(pSysId) ;
			BusinessArea airba = BusinessArea.lookupBySystemId(aSysId);
			Request parRequest = Request.lookupBySystemIdAndRequestId(parba.getSystemId(), pReqId) ;
			String linkedReq=PAR_PREFIX+"#"+parReqId;
			
			if( null == parba || null == airba || null == parRequest  || ! airba.getSystemPrefix().equalsIgnoreCase(AIR_PREFIX))
				throw new TBitsException( "Illegal parameters in the request" ) ;
							
			Hashtable<String,String> prefillTable = new Hashtable<String,String>() ;
			
			Type inj=parRequest.getRequestTypeId();
			Type cont=parRequest.getCategoryId();
			Type dep=parRequest.getSeverityId();
			
			String name=parRequest.get(PAR_NAME);
			String subject= "Contractor:"+cont.getDisplayName()+" "+"person:" +name +" "+"injury type:"+inj.getDisplayName();
			
			
			prefillTable.put(AIR_LINKED_REQUEST, linkedReq) ;
			prefillTable.put(PREFILL_REQUEST_TYPE_ID,inj.getDisplayName());
			prefillTable.put(AIR_BRIEF_DESCRIPTION,parRequest.get(PAR_BRIEF_DESCRIPTION));
			prefillTable.put(PREFILL_ACCIDENTLOCATION,parRequest.get(PAR_LOCATION));
			prefillTable.put(PREFILL_NAME,name);
			prefillTable.put(PREFILL_AGE,parRequest.get(PAR_AGE));
			prefillTable.put(PREFILL_SEX,parRequest.get(PAR_SEX));
			prefillTable.put(PREFILL_CONDITIONS,parRequest.get(PAR_CONDITIONS));
			prefillTable.put(PREFILL_EQUIPMENT,parRequest.get(PAR_EQUIPMENT));
			prefillTable.put(PREFILL_OTHERINFO,parRequest.get(PAR_OTHER_INFO));
			prefillTable.put(PREFILL_REMIDY,parRequest.get(PAR_REMIDY));
			prefillTable.put(PREFILL_SUBJECT,subject);
			prefillTable.put(AIR_DESIGNATION,parRequest.get(PAR_DESIGNATION));
			
			prefillTable.put(AIR_CONTRACTOR,cont.getName());
			prefillTable.put(AIR_INJURY_TYPE, inj.getName());
			prefillTable.put(AIR_DEPARTMENT, dep.getName());
			
			prefillTable.put(AIR_DATE,parRequest.get(PAR_DATE));
			
            
			RequestDispatcher rd=null;
			request.setAttribute(AddHtmlRequest.PREFILL_TABLE, prefillTable) ;
			if(airReqId==null)
		    rd = request.getRequestDispatcher(WebUtil.getNearestPath(request, "add-request/" + airba.getSystemPrefix())) ;
			else
            rd = request.getRequestDispatcher(WebUtil.getNearestPath(request, "q/" + airba.getSystemPrefix()+"/"+airReqId+"?u=1")) ;	
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
		public String getName() {
		// TODO Auto-generated method stub
		return servletName;
	}

}
