/**
 * 
 */
package lntFcnDcn;

import java.sql.Connection;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import transbit.tbits.api.IRule;
import transbit.tbits.api.RuleResult;
import transbit.tbits.common.DatabaseException;
import transbit.tbits.common.PropertiesHandler;
import transbit.tbits.domain.BusinessArea;
import transbit.tbits.domain.Field;
import transbit.tbits.domain.Request;
import transbit.tbits.domain.RequestUser;
import transbit.tbits.domain.Type;
import transbit.tbits.domain.User;

/**
 * @author lokesh
 *
 */
public class ExecMgrDecisionRule implements IRule {

	private static final String DCN_FLOW_STATUS = "dcnflowstatus";
	private static final String NOT_APPLICALBE = "NotApplicalbe";
	private static final String ENG_MGR_DECISION = "EnggMgrDecision";
	private static final String TYPE_APPROVED 		= "Approved";
	private static final String TYPE_REJECTED		= "Rejected";
	private static final String TYPE_NONE			= "None";
	
	private static final String EXEC_MGR_DECISION 	= "ExecMgrDecision";
	private static final String EXEC_MGR_DECISION2 	= "ExecMgrDecision2";
	private static final String EXEC_MGR_DECISION3 	= "ExecMgrDecision3";
	
	private static final String PROJ_EXEC_MGR       = "ProjExecMgr";
	private static final String PROJ_EXEC_MGR2      = "ProjExecMgr2";
	private static final String PROJ_EXEC_MGR3      = "ProjExecMgr3";
	
	private static final String TYPE_DEC_PENDING_WITH_EM = "decisionpendingwithEM";
	private static final String TYPE_DEC_PENDING_WITH_PEM = "decisionpendingwithPEM";
	private static final String TYPE_DEC_PENDING_WITH_PD = "decisionpendingwithPD";
	private static final String PLUGIN_LNT_SO_RULES_BALIST = "plugin.lntFcnDcn.ExecMgrDecisionRule.baList";

	/* (non-Javadoc)
	 * @see transbit.tbits.api.IRule#execute(java.sql.Connection, transbit.tbits.domain.BusinessArea, transbit.tbits.domain.Request, transbit.tbits.domain.Request, int, transbit.tbits.domain.User, boolean)
	 */
	@Override
	public RuleResult execute(Connection connection, BusinessArea ba,
			Request oldRequest, Request currentRequest, int Source, User user,
			boolean isAddRequest) {
		
		RuleResult ruleResult = new RuleResult();
		String baListStr = PropertiesHandler.getProperty(PLUGIN_LNT_SO_RULES_BALIST);
		boolean isApplicableBA = false;
		if (baListStr != null){
			List<String> baList = Arrays.asList(baListStr.trim().split(","));
			if (baList.contains(ba.getSystemPrefix()))
				isApplicableBA = true;
		}
				
		if (!isApplicableBA || isAddRequest){
			return ruleResult;
		}
		
		Type statusType = (Type) currentRequest.getObject(DCN_FLOW_STATUS);
		if (statusType != null){
			String statusName = statusType.getName();
			//Don't have to continue if one of the following types are set.
			if (statusName.equals(TYPE_APPROVED) 
					|| statusName.equals(TYPE_REJECTED) 
					|| statusName.equals(TYPE_DEC_PENDING_WITH_PD))
				return ruleResult;

			int systemId = ba.getSystemId();

			try{
				if (statusName.equals(TYPE_DEC_PENDING_WITH_EM)){
					String emDecision = currentRequest.get(ENG_MGR_DECISION);
					if (emDecision != null){
						if (emDecision.trim().equals(TYPE_APPROVED)){
							Type pemType = Type.lookupBySystemIdAndFieldNameAndTypeName(systemId, 
									DCN_FLOW_STATUS, TYPE_DEC_PENDING_WITH_PEM);
							//currentRequest.setStatusId(pemType);
							currentRequest.setExType(DCN_FLOW_STATUS,pemType);
							return ruleResult;
						}
						else if (emDecision.trim().equals(TYPE_REJECTED)){
							Type pemType = Type.lookupBySystemIdAndFieldNameAndTypeName(systemId, 
									DCN_FLOW_STATUS, TYPE_REJECTED);
							//currentRequest.setStatusId(pemType);
							currentRequest.setExType(DCN_FLOW_STATUS,pemType);
							return ruleResult;
						}
					}
				}
				else if (statusName.equals(TYPE_DEC_PENDING_WITH_PEM)){
					String decision = getDecisionType(currentRequest, PROJ_EXEC_MGR, EXEC_MGR_DECISION, NOT_APPLICALBE);
					decision = getDecisionType(currentRequest, PROJ_EXEC_MGR2, EXEC_MGR_DECISION2, decision);
					decision = getDecisionType(currentRequest, PROJ_EXEC_MGR3, EXEC_MGR_DECISION3, decision);
					System.out.println("decision from EM:@@@@@@:"+decision);
					Type finalType = null;
					if (decision.equals(TYPE_REJECTED))
						finalType = Type.lookupAllBySystemIdAndFieldNameAndTypeName(systemId, DCN_FLOW_STATUS, TYPE_REJECTED);
					else if (decision.equals(TYPE_APPROVED))
						finalType = Type.lookupAllBySystemIdAndFieldNameAndTypeName(systemId, DCN_FLOW_STATUS, TYPE_DEC_PENDING_WITH_PD);
					if (finalType != null)
						//currentRequest.setStatusId(finalType);
						currentRequest.setExType(DCN_FLOW_STATUS,finalType);
				}
			}catch(DatabaseException dbe){
				dbe.printStackTrace();
			}
		}
		
		return ruleResult;
	}
	
	private static String getDecisionType (Request currentRequest, 
			String userTypeFieldName, String decFieldName, String prevDecision) 
	throws DatabaseException{
		String decision = null;
		if (prevDecision.equals(TYPE_REJECTED))
			return TYPE_REJECTED;
		
		Collection<RequestUser> prjExecMGR = currentRequest.getExUserType(userTypeFieldName);
		if ((prjExecMGR != null) && (!prjExecMGR.isEmpty())){
			String execMgrDec = currentRequest.get(decFieldName);
			if (execMgrDec != null){
				if (execMgrDec.trim().equals(TYPE_REJECTED)){
					decision = TYPE_REJECTED;					
				}
				else if (execMgrDec.trim().equals(TYPE_APPROVED) 
							&& (prevDecision.equals(TYPE_APPROVED)|| (prevDecision.equals(NOT_APPLICALBE)))){
					decision = TYPE_APPROVED;
				}
				else
					decision = TYPE_NONE;
			}						
		}
		else{
			decision = prevDecision;
		}
		return decision;
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
