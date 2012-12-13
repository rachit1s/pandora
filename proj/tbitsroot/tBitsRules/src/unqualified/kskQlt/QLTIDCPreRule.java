package kskQlt;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Hashtable;
import java.util.Iterator;

import transbit.tbits.api.AttachmentInfo;
import transbit.tbits.api.IRule;
import transbit.tbits.api.RuleResult;
import transbit.tbits.common.DatabaseException;
import transbit.tbits.common.TBitsLogger;
import transbit.tbits.domain.BusinessArea;
import transbit.tbits.domain.Field;
import transbit.tbits.domain.Request;
import transbit.tbits.domain.RequestEx;
import transbit.tbits.domain.RequestUser;
import transbit.tbits.domain.User;
import transbit.tbits.domain.UserType;

public class QLTIDCPreRule implements IRule {

	final public static String name = "IDC Pre-Rule" ;
	final private static TBitsLogger LOG = TBitsLogger.getLogger("kskQlt");

	public RuleResult execute(Connection connection, BusinessArea ba,
			Request oldRequest, Request currentRequest, int Source, User user, boolean isAddRequest) {


		if( null == ba )
			return new RuleResult( true, name + " : ba passed was null." , false ) ;

		String sysPrefix = ba.getSystemPrefix() ;

		if( ! sysPrefix.equals(QLTIDCConstants.IDC_SYSPREFIX))
			return new RuleResult(true, name + " : bypassing as the ba is not " + QLTIDCConstants.IDC_SYSPREFIX , true ) ;

		//String IDCStatus = currentRequest.get(IDCConstants.IDC_STATUS);

		boolean commentsComplete;
		try {
			int aSystemId = ba.getSystemId();
			commentsComplete = (Boolean)currentRequest.getObject(QLTIDCConstants.IDC_COMMENTS_COMPLETE);
			if(commentsComplete)
			{
				Collection<RequestUser> logger = (Collection<RequestUser>)currentRequest.getObject(Field.LOGGER);
				Collection<RequestUser> assList=(Collection<RequestUser>) currentRequest.getObject(Field.ASSIGNEE);
				Collection<RequestUser> subList=(Collection<RequestUser>)currentRequest.getObject(Field.SUBSCRIBER);
				Iterator<RequestUser> i=logger.iterator();
				while(i.hasNext()){
					RequestUser currentUser=i.next();

					for(RequestUser reqUsr :assList){
						if(reqUsr.equals(currentUser))
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
				}
				currentRequest.setObject(Field.SUBSCRIBER, subList);
				currentRequest.setObject(Field.ASSIGNEE, assList);
			}




		} catch (IllegalStateException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			return new RuleResult( false , name +" : illegal state exception" , false );
		} catch (DatabaseException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			return new RuleResult( false , name +" : database exception" , false );
		}

		return new RuleResult( true , name +"IDCPreRule completed" , true );
	}


	public String getName() {
		// TODO Auto-generated method stub
		return name;
	}

	@Override
	public double getSequence() {
		// TODO Auto-generated method stub
		return 0;
	}

}
