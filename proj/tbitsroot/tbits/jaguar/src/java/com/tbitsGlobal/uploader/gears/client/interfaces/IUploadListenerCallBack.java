package com.tbitsGlobal.uploader.gears.client.interfaces;

/**
 * 
 * @author sourabh
 * 
 * A callback from {@link IUploadListener}
 */
public interface IUploadListenerCallBack {
	/**
	 * To cancel the upload.
	 * @param uploadHandler
	 */
	public void onCancel(IUploadHandler uploadHandler);
	
	/**
	 * To repeat the upload of a file
	 * @param uploadHandler
	 */
	public void onRepeat(IUploadHandler uploadHandler);
}
