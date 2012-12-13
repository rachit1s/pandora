package commons.com.tbitsGlobal.utils.client.grids;

import com.extjs.gxt.ui.client.data.ModelData;
import com.extjs.gxt.ui.client.event.ColumnModelEvent;
import com.extjs.gxt.ui.client.event.ComponentEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.store.Store;
import com.extjs.gxt.ui.client.store.StoreEvent;
import com.extjs.gxt.ui.client.store.StoreListener;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.extjs.gxt.ui.client.widget.layout.FitData;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.google.gwt.user.client.Element;

/**
 * 
 * @author sutta
 * 
 * A container which contains grid changes its height according to amount of data in the grid
 */
@SuppressWarnings("unchecked")
public class AutoHeightGridContainer extends ContentPanel {
	protected Grid grid;
	
	public AutoHeightGridContainer(Grid grid, boolean collapsible, String heading) {
		this(grid, collapsible);
		
		if(heading != null && !heading.equals("")){
			this.setHeaderVisible(true);
			this.setHeading(heading);
		}
	}
	
	public AutoHeightGridContainer(Grid grid, boolean collapsible) {
		super();
		this.setHeaderVisible(false);
		
		this.setStyleAttribute("marginBottom", "5px");
		this.setLayout(new FitLayout());
		this.setCollapsible(collapsible);
		
		this.grid = grid;
	}
	
	@Override
	protected void onRender(Element parent, int pos) {
		super.onRender(parent, pos);
		
		grid.addListener(Events.ViewReady, new Listener<ComponentEvent>() {  
			public void handleEvent(ComponentEvent be) {  
			    grid.getStore().addListener(Store.Add, new Listener<StoreEvent>() {  
			      public void handleEvent(StoreEvent be) {  
			        doAutoHeight();  
			      }  
			    });  
			    doAutoHeight();  
			  }  
		});  
		
		grid.addListener(Events.ColumnResize, new Listener<ComponentEvent>() {  
		  public void handleEvent(ComponentEvent be) {  
		    doAutoHeight();  
		  }  
		});  
		
		grid.getColumnModel().addListener(Events.HiddenChange, new Listener<ColumnModelEvent>() {  
		  public void handleEvent(ColumnModelEvent be) {  
		    doAutoHeight();  
		  }  
		});
		
		grid.getStore().addStoreListener(new StoreListener<ModelData>(){
			@Override
			public void storeRemove(StoreEvent<ModelData> se) {
				super.storeRemove(se);
				doAutoHeight();
			}
		});
		    
		this.add(grid, new FitData());
	}
	
	protected void doAutoHeight() {  
		if (grid.isViewReady()) {  
			grid.getView().getBody().setStyleAttribute("overflowY", "hidden");
			int height = (grid.getView().getBody().isScrollableX() ? 19 : 0) + grid.el().getFrameWidth("tb")  
	          + grid.getView().getHeader().getHeight() + this.getFrameHeight()  
	          + grid.getView().getBody().firstChild().getHeight();
			this.setHeight(height);  
	    }
	}
}
