package commons.com.tbitsGlobal.utils.server;

import gwtupload.server.UploadAction;
import gwtupload.server.exceptions.UploadActionException;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;

import transbit.tbits.api.APIUtil;
import transbit.tbits.api.AttachmentInfo;
import transbit.tbits.common.DatabaseException;
import transbit.tbits.common.Uploader;

//TODO::Code duplication between Uploader and this class.
public class GWTUploadServlet extends UploadAction {
	private static final long serialVersionUID = 1L;
	
	public GWTUploadServlet() {
		super();
//		attachmentBase = new File("/home/sourabh/projects/tmp");
//        attachmentBase = new File(APIUtil.getAttachmentLocation());
	}
	
	@Override
	public String executeAction(HttpServletRequest request,
			List<FileItem> sessionFiles) throws UploadActionException {
		ArrayList<AttachmentInfo> atts = new ArrayList<AttachmentInfo>();
		String folderHint = request.getParameter("folderhint");
		int 	requestId = 0 ;
		int actionId = 0 ;
		
		try
		{
			requestId = Integer.parseInt(request.getParameter("requestid"));
		}
		catch(Exception e){}

		try
		{
			actionId = Integer.parseInt(request.getParameter("actionid"));
		}
		catch(Exception e){}

		File attachmentBase = new File(APIUtil.getAttachmentLocation());
		File parentDir = new File(APIUtil.getAttachmentLocation());
		if(!parentDir.exists())
			parentDir.mkdirs();
		
		parentDir = Uploader.prepareAttachmentFolder(folderHint, parentDir);
		
		
		// Create a factory for disk-based file items
		DiskFileItemFactory factory = new DiskFileItemFactory();

		// Set factory constraints
		//factory.setSizeThreshold(yourMaxMemorySize);
		File repotmp = new File(APIUtil.getTMPDir());
		//File repotmp = new File(tBitsTmpLoc, "repotmp");
		factory.setRepository(repotmp);

		// Set overall request size constraint
		//upload.setSizeMax(yourMaxRequestSize);

		// Parse the request
//		List items = upload.parseRequest(request);
		
		// Process the uploaded items
		if(sessionFiles.size() > 0){
			FileItem item = sessionFiles.get(sessionFiles.size() - 1);
			
		    // Process a file upload
			if (item != null && !item.isFormField()) {
			    String fileName = item.getName();
			    int sp = fileName.lastIndexOf("\\") + 1;
				if(sp < fileName.length())
					fileName = fileName.substring(sp);
				
			    String proposedfileName = requestId + "-" + actionId + "-" 
				+ fileName.replaceAll("[^A-Za-z0-9-\\._\\+]+", "_");
			    
			    //TODO::heck. will consider the name of the file after \
			    
	            File fTarget = Uploader.generateUniqTargetFile(parentDir, proposedfileName);
			    
			    try {
					item.write(fTarget);
					long size = item.getSize();
					//response.getWriter().println(size);
					//Now put it in DB
					String relative = attachmentBase.toURI().relativize(fTarget.toURI()).getPath();
					int id = Uploader.writeFilePropsIntoDB(relative, fileName, fTarget);
					
//					String str = "[{repoFileId:1, name:\"50_Guarantees.pdf\", requestFileId:\"0\", size:385178}]";
					AttachmentInfo att = new AttachmentInfo();
					att.repoFileId = id;
					att.name = fileName;
					att.size = Integer.parseInt(size + "");
					atts.add(att);
					
				} catch (DatabaseException e) {
					e.printStackTrace();
					throw new UploadActionException("Unable to generate fileRepoId", e);
				} catch (Exception e) {
					e.printStackTrace();
					throw new UploadActionException(e);
				}
				System.out.println("Actual File: " + fTarget.getAbsolutePath());
			}
		}
		
		return AttachmentInfo.toJson(atts);
	}
	
	@Override
	public void removeItem(HttpServletRequest request, FileItem item)
			throws UploadActionException {
		List<FileItem> sessionFiles = getSessionFileItems(request);
		if(sessionFiles != null)
			sessionFiles.remove(item);
	}
	
	@Override
	public void removeItem(HttpServletRequest request, String fieldName)
			throws UploadActionException {
		List<FileItem> sessionFiles = getSessionFileItems(request);
		if(sessionFiles != null){
			FileItem item = super.findFileItem(sessionFiles, fieldName);
			sessionFiles.remove(item);
		}
	}
	
	@Override
	public void init(ServletConfig config) throws ServletException {
		super.init(config);
//		this.maxSize = 1024 * 1024 * 1024;
	}
}
