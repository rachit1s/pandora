package tatapower;

import static transbit.tbits.Helper.TBitsConstants.PKG_SCHEDULER;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Hashtable;

import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import transbit.tbits.common.DatabaseException;
import transbit.tbits.common.TBitsLogger;
import transbit.tbits.domain.BusinessArea;
import transbit.tbits.domain.Field;
import transbit.tbits.domain.Request;
import transbit.tbits.domain.User;
import transbit.tbits.exception.APIException;
import transbit.tbits.exception.TBitsException;
import transbit.tbits.search.Searcher;
/**
 * 
 * @author Nitiraj
 *
 */
/**
 * This Job takes as parameter the sysPrefix of the BA.
 * and calls the RequestUpdater to update the request 
 */
public class NonRepeatingRequestUpdaterJob implements Job {

	public static final String PARAM_SYSPREFIX = "BA" ;
	public static final String DISPLAY_NAME = "NonRepeatingRequestUpdaterJob" ;
	private static final TBitsLogger LOG = TBitsLogger.getLogger(PKG_SCHEDULER);
	public static final String PARAM_USER = "User";

	public String getDisplayName() {		
		return DISPLAY_NAME ;
	}

	/*
	public Hashtable<String, JobParameter> getParameters() throws SQLException {
		// TODO : for now it is set to text box but later it should be coverted to dropdown
		Hashtable<String, JobParameter> params = new Hashtable<String, JobParameter>();
		JobParameter param;

		param = new JobParameter();
		param.setName(PARAM_SYSPREFIX) ;
		param.setMandatory(true);
		ArrayList values = new ArrayList() ;
		ArrayList<BusinessArea> baList = null ;
		try {
			baList = BusinessArea.getAllBusinessAreas();
		} catch (DatabaseException e) {
			e.printStackTrace();
			LOG.error( "Database Error while trying to get all business area list." ) ;
			throw new SQLException( "Database Error while trying to get all business area list." ) ;
		}
		if( null != baList ) 
			for( BusinessArea ba : baList )
			{
				String baSysprefix = ba.getSystemPrefix() ;
				values.add(baSysprefix) ;
			}

		param.setType(ParameterType.Select ) ;
		param.setValues(values) ;

		params.put(PARAM_SYSPREFIX, param);	
		return params;
	}
	 */
	public boolean validateParams(Hashtable<String, String> params)
	throws IllegalArgumentException {
		String sysPrefix = params.get(PARAM_SYSPREFIX) ;
		if( null != sysPrefix && !"".equals(sysPrefix.trim()))
		{
			BusinessArea ba = null ;
			try {
				ba = BusinessArea.lookupBySystemPrefix(sysPrefix);			
				if( null == ba )
				{
					throw new IllegalArgumentException( "The business area selected (" + sysPrefix + ") does not exist.") ;
				}
			} catch (DatabaseException e) {			
				e.printStackTrace();
				throw new IllegalArgumentException( "Database exception during accessing the business area field.") ;
			}
		}
		else throw new  IllegalArgumentException( "Illegal argument in business area field." ) ;
		return true ;
	}

