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
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.MessageBox;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.ComboBox;
import com.extjs.gxt.ui.client.widget.form.LabelField;
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
import commons.com.tbitsGlobal.utils.client.log.Log;
import commons.com.tbitsGlobal.utils.client.widgets.ToolBarButton;

import corrGeneric.com.tbitsGlobal.client.CorrConstants;
import corrGeneric.com.tbitsGlobal.client.modelData.BAFieldMapClient;

public class BAFieldMapView extends APTabItem {

	protected ContentPanel contentPanel;
	protected ToolBar topBar;
	protected ToolBar bottomBar;
	
	protected ComboBox<BusinessAreaClient> fromBAComboBox;
	protected ComboBox<BusinessAreaClient> toBAComboBox;
	protected ComboBox<FieldClient> fromFieldComboBox;
	protected ComboBox<FieldClient> toFieldComboBox;
	
	
	protected EditorGrid<BAFieldMapClient> grid;
	protected ListStore<BAFieldMapClient> store;
	protected ListStore<BusinessAreaClient> baListStore;
	protected ArrayList<BAFieldMapClient> savedProperties;
	protected ListStore<FieldClient> fromListStore;
	protected ListStore<FieldClient> toListStore;
	protected BusinessAreaClient fromBA;
	protected BusinessAreaClient toBA;
	protected ColumnModel model;
	
