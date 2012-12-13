package ksksco;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Hashtable;

import transbit.tbits.api.AttachmentInfo;
import transbit.tbits.api.IPostRule;
import transbit.tbits.api.IRule;
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
import  static ksksco.scoConstants.*;

public class scoPostRule implements IPostRule {

	public static final String name="scoPostRule";
	

	
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
			
			if( ! sysPrefix.equals(CORR_sysprefix))
				return new RuleResult(true, name + " : bypassing as the ba is not " + CORR_sysprefix , true ) ;
			
			try {
				String linkedRequests=currentRequest.get(CORR_Source_Requests);
				ArrayList<String> srList = Utilities.toArrayList(linkedRequests);
				int reqid;
	            String baPrefix;							
				Hashtable<String,String> params = new Hashtable<String,String>();
				BusinessArea bai;
				for(String req : srList){
				
					baPrefix=req.split("#")[0];
					reqid=Integer.parseInt(req.split("#")[1]);
				
					//Request rq=Request.lookupBySystemIdAndRequestId(ba.getSystemId(),reqid);
	                bai=BusinessArea.lookupBySystemPrefix(baPrefix);
					params.put(Field.REQUEST,reqid+"");
					params.put(Field.BUSINESS_AREA, bai.getSystemId()+"");
					//int rootId = User.lookupAllByUserLogin(TBITS_ROOT).getUserId() ;
					params.put(Field.USER, TBITS_ROOT) ;


					StringBuilder desc= new StringBuilder();
					desc.append("Information Was Sent Via ");
					desc.append(CORR_sysprefix+'#'); 
					desc.append(currentRequest.getRequestId());
					params.put(CORR_Description,desc.toString());

					

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
			return new RuleResult( true , name +"scoPostRule completed" , true );
			//return null;
	

	}
}
