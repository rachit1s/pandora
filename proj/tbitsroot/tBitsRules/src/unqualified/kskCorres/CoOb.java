package kskCorres;

import static kskCorres.GenCorresHelper.getAttachmentJsonForField;
import static kskCorres.KskConstants.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;

import javax.servlet.http.HttpServletRequest;

import transbit.tbits.api.AttachmentInfo;
import transbit.tbits.common.DatabaseException;
import transbit.tbits.common.Utilities;
import transbit.tbits.domain.BusinessArea;
import transbit.tbits.domain.Field;
import transbit.tbits.domain.Request;
import transbit.tbits.domain.Type;
import transbit.tbits.domain.User;
import transbit.tbits.exception.TBitsException;
import transbit.tbits.webapps.WebUtil;

import commons.com.tbitsGlobal.utils.client.TbitsTreeRequestData;
import commons.com.tbitsGlobal.utils.client.domainObjects.AttachmentInfoClient;
 
public class CoOb {

	private static final String ILLEGAL_LOGIN = "Login user cannot be verified.";
	public String corrProtName = null ;
	public String corrTypeName = null ;
	public String corrInitName = null ;
	
   public User myLoginUser = null ;
   public User myLogger0User = null ;
   public User myAss0User = null ;
   
   public Hashtable<String,String> myLoginInfo = null ;
   public Hashtable<String,String> myLogger0Info = null ;
   
	public Type myCorrProt = null ;
	public Type myCorrType = null ;
	public Type myCorrInit = null ;
	
	public Type myPrevCorrProt = null ;
	public Type myPrevCorrType = null ;
	public Type myPrevCorrInit = null ;
	
	public Request myPrevRequest = null ;
	
	public BusinessArea myBA = null ;
	
	public boolean isAddRequest ;
	
	public String myLoggerList = null ;
	public String mySubList = null ;
	public String myAssList = null ;
	
	public Hashtable<String,String> myAss0Info = null ;
	public ArrayList<String> mySubArray = null ;

	public Hashtable<Integer,ArrayList<String>> myLogger0Map = null ;
	public String myLogger0 = null ;
	public String myAss0 = null ;
	public String myLogin = null ;
	
	public String description = null ;
	public String subject = null ;
	
	public Collection<AttachmentInfo> attInfos = null ;
	
	public CoOb( HttpServletRequest hr ) throws DatabaseException, TBitsException
	{
		User login_user = null;
		login_user = WebUtil.validateUser(hr);
        this.myLoginUser = login_user ;
		this.myBA = BusinessArea.lookupBySystemPrefix(KskConstants.CORR_SYSPREFIX) ;
		
		String previewAction = hr.getParameter("previewAction") ;
		if( previewAction != null )
		{
			if( previewAction.trim().equalsIgnoreCase("update-request"))
			{
				int requestID = Integer.parseInt(hr.getParameter("requestId")) ;
				this.myPrevRequest = Request.lookupBySystemIdAndRequestId(this.myBA.getSystemId(), requestID ) ;
		        this.isAddRequest = false ;
			}
			else 
			{
				this.isAddRequest = true ;
			}
		}
		
		myLoggerList = (String) hr.getParameter(KskConstants.CORR_LOGGER_FIELD_NAME) ;
		myAssList = (String) hr.getParameter(KskConstants.CORR_ASSIGNEE_FIELD_NAME ) ;
		mySubList = (String)hr.getParameter(KskConstants.CORR_SUBSCRIBER_FIELD_NAME) ;
		corrProtName = (String) hr.getParameter(KskConstants.CORR_CORRESPONDANCE_PROTOCOL_FIELD_NAME) ;
		corrTypeName = (String) hr.getParameter(KskConstants.CORR_TYPE_FIELD_NAME) ;
		corrInitName = (String) hr.getParameter(KskConstants.CORR_CORRESPONDANCE_INITIATOR_FIELD_NAME) ;
		subject = hr.getParameter(Field.SUBJECT);
		description = hr.getParameter(Field.DESCRIPTION);
		String attachmentJson  = hr.getParameter(Field.ATTACHMENTS);
		attachmentJson = getAttachmentJsonForField(attachmentJson, CORR_OTHER_ATTACHMENTS_FIELD_NAME);
		if( null == attachmentJson )
			attachmentJson = "[]" ;
		attInfos = AttachmentInfo.fromJson(attachmentJson);
		
		System.out.println("Attachment Json : " + attachmentJson);
		validateAndSet() ;
	}
	
