package com.tbitsGlobal.uploader.gears.client;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.RootPanel;

public class GearsUploaderApp implements EntryPoint{

	public void onModuleLoad() {
		try{
			final GearsUploader uploader = new GearsUploader();
			
			RootPanel.get().add(uploader.getListenerWidget().getWidget());
			
			Button browseBtn = new Button("Browse Files...", new ClickHandler(){
				public void onClick(ClickEvent event) {
					uploader.openFiles();
				}});
			RootPanel.get().add(browseBtn);
		}catch(Exception e){
			Window.alert("Gears is not installed");
		}
		
	}

}
