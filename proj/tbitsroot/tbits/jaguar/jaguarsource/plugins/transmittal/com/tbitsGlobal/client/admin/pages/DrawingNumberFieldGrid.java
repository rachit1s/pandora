package transmittal.com.tbitsGlobal.client.admin.pages;

import java.util.List;

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

import transmittal.com.tbitsGlobal.client.admin.TrnAdminUtils;
import transmittal.com.tbitsGlobal.client.models.TrnDrawingNumber;
import commons.com.tbitsGlobal.utils.client.GlobalConstants;
import commons.com.tbitsGlobal.utils.client.TbitsCellEditor;
import commons.com.tbitsGlobal.utils.client.TbitsInfo;
import commons.com.tbitsGlobal.utils.client.bafield.BAField;
import commons.com.tbitsGlobal.utils.client.bulkupdate.BulkUpdateGridAbstract;
import commons.com.tbitsGlobal.utils.client.domainObjects.BusinessAreaClient;
import commons.com.tbitsGlobal.utils.client.log.Log;

public class DrawingNumberFieldGrid extends BulkUpdateGridAbstract<TrnDrawingNumber> {

	public DrawingNumberFieldGrid(BulkGridMode mode) {
		super(mode);
		showNumberer  = false;
		showStatus	  = false;
	}
	
	/*
	 * Cell renderer for Business area 
	 */
	private GridCellRenderer<TrnDrawingNumber> baCellRenderer = new GridCellRenderer<TrnDrawingNumber>(){
		
		public Object render(TrnDrawingNumber model, String property, ColumnData config, int rowIndex, int colIndex,
				ListStore<TrnDrawingNumber> store, Grid<TrnDrawingNumber> grid) {
			BusinessAreaClient ba = model.get(property);
			if(ba != null){
				return ba.getSystemPrefix() + "<" + ba.getSystemId() + ">";
			}else return "";
		}
	};
	
	/*
	 * Cell Renderer for Field
	 */
	private GridCellRenderer<TrnDrawingNumber> fieldCellRenderer = new GridCellRenderer<TrnDrawingNumber>(){
		
		public Object render(TrnDrawingNumber model, String property, ColumnData config, int rowIndex, int colIndex,
				ListStore<TrnDrawingNumber> store, Grid<TrnDrawingNumber> grid) {
			BAField field = model.get(property);
			if(field != null){
				return field.getName() + "<" + field.getFieldId() + ">";
			}else return "";
		}
		
	};

	@Override
	protected void createColumns() {
		ColumnConfig srcSysIdCol = new ColumnConfig(TrnDrawingNumber.SRC_BA, 200);
		srcSysIdCol.setHeader("BA");
		ComboBox<BusinessAreaClient> baCombo = TrnAdminUtils.getBACombo();
		srcSysIdCol.setEditor(new TbitsCellEditor(baCombo));
		srcSysIdCol.setRenderer(baCellRenderer);
		cm.getColumns().add(srcSysIdCol);
		
		
		ColumnConfig fieldCol = new ColumnConfig(TrnDrawingNumber.FIELD_NAME, 200);
		fieldCol.setHeader("Field");
		final ComboBox<BAField> fieldCombo = TrnAdminUtils.getFieldsCombo();
		fieldCol.setEditor(new TbitsCellEditor(fieldCombo));
		fieldCol.setRenderer(fieldCellRenderer);
		cm.getColumns().add(fieldCol);
		
		this.addListener(Events.BeforeEdit, new Listener<GridEvent<TrnDrawingNumber>>(){
			private BusinessAreaClient lastBa;
			@Override
			public void handleEvent(GridEvent<TrnDrawingNumber> be) {
				if(be.getProperty().equals(TrnDrawingNumber.FIELD_NAME)){
					final BusinessAreaClient ba = be.getModel().getSrcBa();
					if(ba == null || (ba != null && (lastBa == null || !lastBa.equals(ba)))){
						lastBa = ba;
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
			}
			
		});
	}

}
