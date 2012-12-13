package admin.com.tbitsglobal.admin.client.trn.tabs;

import admin.com.tbitsglobal.admin.client.trn.bulkgridpanels.ProcessBulkUpdateGridPanel;

import com.extjs.gxt.ui.client.widget.TabItem;
import com.extjs.gxt.ui.client.widget.layout.FitData;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.google.gwt.user.client.Element;

public class CreateModifyProcessTab extends TabItem{
	
	public CreateModifyProcessTab() {
		super("Create/Modify Process");
		
//		this.setClosable(true);
		this.setLayout(new FitLayout());
	}
	
	@Override
	protected void onRender(Element parent, int index) {
		super.onRender(parent, index);
		
		ProcessBulkUpdateGridPanel cp = new ProcessBulkUpdateGridPanel();
		this.add(cp, new FitData());
	}
}
