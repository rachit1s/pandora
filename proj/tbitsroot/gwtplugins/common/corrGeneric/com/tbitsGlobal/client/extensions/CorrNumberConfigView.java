package corrGeneric.com.tbitsGlobal.client.extensions;

import com.extjs.gxt.ui.client.widget.layout.FitData;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.google.gwt.user.client.Element;
import com.tbitsGlobal.admin.client.utils.LinkIdentifier;
import com.tbitsGlobal.admin.client.widgets.APTabItem;

public class CorrNumberConfigView extends APTabItem {
	

	public CorrNumberConfigView(LinkIdentifier linkId) {
		super(linkId);
		this.setClosable(true);
		this.setBorders(false);
		this.setLayout(new FitLayout());
	}

	public void onRender(Element parent, int index){
		super.onRender(parent, index);
		
		CorrNumberConfigPanel cp = new CorrNumberConfigPanel();
		this.add(cp, new FitData());
	}
}
