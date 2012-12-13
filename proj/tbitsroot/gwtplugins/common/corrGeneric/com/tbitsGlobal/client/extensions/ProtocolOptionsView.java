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
import com.extjs.gxt.ui.client.widget.Window;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.ComboBox;
import com.extjs.gxt.ui.client.widget.form.FormPanel;
import com.extjs.gxt.ui.client.widget.form.StoreFilterField;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.extjs.gxt.ui.client.widget.form.ComboBox.TriggerAction;
import com.extjs.gxt.ui.client.widget.grid.CheckBoxSelectionModel;
import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
import com.extjs.gxt.ui.client.widget.grid.ColumnModel;
import com.extjs.gxt.ui.client.widget.grid.EditorGrid;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.layout.FormLayout;
import com.extjs.gxt.ui.client.widget.toolbar.ToolBar;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.tbitsGlobal.admin.client.events.OnBACreated;
import com.tbitsGlobal.admin.client.state.AppState;
import com.tbitsGlobal.admin.client.utils.LinkIdentifier;
import com.tbitsGlobal.admin.client.widgets.APTabItem;
import commons.com.tbitsGlobal.utils.client.TbitsCellEditor;
import commons.com.tbitsGlobal.utils.client.TbitsInfo;
import commons.com.tbitsGlobal.utils.client.Events.BaseTbitsObservable;
import commons.com.tbitsGlobal.utils.client.Events.ITbitsEventHandle;
import commons.com.tbitsGlobal.utils.client.Events.TbitsObservable;
import commons.com.tbitsGlobal.utils.client.cache.BusinessAreaCache;
import commons.com.tbitsGlobal.utils.client.cache.CacheRepository;
import commons.com.tbitsGlobal.utils.client.domainObjects.BusinessAreaClient;
import commons.com.tbitsGlobal.utils.client.log.Log;
import commons.com.tbitsGlobal.utils.client.widgets.ToolBarButton;

import corrGeneric.com.tbitsGlobal.client.CorrConstants;
import corrGeneric.com.tbitsGlobal.client.modelData.CorrProtocolClient;
import corrGeneric.com.tbitsGlobal.client.modelData.ReportNameClient;
/**
 * Page for the "Protocol Options" tab under "Correspondence"
 * Constructs and populates the grid with correspondence protocol properties for
 * the ba selected in the combo box. Also handles addtion/deletion/modification of 
 * properties from the same table.
 * @author devashish
 *
 */
public class ProtocolOptionsView extends APTabItem {
	
	protected ContentPanel contentPanel;
	protected ToolBar 	topBar;
	protected ToolBar  	bottomBar;
	
	protected BusinessAreaClient currentBA;
	protected ListStore<BusinessAreaClient>		baListStore;
	protected ArrayList<CorrProtocolClient> 	savedProperties;
	
	protected ListStore<CorrProtocolClient> store;
	protected EditorGrid<CorrProtocolClient>  grid;
	protected ComboBox<BusinessAreaClient> baCombobox;
	protected StoreFilterField<CorrProtocolClient> filter;
	protected ColumnModel	model;
	
	protected TbitsObservable observable;
	/**
	 * Constructor
	 */
	public ProtocolOptionsView(LinkIdentifier linkId) {
		super(linkId);
		this.setBorders(false);
		this.setLayout(new FitLayout());
		this.setClosable(true);
		
		observable		= new BaseTbitsObservable();
		observable.attach();
		currentBA = new BusinessAreaClient();
		baListStore		= new ListStore<BusinessAreaClient>();
		savedProperties = new ArrayList<CorrProtocolClient>();
		store		= new ListStore<CorrProtocolClient>();
		contentPanel = new ContentPanel();
	}
	
	public void onRender(Element parent, int index){
		super.onRender(parent, index);
		getBAList();
		build();
	}
	
	/**
	 * Get the list of business areas.
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
	 * Build the contentpanel, the toolbars and the grid in which the values will be shown
	 */
	protected void build(){
		contentPanel.setLayout(new FitLayout());
		contentPanel.setHeaderVisible(false);
		buildGrid();
		buildToolbars();
		setHandlers();
		contentPanel.add(grid);
		this.add(contentPanel);
		this.layout();
	}
	
