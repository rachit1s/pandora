package admin.com.tbitsglobal.admin.client.trn.tabs;

import admin.com.tbitsglobal.admin.client.trn.bulkgridpanels.PostProcessBulkUpdateGridPanel;

import com.extjs.gxt.ui.client.widget.TabItem;
import com.extjs.gxt.ui.client.widget.layout.FitData;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.google.gwt.user.client.Element;

public class PostProcessValuesTab extends TabItem{
	public PostProcessValuesTab() {
		super("Post Process Field Values");
		
//		this.setClosable(true);
		this.setLayout(new FitLayout());	
	}
	
	@Override
	protected void onRender(Element parent, int index) {
		super.onRender(parent, index);
		
		PostProcessBulkUpdateGridPanel cp = new PostProcessBulkUpdateGridPanel();
		this.add(cp, new FitData());
	}
}
