package admin.com.tbitsglobal.admin.client.trn.bulkgrids;

import java.util.List;

import admin.com.tbitsglobal.admin.client.AdminUtils;
import admin.com.tbitsglobal.admin.client.trn.models.TrnPostProcessValue;
import bulkupdate.com.tbitsglobal.bulkupdate.client.BulkUpdateGridAbstract;

import com.allen_sauer.gwt.log.client.Log;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.GridEvent;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.form.ComboBox;
import com.extjs.gxt.ui.client.widget.form.TextField;
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

public class PostProcessBulkGrid extends BulkUpdateGridAbstract<TrnPostProcessValue>{
	
	private GridCellRenderer<TrnPostProcessValue> baCellRenderer = new GridCellRenderer<TrnPostProcessValue>(){
		public Object render(TrnPostProcessValue model, String property,
				ColumnData config, int rowIndex, int colIndex,
				ListStore<TrnPostProcessValue> store, Grid<TrnPostProcessValue> grid) {
			BusinessAreaClient ba = model.get(property);
			if(ba != null)
				return ba.getSystemPrefix() + " <" + ba.getSystemId() + ">";
			else return "";
		}};
		
	private GridCellRenderer<TrnPostProcessValue> fieldCellRenderer = new GridCellRenderer<TrnPostProcessValue>(){
		public Object render(TrnPostProcessValue model, String property,
				ColumnData config, int rowIndex, int colIndex,
				ListStore<TrnPostProcessValue> store, Grid<TrnPostProcessValue> grid) {
			BAField field = model.get(property);
			if(field != null)
				return field.getDisplayName() + " <" + field.getFieldId() + ">";
			else return "";
		}};
	
	public PostProcessBulkGrid(int mode) {
		super(mode);

		showNumberer = true;
	}
	
	@Override
	protected void onRender(Element target, int index) {
		super.onRender(target, index);
		
		this.createColumnModel();
	}
	
	protected void createColumnModel(){
		ColumnConfig targetBACol = new ColumnConfig(TrnPostProcessValue.TARGET_BA, 200);
		targetBACol.setHeader("Target BA");
		ComboBox<BusinessAreaClient> baCombo = AdminUtils.getBACombo(); 
		targetBACol.setEditor(new CellEditor(baCombo));
		targetBACol.setRenderer(baCellRenderer);
		cm.getColumns().add(targetBACol);
		
		ColumnConfig targetFieldCol = new ColumnConfig(TrnPostProcessValue.TARGET_FIELD, 200);
		targetFieldCol.setHeader("Target Field");
		final ComboBox<BAField> fieldCombo = AdminUtils.getFieldsCombo();
		targetFieldCol.setEditor(new CellEditor(fieldCombo));
		targetFieldCol.setRenderer(fieldCellRenderer);
		cm.getColumns().add(targetFieldCol);
		
		this.addListener(Events.BeforeEdit, new Listener<GridEvent<TrnPostProcessValue>>(){
			private BusinessAreaClient lastTargetBA;
			public void handleEvent(GridEvent<TrnPostProcessValue> be) {
				if(be.getProperty().equals(TrnPostProcessValue.TARGET_FIELD)){
					final BusinessAreaClient ba = be.getModel().getTargetBA();
					if(ba == null || (ba != null && (lastTargetBA == null || !lastTargetBA.equals(ba)))){
						lastTargetBA = ba;
						fieldCombo.getStore().removeAll();
						if(ba != null){
							AdminUtils.dbService.getFields(ba.getSystemPrefix(), new AsyncCallback<List<BAField>>(){
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
		
		ColumnConfig targetValueCol = new ColumnConfig(TrnPostProcessValue.TARGET_VALUE, 200);
		targetValueCol.setHeader("Target Field Value");
		targetValueCol.setEditor(new CellEditor(new TextField<String>()));
		cm.getColumns().add(targetValueCol);
		
//		ColumnConfig tempCol = new ColumnConfig(TrnPostProcessValue.TEMP, 70);
//		tempCol.setHeader("Temp");
//		tempCol.setEditor(new CellEditor(new NumberField()){
//			@Override
//			public Object postProcessValue(Object value) {
//				return ((Number)super.postProcessValue(value)).intValue();
//			}
//		});
//		cm.getColumns().add(tempCol);
	}

}