	public BAFieldMapView(LinkIdentifier linkId) {
		super(linkId);
		this.setClosable(true);
		this.setBorders(false);
		this.setLayout(new FitLayout());
		
		fromBA = new BusinessAreaClient();
		toBA = new BusinessAreaClient();
		store = new ListStore<BAFieldMapClient>();
		contentPanel = new ContentPanel();
		baListStore = new ListStore<BusinessAreaClient>();
		savedProperties = new ArrayList<BAFieldMapClient>();
		
		fromListStore = new ListStore<FieldClient>();
		toListStore	= new ListStore<FieldClient>();
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
	
	protected void build(){
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
		fromBAComboBox.addSelectionChangedListener(new SelectionChangedListener<BusinessAreaClient>(){			
			public void selectionChanged(SelectionChangedEvent<BusinessAreaClient> se) {
				fromBA = se.getSelectedItem();
				fromBAComboBox.setValue(fromBA);			
			}
		});	
		
		toBAComboBox.addSelectionChangedListener(new SelectionChangedListener<BusinessAreaClient>(){
			public void selectionChanged(SelectionChangedEvent<BusinessAreaClient> se) {
				toBA = se.getSelectedItem();
				toBAComboBox.setValue(toBA);
				
				getFields(fromBA.getSystemPrefix(), fromListStore);
				getFields(toBA.getSystemPrefix(), toListStore);
				getData();				
			}
		});
	}
	
	protected void getFields(String sysPrefix, final ListStore<FieldClient> fieldStore){
		CorrConstants.corrAdminService.getFields(sysPrefix, new AsyncCallback<ArrayList<FieldClient>>(){
			public void onFailure(Throwable caught) {
				TbitsInfo.error("Could not get fields from database...");
				Log.error("Could not get properties from database..", caught);
			}
			public void onSuccess(ArrayList<FieldClient> result) {
				fieldStore.removeAll();
				fieldStore.add(result);
			}			
		});
	}
	
	protected void getData(){
		
		CorrConstants.corrAdminService.getBAFieldMap(fromBA.getSystemPrefix(), toBA.getSystemPrefix(), new AsyncCallback<ArrayList<BAFieldMapClient>>(){
			public void onFailure(Throwable caught) {
				TbitsInfo.error("Could not get properties from database... See logs for more information");
				Log.error("Could not get properties from database", caught);
			}

			public void onSuccess(ArrayList<BAFieldMapClient> result) {
				if(result == null)
					TbitsInfo.info("No mapping exists for specified combination of business areas...");
				else populateGrid(result);
			}
			
		});		  
	}
	
	/**
	 * Populates the grid with received values from database
	 * @param list of report map properties which are to filled in the grid
	 */
	protected void populateGrid(ArrayList<BAFieldMapClient> result){
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
				for(BAFieldMapClient property : grid.getStore().getModels()){	
					
					if((property.getFromField() == null) || (property.getFromField().getName().equals("")) 
							|| (property.getFromField().getName().equals("NULL"))){
						TbitsInfo.error("Empty value in 'From Field Name'... Please enter a valid value...");
						return;
					}
					
					if((property.getToField() == null) || (property.getToField().getName().equals(""))
							|| (property.getToField().getName().equals("NULL"))){
						TbitsInfo.error("Empty value in 'To Field Name'... Please enter a valid value....");
						return;
					}
					
					savedProperties.add(property);
				}			

				CorrConstants.corrAdminService.saveBaFieldMap(savedProperties, new AsyncCallback<Integer>(){
					public void onFailure(Throwable caught) {
						TbitsInfo.error("Could not save BA Field Map to database... See logs for more information...");
						Log.error("Could not save ba field properties to database...", caught);
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
				
				if((null ==fromBA.getSystemPrefix()) || (null == toBA.getSystemPrefix())){
					if(null == fromBA.getSystemPrefix())
						TbitsInfo.error("Null value of 'From Sysprefix' ... Please select a valid Business Area...");
					else TbitsInfo.error("Null value of 'To Sysprefix' ... Please select a valid User Login...");
					return;
				}
				
				BAFieldMapClient newProperty = new BAFieldMapClient();
				newProperty.setFromSysprefix(fromBA.getSystemPrefix());
				newProperty.setToSysPrefix(toBA.getSystemPrefix());
				
				FieldClient newClient = new FieldClient();
				newClient.setName("NULL");
				newClient.setDisplayName("NULL");
				
				newProperty.setFromField(newClient);
				newProperty.setToField(newClient);
				
				newProperty.setID("-1");
				grid.getStore().add(newProperty);
				
			}
		});	
		
	//-----------------------------DELETE Button------------------------------------------------//
		
		ToolBarButton deleteButton	= new ToolBarButton("Delete Property", new SelectionListener<ButtonEvent>(){
			public void componentSelected(ButtonEvent ce) {
				final List<BAFieldMapClient> propertiesList = grid.getSelectionModel().getSelectedItems();
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
								
								CorrConstants.corrAdminService.deleteBAFieldMapProperties(propertiesList, new AsyncCallback<Integer>(){
									public void onFailure(Throwable caught) {
										TbitsInfo.error("Could not delete BA Field Map properties from database... See logs for more information...");
										Log.error("Could not delete ba field properties from db", caught);
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
	
	protected void removeFromGrid(BAFieldMapClient property){
		for(int i = 0 ;i < store.getCount(); i++){
			if(!property.getId().equals("-1")){
				if(store.getAt(i).getId().equals(property.getId())){
					store.remove(i);
				}
			}else{
				if(store.getAt(i).getFromField().equals(property.getFromField())
						&& store.getAt(i).getToField().equals(property.getToField())){
					 		store.remove(i);
					 		break;
						}
			}
		}
	}
	
	protected void buildTopToolbar(){
		topBar = new ToolBar();
		
		fromBAComboBox = new ComboBox<BusinessAreaClient>();
		buildComboBox(fromBAComboBox);
		toBAComboBox = new ComboBox<BusinessAreaClient>();
		buildComboBox(toBAComboBox);
		
		LabelField fromBaField = new LabelField("From Sysprefix : ");
		topBar.add(fromBaField);
		topBar.add(fromBAComboBox);
		
		LabelField toBaField = new LabelField("To Sysprefix : ");
		topBar.add(toBaField);
		topBar.add(toBAComboBox);
		
		contentPanel.setTopComponent(topBar);
		
	}
	
	protected void buildComboBox(ComboBox<BusinessAreaClient> baCombobox){
		baCombobox.setWidth(300);
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
	
	protected void buildGrid(){
		CheckBoxSelectionModel<BAFieldMapClient> checkBoxModel = new CheckBoxSelectionModel<BAFieldMapClient>();
		checkBoxModel.setSelectionMode(SelectionMode.MULTI);
		
		ColumnConfig id	= new ColumnConfig(BAFieldMapClient.ID, "ID", 50);
		id.setHidden(true);
		
		ColumnConfig fromBA	 = new ColumnConfig(BAFieldMapClient.FROM_SYSPREFIX, "From Sysprefix", 100);
		ColumnConfig fromField = new ColumnConfig(BAFieldMapClient.FROM_FIELD, "From Field Name", 200);
		ColumnConfig toBa	= new ColumnConfig(BAFieldMapClient.TO_SYSPREFIX, "To Sysprefix", 100);
		ColumnConfig toField = new ColumnConfig(BAFieldMapClient.TO_FIELD, "To Field Name", 200);
		
		fromFieldComboBox = new ComboBox<FieldClient>();
		fromField.setEditor(buildComboBox(fromFieldComboBox, fromListStore));
		GridCellRenderer<BAFieldMapClient> fromFieldRenderer = new GridCellRenderer<BAFieldMapClient>(){			
			public String render(BAFieldMapClient model, String property, ColumnData config, int rowIndex, int colIndex,
					ListStore<BAFieldMapClient> store, Grid<BAFieldMapClient> grid) {
				
				if(null == model.getFromField()){
					return null;
				}				
				return model.getFromField().getName();
			}		
		};	
		fromField.setRenderer(fromFieldRenderer);
		
		toFieldComboBox = new ComboBox<FieldClient>();
		toField.setEditor(buildComboBox(toFieldComboBox, toListStore));
		GridCellRenderer<BAFieldMapClient> toFieldRenderer = new GridCellRenderer<BAFieldMapClient>(){			
			public String render(BAFieldMapClient model, String property, ColumnData config, int rowIndex, int colIndex,
					ListStore<BAFieldMapClient> store, Grid<BAFieldMapClient> grid) {
				
				if(null == model.getToField()){
					return null;
				}				
				return model.getToField().getName();
			}		
		};	
		toField.setRenderer(toFieldRenderer);

		
		ArrayList<ColumnConfig> clist = new ArrayList<ColumnConfig>();
		clist.add(checkBoxModel.getColumn());
		clist.add(id);
		clist.add(fromBA);
		clist.add(fromField);
		clist.add(toBa);
		clist.add(toField);
		
		model = new ColumnModel(clist);
		grid = new EditorGrid<BAFieldMapClient>(store, model);
		grid.setSelectionModel(checkBoxModel);
		grid.addPlugin(checkBoxModel);
	}
	
	protected CellEditor buildComboBox(final ComboBox<FieldClient> box, ListStore<FieldClient> boxStore){
		
		box.setTriggerAction(TriggerAction.ALL);
		box.setForceSelection(false);
		box.setStore(boxStore);
		box.setDisplayField(FieldClient.NAME);
//		box.setEmptyText("Select a Field");
		
		CellEditor editor = new CellEditor(box);	
		return editor;
	}
	
}
