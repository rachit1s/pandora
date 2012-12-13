package transmittal.com.tbitsGlobal.client.admin;

import java.util.List;

import transmittal.com.tbitsGlobal.client.extensions.AdmLinks;

import com.tbitsGlobal.admin.client.plugins.AbstractPagePluginContainer;
import com.tbitsGlobal.admin.client.widgets.APPageLink;

public class TrnAdmPagePluginContainer extends AbstractPagePluginContainer {

	@Override
	protected String getCaption() {
		return "Transmittal";
	}

	@Override
	protected List<APPageLink> getPageLinks() {
		return AdmLinks.getInstance().getPageList();
	}

}
