package transmittal.com.tbitsGlobal.client.admin.pages;

import java.util.List;

import transmittal.com.tbitsGlobal.client.admin.TrnAdminUtils;
import transmittal.com.tbitsGlobal.client.models.TrnAttachmentList;

import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.GridEvent;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.form.CheckBox;
import com.extjs.gxt.ui.client.widget.form.ComboBox;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.extjs.gxt.ui.client.widget.grid.CheckColumnConfig;
import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
import com.extjs.gxt.ui.client.widget.grid.ColumnData;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.extjs.gxt.ui.client.widget.grid.GridCellRenderer;
import com.google.gwt.user.client.rpc.AsyncCallback;
import commons.com.tbitsGlobal.utils.client.GlobalConstants;
import commons.com.tbitsGlobal.utils.client.TbitsCellEditor;
import commons.com.tbitsGlobal.utils.client.TbitsInfo;
import commons.com.tbitsGlobal.utils.client.bafield.BAField;
import commons.com.tbitsGlobal.utils.client.bulkupdate.BulkUpdateGridAbstract;
import commons.com.tbitsGlobal.utils.client.domainObjects.BusinessAreaClient;
import commons.com.tbitsGlobal.utils.client.log.Log;

public class AttachmentListGrid extends BulkUpdateGridAbstract<TrnAttachmentList>{
	
	private BusinessAreaClient srcBA;
	
	public AttachmentListGrid(BulkGridMode mode) {
		super(mode);

		showNumberer = false;
		showStatus	 = false;
	}
	
	
	private GridCellRenderer<TrnAttachmentList> fieldCellRenderer = new GridCellRenderer<TrnAttachmentList>(){
		public Object render(TrnAttachmentList model, String property,
				ColumnData config, int rowIndex, int colIndex,
				ListStore<TrnAttachmentList> store, Grid<TrnAttachmentList> grid) {
			BAField field = model.get(property);
			if(field != null)
				return field.getDisplayName() + " <" + field.getFieldId() + ">";
			else return "";
	}};
	
	
	protected void createColumns(){
		ColumnConfig nameCol = new ColumnConfig(TrnAttachmentList.NAME, 170);
		nameCol.setHeader("Name");
		nameCol.setEditor(new TbitsCellEditor(new TextField<String>()));
		cm.getColumns().add(nameCol);
		
		ColumnConfig fieldCol = new ColumnConfig(TrnAttachmentList.FIELD, 200);
		fieldCol.setHeader("Field");
		final ComboBox<BAField> fieldCombo = TrnAdminUtils.getFieldsCombo();
		fieldCol.setEditor(new TbitsCellEditor(fieldCombo));
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
							GlobalConstants.utilService.getFields(srcBA.getSystemPrefix(), new AsyncCallback<List<BAField>>(){
								public void onFailure(Throwable caught) {
									TbitsInfo.error("Error fetching fields", caught);
									Log.error("Error fetching fields", caught);
								}
			
								public void onSuccess(List<BAField> result) {
									if(result != null){
										fieldCombo.getStore().add(result);
									}
								}
							});
						}
					}
				}
			}});
		
		ColumnConfig dataTypeCol = new ColumnConfig(TrnAttachmentList.DATA_TYPE_ID, 70);
		dataTypeCol.setHeader("Data Type");
		dataTypeCol = TrnAdminUtils.getIntegerColConfig(dataTypeCol);
		cm.getColumns().add(dataTypeCol);
		
		ColumnConfig fieldConfigCol = new ColumnConfig(TrnAttachmentList.DEFAULT_VALUE, 150);
		fieldConfigCol.setHeader("Default Value");
		fieldConfigCol.setEditor(new TbitsCellEditor(new TextField<String>()));
		cm.getColumns().add(fieldConfigCol);
		
		CheckColumnConfig isEditableCol = new CheckColumnConfig();
		isEditableCol.setId(TrnAttachmentList.IS_EDITABLE);
		isEditableCol.setWidth(100);
		isEditableCol.setHeader("Is Editable");
		isEditableCol.setEditor(new TbitsCellEditor(new CheckBox()));
		cm.getColumns().add(isEditableCol);
		
		CheckColumnConfig isActiveCol = new CheckColumnConfig();
		isActiveCol.setId(TrnAttachmentList.IS_ACTIVE);
		isActiveCol.setWidth(100);
		isActiveCol.setHeader("Is Active");
		isActiveCol.setEditor(new TbitsCellEditor(new CheckBox()));
		cm.getColumns().add(isActiveCol);
		
		ColumnConfig orderCol = new ColumnConfig(TrnAttachmentList.COLUMN_ORDER, 70);
		orderCol.setHeader("Column Order");
		orderCol = TrnAdminUtils.getIntegerColConfig(orderCol);
		cm.getColumns().add(orderCol);
		
		ColumnConfig typeValueSourceCol = new ColumnConfig(TrnAttachmentList.TYPE_VALUE_SOURCE, 70);
		typeValueSourceCol.setHeader("Type Value Source");
		typeValueSourceCol = TrnAdminUtils.getIntegerColConfig(typeValueSourceCol);
		cm.getColumns().add(typeValueSourceCol);
		
		CheckColumnConfig isIncludedCol = new CheckColumnConfig();
		isIncludedCol.setId(TrnAttachmentList.IS_INCLUDED);
		isIncludedCol.setWidth(100);
		isIncludedCol.setHeader("Is Included");
		isIncludedCol.setEditor(new TbitsCellEditor(new CheckBox()));
		cm.getColumns().add(isIncludedCol);
	}

	public void setSrcBA(BusinessAreaClient srcBA) {
		this.srcBA = srcBA;
	}

	public BusinessAreaClient getSrcBA() {
		return srcBA;
	}

}
