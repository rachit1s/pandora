package lntCCC;

import java.sql.Connection;
import java.util.ArrayList;

import transbit.tbits.api.AttachmentInfo;
import transbit.tbits.api.IRule;
import transbit.tbits.api.RuleResult;
import transbit.tbits.common.DatabaseException;
import transbit.tbits.domain.BusinessArea;
import transbit.tbits.domain.Field;
import transbit.tbits.domain.Request;
import transbit.tbits.domain.Type;
import transbit.tbits.domain.User;
import transbit.tbits.webapps.AttachmentFieldInfo;

public class DecisionWithAttachment implements IRule{

	@Override
	public RuleResult execute(Connection connection, BusinessArea ba,
			Request oldRequest, Request currentRequest, int Source, User user,
			boolean isAddRequest) {
		
		RuleResult rr = new RuleResult();
		
		String[] applicableBA = new String[]{"CCC_HO","APL_CCC_SO","MAL_CCC_SO","Vemagiri_CCC_SO"};
		if(isApplicable(applicableBA, ba))
		{  
			System.out.println("attachment name : rule applicable CCCCC");
			Type currentDecisionfromCNI =  (Type) currentRequest.getObject("LECnIDecision");
			Type currentDecisionfromELE =  (Type) currentRequest.getObject("LEElectDecision");
			Type currentDecisionfromMech =  (Type) currentRequest.getObject("LEMechDecision");
			
			Type currentDecisionfromSCM =  (Type) currentRequest.getObject("DecisionfromSCM");
			Type currentDecisionfromHOD =  (Type) currentRequest.getObject("DecisionfromHOD");
			
			
			
			
			//RFC - File for commenting.
			
			ArrayList<AttachmentInfo> attachments = new ArrayList<AttachmentInfo>(currentRequest.getAttachmentsOfType("RFC"));
			
			
		    if(currentDecisionfromCNI.getName().equals("Approved") || currentDecisionfromCNI.getName().equals("Rejected")
		    		||currentDecisionfromELE.getName().equals("Approved") || currentDecisionfromELE.getName().equals("Rejected")
		    		|| currentDecisionfromMech.getName().equals("Approved")|| currentDecisionfromMech.getName().equals("Rejected")
		    		|| currentDecisionfromSCM.getName().equals("Approved") || currentDecisionfromSCM.getName().equals("Rejected")
		    		|| currentDecisionfromHOD.getName().equals("Approved") || currentDecisionfromHOD.getName().equals("Rejected"))
		        {
		    	    System.out.println("attachment name :"+ attachments.isEmpty());
					if(attachments.isEmpty())
					    {
						  rr.setCanContinue(false);
						  rr.setMessage("with out File for Commenting  attachments you can't not give approved or rejected decision");
					    }
					
		       }
		}
		
		return rr;
	}

	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public double getSequence() {
		// TODO Auto-generated method stub
		return 110;
	}
	
	private boolean isApplicable(String[] applicableBA, BusinessArea ba) {
		for(String s:applicableBA)
		{
			if(ba.getSystemPrefix().equals(s))
			{
				return true;
			}
		}
		return false;
	}

}
