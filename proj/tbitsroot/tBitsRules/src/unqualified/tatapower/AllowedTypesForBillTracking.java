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
public class AllowedTypesForBillTracking implements IRule {

	/* (non-Javadoc)
	 * @see transbit.tbits.api.IRule#execute(transbit.tbits.domain.BusinessArea, transbit.tbits.domain.Request, transbit.tbits.domain.Request, int, transbit.tbits.domain.User, java.util.Hashtable, boolean, java.lang.String)
	 */
	public RuleResult execute(Connection connection, BusinessArea ba,
			Request oldRequest, Request currentRequest, int Source, User user,
			Hashtable<Field, RequestEx> extendedFields, boolean isAddRequest,
			Collection<AttachmentInfo> attachments) {
		Type requestType = currentRequest.getRequestTypeId();
		String reqTypeName = requestType.getName().trim();
		Type categoryType = currentRequest.getCategoryId();
		String catTypeName = categoryType.getName();
		boolean isApplicable = TataPowerUtils.isExistsInBillBABasedRulesSysPrefixes(ba.getSystemPrefix());
		RuleResult ruleResult = new RuleResult();
		if (isApplicable){
			if (((catTypeName.equals(TataPowerUtils.CIVIL) ||  catTypeName.equals(TataPowerUtils.MECHANICAL))&& 
					(!reqTypeName.equals(TataPowerUtils.TYPE_OTHERS))&& 
					(!reqTypeName.equals(TataPowerUtils.TYPE_SIXTY_PERCENT)) &&
					(!reqTypeName.equals(TataPowerUtils.TYPE_FORTY_PERCENT))) &&
					(!reqTypeName.equals(TataPowerUtils.TYPE_RA100)) && 
					(!reqTypeName.equals(TataPowerUtils.TYPE_GRN))){
				ruleResult.setCanContinue(false);
				ruleResult.setMessage("If 'Department' is 'Civil' or 'Mechanical', then 'Type' has to be either " +
				"'60% Payment' or '40% Payment' or 'RA100' or " + TataPowerUtils.TYPE_GRN + ".");
			}
			/*if ((catTypeName.equals(TataPowerUtils.CIVIL) ||  catTypeName.equals(TataPowerUtils.MECHANICAL))&& 
					(!reqTypeName.equals(TataPowerUtils.TYPE_RA100))){
				ruleResult.setCanContinue(false);
				ruleResult.setMessage("If 'Department' is 'Civil' or 'Mechanical' then 'Type' has to be RA100.");
			}*/
			else if (catTypeName.equals(TataPowerUtils.ADMINISTRATION) 
					&& ((!reqTypeName.equals(TataPowerUtils.TYPE_ADMIN_WITH_SAP_PO)) 
							&& (!reqTypeName.equals(TataPowerUtils.TYPE_ADMIN_WITHOUT_SAP_PO))
							&& (!reqTypeName.equals(TataPowerUtils.TYPE_OTHERS))
							&& (!reqTypeName.equals(TataPowerUtils.TYPE_GRN)))){
				ruleResult.setCanContinue(false);
				ruleResult.setMessage("If 'Department' is 'Administration' then 'Type' has to be either 'Admin With SAP PO' or 'Admin WithoutSAP PO' or " +
						TataPowerUtils.TYPE_GRN + " or 'Others'.");				
			}
			else if (catTypeName.equals(TataPowerUtils.ELECTRICAL) 
					&& (!reqTypeName.equals(TataPowerUtils.TYPE_ELECTRICAL_RUNNING))
					&& (!reqTypeName.equals(TataPowerUtils.TYPE_ELECTRICAL_FINAL))
					&& (!reqTypeName.equals(TataPowerUtils.TYPE_ELECTRICAL_THIRTY))
					&& (!reqTypeName.equals(TataPowerUtils.TYPE_OTHERS))
					&& (!reqTypeName.equals(TataPowerUtils.TYPE_GRN))){
				try {
					int systemId = ba.getSystemId();
					Type eType1 = Type.lookupBySystemIdAndFieldNameAndTypeName(systemId, Field.REQUEST_TYPE, TataPowerUtils.TYPE_ELECTRICAL_RUNNING);
					Type eType2 = Type.lookupBySystemIdAndFieldNameAndTypeName(systemId, Field.REQUEST_TYPE, TataPowerUtils.TYPE_ELECTRICAL_FINAL);
					Type eType3 = Type.lookupBySystemIdAndFieldNameAndTypeName(systemId, Field.REQUEST_TYPE, TataPowerUtils.TYPE_ELECTRICAL_THIRTY);
					ruleResult.setCanContinue(false);
					ruleResult.setMessage("If 'Department' is 'Electrical' then 'Type' has to be either '" + eType1.getDisplayName() + "'/'"
												+ eType2.getDisplayName() + "/" + eType3.getDisplayName() + "/" + TataPowerUtils.TYPE_GRN + "/" 
												+ TataPowerUtils.TYPE_OTHERS + "'.");
				} catch (DatabaseException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}				
			}
			else if (catTypeName.equals(TataPowerUtils.MATERIAL_MANAGEMENT) 
					&& (!reqTypeName.startsWith("GRN"))
					&& (!reqTypeName.equals(TataPowerUtils.TYPE_DESP))){
						ruleResult.setCanContinue(false);
						ruleResult.setMessage("If 'Department' is 'Material Management' then 'Type' has to be either 'GRN' or 'Desp'.");				
			}
			else if (catTypeName.equals(TataPowerUtils.HR) 
					&& ((!reqTypeName.equals(TataPowerUtils.TYPE_ADMIN_WITH_SAP_PO)) 
					&& (!reqTypeName.equals(TataPowerUtils.TYPE_ADMIN_WITHOUT_SAP_PO)))
					&& (!reqTypeName.equals(TataPowerUtils.TYPE_GRN))){
				ruleResult.setCanContinue(false);
				ruleResult.setMessage("If 'Department' is 'HR' then 'Type' has to be either 'Admin With SAP PO' or 'Admin WithoutSAP PO' or "
						+ TataPowerUtils.TYPE_GRN + " or 'Others'.");				
			}
			else if (catTypeName.equals(TataPowerUtils.PROCUREMENT) 
					&& ((!reqTypeName.equals(TataPowerUtils.TYPE_ADMIN_WITH_SAP_PO)) 
							&& (!reqTypeName.equals(TataPowerUtils.TYPE_ADMIN_WITHOUT_SAP_PO))
							&& (!reqTypeName.equals(TataPowerUtils.TYPE_GRN)))){
				ruleResult.setCanContinue(false);
				ruleResult.setMessage("If 'Department' is 'PROCUREMENT' then 'Type' has to be either 'Admin With SAP PO' or 'Admin WithoutSAP PO'." +
						" or " + TataPowerUtils.TYPE_GRN + ".");				
			}
			else if (catTypeName.equals(TataPowerUtils.SAFETY) 
					&& ((!reqTypeName.equals(TataPowerUtils.TYPE_ADMIN_WITH_SAP_PO)) 
							&& (!reqTypeName.equals(TataPowerUtils.TYPE_ADMIN_WITHOUT_SAP_PO))
							&& (!reqTypeName.equals(TataPowerUtils.TYPE_GRN)))){
				ruleResult.setCanContinue(false);
				ruleResult.setMessage("If 'Department' is 'SAFETY' then 'Type' has to be either 'Admin With SAP PO' or 'Admin WithoutSAP PO'" +
						" or " + TataPowerUtils.TYPE_GRN + ".");				
			}
			else if (catTypeName.equals(TataPowerUtils.SECURITY) 
					&& ((!reqTypeName.equals(TataPowerUtils.TYPE_ADMIN_WITH_SAP_PO)) 
							&& (!reqTypeName.equals(TataPowerUtils.TYPE_ADMIN_WITHOUT_SAP_PO))
							&& (!reqTypeName.equals(TataPowerUtils.TYPE_GRN)))){
				ruleResult.setCanContinue(false);
				ruleResult.setMessage("If 'Department' is 'SECURITY' then 'Type' has to be either 'Admin With SAP PO' or 'Admin WithoutSAP PO'" +
						" or " + TataPowerUtils.TYPE_GRN + ".");				
			}
			/*else if ((catTypeName.equals(TataPowerUtils.PROCUREMENT) || catTypeName.equals(TataPowerUtils.SAFETY)
					|| catTypeName.equals(TataPowerUtils.HR) || catTypeName.equals(TataPowerUtils.SECURITY))
					&& (!reqTypeName.equals(TataPowerUtils.TYPE_OTHERS))){
				ruleResult.setCanContinue(false);
				ruleResult.setMessage("If 'Department' is '" + catTypeName + "', then 'Type' can only be 'Others'.");
			}*/
			else
				ruleResult.setCanContinue(true);				
		}		
		return ruleResult;
	}

	/* (non-Javadoc)
	 * @see transbit.tbits.api.IRule#getName()
	 */
	public String getName() {
		return this.getClass().getSimpleName() + " allowed 'Types' for Bill tracking BA.";
	}

	/* (non-Javadoc)
	 * @see transbit.tbits.api.IRule#getSequence()
	 */
	public double getSequence() {
		// TODO Auto-generated method stub
		return 0;
	}

}
