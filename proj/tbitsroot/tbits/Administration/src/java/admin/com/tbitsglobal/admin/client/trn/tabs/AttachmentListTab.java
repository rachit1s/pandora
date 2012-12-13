package admin.com.tbitsglobal.admin.client.trn.tabs;

import admin.com.tbitsglobal.admin.client.trn.bulkgridpanels.AttachmentListBulkUpdateGridPanel;

import com.extjs.gxt.ui.client.widget.TabItem;
import com.extjs.gxt.ui.client.widget.layout.FitData;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.google.gwt.user.client.Element;

public class AttachmentListTab extends TabItem{
	public AttachmentListTab() {
		super("Attachment List");
		
//		this.setClosable(true);
		this.setLayout(new FitLayout());
	}
	
	@Override
	protected void onRender(Element parent, int index) {
		super.onRender(parent, index);
		
		AttachmentListBulkUpdateGridPanel cp = new AttachmentListBulkUpdateGridPanel();
		this.add(cp, new FitData());
	}
}
