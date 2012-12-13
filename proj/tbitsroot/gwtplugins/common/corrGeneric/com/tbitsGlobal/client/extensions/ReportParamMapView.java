package corrGeneric.com.tbitsGlobal.client.extensions;

import java.util.ArrayList;
import java.util.List;

import com.extjs.gxt.ui.client.Style.HorizontalAlignment;
import com.extjs.gxt.ui.client.Style.SelectionMode;
import com.extjs.gxt.ui.client.data.ModelData;
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
import com.extjs.gxt.ui.client.widget.form.ComboBox.TriggerAction;
import com.extjs.gxt.ui.client.widget.form.LabelField;
import com.extjs.gxt.ui.client.widget.form.SimpleComboBox;
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
import commons.com.tbitsGlobal.utils.client.log.Log;
import commons.com.tbitsGlobal.utils.client.widgets.ToolBarButton;

import corrGeneric.com.tbitsGlobal.client.CorrConstants;
import corrGeneric.com.tbitsGlobal.client.modelData.ReportParamsClient;
import corrGeneric.com.tbitsGlobal.shared.objects.GenericParams;

/**
 * 
 * @author devashish
 *
 */
public class ReportParamMapView extends APTabItem {

	protected ContentPanel contentPanel;
	protected ToolBar bottomBar;
	protected ToolBar topBar;
	
	protected EditorGrid<ReportParamsClient> grid;
	protected ListStore<ReportParamsClient> store;
	protected StoreFilterField<ReportParamsClient> filter;
	protected ColumnModel	model;
	
	protected ArrayList<ReportParamsClient> savedProperties;
	
	public ReportParamMapView(LinkIdentifier linkId) {
		super(linkId);
		this.setClosable(true);
		this.setLayout(new FitLayout());
		
		store = new ListStore<ReportParamsClient>();
		contentPanel = new ContentPanel();
		savedProperties = new ArrayList<ReportParamsClient>();
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
		
		this.add(contentPanel);
		this.layout();
	}
	
	protected void getData(){
		CorrConstants.corrAdminService.getReportParamProperties(new AsyncCallback<ArrayList<ReportParamsClient>>(){

			public void onFailure(Throwable caught) {
				TbitsInfo.error("Could not fetch properties from database... ");
				Log.error("Could not fetch properties from database...", caught);
			}

			public void onSuccess(ArrayList<ReportParamsClient> result) {
				populateGrid(result);
			}
			
		});
	}
	
