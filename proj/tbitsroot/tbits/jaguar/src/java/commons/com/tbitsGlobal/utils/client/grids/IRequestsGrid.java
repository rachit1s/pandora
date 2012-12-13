package commons.com.tbitsGlobal.utils.client.grids;

import com.extjs.gxt.ui.client.widget.grid.Grid;

import commons.com.tbitsGlobal.utils.client.TbitsTreeRequestData;

/**
 * 
 * @author sourabh
 *
 * Interface to be implemented by all grids that display requests
 */
public interface IRequestsGrid {
	/**
	 * The view id serves as an identifier for a view. It is important when we define user preferences in term of columns and their widths
	 * @return the view id.
	 */
	public GridColumnView getViewId();
	
	/**
	 * Returns the grid instance
	 * @return
	 */
	public Grid<TbitsTreeRequestData> getGrid();
}
