package tbitsCob4;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.Hashtable;


import transbit.tbits.Helper.TBitsConstants;
import transbit.tbits.api.IRule;
import transbit.tbits.api.RuleResult;
import transbit.tbits.common.DatabaseException;
import transbit.tbits.dataProducer.StaticalDataProducer;
import transbit.tbits.domain.BusinessArea;
import transbit.tbits.domain.Field;
import transbit.tbits.domain.Request;
import transbit.tbits.domain.RequestEx;
import transbit.tbits.domain.RequestUser;
import transbit.tbits.domain.Type;
import transbit.tbits.domain.User;
import transbit.tbits.domain.UserType;

public class StatusCycle implements IRule 
{
	
	
	//sysPrefix
	private static final String COB4  = "cob4";
	
	//status Types 
	
	private static final String Active  = "Active";
	private static final String Pending = "Pending Transmittal";
	private static final String sub_client = "Submitted To Client"; 
	private static final String Approved = "Closed";
	private static final String App_as = "Approved As Noted";
	private static final String REJ = "Commented As Noted";
	private static final String Res = "NotApproved";
	private static final String cancel = "Cancelled";
	private static final String info =  "Information";
	
	
	public RuleResult execute(Connection connection, BusinessArea ba,
			Request oldRequest, Request currentRequest, int Source, User user,
			Hashtable<Field, RequestEx> extendedFields, boolean isAddRequest,
			String attachments) {
		// TODO Auto-generated method stub
		
		
		RuleResult ruleresult = new RuleResult();
		String sysPrefix =  ba.getSystemPrefix();
		int asysId = ba.getSystemId();
		int aRequestId = currentRequest.getRequestId();

		
	if(sysPrefix.equals(COB4)&&(!isAddRequest))
	{	
		
			Type statusType = currentRequest.getStatusId();
			String curStatus = statusType.getName();
			
			Type oldStatusType = oldRequest.getStatusId();
			String OldStatus   = oldStatusType.getName();
			try			
			{
				if(OldStatus.equals(Active))
				      {
					   if(curStatus.equals(Pending)||curStatus.equals(OldStatus)||curStatus.equals(cancel))
					     ruleresult.setCanContinue(true);
					   else 
					   {
					    ruleresult.setCanContinue(false);
					    ruleresult.setMessage("status is not Acceptable");
					   }
				      }
				else if(OldStatus.equals(Pending))	
				    {
					   if(curStatus.equals(sub_client)||curStatus.equals(OldStatus)||curStatus.equals(cancel))
						 ruleresult.setCanContinue(true);
					   else
					   {
						 ruleresult.setCanContinue(false);
						 ruleresult.setMessage("status  is not Acceptable");
					   }
					}
				else if(OldStatus.equals(sub_client))
				   {
					if(curStatus.equals(Approved)||curStatus.equals(App_as)||curStatus.equals(info)
					   || curStatus.equals(REJ)||curStatus.equals(OldStatus)||curStatus.equals(Res)
					   ||curStatus.equals(cancel))
					      ruleresult.setCanContinue(true);
					else
					   {
						ruleresult.setCanContinue(false);
						ruleresult.setMessage("status  is not Acceptable");
					   }
				
				   }
				else if(OldStatus.equals(Approved))
				   {
					if(curStatus.equals(Pending)||curStatus.equals(App_as)
					   || curStatus.equals(REJ)||curStatus.equals(OldStatus)||curStatus.equals(Res)
					   ||curStatus.equals(cancel))
					      ruleresult.setCanContinue(true);
					else
					   {
						ruleresult.setCanContinue(false);
						ruleresult.setMessage("status  is not Acceptable");
					   }
				
				   }
				else if(OldStatus.equals(App_as)||OldStatus.equals(REJ)||OldStatus.equals(Res))	
			    {
				    if(curStatus.equals(Approved)||curStatus.equals(Pending)
				       ||curStatus.equals(OldStatus)||curStatus.equals(cancel))
					 ruleresult.setCanContinue(true);
				   else
				   {
					 ruleresult.setCanContinue(false);
					 ruleresult.setMessage("status  is not Acceptable");
				   }
				}
				else if(OldStatus.equals(info))	
			    {
				    if(curStatus.equals(Pending)||curStatus.equals(OldStatus)||curStatus.equals(cancel))
					 ruleresult.setCanContinue(true);
				   else
				   {
					 ruleresult.setCanContinue(false);
					 ruleresult.setMessage("status  is not Acceptable");
				   }
				}	
				
			}
		
			catch (Exception e) {
				e.printStackTrace();
				// TODO: handle exception
			}
			 
	    }
	else
	  {
		ruleresult.setCanContinue(true);
		ruleresult.setMessage("this is not applicable for this  business Areas "+ba.getName());
		
	  }
		
		return ruleresult;
		
	}


	public String getName() {
		// TODO Auto-generated method stub
		return null;
	}


	public double getSequence() {
		// TODO Auto-generated method stub
		return 0;
	}
		
	
}

