package nccCorres;

import static nccCorres.GenCorresHelper.* ;

import java.io.File;
import java.sql.Connection;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.Hashtable;
import java.util.TimeZone;

import transbit.tbits.Helper.TBitsConstants;
import transbit.tbits.api.AttachmentInfo;
import transbit.tbits.api.IRule;
import transbit.tbits.api.RuleResult;
import transbit.tbits.common.TBitsLogger;
import transbit.tbits.common.Timestamp;
import transbit.tbits.common.Uploader;
import transbit.tbits.domain.BusinessArea;
import transbit.tbits.domain.Field;
import transbit.tbits.domain.Request;
import transbit.tbits.domain.RequestEx;
import transbit.tbits.domain.User;
import transbit.tbits.exception.TBitsException;

import static nccCorres.CorresConstants.* ;

public class CorresAddPreRule implements IRule 
{
	private static final String ILL_YEAR = "Year field not properly set.";
	private static final String DO_NOT_CONFORM = "Correspondance number do not conform to the Filed values.";
	private static final String NUM_EXPECTED = "Number Expected in the last token";
	private static final String ILL_FORMAT = "Illegal format of correspondence number.";
	public static final String ERR_CORR_GEN = "Exception occured : Correspondance file cannot be generated" +
						"<br> Please uncheck the generate correspondance checkbox.";	
	
