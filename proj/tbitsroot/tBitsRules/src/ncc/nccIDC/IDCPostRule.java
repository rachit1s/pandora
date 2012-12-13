package nccIDC;

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

public class IDCPostRule implements IPostRule {

	public static final String name="IDCPostRule";
	public RuleResult execute(Connection connection, BusinessArea ba,
			Request oldRequest, Request currentRequest, int Source, User user, boolean isAddRequest) {
		
		if( null == ba )
			return new RuleResult( true, name + " : ba passed was null." , false ) ;
		
		String sysPrefix = ba.getSystemPrefix() ;
		
		if( ! sysPrefix.equals(IDCConstants.IDC_SYSPREFIX))
			return new RuleResult(true, name + " : bypassing as the ba is not " + IDCConstants.IDC_SYSPREFIX , true ) ;
		
		try {
			String linkedRequests=currentRequest.get(IDCConstants.IDC_LINKED_REQUESTS);
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
				int rootId = User.lookupAllByUserLogin(IDCConstants.TBITS_ROOT).getUserId() ;
				params.put(Field.USER, rootId +"") ;


				StringBuilder desc= new StringBuilder();
				desc.append("IDC initiated on the document via ");
				desc.append(IDCConstants.IDC_SYSPREFIX+'#'); 
				desc.append(currentRequest.getRequestId());
				desc.append(" ["+currentRequest.getSubject()+"]");
				desc.append("to receive comments from the following people - ");
				desc.append(currentRequest.get(Field.ASSIGNEE));
				params.put(Field.DESCRIPTION,desc.toString());

				

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
		return new RuleResult( true , name +"IDCPostRule completed" , true );
	}


	public String getName() {
		// TODO Auto-generated method stub
		return "IDCPostRule";
	}

	
	public double getSequence() {
		// TODO Auto-generated method stub
		return 0;
	}

}
