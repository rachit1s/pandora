package commons.com.tbitsGlobal.utils.client.GridCellRenderers;

import com.extjs.gxt.ui.client.data.ModelData;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.grid.ColumnData;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.extjs.gxt.ui.client.widget.grid.GridCellRenderer;

/**
 * 
 * @author sourabh
 *
 * @param <M>
 * 
 * Cell renderer for text type fields
 */
public class WrapGridCellRenderer<M extends ModelData> implements GridCellRenderer<M>{

	public Object render(M model, String property, ColumnData config,
			int rowIndex, int colIndex, ListStore<M> store, Grid<M> grid) {
		Object obj = model.get(property);
		if(obj == null)
			return "";
		
		String value = "";
		if(obj instanceof String){
			value = (String)obj;
		}else
			value = obj.toString();
		
		return value;
	}
}
