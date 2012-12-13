package commons.com.tbitsGlobal.utils.client.GridCellRenderers;

import java.util.HashMap;

import com.extjs.gxt.ui.client.core.DomQuery;
import com.extjs.gxt.ui.client.core.El;
import com.extjs.gxt.ui.client.data.ModelData;
import com.extjs.gxt.ui.client.event.GridEvent;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.grid.ColumnData;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.extjs.gxt.ui.client.widget.grid.GridCellRenderer;
import com.google.gwt.user.client.Element;
import commons.com.tbitsGlobal.utils.client.ClickableLink;

/**
 * 
 * @author sourabh
 *
 * @param <M>
 * 
 * The cell renderer to display {@link ClickableLink}s
 */
public abstract class LinkCellRenderer<M extends ModelData> implements GridCellRenderer<M>{
	
	protected HashMap<String, ClickableLink> linkMap;
	
	public LinkCellRenderer() {
		linkMap = new HashMap<String, ClickableLink>();
	}
	
	public abstract Object render(M model, String property, ColumnData config,
			int rowIndex, int colIndex, ListStore<M> store,
			Grid<M> grid);
	
	protected void addLink(ClickableLink link){
		linkMap.put(link.getClassName(), link);
	}

	public void executeListeners(GridEvent<ModelData> e){
		El el = El.fly(e.getTarget());
        Element elem = el.dom;
        
		for(String className : linkMap.keySet()){
			if(DomQuery.is(elem, className)){
				ClickableLink link = linkMap.get(className);
				if(link != null)
					link.executeListeners(e);
			}
		}
	}
}
