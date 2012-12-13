package corrGeneric.com.tbitsGlobal.client.extensions;

import java.util.ArrayList;
import java.util.List;

import com.extjs.gxt.ui.client.Style.HorizontalAlignment;
import com.extjs.gxt.ui.client.Style.SelectionMode;
import com.extjs.gxt.ui.client.Style.SortDir;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.FieldEvent;
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
import com.extjs.gxt.ui.client.widget.form.LabelField;
import com.extjs.gxt.ui.client.widget.form.StoreFilterField;
import com.extjs.gxt.ui.client.widget.form.TextField;
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
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.tbitsGlobal.admin.client.state.AppState;
import com.tbitsGlobal.admin.client.utils.LinkIdentifier;
import com.tbitsGlobal.admin.client.widgets.APTabItem;

import commons.com.tbitsGlobal.utils.client.TbitsInfo;
import commons.com.tbitsGlobal.utils.client.cache.BusinessAreaCache;
import commons.com.tbitsGlobal.utils.client.cache.CacheRepository;
import commons.com.tbitsGlobal.utils.client.domainObjects.BusinessAreaClient;
import commons.com.tbitsGlobal.utils.client.domainObjects.TypeClient;
import commons.com.tbitsGlobal.utils.client.log.Log;
import commons.com.tbitsGlobal.utils.client.widgets.ToolBarButton;

import corrGeneric.com.tbitsGlobal.client.CorrConstants;
import corrGeneric.com.tbitsGlobal.client.modelData.ReportMapClient;
import corrGeneric.com.tbitsGlobal.client.modelData.ReportTypeClient;
import corrGeneric.com.tbitsGlobal.shared.objects.GenericParams;
/**
 * Page for the 'Report Map' tab under 'Correspondence'
 * Constructs and populates the grid after fetching report properties from the 
 * table report_map. Also handles the addition/deletion/modification of map entries 
 * from the same table.
 * @author devashish
 *
 */
public class ReportMapView extends APTabItem {

	
	protected ContentPanel contentPanel;
	protected ToolBar	topBar;
	protected ToolBar	bottomBar;
	protected ComboBox<BusinessAreaClient> baCombobox;
	
	protected ComboBox<TypeClient> type1Combo;
	protected ComboBox<TypeClient> type2Combo;
	protected ComboBox<TypeClient> type3Combo;
	protected ComboBox<TypeClient> type4Combo;
	protected ComboBox<TypeClient> type5Combo;
	
	protected ListStore<BusinessAreaClient>	baListStore;
	
	protected ListStore<TypeClient> type1Store;
	protected ListStore<TypeClient> type2Store;
	protected ListStore<TypeClient> type3Store;
	protected ListStore<TypeClient> type4Store;
	protected ListStore<TypeClient> type5Store;

	protected BusinessAreaClient currentBA;
	protected ReportTypeClient	reportTypesList;
	protected ArrayList<ReportMapClient> savedProperties;
	protected EditorGrid<ReportMapClient> grid;
	protected ListStore<ReportMapClient> store;
	protected StoreFilterField<ReportMapClient> filter;
	protected ColumnModel	model;

	/**
	 * Constructor
	 */
	public ReportMapView(LinkIdentifier linkId) {
		super(linkId);
		this.setBorders(false);
		this.setLayout(new FitLayout());
		this.setClosable(true);
		
		savedProperties = new ArrayList<ReportMapClient>();
		
		currentBA = new BusinessAreaClient();
		store = new ListStore<ReportMapClient>();
		contentPanel = new ContentPanel();
		baListStore = new ListStore<BusinessAreaClient>();
		
		type1Store = new ListStore<TypeClient>();
		type2Store = new ListStore<TypeClient>();
		type3Store = new ListStore<TypeClient>();
		type4Store = new ListStore<TypeClient>();
		type5Store = new ListStore<TypeClient>();
		
		reportTypesList = new ReportTypeClient();		
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
	
	/**
	 * Build the panel components i.e. Toolbars, grid, comboboxes and attach handlers to them
	 */
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
		baCombobox.addSelectionChangedListener(new SelectionChangedListener<BusinessAreaClient>(){
			
			public void selectionChanged(SelectionChangedEvent<BusinessAreaClient> se) {
				currentBA = se.getSelectedItem();
				baCombobox.setValue(currentBA);			
				getReportTypes();
			}
		});	
		
	}
	
