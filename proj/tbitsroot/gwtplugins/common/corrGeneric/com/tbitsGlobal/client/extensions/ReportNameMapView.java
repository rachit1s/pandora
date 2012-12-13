package corrGeneric.com.tbitsGlobal.client.extensions;

import java.util.ArrayList;
import java.util.List;

import com.axeiya.gwtckeditor.client.Toolbar;
import com.extjs.gxt.ui.client.Style.HorizontalAlignment;
import com.extjs.gxt.ui.client.Style.SelectionMode;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.FieldEvent;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.MessageBoxEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.store.Store;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.MessageBox;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.FormPanel;
import com.extjs.gxt.ui.client.widget.form.LabelField;
import com.extjs.gxt.ui.client.widget.form.StoreFilterField;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.extjs.gxt.ui.client.widget.grid.CellEditor;
import com.extjs.gxt.ui.client.widget.grid.CheckBoxSelectionModel;
import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
import com.extjs.gxt.ui.client.widget.grid.ColumnModel;
import com.extjs.gxt.ui.client.widget.grid.EditorGrid;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.toolbar.ToolBar;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.tbitsGlobal.admin.client.utils.LinkIdentifier;
import com.tbitsGlobal.admin.client.widgets.APTabItem;
import commons.com.tbitsGlobal.utils.client.TbitsInfo;
import commons.com.tbitsGlobal.utils.client.domainObjects.TypeClient;
import commons.com.tbitsGlobal.utils.client.log.Log;
import commons.com.tbitsGlobal.utils.client.widgets.ToolBarButton;

import corrGeneric.com.tbitsGlobal.client.CorrConstants;
import corrGeneric.com.tbitsGlobal.client.modelData.ReportMapClient;
import corrGeneric.com.tbitsGlobal.client.modelData.ReportNameClient;
import corrGeneric.com.tbitsGlobal.client.modelData.ReportParamsClient;

/**
 * Page for 'Report Name Map' tab under 'Correspondence'
 * Constructs and populates the grid after fetching report map properties
 * from corr_report_name_map table. Also handles addition/deletion/modification
 * of properties
 * @author devashish
 *
 */
public class ReportNameMapView extends APTabItem {

	protected ContentPanel contentPanel;
	protected ToolBar topBar;
	protected ToolBar bottomBar;
	protected EditorGrid<ReportNameClient> grid;
	protected ListStore<ReportNameClient> store;
	protected StoreFilterField<ReportNameClient> filter;
	protected ArrayList<ReportNameClient> savedProperties;
	protected ColumnModel model;
	
	public ReportNameMapView(LinkIdentifier linkId) {
		super(linkId);
		this.setBorders(false);
		this.setLayout(new FitLayout());
		this.setClosable(true);
		
		contentPanel = new ContentPanel(); 
		store = new ListStore<ReportNameClient>();
		savedProperties = new ArrayList<ReportNameClient>();
	}
	
	public void onRender(Element parent, int index){
		super.onRender(parent, index);
		build();
	}

	protected void build(){
		contentPanel.setLayout(new FitLayout());
		contentPanel.setHeaderVisible(false);
		buildGrid();
		buildTopToolbar();
		buildBottomToolbar();
		
		getData();
		
		contentPanel.add(grid);
		
		this.add(contentPanel);
		this.layout();
	}
	
	protected void getData(){
		CorrConstants.corrAdminService.getReportFileNameProperties(new AsyncCallback<ArrayList<ReportNameClient>>(){
			public void onFailure(Throwable caught) {
				TbitsInfo.error("Could not fetch report name map from database...");
				Log.error("Could not fetch report name map from database...", caught);
			}

			public void onSuccess(ArrayList<ReportNameClient> result) {
				if(null == result){
					TbitsInfo.info("Report Name Map currently empty...");
					return;
				}else populateGrid(result);
			}			
		});
	}
	
