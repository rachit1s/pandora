package commons.com.tbitsGlobal.utils.client.grids;

import java.util.ArrayList;
import java.util.List;

import com.extjs.gxt.ui.client.Style.SelectionMode;
import com.extjs.gxt.ui.client.Style.SortDir;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.GridEvent;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.store.TreeStore;
import com.extjs.gxt.ui.client.widget.grid.CheckBoxSelectionModel;
import com.extjs.gxt.ui.client.widget.grid.CheckColumnConfig;
import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
import com.extjs.gxt.ui.client.widget.grid.ColumnData;
import com.extjs.gxt.ui.client.widget.grid.ColumnModel;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.extjs.gxt.ui.client.widget.grid.GridCellRenderer;
import com.extjs.gxt.ui.client.widget.grid.RowNumberer;
import com.extjs.gxt.ui.client.widget.treegrid.TreeGrid;
import com.extjs.gxt.ui.client.widget.treegrid.TreeGridCellRenderer;
import com.extjs.gxt.ui.client.widget.treegrid.TreeGridView;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.rpc.AsyncCallback;

import commons.com.tbitsGlobal.utils.client.ClientUtils;
import commons.com.tbitsGlobal.utils.client.GlobalConstants;
import commons.com.tbitsGlobal.utils.client.IFixedFields;
import commons.com.tbitsGlobal.utils.client.TbitsInfo;
import commons.com.tbitsGlobal.utils.client.TbitsTreeRequestData;
import commons.com.tbitsGlobal.utils.client.Events.BaseTbitsObservable;
import commons.com.tbitsGlobal.utils.client.Events.ITbitsEventHandle;
import commons.com.tbitsGlobal.utils.client.Events.TagRequests;
import commons.com.tbitsGlobal.utils.client.Events.TbitsEventRegister;
import commons.com.tbitsGlobal.utils.client.Events.TbitsObservable;
import commons.com.tbitsGlobal.utils.client.Events.ToCustomizeColumns;
import commons.com.tbitsGlobal.utils.client.Events.ToViewRequestOtherBA;
import commons.com.tbitsGlobal.utils.client.GridCellRenderers.AttachmentGridCellRenderer;
import commons.com.tbitsGlobal.utils.client.GridCellRenderers.DateGridCellRenderer;
import commons.com.tbitsGlobal.utils.client.GridCellRenderers.TypeGridCellRenderer;
import commons.com.tbitsGlobal.utils.client.GridCellRenderers.UserListGridCellRenderer;
import commons.com.tbitsGlobal.utils.client.GridCellRenderers.WrapGridCellRenderer;
import commons.com.tbitsGlobal.utils.client.bafield.BAField;
import commons.com.tbitsGlobal.utils.client.bafield.BAFieldAttachment;
import commons.com.tbitsGlobal.utils.client.bafield.BAFieldCheckBox;
import commons.com.tbitsGlobal.utils.client.bafield.BAFieldCombo;
import commons.com.tbitsGlobal.utils.client.bafield.BAFieldDate;
import commons.com.tbitsGlobal.utils.client.bafield.BAFieldMultiValue;
import commons.com.tbitsGlobal.utils.client.bafield.ColPrefs;
import commons.com.tbitsGlobal.utils.client.cache.CacheRepository;
import commons.com.tbitsGlobal.utils.client.cache.FieldCache;
import commons.com.tbitsGlobal.utils.client.log.Log;
import commons.com.tbitsGlobal.utils.client.tags.TagsUtils;

/**
 * 
 * @author sourabh
 * 
 * A generic grid which can display requests. 
 *
 */
public abstract class RequestsViewGrid extends TreeGrid<TbitsTreeRequestData> implements IRequestsGrid, IFixedFields{
	
	protected String sysPrefix;
	
	/**
	 * Column to show tbits id. It serves as the {@link TreeGridCellRenderer}.
	 */
	protected ColumnConfig tbitsId;
	
	/**
	 * Selection model for the grid.
	 */
	protected CheckBoxSelectionModel<TbitsTreeRequestData> sm;
	
	/**
	 * Header of the tbits Id column
	 */
	protected String tBitsIdColumnHeader = "tBits Id";
	
	/**
	 * Column preferences
	 */
	protected List<ColPrefs> prefs;
	
	/**
	 * Various configuration flags
	 */
	protected boolean showNumberer = true;
	protected boolean showSelectionModel = true;
	protected boolean showContextMenu = true;
	protected boolean openRequestOnClick = true;
	protected boolean isCustomizable  = true;
	protected boolean showTags = true;
	
