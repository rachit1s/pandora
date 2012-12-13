package tatapower;

import java.util.Calendar;
import java.util.Hashtable;
import transbit.tbits.Helper.TBitsConstants;
import transbit.tbits.api.AddRequest;
import transbit.tbits.api.UpdateRequest;
import transbit.tbits.common.DatabaseException;
import transbit.tbits.common.TBitsLogger;
import transbit.tbits.common.Timestamp;
import transbit.tbits.domain.Field;
import transbit.tbits.domain.Request;
import transbit.tbits.exception.APIException;
import transbit.tbits.exception.TBitsException;

/**
 * 
 * @author Nitiraj
 *
 */
/**
 * this class is an allied class to update the request according to the Tata power needs
 * this class does all the logic behind the updating
 * 
 * It searches for all the requests with null parent in this BA and does one of the following things
 * 	1. if the Request is marked as Non-repetative
 * 		then it checks if the request's first update has been done or not
 * 		if it is updated, then it does nothing 
 * 		if no then it checks if today is the day for updating 
 * 			if yes then it updates this request and mark it updated
 * 
 * 2. If the Request is marked as repetative
 * 	  (a) then it checks if its first update has been done or not 
 * 	  	  if not then it checks if it should be updated and does accordingly
 * 	  (b) it checks if today is the day when a new subrequest of this should be
 * 		  formed if yes then it create a new subrequest of this request and 
 * 		  mark this new request as non repetative
 * 
 */
public class RequestUpdater 
{
	/**
	 * A copy of this function is in ComplianceTaskRequestUpdateHandler Rule so change might be required there also
	 * @param ts
	 * @param repeatType
	 * @return
	 */
	static Timestamp adjustDate(Timestamp ts, String repeatType ) {

		Calendar cal = Calendar.getInstance() ;
		cal.clear() ;
		cal.setTime( ts ) ;
		
		if( repeatType.equalsIgnoreCase(TataJobUtils.MONTHLY))
			cal.add(Calendar.MONTH, 1) ;
		else if( repeatType.equalsIgnoreCase(TataJobUtils.QUATERLY)) 
			cal.add(Calendar.MONTH, 3) ;
		else if ( repeatType.equalsIgnoreCase(TataJobUtils.HALF_YEARLY))
			cal.add(Calendar.MONTH, 6) ;
		else if ( repeatType.equalsIgnoreCase(TataJobUtils.YEARLY))
			cal.add(Calendar.YEAR, 1) ;
//		System.out.println( "final cal = " + cal.getTime() ) ;
		
		Timestamp nts = new Timestamp( cal.getTime().getTime()) ;		
		
//		String strDate = nts.toCustomFormat("yyyy-MM-dd 00:00:00");					
        
		return nts ;
	}
	
	private static void copyHashTable(
			Hashtable<String, String> src,
			Hashtable<String, String> dest) 
	{
		java.util.Enumeration<String> keys = src.keys() ;
		while( keys.hasMoreElements() ) 
		{
			String key = keys.nextElement() ;
			String value = src.get(key) ;
			
			dest.put( key, value ) ;
		}
	}
	
	static void addSubRequest(Request request, Timestamp ts, String userLogin ) throws APIException 
	{	
		// create the new hashtable
		Hashtable<String,String> subReqTable = new Hashtable<String,String>() ;
		
		// copy all values
		copyHashTable( request.myMapFieldToValues, subReqTable ) ;
		
		// add and remove some fields
		subReqTable.remove(Field.REQUEST) ;
		subReqTable.put(Field.USER, userLogin) ;
		
		subReqTable.remove(Field.ASSIGNEE) ;
		subReqTable.put(Field.ASSIGNEE, request.get(TataJobUtils.FIRST_ASSIGNEE_FIELD_NAME)) ;
		
		// calculate new due date 
		String span_str = request.get(TataJobUtils.SPAN_FIELD_NAME) ;
		if( null == span_str || "".equals(span_str))
			span_str = "0" ;
		int span = Integer.parseInt(span_str); 
		
		Timestamp due_date = TataJobUtils.addDays( ts, span ) ;
		String due_date_str = TataJobUtils.inputDueDateFormat( due_date ) ;		
		subReqTable.remove(Field.DUE_DATE); 		
		subReqTable.put(Field.DUE_DATE, due_date_str ) ;
		
		// set parent
		subReqTable.put( Field.PARENT_REQUEST_ID, request.getRequestId()+""); 

		subReqTable.remove(TataJobUtils.FIRST_ASSIGNEE_FIELD_NAME) ;
		subReqTable.remove(TataJobUtils.NEXT_REPEAT_DATE_FIELD_NAME) ;	
		
		
		subReqTable.remove(TataJobUtils.IS_REPEATING_FIELD_NAME) ;
		subReqTable.put( TataJobUtils.IS_REPEATING_FIELD_NAME, "false" ) ;
		
		subReqTable.remove(TataJobUtils.SPAN_FIELD_NAME) ;
		subReqTable.remove(TataJobUtils.FIRST_ASSIGN_DATE_FIELD_NAME) ;		
		
		subReqTable.remove( Field.LASTUPDATED_DATE ) ;
		subReqTable.remove(Field.LOGGED_DATE ) ;
		// remove the summary as we are adding messages in summary
		subReqTable.remove(Field.SUMMARY) ;
		// print
		TataJobUtils.printTable(subReqTable); 
		
		AddRequest addReq = new AddRequest() ;
		addReq.setSource(TBitsConstants.SOURCE_CMDLINE);
		Request req = addReq.addRequest(subReqTable) ;		
	}

