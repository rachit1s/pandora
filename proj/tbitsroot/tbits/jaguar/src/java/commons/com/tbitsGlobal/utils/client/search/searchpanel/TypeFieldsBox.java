package commons.com.tbitsGlobal.utils.client.search.searchpanel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.extjs.gxt.ui.client.data.ModelData;
import com.extjs.gxt.ui.client.store.TreeStore;
import com.extjs.gxt.ui.client.widget.treepanel.TreePanel;
import com.google.gwt.user.client.Element;

import commons.com.tbitsGlobal.utils.client.ClientUtils;
import commons.com.tbitsGlobal.utils.client.IFixedFields;
import commons.com.tbitsGlobal.utils.client.bafield.BAField;
import commons.com.tbitsGlobal.utils.client.bafield.BAFieldCombo;
import commons.com.tbitsGlobal.utils.client.domainObjects.TypeClient;
import commons.com.tbitsGlobal.utils.client.log.Log;

/**
 * Box for providing search on all the Type Fields.
 * 
 * @author sourabh
 *
 */
public class TypeFieldsBox extends TreePanel<ModelData> implements ISearchBox, IFixedFields{
	/**
	 * Array of items checked by default
	 */
	private List<FilterParam> checkedByDefault;
	
	private List<BAField> fields;
	
	/**
	 * Constructor.
	 * 
	 * @param store
	 */
	public TypeFieldsBox(TreeStore<ModelData> store, List<BAField> fields) {
		super(store);
		
		this.setDisplayProperty(FilterField.DISPLAY_NAME);  
	    this.setCheckable(true);  
	    this.setAutoLoad(true);  
	    this.setTrackMouseOver(true);
//	    this.setCheckStyle(CheckCascade.CHILDREN);
	    this.setStyleAttribute("overflow", "hidden");
	    this.setStyleAttribute("borderBottom", "2px solid #99BBE8");
	    
	    this.fields = fields;
	}
	
	
	protected void onRender(Element target, int index) {
		super.onRender(target, index);
		
		resetToDefault();
	}
	
	private FilterField getTypeField(BAFieldCombo field){
		FilterField fieldFolder = new FilterField(field.getName(), field.getDisplayName());
		List<TypeClient> types = field.getTypes();
		List<TypeClient> checkedTypes = field.getCheckedValues();
		List<String> queryParams = ClientUtils.queryParamsforKey("j.tsp");
		for(TypeClient type : types){
			FilterParam filterParam = new FilterParam(type.getName(), type.getDisplayName(), field.getName());
			if(!queryParams.contains(type.getName())){
				fieldFolder.add(filterParam);
				if(checkedTypes.contains(type))
					this.checkedByDefault.add(filterParam);
			}
		}
		return fieldFolder;
	}

	public HashMap<String, List<String>> getSearchParams(SearchParamType spt) {
		HashMap<String, List<String>> searchParams = new HashMap<String, List<String>>();
		if(spt.equals(SearchParamType.NON_TEXT)){
			List<ModelData> models = this.getStore().getModels();
			List<ModelData> selection = this.getCheckedSelection();
			for(ModelData model : models){
				if(model instanceof FilterField){
					if(selection.containsAll(((FilterField) model).getChildren())){
						Log.info("All Types in Field : " + model.get(FilterField.DISPLAY_NAME) + " selected");
						selection.removeAll(((FilterField) model).getChildren());
					}
				}
			}
			for(ModelData d : selection){
				if(d instanceof FilterField) continue;
				FilterParam param = (FilterParam) d;
				if(searchParams.containsKey(param.getParentName())){
					searchParams.get(param.getParentName()).add("\"" + param.getName() + "\"");
				}else{
					ArrayList<String> params = new ArrayList<String>();
					params.add("\"" + param.getName() + "\"");
					searchParams.put(param.getParentName(), params);
				}
			}
		}
		return searchParams;
	}
	
	public HashMap<SearchParamType, String> getDQL() {
		HashMap<SearchParamType, String> dql = new HashMap<SearchParamType, String>();
		dql.put(SearchParamType.NON_TEXT, TbitsSearchPanel.getDQL(this.getSearchParams(SearchParamType.NON_TEXT)));
		return dql;
	}

	
	public void resetToDefault() {
		this.store.removeAll();
		this.checkedByDefault = new ArrayList<FilterParam>();
		
		FilterField model = new FilterField();
		
		for(BAField field : fields){
			if(field instanceof BAFieldCombo && field.isCanViewInBA() && field.isCanSearch()){
				FilterField fieldFolder = getTypeField((BAFieldCombo) field);
				model.add(fieldFolder);
			}
		}
		this.store.add(model.getChildren(), true);
		
		// Check those needed to be checked
		for(FilterParam param : checkedByDefault){
			this.setChecked(param, true);
		}
	}
}
