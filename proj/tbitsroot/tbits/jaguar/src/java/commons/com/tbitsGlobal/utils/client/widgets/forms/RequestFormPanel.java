package commons.com.tbitsGlobal.utils.client.widgets.forms;

import java.util.HashMap;

import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.layout.FormData;
import com.extjs.gxt.ui.client.widget.layout.FormLayout;
import commons.com.tbitsGlobal.utils.client.bafield.BAField;
import commons.com.tbitsGlobal.utils.client.domainObjects.DisplayGroupClient;

public class RequestFormPanel extends ContentPanel{
	
	protected HashMap<Integer, DisplayGroupContainer> displayGroups;
	
	public RequestFormPanel() {
		super();
		
		this.setHeaderVisible(false);
		this.setBodyBorder(false);
		this.setLayout(new FormLayout());
		
		displayGroups = new HashMap<Integer, DisplayGroupContainer>();
	}
	
	protected boolean add(DisplayGroupClient group, DisplayGroupContainer item) {
		displayGroups.put(group.getId(), item);
		return super.add(item, new FormData("-20"));
	}
	
	public int hasField(BAField field){
		for(int groupId : displayGroups.keySet()){
			DisplayGroupContainer groupContainer = displayGroups.get(groupId);
			if(groupContainer.hasField(field))
				return groupId;
		}
		
		return 0;
	}
	
	public boolean reDrawField(int groupId, BAField field, LayoutContainer fieldPanel){
		DisplayGroupContainer groupContainer = displayGroups.get(groupId);
		if(groupContainer != null){
			return groupContainer.reDrawField(field, fieldPanel);
		}
		
		return false;
	}
}
