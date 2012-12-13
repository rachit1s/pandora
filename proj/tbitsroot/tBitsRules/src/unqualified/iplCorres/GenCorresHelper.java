package iplCorres;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Set;
import java.util.Map.Entry;

import org.eclipse.birt.report.engine.api.EngineConfig;
import org.eclipse.birt.report.engine.api.EngineException;
import org.eclipse.birt.report.engine.api.IReportDocument;
import org.eclipse.birt.report.engine.api.IReportEngine;
import org.eclipse.birt.report.engine.api.IReportRunnable;
import org.eclipse.birt.report.model.api.activity.SemanticException;

import transbit.tbits.api.AttachmentInfo;
import transbit.tbits.common.DatabaseException;
import transbit.tbits.common.TBitsLogger;
import transbit.tbits.domain.Field;
import transbit.tbits.domain.Request;
import transbit.tbits.domain.User;
import transbit.tbits.exception.TBitsException;
import transbit.tbits.report.TBitsReportEngine;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class GenCorresHelper 
{
	public static final TBitsLogger LOG = TBitsLogger.getLogger("KSKCORRES");
	// private static final String TBITS_BASE_URL_KEY = "tbits_base_url";
	/**
	 * take the reportName, the parameters to be set in the javascript and the reportparameter, and the format of the report to be 
	 * generated and returns the name of the generated html/pdf 
	 * @param reportName
	 * @param params
	 * @param reportParams
	 * @param format
	 * @return null if the report was not generated otherwise the File object for this report 
	 */
	public static File generateReport( String reportName,Hashtable<String, String> params, HashMap<String,String> reportParams, String format )
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
				if( format.trim().equalsIgnoreCase("pdf"))
				 outFile = tre.getPDFReport(ird);
				else 
					outFile = tre.getHTMLReport(ird) ; // default
				
				/////// print file info
				if( outFile != null ) 
				{
					System.out.println( "Name:" + outFile.getName() + " path = " + outFile.getAbsolutePath() ) ;
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
		finally
		{
			if (tre != null)
				tre.destroy();
		}
		
	}
	
	
	/**
	 * takes the login-name of the user and generates the html code for his/her complete name and designation
	 * @param loginname
	 * @return
	 */
	public static String getNameDesignation( String loginname ) 
	{		
		if( null == loginname || loginname.trim().equals(""))
			return "" ;
		User user = null ;
		try {
			user = User.lookupAllByUserLogin(loginname);
		} catch (DatabaseException e) {			
			e.printStackTrace();
			return "" ;
		}
		if( null == user ) 
			return "" ;
		
//		String first_name = user.getFirstName() ;
//		String last_name = user.getLastName() ;
		int user_id = user.getUserId() ;
		Hashtable<String, String> user_info= null ;
		try {
			user_info = UserInfoManager.getUserInfo(user_id);
		} catch (TBitsException e) {
			e.printStackTrace();
			return user.getDisplayName() ; // first_name + "  " + last_name ;
		}	
		catch( IllegalArgumentException e )
		{
			e.printStackTrace() ;
			LOG.debug("Data base does not have the userInfo for userLogin : " + loginname ) ;
			return user.getDisplayName() ; // first_name + "  " + last_name ;
		}
				
		String designation = user_info.get(UserInfoManager.DESIGNATION) ;
		String sex = user_info.get(UserInfoManager.SEX) ;

		String nd = "" ;
		if( !sex.trim().equals(""))
		{ 
			nd = sex.trim().equalsIgnoreCase("M") ? "Mr. " : "Ms. " ;
		}
		nd += user.getDisplayName() ; //first_name + "  " + last_name ;
		nd += ",  " + designation ;
		
		return nd ;
	}
	

	
	/**
	 * takes the input list of cc as userLogins and generates html code of their complete name
	 * @param ccs
	 * @return
	 */
	public static String getCCs( String ccs ) 
	{	
		if( null == ccs || ccs.trim().equals("") )
			return "" ;
		
		String ccList = "<br>" ;
		String[] ccArray = ccs.split(",") ;
		for( int i = 0 ; i < ccArray.length ; i++ )
		{
			String ccName = ccArray[i] ;
			if( null == ccName || ccName.trim().equals("") ) 
				continue ;
			ccList += getNameDesignation(ccName) + "<br>";
		}
		
		return ccList ;
	}
	
	public static ArrayList<String> getAttachListForRequest( String currAttachList , String prevAttachList ) 
	{	
		ArrayList<String> attList = new ArrayList<String>() ;
		if( null == currAttachList || currAttachList.trim().equals("") )
			return attList ;
		
		String fileNames = "<br>" ;
		Collection<AttachmentInfo> currAttach = AttachmentInfo.fromJson( currAttachList ) ;
		
		if( null != prevAttachList )
		{			
			Collection<AttachmentInfo> prevAttach = AttachmentInfo.fromJson( prevAttachList ) ;
			
			for( Iterator<AttachmentInfo> curr = currAttach.iterator() ; curr.hasNext() ;  )
			{
				AttachmentInfo c = curr.next() ;
				boolean incl = true ;
				for( Iterator<AttachmentInfo> prev = prevAttach.iterator() ; prev.hasNext() ; )
				{
					AttachmentInfo p = prev.next() ;
					if( p.requestFileId == c.requestFileId && p.repoFileId == c.repoFileId )
					{
						incl = false ;
						break ;
					}
				}
				
				if(incl)
				{
					attList.add(c.name) ;
					System.out.println("Including file : " + c.name );
				}
				else
					System.out.println("Excluding file : " +c.name);
				
			}
		}
		else
		{
			for( Iterator<AttachmentInfo> curr = currAttach.iterator() ; curr.hasNext() ;  )
			{
				attList.add( curr.next().name ) ;
			}
		}
		
		
		return attList ;
	}
	
	/**
	 * takes the Json string of the attachments for preview and generates the html code of 
	 * file names
	 * @param attachList
	 * @return
	 */
	public static String getAttachList( String attachList, Request prevRequest ) 
	{
		//Handle Attachments    
		JsonArray nja = null ;
        String fileNames = "<br>" ;
        if(attachList != null)
        {       
        	JsonParser jp = new JsonParser();        	
        	JsonObject mainObj = jp.parse(attachList).getAsJsonObject();
        	Set<Entry<String, JsonElement>> mainNode = mainObj.entrySet();
        	Iterator<Entry<String, JsonElement>> iter = mainNode.iterator();
        	while(iter.hasNext())
        	{        		
        		Entry<String, JsonElement> element = iter.next();        	
        		if( !element.getKey().equals(IPLConstants.CORR_OTHER_ATTACHMENTS_FIELD_NAME))
        			continue ;
        		
        		JsonElement files = element.getValue().getAsJsonObject().get("files") ;
        		System.out.println("Files : " + files ) ;
        		nja = files.getAsJsonArray() ;
        		break ;        		
        	}        	
        }	
        
        if( null == nja )
        	return "" ;
        		
        JsonArray fja = new JsonArray() ;
        
        if( null != prevRequest )
        {
        	String prevAL = prevRequest.myMapFieldToValues.get(Field.ATTACHMENTS) ;
        	System.out.println( "prev. attach = " + prevAL ) ;
        	JsonParser jsonParser = new JsonParser() ;
        	JsonElement pje = jsonParser.parse(prevAL) ;
        	JsonArray pja = null ;
        	if( pje.isJsonArray() )
        		pja = pje.getAsJsonArray() ; 
        	
        	for( int i = 0 ; i < nja.size() ; i++ )
        	{
        		JsonObject njo = nja.get(i).getAsJsonObject() ;
        		boolean incl = true ;
        		for( int j = 0 ; j < pja.size() ; j++ )
        		{
//        			JsonElement pe =  ;
        			JsonObject pjo = pja.get(j).getAsJsonObject() ;
        			if( pjo.get("repoFileId").getAsInt() == njo.get("repoFileId").getAsInt()
        				&& 
        				pjo.get("requestFileId").getAsInt() == njo.get("requestFileId").getAsInt() )
        			{
        				incl = false ;
        				break ;
        			}
        		}
        		
        		if( true == incl )
        		{
        			System.out.println("Including File : " + njo.toString() ) ;
        			fja.add(njo) ;
        		}
        		else
        		{
        			System.out.println("NOT Including File : " + njo.toString() ) ;
        		}
        	}
        }
        else fja = nja ;
        
        for( int j = 0 ; j < fja.size() ; j++ ) 
		{
			JsonElement file = fja.get( j ) ;			
			fileNames  += file.getAsJsonObject().get("name").toString() + "<br />" ;
		}
		return fileNames ;		
	}


	public static String getKindAtt( User user, Hashtable<String,String> info )
	{
//		String firstName = user.getFirstName() ;
//		String lastName = user.getLastName() ;
		String displayName = user.getDisplayName() ;
		String designation = info.get(UserInfoManager.DESIGNATION) ;
		String kind_att = "" ;
		String sex = info.get(UserInfoManager.SEX ) ;
		if(sex.equalsIgnoreCase("M"))
			kind_att += "Mr. " ;
		else if( sex.equalsIgnoreCase("F"))
			kind_att += "Ms. " ;
				
		kind_att += displayName ; // firstName + " " + lastName ;
		kind_att += ", " + designation ;
		
		return kind_att ;
	}


	public static String getDear( Hashtable<String,String> info )
		{
	//		String dear = "Dear " + ( assignee_sex.trim().equalsIgnoreCase("M") ? "Sir," : "Madam," );
			String dear = "Dear " ;
			String sex = info.get(UserInfoManager.SEX ) ;
			if( sex.equalsIgnoreCase("M"))
				dear += "Sir, " ;
			else if( sex.equalsIgnoreCase("F"))
				dear += "Madam, " ;
			else dear += "Sir/Madam, " ;
			
			return dear ;
		}


	public static String getDesignation( Hashtable<String,String> info )
	{
		return info.get(UserInfoManager.DESIGNATION) ;
	}


	public static String getLogger( User user ) 
	{
		return user.getDisplayName() ; // user.getFirstName() + " " + user.getLastName() ; 
	}


	public static String getCompany(Hashtable<String,String> info )
	{		
		String fullFirm = info.get(UserInfoManager.FULL_FIRM_NAME) ;
		return  fullFirm ;		
	}


	public static String getTo( Hashtable<String,String> info ) 
	{
		String to = "" ;
		String add = info.get(UserInfoManager.ADDRESS) ;
		add = add.replace("\n", "<br>") ;
		String fullFirm = info.get(UserInfoManager.FULL_FIRM_NAME) ;
		to += fullFirm + "<br>" + add ;
		return to ;
	}


}
