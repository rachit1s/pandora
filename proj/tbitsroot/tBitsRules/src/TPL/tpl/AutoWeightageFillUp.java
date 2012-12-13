package tpl;

import java.sql.Connection;
import transbit.tbits.api.IRule;
import transbit.tbits.api.RuleResult;
import transbit.tbits.domain.BusinessArea;
import transbit.tbits.domain.Request;
import transbit.tbits.domain.User;

public class AutoWeightageFillUp
  implements IRule
{
  public RuleResult execute(Connection connection, BusinessArea ba, Request oldRequest, Request currentRequest, int Source, User user, boolean isAddRequest)
  {
    String sysPrefix = ba.getSystemPrefix();
    if (sysPrefix.equalsIgnoreCase("TPL_PG"))
    {
    	String Originator = currentRequest.get("Originator");
    	if(Originator.equalsIgnoreCase("TCE_VENDOR"))
    	{
    		String EngineeringType = currentRequest.get("EngineeringType");
    		if(EngineeringType.equalsIgnoreCase("BasicEngineering") || EngineeringType.equalsIgnoreCase("DetailedEngineering") )
    		{
      String revision = currentRequest.get("Revision");
      String FlowStatusWithVendor = currentRequest.get("FlowStatusWithVendor");
      
      String DecisionFromFI = currentRequest.get("DecisionFromFI");
      if (DecisionFromFI.equalsIgnoreCase("D1"))
      {
        Double a = Double.valueOf(95.0D);
        currentRequest.setObject("OwnerWeightage", a);
      }
      
      else 
      {
      
      if (revision.contains("P")&&FlowStatusWithVendor.equalsIgnoreCase("UnderReview"))
      {
        Double a = Double.valueOf(50.0D);
        currentRequest.setObject("OwnerWeightage", a);
      }

      if (revision.contains("R"))
      {
        Double b = Double.valueOf(80.0D);
        currentRequest.setObject("OwnerWeightage", b);
      }
      }
      
      String VendorRevision = currentRequest.get("VendorRevision");
      if(VendorRevision.equalsIgnoreCase("AsBuilt"))
      {
          Double c = Double.valueOf(100.0D);
          currentRequest.setObject("OwnerWeightage", c);
      }
      
    	}
    		else if (EngineeringType.equalsIgnoreCase("Specification"))
    		{
    			Boolean obj =(Boolean) currentRequest.getObject("IssuedToBidders");
    			Boolean a = new Boolean(true);
    			if(obj.equals(a))
    			{
    		        Double b = Double.valueOf(80.0D);
    		        currentRequest.setObject("OwnerWeightage", b);
    			}
    			
    			Boolean obj1 =(Boolean) currentRequest.getObject("IssuedToEnquiry");
    			Boolean a1 = new Boolean(true);
    			if(obj1.equals(a1))
    			{
    		        Double b = Double.valueOf(100.0D);
    		        currentRequest.setObject("OwnerWeightage", b);
    			}
    			
    		}
    		
    		else if (EngineeringType.equalsIgnoreCase("TER"))
    		{
    			String FlowStatusWithVendor = currentRequest.get("FlowStatusWithVendor");
    			if(FlowStatusWithVendor.equalsIgnoreCase("UnderReview"))
    			{
		        Double b = Double.valueOf(80.0D);
		        currentRequest.setObject("OwnerWeightage", b);
    			}
    			Boolean obj1 =(Boolean) currentRequest.getObject("AwardoC");
    			Boolean a1 = new Boolean(true);
		        if(obj1.equals(a1))
		        {
    		        Double c = Double.valueOf(100.0D);
    		        currentRequest.setObject("OwnerWeightage", c);
		        }
    		}
    	}
    	
    	if(!Originator.equalsIgnoreCase("TCE_VENDOR"))
    	{
    	      String VendorRevision = currentRequest.get("VendorRevision");
    	      if(VendorRevision.equalsIgnoreCase("UnderReview"))
    	      {
    	          Double c = Double.valueOf(60.0D);
    	          currentRequest.setObject("OwnerWeightage", c);
    	      }
    	      
    	      String DecisionFromFI = currentRequest.get("DecisionFromFI");
    	      if (DecisionFromFI.equalsIgnoreCase("D1"))
    	      {
    	        Double a = Double.valueOf(100.0D);
    	        currentRequest.setObject("OwnerWeightage", a);
    	      }
    	}
    }    
    return null;
  }

  public double getSequence()
  {
    return 0.0D;
  }

  public String getName()
  {
    return null;
  }
}