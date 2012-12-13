package com.tbitsGlobal.uploader.gears.client.interfaces;

import com.google.gwt.gears.client.desktop.File;
import com.google.gwt.gears.client.httprequest.HttpRequest;

/**
 * 
 * @author sourabh
 *
 * Implemented to carry the objects related to an upload.
 */
public interface IUploadHandler {
	/**
	 * @return Returns the {@link HttpRequest} for the upload. 
	 */
	public HttpRequest getRequest();
	
	/**
	 * @return Returns the {@link File} for the upload.
	 */
	public File getFile();
	
	/**
	 * @return Returns a Unique Id for an upload.
	 */
	public int getUniqueId();
	
	/**
	 * Refer {@link IUploadListenerCallBack}
	 * @param callback
	 */
	public void setCallBack(IUploadListenerCallBack callback);
	
	/**
	 * @return Refer {@link IUploadListenerCallBack}
	 */
	public IUploadListenerCallBack getCallBack();
}
