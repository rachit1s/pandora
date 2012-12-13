package com.tbitsGlobal.jaguar.client.widgets.myrequests;

import java.util.List;

import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.tbitsGlobal.jaguar.client.JaguarConstants;
import commons.com.tbitsGlobal.utils.client.DQLResults;
import commons.com.tbitsGlobal.utils.client.TbitsInfo;
import commons.com.tbitsGlobal.utils.client.grids.RequestsViewGridContainer;
import commons.com.tbitsGlobal.utils.client.log.Log;
import commons.com.tbitsGlobal.utils.client.widgets.GridPagingBar;

/**
 * Container panel for {@link MyRequestsGrid}.
 * 
 * @author sourabh
 *
 */
public class MyRequestsGridContainer extends RequestsViewGridContainer{	
	private int totalRecords;
	private String sysPrefix;
	/**
	 * Filters applied to the grid.
	 */
	private List<String> filterFields;
	
	public MyRequestsGridContainer(String sysPrefix, final MyRequestsGrid grid, String heading, int pageSize, List<String> filterFields) {
		super(sysPrefix, grid);
		this.setHeading(heading);
		
		this.totalRecords = grid.getBaRequests().getResults().getTotalRecords();
		this.sysPrefix = sysPrefix;
		this.filterFields = filterFields;
		
		this.setPagingBar(new GridPagingBar(pageSize){
			@Override
			protected void loadPage(int page) {
				MyRequestsGridContainer.this.loadPage(page, pageSize);
			}});
	}
	
	/**
	 * Loads a page.
	 * 
	 * @param page. The page number.
	 */
	protected void loadPage(final int page, int pageSize){
		if(page <= 0 || page > pagingBar.getTotalPages()){
			TbitsInfo.error("Invalid Page Number");
			return;
		}
		
		JaguarConstants.dbService.getMyRequestsByBA(sysPrefix, filterFields, pageSize, page, 
			new AsyncCallback<DQLResults>(){
				public void onFailure(Throwable arg0) {
					TbitsInfo.error("Error while loading data...", arg0);
					Log.error("Error while loading data...", arg0);
				}
	
				public void onSuccess(DQLResults result) {
					getGrid().getTreeStore().removeAll();
					getGrid().addModels(result.getRequests());
					
					int totalRecords = result.getTotalRecords();
					pagingBar.adjustButtons(page, totalRecords);
				}});
	}
	
	@Override
	protected void onRender(Element parent, int pos) {
		super.onRender(parent, pos);
		
		pagingBar.adjustButtons(1, totalRecords);
	}
	
	protected void doAutoHeight() {  
		if (getGrid().isViewReady()) {  
			getGrid().getView().getBody().setStyleAttribute("overflowY", "hidden");
			int height = (getGrid().getView().getBody().isScrollableX() ? 19 : 0) + getGrid().el().getFrameWidth("tb")  
	          + getGrid().getView().getHeader().getHeight() + this.getFrameHeight()  
	          + getGrid().getView().getBody().firstChild().getHeight();
			this.setHeight(height);
	    }
	}
}
