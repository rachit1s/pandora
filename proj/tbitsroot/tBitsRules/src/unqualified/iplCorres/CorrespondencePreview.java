package iplCorres;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;

import javax.activation.MimetypesFileTypeMap;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import transbit.tbits.api.IProxyServlet;
import transbit.tbits.common.Configuration;
import transbit.tbits.common.DatabaseException;
import transbit.tbits.common.TBitsLogger;
import transbit.tbits.common.Timestamp;
import transbit.tbits.common.Utilities;
import transbit.tbits.config.WebConfig;
import transbit.tbits.domain.BusinessArea;
import transbit.tbits.domain.Field;
import transbit.tbits.domain.Request;
import transbit.tbits.domain.Type;
import transbit.tbits.domain.User;
import transbit.tbits.exception.TBitsException;
import transbit.tbits.webapps.WebUtil;

//import com.oreilly.servlet.multipart.MultipartParser;
//import com.oreilly.servlet.multipart.ParamPart;
//import com.oreilly.servlet.multipart.Part;

/**
 * 
 * @author nitiraj
 *
 */
/**
 * this plugin will handle the received preview request
 */
public class CorrespondencePreview implements IProxyServlet {

	public static final TBitsLogger LOG = TBitsLogger.getLogger("IPLCORRES");
	//private static final String TBITS_BASE_URL_KEY = "tbits_base_url";
//	private static final String MULTIPART_CONTENT_TYPE		= "multipart/form-data";
	// Default Mime Type.
    public static final String DEFAULT_MIME_TYPE = "application/octet-stream";
    
    public static final String MY_NAME = "corr-preview" ;
    public static final String MY_CLASS_NAME = "iplCorres.CorrespondencePreview" ;

    User myLoginUser = null ;
	User myLogger0User = null ;
	User myAss0User = null ;
	
	Hashtable<String,String> myLoginInfo = null ;
	Hashtable<String,String> myLogger0Info = null ;	
	Hashtable<String,String> myAss0Info = null ;
	
	//Hashtable<Integer,ArrayList<String>> myLogger0Map = null ;	
	
	Type myCorrProt = null ;
	Type myCorrType = null ;
	Type myCorrInit = null ;
	Type myPrevCorrProt = null ;
	Type myPrevCorrType = null ;
	Type myPrevCorrInit = null ;
	
	HttpServletRequest myCurrRequest = null ;	
	Request myPrevRequest = null ;
	BusinessArea myBA = null ; 
	boolean isAddRequest ;
	
	String myLoggerList = null ;
	String mySubList = null ;
	String myAssList = null ;
	ArrayList<String> mySubArray = null ;
	
	String myLogger0 = null ;
	String myAss0 = null ;
	String myLogin = null ;
		
	public void resetAllMembers()
	{
		myLoginUser = null ;
		myLogger0User = null ;
		myAss0User = null ;
		
		 myLoginInfo = null ;
		 myLogger0Info = null ;	
		 myAss0Info = null ;
		
		// myLogger0Map = null ;	
		
		 myCorrProt = null ;
		 myCorrType = null ;
		 myCorrInit = null ;
		 myPrevCorrProt = null ;
		 myPrevCorrType = null ;
		 myPrevCorrInit = null ;
		
		 myCurrRequest = null ;	
		 myPrevRequest = null ;
		 myBA = null ; 
				
		 myLoggerList = null ;
		 mySubList = null ;
		 myAssList = null ;
		 mySubArray = null ;
		
		 myLogger0 = null ;
		 myAss0 = null ;
		 myLogin = null ;
	}

