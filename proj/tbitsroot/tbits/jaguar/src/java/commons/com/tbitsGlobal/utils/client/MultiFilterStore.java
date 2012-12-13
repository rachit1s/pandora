/**
 * 
 */
package commons.com.tbitsGlobal.utils.client;

import java.util.ArrayList;
import java.util.List;

import com.extjs.gxt.ui.client.store.ListStore;

/**
 * 
 * Allows you to apply filter on more than one properties of ModelData
 * @author dheeru
 * 
 */
public class MultiFilterStore<M extends TbitsModelData> extends ListStore<M> {

	private List<String> filterProperties = new ArrayList<String>(); 

	private String filterBeginsWidth;
	
	public MultiFilterStore() {
		super();
	}

	public void addToFilterProperties(String property) {
		filterProperties.add(property);
	}

	public void setFilterProperties(List<String> properties) {
		this.filterProperties = properties;
	}
	
	public List<String> getFilterProperties() {
		return this.filterProperties;
	}

	/**
	 * Filters the store using the given properties.
	 * 
	 * @param properties
	 *            the properties to filter by
	 * @param beginsWith
	 *            a string the value should begin with
	 */
	public void filter(String properties[], String beginsWith) {
		if (properties == null || properties.length == 0)
			return;
		filterProperty = properties[0];
		filterBeginsWidth = beginsWith;
		for (String property : properties) {
			if (!filterProperties.contains(property))
				filterProperties.add(property);
		}
		applyFilters(filterProperty);
	}

	/**
	 * Filters the store using the given property.
	 * 
	 * @param property the property to filter by
	 */
	public void filter(String property) {
		filterProperty = property;
		filterBeginsWidth = null;
		if (!filterProperties.contains(property))
			filterProperties.add(property);
		applyFilters(property);
	}

	/**
	 * Filters the store using the given property.
	 * 
	 * @param property the property to filter by
	 * @param beginsWith a string the value should begin with
	 */
	public void filter(String property, String beginsWith) {
		filterProperty = property;
		if (!filterProperties.contains(property))
			filterProperties.add(property);
		filterBeginsWidth = beginsWith;
		applyFilters(property);
	}

	/**
	 * Applies the current filters to the store. Allows you to apply filter on
	 * more than one propertyField of ModelData
	 * 
	 * @param property
	 *             the optional active property on which filter will
	 *            be applied in case if filterProperties are not set otherwise
	 *            filter will be applied on all filterProperties
	 */
	@Override
	public void applyFilters(String filterProperty) {
		String properties[] = null;
		if (this.filterProperties.size() != 0) {
			properties = new String[filterProperties.size()];
			for (int i = 0; i < filterProperties.size(); i++) {
				properties[i] = filterProperties.get(i);
			}
		} else {
			properties = new String[1];
			properties[0] = filterProperty;
		}

		if (filters != null && filters.size() == 0) {
			return;
		}
		this.filterProperty = filterProperty;
		if (!filtersEnabled) {
			snapshot = all;
		}

		filtersEnabled = true;
		filtered = new ArrayList<M>();

		for (String property : properties) {
			for (M items : snapshot) {
				if (filterBeginsWidth != null) {
					Object o = items.get(property);
					if (o != null) {
						if (!o.toString().toLowerCase().startsWith(filterBeginsWidth.toLowerCase())) {
							continue;
						}
					} else {
						continue;
					}
				}
				if (!isFiltered(items, property)) {
					if (!filtered.contains(items))
						filtered.add(items);
				}
			}
		}
		all = filtered;

		if (storeSorter != null) {
			applySort(false);
		}

		fireEvent(Filter, createStoreEvent());
	}
}
