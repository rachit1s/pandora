package tpl;
import java.sql.Connection;

import transbit.tbits.api.IRule;
import transbit.tbits.api.RuleResult;
import transbit.tbits.domain.BusinessArea;
import transbit.tbits.domain.Request;
import transbit.tbits.domain.User;


public class LogisticInvoicingRules implements IRule {

	@Override
	public RuleResult execute(Connection connection, BusinessArea ba,
			Request oldRequest, Request currentRequest, int Source, User user,
			boolean isAddRequest) {
		// TODO Auto-generated method stub
		int sysId = ba.getSystemId();
		String sysPrefix = ba.getSystemPrefix();
		if(sysId ==36 && sysPrefix.equalsIgnoreCase("Invoicing"))
		{
			String OtherCharge = currentRequest.getObject("OtherCharges").toString();
			String ReasonforOC = currentRequest.getObject("ReasonforOC").toString();
			if(!OtherCharge.equalsIgnoreCase("0.0"))
			{
				if(ReasonforOC.equalsIgnoreCase(""))
					return new RuleResult(false, "Reason for OC cannot be Blank");
			}
				
			String RecoveryofShorts = currentRequest.getObject("RecoveryofShort").toString();
			String ReasonforROS = currentRequest.getObject("ReasonforROS").toString();
			if(!RecoveryofShorts.equalsIgnoreCase("0.0"))
			{
				if(ReasonforROS.equalsIgnoreCase(""))
					return new RuleResult(false, "Reason for ROS cannot be Blank");
			}
			
			String PaidWt = currentRequest.getObject("PaidWeight").toString();
			double PaidWeight = Double.valueOf(PaidWt).doubleValue();
			String rate = currentRequest.getObject("Rate").toString();
			double Rate = Double.valueOf(rate).doubleValue();
			String PMPKK = currentRequest.getObject("PMPK").toString();
			double PMPK = Double.valueOf(PMPKK).doubleValue();
			String Distances = currentRequest.getObject("Distance").toString();
			double Distance=0;
			if(Distances.equalsIgnoreCase(""))
				Distance =0;
			else
			Distance = Double.valueOf(Distances).doubleValue();
			
			String ratePmt = currentRequest.get("RatePMT").toString();
			double BasicValue=0;
			
			if(ratePmt.equalsIgnoreCase("RateContract"))
			{
			BasicValue = PaidWeight * Rate;
			currentRequest.setObject("BasicValue", BasicValue);
			}
			
			if(ratePmt.equalsIgnoreCase("LCV"))
			{
			BasicValue = PaidWeight * Distance * PMPK;
			currentRequest.setObject("BasicValue", BasicValue);
			}
			
			if(ratePmt.equalsIgnoreCase("PartLoad"))
			{
			BasicValue = PaidWeight * Distance * PMPK;
			currentRequest.setObject("BasicValue", BasicValue);
			}
			
			if(ratePmt.equalsIgnoreCase("Fixed"))
			{
			BasicValue = Rate;
			currentRequest.setObject("BasicValue", BasicValue);
			}
			
			String DetentionCharge = currentRequest.getObject("DetentionCharges").toString();
			double DetentionCharges = Double.valueOf(DetentionCharge).doubleValue();
			String TollTaxs = currentRequest.getObject("TollTax").toString();
			double TollTax = Double.valueOf(TollTaxs).doubleValue();
//			String OtherCharge = currentRequest.getObject("OtherCharges").toString();
			double OtherCharges = Double.valueOf(OtherCharge).doubleValue();
			String DoorCollecCharg = currentRequest.getObject("DoorCollecCharge").toString();
			double DoorCollecCharge = Double.valueOf(DoorCollecCharg).doubleValue();
			String DoorDelvCharg = currentRequest.getObject("DoorDelvCharge").toString();
			double DoorDelvCharge = Double.valueOf(DoorDelvCharg).doubleValue();
			double GrossValue=0;
			if(ratePmt.equalsIgnoreCase("PartLoad"))
			{
			GrossValue = BasicValue + DetentionCharges + TollTax + OtherCharges + DoorCollecCharge + DoorDelvCharge;
			}
			else
			{
			GrossValue = BasicValue + DetentionCharges + TollTax + OtherCharges;
			}
			currentRequest.setObject("GrossValue", GrossValue);
			
//			String RecoveryofShorts = currentRequest.getObject("RecoveryofShort").toString();
			double RecoveryofShort = Double.valueOf(RecoveryofShorts).doubleValue();
			String OtherDeduction = currentRequest.getObject("OtherDeductions").toString();
			double OtherDeductions = Double.valueOf(OtherDeduction).doubleValue();
			double TotalDeductions = RecoveryofShort + OtherDeductions;
			currentRequest.setObject("TotalDeductions", TotalDeductions);
			
			double TotalAmount = GrossValue - TotalDeductions;
			currentRequest.setObject("TotalAmount", TotalAmount);
			
		}
		return null;
	}

	@Override
	public double getSequence() {
		// TODO Auto-generated method stub
		return 2.0;
	}

	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return "LogisticInvoicingRules";
	}

}
