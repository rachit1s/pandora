package com.tbitsGlobal.admin.server;

import gwtupload.server.UploadAction;
import gwtupload.server.exceptions.UploadActionException;

import java.io.File;
import java.io.IOException;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import org.apache.commons.fileupload.FileItem;
import com.tbitsGlobal.admin.client.widgets.pages.ReportFormWindow;
import transbit.tbits.common.Configuration;

/**
 * Servlet to save the uploaded report file to database.
 * @author devashish
 */
public class ReportFileUploadServlet extends UploadAction {

	private static final String UPLOAD_DIRECTORY = Configuration.findPath("tbitsreports").getPath();
	
	
	public String executeAction(HttpServletRequest request,	List<FileItem> sessionFiles) throws UploadActionException {
		 
		for(FileItem item : sessionFiles){
			
			if(true == item.isFormField()){
				continue;
			}
			File uploadedFile = new File(UPLOAD_DIRECTORY, item.getName());
			System.out.println(item.getName());
			try {
				if(uploadedFile.createNewFile()){
					item.write(uploadedFile);
				}
			} catch (IOException e) {
				System.out.println("File already exists...");
				e.printStackTrace();
				return ReportFormWindow.FILE_UPLOAD_STATUS_FAILURE;
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		 
		return ReportFormWindow.FILE_UPLOAD_STATUS_SUCCESS;
	}
}
