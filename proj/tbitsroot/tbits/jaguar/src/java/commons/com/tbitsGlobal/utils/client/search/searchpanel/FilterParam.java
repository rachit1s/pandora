package commons.com.tbitsGlobal.utils.client.search.searchpanel;

import com.extjs.gxt.ui.client.data.BaseTreeModel;

public class FilterParam extends BaseTreeModel {
	private static final long serialVersionUID = 1L;
	
	public static String PARENT_NAME = "parentName";

	public FilterParam(String name, String displayName, String parentName) {
	  set(FilterField.NAME, name);
	  set(FilterField.DISPLAY_NAME, displayName);
	  set(PARENT_NAME, parentName);
	}
	
	public String getName() {
	  return (String) get(FilterField.NAME);
	}

	public String getDisplayName() {
	  return (String) get(FilterField.DISPLAY_NAME);
	}
	
	public String getParentName() {
	  return (String) get(PARENT_NAME);
	}

	public String toString() {
	  return getDisplayName();
	}
}
