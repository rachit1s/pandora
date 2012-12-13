package kskorg;

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
import transbit.tbits.domain.Type;
import transbit.tbits.domain.User;

public class OrgAssigneeRule implements IRule{
	
	
	
	private static final String ORG = "org";
	private static final String JOINED = "Joined";
	private static final String VACANT = "Vacant";

	public RuleResult execute(Connection connection, BusinessArea ba,
			Request oldRequest, Request currentRequest, int Source, User user, boolean isAddRequest) {
		
		
		  RuleResult ruleresult = new RuleResult();
		  String Sysprefix = ba.getSystemPrefix();
		  Collection<RequestUser> assigneeList = (Collection<RequestUser>)currentRequest.getObject(Field.ASSIGNEE);
		  
		  
		  Type statusType = currentRequest.getSeverityId();
		   String CurrStatus = statusType.getName();
		  
		  try
		  {
		           if(Sysprefix.equalsIgnoreCase(ORG))
		             { 
		        	     // check only one assignee should be there
		        	      int Size = assigneeList.size();
			              if(Size > 1)
			                 {
			        	        ruleresult.setCanContinue(false);
			        	         ruleresult.setMessage("please mention only one assignee");
			                  }
			              else if((CurrStatus.equalsIgnoreCase(JOINED))&&(assigneeList.isEmpty()))
			        	   // 
			                    {
			        	      		ruleresult.setCanContinue(false);
			        	      		ruleresult.setMessage("Please mention assignee");
			        	   
			                     }
			              else if(CurrStatus.equalsIgnoreCase(VACANT))
			                    {
			        	           if(assigneeList.isEmpty())
			        	            {
			        	        	  ruleresult.setCanContinue(true);
			        	            }
			        	            else
			        	             {
			        	            	ruleresult.setCanContinue(false);
			        	            	ruleresult.setMessage("in vacant status you can't insert Assignee");
			        	             }
			        	         }
			                else 
			                    {
			            	      ruleresult.setCanContinue(true);
			                     }
			              
			           
		                 }
		           else
		           {
		        	   ruleresult.setCanContinue(true);
		           }
		  }
		  catch (Exception e) 
		    {
			
		    }
			  
		  
		  
		
		return ruleresult;
	}

	public String getName() {
		// TODO Auto-generated method stub
		return "org assignee rule executed";
	}

	public double getSequence() {
		// TODO Auto-generated method stub
		return 0;
	}
	
	

}
