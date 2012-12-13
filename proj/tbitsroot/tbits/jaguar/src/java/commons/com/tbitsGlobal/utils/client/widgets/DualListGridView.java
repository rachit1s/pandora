package commons.com.tbitsGlobal.utils.client.widgets;

import com.extjs.gxt.ui.client.widget.grid.GridView;

/**
 * 
 * @author syeda
 * This GridView is created to stop the scrolling to the top on refresh by default.
 * If scroll to top is required then the overloaded version refresh should be called with appropriate parameters 
 */
public class DualListGridView extends GridView
{
	public DualListGridView()
	{
		super();
		this.preventScrollToTopOnRefresh = true;
		//this.onColumnMove();
	}
	/**
	 * @param headerToo : if true then header is also refreshed.
	 * @param scrollToTop : if false then the scrollbar will scroll to the top after refresh.
	 */
	public void refresh(boolean headerToo, boolean scrollToTop) 
	{
		boolean tempPreventScollToTopOnRefresh = preventScrollToTopOnRefresh;
		preventScrollToTopOnRefresh = scrollToTop;
		super.refresh(headerToo);
		preventScrollToTopOnRefresh = tempPreventScollToTopOnRefresh;
	}
}
