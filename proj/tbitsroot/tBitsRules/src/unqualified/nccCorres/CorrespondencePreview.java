package nccCorres;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Date;
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

//import sun.reflect.ReflectionFactory.GetReflectionFactoryAction;
import transbit.tbits.api.IProxyServlet;
import transbit.tbits.common.Configuration;
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
import transbit.tbits.webapps.TBitsError;
import transbit.tbits.webapps.WebUtil;

import static nccCorres.CorresConstants.* ;
import static nccCorres.GenCorresHelper.* ;

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

	public static final TBitsLogger LOG = TBitsLogger.getLogger("nccCorres");
    public static final String DEFAULT_MIME_TYPE = "application/octet-stream";    
    public static final String MY_NAME = "corr-preview" ;

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
		if( outFile == null || !outFile.exists())
		{
			System.out.println("The corr-preview file with path : " + file_path + " was not found.") ;
			Exception e = new Exception("The preview pdf file was not found. Please try clicking on preview again.") ;
			session.setAttribute(TBitsError.EXCEPTION_OBJECT, e) ;
			response.sendRedirect(WebUtil.getServletPath(request, "/error")) ;
			return ;
//			response.sendError(HttpServletResponse.SC_NOT_FOUND, "The file with name (" + fileName + ") was not found.") ;
		}
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
	        System.out.println("mimeType = " + mimeType );
	        
	        try {
	
	            //
	            // Open an inputstream to read the attachment 1MB at a time and
	            // output the same.
	            //
	        	Date start = new Date() ;
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
				Date end = new Date() ;
				System.out.println("Time taken to read and send the pdf file contents : " + ( end.getTime() - start.getTime() ) + " ms." );
	        } catch (FileNotFoundException fnfe) {
	        	fnfe.printStackTrace() ;
	        	Exception e = new Exception("The preview pdf file was not found. Please try clicking on preview again.") ;
	        	session.setAttribute(TBitsError.EXCEPTION_OBJECT, e) ;
	        	response.sendRedirect(WebUtil.getServletPath(request,"/error")) ;
	        	return ;
//	        	response.sendError(HttpServletResponse.SC_NOT_FOUND, "The file with name (" + fileName + ") was not found.") ;
	          //  throw new TBitsException("Requested file not found.");
	        } catch (Exception e) 
	        {
	        	e.printStackTrace() ;
	        	Exception e1 = new Exception("Cannot complete your request. Please try clicking on preview again or contact tBits team") ;
	        	session.setAttribute(TBitsError.EXCEPTION_OBJECT, e1) ;
	        	response.sendRedirect(WebUtil.getServletPath(request,"/error")) ;
	        	return ;
//	        	response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Cannot complete your request. Please try again or contact tBits team.") ;
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
		System.out.println("Inside the doPost of corr-preview.");
		try
		{			
			boolean isAddRequest = true ;
			String callerStr = request.getParameter("caller") ;
			if( callerStr != null )
			{	
				if( callerStr.trim().equals("1") || callerStr.trim().equals("3") ) // add-request and add-sub-request					
					isAddRequest = true ;
				else if( callerStr.trim().equals("2"))				
					isAddRequest = false ;
				else
					throw new TBitsException("Preview is only valid for Add Correspondence / Update Correspondence.");
			}
			else
				throw new TBitsException("Preview is only valid for Add Correspondence / Update Correspondence.");
			
			CorresObject co = new CorresObject(request);
			int reqId = 0 ;
			int sysId = 0 ;
			
			CorresObject oldCo = null ;
			if( isAddRequest == false )
			{				
				String sys_id = request.getParameter("systemId");
				String req_id = request.getParameter("requestId") ;
				System.out.println("Trying to get the old request : " + sys_id + "#" + req_id);
				if(null == req_id || null == sys_id )
					throw new TBitsException("System Id or Request Id cannot be null for update correspondence.") ;
				
				try
				{
					reqId = Integer.parseInt(req_id) ;
					sysId = Integer.parseInt(sys_id) ;
					Request req = Request.lookupBySystemIdAndRequestId(sysId, reqId) ;
					if( null == req )
						throw new TBitsException("Cannot find request : " + sysId + "#" + reqId );
					oldCo = new CorresObject(req) ;
				}catch(NumberFormatException nfe)
				{
					throw new TBitsException("Cannot find request : " + sys_id + "#" + req_id) ;
				}				
			}
			
	//		CorresObject.validate(isAddRequest, co, oldCo);
			CorresObject.checkProtocolConstraints(isAddRequest, co, oldCo);
			CorresObject.agencySpecificConstraints(isAddRequest, co, oldCo);
					
			String format = request.getParameter("format") ;
			if( null == format )
				format = "pdf" ;	
			
			String reportName = getReportFileName(co) ;
			//( CorresObject co, CorresObject prevCo, int requestId, String reportName, String format, int caller,  int fileType, Hashtable<String, String> params, Connection con  )
			Hashtable<String,String> reportParams = new Hashtable<String,String>() ;
			System.out.println("Finding next corr. no. : " );
			String corrNo = GenCorresHelper.getCorrNo(co, GenCorresHelper.PREVIEW, null ) ;
			System.out.println("Found next corr. no. : " + corrNo );
			if( corrNo != null )
			{
				if( co.corrType.getName().equals(CORR_CORR_TYPE_ION))
					reportParams.put(CorresConstants.REP_ION_REF, corrNo );
				
				reportParams.put(CorresConstants.REP_REF_NO, corrNo );
			}
			else
				throw new TBitsException("Cannot generate correspondence number.") ;
			System.out.println("Starting report generation.");
			Date start = new Date() ;
			File pdfFile = GenCorresHelper.generateReport(co, oldCo, reqId, reportName, format, isAddRequest, GenCorresHelper.PREVIEW, reportParams, null ) ;
			pdfFile.deleteOnExit();
			Date end = new Date() ;
			System.out.println("Miliseconds taken to create the pdf file : " + ( end.getTime() - start.getTime() ) + " ms.") ;
			if( null == pdfFile )
				throw new TBitsException("Unexpected Exception occurred while generating report.") ;
			// sent the file name of this pdf
			PrintWriter out     = response.getWriter();
			out.println( pdfFile.getName() ) ;
			out.flush();
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

	private void serialize(CorresObject co) 
	{
		try
		{
			File file = new File("/home/nitiraj/checkouts/tmp/serialObjs");
			if( ! file.exists() )
				file.createNewFile();
			
			FileOutputStream fos = new FileOutputStream(file, true) ;
			ObjectOutputStream oos = new ObjectOutputStream(fos) ;
			oos.writeObject(co);
			oos.flush();
			oos.close();
			fos.close();			
		}
		catch(Exception e)
		{
			e.printStackTrace() ;
		}		
	}

	public static void main(String[] args) 
	{	
		try
		{
		System.setOut(new PrintStream(new File("/home/nitiraj/checkouts/tbits/dist/tomcat/logs/catalina.out")));
		for( int i = 0 ; i < 1000 ; i++ )
		{			
				CorresObject co = getSerializedObject(1);
				CorresObject oldCo = null ;
						
				String format = "pdf" ;	
				
				String reportName = CorresConstants.NCC_REPORT_FILE ;
	
				Hashtable<String,String> reportParams = new Hashtable<String,String>() ;
				System.out.println("Finding next corr. no. : " );
				String corrNo = GenCorresHelper.getCorrNo(co, GenCorresHelper.PREVIEW, null ) ;
				System.out.println("Found next corr. no. : " + corrNo );
				if( corrNo != null )
					reportParams.put(CorresConstants.REP_REF_NO, corrNo );
				else
					throw new TBitsException("Cannot generate correspondence number.") ;
				System.out.println("Starting report generation.");
				Date start = new Date() ;
				// req_id == 0 | 10
				File pdfFile = GenCorresHelper.generateReport(co, oldCo, 0, reportName, format, true, GenCorresHelper.PREVIEW, reportParams, null ) ;
				pdfFile.deleteOnExit();
				Date end = new Date() ;
				System.out.println("Miliseconds taken to create the pdf file : " + ( end.getTime() - start.getTime() ) + " ms.") ;
				if( null == pdfFile )
					throw new TBitsException("Unexpected Exception occurred while generating report.") ;
				System.out.println("Obtained the pdf file with name : " + pdfFile );
				// sent the file name of this pdf
	//			PrintWriter out     = response.getWriter();
	//			out.println( pdfFile.getName() ) ;
	//			out.flush();
			}		
		}
		catch(TBitsException e )
		{
			e.printStackTrace() ;
//			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getDescription() ) ;
			return ;
		}
		catch( Exception e )
		{
			e.printStackTrace() ;
//			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage() ) ;
			return ;
		}
	}
	
	private static CorresObject getSerializedObject(int index ) throws IOException, ClassNotFoundException 
	{
		FileInputStream fis = new FileInputStream("/home/nitiraj/checkouts/tmp/serialObjs");		
		ObjectInputStream in = new ObjectInputStream(fis);
		CorresObject obj = null ;
		for( int i = 0 ; i < index ; i++ )
		{
			obj = ( CorresObject ) in.readObject();			
		}
		in.close() ;
        return obj;
	}

	public String getName() {
		// TODO Auto-generated method stub
		return MY_NAME;
	}
	
}
