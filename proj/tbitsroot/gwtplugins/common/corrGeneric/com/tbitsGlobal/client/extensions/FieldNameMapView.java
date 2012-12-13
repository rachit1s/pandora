package corrGeneric.com.tbitsGlobal.client.extensions;

import java.util.ArrayList;
import java.util.List;

import com.extjs.gxt.ui.client.Style.SelectionMode;
import com.extjs.gxt.ui.client.Style.SortDir;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.MessageBoxEvent;
import com.extjs.gxt.ui.client.event.SelectionChangedEvent;
import com.extjs.gxt.ui.client.event.SelectionChangedListener;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.store.Store;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.MessageBox;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.ComboBox;
import com.extjs.gxt.ui.client.widget.form.Field;
import com.extjs.gxt.ui.client.widget.form.FormPanel;
import com.extjs.gxt.ui.client.widget.form.LabelField;
import com.extjs.gxt.ui.client.widget.form.StoreFilterField;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.extjs.gxt.ui.client.widget.form.Validator;
import com.extjs.gxt.ui.client.widget.form.ComboBox.TriggerAction;
import com.extjs.gxt.ui.client.widget.grid.CellEditor;
import com.extjs.gxt.ui.client.widget.grid.CheckBoxSelectionModel;
import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
import com.extjs.gxt.ui.client.widget.grid.ColumnData;
import com.extjs.gxt.ui.client.widget.grid.ColumnModel;
import com.extjs.gxt.ui.client.widget.grid.EditorGrid;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.extjs.gxt.ui.client.widget.grid.GridCellRenderer;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.toolbar.ToolBar;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.tbitsGlobal.admin.client.state.AppState;
import com.tbitsGlobal.admin.client.utils.LinkIdentifier;
import com.tbitsGlobal.admin.client.widgets.APTabItem;
import commons.com.tbitsGlobal.utils.client.TbitsInfo;
import commons.com.tbitsGlobal.utils.client.cache.BusinessAreaCache;
import commons.com.tbitsGlobal.utils.client.cache.CacheRepository;
import commons.com.tbitsGlobal.utils.client.domainObjects.BusinessAreaClient;
import commons.com.tbitsGlobal.utils.client.domainObjects.FieldClient;
import commons.com.tbitsGlobal.utils.client.domainObjects.TypeClient;
import commons.com.tbitsGlobal.utils.client.log.Log;
import commons.com.tbitsGlobal.utils.client.widgets.ToolBarButton;

import corrGeneric.com.tbitsGlobal.client.CorrConstants;
import corrGeneric.com.tbitsGlobal.client.modelData.BAFieldMapClient;
import corrGeneric.com.tbitsGlobal.client.modelData.FieldNameMapClient;
import corrGeneric.com.tbitsGlobal.client.modelData.ReportMapClient;
import corrGeneric.com.tbitsGlobal.client.modelData.UserMapClient;
import corrGeneric.com.tbitsGlobal.server.cache.FieldNameCache;

public class FieldNameMapView extends APTabItem {

	protected ContentPanel contentPanel;
	protected ToolBar topBar;
	protected ToolBar bottomBar;
	protected ComboBox<FieldClient> fieldsCombo;
	protected ComboBox<BusinessAreaClient> baCombobox;
	
	protected EditorGrid<FieldNameMapClient> grid;
	protected ListStore<FieldNameMapClient> store;
	protected ListStore<BusinessAreaClient> baListStore;
	protected ArrayList<FieldNameMapClient> savedProperties;
	protected ListStore<FieldClient> fieldStore;
	protected StoreFilterField<FieldNameMapClient> filter;
	protected ColumnModel model;
	protected BusinessAreaClient currentBA;
	
	public FieldNameMapView(LinkIdentifier linkId){
		super(linkId);
		
		this.setClosable(true);
		this.setBorders(false);
		this.setLayout(new FitLayout());
		
		baListStore = new ListStore<BusinessAreaClient>();
		store = new ListStore<FieldNameMapClient>();
		fieldStore = new ListStore<FieldClient>();
		contentPanel = new ContentPanel();
		savedProperties = new ArrayList<FieldNameMapClient>();
		currentBA = new BusinessAreaClient();
	}
	
	public void onRender(Element parent, int index){
		super.onRender(parent, index);
		getBAList();
		build();
	}
	
