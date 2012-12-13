package com.tbitsGlobal.jaguar.client.searchgrid;

import java.util.ArrayList;
import java.util.List;

import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.IconButtonEvent;
import com.extjs.gxt.ui.client.event.MenuEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.Dialog;
import com.extjs.gxt.ui.client.widget.MessageBox;
import com.extjs.gxt.ui.client.widget.TabItem;
import com.extjs.gxt.ui.client.widget.TabPanel;
import com.extjs.gxt.ui.client.widget.Window;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.button.ToolButton;
import com.extjs.gxt.ui.client.widget.form.FileUploadField;
import com.extjs.gxt.ui.client.widget.form.FormPanel;
import com.extjs.gxt.ui.client.widget.form.FormPanel.Encoding;
import com.extjs.gxt.ui.client.widget.form.FormPanel.Method;
import com.extjs.gxt.ui.client.widget.form.HiddenField;
import com.extjs.gxt.ui.client.widget.form.SpinnerField;
import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
import com.extjs.gxt.ui.client.widget.layout.FitData;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.layout.FormData;
import com.extjs.gxt.ui.client.widget.menu.Menu;
import com.extjs.gxt.ui.client.widget.menu.MenuItem;
import com.extjs.gxt.ui.client.widget.menu.SeparatorMenuItem;
import com.extjs.gxt.ui.client.widget.toolbar.SeparatorToolItem;
import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.tbitsGlobal.jaguar.client.JaguarConstants;
import com.tbitsGlobal.jaguar.client.bulkupdate.BulkUpdatePanel;
import com.tbitsGlobal.jaguar.client.events.OnConsolidateRequests;
import com.tbitsGlobal.jaguar.client.events.ToAddNewRequest;
import com.tbitsGlobal.jaguar.client.events.ToBulkUpdate;
import com.tbitsGlobal.jaguar.client.events.ToViewDrafts;
import com.tbitsGlobal.jaguar.client.plugins.IWizardPlugin;
import com.tbitsGlobal.jaguar.client.plugins.slots.WizardPluginSlot;
import com.tbitsGlobal.jaguar.client.state.AppState;
import commons.com.tbitsGlobal.utils.client.Captions;
import commons.com.tbitsGlobal.utils.client.ClientUtils;
import commons.com.tbitsGlobal.utils.client.GlobalConstants;
import commons.com.tbitsGlobal.utils.client.TbitsInfo;
import commons.com.tbitsGlobal.utils.client.TbitsNativePreviewHandler;
import commons.com.tbitsGlobal.utils.client.TbitsTreeRequestData;
import commons.com.tbitsGlobal.utils.client.Events.ITbitsEventHandle;
import commons.com.tbitsGlobal.utils.client.Events.KeyboardShortcutEvent;
import commons.com.tbitsGlobal.utils.client.Events.OnChangeBA;
import commons.com.tbitsGlobal.utils.client.Events.OnFieldsReceived;
import commons.com.tbitsGlobal.utils.client.Events.TbitsEventRegister;
import commons.com.tbitsGlobal.utils.client.UIContext.UIContext;
import commons.com.tbitsGlobal.utils.client.bafield.BAField;
import commons.com.tbitsGlobal.utils.client.cache.CacheRepository;
import commons.com.tbitsGlobal.utils.client.cache.FieldCache;
import commons.com.tbitsGlobal.utils.client.domainObjects.PermissionClient;
import commons.com.tbitsGlobal.utils.client.domainObjects.UserDraftClient;
import commons.com.tbitsGlobal.utils.client.fieldconfigs.IFieldConfig;
import commons.com.tbitsGlobal.utils.client.grids.RequestsViewGrid;
import commons.com.tbitsGlobal.utils.client.grids.RequestsViewGridToolBar;
import commons.com.tbitsGlobal.utils.client.log.Log;
import commons.com.tbitsGlobal.utils.client.plugins.GWTPluginRegister;
import commons.com.tbitsGlobal.utils.client.search.grids.AbstractSearchGrid;
import commons.com.tbitsGlobal.utils.client.widgets.ToolBarButton;

