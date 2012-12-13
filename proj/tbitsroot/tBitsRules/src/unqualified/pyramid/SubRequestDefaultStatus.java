package pyramid;


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
import transbit.tbits.domain.RequestUser;
import transbit.tbits.domain.Type;
import transbit.tbits.exception.TBitsException;
import static transbit.tbits.Helper.TBitsConstants.PKG_WEBAPPS;

/**
 * 
 */

/**
 * @author Lokesh
 *
 */
public class SubRequestDefaultStatus implements IPreRenderer {
	
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
		
		//String sysPrefix = "DCR343, DCR326,VDCR326,DCR345";
		boolean isRuleApplicable = false;
		
		HttpSession session = request.getSession();
		Request parentRequest = null;
		try{			
			String uri = request.getRequestURI();
			String[] keys = uri.split("/");
			isRuleApplicable = PyramidUtils.isExistsInCommons(keys[2]);
			
			if (keys[1].equals("add-subrequest") && isRuleApplicable){
				int requestId = -1;
				requestId = Integer.parseInt(keys[3]);
				BusinessArea ba = BusinessArea.lookupBySystemPrefix(keys[2]);
				parentRequest = Request.lookupBySystemIdAndRequestId(ba.getSystemId(), requestId);
				ArrayList<RequestUser> subscriberList = parentRequest.getSubscribers();
				if (parentRequest != null){
					String subscribers = getSubscribers(subscriberList);
					Type idcInitaited = Type.lookupAllBySystemIdAndFieldNameAndTypeName(ba.getSystemId(), "status_id", "IDCInitiated");
					Type idcComplete = Type.lookupAllBySystemIdAndFieldNameAndTypeName(ba.getSystemId(), "status_id", "IDCComplete");

					tagTable.put("status_id_list", "\n<OPTION value='IDCInitiated' SELECTED>" + idcInitaited.getDisplayName() + "</OPTION>"
							+ "\n<OPTION value='IDCInitiated'>" + idcComplete.getDisplayName() + "</OPTION>");
					if (!(subscribers.equals("") || (subscribers == null))){
						tagTable.put("assignee_ids", subscribers);
					}
					else
						System.out.println("%%%%%%%%%%%%%%%%%Subscribers: " + subscribers);
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
	
	private String getSubscribers(ArrayList<RequestUser> subscribersList){
		String existingSubscribers = "";
		for (RequestUser rUser: subscribersList){
			try {
				existingSubscribers = (existingSubscribers.equals("")) 
											? rUser.getUser().getUserLogin() 
													: existingSubscribers + "," + rUser.getUser().getUserLogin();
			} catch (DatabaseException e) {
				LOG.severe("Exception occured while retrieving subscribers");
				e.printStackTrace();
			}
		}
		return existingSubscribers;
	}

}
