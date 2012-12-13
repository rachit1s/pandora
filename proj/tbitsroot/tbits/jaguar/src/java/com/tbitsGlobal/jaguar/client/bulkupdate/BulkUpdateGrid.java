package com.tbitsGlobal.jaguar.client.bulkupdate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.GridEvent;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.store.StoreEvent;
import com.extjs.gxt.ui.client.store.StoreListener;
import com.extjs.gxt.ui.client.widget.form.DateField;
import com.extjs.gxt.ui.client.widget.form.TextArea;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.extjs.gxt.ui.client.widget.grid.CellEditor;
import com.extjs.gxt.ui.client.widget.grid.CheckColumnConfig;
import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
import com.extjs.gxt.ui.client.widget.grid.ColumnModel;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import commons.com.tbitsGlobal.utils.client.ClientUtils;
import commons.com.tbitsGlobal.utils.client.IFixedFields;
import commons.com.tbitsGlobal.utils.client.Mode;
import commons.com.tbitsGlobal.utils.client.TbitsCellEditor;
import commons.com.tbitsGlobal.utils.client.TbitsTreeRequestData;
import commons.com.tbitsGlobal.utils.client.Events.ITbitsEventHandle;
import commons.com.tbitsGlobal.utils.client.Events.ToCustomizeColumns;
import commons.com.tbitsGlobal.utils.client.GridCellRenderers.AttachmentGridCellRenderer;
import commons.com.tbitsGlobal.utils.client.GridCellRenderers.DateGridCellRenderer;
import commons.com.tbitsGlobal.utils.client.GridCellRenderers.TypeGridCellRenderer;
import commons.com.tbitsGlobal.utils.client.GridCellRenderers.UserListGridCellRenderer;
import commons.com.tbitsGlobal.utils.client.GridCellRenderers.WrapGridCellRenderer;
import commons.com.tbitsGlobal.utils.client.UIContext.UIContext;
import commons.com.tbitsGlobal.utils.client.bafield.BAField;
import commons.com.tbitsGlobal.utils.client.bafield.BAFieldAttachment;
import commons.com.tbitsGlobal.utils.client.bafield.BAFieldCheckBox;
import commons.com.tbitsGlobal.utils.client.bafield.BAFieldCombo;
import commons.com.tbitsGlobal.utils.client.bafield.BAFieldDate;
import commons.com.tbitsGlobal.utils.client.bafield.BAFieldMultiValue;
import commons.com.tbitsGlobal.utils.client.bafield.BAFieldTextArea;
import commons.com.tbitsGlobal.utils.client.bafield.ColPrefs;
import commons.com.tbitsGlobal.utils.client.bulkupdate.BulkUpdateGridAbstract;
import commons.com.tbitsGlobal.utils.client.bulkupdate.ExcelImportTypeModel.ExcelImportDataType;
import commons.com.tbitsGlobal.utils.client.bulkupdate.IBulkUpdateConstants;
import commons.com.tbitsGlobal.utils.client.cache.CacheRepository;
import commons.com.tbitsGlobal.utils.client.cache.FieldCache;
import commons.com.tbitsGlobal.utils.client.cellEditors.TypeFieldEditor;
import commons.com.tbitsGlobal.utils.client.cellEditors.UserTypeFieldEditor;
import commons.com.tbitsGlobal.utils.client.domainObjects.FileClient;
import commons.com.tbitsGlobal.utils.client.grids.CustomizeColumnSupport;
import commons.com.tbitsGlobal.utils.client.grids.GridColumnView;
import commons.com.tbitsGlobal.utils.client.grids.IRequestsGrid;
import commons.com.tbitsGlobal.utils.client.widgets.UserPicker;

/**
 * 
 * @author sourabh
 * 
 * The grid to be used for bulk operations.
 * 
 * It runs in two modes : 
 * <li> Individual : each row is edited individually</li>
 * <li> Common : The value is copied to all the selected rows in the Individual grid. </li>
 *
 */