	/**  
	 * 1. generate/ use the given correspondence no.
	 * 2. generate the correspondence file 
	 * 3. rename the correspondence file 
	 * 4. attach the correspondence file 
	 */
	public RuleResult execute(Connection connection, BusinessArea ba,
			Request oldRequest, Request currentRequest, int Source, User user,
			boolean isAddRequest) 
	{	
		if( (null == ba) || (ba.getSystemPrefix() == null) )  
			return new RuleResult( false , "The supplied ba was null.", true ) ;
		
		if( !ba.getSystemPrefix().equals(CorresConstants.CORR_SYSPREFIX) ) 
			return new RuleResult( true, "Skipping : " + getName() + ", because the ba is not : " + CorresConstants.CORR_SYSPREFIX , true ) ;
		

		if( TBitsConstants.SOURCE_EMAIL == Source )
		{		
			return new RuleResult(true,"Skipping : " + getName()  + ", because the appending source is EMAIL",true);
		}
		
		try
		{
		
			// TODO: after correcting the mistakes in API : check the due_date_constraint
//			Timestamp duedate = currentRequest.getDueDate() ;
//			if( duedate != null )
//			{
//				TimeZone tz = TimeZone.getTimeZone("GMT");
//				// due date should be after today
//				Calendar c = Calendar.getInstance(tz);
////				c.setTimeZone(tz);
////				c.clear();
////				c.setTime(new Date());
//				c.set(Calendar.HOUR, 0);
//				c.set(Calendar.MINUTE, 0);
//				c.set(Calendar.SECOND, 0);
//				c.set(Calendar.MILLISECOND, 0);
//				
//				System.out.println("c = " + c );
//				System.out.println("duedate : " + duedate.toCustomFormat("dd-MMM-yyyy mm.dd.yy.zzz"));
//				Calendar dd = Calendar.getInstance(tz) ;
////				dd.setTimeZone();
//				dd.clear();
//				dd.setTime(new Date(duedate.getTime()));
//				dd.set(Calendar.HOUR, 0);
//				dd.set(Calendar.MINUTE, 0);
//				dd.set(Calendar.SECOND, 0);
//				dd.set(Calendar.MILLISECOND, 0);
//				
//				System.out.println("dd = " + dd );
//				
//				if( dd.compareTo(c) < 0 )
//					return new RuleResult(false, "The " + cfdn(Field.DUE_DATE) + " should not be before today.", true);
//			}
			//currentRequest.setObject(CORR_LINKED_REQUESTS_FIELD_NAME, "CL_BILL#11"); // billing
			CorresObject co = new CorresObject(currentRequest);
			String tempFieldBill = currentRequest.get(CORR_TEMP_FIELD_BILLING); // billing
			String lreq = currentRequest.get(CORR_LINKED_REQUESTS_FIELD_NAME); // billing
			CorresObject oldCo = null ;
			int reqId = 0 ;			
			if( isAddRequest == false )
			{				
				reqId = oldRequest.getRequestId();				
				LOG.info("Trying to get the old request with id : " + reqId);
				oldCo = new CorresObject(oldRequest) ;							
			}
			
			CorresObject.validate(isAddRequest,co,oldCo);
			CorresObject.checkProtocolConstraints(isAddRequest,co,oldCo);
			CorresObject.agencySpecificConstraints(isAddRequest,co,oldCo);
			
			if( !co.generate.getName().equals(CorresConstants.CORR_GEN_DONT_GEN_ANYTHING))
			{
				String format = "pdf" ;
				
				String reportName = getReportFileName(co) ;
				//( CorresObject co, CorresObject prevCo, int requestId, String reportName, String format, int caller,  int fileType, Hashtable<String, String> params, Connection con  )
				Hashtable<String,String> reportParams = new Hashtable<String,String>() ;
				LOG.info("Generating real correspondence no.");
				String corrNo = GenCorresHelper.getCorrNo(co, GenCorresHelper.REAL, connection ) ;
				LOG.info("Got the corr. no. : " + corrNo );
				if( corrNo != null )
				{
					if( co.corrType.getName().equals(CORR_CORR_TYPE_ION))
						reportParams.put(CorresConstants.REP_ION_REF, corrNo );
					
					reportParams.put(CorresConstants.REP_REF_NO, corrNo );
				}
				else
					throw new TBitsException("Cannot generate correspondence number.") ;
				
				LOG.info("Starting to generate real correspondence file.");
				Date start = new Date() ;
				File pdfFile = GenCorresHelper.generateReport(co, oldCo, reqId, reportName, format, isAddRequest, GenCorresHelper.REAL, reportParams, connection ) ;
				Date end = new Date() ;
				LOG.info("Milliseconds taken to create the file = " + ( end.getTime() - start.getTime() ) );
				if( pdfFile == null ) 
				{				
					return new RuleResult( false , "Cannot Generate the Correspondance File.", false ) ;
				}
				else
				{						
					// upload this file
					// generate display name this file
					String displayName = corrNo + ".pdf";
					LOG.info("Uploading the corr. file : " + displayName );					
					int requestId = currentRequest.getRequestId() ;
					int actionId = currentRequest.getMaxActionId() ;
					String prefix = ba.getSystemPrefix() ;
					Uploader up = new Uploader( requestId, actionId, prefix ) ;
					AttachmentInfo atinfo = up.moveIntoRepository(pdfFile) ;
					// change display name 
					atinfo.name = displayName ;
	
					// TODO: check field null 
					ArrayList<AttachmentInfo> attachArray = new ArrayList<AttachmentInfo>() ;// arrayList is a Collection
					attachArray.add(atinfo) ;
					currentRequest.setObject(CORR_CORRESPONDANCE_FILE_FIELD_NAME, attachArray);
					// set the correspondence no. 
					currentRequest.setObject(CorresConstants.CORR_CORRESPONDENCE_NUMBER_FIELD, corrNo);
					currentRequest.setObject(CORR_TEMP_FIELD_BILLING, ""); // billing
					LOG.info("Plugin executed successfully.");
				}		
			}	
			
			return new RuleResult( true , "CorresAddPreRule finished Successfully." , true ) ;
			
		}catch( TBitsException e )
		{
			e.printStackTrace() ;
			return new RuleResult( false , e.getDescription() , false ) ;
		}
		catch( Exception e )
		{
			e.printStackTrace() ;
			return new RuleResult( false , e.getMessage() , false ) ;
		}		
	}
	

	
	public String getName() 
	{
		return getClass().getName() + " : Prerule for creating and adding the correspondance file.";
	}

	public double getSequence() 
	{
		return 0;
	}
}
