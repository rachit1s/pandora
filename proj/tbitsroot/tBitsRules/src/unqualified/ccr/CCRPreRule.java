package ccr;

import static ccr.CCRConstants.CCREXT_CLIENT_DEC_FIELD_NAME;
import static ccr.CCRConstants.CCREXT_CORR_NO_FIELD_NAME;
import static ccr.CCRConstants.CCREXT_CORR_STATUS_FIELD_NAME;
import static ccr.CCRConstants.CCREXT_PROJECT_FIELD_NAME;
import static ccr.CCRConstants.CCREXT_PROJECT_NO_FIELD_NAME;
import static ccr.CCRConstants.CCREXT_SMB_SUBMIT_FIELD_NAME;
import static ccr.CCRConstants.CCREXT_SYSPREFIX;
import static ccr.CCRConstants.CCR_CCD_APPROVED;
import static ccr.CCRConstants.CCR_CCD_FIELD_NAME;
import static ccr.CCRConstants.CCR_CCD_PENDING;
import static ccr.CCRConstants.CCR_CCD_RECTIFY;
import static ccr.CCRConstants.CCR_CCD_REJECTED;
import static ccr.CCRConstants.CCR_CCR_CCR;
import static ccr.CCRConstants.CCR_CCR_DR;
import static ccr.CCRConstants.CCR_CCR_DRCR;
import static ccr.CCRConstants.CCR_CCR_EPMR;
import static ccr.CCRConstants.CCR_CCR_FIELD_NAME;
import static ccr.CCRConstants.CCR_CCR_PCCD;
import static ccr.CCRConstants.CCR_CCR_PEPMD;
import static ccr.CCRConstants.CCR_CCR_PFR;
import static ccr.CCRConstants.CCR_CCR_RSR;
import static ccr.CCRConstants.CCR_CCR_STCPR;
import static ccr.CCRConstants.CCR_CC_MAP_TYPE_NAME;
import static ccr.CCRConstants.CCR_CONTRACT_DOC_FIELD_NAME;
import static ccr.CCRConstants.CCR_CON_CLA_REQ_FIELD_NAME;
import static ccr.CCRConstants.CCR_CON_REQ_FIELD_NAME;
import static ccr.CCRConstants.CCR_EPMD_APPROVED;
import static ccr.CCRConstants.CCR_EPMD_FIELD_NAME;
import static ccr.CCRConstants.CCR_EPMD_PENDING;
import static ccr.CCRConstants.CCR_EPMD_RECTIFY;
import static ccr.CCRConstants.CCR_EPMD_REJECTED;
import static ccr.CCRConstants.CCR_LOCATION_FIELD_NAME;
import static ccr.CCRConstants.CCR_MAP_TYPE_NAME;
import static ccr.CCRConstants.CCR_PROJECT_FIELD_NAME;
import static ccr.CCRConstants.CCR_PROJECT_NO_FIELD_NAME;
import static ccr.CCRConstants.CCR_SECTION_FIELD_NAME;
import static ccr.CCRConstants.CCR_SYSPREFIX;
import static ccr.CCRConstants.RPT_APPROVEDBY;
import static ccr.CCRConstants.RPT_APPROVED_DATE;
import static ccr.CCRConstants.RPT_APPROVED_NAME;
import static ccr.CCRConstants.RPT_AUTHORISED_DATE;
import static ccr.CCRConstants.RPT_AUTHORISED_NAME;
import static ccr.CCRConstants.RPT_AUTHORIZEDBY;
import static ccr.CCRConstants.RPT_CCRNO;
import static ccr.CCRConstants.RPT_CONTRACT_CLARIFICATION_REQUEST;
import static ccr.CCRConstants.RPT_CONTRACT_DOCUMENT;
import static ccr.CCRConstants.RPT_CONTRACT_REQUIREMENT;
import static ccr.CCRConstants.RPT_LOCATION;
import static ccr.CCRConstants.RPT_PREPAREDBY;
import static ccr.CCRConstants.RPT_PROJECT;
import static ccr.CCRConstants.RPT_PROJECTNO;
import static ccr.CCRConstants.RPT_SECTION;
import static ccr.CCRConstants.RPT_SUBJECT;

import java.io.File;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;

import org.eclipse.birt.report.engine.api.EngineConfig;
import org.eclipse.birt.report.engine.api.EngineException;
import org.eclipse.birt.report.engine.api.IReportDocument;
import org.eclipse.birt.report.engine.api.IReportEngine;
import org.eclipse.birt.report.engine.api.IReportRunnable;
import org.eclipse.birt.report.model.api.activity.SemanticException;

import transbit.tbits.api.AddRequest;
import transbit.tbits.api.AttachmentInfo;
import transbit.tbits.api.IRule;
import transbit.tbits.api.RuleResult;
import transbit.tbits.common.Configuration;
import transbit.tbits.common.DataSourcePool;
import transbit.tbits.common.DatabaseException;
import transbit.tbits.common.TBitsLogger;
import transbit.tbits.common.TBitsResourceManager;
import transbit.tbits.common.Timestamp;
import transbit.tbits.common.Uploader;
import transbit.tbits.domain.Action;
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
import transbit.tbits.report.TBitsReportEngine;

public class CCRPreRule implements IRule {

	final private static String NOT_ALLOWED = "You are not allowed to update this request.";
	final public static String name = "CCR logic for adding/updating request." ;
	final private static TBitsLogger LOG = TBitsLogger.getLogger("ccr");
	
	public RuleResult execute(Connection connection, BusinessArea ba,
			Request oldRequest, Request currentRequest, int Source, User user,
			Hashtable<Field, RequestEx> extendedFields, boolean isAddRequest,
			Collection<AttachmentInfo> attachments) 
	{
		if( null == ba )
			return new RuleResult( true, name + " : ba passed was null." , false ) ;
		
		String sysPrefix = ba.getSystemPrefix() ;
		
		if( ! sysPrefix.equals(CCR_SYSPREFIX))
			return new RuleResult(true, name + " : bypassing as the ba is not " + CCR_SYSPREFIX , true ) ;
		
		String oldCcrName = null ;
		String oldCcdName = null ;
		String oldEpmdName = null ;
		String ccrName = null ;
		String ccdName = null ;
		String epmdName = null ;
		
		User loginUser = null ;
		User oldLogger = null ;
		User oldAssignee = null ;
		
		User currLogger = null ;
		
		// get the CCR_Status, CCD and EPMD		
		if( ! isAddRequest ) // current request is an update
		{			
			oldCcrName = (String)oldRequest.myMapFieldToObjects.get(CCR_CCR_FIELD_NAME) ;
			oldCcdName = (String)oldRequest.myMapFieldToObjects.get(CCR_CCD_FIELD_NAME ) ;
			oldEpmdName = (String)oldRequest.myMapFieldToObjects.get(CCR_EPMD_FIELD_NAME ) ;
			
			if( null == oldCcrName || oldCcrName.trim().equals("") ||
				null == oldCcdName || oldCcdName.trim().equals("") ||
				null == oldEpmdName || oldEpmdName.trim().equals("") 
			  ) 
			{
				return new RuleResult(false, name + " : illegal values of fields in previous request.", false ) ;
			}
			
			ArrayList<RequestUser> oll = oldRequest.getLoggers() ;
			ArrayList<RequestUser> oal = oldRequest.getAssignees() ;
			
 			if( null == oll || oll.size() == 0 )
 				return new RuleResult(false , name + " : illegal values of logger in previous reqeust." , false ) ;
 			
			try 
			{
				oldLogger = User.lookupByUserId(oll.get(0).getUser11Id()) ;
				
				if( oal.size() > 0 ) // oal.size() == 0 only in state 3/ 6 / 10 / 11
					oldAssignee = User.lookupAllByUserId(oal.get(0).getUser11Id()) ;
			} catch (Exception e) {			
				e.printStackTrace();
				return new RuleResult(false , name + " : illegal values of logger/assingee in previous reqeust." , false ) ;
			}			
		}
		
		ccrName = (String)currentRequest.myMapFieldToObjects.get(CCR_CCR_FIELD_NAME) ;
		ccdName = (String) currentRequest.myMapFieldToObjects.get(CCR_CCD_FIELD_NAME) ;
		epmdName = (String) currentRequest.myMapFieldToObjects.get(CCR_EPMD_FIELD_NAME ) ;
		
		if( null == ccrName || ccrName.trim().equals("") ||
			null == ccdName || ccdName.trim().equals("") ||
			null == epmdName || epmdName.trim().equals("") 
		  ) 
		{
			return new RuleResult(false, name + " : illegal values of fields in this request.", false ) ;
		}
		
		loginUser = currentRequest.getUserId() ;
		ArrayList<RequestUser> loggers = currentRequest.getLoggers() ;
		if( null == loggers || loggers.size() == 0 )
		{
			currLogger = loginUser ;
		}
		else
		{
			try {
				currLogger = loggers.get(0).getUser() ;
			} catch (DatabaseException e) 
			{				
				e.printStackTrace();
				return new RuleResult(false,"Exception while accessing logger field." , false ) ;
			}
		}
		// decide the state of the request
		Integer state = null ;
		try {
			state = new Integer( getRequestState( isAddRequest, loginUser, oldLogger, oldAssignee, ccrName, ccdName, epmdName, oldCcrName, oldCcdName, oldEpmdName ) ) ;
		} catch (Exception e) 
		{			
			e.printStackTrace();
			return new RuleResult( false , name + " : Illegal state of the request." , false ) ;
		}
		
		if( null == state )
		{
			return new RuleResult( false , name + " : Illegal state of the request." , false ) ;
		}		
		// check who is logging the request ? only assignee or previous logger is alowed to log a request
		try
		{
			LOG.info("Nitiraj : state = " + state ) ;
			switch( state.intValue() ) 
			{
				case 0 :
					execState0( ba, oldRequest, currentRequest, extendedFields, currLogger, oldAssignee, oldLogger ) ;
					break ;
				case 1 :
					execState1( ba, oldRequest, currentRequest, extendedFields, currLogger, oldAssignee, oldLogger ) ;
					break ;
				case 3 :
					execState3( ba, oldRequest, currentRequest, extendedFields, currLogger, oldAssignee, oldLogger ) ;
					break ;
				case 4 :
					execState4( ba, oldRequest, currentRequest, extendedFields, currLogger, oldAssignee, oldLogger ) ;
					break ;
				case 5 :
					execState5( ba, oldRequest, currentRequest, extendedFields, currLogger, oldAssignee, oldLogger ) ;
					break ;
				case 6 :
					execState6( ba, oldRequest, currentRequest, extendedFields, currLogger, oldAssignee, oldLogger ) ;
					break ;
				case 7 :
					execState7( ba, oldRequest, currentRequest, extendedFields, currLogger, oldAssignee, oldLogger ) ;
					break ;
				case 8 :
					execState8( ba, oldRequest, currentRequest, extendedFields, currLogger, oldAssignee, oldLogger ) ;
					break ;
				case 9 :
					execState9( ba, oldRequest, currentRequest, extendedFields, currLogger, oldAssignee, oldLogger ) ;
					break ;
				case 10 :
					execState10( ba, oldRequest, currentRequest, extendedFields, currLogger, oldAssignee, oldLogger ) ;
					break ;
				case 11 :
					execState11( ba, oldRequest, currentRequest, extendedFields, currLogger, oldAssignee, oldLogger ) ;
					break ;
					
				default : 
					throw new TBitsException( "Illegal request state. Cannot proceed." ) ;					
			}
		}
		catch(TBitsException e )
		{
			e.printStackTrace() ;
			return new RuleResult( false, e.getDescription() , false ) ;
		}
		catch( Exception e )
		{
			e.printStackTrace() ;
			LOG.info("Exception occured in prerule  : " + name );
			return new RuleResult(false , e.getMessage() , false);
		}
		catch (APIException e) {		
			e.printStackTrace();
			LOG.info("Exception occured in prerule  : " + name );
			return new RuleResult(false , e.getMessage() , false);
		}
		
		// according to present state and the input variable  
			// check if the input variable was correct one to be modified.
			// if yes decide the final state according to the input and current state
			// make modifications into the request
 
		return new RuleResult( true , name +" : Reached END and nothing was returned." , true );
	}

