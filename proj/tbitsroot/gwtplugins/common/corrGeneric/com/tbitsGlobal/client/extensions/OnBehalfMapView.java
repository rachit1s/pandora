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
import com.extjs.gxt.ui.client.widget.form.LabelField;
import com.extjs.gxt.ui.client.widget.form.StoreFilterField;
import com.extjs.gxt.ui.client.widget.form.ComboBox.TriggerAction;
import com.extjs.gxt.ui.client.widget.grid.CellEditor;
import com.extjs.gxt.ui.client.widget.grid.CheckBoxSelectionModel;
import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
import com.extjs.gxt.ui.client.widget.grid.ColumnData;
import com.extjs.gxt.ui.client.widget.grid.ColumnModel;
import com.extjs.gxt.ui.client.widget.grid.EditorGrid;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.extjs.gxt.ui.client.widget.grid.GridCellRenderer;
import com.extjs.gxt.ui.client.widget.layout.FitData;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.toolbar.ToolBar;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.tbitsGlobal.admin.client.cache.UserCacheAdmin;
import com.tbitsGlobal.admin.client.state.AppState;
import com.tbitsGlobal.admin.client.utils.LinkIdentifier;
import com.tbitsGlobal.admin.client.widgets.APTabItem;
import com.tbitsGlobal.admin.client.widgets.BACombo;
import com.tbitsGlobal.jaguar.client.cache.UserCache;

import commons.com.tbitsGlobal.utils.client.TbitsInfo;
import commons.com.tbitsGlobal.utils.client.cache.BusinessAreaCache;
import commons.com.tbitsGlobal.utils.client.cache.CacheRepository;
import commons.com.tbitsGlobal.utils.client.domainObjects.BusinessAreaClient;
import commons.com.tbitsGlobal.utils.client.domainObjects.FieldClient;
import commons.com.tbitsGlobal.utils.client.domainObjects.TypeClient;
import commons.com.tbitsGlobal.utils.client.domainObjects.UserClient;
import commons.com.tbitsGlobal.utils.client.log.Log;
import commons.com.tbitsGlobal.utils.client.widgets.ToolBarButton;

import corrGeneric.com.tbitsGlobal.client.CorrConstants;
import corrGeneric.com.tbitsGlobal.client.modelData.BAFieldMapClient;
import corrGeneric.com.tbitsGlobal.client.modelData.OnBehalfMapClient;
import corrGeneric.com.tbitsGlobal.client.modelData.OnBehalfTypeClient;
import corrGeneric.com.tbitsGlobal.client.modelData.ReportMapClient;
import corrGeneric.com.tbitsGlobal.client.modelData.UserMapClient;
import corrGeneric.com.tbitsGlobal.shared.objects.GenericParams;

public class OnBehalfMapView extends APTabItem {

	protected ContentPanel contentPanel;
	protected ToolBar topBar;
	protected ToolBar bottomBar;
	
	protected ComboBox<TypeClient> type1Combo;
	protected ComboBox<TypeClient> type2Combo;
	protected ComboBox<TypeClient> type3Combo;
	protected ComboBox<UserClient> onBehalfUserCombo;
	
	protected ComboBox<BusinessAreaClient> baComboBox;
	protected ComboBox<UserClient> userComboBox;
	protected EditorGrid<OnBehalfMapClient> grid;
	protected ListStore<OnBehalfMapClient> store;
	protected ListStore<BusinessAreaClient> baListStore;
	protected ListStore<UserClient> userListStore;
	protected ArrayList<OnBehalfMapClient> savedProperties;
	protected BusinessAreaClient currentBA;
	protected UserClient currentUser;
	
	protected ListStore<TypeClient> type1Store;
	protected ListStore<TypeClient> type2Store;
	protected ListStore<TypeClient> type3Store;
	protected OnBehalfTypeClient onBehalfTypes;

