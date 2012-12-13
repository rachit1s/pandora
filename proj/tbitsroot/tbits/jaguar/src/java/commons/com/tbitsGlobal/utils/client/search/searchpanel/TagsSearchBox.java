package commons.com.tbitsGlobal.utils.client.search.searchpanel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.extjs.gxt.ui.client.widget.LayoutContainer;

import commons.com.tbitsGlobal.utils.client.tags.TagsUtils;
import commons.com.tbitsGlobal.utils.client.tags.TagsViewPanel;

public class TagsSearchBox extends LayoutContainer implements ISearchBox {

	private TagsViewPanel tvp;
	
	public TagsSearchBox(){
		
		super();
		
		resetToDefault();
	}
	
	public HashMap<SearchParamType, String> getDQL() {
		HashMap<SearchParamType, String> dql = new HashMap<SearchParamType, String>();
		dql.put(SearchParamType.NON_TEXT, TbitsSearchPanel.getDQL(this.getSearchParams(SearchParamType.NON_TEXT)));
		return dql;
	}

	public HashMap<String, List<String>> getSearchParams(SearchParamType spt) {
		HashMap<String, List<String>> searchParams = new HashMap<String, List<String>>();
	    if(spt.equals(SearchParamType.NON_TEXT)){
			if(tvp != null){
				HashMap<String, List<String>> selectedTags = tvp.getSelectedTags();
				ArrayList<String> tags = new ArrayList<String>();
				for(String tag : selectedTags.get(TagsUtils.PRIVATE)){
					tags.add("\""+tag+"\"");
				}
				searchParams.put(TagsUtils.PRIVATE_TAGS_FIELD_FILTER, tags);
				tags = new ArrayList<String>();
				for(String tag : selectedTags.get(TagsUtils.PUBLIC)){
					tags.add("\""+tag+"\"");
				}
				searchParams.put(TagsUtils.PUBLIC_TAGS_FIELD_FILTER, tags);
			}
	    }
       return searchParams; 
	}

	public void resetToDefault() {
		this.removeAll();
		
		tvp = new TagsViewPanel();
		tvp.setAutoHeight(true);
		tvp.setAutoWidth(true);
		tvp.setBorders(false);
		tvp.setAnimCollapse(false);
		tvp.setHeaderVisible(false);
		tvp.disableButtons();
		
		this.add(tvp);
		this.layout();
	}

	
}
