package transmittal.com.tbitsGlobal.client.admin.pages;

import java.util.List;

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

import transmittal.com.tbitsGlobal.client.admin.TrnAdminUtils;
import transmittal.com.tbitsGlobal.client.models.TrnValidationRule;
import commons.com.tbitsGlobal.utils.client.GlobalConstants;
import commons.com.tbitsGlobal.utils.client.TbitsCellEditor;
import commons.com.tbitsGlobal.utils.client.TbitsInfo;
import commons.com.tbitsGlobal.utils.client.bafield.BAField;
import commons.com.tbitsGlobal.utils.client.bulkupdate.BulkUpdateGridAbstract;
import commons.com.tbitsGlobal.utils.client.domainObjects.BusinessAreaClient;
import commons.com.tbitsGlobal.utils.client.log.Log;

/**
 * Grid to hold Trn Validation Rules Table
 * @author devashish
 *
 */
public class ValidationRulesGrid extends BulkUpdateGridAbstract<TrnValidationRule> {

	private BusinessAreaClient srcBA;
	
	public ValidationRulesGrid(BulkGridMode mode) {
		super(mode);
		
		showNumberer = false;
		showStatus	  = false;
	}
	
	private GridCellRenderer<TrnValidationRule> baCellRenderer = new GridCellRenderer<TrnValidationRule>(){
		public Object render(TrnValidationRule model, String property,
				ColumnData config, int rowIndex, int colIndex,
				ListStore<TrnValidationRule> store, Grid<TrnValidationRule> grid) {
			BusinessAreaClient ba = model.get(property);
			if(ba != null)
				return ba.getSystemPrefix() + " <" + ba.getSystemId() + ">";
			else return "";
		}};
	
	private GridCellRenderer<TrnValidationRule> fieldCellRenderer = new GridCellRenderer<TrnValidationRule>(){
		public Object render(TrnValidationRule model, String property,
				ColumnData config, int rowIndex, int colIndex,
				ListStore<TrnValidationRule> store, Grid<TrnValidationRule> grid) {
			BAField field = model.get(property);
			if(field != null)
				return field.getDisplayName() + " <" + field.getFieldId() + ">";
			else return "";
		}};

	protected void createColumns() {
		
		ColumnConfig fieldCol = new ColumnConfig(TrnValidationRule.FIELD, 200);
		fieldCol.setHeader("Field");
		final ComboBox<BAField> fieldCombo = TrnAdminUtils.getFieldsCombo();
		fieldCol.setEditor(new TbitsCellEditor(fieldCombo));
		fieldCol.setRenderer(fieldCellRenderer);
		cm.getColumns().add(fieldCol);
		
		this.addListener(Events.BeforeEdit, new Listener<GridEvent<TrnValidationRule>>(){
			private BusinessAreaClient lastSrcBA;
			public void handleEvent(GridEvent<TrnValidationRule> be) {
				if(be.getProperty().equals(TrnValidationRule.FIELD)){
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
			}
		});
		
		ColumnConfig valueConfig = new ColumnConfig(TrnValidationRule.RULE_VALUE, 300);
		valueConfig.setHeader("Value");
		valueConfig.setEditor(new TbitsCellEditor(new TextField<String>()));
		cm.getColumns().add(valueConfig);
	}
	
	public void setSrcBA(BusinessAreaClient srcBA) {
		this.srcBA = srcBA;
	}

	public BusinessAreaClient getSrcBA() {
		return srcBA;
	}
}