	/*
	public void checkCorrRestrictions() throws TBitsException
	{
		// check if assignee0 and subs list contains the minimum requirements
		myLogger0Map = UserMapManager.getMappingAsUserLogin(myLogger0User.getUserId(), myCorrType.getName() )	;
		ArrayList<String> toArray = myLogger0Map.get(UserMapManager.TO) ; 
		if(toArray.size() == 0 ) 
			throw new TBitsException("No assignee mapping found for " + myLogger0User.getUserLogin() ) ;
		
		String tass0 = toArray.get(0) ;
		if( !tass0.equals(myAss0User.getUserLogin()) )
			throw new TBitsException("Illegal assignee field.") ;
		
		ArrayList<String> yArray = myLogger0Map.get(UserMapManager.YOUR_CC) ;
		ArrayList<String> oArray = myLogger0Map.get(UserMapManager.OUR_CC) ;
		HashSet<String> subSet = new HashSet<String>( yArray ) ;
		subSet.addAll(oArray) ;
		
		if(myLoginUser.getUserId() != myLogger0User.getUserId() )		
			subSet.add(KskConstants.KSK_BOSS_LOGIN) ;		
		
		for( Iterator<String> iter = mySubArray.iterator() ; iter.hasNext() ; )
		{
			subSet.remove(iter.next()) ;
		}
		
		subSet.remove(myLogger0User.getUserLogin() ) ;
		subSet.remove(myAss0User.getUserLogin()) ;
		
		if( subSet.size() > 0 ) 
		{
			throw new TBitsException("The subscriber list must also contain : " + subSet.toString() ) ;
		}		
	}
	*/
	public void validateAndSet() throws TBitsException
	{		
		myLogin = myLoginUser.getUserLogin() ;
		
		myLoggerList = (String) myCurrRequest.getParameter(IPLConstants.CORR_LOGGER_FIELD_NAME) ;
		
		ArrayList<String> loggerArray = Utilities.toArrayList(myLoggerList) ;
		if( null == myLoggerList || myLoggerList.trim().equals("") || loggerArray.size() == 0  )
			throw new TBitsException( "Logger field cannot be empty." ) ;
		System.out.println("loggerArray = " + loggerArray );
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
		
		myAssList = (String) myCurrRequest.getParameter(IPLConstants.CORR_ASSIGNEE_FIELD_NAME ) ;
		if(null == myAssList || myAssList.trim().equals(""))
			throw new TBitsException( "Assignee Field cannot be empty.") ;
		
		ArrayList<String> assArray = Utilities.toArrayList(myAssList) ;
		if( assArray.size() == 0 )
			throw new TBitsException("There must be exactly one assignee.") ;
		
		System.out.println("Ass Array : " + assArray );
		myAss0 = assArray.get(0).trim() ;
		if( myAss0.equals("") ) 
			throw new TBitsException("There must be exactly one assignee.") ;
		
		try {
			myAss0User = User.lookupAllByUserLogin(myAss0) ;
		} catch (DatabaseException e) {			
			e.printStackTrace();
			throw new TBitsException("Exception while accesing user(" + myAss0 + ")" ) ;
		}
		if( null == myAss0User )
			throw new TBitsException( "The user with userLogin = " + myAss0 + " not found." ) ;
		//	myAss0Id = myAss0User.getUserId() ;
		
		setMyFields() ;

		// only WPCL member are allowed to log on behalf of somebody else
		try
		{
			myLoginInfo = UserInfoManager.getUserInfo(myLoginUser.getUserId()) ;
		}catch(IllegalArgumentException e )
		{
			throw new TBitsException( "No user info found for (" + myLoginUser.getUserLogin() + ")" ) ;
		}

//		if( !myLoginInfo.get(UserInfoManager.FIRM ).trim().equalsIgnoreCase(KskConstants.WPCL_FIRM_NAME))
//		{
//			if( !myLogger0User.getUserLogin().equals(myLoginUser.getUserLogin()) )
//			{
//				throw new TBitsException("You (" + myLoginUser.getUserLogin() + ") are not allowed to log on behalf of (" + myLogger0User.getUserLogin() + ")" ) ;
//			}
//		}
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
			//String lloc = myLogger0Info.get(UserInfoManager.LOCATION) ;
			if( !( cin.indexOf(lfirm) >= 0 ) )// && cin.substring(cin.trim().length()-2).equals(lloc.trim()) ) )
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
		
		mySubList = (String)myCurrRequest.getParameter(IPLConstants.CORR_SUBSCRIBER_FIELD_NAME) ;
		mySubArray = Utilities.toArrayList(mySubList, ",") ;
		
//		if( myCorrProt.getName().equals(KskConstants.CORR_CORR_PROT_WCPL_SEPCO))
//			checkCorrRestrictions() ;		
	}
	
