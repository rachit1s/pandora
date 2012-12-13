package com.tbitsGlobal.uploader.gears.client.implementations;

import com.google.gwt.gears.client.desktop.File;
import com.google.gwt.gears.client.httprequest.HttpRequest;
import com.tbitsGlobal.uploader.gears.client.interfaces.IUploadHandler;
import com.tbitsGlobal.uploader.gears.client.interfaces.IUploadListenerCallBack;

public class BaseUploadHandler implements IUploadHandler{
	private int uniqueId;
	private File file;
	private HttpRequest request;
	
	private IUploadListenerCallBack callback;
	
	public BaseUploadHandler(int uniqueId, File file, HttpRequest request) {
		super();
		this.uniqueId = uniqueId;
		this.file = file;
		this.request = request;
	}

	public File getFile() {
		return file;
	}

	public HttpRequest getRequest() {
		return request;
	}

	public void setUniqueId(int uniqueId) {
		this.uniqueId = uniqueId;
	}

	public int getUniqueId() {
		return uniqueId;
	}

	public void setCallBack(IUploadListenerCallBack callback) {
		this.callback = callback;
	}
	
	public IUploadListenerCallBack getCallBack(){
		return callback;
	}

}
