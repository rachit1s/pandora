package iplCorres;

import java.io.File;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;

import transbit.tbits.Helper.TBitsConstants;
import transbit.tbits.api.AttachmentInfo;
import transbit.tbits.api.IRule;
import transbit.tbits.api.RuleResult;
import transbit.tbits.common.DataSourcePool;
import transbit.tbits.common.DatabaseException;
import transbit.tbits.common.TBitsLogger;
import transbit.tbits.common.Timestamp;
import transbit.tbits.common.Uploader;
import transbit.tbits.common.Utilities;
import transbit.tbits.domain.BusinessArea;
import transbit.tbits.domain.Field;
import transbit.tbits.domain.Request;
import transbit.tbits.domain.RequestEx;
import transbit.tbits.domain.Type;
import transbit.tbits.domain.User;
import transbit.tbits.exception.TBitsException;
import transbit.tbits.webapps.WebUtil;

public class CorresAddPreRule implements IRule 
{
	public static final String ERR_CORR_GEN = "Exception occured : Correspondance file cannot be generated" +
						"<br> Please uncheck the generate correspondance checkbox.";
	public static final TBitsLogger LOG = TBitsLogger.getLogger("IPLCORRES");
		
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
	
	String getCorrNo(Connection con, Hashtable<String,String> logger0Info, Hashtable<String,String> ass0Info, Type corrType ) throws SQLException
	{	
		// Origination-Party/Project-Code/Phase/Recipient-Party/C/Serial-No
		// The running serial number is on the basis of the following string: Origination-Party/Project-Code/Phase/Recipient-Party/C/
 
		String corresNo = logger0Info.get(UserInfoManager.FIRM).toUpperCase() + "/"+IPLConstants.PROJECT_CODE+"/" +IPLConstants.PHASE+"/" + ass0Info.get(UserInfoManager.FIRM).toUpperCase() +"/C" ;		
//		corresNo += corrType.getName() ;
		// generate complete correspondence no.
		int ncid = getNextCorrNo(con, corresNo ) ;
		String nextCorresId =  Integer.toString(ncid);  //Integer.toString(fc.getMaxRequestId() + 1);
		switch( nextCorresId.length() )
		{
			case 1 : nextCorresId = "000" + nextCorresId ; break ;
			case 2 : nextCorresId = "00" + nextCorresId ; break ;
			case 3 : nextCorresId = "0" + nextCorresId ; break ;
		}
		
		// get year 
//		String yr = new Timestamp().toCustomFormat("yy") ;
		corresNo += "/" + nextCorresId ;
		
		return corresNo ;
	}
	// TODO :write a prerule to validate the input-request so that proper report can be generated in this post rule
	/**
	 * STEPS : for ba = "CORR" and when generate correspondence is true ( both for add request and update request )  
	 * 1. generate the correspondence no.
	 * 2. generate the correspondence file 
	 * 3. rename the correspondence file 
	 * 4. attach the correspondence file 
	 * 5. add a request to the FC business Area
	 */
	public RuleResult execute(Connection connection, BusinessArea ba,
			Request oldRequest, Request currentRequest, int Source, User user,
			Hashtable<Field, RequestEx> extendedFields, boolean isAddRequest, Collection<AttachmentInfo> attachments) 
	{	
		if( (null == ba) || (ba.getSystemPrefix() == null) )  
			return new RuleResult( false , "The supplied ba was null.", true ) ;
		
		if( !ba.getSystemPrefix().equals(IPLConstants.CORR_SYSPREFIX) ) 
			return new RuleResult( true, "Skipping : " + getName() + ", because the ba is not : " + IPLConstants.CORR_SYSPREFIX , true ) ;
		
		// if it is an email reply then set the GenerateCorrespondence to false		
		try
		{
			Boolean genCor = currentRequest.getExBoolean(IPLConstants.CORR_GENERATE_CORRESPONDANCE_FIELD_NAME) ;
			
			if( TBitsConstants.SOURCE_EMAIL == Source )
			{
				// put the genCor to false
				genCor = new Boolean(false) ;
			}
			
			if( null == genCor || !genCor  )
				return new RuleResult(true, "Skipping : " + getName()  + ", because the " +  IPLConstants.CORR_GENERATE_CORRESPONDANCE_FIELD_NAME + " field is not true." , true ) ;
		}
		catch(Exception e )
		{
			e.printStackTrace() ;
			return new RuleResult(false, "Cannot Continue : " + getName() + " : because exception occured while accesing the field : " + IPLConstants.CORR_GENERATE_CORRESPONDANCE_FIELD_NAME , true ) ;
		}
		
		// test if the correpondance protocol is WPCL-SEPCO ? yes : check permission ; no : go ahead		
			User myLoginUser = null ;
			User myLogger0User = null ;
			User myAss0User = null ;
			
			Hashtable<String,String> myLoginInfo = null ;
			Hashtable<String,String> myLogger0Info = null ;	
			Hashtable<String,String> myAss0Info = null ;
			
		//	Hashtable<Integer,ArrayList<String>> myLogger0Map = null ;	
			
			Type myCorrProt = null ;
			Type myCorrType = null ;
			Type myCorrInit = null ;
			Type myPrevCorrProt = null ;
			Type myPrevCorrType = null ;
			Type myPrevCorrInit = null ;
			
			Request myCurrRequest = null ;	
			Request myPrevRequest = null ;
			BusinessArea myBA = null ; 
//			boolean isAddRequest ;
			
			String myLoggerList = null ;
			String mySubList = null ;
			String myAssList = null ;
			ArrayList<String> mySubArray = null ;
			
			String myLogger0 = null ;
			String myAss0 = null ;
			String myLogin = null ;
			
			myLoginUser = user ;
			myCurrRequest = currentRequest ;
			myPrevRequest = oldRequest ;
			myBA = ba ;
			
			try
			{
				// check if logger has mistakenly added a file in correspondance file folder then it should he should be asked to remove it
				Field field = Field.lookupBySystemIdAndFieldName(ba.getSystemId(), IPLConstants.CORR_CORRESPONDANCE_FILE_FIELD_NAME) ;
				RequestEx requestex = extendedFields.get(field) ;
				String curratt = requestex.getTextValue() ;
				System.out.println("Pre rule : curratt : " + curratt );
				
				ArrayList<String> attList = new ArrayList<String>() ;
				
				if( !isAddRequest )
				{
					String oldatt = oldRequest.getExString(IPLConstants.CORR_CORRESPONDANCE_FILE_FIELD_NAME) ;
					System.out.println("Pre rule : oldatt : " + oldatt );
					attList = GenCorresHelper.getAttachListForRequest( curratt, oldatt ) ;
				}
				else
				{
					attList = GenCorresHelper.getAttachListForRequest( curratt , null ) ;
				}
				
				System.out.println("Prerule : my corrAttach list : " + attList );
				if( attList.size() > 0 )
				{
					String attachList = "<br />" ;
					for( Iterator<String> i = attList.iterator() ; i.hasNext() ; )
						attachList += i.next() + "<br />" ;
					
					throw new TBitsException("You are not allowed to change files in Correspondance File attachment field.\n Please upload files in Other Attachments" ) ;
				}
				
					
				myLogin = myLoginUser.getUserLogin() ;
				
				myLoggerList = (String) myCurrRequest.myMapFieldToObjects.get(IPLConstants.CORR_LOGGER_FIELD_NAME) ;
				
				ArrayList<String> loggerArray = Utilities.toArrayList(myLoggerList) ;
				if( null == myLoggerList || myLoggerList.trim().equals("") || loggerArray.size() == 0  )
					throw new TBitsException( "Logger field cannot be empty." ) ;
			
				if( loggerArray.size() > 1 )
					throw new TBitsException("More than one logger is not allowed.") ;
				
				myLogger0 = loggerArray.get(0).trim() ;
				if( myLogger0.equals(""))
					throw new TBitsException( "Illegal value in logger Field.") ;
				try {
					myLogger0User = User.lookupAllByUserLogin(myLogger0) ;
				} catch (DatabaseException e) {
					e.printStackTrace();
					throw new TBitsException("Exception while accesing user(" + myLogger0 + ")" ) ;
				}
				if( null == myLogger0User )
					throw new TBitsException( "The user with userLogin = " + myLogger0 + " not found." ) ;
			//	myLogger0Id = myLogger0User.getUserId() ;
				
				myAssList = (String) myCurrRequest.myMapFieldToObjects.get(IPLConstants.CORR_ASSIGNEE_FIELD_NAME ) ;
				if(null == myAssList || myAssList.trim().equals(""))
					throw new TBitsException( "Assignee Field cannot be empty.") ;
				
				ArrayList<String> assArray = Utilities.toArrayList(myAssList) ;
				if( assArray.size() > 1 || assArray.size() == 0 )
					throw new TBitsException("There must be exactly on assignee.") ;
				
				myAss0 = assArray.get(0).trim() ;
				if( myAss0.equals("") ) 
					throw new TBitsException("There must be exactly on assignee.") ;
				
				try {
					myAss0User = User.lookupAllByUserLogin(myAss0) ;
				} catch (DatabaseException e) {			
					e.printStackTrace();
					throw new TBitsException("Exception while accesing user(" + myAss0 + ")" ) ;
				}
				if( null == myAss0User )
					throw new TBitsException( "The user with userLogin = " + myAss0 + " not found." ) ;
				//	myAss0Id = myAss0User.getUserId() ;
				
				//setMyFields() ;
				
				String corrProtName = (String) myCurrRequest.myMapFieldToObjects.get(IPLConstants.CORR_CORRESPONDANCE_PROTOCOL_FIELD_NAME) ;		
				try {
					myCorrProt = Type.lookupAllBySystemIdAndFieldNameAndTypeName(myBA.getSystemId(), IPLConstants.CORR_CORRESPONDANCE_PROTOCOL_FIELD_NAME, corrProtName ) ;
				} catch (DatabaseException e) {			
					e.printStackTrace();
					throw new TBitsException(e) ;
				}
				if( null == myCorrProt )
					throw new TBitsException("The Correspondance Protocol Field was not properly set.") ;
				
				String corrTypeName = (String) myCurrRequest.myMapFieldToObjects.get(IPLConstants.CORR_TYPE_FIELD_NAME) ;
				try {
					myCorrType = Type.lookupAllBySystemIdAndFieldNameAndTypeName(myBA.getSystemId(), IPLConstants.CORR_TYPE_FIELD_NAME, corrTypeName ) ;
				} catch (DatabaseException e) {
					e.printStackTrace();
					throw new TBitsException(e) ;
				}
				if( null == myCorrType )
					throw new TBitsException("The Correspondance Type Field was not properly set.") ;
				
				String corrInitName = (String) myCurrRequest.myMapFieldToObjects.get(IPLConstants.CORR_CORRESPONDANCE_INITIATOR_FIELD_NAME) ;
				try {
					myCorrInit = Type.lookupAllBySystemIdAndFieldNameAndTypeName(myBA.getSystemId(), IPLConstants.CORR_CORRESPONDANCE_INITIATOR_FIELD_NAME, corrInitName ) ;
				} catch (DatabaseException e) {
					e.printStackTrace();
					throw new TBitsException(e) ;
				}
				if( null == myCorrInit )
					throw new TBitsException("The Correspondance initiator Field was not properly set.") ;
				
				if( false == isAddRequest )
				{
					String corrPrevProtName = (String) myPrevRequest.myMapFieldToObjects.get(IPLConstants.CORR_CORRESPONDANCE_PROTOCOL_FIELD_NAME) ;		
					try {
						myPrevCorrProt = Type.lookupAllBySystemIdAndFieldNameAndTypeName(myBA.getSystemId(), IPLConstants.CORR_CORRESPONDANCE_PROTOCOL_FIELD_NAME, corrPrevProtName ) ;
					} catch (DatabaseException e) {			
						e.printStackTrace();
						throw new TBitsException(e) ;
					}
					if( null == myPrevCorrProt )
						throw new TBitsException("The Correspondance Protocol Field was not properly set when the request was added.") ;
					
					String corrPrevTypeName = (String) myPrevRequest.myMapFieldToObjects.get(IPLConstants.CORR_TYPE_FIELD_NAME) ;
					try {
						myPrevCorrType = Type.lookupAllBySystemIdAndFieldNameAndTypeName(myBA.getSystemId(), IPLConstants.CORR_TYPE_FIELD_NAME, corrPrevTypeName ) ;
					} catch (DatabaseException e) {
						e.printStackTrace();
						throw new TBitsException(e) ;
					}
					if( null == myPrevCorrType )
						throw new TBitsException("The Correspondance Type Field was not properly set when the request was added.") ;
					
					String corrPrevInitName = (String) myPrevRequest.myMapFieldToObjects.get(IPLConstants.CORR_CORRESPONDANCE_INITIATOR_FIELD_NAME) ;
					try {
						myPrevCorrInit = Type.lookupAllBySystemIdAndFieldNameAndTypeName(myBA.getSystemId(), IPLConstants.CORR_CORRESPONDANCE_INITIATOR_FIELD_NAME, corrPrevInitName ) ;
					} catch (DatabaseException e) {
						e.printStackTrace();
						throw new TBitsException(e) ;
					}
					if( null == myPrevCorrInit )
						throw new TBitsException("The Correspondance Initiator Field was not properly set when the request was added.") ;
				}

				// only WPCL member are allowed to log on behalf of somebody else
				try
				{
					myLoginInfo = UserInfoManager.getUserInfo(myLoginUser.getUserId()) ;
				}catch(IllegalArgumentException e )
				{
					throw new TBitsException( "No user info found for (" + myLoginUser.getUserLogin() + ")" ) ;
				}

//				if( !myLoginInfo.get(UserInfoManager.FIRM ).trim().equals(KskConstants.WPCL_FIRM_NAME))
//				{
//					if( !myLogger0User.getUserLogin().equals(myLoginUser.getUserLogin()) )
//					{
//						throw new TBitsException("You (" + myLoginUser.getUserLogin() + ") are not allowed to log on behalf of (" + myLogger0User.getUserLogin() + ")" ) ;
//					}
//				}
				try
				{
					myLogger0Info = UserInfoManager.getUserInfo(myLogger0User.getUserId()) ;
				}catch(IllegalArgumentException e )
				{
					throw new TBitsException( "No user info found for (" + myLogger0User.getUserLogin() + ")" ) ;
				}
				
				// corrInit type 
				if( isAddRequest )
				{
					String cin = myCorrInit.getName() ;
					String lfirm = myLogger0Info.get(UserInfoManager.FIRM) ;
				//	String lloc = myLogger0Info.get(UserInfoManager.LOCATION) ;
					if( !( cin.indexOf(lfirm) >= 0  ) ) //&& cin.substring(cin.trim().length()-2).equals(lloc.trim()) ) )
					{
						throw new TBitsException("Correpondance Initiator field was not correctly set.") ;
					}
				}
				
				try
				{
					myAss0Info = UserInfoManager.getUserInfo(myAss0User.getUserId()) ;
				}catch(IllegalArgumentException e )
				{
					throw new TBitsException( "No user info found for (" + myAss0User.getUserLogin() + ")" ) ;
				}
				
				mySubList = (String)myCurrRequest.myMapFieldToObjects.get(IPLConstants.CORR_SUBSCRIBER_FIELD_NAME) ;
				mySubArray = Utilities.toArrayList(mySubList, ",") ;
				
//				if( myCorrProt.getName().equals(KskConstants.CORR_CORR_PROT_WCPL_SEPCO))
//				{
//					//checkCorrRestrictions() ;
//					// check if assignee0 and subs list contains the minimum requirements
//					myLogger0Map = UserMapManager.getMappingAsUserLogin(myLogger0User.getUserId(), myCorrType.getName() )	;
//					ArrayList<String> toArray = myLogger0Map.get(UserMapManager.TO) ; 
//					if(toArray.size() == 0 ) 
//						throw new TBitsException("No assignee mapping found for " + myLogger0User.getUserLogin() ) ;
//					
//					String tass0 = toArray.get(0) ;
//					if( !tass0.equals(myAss0User.getUserLogin()) )
//						throw new TBitsException("Illegal assignee field.") ;
//					
//					ArrayList<String> yArray = myLogger0Map.get(UserMapManager.YOUR_CC) ;
//					ArrayList<String> oArray = myLogger0Map.get(UserMapManager.OUR_CC) ;
//					HashSet<String> subSet = new HashSet<String>( yArray ) ;
//					subSet.addAll(oArray) ;
//					
//					if(myLoginUser.getUserId() != myLogger0User.getUserId() )		
//						subSet.add(KskConstants.KSK_BOSS_LOGIN) ;		
//					
//					for( Iterator<String> iter = mySubArray.iterator() ; iter.hasNext() ; )
//					{
//						subSet.remove(iter.next()) ;
//					}
//					
//					subSet.remove(myLogger0User.getUserLogin() ) ;
//					subSet.remove(myAss0User.getUserLogin()) ;
//					
//					if( subSet.size() > 0 ) 
//					{
//						throw new TBitsException("The subscriber list must also contain : " + subSet.toString() ) ;
//					}
//				}

			}catch( TBitsException e )
			{
				return new RuleResult( false , e.getDescription() , false ) ;
			}
			catch( Exception e )
			{
				return new RuleResult( false , e.getMessage() , false ) ;
			}
		
			Connection con = null ;
		try 
		{		
		    // for both add-request and update-request		
			con = DataSourcePool.getConnection() ;
			con.setAutoCommit(false) ; // so that if exception occurs the nextCorrespondance no. is not affected 
			// now generate the correspondence file 
			// get all js parameters
			Hashtable<String, String> params = new Hashtable<String,String>() ;
			String corresNo = getCorrNo(con, myLogger0Info, myAss0Info, myCorrType ) ;
			params.put(IPLConstants.REP_CURRENTCORRESPONDENCE_NO, corresNo ) ; 

			params.put(IPLConstants.REP_TO, GenCorresHelper.getTo(myAss0Info) ) ;
			params.put(IPLConstants.REP_KIND_ATT, GenCorresHelper.getKindAtt(myAss0User , myAss0Info ) ) ;
//			String dear = "Dear " + ( assignee_sex.trim().equalsIgnoreCase("M") ? "Sir," : "Madam," );
			params.put(IPLConstants.REP_DEAR, GenCorresHelper.getDear(myAss0Info) ) ;
			// TODO : assumption : the address is in html format i.e. using <br /> instead of endline
			
			params.put(IPLConstants.REP_TITLE, currentRequest.get(Field.SUBJECT)) ;
			params.put(IPLConstants.REP_DESCRIPTION, currentRequest.get(Field.DESCRIPTION)) ;			
			params.put(IPLConstants.REP_COMPANY, GenCorresHelper.getCompany( myLogger0Info ) ) ;
			params.put(IPLConstants.REP_LOGGER, GenCorresHelper.getLogger( myLogger0User ) ) ;
			params.put(IPLConstants.REP_DESIGNATION, GenCorresHelper.getDesignation( myLogger0Info ) ) ;			
			// creating cc_list 
			String ccs = GenCorresHelper.getCCs(currentRequest.get(IPLConstants.CORR_SUBSCRIBER_FIELD_NAME)) ;
			params.put(IPLConstants.REP_CC, ccs ) ;
			ArrayList<String> attList = new ArrayList<String>() ;
			
			if( !isAddRequest )
				attList = GenCorresHelper.getAttachListForRequest( currentRequest.get(Field.ATTACHMENTS), oldRequest.get(Field.ATTACHMENTS) ) ;
			else
				attList = GenCorresHelper.getAttachListForRequest( currentRequest.get(Field.ATTACHMENTS), null ) ;
			
			String attachList = "<br />" ; 
			for( Iterator<String> i = attList.iterator() ; i.hasNext() ; )
				attachList += i.next() + "<br />" ;
			
			params.put( IPLConstants.REP_ATTACHMENT, attachList ) ;
			
//			String imageName = myLogger0User.getUserLogin() + ".gif" ;
//			System.out.println( "imagename = " + imageName ) ;
//			File imageFile = Configuration.findPath("tbitsreports/" + imageName);			
//			if( imageFile != null )
//			{
//				String imageLocation = imageFile.getAbsolutePath() ;
//				params.put(KskConstants.REP_IMAGE_PATH, imageLocation ) ;
//			}
			
//			String currentDate = new Timestamp().toCustomFormat("yyyy-MM-dd") ;
//			params.put(KskConstants.REP_CURRENT_DATE, currentDate) ;

			String loggerFirm = myLogger0Info.get(UserInfoManager.FIRM) ;
			String reportName = "Indiabulls_Correspondence.rptdesign" ;
          //  reportName = loggerFirm.trim().toUpperCase() + reportName ;		
			// TODO :set report parameters
			HashMap<String,String> reportParamMap = new HashMap<String,String>() ;
			String rid = Integer.toString(currentRequest.getRequestId()) ;
			reportParamMap.put(IPLConstants.REP_RID, rid  ) ;			
			String tbits_base_url = WebUtil.getNearestPath("") ; // PropertiesHandler.getProperty(TBitsPropEnum.KEY_NEAREST_INSTANCE)
			System.out.println( "tbits_base_url : " + tbits_base_url ) ;
			reportParamMap.put(IPLConstants.REP_TBITS_BASE_URL_KEY, tbits_base_url );

			String format = "pdf";			
			File pdfFile = GenCorresHelper.generateReport( reportName, params, reportParamMap, format ) ;
			////////////////////////////////////
			if( pdfFile == null ) 
			{
				con.rollback() ;
				return new RuleResult( false , "Cannot Generate the Correspondance File.", false ) ;
				// throw new TBitsException( "Cannot Generate the Correspondance File." ) ;
			}
			else
			{				
				// upload this file
				// generate display name this file
				String displayName = /*new Timestamp().toCustomFormat("yyyyMMdd") + "_" + */ corresNo.replace('/', '_') + ".pdf";
									
				int requestId = currentRequest.getRequestId() ;
				int actionId = currentRequest.getMaxActionId() ;
				String prefix = ba.getSystemPrefix() ;
				Uploader up = new Uploader( requestId, actionId, prefix ) ;
				AttachmentInfo atinfo = up.moveIntoRepository(pdfFile) ;
				// change display name 
				atinfo.name = displayName ;

				Field field = Field.lookupBySystemIdAndFieldName(ba.getSystemId(), IPLConstants.CORR_CORRESPONDANCE_FILE_FIELD_NAME) ;
				// TODO: check field null 
				RequestEx requestex = extendedFields.get(field) ;

				ArrayList<AttachmentInfo> attachArray = new ArrayList<AttachmentInfo>() ;// arrayList is a Collection
				attachArray.add(atinfo) ;
				String newJson = AttachmentInfo.toJson(attachArray) ;
				requestex.setTextValue(newJson ) ;
					
				// set the correspondance no. 
				RequestEx corrNoRE = extendedFields.get(Field.lookupBySystemIdAndFieldName(ba.getSystemId(), IPLConstants.CORR_CORRESPONDANCE_NUMBER_FIELD)) ;
				corrNoRE.setVarcharValue(corresNo) ;
				
				con.commit() ;
				
				return new RuleResult( true , "CorresAddPreRule finished Successfully." , true ) ;
			}				
		}
		catch( Exception e ) 
		{
			e.printStackTrace();
			try 
			{	if( null != con )
					con.rollback() ;
			} catch (SQLException e1) {
				LOG.severe("Exception while connection rollback. May lead to corrupted Correspondence Number.") ;
				e1.printStackTrace();
			}
			
			return new RuleResult( false , e.getMessage() , false ) ;
		}
		finally
		{
			if( null != con )
				try {
					con.close() ;
				} catch (SQLException e) 
				{
					e.printStackTrace();
					LOG.error("Connection to the database cannot be closed.") ;
				}			
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
