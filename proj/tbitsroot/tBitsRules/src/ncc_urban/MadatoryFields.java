package ncc_urban;

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
			String BOQItemNo=currentRequest.get("TaskTrackingNo");
			String Subject=currentRequest.get("subject");
			if(BOQItemNo == null || BOQItemNo.equalsIgnoreCase(""))
			return new RuleResult(false,"please fill the value in BOQItemNo field",true);
			if(Subject==null || Subject.equalsIgnoreCase(""))
				return new RuleResult(false,"please fill the value in Subject field",true);
			
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
