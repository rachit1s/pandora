package nccCorres;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.TimeZone;

import transbit.tbits.Helper.TBitsConstants;
import transbit.tbits.api.AddRequest;
import transbit.tbits.api.IPostRule;
import transbit.tbits.api.RuleResult;
import transbit.tbits.api.UpdateRequest;
import transbit.tbits.common.DataSourcePool;
import transbit.tbits.common.DatabaseException;
import transbit.tbits.common.TBitsLogger;
import transbit.tbits.common.TBitsResourceManager;
import transbit.tbits.common.Timestamp;
import transbit.tbits.common.Utilities;
import transbit.tbits.domain.BAForm;
import transbit.tbits.domain.BusinessArea;
import transbit.tbits.domain.Field;
import transbit.tbits.domain.Request;
import transbit.tbits.domain.RequestEx;
import transbit.tbits.domain.RequestUser;
import transbit.tbits.domain.Type;
import transbit.tbits.domain.User;
import transbit.tbits.domain.UserType;
import transbit.tbits.exception.APIException;
import transbit.tbits.exception.TBitsException;
import static nccCorres.CorresConstants.* ;

public class CorresAddPostRule implements IPostRule {
	
	public static final String PKG_KSK = "transbit.tbits.KSK";
	/**
	 * logger for this class
	 */
	public static final TBitsLogger LOG = TBitsLogger.getLogger(PKG_KSK);
	
	
	public Type getCategoryType( int sys_id ,String firm, String location, String fieldName ) throws DatabaseException 
    {
    	// get the location code 
    	// get the user_firm
    	//int loc = Integer.parseInt(location) ;
    	ArrayList<Type> tl = Type.lookupAllBySystemIdAndFieldName(sys_id, fieldName ) ;
    
    	for( Iterator<Type> it = tl.iterator() ; it.hasNext() ; )
    	{
    		Type type = it.next() ;
    		String name = type.getName() ;
    		if( name.contains(firm) && name.substring(name.trim().length()-2).equals(location.trim()))
    			return type ;    	
    	}
    	
    	return null ;
    }
	
