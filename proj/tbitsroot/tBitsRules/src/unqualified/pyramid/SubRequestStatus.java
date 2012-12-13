package pyramid;


import static transbit.tbits.Helper.TBitsConstants.PKG_WEBAPPS;

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
import transbit.tbits.domain.Type;
import transbit.tbits.exception.TBitsException;

/**
 * @author Lokesh
 *
 */
public class SubRequestStatus implements IPreRenderer {
	private static final TBitsLogger LOG = TBitsLogger.getLogger(PKG_WEBAPPS);
	
	/* (non-Javadoc)
	 * @see transbit.tbits.domain.IPreRenderer#getSequence()
	 */
	public double getSequence() {
		// TODO Auto-generated method stub
		return 0;
	}

	/* (non-Javadoc)
	 * @see transbit.tbits.domain.IPreRenderer#process(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse, java.util.Hashtable)
	 */
	public void process(HttpServletRequest request,
			HttpServletResponse response, Hashtable<String, Object> tagTable, ArrayList<String> tagList)
			throws TBitsException {
		int systemId = -1;
		int requestId = -1;
		
		//String sysPrefix = "DCR343,DCR326,VDCR326,DCR345";
		boolean isRuleApplicable = false;
		
		HttpSession session = request.getSession();
		Request tBitsRequest = null;
		try{			
			String uri = request.getRequestURI();
			String[] keys = uri.split("/");
			isRuleApplicable = PyramidUtils.isExistsInCommons(keys[2]);
			
			if (isRuleApplicable && (!keys[1].equals("add-request"))){
				requestId = Integer.parseInt(keys[3]);
				
				BusinessArea ba = BusinessArea.lookupBySystemPrefix(keys[2]);
				if (ba != null)
					systemId = ba.getSystemId();
				
				tBitsRequest = Request.lookupBySystemIdAndRequestId(ba.getSystemId(), requestId);
				Type idcInitaited = Type.lookupAllBySystemIdAndFieldNameAndTypeName(systemId, "status_id", "IDCInitiated");
				Type idcComplete = Type.lookupAllBySystemIdAndFieldNameAndTypeName(systemId, "status_id", "IDCComplete");
				if (tBitsRequest != null){	
					if (keys[1].equals("add-subrequest")){	
						tagTable.put("status_id_list", "\n<OPTION value='IDCInitiated' SELECTED>" + idcInitaited.getDisplayName() + "</OPTION>"
								+ "\n<OPTION value='IDCComplete'>" + idcComplete.getDisplayName() + "</OPTION>");
					}
					else if (tBitsRequest.getParentRequestId() > 0){						
						Type type = tBitsRequest.getStatusId();
						if (type.getName().equals("IDCInitiated"))
							tagTable.put("status_id_list", "\n<OPTION value='IDCInitiated' SELECTED>" + idcInitaited.getDisplayName() + "</OPTION>"
								+ "\n<OPTION value='IDCInitiated'>" + idcComplete.getDisplayName() + "</OPTION>");
						else if (type.getName().equals("IDCComplete"))
							tagTable.put("status_id_list", "\n<OPTION value='IDCInitiated'>" + idcInitaited.getDisplayName() + "</OPTION>"
									+ "\n<OPTION value='IDCComplete' SELECTED>" + idcComplete.getDisplayName() + "</OPTION>");
					}
				}
			}
		} catch (DatabaseException e) {
			LOG.severe("Error occurred while retrieving parent request for IDC");
			session.setAttribute("ExceptionObject", e);
			return;
		} catch (Exception e){
			e.printStackTrace();
		}
	}
}
