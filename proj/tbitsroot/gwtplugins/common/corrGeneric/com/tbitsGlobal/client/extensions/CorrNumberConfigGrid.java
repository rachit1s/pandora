package corrGeneric.com.tbitsGlobal.client.extensions;

import java.util.ArrayList;

import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.form.ComboBox;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.extjs.gxt.ui.client.widget.form.ComboBox.TriggerAction;
import com.extjs.gxt.ui.client.widget.grid.CellEditor;
import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
import com.extjs.gxt.ui.client.widget.grid.ColumnData;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.extjs.gxt.ui.client.widget.grid.GridCellRenderer;
import com.google.gwt.user.client.rpc.AsyncCallback;

import commons.com.tbitsGlobal.utils.client.TbitsCellEditor;
import commons.com.tbitsGlobal.utils.client.TbitsInfo;
import commons.com.tbitsGlobal.utils.client.bulkupdate.BulkUpdateGridAbstract;
import commons.com.tbitsGlobal.utils.client.domainObjects.BusinessAreaClient;
import commons.com.tbitsGlobal.utils.client.domainObjects.FieldClient;
import commons.com.tbitsGlobal.utils.client.domainObjects.TypeClient;
import commons.com.tbitsGlobal.utils.client.log.Log;

import corrGeneric.com.tbitsGlobal.client.CorrConstants;
import corrGeneric.com.tbitsGlobal.client.modelData.CorrNumberConfigClient;
import corrGeneric.com.tbitsGlobal.client.modelData.CorrNumberKeyClient;

import corrGeneric.com.tbitsGlobal.shared.objects.GenericParams;

