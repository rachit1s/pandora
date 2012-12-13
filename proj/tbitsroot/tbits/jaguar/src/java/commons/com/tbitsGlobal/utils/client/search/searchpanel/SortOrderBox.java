package commons.com.tbitsGlobal.utils.client.search.searchpanel;

import java.util.List;

import com.extjs.gxt.ui.client.data.ModelData;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.store.TreeStore;
import com.extjs.gxt.ui.client.widget.Label;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.form.ComboBox;
import com.extjs.gxt.ui.client.widget.form.SimpleComboBox;
import com.extjs.gxt.ui.client.widget.layout.ColumnData;
import com.extjs.gxt.ui.client.widget.layout.ColumnLayout;
import com.extjs.gxt.ui.client.widget.layout.FitData;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.google.gwt.user.client.Element;

import commons.com.tbitsGlobal.utils.client.IFixedFields;
import commons.com.tbitsGlobal.utils.client.bafield.BAField;
import commons.com.tbitsGlobal.utils.client.bafield.BAFieldAttachment;
import commons.com.tbitsGlobal.utils.client.bafield.BAFieldTextArea;
import commons.com.tbitsGlobal.utils.client.domainObjects.PermissionClient;

public class SortOrderBox extends LayoutContainer {
	SimpleComboBox<String> minOrder;
	ComboBox<BAField> dropDown;
	Label label;
	BAField requestIdField = null;
	public SortOrderBox(TreeStore<ModelData> store, List<BAField> fields) {
		this.setLayout(new ColumnLayout());
		
		label = new Label("Sort By");
		
		dropDown = new ComboBox<BAField>();
		ListStore<BAField> fieldStore = new ListStore<BAField>();
		fieldStore.removeAll();
		
		for(BAField field : fields){
			boolean canView = ((field.getUserPerm() & PermissionClient.VIEW) != 0);
			
			if(field.getName().equalsIgnoreCase(IFixedFields.REQUEST))
			{
				requestIdField = field;
				canView = true; //forcing the request_id to be order by
			}
			
			if(!canView || field instanceof BAFieldAttachment || field instanceof BAFieldTextArea)
				continue;
			
			fieldStore.add(field);
		}
		dropDown.setStore(fieldStore);
		dropDown.setEditable(false);
		dropDown.setValue(requestIdField);
		dropDown.setDisplayField(BAField.DISPLAY_NAME);
		
		minOrder = new SimpleComboBox<String>();
		minOrder.add("DESC");
		minOrder.add("ASC");
		minOrder.setEditable(false);
		minOrder.setSimpleValue("DESC");
	}

	protected void onRender(Element parent, int index) {
		super.onRender(parent, index);
		
		LayoutContainer fitLayoutContainer = new LayoutContainer(new FitLayout());
		fitLayoutContainer.add(label, new FitData(1));
		this.add(fitLayoutContainer, new ColumnData(.2));
		
		fitLayoutContainer = new LayoutContainer(new FitLayout());
		fitLayoutContainer.add(dropDown, new FitData(1));
		this.add(fitLayoutContainer, new ColumnData(.5));
		
		fitLayoutContainer = new LayoutContainer(new FitLayout());
		fitLayoutContainer.add(minOrder, new FitData(1));
		this.add(fitLayoutContainer, new ColumnData(.2));
	}
	
	public String getSortingQuery() {
		return getSortBy() + " " + getSortDir();
	}
	
	public String getSortBy()
	{
		return dropDown.getSelection().get(0).getName();
	}
	
	public String getSortDir()
	{
		return minOrder.getValue().getValue();
	}
	
	public void reset()
	{
		dropDown.setValue(requestIdField);
		minOrder.setSimpleValue("DESC");
	}
}
