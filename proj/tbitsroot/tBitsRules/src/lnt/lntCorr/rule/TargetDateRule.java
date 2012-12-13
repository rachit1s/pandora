package lntCorr.rule;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import lntCorr.others.LnTConst;
//import lntCorr.others.LnTManager;

import corrGeneric.com.tbitsGlobal.server.managers.ProtocolOptionsManager;
import corrGeneric.com.tbitsGlobal.shared.domain.ProtocolOptionEntry;

import transbit.tbits.api.IRule;
import transbit.tbits.api.RuleResult;
import transbit.tbits.common.TBitsLogger;
import transbit.tbits.domain.BusinessArea;
import transbit.tbits.domain.HolidaysList;
import transbit.tbits.domain.Request;
import transbit.tbits.domain.User;
//import transbit.tbits.exception.TBitsException;

import static lntCorr.others.LnTConst.*;

public class TargetDateRule implements IRule {

	public static TBitsLogger LOG = TBitsLogger.getLogger("lntCorr");
	public RuleResult execute(Connection connection, BusinessArea ba,
			Request oldRequest, Request currentRequest, int Source, User user,
			boolean isAddRequest) 
	{
		try
		{
//			ArrayList<ProtocolOptionEntry> tdoptions = null;
//			try {
//				tdoptions = LnTManager.getInstance().lookupTargetDateOptions();
//			} catch (TBitsException e) {
//				LOG.error(TBitsLogger.getStackTrace(e));
//			}
//			
//			if( null == tdoptions || tdoptions.size() == 0)
//				return new RuleResult(true,"Rule not applicable.", true);

			ProtocolOptionEntry myTDoption = ProtocolOptionsManager.lookupProtocolEntry(ba.getSystemPrefix(), LnTConst.TargetDateFieldName);
//			for( ProtocolOptionEntry poe : tdoptions )
//			{
//				if( poe.getSysPrefix().equals(ba.getSystemPrefix() ))
//				{
//					myTDoption = poe ;
//					break;
//				}
//			}
			
			if( null == myTDoption )
				return new RuleResult(true,"Rule not applicable for this ba.", true);
			
			Date date = (Date) currentRequest.getObject(myTDoption.getValue());
			
			if( null != date )
				return new RuleResult(true,"Rule not applicable as the "+myTDoption.getValue()+" was already set by the user.", true );
			
			
			String statusValue = currentRequest.get(StatusFieldName);
			if( null == statusValue)
				return new RuleResult(true,"The Status Field was null. So aborting the rule.", false);
			
			if( statusValue.equals(StatusInformation) || statusValue.equals(LnTConst.StatusConcluded) )
				return new RuleResult(true,"The Status Field was " + StatusInformation + " or " + LnTConst.StatusConcluded + ". So aborting the rule.", true);
			
			String prot = currentRequest.get(ProtocolFieldName);
			if( null == prot )
				return new RuleResult(true,ProtocolFieldName + " Field not set in the request.", false);
			
			String optionName = prot + "_" + TargetDateSpan ;
			ProtocolOptionEntry spanOption = ProtocolOptionsManager.lookupProtocolEntry(ba.getSystemPrefix(), optionName);
			
			if( null == spanOption )
				return new RuleResult(true,"No mapping of type : " + optionName + " found for ba : " + ba.getSystemPrefix() , true);
	
			Integer span = null ;
			try
			{
				span = Integer.parseInt(spanOption.getValue().trim());
			}
			catch(NumberFormatException nfe)
			{
				LOG.error("The value expected in poe : " + spanOption + " is an integer representing the number of days to be slide for setting " + myTDoption.getValue());
				return new RuleResult(false,"The value expected in poe : " + spanOption + " is an integer representing the number of days to be slide for setting " + myTDoption.getValue(), false);
			}
	
			Date nextDate = getEscalationDate(new Date(), span, HolidayCalendarName);
			currentRequest.setObject(myTDoption.getValue(), nextDate);
			
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
