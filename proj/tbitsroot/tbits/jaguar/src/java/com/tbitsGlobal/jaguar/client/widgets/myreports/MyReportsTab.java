package com.tbitsGlobal.jaguar.client.widgets.myreports;

import java.util.ArrayList;

import com.extjs.gxt.ui.client.store.GroupingStore;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.TabItem;
import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
import com.extjs.gxt.ui.client.widget.grid.ColumnData;
import com.extjs.gxt.ui.client.widget.grid.ColumnModel;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.extjs.gxt.ui.client.widget.grid.GridCellRenderer;
import com.extjs.gxt.ui.client.widget.grid.GridGroupRenderer;
import com.extjs.gxt.ui.client.widget.grid.GroupColumnData;
import com.extjs.gxt.ui.client.widget.grid.GroupingView;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.tbitsGlobal.jaguar.client.JaguarConstants;
import commons.com.tbitsGlobal.utils.client.ClientUtils;
import commons.com.tbitsGlobal.utils.client.TbitsInfo;
import commons.com.tbitsGlobal.utils.client.domainObjects.ReportClient;

/**
 * Tab to diaplay "My Reports"
 * 
 * @author sourabh
 *
 */
public class MyReportsTab extends TabItem {
	private static final String caption = "My Reports";
	
	public MyReportsTab() {
		super(caption);
		
		this.setBorders(false);
		this.setLayout(new FitLayout());
	}
	
	@Override
	protected void onRender(Element parent, int index) {
		super.onRender(parent, index);
		
		this.build();
	}
	
	/**
	 * Build the page retrieving the Reports.
	 */
	private void build(){
		TbitsInfo.info("Loading " + caption + "... Please Wait...");
		JaguarConstants.dbService.getUserReports(new AsyncCallback<ArrayList<ReportClient>>(){
			public void onFailure(Throwable caught) {
				caught.printStackTrace();
				TbitsInfo.error("Error while loading captions...", caught);
			}

			public void onSuccess(ArrayList<ReportClient> result) {
				buildGrid(result);
			}});
	}
	
	private void buildGrid(ArrayList<ReportClient> reports){
		GroupingStore<ReportClient> store = new GroupingStore<ReportClient>();
		store.add(reports);
		store.groupBy(ReportClient.GROUP);
		
		ArrayList<ColumnConfig> configs = new ArrayList<ColumnConfig>();
		
		ColumnConfig reportId = new ColumnConfig(ReportClient.REPORT_ID, 100);
		reportId.setHeader("Report Id");
		configs.add(reportId);
		
		ColumnConfig name = new ColumnConfig(ReportClient.REPORT_NAME, 300);
		name.setHeader("Report Name");
		configs.add(name);
		
		ColumnConfig desc = new ColumnConfig(ReportClient.DESCRIPTION, 300);
		desc.setHeader("Description");
		configs.add(desc);
		
		ColumnConfig group = new ColumnConfig(ReportClient.GROUP, 100);
		configs.add(group);
		
		ColumnConfig viewCol = new ColumnConfig();
		viewCol.setWidth(70);
		viewCol.setHeader("View");
		viewCol.setRenderer(new GridCellRenderer<ReportClient>(){
			public Object render(ReportClient model, String property,
					ColumnData config, int rowIndex, int colIndex,
					ListStore<ReportClient> store, Grid<ReportClient> grid) {
				String fileName = model.getFileName();
				int userId = ClientUtils.getCurrentUser().getUserId();
				return "<a target='_blank' href='" + ClientUtils.getUrlToFilefromBase("/reports/frameset?__report=" + fileName + "&user_id=" + userId + model.getParamQuery()) + "'>View</a>";
			}});
		configs.add(viewCol);
		
		Grid<ReportClient> grid = new Grid<ReportClient>(store, new ColumnModel(configs));
		
		GroupingView view = new GroupingView();
		view.setShowGroupedColumn(false);
		view.setGroupRenderer(new GridGroupRenderer() {  
	      public String render(GroupColumnData data) {  
	        String f = "Group";  
	        return f + " : " + data.group;  
	      }  
	    }); 
		grid.setView(view);
		
		this.add(grid);
		
		this.layout();
	}
}
