package com.tbitsGlobal.admin.client.widgets;

import java.util.HashMap;

import com.extjs.gxt.ui.client.widget.TabItem;
import com.extjs.gxt.ui.client.widget.TabPanel;
import com.tbitsGlobal.admin.client.events.OnPageRequest;
import com.tbitsGlobal.admin.client.utils.APLinks;
import com.tbitsGlobal.admin.client.utils.LinkIdentifier;
import com.tbitsGlobal.admin.client.widgets.pages.SystemStatusView;
import commons.com.tbitsGlobal.utils.client.Events.BaseTbitsObservable;
import commons.com.tbitsGlobal.utils.client.Events.ITbitsEventHandle;
import commons.com.tbitsGlobal.utils.client.Events.TbitsObservable;

public class TabContainer extends TabPanel {

	/**
	 * To listen to events
	 */
	protected TbitsObservable observable;
	
	private HashMap<LinkIdentifier, APTabItem> tabMap;
	
	public TabContainer() {
		super();
		this.setTabScroll(true);
		this.setAnimScroll(true);
		this.setCloseContextMenu(true);
		this.setPlain(true);
		
		observable = new BaseTbitsObservable();
		observable.attach();
		
		tabMap = new HashMap<LinkIdentifier, APTabItem>();
		
		applyHandlers();
		
		this.add(new SystemStatusView(APLinks.SYSTEM_STATUS));
	}
	
	@Override
	protected void onDetach() {
		super.onDetach();
		
		observable.detach();
	}
	
	@Override
	protected void onAttach() {
		super.onAttach();
		
		observable.attach();
	}
	
	public void add(APTabItem tab) {
		if(tabMap.containsKey(tab.getLinkIdentifier())){
			this.setSelection(tab);
		}else{
			super.add(tab);
			tabMap.put(tab.getLinkIdentifier(), tab);
			this.setSelection(tab);
		}
	}
	
	protected void close(TabItem item){
		if(item instanceof APTabItem && tabMap.containsValue(item))
			tabMap.remove(((APTabItem) item).getLinkIdentifier());
		super.close(item);
	}

	private void applyHandlers() {
		// handler for page requests through leftGrid buttons
		observable.subscribe(OnPageRequest.class,	new ITbitsEventHandle<OnPageRequest>() {
			public void handleEvent(OnPageRequest event) {
				APTabItem tab = tabMap.get(event.getPageLink().getLinkIdentifier());
				if(tab != null){
					setSelection(tab);
				}else{
					tab = event.getPageLink().getPage();
					
					if(tab != null){
						TabContainer.this.add(tab);
					}
				}
			}
		});
	}
}
