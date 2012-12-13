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
import transbit.tbits.domain.Type;
import transbit.tbits.domain.User;

/**
 * @author Lokesh
 *
 */
public class MaterialReconBAGRNNumberStatus implements IRule {
	
	private static final String GRN_COMPLETE = "Closed";
	private static final String I_GRN = "I_GRN";
	private static final String TATAPOWER_SYS_PREFIX_MTRL_RECON = "tatapower.sys_prefix_MtrlRecon";

	/* (non-Javadoc)
	 * @see transbit.tbits.api.IRule#execute(transbit.tbits.domain.BusinessArea, transbit.tbits.domain.Request, transbit.tbits.domain.Request, int, transbit.tbits.domain.User, java.util.Hashtable, boolean, java.lang.String)
	 */
	public RuleResult execute(Connection connection, BusinessArea ba,
			Request oldRequest, Request currentRequest, int Source, User user,
			Hashtable<Field, RequestEx> extendedFields, boolean isAddRequest,
			Collection<AttachmentInfo> attachments) {
		RuleResult ruleResult = new RuleResult();
		ruleResult.setCanContinue(true);
		String parentString = TataPowerUtils.getProperty(TATAPOWER_SYS_PREFIX_MTRL_RECON);
		boolean isApplicableToBA = TataPowerUtils.isExistsInString(parentString , ba.getSystemPrefix());
		if (isApplicableToBA){
			int aSystemId = ba.getSystemId();
			try {
				Type statusId = null;				
				Field grnNumField = Field.lookupBySystemIdAndFieldName(aSystemId, I_GRN);
				if (grnNumField != null){
					RequestEx grnNumEx = extendedFields.get(grnNumField);
					if (grnNumEx != null){
						boolean wasEmpty = false;
						if (!isAddRequest){
							Hashtable<Field, RequestEx> extendedFields2 = oldRequest.getExtendedFields();
							RequestEx prevRequestEx = extendedFields2.get(grnNumField);							
							if ((prevRequestEx != null) && (prevRequestEx.getVarcharValue() == null))
								wasEmpty = true;
						}
						
						String grnVarcharValue = grnNumEx.getVarcharValue();
						if ((isAddRequest || wasEmpty) && (grnVarcharValue != null) && (!grnVarcharValue.trim().equals(""))){
							statusId = Type.lookupAllBySystemIdAndFieldNameAndTypeName(aSystemId, Field.STATUS, GRN_COMPLETE);
							currentRequest.setStatusId(statusId);
							ruleResult.setSuccessful(true);
						}
						else{
							ruleResult.setMessage("Not applicable");
						}
					}
				}
			} catch (DatabaseException e) {
				e.printStackTrace();
			}
		}
		else{
			ruleResult.setCanContinue(true);
			ruleResult.setMessage("Not applicable to this BA: " + ba.getDisplayName());
		}
		return ruleResult;
	}

	/* (non-Javadoc)
	 * @see transbit.tbits.api.IRule#getName()
	 */
	public String getName() {
		return this.getClass().getSimpleName() + " - sets the status based on GRN No";
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