	/**
	 * Get the list of currently loaded business areas from the cache
	 */
	protected void getBAList(){
		if(AppState.checkAppStateIsTill(AppState.BAMapReceived)){
			BusinessAreaCache cache = CacheRepository.getInstance().getCache(BusinessAreaCache.class);
			List<BusinessAreaClient> baList = new ArrayList<BusinessAreaClient>(cache.getValues());
			if((baList != null) && (!baList.isEmpty())){
				baListStore.add(baList);
			}else{
				TbitsInfo.error("Could not get the list of business areas... Please refresh....");
				Log.error("Error while getting list of ba's");
			}
		}
	}
	
	public void build(){
		contentPanel.setLayout(new FitLayout());
		contentPanel.setHeaderVisible(false);
		
		buildGrid();
		
		buildTopToolbar();
		
		buildBottomToolbar();
		
		setHandlers();
		
		contentPanel.add(grid);
		this.add(contentPanel);
		this.layout();
	}
	
	/**
	 * Set the handlers for combo box and other events. 
	 */
	protected void setHandlers(){		
		/*
		 * Initiates the process of populating the grid.
		 */
		baCombobox.addSelectionChangedListener(new SelectionChangedListener<BusinessAreaClient>(){
			
			public void selectionChanged(SelectionChangedEvent<BusinessAreaClient> se) {
				currentBA = se.getSelectedItem();
				baCombobox.setValue(currentBA);			
				getFields();
			}
		});	
	}
	
	protected void getFields(){
		CorrConstants.corrAdminService.getFields(currentBA.getSystemPrefix(), new AsyncCallback<ArrayList<FieldClient>>(){

			public void onFailure(Throwable caught) {
				TbitsInfo.error("Could not load fields from database... See logs for more information");
				Log.error("Could not load fields from database", caught);
			}

			public void onSuccess(ArrayList<FieldClient> result) {
				fieldStore.removeAll();
				fieldStore.add(result);
				getData();
			}
			
		});
	}
	
	protected void getData(){
		CorrConstants.corrAdminService.getFieldNameMap(currentBA.getSystemPrefix(), new AsyncCallback<ArrayList<FieldNameMapClient>>(){

			public void onFailure(Throwable caught) {
				TbitsInfo.error("Could not fetch field name properties from database... See logs for details...", caught);
				Log.error("Could not fetch field name properties", caught);
			}

			public void onSuccess(ArrayList<FieldNameMapClient> result) {
				if(null == result)
					TbitsInfo.info("Field Name Map not available for this business area");
				else populateGrid(result);
			}
			
		});
	}
	
	/**
	 * Populates the grid with received values from database
	 * @param list of report map properties which are to filled in the grid
	 */
	protected void populateGrid(ArrayList<FieldNameMapClient> result){
		savedProperties.addAll(result);
		grid.getStore().removeAll();
		store.add(result);		
		contentPanel.add(grid);
		this.add(contentPanel);
		this.layout();
	}
	
