package commons.com.tbitsGlobal.utils.client.bulkupdate;

import java.util.List;

import com.extjs.gxt.ui.client.data.ModelData;

/**
 * 
 * @author sourabh
 *
 * @param <M>
 * 
 * To be implemented by all Bulk Grid Containers
 */
public interface IBulkGridContainer<M extends ModelData> {
	/**
	 * Adds a {@link ModelData} to the store and original set of models.
	 * @param model
	 */
	public void addModel(M model);
	
	/**
	 * Adds a list of {@link ModelData} to the store and original set of models.
	 * @param models
	 */
	public void addModel(List<M> models);
	
	/**
	 * Removes all models from bulkgrid
	 */
	public void removeAllModels();
	
	/**
	 * @return the Models in grid
	 */
	public List<M> getModels();
	
	/**
	 * @return The Bulk Grid
	 */
	public BulkUpdateGridAbstract<M> getBulkGrid();
}