/**
 * 
 * @author sourabh
 * 
 * Toolbar to be shown at the top of search grids.
 */
public class SearchToolBar extends RequestsViewGridToolBar{
	
	
	//=====================================================================================================
	
	/**
	 * we are using Export menu for all export button, here we have created one button and add 
	 * one menu which contain three menu item for Export To CSV,Export To New Tab, and Export Multiple Pages To CSV.
	 * item selection funtionality also added. 
	 * 
	 * @param sysPrefix
	 * @param parentContext
	 */
	
	private final ToolBarButton EXPORT_MENU=new ToolBarButton("Export");
		
	protected void addExportMenu(){
		Menu exportImportMenu = new Menu();
		exportImportMenu.setMaxHeight(300);
		 
		exportImportMenu.add(addExportToCSV());
		exportImportMenu.add(new SeparatorMenuItem());
		
		exportImportMenu.add(addExportToNewTab());
		exportImportMenu.add(new SeparatorMenuItem());

		
		exportImportMenu.add(addExportMultiplePagesToCSV());
		
		EXPORT_MENU.setMenu(exportImportMenu);	
		EXPORT_MENU.setToolTip("Export");
		this.add(EXPORT_MENU);
	}
	
	private MenuItem addExportToCSV()
	{
		MenuItem exportToCSVItem =new MenuItem("<b>Export To CSV</b>");
		exportToCSVItem.setEnabled(true);
		
		exportToCSVItem.addSelectionListener(new SelectionListener<MenuEvent>(){
		public void componentSelected(MenuEvent ce){
			RequestsViewGrid grid = myContext.getValue(CONTEXT_GRID, RequestsViewGrid.class);
			if(grid == null)
				return;
			List<TbitsTreeRequestData> data = grid.getSelectionModel().getSelectedItems();
			if(data.size() == 0){
				data = grid.getTreeStore().getAllItems();
			}
			List<ColumnConfig> columns = grid.getColumnModel().getColumns();
			List<String> includedFields = new ArrayList<String>();
			for(ColumnConfig col : columns){
				includedFields.add(col.getId());
			}
			GlobalConstants.utilService.exportGrid(grid.getSysPrefix(), data, includedFields, new AsyncCallback<String>(){
				public void onFailure(Throwable arg0) {
					TbitsInfo.error("Error exporting the grid... Try Again!!!", arg0);
					Log.error("Error exporting the grid...", arg0);
				}

				public void onSuccess(String result) {
					if(result != null){
						String url = ClientUtils.getUrlToFilefromBase(result);
						Log.info("Exported CSV available at : " + url);
						ClientUtils.showPreview(url);
					}
					else
						TbitsInfo.error("Error exporting the grid... Try Again!!!");
				}
			});
			
		}
		
		});
			
		return exportToCSVItem;
		
	}

