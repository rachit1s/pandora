package nccCorres;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import transbit.tbits.ExtUI.IAddRequestFooterSlotFiller;
import transbit.tbits.ExtUI.ISubRequestFooterSlotFiller;
import transbit.tbits.ExtUI.IUpdateRequestFooterSlotFiller;
import transbit.tbits.common.DTagReplacer;
import transbit.tbits.common.DatabaseException;
import transbit.tbits.common.TBitsLogger;
import transbit.tbits.domain.BusinessArea;
import transbit.tbits.domain.Request;
import transbit.tbits.domain.Type;
import transbit.tbits.domain.User;
import transbit.tbits.exception.TBitsException;

import static nccCorres.CorresConstants.*;
import com.google.gson.Gson;

public class CorresPrefill implements IAddRequestFooterSlotFiller, IUpdateRequestFooterSlotFiller, ISubRequestFooterSlotFiller
{

	public static final TBitsLogger LOG = TBitsLogger.getLogger("nccCorres");
	
	static class UserAgency
	{
		String userLogin ;
		String agency ;
		public UserAgency( String userLogin, String agency)
		{
			this.userLogin = userLogin ;
			this.agency = agency ;
		}
	}

    public static String getUserAgencyJson(int sys_id) throws DatabaseException 
    {
    	ArrayList<UserAgency> userAgencyArray = new ArrayList<UserAgency>() ;    	
    	ArrayList<User> users = User.getActiveUsers() ;
    	
    	for( User user : users )
    	{
    		if( null != user.getUserLogin() && !user.getUserLogin().trim().equals("") 
    			&&	null != user.getFirmCode() && !user.getFirmCode().trim().equals("")
    		  )
    		{	    			    		
		    	userAgencyArray.add(new UserAgency(user.getUserLogin(),user.getFirmCode()) ) ;		    	
		    }
    	}
    	
    	Gson gson = new Gson() ; 
    	return gson.toJson(userAgencyArray ) ;
    }
    
