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
 * <code>NumberOfSheetsRule</code> ensures that the number of sheets to be mentioned for a drawing it can be
 * transmitted. This is achieved by checking if the field "No of Sheets" contains an integer value greater 
 * than 0 when the fields "Transmit to Client" is set to true.
 */
public class NumberOfSheetsRule implements IRule {

	private static final String NO_OF_SHEETS = "NoofSheets";
	private static final String TRANSMIT_DOC_CLIENT = "TransmitDocClient";

	/* (non-Javadoc)
	 * @see transbit.tbits.api.IRule#execute(java.sql.Connection, transbit.tbits.domain.BusinessArea, transbit.tbits.domain.Request, transbit.tbits.domain.Request, int, transbit.tbits.domain.User, java.util.Hashtable, boolean, java.util.Collection)
	 */
	public RuleResult execute(Connection connection, BusinessArea ba, Request oldRequest,
			Request currentRequest, int Source, User user,
			Hashtable<Field, RequestEx> extendedFields, boolean isAddRequest,
			Collection<AttachmentInfo> attachments){
		int aSystemId = ba.getSystemId();		
		RuleResult ruleResult = new RuleResult();
		boolean isApplicable = PyramidUtils.inProperty345SysPrefixes(ba.getSystemPrefix());
		if (isApplicable){
			try {
				Field ttcField = Field.lookupBySystemIdAndFieldName(aSystemId, TRANSMIT_DOC_CLIENT);
				if (ttcField == null){
					ruleResult.setCanContinue(true);
					ruleResult.setMessage("Not applicable as field \"transmit to client\" does not exist");
				}
				else
				{
					RequestEx ttcReqEx = extendedFields.get(ttcField);
					if (ttcReqEx.getBitValue()){
						Field numSheetsField = Field.lookupBySystemIdAndFieldName(aSystemId, NO_OF_SHEETS);
						if (numSheetsField == null){
							ruleResult.setCanContinue(true);
							ruleResult.setMessage("Not applicable as field \"NoofSheets\" does not exist");
						}
						else{
							RequestEx numSheetsReqEx = extendedFields.get(numSheetsField);
							if (numSheetsReqEx.getIntValue() > 0){
								ruleResult.setCanContinue(true);
								ruleResult.setSuccessful(true);
							}
							else{
								ruleResult.setCanContinue(false);
								ruleResult.setMessage("Please mention number of sheets if you want to set \"transmit to client\" to true");
							}
						}
					}
					else{
						ruleResult.setCanContinue(true);
						ruleResult.setMessage("Not applicable as field \"transmit to client\" is not true");
					}				
				}
			} catch (DatabaseException e) {
				e.printStackTrace();
			}
		}
		else{
			ruleResult.setCanContinue(true);
			ruleResult.setMessage("Not applicable to the business area: " + ba.getName());
		}
		return ruleResult; 
	}

	/* (non-Javadoc)
	 * @see transbit.tbits.api.IRule#getName()
	 */
	public String getName() {
		return "NumberOfSheets - If transmit to client is true, number of sheets must be greater than 0";
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