	private MenuItem addExportToNewTab()
	{
		MenuItem exportToNewTabItem =new MenuItem("<b>Export To New Tab</b>");
		exportToNewTabItem.addSelectionListener(new SelectionListener<MenuEvent>() {

			@Override
			public void componentSelected(MenuEvent ce) {
				RequestsViewGrid grid = myContext.getValue(CONTEXT_GRID, RequestsViewGrid.class);
				if(grid != null){
					List<TbitsTreeRequestData> data = grid.getSelectionModel().getSelectedItems();
					if(data == null || data.size() == 0)
						data = grid.getTreeStore().getAllItems();
					TbitsEventRegister.getInstance().fireEvent(new OnConsolidateRequests(grid.getSysPrefix(), data));
				}
				
			}
		});

		return exportToNewTabItem;
		
	}
	
	
	private MenuItem addExportMultiplePagesToCSV()
	{
		MenuItem exportMultiplePagesToCSVItem =new MenuItem("<b>Export Multiple Pages To CSV</b>");
		
		exportMultiplePagesToCSVItem.addSelectionListener(new SelectionListener<MenuEvent>(){
	
		public void componentSelected(final MenuEvent ce) {
			
			final Dialog dialog = new Dialog(){
				@Override
				protected void onHide() {
					super.onHide();
					//ce.getButton().enable();
				}
				
				@Override
				protected void onShow() {
					super.onShow();
					//ce.getButton().disable();
				}
			};
			dialog.setHeading("Export to CSV");
			dialog.setLayout(new FitLayout());
			dialog.setSize(400, 150);
			dialog.setButtons(Dialog.OK);
			
			dialog.getHeader().addTool(new ToolButton("x-tool-help", new SelectionListener<IconButtonEvent>(){
				@Override
				public void componentSelected(IconButtonEvent ce) {
					MessageBox.info("Help!!", "Exports a specific number of records to CSV.<br />" +
							"Number of records indicate the number of records that would be exported.<br />" +
							"Page Number is used to determine the range of records.<br />" +
							"e.g. Number of records = 1000 and Page Number = 3 would export records in the range 2001 - 3000", null);
				}}));
			
			FormPanel form = new FormPanel();
			form.setHeaderVisible(false);
			form.setBodyBorder(false);
			form.setLabelWidth(150);
			
			final SpinnerField size = new SpinnerField();
			size.setName("num");
			size.setFieldLabel("Number of records");
			size.setMaxValue(1000);
			size.setMinValue(0);
			size.setValue(1000);
			size.getPropertyEditor().setFormat(NumberFormat.getDecimalFormat());
			size.setToolTip("The maximum total number of records that can be exported at a time are 1000. " +
					"This is to avoid sudden the database load. " +
					"If it is required to export more results then export multiple times with different page number");
			form.add(size, new FormData("100%"));
			
			final SpinnerField page = new SpinnerField();
			page.setName("page");
			page.setFieldLabel("Page Number");
			page.setMinValue(1);
			page.setValue(1);
			page.getPropertyEditor().setFormat(NumberFormat.getDecimalFormat());
			page.setToolTip("The nth page you want to export. " +
					"Say you have specified 100 as \"Number of records\" and 5 as \"Page Number\", " +
					"you will get the results from 4*100 + 1 to 5*100.");
			form.add(page, new FormData("100%"));
			
			dialog.add(form, new FitData());
			
			dialog.show();
			
			dialog.getButtonById("ok").addSelectionListener(new SelectionListener<ButtonEvent>(){
				@Override
				public void componentSelected(ButtonEvent ce) {
					int pageSize = size.getValue().intValue();
					int pageNo = page.getValue().intValue();
					
					AbstractSearchGrid grid = myContext.getValue(CONTEXT_GRID, AbstractSearchGrid.class);
					if(grid == null || grid.getDql() == null)
						return;
					List<ColumnConfig> columns = grid.getColumnModel().getColumns();
					List<String> includedFields = new ArrayList<String>();
					for(ColumnConfig col : columns){
						includedFields.add(col.getId());
					}
					JaguarConstants.dbService.exportGrid(grid.getSysPrefix(), grid.getDql(), includedFields, pageSize, pageNo, 
							new AsyncCallback<String>(){
						public void onFailure(Throwable arg0) {
							TbitsInfo.error("Error exporting the grid... Try Again!!!", arg0);
							Log.error("Error exporting the grid...", arg0);
						}

						public void onSuccess(String result) {
							if(result != null){
								String url = ClientUtils.getUrlToFilefromBase(result);
								Log.info("Exported CSV available at : " + url);
								ClientUtils.showPreview(url);
								
								dialog.hide();
							}
							else
								TbitsInfo.error("Error exporting the grid... Try Again!!!");
						}
					});
					
					ce.getButton().enable();
				}});
		
		}
		
		});
		
		return exportMultiplePagesToCSVItem;
		
	}
	
