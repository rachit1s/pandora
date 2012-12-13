package commons.com.tbitsGlobal.utils.client.bulkupdate;

import java.util.List;

import com.extjs.gxt.ui.client.data.ModelData;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.layout.FitData;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.google.gwt.user.client.Element;
import commons.com.tbitsGlobal.utils.client.IFixedFields;

/**
 * 
 * @author sourabh
 * 
 * Abstract container for {@link BulkUpdateGridAbstract}
 * @param <M>
 */
public abstract class AbstractBulkGridContainer<M extends ModelData> extends ContentPanel implements IBulkGridContainer<M>, IFixedFields{
	/**
	 * Contained grid
	 */
	protected BulkUpdateGridAbstract<M> bulkGrid;
	
	/**
	 * Constructor.
	 */
	protected AbstractBulkGridContainer(BulkUpdateGridAbstract<M> bulkGrid) {
		super();
		this.setLayout(new FitLayout());
		this.setHeaderVisible(false);
		this.setLayoutOnChange(true);
		
		this.bulkGrid = bulkGrid;
	}
	
	@Override
	protected void onRender(Element parent, int pos) {
		super.onRender(parent, pos);
		
		this.add(bulkGrid, new FitData());
	}
	
	public void addModel(M model){
		beforeAdd(model);
		this.bulkGrid.getStore().add(model);
	}
	
	public void addModel(List<M> models){
		for(M model : models){
			addModel(model);
		}
	}
	
	/**
	 * Called before a model is added to the grid
	 * The default implementation does not do anything.
	 * @param model
	 */
	protected void beforeAdd(M model){}
	
	public void removeAllModels(){
		this.bulkGrid.getStore().removeAll();
	}
	
	public List<M> getModels(){
		return this.bulkGrid.getStore().getModels();
	}
	
	public BulkUpdateGridAbstract<M> getBulkGrid() {
		return bulkGrid;
	}
}
