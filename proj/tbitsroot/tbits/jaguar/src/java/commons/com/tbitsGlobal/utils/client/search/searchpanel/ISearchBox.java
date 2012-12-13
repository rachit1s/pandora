package commons.com.tbitsGlobal.utils.client.search.searchpanel;

import java.util.HashMap;
import java.util.List;

/**
 * 
 * @author sutta
 * 
 * Interface to be implemented by all panels that provide DQL for search
 */
public interface ISearchBox {
	
	public enum SearchParamType{
		TEXT, NON_TEXT
	}
	
	/**
	 * @return Search params to be added to DQL
	 */
	public HashMap<String, List<String>> getSearchParams(SearchParamType spt);
	
	/**
	 * @return Get DQL
	 */
	public HashMap<SearchParamType, String> getDQL();
	
	/**
	 * Resets the panel to default
	 */
	public void resetToDefault();
}