	/**
	 * Get the report types for each report type column. These are fetched before the actual grid values
	 * so that any discrepency between the value received and the set of valid values can be detected
	 * before the rendering of grid. After the successful call, grid values are fetched.
	 */
	protected void getReportTypes(){
		CorrConstants.corrAdminService.getReportTypes(currentBA.getSystemPrefix(), new AsyncCallback<ReportTypeClient>(){

			
			public void onFailure(Throwable caught) {
				TbitsInfo.error("Could not get report types list from database... Please Refresh... ");
				Log.error("Could not get report types list from database... Please Refresh...", caught);
			}
			
			public void onSuccess(ReportTypeClient reportTypes) {
				if(null == reportTypes){
					TbitsInfo.info("No Report Types present for this Business Area...");
					Log.info("No Report Types present for this Business Area...");
				}else{
					reportTypesList = reportTypes;			
					getReportMapProperties();
				}
			}		
		});
	}
	
	/**
	 * Get the values from report_map table in the database to fill the grid
	 */
	protected void getReportMapProperties(){
		CorrConstants.corrAdminService.getReportMapProperties(currentBA.getSystemPrefix(), new AsyncCallback<ArrayList<ReportMapClient>>(){
		
			public void onFailure(Throwable caught) {
				TbitsInfo.error("Could not load report map properties from database... Please try again...");
				Log.error("Could not load report map properties from database", caught);
			}
			
			public void onSuccess(ArrayList<ReportMapClient> result) {
				if(null == result){
					TbitsInfo.info("No Correspondence Report Properties Present for this Business Area");
					Log.info("No Correspondence Report Properties Present for this Business Area");
				}else populateGrid(result);
			}
			
		});
	}
	
	/**
	 * Populates the grid with received values from database
	 * @param list of report map properties which are to filled in the grid
	 */
	protected void populateGrid(ArrayList<ReportMapClient> result){
		savedProperties.addAll(result);
		grid.getStore().removeAll();
		populateComboBoxes();
		store.add(result);		
		contentPanel.add(grid);
		this.add(contentPanel);
		this.layout();
	}
	
	/**
	 * Populate the combo boxes from their respective list contained in a common collection
	 */
	protected void populateComboBoxes(){
		type1Store.removeAll();
		type1Store.add(reportTypesList.getReportTypeList(GenericParams.ReportType1));
		type2Store.removeAll();
		type2Store.add(reportTypesList.getReportTypeList(GenericParams.ReportType2));
		type3Store.removeAll();
		type3Store.add(reportTypesList.getReportTypeList(GenericParams.ReportType3));
		type4Store.removeAll();
		type4Store.add(reportTypesList.getReportTypeList(GenericParams.ReportType4));
		type5Store.removeAll();
		type5Store.add(reportTypesList.getReportTypeList(GenericParams.ReportType5));
	}
	
	/**
	 * Build the bottom toolbar. It contains the buttons for saving/deleting/adding new properties
	 */
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
				for(ReportMapClient property : grid.getStore().getModels()){	
					ReportMapClient temp = new ReportMapClient();
		
					temp.setId(property.getId());
					temp.setSysPrefix(property.getSysPrefix());
					temp.setReportId(property.getReportId());
					temp.setStatus(property.getStatus());
					
					temp.setType1(property.getType1());
					temp.setType2(property.getType2());
					temp.setType3(property.getType3());
					temp.setType4(property.getType4());
					temp.setType5(property.getType5());
									
					savedProperties.add(temp);
				}			
				
