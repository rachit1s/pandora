package corrGeneric.com.tbitsGlobal.client.extensions;


import java.util.ArrayList;

import com.extjs.gxt.ui.client.Style.HorizontalAlignment;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.FieldEvent;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.SelectionChangedEvent;
import com.extjs.gxt.ui.client.event.SelectionChangedListener;
import com.extjs.gxt.ui.client.store.GroupingStore;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.form.ComboBox;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.extjs.gxt.ui.client.widget.form.ComboBox.TriggerAction;
import com.extjs.gxt.ui.client.widget.grid.CellEditor;
import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
import com.extjs.gxt.ui.client.widget.grid.ColumnData;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.extjs.gxt.ui.client.widget.grid.GridCellRenderer;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.rpc.AsyncCallback;

import commons.com.tbitsGlobal.utils.client.TbitsInfo;
import commons.com.tbitsGlobal.utils.client.bafield.BAField;
import commons.com.tbitsGlobal.utils.client.bulkupdate.BulkUpdateGridAbstract;
import commons.com.tbitsGlobal.utils.client.domainObjects.BusinessAreaClient;
import commons.com.tbitsGlobal.utils.client.domainObjects.FieldClient;
import commons.com.tbitsGlobal.utils.client.domainObjects.TypeClient;
import commons.com.tbitsGlobal.utils.client.domainObjects.UserClient;
import commons.com.tbitsGlobal.utils.client.log.Log;
import corrGeneric.com.tbitsGlobal.client.CorrAdminUtils;
import corrGeneric.com.tbitsGlobal.client.CorrConstants;
import corrGeneric.com.tbitsGlobal.client.modelData.UserMapClient;
import corrGeneric.com.tbitsGlobal.client.modelData.UserMapTypeClient;
import corrGeneric.com.tbitsGlobal.shared.objects.GenericParams;

/**
 * Grid to hold the user map columns
 * @author devashish
 *
 */
public class UserMapGrid extends BulkUpdateGridAbstract<UserMapClient> {

	private BusinessAreaClient currentBa;
	
	private ListStore<TypeClient> type1Store;
	private ListStore<TypeClient> type2Store;
	private ListStore<TypeClient> type3Store;
	private ListStore<FieldClient> typeFieldStore;;
	protected UserMapTypeClient	reportTypesList;
	protected ListStore<UserClient> userListStore;
	
	private GridCellRenderer<UserMapClient> userTypeRenderer = new GridCellRenderer<UserMapClient>(){
		public String render(UserMapClient model, String property, ColumnData config, int rowIndex, int colIndex,
				ListStore<UserMapClient> store, Grid<UserMapClient> grid) {
			
			if(property.equals(UserMapClient.USER_MAP_TYPE1)){
				if(null == model.getType1()){
					TbitsInfo.error("Please select a valid report Type");
					return "NULL";
				}				
				return model.getType1().getDisplayName();
			}else if(property.equals(UserMapClient.USER_MAP_TYPE2)){
				if(null == model.getType2()){
					TbitsInfo.error("Please select a valid report Type");
					return "NULL";
				}				
				return model.getType2().getDisplayName();
			}else if(property.equals(UserMapClient.USER_MAP_TYPE3)){
				if(null == model.getType3()){
					TbitsInfo.error("Please select a valid report Type");
					return "NULL";
				}				
				return model.getType3().getDisplayName();
			}
			return null;
		}		
	};
	
	/**
	 * Default constructor
	 * @param mode
	 */
	public UserMapGrid(BulkGridMode mode) {
		super(mode);
		showNumberer = false;
		showStatus	 = false;
		canRemoveRow = false;
		
		type1Store 		= new ListStore<TypeClient>();
		type2Store 		= new ListStore<TypeClient>();
		type3Store 		= new ListStore<TypeClient>();
		typeFieldStore 	= new ListStore<FieldClient>();
		reportTypesList = new UserMapTypeClient();
		userListStore	= new GroupingStore<UserClient>();
		
	}
	
	/**
	 * Fill the stores of each combobox with the fetched data
	 */
	protected void populateComboBoxes(){
		type1Store.removeAll();
		type1Store.add(reportTypesList.getUserMapTypeList(GenericParams.UserMapType1));
		
		type2Store.removeAll();
		type2Store.add(reportTypesList.getUserMapTypeList(GenericParams.UserMapType2));
		
		type3Store.removeAll();
		type3Store.add(reportTypesList.getUserMapTypeList(GenericParams.UserMapType3));
		
		typeFieldStore.removeAll();
		CorrConstants.corrAdminService.getFields(currentBa.getSystemPrefix(), new AsyncCallback<ArrayList<FieldClient>>(){
			public void onFailure(Throwable caught) {
				TbitsInfo.error("Could not fetch Fields list for selected Business Area... See logs for more information...", caught);
				Log.error("Could not fetch fields list for selected Business Areas...", caught);
			}

			public void onSuccess(ArrayList<FieldClient> result) {
				if(null != result){
					typeFieldStore.add(result);
					Log.info("Successfully fetched Fields List...");
				}
			}
		});
		
		userListStore.removeAll();
		userListStore.add(CorrAdminUtils.getUsersList());
		
	}
	
