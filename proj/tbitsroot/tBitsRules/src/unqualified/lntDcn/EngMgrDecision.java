package lntDcn;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.List;

import transbit.tbits.api.IRule;
import transbit.tbits.api.RuleResult;
import transbit.tbits.common.DatabaseException;
import transbit.tbits.common.PropertiesHandler;
import transbit.tbits.domain.BusinessArea;
import transbit.tbits.domain.Request;
import transbit.tbits.domain.User;
import transbit.tbits.domain.Field;
import transbit.tbits.domain.Request;
import transbit.tbits.domain.RequestEx;
import transbit.tbits.domain.Type;
import transbit.tbits.domain.User;

public class EngMgrDecision implements IRule {

	public RuleResult execute(Connection connection, BusinessArea ba,
			Request oldRequest, Request currentRequest, int Source, User user,
			boolean isAddRequest) {


		RuleResult ruleResult = new RuleResult();

		final String dcnBa = "DCN";
		final String DecisionFieldName = "EnggMgrDecision";
		final String DecisionField2Name = "EnggMgrDecision2";
		final String DecisionField3Name = "EnggMgrDecision3";
		final String PDDecisionFieldName = "ProjectDirectorDecision";
		final String DCN_STATUS = "dcn_status";
		final String REJECTED = "Rejected";
		final String CLOSED = "Closed";
		final String APPROVED = "Approved";
		final String DCN_APPROVED = "approved";

		String sysPrefix = ba.getSystemPrefix();
		int sysId = ba.getSystemId();
		
		String baListStr = PropertiesHandler.getProperty("plugins.lntDCN.EngMgrDecision.baList");
		boolean isApplicableBA = isApplicableBA(baListStr, ba);
		

         if (isApplicableBA) {
        	 
		
		  try {
			Field decisionField = Field.lookupBySystemIdAndFieldName(sysId, DecisionFieldName);
			Field decisionField2 = Field.lookupBySystemIdAndFieldName(sysId, DecisionField2Name);
			Field decisionField3 = Field.lookupBySystemIdAndFieldName(sysId, DecisionField3Name);
			Field PDDecisionField = Field.lookupBySystemIdAndFieldName(sysId, PDDecisionFieldName);
			Field DCN_STATUSField = Field.lookupBySystemIdAndFieldName(sysId, DCN_STATUS);
			
			
		if (null != decisionField
				&& null != decisionField2
				&& null != decisionField3
				&& null != PDDecisionField 
				&& null != DCN_STATUSField ) {
			
			Type EngMgrType = (Type) currentRequest.getObject(DecisionFieldName);
			Type EngMgr2Type = (Type) currentRequest.getObject(DecisionField2Name);
			Type EngMgr3Type = (Type) currentRequest.getObject(DecisionField3Name);
			Type PDType = (Type) currentRequest.getObject(PDDecisionFieldName);
			Type dcnStatusType = (Type) currentRequest.getObject(DCN_STATUS);

			System.out.println("BA type :" + ba.getType());

				if (EngMgrType.getName().equalsIgnoreCase(REJECTED)
						|| EngMgr2Type.getName().equalsIgnoreCase(REJECTED)
						|| EngMgr3Type.getName().equalsIgnoreCase(REJECTED)
						|| PDType.getName().equalsIgnoreCase(REJECTED)) {
					System.out.println("inside decision if block :"
							+ EngMgrType.getName());
					dcnStatusType.setName(CLOSED);
					ruleResult.setMessage("Dcn Status Changed as Closed");
				}
				
				if (EngMgrType.getName().equalsIgnoreCase(APPROVED)
						&& EngMgr2Type.getName().equalsIgnoreCase(APPROVED)
						&& EngMgr3Type.getName().equalsIgnoreCase(APPROVED)
						&& PDType.getName().equalsIgnoreCase(APPROVED)) {
					
					System.out.println("inside decision if block :"+ EngMgrType.getName());
					dcnStatusType.setName(DCN_APPROVED);
					ruleResult.setMessage("Dcn Status Changed asapproved");
				}
		} else {
			
			System.out.println("All EngMgrDecision fields are not present in current BA.");

		}
		
		
		} catch (DatabaseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		}
		
		return ruleResult;

	}

	public String getName() {
		return "DCN status in  business areas changed as Decision.";
	}

	public double getSequence() {
		return 0;
	}
	
	public static boolean isApplicableBA(String baListStr, BusinessArea ba) {
			boolean isApplicableBA = false;
			if ((baListStr != null) && (baListStr.trim().length() != 0)){
				 List<String> baList = Arrays.asList(baListStr.split(","));			
				if (baList.contains(ba.getSystemPrefix()))
					isApplicableBA  = true;
			}
			return isApplicableBA;
		}


}
