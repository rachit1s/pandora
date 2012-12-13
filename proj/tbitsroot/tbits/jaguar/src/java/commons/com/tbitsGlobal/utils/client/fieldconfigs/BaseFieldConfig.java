package commons.com.tbitsGlobal.utils.client.fieldconfigs;

import com.google.gwt.user.client.ui.Widget;
import commons.com.tbitsGlobal.utils.client.bafield.BAField;

/**
 * 
 * @author sutta
 *
 * @param <D> Data Type
 * @param <V>
 * 
 * Base Config for {@link BAField}s
 */
public abstract class BaseFieldConfig<D, V extends Widget> implements IFieldConfig<D, V>{
	protected BAField baField;
	protected V field ;
	public BaseFieldConfig(BAField baField) {
		this.baField = baField;
	}
	
	public BAField getBaField() {
		return baField;
	}

	public String getName() {
		return this.baField.getName();
	}
	
	public void setWidget( V widget )
	{
		field = widget ;
	}
	
	public V getWidget()
	{
		return field;
	}
}