	private void execState11(BusinessArea ba, Request oldRequest,
			Request currentRequest, Hashtable<Field, RequestEx> extendedFields,
			User loginUser, User oldAssignee, User oldLogger) {
		// TODO Auto-generated method stub
		
	}

	private void execState10(BusinessArea ba, Request oldRequest,
			Request currentRequest, Hashtable<Field, RequestEx> extendedFields,
			User loginUser, User oldAssignee, User oldLogger) {
		// TODO Auto-generated method stub
		
	}

	private void execState9(BusinessArea ba, Request oldRequest,
			Request currentRequest, Hashtable<Field, RequestEx> extendedFields,
			User loginUser, User oldAssignee, User oldLogger) 
	{
			
	}

	private void execState8(BusinessArea ba, Request oldRequest,
			Request currentRequest, Hashtable<Field, RequestEx> extendedFields,
			User loginUser, User oldAssignee, User oldLogger) 
	{
		// no changes allowed		
	}

	private void execState7(BusinessArea ba, Request oldRequest,
			Request currentRequest, Hashtable<Field, RequestEx> extendedFields,
			User loginUser, User oldAssignee, User oldLogger) throws TBitsException ,Exception, APIException 
	{
		LOG.info("Nitiraj : inside state 7") ;
		Type ccrType = null ;
		Type ccdType = null ;
		Type epmdType = null ;
		ArrayList<RequestUser> assList = null ;
		ArrayList<RequestUser> logList = null ;
		ArrayList<RequestUser> subList = null ;

		RequestUser myLogger = new RequestUser(ba.getSystemId(),currentRequest.getRequestId(),UserType.LOGGER, loginUser.getUserId(),1,false) ;
		logList = new ArrayList<RequestUser>() ;
		logList.add(myLogger) ;	
		
		if( loginUser.getUserId() == oldLogger.getUserId() )
		{
			LOG.info("Nitiraj : logger = old Logger") ;
			String currCcdName = (String)currentRequest.myMapFieldToObjects.get(CCR_CCD_FIELD_NAME) ;
 			if( currCcdName.equals( CCR_CCD_REJECTED ))
			{
 				LOG.info("Nitiraj : CCD = rejected") ;
				// go to state 6
				ccrType = Type.lookupBySystemIdAndFieldNameAndTypeName(ba.getSystemId(), CCR_CCR_FIELD_NAME, CCR_CCR_CCR ) ;
				epmdType = Type.lookupBySystemIdAndFieldNameAndTypeName(ba.getSystemId(), CCR_EPMD_FIELD_NAME, CCR_EPMD_APPROVED ) ;
				
				assList = new ArrayList<RequestUser>() ;
				subList = new ArrayList<RequestUser>() ;
				int firstLog = getFirstLogger( ba.getSystemId(), currentRequest.getRequestId() ) ;				
				RequestUser fl = new RequestUser( ba.getSystemId() , currentRequest.getRequestId() , UserType.SUBSCRIBER ,firstLog, 1, false ) ;
				subList.add(fl) ;
				int firstAss = getUserFromMap(firstLog,CCR_MAP_TYPE_NAME, UserMapManager.TO ) ;
				RequestUser ll = new RequestUser( ba.getSystemId() , currentRequest.getRequestId() , UserType.SUBSCRIBER , firstAss, 1, false ) ;
				subList.add(ll) ;
			}
			else if( currCcdName.equals(CCR_CCD_RECTIFY))
			{
				LOG.info("Nitiraj : CCD = rectify") ;
				// go to state 7 
				ccrType = Type.lookupBySystemIdAndFieldNameAndTypeName(ba.getSystemId(), CCR_CCR_FIELD_NAME, CCR_CCR_PFR ) ;
				epmdType = Type.lookupBySystemIdAndFieldNameAndTypeName(ba.getSystemId(), CCR_EPMD_FIELD_NAME, CCR_EPMD_APPROVED ) ;
				
				assList = new ArrayList<RequestUser>() ;
				subList = new ArrayList<RequestUser>() ;
				int firstLog = getFirstLogger( ba.getSystemId(), currentRequest.getRequestId() ) ;				
				RequestUser fl = new RequestUser( ba.getSystemId() , currentRequest.getRequestId() , UserType.ASSIGNEE ,firstLog, 1, false ) ;
				assList.add(fl) ;
				int firstAss = getUserFromMap(firstLog,CCR_MAP_TYPE_NAME, UserMapManager.TO ) ;
				RequestUser ll = new RequestUser( ba.getSystemId() , currentRequest.getRequestId() , UserType.SUBSCRIBER , oldLogger.getUserId(), 1, false ) ;
				subList.add(ll) ;
				
			}
			else if( currCcdName.equals(CCR_CCD_APPROVED))
			{
				LOG.info("Nitiraj : CCD = approved.") ;
				// go to state 8
				ccrType = Type.lookupBySystemIdAndFieldNameAndTypeName(ba.getSystemId(), CCR_CCR_FIELD_NAME, CCR_CCR_STCPR ) ;
				epmdType = Type.lookupBySystemIdAndFieldNameAndTypeName(ba.getSystemId(), CCR_EPMD_FIELD_NAME, CCR_EPMD_APPROVED ) ;
				
				assList = new ArrayList<RequestUser>() ;
				subList = new ArrayList<RequestUser>() ;
				int firstLog = getFirstLogger( ba.getSystemId(), currentRequest.getRequestId() ) ;				
				RequestUser fl = new RequestUser( ba.getSystemId() , currentRequest.getRequestId() , UserType.ASSIGNEE ,firstLog, 1, false ) ;
				subList.add(fl) ;
				int firstAss = getUserFromMap(firstLog,CCR_MAP_TYPE_NAME, UserMapManager.TO ) ; ;
				RequestUser ll = new RequestUser( ba.getSystemId() , currentRequest.getRequestId() , UserType.SUBSCRIBER , firstAss , 1, false ) ;
				subList.add(ll) ;
				createReportAndAddNewRequest(ba, oldRequest, currentRequest, extendedFields ) ;
			}
			else
			{
				throw new TBitsException( "Must change the : " + CCR_CCD_FIELD_NAME + " to some value other than " + CCR_CCD_PENDING ) ;
			}			
			
			setState( currentRequest, ccrType, ccdType, epmdType, assList, logList, subList ) ;
		}
		else if( loginUser.getUserId() == oldAssignee.getUserId() )
		{
			LOG.info("Nitiraj : logger = old Assignee") ;
			// go to state 1
			ccrType = Type.lookupAllBySystemIdAndFieldNameAndTypeName(ba.getSystemId(), CCR_CCR_FIELD_NAME, CCR_CCR_PEPMD) ;
			ccdType = Type.lookupBySystemIdAndFieldNameAndTypeName(ba.getSystemId(), CCR_CCD_FIELD_NAME, CCR_CCD_PENDING ) ;
			epmdType = Type.lookupBySystemIdAndFieldNameAndTypeName(ba.getSystemId(), CCR_EPMD_FIELD_NAME, CCR_EPMD_PENDING ) ;
					
			int assUserId = getUserFromMap(loginUser.getUserId(),CCR_MAP_TYPE_NAME, UserMapManager.TO ) ; 
			RequestUser myAssignee = new RequestUser(ba.getSystemId(),currentRequest.getRequestId(), UserType.ASSIGNEE, assUserId ,1,false) ;
			assList = new ArrayList<RequestUser>() ;
			assList.add(myAssignee) ;
			RequestUser subs = new RequestUser(ba.getSystemId(),currentRequest.getRequestId(), UserType.SUBSCRIBER, oldLogger.getUserId() ,1,false) ;
			subList = new ArrayList<RequestUser>() ;
			subList.add(subs) ;
			// set the values
			setState( currentRequest, ccrType, ccdType, epmdType, assList, logList, subList ) ;
		}
		else
		{
			throw new TBitsException( NOT_ALLOWED ) ;
		}
		
	}

