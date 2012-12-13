package mom.com.tbitsGlobal.client.admin;

import com.extjs.gxt.ui.client.widget.layout.FitData;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.google.gwt.user.client.Element;
import com.tbitsGlobal.admin.client.utils.LinkIdentifier;
import com.tbitsGlobal.admin.client.widgets.APTabItem;

public class MOMTemplatesView extends APTabItem {

	public MOMTemplatesView(LinkIdentifier linkId) {
		super(linkId);
		
		this.setClosable(true);
		this.setBorders(false);
		
		this.setLayout(new FitLayout());
	}

	
	protected void onRender(Element parent, int index) {
		super.onRender(parent, index);
		
		MOMTemplatesPanel cp = new MOMTemplatesPanel();
		this.add(cp, new FitData());
	}
}
