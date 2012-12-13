package kskCorres;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Hashtable;
import java.util.Iterator;

import transbit.tbits.Helper.TBitsConstants;
import transbit.tbits.api.AddRequest;
import transbit.tbits.api.IPostRule;
import transbit.tbits.api.RuleResult;
import transbit.tbits.api.UpdateRequest;
import transbit.tbits.common.DataSourcePool;
import transbit.tbits.common.DatabaseException;
import transbit.tbits.common.TBitsLogger;
import transbit.tbits.common.TBitsResourceManager;
import transbit.tbits.common.Utilities;
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
		// TODO : 1. send a new request to the FC : done  
		// 		  2. (if required)send the notification to mailing list 
						// algo :add the mailing list (user) to the cc field of the 

		try 
		{			
			// both for update and add request
			if( (null != ba) && (null != ba.getSystemPrefix()) &&  ba.getSystemPrefix().equals(KskConstants.CORR_SYSPREFIX)  )				
			{
				Boolean genCor =  currentRequest.getExBoolean(KskConstants.CORR_GENERATE_CORRESPONDANCE_FIELD_NAME) ;
				if( null == genCor || !genCor )
					return new RuleResult( true ," Skipping " + getName(), true );
					
				boolean shouldIAddNewRequest = true ;
				// task 1.
				Hashtable<String,String> params = new Hashtable<String,String>() ;
							
				String logger = currentRequest.get(KskConstants.CORR_LOGGER_FIELD_NAME) ;
				String assignee = currentRequest.get(KskConstants.CORR_ASSIGNEE_FIELD_NAME) ;
				String subscribers = currentRequest.get(KskConstants.CORR_SUBSCRIBER_FIELD_NAME) ;				
				String corrInit = (String) currentRequest.get(KskConstants.CORR_CORRESPONDANCE_INITIATOR_FIELD_NAME) ;
				String type = (String) currentRequest.get(KskConstants.CORR_TYPE_FIELD_NAME) ;				
				String subject = currentRequest.getSubject() ;
				String corrNo = currentRequest.getExString(KskConstants.CORR_CORRESPONDANCE_NUMBER_FIELD) ;
				String corrFile = currentRequest.getExString(KskConstants.CORR_CORRESPONDANCE_FILE_FIELD_NAME) ;
				String attachFile = currentRequest.get(Field.ATTACHMENTS) ;
				
				params.put(KskConstants.FC_LOGGER_FIELD_NAME, logger ) ;
				params.put(KskConstants.FC_ASSIGNEE_FIELD_NAME, assignee ) ;
				if( null != subscribers )
					params.put(KskConstants.FC_SUBSCRIBER_FIELD_NAME, subscribers) ;
				if( null != corrInit )
					params.put(KskConstants.FC_CORRESPONDANCE_INITIATOR_FIELD_NAME, corrInit ) ;
				if( null != type ) 
					params.put(KskConstants.FC_TYPE_FIELD_NAME, type) ;
				if( null != subject ) 
					params.put(Field.SUBJECT, subject ) ;
				if( null != corrNo )
				{
					params.put(KskConstants.FC_CORRESPONDANCE_NUMBER_FIELD, corrNo ) ;
					// now check if this correspondence no. was present in some other requests in FC
					// if yes then do not add new request instead update that request
					// if no then add new request
//					int fcsysid = 
					int fcSysID = BusinessArea.lookupBySystemPrefix(KskConstants.FC_SYSPREFIX).getSystemId() ; 
					int cnFieldID = Field.lookupBySystemIdAndFieldName(fcSysID, KskConstants.FC_CORRESPONDANCE_NUMBER_FIELD ).getFieldId() ;
					String sql = "select " + Field.REQUEST + " from requests_ex where " + Field.BUSINESS_AREA +"="+fcSysID + " and field_id="+cnFieldID + " and varchar_value='"+corrNo +"'";
					Connection conx = null ;
					try
					{
						conx = DataSourcePool.getConnection() ;
						PreparedStatement ps = conx.prepareStatement(sql) ;
						ResultSet rs = ps.executeQuery() ;
						if( rs.next() ) 
						{
							// yes the corr. no. was logged before
							int req_id = rs.getInt(Field.REQUEST) ;
							params.put(Field.REQUEST, req_id+"") ;
							shouldIAddNewRequest = false ;
						}
					} catch (SQLException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					finally
					{
						if( null != conx )
						{
							try {
								conx.close() ;
							} catch (SQLException e) {								
								e.printStackTrace();
								LOG.warn("Exception while closing database connection.") ;
							}
						}
					}
				}
				if( null != corrFile )
					params.put(KskConstants.FC_CORRESPONDANCE_FILE_FIELD_NAME, corrFile ) ;
				if( null != attachFile )
					params.put(Field.ATTACHMENTS, attachFile ) ;
				
				String description = "This request was automatically generated in response to Add / Update action on the request : " ;
				description += ba.getSystemPrefix()+"#"+currentRequest.getRequestId() + "#" + currentRequest.getMaxActionId() ;
				params.put(Field.DESCRIPTION, description ) ;
				
				params.put(Field.BUSINESS_AREA, BusinessArea.lookupBySystemPrefix(KskConstants.FC_SYSPREFIX).getSystemId()+"") ;
				params.put(Field.USER,KskConstants.ROOT_USER) ;
				
				// decide on the basis of assignee what should be the value of Recepient (severity_id) in FC
				// get the first assignee
				String assignee0 = Utilities.toArrayList(assignee).get(0) ;
				int assignee0_id = User.lookupAllByUserLogin(assignee0).getUserId() ;
				try
				{
					Hashtable<String,String> ass_info = UserInfoManager.getUserInfo(assignee0_id) ;
					String ass_firm = ass_info.get(UserInfoManager.FIRM) ;
					String ass_loc = ass_info.get(UserInfoManager.LOCATION ) ;
					
					Type ass_cat = getCategoryType( BusinessArea.lookupBySystemPrefix(KskConstants.FC_SYSPREFIX).getSystemId(), ass_firm, ass_loc, KskConstants.FC_RECEPIENT_FIELD_NAME ) ;
					
					if( ass_cat != null )
						params.put(KskConstants.FC_RECEPIENT_FIELD_NAME, ass_cat.getName() ) ;
				}
				catch( Exception iae )
				{
					//iae.printStackTrace() ;
					LOG.error("Exception while accessing the assignee : " + assignee0 + " info in ksk_user_info.\n" +
							"The recepient field for this request will not be correct.") ;
				}
								
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
					UpdateRequest ur = new UpdateRequest();
					ur.setSource(TBitsConstants.SOURCE_CMDLINE);
					ur.updateRequest(connection, trmgr, params);
					trmgr.commit() ;
				}			
				
				// task 2.
				try
				{ 
					User mailingList = getMailingList(currentRequest) ;
					if( mailingList != null )
					{
						Collection<RequestUser> allCCs = currentRequest.getCcs() ;
					
						Field ccField = Field.lookupBySystemIdAndFieldName(currentRequest.getSystemId(), Field.CC);
//					    public RequestUser(int aSystemId, int aRequestId, int aUserId, int aOrdering, boolean aIsPrimary,int aFieldId) {
						RequestUser mru = new RequestUser(ba.getSystemId(), currentRequest.getRequestId(), mailingList.getUserId(), allCCs.size()+1, false, ccField.getFieldId() ) ;
						allCCs.add(mru) ;
						
						currentRequest.setCcs(allCCs) ;
					}
					else
					{
						LOG.info("The mailing list returned was null, hence not added to the cc list") ;
					}
				}
				catch( DatabaseException de )
				{
					de.printStackTrace() ;
					LOG.info("Exception occured while finding the correct Mailing list.\n" +
							"So the mailing list will not get added to the CC list.") ;
					
				}
				
				return new RuleResult() ;
			}
			else return new RuleResult() ;
			
		} catch (IllegalStateException e) {			
			e.printStackTrace();
			return new RuleResult( true , "Exception while adding the correspondance to the Individual Filed Letters business Area", false ) ;
		} catch (DatabaseException e) {			
			e.printStackTrace();
			return new RuleResult( true , "Exception while adding the correspondance to the Individual Filed Letters business Area", false ) ;
		} catch( RuntimeException e )
		{
			e.printStackTrace() ;
			// Log.error("RunTimeException while adding request to FC") ;
			return new RuleResult( true, "Exception while adding the correspondance to the Individual Filed Letters business Area", false ) ;
		} catch (TBitsException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return new RuleResult( true, "Exception while adding the correspondance to the Individual Filed Letters business Area", false ) ;
		} catch (APIException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return new RuleResult( true, "Exception while adding the correspondance to the Individual Filed Letters business Area", false ) ;
		} catch(Exception e)
		{
			e.printStackTrace();
			return new RuleResult( true, "Exception while adding the correspondance to the Individual Filed Letters business Area", false ) ;
		}
		
	}

	// return associated mailing list ( decided from : WPCL_FIRM_NAME + correspondance type
	private User getMailingList(Request currentRequest) throws DatabaseException 
	{
		User mu = null ;
		String corName = (String)currentRequest.get(KskConstants.CORR_TYPE_FIELD_NAME) ;
		String mailingListName = KskConstants.WPCL_FIRM_NAME + "-" + corName ;
		mu = User.lookupAllByUserLogin(mailingListName) ;		
		return mu ;
	}

	public String getName() 
	{
		return "Add Correspondance to the Individual Filed Letters business Area Post Rule ";
	}

	public double getSequence() 
	{
		return 0;
	}
	
	public static void main( String argv[] ) 
	{
		
	}

}
