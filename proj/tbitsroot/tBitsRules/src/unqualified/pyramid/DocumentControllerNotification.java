/**
 * 
 */
package pyramid;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Hashtable;

import transbit.tbits.api.AttachmentInfo;
import transbit.tbits.api.IRule;
import transbit.tbits.api.RuleResult;
import transbit.tbits.common.DatabaseException;
import transbit.tbits.domain.BusinessArea;
import transbit.tbits.domain.Field;
import transbit.tbits.domain.Request;
import transbit.tbits.domain.RequestEx;
import transbit.tbits.domain.RequestUser;
import transbit.tbits.domain.User;
import transbit.tbits.domain.UserType;

/**
 * DocumentControllerNotification is the plug-in which notifies the 'Document Controller' about the documents
 * which have to be transmitted. Every time 'Transmit To Client/Transmit To Field' is set to true, a 
 * notification will be sent to the document controller by email. *
 */
public class DocumentControllerNotification implements IRule {

	/* (non-Javadoc)
	 * @see transbit.tbits.api.IRule#execute(java.sql.Connection, transbit.tbits.domain.BusinessArea, transbit.tbits.domain.Request, transbit.tbits.domain.Request, int, transbit.tbits.domain.User, java.util.Hashtable, boolean, java.util.Collection)
	 */
	public RuleResult execute(Connection connection, BusinessArea ba, Request oldRequest,
			Request currentRequest, int Source, User user,
			Hashtable<Field, RequestEx> extendedFields, boolean isAddRequest, 
			Collection<AttachmentInfo> attachments){
		
		//String sysPrefix = "DCR343,DCR326,VDCR326,DCR345";
		boolean isApplicable = PyramidUtils.isExistsInCommons(ba.getSystemPrefix());
		
		String ttClientStr = "TransmitDocClient";
		String ttFieldStr = "TransmitToField";
		String ttVendorStr = "TransmitToVendor";
		String userName = "document.controller";
		RuleResult ruleResult = new RuleResult();
		if (isApplicable && (currentRequest.getParentRequestId() == 0)){
			int systemId = ba.getSystemId();
			try {
				if (isFieldExists(systemId, ttClientStr, extendedFields, currentRequest,userName, ruleResult)||
						isFieldExists(systemId, ttFieldStr, extendedFields, currentRequest,userName, ruleResult)||
						isFieldExists(systemId, ttVendorStr, extendedFields, currentRequest,userName, ruleResult)){
					return ruleResult;
				}				
				else{
					ruleResult.setMessage("Not applicable as required fields do not exist.");
					ruleResult.setCanContinue(true);
					return ruleResult;
				}
			} catch (DatabaseException e) {
				e.printStackTrace();
			}
		}		
		return ruleResult;
	}
	
	private boolean isFieldExists(int systemId, String fieldName, Hashtable<Field, RequestEx> extendedFields, Request currentRequest, String userName, RuleResult ruleResult) throws DatabaseException{
		boolean fieldExists = false;
		Field field = Field.lookupBySystemIdAndFieldName(systemId, fieldName);
		if (field != null){
			RequestEx fieldReq = extendedFields.get(field);
			if ((fieldReq != null) &&(fieldReq.getBitValue())){
				ArrayList <RequestUser> aCcs = new ArrayList<RequestUser>();
				User dc = User.lookupAllByUserLogin(userName);
				RequestUser ru = new RequestUser(systemId, currentRequest.getRequestId(),UserType.CC,dc.getUserId(),1, false);						
				aCcs.add(ru);
				currentRequest.setCcs(aCcs);
				ruleResult.setCanContinue(true);
				ruleResult.setSuccessful(true);
				fieldExists = true;
			}
		}
		return fieldExists;
	}

	/* (non-Javadoc)
	 * @see transbit.tbits.api.IRule#getName()
	 */
	public String getName() {
		return this.getClass().getSimpleName() + " - Add document controller to CC list if Transmit to Client/field is marked true";
	}

	/* (non-Javadoc)
	 * @see transbit.tbits.api.IRule#getSequence()
	 */
	public double getSequence() {
		// TODO Auto-generated method stub
		return 0;
	}

}
