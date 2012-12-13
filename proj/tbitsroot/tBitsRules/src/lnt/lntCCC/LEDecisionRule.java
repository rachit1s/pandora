/**
 * 
 */
package lntCCC;

import java.sql.Connection;

import transbit.tbits.api.IRule;
import transbit.tbits.api.RuleResult;
import transbit.tbits.common.DatabaseException;
import transbit.tbits.common.PropertiesHandler;
import transbit.tbits.domain.BusinessArea;
import transbit.tbits.domain.Field;
import transbit.tbits.domain.Request;
import transbit.tbits.domain.Type;
import transbit.tbits.domain.User;

/**
 * @author lokesh
 *
 */
public class LEDecisionRule implements IRule {

	private static final String TYPE_APPROVED 		= "Approved";
	private static final String TYPE_AUTHORIZED		= "Authorized";
	private static final String TYPE_NIL 			= "Nil";
	private static final String TYPE_REVIEWED 		= "Reviewed";
	private static final String LE_CNI_DECISION 	= "LECnIDecision";
	private static final String LE_MECH_DECISION 	= "LEMechDecision";
	private static final String LE_ELECT_DECISION 	= "LEElectDecision";
	private static final String CCC_HO 				= "CCC_HO";

	/* (non-Javadoc)
	 * @see transbit.tbits.api.IRule#execute(java.sql.Connection, transbit.tbits.domain.BusinessArea, transbit.tbits.domain.Request, transbit.tbits.domain.Request, int, transbit.tbits.domain.User, boolean)
	 */
	@Override
	public RuleResult execute(Connection connection, BusinessArea ba,
			Request oldRequest, Request currentRequest, int Source, User user,
			boolean isAddRequest) {
		
		RuleResult ruleResult = new RuleResult();
		String baListStr = PropertiesHandler.getProperty(LnTCCCUtils.PLUGIN_LNTCCC_RULES_BALIST);
		boolean isApplicableBA = LnTCCCUtils.isApplicableBA(baListStr, ba);
		
		
		if (!isApplicableBA || isAddRequest){
			return ruleResult;
		}
		
		Type statusType = currentRequest.getStatusId();
		if (statusType != null){
			String statusName = statusType.getName();
			//Don't have to continue if one of the following types are set.
			if (statusName.equals(TYPE_APPROVED) || statusName.equals(TYPE_AUTHORIZED))
				return ruleResult;
			
			int systemId = ba.getSystemId();

			String elecDec = currentRequest.get(LE_ELECT_DECISION);
			String mechDec = currentRequest.get(LE_MECH_DECISION);
			String cniDec = currentRequest.get(LE_CNI_DECISION);

			if (isPermissiveValue(elecDec) && isPermissiveValue(mechDec) && isPermissiveValue(cniDec)){
				Type reviewedType;
				try {
					reviewedType = Type.lookupBySystemIdAndFieldNameAndTypeName(systemId, Field.STATUS, TYPE_REVIEWED);
					if (reviewedType != null)
						currentRequest.setStatusId(reviewedType);
				} catch (DatabaseException e) {
					e.printStackTrace();
				}
			}
		}
		
		return ruleResult;
	}

	private boolean isPermissiveValue(String decision) {
		if (decision.equals(TYPE_NIL) || decision.equals(TYPE_APPROVED))
			return true;
		return false;
	}

	/* (non-Javadoc)
	 * @see transbit.tbits.api.IRule#getName()
	 */
	@Override
	public String getName() {
		return this.getClass().getSimpleName();
	}

	/* (non-Javadoc)
	 * @see transbit.tbits.api.IRule#getSequence()
	 */
	@Override
	public double getSequence() {
		return 101;
	}

}
