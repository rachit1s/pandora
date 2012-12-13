package com.tbitsGlobal.uploader.gears.client.implementations;

import com.google.gwt.gears.client.desktop.File;
import com.google.gwt.gears.client.httprequest.HttpRequest;
import com.tbitsGlobal.uploader.gears.client.AbstractUploader;
import com.tbitsGlobal.uploader.gears.client.interfaces.IUploadHandler;
import com.tbitsGlobal.uploader.gears.client.interfaces.IUploadListener;

public class BaseUploader extends AbstractUploader{
	
	public BaseUploader(IUploadListener uploadListener) {
		super(uploadListener);
	}
	
	protected String getQuery(){return "";}
	
	protected IUploadHandler getUploadHandler(int uniqueId, File file, HttpRequest request){
		return new BaseUploadHandler(uniqueId, file, request);
	}
}
