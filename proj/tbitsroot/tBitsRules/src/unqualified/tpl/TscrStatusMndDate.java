package tpl;

import java.sql.Connection;
import java.util.Collection;
import java.util.Hashtable;

import transbit.tbits.api.AttachmentInfo;
import transbit.tbits.api.IRule;
import transbit.tbits.api.RuleResult;
import transbit.tbits.domain.BusinessArea;
import transbit.tbits.domain.Field;
import transbit.tbits.domain.Request;
import transbit.tbits.domain.RequestEx;
import transbit.tbits.domain.Type;
import transbit.tbits.domain.User;

public class TscrStatusMndDate implements IRule {
    
	
	private static final String TSCR = "tscr";
	private static final String MND = "mndcommenteddate";
	private static final String AAN = "Approved As Noted";
	private static final String CAN = "Commented As Noted";
	private static final String NA = "NotApproved" ;                  
	private static final String INFO = "Information" ;
	private static final String APP = "Closed";
		
		   public RuleResult execute(Connection connection, BusinessArea ba,
			Request oldRequest, Request currentRequest, int Source, User user,
			Hashtable<Field, RequestEx> extendedFields, boolean isAddRequest,
			Collection<AttachmentInfo> attachments) {

			   RuleResult ruleresult = new RuleResult();
			   String Sysprefix = ba.getSystemPrefix();
			   
			   int sysId = ba.getSystemId(); 
			   
			   if(ba.getSystemPrefix().equalsIgnoreCase(TSCR))
			   {
			     try
			     {  
			   Field MNDdateField = Field.lookupBySystemIdAndFieldName(sysId, MND);
			   RequestEx MNDdateEx    = extendedFields.get(MNDdateField);
			   
			   Type statusType = currentRequest.getStatusId();
			   String curStatus = statusType.getName();
			   
			     if(MNDdateEx.getDateTimeValue().isNull())
			            {
				       ruleresult.setCanContinue(true);
				       ruleresult.setMessage("MND date value is null");
				        }
			      else
			          {
				          if(curStatus.equalsIgnoreCase(AAN)||curStatus.equalsIgnoreCase(NA)
				        		  ||curStatus.equalsIgnoreCase(CAN)||curStatus.equalsIgnoreCase(INFO)||curStatus.equalsIgnoreCase(APP))
				           {
				    	   ruleresult.setCanContinue(true);
					       ruleresult.setMessage("MND Staus is set");
				           }
				           else
				            {
				    	    ruleresult.setCanContinue(false);
				            ruleresult.setMessage("Please mention right Status");
				            }
			          }
			  
			      }
		          catch (Exception e) 
		               {
			
		              }
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
