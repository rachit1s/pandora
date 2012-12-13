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
public class MaterialReconBACategoryBasedStatus implements IRule {

	private static final String TATAPOWER_SYS_PREFIX_MTRL_RECON = "tatapower.sys_prefix_MtrlRecon";
	
	//Category
	private static final String INCOMING_MATERIAL = "Incoming_Material";
	private static final String OUTGOING_NON_RETURNABLE_MATERIAL = "Outgoing_Non_Returnable_Material";
	private static final String OUTGOING_RETURNABLE_MATERIAL = "Outgoing_Returnable_Material";
	
	//Status
	private static final String MATERIAL_SENT_OUT = "Suspended";
	private static final String MATERIAL_RETURN_PENDING = "Active";
	private static final String GRN_PENDING = "Pending";

	/* (non-Javadoc)
	 * @see transbit.tbits.api.IRule#execute(transbit.tbits.domain.BusinessArea, transbit.tbits.domain.Request, transbit.tbits.domain.Request, int, transbit.tbits.domain.User, java.util.Hashtable, boolean, java.lang.String)
	 */
	public RuleResult execute(Connection connection, BusinessArea ba,
			Request oldRequest, Request currentRequest, int Source, User user,
			Hashtable<Field, RequestEx> extendedFields, boolean isAddRequest,
			Collection<AttachmentInfo> attachments) {
		
		RuleResult ruleResult = new RuleResult();
		
		String parentString = TataPowerUtils.getProperty(TATAPOWER_SYS_PREFIX_MTRL_RECON);
		boolean isApplicableToBA = TataPowerUtils.isExistsInString(parentString , ba.getSystemPrefix());
		
		if (isApplicableToBA && isAddRequest){
			Type catType = currentRequest.getCategoryId();
			String curCatName = catType.getName();
			int aSystemId = ba.getSystemId();		
			try {
				Type statusId = null;
				if (curCatName.equals(INCOMING_MATERIAL)){
					statusId = Type.lookupAllBySystemIdAndFieldNameAndTypeName(aSystemId, Field.STATUS, GRN_PENDING);
				}
				else if (curCatName.equals(OUTGOING_RETURNABLE_MATERIAL)){
					statusId = Type.lookupAllBySystemIdAndFieldNameAndTypeName(aSystemId, Field.STATUS, MATERIAL_RETURN_PENDING);
				}
				else if (curCatName.equals(OUTGOING_NON_RETURNABLE_MATERIAL)){
					statusId = Type.lookupAllBySystemIdAndFieldNameAndTypeName(aSystemId, Field.STATUS, MATERIAL_SENT_OUT);					
				}
				if (statusId != null){
					currentRequest.setStatusId(statusId);
					ruleResult.setCanContinue(true);
					ruleResult.setSuccessful(true);
				}
				else{
					ruleResult.setCanContinue(true);
					ruleResult.setMessage("Not applicable for current status");
				}
			} catch (DatabaseException e) {
				e.printStackTrace();
			}
				
		}
		else{
			ruleResult.setMessage("Not required.");
			ruleResult.setCanContinue(true);
		}
		return ruleResult;
	}

	/* (non-Javadoc)
	 * @see transbit.tbits.api.IRule#getName()
	 */
	public String getName() {
		return this.getClass().getSimpleName() + " - sets the status based on the category type." ;
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
