package commons.com.tbitsGlobal.utils.client;

import com.extjs.gxt.ui.client.widget.form.Field;
import com.extjs.gxt.ui.client.widget.grid.CellEditor;

/**
 * 
 * @author sourabh
 * 
 * Created to set revertInvalid = false
 * 
 * Must be used by all editor grid implementations
 */
public class TbitsCellEditor extends CellEditor{

	public TbitsCellEditor(Field<? extends Object> field) {
		super(field);
		
		this.setRevertInvalid(false);
	}
	
}
