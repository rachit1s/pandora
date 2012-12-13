package corrGeneric.com.tbitsGlobal.client.extensions;

import java.util.ArrayList;
import java.util.List;

import com.extjs.gxt.ui.client.Style.SelectionMode;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.MessageBoxEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.store.Store;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.MessageBox;
import com.extjs.gxt.ui.client.widget.Window;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.FormPanel;
import com.extjs.gxt.ui.client.widget.form.LabelField;
import com.extjs.gxt.ui.client.widget.form.StoreFilterField;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.extjs.gxt.ui.client.widget.grid.CheckBoxSelectionModel;
import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
import com.extjs.gxt.ui.client.widget.grid.ColumnModel;
import com.extjs.gxt.ui.client.widget.grid.EditorGrid;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.layout.FormLayout;
import com.extjs.gxt.ui.client.widget.toolbar.ToolBar;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.tbitsGlobal.admin.client.utils.LinkIdentifier;
import com.tbitsGlobal.admin.client.widgets.APTabItem;
import commons.com.tbitsGlobal.utils.client.TbitsCellEditor;
import commons.com.tbitsGlobal.utils.client.TbitsInfo;
import commons.com.tbitsGlobal.utils.client.log.Log;
import commons.com.tbitsGlobal.utils.client.widgets.ToolBarButton;

import corrGeneric.com.tbitsGlobal.client.CorrConstants;
import corrGeneric.com.tbitsGlobal.client.modelData.CorrPropertiesClient;
import corrGeneric.com.tbitsGlobal.client.modelData.CorrProtocolClient;
import corrGeneric.com.tbitsGlobal.client.modelData.ReportNameClient;
/**
 * Page for the 'Properties' tab under 'Correspondence'
 * Constructs and populates the grid after fetching correpondence properties from the 
 * table corr_properties. Also handles the addition/deletion/modification of properties 
 * from the same table. 
 * @author devashish
 *
 */
public class CorrPropertiesView extends APTabItem {	
	protected ContentPanel 	contentPanel;
	protected ToolBar top;
	protected ToolBar bottom;
	
	protected List<CorrPropertiesClient> 		savedProperties;
	protected ListStore<CorrPropertiesClient> 	store;
	protected EditorGrid<CorrPropertiesClient> 	grid;
	protected StoreFilterField<CorrPropertiesClient> filter;
	protected ColumnModel							model;
	
	
	/**
	 * Constructor
	 */
	public CorrPropertiesView(LinkIdentifier linkId) {
		super(linkId);
		this.setBorders(false);
		this.setLayout(new FitLayout());
		this.setClosable(true);
		
		savedProperties = new ArrayList<CorrPropertiesClient>();
		contentPanel	= new ContentPanel();
		store			= new ListStore<CorrPropertiesClient>();
	}
	
	public void onRender(Element parent, int index){
		super.onRender(parent, index);
		build();
	}
	
	/**
	 * Build the content panel and the grid in which the information will be shown
	 */
	protected void build(){
	
		contentPanel.setLayout(new FitLayout());
		contentPanel.setHeaderVisible(false);
		
		CheckBoxSelectionModel<CorrPropertiesClient> checkBoxModel = new CheckBoxSelectionModel<CorrPropertiesClient>();
		checkBoxModel.setSelectionMode(SelectionMode.MULTI);
		
		ColumnConfig id	= new ColumnConfig(CorrPropertiesClient.CORR_PROP_ID, "ID", 50);
		id.setHidden(true);
		
		ColumnConfig corrProp = new ColumnConfig(CorrPropertiesClient.CORR_PROP, "Property", 300);
		ColumnConfig corrPropValue	= new ColumnConfig(CorrPropertiesClient.CORR_PROP_VALUE, "Value", 300);
		ColumnConfig corrPropDesc = new ColumnConfig(CorrPropertiesClient.CORR_PROP_DESC, "Description", 400);
		
		
		TextField<String> property = new TextField<String>();
		property.setAllowBlank(false);
		corrProp.setEditor(new TbitsCellEditor(property));
		
		TextField<String> propertyValue = new TextField<String>();
		propertyValue.setAllowBlank(false);
		corrPropValue.setEditor(new TbitsCellEditor(propertyValue));
		
		TextField<String> propertyDesc = new TextField<String>();
		propertyDesc.setAllowBlank(false);
		corrPropDesc.setEditor(new TbitsCellEditor(propertyDesc));
		
		ArrayList<ColumnConfig> clist = new ArrayList<ColumnConfig>();
		clist.add(id);
		clist.add(checkBoxModel.getColumn());
		clist.add(corrProp);
		clist.add(corrPropValue);
		clist.add(corrPropDesc);
		
		model = new ColumnModel(clist);		
		grid  = new EditorGrid<CorrPropertiesClient>(store, model);
		grid.setSelectionModel(checkBoxModel);
		grid.addPlugin(checkBoxModel);
		
		
		buildToolbars();	
		getData();

	}
	
	protected void buildToolbars(){
		buildTopToolbar();
		buildBottomToolbar();
	}
	
