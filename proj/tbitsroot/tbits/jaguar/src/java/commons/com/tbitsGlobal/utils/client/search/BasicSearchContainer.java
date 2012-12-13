package commons.com.tbitsGlobal.utils.client.search;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.extjs.gxt.ui.client.Style.LayoutRegion;
import com.extjs.gxt.ui.client.Style.SortDir;
import com.extjs.gxt.ui.client.util.Margins;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.layout.BorderLayout;
import com.extjs.gxt.ui.client.widget.layout.BorderLayoutData;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.rpc.AsyncCallback;
import commons.com.tbitsGlobal.utils.client.ClientUtils;
import commons.com.tbitsGlobal.utils.client.DQLResults;
import commons.com.tbitsGlobal.utils.client.GlobalConstants;
import commons.com.tbitsGlobal.utils.client.TbitsInfo;
import commons.com.tbitsGlobal.utils.client.TbitsTreeRequestData;
import commons.com.tbitsGlobal.utils.client.Events.BaseTbitsObservable;
import commons.com.tbitsGlobal.utils.client.Events.TbitsObservable;
import commons.com.tbitsGlobal.utils.client.grids.RequestsViewGrid;
import commons.com.tbitsGlobal.utils.client.grids.RequestsViewGridContainer;
import commons.com.tbitsGlobal.utils.client.log.Log;
import commons.com.tbitsGlobal.utils.client.search.grids.AbstractSearchGridContainer;
import commons.com.tbitsGlobal.utils.client.search.grids.BasicSearchGrid;
import commons.com.tbitsGlobal.utils.client.search.grids.BasicSearchGridContainer;
import commons.com.tbitsGlobal.utils.client.search.searchpanel.AbstractSearchPanel;
import commons.com.tbitsGlobal.utils.client.search.searchpanel.DQL;
import commons.com.tbitsGlobal.utils.client.search.searchpanel.TbitsSearchPanel;
import commons.com.tbitsGlobal.utils.client.search.searchpanel.AbstractSearchPanel.ISearchHandle;

/**
 * 
 * @author sourabh
 * 
 * Contains the {@link TbitsSearchPanel} and a search grid
 */
public class BasicSearchContainer extends LayoutContainer{

	protected String sysPrefix;
	
	protected AbstractSearchPanel filterPanel;
	
	protected AbstractSearchGridContainer gridContainer;
	
	/**
	 * To listen to events
	 */
	protected TbitsObservable observable;
	
	public BasicSearchContainer(String sysPrefix) {
		super();
		
		this.setBorders(false);
		this.setLayout(new BorderLayout());
		
		observable = new BaseTbitsObservable();
		observable.attach();
		
		this.sysPrefix = sysPrefix;
	}
	
	@Override
	protected void onRender(Element parent, int index) {
		super.onRender(parent, index);
		
		this.makeLeftPane();
		this.makeRightPane();
	}
	
	@Override
	protected void onAttach() {
		super.onAttach();
		
		observable.attach();
	}
	
	@Override
	protected void onDetach() {
		super.onDetach();
		
		observable.detach();
	}
	
	protected void makeRightPane() {
		final BasicSearchGrid grid = new BasicSearchGrid(sysPrefix);
		
		gridContainer = new BasicSearchGridContainer(sysPrefix, grid){
			@Override
			protected void onSearch(DQL dql, int pageSize, int page) {
				search(dql, pageSize, page);
			}
		};
		this.add(gridContainer, new BorderLayoutData(LayoutRegion.CENTER));
	}
	
	protected void makeLeftPane(){
		BorderLayoutData westData = new BorderLayoutData(LayoutRegion.WEST, 260);
		westData.setSplit(true);
		westData.setMargins(new Margins(0,5,0,0));
		
		if(filterPanel == null)
			filterPanel = new TbitsSearchPanel(sysPrefix);
		
		filterPanel.setSearchHandle(new ISearchHandle(){
			public void onSearch(DQL dql) {
				search(dql, GlobalConstants.SEARCH_PAGESIZE, 1);
			}});
		
		this.add(filterPanel, westData);
	}
	
	protected void search(final DQL dql, int pageSize, final int page){
		gridContainer.getGrid().getStore().removeAll();
		
		GlobalConstants.utilService.getRequestsForDQL(sysPrefix, dql, pageSize, page, new AsyncCallback<DQLResults>(){
			public void onFailure(Throwable caught) {
				TbitsInfo.error("Error while searching.. Please see logs for details...", caught);
				Log.error("Error while searching.. Please see logs for details...", caught);
			}

			public void onSuccess(DQLResults result) {
				if(result != null){
					gridContainer.getGrid().setDql(dql);
					
					List<TbitsTreeRequestData> requestList = result.getRequests();
					
					// Sort the requests
//					List<TbitsTreeRequestData> sortedRequests = ClientUtils.sortRequests(requestList);
					gridContainer.getGrid().getView().setSortingEnabled(false);
					gridContainer.getGrid().addModels(requestList);
					
					
					Map <String, Object> state = gridContainer.getGrid().getState();
					String sortFieldKey = "sortField";
					String sortDirKey = "sortDir";
					if(state.containsKey(sortFieldKey))
					{
						Iterator<String> itr = dql.sortOrder.keySet().iterator();
						
						if(itr.hasNext())
						{
							String sortfield = itr.next();
							int sortDirInt = dql.sortOrder.get(sortfield);
							
							SortDir mySortDir = SortDir.DESC;
							
							if(sortDirInt == DQL.SORTDIR_ASC)
								mySortDir = SortDir.ASC;
							
							state.put(sortFieldKey, sortfield);
							state.put(sortDirKey, mySortDir);
						}
						
					}
//					.get("sortField");
					
//					gridContainer.getGrid().getStore().setSortDir(SortDir.NONE);
//					gridContainer.getGrid().getStore().setSortField(null);
//					gridContainer.getGrid().getView().setSortingEnabled(false);
//					gridContainer.getGrid().getView().refresh(true);
					gridContainer.getPagingBar().adjustButtons(page, result.getTotalRecords());
				}
			}});
	}

	public RequestsViewGridContainer getGridContainer() {
		return gridContainer;
	}
}
