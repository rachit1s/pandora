package transbit.tbits.webapps;

import static transbit.tbits.Helper.TBitsConstants.PKG_WEBAPPS;
import static transbit.tbits.Helper.TBitsPropEnum.KEY_ATTACHMENTDIR;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import javax.activation.MimetypesFileTypeMap;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import transbit.tbits.Helper.Messages;
import transbit.tbits.api.APIUtil;
import transbit.tbits.common.Configuration;
import transbit.tbits.common.DatabaseException;
import transbit.tbits.common.PropertiesHandler;
import transbit.tbits.common.TBitsLogger;
import transbit.tbits.common.Uploader;
import transbit.tbits.exception.TBitsException;

public class DownloadFile extends HttpServlet{
	
	//====================================================================================

	private static final long serialVersionUID = 1L;
	
	// Application Logger.
    public static final TBitsLogger LOG = TBitsLogger.getLogger(PKG_WEBAPPS);
    // Default Mime Type.
    private static final String DEFAULT_MIME_TYPE = "application/octet-stream";
    // Location of attachments.
//    private static final String ourAttachmentLocation = Configuration.findAbsolutePath(PropertiesHandler.getProperty(KEY_ATTACHMENTDIR));
    
	//====================================================================================

	/**
     * This method services the Http-Get Requests to this servlet.
     *
     * @param  aRequest          the HttpServlet Request Object
     * @param  aResponse         the HttpServlet Response Object
     *
     * @throws ServletException
     * @throws IOException
     */
    
    @Override
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
        }
    }

	//====================================================================================

	private void handleGetRequest(HttpServletRequest aRequest, HttpServletResponse aResponse) 
					throws TBitsException, NumberFormatException, DatabaseException, IOException {
		
		aResponse.setContentType(DEFAULT_MIME_TYPE);
        
        ServletOutputStream out = aResponse.getOutputStream();
        
        // Get all the required parameters from the request
        String fileRepoId = aRequest.getParameter("filerepoid");
        String hash = aRequest.getParameter("hash");
        String securityCode = aRequest.getParameter("securitycode");
        if(fileRepoId == null || hash == null || securityCode == null)
        	throw new TBitsException("Incorrect URL.");
        
        // Get file location corresponding to the parameters
        String loc = Uploader.getFileLocation(Integer.parseInt(fileRepoId), hash, Integer.parseInt(securityCode));
        String filePath = APIUtil.getAttachmentLocation() + File.separatorChar + loc;
        String fileName = Uploader.getFileName(Integer.parseInt(fileRepoId));
        
        // Set the content-disposition field.
        String contentDisposition = "fileName=\"" + fileName + "\"";

        // Check if this is a request to save the attachment instead of opening it directly.
        String isSaveRequest = aRequest.getParameter("saveAs");
        if (isSaveRequest != null) {
            contentDisposition = "attachment; " + contentDisposition;
        }
        
       aResponse.setHeader("Content-Disposition", contentDisposition);

        File f = new File(filePath);
        String mimeType = getMimeType(new File(filePath.toLowerCase()));

        if ((mimeType != null) && (mimeType.trim().equals("") == false)) {
            aResponse.setContentType(mimeType);
        } else {
            mimeType = DEFAULT_MIME_TYPE;
        }

        LOG.info("Reading attachment without authentication. Security enabled.");

        try {

            // Open an inputstream to read the attachment 1MB at a time and
            // output the same.
            FileInputStream fis  = new FileInputStream(f);
            int             size = 1024 * 1024;
            byte[]          b    = new byte[size];
            int             read = 0;

            while ((read = fis.read(b, 0, size)) > 0) {
                out.write(b, 0, read);
            }

            fis.close();
            // Flush the output.
            out.flush();
            out.close();
            return;
        } 
        catch (FileNotFoundException fnfe) {
            throw new TBitsException(Messages.getMessage("NO_ATTACHMENT"));
        } 
        catch (Exception e) {
            throw new TBitsException(e.toString());
        }        
	}
	
	//====================================================================================

	// TODO Move all the utility methods to a single utility class.
	// 		Should be done in all packages.
	
	// Utility Method
	
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
	
	//====================================================================================

}