	protected void buildBottomToolbar(){
		bottomBar = new ToolBar();	
		//-----------------------------SAVE Button------------------------------------------------//
		ToolBarButton saveButton = new ToolBarButton("Save Settings", new SelectionListener<ButtonEvent>(){

			public void componentSelected(ButtonEvent ce){
				
				if(0 == store.getCount()){
					TbitsInfo.info("Cannot save empty table...");
					return;
				}
				
				savedProperties.clear();
				for(FieldNameMapClient property : grid.getStore().getModels()){	
					
					if((null == property.getField()) || (property.getField().getName().equals("")) 
							|| (property.getField().getName().equals("NULL"))){
						TbitsInfo.error("Empty value in 'Field Name'... Please enter a valid value...");
						return;
					}
					
					if((null == property.getCorrFieldName()) || (property.getCorrFieldName().equals(""))){
						TbitsInfo.error("Empty value in 'Correspondence Field Name'... Please enter a valid value...");
						return;
					}
									
					savedProperties.add(property);
				}			
				CorrConstants.corrAdminService.saveFieldNamemap(savedProperties, new AsyncCallback<Integer>(){
					public void onFailure(Throwable caught) {
						TbitsInfo.error("Could not save field name map to database... See logs for more information...", caught);
						Log.error("Could not save field name map to database", caught);
					}
					public void onSuccess(Integer result) {
						if(0 == result)
							TbitsInfo.info("All properties already saved into database...");
						else if(1 == result)
							TbitsInfo.info("Successfully saved 1 property into database...");
						else if(result > 1)
							TbitsInfo.info("Successfully saved " + result + " properties into database...");
						getData();						
					}
					
				});
			}
		});
		//-----------------------------ADD Button------------------------------------------------//
		ToolBarButton addButton		= new ToolBarButton("Add New Property", new SelectionListener<ButtonEvent>(){
			public void componentSelected(ButtonEvent ce){
				
				if(null == baCombobox.getValue()){
					TbitsInfo.error("Null value of Business Area... Please select a valid Business Area...");
					return;
				}
				
				FieldNameMapClient newProperty = new FieldNameMapClient();
				newProperty.setSysprefix(currentBA.getSystemPrefix());
				newProperty.setID("-1");
				newProperty.setCorrFieldName("");
				
				FieldClient tempField = new FieldClient();
				tempField.setName("NULL");
				tempField.setDisplayName("NULL");
				newProperty.setField(tempField);
				
				grid.getStore().add(newProperty);
			}
		});	
		//-----------------------------DELETE Button------------------------------------------------//
		
		ToolBarButton deleteButton	= new ToolBarButton("Delete Property", new SelectionListener<ButtonEvent>(){
			public void componentSelected(ButtonEvent ce) {
				final List<FieldNameMapClient> propertiesList = grid.getSelectionModel().getSelectedItems();
				Listener<MessageBoxEvent> l = new Listener<MessageBoxEvent>(){					
					public void handleEvent(MessageBoxEvent be) {
						Button pressedButton = be.getButtonClicked();
						if(pressedButton.getText().endsWith("Yes")){						
							if(propertiesList != null){
								
								for(int i = 0 ;i < propertiesList.size(); i++){
									removeFromGrid(propertiesList.get(i));
									if(propertiesList.get(i).getId().equals("-1")){
										propertiesList.remove(i);
									}
								}
								
								CorrConstants.corrAdminService.deleteFieldNameMapProperties(propertiesList, new AsyncCallback<Integer>(){

									public void onFailure(Throwable caught) {
										TbitsInfo.error("Could not delete properties from database... See logs for more information", caught);
										Log.error("Could not delete properties from database..", caught);									
									}

									public void onSuccess(Integer result) {
										if(1 == result)
											TbitsInfo.info("Successfully deleted 1 property from database...");
										else if(result > 1)
											TbitsInfo.info("Successfully deleted " + result + " properties from database...");
//										getData();	
									}
									
								});
							}
						}
					}
				};
				if(!propertiesList.isEmpty()){
					MessageBox.confirm("Confirm", "Are you sure you want to Delete ?",l);
				}else{
					if(0 == store.getCount())
						TbitsInfo.info("Cannot delete from empty table...");
					else TbitsInfo.info("Select a property to delete...");
				}
			}
		});
		
		bottomBar.add(saveButton);
		bottomBar.add(addButton);
		bottomBar.add(deleteButton);		
		contentPanel.setBottomComponent(bottomBar);	
	}
	
	protected void removeFromGrid(FieldNameMapClient property){
		for(int i = 0 ;i < store.getCount(); i++){
			if(!property.getId().equals("-1")){
				if(store.getAt(i).getId().equals(property.getId())){
					store.remove(i);
				}
			}else{
				if(store.getAt(i).getCorrFieldName().equals(property.getCorrFieldName())
						&& store.getAt(i).getField().equals(property.getField())){
					 		store.remove(i);
					 		break;
				}
			}
		}
	}

	protected void buildTopToolbar(){
		topBar = new ToolBar();
		applySearchFilter();
		buildComboBox();
		
		topBar.add(baCombobox);
		
		LabelField filterLabel = new LabelField("Search : ");
		
		topBar.add(filterLabel);
		topBar.add(filter);
		
		contentPanel.setTopComponent(topBar);
	}
	
	
	protected void buildComboBox(){
		baCombobox = new ComboBox<BusinessAreaClient>();
		baCombobox.setStore(baListStore);
		baCombobox.setDisplayField(BusinessAreaClient.SYSTEM_PREFIX);
		baCombobox.setForceSelection(false);
		baCombobox.setTriggerAction(TriggerAction.ALL);
		baCombobox.setTemplate(getComboTemplate());
		baCombobox.setSelectOnFocus(true);
		baCombobox.getStore().sort(BusinessAreaClient.SYSTEM_PREFIX, SortDir.ASC);
	
		if(baListStore != null){		
			baCombobox.setEmptyText("Choose a BA");
		}else{
			baCombobox.setEmptyText("BA's not loaded... Please Refresh...");
			TbitsInfo.error("Could not load BA's... Please Refresh...");
		}
	}
	
