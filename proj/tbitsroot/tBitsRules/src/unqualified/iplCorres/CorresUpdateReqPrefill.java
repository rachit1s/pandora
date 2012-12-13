package iplCorres;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import transbit.tbits.ExtUI.IExtUIRenderer;
import transbit.tbits.common.DTagReplacer;
import transbit.tbits.common.DatabaseException;
import transbit.tbits.common.TBitsLogger;
import transbit.tbits.config.WebConfig;
import transbit.tbits.domain.BusinessArea;
import transbit.tbits.domain.Field;
import transbit.tbits.domain.Request;
import transbit.tbits.domain.Type;
import transbit.tbits.domain.User;
import transbit.tbits.exception.TBitsException;
import transbit.tbits.webapps.WebUtil;

import com.google.gson.Gson;

public class CorresUpdateReqPrefill implements IExtUIRenderer 
{

	public static final TBitsLogger LOG = TBitsLogger.getLogger("PKG_KSKCORRES");
	
	private String myErrorList = "" ;
	
	public double getSequence() 
	{		
		return 0;
	}
	
	/*
	 * type_field_name * 
	 * logger_field_name * 
 	 * correspondence_initiator_field_name * 
	 * assignee_field_name * 
	 * subscriber_field_name *
	 * generate_correspondence_field_name *
	 * corr_generation_agency_field_name *
	 * userCorrInitMap_value  * 
	 * userMap_value * 
	 * isWPCLMember_value *
	 * corGenAgencyIndex_value *
	 * logger_value *
	 * showMessages_value *
	 * messages_value *
	 * 
	 * corrProtOthersIndex_value
	 * corrProtWPCLSEPCOIndex_value
	 * previewPDFButton_field_name
	 * corr_prot_field_name
	 * submitButton_field_name
	 */
	
//	static class UserMap
//	{
//		String userLogin ;
//		String corr ;
//		ArrayList<String> to ;
//		ArrayList<String> cc;
//		public UserMap( String ul, String cor, ArrayList<String> toa, ArrayList<String> cca )
//		{
//			userLogin = ul ;
//			corr = cor ; 
//			to = toa ;
//			cc = cca ;
//		}
//	}
//	public static String getUserMapValue( String firm, String login_user, String boss, ArrayList<String> prevSubsArray ) throws TBitsException 
//	{
//		Hashtable<String,Hashtable<String, Hashtable<Integer,ArrayList<String>>>> map = UserMapManager.getCompleteTableAsUserLogin(firm) ;
//		ArrayList<UserMap> userMapArray = new ArrayList<UserMap>() ;
//		for( java.util.Enumeration<String> users = map.keys() ; users.hasMoreElements() ;  )
//		{
//			String user = users.nextElement() ;
//			Hashtable<String, Hashtable<Integer,ArrayList<String>>> corTable = map.get(user) ;
//			for( java.util.Enumeration<String> cors = corTable.keys() ; cors.hasMoreElements() ; )
//			{
//				String cor = cors.nextElement() ;
//				Hashtable<Integer,ArrayList<String>> values = corTable.get( cor ) ; 
//				ArrayList<String> toList = values.get(UserMapManager.TO) ;
//				ArrayList<String> yourCCList = values.get(UserMapManager.YOUR_CC) ;
//				ArrayList<String> ourCCList = values.get(UserMapManager.OUR_CC) ;
//				HashSet<String> nccs = new HashSet<String>( ourCCList ) ;
//				nccs.addAll(yourCCList) ;
//				nccs.addAll(prevSubsArray) ;
//				// if logger != login then include the login && wpcl_boss in ourCCList
//				if( !user.equals(login_user) )
//				{					
//					nccs.add(login_user) ;
//					nccs.add(boss) ;				
//				}
//				nccs.removeAll(toList) ;
//				nccs.remove(user) ;
//				userMapArray.add( new UserMap(user,cor,toList,new ArrayList<String>(nccs)) )  ;
//			}
//		}
//		
//		Gson gson = new Gson() ;
//		return gson.toJson(userMapArray) ;
////		return null ;
//	}

    private static class UserCat
    {
    	String userLogin ;
    	int index ;
    	public UserCat( String userLogin, int index ) 
    	{
    		this.userLogin = userLogin ;
    		this.index = index ;
    	}
    }
    
