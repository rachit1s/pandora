package tpl;

import java.sql.Connection;
import java.util.Collection;
import java.util.Date;
import java.util.Hashtable;

import transbit.tbits.api.AttachmentInfo;
import transbit.tbits.api.IRule;
import transbit.tbits.api.RuleResult;
import transbit.tbits.common.Timestamp;
import transbit.tbits.domain.BusinessArea;
import transbit.tbits.domain.Field;
import transbit.tbits.domain.Request;
import transbit.tbits.domain.RequestEx;
import transbit.tbits.domain.Type;
import transbit.tbits.domain.User;


public class TscrMNDDateSet implements IRule {

	private static final String TSCR = "tscr";
	private static final String SC = "Submitted To Client";
	private static final String MND = "mndcommenteddate";
	private static final String AAN = "Approved As Noted";
	private static final String CAN = "Commented As Noted";
	private static final String NA = "NotApproved";                  
	private static final String INFO = "Information";
	
	
	public RuleResult execute(Connection connection, BusinessArea ba,
			Request oldRequest, Request currentRequest, int Source, User user,
			Hashtable<Field, RequestEx> extendedFields, boolean isAddRequest,
			Collection<AttachmentInfo> attachments) {

		
         RuleResult ruleresult = new RuleResult();
         String Sysprefix = ba.getSystemPrefix();
         
         int SysId = ba.getSystemId();
         
         if(Sysprefix.equalsIgnoreCase(TSCR))
         {
        	 try  {
				    Field MNDDateField = Field.lookupBySystemIdAndFieldName(SysId,MND);
				    RequestEx MNDdateEx = extendedFields.get(MNDDateField);
				    
				    Type OldStatusType = oldRequest.getStatusId();
				    String OldStatus  = OldStatusType.getName();
				    
				    System.out.println("Old status:"+OldStatus);
				    
				    Type CurrStatusType = currentRequest.getStatusId();
				    String Status  = CurrStatusType.getName();
				 
				    
				    System.out.println("curr status:"+Status);
				    
				       			    
				    if((OldStatus.equalsIgnoreCase(SC))&&((Status.equalsIgnoreCase(AAN))||(Status.equalsIgnoreCase(CAN))
				    		||(Status.equalsIgnoreCase(INFO))||(Status.equalsIgnoreCase(NA))))
				        {    
				    	
				    		
				    	           if(MNDdateEx.getDateTimeValue() == null)
				    	             {  
				    	    	         System.out.println("rule follow"); 
				    	    	         ruleresult.setCanContinue(false);
				    		             ruleresult.setMessage("please fill the MND date");
				    	              }
				    	           else
				    	             {
				    		            System.out.println("MND date already set");
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
        		 e.printStackTrace();
			      }
         }
         else
          {
        	 ruleresult.setCanContinue(true);
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
