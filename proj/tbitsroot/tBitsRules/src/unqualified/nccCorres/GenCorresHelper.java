package nccCorres;

import java.io.File;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
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
import transbit.tbits.common.TBitsLogger;
import transbit.tbits.common.Timestamp;
import transbit.tbits.common.Utilities;
import transbit.tbits.domain.BusinessArea;
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
import com.ibm.icu.text.DecimalFormat;
import com.ibm.icu.text.NumberFormat;

import static nccCorres.CorresConstants.* ;

public class GenCorresHelper 
{
//	public static final TBitsLogger LOG = TBitsLogger.getLogger("nccCorres");
	public static final int PREVIEW = 1;
	public static final int REAL = 2 ;
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
	
	public static String getUserList( Collection<User> users )
	{
		if( null == users )
			return "" ;
		
		Iterator<User> iter = users.iterator() ;
		StringBuffer userList = new StringBuffer() ;
		while(iter.hasNext())
		{
			User user = iter.next() ;
			userList.append(user.getUserLogin()).append((iter.hasNext() ? ", " : ""));
		}
		
		return userList.toString();
	}
	
	public static Hashtable<String, String> getReportParams(CorresObject co, CorresObject prevCo, int fileType, Hashtable<String,String>params, Connection con ) throws TBitsException 
	{		
		if( null == params )
			params = new Hashtable<String,String>() ;
	
		if( co.corrType.getName().equals(CORR_CORR_TYPE_ION))
		{
			return getIONParams(co,prevCo,fileType,params,con);			
		}
		
		// billing
		//String linkedReq = co.linkedRequests;
		String linkedReq = co.tempFieldBilling;
		String linkedReqID = null;
		String linkedReqBAPrefix = null;
		if(linkedReq != null) {
			if (linkedReq.trim().length() > 0) {
				ArrayList<String> linkdReqs = Utilities.toArrayList(linkedReq.trim());
				for( String r : linkdReqs )
				{
					String[] parts = r.split("#") ;
					if(null != parts && parts.length > 1 )
					{
						String sysPrefx = parts[0].trim() ;
						if(sysPrefx.equalsIgnoreCase(CorresConstants.CORR_CLIENT_BILLING_SYSPREFIX)
								|| sysPrefx.equalsIgnoreCase(CorresConstants.CORR_VENDOR_BILLING_SYSPREFIX) ) {
							linkedReqBAPrefix = sysPrefx;
							linkedReqID = parts[1].trim();
							break;
						}
					}
				}
			}
		}
		if (linkedReqBAPrefix != null && linkedReqID != null) {
			
			if (linkedReqBAPrefix.equalsIgnoreCase(CorresConstants.CORR_CLIENT_BILLING_SYSPREFIX)) {
				try {
					BusinessArea clBillBA = BusinessArea.lookupBySystemPrefix(linkedReqBAPrefix);
					int reqID = Integer.parseInt(linkedReqID);
					Request clBillRequest = Request.lookupBySystemIdAndRequestId(clBillBA.getSystemId(), reqID);
					if (clBillRequest == null)
						throw new TBitsException("Unable to Generate Request : " +linkedReqBAPrefix+ "#" +linkedReqID);
					
					Date dated = (Date)clBillRequest.getObject(CorresConstants.CL_CLIENT_LETTER_REF_DATED);
					
					params.put(REP_CLBILL_CONTRACT_REFERENCE, ((Type)(clBillRequest.getObject(CorresConstants.CL_CONTRACT_REFERENCE))).getDisplayName() );
					params.put(REP_CLBILL_LETTER_REFERENCE, clBillRequest.get(CorresConstants.CL_CLIENT_LETTER_REF));
					if(dated != null)
					params.put(REP_CLBILL_REF_DATED, clBillRequest.get(CorresConstants.CL_CLIENT_LETTER_REF_DATED).split("\\s")[0]);
					params.put(REP_CLBILL_INVOICE_NO, clBillRequest.get(CorresConstants.CL_INVOICENO));
					params.put(REP_CLBILL_INVOICE_NO_DATE, clBillRequest.get(CorresConstants.CL_PENDING_FROM).split("\\s")[0]);
					params.put(REP_CLBILL_BILL_DETAILS, clBillRequest.get(CorresConstants.CL_BILLDETAILS));
					params.put(REP_CLBILL_NET_PAYABLE, clBillRequest.get(CorresConstants.CL_NET_PAYABLE)
														+" "+((Type)clBillRequest.getObject(CorresConstants.CL_CURRENCY_TYPE)).getDisplayName());
					
				} catch (DatabaseException e) {
					e.printStackTrace();
					throw new TBitsException("Error occured : " + e.getDescription() );
				} catch (NumberFormatException nfe) {
					nfe.printStackTrace();
					throw new TBitsException("Error occured : " + nfe.getMessage() );
				}
			}
			
			if (linkedReqBAPrefix.equalsIgnoreCase(CorresConstants.CORR_VENDOR_BILLING_SYSPREFIX)) {
				DecimalFormat doubleDF = new DecimalFormat("#0.00");
				try {
					BusinessArea vnBillBA = BusinessArea.lookupBySystemPrefix(linkedReqBAPrefix);
					int reqID = Integer.parseInt(linkedReqID);
					Request vnBillRequest = Request.lookupBySystemIdAndRequestId(vnBillBA.getSystemId(), reqID);
					if (vnBillRequest == null)
						throw new TBitsException("Unable to Generate Request : " +linkedReqBAPrefix+ "#" +linkedReqID);
					
					Date dated = (Date)vnBillRequest.getObject(CorresConstants.VN_VENDOR_INVOICE_DATED);
					double taxesNdeductions = Double.parseDouble(vnBillRequest.get(CorresConstants.VN_TOTAL_TAXES_APPLICABLE))
													- Double.parseDouble(vnBillRequest.get(CorresConstants.VN_TOTAl_OTHER_DED));
					
					params.put(REP_VNBILL_VENDOR_ADDRESS, vnBillRequest.getSeverityId().getDescription().split(CorresConstants.VN_SPLITTER_DESCRIPTION_BOX)[4]);
					params.put(REP_VNBILL_VENDOR_NAME, vnBillRequest.getSeverityId().getDescription().split(CorresConstants.VN_SPLITTER_DESCRIPTION_BOX)[3]);
					params.put(REP_VNBILL_VENDOR_INVOICE_NO, vnBillRequest.get(CorresConstants.VN_VENDOR_INVOICE_NO));
					if (dated != null)
					params.put(REP_VNBILL_VENDOR_INVOICENO_DATED, vnBillRequest.get(CorresConstants.VN_VENDOR_INVOICE_DATED).split("\\s")[0]);
					params.put(REP_VNBILL_TOTAL_WORKORDER_VAL, vnBillRequest.get(CorresConstants.VN_TOTAL_WORKORDER_VALUE)
															+" "+((Type)vnBillRequest.getObject(CorresConstants.VN_CURRENCY_TYPE)).getDisplayName());
					params.put(REP_VNBILL_TAXES_N_DEDUCTION, doubleDF.format(taxesNdeductions)
															+" "+((Type)vnBillRequest.getObject(CorresConstants.VN_CURRENCY_TYPE)).getDisplayName());
					params.put(REP_VNBILL_NET_PAYABLE, vnBillRequest.get(CorresConstants.VN_NETPAYABLE)
															+" "+((Type)vnBillRequest.getObject(CorresConstants.VN_CURRENCY_TYPE)).getDisplayName());
					
				} catch (DatabaseException e) {
					e.printStackTrace();
					throw new TBitsException("Error occured : " + e.getDescription() );
				} catch (NumberFormatException nfe) {
					nfe.printStackTrace();
					throw new TBitsException("Error occured : " + nfe.getMessage() );
				}
				
				
			} 
		} // billing
		else {
		
			params.put(CorresConstants.REP_TO, getTo(co) ) ;
			params.put(CorresConstants.REP_KIND_ATTN, getKindAttn(co) ) ;
			params.put(CorresConstants.REP_DEAR, getDear(co) ) ;
			params.put(CorresConstants.REP_SUBJECT, co.subject) ;
			params.put(CorresConstants.REP_DESCRIPTION, co.description) ;	
			params.put(CorresConstants.REP_CC, getCCs(co) ) ;
			params.put(CorresConstants.REP_ATT, getAttachList( co,prevCo ) ) ;
			params.put(CorresConstants.REP_PROJECT, getProject(co)) ;
			params.put(CorresConstants.REP_FOR_COMPANY, getCompany(co));
			params.put(CorresConstants.REP_LOGGER, getLogger(co));
			params.put(CorresConstants.REP_SUBS, getSubs(co)) ;		
			String imageName = co.logger0User.getUserLogin() + ".gif" ;
			LOG.info( "imagename = " + imageName ) ;
			File imageFile = Configuration.findPath("tbitsreports/" + imageName);			
			if( imageFile != null )
			{
				String imageLocation = imageFile.getAbsolutePath() ;
				params.put(CorresConstants.REP_IMAGE_PATH, imageLocation ) ;
			}
		}
		return params ;
	}

