package commons.com.tbitsGlobal.utils.client.bulkupdate;

import java.util.List;

import com.extjs.gxt.ui.client.data.ModelData;

/**
 * 
 * @author sourabh
 *
 * Container for grid in single mode
 * @param <M>
 */
public abstract class AbstractSingleBulkGridContainer<M extends ModelData> extends AbstractBulkGridContainer<M> implements ISingleBulkGridContainer<M>{

	protected AbstractSingleBulkGridContainer(BulkUpdateGridAbstract<M> bulkGrid) {
		super(bulkGrid);
	}
	
	public void setColumnValue(String property, Object value){
		List<M> models = getSelectedModels();
		if(models == null || models.size() == 0){
			models = bulkGrid.getStore().getModels();
		}
		
		for(M model : models){
			int index = bulkGrid.getStore().indexOf(model);
			model.set(property, value);
			bulkGrid.getTbitsGridView().refreshRow(index);
		}
	}
	
	public List<M> getSelectedModels(){
		return bulkGrid.getSelectionModel().getSelectedItems();
	}
	
}