	/**
	 * Get user map types
	 */
	protected void getUserMapTypes(){
		CorrConstants.corrAdminService.getUserMapTypes(currentBa.getSystemPrefix(), new AsyncCallback<UserMapTypeClient>(){

			public void onFailure(Throwable caught) {
				TbitsInfo.error("Could not fetch User Map Types from database... Please see logs for more information...", caught);
				Log.error("Could not fetch User Map Types from database...", caught);
			}

			public void onSuccess(UserMapTypeClient result) {
				if(null != result){
					reportTypesList = result;
					Log.info("Successfully fetched User Map Types from database...");
				}else{
					TbitsInfo.info("No Type fields configured for current Business Area and User...");
					Log.info("No Type fields configured for current Business Area and User...");
				}
			}
		});
	}

	protected void createColumns() {
		ColumnConfig sysPrefix = new ColumnConfig(UserMapClient.SYS_PREFIX, 100);
		sysPrefix.setHeader("Sys Prefix");
		
		ColumnConfig type1Config = new ColumnConfig(UserMapClient.USER_MAP_TYPE1, 150);
		type1Config.setHeader("User Map Type 1");
		ComboBox<TypeClient> type1Combo = new ComboBox<TypeClient>();		
		type1Config.setEditor(buildComboBox(type1Combo, type1Store));
		type1Config.setRenderer(userTypeRenderer);
		type1Config.setSortable(false);
		cm.getColumns().add(type1Config);
		
		ColumnConfig type2Config = new ColumnConfig(UserMapClient.USER_MAP_TYPE2, 150);
		type2Config.setHeader("User Map Type 2");
		ComboBox<TypeClient> type2Combo = new ComboBox<TypeClient>();
		type2Config.setEditor(buildComboBox(type2Combo, type2Store));
		type2Config.setRenderer(userTypeRenderer);
		type2Config.setSortable(false);
		cm.getColumns().add(type2Config);
		
		ColumnConfig type3Config = new ColumnConfig(UserMapClient.USER_MAP_TYPE3, 150);
		type3Config.setHeader("User Map Type 3");
		ComboBox<TypeClient> type3Combo = new ComboBox<TypeClient>();
		type3Config.setEditor(buildComboBox(type3Combo, type3Store));
		type3Config.setRenderer(userTypeRenderer);
		type3Config.setSortable(false);
		cm.getColumns().add(type3Config);
		
		
		ColumnConfig typeFieldName = new ColumnConfig(UserMapClient.USER_TYPE_FIELD, 150);
		typeFieldName.setHeader("User Type Field Name");
		ComboBox<FieldClient> typeFieldCombobox	 = new ComboBox<FieldClient>();
		typeFieldCombobox.setTriggerAction(TriggerAction.ALL);
		typeFieldCombobox.setForceSelection(false);
		typeFieldCombobox.setStore(typeFieldStore);
		typeFieldCombobox.setDisplayField(FieldClient.NAME);	
		CellEditor typeFieldEditor = new CellEditor(typeFieldCombobox);	
		typeFieldName.setEditor(typeFieldEditor);
		GridCellRenderer<UserMapClient> typeFieldRenderer = new GridCellRenderer<UserMapClient>(){
			
			public String render(UserMapClient model, String property, ColumnData config, int rowIndex, int colIndex,
					ListStore<UserMapClient> store, Grid<UserMapClient> grid) {
				
				if(null == model.getUserTypeField()){
					TbitsInfo.error("Please select a valid user type field");
					return "NULL";
				}				
				return model.getUserTypeField().getName();
			}		
		};
		typeFieldName.setRenderer(typeFieldRenderer);
		typeFieldName.setSortable(false);
		cm.getColumns().add(typeFieldName);
		
		
//		ColumnConfig userLoginValue = new ColumnConfig(UserMapClient.USER_LOGIN, 150);
//		userLoginValue.setHeader("User Login Value");
//		
//		final ComboBox<UserClient> userComboBox = CorrAdminUtils.getUserCombo();
//		userComboBox.setEmptyText("Choose a user login");
//		userComboBox.addSelectionChangedListener(new SelectionChangedListener<UserClient>(){
//			public void selectionChanged(SelectionChangedEvent<UserClient> se){
//				if(null == currentBa){
//					TbitsInfo.info("Please select a valid BA before selecting the user...");
//					return;
//				}
//				UserClient currentUser = se.getSelectedItem();
//				userComboBox.setValue(currentUser);
//			}
//		});
//		userLoginValue.setEditor(new CellEditor(userComboBox));
//		
//		GridCellRenderer<UserMapClient> onBehalfUserRenderer = new GridCellRenderer<UserMapClient>(){
//			
//			public String render(UserMapClient model, String property, ColumnData config, int rowIndex, int colIndex,
//					ListStore<UserMapClient> store, Grid<UserMapClient> grid) {
//				
//				if(null == model.getUserLoginValue()){
//					TbitsInfo.error("Please select a valid user login value");
//					return "NULL";
//				}				
//				return model.getUserLoginValue().getUserLogin();
//			}		
//		};
//		userLoginValue.setRenderer(onBehalfUserRenderer);
//		userLoginValue.setSortable(false);
//		cm.getColumns().add(userLoginValue);
		
		
		ColumnConfig  onBehalfUser = new ColumnConfig(UserMapClient.USER_LOGIN, "User Login Value", 200);
		ComboBox<UserClient> onBehalfUserCombo = new ComboBox<UserClient>();
		onBehalfUserCombo = new ComboBox<UserClient>();
		onBehalfUserCombo.setTriggerAction(TriggerAction.ALL);
		onBehalfUserCombo.setForceSelection(false);
		onBehalfUserCombo.setStore(userListStore);
		onBehalfUserCombo.setDisplayField(UserClient.USER_LOGIN);	
		
		CellEditor editor = new CellEditor(onBehalfUserCombo);	
		onBehalfUser.setEditor(editor);
		GridCellRenderer<UserMapClient> onBehalfRenderer = new GridCellRenderer<UserMapClient>(){
			
			public String render(UserMapClient model, String property, ColumnData config, int rowIndex, int colIndex,
					ListStore<UserMapClient> store, Grid<UserMapClient> grid) {
				
				if(null == model.getUserLoginValue()){
					TbitsInfo.error("Please select a valid on behalf user");
					return "NULL";
				}				
				return model.getUserLoginValue().getUserLogin();
			}		
		};
		onBehalfUser.setRenderer(onBehalfRenderer);
		onBehalfUser.setSortable(false);
		cm.getColumns().add(onBehalfUser);
		
		ColumnConfig strictness = new ColumnConfig(UserMapClient.STRICTNESS, 100);
		strictness.setHeader("Strictness");
		final TextField<String> strictnessField = new TextField<String>();
		strictnessField.setAllowBlank(false);
		strictnessField.sinkEvents(Event.ONCHANGE);
		strictnessField.addListener(Events.OnChange, new Listener<FieldEvent>(){			
			public void handleEvent(FieldEvent be) {
				try{
					Integer reportId = Integer.parseInt(be.getField().getValue().toString());
					if(-1 == reportId){
						strictnessField.clear();
						TbitsInfo.error("Please enter a valid Report ID value...");
					}else if(reportId < 0){
						strictnessField.clear();
						TbitsInfo.error("Please enter a valid Report ID value...");
					}else strictnessField.setValue(Integer.toString(reportId));
				}catch (NumberFormatException nfe){
					strictnessField.clear();
					strictnessField.setValue(strictnessField.getOriginalValue());				
					TbitsInfo.error("Please Enter an Integer Value in Strictness field...");
				}catch (Exception e){
					strictnessField.clear();
					TbitsInfo.error("Strictness cannot be left blank... Please Enter a value...");
					Log.error("Null value in report id ", e);
				}
			}
			
		});			
		strictness.setSortable(false);
		strictness.setAlignment(HorizontalAlignment.CENTER);
		strictness.setEditor(new CellEditor(strictnessField));
		cm.getColumns().add(strictness);
		
		ColumnConfig statusCol	= new ColumnConfig(UserMapClient.STATUS,"Status", 200);
		GridCellRenderer<UserMapClient> statusRenderer = new GridCellRenderer<UserMapClient>(){
			public String render(UserMapClient model, String property, ColumnData config, int rowIndex, int colIndex,
					ListStore<UserMapClient> store, Grid<UserMapClient> grid) {
				String color = model.getStatus().equals("OK") ? "green" : "red"; 
				return "<span style='color:" + color + "'>" + model.getStatus() + "</span>";
			}
			
		};
		statusCol.setRenderer(statusRenderer);
		statusCol.setSortable(false);
		cm.getColumns().add(statusCol);
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
	
	public void setCurrentBa(BusinessAreaClient currentBa){
		this.currentBa = currentBa;
	}
	
	public BusinessAreaClient getCurrentBa(){
		return this.currentBa;
	}
}
