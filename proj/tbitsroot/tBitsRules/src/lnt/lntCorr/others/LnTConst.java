package lntCorr.others;

import java.util.Collection;

import transbit.tbits.api.APIUtil;
import transbit.tbits.domain.RequestDataType;
import corrGeneric.com.tbitsGlobal.shared.objects.GenericParams;

public class LnTConst 
{
	public static final String DI_UPDATE_DESCRIPTION = "This request was automatically updated and marked to " + GenericParams.StatusClosed + " by the system";
	
	public static final String TargetDateFieldName = "target_date_field_name";
	
	public static final String TargetDateSpan = "span";
	
	public static final String HolidayCalendarName = "LNTSTANDARDCALENDAR";
	
	public static final String ProtocolFieldName = "category_id";
	
	public static final String ProtocolFieldNameOthers = "other" ;
	
	public static final String LnTNumberFieldName = "LNT_new_corr_no";
	
	public static final String LnTNumberPrefix = "LNT_new_corr_no_prefix";

	public static final String PreviousCorrNo = "previous_corr_no";
	
	public static final String CCLetterFieldName = "CC_on_letter";
	
	public static final String IOM_SYSPREFIX = "IOM";
	
	public static final String CorrespondenceCategoryFieldName = "CorrespondenceCategory";
	public static final String CorrCat_Letter = "Letter";
	public static final String CorrCat_Email = "Email";
	
	public static final String StatusFieldName = "request_type_id";
	public static final String StatusInformation = "Information";
	public static final String StatusConcluded = "Concluded";
	
	public static final String ReadAccessMailList = "read_access_mailing_list";
	
	public static final String CorrBaList = "comma_separated_list_of_correspondence_BAs";
	
	public static final String DIBaList = "comma_separated_list_of_di_BAs";
	
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