	private void createReportAndAddNewRequest(BusinessArea ba,
			Request oldRequest, Request currentRequest,
			Hashtable<Field, RequestEx> extendedFields) throws TBitsException , Exception, APIException 
	{
		LOG.info("Nitiraj : inside create Report and Add new request.") ;
//		final public static String RPT_PROJECT = "Project" ;
//		final public static String RPT_PROJECTNO = "ProjectNo" ;
//		final public static String RPT_LOCATION = "Location" ;
//		final public static String RPT_CCRNO = "CcrNo" ;
//		final public static String RPT_CONTRACT_DOCUMENT = "ContractDocument" ;
//		final public static String RPT_SECTION = "Section" ;
//		final public static String RPT_SUBJECT = "Subject" ;
//		final public static String RPT_CONTRACT_REQUIREMENT = "ContractRequirement" ;
//		final public static String RPT_CONTRACT_CLARIFICATION_REQUEST = "ContractClarificationRequest" ;
//		final public static String RPT_PETROBRAS_RESPONSE = "PetrobrasResponse" ;
//		final public static String RPT_PREPAREDBY = "PreparedBy" ;  
//		final public static String RPT_AUTHORIZEDBY = "AuthorisedBy" ; ?
//		final public static String RPT_APPROVEDBY = "ApprovedBy" ; ?
//		final public static String RPT_AUTHORISED_NAME = "AuthorisedName" ;
//		final public static String RPT_AUTHORISED_DATE = "AuthorisedDate" ; today
 //		final public static String RPT_APPROVED_NAME = "ApprovedName" ;
//		final public static String RPT_APPROVED_DATE = "ApprovedDate" ; today
		
		Connection con = null ;
		try
		{
			con = DataSourcePool.getConnection() ;
			con.setAutoCommit(false) ;
			
			String project = currentRequest.get(CCR_PROJECT_FIELD_NAME) ;
			String projectNo = currentRequest.get(CCR_PROJECT_NO_FIELD_NAME) ;
			
			String crrNo = getCrrNo(con, projectNo ) ;
			
			Hashtable<String,String> params = new Hashtable<String,String>() ;
			
			params.put(RPT_PROJECT, project ) ;		
			params.put(RPT_PROJECTNO, projectNo) ;
			params.put(RPT_CCRNO,  crrNo ) ;
			
			String location = currentRequest.get(CCR_LOCATION_FIELD_NAME) ;
			params.put( RPT_LOCATION , location ) ;
					
			String contractDocument = currentRequest.get(CCR_CONTRACT_DOC_FIELD_NAME)  ;
			params.put(RPT_CONTRACT_DOCUMENT, contractDocument) ;
			
			String section = currentRequest.get(CCR_SECTION_FIELD_NAME) ;
			params.put(RPT_SECTION, section ) ;
			
			String subject = currentRequest.get(Field.SUBJECT) ;
			params.put(RPT_SUBJECT, subject) ;
			
			String contractReq = currentRequest.get(CCR_CON_REQ_FIELD_NAME) ;
			params.put(RPT_CONTRACT_REQUIREMENT, contractReq) ;
			
			String conClaReq = currentRequest.get(CCR_CON_CLA_REQ_FIELD_NAME) ;
			params.put(RPT_CONTRACT_CLARIFICATION_REQUEST, conClaReq) ;
			
			int first = getFirstLogger( ba.getSystemId(), currentRequest.getRequestId() ) ;
			User firstLogger = User.lookupAllByUserId(first) ; 
			String preparedBy = firstLogger.getDisplayName() ;
			params.put(RPT_PREPAREDBY, preparedBy);
			
			int pm = getUserFromMap(first, CCR_MAP_TYPE_NAME, UserMapManager.TO) ;
			User pmUser = User.lookupAllByUserId(pm) ;
			String authorizedBy = pmUser.getDisplayName() ;
			params.put(RPT_AUTHORISED_NAME, authorizedBy) ;
			String imageName = pmUser.getUserLogin() + ".gif" ;
			File imageFile = Configuration.findPath("tbitsreports/" + imageName);
			LOG.info("Nitiraj : authorized sign file : " + imageFile ) ;
			if(null != imageFile )
			{
				String imageLocation = imageFile.getAbsolutePath() ;
				params.put(CCRConstants.RPT_AUTHORIZED_BY_IMAGE, imageLocation ) ;
			}
			
			int cpm = getUserFromMap(pm, CCR_CC_MAP_TYPE_NAME, UserMapManager.TO ) ;
			User cpmUser = User.lookupByUserId(cpm) ;
			String approvedBy = cpmUser.getDisplayName() ;
			params.put(RPT_APPROVED_NAME, approvedBy);
			String imageName1 = cpmUser.getUserLogin() + ".gif";			
			LOG.info("Nitiraj : approved sign logger's image name : " + imageName1 ) ; 
			File imageFile1 = Configuration.findPath("tbitsreports/" + imageName1);
			LOG.info("Nitiraj : approved sign file : " + imageFile1 ) ;
			if(null != imageFile1 )
			{
				String imageLocation1 = imageFile1.getAbsolutePath() ;
				params.put(CCRConstants.RPT_APPROVED_BY_IMAGE, imageLocation1 ) ;
			}
						
			Timestamp today = new Timestamp() ;
			String todayStr = today.toCustomFormat("dd-MMM-yy") ;
			params.put(RPT_AUTHORISED_DATE, todayStr) ;
			params.put(RPT_APPROVED_DATE, todayStr) ;
			
			HashMap<String,String> reportParamMap = new HashMap<String,String>() ;
			
			File pdfFile =generateReport( "Sbm_offshore.rptdesign", params, reportParamMap, "pdf" ) ;
//			File docFile = generateReport("Sbm_offshore.rptdesign", params, reportParamMap, "doc" ) ;
			
			if( pdfFile == null /*&& docFile == null*/ )
			{
				con.rollback() ;
				throw new TBitsException( "Cannot generate files for attachments.") ;
			}
			
			AttachmentInfo pdfAtinfo = null ;
//			AttachmentInfo docAtinfo = null ;
			try
			{
				if(null != pdfFile)
				{
					LOG.info("Nitiraj : uploading pdf file");
					String displayName = new Timestamp().toCustomFormat("yyyyMMdd") + "_" + crrNo + ".pdf";
								
					Uploader up = new Uploader() ;
					pdfAtinfo = up.moveIntoRepository(pdfFile) ;
					// change display name 
					pdfAtinfo.name = displayName ;			
				}
			}
			catch(Exception e)
			{
				e.printStackTrace() ;
				//ignored.
			}
			
//			try
//			{
//				if(null != docFile )
//				{
//					LOG.info("Nitiraj : uploading doc file") ;
//					String displayName = new Timestamp().toCustomFormat("yyyyMMdd") + "_" + crrNo + ".doc";
//					
//					Uploader up = new Uploader() ;
//					docAtinfo = up.moveIntoRepository(docFile) ;
//					// change display name 
//					docAtinfo.name = displayName ;
//				}
//			}
//			catch(Exception e)
//			{
//				e.printStackTrace() ;
//				// ignored
//			}
			
			Hashtable<String,String> reqParams = new Hashtable<String,String>() ;
			User root = User.lookupAllByUserLogin(CCRConstants.TBITS_ROOT) ;
			if( null == root )
			{
				throw new TBitsException( "Cannot find : " + CCRConstants.TBITS_ROOT ) ;
			}
			reqParams.put(Field.USER, root.getUserId()+"") ;
			BusinessArea ccrExtBA = BusinessArea.lookupBySystemPrefix(CCREXT_SYSPREFIX) ;
			if(null == ccrExtBA )
			{
				throw new TBitsException( "Cannot find business area " + CCREXT_SYSPREFIX ) ;
			}
			
			reqParams.put(Field.BUSINESS_AREA, ccrExtBA.getSystemId()+"") ;
			
			String subscribers =  cpmUser.getUserLogin() + "," + pmUser.getUserLogin() + "," + firstLogger.getUserLogin() ;
			reqParams.put(Field.SUBSCRIBER, subscribers) ;
			
	//		int smb = getUserFromMap(cpm, CCRConstants.CCR_SMB_MAP_TYPE_NAME, UserMapManager.TO) ;
	//		User smbUser = User.lookupAllByUserId(smb) ;
	//		String logger = smbUser.getUserLogin() ;
			reqParams.put(Field.LOGGER, cpmUser.getUserLogin()) ;
			
			reqParams.put(CCREXT_PROJECT_FIELD_NAME, (String)currentRequest.myMapFieldToObjects.get(CCR_PROJECT_FIELD_NAME)) ;
			reqParams.put(CCREXT_PROJECT_NO_FIELD_NAME, (String)currentRequest.myMapFieldToObjects.get(CCR_PROJECT_NO_FIELD_NAME)) ;
			reqParams.put(Field.SUBJECT, currentRequest.get(Field.SUBJECT)) ;
			reqParams.put(CCREXT_CORR_NO_FIELD_NAME, crrNo ) ;
			reqParams.put(Field.ASSIGNEE, CCRConstants.CLIENT) ;			
			reqParams.put(CCREXT_CORR_STATUS_FIELD_NAME, CCRConstants.CCREXT_CORR_STC ) ;
			reqParams.put(Field.RELATED_REQUESTS, ba.getSystemPrefix() + "#" + currentRequest.getRequestId() ) ;
			reqParams.put(CCREXT_CLIENT_DEC_FIELD_NAME, CCRConstants.CCREXT_CD_PENDING) ;
			
			ArrayList<AttachmentInfo> smbAtt = new ArrayList<AttachmentInfo>() ;
			if( null != pdfAtinfo)
				smbAtt.add(pdfAtinfo) ;
//			if( null != docAtinfo )
//				smbAtt.add(docAtinfo) ;
			
			String smbAttJson = AttachmentInfo.toJson(smbAtt) ;			
			reqParams.put(CCREXT_SMB_SUBMIT_FIELD_NAME, smbAttJson ) ;
			
			AddRequest ar = new AddRequest() ;
			TBitsResourceManager trm = new TBitsResourceManager() ;
			Request req;
			
			req = ar.addRequest(con, trm, reqParams );
			LOG.info("Nitiraj : Added new request with id : " + req.getRequestId() ) ;
			
			con.commit() ;			
			trm.commit() ;
			
			currentRequest.setAssignees(new ArrayList<RequestUser>() ) ;
			ArrayList<RequestUser> subs = new ArrayList<RequestUser>() ;
			RequestUser sub1 = new RequestUser( ba.getSystemId() , currentRequest.getRequestId() , UserType.SUBSCRIBER ,first, 1, false ) ;
			RequestUser sub2 = new RequestUser( ba.getSystemId() , currentRequest.getRequestId() , UserType.SUBSCRIBER ,pm, 1, false ) ;
			subs.add(sub1) ;
			subs.add(sub2) ;
			currentRequest.setSubscribers(subs) ;
			LOG.info("Nitiraj : setting the subs : " + subs ) ;
			currentRequest.setAttachments(smbAtt) ;
			
			Field crrNoField = Field.lookupBySystemIdAndFieldName(ba.getSystemId(), CCRConstants.CCR_CCR_NO_FIELD_NAME) ;
			RequestEx nRequestEx = extendedFields.get(crrNoField);
			nRequestEx.setVarcharValue(crrNo) ;			
  		}
		catch( TBitsException e )
		{
			e.printStackTrace() ;
			try {
				con.rollback() ;
			} catch (SQLException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			throw e ;
		}
		catch( Exception e )
		{
			e.printStackTrace() ;
			try {
				con.rollback() ;
			} catch (SQLException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			throw e ;
		} catch (APIException e) {
			
			e.printStackTrace();
			try {
				con.rollback() ;
			} catch (SQLException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			throw e ;
		}
		finally
		{
			if(null != con)
				try {
					con.close() ;
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		}
		
		
	}

	private File generateReport(String reportName,
			Hashtable<String, String> params,
			HashMap<String, String> reportParams, String format) 
	{
		IReportDocument ird = null ;
		TBitsReportEngine tre = null ;
		try
		{
				tre = new TBitsReportEngine();
				if(tre == null)
				{
					LOG.error("Unable to get the instance of ReportEngine.");
					return null ;
				}
				IReportRunnable reportDesign;
				reportDesign = tre.getReportDesign(reportName);
				if(reportDesign == null)
				{
					LOG.error("Unable to get the design instance of " + reportName);
					return null ;
				}
				
				IReportEngine ire = tre.getEngine() ;
				EngineConfig ec = ire.getConfig() ;
				
				// set all non-report parameters
				for( Enumeration<String> keys = params.keys() ; keys.hasMoreElements() ; )
				{			
					String key = keys.nextElement() ;
					String value = params.get(key) ;
					ec.getAppContext().put(key,value) ;
				}
				
				
				ird = tre.getReportDocument(reportDesign, reportParams) ;
				
				File outFile = null ;
				if( null != format && format.trim().equalsIgnoreCase("pdf"))
				 outFile = tre.getPDFReport(ird);
//				else if (null != format && format.trim().equalsIgnoreCase("doc"))
//					outFile = tre.getDOCReport(ird) ;
				else 
					outFile = tre.getHTMLReport(ird) ; // default
				
				/////// print file info
				if( outFile != null ) 
				{
					LOG.info( "Name:" + outFile.getName() + " path = " + outFile.getAbsolutePath() ) ;
					return outFile ;
				}
				else
				{
					LOG.error("OutPUT file is null" ) ;
					return null ;
				}
//				
//				if(!leaveOutputFile)
//				{
//					if (!outFile.delete())
//						LOG.warn("Can not delete the temporary file: "
//								+ outFile.getAbsolutePath());
//				}
						
		} catch (EngineException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		} catch (SemanticException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}	
		catch( Exception e )
		{
			e.printStackTrace() ;
			return null ;
		}
		finally
		{
			if (tre != null)
				tre.destroy();
		}
//		return null;
	}

	public int getNextCorrNo(Connection con, String corrCat ) throws SQLException
	{
	System.out.println("generating corr. no. for : " + corrCat );
		try {	
			CallableStatement stmt = con.prepareCall("stp_getAndIncrMaxId ?");
			stmt.setString(1, corrCat );
			ResultSet rs = stmt.executeQuery();
			if (rs.next()) {
				int id = rs.getInt("max_id");
				return id;
			} else {
				throw new SQLException();
			}
		} catch (SQLException e) {
			throw e;
		}		
	}
	
	
	private String getCrrNo(Connection con, String projectNo) throws SQLException 
	{
		String ccrNo = CCRConstants.CCR_CCR_NO_PREFIX ;
		ccrNo += projectNo ;
		
		int n = getNextCorrNo(con, ccrNo) ;
		
		String nextCorresId =  Integer.toString(n);  //Integer.toString(fc.getMaxRequestId() + 1);
		switch( nextCorresId.length() )
		{
			case 1 : nextCorresId = "000" + nextCorresId ; break ;
			case 2 : nextCorresId = "00" + nextCorresId ; break ;
			case 3 : nextCorresId = "0" + nextCorresId ; break ;
		}
		
		
		Timestamp currTime = new Timestamp() ;
		String YY = currTime.toCustomFormat("yy") ;
		ccrNo += "-" + YY + "-" + nextCorresId ;
		
		return ccrNo;
	}

	private void execState6(BusinessArea ba, Request oldRequest,
			Request currentRequest, Hashtable<Field, RequestEx> extendedFields,
			User loginUser, User oldAssignee, User oldLogger) throws TBitsException, Exception, APIException 
	{
		LOG.info("Nitiraj : inside state 6") ;
		Type ccrType = null ;
		Type ccdType = null ;
		Type epmdType = null ;
		ArrayList<RequestUser> assList = null ;
		ArrayList<RequestUser> logList = null ;
		ArrayList<RequestUser> subList = null ;

		RequestUser myLogger = new RequestUser(ba.getSystemId(),currentRequest.getRequestId(),UserType.LOGGER, loginUser.getUserId(),1,false) ;
		logList = new ArrayList<RequestUser>() ;
		logList.add(myLogger) ;	
		
		if( loginUser.getUserId() == oldLogger.getUserId() )
		{
			LOG.info("Nitiraj : logger = oldLogger") ;
			String currCcdName = (String)currentRequest.myMapFieldToObjects.get(CCR_CCD_FIELD_NAME) ;
 			if( currCcdName.equals( CCR_CCD_REJECTED ))
			{
 				LOG.info("Nitiraj : CCD = rejected") ;
				// go to state 6
				ccrType = Type.lookupBySystemIdAndFieldNameAndTypeName(ba.getSystemId(), CCR_CCR_FIELD_NAME, CCR_CCR_CCR ) ;
				epmdType = Type.lookupBySystemIdAndFieldNameAndTypeName(ba.getSystemId(), CCR_EPMD_FIELD_NAME, CCR_EPMD_APPROVED ) ;
				
				assList = new ArrayList<RequestUser>() ;
				subList = new ArrayList<RequestUser>() ;
				int firstLog = getFirstLogger( ba.getSystemId(), currentRequest.getRequestId() ) ;				
				RequestUser fl = new RequestUser( ba.getSystemId() , currentRequest.getRequestId() , UserType.SUBSCRIBER ,firstLog, 1, false ) ;
				subList.add(fl) ;
				int firstAss = getUserFromMap(firstLog,CCR_MAP_TYPE_NAME, UserMapManager.TO ) ;
				RequestUser ll = new RequestUser( ba.getSystemId() , currentRequest.getRequestId() , UserType.SUBSCRIBER , firstAss, 1, false ) ;
				subList.add(ll) ;
			}
			else if( currCcdName.equals(CCR_CCD_RECTIFY))
			{
				LOG.info("Nitiraj : CCD = rectify") ;
				// go to state 7 
				ccrType = Type.lookupBySystemIdAndFieldNameAndTypeName(ba.getSystemId(), CCR_CCR_FIELD_NAME, CCR_CCR_PFR ) ;
				epmdType = Type.lookupBySystemIdAndFieldNameAndTypeName(ba.getSystemId(), CCR_EPMD_FIELD_NAME, CCR_EPMD_APPROVED ) ;
				
				assList = new ArrayList<RequestUser>() ;
				subList = new ArrayList<RequestUser>() ;
				int firstLog = getFirstLogger( ba.getSystemId(), currentRequest.getRequestId() ) ;				
				RequestUser fl = new RequestUser( ba.getSystemId() , currentRequest.getRequestId() , UserType.ASSIGNEE ,firstLog, 1, false ) ;
				assList.add(fl) ;
				int firstAss = getUserFromMap(firstLog,CCR_CC_MAP_TYPE_NAME, UserMapManager.TO ) ;
				RequestUser ll = new RequestUser( ba.getSystemId() , currentRequest.getRequestId() , UserType.SUBSCRIBER , oldLogger.getUserId(), 1, false ) ;
				subList.add(ll) ;
				
			}
			else if( currCcdName.equals(CCR_CCD_APPROVED))
			{
				LOG.info("Nitiraj : CCD = approved") ;
				// go to state 8
				ccrType = Type.lookupBySystemIdAndFieldNameAndTypeName(ba.getSystemId(), CCR_CCR_FIELD_NAME, CCR_CCR_STCPR ) ;
				epmdType = Type.lookupBySystemIdAndFieldNameAndTypeName(ba.getSystemId(), CCR_EPMD_FIELD_NAME, CCR_EPMD_APPROVED ) ;
				
				assList = new ArrayList<RequestUser>() ;
				subList = new ArrayList<RequestUser>() ;
				int firstLog = getFirstLogger( ba.getSystemId(), currentRequest.getRequestId() ) ;				
				RequestUser fl = new RequestUser( ba.getSystemId() , currentRequest.getRequestId() , UserType.ASSIGNEE ,firstLog, 1, false ) ;
				subList.add(fl) ;
				int firstAss = getUserFromMap(firstLog,CCR_MAP_TYPE_NAME, UserMapManager.TO ) ; ;
				RequestUser ll = new RequestUser( ba.getSystemId() , currentRequest.getRequestId() , UserType.SUBSCRIBER , firstAss , 1, false ) ;
				subList.add(ll) ;
				
				createReportAndAddNewRequest(ba, oldRequest, currentRequest, extendedFields ) ;
			}
			else
			{
				throw new TBitsException( "Must change the : " + CCR_CCD_FIELD_NAME + " to some value other than " + CCR_CCD_PENDING ) ;
			}			
			
			setState( currentRequest, ccrType, ccdType, epmdType, assList, logList, subList ) ;
		}
		else
		{
			throw new TBitsException( NOT_ALLOWED ) ;
		}
		
	}

	private int getUserFromMap(int firstLog, String typeName, int to) throws TBitsException 
	{
		ArrayList<Integer> tos = UserMapManager.getTos(firstLog, typeName ) ;
		
		return tos.get(0).intValue() ;
	}

	private int getFirstLogger(int systemId, int requestId) throws DatabaseException 
	{
		Action action = Action.lookupBySystemIdAndRequestIdAndActionId(systemId, requestId, 1) ;
		ArrayList<Integer> loggers = action.getLoggerIds() ;
		return loggers.get(0).intValue() ;
	}

	private void execState5(BusinessArea ba, Request oldRequest,
			Request currentRequest, Hashtable<Field, RequestEx> extendedFields,
			User loginUser, User oldAssignee, User oldLogger) throws TBitsException , Exception, APIException 
	{
		LOG.info("Nitiraj : inside state 5") ;
		Type ccrType = null ;
		Type ccdType = null ;
		Type epmdType = null ;
		ArrayList<RequestUser> assList = null ;
		ArrayList<RequestUser> logList = null ;
		ArrayList<RequestUser> subList = null ;

		RequestUser myLogger = new RequestUser(ba.getSystemId(),currentRequest.getRequestId(),UserType.LOGGER, loginUser.getUserId(),1,false) ;
		logList = new ArrayList<RequestUser>() ;
		logList.add(myLogger) ;			

		if( loginUser.getUserId() == oldLogger.getUserId() )
		{
			LOG.info("Nitiraj : logger = oldLogger") ;
			String curEpmdName = (String)currentRequest.myMapFieldToObjects.get(CCR_EPMD_FIELD_NAME) ;
			if( curEpmdName.equals(CCR_EPMD_REJECTED ) ) 
			{
				LOG.info("Nitiraj : epmd = rejected") ;
				// go to state 3
				ccrType = Type.lookupBySystemIdAndFieldNameAndTypeName(ba.getSystemId(), CCR_CCR_FIELD_NAME, CCR_CCR_EPMR ) ;
				ccdType = Type.lookupBySystemIdAndFieldNameAndTypeName(ba.getSystemId(), CCR_CCD_FIELD_NAME, CCR_CCD_PENDING ) ;
				
//				RequestUser myAssignee = new RequestUser(ba.getSystemId(), currentRequest.getRequestId(), UserType.ASSIGNEE, oldLogger.getUserId(), 1, false ) ;
				assList = new ArrayList<RequestUser>() ;
				subList = new ArrayList<RequestUser>() ;
//				assList.add(myAssignee) ;
			}
			else if ( curEpmdName.equals(CCR_EPMD_RECTIFY) ) 
			{
				LOG.info("Nitiraj : epmd = rectify") ;
				// go to state 4
				ccrType = Type.lookupBySystemIdAndFieldNameAndTypeName(ba.getSystemId(), CCR_CCR_FIELD_NAME, CCR_CCR_PFR ) ;
				ccdType = Type.lookupBySystemIdAndFieldNameAndTypeName(ba.getSystemId(), CCR_CCD_FIELD_NAME, CCR_CCD_PENDING ) ;
				
				RequestUser myAssignee = new RequestUser(ba.getSystemId(), currentRequest.getRequestId(), UserType.ASSIGNEE, oldLogger.getUserId(), 1, false ) ;
				assList = new ArrayList<RequestUser>() ;
				assList.add(myAssignee) ;
			}
			else if( curEpmdName.equals(CCR_EPMD_APPROVED ))
			{
				LOG.info("Nitiraj : epmd = approved") ;
				// go to state 5
				ccrType = Type.lookupBySystemIdAndFieldNameAndTypeName(ba.getSystemId(), CCR_CCR_FIELD_NAME, CCR_CCR_PCCD ) ;
				ccdType = Type.lookupBySystemIdAndFieldNameAndTypeName(ba.getSystemId(), CCR_CCD_FIELD_NAME,CCR_CCD_PENDING ) ;
				
				int assUserId = getUserFromMap(loginUser.getUserId(),CCR_CC_MAP_TYPE_NAME, UserMapManager.TO ) ;
				RequestUser myAssignee = new RequestUser( ba.getSystemId() , currentRequest.getRequestId() , UserType.ASSIGNEE , assUserId, 1, false ) ;
				assList = new ArrayList<RequestUser>() ;
				assList.add(myAssignee) ;
			}
			else
			{
				throw new TBitsException("Must change the:" + CCR_EPMD_FIELD_NAME + " to some value other than " + CCR_EPMD_PENDING ) ;
			}

			setState( currentRequest, ccrType, ccdType, epmdType, assList, logList, subList ) ;
		}
		else if( loginUser.getUserId() == oldAssignee.getUserId() )
		{
			LOG.info("Nitiraj : logger = oldAssignee");
			String currCcdName = (String)currentRequest.myMapFieldToObjects.get(CCR_CCD_FIELD_NAME) ;
			if( currCcdName.equals( CCR_CCD_REJECTED ))
			{
				LOG.info("Nitiraj : CCD = rejected") ;
				// go to state 6
				ccrType = Type.lookupBySystemIdAndFieldNameAndTypeName(ba.getSystemId(), CCR_CCR_FIELD_NAME, CCR_CCR_CCR ) ;
				epmdType = Type.lookupBySystemIdAndFieldNameAndTypeName(ba.getSystemId(), CCR_EPMD_FIELD_NAME, CCR_EPMD_APPROVED ) ;
				
				assList = new ArrayList<RequestUser>() ;
				subList = new ArrayList<RequestUser>() ;
				int firstLog = getFirstLogger( ba.getSystemId(), currentRequest.getRequestId() ) ;				
				RequestUser fl = new RequestUser( ba.getSystemId() , currentRequest.getRequestId() , UserType.SUBSCRIBER ,firstLog, 1, false ) ;
				subList.add(fl) ;
				RequestUser ll = new RequestUser( ba.getSystemId() , currentRequest.getRequestId() , UserType.SUBSCRIBER , oldLogger.getUserId(), 1, false ) ;
				subList.add(ll) ;
			}
			else if( currCcdName.equals(CCR_CCD_RECTIFY))
			{
				LOG.info("Nitiraj : CCD = rectify") ;
				// go to state 7 
				ccrType = Type.lookupBySystemIdAndFieldNameAndTypeName(ba.getSystemId(), CCR_CCR_FIELD_NAME, CCR_CCR_PFR ) ;
				epmdType = Type.lookupBySystemIdAndFieldNameAndTypeName(ba.getSystemId(), CCR_EPMD_FIELD_NAME, CCR_EPMD_APPROVED ) ;
				
				assList = new ArrayList<RequestUser>() ;
				subList = new ArrayList<RequestUser>() ;
				int firstLog = getFirstLogger( ba.getSystemId(), currentRequest.getRequestId() ) ;				
				RequestUser fl = new RequestUser( ba.getSystemId() , currentRequest.getRequestId() , UserType.ASSIGNEE ,firstLog, 1, false ) ;
				assList.add(fl) ;
				RequestUser ll = new RequestUser( ba.getSystemId() , currentRequest.getRequestId() , UserType.SUBSCRIBER , oldLogger.getUserId(), 1, false ) ;
				subList.add(ll) ;
				
			}
			else if( currCcdName.equals(CCR_CCD_APPROVED))
			{
				LOG.info("Nitiraj : CCD = approved") ;
				// go to state 8
				ccrType = Type.lookupBySystemIdAndFieldNameAndTypeName(ba.getSystemId(), CCR_CCR_FIELD_NAME, CCR_CCR_STCPR ) ;
				epmdType = Type.lookupBySystemIdAndFieldNameAndTypeName(ba.getSystemId(), CCR_EPMD_FIELD_NAME, CCR_EPMD_APPROVED ) ;
				
				assList = new ArrayList<RequestUser>() ;
				subList = new ArrayList<RequestUser>() ;
				int firstLog = getFirstLogger( ba.getSystemId(), currentRequest.getRequestId() ) ;				
				RequestUser fl = new RequestUser( ba.getSystemId() , currentRequest.getRequestId() , UserType.ASSIGNEE ,firstLog, 1, false ) ;
				subList.add(fl) ;
				RequestUser ll = new RequestUser( ba.getSystemId() , currentRequest.getRequestId() , UserType.SUBSCRIBER , oldLogger.getUserId(), 1, false ) ;
				subList.add(ll) ;
				
				createReportAndAddNewRequest(ba, oldRequest, currentRequest, extendedFields ) ;
			}
			else
			{
				throw new TBitsException( "Must change the : " + CCR_CCD_FIELD_NAME + " to some value other than " + CCR_CCD_PENDING ) ;
			}			
			
			setState( currentRequest, ccrType, ccdType, epmdType, assList, logList, subList ) ;
		}
		else 
		{
			throw new TBitsException(NOT_ALLOWED) ;
		}
		
	}

	private void execState4(BusinessArea ba, Request oldRequest,
			Request currentRequest, Hashtable<Field, RequestEx> extendedFields,
			User loginUser, User oldAssignee, User oldLogger) throws DatabaseException, TBitsException 
	{	
		LOG.info("Nitiraj : inside state 4") ;
		Type ccrType = null ;
		Type ccdType = null ;
		Type epmdType = null ;
		ArrayList<RequestUser> assList = null ;
		ArrayList<RequestUser> logList = null ;
		ArrayList<RequestUser> subList = null ;

		RequestUser myLogger = new RequestUser(ba.getSystemId(),currentRequest.getRequestId(),UserType.LOGGER, loginUser.getUserId(),1,false) ;
		logList = new ArrayList<RequestUser>() ;
		logList.add(myLogger) ;			
		
		if( loginUser.getUserId() == oldAssignee.getUserId() )
		{
			LOG.info("Nitiraj : logger = oldAssignee") ;
			// go to state 1
			ccrType = Type.lookupAllBySystemIdAndFieldNameAndTypeName(ba.getSystemId(), CCR_CCR_FIELD_NAME, CCR_CCR_PEPMD) ;
			ccdType = Type.lookupBySystemIdAndFieldNameAndTypeName(ba.getSystemId(), CCR_CCD_FIELD_NAME, CCR_CCD_PENDING ) ;
			epmdType = Type.lookupBySystemIdAndFieldNameAndTypeName(ba.getSystemId(), CCR_EPMD_FIELD_NAME, CCR_EPMD_PENDING ) ;
			
			//public RequestUser(int aSystemId, int aRequestId, int aUserTypeId, int aUserId, int aOrdering, boolean aIsPrimary) {
			int assUserId = getUserFromMap(loginUser.getUserId(),CCR_MAP_TYPE_NAME, UserMapManager.TO ) ; 
			RequestUser myAssignee = new RequestUser(ba.getSystemId(),currentRequest.getRequestId(), UserType.ASSIGNEE, assUserId ,1,false) ;
			assList = new ArrayList<RequestUser>() ;
			assList.add(myAssignee) ;
			
			setState( currentRequest, ccrType, ccdType, epmdType, assList, logList, subList ) ;
		}
		else if ( loginUser.getUserId() == oldLogger.getUserId() )
		{
			LOG.info("Nitiraj : logger = oldLogger") ;
			String curEpmdName = (String)currentRequest.myMapFieldToObjects.get(CCR_EPMD_FIELD_NAME) ;
			if( curEpmdName.equals(CCR_EPMD_REJECTED ) ) 
			{
				LOG.info("Nitiraj : epmd = rejected") ;
				// go to state 3
				ccrType = Type.lookupBySystemIdAndFieldNameAndTypeName(ba.getSystemId(), CCR_CCR_FIELD_NAME, CCR_CCR_EPMR ) ;
				ccdType = Type.lookupBySystemIdAndFieldNameAndTypeName(ba.getSystemId(), CCR_CCD_FIELD_NAME, CCR_CCD_PENDING ) ;
				
//				RequestUser myAssignee = new RequestUser(ba.getSystemId(), currentRequest.getRequestId(), UserType.ASSIGNEE, oldLogger.getUserId(), 1, false ) ;
				assList = new ArrayList<RequestUser>() ;
				subList = new ArrayList<RequestUser>() ;
//				assList.add(myAssignee) ;
			}
			else if ( curEpmdName.equals(CCR_EPMD_RECTIFY) ) 
			{
				LOG.info("Nitiraj : epmd = rectify") ;
				// go to state 4
				ccrType = Type.lookupBySystemIdAndFieldNameAndTypeName(ba.getSystemId(), CCR_CCR_FIELD_NAME, CCR_CCR_PFR ) ;
				ccdType = Type.lookupBySystemIdAndFieldNameAndTypeName(ba.getSystemId(), CCR_CCD_FIELD_NAME, CCR_CCD_PENDING ) ;
				
				RequestUser myAssignee = new RequestUser(ba.getSystemId(), currentRequest.getRequestId(), UserType.ASSIGNEE, oldLogger.getUserId(), 1, false ) ;
				assList = new ArrayList<RequestUser>() ;
				assList.add(myAssignee) ;
			}
			else if( curEpmdName.equals(CCR_EPMD_APPROVED ))
			{
				LOG.info("Nitiraj : epmd = approved") ;
				// go to state 5
				ccrType = Type.lookupBySystemIdAndFieldNameAndTypeName(ba.getSystemId(), CCR_CCR_FIELD_NAME, CCR_CCR_PCCD ) ;
				ccdType = Type.lookupBySystemIdAndFieldNameAndTypeName(ba.getSystemId(), CCR_CCD_FIELD_NAME,CCR_CCD_PENDING ) ;
				
				int assUserId = getUserFromMap(loginUser.getUserId(),CCR_CC_MAP_TYPE_NAME, UserMapManager.TO ) ;
				RequestUser myAssignee = new RequestUser( ba.getSystemId() , currentRequest.getRequestId() , UserType.ASSIGNEE , assUserId, 1, false ) ;
				assList = new ArrayList<RequestUser>() ;
				assList.add(myAssignee) ;
				
				RequestUser mySubs = new RequestUser( ba.getSystemId(), currentRequest.getRequestId(), UserType.SUBSCRIBER, oldLogger.getUserId(), 1, false ) ;
				subList = new ArrayList<RequestUser>() ;
				subList.add(mySubs) ;
			}
			else
			{
				throw new TBitsException("Must change the:" + CCR_EPMD_FIELD_NAME + " to some value other than " + CCR_EPMD_PENDING ) ;
			}

			setState( currentRequest, ccrType, ccdType, epmdType, assList, logList, subList ) ;
		}
		else
		{
			throw new TBitsException(NOT_ALLOWED) ;
		}
	}

	private void execState3(BusinessArea ba, Request oldRequest,
			Request currentRequest, Hashtable<Field, RequestEx> extendedFields,
			User loginUser, User oldAssignee, User oldLogger) throws TBitsException, DatabaseException 
	{	
		LOG.info("Nitiraj : inside state 3");
		// how is logged in
		Type ccrType = null ;
		Type ccdType = null ;
		Type epmdType = null ;
		ArrayList<RequestUser> assList = null ;
		ArrayList<RequestUser> logList = null ;
		ArrayList<RequestUser> subList = null ;

		RequestUser myLogger = new RequestUser(ba.getSystemId(),currentRequest.getRequestId(),UserType.LOGGER, loginUser.getUserId(),1,false) ;
		logList = new ArrayList<RequestUser>() ;
		logList.add(myLogger) ;	
		
		if( loginUser.getUserId() == oldLogger.getUserId() )
		{
			LOG.info("Nitiraj : logger = oldLogger") ;
			// validate ... 
			String currEpmdName = (String)currentRequest.myMapFieldToObjects.get(CCR_EPMD_FIELD_NAME) ;
			if(currEpmdName.equals(CCR_EPMD_REJECTED))
			{				
				// do nothing
				LOG.info("Nitiraj : epmd rejected. do nothing") ;
			}
			else if(currEpmdName.equals(CCR_EPMD_RECTIFY))
			{
				LOG.info("Nitiraj : epmd = rectify") ;
				// goto state 4
				ccrType = Type.lookupBySystemIdAndFieldNameAndTypeName(ba.getSystemId(), CCR_CCR_FIELD_NAME, CCR_CCR_PFR ) ;
				ccdType = Type.lookupBySystemIdAndFieldNameAndTypeName(ba.getSystemId(), CCR_CCD_FIELD_NAME, CCR_CCD_PENDING ) ;
				
				int firstLogger = getFirstLogger(ba.getSystemId(), currentRequest.getRequestId()) ;
				RequestUser myAssignee = new RequestUser(ba.getSystemId(), currentRequest.getRequestId(), UserType.ASSIGNEE, firstLogger, 1, false ) ;
				assList = new ArrayList<RequestUser>() ;
				assList.add(myAssignee) ;
			}
			else if( currEpmdName.equals(CCR_EPMD_APPROVED))
			{
				LOG.info("Nitiraj : epmd = approved") ;
				// goto state 5
				ccrType = Type.lookupBySystemIdAndFieldNameAndTypeName(ba.getSystemId(), CCR_CCR_FIELD_NAME, CCR_CCR_PCCD ) ;
				ccdType = Type.lookupBySystemIdAndFieldNameAndTypeName(ba.getSystemId(), CCR_CCD_FIELD_NAME,CCR_CCD_PENDING ) ;
				
				int assUserId = getUserFromMap(loginUser.getUserId(),CCR_CC_MAP_TYPE_NAME, UserMapManager.TO ) ;
				RequestUser myAssignee = new RequestUser( ba.getSystemId() , currentRequest.getRequestId() , UserType.ASSIGNEE , assUserId, 1, false ) ;
				assList = new ArrayList<RequestUser>() ;
				assList.add(myAssignee) ;
				
				int firstLogger = getFirstLogger(ba.getSystemId(), currentRequest.getRequestId()) ;
				RequestUser mySubs = new RequestUser( ba.getSystemId(), currentRequest.getRequestId(), UserType.SUBSCRIBER, firstLogger, 1, false ) ;
				subList = new ArrayList<RequestUser>() ;
				subList.add(mySubs) ;
			}
			else
			{
				throw new TBitsException("Must change the:" + CCR_EPMD_FIELD_NAME + " to some value other than " + CCR_EPMD_PENDING ) ;
			}
			
			setState( currentRequest, ccrType, ccdType, epmdType, assList, logList, subList ) ;			
		}
		else
		{
			throw new TBitsException( NOT_ALLOWED ) ;
		}
	}

	private void execState0(BusinessArea ba, Request oldRequest, Request currentRequest,
			Hashtable<Field, RequestEx> extendedFields, User loginUser, User oldAssignee, User oldLogger ) throws DatabaseException, TBitsException 
	{
			LOG.info("Nitiraj : inside state 0 ") ;
			// TODO : validate inputs ( nothing no state variables should be changed ) 
			Type ccrType = Type.lookupAllBySystemIdAndFieldNameAndTypeName(ba.getSystemId(), CCR_CCR_FIELD_NAME, CCR_CCR_PEPMD) ;
			Type ccdType = Type.lookupBySystemIdAndFieldNameAndTypeName(ba.getSystemId(), CCR_CCD_FIELD_NAME, CCR_CCD_PENDING ) ;
			Type epmdType = Type.lookupBySystemIdAndFieldNameAndTypeName(ba.getSystemId(), CCR_EPMD_FIELD_NAME, CCR_EPMD_PENDING ) ;
			
			//public RequestUser(int aSystemId, int aRequestId, int aUserTypeId, int aUserId, int aOrdering, boolean aIsPrimary) {
			RequestUser myLogger = new RequestUser(ba.getSystemId(),currentRequest.getRequestId(),UserType.LOGGER, loginUser.getUserId(),1,false) ;
			int assUserId = getUserFromMap(loginUser.getUserId(),CCR_MAP_TYPE_NAME, UserMapManager.TO ) ; 
			RequestUser myAssignee = new RequestUser(ba.getSystemId(),currentRequest.getRequestId(), UserType.ASSIGNEE, assUserId ,1,false) ;
			ArrayList<RequestUser> assList = new ArrayList<RequestUser>() ;
			assList.add(myAssignee) ;
			ArrayList<RequestUser> logList = new ArrayList<RequestUser>() ;
			logList.add(myLogger) ;
			ArrayList<RequestUser> subList = new ArrayList<RequestUser>() ;
			// set the values
			setState( currentRequest, ccrType, ccdType, epmdType, assList, logList, subList ) ;	
	}
	
	private void execState1(BusinessArea ba, Request oldRequest, Request currentRequest,
			Hashtable<Field, RequestEx> extendedFields, User loginUser, User oldAssignee, User oldLogger ) throws DatabaseException, TBitsException 
	{
		LOG.info("Nitiraj : inside state 1 ") ;
		// how is logged in
		Type ccrType = null ;
		Type ccdType = null ;
		Type epmdType = null ;
		ArrayList<RequestUser> assList = null ;
		ArrayList<RequestUser> logList = null ;
		ArrayList<RequestUser> subList = null ;

		RequestUser myLogger = new RequestUser(ba.getSystemId(),currentRequest.getRequestId(),UserType.LOGGER, loginUser.getUserId(),1,false) ;
		logList = new ArrayList<RequestUser>() ;
		logList.add(myLogger) ;			
		
		if( loginUser.getUserId() == oldLogger.getUserId() )
		{
			LOG.info("Nitiraj : logger = oldlogger") ;
			// TODO : validate inputs ( only EMPD can be changed ) 
			// go to state 1
			ccrType = Type.lookupAllBySystemIdAndFieldNameAndTypeName(ba.getSystemId(), CCR_CCR_FIELD_NAME, CCR_CCR_PEPMD) ;
			ccdType = Type.lookupBySystemIdAndFieldNameAndTypeName(ba.getSystemId(), CCR_CCD_FIELD_NAME, CCR_CCD_PENDING ) ;
			epmdType = Type.lookupBySystemIdAndFieldNameAndTypeName(ba.getSystemId(), CCR_EPMD_FIELD_NAME, CCR_EPMD_PENDING ) ;
			
			//public RequestUser(int aSystemId, int aRequestId, int aUserTypeId, int aUserId, int aOrdering, boolean aIsPrimary) {
			int assUserId = getUserFromMap(loginUser.getUserId(),CCR_MAP_TYPE_NAME, UserMapManager.TO ) ; 
			RequestUser myAssignee = new RequestUser(ba.getSystemId(),currentRequest.getRequestId(), UserType.ASSIGNEE, assUserId ,1,false) ;
			assList = new ArrayList<RequestUser>() ;
			assList.add(myAssignee) ;
			
			setState( currentRequest, ccrType, ccdType, epmdType, assList, logList, subList ) ;
		}	
		else if( loginUser.getUserId() == oldAssignee.getUserId() )
		{
			LOG.info("Nitiraj : logger = oldAssignee ") ;
			String curEpmdName = (String)currentRequest.myMapFieldToObjects.get(CCR_EPMD_FIELD_NAME) ;
			if( curEpmdName.equals(CCR_EPMD_REJECTED ) ) 
			{
				LOG.info("Nitiraj : epmd = rejected " ) ;
				// go to state 3
				ccrType = Type.lookupBySystemIdAndFieldNameAndTypeName(ba.getSystemId(), CCR_CCR_FIELD_NAME, CCR_CCR_EPMR ) ;
				ccdType = Type.lookupBySystemIdAndFieldNameAndTypeName(ba.getSystemId(), CCR_CCD_FIELD_NAME, CCR_CCD_PENDING ) ;
				
//				RequestUser myAssignee = new RequestUser(ba.getSystemId(), currentRequest.getRequestId(), UserType.ASSIGNEE, oldLogger.getUserId(), 1, false ) ;
				assList = new ArrayList<RequestUser>() ;
				subList = new ArrayList<RequestUser>() ;
//				assList.add(myAssignee) ;
			}
			else if ( curEpmdName.equals(CCR_EPMD_RECTIFY) ) 
			{
				LOG.info("Nitiraj : epmd = rectify" ) ;
				// go to state 4
				ccrType = Type.lookupBySystemIdAndFieldNameAndTypeName(ba.getSystemId(), CCR_CCR_FIELD_NAME, CCR_CCR_PFR ) ;
				ccdType = Type.lookupBySystemIdAndFieldNameAndTypeName(ba.getSystemId(), CCR_CCD_FIELD_NAME, CCR_CCD_PENDING ) ;
				
				RequestUser myAssignee = new RequestUser(ba.getSystemId(), currentRequest.getRequestId(), UserType.ASSIGNEE, oldLogger.getUserId(), 1, false ) ;
				assList = new ArrayList<RequestUser>() ;
				assList.add(myAssignee) ;
			}
			else if( curEpmdName.equals(CCR_EPMD_APPROVED ))
			{
				LOG.info("Nitiraj : epmd = approved " ) ;
				// go to state 5
				ccrType = Type.lookupBySystemIdAndFieldNameAndTypeName(ba.getSystemId(), CCR_CCR_FIELD_NAME, CCR_CCR_PCCD ) ;
				ccdType = Type.lookupBySystemIdAndFieldNameAndTypeName(ba.getSystemId(), CCR_CCD_FIELD_NAME,CCR_CCD_PENDING ) ;
				
				int assUserId = getUserFromMap(loginUser.getUserId(),CCR_CC_MAP_TYPE_NAME, UserMapManager.TO ) ;
				RequestUser myAssignee = new RequestUser( ba.getSystemId() , currentRequest.getRequestId() , UserType.ASSIGNEE , assUserId, 1, false ) ;
				assList = new ArrayList<RequestUser>() ;
				assList.add(myAssignee) ;
				
				RequestUser mySubs = new RequestUser( ba.getSystemId(), currentRequest.getRequestId(), UserType.SUBSCRIBER, oldLogger.getUserId(), 1, false ) ;
				subList = new ArrayList<RequestUser>() ;
				subList.add(mySubs) ;
			}
			else
			{
				throw new TBitsException("Must change the:" + CCR_EPMD_FIELD_NAME + " to some value other than " + CCR_EPMD_PENDING ) ;
			}

			setState( currentRequest, ccrType, ccdType, epmdType, assList, logList, subList ) ;
		}
		else
		{
			throw new TBitsException(NOT_ALLOWED) ;
		}
	}

	private void setState(Request currentRequest, Type ccrType, Type ccdType,
			Type epmdType, ArrayList<RequestUser> assList,
			ArrayList<RequestUser> logList, ArrayList<RequestUser> subList) 
	{
		// set the type values
		if( null != ccrType )
		{
			LOG.info("Nitiraj : changing ccrTye to : " + ccrType.getDisplayName() ) ;
			currentRequest.setSeverityId( ccrType ) ;
		}
			
		if( null != ccdType )
		{
			LOG.info("Nitiraj : changing ccdType to : " + ccdType.getDisplayName() ) ;
			currentRequest.setStatusId(ccdType) ;
		}
		if( null != epmdType )
		{
			LOG.info("Nitiraj : changing epmdType to : " + epmdType.getDisplayName() ) ;
			currentRequest.setRequestTypeId(epmdType) ;
		}
		
		// set user values
		if( null != assList )
		{
			LOG.info("Nitiraj : changing assignee to : " + assList ) ;
			
			currentRequest.setAssignees(assList) ;
		}
		
		if( null != logList )
		{
			LOG.info("Nitiraj : changing loggers to : " + logList ) ;
			currentRequest.setLoggers(logList) ;
		}
		
		if( null != subList ) 
		{
			LOG.info("Nitiraj : changing subscribers to : " + subList ) ;			
			currentRequest.setSubscribers(subList) ;
		}
	}

	private int getRequestState(boolean isAddRequest, User loginUser, User oldLogger,
			User oldAssignee, String ccrName, String ccdName, String epmdName,
			String oldCcrName, String oldCcdName, String oldEpmdName) throws TBitsException 
	{
		// states 0 , 1 , 2, 3 , 4, 5, 6, 7, 8, 9, 10, 11
		if(isAddRequest)
			return 0 ;
		else if( oldCcrName.equals(CCR_CCR_PEPMD) && oldCcdName.equals(CCR_CCD_PENDING) && oldEpmdName.equals(CCR_EPMD_PENDING) )
		{
			return 1 ;			
		}
		// there is no state 2 in the system right now.
		else if( oldCcrName.equals(CCR_CCR_EPMR) && oldCcdName.equals(CCR_CCD_PENDING) && oldEpmdName.equals(CCR_EPMD_REJECTED) )
		{
			return 3 ;			
		}
		else if( oldCcrName.equals(CCR_CCR_PFR) && oldCcdName.equals(CCR_CCD_PENDING ) && oldEpmdName.equals(CCR_EPMD_RECTIFY) )
		{
			return 4 ;			
		}
		else if( oldCcrName.equals(CCR_CCR_PCCD) && oldCcdName.equals(CCR_CCD_PENDING) && oldEpmdName.equals(CCR_EPMD_APPROVED) )
		{
			return 5 ;
		}
		else if( oldCcrName.equals(CCR_CCR_CCR) && oldCcdName.equals(CCR_CCD_REJECTED) && oldEpmdName.equals(CCR_EPMD_APPROVED) )
		{
			return 6 ;
		}
		else if( oldCcrName.equals(CCR_CCR_PFR) && oldCcdName.equals(CCR_CCD_RECTIFY) && oldEpmdName.equals(CCR_EPMD_APPROVED) )
		{
			return 7 ;
		}
		else if( oldCcrName.equals(CCR_CCR_STCPR) && oldCcdName.equals(CCR_CCD_APPROVED) && oldEpmdName.equals(CCR_EPMD_APPROVED))
		{
			return 8 ;
		}
		else if( oldCcrName.equals(CCR_CCR_RSR) && oldCcdName.equals(CCR_CCD_PENDING) && oldEpmdName.equals(CCR_EPMD_PENDING))
		{
			return 9 ;
		}
		else if( oldCcrName.equals(CCR_CCR_DR))
		{
			return 10 ;
		}
		else if (oldCcrName.equals(CCR_CCR_DRCR))
		{
			return 11 ;
		}		
		
		throw new TBitsException( "Illegal State of the request." ) ;
	}

	public String getName() {		
		return name ;
	}

	public double getSequence() {
		// TODO Auto-generated method stub
		return 0;
	}

}
