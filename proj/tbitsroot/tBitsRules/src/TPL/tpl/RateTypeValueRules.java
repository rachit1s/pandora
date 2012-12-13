package tpl;

import java.sql.Connection;
import java.util.Collection;

import transbit.tbits.api.APIUtil;
import transbit.tbits.api.IRule;
import transbit.tbits.api.RuleResult;
import transbit.tbits.common.DatabaseException;
import transbit.tbits.domain.BusinessArea;
import transbit.tbits.domain.Request;
import transbit.tbits.domain.RequestDataType;
import transbit.tbits.domain.Type;
import transbit.tbits.domain.User;

public class RateTypeValueRules implements IRule {

	@Override
	public RuleResult execute(Connection connection, BusinessArea ba,
			Request oldRequest, Request currentRequest, int Source, User user,
			boolean isAddRequest) {
		// TODO Auto-generated method stub
		int sysId = ba.getSystemId();
		String sysPrefix = ba.getSystemPrefix();
		if(sysId ==36 && sysPrefix.equalsIgnoreCase("Invoicing"))
		{
			String ratePmt = currentRequest.get("RatePMT").toString();

			if(ratePmt.equalsIgnoreCase("RateContract"))
			{
				String linkedRequests = currentRequest.get("related_requests");
				if(linkedRequests == null)
					return new RuleResult(false, "Linked Request cannot be Blank");
				else
				{
					Collection<RequestDataType> crr1 = APIUtil.getRequestCollection(linkedRequests);
					for (RequestDataType rdt : crr1) {
	                    try
	                    {
	                      Request r = Request.lookupBySystemIdAndRequestId(
	                        rdt.getSysId(), rdt.getRequestId());
	                      
	                      String transporter = r.getObject("subject").toString();
	                      String source = r.getObject("Source").toString();
	                      String Destination = r.getObject("Destination").toString();
	                      String PCRNo = r.getObject("PCRNo").toString();
	                      String NewRates = r.getObject("NewRates").toString();
	                      String applicable = r.get("category_id").toString();
	                      Type app = Type.lookupBySystemIdAndFieldNameAndTypeName(sysId, "Material", applicable);
	                      
	                      currentRequest.setObject("Transporter", transporter);
	                      currentRequest.setObject("Origin", source);
	                      currentRequest.setObject("Destination", Destination);
	                      currentRequest.setObject("PCRNo", PCRNo);
	                      currentRequest.setObject("Rate", Double.valueOf(NewRates).doubleValue());
	                      currentRequest.setObject("Material", app);
	                      
	                    }
	                    catch (DatabaseException e)
	                    {
	                      e.printStackTrace();
	                    }
	                  }
				}
			}
			
			if(ratePmt.equalsIgnoreCase("LCV"))
			{
				String Distance = currentRequest.getObject("Distance").toString();
				if(Distance.equalsIgnoreCase(""))
					return new RuleResult(false, "Distance cannot be Blank");
				
				String PMPK = currentRequest.getObject("PMPK").toString();
				if(PMPK.equalsIgnoreCase("0.0"))
					return new RuleResult(false, "PMPK cannot be Blank or 0.0");
				
			}
			
			if(ratePmt.equalsIgnoreCase("PartLoad"))
			{
				String Distance = currentRequest.getObject("Distance").toString();
				if(Distance.equalsIgnoreCase(""))
					return new RuleResult(false, "Distance cannot be Blank");
				
				String PMPK = currentRequest.getObject("PMPK").toString();
				if(PMPK.equalsIgnoreCase("0.0"))
					return new RuleResult(false, "PMPK cannot be Blank or 0.0");
				
				String DoorDelvCharge = currentRequest.getObject("DoorDelvCharge").toString();
				if(DoorDelvCharge.equalsIgnoreCase("0.0"))
					return new RuleResult(false, "DoorDelvCharge cannot be Blank or 0.0");
				
				String DoorCollecCharge = currentRequest.getObject("DoorCollecCharge").toString();
				if(DoorCollecCharge.equalsIgnoreCase("0.0"))
					return new RuleResult(false, "DoorCollecCharge cannot be Blank or 0.0");
				
			}
			
			if(ratePmt.equalsIgnoreCase("Manual"))
			{
				String Transporter = currentRequest.getObject("Transporter").toString();
				if(Transporter.equalsIgnoreCase(""))
					return new RuleResult(false, "Transporter cannot be Blank");
				
				String Origin = currentRequest.getObject("Origin").toString();
				if(Origin.equalsIgnoreCase(""))
					return new RuleResult(false, "Origin cannot be Blank");
				
				String Destination = currentRequest.getObject("Destination").toString();
				if(Destination.equalsIgnoreCase(""))
					return new RuleResult(false, "Destination cannot be Blank");
				
//				String PCRNo = currentRequest.getObject("PCRNo").toString();
//				if(PCRNo.equalsIgnoreCase(""))
//					return new RuleResult(false, "PCR No cannot be Blank");
				
				String Rate = currentRequest.getObject("Rate").toString();
				if(Rate.equalsIgnoreCase("0.0"))
					return new RuleResult(false, "Rate cannot be Blank or 0.0");
				
				String Material = currentRequest.get("Material");
				if(Material.equalsIgnoreCase("-"))
					return new RuleResult(false, "Please Select Material");
				
			}
			if(ratePmt.equalsIgnoreCase("Fixed"))
			{	
				String Rate = currentRequest.getObject("Rate").toString();
				if(Rate.equalsIgnoreCase("0.0"))
					return new RuleResult(false, "Rates cannot be Blank or 0.0");
				
			}
			
			
		}
		return null;
	}

	@Override
	public double getSequence() {
		// TODO Auto-generated method stub
		return 1.0;
	}

	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return null;
	}

}
