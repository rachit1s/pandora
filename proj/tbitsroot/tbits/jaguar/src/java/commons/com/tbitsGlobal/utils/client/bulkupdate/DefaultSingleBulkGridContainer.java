package commons.com.tbitsGlobal.utils.client.bulkupdate;

import java.util.HashMap;

import com.extjs.gxt.ui.client.data.ModelData;

public class DefaultSingleBulkGridContainer<M extends ModelData> extends AbstractSingleBulkGridContainer<M> {

	public DefaultSingleBulkGridContainer(BulkUpdateGridAbstract<M> bulkGrid) {
		super(bulkGrid);
	}

	public void showStatus(HashMap<Integer, M> statusMap) {
	}

	public void updateModels() {
	}

}
