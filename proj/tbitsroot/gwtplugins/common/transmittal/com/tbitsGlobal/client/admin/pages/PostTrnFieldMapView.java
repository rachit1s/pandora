package transmittal.com.tbitsGlobal.client.admin.pages;

import transmittal.com.tbitsGlobal.client.admin.TrnAdminConstants;

import com.extjs.gxt.ui.client.widget.layout.FitData;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.google.gwt.user.client.Element;
import com.tbitsGlobal.admin.client.utils.LinkIdentifier;
import com.tbitsGlobal.admin.client.widgets.APTabItem;

/**
 * Page for displaying post transmittal field map
 * @author devashish
 *
 */
public class PostTrnFieldMapView extends APTabItem {

	public PostTrnFieldMapView(LinkIdentifier linkId) {
		super(linkId);
		
		
		this.setClosable(true);
		this.setBorders(false);
		this.setLayout(new FitLayout());
	}
	
	protected void onRender(Element parent, int index) {
		super.onRender(parent, index);
		
		PostTrnFieldMapPanel cp = new PostTrnFieldMapPanel();
		this.add(cp, new FitData());
	}

}
