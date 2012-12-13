package transmittal.com.tbitsGlobal.client.admin.pages;

import java.util.List;

import transmittal.com.tbitsGlobal.client.admin.TrnAdminUtils;
import transmittal.com.tbitsGlobal.client.models.TrnPostProcessValue;

import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.GridEvent;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.form.ComboBox;
import com.extjs.gxt.ui.client.widget.form.TextField;
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
 * Grid which holds post transmittal field map values
 * @author devashish
 *
 */
public class PostTrnFieldMapGrid extends BulkUpdateGridAbstract<TrnPostProcessValue>{
	
	public PostTrnFieldMapGrid(BulkGridMode mode) {
		super(mode);

		showNumberer  = false;
		showStatus	  = false;
	}
	

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
	
	/**
	 * Create the coulumn config for the grid	
	 */
	protected void createColumns(){
		ColumnConfig targetBACol = new ColumnConfig(TrnPostProcessValue.TARGET_BA, 200);
		targetBACol.setHeader("Target BA");
		ComboBox<BusinessAreaClient> baCombo = TrnAdminUtils.getBACombo(); 
		targetBACol.setEditor(new TbitsCellEditor(baCombo));
		targetBACol.setRenderer(baCellRenderer);
		cm.getColumns().add(targetBACol);
		
		ColumnConfig targetFieldCol = new ColumnConfig(TrnPostProcessValue.TARGET_FIELD, 200);
		targetFieldCol.setHeader("Target Field");
		final ComboBox<BAField> fieldCombo = TrnAdminUtils.getFieldsCombo();
		targetFieldCol.setEditor(new TbitsCellEditor(fieldCombo));
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
							GlobalConstants.utilService.getFields(ba.getSystemPrefix(), new AsyncCallback<List<BAField>>(){
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
		
		ColumnConfig targetValueCol = new ColumnConfig(TrnPostProcessValue.TARGET_VALUE, 200);
		targetValueCol.setHeader("Target Field Value");
		targetValueCol.setEditor(new TbitsCellEditor(new TextField<String>()));
		cm.getColumns().add(targetValueCol);
		
	}
}
