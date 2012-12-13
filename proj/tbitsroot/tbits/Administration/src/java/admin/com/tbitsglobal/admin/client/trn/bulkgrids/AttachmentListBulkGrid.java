package admin.com.tbitsglobal.admin.client.trn.bulkgrids;

import java.util.List;

import com.allen_sauer.gwt.log.client.Log;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.GridEvent;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.form.CheckBox;
import com.extjs.gxt.ui.client.widget.form.ComboBox;
import com.extjs.gxt.ui.client.widget.form.NumberField;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.extjs.gxt.ui.client.widget.grid.CellEditor;
import com.extjs.gxt.ui.client.widget.grid.CheckColumnConfig;
import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
import com.extjs.gxt.ui.client.widget.grid.ColumnData;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.extjs.gxt.ui.client.widget.grid.GridCellRenderer;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.rpc.AsyncCallback;

import commons.com.tbitsGlobal.utils.client.TbitsInfo;
import commons.com.tbitsGlobal.utils.client.bafield.BAField;
import commons.com.tbitsGlobal.utils.client.domainObjects.BusinessAreaClient;

import admin.com.tbitsglobal.admin.client.AdminUtils;
import admin.com.tbitsglobal.admin.client.trn.models.TrnAttachmentList;
import bulkupdate.com.tbitsglobal.bulkupdate.client.BulkUpdateGridAbstract;

public class AttachmentListBulkGrid extends BulkUpdateGridAbstract<TrnAttachmentList>{
	
	private BusinessAreaClient srcBA;
	
	private GridCellRenderer<TrnAttachmentList> fieldCellRenderer = new GridCellRenderer<TrnAttachmentList>(){
		public Object render(TrnAttachmentList model, String property,
				ColumnData config, int rowIndex, int colIndex,
				ListStore<TrnAttachmentList> store, Grid<TrnAttachmentList> grid) {
			BAField field = model.get(property);
			if(field != null)
				return field.getDisplayName() + " <" + field.getFieldId() + ">";
			else return "";
		}};

	public AttachmentListBulkGrid(int mode) {
		super(mode);

		showNumberer = true;
	}
	
	@Override
	protected void onRender(Element target, int index) {
		super.onRender(target, index);
		
		this.createColumnModel();
	}
	
	protected void createColumnModel(){
		ColumnConfig nameCol = new ColumnConfig(TrnAttachmentList.NAME, 200);
		nameCol.setHeader("Name");
		nameCol.setEditor(new CellEditor(new TextField<String>()));
		cm.getColumns().add(nameCol);
		
		ColumnConfig fieldCol = new ColumnConfig(TrnAttachmentList.FIELD, 200);
		fieldCol.setHeader("Field");
		final ComboBox<BAField> fieldCombo = AdminUtils.getFieldsCombo();
		fieldCol.setEditor(new CellEditor(fieldCombo));
		fieldCol.setRenderer(fieldCellRenderer);
		cm.getColumns().add(fieldCol);
		
		this.addListener(Events.BeforeEdit, new Listener<GridEvent<TrnAttachmentList>>(){
			private BusinessAreaClient lastSrcBA;
			public void handleEvent(GridEvent<TrnAttachmentList> be) {
				if(be.getProperty().equals(TrnAttachmentList.FIELD)){
					if(srcBA == null || (srcBA != null && (lastSrcBA == null || !lastSrcBA.equals(srcBA)))){
						lastSrcBA = srcBA;
						fieldCombo.getStore().removeAll();
						if(srcBA != null){
							AdminUtils.dbService.getFields(srcBA.getSystemPrefix(), new AsyncCallback<List<BAField>>(){
								public void onFailure(Throwable caught) {
									TbitsInfo.write("Error fetching fields", TbitsInfo.ERROR);
									Log.error("Error fetching fields", caught);
								}
			
								public void onSuccess(List<BAField> result) {
									if(result != null){
										fieldCombo.getStore().add(result);
									}
								}});
						}
					}
				}
			}});
		
		ColumnConfig dataTypeCol = new ColumnConfig(TrnAttachmentList.DATA_TYPE_ID, 70);
		dataTypeCol.setHeader("Data Type");
		dataTypeCol.setEditor(new CellEditor(new NumberField()){
			@Override
			public Object postProcessValue(Object value) {
				return ((Number)super.postProcessValue(value)).intValue();
			}
		});
		cm.getColumns().add(dataTypeCol);
		
		ColumnConfig fieldConfigCol = new ColumnConfig(TrnAttachmentList.DEFAULT_VALUE, 200);
		fieldConfigCol.setHeader("Default Value");
		fieldConfigCol.setEditor(new CellEditor(new TextField<String>()));
		cm.getColumns().add(fieldConfigCol);
		
		CheckColumnConfig isEditableCol = new CheckColumnConfig();
		isEditableCol.setId(TrnAttachmentList.IS_EDITABLE);
		isEditableCol.setWidth(100);
		isEditableCol.setHeader("Is Editable");
		isEditableCol.setEditor(new CellEditor(new CheckBox()));
		cm.getColumns().add(isEditableCol);
		
		CheckColumnConfig isActiveCol = new CheckColumnConfig();
		isActiveCol.setId(TrnAttachmentList.IS_ACTIVE);
		isActiveCol.setWidth(100);
		isActiveCol.setHeader("Is Active");
		isActiveCol.setEditor(new CellEditor(new CheckBox()));
		cm.getColumns().add(isActiveCol);
		
		ColumnConfig orderCol = new ColumnConfig(TrnAttachmentList.COLUMN_ORDER, 70);
		orderCol.setHeader("Column Order");
		orderCol.setEditor(new CellEditor(new NumberField()){
			@Override
			public Object postProcessValue(Object value) {
				return ((Number)super.postProcessValue(value)).intValue();
			}
		});
		cm.getColumns().add(orderCol);
		
		ColumnConfig typeValueSourceCol = new ColumnConfig(TrnAttachmentList.TYPE_VALUE_SOURCE, 70);
		typeValueSourceCol.setHeader("Type Value Source");
		typeValueSourceCol.setEditor(new CellEditor(new NumberField()){
			@Override
			public Object postProcessValue(Object value) {
				return ((Number)super.postProcessValue(value)).intValue();
			}
		});
		cm.getColumns().add(typeValueSourceCol);
		
		CheckColumnConfig isIncludedCol = new CheckColumnConfig();
		isIncludedCol.setId(TrnAttachmentList.IS_INCLUDED);
		isIncludedCol.setWidth(100);
		isIncludedCol.setHeader("Is Included");
		isIncludedCol.setEditor(new CellEditor(new CheckBox()));
		cm.getColumns().add(isIncludedCol);
	}

	public void setSrcBA(BusinessAreaClient srcBA) {
		this.srcBA = srcBA;
	}

	public BusinessAreaClient getSrcBA() {
		return srcBA;
	}

}