public class CorrNumberConfigGrid extends
		BulkUpdateGridAbstract<CorrNumberConfigClient> {

	private ListStore<TypeClient> type1Store;
	private ListStore<TypeClient> type2Store;
	private ListStore<TypeClient> type3Store;
//	private ListStore<FieldClient> typeFieldStore;
	protected CorrNumberKeyClient corrNumKeyList;
	private BusinessAreaClient currentBa;
	private GridCellRenderer<CorrNumberConfigClient> typeRenderer = new GridCellRenderer<CorrNumberConfigClient>() {

		@Override
		public Object render(CorrNumberConfigClient model, String property,
				ColumnData config, int rowIndex, int colIndex,
				ListStore<CorrNumberConfigClient> store,
				Grid<CorrNumberConfigClient> grid) {
			if(property.equals(CorrNumberConfigClient.NumType1)){
				if(null == model.getNumType1()){
					TbitsInfo.error("Please select a valid Type");
					return "NULL";
				}				
				return model.getNumType1().getDisplayName();
			}else if(property.equals(CorrNumberConfigClient.NumType2)){
				if(null == model.getNumType2()){
					TbitsInfo.error("Please select a valid Type");
					return "NULL";
				}				
				return model.getNumType2().getDisplayName();
			}else if(property.equals(CorrNumberConfigClient.NumType3)){
				if(null == model.getNumType3()){
					TbitsInfo.error("Please select a valid report Type");
					return "NULL";
				}				
				return model.getNumType3().getDisplayName();
			}
			return null;
		}
	};

	public CorrNumberConfigGrid(BulkGridMode mode) {
		super(mode);
		showNumberer = false;
		showStatus = false;
		canRemoveRow = false;
		
		type1Store = new ListStore<TypeClient>();
		type2Store = new ListStore<TypeClient>();
		type3Store = new ListStore<TypeClient>();
//		typeFieldStore = new ListStore<FieldClient>();
		corrNumKeyList = new CorrNumberKeyClient();
	}

	@Override
	protected void createColumns() {
		ColumnConfig sysPrefix = new ColumnConfig(
				CorrNumberConfigClient.SysPrefix, 100);
		sysPrefix.setHeader("Sys Prefix");
		TextField<String> property = new TextField<String>();
		property.setAllowBlank(false);
		sysPrefix.setEditor(new TbitsCellEditor(property));
		cm.getColumns().add(sysPrefix);

		
		
		ColumnConfig numType1 = new ColumnConfig(
				CorrNumberConfigClient.NumType1, 100);
		numType1.setHeader("Num Type 1");
		ComboBox<TypeClient> type1Combo = new ComboBox<TypeClient>();
		numType1.setEditor(buildComboBox(type1Combo, type1Store));
		numType1.setRenderer(typeRenderer);
		numType1.setSortable(false);
		cm.getColumns().add(numType1);

		
		
		ColumnConfig numType2 = new ColumnConfig(
				CorrNumberConfigClient.NumType2, 100);
		numType2.setHeader("Num Type 2");
		ComboBox<TypeClient> type2Combo = new ComboBox<TypeClient>();
		numType2.setEditor(buildComboBox(type2Combo, type2Store));
		numType2.setRenderer(typeRenderer);
		numType2.setSortable(false);
		cm.getColumns().add(numType2);

		
		
		ColumnConfig numType3 = new ColumnConfig(
				CorrNumberConfigClient.NumType3, 100);
		numType3.setHeader("Num Type 3");
		ComboBox<TypeClient> type3Combo = new ComboBox<TypeClient>();
		numType3.setEditor(buildComboBox(type3Combo, type3Store));
		numType3.setRenderer(typeRenderer);
		numType3.setSortable(false);
		cm.getColumns().add(numType3);

		
		
		ColumnConfig numFormat = new ColumnConfig(
				CorrNumberConfigClient.NumFormat, 150);
		numFormat.setHeader("Number format");
		TextField<String> numFormatProperty = new TextField<String>();
		numFormatProperty.setAllowBlank(false);
		numFormat.setEditor(new TbitsCellEditor(numFormatProperty));
		cm.getColumns().add(numFormat);

		
		
		ColumnConfig numFields = new ColumnConfig(
				CorrNumberConfigClient.NumFields, 150);
		numFields.setHeader("Number fields");
		TextField<String> numFieldProperty = new TextField<String>();
		numFieldProperty.setAllowBlank(false);
		numFields.setEditor(new TbitsCellEditor(numFieldProperty));
		
		/*ComboBox<FieldClient> typeFieldCombobox	 = new ComboBox<FieldClient>();
		typeFieldCombobox.setTriggerAction(TriggerAction.ALL);
		typeFieldCombobox.setForceSelection(false);
		typeFieldCombobox.setStore(typeFieldStore);
		typeFieldCombobox.setDisplayField(FieldClient.NAME);	
		CellEditor typeFieldEditor = new CellEditor(typeFieldCombobox);	
		numFields.setEditor(typeFieldEditor);
		GridCellRenderer<CorrNumberConfigClient> typeFieldRenderer = new GridCellRenderer<CorrNumberConfigClient>(){
			
		
			@Override
			public Object render(CorrNumberConfigClient model, String property,
					ColumnData config, int rowIndex, int colIndex,
					ListStore<CorrNumberConfigClient> store,
					Grid<CorrNumberConfigClient> grid) {
				if(null == model.getNumFields()){
					TbitsInfo.error("Please select a valid Number field");
					return "NULL";
				}				
				return model.getNumFields().getName();
			}		
		};
		numFields.setRenderer(typeFieldRenderer);*/
		//numFields.setSortable(false);
		cm.getColumns().add(numFields);
		
		

		ColumnConfig maxIdFormat = new ColumnConfig(
				CorrNumberConfigClient.MaxIdFormat, 150);
		maxIdFormat.setHeader("Max Id Format");
		TextField<String> maxIdFormatProperty = new TextField<String>();
		// maxIdFormatProperty.setAllowBlank(false);
		maxIdFormat.setEditor(new TbitsCellEditor(maxIdFormatProperty));
		cm.getColumns().add(maxIdFormat);

		ColumnConfig maxIdFields = new ColumnConfig(
				CorrNumberConfigClient.MaxIdFields, 150);
		maxIdFields.setHeader("Max Id Fields");
		TextField<String> maxIdFieldProperty = new TextField<String>();
		maxIdFieldProperty.setAllowBlank(false);
		maxIdFields.setEditor(new TbitsCellEditor(maxIdFieldProperty));

		
		
		/*ComboBox<FieldClient> maxFieldCombobox	 = new ComboBox<FieldClient>();
		maxFieldCombobox.setTriggerAction(TriggerAction.ALL);
		maxFieldCombobox.setForceSelection(false);
		maxFieldCombobox.setStore(typeFieldStore);
		maxFieldCombobox.setDisplayField(FieldClient.NAME);	
		CellEditor maxFieldEditor = new CellEditor(maxFieldCombobox);	
		maxIdFields.setEditor(maxFieldEditor);
		GridCellRenderer<CorrNumberConfigClient> maxFieldRenderer = new GridCellRenderer<CorrNumberConfigClient>(){
			
		
			@Override
			public Object render(CorrNumberConfigClient model, String property,
					ColumnData config, int rowIndex, int colIndex,
					ListStore<CorrNumberConfigClient> store,
					Grid<CorrNumberConfigClient> grid) {
				if(null == model.getMaxIdFields()){
					TbitsInfo.error("Please select a valid Number field");
					return "NULL";
				}				
				return model.getMaxIdFields().getName();
			}		
		};
		maxIdFields.setRenderer(maxFieldRenderer);
		maxIdFields.setSortable(false);*/
		cm.getColumns().add(maxIdFields);
		ColumnConfig statusCol	= new ColumnConfig(CorrNumberConfigClient.STATUS,"Status", 200);
		GridCellRenderer<CorrNumberConfigClient> statusRenderer = new GridCellRenderer<CorrNumberConfigClient>(){
			public String render(CorrNumberConfigClient model, String property, ColumnData config, int rowIndex, int colIndex,
					ListStore<CorrNumberConfigClient> store, Grid<CorrNumberConfigClient> grid) {
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
	 * 
	 * @param ComboBox
	 *            object, box which has to be built
	 * @param boxStore
	 *            , store to be attached to the box
	 * @return CellEditor, editor attached to the box
	 */

	private CellEditor buildComboBox(ComboBox<TypeClient> box,
			ListStore<TypeClient> boxStore) {
		box.setTriggerAction(TriggerAction.ALL);
		box.setForceSelection(false);
		box.setStore(boxStore);
		box.setDisplayField(TypeClient.DISPLAY_NAME);

		CellEditor editor = new CellEditor(box);
		return editor;
	}

	public void setCurrentBa(BusinessAreaClient currentBa) {
		this.currentBa = currentBa;
	}

	public BusinessAreaClient getCurrentBa() {
		return this.currentBa;
	}

	public void getCorrNumTypes() {
		CorrConstants.corrAdminService.getCorrNumberKey(
				currentBa.getSystemPrefix(),
				new AsyncCallback<CorrNumberKeyClient>() {
					public void onFailure(Throwable caught) {
						TbitsInfo
								.error("Could not fetch Corr Number Key from database... Please see logs for more information...",
										caught);
						Log.error(
								"Could not fetch Corr Number Key from database...",
								caught);
					}

					public void onSuccess(CorrNumberKeyClient result) {
						if (null != result) {
							corrNumKeyList = result;
							Log.info("Successfully fetched  Corr Number Key  from database...");
						} else {
							TbitsInfo
									.info("No Type fields configured for current Business Area ...");
							Log.info("No Type fields configured for current Business Area...");
						}
					}
				});

	}

	public void populateComboBoxes() {
		type1Store.removeAll();
		type1Store.add(corrNumKeyList
				.getCorrNumberKeyList(GenericParams.NumType1));

		type2Store.removeAll();
		type2Store.add(corrNumKeyList
				.getCorrNumberKeyList(GenericParams.NumType2));

		type3Store.removeAll();
		type3Store.add(corrNumKeyList
				.getCorrNumberKeyList(GenericParams.NumType3));

	/*	typeFieldStore.removeAll();
		CorrConstants.corrAdminService.getFields(currentBa.getSystemPrefix(),
				new AsyncCallback<ArrayList<FieldClient>>() {
					public void onFailure(Throwable caught) {
						TbitsInfo
								.error("Could not fetch Fields list for selected Business Area... See logs for more information...",
										caught);
						Log.error(
								"Could not fetch fields list for selected Business Areas...",
								caught);
					}

					public void onSuccess(ArrayList<FieldClient> result) {
						if (null != result) {
							typeFieldStore.add(result);
							Log.info("Successfully fetched Fields List...");
						}
					}
				});*/

	}

}