	private static Hashtable<String, String> getIONParams(CorresObject co, CorresObject prevCo,
			int fileType, Hashtable<String, String> params, Connection con) 
	{
		params.put(REP_ION_FROM, getIONFrom(co) ) ;
		params.put(REP_ION_LOGGER, getIONLogger(co)) ;
		// REP_ION_REF filled before only
//		params.put(REP_ION_REF, getION);
		params.put(REP_ION_SUBJECT, getIONSubject(co)) ;
		params.put(REP_ION_DESCRIPTION, getIONDescription(co)) ;
		params.put(REP_ION_TO, getIONTo(co)) ;
		params.put(REP_ION_DEAR, getIONDear(co)) ;
		
		String imageName = co.logger0User.getUserLogin() + ".gif" ;
		LOG.info( "imagename = " + imageName ) ;
		File imageFile = Configuration.findPath("tbitsreports/" + imageName);			
		if( imageFile != null )
		{
			String imageLocation = imageFile.getAbsolutePath() ;
			params.put(CorresConstants.REP_ION_IMAGE_PATH, imageLocation ) ;
		}
		
		return params;
	}

	private static String getIONDear(CorresObject co) 
	{		
		String sex = co.ass0User.getSex() ;
		String dear = "Dear " ;
		if( sex.equalsIgnoreCase("M"))
			dear += " Sir," ;
		else if( sex.equalsIgnoreCase("F"))
			dear += " Madam," ;
		else
			dear += " Sir/Madam," ;
		return dear ;
	}

