package commons.com.tbitsGlobal.utils.client.fieldconfigs;

import com.google.gwt.user.client.ui.Widget;
import commons.com.tbitsGlobal.utils.client.pojo.POJO;

/**
 * 
 * @author sourabh
 *
 * @param <D> The data type of the field. e.g. Int, Bool, Text, String, Date, Type, Multivalue
 * @param <V> The {@link Widget} to be returned
 * 
 * An interface to be used by forms for drawing, filling, and retreiving value to and from fields
 */
public interface IFieldConfig<D, V extends Widget> {
	/**
	 * @return the name of the field
	 */
	public String getName();
	
	/**
	 * Clears the value
	 */
	public void clear();
	
	/**
	 * @param <T>
	 * @return The {@link POJO} value from the field
	 */
	public <T extends POJO<D>> T getPOJO();
	
	/**
	 * @param <T>
	 * @param pojo. Sets the {@link POJO} value to the field
	 */
	public <T extends POJO<D>> void setPOJO(T pojo);
	
	/**
	 * @return. The widget to be added to the form
	 */
	public V getWidget();
	
	public void setWidget(V widget);
}
