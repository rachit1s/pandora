package transmittal.com.tbitsGlobal.client.admin.pages;

import java.util.List;

import transmittal.com.tbitsGlobal.client.admin.TrnAdminUtils;
import transmittal.com.tbitsGlobal.client.models.TrnFieldMapping;

import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.GridEvent;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.form.ComboBox;
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

/**
 * Grid to hold Transmittal Source Target Field Map table
 * @author devashish
 *
 */
public class SrcTargetFieldMapGrid extends BulkUpdateGridAbstract<TrnFieldMapping>{
	
	public SrcTargetFieldMapGrid(BulkGridMode mode) {
		super(mode);

		showNumberer = false;
		showStatus	  = false;
	}
	
	
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
	

	/**
	 * Define Column Configuration
	 */
	protected void createColumns(){
		ColumnConfig srcBACol = new ColumnConfig(TrnFieldMapping.SRC_BA, 200);
		srcBACol.setHeader("Source BA");
		srcBACol.setEditor(new TbitsCellEditor(TrnAdminUtils.getBACombo()));
		srcBACol.setRenderer(baCellRenderer);
		cm.getColumns().add(srcBACol);
		
		ColumnConfig srcFieldCol = new ColumnConfig(TrnFieldMapping.SRC_FIELD, 200);
		srcFieldCol.setHeader("Source Field");
		final ComboBox<BAField> srcFieldCombo = TrnAdminUtils.getFieldsCombo();
		srcFieldCol.setEditor(new TbitsCellEditor(srcFieldCombo));
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
							GlobalConstants.utilService.getFields(ba.getSystemPrefix(), new AsyncCallback<List<BAField>>(){
								public void onFailure(Throwable caught) {
									TbitsInfo.error("Error fetching fields", caught);
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
		targetBACol.setEditor(new TbitsCellEditor(TrnAdminUtils.getBACombo()));
		targetBACol.setRenderer(baCellRenderer);
		cm.getColumns().add(targetBACol);
		
		ColumnConfig targetFieldCol = new ColumnConfig(TrnFieldMapping.TARGET_FIELD, 200);
		targetFieldCol.setHeader("Target Field");
		final ComboBox<BAField> targetFieldCombo = TrnAdminUtils.getFieldsCombo();
		targetFieldCol.setEditor(new TbitsCellEditor(targetFieldCombo));
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
							GlobalConstants.utilService.getFields(ba.getSystemPrefix(), new AsyncCallback<List<BAField>>(){
								public void onFailure(Throwable caught) {
									TbitsInfo.error("Error fetching fields", caught);
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