	protected void populateGrid(ArrayList<ReportNameClient> result){
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
				
				for(ReportNameClient property : grid.getStore().getModels()){
					
					if((null == property.getReportId()) || property.getReportId().equals("")
							|| property.getReportId().equals("-1")){
						TbitsInfo.error("Invalid value in 'Report Id' field... Please enter a valid value...");
						return;
					}
					
					if((null == property.getReportFileName()) || property.getReportFileName().equals("")
							|| property.getReportFileName().equals("NULL")){
						TbitsInfo.error("Invalid value in 'Report File Name' field... Please enter a valid value...");
						return;
					}
					
					savedProperties.add(property);
				}			
				CorrConstants.corrAdminService.saveReportFileNameProperties(savedProperties, new AsyncCallback<Integer>(){

					public void onFailure(Throwable caught) {
						TbitsInfo.error("Could not save report file name properties to database... See logs for more information");
						Log.error("Could not save report file name properties to database... See logs for more information", caught);
					}

					public void onSuccess(Integer result) {
						TbitsInfo.info("Successfully saved " + result + " properties into database...");
						getData();
					}
				});
			}
		});
		//-----------------------------ADD Button------------------------------------------------//
		ToolBarButton addButton		= new ToolBarButton("Add New Property", new SelectionListener<ButtonEvent>(){
			public void componentSelected(ButtonEvent ce){
				ReportNameClient reportName = new ReportNameClient();
				reportName.setId("-1");
				reportName.setReportId("-1");
				reportName.setReportFileName("NULL");
				
				grid.getStore().add(reportName);
			}
		});	
		//-----------------------------DELETE Button------------------------------------------------//
		
		ToolBarButton deleteButton	= new ToolBarButton("Delete Property", new SelectionListener<ButtonEvent>(){
			public void componentSelected(ButtonEvent ce) {
				final List<ReportNameClient> propertiesList = grid.getSelectionModel().getSelectedItems();
				Listener<MessageBoxEvent> l = new Listener<MessageBoxEvent>(){					
					public void handleEvent(MessageBoxEvent be) {
						Button pressedButton = be.getButtonClicked();
						if(pressedButton.getText().endsWith("Yes")){						
							if(propertiesList != null){
								
								for(int i = 0 ; i < propertiesList.size(); i++){
									removeFromGrid(propertiesList.get(i));
								}
								
								CorrConstants.corrAdminService.deleteReportFileNameProperties(propertiesList, new AsyncCallback<Integer>(){

									public void onFailure(Throwable caught) {
										TbitsInfo.error("Could not delete report file name properties from database... See logs for more information");
										Log.error("Could not delete report file name properties from database... See logs for more information", caught);
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
					TbitsInfo.info("select a property to delete...");
				}
			}
		});
		
		bottomBar.add(saveButton);
		bottomBar.add(addButton);
		bottomBar.add(deleteButton);		
		contentPanel.setBottomComponent(bottomBar);
	}
	
	protected void removeFromGrid(ReportNameClient property){
		for(int i = 0 ;i < store.getCount(); i++){
			if(!property.getId().equals("-1")){
				if(store.getAt(i).getId().equals(property.getId())){
					store.remove(i);
					break;
				}
			}else{
				if(store.getAt(i).getReportId().equals(property.getReportId())
						&& store.getAt(i).getReportFileName().equals(property.getReportFileName())){
					 		store.remove(i);
					 		break;
						}
			}
		}
	}
	
	protected void buildTopToolbar(){
		topBar = new ToolBar();
		applySearchFilter();
		
		LabelField filterLabel = new LabelField("Search : ");
		
		topBar.add(filterLabel);
		topBar.add(filter);
		
		contentPanel.setTopComponent(topBar);
	}
	
	/**
	 * Apply search filter to the search box provided in the top toolbar
	 */
	protected void applySearchFilter(){
		filter = new StoreFilterField<ReportNameClient>(){
			
			protected boolean doSelect(Store<ReportNameClient> store, ReportNameClient parent, ReportNameClient record,
					String property, String filter) {
				
				String reportFileName = record.getReportFileName();
				reportFileName = reportFileName.toLowerCase();
				if(reportFileName.contains(filter.toLowerCase()))
					return true;
				return false;
			}
		};
		filter.bind(grid.getStore());
		filter.setEmptyText(" Search ");
	}
	
	protected void buildGrid(){
		CheckBoxSelectionModel<ReportNameClient> checkBoxModel = new CheckBoxSelectionModel<ReportNameClient>();
		checkBoxModel.setSelectionMode(SelectionMode.MULTI);
		
		ColumnConfig id = new ColumnConfig(ReportNameClient.ID, "ID", 50);
		id.setHidden(true);
		
		ColumnConfig reportId = new ColumnConfig(ReportNameClient.REPORT_ID, "Report ID", 100);
		reportId.setAlignment(HorizontalAlignment.CENTER);
		
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
		
		ColumnConfig reportFileName = new ColumnConfig(ReportNameClient.REPORT_FILE_NAME, "Report File Name", 200);
		
		TextField<String> reportFileNameField = new TextField<String>();
		reportFileNameField.setAllowBlank(false);
		reportFileName.setEditor(new CellEditor(reportFileNameField));
		
		ArrayList<ColumnConfig> clist = new ArrayList<ColumnConfig>();
		clist.add(checkBoxModel.getColumn());
		clist.add(id);
		clist.add(reportId);
		clist.add(reportFileName);
		
		model = new ColumnModel(clist);
		grid = new EditorGrid<ReportNameClient>(store, model);
		grid.setSelectionModel(checkBoxModel);
		grid.addPlugin(checkBoxModel);
	}
}