	public static void updateThisRequest( Request request, String root_id ) throws IllegalStateException, DatabaseException, TBitsException, APIException 
	{
		// check if the request is parent request
		if( 0 == request.getParentRequestId() )
		{   
			Timestamp fad = TataJobUtils.getExDate(TataJobUtils.FIRST_ASSIGN_DATE_FIELD_NAME, request) ;
			
			if( null == fad )
				throw new IllegalStateException( "FAD is null." ) ;
			
			// check if repeating ? 
			String is_repeating = request.get(TataJobUtils.IS_REPEATING_FIELD_NAME) ;
			if( null != is_repeating && "true".equalsIgnoreCase(is_repeating)  )
			{
				// this is a repeating request
				// if NRD == null add subrequest according to the FAD
		//		RequestUpdater.LOG.info("NITIRAJ : RequestUpdater : this is repeating request.") ;
				Timestamp nrd = TataJobUtils.getExDate( TataJobUtils.NEXT_REPEAT_DATE_FIELD_NAME, request ) ;  			
				
				if( null == nrd )
				{    
		//			RequestUpdater.LOG.info("NITIRAJ : RequestUpdater : nrd == null") ;
					if( fad.isSameDate(new Timestamp()))
					{
	//					RequestUpdater.LOG.info("NITIRAJ : RequestUpdater : fad(" + TataJobUtils.inputDateFormat(fad) + " == today(" + TataJobUtils.inputDateFormat(new Timestamp())+ ". so adding sub-request.") ;
						addSubRequest( request, fad, root_id ) ;
						
	    				// get repeatType 
	    				String repeatType = TataJobUtils.getExType( TataJobUtils.REPEAT_TYPE_FIELD_NAME, request ).getName() ;
	    				Timestamp newNRD = adjustDate( fad, repeatType ) ;
	    				
	    				// now update this request to include the new newNRD
	    				Hashtable<String,String> updateTable = new Hashtable<String,String>() ;
	    				
	    				updateTable.put( Field.REQUEST, request.getRequestId()+"") ;
	    				updateTable.put(Field.BUSINESS_AREA, request.getSystemId()+"") ;
	    				updateTable.put(Field.USER, root_id) ;
	    				
	    				// set summary to inform
	    				updateTable.put( Field.SUMMARY, "A Sub-Request has been added.") ;
	    				
	    				String nrdStr = newNRD.toCustomFormat("yyyy-MM-dd 00:00:00") ;        				
	    				updateTable.put( TataJobUtils.NEXT_REPEAT_DATE_FIELD_NAME, nrdStr ) ;
	    				
	 //   				RequestUpdater.LOG.info("NITIRAJ : RequestUpdater : updating the repeat date. updatetable=\n") ;
	    				
	    				// print
	    				TataJobUtils.printTable(updateTable); 
	    				
	    				UpdateRequest upReq = new UpdateRequest() ;
	    				upReq.setSource(TBitsConstants.SOURCE_CMDLINE);
	    				Request updatedRequest = upReq.updateRequest(updateTable) ;        				
					}
					else 
					{
						// do nothing
		//				RequestUpdater.LOG.info("NITIRAJ : RequestUpdater : fad(" + TataJobUtils.inputDateFormat(fad) + " != today(" + TataJobUtils.inputDateFormat(new Timestamp()) ) ;
					}    				
				}
				else
				{
		//			RequestUpdater.LOG.info("NITIRAJ : RequestUpdater : nrd NOT NULL ") ;
					// update according to the nrd
					if( nrd.isSameDate( new Timestamp() )) 
					{
		//				RequestUpdater.LOG.info("NITIRAJ : RequestUpdater : NRD(" + TataJobUtils.inputDateFormat(nrd) + " == today(" + TataJobUtils.inputDateFormat(new Timestamp()) + ". so adding sub request.") ;
						addSubRequest( request, nrd, root_id ) ;
						
						// get repeatType
						String repeatType = TataJobUtils.getExType(TataJobUtils.REPEAT_TYPE_FIELD_NAME, request).getName() ;
						
						Timestamp newNRD = adjustDate( nrd, repeatType ) ;
	    				
	    				// now update this request to include the new newNRD
	    				Hashtable<String,String> updateTable = new Hashtable<String,String>() ;
	    				
	    				updateTable.put( Field.REQUEST, request.getRequestId()+"") ;
	    				updateTable.put(Field.BUSINESS_AREA, request.getSystemId()+"") ;
	    				updateTable.put(Field.USER, root_id) ;
	    				
	    				// set summary to inform
	    				updateTable.put( Field.SUMMARY, request.getSummary()+ "\nA Sub-Request has been added on :" + TataJobUtils.inputDateFormat(new Timestamp())) ;
	    				
	    				String nrdStr = TataJobUtils.inputDateFormat(newNRD) ;        				
	    				updateTable.put( TataJobUtils.NEXT_REPEAT_DATE_FIELD_NAME, nrdStr ) ;
	    				
	    //				RequestUpdater.LOG.info("NITIRAJ : RequestUpdater : updating the repeat date. updatetable=\n") ;
	    				// print
	    				TataJobUtils.printTable(updateTable); 
	    				
	    				UpdateRequest upReq = new UpdateRequest() ;
	    				upReq.setSource(TBitsConstants.SOURCE_CMDLINE);
	    				Request updatedRequest = upReq.updateRequest(updateTable) ;    					
					}
					else
					{
						// do nothing 
			//			RequestUpdater.LOG.info("NITIRAJ : RequestUpdater : nrd(" + TataJobUtils.inputDateFormat(nrd) + " != today(" + TataJobUtils.inputDateFormat(new Timestamp() ) ) ;
					}
				}
			}
			else // it is not repeating  
			{    
		//		RequestUpdater.LOG.info("NITIRAJ : RequestUpdater : this is NON Repeating request.") ;
				if( fad.isSameDate(new Timestamp()))
				{
		//			RequestUpdater.LOG.info("NITIRAJ : RequestUpdater : fad(" + TataJobUtils.inputDateFormat(fad) + " == today(" + TataJobUtils.inputDateFormat(new Timestamp())+ ". so updating request.") ;
					// update this request 
					Hashtable<String,String> updateTable = new Hashtable<String,String>() ;
					updateTable.put(Field.USER, root_id) ;
					updateTable.put(Field.REQUEST, request.getRequestId()+"") ;
					updateTable.put(Field.BUSINESS_AREA, request.getSystemId()+"") ;
					
					// change assignee
					updateTable.put(Field.ASSIGNEE, request.get(TataJobUtils.FIRST_ASSIGNEE_FIELD_NAME)) ;
					
					// change duedate
					String span_str = request.get(TataJobUtils.SPAN_FIELD_NAME) ;
					if( null == span_str || "".equals(span_str))
						span_str = "0" ;
					
					int span = Integer.parseInt(span_str); 
					Timestamp due_date = TataJobUtils.addDays( fad, span ) ;
					String due_date_str = TataJobUtils.inputDueDateFormat( due_date ) ;		
					updateTable.put(Field.DUE_DATE, due_date_str ) ;    				
					
					// print
					TataJobUtils.printTable(updateTable); 
					
					UpdateRequest upReq = new UpdateRequest() ;
					upReq.setSource(TBitsConstants.SOURCE_CMDLINE);
					Request updatedRequest = upReq.updateRequest(updateTable) ;
				}
				else
				{
					// do nothing
		//			RequestUpdater.LOG.info("NITIRAJ : RequestUpdater : fad(" + TataJobUtils.inputDateFormat(fad) + " != today(" + TataJobUtils.inputDateFormat(new Timestamp()) ) ;
				}
			}
		}
		else{
			// do nothing for non-parent
	//		RequestUpdater.LOG.info( "THIS IS NOT a parent request." ) ;
		}
	}

	private static final TBitsLogger LOG = TBitsLogger.getLogger("TataPower");

}
