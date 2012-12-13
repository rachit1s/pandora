package kskorg;

import static kskorg.ORGConstants.*;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.Hashtable;

import transbit.tbits.api.IPostRule;
import transbit.tbits.api.RuleResult;
import transbit.tbits.api.UpdateRequest;
import transbit.tbits.common.DatabaseException;
import transbit.tbits.common.Utilities;
import transbit.tbits.domain.BusinessArea;
import transbit.tbits.domain.Field;
import transbit.tbits.domain.Request;
import transbit.tbits.domain.RequestEx;
import transbit.tbits.domain.User;
import transbit.tbits.exception.APIException;
import transbit.tbits.exception.TBitsException;

public class IDPostRule implements IPostRule {
   
	String name="IDPostRule";

	public String getName() {
		
		return name;
	}

	
	public double getSequence() {
		
		return 0;
	}


	public RuleResult execute(Connection connection, BusinessArea ba,
			Request oldRequest, Request currentRequest, int Source, User user, boolean isAddRequest) {
				if( null == ba )
				return new RuleResult( true, name + " : ba passed was null." , false ) ;
			
			String sysPrefix = ba.getSystemPrefix() ;
			
			if(!sysPrefix.equals(ID_sysprefix))
				return new RuleResult(true, name + " : bypassing as the ba is not " + ID_sysprefix , true ) ;
			
			try {
				String linkedRequests=currentRequest.get(ID_Source_Requests);
				ArrayList<String> srList = Utilities.toArrayList(linkedRequests);
				int reqid;
	            String baPrefix;							
				Hashtable<String,String> params = new Hashtable<String,String>();
				BusinessArea bai;
				String desc="the CV was sent for shortlisting via "+sysPrefix+"#"+currentRequest.getRequestId();
				for(String req : srList){
				
					baPrefix=req.split("#")[0];
					reqid=Integer.parseInt(req.split("#")[1]);
	                bai=BusinessArea.lookupBySystemPrefix(baPrefix);
					params.put(Field.REQUEST,reqid+"");
					params.put(Field.BUSINESS_AREA, bai.getSystemId()+"");
					params.put(Field.USER, TBITS_ROOT);
					params.put(CV_Current_Status,CV_Sent_For_ShortListing);
					params.put(CV_Comments,desc);
					

					try {
						UpdateRequest ur = new UpdateRequest() ;
						Request nr=new Request();
						nr = ur.updateRequest(params);
					} catch (TBitsException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (APIException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				
				
			} catch (IllegalStateException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (DatabaseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			// TODO Auto-generated method stub
			return new RuleResult( true , name +"IDPostRule completed" , true );
			//return null;
	

	}
}
