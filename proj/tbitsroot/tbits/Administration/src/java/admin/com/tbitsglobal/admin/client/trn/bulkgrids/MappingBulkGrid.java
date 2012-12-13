package admin.com.tbitsglobal.admin.client.trn.bulkgrids;

import java.util.List;

import admin.com.tbitsglobal.admin.client.AdminUtils;
import admin.com.tbitsglobal.admin.client.trn.models.TrnFieldMapping;
import bulkupdate.com.tbitsglobal.bulkupdate.client.BulkUpdateGridAbstract;

import com.allen_sauer.gwt.log.client.Log;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.GridEvent;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.form.ComboBox;
import com.extjs.gxt.ui.client.widget.grid.CellEditor;
import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
import com.extjs.gxt.ui.client.widget.grid.ColumnData;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.extjs.gxt.ui.client.widget.grid.GridCellRenderer;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.rpc.AsyncCallback;

import commons.com.tbitsGlobal.utils.client.TbitsInfo;
import commons.com.tbitsGlobal.utils.client.bafield.BAField;
import commons.com.tbitsGlobal.utils.client.domainObjects.BusinessAreaClient;

public class MappingBulkGrid extends BulkUpdateGridAbstract<TrnFieldMapping>{
	
	private GridCellRenderer<TrnFieldMapping> baCellRenderer = new GridCellRenderer<TrnFieldMapping>(){
		public Object render(TrnFieldMapping model, String property,
				ColumnData config, int rowIndex, int colIndex,
				ListStore<TrnFieldMapping> store, Grid<TrnFieldMapping> grid) {
			BusinessAreaClient ba = model.get(property);
			if(ba != null)
				return ba.getSystemPrefix() + " <" + ba.getSystemId() + ">";
			else return "";
		}};
		
	private GridCellRenderer<TrnFieldMapping> fieldCellRenderer = new GridCellRenderer<TrnFieldMapping>(){
		public Object render(TrnFieldMapping model, String property,
				ColumnData config, int rowIndex, int colIndex,
				ListStore<TrnFieldMapping> store, Grid<TrnFieldMapping> grid) {
			BAField field = model.get(property);
			if(field != null)
				return field.getDisplayName() + " <" + field.getFieldId() + ">";
			else return "";
		}};
	
	public MappingBulkGrid(int mode) {
		super(mode);

		showNumberer = true;
	}
	
	@Override
	protected void onRender(Element target, int index) {
		super.onRender(target, index);
		
		this.createColumnModel();
	}
	
	protected void createColumnModel(){
		ColumnConfig srcBACol = new ColumnConfig(TrnFieldMapping.SRC_BA, 200);
		srcBACol.setHeader("Source BA");
		srcBACol.setEditor(new CellEditor(AdminUtils.getBACombo()));
		srcBACol.setRenderer(baCellRenderer);
		cm.getColumns().add(srcBACol);
		
		ColumnConfig srcFieldCol = new ColumnConfig(TrnFieldMapping.SRC_FIELD, 200);
		srcFieldCol.setHeader("Source Field");
		final ComboBox<BAField> srcFieldCombo = AdminUtils.getFieldsCombo();
		srcFieldCol.setEditor(new CellEditor(srcFieldCombo));
		srcFieldCol.setRenderer(fieldCellRenderer);
		cm.getColumns().add(srcFieldCol);
		
		this.addListener(Events.BeforeEdit, new Listener<GridEvent<TrnFieldMapping>>(){
			private BusinessAreaClient lastSrcBA;
			public void handleEvent(GridEvent<TrnFieldMapping> be) {
				if(be.getProperty().equals(TrnFieldMapping.SRC_FIELD)){
					final BusinessAreaClient ba = be.getModel().getSrcBA();
					if(ba == null || (ba != null && (lastSrcBA == null || !lastSrcBA.equals(ba)))){
						lastSrcBA = ba;
						srcFieldCombo.getStore().removeAll();
						if(ba != null){
							AdminUtils.dbService.getFields(ba.getSystemPrefix(), new AsyncCallback<List<BAField>>(){
								public void onFailure(Throwable caught) {
									TbitsInfo.write("Error fetching fields", TbitsInfo.ERROR);
									Log.error("Error fetching fields", caught);
								}
			
								public void onSuccess(List<BAField> result) {
									if(result != null){
										srcFieldCombo.getStore().add(result);
									}
								}});
						}
					}
				}
			}});
		
		ColumnConfig targetBACol = new ColumnConfig(TrnFieldMapping.TARGET_BA, 200);
		targetBACol.setHeader("Target BA");
		targetBACol.setEditor(new CellEditor(AdminUtils.getBACombo()));
		targetBACol.setRenderer(baCellRenderer);
		cm.getColumns().add(targetBACol);
		
		ColumnConfig targetFieldCol = new ColumnConfig(TrnFieldMapping.TARGET_FIELD, 200);
		targetFieldCol.setHeader("Target Field");
		final ComboBox<BAField> targetFieldCombo = AdminUtils.getFieldsCombo();
		targetFieldCol.setEditor(new CellEditor(targetFieldCombo));
		targetFieldCol.setRenderer(fieldCellRenderer);
		cm.getColumns().add(targetFieldCol);
		
		this.addListener(Events.BeforeEdit, new Listener<GridEvent<TrnFieldMapping>>(){
			private BusinessAreaClient lastTargetBA;
			public void handleEvent(GridEvent<TrnFieldMapping> be) {
				if(be.getProperty().equals(TrnFieldMapping.TARGET_FIELD)){
					final BusinessAreaClient ba = be.getModel().getTargetBA();
					if(ba == null || (ba != null && (lastTargetBA == null || !lastTargetBA.equals(ba)))){
						lastTargetBA = ba;
						targetFieldCombo.getStore().removeAll();
						if(ba != null){
							AdminUtils.dbService.getFields(ba.getSystemPrefix(), new AsyncCallback<List<BAField>>(){
								public void onFailure(Throwable caught) {
									TbitsInfo.write("Error fetching fields", TbitsInfo.ERROR);
									Log.error("Error fetching fields", caught);
								}
			
								public void onSuccess(List<BAField> result) {
									if(result != null){
										targetFieldCombo.getStore().add(result);
									}
								}});
						}
					}
				}
			}});
	}

}