	public CoOb( TbitsTreeRequestData ttrd, User lu ) throws DatabaseException, TBitsException
	{
//		String loginUserString = ttrd.getAsString(Field.USER);
//		if( null == loginUserString )
//			throw new TBitsException(ILLEGAL_LOGIN);
//		
//		String [] loginUserArray = loginUserString.split(",");
//		
//		if( null == loginUserArray || 0 == loginUserArray.length )
//			throw new TBitsException(ILLEGAL_LOGIN);
//		
//		User lu = User.lookupAllByUserLogin(loginUserArray[0]);
		if( null == lu )
			throw new TBitsException(ILLEGAL_LOGIN);
		
        this.myLoginUser = lu ;
        
		this.myBA = BusinessArea.lookupBySystemPrefix(KskConstants.CORR_SYSPREFIX) ;
		
		this.isAddRequest = true ;
		String reqIdStr = ttrd.getAsString(Field.REQUEST);
		if( null != reqIdStr && !reqIdStr.equals(""))
		{
			try
			{
				int requestID = 0 ;
				try
				{
					requestID = Integer.parseInt(reqIdStr) ;
				}
				catch(NumberFormatException nfe)
				{
					nfe.printStackTrace() ;
				}
				
				if( requestID != 0 )
				{
					this.myPrevRequest = Request.lookupBySystemIdAndRequestId(this.myBA.getSystemId(), requestID ) ;
					this.isAddRequest = false ;
				}
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
		}
		
		myLoggerList 	= ttrd.getAsString(KskConstants.CORR_LOGGER_FIELD_NAME) ;
		myAssList 		= ttrd.getAsString(KskConstants.CORR_ASSIGNEE_FIELD_NAME ) ;
		mySubList 		= ttrd.getAsString(KskConstants.CORR_SUBSCRIBER_FIELD_NAME) ;
		corrProtName 	= ttrd.getAsString(KskConstants.CORR_CORRESPONDANCE_PROTOCOL_FIELD_NAME) ;
		corrTypeName 	= ttrd.getAsString(KskConstants.CORR_TYPE_FIELD_NAME) ;
		corrInitName 	= ttrd.getAsString(KskConstants.CORR_CORRESPONDANCE_INITIATOR_FIELD_NAME) ;
		
		subject = ttrd.getAsString(Field.SUBJECT);
		description = ttrd.getAsString(Field.DESCRIPTION);
		
		// FIXME : I think I will not get json here so directly taking
		// the POJOAttachment
		String attachmentJson  = ttrd.getAsString(Field.ATTACHMENTS);
		// FIXME : check if this returns correct object
		
		Collection<AttachmentInfoClient> cpa = ttrd.get(Field.ATTACHMENTS);
		attInfos = new ArrayList<AttachmentInfo>() ;
		if( null != cpa )
		{
			for( AttachmentInfoClient pa : cpa )
			{
				// public AttachmentInfo(String name, int repoFileId, int requestFileId,int size)
				AttachmentInfo ai = new AttachmentInfo(pa.getFileName(), pa.getRepoFileId(), pa.getRequestFileId(), pa.getSize());
				attInfos.add(ai);
			}
		}

		System.out.println("Attachment Json : " + attachmentJson);
		System.out.println("Attachment Collection : " + attInfos);
		validateAndSet() ;
	}
	
	public void validateAndSet() throws TBitsException
	{		
		myLogin = myLoginUser.getUserLogin() ;		
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
		
		if(null == myAssList || myAssList.trim().equals(""))
			throw new TBitsException( "Assignee Field cannot be empty.") ;
		
		ArrayList<String> assArray = Utilities.toArrayList(myAssList) ;
		if( assArray.size() == 0 || assArray.size() > 1 )
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
		
		setMyFields() ;

		// only WPCL member are allowed to log on behalf of somebody else
		try
		{
			myLoginInfo = UserInfoManager.getUserInfo(myLoginUser.getUserId()) ;
		}catch(Exception e )
		{
			throw new TBitsException( "No user info found for (" + myLoginUser.getUserLogin() + ")" ) ;
		}

		try
		{
			myLogger0Info = UserInfoManager.getUserInfo(myLogger0User.getUserId()) ;
		}catch(Exception e )
		{
			throw new TBitsException( "No user info found for (" + myLogger0User.getUserLogin() + ")" ) ;
		}
		
		if( !myLoginInfo.get(UserInfoManager.FIRM ).trim().equalsIgnoreCase(myLogger0Info.get(UserInfoManager.FIRM).trim()) )
		{
			throw new TBitsException("You (" + myLoginUser.getUserLogin() + ") are not allowed to log on behalf of (" + myLogger0User.getUserLogin() + ")" ) ;
		}
		
		// corrInit type 
		if( isAddRequest )
		{
			String cin = myCorrInit.getName() ;
			String lfirm = myLogger0Info.get(UserInfoManager.FIRM) ;
			String lloc = myLogger0Info.get(UserInfoManager.LOCATION) ;
			if( !( cin.indexOf(lfirm) >= 0  && cin.substring(cin.trim().length()-2).equals(lloc.trim()) ) )
			{
				throw new TBitsException("Correpondance Initiator field was not correctly set.") ;
			}
		}
		
		try
		{
			myAss0Info = UserInfoManager.getUserInfo(myAss0User.getUserId()) ;
		}catch(Exception e )
		{
			throw new TBitsException( "No user info found for (" + myAss0User.getUserLogin() + ")" ) ;
		}
		
		
		mySubArray = Utilities.toArrayList(mySubList, ",") ;
		
		if( myCorrProt.getName().equals(KskConstants.CORR_CORR_PROT_WCPL_SEPCO))
			checkCorrRestrictions() ;		
	}
	

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
	
	
	public void setMyFields() throws TBitsException 
	{				
		try {
			myCorrProt = Type.lookupAllBySystemIdAndFieldNameAndTypeName(myBA.getSystemId(), KskConstants.CORR_CORRESPONDANCE_PROTOCOL_FIELD_NAME, corrProtName ) ;
		} catch (DatabaseException e) {			
			e.printStackTrace();
			throw new TBitsException(e) ;
		}
		if( null == myCorrProt )
			throw new TBitsException("The Correspondance Protocol Field was not properly set.") ;
				
		try {
			myCorrType = Type.lookupAllBySystemIdAndFieldNameAndTypeName(myBA.getSystemId(), KskConstants.CORR_TYPE_FIELD_NAME, corrTypeName ) ;
		} catch (DatabaseException e) {
			e.printStackTrace();
			throw new TBitsException(e) ;
		}
		if( null == myCorrType )
			throw new TBitsException("The Correspondance Type Field was not properly set.") ;
				
		try {
			myCorrInit = Type.lookupAllBySystemIdAndFieldNameAndTypeName(myBA.getSystemId(), KskConstants.CORR_CORRESPONDANCE_INITIATOR_FIELD_NAME, corrInitName ) ;
		} catch (DatabaseException e) {
			e.printStackTrace();
			throw new TBitsException(e) ;
		}
		if( null == myCorrInit )
			throw new TBitsException("The Correspondance initiator Field was not properly set.") ;
	}
	

}
