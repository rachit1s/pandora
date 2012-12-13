package transbit.tbits.webapps;

import static transbit.tbits.Helper.TBitsConstants.PKG_WEBAPPS;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.SQLException;

import javax.activation.MimetypesFileTypeMap;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import transbit.tbits.Helper.TBitsPropEnum;
import transbit.tbits.api.APIUtil;
import transbit.tbits.common.Configuration;
import transbit.tbits.common.DatabaseException;
import transbit.tbits.common.PropertiesHandler;
import transbit.tbits.common.TBitsLogger;
import transbit.tbits.exception.TBitsException;

public class DownloadAndDelete extends HttpServlet implements TBitsPropEnum {
	// Application Logger.
    public static final TBitsLogger LOG = TBitsLogger.getLogger(PKG_WEBAPPS);

    // Default Mime Type.
    public static final String DEFAULT_MIME_TYPE = "application/octet-stream";
    
    // Location of attachments.
//    public static String ourTmpLocation;
    
    // Static block to initialize the tmp location.
//    static {
//        try {
//        	ourTmpLocation = Configuration.findAbsolutePath(PropertiesHandler.getProperty(KEY_TMPDIR));
//          } catch (IllegalArgumentException e) {
//            LOG.severe(e.toString(), e);
//        }
//    }
    
    /**
     * This method services the Http-Get Requests to this servlet.
     *
     * @param  aRequest          the HttpServlet Request Object
     * @param  aResponse         the HttpServlet Response Object
     *
     * @throws ServletException
     * @throws IOException
     */
    public void doGet(HttpServletRequest aRequest, HttpServletResponse aResponse) throws ServletException, IOException {
    	HttpSession session = aRequest.getSession(true);

        try {
        	handleGetRequest(aRequest, aResponse);
        } catch (DatabaseException de) {
            session.setAttribute("ExceptionObject", de);
            aResponse.sendRedirect(WebUtil.getServletPath(aRequest, "error"));
        } catch (TBitsException de) {
            session.setAttribute("ExceptionObject", de);
            LOG.info("",(de));
            aResponse.sendRedirect(WebUtil.getServletPath(aRequest, "error"));
        } catch (SQLException e) {
            session.setAttribute("ExceptionObject", e);
            LOG.info("",(e));
            aResponse.sendRedirect(WebUtil.getServletPath(aRequest, "error"));
        }
    }
    
    /**
     * This method services the Http-Post Requests to this servlet.
     *
     * @param aRequest the HttpServlet Request Object
     * @param aResponse the HttpServlet Response Object
     * @throws ServletException
     * @throws IOException
     */
    public void doPost(HttpServletRequest aRequest, HttpServletResponse aResponse) throws ServletException, IOException {
        doGet(aRequest, aResponse);
    }
    
    /**
     * Method that actually handles the Get Request.
     * It requires the following
     * 	pathinfo: /sysPrefix
     *  mandatory params: sys_id, request_id, request_file_id
     *  optional params: field_id (default is 22), action_id (default is max_action_id)
     * @param aRequest          the HttpServlet Request Object
     * @param aResponse         the HttpServlet Response Object
     * @throws ServletException
     * @throws IOException
     * @throws DESTBitsExceptionthrows DatabaseException
     * @throws FileNotFoundException
     */
    private void handleGetRequest(HttpServletRequest aRequest, HttpServletResponse aResponse) throws ServletException, IOException, DatabaseException, TBitsException, SQLException {
        aResponse.setContentType(DEFAULT_MIME_TYPE);
        
        ServletOutputStream out = aResponse.getOutputStream();
        
        String fileName = aRequest.getParameter("file");
        String filePath = APIUtil.getTMPDir() + "/" + fileName;
        
     // Set the content-disposition field.
        String contentDisposition = "fileName=\"" + fileName + "\"";
        
        //
        // Check if this is a request to save the attachment instead of opening
        // it directly.
        //
        String isSaveRequest = aRequest.getParameter("saveAs");

        if (isSaveRequest != null) {
            contentDisposition = "attachment; " + contentDisposition;
        }
        
        aResponse.setHeader("Content-Disposition", contentDisposition);
        
        File   f        = new File(filePath);
        String mimeType = getMimeType(f);
        
        if ((mimeType != null) && (mimeType.trim().equals("") == false)) {
            aResponse.setContentType(mimeType);
        } else {
            mimeType = DEFAULT_MIME_TYPE;
        }
        
        LOG.info("Reading Temporary File");
        
        try {
            
            //
            // Open an inputstream to read the attachment 1MB at a time and
            // output the same.
            //
            FileInputStream fis  = new FileInputStream(f);
            int             size = 1024 * 1024;
            byte[]          b    = new byte[size];
            int             read = 0;

            while ((read = fis.read(b, 0, size)) > 0) {
                out.write(b, 0, read);
            }

            fis.close();
            
            f.delete();
            
         // Flush the output.
            out.flush();
            return;
        } catch (FileNotFoundException fnfe) {
            throw new TBitsException("No temporary file found with name : " + fileName);
        } catch (Exception e) {
            throw new TBitsException(e.toString());
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
        
}
