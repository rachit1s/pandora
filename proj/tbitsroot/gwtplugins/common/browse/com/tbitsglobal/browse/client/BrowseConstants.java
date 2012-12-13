package browse.com.tbitsglobal.browse.client;

import com.google.gwt.core.client.GWT;

public interface BrowseConstants {
	public final BrowseServiceAsync browseService = GWT.create(BrowseService.class);
}
