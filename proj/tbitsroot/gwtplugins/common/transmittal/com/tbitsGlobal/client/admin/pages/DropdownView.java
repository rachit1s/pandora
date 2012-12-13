package transmittal.com.tbitsGlobal.client.admin.pages;

import com.extjs.gxt.ui.client.widget.layout.FitData;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.google.gwt.user.client.Element;
import com.tbitsGlobal.admin.client.utils.LinkIdentifier;
import com.tbitsGlobal.admin.client.widgets.APTabItem;

public class DropdownView extends APTabItem {

	public DropdownView(LinkIdentifier linkId) {
		super(linkId);
		
		this.setClosable(true);
		this.setBorders(false);
		
		this.setLayout(new FitLayout());
	}
	
	protected void onRender(Element parent, int index){
		super.onRender(parent, index);
		
		DropdownPanel cp = new DropdownPanel();
		this.add(cp, new FitData());
		
	}

}
