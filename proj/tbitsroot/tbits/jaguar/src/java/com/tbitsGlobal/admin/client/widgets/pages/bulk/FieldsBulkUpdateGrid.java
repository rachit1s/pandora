package com.tbitsGlobal.admin.client.widgets.pages.bulk;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.extjs.gxt.ui.client.event.GridEvent;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.Window;
import com.extjs.gxt.ui.client.widget.form.ComboBox;
import com.extjs.gxt.ui.client.widget.form.TextArea;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
import com.extjs.gxt.ui.client.widget.grid.ColumnData;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.extjs.gxt.ui.client.widget.grid.GridCellRenderer;
import com.extjs.gxt.ui.client.widget.layout.FitData;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.tbitsGlobal.admin.client.events.OnFieldsDelete;
import com.tbitsGlobal.admin.client.utils.APConstants;
import commons.com.tbitsGlobal.utils.client.ClickableLink;
import commons.com.tbitsGlobal.utils.client.ClickableLink.ClickableLinkListener;
import commons.com.tbitsGlobal.utils.client.TbitsCellEditor;
import commons.com.tbitsGlobal.utils.client.TbitsInfo;
import commons.com.tbitsGlobal.utils.client.TbitsModelData;
import commons.com.tbitsGlobal.utils.client.Events.TbitsEventRegister;
import commons.com.tbitsGlobal.utils.client.GridCellRenderers.LinkCellRenderer;
import commons.com.tbitsGlobal.utils.client.bulkupdate.BulkUpdateGridAbstract;
import commons.com.tbitsGlobal.utils.client.bulkupdate.ExcelImportTypeModel.ExcelImportDataType;
import commons.com.tbitsGlobal.utils.client.bulkupdate.ExcelImportWindow.Parser;
import commons.com.tbitsGlobal.utils.client.domainObjects.DataTypeClient;
import commons.com.tbitsGlobal.utils.client.domainObjects.FieldClient;
import commons.com.tbitsGlobal.utils.client.log.Log;

public class FieldsBulkUpdateGrid extends BulkUpdateGridAbstract<FieldClient>{

	public static final String tOptions[] = { "Do not track the field",
		"Always display the current value",
		"Display the current value if it is not empty",
		"Display the change in the value",
		"Display the change in the value or the current value if there is no change" };
	
	public FieldsBulkUpdateGrid(BulkGridMode mode) {
		super(mode);
		
		showStatus = false;
	}

