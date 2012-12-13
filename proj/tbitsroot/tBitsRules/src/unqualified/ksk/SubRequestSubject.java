package ksk;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Hashtable;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import transbit.tbits.common.DatabaseException;
import transbit.tbits.common.TBitsLogger;
import transbit.tbits.domain.BusinessArea;
import transbit.tbits.domain.IPreRenderer;
import transbit.tbits.domain.Request;
import transbit.tbits.exception.TBitsException;
import static transbit.tbits.Helper.TBitsConstants.PKG_WEBAPPS;

/**
 *  This plugin is to pre-fill the subject of the sub-request(currently in case of Pyramid) when IDC 
 *  is required in a business area (Sys-prefix: 343 in this case).
 */

/**
 * @author Lokesh
 *
 */
public class SubRequestSubject implements IPreRenderer {	
	static final TBitsLogger LOG = TBitsLogger.getLogger(PKG_WEBAPPS);

	/* (non-Javadoc)
	 * @see transbit.tbits.domain.IPreRenderer#getSequence()
	 */
	public double getSequence() {
		return 1;
	}

	/* (non-Javadoc)
	 * @see transbit.tbits.domain.IPreRenderer#process(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse, java.util.Hashtable)
	 */
	public void process(HttpServletRequest request,
			HttpServletResponse response, Hashtable<String, Object> tagTable, ArrayList<String> tagList)
			throws TBitsException {
		
		//String sysPrefix = "DCR343,DCR326,VDCR326,DCR345";
		boolean isRuleApplicable = false;
		
		HttpSession session = request.getSession();
		Request parentRequest = null;
		try{			
			String uri = request.getRequestURI();
			String[] keys = uri.split("/");
			
			isRuleApplicable = KSKUtils.isExistsInString(KSKUtils.IDCBALIST, keys[2]);
			
			if (keys[1].equals("add-subrequest") && isRuleApplicable){
				int requestId = -1;
				requestId = Integer.parseInt(keys[3]);
				BusinessArea ba = BusinessArea.lookupBySystemPrefix(keys[2]);
				parentRequest = Request.lookupBySystemIdAndRequestId(ba.getSystemId(), requestId);
				int count = KSKUtils.getSubRequestCountBySysIdReqId(ba.getSystemId(), parentRequest.getRequestId());
				tagTable.put("subject", "IDC-" + (count + 1)+ "-" + parentRequest.getSubject());
			}
		} catch (DatabaseException e) {
			LOG.severe("Error occurred while retrieving parent request for IDC");
			session.setAttribute("ExceptionObject", e);
			return;
		} catch (Exception e){
			e.printStackTrace();
		}
	}
	
	public static void main (String[] args){
		try {
			System.out.println("Count: " + KSKUtils.getSubRequestCountBySysIdReqId(6, 42));
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
//previously used servlet path and path translated to retrieve request parameters