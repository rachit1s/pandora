package com.tbitsGlobal.jaguar.client.widgets.search;

import com.extjs.gxt.ui.client.widget.TabItem;
import com.extjs.gxt.ui.client.widget.layout.FitData;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.tbitsGlobal.jaguar.client.state.AppState;
import commons.com.tbitsGlobal.utils.client.ClientUtils;
import commons.com.tbitsGlobal.utils.client.Events.BaseTbitsObservable;
import commons.com.tbitsGlobal.utils.client.Events.ITbitsEventHandle;
import commons.com.tbitsGlobal.utils.client.Events.OnChangeBA;
import commons.com.tbitsGlobal.utils.client.Events.TbitsObservable;
import commons.com.tbitsGlobal.utils.client.log.Log;

/**
 * Tab to provide Basic search.
 * 
 * @author sourabh
 *
 */
public class SearchTab extends TabItem {
	/**
	 * To listen to events
	 */
	protected TbitsObservable observable;
	
	/**
	 * Constructor
	 */
	public SearchTab() {
		super("Search");
		
		Log.info("Initializing Search Tab Panel");
		
		this.setLayout(new FitLayout());
		this.setBorders(false);
		
		observable = new BaseTbitsObservable();
		observable.attach();
		
		if(AppState.checkAppStateIsTill(AppState.BAChanged)){
			this.add(new TbitsSearchContainer(ClientUtils.getSysPrefix()), new FitData());
		}
		
		ITbitsEventHandle<OnChangeBA> baChangeHandle = new ITbitsEventHandle<OnChangeBA>(){
			public void handleEvent(OnChangeBA event) {
				removeAll();
				SearchTab.this.add(new TbitsSearchContainer(ClientUtils.getSysPrefix()), new FitData());
				layout();
			}};
		observable.subscribe(OnChangeBA.class, baChangeHandle);
	}
	
	@Override
	protected void onAttach() {
		super.onAttach();
		
		observable.attach();
	}
	
	@Override
	protected void onDetach() {
		super.onDetach();
		
		observable.detach();
	}
}
