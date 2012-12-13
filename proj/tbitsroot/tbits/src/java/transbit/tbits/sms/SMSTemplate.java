package transbit.tbits.sms;

import java.util.ArrayList;

import transbit.tbits.domain.BusinessArea;
import transbit.tbits.domain.Request;
import transbit.tbits.domain.RequestUser;
import transbit.tbits.domain.User;
import transbit.tbits.common.TBitsLogger;
import static transbit.tbits.Helper.TBitsConstants.PKG_CONFIG;

@Deprecated // Nitiraj because I cannot give a smsId from request. Its not fixed field. 
// we first define its purpose then use it. Just created a constant for the field name to remove the compilation error.
public class SMSTemplate 
{
	public static String SMS_ID_FIELD_NAME = "sms_id" ;
	String template = "";
	public static final TBitsLogger LOG = TBitsLogger.getLogger(PKG_CONFIG);
    public SMSTemplate(String template)
	{
		this.template = template;
	}
	public String getText(Request request)
	{
		try
		{
			
			int smsId=(Integer)request.getObject(SMS_ID_FIELD_NAME);
			String businessArea = BusinessArea.lookupBySystemId(request.getSystemId()).getDisplayName();
			int requestId = request.getRequestId();
			int actionId = request.getMaxActionId();
			String messages = "Request Added";
			String logger= "";
			if(request.getLoggers().size() > 0)
			{
				ArrayList<RequestUser> myLoggers = new ArrayList<RequestUser>() ;
				myLoggers.addAll(request.getLoggers());
				logger = User.lookupByUserId(myLoggers.get(0).getUserId()).getUserLogin(); 
			}
			String smsString = businessArea+"#"+requestId+"#"+actionId+" "+ template +" -"+logger;
			return smsString;
		}
		catch(Exception e){
			LOG.error("e = " + e);
		}
		return "";
	}
}
