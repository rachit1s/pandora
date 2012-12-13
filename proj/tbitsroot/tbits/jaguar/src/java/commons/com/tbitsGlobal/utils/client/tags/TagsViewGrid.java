package commons.com.tbitsGlobal.utils.client.tags;

import commons.com.tbitsGlobal.utils.client.search.grids.AbstractSearchGrid;
import commons.com.tbitsGlobal.utils.client.grids.GridColumnView;

/**
 * View grid for tags tab
 * 
 * @author Karan Gupta
 * 
 */
public class TagsViewGrid extends AbstractSearchGrid{
	
	/**
	 * Constructor. 
	 * 
	 * @param prefs
	 */
	public TagsViewGrid(String sysPrefix) {
		super(sysPrefix);
	}
	
	public GridColumnView getViewId() {
		return GridColumnView.SearchGrid;
	}

}