	protected void populateGrid(ArrayList<ReportParamsClient> result){
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
				for(ReportParamsClient property : grid.getStore().getModels()){
					
					if((null == property.getReportId()) || property.getReportId().equals("-1") 
							|| property.getReportId().equals("")){
						TbitsInfo.error("Invalid value in 'Report ID' field... Please enter a valid value...");
						return;
					}
					
					if((null == property.getParamName()) || property.getParamName().equals("")
							|| property.getParamName().equals("NULL")){
						TbitsInfo.error("Invalid value in 'Param Name' field... Please enter a valid value...");
						return;
					}
					
					if((null == property.getParamValue()) || property.getParamValue().equals("")
							|| property.getParamValue().equals("NULL")){
						TbitsInfo.error("Invalid value in 'Param Value' field... Please enter a valid value...");
						return;
					}
					
					if((null == property.getParamValueType()) || property.getParamValueType().equals("") 
							|| property.getParamValueType().equals("NULL")){
						TbitsInfo.error("Invalid value in 'Param Value Type' field... Please enter a valid value...");
						return;
					}

					savedProperties.add(property);
				}		
				
				CorrConstants.corrAdminService.saveReportParamProperties(savedProperties, new AsyncCallback<Integer>(){

					public void onFailure(Throwable caught) {
						TbitsInfo.error("Could not save report params to database... See logs for more information");
						Log.error("Could not save report params to database", caught);
					}

					public void onSuccess(Integer result) {
						if(result == 1)
							TbitsInfo.info("Successfully saved 1 property to database...");
						else if(result > 1)
							TbitsInfo.info("Successfully saved " + result + " properties to database...");
						
						getData();
					}
					
				});
			}
		});
		//-----------------------------ADD Button------------------------------------------------//
		ToolBarButton addButton		= new ToolBarButton("Add New Property", new SelectionListener<ButtonEvent>(){
			public void componentSelected(ButtonEvent ce){
				
				ReportParamsClient newParamsClient = new ReportParamsClient();
				newParamsClient.setId("-1");
				newParamsClient.setReportId("-1");
				newParamsClient.setParamName("NULL");
				newParamsClient.setParamType("NULL");
				newParamsClient.setParamValue("NULL");
				newParamsClient.setParamValueType("NULL");
				
				grid.getStore().add(newParamsClient);
			}
		});	
		//-----------------------------DELETE Button------------------------------------------------//
		
		ToolBarButton deleteButton	= new ToolBarButton("Delete Property", new SelectionListener<ButtonEvent>(){
			public void componentSelected(ButtonEvent ce) {
				final List<ReportParamsClient> propertiesList = grid.getSelectionModel().getSelectedItems();
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
								
								CorrConstants.corrAdminService.deleteReportParamProperties(propertiesList, new AsyncCallback<Integer>(){
									public void onFailure(Throwable caught) {
										TbitsInfo.error("Could not delete report params to database... See logs for more information");
										Log.error("Could not delete report params to database", caught);
									}
									public void onSuccess(Integer result) {
										if(result == 1)
											TbitsInfo.info("Successfully deleted 1 property to database...");
										else if(result > 1)
											TbitsInfo.info("Successfully deleted " + result + " properties to database...");
										
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
	
	protected void removeFromGrid(ReportParamsClient property){
		for(int i = 0 ;i < store.getCount(); i++){
			if(!property.getId().equals("-1")){
				if(store.getAt(i).getId().equals(property.getId())){
					store.remove(i);
				}
			}else{
				if(store.getAt(i).getParamType().equals(property.getParamType())
						&& store.getAt(i).getParamName().equals(property.getParamName())
							&& store.getAt(i).getParamValueType().equals(property.getParamValueType())
								&& store.getAt(i).getParamValue().equals(property.getParamValue())){
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
		filter = new StoreFilterField<ReportParamsClient>(){
			
			protected boolean doSelect(Store<ReportParamsClient> store, ReportParamsClient parent, ReportParamsClient record,
					String property, String filter) {
				
				String paramName = record.getParamName();
				paramName = paramName.toLowerCase();
				
				String paramValue = record.getParamValue();
				paramValue = paramValue.toLowerCase();
				
				if(paramName.contains(filter.toLowerCase()) || paramValue.contains(filter.toLowerCase()))
					return true;
				return false;
			}
		};
		filter.bind(grid.getStore());
		filter.setEmptyText(" Search ");
	}
	
	protected void buildGrid(){
		CheckBoxSelectionModel<ReportParamsClient> checkBoxModel = new CheckBoxSelectionModel<ReportParamsClient>();
		checkBoxModel.setSelectionMode(SelectionMode.MULTI);
		
		ColumnConfig id	= new ColumnConfig(ReportParamsClient.ID, "ID", 50);
		id.setHidden(true);
		
		ColumnConfig reportId = new ColumnConfig(ReportParamsClient.REPORT_ID, "Report ID", 100);
		ColumnConfig paramType = new ColumnConfig(ReportParamsClient.PARAM_TYPE, "Param Type", 160);
		ColumnConfig paramName = new ColumnConfig(ReportParamsClient.PARAM_NAME, "Param Name", 160);
		ColumnConfig paramValueType = new ColumnConfig(ReportParamsClient.PARAM_VALUE_TYPE, "Param Value Type", 160);
		ColumnConfig paramValue = new ColumnConfig(ReportParamsClient.PARAM_VALUE, "Param Value", 220);
		
		
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
		
		
		
		
	/*	final TextField<String> reportIdField = new TextField<String>();
		reportIdField.setAllowBlank(false);
		reportIdField.sinkEvents(Event.ONCHANGE);
		reportId.setSortable(false);
		reportId.setAlignment(HorizontalAlignment.CENTER);
		reportId.setEditor(new CellEditor(reportIdField){
			@Override
			public Object postProcessValue(Object value) {
				try{
					if(value instanceof String)
						return Integer.parseInt((String) value);
					return (Integer)value;
				}catch(Exception e){
					return 0;
				}
			}
			
			@Override
			public Object preProcessValue(Object value) {
				if(value instanceof Integer){
					return (Integer)value + "";
				}
				return super.preProcessValue(value);
			}
		});*/
		
		final SimpleComboBox<String> paramTypeCombo = new SimpleComboBox<String>();
		paramTypeCombo.setForceSelection(true);  
		paramTypeCombo.setTriggerAction(TriggerAction.ALL); 
		paramTypeCombo.setEmptyText("Select Type");
		paramTypeCombo.add(GenericParams.ParamType_Variable);
		paramTypeCombo.add(GenericParams.ParamType_ReportParameter);
		CellEditor typeEditor = new CellEditor(paramTypeCombo){
			 public Object preProcessValue(Object value) {  
			        if (value == null) {  
			          return value;  
			        }  
			        return paramTypeCombo.findModel(value.toString());  
			      }  
			  
			      @Override  
			      public Object postProcessValue(Object value) {  
			        if (value == null) {  
			          return value;  
			        }  
			        return ((ModelData) value).get("value");  
			      } 
		};
		
		paramType.setEditor(typeEditor);
			
		TextField<String> paramNameField = new TextField<String>();
		paramNameField.setAllowBlank(false);
		paramName.setEditor(new CellEditor(paramNameField));
		
		final SimpleComboBox<String> paramValueTypeCombo = new SimpleComboBox<String>();
		paramValueTypeCombo.setForceSelection(true);  
		paramValueTypeCombo.setTriggerAction(TriggerAction.ALL); 
		paramValueTypeCombo.setEmptyText("Select Value Type");
		paramValueTypeCombo.add(GenericParams.ParamValueType_Const);
		paramValueTypeCombo.add(GenericParams.ParamValueType_JavaClass);
		paramValueTypeCombo.add(GenericParams.ParamValueType_JavaObject);
		
		CellEditor paramValueTypeEditor = new CellEditor(paramValueTypeCombo){
			 public Object preProcessValue(Object value) {  
			        if (value == null) {  
			          return value;  
			        }  
			        return paramValueTypeCombo.findModel(value.toString());  
			      }  
			  
			      @Override  
			      public Object postProcessValue(Object value) {  
			        if (value == null) {  
			          return value;  
			        }  
			        return ((ModelData) value).get("value");  
			      } 
		};
		paramValueType.setEditor(paramValueTypeEditor);
		
		TextField<String> paramValueField = new TextField<String>();
		paramValueField.setAllowBlank(false);
		paramValue.setEditor(new CellEditor(paramValueField));
		
		ArrayList<ColumnConfig> clist = new ArrayList<ColumnConfig>();
		clist.add(checkBoxModel.getColumn());
		clist.add(id);
		clist.add(reportId);
		clist.add(paramType);
		clist.add(paramName);
		clist.add(paramValueType);
		clist.add(paramValue);
		
		model = new ColumnModel(clist);
		grid = new EditorGrid<ReportParamsClient>(store, model);
		grid.setSelectionModel(checkBoxModel);
		grid.addPlugin(checkBoxModel);
	}

}
