package com.tbitsGlobal.admin.client.widgets;

import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.TabPanelEvent;
import com.extjs.gxt.ui.client.widget.TabItem;
import com.tbitsGlobal.admin.client.utils.LinkIdentifier;

import commons.com.tbitsGlobal.utils.client.ClientUtils;
import commons.com.tbitsGlobal.utils.client.TbitsInfo;
import commons.com.tbitsGlobal.utils.client.Events.BaseTbitsObservable;
import commons.com.tbitsGlobal.utils.client.Events.TbitsObservable;
import commons.com.tbitsGlobal.utils.client.urlManager.HistoryToken;
import commons.com.tbitsGlobal.utils.client.urlManager.TbitsURLManager;

public abstract class APTabItem extends TabItem {

	private LinkIdentifier linkIdentifier;
	
	/**
	 * To listen to events
	 */
	protected TbitsObservable observable;

	public APTabItem(LinkIdentifier linkId) {
		super();
		
		if(!ClientUtils.getCurrentUser().getIsSuperUser()){
			TbitsInfo.error("Current User is NOT A SUPERUSER... Hence cannot proceed...");
			return;
		}
		
		this.linkIdentifier = linkId;
		
		this.setBorders(false);
		this.setText(linkIdentifier.getPageCaption());
		
		observable = new BaseTbitsObservable();
		observable.attach();
		
		this.addListener(Events.BeforeClose, new Listener<TabPanelEvent>() {
			@Override
			public void handleEvent(TabPanelEvent be) {
				TbitsURLManager.getInstance().removeToken(new HistoryToken(linkIdentifier.getHistoryKey(), "", false));
			}
		});
	}
	
	@Override
	protected void onAttach() {
		super.onAttach();
		
		observable.attach();
	}
	
	@Override
	/**
	 * this method is always called when a tab item is removed
	 */
	public void onDetach(){
		observable.detach();
		
		super.onDetach();
	}

	public LinkIdentifier getLinkIdentifier() {
		return linkIdentifier;
	}
}