	/**
	 * Sets the handlers 
	 */
	protected void setHandlers(){

		baCombobox.addSelectionChangedListener(new SelectionChangedListener<BusinessAreaClient>(){
			
			public void selectionChanged(SelectionChangedEvent<BusinessAreaClient> se) {
				currentBA = se.getSelectedItem();
				baCombobox.setValue(currentBA);			
				getProtocolProperties(currentBA.getSystemPrefix());
			}
		});	
		
		/**
		 * TODO: test by creating new ba
		 */
		observable.subscribe(OnBACreated.class, new ITbitsEventHandle<OnBACreated>(){
			
			public void handleEvent(OnBACreated event) {
				newBAAdded(event.getNewBA());
			}
			
		});
	}
	
	/**
	 * Called when a new business area is added 
	 * @param businessArea
	 */
	protected void newBAAdded(BusinessAreaClient ba){
		baListStore.add(ba);
		getProtocolProperties(ba.getSystemPrefix());
		
	}
	
	/**
	 * Makes a call to the server to fetch the correspondence protocol properties
	 * @param sysPrefix
	 */
	protected void getProtocolProperties(String sysPrefix){
		grid.getStore().removeAll();
		CorrConstants.corrAdminService.getCorrProtocolProperties(sysPrefix, new AsyncCallback<ArrayList<CorrProtocolClient>>(){
			
			public void onFailure(Throwable caught) {
				TbitsInfo.error("Could not fetch Protocol Entries for specified BA");
				Log.error("Could not fetch protocol entries...", caught);
			}
			
			public void onSuccess(ArrayList<CorrProtocolClient> result) {
				if(result != null){
					TbitsInfo.info("Loading " + result.size() + " Properties... Please Wait...");
					populateGrid(result);
				}else
					TbitsInfo.info("Correspondence Protocol Entries not present for the specified BA");
			}
			
		});
	}
	
	/**
	 * Takes the list of correspondence protocol properties and populates 
	 * the grid store with it. Also lays out the content panel.
	 * @param result
	 */
	protected void populateGrid(ArrayList<CorrProtocolClient> result){
		savedProperties.addAll(result);
		grid.getStore().removeAll();
		store.add(result);		
		contentPanel.add(grid);
		this.add(contentPanel);
		this.layout();
	}
	
	
	/**
	 * Builds the top and bottom toolbars
	 */
	protected void buildToolbars(){
		buildTopToolbar();
		buildBottomToolbar();
	}
	
