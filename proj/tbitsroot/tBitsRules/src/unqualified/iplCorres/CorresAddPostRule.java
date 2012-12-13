package iplCorres;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;

import transbit.tbits.api.AddRequest;
import transbit.tbits.api.IPostRule;
import transbit.tbits.api.RuleResult;
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
	
	public static final String PKG_KSK = "IPLCORRES";
	/**
	 * logger for this class
	 */
	public static final TBitsLogger LOG = TBitsLogger.getLogger(PKG_KSK);
	
	
	public Type getCategoryType( int sys_id ,String firm, String fieldName ) throws DatabaseException 
    {
    	// get the location code 
    	// get the user_firm
    	//int loc = Integer.parseInt(location) ;
    	ArrayList<Type> tl = Type.lookupAllBySystemIdAndFieldName(sys_id, fieldName ) ;
    
    	for( Iterator<Type> it = tl.iterator() ; it.hasNext() ; )
    	{
    		Type type = it.next() ;
    		String name = type.getName() ;
    		if( name.contains(firm) )
    			return type ;    	
    	}
    	
    	return null ;
    }
	
	public RuleResult execute(Connection connection, BusinessArea ba,
			Request oldRequest, Request currentRequest, int Source, User user,
			Hashtable<Field, RequestEx> extendedFields, boolean isAddRequest) 
	{
		// TODO : 1. send a new request to the FC : done  
		// 		  2. (if required)send the notification to mailing list 
						// algo :add the mailing list (user) to the cc field of the 

		try 
		{			
			// both for update and add request
			if( (null != ba) && (null != ba.getSystemPrefix()) &&  ba.getSystemPrefix().equals(IPLConstants.CORR_SYSPREFIX)  )				
			{
				Boolean genCor =  currentRequest.getExBoolean(IPLConstants.CORR_GENERATE_CORRESPONDANCE_FIELD_NAME) ;
				if( null == genCor || !genCor )
					return new RuleResult( true ," Skipping " + getName(), true );
						
				// task 1.
				Hashtable<String,String> params = new Hashtable<String,String>() ;
							
				String logger = currentRequest.get(IPLConstants.CORR_LOGGER_FIELD_NAME) ;
				String assignee = currentRequest.get(IPLConstants.CORR_ASSIGNEE_FIELD_NAME) ;
				String subscribers = currentRequest.get(IPLConstants.CORR_SUBSCRIBER_FIELD_NAME) ;				
				String corrInit = (String) currentRequest.myMapFieldToObjects.get(IPLConstants.CORR_CORRESPONDANCE_INITIATOR_FIELD_NAME) ;
				String type = (String) currentRequest.myMapFieldToObjects.get(IPLConstants.CORR_TYPE_FIELD_NAME) ;				
				String subject = currentRequest.getSubject() ;
				String corrNo = currentRequest.getExString(IPLConstants.CORR_CORRESPONDANCE_NUMBER_FIELD) ;
				String corrFile = currentRequest.getExString(IPLConstants.CORR_CORRESPONDANCE_FILE_FIELD_NAME) ;
				String attachFile = currentRequest.get(Field.ATTACHMENTS) ;
				
				params.put(IPLConstants.FC_LOGGER_FIELD_NAME, logger ) ;
				params.put(IPLConstants.FC_ASSIGNEE_FIELD_NAME, assignee ) ;
				if( null != subscribers )
					params.put(IPLConstants.FC_SUBSCRIBER_FIELD_NAME, subscribers) ;
				if( null != corrInit )
					params.put(IPLConstants.FC_CORRESPONDANCE_INITIATOR_FIELD_NAME, corrInit ) ;
				if( null != type ) 
					params.put(IPLConstants.FC_TYPE_FIELD_NAME, type) ;
				if( null != subject ) 
					params.put(Field.SUBJECT, subject ) ;
				if( null != corrNo )
					params.put(IPLConstants.FC_CORRESPONDANCE_NUMBER_FIELD, corrNo ) ;
				if( null != corrFile )
					params.put(IPLConstants.FC_CORRESPONDANCE_FILE_FIELD_NAME, corrFile ) ;
				if( null != attachFile )
					params.put(Field.ATTACHMENTS, attachFile ) ;
				
//				String description = "This request was automatically generated in response to Add / Update action on the request : " ;
//				description += ba.getSystemPrefix()+"#"+currentRequest.getRequestId() ;
//				params.put(Field.DESCRIPTION, description ) ;
				
				params.put(Field.BUSINESS_AREA, BusinessArea.lookupBySystemPrefix(IPLConstants.FC_SYSPREFIX).getSystemId()+"") ;
				params.put(Field.USER, User.lookupByUserLogin(IPLConstants.ROOT_USER).getUserId()+"") ;
				
				// decide on the basis of assignee what should be the value of Recepient (severity_id) in FC
				// get the first assignee
				String assignee0 = Utilities.toArrayList(assignee).get(0) ;
				int assignee0_id = User.lookupAllByUserLogin(assignee0).getUserId() ;
				try
				{
					Hashtable<String,String> ass_info = UserInfoManager.getUserInfo(assignee0_id) ;
					String ass_firm = ass_info.get(UserInfoManager.FIRM) ;
//					String ass_loc = ass_info.get(UserInfoManager.LOCATION ) ;
					
					Type ass_cat = getCategoryType( BusinessArea.lookupBySystemPrefix(IPLConstants.FC_SYSPREFIX).getSystemId(), ass_firm, IPLConstants.FC_RECEPIENT_FIELD_NAME ) ;
					
					if( ass_cat != null )
						params.put(IPLConstants.FC_RECEPIENT_FIELD_NAME, ass_cat.getName() ) ;
				}
				catch( IllegalArgumentException iae )
				{
					//iae.printStackTrace() ;
					LOG.error("The assignee logger : " + assignee0 + " does not have a database entry into the ksk_user_info.\n" +
							"The recepient field for this request will not be correct.") ;
				}
				catch( TBitsException iae )
				{
					//iae.printStackTrace() ;
					LOG.error("Exception while accessing the assignee : " + assignee0 + " info in ksk_user_info.\n" +
							"The recepient field for this request will not be correct.") ;
				}
				catch( DatabaseException iae )
				{
					//iae.printStackTrace() ;
					LOG.error("Exception while accessing the assignee : " + assignee0 + " info in ksk_user_info.\n" +
							"The recepient field for this request will not be correct.") ;
				}
				
				AddRequest ar = new AddRequest() ;
				
				TBitsResourceManager trmgr = new TBitsResourceManager();
				Request nR  = ar.addRequest(connection, trmgr, params) ;
				trmgr.commit();
				
				// task 2.
//				try
//				{ 
//					User mailingList = getMailingList(currentRequest) ;
//					if( mailingList != null )
//					{
//						ArrayList<RequestUser> allCCs = currentRequest.getCcs() ;
//					
//						//   public RequestUser(int aSystemId, int aRequestId, int aUserTypeId, int aUserId, int aOrdering, boolean aIsPrimary) {
//						RequestUser mru = new RequestUser(ba.getSystemId(), currentRequest.getRequestId(), UserType.CC, mailingList.getUserId(), allCCs.size()+1, false ) ;
//						allCCs.add(mru) ;
//						
//						currentRequest.setCcs(allCCs) ;
//					}
//					else
//					{
//						LOG.info("The mailing list returned was null, hence not added to the cc list") ;
//					}
//				}
//				catch( DatabaseException de )
//				{
//					de.printStackTrace() ;
//					LOG.info("Exception occured while finding the correct Mailing list.\n" +
//							"So the mailing list will not get added to the CC list.") ;
//					
//				}
//				
				return new RuleResult() ;
			}
			else return new RuleResult() ;
			
		} catch (IllegalStateException e) {			
			e.printStackTrace();
			return new RuleResult( true , "Exception while adding the correspondance to the Individual Filed Letters business Area", false ) ;
		} catch (DatabaseException e) {			
			e.printStackTrace();
			return new RuleResult( true , "Exception while adding the correspondance to the Individual Filed Letters business Area", false ) ;
		} catch (APIException e) {			
			e.printStackTrace();
			return new RuleResult( true , "Exception while adding the correspondance to the Individual Filed Letters business Area", false ) ;
		} 
		catch( RuntimeException e )
		{
			e.printStackTrace() ;
			// Log.error("RunTimeException while adding request to FC") ;
			return new RuleResult( true, "Exception while adding the correspondance to the Individual Filed Letters business Area", false ) ;
		}
		
	}

	// return associated mailing list ( decided from : WPCL_FIRM_NAME + correspondance type
//	private User getMailingList(Request currentRequest) throws DatabaseException 
//	{
//		User mu = null ;
//		String corName = (String)currentRequest.myMapFieldToObjects.get(KskConstants.CORR_TYPE_FIELD_NAME) ;
//		String mailingListName = KskConstants.WPCL_FIRM_NAME + "-" + corName ;
//		mu = User.lookupAllByUserLogin(mailingListName) ;		
//		return mu ;
//	}

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
