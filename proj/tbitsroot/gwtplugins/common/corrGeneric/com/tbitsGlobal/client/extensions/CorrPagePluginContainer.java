package corrGeneric.com.tbitsGlobal.client.extensions;

import java.util.List;

import com.tbitsGlobal.admin.client.plugins.AbstractPagePluginContainer;
import com.tbitsGlobal.admin.client.widgets.APPageLink;

public class CorrPagePluginContainer extends AbstractPagePluginContainer {

	@Override
	protected String getCaption() {
		return "Correspondence";
	}

	@Override
	protected List<APPageLink> getPageLinks() {
		return CorrLinks.getInstance().corrLinksList;
	}

}
