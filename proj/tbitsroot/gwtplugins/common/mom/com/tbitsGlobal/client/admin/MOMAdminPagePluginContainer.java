package mom.com.tbitsGlobal.client.admin;

import java.util.List;

import com.tbitsGlobal.admin.client.plugins.AbstractPagePluginContainer;
import com.tbitsGlobal.admin.client.widgets.APPageLink;

public class MOMAdminPagePluginContainer extends AbstractPagePluginContainer {

	protected String getCaption() {
		return "MOM";
	}

	protected List<APPageLink> getPageLinks() {
		return MOMAdmLinks.getInstance().getPageList();
	}

}
