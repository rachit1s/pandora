package commons.com.tbitsGlobal.utils.client.search.searchpanel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.extjs.gxt.ui.client.event.ComponentEvent;
import com.extjs.gxt.ui.client.event.KeyListener;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.form.SimpleComboBox;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.extjs.gxt.ui.client.widget.layout.ColumnData;
import com.extjs.gxt.ui.client.widget.layout.ColumnLayout;
import com.extjs.gxt.ui.client.widget.layout.FitData;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.Window;

import commons.com.tbitsGlobal.utils.client.IFixedFields;

/**
 * Box to provide keyword search.
 * 
 * @author sourabh
 *
 */
public class KeywordBox extends LayoutContainer implements ISearchBox, IFixedFields {
	private TextField<String> text;
	private SimpleComboBox<String> combo;
	
	/**
	 * Constructor
	 */
	public KeywordBox() {
		this.setLayout(new ColumnLayout());
//		this.setStyleAttribute("padding", "3px");
		this.setStyleAttribute("borderBottom", "2px solid #99BBE8");
		
		text = new TextField<String>();
		text.setEmptyText("Search");
		
		combo = new SimpleComboBox<String>();
		combo.add("Subject");
		combo.add("All Text");
		combo.add("Summary");
		combo.add("Text + Attachments");
		combo.setSimpleValue("Subject");
	}
	
	
	public void addKeyListener(KeyListener listener)
	{
		text.addKeyListener(listener);
	}
	
	public void removeKeyListener(KeyListener listener)
	{
		text.removeKeyListener(listener);
	}
	
	protected void onRender(Element parent, int index) {
		super.onRender(parent, index);
		
		LayoutContainer textContainer = new LayoutContainer(new FitLayout());
		
		textContainer.add(text, new FitData(2));
		this.add(textContainer, new ColumnData(0.5));
		
		LayoutContainer comboContainer = new LayoutContainer(new FitLayout());
		
		comboContainer.add(combo, new FitData(2));
		this.add(comboContainer, new ColumnData(0.44));
	}
	
	public HashMap<String, List<String>> getSearchParams(SearchParamType spt) {
		HashMap<String, List<String>> searchParams = new HashMap<String, List<String>>();
		if(spt.equals(SearchParamType.TEXT)){
			List<String> params = new ArrayList<String>();
			String text = this.text.getValue();
			if(text != null && !text.equals("")){
				String[] vals = text.split(" ");
				for(String val : vals){
					if(!val.equals(""))
						params.add("\"" + val.trim() + "\"");
				}
			}
			String option = this.combo.getSimpleValue();
			if(params.size() > 0){
				if(option.equals("Subject")){
					searchParams.put(SUBJECT, params);
				}else if(option.equals("All Text")){
					searchParams.put("alltext", params);
				}else if(option.equals("Summary")){
					searchParams.put(SUMMARY, params);
				}else if(option.equals("Text + Attachments")){
					searchParams.put("all", params);
				}
			}
		}
		return searchParams;
	}

	public HashMap<SearchParamType, String> getDQL() {
		HashMap<SearchParamType, String> dql = new HashMap<SearchParamType, String>();
		dql.put(SearchParamType.TEXT, TbitsSearchPanel.getDQL(this.getSearchParams(SearchParamType.TEXT)));
		return dql;
	}

	
	public void resetToDefault() {
		text.setValue("");
		combo.setSimpleValue("Subject");
	}
}
