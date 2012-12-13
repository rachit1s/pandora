/**
 * 
 */
package pyramid;

import java.sql.Connection;
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
import transbit.tbits.domain.User;

/**
 * IDCStartAndFinishDates is the plug-in which ensures that IDC start and finish dates are not left blank,
 * if IDC is required for a drawing.
 * 
 */
public class IDCStartAndFinishDates implements IRule {

	//Extended field names
	private static final String IDC_REQUIRED = "IDCRequired";
	private static final String TRANSMIT_DOC_CLIENT = "TransmitDocClient";
	private static final String IDC_START = "IDCIssue";
	private static final String IDC_FINISH = "IDCFinish";

	/* (non-Javadoc)
	 * @see transbit.tbits.api.IRule#execute(java.sql.Connection, transbit.tbits.domain.BusinessArea, transbit.tbits.domain.Request, transbit.tbits.domain.Request, int, transbit.tbits.domain.User, java.util.Hashtable, boolean, java.util.Collection)
	 */
	public RuleResult execute(Connection connection, BusinessArea ba, Request oldRequest,
			Request currentRequest, int Source, User user,
			Hashtable<Field, RequestEx> extendedFields, boolean isAddRequest,
			Collection<AttachmentInfo> attachments){
		
		RuleResult ruleResult = new RuleResult();
		int aSystemId = ba.getSystemId();
		boolean isApplicable = PyramidUtils.inProperty345SysPrefixes(ba.getSystemPrefix());
		try {
			if (isApplicable){
				Field tdcField = Field.lookupBySystemIdAndFieldName(aSystemId, TRANSMIT_DOC_CLIENT);
				Field idcField = Field.lookupBySystemIdAndFieldName(aSystemId, IDC_REQUIRED);
				if ((tdcField != null) && (idcField != null)){
					RequestEx tdcReqEx = extendedFields.get(tdcField);
					RequestEx idcReqEx = extendedFields.get(idcField);
					//Check if fields exist
					if ((tdcReqEx!=null) && (idcReqEx!=null)){
						if (tdcReqEx.getBitValue() && idcReqEx.getBitValue()){
							Field idcStartField = Field.lookupBySystemIdAndFieldName(aSystemId, IDC_START);
							Field idcFinishField = Field.lookupBySystemIdAndFieldName(aSystemId, IDC_FINISH);
							RequestEx idcStartReqEx = extendedFields.get(idcStartField);
							RequestEx idcFinishRequestEx = extendedFields.get(idcFinishField);
							// Check values
							if ((idcStartReqEx.getDateTimeValue() == null)||(idcFinishRequestEx.getDateTimeValue() == null) ){
								ruleResult.setMessage("Please select IDC start date/IDC finish date");
								ruleResult.setCanContinue(false);
							}
							else{
								ruleResult.setCanContinue(true);
								ruleResult.setSuccessful(true);								
							}
						}
						else{
							ruleResult.setCanContinue(true);
						}
					}
					else{
						ruleResult.setCanContinue(true);
						ruleResult.setMessage("Not applicable");
					}
				}
				else{
					ruleResult.setCanContinue(true);
					ruleResult.setMessage("Could not find the required fields, IDC Required/Transmit to client, hence did not apply the rule");					
				}
			}
			else{
				ruleResult.setCanContinue(true);
				ruleResult.setMessage("Not applicable to this business area");
			}
			return ruleResult;
		} catch (DatabaseException e) {
			e.printStackTrace();
			ruleResult.setCanContinue(true);
			ruleResult.setMessage("Error occurred, hence did not apply this rule");
			return ruleResult;
		}	
	}

	/* (non-Javadoc)
	 * @see transbit.tbits.api.IRule#getName()
	 */
	public String getName() {
		return "IDCStartAndFinishDates - If \"Transmit To Client and IDC required\" are true then IDC start and finish dates cannot be null";
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
