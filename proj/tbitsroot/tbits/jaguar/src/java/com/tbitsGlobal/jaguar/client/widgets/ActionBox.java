package com.tbitsGlobal.jaguar.client.widgets;

import java.util.HashMap;

import com.extjs.gxt.ui.client.Style.Orientation;
import com.extjs.gxt.ui.client.core.DomQuery;
import com.extjs.gxt.ui.client.core.El;
import com.extjs.gxt.ui.client.event.ComponentEvent;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.Html;
import com.extjs.gxt.ui.client.widget.layout.RowData;
import com.extjs.gxt.ui.client.widget.layout.RowLayout;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.Event;
import com.tbitsGlobal.jaguar.client.JaguarUtils;

import commons.com.tbitsGlobal.utils.client.ClickableLink;
import commons.com.tbitsGlobal.utils.client.ClientUtils;
import commons.com.tbitsGlobal.utils.client.bafield.BAField;
import commons.com.tbitsGlobal.utils.client.domainObjects.ActionClient;
import commons.com.tbitsGlobal.utils.client.domainObjects.UserClient;

/**
 * 
 * @author sutta
 * 
 * A panel to display one history item in View Request.
 */
public class ActionBox extends ContentPanel{

	protected HashMap<String, ClickableLink> linkMap;
	
	public ActionBox(ActionClient actionClient, String sysPrefix, ListStore<BAField> fieldStore) {
		super();
		
		linkMap = new HashMap<String, ClickableLink>();
		sinkEvents(Event.ONCLICK);
		
		RowLayout layout = new RowLayout();
		layout.setOrientation(Orientation.VERTICAL);
		this.setLayout(layout);
		this.setHeaderVisible(false);
		this.setBodyBorder(false);
		this.setStyleAttribute("padding", "5px");
		this.setStyleAttribute("fontSize", "11px");
		this.setStyleAttribute("borderBottom", "1px solid #99BBE8");
		this.setStyleAttribute("borderLeft", "5px solid #99BBE8");
		
		// Action number
		String text = "<div style='display:block; overflow:hidden;'>" +
				"<span style='float:left'>" + actionClient.getActionId() + ". ";
		
		UserClient userClient = actionClient.getActionUser();
		if(userClient != null)
			text += userClient.getDisplayName() + " (" + userClient.getUserLogin() + ")";
		
		// User login and time of update
		if(actionClient.getLastUpdatedDate() != null)
			text += "</span>" +
					"<span style='float:right'>" + 
					DateTimeFormat.getFormat(ClientUtils.getCurrentUser().getWebDateFormat()).format(actionClient.getLastUpdatedDate()) + " " +
					"</span>";
		
		text += "</div>";
		
		this.add(new Html(text), new RowData());
		
		// Get the Description
		String description = JaguarUtils.hyperSmartLinks(actionClient.getDescription(), linkMap);
		this.add(new Html(description), new RowData());
		
		// Attachments tracking options
		if(actionClient.getAttachmentHTML() != null)
			this.addText(actionClient.getAttachmentHTML());
		
		// General Tracking options
		String headerDesc = ActionFormatHelper.formatActionLog(actionClient.getHeaderDescription(), sysPrefix, fieldStore, linkMap);
		this.add(new Html(headerDesc), new RowData());
	}
	
	@Override
	protected void onClick(ComponentEvent e) {
		super.onClick(e);
		
		El el = El.fly(e.getTarget());
        Element elem = el.dom;
        
		for(String className : linkMap.keySet()){
			if(DomQuery.is(elem, className)){
				ClickableLink link = linkMap.get(className);
				if(link != null)
					link.executeListeners(e);
			}
		}
	}
}