	private static String getIONDescription(CorresObject co) 
	{	
		return co.description ;
	}

	private static String getIONTo(CorresObject co) 
	{		
		return getName(co.ass0User) + ( ( null != co.ass0User.getDesignation() && !co.ass0User.getDesignation().trim().equals("") ) 
										? ",<br />" + co.ass0User.getDesignation().trim() : "" ) ;
	}

	private static String getIONSubject(CorresObject co) {
		return co.subject ;
	}

	private static String getIONLogger(CorresObject co) 
	{
		return getIONFrom(co) + ( ( null != co.logger0User.getFullFirmName() && !co.logger0User.getFullFirmName().trim().equals("") ) 
				? ",<br />" + co.logger0User.getFullFirmName().trim() : "" ) ;
	}

	private static String getIONFrom(CorresObject co) {
		return getName(co.logger0User) + ( ( null != co.logger0User.getDesignation() && !co.logger0User.getDesignation().trim().equals("") ) 
				? ",<br />" + co.logger0User.getDesignation().trim() : "" ) ;
	}

	private static String getSubs(CorresObject co) 
	{		
		String subsList = "" ;
		
		if( co.subscribers != null )
			for( User user : co.subscribers )
			{			
				subsList += getNameDesignation(user) + "<br />";
			}
		
		if( !subsList.equals("") )
			subsList = "<br />" + subsList ;
		
		return subsList ;
	}

	private static String getProject(CorresObject co) 
	{		
		return "KVK Nilachal (1 X 350 MW) Thermal Power Project";
	}

	private static String getLogger(CorresObject co) 
	{		
		return getNameDesignation(co.logger0User);
	}

	private static String getCompany(CorresObject co) 
	{	
		String ffn = co.logger0User.getFullFirmName();
		
		if( null != ffn && !ffn.trim().equals("") )
			return "For " + ffn ;
		
		return "" ;	
	}

