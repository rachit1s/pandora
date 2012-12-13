package transmittal.com.tbitsGlobal.client.admin.pages;

import java.util.ArrayList;
import java.util.List;

import com.extjs.gxt.ui.client.store.GroupingStore;
import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
import com.extjs.gxt.ui.client.widget.grid.ColumnModel;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.extjs.gxt.ui.client.widget.grid.GridGroupRenderer;
import com.extjs.gxt.ui.client.widget.grid.GroupColumnData;
import com.extjs.gxt.ui.client.widget.grid.GroupingView;

import transmittal.com.tbitsGlobal.client.models.TrnCreateProcess;

/**
 * Grid to hold the parameters of existing process or newly created process
 * @author devashish
 *
 */
public class CreateProcessGrid{

	protected Grid<TrnCreateProcess> 			groupGrid;
	protected GroupingStore<TrnCreateProcess> 	groupStore;
	
	public CreateProcessGrid() {

		groupStore 	= new GroupingStore<TrnCreateProcess>();
		
		createColumns();
	}

	public void emptyStore(){
		groupStore.removeAll();
	}
	
	/**
	 * Create column config
	 */
	protected void createColumns() {
		ArrayList<ColumnConfig> cm = new ArrayList<ColumnConfig>();
		
		ColumnConfig nameCol = new ColumnConfig(TrnCreateProcess.NAME, 400);
		nameCol.setHeader("Parameter");
		cm.add(nameCol);
		
		ColumnConfig valueCol = new ColumnConfig(TrnCreateProcess.VALUE, 400);
		valueCol.setHeader("Value");
		cm.add(valueCol);
		
		groupGrid = new Grid<TrnCreateProcess>(groupStore, new ColumnModel(cm));
	}

	public void populateStore(List<TrnCreateProcess> store){
		this.groupStore.add(store);
		this.groupStore.groupBy(TrnCreateProcess.GROUP);
		this.groupStore.setGroupOnSort(false);
	}
	
	public Grid<TrnCreateProcess> getGrid(){
		GroupingView view = new GroupingView();
		view.setShowGroupedColumn(false);
		view.setGroupRenderer(new GridGroupRenderer() {  
	      public String render(GroupColumnData data) {    
	        return data.group;
	      }  
	    }); 
		groupGrid.setView(view);
		
		return this.groupGrid;
	}
}
