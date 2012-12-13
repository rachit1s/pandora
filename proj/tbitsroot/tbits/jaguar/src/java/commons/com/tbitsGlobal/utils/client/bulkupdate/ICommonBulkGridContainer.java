package commons.com.tbitsGlobal.utils.client.bulkupdate;

import com.extjs.gxt.ui.client.data.ModelData;

public interface ICommonBulkGridContainer<M extends ModelData> extends IBulkGridContainer<M> {
	/**
	 * Sets the value specified into the column with the specified property in Single Bulk Grid
	 * @param property
	 * @param value
	 */
	public void setColumnValue(String property, Object value);
}