	public static String getCorrNo(CorresObject co, int fileType, Connection con) throws TBitsException 
	{
		/*
		 * cases 
		 * 1 : generate both file and number
		 * 2 : generate only file with given number
		 * 3 : don't generate anything 
		 */
		if( co.generate.getName().equals(CorresConstants.CORR_GEN_DONT_GEN_ANYTHING))
		{
			if( fileType == PREVIEW )
				throw new TBitsException("You have selected not to generate correspondence file.") ;
			else 
				return "" ;
		}
		else if( co.logger0User.getFirmCode().equals(CORR_ORIG_CSEPDI) || co.logger0User.getFirmCode().equals(CORR_ORIG_PTPL))
		{
			return co.corrNo ;
		}
		else if( co.generate.getName().equals(CorresConstants.CORR_GEN_FILE_WITH_GIVEN_NUMBER))
		{
			if( null == co.corrNo || co.corrNo.trim().equals(""))
				throw new TBitsException("You have selected to generate correspondence file with given number but not provided the correspondence number.") ;
			else
			{
				return validateCorresNumber(co,con,fileType) ;									
			}
		}
		else
		{
			String corrNo = null ;
			if( fileType == PREVIEW )
				corrNo = getExpectedCorrNo(co, con) ;
			else
				corrNo = getRealCorrNo(co,con) ;
			
			if( null == corrNo )
				throw new TBitsException("Cannot generate the correspondence number.") ;
			
			return corrNo ;
		}
	}

	private static String validateCorresNumber(CorresObject co,Connection con, int fileType) throws TBitsException 
	{		
		co.corrNo = sanitize( co.corrNo );
		
		String maxIdName = getCorrMaxIdName(co);
		
		int expectedRunningNo = getMaxCorrNo(con,maxIdName);
		expectedRunningNo++ ;
		DecimalFormat df = new DecimalFormat("0000") ;
		String nextCorresId =  df.format(expectedRunningNo);
		
		String corrPrefix = getCorrPrefix(co) ;
		try
		{		
			int dashLoc = co.corrNo.lastIndexOf("-");
			if( dashLoc > 0 && (dashLoc < co.corrNo.length()-1))
			{
				String runNum = co.corrNo.substring(dashLoc + 1);
				int rn = Integer.parseInt(runNum);	
				
				String prefix = co.corrNo.substring(0, dashLoc);
				if(!prefix.equalsIgnoreCase(corrPrefix))
					throw new TBitsException("The prefix should be : " + corrPrefix );
				
				if(rn < 1 || rn > expectedRunningNo )
					throw new TBitsException("The running number for prefix(" + corrPrefix + ") is : " + nextCorresId );
				
				if( fileType == REAL && rn == expectedRunningNo )
				{
					incrAndGetCorrNo(con,maxIdName);
				}
				
				String rnStr = df.format(rn);
				co.corrNo = corrPrefix + "-" + rnStr ; 			
			}
			else
			{
				throw new Exception("Illegal corr. no.");
			}
			
			return co.corrNo;
		}
		catch(TBitsException e )
		{
			throw e ;
		}
		catch(Exception e)
		{
			e.printStackTrace();
			throw new TBitsException("Illegal value in Correspondence Number field.\nExpected value is : " + corrPrefix + "-" + nextCorresId ); 
		}
	}

