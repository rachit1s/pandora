package commons.com.tbitsGlobal.utils.client.bulkupdate;

import com.extjs.gxt.ui.client.data.ModelData;
import commons.com.tbitsGlobal.utils.client.UIContext.UIContext;

public class DefaultCommonBulkGridContainer<M extends ModelData> extends AbstractCommonBulkGridContainer<M> {

	public DefaultCommonBulkGridContainer(UIContext myContext,
			BulkUpdateGridAbstract<M> bulkGrid) {
		super(myContext, bulkGrid);
	}

}
