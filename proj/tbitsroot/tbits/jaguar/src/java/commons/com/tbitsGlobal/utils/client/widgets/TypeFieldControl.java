package commons.com.tbitsGlobal.utils.client.widgets;

import java.util.List;

import com.extjs.gxt.ui.client.Style.SortDir;
import com.extjs.gxt.ui.client.data.BaseListLoadResult;
import com.extjs.gxt.ui.client.data.BaseListLoader;
import com.extjs.gxt.ui.client.data.MemoryProxy;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.ListViewEvent;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.store.Store;
import com.extjs.gxt.ui.client.store.StoreFilter;
import com.extjs.gxt.ui.client.widget.form.ComboBox;
import com.google.gwt.user.client.Element;
import commons.com.tbitsGlobal.utils.client.bafield.BAFieldCombo;
import commons.com.tbitsGlobal.utils.client.domainObjects.TypeClient;

/**
 * 
 * @author sourabh
 * 
 * Control to be used for type fields
 */
public class TypeFieldControl extends ComboBox<TypeClient>{
	private TypeFieldStoreFilter filter;
	private BAFieldCombo baField;
	
	/**
	 * 
	 * @author sourabh
	 * 
	 */
	private class TypeFieldStoreFilter implements StoreFilter<TypeClient>{
		TypeClient model;
		
		public TypeFieldStoreFilter(TypeClient model) {
			this.model = model;
		}

		public boolean select(Store<TypeClient> store, TypeClient parent, TypeClient item, String property) {
			if(item.equals(model))
				return true;
			return false;
		}
	}
	
	/**
	 * Constructor
	 * @param baField. The Type Field
	 */
	public TypeFieldControl(BAFieldCombo baField) {
		super();
		
		this.baField = baField;
		
		this.setName(this.baField.getName());
		this.setFieldLabel(this.baField.getDisplayName());
		this.setLabelStyle("font-weight:bold");
		this.setDisplayField(TypeClient.DISPLAY_NAME);
    	
		List<TypeClient> types = this.baField.getTypes();
		// TODO : nitiraj : we have to use some kind of way to load the list lazily.
		// For now removing this option to resolve the LnT issue of prefilling not working.
		// But the lists will be loaded slowly for longer lists
//		if (types.size() > 100) {
//			this
//					.setStore(new ListStore<TypeClient>(
//							new BaseListLoader<BaseListLoadResult<TypeClient>>(
//									new MemoryProxy<BaseListLoadResult<TypeClient>>(
//											new BaseListLoadResult<TypeClient>(
//													types))))
//													);
//		} else 
		{
			this.setStore(new ListStore<TypeClient>());
			this.getStore().add(types);
		}
		
		// Set the default value
    	if(this.baField.getDefaultValue() != null){
    		TypeClient model = this.baField.getDefaultValue();
    		if(model != null)
    			this.setValue(model);
    	}
    	
    	// Sort by ordering
    	this.getStore().sort(TypeClient.ORDERING, SortDir.ASC);
    	
    	this.getListView().addListener(Events.OnMouseOver, new Listener<ListViewEvent<TypeClient>>(){
			public void handleEvent(ListViewEvent<TypeClient> be) {
				if (be.getIndex() != -1) {
					Element e = be.getListView().getElement(be.getIndex());
					TypeClient model = be.getListView().getStore().getAt(be.getIndex());
					if(e != null && model != null){
						e.setTitle(model.getDisplayName());
					}
				}
			}});
	}
	
	/**
	 * Hide all except one at index
	 * @param index
	 */
	public void hideAllOthers(int index){
		this.getStore().removeFilter(filter);
		final TypeClient model = this.getStore().getAt(index);
		if(model != null){
			filter = new TypeFieldStoreFilter(model);
			this.getStore().addFilter(filter);
			this.setValue(model);
		}
	}
	
	/**
	 * Set the type given by the value
	 * @param value
	 */
	public void setStringValue(String value) {
		TypeClient model = this.getModelForStringValue(value);
		if(model != null)
			this.setValue(model);
	}
	
	/**
	 * Find out type corresponding to value
	 * @param value
	 * @return
	 */
	public TypeClient getModelForStringValue(String value){
		if(value != null){
//			this.getStore().getLoader().load();
			return this.getStore().findModel(TypeClient.NAME, value);
		}
		return null;
	}
	
	/**
	 * Get name of type selected
	 * @return
	 */
	public String getStringValue(){
		TypeClient model = this.getValue();
		if(model == null)
			return null;
		return model.getName();
	}

	public BAFieldCombo getBaField() {
		return baField;
	}
	
	@Override
	public void setRawValue(String text) {
		super.setRawValue(text);
	}
	
	@Override
	public void setValue(TypeClient value) {
		super.setValue(value);
		
		if(value != null)
			this.setToolTip(value.getDisplayName());
	}
}