    public static String getCorrInitJson( Hashtable<String,Hashtable<String,String>> userInfoTable, int sys_id, String fieldName ) throws DatabaseException 
    {
    	ArrayList<Type> tl = Type.lookupAllBySystemIdAndFieldName(sys_id, fieldName ) ;
    	ArrayList<UserCat> userCatArray = new ArrayList<UserCat>() ;
    	for( Enumeration<String> ul = userInfoTable.keys() ; ul.hasMoreElements() ; )
    	{
    		String user_login = ul.nextElement() ; 
    		Hashtable<String,String> userInfo = userInfoTable.get(user_login) ;
    		String firm = userInfo.get(UserInfoManager.FIRM) ;
//    		String location = userInfo.get(UserInfoManager.LOCATION ) ;
	    	for( Iterator<Type> it = tl.iterator() ; it.hasNext() ; )
	    	{
	    		Type type = it.next() ;
	    		String name = type.getName() ;
	    		if( name.indexOf(firm) >= 0 ) // && name.substring(name.trim().length()-2).equals(location.trim()))
	    		{
	    			userCatArray.add(new UserCat(user_login,type.getOrdering() - 1)) ;
	    			break ;
	    		}
	    	}
    	}
    	
    	Gson gson = new Gson() ; 
    	return gson.toJson(userCatArray ) ;
    }
    
