package gmrCorr.rules;

import gmrCorr.others.GMRCorrUtils;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.Collection;



import transbit.tbits.api.IRule;
import transbit.tbits.api.RuleResult;
import transbit.tbits.common.TBitsLogger;
import transbit.tbits.domain.BusinessArea;
import transbit.tbits.domain.Field;
import transbit.tbits.domain.Request;
import transbit.tbits.domain.RequestDataType;
import transbit.tbits.domain.RequestUser;
import transbit.tbits.domain.User;
import corrGeneric.com.tbitsGlobal.server.CorrDBServiceImpl;
import java.lang.String;

/**
 * 
 * @author sharan
 * 
 * Rule: This rule is applicable to GMR_Corr BA. It says that the assignee of a request in GMR_Corr BA can only transfer
 * the request from GMR_Corr to GMR_DI BA. 
 *
 */
public class CorrToDiWithAppropriateAssignee implements IRule {
	

	
	public static TBitsLogger LOG = TBitsLogger.getLogger("gmrRules.corr");
	//private String sysPrefix="";
	//String corrlogger = "CorrLogger";
	//String linkedCorr= "related_requests";
	@Override
	public RuleResult execute(Connection connection, BusinessArea ba,
			Request oldRequest, Request currentRequest, int Source, User user,
			boolean isAddRequest) {
		try
		{
			if( !isAddRequest )
				return new RuleResult(true, "rule not applicable for add request in GMR_CORR BA.", true);
//			if(!isAddRequest)
//			{

			if(ba.getSystemPrefix().equalsIgnoreCase("GMR_DI"))
			{
			String currRelatedRequest = currentRequest.get(Field.RELATED_REQUESTS);
			int i = currRelatedRequest.indexOf("#");
			int j = currRelatedRequest.lastIndexOf("#");
			String baString = currRelatedRequest.substring(0,i);
			int requestid = Integer.parseInt(currRelatedRequest.substring(i+1, j));
			int actionId=Integer.parseInt(currRelatedRequest.substring(j+1));
			BusinessArea relatedBa=BusinessArea.lookupBySystemPrefix(baString);
			Request relatedRequest=Request.lookupBySystemIdAndRequestId(connection,relatedBa.getSystemId(),requestid);
			
			String corrLogger = relatedRequest.get(Field.ASSIGNEE);
			User assigneeUser = User.lookupAllByUserLogin(corrLogger);
			if(assigneeUser.getUserId()== user.getUserId())
			{
				return new RuleResult(true,"This user can update the request as the user is in the assignee list.",true);
			}
			else
			{
				return new RuleResult(false,"You are not the assignee of this request. So you can't trasfer this request to GMR_DI business area.",true);
				
			}
			}
		//	}
			//return new RuleResult(false,"You(" + user.getUserLogin() + ") are not allowed to transfer this Request.");
		}
		catch(Exception e)
		{
			LOG.error(TBitsLogger.getStackTrace(e));
			return new RuleResult(true,"Error occured in the Rule. So ignoring the check.",false);
		}
		return null;
	}

	
	@Override
	public double getSequence() {
		// TODO Auto-generated method stub
		return 1.1;
	}

	@Override
	public String getName() {
		// TODO Auto-generated method stub 
		return "Corr to DI with Appropriate assignee";
	}

}
