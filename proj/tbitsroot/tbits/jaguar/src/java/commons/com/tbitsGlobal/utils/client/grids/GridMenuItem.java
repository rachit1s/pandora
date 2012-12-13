package commons.com.tbitsGlobal.utils.client.grids;

import java.util.ArrayList;
import java.util.HashMap;

import com.extjs.gxt.ui.client.util.IconHelper;
import com.extjs.gxt.ui.client.widget.menu.MenuItem;
import commons.com.tbitsGlobal.utils.client.IconConstants;
import commons.com.tbitsGlobal.utils.client.TbitsTreeRequestData;

/**
 * 
 * @author sourabh
 * 
 * Menu item for {@link AbstractGridContextMenu}
 */
public class GridMenuItem extends MenuItem {
	private HashMap<String, ArrayList<String>> values;
	
	private boolean needsSeparator = false;
	
	public GridMenuItem() {
		super();
		values = new HashMap<String, ArrayList<String>>();
	}
	
	public GridMenuItem(String text){
		this();
		this.setText(text);
		this.setIcon(IconHelper.create(IconConstants.PLUS));
	}
	
	/**
	 * Adds the value of a property for which it would be visible
	 * @param property
	 * @param value
	 */
	public void addValue(String property, String value){
		if(values.get(property) != null){
			values.get(property).add(value);
		}else{
			ArrayList<String> arr = new ArrayList<String>();
			arr.add(value);
			values.put(property, arr);
		}
	}
	
	public boolean removeValue(String property, String value){
		if(values.get(property) != null){
			return values.get(property).remove(value);
		}
		return false;
	}
	
	/**
	 * @param model
	 * @return True if the item has to be display over a model
	 */
	public boolean toBeDisplayed(TbitsTreeRequestData model){
		if(validate()){
			if(values.size() == 0)
				return true;
			if(model == null)
				return false;
			for(String s : values.keySet()){
				String dataValue = model.getAsString(s);
				if(dataValue != null && dataValue != "" && values.get(s).contains(dataValue))
					return true;
			}
		}
		return false;
	}

	public void setNeedsSeparator(boolean needsSeparator) {
		this.needsSeparator = needsSeparator;
	}

	public boolean needsSeparator() {
		return needsSeparator;
	}
	
	protected boolean validate(){
		return true;
	}
}
