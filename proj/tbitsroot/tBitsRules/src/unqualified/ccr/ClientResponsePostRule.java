package ccr;
import java.sql.Connection;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.Hashtable;
import java.util.Iterator;

import transbit.tbits.api.AttachmentInfo;
import transbit.tbits.api.IPostRule;
import transbit.tbits.api.RuleResult;
import transbit.tbits.api.UpdateRequest;
import transbit.tbits.common.DatabaseException;
import transbit.tbits.common.TBitsLogger;
import transbit.tbits.common.TBitsResourceManager;
import transbit.tbits.common.Timestamp;
import transbit.tbits.domain.BusinessArea;
import transbit.tbits.domain.Field;
import transbit.tbits.domain.Request;
import transbit.tbits.domain.RequestEx;
import transbit.tbits.domain.User;
import transbit.tbits.exception.APIException;
import transbit.tbits.exception.TBitsException;
public class ClientResponsePostRule implements IPostRule {
	
	
	
	private static final String EXCEPTION_MSG = "Exception occured.";
	private static final String DESCRIPTION_VALUE = "Revised ReSubmission is Required for CCR";
	public static final String PKG_CCR = "CCR";
	/**
	 * logger for this class
	 */
	public static final TBitsLogger LOG = TBitsLogger.getLogger(PKG_CCR);
	public RuleResult execute(Connection connection, BusinessArea ba,
			Request oldRequest, Request currentRequest, int Source, User user,
			Hashtable<Field, RequestEx> extendedFields, boolean isAddRequest) {
		
		
		
			// TODO : 1. setting the due date  
			// 		  2. checking the attachments
			
		try 
			{			
				//  update and request
				if( (null != ba) && (null != ba.getSystemPrefix()) &&  ba.getSystemPrefix().equals(CCRConstants.CCREXT_SYSPREFIX) && !isAddRequest  )				
				{
					
					Hashtable<String,String> params = new Hashtable<String,String>() ;
					
					String logger = oldRequest.get(CCRConstants.CCREXT_PREPARED_BY_FIELD_NAME) ;
					String CCRFile = currentRequest.get(CCRConstants.CCREXT_CLIENT_RESP_FILE_FIELD_NAME) ;
					String CD = (String)currentRequest.myMapFieldToObjects.get(CCRConstants.CCREXT_CD_FIELD_NAME);
					String CS = (String)currentRequest.myMapFieldToObjects.get(CCRConstants.CCREXT_CORR_STATUS_FIELD_NAME);
					
					Hashtable<String,String> relatedRequests = currentRequest.getRelatedRequests() ;
					LOG.info("related requests : " + relatedRequests ) ;
					
					String relStr = (String)currentRequest.get(Field.RELATED_REQUESTS) ;
					LOG.info( "relSTr : " + relStr ) ;
					if( null == relStr || relStr.trim().equals(""))
						return new RuleResult( false, "No related Request found for this request.", false ) ;
					relStr = relStr.trim();
					String[] allRel = relStr.split(",") ;
					if(null == allRel || allRel.length == 0 ) 
						return new RuleResult( false, "No related Request found for this request.", false ) ;
					
					String firstRel = allRel[0] ;
					String [] parts = firstRel.split("#") ;
					String sysPrefix = parts[0] ;
					String reqIdStr = parts[1] ;
					
					int mappedreqid = Integer.parseInt(reqIdStr);
					int mappedreqsysid = BusinessArea.lookupBySystemPrefix(sysPrefix).getSystemId() ;
					params.put(Field.REQUEST,mappedreqid+"" );
					params.put(Field.BUSINESS_AREA, mappedreqsysid+"");
					int rootId = User.lookupAllByUserLogin(CCRConstants.TBITS_ROOT).getUserId() ;
					params.put(Field.USER, rootId +"") ;
					if(CS.equals(CCRConstants.CCREXT_CS_REVISEDSUBMISSION))
					{					
						params.put(CCRConstants.CCR_CCR_NO_FIELD_NAME," ");
						params.put(CCRConstants.CCR_CLIENT_RESP_FILE_FIELD_NAME,CCRFile);
						
						params.put(CCRConstants.CCR_CCD_FIELD_NAME,CCRConstants.CCR_CCD_PENDING);
						params.put(CCRConstants.CCR_ASSIGNEE_FIELD_NAME, logger);
						params.put(CCRConstants.CCR_CCR_FIELD_NAME, CCRConstants.CCR_CCR_RSR);
						
						Calendar cal = Calendar.getInstance() ;
						cal.add(Calendar.DAY_OF_MONTH, 3); // not taking into account whether the day is working day or not TODO: 
						Date dueDate = cal.getTime() ;
						Timestamp dueDateStamp = new Timestamp(dueDate.getTime()) ;
						String dueDateStr = dueDateStamp.toCustomFormat("yyyy-MM-dd  HH:mm:ss") ;
						LOG.info( "my due date  : " + dueDateStr ) ;
					    params.put(Field.DUE_DATE, dueDateStr ) ;
						params.put(Field.DESCRIPTION,DESCRIPTION_VALUE);
						
						
					}else if(CS.equals(CCRConstants.CCREXT_CS_CONCLUDED))						
					{
						
						params.put(CCRConstants.CCR_CLIENT_RESP_FILE_FIELD_NAME,CCRFile);
						//upload logic here
						params.put(CCRConstants.CCR_ASSIGNEE_FIELD_NAME, " ");
						
						if(CD.equals(CCRConstants.CCREXT_CD_APPROVED))
							params.put(CCRConstants.CCR_CCR_FIELD_NAME, CCRConstants.CCR_CCR_DR);
						else if(CD.equals(CCRConstants.CCREXT_CD_REJECTED))
							params.put(CCRConstants.CCR_CCR_FIELD_NAME, CCRConstants.CCR_CCR_DRCR);
						else if ( CD.equals(CCRConstants.CCREXT_CD_NOTAPPLICABLE))
						{
							// do nothing
							return new RuleResult( true, "The request in CCR will not be updated . " , true ) ;
						}
						else 
						{
							throw new TBitsException( "You must change : " + CCRConstants.CCREXT_CD_FIELD_NAME + " to some value other than : " + CCRConstants.CCREXT_CD_PENDING  );
						}
						
	               }
					
				
						UpdateRequest ur = new UpdateRequest() ;

						Request nr = ur.updateRequest(params ) ;				
						LOG.info("updated reqeust : " + nr ) ;
						return new RuleResult( true , "Client Response Post Rule Finished Successfully" ,true ) ;
				}
					
			
			} catch (IllegalStateException e) {			
				e.printStackTrace();
				return new RuleResult( false , EXCEPTION_MSG, false ) ;
			} catch (DatabaseException e) {			
				e.printStackTrace();
				return new RuleResult( false , EXCEPTION_MSG, false ) ;
			} catch (APIException e) {			
				e.printStackTrace();
				return new RuleResult( false , EXCEPTION_MSG, false ) ;
			} 
			catch (TBitsException e) {
				e.printStackTrace();
				return new RuleResult( false , EXCEPTION_MSG, false ) ;
			}
			catch( Exception e )
			{
				e.printStackTrace() ;
				// Log.error("RunTimeException while adding request to FC") ;
				return new RuleResult( false, EXCEPTION_MSG, false ) ;
			} 
			
			return new RuleResult( true ,"Rule passed", false ) ;	
	}
	
	public String getName() {
		// TODO Auto-generated method stub
		return "Post Rule for adding update into CCR" ;
	}
	
	public double getSequence() {
		// TODO Auto-generated method stub
		return 0;
	}
	
	
	

}
