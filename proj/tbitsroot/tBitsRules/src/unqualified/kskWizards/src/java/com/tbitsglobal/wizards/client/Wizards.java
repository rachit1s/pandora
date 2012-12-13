package com.tbitsglobal.wizards.client;

import invitationLetterWizard.com.tbitsGlobal.client.InvitationLetterWizard;
import invitationLetterWizard.com.tbitsGlobal.client.ILConstants;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.rpc.ServiceDefTarget;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.RootPanel;
import commons.com.tbitsGlobal.utils.client.TbitsUncaughtExceptionHandler;

/**
 * Entry point classes define <code>onModuleLoad()</code>.
 */
public class Wizards implements EntryPoint {

	/**
	 * This is the entry point method.
	 */
	public void onModuleLoad() {
		GWT.setUncaughtExceptionHandler(new TbitsUncaughtExceptionHandler());
		
		((ServiceDefTarget)ILConstants.dbService).setServiceEntryPoint("/gwtproxy/invidb");
		
		Button invitationBtn = new Button("Create Invitation Letter", new ClickHandler(){
			public void onClick(ClickEvent event) {
				new InvitationLetterWizard(preProcess());
			}});
		try{
			RootPanel.get("wizardButtonContainer").add(invitationBtn);
		}catch (Exception e){
			e.printStackTrace();
		}
	}
	
	/**
	 * This method returns the selected values from the search table on search page of tbits
	 * 
	 * @return the {@link JavaScriptObject} containing an Array of request_id's
	 */
	protected native JavaScriptObject getSelectedIds()/*-{
		return $wnd.getSelected();
	}-*/;
	
	protected String preProcess() {
		JavaScriptObject jsObj = getSelectedIds();
		if(jsObj != null){
			String idString = jsObj.toString();
			return idString;
		}
		return "";
	}
}
