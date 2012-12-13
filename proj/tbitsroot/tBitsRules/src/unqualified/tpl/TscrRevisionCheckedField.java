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

public class TscrRevisionCheckedField implements IRule {

	
	
	private static final String TSCR = "tscr";
	private static final String DESIGN = "Design";
	private static final String HCC = "HardCopyChecked";
	private static final String DDC = "DrawingsDateChecked";
	private static final String DRC = "DrawingsRevisionChecked";
	private static final String REV = "Revision_Number";
	
	
	public RuleResult execute(Connection connection, BusinessArea ba,
			Request oldRequest, Request currentRequest, int Source, User user,
			Hashtable<Field, RequestEx> extendedFields, boolean isAddRequest,
			Collection<AttachmentInfo> attachments) {
		    
		    
		    RuleResult ruleresult = new RuleResult();
		    String Sysprefix = ba.getSystemPrefix();
		    int sysId = ba.getSystemId();
		    
		if(ba.getSystemPrefix().equalsIgnoreCase(TSCR)||ba.getSystemPrefix().equalsIgnoreCase(DESIGN))
		   {
		  try{  
			  // hard copy checked field
		       Field HCCField = Field.lookupBySystemIdAndFieldName(sysId, HCC);
		       RequestEx HCCEx = extendedFields.get(HCCField);
		       
		       Field DDCField = Field.lookupBySystemIdAndFieldName(sysId, DDC);
		       RequestEx DDCEx = extendedFields.get(DDCField);
		       
		       Field DRCField = Field.lookupBySystemIdAndFieldName(sysId, DRC);
		       RequestEx DRCEx = extendedFields.get(DRCField);
		       
		       if((HCCField == null)||(DDCField == null)||(DRCField == null))
		             {
		    	     ruleresult.setCanContinue(true);
		    	     ruleresult.setMessage("Checked field are not valid names");
		             }
		       
		       else
		       {
		    	   Type CurrentRevType = currentRequest.getExType(REV);
		           String CurrentRevName = CurrentRevType.getName();
		       
		           Type OldRevType = oldRequest.getExType(REV);
		           String OldRevName = OldRevType.getName();
		        
		           if(CurrentRevName.equalsIgnoreCase(OldRevName))
		                {
		        	    ruleresult.setCanContinue(true);
		        	    }
		           else
		               {   
		        	    if(HCCEx.getBitValue()&& DRCEx.getBitValue()&& DDCEx.getBitValue())
        	              {
	        		        ruleresult.setCanContinue(true);
	        		       }
	        	        else
	        	          {
	        		       ruleresult.setCanContinue(false);
	        		       ruleresult.setMessage("Please check deliverable deatils fields");
	        	         }
		        	   
		        	   
		               }
		        }
		  }
		        catch (Exception e) {
			
		                            }
		   }
		else
			ruleresult.setCanContinue(true);
		  
		return ruleresult;
	}

	public String getName() {
		// TODO Auto-generated method stub
		return "checked value for new revision";
	}

	public double getSequence() {
		// TODO Auto-generated method stub
		return 0;
	}

}
