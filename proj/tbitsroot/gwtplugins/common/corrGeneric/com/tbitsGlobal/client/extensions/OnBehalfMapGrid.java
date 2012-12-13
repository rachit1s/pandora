package corrGeneric.com.tbitsGlobal.client.extensions;

import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.form.ComboBox;
import com.extjs.gxt.ui.client.widget.form.ComboBox.TriggerAction;
import com.extjs.gxt.ui.client.widget.grid.CellEditor;
import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
import com.extjs.gxt.ui.client.widget.grid.ColumnData;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.extjs.gxt.ui.client.widget.grid.GridCellRenderer;
import com.google.gwt.user.client.rpc.AsyncCallback;

import commons.com.tbitsGlobal.utils.client.TbitsInfo;
import commons.com.tbitsGlobal.utils.client.bulkupdate.BulkUpdateGridAbstract;
import commons.com.tbitsGlobal.utils.client.domainObjects.BusinessAreaClient;
import commons.com.tbitsGlobal.utils.client.domainObjects.TypeClient;
import commons.com.tbitsGlobal.utils.client.domainObjects.UserClient;
import commons.com.tbitsGlobal.utils.client.log.Log;
import corrGeneric.com.tbitsGlobal.client.CorrAdminUtils;
import corrGeneric.com.tbitsGlobal.client.CorrConstants;
import corrGeneric.com.tbitsGlobal.client.modelData.OnBehalfMapClient;
import corrGeneric.com.tbitsGlobal.client.modelData.OnBehalfTypeClient;
import corrGeneric.com.tbitsGlobal.shared.objects.GenericParams;

/**
 * Grid to hold the grid of the OnBehalfMap
 * @author devashish
 *
 */
public class OnBehalfMapGrid extends BulkUpdateGridAbstract<OnBehalfMapClient> {

	private BusinessAreaClient currentBa;
	
	protected ListStore<TypeClient> type1Store;
	protected ListStore<TypeClient> type2Store;
	protected ListStore<TypeClient> type3Store;
	protected ListStore<UserClient> userListStore;
	protected OnBehalfTypeClient onBehalfTypes;
	
	private GridCellRenderer<OnBehalfMapClient> userTypeRenderer = new GridCellRenderer<OnBehalfMapClient>(){
		public String render(OnBehalfMapClient model, String property, ColumnData config, int rowIndex, int colIndex,
				ListStore<OnBehalfMapClient> store, Grid<OnBehalfMapClient> grid) {
			
			
			if(property.equals(OnBehalfMapClient.TYPE_1)){
				if(null == model.getType1()){
					TbitsInfo.error("Please select a valid report Type");
					return "NULL";
				}				
				return model.getType1().getDisplayName();
			}else if(property.equals(OnBehalfMapClient.TYPE_2)){
				if(null == model.getType2()){
					TbitsInfo.error("Please select a valid report Type");
					return "NULL";
				}				
				return model.getType2().getDisplayName();
			}else if(property.equals(OnBehalfMapClient.TYPE_3)){
				if(null == model.getType3()){
					TbitsInfo.error("Please select a valid report Type");
					return "NULL";
				}				
				return model.getType3().getDisplayName();
			}
			return null;
		}		
	};
	
	public OnBehalfMapGrid(BulkGridMode mode) {
		super(mode);
		
		showNumberer = false;
		showStatus	 = false;
		canRemoveRow = false;
		onBehalfTypes = new OnBehalfTypeClient();
		
		type1Store = new ListStore<TypeClient>();
		type2Store = new ListStore<TypeClient>();
		type3Store = new ListStore<TypeClient>();
		userListStore = new ListStore<UserClient>();
	}
	
	
	/**
	 * Fill the stores of the type comboboxes of the grid
	 */
	protected void populateComboBoxes(){
		type1Store.removeAll();
		type1Store.add(onBehalfTypes.getOnBehalfTypeList(GenericParams.OnBehalfType1));
		type2Store.removeAll();
		type2Store.add(onBehalfTypes.getOnBehalfTypeList(GenericParams.OnBehalfType2));
		type3Store.removeAll();
		type3Store.add(onBehalfTypes.getOnBehalfTypeList(GenericParams.OnBehalfType3));
		
		userListStore.removeAll();
		userListStore.add(CorrAdminUtils.getUsersList());
	}
	
