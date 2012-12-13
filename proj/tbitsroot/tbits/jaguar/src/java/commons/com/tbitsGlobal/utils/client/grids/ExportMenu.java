package commons.com.tbitsGlobal.utils.client.grids;

import java.util.ArrayList;
import java.util.List;

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
import com.tbitsGlobal.jaguar.client.JaguarConstants;
import com.tbitsGlobal.jaguar.client.events.OnConsolidateRequests;

import commons.com.tbitsGlobal.utils.client.ClientUtils;
import commons.com.tbitsGlobal.utils.client.GlobalConstants;
import commons.com.tbitsGlobal.utils.client.TbitsInfo;
import commons.com.tbitsGlobal.utils.client.TbitsTreeRequestData;
import commons.com.tbitsGlobal.utils.client.Events.TbitsEventRegister;
import commons.com.tbitsGlobal.utils.client.Events.TbitsObservable;
import commons.com.tbitsGlobal.utils.client.UIContext.UIContext;
import commons.com.tbitsGlobal.utils.client.log.Log;
import commons.com.tbitsGlobal.utils.client.search.grids.AbstractSearchGrid;
import commons.com.tbitsGlobal.utils.client.widgets.ToolBarButton;
/**
 *  @author mukesh
 *	
 *	This class is used for creating Menu of all export button for toolbar of request search grid panel.
 *	three items Export To New Tab,Export To CSV,Export Multiple Pages To CSV has been added in this menu.
 *	so menu item selection handler has been defined in this class.
 *  
 */