public class BulkUpdateGrid extends BulkUpdateGridAbstract<TbitsTreeRequestData> 
		implements IFixedFields, IRequestsGrid {
	
	public static final String CONTEXT_BULK_UPDATE_PANEL = "bulk_update_panel";
	
	/**
	 * Map of {@link AttachmentGridCellRenderer} per field
	 */
	private HashMap<String, AttachmentGridCellRenderer> attachmentRenderersMap;
	
	/**
	 * All fields for which columns are to be displayed
	 */
	private List<ColPrefs> prefs;
	
	protected UIContext myContext;
	
	/**
	 * True to enable column preferences
	 */
	private boolean isCustomizable  = true;
	
	/**
	 * To enable customization of columns
	 */
	protected CustomizeColumnSupport customizeColSupport;
	
	/**
	 * Constructor
	 * 
	 * @param parentContext
	 * @param mode. Either View or Edit
	 * @param fields
	 */
	public BulkUpdateGrid(String sysPrefix, UIContext parentContext, BulkGridMode mode){
		super(mode);
		
		this.myContext = parentContext;
		
		this.prefs = ClientUtils.getPrefsForView(getViewId(), sysPrefix);
		this.attachmentRenderersMap = new HashMap<String, AttachmentGridCellRenderer>();
		
		customizeColSupport = new CustomizeColumnSupport(getViewId()){
			@Override
			protected void afterSave(List<ColPrefs> prefs) {
				super.afterSave(prefs);
				
				setPrefs(prefs);
				
				if(myContext.hasKey(CONTEXT_BULK_UPDATE_PANEL)){
					BulkUpdatePanel panel = myContext.getValue(CONTEXT_BULK_UPDATE_PANEL, BulkUpdatePanel.class);
					if(panel != null)
						((BulkUpdateGrid)panel.getCommonGridContainer().getBulkGrid()).setPrefs(prefs);
				}
			}
			
			@Override
			public boolean shouldShowField(BAField baField) {
				return BulkUpdateGrid.this.shouldShowField(baField);
			}
		};
		
	    final BulkUpdateGridContextMenu menu = new BulkUpdateGridContextMenu(this);
		this.setContextMenu(menu);
		
		this.addListener(Events.ContextMenu, new Listener<GridEvent<TbitsTreeRequestData>>() {
            public void handleEvent(GridEvent<TbitsTreeRequestData> be) {
            	menu.hide();
            	int col = be.getColIndex();
            	ColumnConfig colConfig = cm.getColumn(col);
            	String p = null;
            	if(colConfig != null){
	            	p = colConfig.getId();
            	}
            	menu.calculateItemsAndrender(be.getModel(), p);
            }
		});
	    
	    if(gridMode == BulkGridMode.SINGLE){
		    StoreListener<TbitsTreeRequestData> storeListener = new StoreListener<TbitsTreeRequestData>(){
		    	@Override
		    	public void storeRemove(StoreEvent<TbitsTreeRequestData> se) {
		    		super.storeRemove(se);
		    		TbitsTreeRequestData model = BulkUpdateConstants.models.findModel(REQUEST, se.getModel().getRequestId());
		    		BulkUpdateConstants.models.remove(model);
		    	}
		    	
		    	@Override
		    	public void storeClear(StoreEvent<TbitsTreeRequestData> se) {
		    		super.storeClear(se);
		    		BulkUpdateConstants.models.removeAll();
		    	}
		    };
		    this.getStore().addStoreListener(storeListener);
		    
//		    this.addListener(Events.AfterEdit, new Listener<GridEvent<TbitsTreeRequestData>>(){
//				public void handleEvent(GridEvent<TbitsTreeRequestData> be) {
//					int rowIndex = be.getRowIndex();
//					if(be.getGrid().getStore().getCount() > rowIndex + 1)
//						BulkUpdateGrid.this.startEditing(rowIndex + 1, be.getColIndex());
//				}});
	    }
	    
	    /**
		 * Add event handle to show Customize column window if flag is on.
		 */
		if(isCustomizable){
			final ITbitsEventHandle<ToCustomizeColumns> handle = new ITbitsEventHandle<ToCustomizeColumns>(){
				public void handleEvent(ToCustomizeColumns event) {
					if(event.getSource().equals(BulkUpdateGrid.this))
						customizeColSupport.showWindow(BulkUpdateGrid.this.prefs);
				}};
			observable.subscribe(ToCustomizeColumns.class, handle);
		}
	}
	
	/**
	 * Sets the preferences, clears the columns and rebuilds the column model.
	 * @param prefs
	 */
	public void setPrefs(List<ColPrefs> prefs) {
		this.prefs = prefs;
		
		this.clearColumns();
		this.addDefaltColumns();
		this.createColumns();
	}
	
	@Override
	protected void addDefaltColumns() {
		super.addDefaltColumns();
		
		this.addParentRowColumn();
	}
	
	protected void addParentRowColumn(){
		if(gridMode == BulkGridMode.SINGLE){
			ColumnConfig parentRow = new ColumnConfig();
			parentRow.setId(IBulkUpdateConstants.PARENT_ROW);
			parentRow.setWidth(70);
		    parentRow.setFixed(true);
		    parentRow.setMenuDisabled(true);
		   
	    	parentRow.setHeader("Parent Row");
		    parentRow.setEditor(new CellEditor(new TextField<String>()){
		    	@Override
		    	public Object preProcessValue(Object value) {
		    		if(value != null)
		    			return (Integer)value + "";
		    		return "";
		    	}
		    	
		    	@Override
		    	public Object postProcessValue(Object value) {
		    		if(value != null){
		    			try{
		    				return Integer.parseInt((String) value);
		    			}catch(Exception e){}
		    		}
		    		return 0;
		    	}
		    });
		    cm.getColumns().add(parentRow);
		}
	}
	
	/**
	 * empties the column model.
	 */
	public void clearColumns(){
		this.getColumnModel().getColumns().clear();
	}
	
	/**
	 * @param baField
	 * @return True if the field should be shown in the grid
	 */
	public boolean shouldShowField(BAField baField) {
		return baField.isCanAddInBA() || baField.isCanUpdateInBA();
	}
	
	public void createColumns(){
		if(gridMode == BulkGridMode.SINGLE){
			ColumnConfig tbitsId = new ColumnConfig();
			tbitsId.setId(REQUEST);
			tbitsId.setHeader("tBits Id");
			tbitsId.setWidth(80);
			tbitsId.setMenuDisabled(true);
			cm.getColumns().add(tbitsId);
			dataTypeMap.put(REQUEST, ExcelImportDataType.Number);
		}
		
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
				if(!baField.getName().equals(REQUEST) && shouldShowField(baField)){
					ColumnConfig column = this.addColumn(baField, pref.getColSize());
					cm.getColumns().add(column);
				}
			}
		}
		
		this.reconfigure(store, cm);
	}
	
	/**
	 * Create a column.
	 * 
	 * @param baField. The field used to create column.
	 * @return The {@link ColumnConfig} to be added to the {@link ColumnModel}
	 */
	private ColumnConfig addColumn(final BAField baField, int size){
		final SingleGridContainer gridContainer = myContext.getValue(CONTEXT_SINGLE_GRID_CONTAINER, SingleGridContainer.class);
		
		ColumnConfig column = new ColumnConfig(); 
		column.setId(baField.getName());  
		column.setHeader(baField.getDisplayName());  
		column.setWidth(size);
		
		if(baField instanceof BAFieldCheckBox){
			CheckColumnConfig checkColumn = getCheckColumn();
			checkColumn.setId(baField.getName());  
			checkColumn.setHeader(baField.getDisplayName());
			checkColumn.setWidth(size);
			this.addPlugin(checkColumn);
			return checkColumn;
		}else if(baField instanceof BAFieldCombo){
			TypeFieldEditor editor = TypeFieldEditor.newInstance((BAFieldCombo) baField);
			column.setEditor(editor);
			column.setRenderer(new TypeGridCellRenderer((BAFieldCombo)baField));
			parserMap.put(baField.getName(), new TypeFieldParser(editor));
		}else if(baField instanceof BAFieldDate){
			column.setRenderer(new DateGridCellRenderer((BAFieldDate) baField));
			DateField dateField = new DateField();
			CellEditor editor = new TbitsCellEditor(dateField);
			editor.setCancelOnEsc(true);
			editor.setCompleteOnEnter(true);
			column.setEditor(editor);
//			column.setDateTimeFormat(DateTimeFormat.getFormat(BulkUpdateConstants.DATE_FORMAT));
		}else if(baField instanceof BAFieldMultiValue){
			UserListGridCellRenderer cellRenderer = new UserListGridCellRenderer((BAFieldMultiValue)baField);
			column.setRenderer(cellRenderer);
		    
		    UserPicker field = new UserPicker((BAFieldMultiValue) baField);
		    UserTypeFieldEditor editor = new UserTypeFieldEditor(field);
	    	column.setEditor(editor);
		}else if(baField instanceof BAFieldAttachment){
			AttachmentGridCellRenderer cellRenderer = new AttachmentGridCellRenderer((BAFieldAttachment)baField, Mode.EDIT){
				@Override
				public void afterUpdate(List<FileClient> atts, int row) {
					BulkUpdateGrid.this.gridView.refreshRow(row);
					if(BulkUpdateGrid.this.gridMode == BulkGridMode.COMMON && gridContainer != null){
						gridContainer.addFilesToAll((BAFieldAttachment) baField, atts);
					}
				}
				
				@Override
				public void onStatusChanged(int row) {
					BulkUpdateGrid.this.gridView.refreshRow(row);
				}
			};
			column.setRenderer(cellRenderer);
			
			attachmentRenderersMap.put(baField.getName(), cellRenderer);
		}else if(baField instanceof BAFieldTextArea){
			TextArea textAreaField = new TextArea();
			CellEditor editor = new TbitsCellEditor(textAreaField);
			editor.setCancelOnEsc(true);
			editor.setCompleteOnEnter(true);
			column.setEditor(editor);
			textAreaField.removeStyleName("x-small-editor");
			column.setRenderer(new WrapGridCellRenderer<TbitsTreeRequestData>());
		}else{
			TextField<String> text = new TextField<String>(); 
			CellEditor editor = new TbitsCellEditor(text);
			editor.setCancelOnEsc(true);
			editor.setCompleteOnEnter(true);
			column.setEditor(editor);
			column.setRenderer(new WrapGridCellRenderer<TbitsTreeRequestData>());
		}
		
		return column;
	}

	/**
	 * @return true if file upload is in progress 
	 */
	public boolean isBusyUploading() {
		for(AttachmentGridCellRenderer renderer : attachmentRenderersMap.values()){
			if(renderer.isBusyUploading())
				return true;
		}
		return false;
	}
	
	public void hideWindows(){
		for(AttachmentGridCellRenderer r : attachmentRenderersMap.values()){
			r.hideWindows();
		}
	}

	public Grid<TbitsTreeRequestData> getGrid() {
		return this;
	}

	public GridColumnView getViewId() {
		return GridColumnView.BulkUpdateGrid;
	}
	
	public List<ColPrefs> getPrefs(){
		return this.prefs;
	}

	public void setCustomizable(boolean isCustomizable) {
		this.isCustomizable = isCustomizable;
	}

	public boolean isCustomizable() {
		return isCustomizable;
	}
}
