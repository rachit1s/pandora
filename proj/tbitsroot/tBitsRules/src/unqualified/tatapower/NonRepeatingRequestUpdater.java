package tatapower;

import java.util.Hashtable;
import transbit.tbits.Helper.TBitsConstants;
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
 * It takes a requests and does following things
 * 	 the Request is assumed to be non-repeatative *	
 * 		if today is the day for updating then it updates this request
 */
public class NonRepeatingRequestUpdater 
{
	private static final TBitsLogger LOG = TBitsLogger.getLogger("TataPower");
	
	public static void updateThisRequest( Request request, String user ) throws IllegalStateException, DatabaseException, TBitsException, APIException 
	{		
		Timestamp fad = TataJobUtils.getExDate(TataJobUtils.FIRST_ASSIGN_DATE_FIELD_NAME, request) ;
		
		if( null == fad )
			throw new IllegalStateException( "FAD is null." ) ;
		// just process each request as non-repeating    	
		if( fad.isSameDate(new Timestamp()))
		{
//			LOG.info("NITIRAJ : RequestUpdater : fad(" + TataJobUtils.inputDateFormat(fad) + " == today(" + TataJobUtils.inputDateFormat(new Timestamp())+ ". so updating request.") ;
			// update this request 
			Hashtable<String,String> updateTable = new Hashtable<String,String>() ;
			//TODO: Have to replace with user_login instead of the user_id
			updateTable.put(Field.USER, user) ;			
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
//			LOG.info("NITIRAJ : RequestUpdater : fad(" + TataJobUtils.inputDateFormat(fad) + " != today(" + TataJobUtils.inputDateFormat(new Timestamp()) ) ;
		}		
	}

}
