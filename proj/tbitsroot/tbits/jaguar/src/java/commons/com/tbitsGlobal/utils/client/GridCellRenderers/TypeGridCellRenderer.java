package commons.com.tbitsGlobal.utils.client.GridCellRenderers;

import java.util.List;

import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.grid.ColumnData;
import com.extjs.gxt.ui.client.widget.grid.Grid;

import commons.com.tbitsGlobal.utils.client.Mode;
import commons.com.tbitsGlobal.utils.client.TbitsTreeRequestData;
import commons.com.tbitsGlobal.utils.client.bafield.BAFieldCombo;
import commons.com.tbitsGlobal.utils.client.domainObjects.TypeClient;

/**
 * 
 * @author sourabh
 * 
 * Cell renderer for Type fields
 */
public class TypeGridCellRenderer extends AbstractFieldCellRenderer<TbitsTreeRequestData, BAFieldCombo> {

	public TypeGridCellRenderer(BAFieldCombo field) {
		super(Mode.VIEW, field);
	}

	@Override
	public Object render(TbitsTreeRequestData model, String property,
			ColumnData config, int rowIndex, int colIndex,
			ListStore<TbitsTreeRequestData> store,
			Grid<TbitsTreeRequestData> grid) {
		String value = model.getAsString(property);
		List<TypeClient> types = field.getTypes();
		
		if(value != null){
			for(TypeClient type : types){
				if(type.getName().equals(value))
					return type.getDisplayName();
			}
		}
		
		return "";
	}

}
