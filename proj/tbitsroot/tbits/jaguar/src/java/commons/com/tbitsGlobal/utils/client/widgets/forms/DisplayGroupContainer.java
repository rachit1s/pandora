package commons.com.tbitsGlobal.utils.client.widgets.forms;

import java.util.HashMap;

import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.form.FieldSet;
import com.extjs.gxt.ui.client.widget.layout.ColumnData;
import com.extjs.gxt.ui.client.widget.layout.ColumnLayout;
import commons.com.tbitsGlobal.utils.client.bafield.BAField;
import commons.com.tbitsGlobal.utils.client.domainObjects.DisplayGroupClient;

public class DisplayGroupContainer extends FieldSet{
	protected HashMap<String, LayoutContainer> fieldPanels;
	
	public DisplayGroupContainer(DisplayGroupClient group) {
		super();
		
		this.setCollapsible(true);
		this.setHeading(group.getDisplayName());
		this.setLayout(new ColumnLayout());
		this.setLayoutOnChange(true);
		
		fieldPanels = new HashMap<String, LayoutContainer>();
	}
	
	protected boolean add(BAField baField, LayoutContainer fieldPanel) {
		fieldPanels.put(baField.getName(), fieldPanel);
		return this.add(fieldPanel, new ColumnData(baField.getDisplaySize()));
	}
	
	public boolean hasField(BAField field){
		return fieldPanels.get(field.getName()) != null;
	}
	
	public boolean reDrawField(BAField baField, LayoutContainer fieldPanel){
		LayoutContainer currentFieldPanel = fieldPanels.get(baField.getName());
		
		if(currentFieldPanel != null){
			int index = this.indexOf(currentFieldPanel);
			
			currentFieldPanel.removeFromParent();
			
			fieldPanels.put(baField.getName(), fieldPanel);
			return this.insert(fieldPanel, index, new ColumnData(baField.getDisplaySize()));
		}
		
		return false;
	}
}
