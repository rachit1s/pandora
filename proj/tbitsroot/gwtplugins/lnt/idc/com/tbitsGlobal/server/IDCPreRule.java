package idc.com.tbitsGlobal.server;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import transbit.tbits.api.IRule;
import transbit.tbits.api.RuleResult;
import transbit.tbits.common.DatabaseException;
import transbit.tbits.domain.BusinessArea;
import transbit.tbits.domain.Field;
import transbit.tbits.domain.Request;
import transbit.tbits.domain.RequestUser;
import transbit.tbits.domain.User;
import transbit.tbits.exception.TBitsException;

import commons.com.tbitsGlobal.utils.client.TbitsExceptionClient;

public class IDCPreRule implements IRule {
	final public static String name = "IDC Pre-Rule" ;
	public RuleResult execute(Connection connection, BusinessArea ba,
			Request oldRequest, Request currentRequest, int Source, User user, boolean isAddRequest) {


		if( null == ba )
			return new RuleResult( true, name + " : ba passed was null." , false ) ;

		ArrayList<String> validBAs = null;
		try {
			validBAs = IDCUtils.getAllTargetBAs();
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

		//String IDCStatus = currentRequest.get(IDCConstants.IDC_STATUS);
		int aSystemId=ba.getSystemId();
		Boolean commentsComplete;
		try {
			commentsComplete = (Boolean)currentRequest.getObject(IDCUtils.IDC_COMMENTS_COMPLETE);
			if(commentsComplete)
			{
				Collection<RequestUser> assList=(Collection<RequestUser>) currentRequest.getObject(Field.ASSIGNEE);
				Collection<RequestUser> subList=(Collection<RequestUser>)currentRequest.getObject(Field.SUBSCRIBER);

               Collection<RequestUser> iterList=new ArrayList<RequestUser>();
               iterList.addAll(assList);
				for(RequestUser reqUsr :iterList){
					if(reqUsr.getUserId() == user.getUserId())
					{

						Field field=Field.lookupBySystemIdAndFieldName(aSystemId, Field.SUBSCRIBER);
						int fieldId=field.getFieldId();
						int ordering=subList.size()+1;
						RequestUser temp=new RequestUser(reqUsr.getSystemId(),reqUsr.getRequestId(),
								reqUsr.getUserId(),ordering,reqUsr.getIsPrimary(),fieldId);

						if(!subList.contains(temp)){
							subList.add(temp);
						}
						assList.remove(reqUsr);
					}
				}
				currentRequest.setObject(Field.SUBSCRIBER, subList);
				currentRequest.setObject(Field.ASSIGNEE, assList);

			}


		} catch (IllegalStateException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (DatabaseException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		return new RuleResult( true , name +"IDCPreRule completed" , true );
	}


	public String getName() {
		// TODO Auto-generated method stub
		return name;
	}

	
	public double getSequence() {
		// TODO Auto-generated method stub
		return 0;
	}

}
