package icms.icmscommon;

import java.sql.Connection;

import transbit.tbits.api.IPostRule;
import transbit.tbits.api.IRule;
import transbit.tbits.api.RuleResult;
import transbit.tbits.domain.BusinessArea;
import transbit.tbits.domain.Request;
import transbit.tbits.domain.User;

public class InspectionCallGeneration implements IRule {

	@Override
	public RuleResult execute(Connection connection, BusinessArea ba,
			Request oldRequest, Request currentRequest, int Source, User user,
			boolean isAddRequest) {
		String sysPrefix=ba.getSystemPrefix();
		if(sysPrefix.equalsIgnoreCase("INSPECTION") && isAddRequest)
		{
			Boolean obj =(Boolean) currentRequest.getObject("AutoGenerateInspectionNumber");
			currentRequest.setObject("AutoGenerateInspectionNumber", true);
			String str = currentRequest.get("AutoGenerateInspectionNumber");
			System.out.println(str);
		}
		return new RuleResult(true);
	}

	@Override
	public double getSequence() {
		// TODO Auto-generated method stub
		return 1.0;
	}

	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return "Inspection call no. generation rule";
	}

}
