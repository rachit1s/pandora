package com.tbitsGlobal.uploader.gears.client.interfaces;

import com.google.gwt.gears.client.desktop.File;

/**
 * 
 * @author sourabh
 * 
 * Implemented to get notified on various events in upload.
 */
public interface IUploadListener {
	/**
	 * Called when file is queued
	 * @param file
	 * @return
	 */
	public int onQueue(File file);
	
	/**
	 * Called when the request is sent for upload.
	 * @param uploadHandler
	 */
	public void onUpload(IUploadHandler uploadHandler);
	
	/**
	 * Called when progress data is recieved from the server
	 * @param uniqueId
	 * @param loaded
	 * @param total
	 */
	public void onProgress(int uniqueId, int loaded, int total);
	
	/**
	 * Called when the Upload finishes succesfully.
	 * @param uniqueId
	 */
	public void onComplete(int uniqueId);
	
	/**
	 * Called when error occurs in upload.
	 * @param uniqueId
	 */
	public void onError(int uniqueId);
}
