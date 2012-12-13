package pyramid;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Hashtable;

import pyramid.PyramidUtils;

import transbit.tbits.api.AttachmentInfo;
import transbit.tbits.api.IRule;
import transbit.tbits.api.RuleResult;
import transbit.tbits.api.UpdateRequest;
import transbit.tbits.common.DatabaseException;
import transbit.tbits.common.PropertiesHandler;
import transbit.tbits.domain.BusinessArea;
import transbit.tbits.domain.Field;
import transbit.tbits.domain.Request;
import transbit.tbits.domain.RequestEx;
import transbit.tbits.domain.RequestUser;
import transbit.tbits.domain.User;
import transbit.tbits.exception.APIException;
import transbit.tbits.exception.TBitsException;

/**
 * <code>TransmittalCCInsertion</code> inserts the users into the CC list of the 
 * transmittal when the transmittal is created. * 
 *
 */
public class TransmittalCCInsertion implements IRule {

	private static final String TRANSBIT_TBITS_TRANSMITTALS_CCLIST = "transbit.tbits.transmittals.cclist.";

	/* (non-Javadoc)
	 * @see transbit.tbits.api.IRule#execute(java.sql.Connection, transbit.tbits.domain.BusinessArea, transbit.tbits.domain.Request, transbit.tbits.domain.Request, int, transbit.tbits.domain.User, java.util.Hashtable, boolean, java.util.Collection)
	 */
	public RuleResult execute(Connection connection, BusinessArea ba, Request oldRequest,
			Request currentRequest, int Source, User user,
			Hashtable<Field, RequestEx> extendedFields, boolean isAddRequest,
			Collection<AttachmentInfo> attachments){
		RuleResult ruleResult = new RuleResult();
		int systemId = ba.getSystemId();
		String sysPrefix = ba.getSystemPrefix();
		
		boolean isApplicableToBA = PyramidUtils.inPropertyDTNSysPrefixes(sysPrefix);
		if (isApplicableToBA && (!isAddRequest)){
			ArrayList<RequestUser> ruList = new ArrayList<RequestUser>();
			try {
				String trnCCList = PropertiesHandler.getAppProperties().getProperty(TRANSBIT_TBITS_TRANSMITTALS_CCLIST + sysPrefix.trim());
				if ((trnCCList != null) || (!trnCCList.trim().equals("")))
					ruList.addAll(PyramidUtils.getRequestUsersSet(systemId, currentRequest.getRequestId(), trnCCList));
			} catch (DatabaseException e) {
				e.printStackTrace();
			}
			
			if ((ruList != null) && (!ruList.isEmpty())){
				currentRequest.setCcs(ruList);
				currentRequest.setNotify(1);				
			}
			else
				System.out.println("No cc list found");
			ruleResult.setSuccessful(true);
			ruleResult.setCanContinue(true);
		}
		else{
			ruleResult.setCanContinue(true);
			ruleResult.setMessage("Not applicable to this Business Area");
		}		
		return ruleResult;
	}

	/* (non-Javadoc)
	 * @see transbit.tbits.api.IRule#getName()
	 */
	public String getName() {
		return "TransmittalCCInsertion - Add users into the CC List for circulation.";
	}

	/* (non-Javadoc)
	 * @see transbit.tbits.api.IRule#getSequence()
	 */
	public double getSequence() {
		// TODO Auto-generated method stub
		return 0;
	}

	/**
	 * @param args
	 * @throws DatabaseException 
	 * @throws APIException 
	 * @throws TBitsException 
	 */
	public static void main(String[] args) throws DatabaseException, APIException, TBitsException {
		
		UpdateRequest updateRequest = new UpdateRequest();
		Hashtable<String, String> aParamTable = new Hashtable<String, String>();
		aParamTable.put(Field.BUSINESS_AREA, "DTN343");
		aParamTable.put(Field.USER, "document.controller");
		aParamTable.put(Field.REQUEST, "54");
		aParamTable.put(Field.DESCRIPTION, "testing cclist 5");
		updateRequest.updateRequest(aParamTable);
	}
}
