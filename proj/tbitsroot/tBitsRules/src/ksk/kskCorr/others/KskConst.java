package kskCorr.others;

import java.util.Collection;

import transbit.tbits.api.APIUtil;
import transbit.tbits.domain.Field;
import transbit.tbits.domain.RequestDataType;
import corrGeneric.com.tbitsGlobal.shared.objects.GenericParams;

public class KskConst 
{
	public static final String WPCL_FULL_FIRM_NAME = "KSK Mahanadi Power Company Limited" ;
	public static final String WPCL_FIRM_NAME = "WPCL" ;
	public static final String SEPCO_FIRM_NAME = "SEPCO" ;
	public static final String ROOT_USER = "root";
	
	public static final String DI_UPDATE_DESCRIPTION = "This request was automatically updated and marked to " + GenericParams.StatusClosed + " by the system";
	
	public static final String SubscriberFieldName = Field.SUBSCRIBER;
	
	public static final String TargetDateFieldName = "target_date_field_name";
	public static final String TargetDateSpan = "span";
	
	public static final String HolidayCalendarName = "KSKSTANDARDCALENDAR";
	
	public static final String CorrBaList = "comma_separated_list_of_correspondence_BAs";
	public static final String DIBaList = "comma_separated_list_of_di_BAs";
	
	public static final String CorrTypeFieldName = Field.SEVERITY ;
	public static final String CorrProtocolFieldName = Field.CATEGORY;
	
	public static final String CORR_TYPE_FIELD_NAME  = "severity_id" ;
	
	public static RequestDataType getDiffRelReq(String currRelReqs, String prevRelReqs) 
	{
		if( null == currRelReqs || currRelReqs.trim().equals(""))
			return null;
		Collection<RequestDataType> crr = APIUtil.getRequestCollection(currRelReqs);
		
		if( null == crr )
			return null;
		
		Collection<RequestDataType> prr = null ;
		if( null != prevRelReqs && ! prevRelReqs.trim().equals(""))
			prr = APIUtil.getRequestCollection(prevRelReqs);
		
		
		for( RequestDataType rdt : crr )
		{
			boolean inPrevious = false;
//			if( rdt.getSysId() != sysId )
//				continue;
			
			if( null != prr )
			{
				for( RequestDataType prdt : prr )
				{
					if( rdt.getSysId() == prdt.getSysId() && rdt.getRequestId() == prdt.getRequestId() )
					{
						inPrevious = true;
						break;
					}
				}
			}
			
			if( ! inPrevious )
			{
				return rdt;
			}
		}
		
		return null;
	}
}
