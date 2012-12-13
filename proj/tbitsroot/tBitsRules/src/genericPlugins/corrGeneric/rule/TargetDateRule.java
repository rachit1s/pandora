package corrGeneric.rule;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import corrGeneric.com.tbitsGlobal.server.managers.ProtocolOptionsManager;
import corrGeneric.com.tbitsGlobal.shared.domain.ProtocolOptionEntry;
import corrGeneric.others.CorrConst;

import transbit.tbits.api.IRule;
import transbit.tbits.api.RuleResult;
import transbit.tbits.common.TBitsLogger;
import transbit.tbits.domain.BusinessArea;
import transbit.tbits.domain.Field;
import transbit.tbits.domain.HolidaysList;
import transbit.tbits.domain.Request;
import transbit.tbits.domain.User;
import transbit.tbits.exception.TBitsException;

public class TargetDateRule implements IRule {

	public static TBitsLogger LOG = TBitsLogger.getLogger("kskCorr");
	public RuleResult execute(Connection connection, BusinessArea ba,
			Request oldRequest, Request currentRequest, int Source, User user,
			boolean isAddRequest) 
	{
		try
		{
			ProtocolOptionEntry targetDateFieldName = ProtocolOptionsManager.getInstance().lookupProtocolEntry(ba.getSystemPrefix(),CorrConst.TargetDateFieldName);
			ProtocolOptionEntry targetDateDependentType = ProtocolOptionsManager.getInstance().lookupProtocolEntry(ba.getSystemPrefix(),CorrConst.TargetDateDependentType); 
			ProtocolOptionEntry diffAddUpdate = ProtocolOptionsManager.getInstance().lookupProtocolEntry(ba.getSystemPrefix(),CorrConst.IsSpanDiffForAddAndUpdate);
			ProtocolOptionEntry holidayCalendarEntry = ProtocolOptionsManager.lookupProtocolEntry(ba.getSystemPrefix(), CorrConst.HolidayCalendarName);
			
			if( null == targetDateFieldName )
				return new RuleResult(true,"Skipping the rule as no mapping for " + CorrConst.TargetDateFieldName + " found for ba " + ba.getSystemPrefix());
			
			if( null == holidayCalendarEntry || holidayCalendarEntry.getValue() == null )
				return new RuleResult(true,CorrConst.HolidayCalendarName +" not defined for ba ", false);
			
			String spanString = CorrConst.TargetDateSpan ;
			if( null != targetDateDependentType && targetDateDependentType.getValue() != null )
			{
				Field f = Field.lookupBySystemIdAndFieldName(ba.getSystemId(), targetDateDependentType.getValue());
				if( null != f && null != currentRequest.getObject(f.getName()) )
					spanString = currentRequest.get(f.getName()) + "_" + spanString ;
			}
			
			if( diffAddUpdate != null && diffAddUpdate.getValue() != null && diffAddUpdate.getValue().equals(CorrConst.IsSpanDiffForAddAndUpdate_Yes))
			{
				String prefix = "add_" ;
				if( isAddRequest == false )
					prefix = "update_" ;
				
				spanString = prefix + spanString;
						
			}
			
			ProtocolOptionEntry spanEntry = ProtocolOptionsManager.lookupProtocolEntry(ba.getSystemPrefix(), spanString);
			
			if( spanEntry == null || spanEntry.getValue() == null )
			{
				return new RuleResult( true, "Cannot find the property for span with name : " + spanString, false );
			}

			Date date = (Date) currentRequest.getObject(targetDateFieldName.getValue());
			
			if( null != date )
				return new RuleResult(true,"Rule not applicable as the "+targetDateFieldName.getValue()+" was already set by the user.", true );

			Integer span = null ;
			try
			{
				span = Integer.parseInt(spanEntry.getValue().trim());
			}
			catch(NumberFormatException nfe)
			{
				LOG.error("The value expected in poe : " + spanEntry + " is an integer representing the number of days to be slide for setting " + targetDateFieldName.getValue());
				return new RuleResult(false,"The value expected in poe : " + spanEntry + " is an integer representing the number of days to be slide for setting " + targetDateFieldName.getValue(), false);
			}
	
			Date nextDate = getEscalationDate(new Date(), span, holidayCalendarEntry.getValue());
			currentRequest.setObject(targetDateFieldName.getValue(), nextDate);
			
			return new RuleResult(true,"Rule executed successfully.",true);
		}
		catch(Exception e)
		{
			LOG.error(TBitsLogger.getStackTrace(e));
			return new RuleResult(false,e.getMessage(),false);
		}
	}


	private Date getEscalationDate(Date currDate, int span, String calName)
	{
		int left = span ;
		
		// assuming every thing is in default time zone of server.	
		Calendar cal = Calendar.getInstance() ;
		cal.setTime(currDate);
		
		while( left != 0 )
		{
			cal.add(Calendar.DATE, 1);
			if( !HolidaysList.isHoliday(cal.getTime(), calName.toUpperCase()))
			{
				left-- ;
			}
		}
		
		return cal.getTime() ;
	}
	
	public String getName() 
	{
		return "Changes the target date for the ba based on the configured field and span";
	}

	public double getSequence() 
	{
		return 0;
	}

}