public class ExportMenu extends ToolBarButton{
	
	
//	private String CONTEXT_GRID;
//	private UIContext myContext;
//	public ExportMenu(String name,UIContext  myContext,String CONTEXT_GRID)
//	{
//		super(name);
//		this.myContext=myContext;
//		this.CONTEXT_GRID=CONTEXT_GRID;
//		makeNewExportEmportMenu();
//	}
//
//	private void makeNewExportEmportMenu()
//	{
//		Menu exportImportMenu = new Menu();
//		exportImportMenu.setMaxHeight(300);
//		 
//		exportImportMenu.add(addExportToCSV());
//		exportImportMenu.add(new SeparatorMenuItem());
//		
//		exportImportMenu.add(addExportToNewTab());
//		exportImportMenu.add(new SeparatorMenuItem());
//
//		
//		exportImportMenu.add(addExportMultiplePagesToCSV());
//		
//		this.setMenu(exportImportMenu);
//		this.setToolTip("Export");
//
//	}
//	
//	
//	private MenuItem addExportToCSV()
//	{
//		MenuItem exportToCSVItem =new MenuItem("<b>Export To CSV</b>");
//		exportToCSVItem.setEnabled(true);
//		
//		exportToCSVItem.addSelectionListener(new SelectionListener<MenuEvent>(){
//		public void componentSelected(MenuEvent ce){
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
//			
//		}
//		
//		});
//			
//		return exportToCSVItem;
//		
//	}
//
//	private MenuItem addExportToNewTab()
//	{
//		MenuItem exportToNewTabItem =new MenuItem("<b>Export To New Tab</b>");
//		exportToNewTabItem.addSelectionListener(new SelectionListener<MenuEvent>() {
//
//			@Override
//			public void componentSelected(MenuEvent ce) {
//				RequestsViewGrid grid = myContext.getValue(CONTEXT_GRID, RequestsViewGrid.class);
//				if(grid != null){
//					List<TbitsTreeRequestData> data = grid.getSelectionModel().getSelectedItems();
//					if(data == null || data.size() == 0)
//						data = grid.getTreeStore().getAllItems();
//					TbitsEventRegister.getInstance().fireEvent(new OnConsolidateRequests(grid.getSysPrefix(), data));
//				}
//				
//			}
//		});
//
//		return exportToNewTabItem;
//		
//	}
//	
//	
//	private MenuItem addExportMultiplePagesToCSV()
//	{
//		MenuItem exportMultiplePagesToCSVItem =new MenuItem("<b>Export Multiple Pages To CSV</b>");
//		
//		exportMultiplePagesToCSVItem.addSelectionListener(new SelectionListener<MenuEvent>(){
//	
//		public void componentSelected(final MenuEvent ce) {
//			
//			final Dialog dialog = new Dialog(){
//				@Override
//				protected void onHide() {
//					super.onHide();
//					//ce.getButton().enable();
//				}
//				
//				@Override
//				protected void onShow() {
//					super.onShow();
//					//ce.getButton().disable();
//				}
//			};
//			dialog.setHeading("Export to CSV");
//			dialog.setLayout(new FitLayout());
//			dialog.setSize(400, 150);
//			dialog.setButtons(Dialog.OK);
//			
//			dialog.getHeader().addTool(new ToolButton("x-tool-help", new SelectionListener<IconButtonEvent>(){
//				@Override
//				public void componentSelected(IconButtonEvent ce) {
//					MessageBox.info("Help!!", "Exports a specific number of records to CSV.<br />" +
//							"Number of records indicate the number of records that would be exported.<br />" +
//							"Page Number is used to determine the range of records.<br />" +
//							"e.g. Number of records = 1000 and Page Number = 3 would export records in the range 2001 - 3000", null);
//				}}));
//			
//			FormPanel form = new FormPanel();
//			form.setHeaderVisible(false);
//			form.setBodyBorder(false);
//			form.setLabelWidth(150);
//			
//			final SpinnerField size = new SpinnerField();
//			size.setName("num");
//			size.setFieldLabel("Number of records");
//			size.setMaxValue(1000);
//			size.setMinValue(0);
//			size.setValue(1000);
//			size.getPropertyEditor().setFormat(NumberFormat.getDecimalFormat());
//			size.setToolTip("The maximum total number of records that can be exported at a time are 1000. " +
//					"This is to avoid sudden the database load. " +
//					"If it is required to export more results then export multiple times with different page number");
//			form.add(size, new FormData("100%"));
//			
//			final SpinnerField page = new SpinnerField();
//			page.setName("page");
//			page.setFieldLabel("Page Number");
//			page.setMinValue(1);
//			page.setValue(1);
//			page.getPropertyEditor().setFormat(NumberFormat.getDecimalFormat());
//			page.setToolTip("The nth page you want to export. " +
//					"Say you have specified 100 as \"Number of records\" and 5 as \"Page Number\", " +
//					"you will get the results from 4*100 + 1 to 5*100.");
//			form.add(page, new FormData("100%"));
//			
//			dialog.add(form, new FitData());
//			
//			dialog.show();
//			
//			dialog.getButtonById("ok").addSelectionListener(new SelectionListener<ButtonEvent>(){
//				@Override
//				public void componentSelected(ButtonEvent ce) {
//					int pageSize = size.getValue().intValue();
//					int pageNo = page.getValue().intValue();
//					
//					AbstractSearchGrid grid = myContext.getValue(CONTEXT_GRID, AbstractSearchGrid.class);
//					if(grid == null || grid.getDql() == null)
//						return;
//					List<ColumnConfig> columns = grid.getColumnModel().getColumns();
//					List<String> includedFields = new ArrayList<String>();
//					for(ColumnConfig col : columns){
//						includedFields.add(col.getId());
//					}
//					JaguarConstants.dbService.exportGrid(grid.getSysPrefix(), grid.getDql(), includedFields, pageSize, pageNo, 
//							new AsyncCallback<String>(){
//						public void onFailure(Throwable arg0) {
//							TbitsInfo.error("Error exporting the grid... Try Again!!!", arg0);
//							Log.error("Error exporting the grid...", arg0);
//						}
//
//						public void onSuccess(String result) {
//							if(result != null){
//								String url = ClientUtils.getUrlToFilefromBase(result);
//								Log.info("Exported CSV available at : " + url);
//								ClientUtils.showPreview(url);
//								
//								dialog.hide();
//							}
//							else
//								TbitsInfo.error("Error exporting the grid... Try Again!!!");
//						}
//					});
//					
//					ce.getButton().enable();
//				}});
//		
//		}
//		
//		});
//		
//		return exportMultiplePagesToCSVItem;
//		
//	}

}
