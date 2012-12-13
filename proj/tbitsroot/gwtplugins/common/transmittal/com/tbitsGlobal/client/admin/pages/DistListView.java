package transmittal.com.tbitsGlobal.client.admin.pages;

import com.extjs.gxt.ui.client.widget.layout.FitData;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.google.gwt.user.client.Element;
import com.tbitsGlobal.admin.client.utils.LinkIdentifier;
import com.tbitsGlobal.admin.client.widgets.APTabItem;

public class DistListView extends APTabItem {

	public DistListView(LinkIdentifier linkId) {
		super(linkId);
		
		this.setClosable(true);
		this.setBorders(false);
		this.setLayout(new FitLayout());
	}

	protected void onRender(Element parent, int index) {
		super.onRender(parent, index);
		
		DistListPanel cp = new DistListPanel();
		this.add(cp, new FitData());
	}
}
