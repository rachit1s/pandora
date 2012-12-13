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

/**
 * <code>ProcurementOfficerRule</code> notifies the procurement officer if a document is "Issued for Purchase"
 * or "Issued for Enquiry". This is done by checking whether the fields, "Issued for Purchase"
 * or "Issued for Enquiry" are set to true.
 *
 */
public class ProcurementOfficerRule implements IRule {
	
	private static final String EMPTY_STRING = "";
	private static final String PYRAMID_PROCUREMENT_OFFICER = "pyramid.procurement_officer";
	private static final String ISSUED_FOR_PROCUREMENT = "issuedforprocurement";
	private static final String ISSUED_FOR_FABRICATION = "issuedforfabrication";

	/* (non-Javadoc)
	 * @see transbit.tbits.api.IRule#execute(java.sql.Connection, transbit.tbits.domain.BusinessArea, transbit.tbits.domain.Request, transbit.tbits.domain.Request, int, transbit.tbits.domain.User, java.util.Hashtable, boolean, java.util.Collection)
	 */
	public RuleResult execute(Connection connection, BusinessArea ba, Request oldRequest,
			Request currentRequest, int Source, User user,
			Hashtable<Field, RequestEx> extendedFields, boolean isAddRequest,
			Collection<AttachmentInfo> attachments){
		//System.out.println("%%%%%%%%%%%%%%%%%%%%%%$$$$$$$$$$$$$$$$$$$$$$$$CC list for procurement");
		RuleResult ruleResult = new RuleResult();
		String sysPrefix = ba.getSystemPrefix();
		int aSystemId = ba.getSystemId();
		int aRequestId = currentRequest.getRequestId();
		boolean isApplicable = PyramidUtils.inProperty345SysPrefixes(sysPrefix);
		if (isApplicable){
			try {
				Field ifeField = Field.lookupBySystemIdAndFieldName(aSystemId, ISSUED_FOR_FABRICATION);
				Field ifpField = Field.lookupBySystemIdAndFieldName(aSystemId, ISSUED_FOR_PROCUREMENT);
				if (isFieldsExistAndApplicable(extendedFields, ifeField, ifpField)){			
					String ccList = PyramidUtils.getProperty(PYRAMID_PROCUREMENT_OFFICER);
					ccList = ((ccList== null) || ccList.trim().equals(EMPTY_STRING))? EMPTY_STRING : ccList.trim();
					if (!ccList.equals(EMPTY_STRING)){
						//System.out.println("CCs names: " + ccList);
						ArrayList<RequestUser> ruList = PyramidUtils.getRequestUsersList(
								aSystemId, aRequestId, ccList);
						//System.out.println("CCs: " + ruList.toString());
						currentRequest.setCcs(ruList);
						ruleResult.setCanContinue(true);
						ruleResult.setSuccessful(true);
					}
					else{
						ruleResult.setCanContinue(true);
						ruleResult.setMessage("Did not add anyone to CC as ccList was empty.");
					}
				}
				else{
					ruleResult.setCanContinue(true);
				}
			} catch (DatabaseException e) {
				e.printStackTrace();
			}			
		}
		else{
			ruleResult.setCanContinue(true);
		}
		return ruleResult;		
	}

	/**
	 * @param extendedFields
	 * @param ifeField
	 * @param ifpField
	 * @return
	 */
	private boolean isFieldsExistAndApplicable(
			Hashtable<Field, RequestEx> extendedFields, Field ifeField,
			Field ifpField) {
		return getFieldValue(extendedFields, ifeField) || (getFieldValue(extendedFields, ifpField));
	}
	
	private boolean getFieldValue(Hashtable<Field, RequestEx> extendedFields, Field extField) {
		if (extField == null)
			return false;
		else{
			RequestEx extFieldReqEx = extendedFields.get(extField);
			return extFieldReqEx.getBitValue();			
		}
	}
	
	/* (non-Javadoc)
	 * @see transbit.tbits.api.IRule#getName()
	 */
	public String getName() {
		return "ProcurementOfficerRule - Adds procurement officer to CC if \"Issued for enquiry\" or \"Issued for purchase\" is set to true";
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
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
