package corrGeneric.com.tbitsGlobal.client.extensions;

import com.extjs.gxt.ui.client.widget.layout.FitData;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.google.gwt.user.client.Element;
import com.tbitsGlobal.admin.client.utils.LinkIdentifier;
import com.tbitsGlobal.admin.client.widgets.APTabItem;

/**
 * Page for the "User Map View" tab under "Correspondence"
 * Constructs and populates the grid with user map properties for
 * the ba and the selected user. Also handles addtion/deletion/modification of 
 * properties from the same table.
 * @author devashish
 *
 */
public class UserMapView extends APTabItem {
	
	public UserMapView(LinkIdentifier linkId) {
		super(linkId);
		this.setBorders(false);
		this.setLayout(new FitLayout());
		this.setClosable(true);
	}
	
	public void onRender(Element parent, int index){
		super.onRender(parent, index);
		
		UserMapPanel cp = new UserMapPanel();
		this.add(cp, new FitData());
	}
	
}
