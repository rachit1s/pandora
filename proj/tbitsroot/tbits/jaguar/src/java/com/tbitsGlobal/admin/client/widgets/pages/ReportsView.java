package com.tbitsGlobal.admin.client.widgets.pages;

import java.util.ArrayList;

import com.extjs.gxt.ui.client.Style.Scroll;
import com.extjs.gxt.ui.client.Style.SelectionMode;
import com.extjs.gxt.ui.client.Style.SortDir;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.GridEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.store.GroupingStore;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.store.Store;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.LabelField;
import com.extjs.gxt.ui.client.widget.form.StoreFilterField;
import com.extjs.gxt.ui.client.widget.grid.CheckBoxSelectionModel;
import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
import com.extjs.gxt.ui.client.widget.grid.ColumnData;
import com.extjs.gxt.ui.client.widget.grid.ColumnModel;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.extjs.gxt.ui.client.widget.grid.GridCellRenderer;
import com.extjs.gxt.ui.client.widget.grid.GridGroupRenderer;
import com.extjs.gxt.ui.client.widget.grid.GroupColumnData;
import com.extjs.gxt.ui.client.widget.grid.GroupingView;
import com.extjs.gxt.ui.client.widget.layout.FitData;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.toolbar.ToolBar;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.tbitsGlobal.admin.client.utils.APConstants;
import com.tbitsGlobal.admin.client.utils.LinkIdentifier;
import com.tbitsGlobal.admin.client.widgets.APTabItem;
import com.tbitsGlobal.admin.client.widgets.pages.bulk.ReportParamsBulkGridPanel;
import commons.com.tbitsGlobal.utils.client.ClickableLink;
import commons.com.tbitsGlobal.utils.client.ClickableLink.ClickableLinkListener;
import commons.com.tbitsGlobal.utils.client.TbitsExceptionClient;
import commons.com.tbitsGlobal.utils.client.TbitsInfo;
import commons.com.tbitsGlobal.utils.client.GridCellRenderers.LinkCellRenderer;
import commons.com.tbitsGlobal.utils.client.GridCellRenderers.LinkCellRendererPlugin;
import commons.com.tbitsGlobal.utils.client.domainObjects.ReportClient;
import commons.com.tbitsGlobal.utils.client.widgets.ToolBarButton;

/**
 * @author dheeru
 * 
 */
public class ReportsView extends APTabItem {

	protected Grid<ReportClient> reportsGrid;
	protected StoreFilterField<ReportClient> filter;
	GroupingStore<ReportClient> reportsStore;

	public ReportsView(LinkIdentifier linkId) {
		super(linkId);
		
		this.setClosable(true);
		this.setLayout(new FitLayout());
		this.setScrollMode(Scroll.NONE);
		
		reportsStore = new GroupingStore<ReportClient>();
	}

	public void onRender(Element parent, int pos) {
		super.onRender(parent, pos);

		ContentPanel mainContainer = new ContentPanel();
		mainContainer.setBodyBorder(false);
		mainContainer.setHeaderVisible(false);
		mainContainer.setLayout(new FitLayout());
		mainContainer.setScrollMode(Scroll.AUTO);

		ToolBar buttonBar = new ToolBar();
		Button addButton = new ToolBarButton("Add Report");
		addButton.addSelectionListener(new SelectionListener<ButtonEvent>() {
			@Override
			public void componentSelected(ButtonEvent ce) {
				ReportFormWindow addForm = new ReportFormWindow();
				addForm.show();
			}
		});
		buttonBar.add(addButton);

		Button deleteButton = new ToolBarButton("Delete Selected Reports");
		deleteButton.addSelectionListener(new SelectionListener<ButtonEvent>() {

			public void componentSelected(ButtonEvent ce) {
				
				final ArrayList<Integer> reportIds = new ArrayList<Integer>();
				for (ReportClient reportClient : reportsGrid.getSelectionModel().getSelectedItems()) {
					reportIds.add(reportClient.getReportId());
				}
				if (reportIds.size() == 0) {
					TbitsInfo.warn("No report selected, please select a report");
					return;
				}
				if(Window.confirm("Do you really want to delete reports with report IDs : " + reportIds.toString())){
					deleteReports(reportIds);
				}
			}

		});
		buttonBar.add(deleteButton);
		
		Button refreshButton = new ToolBarButton("Refresh");
		refreshButton.addSelectionListener(new SelectionListener<ButtonEvent>(){

			public void componentSelected(ButtonEvent ce) {
				reportsGrid.getStore().removeAll();
				getReports();
			}
			
		});
		
		buttonBar.add(refreshButton);
		createGrid();
		
		mainContainer.setTopComponent(buttonBar);
		
		mainContainer.add(reportsGrid, new FitData());
		
		this.add(mainContainer, new FitData());
		
		getReports();
	}
	