	public void setMyFields() throws TBitsException 
	{
		String corrProtName = (String) myCurrRequest.getParameter(IPLConstants.CORR_CORRESPONDANCE_PROTOCOL_FIELD_NAME) ;		
		try {
			myCorrProt = Type.lookupAllBySystemIdAndFieldNameAndTypeName(myBA.getSystemId(), IPLConstants.CORR_CORRESPONDANCE_PROTOCOL_FIELD_NAME, corrProtName ) ;
		} catch (DatabaseException e) {			
			e.printStackTrace();
			throw new TBitsException(e) ;
		}
		if( null == myCorrProt )
			throw new TBitsException("The Correspondance Protocol Field was not properly set.") ;
		
		String corrTypeName = (String) myCurrRequest.getParameter(IPLConstants.CORR_TYPE_FIELD_NAME) ;
		try {
			myCorrType = Type.lookupAllBySystemIdAndFieldNameAndTypeName(myBA.getSystemId(), IPLConstants.CORR_TYPE_FIELD_NAME, corrTypeName ) ;
		} catch (DatabaseException e) {
			e.printStackTrace();
			throw new TBitsException(e) ;
		}
		if( null == myCorrType )
			throw new TBitsException("The Correspondance Type Field was not properly set.") ;
		
		String corrInitName = (String) myCurrRequest.getParameter(IPLConstants.CORR_CORRESPONDANCE_INITIATOR_FIELD_NAME) ;
		try {
			myCorrInit = Type.lookupAllBySystemIdAndFieldNameAndTypeName(myBA.getSystemId(), IPLConstants.CORR_CORRESPONDANCE_INITIATOR_FIELD_NAME, corrInitName ) ;
		} catch (DatabaseException e) {
			e.printStackTrace();
			throw new TBitsException(e) ;
		}
		if( null == myCorrInit )
			throw new TBitsException("The Correspondance initiator Field was not properly set.") ;
		
//		if( false == isAddRequest )
//		{
//			String corrPrevProtName = (String) myPrevRequest.myMapFieldToObjects.get(KskConstants.CORR_CORRESPONDANCE_PROTOCOL_FIELD_NAME) ;		
//			try {
//				myPrevCorrProt = Type.lookupAllBySystemIdAndFieldNameAndTypeName(myBA.getSystemId(), KskConstants.CORR_CORRESPONDANCE_PROTOCOL_FIELD_NAME, corrPrevProtName ) ;
//			} catch (DatabaseException e) {			
//				e.printStackTrace();
//				throw new TBitsException(e) ;
//			}
//			if( null == myPrevCorrProt )
//				throw new TBitsException("The Correspondance Protocol Field was not properly set when the request was added.") ;
//			
//			String corrPrevTypeName = (String) myPrevRequest.myMapFieldToObjects.get(KskConstants.CORR_TYPE_FIELD_NAME) ;
//			try {
//				myPrevCorrType = Type.lookupAllBySystemIdAndFieldNameAndTypeName(myBA.getSystemId(), KskConstants.CORR_TYPE_FIELD_NAME, corrPrevTypeName ) ;
//			} catch (DatabaseException e) {
//				e.printStackTrace();
//				throw new TBitsException(e) ;
//			}
//			if( null == myPrevCorrType )
//				throw new TBitsException("The Correspondance Type Field was not properly set when the request was added.") ;
//			
//			String corrPrevInitName = (String) myPrevRequest.myMapFieldToObjects.get(KskConstants.CORR_CORRESPONDANCE_INITIATOR_FIELD_NAME) ;
//			try {
//				myPrevCorrInit = Type.lookupAllBySystemIdAndFieldNameAndTypeName(myBA.getSystemId(), KskConstants.CORR_CORRESPONDANCE_INITIATOR_FIELD_NAME, corrPrevInitName ) ;
//			} catch (DatabaseException e) {
//				e.printStackTrace();
//				throw new TBitsException(e) ;
//			}
//			if( null == myPrevCorrInit )
//				throw new TBitsException("The Correspondance Initiator Field was not properly set when the request was added.") ;
//		}
	}

