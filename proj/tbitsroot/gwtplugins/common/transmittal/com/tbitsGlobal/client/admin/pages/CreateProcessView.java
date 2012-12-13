package transmittal.com.tbitsGlobal.client.admin.pages;

import java.util.HashMap;

import transmittal.com.tbitsGlobal.client.models.TrnCreateProcess;

import com.extjs.gxt.ui.client.store.GroupingStore;
import com.extjs.gxt.ui.client.widget.layout.FitData;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.google.gwt.user.client.Element;
import com.tbitsGlobal.admin.client.utils.LinkIdentifier;
import com.tbitsGlobal.admin.client.widgets.APTabItem;

/**
 * Tab item to display properties of existing processes or
 * newly created process
 * @author devashish
 *
 */
public class CreateProcessView extends APTabItem {

	protected HashMap<String, String> trnProperties;
	protected GroupingStore<TrnCreateProcess> store;
	
	public CreateProcessView(LinkIdentifier linkId) {
		super(linkId);
		this.setClosable(true);
		this.setBorders(false);
		
		this.setLayout(new FitLayout());
	}

	
	protected void onRender(Element parent, int index) {
		super.onRender(parent, index);
		
		CreateProcessPanel cp = new CreateProcessPanel();
		this.add(cp, new FitData());
	}
}
