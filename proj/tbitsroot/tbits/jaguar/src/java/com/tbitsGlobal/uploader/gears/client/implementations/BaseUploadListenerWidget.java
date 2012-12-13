package com.tbitsGlobal.uploader.gears.client.implementations;

import java.util.HashMap;

import com.google.gwt.gears.client.desktop.File;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Widget;
import com.tbitsGlobal.uploader.gears.client.interfaces.IUploadHandler;
import com.tbitsGlobal.uploader.gears.client.interfaces.IUploadListenerWidget;

public class BaseUploadListenerWidget extends FlowPanel implements IUploadListenerWidget{
	private HashMap<Integer, IUploadHandler> uploadHandlerMap;
	private HashMap<Integer, HTML> htmlMap;
	public BaseUploadListenerWidget() {
		super();
		
		uploadHandlerMap = new HashMap<Integer, IUploadHandler>();
		htmlMap = new HashMap<Integer, HTML>();
	}

	public Widget getWidget() {
		return this;
	}

	public void onComplete(int uniqueId) {
		IUploadHandler uploadHandler = uploadHandlerMap.get(uniqueId);
		htmlMap.get(uniqueId).setHTML(uploadHandler.getFile().getName() + " Uploaded");
	}

	public void onError(int uniqueId) {
		IUploadHandler uploadHandler = uploadHandlerMap.get(uniqueId);
		String msg = uploadHandler.getRequest().getStatus() + " " + uploadHandler.getRequest().getStatusText();
		htmlMap.get(uniqueId).setHTML(msg);
	}

	public void onProgress(int uniqueId, int loaded, int total) {
		IUploadHandler uploadHandler = uploadHandlerMap.get(uniqueId);
		double pcnt = ((double) loaded / total);
		htmlMap.get(uniqueId).setHTML(uploadHandler.getFile().getName() + " - " + ((int) Math.floor(pcnt * 100) + "%"));
	}

	public int onQueue(File file) {
		HTML html = new HTML(); 
		htmlMap.put(file.hashCode(), html);
		this.add(html);
		html.setHTML(file.getName() + "Queued");
		
		return file.hashCode();
	}

	public void onUpload(IUploadHandler uploadHandler) {
		uploadHandlerMap.put(uploadHandler.getUniqueId(), uploadHandler);
		htmlMap.get(uploadHandler.getUniqueId()).setHTML(uploadHandler.getFile().getName() + " Uploading");
	}
}