				CorrConstants.corrAdminService.saveReportMapProperties(savedProperties, new AsyncCallback<ArrayList<ReportMapClient>>(){

					
					public void onFailure(Throwable caught) {
						TbitsInfo.error("Could not save report properties to database... see logs for details");
						Log.error("Could not save report properties to database...", caught);						
					}

					public void onSuccess(ArrayList<ReportMapClient> result){
						TbitsInfo.info("Successfully saved report properties to database...");
						populateGrid(result);
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
				
				TypeClient newReportType = new TypeClient();
				newReportType.setName("NULL");
				newReportType.setDisplayName("NULL");
				newReportType.setDescription("NULL");
				newReportType.setSystemId(-1);
				newReportType.setTypeId(-1);
				
				ReportMapClient newReportMap = new ReportMapClient();
				newReportMap.setSysPrefix(currentBA.getSystemPrefix());
				newReportMap.setId("-1");
				newReportMap.setReportId("-1");
				newReportMap.setStatus("---");
				
				newReportMap.setType1(newReportType);
				newReportMap.setType2(newReportType);
				newReportMap.setType3(newReportType);
				newReportMap.setType4(newReportType);
				newReportMap.setType5(newReportType);
				
				grid.getStore().add(newReportMap);
			}
		});	
		//-----------------------------DELETE Button------------------------------------------------//
		