	public void execute(JobExecutionContext jec) throws JobExecutionException 
	{		
		// TODO : query database to get all the requests in my business area
		// Get the JobDetail object.
		JobDetail jd = jec.getJobDetail();

		// Read the properties of the Job from the JobDetail object.
		JobDataMap jdm      = jd.getJobDataMap();
		String mySysPrefix = jdm.getString(PARAM_SYSPREFIX);
		BusinessArea ba = null ;
		if( null != mySysPrefix && !"".equals(mySysPrefix.trim()))
		{			
			try {
				ba = BusinessArea.lookupBySystemPrefix(mySysPrefix);			
				if( null == ba )
				{
					LOG.error("There is no Business Area with SystemPrefix = " + mySysPrefix + ". Contact your administrator." ) ;
					throw new JobExecutionException("There is no Business Area with SystemPrefix = " + mySysPrefix + ". Contact your administrator." ) ;
				}
			} catch (DatabaseException e) {			
				e.printStackTrace();
				throw new JobExecutionException( "Database exception during accessing the business area with sysPrefix = " + mySysPrefix ) ;
			}
		}
		else throw new  JobExecutionException( "Illegal argument in business area field." ) ;


		int myBaID = ba.getSystemId() ;
		// using the root for the user to search for requests
		// using the root for the user to search for requests
		String user = jdm.getString(PARAM_USER) ;

		User root;
		try {
			root = User.lookupAllByUserLogin(user);
		} catch (DatabaseException e1) {
			e1.printStackTrace();
			throw new JobExecutionException( "Database exception while accessing the field : " + PARAM_USER ) ;
		}

		String root_id_str = root.getUserLogin();
		String myDQL = "parent:0" ;
		Searcher searcher = new Searcher( myBaID, root.getUserId() , myDQL );

		try
		{	       
			searcher.search();
		}
		catch( Exception ex ) 
		{
			ex.printStackTrace() ;
			LOG.error( "Error encountered during searching for DQL = " + myDQL ) ;
			return ;
		}

		ArrayList<String> requestIDs = searcher.getAllRequestIdList();       

		for( String nextRequestID : requestIDs )
		{
			try
			{
				// obtain the request object        		
				// the pair is ( sys_id + "_" + request_id ) and sys_id being an integer will never contain a "_"
				// but I recommend returning JSON string in searcher rather than this type string
				nextRequestID = nextRequestID.substring(nextRequestID.indexOf("_")+1);
				//       		LOG.info("NITIRAJ : Checking request id = " + nextRequestID ) ;
				int nextReqID = Integer.parseInt( nextRequestID ) ;
				Request request = Request.lookupBySystemIdAndRequestId(myBaID, nextReqID );

				NonRepeatingRequestUpdater.updateThisRequest( request, root_id_str ) ;
			}
			catch( NumberFormatException nfe )
			{
				nfe.printStackTrace() ;
				LOG.error( "NuberFormat exception while accessing the request with reqeust id = " + nextRequestID ) ;
			} catch (DatabaseException e) {        		
				e.printStackTrace();
				//	LOG.error( "Database exception while retriving the Reqeust object with request ID = " + nextReqID ) ;
			} catch (IllegalStateException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (TBitsException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (APIException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}        

	}


	/**
	 * @param args
	 */
	public static void main(String[] args) {
		try
		{
			int myBaID = BusinessArea.lookupBySystemPrefix("CNBA").getSystemId() ;
			// using the root for the user to search for requests     
			User rootID;			
			rootID = User.lookupAllByUserLogin("expediter");

			String root_id_str = rootID.getUserLogin();

			String myDQL = "parent:0" ;
			Searcher searcher = new Searcher( myBaID, rootID.getUserId() , myDQL );

			searcher.search();		    

			ArrayList<String> requestIDs = searcher.getAllRequestIdList();       

			for( String nextRequestID : requestIDs )
			{

				// obtain the request object        		
				// the pair is ( sys_id + "_" + request_id ) and sys_id being an integer will never contain a "_"
				// but I recommend returning JSON string in searcher rather than this type string        		
				nextRequestID = nextRequestID.substring(nextRequestID.indexOf("_")+1);
				//       		LOG.info("NITIRAJ : Checking request id = " + nextRequestID ) ;
				int nextReqID = Integer.parseInt( nextRequestID ) ;
				Request request = Request.lookupBySystemIdAndRequestId(myBaID, nextReqID );		        	
				RequestUpdater.updateThisRequest( request, root_id_str ) ;	        	
			}
		}
		catch( Exception ex ) 
		{
			ex.printStackTrace() ;
			//			LOG.error("NITIRAJ"); 
		} catch (APIException e) 
		{
			e.printStackTrace();
			//			LOG.error("NITIRAJ"); 
		}

	}

}