	protected StoreFilterField<OnBehalfMapClient> filter;
	protected ColumnModel model;
	
	
	public OnBehalfMapView(LinkIdentifier linkId) {
		super(linkId);
		this.setClosable(true);
		this.setBorders(false);
		this.setLayout(new FitLayout());
		
		store = new ListStore<OnBehalfMapClient>();
		baListStore = new ListStore<BusinessAreaClient>();
		userListStore = new ListStore<UserClient>();
		contentPanel = new ContentPanel();
		baListStore = new ListStore<BusinessAreaClient>();
		savedProperties = new ArrayList<OnBehalfMapClient>();
		currentBA = new BusinessAreaClient();
		currentUser = new UserClient();
		onBehalfTypes = new OnBehalfTypeClient();
		
		type1Store = new ListStore<TypeClient>();
		type2Store = new ListStore<TypeClient>();
		type3Store = new ListStore<TypeClient>();
	}
	
	public void onRender(Element parent, int index){
		super.onRender(parent, index);
		
		
		OnBehalfMapPanel cp = new OnBehalfMapPanel();
		this.add(cp, new FitData());
		
//		getBAList();
//		getUsersList();
//		build();
	}

	protected void getUsersList(){
		if(AppState.checkAppStateIsTill(AppState.UserReceived)){
			UserCacheAdmin cache = CacheRepository.getInstance().getCache(UserCacheAdmin.class);
			List<UserClient> userList = new ArrayList<UserClient>(cache.getValues());
			if((userList != null) && (!userList.isEmpty())){
				for(UserClient user : userList){
					if(null != user)
						userListStore.add(user);
				}
			}else{
				TbitsInfo.error("Could not get the list of users... Please refresh....");
				Log.error("Error while getting list of users");
			}
		}
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
	
	protected void getData(){
		CorrConstants.corrAdminService.getOnBehalfMap(currentBA.getSystemPrefix(), currentUser.getUserLogin(), new AsyncCallback<ArrayList<OnBehalfMapClient>>(){

			public void onFailure(Throwable caught) {
				TbitsInfo.error("Could not fetch On Behalf Map from database... Please see logs for more information...", caught);
				Log.error("Could not fetch On Behalf Map from database", caught);
			}

			public void onSuccess(ArrayList<OnBehalfMapClient> result) {
				if(null == result){
					TbitsInfo.info("No map exists for the specified combination of BA and User Login...");
					grid.getStore().removeAll();
					return;
				}else{
					populateGrid(result);
				}			
			}
		});
	}
	
	protected void getOnBehalfTypes(){
		CorrConstants.corrAdminService.getOnBehalfTypes(currentBA.getSystemPrefix(), new AsyncCallback<OnBehalfTypeClient>(){

			public void onFailure(Throwable caught) {
				TbitsInfo.error("Could not get on behalf types from database... See logs for more information...");
				Log.error("Could not get on behalf types", caught);
				
			}
			public void onSuccess(OnBehalfTypeClient result) {
				if(null != result){
					onBehalfTypes = result;		
					Log.info("Successfully fetched on behalf types from database");
				}else{
					TbitsInfo.info("No on behalf types present for this BA");
					Log.info("No on behalf types present for this BA");
				}
			}		
		});
	}
	
	protected void populateGrid(ArrayList<OnBehalfMapClient> result){
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
		type1Store.add(onBehalfTypes.getOnBehalfTypeList(GenericParams.OnBehalfType1));
		type2Store.removeAll();
		type2Store.add(onBehalfTypes.getOnBehalfTypeList(GenericParams.OnBehalfType2));
		type3Store.removeAll();
		type3Store.add(onBehalfTypes.getOnBehalfTypeList(GenericParams.OnBehalfType3));
	}
	
	protected void setHandlers(){
		
		baComboBox.addSelectionChangedListener(new SelectionChangedListener<BusinessAreaClient>(){			
			public void selectionChanged(SelectionChangedEvent<BusinessAreaClient> se) {
				if(null != userComboBox.getSelection())
					userComboBox.clearSelections();
				if(null != grid.getStore())
					grid.getStore().removeAll();
				currentBA = se.getSelectedItem();
				baComboBox.setValue(currentBA);		
				getOnBehalfTypes();
				
			}
		});
		
		userComboBox.addSelectionChangedListener(new SelectionChangedListener<UserClient>(){
			public void selectionChanged(SelectionChangedEvent<UserClient> se){
				if(null == se.getSelectedItem() || (0 != userComboBox.getSelectionLength())){
					TbitsInfo.error("Null value of User or BA selected.... Please select a valid value");
					return;
				}
				currentUser = se.getSelectedItem();
				userComboBox.setValue(currentUser);
				getData();
			}
		});
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

				for(OnBehalfMapClient property : grid.getStore().getModels()){	
					
					if((null == property.getOnBehalfUser() || (property.getOnBehalfUser().getUserLogin().equals(""))
							|| (property.getOnBehalfUser().getUserLogin().equals("NULL")))){
						TbitsInfo.error("Invalid value in 'On Behalf Login' field... Please enter a valid value...");
						return;
					}
					
					savedProperties.add(property);
				}			
				CorrConstants.corrAdminService.saveOnBehalfMap(savedProperties, new AsyncCallback<ArrayList<OnBehalfMapClient>>(){

					public void onFailure(Throwable caught) {
						TbitsInfo.error("Could not save On Behalf Map to database... See logs for more information...");
						Log.error("Could not save On Behalf Map to database...", caught);
					}

					public void onSuccess(ArrayList<OnBehalfMapClient> result) {
						if(null != result){
							TbitsInfo.info("Successfully saved report properties to database...");
							populateGrid(result);
						}
					}
					
				});
			}
		});
		
		//-----------------------------ADD Button------------------------------------------------//
		ToolBarButton addButton		= new ToolBarButton("Add New Property", new SelectionListener<ButtonEvent>(){
			
			public void componentSelected(ButtonEvent ce){
				
				if((null == baComboBox.getValue()) || (null == userComboBox.getValue())){
					if(null == baComboBox.getValue())
						TbitsInfo.error("Null value of Business Area... Please select a valid Business Area...");
					else TbitsInfo.error("Null value of User Login... Please select a valid User Login...");
					return;
				}
					
				TypeClient newType = new TypeClient();
				newType.setName("NULL");
				newType.setDisplayName("NULL");
				newType.setDescription("NULL");
				newType.setSystemId(-1);
				newType.setTypeId(-1);
				
				UserClient newUser = new UserClient();
				newUser.setUserLogin("NULL");
				
				OnBehalfMapClient newOnBehalfClient = new OnBehalfMapClient();
				newOnBehalfClient.setSysprefix(currentBA.getSystemPrefix());
				newOnBehalfClient.setID("-1");
				newOnBehalfClient.setUser(currentUser);
				newOnBehalfClient.setOnBehalfUser(newUser);
				newOnBehalfClient.setStatus("---");
				
				newOnBehalfClient.setType1(newType);
				newOnBehalfClient.setType2(newType);
				newOnBehalfClient.setType3(newType);
				
				grid.getStore().add(newOnBehalfClient);			
			}
		});	
		
	//-----------------------------DELETE Button------------------------------------------------//
		
		ToolBarButton deleteButton	= new ToolBarButton("Delete Property", new SelectionListener<ButtonEvent>(){
			public void componentSelected(ButtonEvent ce) {
				final List<OnBehalfMapClient> propertiesList = grid.getSelectionModel().getSelectedItems();
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
								
								CorrConstants.corrAdminService.deleteOnBehalfProperties(propertiesList, new AsyncCallback<Integer>(){

									public void onFailure(Throwable caught) {
										TbitsInfo.error("Could not delete On Behalf Properties from database... See logs for more information...");
										Log.error("Could not delete on behlaf properties from database...", caught);
									}

									public void onSuccess(Integer count) {
										if(1 == count)
											TbitsInfo.info("Successfully deleted 1 property from database...");
										else if(count > 1)
											TbitsInfo.info("Successfully deleted " + count + " properties from database...");
//											getOnBehalfTypes();
//											getData();
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
	
	protected void removeFromGrid(OnBehalfMapClient property){
		for(int i = 0 ;i < store.getCount(); i++){
			if(!property.getId().equals("-1")){
				if(store.getAt(i).getId().equals(property.getId())){
					store.remove(i);
				}
			}else{
				if(store.getAt(i).getType1().getName().equals(property.getType1().getName())
						&& store.getAt(i).getType2().getName().equals(property.getType2().getName())
							&& store.getAt(i).getType3().getName().equals(property.getType3().getName())
								&& store.getAt(i).getOnBehalfUser().getUserLogin().equals(property.getOnBehalfUser().getUserLogin())){
									 		store.remove(i);
									 		break;
									 	}
			}
		}
	}
	
	protected void buildTopToolbar(){
		topBar = new ToolBar();
		applySearchFilter();
		baComboBox = new ComboBox<BusinessAreaClient>();
		buildBAComboBox(baComboBox);			
		topBar.add(baComboBox);
		
		LabelField userField = new LabelField("User Login : ");
		topBar.add(userField);
		userComboBox = new ComboBox<UserClient>();
		buildUserComboBox(userComboBox);
		topBar.add(userComboBox);
		
		LabelField filterLabel = new LabelField("Search : ");
		
		topBar.add(filterLabel);
		topBar.add(filter);
		
		contentPanel.setTopComponent(topBar);
	}
	
	/**
	 * Apply search filter to the search box provided in the top toolbar
	 */
	protected void applySearchFilter(){
		filter = new StoreFilterField<OnBehalfMapClient>(){
			
			protected boolean doSelect(Store<OnBehalfMapClient> store, OnBehalfMapClient parent, OnBehalfMapClient record,
					String property, String filter) {
				
				String type1 = record.getType1().getDisplayName();
				type1 = type1.toLowerCase();
				
				String type2 = record.getType2().getDisplayName();
				type2 = type2.toLowerCase();
				
				String type3 = record.getType3().getDisplayName();
				type3 = type3.toLowerCase();
			
				String onBehalfLogin = record.getOnBehalfUser().getUserLogin();
				onBehalfLogin = onBehalfLogin.toLowerCase();
				
				if((type1.contains(filter.toLowerCase())) || (type2.contains(filter.toLowerCase())) || (type3.contains(filter.toLowerCase())) 
						 || (onBehalfLogin.contains(filter.toLowerCase())))
					return true;
				return false;
			}
		};
		filter.bind(grid.getStore());
		filter.setEmptyText(" Search ");
	}
	
	protected void buildUserComboBox(ComboBox<UserClient> userComboBox){
		userComboBox.setStore(userListStore);
		userComboBox.setDisplayField(UserClient.USER_LOGIN);
		userComboBox.setForceSelection(false);
		userComboBox.setTriggerAction(TriggerAction.ALL);
		userComboBox.setSelectOnFocus(true);
		userComboBox.getStore().sort(UserClient.DISPLAY_NAME, SortDir.ASC);
	
		if(baListStore != null){		
			userComboBox.setEmptyText("Choose a user login");
		}else{
			userComboBox.setEmptyText("User list not loaded... Please Refresh...");
			TbitsInfo.error("Could not load user list... Please Refresh...");
		}
	}
	
	protected void buildBAComboBox(ComboBox<BusinessAreaClient> baCombobox){

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
		CheckBoxSelectionModel<OnBehalfMapClient> checkBoxModel = new CheckBoxSelectionModel<OnBehalfMapClient>();
		checkBoxModel.setSelectionMode(SelectionMode.MULTI);
		
		ColumnConfig id	= new ColumnConfig(OnBehalfMapClient.ID, "ID", 50);
		id.setHidden(true);
		
		ColumnConfig user  = new ColumnConfig(OnBehalfMapClient.USER, "User", 200);
		user.setHidden(true);
		
		ColumnConfig  type1 = new ColumnConfig(OnBehalfMapClient.TYPE_1, "On Behalf Type 1", 200);
		ColumnConfig  type2 = new ColumnConfig(OnBehalfMapClient.TYPE_2, "On Behalf Type 2", 200);
		ColumnConfig  type3 = new ColumnConfig(OnBehalfMapClient.TYPE_3, "On Behalf Type 3", 200);
		ColumnConfig  onBehalfUser = new ColumnConfig(OnBehalfMapClient.ON_BEHALF_USER, "On Behalf of login", 200);
		ColumnConfig  statusCol	  = new ColumnConfig(OnBehalfMapClient.STATUS, "Status", 200);
		
		GridCellRenderer<OnBehalfMapClient> statusRenderer = new GridCellRenderer<OnBehalfMapClient>(){
			public String render(OnBehalfMapClient model, String property,
					ColumnData config, int rowIndex, int colIndex,
					ListStore<OnBehalfMapClient> store, Grid<OnBehalfMapClient> grid) {
				String color = model.getStatus().equals("OK") ? "green" : "red"; 
				return "<span style='color:" + color + "'>" + model.getStatus() + "</span>";
			}
			
		};
		statusCol.setRenderer(statusRenderer);
		statusCol.setSortable(false);
		
		type1Combo = new ComboBox<TypeClient>();
		type1.setEditor(buildComboBox(type1Combo, type1Store));
		GridCellRenderer<OnBehalfMapClient> type1Renderer = new GridCellRenderer<OnBehalfMapClient>(){
			
			public String render(OnBehalfMapClient model, String property, ColumnData config, int rowIndex, int colIndex,
					ListStore<OnBehalfMapClient> store, Grid<OnBehalfMapClient> grid) {
				
				if(null == model.getType1()){
					TbitsInfo.error("Please select a valid Type");
					return "NULL";
				}				
				return model.getType1().getName();
			}		
		};
		type1.setRenderer(type1Renderer);
		type1.setSortable(false);
		
		type2Combo = new ComboBox<TypeClient>();
		type2.setEditor(buildComboBox(type2Combo, type2Store));
		GridCellRenderer<OnBehalfMapClient> type2Renderer = new GridCellRenderer<OnBehalfMapClient>(){
			
			public String render(OnBehalfMapClient model, String property, ColumnData config, int rowIndex, int colIndex,
					ListStore<OnBehalfMapClient> store, Grid<OnBehalfMapClient> grid) {
				
				if(null == model.getType2()){
					TbitsInfo.error("Please select a valid Type");
					return "NULL";
				}				
				return model.getType2().getName();
			}		
		};
		type2.setRenderer(type2Renderer);
		type2.setSortable(false);
		
		type3Combo = new ComboBox<TypeClient>();
		type3.setEditor(buildComboBox(type3Combo, type3Store));
		GridCellRenderer<OnBehalfMapClient> type3Renderer = new GridCellRenderer<OnBehalfMapClient>(){
			
			public String render(OnBehalfMapClient model, String property, ColumnData config, int rowIndex, int colIndex,
					ListStore<OnBehalfMapClient> store, Grid<OnBehalfMapClient> grid) {
				
				if(null == model.getType3()){
					TbitsInfo.error("Please select a valid Type");
					return "NULL";
				}				
				return model.getType3().getName();
			}		
		};
		type3.setRenderer(type3Renderer);
		type3.setSortable(false);
		
		onBehalfUserCombo = new ComboBox<UserClient>();
		onBehalfUserCombo.setTriggerAction(TriggerAction.ALL);
		onBehalfUserCombo.setForceSelection(false);
		onBehalfUserCombo.setStore(userListStore);
		onBehalfUserCombo.setDisplayField(UserClient.USER_LOGIN);	
		CellEditor editor = new CellEditor(onBehalfUserCombo);	
		onBehalfUser.setEditor(editor);
		GridCellRenderer<OnBehalfMapClient> onBehalfUserRenderer = new GridCellRenderer<OnBehalfMapClient>(){
			
			public String render(OnBehalfMapClient model, String property, ColumnData config, int rowIndex, int colIndex,
					ListStore<OnBehalfMapClient> store, Grid<OnBehalfMapClient> grid) {
				
				if(null == model.getOnBehalfUser()){
					TbitsInfo.error("Please select a valid on behalf user");
					return "NULL";
				}				
				return model.getOnBehalfUser().getUserLogin();
			}		
		};
		onBehalfUser.setRenderer(onBehalfUserRenderer);
		onBehalfUser.setSortable(false);
		
		ArrayList<ColumnConfig> clist = new ArrayList<ColumnConfig>();
		clist.add(checkBoxModel.getColumn());
		clist.add(id);
		clist.add(user);
		clist.add(type1);
		clist.add(type2);
		clist.add(type3);
		clist.add(onBehalfUser);
		clist.add(statusCol);
		
		model = new ColumnModel(clist);
		grid = new EditorGrid<OnBehalfMapClient>(store, model);
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
		box.setDisplayField(TypeClient.NAME);
		
		CellEditor editor = new CellEditor(box);		
		return editor;
	}

}