		ToolBarButton deleteButton	= new ToolBarButton("Delete Property", new SelectionListener<ButtonEvent>(){
			public void componentSelected(ButtonEvent ce) {
				final List<ReportMapClient> propertiesList = grid.getSelectionModel().getSelectedItems();
				Listener<MessageBoxEvent> l = new Listener<MessageBoxEvent>(){					
					public void handleEvent(MessageBoxEvent be) {
						Button pressedButton = be.getButtonClicked();
						if(pressedButton.getText().endsWith("Yes")){						
							if(propertiesList != null){

								for(int i = 0 ; i < propertiesList.size(); i++){
									removeFromGrid(propertiesList.get(i));
								}
								
								CorrConstants.corrAdminService.deleteReportMapProperties(propertiesList, new AsyncCallback<Integer>(){
									
									public void onFailure(Throwable caught) {
										TbitsInfo.error("Could not delete properties from database... See Logs for details...");
										Log.error("Could not delete properties from database", caught);
									}
									
									public void onSuccess(Integer count) {
										if(1 == count)
											TbitsInfo.info("Successfully deleted 1 property from database...");
										else if(count > 1)
											TbitsInfo.info("Successfully deleted " + count + " properties from database...");
//										getReportTypes();		
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
	
	protected void removeFromGrid(ReportMapClient property){
		for(int i = 0 ;i < store.getCount(); i++){
			if(!property.getId().equals("-1")){
				if(store.getAt(i).getId().equals(property.getId())){
					store.remove(i);
					break;
				}
			}else{
				if(store.getAt(i).getReportId().equals(property.getReportId())
						&& store.getAt(i).getType1().getName().equals(property.getType1().getName())
							&& store.getAt(i).getType2().getName().equals(property.getType2().getName())
								&& store.getAt(i).getType3().getName().equals(property.getType3().getName())
									&& store.getAt(i).getType4().getName().equals(property.getType4().getName())
										&& store.getAt(i).getType5().getName().equals(property.getType5().getName())){
					 		store.remove(i);
					 		break;
						}
			}
		}
	}
	
	/**
	 * Build the top toolbar. It contains the list of business areas currently loaded
	 * and the search bar
	 */
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
	
	/**
	 * Build the combobox for showing the list of business areas.
	 */
	protected void buildComboBox(){
		baCombobox = new ComboBox<BusinessAreaClient>();
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
	
	/**
	 * Apply search filter to the search box provided in the top toolbar
	 */
	protected void applySearchFilter(){
		filter = new StoreFilterField<ReportMapClient>(){
			
			protected boolean doSelect(Store<ReportMapClient> store, ReportMapClient parent, ReportMapClient record,
					String property, String filter) {
				
				String reportType1 = record.getType1().getDisplayName();
				reportType1 = reportType1.toLowerCase();
				
				String reportType2 = record.getType2().getDisplayName();
				reportType2 = reportType2.toLowerCase();
				
				String reportType3 = record.getType3().getDisplayName();
				reportType3 = reportType3.toLowerCase();
				
				String reportType4 = record.getType4().getDisplayName();
				reportType4 = reportType4.toLowerCase();
				
				String reportType5 = record.getType5().getDisplayName();
				reportType5 = reportType5.toLowerCase();
				
				
				if((reportType1.contains(filter.toLowerCase())) || (reportType2.contains(filter.toLowerCase())) || (reportType3.contains(filter.toLowerCase()))
						|| (reportType4.contains(filter.toLowerCase())) || reportType5.contains(filter.toLowerCase()))
					return true;
				return false;
			}
		};
		filter.bind(grid.getStore());
		filter.setEmptyText(" Search ");
	}
	
	/**
	 * Builds the grid into which the values will be populated. 
	 * The report Id and report type fields are editable while the status field is not editable.
	 * The report type cells are value constrained cells with the values in combo boxes attached.
	 */
	protected void buildGrid(){
		
		CheckBoxSelectionModel<ReportMapClient> checkBoxModel = new CheckBoxSelectionModel<ReportMapClient>();
		checkBoxModel.setSelectionMode(SelectionMode.MULTI);
		
		ColumnConfig id	= new ColumnConfig(ReportMapClient.ID, "ID", 50);
		id.setHidden(true);
		
		ColumnConfig reportId = new ColumnConfig(ReportMapClient.REPORT_ID, "Report ID", 100);
		ColumnConfig sysPrefix = new ColumnConfig(ReportMapClient.SYS_PREFIX, "Sys Prefix", 150);
		ColumnConfig reportType1 = new ColumnConfig(GenericParams.ReportType1, "Report Type 1", 130);
		ColumnConfig reportType2 = new ColumnConfig(GenericParams.ReportType2, "Report Type 2", 130);
		ColumnConfig reportType3 = new ColumnConfig(GenericParams.ReportType3, "Report Type 3", 130);
		ColumnConfig reportType4 = new ColumnConfig(GenericParams.ReportType4, "Report Type 4", 130);
		ColumnConfig reportType5 = new ColumnConfig(GenericParams.ReportType5, "Report Type 5", 130);
		ColumnConfig statusCol	= new ColumnConfig(ReportMapClient.STATUS,"Status", 200);
		
		GridCellRenderer<ReportMapClient> statusRenderer = new GridCellRenderer<ReportMapClient>(){
			public String render(ReportMapClient model, String property,
					ColumnData config, int rowIndex, int colIndex,
					ListStore<ReportMapClient> store, Grid<ReportMapClient> grid) {
				String color = model.getStatus().equals("OK") ? "green" : "red"; 
				return "<span style='color:" + color + "'>" + model.getStatus() + "</span>";
			}
			
		};
		statusCol.setRenderer(statusRenderer);
		statusCol.setSortable(false);
		
		
		final TextField<String> reportIdField = new TextField<String>();
		reportIdField.setAllowBlank(false);
		reportIdField.sinkEvents(Event.ONCHANGE);
		reportIdField.addListener(Events.OnChange, new Listener<FieldEvent>(){			
			public void handleEvent(FieldEvent be) {
				try{
					Integer reportId = Integer.parseInt(be.getField().getValue().toString());
					if(-1 == reportId){
						reportIdField.clear();
						TbitsInfo.error("Please enter a valid Report ID value...");
					}else reportIdField.setValue(Integer.toString(reportId));
				}catch (NumberFormatException nfe){
					reportIdField.clear();
					reportIdField.setValue(reportIdField.getOriginalValue());				
					TbitsInfo.error("Please Enter an Integer Value in Report ID");
				}catch (Exception e){
					reportIdField.clear();
					TbitsInfo.error("Report ID cannot be left blank... Please Enter a value...");
					Log.error("Null value in report id ", e);
				}
			}
			
		});			
		reportId.setSortable(false);
		reportId.setAlignment(HorizontalAlignment.CENTER);
		reportId.setEditor(new CellEditor(reportIdField));
		
		type1Combo = new ComboBox<TypeClient>();
		reportType1.setEditor(buildComboBox(type1Combo, type1Store));
		GridCellRenderer<ReportMapClient> type1Renderer = new GridCellRenderer<ReportMapClient>(){
			
			public String render(ReportMapClient model, String property, ColumnData config, int rowIndex, int colIndex,
					ListStore<ReportMapClient> store, Grid<ReportMapClient> grid) {
				
				if(null == model.getType1()){
					TbitsInfo.error("Please select a valid report Type");
					return "NULL";
				}				
				return model.getType1().getDisplayName();
			}		
		};
		reportType1.setRenderer(type1Renderer);
		reportType1.setSortable(false);
		

		type2Combo = new ComboBox<TypeClient>();
		reportType2.setEditor(buildComboBox(type2Combo, type2Store));
		GridCellRenderer<ReportMapClient> type2Renderer = new GridCellRenderer<ReportMapClient>(){
			
			public String render(ReportMapClient model, String property, ColumnData config, int rowIndex, int colIndex,
					ListStore<ReportMapClient> store, Grid<ReportMapClient> grid) {
				if(null == model.getType2()){
					TbitsInfo.error("Please select a valid report Type");
					return "NULL";
				}
				return model.getType2().getDisplayName();
			}		
		};
		reportType2.setRenderer(type2Renderer);
		reportType2.setSortable(false);
		
		type3Combo = new ComboBox<TypeClient>();
		reportType3.setEditor(buildComboBox(type3Combo, type3Store));
		GridCellRenderer<ReportMapClient> type3Renderer = new GridCellRenderer<ReportMapClient>(){
			
			public String render(ReportMapClient model, String property, ColumnData config, int rowIndex, int colIndex,
					ListStore<ReportMapClient> store, Grid<ReportMapClient> grid) {
				if(null == model.getType3()){
					TbitsInfo.error("Please select a valid report Type");
					return "NULL";
				}
				return model.getType3().getDisplayName();
			}		
		};
		reportType3.setRenderer(type3Renderer);
		reportType3.setSortable(false);
		
		type4Combo = new ComboBox<TypeClient>();
		reportType4.setEditor(buildComboBox(type4Combo, type4Store));
		GridCellRenderer<ReportMapClient> type4Renderer = new GridCellRenderer<ReportMapClient>(){
			
			public String render(ReportMapClient model, String property, ColumnData config, int rowIndex, int colIndex,
					ListStore<ReportMapClient> store, Grid<ReportMapClient> grid) {
				if(null == model.getType4()){
					TbitsInfo.error("Please select a valid report Type");
					return "NULL";
				}
				return model.getType4().getDisplayName();
			}		
		};
		reportType4.setRenderer(type4Renderer);
		reportType4.setSortable(false);
		
		type5Combo = new ComboBox<TypeClient>();
		reportType5.setEditor(buildComboBox(type5Combo, type5Store));
		GridCellRenderer<ReportMapClient> type5Renderer = new GridCellRenderer<ReportMapClient>(){
			
			public String render(ReportMapClient model, String property, ColumnData config, int rowIndex, int colIndex,
					ListStore<ReportMapClient> store, Grid<ReportMapClient> grid) {
				if(null == model.getType5()){
					TbitsInfo.error("Please select a valid report Type");
					return "NULL";
				}
				return model.getType5().getDisplayName();
			}		
		};
		reportType5.setRenderer(type5Renderer);
		reportType5.setSortable(false);
		
		ArrayList<ColumnConfig> clist = new ArrayList<ColumnConfig>();
		clist.add(checkBoxModel.getColumn());
		clist.add(id);
		clist.add(sysPrefix);
		clist.add(reportId);
		clist.add(reportType1);
		clist.add(reportType2);
		clist.add(reportType3);
		clist.add(reportType4);
		clist.add(reportType5);
		clist.add(statusCol);
				
		model = new ColumnModel(clist);		
		grid  = new EditorGrid<ReportMapClient>(store, model);
		grid.setSelectionModel(checkBoxModel);
		grid.addPlugin(checkBoxModel);	
	}
	
	/**
	 * Builds the combo box and attaches a celleditor to it
	 * @param ComboBox object, box which has to be built
	 * @param boxStore, store to be attached to the box
	 * @return CellEditor, editor attached to the box
	 */
	protected CellEditor buildComboBox(final ComboBox<TypeClient> box, ListStore<TypeClient> boxStore){
		
		box.setTriggerAction(TriggerAction.ALL);
		box.setForceSelection(false);
		box.setStore(boxStore);
		box.setDisplayField(TypeClient.DISPLAY_NAME);
		
		CellEditor editor = new CellEditor(box);		
		return editor;
	}
}
