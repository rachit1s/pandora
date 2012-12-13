package commons.com.tbitsGlobal.utils.client.grids;

import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.layout.FitData;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.google.gwt.user.client.Element;

import commons.com.tbitsGlobal.utils.client.Events.BaseTbitsObservable;
import commons.com.tbitsGlobal.utils.client.Events.TbitsObservable;
import commons.com.tbitsGlobal.utils.client.widgets.GridPagingBar;

/**
 * 
 * @author sourabh
 * 
 * Container panels for {@link RequestsViewGrid}.
 * 
 * They carry the grid, the tool bar and the paging bar
 */
public class RequestsViewGridContainer extends ContentPanel{
	protected String sysPrefix;
	
	protected RequestsViewGrid grid;
	protected RequestsViewGridToolBar toolbar;
	protected GridPagingBar pagingBar;
	
	protected TbitsObservable observable;
	
	/**
	 * Constructor
	 * @param grid
	 */
	public RequestsViewGridContainer(String sysPrefix, RequestsViewGrid grid) {
		super();
		
		this.setLayout(new FitLayout());
		this.setLayoutOnChange(true);
		
		this.sysPrefix = sysPrefix;
		this.grid = grid;
		
		observable = new BaseTbitsObservable();
		observable.attach();
	}
	
	@Override
	protected void onAttach() {
		super.onAttach();
		
		observable.attach();
	}
	
	@Override
	protected void beforeRender() {
		super.beforeRender();
		
		if(this.toolbar != null)
			this.setTopComponent(toolbar);
		
		if(this.pagingBar != null)
			this.setBottomComponent(pagingBar);
	}
	
	@Override
	protected void onRender(Element parent, int pos) {
		super.onRender(parent, pos);
		
		this.add(grid, new FitData());
	}
	
	@Override
	protected void onDetach() {
		super.onDetach();
		
		observable.detach();
	}

	public void setToolbar(RequestsViewGridToolBar toolbar) {
		this.toolbar = toolbar;
	}

	public RequestsViewGridToolBar getToolbar() {
		return toolbar;
	}

	public void setPagingBar(GridPagingBar pagingBar) {
		this.pagingBar = pagingBar;
	}

	public GridPagingBar getPagingBar() {
		return pagingBar;
	}

	public RequestsViewGrid getGrid() {
		return grid;
	}
}
