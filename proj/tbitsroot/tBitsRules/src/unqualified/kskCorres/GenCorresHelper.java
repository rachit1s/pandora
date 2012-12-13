package kskCorres;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
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
import transbit.tbits.common.Configuration;
import transbit.tbits.common.DataSourcePool;
import transbit.tbits.common.DatabaseException;
import transbit.tbits.common.PropertiesHandler;
import transbit.tbits.common.TBitsLogger;
import transbit.tbits.common.Timestamp;
import transbit.tbits.domain.Field;
import transbit.tbits.domain.Request;
import transbit.tbits.domain.Type;
import transbit.tbits.domain.User;
import transbit.tbits.exception.TBitsException;
import transbit.tbits.report.TBitsReportEngine;
import transbit.tbits.webapps.WebUtil;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import static kskCorres.KskConstants.*;

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
				
				// FIXME : this is not the directory not the correct path fo the director.
				File tempDir = Configuration.findPath("webapps/tmp");
				
				System.out.println("tempDir : " + tempDir.getAbsolutePath());
				String fileName = params.get(REP_CURRENTCORRESPONDENCE_NO);
				File outFile = null ;
				// always create pdf file.
				outFile = File.createTempFile(fileName, ".pdf", tempDir) ;
				outFile = tre.getPDFReport(ird, outFile);

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
		} catch (IOException e) {
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
	public static String getName( User user)
	{
		String name = user.getDisplayName() ;
		if( null == name || name.trim().equals(""))
		{
			name = "" ;
			String first_name = user.getFirstName() ;
			String last_name = user.getLastName() ;
			if( null != first_name )
				name = first_name ;
			if( null != last_name )
				name += " " + last_name ;
			
			if( name.trim().equals(""))
				name = user.getUserLogin() ;
		}
		
		return name ;
	}
	
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
			
		String name = getName(user) ;
		int user_id = user.getUserId() ;
		Hashtable<String, String> user_info= null ;
		try {
			user_info = UserInfoManager.getUserInfo(user_id);
		} catch (TBitsException e) {
			e.printStackTrace();
			return name ;
		}	
				
		String designation = user_info.get(UserInfoManager.DESIGNATION) ;
		String sex = user_info.get(UserInfoManager.SEX) ;

		String nd = "" ;
		if( !sex.trim().equals(""))
		{ 
			nd = sex.trim().equalsIgnoreCase("M") ? "Mr. " : "Ms. " ;
		}
		
		nd += name + ",  " + designation ;
		
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
	
	
	public static String getAttachmentJsonForField( String attachList, String fieldName )
	{
		//Handle Attachments
		String attFilesJson = null ;
		JsonArray nja = null ;
        if(attachList != null)
        {       
        	JsonParser jp = new JsonParser();        	
        	JsonObject mainObj = jp.parse(attachList).getAsJsonObject();
        	Set<Entry<String, JsonElement>> mainNode = mainObj.entrySet();
        	Iterator<Entry<String, JsonElement>> iter = mainNode.iterator();
        	while(iter.hasNext())
        	{        		
        		Entry<String, JsonElement> element = iter.next();        	
        		if( !element.getKey().equals(fieldName))
        			continue ;
        		
        		JsonElement files = element.getValue().getAsJsonObject().get("files") ;
        		System.out.println("Files : " + files ) ;
        		nja = files.getAsJsonArray() ;
        		attFilesJson = files.toString();
        		break ;        		
        	}
        }
        
        return attFilesJson ;
	}
	/**
	 * takes the Json string of the attachments for preview and generates the html code of 
	 * file names
	 * @param attachList
	 * @return
	 */
	
	public static String getAttachList( Collection<AttachmentInfo> cur, Collection<AttachmentInfo> prev )
	{
		String retStr = "" ;
		if( null == cur )
			return retStr ;
		
		for( AttachmentInfo currInfo : cur )
    	{
    		boolean incl = true ;
    		if( null != prev )
	    		for( AttachmentInfo prevInfo : prev )
	    		{
	    			if( prevInfo.repoFileId == currInfo.repoFileId
	    				&& 
	    				prevInfo.requestFileId == prevInfo.requestFileId )
	    			{
	    				incl = false ;
	    				break ;
	    			}
	    		}
    		
    		if( incl )
    			retStr += currInfo.name + "<br />" ;
    	}
		
		return retStr ;
	}
	
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
        		if( !element.getKey().equals(KskConstants.CORR_OTHER_ATTACHMENTS_FIELD_NAME))
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
        	String prevAL = prevRequest.get(Field.ATTACHMENTS) ;
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
		String name = getName(user) ;
//		String firstName = user.getFirstName() ;
//		String lastName = user.getLastName() ;
		String designation = info.get(UserInfoManager.DESIGNATION) ;
		String kind_att = "" ;
		String sex = info.get(UserInfoManager.SEX ) ;
		if(sex.equalsIgnoreCase("M"))
			kind_att += "Mr. " ;
		else if( sex.equalsIgnoreCase("F"))
			kind_att += "Ms. " ;
				
		kind_att += name ;
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
		return getName(user) ; 
	}


	public static String getCompany(Hashtable<String,String> logInfo, Hashtable<String,String> assInfo, Type corrProt )
	{		
		String fullFirm = logInfo.get(UserInfoManager.FULL_FIRM_NAME) ;
		if( assInfo.get(UserInfoManager.FIRM).trim().equalsIgnoreCase(KskConstants.SEPCO_FIRM_NAME) && logInfo.get(UserInfoManager.FIRM).equalsIgnoreCase(KskConstants.WPCL_FIRM_NAME) )
		{
			fullFirm = KskConstants.WPCL_FULL_FIRM_NAME ;
		}
		return  "for " + fullFirm ;		
	}


	public static String getTo( Hashtable<String,String> assInfo,Hashtable<String,String> logInfo, Type corrProt ) 
	{
		String fullFirm = assInfo.get(UserInfoManager.FULL_FIRM_NAME) ;
		if( logInfo.get(UserInfoManager.FIRM).trim().equalsIgnoreCase(KskConstants.SEPCO_FIRM_NAME) && assInfo.get(UserInfoManager.FIRM).equalsIgnoreCase(KskConstants.WPCL_FIRM_NAME) )
		{
			fullFirm = KskConstants.WPCL_FULL_FIRM_NAME ;
		}
		String to = "" ;
		String add = assInfo.get(UserInfoManager.ADDRESS) ;
		add = add.replace("\n", "<br>") ;
		
		to += fullFirm + "<br>" + add ;
		return to ;
	}


	public static int getMaxCorrNo(String name ) throws TBitsException
	{
		String query = "select id from max_ids where name='" + name + "'" ;
		Connection con = null ;
		try
		{
			con = DataSourcePool.getConnection() ;
			PreparedStatement ps = con.prepareStatement(query) ;
			ResultSet rs = ps.executeQuery() ;
			if( rs.next()  )
			{
				String max_id = rs.getString("id") ;
				if( null == max_id )
				{
					// treat this as 0 
					return 0 ;
				}
				else
				{
					return Integer.parseInt(max_id) ;
				}
			}
			else
			{
				// treat this also as 0
				return 0 ;
			}
		}
		catch(SQLException s)
		{
			throw new TBitsException(s) ;
		}
		catch(NumberFormatException n )
		{
			throw new TBitsException(n) ;
		}
		finally
		{
			if( null != con )
			{
				try {
					con.close() ;
				} catch (SQLException e) {
					CorrespondencePreview.LOG.warn(CorrespondencePreview.ERR_CON_ONCLOSE) ;
					e.printStackTrace();
				}
			}
		}
	}


	public static String getCorrPrefix(Hashtable<String,String> logger0Info, Type corrType ) throws TBitsException
	{
		String corresPrefix = "KMP-" ;
		
		String firmLetter = "?" ;
		if( logger0Info.get(UserInfoManager.FIRM).toUpperCase().equalsIgnoreCase(WPCL_FIRM_NAME) )
			firmLetter = "K";
		else if( logger0Info.get(UserInfoManager.FIRM).toUpperCase().equalsIgnoreCase(SEPCO_FIRM_NAME) )
			firmLetter = "S" ;
		else
			throw new TBitsException("Illegal value for firm_code for the user for the logger." );
		
		corresPrefix += firmLetter + "-" + logger0Info.get(UserInfoManager.LOCATION) + "-" ;		
		corresPrefix += corrType.getName() ;
		
		return corresPrefix ;
	}
	public static String getExpectedCorrNo( Hashtable<String,String> logger0Info, Type corrType ) throws SQLException, TBitsException
	{	    	
		String corresNo = getCorrPrefix(logger0Info, corrType ) ;
		// generate complete correspondence no.
		String yr = new Timestamp().toCustomFormat("yy") ;
		corresNo += "-" + yr ;
		int ncid;
		try {
			ncid = getMaxCorrNo( corresNo );
		} catch (TBitsException e) {
			e.printStackTrace();
			return "Cannot Generate Correspondence No." ;			
		}
		ncid += 1 ;
		String nextCorresId =  Integer.toString(ncid);  //Integer.toString(fc.getMaxRequestId() + 1);
		switch( nextCorresId.length() )
		{
			case 1 : nextCorresId = "000" + nextCorresId ; break ;
			case 2 : nextCorresId = "00" + nextCorresId ; break ;
			case 3 : nextCorresId = "0" + nextCorresId ; break ;
		}
		
		// get year 		
		corresNo += "-" + nextCorresId ;
		
		return "[Likely]" + corresNo ;
	}


	public static String generateAndGetFileName(CoOb coob) throws TBitsException
		{
			Hashtable<String, String> params = new Hashtable<String,String>() ;
			
			String corresNo = "";
			try {
				corresNo = getExpectedCorrNo( coob.myLogger0Info, coob.myCorrType );
			} 
			catch (SQLException e1) 
			{		
				e1.printStackTrace();
				throw new TBitsException("Exception while generating correspondence number. Please try again.");
			}
			params.put(KskConstants.REP_CURRENTCORRESPONDENCE_NO, corresNo ) ;
			
			params.put(KskConstants.REP_TO, getTo(coob.myAss0Info, coob.myLogger0Info, coob.myCorrProt) ) ;
			params.put(KskConstants.REP_KIND_ATT, getKindAtt(coob.myAss0User , coob.myAss0Info ) ) ;
	//		String dear = "Dear " + ( assignee_sex.trim().equalsIgnoreCase("M") ? "Sir," : "Madam," );
			params.put(KskConstants.REP_DEAR, getDear(coob.myAss0Info) ) ;
			params.put(KskConstants.REP_TITLE, coob.subject) ;
			String description = coob.description ;
			try 
			{
				description = WebUtil.prepareValidHtml(description);
			} catch (IOException e) 
			{			
				e.printStackTrace();
			}
			params.put(KskConstants.REP_DESCRIPTION, description ) ;			
			params.put(KskConstants.REP_COMPANY, getCompany( coob.myLogger0Info, coob.myAss0Info, coob.myCorrProt ) ) ;
			params.put(KskConstants.REP_LOGGER, getLogger( coob.myLogger0User ) ) ;
			params.put(KskConstants.REP_DESIGNATION, getDesignation( coob.myLogger0Info ) ) ;			
			// creating cc_list 
			String ccs = getCCs(coob.mySubList) ;
			params.put(KskConstants.REP_CC, ccs ) ;
			//TODO :changes required here
			String attachList = getAttachList( coob.attInfos , ( coob.myPrevRequest == null ? null :  coob.myPrevRequest.getAttachments() ) );
			params.put( KskConstants.REP_ATTACHMENT, attachList ) ;
			
			String imageName = coob.myLogger0User.getUserLogin() + ".gif" ;
			System.out.println( "imagename = " + imageName ) ;
			File imageFile = Configuration.findPath("tbitsreports/" + imageName);			
			if( imageFile != null )
			{
				String imageLocation = imageFile.getAbsolutePath() ;
				params.put(KskConstants.REP_IMAGE_PATH, imageLocation ) ;
			}
			
			String currentDate = new Timestamp().toCustomFormat("yyyy-MM-dd") ;
			params.put(KskConstants.REP_CURRENT_DATE, currentDate) ;
	
			String loggerFirm = coob.myLogger0Info.get(UserInfoManager.FIRM) ;
			String reportName = "CorrespondanceTemplate.rptdesign" ;
	        reportName = loggerFirm.trim().toUpperCase() + reportName ;		
			// TODO :set report parameters
			HashMap<String,String> reportParamMap = new HashMap<String,String>() ;
	
			if( coob.isAddRequest )
			{
				reportParamMap.put(KskConstants.REP_RID, "0") ;
			}
			else 
			{
				String reqID = coob.myPrevRequest.getRequestId() + "" ;
				reportParamMap.put(KskConstants.REP_RID, reqID ) ;
			}
	
			String tbits_base_url = WebUtil.getNearestPath("") ; // PropertiesHandler.getProperty(TBitsPropEnum.KEY_NEAREST_INSTANCE)
			System.out.println( "tbits_base_url : " + tbits_base_url ) ;
			reportParamMap.put(KskConstants.REP_TBITS_BASE_URL_KEY, tbits_base_url );
	
			String format = "pdf";
			File pdfFile = generateReport( reportName, params, reportParamMap, format ) ;
			if( null == pdfFile )
				throw new TBitsException("Unexpected Exception occurred while generating report.") ;
			// sent the file name of this pdf
			return pdfFile.getName() ;
		}


}
