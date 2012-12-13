package kskbilltracking;

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
import transbit.tbits.domain.User;
import transbit.tbits.exception.TBitsException;



import static kskbilltracking.BillConstants.*;
import static kskbilltracking.BillHelper.*;


public class BillPreRule implements IRule {

	public RuleResult execute(Connection connection, BusinessArea ba,
			Request oldRequest, Request currentRequest, int Source, User user,boolean isAddRequest) {
		if( null == ba || null == ba.getSystemPrefix())
			return new RuleResult(false,"null BA",false);

		if(ba.getSystemPrefix().equalsIgnoreCase(Bill_sysprefix))
		{
			int sysId=ba.getSystemId();
			try{

				if(isAddRequest){			
					processAddState(currentRequest,oldRequest,isAddRequest,sysId);			  			   
				}
				else {                
					processUpdateState(currentRequest, oldRequest, isAddRequest, sysId);				
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
			return new RuleResult(true,"ignoring the rule as ba is not :"+Bill_sysprefix,true);

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
