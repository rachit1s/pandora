package commons.com.tbitsGlobal.utils.client.tags;

import java.util.ArrayList;
import java.util.List;

import commons.com.tbitsGlobal.utils.client.search.grids.AbstractSearchGrid;
import commons.com.tbitsGlobal.utils.client.search.grids.AbstractSearchGridContainer;

import commons.com.tbitsGlobal.utils.client.GlobalConstants;
import commons.com.tbitsGlobal.utils.client.TbitsInfo;
import commons.com.tbitsGlobal.utils.client.TbitsTreeRequestData;
import commons.com.tbitsGlobal.utils.client.Events.ITbitsEventHandle;
import commons.com.tbitsGlobal.utils.client.Events.ShowTaggedRequests;
import commons.com.tbitsGlobal.utils.client.widgets.GridPagingBar;

public class TagsViewGridContainer extends AbstractSearchGridContainer{

	private List<TbitsTreeRequestData> fetchedRequests;

	public TagsViewGridContainer(String sysPrefix, final AbstractSearchGrid grid) {
		super(sysPrefix, grid);
		
		this.setPagingBar(new GridPagingBar(GlobalConstants.SEARCH_PAGESIZE){
			protected void loadPage(final int page) {
				if(page <= 0 || page > this.getTotalPages()){
					TbitsInfo.error("Invalid Page Number");
					return;
				}
				
				grid.getTreeStore().removeAll();
				int offset = (page-1)*pageSize;
				for(int i=0; i<pagingBar.getPageSize() && (offset+i)<fetchedRequests.size(); i++){
					grid.addModel(fetchedRequests.get(offset+i));
				}
				grid.getView().refresh(false);
				pagingBar.adjustButtons(page, fetchedRequests.size());
			}});
		
		observable.subscribe(ShowTaggedRequests.class, new ITbitsEventHandle<ShowTaggedRequests>(){
			
			public void handleEvent(ShowTaggedRequests event) {
				
				fetchedRequests = event.getRequests();
				List<TbitsTreeRequestData> displayedRequests = new ArrayList<TbitsTreeRequestData>();
				for(int i=0; i<pagingBar.getPageSize() && i<fetchedRequests.size(); i++){
					displayedRequests.add(fetchedRequests.get(i));
				}
				
				grid.clearStore();
				grid.addModels(displayedRequests);
				pagingBar.adjustButtons(1, fetchedRequests.size());
			}

		});
	}

}
