package commons.com.tbitsGlobal.utils.client.search.grids;

import commons.com.tbitsGlobal.utils.client.GlobalConstants;
import commons.com.tbitsGlobal.utils.client.TbitsInfo;
import commons.com.tbitsGlobal.utils.client.search.searchpanel.DQL;
import commons.com.tbitsGlobal.utils.client.widgets.GridPagingBar;

/**
 * 
 * @author sutta
 * 
 * Panel to contain {@link BasicSearchGrid}
 */
public class BasicSearchGridContainer extends AbstractSearchGridContainer{

	public BasicSearchGridContainer(String sysPrefix, final AbstractSearchGrid grid) {
		super(sysPrefix, grid);
		
		setHeaderVisible(false);
		
		setPagingBar(new GridPagingBar(GlobalConstants.SEARCH_PAGESIZE){
			@Override
			protected void loadPage(int page) {
				if(page <= 0 || page > this.getTotalPages()){
					TbitsInfo.error("Invalid Page Number");
					return;
				}
				
				onSearch(grid.getDql(), pageSize, page);
			}});
	}
	
	protected void onSearch(final DQL dql, int pageSize, final int page){
		
	}

}