	private void createGrid(){
		ArrayList<ColumnConfig> configs = new ArrayList<ColumnConfig>();

		CheckBoxSelectionModel<ReportClient> sm = new CheckBoxSelectionModel<ReportClient>();
		sm.setSelectionMode(SelectionMode.MULTI);
		configs.add(sm.getColumn());
		ColumnConfig idConf = new ColumnConfig(ReportClient.REPORT_ID, "ID", 40);
		configs.add(idConf);
		ColumnConfig enabledConf = new ColumnConfig(ReportClient.IS_ENABLED, "is Enabled", 60);
		configs.add(enabledConf);
		ColumnConfig nameConf = new ColumnConfig(ReportClient.REPORT_NAME, "Report Name", 240);
		configs.add(nameConf);
		ColumnConfig fileConf = new ColumnConfig(ReportClient.FILE_NAME, "File Name", 240);
		configs.add(fileConf);
		ColumnConfig descConf = new ColumnConfig(ReportClient.DESCRIPTION, "Description", 240);
		configs.add(descConf);
		ColumnConfig privateConf = new ColumnConfig(ReportClient.IS_PRIVATE, "Is Private", 60);
		configs.add(privateConf);
		ColumnConfig groupingConf = new ColumnConfig(ReportClient.GROUP, 0); configs.add(groupingConf);
		
		ColumnConfig editColumn = new ColumnConfig("edit", "Edit", 70);
		GridCellRenderer<ReportClient> editRenderer = new LinkCellRenderer<ReportClient>(){
			@Override
			public Object render(final ReportClient model, String property,
					ColumnData config, int rowIndex, int colIndex,
					ListStore<ReportClient> store,
					Grid<ReportClient> grid) {
				ClickableLink link = new ClickableLink("Edit", new ClickableLinkListener<GridEvent<ReportClient>>(){
					public void onClick(GridEvent<ReportClient> e) {
						ReportFormWindow editForm = new ReportFormWindow(model);
						editForm.show();
					}
				});
			addLink(link);
			return link.getHtml();
			}};
		editColumn.setRenderer(editRenderer);
		configs.add(editColumn);
		
		ColumnConfig paramsColumn = new ColumnConfig("params", "Parameters", 100);
		GridCellRenderer<ReportClient> paramsRenderer = new LinkCellRenderer<ReportClient>(){
			@Override
			public Object render(final ReportClient model, String property,
					ColumnData config, int rowIndex, int colIndex,
					ListStore<ReportClient> store,
					Grid<ReportClient> grid) {
				ClickableLink link = new ClickableLink("Edit Parameters", new ClickableLinkListener<GridEvent<ReportClient>>(){
					public void onClick(GridEvent<ReportClient> e) {
						com.extjs.gxt.ui.client.widget.Window window = new com.extjs.gxt.ui.client.widget.Window();
						window.setHeading("Report Parameters");
						window.setLayout(new FitLayout());
						window.setModal(true);
						window.setClosable(true);
						
						window.setWidth(com.google.gwt.user.client.Window.getClientWidth() - 100);
						window.setHeight(com.google.gwt.user.client.Window.getClientHeight() - 100);
						
						ReportParamsBulkGridPanel typeEditor = new ReportParamsBulkGridPanel(model.getReportId(), reportsGrid.getStore());
						window.add(typeEditor, new FitData());
						window.show();
					}
				});
				addLink(link);
				return link.getHtml();
			}};
		paramsColumn.setRenderer(paramsRenderer);
		configs.add(paramsColumn);

		reportsStore.groupBy(ReportClient.GROUP);
		reportsGrid = new Grid<ReportClient>(reportsStore, new ColumnModel(configs));

		reportsGrid.setSelectionModel(sm);

		GroupingView view = new GroupingView();
		view.setShowGroupedColumn(false);
		view.setGroupRenderer(new GridGroupRenderer() {
			public String render(GroupColumnData data) {
				if ((data.group == null) || (data.group.trim() == ""))
					return "Group : Default Group";
				return "Group : " + data.group;
			}
		});
		reportsGrid.setView(view);
		reportsGrid.addPlugin(sm);
		reportsGrid.setAutoExpandColumn(ReportClient.DESCRIPTION);
		reportsGrid.setAutoExpandMin(100);
		reportsGrid.setAutoHeight(true);
		
		// Register LinkCellRenderers
		reportsGrid.addPlugin(new LinkCellRendererPlugin());
		
	}

	private void deleteReports(ArrayList<Integer> reportIds) {
		if (reportIds == null || reportIds.size() == 0)
			return;

		TbitsInfo.info("Deleting reports : " + reportIds.toString());
		APConstants.apService.deleteReports(reportIds, new AsyncCallback<ArrayList<Integer>>() {

			public void onFailure(Throwable caught) {
				TbitsInfo.error("Failed to delete reports", new TbitsExceptionClient(caught));
			}

			public void onSuccess(ArrayList<Integer> result) {
				getReports();
				if (!result.isEmpty()) {
					TbitsInfo.warn("Delete query executed properly but these reports can not be deleted :"	+ result.toString());
				} else {
					TbitsInfo.info("Deleted Successfully");
				}
			}

		});
	}

	private void getReports() {
		reportsGrid.getStore().removeAll();
		TbitsInfo.info("Retrieving Reports....");
		APConstants.apService.getReports(new AsyncCallback<ArrayList<ReportClient>>() {
			public void onFailure(Throwable caught) {
				TbitsInfo.error("Call failed on server", new TbitsExceptionClient(caught));
			}

			public void onSuccess(ArrayList<ReportClient> result) {
				reportsGrid.getStore().add(result);
				reportsGrid.getStore().sort(ReportClient.REPORT_ID, SortDir.ASC);
			}
		});
	}
}
