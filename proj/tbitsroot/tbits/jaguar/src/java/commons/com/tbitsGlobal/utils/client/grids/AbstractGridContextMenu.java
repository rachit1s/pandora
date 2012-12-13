package commons.com.tbitsGlobal.utils.client.grids;

import java.util.ArrayList;

import com.extjs.gxt.ui.client.widget.Component;
import com.extjs.gxt.ui.client.widget.menu.Menu;
import com.extjs.gxt.ui.client.widget.menu.MenuItem;
import commons.com.tbitsGlobal.utils.client.TbitsTreeRequestData;

/**
 * 
 * @author sourabh
 * 
 * Abstract class for Context Menus to be shown in grids
 */
public abstract class AbstractGridContextMenu extends Menu{
	protected ArrayList<GridMenuItem> items;
	protected TbitsTreeRequestData selModel;
	protected String property;
	
	/**
	 * Grid over which it is to be shown
	 */
	protected IRequestsGrid iGrid;
	
	public AbstractGridContextMenu(IRequestsGrid iGrid){
		super();
		
		this.iGrid = iGrid;
		
		items = new ArrayList<GridMenuItem>();
	}
	
	/**
	 * Calculates which items to show and which to hide
	 * @param data
	 * @param property
	 */
	public void calculateItemsAndrender(TbitsTreeRequestData data, String property){
		this.selModel = data;
		this.property = property;
		
		for(MenuItem item : items){
			if(((GridMenuItem) item).toBeDisplayed(data))
				item.show();
			else
				item.hide();
		}
	}
	
	@Override
	public boolean add(Component item) {
		items.add((GridMenuItem) item);
		return super.add(item);
	}
	
	public boolean add(Component item, boolean addToList){
		if(addToList)
			return add(item);
		else
			return super.add(item);
	}
}
