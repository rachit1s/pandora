package essardcn;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Hashtable;
import java.util.List;

import transbit.tbits.Helper.TBitsConstants;
import transbit.tbits.api.APIUtil;
import transbit.tbits.api.IRule;
import transbit.tbits.api.RuleResult;
import transbit.tbits.api.UpdateRequest;
import transbit.tbits.common.DatabaseException;
import transbit.tbits.common.PropertiesHandler;
import transbit.tbits.common.TBitsLogger;
import transbit.tbits.common.TBitsResourceManager;
import transbit.tbits.domain.BusinessArea;
import transbit.tbits.domain.Field;
import transbit.tbits.domain.Request;
import transbit.tbits.domain.RequestDataType;
import transbit.tbits.domain.User;
import transbit.tbits.exception.APIException;
import transbit.tbits.exception.TBitsException;

public class RelatedRequestUpdatingRule  implements IRule{

	private static final String PLUGIN_LINKED_REQUEST_UPDATE_RULES_BALIST = "plugin.linkedrequestupdating.baList";

	public static TBitsLogger LOG = TBitsLogger.getLogger("essardcn");
	public RuleResult execute(Connection connection, BusinessArea ba,
			Request oldRequest, Request currentRequest, int Source, User user,
			boolean isAddRequest) {
		

		if( Source == TBitsConstants.SOURCE_CMDLINE )
	{
		return new RuleResult(true,"Rule not applicable for cmd-request.", true);
	}

		String baListStr = PropertiesHandler
				.getProperty(PLUGIN_LINKED_REQUEST_UPDATE_RULES_BALIST);
		boolean isApplicableBA = false;
		if (baListStr != null) {
			List<String> baList = Arrays.asList(baListStr.trim().split(","));
			if (baList.contains(ba.getSystemPrefix()))
				isApplicableBA = true;
		}
		if (isApplicableBA) {
		try {
		if (isAddRequest)	
		{
			return new RuleResult(true,"Rule not applicable for add request.",true);
		}
		String  is_Dcn = currentRequest.get("isdsn");
		if(is_Dcn.equalsIgnoreCase("true"))
		{
			int reqId = oldRequest.getRequestId();
			String currRelReqs = currentRequest.getRelatedRequests();
			//String prevRelReqs = oldRequest.getRelatedRequests();
			
			ArrayList<Request> reqToUpdate = new ArrayList<Request>();
			Collection<RequestDataType> crr = APIUtil.getRequestCollection(currRelReqs);
			for(RequestDataType rdt : crr )
			{
				
					Request upRequest = Request.lookupBySystemIdAndRequestId(connection, rdt.getSysId(), rdt.getRequestId());
				    if (upRequest != null)
				    {
				    	reqToUpdate.add(upRequest);
				    }
				
				
			}
		
			for(Request req : reqToUpdate)
			{
				Hashtable<String,String> params = new Hashtable<String,String>();
				//int uId = req.getUserId();
				User su = null;
				try {
					su = User.lookupByUserLogin("root");
				} catch (DatabaseException e) {
				
					e.printStackTrace();
				}
			
				params.put(Field.USER, su.getUserLogin());
				params.put(Field.DESCRIPTION, "Update this request " + (isAddRequest == true ? "" : "in response to action on request :" + ba.getSystemPrefix()+"#"+currentRequest.getRequestId() +"#"+currentRequest.getMaxActionId()) );
			
				
				params.put(Field.BUSINESS_AREA, req.getSystemId()+"");
				params.put(Field.REQUEST, req.getRequestId()+"");
				
				
				UpdateRequest up = new UpdateRequest();
				up.setSource(TBitsConstants.SOURCE_CMDLINE);
				TBitsResourceManager trm = new TBitsResourceManager();
				try {
					Request requ = up.updateRequest(connection,trm, params);
				} catch (TBitsException e) {
					LOG.info("Exception while updating............");
					e.printStackTrace();
				} catch (APIException e) {
					LOG.info("Exception while updating............");
					e.printStackTrace();
				}

			

				
			}
			
			
		}	
			
		} catch (DatabaseException e) {
			
			e.printStackTrace();
		}
		
		}
		return new RuleResult(true ,"Rule executed successfully.............." ,true);
	}


	

	public double getSequence() {
	
		return 0.1;
	}

	
	public String getName() {
		
		return "RelatedRequestUpdatingRule";
	}

}