	/**
	 * STEPS.
	 * this method will retrive the correspondence pdf file from the given location and will send the 
	 * embeded html for preview 
	 */
	public void doGet(HttpServletRequest request, HttpServletResponse response)throws IOException, ServletException
	{
		// get the file name 
		// read the file from the tmp folder 
		// send the file
        HttpSession session = request.getSession();
		ServletOutputStream out = response.getOutputStream();
	
		String fileName = request.getParameter("filename") ;
		if( fileName == null )
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Malformed URL") ;
            
		// obtain the File object
		String birt_home = Configuration.findPath("birt-runtime/ReportEngine").getAbsolutePath();
		String file_path = birt_home + "/tmp/" + fileName ;
		File outFile = new File(file_path) ;
		if( outFile == null )
			response.sendError(HttpServletResponse.SC_NOT_FOUND, "The file with name (" + fileName + ") was not found.") ;
		
		// Set the content-disposition field.
	        String contentDisposition = "fileName=\"" + fileName + "\"";
	
	        //
	        // Check if this is a request to save the attachment instead of opening
	        // it directly.
	        //
	            
	       response.setHeader("Content-Disposition", contentDisposition);
	
	        // The user has permissions to view the attachment.
	        // Open the file.
	        
	        String mimeType = getMimeType(outFile);
	
	     //   System.out.println( " NITIRAJ : mimetype = " + mimeType ) ;
	        if ((mimeType != null) && (mimeType.trim().equals("") == false)) {
	            response.setContentType(mimeType);
	        } else {
	            mimeType = DEFAULT_MIME_TYPE;
	        }
	
	        LOG.info("mimeType = " + mimeType );
	        System.out.println( "mimeType = " + mimeType ) ;
	        try {
	
	            //
	            // Open an inputstream to read the attachment 1MB at a time and
	            // output the same.
	            //
	            FileInputStream fis  = new FileInputStream(outFile);
	            int             size = 1024 * 1024;
	            byte[]          b    = new byte[size];
	            int             read = 0;
	
	            while ((read = fis.read(b, 0, size)) > 0) {
	                out.write(b, 0, read);
	            }
	
	            fis.close();
	            // now delete the file as it will not be used later
	            outFile.delete() ;
	            // Flush the output.
		        
				out.flush();
	        } catch (FileNotFoundException fnfe) {
	        	fnfe.printStackTrace() ;
	        	response.sendError(HttpServletResponse.SC_NOT_FOUND, "The file with name (" + fileName + ") was not found.") ;
	          //  throw new TBitsException("Requested file not found.");
	        } catch (Exception e) {
	        	e.printStackTrace() ;
	        	response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Cannot complete your request. Please try again or contact tBits team.") ;
	           // throw new TBitsException(e.toString());
	        }	       
	}
	
