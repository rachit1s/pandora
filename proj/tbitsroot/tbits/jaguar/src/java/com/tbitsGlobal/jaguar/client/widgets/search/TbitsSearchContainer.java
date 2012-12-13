package com.tbitsGlobal.jaguar.client.widgets.search;

import java.util.ArrayList;

import com.extjs.gxt.ui.client.Style.HorizontalAlignment;
import com.extjs.gxt.ui.client.Style.LayoutRegion;
import com.extjs.gxt.ui.client.Style.SelectionMode;
import com.extjs.gxt.ui.client.data.ModelData;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.GridEvent;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.MessageBoxEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.util.Margins;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.MessageBox;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.CheckBox;
import com.extjs.gxt.ui.client.widget.grid.CheckBoxSelectionModel;
import com.extjs.gxt.ui.client.widget.grid.CheckColumnConfig;
import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
import com.extjs.gxt.ui.client.widget.grid.ColumnData;
import com.extjs.gxt.ui.client.widget.grid.ColumnModel;
import com.extjs.gxt.ui.client.widget.grid.EditorGrid;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.extjs.gxt.ui.client.widget.grid.GridCellRenderer;
import com.extjs.gxt.ui.client.widget.layout.AccordionLayout;
import com.extjs.gxt.ui.client.widget.layout.BorderLayoutData;
import com.extjs.gxt.ui.client.widget.layout.FitData;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.toolbar.ToolBar;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.tbitsGlobal.jaguar.client.JaguarConstants;
import com.tbitsGlobal.jaguar.client.searchgrid.SearchGrid;
import com.tbitsGlobal.jaguar.client.searchgrid.SearchGridContainer;

import commons.com.tbitsGlobal.utils.client.ClientUtils;
import commons.com.tbitsGlobal.utils.client.GlobalConstants;
import commons.com.tbitsGlobal.utils.client.TbitsCellEditor;
import commons.com.tbitsGlobal.utils.client.TbitsInfo;
import commons.com.tbitsGlobal.utils.client.TbitsModelData;
import commons.com.tbitsGlobal.utils.client.Events.ITbitsEventHandle;
import commons.com.tbitsGlobal.utils.client.Events.OnChangeBA;
import commons.com.tbitsGlobal.utils.client.domainObjects.ShortcutClient;
import commons.com.tbitsGlobal.utils.client.log.Log;
import commons.com.tbitsGlobal.utils.client.search.BasicSearchContainer;
import commons.com.tbitsGlobal.utils.client.search.grids.BasicSearchGrid;
import commons.com.tbitsGlobal.utils.client.search.grids.BasicSearchGridContainer;
import commons.com.tbitsGlobal.utils.client.search.searchpanel.DQL;
import commons.com.tbitsGlobal.utils.client.search.searchpanel.TbitsSearchPanel;
import commons.com.tbitsGlobal.utils.client.search.searchpanel.AbstractSearchPanel.ISearchHandle;
import commons.com.tbitsGlobal.utils.client.tags.TagsUtils;
import commons.com.tbitsGlobal.utils.client.urlManager.HistoryToken;
import commons.com.tbitsGlobal.utils.client.urlManager.TbitsURLManager;
import commons.com.tbitsGlobal.utils.client.widgets.TbitsHyperLink;
import commons.com.tbitsGlobal.utils.client.widgets.TbitsHyperLinkEvent;

public class TbitsSearchContainer extends BasicSearchContainer{

	public TbitsSearchContainer(String sysPrefix) {
		super(sysPrefix);
	}
	
	protected void makeRightPane(){
		if(gridContainer == null){
			
			SearchGrid grid = new SearchGrid(sysPrefix);
			gridContainer =  new SearchGridContainer(sysPrefix, grid);
			this.add(gridContainer, new BorderLayoutData(LayoutRegion.CENTER));
		}
	}
	