	public String process(HttpServletRequest request,
			HttpServletResponse response, BusinessArea ba, Request oldRequest, User user  ) 
	{
		
		if( (null == ba) || (null == ba.getSystemPrefix()) ) 
			return "" ;
		
		if( ! ba.getSystemPrefix().trim().equalsIgnoreCase(CorresConstants.CORR_SYSPREFIX) ) // this is Correspondence business area
			return "" ;
			
		String myErrorList = "" ;
		
		LOG.info("inside + " + getClass().getName( )) ;
	
		URL fileURL = getClass().getResource(CorresConstants.PREFILL_JS_FILE) ;
		if( null == fileURL )
		{
			LOG.error( "File not found = " + CorresConstants.PREFILL_JS_FILE ) ;
			String sher = CorresConstants.showError(CorresConstants.MSG_CANNOT_PREFILL) ;
			return sher;
		}
		
		String filePath = fileURL.getFile() ;
		if( filePath.equals(""))
		{
			LOG.error( "File not found = " + CorresConstants.PREFILL_JS_FILE ) ;
			String sher = CorresConstants.showError(CorresConstants.MSG_CANNOT_PREFILL) ;
			return sher;
		}		
	
		DTagReplacer dtagreplacer = null ;
		File myFile = new File(filePath) ;
		try {
			 dtagreplacer = new DTagReplacer( myFile ) ;
		} catch (FileNotFoundException e1) {
			LOG.error("DTagReplacer Exception : file not found" ) ;
			e1.printStackTrace();
			String sher = CorresConstants.showError(CorresConstants.MSG_CANNOT_PREFILL) ;
			return sher;
		} catch (IOException e1) {
			LOG.error("DTagReplacer Exception" ) ;
			e1.printStackTrace();
			String sher = CorresConstants.showError(CorresConstants.MSG_CANNOT_PREFILL) ;
			return sher;
		}
		
		try
		{
			Hashtable<String,String> params = new Hashtable<String,String>() ;		
			params.put("originator_field_name", CorresConstants.CORR_ORIGINATOR_FIELD_NAME) ;
			params.put("recepient_field_name", CorresConstants.CORR_RECEPIENT_FIELD_NAME) ;		
			params.put("logger_field_name", CorresConstants.CORR_LOGGER_FIELD_NAME) ;
			params.put("assignee_field_name", CorresConstants.CORR_ASSIGNEE_FIELD_NAME) ;
			params.put("generate_field_name", CorresConstants.CORR_GENERATE_FIELD_NAME);
			params.put("generate_action_only_value", CorresConstants.CORR_GEN_DONT_GEN_ANYTHING);
			params.put("corr_no_field_name", CORR_CORRESPONDENCE_NUMBER_FIELD );
			params.put("corrType_field_name", CORR_CORR_TYPE_FIELD_NAME);
			params.put("cc_field_name", CORR_CC_FIELD_NAME);
			params.put("general_attributes_field_name", CORR_GENATT_FIELD_NAME);
			params.put("wbs_attributes_field_name", CORR_WBSATT_FIELD_NAME ) ;
			params.put("subs_field_name", CORR_SUBSCRIBER_FIELD_NAME);
//			
//			if( isValidIONUser(user) )
//			{
//				params.put("always_disable_ION_value", "false");
//			}
//			else
//			{
//				params.put("always_disable_ION_value", "true");
//			}
			
			params.put("previewPDFButton_field_name", "previewPdfButton") ;
			params.put( "submitButton_field_name", "btnSubmit2") ;
			String previewPdfButton = CorresConstants.getExtSubmitButtonHTML("previewPdfButton", "previewPdfButton", "PreviewPDF", "javascript:onPreview('pdf')" ) ;
		
			String buttons = previewPdfButton ;		
			params.put("previewButtons", buttons ) ;
			/*
			subs_field_name *			
			assignee_field_name *
			cc_field_name *
			corr_no_field_name *
			corrType_field_name *
			general_attributes_field_name *			
			generate_field_name *			
			logger_field_name *			
			originator_field_name *
			previewButtons *
			previewPDFButton_field_name *
			recepient_field_name *			
			submitButton_field_name *
			wbs_attributes_field_name *
			allowedAgencies *			
			generate_action_only_value *
			user_agency_map_value*
			generate_file_and_no_value*
			ion_value *
			isUpdate *
			wasLastION *
			messages_value*
			showMessages_value*
			*/
			
//			params.put("logger_value", user.getUserLogin() ) ;
			Gson gson = new Gson() ;
			System.out.println("Valid agencies are : " + VALID_ION_AGENCY);
			String validAgencies = gson.toJson(VALID_ION_AGENCY);
			params.put("allowedAgencies", validAgencies);
			params.put("generate_file_and_no_value", CORR_GEN_BOTH_FILE_AND_NUMBER);
			params.put("ion_value", CORR_CORR_TYPE_ION);
			
			if( null == oldRequest ) // this is add request
			{
				params.put("isUpdate", "false");
				params.put("wasLastION", "false"); // ignored in js
			}
			else
			{
				params.put("isUpdate", "true");
				String corrTypeName = oldRequest.get(CORR_CORR_TYPE_FIELD_NAME);
				if( null != corrTypeName && corrTypeName.equals(CORR_CORR_TYPE_ION))
					params.put("wasLastION", "true");
				else
					params.put("wasLastION", "false");
			}
			
			params.put("user_agency_map_value", getUserAgencyJson( ba.getSystemId() ) ) ;
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
			
			for( Enumeration<String> keys = params.keys() ; keys.hasMoreElements() ; )
			{
				String key = keys.nextElement() ;
				String value = params.get(key) ;
				dtagreplacer.replace(key, value) ;
			}
			String filedata1 = dtagreplacer.parse() ;
			
			return filedata1;
		}		
		catch( Exception e ) 
		{
			e.printStackTrace();
			LOG.error("Exception while filling the prefill javascript") ;
			String sher = CorresConstants.showError(CorresConstants.MSG_CANNOT_PREFILL) ;
			return sher;
		}
	}	
	
	public String getAddRequestFooterHtml(HttpServletRequest httpRequest,
			HttpServletResponse httpResponse, BusinessArea ba, User user) {		
		return process( httpRequest, httpResponse, ba,null, user );
	}

	public double getAddRequestFooterSlotFillerOrder() 
	{
		return 0;
	}

	public String getUpdateRequestFooterHtml(HttpServletRequest httpRequest,
			HttpServletResponse httpResponse, BusinessArea ba,
			Request oldRequest, User user) {

		return process( httpRequest, httpResponse, ba,oldRequest, user );
	}

	public double getUpdateRequestFooterSlotFillerOrder() {
		
		return 0;
	}

	public String getSubRequestFooterHtml(HttpServletRequest httpRequest,
			HttpServletResponse httpResponse, BusinessArea ba,
			Request parentRequest, User user) 
	{
		return process( httpRequest, httpResponse, ba,null, user );
	}

	public double getSubRequestFooterOrder() {		
		return 0;
	}

}