	public void process(HttpServletRequest request,
			HttpServletResponse response, Hashtable tagTable,
			ArrayList<String> tagList, BusinessArea ba, int action )  throws TBitsException 
	{
		if( (null == ba) || (null == ba.getSystemPrefix()) ) 
			return ;
		
		if( ! ba.getSystemPrefix().trim().equalsIgnoreCase(IPLConstants.CORR_SYSPREFIX) ) // this is Correspondence business area
			return ;
		
		if( IExtUIRenderer.UPDATE_REQUEST != action )
			return ;		
		
		LOG.info("inside + " + getClass().getName( )) ;
				
		User login_user = null ;
		int login_id ;
		Hashtable paramInfo = null ;
		try {
			login_user = WebUtil.validateUser(request);
			
			WebConfig userConfig = login_user.getWebConfigObject();
			//
	        // Call the WebUtil\"s getParamInfo, which reads the pathInfo and
	        // get the corresponding BA and request object. Throws exception
	        // if either Ba or requestId is invalid.
	        //
	        paramInfo = WebUtil.getRequestParams(request, userConfig, WebUtil.VIEW_REQUEST);
			
		} catch (DatabaseException e2) {
			e2.printStackTrace();
			throw new TBitsException(e2) ;
		} catch (ServletException e) {
			e.printStackTrace();
			throw new TBitsException(e) ;
		}
		
        Request myRequest   = (Request) paramInfo.get(Field.REQUEST);        
		
		login_id = login_user.getUserId() ;
		
		Hashtable<String,String> login_info ; 
		
		try
		{
			login_info = UserInfoManager.getUserInfo(login_id) ;
		}
		catch( IllegalArgumentException iae ) 
		{
			iae.printStackTrace() ;
			LOG.error(IPLConstants.MSG_NO_MAPPING_FOUND) ;			
			String disSub = IPLConstants.disableSubmit(IPLConstants.MSG_NO_MAPPING_FOUND) ;
			tagTable.put( IPLConstants.EXT_SUBMIT_BUTTON_LIST, disSub ) ;
			return ;
		}
		catch( TBitsException e ) 
		{
			e.printStackTrace() ;
			String sher = IPLConstants.showError(IPLConstants.MSG_CANNOT_PREFILL) ;
			tagTable.put(IPLConstants.EXT_SUBMIT_BUTTON_LIST, sher ) ;
			return ;
		}
			
		URL fileURL = getClass().getResource(IPLConstants.UPDATE_PREFILL_FILE_NAME) ;
		if( null == fileURL )
		{
			LOG.error( "File not found = " + IPLConstants.UPDATE_PREFILL_FILE_NAME ) ;
			String sher = IPLConstants.showError(IPLConstants.MSG_CANNOT_PREFILL) ;
			tagTable.put(IPLConstants.EXT_SUBMIT_BUTTON_LIST, sher ) ;
			return ;
		}
		
		String filedata = "" ;
		String filePath = fileURL.getFile() ;
		if( filePath.equals(""))
		{
			LOG.error( "File not found = " + IPLConstants.UPDATE_PREFILL_FILE_NAME ) ;
			String sher = IPLConstants.showError(IPLConstants.MSG_CANNOT_PREFILL) ;
			tagTable.put(IPLConstants.EXT_SUBMIT_BUTTON_LIST, sher ) ;
			return ;
		}		
	
//		 test if DtagReplacer can find my file 
		DTagReplacer dtagreplacer = null ;
		File myFile = new File(filePath) ;
		try {
			 dtagreplacer = new DTagReplacer( myFile ) ;
		} catch (FileNotFoundException e1) {
			LOG.error("DTagReplacer Exception : file not found" ) ;
			e1.printStackTrace();
			String sher = IPLConstants.showError(IPLConstants.MSG_CANNOT_PREFILL) ;
			tagTable.put(IPLConstants.EXT_SUBMIT_BUTTON_LIST, sher ) ;
			return ;
		} catch (IOException e1) {
			LOG.error("DTagReplacer Exception" ) ;
			e1.printStackTrace();
			String sher = IPLConstants.showError(IPLConstants.MSG_CANNOT_PREFILL) ;
			tagTable.put(IPLConstants.EXT_SUBMIT_BUTTON_LIST, sher ) ;
			return ;
		}
		
		LOG.info("No Exception in creating detagreplacer.") ;
//		RandomAccessFile raf = null ;
//		try {
//			String line = "" ;
//			raf = new RandomAccessFile( filePath , "r") ;
//			while( null != ( line = raf.readLine() ) )
//			{
//				filedata += line + "\n" ;
//			}
//		} catch (IOException e) {
//			LOG.error( "IOException while reading the file = " + KskConstants.UPDATE_PREFILL_FILE_NAME ) ;
//			e.printStackTrace();
//			String sher = KskConstants.showError(KskConstants.MSG_CANNOT_PREFILL) ;
//			tagTable.put(KskConstants.EXT_SUBMIT_BUTTON_LIST, sher ) ;
//			return ;
//		}
//		catch (Exception e) 
//		{
//			LOG.error( "File not found = " + KskConstants.UPDATE_PREFILL_FILE_NAME ) ;
//			e.printStackTrace();
//			String sher = KskConstants.showError(KskConstants.MSG_CANNOT_PREFILL) ;
//			tagTable.put(KskConstants.EXT_SUBMIT_BUTTON_LIST, sher ) ;
//			return ;
//		}
//		finally
//		{
//			if( raf != null )
//				try {
//					raf.close() ;
//				} catch (IOException e) {
//					LOG.error("Exception while closing the file : " + filePath ) ;
//					e.printStackTrace();
//				}			
//		}	
		
		Hashtable<String,String> params = new Hashtable<String,String>() ;		
		params.put("correspondence_initiator_field_name", IPLConstants.CORR_CORRESPONDANCE_INITIATOR_FIELD_NAME) ;
		params.put("type_field_name", IPLConstants.CORR_TYPE_FIELD_NAME) ;
		params.put("generate_correspondence_field_name", IPLConstants.CORR_GENERATE_CORRESPONDANCE_FIELD_NAME) ;
		params.put("corr_generation_agency_field_name", IPLConstants.CORR_CORR_GENERATION_AGENCY_FIELD_NAME) ;
		params.put("logger_field_name", IPLConstants.CORR_LOGGER_FIELD_NAME) ;
		params.put("assignee_field_name", IPLConstants.CORR_ASSIGNEE_FIELD_NAME) ;
		params.put("subscriber_field_name", IPLConstants.CORR_SUBSCRIBER_FIELD_NAME) ;
		params.put("corr_prot_field_name", IPLConstants.CORR_CORRESPONDANCE_PROTOCOL_FIELD_NAME) ;
		params.put("previewPDFButton_field_name", "previewPdfButton") ;
		params.put( "submitButton_field_name", "btnSubmit2") ;
		String previewPdfButton = IPLConstants.getExtSubmitButtonHTML("previewPdfButton", "previewPdfButton", "PreviewPDF", "javascript:onPreview('pdf')" ) ;
//		String previewHtmlButton = KskConstants.getExtSubmitButtonHTML("previewPdfButton", "previewPdfButton", "PreviewHTML", "javascript:onPreview('html')" ) ;
	
		String buttons = previewPdfButton ; //+ "&nbsp&nbsp&nbsp" + previewHtmlButton ;		
		
		params.put("previewButtons", buttons ) ;
		
	//	params.put("logger_value", login_user.getUserLogin() ) ;				
		// get  user info	
		String login_firm = login_info.get(UserInfoManager.FIRM) ;	
//		boolean isWPCLMember = login_firm.trim().equalsIgnoreCase( KskConstants.WPCL_FIRM_NAME ) ? true : false ;
		
		try
		{
			String corrInitName = (String) myRequest.myMapFieldToObjects.get(IPLConstants.CORR_CORRESPONDANCE_INITIATOR_FIELD_NAME) ;
			Type corrInitType = Type.lookupBySystemIdAndFieldNameAndTypeName(ba.getSystemId(), IPLConstants.CORR_CORRESPONDANCE_INITIATOR_FIELD_NAME , corrInitName ) ;
			String corrInitSelectedIndex_value = Integer.toString(corrInitType.getOrdering() - 1 ) ;
			params.put("corrInitSelectedIndex_value", corrInitSelectedIndex_value) ;
			
			ArrayList<Type> tal = Type.lookupAllBySystemIdAndFieldName(ba.getSystemId(), IPLConstants.CORR_CORR_GENERATION_AGENCY_FIELD_NAME) ;
			for( Iterator<Type> iter = tal.iterator() ; iter.hasNext() ; )
			{
				Type type = iter.next() ;
				if( type.getName().equalsIgnoreCase(login_firm))
				{
					params.put("corGenAgencyIndex_value", Integer.toString(type.getOrdering() - 1) ) ;
				}
			}		
		
//			ArrayList<Type> cpl = Type.lookupAllBySystemIdAndFieldName(ba.getSystemId(), KskConstants.CORR_CORRESPONDANCE_PROTOCOL_FIELD_NAME ) ;
//			for( Iterator<Type> iter = cpl.iterator() ; iter.hasNext() ; )
//			{
//				Type type = iter.next() ;
//				if( type.getName().equals(KskConstants.CORR_CORR_PROT_WCPL_SEPCO) )
//					params.put("corrProtWPCLSEPCOIndex_value", Integer.toString(type.getOrdering()-1)) ;
//				else if ( type.getName().equals(KskConstants.CORR_CORR_PROT_OTHERS) )
//					params.put("corrProtOthersIndex_value", Integer.toString(type.getOrdering()-1) ) ;
//			}
			
			Hashtable<String,Hashtable<String,String>> userInfoTable = new Hashtable<String,Hashtable<String,String>>() ;
//			if( isWPCLMember == false )
//			{			
//				userInfoTable.put(login_user.getUserLogin(), login_info) ;			
//			}
//			else
//			{
				userInfoTable = UserInfoManager.lookupUserInfoWithFirm(login_firm) ;
//			}
			
			String userCorrInitMap_value = getCorrInitJson(userInfoTable, ba.getSystemId(), IPLConstants.CORR_CORRESPONDANCE_INITIATOR_FIELD_NAME) ;
			
			params.put("userCorrInitMap_value", userCorrInitMap_value) ;
			
//			String userMap_value = "" ;
//			if( isWPCLMember == false )
//			{
//				Hashtable<String,Hashtable<Integer,ArrayList<String>>> loginMap = UserMapManager.getCompleteMappingAsUserLogin(login_id) ;
//				ArrayList<UserMap> uma = new ArrayList<UserMap>() ;
//				for( Enumeration<String> iter = loginMap.keys() ; iter.hasMoreElements() ; )
//				{
//					String cor = iter.nextElement() ;
//					Hashtable<Integer, ArrayList<String>> values =  loginMap.get(cor) ;
//					ArrayList<String> toList = values.get(UserMapManager.TO) ;
//					ArrayList<String> yourCCList = values.get(UserMapManager.YOUR_CC) ;
//					ArrayList<String> ourCCList = values.get(UserMapManager.OUR_CC) ;
//					HashSet<String> ccs = new HashSet<String>( yourCCList ) ;
//					ccs.addAll(ourCCList) ;
//					ccs.removeAll(toList) ;
//					ccs.remove(login_user.getUserLogin()) ;
//					uma.add( new UserMap( login_user.getUserLogin(), cor, toList,  new ArrayList<String>(ccs) ) ) ;
//				}
//				
//				userMap_value = new Gson().toJson(uma) ;
//			}
//			else
//			{
//				userMap_value = getUserMapValue(KskConstants.WPCL_FIRM_NAME, login_user.getUserLogin() , KskConstants.KSK_BOSS_LOGIN, new ArrayList<String>() ) ;
//			}
//			
//			params.put("userMap_value", userMap_value) ;
			
	//		System.out.println( "myErrorList :" + myErrorList ) ; 
			if( myErrorList.length() > 0 )
			{
				params.put("showMessages_value", "true") ;
				params.put("messages_value", myErrorList ) ;
			}
			else
			{
				params.put("showMessages_value", "false") ;
				params.put("messages_value", "" ) ;
			}
			
//			filedata = KskConstants.tagReplacer(params, filedata) ;
			for( Enumeration<String> keys = params.keys() ; keys.hasMoreElements() ; )
			{
				String key = keys.nextElement() ;
				String value = params.get(key) ;
				dtagreplacer.replace(key, value) ;
			}
			String filedata1 = dtagreplacer.parse() ;
	//		System.out.println("fileData\n**********START********\n"+ filedata + "\n**********END********\n") ;
			
			tagTable.put(IPLConstants.EXT_SUBMIT_BUTTON_LIST, filedata1 ) ;
					
			return ;
		}
		catch( Exception e ) 
		{
			LOG.error("Exception while filling the prefill javascript") ;
			String sher = IPLConstants.showError(IPLConstants.MSG_CANNOT_PREFILL) ;
			tagTable.put(IPLConstants.EXT_SUBMIT_BUTTON_LIST, sher ) ;
			return ;
		}
	}	
	
	
	public static void main( String argv[] ) 
	{
//		try {
//			System.out.println(getWPCLCategoryJson( UserInfoManager.getWPCLUserLocation() , BusinessArea.lookupBySystemPrefix("CORR").getSystemId()) );
//		} catch (DatabaseException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (TBitsException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
	}
}
