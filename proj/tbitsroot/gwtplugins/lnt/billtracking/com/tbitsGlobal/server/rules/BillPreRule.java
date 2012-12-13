package billtracking.com.tbitsGlobal.server.rules;

import static billtracking.com.tbitsGlobal.server.BillProcessor.processAddBill;
import static billtracking.com.tbitsGlobal.server.BillProcessor.processUpdateBill;
import static billtracking.com.tbitsGlobal.server.BillProperties.billProperties;

import java.sql.Connection;

import transbit.tbits.api.IRule;
import transbit.tbits.api.RuleResult;
import transbit.tbits.domain.BusinessArea;
import transbit.tbits.domain.Request;
import transbit.tbits.domain.User;
import transbit.tbits.exception.TBitsException;
import billtracking.com.tbitsGlobal.shared.IBillProperties;


public class BillPreRule implements IRule,IBillProperties {

	public RuleResult execute(Connection connection, BusinessArea ba,
			Request oldRequest, Request currentRequest, int Source, User user,boolean isAddRequest) {
		
		if( null == ba || null == ba.getSystemPrefix())
			return new RuleResult(false,"null BA",false);
		
		String billPrefix = billProperties.get(PROPERTY_BILL_BA_PREFIX);
		if(ba.getSystemPrefix().equalsIgnoreCase(billPrefix))
		{
			try{

				if(isAddRequest){			
					processAddBill(currentRequest,oldRequest,isAddRequest);			  			   
				}
				else {                
					processUpdateBill(currentRequest, oldRequest, isAddRequest);				
				}

			}
			catch(TBitsException t){
				t.printStackTrace();
				return new RuleResult(false, t.getDescription(), false);
			}
			catch(Exception e){
				e.printStackTrace();
				return new RuleResult(false, e.getMessage(),false);
			}


			return new RuleResult(true,"BillPreRule Executed SuccessFully",true);

		}


		else 
			return new RuleResult(true,"ignoring the rule as ba is not :"+billPrefix,true);

	}


	public String getName() {
		// TODO Auto-generated method stub
		return "BillPreRule";
	}


	public double getSequence() {
		// TODO Auto-generated method stub
		return 0;
	}



}















