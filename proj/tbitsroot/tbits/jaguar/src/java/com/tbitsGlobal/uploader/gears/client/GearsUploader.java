package com.tbitsGlobal.uploader.gears.client;

import com.google.gwt.gears.client.Factory;
import com.google.gwt.gears.client.desktop.File;
import com.google.gwt.gears.client.desktop.OpenFilesHandler;
import com.tbitsGlobal.uploader.gears.client.implementations.BaseUploadListenerWidget;
import com.tbitsGlobal.uploader.gears.client.implementations.BaseUploader;
import com.tbitsGlobal.uploader.gears.client.interfaces.IUploadListenerWidget;
import commons.com.tbitsGlobal.utils.client.TbitsExceptionClient;

public class GearsUploader {
	/**
	 *  Constants to identify whether Gears is installed or not
	 */
	public static final int ENABLED		=	1;
	public static final int DISABLED	=	0;
	
	/**
	 * Refer {@link IUploadListenerWidget}
	 */
	protected IUploadListenerWidget listenerWidget;
	
	/**
	 * Uploader to perform uploads
	 */
	protected AbstractUploader uploader;
	
	/**
	 * Constructor. 
	 * @throws TbitsExceptionClient 
	 */
	public GearsUploader() throws Exception{
		if(getStatus() == DISABLED){ // not installed
			throw new Exception("Gears not installed");
		}
		
		init();
	}
	
	/**
	 * Initialize Uploader
	 */
	protected void init(){
		listenerWidget = new BaseUploadListenerWidget();
		uploader = new BaseUploader(listenerWidget);
	}
	
	/**
	 * @return. 1 if gears is installed, 0 otherwise
	 */
	public static int getStatus(){
		Factory factory = Factory.getInstance();
		if(factory != null)
			return ENABLED;
		return DISABLED;
	}
	
	/**
	 * Opens the file explorer dialog
	 */
	public void openFiles(){
		Factory factory = Factory.getInstance();
		if(factory != null){
			 factory.createDesktop().openFiles(new OpenFilesHandler(){
				public void onOpenFiles(OpenFilesEvent event) {
					onFileSelect(event.getFiles());
				}});
		}
	}
	
	/**
	 * Called when the user selects files
	 * @param files
	 */
	protected void onFileSelect(File[] files){
		for(File file : files){
			uploader.queue(file);
		}
	}
	
	public AbstractUploader getUploader() {
		return uploader;
	}

	public IUploadListenerWidget getListenerWidget() {
		return listenerWidget;
	}
}
