package kskCorr.rule;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Hashtable;
import java.util.Iterator;

import corrGeneric.com.tbitsGlobal.server.util.Utility;
import corrGeneric.com.tbitsGlobal.shared.objects.GenericParams;

import kskCorr.others.KskConst;

import transbit.tbits.Helper.TBitsConstants;
import transbit.tbits.api.AddRequest;
import transbit.tbits.api.IPostRule;
import transbit.tbits.api.RuleResult;
import transbit.tbits.api.UpdateRequest;
import transbit.tbits.common.DataSourcePool;
import transbit.tbits.common.DatabaseException;
import transbit.tbits.common.PropertiesHandler;
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
		// 		  1. (if required)send the notification to mailing list 
						// algo :add the mailing list (user) to the cc field of the 

		try 
		{
			String corrBAs = PropertiesHandler.getProperty(GenericParams.CorrBaList);
			ArrayList<String> corrBAList = Utility.splitToArrayList(corrBAs);
			// both for update and add request
			if( (null != ba) && (null != ba.getSystemPrefix()) &&  null != corrBAList && corrBAList.contains(ba.getSystemPrefix())  )				
			{
				// task 1.
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
			
		} catch (Exception e) {			
			e.printStackTrace();
			return new RuleResult( true , "Exception while adding mailing list to the request", false ) ;
		} 
	}

	// return associated mailing list ( decided from : WPCL_FIRM_NAME + correspondance type
	private User getMailingList(Request currentRequest) throws DatabaseException 
	{
		User mu = null ;
		String corName = (String)currentRequest.get(KskConst.CORR_TYPE_FIELD_NAME) ;
		String mailingListName = KskConst.WPCL_FIRM_NAME + "-" + corName ;
		mu = User.lookupAllByUserLogin(mailingListName) ;		
		return mu ;
	}

	public String getName() 
	{
		return "Add mailing list to CC so that mails go to the people in the mailing list.";
	}

	public double getSequence() 
	{
		return 0;
	}
}
