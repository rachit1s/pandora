package commons.com.tbitsGlobal.utils.client.bulkupdate;

import com.extjs.gxt.ui.client.data.ModelData;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.GridEvent;
import com.extjs.gxt.ui.client.widget.grid.CheckColumnConfig;
import commons.com.tbitsGlobal.utils.client.bulkupdate.BulkUpdateGridAbstract.BulkGridMode;

/**
 * 
 * @author sourabh
 * 
 * {@link CheckColumnConfig} to be used in Bulk Grids
 */
public class BulkGridCheckColumnConfig extends CheckColumnConfig{
	
	private BulkGridMode gridMode;
	
	public BulkGridCheckColumnConfig(BulkGridMode gridMode) {
		super();
		
		this.gridMode = gridMode;
	}
	
	@Override
	protected void onMouseDown(GridEvent<ModelData> ge) {
		super.onMouseDown(ge);
		
		if(gridMode != null && gridMode == BulkGridMode.COMMON){
			String cls = ge.getTarget().getClassName();
			if (cls != null && cls.indexOf("x-grid3-cc-" + getId()) != -1 && cls.indexOf("disabled") == -1) {
				GridEvent<ModelData> event = new GridEvent<ModelData>(ge.getGrid());
				event.setProperty(this.getId());
				int index = grid.getView().findRowIndex(ge.getTarget());
			    ModelData m = grid.getStore().getAt(index);
			    Boolean b = (Boolean) m.get(getDataIndex());
				event.setValue(b);
				
				grid.fireEvent(Events.AfterEdit, event);
			}
		}
	}
}
