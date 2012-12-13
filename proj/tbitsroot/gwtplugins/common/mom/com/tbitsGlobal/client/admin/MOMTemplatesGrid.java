package mom.com.tbitsGlobal.client.admin;

import java.util.List;


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
import com.google.gwt.user.client.rpc.AsyncCallback;

import mom.com.tbitsGlobal.client.admin.models.MOMTemplate;
import commons.com.tbitsGlobal.utils.client.GlobalConstants;
import commons.com.tbitsGlobal.utils.client.TbitsCellEditor;
import commons.com.tbitsGlobal.utils.client.TbitsInfo;
import commons.com.tbitsGlobal.utils.client.bafield.BAField;
import commons.com.tbitsGlobal.utils.client.bulkupdate.BulkUpdateGridAbstract;
import commons.com.tbitsGlobal.utils.client.domainObjects.BusinessAreaClient;
import commons.com.tbitsGlobal.utils.client.log.Log;

/**
 * Grid to hold mom template columns
 * @author devashish
 *
 */
public class MOMTemplatesGrid extends BulkUpdateGridAbstract<MOMTemplate> {

	private BusinessAreaClient momBa;
	
	private GridCellRenderer<MOMTemplate> fieldCellRenderer = new GridCellRenderer<MOMTemplate>(){
		public Object render(MOMTemplate model, String property, ColumnData config, int rowIndex, int colIndex,
				ListStore<MOMTemplate> store, Grid<MOMTemplate> grid) {
			BAField field = model.get(property);
			if(field != null)
				return field.getDisplayName() + " <" + field.getFieldId() + ">";
			else return "";
	}};
	
	public MOMTemplatesGrid(BulkGridMode mode) {
		super(mode);
		
		showNumberer = false;
		showStatus	 = false;
	}

	protected void createColumns() {
		ColumnConfig fieldCol = new ColumnConfig(MOMTemplate.MOM_FIELD, 200);
		fieldCol.setHeader("Field");
		final ComboBox<BAField> fieldCombo = this.getFieldsCombo();
		fieldCol.setEditor(new TbitsCellEditor(fieldCombo));
		fieldCol.setRenderer(fieldCellRenderer);
		cm.getColumns().add(fieldCol);
		
		this.addListener(Events.BeforeEdit, new Listener<GridEvent<MOMTemplate>>(){
			private BusinessAreaClient lastSrcBA;
			public void handleEvent(GridEvent<MOMTemplate> be) {
				if(be.getProperty().equals(MOMTemplate.MOM_FIELD)){
					if(momBa == null || (momBa != null && (lastSrcBA == null || !lastSrcBA.equals(momBa)))){
						lastSrcBA = momBa;
						fieldCombo.getStore().removeAll();
						if(momBa != null){
							GlobalConstants.utilService.getFields(momBa.getSystemPrefix(), new AsyncCallback<List<BAField>>(){
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
		
		ColumnConfig dataTypeCol = new ColumnConfig(MOMTemplate.MOM_TYPE_ID, 100);
		dataTypeCol.setHeader("Type Id");
		dataTypeCol = this.getIntegerColConfig(dataTypeCol);
		cm.getColumns().add(dataTypeCol);
		
		ColumnConfig isMeetingCol = new ColumnConfig(MOMTemplate.IS_MEETING, 100);
		isMeetingCol.setHeader("Is Meeting");
		isMeetingCol = this.getIntegerColConfig(isMeetingCol);
		cm.getColumns().add(isMeetingCol);
		
		ColumnConfig templateCol = new ColumnConfig(MOMTemplate.MOM_TEMPLATE, 300);
		templateCol.setHeader("Template");
		templateCol.setEditor(new TbitsCellEditor(new TextField<String>()));
		cm.getColumns().add(templateCol);
	}
	
	
	public void setMOMBa(BusinessAreaClient ba){
		this.momBa = ba;
	}

	public BusinessAreaClient getMOMBa(){
		return this.momBa;
	}
	
	public  ComboBox<BAField> getFieldsCombo(){
		final ListStore<BAField> fieldStore = new ListStore<BAField>();
		ComboBox<BAField> fieldCombo = new ComboBox<BAField>();
		fieldCombo.setStore(fieldStore);
		fieldCombo.setDisplayField(BAField.DISPLAY_NAME);
		return fieldCombo;
	}
	

	/**
	 * Get the column config for a Number Field
	 * @param config
	 * @return
	 */
	public  ColumnConfig getIntegerColConfig(ColumnConfig config){
		final TextField<String> field = new TextField<String>();
		field.setAllowBlank(false);
		config.setEditor(new CellEditor(field){
			public Object postProcessValue(Object value) {
				try{
					if(value instanceof String)
						return Integer.parseInt((String) value);
					return (Integer)value;
				}catch(Exception e){
					return 0;
				}
			}
			
			public Object preProcessValue(Object value) {
				if(value instanceof Integer){
					return (Integer)value + "";
				}
				return super.preProcessValue(value);
			}
		});
		return config;
	}
}
