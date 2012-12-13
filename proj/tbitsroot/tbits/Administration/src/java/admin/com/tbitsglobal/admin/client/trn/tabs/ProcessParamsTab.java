package admin.com.tbitsglobal.admin.client.trn.tabs;

import admin.com.tbitsglobal.admin.client.trn.bulkgridpanels.ProcessParamsBulkUpdateGridPanel;

import com.extjs.gxt.ui.client.widget.TabItem;
import com.extjs.gxt.ui.client.widget.layout.FitData;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.google.gwt.user.client.Element;

public class ProcessParamsTab extends TabItem {
	public ProcessParamsTab() {
		super("Process Parameters");
		
//		this.setClosable(true);
		this.setLayout(new FitLayout());
	}
	
	@Override
	protected void onRender(Element parent, int index) {
		super.onRender(parent, index);
		
		ProcessParamsBulkUpdateGridPanel cp = new ProcessParamsBulkUpdateGridPanel();
		this.add(cp, new FitData());
	}
}