	private native String getComboTemplate()/*-{ 
    return  [ 
    '<tpl for=".">', 
    '<div class="x-combo-list-item" qtip="Description:{description}" qtitle="{system_prefix}">{display_name} [{system_prefix}]</div>', 
    '</tpl>' 
    ].join("");
	}-*/;
	
	/**
	 * Apply search filter to the search box provided in the top toolbar
	 */
	protected void applySearchFilter(){
		filter = new StoreFilterField<FieldNameMapClient>(){
			
			protected boolean doSelect(Store<FieldNameMapClient> store, FieldNameMapClient parent, FieldNameMapClient record,
					String property, String filter) {
		
				String corrFieldName = record.getCorrFieldName();
				corrFieldName = corrFieldName.toLowerCase();
				
				String fieldName = record.getField().getName();
				fieldName = fieldName.toLowerCase();
				
				
				if(corrFieldName.contains(filter.toLowerCase()) || fieldName.contains(filter.toLowerCase()))
					return true;
				return false;
			}
		};
		filter.bind(grid.getStore());
		filter.setEmptyText(" Search ");
	}
	
	
	protected void buildGrid(){
		CheckBoxSelectionModel<FieldNameMapClient> checkBoxModel = new CheckBoxSelectionModel<FieldNameMapClient>();
		checkBoxModel.setSelectionMode(SelectionMode.MULTI);
		
		ColumnConfig id	= new ColumnConfig(FieldNameMapClient.ID, "ID", 50);
		id.setHidden(true);
		
		ColumnConfig sysprefix = new ColumnConfig(FieldNameMapClient.SYS_PREFIX, "Sys Prefix", 100);
		ColumnConfig corrFieldName = new ColumnConfig(FieldNameMapClient.CORR_FIELD_NAME, "Correspondence Field Name", 250);
		ColumnConfig fieldName 	= new ColumnConfig(FieldNameMapClient.FIELD_NAME, "Field Name", 250);
		
		final TextField<String> corrFieldNameField = new TextField<String>();
		corrFieldNameField.setAllowBlank(false);
		GridCellRenderer<FieldNameMapClient> corrFieldNameRenderer = new GridCellRenderer<FieldNameMapClient>(){
			public String render(FieldNameMapClient model, String property, ColumnData config, int rowIndex, int colIndex,
					ListStore<FieldNameMapClient> store, Grid<FieldNameMapClient> grid) {
				if(model.getCorrFieldName().equals("")){
					return null;
				}
				return model.getCorrFieldName();
			}		
		};
		corrFieldName.setEditor(new CellEditor(corrFieldNameField));
		corrFieldName.setRenderer(corrFieldNameRenderer);
		corrFieldName.setSortable(false);
		
		fieldsCombo = new ComboBox<FieldClient>();
		fieldsCombo.setTriggerAction(TriggerAction.ALL);
		fieldsCombo.setForceSelection(false);
		fieldsCombo.setStore(fieldStore);
		fieldsCombo.setDisplayField(FieldClient.NAME);
		CellEditor editor = new CellEditor(fieldsCombo);
		fieldName.setEditor(editor);
		
		GridCellRenderer<FieldNameMapClient> fieldNameRenderer = new GridCellRenderer<FieldNameMapClient>(){
			public String render(FieldNameMapClient model, String property,	ColumnData config, int rowIndex, int colIndex,
					ListStore<FieldNameMapClient> store, Grid<FieldNameMapClient> grid) {
				if(null == model.getField())
					return null;
				return model.getField().getName();
			}
		};
		fieldName.setRenderer(fieldNameRenderer);
		fieldName.setSortable(false);
		
		ArrayList<ColumnConfig> clist = new ArrayList<ColumnConfig>();
		clist.add(checkBoxModel.getColumn());
		clist.add(id);
		clist.add(sysprefix);
		clist.add(corrFieldName);
		clist.add(fieldName);
		
		model = new ColumnModel(clist);
		grid = new EditorGrid<FieldNameMapClient>(store, model);
		grid.setSelectionModel(checkBoxModel);
		grid.addPlugin(checkBoxModel);
	}
}