	@Override
	protected void createColumns() {
		ColumnConfig idCol = new ColumnConfig(FieldClient.FIELD_ID, 100);
		idCol.setHeader("Id");
		idCol.setFixed(true);
		cm.getColumns().add(idCol);
		dataTypeMap.put(FieldClient.FIELD_ID, ExcelImportDataType.Number);
		
		ColumnConfig nameCol = new ColumnConfig(FieldClient.NAME, 200);
		nameCol.setHeader("Name");
		cm.getColumns().add(nameCol);
		
		ColumnConfig dataTypeCol = new ColumnConfig(FieldClient.DATA_TYPE_ID, 100);
		dataTypeCol.setHeader("Data Type");
		dataTypeCol.setRenderer(new GridCellRenderer<FieldClient>() {
			@Override
			public Object render(FieldClient model, String property,
					ColumnData config, int rowIndex, int colIndex,
					ListStore<FieldClient> store, Grid<FieldClient> grid) {
				if(model.get(FieldClient.DATA_TYPE_ID) != null)
					return DataTypeClient.getDataTypeMap().get(model.getDataTypeId());
				return "";
			}
		});
		cm.getColumns().add(dataTypeCol);
		parserMap.put(FieldClient.DATA_TYPE_ID, new Parser() {
			@Override
			public Object parse(String value) {
				try{
					int dataTypeId = Integer.parseInt(value);
					return dataTypeId;
				}catch(Exception e){
					HashMap<Integer, String> dataTypeMap = DataTypeClient.getDataTypeMap();
					for(int key : dataTypeMap.keySet()){
						if(dataTypeMap.get(key).equals(value))
							return key;
					}
				}
				return null;
			}
		});
		
		ColumnConfig displayNameCol = new ColumnConfig(FieldClient.DISPLAY_NAME, 200);
		displayNameCol.setHeader("Display Name");
		displayNameCol.setEditor(new TbitsCellEditor(new TextField<String>()));
		cm.getColumns().add(displayNameCol);
		
		ColumnConfig regexCol = new ColumnConfig(FieldClient.REGEX, 100);
		regexCol.setHeader("Regex");
		regexCol.setEditor(new TbitsCellEditor(new TextField<String>()));
		cm.getColumns().add(regexCol);
		
		ColumnConfig errorCol = new ColumnConfig(FieldClient.ERROR, 150);
		errorCol.setHeader("Regex Error Message");
		errorCol.setEditor(new TbitsCellEditor(new TextField<String>()));
		cm.getColumns().add(errorCol);
		
		final ComboBox<TbitsModelData> trackingOptCombo = new ComboBox<TbitsModelData>();
		final ListStore<TbitsModelData> trackingOptStore = new ListStore<TbitsModelData>();
		for (int i = 0; i < 5; i++) {
			TbitsModelData m = new TbitsModelData();
			m.set("TRACKING_OPTION", tOptions[i]);
			m.set("ID", i);
			trackingOptStore.add(m);
		}
		trackingOptCombo.setDisplayField("TRACKING_OPTION");
		trackingOptCombo.setStore(trackingOptStore);
		trackingOptCombo.setEditable(false);
		
		ColumnConfig trackingOptionsCol = new ColumnConfig(FieldClient.TRACKING_OPTION, 200);
		trackingOptionsCol.setHeader("Tracking Option");
		trackingOptionsCol.setEditor(new TbitsCellEditor(trackingOptCombo){
			@Override
			public Object preProcessValue(Object value) {
				if(value != null){
					int trackingOptionId = (Integer) value;
					TbitsModelData model = trackingOptCombo.getStore().findModel("ID", trackingOptionId);
					return model;
				}
				return null;
			}
			
			@Override
			public Object postProcessValue(Object value) {
				TbitsModelData model = (TbitsModelData) value;
				return model.get("ID");
			}
		});
		trackingOptionsCol.setRenderer(new GridCellRenderer<FieldClient>() {
			@Override
			public Object render(FieldClient model, String property,
					ColumnData config, int rowIndex, int colIndex,
					ListStore<FieldClient> store, Grid<FieldClient> grid) {
				if(model.get(FieldClient.TRACKING_OPTION) != null){
					int trackingOptionId = model.getTrackingOption();
					TbitsModelData tro = trackingOptStore.findModel("ID", trackingOptionId);
					if(tro != null)
						return tro.get("TRACKING_OPTION");
				}
				
				return "";
			}
		});
		cm.getColumns().add(trackingOptionsCol);
		parserMap.put(FieldClient.TRACKING_OPTION, new Parser() {
			@Override
			public Object parse(String value) {
				try{
					int trackingOptionId = Integer.parseInt(value);
					return trackingOptionId;
				}catch(Exception e){
					TbitsModelData model = trackingOptStore.findModel("TRACKING_OPTION", value);
					if(model != null){
						return model.get("ID");
					}
				}
				return null;
			}
		});
		
		ColumnConfig displayGrpCol = new ColumnConfig(FieldClient.DISPLAY_GROUP, 200);
		displayGrpCol.setHeader("Display Group");
		displayGrpCol.setEditor(new TbitsCellEditor(new TextArea()));
		cm.getColumns().add(displayGrpCol);
		
		ColumnConfig descCol = new ColumnConfig(FieldClient.DESCRIPTION, 200);
		descCol.setHeader("Description");
		descCol.setEditor(new TbitsCellEditor(new TextArea()));
		cm.getColumns().add(descCol);
		
		ColumnConfig addFieldsColumn = new ColumnConfig("types", "", 120);
		addFieldsColumn.setFixed(true);
		GridCellRenderer<FieldClient> fieldsbuttonRenderer = new LinkCellRenderer<FieldClient>(){
			@Override
			public Object render(final FieldClient model, String property,
					ColumnData config, int rowIndex, int colIndex,
					final ListStore<FieldClient> store,
					Grid<FieldClient> grid) {
				if(model.get(FieldClient.DATA_TYPE_ID) != null && model.getDataTypeId() == DataTypeClient.TYPE){
					ClickableLink link = new ClickableLink("Types", new ClickableLinkListener<GridEvent<FieldClient>>(){
							public void onClick(GridEvent<FieldClient> e) {
								Window window = new Window();
								window.setHeading("Types");
								window.setLayout(new FitLayout());
								window.setModal(true);
								window.setClosable(true);
								
								window.setWidth(com.google.gwt.user.client.Window.getClientWidth() - 100);
								window.setHeight(com.google.gwt.user.client.Window.getClientHeight() - 100);
								
								TypeBulkGridPanel typeEditor = new TypeBulkGridPanel(model);
								window.add(typeEditor, new FitData());
								window.show();
							}
						});
					addLink(link);
					return link.getHtml();
				}
				
				return "";
			}};
		addFieldsColumn.setRenderer(fieldsbuttonRenderer);
		cm.getColumns().add(addFieldsColumn);
	}
	
	@Override
	protected boolean beforeRemoveRow(FieldClient model) {
		if(model != null){
			if(model.getIsExtended())
				return com.google.gwt.user.client.Window.confirm("Do you want to delete the selected Field?");
			com.google.gwt.user.client.Window.alert("Only Extended Fields can be deleted");
		}
		
		return false;
	}
	
	@Override
	protected void onRemove(FieldClient model) {
		final List<FieldClient> selectedItems = new ArrayList<FieldClient>();
		selectedItems.add(model);
		APConstants.apService.deleteFields(selectedItems, new AsyncCallback<Boolean>(){
			public void onFailure(Throwable caught) {
				TbitsInfo.error("Unable to delete field.", caught);
				Log.error("Unable to delete field.", caught);
			}
			public void onSuccess(Boolean result) {
				if(result){
					TbitsInfo.info("Fields deleted successfully");
					for(final FieldClient model : selectedItems){
						FieldsBulkUpdateGrid.this.getStore().remove(model);
					}
					TbitsEventRegister.getInstance().fireEvent(new OnFieldsDelete(selectedItems));
				}
			}
			
		});
	}
}
