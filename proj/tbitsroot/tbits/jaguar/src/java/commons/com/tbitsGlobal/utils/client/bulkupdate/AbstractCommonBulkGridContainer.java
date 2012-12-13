package commons.com.tbitsGlobal.utils.client.bulkupdate;

import com.extjs.gxt.ui.client.data.ModelData;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.GridEvent;
import com.extjs.gxt.ui.client.event.Listener;
import commons.com.tbitsGlobal.utils.client.UIContext.UIContext;

/**
 * 
 * @author sourabh
 * 
 * Container for bulk grid in common mode
 * @param <M>
 */
public abstract class AbstractCommonBulkGridContainer<M extends ModelData> extends AbstractBulkGridContainer<M> implements ICommonBulkGridContainer<M>{
	
	private UIContext myContext;

	protected AbstractCommonBulkGridContainer(UIContext myContext, BulkUpdateGridAbstract<M> bulkGrid) {
		super(bulkGrid);
		
		this.myContext = myContext;
		
		bulkGrid.addListener(Events.AfterEdit, new Listener<GridEvent<M>>(){
			public void handleEvent(GridEvent<M> be) {
				String property = be.getProperty();
				Object value = be.getValue();
				
				setColumnValue(property, value);
			}});
	}
	
	public void setColumnValue(String property, Object value){
		ISingleBulkGridContainer gridContainer = myContext.getValue(BulkUpdateGridAbstract.CONTEXT_SINGLE_GRID_CONTAINER, ISingleBulkGridContainer.class);
		gridContainer.setColumnValue(property, value);
	}

}
