package transmittal.com.tbitsGlobal.client.admin.pages;


import com.extjs.gxt.ui.client.widget.layout.FitData;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.google.gwt.user.client.Element;
import com.tbitsGlobal.admin.client.utils.LinkIdentifier;
import com.tbitsGlobal.admin.client.widgets.APTabItem;

public class SrcTargetFieldMapView extends APTabItem {

	public SrcTargetFieldMapView(LinkIdentifier linkId) {
		super(linkId);
		
		this.setClosable(true);
		this.setBorders(false);
		this.setLayout(new FitLayout());
	}
	
	@Override
	protected void onRender(Element parent, int index) {
		super.onRender(parent, index);
		
		SrcTargetFieldMapPanel cp = new SrcTargetFieldMapPanel();
		this.add(cp, new FitData());
	}

}