	protected void getOnBehalfTypes(){
		CorrConstants.corrAdminService.getOnBehalfTypes(currentBa.getSystemPrefix(), new AsyncCallback<OnBehalfTypeClient>(){

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

	protected void createColumns() {
		ColumnConfig type1Config = new ColumnConfig(OnBehalfMapClient.TYPE_1, 150);
		type1Config.setHeader("On Behalf Type 1");
		ComboBox<TypeClient> type1Combo = new ComboBox<TypeClient>();		
		type1Config.setEditor(buildComboBox(type1Combo, type1Store));
		type1Config.setRenderer(userTypeRenderer);
		type1Config.setSortable(false);
		cm.getColumns().add(type1Config);
		
		ColumnConfig type2Config = new ColumnConfig(OnBehalfMapClient.TYPE_2, 150);
		type2Config.setHeader("On Behalf Type 2");
		ComboBox<TypeClient> type2Combo = new ComboBox<TypeClient>();
		type2Config.setEditor(buildComboBox(type2Combo, type2Store));
		type2Config.setRenderer(userTypeRenderer);
		type2Config.setSortable(false);
		cm.getColumns().add(type2Config);
		
		ColumnConfig type3Config = new ColumnConfig(OnBehalfMapClient.TYPE_3, 150);
		type3Config.setHeader("On Behalf Type 3");
		ComboBox<TypeClient> type3Combo = new ComboBox<TypeClient>();
		type3Config.setEditor(buildComboBox(type3Combo, type3Store));
		type3Config.setRenderer(userTypeRenderer);
		type3Config.setSortable(false);
		cm.getColumns().add(type3Config);
		
		
//		ColumnConfig userLoginValue = new ColumnConfig(OnBehalfMapClient.USER, 150);
//		userLoginValue.setHeader("On Behalf of Login");
//		final ComboBox<UserClient> userComboBox = CorrAdminUtils.getUserCombo();
//		userComboBox.setEmptyText("Choose a user login");
//		userComboBox.addSelectionChangedListener(new SelectionChangedListener<UserClient>(){
//			public void selectionChanged(SelectionChangedEvent<UserClient> se){
//				if(null == currentBa){
//					TbitsInfo.info("Please select a valid BA before selecting the user...");
//					return;
//				}
//				userComboBox.setValue(se.getSelectedItem());
//			}
//		});
//		
//		userLoginValue.setEditor(new CellEditor(userComboBox));
//		GridCellRenderer<OnBehalfMapClient> onBehalfUserRenderer = new GridCellRenderer<OnBehalfMapClient>(){
//			public String render(OnBehalfMapClient model, String property, ColumnData config, int rowIndex, int colIndex,
//					ListStore<OnBehalfMapClient> store, Grid<OnBehalfMapClient> grid) {
//				
//				if(null == model.getOnBehalfUser()){
//					TbitsInfo.error("Please select a valid on behalf user");
//					return "NULL";
//				}				
//				return model.getOnBehalfUser().getUserLogin();
//			}		
//		};
//		userLoginValue.setRenderer(onBehalfUserRenderer);
//		userLoginValue.setSortable(false);
//		cm.getColumns().add(userLoginValue);
		
		ColumnConfig  onBehalfUser = new ColumnConfig(OnBehalfMapClient.ON_BEHALF_USER, "On Behalf of login", 200);
		ComboBox<UserClient> onBehalfUserCombo = new ComboBox<UserClient>();
		onBehalfUserCombo = new ComboBox<UserClient>();
		onBehalfUserCombo.setTriggerAction(TriggerAction.ALL);
		onBehalfUserCombo.setForceSelection(false);
		onBehalfUserCombo.setStore(userListStore);
		onBehalfUserCombo.setDisplayField(UserClient.USER_LOGIN);	
		
		CellEditor editor = new CellEditor(onBehalfUserCombo);	
		onBehalfUser.setEditor(editor);
		GridCellRenderer<OnBehalfMapClient> onBehalfRenderer = new GridCellRenderer<OnBehalfMapClient>(){
			
			public String render(OnBehalfMapClient model, String property, ColumnData config, int rowIndex, int colIndex,
					ListStore<OnBehalfMapClient> store, Grid<OnBehalfMapClient> grid) {
				
				if(null == model.getOnBehalfUser()){
					TbitsInfo.error("Please select a valid on behalf user");
					return "NULL";
				}				
				return model.getOnBehalfUser().getUserLogin();
			}		
		};
		onBehalfUser.setRenderer(onBehalfRenderer);
		onBehalfUser.setSortable(false);
		cm.getColumns().add(onBehalfUser);
		
		ColumnConfig statusCol	= new ColumnConfig(OnBehalfMapClient.STATUS,"Status", 200);
		GridCellRenderer<OnBehalfMapClient> statusRenderer = new GridCellRenderer<OnBehalfMapClient>(){
			public String render(OnBehalfMapClient model, String property, ColumnData config, int rowIndex, int colIndex,
					ListStore<OnBehalfMapClient> store, Grid<OnBehalfMapClient> grid) {
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
		box.setDisplayField(TypeClient.NAME);
		
		CellEditor editor = new CellEditor(box);		
		return editor;
	}
	
	public void setUserStore(ListStore<UserClient> userListStore){
		this.userListStore = userListStore;
	}
	
	public void setCurrentBa(BusinessAreaClient currentBa){
		this.currentBa = currentBa;
	}
	
	public BusinessAreaClient getCurrentBa(){
		return this.currentBa;
	}

}
