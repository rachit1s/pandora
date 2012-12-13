package commons.com.tbitsGlobal.utils.client.search.grids;

import commons.com.tbitsGlobal.utils.client.grids.GridColumnView;

/**
 * 
 * @author sutta
 * 
 * Grid to be used for simple search
 */
public class BasicSearchGrid extends AbstractSearchGrid{

	public BasicSearchGrid(String sysPrefix) {
		super(sysPrefix);
		
		showContextMenu = false;
		isCustomizable = false;
		showTags = false;
	}

	public GridColumnView getViewId() {
		return GridColumnView.SearchGrid;
	}

}