	/**
	 * Builds the bottom toolbar
	 */
	protected void buildBottomToolbar(){
		bottomBar = new ToolBar();
		
		//-----------------------------SAVE Button------------------------------------------------//
		ToolBarButton saveButton = new ToolBarButton("Save Settings", new SelectionListener<ButtonEvent>(){
			public void componentSelected(ButtonEvent ce){
				//-----------------First save all the values in the grid---------------------//
				savedProperties.clear();
				
				if(0 == store.getCount()){
					TbitsInfo.info("Cannot save empty table...");
					return;
				}
				
				for(CorrProtocolClient property : grid.getStore().getModels()){
					
					if((null == property.getProperty()) || property.getProperty().equals("")){
						TbitsInfo.info("Invalid value in 'Property' field... Please enter valid values");
						return;
					}
					
					if((null == property.getPropertyValue()) || property.getPropertyValue().equals("")){
						TbitsInfo.info("Invalid value in 'Value' field... Please enter valid values");
						return;
					}
					
					if((null == property.getDescription()) || property.getDescription().equals("")){
						TbitsInfo.info("Invalid value in 'Description' field... Please enter valid values");
						return;
					}
					
					savedProperties.add(property);
				}
				
				CorrConstants.corrAdminService.saveCorrProtocolProperties(savedProperties, new AsyncCallback<Integer>(){

					
					public void onFailure(Throwable caught) {
						TbitsInfo.error("Could not save correspondence protocol properties to db... Check logs for details....");
						Log.error("Error while saving protocol properties", caught);	
					}
					
					public void onSuccess(Integer result) {
						if(result == 1)
							TbitsInfo.info("Successfully saved " + result + " property into database...");
						else if(result > 0)
							TbitsInfo.info("Successfully saved " + result + " properties into database...");
						
						getProtocolProperties(currentBA.getSystemPrefix());
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
				
				CorrProtocolClient newClient = new CorrProtocolClient();
				newClient.setSysPrefix(currentBA.getSystemPrefix());
				newClient.setId("-1");
				newClient.setProperty("NULL");
				newClient.setPropertyValue("NULL");
				newClient.setDescription("NULL");
				
				grid.getStore().add(newClient);
			}
		});
		
		//-----------------------------DELETE Button------------------------------------------------//
		
		ToolBarButton deleteButton	= new ToolBarButton("Delete Property", new SelectionListener<ButtonEvent>(){
			public void componentSelected(ButtonEvent ce) {
				final List<CorrProtocolClient> propertiesList = grid.getSelectionModel().getSelectedItems();
				Listener<MessageBoxEvent> l = new Listener<MessageBoxEvent>(){					
					public void handleEvent(MessageBoxEvent be) {
						Button pressedButton = be.getButtonClicked();
						if(pressedButton.getText().endsWith("Yes")){						
							if(propertiesList != null){
								
								for(int i = 0 ; i < propertiesList.size(); i++){
									removeFromGrid(propertiesList.get(i));
								}
								
								CorrConstants.corrAdminService.deleteCorrProtocolProperties(propertiesList, new AsyncCallback<Integer>(){
									
									public void onFailure(Throwable caught) {
										TbitsInfo.error("Error... Could not delete entry from database...");
										Log.error("Unable to delete entry from database...", caught);
									}
									
									public void onSuccess(Integer count) {
										if(count == 1)
											TbitsInfo.info("Successfully deleted " + count + " property from database...");
										else if(count > 1)
											TbitsInfo.info("Successfully deleted " + count + " properties from database...");
										propertiesList.clear();
//										getProtocolProperties(currentBA.getSystemPrefix());
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
					else TbitsInfo.info("Select a property to delete...");;
				}
			}
		});
		
		bottomBar.add(saveButton);
		bottomBar.add(addButton);
		bottomBar.add(deleteButton);		
		contentPanel.setBottomComponent(bottomBar);
	}
	
	protected void removeFromGrid(CorrProtocolClient property){
		for(int i = 0 ;i < store.getCount(); i++){
			if(!property.getId().equals("-1")){
				if(store.getAt(i).getId().equals(property.getId())){
					store.remove(i);
					break;
				}
			}else{
				if(store.getAt(i).getProperty().equals(property.getProperty())
						&& store.getAt(i).getPropertyValue().equals(property.getPropertyValue())
							&& store.getAt(i).getDescription().equals(property.getDescription())){
					 		store.remove(i);
					 		break;
						}
			}
		}
	}
	
	/**
	 * Builds the top toolbar
	 */
	protected void buildTopToolbar(){
		
		topBar = new ToolBar();
		topBar.setWidth(150);		
		buildComboBox();
		topBar.add(baCombobox);
	
		contentPanel.setTopComponent(topBar);
	}
	
	/**
	 * Builds the combo box for displaying the list of current businessares
	 */
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
	 * Buils the grid in which the values will be shown.
	 */
	protected void buildGrid(){
		
		CheckBoxSelectionModel<CorrProtocolClient> checkBoxModel = new CheckBoxSelectionModel<CorrProtocolClient>();
		checkBoxModel.setSelectionMode(SelectionMode.MULTI);
		
		ColumnConfig id	= new ColumnConfig(CorrProtocolClient.CORR_PROT_ID, "ID", 50);
		id.setHidden(true);
		
		ColumnConfig sysPrefix = new ColumnConfig(CorrProtocolClient.SYS_PREFIX, "Sys Prefix", 100);
		ColumnConfig corrProt = new ColumnConfig(CorrProtocolClient.CORR_PROT, "Property", 300);
		ColumnConfig corrProtValue	= new ColumnConfig(CorrProtocolClient.CORR_PROT_VALUE, "Value", 300);
		ColumnConfig corrProtDesc = new ColumnConfig(CorrProtocolClient.CORR_PROT_DESC, "Description", 400);
		
		
		TextField<String> property = new TextField<String>();
		property.setAllowBlank(false);
		corrProt.setEditor(new TbitsCellEditor(property));
		
		TextField<String> propertyValue = new TextField<String>();
		propertyValue.setAllowBlank(false);
		corrProtValue.setEditor(new TbitsCellEditor(propertyValue));
		
		TextField<String> propertyDesc = new TextField<String>();
		propertyDesc.setAllowBlank(false);
		corrProtDesc.setEditor(new TbitsCellEditor(propertyDesc));
		
		ArrayList<ColumnConfig> clist = new ArrayList<ColumnConfig>();
		
		clist.add(checkBoxModel.getColumn());
		clist.add(id);
		clist.add(sysPrefix);
		clist.add(corrProt);
		clist.add(corrProtValue);
		clist.add(corrProtDesc);
		
		model = new ColumnModel(clist);		
		grid  = new EditorGrid<CorrProtocolClient>(store, model);
		grid.setSelectionModel(checkBoxModel);
		grid.addPlugin(checkBoxModel);
	}
}
