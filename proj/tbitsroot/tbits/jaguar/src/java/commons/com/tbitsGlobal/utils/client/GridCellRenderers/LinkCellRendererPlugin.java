package commons.com.tbitsGlobal.utils.client.GridCellRenderers;

import com.extjs.gxt.ui.client.data.ModelData;
import com.extjs.gxt.ui.client.event.EventType;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.GridEvent;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.widget.Component;
import com.extjs.gxt.ui.client.widget.ComponentPlugin;
import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
import com.extjs.gxt.ui.client.widget.grid.GridCellRenderer;

/**
 * 
 * @author sourabh
 * 
 * Plugin that adds a listener on grids on cellClick that triggers {@link LinkCellRenderer}
 */
public class LinkCellRendererPlugin implements ComponentPlugin{

	public void init(Component component) {
		Listener<GridEvent<ModelData>> gridListener = new Listener<GridEvent<ModelData>>() {
		      public void handleEvent(GridEvent<ModelData> e) {
		        EventType type = e.getType();
		        if (type == Events.CellClick) {
		          e.cancelBubble();

		          ColumnConfig col = e.getGrid().getColumnModel().getColumn(e.getColIndex());
		          GridCellRenderer<ModelData> renderer = col.getRenderer();
		          if(renderer != null && renderer instanceof LinkCellRenderer){
		        	  ((LinkCellRenderer)renderer).executeListeners(e);
		          }
		        }
		      }
		    };
		    
		component.addListener(Events.CellClick, gridListener);
	}

}
