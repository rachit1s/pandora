package lntCCC;

import java.sql.Connection;
import java.util.Collection;

import transbit.tbits.api.IRule;
import transbit.tbits.api.RuleResult;
import transbit.tbits.common.PropertiesHandler;
import transbit.tbits.domain.BusinessArea;
import transbit.tbits.domain.Request;
import transbit.tbits.domain.RequestUser;
import transbit.tbits.domain.Type;
import transbit.tbits.domain.User;

public class OnlyReviewedCanBeApproved implements IRule {

	private static final String DECISIONFROM_SCM_FIELD = "DecisionfromSCM";

	@Override
	public RuleResult execute(Connection connection, BusinessArea ba,
			Request oldRequest, Request currentRequest, int Source, User user,
			boolean isAddRequest) {
		
		RuleResult rr = new RuleResult();
		
		String baListStr = PropertiesHandler.getProperty(LnTCCCUtils.PLUGIN_LNTCCC_RULES_BALIST);
		boolean isApplicableBA = LnTCCCUtils.isApplicableBA(baListStr, ba);
		
		//String[] applicableBAs = new String[]{"CCC_HO", "MAL_CCC_SO", "APL_CCC_SO", "CCC_SO"};
		if(isApplicableBA)
		{
			
			Type currentDecisionfromSCM = null;
			
			Object o = null;
			try
			{
				o = currentRequest.getObject(DECISIONFROM_SCM_FIELD);
			}
			catch(Exception e)
			{
				e.printStackTrace();
				System.out.println(" The field " + DECISIONFROM_SCM_FIELD + " is either not type or does not exist in ba [" + ba.getSystemPrefix() + "]");
				return rr;
			}

			if ((o != null) && (o instanceof Type)) {
				currentDecisionfromSCM = (Type) o;
			} else {
				System.out.println(" The field " + DECISIONFROM_SCM_FIELD
						+ " is either not type or does not exist in ba [" + ba.getSystemPrefix() + "]");
				return rr;
			}
			
			o = null;
			try
			{
				o = oldRequest.getObject(DECISIONFROM_SCM_FIELD);
			}
			catch(Exception e)
			{
				e.printStackTrace();
				System.out.println(" The field " + DECISIONFROM_SCM_FIELD + " is either not type or does not exist in ba [" + ba.getSystemPrefix() + "]");
				return rr;
			}
			Type oldDecisionfromSCM =  null;
			if ((o != null) && (o instanceof Type)) {
				oldDecisionfromSCM = (Type) o;
			} 
			else if((o != null) && !(o instanceof Type)){
				System.out.println(" The field " + DECISIONFROM_SCM_FIELD
						+ " is either not type or does not exist");
				return rr;
			}

			//if there is a change in DecisionfromSCM
			boolean hasDecisionFromSCMChange = isAddRequest || (oldDecisionfromSCM == null) ||
				(currentDecisionfromSCM.getTypeId() != oldDecisionfromSCM.getTypeId());
			
			if(hasDecisionFromSCMChange && (currentDecisionfromSCM.getName().equals("Approved") ||currentDecisionfromSCM.getName().equals("Rejected")))
			{
				Type status = currentRequest.getStatusId();
				if(status.getName().equals("Reviewed"))
				{
					return rr;
				}
				else if(status.getName().equals("Prepared") && areAllUserFieldsEmpty(ba, currentRequest, new String[]{"LECnI", "LEElect", "LEMech"}))
				{
					return rr;
				}
				else
				{
					rr.setCanContinue(false);
					rr.setMessage("you can mark decision from SCM as Reject or approved only if status_id == Reviewed or (Document-flow-status == Prepared and all three LECNI fields are empty) "); 
				}
			}
		}
		return rr;
	}

	private boolean areAllUserFieldsEmpty(BusinessArea ba, Request currentRequest,
			String[] userFieldNames) {
		for(String userFieldName:userFieldNames)
		{
			if(!isUserFieldEmpty(ba, currentRequest, userFieldName))
				return false;
		}
		return true;
	}

	private boolean isUserFieldEmpty(BusinessArea ba, Request currentRequest,
			String userFieldName) {
		Object reqUserObj = null;
		try
		{
			reqUserObj = currentRequest.getObject(userFieldName);
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		if(reqUserObj != null)
		{
			Collection<RequestUser> reqUsers = (Collection<RequestUser>) reqUserObj;
			return (reqUsers.size() == 0);
		}
		else
		{
			System.out.println("The ba [" + ba.getSystemPrefix() + "] does not have the user type field '" + userFieldName + "'");
			return true;
		}
	}

	@Override
	public String getName() {
		return "OnlyReviewedCanBeApproved";
	}

	@Override
	public double getSequence() {
		return 100.5;
	}

}
