package transmittal.com.tbitsGlobal.client.admin.pages;


import com.extjs.gxt.ui.client.widget.layout.FitData;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.google.gwt.user.client.Element;
import com.tbitsGlobal.admin.client.utils.LinkIdentifier;
import com.tbitsGlobal.admin.client.widgets.APTabItem;

/**
 * Tab item for displaying/manipulating Transmittal Processes.
 * @author devashish
 *
 */
public class ProcessParamsView extends APTabItem {

	public ProcessParamsView(LinkIdentifier linkId) {
		super(linkId);	
		this.setClosable(true);
		this.setBorders(false);
		this.setLayout(new FitLayout());	
	}
	
	@Override
	protected void onRender(Element parent, int index) {
		super.onRender(parent, index);
		
		ProcessParamsPanel cp = new ProcessParamsPanel();
		this.add(cp, new FitData());
	}

}
