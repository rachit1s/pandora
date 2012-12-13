package commons.com.tbitsGlobal.utils.client.search.grids;


import commons.com.tbitsGlobal.utils.client.grids.RequestsViewGridContainer;

/**
 * 
 * @author sourabh
 * 
 * abstract container panel for {@link AbstractSearchGrid}
 */
public abstract class AbstractSearchGridContainer extends RequestsViewGridContainer {

	/**
	 * Constructor
	 * 
	 * @param grid
	 */
	public AbstractSearchGridContainer(String sysPrefix, AbstractSearchGrid grid) {
		super(sysPrefix, grid);
		
		this.setHeaderVisible(false);
	}

	@Override
	public AbstractSearchGrid getGrid() {
		return (AbstractSearchGrid) super.getGrid();
	}

}