	/**
	 * Export to CSV Button.
	 */
//	private final ToolBarButton EXPORT_ALL = new ToolBarButton("Export multiple pages to CSV", new SelectionListener<ButtonEvent>(){
//		@Override
//		public void componentSelected(final ButtonEvent ce) {
//			
//			final Dialog dialog = new Dialog(){
//				@Override
//				protected void onHide() {
//					super.onHide();
//					ce.getButton().enable();
//				}
//				
//				@Override
//				protected void onShow() {
//					super.onShow();
//					ce.getButton().disable();
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
//		}});
//	
//	private final ToolBarButton EXPORT_TO_CONSOLIDATE = new ToolBarButton("Export to new Tab", new SelectionListener<ButtonEvent>(){
//		@Override
//		public void componentSelected(ButtonEvent ce) {
//			RequestsViewGrid grid = myContext.getValue(CONTEXT_GRID, RequestsViewGrid.class);
//			if(grid != null){
//				List<TbitsTreeRequestData> data = grid.getSelectionModel().getSelectedItems();
//				if(data == null || data.size() == 0)
//					data = grid.getTreeStore().getAllItems();
//				TbitsEventRegister.getInstance().fireEvent(new OnConsolidateRequests(grid.getSysPrefix(), data));
//			}
//		}});
	
	/**
	 * The Bulk Update Button.
	 */
	private final ToolBarButton BULK_UPDATE = new ToolBarButton("Bulk Add/Update", new SelectionListener<ButtonEvent>(){
		@Override
		public void componentSelected(ButtonEvent ce) {
			RequestsViewGrid grid = myContext.getValue(CONTEXT_GRID, RequestsViewGrid.class);
			if(grid == null)
				return;
			List<TbitsTreeRequestData> selectedItems = getGridSelectedItems();
			
			FieldCache cache = CacheRepository.getInstance().getCache(FieldCache.class);
			for( TbitsTreeRequestData ttrd : selectedItems )
			{
				for(String fieldName : ttrd.getPropertyNames() )
				{
					BAField baf = cache.getObject(fieldName);
					if( baf != null && !baf.isSetEnabled() )
					{
						ttrd.remove(fieldName);
					}
				}
			}

			TbitsEventRegister.getInstance().fireEvent(new ToBulkUpdate(grid.getSysPrefix(), selectedItems));
		}});
	
