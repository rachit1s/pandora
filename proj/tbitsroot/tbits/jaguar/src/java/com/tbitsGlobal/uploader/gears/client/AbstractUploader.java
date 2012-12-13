package com.tbitsGlobal.uploader.gears.client;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.gears.client.Factory;
import com.google.gwt.gears.client.blob.Blob;
import com.google.gwt.gears.client.desktop.File;
import com.google.gwt.gears.client.httprequest.HttpRequest;
import com.google.gwt.gears.client.httprequest.ProgressEvent;
import com.google.gwt.gears.client.httprequest.ProgressHandler;
import com.google.gwt.gears.client.httprequest.RequestCallback;
import com.tbitsGlobal.uploader.gears.client.interfaces.IUploadHandler;
import com.tbitsGlobal.uploader.gears.client.interfaces.IUploadListener;

/**
 * 
 * @author sourabh
 *
 * The Abstract Uploader that works with Gears.
 */
public abstract class AbstractUploader {
	/**
	 * The Url of the servlet
	 */
	protected String servletUrl = "/gearsupload";
	
	/**
	 * Refer {@link IUploadListener}
	 */
	protected IUploadListener uploadListener;
	
	/**
	 * It is basically the list of all running or queued uploads
	 */
	protected List<IUploadHandler> queue = new ArrayList<IUploadHandler>();
	
	/**
	 * Constructor
	 * 
	 * @param uploadListener.
	 */
	public AbstractUploader(IUploadListener uploadListener) {
		this.uploadListener = uploadListener;
	}
	
	/**
	 * Adds the file to the queue and starts uploading it.
	 * @param file
	 */
	public void queue(File file){
		
		int uniqueId = uploadListener.onQueue(file);
		
		IUploadHandler uploadHandler = upload(uniqueId, file);
		queue.add(uploadHandler);
		
		// Call onUpload
		uploadListener.onUpload(uploadHandler);
	}
	
	/**
	 * Performs the upload.
	 * @param uniqueId
	 * @param file
	 * @return
	 */
	protected IUploadHandler upload(final int uniqueId, File file){
		Blob selectedFile = file.getBlob();
		HttpRequest request = Factory.getInstance().createHttpRequest();
		
		final IUploadHandler uploadHandler = getUploadHandler(uniqueId, file, request);
		
		String query = getQuery();
		if(query == null)
			query = "";
        request.open("POST", servletUrl + query);
        
        // To mark the source.
        request.setRequestHeader("X-Source", "gwt");
        request.setRequestHeader("X-Filename", file.getName());
        request.setRequestHeader("Content-type", "multipart/form-data; boundary=fU3W4Vzr4G3D54f3");
        
        request.setCallback(new RequestCallback() {
          public void onResponseReceived(HttpRequest request) {
        	  queue.remove(uploadHandler);
        	  if (request.getStatus() != 200) { // Some Error
            	  uploadListener.onError(uniqueId);
              }
              else { // Successfull !!
            	  uploadListener.onComplete(uniqueId);
              }
          }
        });

        request.getUpload().setProgressHandler(new ProgressHandler() {
          public void onProgress(ProgressEvent event) {
        	  uploadListener.onProgress(uniqueId, event.getLoaded(), event.getTotal());
          }
        });
        
        // Send the request
        request.send(selectedFile);
        
        return uploadHandler;
	}
	
	public void setServletUrl(String servletUrl) {
		this.servletUrl = servletUrl;
	}

	public String getServletUrl() {
		return servletUrl;
	}
	
	/**
	 * Returns the number of Uploads that are in progress.
	 * @return
	 */
	public int getInProgressUploads() {
		int uploadCount = 0;
		for(IUploadHandler handler : queue){
			if(handler.getRequest().getReadyState() == 2 || handler.getRequest().getReadyState() == 3)
				uploadCount ++;
		}
		return uploadCount;
	}

	/**
	 * 
	 * @return Returns the number of Uploads that are queued.
	 */
	public int getQueuedUploads() {
		int uploadCount = 0;
		for(IUploadHandler handler : queue){
			if(handler.getRequest().getReadyState() == 0 || handler.getRequest().getReadyState() == 1)
				uploadCount ++;
		}
		return uploadCount;
	}
	
	/**
	 * @return -- Query to be appended to the servletUrl
	 */
	protected abstract String getQuery();
	
	/**
	 * Refer {@link IUploadHandler}
	 * @param uniqueId
	 * @param file
	 * @param request
	 * @return
	 */
	protected abstract IUploadHandler getUploadHandler(int uniqueId, File file, HttpRequest request);
}
