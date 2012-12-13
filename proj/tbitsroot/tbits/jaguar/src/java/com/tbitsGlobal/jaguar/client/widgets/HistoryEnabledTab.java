package com.tbitsGlobal.jaguar.client.widgets;

import java.util.ArrayList;
import java.util.HashMap;

import com.extjs.gxt.ui.client.widget.TabItem;
import commons.com.tbitsGlobal.utils.client.urlManager.HistoryToken;
import commons.com.tbitsGlobal.utils.client.urlManager.TbitsURLManager;

/**
 * {@link TabItem} that carries a key and a value.
 * @author sourabh
 *
 */
public class HistoryEnabledTab extends TabItem {
	private String key;
	private String value;
	
	private HashMap<String, ArrayList<HistoryEnabledTab>> tabMap;
	
	public HistoryEnabledTab() {
		super();
		this.setClosable(true);
	}
	
	public HistoryEnabledTab(String title){
		this();
		this.setText(title);
	}
	
	public HistoryEnabledTab(String title, String key, String value, HashMap<String, ArrayList<HistoryEnabledTab>> tabMap) {
		this(title);
		this.key = key;
		this.value = value;
		this.tabMap = tabMap;
	}

	protected void onDetach() {
		super.onDetach();
		ArrayList<HistoryEnabledTab> tabArr = tabMap.get(key);
		if(tabArr != null){
			HistoryEnabledTab tempTab = null;
			for(HistoryEnabledTab tab : tabArr){
				if(tab.getValue().equals(value)){
					tempTab = tab;
					break;
				}
			}
			if(tempTab != null)
				tabArr.remove(tempTab);
		}
		TbitsURLManager.getInstance().removeToken(new HistoryToken(key, value, true));
	}
	
	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}
	
	public void refresh(){}
}
