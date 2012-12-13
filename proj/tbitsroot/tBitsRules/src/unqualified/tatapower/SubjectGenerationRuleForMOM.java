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
public class SubjectGenerationRuleForMOM implements IRule {

	private static final String ACTION_ITEM = "ActionItem";
	private static final String TATAPOWER_SYS_PREFIX_MOM_SUBJECT_RULE = "tatapower.sys_prefix_mom_subject_rule";

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
		
		String applicableBA = TataPowerUtils.getProperty(TATAPOWER_SYS_PREFIX_MOM_SUBJECT_RULE);
		if (applicableBA == null){
			ruleResult.setCanContinue(true);
			ruleResult.setMessage("Could not find the property in configuration file");
			return ruleResult;
		}
		
		boolean isApplicable = TataPowerUtils.isExistsInString(applicableBA, sysPrefix);
		if (isApplicable && isAddRequest){
			try {
				String activityArea = currentRequest.getSubject().trim();
				Field actionItemField = Field.lookupBySystemIdAndFieldName(aSystemId, ACTION_ITEM);
				if (actionItemField != null){
					RequestEx aiReqEx = extendedFields.get(actionItemField);
					if ((aiReqEx != null) && (!activityArea.equals(""))){
						String subject = activityArea + "-" + aiReqEx.getVarcharValue();			
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
		return this.getClass().getSimpleName() + " - Sets the subject in business area 'MOM' as subjec + actionitem";
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
