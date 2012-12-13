package commons.com.tbitsGlobal.utils.client.bulkupdate;

import java.util.HashMap;
import java.util.List;

import com.extjs.gxt.ui.client.data.ModelData;

public interface ISingleBulkGridContainer<M extends ModelData> extends IBulkGridContainer<M> {
	/**
	 * Shows the status of records after submit.
	 * 
	 * @param statusMap
	 */
	public void showStatus(HashMap<Integer, M> statusMap);
	
	/**
	 * Sets a value to all the selected models.
	 * 
	 * @param field
	 * @param value
	 */
	public void setColumnValue(String property, Object value);
	
	/**
	 * @return Selected models
	 */
	public List<M> getSelectedModels();
	
	/**
	 * Updates the requests which have been added or updated
	 */
	public void updateModels();
}
