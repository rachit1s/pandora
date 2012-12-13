package nccucommon;

import java.sql.Connection;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import transbit.tbits.api.IRule;
import transbit.tbits.api.RuleResult;
import transbit.tbits.domain.BusinessArea;
import transbit.tbits.domain.Request;
import transbit.tbits.domain.User;

public class TargetDateValidation implements IRule {

	@Override
	public RuleResult execute(Connection connection, BusinessArea ba,
			Request oldRequest, Request currentRequest, int Source, User user,
			boolean isAddRequest) {
		
		String sysPrefix=ba.getSystemPrefix();
		if(sysPrefix.equalsIgnoreCase("INTL"))
		{
				Calendar targetCal=Calendar.getInstance();
				Calendar todayCal=Calendar.getInstance();
				
				Date targetDate=(Date) currentRequest.getObject("due_datetime");
				Date todayDate=new Date();
				targetCal.setTime(targetDate);
				todayCal.setTime(todayDate);
				
				targetCal.set(Calendar.MINUTE, 0);
				targetCal.set(Calendar.SECOND, 0);
				targetCal.set(Calendar.HOUR,12);
				targetCal.set(Calendar.MILLISECOND,0);
				
				todayCal.set(Calendar.MINUTE, 0);
				todayCal.set(Calendar.SECOND, 0);
				todayCal.set(Calendar.HOUR, 0);
				todayCal.set(Calendar.MILLISECOND,0);
				
				targetDate=targetCal.getTime();
				todayDate=todayCal.getTime();
				
				int results = targetDate.compareTo(todayDate);

			    if(results > 0)
			    	return new RuleResult(true,"TargetDateValidation rule executed successfully",true);
			    else if (results < 0)
			    	return new RuleResult(false,"Please enter the future date in target date field",true);
			    else
			    	return new RuleResult(true,"TargetDateValidation rule executed successfully",true);
			
		}
		
		return new RuleResult();
	}

	@Override
	public String getName() {
		
		return "TargetDateValidation";
	}

	@Override
	public double getSequence() {
		
		return 0;
	}

}
