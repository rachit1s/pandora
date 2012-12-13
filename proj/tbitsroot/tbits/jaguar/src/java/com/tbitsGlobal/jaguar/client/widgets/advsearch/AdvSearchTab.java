package com.tbitsGlobal.jaguar.client.widgets.advsearch;

import com.extjs.gxt.ui.client.Style.LayoutRegion;
import com.extjs.gxt.ui.client.util.Margins;
import com.extjs.gxt.ui.client.widget.TabItem;
import com.extjs.gxt.ui.client.widget.layout.BorderLayout;
import com.extjs.gxt.ui.client.widget.layout.BorderLayoutData;
import com.tbitsGlobal.jaguar.client.state.AppState;
import commons.com.tbitsGlobal.utils.client.ClientUtils;
import commons.com.tbitsGlobal.utils.client.Events.BaseTbitsObservable;
import commons.com.tbitsGlobal.utils.client.Events.ITbitsEventHandle;
import commons.com.tbitsGlobal.utils.client.Events.OnChangeBA;
import commons.com.tbitsGlobal.utils.client.Events.TbitsObservable;

/**
 * Tab for advanced search.
 * 
 * @author sourabh
 *
 */
public class AdvSearchTab extends TabItem {
	
	/**
	 * To listen to events
	 */
	protected TbitsObservable observable;
	
	public AdvSearchTab() {
		super("Advanced Search");
		
		this.setLayout(new BorderLayout());
		this.setBorders(false);
			
		// Add Search Grid
//		List<ISearchResultsPanelPlugin> plugins = 
//			GWTPluginRegister.getInstance().getPlugins(SearchResultsViewPluginSlot.class, ISearchResultsPanelPlugin.class);
//		if(plugins != null && plugins.size() > 0){
//			ISearchResultsPanelPlugin plugin = plugins.get(0);
//			SearchResultsPanel widget = plugin.getWidget(null);
//			widget.setBasicSearch(false);
//			this.add(widget, new BorderLayoutData(LayoutRegion.CENTER));
//		}else{
//			SearchGridPanel panel = new SearchGridPanel();
//			panel.setBasicSearch(false);
//			this.add(panel, new BorderLayoutData(LayoutRegion.CENTER));
//		}
		
		observable = new BaseTbitsObservable();
		observable.attach();
		
		final String sysPrefix = ClientUtils.getSysPrefix();
		
		if(AppState.checkAppStateIsTill(AppState.BAChanged)){
			this.makeLeftPane(sysPrefix);
			this.makeRightPane(sysPrefix);
		}
		
		ITbitsEventHandle<OnChangeBA> baChangeHandle = new ITbitsEventHandle<OnChangeBA>(){
			public void handleEvent(OnChangeBA event) {
				removeAll();
				makeLeftPane(event.getSysPrefix());
				makeRightPane(event.getSysPrefix());
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
	
	private void makeLeftPane(String sysPrefix){
		BorderLayoutData westData = new BorderLayoutData(LayoutRegion.WEST, 260);
		westData.setSplit(true);
		westData.setMargins(new Margins(0,5,0,0));
		this.add(new AdvSearchPanel(sysPrefix), westData);
	}
	
	private void makeRightPane(String sysPrefix){
		AdvSearchGrid grid = new AdvSearchGrid(sysPrefix);
		AdvSearchGridContainer container =  new AdvSearchGridContainer(sysPrefix, grid);
		this.add(container, new BorderLayoutData(LayoutRegion.CENTER));
		
//		grid.getView().addListener(Events.Refresh, new Listener<BaseEvent>(){
//			@Override
//			public void handleEvent(BaseEvent be) {
//				AdvSearchTab.this.layout();
//			}});
	}
}
