package com.tbitsGlobal.jaguar.client.widgets;

import java.util.ArrayList;
import java.util.Date;

import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.tbitsGlobal.jaguar.client.JaguarConstants;
import commons.com.tbitsGlobal.utils.client.TbitsInfo;
import commons.com.tbitsGlobal.utils.client.bafield.BAField;
import commons.com.tbitsGlobal.utils.client.domainObjects.ActionClient;
import commons.com.tbitsGlobal.utils.client.log.Log;

/**
 * @author sourabh
 *
 * Widget that holds all the actions when users views a request
 */
public class ActionHistoryPanel extends ContentPanel{
	
	private ListStore<BAField> fieldStore;
	
	public ActionHistoryPanel(ListStore<BAField> fieldStore) {
		super();
		
		this.fieldStore = fieldStore;
		
		this.setHeading("History");
		this.setCollapsible(true);
		this.setAnimCollapse(true);
		this.setStyleAttribute("marginLeft", "5px");
		this.setStyleAttribute("marginRight", "5px");
	}
	
	public ActionHistoryPanel(String sysPrefix, int requestId, ListStore<BAField> fieldStore){
		this(fieldStore);
		this.fillActions(sysPrefix, requestId);
	}
	
	/**
	 * Get actions for a specified requestId and displays them.
	 * 
	 * @param requestId
	 */
	public void fillActions(final String sysPrefix, int requestId){
		this.removeAll();
		
		JaguarConstants.dbService.getActions(sysPrefix, requestId, new AsyncCallback<ArrayList<ActionClient>>(){

			public void onFailure(Throwable caught) {
				TbitsInfo.error("Error while loading action history...", caught);
				Log.error("Error while loading action history...", caught);
			}

			public void onSuccess(ArrayList<ActionClient> result) {
				if(result.size() == 0){ // No actions... not possible in normal circumstances
					addText("No Actions in the history");
					ActionHistoryPanel.this.layout();
					return;
				}
				for(ActionClient actionClient : result){
					ActionBox action = new ActionBox(actionClient, sysPrefix, fieldStore);
					add(action);
				}
				ActionHistoryPanel.this.layout();
			}
		});
	}
	
	/**
	 * get client's time zone
	 * @param type
	 * @return
	 */
	@SuppressWarnings("deprecation")
	private String getTimeZone(int type) {
	        Date today = new Date();
	        String timezone = null;
	        switch (type) {
	                case 1: // date timeZoneMinOffset in minutes
	                        timezone = Integer.toString(today.getTimezoneOffset()); //like "420"
	                        break;
	                case 2: // time zone RFC822
	                        timezone = DateTimeFormat.getFormat("Z").format(today); //like "-0700"
	                        break;
	                case 3: //time zone TextShort
	                        timezone = DateTimeFormat.getFormat("v").format(today); //like "GMT-07:00"
	                        break;
	                case 4: //time zone Name 

	                        //THIS WONT COMPILE - WORKS IN DEBUGGER - this will not work on client side
	                        //TimeZone tz = TimeZone.getDefault();
	                        //timezone = tz.getID(); //like "America/Vancouver"
	                        timezone = DateTimeFormat.getFormat("z").format(today); //like "GMT-07:00";

	                        break;
	                case 5: //timezone TextLong
	                        timezone = DateTimeFormat.getLongDateTimeFormat().format(today); //like "March 22, 2008 5:39:22 PM GMT-07:00"
	                        break;
	        }
	        return timezone;
	}
}
