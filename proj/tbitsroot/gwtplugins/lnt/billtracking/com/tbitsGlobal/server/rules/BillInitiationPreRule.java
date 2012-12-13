package billtracking.com.tbitsGlobal.server.rules;

import static billtracking.com.tbitsGlobal.server.BillProperties.billProperties;

import java.sql.Connection;

import transbit.tbits.api.IRule;
import transbit.tbits.api.RuleResult;
import transbit.tbits.domain.BusinessArea;
import transbit.tbits.domain.Request;
import transbit.tbits.domain.User;
import billtracking.com.tbitsGlobal.shared.IBillProperties;

public class BillInitiationPreRule implements IRule,IBillProperties {

	@Override
	public RuleResult execute(Connection connection, BusinessArea ba,
			Request oldRequest, Request currentRequest, int Source, User user,
			boolean isAddRequest) {

		if( null == ba || null == ba.getSystemPrefix())
			return new RuleResult(false,"null BA",false);
		String billPrefix=billProperties.get(PROPERTY_SUBMISSION_BA_PREFIX);
		if(ba.getSystemPrefix().equalsIgnoreCase(billPrefix))
		{
			currentRequest.setIsPrivate(true);
			return new RuleResult(true,"BillInitiationPreRule Executed SuccessFully",true);
		}
		else 
			return new RuleResult(true,"ignoring the rule as ba is not :"+billPrefix,true);
		// TODO Auto-generated method stub

	}
	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return "BillInitiationPreRule";
	}

	@Override
	public double getSequence() {
		// TODO Auto-generated method stub
		return 0;
	}

}
