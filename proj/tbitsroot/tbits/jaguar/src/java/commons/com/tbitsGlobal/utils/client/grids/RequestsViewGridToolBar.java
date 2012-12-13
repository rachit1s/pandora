package commons.com.tbitsGlobal.utils.client.grids;

import java.util.ArrayList;
import java.util.List;

import com.extjs.gxt.ui.client.data.ModelData;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.IconButtonEvent;
import com.extjs.gxt.ui.client.event.MenuEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.Dialog;
import com.extjs.gxt.ui.client.widget.MessageBox;
import com.extjs.gxt.ui.client.widget.button.ToolButton;
import com.extjs.gxt.ui.client.widget.form.FormPanel;
import com.extjs.gxt.ui.client.widget.form.SpinnerField;
import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
import com.extjs.gxt.ui.client.widget.layout.FitData;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.layout.FormData;
import com.extjs.gxt.ui.client.widget.menu.Menu;
import com.extjs.gxt.ui.client.widget.menu.MenuItem;
import com.extjs.gxt.ui.client.widget.menu.SeparatorMenuItem;
import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.extjs.gxt.ui.client.widget.button.SplitButton;
import com.tbitsGlobal.jaguar.client.JaguarConstants;
import com.tbitsGlobal.jaguar.client.events.OnConsolidateRequests;

import commons.com.tbitsGlobal.utils.client.ClientUtils;
import commons.com.tbitsGlobal.utils.client.GlobalConstants;
import commons.com.tbitsGlobal.utils.client.IFixedFields;
import commons.com.tbitsGlobal.utils.client.TbitsInfo;
import commons.com.tbitsGlobal.utils.client.TbitsTreeRequestData;
import commons.com.tbitsGlobal.utils.client.Events.TbitsEventRegister;
import commons.com.tbitsGlobal.utils.client.UIContext.UIContext;
import commons.com.tbitsGlobal.utils.client.log.Log;
import commons.com.tbitsGlobal.utils.client.pojo.POJO;
import commons.com.tbitsGlobal.utils.client.pojo.POJOInt;
import commons.com.tbitsGlobal.utils.client.search.grids.AbstractSearchGrid;
import commons.com.tbitsGlobal.utils.client.widgets.ToolBarButton;

/**
 * 
 * @author sourabh
 * 
 * ToolBar for any generic request grid
 */
public class RequestsViewGridToolBar extends TbitsToolBar{
	
	/**
	 * Export to CSV Button.
	 */
//	protected final ToolBarButton EXPORT = new ToolBarButton("Export to CSV", new SelectionListener<ButtonEvent>(){
//		@Override
//		public void componentSelected(ButtonEvent ce) {
//			RequestsViewGrid grid = myContext.getValue(CONTEXT_GRID, RequestsViewGrid.class);
//			if(grid == null)
//				return;
//			List<TbitsTreeRequestData> data = grid.getSelectionModel().getSelectedItems();
//			if(data.size() == 0){
//				data = grid.getTreeStore().getAllItems();
//			}
//			List<ColumnConfig> columns = grid.getColumnModel().getColumns();
//			List<String> includedFields = new ArrayList<String>();
//			for(ColumnConfig col : columns){
//				includedFields.add(col.getId());
//			}
//			GlobalConstants.utilService.exportGrid(grid.getSysPrefix(), data, includedFields, new AsyncCallback<String>(){
//				public void onFailure(Throwable arg0) {
//					TbitsInfo.error("Error exporting the grid... Try Again!!!", arg0);
//					Log.error("Error exporting the grid...", arg0);
//				}
//
//				public void onSuccess(String result) {
//					if(result != null){
//						String url = ClientUtils.getUrlToFilefromBase(result);
//						Log.info("Exported CSV available at : " + url);
//						ClientUtils.showPreview(url);
//					}
//					else
//						TbitsInfo.error("Error exporting the grid... Try Again!!!");
//				}
//			});
//		}});
	
	private final ToolBarButton COPY = new ToolBarButton("Copy", new SelectionListener<ButtonEvent>(){
		@Override
		public void componentSelected(ButtonEvent ce) {
			copyToClipboard(false);
		}});
	
	private final ToolBarButton PASTE = new ToolBarButton("Paste", new SelectionListener<ButtonEvent>(){
		@Override
		public void componentSelected(ButtonEvent ce) {
			RequestsViewGrid grid = myContext.getValue(CONTEXT_GRID, RequestsViewGrid.class);
			if(grid == null)
				return;
			if (GlobalConstants.requestClipboard != null) {
				int count = 0;
				for (ModelData model : GlobalConstants.requestClipboard.getChildren()) {
					int requestId = ((TbitsTreeRequestData) model).getRequestId();
					if(grid.getTreeStore().findModel(IFixedFields.REQUEST, requestId) == null){
						POJO parentRequest = ((TbitsTreeRequestData) model).getAsPOJO(IFixedFields.PARENT_REQUEST_ID);
						if(parentRequest != null){
							int parentRequestId = ((POJOInt)parentRequest).getValue();
							if(parentRequestId > 0){
								TbitsTreeRequestData parentModel = grid.getTreeStore().findModel(IFixedFields.REQUEST, parentRequestId);
								grid.getTreeStore().add(parentModel, (TbitsTreeRequestData) model, true);
								count++;
								continue;
							}
						}
						grid.addModel((TbitsTreeRequestData) model);
						count++;
					}
				}
				TbitsInfo.info(count + " items copied to the grid");
			} else
				TbitsInfo.info("Clipboard is empty");
		}});
	
	private final ToolBarButton DELETE = new ToolBarButton("Delete", new SelectionListener<ButtonEvent>(){
		@Override
		public void componentSelected(ButtonEvent ce) {
			RequestsViewGrid grid = myContext.getValue(CONTEXT_GRID, RequestsViewGrid.class);
			if(grid == null)
				return;
			List<TbitsTreeRequestData> data = grid.getSelectionModel().getSelectedItems();
			if(data != null){
				for(TbitsTreeRequestData model : data)
					grid.getTreeStore().remove(model);
			}
		}});
	
	
	public RequestsViewGridToolBar(String sysPrefix, UIContext parentContext) {
		super(sysPrefix, parentContext);
	}

	@Override
	protected void initializeButtons() {
		//this.addExportMenu();
			
	}
	
	/**
	 * Adds export to csv button.
	 */
//	protected void addExportButton(){
////		EXPORT.disable();
//		EXPORT.setToolTip("Export data in the grid to csv");
////		EXPORT.disableOn(OnChangeBA.class);
//		this.add(addExportButton);
//	}
	
	protected void addCopyButton(){
		COPY.setToolTip("Copies selected records in the grid to the clipboard");
		this.add(COPY);
	}
	
	protected void addPasteButton(){
		PASTE.setToolTip("Pastes records in the grid from the clipboard");
		this.add(PASTE);
	}
	
	protected void addDeleteButton(){
		DELETE.setToolTip("Deletes selected records from the grid");
		this.add(DELETE);
	}

}