	/**
	 * Import Data Button.
	 */
	private final ToolBarButton IMPORT_DATA = new ToolBarButton("Import Data", new SelectionListener<ButtonEvent>(){
		
		public void componentSelected(ButtonEvent ce) {
			Window window = new Window();
			window.setWidth(400);
			window.setHeight(250);
			window.setHeading("Import Data from CSV File");
			window.setModal(true);
			window.addText("<div style='margin:5px'>This wizard helps import bulk data (in CSV) to create or update requests</div>");
			final TabPanel tabPanel = new TabPanel();
			tabPanel.setBorderStyle(false);
			tabPanel.setHeight(200);
			final TabItem templateTab = new TabItem("Download Template");
			templateTab.addText("<div style='margin:5px'><p>Click '<a target='_blank' href='" + 
					ClientUtils.getUrlToFilefromBase("/importdata?ba=" + sysPrefix) + 
					"'>template.csv</a>' on this tab<br />" + 
			"Click 'Save to Disk' on the window prompt and click 'OK' Open the template<br />" + 
			"The first two rows on the template contain field names and field display names respectively.<br />" + 
			"Fill in data from the third row. You can use Microsoft Excel or any text editor to edit this file<br />" + 
			"To update a request instead of creating a new one, mention the request id in the 'request_id' column<br />" + 
			"Click 'Next.' This will take you to the 'Import Data' tab.</p></div>");
			tabPanel.add(templateTab);
			
			final TabItem importTab = new TabItem("Import Data");
//			importTab.setLayout(new FitLayout());
			importTab.addText("<div style='margin:5px'><p>Click 'Browse.' Select the file containing data in the template format.<br />" + 
			"Click 'Next.' This will upload the data and create or update requests. This may take some time.<br />" + 
			"Click 'Save As' on the window prompt to save the uploaded results.<br />" + 
			"NB: Uploading the data may take some time. You can open tbits in a new browser window to check its progress</p></div>");
			
			final FormPanel form = new FormPanel();
			form.setHeaderVisible(false);
			form.setBodyBorder(false);
			form.setEncoding(Encoding.MULTIPART);
			form.setAction(ClientUtils.getUrlToFilefromBase("/importdata"));
			form.setMethod(Method.POST);
			
			HiddenField<String> hiddenBa = new HiddenField<String>();
			hiddenBa.setName("ba");
			hiddenBa.setValue(ClientUtils.getSysPrefix());
			form.add(hiddenBa);
			
			FileUploadField uploadField = new FileUploadField();
			uploadField.setName("up");
			uploadField.setFieldLabel("Upload CSV");
			form.add(uploadField, new FormData("-20"));
			
			importTab.add(form);
			tabPanel.add(importTab);
			
			window.add(tabPanel);
			
			Button next = new Button("Next", new SelectionListener<ButtonEvent>(){
				
				public void componentSelected(ButtonEvent ce) {
					if(tabPanel.getSelectedItem().equals(templateTab)){
						tabPanel.setSelection(importTab);
					}else{
//						formPanel.submit();
						form.submit();
					}
				}});
			window.addButton(next);
			window.show();
		}});
	
	/**
	 * Drafts Button.
	 */
	private final ToolBarButton DRAFTS = new ToolBarButton("View Drafts" , new SelectionListener<ButtonEvent>(){
		
		public void componentSelected(ButtonEvent ce) {
			TbitsEventRegister.getInstance().fireEvent(new ToViewDrafts());
		}});
	
	/**
	 * New Request Button.
	 */
	private final ToolBarButton NEW_REQUEST = new ToolBarButton(Captions.getCurrentBACaptionByKey(Captions.CAPTIONS_VIEW_ADD_REQUEST), new SelectionListener<ButtonEvent>(){
		
		public void componentSelected(ButtonEvent ce) {
			ToAddNewRequest event = new ToAddNewRequest("New " + Captions.getRecordDisplayName());
			TbitsEventRegister.getInstance().fireEvent(event);
		}});
	
	public SearchToolBar(String sysPrefix, UIContext parentContext) {
		super(sysPrefix, parentContext);
		
		observable.subscribe(KeyboardShortcutEvent.class, new ITbitsEventHandle<KeyboardShortcutEvent>(){
			
			public void handleEvent(KeyboardShortcutEvent event) {
				// N for New Request
				if(event.getKeyCode() == TbitsNativePreviewHandler.baseASCII + 14){
					if(NEW_REQUEST.isAttached()){
						ToAddNewRequest newEvent = new ToAddNewRequest("New " + Captions.getRecordDisplayName());
						TbitsEventRegister.getInstance().fireEvent(newEvent);
					}
				}
			}});
	}
	
