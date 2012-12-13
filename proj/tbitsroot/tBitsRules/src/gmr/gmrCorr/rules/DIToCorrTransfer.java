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
/**
 *  Objective: - The user who brings the CORR record in DI (logger_ids) Business area can only take it back from DI to CORR(user_id).
 *  This rule does the half part of it. i.e. the user_id of CORR should be in the logger_ids of DI from which it was created.
 * @author nitiraj
 *
 */
public class DIToCorrTransfer implements IRule
{
	public static TBitsLogger LOG = TBitsLogger.getLogger("gmrCorr");
	@Override
	public RuleResult execute(Connection connection, BusinessArea ba,
			Request oldRequest, Request currentRequest, int Source, User user,
			boolean isAddRequest) 
	{
		try
		{
			if( ! ba.getSystemPrefix().equals("GMR_CORR") )
				return new RuleResult(true,"rule not applicable for this ba.",true);
			
			if( isAddRequest )
				return new RuleResult(true, "rule not applicable for add request in GMR_CORR BA.", true);
			
			String currRelatedRequest = currentRequest.get(Field.RELATED_REQUESTS);
			String prevRelatedRequest = oldRequest.get(Field.RELATED_REQUESTS);
			
			BusinessArea diBA = BusinessArea.lookupBySystemPrefix("GMR_DI");
			Collection<RequestDataType> diffReqs = GMRCorrUtils.getDiffRelatedRequest(currRelatedRequest,prevRelatedRequest,diBA);
			if( diffReqs.size() != 1 )
			{
				return new RuleResult(true,"Zero or more than one RelatedRequests. So not ignoring.",true);
//				int i = currRelatedRequest.indexOf("#");
//				int j = currRelatedRequest.indexOf("#",	i+1);
//				String baString = currRelatedRequest.substring(0,i);
//				int requestid = Integer.parseInt(currRelatedRequest.substring(i+1, j));
//				//int actionId=Integer.parseInt(currRelatedRequest.substring(j+1));
//				BusinessArea relatedBa=BusinessArea.lookupBySystemPrefix(baString);
//				Request relatedRequest=Request.lookupBySystemIdAndRequestId(connection,relatedBa.getSystemId(),requestid);
//				String corrLogger = relatedRequest.get(Field.LOGGER);
//				User corrLoggerUser = User.lookupAllByUserLogin(corrLogger);
//				if(corrLoggerUser.getUserId()== user.getUserId())
//				{
//					return new RuleResult(true,"This user can update the request as the user is in the corr logger list.",true);
//				}
//				else
//				{
//					return new RuleResult(false,"You are not the logger of this request. So you can't trasfer this request to GMR_Corr business area.",true);
//					
//				}
			}
			
			else
				{		
			RequestDataType rdt = diffReqs.iterator().next();
			
			//* check that the user_id of CORR should be in the logger_ids of the DI
			// get the request.
			Request req = Request.lookupBySystemIdAndRequestId(rdt.getSysId(), rdt.getRequestId());
			
			if( null == req )
				return new RuleResult(true, "Cannot find the related DI request : " + rdt + " in DB");
			ArrayList<RequestUser> loggers =  (ArrayList<RequestUser>) req.getObject(Field.LOGGER);
			
				for( RequestUser ru : loggers )
				{
					if( ru.getUserId() == user.getUserId() )
					{
					return new RuleResult(true, "This user can update the request as the user is in the logger list of related di.",true);
					}
				}
				}
			
			return new RuleResult(false,"You(" + user.getUserLogin() + ") are not allowed to transfer this Request to GMR_Corr as you are not the logger of this request.");
		}
		catch(Exception e)
		{
			LOG.error(TBitsLogger.getStackTrace(e));
			return new RuleResult(true,"Error occured in the Rule. So ignoring the check.",false);
		}
	}

	@Override
	public double getSequence() {
		return 0;
	}

	@Override
	public String getName() {
		return "The user who brings the CORR record in DI Business area can only take it back from DI to CORR."; 
	}
	
}
