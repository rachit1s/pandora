package com.tbitsGlobal.jaguar.client.widgets;

import java.util.ArrayList;
import java.util.HashMap;

import com.extjs.gxt.ui.client.Style.LayoutRegion;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.util.Margins;
import com.extjs.gxt.ui.client.widget.TabItem;
import com.extjs.gxt.ui.client.widget.layout.BorderLayout;
import com.extjs.gxt.ui.client.widget.layout.BorderLayoutData;
import commons.com.tbitsGlobal.utils.client.ClientUtils;
import commons.com.tbitsGlobal.utils.client.GlobalConstants;
import commons.com.tbitsGlobal.utils.client.Events.BaseTbitsObservable;
import commons.com.tbitsGlobal.utils.client.Events.ITbitsEventHandle;
import commons.com.tbitsGlobal.utils.client.Events.OnHistoryTokensChanged;
import commons.com.tbitsGlobal.utils.client.Events.TbitsObservable;
import commons.com.tbitsGlobal.utils.client.log.Log;
import commons.com.tbitsGlobal.utils.client.tags.TagsUtils;
import commons.com.tbitsGlobal.utils.client.tags.TagsViewGrid;
import commons.com.tbitsGlobal.utils.client.tags.TagsViewGridContainer;
import commons.com.tbitsGlobal.utils.client.tags.TagsViewPanel;
import commons.com.tbitsGlobal.utils.client.urlManager.HistoryToken;
import commons.com.tbitsGlobal.utils.client.widgets.GridPagingBar;

/**
 * The TagsTab defines the tab that contains all the tag related functions.
 * The tagged requests can be viewed and tags can be modified or deleted in this tab.
 * <br><br>
 * The TagsTab contains a TagsViewPanel to display all the tags and their related operations.
 * It also contains a RequestsViewGrid to display the tagged requests.
 * <br><br>
 * The tags tab is loaded when the main tab is loaded.
 * 
 * @author karan
 *
 */

public class TagsTab extends TabItem{
	
	//============================================================================================

	protected TagsViewPanel tagsViewPanel;
	protected TbitsObservable observable;
	protected TagsViewGrid grid;
	protected GridPagingBar pagingBar;

	//============================================================================================
	/**
	 * <b>Constructor</b>
	 * <br>
	 * The TagsTab defines the tab that contains all the tag related functions.
	 * The tagged requests can be viewed and tags can be modified or deleted in this tab.
	 * <br><br>
	 * The TagsTab contains a TagsViewPanel to display all the tags and their related operations.
	 * It also contains a RequestsViewGrid to display the tagged requests.
	 */
	public TagsTab(){
		super("My Tags");
		
		Log.info("Initializing Tags Tab Panel");
		
		this.setLayout(new BorderLayout());
		this.setBorders(false);
		
		observable = new BaseTbitsObservable();
		observable.attach();
		
		observable.subscribe(OnHistoryTokensChanged.class, new ITbitsEventHandle<OnHistoryTokensChanged>() {

			public void handleEvent(OnHistoryTokensChanged event) {
				
				ListStore<HistoryToken> existingTokens = event.getStore();
				HashMap<String, ArrayList<String>> selectedTags = new HashMap<String, ArrayList<String>>();
				selectedTags.put("public", new ArrayList<String>());
				selectedTags.put("private", new ArrayList<String>());
				for(HistoryToken ht : existingTokens.findModels(HistoryToken.KEY, GlobalConstants.TOKEN_TAGS_PUBLIC)){
					selectedTags.get("public").add(ht.getValue());
				}
				for(HistoryToken ht : existingTokens.findModels(HistoryToken.KEY, GlobalConstants.TOKEN_TAGS_PRIVATE)){
					selectedTags.get("private").add(ht.getValue());
				}
				if(selectedTags.get("public").size() > 0 || selectedTags.get("private").size() > 0)
					TagsUtils.fetchTaggedRequests(selectedTags, ClientUtils.getCurrentUser(), ClientUtils.getCurrentBA());
			}
			
		});
		
		makeLeftPane();
		makeRightPane();
	}
	
	@Override
	protected void onAttach() {
		super.onAttach();
		
		observable.attach();
	}
	
	@Override
	protected void onDetach() {
		super.onDetach();
		
//		observable.detach();
	}
	
	//============================================================================================

	/**
	 * Makes the left pane of the tags tab. The left pane contains an instance of the TagsViewPanel.
	 */
	private void makeLeftPane(){
		tagsViewPanel = new TagsViewPanel();
		BorderLayoutData westData = new BorderLayoutData(LayoutRegion.WEST, 260);
		westData.setSplit(true);
		westData.setMargins(new Margins(0,5,0,0));
		this.add(tagsViewPanel, westData);
	}
	
	//============================================================================================

	/**
	 * Makes the right pane of the tags tab. The right pane contains a RequestViewGrid in a RequestViewGridContainer.
	 */
	private void makeRightPane(){
		String sysPrefix = ClientUtils.getSysPrefix();
		
		grid = new TagsViewGrid(ClientUtils.getSysPrefix());
		TagsViewGridContainer container =  new TagsViewGridContainer(sysPrefix, grid);
		this.add(container, new BorderLayoutData(LayoutRegion.CENTER));
	}

	//============================================================================================

}
