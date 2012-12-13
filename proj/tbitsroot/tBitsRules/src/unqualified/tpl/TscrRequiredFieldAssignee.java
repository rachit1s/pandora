package tpl;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Hashtable;

import transbit.tbits.api.AttachmentInfo;
import transbit.tbits.api.IRule;
import transbit.tbits.api.RuleResult;
import transbit.tbits.domain.BusinessArea;
import transbit.tbits.domain.Field;
import transbit.tbits.domain.Request;
import transbit.tbits.domain.RequestEx;
import transbit.tbits.domain.RequestUser;
import transbit.tbits.domain.User;

public class TscrRequiredFieldAssignee implements IRule{
	
	
	private static final String TSCR = "tscr";
	
	
	public RuleResult execute(Connection connection, BusinessArea ba,
			Request oldRequest, Request currentRequest, int Source, User user,
			Hashtable<Field, RequestEx> extendedFields, boolean isAddRequest,
			Collection<AttachmentInfo> attachments) {
		
		
	  RuleResult ruleresult = new RuleResult();
	  String Sysprefix = ba.getSystemPrefix();
	  ArrayList<RequestUser> assigneeList = currentRequest.getAssignees();
	  
	   if(Sysprefix.equalsIgnoreCase(TSCR)&&(isAddRequest))
	      {
		     if(assigneeList.isEmpty())
		       {
			     ruleresult.setCanContinue(false);
			     ruleresult.setMessage("Assignee is compulsory please mention");
		        }
		     else 
			    ruleresult.setCanContinue(true);
	       }
		
		
		// TODO Auto-generated method stub
		return ruleresult;
	}
	public String getName() {
		return "TSCRRequiredFieldAsignee: assignee is compulsory in TSCR business areas'";
	}

	/* (non-Javadoc)
	 * @see transbit.tbits.api.IRule#getSequence()
	 */
	public double getSequence() {
		// TODO Auto-generated method stub
		return 0;
	}


}
