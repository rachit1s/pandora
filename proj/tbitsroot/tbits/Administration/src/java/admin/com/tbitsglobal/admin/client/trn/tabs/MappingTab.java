package admin.com.tbitsglobal.admin.client.trn.tabs;

import admin.com.tbitsglobal.admin.client.trn.bulkgridpanels.MappingBulkUpdateGridPanel;

import com.extjs.gxt.ui.client.widget.TabItem;
import com.extjs.gxt.ui.client.widget.layout.FitData;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.google.gwt.user.client.Element;

public class MappingTab extends TabItem{
	public MappingTab() {
		super("Source Target Field Mapping");
		
//		this.setClosable(true);
		this.setLayout(new FitLayout());	
	}
	
	@Override
	protected void onRender(Element parent, int index) {
		super.onRender(parent, index);
		
		MappingBulkUpdateGridPanel cp = new MappingBulkUpdateGridPanel();
		this.add(cp, new FitData());
	}
}
