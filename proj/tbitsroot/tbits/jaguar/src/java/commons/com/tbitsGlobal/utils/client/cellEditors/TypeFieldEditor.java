package commons.com.tbitsGlobal.utils.client.cellEditors;

import com.extjs.gxt.ui.client.event.SelectionChangedListener;
import com.extjs.gxt.ui.client.widget.grid.CellEditor;
import commons.com.tbitsGlobal.utils.client.bafield.BAFieldCombo;
import commons.com.tbitsGlobal.utils.client.domainObjects.TypeClient;
import commons.com.tbitsGlobal.utils.client.widgets.TypeFieldControl;

/**
 * 
 * @author sourabh
 * 
 * A cell editor for Type Fields
 */
public class TypeFieldEditor extends CellEditor{

	private TypeFieldControl typeFieldControl;
	
	private TypeFieldEditor(TypeFieldControl field) {		
		super(field);
		this.setTypeFieldControl(field);
	}
	
	public static TypeFieldEditor newInstance(BAFieldCombo baField){
		return new TypeFieldEditor(new TypeFieldControl(baField));
	}
		
	@Override
	public TypeClient preProcessValue(Object value) {
		TypeFieldControl field = (TypeFieldControl)this.getField();
		return field.getModelForStringValue((String) value);
	}
	
	@Override
	public String postProcessValue(Object value) {
		TypeFieldControl field = (TypeFieldControl)this.getField();
		TypeClient type = field.getValue();
		if(type != null){
			String stringValue = type.getName();
			return stringValue;
		}
		return null;
	}
	
	public void addSelectionChangedListener(SelectionChangedListener<TypeClient> listener) {
		TypeFieldControl field = (TypeFieldControl)this.getField();
		field.addSelectionChangedListener(listener);
	}

	public void setTypeFieldControl(TypeFieldControl typeFieldControl) {
		this.typeFieldControl = typeFieldControl;
	}

	public TypeFieldControl getTypeFieldControl() {
		return typeFieldControl;
	}
}
