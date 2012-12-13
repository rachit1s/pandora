package kskCorres;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashSet;
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
import transbit.tbits.common.Utilities;
import transbit.tbits.domain.BusinessArea;
import transbit.tbits.domain.Field;
import transbit.tbits.domain.Request;
import transbit.tbits.domain.Type;
import transbit.tbits.domain.User;
import transbit.tbits.exception.TBitsException;
import transbit.tbits.webapps.TBitsError;
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

	public static final TBitsLogger LOG = TBitsLogger.getLogger("KSKCORRES");
	static final String ERR_CON_ONCLOSE = "Exception while closing the connection object.";
	//private static final String TBITS_BASE_URL_KEY = "tbits_base_url";
//	private static final String MULTIPART_CONTENT_TYPE		= "multipart/form-data";
	// Default Mime Type.
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
		File tbitsTmp = Configuration.findPath("webapps/tmp");
		String file_path = tbitsTmp + "/" + fileName ;
		System.out.println("tbits tmp dir : " + tbitsTmp.getAbsolutePath());		
		File outFile = new File(file_path) ;
		System.out.println("preview file : " + outFile.getAbsolutePath());
		if( outFile == null || !outFile.exists())
		{
			LOG.info("The corr-preview file with path : " + file_path + " was not found.") ;
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
		// TODO: assuming all the users in cc, subscribers, logger, assignee are userlogins
		// parameters to set
		
		// TODO : check if all the values are available and not null		
		try
		{			
			CoOb coob = new CoOb(request) ;
			String pdfFileName = GenCorresHelper.generateAndGetFileName(coob);
			if( pdfFileName == null )
				throw new TBitsException("Exception occured while generating report. Please try again.");
			PrintWriter out     = response.getWriter();
			out.println( pdfFileName ) ;
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
	
	public static void main(String argv[] )
	{
		try 
		{
			System.out.println( "max_id = " + GenCorresHelper.getMaxCorrNo("nitiraj") ) ;
		} 
		catch (TBitsException e) 
		{
			e.printStackTrace();
		}
	}
	
	
}
