package nccucommon;

import java.sql.Connection;

import transbit.tbits.api.IRule;
import transbit.tbits.api.RuleResult;
import transbit.tbits.domain.BusinessArea;
import transbit.tbits.domain.Request;
import transbit.tbits.domain.User;

public class MadatoryFields implements IRule{

	@Override
	public RuleResult execute(Connection connection, BusinessArea ba,
			Request oldRequest, Request currentRequest, int Source, User user,
			boolean isAddRequest) {
		String sysPrefix=ba.getSystemPrefix();
		if(sysPrefix.equalsIgnoreCase("INTL"))
		{
			
			String Subject=currentRequest.get("subject");
			if(Subject==null || Subject.equalsIgnoreCase(""))
				return new RuleResult(false,"please fill the value in Subject field",true);
			String assigne=currentRequest.get("assignee_ids");
			if(assigne==null || assigne.equalsIgnoreCase(""))
				return new RuleResult(false,"please fill the value in assignee field",true);
			
		}
		return new RuleResult();
	}

	@Override
	public String getName() {
		
		return "MadatoryFields";
	}

	@Override
	public double getSequence() {
		
		return 0;
	}

}