	@Override
	protected void initializeButtons() {
		this.addNewRequestButton();
		this.addViewDraftsButton();
		this.addBulkUpdateButton();
		
		this.add(new SeparatorToolItem());
		
		//this.addConsolidateButton();
		 //this.addExportButton();
		//this.addExportAllButton();
		this.addImportDataButton();
		this.add(new SeparatorToolItem());
		this.addExportMenu();
		//this.addExportButton();
		if(GlobalConstants.isTagsSupported){
			this.add(new SeparatorToolItem());
			this.addTagsButton();
		}
		
		
		
		/**
		 * Check for wizard plugins.
		 */
		ArrayList<IWizardPlugin> wizards = GWTPluginRegister.getInstance().getPlugins(WizardPluginSlot.class, IWizardPlugin.class);
		int count  = 0;
		if(wizards != null){
			for(final IWizardPlugin wizardPlugin : wizards){
				if(wizardPlugin.shouldExecute(ClientUtils.getSysPrefix())){
					count++ ;
					if(count == 1)
						this.add(new SeparatorToolItem());
					ToolBarButton btn = new ToolBarButton(wizardPlugin.getButtonCaption(), new SelectionListener<ButtonEvent>(){
						@Override
						public void componentSelected(ButtonEvent ce) {
							List<TbitsTreeRequestData> selectedItems = getGridSelectedItems();
							ArrayList<Integer> requestIds = new ArrayList<Integer>();
							for(TbitsTreeRequestData model : selectedItems){
								if(model.getRequestId() > 0)
									requestIds.add(model.getRequestId());
							}
							wizardPlugin.getWidget(requestIds);
						}});
					this.add(btn);
				}
			}
		}
		
		this.addCopyButton();
	}
	
	/**
	 * Adds export All to csv button.
	 */
//	protected void addExportAllButton(){
////		EXPORT_ALL.disable();
//		EXPORT_ALL.setToolTip("Export all pages to csv");
////		EXPORT_ALL.disableOn(OnChangeBA.class);
////		EXPORT_ALL.enableOn(OnRequestsRecieved.class);
//		this.add(EXPORT_ALL);
//	}
	
	/**
	 * Adds consolidate button.
	 */
//	protected void addConsolidateButton(){
////		EXPORT_TO_CONSOLIDATE.disable();
//		EXPORT_TO_CONSOLIDATE.setToolTip("Export selected records in the grid to a new tab");
////		EXPORT_TO_CONSOLIDATE.disableOn(OnChangeBA.class);
////		EXPORT_TO_CONSOLIDATE.enableOn(OnRequestsRecieved.class);
//		this.add(EXPORT_TO_CONSOLIDATE);
//	}
	
	/**
	 * Opens up a {@link BulkUpdatePanel} for selected items.
	 * Shows up when user has add or change permission on request_id
	 */
	protected void addBulkUpdateButton(){
		BULK_UPDATE.setToolTip("Opens Bulk Update for the selected rows");
		BULK_UPDATE.disable();
		BULK_UPDATE.disableOn(OnChangeBA.class);
		
		if(this.sysPrefix.equals(ClientUtils.getSysPrefix())){
			ITbitsEventHandle<OnFieldsReceived> onFieldsReceivedhandle = new ITbitsEventHandle<OnFieldsReceived>(){
				public void handleEvent(OnFieldsReceived event) {
					FieldCache fieldCache = CacheRepository.getInstance().getCache(FieldCache.class);
					if(fieldCache != null && fieldCache.isInitialized()){
						BAField field = fieldCache.getObject(REQUEST);
						if((field.getUserPerm() & PermissionClient.ADD) != 0 || (field.getUserPerm() & PermissionClient.CHANGE) != 0){
							BULK_UPDATE.enable();
						}
					}
				}};
			if(AppState.checkAppStateIsTill(AppState.FieldsReceived)){
				onFieldsReceivedhandle.handleEvent(null);
			}
			observable.subscribe(OnFieldsReceived.class, onFieldsReceivedhandle);
		}
		this.add(BULK_UPDATE);
	}
	
