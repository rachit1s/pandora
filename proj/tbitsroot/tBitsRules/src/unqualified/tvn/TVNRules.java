package tvn;
import java.util.Hashtable;

import transbit.tbits.TVN.WebdavUtil;
import transbit.tbits.api.IRule;
import transbit.tbits.api.RuleResult;
import transbit.tbits.common.DatabaseException;
import transbit.tbits.domain.BusinessArea;
import transbit.tbits.domain.Field;
import transbit.tbits.domain.Request;
import transbit.tbits.domain.RequestEx;
import transbit.tbits.domain.User;
import transbit.tbits.exception.TBitsException;

/*
 * To ensure that No request with same subject already exists
 * in the system
 * @Abhishek Agarwal 
 */
public class TVNRules implements IRule {

	public RuleResult execute(BusinessArea ba, Request oldRequest,
			Request currentRequest, int Source, User user,
			Hashtable<Field, RequestEx> extendedFields, boolean isAddRequest,
			String attachments) {
		RuleResult ruleResult = new RuleResult();
		ruleResult.setCanContinue(true);
		ruleResult.setSuccessful(true);
        try {
        	if(isAddRequest) {
        		String subj = currentRequest.getSubject(); 
        		if( (subj == null) || (subj.length() == 0))
        		{
        			ruleResult.setMessage("Can not create a request without subject.");
        			ruleResult.setCanContinue(false);
        			ruleResult.setSuccessful(false);
        			return ruleResult;
        		}
        		
        		char[] specialChars = {'/', '\\', ':', '?', '*', '"', '>', '<', '|'}; 
        		for(char ch: specialChars)
        		{
        			if(subj.indexOf(ch) > -1)
        			{
        				ruleResult.setMessage("Can not create a request with subject containing " +
        						"any of the following characters: /, \\, :, ?, *, \", >, <, |");
            			ruleResult.setCanContinue(false);
            			ruleResult.setSuccessful(false);
            			return ruleResult;
        			}
        		}
        		
        		Request oldReq = WebdavUtil.lookupBySystemIdAndRequestSubject
        		(ba.getSystemId(),currentRequest.getSubject());
        		if(null != oldReq
        				&& oldReq.getCategoryId().getName()
        										.equals(currentRequest.getCategoryId().getName())
        				&& oldReq.getRequestTypeId().getName()
        										.equals(currentRequest.getRequestTypeId().getName())
        				&& oldReq.getStatusId().getName().equals(currentRequest.getStatusId().getName())
        										) {
        			ruleResult.setMessage("Request with same subject,category, Request Type, Status" +
        										" Already Exists");
        			ruleResult.setCanContinue(false);
        			ruleResult.setSuccessful(true);
        		}
        	}
        	else {
        		if(!oldRequest.getSubject().equals(currentRequest.getSubject())) {
        			ruleResult.setMessage("Subject of Request Cannot be updated in versionable Business Area");
        			ruleResult.setCanContinue(false);
        			ruleResult.setSuccessful(true);
        		}
        	}
		} catch (DatabaseException e) {
			ruleResult.setMessage("Database Exception occured " + e.toString());
			ruleResult.setCanContinue(false);
			ruleResult.setSuccessful(false);
		} catch (TBitsException e) {
			ruleResult.setMessage("tBits Exception occured " + e.toString());
			ruleResult.setCanContinue(false);
			ruleResult.setSuccessful(false);
		}
		return ruleResult;
	}

	public String getName() {
		return "tBits-Versioining System - No Request with same Subject,Category and Type cannot be added";
	}

	public double getSequence() {
		return 0;
	}

}
