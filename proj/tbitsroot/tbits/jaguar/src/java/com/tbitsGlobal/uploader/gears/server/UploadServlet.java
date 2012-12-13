package com.tbitsGlobal.uploader.gears.server;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author sourabh
 * 
 * A sample Servlet.
 * 
 * 
 * Accepts a POST request containing a file and writes the data to a temporary
 * file. A size limit of 1GB is enforced.
 */
public class UploadServlet extends HttpServlet {
	private static final long serialVersionUID = -5911011533094624685L;

	private static final int GB = 1048576 * 1024;
	private static final int MAX_FILE_SIZE = 1 * GB;

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
      	throws ServletException, IOException {

	    if (req.getContentLength() > MAX_FILE_SIZE) {
	      resp.sendError(HttpServletResponse.SC_REQUEST_ENTITY_TOO_LARGE,
	          "File is too large");
	      return;
	    }
	    
	    String fileName = req.getHeader("X-Filename");
	    if (fileName != null) {
	      System.out.println("File name: " + fileName);
	    }
	    
	    File parentDir = new File("./uploads");
		if(!parentDir.exists())
			parentDir.mkdirs();
		File tmp = generateUniqTargetFile(parentDir, fileName);
		
	    InputStream in = req.getInputStream();
	    if (in != null) {
	      FileOutputStream out = new FileOutputStream(tmp);
	      byte[] buffer = new byte[65536];
	
	      int l;
	      while ((l = in.read(buffer)) != -1) {
	        out.write(buffer, 0, l);
	      }
	      out.close();
	      in.close();
	
	      System.out.println("Saved " + req.getContentLength() + " bytes to "
	          + tmp.getAbsolutePath());
	    }
	
	    resp.setStatus(HttpServletResponse.SC_OK);
	    resp.setContentType("text/plain");
	    resp.getWriter().println("OK");
	}
  	
  	protected File generateUniqTargetFile(File parentDir, String proposedfileName) {
		int dotIndex = proposedfileName.lastIndexOf(".");
		String proposedPrefix = null;
		String proposedSuffix = ".tmp";
		if(dotIndex > -1)
		{
			proposedPrefix = proposedfileName.substring(0, dotIndex);
			proposedSuffix = proposedfileName.substring(dotIndex + 1);
		}
		else
		{
			proposedPrefix = proposedfileName;
		}
		
		File fTarget = new File(parentDir, proposedPrefix + "." + proposedSuffix);					
		int counter = 1;
		while(fTarget.exists())
		{
			fTarget = new File(parentDir, proposedPrefix + counter++ + "." + proposedSuffix);
		}
		return fTarget;
	}
}