	public static String getReportFileName(CorresObject co)
	{
		if( co.logger0User.getFirmCode().equals(CORR_ORIG_KNPL))
		{
			return KNPL_REPORT_FILE ;
		}
		else if( co.logger0User.getFirmCode().equals(CORR_ORIG_NCCB) || co.logger0User.getFirmCode().equals(CORR_ORIG_NCCP) )
		{
			if( co.corrType.getName().equals(CORR_CORR_TYPE_ION))
				return NCC_ION_REPORT_FILE ;
			// billing
			//String linkedReq = co.linkedRequests;
			String linkedReq = co.tempFieldBilling;
			String linkdReqBaSysprefix = null;
			if (linkedReq != null) {
				if (linkedReq.trim().length() > 0) {
					ArrayList<String> linkdReqs = Utilities.toArrayList(linkedReq.trim());
					for( String r : linkdReqs )
					{
						String[] parts = r.split("#") ;
						if(null != parts && parts.length > 1 )
						{
							String sysPrefx = parts[0].trim() ;
							if(sysPrefx.equalsIgnoreCase(CorresConstants.CORR_CLIENT_BILLING_SYSPREFIX)
									|| sysPrefx.equalsIgnoreCase(CorresConstants.CORR_VENDOR_BILLING_SYSPREFIX) ) {
								linkdReqBaSysprefix = sysPrefx;
								break;
							}
						}
					}
					if (linkdReqBaSysprefix != null) {
						if(linkdReqBaSysprefix.equalsIgnoreCase(CorresConstants.CORR_CLIENT_BILLING_SYSPREFIX))
							return NCC_CLIENT_BILLING_REPORT_FILE;
						if(linkdReqBaSysprefix.equalsIgnoreCase(CorresConstants.CORR_VENDOR_BILLING_SYSPREFIX))
							return NCC_VENDOR_BILLING_REPORT_FILE;
					}
					else 
						return NCC_REPORT_FILE;
				}
				return NCC_REPORT_FILE;
				
			} // billing
			else
				return NCC_REPORT_FILE;
			
		}
		else if( co.logger0User.getFirmCode().equals(CORR_ORIG_DCPL))
			return DCPL_REPORT_FILE ;
		else if( co.logger0User.getFirmCode().equals(CORR_ORIG_DESEIN))
			return DESIGN_REPORT_FILE ;
		else if( co.logger0User.getFirmCode().equals(CORR_ORIG_CSEPDI))
		{
			return CSEPDI_REPORT_FILE ;
		}
		else if( co.logger0User.getFirmCode().equals(CORR_ORIG_PTPL))
		{
			return CSEPDI_REPORT_FILE; // return the CSEPDI file itself.
		}
		else if( co.logger0User.getFirmCode().equals(CORR_ORIG_EDTD))
		{
			return EDTD_REPORT_FILE ;
		}
		else
			return  NCC_REPORT_FILE ;
	}
	public static String removeRedundantDashes(String corrNo )
	{
		if( null == corrNo || corrNo.equalsIgnoreCase("") ) 
			return "" ;
		
		String ncn = "" ;
		boolean gotD = false ;
		 
		for( int i = 0 ; i < corrNo.length() ; i++ )
		{
			if(  '-' == corrNo.charAt(i) )
			{
				if( true == gotD  )
					continue ;
				else 
				{
					ncn += corrNo.charAt(i) ;
					gotD = true ;
				}
			}
			else
			{
				ncn += corrNo.charAt(i) ;
				gotD = false ;
			}
		}
		
		if(ncn.length() == 0 )
			return ncn ;
		else
		{
			// remove trailing -es
			if( '-' == ncn.charAt(0) ) // first character
			{
				if( ncn.length() > 1 )
				{
					ncn = ncn.substring(1) ;				
				}
				else return "" ;
			}
			
			if('-' == ncn.charAt(ncn.length()-1)) // last character
			{
				if( ncn.length() > 1 )					
				{
					ncn = ncn.substring(0, ncn.length()-1) ;
				}
				else return "" ;
			}
		}
		
		return ncn ;
	}	
		
	public static String replaceStrangeDash( String str ) 
	{
		String out = "" ;
		if( null == str ) 
			return out ;
		
		char strangeDash1 = (char)150 ;
		char strangeDash2 = (char)8211 ;
		char validDash = '-' ;
//		String regex = "[" + strangeDash1 + strangeDash2 + "]" ;
//		String replacement = validDash+"" ;
		
		for( int i = 0 ; i < str.length() ; i++ )
		{
			if( str.charAt(i) == strangeDash1 || str.charAt(i) == strangeDash2 )
				out += validDash ;
			else
				out += str.charAt(i) ;
		}
		
		return out ;
	}
	
	private static String sanitize(String corrNo) 
	{		
		return removeRedundantDashes(replaceStrangeDash(corrNo));
	}

