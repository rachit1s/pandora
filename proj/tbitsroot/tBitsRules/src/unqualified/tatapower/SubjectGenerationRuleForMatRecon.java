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
public class SubjectGenerationRuleForMatRecon implements IRule {
	
	private static final String TATAPOWER_SYS_PREFIX_MTRL_RECON = "tatapower.sys_prefix_MtrlRecon";
	
	//Category
	private static final String INCOMING_MATERIAL = "Incoming_Material";
	private static final String OUTGOING_NON_RETURNABLE_MATERIAL = "Outgoing_Non_Returnable_Material";
	private static final String OUTGOING_RETURNABLE_MATERIAL = "Outgoing_Returnable_Material";

	
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
		if (parentString == null){
			ruleResult.setMessage("Could not find the required property from configuration file. Hence continuing.");
			return ruleResult;
		}
		
		boolean isApplicableBA = TataPowerUtils.isExistsInString(parentString, ba.getSystemPrefix());
		
		if (isApplicableBA && isAddRequest){
			Type catType = currentRequest.getCategoryId();
			String catName = catType.getName();
			int aSystemId = ba.getSystemId();
			
			if (catName.equals(INCOMING_MATERIAL)){
				setSubjectWithPartyNameAndCategory(currentRequest, extendedFields, ruleResult,
						aSystemId, "I_PartyName", INCOMING_MATERIAL);
			}
			else if (catName.equals(OUTGOING_RETURNABLE_MATERIAL)){
				setSubjectWithPartyNameAndCategory(currentRequest, extendedFields, ruleResult,
						aSystemId, "O_PartyName", OUTGOING_RETURNABLE_MATERIAL);
			}
			else if (catName.equals(OUTGOING_NON_RETURNABLE_MATERIAL)){
				setSubjectWithPartyNameAndCategory(currentRequest, extendedFields, ruleResult,
						aSystemId, "O_PartyName", OUTGOING_NON_RETURNABLE_MATERIAL);
			}
		}		
		return ruleResult;
	}

	/**
	 * @param currentRequest
	 * @param extendedFields
	 * @param ruleResult
	 * @param aSystemId
	 * @param fieldName
	 * @param categoryName
	 */
	private void setSubjectWithPartyNameAndCategory(Request currentRequest,
			Hashtable<Field, RequestEx> extendedFields, RuleResult ruleResult,
			int aSystemId, String fieldName, String categoryName) {
		Field partyNameField;
		try {
			partyNameField = Field.lookupBySystemIdAndFieldName(aSystemId, fieldName);
			if (partyNameField != null){
				RequestEx reqEx = extendedFields.get(partyNameField);
				String varcharValue = reqEx.getVarcharValue();
				if ((varcharValue != null) && (!varcharValue.trim().equals(""))){
					currentRequest.setSubject(varcharValue + "-" + categoryName);
				}
				else{
					currentRequest.setSubject(categoryName);
				}
				ruleResult.setSuccessful(true);
			}
		} catch (DatabaseException e) {
			e.printStackTrace();
		}
	}

	/* (non-Javadoc)
	 * @see transbit.tbits.api.IRule#getName()
	 */
	public String getName() {
		return this.getClass().getSimpleName() + "- Sets the subject of Material Recon BA with party name and category.";
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
