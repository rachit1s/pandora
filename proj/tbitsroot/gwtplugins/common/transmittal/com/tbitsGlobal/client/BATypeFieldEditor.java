package transmittal.com.tbitsGlobal.client;

import com.extjs.gxt.ui.client.event.SelectionChangedListener;
import com.extjs.gxt.ui.client.widget.grid.CellEditor;
import commons.com.tbitsGlobal.utils.client.bafield.BAFieldCombo;
import commons.com.tbitsGlobal.utils.client.domainObjects.TypeClient;
import commons.com.tbitsGlobal.utils.client.widgets.TypeFieldControl;

public class BATypeFieldEditor extends CellEditor{

private TypeFieldControl typeFieldControl;
	
	private BATypeFieldEditor(TypeFieldControl field) {		
		super(field);
		this.setTypeFieldControl(field);
	}
	
	public static BATypeFieldEditor newInstance(BAFieldCombo baField){
		return new BATypeFieldEditor(new TypeFieldControl(baField));
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
		String stringValue = type.getName();
		return stringValue;
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