	/**
	 * Context menu for the grid
	 */
	protected AbstractGridContextMenu menu;
	
	/**
	 * To enable customization of columns
	 */
	protected CustomizeColumnSupport customizeColSupport;
	
	/**
	 * To listen to events
	 */
	protected TbitsObservable observable;
	
	/**
	 * Constructor.
	 * 
	 * Initializes the static columns and optional functionalities.
	 */
	public RequestsViewGrid(String sysPrefix){
		super(new TreeStore<TbitsTreeRequestData>(), new ColumnModel(new ArrayList<ColumnConfig>()));
		
		this.setColumnLines(true);
		
		this.sysPrefix = sysPrefix;
		
		tbitsId = new ColumnConfig(REQUEST, 100);
		tbitsId.setRenderer(new TreeGridCellRenderer<TbitsTreeRequestData>());
		tbitsId.setId(REQUEST);
		tbitsId.setHeader(tBitsIdColumnHeader);
		tbitsId.setWidth(65);
		tbitsId.setMenuDisabled(true);
		
		sm = new CheckBoxSelectionModel<TbitsTreeRequestData>();
		sm.setSelectionMode(SelectionMode.MULTI);
		
		customizeColSupport = new CustomizeColumnSupport(getViewId()){
			@Override
			protected void afterSave(List<ColPrefs> prefs) {
				super.afterSave(prefs);
				
				setPrefs(prefs);
			}
		};
		
		this.setView(new ReadStatusView());
		this.prefs = ClientUtils.getPrefsForView(getViewId(), this.sysPrefix);
		
		observable = new BaseTbitsObservable();
		observable.attach();
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
	
	@Override
	protected void onRender(Element target, int index) {
		super.onRender(target, index);
		
		/**
		 * Initialize context menu if the flag is on. 
		 */
		if(showContextMenu){
			if(menu == null)
				menu = new GridContextMenu(this);
			
			this.setContextMenu(menu);
			
			this.addListener(Events.ContextMenu, new Listener<GridEvent<TbitsTreeRequestData>>() {
	            public void handleEvent(GridEvent<TbitsTreeRequestData> be) {
	            	if(menu != null){
//	            		menu.hide();
	            		int col = be.getColIndex();
	                	ColumnConfig colConfig = cm.getColumn(col);
	                	String p = null;
	                	if(colConfig != null){
	    	            	p = colConfig.getId();
	                	}
	                	menu.calculateItemsAndrender(be.getModel(), p);
	            	}
	            }
			});
		}
		
		/**
		 * View request on click
		 */
		if(openRequestOnClick){
			this.addListener(Events.OnClick, new Listener<GridEvent<TbitsTreeRequestData>>(){
				public void handleEvent(GridEvent<TbitsTreeRequestData> be) {
					if(be.getColIndex() == 1 || be.getColIndex() == -1  || be.getRowIndex() == -1)
						return;
					TbitsEventRegister.getInstance().fireEvent(new ToViewRequestOtherBA(sysPrefix, be.getModel().getRequestId()));
				}
			});
		}
		
		// Subscribe to the apply/remove tag event
		observable.subscribe(TagRequests.class, new ITbitsEventHandle<TagRequests>(){
			public void handleEvent(TagRequests event) {
				// Return if the grid is not visible
				if(!isVisible())
					return;
				// Form the list of request_ids from the list of TbitsTreeRequestData
				ArrayList<Integer> requests = new ArrayList<Integer>();
				for(TbitsTreeRequestData currItem : getSelectionModel().getSelectedItems()){
					requests.add(currItem.getRequestId());
				}
				if(event.getAction() == TagRequests.APPLY)
					TagsUtils.applyTag(ClientUtils.getCurrentUser(), ClientUtils.getCurrentBA(), requests, event.getTag(), event.getTagType());
				else
					TagsUtils.removeTagFromRequests(ClientUtils.getCurrentUser(), ClientUtils.getCurrentBA(), requests, event.getTag(), event.getTagType());
			}});
		
		/**
		 * Add event handle to show Customize column window if flag is on.
		 */
		if(isCustomizable){
			final ITbitsEventHandle<ToCustomizeColumns> handle = new ITbitsEventHandle<ToCustomizeColumns>(){
				public void handleEvent(ToCustomizeColumns event) {
					if(event.getSource().equals(RequestsViewGrid.this))
						customizeColSupport.showWindow(prefs);
				}};
			observable.subscribe(ToCustomizeColumns.class, handle);
		}
	}
	
	@Override
	protected void afterRenderView() {
		super.afterRenderView();
		
		this.getView().refresh(true);
	}
	
	/**
	 * Add a single {@link TbitsTreeRequestData} to the store.
	 * @param model
	 */
	public void addModel(TbitsTreeRequestData model){
		this.getTreeStore().add(model, true);
	}
	
	/**
	 * Add a list of {@link TbitsTreeRequestData} to the store.
	 * @param models
	 */
	public void addModels(List<TbitsTreeRequestData> models){
		this.clearStore();
		this.getTreeStore().add(models, true);
	}
	
	public void setSortOrder(String sortOrderColumn,
			int sortDirection) {
		SortDir sortDir = SortDir.DESC;
		if(sortDirection == 1)
			sortDir = SortDir.ASC;
		
		this.getTreeStore().getSortState().setSortField(sortOrderColumn);
		this.getTreeStore().getSortState().setSortDir(sortDir);
	};
	/**
	 * Empties the store.
	 */
	public void clearStore(){
		this.getTreeStore().removeAll();
	}
	
	/**
	 * Return config for a specified {@link BAField} and column size.
	 * 
	 * @param baField
	 * @param size
	 * @return
	 */
	public ColumnConfig getColumn(BAField baField, int size){
		ColumnConfig column = new ColumnConfig(); 
		column.setId(baField.getName());  
		column.setHeader(baField.getDisplayName());  
		column.setWidth(size);
		if(baField instanceof BAFieldCheckBox){
			column = new CheckColumnConfig();
			column.setId(baField.getName());  
			column.setHeader(baField.getDisplayName());
			column.setWidth(size);
		}else if(baField instanceof BAFieldDate){
			column.setRenderer(new DateGridCellRenderer((BAFieldDate) baField));
		}else if(baField instanceof BAFieldAttachment){
			column.setRenderer(new AttachmentGridCellRenderer((BAFieldAttachment)baField));
		}else if(baField instanceof BAFieldMultiValue){
			column.setRenderer(new UserListGridCellRenderer((BAFieldMultiValue)baField));
		}else if(baField instanceof BAFieldCombo){
			column.setRenderer(new TypeGridCellRenderer((BAFieldCombo)baField));
		}else{
			column.setRenderer(new WrapGridCellRenderer<TbitsTreeRequestData>());
		}
		return column;
	}
	
	/**
	 * This method must be called after the field cache has been initialized.
	 * @return Returns the columns.(forms from Field cache)
	 */
	protected List<ColumnConfig> getColumnsFromCache(){
		List<ColumnConfig> columns = new ArrayList<ColumnConfig>();
		
		FieldCache cache = CacheRepository.getInstance().getCache(FieldCache.class);
		if(cache.isInitialized()){
			/**
			 * make prefs from cache if prefs are null
			 */
			if(prefs == null){
				prefs = new ArrayList<ColPrefs>();
				if(cache != null && cache.isInitialized()){
					for(BAField baField : cache.getValues()){
						prefs.add(ClientUtils.fieldToColPref(baField));
					}
				}
			}
			
			for(ColPrefs pref : prefs){
				BAField baField = cache.getObject(pref.getName());
				if(baField == null || baField.getName().equals(REQUEST) || !baField.isCanViewInBA())
					continue;
				ColumnConfig config = this.getColumn(baField, pref.getColSize());
				if(config != null)
					columns.add(config);
			}
		}
		
		return columns;
	}
	
	/**
	 * Loads columns by making an RPC call
	 */
	protected void loadColumnsAsync(){
		GlobalConstants.utilService.getFields(sysPrefix, new AsyncCallback<List<BAField>>(){
			public void onFailure(Throwable caught) {
				TbitsInfo.error("Unable to load fields.. Please see logs for details...", caught);
				Log.error("Unable to load fields.. Please see logs for details...", caught);
			}

			public void onSuccess(List<BAField> result) {
				if(result != null){
					if(prefs == null){
						prefs = new ArrayList<ColPrefs>();
						for(BAField baField : result){
							prefs.add(ClientUtils.fieldToColPref(baField));
						}
					}
					
					ListStore<BAField> fieldsStore = new ListStore<BAField>();
					fieldsStore.add(result);
					
					List<ColumnConfig> columns = new ArrayList<ColumnConfig>();
					for(ColPrefs pref : prefs){
						BAField baField = fieldsStore.findModel(BAField.NAME, pref.getName());
						if(baField == null || baField.getName().equals(REQUEST) || !baField.isCanViewInBA())
							continue;
						ColumnConfig config = RequestsViewGrid.this.getColumn(baField, pref.getColSize());
						if(config != null)
							columns.add(config);
					}
					
					RequestsViewGrid.this.cm.getColumns().addAll(columns);
					RequestsViewGrid.this.getView().refresh(true);
				}
			}});
	}
	
	/**
	 * empties the column model.
	 */
	public void clearColumns(){
		this.getColumnModel().getColumns().clear();
	}
	
	@Override
	protected void beforeRender() {
		super.beforeRender();
		
		this.displayColumns();
	}
	
	/**
	 * Adds the row number column
	 */
	protected void addRowNumberer(){
		if(showNumberer){
			RowNumberer rowNum = new RowNumberer();
			rowNum.setRenderer(new GridCellRenderer<TbitsTreeRequestData>() {
				public Object render(TbitsTreeRequestData model, String property,
						ColumnData config, int rowIndex, int colIndex,
						ListStore<TbitsTreeRequestData> store,
						Grid<TbitsTreeRequestData> grid) {
							config.cellAttr = "rowspan='2'";
					        String s = "<b>Subject : </b> " + model.getAsString(SUBJECT) + "<br>" +
							"<b>Description : </b><br />" + model.getAsString(DESCRIPTION);
					        return "<span qtip ='" + s.replace("\'", "").replaceAll("\\n", "")  + "'>" +(rowIndex + 1) + "</span>";
						}
			    });
			cm.getColumns().add(rowNum);
			if(!rendered)
				this.addPlugin(rowNum);
		}
	}
	
	/**
	 * Adds the selection model to the grid.
	 */
	protected void addSelectionModel(){
		if(showSelectionModel){
			this.setSelectionModel(sm);
			cm.getColumns().add(sm.getColumn());
			if(!rendered)
				this.addPlugin(sm);
		}
	}
	
	public void setTBitsIdColumnHeader(String tBitsIdColumnHeader) {
		this.tBitsIdColumnHeader = tBitsIdColumnHeader;
	}

	public String getTBitsIdColumnHeader() {
		return tBitsIdColumnHeader;
	}
	
	/**
	 * Sets the preferences, clears the columns and rebuilds the column model.
	 * @param prefs
	 */
	public void setPrefs(List<ColPrefs> prefs) {
		this.prefs = prefs;
		this.displayColumns();
	}
	
	/**
	 * Build the column model
	 */
	protected void displayColumns() {
		this.clearColumns();
		
		this.addRowNumberer();
		this.addSelectionModel();
		
		cm.getColumns().add(tbitsId);
		
		if(GlobalConstants.isTagsSupported)
			this.addTags();
		
		if(sysPrefix.equals(ClientUtils.getSysPrefix())){
			List<ColumnConfig> columns = this.getColumnsFromCache();
			this.cm.getColumns().addAll(columns);
			this.getView().refresh(true);
		}else
			loadColumnsAsync();
		
		
	}
	
	/**
	 * Adds the tags column to the grid.
	 */
	protected void addTags(){
		if(showTags){
			ColumnConfig column = new ColumnConfig();  
			column.setId("request_tags");  
			column.setHeader("Tags");  
			column.setWidth(45);
			column.setMenuDisabled(true);
			cm.getColumns().add(column);  
		}
	}
	
	/**
	 * @return. The column preferences for the grid.
	 */
	public List<ColPrefs> getPrefs() {
		return prefs;
	}

	public void setShowContextMenu(boolean showContextMenu) {
		this.showContextMenu = showContextMenu;
	}

	public boolean isShowContextMenu() {
		return showContextMenu;
	}

	public void setOpenRequestOnClick(boolean openRequestOnClick) {
		this.openRequestOnClick = openRequestOnClick;
	}

	public boolean isOpenRequestOnClick() {
		return openRequestOnClick;
	}

	public void setCustomizable(boolean isCustomizable) {
		this.isCustomizable = isCustomizable;
	}

	public boolean isCustomizable() {
		return isCustomizable;
	}

	public Grid<TbitsTreeRequestData> getGrid() {
		return this;
	}

	public void setSysPrefix(String sysPrefix) {
		this.sysPrefix = sysPrefix;
	}

	public String getSysPrefix() {
		return sysPrefix;
	}
}
