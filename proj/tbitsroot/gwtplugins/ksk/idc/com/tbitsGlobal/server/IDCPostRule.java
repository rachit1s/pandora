package idc.com.tbitsGlobal.server;

import static idc.com.tbitsGlobal.server.IDCUtils.IDC_SOURCE_REQUESTS;
import static idc.com.tbitsGlobal.server.IDCUtils.TBITS_ROOT;
import static idc.com.tbitsGlobal.server.IDCUtils.getAllTargetBAs;

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
import transbit.tbits.domain.User;
import transbit.tbits.exception.APIException;
import transbit.tbits.exception.TBitsException;

import commons.com.tbitsGlobal.utils.client.TbitsExceptionClient;

public class IDCPostRule implements IPostRule {

	public static final String name="IDCPostRule";
	public RuleResult execute(Connection connection, BusinessArea ba,
			Request oldRequest, Request currentRequest, int Source, User user, boolean isAddRequest) {

		if( null == ba )
			return new RuleResult( true, name + " : ba passed was null." , false ) ;

		ArrayList<String> validBAs = null;
		try {
			validBAs = getAllTargetBAs();
		} catch (TBitsException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (TbitsExceptionClient e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		String sysPrefix = ba.getSystemPrefix() ;

		if(!validBAs.contains(sysPrefix))
			return new RuleResult(true, name + " : bypassing as the ba is not valid For IDC " + sysPrefix, true ) ;

		try {
			String linkedRequests=currentRequest.get(IDC_SOURCE_REQUESTS);
			ArrayList<String> srList = Utilities.toArrayList(linkedRequests);
			int reqid;
			String baPrefix;							
			Hashtable<String,String> params = new Hashtable<String,String>();
			BusinessArea bai;
			for(String req : srList){

				baPrefix=req.split("#")[0];
				reqid=Integer.parseInt(req.split("#")[1]);

				bai=BusinessArea.lookupBySystemPrefix(baPrefix);
				params.put(Field.REQUEST,reqid+"");
				params.put(Field.BUSINESS_AREA, bai.getSystemId()+"");
				params.put(Field.USER, TBITS_ROOT) ;


				StringBuilder desc= new StringBuilder();
				desc.append("IDC initiated on the document via ");
				desc.append(sysPrefix+'#'); 
				desc.append(currentRequest.getRequestId());
				desc.append(" ["+currentRequest.getSubject()+"]<br>");
				desc.append("to receive comments from the following people -");
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
