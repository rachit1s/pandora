package mom.com.tbitsGlobal.client;

import com.google.gwt.core.client.GWT;
import com.tbitsGlobal.admin.client.utils.LinkIdentifier;

import mom.com.tbitsGlobal.client.service.MOMAdminService;
import mom.com.tbitsGlobal.client.service.MOMAdminServiceAsync;

public interface MOMAdminConstants {
	public final MOMAdminServiceAsync momAdminService = GWT.create(MOMAdminService.class);
	
	public static final LinkIdentifier MOM_TEMPLATES = new LinkIdentifier("MOM Templates", "mtemp");
}