	public RuleResult execute(Connection connection, BusinessArea ba,
			Request oldRequest, Request currentRequest, int Source, User user,
			boolean isAddRequest) 
	{
		try 
		{			
			// both for update and add request
			if( (null != ba) && (null != ba.getSystemPrefix()) &&  ba.getSystemPrefix().equalsIgnoreCase(CorresConstants.CORR_SYSPREFIX)  )				
			{
//				try
//				{
//					User knplUser = User.lookupAllByUserLogin(KNPL_CC_USER);
//					if( hasKNPLUser( currentRequest ) && null != knplUser )
//					{						
//						Collection<RequestUser> ccs = (Collection<RequestUser>) currentRequest.getObject(CORR_CC_FIELD_NAME) ;
//						if( null == ccs )
//							ccs = new ArrayList<RequestUser>() ;
//						//   public RequestUser(int aSystemId, int aRequestId, int aUserId, int aOrdering, boolean aIsPrimary,int aFieldId) {
//						Field ccField = Field.lookupBySystemIdAndFieldName(currentRequest.getSystemId(), CORR_CC_FIELD_NAME);
//						RequestUser extraRs = new RequestUser(currentRequest.getSystemId(),currentRequest.getRequestId(),knplUser.getUserId(),ccs.size()+1,false,ccField.getFieldId());
//						ccs.add(extraRs);
//						currentRequest.setCcs(ccs);
//					}
//				}
//				catch(DatabaseException de)
//				{
//					LOG.info(TBitsLogger.getStackTrace(de));
//				}
				
				// 2. log the request in FC business area
				String generate = currentRequest.get(CorresConstants.CORR_GENERATE_FIELD_NAME);
				if( generate.equals(CorresConstants.CORR_GEN_DONT_GEN_ANYTHING))
					return new RuleResult(true, "Rule says no corr file to be generated. Hence nothing to be logged into FC" , true);
					
				boolean shouldIAddNewRequest = true ;

				Hashtable<String,String> params = new Hashtable<String,String>() ;
							
				String logger = currentRequest.get(CorresConstants.CORR_LOGGER_FIELD_NAME) ;
				String assignee = currentRequest.get(CorresConstants.CORR_ASSIGNEE_FIELD_NAME) ;
				String subscribers = currentRequest.get(CorresConstants.CORR_SUBSCRIBER_FIELD_NAME) ;				
				String corrType = currentRequest.get(CorresConstants.CORR_CORR_TYPE_FIELD_NAME) ;
				String originator = currentRequest.get(CorresConstants.CORR_ORIGINATOR_FIELD_NAME) ;
				String recepient = currentRequest.get(CorresConstants.CORR_RECEPIENT_FIELD_NAME);
				String subject = currentRequest.get(CorresConstants.CORR_SUBJECT_FIELD_NAME) ;
				String corrNo = currentRequest.getExString(CorresConstants.CORR_CORRESPONDENCE_NUMBER_FIELD) ;
				String corrFile = currentRequest.getExString(CorresConstants.CORR_CORRESPONDANCE_FILE_FIELD_NAME) ;
				String otherFile = currentRequest.get(CorresConstants.CORR_OTHER_ATTACHMENTS_FIELD_NAME) ;
				String dicp = currentRequest.get(CorresConstants.CORR_DISCIPLINE_FIELD_NAME);
				String genAtt = currentRequest.get(CORR_GENATT_FIELD_NAME);
				String genWbs = currentRequest.get(CORR_WBSATT_FIELD_NAME);
				String pack = currentRequest.get(CORR_PACKAGE_FIELD_NAME);				
				String cc = currentRequest.get(CORR_CC_FIELD_NAME);
				
				if( null != logger)
					params.put(CorresConstants.FC_LOGGER_FIELD_NAME, logger ) ;
				if( null != assignee )
					params.put(CorresConstants.FC_ASSIGNEE_FIELD_NAME, assignee ) ;
				if( null != subscribers )
					params.put(CorresConstants.FC_SUBSCRIBER_FIELD_NAME, subscribers) ;
				if( null != cc)
					params.put(FC_CC_FIELD_NAME, cc);
				if( null != corrType )
					params.put(CorresConstants.FC_CORR_TYPE_FIELD_NAME, corrType ) ;
				if( null != originator ) 
					params.put(CorresConstants.FC_ORIGINATOR_FIELD_NAME, originator) ;
				if( null != recepient ) 
					params.put(CorresConstants.FC_RECEPIENT_FIELD_NAME, recepient) ;
				if( null != subject ) 
					params.put(FC_SUBJECT_FIELD_NAME, subject ) ;
				if( null != dicp )
					params.put(FC_DISCIPLINE_FIELD_NAME, dicp);
				if( null != genAtt )
					params.put(FC_GENATT_FIELD_NAME, genAtt ) ;
				if( null != genWbs )
					params.put(FC_WBSATT_FIELD_NAME, genWbs ) ;
				if(null != pack)
					params.put(FC_PACKAGE_FIELD_NAME, pack);
				
				if( null != corrNo )
				{
					params.put(CorresConstants.FC_CORRESPONDANCE_NUMBER_FIELD_NAME, corrNo ) ;
					// now check if this correspondence no. was present in some other requests in FC
					// if yes then do not add new request instead update that request
					// if no then add new request
//					int fcsysid = 
					int fcSysID = BusinessArea.lookupBySystemPrefix(CorresConstants.FC_SYSPREFIX).getSystemId() ; 
					int cnFieldID = Field.lookupBySystemIdAndFieldName(fcSysID, CorresConstants.CORR_CORRESPONDENCE_NUMBER_FIELD ).getFieldId() ;
					String sql = "select " + Field.REQUEST + " from requests_ex where " + Field.BUSINESS_AREA +"="+fcSysID + " and field_id="+cnFieldID + " and varchar_value='"+corrNo +"'";
//					Connection conx = null ;
					try
					{
//						conx = DataSourcePool.getConnection() ;
						PreparedStatement ps = connection.prepareStatement(sql) ;
						ResultSet rs = ps.executeQuery() ;
						if( rs.next() ) 
						{
							// yes the corr. no. was logged before
							int req_id = rs.getInt(Field.REQUEST) ;
							params.put(Field.REQUEST, req_id+"") ;
							shouldIAddNewRequest = false ;
						}
					} catch (SQLException e) {
						LOG.info(TBitsLogger.getStackTrace(e));
					}
				}
				if( null != corrFile )
					params.put(CorresConstants.FC_CORRESPONDANCE_FILE_FIELD_NAME, corrFile ) ;
				if( null != otherFile )
					params.put(CorresConstants.FC_OTHER_ATTACHMENT_FIELD_NAME, otherFile ) ;
				
				String description = "This request was automatically generated in response to Add / Update action on the request : " ;
				description += ba.getSystemPrefix()+"#"+currentRequest.getRequestId() + "#" + currentRequest.getMaxActionId() ;
				params.put(Field.DESCRIPTION, description ) ;
				
				params.put(Field.BUSINESS_AREA, BusinessArea.lookupBySystemPrefix(CorresConstants.FC_SYSPREFIX).getSystemId()+"") ;
				params.put(Field.USER, CorresConstants.ROOT_USER) ;
												
				if( shouldIAddNewRequest )
				{
					TBitsResourceManager trmgr = new TBitsResourceManager();
					AddRequest ar = new AddRequest() ;
					ar.setSource(TBitsConstants.SOURCE_CMDLINE);
					ar.addRequest(connection, trmgr, params);
					trmgr.commit();
				}
				else
				{
					TBitsResourceManager trmgr = new TBitsResourceManager();
					UpdateRequest ur = new UpdateRequest() ;
					ur.setSource(TBitsConstants.SOURCE_CMDLINE);
					ur.updateRequest(connection, trmgr, params);
					trmgr.commit() ;
				}

				return new RuleResult() ;
			}
			else return new RuleResult() ;
			
		} catch (TBitsException te) {
			te.printStackTrace();
			return new RuleResult( false , te.getDescription(), false );
		}
		catch (Exception e) {			
			e.printStackTrace();
			return new RuleResult( true , "Exception while adding the correspondance to the Individual Filed Letters business Area", false ) ;
		} catch (APIException e) {
			e.printStackTrace();
			return new RuleResult( true , "Exception while adding the correspondance to the Individual Filed Letters business Area", false ) ;
		} 
	}

	private boolean hasKNPLUser(Request currentRequest) 
	{
		ArrayList<RequestUser> users = new ArrayList<RequestUser>() ;
		Collection<RequestUser> loggers = currentRequest.getLoggers() ;
		Collection<RequestUser> ccs = currentRequest.getCcs() ;
		Collection<RequestUser> assignees = currentRequest.getAssignees() ;
		Collection<RequestUser> subs = currentRequest.getSubscribers() ;
		
		if( null != loggers )
			users.addAll(loggers) ;
		if( null != ccs )
			users.addAll(ccs);
		if( null != assignees )
			users.addAll(assignees);
		if( null != subs )
			users.addAll(subs);
		
		for( RequestUser ru : users )
		{
			try {
				if( ru.getUser().getFirmCode().equalsIgnoreCase(CORR_ORIG_KNPL) )
					return true ;
			} catch (DatabaseException e) {			
				e.printStackTrace();
			}
		}
		
		return false ;			
	}

	public String getName() 
	{
		return "Add Correspondance to the Individual Filed Letters business Area Post Rule ";
	}

	public double getSequence() 
	{
		return 0;
	}
}