	/**
	 * Builds the toolbar at the bottom of the grid. This is used to save/update/load 
	 * the properties value.
	 */
	protected void buildBottomToolbar(){
		bottom = new ToolBar();
		
		//-----------------------------SAVE Button------------------------------------------------//
		ToolBarButton saveButton = new ToolBarButton("Save Settings", new SelectionListener<ButtonEvent>(){
			public void componentSelected(ButtonEvent ce){
				//-----------------First save all the values in the grid---------------------//
				savedProperties.clear();
				for(CorrPropertiesClient property : grid.getStore().getModels()){
					
					CorrPropertiesClient temp = new CorrPropertiesClient();
					
					temp.setId(property.getId());
					temp.setProperty(property.getProperty());
					temp.setPropertyValue(property.getPropertyValue());
					temp.setDescription(property.getDescription());
					
					savedProperties.add(temp);
				}
				
				CorrConstants.corrAdminService.saveCorrProperties(savedProperties ,new AsyncCallback<Integer>(){
					
					public void onFailure(Throwable caught) {
						TbitsInfo.error("Unable to save properties into database... see logs for more information...");						
						Log.error("Unable to save properties into database...", caught);
					}
					
					public void onSuccess(Integer propertyCount) {
						TbitsInfo.info("Successfully saved " + propertyCount + " properties into database...");
						//-------------------Re-Initialize the grid---------------------//
						getData();
					}
				});
			}
		});
		//-----------------------------ADD Button------------------------------------------------//
		ToolBarButton addButton		= new ToolBarButton("Add New Property", new SelectionListener<ButtonEvent>(){
			public void componentSelected(ButtonEvent ce){
				
				CorrPropertiesClient newClient = new CorrPropertiesClient();
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
				final List<CorrPropertiesClient> propertiesList = grid.getSelectionModel().getSelectedItems();
				Listener<MessageBoxEvent> l = new Listener<MessageBoxEvent>(){					
					public void handleEvent(MessageBoxEvent be) {
						Button pressedButton = be.getButtonClicked();
						if(pressedButton.getText().endsWith("Yes")){						
							if(propertiesList != null){
								
								for(int i = 0 ; i < propertiesList.size(); i++){
									removeFromGrid(propertiesList.get(i));
								}
								
								CorrConstants.corrAdminService.deleteCorrProperty(propertiesList, new AsyncCallback<Integer>(){
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
										//-------------------Re-Initialize the grid---------------------//
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
					TbitsInfo.info("select a property to delete...");
				}
			}
		});
		
		bottom.add(saveButton);
		bottom.add(addButton);
		bottom.add(deleteButton);		
		contentPanel.setBottomComponent(bottom);
	}
	
	protected void removeFromGrid(CorrPropertiesClient property){
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
	 * Builds the toolbar at the top of the grid which consists of the search panel
	 */
	protected void buildTopToolbar(){
		applySearchFilter();
		
		LabelField filterLabel = new LabelField("Search:");
		top = new ToolBar();
		top.add(filterLabel);
		top.add(filter);
		contentPanel.setTopComponent(top);
	}
	
	/**
	 * Apply the filter to the search field.
	 * This searches all the entries in the grid
	 */
	protected void applySearchFilter(){
			filter = new StoreFilterField<CorrPropertiesClient>(){
			
			protected boolean doSelect(Store<CorrPropertiesClient> store,CorrPropertiesClient parent, CorrPropertiesClient record,String property, String filter) {
				String corrProperty = record.get(CorrPropertiesClient.CORR_PROP);
				corrProperty = corrProperty.toLowerCase();
				
				String corrPropertyValue = record.get(CorrPropertiesClient.CORR_PROP_VALUE);
				corrPropertyValue = corrPropertyValue.toLowerCase();
				
				String description = record.get(CorrPropertiesClient.CORR_PROP_DESC);
				description = description.toLowerCase();
				
				if (corrProperty.contains(filter.toLowerCase()) || corrPropertyValue.contains(filter.toLowerCase()) 
							|| description.contains(filter.toLowerCase())) {  
					return true;  
				}
				return false;
			}
		};
		
		filter.bind(grid.getStore());
		filter.setEmptyText("Search Properties");
	}
	
	/**
	 * Does the RPC call to get the correspondence properties from server
	 */
	protected void getData(){
		CorrConstants.corrAdminService.getCorrProperties(new AsyncCallback<ArrayList<CorrPropertiesClient>>(){

			
			public void onFailure(Throwable caught) {
				TbitsInfo.error("Error in getting correspondence properties... Please Refresh ...", caught);
				Log.error("Unable to fetch correspondence properties... ", caught);
			}
			
			public void onSuccess(ArrayList<CorrPropertiesClient> result) {
				populateGrid(result);
			}	
		});
	}
	
	/**
	 * This is called after successful call to server for fetching
	 * correspondence properties
	 * @param list of correspondence properties
	 */
	protected void populateGrid(ArrayList<CorrPropertiesClient> result){
		savedProperties.addAll(result);		
		grid.getStore().removeAll();
		store.add(result);		
		contentPanel.add(grid);
		this.add(contentPanel);
		this.layout();
	}
}
