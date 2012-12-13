/**
 * 
 */
package tatapower;

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
 * @author Lokesh
 *
 */
public class SubjectGenerationRule implements IRule {

	private static final String INVOICENO = "invoiceno";
	private static final String WORKORDERNO = "workorderno1";
	private static final String CONTRACTORNAME = "contractorname1";

	/* (non-Javadoc)
	 * @see transbit.tbits.api.IRule#execute(transbit.tbits.domain.BusinessArea, transbit.tbits.domain.Request, transbit.tbits.domain.Request, int, transbit.tbits.domain.User, java.util.Hashtable, boolean, java.lang.String)
	 */
	public RuleResult execute(Connection connection, BusinessArea ba,
			Request oldRequest, Request currentRequest, int Source, User user,
			Hashtable<Field, RequestEx> extendedFields, boolean isAddRequest,
			Collection<AttachmentInfo> attachments) {
		RuleResult ruleResult = new RuleResult();
		String sysPrefix = ba.getSystemPrefix();
		int aSystemId = ba.getSystemId();
		boolean isApplicable = TataPowerUtils.isExistsInBillBABasedRulesSysPrefixes(sysPrefix);
		if (isApplicable && isAddRequest){
			try {
				Field cnField = Field.lookupBySystemIdAndFieldName(aSystemId, CONTRACTORNAME);
				Field woField = Field.lookupBySystemIdAndFieldName(aSystemId, WORKORDERNO);
				Field invoiceField = Field.lookupBySystemIdAndFieldName(aSystemId, INVOICENO);
				if ((cnField != null) && (woField != null) && (invoiceField != null)){
					RequestEx cnReqEx = extendedFields.get(cnField);
					RequestEx woRequestEx = extendedFields.get(woField);
					RequestEx invRequestEx = extendedFields.get(invoiceField);
					if ((cnReqEx != null) && (woRequestEx != null) && (invRequestEx != null)){
						String subject = cnReqEx.getVarcharValue() + "-" + woRequestEx.getVarcharValue()
												+ "-" + invRequestEx.getVarcharValue();					
						currentRequest.setSubject(subject);
						ruleResult.setCanContinue(true);
						ruleResult.setSuccessful(true);
					}
					else{
						ruleResult.setCanContinue(true);
						ruleResult.setMessage("Could not find values of the required fields.");
					}
				}
				else{
					ruleResult.setCanContinue(true);
					ruleResult.setMessage("Required fields to generate subject were not found.");
				}
			} catch (DatabaseException e) {
				e.printStackTrace();
			} 
		}			
		return ruleResult;
	}

	/* (non-Javadoc)
	 * @see transbit.tbits.api.IRule#getName()
	 */
	public String getName() {
		return "SubjectGenerationRule - The subject generated will be the combination of 'Contractor Name', 'Work Order Number'" +
				" and invoice number.";
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
	 */
	public static void main(String[] args) throws DatabaseException {
		/*BusinessArea ba =BusinessArea.lookupBySystemPrefix("Bill");
		User root = User.lookupAllByUserId(1);
		IRule irule = new SubjectGenerationRule();
		irule.execute(ba, null, req, TBitsConstants.SOURCE_CMDLINE, root, 
				req.getExtendedFields(), false, "");
		 */
	}

}
