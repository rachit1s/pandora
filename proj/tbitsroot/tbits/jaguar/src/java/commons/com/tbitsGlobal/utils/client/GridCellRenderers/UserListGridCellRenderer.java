package commons.com.tbitsGlobal.utils.client.GridCellRenderers;

import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.grid.ColumnData;
import com.extjs.gxt.ui.client.widget.grid.Grid;

import commons.com.tbitsGlobal.utils.client.Mode;
import commons.com.tbitsGlobal.utils.client.TbitsTreeRequestData;
import commons.com.tbitsGlobal.utils.client.bafield.BAFieldMultiValue;
import commons.com.tbitsGlobal.utils.client.pojo.POJO;
import commons.com.tbitsGlobal.utils.client.pojo.POJOString;

/**
 * 
 * @author sourabh
 * 
 * Cell renderer for User Type fields
 */
public class UserListGridCellRenderer extends AbstractFieldCellRenderer<TbitsTreeRequestData, BAFieldMultiValue>{
	
	public UserListGridCellRenderer(BAFieldMultiValue field){
		super(Mode.VIEW, field);
	}
	
	public Object render(final TbitsTreeRequestData model, String property,
			ColumnData config, final int rowIndex, int colIndex,
			ListStore<TbitsTreeRequestData> store,
			Grid<TbitsTreeRequestData> grid) {
		String userString = "";
		POJO obj = model.getAsPOJO(property);
		if(obj == null || !(obj instanceof POJOString))
			obj = new POJOString("");
		if(obj != null){
			if(model != null){
				String userIds = (String)obj.getValue();
				String[] userArr = userIds.split(",");
				for(String user : userArr){
					if(!userString.equals(""))
						userString += ", ";
					userString += user;
				}
			}
		}
		return userString;
	}
}
