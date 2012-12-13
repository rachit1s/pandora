package commons.com.tbitsGlobal.utils.client.search.searchpanel;

import java.io.Serializable;

import com.extjs.gxt.ui.client.data.BaseTreeModel;

public class FilterField extends BaseTreeModel implements Serializable  {
	private static final long serialVersionUID = 1L;
	
	public static String NAME = "name";
	public static String DISPLAY_NAME = "displayName";
	
	public FilterField() {
		
	}
	
	public FilterField(String name, String displayame) {
		set(NAME, name);
		set(DISPLAY_NAME, displayame);
	}
	
	public FilterField(String name, String displayame, BaseTreeModel[] children) {
	  this(name, displayame);
	  for (int i = 0; i < children.length; i++) {
	    add(children[i]);
	  }
	}
	
	public String getName() {
	  return (String) get(NAME);
	}
	
	public String getDisplayName() {
	  return (String) get(DISPLAY_NAME);
	}
	
	public String toString() {
	  return getDisplayName();
	}
}