	public static int incrAndGetCorrNo(Connection con, String corrCat ) throws TBitsException
	{
		System.out.println("generating corr. no. for : " + corrCat );
		try {	
			CallableStatement stmt = con.prepareCall("stp_getAndIncrMaxId ?");
			stmt.setString(1, corrCat );
			ResultSet rs = stmt.executeQuery();
			if (rs.next()) {
				int id = rs.getInt("max_id");
				System.out.println("Returning the next corr. no. = " + id );
				return id;
			} else {
				throw new TBitsException("Cannot generate the next correspondence number");
			}
		} catch (SQLException e) {
			throw new TBitsException("Cannot generate the next correspondence number");
		}		
	}
	
	public static String getRealCorrNo(CorresObject co, Connection con) throws TBitsException 
	{
		String maxIdName = getCorrMaxIdName(co);
		int ncid = incrAndGetCorrNo(con,maxIdName);
				
		DecimalFormat df = new DecimalFormat("0000") ;
		String nextCorresId =  df.format(ncid); 
		String corresNo = getCorrPrefix(co) ;
		
		corresNo += "-" + nextCorresId ;
				
		return corresNo ;
	}

	public static File generateReport( CorresObject co, CorresObject prevCo, int requestId, String reportName, String format, boolean isAddRequest,  int fileType, Hashtable<String, String> params, Connection con  ) throws TBitsException
	 {
		IReportDocument ird = null ;
		TBitsReportEngine tre = null ;
		getReportParams(co, prevCo, fileType, params, con) ;
		try
		{
				tre = new TBitsReportEngine();
				if(tre == null)
				{
					System.out.println("Unable to get the instance of ReportEngine.");
					throw new TBitsException("Generating correspondence file failed.") ;
				}
				IReportRunnable reportDesign;
				reportDesign = tre.getReportDesign(reportName);
				if(reportDesign == null)
				{
					System.out.println("Unable to get the design instance of " + reportName);
					throw new TBitsException("Generating correspondence file failed.") ;
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
				
				// TODO :set report parameters
				HashMap<String,String> reportParamMap = new HashMap<String,String>() ;
				reportParamMap.put(CorresConstants.REP_RID, requestId+"") ;

				String tbits_base_url = WebUtil.getNearestPath("") ;
				System.out.println( "tbits_base_url : " + tbits_base_url ) ;
				reportParamMap.put(CorresConstants.REP_TBITS_BASE_URL_KEY, tbits_base_url );
				
				Date start = new Date() ;				
				ird = tre.getReportDocument(reportDesign, reportParamMap) ;
				Date end = new Date() ;
				
				System.out.println("got the report document as : " + ird + ". Time taken : " + ( end.getTime() - start.getTime() ) +"\n Now creating the report" );
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
					LOG.error("Output file is null" ) ;
					throw new TBitsException("Generating correspondence file failed.") ;
				}						
		} catch (EngineException e) {
			e.printStackTrace();
			throw new TBitsException("Generating correspondence file failed.") ;
		} catch (SemanticException e) {			
			e.printStackTrace();
			throw new TBitsException("Generating correspondence file failed.") ;
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
			throw new TBitsException("Generating correspondence file failed.") ;
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
	
	public static String getNameDesignation( User user ) 
	{				
		String name = getName(user) ;
		
		String designation = user.getDesignation() ;
		String sex = user.getSex() ;
		
		String nd = "" ;
		if( null != sex && !sex.trim().equals(""))
		{ 
			if( sex.equalsIgnoreCase("M"))
				nd = "Mr. " ; 
			else if( sex.equalsIgnoreCase("F") )
				nd = "Ms. " ;
		}
	
		nd += name ;
		if( null != designation && !designation.trim().equals(""))
			nd +=  ",  " + designation ;
		
		return nd ;
	}
	

	
	/**
	 * takes the input list of cc as userLogins and generates html code of their complete name
	 * @param ccs
	 * @return
	 */
	public static String getCCs( CorresObject co ) 
	{
		/*
		 * String subsList = "" ;
		
		if( co.subscribers != null )
			for( User user : co.subscribers )
			{			
				subsList += getNameDesignation(user) + "<br />";
			}
			
		return subsList ;
		
		 */	
		HashSet<User> users = new HashSet<User>() ; 
		String ccList = "" ;	
		if( co.ccs != null )
			users.addAll(co.ccs);
		
//		if( co.subscribers != null )
//			users.addAll(co.subscribers);
//		
		for( User user : users )
		{
			ccList += getNameDesignation(user) + "<br />" ;
		}
		
		if(!ccList.equals(""))
			ccList = "<br />" + ccList ;
		
		return ccList ;
	}

	public static String getAttachList( CorresObject co, CorresObject prevCo ) 
	{   
		String fileNames = "<br />" ;
		if( null == co || null == co.otherAttach || co.otherAttach.size() == 0 )
			return fileNames ;
		
        if( null != prevCo && null != prevCo.corrAttach && prevCo.corrAttach.size() != 0 )
        {        	
        	for( AttachmentInfo nai : co.otherAttach )
        	{      
        		boolean include = true ;
        		for( AttachmentInfo pai : prevCo.otherAttach )
        		{        		
        			if( nai.repoFileId == pai.repoFileId
        				&& 
        				nai.requestFileId == pai.requestFileId
        			  )
        			{
        				include = false ;
        				break ;
        			}
        		}    
        		if( include == true )
        			fileNames += nai.name + "<br />" ;
        	}
        }
        else
        {
        	for( AttachmentInfo nai : co.otherAttach )
        	{  
        		fileNames += nai.name + "<br />" ;
        	}
        }
		return fileNames + "<br />";		
	}


	public static String getKindAttn(CorresObject co)
	{
		String name = getName(co.ass0User) ;
		String designation = co.ass0User.getDesignation() ;
		String kind_att = "" ;
		String sex = co.ass0User.getSex() ;
		if(sex.equalsIgnoreCase("M"))
			kind_att += "Mr. " ;
		else if( sex.equalsIgnoreCase("F"))
			kind_att += "Ms. " ;
				
		kind_att += name ;
		if( null != designation && !designation.trim().equals(""))
			kind_att += ", " + designation ;
		
		return kind_att ;
	}


	public static String getDear( CorresObject co )
	{
		String dear = "Dear " ;
		String sex = co.ass0User.getSex();
		if( sex.equalsIgnoreCase("M"))
			dear += "Sir, " ;
		else if( sex.equalsIgnoreCase("F"))
			dear += "Madam, " ;
		else dear += "Sir/Madam, " ;
		
		return dear ;
	}

	public static String getLogger( User user ) 
	{
		return getName(user) ; 
	}


//	public static String getCompany(Hashtable<String,String> logInfo, Hashtable<String,String> assInfo, Type corrProt )
//	{		
//		String fullFirm = logInfo.get(UserInfoManager.FULL_FIRM_NAME) ;
//		if( assInfo.get(UserInfoManager.FIRM).trim().equalsIgnoreCase(KskConstants.CorresConstants) && logInfo.get(UserInfoManager.FIRM).equalsIgnoreCase(KskConstants.CorresConstants) )
//		{
//			fullFirm = KskConstants.CorresConstants ;
//		}
//		return  "for " + fullFirm ;		
//	}


	public static String getTo( CorresObject co) 
	{
		String fullFirm = co.ass0User.getFullFirmName() ;		
		String to = "" ;
		String add = co.ass0User.getFirmAddress();
		add = add.replace("\n", "<br>") ;
		
		to += fullFirm + "<br>" + add ;
		return to ;
	}


	public static int getMaxCorrNo(Connection con,String name ) throws TBitsException
	{
		String query = "select id from max_ids where name='" + name + "'" ;
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
			throw new TBitsException("Cannot find the next correspondence number") ;
		}
		catch(NumberFormatException n )
		{
			throw new TBitsException("Cannot find the next correspondence number") ;
		}
	}

	public static String getCorrMaxIdName(CorresObject co) throws TBitsException
	{
		// for both ION and non-ION .. and for both NCCB and NCCP
		if( co.logger0User.getFirmCode().equals(CORR_ORIG_NCCB) ||
				co.logger0User.getFirmCode().equals(CORR_ORIG_NCCP)
		)
		{
			String projectCode = "NPT10109" ; // hard-coded		
			String financialYear = getFinancialYear() ; 
			String cp = projectCode + "-" + financialYear ;
			return cp ;
		}
		else if( co.logger0User.getFirmCode().equals(CORR_ORIG_DCPL))
		{
			return DCPL_CORRES_MAX_ID ;
		}
		else if (co.logger0User.getFirmCode().equals(CORR_ORIG_KNPL))
		{
//			KNPL-NCCP-CR-Package-10-11-0001
//
//			For E.g:If Contract reference code is 1 then the number will be like
//
//			KNPL-NCCP-1-BOP-10-11-0001
			return co.logger0User.getFirmCode() + 
								"-" + getContRef(co.contractReference) +
								"-" + co.pack.getName() + "-" + getFinancialYear();
		}
		else if (co.logger0User.getFirmCode().equals(CORR_ORIG_DESEIN))
		{
			return DESEIN_CORRES_MAX_ID ;
		}
		else
		{
			return getCorrPrefix(co);
		}
	}

	public static String getCorrPrefix(CorresObject co ) throws TBitsException
	{
		if( co.logger0User.getFirmCode().equals(CORR_ORIG_KNPL))
		{
			return co.logger0User.getFirmCode() + "-" + co.ass0User.getFirmCode() +
				   "-" + getContRef(co.contractReference) +
				   "-" + co.pack.getName() + "-" + getFinancialYear();
		}else if( co.logger0User.getFirmCode().equals(CORR_ORIG_DCPL))
		{
			return DCPL_PROJECT_CONSTANT + "-" + getDisciplineConstant( co ) ;
		}
		else if ( co.logger0User.getFirmCode().equals(CORR_ORIG_DESEIN))
		{
			String prefix = DESEIN_PROJECT_CONSTANT ;
			if( co.location.getName().equals(CORR_LOC_HYD))
				prefix += "-HYD" ;
			
			return  prefix ;
		}
		
		if(co.corrType.getName().equals(CORR_CORR_TYPE_ION))
		{
			String projectCode = "NPT10109" ; // hard-coded
			String contRefCode = CorresConstants.getContRef(co.contractReference) ;
			String financialYear = getFinancialYear() ;
			
			String cp = projectCode + "-" + contRefCode + "-" + co.originator.getName() + "-" + co.recepient.getName() +
						"-" + co.corrType.getName() +"-"+ financialYear ;
			return cp ;
		}
		else
		{
			String projectCode = "NPT10109" ; // hard-coded
			String contRefCode = CorresConstants.getContRef(co.contractReference) ;
			String financialYear = getFinancialYear() ; 
			String cp = projectCode + "-" + contRefCode + "-" + co.originator.getName() + "-" + co.recepient.getName() +
						"-" + co.corrType.getName() + "-" + financialYear ;
			return cp ;
		}
	}

	private static String getFinancialYear() 
	{
		Calendar ndd = Calendar.getInstance() ;
		int currMonth = ndd.get(Calendar.MONTH) ;
		int currYear = ndd.get(Calendar.YEAR);
		
		if( currMonth < Calendar.APRIL )
		{
			Calendar other = Calendar.getInstance(); 
			other.add(Calendar.YEAR, -1);
			Timestamp nowTs = new Timestamp(ndd.getTimeInMillis()) ;
			Timestamp otherTs = new Timestamp(other.getTimeInMillis()) ;
			return otherTs.toCustomFormat("yy") + "-" + nowTs.toCustomFormat("yy") ;
		}
		else
		{
			Calendar other = Calendar.getInstance(); 
			other.add(Calendar.YEAR, 1);
			Timestamp nowTs = new Timestamp(ndd.getTimeInMillis()) ;
			Timestamp otherTs = new Timestamp(other.getTimeInMillis()) ;
			return nowTs.toCustomFormat("yy") + "-" + otherTs.toCustomFormat("yy") ;
		}		
	}

	public static String getExpectedCorrNo( CorresObject co, Connection con ) throws TBitsException
	{	
		String maxIdName = getCorrMaxIdName(co);
		// generate complete correspondence no.		
		int ncid = getMaxCorrNo( con, maxIdName );
		ncid += 1 ;
		DecimalFormat df = new DecimalFormat("0000") ;
		String nextCorresId =  df.format(ncid);  //Integer.toString(fc.getMaxRequestId() + 1);
	
		String corresNo = getCorrPrefix(co) ;
		// get year 		
		corresNo += "-" + nextCorresId ;
		
		return "[Likely]" + corresNo ;
	}


}