	/**
	 * Adds the new request button. 
	 * Shows up when user has add permission on request_id
	 */
	protected void addNewRequestButton(){
		NEW_REQUEST.disable();
		NEW_REQUEST.disableOn(OnChangeBA.class);
		
		ITbitsEventHandle<OnFieldsReceived> onFieldsReceivedhandle = new ITbitsEventHandle<OnFieldsReceived>(){
			public void handleEvent(OnFieldsReceived event) {
				FieldCache fieldCache = CacheRepository.getInstance().getCache(FieldCache.class);
				if(fieldCache != null && fieldCache.isInitialized()){
					BAField field = fieldCache.getObject(REQUEST);
					if((field.getUserPerm() & PermissionClient.ADD) != 0){
						NEW_REQUEST.enable();
					}
				}
			}};
		if(AppState.checkAppStateIsTill(AppState.FieldsReceived)){
			onFieldsReceivedhandle.handleEvent(null);
		}
		observable.subscribe(OnFieldsReceived.class, onFieldsReceivedhandle);
		
		this.add(NEW_REQUEST);
	}
	
	/**
	 * Adds import data button.
	 * Shows up when user has add and change permission on request_id.
	 */
	protected void addImportDataButton(){
		IMPORT_DATA.setToolTip("Opens Import Data Wizard");
		IMPORT_DATA.disable();
		IMPORT_DATA.disableOn(OnChangeBA.class);
		
		ITbitsEventHandle<OnFieldsReceived> onFieldsReceivedhandle = new ITbitsEventHandle<OnFieldsReceived>(){
			public void handleEvent(OnFieldsReceived event) {
				FieldCache fieldCache = CacheRepository.getInstance().getCache(FieldCache.class);
				if(fieldCache != null && fieldCache.isInitialized()){
					BAField field = fieldCache.getObject(REQUEST);
					if((field.getUserPerm() & PermissionClient.ADD) != 0 && (field.getUserPerm() & PermissionClient.CHANGE) != 0){
						IMPORT_DATA.enable();
					}
				}
			}};
		if(AppState.checkAppStateIsTill(AppState.FieldsReceived)){
			onFieldsReceivedhandle.handleEvent(null);
		}
		observable.subscribe(OnFieldsReceived.class, onFieldsReceivedhandle);
		this.add(IMPORT_DATA);
	}
	
	/**
	 * Adds the view drafts button. 
	 * Retrieves the drafts when user has Add or Change permission on request_id.
	 */
	protected void addViewDraftsButton(){
		DRAFTS.setToolTip("Shows drafts");
		DRAFTS.disable();
		DRAFTS.disableOn(OnChangeBA.class);
		
		ITbitsEventHandle<OnFieldsReceived> onFieldsReceivedhandle = new ITbitsEventHandle<OnFieldsReceived>(){
			public void handleEvent(OnFieldsReceived event) {
				FieldCache fieldCache = CacheRepository.getInstance().getCache(FieldCache.class);
				if(fieldCache != null && fieldCache.isInitialized()){
					BAField field = fieldCache.getObject(REQUEST);
					if((field.getUserPerm() & PermissionClient.ADD) != 0 || (field.getUserPerm() & PermissionClient.CHANGE) != 0){
						JaguarConstants.dbService.getUserDrafts(ClientUtils.getSysPrefix(), new AsyncCallback<List<UserDraftClient>>(){
							public void onFailure(Throwable caught) {
								Log.error("Unable to load Drafts", caught);
							}
				
							public void onSuccess(List<UserDraftClient> result) {
								if(result != null){
									JaguarConstants.drafts = new ListStore<UserDraftClient>();
									if(result.size() > 0){
										JaguarConstants.drafts.add(result);
										DRAFTS.setText("View Drafts (" + result.size() + ")");
										DRAFTS.enable();
									}else{
										DRAFTS.setText("View Drafts");
										DRAFTS.enable();
									}
								}
							}});
					}
				}
			}
		};
		if(AppState.checkAppStateIsTill(AppState.FieldsReceived)){
			onFieldsReceivedhandle.handleEvent(null);
		}
		observable.subscribe(OnFieldsReceived.class, onFieldsReceivedhandle);
		this.add(DRAFTS);
	}
}