	private String getMimeType(File file)
    {
    	File f = Configuration.findPath("etc/mimetypes.default");
    	String path = f.getAbsolutePath();
    	MimetypesFileTypeMap mtftm;
		try {
			mtftm = new MimetypesFileTypeMap(path);
		} catch (IOException e) {
			LOG.warn("file '"+ path +"' not found. Using the default mimetypes from activation.jar");
			mtftm = new MimetypesFileTypeMap();
		}
    	String mimeType = mtftm.getContentType(file);
    	if((mimeType == null) || (mimeType.length() == 0))
    		mimeType = DEFAULT_MIME_TYPE;
    	return mimeType;
    }
	/**  
	 *	STEPS:
	 *	1. the user will click on preview before submitting the request 
	 *  2. ProxyServlet ( or other ) calls this doPost
	 *  3. validate parameters
	 *  4. call TBitsReportEngine to create the report	
	 * @throws TBitsException TODO: how to handle errors and exception ???   
	 */
	public void doPost(HttpServletRequest request, HttpServletResponse response)  throws IOException, ServletException
	{
		// TODO: assuming all the users in cc, subscribers, logger, assignee are userlogins
		// parameters to set
		
		// TODO : check if all the values are available and not null 
		try
		{			
			User login_user = null;
			login_user = WebUtil.validateUser(request);
	        WebConfig userConfig = login_user.getWebConfigObject();
	        
	        resetAllMembers() ;
	        
	        myLoginUser = login_user ;
	        myCurrRequest = request ;
			myBA = BusinessArea.lookupBySystemPrefix(IPLConstants.CORR_SYSPREFIX) ;
			
			String previewAction = request.getParameter("previewAction") ;
			if( previewAction != null )
			{
				if( previewAction.trim().equalsIgnoreCase("update-request"))
				{
//					Hashtable paramInfo = WebUtil.getRequestParams(request, userConfig, WebUtil.VIEW_REQUEST);
//			        myPrevRequest   = (Request) paramInfo.get(Field.REQUEST);
					int requestID = Integer.parseInt(request.getParameter("requestId")) ;
					myPrevRequest = Request.lookupBySystemIdAndRequestId(myBA.getSystemId(), requestID ) ;
			        isAddRequest = false ;
				}
				else 
				{
					isAddRequest = true ;
				}
			}
			
			try
			{
				validateAndSet() ;
			}catch( TBitsException e )
			{
				response.sendError(response.SC_BAD_REQUEST, e.getDescription() ) ;
				 return ;
			}
			catch( Exception e )
			{
				response.sendError(response.SC_BAD_REQUEST, e.getMessage() ) ;
				 return ;
			}
			
			Hashtable<String, String> params = new Hashtable<String,String>() ;
		
			params.put(IPLConstants.REP_TO, GenCorresHelper.getTo(myAss0Info) ) ;
			params.put(IPLConstants.REP_KIND_ATT, GenCorresHelper.getKindAtt(myAss0User , myAss0Info ) ) ;
//			String dear = "Dear " + ( assignee_sex.trim().equalsIgnoreCase("M") ? "Sir," : "Madam," );
			params.put(IPLConstants.REP_DEAR, GenCorresHelper.getDear(myAss0Info) ) ;
			params.put(IPLConstants.REP_TITLE, myCurrRequest.getParameter(Field.SUBJECT)) ;
			params.put(IPLConstants.REP_DESCRIPTION, myCurrRequest.getParameter(Field.DESCRIPTION)) ;			
			params.put(IPLConstants.REP_COMPANY, GenCorresHelper.getCompany( myLogger0Info ) ) ;
			params.put(IPLConstants.REP_LOGGER, GenCorresHelper.getLogger( myLogger0User ) ) ;
			params.put(IPLConstants.REP_DESIGNATION, GenCorresHelper.getDesignation( myLogger0Info ) ) ;			
			// creating cc_list 
			String ccs = GenCorresHelper.getCCs(myCurrRequest.getParameter(IPLConstants.CORR_SUBSCRIBER_FIELD_NAME)) ;
			params.put(IPLConstants.REP_CC, ccs ) ;
			//TODO :changes required here
			String attachList = GenCorresHelper.getAttachList( myCurrRequest.getParameter(Field.ATTACHMENTS), myPrevRequest ) ;
			
//				attachList = GenCorresHelper.getAttachList(, myPrevRequest. )
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
       //     reportName = loggerFirm.trim().toUpperCase() + reportName ;		
			// TODO :set report parameters
			HashMap<String,String> reportParamMap = new HashMap<String,String>() ;
		//	String rid = "0" ;

//			if( previewAction != null )
//			{
			if( isAddRequest )
			{
				reportParamMap.put(IPLConstants.REP_RID, "0") ;
			}
			else 
			{
				String reqID = request.getParameter("requestId") ;
				reportParamMap.put(IPLConstants.REP_RID, reqID ) ;
		//		reportParamMap.put(KskConstants.REP_RID, Integer.toString(myPrevRequest.getRequestId()) ) ;
			}
//			}
//			else reportParamMap.put(KskConstants.REP_RID, "0") ;

			
		//	reportParamMap.put(KskConstants.REP_RID, rid  ) ;			
			String tbits_base_url = WebUtil.getNearestPath("") ; // PropertiesHandler.getProperty(TBitsPropEnum.KEY_NEAREST_INSTANCE)
			System.out.println( "tbits_base_url : " + tbits_base_url ) ;
			reportParamMap.put(IPLConstants.REP_TBITS_BASE_URL_KEY, tbits_base_url );

			String format = request.getParameter("format") ;
			if( null == format )
				format = "pdf" ;			
			File pdfFile = GenCorresHelper.generateReport( reportName, params, reportParamMap, format ) ;
			if( null == pdfFile )
				throw new TBitsException("Unexpected Exception occurred while generating report.") ;
			// sent the file name of this pdf
			PrintWriter out     = response.getWriter();
			out.println( pdfFile.getName() ) ;
		}
		catch(TBitsException e )
		{
			e.printStackTrace() ;
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getDescription() ) ;
			return ;
		}
		catch( Exception e )
		{
			e.printStackTrace() ;
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage() ) ;
			return ;
		}
	}

	public String getName() {
		// TODO Auto-generated method stub
		return MY_NAME;
	}	
	
}
