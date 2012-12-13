package commons.com.tbitsGlobal.utils.client.bulkupdate;

import com.extjs.gxt.ui.client.data.ModelData;

/**
 * 
 * @author sourabh
 *
 * @param <M>
 * 
 * To be implemented by all Bulk Grid Panels
 */
public interface IBulkUpdatePanel<M extends ModelData> {
	/**
	 * @return Get single grid container
	 */
	public ISingleBulkGridContainer<M> getSingleGridContainer();
	
	/**
	 * @return Get Common grid container
	 */
	public ICommonBulkGridContainer<M> getCommonGridContainer();
}
