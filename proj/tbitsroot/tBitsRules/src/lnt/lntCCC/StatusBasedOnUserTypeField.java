                                                                     
                                                                     
                                                                     
                                             
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
import transbit.tbits.domain.Request;
import transbit.tbits.domain.Type;
import transbit.tbits.domain.User;

/**
 * @author Lokesh
 *
 */
public class StatusBasedOnUserTypeField implements IRule {

	private static final String NIL 				= "Nil";
	private static final String USER_TYPE_LE_MECH	= "LEMech";
	private static final String USER_TYPE_LE_CNI	= "LECnI";
	private static final String USER_TYPE_LE_ELEC	= "LEElect";
	private static final String USER_TYPE_SCM       = "ResponsibleSCM";
	private static final String USER_TYPE_HOD       = "HOD";
	private static final String TYPE_PENDING		= "Pending";
	
	private static final String LE_CNI_DECISION 	= "LECnIDecision";
	private static final String LE_MECH_DECISION 	= "LEMechDecision";
	private static final String LE_ELECT_DECISION 	= "LEElectDecision";
	private static final String SCM_DECISION     	= "DecisionfromSCM";
	private static final String HOD_DECISION     	= "DecisionfromHOD";
	
	/* (non-Javadoc)
	 * @see transbit.tbits.api.IRule#execute(java.sql.Connection, transbit.tbits.domain.BusinessArea, transbit.tbits.domain.Request, transbit.tbits.domain.Request, int, transbit.tbits.domain.User, boolean)
	 */
	@Override
	public RuleResult execute(Connection connection, BusinessArea ba,
			Request oldRequest, Request currentRequest, int Source, User user,
			boolean isAddRequest) {

		String baListStr = PropertiesHandler.getProperty(LnTCCCUtils.PLUGIN_LNTCCC_RULES_BALIST);
		boolean isApplicableBA = LnTCCCUtils.isApplicableBA(baListStr, ba);
		
		RuleResult ruleResult = new RuleResult();
		if (!isApplicableBA)
			return ruleResult;
		
		int systemId = ba.getSystemId();
		String cniUsers = currentRequest.get(USER_TYPE_LE_CNI);
		setTypeValue(systemId, currentRequest, cniUsers, LE_CNI_DECISION);
		
		String elecUsers = currentRequest.get(USER_TYPE_LE_ELEC);
		setTypeValue(systemId, currentRequest, elecUsers, LE_ELECT_DECISION);
		
		String mechUsers = currentRequest.get(USER_TYPE_LE_MECH);
		setTypeValue(systemId, currentRequest, mechUsers, LE_MECH_DECISION);	

		String scmUsers = currentRequest.get(USER_TYPE_SCM);
		setTypeValue(systemId, currentRequest, scmUsers, SCM_DECISION);	
		
		String hodUsers = currentRequest.get(USER_TYPE_HOD);
		setTypeValue(systemId, currentRequest, 	hodUsers, HOD_DECISION);
		return ruleResult;
	}

	private void setTypeValue(int systemId, Request currentRequest,
			String usersType, String fieldName) {
		
		Type prevType = (Type)currentRequest.getObject(fieldName);
		if ((prevType != null) && prevType.getName().equals(NIL)){
			if ((usersType != null) && (usersType.trim().length() != 0)){
				try {
					Type pendingType = Type.lookupBySystemIdAndFieldNameAndTypeName(systemId, fieldName, TYPE_PENDING);
					if (pendingType != null)
						currentRequest.setObject(fieldName, pendingType);
				} catch (DatabaseException e) {
					e.printStackTrace();
				}			
			}
		}
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
		// TODO Auto-generated method stub
		return 0;
	}

}