	protected void makeLeftPane(){
		BorderLayoutData westData = new BorderLayoutData(LayoutRegion.WEST, 260);
		westData.setSplit(true);
		westData.setMargins(new Margins(0,5,0,0));
		
		if(filterPanel == null)
			filterPanel = new TbitsSearchPanel(sysPrefix);
		
		filterPanel.setSearchHandle(new ISearchHandle(){
			public void onSearch(DQL dql) {
				TbitsURLManager.getInstance().addToken(new HistoryToken(GlobalConstants.TOKEN_DQL, dql.dql, true));
				Log.info("Start searching.");
			}});
	
		
		LayoutContainer leftPane = new LayoutContainer(new AccordionLayout());
		leftPane.add(filterPanel);
		
		ContentPanel savedQueriesBox = this.getSavedQueriesBox();
		leftPane.add(savedQueriesBox);
		
		this.add(leftPane, westData);
	}
	
	/**
	 * Get the panel for saved queries.
	 * 
	 * @return. The panel
	 */
	private ContentPanel getSavedQueriesBox(){
		final ContentPanel savedQueriesBox = new ContentPanel(new FitLayout());
		savedQueriesBox.setHeading("Saved Searches");		
		
		
		ListStore<ShortcutClient> store = new ListStore<ShortcutClient>();
		
		ArrayList<ColumnConfig> myConfigs = new ArrayList<ColumnConfig>();
		
		ColumnConfig name = new ColumnConfig(ShortcutClient.NAME, 130);
		
		name.setHeader("Name");
		name.setRenderer(new GridCellRenderer<ShortcutClient>(){
			public Object render(final ShortcutClient sc, String property,
					ColumnData config, int rowIndex, int colIndex,
					ListStore<ShortcutClient> store, Grid<ShortcutClient> grid) {
				     TbitsHyperLink link = new TbitsHyperLink(sc.getName(), new SelectionListener<TbitsHyperLinkEvent>(){
					@Override
					public void componentSelected(TbitsHyperLinkEvent ce) {
						
						String dql = sc.getQuery();
						TbitsURLManager.getInstance().addToken(new HistoryToken(GlobalConstants.TOKEN_DQL, dql, true));
					}});
				return link;
			}});
		myConfigs.add(name);
		
//		CheckColumnConfig isShared = new CheckColumnConfig(){
//			@Override
//			protected void onMouseDown(final GridEvent<ModelData> ge) {
//				super.onMouseDown(ge);
//				ShortcutClient sc = (ShortcutClient)ge.getModel();
//				boolean share = sc.getIsPublic();
//				JaguarConstants.dbService.shareSavedSearch(ClientUtils.getSysPrefix(), sc.getName(), share, new AsyncCallback<Boolean>(){
//					public void onFailure(Throwable arg0) {
//						TbitsInfo.error("Error while sharing search...", arg0);
//					}
//
//					public void onSuccess(Boolean result) {
//						
//					}});
//			}
//		};
//		isShared.setId(ShortcutClient.IS_PUBLIC);
//		isShared.setWidth(45);
//		isShared.setHeader("Shared");
//		isShared.setEditor(new TbitsCellEditor(new CheckBox()));
//		myConfigs.add(isShared);
		
		//code added for default search by mukesh rawat
		CheckColumnConfig isDefault = new CheckColumnConfig(){
			@Override
			protected void onMouseDown(final GridEvent<ModelData> ge) {
				super.onMouseDown(ge);
//				ShortcutClient sc = (ShortcutClient)ge.getModel();
//				boolean _default = sc.getIsDefault();
//				JaguarConstants.dbService.defaultSavedSearch(ClientUtils.getSysPrefix(), sc.getName(), _default, new AsyncCallback<Boolean>(){
//					public void onFailure(Throwable arg0) {
//						TbitsInfo.error("Error while setting default search...", arg0);
//					}
//
//					public void onSuccess(Boolean result) {
//						
//					}});
			}
		};
		
		isDefault.setId(ShortcutClient.IS_DEFAULT);
		isDefault.setWidth(45);
		isDefault.setHeader("Default");
		isDefault.setEditor(new TbitsCellEditor(new CheckBox()));
		myConfigs.add(isDefault);
		
		
		ColumnConfig save = new ColumnConfig();
		save.setWidth(45);
		save.setHeader("Save");
		save.setRenderer(new GridCellRenderer<ShortcutClient>(){
			public Object render(final ShortcutClient model, String property,
					ColumnData config, int rowIndex, int colIndex,
					final ListStore<ShortcutClient> store, final Grid<ShortcutClient> grid) {
				
				
				Button btn = new Button("S", new SelectionListener<ButtonEvent>(){
					@Override
					public void componentSelected(ButtonEvent ce) {
						ArrayList<ShortcutClient> allShortcuts=(ArrayList<ShortcutClient>) store.getModels();
						 int count=0;
						for(ShortcutClient sc : allShortcuts)
						{	
							if(sc.getIsDefault()==true)
							{
								count++;
							}
						}
						if(count>1)
							{
								MessageBox.info("Select","you can not select more than one search as default", new Listener<MessageBoxEvent>()
		            					{
	
											@Override
											public void handleEvent(MessageBoxEvent be) {
												// TODO Auto-generated method stub
												
											}
		            					});		
		            			return;
		         
						}
						
						else
						{	for(ShortcutClient sc : allShortcuts)
							{	
									boolean _default = sc.getIsDefault();
									JaguarConstants.dbService.defaultSavedSearch(ClientUtils.getSysPrefix(),sc.getName(), _default, new AsyncCallback<Boolean>(){
										public void onFailure(Throwable arg0) {
											TbitsInfo.error("Error while deleting search...", arg0);
										}

										public void onSuccess(Boolean result) {
											//store.remove(model);
											grid.getView().refresh(true);
										}});

								
							}
							

						}
						
					}});
				
				return btn;
			}});
		myConfigs.add(save);
		
		
		ColumnConfig delete = new ColumnConfig();
		delete.setWidth(45);
		delete.setHeader("Delete");
		delete.setRenderer(new GridCellRenderer<ShortcutClient>(){
			public Object render(final ShortcutClient model, String property,
					ColumnData config, int rowIndex, int colIndex,
					final ListStore<ShortcutClient> store, final Grid<ShortcutClient> grid) {
				Button btn = new Button("X", new SelectionListener<ButtonEvent>(){
					@Override
					public void componentSelected(ButtonEvent ce) {
						
						JaguarConstants.dbService.deleteSavedSearch(ClientUtils.getSysPrefix(), model.getName(), new AsyncCallback<Boolean>(){
							public void onFailure(Throwable arg0) {
								TbitsInfo.error("Error while deleting search...", arg0);
							}

							public void onSuccess(Boolean result) {
								store.remove(model);
								grid.getView().refresh(false);
							}});
					}});
				return btn;
			}});
		myConfigs.add(delete);
		
		ColumnModel colModel = new ColumnModel(myConfigs);
		
		final EditorGrid<ShortcutClient> grid = new EditorGrid<ShortcutClient>(store, colModel);
		//grid.addPlugin(isShared);
		grid.addPlugin(isDefault);
		savedQueriesBox.add(grid, new FitData());
			
		JaguarConstants.dbService.getSavedSearches(ClientUtils.getSysPrefix(), new AsyncCallback<ArrayList<ShortcutClient>>(){
			public void onFailure(Throwable arg0) {
				TbitsInfo.error(arg0.getMessage(), arg0);
			}

			public void onSuccess(ArrayList<ShortcutClient> result) {
				if(result == null)
					return;
				grid.getStore().add(result);
				grid.getView().refresh(false);
				for(ShortcutClient sc : result)
				{ int count =0;
					if((sc.getIsDefault()==true)&&(count<1)){
							
							String dql = sc.getQuery();
							TbitsURLManager.getInstance().addToken(new HistoryToken(GlobalConstants.TOKEN_DQL, dql, true));
							count++;
					}
					
				}
				
			}});
		
		observable.subscribe(OnChangeBA.class, new ITbitsEventHandle<OnChangeBA>(){
			public void handleEvent(OnChangeBA event) {
				JaguarConstants.dbService.getSavedSearches(ClientUtils.getSysPrefix(), new AsyncCallback<ArrayList<ShortcutClient>>(){
					public void onFailure(Throwable arg0) {
						TbitsInfo.error("Error while loading saved searches...", arg0);
					}

					public void onSuccess(ArrayList<ShortcutClient> result) {
						grid.getStore().removeAll();
						if(result == null)
							return;
						grid.getStore().add(result);
						grid.getView().refresh(false);
						
					}});
			}});
		
		return savedQueriesBox;
	}
 

}
